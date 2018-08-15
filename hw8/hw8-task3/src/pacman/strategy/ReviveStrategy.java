package pacman.strategy;

import pacman.model.*;
import pacman.sprite.*;

/** Revival strategy used by dead ghosts to return to their hive.
  * Uses smart strategy's algorithm.
  */
public class ReviveStrategy extends SmartStrategy {
	private MovingSprite myTarget = null;

	/** Constructs a new strategy to move the given sprite. */
	public ReviveStrategy(Ghost gh) {
		super(gh);
		myTarget = new PacMan(gh.getRespawnX(), gh.getRespawnY());
	}

	/** Returns this strategy's next move toward the given target. */
	public Move getMove(Level level, MovingSprite target) {
		// figure out how to get back home, only calculate this info once
		myTarget.setX(((Ghost)mySprite).getRespawnX());
		myTarget.setY(((Ghost)mySprite).getRespawnY());
		return super.getMove(level, myTarget);
	}
}