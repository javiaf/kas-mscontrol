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

import android.util.Log;

import com.kurento.commons.media.format.MediaSpec;
import com.kurento.commons.media.format.SessionSpec;
import com.kurento.commons.sdp.enums.MediaType;
import com.kurento.kas.media.codecs.AudioCodecType;
import com.kurento.kas.media.codecs.VideoCodecType;
import com.kurento.kas.media.exception.CodecNotSupportedException;

public class RTPInfo {

	public final static String LOG_TAG = "RTPInfo";

	private String dstIp;

	private int dstVideoPort;
	private VideoCodecType videoCodecType;
	private int videoPayloadType;
	private int videoBandwidth;
	private int frameWidth;
	private int frameHeight;

	private int dstAudioPort;
	private AudioCodecType audioCodecType;
	private int audioPayloadType;

	public String getDstIp() {
		return dstIp;
	}

	public int getDstVideoPort() {
		return dstVideoPort;
	}

	public VideoCodecType getVideoCodecType() {
		return videoCodecType;
	}

	public int getVideoPayloadType() {
		return videoPayloadType;
	}

	public int getVideoBandwidth() {
		return videoBandwidth;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public int getDstAudioPort() {
		return dstAudioPort;
	}

	public AudioCodecType getAudioCodecType() {
		return audioCodecType;
	}

	public int getAudioPayloadType() {
		return audioPayloadType;
	}

	public RTPInfo(SessionSpec se) {
		Log.d(LOG_TAG, "sessionSpec:\n" + se);
		try {
			this.dstIp = se.getOriginAddress();
			for (MediaSpec ms : se.getMediaSpec()) {
				Log.d(LOG_TAG, "ms: " + ms.toString());
				if (ms.getMediaType().equals(MediaType.AUDIO)) {
					this.dstAudioPort = ms.getPort();
					if (ms.getPayloadList() != null
							&& ms.getPayloadList().size() > 0) {
						String encodingName = ms.getPayloadList().get(0)
								.getEncodingName();
						try {
							this.audioCodecType = AudioCodecType
									.getCodecTypeFromName(encodingName);
						} catch (CodecNotSupportedException e) {
							Log.w(LOG_TAG, encodingName + " not supported.");
						}
						this.audioPayloadType = ms.getPayloadList().get(0)
								.getPayload();
					}
				} else if (ms.getMediaType().equals(MediaType.VIDEO)) {
					this.dstVideoPort = ms.getPort();
					if (ms.getPayloadList() != null
							&& ms.getPayloadList().size() > 0) {
						String encodingName = ms.getPayloadList().get(0)
								.getEncodingName();
						try {
							this.videoCodecType = VideoCodecType
									.getCodecTypeFromName(encodingName);
						} catch (CodecNotSupportedException e) {
							Log.w(LOG_TAG, encodingName + " not supported.");
						}
						this.videoPayloadType = ms.getPayloadList().get(0)
								.getPayload();
						this.videoBandwidth = ms.getBandWidth();
					}
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error trying get RTP info from SDP");
			e.printStackTrace();
		}
	}

	public String getVideoRTPDir() {
		return "rtp://" + dstIp + ":" + dstVideoPort;
	}

	public String getAudioRTPDir() {
		return "rtp://" + dstIp + ":" + dstAudioPort;
	}

}
