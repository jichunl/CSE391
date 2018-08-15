package pacman.strategy;

import java.util.*;

import pacman.model.*;
import pacman.sprite.*;


/** A simple stupid strategy used by ghosts that are still locked in their cage.
  * It just makes the ghost move up and down until it hits a wall, then
  * exit the cage once their cage counters expire.
  */
public class CageStrategy extends Strategy {
	private static int ourNumInstances = 0;
	private boolean myIsGoingUp;
	private List<Move> myMoveList = null;

	/** Constructs a new strategy to move the given sprite. */
	public CageStrategy(MovingSprite gh) {
		super(gh);
		ourNumInstances++;
		myIsGoingUp = (ourNumInstances % 2 == 1);
	}

	/** Called when this strategy is garbage collected. */
	protected void finalize() throws Throwable {
		ourNumInstances--;
	}

	/** Returns this strategy's next move toward the given target. */
	public Move getMove(Level level, MovingSprite target) {
		if (mySprite.getStatus() == Sprite.STATUS_CAGED) {
			if (myMoveList != null)
				myMoveList.clear();

			// just go up and down in the cage
			if (myIsGoingUp) {
				if (canMoveUp(level, mySprite))
					return Move.UP.times(mySprite.getSpeed());
				else {
					myIsGoingUp = !myIsGoingUp;
					return Move.DOWN.times(mySprite.getSpeed());
				}
			} else {
				if (canMoveDown(level, mySprite))
					return Move.DOWN.times(mySprite.getSpeed());
				else {
					myIsGoingUp = !myIsGoingUp;
					return Move.UP.times(mySprite.getSpeed());
				}
			}
		} else if (mySprite.getStatus() == Sprite.STATUS_LEAVING_CAGE) {
			// ghost wants to escape from the cage;
			// find the nearest square out of the cage and go there

			if (myMoveList == null)
				myMoveList = getMoveList(level);

			if (!myMoveList.isEmpty()) {
				Move mov = myMoveList.remove(0);
				return mov;
			} else {
				// move list is out of moves, so we must be out of the cage!  yay
				myMoveList = null;
				if (mySprite.type == Sprite.GHOST)
					((Ghost)mySprite).killCounter();
				return Move.NEUTRAL;
			}
		} else {
			// shouldn't even be using cage strategy!
			mySprite.setStatus(Sprite.STATUS_NORMAL);
			return Move.NEUTRAL;
		}
	}

	/** Returns whether or not the given sprite can move up. */
	public boolean canMoveUp(Level level, MovingSprite gh) {
		return level.canMoveUp(gh)
				&&  level.isOnMap(gh.getGridX(), gh.getGridY() - 1)
				&&  !(level.myMap[gh.getGridX()][gh.getGridY() - 1] instanceof ObstacleSprite);
	}

	/** Returns whether or not the given sprite can move down. */
	public boolean canMoveDown(Level level, MovingSprite gh) {
		return level.canMoveUp(gh)
				&&  level.isOnMap(gh.getGridX(), gh.getGridY() + 1)
				&&  !(level.myMap[gh.getGridX()][gh.getGridY() + 1] instanceof ObstacleSprite);
	}

	/** Constants used for weighing the map. */
	private static final char UNTRIED = '?',
			TRIED   = 'T',
//			IN_CAGE = 'C',
//			ON_GATE = 'G',
			OUT     = 'O';

	/** Returns a path from this ghost to the nearest square that is not in the cage. */
	public List<Move> getMoveList(Level level) {
		// all squares will be initialized to UNTRIED, 0, \0
		char[][] map = new char[level.getGridWidth()][level.getGridHeight()];
		for (int r = 0;  r < map.length;  r++)
			for (int c = 0;  c < map[0].length;  c++)
				map[r][c] = (level.myMap[r][c].type == Sprite.WALL)  ?  TRIED  :  UNTRIED;

		int gx = mySprite.getGridX();
		int gy = mySprite.getGridY();

		boolean gotOut = check(level, map, gx, gy, false);
		// printMap(map);

		List<Move> moveList = new ArrayList<Move>();

		if (gotOut) {
			// move onto the nearest grid square
			moveList = getMoveList(mySprite, mySprite.getX(), mySprite.getY(),
				mySprite.getGridX() * Level.GRID_SIZE, mySprite.getGridY() * Level.GRID_SIZE);

			// retrace our path back out
			while (true) {
				map[gx][gy] = TRIED;
				if (level.isOnMap(gx, gy - 1)  &&  map[gx][gy - 1] == OUT) {
					// add moves to go up one grid square
					gy--;
					moveSquare(moveList, Move.UP);
				}
				else if (level.isOnMap(gx + 1, gy)  &&  map[gx + 1][gy] == OUT) {
					gx++;
					moveSquare(moveList, Move.RIGHT);
				}
				else if (level.isOnMap(gx - 1, gy)  &&  map[gx - 1][gy] == OUT) {
					gx--;
					moveSquare(moveList, Move.LEFT);
				}
				else if (level.isOnMap(gx, gy + 1)  &&  map[gx][gy + 1] == OUT) {
					gy++;
					moveSquare(moveList, Move.DOWN);
				}
				else
					break;
			}
		}

		return moveList;
	}

	/** Adds enough moves to move one square in the given move's direction to
	  * the given vector.
	  */
	private void moveSquare(List<Move> v, Move mov) {
		for (int i = 0;  i < Level.GRID_SIZE / mySprite.getSpeed();  i++)
			v.add(mov.times(mySprite.getSpeed()));
	}

//	/** Prints this strategy's weight map. */
//	private void printMap(char[][] map) {
//		for (int c = 0;  c < map.length;  c++) {
//			for (int r = 0;  r < map.length;  r++)
//				System.out.print(" " + map[r][c]);
//			System.out.println();
//		}
//	}

	/** Checks the given grid square on the given map to find a path out of the cage. */
	private boolean check(Level level, char[][] map, int gx, int gy, boolean money) {
		if (!level.isOnMap(gx, gy)  ||  map[gx][gy] == TRIED)
			return false;

		Sprite cell = level.myMap[gx][gy];
		if (cell.type == Sprite.EMPTYSQUARE  &&  money) {
			map[gx][gy] = OUT;
			return true;
		}

		boolean gotOut = false;
		map[gx][gy] = TRIED;

		if (cell.type == Sprite.GATE  &&  !(cell.type == Sprite.WALL)) {
			// then this is a way out!  mark it
			gotOut = check(level, map, gx, gy - 1, true)
					||  check(level, map, gx + 1, gy, true)
					||  check(level, map, gx - 1, gy, true)
					||  check(level, map, gx, gy + 1, true);
		}
		else {
			gotOut = check(level, map, gx, gy - 1, money)
					||  check(level, map, gx + 1, gy, money)
					||  check(level, map, gx - 1, gy, money)
					||  check(level, map, gx, gy + 1, money);
		}

		if (gotOut)  map[gx][gy] = OUT;

		return gotOut;
	}
}