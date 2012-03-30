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

public class AudioRecorderComponent extends MediaComponentBase implements AudioRx {

	private static final String LOG_TAG = "NDK-audio-rx"; // "AudioRecorder";

	private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private AudioTrack audioTrack;
	private int streamType;

	private AudioTrackControl audioTrackControl = null;

	private class AudioFrame {
		private AudioSamples audioSamples;
		private long timeArrive;

		public AudioFrame(AudioSamples audioSamples, long timeArrive) {
			this.audioSamples = audioSamples;
			this.timeArrive = timeArrive;
		}
	}

	// private int QUEUE_SIZE = 5;
	private BlockingQueue<AudioFrame> audioFramesQueue;

	private static final long T_MIN = 20;
	private static final long T_MAX = 2000;

	private long initTime;

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
		this.audioFramesQueue = new LinkedBlockingQueue<AudioFrame>();
		this.initTime = System.currentTimeMillis();
	}

	@Override
	public synchronized void putAudioSamplesRx(AudioSamples audioSamples) {
		Log.d(LOG_TAG, "queue size: " + audioFramesQueue.size());
		long timeArrived = System.currentTimeMillis() - this.initTime;
		long sum = 0;
		for (AudioFrame af : audioFramesQueue) {
			sum += Math.max(0, (timeArrived - af.timeArrive) - T_MIN);
		}

		if (sum > T_MAX) {
			Log.w(LOG_TAG, "Clear audio jitter buffer.");
			audioFramesQueue.clear();
		}
		audioFramesQueue.offer(new AudioFrame(audioSamples, timeArrived));
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
	}

	@Override
	public synchronized void stop() {
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
				AudioFrame audioFrameProcessed;
				for (;;) {
					if (audioFramesQueue.isEmpty())
						Log.w(LOG_TAG, "jitter_buffer_underflow: Audio frames queue is empty");

					audioFrameProcessed = audioFramesQueue.take();
					Log.d(LOG_TAG, "play frame");
					if (audioTrack != null
							&& (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)) {
						audioTrack.write(audioFrameProcessed.audioSamples
								.getDataSamples(), 0,
								audioFrameProcessed.audioSamples.getSize());
					}
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "AudioTrackControl stopped");
			}
		}
	}

}
