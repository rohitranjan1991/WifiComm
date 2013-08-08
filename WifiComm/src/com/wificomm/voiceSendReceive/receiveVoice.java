package com.wificomm.voiceSendReceive;

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
				isReceiving = false;

		}
	};
	private Handler baseHandler;
	private AudioTrack at;
	private boolean isReceiving = true;
	private AudioManager am;

	public receiveVoice(Handler MainHandler, AudioManager am) {

		this.baseHandler = MainHandler;
		this.am=am;
		MainHandler.sendMessage(MainHandler.obtainMessage(0, 5, 0, mHandle));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		receiveAndPlayVoice();
		super.run();
	}

	public void receiveAndPlayVoice() {
		//am.setMode(AudioManager.MODE_IN_CALL);

		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(7689);
			byte[] buffer = new byte[intSize];
			at = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE,
					RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, intSize,
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

					}
				} catch (Exception e) {
					isReceiving = false;
					at.release();
					
				}
				
			} 
			
			//am.setMode(AudioManager.MODE_NORMAL);
			
			if (!socket.isClosed()) {
				socket.close();
				socket = null;
			}

			at.release();
		} catch (Exception e) {
			if (!socket.isClosed()) {
				socket.close();
				socket = null;
			}
			e.printStackTrace();
		}
		baseHandler.sendMessage(baseHandler
				.obtainMessage(4, 1, 0, "Disconnect"));

	}

}
