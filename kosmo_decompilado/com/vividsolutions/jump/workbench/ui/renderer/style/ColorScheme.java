/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import es.kosmo.core.utils.ColorGenerator;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.simbology.CustomColorSchemesXMLPersistence;
import org.saig.jump.widgets.util.DialogFactory;

public class ColorScheme {
    private static final Logger LOGGER = Logger.getLogger(ColorScheme.class);
    private static ArrayList<String> discreteColorSchemeNames;
    private static ArrayList<String> rangeColorSchemeNames;
    private static CollectionMap nameToColorsMap;
    private String name;
    private int lastColorReturned = -1;
    private List<Color> colors;

    public ColorScheme(String name, Collection<Color> colors) {
        this.name = name;
        this.colors = new ArrayList<Color>(colors);
    }

    public static ColorScheme create(String name) {
        Assert.isTrue((boolean)ColorScheme.nameToColorsMap().keySet().contains(name));
        return new ColorScheme(name, ColorScheme.nameToColorsMap().getItems(name));
    }

    private static void load() {
        try {
            if (rangeColorSchemeNames == null) {
                rangeColorSchemeNames = new ArrayList();
            }
            if (discreteColorSchemeNames == null) {
                discreteColorSchemeNames = new ArrayList();
            }
            if (nameToColorsMap == null) {
                nameToColorsMap = new CollectionMap();
            }
            ColorScheme.loadCustomColorSchemes();
            if (nameToColorsMap.isEmpty()) {
                InputStream inputStream = ColorScheme.class.getResourceAsStream("ColorScheme.txt");
                try {
                    for (String line : FileUtil.getContents(inputStream)) {
                        ColorScheme.add(line);
                    }
                }
                finally {
                    inputStream.close();
                }
            }
            Collections.sort(rangeColorSchemeNames);
            Collections.sort(discreteColorSchemeNames);
        }
        catch (IOException e) {
            Assert.shouldNeverReachHere((String)e.toString());
        }
    }

    public static void loadCustomScheme(String absoluteFilePath) {
        if (rangeColorSchemeNames == null || discreteColorSchemeNames == null || nameToColorsMap == null) {
            ColorScheme.load();
        }
        try {
            File file = new File(absoluteFilePath);
            if (!file.exists() || !file.canRead()) {
                DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme.the-definition-file-{0}-can-not-be-read", new Object[]{absoluteFilePath}), I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme.wrong-file"));
            }
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                for (String line : FileUtil.getContents(inputStream)) {
                    ColorScheme.add(line);
                }
            }
            finally {
                if (inputStream != null) {
                    ((InputStream)inputStream).close();
                }
            }
            Collections.sort(rangeColorSchemeNames);
            Collections.sort(discreteColorSchemeNames);
        }
        catch (IOException e) {
            Assert.shouldNeverReachHere((String)e.toString());
        }
    }

    private static void add(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        String name = tokenizer.nextToken().trim();
        String colorSchemeItemName = tokenizer.nextToken().trim();
        if (colorSchemeItemName.equalsIgnoreCase("range") || colorSchemeItemName.equalsIgnoreCase("discrete")) {
            boolean range = colorSchemeItemName.equalsIgnoreCase("range");
            (range ? rangeColorSchemeNames : discreteColorSchemeNames).add(name);
            while (tokenizer.hasMoreTokens()) {
                String hex = tokenizer.nextToken().trim();
                Assert.isTrue((hex.length() == 6 ? 1 : 0) != 0, (String)hex);
                ColorScheme.nameToColorsMap().addItem(name, Color.decode("#" + hex));
            }
        } else if (colorSchemeItemName.equalsIgnoreCase("rgb-range") || colorSchemeItemName.equalsIgnoreCase("rgb-discrete")) {
            ArrayList<Color> colors = new ArrayList<Color>();
            while (tokenizer.hasMoreTokens()) {
                String rgbString = tokenizer.nextToken().trim();
                StringTokenizer rgbTokenizer = new StringTokenizer(rgbString, "-");
                int red = new Integer(rgbTokenizer.nextToken().trim());
                int green = new Integer(rgbTokenizer.nextToken().trim());
                int blue = new Integer(rgbTokenizer.nextToken().trim());
                Color currentColor = new Color(red, green, blue);
                colors.add(currentColor);
            }
            boolean range = colorSchemeItemName.equalsIgnoreCase("rgb-range");
            (range ? rangeColorSchemeNames : discreteColorSchemeNames).add(name);
            ColorScheme.nameToColorsMap().addItems(name, colors);
        } else if (colorSchemeItemName.equalsIgnoreCase("custom-range") || colorSchemeItemName.equalsIgnoreCase("custom-discrete")) {
            int steps = new Integer(tokenizer.nextToken().trim());
            ArrayList<Color> colors = new ArrayList<Color>();
            while (tokenizer.hasMoreTokens()) {
                String rgbString = tokenizer.nextToken().trim();
                StringTokenizer rgbTokenizer = new StringTokenizer(rgbString, "-");
                int red = new Integer(rgbTokenizer.nextToken().trim());
                int green = new Integer(rgbTokenizer.nextToken().trim());
                int blue = new Integer(rgbTokenizer.nextToken().trim());
                Color currentColor = new Color(red, green, blue);
                colors.add(currentColor);
            }
            ColorGenerator generator = new ColorGenerator(steps, colors);
            boolean range = colorSchemeItemName.equalsIgnoreCase("custom-range");
            (range ? rangeColorSchemeNames : discreteColorSchemeNames).add(name);
            ColorScheme.nameToColorsMap().addItems(name, Arrays.asList(generator.getColorArray()));
        }
    }

    public static CollectionMap nameToColorsMap() {
        if (nameToColorsMap == null) {
            ColorScheme.load();
        }
        return nameToColorsMap;
    }

    public static Collection<String> rangeColorSchemeNames() {
        if (rangeColorSchemeNames == null) {
            ColorScheme.load();
        }
        return rangeColorSchemeNames;
    }

    public static Collection<String> discreteColorSchemeNames() {
        if (discreteColorSchemeNames == null) {
            ColorScheme.load();
        }
        return discreteColorSchemeNames;
    }

    public int getLastColorReturned() {
        return this.lastColorReturned;
    }

    public void setLastColorReturned(int lastColorReturned) {
        this.lastColorReturned = lastColorReturned;
    }

    public Color next() {
        ++this.lastColorReturned;
        if (this.lastColorReturned >= this.colors.size()) {
            this.lastColorReturned = 0;
        }
        return this.colors.get(this.lastColorReturned);
    }

    public List<Color> getColors() {
        return Collections.unmodifiableList(this.colors);
    }

    public String getName() {
        return this.name;
    }

    public static void saveCustomColorSchemes() {
        if (nameToColorsMap != null && discreteColorSchemeNames != null && rangeColorSchemeNames != null) {
            CustomColorSchemesXMLPersistence persistence = new CustomColorSchemesXMLPersistence();
            persistence.setCustomColorSchemes(nameToColorsMap, discreteColorSchemeNames, rangeColorSchemeNames);
            persistence.setPersistent();
        } else {
            LOGGER.warn((Object)I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme.there-are-no-color-schemes-to-save"));
        }
    }

    public static void loadCustomColorSchemes() {
        CustomColorSchemesXMLPersistence persistence = new CustomColorSchemesXMLPersistence();
        Object[] restored = persistence.getCustomColorSchemes();
        nameToColorsMap = (CollectionMap)restored[0];
        discreteColorSchemeNames = (ArrayList)restored[1];
        rangeColorSchemeNames = (ArrayList)restored[2];
    }
}

