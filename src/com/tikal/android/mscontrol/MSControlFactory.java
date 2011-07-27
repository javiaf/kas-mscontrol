package com.tikal.android.mscontrol;

import com.tikal.mscontrol.MediaSession;
import com.tikal.mscontrol.MsControlException;
import com.tikal.mscontrol.Parameters;

/**
 * <p>
 * This factory creates MediaSessions instances of MediaSession from a set of
 * parameters.
 * 
 * </p>
 * 
 * @author Miguel París Díaz
 * 
 */
public class MSControlFactory {

	/**
	 * </p> Create a MediaSession from a set of parameters.
	 * 
	 * </p>
	 * 
	 * @param params
	 *            Parameters to create a MediaSession. The possible parameter
	 *            keys are:
	 *            <ul>
	 *            <li>MediaSessionAndroid.AUDIO_CODECS:
	 *            ArrayList<AudioCodecType> as value to indicate the audio
	 *            codecs supported.
	 *            <li>MediaSessionAndroid.VIDEO_CODECS:
	 *            ArrayList<VideoCodecType> as value to indicate the video
	 *            codecs supported.
	 *            <li>MediaSessionAndroid.LOCAL_ADDRESS: has as value an
	 *            InetAddress to indicate the local IP address.
	 *            <li>MediaSessionAndroid.CONNECTION_TYPE: has as value a
	 *            ConnectionType to indicate if the connection is WIFI or
	 *            MOBILE.
	 *            </ul>
	 * @return a MediaSession
	 * @throws MsControlException
	 */
	public static MediaSession createMediaSession(Parameters params) throws MsControlException {
		return new MediaSessionImpl(params);
	}

}
