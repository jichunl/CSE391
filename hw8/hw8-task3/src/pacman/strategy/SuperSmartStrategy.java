package pacman.strategy;

import pacman.sprite.*;

/** Smartest of all strategies; seeks directly at Pac-Man using the shortest
  * path each time.
  */
public class SuperSmartStrategy extends SmartStrategy {
	/** Constructs a new Super smart strategy to move the given sprite. */
	public SuperSmartStrategy(MovingSprite gh) {
		super(gh);
	}

/*
	public Move getMove(GameModel model, Sprite target) {
		if (noMovesLeft()  ||  myMovesLeft == 0) {
			myMoveList = getMoveList(model, target);
			myMovesLeft = (int)Math.min(MAX_MOVES, myMoveList.size());
		}

		Move mov = Move.NEUTRAL;

		// optimization: don't weigh map if on same square as pac-man   ***
		// 2nd opt: don't weigh map if i have a clear path (no walls) to pac-man
		// improvement: make it recalculate when moves are, say, half-depleted
		// rather than 100% gone

		if (myMoveList != null  &&  !myMoveList.isEmpty()) {
			if (myMovesLeft > 0) {
				// pull first move off the list and go there
				mov = (Move)myMoveList.firstElement();
				myMoveList.removeElementAt(0);
				myMovesLeft--;
			}
			else {

			}
		}

		return mov;
	}

*/
}