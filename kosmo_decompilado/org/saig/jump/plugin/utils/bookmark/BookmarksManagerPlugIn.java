/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.bookmark;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.Frame;
import javax.swing.Icon;
import javax.swing.JDialog;
import org.apache.log4j.Logger;
import org.saig.core.model.bookmark.BookmarkManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.ApplicationExitListener;
import org.saig.jump.widgets.util.ILeftTabTaskFrameComponent;
import org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel;

public class BookmarksManagerPlugIn
extends AbstractPlugIn
implements ApplicationExitListener,
ILeftTabTaskFrameComponent {
    private static final Logger LOGGER = Logger.getLogger(BookmarksManagerPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.bookmark.BookmarksManagerPlugIn.bookmark-manager");
    public static final Icon ICON = IconLoader.icon("book_addresses.png");

    @Override
    public void initialize(PlugInContext context) throws Exception {
        context.getWorkbenchFrame().getApplicationExitHandler().addExitListener(this);
        BookmarkManager.getInstance().restoreBookmarks();
        TaskFrame.registerTabbedPanel(this);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        BookmarksManagerPanel panel = new BookmarksManagerPanel();
        JDialog dialog = new JDialog((Frame)context.getWorkbenchFrame(), true);
        dialog.setTitle(NAME);
        dialog.setContentPane(panel);
        dialog.pack();
        GUIUtil.centreOnScreen(dialog);
        dialog.setVisible(true);
        return true;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return BookmarksManagerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        return new MultiEnableCheck();
    }

    @Override
    public boolean exitingApplication() {
        LOGGER.info((Object)(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.bookmark.BookmarksManagerPlugIn.saving-bookmarks-to-the-xml-file")) + "..."));
        BookmarkManager.getInstance().saveBookmarks();
        return true;
    }

    @Override
    public Component getComponent(LayerManager layerManager) {
        return new BookmarksManagerPanel();
    }
}

