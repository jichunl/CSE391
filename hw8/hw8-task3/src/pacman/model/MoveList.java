package pacman.model;

import pacman.model.Move;
import java.util.*;

/** Represents a collection of moves to be made by a sprite. */
public class MoveList {
	private List<Move> myMoves = new ArrayList<Move>();

	/** Adds the given move to this move list. */
	public void addMove(Move mov) {
		addMoves(mov, 1);
	}

	/** Adds the given move the given number of times to this move list. */
	public void addMoves(Move mov, int numTimes) {
		for (int i = 0; i < numTimes; i++) {
			myMoves.add(mov);
        }
	}

	/** Removes all elements from this move list. */
	public void clear() {
		myMoves.clear();
	}

	/** Returns whether or not there are no moves in this list. */
	public boolean isEmpty() {
		return myMoves.isEmpty();
	}

	/** Returns the size of this move list. */
	public int size() {
		return myMoves.size();
	}

	/** Returns and removes the first move from this list. */
	public Move firstMove() {
		Move mov = myMoves.remove(0);
		return mov;
	}

	/** Returns the move at the given index of this list. */
	public Move getMoveAt(int index) {
		return myMoves.get(index);
	}

	/** Appends the moves in the given other list to this move list. */
	public void merge(MoveList other) {
		int size = other.size();
		for (int i = 0;  i < size;  i++) {
			addMove(other.getMoveAt(i));
		}
	}

	/** Returns a string representation of this move list. */
	public String toString() {
		return myMoves.toString();
	}

	/** Returns true if this MoveList has the same moves as the given other MoveList. */
	public boolean equals(Object o) {
		try {
			MoveList other = (MoveList)o;
			return myMoves.equals(other.myMoves);
		} catch (Exception e) {
			return false;
		}
	}
}