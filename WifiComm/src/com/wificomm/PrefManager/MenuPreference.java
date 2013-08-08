package com.wificomm.PrefManager;


import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.wificomm.R;

public class MenuPreference extends Activity{

	
	EditText eName=null;
	private SharedPreferences settings=null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferancepage);
		initialize();
	}

	private void initialize() {
		
		eName=(EditText) findViewById(R.id.ePrefName);
		settings = getSharedPreferences("wificomm", Context.MODE_PRIVATE); 
		eName.setText(Prefs.getInstance(settings).fetch("username"));
	}

	@Override
	protected void onPause() {

		Map<String,String> map=new HashMap<String, String>();
		map.put("username", eName.getText().toString().trim());
		Prefs.getInstance(settings).save(map);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Log.e("Message From Thread Sleep", e.getMessage());
		}
		
		
		super.onPause();
	}

	@Override
	protected void onResume() {
		initialize();
				
		super.onResume();
	}
	
	
	
	

}
