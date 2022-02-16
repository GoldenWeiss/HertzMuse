package model.utility;

import javafx.scene.paint.Color;

public class CColor {

	public static String webstring(Color c) {
		return String.format("rgba(%d,%d,%d,%d)", (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255), (int)(c.getOpacity() * 255));
	}
	public static int interpolate(int a, int b, double p) {
		int t;
		return toInt((((((b >> 16) & 0xff) - (t = ((a >> 16) & 0xff))) * p + t)/255d),
				(((((b >> 8) & 0xff) - (t = ((a >> 8) & 0xff))) * p + t)/255d),
				((((b & 0xff) - (t = (a & 0xff))) * p + t)/255d),
				((((b >> 24) & 0xff) - (t = ((a >> 24) & 0xff))) * p + t)/255d);
	}

	public static int toInt(double re, double g, double b, double a) {

		return ((int) (a * 255) << 24) | ((int) (re * 255) << 16) | ((int) (g * 255) << 8) | ((int) (b * 255));
	}

	public static int toInt(Color c) {
		return toInt(c.getRed(), c.getGreen(), c.getBlue(), c.getOpacity());
	}

	public static Color interpolate(Color x, Color y, Color z, Color d, double p) {
		double r = 0, g = 0, b = 0, a = 0, div = 0, odiv = 0;

		if (p < (div = 1 / 3d)) {
			r = (y.getRed() * p * 3) + (x.getRed() * (div - p) * 3);
			g = (y.getGreen() * p * 3) + (x.getGreen() * (div - p) * 3);
			b = (y.getBlue() * p * 3) + (x.getBlue() * (div - p) * 3);
			a = (y.getOpacity() * p * 3) + (x.getOpacity() * (div - p) * 3);
		} else if (p < (div = 2 / 3d)) {
			odiv = 1 / 3d;
			r = (z.getRed() * (p - odiv) * 3) + (y.getRed() * (div - p) * 3);
			g = (z.getGreen() * (p - odiv) * 3) + (y.getGreen() * (div - p) * 3);
			b = (z.getBlue() * (p - odiv) * 3) + (y.getBlue() * (div - p) * 3);
			a = (z.getOpacity() * (p - odiv) * 3) + (y.getOpacity() * (div - p) * 3);
		} else {

			r = (d.getRed() * (p - div) * 3) + (z.getRed() * (1 - p) * 3);
			g = (d.getGreen() * (p - div) * 3) + (z.getGreen() * (1 - p) * 3);
			b = (d.getBlue() * (p - div) * 3) + (z.getBlue() * (1 - p) * 3);
			a = (d.getOpacity() * (p - div) * 3) + (z.getOpacity() * (1 - p) * 3);
		}
		return Color.rgb((int) (r * 255), (int) (g * 255), (int) (b * 255), a);
	}
}
