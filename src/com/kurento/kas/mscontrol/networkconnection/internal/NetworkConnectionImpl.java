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

package com.kurento.kas.mscontrol.networkconnection.internal;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Exchanger;

import javax.sdp.SdpException;

import android.util.Log;

import com.kurento.commons.media.format.MediaSpec;
import com.kurento.commons.media.format.PayloadSpec;
import com.kurento.commons.media.format.SessionSpec;
import com.kurento.commons.media.format.formatparameters.FormatParameters;
import com.kurento.commons.media.format.formatparameters.impl.H263FormatParameters;
import com.kurento.commons.media.format.formatparameters.impl.H263VideoProfile;
import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.join.JoinableStream.StreamType;
import com.kurento.commons.sdp.enums.MediaType;
import com.kurento.commons.sdp.enums.Mode;
import com.kurento.commons.types.Fraction;
import com.kurento.kas.media.codecs.AudioCodecType;
import com.kurento.kas.media.codecs.VideoCodecType;
import com.kurento.kas.media.ports.MediaPortManager;
import com.kurento.kas.media.profiles.AudioProfile;
import com.kurento.kas.media.profiles.VideoProfile;
import com.kurento.kas.mscontrol.internal.MediaSessionConfig;
import com.kurento.kas.mscontrol.join.AudioJoinableStreamImpl;
import com.kurento.kas.mscontrol.join.JoinableStreamBase;
import com.kurento.kas.mscontrol.join.VideoJoinableStreamImpl;
import com.kurento.kas.mscontrol.networkconnection.NetIF;

import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.test.DiscoveryTest;

public class NetworkConnectionImpl extends NetworkConnectionBase {

	private static final long serialVersionUID = 1L;
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

	@Override
	public void setLocalSessionSpec(SessionSpec arg0) {
		this.localSessionSpec = arg0;
		Log.d(LOG_TAG, "localSessionSpec:\n" + localSessionSpec);
	}

	@Override
	public void setRemoteSessionSpec(SessionSpec arg0) {
		this.remoteSessionSpec = arg0;
		Log.d(LOG_TAG, "** remoteSessionSpec:\n" + remoteSessionSpec);
	}

	public NetworkConnectionImpl(MediaSessionConfig mediaSessionConfig)
			throws MsControlException {
		super();

		if (mediaSessionConfig == null)
			throw new MsControlException("Media Session Config is NULL");
		this.streams = new JoinableStreamBase[2];
		this.mediaSessionConfig = mediaSessionConfig;

		// Process MediaConfigure and determinate media profiles
		audioProfiles = getAudioProfiles(this.mediaSessionConfig);
		this.maxAudioBitrate = getMaxAudioBitrate();
		videoProfiles = getVideoProfiles(this.mediaSessionConfig,
				maxAudioBitrate);
		publicAddress = getLocalAddress();
	}

	@Override
	public void confirm() throws MsControlException {
		if ((localSessionSpec == null) || (remoteSessionSpec == null))
			return;
		// TODO: throw some Exception eg: throw new
		// MediaException("SessionSpec corrupt");

		audioJoinableStreamImpl = new AudioJoinableStreamImpl(this,
				StreamType.audio, remoteSessionSpec, localSessionSpec);
		this.streams[0] = audioJoinableStreamImpl;

		videoJoinableStreamImpl = new VideoJoinableStreamImpl(this,
				StreamType.video, this.videoProfiles, remoteSessionSpec,
				localSessionSpec, mediaSessionConfig.getFramesQueueSize());
		this.streams[1] = videoJoinableStreamImpl;
	}

	@Override
	public void release() {
		if (videoJoinableStreamImpl != null)
			videoJoinableStreamImpl.stop();
		if (audioJoinableStreamImpl != null)
			audioJoinableStreamImpl.stop();
	}

	private void addPayloadSpec(List<PayloadSpec> videoList, String payloadStr,
			MediaType mediaType, int port, FormatParameters formatParameters) {
		try {
			PayloadSpec payload = new PayloadSpec(payloadStr);
			payload.setMediaType(mediaType);
			payload.setPort(port);
			if (formatParameters != null)
				payload.setFormatParams(formatParameters);
			videoList.add(payload);
		} catch (SdpException e) {
			e.printStackTrace();
		}
	}

	private void takeMediaPort() {

		final Exchanger<Integer> exchanger = new Exchanger<Integer>();

		MediaPortManager.releaseAudioLocalPort();
		MediaPortManager.releaseVideoLocalPort();

		new Thread(new Runnable() {
			public void run() {
				Log.d(LOG_TAG, "Video: Test Port ...." + getLocalAddress());
				DiscoveryTest test = new DiscoveryTest(getLocalAddress(),
						getStunHost(), getStunPort());
				DiscoveryInfo info = new DiscoveryInfo(getLocalAddress());
				try {
					info = test.test();
				} catch (Exception e) {
					Log.e(LOG_TAG, "TakeMediaPort: " + e.toString());
				}

				MediaPortManager.takeVideoLocalPort(info.getLocalPort());
				videoPort = info.getPublicPort();
				Log.d(LOG_TAG,
						"Video: Private IP:" + info.getLocalIP() + ":"
								+ info.getLocalPort() + "\nPublic IP: "
								+ info.getPublicIP() + ":"
								+ info.getPublicPort() + "\nPort: Media = "
								+ info.getLocalPort() + " SDP = " + videoPort);
				try {
					exchanger.exchange(null);
				} catch (InterruptedException e) {
					Log.d(LOG_TAG, "exchanger: " + e.toString());
					e.printStackTrace();
				}
			}
		}).start();

		Log.d(LOG_TAG, "Audio : Test Port ...." + getLocalAddress());
		DiscoveryTest test = new DiscoveryTest(getLocalAddress(),
				getStunHost(), getStunPort());
		DiscoveryInfo info = new DiscoveryInfo(getLocalAddress());

		try {
			info = test.test();
		} catch (Exception e) {
			Log.e(LOG_TAG, "TakeMediaPort: " + e.toString());
		}

		MediaPortManager.takeAudioLocalPort(info.getLocalPort());
		audioPort = info.getPublicPort();

		Log.d(LOG_TAG,
				"Audio: Private IP:" + info.getLocalIP() + ":"
						+ info.getLocalPort() + "\nPublic IP: "
						+ info.getPublicIP() + ":" + info.getPublicPort()
						+ "\nAudio Port: Media = " + info.getLocalPort()
						+ " SDP = " + audioPort);
		try {
			exchanger.exchange(null);
		} catch (InterruptedException e) {
			Log.d(LOG_TAG, "exchanger: " + e.toString());
			e.printStackTrace();
		}

		publicAddress = info.getPublicIP();
		Log.d(LOG_TAG, "Port reserved, Audio:" + audioPort + "; Video: "
				+ videoPort);
	}

	@Override
	public SessionSpec generateSessionSpec() {

		takeMediaPort();

		int payload = 96;

		// VIDEO
		MediaSpec videoMedia = null;

		if (videoProfiles != null && videoProfiles.size() > 0) {
			List<PayloadSpec> videoList = new Vector<PayloadSpec>();
			for (VideoProfile vp : videoProfiles) {
				if (VideoCodecType.MPEG4.equals(vp.getVideoCodecType()))
					addPayloadSpec(videoList, payload + " MP4V-ES/90000",
							MediaType.VIDEO, videoPort, null);
				else if (VideoCodecType.H263.equals(vp.getVideoCodecType())) {
					ArrayList<H263VideoProfile> profilesList = new ArrayList<H263VideoProfile>();
					profilesList.add(new H263VideoProfile(vp.getWidth(), vp
							.getHeight(), new Fraction(vp.getFrameRate()*1000, 1001)));
					H263FormatParameters h263fp = null;
					try {
						h263fp = new H263FormatParameters(profilesList);
					} catch (SdpException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					addPayloadSpec(videoList, payload + " H263-1998/90000",
							MediaType.VIDEO, videoPort, h263fp);
				}
				payload++;
			}

			videoMedia = new MediaSpec();
			videoMedia.setPayloadList(videoList);
			videoMedia.setBandWidth(videoProfiles.get(0).getBitRate());

			Mode videoMode = Mode.SENDRECV;
			if (this.mediaSessionConfig.getMediaTypeModes() != null
					&& this.mediaSessionConfig.getMediaTypeModes().get(
							MediaType.VIDEO) != null)
				videoMode = this.mediaSessionConfig.getMediaTypeModes().get(
						MediaType.VIDEO);
			videoMedia.setMode(videoMode);
		}

		// // AUDIO
		MediaSpec audioMedia = null;

		if (audioProfiles != null && audioProfiles.size() > 0) {
			List<PayloadSpec> audioList = new Vector<PayloadSpec>();
			for (AudioProfile ap : audioProfiles) {
				if (AudioProfile.MP2.equals(ap)) {
					PayloadSpec payloadAudioMP2 = new PayloadSpec();
					payloadAudioMP2.setMediaType(MediaType.AUDIO);
					payloadAudioMP2.setPort(audioPort);
					payloadAudioMP2.setPayload(14);
					audioList.add(payloadAudioMP2);
				} else if (AudioProfile.AMR.equals(ap)) {
					PayloadSpec audioPayloadAMR = null;
					try {
						audioPayloadAMR = new PayloadSpec(payload
								+ " AMR/8000/1");
						audioPayloadAMR.setFormatParams("octet-align=1");
						audioPayloadAMR.setMediaType(MediaType.AUDIO);
						audioPayloadAMR.setPort(audioPort);
					} catch (SdpException e) {
						e.printStackTrace();
					}
					audioList.add(audioPayloadAMR);
				}
				payload++;
			}

			audioMedia = new MediaSpec();
			audioMedia.setPayloadList(audioList);
			audioMedia.setBandWidth(maxAudioBitrate);

			Mode audioMode = Mode.SENDRECV;
			if (this.mediaSessionConfig.getMediaTypeModes() != null
					&& this.mediaSessionConfig.getMediaTypeModes().get(
							MediaType.AUDIO) != null)
				audioMode = this.mediaSessionConfig.getMediaTypeModes().get(
						MediaType.AUDIO);
			audioMedia.setMode(audioMode);
		}

		List<MediaSpec> mediaList = new Vector<MediaSpec>();
		if (videoMedia != null)
			mediaList.add(videoMedia);
		if (audioMedia != null)
			mediaList.add(audioMedia);

		SessionSpec session = new SessionSpec();
		session.setMediaSpec(mediaList);

		session.setOriginAddress(publicAddress.getHostAddress().toString());
		session.setRemoteHandler(publicAddress.getHostAddress().toString());
		session.setSessionName("TestSession");

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
		ArrayList<AudioCodecType> audioCodecs = mediaSessionConfig
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
		ArrayList<VideoCodecType> videoCodecs = mediaSessionConfig
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
			if (maxFrameRate != null)
				vp.setFrameRate(maxFrameRate);
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

}
