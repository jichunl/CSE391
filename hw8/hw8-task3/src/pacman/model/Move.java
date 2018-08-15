package pacman.model;

import java.util.*;

/** Represents a move to be made by a sprite, with a delta-x and delta-y coordinate.
  * Uses flyweighted moves to avoid many instances being constructed.
  */
public final class Move {
	/** Number of instances of this class. */
	public static int ourNumInstances = 0;
	private static Map<String, Move> ourInstanceTable = new HashMap<String, Move>();

	/** Constant moves used as singletons to avoid object overhead. */
	public static final Move NEUTRAL = newMove(0, 0),
		LEFT    = newMove(-1,  0),
		RIGHT   = newMove( 1,  0),
		UP      = newMove( 0, -1),
		DOWN    = newMove( 0,  1),

		LEFT2   = newMove(-2,  0),
		RIGHT2  = newMove( 2,  0),
		UP2     = newMove( 0, -2),
		DOWN2   = newMove( 0,  2),

		LEFT4   = newMove(-4,  0),
		RIGHT4  = newMove( 4,  0),
		UP4     = newMove( 0, -4),
		DOWN4   = newMove( 0,  4);

	/** A list of all known singleton moves. */
/*
	public static final Move[] CANONICAL_MOVES = {NEUTRAL,
		LEFT, RIGHT, UP, DOWN,
		LEFT2, RIGHT2, UP2, DOWN2,
		LEFT4, RIGHT4, UP4, DOWN4};
*/


	/** Instance variables to represent the coordinate movement of this move. */
	public final int dx, dy;

	/* Constructs a new Move.  Not called by user.  Instead newMove(int, int) is used. */
	private Move(int x, int y) {
		dx = x;
		dy = y;
		ourNumInstances++;
	}

//	/* Constructs a new Move.  Not called by user.  Instead newMove(int, int) is used. */
//	private Move(Move m) {
//		this(m.dx, m.dy);
//	}

	/** Constructs a new Move with the given x/y coordinates. */
	public static Move newMove(int x, int y) {
		String key = x + "," + y;
		if (ourInstanceTable.containsKey(key))
			return (Move)ourInstanceTable.get(key);
		else {
			Move m = new Move(x, y);
			ourInstanceTable.put(key, m);
			return m;
		}
		//for (int i = 0;  i < CANONICAL_MOVES.length;  i++)
		//	if (CANONICAL_MOVES[i].dx == x  &&  CANONICAL_MOVES[i].dy == y)
		//		return CANONICAL_MOVES[i];

		// throw new RuntimeException
		// System.out.println("Probably shouldn't be here!  Creating unknown Move object: (" + x + ", " + y + ")");
		// return new Move(x, y);
	}

	/** Returns true if this move is in the same direction as the given other move. */
	public final boolean isSameDirectionAs(Move other) {
		return isSameXDirectionAs(other)  &&  isSameYDirectionAs(other);
	}

	/** Returns true if this move is in the opposite direction of the given other move. */
	public final boolean isOppositeDirectionTo(Move other) {
		return normalize().isOppositeOf(other.normalize());
	}

	/** Returns true if this move is the exact opposite of the given other move; that is, if
	  * its x and y coordinates are the negative of the other's.
	  */
	public final boolean isOppositeOf(Move other) {
		return dx == -other.dx  &&  dy == -other.dy;
	}

	/** Returns true if this move's x coordinate has the same sign as that of the given other move. */
	public final boolean isSameXDirectionAs(Move other) {
		return (dx < 0  &&  other.dx < 0)  ||  (dx == 0  &&  other.dx == 0)  ||  (dx > 0  &&  other.dx > 0);
	}

	/** Returns true if this move's y coordinate has the same sign as that of the given other move. */
	public final boolean isSameYDirectionAs(Move other) {
		return (dy < 0  &&  other.dy < 0)  ||  (dy == 0  &&  other.dy == 0)  ||  (dy > 0  &&  other.dy > 0);
	}

	/** Returns the magnitude of this move; the greater of the absolute values of its x/y coordinates. */
	public final int getMagnitude() {
		return (int)Math.max(Math.abs(dx), Math.abs(dy));
	}

	/** Returns a new move that represents this move scaled by the given factor. */
	public final Move times(int n) {
		return newMove(dx * n, dy * n);
	}

	/** Returns a new move that represents this move plus the coordinates of the given move. */
	public final Move plus(Move mov) {
		return newMove(dx + mov.dx, dy + mov.dy);
	}

	/** Returns a new move that represents this move with its signs switched. */
	public final Move reverse() {
		return times(-1);
	}

	/** Returns a new move that represents this move rotated 90-degrees to the left. */
	public final Move rotateLeft() {
		int mag = getMagnitude();
		if (isSameDirectionAs(Move.LEFT))
			return Move.DOWN.times(mag);
		else if (isSameDirectionAs(Move.RIGHT))
			return Move.UP.times(mag);
		else if (isSameDirectionAs(Move.UP))
			return Move.LEFT.times(mag);
		else if (isSameDirectionAs(Move.DOWN))
			return Move.RIGHT.times(mag);

		return Move.NEUTRAL;
	}

	/** Returns a new move that represents this move rotated 90-degrees to the right. */
	public final Move rotateRight() {
		return rotateLeft().reverse();
	}

	/** Returns a new Move with magnitudes reduced to 1. */
	public final Move normalize() {
		return crop(1);
	}

	/** Returns a new Move whose components are no more in magnitude than the given amount. */
	public final Move crop(int factor) {
		int newdx = (dx == 0)  ?  0  :  (Math.abs(dx) > factor)  ?  dx * factor / Math.abs(dx)  :  dx;
		int newdy = (dy == 0)  ?  0  :  (Math.abs(dy) > factor)  ?  dy * factor / Math.abs(dy)  :  dy;

		return newMove(newdx, newdy);
	}

	/** Returns true if this move has equal x/y coordinates to the other move. */
	public boolean equals(Object other) {
		try {
			Move otherMove = (Move)other;
			return dx == otherMove.dx  &&  dy == otherMove.dy;
		} catch (Exception e) {
			return false;
		}
	}

	/** Returns true if this move has the given x/y coordinates. */
	public boolean equals(int x, int y) {
		return dx == x  &&  dy == y;
	}

	/** Parses the given string to determine what Move it represents, then returns that move.
	  * The string "(2, -1)" will return a new Move with dx = 2 and dy = -1.
	  */
	public static final Move parseMove(String word) {
		try {
			//System.out.println("word = " + word);
			int dx = Integer.parseInt(word.substring(word.indexOf("(") + 1, word.indexOf(",")));
			int dy = Integer.parseInt(word.substring(word.indexOf(",") + 2, word.indexOf(")")));
			return newMove(dx, dy);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse into a Move: " + word);
		}
	}

	/** Returns a String representation of this Move.
	  * The move with dx = 2 and dy = -1 will return (2, -1) from its toString method.
	  */
	public String toString() {
		return "(" + dx + ", " + dy + ")";
	}
}