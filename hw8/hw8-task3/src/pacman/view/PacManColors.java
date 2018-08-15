package pacman.view;

import java.awt.Color;

/** A class to store colors to be used in the Pac-Man game. */
public final class PacManColors {
	/** A color but with an easier-to-read toString method. */
	private static class PacManColor extends Color {
        private static final long serialVersionUID = 0;
        
		private String myName;

		/** Constructs a new color with the given name and RGB values. */
		public PacManColor(String name, int r, int g, int b) {
			super(r, g, b);
			myName = name;
		}

		/** Returns a String representation of this color (its name). */
		public String toString() {
			return myName;
		}
	}

	/** Constant colors used by Pac-Man. */
	public static final Color BLACK = new PacManColor("black", 0, 0, 0),
		FLESH = new PacManColor("flesh", 255, 184, 151),
		BLUE = new PacManColor("blue", 33, 33, 222),
		ORANGE = new PacManColor("orange", 255, 184, 71),
		CYAN = new PacManColor("cyan", 0, 255, 222),
		PINK = new PacManColor("pink", 255, 184, 222),
		RED = new PacManColor("red", 255, 0, 0),
		YELLOW = new PacManColor("yellow", 255, 255, 0),
		WHITE = new PacManColor("white", 222, 222, 222),
		BROWN = new PacManColor("brown", 222, 151, 71),
		GREEN = new PacManColor("green", 0, 255, 0);
}