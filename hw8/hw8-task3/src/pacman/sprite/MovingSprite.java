package pacman.sprite;

import pacman.model.GameModel;
import pacman.model.Move;
import pacman.model.MoveList;
import pacman.sprite.Sprite;

/** A moving sprite that can change position over time. */
public abstract class MovingSprite extends Sprite {
	// protected int dx = 0, dy = 0, speed = 0;
	protected int speed = 0;
	protected Move myVelocity = Move.NEUTRAL;
	protected Move myLastMove = Move.NEUTRAL;
	protected MoveList myMoveList = null;

	/** Constructs a new moving sprite with the given name and x/y pixel position. */
	public MovingSprite(String name, int x, int y) {
		super(name, x, y);
	}

	/** Constructs a new moving sprite with the given name, size, and x/y pixel position. */
	public MovingSprite(String name, int x, int y, int size) {
		super(name, x, y, size);
	}

	/** Constructs a new moving sprite that mirrors the state of the given other sprite. */
	public MovingSprite(MovingSprite spr) {
		super(spr);
		setSpeed(spr.getSpeed());
		setVelocity(spr.getdX(), spr.getdY());
	}

	/** Returns this moving sprite's speed in pixels in the x direction. */
	public final int getdX() {
		return myVelocity.dx;
	}

	/** Returns this moving sprite's speed in pixels in the y direction. */
	public final int getdY() {
		return myVelocity.dy;
	}

	/** Returns this moving sprite's overall speed. */
	public final int getSpeed() {
		return speed;
	}

	/** Returns this sprite's current move that it makes if it continues the way it is going. */
	public final Move getCurrentMove() {
//		if (isMoving()) {
			// x = GameModel.roundOff(x + dx);
			// y = GameModel.roundOff(y + dy);
			// return Move.newMove(dx, dy);
//		}
//		else return Move.NEUTRAL;
		return myVelocity;
	}

	/** Returns this sprite's last move that it has made. */
	public final Move getLastMove() {
		return myLastMove;
	}

	/** Returns this sprite's next move that it would like to make.
	  * This move is either the same as getCurrentMove(), to continue
	  * in the way the ghost is currently going; or, if the ghost has
	  * queued moves in his list, it is the first move off the list.
	  */
	public final Move getDesiredMove() {
		if (hasQueuedMoves())
			myVelocity = myMoveList.firstMove();

		return myVelocity;
	}

	/** Sets this moving sprite's speed in the x direction to the given value. */
	public final void setdX(int n) {
		// dx = n;
		myVelocity = Move.newMove(n, myVelocity.dy);
	}  // if (Math.abs(dx) > Math.abs(speed))  throw new RuntimeException("bad speed"); }

	/** Sets this moving sprite's speed in the y direction to the given value. */
	public final void setdY(int n) {
		// dy = n;
		myVelocity = Move.newMove(myVelocity.dx, n);
	}  // if (Math.abs(dy) > Math.abs(speed))  throw new RuntimeException("bad speed"); }

	/** Sets this moving sprite's overall speed to the given value. */
	public void setSpeed(int n) {
		speed = n;

		// if we are not at a position that is an even multiple of
		// this speed, adjust position
		if (speed != 0) {
			rect.x = rect.x / speed * speed;
		    rect.y = rect.y / speed * speed;
		}
	}

	/** Sets this moving sprite's speed in both x/y direction to the given values. */
	public final void setVelocity(int thedX, int thedY) {
		// setdX(thedX);
		// setdY(thedY);
		setVelocity(Move.newMove(thedX, thedY));
	}

	/** Sets this sprite's velocity to equal that of the given move. */
	public final void setVelocity(Move m) {
		// myVelocity = m.crop(getSpeed());
		myVelocity = m;
		// setSpeed(m.getMagnitude());
	}

	/** Sets this sprite's last move to the given move. */
	public final void setLastMove(Move mov) {
		// System.out.println("setLastMove(Move): " + getClass().getName() + " setting to " + mov);
		myLastMove = mov;
	}

	/** Returns whether or not this sprite is moving. */
	public final boolean isMoving() {
		// return dx != 0  ||  dy != 0;
		return myVelocity != Move.NEUTRAL;
	}

	/** Returns whether or not this sprite is moving up. */
	public final boolean isMovingUp() {
		// return dy < 0;
		return myVelocity.dy < 0;
	}

	/** Returns whether or not this sprite is moving down. */
	public final boolean isMovingDown() {
		// return dy > 0;
		return myVelocity.dy > 0;
	}

	/** Returns whether or not this sprite is moving left. */
	public final boolean isMovingLeft() {
		// return dx < 0;
		return myVelocity.dx < 0;
	}

	/** Returns whether or not this sprite is moving right. */
	public final boolean isMovingRight() {
		// return dx > 0;
		return myVelocity.dx > 0;
	}

	/** Instructs this sprite to stop moving. */
	public final void stop() {
		//dx = 0;
		//dy = 0;
		myVelocity = Move.NEUTRAL;
	}

	/** Makes this sprite make the given move, but not change its original
	  * velocity for subsequent moves.
	  */
	public final void go(Move m) {
		// System.out.println(getClass().getName() + ": go(Move): " + m);
		rect.x = rect.x + m.dx;
		rect.y = rect.y + m.dy;
		setLastMove(m);
	}

	/** Returns whether this sprite has moves in a list that are to be made. */
	public final boolean hasQueuedMoves() {
		return myMoveList != null  &&  !myMoveList.isEmpty();
	}

	/** Returns number of moves left in move queue to be made. */
	public final int getNumQueuedMoves() {
		return myMoveList == null  ?  0  :  myMoveList.size();
	}

	/** Adds the moves in the given move list to this sprite's move queue. */
	public final void queueMoves(MoveList list) {
		if (myMoveList == null)
			myMoveList = list;
		else
			myMoveList.merge(list);
	}

	/** Adds the given move to this sprite's move queue. */
	public final void queueMove(Move mov, int numTimes) {
		if (myMoveList == null)
			myMoveList = new MoveList();

		myMoveList.addMoves(mov, numTimes);
	}

	/** Adds the given move to this sprite's move queue. */
	public final void queueMove(Move mov) {
		queueMove(mov, 1);
	}

	/** Erases all moves in this sprite's move queue. */
	public final void killMoves() {
		myMoveList = null;
	}

	/** Constrains this sprite's position to the given maximum width and height. */
	public final void wrap(int w, int h) {
		if (rect.x <= -rect.width)
			rect.x = w - speed;
		else if (rect.x >= w)
			rect.x = -rect.width + speed;

		if (rect.y <= -rect.height)
			rect.y = h - 1;
		else if (rect.y >= h)
			rect.y = -rect.height + speed;
	}

	/** Returns a string representation of this sprite's move list. */
	public String dumpMoves() {
		return (myMoveList != null)  ?  myMoveList.toString()  :  "null";
	}

	/** Returns a detailed string representation of this sprite. */
	public String dump() { return super.dump() + ",s=" + speed + ", v=" + myVelocity; }

	/** Notifies this moving sprite of updates in the given game model. */
	public abstract void update(GameModel model);
}