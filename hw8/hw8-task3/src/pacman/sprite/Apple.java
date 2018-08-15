package pacman.sprite;

import pacman.sprite.Fruit;
import pacman.view.PacManColors;

/** An apple, the fifth fruit in pac-man. */
public class Apple extends Fruit {
	/** Constructs a new apple at the given pixel coordinates. */
	public Apple(int thex, int they) {
		super("apple", thex, they);
		setColor(PacManColors.GREEN);
		setScore(1000);
		type = APPLE;
	}

	/** Returns this apple's name. */
	public String getName() { return "Apple"; }
}