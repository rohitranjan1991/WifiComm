package com.wificomm.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.wificomm.PrefManager.Prefs;
import com.wificomm.common.DeviceList;
import com.wificomm.common.wificommApplication;
import com.wificomm.constants.Constants;
import com.wificomm.deviceScan.wifiScanReceive;
import com.wificomm.deviceScan.wifiScanSend;
import com.wificomm.duringCall.OnCallActivity;
import com.wificomm.handshake.CallRequestAcceptor;
import com.wificomm.incomingcall.IncomingCall;
import com.wificomm.voiceSendReceive.DataSend;

public class wificommService extends Service {

	private Handler callRequestHandler;
	private Handler scanReceiveHandler;
	private CallRequestAcceptor callReqAcceptor;
	private String ipCaller;
	List<DeviceList> myDevices = new ArrayList<DeviceList>();
	private DataSend send;
	private Handler sendHandler;
	private SharedPreferences settings;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return START_STICKY;
	}

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
			case 1:

				// for wifi scan
				settings = getSharedPreferences("wificomm",
						Context.MODE_PRIVATE);
				WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
				WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
				int ip = wifiInfo.getIpAddress();
				String ipAddress = Formatter.formatIpAddress(ip);
				new wifiScanSend(mHandle, "DeviceName : "
						+ Prefs.getInstance(settings).fetch("username")
						+ "|IP : " + ipAddress, msg.obj.toString(),
						"DeviceInfo Sent", "DeviceInfo Not Sent").start();

				break;
			case 2:
				// for wifi scan
				String[] separated = msg.obj.toString().split("\\|");
				String name = separated[0].substring(13, separated[0].length())
						.trim();
				String ipAddr = separated[1].substring(5,
						separated[1].length()).trim();
				if(!hasDevice(ipAddr))
				{myDevices.add(new DeviceList(name, ipAddr));
				wificommApplication.getInstance().setMyDevices(myDevices);
				}
				sendBroadcast(Constants.MSG_UPDATE_LIST);
				
				break;
			case 3:

				switch (msg.arg1) {
				case 0:
					// from CallRequestAcceptor
					// from the call receiver side
					ipCaller = msg.obj.toString();
					callReqAcceptor = null;

					startIncommingCall(ipCaller, getDeviceName(ipCaller)
							.toString(), Constants.PURPOSE_RECEIVING,
							"Reply True");

					/*
					 * AlertDialog.Builder alertDialogBuilder = new
					 * AlertDialog.Builder( getApplicationContext());
					 * 
					 * // set title
					 * alertDialogBuilder.setTitle("Incoming Call");
					 * 
					 * // set dialog message alertDialogBuilder
					 * 
					 * .setMessage("Call From " + ipCaller)
					 * .setCancelable(false) .setPositiveButton("Pick Up!!", new
					 * DialogInterface.OnClickListener() { public void onClick(
					 * DialogInterface dialog, int id) {
					 * 
					 * startMainCall( ipCaller, getDeviceName(ipCaller)
					 * .toString(), Constants.PURPOSE_RECEIVING, "Reply True");
					 * 
					 * 
					 * send = new DataSend(mHandle, ipCaller, "Reply True");
					 * send.start();
					 * 
					 * 
					 * } }) .setNegativeButton("Or Not!!", new
					 * DialogInterface.OnClickListener() {
					 * 
					 * public void onClick( DialogInterface dialog, int id) {
					 * send = new DataSend(mHandle, ipCaller, "Reply False");
					 * send.start(); dialog.cancel(); startCallAcceptor(); } });
					 * 
					 * // create alert dialog AlertDialog alertDialog =
					 * alertDialogBuilder.create(); alertDialog.show();
					 * timerDelayRemoveDialog(Constants.callTimeoutTime,
					 * alertDialog);
					 */
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
	private Boolean hasDevice(String ip) {
		myDevices = wificommApplication.getInstance().getMyDevices();
		Iterator<DeviceList> iterator = myDevices.iterator();
		for (DeviceList device : myDevices) {
			if (device.getIP().contentEquals(ip))
				return true;
		}

		return false;
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
				} else if (intent.getStringExtra("activity").contentEquals(
						"IncomingCall")) {
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
			IntentFilter intentFilter = new IntentFilter(
					Constants.ACTION_STRING_SERVICE);

			registerReceiver(serviceReceiver, intentFilter);
		}
		startDeviceScan();
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

		Toast.makeText(getApplicationContext(), "Service Destroyed",
				Toast.LENGTH_SHORT).show();
	}

	private void sendBroadcast(int message) {
		Intent new_intent = new Intent();
		new_intent.setAction(Constants.ACTION_STRING_ACTIVITY);
		new_intent.putExtra("message", message);
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

	public void startCallAcceptor() {
		if (callReqAcceptor == null) {
			callReqAcceptor = new CallRequestAcceptor(mHandle);
			callReqAcceptor.start();
		}
	}

	public void startDeviceScan() {
		new wifiScanReceive(mHandle).start();
	}

	public void startIncommingCall(String callerIp, String name, int purpose,
			String extraText) {
		Intent intent = new Intent(getApplicationContext(), IncomingCall.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("ip", callerIp);
		intent.putExtra("name", name);
		intent.putExtra("purpose", purpose);
		intent.putExtra("extraText", extraText);
		startActivity(intent);

	}

}
