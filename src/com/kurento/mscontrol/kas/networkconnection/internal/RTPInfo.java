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

package com.kurento.mscontrol.kas.networkconnection.internal;

import java.util.List;
import java.util.Set;

import android.util.Log;

import com.kurento.kas.media.codecs.AudioCodecType;
import com.kurento.kas.media.codecs.VideoCodecType;
import com.kurento.kas.media.exception.CodecNotSupportedException;
import com.kurento.mediaspec.ArgumentNotSetException;
import com.kurento.mediaspec.MediaSpec;
import com.kurento.mediaspec.MediaType;
import com.kurento.mediaspec.Mode;
import com.kurento.mediaspec.Payload;
import com.kurento.mediaspec.PayloadRtp;
import com.kurento.mediaspec.SessionSpec;

public class RTPInfo {

	public final static String LOG_TAG = "RTPInfo";

	private String dstIp;

	private Mode videoMode;
	private int dstVideoPort;
	private VideoCodecType videoCodecType;
	private int videoPayloadType = -1;
	private int videoBandwidth = -1;
	private int frameWidth = -1;
	private int frameHeight = -1;
	private Fraction frameRate;

	private Mode audioMode;
	private int dstAudioPort;
	private AudioCodecType audioCodecType;
	private int audioPayloadType;

	public String getDstIp() {
		return dstIp;
	}

	public Mode getVideoMode() {
		return videoMode;
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

	public Fraction getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(Fraction frameRate) {
		this.frameRate = frameRate;
	}

	public Mode getAudioMode() {
		return audioMode;
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

		List<MediaSpec> medias = se.getMediaSpecs();
		if (medias.isEmpty())
			return;

		for (MediaSpec m : medias) {
			try {
				this.dstIp = m.getTransport().getRtp().getAddress();
				break;
			} catch (ArgumentNotSetException e) {
				Log.w(LOG_TAG, e.toString());
			}
		}

		videoMode = Mode.INACTIVE;
		audioMode = Mode.INACTIVE;

		for (MediaSpec m : medias) {
			Set<MediaType> mediaTypes = m.getTypes();
			if (mediaTypes.size() != 1)
				continue;
			for (MediaType t : mediaTypes) {
				if (Mode.INACTIVE.equals(audioMode) && t == MediaType.AUDIO) {
					audioMode = m.getMode();
					if (Mode.INACTIVE.equals(audioMode))
						continue;
					try {
						this.dstAudioPort = m.getTransport().getRtp().getPort();
					} catch (ArgumentNotSetException e) {
						Log.w(LOG_TAG, "Can not get port for audio " + e.toString());
						continue;
					}
					List<Payload> payloads = m.getPayloads();
					if ((payloads != null) && !payloads.isEmpty()) {
						Payload p = payloads.get(0);
						String encodingName = "";
						try {
							PayloadRtp rtpInfo = p.getRtp();
							encodingName = rtpInfo.getCodecName();
							this.audioCodecType = AudioCodecType
									.getCodecTypeFromName(encodingName);
							this.audioPayloadType = rtpInfo.getId();
						} catch (ArgumentNotSetException e1) {
							Log.w(LOG_TAG, "Can not get payload RTP info.");
						} catch (CodecNotSupportedException e) {
							Log.w(LOG_TAG, encodingName + " not supported.");
						}
					}
				} else if (Mode.INACTIVE.equals(videoMode)
						&& t == MediaType.VIDEO) {
					videoMode = m.getMode();
					if (Mode.INACTIVE.equals(videoMode))
						continue;
					try {
						this.dstVideoPort = m.getTransport().getRtp().getPort();
					} catch (ArgumentNotSetException e) {
						Log.w(LOG_TAG, "Can not get port for video " + e.toString());
						continue;
					}
					List<Payload> payloads = m.getPayloads();
					if ((payloads != null) && !payloads.isEmpty()) {
						Payload p = payloads.get(0);
						String encodingName = "";
						try {
							PayloadRtp rtpInfo = p.getRtp();
							encodingName = rtpInfo.getCodecName();
							this.videoCodecType = VideoCodecType
									.getCodecTypeFromName(encodingName);
							this.videoPayloadType = rtpInfo.getId();
							this.videoBandwidth = p.getRtp().getBitrate();
						} catch (ArgumentNotSetException e1) {
							Log.w(LOG_TAG, "Can not get payload RTP info.");
						} catch (CodecNotSupportedException e) {
							Log.w(LOG_TAG, encodingName + " not supported.");
						}
					}
				}
				break;
			}
		}
	}

	public String getVideoRTPDir() {
		return "rtp://" + dstIp + ":" + dstVideoPort;
	}

	public String getAudioRTPDir() {
		return "rtp://" + dstIp + ":" + dstAudioPort;
	}

}
