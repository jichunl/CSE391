package pacman.sprite;

import java.awt.Color;
import pacman.model.*;
import pacman.sprite.EdibleSprite;
import pacman.strategy.*;
import pacman.view.PacManColors;

/** A Ghost, which is Pac-Man's main enemy in the game. */
public class Ghost extends EdibleSprite {
	public static final int TYPE = GHOST;

	/** The speed of a normal ghost. */
	public static final int GHOST_SPEED             = 60 / GameModel.UPDATES_PER_SECOND;

	/** The speed of a scared ghost. */
	public static final int SCARED_GHOST_SPEED      = GHOST_SPEED / 2;

	/** The speed of a ghost in his cage. */
	public static final int CAGED_GHOST_SPEED       = GHOST_SPEED / 2;

	/** The speed of a ghost who is stuck in a tunnel. */
	public static final int GHOST_SPEED_IN_TUNNEL   = GHOST_SPEED / 2;

	/** The speed of a ghost who has been eaten and is running back to his hive. */
	public static final int EATEN_GHOST_SPEED       = GHOST_SPEED * 2;

	/** Counters for the numbers of instances of various types of ghosts. */
	public static int ourScaredCount = 0;
	public static int ourEatenCount  = 0;
	public static int ourGhostCount  = 0;
	public static int ourCagedCount  = 0;

	/** Returns true if there are any instances of Ghosts who are chasing pac-man.
	  * Used to know what sounds to play.
	  */
	public static boolean areAngryGhosts() {
		return ourScaredCount + ourEatenCount < ourGhostCount;
	}

	/** Returns true if there are any instances of Ghosts who are eaten and dead.
	  * Used to know what sounds to play.
	  */
	public static boolean areEatenGhosts() {
		return ourEatenCount  > 0;
	}

	/** Returns true if there are any instances of Ghosts who are scared.
	  * Used to know what sounds to play.
	  */
	public static boolean areScaredGhosts() { return ourScaredCount > 0; }

	/** Returns a Blinky, the red ghost, at the given pixel coordinates. */
	public static Ghost createBlinky(int x, int y) {
		Ghost gh = new Ghost("blinky", x, y, PacManColors.RED);
		gh.setNormalStrategy(new SmartStrategy(gh));
		ourGhostCount++;
		return gh;
	}

	/** Returns a Clyde, the orange ghost, at the given pixel coordinates. */
	public static Ghost createClyde(int x, int y) {
		Ghost gh = new Ghost("clyde", x, y, PacManColors.ORANGE);
		// gh.setNormalStrategy(new SmartAheadStrategy(gh));
		gh.setNormalStrategy(new SmartStrategy(gh));
		ourGhostCount++;
		return gh;
	}

	/** Returns an Inky, the cyan ghost, at the given pixel coordinates. */
	public static Ghost createInky(int x, int y) {
		Ghost gh = new Ghost("inky", x, y, PacManColors.CYAN);
		gh.setNormalStrategy(new TurnStrategy(gh));
		ourGhostCount++;
		return gh;
	}

	/** Returns a Pinky, the pink ghost (duh), at the given pixel coordinates. */
	public static Ghost createPinky(int x, int y) {
		Ghost gh = new Ghost("pinky", x, y, PacManColors.PINK);
		gh.setNormalStrategy(new LineOfSightStrategy(gh));
		ourGhostCount++;
		return gh;
	}

	/** Returns a Satan, the special green ghost, at the given pixel coordinates. */
	public static Ghost createSatan(int x, int y) {
		Ghost gh = new Ghost("satan", x, y, PacManColors.GREEN);
		gh.setNormalStrategy(new SuperSmartStrategy(gh));
		ourGhostCount++;
		return gh;
	}


	private String myName;
	private Strategy myCurrentStrategy,
		myNormalStrategy,
		myScaredStrategy,
		myEatenStrategy,
		myCageStrategy;
	private Color myNormalColor;
	private int myRespawnX;
	private int myRespawnY;
	private int myInitialDelay = 0;
	private int myCageCounter = 0;
	private int myScaredCounter = 0;
	private boolean myShouldAnimate = true;
	private boolean myIsCleanedUp = false;
	private boolean myShouldConsultStrategy = true;

	/** Constructs a new dummy ghost of the given name, coordinates, and color. */
	public Ghost(String name, int thex, int they, Color normalColor) {
		super(name + "neutral1", thex, they);

		myNormalStrategy = new RandomStrategy(this);
		myScaredStrategy = new ScaredStrategy(this);
		myEatenStrategy  = new ReviveStrategy(this);
		myCageStrategy   = new CageStrategy(this);
		myNormalColor = normalColor;

		setStatus(STATUS_NORMAL);

		setScore(200);
		setRespawnX(getInitialX());
		setRespawnY(getInitialY());

		// ourGhostCount++;
		myName = name;
		type = GHOST;
	}

	/** Wipes out this ghost's strategies so he will not move. */
	public void clearAllStrategies() {
		myCurrentStrategy = null;
		myNormalStrategy  = null;
		myScaredStrategy  = null;
		myEatenStrategy   = null;
		myCageStrategy    = null;

		myShouldConsultStrategy = false;
		killCounters();
		setStatus(STATUS_ZOMBIE);
	}

	/** Makes this ghost('s strategy) calculate his next move he'd like to make and return it. */
	public Move calculateMove(Level level, MovingSprite target) {
		if (myStatus == STATUS_NORMAL)
			setSpeed(level.isInTunnel(this)  ?  GHOST_SPEED_IN_TUNNEL  :  GHOST_SPEED);

		if (myShouldConsultStrategy  &&  myCurrentStrategy != null  &&  myCurrentStrategy.shouldMove()) {
			// most strategies move every time
			Move mov = myCurrentStrategy.getMove(level, target);

			if (myCurrentStrategy.shouldMoveAgain()) {
				// ghosts occasionally get 2 moves so they're
				// slightly faster than pac-man
				// System.out.println("moving again");
				mov = mov.plus(myCurrentStrategy.getMove(level, target));
			}

			setVelocity(mov);
			return getCurrentMove();
		}
		else
			return Move.NEUTRAL;
	}

	/** Called when this ghost is no longer needed; used for sounds. */
	public void cleanUp() {
		// System.err.println("garbage collecting ghost " + dump());
		revive();
		ourGhostCount--;
		// super.finalize();
		myIsCleanedUp = true;
	}

	/** Called when this Ghost is garbage-collected. */
	protected void finalize() throws Throwable {
		if (!myIsCleanedUp)
			cleanUp();
		super.finalize();
	}

	/** Returns how long this ghost will still remain in his cage. */
	public int getCageDelay() {
		return myInitialDelay;
	}

	/** Returns this Ghost's color.  The color will be blue/white if the ghost is scared. */
	public Color getColor() {
		// make sure ghost is appropriate color
		Color color = myNormalColor;
		if (isScared()) {
			if (!isAlmostNotScared()
					||  myScaredCounter % GameModel.UPDATES_PER_SECOND
					    < GameModel.UPDATES_PER_SECOND / 2)
				color = PacManColors.BLUE;
			else
				color = PacManColors.WHITE;
		} else if (isEaten())
			color = PacManColors.WHITE;

		return color;
	}

	/** Returns this Ghost's name. */
	public String getName() {
		return myName;
	}

	/** Returns the x pixel position at which this ghost will return to revive himself. */
	public int getRespawnX() {
		return myRespawnX;
	}

	/** Returns the y pixel position at which this ghost will return to revive himself. */
	public int getRespawnY() {
		return myRespawnY;
	}

	/** Returns this ghost's currently active strategy. */
	public Strategy getStrategy() {
		return myCurrentStrategy;
	}

	/** Returns whether or not this ghost is at his x/y coordinates to respawn himself. */
	public boolean isAtRespawnPosition() {
		return getX() == myRespawnX  &&  getY() == myRespawnY;
	}

	/** Returns true if this ghost is in his cage still. */
	public boolean isInCage() {
		return myCageCounter > 0  ||  myStatus == STATUS_CAGED  ||  myStatus == STATUS_LEAVING_CAGE;
	}

	/** Returns true if this ghost is scared. */
	public boolean isScared() {
		return myScaredCounter > 0  ||  myStatus == STATUS_SCARED;
	}

	/** Resets this ghost's update counter so he will revert to his caged behavior. */
	public void killCounter() {
		myCageCounter = 0;
		setStatus(myScaredCounter > 0  ?  STATUS_SCARED  :  STATUS_NORMAL);
	}

	/** Kills all of this ghost's update counters so he will behave normally. */
	public void killCounters() {
		myCageCounter = 0;
		myScaredCounter = 0;
//		setStatus(STATUS_NORMAL);
	}

	/** Notifies this ghost that pac-man has died.  Normally a ghost passes this information
	  * on to his strategy so it can reset itself.
	  */
	public void notifyOfPacManDeath() {
		if (myNormalStrategy != null)
			myNormalStrategy.notifyOfPacManDeath();
	}

	/** Moves this ghost back to his original position. */
	public void returnToStart() {
		super.returnToStart();
		stop();
		setStatus(STATUS_NORMAL);
		//System.out.println(dump() + " returning to start");
		setCageDelayCounter(myInitialDelay);
	}

	/** Revives this ghost from the dead. */
	public void revive() {
		// set the ghost back to caged mode so he will escape from the cage
		setStatus(STATUS_NORMAL);
		setCageDelayCounter(Math.max(1, myCageCounter));
	}

	/** Makes this ghost scared of pac-man for the given number of updates. */
	public void scare(int howLong) {
		if (howLong <= 0)
			throw new IllegalArgumentException("invalid time");

//		System.out.println("Scaring ghost for " + howLong);

		myScaredCounter = howLong;
		if (!isInCage()) {
//			System.out.println("scared!");
			setStatus(STATUS_SCARED);
		}
		// else System.out.println("not scared!  in cage");

//		System.out.println("after scaring: ");
//		System.out.println(dump());
	}

	/** Sets this ghost's cage update counter to the given value, which will
	  * make the ghost stay in his cage for this many updates.
	  */
	public void setCageDelay(int n) {
		myInitialDelay = n;
		setCageDelayCounter(n);
	}

	/** Sets this ghost's cage update counter to the given value, which will
	  * make the ghost stay in his cage for this many updates.
	  */
	private void setCageDelayCounter(int n) {
		myCageCounter = n;

		if (myCageCounter > 0) {
			setStatus(STATUS_CAGED);
		}
	}

	/** Makes ghost take an extra move (get harder) the given percent of the time. */
	public void setDifficultyLevel(int n) {
		myNormalStrategy.setMoveAgainPercentage(n);
	}

	/** Sets this ghost's normal strategy, the one he will use to chase pac-man,
	  * to the given value.
	  */
	public void setNormalStrategy(Strategy strat)  {
		myNormalStrategy = strat;
		if (myStatus == STATUS_NORMAL)
			myCurrentStrategy = strat;
	}

	/** Sets this ghost's respawning position to the given x and y pixel values. */
	public void setRespawnPosition(int rx, int ry) {
		myRespawnX = rx;
		myRespawnY = ry;
	}

	/** Sets this ghost's respawning x position to the given x pixel value. */
	public void setRespawnX(int rx) {
		myRespawnX = rx;
	}

	/** Sets this ghost's respawning y position to the given y pixel value. */
	public void setRespawnY(int ry) {
		myRespawnY = ry;
	}

	/** Sets this ghost's speed to the given value. */
	public void setSpeed(int n) {
		super.setSpeed(n);

	}

	/** Sets this ghost's status to the given value. */
	public void setStatus(Object status) {
		Object oldStatus = getStatus();
		super.setStatus(status);

		// do tasks associated with leaving a particular status
		if (oldStatus == STATUS_EATEN  &&  status != STATUS_EATEN)
			synchronized (getClass()) { ourEatenCount--; }
		else if (oldStatus == STATUS_SCARED  &&  status != STATUS_SCARED)
			synchronized (getClass()) { ourScaredCount--; }
		else if (oldStatus == STATUS_CAGED  &&  status != STATUS_CAGED)
			synchronized (getClass()) { ourCagedCount--; }

		// do tasks associated with going to a particular status
		if (status == STATUS_NORMAL) {
			setVisible(true);
			setSpeed(GHOST_SPEED);
			setStrategy(myNormalStrategy);
			notifyOfPacManDeath();  // *** ? why
		}
		else if (status == STATUS_CAGED  ||  status == STATUS_LEAVING_CAGE) {
			synchronized (getClass()) { ourCagedCount++; }
			setSpeed(CAGED_GHOST_SPEED);
			setStrategy(myCageStrategy);
		}
		else if (oldStatus != STATUS_EATEN  &&  status == STATUS_EATEN) {
			synchronized (getClass()) { ourEatenCount++; }
			setSpeed(EATEN_GHOST_SPEED);
			setStrategy(myEatenStrategy);
			myScaredCounter = 0;
		}
		else if (oldStatus != STATUS_SCARED  &&  status == STATUS_SCARED) {
			synchronized (getClass()) { ourScaredCount++; }
			setSpeed(SCARED_GHOST_SPEED);
			setStrategy(myScaredStrategy);
		}
	}

	/** Sets this ghost's current strategy to the given one. */
	private void setStrategy(Strategy strat)  {
//		if (myShouldConsultStrategy)
		myCurrentStrategy = strat;
	}

	/** Returns whether or not this ghost should animate itself. */
	public boolean shouldAnimate() {
		return myShouldAnimate;
	}

	/** Returns a string representation of this ghost. */
	public String toString() {
		return "G";
	}

	/** Notifies this ghost of an update in the given game model. */
	public void update(GameModel gm) {
		super.update(gm);  // checks things related to just being eaten

		boolean doingMovement = gm.isDoingMovement();

		// make sure ghost is in right status
		if (myScaredCounter > 0  &&  doingMovement) {
			myScaredCounter--;
			if (myScaredCounter == 0) {
				// recovers from being afraid
				if (myStatus != STATUS_LEAVING_CAGE) {
					setStatus(STATUS_NORMAL);
					setCageDelayCounter(myCageCounter);
				}
			}
		}

		if (myCageCounter > 0  &&  doingMovement) {
			if (myStatus != STATUS_CAGED) {
				// setStatus(STATUS_CAGED);
				// throw new RuntimeException(dump() + " should not be here");
			} else {
				myCageCounter--;
				if (myCageCounter == 0) {
					// ready to leave cage
					setStatus(STATUS_LEAVING_CAGE);
				}
			}
		}
	}

	/** Returns true if this ghost is almost through being scared. */
	public boolean isAlmostNotScared() {
		return 0 < myScaredCounter
			&& myScaredCounter <= GameModel.UPDATES_PER_SECOND * GameModel.PELLET_WEARING_OFF_TIME;
	}

	/** Returns true if this ghost was eaten recently. */
	public boolean wasJustEaten() {
		return isEaten()  &&  myNumUpdatesSinceEaten < GameModel.UPDATES_PER_SECOND;
	}

	/** Makes this ghost stop animating. */
	public void zombify() {
		myShouldAnimate = false;
	}

	/** Returns a detailed string representation of this ghost. */
	public String dump() {
		return super.dump() + ", Ccnt=" + myCageCounter + ", Scnt=" + myScaredCounter + ", strat=" + (myCurrentStrategy != null  ?  myCurrentStrategy.getName()  :  "null");
	}
}