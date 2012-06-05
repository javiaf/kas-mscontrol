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

package com.kurento.kas.mscontrol.internal;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

import com.kurento.commons.media.format.enums.MediaType;
import com.kurento.commons.media.format.enums.Mode;
import com.kurento.commons.mscontrol.Configuration;
import com.kurento.commons.mscontrol.MediaSession;
import com.kurento.kas.media.codecs.AudioCodecType;
import com.kurento.kas.media.codecs.VideoCodecType;
import com.kurento.kas.mscontrol.networkconnection.NetIF;
import com.kurento.kas.mscontrol.networkconnection.PortRange;

public class MediaSessionConfig implements Configuration<MediaSession> {

	private String stunHost;
	private Integer stunPort;

	private NetIF netIF;
	private InetAddress localAddress;
	private Integer maxBW;
	private Integer maxDelay;

	private Map<MediaType, Mode> mediaTypeModes;
	private ArrayList<AudioCodecType> audioCodecs;
	private PortRange audioPortRange;
	private ArrayList<VideoCodecType> videoCodecs;
	private PortRange videoPortRange;

	private Integer frameWidth;
	private Integer frameHeight;
	private Integer maxFrameRate;
	private Integer gopSize;
	private Integer framesQueueSize;

	private Boolean syncMediaStreams;

	public String getStunHost() {
		return stunHost;
	}

	public Integer getStunPort() {
		return stunPort;
	}

	public NetIF getNetIF() {
		return netIF;
	}

	public InetAddress getLocalAddress() {
		return localAddress;
	}

	public Integer getMaxBW() {
		return maxBW;
	}

	public Integer getMaxDelay() {
		return maxDelay;
	}

	public Map<MediaType, Mode> getMediaTypeModes() {
		return mediaTypeModes;
	}

	public ArrayList<AudioCodecType> getAudioCodecs() {
		return audioCodecs;
	}

	public PortRange getAudioPortRange() {
		return audioPortRange;
	}

	public ArrayList<VideoCodecType> getVideoCodecs() {
		return videoCodecs;
	}

	public PortRange getVideoPortRange() {
		return videoPortRange;
	}

	public Integer getFrameWidth() {
		return frameWidth;
	}

	public Integer getFrameHeight() {
		return frameHeight;
	}

	public Integer getMaxFrameRate() {
		return maxFrameRate;
	}

	public Integer getGopSize() {
		return gopSize;
	}

	public Integer getFramesQueueSize() {
		return framesQueueSize;
	}

	public Boolean getSyncMediaStreams() {
		return syncMediaStreams;
	}

	protected MediaSessionConfig(NetIF netIF, InetAddress localAddress,
			Integer maxBW, Integer maxDelay,
			Map<MediaType, Mode> mediaTypeModes,
			ArrayList<AudioCodecType> audioCodecs, PortRange audioPortRange,
			ArrayList<VideoCodecType> videoCodecs, PortRange videoPortRange,
			Integer frameWidth, Integer frameHeight, Integer maxFrameRate,
			Integer gopSize, Integer framesQueueSize, Boolean syncMediaStreams,
			String stunHost, Integer stunPort) {

		this.stunHost = stunHost;
		this.stunPort = stunPort;

		this.netIF = netIF;
		this.localAddress = localAddress;
		this.maxBW = maxBW;
		this.maxDelay = maxDelay;

		this.mediaTypeModes = mediaTypeModes;
		this.audioCodecs = audioCodecs;
		this.audioPortRange = audioPortRange;
		this.videoCodecs = videoCodecs;
		this.videoPortRange = videoPortRange;

		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		this.maxFrameRate = maxFrameRate;
		this.gopSize = gopSize;
		this.framesQueueSize = framesQueueSize;

		this.syncMediaStreams = syncMediaStreams;
	}

}
