/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.util;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.ImageIcon;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class IniFileLoader {
    private static final Logger LOGGER = Logger.getLogger(IniFileLoader.class);
    private static Properties iniProperties = new Properties();
    private static final String ABOUT_IMAGE_KEY = "about_image";
    private static final String APP_ICON_KEY = "app_icon";
    private static final String DESKTOP_IMAGE_KEY = "desktop_image";
    private static final String SPLASH_IMAGE_KEY = "splash_image";
    private static final String CONFIG_FILE_OPTION_KEY = "config_file_option";
    private static final String I18N_OPTION_KEY = "i18n_file_option";
    private static final String PLUGIN_DIRECTORY_OPTION_KEY = "plugin_directory_option";
    private static final String PROJECT_DIRECT_LOAD_OPTION_KEY = "proj_option";
    private static final String APPLICATION_DIRECT_LOAD_OPTION_KEY = "idApp_option";
    private static final String PROPERTIES_OPTION_KEY = "properties_option";
    private static final String I18N_KEY = "i18n";
    private static final String ROOT_LOG_LEVEL = "log4j_root_level";

    public static void loadIniFile() {
        block15: {
            iniProperties.setProperty(ABOUT_IMAGE_KEY, "about.png");
            iniProperties.setProperty(APP_ICON_KEY, "app-icon.gif");
            iniProperties.setProperty(DESKTOP_IMAGE_KEY, "background.png");
            iniProperties.setProperty(SPLASH_IMAGE_KEY, "splash.png");
            iniProperties.setProperty(CONFIG_FILE_OPTION_KEY, "config-file");
            iniProperties.setProperty(I18N_OPTION_KEY, I18N_KEY);
            iniProperties.setProperty(PLUGIN_DIRECTORY_OPTION_KEY, "plug-in-directory");
            iniProperties.setProperty(PROPERTIES_OPTION_KEY, "properties");
            File iniFile = new File("kosmo.ini");
            if (iniFile.canRead()) {
                FileInputStream iniStream = null;
                try {
                    try {
                        iniStream = new FileInputStream(iniFile);
                        iniProperties.load(iniStream);
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (iniStream != null) {
                            try {
                                iniStream.close();
                            }
                            catch (IOException iOException) {}
                        }
                        break block15;
                    }
                }
                catch (Throwable throwable) {
                    if (iniStream != null) {
                        try {
                            iniStream.close();
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                    }
                    throw throwable;
                }
                if (iniStream != null) {
                    try {
                        iniStream.close();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
            }
        }
        IniFileLoader.loadImagesAndIconsFromProperties();
        IniFileLoader.loadOptionsFromProperties();
    }

    private static void loadOptionsFromProperties() {
        JUMPWorkbench.CONFIG_FILE_OPTION = iniProperties.getProperty(CONFIG_FILE_OPTION_KEY, "config-file");
        JUMPWorkbench.I18N_FILE_OPTION = iniProperties.getProperty(I18N_OPTION_KEY, I18N_KEY);
        JUMPWorkbench.PLUG_IN_DIRECTORY_OPTION = iniProperties.getProperty(PLUGIN_DIRECTORY_OPTION_KEY, "plug-in-directory");
        JUMPWorkbench.PROJECT_DIRECT_LOAD_OPTION = iniProperties.getProperty(PROJECT_DIRECT_LOAD_OPTION_KEY, "proj");
        JUMPWorkbench.APPLICATION_DIRECT_LOAD_OPTION = iniProperties.getProperty(APPLICATION_DIRECT_LOAD_OPTION_KEY, "idApp");
        JUMPWorkbench.PROPERTIES_OPTION = iniProperties.getProperty(PROPERTIES_OPTION_KEY, "properties");
        JUMPWorkbench.I18N_SETLOCALE = iniProperties.getProperty(I18N_KEY, "");
        JUMPWorkbench.ROOT_LOG_LEVEL = iniProperties.getProperty(ROOT_LOG_LEVEL, "");
    }

    private static void loadImagesAndIconsFromProperties() {
        JUMPWorkbench.ABOUT_IMAGE = IniFileLoader.loadImage(iniProperties.getProperty(ABOUT_IMAGE_KEY), "splash.png");
        JUMPWorkbench.DESKTOP_IMAGE = IniFileLoader.loadImage(iniProperties.getProperty(DESKTOP_IMAGE_KEY), null);
        JUMPWorkbench.SPLASH_IMAGE = IniFileLoader.loadImage(iniProperties.getProperty(SPLASH_IMAGE_KEY), "splash.png");
        JUMPWorkbench.APP_ICON = IniFileLoader.loadIcon(iniProperties.getProperty(APP_ICON_KEY), "app-icon.gif");
    }

    private static Image loadImage(String imagePath, String defaultImageName) {
        ImageIcon icon;
        Image imageToAssign = null;
        File splashFile = new File(imagePath);
        if (splashFile.canRead()) {
            try {
                imageToAssign = IconLoader.icon(splashFile.toURI().toURL()).getImage();
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
            }
        } else {
            icon = IconLoader.icon(imagePath, false);
            if (icon != null) {
                imageToAssign = icon.getImage();
            }
        }
        if (imageToAssign == null && !StringUtils.isEmpty((String)defaultImageName) && (icon = IconLoader.icon(defaultImageName, false)) != null) {
            imageToAssign = icon.getImage();
        }
        return imageToAssign;
    }

    private static ImageIcon loadIcon(String iconPath, String defaultIconName) {
        ImageIcon iconToAssign = null;
        File splashFile = new File(iconPath);
        if (splashFile.canRead()) {
            try {
                iconToAssign = IconLoader.icon(splashFile.toURI().toURL());
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
            }
        } else {
            iconToAssign = IconLoader.icon(iconPath, false);
        }
        if (iconToAssign == null && !StringUtils.isEmpty((String)defaultIconName)) {
            iconToAssign = IconLoader.icon(defaultIconName, false);
        }
        return iconToAssign;
    }
}

