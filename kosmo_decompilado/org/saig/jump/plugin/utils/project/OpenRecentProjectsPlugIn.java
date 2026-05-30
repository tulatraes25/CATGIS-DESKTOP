/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.project;

import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class OpenRecentProjectsPlugIn
extends AbstractPlugIn {
    public static final Logger LOGGER = Logger.getLogger(OpenRecentProjectsPlugIn.class);
    public static final String RECENT_PROJECTS_KEY = String.valueOf(OpenRecentProjectsPlugIn.class.getName()) + " - " + "RECENT PROJECTS";
    public static final int TOTAL_SLOTS = 5;
    public static final String EMPTY = I18N.getString("org.saig.jump.plugin.utils.OpenRecentProjectsPlugIn.empty");
    public static final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.utils.OpenRecentProjectsPlugIn.recent-projects")) + "...";
    private static final int MAX_MENU_PATH_LENGHT = 50;
    public static final Icon ICON = null;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    public JMenu getMenu() {
        JMenu openSelectedProjectMenu = new JMenu(NAME);
        openSelectedProjectMenu.setIcon(GUIUtil.toSmallIcon(ICON));
        openSelectedProjectMenu.addMenuListener(new MenuListener(){

            @Override
            public void menuCanceled(MenuEvent e) {
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuSelected(MenuEvent e) {
                JMenu source = (JMenu)e.getSource();
                source.removeAll();
                String errorMessage = null;
                MultiEnableCheck trueCheck = new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(JUMPWorkbench.getPostGenericCheck());
                JMenuItem defaultItem = new JMenuItem(EMPTY);
                defaultItem.setName(NAME);
                try {
                    errorMessage = trueCheck.check(defaultItem);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (errorMessage != null) {
                    defaultItem.setEnabled(false);
                    defaultItem.setToolTipText(errorMessage);
                    source.add(defaultItem);
                    return;
                }
                LinkedList recentProjectsLinkedList = null;
                Object value = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(RECENT_PROJECTS_KEY);
                if (value == null || !(value instanceof LinkedList)) {
                    JMenuItem item = new JMenuItem(EMPTY);
                    item.setEnabled(false);
                    source.add(item);
                    return;
                }
                recentProjectsLinkedList = (LinkedList)value;
                Iterator it = null;
                try {
                    for (String path : recentProjectsLinkedList) {
                        File file = new File(path);
                        if (!file.exists()) continue;
                        JMenuItem item = new JMenuItem();
                        item.setToolTipText(path);
                        item.setText(StringUtil.reducePath(path, 50));
                        item.addActionListener(new ActionListener(){

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                OpenRecentProjectsPlugIn.this.openSelectedProject(((JMenuItem)e.getSource()).getToolTipText());
                            }
                        });
                        source.add(item);
                    }
                }
                catch (Exception e1) {
                    LOGGER.error((Object)"", (Throwable)e1);
                }
                if (source.getItemCount() == 0) {
                    JMenuItem item = new JMenuItem(EMPTY);
                    item.setEnabled(false);
                    source.add(item);
                }
            }
        });
        return openSelectedProjectMenu;
    }

    void openSelectedProject(String projectPath) {
        OpenProjectPlugIn openProject = new OpenProjectPlugIn();
        openProject.setDirectAbsolutePath(projectPath);
        PlugInContext pContext = JUMPWorkbench.getFrameInstance().getContext().createPlugInContext();
        try {
            if (openProject.execute(pContext)) {
                new TaskMonitorManager().execute(openProject, pContext);
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }
}

