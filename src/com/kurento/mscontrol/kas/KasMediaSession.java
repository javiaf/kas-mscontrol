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

package com.kurento.mscontrol.kas;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import com.kurento.commons.config.Parameter;
import com.kurento.commons.config.Parameters;
import com.kurento.kas.media.codecs.AudioCodecType;
import com.kurento.kas.media.codecs.VideoCodecType;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.Mode;
import com.kurento.mscontrol.commons.Configuration;
import com.kurento.mscontrol.commons.MediaSession;
import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.commons.mediacomponent.MediaComponent;
import com.kurento.mscontrol.kas.mediacomponent.MediaComponentAndroid;
import com.kurento.mscontrol.kas.networkconnection.NetIF;
import com.kurento.mscontrol.kas.networkconnection.PortRange;

/**
 * KasMediaSession is an interface that extends MediaSession.<br>
 * 
 * Defines a set of parameters to configure a MediaSession instance.
 * 
 */
public interface KasMediaSession extends MediaSession {

	/**
	 * String to indicate the stun server name
	 */
	public static final Parameter<String> STUN_HOST = new Parameter<String>(
			"STUN_HOST");

	/**
	 * Integer to indicate the stun server port
	 */
	public static final Parameter<Integer> STUN_PORT = new Parameter<Integer>(
			"STUN_PORT");

	/**
	 * MediaSessionAndoid.NetIF to indicate if the network interface is WIFI or
	 * MOBILE.
	 */
	public static final Parameter<NetIF> NET_IF = new Parameter<NetIF>("NET_IF");

	/**
	 * InetAddress to indicate the local IP address
	 */
	public static final Parameter<InetAddress> LOCAL_ADDRESS = new Parameter<InetAddress>(
			"LOCAL_ADDRESS");

	/**
	 * Integer to indicate the max bandwidth will be used in bps(bits per
	 * second).
	 */
	public static final Parameter<Integer> MAX_BANDWIDTH = new Parameter<Integer>(
			"MAX_BANDWIDTH");

	/**
	 * Integer to indicate the max delay for media reception in ms
	 * (miliseconds).
	 */
	public static final Parameter<Integer> MAX_DELAY = new Parameter<Integer>(
			"MAX_DELAY");

	/**
	 * Map&lt;MediaType, Mode&gt; to indicate the mode of each media stream.
	 */
	public static final Parameter<Map<MediaType, Mode>> STREAMS_MODES = new Parameter<Map<MediaType, Mode>>(
			"STREAMS_MODES");

	/**
	 * List&lt;AudioCodecType&gt; to indicate the audio codecs supported.
	 */
	public static final Parameter<List<AudioCodecType>> AUDIO_CODECS = new Parameter<List<AudioCodecType>>(
			"AUDIO_CODECS");

	/**
	 * PortRange to indicate local port interval to select an audio port.
	 */
	public static final Parameter<PortRange> AUDIO_LOCAL_PORT_RANGE = new Parameter<PortRange>(
			"AUDIO_LOCAL_PORT_RANGE");

	/**
	 * List&lt;VideoCodecType&gt; to indicate the video codecs supported.
	 */
	public static final Parameter<List<VideoCodecType>> VIDEO_CODECS = new Parameter<List<VideoCodecType>>(
			"VIDEO_CODECS");

	/**
	 * PortRange to indicate local port interval to select a video port.
	 */
	public static final Parameter<PortRange> VIDEO_LOCAL_PORT_RANGE = new Parameter<PortRange>(
			"VIDEO_LOCAL_PORT_RANGE");

	/**
	 * Integer to indicate the frame width in pixels.
	 */
	public static final Parameter<Integer> FRAME_WIDTH = new Parameter<Integer>(
			"FRAME_WIDTH");

	/**
	 * Integer to indicate the frame height in pixels.
	 */
	public static final Parameter<Integer> FRAME_HEIGHT = new Parameter<Integer>(
			"FRAME_HEIGHT");

	/**
	 * Integer to indicate the max frame rate will be used.
	 */
	public static final Parameter<Integer> MAX_FRAME_RATE = new Parameter<Integer>(
			"MAX_FRAME_RATE");
	/**
	 * Integer to indicate the max number of frames in a group of pictures, or 0
	 * for intra_only.
	 */
	public static final Parameter<Integer> GOP_SIZE = new Parameter<Integer>(
			"GOP_SIZE");

	/**
	 * Integer to indicate the number of frames will be buffered from the
	 * camera.
	 */
	public static final Parameter<Integer> FRAMES_QUEUE_SIZE = new Parameter<Integer>(
			"FRAMES_QUEUE_SIZE");

	/**
	 * Boolean to indicate if the received media streams must be synchronized
	 * when they are show to the user.
	 */
	public static final Parameter<Boolean> SYNCHRONIZE_MEDIA_STREAMS = new Parameter<Boolean>(
			"SYNCHRONIZE_MEDIA_STREAMS");

	/**
	 * Create a MediaComponentAndroid.
	 * 
	 * @param predefinedConfig
	 *            Defines the concrete MediaComponentAndroid to create. <br>
	 *            Predefined Configurations are:
	 *            <ul>
	 *            <li>{@link MediaComponentAndroid#AUDIO_PLAYER AUDIO_PLAYER}: create a
	 *            component that record audio from microphone.
	 *            <li>{@link MediaComponentAndroid#AUDIO_RECORDER AUDIO_RECORDER}: create a
	 *            component that play audio.
	 *            <li>{@link MediaComponentAndroid#VIDEO_PLAYER VIDEO_PLAYER}: create a
	 *            component that record video from camera.
	 *            <li>{@link MediaComponentAndroid#VIDEO_RECORDER VIDEO_RECORDER}: create a
	 *            component that show video in a display.
	 *            </ul>
	 * @param params
	 *            Parameters to create a MediaComponent. Each concrete component
	 *            has its own parameters:
	 * 
	 *            <table border="1" cellpadding=5>
	 *            <tr>
	 *            <thead>
	 *            <th>Configurarion&lt;MediaComponentAndroid&gt;</th>
	 *            <th colspan=3>Parameters</th>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <th></th>
	 *            <th>Parameter</th>
	 *            <th>Value type</th>
	 *            <th>Description</th>
	 *            </tr>
	 *            </thead> <tbody>
	 * 
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid#AUDIO_PLAYER AUDIO_PLAYER}</td>
	 *            <td colspan=3>{@link Parameters#NO_PARAMETER NO_PARAMETER}</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid#AUDIO_RECORDER AUDIO_RECORDER}</td>
	 *            <td>{@link MediaComponentAndroid#STREAM_TYPE STREAM_TYPE}</td>
	 *            <td>int (see android.media.AudioManager)</td>
	 *            <td>Speaker that will be used.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid#VIDEO_PLAYER VIDEO_PLAYER}</td>
	 *            <td>{@link MediaComponentAndroid#PREVIEW_SURFACE PREVIEW_SURFACE}</td>
	 *            <td>android.view.View</td>
	 *            <td>Surface in which the preview images from the camera will
	 *            be previewed</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td rowspan=4 valign=top>
	 *            {@link MediaComponentAndroid#VIDEO_RECORDER VIDEO_RECORDER}</td>
	 *            <td>{@link MediaComponentAndroid#VIEW_SURFACE VIEW_SURFACE}</td>
	 *            <td>android.view.View</td>
	 *            <td>Surface in which the video received will be played.</td>
	 *            </tr>
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid#DISPLAY_WIDTH DISPLAY_WIDTH}</td>
	 *            <td>int</td>
	 *            <td>Width of surface designated by
	 *            {@link MediaComponentAndroid#VIEW_SURFACE VIEW_SURFACE}</td>
	 *            </tr>
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid#DISPLAY_HEIGHT DISPLAY_HEIGHT}</td>
	 *            <td>int</td>
	 *            <td>Height of surface designated by
	 *            {@link MediaComponentAndroid#VIEW_SURFACE VIEW_SURFACE}</td>
	 *            </tr>
	 *            <tr>
	 *            <td>{@link MediaComponentAndroid#DISPLAY_ORIENTATION DISPLAY_ORIENTATION}</td>
	 *            <td>int</td>
	 *            <td>Orientation in which the video will be played.</td>
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
