/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicFillPattern;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class ImageFillPattern
extends BasicFillPattern {
    public static final String FILENAME_KEY = "FILENAME";
    public static final String CLASS_KEY = "CLASS";

    public ImageFillPattern(Class c, String resourceName) {
        super(new Blackboard().putAll(CollectionUtil.createMap(new Object[]{"COLOR", Color.black, CLASS_KEY, c, FILENAME_KEY, resourceName})));
    }

    public ImageFillPattern() {
    }

    @Override
    public BufferedImage createImage(Blackboard properties) {
        ImageIcon imageIcon = new ImageIcon(((Class)properties.get(CLASS_KEY)).getResource(properties.get(FILENAME_KEY).toString()));
        BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), 2);
        Graphics2D g = (Graphics2D)bufferedImage.getGraphics();
        g.setComposite(AlphaComposite.getInstance(3, (float)((Color)this.getProperties().get("COLOR")).getAlpha() / 255.0f));
        g.drawImage(imageIcon.getImage(), 0, 0, null);
        return bufferedImage;
    }
}

