package pacman.strategy;

import pacman.model.*;
import pacman.sprite.*;
import pacman.utility.*;

import java.util.*;

/** Superclass for all strategies for moving enemy sprites. */
public abstract class Strategy {
	protected MovingSprite mySprite = null;
	protected int myMovePercentage      = 100;
	protected int myMoveAgainPercentage =   0;

	/** Constructs a new strategy to move the given sprite. */
	public Strategy(MovingSprite spr) {
		mySprite = spr;
	}

	/** Returns the percentage of the time that this strategy will move. */
	public int getMovePercentage() {
		return myMovePercentage;
	}

	/** Sets this strategy's moving percentage to the given amount. */
	public void setMovePercentage(int pct) {
		myMovePercentage = pct;
	}

	/** Returns this strategy's percentage at which it will choose to move a second time. */
	public int getMoveAgainPercentage() {
		return myMoveAgainPercentage;
	}

	/** Sets this strategy's move-again percentage to the given amount. */
	public void setMoveAgainPercentage(int pct) {
		myMoveAgainPercentage = pct;
	}

	/** Returns true if this strategy should move now. */
	public boolean shouldMove() {
		int roll = (int)(Math.random() * 100) + 1;  // random # between 0--100 inclusive
		return roll <= myMovePercentage;
	}

	/** Returns true if this strategy should move again now. */
	public boolean shouldMoveAgain() {
		int roll = (int)(Math.random() * 100) + 1;  // random # between 0--100 inclusive
		return roll <= myMoveAgainPercentage;
	}

	/** Notifies this strategy of pac-man's death. */
	public void notifyOfPacManDeath() {}

	/** Returns this strategy's next move toward the given target. */
	public abstract Move getMove(Level level, MovingSprite target);


	/** Returns the list of moves needed to move this ghost so that he's exactly
	  * on the nearest grid square.
	  */
	protected static List<Move> getMoveList(MovingSprite ghost, int fromX, int fromY, int toX, int toY) {
        List<Move> moveList = new ArrayList<Move>();

		int deltax = toX - fromX;

		// left moves
		while (deltax < 0) {
			int dx = (int)Math.max(-ghost.getSpeed(), deltax);
			deltax -= dx;
			moveList.add(Move.newMove(dx, 0));
		}

		// right moves
		while (deltax > 0) {
			int dx = (int)Math.min(ghost.getSpeed(), deltax);
			deltax -= dx;
			moveList.add(Move.newMove(dx, 0));
		}

		int deltay = toY - fromY;

		// up moves
		while (deltay < 0) {
			int dy = (int)Math.max(-ghost.getSpeed(), deltay);
			deltay -= dy;
			moveList.add(Move.newMove(0, dy));
		}

		// down moves
		while (deltay > 0) {
			int dy = (int)Math.max(ghost.getSpeed(), deltay);
			deltay -= dy;
			moveList.add(Move.newMove(0, dy));
		}

		// System.out.println("moving from (" + fromX + ", " + fromY + ") to (" + toX + ", " + toY + "):\n" + moveList);

		return moveList;
	}

//	/** Removes each element in normal order from v2 and appends
//	  * each at the end of v1.
//	  */
//	public static void appendVector(List<String> v1, List<String> v2) {
//		while (!v2.isEmpty()) {
//			v1.add(v2.remove(0));
//		}
//	}
//
//	/** Removes each element in reverse order from v2 and inserts
//	  * each at the start of v1.
//	  */
//	public static void insertVector(List v1, List v2) {
//        while (!v2.isEmpty()) {
//            v1.add(0, v2.remove(0));
//        }
//	}

	/** Prints the given array. */
	public static void printWeightMap(int[][] myWeightMap) {
		for (int y = 0;  y < myWeightMap[0].length;  y++) {
			for (int x = 0 ; x < myWeightMap.length;  x++)
				System.out.print(Utility.padStringR("" + myWeightMap[x][y], 4));
			System.out.println();
		}
	}

	/** Returns this strategy's name. */
	public String getName() {
		String className = getClass().getName();
		return className.substring(className.lastIndexOf(".") + 1, className.length());
	}
}