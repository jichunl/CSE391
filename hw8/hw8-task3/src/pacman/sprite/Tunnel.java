package pacman.sprite;

import pacman.model.Level;
import pacman.sprite.Sprite;

/** A tunnel through which sprites may pass (ghosts at reduced speed). */
public class Tunnel extends Sprite {
	/** Constructs a new tunnel at the given pixel coordinates. */
	public Tunnel(int thex, int they) {
		super(thex, they, Level.GRID_SIZE);
		setVisible(false);
		type = TUNNEL;
	}

	/** Returns a string representation of this tunnel. */
	public String toString() { return " "; }
}