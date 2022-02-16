package model.music;

import java.util.ArrayList;
import java.util.List;

import Exception.ConstructorException;
import model.utility.Fraction;

public class Measure
{
	public static final int DEFAULT_BEAT = 4;
	public static final int DEFAULT_TIME = 4;

	/**
	 * Number of beat per measure
	 */
	private int beat;

	/**
	 * Duration of each beat
	 */
	private int time;

	private List<Note> notes;

	public Measure()
	{
		this(DEFAULT_BEAT, DEFAULT_TIME);
	}

	public Measure(int beat, int time)
	{
		// TODO check for unconventional time signature (not 4/4)

		if (beat <= 0 || time <= 0)
			throw new ConstructorException();

		this.beat = beat;
		this.time = time;
		notes = new ArrayList<Note>();
	}

	public Fraction getFullTime()
	{
		return new Fraction(beat, time);
	}

	public void addNote(Note note)
	{
		if (canAddNote(note))
			notes.add(note);

	}

	public List<Note> getNotes()
	{
		return notes;
	}

	public Note getNoteAt(int index)
	{
		if (index >= 0 && index <= notes.size() - 1)
			return notes.get(index);
		return null;
	}

	public int getNbrNote()
	{
		return notes.size();
	}

	public Note removeNoteAt(int index)
	{
		Note removedNote = null;

		if (index >= 0 && index < notes.size())
			removedNote = notes.remove(index);

		return removedNote;
	}

	public Fraction getRemainingTime()
	{
		Fraction time = getFullTime();

		for (Note note : notes)
			time = Fraction.substract(time, note.getFractTime());

		return time;
	}

	public boolean canAddNote(Note note)
	{
		Fraction timeLeft = Fraction.substract(getRemainingTime(), note.getFractTime());
		return timeLeft.compareTo(new Fraction(0, 1)) >= 0;

	}

	public String toString()
	{
		String str = "";
		
		for(Note note: notes)
			str += note + ", ";
		
		return str;
	}
	
}