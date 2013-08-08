package com.wificomm.common;

import java.util.ArrayList;
import java.util.List;

public class wificommApplication {

	
	private static wificommApplication instance=null;
	List<DeviceList> myDevices = new ArrayList<DeviceList>();
	public static wificommApplication getInstance()
	{
		if(instance==null)
			instance=new wificommApplication();
		return instance;
	}
	
	
	
	public List<DeviceList> getMyDevices() {
		return myDevices;
	}
	public void setMyDevices(List<DeviceList> myDevices) {
		this.myDevices = myDevices;
	}
	
	
	
	
}
