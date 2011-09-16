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
import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.join.JoinableStream.StreamType;
import com.kurento.commons.sdp.enums.MediaType;
import com.kurento.commons.sdp.enums.Mode;
import com.kurento.kas.media.AudioCodecType;
import com.kurento.kas.media.MediaPortManager;
import com.kurento.kas.media.VideoCodecType;
import com.kurento.kas.media.profiles.AudioProfile;
import com.kurento.kas.media.profiles.MediaQuality;
import com.kurento.kas.media.profiles.VideoProfile;
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

	private VideoJoinableStreamImpl videoJoinableStreamImpl;
	private AudioJoinableStreamImpl audioJoinableStreamImpl;

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

		// Process MediaConfigure and determinate media profiles
		audioProfiles = getAudioProfiles(this.mediaSessionConfig);
		videoProfiles = getVideoProfiles(this.mediaSessionConfig);

		if (videoPort == -1)
			videoPort = MediaPortManager.takeVideoLocalPort();
		if (audioPort == -1)
			audioPort = MediaPortManager.takeAudioLocalPort();
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
				StreamType.video, remoteSessionSpec, localSessionSpec);
		this.streams[1] = videoJoinableStreamImpl;
	}

	@Override
	public void release() {
		Log.d(LOG_TAG, "release");
		videoJoinableStreamImpl.stop();
		audioJoinableStreamImpl.stop();
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
		int payload = 96;

		// VIDEO
		MediaSpec videoMedia = null;

		if (videoProfiles != null) {
			List<PayloadSpec> videoList = new Vector<PayloadSpec>();
			for (VideoProfile vp : videoProfiles) {
				if (VideoProfile.MPEG4.equals(vp))
					addPayloadSpec(videoList, payload + " MP4V-ES/90000",
							MediaType.VIDEO, videoPort);
				else if (VideoProfile.H263.equals(vp))
					addPayloadSpec(videoList, payload + " H263-1998/90000",
							MediaType.VIDEO, videoPort);
				payload++;
			}

			videoMedia = new MediaSpec();
			videoMedia.setPayloadList(videoList);
			Mode videoMode = this.mediaSessionConfig.getMediaTypeModes().get(
					MediaType.VIDEO);
			videoMedia.setMode(videoMode);
		}

		// // AUDIO
		MediaSpec audioMedia = null;

		if (audioProfiles != null) {
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

			audioMedia = new MediaSpec();
			audioMedia.setPayloadList(audioList);
			Mode audioMode = this.mediaSessionConfig.getMediaTypeModes().get(
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

		session.setOriginAddress(getLocalAddress().getHostAddress().toString());
		session.setRemoteHandler("0.0.0.0");
		session.setSessionName("TestSession");

		return session;
	}

	@Override
	public InetAddress getLocalAddress() {
		return this.mediaSessionConfig.getLocalAddress();
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
