package pacman.sprite;

import pacman.sprite.EdibleSprite;
import pacman.view.PacManColors;

/** A power pellet that makes pac-man invincible. */
public class PowerPellet extends EdibleSprite {
	/** Constructs a new power pellet at the given pixel coordinates. */
	public PowerPellet(int thex, int they) {
		super("pelletflesh", thex, they);
		setColor(PacManColors.FLESH);
		setScore(50);
		type = POWERPELLET;
	}

	/** Sets this power pellet's status to the given value. */
	public void setStatus(Object status) {
		super.setStatus(status);

		// make pellet disappear if it is eaten
		if (status == STATUS_EATEN)
			setVisible(false);
	}

	/** Returns a string representation of this power pellet. */
	public String toString() { return "O"; }
}