package com.kurento.kas.mscontrol.networkconnection.internal;

import java.net.InetAddress;

import com.kurento.commons.media.format.SessionSpec;
import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.networkconnection.NetworkConnection;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManager;
import com.kurento.kas.mscontrol.join.JoinableContainerImpl;

public abstract class NetworkConnectionBase extends JoinableContainerImpl
		implements NetworkConnection {

	protected SdpPortManager sdpPortManager;

	protected NetworkConnectionBase() {
		sdpPortManager = new SdpPortManagerImpl(this);
	}

	@Override
	public SdpPortManager getSdpPortManager() throws MsControlException {
		return sdpPortManager;
	}

	/**
	 * Gets a session template copy with all medias capacities and ports
	 * assigned.
	 * 
	 * @return
	 */
	public abstract SessionSpec generateSessionSpec();

	/**
	 * Indicates own medias and ports assigned. Warning: It could has
	 * inconsistencies with template.
	 * 
	 * @param localSpec
	 */
	public abstract void setLocalSessionSpec(SessionSpec localSpec);

	/**
	 * Indicates other party medias and ports assigned.
	 * 
	 * @return
	 */
	public abstract void setRemoteSessionSpec(SessionSpec remote);

	public abstract InetAddress getLocalAddress();

}
