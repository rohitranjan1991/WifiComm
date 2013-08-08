package com.wificomm.deviceScanView;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wificomm.CallDisconnect;
import com.wificomm.R;
import com.wificomm.PrefManager.Prefs;
import com.wificomm.PrefManager.MenuPreference;
import com.wificomm.common.DeviceList;
import com.wificomm.constants.Constants;
import com.wificomm.deviceScan.wifiScanReceive;
import com.wificomm.deviceScan.wifiScanSend;
import com.wificomm.duringCall.OnCallActivity;
import com.wificomm.handshake.CallReplyAcceptor;
import com.wificomm.handshake.CallRequestAcceptor;
import com.wificomm.voiceSendReceive.DataSend;
import com.wificomm.voiceSendReceive.receiveVoice;

public class MainActivity extends Activity implements OnClickListener {

	// //////////////////////////////// variables
	// ////////////////////////////////////////

	Button bScan, bIterate;
	ImageButton bCall;
	TextView name, ip;
	Handler recHandler, sendHandler, CallManage, disCallHandler,
			scanReceiveHandler = null;
	Handler callRequestHandler = null, callReply, receiveVoice;
	TextView displayName;
	Integer rowNum;
	private SharedPreferences settings;
	DataSend send;
	ProgressDialog progress;
	CallRequestAcceptor callReqAcceptor;
	receiveVoice rVoice;
	CallManage callManage;
	View connectionrow;
	String ipToCall, ipCaller;
	CallDisconnect callDis;
	int currentCallerPos = -1;
	private AudioManager am;
	

	private SensorManager mSensorManager;
	private Boolean CallRequestSent = false;
	Boolean IsOnCall = false;
	ArrayAdapter<DeviceList> adapter;
	ListView list;
	private CallReplyAcceptor callReplyObject;

	List<DeviceList> myDevices = new ArrayList<DeviceList>();
	private AudioManager audioManager;
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
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
				case 4:
					callReply = (Handler) msg.obj;
					break;
				case 7:
					scanReceiveHandler = (Handler) msg.obj;
					break;
				}
				break;
			case 1:
				// for wifi scan
				WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
				WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
				int ip = wifiInfo.getIpAddress();
				String ipAddress = Formatter.formatIpAddress(ip);
				new wifiScanSend(mHandle, "DeviceName : "
						+ Prefs.getInstance(settings).fetch("username") + "|IP : " + ipAddress,
						msg.obj.toString(), "DeviceInfo Sent",
						"DeviceInfo Not Sent").start();

				break;
			case 2:
				// for wifi scan
				String[] separated = msg.obj.toString().split("\\|");
				String name = separated[0].substring(13, separated[0].length())
						.trim();
				String ipAddr1 = separated[1].substring(5,
						separated[1].length()).trim();
				myDevices.add(new DeviceList(name, ipAddr1));
				adapter.notifyDataSetChanged();
				break;

			case 3:

				switch (msg.arg1) {
				case 0:
					// from CallRequestAcceptor
					// from the call receiver side
					ipCaller = msg.obj.toString();
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(

					MainActivity.this);

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
											currentCallerPos = findRow(ipCaller);

											if (currentCallerPos != -1) {
												myDevices.get(currentCallerPos)
														.setOnCallState(true);

												connectionrow = list
														.getAdapter()
														.getView(
																currentCallerPos,
																findViewById(R.layout.row_layout),
																(ViewGroup) findViewById(R.id.listView1));

											} else {
												View v1 = null;
												myDevices.add(new DeviceList(
														"Unknown", ipCaller));
												adapter.notifyDataSetChanged();
												try {
													Thread.sleep(500);
												} catch (InterruptedException e) {
													// TODO Auto-generated catch
													// block
													e.printStackTrace();
												}
												currentCallerPos = findRow(ipCaller);

												if (currentCallerPos != -1) {

													myDevices.get(
															currentCallerPos)
															.setOnCallState(
																	true);
													connectionrow = list
															.getAdapter()
															.getView(
																	currentCallerPos,
																	findViewById(R.layout.row_layout),
																	(ViewGroup) findViewById(R.id.listView1));
												}
											}
											adapter.notifyDataSetChanged();
											TextView callerName = (TextView) list
													.getAdapter()
													.getView(
															currentCallerPos,
															findViewById(R.layout.row_layout),
															(ViewGroup) findViewById(R.id.listView1))
													.findViewById(R.id.topText)
													.findViewById(R.id.text)
													.findViewById(R.id.nameView)
													.findViewById(R.id.Name);
											startMainCall(
													ipCaller,
													callerName.getText()
															.toString(),
													Constants.PURPOSE_RECEIVING,
													"Reply True", 2);
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

				Toast.makeText(MainActivity.this,
						"Port Already in Use . Closing the app!!!",
						Toast.LENGTH_SHORT).show();
				MainActivity.this.finish();
				break;
			}

		}
	};
	
	// //////////////////////////////// X-X-X-X

	// ////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize();

	}

	@Override
	protected void onResume() {

		
		refreshOnResume();
		
		super.onResume();
	}

	private void refreshOnResume() {
		if (scanReceiveHandler == null) {
			new wifiScanReceive(mHandle).start();
		}
		if (callRequestHandler == null) {
			callReqAcceptor = new CallRequestAcceptor(mHandle);
			callReqAcceptor.start();
		}
		
		if(Prefs.getInstance(settings).contains("username"))
		{
			displayName.setText(Prefs.getInstance(settings).fetch("username"));
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected void onPause() {

		if (scanReceiveHandler != null) {
			scanReceiveHandler.sendMessage(scanReceiveHandler.obtainMessage(0,
					"stopService"));
			scanReceiveHandler = null;
		}
		if (callRequestHandler != null) {
			callRequestHandler.sendMessage(callRequestHandler.obtainMessage(0,
					"Stop"));
			callRequestHandler = null;
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onPause();
	}

	private void initialize() {

		bScan = (Button) findViewById(R.id.bScan);
		bIterate = (Button) findViewById(R.id.bIterate);

		bIterate.setOnClickListener(this);
		bScan.setOnClickListener(this);

		// called during onresume Methord
		// new wifiScanReceive(mHandle).start();
		// initializing audiomanager
		settings = getSharedPreferences("wificomm", Context.MODE_PRIVATE); 
		// ///////////////////////////////////list
		adapter = new MyListAdapter();
		list = (ListView) findViewById(R.id.listView1);
		list.setAdapter(adapter);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		displayName = (TextView) findViewById(R.id.displayName);
		if(Prefs.getInstance(settings).contains("username"))
		{
			displayName.setText(Prefs.getInstance(settings).fetch("username"));
		}
			
		
		
		/*
		 * rec = new DataReceive(mHandle); rec.start();
		 */

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		callManage = new CallManage(mHandle, mSensorManager);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater menuInflater = getMenuInflater();
	        menuInflater.inflate(R.layout.menu, menu);
	        return true;
		/*getMenuInflater().inflate(R.menu.main, menu);
		return true;*/
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// adapter

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		  switch (item.getItemId())
		  {
		  case R.id.menu_preferences:
			  Intent intent = new Intent(MainActivity.this, MenuPreference.class);
			  startActivityForResult(intent, 3);
			  break;
		  }
		  
		return super.onOptionsItemSelected(item);
	}

	public class MyListAdapter extends ArrayAdapter<DeviceList> implements
			OnClickListener {

		@Override
		public void notifyDataSetChanged() {

			super.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			View view = convertView;
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.row_layout, parent,
						false);
			}

			DeviceList devList = myDevices.get(position);
			name = (TextView) view.findViewById(R.id.Name);
			ip = (TextView) view.findViewById(R.id.IPAddress);
			name.setText(devList.getName());
			ip.setText(devList.getIP());
			bCall = (ImageButton) view.findViewById(R.id.bCall);
		
			/*if (devList.onCall()) {
				bCall.setEnabled(false);
				bDisconnect.setEnabled(true);
				bDisconnect.setClickable(true);
			} else {
				bCall.setEnabled(true);
				bDisconnect.setEnabled(false);
				bDisconnect.setClickable(false);
			}*/
			bCall.setOnClickListener(this);
			
			return view;

			// return super.getView(position, convertView, parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getCount()
		 */
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return myDevices.size();
		}

		public MyListAdapter() {
			super(MainActivity.this, R.layout.row_layout, myDevices);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onClick(View v) {
			View row = (View) v.getParent().getParent();
			TextView ip = (TextView) row.findViewById(R.id.IPAddress);
			TextView name = (TextView) row.findViewById(R.id.Name);
			ipToCall = ip.getText().toString();
			Toast.makeText(MainActivity.this,
					"IP Address to connect = " + ipToCall, Toast.LENGTH_SHORT)
					.show();
			connectionrow = (View) v.getParent();
			switch (v.getId()) {
			case R.id.bCall:
				currentCallerPos = findRow(ipToCall);
				myDevices.get(currentCallerPos).setOnCallState(true);
				adapter.notifyDataSetChanged();

				/*
				 * callRequestHandler.sendMessage(callRequestHandler
				 * .obtainMessage(0, "Stop")); callRequestHandler=null;
				 */
				startMainCall(ipToCall, name.getText().toString(),
						Constants.PURPOSE_CALLING, null, 1);
				/*
				 * call = new DataSend(mHandle, ipAddr, "Initiate Call !!");
				 * call.start();
				 */
				break;
			
			}

		}

	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {

			if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("result");
				myDevices.get(currentCallerPos).setOnCallState(false);
				adapter.notifyDataSetChanged();
				Toast.makeText(this, "Result From Caller : " + result,
						Toast.LENGTH_SHORT).show();
			}
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Result From Caller : Cancelled",
						Toast.LENGTH_SHORT).show();
			}
		} else if (requestCode == 2) {
			if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("result");
				myDevices.get(currentCallerPos).setOnCallState(false);
				adapter.notifyDataSetChanged();
				Toast.makeText(this, "Result From Call Receiver : " + result,
						Toast.LENGTH_SHORT).show();
			}
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Result From Call Receiver : ",
						Toast.LENGTH_SHORT).show();
			}
		}
		else if (requestCode == 3) {
			if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("result");
				myDevices.get(currentCallerPos).setOnCallState(false);
				adapter.notifyDataSetChanged();
				Toast.makeText(this, "Result From Preference Activity: " + result,
						Toast.LENGTH_SHORT).show();
			}
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Result From Preference Activity: Canceled !!",
						Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.bScan:

			ScanDevices();

			break;
		case R.id.bIterate:

			finish();

			break;

		}

	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// find row
	public int findRow(String str) {
		if (list.getCount() != 0) {
			int i = 0;
			for (i = 0; i < list.getCount(); i++) {
				// v = list.getAdapter().getView(i, null, null);
				View v1 = list.getAdapter().getView(i,
						findViewById(R.layout.row_layout),
						(ViewGroup) findViewById(R.id.listView1));
				TextView v2 = (TextView) v1.findViewById(R.id.topText)
						.findViewById(R.id.text).findViewById(R.id.IpView)
						.findViewById(R.id.IPAddress);

				// Toast.makeText(this,
				// "IP Address of the "+i+" row = "+v2.getText(),
				// Toast.LENGTH_SHORT).show();
				if (v2.getText().toString().contentEquals(str))
					return i;
			}

		}

		return -1;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void ScanDevices() {

		// clear the device list first
		myDevices.clear();

		WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();
		String ipAddress = Formatter.formatIpAddress(ip);
		for (int i = 0; i < 256; i++) {
			String currIp = "10.0.2." + i;
			// change it for proper use and ! in if condition for proper use
			if (!currIp.contentEquals(ipAddress)) {
				new wifiScanSend(mHandle, "DeviceInfo request", currIp,
						"DeviceInfo request Sent",
						"DeviceInfo request Not Sent").start();

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		// bScan.setEnabled(true);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// / call manage

	// start callAcceptor

	public class CallManage extends Thread implements SensorEventListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */

		private SensorManager mSensorManager;
		private Sensor mProximity;

		private Handler mainHandler;
		private Boolean appActive = true;
		private boolean IsOnCall = false;
		final Handler mHandle = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.obj.toString().contentEquals("Call Started")) {

				} else if (msg.obj.toString().contentEquals("Call Stoped")) {

				}
			}
		};

		public CallManage(Handler mainHandle, SensorManager mSensorManager) {
			this.mainHandler = mainHandle;
			Message msg1 = mainHandler.obtainMessage(0, 1, 0, mHandle);
			mainHandler.sendMessage(msg1);
			this.mSensorManager = mSensorManager;
			mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		}

		@Override
		public void run() {

			super.run();

			// mSensorManager.registerListener(this, mProximity,
			// SensorManager.SENSOR_DELAY_NORMAL);

			// mSensorManager.unregisterListener(this);

		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			final Window window = getWindow();
			WindowManager.LayoutParams lAttrs = getWindow().getAttributes();
			View view = ((ViewGroup) window.getDecorView().findViewById(
					android.R.id.content)).getChildAt(0);

			if (event.values[0] > 4) {

				lAttrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
				lAttrs.screenBrightness = -1;
				LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main);
				linearLayout.setBackgroundResource(R.drawable.back);
				getWindow().getDecorView().setBackgroundColor(Color.WHITE);
				view.setVisibility(View.VISIBLE);
			} else {

				lAttrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				lAttrs.screenBrightness = 0.1f;
				getWindow().getDecorView().setBackgroundColor(Color.BLACK);
				view.setVisibility(View.INVISIBLE);
			}
			window.setAttributes(lAttrs);

		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////End Call Manager//////////////////////////////////

	public void resetState() {

		// ScanDevices();

		callReqAcceptor = new CallRequestAcceptor(mHandle);
		callReqAcceptor.start();

	}

	public void startMainCall(String callerIp, String name, int purpose,
			String extraText, int requestcode) {
		Intent intent = new Intent(MainActivity.this, OnCallActivity.class);
		intent.putExtra("ip", callerIp);
		intent.putExtra("name", name);
		intent.putExtra("purpose", purpose);
		intent.putExtra("extraText", extraText);
		startActivityForResult(intent, requestcode);

	}

	public void timerDelayRemoveDialog(long time, final Dialog d) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				// resetState();
				d.dismiss();
			}
		}, time);
	}

}
