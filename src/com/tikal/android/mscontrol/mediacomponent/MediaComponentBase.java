package com.tikal.android.mscontrol.mediacomponent;

import com.tikal.android.mscontrol.join.JoinableContainerImpl;
import com.tikal.mscontrol.MsControlException;

public abstract class MediaComponentBase extends JoinableContainerImpl implements MediaComponentAndroid {

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
	public abstract  void stop();

}
