package model.music;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public enum NoteEnum
{
	C(1), Db(2), D(3), Eb(4), E(5), F(6), Gb(7), G(8), Ab(9), A(10), Bb(11), B(12);

	private final int position;
	
	private NoteEnum(int position)
	{
		this.position = position;
	}
	
	public int getPosition()
	{
		return position;
	}

	public static NoteEnum getNoteFromPosition(int position)
	{
		if (position >= 0 && position < NoteEnum.values().length)
			return NoteEnum.values()[position];
		return null;
	}
	
	public static ObservableList<NoteEnum> getItems()
	{
		return FXCollections.observableArrayList(NoteEnum.values());
				
	}

	public static int getPositionFromNote(String note)
	{
		for(int i = 0; i < NoteEnum.values().length;i++)
			if(NoteEnum.values()[i].toString().equals(note))
				return NoteEnum.values()[i].getPosition();
			
		return -1;
	}
}