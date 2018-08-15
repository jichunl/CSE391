package pacman.strategy;

import pacman.model.*;
import pacman.sprite.*;

/** A random strategy that moves in a completely arbitrary direction. */
public class RandomStrategy extends Strategy {
	/** Constructs a new strategy to move the given sprite. */
	public RandomStrategy(MovingSprite gh) { super(gh); }

	/** Returns this strategy's next move toward the given target. */
	public Move getMove(Level level, MovingSprite target) {
		Move mov = mySprite.getCurrentMove();  // .normalize();
		if (mySprite.isAtJuncture()) {
			// may change direction
			Move[] possibleMoves = {Move.LEFT, Move.RIGHT, Move.UP, Move.DOWN};
			boolean[] isLegalMove = {level.canMoveLeft(mySprite), level.canMoveRight(mySprite),
				level.canMoveUp(mySprite), level.canMoveDown(mySprite)};

			int index = 0;
			while (true) {
				index = (int)(Math.random() * possibleMoves.length);
				if (isLegalMove[index]) {
					mov = possibleMoves[index];
					break;
				}
			}

			mov = mov.times(mySprite.getSpeed());
		}

		return mov;
	}
}