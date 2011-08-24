package com.kurento.kas.mscontrol.mediacomponent;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.kas.mscontrol.join.JoinableContainerImpl;

public abstract class MediaComponentBase extends JoinableContainerImpl
		implements MediaComponentAndroid {

	@Override
	public void confirm() throws MsControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	@Override
	public abstract void start() throws MsControlException;

	@Override
	public abstract void stop();

}
