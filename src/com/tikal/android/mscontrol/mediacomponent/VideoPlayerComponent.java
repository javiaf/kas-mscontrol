package com.tikal.android.mscontrol.mediacomponent;

import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.tikal.android.media.profiles.VideoProfile;
import com.tikal.android.mscontrol.join.VideoJoinableStreamImpl;
import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.Parameters;
import com.tikal.mscontrol.join.Joinable;

public class VideoPlayerComponent extends MediaComponentBase implements
		SurfaceHolder.Callback, PreviewCallback {	
	
	private static final String LOG_TAG = "VideoPlayer";

	private SurfaceView mVideoView;
	private SurfaceHolder mHolder;

	private Camera mCamera;
	private View videoSurfaceTx;

	private int width;
	private int height;
	
	
	
	public View getVideoSurfaceTx() {
		return videoSurfaceTx;
	}
	
	@Override
	public boolean isStarted() {
		return mCamera!=null;
	}

	public VideoPlayerComponent(Parameters params)
			throws MsControlException {
		if(params == null)
			throw new MsControlException("Parameters are NULL");
		
		View videoSurfaceTx = (View) params.get(PREVIEW_SURFACE);
		if(videoSurfaceTx == null)
			throw new MsControlException("Params must have VideoPlayerComponent.PREVIEW_SURFACE param");
		this.videoSurfaceTx = videoSurfaceTx;
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

	private void startRecording() {
		Log.d(LOG_TAG, "Start Camera Capturing");

		Log.d(LOG_TAG, " Versión SDK " + VERSION.SDK_INT);

		if (mCamera == null) {
			if (VERSION.SDK_INT < 9) {
				mCamera = Camera.open();
			} else
				mCamera = openFrontFacingCameraGingerbread();
		}

		Camera.Parameters parameters = mCamera.getParameters();

		// parameters.set("camera-id", 2);
		// mCamera.setParameters(parameters);

		List<Size> sizes = parameters.getSupportedPreviewSizes();
		String cad = "";
		// Video Preferences is support?
		boolean isSupport = false;
		for (int i = 0; i < sizes.size(); i++) {
			cad += sizes.get(i).width + " x " + sizes.get(i).height + "\n";
			if ((width == sizes.get(i).width)
					&& (height == sizes.get(i).height)) {
				isSupport = true;
				break;
			}
		}
		if (!isSupport) {
			width = sizes.get(0).width;
			height = sizes.get(0).height;
		}
		parameters.setPreviewSize(width, height);
		mCamera.setParameters(parameters);

		mCamera.setPreviewCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(LOG_TAG, "surface Changed");
		if (mCamera != null)
			mCamera.startPreview();
		// Parameters params = mCamera.getParameters();
		// if (mCamera != null) {
		// int degrees = 90;
		//
		// mCamera.setDisplayOrientation(degrees);
		// Camera.Parameters parameters = mCamera.getParameters();
		//
		// parameters.setRotation(degrees);
		// // params.setPreviewSize(width, height);
		// mCamera.setParameters(parameters);
		//
		// mCamera.startPreview();
		// }
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			Log.d(LOG_TAG, "Surface Create");
			if (mCamera == null) {
				if (VERSION.SDK_INT < 9) {
					mCamera = Camera.open();
				} else
					mCamera = openFrontFacingCameraGingerbread();
			}

			// mCamera.setPreviewCallback(this);
			// mCamera = Camera.open();
			if (mCamera != null) {
				Log.d(LOG_TAG, " mCamera opened " + mCamera.toString());

				// int degrees = 90;
				//
				// mCamera.setDisplayOrientation(degrees);
				// Camera.Parameters parameters = mCamera.getParameters();
				//
				// parameters.setRotation(degrees);
				// // params.setPreviewSize(width, height);
				// mCamera.setParameters(parameters);

				mCamera.setPreviewDisplay(holder);

			} else
				Log.w(LOG_TAG, "Not Surface Create");

			// startRecording();
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception : " + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(LOG_TAG, "surfaceDestroyed");
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (data == null)
			return;
		// Send video to subscribers
		try {
			for (Joinable j : getJoinees(Direction.SEND))
				if (j instanceof VideoSink)
					((VideoSink) j).putVideoFrame(data);
			for (Joinable j : getJoinees(Direction.DUPLEX))
				if (j instanceof VideoSink)
					((VideoSink) j).putVideoFrame(data);
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
		if(videoProfile == null)
			throw new MsControlException("Cannot get video profile.");
		
		this.width = videoProfile.getWidth();
		this.height = videoProfile.getHeight();
		
		
		if (videoSurfaceTx != null) {
			mVideoView = (SurfaceView) videoSurfaceTx;
			mHolder = mVideoView.getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		startRecording();
	}

	@Override
	public void stop() {
		Log.d(LOG_TAG, "Stop");
		// mCamera.stopPreview();
		// mCamera.release();
		mCamera = null;
	}

}
