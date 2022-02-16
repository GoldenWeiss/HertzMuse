package model.music;

import Exception.ConstructorException;
import model.utility.Fraction;

public class Note {
	public static final double C0_FREQ = 16.35;

	/**
	 * Time is to be counted as a fraction (4 means 1/4)
	 */
	private Fraction time;
	private int frequency;
	private boolean isHeld;
	private Note tiedNote;
	

	private int nTonguing;

	public Note(Fraction time, int frequency) {
		this(time, frequency, false, 0,null);
	}

	public Note(Fraction time, int frequency, boolean isHeld, int nTonguing,Note tiedNote) {
		if (time.getDenominator() <= 0 || time.getNumerator() <= 0 || frequency < 0)
			throw new ConstructorException("Time: " + time + " Freq: " + frequency);

		this.time = time;
		this.frequency = frequency;
		this.nTonguing = nTonguing;
		this.isHeld = isHeld;
		if(tiedNote != null)
			tiedNote.setTied(this);
	}

	public Note(Fraction time, int frequency, int nTonguing) {
		this(time, frequency, false, nTonguing,null);
	}

	public Note(Fraction time, int frequency, boolean isHeld,Note tiedNote) {
		this(time, frequency, isHeld, 0,tiedNote);
	}
	
	public int getnTonguing() {
		return nTonguing;
	}

	public void setnTonguing(int nTonguing) {
		this.nTonguing = nTonguing;
	}
	
	public boolean isUnit()
	{
		return time.getNumerator() == 1;
	}
	
	public int getTime() {
		return time.getDenominator();
	}
	
	public int getNumerator()
	{
		return time.getNumerator();
	}

	/*public void setTime(int time) {
		if (time >= 0)
			this.time = time;
	}*/

	public int getFrequency() {
		return (int) frequency;
	}

	public void setFrequency(int frequency) {
		if (frequency >= 0)
			this.frequency = frequency;
	}

	public boolean isHeld() {
		return isHeld;
	}
	
	public void setTied(Note note)
	{
		this.tiedNote = note;
	}
	
	public Note getTied()
	{
		return tiedNote;
	}

	public Fraction getFractTime() {
		return time;
	}

	public String getNoteString() {
		return getNoteFromFreq(frequency);
	}

	public void transposeTo(NoteEnum fromKey, NoteEnum toKey) {
		int difference = toKey.getPosition() - fromKey.getPosition();

		int newFrequency = (int) Math.round(this.getFrequency() * Math.pow(2, difference / 12d));

		this.setFrequency(newFrequency);
	}

	public static boolean equalFrequency(float freq1, float freq2) {
		return getNoteFromFreq(freq1).equals(getNoteFromFreq(freq2));
	}

	public static String getNoteFromFreq(float frequency) {
		int difference = (int) Math.round((12 * Math.log(frequency / C0_FREQ) / Math.log(2)));

		int note = difference % 12;
		int octave = difference / 12;

		return NoteEnum.getNoteFromPosition(note).toString() + octave;
	}

	@Override
	public String toString() {
		return frequency + "Hz (" + getNoteString() + ") " + time + " " + "T.ing: " + nTonguing;
	}

}