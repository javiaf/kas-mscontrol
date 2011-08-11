package com.tikal.android.mscontrol.mediacomponent;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.tikal.android.media.rx.VideoRx;
import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.Parameters;

public class VideoRecorderComponent extends MediaComponentBase implements
		VideoRx {

	private static final String LOG_TAG = "VideoRecorder";

	private SurfaceView mVideoReceiveView;
	private SurfaceHolder mHolderReceive;
	private Surface mSurfaceReceive;
	private View videoSurfaceRx;

	private int screenWidth;
	private int screenHeight;
	private RectF dirty2;
	private Canvas canvas = new Canvas();

	Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

	private boolean isRecording = false;

	public View getVideoSurfaceRx() {
		return videoSurfaceRx;
	}

	@Override
	public boolean isStarted() {
		return isRecording;
	}

	public VideoRecorderComponent(Parameters params) throws MsControlException {
		if (params == null)
			throw new MsControlException("Parameters are NULL");

		View surface = (View) params.get(VIEW_SURFACE);
		if (surface == null)
			throw new MsControlException(
					"Params must have VideoRecorderComponent.VIEW_SURFACE param");
		Integer displayWidth = (Integer) params.get(DISPLAY_WIDTH);
		if (displayWidth == null)
			throw new MsControlException(
					"Params must have VideoRecorderComponent.DISPLAY_WIDTH param");
		Integer displayHeight = (Integer) params.get(DISPLAY_HEIGHT);
		if (displayHeight == null)
			throw new MsControlException(
					"Params must have VideoRecorderComponent.DISPLAY_HEIGHT param");

		this.videoSurfaceRx = surface;
		this.screenWidth = displayWidth;
		this.screenHeight = displayHeight * 3 / 4;
		dirty2 = new RectF(0, 0, screenWidth, screenHeight);
		if (surface != null) {
			mVideoReceiveView = (SurfaceView) videoSurfaceRx;
			mHolderReceive = mVideoReceiveView.getHolder();
			mSurfaceReceive = mHolderReceive.getSurface();
		}
	}

	@Override
	public void putVideoFrameRx(int[] rgb, int width, int height) {
		if (!isRecording)
			return;
		if (rgb == null || rgb.length == 0)
			return;

		try {
			if (mSurfaceReceive == null)
				return;

			canvas = mSurfaceReceive.lockCanvas(null);
			if (canvas == null)
				return;

			// RectF dirty2 = new RectF(0, 0, screenWidth, screenHeight);

			Bitmap srcBitmap = Bitmap.createBitmap(rgb, width, height,
					Bitmap.Config.ARGB_8888);

			canvas.drawBitmap(srcBitmap, null, dirty2, null);
			Canvas.freeGlCaches();
			if (mSurfaceReceive == null)
				return;
			mSurfaceReceive.unlockCanvasAndPost(canvas);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG, "Exception: " + e.toString());
			e.printStackTrace();
		} catch (OutOfResourcesException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG, "Exception: " + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		isRecording = true;
	}

	@Override
	public void stop() {
		isRecording = false;
	}

}
