package pacman.utility;

/** Utility methods. */
public class Utility {
	/** Returns a word representation of the given number.
	  * i.e. 1 -> "one", 2 -> "two", etc.
	  * Only goes up to ten out of programmer laziness.
	  */
	public static String getWordForNumber(int number) {
		String result;

		if (number == 0)        result = "zero";
		else if (number ==  1)  result = "one";
		else if (number ==  2)  result = "two";
		else if (number ==  3)  result = "three";
		else if (number ==  4)  result = "four";
		else if (number ==  5)  result = "five";
		else if (number ==  6)  result = "six";
		else if (number ==  7)  result = "seven";
		else if (number ==  8)  result = "eight";
		else if (number ==  9)  result = "nine";
		else if (number == 10)  result = "ten";
		else                    result = "??";

		return result;
	}

	/** Returns an upper-case word representation of the given number.
	  * i.e. 1 -> "ONE", 2 -> "TWO", etc.
	  * Only goes up to ten out of programmer laziness.
	  */
	public static String getUpperCaseWordForNumber(int number) {
		String result;

		if (number == 0)        result = "ZERO";
		else if (number ==  1)  result = "ONE";
		else if (number ==  2)  result = "TWO";
		else if (number ==  3)  result = "THREE";
		else if (number ==  4)  result = "FOUR";
		else if (number ==  5)  result = "FIVE";
		else if (number ==  6)  result = "SIX";
		else if (number ==  7)  result = "SEVEN";
		else if (number ==  8)  result = "EIGHT";
		else if (number ==  9)  result = "NINE";
		else if (number == 10)  result = "TEN";
		else                    result = "??";

		return result;
	}

	/** Returns a right-justified string of the given length. */
	public static String padStringR(String s, int n) {
		while (s.length() < n)
			s = " " + s;

		return s;
	}
}