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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.Parameters;
import com.kurento.kas.media.rx.VideoFrame;
import com.kurento.kas.media.rx.VideoRx;

public class VideoRecorderComponent extends MediaComponentBase implements
		Recorder, VideoRx {

	private static final String LOG_TAG = "NDK-video-rx";

	private SurfaceView mVideoReceiveView;
	private SurfaceHolder mHolderReceive;
	private Surface mSurfaceReceive;
	private View videoSurfaceRx;

	// private int screenWidth;
	private int screenHeight;

	private boolean isRecording = false;
	private long targetPtsNorm;

	private SurfaceControl surfaceControl = null;

	private int QUEUE_SIZE = 100;
	private BlockingQueue<VideoFrame> videoFramesQueue;

	private final Object controll = new Object();

	public View getVideoSurfaceRx() {
		return videoSurfaceRx;
	}

	public synchronized boolean isRecording() {
		return isRecording;
	}

	public synchronized void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	public long getTargetPtsNorm() {
		return targetPtsNorm;
	}

	public void setTargetPtsNorm(long targetPtsNorm) {
		this.targetPtsNorm = targetPtsNorm;
	}

	@Override
	public boolean isStarted() {
		return isRecording();
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
		// this.screenWidth = displayWidth;
		this.screenHeight = displayHeight;

		mVideoReceiveView = (SurfaceView) videoSurfaceRx;
		mHolderReceive = mVideoReceiveView.getHolder();
		mSurfaceReceive = mHolderReceive.getSurface();

		this.videoFramesQueue = new ArrayBlockingQueue<VideoFrame>(QUEUE_SIZE);
	}

	@Override
	public void putVideoFrameRx(VideoFrame videoFrame) {
		Log.d(LOG_TAG, "queue size: " + videoFramesQueue.size());
		Log.d(LOG_TAG, "width: " + videoFrame.getWidth() + "\theight: "
				+ videoFrame.getHeight());
		if ((videoFramesQueue.size() >= QUEUE_SIZE)
				|| (videoFrame.getPts() < 0)) {
			// VideoFrame vf = videoFramesQueue.poll();
			// if (vf != null)
				Log.w(LOG_TAG, "jitter_buffer_overflow: Drop video frame");
			return;
		}
		videoFramesQueue.offer(videoFrame);
	}

	@Override
	public void start() {
		Log.d(LOG_TAG, "QUEUE_SIZE: " + QUEUE_SIZE);
		surfaceControl = new SurfaceControl();
		surfaceControl.start();
		// startRecord();
		RecorderControllerComponent.getInstance().addRecorder(this);
		Log.d(LOG_TAG, "add to controller");
	}

	@Override
	public void stop() {
		RecorderControllerComponent.getInstance().deleteRecorder(this);
		stopRecord();
		if (surfaceControl != null)
			surfaceControl.interrupt();
	}

	private class SurfaceControl extends Thread {
		@Override
		public void run() {
			try {
				if (mSurfaceReceive == null) {
					Log.e(LOG_TAG, "mSurfaceReceive is null");
					return;
				}

				VideoFrame videoFrameProcessed;
				int[] rgb;
				int width, height, heighAux, widthAux;
				int lastHeight = 0;
				int lastWidth = 0;
				double aux;

				Canvas canvas = null;
				Rect dirty = null;
				Bitmap srcBitmap = null;

				long tStart, tEnd;
				long i = 1;
				long t;
				long total = 0;

				for (;;) {
					if (!isRecording()) {
						synchronized (controll) {
							controll.wait();
						}
						continue;
					}

					if (videoFramesQueue.isEmpty())
						Log.w(LOG_TAG,
								"jitter_buffer_underflow: Video frames queue is empty");

					long targetPtsNorm = getTargetPtsNorm();
					if (targetPtsNorm != -1) {
						long ptsNorm = calcPtsNorm(videoFramesQueue.peek());
						Log.d(LOG_TAG, "ptsNorm: " + ptsNorm + " targetPts: "
								+ targetPtsNorm);
						if ((ptsNorm == -1) || (ptsNorm > targetPtsNorm)) {
							Log.d(LOG_TAG, "wait");
							synchronized (controll) {
								controll.wait();
							}
							continue;
						}
					}
					videoFrameProcessed = videoFramesQueue.take();
					Log.d(LOG_TAG, "play frame "
							+ calcPtsNorm(videoFrameProcessed));
					tStart = System.currentTimeMillis();

					rgb = videoFrameProcessed.getDataFrame();
					width = videoFrameProcessed.getWidth();
					height = videoFrameProcessed.getHeight();

					if (rgb == null || rgb.length == 0)
						continue;

					try {
						canvas = mSurfaceReceive.lockCanvas(null);
						if (canvas == null)
							continue;

						if (height != lastHeight) {
							if (width != lastWidth || srcBitmap == null) {
								if (srcBitmap != null)
									srcBitmap.recycle();
								srcBitmap = Bitmap.createBitmap(width, height,
										Bitmap.Config.ARGB_8888);
								lastWidth = width;
								if (srcBitmap == null)
									Log.w(LOG_TAG, "srcBitmap is null");
							}

							aux = (double) screenHeight / (double) height;
							heighAux = screenHeight;
							widthAux = (int) (aux * width);

							dirty = new Rect(0, 0, widthAux, heighAux);

							lastHeight = height;
						}
						if (srcBitmap != null) {
							srcBitmap.setPixels(rgb, 0, width, 0, 0, width,
									height);
							canvas.drawBitmap(srcBitmap, null, dirty, null);
						}
						mSurfaceReceive.unlockCanvasAndPost(canvas);
					} catch (IllegalArgumentException e) {
						Log.e(LOG_TAG, "Exception: " + e.toString());
					} catch (OutOfResourcesException e) {
						Log.e(LOG_TAG, "Exception: " + e.toString());
					}

					tEnd = System.currentTimeMillis();
					t = tEnd - tStart;
					total += t;
					Log.d(LOG_TAG, "frame played in: " + t + " ms. Average: "
							+ (total / i));
					i++;
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "SurfaceControl stopped");
			}
		}
	}

	@Override
	public long getPtsNorm() {
		return calcPtsNorm(videoFramesQueue.peek());
	}

	@Override
	public boolean hasMediaPacket() {
		return !videoFramesQueue.isEmpty();
	}

	@Override
	public void startRecord() {
		startRecord(-1);
	}

	@Override
	public void startRecord(long targetPtsNorm) {
		setTargetPtsNorm(targetPtsNorm);
		setRecording(true);
		synchronized (controll) {
			controll.notify();
		}
	}

	@Override
	public void stopRecord() {
		setRecording(false);
	}

	private long calcPtsNorm(VideoFrame vf) {
		if (vf == null)
			return -1;

		// Log.d(LOG_TAG,
		// "vf.getPts(): " + vf.getPts() + " vf.getStartTime(): "
		// + vf.getStartTime() + " vf.getTimeBaseDen(): "
		// + vf.getTimeBaseDen() + " vf.getTimeBaseNum(): "
		// + vf.getTimeBaseNum());

		return 1000 * ((vf.getPts() - vf.getStartTime()) * vf
				.getTimeBaseNum()) / vf.getTimeBaseDen();
	}

}
