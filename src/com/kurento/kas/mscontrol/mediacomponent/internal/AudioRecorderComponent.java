/*
 * Kurento Android MSControl: MSControl implementation for Android.
 * Copyright (C) 2011  Tikal Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kurento.kas.mscontrol.mediacomponent.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.Parameters;
import com.kurento.commons.mscontrol.join.Joinable;
import com.kurento.kas.media.profiles.AudioProfile;
import com.kurento.kas.media.rx.AudioRx;
import com.kurento.kas.media.rx.AudioSamples;
import com.kurento.kas.mscontrol.join.AudioJoinableStreamImpl;

public class AudioRecorderComponent extends RecorderComponentBase implements
		Recorder, AudioRx {

	private static final String LOG_TAG = "NDK-audio-rx"; // "AudioRecorder";

	private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private AudioTrack audioTrack;
	private int streamType;

	private AudioTrackControl audioTrackControl = null;

	private BlockingQueue<AudioSamples> audioSamplesQueue;

	private final Object controll = new Object();

	private boolean isRecording = false;

	public synchronized boolean isRecording() {
		return isRecording;
	}

	public synchronized void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	@Override
	public synchronized boolean isStarted() {
		return audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
	}

	public AudioRecorderComponent(Parameters params) throws MsControlException {
		if (params == null)
			throw new MsControlException("Parameters are NULL");

		Integer streamType = (Integer) params.get(STREAM_TYPE);
		if (streamType == null)
			throw new MsControlException(
					"Params must have AudioRecorderComponent.STREAM_TYPE param.");
		this.streamType = streamType;
		this.audioSamplesQueue = new LinkedBlockingQueue<AudioSamples>();
	}

	@Override
	public synchronized void putAudioSamplesRx(AudioSamples audioSamples) {
		Log.i(LOG_TAG, "Enqueue audio samples (ptsNorm/rxTime)"
				+ calcPtsMillis(audioSamples) + "/" + audioSamples.getRxTime()
				+ " queue size: " + audioSamplesQueue.size());
		long ptsNorm = calcPtsMillis(audioSamples);
		setLastPtsNorm(ptsNorm);
		long estStartTime = caclEstimatedStartTime(ptsNorm,
				audioSamples.getRxTime());
		Log.i(LOG_TAG, "estimated start time: " + estStartTime);
		audioSamplesQueue.offer(audioSamples);
	}

	@Override
	public synchronized void start() throws MsControlException {
		AudioProfile audioProfile = null;
		for (Joinable j : getJoinees(Direction.RECV))
			if (j instanceof AudioJoinableStreamImpl) {
				audioProfile = ((AudioJoinableStreamImpl) j).getAudioInfoTx()
						.getAudioProfile();
			}
		if (audioProfile == null)
			throw new MsControlException("Cannot ger audio profile.");

		int frequency = audioProfile.getSampleRate();

		int minBufferSize = AudioTrack.getMinBufferSize(frequency,
				channelConfiguration, audioEncoding);

		Log.d(LOG_TAG, "minBufferSize: " + minBufferSize);
		audioTrack = new AudioTrack(this.streamType, frequency,
				channelConfiguration, audioEncoding, minBufferSize,
				AudioTrack.MODE_STREAM);

		if (audioTrack != null) {
			audioTrack.play();
		}
		audioTrackControl = new AudioTrackControl();
		audioTrackControl.start();

		RecorderControllerComponent.getInstance().addRecorder(this);
		Log.d(LOG_TAG, "add to controller");
	}

	@Override
	public synchronized void stop() {
		RecorderControllerComponent.getInstance().deleteRecorder(this);

		if (audioTrackControl != null)
			audioTrackControl.interrupt();
		if (audioTrack != null) {
			audioTrack.stop();
			audioTrack.release();
			audioTrack = null;
		}
	}

	private class AudioTrackControl extends Thread {
		@Override
		public void run() {
			try {
				AudioSamples audioSamplesProcessed;
				for (;;) {
					if (!isRecording()) {
						synchronized (controll) {
							controll.wait();
						}
						continue;
					}

					if (audioSamplesQueue.isEmpty())
						Log.w(LOG_TAG, "jitter_buffer_underflow: Audio frames queue is empty");

					// Log.d(LOG_TAG, "Process audio samples...");
					long targetTime = getTargetTime();
					if (targetTime != -1) {
						long ptsMillis = calcPtsMillis(audioSamplesQueue.peek());
						if (audioTrack != null
								&& (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)) {
							Log.d(LOG_TAG,
									"currentPts: "
										+ ptsMillis
										+ " targetTime: "
										+ targetTime
										+ " playback head position: "
										+ (1000 * audioTrack
												.getPlaybackHeadPosition() / audioTrack
												.getPlaybackRate()));
						}
						if ((ptsMillis == -1)
								|| (ptsMillis + getEstimatedStartTime() > targetTime)) {
							// Log.d(LOG_TAG, "wait");
							synchronized (controll) {
								controll.wait();
							}
							continue;
						}
					}

					audioSamplesProcessed = audioSamplesQueue.take();
					Log.d(LOG_TAG, "play audio samples "
							+ calcPtsMillis(audioSamplesProcessed));
					if (audioTrack != null
							&& (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)) {
						// Log.d(LOG_TAG,
						// "write... getNotificationMarkerPosition(): "
						// + audioTrack
						// .getNotificationMarkerPosition()
						// + " getPlaybackHeadPosition(): "
						// + audioTrack.getPlaybackHeadPosition()
						// + " getPlaybackRate(): "
						// + audioTrack.getPlaybackRate()
						// + " 	getPositionNotificationPeriod(): "
						// + audioTrack
						// .getPositionNotificationPeriod());
						audioTrack.write(
								audioSamplesProcessed.getDataSamples(), 0,
								audioSamplesProcessed.getSize());
						// Log.d(LOG_TAG, "write OK");
					}
					// Log.d(LOG_TAG, "play OK");
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "AudioTrackControl stopped");
			}
		}
	}

	@Override
	public long getPtsMillis() {
		return calcPtsMillis(audioSamplesQueue.peek());
	}

	@Override
	public long getHeadTime() {
		long ptsMillis = calcPtsMillis(audioSamplesQueue.peek());
		if (ptsMillis < 0)
			return -1;
		return ptsMillis + getEstimatedStartTime();
	}

	@Override
	public boolean hasMediaPacket() {
		return !audioSamplesQueue.isEmpty();
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

	private long calcPtsMillis(AudioSamples as) {
		if (as == null)
			return -1;

		return 1000 * ((as.getPts() - as.getStartTime()) * as.getTimeBaseNum())
				/ as.getTimeBaseDen();
	}

	@Override
	public void flushTo(long time) {
		AudioSamples as = audioSamplesQueue.peek();
		while (as != null) {
			if ((calcPtsMillis(as) + getEstimatedStartTime()) > time)
				break;
			audioSamplesQueue.remove(as);
			as = audioSamplesQueue.peek();
		}
	}

	@Override
	public void flushAll() {
		audioSamplesQueue.clear();
	}

	@Override
	public long getLatency() {
		long firstPtsNorm = calcPtsMillis(audioSamplesQueue.peek());
		if (firstPtsNorm < 0)
			return -1;
		return getLastPtsNorm() - firstPtsNorm;
	}

}
