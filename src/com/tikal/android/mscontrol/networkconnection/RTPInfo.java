package com.tikal.android.mscontrol.networkconnection;

import android.util.Log;

import com.tikal.android.media.AudioCodecType;
import com.tikal.android.media.VideoCodecType;
import com.tikal.media.format.MediaSpec;
import com.tikal.media.format.SessionSpec;
import com.tikal.sdp.enums.MediaType;

/**
 * 
 * @author Miguel París Díaz
 * 
 */
public class RTPInfo {

	public final static String LOG_TAG = "RTPInfo";

	private String dstIp;

	private int dstVideoPort;
	private VideoCodecType videoCodecType;
	private int videoPayloadType;

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

	public int getDstAudioPort() {
		return dstAudioPort;
	}

	public AudioCodecType getAudioCodecType() {
		return audioCodecType;
	}

	public int getAudioPayloadType() {
		return audioPayloadType;
	}

	public RTPInfo(SessionSpec se){ //throws NoSuchMediaInfoException {
		try {
			this.dstIp = se.getOriginAddress();

			for (MediaSpec ms : se.getMediaSpec()) {
				Log.d(LOG_TAG, "ms: " + ms.toString());
				if (ms.getMediaType().equals(MediaType.AUDIO)) {
					Log.d(LOG_TAG, "audio");
					this.dstAudioPort = ms.getPort();
					Log.d(LOG_TAG, "dstAudioPort: " + dstAudioPort);
					Log.d(LOG_TAG,
							"encoding name: "
									+ ms.getPayloadList().get(0)
											.getEncodingName());
					try {
						this.audioCodecType = AudioCodecType.getCodecTypeFromName(ms.getPayloadList()
								.get(0).getEncodingName());
					} catch (Exception e) {
						Log.d(LOG_TAG, e.toString());
						e.printStackTrace();
					}
					this.audioPayloadType = ms.getPayloadList().get(0)
							.getPayload();
				} else if (ms.getMediaType().equals(MediaType.VIDEO)) {
					Log.d(LOG_TAG, "video");
					this.dstVideoPort = ms.getPort();
					Log.d(LOG_TAG, "dstVideoPort: " + dstVideoPort);
					Log.d(LOG_TAG,
							"encoding name: "
									+ ms.getPayloadList().get(0)
											.getEncodingName());
					try {
						this.videoCodecType = VideoCodecType.getCodecTypeFromName(ms.getPayloadList()
								.get(0).getEncodingName());
					} catch (Exception e) {
						Log.d(LOG_TAG, e.toString());
						e.printStackTrace();
					}
					this.videoPayloadType = ms.getPayloadList().get(0)
							.getPayload();
				}
			}
		} catch (IndexOutOfBoundsException ioobe) {
//			throw new NoSuchMediaInfoException(
//					"No such media info in SessionSpec object");
		}
	}

	public String getVideoRTPDir() {
		return "rtp://" + dstIp + ":" + dstVideoPort;
	}

	public String getAudioRTPDir() {
		return "rtp://" + dstIp + ":" + dstAudioPort;
	}

}
