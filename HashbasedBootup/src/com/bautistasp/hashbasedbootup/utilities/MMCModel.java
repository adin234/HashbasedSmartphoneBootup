package com.bautistasp.hashbasedbootup.utilities;

public class MMCModel {
	String mountPoint;
	String mmc_host;
	
	
	public MMCModel(String mpoint, String m_h){
		this.mountPoint = mpoint;
		this.mmc_host = m_h;
	}


	public String getMountPoint() {
		return mountPoint;
	}


	public void setMountPoint(String mountPoint) {
		this.mountPoint = mountPoint;
	}


	public String getMmc_host() {
		return mmc_host;
	}


	public void setMmc_host(String mmc_host) {
		this.mmc_host = mmc_host;
	}
	
	
	
}
