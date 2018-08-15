package pacman.model;

import java.io.*;
import java.util.*;

/** Represents a collection of high scores in the game. */
public class HighScoreList {
	private static final int MAX_HIGH_SCORES = 10;

	private List<HighScore> myHighScores = new ArrayList<HighScore>(MAX_HIGH_SCORES);

	/** Constructs a new empty high score list. */
	public HighScoreList() {}

	/** Constructs a new high score list with the given scores in it. */
	public HighScoreList(HighScore[] highScores) {
		clear(highScores);
	}

	/** Removes all elements from this high score list. */
	public void clear() {
		myHighScores.clear();
	}

	/** Sets this high score list to contain only the scores in the given array. */
	public void clear(HighScore[] highScores) {
		myHighScores.clear();
		for (int i = 0;  i < highScores.length;  i++)
			myHighScores.add(highScores[i]);
	}

	/** Returns the highest number of points in this high score list. */
	public int getHighScore() {
		if (getSize() < 1)
			// throw new RuntimeException("should not be in this method with no high scores!");
			return 100;

		// return Math.max(getScore(), getHighScore(0).getScore());
		return getHighScore(0).getScore();
	}

	/** Returns the score at the given index. */
	public HighScore getHighScore(int index) {
		return (HighScore)myHighScores.get(index);
	}

	/** Returns the size of this high score list. */
	public int getSize() {
	 	return myHighScores.size();
	}

	/** Returns true if this list has no scores in it. */
	public boolean isEmpty() {
		return myHighScores.size() == 0;
	}

	/** Returns true if the given score would make the high score list. */
	public boolean wouldMakeList(int score) {
		if (myHighScores.size() < MAX_HIGH_SCORES)
			return true;
		HighScore lowestHighScore = myHighScores.get(myHighScores.size() - 1);
		return new HighScore(score, "").compareTo(lowestHighScore) > 0;
	}

	/** Loads a high score list from the given file. */
	public void load(String fileName) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		while (in.ready()) {
			String line = in.readLine();
			if (line == null)
				break;

			line = line.trim();
			if (line.equals(""))
				break;

			StringTokenizer tokenizer = new StringTokenizer(line);
			if (tokenizer.countTokens() < 2)
				throw new IOException("invalid score data");

			int score = 0;
			try {
				score = Integer.parseInt(tokenizer.nextToken());
			} catch (NumberFormatException nfe) {
				throw new IOException("invalid score number format:\n" + nfe);
			}
			String name = line.substring(line.indexOf(tokenizer.nextToken()), line.length());
			HighScore highScore = new HighScore(score, name);
			myHighScores.add(highScore);
		}
		sort();
	}

	/** Tries to save the high scores to the given file.
	  * @return whether or not the save was successful.
	  */
	public boolean save(String fileName) {
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(fileName));
			int size = getSize();
			for (int i = 0;  i < size;  i++) {
				HighScore score = getHighScore(i);
				out.println(score.getScore() + '\t' + score.getName());
			}
			return true;
		} catch (Exception ioe) {
			return false;
		}
	}

	/** Attempts to add the current game's score to the list of high scores.
	  * @return whether or not the score made the list.
	  */
	public boolean record(String name, int score) {
		if (wouldMakeList(score)) {
			HighScore hiscore = new HighScore(score, name);
			myHighScores.add(hiscore);
			sort();
			return myHighScores.contains(hiscore);
		}
		else return false;
	}

	/** Sorts this high score list in decending order. */
	public void sort() {
		Collections.sort(this.myHighScores, Collections.reverseOrder());

		// trim out if there are too many high scores
		while (myHighScores.size() > MAX_HIGH_SCORES)
			myHighScores.remove(myHighScores.size() - 1);
	}
}
