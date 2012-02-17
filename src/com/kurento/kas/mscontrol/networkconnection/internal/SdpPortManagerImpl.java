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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sdp.SdpException;
import javax.sdp.SessionDescription;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kurento.commons.media.format.MediaSpec;
import com.kurento.commons.media.format.SessionSpec;
import com.kurento.commons.media.format.SpecTools;
import com.kurento.commons.mscontrol.MediaEventListener;
import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.networkconnection.NetworkConnection;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManager;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManagerEvent;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManagerException;
import com.kurento.commons.sdp.enums.Mode;

public class SdpPortManagerImpl implements SdpPortManager {

	private static Log log = LogFactory.getLog(SdpPortManagerImpl.class);

	private NetworkConnectionBase resource;
	private SessionSpec userAgentSDP; // this is remote session spec

	@SuppressWarnings("unchecked")
	private CopyOnWriteArrayList<MediaEventListener> mediaListenerList = new CopyOnWriteArrayList<MediaEventListener>();

	private SessionSpec localSpec;

	protected SdpPortManagerImpl(NetworkConnectionBase resource) {
		this.resource = resource;
	}

	@Override
	public NetworkConnection getContainer() {
		return this.resource;
	}

	@Override
	public void addListener(MediaEventListener<SdpPortManagerEvent> arg0) {
		mediaListenerList.add(arg0);
	}

	@Override
	public void removeListener(MediaEventListener<SdpPortManagerEvent> arg0) {
		mediaListenerList.remove(arg0);
	}

	/**
	 * <p>
	 * Request a SDP offer from the Media Server. When complete, sends a
	 * SdpPortManagerEvent with an EventType of
	 * SdpPortManagerEvent.OFFER_GENERATED. The resulting offer is available
	 * with SdpPortManagerEvent.getMediaServerSdp() This can be used to initiate
	 * a connection, or to increase/augment the capabilities of an established
	 * connection, like for example adding a video stream to an audio-only
	 * connection.
	 * <p>
	 */
	@Override
	public void generateSdpOffer() throws SdpPortManagerException {
		// Use localSpec as temp to use later in processSdpAnswer
		// This is important to not release media ports calling
		// resource.generateSessionSpec() newly in processSdpAnswer
		SdpPortManagerEventImpl event = null;

		try {
			localSpec = resource.generateSessionSpec();
			event = new SdpPortManagerEventImpl(
					SdpPortManagerEvent.OFFER_GENERATED, this,
					localSpec.getSessionDescription(),
					SdpPortManagerEvent.NO_ERROR);
		} catch (MsControlException e) {
			log.error(e.getMessage(), e);
			event = new SdpPortManagerEventImpl(null, this, null,
					SdpPortManagerEvent.RESOURCE_UNAVAILABLE);
		} catch (SdpException e) {
			event = new SdpPortManagerEventImpl(null, this, null,
					SdpPortManagerEvent.RESOURCE_UNAVAILABLE);
			log.error(
					"Error creating Session Description from resource media list",
					e);
			throw new SdpPortManagerException(
					"Error creating Session Description from resource media list",
					e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			notifyEvent(event);
		}
	}

	/**
	 * Request the MediaServer to process the given SDP offer (from the remote
	 * User Agent). When complete, sends a SdpPortManagerEvent with an EventType
	 * of SdpPortManagerEvent.ANSWER_GENERATED. The resulting answer is
	 * available with SdpPortManagerEvent.getMediaServerSdp()
	 */
	@Override
	public void processSdpOffer(byte[] offer) throws SdpPortManagerException {
		log.info("processSdpOffer");
		SdpPortManagerEventImpl event = null;

		try {
			userAgentSDP = new SessionSpec(new String(offer));
			SessionSpec[] intersectionSessions = SpecTools
					.intersectSessionSpec(resource.generateSessionSpec(),
							userAgentSDP);
			List<MediaSpec> combinedMediaList = intersectionSessions[1]
					.getMediaSpec();

			userAgentSDP.setMediaSpec(combinedMediaList);
			resource.setRemoteSessionSpec(userAgentSDP);

			localSpec = intersectionSessions[0];

			resource.setLocalSessionSpec(localSpec);

			boolean allInactive = true;
			for (MediaSpec ms : combinedMediaList) {
				if (!Mode.INACTIVE.equals(ms.getMode())) {
					allInactive = false;
					break;
				}
			}

			// if combinedMediaList.isEmpty() then allInactive==true
			if (allInactive) {
				event = new SdpPortManagerEventImpl(null, this,
						localSpec.getSessionDescription(),
						SdpPortManagerEvent.SDP_NOT_ACCEPTABLE);
			} else {
				event = new SdpPortManagerEventImpl(
						SdpPortManagerEvent.ANSWER_GENERATED, this,
						localSpec.getSessionDescription(),
						SdpPortManagerEvent.NO_ERROR);
			}
		} catch (MsControlException e) {
			log.error(e.getMessage(), e);
			event = new SdpPortManagerEventImpl(null, this, null,
					SdpPortManagerEvent.RESOURCE_UNAVAILABLE);
		} catch (SdpException e) {
			event = new SdpPortManagerEventImpl(null, this, null,
					SdpPortManagerEvent.SDP_NOT_ACCEPTABLE);
			log.error("Error processing SDPOffer", e);
			throw new SdpPortManagerException("Error processing SDPOffer", e);
		} finally {
			notifyEvent(event);
		}
	}

	@Override
	public void processSdpAnswer(byte[] answer) throws SdpPortManagerException {
		try {
			userAgentSDP = new SessionSpec(new String(answer));
			resource.setRemoteSessionSpec(SpecTools.intersectSessionSpec(
					localSpec, userAgentSDP)[1]);
			localSpec = SpecTools.intersectSessionSpec(localSpec, userAgentSDP)[0];
			resource.setLocalSessionSpec(localSpec);

			notifyEvent(new SdpPortManagerEventImpl(
					SdpPortManagerEvent.ANSWER_PROCESSED, this, null,
					SdpPortManagerEvent.NO_ERROR));
		} catch (SdpException e) {
			notifyEvent(new SdpPortManagerEventImpl(null, this, null,
					SdpPortManagerEvent.SDP_NOT_ACCEPTABLE));
			throw new SdpPortManagerException("Error getting media info", e);
		}
	}

	@Override
	public void rejectSdpOffer() throws SdpPortManagerException {
		resource.release();
	}

	/**
	 * <P>
	 * This method returns the media previously agreed after a complete
	 * offer-answer exchange. If no media has been agreed yet, it returns null.
	 * If an offer is in progress from either side, that offer's session
	 * description is not returned here.
	 * <P>
	 */
	@Override
	public byte[] getMediaServerSessionDescription()
			throws SdpPortManagerException {
		SessionDescription sdp = null;
		try {
			if (localSpec != null) {
				sdp = localSpec.getSessionDescription();
			}
		} catch (SdpException e) {
			log.error("Error creating session description.", e);
			throw new SdpPortManagerException(
					"Error creating Session Description", e);
		}
		return sdp.toString().getBytes();
	}

	@Override
	public byte[] getUserAgentSessionDescription()
			throws SdpPortManagerException {
		SessionDescription sdp = null;
		try {
			if (userAgentSDP != null) {
				sdp = userAgentSDP.getSessionDescription();
			}
		} catch (SdpException e) {
			throw new SdpPortManagerException(
					"Error creating SessionDescription", e);
		}
		return sdp.toString().getBytes();
	}

	@SuppressWarnings("unchecked")
	private void notifyEvent(SdpPortManagerEventImpl event) {
		for (MediaEventListener listener : mediaListenerList) {
			listener.onEvent(event);
		}
	}

}
