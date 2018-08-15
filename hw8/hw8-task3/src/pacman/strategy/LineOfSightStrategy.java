package pacman.strategy;

import pacman.model.*;
import pacman.sprite.*;

/** A strategy that moves like a turn strategy, unless it can see pac-man,
  * in which case it chases him using the seeker strategy's algorithm.
  */
public class LineOfSightStrategy extends TurnStrategy {
	private Strategy mySeeker;

	/** Constructs a new strategy to move the given sprite. */
	public LineOfSightStrategy(MovingSprite gh) {
		super(gh);
		mySeeker = new SeekerStrategy(gh);
	}

	/** Returns this strategy's next move toward the given target. */
	public Move getMove(Level level, MovingSprite target) {
		// chase pac-man if we see him
		if (level.canSee(mySprite, target))
			return mySeeker.getMove(level, target);

		else return super.getMove(level, target);
	}
}