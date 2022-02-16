package vue;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import controleur.IControleurC;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.music.Note;
import model.utility.CColor;

public class VuePianoVisualizer extends IVueTask {
	private static final int COLOR_WHITE_NOTE = CColor.toInt(Color.WHITE);
	private static final int COLOR_BLACK_NOTE = CColor.toInt(Color.BLACK);

	private static final int COLOR_WHITE_NOTE_SPACING = CColor.toInt(Color.BLACK);
	private static final int COLOR_PREVIEW = CColor.toInt(Color.BLUE);
	private static final int COLOR_CLICKED = CColor.toInt(Color.DARKBLUE);
	private static final int COLOR_TRANSPARENT = CColor.toInt(Color.TRANSPARENT);

	private static final int SIZE_WHITE_NOTE = 22;
	private static final int SIZE_BLACK_NOTE = 12;

	private static final int SPACING_WHITE_NOTE = 1;
	private static final int SHADOW_OFFSET_BLACK_NOTE = 3;

	private static final int N_WHITE_NOTE = 52;
	private static final int N_BLACK_NOTE = 36;// 36;

	private static final String FILENAME_WHITE_NOTE = "testData/testMp3File/s3.png";
	private static final String FILENAME_BLACK_NOTE = "testData/testMp3File/s4.png";
	private static final String PIANO_KEY_PATH = "testData/PianoNote/";

	@FXML
	private Pane root;
	private WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();
	private List<MediaPlayer> medias;

	@FXML
	Canvas canvas1;
	private GraphicsContext gc;
	private PixelWriter px;
	private int canvasWidth, canvasHeight;
	private int[] buffCache;
	private int[] buff;

	private Image imgWhiteNote;
	private int[] buffWhiteNote;
	private int[] xCacheWhiteNotes;

	private Image imgBlackNote;
	private int[] buffBlackNote;
	private int[] xCacheBlackNotes;

	private IVueA vueMere;
	private IControleurC controleur;
	private QueueNote previewNote;
	private Text[] whiteLabels;
	private Text[] blackLabels;
	private static final Font FONT = Font.font("Times New Roman", FontWeight.BOLD, 12);
	private static final double BLACK_LABEL_Y = 20;
	private static final double WHITE_LABEL_Y = 80;

	private class QueueNote {
		private int index;
		private int duration;
		private boolean isBlack;
		private int color;

		QueueNote(int pIndex, int pDuration, int pColor, boolean pIsBlack) {
			index = pIndex;
			duration = pDuration;
			color = pColor;
			isBlack = pIsBlack;
		}

		public void drawBlack() {
			for (int j = 0, bHeight = (int) imgBlackNote.getHeight(); j < bHeight; j++) {
				for (int i = 0, bWidth = (int) imgBlackNote.getWidth(); i < bWidth - SHADOW_OFFSET_BLACK_NOTE; i++) {
					buff[xCacheBlackNotes[index] + i - (bWidth - SHADOW_OFFSET_BLACK_NOTE) / 2
							+ j * canvasWidth] = CColor.interpolate(buffBlackNote[i + j * bWidth], color, 0.5);
				}
			}
		}

		public void drawWhite() {

			for (int i = 0; i < SIZE_WHITE_NOTE; i++) {
				for (int j = 0; j < canvasHeight; j++) {
					buff[xCacheWhiteNotes[index] + i + j * canvasWidth] = CColor
							.interpolate(buffWhiteNote[i + j * SIZE_WHITE_NOTE], color, 0.8);
				}
			}
		}

	}

	public VuePianoVisualizer(IVueA pVueMere, IControleurC pControleur) {
		super();
		medias = new ArrayList<>();
		vueMere = pVueMere;
		controleur = pControleur;
		wndptr.setOnCloseRequest(e -> {
			close = true;
			vueMere.disposeWindow(this);
		});

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VuePianoVisualizer.fxml"));
			loader.setController(this);
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		wndptr.setScene(new Scene(root));
		wndptr.initOwner(vueMere.getScene().getWindow());
		wndptr.setTitle("Visualisation sur piano");
		wndptr.show();
	}

	@FXML
	private void initialize() {
		gc = canvas1.getGraphicsContext2D();
		px = gc.getPixelWriter();

		canvasWidth = (int) canvas1.getWidth();
		canvasHeight = (int) canvas1.getHeight();

		canvas1.setOnMouseMoved(e -> {
			int n = -1;
			int x = (int) e.getX();
			boolean isWhiteNote = true;

			if (e.getY() < imgBlackNote.getHeight()) {
				if (isWhiteNote = (n = getBlackNoteIndex(x)) == -1)
					n = getWhiteNoteIndex(x);
			} else {
				n = getWhiteNoteIndex(x);
			}

			if (n != -1) {
				previewNote = new QueueNote(n, 0, COLOR_PREVIEW, !isWhiteNote);
			}

		});

		canvas1.setOnMousePressed(e -> {
			if (previewNote != null)
				if (previewNote.isBlack)
					playNote(blackNoteIndexToGlobal(getBlackNoteIndex((int) e.getX())));
				else
					playNote(whiteNoteIndexToGlobal(getWhiteNoteIndex((int) e.getX())));
		});
		canvas1.setOnMouseReleased(e -> {
			if (previewNote != null)
				previewNote.color = COLOR_PREVIEW;
		});
		canvas1.setOnMouseExited(e -> previewNote = null);
		imgWhiteNote = new Image(new File(FILENAME_WHITE_NOTE).toURI().toString());
		buffWhiteNote = new int[(int) imgWhiteNote.getWidth() * (int) imgWhiteNote.getHeight()];
		PixelReader pr = imgWhiteNote.getPixelReader();
		pr.getPixels(0, 0, (int) imgWhiteNote.getWidth(), (int) imgWhiteNote.getHeight(), format, buffWhiteNote, 0,
				(int) imgWhiteNote.getWidth());

		imgBlackNote = new Image(new File(FILENAME_BLACK_NOTE).toURI().toString());
		buffBlackNote = new int[(int) imgBlackNote.getWidth() * (int) imgBlackNote.getHeight()];
		pr = imgBlackNote.getPixelReader();
		pr.getPixels(0, 0, (int) imgBlackNote.getWidth(), (int) imgBlackNote.getHeight(), format, buffBlackNote, 0,
				(int) imgBlackNote.getWidth());

		computeBuffCache();

		update();
	}

	private void computeBuffCache() {
		int s = canvasWidth * canvasHeight;
		buffCache = new int[s];
		for (int i = 0; i < s; i++)
			buffCache[i] = COLOR_WHITE_NOTE;

		xCacheWhiteNotes = new int[N_WHITE_NOTE];
		for (int n = 0; n < N_WHITE_NOTE; n++) {
			xCacheWhiteNotes[n] = SPACING_WHITE_NOTE + (SIZE_WHITE_NOTE + SPACING_WHITE_NOTE) * n;
			for (int j = 0; j < canvasHeight; j++) {
				for (int i = 0, wWidth = (int) imgWhiteNote.getWidth(); i < wWidth; i++)
					buffCache[xCacheWhiteNotes[n] + i + j * canvasWidth] = buffWhiteNote[i + j * wWidth];
			}
		}

		for (int i = 0; i < N_WHITE_NOTE + 1; i++)
			for (int k = 0; k < SPACING_WHITE_NOTE; k++) {
				for (int j = 0; j < canvasHeight; j++) {
					buffCache[(SIZE_WHITE_NOTE + SPACING_WHITE_NOTE) * i + k
							+ j * canvasWidth] = COLOR_WHITE_NOTE_SPACING;
				}
			}

		blackLabels = new Text[N_BLACK_NOTE];
		xCacheBlackNotes = new int[N_BLACK_NOTE];
		for (int i = 0; i < N_BLACK_NOTE; i++) {
			int o = i + 4;
			int t = o % 5;
			int n = t > 1 ? 3 : 2;
			int inc = n % 2;
			int k = t > 1 ? t - 2 : t;

			double dev = (dev = k - (n - 1) / 2d) > 0 ? Math.ceil(dev) : Math.floor(dev);
			xCacheBlackNotes[i] = (i + (o / 5) * 2 + inc) * (SIZE_WHITE_NOTE + SPACING_WHITE_NOTE) + (int) dev;

			blackLabels[i] = new Text(Note.getNoteFromFreq((float) getFrequency(blackNoteIndexToGlobal(i)+9)));
			blackLabels[i].setFont(FONT);
			blackLabels[i].applyCss();
			int w = (int) (blackLabels[i].getLayoutBounds().getWidth() / 2);

			blackLabels[i].setX(xCacheBlackNotes[i]  - w);
			blackLabels[i].setY(BLACK_LABEL_Y);
		}

		whiteLabels = new Text[N_WHITE_NOTE];
		for (int i = 0; i < N_WHITE_NOTE; i++) {
			whiteLabels[i] = new Text(Note.getNoteFromFreq((float) getFrequency(whiteNoteIndexToGlobal(i)+9)));
			whiteLabels[i].setFont(FONT);
			whiteLabels[i].applyCss();
			int w = (int) (whiteLabels[i].getLayoutBounds().getWidth() / 2);

			whiteLabels[i].setX(i * (SIZE_WHITE_NOTE + SPACING_WHITE_NOTE) + SIZE_WHITE_NOTE / 2 - w);
			whiteLabels[i].setY(WHITE_LABEL_Y);
		}
	}

	private int getBlackNoteIndex(int mouseX) {
		int index = -1;

		int bWidth = (int) imgBlackNote.getWidth() - SHADOW_OFFSET_BLACK_NOTE;

		for (int n = 0; n < N_BLACK_NOTE; n++)
			if (mouseX >= xCacheBlackNotes[n] - bWidth / 2 && mouseX <= xCacheBlackNotes[n] + bWidth / 2) {
				index = n;
				break;
			}

		return index;
	}

	private int blackNoteIndexToGlobal(int bIndex) {
		int i = bIndex + 4;
		return 2 * bIndex + (i % 5 > 1 ? 1 : 0) + 2 * (i / 5);
	}

	private int whiteNoteIndexToGlobal(int wIndex) {
		int i = wIndex + 5;
		return 2 * wIndex + (i % 7 > 2 ? 0 : 1) - 2 * (i / 7);
	}

	private int getWhiteNoteIndex(int mouseX) {
		int index = -1;

		for (int n = 0; n < N_WHITE_NOTE; n++) {
			int x = SPACING_WHITE_NOTE + SIZE_WHITE_NOTE / 2 + (SIZE_WHITE_NOTE + SPACING_WHITE_NOTE) * n;

			if (mouseX >= x - SIZE_WHITE_NOTE / 2 && mouseX <= x + SIZE_WHITE_NOTE / 2) {
				index = n;
				break;
			}
		}

		return index;
	}

	void draw() {
		px.setPixels(0, 0, canvasWidth, canvasHeight, format, buff, 0, canvasWidth);
		gc.setFont(FONT);
		
		gc.setFill(Color.BLACK);
		for (int i = 0; i < N_WHITE_NOTE; i++)
			gc.fillText(whiteLabels[i].getText(), whiteLabels[i].getX() + 0.5, whiteLabels[i].getY() + 0.5);
		gc.setFill(Color.RED);
		for (int i = 0; i < N_BLACK_NOTE; i++)
			gc.fillText(blackLabels[i].getText(), blackLabels[i].getX() + 0.5, blackLabels[i].getY() + 0.5);
	}

	void update() {
		buff = buffCache.clone();

		if (previewNote != null && !previewNote.isBlack) {
			previewNote.drawWhite();
		}
		int pos1, pos2;
		for (int i = 0; i < N_BLACK_NOTE; i++) {
			for (int j = 0, bHeight = (int) imgBlackNote.getHeight(); j < bHeight; j++) {
				for (int a = 0, bWidth = (int) imgBlackNote.getWidth(); a < bWidth; a++) {
					pos1 = xCacheBlackNotes[i] + a - (bWidth - SHADOW_OFFSET_BLACK_NOTE) / 2 + j * canvasWidth;
					pos2 = j * bWidth + a;
					buff[pos1] = a < bWidth - SHADOW_OFFSET_BLACK_NOTE ? buffBlackNote[pos2]
							: buffBlackNote[pos2] == COLOR_TRANSPARENT ? buff[pos1]
									: CColor.interpolate(buffBlackNote[pos2], buff[pos1], 0.5);
				}
			}
		}

		if (previewNote != null && previewNote.isBlack) {
			previewNote.drawBlack();
		}
	}

	private double getFrequency(int n) {
		return Note.C0_FREQ * Math.pow(2, n / 12d);
	}

	public void playNote(int keyIndex) {
		double freq = getFrequency(keyIndex);
		String note = Note.getNoteFromFreq((float) freq);
		File file = new File(PIANO_KEY_PATH + note + ".mp3");
		MediaPlayer newMedia = new MediaPlayer(new Media(file.toURI().toString()));
		medias.add(newMedia);
		newMedia.play();
		newMedia.setOnEndOfMedia(() -> medias.remove(newMedia));
	}

}
