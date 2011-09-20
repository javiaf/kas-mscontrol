package com.kurento.kas.mscontrol;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.Parameters;
import com.kurento.kas.mscontrol.internal.MediaSessionImpl;
import com.kurento.kas.mscontrol.networkconnection.NetIF;

/**
 * This factory creates MediaSessionAndroid from a set of parameters.
 * 
 * @author mparis
 * 
 */
public class MSControlFactory {

	/**
	 * Create a MediaSessionAndroid from a set of parameters.
	 * 
	 * @param params
	 *            Parameters to create a MediaSessionAndroid. The possible
	 *            parameter keys are:
	 *            <ul>
	 *            <li>{@link MediaSessionAndroid.NET_IF}: a {@link NetIF} to
	 *            indicate if the network interface is WIFI or MOBILE.
	 *            MANDATORY.
	 *            <li>{@link MediaSessionAndroid.LOCAL_ADDRESS}: an InetAddress
	 *            to indicate the local IP address.
	 *            <li>{@link MediaSessionAndroid.MAX_BANDWIDTH}: Integer to
	 *            indicate the max bandwidth will be used.
	 * 
	 *            <li>{@link MediaSessionAndroid.STREAMS_MODES}: a
	 *            HashMap&lt;MediaType, Mode&gt;to indicate the mode of each
	 *            media stream (SENDRECV, SENDONLY, RECVONLY)
	 *            <li>{@link MediaSessionAndroid.AUDIO_CODECS}: an
	 *            ArrayList&lt;AudioCodecType&gt; to indicate the audio codecs
	 *            supported.
	 *            <li>{@link MediaSessionAndroid.VIDEO_CODECS}: an
	 *            ArrayList&lt;VideoCodecType&gt; to indicate the video codecs
	 *            supported.
	 * 
	 *            <li>{@link MediaSessionAndroid.FRAME_SIZE}: Dimension to
	 *            indicate the video size.
	 *            <li>{@link MediaSessionAndroid.MAX_FR}: Integer to indicate
	 *            the max frame rate will be used.
	 *            <li>{@link MediaSessionAndroid.GOP_SIZE}: Integer to indicate
	 *            the number of frames in a group of pictures, or 0 for
	 *            intra_only.
	 *            <li>{@link MediaSessionAndroid.FRAMES_QUEUE_SIZE}: Integer to
	 *            indicate the number of frames will be buffered from the
	 *            camera.
	 *            </ul>
	 * @return a MediaSessionAndroid
	 * @throws MsControlException
	 */
	public static MediaSessionAndroid createMediaSession(Parameters params)
			throws MsControlException {
		return new MediaSessionImpl(params);
	}

}
