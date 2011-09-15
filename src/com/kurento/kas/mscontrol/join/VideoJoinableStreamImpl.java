package com.kurento.kas.mscontrol.join;

import java.util.concurrent.LinkedBlockingDeque;

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

	private long t_suma = 0;
	private long n = 1;

	private long t_suma50 = 0;
	private long n50 = 1;

	private long t_rx = 0;
	private long t_tx = 0;
	private long t_tx_suma = 0;

	private long t_tx_total = 0;
	private long t_tx_total_suma = 0;

	private long t_tx_total_medio = 0;

	// private int nSent = 0;

	private long t_frame_received = 0;
	private long t_frame_received_max = 0;

	private long t_tx_suma_camera = 0;
	private long n_camera = 1;
	private long n50_camera = 1;

	private class Frame {
		private byte[] data;
		private int width;
		private int height;
		private long time;

		public Frame(byte[] data, int width, int height, long time) {
			this.data = data;
			this.width = width;
			this.height = height;
			this.time = time;
		}
	}

	// private ArrayBlockingQueue<Frame> framesQueue = new
	// ArrayBlockingQueue<Frame>(
	// 1);
	private static final int QUEUE_SIZE = 2;
	private LinkedBlockingDeque<Frame> framesQueue = new LinkedBlockingDeque<Frame>(
			QUEUE_SIZE);
	private LinkedBlockingDeque<Long> txTimes = new LinkedBlockingDeque<Long>(
			QUEUE_SIZE);

	public VideoProfile getVideoProfile() {
		return videoProfile;
	}

	public VideoJoinableStreamImpl(JoinableContainer container,
			StreamType type, VideoProfile videoProfile) {
		super(container, type);
		this.videoProfile = videoProfile;
		(new VideoTxThread()).start();
	}

	@Override
	public void putVideoFrame(byte[] data, int width, int height) {
		// long t = System.currentTimeMillis();
		// if (t_frame_received != 0 && n_camera > 50) {
		// long t_diff = t - t_frame_received;
		// if (t_diff > t_frame_received_max)
		// t_frame_received_max = t_diff;
		//
		// t_tx_suma_camera += t_diff;
		// long t_medio50 = t_tx_suma_camera / n50_camera;
		//
		// Log.e(LOG_TAG, "RECEIVE FRAME FROM CAMERA. T from last frame: "
		// + t_diff + "\t\tT max: " + t_frame_received_max
		// + "\t\tt_medio50: " + t_medio50);
		// n50_camera++;
		// }
		// n_camera++;
		// t_frame_received = t;

		// algA
		// framesQueue.clear();
		// framesQueue.offer(new Frame(data, width, height, System
		// .currentTimeMillis()));

		// algB
		if (framesQueue.size() >= QUEUE_SIZE)
			framesQueue.pollLast();
		framesQueue.offerFirst(new Frame(data, width, height, System
				.currentTimeMillis()));
	}

	private class VideoTxThread extends Thread {
		@Override
		public void run() {
			// algA();
			algB();
		}

		private void algA() {
			for (;;) {
				Frame frameProcessed;
				try {
					frameProcessed = framesQueue.take();
				} catch (InterruptedException e) {
					break;
				}

				Log.e(LOG_TAG, "\t\tPROCESS");

				long t = System.currentTimeMillis();
				long t_diff = t - t_tx;
				Log.d(LOG_TAG, "Diff TX frame times: " + t_diff
						+ "\t\tFrame rate: " + videoProfile.getFrameRate());

				if (n > 50) {
					t_tx_suma += t_diff;
					long t_medio50 = t_tx_suma / n;
					Log.d(LOG_TAG, "Diff TX frame times: " + t_diff
							+ "\t\tDiff TX frame times MEDIO: " + t_medio50
							+ "\t\tFrame rate: " + videoProfile.getFrameRate());

					if (videoProfile != null) {

						// if (t_tx_total_medio != 0
						// && t_tx_total_medio < 1.1 * 1000 / videoProfile
						// .getFrameRate()) {
						if (t_diff < 1000 / videoProfile.getFrameRate()) {
							long s = 1000 / videoProfile.getFrameRate()
									- t_diff;
							Log.e(LOG_TAG, "sleep: " + s);
							try {
								sleep(s);
								Log.e(LOG_TAG, "ok");
							} catch (InterruptedException e) {
								break;
							}
						}
					}
				}

				t_tx = t;

				long t_init = System.currentTimeMillis();

				MediaTx.putVideoFrame(frameProcessed.data,
						frameProcessed.width, frameProcessed.height);

				long t_fin = System.currentTimeMillis();
				long tiempo = t_fin - t_init;
				t_suma += tiempo;
				long t_medio = t_suma / n;
				if (n > 50) {
					t_suma50 += tiempo;
					long t_medio50 = t_suma50 / n50;
					Log.d(LOG_TAG, "n: " + n + "\t\tTiempo: " + tiempo
							+ "\t\tTiempo medio: " + t_medio
							+ "\t\tTiempo medio50: " + t_medio50);

					t_diff = t_fin - t_tx_total;
					t_tx_total_suma += t_diff;
					t_tx_total_medio = t_tx_total_suma / n50;
					Log.d(LOG_TAG, "t TX total: " + t_diff
							+ "\t\t t TX total medio: " + t_tx_total_medio);

					n50++;
				} else {
					Log.d(LOG_TAG, "n: " + n + "\t\tTiempo: " + tiempo
							+ "\t\tTiempo medio: " + t_medio);
				}

				t_tx_total = t_fin;

				n++;
				// nSent++;
			}
		}

		private void algBLog() {
			Log.d(LOG_TAG, "algBLog\t\tQUEUE_SIZE: " + QUEUE_SIZE);
			for (;;) {
				Log.e(LOG_TAG, "\t\tPROCESS");

				long t = System.currentTimeMillis();
				long t_diff = t - t_tx;
				Log.d(LOG_TAG, "Diff TX frame times: " + t_diff
						+ "\t\tFrame rate: " + videoProfile.getFrameRate());

				if (n > 50) {
					t_tx_suma += t_diff;
					long t_medio50 = t_tx_suma / n50;
					Log.d(LOG_TAG, "Diff TX frame times: " + t_diff
							+ "\t\tDiff TX frame times MEDIO: " + t_medio50
							+ "\t\tFrame rate: " + videoProfile.getFrameRate());

					if (videoProfile != null) {

						// if (t_tx_total_medio != 0
						// && t_tx_total_medio < 1.1 * 1000 / videoProfile
						// .getFrameRate()) {
						if (t_diff < 1000 / videoProfile.getFrameRate()) {
							long s = 1000 / videoProfile.getFrameRate()
									- t_diff;
							Log.e(LOG_TAG, "sleep: " + s);
							try {
								sleep(s);
								Log.e(LOG_TAG, "ok");
							} catch (InterruptedException e) {
								break;
							}
						}
					}
				}

				Frame frameProcessed;
				try {
					frameProcessed = framesQueue.takeLast();
				} catch (InterruptedException e) {
					break;
				}

				t_tx = t;

				long t_init = System.currentTimeMillis();

				MediaTx.putVideoFrame(frameProcessed.data,
						frameProcessed.width, frameProcessed.height);

				long t_fin = System.currentTimeMillis();
				long tiempo = t_fin - t_init;
				t_suma += tiempo;
				long t_medio = t_suma / n;
				if (n > 50) {
					t_suma50 += tiempo;
					long t_medio50 = t_suma50 / n50;
					Log.d(LOG_TAG, "n: " + n + "\t\tTiempo: " + tiempo
							+ "\t\tTiempo medio: " + t_medio
							+ "\t\tTiempo medio50: " + t_medio50);

					t_diff = t_fin - t_tx_total;
					t_tx_total_suma += t_diff;
					t_tx_total_medio = t_tx_total_suma / n50;
					Log.d(LOG_TAG, "t TX total: " + t_diff
							+ "\t\t t TX total medio: " + t_tx_total_medio);

					n50++;
				} else {
					Log.d(LOG_TAG, "n: " + n + "\t\tTiempo: " + tiempo
							+ "\t\tTiempo medio: " + t_medio);
				}

				t_tx_total = t_fin;

				n++;
				// nSent++;
			}
			Log.d(LOG_TAG, "FIN");
		}

		private void algB() {
			Log.d(LOG_TAG, "algB\t\tQUEUE_SIZE: " + QUEUE_SIZE);
			int tFrame = 1000 / videoProfile.getFrameRate();
			Frame frameProcessed;

			try {
				for (int i = 0; i < QUEUE_SIZE; i++)
					txTimes.offerFirst( new Long(0) );
				for (;;) {
					long t = System.currentTimeMillis();
					long h = (t - txTimes.takeLast()) / QUEUE_SIZE;
					if (h < tFrame) {
						long s = tFrame - h;
						sleep(s);
					}
					frameProcessed = framesQueue.takeLast();
					txTimes.offerFirst(t);
					MediaTx.putVideoFrame(frameProcessed.data,
							frameProcessed.width, frameProcessed.height);
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "FIN");
			}
		}
	}

	@Override
	public void putVideoFrameRx(int[] rgb, int width, int height) {

		long t = System.currentTimeMillis();
		long t_diff = t - t_rx;
		Log.d(LOG_TAG, "Diff RX frame times: " + t_diff);
		t_rx = t;

		try {
			for (Joinable j : getJoinees(Direction.SEND))
				if (j instanceof VideoRx)
					((VideoRx) j).putVideoFrameRx(rgb, width, height);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
