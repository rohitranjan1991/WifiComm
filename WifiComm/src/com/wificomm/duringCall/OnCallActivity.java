package com.wificomm.duringCall;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.wificomm.R;
import com.wificomm.constants.Constants;
import com.wificomm.deviceScanView.MainActivity;
import com.wificomm.handshake.CallReplyAcceptor;
import com.wificomm.voiceSendReceive.DataSend;
import com.wificomm.voiceSendReceive.receiveVoice;

public class OnCallActivity extends Activity implements OnClickListener {
	
	
	private AudioManager am;
	TextView tCallerName, tCallerIp;
	Button bDisconnect;
	private Handler sendHandler;
	private String callIp,callName;
	private int purpose;
	private String extraText;
	private DataSend send;
	private boolean reply;
	private receiveVoice rVoice;
	private boolean CallRequestSent;
	private CallReplyAcceptor callReplyObject;
	private Handler callReplyHandler;
	private Handler receiveVoiceHandler;
	private Handler callManageHandler;
	
	
	public OnCallActivity() {
		am=null;
		tCallerName=null;
		tCallerIp=null;
		bDisconnect=null;
		sendHandler=null;
		callIp=null;
		callName=null;
		purpose=0;
		extraText=null;
		send=null;
		reply=false;
		rVoice=null;
		CallRequestSent=false;
		callReplyObject=null;
		callReplyHandler=null;
		receiveVoiceHandler=null;
		callManageHandler=null;
	}

	final Handler onCallHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:

				// for storing the handler
				switch (msg.arg1) {
				
				case 2:
					sendHandler = (Handler) msg.obj;
					break;
				case 3:
					callManageHandler = (Handler) msg.obj;
					break;

				case 4:
					callReplyHandler = (Handler) msg.obj;
					break;
				case 5:
					receiveVoiceHandler = (Handler) msg.obj;
					break;
				
				
				}
				break;
			

			case 3:

				switch (msg.arg1) {
				
				case 1:
					// after call request send
					// 3,1,0
					CallRequestSent = msg.obj.toString().contentEquals(
							"Call Request Sent");
					if (CallRequestSent) {
						
						callReplyObject = new CallReplyAcceptor(onCallHandler);
						callReplyObject.start();
					}

					break;
				case 2:

					// from receiver
					//3,2,0
					reply = (Boolean) msg.obj;

					if (reply) {
						// start receiving main call
						rVoice = new receiveVoice(onCallHandler, am);
						rVoice.start();
						// if call accepted
						// start sending main call
						send = new DataSend(onCallHandler, callIp, "Main Call");
						send.start();
						
					} else {// if call rejected

						resetState(Constants.CALL_REJECT_RECEIVER);
					}

					break;
				case 3:
					// from caller
					//if the reply from receiver is true
					//3,3,0

					reply = (Boolean) msg.obj;

					if (reply) {
						// start receiving main call
						rVoice = new receiveVoice(onCallHandler, am);
						rVoice.start();
						// if call accepted
						// start sending main call
						send = new DataSend(onCallHandler, callIp, "Main Call");
						send.start();
						

					} else {
						
						//to be defined for sending result back to activity
						resetState(Constants.CALL_RESPONSE_REJECT_CALLER);
					}

					break;
				case 4:
					//3,4,0
					//from call reply acceptor in case of out of time
					reply = (Boolean) msg.obj;

					if (!reply) {
						
						resetState(Constants.CALL_REPLY_TIMEOUT);
					}

					break;
				}

				break;
				
			case 4:
				// when call on progress
				//4,1,0
				switch (msg.arg1) {
				
				case 1:
					try {
						sendHandler.sendMessage(sendHandler.obtainMessage(0,
								"Stop"));
					} catch (Exception e) {
						//	Log.e("Error from sendHandler while disconnecting the call : ", e.getLocalizedMessage());
					}
					resetState(Constants.CALL_DISCONNECTED);
					break;
				}
				break;
			
			}

		}

		
		
	};



	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.oncall);
		
		initialize();
		initiateCall();

	}

	private void initialize() {
		//initilaizing view variables
		tCallerIp = (TextView) findViewById(R.id.tCallerIp);
		tCallerName = (TextView) findViewById(R.id.tCallerName);
		am = (AudioManager) getSystemService(MainActivity.AUDIO_SERVICE);
		
		//getting the Callers Details
		Intent intent = getIntent();
		callIp=intent.getStringExtra("ip");
		callName=intent.getStringExtra("name");
		purpose=intent.getIntExtra("purpose", 0);
		if(purpose==Constants.PURPOSE_RECEIVING)
		{
			extraText=intent.getStringExtra("extraText");
		}
		
		//setting the Callers Details
		tCallerIp.setText(callIp);
		tCallerName.setText(callName);
		bDisconnect=(Button) findViewById(R.id.bDisconnectCall);
		bDisconnect.setOnClickListener(this);
			}


	@Override
	protected void onPause() {
		if(sendHandler!=null){
			sendHandler.sendMessage(sendHandler.obtainMessage(0,
				"Stop"));
		sendHandler=null;
		}
		if(callReplyHandler!=null)
		{
			callReplyHandler.sendMessage(callReplyHandler.obtainMessage(0,
					"Stop"));
			callReplyHandler=null;
		}
		if(receiveVoiceHandler!=null)
		{
			receiveVoiceHandler.sendMessage(receiveVoiceHandler.obtainMessage(0, "Stop"));
		}
		resetState(Constants.CALL_ACITIVITY_FOCOUS_LOST);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void initiateCall() {
		if(purpose==Constants.PURPOSE_CALLING)
		{
			send = new DataSend(onCallHandler, callIp, "Initiate Call !!");
			send.start();
		}
		else
		if(purpose==Constants.PURPOSE_RECEIVING)
		{
			send = new DataSend(onCallHandler,
					callIp, extraText);
			send.start();
			
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.bDisconnectCall:
			if(receiveVoiceHandler!=null)
			{	receiveVoiceHandler.sendMessage(receiveVoiceHandler.obtainMessage(0, "Stop"));
			resetState(Constants.CALL_DISCONNECTED);}
			else
				resetState(99);
			
			break;
		}
	}
	
	private void resetState(int reason) {
		//reset view also of mainactivity
		
		 Intent returnIntent = new Intent();
		 returnIntent.setAction(Constants.ACTION_STRING_SERVICE);
		 returnIntent.putExtra("activity","OnCallActivity");
		 returnIntent.putExtra("purpose",purpose);
		 returnIntent.putExtra("result",reason);
		 sendBroadcast(returnIntent);
		// setResult(RESULT_OK,returnIntent);     
		 finish();
		 
	        
	        
		
	}
	
	

}
