package model.layers;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import javafx.util.Duration;
import model.audio.ThreadModele;

public abstract class Layer {
	public enum LayerType {
		INDEFINI, FICHIER, ENREGISTREMENT;
	}

	private LayerType layerType;
	protected static AudioInputStream inputStream;
	protected float[][] decodedAmpTimePoints;
	protected int realTimeIndex;
	protected int decodedStreamLength;
	protected int decodedLenWithChannels;
	protected static float[] musicalContourBuffer;
	protected static int computedContourBufferLen;
	protected int fpsTimeIndex;
	protected long lastReplay;
	protected long lastPause;

	public Layer(LayerType pLayerType) {
		layerType = pLayerType;

		musicalContourBuffer = new float[1];
		lastReplay = 0;
		lastPause = 0;
	}

	public int getDecodedStreamLength() {
		return decodedStreamLength;
	}

	public LayerType getLayerType() {
		return layerType;
	}

	public void updateFpsTimeIndex() {
		fpsTimeIndex = (int) Math.floor((realTimeIndex / ThreadModele.SAMPLE_RATE) * 60);
	}

	public int getFpsTimeIndex() {
		return fpsTimeIndex;
	}

	public void computePreAverageBuffers(float[] decodedAmpTimePoints, float harmonicFrequency) {
		computedContourBufferLen = musicalContourBuffer.length;
		updateFpsTimeIndex();

		if ((fpsTimeIndex) >= computedContourBufferLen) {
			musicalContourBuffer = Arrays.copyOfRange(musicalContourBuffer, 0, computedContourBufferLen + 60 * 3); // loadCapacity
		}
		if (fpsTimeIndex < computedContourBufferLen + 60 * 3)
			musicalContourBuffer[fpsTimeIndex] = harmonicFrequency;

	}

	public abstract double getMpDuration();

	public abstract float[][] getTimePoints();

	public boolean typeEnregistrement() {
		return layerType == LayerType.ENREGISTREMENT;
	}

	public boolean typeFichier() {
		return layerType == LayerType.FICHIER;
	}

	public AudioInputStream getInputStream() {
		return inputStream;
	}

	public int getDecodedLenWithChannels() {
		return decodedLenWithChannels;
	}

	public abstract boolean loaded();

	public boolean didntReachEndOfStream() {
		return realTimeIndex + ThreadModele.SAMPLE_SIZE <= decodedStreamLength;
	}

	public float[][] getDecodedAmpTimePoints() {
		return decodedAmpTimePoints.clone();
	};

	public abstract void updateDecodedAmpTimePoints();

	public abstract void updateRealTimeIndex(long timestamp);

	public int getRealTimeIndex() {
		return realTimeIndex;
	}

	/**
	 * Decode a byte buffer from and input stream to be in a readable float format.
	 * 
	 * @param pBuffer the encrypted sound data on the time domain
	 * @param pFormat the audio format
	 * @return the decrypted sound data on the time domain
	 */
	public static float[][] getByteFloatArray(byte[] pBuffer, AudioFormat pFormat) {

		int ch = pFormat.getChannels();
		int bytesPerInt = pFormat.getSampleSizeInBits() / ThreadModele.BYTES_PER_SAMPLE;

		int s = pBuffer.length / (bytesPerInt * ch);

		int offset = -1;

		float[][] decoded = new float[ch][s];

		for (int i = 0; i < s; i++) {
			for (int j = 0; j < ch; j++) {
				offset = i * bytesPerInt * ch + bytesPerInt * j;

				if (bytesPerInt == 2) {
					if (pFormat.isBigEndian())
						decoded[j][i] = (int) ((pBuffer[offset + 1] & 0xff) | ((pBuffer[offset]) << 8));
					else
						decoded[j][i] = (int) ((pBuffer[offset] & 0xff) | ((pBuffer[offset + 1]) << 8));
				} else if (bytesPerInt == 4) {
					if (pFormat.isBigEndian())
						decoded[j][i] = ((pBuffer[offset] & 0xff) << 24) | ((pBuffer[offset + 1] & 0xff) << 16)
								| ((pBuffer[offset + 2] & 0xff) << 8) | (pBuffer[offset + 3] & 0xff);
					else
						decoded[j][i] = ((pBuffer[offset + 3] & 0xff) << 24) | ((pBuffer[offset + 2] & 0xff) << 16)
								| ((pBuffer[offset + 1] & 0xff) << 8) | (pBuffer[offset] & 0xff);
				}
			}
		}

		return decoded;
	}

	public float[] getMusicalContourBuffer() {

		return musicalContourBuffer;

	}

	public int getComputedContourBufferLen() {

		return computedContourBufferLen;
	}

	public abstract void stopSaveState();

	public abstract void dispose();

	public abstract boolean loadState();

	public abstract boolean togglePlay();

	public abstract void setVolume(double pValue);

	public abstract void setRealCurrentIndex(int pNewRealCurrentIndex);

	public long getTimeMilli(long lastNoteTime) {

		return lastReplay > 0 ? (System.currentTimeMillis() - lastNoteTime) - (lastReplay - lastPause)
				: System.currentTimeMillis() - lastNoteTime;

	}

}
