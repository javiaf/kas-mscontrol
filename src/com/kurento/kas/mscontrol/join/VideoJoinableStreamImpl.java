package com.kurento.kas.mscontrol.join;

import android.util.Log;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.join.Joinable;
import com.kurento.commons.mscontrol.join.JoinableContainer;
import com.kurento.kas.media.profiles.VideoProfile;
import com.kurento.kas.media.rx.VideoRx;
import com.kurento.kas.media.tx.MediaTx;
import com.kurento.kas.mscontrol.mediacomponent.VideoSink;

public class VideoJoinableStreamImpl extends JoinableStreamBase implements
		VideoSink, VideoRx {

	public final static String LOG_TAG = "VideoJoinableStream";

	private VideoProfile videoProfile;

	private static long t_suma = 0;
	private static long n = 1;

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
		long t_init = System.currentTimeMillis();

		MediaTx.putVideoFrame(frame);

		long t_fin = System.currentTimeMillis();
		long tiempo = t_fin - t_init;
		t_suma += tiempo;
		long t_medio = t_suma / n;
		Log.d(LOG_TAG, "Tiempo: " + tiempo + "\t\tTiempo medio: " + t_medio);
		n++;
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
