package com.kurento.kas.mscontrol.mediacomponent;

import com.kurento.commons.mscontrol.Configuration;
import com.kurento.commons.mscontrol.Parameter;
import com.kurento.commons.mscontrol.mediacomponent.MediaComponent;

/**
 * MediaComponentAndroid is an interface that extends MediaComponent.<br>
 * 
 * Defines a set of Configuration<MediaComponent> to create a concrete
 * MediaComponent. They must be used in
 * createMediaComponent(Configuration<MediaComponent> predefinedConfig,
 * Parameters params)
 */
public interface MediaComponentAndroid extends MediaComponent {

	/**
	 * To create a component that record audio from microphone.
	 */
	public static final Configuration<MediaComponent> AUDIO_PLAYER = new Configuration<MediaComponent>() {
	};

	/**
	 * To create a component that play audio.
	 */
	public static final Configuration<MediaComponent> AUDIO_RECORDER = new Configuration<MediaComponent>() {
	};

	/**
	 * Parameter whose value must be an int indicates the stream type in
	 * Android, for example AudioManager.STREAM_MUSIC.
	 */
	public static final Parameter STREAM_TYPE = new Parameter() {
	};

	/**
	 * To create a component that record video from camera.
	 */
	public static final Configuration<MediaComponent> VIDEO_PLAYER = new Configuration<MediaComponent>() {
	};

	// public static final Parameter CAMERA = new Parameter(){};
	/**
	 * Parameter whose value must be an Android View to preview the camera
	 * video.
	 */
	public static final Parameter PREVIEW_SURFACE = new Parameter() {
	};

	/**
	 * To create a component that show video in a display.
	 */
	public static final Configuration<MediaComponent> VIDEO_RECORDER = new Configuration<MediaComponent>() {
	};

	/**
	 * Parameter whose value must be an Android View to view the received video.
	 */
	public static final Parameter VIEW_SURFACE = new Parameter() {
	};

	/**
	 * Parameter whose value must be an Integer value that indicate the display
	 * width.
	 */
	public static final Parameter DISPLAY_WIDTH = new Parameter() {
	};

	/**
	 * Parameter whose value must be an Integer value that indicate the display
	 * height.
	 */
	public static final Parameter DISPLAY_HEIGHT = new Parameter() {
	};
	
	/**
	 * Parameter whose value must be an Integer value that indicate the display
	 * orientation
	 */
	public static final Parameter DISPLAY_ORIENTATION = new Parameter() {
	};
	
	public boolean isStarted();

}
