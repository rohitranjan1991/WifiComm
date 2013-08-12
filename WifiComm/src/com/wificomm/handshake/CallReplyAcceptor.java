package com.wificomm.handshake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.wificomm.constants.Constants;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CallReplyAcceptor extends Thread{

	Handler baseHandler;

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
		this.baseHandler=MainHandler;
		MainHandler.sendMessage(MainHandler.obtainMessage(0, 4, 0, mHandle));
		waitForReply=true;
		MainInterupt=false;
	}
	
	
	@Override
	public void run() {
		waitForReply();
		super.run();
	}


	private Boolean waitForReply() {
		Socket client = null;
		String res=null;
		try {
			serverSocket = new ServerSocket(7690);
			serverSocket.setSoTimeout(Constants.callTimeoutTime/100);

			
			int count=1;
			while (waitForReply && !MainInterupt) {
				try {
					count++;
					client = serverSocket.accept();
					InputStreamReader istr = new InputStreamReader(
							client.getInputStream());

					BufferedReader br = new BufferedReader(istr);
					res = br.readLine();
					//callAccept = false;
					//ReceiverIP = client.getInetAddress().getHostAddress();
					serverSocket.close();
					client.close();
					if(res.contentEquals("Reply true"))
						baseHandler.sendMessage(baseHandler.obtainMessage(3, 3, 0, true));
					else if(res.contentEquals("Reply false"))
						baseHandler.sendMessage(baseHandler.obtainMessage(3, 3, 0, false));

					return true;

				} catch (Exception e) {
					if(count>100){
					waitForReply=false;
					serverSocket.close();
					client.close();}
					
				}
			}
		} catch (Exception e1) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				Log.e("Error from CallReplyAcceptor : ", e.getLocalizedMessage());
			}
			//Log.e("Error from CallReplyAcceptor : ", e1.getLocalizedMessage());
		}
		finally{
			
		}
		baseHandler.sendMessage(baseHandler.obtainMessage(3, 4, 0, false));
		return false;
		
	}

}
