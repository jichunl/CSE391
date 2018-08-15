package pacman.sprite;

import pacman.model.GameModel;

/** Represents an edible sprite which may be consumed by Pac-Man. */
public abstract class EdibleSprite extends MovingSprite {
	public static final int TYPE = EDIBLESPRITE;

	protected int myScore = 0;
	protected int myNumUpdatesSinceEaten = 0;

	/** Constructs a new edible sprite with the given image name at the given coordinates. */
	public EdibleSprite(String image, int x, int y) {
		super(image, x, y);
	}

	/** Constructs a new edible sprite with the given image name and size at the given coordinates. */
	public EdibleSprite(String image, int x, int y, int size) {
		super(image, x, y, size);
	}

	/** Returns this edible's score. */
	public int getScore() {
		return myScore;
	}

	/** Sets this edible's score to the given amount. */
	public void setScore(int n) {
		myScore = n;
	}

	/** Returns true if this sprite is eaten. */
	public final boolean isEaten() {
		return myStatus == STATUS_EATEN;
	}

	/** Sets this edible's status to the given one. */
	public void setStatus(Object status) {
		Object oldStatus = getStatus();
		super.setStatus(status);

		if (status == STATUS_EATEN  ||  oldStatus == STATUS_EATEN) {
			// reset eaten timer if I was eaten before, but now I'm not
			myNumUpdatesSinceEaten = 0;
		}
	}

	/** Notifies this edible sprite of an update to the given game model. */
	public void update(GameModel gm) {
		if (isEaten())
			myNumUpdatesSinceEaten++;
	}

	/** Returns the number of updates passed since this edible was eaten. */
	public int getNumUpdatesSinceEaten() {
		return myNumUpdatesSinceEaten;
	}

	/** Returns true if this edible was just eaten. */
	public boolean wasJustEaten() {
		return isEaten()  &&  myNumUpdatesSinceEaten < GameModel.UPDATES_PER_SECOND;
	}
}