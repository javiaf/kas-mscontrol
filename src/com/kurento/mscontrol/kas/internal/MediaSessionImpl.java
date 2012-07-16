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

package com.kurento.mscontrol.kas.internal;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import com.kurento.commons.config.Parameters;
import com.kurento.commons.config.Value;
import com.kurento.kas.media.codecs.AudioCodecType;
import com.kurento.kas.media.codecs.VideoCodecType;
import com.kurento.kas.media.rx.MediaRx;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.Mode;
import com.kurento.mscontrol.commons.Configuration;
import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.commons.mediacomponent.MediaComponent;
import com.kurento.mscontrol.commons.mediamixer.MediaMixer;
import com.kurento.mscontrol.commons.networkconnection.NetworkConnection;
import com.kurento.mscontrol.kas.KasMediaSession;
import com.kurento.mscontrol.kas.mediacomponent.MediaComponentAndroid;
import com.kurento.mscontrol.kas.mediacomponent.internal.AudioPlayerComponent;
import com.kurento.mscontrol.kas.mediacomponent.internal.AudioRecorderComponent;
import com.kurento.mscontrol.kas.mediacomponent.internal.VideoPlayerComponent;
import com.kurento.mscontrol.kas.mediacomponent.internal.VideoRecorderComponent;
import com.kurento.mscontrol.kas.networkconnection.NetIF;
import com.kurento.mscontrol.kas.networkconnection.PortRange;
import com.kurento.mscontrol.kas.networkconnection.internal.NetworkConnectionImpl;

public class MediaSessionImpl implements KasMediaSession {

	public final static String LOG_TAG = "MSImpl";

	private MediaSessionConfig mediaSessionConfig;

	public MediaSessionImpl(Parameters params) throws MsControlException {
		this.mediaSessionConfig = getMediaSessionconfigFromParams(params);
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	@Override
	public NetworkConnection createNetworkConnection()
			throws MsControlException {
		return new NetworkConnectionImpl(mediaSessionConfig);
	}

	@Override
	public MediaComponentAndroid createMediaComponent(
			Configuration<MediaComponent> predefinedConfig, Parameters params)
			throws MsControlException {

		if (predefinedConfig == null)
			throw new MsControlException("Configuration is NULL");

		if (MediaComponentAndroid.AUDIO_PLAYER.equals(predefinedConfig))
			return new AudioPlayerComponent();
		else if (MediaComponentAndroid.AUDIO_RECORDER.equals(predefinedConfig))
			return new AudioRecorderComponent(mediaSessionConfig.getMaxDelay(),
					mediaSessionConfig.getSyncMediaStreams(), params);
		else if (MediaComponentAndroid.VIDEO_PLAYER.equals(predefinedConfig))
			return new VideoPlayerComponent(params);
		else if (MediaComponentAndroid.VIDEO_RECORDER.equals(predefinedConfig))
			return new VideoRecorderComponent(mediaSessionConfig.getMaxDelay(),
					mediaSessionConfig.getSyncMediaStreams(), params);

		throw new MsControlException("Configuration is not supported: "
				+ predefinedConfig);
	}

	private MediaSessionConfig getMediaSessionconfigFromParams(Parameters params)
			throws MsControlException {
		if (params == null)
			throw new MsControlException("Parameters are NULL");

		Value<String> stunHostValue = params.get(STUN_HOST);
		if (stunHostValue == null)
			throw new MsControlException(
					"Params must have KasMediaSession.STUN_HOST param");
		String stunHost = stunHostValue.getValue();

		Value<Integer> stunPortValue = params.get(STUN_PORT);
		if (stunPortValue == null)
			throw new MsControlException(
					"Params must have KasMediaSession.STUN_PORT param");
		Integer stunPort = stunPortValue.getValue();

		Value<NetIF> netIFValue = params.get(NET_IF);
		if (netIFValue == null)
			throw new MsControlException(
					"Params must have KasMediaSession.NET_IF param");
		NetIF netIF = netIFValue.getValue();

		Value<InetAddress> localAddressValue = params.get(LOCAL_ADDRESS);
		if (localAddressValue == null)
			throw new MsControlException(
					"Params must have KasMediaSession.LOCAL_ADDRESS param");
		InetAddress localAddress = localAddressValue.getValue();

		Integer maxBW = null;
		Value<Integer> maxBWValue = params.get(MAX_BANDWIDTH);
		if (maxBWValue != null)
			maxBW = maxBWValue.getValue();

		Integer maxDelay = MediaRx.DEFAULT_MAX_DELAY;
		Value<Integer> maxDelayValue = params.get(MAX_DELAY);
		if (maxDelayValue != null)
			maxDelay = maxDelayValue.getValue();

		Map<MediaType, Mode> mediaTypeModes = null;
		Value<Map<MediaType, Mode>> mediaTypeModesValue = params.get(STREAMS_MODES);
		if (mediaTypeModesValue != null)
			mediaTypeModes = mediaTypeModesValue.getValue();

		List<AudioCodecType> audioCodecs = null;
		Value<List<AudioCodecType>> audioCodecsValue = params.get(AUDIO_CODECS);
		if (audioCodecsValue != null)
			audioCodecs = audioCodecsValue.getValue();

		PortRange audioPortRange = null;
		Value<PortRange> audioPortRangeValue = params
				.get(AUDIO_LOCAL_PORT_RANGE);
		if (audioPortRangeValue != null)
			audioPortRange = audioPortRangeValue.getValue();

		List<VideoCodecType> videoCodecs = null;
		Value<List<VideoCodecType>> videoCodecsValue = params.get(VIDEO_CODECS);
		if (videoCodecsValue != null)
			videoCodecs = videoCodecsValue.getValue();

		PortRange videoPortRange = null;
		Value<PortRange> videoPortRangeValue = params
				.get(VIDEO_LOCAL_PORT_RANGE);
		if (videoPortRangeValue != null)
			videoPortRange = videoPortRangeValue.getValue();

		Integer frameWidth = null;
		Value<Integer> frameWidthValue = params.get(FRAME_WIDTH);
		if (frameWidthValue != null)
			frameWidth = frameWidthValue.getValue();

		Integer frameHeight = null;
		Value<Integer> frameHeightValue = params.get(FRAME_HEIGHT);
		if (frameHeightValue != null)
			frameHeight = frameHeightValue.getValue();

		Integer maxFrameRate = null;
		Value<Integer> maxFrameRateValue = params.get(MAX_FRAME_RATE);
		if (maxFrameRateValue != null)
			maxFrameRate = maxFrameRateValue.getValue();

		Integer gopSize = null;
		Value<Integer> gopSizeValue = params.get(GOP_SIZE);
		if (gopSizeValue != null)
			gopSize = gopSizeValue.getValue();

		Integer framesQueueSize = null;
		Value<Integer> framesQueueSizeValue = params.get(FRAMES_QUEUE_SIZE);
		if (framesQueueSizeValue != null)
			framesQueueSize = framesQueueSizeValue.getValue();

		Boolean syncMediaStreams = false;
		Value<Boolean> syncMediaStreamsValue = params
				.get(SYNCHRONIZE_MEDIA_STREAMS);
		if (syncMediaStreamsValue != null)
			syncMediaStreams = syncMediaStreamsValue.getValue();

		return new MediaSessionConfig(netIF, localAddress, maxBW, maxDelay,
				mediaTypeModes, audioCodecs, audioPortRange, videoCodecs,
				videoPortRange, frameWidth, frameHeight, maxFrameRate, gopSize,
				framesQueueSize, syncMediaStreams, stunHost, stunPort);
	}

	@Override
	public NetworkConnection createNetworkConnection(
			Configuration<NetworkConnection> predefinedConfig)
			throws MsControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MediaMixer createMediaMixer(
			Configuration<MediaMixer> predefinedConfig, Parameters params)
			throws MsControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MediaMixer createMediaMixer() throws MsControlException {
		// TODO Auto-generated method stub
		return null;
	}

}
