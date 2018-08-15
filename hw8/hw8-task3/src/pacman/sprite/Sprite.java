package pacman.sprite;

import java.awt.Color;
import java.awt.Rectangle;
import pacman.model.Level;
import pacman.utility.Enum;

/** A sprite, which means any significant object in the game: the player, enemies, walls, etc. */
public abstract class Sprite {
	private static final int DEFAULT_SIZE = Level.GRID_SIZE;

	/** Sprite status enumeration. */
	public static final Object STATUS_NORMAL = new Enum("NORMAL"),
		STATUS_EATEN        = new Enum("EATEN"),
		STATUS_SCARED       = new Enum("SCARED"),
		STATUS_CAGED        = new Enum("CAGED"),
		STATUS_LEAVING_CAGE = new Enum("LEAVING_CAGE"),
		STATUS_ZOMBIE       = new Enum("ZOMBIE");

	/** Type enumeration; avoids expensive instanceof calls. */
	public static final int APPLE = 1;
	public static final int CHERRY = 2;
	public static final int DOT = 4;
	public static final int DUMMYSPRITE = 8;
	public static final int EDIBLESPRITE = 16;
	public static final int EMPTYSQUARE = 32;
	public static final int FRUIT = 64;
	public static final int GATE = 128;
	public static final int GHOST = 256;
	public static final int LETTER = 512;
	public static final int MOVINGSPRITE = 1024;
	public static final int OBSTACLESPRITE = 4096;
	public static final int ORANGE = 8192;
	public static final int PACMAN = 16384;
	public static final int POWERPELLET = 32768;
	public static final int SPRITE = 65536;
	public static final int STRAWBERRY = 131072;
	public static final int TUNNEL = 262144;
	public static final int WALL = 524288;

	/** A global empty square that may be put into empty slots in levels. */
	public static final Sprite EMPTY = new EmptySquare();

	/** A global dummy sprite that may be used for collision detection. */
	public static final DummySprite DUMMY = new DummySprite();

	/** Number of instances of this class. */
	public static int ourNumInstances = 0;

	// INSTANCE VARIABLES
	protected String img;
	protected int initialX, initialY;
	public Rectangle rect;
	public boolean isVisible = true;
	protected Object myStatus = STATUS_NORMAL;
	protected Color myColor = Color.black;
	public int type;

	/** Constructs a new Sprite at the given pixel coordinates with a default size and image name. */
	public Sprite(int thex, int they) {
		this("", thex, they, DEFAULT_SIZE);
	}

	/** Constructs a new Sprite at the given pixel coordinates with the given size and a default image name. */
	public Sprite(int thex, int they, int thesize) {
		this("", thex, they, thesize);
	}

	/** Constructs a new Sprite at the given pixel coordinates with the given width/height and a default image name. */
	public Sprite(int thex, int they, int thewidth, int theheight) {
		this("", thex, they, thewidth, theheight);
	}

	/** Constructs a new Sprite at the given pixel coordinates with the given image name and a default size. */
	public Sprite(String theimage, int thex, int they) {
		this(theimage, thex, they, DEFAULT_SIZE);
	}

	/** Constructs a new Sprite at the given pixel coordinates with the given image name and size. */
	public Sprite(String theimage, int thex, int they, int thesize) {
		this(theimage, thex, they, thesize, thesize);
	}

	/** Constructs a new Sprite at the given pixel coordinates with the given width/height and image name. */
	public Sprite(String theimage, int thex, int they, int thewidth, int theheight) {
		img = theimage;
		rect = new Rectangle(thex, they, thewidth, theheight);
		initialX = thex;  initialY = they;
		myStatus = STATUS_NORMAL;
		ourNumInstances++;
	}

	/** Constructs a new Sprite whose data is a copy of that in the given other Sprite. */
	public Sprite(Sprite spr) {
		this(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());
		setVisible(spr.isVisible);
		setStatus(spr.getStatus());
		setColor(spr.getColor());
		setImageName(spr.getImageName());
	}

	/** Returns this Sprite's top-left corner horizontal x pixel position. */
	public final int getX() {
		return rect.x;
	}

	/** Returns this Sprite's top-left corner vertical y pixel position. */
	public final int getY() {
		return rect.y;
	}

	/** Returns this Sprite's center horizontal x pixel position. */
	public final int getCenterX() {
		return rect.x + rect.width/2;
	}

	/** Returns this Sprite's center vertical y pixel position. */
	public final int getCenterY() {
		return rect.y + rect.height/2;
	}

	/** Returns this Sprite's rightmost x pixel position. */
	public final int getRightX() {
		return rect.x + rect.width;
	}

	/** Returns this Sprite's bottommost y pixel position. */
	public final int getBottomY() {
		return rect.y + rect.height;
	}

	/** Returns this Sprite's horizontal grid column. */
	public final int getGridX() {
		return (int)Math.round(rect.x / Level.GRID_SIZE);
	}

	/** Returns this Sprite's vertical grid row. */
	public final int getGridY() {
		return (int)Math.round(rect.y / Level.GRID_SIZE);
	}

	/** Returns this Sprite's starting x pixel position from when it was created. */
	public final int getInitialX() {
		return initialX;
	}

	/** Returns this Sprite's starting y pixel position from when it was created. */
	public final int getInitialY() {
		return initialY;
	}

	/** Returns this Sprite's starting horizontal grid column from when it was created. */
	public final int getInitialGridX() {
		return (int)Math.round(initialX / Level.GRID_SIZE);
	}

	/** Returns this Sprite's starting vertical grid row from when it was created. */
	public final int getInitialGridY() {
		return (int)Math.round(initialY / Level.GRID_SIZE);
	}

	/** Returns this Sprite's width in pixels. */
	public final int getWidth() {
		return rect.width;
	}

	/** Returns this Sprite's height in pixels. */
	public final int getHeight() {
		return rect.height;
	}

	/** Returns this sprite's class name. */
	public String getName() {
		String className = getClass().getName();
		return className.substring(className.lastIndexOf(".") + 1, className.length());
	}

	/** Returns this Sprite's image name as a string.  This image name will be used
	  * by the image-using arcade view.
	  */
	public final String getImageName() {
		return img;
	}

	/** Returns this Sprite's status object. */
	public final Object getStatus() {
		return myStatus;
	}

	/** Returns this Sprite's current color. */
	public Color getColor() {
		return myColor;
	}

	/** Sets this Sprite's x pixel position to be the given value. */
	public final void setX(int n) {
		rect.x = n;
	}

	/** Sets this Sprite's y pixel position to be the given value. */
	public final void setY(int n) {
		rect.y = n;
	}

	/** Sets this Sprite's starting x pixel position to be the given value. */
	public final void setInitialX(int n) {
		initialX = n;
	}

	/** Sets this Sprite's starting y pixel position to be the given value. */
	public final void setInitialY(int n) {
		initialY = n;
	}

	/** Sets this Sprite's width to be the given value. */
	public final void setWidth(int n) {
		rect.width = n;
	}

	/** Sets this Sprite's height to be the given value. */
	public final void setHeight(int n) {
		rect.height = n;
	}

	/** Sets whether this Sprite is visible or not. */
	public final void setVisible(boolean b) {
		isVisible = b;
	}

	/** Sets this Sprite's image name string to be the given value. */
	public final void setImageName(String s) {
		img = s;
	}

	/** Sets this Sprite's color to be the given value. */
	public final void setColor(Color c) {
		myColor = c;
	}

	/** Sets this Sprite's pixel position to be the given x/y pair. */
	public final void setPosition(int theX, int theY) {
		rect.x = theX;
		rect.y = theY;
	}

	/** Sets this Sprite's pixel size to be the given width/height pair. */
	public final void setSize(int theW, int theH) {
		rect.width  = theW;
		rect.height = theH;
	}

	/** Sets this Sprite's location and size to be the given values. */
	public final void setBounds(int theX, int theY, int theW, int theH) {
		rect.x = theX;
		rect.y = theY;
		rect.width  = theW;
		rect.height = theH;
	}

	/** Sets this Sprite's status to be the given value. */
	public void setStatus(Object o) {
		myStatus = o;
		if (o == STATUS_NORMAL)
			setVisible(true);
	}

	/** Sets this Sprite's position to be its initial value from when this sprite was created. */
	public void returnToStart() {
		setPosition(initialX, initialY);
	}

	/** Returns true if this sprite is visible. */
	public final boolean isVisible() {
		return isVisible;
	}

	/** Returns whether this sprite is at its starting position. */
	public final boolean isAtStart() {
		return rect.x == initialX  &&  rect.y == initialY;
	}

	/** Returns whether or not this sprite is at an even grid square; that is, whether
	  * or not its pixel positions in the x and y direction are an even multiple of
	  * the GRID_SIZE constant in the Level class.
	  */
	public final boolean isAtJuncture()  {
		return rect.x % Level.GRID_SIZE == 0  &&  rect.y % Level.GRID_SIZE == 0;
	}

//	/** Returns a Rectangle representing this sprite's position and size. */
//	private final Rectangle getBoundingBox() {
//		return rect;
//	}

	/** Returns whether or not this sprite's bounding box intersects that of the given
	  * other sprite; in other words, if the two sprites are currently touching/colliding.
	  */
	public final boolean collidesWith(Sprite spr) {
		return rect.intersects(spr.rect);
//		return !(spr.x + spr.width < x   ||  spr.x > x + width
//		     &&  spr.y + spr.height < y  ||  spr.y > y + height);
	}

	/** Returns a detailed string representation of this object. */
	public String dump() {
		return getClass().getName() + "(x=" + rect.x + ",y=" + rect.y + "), w=" + rect.width + ",h=" + rect.height + ",status=" + myStatus;
	}
}