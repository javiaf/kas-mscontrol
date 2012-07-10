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
import java.util.ArrayList;
import java.util.Map;

import com.kurento.commons.config.Parameters;
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
	public NetworkConnection createNetworkConnection() throws MsControlException {
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

		Object obj;

		obj = params.get(STUN_HOST);
		if (obj == null)
			throw new MsControlException(
					"Params must have KasMediaSession.STUN_HOST param");
		if (!(obj instanceof String))
			throw new MsControlException(
					"Parameter KasMediaSession.STUN_HOST must be instance of String");
		String stunHost = (String) obj;

		obj = params.get(STUN_PORT);
		if (obj == null)
			throw new MsControlException(
					"Params must have KasMediaSession.STUN_PORT param");
		if (!(obj instanceof Integer))
			throw new MsControlException(
					"Parameter KasMediaSession.STUN_PORT must be instance of Integer");
		Integer stunPort = (Integer) obj;

		obj = params.get(NET_IF);
		if (obj == null)
			throw new MsControlException(
					"Params must have KasMediaSession.NET_IF param");
		if (!(obj instanceof NetIF))
			throw new MsControlException(
					"Parameter KasMediaSession.NET_IF must be instance of NetIF");
		NetIF netIF = (NetIF) obj;

		obj = params.get(LOCAL_ADDRESS);
		if (obj == null) {
			throw new MsControlException(
					"Params must have KasMediaSession.LOCAL_ADDRESS param");
		} else if (!(obj instanceof InetAddress))
			throw new MsControlException(
					"Parameter KasMediaSession.LOCAL_ADDRESS must be instance of InetAddress");
		InetAddress localAddress = (InetAddress) obj;

		Integer maxBW = null;
		obj = params.get(MAX_BANDWIDTH);
		if (obj == null) {
			// Por defecto
		} else if (!(obj instanceof Integer))
			throw new MsControlException(
					"Parameter KasMediaSession.MAX_BANDWIDTH must be instance of Integer");
		else
			maxBW = (Integer) obj;

		Integer maxDelay = null;
		obj = params.get(MAX_DELAY);
		if (obj == null) {
			maxDelay = MediaRx.DEFAULT_MAX_DELAY;
		} else if (!(obj instanceof Integer))
			throw new MsControlException(
					"Parameter KasMediaSession.DELAY must be instance of Integer");
		else
			maxDelay = (Integer) obj;

		Map<MediaType, Mode> mediaTypeModes = null;
		obj = params.get(STREAMS_MODES);
		if (obj == null) {
			// Por defecto
		} else
			try {
				mediaTypeModes = (Map<MediaType, Mode>) obj;
			} catch (ClassCastException e) {
				throw new MsControlException(
						"Parameter KasMediaSession.STREAMS_MODES must be instance of Map<MediaType, Mode>",
						e);
			}

		ArrayList<AudioCodecType> audioCodecs = null;
		obj = params.get(AUDIO_CODECS);
		if (obj == null) {
			// Por defecto
		} else
			try {
				audioCodecs = (ArrayList<AudioCodecType>) obj;
			} catch (ClassCastException e) {
				throw new MsControlException(
						"Parameter KasMediaSession.AUDIO_CODECS must be instance of ArrayList<AudioCodecType>",
						e);
			}

		PortRange audioPortRange = null;
		obj = params.get(AUDIO_LOCAL_PORT_RANGE);
		if (obj == null) {
			// Por defecto
		} else if (!(obj instanceof PortRange))
			throw new MsControlException(
					"Parameter KasMediaSession.AUDIO_LOCAL_PORT_RANGE must be instance of PortRange");
		else
			audioPortRange = (PortRange) obj;

		ArrayList<VideoCodecType> videoCodecs = null;
		obj = params.get(VIDEO_CODECS);
		if (obj == null) {
			// Por defecto
		} else
			try {
				videoCodecs = (ArrayList<VideoCodecType>) obj;
			} catch (ClassCastException e) {
				throw new MsControlException(
						"Parameter KasMediaSession.VIDEO_CODECS must be instance of ArrayList<VideoCodecType>",
						e);
			}

		PortRange videoPortRange = null;
		obj = params.get(VIDEO_LOCAL_PORT_RANGE);
		if (obj == null) {
			// Por defecto
		} else if (!(obj instanceof PortRange))
			throw new MsControlException(
					"Parameter KasMediaSession.VIDEO_LOCAL_PORT_RANGE must be instance of PortRange");
		else
			videoPortRange = (PortRange) obj;

		Integer frameWidth = null;
		obj = params.get(FRAME_WIDTH);
		if (obj == null) {
			// Por defecto
		} else if (!(obj instanceof Integer))
			throw new MsControlException(
					"Parameter KasMediaSession.FRAME_WIDTH must be instance of Integer");
		else
			frameWidth = (Integer) obj;

		Integer frameHeight = null;
		obj = params.get(FRAME_HEIGHT);
		if (obj == null) {
			// Por defecto
		} else if (!(obj instanceof Integer))
			throw new MsControlException(
					"Parameter KasMediaSession.FRAME_HEIGHT must be instance of Integer");
		else
			frameHeight = (Integer) obj;

		Integer maxFrameRate = null;
		obj = params.get(MAX_FRAME_RATE);
		if (obj == null) {
			// Por defecto
		} else if (!(obj instanceof Integer))
			throw new MsControlException(
					"Parameter KasMediaSession.MAX_FRAME_RATE must be instance of Integer");
		else
			maxFrameRate = (Integer) obj;

		Integer gopSize = null;
		obj = params.get(GOP_SIZE);
		if (obj == null) {
			// Por defecto
		} else if (!(obj instanceof Integer))
			throw new MsControlException(
					"Parameter KasMediaSession.GOP_SIZE must be instance of Integer");
		else
			gopSize = (Integer) obj;

		Integer framesQueueSize = null;
		obj = params.get(FRAMES_QUEUE_SIZE);
		if (obj == null) {
			// Por defecto
		} else if (!(obj instanceof Integer))
			throw new MsControlException(
					"Parameter KasMediaSession.FRAMES_QUEUE_SIZE must be instance of Integer");
		else
			framesQueueSize = (Integer) obj;

		Boolean syncMediaStreams = null;
		obj = params.get(SYNCHRONIZE_MEDIA_STREAMS);
		if (obj == null) {
			syncMediaStreams = false;
		} else if (!(obj instanceof Boolean))
			throw new MsControlException(
					"Parameter KasMediaSession.SYNCHRONIZE_MEDIA_STREAMS must be instance of Boolean");
		else
			syncMediaStreams = (Boolean) obj;

		return new MediaSessionConfig(netIF, localAddress, maxBW, maxDelay,
				mediaTypeModes, audioCodecs, audioPortRange, videoCodecs, videoPortRange,
				frameWidth, frameHeight, maxFrameRate, gopSize, framesQueueSize,
				syncMediaStreams, stunHost, stunPort);
	}

	@Override
	public NetworkConnection createNetworkConnection(
			Configuration<NetworkConnection> predefinedConfig) throws MsControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MediaMixer createMediaMixer(Configuration<MediaMixer> predefinedConfig,
			Parameters params) throws MsControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MediaMixer createMediaMixer() throws MsControlException {
		// TODO Auto-generated method stub
		return null;
	}

}
