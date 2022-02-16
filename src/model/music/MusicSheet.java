package model.music;

import java.util.ArrayList;
import java.util.List;

import Exception.ConstructorException;
import javafx.collections.FXCollections;
import model.utility.Fraction;

public class MusicSheet implements IMusicSheet {
	private List<Measure> notes;
	private NoteEnum currentKey;

	private int beat;
	private int time;

	public MusicSheet() {
		this(Measure.DEFAULT_BEAT, Measure.DEFAULT_TIME);
	}

	public MusicSheet(int beat, int time) {
		if (beat <= 0 || time <= 0)
			throw new ConstructorException();

		this.beat = beat;
		this.time = time;

		currentKey = NoteEnum.C;
		notes = new ArrayList<Measure>();

		notes.add(new Measure(beat, time));
	}

	@Override
	public void addNote(Note note) {
		boolean canAdd = addToMeasure(note);
		if (!canAdd) {
			Fraction time = getLastMeasure().getRemainingTime();
			addNote(new Note(time, note.getFrequency(), false, null));

			Fraction newTime = Fraction.substract(note.getFractTime(), time);
			addNote(new Note(newTime, note.getFrequency(), true, getLastNote()));

		}

	}

	private boolean addToMeasure(Note note) {
		Measure currentMeasure = getLastMeasure();
		if (currentMeasure.canAddNote(note)) {
			currentMeasure.addNote(note);
			if (currentMeasure.getRemainingTime().equals(new Fraction(0, 1)))
				addNewMeasure();
		} else
			return false;

		return true;
	}

	private void addNewMeasure() {
		notes.add(new Measure(beat, time));
	}

	/*
	 * private void addDividedNote(Fraction time, int frequency, boolean isTied) {
	 * List<Fraction> newFract = Fraction.divideInUniteFractions(time); for (int i =
	 * 0; i < newFract.size(); i++) { Note tiedNote = isTied == false && i == 0 ?
	 * null : getLastNote(); Note newNote = new
	 * Note(newFract.get(i).getDenominator(), frequency, true, tiedNote);
	 * addNote(newNote); } }
	 */

	public Note getLastNote() {
		Measure activeMeasure = getLastActiveMeasure();
		return activeMeasure.getNoteAt(activeMeasure.getNbrNote() - 1);
	}

	@Override
	public void transposeTo(NoteEnum key) {
		for (int i = 0; i < notes.size(); i++) {
			for (int j = 0; j < notes.get(i).getNbrNote(); j++) {
				notes.get(i).getNotes().get(j).transposeTo(currentKey, key);
			}
		}
		currentKey = key;

	}

	@Override
	public List<Measure> getMeasures() {
		return FXCollections.observableArrayList(notes);
	}

	public int size() {
		return notes.size();
	}

	private Measure getLastMeasure() {
		return notes.get(notes.size() - 1);
	}

	public Measure getLastActiveMeasure() {
		Measure m;
		return ((m = getLastMeasure()).getNbrNote() > 0) ? m : notes.size() >= 2 ? notes.get(notes.size() - 2) : null;
	}

	private boolean noteIndexExist(int measure, int time) {
		return measure >= 0 && measure < notes.size() - 1 && time >= 0 && time < notes.get(time).getNbrNote() - 1;

	}

	public int getNbrNote() {
		int nbrNote = 0;

		for (Measure measure : notes)
			for (Note note : measure.getNotes())
				nbrNote++;
		return nbrNote;
	}

	public NoteEnum getKey() {
		return currentKey;
	}

	public String toString() {
		String str = "";
		for (Measure measure : notes)
			str += measure + "\n";

		return str;
	}

}