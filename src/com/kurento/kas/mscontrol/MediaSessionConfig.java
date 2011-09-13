package com.kurento.kas.mscontrol;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

import com.kurento.commons.mscontrol.Configuration;
import com.kurento.commons.mscontrol.MediaSession;
import com.kurento.commons.sdp.enums.MediaType;
import com.kurento.commons.sdp.enums.Mode;
import com.kurento.kas.media.AudioCodecType;
import com.kurento.kas.media.VideoCodecType;
import com.kurento.kas.mscontrol.networkconnection.ConnectionType;

public class MediaSessionConfig implements Configuration<MediaSession> {

	private ArrayList<AudioCodecType> audioCodecs;
	private ArrayList<VideoCodecType> videoCodecs;

	private InetAddress localAddress;
	private ConnectionType connectionType;

	private Map<MediaType, Mode> mediaTypeModes;

	public ArrayList<AudioCodecType> getAudioCodecs() {
		return audioCodecs;
	}

	public ArrayList<VideoCodecType> getVideoCodecs() {
		return videoCodecs;
	}

	public InetAddress getLocalAddress() {
		return localAddress;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public Map<MediaType, Mode> getMediaTypeModes() {
		return mediaTypeModes;
	}

	protected MediaSessionConfig(ArrayList<AudioCodecType> audioCodecs,
			ArrayList<VideoCodecType> videoCodecs, InetAddress localAddress,
			ConnectionType connectionType, Map<MediaType, Mode> mediaTypeModes) {
		this.audioCodecs = audioCodecs;
		this.videoCodecs = videoCodecs;
		this.connectionType = connectionType;
		this.localAddress = localAddress;
		this.mediaTypeModes = mediaTypeModes;
	}

}
