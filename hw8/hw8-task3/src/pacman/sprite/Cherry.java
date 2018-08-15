package pacman.sprite;

import pacman.sprite.Fruit;
import pacman.view.PacManColors;

/** A cherry, the first/second fruit in the game. */
public class Cherry extends Fruit {
	/** Constructs a new cherry at the given x/y coordinates. */
	public Cherry(int thex, int they) {
		super("cherry", thex, they);
		setColor(PacManColors.RED);
		setScore(100);
		type = CHERRY;
	}

	/** Returns this cherry's name. */
	public String getName() { return "Cherry"; }
}