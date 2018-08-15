package pacman.utility;

/** A sorted treelike list of elements used to store locations in order for
  * my smart strategy.  Implemented using an array.
  */
public class PriorityQueue implements java.io.Serializable {
    private static final long serialVersionUID = 0;
    
	public static class Location {
		public final Location parent;
		public final int x, y, cost, heuristic, f;

		public Location(Location p, int tx, int ty, int tcost, int theur) {
			parent = p;  x = tx;  y = ty;  cost = tcost;  heuristic = theur;
			f = cost + heuristic;
		}

		public boolean equals(Object other) {
			Location l = (Location)other;
			return x == l.x  &&  y == l.y;
		}

		public String toString() {
			return "(" + x + "," + y + "):cost=" + cost + ",heur=" + heuristic + ",f=" + f;
		}
	}

	public static final Object ASCENDING  = new Object();
	public static final Object DESCENDING = new Object();

	private static final int FIRST_VALID_INDEX = 1;

	private Location[] myElements;
	private int mySize = 0;
	private int myBoundingValue;
	private Object myType;

	/** Constructs a new PriorityQueue with the given maximum size. */
	public PriorityQueue(int size, Object type, int boundingValue) {
		myElements = new Location[size + 1];
		myBoundingValue = boundingValue;
		myType = type;
	}

	/** Adds a new location into this PriorityQueue. */
	public void push(Location l) {
		int i = ++mySize;
		int rating = 0;

		// check where to insert this node; if it's a hi-pri node, slide old nodes
		// ( a parent is always at location [child/2] )
		while (true) {
			rating = (i != FIRST_VALID_INDEX  ?  myElements[i/2].f  :  myBoundingValue);
			if ((myType == ASCENDING) ? (rating > l.f) : (rating < l.f)) {
				myElements[i] = myElements[i/2];
				i /= 2;
			}
			else break;
		}

		myElements[i] = l;
	}

	/** Removes and returns the location on top of this priority queue. */
	public Location pop() {
		return removeElementAt(FIRST_VALID_INDEX);
	}

	public Location removeElementAt(int index) {
//		if (isEmpty())
//			throw new EmptyStackException();

		int i, child;
		Location best = myElements[index];
		Location last = myElements[mySize--];

		// must reorganize this index and all its children
		for (i = index;  i*2 <= mySize;  i = child) {
			child = i*2;
			// System.out.println("pop: mySize=" + mySize + ", i=" + i + ", child=" + child);
			if (child != mySize  &&  (myType == ASCENDING  ?  (myElements[child + 1].f < myElements[child].f)
			                                               :  (myElements[child + 1].f > myElements[child].f)))
				child++;
			if (myType == ASCENDING  ?  (last.f > myElements[child].f)
			                         :  (last.f < myElements[child].f))
				myElements[i] = myElements[child];
			else break;
		}

		myElements[i] = last;
		return best;
	}

	/** Returns true if this priority queue has no elements. */
	public boolean isEmpty() {
		return mySize == 0;
	}

	/** Returns the element at the given index of this priority queue. */
	public Location get(int index) {
		return elementAt(index);
	}

	/** Returns the element at the given index of this priority queue. */
	public Location elementAt(int index) {
//		if (!(0 <= index  &&  index < mySize))
//			throw new IllegalArgumentException("invalid index passed");
		return myElements[index];
	}

	/** Finds index of object o using a binary search. */
	public int indexOf(Location l, int boundingValue) {

/*
		int i = mySize / 2;
		int amountToSearth = mySize;
		Location curr = null;

		while (true) {
			curr = myElements[i];

			// easy case--found it
			if (curr.equals(l))
				return i;

			// harder case--keep searching
			else {
				// we can now divide up the array in half
				amountToSearch /= 2;

				// if there are 0 indeces left to search, it isn't here!
				if (amountToSearch == 0)
					return -1;

				if (curr.f < l.f) {
					// element we're looking for is to the right
					i += amountToSearch/2;

				}
				else if (curr.f < l.f) {
					i -= amountToSearch/2;
				}
			}
		}
*/


		for (int i = FIRST_VALID_INDEX;  i < mySize;  i++)
			if ((myType == ASCENDING)  ?  (myElements[i].f > boundingValue)
			                           :  (myElements[i].f < boundingValue))
				break;
			else // if (myElements[i].equals(o))
				if (myElements[i].x == l.x  &&  myElements[i].y == l.y)
				return i;
		return -1;
	}

	/** Returns true if this queue contains the given element. */
	public boolean contains(Location l) {
		return indexOf(l, myBoundingValue) != -1;
	}

	/** Returns the given element from this queue. */
	public void remove(Location l) {
		int index = indexOf(l, myBoundingValue);
		if (index != -1)
			removeElementAt(index);
	}

	/** Returns a string representation of this queue. */
	public String toString() {
		String result = "";
		for (int i = 0;  i <= mySize;  i++)
			result += i + ") " + myElements[i] + "\n";
		return result;
	}


/*
	// Tests this class.
	public static void main(String[] args) {
		//PriorityQueue q = new PriorityQueue(100, PriorityQueue.DESCENDING, 1000);
		PriorityQueue q = new PriorityQueue(100, PriorityQueue.ASCENDING, 0);

		System.out.println("Adding 1st element.");
		q.push(new Location(null, 1, 1, 10, 23));
		System.out.println(q.toString());

		System.out.println("Adding 2nd element.");
		q.push(new Location(null, 2, 2, 3, 5));
		System.out.println("---\n" + q.toString());

		System.out.println("Adding 3rd element.");
		q.push(new Location(null, 3, 3, 7, 16));
		System.out.println("---\n" + q.toString());

		System.out.println("Adding 4th element.");
		q.push(new Location(null, 4, 4, 11, 9));
		System.out.println("---\n" + q.toString());

		System.out.println("Popping best element: ");
		while (!q.isEmpty()) {
			Location l = q.pop();
			System.out.println("I got " + l);
		}
	}
*/
}