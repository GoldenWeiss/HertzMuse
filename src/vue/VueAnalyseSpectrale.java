package vue;

import java.io.IOException;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;

import controleur.IControleurC;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.audio.IFourierFacile;
import model.audio.ThreadModele;
import model.utility.CColor;

public class VueAnalyseSpectrale extends IVueTask {
	private int ns;
	private float[][] oldPs;
	private BorderPane root;
	private IVueA vueMere;
	private int[] buff;
	private WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();

	private DecimalFormat decimalFormat = new DecimalFormat("0.0");

	private static final int CURVE_COLOR = CColor.toInt(Color.AZURE);
	private static final int BACKGROUND_COLOR = CColor.toInt(Color.LIGHTGREY);
	private static final int ZONE_CONTOUR_COLOR = CColor.toInt(Color.DARKSLATEGREY);
	private static final int GRILLE_CONTOUR_COLOR = CColor.toInt(Color.DARKGRAY);

	private static final int PEAK_COLOR = CColor.toInt(Color.BLUE);
	private static final int FUNDAMENTAL_COLOR = CColor.toInt(Color.RED);
	private static final int DOMINANT_COLOR = CColor.toInt(Color.ORANGERED);

	private static final String[] cbWindowing_choices = { "Hamming", "Hann", "Rectangulaire" };
	private static final String[] cbDetectionf0_choices = { "Autocorrélation", "Cepstre" };
	private static final String[] cbChannel_choices = { "Gauche", "Droite", "Moyenne" };
	private static final String[] cbUnits_choices = { "Intensité", "Décibels" };
	public static final int AXIS_BANDWIDTH = 4000;
	private static final int DEFAULT_DATA_BANDWIDTH = 4500;
	private static final int DEFAULT_PIXEL_TRESHOLD = (int) (DEFAULT_DATA_BANDWIDTH / ThreadModele.FREQUENCY_BINS);

	private int dataBandWidth;
	private int pixelThreshold;
	private int canvasWidth, canvasHeight;

	GraphicsContext gc;

	@FXML
	private Canvas canvas1;

	@FXML
	private ChoiceBox<String> cbWindowing;

	@FXML
	private ChoiceBox<String> cbDetectionf0;

	@FXML
	private ChoiceBox<String> cbChannel;

	@FXML
	private ChoiceBox<String> cbUnits;

	@FXML
	private CheckBox cbxCentrage;

	@FXML
	private CheckBox cbxNorm;

	@FXML
	private ScrollBar scrollBar1;

	private int lmargin;
	private int dmargin;
	private double factor;
	private PixelWriter px;
	private int[] buffCache;
	private IControleurC controleur;
	private int unitsChosen;
	private String unitsSymbol;
	private double unitsFactor;
	private int scrollDelta;
	private boolean afficherGrille;

	public VueAnalyseSpectrale(IVueA pVueMere, IControleurC pControleur) {
		super();
		vueMere = pVueMere;
		controleur = pControleur;
		close = true;
		wndptr.setOnCloseRequest(e -> {close = true; vueMere.disposeWindow(this);});
		ns = 1;
		oldPs = new float[ns][2];
		Arrays.fill(oldPs[0], 1);
		pixelThreshold = DEFAULT_PIXEL_TRESHOLD;
		dataBandWidth = DEFAULT_DATA_BANDWIDTH;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueK.fxml"));
			loader.setController(this);
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		wndptr.setScene(new Scene(root));
		wndptr.initOwner(vueMere.getScene().getWindow());
		wndptr.setTitle("Analyse spectrale");
		wndptr.show();

	}

	@FXML
	private void ecouteurCentrage() {
		controleur.ecouteurToggleCentrage();
	}

	@FXML
	private void ecouteurNorm() {
		controleur.ecouteurToggleNorm();
	}

	@FXML
	private void toggleGrille(ActionEvent e) {
		afficherGrille = !afficherGrille;
		computeCacheBuffer();
	}

	private void computeCacheBuffer() {
		
		
		int s = canvasWidth * canvasHeight;
		buffCache = new int[s];

		for (int j = 4, h = canvasHeight - 1 - dmargin; j < h; j++)
			for (int i = lmargin; i < canvasWidth - 4; i++)
				buffCache[canvasWidth * j + i] = BACKGROUND_COLOR;

		if (afficherGrille) {
			for (int j = 0; j < 300; j += 20)
				for (int i = lmargin; i < canvasWidth - 4; i++)
					buffCache[i + canvasWidth * (canvasHeight - 1 - dmargin - j)] = GRILLE_CONTOUR_COLOR;
		}

		for (int i = dmargin; i < canvasHeight - 4; i++) {

			buffCache[canvasWidth * (canvasHeight - 1 - i) + lmargin] = ZONE_CONTOUR_COLOR;
			buffCache[canvasWidth - 1 - 3 + canvasWidth * (canvasHeight - 1 - i)] = ZONE_CONTOUR_COLOR;

		}

		for (int i = lmargin; i < canvasWidth - 4; i++) {
			buffCache[i + 4 * canvasWidth] = ZONE_CONTOUR_COLOR;
			buffCache[i + canvasWidth * (canvasHeight - 1 - dmargin)] = ZONE_CONTOUR_COLOR;
		}

		for (int j = 0; j < 300; j += 20)
			for (int i = 0; i < 3; i++)
				buffCache[lmargin - i + canvasWidth * (canvasHeight - 1 - dmargin - j)] = ZONE_CONTOUR_COLOR;
	}

	@FXML
	private void initialize() {
		scrollDelta = 0;
		scrollBar1.valueProperty().addListener((o, ov, nv) -> scrollDelta = nv.intValue());

		wndptr.widthProperty().addListener((obs, oldVal, newVal) -> {
			
			int delta = (newVal.intValue() - oldVal.intValue());
			if (delta > canvas1.getWidth())
				return;
			
			pixelThreshold = pixelThreshold + delta;
			dataBandWidth = (int) (pixelThreshold * ThreadModele.FREQUENCY_BINS);
			
			scrollBar1.setVisibleAmount(dataBandWidth);
			scrollBar1.setMax(22050 - dataBandWidth);
			canvas1.setWidth(canvas1.getWidth() + delta); 
			canvasWidth = (int)canvas1.getWidth();
			
			computeCacheBuffer();
			update();
		});

		wndptr.heightProperty().addListener((obs, oldVal, newVal) -> {
		    
		});
		
		cbxCentrage.setSelected(vueMere.getLastDataModele().isSubstractMean());
		cbxNorm.setSelected(vueMere.getLastDataModele().isNorm());

		for (String systemTitle : cbWindowing_choices) {
			cbWindowing.getItems().add(systemTitle);
		}

		cbWindowing.getSelectionModel().select(vueMere.getLastDataModele().getWindowingTitle());
		cbWindowing.getSelectionModel().selectedIndexProperty()
				.addListener((o, ov, nv) -> controleur.ecouteurFenetrage(cbWindowing_choices[nv.intValue()]));

		for (String systemTitle : cbDetectionf0_choices) {
			cbDetectionf0.getItems().add(systemTitle);
		}
		cbDetectionf0.getSelectionModel().select(vueMere.getLastDataModele().getPitchDetectionTitle());
		cbDetectionf0.getSelectionModel().selectedIndexProperty()
				.addListener((o, ov, nv) -> controleur.ecouteurDetectionf0(cbDetectionf0_choices[nv.intValue()]));

		for (String systemTitle : cbChannel_choices) {
			cbChannel.getItems().add(systemTitle);
		}
		if (vueMere.getLastDataModele().getDisplayedChannel() < 0)
			cbChannel.getSelectionModel().selectLast();
		else
			cbChannel.getSelectionModel().select(vueMere.getLastDataModele().getDisplayedChannel());
		cbChannel.getSelectionModel().selectedIndexProperty().addListener(
				(o, ov, nv) -> controleur.ecouteurCanal((nv.intValue() + 1) % cbChannel_choices.length - 1));

		for (String systemTitle : cbUnits_choices) {
			cbUnits.getItems().add(systemTitle);
		}

		cbUnits.getSelectionModel().selectedIndexProperty().addListener((o, ov, nv) -> {
			unitsChosen = nv.intValue();
			controleur.ecouteurUnites(unitsChosen);
			switch (unitsChosen) {
			case 0:
				unitsFactor = 40;
				unitsSymbol = "W/m²";
				break;
			case 1:
				unitsFactor = 1;
				unitsSymbol = "dB";
				break;
			}
		});
		cbUnits.getSelectionModel().select(unitsChosen = vueMere.getLastDataModele().getUnitsIndex());
		gc = canvas1.getGraphicsContext2D();
		px = gc.getPixelWriter();
		canvasWidth = (int) canvas1.getWidth();
		canvasHeight = (int) canvas1.getHeight();

		lmargin = 70;
		dmargin = 30;

		gc.setFill(Color.BLACK);
		gc.setTextAlign(TextAlignment.LEFT);
		gc.setTextBaseline(VPos.CENTER);

		computeCacheBuffer();
		buff = buffCache.clone();
		close = false;
	}

	void draw() {

		px.setPixels(0, 0, canvasWidth, canvasHeight, format, buff, 0, canvasWidth);

		gc.setFont(Font.font("Courier New", FontWeight.BLACK, 12));
		gc.setFill(Color.BLACK);
		for (int i = 0; i <= dataBandWidth; i += 500)
			gc.fillText(i + 500 * (scrollDelta / 500) + "Hz",
					lmargin + (i - scrollDelta % 500) / ThreadModele.FREQUENCY_BINS, canvasHeight - 1 - dmargin + 12);
		gc.clearRect(0, canvasHeight - 1 + 4 - dmargin, lmargin, 12);
		gc.clearRect(canvasWidth - 3 - 1, canvasHeight - 1 + 4 - dmargin, lmargin, 12);

		gc.setTextAlign(TextAlignment.RIGHT);

		for (int j = 0; j < 300; j += 20)
			gc.fillText((int) (j * unitsFactor) + unitsSymbol, lmargin - 3, canvasHeight - 1 - dmargin - j);

		gc.setFont(Font.font("Calibri", FontWeight.NORMAL, 12));

		int y = canvasHeight - 1 - dmargin + 12 + 12;

		gc.fillText(Math.round(vueMere.getLastDataModele().getPeakFrequency() * 10) / 10d + "Hz", lmargin + 108, y);
		gc.fillText(Math.round(vueMere.getLastDataModele().getHarmonicFrequency() * 10) / 10d + "Hz", lmargin + 308, y);
		gc.fillText(Math.round(2 * vueMere.getLastDataModele().getHarmonicFrequency() * 10) / 10d + "Hz", lmargin + 488,
				y);

		gc.setTextAlign(TextAlignment.LEFT);
		gc.setFill(Color.BLUE);
		gc.fillText("Sommet :", lmargin + 4, y);

		gc.setFill(Color.RED);
		gc.fillText("Fondamentale :", lmargin + 168, y);

		gc.setFill(Color.ORANGERED);
		gc.fillText("Dominante :", lmargin + 368, y);

	}

	void update() {

		float[] amplitudes = vueMere.getLastDataModele().getAmplitudes();

		buff = buffCache.clone();
		int y0 = 0;
		int t = 0;
		int p = (int) (scrollDelta / ThreadModele.FREQUENCY_BINS);

		for (int i = 0; i <= dataBandWidth - 500; i += 500)
			if ((t = (int) ((i - scrollDelta % 500) / ThreadModele.FREQUENCY_BINS)) >= 0)
				for (int j = 0; j < 3; j++)
					buff[(int) (lmargin + t + canvasWidth * (canvasHeight - 1 - dmargin + j))] = ZONE_CONTOUR_COLOR;

		if (afficherGrille) {
			for (int i = 0; i <= dataBandWidth; i += 100) {
				if ((t = (int) ((i - scrollDelta % 100) / ThreadModele.FREQUENCY_BINS)) >= 0)
					for (int j = 4, h = canvasHeight - 1 - dmargin; j < h; j++) {
						buff[canvasWidth * j + lmargin + t] = GRILLE_CONTOUR_COLOR;
					}
			}
		}

		switch (unitsChosen) {
		case 0: // w/m2
			for (int n = 0; n < pixelThreshold; n++) {
				y0 = Math.max(canvasHeight - 1 - dmargin - (int) (0.025 * amplitudes[n + p]), 0);
				for (int y = y0, s = canvasHeight - 1 - dmargin; y < s; y++)
					buff[n + canvasWidth * y + lmargin + 1] = CURVE_COLOR;
			}
			for (int i = 0, s = Math.min((int) (vueMere.getLastDataModele().getPeakAmplitude() * 0.025),
					canvasHeight - 1 - dmargin); i < s; i++)
				if ((t = (int) vueMere.getLastDataModele().getPeakIndex() - p) >= 0)
					buff[canvasWidth * (canvasHeight - 1 - dmargin - i) + lmargin + 1 + t] = PEAK_COLOR;
			for (int i = 1, s = Math.min((int) (vueMere.getLastDataModele().getHarmonicAmplitude() * 0.025),
					canvasHeight - 1 - dmargin); i < s; i++)
				if ((t = (int) vueMere.getLastDataModele().getHarmonicIndex() - p) >= 0)
					buff[canvasWidth * (canvasHeight - 1 - dmargin - i) + lmargin + 1 + t] = FUNDAMENTAL_COLOR;
			break;
		case 1: // dB
			for (int n = 0; n < pixelThreshold; n++) {
				y0 = Math.max(
						canvasHeight - 1 - dmargin - (int) (IFourierFacile.soundIntensityToDecibels(amplitudes[n + p])),
						0);
				for (int y = y0, s = canvasHeight - 1 - dmargin; y < s; y++)
					buff[n + canvasWidth * y + lmargin + 1] = CURVE_COLOR;
			}
			for (int i = 0, s = Math.min(
					(int) (IFourierFacile.soundIntensityToDecibels(vueMere.getLastDataModele().getPeakAmplitude())),
					canvasHeight - 1 - dmargin); i < s; i++)
				if ((t = (int) vueMere.getLastDataModele().getPeakIndex() - p) >= 0)
					buff[canvasWidth * (canvasHeight - 1 - dmargin - i) + lmargin + 1 + t] = PEAK_COLOR;
			for (int i = 1, s = Math.min(
					(int) (IFourierFacile.soundIntensityToDecibels(vueMere.getLastDataModele().getHarmonicAmplitude())),
					canvasHeight - 1 - dmargin); i < s; i++)
				if ((t = (int) vueMere.getLastDataModele().getHarmonicIndex() - p) >= 0)
					buff[canvasWidth * (canvasHeight - 1 - dmargin - i) + lmargin + 1 + t] = FUNDAMENTAL_COLOR;
			break;
		}

	}

}
