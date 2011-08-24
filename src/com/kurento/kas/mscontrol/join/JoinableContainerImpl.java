package com.kurento.kas.mscontrol.join;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.join.JoinableContainer;
import com.kurento.commons.mscontrol.join.JoinableStream;
import com.kurento.commons.mscontrol.join.JoinableStream.StreamType;

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
