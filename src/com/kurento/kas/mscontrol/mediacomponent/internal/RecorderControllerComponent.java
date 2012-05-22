package com.kurento.kas.mscontrol.mediacomponent.internal;

import java.util.concurrent.CopyOnWriteArraySet;

import android.util.Log;

public class RecorderControllerComponent implements
		RecorderController {

	private static final String LOG_TAG = "RecorderController";

	private CopyOnWriteArraySet<Recorder> recorders = new CopyOnWriteArraySet<Recorder>();
	private Controller controller;

	private int maxDelay;

	public int getMaxDelay() {
		return maxDelay;
	}

	public void setMaxDelay(int maxDelay) {
		this.maxDelay = maxDelay;
	}
	public RecorderControllerComponent(int maxDelay) {
		this.maxDelay = maxDelay;
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

	public static final int INTERVAL = 40;
	public static final int MAX_WAIT = 200;

	private class Controller extends Thread {
		@Override
		public void run() {
			try {
				long inc = 20;
				long t, lastT;
				long globalHeadTime, globalFinishTime, globalStartT;

				lastT = System.currentTimeMillis();
				long relativeTargetTime = 0;
				long absoluteTargetTime = lastT;
				long latency;
				long nToRecord;

				for (;;) {
					t = System.currentTimeMillis();

					globalHeadTime = Long.MAX_VALUE;
					globalFinishTime = Long.MAX_VALUE;
					globalStartT = Long.MAX_VALUE;
					nToRecord = 0;
					for (Recorder r : recorders) {
						long finishTime = r.getEstimatedFinishTime();
//						Log.d(LOG_TAG, r + " lastPtsNorm: " + r.getLastPtsNorm());
						if (r.hasMediaPacket()) {
							globalHeadTime = Math.min(globalHeadTime, r.getHeadTime());
							globalFinishTime = Math.min(globalFinishTime, finishTime);
							globalStartT = Math.min(globalStartT, r.getEstimatedStartTime());
							r.setSynchronize(true);
							nToRecord++;
						} else if ((absoluteTargetTime - finishTime) > MAX_WAIT) {
							r.setSynchronize(false);
						}
					}

					if (nToRecord == 0) {
						lastT = t;
						sleep(inc);
						continue;
					}

					relativeTargetTime += t - lastT;
					absoluteTargetTime = globalStartT + relativeTargetTime;

					Log.d(LOG_TAG, "absoluteTargetTime: " + absoluteTargetTime
							+ " globalHeadTime: " + globalHeadTime
							+ " relativeTargetTime before: " + relativeTargetTime);

					absoluteTargetTime = Math.min(absoluteTargetTime, globalHeadTime);
					latency = globalFinishTime - absoluteTargetTime;
//					relativeTargetTime = absoluteTargetTime - globalStartT;

					Log.d(LOG_TAG, "estStartT: " + globalStartT
							+ " targetTime: " + absoluteTargetTime
							+ " latency: " + latency
							+ " relativeTargetTime after: " + relativeTargetTime);

					if (latency > maxDelay) {
						long flushTo = globalFinishTime - 1;
						Log.w(LOG_TAG, "flush to " + flushTo);
						for (Recorder r : recorders) {
							Log.w(LOG_TAG, r + " latency: " + r.getLatency());
							r.flushTo(flushTo);
						}
						relativeTargetTime = flushTo - globalStartT;

						lastT = t;
						sleep(inc);
						continue;
					}

					for (Recorder r : recorders) {
						if (r.isSynchronize())
							r.startRecord(absoluteTargetTime + INTERVAL);
					}

					lastT = t;
					sleep(inc);
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "Controller stopped");
			}
		}
	}

}
