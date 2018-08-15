package pacman.sprite;

import pacman.sprite.EmptySquare;
import java.awt.Color;

/** A letter written on the map. */
public class Letter extends EmptySquare {
	public static final int TYPE = LETTER;

	private String myLetter;

	/** Constructs a new Letter with the given letter, color, and x/y pixel coordinates. */
	public Letter(String letter, int x, int y, Color color) {
		super(x, y);
		myLetter = letter;
		setColor(color);
		setVisible(true);
		setImageName(letter.toLowerCase() + color);
		type = LETTER;
	}

	/** Returns the letter this Letter represents. */
	public String toString() {
		return myLetter;
	}
}