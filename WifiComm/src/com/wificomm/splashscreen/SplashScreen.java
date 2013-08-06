package com.wificomm.splashscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.wificomm.R;
import com.wificomm.constants.Constants;
import com.wificomm.deviceScanView.MainActivity;

public class SplashScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        
	        setContentView(R.layout.splashscreen);
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
	

}
