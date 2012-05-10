package com.kurento.kas.mscontrol.networkconnection.internal;

public class Fraction implements Comparable<Fraction> {

	private int numerator;
	private int denominator;

	public Fraction(int numerator, int denominator) {
		if (denominator == 0)
			throw new IllegalArgumentException(
					"Fraction denominator can not be 0");
		int gcd = gcd(numerator, denominator);
		this.numerator = numerator / gcd;
		this.denominator = denominator / gcd;
	}

	public int getNumerator() {
		return numerator;
	}

	public int getDenominator() {
		return denominator;
	}

	public Fraction min(Fraction other) {
		return this.compareTo(other) <= 0 ? this : other;
	}

	public Fraction max(Fraction other) {
		return this.compareTo(other) >= 0 ? this : other;
	}

	public Double getDouble() {
		return ((double) numerator / (double) denominator);
	}

	public Fraction multiply(int factor) {
		return new Fraction(numerator * factor, denominator);
	}

	public Fraction multiply(Fraction other) {
		return new Fraction(numerator * other.numerator, denominator
				* other.denominator);
	}

	public Fraction divide(int factor) {
		return new Fraction(numerator, denominator * factor);
	}

	public Fraction divide(Fraction other) {
		return new Fraction(numerator * other.denominator, denominator
				* other.numerator);
	}

	/**
	 * @return true if this is multiple of other, or false if not.
	 */
	public boolean isMultiple(Fraction other) {
		return this.divide(other).getDenominator() == 1;
	}

	public Fraction abs() {
		return new Fraction(Math.abs(numerator), Math.abs(denominator));
	}

	@Override
	public int compareTo(Fraction o) {
		/* TODO: Posibly there is a better way to do this */
		return getDouble().compareTo(o.getDouble());

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + denominator;
		result = prime * result + numerator;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fraction other = (Fraction) obj;
		if (denominator != other.denominator)
			return false;
		if (numerator != other.numerator)
			return false;
		return true;
	}

	public String toString() {
		return this.numerator + "/" + this.denominator;
	}

	/**
	 * Return lowest common multiple.
	 */
	private int lcm(int a, int b) {
		return a * b / gcd(a, b);
	}

	private int gcd(int a, int b) {
		int iaux;
		a = Math.abs(a);
		b = Math.abs(b);
		int i1 = Math.max(a, b);
		int i2 = Math.min(a, b);
		do {
			iaux = i2;
			i2 = i1 % i2;
			i1 = iaux;
		} while (i2 != 0);
		return i1;
	}

}
