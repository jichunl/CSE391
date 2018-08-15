package pacman;

import pacman.model.*;
import pacman.view.*;
import pacman.utility.*;

// import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;


/** This class represents the main applet that shows the Pac-Man game.
  *
  * <p>In terms of Model-View-Controller, this class is the Controller,
  * pulling the strings to make the game go.  It listens to button
  * clicks and key presses to progress the game.
  */
public class PacManPanel extends Panel implements ActionListener, Listener {
    private static final long serialVersionUID = 0;
    
	private static final String[] myPacLevels = {"levels/level1.map", "levels/level2.map", "levels/level3.map", "levels/level4.map"};
//	private static final String[] myPacLevels = {"levels/level1.map"};
	private static final String[] myMrsPacLevels = {"levels/level2.map", "levels/level3.map", "levels/level4.map"};
	private static final String[] myPacDemos = {"demos/demotest.dem"};
	private static final String[] myMrsPacDemos = {};  // "demos/demo2.dem", "demos/demo3.dem"};
	// private static final String HIGH_SCORE_FILE_NAME = "scores.dat";

	public static void main(String[] args) {
		// new MainFrame(new PacManPanel(), 224, 288).show();
	}

	private ResourceFetcher myFetcher;
	private PopupMenu myPopupMenu;
	private Dimension myViewDimension;
	private boolean myShouldAllow3DView = false;

	private GameModel myModel;

	private View myView;

	private KeyStroker myStroker;
	private MouseMonitor myMouser;

//	private URL myCodeBase = null;


	public PacManPanel() {
		// Compiler.disable();
        this.setPreferredSize(new Dimension(224, 288));
		setBackground(Color.black);
	}

	private void addAllLevels(String[] array, GameModel.Type type) {
		for (int i = 0;  i < array.length;  i++) {
			InputStream is = myFetcher.fetchFile(array[i]);
			myModel.addLevel(is, type);
		}
	}

	private void addAllDemos(String[] array, Object type) {
		for (int i = 0;  i < array.length;  i++) {
			InputStream is = myFetcher.fetchFile(array[i]);
		}
	}

	/** Constructs a new frame. */
	public void init() {
		// Compiler.disable();

		// consult HTML page to learn whether or not we should allow 3D view
		showStatus("Applet initializing...");
//		String threeD = getParameter("allow3Dview");
//		myShouldAllow3DView = (threeD != null  &&  threeD.equalsIgnoreCase("true"));

		setLayout(new BorderLayout());
//		myCodeBase = getCodeBase();

		// set up menus
		myPopupMenu = createMenu();

		// add various listeners
		myStroker = new KeyStroker();
		myMouser = new MouseMonitor();
		// addWindowListener(new WindowCloser());

		// set up image retrieval for views (different for applet/application)
		// myFetcher = new ResourceFetcher(this);
        myFetcher = new ResourceFetcher(Toolkit.getDefaultToolkit());

		// create game model
		showStatus("Constructing game model and level data...");
		myModel = new GameModel();
		myModel.addListener(this);

//		System.out.println("whee!");

		// read in the level map data
		addAllLevels(myPacLevels, GameModel.Type.PACMAN);
		addAllLevels(myMrsPacLevels, GameModel.Type.MRS_PACMAN);

		// load demo games
		addAllDemos(myPacDemos, GameModel.Type.PACMAN);
		addAllDemos(myMrsPacDemos, GameModel.Type.MRS_PACMAN);

		// layout
		// setSize(224, 272);
		showStatus("Loading view...");
		//setView(new ArcadeView(myModel, this, myFetcher, GameModel.PACMAN_TYPE));
		setView(new ArcadeView(myModel, this, myFetcher));
		showStatus("Running game.");
	}


	/** Called whenever a GUI component has an action performed on it.
	  * Used to process button and JRadioButton clicks.
	  */
	public void actionPerformed(ActionEvent event) {
		synchronized (myModel) {
			String command = event.getActionCommand().intern();
			if (command == "1 Player") {
				int result = myModel.newGame(1);
				if (result == -1) {
					showStatus("Not enough credits!");
                } else {
					showStatus("One-player Game");
                }
			} else if (command == "2 Players") {
				int result = myModel.newGame(2);
				if (result == -1)
					showStatus("Not enough credits!");
				else
					showStatus("Two-player Game");
			} else if (command == "View High Scores...") {
				String text = "";
				HighScoreList highScores = myModel.getHighScoreList();
				int numScores = highScores.getSize();
				for (int i = 0;  i < numScores;  i++) {
					text += i + ") " + highScores.getHighScore(i);
					if (i < numScores)
						text += "\n";
				}

				JOptionPane.showMessageDialog(this, text);
			} else if (command == "View Recorded Moves...") {
				final Frame f = new Frame();
				f.setSize(600, 300);
				f.setTitle("Saved Moves");
				f.addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent we) {f.dispose();} });

				TextArea area = new TextArea(/*myModel.getSavedMoves()*/);
				f.add(area);
				f.setVisible(true);
			} else if (command == "Insert Coin") {
				myModel.insertCoin();
				showStatus("Coin inserted");
			} else if (command == "End Game") {
				myModel.endGame();
			} else if (command == "2D View") {
				setView(new TwoDView(myModel, this, myFetcher));
			} else if (command == "Text View") {
				setView(new TextView(myModel, this, myFetcher));
			} else if (command == "Pac-Man") {
				setView(new ArcadeView(myModel, this, myFetcher, GameModel.Type.PACMAN));
			} else if (command == "Mrs. Pac-Man") {
				setView(new ArcadeView(myModel, this, myFetcher, GameModel.Type.MRS_PACMAN));
			// else if (command == "Pac-Mania")
			// 	setView(new PacManiaView(myModel, this));
			} else if (command == "3D View") {
				// setView(new ThreeDView(myModel, this, myFetcher));
			} else if (command == "Sound") {
				CheckboxMenuItem source = (CheckboxMenuItem)event.getSource();
				myModel.setSound(source.getState());
			} else if (command == "Color text view") {
				CheckboxMenuItem source = (CheckboxMenuItem)event.getSource();
				TextView.IN_COLOR = source.getState();
				myView.repaint();
			} else if (command == "Show FPS") {
				CheckboxMenuItem source = (CheckboxMenuItem)event.getSource();
				myModel.setWantsToShowFPS(source.getState());
			} else if (command == "About") {
				JOptionPane.showMessageDialog(this, "PacMartin\n\nby Martin Stepp\n\nStarted in Summer 2001 for UA C SC 335 project;\nrevised throughout summer and Fall 2001\n\nThis applet may not be redistributed, reposted, duplicated, reverse-engineered,\nor otherwise reused without Martin Stepp's written permission.\n\nPac-Man is (C) 1980 Namco");
			} else if (command == "Easy") {
				myModel.setDifficultyLevel(0);
			} else if (command == "Medium") {
				myModel.setDifficultyLevel(1);
			} else if (command == "Hard") {
				myModel.setDifficultyLevel(2);
			}
		}
	}


	/** Helper. Used by createMenuBar. */
	private MenuItem addMenuItem(String text, int mnemonic, Menu menu) {
		MenuItem item = new MenuItem(text);  //, enabled);
		if (mnemonic != -1)
			item.setShortcut(new MenuShortcut(mnemonic));
		item.addActionListener(this);
		menu.add(item);
		return item;
	}


	/** Sets this frame's size according to the preferred size of its view. */
	private void checkViewSize() {
		if (myView == null)
			return;

		else if (myViewDimension == null  ||  !myViewDimension.equals(myView.getPreferredSize())) {
			myViewDimension = myView.getPreferredSize();
			setSize(myViewDimension);
			validate();
		}
	}

	private CheckboxMenuItem addCheckbox(String title, boolean selected, char shortcut, Menu menu) {
		CheckboxMenuItem item = new CheckboxMenuItem(title, selected);
		item.setShortcut(new MenuShortcut(shortcut));
		item.addActionListener(this);
		menu.add(item);
		return item;
	}


	/** Creates this frame's menu system. */
	private PopupMenu createMenu() {
		PopupMenu bar = new PopupMenu("Main Menu");

		Menu jm_game = new Menu("Game");
		addMenuItem("Insert Coin", 'C', jm_game);
		addMenuItem("1 Player", '1', jm_game);
		addMenuItem("2 Players", '2', jm_game).setEnabled(false);
		jm_game.addSeparator();
		addMenuItem("Open", 'O', jm_game).setEnabled(false);
		addMenuItem("Save", 'S', jm_game).setEnabled(false);
		addMenuItem("Save As...", 'A', jm_game).setEnabled(false);
		addMenuItem("Close", 'X', jm_game);
		jm_game.addSeparator();
		Menu jm_difficulty = new Menu("Difficulty");
		addMenuItem("Easy", 'E', jm_difficulty);
		addMenuItem("Medium", 'M', jm_difficulty);
		addMenuItem("Hard", 'H', jm_difficulty);
		jm_game.add(jm_difficulty);
		jm_game.addSeparator();
		addMenuItem("View High Scores...", 'H', jm_game);
		addMenuItem("View Recorded Moves...", 'M', jm_game).setEnabled(GameModel.DEBUG);


		Menu jm_views = new Menu("Views");

		// add view selections
		addMenuItem("Text View",         'T', jm_views);
		addMenuItem("2D View",           '2', jm_views);
		if (myShouldAllow3DView)
			addMenuItem("3D View",      '3', jm_views);
		
		Menu jm_arcadeview = new Menu("Arcade View");
		addMenuItem("Pac-Man",      'P', jm_arcadeview);
		addMenuItem("Mrs. Pac-Man", 'M', jm_arcadeview);
		jm_views.add(jm_arcadeview);

		Menu jm_options = new Menu("Options");
		addCheckbox("Sound", true, 'S', jm_options);
		jm_options.addSeparator();
		addCheckbox("Color text view", true, 'C', jm_options).setEnabled(false);
		addCheckbox("Show FPS", false, 'F', jm_options).setEnabled(false);

		// addMenuItem("Pac-Mania",    'N', jm_views);

		Menu jm_help = new Menu("Help");
		addMenuItem("About", 'A', jm_help);

		// add menus to bar
		bar.add(jm_game);
		bar.add(jm_views);
		bar.add(jm_options);
		bar.add(jm_help);

		return bar;
	}


	/** Does nothing; required as part of Listener interface. */
	public void detach() {}


	/** Implementation of the Listener interface; used to respond
	  * to changes in the state of the Pac-Man game model.
	  */
	public void gameUpdated(Listenable origin, Object source, Object eventType) {
		// choose action to take based on type of event fired
		if (eventType == GameModel.Event.GAME_OVER) {
			HighScoreList highScores = myModel.getHighScoreList();
			int score = myModel.getScore(myModel.getPlayerNumber());
			if (highScores.wouldMakeList(score)) {
				// put this game's score into the high scores list
				String name = JOptionPane.showInputDialog(this, "Your score of " + score + " made the top ten scores list!\n\nEnter your name:");
				highScores.record(name, score);
			}
		}
		else if (eventType == GameModel.Event.SHUTTING_DOWN) {
			// stop();
		}

		checkViewSize();
	}


	/** Sets this applet's view of the game to be the given View. */
	private void setView(View v) {
		if (myView != null) {
			// remove old view
			myView.removeKeyListener(myStroker);
			myView.remove(myPopupMenu);
			myView.removeMouseListener(myMouser);
			myView.removeMouseMotionListener(myMouser);
			myModel.removeListener(myView);
			remove(myView);
		}

		// create new view
		myView = v;
		myView.addKeyListener(myStroker);
		myView.addMouseListener(myMouser);
		myView.addMouseMotionListener(myMouser);
		myView.add(myPopupMenu);

		myModel.addListener(myView);

		// add it to layout
		add(myView, BorderLayout.CENTER);
		validate();

		checkViewSize();

		myView.requestFocus();
	}


	public void start() {
		Thread th = new ModelUpdater();
		th.start();  //  *** I really should wait for new game to start this, I think
	}


	/** Stops this applet, ending the current game. */
	public void stop() {
		// myModel.saveScores(HIGH_SCORE_FILE_NAME);
		// myModel.endGame();
		myModel.terminateGame();
		myView.haltAllSounds();
	}


	public void destroy() {
		myModel.stop();
		myView.haltAllSounds();

		// wait for model to shut down
		try {
		    Thread.sleep(500);
		} catch (InterruptedException ie) {}
	}


	/** This class listens to key presses and uses them to drive the game. */
	private class KeyStroker extends KeyAdapter {
		protected long startTime = -1;

		public void keyPressed(KeyEvent event) {
			synchronized (myModel) {
				switch (event.getKeyCode()) {
					case KeyEvent.VK_1:
						int result = myModel.newGame(1);
						if (result == -1)
							showStatus("Not enough credits!");
						else
							showStatus("One-player Game");
						break;
					case KeyEvent.VK_2:
						if (myModel.getNumCredits() > 1  &&  myModel.isGameOver()) {
							showStatus("Two-player Game");
							myModel.newGame(2);
						} else {
							showStatus("Insert more coins!");
						}
						break;
					case KeyEvent.VK_UP:
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_LEFT:
					case KeyEvent.VK_RIGHT:
						myModel.setDesiredDir(event.getKeyCode());
						break;
					case KeyEvent.VK_C:
						myModel.insertCoin();
						showStatus("Coin inserted");
						break;
					case KeyEvent.VK_D:
						myModel.debugDump();
						break;
					case KeyEvent.VK_L:
						Level l = myModel.getCurrentLevel();
						if (l == null)  break;
						System.out.println(l);
						break;
					case KeyEvent.VK_P:
						boolean b_pause = !myModel.isPaused();
						showStatus("Game " + (b_pause ? "" : "un") + "paused");
						myModel.setPaused(b_pause);
						break;
					case KeyEvent.VK_R:
//						if (myModel.isRecording())
//							myModel.stopRecording();
//						else
//							myModel.startRecording();
//						showStatus( (myModel.isRecording()  ?  "Start"  :  "Stopp") + "ing recording");
						break;
					case KeyEvent.VK_S:
						boolean b_sound = !myModel.wantsSound();
						showStatus("Sound " + (b_sound ? "On" : "Off"));
						myModel.setSound(b_sound);
						break;
					case KeyEvent.VK_T:
						if (startTime == -1) {
							showStatus("Starting timer:");
							startTime = System.currentTimeMillis();
						}
						else {
							long time = System.currentTimeMillis() - startTime;
							showStatus("Timer stopped; took " + time + " ms");
							startTime = -1;
						}
					case KeyEvent.VK_X:
						// stop();
						break;

					// view hotkeys
//					case KeyEvent.VK_F1:
//						showStatus("Setting view to Text View");
//						setView(new TextView(myModel, PacManPanel.this, myFetcher));
//						break;
//					case KeyEvent.VK_F2:
//						showStatus("Setting view to 2D View");
//						setView(new TwoDView(myModel, PacManPanel.this, myFetcher));
//						break;
//					case KeyEvent.VK_F3:
//						if (myShouldAllow3DView) {
//							showStatus("Setting view to 3D View");
//							; // setView(new ThreeDView(myModel, PacManPanel.this, myFetcher));
//						}
//						break;
//					case KeyEvent.VK_F4:
//						showStatus("Setting view to Arcade View");
//						setView(new ArcadeView(myModel, PacManPanel.this, myFetcher, GameModel.Type.PACMAN));
//						break;
//					case KeyEvent.VK_F5:
//						showStatus("Setting view to Arcade Mrs. Pac-Man View");
//						setView(new ArcadeView(myModel, PacManPanel.this, myFetcher, GameModel.Type.MRS_PACMAN));
//						break;
				}
			}
		}
	}
    
    private void showStatus(String message) {
        System.out.println(message);
    }



	private class MouseMonitor implements MouseListener, MouseMotionListener {
		/** MouseListener implementation. */
		public void mousePressed(MouseEvent event) {}

		/** MouseListener implementation. */
		public void mouseReleased(MouseEvent event) {}

		/** MouseListener implementation. */
		public void mouseEntered(MouseEvent event) {}

		/** MouseListener implementation. */
		public void mouseExited(MouseEvent event) {}

		/** MouseListener implementation.  Pops up a popup menu. */
		public void mouseClicked(MouseEvent event) {
			// catch all but left-button clicks
			if ((event.getModifiers() & ~InputEvent.BUTTON1_MASK) != 0) {
				myModel.pause();
				myPopupMenu.show(myView, event.getX(), event.getY());
			}
		}

		/** MouseMotionListener implementation. */
		public void mouseDragged(MouseEvent event) {}

		/** MouseMotionListener implementation. Makes view take keyboard focus so controls work. */
		public void mouseMoved(MouseEvent event) {
			myView.requestFocus();
		}
	}


	private class ModelUpdater extends Thread {

		/** Runs the model.  Used to run the game in its own thread. */
		public void run() {
			long startTime = System.currentTimeMillis();
			int fpsCounter = 0;

			while (myModel.wantsToKeepRunning()) {
				// time how long it takes to update, so that fps is relatively correct
				if (myModel.getState() != GameModel.State.PAUSED) {
					myModel.update();
					myModel.notifyListeners(null, GameModel.Event.GAME_UPDATED);
					myView.requestFocus();
				}

				fpsCounter++;

				long updateTime = System.currentTimeMillis();

				if (updateTime - startTime >= 1000) {
					// one second has passed
					// if (SHOULD_PRINT_FPS)  System.out.println(fpsCounter + " fps");
					startTime = updateTime;
					myModel.setFPS(fpsCounter);
					fpsCounter = 0;
				}

				try {
					Thread.sleep(GameModel.REFRESH_DELAY);
				} catch (InterruptedException ie) {}
			}

//			notifyListeners(null, EVENT_SHUTTING_DOWN);
		}

	}
}
