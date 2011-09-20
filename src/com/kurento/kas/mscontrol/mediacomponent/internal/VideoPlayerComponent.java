package com.kurento.kas.mscontrol.mediacomponent.internal;

import java.io.IOException;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.Parameters;
import com.kurento.commons.mscontrol.join.Joinable;
import com.kurento.kas.media.profiles.VideoProfile;
import com.kurento.kas.mscontrol.join.VideoJoinableStreamImpl;

public class VideoPlayerComponent extends MediaComponentBase implements
		PreviewCallback {
	// SurfaceHolder.Callback, {

	private static final String LOG_TAG = "VideoPlayer";

	private SurfaceView mVideoView;
	// private SurfaceHolder mHolder;

	private Camera mCamera;
	private View videoSurfaceTx;

	private int width;
	private int height;
	private int screenOrientation;

	public View getVideoSurfaceTx() {
		return videoSurfaceTx;
	}

	@Override
	public boolean isStarted() {
		return mCamera != null;
	}

	public VideoPlayerComponent(Parameters params) throws MsControlException {
		if (params == null)
			throw new MsControlException("Parameters are NULL");

		final View sv = (View) params.get(PREVIEW_SURFACE);
		if (sv == null)
			throw new MsControlException(
					"Params must have VideoPlayerComponent.PREVIEW_SURFACE param");

		videoSurfaceTx = sv;
		screenOrientation = (Integer) params.get(DISPLAY_ORIENTATION) * 90;

		Log.d(LOG_TAG, "VideoPlayerComponent " + videoSurfaceTx.toString());

	}

	private Camera openFrontFacingCameraGingerbread() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				try {
					cam = Camera.open(camIdx);
				} catch (RuntimeException e) {
					Log.e(LOG_TAG,
							"Camera failed to open: " + e.getLocalizedMessage()
									+ " ; " + e.toString());
				}
			}
		}

		return cam;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (data == null)
			return;
		// Send frame to subscribers
		try {
			for (Joinable j : getJoinees(Direction.SEND))
				if (j instanceof VideoSink)
					((VideoSink) j).putVideoFrame(data, width, height);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void start() throws MsControlException {

		VideoProfile videoProfile = null;
		for (Joinable j : getJoinees(Direction.SEND))
			if (j instanceof VideoJoinableStreamImpl) {
				videoProfile = ((VideoJoinableStreamImpl) j).getVideoProfile();
			}
		if (videoProfile == null)
			throw new MsControlException("Cannot get video profile.");

		this.width = videoProfile.getWidth();
		this.height = videoProfile.getHeight();

		mVideoView = (SurfaceView) videoSurfaceTx;

		final SurfaceHolder mHolder2 = mVideoView.getHolder();
		mHolder2.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHolder2.addCallback(new Callback() {

			public void surfaceDestroyed(SurfaceHolder holder) {
				Log.d(LOG_TAG, "Surface Destroy");
				if (mCamera != null) {
					mCamera.setPreviewCallback(null);
					mCamera.stopPreview();
					mCamera.release();
					mCamera = null;
				}
			}

			public void surfaceCreated(SurfaceHolder holder) {

				Log.d(LOG_TAG, "Surface Create");

				if (mCamera == null) {
					if (VERSION.SDK_INT < 9) {
						mCamera = Camera.open();
					} else
						mCamera = openFrontFacingCameraGingerbread();
				}
				mCamera.setErrorCallback(new ErrorCallback() {
					public void onError(int error, Camera camera) {
						Log.e(LOG_TAG, "Camera error : " + error);
					}
				});

				Log.d(LOG_TAG, "mCamera = " + mCamera.toString());
				Camera.Parameters parameters = mCamera.getParameters();

				// parameters.set("camera-id", 2);
				// mCamera.setParameters(parameters);

				List<Size> sizes = parameters.getSupportedPreviewSizes();
				// Video Preferences is support?
				boolean isSupport = false;
				int sizeSelected = -1;
				for (int i = 0; i < sizes.size(); i++) {
					if ((width == sizes.get(i).width)
							&& (height == sizes.get(i).height)) {
						isSupport = true;
						break;
					}
					if (sizeSelected == -1) {
						if (sizes.get(i).width <= width)
							sizeSelected = i;
					} else if ((sizes.get(i).width >= sizes.get(sizeSelected).width)
							&& (sizes.get(i).width <= width))
						sizeSelected = i;
				}
				if (sizeSelected == -1)
					sizeSelected = 0;
				if (!isSupport) {
					width = sizes.get(sizeSelected).width;
					height = sizes.get(sizeSelected).height;
				}
				parameters.setPreviewSize(width, height);
				mCamera.setParameters(parameters);

				String cad = "";
				for (int i = 0; i < sizes.size(); i++)
					cad += sizes.get(i).width + " x " + sizes.get(i).height
							+ "\n";
				Log.d(LOG_TAG, "getPreviewSize: "
						+ parameters.getPreviewSize().width + " x "
						+ parameters.getPreviewSize().height);
				Log.d(LOG_TAG, "getSupportedPreviewSizes:\n" + cad);

				// int result = 0;
				// if (VERSION.SDK_INT < 9) {
				// result = (360 + 90 - screenOrientation) % 360;
				// // mCamera.setDisplayOrientation(result);
				//
				// } else {
				//
				// android.hardware.Camera.CameraInfo info = new
				// android.hardware.Camera.CameraInfo();
				// android.hardware.Camera.getCameraInfo(0, info);
				// int rotation = screenOrientation;
				// int degrees = 0;
				// switch (rotation) {
				// case Surface.ROTATION_0:
				// degrees = 0;
				// break;
				// case Surface.ROTATION_90:
				// degrees = 90;
				// break;
				// case Surface.ROTATION_180:
				// degrees = 180;
				// break;
				// case Surface.ROTATION_270:
				// degrees = 270;
				// break;
				// }
				//
				// if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				// result = (info.orientation + degrees) % 360;
				// result = (360 - result) % 360; // compensate the mirror
				// } else { // back-facing
				// result = (info.orientation - degrees + 360) % 360;
				// }
				// Log.d(LOG_TAG, "info.orientation = " + info.orientation
				// + "; Result-Orientation = " + result);
				// mCamera.setDisplayOrientation(result);
				//
				// }
				// parameters.setRotation(result);
				// mCamera.setParameters(parameters);

				try {

					mCamera.setPreviewDisplay(mHolder2);
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					mCamera.startPreview();

				} catch (Throwable e) {
					Log.e(LOG_TAG, "Can't start camera preview");
				}

			}

			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				mCamera.setPreviewCallback(VideoPlayerComponent.this);
			}
		});

		// if (videoSurfaceTx != null) {
		// mVideoView = (SurfaceView) videoSurfaceTx;
		// mHolder = mVideoView.getHolder();
		// mHolder.addCallback(this);
		// mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// }
		// startRecording();
	}

	@Override
	public void stop() {
		Log.d(LOG_TAG, "Stop");
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
		}
	}
}
