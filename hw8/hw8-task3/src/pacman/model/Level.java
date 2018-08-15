package pacman.model;

import pacman.sprite.*;
import pacman.utility.Utility;
import java.io.*;
import java.util.*;

/** Represents a level in the game, with its walls, dots, and pellets. */
public class Level {
	/** Size of one level grid square in pixels. */
	public static final int GRID_SIZE  =  8;
	private static final int VISITED   =  0;
	private static final int UNVISITED = -1;
//	private static final int WALL      = -2;

	/** A constant empty level used to reduce object construction. */
	public static final Level EMPTY_LEVEL = new Level("empty", 28, 31);

	private String myBuffer = "";
	private String myName;
	public Sprite[][] myMap;
	private Object myType;
	private int myNumDots;
	private int myNumTotalDots;

	/** Constructs a new level with the given name and grid size.  Not called externally;
	  * instead generateLevel(InputStream) is used.
	  */
	private Level(String name, int gx, int gy) {
		this(name, gx, gy, GameModel.Type.PACMAN);
	}

	private Level(String name, int gx, int gy, GameModel.Type type) {
		myName = name;
		myType = type;
		myMap = new Sprite[gx][gy];
		for (int x = 0;  x < gx;  x++)
		for (int y = 0;  y < gy;  y++)
			myMap[x][y] = Sprite.EMPTY;
	}

	/** Returns a level whose grid data is taken from text in the given input stream. */
	public static Level generateLevel(InputStream is) {
	    return generateLevel(is, GameModel.Type.PACMAN);
	}

	public static Level generateLevel(InputStream is, GameModel.Type type) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringTokenizer st = new StringTokenizer(reader.readLine());
			String name = st.nextToken();
			int numCols = Integer.parseInt(st.nextToken());
			int numRows = Integer.parseInt(st.nextToken());

			Level lev = new Level(name, numCols, numRows, type);
			while (reader.ready())
				lev.myBuffer += reader.readLine() + "\n";

//			lev.regenerate();
			return lev;
		} catch (IOException ioe) {
			throw new RuntimeException("Could not read level: " + ioe);
		}
	}

	/** Refreshes all objects in the given level, restoring its dots and pellets
	  * to their original state.
	  */
	public void regenerate() {
		List<String> lines = new ArrayList<String>();
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(getStream()));
			while (input.ready()) {
				String line = input.readLine();
				if (line != null  &&  !line.equals(""))
					lines.add(line);
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Error regenerating level: " + ioe);
		}

		int w = getGridWidth();
		int h = getGridHeight();

		if (lines.size() == 0) {
			// empty level; wipe it
			for (int y = 0;  y < h;  y++)
    		for (int x = 0;  x < w;  x++)
				myMap[x][y] = Sprite.EMPTY;
			return;
		}

		char curr = '\0';
		int gridx = 0;
		int gridy = 0;
		for (int y = 0;  y < h;  y++) {
			String line = lines.get(y);
			for (int x = 0;  x < w;  x++) {
				curr = line.charAt(x);
				gridx = x * GRID_SIZE;
				gridy = y * GRID_SIZE;

				// non-moving sprites
				if (curr == 'X') {
					// Wall
					if (!(myMap[x][y].type == Sprite.WALL))
						myMap[x][y] = new Wall(gridx, gridy);
				} else if (curr == '=') {
					// Ghost gate
					if (!(myMap[x][y].type == Sprite.GATE))
						myMap[x][y] = new Gate(gridx, gridy);
				} else if (curr == '.') {
					// Dot
					if (!(myMap[x][y].type == Sprite.DOT)) {
						myMap[x][y] = new Dot(gridx + (GRID_SIZE - Dot.DOT_SIZE) / 2,
						                      gridy + (GRID_SIZE - Dot.DOT_SIZE) / 2);
					} else {
						myMap[x][y].setStatus(Sprite.STATUS_NORMAL);
					}
					myNumDots++;
					myNumTotalDots++;
				} else if (curr == 'O') {
					// Power pellet
					if (!(myMap[x][y].type == Sprite.POWERPELLET)) {
						myMap[x][y] = new PowerPellet(gridx, gridy);
					} else {
						myMap[x][y].setStatus(Sprite.STATUS_NORMAL);
					}
					myNumDots++;
					myNumTotalDots++;
				} else if (curr == 'T') {
					if (!(myMap[x][y].type == Sprite.TUNNEL))
						myMap[x][y] = new Tunnel(gridx, gridy);
				} else {
					myMap[x][y] = Sprite.EMPTY;
				}
			}
		}

		// analyze each wall and what it should be shaped like when I
		// draw it (round edges, etc.)
		setWallHexValues();
	}

	/** Returns this level's input stream. */
	public final InputStream getStream() {
		return new ByteArrayInputStream(myBuffer.getBytes());
	}

	/** Returns this level's name. */
	public final String getName() {
		return myName;
	}

	public final Object getType() {
		return myType;
	}

	/** Used to recursively tag the walls in this level to make them look right. */
	private void checkWallType(String[][] typeMap, int x, int y, String tainter) {
		if (!isOnMap(x, y))
			return;

		if (!(myMap[x][y].type == Sprite.WALL))
			return;

		if (typeMap[x][y].equals(tainter))
			// already tainted this square; abort
			return;

		// taint all touching cells
		if (true // isOnBorder(x, y)
			||  isOnMap(x-1, y)  &&  typeMap[x-1][y].equals(tainter)
			||  isOnMap(x+1, y)  &&  typeMap[x+1][y].equals(tainter)
			||  isOnMap(x, y-1)  &&  typeMap[x][y-1].equals(tainter)
			||  isOnMap(x, y+1)  &&  typeMap[x][y+1].equals(tainter))
		{
			typeMap[x][y] = tainter;
			checkWallType(typeMap, x-1, y, tainter);
			checkWallType(typeMap, x+1, y, tainter);
			checkWallType(typeMap, x, y-1, tainter);
			checkWallType(typeMap, x, y+1, tainter);
		}
	}

	/** Returns which wall image should be used.  Nasty. */
	private int getWallHex(int[][] weightMap, int x, int y) {
		/*
		 * the wall regions:
		 *  1 2
		 *  4 8
		 * deciding which image to use is done by adding them and
		 * getting the hex value string.
		 *
		 * for example: if the following regions are painted,
		 * 1
		 * 4 8
		 *
		 * then the hex value is 1 + 4 + 8 = 13, or D in hex.
		 */

		int hexValue = 15;

		// tests to help us know what kind of wall this is
		boolean pathToLeft  = isOnMap(x-1, y)    &&  weightMap[x-1][y]     != UNVISITED;
		boolean pathToRight = isOnMap(x+1, y)    &&  weightMap[x+1][y]     != UNVISITED;
		boolean pathAbove   = isOnMap(x, y-1)    &&  weightMap[x][y-1]     != UNVISITED;
		boolean pathBelow   = isOnMap(x, y+1)    &&  weightMap[x][y+1]     != UNVISITED;
		boolean pathToUL    = isOnMap(x-1, y-1)  &&  weightMap[x-1][y-1]   != UNVISITED;
		boolean pathToUR    = isOnMap(x+1, y-1)  &&  weightMap[x+1][y-1]   != UNVISITED;
		boolean pathToLL    = isOnMap(x-1, y+1)  &&  weightMap[x-1][y+1]   != UNVISITED;
		boolean pathToLR    = isOnMap(x+1, y+1)  &&  weightMap[x+1][y+1]   != UNVISITED;

		boolean onLeftBorder    = x == 0;
		boolean onRightBorder   = x == getGridWidth() - 1;
		boolean onTopBorder     = y == 0;
		boolean onBottomBorder  = y == getGridHeight() - 1;
		// boolean onBorder        = onLeftBorder  ||  onRightBorder  ||  onTopBorder  ||  onBottomBorder;

		// only trim L/R if there is a
		if (!onLeftBorder  &&  pathToLeft) {
			// movable path to left; cut the left side
			hexValue &= ~1;
			hexValue &= ~4;
		}

		if (!onRightBorder  &&  pathToRight) {
			// path to right; cut right
			hexValue &= ~2;
			hexValue &= ~8;
		}

		if (!onTopBorder  &&  pathAbove) {
			// path above; cut top
			hexValue &= ~1;
			hexValue &= ~2;
		}

		if (!onBottomBorder  &&  pathBelow) {
			// path below; cut bottom
			hexValue &= ~4;
			hexValue &= ~8;
		}


		if (hexValue == 15) {
			// still nothing trimmed; try diagonals to round edges
			if (pathToUL)  hexValue &= ~1;
			if (pathToUR)  hexValue &= ~2;
			if (pathToLL)  hexValue &= ~4;
			if (pathToLR)  hexValue &= ~8;
		}

		if (!(0 <= hexValue  &&  hexValue <= 15))
			throw new RuntimeException("invalid hex value; this should not happen");
		return hexValue;
	}

	/** Maps the hex types of all walls in this level. */
	private int[][] mapOut() {
		int gx = 0;
		int gy = 0;
		while (!(myMap[gx][gy].type == Sprite.EMPTYSQUARE)) {
			gx++;
			gy++;
		}
		int gridWidth = getGridWidth();
		int gridHeight = getGridHeight();
		int[][] weightMap = new int[gridWidth][gridHeight];

		for (int y = 0;  y < gridHeight;  y++)
		for (int x = 0 ; x < gridWidth;  x++)
			weightMap[x][y] = UNVISITED;

		weigh(weightMap, gx, gy);
		return weightMap;
	}

	/** Tags the given square as visitable or not visitable. */
	private void weigh(int[][] weightMap, int x, int y) {
		// exit if this isn't a valid square
		if (!isOnMap(x, y)  ||  myMap[x][y] instanceof ObstacleSprite)
			return;

		else if (weightMap[x][y] == UNVISITED) {
			weightMap[x][y] = VISITED;
			weigh(weightMap, x + 1, y);
			weigh(weightMap, x - 1, y);
			weigh(weightMap, x,     y + 1);
			weigh(weightMap, x,     y - 1);
		}
	}

	/** Sets all wall hex values so that the views can draw the walls accordingly. */
	private void setWallHexValues() {
		// steal the smart strategy's algorithm so that we can use it to
		// map out every visitable square--so we know better how to draw walls
		int[][] weightMap = mapOut();

		// strat.printWeightMap(weightMap);

		// figure out which cells are walls, for starters
		String[][] typeMap = new String[weightMap.length][weightMap[0].length];
		int gridWidth = getGridWidth();
		int gridHeight = getGridHeight();
		for (int x = 0;  x < gridWidth;  x++)
		for (int y = 0;  y < gridHeight;  y++)
			if (myMap[x][y].type == Sprite.WALL)
				typeMap[x][y] = "wall";
			else if (myMap[x][y].type == Sprite.GATE)
				typeMap[x][y] = "gate";
			else
				typeMap[x][y] = " ";

		// tag the border walls
		for (int x = 0;  x < gridWidth;  x++)
		for (int y = 0;  y < gridHeight;  y++)
			if (isOnBorder(x, y)  &&  myMap[x][y].type == Sprite.WALL  &&  !typeMap[x][y].equals("border"))
				checkWallType(typeMap, x, y, "border");

		// find walls around gates and taint them
		for (int x = 0;  x < gridWidth;  x++)
		for (int y = 0;  y < gridHeight;  y++)
			if (myMap[x][y].type == Sprite.GATE) {
				checkWallType(typeMap, x-1, y, "cage");
				checkWallType(typeMap, x+1, y, "cage");
				checkWallType(typeMap, x, y-1, "cage");
				checkWallType(typeMap, x, y+1, "cage");
			}

		// now append each square's hex type
		for (int x = 0;  x < gridWidth;  x++)
		for (int y = 0;  y < gridHeight;  y++) {
			if (myMap[x][y] instanceof ObstacleSprite) {
				// figure out which wall image to draw
				ObstacleSprite obst = (ObstacleSprite)myMap[x][y];
				obst.setHex(getWallHex(weightMap, x, y));
				typeMap[x][y] += Integer.toHexString(obst.getHex());
				obst.setImageName(typeMap[x][y]);
			}
		}
	}

	/** Returns this level's grid of squares. */
	public final Sprite[][] getGrid() {
		return myMap;
	}

	/** Returns the Sprite at the given grid cell. */
	public final Sprite getGridCell(int x, int y) {
		if (!isOnMap(x, y))
			throw new IllegalArgumentException("invalid x, y");
		return myMap[x][y];
	}

	/** Sets the given grid cell to be the given Sprite. */
	public final void setGridCell(int x, int y, Sprite value) {
		if (!(0 <= x  &&  x <= getGridWidth())  ||  !(0 <= y  &&  y <= getGridHeight()))
			throw new IllegalArgumentException("invalid x, y");
		myMap[x][y] = value;
	}

	/** Returns the height of the grid. */
	public final int getGridHeight()   { return myMap == null ? 0 : myMap[0].length; }

	/** Returns the width of the grid. */
	public final int getGridWidth()    { return myMap == null ? 0 : myMap.length; }

	/** Returns the height in pixels of the board. */
	public final int getHeight()       { return getGridHeight() * GRID_SIZE; }

	/** Returns the width in pixels of the board. */
	public final int getWidth()        { return getGridWidth()  * GRID_SIZE; }

	/** Returns whether or not this game's board is non-existant. */
	public final boolean isEmpty() {
		return getGridWidth() == 0  &&  getGridHeight() == 0;
	}

	/** Returns whether the given x/y pair lies on the grid. */
	public final boolean isOnMap(int gx, int gy) {
		return 0 <= gx  &&  gx < myMap.length  &&  0 <= gy  &&  gy < myMap[0].length;
	}

	/** Returns whether the given x/y pixel is on the screen. */
	public final boolean isOnScreen(int gx, int gy) {
		return 0 <= gx  &&  gx < getWidth()  &&  0 <= gy  &&  gy < getHeight();
	}

	/** Returns whether the given x/y pixel is on the edge of the screen. */
	public final boolean isOnBorder(int gx, int gy) {
		return (0 == gx  ||  gx == getGridWidth() - 1)
			&&  (0 == gy  ||  gy == getGridHeight() - 1);
	}

	/** Returns whether or not half of the board's dots have been eaten. */
	public final boolean halfOfDotsAreEaten() {
		return 2 * myNumDots == myNumTotalDots / 2 * 2;
	}

	/** Returns whether or not three quarters of the board's dots have been eaten. */
	public final boolean threeQuartersOfDotsAreEaten() {
		return 4 * myNumDots == myNumTotalDots / 4 * 4;
	}

	/** Returns how many walls neighbor the given grid square. */
	public final int numNeighbors(int gx, int gy) {
		int num = -1;

		for (int y = gy - 1;  y <= gy + 1;  y++)
			for (int x = gx - 1;  x <= gx + 1;  x++)
				if (isOnMap(x, y)  &&  getGridCell(x, y).type == Sprite.WALL)
					num++;

		return num;
	}

	/** Prints the grid on the screen. */
	public void printGrid() {
		if (myMap == null)  { System.out.println("null");  return;  }

		int gridWidth = getGridWidth();
		int gridHeight = getGridHeight();
		System.out.println(gridWidth + "\t" + gridHeight);
		for (int y = 0;  y < gridHeight;  y++) {
			for (int x = 0;  x < gridWidth;  x++)
				System.out.print(myMap[x][y]);
			System.out.println();
		}
	}

	/** Prints this level's map on the screen. */
	public void printMap(String[][] weightMap) {
		printMap(weightMap, 2);
	}

	/** Prints this level's map on the screen, justified to the given width. */
	public void printMap(String[][] weightMap, int width) {
		for (int y = 0;  y < weightMap[0].length;  y++) {
			for (int x = 0 ; x < weightMap.length;  x++)
				System.out.print(Utility.padStringR(weightMap[x][y], width));
			System.out.println();
		}
	}



	/** Returns whether or not the given sprite can move in his current direction. */
	public final boolean canMove(MovingSprite spr) { return canMove(spr, spr.getCurrentMove()); }

	/** Returns whether or not the given sprite can make the given move. */
	public final boolean canMove(MovingSprite spr, Move mov) {
		// try moving onto the square, and see if I collide with anything
		Sprite.DUMMY.setOther(spr);
		Sprite.DUMMY.go(mov);
		Sprite.DUMMY.wrap(getWidth(), getHeight());
		return !collidesWithWall(Sprite.DUMMY);
	}

	/** Returns whether or not the given sprite may move in his current direction,
	  * or in the one given.  Useful for deciding whether or not to make pac-man
	  * move his lips.
	  */
	public final boolean canMoveCurrentWayOrThisWay(MovingSprite spr, Move mov) {
		return canMove(spr)  ||  canMove(spr, mov);
	}

	/** Returns whether the given Sprite may move down. */
	public final boolean canMoveDown(MovingSprite spr)  { return canMove(spr, Move.DOWN.times(spr.getSpeed())); }

	/** Returns whether the given Sprite may move left. */
	public final boolean canMoveLeft(MovingSprite spr)  { return canMove(spr, Move.LEFT.times(spr.getSpeed())); }

	/** Returns whether the given Sprite may move right. */
	public final boolean canMoveRight(MovingSprite spr) { return canMove(spr, Move.RIGHT.times(spr.getSpeed())); }

	/** Returns whether the given Sprite may move up. */
	public final boolean canMoveUp(MovingSprite spr)    { return canMove(spr, Move.UP.times(spr.getSpeed())); }

	/** Returns whether or not the given sprite can see the given other sprite. */
	public final boolean canSee(Sprite spr, Sprite spr2) {
		return canSee(spr, spr2.getGridX(), spr2.getGridY());
	}

	/** Returns true if the given Sprite can "see" the grid square at (x, y)--
	  * that is, whether or not the sprite has a clear path to that square.
	  */
	public boolean canSee(Sprite spr, int x, int y) {
		// invalid argument(s) case
		if (spr == null  ||  !isOnMap(x, y))
			return false;

		int sprx = spr.getGridX();
		int spry = spr.getGridY();

		if (!isOnMap(sprx, spry)  ||  !(x == sprx  ||  y == spry))
			// couldn't possibly see it; not on straight-line
			return false;

		if (x == sprx) {
			// above/below me; check if I can see it
			int increment = (y < spry)  ?  -1  :  1;
			while (spry != y) {
				if (getGridCell(sprx, spry) instanceof ObstacleSprite)
					return false;
				spry += increment;
			}
		}
		else if (y == spry) {
			// left/right from me; check if I can see it
			int increment = (x < sprx)  ?  -1  :  1;
			while (sprx != x) {
				if (getGridCell(sprx, spry) instanceof ObstacleSprite)
					return false;
				sprx += increment;
			}
		}

		return true;
	}

	/** Returns true if the given moving sprite collides with a wall. */
	public final boolean collidesWithWall(MovingSprite spr) {
		return collision(spr) != null;
	}

	/** Returns the sprite, if any, that the given moving sprite has collided with. */
	private Sprite collision(MovingSprite spr) {
		MovingSprite sprCollide = spr;

		// since sprite may be between grid squares, inspect those
		// immediately around it for accurate collision detection
		if (spr.type == Sprite.DUMMYSPRITE) {
			spr = ((DummySprite)spr).getOther();
//			if (MOVE_DEBUG)  System.err.println("replacing dummy with a " + spr.getClass().getName());
		}

		int gridx = sprCollide.getGridX();
		int gridy = sprCollide.getGridY();
		boolean isValidSprite = false;
		Sprite collider = null;
		Ghost gh = null;

		for (int x = gridx;  x <= gridx + 1;  x++)  // *** REMOVED gridx/y - 1
		for (int y = gridy;  y <= gridy + 1;  y++) {
			// eliminate non-collision cases
			if (!isOnMap(x, y))
				continue;

			collider = getGridCell(x, y);
			isValidSprite = collider != null  &&  collider != Sprite.EMPTY
					&&  (collider.isVisible  ||  collider.type == Sprite.POWERPELLET);

			if (!isValidSprite)
				continue;


			if (spr.type == Sprite.GHOST) {
				gh = (Ghost)spr;

				if (collider.type == Sprite.WALL  //    ||  gh.getStrategy() == null
						||  (!(gh.isEaten()  ||  gh.isInCage())  &&  collider instanceof ObstacleSprite))
					// ghost is stopped by walls, and non-(eaten/caged) ghosts are also stopped by gates
					if (sprCollide.collidesWith(collider)) {
						return collider;
					}
			} else {
				// pac-man is stopped by any obstacle sprite (wall/gate)
				if (collider instanceof ObstacleSprite  &&  sprCollide.collidesWith(collider)) {
					return collider;
				}
			}
		}

//		if (MOVE_DEBUG)  System.out.println("collidesWithWall: " + spr.getClass().getName() + " returning " + result);
		return null;
	}


	/** Sees what dots this Sprite has run into, notifying the given model and its listeners accordingly. */
	public void checkDotsEaten(GameModel model, PacMan pac) {
		if (pac == null)
			return;

		for (int x = pac.getGridX() - 1; x <= pac.getGridX() + 1;  x++)
		for (int y = pac.getGridY() - 1;  y <= pac.getGridY() + 1;  y++) {
			// collision detection on dots/pellets (if pac-man ate anything)

			// eliminate non-collision cases
			boolean isValidSprite = isOnMap(x, y)
				&&  myMap[x][y] != null
				&&  myMap[x][y] != Sprite.EMPTY
				&&  myMap[x][y] instanceof EdibleSprite
				&&  (myMap[x][y].isVisible  ||  myMap[x][y].type == Sprite.POWERPELLET)
				&&  pac.collidesWith(myMap[x][y]);

			if (!isValidSprite)
				continue;

			EdibleSprite edible = (EdibleSprite)myMap[x][y];
			if (!edible.isEaten()) {
				if (model.isInProgress())
					model.addToScore(edible.getScore());
				edible.setStatus(Sprite.STATUS_EATEN);

				if (edible.type == Sprite.DOT  ||  edible.type == Sprite.POWERPELLET) {
					myNumDots--;

					pac.chompDot();  // notifies pac-man that he is eating a dot

					if (threeQuartersOfDotsAreEaten())
						model.notifyListeners(null, GameModel.Event.THREE_QUARTERS_DOTS_EATEN);
					if (halfOfDotsAreEaten())
						model.notifyListeners(null, GameModel.Event.HALF_DOTS_EATEN);

					if (edible.type == Sprite.POWERPELLET) {
						PowerPellet pellet = (PowerPellet)edible;
						model.chompPellet(pac, pellet);
					}

					if (myNumDots == 0) {
						model.notifyListeners(null, GameModel.Event.LEVEL_CLEARED);
					}
				}
				model.notifyListeners(edible, edible.type == Sprite.POWERPELLET  ?  GameModel.Event.POWER_PELLET_EATEN  :  GameModel.Event.DOT_EATEN);
			}
		}
	}

	/** Returns true if all of this level's dots are eaten. */
	public final boolean isCleared() {
		return myNumDots == 0;
	}

	/** Returns true if the given sprite is inside a tunnel. */
	public boolean isInTunnel(MovingSprite spr) {
		int gridx = spr.getGridX();
		int gridy = spr.getGridY();

		for (int x = gridx - 1;  x <= gridx + 1;  x++)
		for (int y = gridy - 1;  y <= gridy + 1;  y++) {
			// eliminate non-collision cases
			if (!isOnMap(x, y))
				continue;

			Sprite collider = myMap[x][y];
			boolean isValidSprite = collider != null  &&  collider != Sprite.EMPTY;

			if (!isValidSprite)
				continue;

			if (collider.type == Sprite.TUNNEL  &&  spr.collidesWith(collider))
				return true;
		}

		return false;
	}

	/** Makes all power pellets on this level's map blink on or off. */
	public void blinkPellets() {
		int gridWidth = getGridWidth();
		int gridHeight = getGridHeight();

		for (int x = 0;  x < gridWidth;  x++)
		for (int y = 0;  y < gridHeight;  y++)
			if (myMap[x][y].type == Sprite.POWERPELLET  &&  !((PowerPellet)myMap[x][y]).isEaten())
				myMap[x][y].setVisible(!myMap[x][y].isVisible);
	}

	/** Returns a string representation of this level. */
	public String toString() {
		String result = myName + "[gx=" + getGridWidth() + "][gy=" + getGridHeight() + "]\n";

		int gridWidth = getGridWidth();
		int gridHeight = getGridHeight();
		for (int y = 0;  y < gridHeight;  y++) {
			for (int x = 0;  x < gridWidth;  x++) {
				result += myMap[x][y];
			}
			result += '\n';
		}

		return result;
	}
}