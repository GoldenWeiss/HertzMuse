package model.audio;

import javax.sound.sampled.*;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.file.TAudioFileFormat;

import controleur.ControleurC;
import controleur.DataModele;
import controleur.IControleurC;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.Obuffer;
import javazoom.jl.decoder.SampleBuffer;
import model.layers.Layer;
import model.layers.LayerEnregistrement;
import model.layers.LayerFichier;
import model.music.Measure;
import model.music.MusicFactory;
import model.music.Note;
import model.utility.CColor;
import model.utility.Complex;
import model.utility.Vector;

public class ThreadModele {
	IControleurC controleur;
	private static final String FILENAME = "TestData/TestFrequencyNote/830Hz_Ab5.wav";
	private static final String EUPHOFILENAME = "testData/testMp3File/FScale2OctaveDuck.wav";// FScale16thDuck//FScale2OctaveDuck
//FScale2OctaveDuck
	public static final int BYTES_PER_SAMPLE = 8;
	private static final boolean eupho = true;
	// This buffer size gets every frequency in the test with a margin of only 1Hz.
	public static final int SAMPLE_SIZE = 8192;
	public static final int BUFFER_SIZE = 2 * SAMPLE_SIZE; // 2 * SAMPLE_SIZE;

	public static final int MIN_FREQUENCY = 55;
	public static final int MAX_FREQUENCY = 10000;

	public static final double FPS = 60;
	public static final double FPS_INVERSE = 1 / FPS;
	public static final double SAMPLE_RATE = 44100;
	public static final double FREQUENCY_BINS = SAMPLE_RATE / SAMPLE_SIZE;
	public static final boolean AUTO_PLAY_FILE = false;
	public static final boolean AUTO_RECORD = true;

	private static MediaPlayer mp;
	private static AudioInputStream inputStream;
	private static float[][] orderedData;
	private SourceDataLine dataLine;
	private int currentFrame;
	private static float[][] frequencyBandPoints;
	private static float harmonicFrequency;
	private static float harmonicAmplitude;
	private static int harmonicIndex;
	private static int peakIndex;
	private static float peakFrequency;
	private static float peakAmplitude;
	private static int realTimeIndex;

	private MusicFactory musicFactory;
	private float instrumentHarmonicFamily;
	private AnimationTimer updater;
	private int displayedChannel;
	private int decodedStreamLength;
	private float[][] decodedStreamBuffer;

	private int decodedLenWithChannels;

	private static int fpsTimeIndex;

	private static float[] musicalContourBuffer;
	private static int computedContourBufferLen;
	private static float[] microStreamBuffer;
	private static int computedMicroBufferLen;

	private static boolean normalize;
	private static boolean substractMean;

	private static boolean useMicro;
	private static boolean loadedMedia;

	private static enum windowingTitleEnum {
		Hamming, Hann, Rectangulaire
	};

	private static windowingTitleEnum windowingTitle;

	private static enum pitchDetectionTitleEnum {
		Autocorrélation, Cepstre
	};

	private static pitchDetectionTitleEnum pitchDetectionTitle;

	private static int displayedUnitsIndex;

	public void setDisplayedUnits(int pUnitsIndex) {
		displayedUnitsIndex = pUnitsIndex;
	}

	public void setDisplayedChannel(int pSystemChannel) {
		displayedChannel = pSystemChannel;
	}

	private List<Layer> layers;
	private int currentLayerIndex;

	public ThreadModele(ControleurC pControleurC, MusicFactory pMusicFactory) {
		controleur = pControleurC;
		musicFactory = pMusicFactory;

		instrumentHarmonicFamily = 2;

		currentFrame = 0; // micro only
		windowingTitle = windowingTitleEnum.Hamming;
		pitchDetectionTitle = pitchDetectionTitleEnum.Autocorrélation;
		displayedChannel = 0;
		displayedUnitsIndex = 0;
		windowingTitle = windowingTitleEnum.Hamming;
		normalize = true;
		substractMean = true;

		loadedMedia = false;
		useMicro = false;

		musicalContourBuffer = new float[1];
		microStreamBuffer = new float[1];
		layers = new ArrayList<>();
		currentLayerIndex = -1;
		// loadMedia(new File(eupho ? EUPHOFILENAME : FILENAME));
		// loadMicro();

		updater = getTask();
		updater.start();

	}

	public void newLayer(int pType, File pFile) {

		Layer layer = null;// new LayerFichier(eupho ? EUPHOFILENAME : FILENAME);
		if (pType == 0) {
			layer = new LayerFichier(pFile);
		} else {

			try {
				layer = new LayerEnregistrement();
			} catch (IllegalArgumentException e) {

			} catch (LineUnavailableException e) {

			}
			
		}

		if (layer != null) {
			layers.add(layer);

			if (layers.size() == 1) {
				currentLayerIndex = 0;
			}
		}

	}

	public void setCurrentLayerIndex(int pIndice) {

		if (currentLayerIndex == pIndice)
			return;

		if (currentLayerIndex != -1) {
			Layer currentLayer = layers.get(currentLayerIndex);
			currentLayer.stopSaveState();
		}

		currentLayerIndex = pIndice;
		Layer currentLayer = layers.get(currentLayerIndex);
		currentLayer.loadState();

	}

	public void deleteCurrentLayer() {
		if (currentLayerIndex != -1) {
			//System.out.println(currentLayerIndex);
			//System.exit(0);
			layers.get(currentLayerIndex).dispose();

			layers.remove(currentLayerIndex);
			int s = layers.size();
			if (s != 0)
				layers.get(s != currentLayerIndex ? currentLayerIndex : --currentLayerIndex).loadState();
			else
				currentLayerIndex--;

		}
	}

	public void setCurrentLayerVolume(double pValue) {
		if (currentLayerIndex != -1) {
			Layer currentLayer = layers.get(currentLayerIndex);
			currentLayer.setVolume(pValue);
		}
	}

	public int getCurrentLayerIndex() {
		return currentLayerIndex;
	}

	public int getLayersSize() {
		return layers.size();
	}

	private AnimationTimer getTask() {

		return new AnimationTimer() {
			

			public void handle(long timestamp) {

				if (currentLayerIndex == -1)
					return;

				Layer currentLayer = layers.get(currentLayerIndex);

				if (!currentLayer.loaded())
					return;

				currentLayer.updateRealTimeIndex(timestamp);
				realTimeIndex = currentLayer.getRealTimeIndex();

				if (currentLayer.didntReachEndOfStream()) {

					currentLayer.updateDecodedAmpTimePoints();

					inputStream = currentLayer.getInputStream();
					float[][] decodedAmpTimePoints = currentLayer.getDecodedAmpTimePoints();
					int bytesPerInt = 2;// inputStream.getFormat().getSampleSizeInBits() / BYTES_PER_SAMPLE;
					int s = (2 * currentLayer.getDecodedLenWithChannels())
							/ (bytesPerInt * inputStream.getFormat().getChannels());

					applyParamTransforms(decodedAmpTimePoints, inputStream.getFormat(), s);
					float[] points = null;
					if (inputStream.getFormat().getChannels() == 1) {
						points = decodedAmpTimePoints[0];

					} else {
						if (displayedChannel < 0)
							points = IFourierFacile.anyChannelToMono(decodedAmpTimePoints);
						else
							points = decodedAmpTimePoints[displayedChannel];

					}

					getFourierTransform(points, inputStream.getFormat().getSampleRate());

					currentLayer.computePreAverageBuffers(decodedAmpTimePoints[0], harmonicFrequency);

					DataModele dataModele = new DataModele();

					dataModele.setSamplePoints(points);
					dataModele.setTimePoints(currentLayer.getTimePoints());

					dataModele.setTimePointsLength(currentLayer.getDecodedStreamLength());// useMicro ?
																							// realTimeIndex+SAMPLE_SIZE:
					// decodedStreamLength);

					dataModele.setRealTimeIndex(realTimeIndex);

					dataModele.setOrderedData(orderedData);
					dataModele.setFrequencies(frequencyBandPoints[0]);
					dataModele.setAmplitudes(frequencyBandPoints[1]);
					dataModele.setPhases(frequencyBandPoints[2]);

					dataModele.setHarmonicIndex(harmonicIndex);
					dataModele.setHarmonicFrequency(harmonicFrequency);
					dataModele.setHarmonicAmplitude(harmonicAmplitude);

					dataModele.setPeakIndex(peakIndex);
					dataModele.setPeakFrequency(peakFrequency);
					dataModele.setPeakAmplitude(peakAmplitude);
					dataModele.setInstrumentHarmonicFamily(instrumentHarmonicFamily);

					dataModele.setWindowingTitle(windowingTitle.name());
					dataModele.setPitchDetectionTitle(pitchDetectionTitle.name());
					dataModele.setUnitsIndex(displayedUnitsIndex);
					dataModele.setDisplayedChannel(displayedChannel);
					dataModele.setChannels(inputStream.getFormat().getChannels());
					dataModele.setSubstractMean(substractMean);
					dataModele.setNorm(normalize);

					dataModele.setMpDuration(currentLayer.getMpDuration());// mp.getMedia().getDuration().toMillis());

					dataModele.setMusicalContourBuffer(currentLayer.getMusicalContourBuffer());
					dataModele.setComputedContourBufferLength(currentLayer.getComputedContourBufferLen());
					dataModele.setFpsTimeIndex(currentLayer.getFpsTimeIndex());
					dataModele.setCurrentLayerIndex(currentLayerIndex);

					musicFactory.update(harmonicFrequency, currentLayer, dataModele);

					Measure lastMeasure = musicFactory.getMusicSheet().getLastActiveMeasure();
					if (lastMeasure != null) {
						Note note = lastMeasure.getNoteAt(lastMeasure.getNbrNote() - 1);
						dataModele.setLastNote(note);
					}

					controleur.ecouteurModele(dataModele);

				}

			}
		};

	}

	public void loadMedia(File f) {
		if (updater != null)
			updater.stop();
		if (mp != null)
			mp.dispose();

		Media m = new Media(f.toURI().toString());
		mp = new MediaPlayer(m);

		mp.setOnReady(() -> {

			try {
				inputStream = getFileAudioInputStream(f);
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			decodedLenWithChannels = SAMPLE_SIZE * inputStream.getFormat().getChannels();

			AudioFileFormat baseFileFormat = null;
			try {
				baseFileFormat = AudioSystem.getAudioFileFormat(f);
			} catch (UnsupportedAudioFileException e1) {

				e1.printStackTrace();
			} catch (IOException e1) {

				e1.printStackTrace();
			}

			if (baseFileFormat instanceof TAudioFileFormat) {

				decodedStreamLength = (int) Math
						.ceil(mp.getMedia().getDuration().toSeconds() * inputStream.getFormat().getSampleRate());
				Decoder d = new Decoder();
				Bitstream bit = new Bitstream(inputStream);

				Header a = null;
				short[] t;
				SampleBuffer p;
				int pos = 0, postemp = 0;
				try {

					decodedStreamBuffer = new float[inputStream.getFormat().getChannels()][(int) (decodedStreamLength)];
					while ((a = bit.readFrame()) != null) {
						p = ((SampleBuffer) d.decodeFrame(a, bit));
						t = p.getBuffer();

						for (int i = 0, w = p.getBufferLength() / 2; i < w; i++) {
							if ((postemp = pos + i) < decodedStreamLength) {
								decodedStreamBuffer[0][postemp] = (float) t[2 * i];
								decodedStreamBuffer[1][postemp] = (float) t[2 * i + 1];
							}
						}
						pos += p.getBufferLength() / 2;
						bit.closeFrame();
					}
				} catch (DecoderException e) {

					e.printStackTrace();
				} catch (BitstreamException e) {

					e.printStackTrace();
				}

			} else {
				decodedStreamLength = (int) (baseFileFormat.getFrameLength());
				byte[] streamBuffer = new byte[(int) (decodedStreamLength * 2 * inputStream.getFormat().getChannels())];
				try {
					inputStream.read(streamBuffer);
				} catch (IOException e) {

					e.printStackTrace();
				}

				decodedStreamBuffer = getByteFloatArray(streamBuffer, inputStream.getFormat());
			}

			mp.play();

			loadedMedia = true;
			updater = getTask();
			updater.start();
		});

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
		int bytesPerInt = pFormat.getSampleSizeInBits() / BYTES_PER_SAMPLE;

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

	private static void applyParamTransforms(float[][] decoded, AudioFormat pFormat, int s) {
		if (substractMean)
			for (int i = 0; i < pFormat.getChannels(); i++) {
				float mean = Vector.mean(decoded[i]);
				for (int j = 0; j < s; j++)
					decoded[i][j] -= mean;
			}

		switch (windowingTitle) {
		case Hamming:
		case Hann:
			float a0 = windowingTitle == windowingTitleEnum.Hamming ? 25 / 46f : 0.5f, a1 = 1 - a0;
			for (int i = 0; i < pFormat.getChannels(); i++) {
				decoded[i] = Vector.add(decoded[i], -Vector.mean(decoded[i]));
				for (int j = 0; j < s; j++)
					decoded[i][j] *= a0 - a1 * (float) Math.cos(2 * Math.PI * j / s);

			}
			break;
		case Rectangulaire:
		default:
			break;
		}

		if (normalize)
			for (int i = 0; i < pFormat.getChannels(); i++)
				for (int j = 0; j < s; j++)
					decoded[i][j] /= s;
	}

	public void setWindowFunction(String pSystemTitle) {
		windowingTitle = windowingTitleEnum.valueOf(pSystemTitle);
	}

	private static void computePreAverageBuffers(float[] decodedAmpTimePoints) {
		computedContourBufferLen = musicalContourBuffer.length;
		if ((fpsTimeIndex = (int) Math.floor((realTimeIndex / SAMPLE_RATE) * 60)) >= computedContourBufferLen) {
			musicalContourBuffer = Arrays.copyOfRange(musicalContourBuffer, 0, computedContourBufferLen + 60 * 3); // loadCapacity
		}
		musicalContourBuffer[fpsTimeIndex] = harmonicFrequency;

		if (useMicro) {
			computedMicroBufferLen = microStreamBuffer.length;

			if (realTimeIndex + SAMPLE_SIZE >= computedMicroBufferLen) {
				microStreamBuffer = Arrays.copyOfRange(microStreamBuffer, 0,
						computedMicroBufferLen + (int) SAMPLE_RATE * 3 + SAMPLE_SIZE);
			}

			for (int i = 0; i < SAMPLE_SIZE; i++) {
				microStreamBuffer[realTimeIndex + i] = decodedAmpTimePoints[i];
			}
		}
	}

	public static void getFourierTransform(float[] points, float sampleRate) {

		// Transform float array into Complex array
		int s = points.length;
		Complex[] complexBuffer = new Complex[s];
		for (int i = 0; i < s; i++)
			complexBuffer[i] = new Complex(points[i], 0);
		complexBuffer = IFourierFacile.fft(complexBuffer);
		frequencyBandPoints = new float[3][s / 2];
		if (!normalize)
			for (int i = 1; i < s / 2; i++) {
				frequencyBandPoints[0][i] = i * sampleRate / s;
				frequencyBandPoints[1][i] = complexBuffer[i].getMagnitude() / s * 2; // /20;
				frequencyBandPoints[2][i] = complexBuffer[i].getPhase();
			}
		else
			for (int i = 1; i < s / 2; i++) {
				frequencyBandPoints[0][i] = i * sampleRate / s;
				frequencyBandPoints[1][i] = complexBuffer[i].getMagnitude() * 2; // /20;
				frequencyBandPoints[2][i] = complexBuffer[i].getPhase();
			}

		// peak detection
		float v = 0;
		float max = Float.MIN_VALUE;
		int index = -1;
		for (int i = 0; i < s / 2; i++) {
			v = complexBuffer[i].getMagnitude();
			if (v > max) {
				max = v;
				index = i;
			}
		}
		if (index == -1)
			return;
		peakIndex = index == -1 ? 0 : index;

		float fBands = sampleRate / s;
		peakFrequency = fBands * IFourierFacile.centroidPeak(peakIndex, s / 2, frequencyBandPoints[1]);// frequencyBandPoints[0][index];

		peakAmplitude = frequencyBandPoints[1][peakIndex];

		harmonicIndex = (int) Math.floor(pitchDetectionTitle == pitchDetectionTitleEnum.Autocorrélation
				? IFourierFacile.autocorrelation(58 - 1, 5 * 466 + 1, complexBuffer)// IFourierFacile.autocorrelation(58
																					// - 1, 5 * 466 + 1, points)//
				: IFourierFacile.cepstrum(20, 5 * 466, complexBuffer));

		harmonicFrequency = fBands * IFourierFacile.centroidPeak(harmonicIndex, s / 2, frequencyBandPoints[1]);// harmonicIndex
																												// *
																												// sampleRate
																												// / s;
		harmonicAmplitude = frequencyBandPoints[1][harmonicIndex];
	}
//TODO
	public void setRealCurrentIndex(int pNewRealCurrentIndex, boolean isPrimaryButton, boolean isDragging) {
		
		Layer currentLayer = layers.get(currentLayerIndex);
		currentLayer.setRealCurrentIndex(pNewRealCurrentIndex);
		
		

	}

	public void setPitchDetectionFunction(String pSystemTitle) {
		pitchDetectionTitle = pitchDetectionTitleEnum.valueOf(pSystemTitle);
	}

	private AudioInputStream getFileAudioInputStream(File f) throws UnsupportedAudioFileException, IOException {
		AudioInputStream in = null;

		// FileInputStream fs = new FileInputStream(f);
		// InputStream bufferedIn = new BufferedInputStream(fs);
		in = AudioSystem.getAudioInputStream(f);

		AudioInputStream din = null;
		AudioFormat baseFormat = in.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float) SAMPLE_RATE, 16,
				baseFormat.getChannels(), baseFormat.getChannels() * 2, (float) SAMPLE_RATE, false);
		din = AudioSystem.getAudioInputStream(decodedFormat, in);

		return in;
	}

	private AudioInputStream getMicroAudioInputStream() throws LineUnavailableException {
		AudioInputStream audioInputStream = null;

		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float) SAMPLE_RATE, 16, 2, 4,
				(float) SAMPLE_RATE, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
		TargetDataLine targetLine = null;

		targetLine = (TargetDataLine) AudioSystem.getLine(info);
		targetLine.open();

		targetLine.start();

		// dataLine = targetLine;
		audioInputStream = new AudioInputStream(targetLine);
		return audioInputStream;
	}

	public boolean togglePlay() {
		Layer currentLayer = layers.get(currentLayerIndex);

		return currentLayer.togglePlay();

	}

	public void toggleCentrage() {
		substractMean = !substractMean;
	}

	public void toggleNorm() {
		normalize = !normalize;
	}

}
