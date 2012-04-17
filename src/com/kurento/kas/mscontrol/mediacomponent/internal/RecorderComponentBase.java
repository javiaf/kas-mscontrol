package com.kurento.kas.mscontrol.mediacomponent.internal;

import com.kurento.commons.mscontrol.MsControlException;

public abstract class RecorderComponentBase extends MediaComponentBase
		implements Recorder {

	private long estimatedStartTime;
	private long targetTime;
	private long lastPtsNorm;

	private long n = 0;

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
	public abstract long getPtsMillis();

	@Override
	public abstract boolean hasMediaPacket();

	@Override
	public abstract void startRecord();

	@Override
	public abstract void startRecord(long pts);

	@Override
	public abstract void stopRecord();

	@Override
	public abstract void flushAll();

	@Override
	public abstract long getLatency();

}
