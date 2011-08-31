package com.kurento.kas.mscontrol;

import java.net.InetAddress;
import java.util.ArrayList;

import com.kurento.commons.mscontrol.Configuration;
import com.kurento.commons.mscontrol.MediaSession;
import com.kurento.kas.media.AudioCodecType;
import com.kurento.kas.media.VideoCodecType;
import com.kurento.kas.mscontrol.networkconnection.ConnectionType;

public class MediaSessionConfig implements Configuration<MediaSession> {

	private ArrayList<AudioCodecType> audioCodecs;
	private ArrayList<VideoCodecType> videoCodecs;

	private InetAddress localAddress;
	private ConnectionType connectionType;

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

	protected MediaSessionConfig(ArrayList<AudioCodecType> audioCodecs,
			ArrayList<VideoCodecType> videoCodecs, InetAddress localAddress,
			ConnectionType connectionType) {
		this.audioCodecs = audioCodecs;
		this.videoCodecs = videoCodecs;
		this.connectionType = connectionType;
		this.localAddress = localAddress;
	}

}