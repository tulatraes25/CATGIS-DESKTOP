/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.lite;

import java.awt.Canvas;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageLoader
implements Runnable {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");
    private static Map<URL, BufferedImage> images = new HashMap<URL, BufferedImage>();
    private static Canvas obs = new Canvas();
    private static MediaTracker tracker = new MediaTracker(obs);
    private static int imageID = 1;
    private static long timeout = 10000L;
    private URL location;
    private boolean waiting = true;

    public static long getTimeout() {
        return timeout;
    }

    public static void setTimeout(long newTimeout) {
        timeout = newTimeout;
    }

    private void add(URL location, boolean interactive) {
        int imgId = imageID;
        this.location = location;
        LOGGER.finest("adding image, interactive? " + interactive);
        Thread t = new Thread(this);
        t.start();
        if (interactive) {
            LOGGER.finest("fast return");
            return;
        }
        this.waiting = true;
        long elapsed = 0L;
        long step = 500L;
        while (this.waiting && (elapsed < timeout || timeout < 0L)) {
            LOGGER.finest("waiting..." + this.waiting);
            try {
                Thread.sleep(500L);
                elapsed += 500L;
                if (!LOGGER.isLoggable(Level.FINEST)) continue;
                LOGGER.finest("Waiting for image " + location + ", elapsed " + elapsed + " milliseconds");
            }
            catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(String.valueOf(imgId) + " complete?: " + this.isFlagUp(imgId, 8));
            LOGGER.finest(String.valueOf(imgId) + " abort?: " + this.isFlagUp(imgId, 2));
            LOGGER.finest(String.valueOf(imgId) + " error?: " + this.isFlagUp(imgId, 4));
            LOGGER.finest(String.valueOf(imgId) + " loading?: " + this.isFlagUp(imgId, 1));
            LOGGER.finest(String.valueOf(imgId) + "slow return " + this.waiting);
        }
    }

    private boolean isFlagUp(int id, int flag) {
        return (tracker.statusID(id, true) & flag) == flag;
    }

    public BufferedImage get(URL location, boolean interactive) {
        if (images.containsKey(location)) {
            LOGGER.finest("found it");
            return images.get(location);
        }
        if (!interactive) {
            images.put(location, null);
        }
        LOGGER.finest("adding " + location);
        this.add(location, interactive);
        return images.get(location);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void run() {
        myID = 0;
        img = null;
        try {
            img = Toolkit.getDefaultToolkit().createImage(this.location);
            myID = ImageLoader.imageID++;
            ImageLoader.tracker.addImage(img, myID);
            if (true) ** GOTO lbl18
        }
        catch (Exception e) {
            ImageLoader.LOGGER.warning("Exception fetching image from " + this.location + "\n" + e);
            ImageLoader.images.remove(this.location);
            this.waiting = false;
            return;
        }
        {
            do {
                ImageLoader.tracker.waitForID(myID, 500L);
                ImageLoader.LOGGER.finest(String.valueOf(myID) + "loading - waiting....");
lbl18:
                // 2 sources

            } while ((ImageLoader.tracker.statusID(myID, true) & 1) != 0);
        }
        state = ImageLoader.tracker.statusID(myID, true);
        if (state == 4) {
            ImageLoader.LOGGER.finer(myID + " Error loading");
            this.waiting = false;
            return;
        }
        if ((state & 8) == 8) {
            ImageLoader.LOGGER.finest(myID + "completed load");
            iw = img.getWidth(ImageLoader.obs);
            ih = img.getHeight(ImageLoader.obs);
            bi = new BufferedImage(iw, ih, 2);
            big = bi.createGraphics();
            big.drawImage(img, 0, 0, ImageLoader.obs);
            ImageLoader.images.put(this.location, bi);
            this.waiting = false;
            return;
        }
        ImageLoader.LOGGER.finer(myID + " whoops - some other outcome " + state);
        this.waiting = false;
    }

    public void reset() {
        images.clear();
    }
}

