package pacman.strategy;

import pacman.model.*;
import pacman.sprite.*;
import pacman.view.*;

/** A variant of the smart strategy that instead seeks after a spot slightly ahead
  * of pac-man, in an attempt to go around and sandwich him.
  */
public class SmartAheadStrategy extends SmartStrategy {
	private static final int MAX_SQUARES_AHEAD = 8;

	/** Constructs a new smart ahead strategy to move the given sprite. */
	public SmartAheadStrategy(MovingSprite gh) {
		super(gh);
	}

	/** Returns this strategy's next move toward the given target. */
	public Move getMove(Level level, MovingSprite target) {
		// relies on the fact that inky uses a turn strategy...
		Ghost gh = new Ghost("smartaheadghost", target.getX(), target.getY(), PacManColors.BLACK);

		int i = 0;
		while (i++ < MAX_SQUARES_AHEAD * Level.GRID_SIZE / gh.getSpeed())
			gh.go(gh.calculateMove(level, target));

		return getMove(level, gh);
	}
}