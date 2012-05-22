package com.kurento.kas.mscontrol.mediacomponent.internal;

import com.kurento.kas.mscontrol.mediacomponent.MediaComponentAndroid;

public interface Recorder extends MediaComponentAndroid {

	public boolean isSynchronize();

	public void setSynchronize(boolean sync);

	/**
	 * 
	 * @return pts normalized to milliseconds or -1 if there is no media packet.
	 */
	public long getPtsMillis();

	public long getHeadTime();

	public long getEstimatedStartTime();

	public long getEstimatedFinishTime();

	/**
	 * 
	 * @return true if there are some media packets waiting to be recorder.
	 */
	public boolean hasMediaPacket();

	public void startRecord();

	public void startRecord(long time);

	public void stopRecord();

	public void flushTo(long time);

	public void flushAll();

	public long getLatency();

	public long getLastPtsNorm();

}
