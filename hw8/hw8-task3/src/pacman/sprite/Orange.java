package pacman.sprite;

import pacman.sprite.Fruit;
import pacman.view.PacManColors;

/** An orange, the fourth fruit in the game. */
public class Orange extends Fruit {
	/** Constructs a new Orange at the given pixel coordinates. */
	public Orange(int thex, int they) {
		super("orange", thex, they);
		setColor(PacManColors.ORANGE);
		setScore(500);
		type = ORANGE;
	}

	/** Returns this Orange's name. */
	public String getName() { return "Orange"; }
}