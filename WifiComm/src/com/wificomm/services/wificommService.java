package com.wificomm.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.wificomm.handshake.CallRequestAcceptor;

public class wificommService extends Service{

	private static final String ACTION_STRING_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";
	private Handler callRequestHandler;
	private Handler scanReceiveHandler;
	private CallRequestAcceptor callReqAcceptor;
	
    
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
				case 7:
					scanReceiveHandler = (Handler) msg.obj;
					break;
				}
				break;
				}
			super.handleMessage(msg);
		}
    	
    };
    
    
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	 private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

	      

			@Override
	        public void onReceive(Context context, Intent intent) {
	            Toast.makeText(getApplicationContext(), "received message in service..!", Toast.LENGTH_SHORT).show();
	           /* Intent i = new Intent();
	            i.setClass(context, sampleActivity.class);
	            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(i);  
	            */
	          
	            Log.d("Service", "Sending broadcast to activity");
	            sendBroadcast();
	        }
	    };
	
	    
	    public void onCreate() {
	        super.onCreate();
	        Log.d("Service", "onCreate");
	        if (serviceReceiver != null) {
	            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_SERVICE);
	
	            registerReceiver(serviceReceiver, intentFilter);
	        }
	        
	        callReqAcceptor = new CallRequestAcceptor(mHandle);
			callReqAcceptor.start();
	        
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
	        sendBroadcast(new_intent);
	    }

	

}
