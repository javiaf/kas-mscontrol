package com.kurento.kas.mscontrol.networkconnection;

/**
 * NetIF indicate the network interface.
 * 
 * @author mparis
 * 
 */
public enum NetIF {
	WIFI(1500000), MOBILE(384000);

	public static final int MIN_BANDWITH = 200000;
	private int maxBandwidth;

	public int getMaxBandwidth() {
		return maxBandwidth;
	}

	private NetIF(int maxBandwidth) {
		this.maxBandwidth = maxBandwidth;
	}
}