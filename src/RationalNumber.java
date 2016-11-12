
public class RationalNumber {

	private int numerator;
	private int denominator;

	public RationalNumber(int wholeNumberValue) {
		this(wholeNumberValue, 1);
	}
		
	public RationalNumber(int numerator, int denominator) {
		if (denominator == 0) {
			throw new IllegalArgumentException("denominator must be nonzero");
		}
		if (denominator < 0) {
			numerator = -numerator;
			denominator = -denominator;
		}
		int gcd = GCD(numerator, denominator);
		if (gcd != 1) {
			numerator /= gcd;
			denominator /= gcd;
		}
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public static int GCD(int a, int b) { return b==0 ? a : GCD(b, a%b); }
	
	public static int LCM(int a, int b) {
		int gcd = GCD(a, b);
		return a * (b / gcd);
	}
	
	public boolean isZero() { return numerator == 0; }
	public boolean isNegative() { return numerator < 0; }
	
	public int getNumerator() { return numerator; }
	public int getDenominator() { return denominator; }
	
	public boolean greaterThan(RationalNumber other) {
		return this.numerator * other.denominator >
			other.numerator * this.denominator;
	}
	
	public RationalNumber add(RationalNumber other) {
		int gcd = GCD(this.denominator, other.denominator);
		int numeratorSum =
				this.numerator * (other.denominator / gcd) +
				other.numerator * (this.denominator / gcd);
		int denominatorSum = (this.denominator / gcd) *
				other.denominator;
		return new RationalNumber(numeratorSum, denominatorSum);
	}
	
	public RationalNumber mul(RationalNumber other) {
		return new RationalNumber(this.numerator * other.numerator,
				this.denominator * other.denominator);
	}
	
	public RationalNumber inverse() {
		return new RationalNumber(denominator, numerator);
	}
	
	public boolean isWholeNumber() {
		return denominator == 1;
	}
	
	public int getAsInt() {
		if (!this.isWholeNumber()) throw new IllegalArgumentException(
				"can only get whole numbers as ints");
		return this.numerator;
	}
	
	@Override
	public String toString() {
		if (this.isWholeNumber()) return Integer.toString(this.numerator);
		return this.numerator + "/" + this.denominator;
	}
	
}

