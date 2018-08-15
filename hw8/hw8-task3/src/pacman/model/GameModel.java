package pacman.model;

import pacman.sprite.*;
import pacman.strategy.*;
import pacman.view.PacManColors;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;

/** Represents the overall model for the pac-man game state.
  * This model contains many constants for events, statuses of the game,
  * as well as information about the current score, level, high scores,
  * number of lives, moving sprites like Pac-Man and the ghosts, and so
  * on.  The model is also Listenable, which means that other Listener
  * objects such as views may be attached to it in order to hear about
  * events that occur in the model.
  */
public class GameModel extends Listenable {
    // CONSTANTS
    public static final boolean DEBUG            = false;
    public static final boolean DEMO_DEBUG       = false;
    public static final boolean MOVE_DEBUG       = false;
    public static final boolean SHOULD_PRINT_FPS = false;

    public static final int UPDATES_PER_SECOND = 30;
    public static final int REFRESH_DELAY      = 1000 / UPDATES_PER_SECOND;
    public static final int DEMO_LENGTH        = 21 * UPDATES_PER_SECOND;

    public static final int TIME_BETWEEN_GHOSTS     =  6;
    public static final int INITIAL_PELLET_TIME     =  7;
    public static final int MIN_PELLET_TIME         =  2;
    public static final int PELLET_WEARING_OFF_TIME =  2;

    public static final int EXTRA_LIFE_POINTS      = 10000;

    public static final HighScore[] DEFAULT_HIGH_SCORES = {
        new HighScore(100, "Marty"),
        new HighScore( 90, "Marty"),
        new HighScore( 80, "Marty"),
        new HighScore( 70, "Marty"),
        new HighScore( 60, "Marty"),
        new HighScore( 50, "Marty"),
        new HighScore( 40, "Marty"),
        new HighScore( 30, "Marty"),
        new HighScore( 20, "Marty"),
        new HighScore( 10, "Marty")
    };

    /** Constants for types of games. */
    public static enum Type {
        PACMAN,
        MRS_PACMAN
    }

    /** Game state enumeration. */
    public static enum State {
        GAME_OVER,
        IN_PROGRESS,
        PAUSED
    }

    /** Game event type enumeration. */
    public static enum Event {
        DOT_EATEN,
        PLAYER_DEATH,
        POWER_PELLET_EATEN,
        FRUIT_EATEN,
        GHOST_EATEN,
        COIN_INSERTED,
        NEW_GAME,
        GAME_OVER,
        EXTRA_LIFE,
        LEVEL_CLEARED,
        POWER_PELLET_WORN_OFF,
        GHOST_REVIVED,
        NEW_LEVEL,
        GAME_UPDATED,
        HALF_DOTS_EATEN, 
        THREE_QUARTERS_DOTS_EATEN,
        GAME_PAUSED,
        GAME_UNPAUSED,
        SHUTTING_DOWN
    }

    private class GameEvent {
        Object source;
        Object type;

        public GameEvent(Object s, Object t) {
            source = s;
            type = t;
        }
    }

    // INSTANCE VARIABLES
    private List<Demo> myDemos = new ArrayList<Demo>();
    private List<GameEvent> events = new ArrayList<GameEvent>();
    private List<MovingSprite> myMovingSprites = new ArrayList<MovingSprite>();
    private List<Level> myLevels        = new ArrayList<Level>();
    private Level myCurrentLevel = null;

    private HighScoreList myHighScores = new HighScoreList(DEFAULT_HIGH_SCORES);

//    private String[] mySavedMoves = null;
//    private boolean myIsRecording = false;

    private Type myType;
    private State myState = State.GAME_OVER;
    private int myDesiredDir;

    private long myGameOverTime;
    private int myPelletTime;
    private int myNumCredits = 0;
    private int myNumLives;
    private int myScore;
    private int myGhostScorePower;
    private int myLevelNumber = 0;
    private int myFPS = 0;
    private int myDifficultyMultiplier = 0;
    private int myDifficultyStart = 0;
    private long myUpdateCounter;
    private boolean myShouldKeepRunning = true;
    private boolean myWantsSound = true;
    private boolean myShouldDoGhostCollisions  = true;
    private boolean myShouldDoEdibleCollisions = true;
    private boolean myShouldShowPlayerNumber = true;
    private boolean myWantsToShowFPS = false;

//  CONSTRUCTORS
    /** Constructs a new game model. */
    public GameModel() {
        this(Type.PACMAN);
    }

    /** Constructs a new game model of the given type of game. */
    public GameModel(Type type) {
        myType = type;
        setGameOver();
    }


//  (MOSTLY) SIMPLE ACCESSOR/MUTATOR GET/SET METHODS

    /** Returns the delay in milliseconds between refreshes of the game state. */
    public int getDelay()       { return REFRESH_DELAY; }

    /** Returns a Move representing which way the current player
      * would like to go, as per what they last pushed on the keyboard.
      */
    public Move getDesiredMove() {
        return Move.newMove(
            myDesiredDir == KeyEvent.VK_LEFT  ? -PacMan.PAC_SPEED :
            myDesiredDir == KeyEvent.VK_RIGHT ?  PacMan.PAC_SPEED : 0,

            myDesiredDir == KeyEvent.VK_UP    ? -PacMan.PAC_SPEED :
            myDesiredDir == KeyEvent.VK_DOWN  ?  PacMan.PAC_SPEED : 0
        );
    }

    /** Returns this Game's Fruit. */
    public Fruit getFruit() {
        for (MovingSprite mspr : myMovingSprites) {
            if (mspr instanceof Fruit)
                return (Fruit) mspr;
        }

        return null;
    }

    /** Returns the power to which the last eaten ghost's score (200) was raised.
      * Used to show the score you get when you eat a ghost.
      */
    public int getGhostScorePower() {
        return myGhostScorePower;
    }

    /** Returns how many frames per second the model is running at. */
    public int getFPS() {
        return myFPS;
    }

    /** Returns true if this model would like its frames/second displayed. */
    public boolean wantsToShowFPS() {
        return myWantsToShowFPS;
    }

    /** Returns true if the model would like to continue executing (game is
      * not dead and over.)
      */
    public boolean wantsToKeepRunning() {
        return myShouldKeepRunning;
    }

    /** Returns a reference to this model's current level. */
    public Level getCurrentLevel() {
        return myCurrentLevel;
    }

    /** Returns the current level. */
    public int getLevelNumber() {
        return myLevelNumber;
    }

    /** Returns the name of the current level; can be used to load a background image for a level.
      * ( e.g. if the current level is "level2", load level2.gif )
      */
    public String getLevelName() { return myCurrentLevel.getName(); }

    public int getNumGhosts() {
        int count = 0;

        for (MovingSprite mspr : myMovingSprites) {
            if (mspr instanceof Ghost)
                count++;
        }

        return count;
    }

    /** Returns the number of coins/credits remaining. */
    public int getNumCredits() {
        return myNumCredits;
    }

    /** Returns the number of lives remaining. */
    public int getNumLives() {
        return myNumLives;
    }

    /** Returns number of players. */
    public int getNumPlayers() {
        // no multiplayer support yet!
        return 1;
    }

    /** Returns the number of times this model has updated itself since it went
      * into game over state.
      */
    public long getNumUpdatesSinceGameOver() {
        return myGameOverTime;
    }

    /** Returns the Vector of moving Sprites for the game, including Pac-Man
      * and all Ghosts.
      */
    public List<MovingSprite> getMovingSprites() {
        return myMovingSprites;
    }

    /** Returns a reference to this game's Pac-Man. */
    public PacMan getPacMan() {
        for (MovingSprite mspr : myMovingSprites) {
            if (mspr.type == Sprite.PACMAN)
                return (PacMan)mspr;
        }

        return null;
    }

    /** Returns the pac-man sprite nearest to this sprite. */
    private PacMan getNearestPacManTo(Sprite spr) {
        return getPacMan();
    }

    /** Returns the number of the current player. */
    public int getPlayerNumber() {
        // *** for now; no multi-player support
        return 1;
    }

    /** Returns the current score. */
    public int getScore(int num) {
        // *** for now; no multi-player support
        return myScore;
    }

    /** Returns the current highest number of points. */
    public int getHighScore() {
        if (myHighScores.isEmpty())
            return myScore;
        else
            return Math.max(myScore, myHighScores.getHighScore());
    }

    /** Returns this model's high score list. */
    public HighScoreList getHighScoreList() {
        return myHighScores;
    }

    /** Returns this game's current status. */
    public Object getState() {
        return myState;
    }

    /** Returns what type of pac-man game this is. */
    public Object getType() {
        return myType;
    }

    /** Returns how many times this model has updated. */
    public long getUpdateCounter() {
        return myUpdateCounter;
    }

    /** Returns whether this model currently wants sounds to be played.
      * This method differs from wantsSound() in that wantsSound will be
      * true even during a silent game-over period, while isDoingSound
      * will be false if the game should not play sound right now.
      */
    public boolean isDoingSound() {
        return isInProgress()  &&  myWantsSound;
    }

    /** Returns whether this model has sound enabled. */
    public boolean wantsSound() {
        return myWantsSound;
    }

    /** Returns whether or not the game should play the stupid "wee-oo" ghost sounds now. */
    public boolean isDoingGhostSounds() {
        PacMan pac = getPacMan();
        if (pac == null)
            return false;

        return !justStarted()  &&  pac.isAlive  &&  !pac.wasJustRevived();
    }

    /** Returns true if the game is not in progress. */
    public boolean isGameOver() {
        return myState == State.GAME_OVER;
    }

    public boolean isInProgress() {
        return myState == State.IN_PROGRESS;
    }

    /** Returns whether or not the game is currently paused. */
    public boolean isPaused() {
        return myState == State.PAUSED;
    }

    /** Returns whether or not this game has just started updating itself.
      * There are 4.324 seconds to play the game-starting wave file.
      */
    public boolean justStarted() {
        return isInProgress()  &&  getUpdateCounter() < GameModel.UPDATES_PER_SECOND * 2;
    }

    /** Returns whether or not Pac-Man should be drawn on the screen right now. */
    public boolean shouldDrawPacMan() {
        for (MovingSprite movspr : myMovingSprites) {
            if (movspr.type == Sprite.GHOST) {
                Ghost gh = (Ghost)movspr;
                if (gh.wasJustEaten())
                    return false;
            }
        }
        return true;
    }



    /** Inserts a coin or credit into the game. */
    public void insertCoin() {
        myNumCredits++;

        // 48, 128 PUSH START BUTTON orange
        // 66, 160 1 PLAYER ONLY   or   1 OR 2 PLAYERS
        // 8, 192 BONUS PAC-MAN FOR 10000 pts
        // 32, 224 @ 1980 MIDWAY MFG. CO.
        // - 3 rows (-24 on y)
        if (isGameOver()) {
            if (myNumCredits == 1) {
                // show "push start" screen
//              System.out.println("first credit; setting shit up");
                setGameOver();
                putWordOnMap("PUSH START BUTTON", 6, 13, PacManColors.ORANGE);
                putWordOnMap((myNumCredits >= 2)  ?  "1 OR 2 PLAYERS"  :  "1 PLAYER ONLY", 8, 17, PacManColors.CYAN);
                putWordOnMap("BONUS PAC-MAN FOR 10000 PTS", 1, 21, PacManColors.FLESH);
                putWordOnMap("@ 1980 MIDWAY MFG. CO.", 4, 25, PacManColors.PINK);
            } else if (myNumCredits == 2) {
                // change screen slightly for 2 coins inserted
                putWordOnMap((myNumCredits >= 2)  ?  "1 OR 2 PLAYERS"  :  "1 PLAYER ONLY", 8, 17, PacManColors.CYAN);
            }
        }
        
        System.runFinalization();
        System.gc();
        notifyListeners(null, Event.COIN_INSERTED);
    }

    /** Starts a new game.
      * @return 0 on success; -1 on not enough credits; -2 if game is in progress.
      */
    public int newGame(int numPlayers) {
        if (myNumCredits < numPlayers)
            return -1;  // throw new RuntimeException("Can't start a new game with " + numPlayers + " players!  Not enough credits.");
        else if (!isGameOver())
            return -2;  // throw new RuntimeException("Can't interrupt current game in progress!");

//      try {
//          Thread.sleep(500);
//      } catch (InterruptedException ie) {}
//
        myNumCredits -= numPlayers;
        myState = State.GAME_OVER;
        myGameOverTime = 0;
        myLevelNumber = 0;
        myScore = 0;
        myNumLives = 3;
//        mySavedMoves = null;

        gotoRandomValidLevel();
        myState = State.IN_PROGRESS;
        notifyListeners(null, Event.NEW_GAME);
        return 0;
    }

    /** Called to end the current game. */
    public synchronized void endGame() {
        // myState = State.GAME_OVER;
        notifyListeners(null, Event.GAME_OVER);
        setGameOver();
    }

    /** Sets this game to game over state, making it loop through its animations
      * and demos.
      */
    private void setGameOver() {
//      System.out.println("setGameOver()");
        myState = State.GAME_OVER;

        // new game code--sleep a bit to make sure we're not in an update
//      try {
//          Thread.sleep(2000);
//      } catch (InterruptedException ie) {}

        myGameOverTime = 0;
        myUpdateCounter = 0;
        myLevelNumber = 0;
        myScore = 0;
        myNumLives = 3;
//        mySavedMoves = null;
//        myIsRecording = false;
        myGhostScorePower = 0;
        myUpdateCounter = 0;

        setCurrentLevel(Level.EMPTY_LEVEL);
        myPelletTime = Math.max(MIN_PELLET_TIME, INITIAL_PELLET_TIME - myLevelNumber);

        // next level code
        //myMovingSprites.removeAllElements();
        //myDesiredDir = KeyEvent.VK_LEFT;
    }

    /** Adds the given level to this game's list of levels. */
    public void addLevel(String fileName) {
        try {
            InputStream is = new FileInputStream(fileName);
            Level lev = Level.generateLevel(is);
            myLevels.add(lev);
        } catch (IOException ioe) {
            throw new RuntimeException("Can't add this level: I/O error\n" + ioe);
        }
    }
    public void addLevel(InputStream is, Type type) {
        Level lev = Level.generateLevel(is, type);
        myLevels.add(lev);
    }

    /** Reads a demo for this game type (Pac-Man, Mrs. Pac-Man, etc). */
    private void gotoRandomValidLevel() {
        if (myLevels == null)
            return;

        int size = myLevels.size();
        if (size == 0)
            return;

        int count = 0;

        // make sure there is at least 1 demo of this type
        for (Level lev : myLevels) {
            if (lev.getType() == myType)
                count++;
        }
        if (count == 0)
            // no demos of this game type available!
            return;

        // pick a random demo of this game type
        int index = -1;
        while (true) {
            index = (int)(Math.random() * size);
            Level lev = myLevels.get(index);
            if (lev.getType() == myType)
                break;
        }

        setCurrentLevel(myLevels.get(index));
        myPelletTime = Math.max(MIN_PELLET_TIME, INITIAL_PELLET_TIME - myLevelNumber);
        myLevelNumber++;
        notifyListeners(null, Event.NEW_LEVEL);
    }

    /** Starts a new level. */
    private void setCurrentLevel(Level level) {  // int index) {
//      new RuntimeException().printStackTrace();
//      System.out.println("setCurrentLevel(): changing level: before=");
//      System.out.println(myCurrentLevel);

//      Object oldState = myState;
//      myState = State.GAME_LOADING;

        // clean up old ghosts
        for (MovingSprite mspr : myMovingSprites) {
            if (mspr.type == Sprite.GHOST) {
                ((Ghost)mspr).cleanUp();
            }
        }
        myMovingSprites.clear();
        myDesiredDir = KeyEvent.VK_LEFT;
        myGhostScorePower = 0;
        myUpdateCounter = 0;
        myShouldShowPlayerNumber = true;

        // go to the given level now
        myCurrentLevel = level;
        myCurrentLevel.regenerate();

        // read level text data to see positions of moving sprites
        readMovingSprites(myCurrentLevel);

//      readMap((Level)myLevels.elementAt(levelNum));
//      myState = oldState;

//      System.out.println("changing level: after=");
//      System.out.println(myCurrentLevel);
//      new RuntimeException().printStackTrace();

        notifyListeners(null, Event.NEW_LEVEL);
    }

    /** Reads the locations and constructs the moving sprites contained in the given level. */
    private void readMovingSprites(Level level) {
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(level.getStream()));
            while (input.ready()) {
                String line = input.readLine();
                if (line != null  &&  !line.equals(""))
                    lines.add(line);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error regenerating level: " + ioe);
        }

        if (lines.size() == 0)
            return;

        int w = myCurrentLevel.getGridWidth();
        int h = myCurrentLevel.getGridHeight();
        int SPRITE_OFFSET = Level.GRID_SIZE / 2;
        char curr = '\0';
        int gridx = 0;
        int gridy = 0;
        for (int y = 0;  y < h;  y++) {
            String line = lines.get(y);
            for (int x = 0;  x < w;  x++) {
                curr = line.charAt(x);
                gridx = x * Level.GRID_SIZE;
                gridy = y * Level.GRID_SIZE;

                if (Character.isDigit(curr)) {
                    // Pac-Man himself!
                    gridx += SPRITE_OFFSET;
                    myMovingSprites.add(0, new PacMan(gridx, gridy));
                }
                else if (curr == 'B'  ||  curr == 'I'  ||  curr == 'P'  ||  curr == 'C') {
                    // a Ghost
                    Ghost gh = null;
                    switch (curr) {
                    case 'B':
                        gh = Ghost.createBlinky(gridx, gridy);
                        break;
                    case 'I':
                        gh = Ghost.createInky(gridx, gridy);
                        break;
                    case 'P':
                        gh = Ghost.createPinky(gridx, gridy);
                        break;
                    case 'C':
                        gh = Ghost.createClyde(gridx, gridy);
                        break;
                    default:
                        gh = Ghost.createSatan(gridx, gridy);
                        break;
                    }

                    myMovingSprites.add(gh);
                    int numGhosts = getNumGhosts();

                    // make ghosts chill out in their cage for a while before they come out;
                    // first ghost (Blinky) needs delay of 1, since he starts outside--
                    // also third ghost (Pinky) comes out of hive instantly
                    int delay = 0;
                    if (numGhosts % 2 == 0) {
                    // give every other ghost a delay
                        delay = 1;
                        delay = UPDATES_PER_SECOND * numGhosts * TIME_BETWEEN_GHOSTS / 2;
                    } else if (numGhosts > 1) {
                    // I'm setting it so that even ghosts who leave the cage right away
                    // briefly use their cage strategy in order to figure out how to get out
                    // of the cage correctly
                        delay = 1;
                    }

                    if (delay > 0)
                        gh.setCageDelay(delay);
                }
                else if (curr == 'F') {
                    // Fruit
                    gridx += SPRITE_OFFSET;
                    Fruit fr = null;

                    if (myLevelNumber % 5 == 0)
                        fr = new Cherry(gridx, gridy);
                    else if (myLevelNumber % 5 == 1)
                        fr = new Strawberry(gridx, gridy);
                    else if (myLevelNumber % 5 == 2  ||  myLevelNumber % 5 == 3)
                        fr = new Orange(gridx, gridy);
                    else if (myLevelNumber % 5 == 4)
                        fr = new Apple(gridx, gridy);
                    else
                        throw new RuntimeException("Why is level number this?  " + myLevelNumber);

                    fr.setVisible(false);

                    // moving fruit in mrs. pac-man game
                    if (myType != Type.PACMAN) {
                        fr.setSpeed(Fruit.FRUIT_SPEED);
                        Strategy strat = new TurnStrategy(fr);
                        fr.setStrategy(strat);
                    }
                    myMovingSprites.add(fr);
                }

            }
        }

        // make sure this level was valid
        int numGhosts = getNumGhosts();
        if (getNumPlayers() == 0  ||  numGhosts == 0)
            throw new RuntimeException("this level is missing some needed sprites!");

        // Since some ghosts start outside the cage, I need to adjust their respawn positions
        // to make all ghosts return to a place inside the cage.
        // I'll use Pinky's respawn position, since he's in the cage center (kludgey)
        Ghost ghostToUse = null;
        for (MovingSprite mspr : myMovingSprites) {
            if (mspr.type == Sprite.GHOST) {
                ghostToUse = (Ghost)mspr;
                if (ghostToUse.getName().equalsIgnoreCase("Pinky"))
                    break;
            }
        }

        int rx = ghostToUse.getInitialX();
        int ry = ghostToUse.getInitialY();

        // if there's not enough ghosts
        if (ghostToUse.getName().equalsIgnoreCase("Blinky"))
            ry += 3 * Level.GRID_SIZE;

        // offset ghosts a bit, so they'll look like they're in the right place as per arcade game
        for (MovingSprite mspr : myMovingSprites) {
            if (mspr.type == Sprite.GHOST) {
                Ghost gh = (Ghost)mspr;

                Sprite rightcell = myCurrentLevel.getGridCell(gh.getGridX() + 1, gh.getGridY());
                Sprite downcell  = myCurrentLevel.getGridCell(gh.getGridX(), gh.getGridY() + 1);
                if (!(rightcell instanceof ObstacleSprite)) {
                    gh.setX(gh.getX() + SPRITE_OFFSET);
                    gh.setInitialX(gh.getInitialX() + SPRITE_OFFSET);
                }
                if (!(downcell instanceof ObstacleSprite)) {
                    gh.setY(gh.getY() + SPRITE_OFFSET);
                    gh.setInitialY(gh.getInitialY() + SPRITE_OFFSET);
                }

                gh.setRespawnPosition(rx, ry);
            }
        }
        
        // removed because it does some whack shit
        // setGhostDifficulties();
    }
    
    /* Sets how fast the ghosts move. */
    private void setGhostDifficulties() {
        // *** THIS CAN CAUSE WEIRD PROBLEMS--PERHAPS BEST TO DISABLE IT
        // set the ghosts to get slightly faster each level
        for (MovingSprite mspr : myMovingSprites) {
            if (mspr.type == Sprite.GHOST) {
                Ghost gh = (Ghost)mspr;
                gh.setDifficultyLevel(Math.min(myDifficultyMultiplier*myLevelNumber + myDifficultyStart, 30));
            }
        }
    }

    /** Adds the given amount to the game's score. */
    public void addToScore(int n) {
        int oldScore = myScore % EXTRA_LIFE_POINTS;
        myScore += n;
        oldScore += n;
        if (oldScore > EXTRA_LIFE_POINTS) {
            myNumLives++;
            notifyListeners(null, Event.EXTRA_LIFE);
        }
    }

    /** Sets whether this model should play sounds. */
    public void setSound(boolean b) {
        myWantsSound = b;
    }
    
    /** Sets game's difficulty level. */
    public void setDifficultyLevel(int n) {
        myDifficultyMultiplier = 5*n;
        myDifficultyStart = 5*n;
        
        setGhostDifficulties();
    }

    /** Kills the given Ghost, turning it into eyes and making it run
      * back to its generator to be revived.
      */
    private void killGhost(Ghost gh) {
        gh.setStatus(Sprite.STATUS_EATEN);
        myGhostScorePower = Math.min(myGhostScorePower + 1, 4);
        addToScore(gh.getScore() * (int)Math.pow(2, myGhostScorePower));
        notifyListeners(gh, Event.GHOST_EATEN);
    }

    /** Called about 1 second after a ghost Kills Pac-Man.
      * Makes the game reset a few things, like pac-man's movement direction
      * and also making the ghosts disappear.
      */
    private void killPlayer() {
        Fruit fr = getFruit();
        if (fr != null)
            fr.resetTimer();

        // turn pac-man back to the left
        setDesiredDir(KeyEvent.VK_LEFT);

        // make the ghosts disappear
        for (MovingSprite mspr : myMovingSprites) {
            if (mspr.type == Sprite.GHOST) {
                Ghost gh = (Ghost)mspr;
                gh.setStatus(Sprite.STATUS_NORMAL);
                gh.setVisible(false);
            }
        }

        notifyListeners(null, Event.PLAYER_DEATH);
    }

    /** Revives Pac-Man about 4 seconds after he has been killed by a ghost.
      * @return whether or not Pac-Man was actually revived, which will
      * be false if no lives were remaining.
      */
    private boolean revivePlayer() {
        if (myNumLives == 0)
            // no lives left; can't revive!
            return false;

        Fruit fr = getFruit();
        if (fr != null)
            fr.resetTimer();

        myNumLives--;

        // reset the sprites
        for (MovingSprite mspr : myMovingSprites) {
            if (mspr.type == Sprite.PACMAN) {
                PacMan pac = (PacMan)mspr;
                pac.revive();
            } else if (mspr.type == Sprite.GHOST) {
                Ghost gh = (Ghost)mspr;
                gh.returnToStart();
            }
        }

        return true;
    }

    /** Sets the game's delay in milliseconds between updates to
      * be the given number.
      */
    public void setDelay(int n) {}

    /** Sets Pac-Man's desired direction of movement to the given integer. */
    public void setDesiredDir(int dir) {
        myDesiredDir = dir;
    }

    /** Sets this model's frames-per-second to the given amount.
      * This isn't an instruction on how many FPS are desired, but rather
      * a reading of how many frames were drawn in the last second.
      *
      * (It is used to display said information in the view so the
      * programmer can see how fast the game is running.)
      */
    public void setFPS(int fps) {
        myFPS = fps;
    }

    public void setWantsToShowFPS(boolean b) {
        myWantsToShowFPS = b;
    }

    /** Runs the game in its own Thread. */
    public void start() {
//      myTimer.start();
    }

    /** Tells model the program is shutting down. */
    public void stop() {
        myShouldKeepRunning = false;
        myState = State.GAME_OVER;
    }

    /** Called to end the current game without all the high score BS. */
    public void terminateGame() {
        myState = State.GAME_OVER;
    }

    /** Removes folder name and extension information from the given file name. */
/*
    public static String trimFileName(String fileName) {
        int startIndex = Math.max(0, fileName.lastIndexOf("/") + 1);  // System.getProperty("file.separator")

        int endIndex = fileName.indexOf(".");
        if (endIndex == -1)
            endIndex = fileName.length();

        String levelName = fileName.substring(startIndex, endIndex);  // trim extension

        return levelName;
    }
*/

    /** Sets the game to be paused or not, without triggering a game event. */
    public void setPaused(boolean b) {
        if (b) pause();
        else   unpause();
    }

    /** Pauses the game.
      * @return whether or not the game was successfully paused.
      */
    public boolean pause() {
        if (myState == State.IN_PROGRESS) {
            myState = State.PAUSED;
            notifyListeners(null, Event.GAME_PAUSED);
            return true;
        } else return false;
    }

    /** Unpauses the game.
      * @return whether or not the game was successfully unpaused.
      */
    public boolean unpause() {
        if (isPaused()) {
            myState = State.IN_PROGRESS;
            notifyListeners(null, Event.GAME_UNPAUSED);
            return true;
        } else return false;
    }

    /** Places the given word on this model's current map, one letter per grid square. */
    private void putWordOnMap(String word, int x, int y, Color color) {
        // myCurrentLevel.setGridCell(x, y, new Letter(word, x * Level.GRID_SIZE, y * Level.GRID_SIZE, color));
        for (int i = 0;  i < word.length();  i++)
            myCurrentLevel.setGridCell(x+i, y, new Letter("" + word.charAt(i), (x+i) * Level.GRID_SIZE, y * Level.GRID_SIZE, color));
    }


    /** Updates the game state of all sprites.  Called UPDATES_PER_SECOND times per second
      * to make the game run at a fast, smooth pace.
      */
    public synchronized void update() {
        if (isGameOver()  &&  myGameOverTime <= DEMO_LENGTH) {
            if (myNumCredits == 0) {
                // do demo stuff
                updateGameOver();
            }
            return;
        }

        if (myCurrentLevel == null)
            return;

        if (MOVE_DEBUG)  System.out.println("\nUPDATE\n");

        // synchronize on this model so no one can mess with me while I am updating myself
        events.clear();

        boolean doingMovement = isDoingMovement();
        boolean demoDone = false;

        PacMan currentPlayer = getPacMan();
        if (currentPlayer != null  &&  !currentPlayer.isAlive) {
            // one-second pause after death, then try to revive;
            // if I can't revive him (no lives left), end game
            currentPlayer.update(this);
            int count = currentPlayer.getNumUpdatesSinceKilled();
            if (!justStarted()  &&  count == UPDATES_PER_SECOND) {
                killPlayer();
            } else if (count == UPDATES_PER_SECOND * 3) {
                if (!revivePlayer())
                    endGame();
            }
        } else {
            for (MovingSprite mspr : myMovingSprites) {
                mspr.update(this);

                if (mspr.type == Sprite.PACMAN) {
                    PacMan pac = (PacMan)mspr;
                    if (pac.isAlive) {
                        // living pac-man's movement
                        if (doingMovement) {
                            Move desiredMove = getDesiredMove();
                            if (pac.hasQueuedMoves()) {
                                desiredMove = pac.getDesiredMove();
                                if (!pac.hasQueuedMoves())
                                    demoDone = true;
                            }
                            tryMove(pac, desiredMove);

                            if (myCurrentLevel.collidesWithWall(pac)) {
                                debugDump();
                                System.out.println("Pac-Man in wall--this should not happen");
                            }
                        }

                        // check if pac-man ate a dot/pellet
                        if (myShouldDoEdibleCollisions)
                            myCurrentLevel.checkDotsEaten(this, pac);
                    }
                } else if (mspr.type == Sprite.GHOST) {
                    Ghost gh = (Ghost)mspr;
                    PacMan pac = getNearestPacManTo(gh);
                    if (pac == null)
                        continue;

                    // make the ghosts move
                    if (doingMovement) {  //   &&  pac.isAlive
                        Move desiredMove = null;
                        if (gh.hasQueuedMoves())
                            desiredMove = gh.getDesiredMove();
                        else 
                            desiredMove = gh.calculateMove(myCurrentLevel, pac);
                        tryMove(gh, desiredMove);
                    }

                    // debugging test for a stuck ghost
                    if (myCurrentLevel.collidesWithWall(gh))
                        System.out.println(gh.dump() + " stuck in wall at (" + gh.getGridX() + "," + gh.getGridY() + ")");

                    // check if we need to revive any ghosts that have come
                    // back to the generator as eyeballs
                    if (gh.isAtRespawnPosition()  &&  gh.isEaten()) {
                        gh.revive();
                        events.add(new GameEvent(gh, Event.GHOST_REVIVED));
                    }

                    // do collision detection between pac-man and ghosts
                    if (myShouldDoGhostCollisions  &&  !gh.isEaten()
                            &&  pac.isAlive  &&  pac.collidesWith(gh)) {
                        if (!gh.isScared()) {
                            // set all pac-men to be dead, tell all ghosts;
                            // set wheels in motion for death animation
                            for (MovingSprite movspr : myMovingSprites) {
                                if (movspr.type == Sprite.PACMAN)
                                    ((PacMan)movspr).kill();
                                else if (movspr.type == Sprite.GHOST)
                                    ((Ghost)movspr).notifyOfPacManDeath();
                            }
                        }
                        else
                            killGhost(gh);
                    }
                } else if (mspr instanceof Fruit) {
                    Fruit fr = (Fruit)mspr;
                    PacMan pac = getNearestPacManTo(fr);
                    if (pac == null)
                        continue;

                    // make fruit move (if it should move, that is)
                    tryMove(fr, fr.calculateMove(myCurrentLevel, pac));

                    // do collision detection between pac-man and fruit
                    if (!fr.isEaten()  &&  fr.isVisible  &&  pac.isAlive  &&  pac.collidesWith(fr)) {
                        addToScore(fr.getScore());
                        fr.setStatus(Sprite.STATUS_EATEN);
                        events.add(new GameEvent(fr, Event.FRUIT_EATEN));
                    }
                }

                // record game moves
//                if (myIsRecording  &&  doingMovement)
//                    mySavedMoves[i] += mspr.getLastMove() + "\t";
            }

            // make power pellets blink every half second
            if (doingMovement  &&  getUpdateCounter() % (UPDATES_PER_SECOND / 4) == 0) {
                getCurrentLevel().blinkPellets();
                blinkPlayerNumber();
            }

            // "revive" pac-man to start the game, so it'll say "ready" on the screen
            boolean wasJustStarted = justStarted();
            myUpdateCounter++;
            if (wasJustStarted  &&  !justStarted()) {
                if (!revivePlayer())
                    endGame();
            }

            if (isInProgress()  &&  myCurrentLevel.isCleared()) {
//              System.out.println("update(): Level clear!  loading next level.");
                gotoRandomValidLevel();
            }

            if (demoDone) {
                // put back into game-over loop
                setGameOver();
            }
        }

        for (GameEvent event : events) {
            notifyListeners(event.source, event.type);
        }
    }

    /** Updates the game state of all sprites when the game is in game over state.
      * Called UPDATES_PER_SECOND times per second to make the game run at a fast, smooth pace.
      */
    private void updateGameOver() {
        myScore = 0;
        Ghost gh = null;
        PacMan pac = null;

        for (MovingSprite mspr : myMovingSprites) {
            mspr.update(this);
        }

        if (myGameOverTime == 0) {
            // for fast demo testing
            if (DEMO_DEBUG)
                myGameOverTime = DEMO_LENGTH;
            else {
                setGameOver();
                putWordOnMap("CHARACTER / NICKNAME", 7, 2, PacManColors.WHITE);
            }
        }
        else if (myGameOverTime == UPDATES_PER_SECOND) {
            // after 1 second, make red ghost
            gh = Ghost.createBlinky(4 * Level.GRID_SIZE, 4 * Level.GRID_SIZE);
            gh.setdX(1);
            gh.clearAllStrategies();
            gh.zombify();
            myMovingSprites.add(gh);
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 2) {
            // show red ghost's name
            gh = (Ghost)myMovingSprites.get(myMovingSprites.size() - 1);
            putWordOnMap("-SHADOW", 7, 4, gh.getColor());
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 5 / 2) {
            // show red ghost's nickname
            gh = (Ghost)myMovingSprites.get(myMovingSprites.size() - 1);
            putWordOnMap("\"BLINKY\"", 18, 4, gh.getColor());
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 3) {
            // make pink ghost
            gh = Ghost.createPinky(4 * Level.GRID_SIZE, 7 * Level.GRID_SIZE);
            gh.setdX(1);
            gh.clearAllStrategies();
            gh.zombify();
            myMovingSprites.add(gh);
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 4) {
            // show pink ghost's name
            gh = (Ghost)myMovingSprites.get(myMovingSprites.size() - 1);
            putWordOnMap("-SPEEDY", 7, 7, gh.getColor());
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 9 / 2) {
            // show pink ghost's nickname
            gh = (Ghost)myMovingSprites.get(myMovingSprites.size() - 1);
            putWordOnMap("\"PINKY\"", 18, 7, gh.getColor());
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 5) {
            // make blue ghost
            gh = Ghost.createInky(4 * Level.GRID_SIZE, 10 * Level.GRID_SIZE);
            gh.setdX(1);
            gh.clearAllStrategies();
            gh.zombify();
            myMovingSprites.add(gh);
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 6) {
            // show blue ghost's name
            gh = (Ghost)myMovingSprites.get(myMovingSprites.size() - 1);
            putWordOnMap("-BASHFUL", 7, 10, gh.getColor());
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 13 / 2) {
            // show blue ghost's nickname
            gh = (Ghost)myMovingSprites.get(myMovingSprites.size() - 1);
            putWordOnMap("\"INKY\"", 18, 10, gh.getColor());
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 7) {
            // make orange ghost
            gh = Ghost.createClyde(4 * Level.GRID_SIZE, 13 * Level.GRID_SIZE);
            gh.setdX(1);
            gh.clearAllStrategies();
            gh.zombify();
            myMovingSprites.add(gh);
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 8) {
            // show orange ghost's name
            gh = (Ghost)myMovingSprites.get(myMovingSprites.size() - 1);
            putWordOnMap("-POKEY", 7, 13, gh.getColor());
        }
        else if (myGameOverTime == UPDATES_PER_SECOND * 17 / 2) {
            // show orange ghost's nickname
            gh = (Ghost)myMovingSprites.get(myMovingSprites.size() - 1);
            putWordOnMap("\"CLYDE\"", 18, 13, gh.getColor());
        }

        else if (myGameOverTime == UPDATES_PER_SECOND * 9) {
            // show dot/pellet and their point value
            myCurrentLevel.setGridCell(10, 21, new Dot(10 * Level.GRID_SIZE + (Level.GRID_SIZE - Dot.DOT_SIZE) / 2,
                    21 * Level.GRID_SIZE + (Level.GRID_SIZE - Dot.DOT_SIZE) / 2));
            putWordOnMap("10 PTS", 12, 21, PacManColors.WHITE);

            myCurrentLevel.setGridCell(10, 23, new PowerPellet(10 * Level.GRID_SIZE, 23 * Level.GRID_SIZE));
            putWordOnMap("50 PTS", 12, 23, PacManColors.WHITE);
        }

        else if (myGameOverTime == UPDATES_PER_SECOND * 11) {
            // start brief animation with pacman and ghosts
            // to show how eating is done and ghost point values
            putWordOnMap("@ 1980 MIDWAY MFG. CO.", 4, 28, PacManColors.PINK);

            myCurrentLevel.setGridCell(4, 17, new PowerPellet(4 * Level.GRID_SIZE, 17 * Level.GRID_SIZE));
            pac = new PacMan(27 * Level.GRID_SIZE, 17 * Level.GRID_SIZE);
            pac.killCounters();  // stops "READY!" and other weirdness
            myMovingSprites.add(pac);

            // make pac-man move a lot
            MoveList list = new MoveList();
            list.addMoves(Move.LEFT.times(PacMan.PAC_SPEED), 23 * Level.GRID_SIZE / PacMan.PAC_SPEED);
            list.addMoves(Move.RIGHT.times(PacMan.PAC_SPEED), 15 * Level.GRID_SIZE / PacMan.PAC_SPEED);
            list.addMove(Move.NEUTRAL);
            pac.queueMoves(list);

            // make dumb ghosts for pac-man to eat
            gh = Ghost.createBlinky(29 * Level.GRID_SIZE, 17 * Level.GRID_SIZE);
            gh.clearAllStrategies();
            myMovingSprites.add(gh);
            list = new MoveList();
            list.addMoves(Move.LEFT.times(PacMan.PAC_SPEED), 23 * Level.GRID_SIZE / PacMan.PAC_SPEED);
            list.addMoves(Move.RIGHT, 23 * Level.GRID_SIZE);
            gh.queueMoves(list);

            gh = Ghost.createPinky(31 * Level.GRID_SIZE, 17 * Level.GRID_SIZE);
            gh.clearAllStrategies();
            myMovingSprites.add(gh);
            list = new MoveList();
            list.addMoves(Move.LEFT.times(PacMan.PAC_SPEED), 23 * Level.GRID_SIZE / PacMan.PAC_SPEED);
            list.addMoves(Move.RIGHT, 23 * Level.GRID_SIZE);
            gh.queueMoves(list);

            gh = Ghost.createInky(33 * Level.GRID_SIZE, 17 * Level.GRID_SIZE);
            gh.clearAllStrategies();
            myMovingSprites.add(gh);
            list = new MoveList();
            list.addMoves(Move.LEFT.times(PacMan.PAC_SPEED), 23 * Level.GRID_SIZE / PacMan.PAC_SPEED);
            list.addMoves(Move.RIGHT, 23 * Level.GRID_SIZE);
            gh.queueMoves(list);

            gh = Ghost.createClyde(35 * Level.GRID_SIZE, 17 * Level.GRID_SIZE);
            gh.clearAllStrategies();
            myMovingSprites.add(gh);
            list = new MoveList();
            list.addMoves(Move.LEFT.times(PacMan.PAC_SPEED), 23 * Level.GRID_SIZE / PacMan.PAC_SPEED);
            list.addMoves(Move.RIGHT, 23 * Level.GRID_SIZE);
            gh.queueMoves(list);
        }

        else if (myGameOverTime > UPDATES_PER_SECOND * 11
                &&  myGameOverTime < DEMO_LENGTH) {
            // do brief animation described above
            pac = getPacMan();

            if (isDoingMovement()) {
                pac.go(pac.getDesiredMove());
                myCurrentLevel.checkDotsEaten(this, pac);

                // make the stationary ghosts stand still
                int count = 0;
                int size = myMovingSprites.size();
                for (int j = 0;  j < size;  j++) {
                    MovingSprite mspr = (MovingSprite)myMovingSprites.get(j);
                    if (mspr.type == Sprite.GHOST) {
                        gh = (Ghost)mspr;
                        if (count < 4) {
                            // stationary ghost
                            gh.killCounters();
                        } else {
                            // moving ghost
                            if (gh.isEaten()  &&  !gh.wasJustEaten()) {
                                myMovingSprites.remove(j);
                                size--;  j--;
                                continue;
                            }
                            gh.go(gh.getDesiredMove());
                            if (!gh.isEaten()  &&  gh.isScared()  &&  pac.collidesWith(gh))
                                killGhost(gh);
                        }

                        count++;
                    }
                }
            }

            // make power pellets blink every half second
            if (myUpdateCounter % (UPDATES_PER_SECOND / 4) == 0)
                myCurrentLevel.blinkPellets();
        }

        if (myGameOverTime == DEMO_LENGTH) {
            // show a random demo game
            myMovingSprites.clear();

            if (!myDemos.isEmpty()) {
                readRandomValidDemo();
            }
        }

        myScore = 0;
        myGameOverTime++;
        myUpdateCounter++;
    }

// DEMO GAME RELATED METHODS
    /** Adds a new demo game to this model from the given input stream. */
    public void addDemo(InputStream is, String demoName, Object type) {
        myDemos.add(new Demo(is, demoName, type));
    }

    /** Reads a demo for this game type (Pac-Man, Mrs. Pac-Man, etc). */
    private void readRandomValidDemo() {
        if (myDemos == null)
            return;

        int size = myDemos.size();
        if (size == 0)
            return;

        int count = 0;

        // make sure there is at least 1 demo of this type
        for (Demo dem : myDemos) {
            if (dem.getType() == myType)
                count++;
        }
        if (count == 0)
            // no demos of this game type available!
            return;

        // pick a random demo of this game type
        Demo dem = null;
        while (true) {
            int index = (int)(Math.random() * size);
            dem = myDemos.get(index);
            if (dem.getType() == myType)
                break;
        }

        readDemo(dem.getStream());

        // make ghosts zombies
//      for (int i = 0;  i < myMovingSprites.size();  i++) {
//          MovingSprite mspr = (MovingSprite)myMovingSprites.elementAt(i);
//          if (mspr instanceof Ghost) {
//              ((Ghost)mspr).clearAllStrategies();
//          }
//          mspr.stop();
//      }
    }

    /** Reads the demo at the given input stream and sets up its moves to be made
      * by the game's sprites.
      */
    public synchronized void readDemo(InputStream in) {
        try {
            String levelName = "";
            BufferedReader input = new BufferedReader(new InputStreamReader(in));

            if (input.ready()) {
                levelName = input.readLine();
            }

            // find what level was referred to
            Level level = null;
            int index = 0;
            for (Level lev : myLevels) {
                if (lev.getName().equals(levelName)) {
                    level = lev;
                    break;
                }
                index++;
            }

            if (level == null)
                throw new RuntimeException("invalid demo: I can't find level named " + levelName);

            setCurrentLevel(level);
            myLevelNumber = index;
            putWordOnMap("GAME  OVER", 9, 17, PacManColors.RED);

            // read in moves for each sprite
            int i = 0;
            int overallMoveCount = -1;

            while (input.ready()) {
                int thisMoveCount = 0;
                String line = input.readLine().trim();
                if (line.equals(""))
                    continue;
                StringTokenizer st = new StringTokenizer(line, "\t:\f\r\n");

                // wipe out sprite name
                st.nextToken();

                MovingSprite mspr = (MovingSprite)myMovingSprites.get(i);
                while (st.hasMoreTokens()) {
                    Move mov = Move.parseMove(st.nextToken());
                    int numTimes = Integer.parseInt(st.nextToken());
                    mspr.queueMove(mov, numTimes);
                    thisMoveCount += numTimes;
                }

                i++;
                if (overallMoveCount == -1)
                    overallMoveCount = thisMoveCount;
                else if (overallMoveCount != thisMoveCount)
                    throw new RuntimeException("Invalid demo file!  Sprite " + mspr + " has " + thisMoveCount + " instead of " + overallMoveCount + " moves.");
                else
                    ;  // System.err.println("Good!  Sprite " + mspr + " indeed has " + thisMoveCount + " moves.");
            }
        } catch (IOException ioe) {
            System.err.println("Invalid demo file!  IO ERROR: " + ioe);
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid demo file!  BAD NUMBER: " + nfe);
        } catch (NoSuchElementException nsee) {
            System.err.println("Invalid demo file!  BAD TOKENS: " + nsee);
        }

        notifyListeners(null, Event.NEW_LEVEL);
    }

    /** Returns true if the game is playing a demo. */
//  public boolean isDemoPlaying() {
//      return myState == State.DEMO_PLAYING;
//  }

//    /** Starts recording a demo. */
//    public void startRecording() {
//        myIsRecording = true;
//        mySavedMoves = new String[myMovingSprites.size()];
//        for (int i = 0;  i < mySavedMoves.length;  i++) {
//            mySavedMoves[i] = "";
//        }
//    }
//
//    /** Stops recording a demo. */
//    public void stopRecording() {
//        myIsRecording = false;
//
//        if (mySavedMoves == null)
//            return;
//
//        // condense the saved moves list
//        boolean SHOULD_CONDENSE = true;
//        for (int i = 0;  i < mySavedMoves.length;  i++) {
//            String curr = null;
//            String prev = null;
//            int count = 0;
//            StringTokenizer st = new StringTokenizer(mySavedMoves[i], "\t:");
//            mySavedMoves[i] = ((MovingSprite)myMovingSprites.elementAt(i)).getName() + '\t';
//
//            while (st.hasMoreTokens()) {
//                curr = st.nextToken();
//
//                if (SHOULD_CONDENSE  &&  curr.equals(prev)) {
//                    count++;
//                } else {
//                    if (prev != null) {
//                        mySavedMoves[i] += prev + ':' + count + '\t';
//                    }
//                    prev = curr;
//                    count = 1;
//                }
//
//                // necessary to write out last move(s)
//                if (!st.hasMoreTokens()) {
//                    mySavedMoves[i] += prev + ':' + count + '\t';
//                }
//
//                /* PSEUDOCODE
//
//                for (each token)
//                    get token
//
//                    if this token is same as prev ones
//                        count += 1
//
//                    else (different token)
//                        if there were prev tokens
//                            write out (prev tokens, their count)
//                        prev token = token
//                        count = 1
//
//                    if no tokens ahead
//                        write out (token, count)
//
//                */
//            }
//        }
//    }
//
//    /** Returns true if this model is recording a demo. */
//    public boolean isRecording() {
//        return myIsRecording;
//    }
//
//    /** Returns the saved moves in this game's currently saved demo game. */
//    public String getSavedMoves() {
//        // String result = getLevelName() + '\n';
//        String result = myCurrentLevel.getName() + '\n';
//        for (int i = 0;  i < mySavedMoves.length;  i++) {
//            result += mySavedMoves[i];
//            if (i < mySavedMoves.length - 1)
//                result += '\n';
//        }
//
//        return result;
//    }

// MOVEMENT RELATED METHODS
    /** Attempts to move the given sprite in the given move.
      * If unable to do so, attempts to move sprite in its original move.
      * @return The Move made.
      */
    private Move tryMove(MovingSprite spr, Move desiredMove) {
        Move result = null;

        if (myCurrentLevel.canMove(spr, desiredMove)) {
            // can move in desired direction, so do so
            spr.go(desiredMove);
            spr.setVelocity(desiredMove);
            result = desiredMove;
        } else if (myCurrentLevel.canMove(spr)) {
            // can't move in desired direction, but can keep going straight, so do so
            spr.go(spr.getCurrentMove());
            result = spr.getLastMove();
        } else {
            // just plain can't move!
            spr.go(Move.NEUTRAL);
            result = Move.NEUTRAL;
        }

        // make sure I'm on the game board still
        spr.wrap(myCurrentLevel.getWidth(), myCurrentLevel.getHeight());
        return result;
    }

    /** Returns whether or not the game is having all sprites move right now. */
    public boolean isDoingMovement() {
        for (MovingSprite mspr : myMovingSprites) {
            if (mspr.type == Sprite.GHOST) {
                Ghost gh = (Ghost)mspr;
                if (gh.wasJustEaten())
                    return false;
            }
            else if (mspr.type == Sprite.PACMAN) {
                PacMan pac = (PacMan)mspr;
                if (!pac.isAlive  ||  pac.wasJustKilled()  ||  pac.wasJustRevived())
                    return false;
            }
        }

        // (isInProgress()  ||  isDemoPlaying())  &&
        return !isPaused()  &&  !justStarted();
    }

    /** Returns whether or not pac-man is doing his movement (mouth chomping). */
    public boolean shouldMovePacManMouth(PacMan pac) {
        // *** this aint quite right, there is only a 1st player's desired move thus far
        return myCurrentLevel != null  &&  isDoingMovement()  &&   myCurrentLevel.canMoveCurrentWayOrThisWay(pac, getDesiredMove());
    }

    private void blinkPlayerNumber() {
        myShouldShowPlayerNumber = !myShouldShowPlayerNumber;
    }

    public boolean shouldShowPlayerNumber(int number) {
        return getNumPlayers() >= number  &&  (number != getPlayerNumber()  ||  myShouldShowPlayerNumber);
    }

// COLLISION DETECTION
    /** Sets the given power pellet to be turned on or off,
      * making Pac-Man invincible.
      */
    public void chompPellet(PacMan pac, PowerPellet pellet) {
        pac.setInvincible(true);

        myGhostScorePower = 0;

        for (MovingSprite movspr : myMovingSprites) {
            if (movspr.type == Sprite.GHOST) {
                Ghost gh = (Ghost)movspr;
                if (!gh.isEaten()) {
//                  System.out.println("scaring ghost - sprite #" + j);
                    gh.scare(myPelletTime * UPDATES_PER_SECOND);
                }
            }
        }

        notifyListeners(pellet, Event.POWER_PELLET_EATEN);
    }

// DEBUG CODE
    /** Prints debug information about this model. */
    public void debugDump() {
        // debug dump

        // dump-of-the-day 2001-08-06: sprites
        System.out.println("sprites:");
        for (MovingSprite ms : getMovingSprites()) {
            System.out.println(ms.dump());
        }
        // Ghost stats
        System.out.println("status=" + myState + "; type=" + myType);
        System.out.println(myNumCredits + " credits; " + myNumLives + " lives; " + myScore + " points");
        System.out.println(myFPS + " fps; " + myGameOverTime + " gameovertime; " + myPelletTime + " pelletTime; " + myGhostScorePower + " ghostPower");
        System.out.println("sound? " + myWantsSound + "  ghost collisions? " + myShouldDoGhostCollisions + "  edible collisions? " + myShouldDoEdibleCollisions);
        System.out.println(Ghost.ourGhostCount + " total ghosts; are there angry ghosts? " + Ghost.areAngryGhosts());
        System.out.println(Ghost.ourScaredCount + " scared; are there scared ghosts? " + Ghost.areScaredGhosts());
        System.out.println(Ghost.ourEatenCount + " eaten; are there eaten ghosts? " + Ghost.areEatenGhosts() + "\n");
        System.out.println();
    }
}