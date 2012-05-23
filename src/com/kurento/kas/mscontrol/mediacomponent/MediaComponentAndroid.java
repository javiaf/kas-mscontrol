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

package com.kurento.kas.mscontrol.mediacomponent;

import com.kurento.commons.mscontrol.Configuration;
import com.kurento.commons.mscontrol.MsControlException;
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

	/**
	 * Parameter whose value must be an int as Camera_Facing_Back or Camera_Facing_Front
	 */
	public static final Parameter CAMERA_FACING = new Parameter(){};
	
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

	public void onAction(AndroidAction action) throws MsControlException;

}
