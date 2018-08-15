package pacman.strategy;

import pacman.model.*;
import pacman.sprite.*;

/** Seeks directly after pac-man, even if walls are in the way.  Can get stuck because of this. */
public class SeekerStrategy extends Strategy {
	/** Constructs a new seeker strategy to move the given sprite. */
	public SeekerStrategy(MovingSprite gh) { super(gh); }

	/** Returns this strategy's next move toward the given target. */
	public Move getMove(Level level, MovingSprite target) {
		Move mov = null;

		//if (mySprite.isAtJuncture()) {
			double dx = mySprite.getX() - target.getX(),
			       dy = mySprite.getY() - target.getY();

			double dxmag = Math.abs(dx),
			       dymag = Math.abs(dy);

			Move firstChoice  = Move.NEUTRAL,
			     secondChoice = Move.NEUTRAL;

			if (dxmag >= dymag) {
				if (dx > 0  &&  level.canMoveLeft(mySprite))       firstChoice = Move.LEFT;
				else if (dx < 0  &&  level.canMoveRight(mySprite)) firstChoice = Move.RIGHT;

				if (dy > 0  &&  level.canMoveUp(mySprite))         secondChoice = Move.UP;
				else if (dy < 0  &&  level.canMoveDown(mySprite))  secondChoice = Move.DOWN;
			}
			else {
				if (dy > 0  &&  level.canMoveUp(mySprite))         firstChoice = Move.UP;
				else if (dy < 0  &&  level.canMoveDown(mySprite))  firstChoice = Move.DOWN;

				if (dx > 0  &&  level.canMoveLeft(mySprite))       secondChoice = Move.LEFT;
				else if (dx < 0  &&  level.canMoveRight(mySprite)) secondChoice = Move.RIGHT;
			}

			mov = (firstChoice != Move.NEUTRAL) ? firstChoice : secondChoice;
			mov = mov.times(mySprite.getSpeed());
		//}

		// System.out.println("moving " + mov);

		return mov;
	}
}