package com.tikal.android.mscontrol.mediacomponent;

import com.tikal.mscontrol.Configuration;
import com.tikal.mscontrol.mediacomponent.MediaComponent;

public interface MediaComponentAndroid extends MediaComponent {

	/**
	 * To create an AudioPlayerComponent.
	 */
	public static final Configuration<MediaComponent> AUDIO_PLAYER = new Configuration<MediaComponent>(){};
	
	/**
	 * To create an AudioRecorderComponent.
	 */
	public static final Configuration<MediaComponent> AUDIO_RECORDER = new Configuration<MediaComponent>(){};
	
	/**
	 * To create an VideoPlayerComponent.
	 */
	public static final Configuration<MediaComponent> VIDEO_PLAYER = new Configuration<MediaComponent>(){};
	
	/**
	 * To create an VideoRecorderComponent.
	 */
	public static final Configuration<MediaComponent> VIDEO_RECORDER = new Configuration<MediaComponent>(){};
	
}
