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
			long t, tStart, flushedT, currentFlushed, currentT, targetRelTime, targetTime, latency;
			long inc = 20;
			long tIncStart;
			long minTime, estStartT;

			try {
				tStart = System.currentTimeMillis();
				flushedT = tStart;
				tIncStart = tStart;
				targetRelTime = 0;
				Log.d(LOG_TAG, "Controller start with scheduler");
				for (;;) {
					Log.i(LOG_TAG, "start");
					t = System.currentTimeMillis();
					minTime = Long.MAX_VALUE;
					estStartT = Long.MAX_VALUE;
					for (Recorder r : recorders) {
						minTime = Math.min(minTime, r.getHeadTime());
						estStartT = Math.min(estStartT,
								r.getEstimatedStartTime());
					}
					if (minTime < 0) {
						Log.w(LOG_TAG, "can not record");
						tIncStart = t;
						sleep(inc);
						continue;
					}

					Log.d(LOG_TAG, "(t - tStart): " + (t - tStart)
							+ " (t - flushedT): " + (t - flushedT)
							+ " (t - tIncStart): " + (t - tIncStart));

					currentT = estStartT + (t - tStart);
					currentFlushed = estStartT + (t - flushedT);
					targetRelTime += t - tIncStart;
					targetTime = estStartT + targetRelTime;

					Log.d(LOG_TAG, "targetTime: " + targetTime
							+ " minTime + 2 * inc: " + (minTime + 2 * inc));

					targetTime = Math.min(targetTime, minTime + 2 * inc);
					latency = currentFlushed - targetTime;

					Log.d(LOG_TAG, "estStartT: " + estStartT + " currentT: "
							+ currentT + " currentFlushed: " + currentFlushed
							+ " targetTime: " + targetTime + " targetRelTime: "
							+ targetRelTime + " latency: " + latency);

					if (latency > 500) {
						long flushTo = targetTime + 500;
						Log.w(LOG_TAG, "flush to " + flushTo);
						for (Recorder r : recorders) {
							Log.w(LOG_TAG, r + " latency: " + r.getLatency());
							r.flushTo(flushTo);
						}
						targetRelTime = Math.min(currentT, flushTo) - estStartT;
						flushedT = System.currentTimeMillis() - targetRelTime;
						continue;
					}

					for (Recorder r : recorders)
						r.startRecord(targetTime);

					tIncStart = t;
					Log.i(LOG_TAG, "finish");
					sleep(inc);
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "Controller stopped");
			}
		}
	}

}
