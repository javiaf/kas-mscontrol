/*
 * Kurento Android MSControl: MSControl implementation for Android.
 * Copyright (C) 2011  Tikal Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kurento.kas.mscontrol.networkconnection.internal;

import java.util.EventObject;

import javax.sdp.SessionDescription;

import com.kurento.commons.mscontrol.EventType;
import com.kurento.commons.mscontrol.MediaErr;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManager;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManagerEvent;

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

	// @Override
	// public Qualifier getQualifier() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public Trigger getRTCTrigger() {
	// // TODO Auto-generated method stub
	// return null;
	// }

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
