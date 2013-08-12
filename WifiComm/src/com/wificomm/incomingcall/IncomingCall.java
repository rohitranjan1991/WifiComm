package com.wificomm.incomingcall;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.wificomm.R;
import com.wificomm.constants.Constants;
import com.wificomm.duringCall.OnCallActivity;


public class IncomingCall extends Activity implements OnClickListener {

	Button bPickUp,bOrNot;
	String ipCaller,callerName;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.incommingcall);
		
		initialize();
		
	}

	private void initialize() {
		Intent intent=getIntent();
		ipCaller=intent.getStringExtra("ip");
		callerName=intent.getStringExtra("name");
		bPickUp=(Button) findViewById(R.id.bPickUp);
		bOrNot=(Button) findViewById(R.id.bOrNot);
		bPickUp.setOnClickListener(this);
		bOrNot.setOnClickListener(this);
		
	}

	@Override
	protected void onPause() {
		finish();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId())
		{
		case R.id.bPickUp:

			startMainCall(
					ipCaller,
					callerName,
					Constants.PURPOSE_RECEIVING,
					"Reply True");

			
			
			break;
		case R.id.bOrNot:
			
			 Intent returnIntent = new Intent();
			 returnIntent.setAction(Constants.ACTION_STRING_SERVICE);
			 returnIntent.putExtra("activity","IncomingCall");
			 //returnIntent.putExtra("purpose",purpose);
			 returnIntent.putExtra("result",Constants.CALL_REJECT_RECEIVER);
			 sendBroadcast(returnIntent);
			
			break;
		}
		
	}
	
	public void startMainCall(String callerIp, String name, int purpose,
			String extraText) {
		Intent intent = new Intent(getApplicationContext(),
				OnCallActivity.class);
		intent.putExtra("ip", callerIp);
		intent.putExtra("name", name);
		intent.putExtra("purpose", purpose);
		intent.putExtra("extraText", extraText);
		startActivity(intent);

	}

}
