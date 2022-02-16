package model.utility;

import java.util.ArrayList;
import java.util.List;

import Exception.ConstructorException;

/**
 * Classe permettant de faire des calculs de fraction sans recourir a des float.
 * 
 * Les fractions sont toute r�duite a leur forme la plus simple est le
 * denominateur est toujours plus grand que 0.
 * 
 * @author Samuel Rouleau
 *
 */
public class Fraction
{
	private int numerator;
	private int denominator;

	public Fraction(int numerator, int denominator)
	{
		if (denominator == 0)
			throw new ConstructorException("Cannot create a fraction with a denominator of 0");

		if (denominator < 0)
		{
			denominator *= -1;
			numerator *= -1;
		}

		this.numerator = numerator;
		this.denominator = denominator;
		reduceFraction();
	}

	private void reduceFraction()
	{
		int gcd = GCD(Math.abs(numerator), denominator);

		numerator /= gcd;
		denominator /= gcd;
	}

	public int getNumerator()
	{
		return numerator;
	}

	public void setNumerator(int numerator)
	{
		this.numerator = numerator;
	}

	public int getDenominator()
	{
		return denominator;
	}

	public void setDenominator(int denominator)
	{
		if (denominator != 0)
		{
			if (denominator < 0)
			{
				numerator *= -1;
				denominator *= -1;
			}

			this.denominator = denominator;
		}
	}

	public static Fraction add(Fraction fraction1, Fraction fraction2)
	{

		int lcd = fraction1.getDenominator() * fraction2.getDenominator();
		int num1 = fraction1.getNumerator() * fraction2.getDenominator();
		int num2 = fraction2.getNumerator() * fraction1.getDenominator();

		return new Fraction(num1 + num2, lcd);
	}

	public static Fraction substract(Fraction fraction1, Fraction fraction2)
	{
		return add(fraction1, Multiply(fraction2, -1));
	}

	private static Fraction Multiply(Fraction fraction1, int multiplier)
	{
		return new Fraction(fraction1.getNumerator() * multiplier, fraction1.getDenominator());
	}

	@Override
	public boolean equals(Object obj)
	{
		{
			if (obj == null || !(obj instanceof Fraction))
				return false;

			Fraction other = (Fraction) obj;

			if (this.numerator == 0 && other.numerator == 0)
				return true;

			return other.numerator == this.numerator && other.denominator == this.denominator;
		}
	}

	public int compareTo(Fraction other)
	{
		if (this.equals(other))
			return 0;

		int num1 = this.getNumerator() * other.getDenominator();
		int num2 = other.getNumerator() * this.getDenominator();

		if (num1 > num2)
			return 1;
		return -1;
	}

	public float getFloat()
	{
		return (float) numerator / denominator;
	}

	private static int GCD(int number1, int number2)
	{
		if (number2 == 0)
			return number1;
		return GCD(number2, number1 % number2);
	}

	public static List<Fraction> divideInUniteFractions(Fraction fraction)
	{
		return divideInUniteFractions(fraction, new ArrayList<Fraction>());
	}

	private static List<Fraction> divideInUniteFractions(Fraction fraction, List<Fraction> currentFract)
	{
		if (fraction.getNumerator() == 0)
			return currentFract;

		int num = fraction.getNumerator() > 0 ? 1 : -1;
		Fraction newSum = new Fraction(num, fraction.getDenominator());
		currentFract.add(newSum);

		return divideInUniteFractions(Fraction.substract(fraction, newSum), currentFract);
	}

	public static Fraction roundToNearestPw2Fract(float number, int smallestFract)
	{
		number = Math.abs(number);
		int intValue = (int) Math.floor(number);
		float decimalValue = number-intValue;

		int i = 0;
		while(((float)i/smallestFract) < decimalValue)
			i++;
		
		double minBound = (i-1)/(float)smallestFract;
		double maxBound = i/(float)smallestFract;
		
		if(maxBound-decimalValue < decimalValue-minBound)
			return Fraction.add(new Fraction(intValue,1), new Fraction(i,smallestFract));
		else
			return Fraction.add(new Fraction(intValue,1), new Fraction(i-1,smallestFract));

	}

	@Override
	public String toString()
	{
		return numerator + "/" + denominator;
	}

}