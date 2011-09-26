package com.kurento.kas.mscontrol;

import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.Parameters;
import com.kurento.kas.mscontrol.internal.MediaSessionImpl;
import com.kurento.kas.mscontrol.mediacomponent.MediaComponentAndroid;
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
	 *            parameters are:
	 * 
	 * 
	 *            <table border="1" cellpadding=5>
	 *            <thead>
	 *            <tr>
	 *            <th>Basic Parameters</th>
	 *            <th>M&nbsp;/&nbsp;O</th>
	 *            <th>Type</th>
	 *            <th>Rank</th>
	 *            <th>Default value</th>
	 *            <th>Description</th>
	 *            </tr>
	 *            </thead>
	 * 
	 *            <tbody>
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid.NET_IF}</td>
	 *            <td>M</td>
	 *            <td>{@link NetIF}</td>
	 *            <td>[WIFI, MOBILE]</td>
	 *            <td></td>
	 *            <td>Indicate if the network interface is WIFI or MOBILE.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid.LOCAL_ADDRESS}</td>
	 *            <td>M</td>
	 *            <td>InetAddress</td>
	 *            <td>NA</td>
	 *            <td></td>
	 *            <td>Indicate the local IP address.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid.MAX_BANDWIDTH}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>NET_IF.MOBILE: [150000, 384000] NET_IF.WIFI: [150000, 1500000]</td>
	 *            <td>
	 *            <ul>
	 *            <li>NET_IF.MOBILE: 384000</li>
	 *            <li>NET_IF.WIFI: 1500000</li>
	 *            </ul>
	 *            </td>
	 *            <td>Indicate the max bandwidth will be used in bps(bits per
	 *            second).</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid.STREAMS_MODES}</td>
	 *            <td>O</td>
	 *            <td>HashMap&lt;MediaType, Mode&gt;</td>
	 *            <td>[SENDRECV, SENDONLY, RECVONLY]</td>
	 *            <td>SENDRECV</td>
	 *            <td>Indicate the mode of each media stream.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid.AUDIO_CODECS}</td>
	 *            <td>O</td>
	 *            <td>ArrayList&lt;AudioCodecType&gt;</td>
	 *            <td>[AMR, MP2]</td>
	 *            <td>[AMR, MP2]</td>
	 *            <td>Indicate the audio codecs supported.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid.VIDEO_CODECS}</td>
	 *            <td>O</td>
	 *            <td>ArrayList&lt;VideoCodecType&gt;</td>
	 *            <td>[H263, MPEG4]</td>
	 *            <td>[H263, MPEG4]</td>
	 *            <td>Indicate the video codecs supported.</td>
	 *            </tr>
	 *            </tbody>
	 * 
	 *            <thead>
	 *            <tr>
	 *            <th>Advanced Parameters</th>
	 *            </tr>
	 *            </thead> <tbody>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid.FRAME_SIZE}</td>
	 *            <td>O</td>
	 *            <td>Dimension</td>
	 *            <td>NA</td>
	 *            <td>352x288</td>
	 *            <td>Indicate the frame size in pixels (width x height).</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid.MAX_FRAME_RATE}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>[1, Integer.MAX_VALUE]</td>
	 *            <td>15</td>
	 *            <td>Indicate the max frame rate will be used.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid.GOP_SIZE}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>[0, Integer.MAX_VALUE]</td>
	 *            <td>6</td>
	 *            <td>Indicate the max number of frames in a group of pictures,
	 *            or 0 for intra_only.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid.FRAMES_QUEUE_SIZE}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>[2, Integer.MAX_VALUE]</td>
	 *            <td>2</td>
	 *            <td>Indicate the number of frames will be buffered from the
	 *            camera.</td>
	 *            </tr>
	 * 
	 *            </tbody>
	 *            </table>
	 * 
	 * @return a MediaSessionAndroid
	 * @throws MsControlException
	 */
	public static MediaSessionAndroid createMediaSession(Parameters params)
			throws MsControlException {
		return new MediaSessionImpl(params);
	}

}
