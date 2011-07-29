package com.tikal.android.mscontrol.join;

import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.join.JoinableContainer;
import com.tikal.mscontrol.join.JoinableStream;
import com.tikal.mscontrol.join.JoinableStream.StreamType;

public class JoinableContainerImpl extends JoinableImpl implements
		JoinableContainer {

	protected JoinableStreamBase[] streams = new JoinableStreamBase[2];

	@Override
	public JoinableStream getJoinableStream(StreamType value)
			throws MsControlException {
		for (JoinableStreamBase s : streams) {
			if (s.getType().equals(value)) {
				return s;
			}
		}
		throw new MsControlException("Stream of type " + value
				+ " is not supported");
	}

	@Override
	public JoinableStream[] getJoinableStreams() throws MsControlException {
		return streams;
	}

}
