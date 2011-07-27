package com.tikal.android.mscontrol;

import com.tikal.mscontrol.Configuration;
import com.tikal.mscontrol.MediaSession;
import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.Parameter;
import com.tikal.mscontrol.Parameters;
import com.tikal.mscontrol.mediacomponent.MediaComponent;

public interface MediaSessionAndroid extends MediaSession {

	/**
	 * Parameter whose value must be an ArrayList<AudioCodecType>.
	 */
	public static final Parameter AUDIO_CODECS = new Parameter() {
	};

	/**
	 * Parameter whose value must be an ArrayList<VideoCodecType>.
	 */
	public static final Parameter VIDEO_CODECS = new Parameter() {
	};

	/**
	 * Parameter whose value must be an InetAddress.
	 */
	public static final Parameter LOCAL_ADDRESS = new Parameter() {
	};

	/**
	 * Parameter whose value must be a ConnectionType.
	 */
	public static final Parameter CONNECTION_TYPE = new Parameter() {
	};

	/**
	 * </p> Create a MediaComponent.
	 * 
	 * </p>
	 * 
	 * @param predefinedConfig
	 *            Defines the concrete MediaComponent to create. <br>
	 *            Predefined Configurations are:
	 *            <ul>
	 *            <li>MediaComponentAndroid.AUDIO_PLAYER: create an
	 *            AudioPlayerComponent.
	 *            <li>MediaComponentAndroid.AUDIO_RECORDER: create an
	 *            AudioRecorderComponent.
	 *            <li>MediaComponentAndroid.VIDEO_PLAYER: create an
	 *            VideoPlayerComponent.
	 *            <li>MediaComponentAndroid.VIDEO_RECORDER: create an
	 *            VideoRecorderComponent.
	 *            </ul>
	 * @param params
	 *            Parameters to create a MediaComponent. Each concrete component
	 *            has its own parameters:
	 *            <ul>
	 *            <li>MediaComponentAndroid.AUDIO_PLAYER:
	 *            <ul>
	 *            <li>Parameters.NO_PARAMETER
	 *            </ul>
	 * 
	 *            <li>MediaComponentAndroid.AUDIO_RECORDER:
	 *            <ul>
	 *            <li>AudioRecorderComponent.STREAM_TYPE
	 *            </ul>
	 * 
	 *            <li>MediaComponentAndroid.VIDEO_PLAYER:
	 *            <ul>
	 *            <li>VideoPlayerComponent.PREVIEW_SURFACE
	 *            </ul>
	 * 
	 *            <li>MediaComponentAndroid.VIDEO_RECORDER:
	 *            <ul>
	 *            <li>VideoRecorderComponent.VIEW_SURFACE
	 *            <li>VideoRecorderComponent.DISPLAY_WIDTH
	 *            <li>VideoRecorderComponent.DISPLAY_HEIGHT
	 *            </ul>
	 * 
	 *            </ul>
	 * @return a MediaComponent
	 * @throws MsControlException
	 */
	@Override
	public MediaComponent createMediaComponent(Configuration<MediaComponent> predefinedConfig,
			Parameters params) throws MsControlException;

}
