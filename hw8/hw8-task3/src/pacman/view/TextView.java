package pacman.view;

import pacman.model.*;
import pacman.sprite.*;
import pacman.utility.*;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

/** This class represents the JPanel for the second view of the game, which is
  * a 2D text representation of the game.  This view is cheap and easy to write.
  */
public class TextView extends View implements ComponentListener {
    private static final long serialVersionUID = 0;
    
	public static boolean IN_COLOR = true;
	protected Font    myFont         = null;
	protected int     myFontSize     = 8;
	protected double  myFontSpacingX = 8.0;
	protected double  myFontSpacingY = 8.0;

	/** Constructs a new View to view the given GameModel. */
	public TextView(GameModel gm, Component parent, ResourceFetcher fetch) {
		this(gm, parent, fetch, GameModel.Type.PACMAN);
	}

	/** Constructs a new text view of the given game type. */
	public TextView(GameModel gm, Component parent, ResourceFetcher fetch, Object type) {
		super(gm, parent, fetch, type);
		this.setBackground(Color.black);
		setFont(8);
	}

	/** Paints this view on the screen. */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setFont(myFont);
		if (!IN_COLOR)
			g.setColor(Color.white);

		Level level = myModel.getCurrentLevel();

		// scale text
		myFontSpacingX = 1.0 * getSize().width  /  level.getGridWidth();
		myFontSpacingY = 1.0 * getSize().height / (level.getGridHeight() + 5);
		int fontSize = (int)Math.min(myFontSpacingX, myFontSpacingY);
		if (fontSize != myFontSize)
			setFont(fontSize);

		// draw header with score, etc.
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

		g.translate(0, (int)(myFontSpacingY * 3));

		// draw grid
		int gridWidth = level.getGridWidth();
		int gridHeight = level.getGridHeight();
		Sprite[][] map = level.getGrid();
		for (int y = 0;  y < gridHeight;  y++)
		for (int x = 0;  x < gridWidth;  x++)
			if (!drawOneSprite(g, map[x][y]))
				System.out.println("null square at (" + x + ", " + y + ")");

		// draw fruit on bottom
		Fruit fr = myModel.getFruit();
		if (fr != null) {
			if (IN_COLOR)
				g.setColor(fr.getColor());
			drawWord(g, fr.toString(), "", level.getGridWidth() - 4, level.getGridHeight());
		}

		// draw lives on bottom
		String numLives = "";
		for (int i = 0;  i < myModel.getNumLives();  i++)
			numLives += "P ";
		if (IN_COLOR)  g.setColor(PacManColors.YELLOW);
		drawWord(g, numLives, "", 2, level.getGridHeight());

		// draw "game over" if demo is playing
		if (myModel.justStarted()) {
			// draw player's prompt but no sprites
			g.setColor(PacManColors.WHITE);
			drawWord(g, "PLAYER " + Utility.getUpperCaseWordForNumber(myModel.getPlayerNumber()), "", 9, 10);

			if (IN_COLOR)  g.setColor(PacManColors.YELLOW);
			drawWord(g, "READY!", "", 11, 16);
		} else {
			// draw pac man and ghosts
			List<MovingSprite> sprites = myModel.getMovingSprites();
			if (sprites != null)
				for (MovingSprite mspr : sprites) {
					if (mspr.type == Sprite.PACMAN  &&  !myModel.shouldDrawPacMan())  continue;

					drawOneSprite(g, mspr);
				}
		}

		if (myModel.getPacMan() != null  &&  myModel.getPacMan().wasJustRevived()) {
			if (IN_COLOR)  g.setColor(PacManColors.YELLOW);
			drawWord(g, "READY!", "", 11, 16);
		}

		if (myModel.isPaused()) {
			g.setColor(PacManColors.WHITE);
			drawWord(g, "PAUSED!", "", 11, 16);
		}
	}

	/** Draws the given sprite on the screen. */
	private boolean drawOneSprite(Graphics g, Sprite spr) {
		if (spr == null) {
			//  System.out.println("null square");
			return false;
		}
		if (spr.isVisible) {
			double gx = (spr.type == Sprite.GHOST  ||  spr.type == Sprite.PACMAN)
				?  1.0 * spr.getX() / Level.GRID_SIZE
				:  1.0 * spr.getGridX();
			double gy = (spr.type == Sprite.GHOST  ||  spr.type == Sprite.PACMAN)
				?  1.0 * (spr.getY() + 1) / Level.GRID_SIZE
				:  1.0 * spr.getGridY();

			int drawx = (int)(myFontSpacingX * gx);
			int drawy = (int)(myFontSpacingY * gy);

			if (IN_COLOR)
				g.setColor(spr.getColor());
			g.drawString("" + spr.toString(), drawx, drawy);
		}

		return true;
	}

	/** Draws the given word on the screen, one letter per grid square. */
	protected void drawWord(Graphics g, String word, String throwaway, int gx, int gy) {
		for (int i = 0;  i < word.length();  i++)
			g.drawString("" + word.charAt(i),
					(int)((gx+i) * myFontSpacingX),
					(int)((gy+1) * myFontSpacingY));
	}

	/** Changes fonts.  Used when the game scales. */
	private void setFont(int size) {
		myFontSize = size;
		myFont = new Font("Monospaced", Font.BOLD, myFontSize + 1);
	}

	/** Called to notify this view that its size has changed; used for scaling. */
	public void updateSize() {
		Level level = myModel.getCurrentLevel();

		// figure out how big I can make the text
		if (level.getGridWidth() == 0  ||  level.getGridHeight() == 0) {
			this.setPreferredSize(new Dimension(28 * Level.GRID_SIZE, (31+5) * Level.GRID_SIZE));
			return;
		}

		int preferredFontSize   = 8;
		int preferredGridWidth  = level.getGridWidth();
		int preferredGridHeight = level.getGridHeight() + 5;


		// set how big I would _like to be
		int preferredWidth  = preferredGridWidth  * preferredFontSize + 2;
		int preferredHeight = preferredGridHeight * preferredFontSize + 2;
		this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
	}

	/** ComponentListener implementation; does nothing. */
	public void componentResized(ComponentEvent event) {
		super.componentResized(event);
		updateSize();
	}
}