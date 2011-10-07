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

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.Parameters;
import com.kurento.commons.mscontrol.join.Joinable;
import com.kurento.kas.media.profiles.AudioProfile;
import com.kurento.kas.media.rx.AudioRx;
import com.kurento.kas.mscontrol.join.AudioJoinableStreamImpl;

public class AudioRecorderComponent extends MediaComponentBase implements AudioRx {
	
	private static final String LOG_TAG = "AudioReceive";

	private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private AudioTrack audioTrack;
	private int streamType;

	private boolean isRecording = false;

	@Override
	public boolean isStarted() {
		return isRecording;
	}

	public AudioRecorderComponent(Parameters params) throws MsControlException {
		if (params == null)
			throw new MsControlException("Parameters are NULL");

		Integer streamType = (Integer) params.get(STREAM_TYPE);
		if (streamType == null)
			throw new MsControlException(
					"Params must have AudioRecorderComponent.STREAM_TYPE param.");
		this.streamType = streamType;
	}

	// public void release() {
	// Log.d(LOG_TAG, "Release");
	// // MediaRx.stopAudioRx();
	// if (audioTrack != null)
	// audioTrack.release();
	// }

	@Override
	// public synchronized void putAudioSamplesRx(byte[] audio, int length) {
	public void putAudioSamplesRx(byte[] audio, int length) {
		if (isRecording && audioTrack != null)
			audioTrack.write(audio, 0, length);
	}

	@Override
	public void start() throws MsControlException {
		// TODO Create audioTrack in putAudioSamplesRx and receive sampleRate in
		// it??

		AudioProfile audioProfile = null;
		for (Joinable j : getJoinees(Direction.RECV))
			if (j instanceof AudioJoinableStreamImpl) {
				audioProfile = ((AudioJoinableStreamImpl) j).getAudioInfoTx().getAudioProfile();
			}
		if (audioProfile == null)
			throw new MsControlException("Cannot ger audio profile.");

		int frequency = audioProfile.getSampleRate();
		Log.d(LOG_TAG, "Frequency = " + frequency);

		int buffer_min = AudioTrack
				.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

		audioTrack = new AudioTrack(this.streamType, frequency, channelConfiguration,
				audioEncoding, buffer_min, AudioTrack.MODE_STREAM);

		if (audioTrack != null) {
			audioTrack.play();
			isRecording = true;
		}
	}

	@Override
	public synchronized void stop() {
		if (audioTrack != null) {
			audioTrack.stop();
			audioTrack = null;
			isRecording = false;
		}
	}

}
