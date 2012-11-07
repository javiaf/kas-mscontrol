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

package com.kurento.mscontrol.kas;

import com.kurento.commons.config.Parameters;
import com.kurento.kas.media.codecs.AudioCodecType;
import com.kurento.kas.media.codecs.VideoCodecType;
import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.kas.internal.MediaSessionImpl;
import com.kurento.mscontrol.kas.networkconnection.NetIF;
import com.kurento.mscontrol.kas.networkconnection.PortRange;

/**
 * This factory must be used to create {@link MediaSessionAndroid} objects from a
 * set of parameters.
 * 
 */
public class MsControlFactoryAndroid {

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
	 *            <th>Range</th>
	 *            <th>Default value</th>
	 *            <th>Description</th>
	 *            </tr>
	 *            </thead>
	 * 
	 *            <tbody>
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#NET_IF NET_IF}</td>
	 *            <td>M</td>
	 *            <td>{@link NetIF}</td>
	 *            <td>[WIFI, MOBILE]</td>
	 *            <td></td>
	 *            <td>Indicate if the network interface is WIFI or MOBILE.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#LOCAL_ADDRESS LOCAL_ADDRESS}</td>
	 *            <td>M</td>
	 *            <td>InetAddress</td>
	 *            <td>NA</td>
	 *            <td></td>
	 *            <td>Indicate the local IP address.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#MAX_BANDWIDTH MAX_BANDWIDTH}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>
	 *            - NET_IF.MOBILE: [50000, 384000] <br>
	 *            - NET_IF.WIFI: [50000, 3000000]</td>
	 *            <td>
	 *            - NET_IF.MOBILE: 384000 <br>
	 *            - NET_IF.WIFI: 3000000</td>
	 *            <td>Indicate the max bandwidth will be used in bps(bits per
	 *            second).</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#MAX_DELAY MAX_DELAY}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>[0, {@link Integer#MAX_VALUE}]</td>
	 *            <td>200</td>
	 *            <td>Indicate the max delay for media reception in ms
	 *            (miliseconds).</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#STREAMS_MODES STREAMS_MODES}</td>
	 *            <td>O</td>
	 *            <td>Map&lt; MediaType, Mode&gt;</td>
	 *            <td>[SENDRECV, SENDONLY, RECVONLY]</td>
	 *            <td>SENDRECV</td>
	 *            <td>Indicate the mode of each media stream.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#AUDIO_CODECS AUDIO_CODECS}</td>
	 *            <td>O</td>
	 *            <td>List&lt;{@link AudioCodecType}&gt;</td>
	 *            <td>[AMR, MP2, PCMU, PCMA]</td>
	 *            <td>[AMR, MP2, PCMU, PCMA]</td>
	 *            <td>Indicate the audio codecs supported.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#AUDIO_LOCAL_PORT_RANGE
	 *            AUDIO_LOCAL_PORT_RANGE}</td>
	 *            <td>O</td>
	 *            <td>{@link PortRange}</td>
	 *            <td>NA</td>
	 *            <td></td>
	 *            <td>Indicate local port interval to select an audio port. If
	 *            it is not set a random free port will be selected.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#VIDEO_CODECS VIDEO_CODECS}</td>
	 *            <td>O</td>
	 *            <td>List&lt;{@link VideoCodecType}&gt;</td>
	 *            <td>[H263, MPEG4, H.264]</td>
	 *            <td>[H263, MPEG4, H.264]</td>
	 *            <td>Indicate the video codecs supported.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#VIDEO_LOCAL_PORT_RANGE
	 *            VIDEO_LOCAL_PORT_RANGE}</td>
	 *            <td>O</td>
	 *            <td>{@link PortRange}</td>
	 *            <td>NA</td>
	 *            <td></td>
	 *            <td>Indicate local port interval to select a video port. If it
	 *            is not set a random free port will be selected.</td>
	 *            </tr>
	 * 
	 *            </tbody>
	 * 
	 *            <thead>
	 *            <tr>
	 *            <th>Advanced Parameters</th>
	 *            </tr>
	 *            </thead> <tbody>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#FRAME_WIDTH FRAME_WIDTH}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>NA</td>
	 *            <td>352</td>
	 *            <td>Indicate the frame width in pixels.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#FRAME_HEIGHT FRAME_HEIGHT}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>NA</td>
	 *            <td>288</td>
	 *            <td>Indicate the frame height in pixels.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#MAX_FRAME_RATE MAX_FRAME_RATE}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>[1, {@link Integer#MAX_VALUE}]</td>
	 *            <td>15</td>
	 *            <td>Indicate the max frame rate will be used.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#GOP_SIZE GOP_SIZE}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>[0, {@link Integer#MAX_VALUE}]</td>
	 *            <td>6</td>
	 *            <td>Indicate the max number of frames in a group of pictures,
	 *            or 0 for intra_only.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#FRAMES_QUEUE_SIZE
	 *            FRAMES_QUEUE_SIZE}</td>
	 *            <td>O</td>
	 *            <td>Integer</td>
	 *            <td>[2, {@link Integer#MAX_VALUE}]</td>
	 *            <td>2</td>
	 *            <td>Indicate the number of frames will be buffered from the
	 *            camera.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#SYNCHRONIZE_MEDIA_STREAMS
	 *            SYNCHRONIZE_MEDIA_STREAMS}</td>
	 *            <td>O</td>
	 *            <td>Boolean</td>
	 *            <td>{true, false}</td>
	 *            <td>false</td>
	 *            <td>Indicate if the received streams must be synchronized.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#STUN_HOST STUN_HOST}</td>
	 *            <td>M</td>
	 *            <td>String</td>
	 *            <td>NA</td>
	 *            <td></td>
	 *            <td>Stun server URL.</td>
	 *            </tr>
	 * 
	 *            <tr>
	 *            <td>{@link MediaSessionAndroid#STUN_PORT STUN_PORT}</td>
	 *            <td>M</td>
	 *            <td>Integer</td>
	 *            <td>[1024, 65535]</td>
	 *            <td>3478</td>
	 *            <td>Stun server port.</td>
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

	/**
	 * Create an empty Parameters map.
	 * 
	 * @return Parameters
	 */
	public static Parameters createParameters() {
		return new Parameters();
	}

}
