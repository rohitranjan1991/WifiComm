package com.wificomm.deviceScan;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class wifiScanSend extends Thread {

	private String message;
	private Handler mainHandler;
	private String initAddress;
	private String report;
	private String msgToReport;
	private String successMessage;
	private String failureMessage;

	public Handler getHandle() {
		return thisHandler;
	}

	public wifiScanSend(Handler handle, String msg,String initAddress,String successMessage,String failureMessage) {
		this.mainHandler = handle;
		this.message = msg;
		this.initAddress=initAddress;
		this.successMessage=successMessage;
		this.failureMessage=failureMessage;
	}

	final static Handler thisHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// if(msg.obj.toString().contentEquals("Stop"))
		}
	};

	@Override
	public void run() {

		super.run();
		Socket socket = new Socket();
		PrintWriter out = null;
		// BufferedReader in = null;

		try {Thread.sleep(10);
			/**
			 * Create a client socket with the host, port, and timeout
			 * information.
			 */
			socket.bind(null);

			socket.connect((new InetSocketAddress(initAddress, 9071)), 1000);

			/**
			 * Create a byte stream from a JPEG file and pipe it to the output
			 * stream of the socket. This data will be retrieved by the server
			 * device.
			 */

			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(message);
			report=successMessage;
			
			

		} catch (Exception e) {
			report=failureMessage;
			e.printStackTrace();

		}

		/**
		 * Clean up any open sockets when done transferring or if an exception
		 * occurred.
		 */
		finally {
			Message msg = mainHandler.obtainMessage(0, report);
		    mainHandler.sendMessage(msg);
			if (socket != null) {
				if (socket.isConnected()) {
					try {
						socket.close();
					} catch (IOException e) {
						// catch logic
					}
				}
			}
		}
	}

}
