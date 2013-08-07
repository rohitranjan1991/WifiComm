package com.wificomm.duringCall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wificomm.R;
import com.wificomm.constants.Constants;
import com.wificomm.deviceScan.wifiScanSend;
import com.wificomm.deviceScanView.MainActivity;
import com.wificomm.handshake.CallReplyAcceptor;
import com.wificomm.storeClasses.DeviceList;
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
	private Boolean reply;
	private receiveVoice rVoice;
	private boolean CallRequestSent;
	private CallReplyAcceptor callReplyObject;
	private Handler callReply;
	private Handler receiveVoice;
	private Handler callManage;
	
	public OnCallActivity() {
		callIp=null;
		callName=null;
		purpose=0;
		extraText=null;
		send=null;
				
	}

	final Handler mHandle = new Handler() {

		
		

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
					callManage = (Handler) msg.obj;
					break;

				case 4:
					callReply = (Handler) msg.obj;
					break;
				case 5:
					receiveVoice = (Handler) msg.obj;
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
						
						callReplyObject = new CallReplyAcceptor(mHandle);
						callReplyObject.start();
					}

					break;
				case 2:

					// from receiver
					reply = (Boolean) msg.obj;

					if (reply) {
						// start receiving main call
						rVoice = new receiveVoice(mHandle, am);
						rVoice.start();
						// if call accepted
						// start sending main call
						send = new DataSend(mHandle, callIp, "Main Call");
						send.start();
						// starting CallDisconnector Thread
						// callDis = new CallDisconnect(mHandle);
						// callDis.start();
					} else {// if call rejected

						resetState(Constants.CALL_REJECT_RECEIVER);
					}

					break;
				case 3:
					// from caller
					//if the reply from receiver is true

					reply = (Boolean) msg.obj;

					if (reply) {
						// start receiving main call
						rVoice = new receiveVoice(mHandle, am);
						rVoice.start();
						// if call accepted
						// start sending main call
						send = new DataSend(mHandle, callIp, "Main Call");
						send.start();
						

					} else {
						
						//to be defined for sending result back to activity
						resetState(Constants.CALL_REJECT_RESPONSE_CALLER);
					}

					break;
				case 4:
					
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
				switch (msg.arg1) {
				
				case 1:
					try {
						sendHandler.sendMessage(sendHandler.obtainMessage(0,
								"Stop"));
					} catch (Exception e) {

					}
					
					
					/*myDevices.get(currentCallerPos).setOnCallState(false);
					// receiveVoice.sendMessage(receiveVoice.obtainMessage(0,"Stop"));
					adapter.notifyDataSetChanged();*/
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


	private void initiateCall() {
		if(purpose==Constants.PURPOSE_CALLING)
		{
			send = new DataSend(mHandle, callIp, "Initiate Call !!");
			send.start();
		}
		else
		if(purpose==Constants.PURPOSE_RECEIVING)
		{
			send = new DataSend(mHandle,
					callIp, extraText);
			send.start();
			
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.bDisconnectCall:
			receiveVoice.sendMessage(receiveVoice.obtainMessage(0, "Stop"));
			
			/*Intent returnIntent = new Intent();
			 returnIntent.putExtra("result","Call Ended");
			 setResult(RESULT_OK,returnIntent);     
			 finish();*/
			break;
		}
	}
	
	private void resetState(int reason) {
		//reset view also of mainactivity
		
		 Intent returnIntent = new Intent();
		 returnIntent.putExtra("purpose",purpose);
		 returnIntent.putExtra("result",reason);
		 setResult(RESULT_OK,returnIntent);     
		 finish();
		
	}

	

}
