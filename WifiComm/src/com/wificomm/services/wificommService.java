package com.wificomm.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.wificomm.common.DeviceList;
import com.wificomm.common.wificommApplication;
import com.wificomm.constants.Constants;
import com.wificomm.deviceScanView.MainActivity;
import com.wificomm.duringCall.OnCallActivity;
import com.wificomm.handshake.CallRequestAcceptor;
import com.wificomm.voiceSendReceive.DataSend;

public class wificommService extends Service {

	private static final String ACTION_STRING_SERVICE = "ToService";
	private static final String ACTION_STRING_ACTIVITY = "ToActivity";
	private Handler callRequestHandler;
	private Handler scanReceiveHandler;
	private CallRequestAcceptor callReqAcceptor;
	private String ipCaller;
	List<DeviceList> myDevices = new ArrayList<DeviceList>();
	private DataSend send;
	private Handler sendHandler;

	final Handler mHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:

				// for storing the handler
				switch (msg.arg1) {
				case 1:
					callRequestHandler = (Handler) msg.obj;
					break;
				case 2:
					sendHandler = (Handler) msg.obj;
					break;
				case 7:
					scanReceiveHandler = (Handler) msg.obj;
					break;
				}
				break;

			case 3:

				switch (msg.arg1) {
				case 0:
					// from CallRequestAcceptor
					// from the call receiver side
					ipCaller = msg.obj.toString();
					callReqAcceptor = null;
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							getApplicationContext());

					// set title
					alertDialogBuilder.setTitle("Incoming Call");

					// set dialog message
					alertDialogBuilder

							.setMessage("Call From " + ipCaller)
							.setCancelable(false)
							.setPositiveButton("Pick Up!!",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {

											startMainCall(
													ipCaller,
													getDeviceName(ipCaller)
															.toString(),
													Constants.PURPOSE_RECEIVING,
													"Reply True");

											/*
											 * send = new DataSend(mHandle,
											 * ipCaller, "Reply True");
											 * send.start();
											 */

										}
									})
							.setNegativeButton("Or Not!!",
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface dialog, int id) {
											send = new DataSend(mHandle,
													ipCaller, "Reply False");
											send.start();
											dialog.cancel();
											startCallAcceptor();
										}
									});
					
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
					timerDelayRemoveDialog(Constants.callTimeoutTime,
							alertDialog);
					break;
				}

				break;
				
			case 5:
				// for wifi scan receive in case of bind Exception

				Toast.makeText(getApplicationContext(),
						"Port Already in Use . Closing the app!!!",
						Toast.LENGTH_SHORT).show();
				getApplicationContext().unregisterReceiver(serviceReceiver);
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private String getDeviceName(String ip) {
		myDevices = wificommApplication.getInstance().getMyDevices();
		Iterator<DeviceList> iterator = myDevices.iterator();
		for (DeviceList device : myDevices) {
			if (device.getIP().contentEquals(ip))
				return device.getName();
		}
		
		return "Unknown";
	}

	private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.hasExtra("activity")) {
				if (intent.getStringExtra("activity").contentEquals(
						"OnCallActivity")) {
					Toast.makeText(getApplicationContext(),
							"Result = " + intent.getIntExtra("result", -1),
							Toast.LENGTH_SHORT).show();
					startCallAcceptor();
				}
			}
			/*
			 * Toast.makeText(getApplicationContext(),
			 * "received message in service..!", Toast.LENGTH_SHORT) .show();
			 *//*
				 * Intent i = new Intent(); i.setClass(context,
				 * sampleActivity.class);
				 * i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i);
				 */

			/*
			 * Log.d("Service", "Sending broadcast to activity");
			 * sendBroadcast();
			 */
		}
	};

	public void onCreate() {
		super.onCreate();
		Log.d("Service", "onCreate");
		if (serviceReceiver != null) {
			IntentFilter intentFilter = new IntentFilter(ACTION_STRING_SERVICE);

			registerReceiver(serviceReceiver, intentFilter);
		}

		startCallAcceptor();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("Service", "onDestroy");
		callRequestHandler.sendMessage(callRequestHandler.obtainMessage(0,
				"Stop"));
		callRequestHandler = null;
		unregisterReceiver(serviceReceiver);
	}

	private void sendBroadcast() {
		Intent new_intent = new Intent();
		new_intent.setAction(ACTION_STRING_ACTIVITY);
		// new_intent.putExtra(name, value)
		sendBroadcast(new_intent);
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

	public void timerDelayRemoveDialog(long time, final Dialog d) {
		new Handler().postDelayed(new Runnable() {
			public void run() {

				
				d.dismiss();
			}
		}, time);
	}
	
	public void startCallAcceptor()
	{
		if(callReqAcceptor==null)
		{
			callReqAcceptor = new CallRequestAcceptor(mHandle);
			callReqAcceptor.start();
		}
	}

}
