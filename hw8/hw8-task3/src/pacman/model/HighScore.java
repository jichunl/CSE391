package pacman.model;

/** Represents one high score in a game. */
public class HighScore implements Comparable<HighScore> {
	private final int myScore;
	private final String myName;

	/** Constructs a new high score with the given number of points and name. */
	public HighScore(int score, String name) {
		myScore = score;
		myName = name;
	}

	/** Returns this score's number of points. */
	public int getScore() {
		return myScore;
	}

	/** Returns this score's name. */
	public String getName() {
		return myName;
	}

	/** Returns an integer comparison by points between this score and the given other score. */
	public int compareTo(HighScore other) {
		return myScore - other.myScore;
	}

	/** Returns true if this high score has the same number of points as the other score. */
	public boolean equals(Object other) {
		try {
			return compareTo((HighScore) other) == 0;
		} catch (ClassCastException cce) {
			return false;
		}
	}

	/** Returns a string representation of this high score. */
	public String toString() {
		return myScore + " : " + myName;
	}
}