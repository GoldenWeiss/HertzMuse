package model.utility;

public class Complex
{
	public final double real;
	public final double imaginary;

	public Complex()
	{
		this(0, 0);
	}

	public Complex(double real, double imaginary)
	{
		this.real = real;
		this.imaginary = imaginary;
	}
	
	public Complex conjugate() {
		return new Complex(real, -imaginary);
	}
	
	public Complex scale(double factor) {
		return new Complex(real*factor, imaginary*factor);
	}
	
	public Complex square() {
		return new Complex(real*real, imaginary*imaginary);
	}
	
	public double getReal()
	{
		return real;
	}

	public double getImaginary()
	{
		return imaginary;
	}

	public static Complex add(Complex firstNumber, Complex secondNumber)
	{
		return new Complex(firstNumber.real + secondNumber.real, firstNumber.imaginary + secondNumber.imaginary);
	}

	public static Complex sub(Complex firstNumber, Complex secondNumber)
	{
		return new Complex(firstNumber.real - secondNumber.real, firstNumber.imaginary - secondNumber.imaginary);
	}

	public static Complex mult(Complex firstNumber, Complex secondNumber)
	{
		return new Complex(firstNumber.real * secondNumber.real - firstNumber.imaginary * secondNumber.imaginary,
				firstNumber.real * secondNumber.imaginary + firstNumber.imaginary * secondNumber.real);
	}

	@Override
	public String toString()
	{
		return String.format("(%f+%fi)", real, imaginary);
	}

	public float getMagnitude()
	{
		return (float) Math.sqrt((real * real) + (imaginary * imaginary));
	}

	public float getPhase()
	{
		return (float) Math.atan2(imaginary, real);
	}


}
