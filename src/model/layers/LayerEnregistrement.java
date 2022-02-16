package model.layers;

import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.audio.ThreadModele;

public class LayerEnregistrement extends Layer {

	private boolean useMicro, recording;
	private long st;
	private float[] microStreamBuffer;
	private int computedMicroBufferLen;
	private TargetDataLine targetLine;
	
	private static boolean CAN_MOVE_RECORDING = false;

	public LayerEnregistrement() throws LineUnavailableException {
		super(LayerType.ENREGISTREMENT);
		microStreamBuffer = new float[1];
		st = 0;
		useMicro = false;
		recording = true;

		loadMicro();

	}

	public boolean loaded() {
		return useMicro;
	}

	@Override
	public void updateDecodedAmpTimePoints() {

		if (!targetLine.isOpen())
			return;
		
		int bs = inputStream.getFormat().getChannels() * ThreadModele.BUFFER_SIZE ; 
		byte[] buffer = new byte[bs];
		try {
			inputStream.read(buffer, 0, bs);
		} catch (IOException e) {

			e.printStackTrace();
		}

		decodedAmpTimePoints = getByteFloatArray(buffer, inputStream.getFormat());
	}

	public void loadMicro() throws LineUnavailableException {
		useMicro = false;

		try {
			inputStream = getMicroAudioInputStream();
			decodedLenWithChannels = ThreadModele.SAMPLE_SIZE * inputStream.getFormat().getChannels();
			useMicro = true;
		} catch (IllegalArgumentException e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Erreur");
			alert.setContentText("Pas d'entrée microphone détectée!");
			alert.setHeaderText(null);

			alert.showAndWait();

			throw e;
		} catch (LineUnavailableException e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Erreur");
			alert.setContentText("Pas d'entrée microphone détectée!");
			alert.setHeaderText(null);

			alert.showAndWait();
			throw e;
		}

	}

	private AudioInputStream getMicroAudioInputStream() throws LineUnavailableException {
		AudioInputStream audioInputStream = null;

		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float) ThreadModele.SAMPLE_RATE, 16,
				2, 4, (float) ThreadModele.SAMPLE_RATE, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
		targetLine = null;

		targetLine = (TargetDataLine) AudioSystem.getLine(info);

		targetLine.open();

		targetLine.start();

		audioInputStream = new AudioInputStream(targetLine);
		return audioInputStream;
	}

	@Override
	public void updateRealTimeIndex(long timestamp) {
		if (targetLine.isOpen())
			realTimeIndex += (int) (ThreadModele.SAMPLE_RATE
					* (st > 1 ? (timestamp - st) / 10e9 : ThreadModele.FPS_INVERSE));

		st = timestamp;

		if (recording)
			decodedStreamLength = realTimeIndex + ThreadModele.SAMPLE_SIZE;
	}

	@Override
	public double getMpDuration() {
		return decodedStreamLength / ThreadModele.SAMPLE_RATE * 10000d;
	}

	public void computePreAverageBuffers(float[] decodedAmpTimePoints, float harmonicFrequency) {
		super.computePreAverageBuffers(decodedAmpTimePoints, harmonicFrequency);

		computedMicroBufferLen = microStreamBuffer.length;

		if (realTimeIndex + ThreadModele.SAMPLE_SIZE >= computedMicroBufferLen) {
			microStreamBuffer = Arrays.copyOfRange(microStreamBuffer, 0,
					computedMicroBufferLen + (int) ThreadModele.SAMPLE_RATE * 3 + ThreadModele.SAMPLE_SIZE);
		}

		for (int i = 0; i < ThreadModele.SAMPLE_SIZE; i++) {
			microStreamBuffer[realTimeIndex + i] = decodedAmpTimePoints[i];
		}

	}

	@Override
	public float[][] getTimePoints() {
		return new float[][] { microStreamBuffer, microStreamBuffer };
	}

	@Override
	public void stopSaveState() {

		targetLine.stop();
		targetLine.close();
	}

	@Override
	public void dispose() {
		stopSaveState();

	}

	@Override
	public boolean loadState() {

		try {
			loadMicro();
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	@Override
	public boolean togglePlay() {

		if (targetLine.isOpen()) {
			lastPause = System.currentTimeMillis();
			lastReplay = 0;
			
			stopSaveState();

			return true;
		} else {
			
			if (lastPause > 0)
				lastReplay = System.currentTimeMillis();
			
			return loadState();

		}
	}

	@Override
	public void setVolume(double pValue) {

	}

	@Override
	public void setRealCurrentIndex(int pNewRealCurrentIndex) {
		if (!CAN_MOVE_RECORDING)
			return;

		int formatIndex = Math.max(0, Math.min(decodedStreamLength - 1, pNewRealCurrentIndex));
		double d = formatIndex / ThreadModele.SAMPLE_RATE * 10000d;
		realTimeIndex = formatIndex;
		microStreamBuffer = Arrays.copyOfRange(microStreamBuffer, 0, formatIndex);
		musicalContourBuffer = Arrays.copyOfRange(musicalContourBuffer, 0, (int) Math.floor((d / 1000) * 60));
	}

	
	

}
