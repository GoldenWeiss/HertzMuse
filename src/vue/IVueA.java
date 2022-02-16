package vue;

import controleur.DataModele;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioSpectrumListener;
import model.music.Note;

public interface IVueA {
	public int getLastClef();

	public Scene getScene();

	public DataVue getValeur();
	public void setValeur(DataModele pDataModele);

	public DataModele getLastDataModele();
	
	public void disposeWindow(IVueTask wndptr);

	public void setDrawnComponent(Node root);

	public void ajouterNote();

	public void ecouteurNouvelleFeuille();
	public void setDisableSelectNote(boolean disable);

	public void showSine(Note note);

	public void showNote(Note note);
}
