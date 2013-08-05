package com.example.wificomm;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;

public class receiveVoice extends Thread {

	private static final int RECORDER_SAMPLERATE = 16000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_OUT_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	int intSize = 8192;
	
	
	
	final Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj.toString().contentEquals("Stop"))
				isReceiving=false;
			
		}
	};
	private Handler MainHandler;
	private AudioTrack at;
	private boolean isReceiving=true;

	public receiveVoice(Handler MainHandler) {
	
		this.MainHandler=MainHandler;
		
		
		MainHandler.sendMessage(MainHandler.obtainMessage(0,5,0,mHandle));
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
	}
	public void ReceiveAndPlayVoice()
	{
		
		DatagramSocket socket = null ;
		try{
			socket = new DatagramSocket(7689);
		byte[] buffer = new byte[intSize];
		at = new AudioTrack(AudioManager.STREAM_MUSIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, intSize,
				AudioTrack.MODE_STREAM);

		
		isReceiving = true;
		socket.setSoTimeout(2000);

		while (isReceiving) {

			DatagramPacket packet = new DatagramPacket(buffer,
					buffer.length);
			try {
				socket.receive(packet);
				buffer = packet.getData();
				if (at != null) {
					at.play();
					at.write(buffer, 0, buffer.length);
					at.stop();

				}// end inner if
			} catch (Exception e) {
				isReceiving = false;
				at.release();
				// audioManager.setMode(AudioManager.MODE_RINGTONE);
			}
			// end of inner buff
		} // end inner while
		
		socket.close();
		socket = null;// end if
		// at.release();

	
		
		
		at.release();
		}
		catch(Exception e)
		{}
		MainHandler.sendMessage(MainHandler.obtainMessage(4,0,0, "Disconnect"));
		socket.close();
		
		
		
	}
	
}
