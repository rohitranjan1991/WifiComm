package com.wificomm.deviceScan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class wifiScanReceive extends Thread {

	private static Handler mainHandler;
	private String temp = "";
	private ServerSocket serverSocket = null;
	private static Boolean scanReceive=true;

	// HandlerThread handle = new HandlerThread("My Thread");

	public Handler getHandle() {
		return rHandle;
	}

	final static Handler rHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj.toString().contentEquals("DeviceInfo Not Sent")) {
				Message msgToSend = mainHandler.obtainMessage(0,
						msg.obj.toString());
				mainHandler.sendMessage(msgToSend);
			} else if (msg.obj.toString().contentEquals("DeviceInfo Sent")) {
				Message msgToSend = mainHandler.obtainMessage(0,
						msg.obj.toString());
				mainHandler.sendMessage(msgToSend);
			} else if (msg.obj.toString().contains("DeviceName")) {
				Message msgToSend = mainHandler.obtainMessage(0,
						msg.obj.toString());
				mainHandler.sendMessage(msgToSend);
			}else if (msg.obj.toString().contains("stopService"))
			{
				scanReceive=false;
				
			}

		}
	};

	public wifiScanReceive(Handler handle) {
		this.mainHandler = handle;
		mainHandler.sendMessage(mainHandler.obtainMessage(0, 7, 0, rHandle));

	}

	@Override
	public void run() {

		super.run();
		
		String res = null;
		InputStreamReader istr;
		BufferedReader br;
		while (scanReceive) {

			try {
				Thread.sleep(200);
				serverSocket = new ServerSocket(9071);
//				serverSocket.setReuseAddress(true);
				while (true) {

					Socket client = serverSocket.accept();
					
					istr = new InputStreamReader(client.getInputStream());

					br = new BufferedReader(istr);
					res = br.readLine();
					if (res.contentEquals("DeviceInfo request")) {
						InetAddress addr = client.getInetAddress();

						Message msg = mainHandler.obtainMessage(1,
								addr.getHostAddress());
						mainHandler.sendMessage(msg);

					} else if (res.contains("DeviceName : ")) {
						Message msg = mainHandler.obtainMessage(2, res.trim());
						mainHandler.sendMessage(msg);
					}

					// if end
				}// while end

			} catch (Exception e) {
				Log.e("Error from wifiScanReceive : ", e.getLocalizedMessage());
				Message msg = mainHandler.obtainMessage(5, "Bing Exception");
				mainHandler.sendMessage(msg);
				scanReceive=false;

			} finally {
				if (serverSocket != null) {
					if (!serverSocket.isClosed()) {
						try {
							serverSocket.close();
						} catch (IOException e) {
							// catch logic
						}
					}
				}
			}

			// mHandler.obtainMessage(0,res).sendToTarget();
			// publishProgress(values)
		}

	}
}
