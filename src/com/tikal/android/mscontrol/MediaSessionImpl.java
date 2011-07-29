package com.tikal.android.mscontrol;

import java.net.InetAddress;
import java.util.ArrayList;

import android.util.Log;

import com.tikal.android.media.AudioCodecType;
import com.tikal.android.media.VideoCodecType;
import com.tikal.android.mscontrol.mediacomponent.AudioPlayerComponent;
import com.tikal.android.mscontrol.mediacomponent.AudioRecorderComponent;
import com.tikal.android.mscontrol.mediacomponent.MediaComponentAndroid;
import com.tikal.android.mscontrol.mediacomponent.VideoPlayerComponent;
import com.tikal.android.mscontrol.mediacomponent.VideoRecorderComponent;
import com.tikal.android.mscontrol.networkconnection.ConnectionType;
import com.tikal.android.mscontrol.networkconnection.NetworkConnectionImpl;
import com.tikal.mscontrol.Configuration;
import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.Parameters;
import com.tikal.mscontrol.mediacomponent.MediaComponent;
import com.tikal.mscontrol.networkconnection.NetworkConnection;

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
