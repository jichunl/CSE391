package pacman.sprite;

import pacman.sprite.Fruit;
import pacman.view.PacManColors;

/** A strawberry, the third fruit in the game. */
public class Strawberry extends Fruit {
	/** Constructs a new strawberry at the given pixel coordinates. */
	public Strawberry(int thex, int they) {
		super("strawberry", thex, they);
		setColor(PacManColors.PINK);
		setScore(300);
		type = STRAWBERRY;
	}

	/** Returns this strawberry's name. */
	public String getName() { return "Strawberry"; }
}