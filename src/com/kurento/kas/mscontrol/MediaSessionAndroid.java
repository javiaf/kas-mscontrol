package com.kurento.kas.mscontrol;

import com.kurento.commons.mscontrol.Configuration;
import com.kurento.commons.mscontrol.MediaSession;
import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.Parameter;
import com.kurento.commons.mscontrol.Parameters;
import com.kurento.commons.mscontrol.mediacomponent.MediaComponent;
import com.kurento.kas.mscontrol.mediacomponent.MediaComponentAndroid;

/**
 * MediaSessionAndroid is an interface that extends MediaSession.<br>
 * 
 * Defines a set of parameters to config a MediaSession instance.
 * 
 * @author mparis
 * 
 */
public interface MediaSessionAndroid extends MediaSession {

	/**
	 * Parameter whose value must be a NetIF.
	 */
	public static final Parameter NET_IF = new Parameter() {
	};

	/**
	 * Parameter whose value must be an InetAddress.
	 */
	public static final Parameter LOCAL_ADDRESS = new Parameter() {
	};

	/**
	 * Parameter whose value must be an Integer.
	 */
	public static final Parameter MAX_BANDWIDTH = new Parameter() {
	};

	/**
	 * Parameter whose value must be a HashMap&lt;MediaType, Mode&gt;.
	 */
	public static final Parameter STREAMS_MODES = new Parameter() {
	};

	/**
	 * Parameter whose value must be an ArrayList&lt;AudioCodecType&gt;.
	 */
	public static final Parameter AUDIO_CODECS = new Parameter() {
	};

	/**
	 * Parameter whose value must be an ArrayList&lt;VideoCodecType&gt;.
	 */
	public static final Parameter VIDEO_CODECS = new Parameter() {
	};

	/**
	 * Parameter whose value must be a Dimension.
	 */
	public static final Parameter FRAME_SIZE = new Parameter() {
	};

	/**
	 * Parameter whose value must be an Integer.
	 */
	public static final Parameter MAX_FRAME_RATE = new Parameter() {
	};
	/**
	 * Parameter whose value must be an Integer.
	 */
	public static final Parameter GOP_SIZE = new Parameter() {
	};

	/**
	 * Parameter whose value must be an Integer.
	 */
	public static final Parameter FRAMES_QUEUE_SIZE = new Parameter() {
	};

	/**
	 * Create a MediaComponentAndroid.
	 * 
	 * @param predefinedConfig
	 *            Defines the concrete MediaComponentAndroid to create. <br>
	 *            Predefined Configurations are:
	 *            <ul>
	 *            <li>{@link MediaComponentAndroid.AUDIO_PLAYER}: create a
	 *            component that record audio from microphone.
	 *            <li>{@link MediaComponentAndroid.AUDIO_RECORDER}: create a
	 *            component that play audio.
	 *            <li>{@link MediaComponentAndroid.VIDEO_PLAYER}: create a
	 *            component that record video from camera.
	 *            <li>{@link MediaComponentAndroid.VIDEO_RECORDER}: create a
	 *            component that show video in a display.
	 *            </ul>
	 * @param params
	 *            Parameters to create a MediaComponent. Each concrete component
	 *            has its own parameters:
	 * 
	 *            <table border="1" cellpadding=5>
	 *            <col width="25%"/> <col width="75%"/> <thead>
	 *            <tr>
	 *            <th>Configurarion&lt;MediaComponentAndroid&gt;</th>
	 *            <th colspan=2>Parameters</th>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <th></th>
	 *            <th>Parameter</th>
	 *            <th>Value type</th>
	 *            </tr>
	 *            </thead> <tbody>
	 * 
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid.AUDIO_PLAYER}</td>
	 *            <td colspan=2>{@link Parameters.NO_PARAMETER}</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid.AUDIO_RECORDER}</td>
	 *            <td>{@link MediaComponentAndroid.STREAM_TYPE}</td>
	 *            <td>int</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid.VIDEO_PLAYER}</td>
	 *            <td>{@link MediaComponentAndroid.PREVIEW_SURFACE}</td>
	 *            <td>android.view.View</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td rowspan=3 valign=top>
	 *            {@link MediaComponentAndroid.VIDEO_RECORDER}</td>
	 *            <td>{@link MediaComponentAndroid.VIEW_SURFACE}</td>
	 *            <td>android.view.View</td>
	 *            </tr>
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid.DISPLAY_WIDTH}</td>
	 *            <td>int</td>
	 *            </tr>
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid.DISPLAY_HEIGHT}</td>
	 *            <td>int</td>
	 *            </tr>
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid.DISPLAY_ORIENTATION}</td>
	 *            <td>int</td>
	 *            </tr>
	 * 
	 * 
	 *            </tbody>
	 *            </table>
	 * 
	 * @return a MediaComponentAndroid
	 * @throws MsControlException
	 */
	@Override
	public MediaComponentAndroid createMediaComponent(
			Configuration<MediaComponent> predefinedConfig, Parameters params)
			throws MsControlException;

}
