package model.music;

import java.util.List;

public interface IMusicSheet
{
	void addNote(Note note);
	
	void transposeTo(NoteEnum key);

	List<Measure> getMeasures();
}