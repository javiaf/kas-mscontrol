package com.kurento.kas.mscontrol;

import java.net.InetAddress;
import java.util.ArrayList;

import android.util.Log;

import com.kurento.commons.mscontrol.Configuration;
import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.Parameters;
import com.kurento.commons.mscontrol.mediacomponent.MediaComponent;
import com.kurento.commons.mscontrol.networkconnection.NetworkConnection;
import com.kurento.kas.media.AudioCodecType;
import com.kurento.kas.media.VideoCodecType;
import com.kurento.kas.mscontrol.mediacomponent.AudioPlayerComponent;
import com.kurento.kas.mscontrol.mediacomponent.AudioRecorderComponent;
import com.kurento.kas.mscontrol.mediacomponent.MediaComponentAndroid;
import com.kurento.kas.mscontrol.mediacomponent.VideoPlayerComponent;
import com.kurento.kas.mscontrol.mediacomponent.VideoRecorderComponent;
import com.kurento.kas.mscontrol.networkconnection.ConnectionType;
import com.kurento.kas.mscontrol.networkconnection.NetworkConnectionImpl;

public class MediaSessionImpl implements MediaSessionAndroid {

	public final static String LOG_TAG = "MSImpl";
	
	private MediaSessionConfig mediaSessionConfig;

	public MediaSessionImpl(Parameters params) throws MsControlException {
		if (params == null)
			throw new MsControlException("Parameters are NULL");

		ArrayList<AudioCodecType> audioCodecs = (ArrayList<AudioCodecType>) params.get(AUDIO_CODECS);
		if (audioCodecs == null)
			throw new MsControlException(
					"Params must have MediaSessionAndroid.AUDIO_CODECS param");
		ArrayList<VideoCodecType> videoCodecs = (ArrayList<VideoCodecType>) params.get(VIDEO_CODECS);
		if (videoCodecs == null)
			throw new MsControlException(
					"Params must have MediaSessionAndroid.VIDEO_CODECS param");
		InetAddress localAddress = (InetAddress) params.get(LOCAL_ADDRESS);
		if (localAddress == null)
			throw new MsControlException(
					"Params must have MediaSessionAndroid.LOCAL_ADDRESS param");
		ConnectionType connectionType = (ConnectionType) params.get(CONNECTION_TYPE);
		if (connectionType == null)
			throw new MsControlException(
					"Params must have MediaSessionAndroid.CONNECTION_TYPE param");

		this.mediaSessionConfig = new MediaSessionConfig(audioCodecs, videoCodecs, localAddress,
				connectionType);
		Log.d(LOG_TAG, "this.mediaSessionConfig: " + this.mediaSessionConfig);
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	@Override
	public NetworkConnection createNetworkConnection() throws MsControlException {
		Log.d(LOG_TAG, "createNetworkConnection");
		return new NetworkConnectionImpl(mediaSessionConfig);
	}

	@Override
	public MediaComponentAndroid createMediaComponent(Configuration<MediaComponent> predefinedConfig,
			Parameters params) throws MsControlException {

		if (predefinedConfig == null)
			throw new MsControlException("Configuration is NULL");

		if (MediaComponentAndroid.AUDIO_PLAYER.equals(predefinedConfig))
			return new AudioPlayerComponent();
		else if (MediaComponentAndroid.AUDIO_RECORDER.equals(predefinedConfig))
			return new AudioRecorderComponent(params);
		else if (MediaComponentAndroid.VIDEO_PLAYER.equals(predefinedConfig))
			return new VideoPlayerComponent(params);
		else if (MediaComponentAndroid.VIDEO_RECORDER.equals(predefinedConfig))
			return new VideoRecorderComponent(params);

		throw new MsControlException("Configuration is not supported: " + predefinedConfig);
	}

}
