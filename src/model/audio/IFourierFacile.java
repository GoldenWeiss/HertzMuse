package model.audio;

import java.util.Arrays;
import java.util.PriorityQueue;

import model.utility.Complex;

public class IFourierFacile {

	public static float[] getPeak(float frequenciesBand, float[] magnitudes, float[] frequencies) {
		int l = 0;
		int s = magnitudes.length;
		for (int i = 1; i < s; i++)
			if (magnitudes[i] > magnitudes[l])
				l = i;

		float q = magnitudes[l] * l;
		float g = magnitudes[l];
		for (int i = 1; l - i > -1 && l + i < s && magnitudes[l - i] > 0.1d * magnitudes[l]
				&& magnitudes[l + i] > 0.1d * magnitudes[l]; i++) {
			q += magnitudes[l - i] * (l - i) + magnitudes[l + i] * (l + i);
			g += magnitudes[l - i] + magnitudes[l + i];
		}
		return new float[] { l, frequenciesBand * q / g };
	}

	public static float[] getPeaks(float[] magnitudes, float[] frequencies) {
		// TODO Ne retourne pas toujours la bonne valeur pour la plus haute fr�quence.

		PriorityQueue<Integer> priorityQueue = new PriorityQueue<>((a, b) -> (int) (magnitudes[b] - magnitudes[a]));
		for (int i = 0, s = magnitudes.length - 2; i < s && frequencies[i] < 5_000; i++) {
			if ((magnitudes[i + 1]) > magnitudes[i] && (magnitudes[i + 2]) < magnitudes[i + 1]) {
				priorityQueue.add(i + 1);
			}
		}

		int i = 0;
		float[] peaks = new float[priorityQueue.size()];
		while (!priorityQueue.isEmpty())
			peaks[i++] = frequencies[priorityQueue.poll()];
		return peaks;

	}

	public static Complex[] fft(Complex[] x) {
		int n = x.length;

		// base case
		if (n == 1)
			return new Complex[] { x[0] };

		// radix 2 Cooley-Tukey FFT
		if (n % 2 != 0) {
			throw new IllegalArgumentException("n is not a power of 2");
		}

		// compute FFT of even terms
		Complex[] even = new Complex[n / 2];
		for (int k = 0; k < n / 2; k++) {
			even[k] = x[2 * k];
		}
		Complex[] evenFFT = fft(even);

		// compute FFT of odd terms
		Complex[] odd = even; // reuse the array (to avoid n log n space)
		for (int k = 0; k < n / 2; k++) {
			odd[k] = x[2 * k + 1];
		}
		Complex[] oddFFT = fft(odd);

		// combine
		Complex[] y = new Complex[n];
		for (int k = 0; k < n / 2; k++) {
			double kth = -2 * k * Math.PI / n;
			Complex expOdd = Complex.mult(new Complex(Math.cos(kth), Math.sin(kth)), oddFFT[k]);

			y[k] = Complex.add(evenFFT[k], expOdd);
			y[k + n / 2] = Complex.sub(evenFFT[k], expOdd);
		}
		return y;
	}

	public static float soundIntensityToDecibels(float pI) {
		return (float) (20 * Math.log10(pI / 1e-5));
	}

	public static float[][] averagingOverNSamples(int nSamples, float[][] data) {
		float[][] retour = new float[data.length][nSamples];

		double divs = data[0].length / (double) nSamples;

		for (int n = 0, r = data.length; n < r; n++)
			for (int k = 0; k < nSamples; k++) {
				for (int d = 0; d < divs; d++)
					retour[n][k] += data[n][(int) (k * divs + d)];
				retour[n][k] /= divs;
			}

		return retour;

	}

	public static float[] averagingOverNSamples(int nSamples, float[] data) {
		float[] retour = new float[nSamples];

		double divs = data.length / (double) nSamples;

		for (int k = 0; k < nSamples; k++) {
			for (int d = 0; d < divs; d++)
				retour[k] += data[(int) (k * divs + d)];
			retour[k] /= divs;
		}

		return retour;

	}

	public static float[] averagingOverNSamples(int nSamples, float[] data, int datalength) {
		float[] retour = new float[nSamples];

		double divs = datalength / (double) nSamples;

		for (int k = 0; k < nSamples; k++) {
			for (int d = 0; d < divs; d++)
				retour[k] += data[(int) (k * divs + d)];
			retour[k] /= divs;
		}

		return retour;

	}

	public static float centroidPeak(int l, int s, float[] magnitudes) {
		float q = magnitudes[l] * l;
		float g = magnitudes[l];
		for (int i = 1; l - i > -1 && l + i < s && magnitudes[l - i] > 0.1d * magnitudes[l]
				&& magnitudes[l + i] > 0.1d * magnitudes[l]; i++) {
			q += magnitudes[l - i] * (l - i) + magnitudes[l + i] * (l + i);
			g += magnitudes[l - i] + magnitudes[l + i];
		}
		return q / g;

	}

	public static float centroidPeak2(int l, int s, float[] magnitudes) {
		float q = magnitudes[l] * l;
		float g = magnitudes[l];
		for (int i = 1; l - i > -1 && l + i < s && i < 5; i++) {
			q += magnitudes[l - i] * (l - i) + magnitudes[l + i] * (l + i);
			g += magnitudes[l - i] + magnitudes[l + i];
		}
		return q / g;
	}

	public static float[][] getPeaks2(int nSamples, int lowerFreqBound, int higherFreqBound, float frequenciesBand,
			float[] magnitudes, float[] frequencies) {

		PriorityQueue<Float> ampQueue = new PriorityQueue<>(
				(a, b) -> (int) (magnitudes[b.intValue()] - magnitudes[a.intValue()]));
		int s = magnitudes.length;

		for (int i = (int) (lowerFreqBound / frequenciesBand), t = (int) (Math.min(higherFreqBound / frequenciesBand,
				s)); i < t; i++)
			ampQueue.add((float) i);

		float[][] retour = new float[nSamples][2];

		for (int n = 0; n < nSamples; n++) {
			int l = ampQueue.poll().intValue();
			retour[n] = new float[] { l, frequenciesBand * centroidPeak(l, s, magnitudes) };
		}

		return retour;
	}

	public static int[] getPeaks3(int nPeaks, int lowerIndex, int higherIndex, int nSamples, float[] magnitudes) {
		PriorityQueue<Float> ampQueue = new PriorityQueue<>(
				(a, b) -> (int) (magnitudes[b.intValue()] - magnitudes[a.intValue()]));

		for (int i = lowerIndex, t = (int) (Math.min(higherIndex, nSamples)); i < t; i++)
			//if ((magnitudes[i - 1] - magnitudes[i]) <= 1e-5f && magnitudes[i + 1] - magnitudes[i] <= 1e-5f)
				ampQueue.add((float) i);

		int[] retour = new int[nPeaks];

		for (int n = 0; n < nPeaks; n++) {
			Float v = ampQueue.poll();
			if (v == null)
				return null;
			retour[n] = v.intValue();
		}

		return retour;
	}

	public static int nextPeak(int index, int increment, int lowerIndex, int higherIndex, int nSamples,
			float[] magnitudes) {
		int i = index;
		int chosenIndex = -1;
		while (i >= lowerIndex && i <= higherIndex && i < nSamples) {
			i += increment;

			if ((magnitudes[i - 1] - magnitudes[i]) <= 1e-5f && magnitudes[i + 1] - magnitudes[i] <= 1e-5f) {
				chosenIndex = i;
				break;
			}
		}
		return chosenIndex;
	}

	public static float[] downsample(int nSamples, float[] amplitudes, int dataLength) {
		double n = dataLength / (double) nSamples;

		float[] downedSample = new float[nSamples];
		for (int i = 0; i < nSamples; i++) {
			downedSample[i] = amplitudes[(int) (i * n)];
		}
		return downedSample;
	}

	public static float autocorrelation(int lowerFreqBound, int higherFreqBound, float[] amplitudes) {
		int nLowerBound = (int) (ThreadModele.SAMPLE_RATE / higherFreqBound);
		int nHigherBound = (int) (ThreadModele.SAMPLE_RATE / lowerFreqBound);

		int len = amplitudes.length;

		if (len < nHigherBound)
			System.exit(0);

		float max = Float.MIN_VALUE;
		int selectedIndex = -1;
		float[] sum = new float[nHigherBound - nLowerBound];
		for (int period = nLowerBound; period < nHigherBound; period++) {

			for (int j = 0, s = len - period; j < s; j++)
				sum[period - nLowerBound] += amplitudes[j] * amplitudes[j + period];

			if (sum[period - nLowerBound] > max) {
				max = sum[period - nLowerBound];
				selectedIndex = period;
			}
		}
		float cSelectedIndex = centroidPeak(selectedIndex, nHigherBound - nLowerBound, sum);
		return (float) (len / cSelectedIndex);
	}

	public static float autocorrelation(int lowerFreqBound, int higherFreqBound, Complex[] complexBuffer) {

		int nLowerBound = (int) (lowerFreqBound / ThreadModele.FREQUENCY_BINS);
		int nHigherBound = (int) (higherFreqBound / ThreadModele.FREQUENCY_BINS );

		int len = complexBuffer.length;
		// ... ifft start
		Complex[] autoBuff = new Complex[len];
		for (int i = 0; i < len; i++)
			autoBuff[i] = Complex.mult(complexBuffer[i], complexBuffer[i].conjugate()).conjugate();

		autoBuff = IFourierFacile.fft(autoBuff);

		for (int i = 0; i < len; i++) {
			autoBuff[i] = autoBuff[i].conjugate();
			autoBuff[i] = autoBuff[i].scale(1 / (double) len);
		}
		// ... ifft end

		float v = 0;
		float max = Float.MIN_VALUE;
		float index = -1;

		for (int i = nLowerBound; i < nHigherBound - 1; i++) {
			v = autoBuff[i].getMagnitude();
			if (v > max) {
				max = v;
				index = i;
			}
		}
		return len / index; // assuming len = sample_size
	}

	public static float cepstrum(int lowerFreqBound, int higherFreqBound, Complex[] complexBuffer) {

		int nLowerBound = (int) (ThreadModele.SAMPLE_RATE / higherFreqBound);
		int nHigherBound = (int) (ThreadModele.SAMPLE_RATE / lowerFreqBound);

		int len = complexBuffer.length;
		// ... ifft start
		Complex[] autoBuff = new Complex[len];
		for (int i = 0; i < len; i++)
			autoBuff[i] = new Complex(Math.log(complexBuffer[i].getMagnitude()), 0).conjugate();

		autoBuff = IFourierFacile.fft(autoBuff);

		for (int i = 0; i < len; i++) {
			autoBuff[i] = autoBuff[i].conjugate();
			autoBuff[i] = autoBuff[i].scale(1 / (double) len);
		}
		// ... ifft end

		float v = 0;
		float max = Float.MIN_VALUE;
		float index = -1;

		for (int i = nLowerBound; i < nHigherBound - 1; i++) {
			v = autoBuff[i].getMagnitude();
			if (v > max) {
				max = v;
				index = i;
			}
		}
		return len / index; // assuming len = sample_size
	}

	public static float[][] sort(int nSamples, float[] frequencies, float[] amplitudes, float[] phases) {
		PriorityQueue<Integer> ampQueue = new PriorityQueue<>((a, b) -> (int) (amplitudes[b] - amplitudes[a]));
		int s = frequencies.length;
		for (int i = 0; i < s; i++)
			ampQueue.add(i);

		PriorityQueue<Integer> freqQueue = new PriorityQueue<>((a, b) -> (int) (frequencies[a] - frequencies[b]));

		for (int i = 0; i < nSamples; i++)
			freqQueue.add(ampQueue.poll());

		float[][] retour = new float[3][nSamples];

		for (int i = 0; i < nSamples; i++) {
			int indice = freqQueue.poll();

			retour[0][i] = frequencies[indice];

			retour[1][i] = amplitudes[indice];

			retour[2][i] = phases[indice];
		}

		return retour;
	}

	public static float[][] threshold(int t, float[][] frequencyBand) {

		frequencyBand[0] = Arrays.copyOf(frequencyBand[0], t);
		frequencyBand[1] = Arrays.copyOf(frequencyBand[1], t);
		frequencyBand[2] = Arrays.copyOf(frequencyBand[2], t);
		return frequencyBand;
	}

	public static float[] anyChannelToMono(float[][] d) {
		int depth = d.length;
		int s = d[0].length;
		float[] p = new float[s];

		for (int k = 0; k < s; k++) {
			for (int i = 0; i < depth; i++)
				p[k] += d[i][k];
			p[k] /= depth;
		}

		return p;
	}

	public static float std(float mean, float[] vec) {
		int w = vec.length;
		int sum = 0;
		for (int i = 0; i < w; i++)
			sum += (vec[i] - mean) * (vec[i] - mean);
		return (float) Math.sqrt(sum / w);
	}

	/**
	 * Retourne la moyenne des valeurs d'une liste de float
	 * 
	 * @param data liste
	 * @return Moyenne des valeurs de la liste
	 */
	public static float getAverage(float[] data) {
		float retour = 0;

		for (int i = 0; i < data.length; i++)
			retour += data[i];
		return retour / data.length;
	}

	/**
	 * Retourne la liste recue en parametre avec les valeurs au carré
	 * 
	 * @param data Liste
	 * @return Liste avec les valeur au carré
	 */
	public static float[] squareValue(float[] data) {
		float[] retour = new float[data.length];
		for (int i = 0; i < data.length; i++)
			retour[i] = (float) Math.pow(data[i], 2);
		return retour;
	}

	public static float[] lowPassFilter(float[] input, int numberNeighbors) {
		float[] output = new float[input.length - numberNeighbors * 2];
		for (int i = numberNeighbors; i < input.length - numberNeighbors; i++) {
			float averageSum = input[i];
			for (int j = 0; j < numberNeighbors; j++) {
				averageSum += input[i + j];
				averageSum += input[i - j];
			}
			output[i - numberNeighbors] = averageSum / (numberNeighbors * 2 + 1);
		}

		return output;
	}

	/**
	 * Reduce every value of the list so that they are below the treshold
	 * 
	 * @param treshold max size for value
	 * @param data     List
	 * @return List with values reduced under the treshhold.
	 */
	public static float[] normalise(int treshold, float[] data) {
		float largestValue = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i] > largestValue)
				largestValue = data[i];
		float divider = largestValue / (float) treshold;

		float[] retour = new float[data.length];
		for (int i = 0; i < data.length; i++)
			retour[i] = data[i] / divider;
		return retour;
	}
}
