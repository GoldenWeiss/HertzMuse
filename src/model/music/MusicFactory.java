package model.music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.print.attribute.standard.PDLOverrideSupported;

import controleur.DataModele;
import controleur.IControleurC;
import model.audio.IFourierFacile;
import model.audio.ThreadModele;
import model.layers.Layer;
import model.utility.Complex;
import model.utility.Fraction;
import model.utility.Vector;

public class MusicFactory {
	public static final int RAW_AUDIO_DEPTH = 10;

	public static final int CONSECUTIVE_FREQ_TRESHOLD = 5;

	public static final int LIMIT_LENGHT = 8;

	public static final int TONGUING_PEAKS = 8;

	private int tempo;
	private int beat;
	private int consecutiveNewFreq;
	private float currentFreq;
	private long lastNoteTime;
	private MusicSheet currentSheet;

	private float[] contourPoints;

	private int contourIndex;

	private int contourLength;

	private boolean tonguingAnalysis;

	private int frequency0;

	private int frequency1;

	private IControleurC controleur;

	private boolean detectTonguing;

	public MusicFactory(IControleurC pControleur, int tempo, int beat, int time) {
		this.controleur = pControleur;
		this.tempo = tempo;
		this.detectTonguing = false;
		this.beat = beat;
		consecutiveNewFreq = 0;
		currentFreq = -1;
		lastNoteTime = 0;
		currentSheet = new MusicSheet(beat, time);

	}

	public void setDetectTonguing(boolean pDetectTonguing) {
		detectTonguing = pDetectTonguing;
	}
	public boolean getDetectTonguing() {
		return detectTonguing;
	}
	public void setTempo(int tempo) {
		this.tempo = tempo;
	}

	public int getTempo() {
		return tempo;
	}

	public MusicSheet getMusicSheet() {
		return currentSheet;
	}

	private void updateDataModele(DataModele pDataModele) {
		contourPoints = pDataModele.getMusicalContourBuffer();
		contourIndex = pDataModele.getFpsTimeIndex();
		contourLength = pDataModele.getComputedContourBufferLength();
	}

	public void update(float freq, Layer currentLayer, DataModele pDataModele) {
		
		updateDataModele(pDataModele);
		boolean changeSilence = changeSilence();
		boolean freqChange = freqChange(freq);

		if (changeSilence&&freqChange) {
			frequency1 = contourIndex - 1;

			int nTonguing = detectTonguing && changeSilence ? tonguingAnalysis() : 0;

			long timeMili = currentLayer.getTimeMilli(lastNoteTime); 
			float beat =	((timeMili/60000f)*tempo)/this.beat;
			Fraction newBeat = Fraction.roundToNearestPw2Fract(beat, LIMIT_LENGHT);

			if (newBeat.compareTo(new Fraction(1, 32)) >= 0 & contourPoints[frequency1]!=0) {
				currentSheet.addNote(new Note(newBeat, (int) contourPoints[frequency1], nTonguing));
				controleur.ecouteurAjouterNote();
			}
			currentFreq = -1;
			lastNoteTime = System.currentTimeMillis();
		}
		
	}

	

	private int tonguingAnalysis() {
		
		int d = Math.abs(frequency1 - frequency0);
		if (d < 3)
			return 0; // early exit
		float[] vec = new float[d + 1];
		int lasti = 0;
		for (int i = 0; i <= d; i++) {
			float b = contourPoints[frequency0 + i] > 1 ? contourPoints[frequency0 + i]
					: i == 0 ? contourPoints[frequency0 + i + 1]
							: i == d ? contourPoints[frequency0 + i - 1]
									: frequency0 + i - 1 > contourLength ? vec[lasti] : (contourPoints[frequency0 + i - 1] + contourPoints[frequency0 + i + 1]) / 2f;
			vec[i] = b;
			lasti = i;
		}

		float mean = Vector.mean(vec);
		float std = Vector.std(mean, vec);
		vec = Vector.apply(vec, e -> (e - mean) / (std + 1e-5f));

		int bufferSize = 2 * (int) Math.pow(2, Math.ceil(Math.log(Math.abs(d)) / Math.log(2)));
		Complex[] amplitudes = new Complex[bufferSize];

		for (int i = 0; i <= d; i++) {
			float b = Math.abs(vec[i]) >= 2.5 ? mean
					: (contourPoints[frequency0 + i] > 1 ? contourPoints[frequency0 + i]
							: i == 0 ? contourPoints[frequency0 + i + 1]
									: i == d ? contourPoints[frequency0 + i - 1]
											: (contourPoints[frequency0 + i - 1] + contourPoints[frequency0 + i + 1])
													/ 2);

			amplitudes[i] = new Complex((b - mean), 0);
		}

		for (int i = d + 1; i < bufferSize; i++)
			amplitudes[i] = new Complex();

		amplitudes = IFourierFacile.fft(amplitudes);
		int s = bufferSize / 2;
		float[] a = new float[s];
		for (int i = 0; i < s; i++) {
			a[i] = amplitudes[i].getMagnitude();
		}

		float m = Vector.mean(a);// , 10, s - 10);
		float ss = Vector.std(m, a);// , a, 10, s - 10);

		int np = TONGUING_PEAKS;
		int[] peaks = IFourierFacile.getPeaks3(np, 20, s - 20, s, a);

		int tonguing = -1;
		if (peaks != null)
			for (int i = 0; i < np; i++) {
				float c = (peaks[i] * 60 * 2) / (float) bufferSize;
				String k = c + "[" + a[peaks[i]] + "]";
				int p = -1;
				boolean b = false;
				if (((p = IFourierFacile.nextPeak(peaks[i], -1, 10, s - 10, s, a)) != -1) && a[p] < a[peaks[i]]
						&& ((p = IFourierFacile.nextPeak(peaks[i], +1, 10, s - 10, s, a)) != -1) && a[p] < a[peaks[i]])
					b = true;
				float ind = (a[peaks[i]] - m) / ss;

				boolean e = i != np - 1 ? a[peaks[i]] / a[peaks[i + 1]] > 1.09 : false;
				if (c >= 4.9 && c <= 8.6 && ((b && ind > 1) || ind > 2.5 || e)) {
					if (tonguing == -1)
						tonguing = i;
				}
			}
		if (tonguing != -1) {
			int i0 = 0;
			for (int i = frequency0; i <= frequency1; i++)
				if ((contourPoints[i] - contourPoints[frequency0]) >= 2f - 1e5f) {
					i0++;
				}
			return (int) Math.floor((i0 * ((peaks[tonguing] * 60 * 2) / (float) bufferSize)) / 60f);
		}
		
	
		return 0;
	}

	private boolean freqChange(float freq) {
		if (currentFreq != -1) {
			if (!Note.equalFrequency(freq, currentFreq)) {
				if (consecutiveNewFreq > CONSECUTIVE_FREQ_TRESHOLD) {

					return true;
				} else
					consecutiveNewFreq++;
			}

		} else {
			currentFreq = freq;
		}

		return false;
	}

	private boolean changeSilence() {
		// runningmean = (f + (contourIndex - frequency0 - 1) * runningmean) /
		// ((contourIndex - frequency0) + 1e-5f);
		if (contourIndex >= 5) {// Math.abs(f - runningmean) < 50 supprime
			// quelques faux positifs
			float f = (float) contourPoints[contourIndex];
			if (f > 22 && f <= 600) {
				if (!tonguingAnalysis) {
					tonguingAnalysis = true;
					frequency0 = contourIndex;
				}

			} else {
				if (tonguingAnalysis) {
					tonguingAnalysis = false;
					frequency1 = contourIndex - 1;
					return true;
				}
			}

		}
		return false;

	}
}