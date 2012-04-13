package com.kurento.kas.mscontrol.mediacomponent.internal;

import java.util.concurrent.CopyOnWriteArraySet;

import android.util.Log;

public class RecorderControllerComponent implements
		RecorderController {

	private static final String LOG_TAG = "RecorderController";

	private CopyOnWriteArraySet<Recorder> recorders = new CopyOnWriteArraySet<Recorder>();
	private Controller controller;
	
	private static RecorderControllerComponent instance;

	private synchronized static void createInstance() {
		if (instance == null) {
			instance = new RecorderControllerComponent();
		}
	}

	public static RecorderControllerComponent getInstance() {
		if (instance == null)
			createInstance();
		return instance;
	}

	@Override
	public synchronized void addRecorder(Recorder r) {
		recorders.add(r);
		if (controller == null) {
			controller = new Controller();
			controller.start();
		}
	}

	@Override
	public synchronized void deleteRecorder(Recorder r) {
		recorders.remove(r);
		if ((controller != null) && recorders.isEmpty()) {
			controller.interrupt();
			controller = null;
		}
	}

	private class Controller extends Thread {
		@Override
		public void run() {
			long t, tStart, currentT, targetPts, latency;
			long inc = 20;
			boolean started = false;

			try {
				tStart = 0;
				Log.d(LOG_TAG, "Controller start");
				for (;;) {
					targetPts = Long.MAX_VALUE;
					for (Recorder r : recorders) {
						targetPts = Math.min(targetPts, r.getPtsNorm());
						if (targetPts < 0)
							break;
					}
					t = System.currentTimeMillis();
					currentT = t - tStart;
					latency = currentT - targetPts;

					if ((targetPts < 0) || (latency < 0)) {
						sleep(inc);
						continue;
					}
					if (!started) {
						tStart = System.currentTimeMillis();
						started = true;
					}

					Log.d(LOG_TAG, "currentT: " + currentT + " targetPts: "
							+ targetPts + " latency: " + latency);

					for (Recorder r : recorders)
						r.startRecord(targetPts);

					sleep(inc);
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "Controller stopped");
			}
		}
	}

}
