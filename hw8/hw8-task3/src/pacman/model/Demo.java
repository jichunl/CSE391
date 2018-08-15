package pacman.model;

import java.io.*;

/** Represents a demo game that can be played back by the game model. */
public class Demo {
	private String myBuffer = "";
	private String myName;
	private Object myType;

	/** Constructs a new demo game with the given name that reads its moves
	  * from the given input stream.
	  */
	public Demo(InputStream is, String name, Object type) {
		myName = name;
		myType = type;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			while (reader.ready()) {
				myBuffer += reader.readLine() + "\n";
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Could not read demo: " + ioe);
		}
	}

	/** Returns a new full input stream from which to read this demo. */
	public InputStream getStream() {
		return new ByteArrayInputStream(myBuffer.getBytes());
	}

	/** Returns this demo's name. */
	public String getName() {
		return myName;
	}

	/** Returns this demo's type. */
	public Object getType() {
		return myType;
	}
}