package pacman.utility;

import java.applet.AudioClip;
import java.applet.Applet;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/** A widget to fetch images and sound in a black-box manner regardless
  * of whether in an applet or application environment.
  */
public class ResourceFetcher {
	private static final boolean DEBUG = false;
	private static final Component DUMMY_COMPONENT = new Panel();

	private Applet myApplet = null;
	private Toolkit myToolkit = null;
	private MediaTracker myTracker;
	private boolean myIsApplet;

	/** Constructs a new resource fetcher to get its resources from
	  * the given applet.  Used in an applet environment.
	  */
	public ResourceFetcher(Applet applet) {
		if (applet == null)
			throw new IllegalArgumentException("null applet passed");

		myApplet = applet;
		myIsApplet = true;
	}

	/** Constructs a new resource fetcher to get its resources from
	  * the given toolkit.  Used in an application environment.
	  */
	public ResourceFetcher(Toolkit tk) {
		if (tk == null)
			throw new IllegalArgumentException("null toolkit passed");

		myToolkit = tk;
		myIsApplet = false;
	}

	/** Gets and returns the image at the given file location, without
	  * waiting for it to load from the disk.  But it does, however,
	  * queue up this image for tracking if the waitForAll method is
	  * later called.
	  */
	public Image addImage(String fileName) {
		Image img = null;

		try {
			if (myIsApplet) {
				// string represents a relative path to an url
				URL source = ClassLoader.getSystemResource(fileName);
				if (source == null) {
					// System.err.println("null!  backup");
					try {
						source = new URL(myApplet.getCodeBase(), fileName);
					} catch (MalformedURLException mfurle) {
						source = null;
					}
				}

				img = myApplet.getImage(source);
				if (img == null) {
					// System.out.println("still null!");
				}

/*
				// changed for compatibility with Netscape browser (POS)
				InputStream is = myApplet.getClass().getResourceAsStream(fileName);
				if (is == null)
					throw new RuntimeException("Can't find image");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					int c;
					while ((c = is.read()) >= 0)
						baos.write(c);
					img = myApplet.getToolkit().createImage(baos.toByteArray());
				} catch (IOException ioe) {
					throw new RuntimeException(ioe.getMessage());
				}
*/
			}
			else
				// string represents a local file name
				img = myToolkit.getImage(fileName);
				// img = myToolkit.getImage(ClassLoader.getSystemResource(fileName));
		}
		catch (SecurityException sex) {
			throw new RuntimeException("ResourceFetcher: I don't have permission to load this image: " + fileName);
		}

		return img;
	}

	/** Fetches the file at the given file location as a byte stream. */
	public InputStream fetchFile(String fileName) {
		// InputStream stream = ClassLoader.getSystemResourceAsStream(fileName);
		InputStream stream = null;
		try {
			stream = new FileInputStream(fileName);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		if (stream == null) {
			throw new IllegalStateException("Null resource (file not found) for " + fileName + "\n"
					+ "full path: " + ClassLoader.getSystemResource(fileName));
		}
		return stream;
	}

	/** Fetches the image at the given file location.  Does not return until
	  * that image has loaded from the disk.	  */
	public Image fetchImage(String fileName) {
		Image img = addImage(fileName);
		waitFor(img);
		// System.out.println("
		return img;
	}

	/** Waits for the given image to load from its disk file. */
	public void waitFor(Image img) {

		if (myTracker == null) {
			Component componentToTrack = (myIsApplet) ? myApplet : DUMMY_COMPONENT;
			myTracker = new MediaTracker(componentToTrack);
		}

		myTracker.addImage(img, 0);
		try {
			myTracker.waitForAll();
		} catch (InterruptedException ie) {};
	}

	/** Waits for all images associated with this fetcher. */
	public void waitForAll() {
		try {
			myTracker.waitForAll();
		} catch (InterruptedException ie) {};
	}

	/** Returns the audio clip at the given file location. */
	public AudioClip getAudioClip(String fileName) {
		AudioClip clip = null;

		try {
			if (myIsApplet) {
				// string represents a relative path to an url
				if (DEBUG) System.out.println("ResourceFetcher.getAudioClip:");
				if (DEBUG) System.out.println("-- in applet mode; fetching " + fileName);
				URL source = ClassLoader.getSystemResource(fileName);
				if (source == null) {
					if (DEBUG) System.out.println("-- null; fetching from codebase " + myApplet.getCodeBase());
					try {
						source = new URL(myApplet.getCodeBase(), fileName);
					} catch (MalformedURLException mfurle) {
						if (DEBUG) System.out.println("-- null again");
						source = null;
					}
				}

				if (DEBUG) System.out.println("-- getting from source " + source);
				clip = myApplet.getAudioClip(source);
			}
			else {
				// string represents a local file name
				// clip = Applet.newAudioClip(new URL("file:" + fileName));
				// throw new RuntimeException("unsupported");

				// URL url = ClassLoader.getSystemResource(fileName);
				URL url = null;
				try {
					url = new URL("file:" + System.getProperty("user.dir") + "/" + fileName);
				} catch (MalformedURLException mfurle) {}
				
				if (DEBUG) System.out.println("-- not in applet mode; fetching " + fileName);
				if (DEBUG) System.out.println("-- working dir " + System.getProperty("user.dir"));
				if (DEBUG) System.out.println("-- fetching from system resource " + url);
				
                clip = Applet.newAudioClip(url);
            }
		}
		catch (SecurityException sex) {
			throw new RuntimeException("ResourceFetcher: I don't have permission to load this image: " + fileName);
		}
//		catch (MalformedURLException sex) {
//			throw new RuntimeException("bad URL for file: " + fileName);
//		}

		return clip;
	}
	
	/*
	public Clip getClip(String fileName) {
		AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start(); 
	}
	*/
}
