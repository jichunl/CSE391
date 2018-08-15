package pacman.model;

import pacman.model.Listenable;

/** An interface for objects that can listen to events in a Listenable model. */
public interface Listener {
	/** Called to notify this Listener of an event in the given listenable object. */
	public void gameUpdated(Listenable origin, Object source, Object eventType);

	/** Called to notify this Listener that it has been removed from its
	  * Listenable object's group of listeners.
	  */
	public void detach();
}