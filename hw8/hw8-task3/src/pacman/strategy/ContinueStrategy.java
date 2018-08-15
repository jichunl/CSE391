package pacman.strategy;

import pacman.model.*;
import pacman.sprite.*;

/** A dumb strategy that just continues going in its current direction. */
public class ContinueStrategy extends Strategy {
	/** Constructs a new strategy to move the given sprite. */
	public ContinueStrategy(MovingSprite gh) { super(gh); }

	/** Returns this strategy's next move toward the given target. */
	public Move getMove(Level level, MovingSprite target) {
		return (level.canMove(mySprite)) ? mySprite.getLastMove() : Move.NEUTRAL;
	}
}