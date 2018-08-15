package pacman.sprite;

import pacman.sprite.EdibleSprite;
import pacman.view.PacManColors;

/** A dot that may be eaten by Pac-Man. */
public class Dot extends EdibleSprite {
	public final int TYPE = DOT;

	/** The size of a dot on screen. */
	public static final int DOT_SIZE = 2;

	/** Constructs a new Dot at the given x/y coordinates. */
	public Dot(int thex, int they) {
		super("dotflesh", thex, they, DOT_SIZE);
		setColor(PacManColors.FLESH);
		setScore(10);
		type = DOT;
	}

	/** Sets this dot's status to the given value. */
	public void setStatus(Object status) {
		super.setStatus(status);

		// make pellet disappear if it is eaten
		if (status == STATUS_EATEN)
			setVisible(false);
	}

	/** Returns a string representation of this dot. */
	public String toString() { return "."; }
}