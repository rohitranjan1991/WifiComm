package com.example.wificomm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;

public class CallReplyAcceptor extends Thread{

	Handler MainHandler;

	final Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			
			if (msg.obj.toString().contentEquals("Stop"))
			{			
				waitForReply=false;
			}
			
			
			
			}
	};

	private ServerSocket serverSocket;

	private boolean waitForReply,MainInterupt;

	
	public CallReplyAcceptor(Handler MainHandler) {
		this.MainHandler=MainHandler;
		MainHandler.sendMessage(MainHandler.obtainMessage(0, 1, 0, mHandle));
		waitForReply=true;
		MainInterupt=false;
	}
	
	
	@Override
	public void run() {
		waitForReply();
		super.run();
	}


	private void waitForReply() {
		Socket client = null;
		String res = null;
		try {
			serverSocket = new ServerSocket(7690);
			serverSocket.setSoTimeout(10000);

			

			while (waitForReply && !MainInterupt) {
				try {
					client = serverSocket.accept();
					InputStreamReader istr = new InputStreamReader(
							client.getInputStream());

					BufferedReader br = new BufferedReader(istr);
					res = br.readLine();
					//callAccept = false;
					//ReceiverIP = client.getInetAddress().getHostAddress();
					serverSocket.close();
					client.close();
					
					MainHandler.sendMessage(MainHandler.obtainMessage(3, 3, 0, true));

					//return true;

				} catch (Exception e) {
					waitForReply=false;
					serverSocket.close();
					client.close();
				}
			}
		} catch (Exception e1) {
			try {
				serverSocket.close();
				client.close();
			} catch (IOException e) {
					e.printStackTrace();
			}
			
			e1.printStackTrace();
		}
		//return false;
		
		
	}

}
