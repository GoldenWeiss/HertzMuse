package main;

import controleur.ControleurC;
import controleur.IControleurC;
import de.jensd.fx.glyphs.fontawesome.demo.FontAwesomeIconsDemoApp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Point d'entr�e du programme
 * 
 * @author Frédéric Gosselin et Théo Gutton
 * @since 2019/11/01
 * 
 */
public class App extends Application {
	final KeyCombination FullScreenKeyCombo = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.ALT_ANY);

	/**
	 * Démarre l'application principale JavaFx
	 */
	@Override
	public void start(Stage pStage) throws Exception {

		IControleurC controleur = new ControleurC();

		pStage.setTitle("Hertz-Muse");
		pStage.setScene(controleur.getScene());
		pStage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0);
		});
		pStage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (FullScreenKeyCombo.match(event)) {
				pStage.setFullScreen(!pStage.isFullScreen());
			}
		});
		pStage.setMaximized(true);
		pStage.show();
		
	}

	/**
	 * Point d'entrée du programme
	 * 
	 * @param args les entrées de la console
	 */
	public static void main(String[] args) {

		launch(args);
	}
}
