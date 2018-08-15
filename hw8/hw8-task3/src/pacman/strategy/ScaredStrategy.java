package pacman.strategy;

import pacman.model.*;
import pacman.sprite.*;

/** A scared strategy that runs away from pac-man. */
public class ScaredStrategy extends Strategy {
	private Strategy myTurn;
	private Strategy mySeeker;
	private Strategy myRandom;

	/** Constructs a new strategy to move the given sprite. */
	public ScaredStrategy(MovingSprite gh) {
		super(gh);
		myTurn = new TurnStrategy(gh);
		mySeeker = new SeekerStrategy(gh);
		myRandom = new RandomStrategy(gh);
	}

	/** Returns this strategy's next move toward the given target. */
	public Move getMove(Level level, MovingSprite target) {
		Move turnMov = myTurn.getMove(level, target);
		Move seekMov = mySeeker.getMove(level, target);

		// move randomly, as long as it's not straight toward pac-man
		Move moveToReturn = turnMov;
		if (mySprite.isAtJuncture()  &&  moveToReturn.equals(seekMov))
			moveToReturn = myRandom.getMove(level, target);  // turnMov.equals(seekMov) ? turnMov : turnMov;

		return moveToReturn;
	}
}