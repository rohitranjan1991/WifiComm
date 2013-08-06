package com.wificomm.handshake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;

public class CallRequestAcceptor extends Thread{

	
	
	Handler MainHandler;

	final Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			
			if (msg.obj.toString().contentEquals("Stop"))
			{		GotCallRequest=true;
			}
			
			
			
			}
	};

	private ServerSocket serverSocket;

	private boolean callAccept=true;

	private boolean GotCallRequest=false;

	private String ReceiverIP;

	private boolean waitForReply;
	
	

	public CallRequestAcceptor(Handler MainHandler) {
		this.MainHandler=MainHandler;
		MainHandler.sendMessage(MainHandler.obtainMessage(0, 1, 0, mHandle));
		
	}
	
	
	@Override
	public void run() {
		start_handshake();
		super.run();
	}
	
	
	

	public Boolean start_handshake() {
		try {serverSocket=null;
			serverSocket = new ServerSocket(7680);
			serverSocket.setSoTimeout(1000);

			Socket client = null;
			String res = null;

			
			while (callAccept && !GotCallRequest) {
				try {
					client = serverSocket.accept();
					InputStreamReader istr = new InputStreamReader(
							client.getInputStream());

					BufferedReader br = new BufferedReader(istr);
					res = br.readLine();
					ReceiverIP = client.getInetAddress().getHostAddress();
					client.close();
					serverSocket.close();
					serverSocket=null;
					
					MainHandler.sendMessage(MainHandler.obtainMessage(3,0,0,ReceiverIP));
					//waitForReply=true;
					return true;

				} catch (Exception e) {

				}
			}
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
		if(serverSocket!=null)
			try {
				if(!serverSocket.isClosed())
				{
				serverSocket.close();
				serverSocket=null;}
			} catch (IOException e) {
				e.printStackTrace();
			}
		return false;
	}
	

}
