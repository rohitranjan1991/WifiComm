package com.example.wificomm;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class DataSend extends Thread {

	private static String mFileName = null;

	// Audio Configuration.

	// /////////////////////
	// new Inputs
	int buffersize;
	int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we
									// use only 1024
	int BytesPerElement = 2; // 2 bytes in 16bit format

	// private static final int RECORDER_SAMPLERATE = 11025;
	private static final int RECORDER_SAMPLERATE = 16000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private AudioRecord recorder = null;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	DatagramSocket dsocket = null;
	String ip = null;
	// ///////////////////////////////////////////////////////////////
	private Handler mainHandler;
	HandlerThread handle = new HandlerThread("My Thread");
	String purpose;

	// ////////////////////////////////////////////////////////////////////////////////
	public void disconnect() {
		isRecording = false;
	}

	public Handler getHandle() {
		return mHandle;
	}

	public DataSend(Handler handle, String IP, String purpose) {
		this.mainHandler = handle;
		this.ip = IP;
		this.purpose = purpose;
		Message msg1 = mainHandler.obtainMessage(0, 2, 0, mHandle);
		mainHandler.sendMessage(msg1);
	}

	final Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj.toString().contentEquals("Stop"))
				isRecording = false;
			if (msg.obj.toString().contentEquals("Disconnect"))
				isRecording = false;
		}
	};

	private String report;

	@Override
	public void run() {

		super.run();
		// Socket socket = new Socket();
		
		try {
			dsocket = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			/**
			 * Create a client socket with the host, port, and timeout
			 * information.
			 */
			// socket.bind(null);
			// ////////////////////////////////////////////////////////////////////////////////////////////////
			if (purpose.contentEquals("Initiate Call !!"))
			// ////////////////////////////////initiating
			// call/////////////////////////////////
			{
				InitiateCall();
			}
			else if(purpose.contentEquals("Reply False"))
			{
				reply(false);
			}
			else if(purpose.contentEquals("Reply True"))
			{
				reply(true);
			}
			// ////////////////////////////////initiating
			// Done/////////////////////////////////

			// ////////////////////////////////Main
			// Calling/////////////////////////////////
			else if (purpose.contentEquals("Main Call")) {
				
		}
		
	} catch (Exception e) {
		e.printStackTrace();
		recorder.release();
		recorder=null;
	}
	/**
	 * Clean up any open sockets when done transferring or if an exception
	 * occurred.
	 */
	finally {
		if (dsocket != null) {
			if (dsocket.isConnected()) {
				dsocket.close();
			}
		}
		
	}}
	
//initial call
	
	public void InitiateCall()
	{

		Socket socket = new Socket();
		PrintWriter out = null;

		try {
			
			/**
			 * Create a client socket with the host, port, and timeout
			 * information.
			 */
			socket.bind(null);

			socket.connect((new InetSocketAddress(ip, 7680)), 2000);

			/**
			 * Create a byte stream from a JPEG file and pipe it to the
			 * output stream of the socket. This data will be retrieved
			 * by the server device.
			 */

			out = new PrintWriter(socket.getOutputStream(), true);
			out.println("Initiate Call!!");
			report = "Call Request Sent";

		} catch (Exception e) {
			report = "Call Request Not Sent";
			e.printStackTrace();

		}

		/**
		 * Clean up any open sockets when done transferring or if an
		 * exception occurred.
		 */
		finally {
			/*if (report.contentEquals("Connection Succesful")) {
				mainHandler.sendMessage(mainHandler.obtainMessage(4,
						"inform receiver"));
			}
			else
				if (report.contentEquals("Connection Unsuccesful")) {
					mainHandler.sendMessage(mainHandler.obtainMessage(4,
							"inform receiver"));
				}*/
			mainHandler.sendMessage(mainHandler
					.obtainMessage(3,2,0, report));

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
	//Reply for Incoming call
	
	public void reply(Boolean reply)
	{
		Socket socket = new Socket();
		PrintWriter out = null;

		try {
			
			/**
			 * Create a client socket with the host, port, and timeout
			 * information.
			 */
			socket.bind(null);

			socket.connect((new InetSocketAddress(ip, 7690)), 2000);

			/**
			 * Create a byte stream from a JPEG file and pipe it to the
			 * output stream of the socket. This data will be retrieved
			 * by the server device.
			 */

			out = new PrintWriter(socket.getOutputStream(), true);
			out.println("Reply "+(reply?"true":"false"));
			report = "Call Request Send";

		} catch (Exception e) {
			report = "Call Request Not Sent";
			e.printStackTrace();

		}

		/**
		 * Clean up any open sockets when done transferring or if an
		 * exception occurred.
		 */
		finally {
			
			mainHandler.sendMessage(mainHandler
					.obtainMessage(3,1,0, report));

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
	
	//send main Call
	
	public void sendVoice()
	{
		
		try{
		DatagramPacket packet;
		//Socket socket = new Socket();
		final InetAddress destination = InetAddress.getByName(ip);


		isRecording = true;
		int bufferSize = 8192;
		// AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, bufferSize);
		

		short sData[] = new short[bufferSize];
		recorder.startRecording();
		isRecording = true;
		//dsocket.setSoTimeout(3000);
		
			while (isRecording) {
				recorder.read(sData, 0, bufferSize);
				try {
					// // writes the data to file from buffer
					// // stores the voice buffer

					byte bData[] = short2byte(sData);
					packet = new DatagramPacket(bData, bufferSize,
							destination, 7689);// 
					Thread.sleep(10);
					dsocket.send(packet);
					
					

					// os.write(bData, 0, BufferElements2Rec *
					// BytesPerElement);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		
			recorder.release();
			recorder=null;
		// out.println("Finally !!!");
		}
		catch(Exception e)
		{}

	// Thread.currentThread().interrupt();	
	}

	// ////////////////////////////////call
	// completed/////////////////////////////////
	// // //convert short to byte
	private byte[] short2byte(short[] sData) {
		int shortArrsize = sData.length;
		byte[] bytes = new byte[shortArrsize * 2];
		for (int i = 0; i < shortArrsize; i++) {
			bytes[i * 2] = (byte) (sData[i] & 0x00FF);
			bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
			sData[i] = 0;
		}
		return bytes;

	}

	// ////initial settings
	private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };

	public AudioRecord findAudioRecord() {
		for (int rate : mSampleRates) {
			for (short audioFormat : new short[] {
					AudioFormat.ENCODING_PCM_8BIT,
					AudioFormat.ENCODING_PCM_16BIT }) {
				for (short channelConfig : new short[] {
						AudioFormat.CHANNEL_IN_MONO,
						AudioFormat.CHANNEL_IN_STEREO,
						AudioFormat.CHANNEL_CONFIGURATION_DEFAULT,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.CHANNEL_CONFIGURATION_STEREO }) {
					try {
						// Log.d(C.TAG, "Attempting rate " + rate + "Hz, bits: "
						// + audioFormat + ", channel: "
						// + channelConfig);
						int bufferSize = AudioRecord.getMinBufferSize(rate,
								channelConfig, audioFormat);

						if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
							// check if we can instantiate and have a success
							AudioRecord recorder = new AudioRecord(
									AudioSource.DEFAULT, rate, channelConfig,
									audioFormat, bufferSize);

							if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
								return recorder;
						}
					} catch (Exception e) {
						// Log.e(C.TAG, rate + "Exception, keep trying.",e);
					}
				}
			}
		}
		return null;
	}

}
