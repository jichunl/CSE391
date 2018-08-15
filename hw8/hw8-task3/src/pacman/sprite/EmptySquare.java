package pacman.sprite;

import pacman.sprite.Sprite;

/** An empty square; a do-nothing invisible uncollidable sprite. */
public class EmptySquare extends Sprite {
	/** Constructs a new empty square at (0, 0). */
	public EmptySquare() {
		this(0, 0);
	}

	/** Constructs a new empty square at (x, y) with size 0. */
	public EmptySquare(int x, int y) {
		super(x, y, 0);
		setVisible(false);
		type = EMPTYSQUARE;
	}

	/** Returns a string representation of this empty square. */
	public String toString() { return " "; }
}