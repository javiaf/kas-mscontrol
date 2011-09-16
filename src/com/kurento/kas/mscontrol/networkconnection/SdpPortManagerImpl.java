package com.kurento.kas.mscontrol.networkconnection;

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
import com.kurento.commons.mscontrol.networkconnection.NetworkConnection;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManager;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManagerEvent;
import com.kurento.commons.mscontrol.networkconnection.SdpPortManagerException;

public class SdpPortManagerImpl implements SdpPortManager {

	private static Log log = LogFactory.getLog(SdpPortManagerImpl.class);

	private List<MediaSpec> combinedMediaList;
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
		// TODO Auto-generated method stub
		return null;
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
		SessionSpec sessionSpec = resource.generateSessionSpec();
		String localAddress = resource.getLocalAddress().getHostAddress();
		sessionSpec.setOriginAddress(localAddress);
		sessionSpec.setRemoteHandler(localAddress);
		SdpPortManagerEventImpl event = null;
		try {
			event = new SdpPortManagerEventImpl(
					SdpPortManagerEvent.OFFER_GENERATED, this,
					sessionSpec.getSessionDescription(),
					SdpPortManagerEvent.NO_ERROR);
		} catch (SdpException e) {
			event = new SdpPortManagerEventImpl(null, this, null,
					SdpPortManagerEvent.RESOURCE_UNAVAILABLE);
			log.error(
					"Error creating Session Description from resource media list",
					e);
			throw new SdpPortManagerException(
					"Error creating Session Description from resource media list",
					e);
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
		log.debug("processSdpOffer");
		SdpPortManagerEventImpl event = null;

		try {
			userAgentSDP = new SessionSpec(new String(offer));
			SessionSpec[] intersectionSessions = SpecTools
					.intersectionSessionSpec(userAgentSDP,
							resource.generateSessionSpec());
			combinedMediaList = intersectionSessions[1].getMediaSpec();

			if (combinedMediaList.isEmpty()) {
				event = new SdpPortManagerEventImpl(null, this, null,
						SdpPortManagerEvent.SDP_NOT_ACCEPTABLE);
			} else {
				userAgentSDP.setMediaSpec(combinedMediaList);
				resource.setRemoteSessionSpec(userAgentSDP);

				localSpec = intersectionSessions[0];
				String localAddress = resource.getLocalAddress()
						.getHostAddress();
				localSpec.setOriginAddress(localAddress);
				localSpec.setRemoteHandler(localAddress);
				resource.setLocalSessionSpec(localSpec);

				event = new SdpPortManagerEventImpl(
						SdpPortManagerEvent.ANSWER_GENERATED, this,
						localSpec.getSessionDescription(),
						SdpPortManagerEvent.NO_ERROR);
			}
		} catch (SdpException e) {
			event = new SdpPortManagerEventImpl(null, this, null,
					SdpPortManagerEvent.SDP_NOT_ACCEPTABLE);
			log.error("Error processing SDPOffer", e);
			throw new SdpPortManagerException("Error processing SDPOffer", e);
		}

		notifyEvent(event);
	}

	@Override
	public void processSdpAnswer(byte[] answer) throws SdpPortManagerException {
		try {
			userAgentSDP = new SessionSpec(new String(answer));

			combinedMediaList = userAgentSDP.getMediaSpec();
			resource.setRemoteSessionSpec(userAgentSDP);

			localSpec = SpecTools.intersectionSessionSpec(userAgentSDP,
					resource.generateSessionSpec())[0];
			resource.setLocalSessionSpec(localSpec);
			String localAddress = resource.getLocalAddress().getHostAddress();
			localSpec.setOriginAddress(localAddress);
			localSpec.setRemoteHandler(localAddress);
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
		combinedMediaList = null;
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
