package vue;

import java.io.IOException;
import java.nio.IntBuffer;

import controleur.IControleurC;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import model.audio.IFourierFacile;
import model.audio.ThreadModele;
import model.utility.CColor;
import model.utility.Vector;

public class VueFormeOnde extends IVueTask {
	private static final int SAMPLE_SIZE = ThreadModele.SAMPLE_SIZE;
	private static final long MS_COMPUTE_AVERAGE_DELAY = 3000;

	private static final int COLOR_BUFF2_BG = CColor.toInt(Color.rgb(20, 20, 20));
	private static final int COLOR_BUFF2_PT = CColor.toInt(Color.CORNFLOWERBLUE);

	private static final int COLOR_BUFF1_BG = CColor.toInt(Color.rgb(50, 50, 50));
	private static final int COLOR_BUFF1_PT = CColor.toInt(Color.rgb(70, 119, 207));
	private static final int COLOR_BUFF1_MARK = CColor.toInt(Color.DARKKHAKI);
	private static final int COLOR_BUFF1_CURSOR = CColor.toInt(Color.GREEN);

	private IVueA vueMere;
	private IControleurC controleur;
	private BorderPane root;
	private WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();

	private Tooltip tooltip;

	@FXML
	private Canvas canvas1;
	private GraphicsContext gc1;
	private PixelWriter px1;
	private int canvas1Width, canvas1Height;
	private int[] buffCache1;
	private int[] buff1;
	private double buffPxThreshold1;
	private int markPxThreshold1;

	@FXML
	private Canvas canvas2;
	private GraphicsContext gc2;
	private PixelWriter px2;
	private int canvas2Width, canvas2Height;
	private int[] buffCache2;
	private int[] buff2;
	private double buffPxThreshold2;
	private float[] avgAmps;
	private long elapsedMillis;
	private int cursorX;
	private float peak;

	public VueFormeOnde(VueA pVueMere, IControleurC pControleur) {
		super();

		vueMere = pVueMere;
		controleur = pControleur;
		wndptr.setOnCloseRequest(e -> {
			close = true;
			vueMere.disposeWindow(this);
		});
		elapsedMillis = System.currentTimeMillis();
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueFormeOnde.fxml"));
			loader.setController(this);
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		vueMere = pVueMere;
		wndptr.setScene(new Scene(root));
		wndptr.initOwner(vueMere.getScene().getWindow());
		wndptr.setTitle("Forme d'onde");
		wndptr.show();

		
		
	}

	@FXML
	private void initialize() {
		canvas1Width = (int) canvas1.getWidth();
		canvas1Height = (int) canvas1.getHeight();
		gc1 = canvas1.getGraphicsContext2D();
		px1 = gc1.getPixelWriter();
		buffPxThreshold1 = vueMere.getLastDataModele().getTimePointsLength() / (double) canvas1Width;
		markPxThreshold1 = (int) Math.ceil(SAMPLE_SIZE / buffPxThreshold1);
		canvas2Width = (int) canvas2.getWidth();
		canvas2Height = (int) canvas2.getHeight();
		gc2 = canvas2.getGraphicsContext2D();
		px2 = gc2.getPixelWriter();
		buffPxThreshold2 = SAMPLE_SIZE / (double) canvas2Width;

		computeAverageAmplitudes();
		computeBuffsCaches();
		
		canvas1.setOnMousePressed(
				e -> controleur.ecouteurDragRead((int) (e.getX() * buffPxThreshold1), e.isPrimaryButtonDown(), false));
		canvas1.setOnMouseDragged(
				e -> {canvas1.getOnMouseMoved().handle(e);controleur.ecouteurDragRead((int) (e.getX() * buffPxThreshold1), e.isPrimaryButtonDown(), true);});
		cursorX = -1;
		tooltip = new Tooltip();
		canvas1.setOnMouseMoved(e -> {
			double t = (e.getX()  * vueMere.getLastDataModele().getMpDuration()) / (double)canvas1Width;
					
			tooltip.setText(String.format("%d:%02d", (int) (t / 60000), (int) ((t / 1000) % 60)) + "s");
			tooltip.show(canvas1, e.getX() + canvas1.getScene().getX() + canvas1.getScene().getWindow().getX(), e.getY()
					+ canvas1.getScene().getY() + canvas1.getScene().getWindow().getY() + canvas1.getHeight() + 15);
			cursorX = (int) e.getX();
		});
		canvas1.setOnMouseExited(e -> {
			tooltip.hide();
			cursorX = -1;
		});
		canvas2.setCursor(Cursor.OPEN_HAND);
		canvas2.setOnMouseClicked(e->((VueA) vueMere).togglePlay(null));
		update();
	}

	private void computeBuffsCaches() {
		int s = canvas1Width * canvas1Height;
		buffCache1 = new int[s];
		for (int i = 0; i < s; i++)
			buffCache1[i] = COLOR_BUFF1_BG;

		buffCache2 = new int[s = canvas2Width * canvas2Height];
		for (int i = 0; i < s; i++)
			buffCache2[i] = COLOR_BUFF2_BG;
	}

	void draw() {
		px1.setPixels(0, 0, canvas1Width, canvas1Height, format, buff1, 0, canvas1Width);

		px2.setPixels(0, 0, canvas2Width, canvas2Height, format, buff2, 0, canvas2Width);
	}

	void update() {
		buffPxThreshold1 = vueMere.getLastDataModele().getTimePointsLength() / (double) canvas1Width;
		
		markPxThreshold1 = (int) Math.ceil(SAMPLE_SIZE / buffPxThreshold1);
		//System.out.println(buffPxThreshold1);
		buff1 = buffCache1.clone();

		if (System.currentTimeMillis() - elapsedMillis > MS_COMPUTE_AVERAGE_DELAY) {
			Platform.runLater(()->computeAverageAmplitudes());
			elapsedMillis = System.currentTimeMillis();
		}

		for (int j = 0; j < canvas1Height; j++) {
			for (int i = 0; i < markPxThreshold1; i++)
				buff1[j * canvas1Width + i
						+ (int) (vueMere.getLastDataModele().getRealTimeIndex() / buffPxThreshold1)] = COLOR_BUFF1_MARK;
		}

		if (cursorX >= 0)
			for (int j = 0; j < canvas1Height; j++)
				buff1[j * canvas1Width + cursorX] = COLOR_BUFF1_CURSOR;

		for (int i = 0; i < canvas1Width; i++) {
			int y0 = (int) (avgAmps[i] * (canvas1Height / 2 - 1));
			if (y0 > 0)
				for (int j = 0; j < y0; j++)
					buff1[i + canvas1Width * (canvas1Height - 1 - (j + canvas1Height / 2))] = COLOR_BUFF1_PT;
			else
				for (int j = y0 + 1; j <= 0; j++)
					buff1[i + canvas1Width * (canvas1Height - 1 - (j + canvas1Height / 2))] = COLOR_BUFF1_PT;// }
		}

		buff2 = buffCache2.clone();
		float[] amps = vueMere.getLastDataModele().getSamplePoints();
		float peak = Vector.absmax(amps);
		
		for (int i = 0; i < canvas2Width; i++) {
/*
			int y0 = vueMere.getLastDataModele().isNorm()
					? (int) ((amps[(int) (i * buffPxThreshold2)]) * ((canvas2Height / 8 - 1)))
					: (int) ((amps[(int) (i * buffPxThreshold2)] / (SAMPLE_SIZE)) * ((canvas2Height / 8 - 1)));
*/
			int y0 = (int) ((amps[(int) (i * buffPxThreshold2)] / peak) * (canvas2Height / 2 - 1));
			if (y0 > 0)
				for (int j = 0; j < y0; j++)
					buff2[i + canvas2Width * (canvas2Height - 1 - (j + canvas2Height / 2))] = COLOR_BUFF2_PT;
			else
				for (int j = y0 + 1; j <= 0; j++)
					buff2[i + canvas2Width * (canvas2Height - 1 - (j + canvas2Height / 2))] = COLOR_BUFF2_PT;
		}

	}

	private void computeAverageAmplitudes() {

		avgAmps = IFourierFacile.averagingOverNSamples(canvas1Width, vueMere.getLastDataModele().getTimePoints()[0],
				vueMere.getLastDataModele().getTimePointsLength());
		peak = Vector.absmax(avgAmps);
		for (int i = 0; i < canvas1Width; i++)
			avgAmps[i] = avgAmps[i] / peak;

	}

}
