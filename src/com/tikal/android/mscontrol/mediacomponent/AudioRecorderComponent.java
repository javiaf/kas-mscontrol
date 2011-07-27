package com.tikal.android.mscontrol.mediacomponent;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import com.tikal.android.media.profiles.AudioProfile;
import com.tikal.android.media.rx.AudioRx;
import com.tikal.android.mscontrol.join.AudioJoinableStreamImpl;
import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.Parameter;
import com.tikal.mscontrol.Parameters;
import com.tikal.mscontrol.join.Joinable;

public class AudioRecorderComponent extends MediaComponentBase implements AudioRx {

	/**
	 * Parameter whose value must be an int indicates the stream type in
	 * Android, for example AudioManager.STREAM_MUSIC.
	 */
	public static final Parameter STREAM_TYPE = new Parameter() {};

	private static final String LOG_TAG = "AudioReceive";

	private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private AudioTrack audioTrack;
	private int streamType;

	private boolean isRecording = false;

	public boolean isRecording() {
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
