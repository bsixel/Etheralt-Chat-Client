package tools;

import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import client.Client;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;



/*
 * 
 * @author Ben Sixel
 *   Copyright 2015 Benjamin Sixel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

public class AudioHandler {

	private static final long RECORD_TIME = 15000;
	private File soundFile = new File(System.getProperty("user.home") + "/Documents/Etheralt Chat Client/RecordAudio.wav");
	private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
	private TargetDataLine mic;
	private byte[] audioBuffer = new byte[10000];
	//private Client client;
	private Thread voice;

	/**
	 * Default contructor for and AudioHandler. Takes in a client to use for incoming/outgoing streams.
	 * @param client Client to use for incoming/outgoing streams.
	 */
	public AudioHandler(Client client) {
		//this.client = client;
	}

	/**
	 * A method used for getting a workable audio format for recording.
	 * @return An AudioFormat used for recording.
	 */
	public static AudioFormat getAudioFormat() {
		float sampleRate = 16000;
		int sampleSizeInBits = 8;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = true;
		AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		return format;
	}

	/**
	 * Plays an audio file on the client side.
	 * @param path The path to the audio file.
	 */
	public void playFile(String path) {
		/*try {

		} catch (Exception e) {
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		}*/
		try{
			Media hit = new Media(path);
			MediaPlayer mediaPlayer = new MediaPlayer(hit);
			mediaPlayer.play();
		}
		catch(Exception ex) {
			//FileHandler.debugPrint(ex);
			ex.printStackTrace();
		}
	}

	/**
	 * Starts the recording process.
	 * @param record A boolean for whether or not the audio should be saved to the system.
	 */
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
			mic.read(audioBuffer, 0, (int) RECORD_TIME);


			voice = new Thread(() -> {
				while (true) {
					try {
						//this.client.getVoiceOutData().write(audioBuffer);
					} catch (Exception e) {
						System.out.println("Voice chat failed: unable to send voice bytes.");
						FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
					}
				}
			});
			voice.setDaemon(true);
			voice.start();

			System.out.println("Started recording.");



			if (record) {
				AudioSystem.write(ais, fileType, soundFile);
			}

		} catch (Exception e) {
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		}
	}

	/**
	 * Stops the recording process.
	 */
	public void stop() {
		mic.stop();
		mic.close();
		System.out.println("Stopped recording.");
	}
}