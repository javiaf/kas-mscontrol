package com.kurento.mscontrol.kas.mediacomponent;

/**
 * AndroidInfo is used to ask to some media components for a type of info.
 */
public enum AndroidInfo {
	/**
	 * This info can be asked to a {@link MediaComponentAndroid#AUDIO_RECORDER}
	 * or a {@link MediaComponentAndroid#VIDEO_RECORDER} object to obtain the
	 * media packet queue size.
	 */
	RECORDER_QUEUE,
	/**
	 * This info can be asked to a a
	 * {@link MediaComponentAndroid#VIDEO_RECORDER} object to obtain the frame
	 * width
	 */
	FRAME_WIDTH,
	/**
	 * This info can be asked to a a
	 * {@link MediaComponentAndroid#VIDEO_RECORDER} object to obtain the frame
	 * height
	 */
	FRAME_HEIGHT

}
