package com.wificomm.duringCall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.wificomm.R;

public class OnCallActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oncall);
		Intent intent = getIntent();
		String message = intent.getStringExtra("Value 1");
		Toast.makeText(this, message,Toast.LENGTH_SHORT).show();
		
	}

}
