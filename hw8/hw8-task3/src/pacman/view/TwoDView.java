package pacman.view;

import pacman.model.*;
import pacman.sprite.*;
import pacman.view.View;
import pacman.utility.*;

import java.awt.*;
import java.awt.event.*;
import java.util.List;


/** This class represents the Panel for the 2D view of the game, which is
  * a 2D representation vaguely similar to the arcade Pac-Man look.
  */
public class TwoDView extends View {
    private static final long serialVersionUID = 0;
    
	protected static final int PREFERRED_SCALE_FACTOR = 1;
	protected static final boolean SHOULD_MAINTAIN_ASPECT_RATIO = true;
	protected double myXScaleFactor = 1.0;
	protected double myYScaleFactor = 1.0;
	private int myMouthNum = 1;
	private int myGhostLegsNum = 1;
	private Font myFont = new Font("SansSerif", Font.BOLD, 12);

	/** Constructs a new View to view the given GameModel. */
	public TwoDView(GameModel gm, Component parent, ResourceFetcher fetch) {
		super(gm, parent, fetch);
		this.setBackground(PacManColors.BLACK);
	}

	/** Constructs a new 2D view of the given game type. */
	public TwoDView(GameModel gm, Component parent, ResourceFetcher fetch, Object type) {
		super(gm, parent, fetch, type);
		this.setBackground(PacManColors.BLACK);
	}


	/** Updates this view's image for drawing the next frame of animation. */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setFont(myFont);

		Level level = myModel.getCurrentLevel();
		if (level == null)
			return;

//		Font oldFont = g.getFont();
		double fontSpacingX = 1.0 * getSize().width  /  level.getGridWidth();
		double fontSpacingY = 1.0 * getSize().height / (level.getGridHeight() + 5);
		int fontSize = (int)Math.min(fontSpacingX, fontSpacingY);
		if (fontSize != myFont.getSize())
			setFont(fontSize);

		if (myBackground == null
		||  myBackground.getWidth(this)  != getSize().width
		||  myBackground.getHeight(this) != getSize().height)
			updateBackground();

		// draw static walls and gates and such
		if (myBackground != null)
			g.drawImage(myBackground, 0, 0, this);

		// write current score(s)/high score on screen
		g.setColor(PacManColors.WHITE);
		if (myModel.shouldShowPlayerNumber(1))
			drawWord(g, "1UP", "", 3, 0);
		if (myModel.getNumPlayers() >= 1)
			drawWord(g, Utility.padStringR("" + myModel.getScore(1), 6), "",  1, 1);

		if (myModel.shouldShowPlayerNumber(2))
			drawWord(g, "2UP", "", 22, 0);
		if (myModel.getNumPlayers() >= 2)
			drawWord(g, Utility.padStringR("" + myModel.getScore(2), 6), "", 20, 1);

		drawWord(g, "HIGH SCORE", "", 9, 0);
		drawWord(g, Utility.padStringR("" + myModel.getHighScore(), 6), "", 11, 1);

		// move the 'origin' so that the coordinates will line up right
		g.translate(0, (int)(2.5 * myYScaleFactor * Level.GRID_SIZE));

		int gridWidth  = level.getGridWidth();
		int gridHeight = level.getGridHeight();
		Sprite spr = null;
		int sprX      = 0;
		int sprY      = 0;
		int sprWidth  = 0;
		int sprHeight = 0;
		int frX = 0;
		int frY = 0;

		// draw the level map (walls/dots/pellets/fruit)
		Sprite[][] map = level.getGrid();
		for (int y = 0;  y < gridHeight;  y++)
		for (int x = 0;  x < gridWidth;  x++) {
			spr = map[x][y];
			if (!spr.isVisible)
				continue;

			sprX      = (int)(myXScaleFactor * spr.rect.x);
			sprY      = (int)(myYScaleFactor * spr.rect.y);
			sprWidth  = (int)(myXScaleFactor * spr.rect.width);
			sprHeight = (int)(myYScaleFactor * spr.rect.height);

			if (spr.type == Sprite.DOT) {
				g.setColor(PacManColors.FLESH);
				g.fillRect(sprX, sprY, sprWidth, sprHeight);
			}
			else if (spr.type == Sprite.POWERPELLET) {
				g.setColor(PacManColors.FLESH);
				g.fillOval(sprX, sprY, sprWidth, sprHeight);
			}
			else if (spr.type == Sprite.LETTER) {
				g.setColor(spr.getColor());
				drawWord(g, "" + spr, "", spr.getGridX(), spr.getGridY());
			}
		}

		Fruit fr = myModel.getFruit();
		if (myModel.isInProgress()) {
			// draw fruit on bottom
			if (fr != null) {
				frX = (int)(myXScaleFactor * (level.getWidth() - Level.GRID_SIZE * 4 + 2));
				frY = (int)(myYScaleFactor * (level.getHeight() + 2));
				drawFruit(g, fr, frX, frY);
			}

			// draw lives on bottom
			for (int i = 0;  i < myModel.getNumLives();  i++) {
				sprX      = (int)(myXScaleFactor * (Level.GRID_SIZE * (2*i+2)));
				sprY      = (int)(myYScaleFactor * (level.getHeight() + 2));
				sprWidth  = (int)(myXScaleFactor * 2*Level.GRID_SIZE);
				sprHeight = (int)(myYScaleFactor * 2*Level.GRID_SIZE);

				// draw a Pac-Man facing left for lives
				g.setColor(PacManColors.YELLOW);
				g.fillArc(sprX, sprY, sprWidth, sprHeight, 225, 270);

			}

			// write "game over" on screen while demo is playing
			if (myModel.justStarted()) {
				// draw player's prompt but no sprites
				g.setColor(PacManColors.WHITE);
				drawWord(g, "PLAYER " + Utility.getWordForNumber(myModel.getPlayerNumber()).toUpperCase(), "", 9, 11);
			}


			if (myModel.getPacMan() != null  &&  myModel.getPacMan().wasJustRevived()) {
				g.setColor(PacManColors.YELLOW);
				drawWord(g, "READY!", "", 11, 17);
			}
		} else {
		    // draw credits on bottom
		    g.setColor(PacManColors.WHITE);
		    drawWord(g, "CREDIT " + Utility.padStringR("" + myModel.getNumCredits(), 2), "", 2, level.getGridHeight() + 1);
		}

		if (!myModel.justStarted()) {
			// draw moving sprites (Pac-Man/ghosts)
			List<MovingSprite> sprites = myModel.getMovingSprites();
			if (sprites == null)  return;

			// variables used for ghost
			int jaggedX       = 0;
			int jaggedY1      = 0;
			int jaggedY2      = 0;
			int jaggedY3      = 0;
			int eyeWidth      = 0;
			int eyeHeight     = 0;
			int eye1X         = 0;
			int eye2X         = 0;
			int eyeY          = 0;
			int eyeBallX      = 0;
			int eyeBallY      = 0;
			int eyeBallWidth  = 0;
			int eyeBallHeight = 0;
			int[] xPoints       = null;
			int[] yPoints       = null;
			int[] jaggedXpoints = null;
			int[] jaggedYpoints = null;

			if (myModel.isDoingMovement()) {
				// change images each half second
				myGhostLegsNum = (int)(myModel.getUpdateCounter() / (GameModel.UPDATES_PER_SECOND / 4) % 2 + 1);
			}

			// draw each moving sprite (pac-man/ghost/fruit)
			for (MovingSprite mspr : sprites) {
				if (!mspr.isVisible)
					continue;

				// scale by 2 so sprites look bigger
				sprX      = (int)(myXScaleFactor * (mspr.rect.x - mspr.rect.width /2));
				sprY      = (int)(myYScaleFactor * (mspr.rect.y - mspr.rect.height/2));
				sprWidth  = (int)(myXScaleFactor * 2*mspr.rect.width);
				sprHeight = (int)(myYScaleFactor * 2*mspr.rect.height);

				// Pac-Man
				if (mspr.type == Sprite.PACMAN) {
					if (!myModel.shouldDrawPacMan())
						continue;

					PacMan pac = (PacMan)mspr;

					// figure out what arc of a circle to draw based on facing dir
					int angleExtent = 360;
					int startAngle  = 0;

					if (pac.isAlive()  ||  pac.wasJustKilled()) {
						// figure out which way he's facing
						startAngle = (mspr.isMovingUp())  ?  90
								:  (mspr.isMovingDown())  ?  270
								:  (mspr.isMovingLeft())  ?  180  :  0;

						// figure out how much his mouth is open
						if (myModel.shouldMovePacManMouth(pac)) {
							int framesPerSwitch = (int)Math.max(GameModel.UPDATES_PER_SECOND / 21, 1);

							myMouthNum = (int)((myModel.getUpdateCounter() / framesPerSwitch) % 4);
							if (myMouthNum == 3)
								myMouthNum = 1;
						}
						startAngle  += 30 * myMouthNum;
						angleExtent -= 2*30 * myMouthNum;
					}
					else {
						// he's dying

						// in 1 +  8 * (1/6) seconds he shrivels 180deg, or in 4*fps/3 updates (blech)
						int angle = Math.min(180, (pac.getNumUpdatesSinceKilled()
								- GameModel.UPDATES_PER_SECOND) * 180*3/4/GameModel.UPDATES_PER_SECOND); // / framesPerSwitch + 1;

						startAngle  = 90 + angle;
						angleExtent = 360 - 2*angle;
					}

					// now actually draw him
					g.setColor(PacManColors.YELLOW);
					g.fillArc(sprX, sprY, sprWidth, sprHeight, startAngle, angleExtent);
				} else if (mspr.type == Sprite.GHOST) {
					// let's precalculate various significant locations
					jaggedX  = sprX;
					jaggedY1 = sprY + sprHeight / 2;
					jaggedY2 = sprY + 3 * sprHeight / 4;
					jaggedY3 = sprY + sprHeight;
					eyeWidth  = 3*sprWidth/8;
					eyeHeight = 3*sprHeight/8;
					eye1X     = sprX + sprWidth/2 - eyeWidth;
					eye2X     = sprX + sprWidth/2;
					eyeY      = sprY + sprHeight/2 - eyeHeight;
					eyeBallX  = sprX + 7*sprWidth/32;   // 1/8 + 3/16 - 3/32
					eyeBallY  = sprY + 7*sprHeight/32;
					eyeBallWidth  = eyeWidth/2;
					eyeBallHeight = eyeHeight/2;

					Ghost gh = (Ghost)mspr;

					// draw just-eaten ghost as score he was worth
					if (gh.wasJustEaten()) {
						g.setColor(PacManColors.CYAN);
						g.drawString("" + (gh.getScore() * (int)Math.pow(2, myModel.getGhostScorePower() - 1)),
								sprX, sprY + g.getFont().getSize());
					}

					else {
						if (!gh.isScared()) {
							// shift eyes so ghost looks like he's looking in the direction he's moving
							// (unless he's blue from a pellet)
							eyeBallX += gh.isMovingLeft() ? -3*sprWidth/32  : gh.isMovingRight() ? 3*sprWidth/32  : 0;
							eyeBallY += gh.isMovingUp()   ? -3*sprHeight/32 : gh.isMovingDown()  ? 3*sprHeight/32 : 0;
						}

						if (!gh.isEaten()) {
							// body
							jaggedXpoints = new int[] {jaggedX,  jaggedX,  jaggedX + sprWidth / 6, jaggedX + sprWidth / 3, jaggedX + sprWidth / 2, jaggedX + 2 * sprWidth / 3, jaggedX + 5 * sprWidth / 6, jaggedX + sprWidth, jaggedX + sprWidth};
							jaggedYpoints = new int[] {jaggedY1, jaggedY3, jaggedY2,               jaggedY3,               jaggedY2,               jaggedY3,                   jaggedY2,                   jaggedY3,           jaggedY1};

							if (gh.shouldAnimate()  &&  myGhostLegsNum != 1)
								jaggedYpoints = new int[] {jaggedY1, jaggedY2, jaggedY3,               jaggedY2,               jaggedY3,               jaggedY2,                   jaggedY3,                   jaggedY2,           jaggedY1};

							g.setColor(gh.getColor());
							g.fillArc(sprX, sprY, sprWidth, sprHeight, 0, 180);
							g.fillPolygon(jaggedXpoints, jaggedYpoints, jaggedXpoints.length);
						}


						if (gh.isScared()) {
							// jagged mouth
							jaggedY1 = sprY + 5 * sprHeight / 8;
							xPoints = new int[] {jaggedX + sprWidth / 8, jaggedX + sprWidth / 4, jaggedX + 3* sprWidth / 8, jaggedX + sprWidth / 2, jaggedX + 5 * sprWidth / 8, jaggedX + 3 * sprWidth / 4, jaggedX + 7 * sprWidth / 8};
							yPoints = new int[] {jaggedY2,               jaggedY1,               jaggedY2,                  jaggedY1,               jaggedY2,                   jaggedY1,                   jaggedY2};
							g.setColor(PacManColors.FLESH);
							g.drawPolyline(xPoints, yPoints, xPoints.length);

							// square eyeballs
							g.fillRect(eyeBallX,            eyeBallY, eyeBallWidth, eyeBallHeight);
							g.fillRect(eyeBallX + eyeWidth, eyeBallY, eyeBallWidth, eyeBallHeight);
						}
						else {
							// eyes
							g.setColor(PacManColors.WHITE);
							g.fillOval(eye1X, eyeY, eyeWidth, eyeHeight);
							g.fillOval(eye2X, eyeY, eyeWidth, eyeHeight);

							// eyeballs
							g.setColor(PacManColors.BLUE);
							g.fillOval(eyeBallX,            eyeBallY, eyeBallWidth, eyeBallHeight);
							g.fillOval(eyeBallX + eyeWidth, eyeBallY, eyeBallWidth, eyeBallHeight);
						}
					}
				} else if (mspr instanceof Fruit) {
					fr = (Fruit)mspr;
					// only non-visible sprite we should draw is fruit, as points
					if (fr.wasJustEaten()) {
						g.setColor(PacManColors.PINK);
						g.drawString("" + fr.getScore(), sprX, sprY + g.getFont().getSize());
					}
					else {
						drawFruit(g, fr, sprX, sprY);
					}
				}
			}
		}

		if (myModel.isPaused()) {
			g.setColor(PacManColors.WHITE);
			drawWord(g, "PAUSED!", "", 11, 17);
		}
	}

	/** Draw the given word on the screen, one letter per game grid cell. */
	protected void drawWord(Graphics g, String word, String throwaway, int gx, int gy) {
		g.drawString(word,
					(int)(gx * myXScaleFactor * Level.GRID_SIZE),
					(int)((gy+1) * myYScaleFactor * Level.GRID_SIZE));
//		int length = word.length();
//		for (int i = 0;  i < length;  i++)
//			g.drawString("" + word.charAt(i),
//					(int)((gx+i) * myXScaleFactor * Level.GRID_SIZE),
//					(int)((gy+1) * myYScaleFactor * Level.GRID_SIZE));
	}

	/** Draw the given fruit on the screen at the given coordinates. */
	private void drawFruit(Graphics g, Fruit fr, int frX, int frY) {
		int frWidth  = (int)(myXScaleFactor * 2*fr.getWidth());
		int frHeight = (int)(myYScaleFactor * 2*fr.getHeight());

		if (fr instanceof Cherry) {
			g.setColor(PacManColors.RED);
			g.fillOval(frX, frY + frHeight/4, frWidth/2, frHeight/2);
			g.fillOval(frX + frWidth/2, frY + frHeight/2, frWidth/2, frHeight/2);

			g.setColor(PacManColors.BROWN);
			g.drawLine(frX + frWidth/4, frY + frHeight/2, frX + frWidth, frY);
			g.drawLine(frX + 3*frWidth/4, frY + 3*frHeight/4, frX + frWidth, frY);

			g.setColor(PacManColors.WHITE);
			g.drawLine(frX + frWidth/8, frY + frHeight/2, frX + frWidth/4, frY + 5*frHeight/8);
			g.drawLine(frX + 5*frWidth/8, frY + 3*frHeight/4, frX + 3*frWidth/4, frY + 7*frHeight/8);
		}

		// Strawberry
		else if (fr instanceof Strawberry) {
			g.setColor(PacManColors.RED);
			g.fillOval(frX, frY + frHeight/4, frWidth, 3*frHeight/4);

			g.setColor(PacManColors.GREEN);
			g.fillOval(frX + frWidth/8, frY + frHeight/4, 3*frWidth/4, frHeight/4);

			g.setColor(PacManColors.WHITE);
			g.fillRect(frX + frWidth/2 - 1, frY + frHeight/8, 2, frHeight/4);
			g.fillRect(frX + frWidth/8, frY + frHeight/2, 1, 1);
			g.fillRect(frX + 7*frWidth/8, frY + frHeight/2, 1, 1);
			g.fillRect(frX + frWidth/2, frY + 3*frHeight/4, 1, 1);
			g.fillRect(frX + frWidth/4, frY + 5*frHeight/8, 1, 1);
			g.fillRect(frX + 3*frWidth/4, frY + 5*frHeight/8, 1, 1);
			g.fillRect(frX + 3*frWidth/8, frY + 7*frHeight/8, 1, 1);
			g.fillRect(frX + 5*frWidth/8, frY + 7*frHeight/8, 1, 1);
		}

		// Orange
		else if (fr instanceof Orange) {
			g.setColor(PacManColors.ORANGE);
			g.fillOval(frX + frWidth/8, frY + frHeight/8, 3*frWidth/4, 3*frHeight/4);

			g.setColor(PacManColors.BROWN);
			g.fillRect(frX + frWidth/2 - 1, frY + frHeight/8, 2, frHeight/4);

			g.setColor(PacManColors.GREEN);
			g.fillOval(frX + frWidth/2 - 1, frY, 3*frWidth/8, frHeight/4);
		}

		// Apple
		else if (fr instanceof Apple) {
			g.setColor(PacManColors.RED);
			g.fillOval(frX + frWidth/8, frY + frHeight/8, 3*frWidth/4, 3*frHeight/4);

			g.setColor(PacManColors.BROWN);
			g.drawLine(frX + frWidth/2, frY + frHeight/4, frX + 5*frWidth/8, frY + frHeight/8);

			g.setColor(PacManColors.WHITE);
			g.drawLine(frX + 5*frWidth/8, frY + 5*frHeight/8, frX + 3*frWidth/4, frY + 3*frHeight/8);
		}
	}

	/** Changes fonts.  Used when the game scales. */
	private void setFont(int size) {
		myFont = new Font("SansSerif", Font.BOLD, size + 1);
	}

	/** Called to notify this view that its size has changed; used for scaling. */
	public void updateSize() {
		Level level = myModel.getCurrentLevel();
		if (level == null)
			return;

		// preferred size is the width and height of the board, with 5 extra
		// vertical rows (3 for top status, 2 for bottom lives/fruit)
		int width  = (int)(PREFERRED_SCALE_FACTOR * Level.GRID_SIZE
				* Math.max(28, level.getGridWidth())  + 1);
		int height = (int)(PREFERRED_SCALE_FACTOR * Level.GRID_SIZE
				* Math.max(31 + 3 + 2, level.getGridHeight() + 3 + 2) + 1);
		this.setPreferredSize(new Dimension(width, height));

		setFont((int)(Math.min(myXScaleFactor, myYScaleFactor) * Level.GRID_SIZE));
	}

	/** Updates this view's image of the level background.  Done whenever the level changes. */
	public void updateBackground() {
		// construct offscreen buffer and other stuff
		// (not done at construction because it won't work;
		//  the view context/peer isn't set up until after construction!)
		Level level = myModel.getCurrentLevel();
		myBackground = this.createImage(getSize().width, getSize().height);

		// create predrawn background image of walls for faster painting
		Graphics g2 = myBackground.getGraphics();
		g2.setColor(Color.black);
		g2.setFont(myFont);
		g2.fillRect(0, 0, getSize().width, getSize().height);

		// move the 'origin' so that the coordinates will line up right
		g2.translate(0, (int)(2.5 * myYScaleFactor * Level.GRID_SIZE));

		int sprX      = 0;
		int sprY      = 0;
		int sprWidth  = 0;
		int sprHeight = 0;
		int hex = 0;
		int x1, x2, x3, x4, x5, x6, x7, x8, x9;
		int y1, y2, y3, y4, y5, y6, y7, y8, y9;
		int[] xPoints = null;
		int[] yPoints = null;

		int gridWidth  = level.getGridWidth();
		int gridHeight = level.getGridHeight();
		Sprite[][] map = level.getGrid();

		for (int x = 0;  x < gridWidth;  x++)
		for (int y = 0;  y < gridHeight;  y++) {
			Sprite spr = map[x][y];

			if (!(spr instanceof ObstacleSprite))
				continue;

			sprX      = (int)(myXScaleFactor * spr.rect.x);
			sprY      = (int)(myYScaleFactor * spr.rect.y);
			sprWidth  = (int)(myXScaleFactor * spr.rect.width  + 1);
			sprHeight = (int)(myYScaleFactor * spr.rect.height + 1);

			hex = ((ObstacleSprite)spr).getHex();

			// points in the wall's grid square:
			// 1  2  3
			// 4  5  6
			// 7  8  9
			x1 = sprX;					y1 = sprY;
			x2 = sprX + sprWidth/2;		y2 = sprY;
			x3 = sprX + sprWidth;		y3 = sprY;
			x4 = sprX;					y4 = sprY + sprHeight/2;
			x5 = x2;					y5 = y4;
			x6 = x3;					y6 = y4;
			x7 = sprX;					y7 = sprY + sprHeight;
			x8 = x2;					y8 = y7;
			x9 = x3;					y9 = y7;

			xPoints =  (hex ==  1)      ?  new int[] {x1, x2, x5, x4}
					:  (hex ==  2)      ?  new int[] {x2, x3, x6, x5}
					:  (hex ==  3)      ?  new int[] {x1, x3, x6, x4}
					:  (hex ==  4)      ?  new int[] {x4, x5, x8, x7}
					:  (hex ==  5)      ?  new int[] {x1, x2, x8, x7}
					:  (hex ==  6)      ?  new int[] {x1, x2, x5, x4}  // won't happen
					:  (hex ==  7)      ?  new int[] {x1, x3, x6, x5, x8, x7}
					:  (hex ==  8)      ?  new int[] {x5, x6, x9, x8}
					:  (hex ==  9)      ?  new int[] {x1, x2, x5, x4}  // won't happen
					:  (hex == 10)      ?  new int[] {x2, x3, x9, x8}
					:  (hex == 11)      ?  new int[] {x1, x3, x9, x8, x5, x4}
					:  (hex == 12)      ?  new int[] {x4, x6, x9, x7}
					:  (hex == 13)      ?  new int[] {x1, x2, x5, x6, x9, x7}
					:  (hex == 14)      ?  new int[] {x2, x3, x9, x7, x4, x5}
					:  (hex == 15)      ?  new int[] {x1, x3, x9, x7}
					:  null;

			yPoints =  (hex ==  1)      ?  new int[] {y1, y2, y5, y4}
					:  (hex ==  2)      ?  new int[] {y2, y3, y6, y5}
					:  (hex ==  3)      ?  new int[] {y1, y3, y6, y4}
					:  (hex ==  4)      ?  new int[] {y4, y5, y8, y7}
					:  (hex ==  5)      ?  new int[] {y1, y2, y8, y7}
					:  (hex ==  6)      ?  new int[] {y1, y2, y5, y4}  // won't happen
					:  (hex ==  7)      ?  new int[] {y1, y3, y6, y5, y8, y7}
					:  (hex ==  8)      ?  new int[] {y5, y6, y9, y8}
					:  (hex ==  9)      ?  new int[] {y1, y2, y5, y4}  // won't happen
					:  (hex == 10)      ?  new int[] {y2, y3, y9, y8}
					:  (hex == 11)      ?  new int[] {y1, y3, y9, y8, y5, y4}
					:  (hex == 12)      ?  new int[] {y4, y6, y9, y7}
					:  (hex == 13)      ?  new int[] {y1, y2, y5, y6, y9, y7}
					:  (hex == 14)      ?  new int[] {y2, y3, y9, y7, y4, y5}
					:  (hex == 15)      ?  new int[] {y1, y3, y9, y7}
					:  null;

			g2.setColor((spr.type == Sprite.WALL) ? PacManColors.BLUE
											  : PacManColors.PINK);
			if (xPoints == null  ||  yPoints == null)
				throw new RuntimeException("null shape, type = " + spr.getClass().getName() + ", hex = " + hex);
			g2.fillPolygon(xPoints, yPoints, xPoints.length);
		}
	}

	/** ComponentListener implementation. */
	public void componentResized(ComponentEvent event) {
		super.componentResized(event);
		Level level = myModel.getCurrentLevel();
		if (level == null)
			return;
		if (getSize().width == 0  ||  level.getWidth() == 0)
			return;

		myXScaleFactor = 1.0 * getSize().width  / level.getWidth();
		myYScaleFactor = 1.0 * getSize().height / (level.getHeight() + 5 * Level.GRID_SIZE);

		if (SHOULD_MAINTAIN_ASPECT_RATIO)
			myXScaleFactor = myYScaleFactor = Math.min(myXScaleFactor, myYScaleFactor);
	}
}