package vue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import controleur.ControleurC;
import controleur.DataModele;
import controleur.IControleurC;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import model.audio.IFourierFacile;
import model.audio.ThreadModele;
import model.utility.CColor;
import model.utility.Vector;

public class VueS extends IVueTask {

	private BorderPane root;
	@FXML
	StackPane stackPane1;

	@FXML
	Canvas canvas1;

	@FXML
	ImageView imgView1;

	@FXML
	ImageView imgView2;

	private Image image;
	private IVueA vueMere;
	private int[] buff;
	private double zoomFactor;
	private int smoothMax;
	private WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();
	GraphicsContext gc;

	private PixelWriter px;
	private int oldSmoothMax;
	private int canvasWidth;
	private int canvasHeight;
	private int bassPixelThreshold;
	private int frameTick;

	public VueS(IVueA pVueMere, int pBassThreshold) {
		super();
		vueMere = pVueMere;
		wndptr.setOnCloseRequest(e -> {
			close = true;

			vueMere.disposeWindow(this);
		});
		close = false;
		oldSmoothMax = (int) (canvasHeight - 1);
		buff = new int[oldSmoothMax * oldSmoothMax];
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueS.fxml"));
			loader.setController(this);
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		wndptr.initOwner(vueMere.getScene().getWindow());
		wndptr.setScene(new Scene(root));
		setBassThreshold(pBassThreshold);
		wndptr.show();

	}

	@Override
	void update() {
		// if (((frameTick = (frameTick + 1) % 6) == 1))
		// return;
		float[] amplitudes = vueMere.getLastDataModele().getAmplitudes();
		//float m = Vector.absmax(amplitudes);
		//amplitudes = Vector.apply(amplitudes, e->e/m);
		// amplitudes

		// int lmargin = 50, rmargin = 50, dmargin = 50;
		int w = 800;// (int) canvasWidth - lmargin - rmargin, h = (int) canvasHeight;

		int bar_spacing = 1;
		int desired_size = (int) (w - 1);

		int q = Math.min(Math.min(w - 1, desired_size), bassPixelThreshold - 1);
		float factor = desired_size / (float) q;
		double circ = desired_size;
		double r = circ / (2 * Math.PI);
		float[] smoothAmplitudes = new float[desired_size];

		int lp, rp, ind;
		for (int n1 = 0; n1 < q; n1 += 1) {
			for (float xb = (n1 * factor), xc = ((n1 + 1) * factor); xb <= xc; xb += bar_spacing) {
				ind = (int) xb;
				smoothAmplitudes[ind] = amplitudes[n1];

				int nn = (int) (factor * 0.75);
				for (int i = 1; i <= nn && (lp = (int) ((xb - i) / factor)) > -1
						&& (rp = (int) ((xb + i) / factor)) < bassPixelThreshold - 1; i++) {
					smoothAmplitudes[ind] += amplitudes[lp] + amplitudes[rp];
				}
				smoothAmplitudes[ind] /= (2 * nn + 1) * 40;
				
					
			}
		}

		float max = Vector.absmax(smoothAmplitudes);
		float mean = Vector.mean(smoothAmplitudes);
		// System.out.println(max + " " + mean);
		zoomFactor = 1 + mean / 3000;
		zoomFactor *= zoomFactor;

		smoothMax = (int) (Math.floor(max * 0.17f) * 2) + (int) (Math.ceil(r * zoomFactor) * 2) + 1;
		// System.out.println(Vector.absmax(amplitudes));
		buff = new int[(smoothMax) * (smoothMax)];

		for (int n1 = 0; n1 < q; n1 += 1) {

			for (double xb = (n1 * factor), xc = ((n1 + 1) * factor); xb <= xc; xb += bar_spacing) {

				ind = (int) xb;
				double n = xb / (double) circ * Math.PI + Math.PI / 2;
				double cos = Math.cos(n);
				double sin = Math.sin(n);
				double smin = Math.min(Math.abs(cos), Math.abs(sin));

				Color ec = CColor.interpolate(Color.CORAL, Color.AQUA, Color.BLUEVIOLET, Color.RED,
						(xb + circ / 4) / (1.5 * circ));

				Color ed = CColor.interpolate(Color.BLUEVIOLET, Color.RED, Color.CORAL, Color.AQUA,
						(((1d - n / (2 * Math.PI)) * (2 * circ)) - circ / 4) / (1.5 * circ));

				for (double u = 0; u < smoothAmplitudes[ind] * 0.17; u += smin) {
					int xk = (int) ((u + r * zoomFactor) * cos);
					int yk = (int) ((u + r * zoomFactor) * sin);
					buff[xk + (smoothMax) / 2 + (smoothMax) * (smoothMax - 1 - (yk + smoothMax / 2))] = CColor
							.toInt(ec);
					buff[-xk + (smoothMax) / 2 + (smoothMax) * (smoothMax - 1 - (yk + smoothMax / 2))] = CColor
							.toInt(ed);
				}
			}
		}
	}

	@Override
	void draw() {

		gc.clearRect((canvasWidth - (int) oldSmoothMax) / 2, (canvasHeight - (int) oldSmoothMax) / 2, oldSmoothMax,
				oldSmoothMax);
	
		imgView1.setScaleX(zoomFactor);
		imgView1.setScaleY(zoomFactor);
		px.setPixels((canvasWidth - (int) smoothMax) / 2, (canvasHeight - (int) smoothMax) / 2, smoothMax, smoothMax,
				format, buff, 0, smoothMax);
		
		
		oldSmoothMax = smoothMax + 1;
	}

	@FXML
	private void initialize() {

		gc = canvas1.getGraphicsContext2D();
		px = gc.getPixelWriter();
		FileInputStream inputstream = null;
		canvasWidth = (int) canvas1.getWidth();
		canvasHeight = (int) canvas1.getHeight();
		try {
			inputstream = new FileInputStream("TestData/TestMp3File/s2.png");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		image = new Image(inputstream);
		imgView1.setImage(image);

		try {
			inputstream = new FileInputStream("TestData/TestMp3File/s1.jpg");
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		image = new Image(inputstream);
		imgView2.setImage(image);
		GaussianBlur gaussianBlur = new GaussianBlur();
		gaussianBlur.setRadius(4);
		imgView2.setEffect(gaussianBlur);
		update();

	}

	public void setBassThreshold(int pBassThreshold) {
		bassPixelThreshold = (int) (pBassThreshold / ThreadModele.FREQUENCY_BINS);
		wndptr.setTitle("Rendu des basses " + pBassThreshold + "Hz");
	}
}
