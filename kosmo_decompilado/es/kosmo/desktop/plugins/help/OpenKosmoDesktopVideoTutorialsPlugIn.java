/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.help;

import es.kosmo.desktop.images.DesktopIconLoader;
import es.kosmo.desktop.plugins.help.AbstractOpenWebSitePlugIn;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Icon;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;

public class OpenKosmoDesktopVideoTutorialsPlugIn
extends AbstractOpenWebSitePlugIn {
    public static final String NAME = I18N.getString("es.kosmo.desktop.plugins.help.OpenKosmoDesktopVideoTutorialsPlugIn.Open-Kosmo-Desktop-video-tutorials-web-page");
    public static final Icon ICON = DesktopIconLoader.icon("helpVideos.png");

    @Override
    public URL getURL() throws MalformedURLException {
        boolean spanishLocale = LocaleManager.getActiveLocale().getLanguage().equalsIgnoreCase("es") || LocaleManager.getActiveLocale().getLanguage().equalsIgnoreCase("ca");
        String url = "http://www.opengis.es/index.php?option=com_weblinks&catid=24&Itemid=58";
        if (!spanishLocale) {
            url = String.valueOf(url) + "&lang=en";
        }
        return new URL(url);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }
}

