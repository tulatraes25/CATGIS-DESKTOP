/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer.style;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class FontCache {
    private static final Logger LOGGER = Logger.getLogger(FontCache.class);
    static FontCache defaultInstance;
    Set<String> systemFonts = new HashSet<String>();
    Map<String, Font> loadedFonts = new ConcurrentHashMap<String, Font>();

    @Deprecated
    public static FontCache getDefaultInsance() {
        return FontCache.getDefaultInstance();
    }

    public static FontCache getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new FontCache();
        }
        return defaultInstance;
    }

    public synchronized Font getFont(String requestedFont) {
        Font javaFont = null;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)("trying to load " + requestedFont));
        }
        if (this.loadedFonts.containsKey(requestedFont)) {
            return this.loadedFonts.get(requestedFont);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)"not already loaded");
        }
        if (this.getSystemFonts().contains(requestedFont)) {
            javaFont = new Font(requestedFont, 0, 12);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug((Object)"not a system font");
            }
            javaFont = this.loadFromUrl(requestedFont);
        }
        if (javaFont == null) {
            LOGGER.warn((Object)("Could not load font " + requestedFont));
        } else {
            this.loadedFonts.put(requestedFont, javaFont);
        }
        return javaFont;
    }

    Font loadFromUrl(String fontUrl) {
        InputStream is;
        block17: {
            is = null;
            if (fontUrl.startsWith("http") || fontUrl.startsWith("file:")) {
                try {
                    URL url = new URL(fontUrl);
                    is = url.openStream();
                }
                catch (MalformedURLException mue) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info((Object)("Bad url in SLDStyleFactory " + fontUrl + "\n" + mue));
                    }
                    break block17;
                }
                catch (IOException ioe) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info((Object)("IO error in SLDStyleFactory " + fontUrl + "\n" + ioe));
                    }
                    break block17;
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug((Object)"not a URL");
            }
            File file = new File(fontUrl);
            try {
                is = new FileInputStream(file);
            }
            catch (FileNotFoundException fne) {
                if (!LOGGER.isInfoEnabled()) break block17;
                LOGGER.info((Object)("Bad file name in SLDStyleFactory" + fontUrl + "\n" + fne));
            }
        }
        if (is == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info((Object)"null input stream, could not load the font");
            }
            return null;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)"about to load");
        }
        try {
            return Font.createFont(0, is);
        }
        catch (FontFormatException ffe) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info((Object)("Font format error in SLDStyleFactory " + fontUrl + "\n" + ffe));
            }
            return null;
        }
        catch (IOException ioe) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info((Object)("IO error in SLDStyleFactory " + fontUrl + "\n" + ioe));
            }
            return null;
        }
    }

    public void registerFont(Font f) {
        this.loadedFonts.put(f.getName(), f);
    }

    public synchronized void resetCache() {
        if (this.systemFonts != null) {
            this.systemFonts.clear();
        }
        if (this.loadedFonts != null) {
            this.loadedFonts.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Set<String> getSystemFonts() {
        if (this.systemFonts.size() == 0) {
            Set<String> set = this.systemFonts;
            synchronized (set) {
                if (this.systemFonts.size() == 0) {
                    Font[] fonts;
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    HashSet<String> fontset = new HashSet<String>();
                    Font[] fontArray = fonts = ge.getAllFonts();
                    int n = fonts.length;
                    int n2 = 0;
                    while (n2 < n) {
                        Font font = fontArray[n2];
                        fontset.add(font.getName());
                        fontset.add(font.getFamily());
                        ++n2;
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug((Object)("there are " + fontset.size() + " fonts available"));
                    }
                    this.systemFonts.addAll(fontset);
                }
            }
        }
        return this.systemFonts;
    }

    public Set<String> getAvailableFonts() {
        HashSet<String> availableFonts = new HashSet<String>();
        availableFonts.addAll(this.getSystemFonts());
        availableFonts.addAll(this.loadedFonts.keySet());
        return availableFonts;
    }
}

