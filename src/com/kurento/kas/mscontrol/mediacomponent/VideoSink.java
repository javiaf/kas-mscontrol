package com.kurento.kas.mscontrol.mediacomponent;

public interface VideoSink {

	public void putVideoFrame(byte[] data, int width, int height);
	
}
