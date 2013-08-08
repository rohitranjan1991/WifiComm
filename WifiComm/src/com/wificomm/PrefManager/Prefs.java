package com.wificomm.PrefManager;

import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Prefs implements PrefManagerInterface{

	private static Prefs instance=null;
	private static SharedPreferences settings;
	private static Editor edit;
	public static Prefs getInstance(SharedPreferences pref)
 {

		if(instance==null)
		{
			instance=new Prefs();
			
		}
		
		settings=pref;
		edit = settings.edit();
		return instance;
		
	}
	

	

	@Override
	public void save(Map <String ,String> map) {
		
		for(String key:map.keySet())
		{
			edit.putString(key, map.get(key));
		}
		
		edit.commit();
				
	}


	@Override
	public void wipe() {
		edit.clear();
	}


	@Override
	public String fetch(String key) {
		
		/*Map<String,String> retMap=new HashMap<String,String>();
		for(String key :keys)
		{
			retMap.put(key, settings.getString(key, null));
			
		}
		*/
			return settings.getString(key, null);
		
			
	}




	@Override
	public Boolean contains(String key) {
		if(settings.contains(key))
			return true;
		return false;
	}
	
	
	
}
