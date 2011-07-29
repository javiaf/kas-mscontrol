package com.tikal.android.mscontrol.join;

import com.tikal.android.media.profiles.VideoProfile;
import com.tikal.android.media.rx.VideoRx;
import com.tikal.android.media.tx.MediaTx;
import com.tikal.android.mscontrol.mediacomponent.VideoSink;
import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.join.Joinable;
import com.tikal.mscontrol.join.JoinableContainer;

public class VideoJoinableStreamImpl extends JoinableStreamBase implements
		VideoSink, VideoRx {

	private VideoProfile videoProfile;

	
	public VideoProfile getVideoProfile() {
		return videoProfile;
	}

	public VideoJoinableStreamImpl(JoinableContainer container,
			StreamType type, VideoProfile videoProfile) {
		super(container, type);
		this.videoProfile = videoProfile;
	}

	@Override
	public void putVideoFrame(byte[] frame) {
		MediaTx.putVideoFrame(frame);
	}

	@Override
	public void putVideoFrameRx(int[] rgb, int width, int height) {
		try {
			for (Joinable j : getJoinees(Direction.SEND))
				if (j instanceof VideoRx)
					((VideoRx) j).putVideoFrameRx(rgb, width, height);
			for (Joinable j : getJoinees(Direction.DUPLEX))
				if (j instanceof VideoRx)
					((VideoRx) j).putVideoFrameRx(rgb, width, height);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
