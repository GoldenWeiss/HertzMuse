package model.utility;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

public class Vector {
	@FunctionalInterface
	public interface FloatUnaryFunction {
		public float apply(float i);
	}

	public static float[] build(int w, DoubleUnaryOperator block) {
		float[] ary = new float[w];
		for (int i = 0; i < w; i++)
			ary[i] = (float) block.applyAsDouble(i);
		return ary;
	}

	public static float[] apply(float[] vector, FloatUnaryFunction block) {
		int h = vector.length;
		float[] ary = new float[h];
		for (int i = 0; i < h; i++) {
			ary[i] = block.apply(vector[i]);
		}
		return ary;
	}

	public static int iMin(float[] vec) {
		int index = 0;

		for (int i = 0, s = vec.length; i < s; i++)
			if (vec[i] < vec[index])
				index = i;

		return index;
	}

	public static int iMax(float[] vec) {
		int index = 0;

		for (int i = 0, s = vec.length; i < s; i++)
			if (vec[i] > vec[index])
				index = i;

		return index;
	}

	public static int iMax(float[] vec, int a, int b) {
		int index = a;

		for (int i = a, s = vec.length; i < s && i < b; i++)
			if (vec[i] > vec[index])
				index = i;

		return index;
	}

	public static float max(float[] vec) {
		float max = 0;

		for (int i = 0, s = vec.length; i < s; i++)
			if (vec[i] > max)
				max = vec[i];

		return max;

	}

	public static float absmax(float[] vec) {
		float max = 0;

		for (int i = 0, s = vec.length; i < s; i++)
			if (Math.abs(vec[i]) > max)
				max = Math.abs(vec[i]);

		return max;

	}

	public static float max(float[] vec, int s) {
		float max = 0;
		for (int i = 0; i < s; i++)
			if (vec[i] > max)
				max = vec[i];
		return max;
	}

	public static float[] multiply(float[] vec, float scalar) {
		int s = vec.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = vec[i] * scalar;
		return ary;
	}

	public static float[] divide(float[] vec, float scalar) {
		int s = vec.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = vec[i] / scalar;
		return ary;
	}

	public static void divide_(float[] vec, float scalar, int veclength) {
		for (int i = 0; i < veclength; i++)
			vec[i] = vec[i] / scalar;
	}

	public static float[] add(float[] vec, float scalar) {
		int s = vec.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = vec[i] + scalar;
		return ary;
	}

	public static float[] addition(float[] vec1, float[] vec2) {
		// System.out.println(vec2.length);
		int s = vec1.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = vec1[i] + vec2[i];
		return ary;
	}

	public static float[] multiplication(float[] vec1, float[] vec2) {
		int s = vec1.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = vec1[i] * vec2[i];
		return ary;
	}

	public static float[] division(float[] vec1, float[] vec2) {
		int s = vec1.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = vec1[i] / vec2[i];
		return ary;
	}

	public static float[] multiplication(float[] vec1, float[] vec2, float scal) {
		int s = vec1.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = vec1[i] * vec2[i] * scal;
		return ary;
	}

	/**
	 * 
	 * @param vec
	 * @param scalar - for numerical stability
	 * @return
	 */
	public static float[] sqrt(float[] vec, float scalar) {
		int s = vec.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = (float) Math.sqrt(vec[i] + scalar);
		return ary;
	}

	public static float[] sqrt(float[] vec) {
		int s = vec.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = (float) Math.sqrt(vec[i]);
		return ary;
	}

	public static float[] sqrtinv(float[] vec, float scalar) {
		int s = vec.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = 1f / (float) Math.sqrt(vec[i] + scalar);
		return ary;
	}

	public static float[] cube(float[] vec, float scal) {
		int s = vec.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = vec[i] * vec[i] * vec[i] * scal;
		return ary;
	}

	public static float[] square(float[] vec) {
		int s = vec.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = vec[i] * vec[i];
		return ary;
	}

	public static void square_(float[] vec) {
		int s = vec.length;

		for (int i = 0; i < s; i++)
			vec[i] *= vec[i];

	}

	public static float[] inv(float[] vec) {
		int s = vec.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = 1f / vec[i];
		return ary;
	}

	public static float[] inv(float[] vec, float scalar) {
		int s = vec.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = 1f / (vec[i] + scalar);
		return ary;
	}

	public static float[] substraction(float[] vec1, float[] vec2) {
		int s = vec1.length;
		float[] ary = new float[s];
		for (int i = 0; i < s; i++)
			ary[i] = vec1[i] - vec2[i];
		return ary;
	}

	public static void substract_(float[] v1, float[] v2, float rate) {
		for (int i = 0, s = v1.length; i < s; i++)
			v1[i] = v1[i] - v2[i] * rate;

	}

	public static float mean(float[] vec) {
		int w = vec.length;
		int sum = 0;
		for (int i = 0; i < w; i++)
			sum += vec[i];
		return sum / (float) w;

	}

	public static float mean(float[] vec, int a, int b) {
		int w = vec.length;
		int sum = 0;
		for (int i = a; i < w && i <= b; i++)
			sum += vec[i];
		return sum / (float) (b - a);

	}

	public static float sum(float[] vec) {
		int w = vec.length;
		int s = 0;
		for (int i = 0; i < w; i++)
			s += vec[i];
		return s;

	}

	public static void print(float[] vec) {
		System.out.println(Arrays.toString(vec));
	}

	public static void multiply_(float[] vec, float scalar) {
		int s = vec.length;

		for (int i = 0; i < s; i++)
			vec[i] = vec[i] * scalar;

	}

	public static float std(float mean, float[] vec) {
		int w = vec.length;
		float sum = 0;
		for (int i = 0; i < w; i++)
			sum += (vec[i] - mean) * (vec[i] - mean);
		return (float) Math.sqrt(sum / (double) w);
	}

	public static float std(float mean, float[] vec, int a, int b) {
		int w = vec.length;
		float sum = 0;
		for (int i = a; i < w && i <= b; i++)
			sum += (vec[i] - mean) * (vec[i] - mean);
		return (float) Math.sqrt(sum / (double) (b - a));
	}

}
