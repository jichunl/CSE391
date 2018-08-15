package pacman.sprite;

import pacman.model.GameModel;
import pacman.view.PacManColors;
import java.awt.Color;

/** Represents Pac-Man himself, the character controlled by the player. */
public class PacMan extends MovingSprite {
	/** Pac-Man's speed of movement in pixels per update. */
	public static final int PAC_SPEED   =  60 / GameModel.UPDATES_PER_SECOND;
	// takes 3.8s to cross board at this speed

	private boolean isInvincible = false;
	public boolean isAlive = true;
	private int myNumUpdatesSinceKilled  = 0;
	private int myNumUpdatesSinceRevived = 0;
	private int myNumUpdatesSinceAteDot  = 9999;  // kludge

	/** Constructs a new Pac-Man at the given pixel coordinates. */
	public PacMan(int thex, int they) {
		super("pacman", thex, they);
		setColor(PacManColors.YELLOW);
		setSpeed(PAC_SPEED);
		setVelocity(-PAC_SPEED, 0);  // starts out moving left by default

		// *** changing this to implement pre-game animations -- may break something
		myNumUpdatesSinceKilled  = 0;
		myNumUpdatesSinceRevived = 0;
		type = PACMAN;
	}

	/** Notifies this Pac-Man that he has eaten a dot or pellet. */
	public void chompDot() {
		myNumUpdatesSinceAteDot = 0;
	}

	/** Returns this Pac-Man's color. */
	public Color getColor() {
		return (isAlive)  ?  super.getColor()  :  PacManColors.WHITE;
	}

	/** Returns this Pac-Man's name. */
	public String getName() {
		return "Pac-Man";
	}

	/** Returns whether this Pac-Man is currently alive. */
	public boolean isAlive() {
		return isAlive;
	}

	/** Returns whether this Pac-Man is currently eating dots. */
	public boolean isEating() {
		return myNumUpdatesSinceAteDot <= rect.width / PAC_SPEED + 1;
	}

	/** Returns whether this Pac-Man is currently invincible (has eaten a pellet). */
	public boolean isInvincible()        { return isInvincible; }

	/** Notifies this Pac-Man that he has been killed. */
	public void kill() {
		setAlive(false);
		myNumUpdatesSinceKilled  = 0;
		myNumUpdatesSinceRevived = 0;
	}

	/** Resets this Pac-Man's update counters after a level is cleared. */
	public void killCounters() {
		myNumUpdatesSinceKilled  = 5*GameModel.UPDATES_PER_SECOND;
		myNumUpdatesSinceRevived = 5*GameModel.UPDATES_PER_SECOND;
	}

	/** Returns the number of game updates since this Pac-Man was killed. */
	public int getNumUpdatesSinceKilled() {
		return myNumUpdatesSinceKilled;
	}

	/** Returns the number of game updates since this Pac-Man was revived. */
	public int getNumUpdatesSinceRevived() {
		return myNumUpdatesSinceRevived;
	}

	/** Notifies this Pac-Man that he has been revived from the dead. */
	public void revive() {
		setAlive(true);
		returnToStart();
		myNumUpdatesSinceKilled  = 0;
		myNumUpdatesSinceRevived = 0;
	}

	/** Sets this Pac-Man to be alive or dead. */
	private void setAlive(boolean b) { isAlive = b; }

	/** Sets this Pac-Man to be invincible or not. */
	public void setInvincible(boolean b) { isInvincible = b; }

	/** Returns whether or not this Pac-Man has just been killed by an enemy. */
	public boolean wasJustKilled() {
		return !isAlive  &&  myNumUpdatesSinceKilled < GameModel.UPDATES_PER_SECOND;
	}

	/** Notifies this Pac-Man that the given game model has updated. */
	public void update(GameModel gm) {
		if (isAlive)
			myNumUpdatesSinceRevived++;
		else
			myNumUpdatesSinceKilled++;

		myNumUpdatesSinceAteDot++;
	}

	/** Returns whether this Pac-Man has just been revived from the dead. */
	public boolean wasJustRevived() {
		return isAlive  &&  myNumUpdatesSinceRevived < GameModel.UPDATES_PER_SECOND * 2;
	}

	/** Returns a String representation of this Pac-Man. */
	public String toString() { return "P"; }
}