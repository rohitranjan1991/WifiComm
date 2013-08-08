package com.wificomm.splashscreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.wificomm.R;
import com.wificomm.PrefManager.Prefs;
import com.wificomm.constants.Constants;
import com.wificomm.deviceScanView.MainActivity;
import com.wificomm.initialSettings.InitialSettings;

public class SplashScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        
	        setContentView(R.layout.splashscreen);
	        
	        SharedPreferences settings = getSharedPreferences("wificomm", Context.MODE_PRIVATE); 
	         Prefs prefs= Prefs.getInstance(settings);
	        
	        if(prefs.contains("username"))
	        {
	            new Handler().postDelayed(new Runnable(){
		            @Override
		            public void run() {
		                /* Create an Intent that will start the Menu-Activity. */
		                Intent mainIntent = new Intent(SplashScreen.this,MainActivity.class);
		                SplashScreen.this.startActivity(mainIntent);
		                SplashScreen.this.finish();
		            }
		        }, Constants.SPLASH_DISPLAY_LENGHT);
	        }
	        else
	        {
	        	  new Handler().postDelayed(new Runnable(){
			            @Override
			            public void run() {
			                /* Create an Intent that will start the Menu-Activity. */
			                Intent mainIntent = new Intent(SplashScreen.this,InitialSettings.class);
			                SplashScreen.this.startActivity(mainIntent);
			                SplashScreen.this.finish();
			            }
			        }, Constants.SPLASH_DISPLAY_LENGHT);
	        	
	        }
	   
	       
		
	}

	@Override
	protected void onPause() {
		finish();
		super.onPause();
	}
	

}
