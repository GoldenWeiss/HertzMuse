package vue;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import controleur.IControleurC;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import model.audio.IFourierFacile;

import model.utility.CColor;
import model.utility.Complex;
import model.utility.Vector;

public class VueContourMelodique extends IVueTask {
	private IVueA vueMere;
	private IControleurC controleur;

	private BorderPane root;
	private WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();

	@FXML
	Canvas canvas1;
	GraphicsContext gc1;

	@FXML
	Canvas canvas2;
	GraphicsContext gc2;
	PixelWriter px2;
	private int[] buffCache2;
	private int[] buff2;

	@FXML
	Canvas canvas3;
	GraphicsContext gc3;

	private float[] contourPoints;
	private int contourIndex;
	private int contourLength;

	private int canvas1Width;
	private int canvas1Height;

	private static final int DMARGIN = 30;
	private static final Color CONTOUR_COLOR = Color.rgb(0, 0, 0);
	private static final Color SAMPLES_COLOR = Color.RED;
	private final static long MS_COMPUTE_AVERAGE_DELAY = 3000;
	private static final int COLOR_BUFF2_BG = CColor.toInt(Color.WHEAT);
	private static final int COLOR_BUFF2_SAMPLES = CColor.toInt(Color.DARKRED);

	private int canvas2Width;
	private int canvas2Height;
	private int drawnWidth;
	private int frequency0;
	private int frequency1;
	private int behavior;

	private int canvas3Width;
	private int canvas3Height;
	private float[] avgAmps;
	private long elapsedMillis;
	private int counter;
	private boolean record;
	private List<Float> recordedList;
	private float runningmean;
	private boolean closed;

	public VueContourMelodique(IVueA pVueMere, IControleurC pControleur) {

		vueMere = pVueMere;
		controleur = pControleur;

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueContourMelodique.fxml"));
			loader.setController(this);
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		vueMere.setDrawnComponent(root);
	}

	@FXML
	private void initialize() {
		recordedList = new ArrayList<>();
		elapsedMillis = System.currentTimeMillis();
		behavior = 0;

		gc1 = canvas1.getGraphicsContext2D();
		canvas1Width = (int) canvas1.getWidth();
		canvas1Height = (int) canvas1.getHeight();
		drawnWidth = canvas1Width;
		gc1.setStroke(SAMPLES_COLOR);
		gc1.setFill(Color.GAINSBORO);
		gc2 = canvas2.getGraphicsContext2D();
		px2 = gc2.getPixelWriter();

		canvas2Width = (int) canvas2.getWidth();

		canvas2Height = (int) canvas2.getHeight();

		
		gc3 = canvas3.getGraphicsContext2D();
		canvas3Width = (int) canvas3.getWidth();
		canvas3Height = (int) canvas3.getHeight();
		gc3.setStroke(Color.BLACK);
		computeBuffCaches();
		
		if (vueMere.getLastDataModele() != null) {
			
			updateDataModele();
			computeAverageAmplitudes();
			float f = (float) contourPoints[contourIndex];
			runningmean = f;
			closed = (f > 12 && f < 600);
			update();

		}
	}

	private void computeBuffCaches() {
		int s;
		buffCache2 = new int[s = canvas2Width * canvas2Height];
		for (int i = 0; i < s; i++)
			buffCache2[i] = COLOR_BUFF2_BG;
		buff2 = buffCache2.clone();
		avgAmps = new float[canvas2Width];
	}

	void draw() {
		gc1.clearRect(0, 0, canvas1Width, canvas1Height);
		int y, x, pos;

		for (int j = 0; j < canvas3Height + 3; j += 10) {
			gc3.strokeLine(77 + 0.5, canvas3Height - 1 - (j + 10) + 0.5, 80, canvas3Height - 1 - (j + 10) + 0.5);
		}

		gc3.setTextAlign(TextAlignment.RIGHT);
		gc3.setTextBaseline(VPos.CENTER);
		gc1.setStroke(Color.LIGHTGREY);
		for (int j = 0; j < canvas3Height + 3; j += 50) {
			gc1.strokeLine(0.5, canvas1Height - 1 - (j) + 0.5, canvas1Width + 0.5, canvas1Height - 1 - (j) + 0.5);
			if (j % 100 == 0) {
				gc1.fillRect(0.5, canvas1Height - 1 - (j + 50) + 0.5, canvas1Width, 50);
			}
			gc3.strokeLine(72 + 0.5, canvas3Height - 1 - (j + 10) + 0.5, 80, canvas3Height - 1 - (j + 10) + 0.5);

			gc3.fillText(j + "Hz", 72 + 0.5, canvas3Height - 1 - (j + 10) + 0.5);
		}
		gc1.setStroke(SAMPLES_COLOR);
		for (int i = 0, s = drawnWidth; i < s; i++) {
			if (i < contourLength && (pos = contourIndex - i) >= 0) {
				x = drawnWidth - 1 - i;
				y = canvas1Height - 1 - (int) Math.floor(contourPoints[pos]);
				gc1.strokeLine(x + 0.5, y + 0.5, x + 0.5, y + 0.5);
			}
		}
		px2.setPixels(0, 0, canvas2Width, canvas2Height, format, buff2, 0, canvas2Width);
		// gc2.setFill(Color.RED);
		// gc2.fillRect(0, 0, canvas2Width, canvas2Height);
	}

	private void updateDataModele() {
		contourPoints = vueMere.getLastDataModele().getMusicalContourBuffer();
		contourIndex = vueMere.getLastDataModele().getFpsTimeIndex();
		contourLength = vueMere.getLastDataModele().getComputedContourBufferLength();
	}

	void update() {
		updateDataModele();

		if (System.currentTimeMillis() - elapsedMillis > MS_COMPUTE_AVERAGE_DELAY) {
			Platform.runLater(() -> computeAverageAmplitudes());
			elapsedMillis = System.currentTimeMillis();
		}

		buff2 = buffCache2.clone();
		for (int i = 0; i < canvas2Width; i++) {
			if (avgAmps[i] <= canvas1Height) {
				int h = (int) (avgAmps[i] * (canvas2Height - 1) / canvas1Height);
				buff2[i + (canvas2Height - 1 - h) * canvas2Width] = COLOR_BUFF2_SAMPLES;
			}
		}

	}

	private void computeAverageAmplitudes() {

		avgAmps = vueMere.getLastDataModele().getComputedContourBufferLength() == 1 ? new float[canvas2Width] : IFourierFacile.downsample(canvas2Width, vueMere.getLastDataModele().getMusicalContourBuffer(),
				vueMere.getLastDataModele().getComputedContourBufferLength() - 60 * 3);

	}

}
