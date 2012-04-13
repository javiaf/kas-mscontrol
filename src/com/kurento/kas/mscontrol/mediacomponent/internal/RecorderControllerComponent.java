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
			long tIncStart;
			boolean record;

			try {
				tStart = System.currentTimeMillis();
				tIncStart = tStart;
				targetPts = 0;
				Log.d(LOG_TAG, "Controller start with scheduler");
				for (;;) {
					t = System.currentTimeMillis();
					record = true;
					for (Recorder r : recorders) {
						if (!r.hasMediaPacket())
							record = false;
					}
					if (!record) {
						Log.w(LOG_TAG, "can not record");
						tIncStart = t;
						sleep(inc);
						continue;
					}

					currentT = t - tStart;
					targetPts += t - tIncStart;
					latency = currentT - targetPts;

					Log.d(LOG_TAG, "currentT: " + currentT + " targetPts: "
							+ targetPts + " latency: " + latency);

					for (Recorder r : recorders)
						r.startRecord(targetPts);

					tIncStart = t;
					sleep(inc);
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "Controller stopped");
			}
		}
	}

}
