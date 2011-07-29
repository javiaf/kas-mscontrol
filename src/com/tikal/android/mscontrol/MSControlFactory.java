package com.tikal.android.mscontrol;

import com.tikal.android.mscontrol.networkconnection.ConnectionType;
import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.Parameters;

/**
 * This factory creates MediaSessionAndroid from a set of parameters.
 * 
 * @author Miguel París Díaz
 * 
 */
public class MSControlFactory {

	/**
	 * Create a MediaSessionAndroid from a set of parameters.
	 * 
	 * @param params
	 *            Parameters to create a MediaSessionAndroid. The possible parameter
	 *            keys are:
	 *            <ul>
	 *            <li>{@link MediaSessionAndroid.AUDIO_CODECS}:
	 *            ArrayList&lt;AudioCodecType&gt; as value to indicate the audio
	 *            codecs supported.
	 *            <li>{@link MediaSessionAndroid.VIDEO_CODECS}:
	 *            ArrayList&lt;VideoCodecType&gt; as value to indicate the video
	 *            codecs supported.
	 *            <li>{@link MediaSessionAndroid.LOCAL_ADDRESS}: has as value an
	 *            InetAddress to indicate the local IP address.
	 *            <li>{@link MediaSessionAndroid.CONNECTION_TYPE}: has as value
	 *            a {@link ConnectionType} to indicate if the connection is WIFI
	 *            or MOBILE.
	 *            </ul>
	 * @return a MediaSessionAndroid
	 * @throws MsControlException
	 */
	public static MediaSessionAndroid createMediaSession(Parameters params)
			throws MsControlException {
		return new MediaSessionImpl(params);
	}

}
