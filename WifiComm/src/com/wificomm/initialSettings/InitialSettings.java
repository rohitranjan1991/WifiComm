package com.wificomm.initialSettings;


import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wificomm.R;
import com.wificomm.PrefManager.Prefs;
import com.wificomm.constants.Constants;
import com.wificomm.deviceScanView.MainActivity;

public class InitialSettings extends Activity implements OnClickListener {

	Button set;
	EditText eName;
	SharedPreferences settings;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.initialsettings);
		
		initialize();
		
	}

	private void initialize() {
		settings = getSharedPreferences("wificomm", Context.MODE_PRIVATE); 
		
		set=(Button) findViewById(R.id.bSetName);
		set.setOnClickListener(this);
		eName=(EditText) findViewById(R.id.eName);

	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.bSetName:
				
				
				if(!eName.getText().toString().trim().contentEquals(""))
				{ 
					Map<String,String > map=new HashMap<String, String>();
					map.put("username",eName.getText().toString().trim() );
					Prefs.getInstance(settings).save(map);;
					
					new Handler().postDelayed(new Runnable(){
			            @Override
			            public void run() {
			                /* Create an Intent that will start the Menu-Activity. */
			                Intent mainIntent = new Intent(InitialSettings.this,MainActivity.class);
			                InitialSettings.this.startActivity(mainIntent);
			                InitialSettings.this.finish();
			            }
			        }, Constants.SPLASH_DISPLAY_LENGHT);}
				else
				{
					Toast.makeText(this, "Enter Your name !!", Toast.LENGTH_SHORT).show();
				}
				break;
		}
		
	}
	
	
	
	

}
