package pacman.view;

import java.awt.*;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/** This class is an AWT Panel, but with automatic double-buffering support.
  * Double-buffering is the technique by which a graphical canvas's next frame
  * of image data is all drawn into an offscreen image buffer, then that image
  * buffer is drawn onto the visible screen.  The advantage of this technique
  * is that the canvas on the screen then appears smooth and does not flicker.
  * Java's Swing package includes components that already have automatic
  * double-buffering, such as JPanel.  However, AWT Panel objects do not
  * have this double-buffering.  This class alleviates that problem.

  * <p>The class has a few other enhancements over a standard AWT Panel:
  * - It has a working set/getPreferredSize system as exists in Swing;
  * - It has a paintComponent method to match Swing's method name;
  * - It can be set to scale its drawn image to fill its onscreen area
  *   using the setScaling(boolean) method.
  */
public class DoubleBufferedPanel extends Panel implements ComponentListener {
    private static final long serialVersionUID = 0;
    
	private static final boolean DEBUG = false;

	protected Image myOffScreenBuffer;
	protected boolean myIsScaling = false;
	protected Dimension myPreferredSize = new Dimension(10, 10);  // a Java default
	protected Component myImageCreatingComponent = this;


	/** Constructs a new double-buffered, non-scaling panel. */
	public DoubleBufferedPanel() {
		super();
		addComponentListener(this);
	}


	/** Constructs a new double-buffered, non-scaling panel using the given
	  * layout manager.
	  */
	public DoubleBufferedPanel(LayoutManager manager) {
		super(manager);
		addComponentListener(this);
	}


	/** Creates/resizes offscreen buffer as needed. */
	private void checkBuffer() {
		Dimension sizeToUse = getSize();
		if (myOffScreenBuffer == null  ||  myOffScreenBuffer.getWidth(this) != sizeToUse.width  ||  myOffScreenBuffer.getHeight(this) != sizeToUse.height) {
			if (sizeToUse.width == 0  ||  sizeToUse.height == 0)
				// can't make a 0x0 image; bail out
				return;

			if (DEBUG)  System.out.println("creating image of size " + sizeToUse.width + " x " + sizeToUse.height);
			// myOffScreenBuffer.getWidth(this) == 0  ||  myOffScreenBuffer.getHeight(this) == 0
			myOffScreenBuffer = myImageCreatingComponent.createImage(Math.max(sizeToUse.width, 1), Math.max(sizeToUse.height, 1));
		}
	}


	/** ComponentListener implementation; does nothing. */
	public void componentHidden(ComponentEvent e)  {}

	/** ComponentListener implementation; does nothing. */
	public void componentMoved(ComponentEvent e)   {}

	/** ComponentListener implementation; tracks resizing because the size
	  * of this panel's offscreen image buffer must match its size on the screen.
	  */
	public void componentResized(ComponentEvent e) {
		if (DEBUG)  System.out.println("componentResized");
		// desired size is whole panel if not scaling, or else the set preferred size
		// if we are in scaling mode
		if (myPreferredSize == null)
			myPreferredSize = new Dimension(getSize().width, getSize().height);

		checkBuffer();
		repaint();
	}

	/** ComponentListener implementation; does nothing. */
	public void componentShown(ComponentEvent e)   {}


	/** Returns the preferred size of this panel, which is the same as its current size. */
	public Dimension getPreferredSize() {
		return myPreferredSize;
	}


	/** Returns whether or not this panel is in scaling mode. */
	public boolean isScaling() {
		return myIsScaling;
	}


	/** Paints this panel on the screen. */
	public void paint(Graphics g) {
		if (DEBUG)  System.out.println("paint");
		checkBuffer();
		if (myOffScreenBuffer != null  &&  getSize().width > 0  &&  getSize().height > 0) {
			paintComponent(myOffScreenBuffer.getGraphics());
			g.drawImage(myOffScreenBuffer, 0, 0, this);
		}
		else {
			System.err.println("DoubleBufferedPanel.paint(Graphics): can't paint: " + myOffScreenBuffer);
		}
	}


	/** Convenience method; identical to paint(g), but has the same name
	  * as the cooresponding Swing class's painting method for Swing programmers.
	  */
	public void paintComponent(Graphics g) {
		if (DEBUG)  System.out.println("paintComponent");
		// super.paint(g);

		// fill in panel with background color to wipe it, a la super.paintComponent(g)
		g.setColor(getBackground());
		g.fillRect(0, 0, getSize().width, getSize().height);

		// debug prints
		if (DEBUG)  System.out.println("   offscreen: " + myOffScreenBuffer.getWidth(this) + ", " + myOffScreenBuffer.getHeight(this));
		if (DEBUG)  System.out.println("   preferred: " + myPreferredSize.width + ", " + myPreferredSize.height);
		if (DEBUG)  System.out.println("   actual   : " + getSize().width + ", " + getSize().height);

	}


/*
	public void repaint() {
		if (DEBUG)  System.out.println("repaint");

		Graphics g = getGraphics();
		if (g != null)
			update(g);

		super.repaint();
	}


	public void repaint(int x, int y, int width, int height) {
		if (DEBUG)  System.out.println("repaint (...)");

		Graphics g = getGraphics();
		if (g != null)
			update(g);

		super.repaint(x, y, width, height);
	}
*/


	/** Sets the component from which this Panel will create its images.
	  * By default, the panel will use itself to create images; however,
	  * if the panel is used in an applet, you must call this method and pass
	  * it a reference to the applet, otherwise the panel will be unable
	  * to create its offscreen buffer.
	  */
	public void setImageCreatingComponent(Component comp) {
		myImageCreatingComponent = comp;
	}



	/** Sets this panel's preferred size to the given dimensions. */
	public void setPreferredSize(Dimension preferredSize) {
		if (DEBUG)  System.out.println("setPreferredSize");
		myPreferredSize = new Dimension(preferredSize.width, preferredSize.height);
		// setSize(myPreferredSize);
	}


	/** Enables or disables scaling this panel.
	  * If in scaling mode, the panel scales its offscreen buffer from that buffer's
	  * preferred size to the panel's actual size.
	  *
	  * <p>So, for example, in resizing mode, if your preferred panel size is 30 x 30
	  * and the panel has actually been resized to 60 x 60, then everything you draw
	  * in your paint methods will be scaled by 2.
	  */
	public void setScaling(boolean b) {
		myIsScaling = b;
		repaint();
	}


	/** Called by AWT system to update the graphical context of this Panel.
	  * Writes all graphics to an offscreen image buffer to create double
	  * buffering.
	  */
	public void update(Graphics g) {
		if (DEBUG)  System.out.println("update");

		paint(g);
	}


/*
	// tests this class
	public static void main(String[] args) {
		Frame f = new Frame();
		f.setSize(200, 200);

		DoubleBufferedPanel panel = new DoubleBufferedPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.red);
				g.fillRect(0, 0, 10, 10);
			}
		};
		panel.setPreferredSize(new Dimension(20, 20));
		panel.setScaling(true);
		f.add(panel);
		f.show();
	}
*/
}
