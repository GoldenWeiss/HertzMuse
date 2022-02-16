package vue;

import java.io.IOException;

import controleur.DataModele;
import controleur.IControleurC;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.audio.ThreadModele;
import model.music.Note;
import model.music.NoteEnum;

public class VueNote extends IVueTask {

	private IControleurC controleur;
	private VueA vue;
	private Scene scene;
	private HBox root;
	private IVueTask _wndptr_waveform;
	private double xOffset = 0;
	private double yOffset = 0;

	@FXML
	Label noteText;

	@FXML
	Label frequency;
	
	@FXML
	Label time;

	VueA vueMere;
	private Note note;

	public VueNote(VueA vueA, IControleurC pControleur, Note pNote) {
		super();
		vueMere = vueA;
		this.note = pNote;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueNote.fxml"));
			loader.setController(this);
			root = loader.load();
			scene = new Scene(root);

		} catch (IOException e) {
			e.printStackTrace();
		}

		controleur = pControleur;

		setNote(pNote);

		wndptr.setScene(scene);

		wndptr.initOwner(vueA.getScene().getWindow());
		wndptr.show();

	}

	@FXML
	private void afficherOnde() {
		vueMere.showSine(note);
	}

	public void setNote(Note pNote) {
		this.noteText.setText("Note: " + pNote.getNoteString());
		this.frequency.setText("Fréquence: " + pNote.getFrequency());
		this.time.setText("Temps: " + pNote.getFractTime());
		wndptr.setTitle("Voir la note - Note:" + pNote.getNoteString());
	}

	@Override
	void draw() {
		// TODO Auto-generated method stub

	}

	@Override
	void update() {
		// TODO Auto-generated method stub

	}

}
