package com.example.wificomm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;

public class CallDisconnect extends Thread{

	
	
	Handler MainHandler;

	final Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			
			if (msg.obj.toString().contentEquals("Stop"))
			{		disconnectCall=true;
			}
			
			}
	};

	private ServerSocket serverSocket;



	private boolean disconnectCall=false;



	
	

	public CallDisconnect(Handler MainHandler) {
		this.MainHandler=MainHandler;
		MainHandler.sendMessage(MainHandler.obtainMessage(0, 6, 0, mHandle));
		
	}
	
	
	@Override
	public void run() {
		disconnectCall();
		super.run();
	}
	
	
	

	public Boolean disconnectCall() {
		try {
			serverSocket = new ServerSocket(7691);
			serverSocket.setSoTimeout(1000);

			Socket client = null;
			String res = null;

			
			while (!disconnectCall) {
				try {
					client = serverSocket.accept();
					InputStreamReader istr = new InputStreamReader(
							client.getInputStream());

					BufferedReader br = new BufferedReader(istr);
					res = br.readLine();
					if(res.contentEquals("Disconnect Call"))
					{client.close();
					serverSocket.close();
					serverSocket=null;
					disconnectCall=true;
					MainHandler.sendMessage(MainHandler.obtainMessage(4,0,0,true));
						
					}
					//ReceiverIP = client.getInetAddress().getHostAddress();
					
					//waitForReply=true;
					return true;

				} catch (Exception e) {

				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return false;
	}
	

}