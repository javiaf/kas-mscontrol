package com.kurento.kas.mscontrol.mediacomponent.internal;

public interface AudioSink {

	public void putAudioSamples(short[] in_buffer, int in_size);
	
}
