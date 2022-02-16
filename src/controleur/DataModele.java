package controleur;

import model.audio.IFourierFacile;
import model.music.Note;

public class DataModele {

	private float[][] data;
	private float[][] orderedData;
	private int[] buff;
	private int currentFrame;
	private int buffWidth;
	private float zoomFactor;
	private int[] buff2;

	private int harmonicIndex;
	private float harmonicFrequency;
	private float harmonicAmplitude;

	private float peakIndex;
	private float peakFrequency;
	private float peakAmplitude;
	private float instrumentHarmonicFamily;

	private String pitchDetectionTitle;
	private String windowingTitle;
	private int unitsIndex;
	private boolean substractMean;
	private boolean norm;

	private int channels;
	private int displayedChannel;
	
	private float[] samplePoints;
	private float[][] timePoints;
	private int timePointsLength;
	private int realTimeIndex;
	private double mpDuration;
	
	private float[] musicalContourBuffer;
	private int computedContourBufferLength;
	private int fpsTimeIndex;
	
	private int currentLayerIndex;
	
	private Note lastNote;
	
	public DataModele() {
		data = new float[3][];
		orderedData = new float[3][];
		setLastNote(null);
	}

	public synchronized float[] getAmplitudes() {
		return data[1];
	}

	public synchronized void setAmplitudes(float[] correctedMagnitudes) {
		data[1] = correctedMagnitudes;
	}

	public synchronized float[] getFrequencies() {
		return data[0];
	}

	public synchronized void setFrequencies(float[] pFrequencies) {
		data[0] = pFrequencies;
	}

	public synchronized float[] getPhases() {
		return data[2];
	}

	public synchronized void setPhases(float[] pPhases) {
		data[2] = pPhases;
	}

	public synchronized float[][] getOrderedData() {
		return orderedData;
	}

	public synchronized void setOrderedData(float[][] pOrderedData) {
		orderedData = pOrderedData;
	}

	public synchronized void setCurrentFrame(int pCurrentFrame) {
		currentFrame = pCurrentFrame;
	}

	public synchronized int getCurrentFrame() {
		return currentFrame;
	}

	public synchronized void setMask(int[] pBuff) {
		buff = pBuff;
	}

	public synchronized int[] getMask() {

		return buff;
	}

	public synchronized int getBuffWidth() {
		return buffWidth;
	}

	public synchronized void setBuffWidth(int pBuffWidth) {
		buffWidth = pBuffWidth;
	}

	public synchronized float getZoomFactor() {
		return zoomFactor;
	}

	public synchronized void setZoomFactor(float pZoomFactor) {
		this.zoomFactor = pZoomFactor;
	}

	public synchronized void setHorizontalMask(int[] pBuff) {
		buff2 = pBuff;

	}

	public synchronized int[] getBuff2() {
		return buff2;
	}

	public synchronized int getHarmonicIndex() {
		return harmonicIndex;
	}

	public synchronized float getHarmonicFrequency() {
		return harmonicFrequency;
	}

	public synchronized float getHarmonicAmplitude() {
		return harmonicAmplitude;
	}

	public synchronized float getPeakIndex() {
		return peakIndex;
	}

	public synchronized void setPeakIndex(float peakIndex) {
		this.peakIndex = peakIndex;
	}

	public synchronized float getPeakFrequency() {
		return peakFrequency;
	}

	public synchronized void setPeakFrequency(float peakFrequency) {
		this.peakFrequency = peakFrequency;
	}

	public synchronized float getPeakAmplitude() {
		return peakAmplitude;
	}

	public synchronized void setPeakAmplitude(float peakAmplitude) {
		this.peakAmplitude = peakAmplitude;
	}

	public synchronized void setHarmonicIndex(int harmonicIndex) {
		this.harmonicIndex = harmonicIndex;
	}

	public synchronized void setHarmonicFrequency(float harmonicFrequency) {
		this.harmonicFrequency = harmonicFrequency;
	}

	public synchronized void setHarmonicAmplitude(float harmonicAmplitude) {
		this.harmonicAmplitude = harmonicAmplitude;
	}

	public synchronized void setInstrumentHarmonicFamily(float instrumentHarmonicFamily) {
		this.instrumentHarmonicFamily = instrumentHarmonicFamily;
	}

	public synchronized float getInstrumentHarmonicFamily() {
		return instrumentHarmonicFamily;
	}

	public synchronized String getPitchDetectionTitle() {
		return pitchDetectionTitle;
	}

	public synchronized void setPitchDetectionTitle(String pitchDetectionTitle) {
		this.pitchDetectionTitle = pitchDetectionTitle;
	}

	public synchronized String getWindowingTitle() {
		return windowingTitle;
	}

	public synchronized void setWindowingTitle(String windowingTitle) {
		this.windowingTitle = windowingTitle;
	}

	public synchronized int getChannels() {
		return channels;
	}

	public synchronized void setChannels(int channels) {
		this.channels = channels;
	}

	public synchronized int getDisplayedChannel() {
		return displayedChannel;
	}

	public synchronized void setDisplayedChannel(int displayedChannel) {
		this.displayedChannel = displayedChannel;
	}

	public synchronized int getUnitsIndex() {
		return unitsIndex;
	}

	public synchronized void setUnitsIndex(int unitsIndex) {
		this.unitsIndex = unitsIndex;
	}

	public synchronized boolean isSubstractMean() {
		return substractMean;
	}

	public synchronized void setSubstractMean(boolean substractMean) {
		this.substractMean = substractMean;
	}

	public synchronized boolean isNorm() {
		return norm;
	}

	public synchronized void setNorm(boolean norm) {
		this.norm = norm;
	}

	public synchronized float[][] getTimePoints() {
		return timePoints;
	}

	public synchronized void setTimePoints(float[][] timePoints) {
		this.timePoints = timePoints;
	}

	public synchronized int getTimePointsLength() {
		return timePointsLength;
	}

	public synchronized void setTimePointsLength(int timePointsLength) {
		this.timePointsLength = timePointsLength;
	}

	public synchronized int getRealTimeIndex() {
		return realTimeIndex;
	}

	public synchronized void setRealTimeIndex(int realTimeIndex) {
		this.realTimeIndex = realTimeIndex;
	}

	public synchronized float[] getSamplePoints() {
		return samplePoints;
	}

	public synchronized void setSamplePoints(float[] points) {
		this.samplePoints = points;
	}

	public synchronized double getMpDuration() {
		return mpDuration;
	}

	public synchronized void setMpDuration(double mpDuration) {
		this.mpDuration = mpDuration;
	}

	public synchronized float[] getMusicalContourBuffer() {
		return musicalContourBuffer;
	}

	public synchronized void setMusicalContourBuffer(float[] musicalContourBuffer) {
		this.musicalContourBuffer = musicalContourBuffer;
	}

	public synchronized int getComputedContourBufferLength() {
		return computedContourBufferLength;
	}

	public synchronized void setComputedContourBufferLength(int computedContourBufferLength) {
		this.computedContourBufferLength = computedContourBufferLength;
	}

	public synchronized int getFpsTimeIndex() {
		return fpsTimeIndex;
	}

	public synchronized void setFpsTimeIndex(int fpsTimeIndex) {
		this.fpsTimeIndex = fpsTimeIndex;
	}

	public Note getLastNote() {
		return lastNote;
	}

	public void setLastNote(Note lastNote) {
		this.lastNote = lastNote;
	}

	public int getCurrentLayerIndex() {
		return currentLayerIndex;
	}

	public void setCurrentLayerIndex(int currentLayerIndex) {
		this.currentLayerIndex = currentLayerIndex;
	}

	







	





}
