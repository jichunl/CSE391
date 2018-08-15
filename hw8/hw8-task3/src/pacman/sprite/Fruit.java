package pacman.sprite;

import pacman.model.*;
import pacman.sprite.EdibleSprite;
import pacman.strategy.Strategy;

/** A fruit that may be eaten by Pac-Man. */
public abstract class Fruit extends EdibleSprite {
	public static final int TYPE = FRUIT;

	public static final int FRUIT_SPEED = 30 / GameModel.UPDATES_PER_SECOND;
	public static final int FRUIT_TIME  = 20;  // in ms, how long fruit stays on screen

	private Strategy myStrategy = null;
	protected long myUpdateCounter = 0;

	/** Constructs a new Fruit with the given image name and x/y pixel coordinates. */
	public Fruit(String image, int thex, int they) {
		super(image, thex, they);
		type = FRUIT;
		//setVisible(false);
	}

	/** Sets this fruit's status to be the given value. */
	public void setStatus(Object status) {
		super.setStatus(status);
//
//		// make pellet disappear if it is eaten
//		if (status == STATUS_EATEN)
//			setVisible(false);
	}

	/** Returns this fruit's name. */
	public abstract String getName();

	/** Returns a string representation of this fruit. */
	public String toString() { return "F"; }

	/** Returns true if this fruit was recently eaten. */
	public boolean wasJustEaten() {
		// 2-second delay on eaten fruit, as opposed to superclass's one-second delay
		return isEaten()  &&  myNumUpdatesSinceEaten < GameModel.UPDATES_PER_SECOND * 2;
	}

	/** Returns this fruit's game update counter. */
	public long getCounter() {
		return myUpdateCounter;
	}

	/** Notifies this fruit of an update to the given game model. */
	public void update(GameModel gm) {
		super.update(gm);

		// check if I should turn self on or off
		myUpdateCounter++;

		// check fruit to make it appear/disappear every 20 seconds
		// (if pac-man is alive and game is in progress)
		if (isEaten()) {
			if (!wasJustEaten()  &&  isVisible)
				setVisible(false);
		} else {
			int fruitUpdates = GameModel.UPDATES_PER_SECOND * FRUIT_TIME;
			int fruitNum = (int)(myUpdateCounter % (fruitUpdates * 2));

			if (fruitNum == 0  &&  isVisible)
				setVisible(false);

			else if (fruitNum == fruitUpdates  &&  !isVisible) {
				setVisible(true);
			}
		}
	}

	/** Determines and returns this fruit's next move. */
	public Move calculateMove(Level level, MovingSprite target) {
		if (myStrategy != null  &&  !isEaten()  &&  isVisible)
			return myStrategy.getMove(level, target);
		else
			return Move.NEUTRAL;
	}

	/** Sets this fruit's movement strategy to the given one. */
	public void setStrategy(Strategy strat) {
		myStrategy = strat;
	}

	/** Resets this fruit's timer when a level is cleared. */
	public void resetTimer() {
		myUpdateCounter = 0;
		setVisible(false);
	}
}