package vue;

import java.io.IOException;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;

import controleur.DataModele;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.audio.IFourierFacile;
import model.audio.ThreadModele;
import model.utility.CColor;

public class VueSine extends IVueTask {
	private DataModele lastDataModele;
	private StackPane root;
	private double FPS = 20;
	private double SPEED = 20000 * 2 * Math.PI / FPS; // (N over FPS) px per second
	private int BACKGROUND_COLOR = CColor.toInt(Color.rgb(10, 30, 30));
	private int BLACK = CColor.toInt(Color.rgb(0, 0, 0, 1));
	private double lambda;
	private double frequency;
	private double phase;
	private double amplitude;
	private double angularFreq;
	private int currentFrame;
	private int[] buffer;
	private int bufferLength;
	private int desiredWaveHeight, desiredWaveWidth;

	WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();
	GraphicsContext gc;

	@FXML
	Canvas canvas1;
	private int cwidth;
	private int cheight;
	private double kConstant;
	private VueA vueMere;

	public VueSine(VueA pVueMere, DataModele pLastDataModele) {
		super();

		vueMere = pVueMere;
		setWave(pLastDataModele);

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueD.fxml"));
			loader.setController(this);
			root = loader.load();

		} catch (IOException e) {
			e.printStackTrace();
		}

		wndptr.setScene(new Scene(root));

		root.setOnMouseClicked(e -> close());

		DecimalFormat f = new DecimalFormat("0.00");

		wndptr.setTitle("@[" + f.format(amplitude) + "*sin(" + f.format(kConstant) + "*x +" + f.format(angularFreq)
				+ "*t +" + f.format(phase) + "]");
		wndptr.initOwner(vueMere.getScene().getWindow());
		wndptr.show();

	}

	@FXML
	private void initialize() {
		gc = canvas1.getGraphicsContext2D();
		cwidth = (int) canvas1.getWidth();
		cheight = (int) canvas1.getHeight();

		desiredWaveHeight = cheight / 2;
		desiredWaveWidth = cwidth;
		buffer = new int[bufferLength = desiredWaveWidth * desiredWaveHeight * 2];
		if (lastDataModele != null)
			update();

	}

	public void setWave(DataModele pLastDataModele) {
		lastDataModele = pLastDataModele;

		float[][] peak = IFourierFacile.getPeaks2(1, 0, 4500, (float) ThreadModele.FREQUENCY_BINS,
				lastDataModele.getAmplitudes(), lastDataModele.getFrequencies());
		int indice = (int) peak[0][0];
		phase = lastDataModele.getPhases()[indice];
		amplitude = lastDataModele.getAmplitudes()[indice];
		frequency = peak[0][1];

		angularFreq = 2 * Math.PI * frequency;
		lambda = SPEED / frequency;
		kConstant = (2 * Math.PI) / lambda;

		currentFrame = 0;
	}

	void update() {

		int t = currentFrame++;
		for (int i = 0; i < bufferLength; i++)
			buffer[i] = BACKGROUND_COLOR;
		double inc = lambda * (desiredWaveHeight) * (desiredWaveHeight) / amplitude / 8;
		for (int i = desiredWaveWidth / 5; i < desiredWaveWidth; i += inc) {
			for (int y = 0; y < desiredWaveHeight * 2; y++) {
				buffer[i + desiredWaveWidth * y] = BLACK;
			}
		}
		for (int i = desiredWaveWidth / 5; i > -1; i -= inc) {
			for (int y = 0; y < desiredWaveHeight * 2; y++) {
				buffer[i + desiredWaveWidth * y] = BLACK;
			}
		}
		int fac = (int) Math.max(1, (2 * desiredWaveHeight) / amplitude);
		for (int i = 0; i < desiredWaveHeight; i += fac) {

			for (int x = 0; x < desiredWaveWidth; x++) {
				buffer[x + desiredWaveWidth * (desiredWaveHeight + i)] = BLACK;
				buffer[x + desiredWaveWidth * (desiredWaveHeight - i)] = BLACK;
			}
			// TODO
			if ((desiredWaveHeight + i) - fac == 0) {
				for (int x = desiredWaveWidth / 5 - 16; x <= desiredWaveWidth / 5; x++) {
					buffer[x + desiredWaveWidth * i] = CColor.toInt(Color.rgb(105, 105, 105, 1));
				}
			}
		}
		for (int i = 0; i < desiredWaveHeight * 2; i++) {
			buffer[desiredWaveWidth / 5 + desiredWaveWidth * i] = CColor.toInt(Color.rgb(105, 105, 105, 1));
		}
		for (int i = 0; i < desiredWaveWidth; i++) {
			buffer[i + desiredWaveWidth * desiredWaveHeight] = CColor.toInt(Color.rgb(105, 105, 105, 1));
		}

		for (double x = 0; x <= desiredWaveWidth - 1; x += 1) {
			double y0 = (desiredWaveHeight - 1)
					* Math.sin(kConstant * x * amplitude / (desiredWaveHeight * desiredWaveHeight)
							+ t * angularFreq / FPS / 500 + phase);// +
			// *(amplitude
			// /500)
			double y1 = (desiredWaveHeight - 1)
					* Math.sin(kConstant * (x + 1) * amplitude / (desiredWaveHeight * desiredWaveHeight)
							+ t * angularFreq / FPS / 500 + phase);
			if (y0 > y1) {
				double temp = y0;
				y0 = y1;
				y1 = temp;
			}
			for (y1 = Math.ceil(y1); y0 <= y1; y0 += 1)
				buffer[(int) x + desiredWaveWidth * ((int) (y0) + desiredWaveHeight)] = CColor
						.toInt(Color.rgb((int) (Color.YELLOW.getRed() * 255), (int) (Color.YELLOW.getGreen() * 255),
								(int) (Color.YELLOW.getBlue() * 255), (Math.abs(y0) - (int) Math.abs(y0))));
		}

	}

	void draw() {

		gc = canvas1.getGraphicsContext2D();
		// gc.setTransform(1, 0, 0, 1, 0, 0);
		gc.clearRect(0, 0, canvas1.getWidth(), canvas1.getHeight());
		PixelWriter px = gc.getPixelWriter();

		int lmargin = 0, rmargin = 0, umargin = 0;
		int w = (int) canvas1.getWidth() - lmargin - rmargin, h = (int) canvas1.getHeight() - umargin;
// TODO add periodic draw
		px.setPixels(lmargin, umargin, w, h, format, buffer, 0, w);

	}

}
