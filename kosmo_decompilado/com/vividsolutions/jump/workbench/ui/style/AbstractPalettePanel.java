/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.style;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.java2xml.XML2Java;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import java.awt.Color;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;

public abstract class AbstractPalettePanel
extends JPanel {
    protected ArrayList listeners = new ArrayList();
    private static BasicStyleList basicStyleList = null;

    public void add(Listener listener) {
        this.listeners.add(listener);
    }

    public abstract void setAlpha(int var1);

    protected void fireBasicStyleChosen(BasicStyle basicStyle) {
        for (Listener listener : this.listeners) {
            listener.basicStyleChosen(basicStyle);
        }
    }

    protected List basicStyles() {
        block8: {
            try {
                if (basicStyleList != null) break block8;
                InputStream stream = this.getClass().getResourceAsStream("AbstractPalettePanel.xml");
                try {
                    InputStreamReader reader = new InputStreamReader(stream);
                    try {
                        basicStyleList = (BasicStyleList)new XML2Java().read(reader, BasicStyleList.class);
                    }
                    finally {
                        reader.close();
                    }
                }
                finally {
                    stream.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
                Assert.shouldNeverReachHere();
                return null;
            }
        }
        return basicStyleList.getBasicStyles();
    }

    public static void main(String[] args) {
        Color c = new Color(255, 28, 174).darker();
        System.out.println(String.valueOf(c.getRed()) + ", " + c.getGreen() + ", " + c.getBlue());
    }

    public static class BasicStyleList {
        private ArrayList basicStyles = new ArrayList();

        public List getBasicStyles() {
            return Collections.unmodifiableList(this.basicStyles);
        }

        public void addBasicStyle(BasicStyle basicStyle) {
            this.basicStyles.add(basicStyle);
        }
    }

    public static interface Listener {
        public void basicStyleChosen(BasicStyle var1);
    }
}

