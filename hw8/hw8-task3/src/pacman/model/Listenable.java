package pacman.model;

import pacman.model.Listener;
import java.util.*;

/** A class that represents an object that can be observed for events by outside Listeners. */
public class Listenable {
	private Set<Listener> myListeners = new HashSet<Listener>();

	/** Adds a listener for game events. */
	public void addListener(Listener l) {
        myListeners.add(l);
    }

	/** Notifies all listeners of a game event. */
	public void notifyListeners(Object source, Object eventType) {
		for (Listener listener : myListeners) {
			listener.gameUpdated(this, source, eventType);
		}
	}

	/** Removes the given listener for game events. */
	public void removeListener(Listener l) {
		myListeners.remove(l);
		l.detach();
	}
}