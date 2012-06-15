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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kurento.commons.media.format.MediaSpec;
import com.kurento.commons.media.format.SessionSpec;
import com.kurento.commons.mscontrol.MediaEventListener;
import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.networkconnection.NetworkConnection;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManager;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManagerEvent;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManagerException;

public class SdpPortManagerImpl implements SdpPortManager {

	private static Log log = LogFactory.getLog(SdpPortManagerImpl.class);

	private NetworkConnectionBase resource;
	private SessionSpec userAgentSDP; // this is remote session spec

	private Set<MediaEventListener<SdpPortManagerEvent>> mediaListenerList =
				new CopyOnWriteArraySet<MediaEventListener<SdpPortManagerEvent>>();

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
					SdpPortManagerEvent.OFFER_GENERATED, this, localSpec,
					SdpPortManagerEvent.NO_ERROR);
		} catch (MsControlException e) {
			log.error(e.getMessage(), e);
			event = new SdpPortManagerEventImpl(null, this, null,
					SdpPortManagerEvent.RESOURCE_UNAVAILABLE);
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
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
	public void processSdpOffer(SessionSpec offer)
			throws SdpPortManagerException {
		log.info("processSdpOffer");
		SdpPortManagerEventImpl event = null;

		try {
			if (offer == null) {
				event = new SdpPortManagerEventImpl(null, this, localSpec,
						SdpPortManagerEvent.SDP_NOT_ACCEPTABLE);
			} else {

				userAgentSDP = offer;
				SessionSpec[] intersectionSessions = SessionSpec.intersect(
						resource.generateSessionSpec(), offer);
				List<MediaSpec> combinedMediaList = intersectionSessions[1]
						.getMediaSpecs();

				userAgentSDP.deleteAllMediaSpecs();
				userAgentSDP.addMediaSpecs(combinedMediaList);
				resource.setRemoteSessionSpec(userAgentSDP);
				localSpec = intersectionSessions[0];
				resource.setLocalSessionSpec(localSpec);

				boolean sdpNotAcceptable = true;
				for (MediaSpec ms : combinedMediaList) {
					if (!ms.getPayloads().isEmpty()) {
						sdpNotAcceptable = false;
						break;
					}
				}

				if (sdpNotAcceptable) {
					event = new SdpPortManagerEventImpl(null, this, localSpec,
							SdpPortManagerEvent.SDP_NOT_ACCEPTABLE);
				} else {
					event = new SdpPortManagerEventImpl(
							SdpPortManagerEvent.ANSWER_GENERATED, this,
							localSpec, SdpPortManagerEvent.NO_ERROR);
				}
			}
		} catch (MsControlException e) {
			log.error(e.getMessage(), e);
			event = new SdpPortManagerEventImpl(null, this, null,
					SdpPortManagerEvent.RESOURCE_UNAVAILABLE);
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		} finally {
			notifyEvent(event);
		}
	}

	@Override
	public void processSdpAnswer(SessionSpec answer)
			throws SdpPortManagerException {
		log.info("processSdpAnswer");
		SdpPortManagerEventImpl event = null;

		try {
			if (answer == null) {
				event = new SdpPortManagerEventImpl(null, this, localSpec,
						SdpPortManagerEvent.SDP_NOT_ACCEPTABLE);
			} else {
				userAgentSDP = answer;
				SessionSpec[] intersectionSessions = SessionSpec.intersect(
						localSpec, userAgentSDP);
				resource.setRemoteSessionSpec(intersectionSessions[1]);
				localSpec = intersectionSessions[0];
				resource.setLocalSessionSpec(localSpec);

				boolean sdpNotAcceptable = true;
				for (MediaSpec ms : localSpec.getMediaSpecs()) {
					if (!ms.getPayloads().isEmpty()) {
						sdpNotAcceptable = false;
						break;
					}
				}

				if (sdpNotAcceptable) {
					event = new SdpPortManagerEventImpl(null, this, localSpec,
							SdpPortManagerEvent.SDP_NOT_ACCEPTABLE);
				} else {
					event = new SdpPortManagerEventImpl(
							SdpPortManagerEvent.ANSWER_PROCESSED, this, null,
							SdpPortManagerEvent.NO_ERROR);
				}
			}
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		} finally {
			notifyEvent(event);
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
	public SessionSpec getMediaServerSessionDescription()
			throws SdpPortManagerException {
		return localSpec;
	}

	@Override
	public SessionSpec getUserAgentSessionDescription()
			throws SdpPortManagerException {
		return userAgentSDP;
	}

	@SuppressWarnings("unchecked")
	private void notifyEvent(SdpPortManagerEventImpl event) {
		for (MediaEventListener listener : mediaListenerList) {
			listener.onEvent(event);
		}
	}

}
