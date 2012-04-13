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

public class AudioRecorderComponent extends MediaComponentBase implements
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
	private long targetPtsNorm;

	public synchronized boolean isRecording() {
		return isRecording;
	}

	public synchronized void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	public synchronized long getTargetPtsNorm() {
		return targetPtsNorm;
	}

	public synchronized void setTargetPtsNorm(long targetPtsNorm) {
		this.targetPtsNorm = targetPtsNorm;
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
		Log.d(LOG_TAG, "queue size: " + audioSamplesQueue.size());
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

		int buffer_min = AudioTrack.getMinBufferSize(frequency,
				channelConfiguration, audioEncoding);

		audioTrack = new AudioTrack(this.streamType, frequency,
				channelConfiguration, audioEncoding, buffer_min,
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

					long targetPtsNorm = getTargetPtsNorm();
					if (targetPtsNorm != -1) {
						long ptsNorm = calcPtsNorm(audioSamplesQueue.peek());
						Log.d(LOG_TAG, "ptsNorm: " + ptsNorm + " targetPts: "
								+ targetPtsNorm);
						if ((ptsNorm == -1) || (ptsNorm > targetPtsNorm)) {
							Log.d(LOG_TAG, "wait");
							synchronized (controll) {
								controll.wait();
							}
							continue;
						}
					}

					audioSamplesProcessed = audioSamplesQueue.take();
					Log.d(LOG_TAG, "play audio samples");
					if (audioTrack != null
							&& (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)) {
						Log.d(LOG_TAG, "ok");
						audioTrack.write(
								audioSamplesProcessed.getDataSamples(), 0,
								audioSamplesProcessed.getSize());
					}
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "AudioTrackControl stopped");
			}
		}
	}

	@Override
	public long getPtsNorm() {
		return calcPtsNorm(audioSamplesQueue.peek());
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
	public void startRecord(long pts) {
		setTargetPtsNorm(pts);
		setRecording(true);
		synchronized (controll) {
			controll.notify();
		}
	}

	@Override
	public void stopRecord() {
		setRecording(false);
	}

	private long calcPtsNorm(AudioSamples as) {
		if (as == null)
			return -1;

		return 1000 * ((as.getPts() - as.getStartTime()) * as.getTimeBaseNum())
				/ as.getTimeBaseDen();
	}

}
