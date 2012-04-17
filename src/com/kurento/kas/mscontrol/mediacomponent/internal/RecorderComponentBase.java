package com.kurento.kas.mscontrol.mediacomponent.internal;

import java.util.concurrent.BlockingQueue;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.kas.media.rx.RxPacket;

public abstract class RecorderComponentBase extends MediaComponentBase
		implements Recorder {

	private long estimatedStartTime;
	private long targetTime;
	private long lastPtsNorm;

	protected BlockingQueue<RxPacket> packetsQueue;

	private long n = 0;

	private boolean isRecording = false;
	protected final Object controll = new Object();

	protected synchronized boolean isRecording() {
		return isRecording;
	}

	protected synchronized void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	@Override
	public synchronized long getEstimatedStartTime() {
		return estimatedStartTime;
	}

	public synchronized void setEstimatedStartTime(long estimatedStartTime) {
		this.estimatedStartTime = estimatedStartTime;
	}

	public synchronized long getTargetTime() {
		return targetTime;
	}

	public synchronized void setTargetTime(long targetPtsNorm) {
		this.targetTime = targetPtsNorm;
	}

	@Override
	public synchronized long getLastPtsNorm() {
		return lastPtsNorm;
	}

	public synchronized void setLastPtsNorm(long lastAudioSamplesPtsNorm) {
		this.lastPtsNorm = lastAudioSamplesPtsNorm;
	}

	public synchronized long caclEstimatedStartTime(long ptsNorm, long rxTime) {
		if (n > 15)
			estimatedStartTime = (15 * estimatedStartTime + (rxTime - ptsNorm)) / 16;
		else
			estimatedStartTime = (n * estimatedStartTime + (rxTime - ptsNorm))
					/ (n + 1);
		n++;
		return estimatedStartTime;
	}

	@Override
	public abstract boolean isStarted();

	@Override
	public abstract void start() throws MsControlException;

	@Override
	public abstract void stop();

	@Override
	public long getPtsMillis() {
		return calcPtsMillis(packetsQueue.peek());
	}

	@Override
	public long getHeadTime() {
		long ptsMillis = calcPtsMillis(packetsQueue.peek());
		if (ptsMillis < 0)
			return -1;
		return ptsMillis + getEstimatedStartTime();
	}

	@Override
	public boolean hasMediaPacket() {
		return !packetsQueue.isEmpty();
	}

	@Override
	public void startRecord() {
		startRecord(-1);
	}

	@Override
	public void startRecord(long time) {
		setTargetTime(time);
		setRecording(true);
		synchronized (controll) {
			controll.notify();
		}
	}

	@Override
	public void stopRecord() {
		setRecording(false);
	}

	@Override
	public void flushTo(long time) {
		RxPacket p = packetsQueue.peek();
		while (p != null) {
			if ((calcPtsMillis(p) + getEstimatedStartTime()) > time)
				break;
			packetsQueue.remove(p);
			p = packetsQueue.peek();
		}
	}

	@Override
	public void flushAll() {
		packetsQueue.clear();
	}

	@Override
	public long getLatency() {
		long firstPtsNorm = calcPtsMillis(packetsQueue.peek());
		if (firstPtsNorm < 0)
			return -1;
		return getLastPtsNorm() - firstPtsNorm;
	}

	protected long calcPtsMillis(RxPacket p) {
		if (p == null)
			return -1;

		return 1000 * ((p.getPts() - p.getStartTime()) * p.getTimeBaseNum())
				/ p.getTimeBaseDen();
	}

}
