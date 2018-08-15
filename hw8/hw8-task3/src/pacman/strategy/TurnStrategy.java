package pacman.strategy;

import pacman.model.*;
import pacman.sprite.*;

import java.util.*;

/** Moves until it can turn, then randomly does so.  Does not turn around
  * back the way it came; can only make 90-degree turns, unless absolutely necessary to do otherwise.
  */
public class TurnStrategy extends Strategy {
	List<Move> possibleMoves = new ArrayList<Move>();

	/** Constructs a new Turn strategy to move the given sprite. */
	public TurnStrategy(MovingSprite gh) { super(gh); }

	/** Returns this strategy's next move toward the given target sprite. */
	public Move getMove(Level level, MovingSprite target) {
		possibleMoves.clear();

		if (!mySprite.isMoving()) {
			// not currently moving; pick any direction that is legal
			if (level.canMoveLeft(mySprite)) {
                possibleMoves.add(Move.LEFT.times(mySprite.getSpeed()));
            }
			if (level.canMoveRight(mySprite)) {
                possibleMoves.add(Move.RIGHT.times(mySprite.getSpeed()));
            }
			if (level.canMoveUp(mySprite)) {
                possibleMoves.add(Move.UP.times(mySprite.getSpeed()));
            }
			if (level.canMoveDown(mySprite)) {
                possibleMoves.add(Move.DOWN.times(mySprite.getSpeed()));
            }
		}

		else if (!mySprite.isAtJuncture()  &&  level.canMove(mySprite)) {
			// moving, but not on an even grid square, and may continue to go
			// straight ahead; so just keep going
			Move mov = mySprite.getCurrentMove();
			if (mov.getMagnitude() != mySprite.getSpeed())
				mov = mov.normalize().times(mySprite.getSpeed());

			return mov;
		}

		else {
			// can't move straight, or moving and at an even grid square; possibly turn
			Move currentMove   = mySprite.getCurrentMove();
			if (currentMove.getMagnitude() != mySprite.getSpeed())
				currentMove = currentMove.normalize().times(mySprite.getSpeed());
			Move leftTurnMove  = currentMove.rotateLeft();
			Move rightTurnMove = currentMove.rotateRight();

			if (level.canMove(mySprite, currentMove)) {
                possibleMoves.add(currentMove);
            }
			if (level.canMove(mySprite, leftTurnMove)) {
                possibleMoves.add(leftTurnMove);
            }
			if (level.canMove(mySprite, rightTurnMove)) {
                possibleMoves.add(rightTurnMove);
            }

			// last resort: turn around if there are no other legal moves
			if (possibleMoves.isEmpty()) {
				Move turnAroundMove = currentMove.reverse();
				if (level.canMove(mySprite, turnAroundMove)) {
                    possibleMoves.add(turnAroundMove);
                }
			}
		}

		if (possibleMoves.isEmpty())
			// ghost is stuck
			return Move.NEUTRAL;

		// pick a move at random from our list
		return possibleMoves.get((int)(Math.random() * possibleMoves.size()));
	}
}