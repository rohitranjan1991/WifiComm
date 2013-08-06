package com.wificomm.storeClasses;

public class DeviceList {
	
	private String name;
	private String IP;
	private Boolean onCall=false;
	
	
	public DeviceList(String name,String IP) {
		//super();
		this.name=name;
		this.IP=IP;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the iP
	 */
	public String getIP() {
		return IP;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param iP the iP to set
	 */
	public void setIP(String iP) {
		IP = iP;
	}

	public Boolean onCall() {
		return onCall;
	}

	public void setOnCallState(Boolean callState) {
		this.onCall = callState;
	}
	
	
	
		
	 
}
