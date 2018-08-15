package pacman.sprite;

import pacman.sprite.ObstacleSprite;
import pacman.view.PacManColors;

/** A wall that may not be passed through by pac-man or ghosts. */
public class Wall extends ObstacleSprite {
	/** Constructs a new Wall at the given pixel coordinates. */
	public Wall(int thex, int they) {
		super("wall", thex, they, PacManColors.BLUE);
		type = WALL;
	}

	/** Returns a string representation of this Wall. */
	public String toString() { return "X"; }
}