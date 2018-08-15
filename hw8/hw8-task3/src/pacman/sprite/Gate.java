package pacman.sprite;

import pacman.view.PacManColors;

/** A gate, which ghosts may pass through but pac-man cannot. */
public class Gate extends ObstacleSprite {
	public static final int TYPE = GATE;

	/** Constructs a new gate with the given x/y pixel coordinates. */
	public Gate(int thex, int they) {
		super("gate", thex, they, PacManColors.PINK);
		type = GATE;
	}

	/** Returns a string representation of this gate. */
	public String toString() { return "="; }
}