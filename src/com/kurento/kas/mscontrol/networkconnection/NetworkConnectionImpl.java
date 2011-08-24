package com.kurento.kas.mscontrol.networkconnection;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.sdp.SdpException;

import android.util.Log;

import com.kurento.commons.media.format.MediaSpec;
import com.kurento.commons.media.format.PayloadSpec;
import com.kurento.commons.media.format.SessionSpec;
import com.kurento.commons.media.format.SpecTools;
import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.join.JoinableStream.StreamType;
import com.kurento.commons.sdp.enums.MediaType;
import com.kurento.kas.media.AudioCodecType;
import com.kurento.kas.media.MediaPortManager;
import com.kurento.kas.media.VideoCodecType;
import com.kurento.kas.media.profiles.AudioProfile;
import com.kurento.kas.media.profiles.MediaQuality;
import com.kurento.kas.media.profiles.VideoProfile;
import com.kurento.kas.media.rx.AudioRx;
import com.kurento.kas.media.rx.MediaRx;
import com.kurento.kas.media.rx.VideoRx;
import com.kurento.kas.media.tx.AudioInfoTx;
import com.kurento.kas.media.tx.MediaTx;
import com.kurento.kas.media.tx.VideoInfoTx;
import com.kurento.kas.mscontrol.MediaSessionConfig;
import com.kurento.kas.mscontrol.join.AudioJoinableStreamImpl;
import com.kurento.kas.mscontrol.join.JoinableStreamBase;
import com.kurento.kas.mscontrol.join.VideoJoinableStreamImpl;

/**
 * 
 * @author Miguel París Díaz
 * 
 */
public class NetworkConnectionImpl extends NetworkConnectionBase {

	private static final long serialVersionUID = 1L;
	public final static String LOG_TAG = "NW";

	private MediaSessionConfig mediaSessionConfig;

	private ArrayList<AudioProfile> audioProfiles;
	private ArrayList<VideoProfile> videoProfiles;

	private SessionSpec localSessionSpec;
	private SessionSpec remoteSessionSpec;

	private static int videoPort = -1;
	private static int audioPort = -1;

	private String sdpVideo = "";
	private String sdpAudio = "";

	public String getSdpVideo() {
		return sdpVideo;
	}

	public String getSdpAudio() {
		return sdpAudio;
	}

	@Override
	public void setLocalSessionSpec(SessionSpec arg0) {
		this.localSessionSpec = arg0;
	}

	@Override
	public void setRemoteSessionSpec(SessionSpec arg0) {
		this.remoteSessionSpec = arg0;
	}

	public NetworkConnectionImpl(MediaSessionConfig mediaSessionConfig)
			throws MsControlException {
		super();
		Log.d(LOG_TAG, "ON NEW mediaSessionConfig: " + this.mediaSessionConfig);
		if (mediaSessionConfig == null)
			throw new MsControlException("Media Session Config are NULL");
		this.mediaSessionConfig = mediaSessionConfig;
		this.streams = new JoinableStreamBase[2];

		// Process MediaConfigure and determinate media profiles
		audioProfiles = getAudioProfiles(this.mediaSessionConfig);
		videoProfiles = getVideoProfiles(this.mediaSessionConfig);

		Log.d(LOG_TAG, "Take ports");
		if (videoPort == -1)
			videoPort = MediaPortManager.takeVideoLocalPort();
		if (audioPort == -1)
			audioPort = MediaPortManager.takeAudioLocalPort();
	}

	@Override
	public void confirm() throws MsControlException {
		Log.d(LOG_TAG, "start on NCImpl");
		Log.d(LOG_TAG, "remoteSessionSpec:\n" + remoteSessionSpec);
		Log.d(LOG_TAG, "localSessionSpec:\n" + localSessionSpec);

		if (remoteSessionSpec == null)
			// throw new MediaException("SessionSpec corrupt");
			return;

		RTPInfo rtpInfo = new RTPInfo(remoteSessionSpec);

		if (!SpecTools.filterMediaByType(localSessionSpec, "video")
				.getMediaSpec().isEmpty())
			sdpVideo = SpecTools.filterMediaByType(localSessionSpec, "video")
					.toString();
		if (!SpecTools.filterMediaByType(localSessionSpec, "audio")
				.getMediaSpec().isEmpty())
			sdpAudio = SpecTools.filterMediaByType(localSessionSpec, "audio")
					.toString();

		AudioCodecType audioCodecType = rtpInfo.getAudioCodecType();
		AudioProfile audioProfile = AudioProfile
				.getAudioProfileFromAudioCodecType(audioCodecType);
		if (audioProfiles != null && audioProfile != null) {
			AudioInfoTx audioInfo = new AudioInfoTx(audioProfile);
			audioInfo.setOut(rtpInfo.getAudioRTPDir());
			audioInfo.setPayloadType(rtpInfo.getAudioPayloadType());
			audioInfo.setFrameSize(MediaTx.initAudio(audioInfo));
			if (audioInfo.getFrameSize() < 0) {
				Log.d(LOG_TAG, "Error in initAudio");
				MediaTx.finishAudio();
				return;
			}
			this.streams[0] = new AudioJoinableStreamImpl(this,
					StreamType.audio, audioInfo);
		}

		VideoCodecType videoCodecType = rtpInfo.getVideoCodecType();
		VideoProfile videoProfile = VideoProfile
				.getVideoProfileFromVideoCodecType(videoCodecType);
		if (videoProfiles != null && videoProfile != null) {
			VideoInfoTx videoInfo = new VideoInfoTx(videoProfile);
			videoInfo.setOut(rtpInfo.getVideoRTPDir());
			videoInfo.setPayloadType(rtpInfo.getVideoPayloadType());
			int ret = MediaTx.initVideo(videoInfo);
			if (ret < 0) {
				Log.d(LOG_TAG, "Error in initVideo");
				MediaTx.finishVideo();
			}
			this.streams[1] = new VideoJoinableStreamImpl(this,
					StreamType.video, videoProfile);
		}

		if (!sdpVideo.equals(""))
			(new VideoRxThread()).start();
		if (!sdpAudio.equals(""))
			(new AudioRxThread()).start();
	}

	@Override
	public void release() {
		Log.d(LOG_TAG, "release");
		Log.d(LOG_TAG, "finishVideo");
		MediaTx.finishVideo();
		Log.d(LOG_TAG, "stopVideoRx");
		MediaRx.stopVideoRx();

		Log.d(LOG_TAG, "finishAudio");
		MediaTx.finishAudio();
		Log.d(LOG_TAG, "stopAudioRx");
		MediaRx.stopAudioRx();
		Log.d(LOG_TAG, "ALL OK");

		// MediaPortManager.releaseAudioLocalPort();
		// MediaPortManager.releaseVideoLocalPort();
	}

	private void addPayloadSpec(List<PayloadSpec> videoList, String payloadStr,
			MediaType mediaType, int port) {
		try {
			PayloadSpec payload = new PayloadSpec(payloadStr);
			payload.setMediaType(mediaType);
			payload.setPort(port);
			videoList.add(payload);
		} catch (SdpException e) {
			e.printStackTrace();
		}
	}

	@Override
	public SessionSpec generateSessionSpec() {
		Log.d(LOG_TAG, "generateSessionSpec");

		int payload = 96;

		// VIDEO
		List<PayloadSpec> videoList = new Vector<PayloadSpec>();

		if (videoProfiles != null) {
			for (VideoProfile vp : videoProfiles) {
				if (VideoProfile.MPEG4.equals(vp))
					addPayloadSpec(videoList, payload + " MP4V-ES/90000",
							MediaType.VIDEO, videoPort);
				else if (VideoProfile.H263.equals(vp))
					addPayloadSpec(videoList, payload + " H263-1998/90000",
							MediaType.VIDEO, videoPort);
				payload++;
			}
		}

		MediaSpec videoMedia = new MediaSpec();
		videoMedia.setPayloadList(videoList);

		// // AUDIO
		List<PayloadSpec> audioList = new Vector<PayloadSpec>();

		if (audioProfiles != null) {
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
						audioPayloadAMR = new PayloadSpec("100 AMR/8000/1");
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
		}

		MediaSpec audioMedia = new MediaSpec();
		audioMedia.setPayloadList(audioList);

		List<MediaSpec> mediaList = new Vector<MediaSpec>();
		mediaList.add(videoMedia);
		mediaList.add(audioMedia);

		SessionSpec session = new SessionSpec();
		session.setMediaSpec(mediaList);

		session.setOriginAddress(getLocalAddress().getHostAddress().toString());
		session.setRemoteHandler("0.0.0.0");
		session.setSessionName("TestSession");

		return session;
	}

	@Override
	public InetAddress getLocalAddress() {
		Log.d(LOG_TAG, "mediaSessionConfig: " + this.mediaSessionConfig);
		Log.d(LOG_TAG, "mediaSessionConfig.getLocalAddress(): "
				+ this.mediaSessionConfig.getLocalAddress());
		return this.mediaSessionConfig.getLocalAddress();
	}

	private class AudioRxThread extends Thread {
		@Override
		public void run() {
			Log.d(LOG_TAG, "startVideoRx");
			MediaRx.startAudioRx(sdpAudio, (AudioRx) streams[0]);
		}
	}

	private class VideoRxThread extends Thread {
		@Override
		public void run() {
			Log.d(LOG_TAG, "startVideoRx");
			MediaRx.startVideoRx(sdpVideo, (VideoRx) streams[1]);
		}
	}

	private ArrayList<AudioProfile> getAudioProfiles(
			MediaSessionConfig mediaSessionConfig) {
		ArrayList<AudioCodecType> audioCodecs = mediaSessionConfig
				.getAudioCodecs();
		if (audioCodecs == null)
			return null;

		ArrayList<AudioProfile> audioProfiles = new ArrayList<AudioProfile>(0);
		// Discard phase
		for (AudioProfile ap : AudioProfile.values()) {
			if (MediaQuality.HEIGH.equals(ap.getMediaQuality())
					&& !ConnectionType.WIFI.equals(mediaSessionConfig
							.getConnectionType()))
				continue;
			for (AudioCodecType act : audioCodecs) {
				if (act.equals(ap.getAudioCodecType()))
					audioProfiles.add(ap);
			}
		}

		// Scoring phase
		// TODO

		return audioProfiles;
	}

	private ArrayList<VideoProfile> getVideoProfiles(
			MediaSessionConfig mediaSessionConfig) {
		ArrayList<VideoCodecType> videoCodecs = mediaSessionConfig
				.getVideoCodecs();
		if (videoCodecs == null)
			return null;

		ArrayList<VideoProfile> videoProfiles = new ArrayList<VideoProfile>(0);
		// Discard phase
		for (VideoProfile vp : VideoProfile.values()) {
			if (MediaQuality.HEIGH.equals(vp.getMediaQuality())
					&& !ConnectionType.WIFI.equals(mediaSessionConfig
							.getConnectionType()))
				continue;
			for (VideoCodecType vct : videoCodecs) {
				if (vct.equals(vp.getVideoCodecType()))
					videoProfiles.add(vp);
			}
		}

		// Scoring phase
		// TODO

		return videoProfiles;
	}

}
