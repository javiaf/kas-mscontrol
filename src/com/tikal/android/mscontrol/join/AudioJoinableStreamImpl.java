package com.tikal.android.mscontrol.join;

import com.tikal.android.media.rx.AudioRx;
import com.tikal.android.media.tx.AudioInfoTx;
import com.tikal.android.media.tx.MediaTx;
import com.tikal.android.mscontrol.mediacomponent.AudioSink;
import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.join.Joinable;
import com.tikal.mscontrol.join.JoinableContainer;

public class AudioJoinableStreamImpl extends JoinableStreamBase implements
		AudioSink, AudioRx {

	private AudioInfoTx audioInfo;

	//TODO Use JoinEvent?
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
			for (Joinable j : getJoinees(Direction.DUPLEX))
				if (j instanceof AudioRx)
					((AudioRx) j).putAudioSamplesRx(audio, length);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
