package com.kurento.kas.mscontrol.join;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.join.Joinable;
import com.kurento.commons.mscontrol.join.JoinableContainer;
import com.kurento.kas.media.rx.AudioRx;
import com.kurento.kas.media.tx.AudioInfoTx;
import com.kurento.kas.media.tx.MediaTx;
import com.kurento.kas.mscontrol.mediacomponent.AudioSink;

public class AudioJoinableStreamImpl extends JoinableStreamBase implements
		AudioSink, AudioRx {

	private AudioInfoTx audioInfo;

	// TODO Use JoinEvent?
	public AudioInfoTx getAudioInfoTx() {
		return audioInfo;
	}

	public AudioJoinableStreamImpl(JoinableContainer container,
			StreamType type, AudioInfoTx audioInfo) {
		super(container, type);
		this.audioInfo = audioInfo;
	}

	@Override
	public void putAudioSamples(short[] in_buffer, int in_size) {
		MediaTx.putAudioSamples(in_buffer, in_size);
	}

	@Override
	public void putAudioSamplesRx(byte[] audio, int length) {
		try {
			for (Joinable j : getJoinees(Direction.SEND))
				if (j instanceof AudioRx)
					((AudioRx) j).putAudioSamplesRx(audio, length);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
