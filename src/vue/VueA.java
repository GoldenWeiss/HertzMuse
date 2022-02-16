package vue;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.skins.JFXPopupSkin;
import com.jfoenix.transitions.CachedTransition;

import controleur.DataModele;
import controleur.IControleurC;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.animation.AnimationTimer;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import main.App;
import model.audio.ThreadModele;
import model.music.Measure;
import model.music.MusicSheet;
import model.music.Note;
import model.music.NoteEnum;
import model.utility.CColor;

public class VueA implements IVueA {

	private static final String CHM_FILENAME = "testData/testMp3File/sample.chm";
	private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
	private static final PseudoClass SELECTED2_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected2");

	private StackPane root;

	private Scene scene;
	private DataModele lastDataModele;
	private IVueTask _wndptr_bassesRenderer;
	private IVueTask _wndptr_spectralAnalyser;
	private IVueTask _wndptr_waveform;
	private IVueTask _wndptr_pianoVisualizer;
	private IVueTask _wndptr_drawnCanvas;
	private IVueTask _wndptr_tableView;
	private IVueTask _wndptr_noteInfos;
	private IVueTask _wndptr_sine;

	private List<IVueTask> wndCache;

	private IControleurC controleur;
	private AnimationTimer actualDrawProcess;
	private FileChooser fileChooser;

	@FXML
	StackPane stackPane1;
	@FXML
	StackPane stackPane2;
	@FXML
	StackPane stackPane3;

	@FXML
	BorderPane borderPane1;

	/*
	 * @FXML RadioMenuItem rdiMusicSheet;
	 * 
	 * @FXML RadioMenuItem rdiMelodicContour;
	 * 
	 * @FXML ToggleButton btnMusicSheet1;
	 * 
	 * @FXML ToggleButton btnMusicSheet2;
	 * 
	 * @FXML ToggleButton btnMusicSheet3;
	 * 
	 * @FXML ComboBox<NoteEnum> key;
	 * 
	 * @FXML TextField tempo;
	 */
	@FXML
	JFXButton jfxFichier;
	@FXML
	JFXButton jfxAccueil;
	@FXML
	JFXButton jfxEdition;
	@FXML
	JFXButton jfxEnregistrement;
	@FXML
	JFXButton jfxFenetre;
	@FXML
	JFXButton jfxAide;

	// 0
	@FXML
	private JFXButton jfxNewWindow;
	@FXML
	private JFXButton jfxRestart;
	@FXML
	private JFXButton jfxExit;

	// 1
	@FXML
	JFXButton jfxNewEmptySheet;
	@FXML
	JFXButton jfxNewLayer;
	@FXML
	JFXButton jfxNewLayerArrow;
	@FXML
	JFXButton jfxMusicSheet1;
	@FXML
	JFXButton jfxMelodicContour1;
	@FXML
	JFXButton jfxTogglePlay;

	// 2
	@FXML
	private JFXButton jfxKey;
	@FXML
	private JFXButton jfxTempo;
	@FXML
	private JFXButton jfxVolume;
	@FXML
	private JFXButton jfxTonguing;
	@FXML
	private JFXButton jfxTone;
	@FXML
	private JFXButton jfxDeleteLayer;
	@FXML
	private JFXButton jfxNoteInfo;
	@FXML
	private JFXButton jfxNoteSine;

	// 3
	@FXML
	private JFXButton jfxNewRecording;
	@FXML
	private JFXButton jfxToggleRecording;
	@FXML
	private JFXButton jfxDeleteRecording;
	// 4
	@FXML
	JFXButton jfxMusicSheet4;
	@FXML
	JFXButton jfxMelodicContour4;
	@FXML
	JFXButton jfxSpectralAnalysis;
	@FXML
	JFXButton jfxWaveForm;
	@FXML
	JFXButton jfxTableView;
	@FXML
	JFXButton jfxPianoVisualizer;
	@FXML
	JFXButton jfxBassesRenderer;
	@FXML
	JFXButton jfxBassesRendererArrow;
	@FXML
	JFXButton jfxHider;
	// 5
	@FXML
	JFXButton jfxHelp;
	@FXML
	JFXButton jfxAbout;
	@FXML
	JFXButton jfxHomepage;

	@FXML
	JFXListView<Label> jfxPageList;

	@FXML
	JFXButton jfxPageName;
	@FXML
	JFXButton jfxPagePlus;
	@FXML
	JFXButton jfxPageMinus;
	@FXML
	JFXButton jfxPageHider;
	@FXML
	HBox leftPane;
	@FXML
	Label lblSheetTitle;

	@FXML
	HBox hboxMenuBar;

	@FXML
	VBox layersVbox;

	@FXML
	JFXButton jfxNewEmptySheetKey;
	@FXML
	JFXButton jfxNewEmptySheetAccept;
	@FXML
	JFXButton jfxNewEmptySheetCancel;
	@FXML
	TextField jfxNewEmptySheetName;
	@FXML
	TextField jfxNewEmptySheetTempo;

	private Node[] menuComponentRoot;
	private JFXButton[][] menuComponentButtons;

	private JFXButton[] menuButtons;

	private int selectedButton;
	private boolean init;
	private int nMenuButtons;
	private boolean showMenuComponentRoot;

	private List<Boolean> playing;

	private boolean showLeftPane;
	public int feuilleCourante;
	public int feuillesTaille;
	public boolean isPastSheet;
	public List<String> nomPages;
	private int nLayerFile;
	private int nLayerMicro;
	private int currentLayerIndex;
	private int lastClef;
	private List<Double> volume;

	public void setDrawnComponent(Node pNode) {

		if (pNode == null) {
			stackPane1.getChildren().clear();
			return;
		}

		if (stackPane1.getChildren().isEmpty()) {
			stackPane1.getChildren().add(pNode);
		} else {

			stackPane1.getChildren().remove(0);
			stackPane1.getChildren().add(pNode);

			Duration d = Duration.millis(100);

			FadeTransition fp = new FadeTransition(d, pNode);
			fp.setFromValue(0);
			fp.setToValue(1);

			TranslateTransition tp = new TranslateTransition(d, pNode);
			tp.setFromX(-10);
			tp.setToX(0);

			ParallelTransition p = new ParallelTransition();
			p.getChildren().addAll(fp, tp);
			p.setCycleCount(1);
			p.play();

		}

	}

	public VueA(IControleurC pControleur) {
		controleur = pControleur;
		wndCache = new ArrayList<>();
		_wndptr_drawnCanvas = null;
		init = false;
		currentLayerIndex = -1;
		playing = new ArrayList<>();
		volume = new ArrayList<>();

		feuillesTaille = 1;
		feuilleCourante = 0;
		isPastSheet = false;
		nomPages = new ArrayList<>();
		lastClef = 2;

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VuePrincipale.fxml"));
			loader.setController(this);
			root = loader.load();

			scene = new Scene(root);

		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * key.setItems(NoteEnum.getItems());
		 * key.getSelectionModel().select(NoteEnum.C); key.setOnAction(e ->
		 * changeKey(key));
		 */
		fileChooser = new FileChooser();
		String userDirectoryString = Paths.get(".").toAbsolutePath().normalize().toString();
		File userDirectory = new File(userDirectoryString);
		if (!userDirectory.canRead())
			userDirectory = new File("c:/");

		fileChooser.setInitialDirectory(userDirectory);

		actualDrawProcess = new AnimationTimer() {

			public void handle(long timestamp) {
				for (IVueTask wndptr : wndCache)
					Platform.runLater(() -> wndptr.draw());
			}
		};

		actualDrawProcess.start();

	}

	private void changeKey(ComboBox<NoteEnum> self) {
		controleur.setKey(self.getValue());
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public DataVue getValeur() {
		return null;
	}

	@Override
	public void setValeur(DataModele pDataModele) {
		lastDataModele = pDataModele;

		// drawing computations ->
		// [...]
		for (IVueTask wndptr : wndCache)
			wndptr.update();

		// actual draw phase ->
		// Platform.runLater(() -> //draw(););
	}

	public void setMenuComponent(Node pNode) {
		if (stackPane2.getChildren().isEmpty()) {
			stackPane2.getChildren().add(pNode);
		} else {

			stackPane2.getChildren().clear();
			stackPane2.getChildren().add(pNode);

			Duration d = Duration.millis(100);

			FadeTransition fp = new FadeTransition(d, pNode);
			fp.setFromValue(0);
			fp.setToValue(1);

			TranslateTransition tp = new TranslateTransition(d, pNode);
			tp.setFromX(-10);
			tp.setToX(0);

			ParallelTransition p = new ParallelTransition();
			p.getChildren().addAll(fp, tp);
			p.setCycleCount(1);
			p.play();

		}
	}

	@FXML
	private void initialize() {

		if (init)
			return; // early exited on multiple loads

		jfxHider.getGraphic().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, showMenuComponentRoot = true);
		jfxPageHider.getGraphic().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, showLeftPane = true);

		init = true;
		selectedButton = -1;
		int borderHeight = 4;
		Duration d = Duration.millis(100);

		menuButtons = new JFXButton[] { jfxFichier, jfxAccueil, jfxEdition, jfxEnregistrement, jfxFenetre, jfxAide };
		String[] colors = new String[] { "dimgrey", "green", "rgb(0,128,255)", "red", "purple", "orange" };

		nMenuButtons = menuButtons.length;
		menuComponentRoot = new Node[nMenuButtons];
		menuComponentButtons = new JFXButton[nMenuButtons][];
		IntegerProperty[] bottomInsetsProperty = new SimpleIntegerProperty[nMenuButtons];
		IntegerProperty[] bottomBorderProperty = new SimpleIntegerProperty[nMenuButtons];
		ReadOnlyStringWrapper[] cssString = new ReadOnlyStringWrapper[nMenuButtons];
		for (int j = 0; j < nMenuButtons; j++) {
			final int i = j;

			boldButtonWidthCorrection(menuButtons[i], menuButtons[i], 22);

			bottomInsetsProperty[i] = new SimpleIntegerProperty(0);
			bottomBorderProperty[i] = new SimpleIntegerProperty(0);
			cssString[i] = new ReadOnlyStringWrapper();

			cssString[i]
					.bind(Bindings.createStringBinding(
							() -> String.format(
									"-fx-padding: 0 -8 0 -8;" + "-fx-border-color: " + colors[i]
											+ ";-fx-border-width: 0 0 %d 0;" + "-fx-border-insets: %d %d 0 %d",
									bottomBorderProperty[i].get(), bottomBorderProperty[i].get(),
									bottomInsetsProperty[i].get(), bottomInsetsProperty[i].get()),
							bottomBorderProperty[i], bottomInsetsProperty[i]));

			menuButtons[i].setRipplerFill(Color.web(colors[i]));
			menuButtons[i].styleProperty().bind(cssString[i].getReadOnlyProperty());

			menuButtons[i].setOnMousePressed(e -> {
				if (selectedButton != -1) {
					menuButtons[selectedButton].pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
					bottomBorderProperty[selectedButton].set(0);
				}
				if (i != selectedButton) {
					setMenuComponent(menuComponentRoot[i]);
					selectedButton = i;
				}
				menuButtons[i].pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);

				bottomBorderProperty[i].set(borderHeight);
				Timeline t = new Timeline(
						new KeyFrame(d, new KeyValue(bottomInsetsProperty[i], 0, Interpolator.EASE_BOTH)));
				t.play();

			}// }

			);
			menuButtons[i].setOnMouseExited(e -> {
				if (bottomBorderProperty[i].get() != 0) {
					Timeline t = new Timeline(new KeyFrame(d,
							new KeyValue(bottomInsetsProperty[i], computeJFXButtonInsetsWidth(menuButtons[i]))));
					t.play();

				}
			});
			menuButtons[i].setOnMouseEntered(e -> {
				if (bottomBorderProperty[i].get() != 0) {
					Timeline t = new Timeline(new KeyFrame(d, new KeyValue(bottomInsetsProperty[i], 0)));
					t.play();
				}

			});
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource(String.format("VueMenuRoot%d.fxml", i)));

				loader.setController(this);
				menuComponentRoot[i] = loader.load();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String menuComponentStyle = "-fx-background-color:transparent";
		menuComponentButtons[0] = new JFXButton[] { jfxNewWindow, jfxRestart, jfxExit };
		menuComponentButtons[1] = new JFXButton[] { jfxNewEmptySheet, jfxNewLayer, jfxMusicSheet1, jfxMelodicContour1,
				jfxTogglePlay };
		menuComponentButtons[2] = new JFXButton[] { jfxKey, jfxTempo, jfxVolume, jfxTonguing, jfxTone, jfxDeleteLayer,
				jfxNoteInfo, jfxNoteSine };
		menuComponentButtons[3] = new JFXButton[] { jfxNewRecording, jfxToggleRecording, jfxDeleteRecording };
		menuComponentButtons[4] = new JFXButton[] { jfxMusicSheet4, jfxMelodicContour4, jfxSpectralAnalysis,
				jfxWaveForm, jfxTableView, jfxPianoVisualizer, jfxBassesRenderer };
		menuComponentButtons[5] = new JFXButton[] { jfxHelp, jfxAbout, jfxHomepage };

		int incr = 16;
		int h = 64;
		for (int j = 0; j < nMenuButtons; j++)
			for (int k = 0, s = menuComponentButtons[j].length; k < s; k++) {
				JFXButton btn = menuComponentButtons[j][k];
				btn.setStyle(menuComponentStyle);
				boldButtonWidthCorrection(btn, btn, incr);
				setButtonHeight(btn, h);

				btn.setOnMouseEntered(e -> {
					((JFXButton) e.getSource()).getGraphic().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
				});
				btn.setOnMouseExited(e -> {
					((JFXButton) e.getSource()).getGraphic().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
				});

			}

		for (JFXButton arrow : new JFXButton[] { jfxBassesRendererArrow, jfxNewLayerArrow }) {
			arrow.setStyle(menuComponentStyle);
			setButtonHeight(arrow, h);
		}

		final EventHandler<? super MouseEvent> h1 = jfxMusicSheet4.getOnMouseEntered();
		jfxMusicSheet4.setOnMouseEntered(e -> {
			if (!(_wndptr_drawnCanvas instanceof VueMusicSheet))
				h1.handle(e);
		});
		final EventHandler<? super MouseEvent> h11 = jfxMusicSheet4.getOnMouseExited();
		jfxMusicSheet4.setOnMouseExited(e -> {
			if (!(_wndptr_drawnCanvas instanceof VueMusicSheet))
				h11.handle(e);
		});

		final EventHandler<? super MouseEvent> h2 = jfxMelodicContour4.getOnMouseEntered();
		jfxMelodicContour4.setOnMouseEntered(e -> {
			if (!(_wndptr_drawnCanvas instanceof VueContourMelodique))
				h2.handle(e);
		});
		final EventHandler<? super MouseEvent> h22 = jfxMelodicContour4.getOnMouseExited();
		jfxMelodicContour4.setOnMouseExited(e -> {
			if (!(_wndptr_drawnCanvas instanceof VueContourMelodique))
				h22.handle(e);
		});

		jfxMusicSheet1.setOnMouseEntered(e -> jfxMusicSheet4.getOnMouseEntered().handle(e));
		jfxMusicSheet1.setOnMouseExited(e -> jfxMusicSheet4.getOnMouseExited().handle(e));
		jfxMelodicContour1.setOnMouseEntered(e -> jfxMelodicContour4.getOnMouseEntered().handle(e));
		jfxMelodicContour1.setOnMouseExited(e -> jfxMelodicContour4.getOnMouseExited().handle(e));

		jfxTogglePlay.getGraphic().pseudoClassStateChanged(SELECTED2_PSEUDO_CLASS, (false));
		jfxToggleRecording.getGraphic().pseudoClassStateChanged(SELECTED2_PSEUDO_CLASS, false);
		jfxTogglePlay.setText(false ? "Arrêter" : "Jouer");
		jfxToggleRecording.setText(false ? "Désactiver l'écoute" : "Activer l'écoute");

		/* Pages évents */

		
		jfxPageList.setOnMouseClicked(e -> {
			
			if (jfxPageList.getSelectionModel().getSelectedIndex() >= 0) {
				feuilleCourante = jfxPageList.getSelectionModel().getSelectedIndex();
				isPastSheet = feuilleCourante != jfxPageList.getItems().size()-1;
				jfxPageName.setText(nomPages.get(feuilleCourante));
				if (_wndptr_drawnCanvas instanceof VueMusicSheet) {
					((VueMusicSheet) _wndptr_drawnCanvas).drawAllNotes();
				}
			}

		});
		jfxPagePlus.setOnMouseClicked(e -> {
			if (feuilleCourante < feuillesTaille - 1) {
				feuilleCourante++;
				isPastSheet = feuilleCourante != jfxPageList.getItems().size()-1;
				jfxPageList.getSelectionModel().select(feuilleCourante);
				jfxPageName.setText(nomPages.get(feuilleCourante));
				if (_wndptr_drawnCanvas instanceof VueMusicSheet) {
					((VueMusicSheet) _wndptr_drawnCanvas).drawAllNotes();
				}
			}
		});
		jfxPageMinus.setOnMouseClicked(e -> {
			if (feuilleCourante > 0) {
				feuilleCourante--;
				isPastSheet = feuilleCourante != jfxPageList.getItems().size()-1;
				jfxPageList.getSelectionModel().select(feuilleCourante);
				jfxPageName.setText(nomPages.get(feuilleCourante));
				if (_wndptr_drawnCanvas instanceof VueMusicSheet) {
					((VueMusicSheet) _wndptr_drawnCanvas).drawAllNotes();
				}
			}
		});

		TextField tf = new TextField();
		jfxPageName.setOnMouseClicked(e -> {
			tf.setText(jfxPageName.getText());
			tf.selectAll();
			jfxPageName.setText("");
			jfxPageName.setGraphic(tf);

		});
		tf.setOnAction(ae -> {
			nomPages.set(feuilleCourante, tf.getText());
			jfxPageList.getItems().get(feuilleCourante)
					.setText(String.format("%03d: %s", feuilleCourante + 1, tf.getText()));
			jfxPageName.setText(tf.getText());
			jfxPageName.setGraphic(null);
		});

		jfxTogglePlay.setDisable(true);
		jfxToggleRecording.setDisable(true);

		menuButtons[1].getOnMousePressed().handle(null);
		setMenuComponent(menuComponentRoot[1]);

		// showMusicSheet();
		setDisableLayerButtons(true);
		showHomepage();
	}

	// TODO
	public void ecouteurNouvelleFeuille() {
		feuillesTaille++;
		
		if (!isPastSheet) {
			feuilleCourante = feuillesTaille - 1;
			jfxPageList.getSelectionModel().select(feuilleCourante);
		}
		
		nomPages.add("");
		jfxPageList.getItems().add(new Label(String.format("%03d:", feuillesTaille)));
		jfxPageList.scrollTo(jfxPageList.getItems().size()-1);
	}

	private void setButtonHeight(Control modifier, int h) {
		modifier.setPrefHeight(h);
		modifier.setMaxHeight(h);
		modifier.setMinHeight(h);
	}

	private void setButtonWidth(Control modifier, double w) {
		modifier.setPrefWidth(w);
		modifier.setMaxWidth(w);
		modifier.setMinWidth(w);
	}

	private void boldButtonWidthCorrection(Control modifier, Button src, int increment) {
		double w = computeBoldButtonTextWidth(src) + increment;
		modifier.setPrefWidth(w);
		modifier.setMaxWidth(w);
		modifier.setMinWidth(w);

	}

	private double computeBoldButtonTextWidth(Button btn) {
		Text txt = new Text(btn.getText());
		Font font = btn.getFont();
		txt.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));
		txt.applyCss();
		return txt.getLayoutBounds().getWidth();
	}

	private int computeJFXButtonInsetsWidth(JFXButton btn) {
		return (int) ((btn.getWidth() - computeBoldButtonTextWidth(btn)) / 2);
	}

	@FXML
	private void openBassesRendererArrow(ActionEvent pe) {

		double s = jfxBassesRendererArrow.getWidth() + jfxBassesRenderer.getWidth();
		VBox list = new VBox();
		for (int hz : new int[] { 130, 250 }) {
			JFXButton b = new JFXButton(String.format("%d Hz", hz));
			b.setId(Integer.toString(hz));
			b.setOnAction(e -> showBassesRenderer(e));
			setButtonWidth(b, s);
			list.getChildren().add(b);

		}

		list.setPrefWidth(s);

		JFXPopup popup = new JFXPopup();

		popup.setPopupContent(list);
		popup.show(jfxBassesRendererArrow, PopupVPosition.TOP, PopupHPosition.RIGHT, 0, 64);

	}

	@FXML
	private void openNewLayerArrow(ActionEvent pe) {

		double s = jfxNewLayerArrow.getWidth() + jfxNewLayer.getWidth();
		VBox list = new VBox();
		for (String str : new String[] { "Fichier audio", "Enregistrement" }) {
			JFXButton b = new JFXButton(str);

			setButtonWidth(b, s);
			list.getChildren().add(b);

		}
		((JFXButton) list.getChildren().get(0)).setOnAction(e -> newLayer());
		((JFXButton) list.getChildren().get(1)).setOnAction(e -> rawNewLayer(1, null));
		list.setPrefWidth(s);

		JFXPopup popup = new JFXPopup();

		popup.setPopupContent(list);
		popup.show(jfxNewLayerArrow, PopupVPosition.TOP, PopupHPosition.RIGHT, 0, 64);

	}

	@FXML
	private void newWindow() {
		try {
			Runtime.getRuntime().exec(getRunnableJarString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void restart() {
		// execute the command in a shutdown hook, to be sure that all the
		// resources have been disposed before restarting the application
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					Runtime.getRuntime().exec(getRunnableJarString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		exit();
	}

	// https://dzone.com/articles/programmatically-restart-java
	public static final String SUN_JAVA_COMMAND = "sun.java.command";

	private String getRunnableJarString() {
		// java binary
		String java = System.getProperty("java.home") + "/bin/java";
		// vm arguments
		List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		StringBuffer vmArgsOneLine = new StringBuffer();
		for (String arg : vmArguments) {
			// if it's the agent argument : we ignore it otherwise the
			// address of the old application and the new one will be in conflict
			if (!arg.contains("-agentlib")) {
				vmArgsOneLine.append(arg);
				vmArgsOneLine.append(" ");
			}
		}
		// init the command to execute, add the vm args
		final StringBuffer cmd = new StringBuffer("\"" + java + "\" " + vmArgsOneLine);

		// program main and program arguments
		String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
		// program main is a jar
		if (mainCommand[0].endsWith(".jar")) {
			// if it's a jar, add -jar mainJar
			cmd.append("-jar " + new File(mainCommand[0]).getPath());
		} else {
			// else it's a .class, add the classpath and mainClass
			cmd.append("-cp \"" + System.getProperty("java.class.path") + "\" " + mainCommand[0]);
		}
		// finally add program arguments
		for (int i = 1; i < mainCommand.length; i++) {
			cmd.append(" ");
			cmd.append(mainCommand[i]);
		}
		return cmd.toString();
	}

	@FXML
	private void exit() {
		Platform.exit();
		System.exit(0);
	}

	@FXML
	private void toggleMenuComponent() {
		jfxHider.getGraphic().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS,
				showMenuComponentRoot = !showMenuComponentRoot);
		Duration d = Duration.millis(200);

		Timeline t1;
		if (showMenuComponentRoot) {
			hboxMenuBar.setStyle("");

			t1 = new Timeline(new KeyFrame(d, new KeyValue(stackPane1.translateYProperty(), 0, Interpolator.EASE_BOTH),
					new KeyValue(borderPane1.translateYProperty(), 0, Interpolator.EASE_BOTH),
					new KeyValue(leftPane.translateYProperty(), 0, Interpolator.EASE_BOTH)));
			t1.setOnFinished(e -> stackPane1.setMinHeight(-1));
		} else {
			hboxMenuBar.setStyle("-fx-background-color:-fx-base");

			stackPane1.setMinHeight(Short.MAX_VALUE);
			t1 = new Timeline(new KeyFrame(d,

					new KeyValue(stackPane1.translateYProperty(), -90, Interpolator.EASE_BOTH),
					new KeyValue(borderPane1.translateYProperty(), -90, Interpolator.EASE_BOTH),
					new KeyValue(leftPane.translateYProperty(), -90, Interpolator.EASE_BOTH)));

		}
		t1.setCycleCount(1);
		t1.play();
	}

	@FXML
	private void toggleLeftPane() {
		jfxPageHider.getGraphic().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, showLeftPane = !showLeftPane);
		Duration d = Duration.millis(200);
		Timeline t1;
		double w = -leftPane.getWidth() + jfxPageHider.getMaxWidth();
		if (showLeftPane) {

			t1 = new Timeline(new KeyFrame(d, new KeyValue(stackPane1.translateXProperty(), 0, Interpolator.EASE_BOTH),
					new KeyValue(leftPane.translateXProperty(), 0, Interpolator.EASE_BOTH)));
			t1.setOnFinished(e -> stackPane1.setMinWidth(-1));
		} else {

			stackPane1.setMinWidth(Short.MAX_VALUE);
			t1 = new Timeline(new KeyFrame(d, new KeyValue(stackPane1.translateXProperty(), w, Interpolator.EASE_BOTH),
					new KeyValue(leftPane.translateXProperty(), w, Interpolator.EASE_BOTH)));
		}
		t1.setCycleCount(1);
		t1.play();
	}

	private void registerWindow(IVueTask wndptr, JFXButton btnptr) {
		btnptr.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
		wndCache.add(wndptr);
		EventHandler<WindowEvent> h = wndptr.getStage().getOnCloseRequest();
		wndptr.getStage().setOnCloseRequest(e -> {
			h.handle(e);
			btnptr.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
		});
	}

	@FXML
	private void showHomepage() {

		Pane p = null;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueAccueil.fxml"));
			loader.setController(this);
			p = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setDrawnComponent(p);
	}
	
	@FXML
	private void showAbout() {

		JFXDialog dialog = new JFXDialog();
		Pane p = null;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueAbout.fxml"));
			loader.setController(this);
			p = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		p.setOnMouseClicked(e->dialog.close());
		
		dialog.setContent(p);
		dialog.getContent().setStyle("-fx-background-color: -fx-base");
		dialog.show(root);
		
	}

	@FXML
	private void showSpectrumAnalysis() {
		if (_wndptr_spectralAnalyser == null || _wndptr_spectralAnalyser.closed()) {
			_wndptr_spectralAnalyser = new VueAnalyseSpectrale(this, this.controleur);
			registerWindow(_wndptr_spectralAnalyser, jfxSpectralAnalysis);
		} else
			_wndptr_spectralAnalyser.requestFocus();
	}

	@FXML
	private void showBassesRenderer(ActionEvent e) {
		int bassThreshold = Integer.parseInt(((Node) e.getSource()).getId());
		if (_wndptr_bassesRenderer == null || _wndptr_bassesRenderer.closed()) {
			_wndptr_bassesRenderer = new VueS(this, bassThreshold);
			registerWindow(_wndptr_bassesRenderer, jfxBassesRenderer);
		} else {
			_wndptr_bassesRenderer.requestFocus();
			((VueS) _wndptr_bassesRenderer).setBassThreshold(bassThreshold);
		}
	}

	@FXML
	private void showWaveform() {

		if (_wndptr_waveform == null || _wndptr_waveform.closed()) {
			_wndptr_waveform = new VueFormeOnde(this, this.controleur);
			registerWindow(_wndptr_waveform, jfxWaveForm);
		} else
			_wndptr_waveform.requestFocus();
	}

	@FXML
	private void showPianoVisualizer() {
		if (_wndptr_pianoVisualizer == null || _wndptr_pianoVisualizer.closed()) {
			_wndptr_pianoVisualizer = new VuePianoVisualizer(this, this.controleur);
			registerWindow(_wndptr_pianoVisualizer, jfxPianoVisualizer);
		} else
			_wndptr_pianoVisualizer.requestFocus();
	}

	@FXML
	private void showMelodicContour() {
		for (Node n : new Node[] { jfxMusicSheet4, jfxMusicSheet4.getGraphic(), jfxMusicSheet1,
				jfxMusicSheet1.getGraphic() })
			n.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
		for (Node n : new Node[] { jfxMelodicContour4, jfxMelodicContour4.getGraphic(), jfxMelodicContour1,
				jfxMelodicContour1.getGraphic(), })
			n.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);

		if (!(_wndptr_drawnCanvas instanceof VueContourMelodique)) {
			wndCache.remove(_wndptr_drawnCanvas);
			_wndptr_drawnCanvas = new VueContourMelodique(this, this.controleur);
			wndCache.add(_wndptr_drawnCanvas);
		}
	}

	@FXML
	private void showMusicSheet() {
		for (Node n : new Node[] { jfxMusicSheet4, jfxMusicSheet4.getGraphic(), jfxMusicSheet1,
				jfxMusicSheet1.getGraphic() })
			n.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
		for (Node n : new Node[] { jfxMelodicContour4, jfxMelodicContour4.getGraphic(), jfxMelodicContour1,
				jfxMelodicContour1.getGraphic(), })
			n.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);

		if (!(_wndptr_drawnCanvas instanceof VueMusicSheet)) {
			wndCache.remove(_wndptr_drawnCanvas);
			_wndptr_drawnCanvas = new VueMusicSheet(this, this.controleur, this.controleur.getMusicFactory());
			wndCache.add(_wndptr_drawnCanvas);
		}
	}

	@FXML
	private void showTableView() {
		if (_wndptr_tableView == null || _wndptr_tableView.closed()) {
			_wndptr_tableView = new VueTableView(this, this.controleur);
			registerWindow(_wndptr_tableView, jfxTableView);
		} else
			_wndptr_tableView.requestFocus();
	}

	@FXML
	private void showCHM() {
		try {
			String[] commands = { "cmd", "/c", new File(CHM_FILENAME).getAbsolutePath() };
			Runtime.getRuntime().exec(commands);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void showNotes() {
		if (_wndptr_drawnCanvas instanceof VueMusicSheet) {
			List<Note> notes = ((VueMusicSheet) _wndptr_drawnCanvas).getSelectedNotes();
			showNote(notes.get(notes.size() - 1));
		}
	}

	@FXML
	public void showSines() {
		if (_wndptr_drawnCanvas instanceof VueMusicSheet) {
			List<Note> notes = ((VueMusicSheet) _wndptr_drawnCanvas).getSelectedNotes();
			showSine(notes.get(notes.size() - 1));
		}
	}

	public void showNote(Note note) {

		if (_wndptr_noteInfos == null || _wndptr_noteInfos.closed()) {
			_wndptr_noteInfos = new VueNote(this, this.controleur, note);
			wndCache.add(_wndptr_noteInfos);
		} else {
			_wndptr_noteInfos.requestFocus();
			((VueNote) _wndptr_noteInfos).setNote(note);
		}
	}

	public void showSine(Note note) {
		if (_wndptr_sine == null || _wndptr_sine.closed()) {
			_wndptr_sine = new VueSine(this, lastDataModele);
			wndCache.add(_wndptr_sine);
		} else {
			_wndptr_sine.requestFocus();
			((VueSine) _wndptr_sine).setWave(lastDataModele);
		}

	}

	@Deprecated
	@FXML
	private void loadAudioFile() {
		File f = fileChooser.showOpenDialog(root.getScene().getWindow());
		if (f != null)
			controleur.ecouteurLoadAudioFile(f);
	}

	@FXML
	private void newLayer() {
		File f = fileChooser.showOpenDialog(root.getScene().getWindow());
		if (f != null)
			rawNewLayer(0, f);
	}

	private void rawNewLayer(int type, File pFile) {
		int indice = controleur.ecouteurNewLayer(type, pFile);

		if (indice == -1 || (currentLayerIndex == 0 && indice == 0))
			return;

		playing.add(type == 0 ? ThreadModele.AUTO_PLAY_FILE : ThreadModele.AUTO_RECORD);
		volume.add(100d);
		boolean currentLayerPlaying = playing.get(playing.size() - 1);

		currentLayerIndex = indice;

		jfxTogglePlay.getGraphic().pseudoClassStateChanged(SELECTED2_PSEUDO_CLASS, (currentLayerPlaying));

		jfxTogglePlay.setText(currentLayerPlaying ? "Arrêter" : "Jouer");

		String txt = type == 0 ? "Calque " + ++nLayerFile : "Enregistrement " + ++nLayerMicro;
		JFXButton btn = new JFXButton(txt);
		btn.getStyleClass().add("layer");
		btn.setPrefHeight(50);
		btn.setAlignment(Pos.CENTER_LEFT);
		MaterialIconView icon = new MaterialIconView();
		icon.setGlyphSize(28);
		icon.setGlyphName(type == 0 ? "LIBRARY_MUSIC" : "VIDEO_LIBRARY");
		icon.getStyleClass().add(type == 0 ? "iconLayerFile" : "icon3");
		btn.setGraphic(icon);
		btn.setContentDisplay(ContentDisplay.LEFT);
		btn.setGraphicTextGap(60 - computeBoldButtonTextWidth(btn) / 2);

		Color fxBase = Color.web("aliceblue");

		IntegerProperty r = new SimpleIntegerProperty((int) (255 * fxBase.getRed()));
		IntegerProperty g = new SimpleIntegerProperty((int) (255 * fxBase.getGreen()));
		IntegerProperty b = new SimpleIntegerProperty((int) (255 * fxBase.getBlue()));

		ReadOnlyStringWrapper cssString = new ReadOnlyStringWrapper();
		cssString.bind(Bindings.createStringBinding(
				() -> String.format("-fx-background-color: rgb(%d,%d,%d)", r.get(), g.get(), b.get()), r, g, b));

		Timeline t = new Timeline(
				new KeyFrame(Duration.millis(300), new KeyValue(r, 135), new KeyValue(g, 206), new KeyValue(b, 250)));
		t.setCycleCount(Timeline.INDEFINITE);
		t.setAutoReverse(true);
		btn.setOnMouseEntered(e -> {

			if (currentLayerIndex != layersVbox.getChildren().indexOf(btn)) {

				btn.styleProperty().bind(cssString.getReadOnlyProperty());
				t.play();
			}
		});
		btn.setOnMouseExited(e -> {
			if (currentLayerIndex != layersVbox.getChildren().indexOf(btn)) {
				t.stop();
				r.set((int) (255 * fxBase.getRed()));
				g.set((int) (255 * fxBase.getGreen()));
				b.set((int) (255 * fxBase.getBlue()));
				btn.styleProperty().unbind();
			}
		});

		btn.setOnMouseClicked(e -> {
			btn.getOnMouseExited().handle(null);

			for (Node n : layersVbox.getChildren()) {
				((Button) n).pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
				((Button) n).getGraphic().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
			}
			btn.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
			icon.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
			currentLayerIndex = layersVbox.getChildren().indexOf(btn);

			boolean layerPlaying = playing.get(layersVbox.getChildren().indexOf(btn));

			jfxTogglePlay.getGraphic().pseudoClassStateChanged(SELECTED2_PSEUDO_CLASS, layerPlaying);
			jfxTogglePlay.setText(layerPlaying ? "Arrêter" : "Jouer");

			boolean toggleRecording = type == 0;
			jfxDeleteRecording.setDisable(toggleRecording);
			jfxToggleRecording.setDisable(toggleRecording);

			controleur.ecouteurSetLayer(layersVbox.getChildren().indexOf(btn));

			if (e != null && e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
				TextField tf = new TextField();
				final Node cache = btn.getGraphic();

				tf.setText(btn.getText());
				tf.selectAll();
				btn.setText("");
				btn.setGraphic(tf);

				tf.setOnAction(ae -> {
					btn.setText(tf.getText());
					btn.setGraphic(cache);
				});
			}
		});
		setButtonWidth(btn, layersVbox.getWidth());
		layersVbox.getChildren().add(btn);

		if (indice == 0) {

			setDisableLayerButtons(false);
			showMusicSheet();

			initPageParameters();
			
		}

		btn.getOnMouseClicked().handle(null);

	}
	// TODO
	private void initPageParameters() {
		((VueMusicSheet) _wndptr_drawnCanvas).clear();
		
		feuillesTaille = 1;
		feuilleCourante = 0;
		isPastSheet = false;
		nomPages = new ArrayList<>();
		nomPages.add("");
		jfxPageName.setText("");
		jfxPageList.getItems().clear();
		jfxPageList.getItems().add(new Label("001: "));
	}

	@Override
	public void disposeWindow(IVueTask wndptr) {
		wndCache.remove(wndptr);
	}

	@FXML
	public void togglePlay(ActionEvent e) {

		if (controleur.ecouteurTogglePlay()) {
			playing.set(currentLayerIndex, !playing.get(currentLayerIndex));
			boolean p = playing.get(currentLayerIndex);

			jfxTogglePlay.getGraphic().pseudoClassStateChanged(SELECTED2_PSEUDO_CLASS, p);
			jfxTogglePlay.setText(p ? "Arrêter" : "Jouer");

			jfxToggleRecording.getGraphic().pseudoClassStateChanged(SELECTED2_PSEUDO_CLASS, p);
			jfxToggleRecording.setText(p ? "Désactiver l'écoute" : "Activer l'écoute");
		}

	}

	@FXML
	private void setTempo() {
		JFXDialog dialog = new JFXDialog();

		VBox vbox = new VBox();

		TextField tempo = new TextField();
		tempo.setText(Integer.toString(controleur.getTempo()));
		tempo.setStyle("-fx-border-radius: 0; -fx-background-radius: 0;");
		JFXButton bt = new JFXButton("Ok");
		bt.setOnAction(e -> tempo.getOnAction().handle(e));
		setButtonWidth(bt, 210);

		tempo.setOnAction(e -> {
			try {
				int intTempo = Integer.parseInt(tempo.getText());

				if (intTempo > 0) {
					controleur.setTempo(intTempo);
					dialog.close();
				} else

				{
					tempo.setText(Integer.toString(controleur.getTempo()));
					VueAlert.sendAlert("Erreur", "L'entrée doit être un nombre entier positif!", AlertType.WARNING);
				}
			} catch (NumberFormatException w) {
				tempo.setText(Integer.toString(controleur.getTempo()));
				VueAlert.sendAlert("Erreur", "L'entrée doit être un nombre entier positif!", AlertType.WARNING);
			}

		});

		vbox.getChildren().addAll(new Label("Entrer le tempo"), tempo, bt);
		vbox.setAlignment(Pos.TOP_CENTER);
		dialog.setContent(vbox);
		dialog.getContent().setStyle("-fx-background-color: -fx-base");
		dialog.show(root);

	}

	private void setRawKey(boolean closeOnSelection) {
		JFXDialog dialog = new JFXDialog();

		VBox vbox = new VBox();

		HBox hbox = new HBox();
		double max = 0, current = 0;
		String[] clefsNom = new String[] { "Clef de sol", "Clef d'ut", "Clef de fa" };
		for (int i = 0; i < clefsNom.length; i++) {
			JFXButton b = new JFXButton(clefsNom[i]);
			final int j = i;
			b.setOnAction(e -> {
				lastClef = j;

				if (closeOnSelection)
					dialog.close();
				else if (_wndptr_drawnCanvas instanceof VueMusicSheet) {
					((VueMusicSheet) _wndptr_drawnCanvas).setLastClef(lastClef);

				}

			});
			Image img = new Image(new File("res/highresClef" + (i + 1) + ".png").toURI().toString());
			ImageView a = new ImageView(img);
			a.setFitWidth(30);
			max = Math.max(max, current = 30 * img.getHeight() / img.getWidth());
			a.setFitHeight(current);
			b.setGraphic(a);

			hbox.getChildren().add(b);
		}

		for (Node n : hbox.getChildren()) {
			Button btn = (Button) n;
			setButtonHeight(btn, (int) max + 10);
			boldButtonWidthCorrection(btn, btn, 22 + 30 + 4);
		}
		vbox.getChildren().addAll(new Label("Choisir une clé"), hbox);
		vbox.setAlignment(Pos.TOP_CENTER);

		dialog.setContent(vbox);
		dialog.getContent().setStyle("-fx-background-color: -fx-base");
		dialog.show(root);
	}

	@FXML
	private void setKey() {
		setRawKey(false);
	}

	@FXML
	private void newEmptySheet() {
		JFXDialog dialog = new JFXDialog();

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueNewEmptySheet.fxml"));
			loader.setController(this);
			dialog.setContent(loader.load());

		} catch (IOException e) {
			e.printStackTrace();
		}

		dialog.getContent().setStyle("-fx-background-color: -fx-base");
		jfxNewEmptySheetKey.setOnAction(e -> setRawKey(true));
		jfxNewEmptySheetCancel.setOnAction(e -> dialog.close());
		jfxNewEmptySheetAccept.setOnAction(e -> {
			try {
				int intTempo = Integer.parseInt(jfxNewEmptySheetTempo.getText());

				if (intTempo > 0) {
					controleur.setTempo(intTempo);
					dialog.close();
				} else

				{
					jfxNewEmptySheetTempo.setText(Integer.toString(controleur.getTempo()));
					VueAlert.sendAlert("Erreur", "L'entrée doit être un nombre entier positif!", AlertType.WARNING);
					return;
				}
			} catch (NumberFormatException w) {
				jfxNewEmptySheetTempo.setText(Integer.toString(controleur.getTempo()));
				VueAlert.sendAlert("Erreur", "L'entrée doit être un nombre entier positif!", AlertType.WARNING);
				return;
			}

			lblSheetTitle.setText(
					jfxNewEmptySheetName.getText().isEmpty() ? "Feuille de musique" : jfxNewEmptySheetName.getText());
			showMusicSheet();
			initPageParameters();

			dialog.close();

		});
		dialog.show(root);
	}

	@FXML
	private void setVolume() {
		JFXDialog dialog = new JFXDialog();
		
		VBox vbox = new VBox();

		JFXSlider slider = new JFXSlider();

		slider.setValue(volume.size() > 0 ? volume.get(currentLayerIndex) : 100);

		slider.valueProperty().addListener((bs, o, n) -> controleur.ecouteurSetVolume(n.doubleValue()));
		// slider.
		// tempo.setStyle("-fx-border-radius: 0; -fx-background-radius: 0;");

		JFXButton bt = new JFXButton("Ok");
		setButtonWidth(bt, 300);
		bt.setOnAction(e -> dialog.close());

		vbox.getChildren().addAll(new Label("Ajuster le volume du calque courant"), slider, bt);
		vbox.setAlignment(Pos.TOP_CENTER);
		dialog.setContent(vbox);
		dialog.getContent().setStyle("-fx-background-color: -fx-base");
		dialog.show(root);
	}

	@FXML
	private void setTone() {
		JFXDialog dialog = new JFXDialog();
		
		VBox vbox = new VBox();

		JFXComboBox<NoteEnum> a = new JFXComboBox();
		a.setPromptText("Choisir une tonalité");
		a.setItems(NoteEnum.getItems());
		a.setOnAction(e -> {
			if (_wndptr_drawnCanvas instanceof VueMusicSheet) {
				
				controleur.setKey(a.getValue());
				
				((VueMusicSheet)_wndptr_drawnCanvas).drawAllNotes();
			}
			
		});

		a.setStyle("-fx-border-radius: 0; -fx-background-radius: 0;");

		JFXButton bt = new JFXButton("Ok");

		bt.setOnAction(e -> dialog.close());

		vbox.getChildren().addAll(new Label("Ajuster la tonalité"), a, bt);
		vbox.setAlignment(Pos.TOP_CENTER);
		setButtonWidth(bt, 300);
		dialog.setContent(vbox);
		dialog.getContent().setStyle("-fx-background-color: -fx-base");
		dialog.show(root);
	}

	@FXML
	private void setTonguing() {
		JFXDialog dialog = new JFXDialog();

		
		VBox vbox = new VBox();

		JFXButton a = new JFXButton("Oui");
		a.setOnAction(e -> {
			controleur.ecouteurSetDetectTonguing(true);
			dialog.close();
		});

		JFXButton b = new JFXButton("Non");
		b.setOnAction(e -> {
			controleur.ecouteurSetDetectTonguing(false);
			dialog.close();
		});

		vbox.getChildren().addAll(new Label(
				"[BETA] Détection tonguing\nVérifier s'il y a présence de vibrato à basse fréquence (4.9Hz-8.6Hz)\ndans le contour mélodique.\nVoir la série temporelle>Note jouée."),
				a, b);
		double w = 380;
		setButtonWidth(b, w);
		setButtonWidth(a, w);
		vbox.setAlignment(Pos.TOP_CENTER);
		dialog.setContent(vbox);
		dialog.getContent().setStyle("-fx-background-color: -fx-base");
		dialog.show(root);
	}

	@FXML
	private void deleteCurrentLayer() {
		if (currentLayerIndex != -1) {
			
			playing.remove(currentLayerIndex);
			volume.remove(currentLayerIndex);

			layersVbox.getChildren().remove(currentLayerIndex);

			currentLayerIndex = controleur.ecouteurDeleteCurrentLayer();

			if (currentLayerIndex != -1)
				((Button) layersVbox.getChildren().get(currentLayerIndex)).getOnMouseClicked().handle(null);

			if (layersVbox.getChildren().isEmpty())
				setDisableLayerButtons(true);

		}
	}

	public void setDisableSelectNote(boolean disable) {
		jfxNoteInfo.setDisable(disable);
		jfxNoteSine.setDisable(disable);
	}

	private void setDisableLayerButtons(boolean disable) {
		if (disable)
			setDrawnComponent(null);

		setDisableSelectNote(disable);
		leftPane.setVisible(!disable);

		for (JFXButton btn : new JFXButton[] { jfxTone, jfxTonguing, jfxDeleteLayer, jfxDeleteRecording, jfxVolume,
				jfxMusicSheet1, jfxMelodicContour1, jfxTogglePlay, jfxToggleRecording, jfxTempo, jfxKey })
			btn.setDisable(disable);

		for (JFXButton btn : menuComponentButtons[4])
			btn.setDisable(disable);
		jfxBassesRendererArrow.setDisable(disable);
	}

	@FXML
	private void newRecording() {
		rawNewLayer(1, null);
	}

	@FXML
	private void toggleRecording(ActionEvent e) {
		togglePlay(e);
	}

	@Override
	public DataModele getLastDataModele() {
		return lastDataModele;
	}

	@Override
	public void ajouterNote() {

		if (_wndptr_drawnCanvas instanceof VueMusicSheet)
			((VueMusicSheet) _wndptr_drawnCanvas).drawLastNote();
	}

	@Override
	public int getLastClef() {

		return lastClef;
	}

}
