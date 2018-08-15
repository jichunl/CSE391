package pacman.view;

import pacman.model.*;
import pacman.sprite.*;
//import pacman.utility.ExtensionBasedFileFilter;
import pacman.utility.*;
import java.awt.MediaTracker;
import pacman.view.View;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

/** This class represents the Panel for the arcade view of the game, which is
  * a 2D representation meant to look just like the actual arcade Pac-Man game.
  */
public class ArcadeView extends View {
    private static final long serialVersionUID = 0;
    
    private static final int BG_IMAGE_WIDTH = 686,
        BG_IMAGE_HEIGHT = 769,  // bg image size
        X_OFFSET = 204,
        Y_OFFSET = 95,            // u/l corner of cut-out area
        WINDOW_WIDTH = 300,
        WINDOW_HEIGHT = 305;  // size of cut-out transparent area in bg image

    // for speed, all instances of one view share images
    private static final boolean WANTS_BORDER = false;
    private static final boolean USING_IMAGE_STRIP = true;
    protected static Map<String, Image> ourImageTable = new HashMap<String, Image>();
    protected static boolean weGotImages = false;

    // a list, in order, of every image in the big pac-man image strip
    private static String[][] bigImageList    = {{"pacicon", "midway", "act0", "act1", "act2"}};
    private static String[][] mediumImageList = {{"pacmandown1",  "pacmandown2",     "pacmandown3",        "pacmanleft1",     "pacmanleft2",        "pacmanleft3",        "pacmanright1",     "pacmanright2",     "pacmanright3",    "pacmanup1"},
        {"pacmanup2",       "pacmanup3",       "pacmandie1",      "pacmandie2",      "pacmandie3",         "pacmandie4",         "pacmandie5",       "pacmandie6",       "pacmandie7",      "pacmandie8"},
        {"mrspacmandown1",  "mrspacmandown2",  "mrspacmandown3",  "mrspacmanleft1",  "mrspacmanleft2",     "mrspacmanleft3",     "mrspacmanright1",  "mrspacmanright2",  "mrspacmanright3", "mrspacmanup1"},
        {"mrspacmanup2",    "mrspacmanup3",    "mrspacmandie1",   "mrspacmandie2",   "mrspacmandie3",      "mrspacmandie4",      "mrspacmandie5",    "mrspacmandie6",    "mrspacmandie7",   "mrspacmandie8"},
        {"blinkydown2",     "blinkydown1",     "blinkyleft2",     "blinkyleft1",     "blinkyneutral2",     "blinkyneutral1",     "blinkyright2",     "blinkyright1",     "blinkyup2",       "blinkyup1"},
        {"clydedown2",      "clydedown1",      "clydeleft2",      "clydeleft1",      "clydeneutral2",      "clydeneutral1",      "clyderight2",      "clyderight1",      "clydeup2",        "clydeup1"},
        {"inkydown2",       "inkydown1",       "inkyleft2",       "inkyleft1",       "inkyneutral2",       "inkyneutral1",       "inkyright2",       "inkyright1",       "inkyup2",         "inkyup1"},
        {"pinkydown2",      "pinkydown1",      "pinkyleft2",      "pinkyleft1",      "pinkyneutral2",      "pinkyneutral1",      "pinkyright2",      "pinkyright1",      "pinkyup2",        "pinkyup1"},
        {"satandown2",      "satandown1",      "satanleft2",      "satanleft1",      "satanneutral2",      "satanneutral1",      "satanright2",      "satanright1",      "satanup2",        "satanup1"},
        {"scaredblue2",     "scaredblue1",     "scaredwhite2",    "scaredwhite1",    "eyesdown",           "eyesleft",           "eyesneutral",      "eyesright",        "eyesup",          "whitesquare"},
        {"cherry",          "strawberry",      "orange",          "apple",           "pretzel",            "eggplant",           "banana",           "paclives",         "mrspaclives",     "heart"},
        {"mrs100",          "mrs200",          "mrs300",          "mrs500",          "mrs1000",            "mrs2000",            "mrs4000", "emptyicon"},
        {"200cyan", "400cyan", "800cyan", "1600cyan", "100pink", "300pink", "500pink", "1000pink"}};
    private static String[][] smallImageList  = {{"dotblue", "dotcyan", "dotflesh", "dotgreen", "dotorange", "dotpink", "dotred", "dotwhite", "dotyellow", "pelletblue", "pelletcyan", "pelletflesh", "pelletgreen", "pelletorange", "pelletpink", "pelletred", "pelletwhite", "pelletyellow"},
        {"border0", "border1", "border2", "border3", "border4", "border5", "border6", "border7", "border8", "border9", "bordera", "borderb", "borderc", "borderd", "bordere", "borderf"},
        {"cage0", "cage1", "cage2", "cage3", "cage4", "cage5", "cage6", "cage7", "cage8", "cage9", "cagea", "cageb", "cagec", "caged", "cagee", "cagef"},
        {"wall0", "wall1", "wall2", "wall3", "wall4", "wall5", "wall6", "wall7", "wall8", "wall9", "walla", "wallb", "wallc", "walld", "walle", "wallf"},
        {"level2border0", "level2border1", "level2border2", "level2border3", "level2border4", "level2border5", "level2border6", "level2border7", "level2border8", "level2border9", "level2bordera", "level2borderb", "level2borderc", "level2borderd", "level2bordere", "level2borderf"},
        {"level2cage0", "level2cage1", "level2cage2", "level2cage3", "level2cage4", "level2cage5", "level2cage6", "level2cage7", "level2cage8", "level2cage9", "level2cagea", "level2cageb", "level2cagec", "level2caged", "level2cagee", "level2cagef"},
        {"level2wall0", "level2wall1", "level2wall2", "level2wall3", "level2wall4", "level2wall5", "level2wall6", "level2wall7", "level2wall8", "level2wall9", "level2walla", "level2wallb", "level2wallc", "level2walld", "level2walle", "level2wallf"},
        {"level3border0", "level3border1", "level3border2", "level3border3", "level3border4", "level3border5", "level3border6", "level3border7", "level3border8", "level3border9", "level3bordera", "level3borderb", "level3borderc", "level3borderd", "level3bordere", "level3borderf"},
        {"level3cage0", "level3cage1", "level3cage2", "level3cage3", "level3cage4", "level3cage5", "level3cage6", "level3cage7", "level3cage8", "level3cage9", "level3cagea", "level3cageb", "level3cagec", "level3caged", "level3cagee", "level3cagef"},
        {"level3wall0", "level3wall1", "level3wall2", "level3wall3", "level3wall4", "level3wall5", "level3wall6", "level3wall7", "level3wall8", "level3wall9", "level3walla", "level3wallb", "level3wallc", "level3walld", "level3walle", "level3wallf"},
        {"level4border0", "level4border1", "level4border2", "level4border3", "level4border4", "level4border5", "level4border6", "level4border7", "level4border8", "level4border9", "level4bordera", "level4borderb", "level4borderc", "level4borderd", "level4bordere", "level4borderf"},
        {"level4cage0", "level4cage1", "level4cage2", "level4cage3", "level4cage4", "level4cage5", "level4cage6", "level4cage7", "level4cage8", "level4cage9", "level4cagea", "level4cageb", "level4cagec", "level4caged", "level4cagee", "level4cagef"},
        {"level4wall0", "level4wall1", "level4wall2", "level4wall3", "level4wall4", "level4wall5", "level4wall6", "level4wall7", "level4wall8", "level4wall9", "level4walla", "level4wallb", "level4wallc", "level4walld", "level4walle", "level4wallf"},
        {"gate0", "gate1", "gate2", "gate3", "gate4", "gate5", "gate6", "gate7", "gate8", "gate9", "gatea", "gateb", "gatec", "gated", "gatee", "gatef"},
        {"ablue", "bblue", "cblue", "dblue", "eblue", "fblue", "gblue", "hblue", "iblue", "jblue", "kblue", "lblue", "mblue", "nblue", "oblue", "pblue", "qblue", "rblue", "sblue", "tblue", "ublue", "vblue"},
        {"wblue", "xblue", "yblue", "zblue", "0blue", "1blue", "2blue", "3blue", "4blue", "5blue", "6blue", "7blue", "8blue", "9blue", "-blue", ".blue", "!blue", "\"blue", "/blue", "@blue", "ptsblue1", "ptsblue2", " blue"},
        {"acyan", "bcyan", "ccyan", "dcyan", "ecyan", "fcyan", "gcyan", "hcyan", "icyan", "jcyan", "kcyan", "lcyan", "mcyan", "ncyan", "ocyan", "pcyan", "qcyan", "rcyan", "scyan", "tcyan", "ucyan", "vcyan"},
        {"wcyan", "xcyan", "ycyan", "zcyan", "0cyan", "1cyan", "2cyan", "3cyan", "4cyan", "5cyan", "6cyan", "7cyan", "8cyan", "9cyan", "-cyan", ".cyan", "!cyan", "\"cyan", "/cyan", "@cyan", "ptscyan1", "ptscyan2", " cyan"},
        {"aflesh", "bflesh", "cflesh", "dflesh", "eflesh", "fflesh", "gflesh", "hflesh", "iflesh", "jflesh", "kflesh", "lflesh", "mflesh", "nflesh", "oflesh", "pflesh", "qflesh", "rflesh", "sflesh", "tflesh", "uflesh", "vflesh"},
        {"wflesh", "xflesh", "yflesh", "zflesh", "0flesh", "1flesh", "2flesh", "3flesh", "4flesh", "5flesh", "6flesh", "7flesh", "8flesh", "9flesh", "-flesh", ".flesh", "!flesh", "\"flesh", "/flesh", "@flesh", "ptsflesh1", "ptsflesh2", " flesh"},
        {"agreen", "bgreen", "cgreen", "dgreen", "egreen", "fgreen", "ggreen", "hgreen", "igreen", "jgreen", "kgreen", "lgreen", "mgreen", "ngreen", "ogreen", "pgreen", "qgreen", "rgreen", "sgreen", "tgreen", "ugreen", "vgreen"},
        {"wgreen", "xgreen", "ygreen", "zgreen", "0green", "1green", "2green", "3green", "4green", "5green", "6green", "7green", "8green", "9green", "-green", ".green", "!green", "\"green", "/green", "@green", "ptsgreen1", "ptsgreen2", " green"},
        {"aorange", "borange", "corange", "dorange", "eorange", "forange", "gorange", "horange", "iorange", "jorange", "korange", "lorange", "morange", "norange", "oorange", "porange", "qorange", "rorange", "sorange", "torange", "uorange", "vorange"},
        {"worange", "xorange", "yorange", "zorange", "0orange", "1orange", "2orange", "3orange", "4orange", "5orange", "6orange", "7orange", "8orange", "9orange", "-orange", ".orange", "!orange", "\"orange", "/orange", "@orange", "ptsorange1", "ptsorange2", " orange"},
        {"apink", "bpink", "cpink", "dpink", "epink", "fpink", "gpink", "hpink", "ipink", "jpink", "kpink", "lpink", "mpink", "npink", "opink", "ppink", "qpink", "rpink", "spink", "tpink", "upink", "vpink"},
        {"wpink", "xpink", "ypink", "zpink", "0pink", "1pink", "2pink", "3pink", "4pink", "5pink", "6pink", "7pink", "8pink", "9pink", "-pink", ".pink", "!pink", "\"pink", "/pink", "@pink", "ptspink1", "ptspink2", " pink"},
        {"ared", "bred", "cred", "dred", "ered", "fred", "gred", "hred", "ired", "jred", "kred", "lred", "mred", "nred", "ored", "pred", "qred", "rred", "sred", "tred", "ured", "vred"},
        {"wred", "xred", "yred", "zred", "0red", "1red", "2red", "3red", "4red", "5red", "6red", "7red", "8red", "9red", "-red", ".red", "!red", "\"red", "/red", "@red", "ptsred1", "ptsred2", " red"},
        {"awhite", "bwhite", "cwhite", "dwhite", "ewhite", "fwhite", "gwhite", "hwhite", "iwhite", "jwhite", "kwhite", "lwhite", "mwhite", "nwhite", "owhite", "pwhite", "qwhite", "rwhite", "swhite", "twhite", "uwhite", "vwhite"},
        {"wwhite", "xwhite", "ywhite", "zwhite", "0white", "1white", "2white", "3white", "4white", "5white", "6white", "7white", "8white", "9white", "-white", ".white", "!white", "\"white", "/white", "@white", "ptswhite1", "ptswhite2", " white"},
        {"ayellow", "byellow", "cyellow", "dyellow", "eyellow", "fyellow", "gyellow", "hyellow", "iyellow", "jyellow", "kyellow", "lyellow", "myellow", "nyellow", "oyellow", "pyellow", "qyellow", "ryellow", "syellow", "tyellow", "uyellow", "vyellow"},
        {"wyellow", "xyellow", "yyellow", "zyellow", "0yellow", "1yellow", "2yellow", "3yellow", "4yellow", "5yellow", "6yellow", "7yellow", "8yellow", "9yellow", "-yellow", ".yellow", "!yellow", "\"yellow", "/yellow", "@yellow", "ptsyellow1", "ptsyellow2", " yellow"}};

    private int myMouthNum = 1;
    private int myGhostLegsNum = 1;

    /** Constructs a new View to view the given GameModel. */
    public ArcadeView(GameModel gm, Component parent, ResourceFetcher fetch) {
        super(gm, parent, fetch);
    }

    /** Constructs a new arcade view of the given game type. */
    public ArcadeView(GameModel gm, Component parent, ResourceFetcher fetch, Object type) {
        super(gm, parent, fetch, type);
        setBackground(Color.black);

        // load appropriate sound files
        if (myType == GameModel.Type.MRS_PACMAN)
            loadSounds("sounds/mspacman");
    }

    /** Loads sound files for the game. */
    public void loadSounds(String folder) {
        if (myType != GameModel.Type.MRS_PACMAN) {
            super.loadSounds("sounds");
            return;
        }

        // else load special mrs. pac sounds
        String[] fileNames = {"angry1", "angry2", "angry3", "eat", "extrapac", "eyesrun", "fruit", "fruiteat",
            "ghosteat", "interm", "killed", "pacchomp", "pacstart", "pellet"};
        int[] times = {404, 666, 666, 219, 1899, 302, 60, 408, 493, 8542, 1119, 134, 4209, 6296};

        myClipTable.clear();
        for (int i = 0;  i < fileNames.length;  i++)
            myClipTable.put(fileNames[i], createTimedAudioClip(folder + '/' + fileNames[i] + ".au", times[i]));

        myClipTable.put("angry", getSound("angry1"));
    }

    /** Called to notify this view of an event in its model. */
    public void gameUpdated(Listenable origin, Object source, Object eventType) {
        // turn on/off appropriate sounds
        doSounds(eventType);

        if (eventType == GameModel.Event.NEW_LEVEL
                ||  eventType == GameModel.Event.NEW_GAME) {
            // level changed; reset game board and dimensions
            myBackground = null;
            updateSize();
            repaint();
        } else if (eventType == GameModel.Event.GAME_UPDATED) {
            if (WANTS_BORDER)
                repaint(X_OFFSET, Y_OFFSET, WINDOW_WIDTH, WINDOW_HEIGHT);
            else
                repaint(0, 0, getPreferredSize().width, getPreferredSize().height);
        }
    }

    /** Called to notify this view that its size has changed; used for scaling. */
    public void updateSize() {
        int width = 1, height = 1;
        if (WANTS_BORDER) {
            width = BG_IMAGE_WIDTH;
            height = BG_IMAGE_HEIGHT;
        }
        else {
            Level level = myModel.getCurrentLevel();
            if (level != null) {
                width = (int)Math.max(level.getWidth(), Level.GRID_SIZE * 28);
                height = (int)Math.max(level.getHeight(), Level.GRID_SIZE * 31) + Level.GRID_SIZE * 5;
            }
        }
        setPreferredSize(new Dimension(width, height));
    }

    /** Returns whether or not the given key maps to an image in this view's table. */
    private final boolean weHaveImage(String key) {
        return ourImageTable.containsKey(key);

    }

    /** Returns the image mapped to the given key name. */
    private final Image getImage(String key) {
        Image img = (Image)ourImageTable.get(key);
        if (img == null) {
            String message = "no such image: \"" + key + "\"";
            throw new IllegalArgumentException(message);
        }
        else
            return img;
    }

    /** Gets all the images that will be drawn on the screen, storing them
      * into a hash table.
      */
    private synchronized void fetchAllImages() {
        // Fetch all images from given folder (cool!), put into hash table
        int y = 0;
        Image strip = myFetcher.fetchImage("images/strip.gif");
        //System.out.println("strip = " + strip + "\nstrip's size = " + strip.getWidth(this) + ", " + strip.getHeight(this));

        ImageProducer source = strip.getSource();
        MediaTracker mt = new MediaTracker(this);

        y = getAllImagesFromArray(bigImageList,    32, y, source, mt);
        y = getAllImagesFromArray(mediumImageList, 16, y, source, mt);
        getAllImagesFromArray(smallImageList,   8, y, source, mt);
        getAllImagesFromArray(smallImageList,   8, y, source, mt);

        // a hack
        ourImageTable.put("pacmanneutral1",    getImage("pacmanup1"));
        ourImageTable.put("pacmanneutral2",    getImage("pacmanup1"));
        ourImageTable.put("pacmanneutral3",    getImage("pacmanup1"));
        ourImageTable.put("mrspacmanneutral1", getImage("mrspacmanup1"));
        ourImageTable.put("mrspacmanneutral2", getImage("mrspacmanup1"));
        ourImageTable.put("mrspacmanneutral3", getImage("mrspacmanup1"));

        try {
            mt.waitForAll();
        } catch (InterruptedException ie) {}

        weGotImages = true;
    }

    /** Retrieves all images from the given array of image names. */
    private int getAllImagesFromArray(String[][] array, int size, int y, ImageProducer source, MediaTracker mt) {
        String folder = "images/done/";
        ImageProducer producer = null;
        ImageFilter stripFilter = null;

        for (int row = 0;  row < array.length;  row++) {
            for (int col = 0;  col < array[row].length;  col++) {
                Image img = null;
                if (array[row][col].startsWith("level"))
                    img = getImage(array[row][col].substring(6, array[row][col].length()));
                else if (   array[row][col].substring(1, array[row][col].length()).equals("blue")
                        ||  array[row][col].substring(1, array[row][col].length()).equals("cyan")
                        ||  array[row][col].substring(1, array[row][col].length()).equals("flesh")
                        ||  array[row][col].substring(1, array[row][col].length()).equals("green")
                        ||  array[row][col].substring(1, array[row][col].length()).equals("orange")
                        ||  array[row][col].substring(1, array[row][col].length()).equals("pink")
                        ||  array[row][col].substring(1, array[row][col].length()).equals("red")
                        ||  array[row][col].substring(1, array[row][col].length()).equals("yellow")) {
                    if (!USING_IMAGE_STRIP)
                        // img = myFetcher.fetchImage(array[row][col].charAt(0) + "white.gif");
                        if (!weHaveImage("0.gif"))
                            img = myFetcher.fetchImage(folder + "0.gif");
                        else
                            img = getImage("0.gif");
                    else {
                        if (!weHaveImage(array[row][col].charAt(0) + "white"))
                            continue;
                        img = getImage(array[row][col].charAt(0) + "white");
                    }
                }
                else {
                    if (!USING_IMAGE_STRIP) {
                        img = myFetcher.fetchImage(folder + array[row][col] + ".gif");
                    } else {
                        stripFilter = new CropImageFilter(col*size, y, size, size);
                        producer = new FilteredImageSource(source, stripFilter);
                        img = createImage(producer);
                    }
                    mt.addImage(img, 0);
                }

                ourImageTable.put(array[row][col], img);
            }
            y += size;
        }

        return y;
    }

    /** Paints this arcade view on the screen. */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!weGotImages) {
            //System.out.println("fetching images");
            fetchAllImages();
            //System.out.println("done fetching images");
            //fetchAllImages();
        }

        Level level = myModel.getCurrentLevel();
        if (level == null)
            return;

        // Image machine = getImage("machine");
        // setPreferredSize(new Dimension(machine.getWidth(this), machine.getHeight(this)));

        // draw the picture of the pac-man arcade machine
        if (WANTS_BORDER)
            g.drawImage(getImage("machine"), 0, 0, this);

        // draw header with scores etc.
//      g.setColor(Color.black);
//      g.fillRect(0, 0, myModel.getWidth(), 3 * Level.GRID_SIZE);

        // adjust origin because of background image
        if (WANTS_BORDER) {
            int xtrans = X_OFFSET + (WINDOW_WIDTH  - level.getWidth())  / 2;
            int ytrans = Y_OFFSET + (WINDOW_HEIGHT - (level.getHeight() + 3 * Level.GRID_SIZE)) / 2;
            g.translate(xtrans, ytrans);
        }

        if (myModel.shouldShowPlayerNumber(1))
            drawWord(g, "1up", "white", 3, 0);
        if (myModel.getNumPlayers() >= 1)
            drawWord(g, Utility.padStringR("" + myModel.getScore(1), 6), "white",  1, 1);

        if (myModel.shouldShowPlayerNumber(2))
            drawWord(g, "2UP", "white", 22, 0);
        if (myModel.getNumPlayers() >= 2)
            drawWord(g, Utility.padStringR("" + myModel.getScore(2), 6), "white", 20, 1);

        drawWord(g, "high score", "white", 9, 0);
        drawWord(g, Utility.padStringR("" + myModel.getHighScore(), 6), "white", 11, 1);

        g.translate(0, 3 * Level.GRID_SIZE);

        // draw game board (construct it if not present)
        boolean gotBoardImage = ourImageTable.containsKey(level.getName());
        if (!gotBoardImage) {
            Image board = generateBoardImage();
            ourImageTable.put(level.getName(), board);
        }
        g.drawImage(getImage(level.getName()), 0, 0, this);

        // draw dots, pellets, fruit (and walls, if we have no map image file)
        Sprite[][] map = level.getGrid();
        for (int x = 0;  x < level.getGridWidth();  x++)
        for (int y = 0;  y < level.getGridHeight();  y++) {
            Sprite spr = map[x][y];
            if (spr.isVisible) {
                // don't draw walls unless we have to because of missing map image
                if (!(spr.type == Sprite.WALL  ||  spr.type == Sprite.GATE))
                    g.drawImage(getImage(spr.getImageName()), spr.rect.x, spr.rect.y, this);
            }
        }

        if (myModel.isInProgress()) {
            // draw lives on bottom
            for (int i = 0;  i < myModel.getNumLives();  i++)
                g.drawImage(getImage((myType == GameModel.Type.MRS_PACMAN ?  "mrs"  :  "") + "paclives"),
                        Level.GRID_SIZE * (2*i+2),
                        level.getHeight() + 2, this);
            // draw fruit on bottom
            Fruit fr = myModel.getFruit();
            if (fr != null) {
                g.drawImage(getImage(fr.getImageName()),
                        level.getWidth() - Level.GRID_SIZE * 4 + 2,
                        level.getHeight() + 2, this);
            }
        } else {
            // draw credits on bottom
            drawWord(g, "credit " + Utility.padStringR("" + myModel.getNumCredits(), 2), "white", 2, level.getGridHeight() + 1);
        }

//      if (myModel.isDemoPlaying()) {
//          drawWord(g, "game  over", "red", 9, 17);
//      }

        if (myModel.justStarted()) {
            // draw player's prompt but no sprites
            drawWord(g, "player " + Utility.getWordForNumber(myModel.getPlayerNumber()), "cyan", 9, 11);
            drawWord(g, "ready!", "yellow", 11, 17);
        }

        else {
            // draw sprites (Pac-Man/ghosts)
            List<MovingSprite> sprites = myModel.getMovingSprites();
            int spriteOffset = -3;

            if (myModel.isDoingMovement()) {
                // change images each half second
                myGhostLegsNum = (int)(myModel.getUpdateCounter() / (GameModel.UPDATES_PER_SECOND / 4) % 2 + 1);
            }

            for (MovingSprite spr : sprites) {
                if (!spr.isVisible)  continue;

                String imageName = "";
                if (spr.type == Sprite.PACMAN) {
                    if (!myModel.shouldDrawPacMan())  continue;

                    if (myType == GameModel.Type.MRS_PACMAN)
                        imageName += "mrs";
                    imageName += "pacman";
                    PacMan pac = (PacMan)spr;

                    if (pac.isAlive()  ||  pac.wasJustKilled()) {
                        if (spr.isMovingLeft())        imageName += "left";
                        else if (spr.isMovingRight())  imageName += "right";
                        else if (spr.isMovingUp())     imageName += "up";
                        else if (spr.isMovingDown())   imageName += "down";
                        else                           imageName += "neutral";

                        if (myModel.shouldMovePacManMouth(pac)) {
                            int framesPerSwitch = (int)Math.max(GameModel.UPDATES_PER_SECOND / 21, 1);

                            myMouthNum = (int)((myModel.getUpdateCounter() / framesPerSwitch) % 4 + 1);
                            if (myMouthNum == 4)
                                myMouthNum = 2;
                        }

                        imageName += myMouthNum;
                    }
                    else {
                        // show his ass dying
                        int framesPerSwitch = GameModel.UPDATES_PER_SECOND / 6;
                        int q = (pac.getNumUpdatesSinceKilled() - GameModel.UPDATES_PER_SECOND) / framesPerSwitch + 1;
                        if (1 <= q  &&  q <= 8)
                            imageName += "die" + q;
                        else
                            imageName = "emptyicon";
                    }
                }
                else if (spr.type == Sprite.GHOST) {
                    Ghost gh = (Ghost)spr;

                    String ghostName = "";

                    // draw just-eaten ghost as score he was worth
                    if (gh.wasJustEaten())
                        imageName = "" + (gh.getScore() * (int)Math.pow(2, myModel.getGhostScorePower() - 1)) + "cyan";

                    // otherwise, figure out which ghost this is, his status, which
                    // way he is facing, etc., and draw appropriate image
                    else {
                        if (gh.isEaten())
                                ghostName = "eyes";

                        else if (gh.isScared()) {
                            ghostName = "scared" + gh.getColor();
                        }
                        else
                            ghostName = gh.getName();

                        imageName += ghostName;

                        if (!gh.isScared()) {
                            // then the image depends on direction
                            if (gh.isMovingUp())
                                imageName += "up";
                            else if (gh.isMovingDown())
                                imageName += "down";
                            else if (gh.isMovingLeft())
                                imageName += "left";
                            else if (gh.isMovingRight())
                                imageName += "right";
                            else  // not moving
                                imageName += "neutral";
                        }

                        int ghostLegsNum = gh.shouldAnimate()  ?  ghostLegsNum = myGhostLegsNum  :  1;
                        if (!gh.isEaten())
                            imageName += ghostLegsNum;
                    }
                }
                else if (spr instanceof Fruit) {
                    // draw just-eaten fruit as the score it was worth
                    // (stupid complicated game)
                    Fruit fr = (Fruit)spr;
                    if (fr.wasJustEaten()) {
                        imageName = "" + fr.getScore();
                        if (myType == GameModel.Type.MRS_PACMAN)
                            imageName = "mrs" + imageName;
                        else
                            imageName += "pink";
                    } else {
                        imageName += fr.getImageName();
                    }
                }
                g.drawImage(getImage(imageName), spr.rect.x + spriteOffset, spr.rect.y + spriteOffset, this);
            }

            // draw "PLAYER ONE" / "READY" prompts
            if (myModel.isInProgress()  &&  myModel.getPacMan() != null  &&  myModel.getPacMan().wasJustRevived()) {
                // g.drawImage(getImage("player" + myModel.getPlayerNumber()), Level.GRID_SIZE * 9, Level.GRID_SIZE * 11, this);
                // g.drawImage(getImage("ready"), Level.GRID_SIZE * 11, Level.GRID_SIZE * 17, this);
                drawWord(g, "ready!", "yellow", 11, 17);
            }
        }

        if (myModel.isPaused()) {
            g.setColor(PacManColors.WHITE);
            drawWord(g, "paused!", "white", 11, 17);
        }
    }

    /** draws a number (ie a score) on the screen in Pac-Man's arcade font. */
    protected void drawWord(Graphics g, String word, String color, int gx, int gy) {
        // word = word.toLowerCase();
        for (int i = 0;  i < word.length();  i++)
            g.drawImage(getImage(word.charAt(i) + color), (int)((gx+i) * Level.GRID_SIZE), gy * Level.GRID_SIZE, this);
    }

    /** Updates the background image for this view, which contains the walls and gates
      * in the level.  It is repainted each time the game updates.
      */
    private Image generateBoardImage() {
        // we'll draw an image for the game board
        Level level = myModel.getCurrentLevel();
        Image board = createImage(level.getWidth() + 1, level.getHeight() + 1);
        Graphics board_g = board.getGraphics();
        board_g.setColor(Color.black);
        board_g.fillRect(0, 0, level.getWidth(), level.getHeight());

        // look through grid to find each wall, so we can draw it
        Sprite[][] map = level.getGrid();
        int gridWidth = level.getGridWidth();
        int gridHeight = level.getGridHeight();
        for (int x = 0;  x < gridWidth;   x++)
        for (int y = 0;  y < gridHeight;  y++) {
            Sprite spr = map[x][y];
            if (spr instanceof ObstacleSprite) {
                Image wallImage = null;
                String preferredImageName = level.getName() + spr.getImageName();

                // draw walls specific to a given level if they exist; otherwise use default walls
                wallImage = (weHaveImage(preferredImageName))  ?  getImage(preferredImageName)
                                                               :  getImage(spr.getImageName());
                board_g.drawImage(wallImage, spr.rect.x, spr.rect.y, this);

            }
        }

        return board;
    }
}