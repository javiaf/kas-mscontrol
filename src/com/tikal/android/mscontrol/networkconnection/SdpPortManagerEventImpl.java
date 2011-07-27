package com.tikal.android.mscontrol.networkconnection;

import java.util.EventObject;

import javax.sdp.SessionDescription;

import com.tikal.mscontrol.EventType;
import com.tikal.mscontrol.MediaErr;
import com.tikal.mscontrol.networkconnection.SdpPortManager;
import com.tikal.mscontrol.networkconnection.SdpPortManagerEvent;

public class SdpPortManagerEventImpl extends EventObject implements
		SdpPortManagerEvent {

	private static final long serialVersionUID = -2136152163038746198L;

	private EventType eventType;
	private MediaErr error;
	private SessionDescription sdp;

	public SdpPortManagerEventImpl(EventType eventType, SdpPortManager source,
			SessionDescription sdp, MediaErr error) {
		super(source);
		this.eventType = eventType;
		this.source = source;
		this.sdp = sdp;
		this.error = error;
	}
	
	@Override
	public EventType getEventType() {
		return eventType;
	}
	
	@Override
	public MediaErr getError() {
		return error;
	}

	public SessionDescription getSdp() {
		return sdp;
	}

	public SdpPortManager getSource() {
		return (SdpPortManager) source;
	}

//	@Override
//	public Qualifier getQualifier() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Trigger getRTCTrigger() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public String getErrorText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSuccessful() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getMediaServerSdp() {
		if (sdp == null)
			return null;
		return sdp.toString().getBytes();
	}

}
