package pacman.sprite;

import pacman.model.GameModel;
import pacman.sprite.MovingSprite;

/** A dummy sprite that stands in for another in collision tests.
  * Used to avoid actually moving and colliding the game's important sprites,
  * since this would trigger unwanted game events.
  */
public class DummySprite extends MovingSprite {
	private MovingSprite myOther = null;

	/** Constructs a new dummy sprite. */
	public DummySprite() {
		super("DUMMY", 0, 0);
		type = DUMMYSPRITE;
	}

	/** Constructs a new dummy to stand in for the given other sprite. */
	public DummySprite(MovingSprite other) {
		super(other);
		myOther = other;
	}

	/** Returns the moving sprite in for which this dummy stands. */
	public final MovingSprite getOther() {
		return myOther;
	}

	/** Sets this dummy's stand-in sprite to be the given other sprite. */
	public final void setOther(MovingSprite other) {
		myOther = other;
		rect.x = other.rect.x;
		rect.y = other.rect.y;
		rect.width = other.rect.width;
		rect.height = other.rect.height;
		isVisible = other.isVisible;
		//setBounds(other.getX(), other.getY(), other.getWidth(), other.getHeight());
		//setVisible(other.isVisible);
//		setStatus(other.getStatus());
//		setColor(other.getColor());
//		setImageName(other.getImageName());
	}

	/** Notifies this dummy of an update to the given game model (does nothing). */
	public void update(GameModel gm) {}

	/** Returns a string representation of this dummy sprite. */
	public String toString() { return " "; }
}