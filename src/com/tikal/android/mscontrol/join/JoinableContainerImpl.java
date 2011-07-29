package com.tikal.android.mscontrol.join;

import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.join.JoinableContainer;
import com.tikal.mscontrol.join.JoinableStream;
import com.tikal.mscontrol.join.JoinableStream.StreamType;

public class JoinableContainerImpl extends JoinableImpl implements
		JoinableContainer {

	protected JoinableStream[] streams;

	@Override
	public JoinableStream getJoinableStream(StreamType value)
			throws MsControlException {
		if (streams == null)
			return null;
		
		for (JoinableStream s : streams) {
			if (s != null && s.getType().equals(value)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public JoinableStream[] getJoinableStreams() throws MsControlException {
		return streams;
	}

}
