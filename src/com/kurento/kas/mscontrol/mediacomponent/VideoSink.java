package com.kurento.kas.mscontrol.mediacomponent;

public interface VideoSink {

	public void putVideoFrame(byte[] frame, int width, int height);
	
}
