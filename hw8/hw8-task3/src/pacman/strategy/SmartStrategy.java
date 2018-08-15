package pacman.strategy;

import java.util.Stack;

import pacman.model.*;
import pacman.sprite.*;
import pacman.strategy.Strategy;
import pacman.utility.PriorityQueue;
import pacman.utility.PriorityQueue.Location;


/** A smart strategy that uses the A* shortest path algorithm to find the best way to
  * go to catch Pac-Man.
  */
public class SmartStrategy extends Strategy {
	protected int[][] myWeightMap = null;
	protected Stack<Move> myMoveStack;
	protected int mySpeedUsed = 0;

	/** Constructs a new smart strategy to move the given sprite. */
	public SmartStrategy(MovingSprite gh) {
		super(gh);
		myMoveStack = new Stack<Move>();
	}

	/** Notifies this strategy of pac-man's death. */
	public void notifyOfPacManDeath() {
		if (myMoveStack != null)
			myMoveStack.removeAllElements();
	}

	/** Returns true if there are no moves left in this strategy's move list. */
	protected boolean noMovesLeft() {
		return myMoveStack == null  ||  myMoveStack.isEmpty();
	}

	/** Returns this strategy's next move toward the given target. */
	public Move getMove(Level level, MovingSprite target) {
		//System.out.println("smart-strat targeting " + target.dump());

		if (myMoveStack == null)
			myMoveStack = new Stack<Move>();

		if (myMoveStack.isEmpty()  ||  mySprite.getSpeed() != mySpeedUsed) {
//			System.out.println("Recalculating path");
			Sprite[][] map = level.getGrid();
			int x = mySprite.getGridX(),
				y = mySprite.getGridY(),
				targetx = target.getGridX(),
				targety = target.getGridY(),
				gridWidth = map.length,
				gridHeight = map[0].length;

			// weigh the map using A* shortest-path algorithm!
			myMoveStack.removeAllElements();
			mySpeedUsed = mySprite.getSpeed();

			PriorityQueue openList = new PriorityQueue(300, PriorityQueue.ASCENDING, 0);  // size, type, min_weight
			Stack<Location> closedList  = new Stack<Location>();

			Location curr = new Location(null, x, y, 0,
					Math.min(Math.abs(targetx - x), Math.abs(targetx - x + ((targetx > x)  ?  gridWidth  :  -gridWidth)))
					+ Math.min(Math.abs(targety - y), Math.abs(targety - y + ((targety > x)  ?  gridHeight  :  -gridHeight))));
			Location child  = null;
			Location other  = null;
			Move mov = null;
			int i = 0;
			int openindex, closedindex;
			int movesPerSquare = Level.GRID_SIZE / mySpeedUsed;
			int childx, childy;

			// System.out.println("moving from grid " + x + "," + y + " to " + targetx + "," + targety);

			openList.push(curr);

			while (!openList.isEmpty()) {
				// System.out.println("A* non-empty open list; looping");
				// pluck off best element in open list
				// (priority queue rapes all, because it puts best first every time)
				curr = openList.pop();
				// System.out.println("trying best of " + curr);

				// check if current node is the "goal" node
				if (curr.x == targetx  &&  curr.y == targety) {
					// we found pac-man!  reconstruct path back
					// myMoveStack.removeAllElements();

					// add the moves generated by A*
					while (curr.parent != null) {
						mov = (curr.parent.x == (curr.x - 1 + gridWidth)  % gridWidth)   ?  Move.RIGHT
							: (curr.parent.x == (curr.x + 1 + gridWidth)  % gridWidth)   ?  Move.LEFT
							: (curr.parent.y == (curr.y - 1 + gridHeight) % gridHeight)  ?  Move.DOWN
							: (curr.parent.y == (curr.y + 1 + gridHeight) % gridHeight)  ?  Move.UP
							:  Move.NEUTRAL;

						mov = mov.times(mySprite.getSpeed());
						for (i = 0;  i < movesPerSquare;  i++)
							myMoveStack.push(mov);

						curr = curr.parent;
					}

					// last moves to be pushed (first moves to be made) are moves
					// to get ghost to nearest square
					int toX = x * Level.GRID_SIZE;
					int toY = y * Level.GRID_SIZE;
					int fromX = mySprite.getX();
					int fromY = mySprite.getY();
					int deltax = toX - fromX;
					int deltay = toY - fromY;
					int dx, dy;
					Move top = null;

					// while looping, we'll also check for and remove redundant moves!
					while (deltax != 0  ||  deltay != 0) {
						dx = (deltax < 0)  ?  (int)Math.max(-mySprite.getSpeed(), deltax)
								:  (deltax > 0)  ?  (int)Math.min(mySprite.getSpeed(), deltax)  :  0;
						dy = (deltay < 0)  ?  (int)Math.max(-mySprite.getSpeed(), deltay)
								:  (deltay > 0)  ?  (int)Math.min(mySprite.getSpeed(), deltay)  :  0;
						deltax -= dx;
						deltay -= dy;
						mov = Move.newMove(dx, dy);

						if (!myMoveStack.isEmpty()) {
							top = (Move)myMoveStack.peek();
							if (top.isOppositeOf(mov))
								// contradictory moves; pull them off
								myMoveStack.pop();
							else
								myMoveStack.push(mov);
						}
						else
							myMoveStack.push(mov);
					}

					// successfully regenerated path, so exit
					break;
				}

				else {
					// not at pac-man, so must see which ways we can go from this node
					int[] xPoints = new int[] {(curr.x - 1 + gridWidth)   % gridWidth,  curr.x, (curr.x + 1 + gridWidth)   % gridWidth};
					int[] yPoints = new int[] {(curr.y - 1 + gridHeight)  % gridHeight, curr.y, (curr.y + 1 + gridHeight)  % gridHeight};
					for (i = 0;  i < xPoints.length;  i++)
					for (int j = 0;  j < yPoints.length;  j++) {
						childx = xPoints[i];
						childy = yPoints[j];

					// for (childx = curr.x - 1;  childx <= curr.x + 1;  childx++)
					// for (childy = curr.y - 1;  childy <= curr.y + 1;  childy++) {
						// exclude diagonals and current square itself
						if ( (childx != curr.x  &&  childy != curr.y)
								||  (childx == curr.x  &&  childy == curr.y) )
							continue;

						// System.out.println("(" + curr.x + ", " + curr.y + ") has child of (" + childx + ", " + childy + ")");

						// for each valid move direction, push node onto open list
						// (if it's not already reachable in less moves)
						if (0 <= childx  &&  childx < map.length  &&  0 <= childy  &&  childy < map[0].length
								&&  !(map[childx][childy].type == Sprite.WALL)) {  // ObstacleSprite
							// child = new Location(curr, childx, childy, 1 + curr.cost, Math.abs(targetx - childx) + Math.abs(targety - childy));
							child = new Location(curr, childx, childy, 1 + curr.cost,
									Math.min(Math.abs(targetx - childx), Math.abs(targetx - childx + ((targetx > childx)  ?  -gridWidth  :  gridWidth)))
									+ Math.min(Math.abs(targety - childy), Math.abs(targety - childy + ((targety > childx)  ?  -gridHeight  :  gridHeight))));

							// System.out.println("(" + curr.x + ", " + curr.y + ")'s child (" + childx + ", " + childy + "): heur=" + child.heuristic + ", f=" + child.f);

							// check if this location is already on the CLOSED list but in less moves
							closedindex = closedList.indexOf(child);
							if (closedindex != -1) {
								other = (Location)closedList.elementAt(closedindex);
								if (other.f < child.f)
									continue;
							}

							// check if this location is already on the OPEN list but in less moves
							openindex = openList.indexOf(child, child.f);
							if (openindex != -1) {
								other = (Location)openList.elementAt(openindex);
								if (other.f < child.f)
									continue;
							}

							// don't allow ghost to do a 180-deg turn from the start (kludge?) (but it works!)
							if (child.parent != null  &&  child.parent.x == x  &&  child.parent.y == y
									&&  Move.newMove(child.x - child.parent.x, child.y - child.parent.y).times(mySprite.getSpeed()).isOppositeOf(mySprite.getCurrentMove())) {
								// System.out.println("throwing away " + child + " because opposite of " + mySprite.move());
								continue;
							}

							// if we got this far, we haven't already gotten here in less
							// moves, so remove new node from open/closed list, if it's on them
							if (openindex != -1)
								openList.removeElementAt(openindex);

							if (closedindex != -1)
								closedList.removeElementAt(closedindex);

							// put new node on list of nodes to visit
							openList.push(child);
						}
					}

					// done pushing successors; mark this node as CLOSED
					closedList.push(curr);
				}
			}
		}
		Move mov = Move.NEUTRAL;

		if (myMoveStack != null  &&  !myMoveStack.isEmpty()) {
			// pull first move off the list and go there
			mov = (Move)myMoveStack.pop();
		}

		return mov;
	}
}