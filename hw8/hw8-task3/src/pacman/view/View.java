package pacman.view;

import pacman.model.*;
import pacman.sprite.*;
import pacman.utility.*;

import java.applet.*;
import java.awt.*;
import java.util.*;

/** The superclass for all game views.  It is responsible for playing sounds
  * and updating its size accordingly as the game progresses.
  */
public abstract class View extends DoubleBufferedPanel implements Listener {
    protected GameModel myModel;
    protected ResourceFetcher myFetcher;
    protected Object myType;
    protected Image myBackground = null;
    protected Map<String, TimedAudioClip> myClipTable = new HashMap<String, TimedAudioClip>();

    /** Constructs a new view. */
    public View(GameModel gm, Component parent, ResourceFetcher fetcher) {
        this(gm, parent, fetcher, GameModel.Type.PACMAN);
    }

    /** Constructs a new view for the given game type. */
    public View(GameModel gm, Component parent, ResourceFetcher fetcher, Object type) {
        myModel = gm;
        myFetcher = fetcher;
        myType = type;
        setBackground(PacManColors.BLACK);
        setImageCreatingComponent(parent);
        updateSize();
        loadSounds("sounds");
    }

    /** Called when this view is notified of an event by its model. */
    public void gameUpdated(Listenable origin, Object source, Object eventType) {
        // turn on/off appropriate sounds
        doSounds(eventType);
        requestFocus();

        if (eventType == GameModel.Event.NEW_LEVEL
                ||  eventType == GameModel.Event.NEW_GAME) {
            // level changed; reset game board and dimensions
            myBackground = null;
            updateSize();
        }

        else if (eventType == GameModel.Event.GAME_UPDATED
                ||  eventType == GameModel.Event.GAME_PAUSED
                ||  eventType == GameModel.Event.GAME_UNPAUSED)
            // this.repaint(0, 0, getPreferredSize().width, getPreferredSize().height);
            this.repaint();
    }

    /** Called to notify this view that it is being removed as a listener from its model. */
    public void detach() {
        haltAllSounds();
    }

    /** Performs actions necessary when the game view changes size. */
    public abstract void updateSize();

    /** Draws text at some point on the screen. */
    protected abstract void drawWord(Graphics g, String word, String color, int gx, int gy);


    /** Creates a TimedAudioClip based upon the given URL, that plays for the
      * given duration of time in ms.
      */
    protected TimedAudioClip createTimedAudioClip(String fileName, long duration) {
        AudioClip clip = myFetcher.getAudioClip(fileName);
        return new TimedAudioClip(clip, duration);
    }

    /** Plays sounds at the appropriate times. */
    protected void doSounds(Object eventType) {
        // System.out.println("in progress? " + myModel.isInProgress());
        // System.out.println(Ghost.ourGhostCount + " total ghosts; are there angry ghosts? " + Ghost.areAngryGhosts());
        // System.out.println(Ghost.ourScaredCount + " scared; are there scared ghosts? " + Ghost.areScaredGhosts());
        // System.out.println(Ghost.ourEatenCount + " eaten; are there eaten ghosts? " + Ghost.areEatenGhosts() + "\n");

        PacMan pac = myModel.getPacMan();
        boolean pacJustKilled = pac != null  &&  pac.wasJustKilled();
        boolean ghostJustEaten = false;

        for (MovingSprite spr : myModel.getMovingSprites()) {
            if (spr.type == Sprite.GHOST) {
                Ghost gh = (Ghost)spr;
                if (gh.wasJustEaten()) {
                    ghostJustEaten = true;
                    break;
                }
            }
        }

        // stop incorrectly looping background noises
        if (getSound("pacchomp").isPlaying()  &&  ((pac != null  &&  !pac.isEating())  ||  pacJustKilled))
            getSound("pacchomp").stop();

        if (getSound("angry").isPlaying()  &&  (!Ghost.areAngryGhosts()  ||  !myModel.isDoingGhostSounds()  ||  pacJustKilled)) {
            getSound("angry").stop();
            getSound("angry1").stop();
            getSound("angry2").stop();
            getSound("angry3").stop();
        }

        if (getSound("pellet").isPlaying()  &&  (!Ghost.areScaredGhosts()  ||  pacJustKilled))
            getSound("pellet").stop();

        if (getSound("eyesrun").isPlaying()  &&  (!Ghost.areEatenGhosts()  ||  pacJustKilled))
            getSound("eyesrun").stop();


        if (eventType == GameModel.Event.GAME_OVER
        ||  eventType == GameModel.Event.GAME_PAUSED)
            haltAllSounds();
        else if (eventType == GameModel.Event.COIN_INSERTED)
            getSound("credit").play();

        // if (!myModel.isGameOver()) {
        else if (eventType == GameModel.Event.LEVEL_CLEARED) {
            haltAllSounds();
            setAngry(getSound("angry1"));
            pausedPlaySound("interm");
        }
        else if (eventType == GameModel.Event.NEW_GAME) {
            setAngry(getSound("angry1"));
            haltAllSounds();
            if (!GameModel.DEBUG)
                getSound("pacstart").play();
        }
        else if (eventType == GameModel.Event.NEW_LEVEL) {
            setAngry(getSound("angry1"));
            haltAllSounds();
        }
        else if (eventType == GameModel.Event.THREE_QUARTERS_DOTS_EATEN)
            setAngry(getSound("angry3"));

        if (myModel.isInProgress()) {
            // play looping background noises that should be played
            if (!myModel.justStarted()  &&  myModel.isDoingGhostSounds()) {
                if (myModel.getPacMan().isEating()  &&  !getSound("pacchomp").isPlaying())
                    getSound("pacchomp").loop();
                if (Ghost.areScaredGhosts()  &&  !pacJustKilled  &&  !getSound("pellet").isPlaying())
                    getSound("pellet").loop();
                if (Ghost.areAngryGhosts()   &&  myModel.isDoingGhostSounds()  &&  !getSound("angry").isPlaying())
                    getSound("angry").loop();
                if (Ghost.areEatenGhosts()   &&  myModel.isDoingGhostSounds()  &&  !ghostJustEaten  &&  !getSound("eyesrun").isPlaying())
                    getSound("eyesrun").loop();
            }

            // choose action to take based on type of event fired
            if (eventType == GameModel.Event.EXTRA_LIFE)
                getSound("extrapac").play();
            else if (eventType == GameModel.Event.FRUIT_EATEN)
                getSound("fruiteat").play();
            else if (eventType == GameModel.Event.GHOST_EATEN)
                getSound("ghosteat").play();
            else if (eventType == GameModel.Event.HALF_DOTS_EATEN)
                setAngry(getSound("angry2"));
            else if (eventType == GameModel.Event.PLAYER_DEATH) {
                haltAllSounds();
                getSound("killed").play();
            }
            else if (eventType == GameModel.Event.POWER_PELLET_EATEN)
                getSound("eat").play();
        }
    }

    /** Returns the sound clip with the given name key. */
    protected TimedAudioClip getSound(String fileName) {
        if (!myClipTable.containsKey(fileName))
            throw new IllegalArgumentException("sound file not found in table: " + fileName);

        return (TimedAudioClip)myClipTable.get(fileName);
    }

    /** Stops all of the game's sounds that are currently playing. */
    public void haltAllSounds() {
        for (TimedAudioClip clip : myClipTable.values()) {
            clip.stop();
        }
    }

    /** Loads sound files for the game. */
    public void loadSounds(String folder) {
        // myApplet.showStatus("Loading sounds...");
        String[] fileNames = {"angry1", "angry2", "angry3", "credit", "eat", "extrapac", "eyesrun", "fruit", "fruiteat",
            "ghosteat", "interm", "killed", "pacchomp", "pacstart", "pellet"};
        int[] times = (myType == GameModel.Type.MRS_PACMAN)
                ? new int[] {404, 666, 666, 218, 219, 1899, 302, 60, 408, 493, 8542, 1119, 134, 4209, 6296}
                : new int[] {1586, 1463, 541, 218, 219, 1899, 250, 60, 384, 573, 5204, 1618, 236, 4324, 281};

        for (int i = 0;  i < fileNames.length;  i++) {
            // if (GameModel.DEBUG)  myApplet.showStatus("Loading sound " + fileNames[i]);
            myClipTable.put(fileNames[i], createTimedAudioClip(folder + '/' + fileNames[i] + ".au", times[i]));
        }

        myClipTable.put("angry", getSound("angry1"));
        // myApplet.showStatus("Done loading sounds");
    }

    /** Plays the given sound. */
    protected void pausedPlaySound(String fileName) {
        myModel.setPaused(true);
        getSound(fileName).playAndWait();
        myModel.setPaused(false);
    }

    /** Sets the audio sound played when ghosts are chasing Pac-Man. */
    protected void setAngry(TimedAudioClip clip) {
        getSound("angry").stop();
        myClipTable.put("angry", clip);
    }
}
