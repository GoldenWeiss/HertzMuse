package controleur;

import java.io.File;

import javafx.scene.Scene;
import model.music.MusicFactory;
import model.music.NoteEnum;

public interface IControleurC {
	Scene getScene();
	public void ecouteurModele(DataModele pDataModele);
	boolean ecouteurTogglePlay();
	void ecouteurFenetrage(String a);
	void ecouteurDetectionf0(String string);
	void ecouteurCanal(int i);
	void ecouteurUnites(int i);
	void ecouteurToggleCentrage();
	void ecouteurToggleNorm();
	void ecouteurDragRead(int x, boolean b, boolean c);
	void ecouteurLoadAudioFile(File f);
	void setTempo(int tempo);
	int getTempo();
	void setKey(NoteEnum key);
	MusicFactory getMusicFactory();
	void ecouteurAjouterNote();
	void ecouteurNouvelleFeuille();
	int ecouteurNewLayer(int pType, File pFile);
	void ecouteurSetLayer(int pIndice);
	void ecouteurSetVolume(double doubleValue);
	int ecouteurDeleteCurrentLayer();
	void ecouteurSetDetectTonguing(boolean pDetectTonguing);
	
}
