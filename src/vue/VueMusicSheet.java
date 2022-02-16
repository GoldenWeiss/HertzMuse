package vue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import controleur.IControleurC;
import javafx.animation.FadeTransition;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.QuadCurve;
import javafx.util.Duration;
import model.music.Measure;
import model.music.MusicFactory;
import model.music.MusicSheet;
import model.music.Note;
import model.utility.Fraction;

public class VueMusicSheet extends IVueTask {

	private static final int LINE_PER_OCTAVE = 7;
	private static final int HEAD_HEIGHT = 10;

	private static final double EXTRA_LINE_WIDTH = 16;
	private static final int NOTES_SPACING = 40;
	private static final int FIRST_NOTE_DISTANCE = 50;
	private static final int MEASURE_DISTANCE = 20;

	private static final int FIRST_STAFF_POSITION = 5 * HEAD_HEIGHT + 10;
	private static final int SPACING_LINES = 10 * HEAD_HEIGHT + 20;
	private static final double LINES_HEIGHT = 4 * HEAD_HEIGHT;

	private static final int NBR_DEFAULT_NOTES = 16;
	private static final int N_SHEET_STAFF = 4;

	private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

	private static final int IMG_HEIGHT = 43;
	private static final int START_X = 10;
	private static final int NOTE_SPACING = 40;
	private static final int DOT_X = 10;
	private static final int DOT_Y = 0;
	private static final int TIME_SIGNATURE_INDENT = 30;
	private static final int B_SPACE = 10;

	@FXML
	private Pane root;
	private Image[] cles;
	private Image timeSignature;
	private int positionX = 0;
	private int currentStaffPosition;
	private int lastClef;

	private IVueA vueMere;
	private IControleurC controleur;

	@FXML
	private Canvas canvas1;
	private GraphicsContext gc;

	private MusicSheet currentSheet;
	private int[] clefNotesTranslations;
	private List<Node> noteSpritesCache;
	private int lastYPos;
	private int nbrNotes;
	private boolean lastHeadDown;
	private int currentNumberNote;

	private ScrollPane resizer;

	private int nCurrentSheet;

	private List<Note> selectedNotes;

	private enum NoteImage {

		WHOLE_NOTE(1), HALF_NOTE(2), QUARTER_NOTE(4), EIGHT_NOTE(8);

		private int time;

		private NoteImage(int time) {
			this.time = time;
		}

		public static NoteImage getNoteImage(int time) {
			for (int i = 0; i < NoteImage.values().length; i++)
				if (NoteImage.values()[i].time == time)
					return NoteImage.values()[i];
			return null;
		}
	}

	public VueMusicSheet(IVueA pVueMere, IControleurC pControleur, MusicFactory pMusicFactory) {
		vueMere = pVueMere;
		controleur = pControleur;

		currentSheet = pMusicFactory.getMusicSheet();

		positionX = 0;
		currentStaffPosition = FIRST_STAFF_POSITION;
		lastClef = vueMere.getLastClef();
		lastYPos = 0;
		lastHeadDown = false;
		nbrNotes = NBR_DEFAULT_NOTES;
		nCurrentSheet = 0;
		currentNumberNote = 0;

		selectedNotes = new ArrayList<>();
		noteSpritesCache = new ArrayList<>();
		clefNotesTranslations = new int[3];
		String[] notes = new String[] { "D4", "E3", "F2" };
		for (int i = 0; i < 3; i++)
			clefNotesTranslations[i] = getNoteLine(notes[i]);

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueMusicSheet.fxml"));
			loader.setController(this);
			resizer = loader.load();

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (vueMere.getLastDataModele() != null) {
			update();
			drawAllNotes();
		}
		vueMere.setDrawnComponent(resizer);

	}

	public List<Note> getSelectedNotes() {
		return selectedNotes;
	}

	@FXML
	private void initialize() {
		gc = canvas1.getGraphicsContext2D();

		cles = new Image[3];
		for (int i = 0; i < 3; i++)
			cles[i] = new Image(new File("res/Cle" + (i + 1) + ".png").toURI().toString());

		timeSignature = new Image(new File("res/TimeSignature1.png").toURI().toString());

		drawSheet();

	}

	private void drawStaff() {
		gc.fillRect(0, FIRST_STAFF_POSITION - 22, canvas1.getWidth(), canvas1.getHeight());
		for (int i = 0; i < N_SHEET_STAFF; i++) {

			double y = FIRST_STAFF_POSITION + i * (LINES_HEIGHT + SPACING_LINES);
			for (int j = 0; j < 5; j++) {
				gc.strokeLine(0.5, y + j * HEAD_HEIGHT + 0.5, 800 + 0.5, y + j * HEAD_HEIGHT + 0.5);
			}
			for (int k = 0; k < 800; k += 800 - 1) {
				gc.strokeLine(k + 0.5, y + .05, k + 0.5, y + 4 * HEAD_HEIGHT + 0.5);
			}

			gc.drawImage(cles[lastClef], 0, FIRST_STAFF_POSITION - 10 + i * (LINES_HEIGHT + SPACING_LINES));
			gc.drawImage(timeSignature, TIME_SIGNATURE_INDENT,
					FIRST_STAFF_POSITION - 10 + i * (LINES_HEIGHT + SPACING_LINES));
		}

	}

	private void drawSheet() {
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas1.getWidth(), canvas1.getHeight());
		drawStaff();
	}

	// Dessine la note
	private void drawNote(Note note, Measure measure) {
		drawNote(note, measure, false);
	}

	private void drawNote(Note note, Measure measure, boolean drawingSheet) {

		int positionNote = getNoteLine(note);
		// System.out.println(positionNote);

		int pos = positionNote - clefNotesTranslations[lastClef];

		if (pos <= 13 && pos >= -3) { //|| drawingSheet

			positionX = (positionX + 1) % (nbrNotes + 1);
			if (positionX == 0) {
				positionX = 1;

				nCurrentSheet = (nCurrentSheet + 1) % (N_SHEET_STAFF);
				if (nCurrentSheet == 0 ) {
					currentStaffPosition = FIRST_STAFF_POSITION;

					if (!drawingSheet) {
						clear();
						controleur.ecouteurNouvelleFeuille();
					}
				} else {
					currentStaffPosition += SPACING_LINES + LINES_HEIGHT;
				}
			}
			
			if ((((VueA) vueMere).isPastSheet && !drawingSheet)) //|| (drawingSheet && !(pos <= 13 && pos >= -3)))
				return;
			
			int translate_y = 0;
			String orientation = "";
			boolean headDown = false;
			ImageView imv = null;
			// tete vers le bas
			if (headDown = pos < 5)
			{
				
				translate_y = (int) (currentStaffPosition - IMG_HEIGHT + (3 / (float) 2 * HEAD_HEIGHT));
				orientation = "_UP.png";
			} else
			{ // vers le haut
				translate_y = (int) (currentStaffPosition);
				orientation = "_DOWN.png";
			}

			int dotted = 0;
			Fraction unshowable = null;

			if (note.getNumerator() == 1)
			{
				String url = "res/" + NoteImage.getNoteImage(note.getTime()).toString() + orientation;
				imv = new ImageView(new Image(new File(url).toURI().toString()));
			} else
			{
				List<Fraction> fracs = Fraction.divideInUniteFractions(note.getFractTime());
				Collections.reverse(fracs);

				int time = fracs.get(0).getDenominator();

				if (note.getFractTime().equals(new Fraction(7, 8)))
					;

				for (int i = 0; i < fracs.size(); i++)
				{
					if (unshowable == null)
					{
						if (i == fracs.size() - 1)
							break;

						if (fracs.get(i).getDenominator() * 2 == fracs.get(i + 1).getDenominator())
							dotted++;
						else
							unshowable = new Fraction(0, 1);

					} else
					{
						unshowable = Fraction.add(unshowable, fracs.get(i));
					}
				}

				String url = "res/" + NoteImage.getNoteImage(time).toString() + orientation;
				imv = new ImageView(new Image(new File(url).toURI().toString()));
			}

			imv.setX(START_X + positionX * NOTE_SPACING);
			imv.setY(translate_y + (12 - pos - 1) * 5);

			for (int i = 0; i < dotted; i++)
			{
				int py = translate_y + (12 - pos - 1) * 5;
				double y1 = headDown ? py + IMG_HEIGHT + DOT_Y : py - DOT_Y - HEAD_HEIGHT;
				Circle dot = new Circle(START_X + (positionX + 1) * NOTE_SPACING + DOT_X + i * 5, y1,2);
				noteSpritesCache.add(dot);
				root.getChildren().add(dot);
			}

			if (note.getNoteString().length() == 3)
			{
				int py = translate_y + (12 - pos - 1) * 5;
				double y1 = headDown ? py + IMG_HEIGHT + DOT_Y : py - DOT_Y;
				gc.fillText("b", START_X + positionX * NOTE_SPACING + B_SPACE * 5, y1);
			}

			int demiWidth = (int) imv.getImage().getWidth() / 2;
			int px = FIRST_NOTE_DISTANCE + positionX * NOTES_SPACING - demiWidth,
					py = translate_y + (12 - pos - 1) * 5 - 3 * (HEAD_HEIGHT / 2);
			addNoteSprite(imv, note, px, py);

			if (measure.getNoteAt(0) == note) {
				gc.strokeLine(px - MEASURE_DISTANCE + 0.5, currentStaffPosition + 0.5, px - MEASURE_DISTANCE + 0.5,
						currentStaffPosition + LINES_HEIGHT + 0.5);
			}

			if (note.isHeld() && positionX > 1) {
				int spacing = 3;
				double y1 = (headDown ? py + IMG_HEIGHT + spacing : py - spacing),
						y2 = (lastHeadDown ? lastYPos + IMG_HEIGHT + spacing : lastYPos - spacing);

				QuadCurve q = new QuadCurve(px + demiWidth - 2 + 0.5, (y1) + 0.5,
						px + demiWidth - NOTES_SPACING / 2 + 0.5,
						(y1 + y2) / 2 + (lastHeadDown || headDown ? 1 : -1) * Math.floor(Math
								.sqrt(Math.pow(1d / Math.sqrt(2) * NOTES_SPACING, 2) - Math.pow(NOTES_SPACING / 2, 2)))
								+ 0.5,
						px - NOTES_SPACING + 2 + demiWidth + 0.5, (y2) + 0.5);
				q.setStroke(Color.BLACK);
				q.setFill(Color.TRANSPARENT);
				noteSpritesCache.add(q);
				root.getChildren().add(q);
			}

			if (pos < 0 || pos > 8) {
				gc.setStroke(Color.BLACK);
				gc.setFill(Color.BLACK);
				int sgn = pos < 0 ? -1 : 1;

				for (int i = pos < 0 ? -2 : 8, t = pos < 0 ? -pos -1 : pos + 1; i <= t; i += 2) {
					final double x = px + demiWidth + 0.5,
							y = pos < 0 ? (translate_y + 5 * (12 - sgn * (i - 1)) + 0.5 + IMG_HEIGHT - 2 * HEAD_HEIGHT)
									: translate_y + 5 * (12 - i - 2) + 0.5;
					gc.fillRect(x - EXTRA_LINE_WIDTH / 2, y, EXTRA_LINE_WIDTH, 0.5);
				}
			} 
			lastYPos = py;
			lastHeadDown = headDown;

			if (unshowable != null)
				drawNote(new Note(unshowable, note.getFrequency(), true, note), measure, drawingSheet);

		}

	}

	private void addNoteSprite(ImageView imv, Note note, int px, int py) {
		imv.setX(px);
		imv.setY(py);
		FadeTransition fp = new FadeTransition(Duration.millis(60), imv);
		fp.setFromValue(0);
		fp.setToValue(1);
		fp.play();

		imv.setOnMouseEntered(e -> root.setCursor(Cursor.OPEN_HAND));
		imv.setOnMouseExited(e -> root.setCursor(Cursor.DEFAULT));
		final HBox hb1 = new HBox(imv);
		hb1.getStyleClass().add("noteImage");
		canvas1.setOnMouseClicked(e -> {
			for (Node n : noteSpritesCache)
				n.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
			vueMere.setDisableSelectNote(true);
			selectedNotes.clear();
		});
		hb1.setOnMouseClicked(e -> {
			if (!e.isControlDown())
				canvas1.getOnMouseClicked().handle(e);

			vueMere.setDisableSelectNote(false);
			selectedNotes.add(note);

			hb1.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
			if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2)
				(vueMere).showNote(note);
		});

		ContextMenu contextMenu = new ContextMenu();
		MenuItem m1 = new MenuItem("Voir la note...");
		MenuItem m2 = new MenuItem("Graphique de la fréquence...");
		m1.setOnAction((event) -> (vueMere).showNote(note));
		m2.setOnAction((event) -> (vueMere).showSine(note));
		contextMenu.getItems().addAll(m1, m2);
		hb1.setOnContextMenuRequested(e -> contextMenu.show(hb1, e.getScreenX(), e.getScreenY()));

		hb1.setLayoutX(px);
		hb1.setLayoutY(py);

		root.getChildren().add(hb1);
		noteSpritesCache.add(hb1);

	}

	public int getNoteLine(Note note) {
		return getNoteLine(note.getNoteString());
	}

	public int getNoteLine(String noteString) {
		int position = -1;
		switch (noteString.charAt(0)) {
		case 'C':
			position = 0;
			break;
		case 'D':
			position = 1;
			break;
		case 'E':
			position = 2;
			break;
		case 'F':
			position = 3;
			break;
		case 'G':
			position = 4;
			break;
		case 'A':
			position = 5;
			break;
		case 'B':
			position = 6;
			break;

		}

		int line = noteString.length() == 2 ? Integer.parseInt("" + noteString.charAt(1))
				: Integer.parseInt("" + noteString.charAt(2));

		return (line * LINE_PER_OCTAVE) + position;
	}

	public void drawAllNotes() {
		clear();

		int lowerLimit = ((VueA) vueMere).feuilleCourante * N_SHEET_STAFF * NBR_DEFAULT_NOTES, counter = -1;
		int higherLimit = lowerLimit + N_SHEET_STAFF * NBR_DEFAULT_NOTES - 1;

		Note lastNote = null;
		if (currentSheet.getMeasures().get(0).getNbrNote() == 0)
			return;

		for (Measure measure : currentSheet.getMeasures()) {

			for (int i = 0; i < measure.getNbrNote(); i++) {
				
				Note note = measure.getNoteAt(i);
				int positionNote = getNoteLine(note);
				int pos = positionNote - clefNotesTranslations[lastClef];
				if (pos <= 13 && pos >= -3)
					counter++;
				
				if (counter < lowerLimit)
					continue;

				if (counter > higherLimit)
					return;

					if (note != null)
						drawNote(note, measure, true);
				//}
			}
		}

	}

	public void drawLastNote() {

		int missingNotes = currentSheet.getNbrNote() - currentNumberNote;
		Stack<Measure> measures = new Stack<>();
		List<Measure> currentMeasure = currentSheet.getMeasures();

		int currentMeasureIndex = 1;
		while (missingNotes > 0)
		{
			Measure lastMeasure = currentMeasure.get(currentMeasure.size() - currentMeasureIndex);
			missingNotes -= lastMeasure.getNbrNote();
			currentMeasureIndex++;
			measures.add(lastMeasure);
		}

		int measuresSize = measures.size();
		for (int i = 0; i < measuresSize; i++)
		{
			Measure thisMeasure = measures.pop();
			for (int j = i == 0 ? -1 * missingNotes : 0; j < thisMeasure.getNbrNote(); j++)
			{
				drawNote(thisMeasure.getNoteAt(j), thisMeasure);
			}
		}
		currentNumberNote = currentSheet.getNbrNote();

	}

	private void transposeToClef(int ClefNumber) {
		if (ClefNumber != lastClef) {
			lastClef = ClefNumber;
			rewriteSheet();
		}
	}

	public void clear() {
		vueMere.setDisableSelectNote(true);
		selectedNotes.clear();

		lastClef = vueMere.getLastClef();
		positionX = 0;
		currentStaffPosition = FIRST_STAFF_POSITION;
		gc.clearRect(0, 0, canvas1.getWidth(), canvas1.getHeight());
		root.getChildren().removeAll(noteSpritesCache);
		noteSpritesCache.clear();
		drawSheet();
	}

	// Réécrit la feuille *l'efface et dessine le background
	private void rewriteSheet() {
		clear();
		update();
		drawAllNotes();
	}

	void update() {

	}

	void draw() {
		gc.fillRect(0, 1, 50, 23);
        gc.strokeText("BPM:" + controleur.getTempo(), 2, 15);
	}

	private void setVueDim(double height) {
		root.setPrefHeight(height + 50);

	}

	private void setCanvasDim(double width, double height) {
		canvas1.setWidth(width);
		canvas1.setHeight(height);
	}

	public void setLastClef(int pLastClef) {
		transposeToClef(pLastClef);

	}
}
