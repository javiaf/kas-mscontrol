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

package com.kurento.kas.mscontrol.mediacomponent.internal;

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

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.Parameters;
import com.kurento.kas.media.rx.VideoRx;

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
		this.screenHeight = displayHeight;// * 3 / 4;
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

			int heighAux = height;
			int widthAux = width;

			double aux = (double) screenHeight / (double) heighAux;
			heighAux = (int) (aux * heighAux);
			widthAux = (int) (aux * widthAux);

			Bitmap srcBitmap = Bitmap.createBitmap(rgb, width, height,
					Bitmap.Config.ARGB_8888);

			dirty2 = new RectF(0, 0, widthAux, heighAux);
			canvas.drawBitmap(srcBitmap, null, dirty2, null);
			Canvas.freeGlCaches();
			if (mSurfaceReceive == null)
				return;
			mSurfaceReceive.unlockCanvasAndPost(canvas);
		} catch (IllegalArgumentException e) {
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
