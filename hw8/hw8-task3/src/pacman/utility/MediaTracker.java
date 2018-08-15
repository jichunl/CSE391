package pacman.utility;

import java.awt.Component;
import java.awt.Image;
import java.util.*;

public class MediaTracker {
	private Component myComponent;
	private List<Image> myImages = new ArrayList<Image>();

	public MediaTracker(Component comp) {
		myComponent = comp;
	}

	public void addImage(Image img) {
		addImage(img, 0);
	}

	public void addImage(Image img, int id) {
		myImages.add(img);
	}
	
	public void waitForID(int id) throws InterruptedException {
		waitForAll();
	}

	public void waitForAll() throws InterruptedException {
		System.out.println("Marty's media tracker running!");
		for (Image img : myImages) {
			while (img.getWidth(myComponent) == -1  ||  img.getHeight(myComponent) == -1)
				Thread.sleep(50);
		}
	}
}