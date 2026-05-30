/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.images;

import java.net.URL;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class IconLoader {
    public static final ImageIcon DEFAULT_UNKNOW_ICON = new ImageIcon(IconLoader.class.getResource("default_icon.png"));
    public static final Logger LOGGER = Logger.getLogger(IconLoader.class);

    public static ImageIcon icon(String filename) {
        return IconLoader.icon(filename, true);
    }

    public static ImageIcon icon(String filename, boolean useDefaultForNull) {
        URL urlIcon = IconLoader.class.getResource(filename);
        if (urlIcon == null) {
            if (useDefaultForNull) {
                LOGGER.warn((Object)I18N.getMessage("com.vividsolutions.jump.workbench.ui.images.IconLoader.The-icon-{0}-has-not-been-found-default-icon-will-be-used", new Object[]{filename}));
                return DEFAULT_UNKNOW_ICON;
            }
            return null;
        }
        return new ImageIcon(urlIcon);
    }

    public static ImageIcon icon(URL url) {
        if (url == null) {
            LOGGER.warn((Object)I18N.getMessage("com.vividsolutions.jump.workbench.ui.images.IconLoader.The-icon-{0}-has-not-been-found-default-icon-will-be-used", new Object[]{url}));
            return DEFAULT_UNKNOW_ICON;
        }
        return new ImageIcon(url);
    }
}

