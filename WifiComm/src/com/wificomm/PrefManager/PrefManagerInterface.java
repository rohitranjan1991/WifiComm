package com.wificomm.PrefManager;

import java.util.Map;



	public interface PrefManagerInterface
	{
		void save(Map<String,String> map);
		void wipe();
		String fetch(String keys);
		Boolean contains(String key);
		
	}


