/*
 * Kurento Android MSControl: MSControl implementation for Android.
 * Copyright (C) 2011  Tikal Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kurento.mscontrol.kas.networkconnection.internal;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Exchanger;

import android.util.Log;

import com.kurento.kas.media.codecs.AudioCodecType;
import com.kurento.kas.media.codecs.VideoCodecType;
import com.kurento.kas.media.ports.MediaPortManager;
import com.kurento.kas.media.profiles.AudioProfile;
import com.kurento.kas.media.profiles.VideoProfile;
import com.kurento.mediaspec.MediaSpec;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.Payload;
import com.kurento.mediaspec.PayloadRtp;
import com.kurento.mediaspec.SessionSpec;
import com.kurento.mediaspec.Transport;
import com.kurento.mediaspec.TransportRtp;
import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.commons.join.JoinableStream.StreamType;
import com.kurento.mscontrol.kas.internal.MediaSessionConfig;
import com.kurento.mscontrol.kas.join.AudioJoinableStreamImpl;
import com.kurento.mscontrol.kas.join.JoinableStreamBase;
import com.kurento.mscontrol.kas.join.VideoJoinableStreamImpl;
import com.kurento.mscontrol.kas.networkconnection.NetIF;
import com.kurento.mscontrol.kas.networkconnection.PortRange;

import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.test.DiscoveryTest;

public class NetworkConnectionImpl extends NetworkConnectionBase {

	public final static String LOG_TAG = "NW";

	private MediaSessionConfig mediaSessionConfig;

	private ArrayList<AudioProfile> audioProfiles;
	private ArrayList<VideoProfile> videoProfiles;

	private SessionSpec localSessionSpec;
	private SessionSpec remoteSessionSpec;

	private InetAddress publicAddress;

	private static int videoPort = -1;
	private static int audioPort = -1;

	private VideoJoinableStreamImpl videoJoinableStreamImpl;
	private AudioJoinableStreamImpl audioJoinableStreamImpl;

	private int maxAudioBitrate;

	private static boolean freePorts = true;

	@Override
	public void setLocalSessionSpec(SessionSpec arg0) {
		this.localSessionSpec = arg0;
		Log.d(LOG_TAG, "localSessionSpec:\n" + localSessionSpec);
	}

	@Override
	public void setRemoteSessionSpec(SessionSpec arg0) {
		this.remoteSessionSpec = arg0;
		Log.d(LOG_TAG, "remoteSessionSpec:\n" + remoteSessionSpec);
	}

	public NetworkConnectionImpl(MediaSessionConfig mediaSessionConfig)
			throws MsControlException {
		super();

		if (mediaSessionConfig == null)
			throw new MsControlException("Media Session Config is NULL");
		this.streams = new JoinableStreamBase[2];
		this.mediaSessionConfig = mediaSessionConfig;

		audioProfiles = getAudioProfiles(this.mediaSessionConfig);
		this.maxAudioBitrate = getMaxAudioBitrate();
		videoProfiles = getVideoProfiles(this.mediaSessionConfig,
				maxAudioBitrate);
		publicAddress = getLocalAddress();
	}

	@Override
	public void confirm() throws MsControlException {
		if (localSessionSpec == null)
			throw new MsControlException("Local session spec is null.");
		if (remoteSessionSpec == null)
			throw new MsControlException("Remote session spec is null.");

		audioJoinableStreamImpl = new AudioJoinableStreamImpl(this,
				StreamType.audio, remoteSessionSpec, localSessionSpec,
				mediaSessionConfig.getMaxDelay());
		this.streams[0] = audioJoinableStreamImpl;

		videoJoinableStreamImpl = new VideoJoinableStreamImpl(this,
				StreamType.video, this.videoProfiles, remoteSessionSpec,
				localSessionSpec, mediaSessionConfig.getMaxDelay(),
				mediaSessionConfig.getFramesQueueSize());
		this.streams[1] = videoJoinableStreamImpl;
	}

	@Override
	public void release() {
		if (videoJoinableStreamImpl != null)
			videoJoinableStreamImpl.stop();
		if (audioJoinableStreamImpl != null)
			audioJoinableStreamImpl.stop();

		synchronized (NetworkConnectionImpl.class) {
			freePorts = true;
		}
	}

	private Payload addPayload(MediaSpec mediaSpec, int id, String codecName,
			int clockRate, int bitrate, Integer channels) {
		Log.d(LOG_TAG, "addPayload: " + codecName);

		PayloadRtp rtpInfo = new PayloadRtp(id, codecName, clockRate);
		rtpInfo.setBitrate(bitrate);
		if (channels != null)
			rtpInfo.setChannels(channels);

		Payload payload = new Payload();
		payload.setRtp(rtpInfo);
		mediaSpec.addToPayloads(payload);

		Log.d(LOG_TAG, "payload: " + payload);
		Log.d(LOG_TAG, "mediaSpec: " + mediaSpec);

		return payload;
	}

	private Payload addPayload(MediaSpec mediaSpec, int id, String codecName,
			int clockRate, int bitrate) {
		return addPayload(mediaSpec, id, codecName, clockRate, bitrate, null);
	}

	private class StunThread extends Thread {
		private String stunHost;
		private int stunPort;
		private Exchanger<DiscoveryInfo> e;

		public StunThread(String stunHost, int stunPort,
				Exchanger<DiscoveryInfo> e) {
			this.stunHost = stunHost;
			this.stunPort = stunPort;
			this.e = e;
		}

		@Override
		public void run() {
			int minPort = 0;
			int maxPort = 0;
			PortRange videoPortRange = mediaSessionConfig.getVideoPortRange();
			if (videoPortRange != null) {
				minPort = videoPortRange.getMinPort();
				maxPort = videoPortRange.getMaxPort();
			}

			DiscoveryInfo info = null;
			for (int i = minPort; i <= maxPort; i++) {
				DiscoveryTest test = new DiscoveryTest(null, i, stunHost,
						stunPort);
				try {
					info = test.testPublicPorts();
					Log.d(LOG_TAG,
							"Private IP:" + info.getLocalIP() + ":"
									+ info.getLocalPort() + "\nPublic IP: "
									+ info.getPublicIP() + ":"
									+ info.getPublicPort());
					break;
				} catch (Exception e) {
					Log.w(LOG_TAG,
							"Error while taking port " + i + " " + e.toString());
					info = null;
				}
			}

			try {
				e.exchange(info);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void takeMediaResources() throws MsControlException {
		synchronized (NetworkConnectionImpl.class) {
			if (!freePorts)
				throw new MsControlException(
						"Can not take ports, they are in use.");

			Log.d(LOG_TAG, "takeMediaPortThreadSafe");
			int audioRemainder = MediaPortManager.releaseAudioLocalPort();
			int videoRemainder = MediaPortManager.releaseVideoLocalPort();

			if ((audioRemainder != 0) || (videoRemainder != 0))
				throw new MsControlException(
						"Can not take ports, they are in use.");

			String stunHost = getStunHost();

			if (!stunHost.equals("")) {
				int stunPort = getStunPort();
				Exchanger<DiscoveryInfo> audioEx = new Exchanger<DiscoveryInfo>();
				Exchanger<DiscoveryInfo> videoEx = new Exchanger<DiscoveryInfo>();
				StunThread audioStun = new StunThread(stunHost, stunPort,
						audioEx);
				StunThread videoStun = new StunThread(stunHost, stunPort,
						videoEx);

				audioStun.start();
				videoStun.start();

				DiscoveryInfo audioInfo = null;
				DiscoveryInfo videoInfo = null;
				try {
					audioInfo = audioEx.exchange(null);
					videoInfo = videoEx.exchange(null);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new MsControlException(
							"Error when retrieve public net if info.");
				}

				if (audioInfo == null || videoInfo == null)
					throw new MsControlException(
							"Error when retrieve public net if info.");

				InetAddress audioPublicIp = audioInfo.getPublicIP();
				InetAddress videoPublicIp = videoInfo.getPublicIP();
				if (audioPublicIp == null
						|| !audioPublicIp.equals(videoPublicIp))
					throw new MsControlException(
							"Error when retrieve public address.");

				publicAddress = audioPublicIp;

				int audioLocalPort = MediaPortManager
						.takeAudioLocalPort(audioInfo.getLocalPort());
				int videoLocalPort = MediaPortManager
						.takeVideoLocalPort(videoInfo.getLocalPort());

				if (audioLocalPort < 0 || videoLocalPort < 0)
					throw new MsControlException("Can not take ports.");

				audioPort = audioInfo.getPublicPort();
				videoPort = videoInfo.getPublicPort();
			} else {
				audioPort = MediaPortManager.takeAudioLocalPort();
				videoPort = MediaPortManager.takeVideoLocalPort();
			}

			if (audioPort < 0 || videoPort < 0)
				throw new MsControlException("Can not take ports.");

			freePorts = false;
		}
	}

	@Override
	public SessionSpec generateSessionSpec() throws MsControlException {
		takeMediaResources();

		if (publicAddress == null)
			throw new MsControlException("Error when retrieve public address.");

		int payloadId = 96;

		// VIDEO
		MediaSpec videoMedia = null;

		if (videoProfiles != null && videoProfiles.size() > 0) {
			int bitrate = (int) Math
					.ceil(videoProfiles.get(0).getBitRate() / 1000.0);

			TransportRtp transRtp = new TransportRtp(publicAddress
					.getHostAddress().toString(), videoPort);
			Transport trans = new Transport();
			trans.setRtp(transRtp);

			HashSet<MediaType> types = new HashSet<MediaType>();
			types.add(MediaType.VIDEO);

			com.kurento.mediaspec.Direction videoMode = com.kurento.mediaspec.Direction.SENDRECV;
			if (this.mediaSessionConfig.getMediaTypeModes() != null
					&& this.mediaSessionConfig.getMediaTypeModes().get(
							MediaType.VIDEO) != null)
				videoMode = this.mediaSessionConfig.getMediaTypeModes().get(
						MediaType.VIDEO);

			videoMedia = new MediaSpec(null, types, trans, videoMode);

			for (VideoProfile vp : videoProfiles) {
				if (VideoCodecType.MPEG4.equals(vp.getVideoCodecType()))
					addPayload(videoMedia, payloadId, "MP4V-ES", 90000, bitrate);
				else if (VideoCodecType.H263.equals(vp.getVideoCodecType()))
					addPayload(videoMedia, payloadId, "H263-1998", 90000,
							bitrate);
				else if (VideoCodecType.H264.equals(vp.getVideoCodecType()))
					addPayload(videoMedia, payloadId, "H264", 90000, bitrate);
				payloadId++;
			}
		}

		// // AUDIO
		MediaSpec audioMedia = null;

		if (audioProfiles != null && audioProfiles.size() > 0) {
			int bitrate = (int) Math.ceil(maxAudioBitrate / 1000.0);

			TransportRtp transRtp = new TransportRtp(publicAddress
					.getHostAddress().toString(), audioPort);
			Transport trans = new Transport();
			trans.setRtp(transRtp);

			HashSet<MediaType> types = new HashSet<MediaType>();
			types.add(MediaType.AUDIO);

			com.kurento.mediaspec.Direction audioMode = com.kurento.mediaspec.Direction.SENDRECV;
			if (this.mediaSessionConfig.getMediaTypeModes() != null
					&& this.mediaSessionConfig.getMediaTypeModes().get(
							MediaType.AUDIO) != null)
				audioMode = this.mediaSessionConfig.getMediaTypeModes().get(
						MediaType.AUDIO);

			audioMedia = new MediaSpec(null, types, trans, audioMode);

			for (AudioProfile ap : audioProfiles) {
				if (AudioProfile.MP2.equals(ap))
					addPayload(audioMedia, 14, "MPA", 90000, bitrate);
				else if (AudioProfile.AMR.equals(ap)) {
					Payload p = addPayload(audioMedia, payloadId, "AMR", 8000,
							bitrate, 1);
					if (p.isSetRtp())
						p.getRtp().putToExtraParams("octet-align", "1");
				} else if (AudioProfile.PCMU.equals(ap))
					addPayload(audioMedia, 0, "PCMU", 8000, bitrate);
				else if (AudioProfile.PCMA.equals(ap))
					addPayload(audioMedia, 8, "PCMA", 8000, bitrate);
				payloadId++;
			}
		}

		Log.d(LOG_TAG, "videoMedia: " + videoMedia);
		Log.d(LOG_TAG, "audioMedia: " + audioMedia);

		List<MediaSpec> medias = new ArrayList<MediaSpec>();

		if (videoMedia != null)
			medias.add(videoMedia);

		if (audioMedia != null)
			medias.add(audioMedia);

		SessionSpec session = new SessionSpec(medias, "12345");

		return session;
	}

	@Override
	public InetAddress getLocalAddress() {
		return this.mediaSessionConfig.getLocalAddress();
	}

	@Override
	public String getStunHost() {
		return this.mediaSessionConfig.getStunHost();
	}

	@Override
	public Integer getStunPort() {
		return this.mediaSessionConfig.getStunPort();
	}

	private int getMaxAudioBitrate() {
		int maxAudioBitrate = 0;
		for (AudioProfile ap : audioProfiles) {
			if (ap.getBitRate() > maxAudioBitrate)
				maxAudioBitrate = ap.getBitRate();
		}

		return maxAudioBitrate;
	}

	private ArrayList<AudioProfile> getAudioProfiles(
			MediaSessionConfig mediaSessionConfig) {
		List<AudioCodecType> audioCodecs = mediaSessionConfig
				.getAudioCodecs();

		ArrayList<AudioProfile> audioProfiles = new ArrayList<AudioProfile>(0);

		// Discard/Select phase
		if (audioCodecs == null) {// Default: all codecs
			for (AudioProfile ap : AudioProfile.values())
				audioProfiles.add(ap);
		} else {
			for (AudioProfile ap : AudioProfile.values()) {
				for (AudioCodecType act : audioCodecs) {
					if (act.equals(ap.getAudioCodecType()))
						audioProfiles.add(ap);
				}
			}
		}

		// Scoring phase
		// TODO

		return audioProfiles;
	}

	private ArrayList<VideoProfile> getVideoProfiles(
			MediaSessionConfig mediaSessionConfig, int maxAudioBitrate) {
		List<VideoCodecType> videoCodecs = mediaSessionConfig
				.getVideoCodecs();
		NetIF netIF = mediaSessionConfig.getNetIF();

		ArrayList<VideoProfile> videoProfiles = new ArrayList<VideoProfile>(0);

		// Discard/Select phase
		if (videoCodecs == null) {// Default: all codecs
			for (VideoCodecType vct : VideoCodecType.values())
				videoProfiles.add(new VideoProfile(vct, netIF.getMaxBandwidth()
						- maxAudioBitrate));
		} else {
			for (VideoCodecType vct : videoCodecs) {
				videoProfiles.add(new VideoProfile(vct, netIF.getMaxBandwidth()
						- maxAudioBitrate));
			}
		}

		// Set new attrs
		Integer maxBW = null;
		if (mediaSessionConfig.getMaxBW() != null)
			maxBW = Math.max(
					NetIF.MIN_BANDWITH,
					Math.min(netIF.getMaxBandwidth(),
							mediaSessionConfig.getMaxBW()));
		Integer maxFrameRate = null;
		if (mediaSessionConfig.getMaxFrameRate() != null)
			maxFrameRate = Math.max(1, mediaSessionConfig.getMaxFrameRate());

		Integer maxGopSize = null;
		if (mediaSessionConfig.getGopSize() != null)
			maxGopSize = Math.max(0, mediaSessionConfig.getGopSize());

		Integer width = null;
		Integer height = null;
		if (mediaSessionConfig.getFrameWidth() != null
				&& mediaSessionConfig.getFrameHeight() != null) {
			width = Math.abs((int) mediaSessionConfig.getFrameWidth());
			height = Math.abs((int) mediaSessionConfig.getFrameHeight());
		}

		for (VideoProfile vp : videoProfiles) {
			if (maxBW != null)
				vp.setBitRate(maxBW - maxAudioBitrate);
			if (maxFrameRate != null) {
				vp.setFrameRateNum(maxFrameRate);
				vp.setFrameRateDen(1);
			}
			if (maxGopSize != null)
				vp.setGopSize(maxGopSize);
			if (width != null)
				vp.setWidth(width);
			if (height != null)
				vp.setHeight(height);
		}

		// Scoring phase
		// TODO

		return videoProfiles;
	}

	@Override
	public long getBitrate(StreamType streamType,
			Direction direction) {
		if (StreamType.video.equals(streamType)
				&& videoJoinableStreamImpl != null) {
			if (Direction.SEND.equals(direction))
				return videoJoinableStreamImpl.getOutBitrate();
			else if (Direction.RECV.equals(direction))
				return videoJoinableStreamImpl.getInBitrate();
			else if (Direction.SEND.equals(direction))
				return videoJoinableStreamImpl.getOutBitrate()
						+ videoJoinableStreamImpl.getInBitrate();
		} else if (StreamType.audio.equals(streamType)
				&& audioJoinableStreamImpl != null) {
			if (Direction.SEND.equals(direction))
				return audioJoinableStreamImpl.getOutBitrate();
			else if (Direction.RECV.equals(direction))
				return audioJoinableStreamImpl.getInBitrate();
			else if (Direction.SEND.equals(direction))
				return audioJoinableStreamImpl.getOutBitrate()
						+ audioJoinableStreamImpl.getInBitrate();
		}
		return 0;
	}

}
