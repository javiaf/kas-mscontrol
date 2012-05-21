package com.kurento.kas.mscontrol.mediacomponent.internal;

import com.kurento.kas.media.rx.VideoFrame;

public interface VideoRecorder {

	public void putVideoFrame(VideoFrame videoFrame, VideoFeeder feeder);

}
