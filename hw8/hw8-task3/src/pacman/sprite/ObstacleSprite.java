package pacman.sprite;

import pacman.sprite.Sprite;
import java.awt.Color;

/** An obstacle sprite, which can be a wall or gate.
  * Ghosts may pass through gates while Pac-Man cannot.
  */
public abstract class ObstacleSprite extends Sprite {
	private int myHex = 15;

	/** Constructs a new obstacle sprite with the given image name and color
	  * at the given x/y pixel coordinates.
	  */
	public ObstacleSprite(String theImageName, int thex, int they, Color thecolor) {
		super(theImageName, thex, they);
		setColor(thecolor);
	}

	/** Returns the hexadecimal representation of this obstacle sprite.  This is
	  * determined by the game model by looking at the squares surrounding this
	  * obstacle.  The hex number represents the binary AND of the four corners of
	  * this obstacle's grid square, as shown in this picture:<p>
	  *
	  * 1 2                <br>
	  * 4 8
	  *
	  * <p>Normally a wall would fill all four quadrants of its square, but if
	  * this wall is not surrounded by other walls, it is punched in a bit.
	  * Hard to describe; but implementing this makes it possible to draw the
	  * game's levels nearer to their intended look.
	  */
	public int getHex() {
		return myHex;
	}

	/** Sets this obstacle's hex value to the given value. */
	public void setHex(int n) {
		if (!(0 <= n  &&  n <= 15))
			throw new RuntimeException("invalid hex");

		myHex = n;
	}
}