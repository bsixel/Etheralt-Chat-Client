package tools;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import client.Client;



public class AudioHandler {
	
	private static final long RECORD_TIME = 15000;
	private File soundFile = new File(System.getProperty("user.home") + "/Documents/Etheralt Chat Client/RecordAudio.wav");
	private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
	private TargetDataLine mic;
	private byte[] audioBuffer = new byte[10000];
	private Client client;
	
	public AudioHandler(Client client) {
		this.client = client;
	}

	AudioFormat getAudioFormat() {
		float sampleRate = 16000;
		int sampleSizeInBits = 8;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = true;
		AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
				channels, signed, bigEndian);
		return format;
	}

	public void start(boolean record) {
		try {
			AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 2, 4, 44100.0F, false);
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

			if (!AudioSystem.isLineSupported(info)) {
				System.out.println("Line not supported");
				return;
			}
			mic = (TargetDataLine) AudioSystem.getLine(info);
			mic.open(format);
			mic.start();

			AudioInputStream ais = new AudioInputStream(mic);
			mic.read(audioBuffer, 0, 10000);
			
			
			Thread voice = new Thread(() -> {
				while (true) {
					try {
						this.client.getVoiceOutData().write(audioBuffer);
					} catch (Exception e) {
						System.out.println("Voice chat failed: unable to send voice bytes.");
						e.printStackTrace();
					}
				}
			});
			voice.setDaemon(true);
			voice.start();
			
			System.out.println("Started recording.");
			
			
			
			if (record) {
				AudioSystem.write(ais, fileType, soundFile);
			}

		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void stop() {
		mic.stop();
		mic.close();
		System.out.println("Stopped recording.");
	}
}