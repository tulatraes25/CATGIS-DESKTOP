/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class KeyboardPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.KeyboardPlugIn.name");
    public static final Icon ICON = IconLoader.icon("keyboard.png");
    private static final Logger LOGGER = Logger.getLogger(KeyboardPlugIn.class);
    private static String html = null;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    private HTMLFrame frame(WorkbenchContext context) {
        String key = String.valueOf(this.getClass().getName()) + " - Frame";
        if (JUMPWorkbench.getBlackboard().get(key) == null) {
            HTMLFrame frame = new HTMLFrame(context.getWorkbench().getFrame());
            frame.setRecordNavigationControlVisible(false);
            frame.createNewDocument();
            try {
                this.append(frame);
            }
            catch (IOException e) {
                Assert.shouldNeverReachHere();
            }
            frame.setTitle(I18N.getString("workbench.ui.plugin.KeyboardPlugIn.shortcut-keys"));
            frame.setSize(600, 600);
            JUMPWorkbench.getBlackboard().put(key, frame);
        }
        return (HTMLFrame)JUMPWorkbench.getBlackboard().get(key);
    }

    public static String html() throws IOException {
        if (html == null) {
            html = "";
            String fileName = "KeyboardPlugIn_" + I18N.getLanguage() + ".html";
            InputStream inputStream = KeyboardPlugIn.class.getResourceAsStream(fileName);
            if (inputStream == null) {
                LOGGER.error((Object)I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.KeyboardPlugIn.The-file-{0}-have-not-been-found-The-default-file-will-be-used", new Object[]{fileName}));
                inputStream = KeyboardPlugIn.class.getResourceAsStream("KeyboardPlugIn.html");
            }
            try {
                for (String line : FileUtil.getContents(inputStream)) {
                    html = String.valueOf(html) + line;
                }
            }
            finally {
                inputStream.close();
            }
        }
        return html;
    }

    private void append(HTMLFrame frame) throws IOException {
        frame.append(this.removeHTMLTags(KeyboardPlugIn.html()));
    }

    private String removeHTMLTags(String s) {
        return StringUtil.replaceAll(StringUtil.replaceAll(s, "<html>", ""), "</html>", "");
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.frame(context.getWorkbenchContext()).surface();
        return true;
    }
}

