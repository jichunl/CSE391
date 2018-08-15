package pacman;

import javax.swing.*;

/** Runs the game in a window frame. */
public class PacManMain {
    public static void main(String[] args) {
    	System.out.println("Pac-Man!");
    	System.out.println("by Marty Stepp");
    	System.out.println();
    	System.out.println("Right-click the window for game options.");
    	System.out.println("Type C to insert coin; type 1 for 1 player game.");
    	System.out.println("Use the arrow keys to move; type P to pause.");
    	System.out.println();
    	System.out.println("Make sure to try out the cool views from the menu!");
    	System.out.println();
    
        JFrame f = new JFrame();
        f.setTitle("Pac-Man");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
        PacManPanel a = new PacManPanel();
        a.init();
        f.add(a);
        f.pack();
        f.setVisible(true);
        a.start();
        
        // new MainFrame(new PacManApplet(), 640, 480).setVisible(true);
    }
}
