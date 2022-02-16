package model.layers;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.file.TAudioFileFormat;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import model.audio.ThreadModele;

public class LayerFichier extends Layer {

	MediaPlayer mp;

	private float[][] decodedStreamBuffer;

	private boolean loadedMedia;

	private boolean savedPlayingState;

	
	public LayerFichier(String pFilename) {
		super(LayerType.FICHIER);

		savedPlayingState = false;
		mp = null;
		loadedMedia = false;

		lastPause = 0;
		lastReplay = 0;
		
		loadMedia(pFilename);

	}

	public LayerFichier(File pFile) {
		super(LayerType.FICHIER);

		savedPlayingState = false;
		mp = null;
		loadedMedia = false;

		loadMedia(pFile);
	}

	public boolean loaded() {
		return loadedMedia;
	}

	public void updateDecodedAmpTimePoints() {
		decodedAmpTimePoints = new float[inputStream.getFormat().getChannels()][ThreadModele.SAMPLE_SIZE];
		for (int i = 0; i < inputStream.getFormat().getChannels(); i++)
			decodedAmpTimePoints[i] = Arrays.copyOfRange(decodedStreamBuffer[i], realTimeIndex,
					realTimeIndex + ThreadModele.SAMPLE_SIZE);
	}

	public void updateRealTimeIndex(long timestamp) {
		BigDecimal a = new BigDecimal(Double.toString(mp.getCurrentTime().toMillis()));
		BigDecimal b = new BigDecimal(Double.toString(mp.getMedia().getDuration().toMillis()));
		BigDecimal c = new BigDecimal(Integer.toString(decodedStreamLength));
		realTimeIndex = ((a.divide(b, MathContext.DECIMAL128).multiply(c)).intValue());

	}

	private void loadMedia(String pFilename) {
		loadMedia(new File(pFilename));
	}

	public void loadMedia(File f) {
		loadedMedia = false;

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

			decodedLenWithChannels = ThreadModele.SAMPLE_SIZE * inputStream.getFormat().getChannels();

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
				int pos = 0, postemp;
				try {

					decodedStreamBuffer = new float[inputStream.getFormat().getChannels()][(int) (decodedStreamLength)];
					while ((a = bit.readFrame()) != null) {
						p = ((SampleBuffer) d.decodeFrame(a, bit));
						t = p.getBuffer();

						for (int i = 0, w = p.getBufferLength() / 2; i < w; i++) {
							if ((postemp = pos + i) < decodedStreamLength) {
								decodedStreamBuffer[0][pos + i] = (float) t[2 * i];
								decodedStreamBuffer[1][pos + i] = (float) t[2 * i + 1];
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

			decodedAmpTimePoints = new float[inputStream.getFormat().getChannels()][ThreadModele.SAMPLE_SIZE];
			loadedMedia = true;
			if (ThreadModele.AUTO_PLAY_FILE)
				mp.play();
		});
		
			
		
			
	}

	private AudioInputStream getFileAudioInputStream(File f) throws UnsupportedAudioFileException, IOException {
		AudioInputStream in = AudioSystem.getAudioInputStream(f);
		AudioInputStream din = null;
		AudioFormat baseFormat = in.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float) ThreadModele.SAMPLE_RATE,
				16, baseFormat.getChannels(), baseFormat.getChannels() * 2, (float) ThreadModele.SAMPLE_RATE, false);
		din = AudioSystem.getAudioInputStream(decodedFormat, in);

		return in;
	}

	@Override
	public double getMpDuration() {
		return decodedStreamLength / ThreadModele.SAMPLE_RATE * 1000;
	}

	@Override
	public float[][] getTimePoints() {
		return decodedStreamBuffer;
	}

	@Override
	public void stopSaveState() {
		savedPlayingState = (mp.getStatus() == MediaPlayer.Status.PLAYING);

		mp.pause();
	}

	@Override
	public void dispose() {
		stopSaveState();
		mp.dispose();
	}

	@Override
	public boolean loadState() {

		if (savedPlayingState)
			mp.play();

		return true;
	}

	@Override
	public boolean togglePlay() {
		switch (mp.getStatus()) {
		case READY:
		case PAUSED:
		case STOPPED:
		case STALLED:
		case HALTED:
		case UNKNOWN:
		case DISPOSED:
			if (lastPause > 0)
				lastReplay = System.currentTimeMillis();
			
			mp.play();
			break;
		case PLAYING:
			lastPause = System.currentTimeMillis();
			lastReplay = 0; 
			
			mp.pause();
			
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void setVolume(double pValue) {
		mp.setVolume(pValue / 100d);
	}

	@Override
	public void setRealCurrentIndex(int pNewRealCurrentIndex) {
		int formatIndex = Math.max(0, Math.min(decodedStreamLength - 1, pNewRealCurrentIndex));
		double d = formatIndex * mp.getMedia().getDuration().toMillis() / decodedStreamLength;
		mp.seek(Duration.millis(d));
		musicalContourBuffer = Arrays.copyOfRange(musicalContourBuffer, 0, (int) Math.floor((d / 1000) * 60));

	}

	

}
