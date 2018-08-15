package pacman.utility;

/** Class workaround for Java's lack of printable enumerated types. */
public class Enum {
	private String myName;
	public Enum(String name) { myName = name; }
	public String toString() { return myName; }
}