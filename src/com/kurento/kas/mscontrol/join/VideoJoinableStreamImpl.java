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

package com.kurento.kas.mscontrol.join;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sdp.SdpException;

import android.util.Log;

import com.kurento.commons.media.format.SessionSpec;
import com.kurento.commons.media.format.conversor.SdpConversor;
import com.kurento.commons.media.format.enums.MediaType;
import com.kurento.commons.media.format.enums.Mode;
import com.kurento.commons.mscontrol.MsControlException;
import com.kurento.commons.mscontrol.join.Joinable;
import com.kurento.commons.mscontrol.join.JoinableContainer;
import com.kurento.kas.media.codecs.VideoCodecType;
import com.kurento.kas.media.profiles.VideoProfile;
import com.kurento.kas.media.rx.MediaRx;
import com.kurento.kas.media.rx.VideoFrame;
import com.kurento.kas.media.rx.VideoRx;
import com.kurento.kas.media.tx.MediaTx;
import com.kurento.kas.media.tx.VideoInfoTx;
import com.kurento.kas.mscontrol.mediacomponent.internal.VideoSink;
import com.kurento.kas.mscontrol.networkconnection.internal.RTPInfo;

public class VideoJoinableStreamImpl extends JoinableStreamBase implements
		VideoSink, VideoRx {

	public final static String LOG_TAG = "VideoJoinableStream";

	private VideoProfile videoProfile = null;
	private SessionSpec localSessionSpec;

	private VideoTxThread videoTxThread = null;
	private VideoRxThread videoRxThread = null;

	private class Frame {
		private byte[] data;
		private int width;
		private int height;
		private long time;

		public Frame(byte[] data, int width, int height, long time) {
			this.data = data;
			this.width = width;
			this.height = height;
			this.time = time;
		}
	}

	private int QUEUE_SIZE = 2;
	private LinkedBlockingQueue<Frame> framesQueue;
	private LinkedBlockingQueue<Long> txTimes;

	private long timeFirstFrame;

	public VideoProfile getVideoProfile() {
		return videoProfile;
	}

	public VideoJoinableStreamImpl(JoinableContainer container,
			StreamType type, ArrayList<VideoProfile> videoProfiles,
			SessionSpec remoteSessionSpec, SessionSpec localSessionSpec,
			Integer maxDelayRx, Integer framesQueueSize) {
		super(container, type);
		this.localSessionSpec = localSessionSpec;
		if (framesQueueSize != null && framesQueueSize > QUEUE_SIZE)
			QUEUE_SIZE = framesQueueSize;
		Log.d(LOG_TAG, "QUEUE_SIZE: " + QUEUE_SIZE);

		framesQueue = new LinkedBlockingQueue<Frame>(QUEUE_SIZE);
		txTimes = new LinkedBlockingQueue<Long>(QUEUE_SIZE);

		Map<MediaType, Mode> mediaTypesModes = getModesOfMediaTypes(localSessionSpec);
		Mode videoMode = mediaTypesModes.get(MediaType.VIDEO);
		RTPInfo remoteRTPInfo = new RTPInfo(remoteSessionSpec);

		if (videoMode != null) {
			VideoCodecType videoCodecType = remoteRTPInfo.getVideoCodecType();
			VideoProfile videoProfile = getVideoProfileFromVideoCodecType(
					videoProfiles, videoCodecType);
			if (remoteRTPInfo.getFrameWidth() > 0
					&& remoteRTPInfo.getFrameHeight() > 0) {
				videoProfile.setWidth(remoteRTPInfo.getFrameWidth());
				videoProfile.setHeight(remoteRTPInfo.getFrameHeight());
			}
			if (remoteRTPInfo.getFrameRate() != null) {
				videoProfile.setFrameRateNum(remoteRTPInfo.getFrameRate()
						.getNumerator());
				videoProfile.setFrameRateDen(remoteRTPInfo.getFrameRate()
						.getDenominator());
			}

			if ((Mode.SENDRECV.equals(videoMode) || Mode.SENDONLY
					.equals(videoMode)) && videoProfile != null) {
				if (remoteRTPInfo.getVideoBandwidth() > 0)
					videoProfile.setBitRate(remoteRTPInfo.getVideoBandwidth()*1000);
				VideoInfoTx videoInfo = new VideoInfoTx(videoProfile);
				videoInfo.setOut(remoteRTPInfo.getVideoRTPDir());
				videoInfo.setPayloadType(remoteRTPInfo.getVideoPayloadType());
				int ret = MediaTx.initVideo(videoInfo);
				if (ret < 0) {
					Log.e(LOG_TAG, "Error in initVideo");
					MediaTx.finishVideo();
				}
				this.videoProfile = videoProfile;
				this.videoTxThread = new VideoTxThread();
				this.videoTxThread.start();
			}

			if ((Mode.SENDRECV.equals(videoMode) || Mode.RECVONLY
					.equals(videoMode))) {
				this.videoRxThread = new VideoRxThread(this, maxDelayRx);
				this.videoRxThread.start();
			}
		}

		this.timeFirstFrame = -1;

	}

	@Override
	public void putVideoFrame(byte[] data, int width, int height, long time) {
		if (timeFirstFrame == -1)
			timeFirstFrame = time;
		if (framesQueue.size() >= QUEUE_SIZE)
			framesQueue.poll();
		framesQueue.offer(new Frame(data, width, height, time-timeFirstFrame));
	}

	@Override
	public void putVideoFrameRx(VideoFrame videoFrame) {
		try {
			for (Joinable j : getJoinees(Direction.SEND))
				if (j instanceof VideoRx)
					((VideoRx) j).putVideoFrameRx(videoFrame);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		if (videoTxThread != null)
			videoTxThread.interrupt();

		Log.d(LOG_TAG, "finishVideo");
		MediaTx.finishVideo();
		Log.d(LOG_TAG, "stopVideoRx");
		MediaRx.stopVideoRx();
	}

	private class VideoTxThread extends Thread {
		@Override
		public void run() {
			int tFrame = 1000 / (videoProfile.getFrameRateNum() / videoProfile
					.getFrameRateDen());
			Frame frameProcessed;

			long tStart, tEnd, tEncode;
			long tTotal = 0;
			long n = 1;

			try {
				for (int i = 0; i < QUEUE_SIZE; i++)
					txTimes.offer(new Long(0));
				for (;;) {
					long t = System.currentTimeMillis();
					long h = (t - txTimes.take()) / QUEUE_SIZE;
					if (h < tFrame) {
						long s = tFrame - h;
						sleep(s);
					}
					frameProcessed = framesQueue.take();
					txTimes.offer(t);
					tStart = System.currentTimeMillis();
					MediaTx.putVideoFrame(frameProcessed.data,
							frameProcessed.width, frameProcessed.height,
							frameProcessed.time);
					tEnd = System.currentTimeMillis();
					tEncode = tEnd - tStart;
					tTotal += tEncode;
					Log.i(LOG_TAG, "Encode/send RTP frame time: " + tEncode
							+ "ms Average time: " + (tTotal / n) + " ms");
					n++;
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "VideoTxThread stopped");
			}
		}
	}

	private class VideoRxThread extends Thread {
		private VideoRx videoRx;
		private int maxDelayRx;

		public VideoRxThread(VideoRx videoRx, int maxDelayRx) {
			this.videoRx = videoRx;
			this.maxDelayRx = maxDelayRx;
			Log.d(LOG_TAG, "maxDelayRx: " + maxDelayRx);
		}

		@Override
		public void run() {
			Log.d(LOG_TAG, "startVideoRx");
			SessionSpec s = filterMediaByType(localSessionSpec, MediaType.VIDEO);
			if (!s.getMediaSpecs().isEmpty()) {
				try {
					String sdpVideo = SdpConversor.sessionSpec2Sdp(s);
					MediaRx.startVideoRx(sdpVideo, maxDelayRx, this.videoRx);
				} catch (SdpException e) {
					Log.e(LOG_TAG, "Could not start video rx " + e.toString());
				}
			}
		}
	}

	private VideoProfile getVideoProfileFromVideoCodecType(
			ArrayList<VideoProfile> videoProfiles, VideoCodecType videoCodecType) {
		if (videoCodecType == null)
			return null;
		for (VideoProfile vp : videoProfiles)
			if (videoCodecType.equals(vp.getVideoCodecType()))
				return vp;
		return null;
	}

}
