/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MainMenuNames;
import com.vividsolutions.jump.workbench.ui.plugin.MenuItemShownListener;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class FeatureInstaller {
    private WorkbenchContext workbenchContext;
    private TaskMonitorManager taskMonitorManager = new TaskMonitorManager();
    private EnableCheckFactory checkFactory;

    public FeatureInstaller(WorkbenchContext workbenchContext) {
        this.workbenchContext = workbenchContext;
        this.checkFactory = new EnableCheckFactory(workbenchContext);
    }

    public MultiEnableCheck createLayersSelectedCheck() {
        return new MultiEnableCheck().add(this.checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(this.checkFactory.createAtLeastNLayersMustBeSelectedCheck(1));
    }

    public MultiEnableCheck createOneLayerSelectedCheck() {
        return new MultiEnableCheck().add(this.checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(this.checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
    }

    public MultiEnableCheck createVectorsExistCheck() {
        return new MultiEnableCheck().add(this.checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(this.checkFactory.createAtLeastNVectorsMustBeDrawnCheck(1));
    }

    public MultiEnableCheck createFenceExistsCheck() {
        return new MultiEnableCheck().add(this.checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(this.checkFactory.createFenceMustBeDrawnCheck());
    }

    public void addMenuSeparator(String menu) {
        this.addMenuSeparator(new String[]{menu});
    }

    public void addMenuSeparator(String[] menuPath) {
        JMenu mainMenu = this.menuBarMenu(menuPath[0]);
        this.addMenuSeparator(this.createMenusIfNecessary(mainMenu, this.behead(menuPath)));
    }

    public void addMenuSeparator(JMenu menu) {
        Component separator = null;
        Component exitMenu = null;
        if (menu.getText().equals(MainMenuNames.FILE)) {
            separator = menu.getMenuComponent(menu.getMenuComponentCount() - 2);
            exitMenu = menu.getMenuComponent(menu.getMenuComponentCount() - 1);
            menu.remove(separator);
            menu.remove(exitMenu);
        }
        menu.addSeparator();
        if (menu.getText().equals(MainMenuNames.FILE)) {
            menu.add(separator);
            menu.add(exitMenu);
        }
    }

    private void associate(JMenuItem menuItem, PlugIn plugIn) {
        menuItem.addActionListener(AbstractPlugIn.toActionListener(plugIn, this.workbenchContext, this.taskMonitorManager));
    }

    public String[] behead(String[] a1) {
        String[] a2 = new String[a1.length - 1];
        System.arraycopy(a1, 1, a2, 0, a2.length);
        return a2;
    }

    public void addMainMenuItem(PlugIn executable, String menuName, String menuItemName, Icon icon, EnableCheck enableCheck) {
        this.addMainMenuItem(executable, new String[]{menuName}, menuItemName, false, icon, enableCheck);
    }

    public void addMainMenuItem(PlugIn executable, String[] menuPath, String menuItemName, boolean checkBox, Icon icon, EnableCheck enableCheck) {
        Map properties = this.extractProperties(menuItemName);
        menuItemName = FeatureInstaller.removeProperties(menuItemName);
        JMenu menu = this.menuBarMenu(menuPath[0]);
        if (menu == null) {
            menu = (JMenu)FeatureInstaller.installMnemonic(new JMenu(menuPath[0]), this.menuBar());
            this.addToMenuBar(menu);
        }
        JMenu parent = this.createMenusIfNecessary(menu, this.behead(menuPath));
        JMenuItem menuItem = FeatureInstaller.installMnemonic(checkBox ? new JCheckBoxMenuItem(menuItemName) : new JMenuItem(menuItemName), parent);
        menuItem.setIcon(icon);
        menuItem.setName(menuItemName);
        this.associate(menuItem, executable);
        this.insert(menuItem, this.createMenu(parent), properties);
        MultiEnableCheck trueCheck = null;
        trueCheck = enableCheck == null ? new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(JUMPWorkbench.getPostGenericCheck()) : new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(enableCheck).add(JUMPWorkbench.getPostGenericCheck());
        this.addMenuItemShownListener(menuItem, this.toMenuItemShownListener(trueCheck));
    }

    public JMenu addEmptyMainMenuItem(String[] menuPath) {
        JMenu menu = this.menuBarMenu(menuPath[0]);
        if (menu == null) {
            menu = (JMenu)FeatureInstaller.installMnemonic(new JMenu(menuPath[0]), this.menuBar());
            this.addToMenuBar(menu);
        }
        return this.createMenusIfNecessary(menu, this.behead(menuPath));
    }

    public void removeMainMenuItem(PlugIn plugin, String[] menuPath, String menuItemName) {
        JMenu parentMenu = this.getMenu(menuPath, menuPath[menuPath.length - 1]);
        JMenuItem item = FeatureInstaller.childMenuItem(menuItemName, parentMenu);
        if (item != null) {
            parentMenu.remove(item);
        }
        this.removeParentMenu(menuPath);
    }

    private JMenu getMenu(String[] menuPath, String menuName) {
        JMenu parentMenu = this.menuBarMenu(menuPath[0]);
        if (menuPath[0].equals(menuName)) {
            return parentMenu;
        }
        if (parentMenu != null && menuPath.length > 1) {
            boolean enc = false;
            int j = 1;
            while (j < menuPath.length && !enc) {
                String menuPathName = menuPath[j];
                parentMenu = (JMenu)FeatureInstaller.childMenuItem(menuPathName, parentMenu);
                if (menuPathName.equals(menuName)) {
                    enc = true;
                }
                ++j;
            }
        }
        return parentMenu;
    }

    private void removeParentMenu(String[] menuPath) {
        int i = menuPath.length - 1;
        while (i >= 0) {
            JMenu parentMenu = this.getMenu(menuPath, menuPath[i]);
            if (parentMenu != null && parentMenu.getPopupMenu() != null && parentMenu.getPopupMenu().getSubElements().length == 0) {
                if (i != 0) {
                    JMenu ancestorMenu = this.getMenu(menuPath, menuPath[i - 1]);
                    ancestorMenu.remove(parentMenu);
                } else {
                    Container parent = parentMenu.getParent();
                    parent.remove(parentMenu);
                }
            }
            --i;
        }
    }

    public void removePopupMenuItem(JPopupMenu popupMenu, String menuItemName) {
        JMenuItem item = FeatureInstaller.childMenuItem(menuItemName, popupMenu);
        if (item != null) {
            popupMenu.remove(item);
        }
    }

    private Menu createMenu(final JMenu menu) {
        return new Menu(){

            @Override
            public void insert(JMenuItem menuItem, int i) {
                menu.insert(menuItem, i);
            }

            @Override
            public String getText() {
                return menu.getText();
            }

            @Override
            public int getItemCount() {
                return menu.getItemCount();
            }

            @Override
            public void add(JMenuItem menuItem) {
                menu.add(menuItem);
            }

            @Override
            public void insertSeparator(int i) {
                menu.insertSeparator(i);
            }
        };
    }

    private void insert(JMenuItem menuItem, Menu parent, Map properties) {
        if (properties.get("pos") != null) {
            parent.insert(menuItem, Integer.parseInt((String)properties.get("pos")));
        } else if (parent.getText().equals(MainMenuNames.FILE)) {
            parent.insert(menuItem, parent.getItemCount() - 2);
        } else {
            parent.add(menuItem);
        }
    }

    private Map extractProperties(String menuItemName) {
        if (menuItemName.indexOf(123) == -1) {
            return new HashMap();
        }
        HashMap<String, String> properties = new HashMap<String, String>();
        String s = menuItemName.substring(menuItemName.indexOf(123) + 1, menuItemName.indexOf(125));
        for (String property : StringUtil.fromCommaDelimitedString(s)) {
            properties.put(property.substring(0, property.indexOf(58)).trim(), property.substring(property.indexOf(58) + 1, property.length()).trim());
        }
        return properties;
    }

    public static String removeProperties(String menuItemName) {
        return menuItemName.indexOf(123) > -1 ? menuItemName.substring(0, menuItemName.indexOf(123)) : menuItemName;
    }

    public static JMenuItem installMnemonic(JMenuItem menuItem, MenuElement parent) {
        String text = menuItem.getText();
        StringUtil.replaceAll(text, "&&", "##");
        int ampersandPosition = text.indexOf(38);
        if (-1 < ampersandPosition && ampersandPosition + 1 < text.length()) {
            menuItem.setMnemonic(text.charAt(ampersandPosition + 1));
            text = StringUtil.replace(text, "&", "", false);
        } else {
            FeatureInstaller.installDefaultMnemonic(menuItem, parent);
        }
        StringUtil.replaceAll(text, "##", "&");
        menuItem.setText(text);
        return menuItem;
    }

    private static void installDefaultMnemonic(JMenuItem menuItem, MenuElement parent) {
        int i = 0;
        while (i < menuItem.getText().length()) {
            block5: {
                char candidate = Character.toUpperCase(menuItem.getText().charAt(i));
                if (Character.isLetter(candidate)) {
                    for (JMenuItem other : FeatureInstaller.menuItems(parent)) {
                        if (other.getMnemonic() != candidate) {
                            continue;
                        }
                        break block5;
                    }
                    menuItem.setMnemonic(candidate);
                    return;
                }
            }
            ++i;
        }
        if (menuItem != null && menuItem.getText() != null && !menuItem.getText().equals("")) {
            menuItem.setMnemonic(menuItem.getText().charAt(0));
        }
    }

    private static Collection menuItems(MenuElement element) {
        ArrayList<Object> menuItems = new ArrayList<Object>();
        if (element instanceof JMenuBar) {
            int i = 0;
            while (i < ((JMenuBar)element).getMenuCount()) {
                CollectionUtil.addIfNotNull(((JMenuBar)element).getMenu(i), menuItems);
                ++i;
            }
        } else if (element instanceof JMenu) {
            int i = 0;
            while (i < ((JMenu)element).getItemCount()) {
                CollectionUtil.addIfNotNull(((JMenu)element).getItem(i), menuItems);
                ++i;
            }
        } else if (element instanceof JPopupMenu) {
            MenuElement[] children = ((JPopupMenu)element).getSubElements();
            int i = 0;
            while (i < children.length) {
                if (children[i] instanceof JMenuItem) {
                    menuItems.add(children[i]);
                }
                ++i;
            }
        } else {
            Assert.shouldNeverReachHere((String)element.getClass().getName());
        }
        return menuItems;
    }

    private MenuItemShownListener toMenuItemShownListener(final EnableCheck enableCheck) {
        return new MenuItemShownListener(){

            @Override
            public void menuItemShown(JMenuItem menuItem) {
                String errorMessage = null;
                try {
                    errorMessage = enableCheck.check(menuItem);
                }
                catch (Exception e) {
                    FeatureInstaller.this.workbenchContext.getWorkbench().getFrame().log(menuItem.getText());
                    FeatureInstaller.this.workbenchContext.getWorkbench().getFrame().handleThrowable(e);
                }
                if (errorMessage != null) {
                    menuItem.setEnabled(false);
                    menuItem.setToolTipText(errorMessage);
                    return;
                }
                menuItem.setEnabled(true);
                menuItem.setToolTipText(null);
            }
        };
    }

    public JMenu createMenusIfNecessary(JMenu parent, String[] menuPath) {
        if (menuPath.length == 0) {
            return parent;
        }
        JMenu child = (JMenu)FeatureInstaller.childMenuItem(menuPath[0], parent);
        if (child == null) {
            child = (JMenu)FeatureInstaller.installMnemonic(new JMenu(menuPath[0]), parent);
            parent.add(child);
        }
        return this.createMenusIfNecessary(child, this.behead(menuPath));
    }

    public void addMenuItemShownListener(final JMenuItem menuItem, final MenuItemShownListener menuItemShownListener) {
        JMenu menu = (JMenu)((JPopupMenu)menuItem.getParent()).getInvoker();
        menu.addMenuListener(new MenuListener(){

            @Override
            public void menuSelected(MenuEvent e) {
                menuItemShownListener.menuItemShown(menuItem);
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
    }

    public void addPopupMenuItem(JPopupMenu popupMenu, PlugIn executable, String menuItemName, boolean checkBox, Icon icon, EnableCheck enableCheck) {
        Map properties = this.extractProperties(menuItemName);
        menuItemName = FeatureInstaller.removeProperties(menuItemName);
        JMenuItem menuItem = FeatureInstaller.installMnemonic(checkBox ? new JCheckBoxMenuItem(menuItemName) : new JMenuItem(menuItemName), popupMenu);
        menuItem.setIcon(icon);
        this.addPopupMenuItem(popupMenu, executable, menuItem, properties, enableCheck);
    }

    private void addPopupMenuItem(JPopupMenu popupMenu, PlugIn executable, final JMenuItem menuItem, Map properties, EnableCheck enableCheck) {
        this.associate(menuItem, executable);
        this.insert(menuItem, this.createMenu(popupMenu), properties);
        menuItem.setName(menuItem.getText());
        enableCheck = enableCheck == null ? new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(JUMPWorkbench.getPostGenericCheck()) : new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(enableCheck).add(JUMPWorkbench.getPostGenericCheck());
        final EnableCheck trueCheck = enableCheck;
        if (enableCheck != null) {
            popupMenu.addPopupMenuListener(new PopupMenuListener(){

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    FeatureInstaller.this.toMenuItemShownListener(trueCheck).menuItemShown(menuItem);
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    }

    private Menu createMenu(final JPopupMenu popupMenu) {
        return new Menu(){

            @Override
            public void insert(JMenuItem menuItem, int i) {
                popupMenu.insert(menuItem, i);
            }

            @Override
            public String getText() {
                return "";
            }

            @Override
            public int getItemCount() {
                return popupMenu.getComponentCount();
            }

            @Override
            public void add(JMenuItem menuItem) {
                popupMenu.add(menuItem);
            }

            @Override
            public void insertSeparator(int i) {
                popupMenu.insert(new JSeparator(), i);
            }
        };
    }

    public JMenuBar menuBar() {
        return this.workbenchContext.getWorkbench().getFrame().getJMenuBar();
    }

    public JMenu menuBarMenu(String childName) {
        MenuElement[] subElements = this.menuBar().getSubElements();
        int i = 0;
        while (i < subElements.length) {
            JMenuItem menuItem;
            if (subElements[i] instanceof JMenuItem && (menuItem = (JMenuItem)subElements[i]).getText().equals(childName)) {
                return (JMenu)menuItem;
            }
            ++i;
        }
        return null;
    }

    private void addToMenuBar(JMenu menu) {
        this.menuBar().add(menu);
        JMenu windowMenu = this.menuBarMenu(MainMenuNames.WINDOW);
        JMenu helpMenu = this.menuBarMenu(MainMenuNames.HELP);
        if (windowMenu != null) {
            this.menuBar().remove(windowMenu);
        }
        if (helpMenu != null) {
            this.menuBar().remove(helpMenu);
        }
        if (windowMenu != null) {
            this.menuBar().add(windowMenu);
        }
        if (helpMenu != null) {
            this.menuBar().add(helpMenu);
        }
    }

    public static JMenuItem childMenuItem(String childName, MenuElement menu) {
        if (menu instanceof JMenu) {
            return FeatureInstaller.childMenuItem(childName, ((JMenu)menu).getPopupMenu());
        }
        MenuElement[] childMenuItems = menu.getSubElements();
        int i = 0;
        while (i < childMenuItems.length) {
            if (childMenuItems[i] instanceof JMenuItem && ((JMenuItem)childMenuItems[i]).getText().equals(childName)) {
                return (JMenuItem)childMenuItems[i];
            }
            ++i;
        }
        return null;
    }

    public void addMainMenuItemWithJava14Fix(PlugIn executable, String[] menuPath, String menuItemName, boolean checkBox, Icon icon, EnableCheck enableCheck) {
        this.addMainMenuItem(executable, menuPath, menuItemName, checkBox, icon, enableCheck);
        JMenuItem menuItem = FeatureInstaller.childMenuItem(FeatureInstaller.removeProperties(menuItemName), this.createMenusIfNecessary(this.menuBarMenu(menuPath[0]), this.behead(menuPath)));
        final ActionListener listener = this.abstractPlugInActionListener(menuItem.getActionListeners());
        menuItem.removeActionListener(listener);
        menuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(final ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable(){

                    @Override
                    public void run() {
                        listener.actionPerformed(e);
                    }
                });
            }
        });
    }

    private ActionListener abstractPlugInActionListener(ActionListener[] actionListeners) {
        int i = 0;
        while (i < actionListeners.length) {
            if (actionListeners[i].getClass().getName().indexOf(AbstractPlugIn.class.getName()) > -1) {
                return actionListeners[i];
            }
            ++i;
        }
        Assert.shouldNeverReachHere();
        return null;
    }

    public void addMainMenuItem(PlugIn plugIn, String menuName) {
        this.addMainMenuItem(plugIn, menuName, plugIn.getName(), plugIn.getIcon(), plugIn.getCheck());
    }

    public void addMainMenuItem(PlugIn plugIn, String menuName, boolean checkbox) {
        this.addMainMenuItem(plugIn, new String[]{menuName}, checkbox);
    }

    public void addMainMenuItem(PlugIn plugIn, String[] menuNames, boolean checkbox) {
        this.addMainMenuItem(plugIn, menuNames, plugIn.getName(), checkbox, plugIn.getIcon(), plugIn.getCheck());
    }

    public void addMainMenuItem(PlugIn plugIn, String menuName, boolean checkbox, boolean toSmallIcon) {
        this.addMainMenuItem(plugIn, new String[]{menuName}, checkbox, toSmallIcon);
    }

    public void addMainMenuItem(PlugIn plugIn, String[] menuNames, boolean checkbox, boolean toSmallIcon) {
        if (toSmallIcon) {
            this.addMainMenuItem(plugIn, menuNames, plugIn.getName(), checkbox, GUIUtil.toSmallIcon(plugIn.getIcon()), plugIn.getCheck());
        } else {
            this.addMainMenuItem(plugIn, menuNames, plugIn.getName(), checkbox, plugIn.getIcon(), plugIn.getCheck());
        }
    }

    public void addPopupMenuItem(JPopupMenu popupMenu, PlugIn plugIn, boolean checkbox) {
        this.addPopupMenuItem(popupMenu, plugIn, plugIn.getName(), checkbox, plugIn.getIcon(), plugIn.getCheck());
    }

    public void addPopupMenuItem(JPopupMenu popupMenu, PlugIn plugIn, boolean checkbox, boolean toSmallIcon) {
        if (toSmallIcon) {
            this.addPopupMenuItem(popupMenu, plugIn, plugIn.getName(), checkbox, GUIUtil.toSmallIcon(plugIn.getIcon()), plugIn.getCheck());
        } else {
            this.addPopupMenuItem(popupMenu, plugIn, plugIn.getName(), checkbox, plugIn.getIcon(), plugIn.getCheck());
        }
    }

    public void addToCustomMenuMenuItem(PlugIn plugIn, String menuName, JMenu customMenu) {
        this.addToCustomMenuMenuItem(plugIn, menuName, plugIn.getName(), plugIn.getIcon(), plugIn.getCheck(), customMenu);
    }

    public void addToCustomMenuMenuItem(PlugIn executable, String menuName, String menuItemName, Icon icon, EnableCheck enableCheck, JMenu customMenu) {
        this.addToCustomMenuMenuItem(executable, new String[]{menuName}, menuItemName, false, icon, enableCheck, customMenu);
    }

    public void addToCustomMenuMenuItem(PlugIn plugIn, String menuName, boolean checkbox, JMenu customMenu) {
        this.addToCustomMenuMenuItem(plugIn, new String[]{menuName}, checkbox, customMenu);
    }

    public void addToCustomMenuMenuItem(PlugIn plugIn, String[] menuNames, boolean checkbox, JMenu customMenu) {
        this.addToCustomMenuMenuItem(plugIn, menuNames, plugIn.getName(), checkbox, plugIn.getIcon(), plugIn.getCheck(), customMenu);
    }

    public void addToCustomMenuMenuItem(PlugIn plugIn, String menuName, boolean checkbox, boolean toSmallIcon, JMenu customMenu) {
        this.addToCustomMenuMenuItem(plugIn, new String[]{menuName}, checkbox, toSmallIcon, customMenu);
    }

    public void addToCustomMenuMenuItem(PlugIn plugIn, String[] menuNames, boolean checkbox, boolean toSmallIcon, JMenu customMenu) {
        if (toSmallIcon) {
            this.addToCustomMenuMenuItem(plugIn, menuNames, plugIn.getName(), checkbox, GUIUtil.toSmallIcon(plugIn.getIcon()), plugIn.getCheck(), customMenu);
        } else {
            this.addToCustomMenuMenuItem(plugIn, menuNames, plugIn.getName(), checkbox, plugIn.getIcon(), plugIn.getCheck(), customMenu);
        }
    }

    public void addToCustomMenuMenuItem(PlugIn executable, String[] menuPath, String menuItemName, boolean checkBox, Icon icon, EnableCheck enableCheck, JMenu customMenu) {
        Map properties = this.extractProperties(menuItemName);
        menuItemName = FeatureInstaller.removeProperties(menuItemName);
        JMenu parent = null;
        if (menuPath.length > 0) {
            JMenu menu = (JMenu)FeatureInstaller.installMnemonic(new JMenu(menuPath[0]), this.menuBar());
            customMenu.add(menu);
            parent = this.createMenusIfNecessary(menu, this.behead(menuPath));
        } else {
            parent = customMenu;
        }
        JMenuItem menuItem = FeatureInstaller.installMnemonic(checkBox ? new JCheckBoxMenuItem(menuItemName) : new JMenuItem(menuItemName), parent);
        menuItem.setIcon(icon);
        menuItem.setName(menuItemName);
        this.associate(menuItem, executable);
        this.insert(menuItem, this.createMenu(parent), properties);
        MultiEnableCheck trueCheck = null;
        trueCheck = enableCheck == null ? new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(JUMPWorkbench.getPostGenericCheck()) : new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(enableCheck).add(JUMPWorkbench.getPostGenericCheck());
        this.addMenuItemShownListener(menuItem, this.toMenuItemShownListener(trueCheck));
    }

    private static interface Menu {
        public void insert(JMenuItem var1, int var2);

        public void insertSeparator(int var1);

        public String getText();

        public int getItemCount();

        public void add(JMenuItem var1);
    }
}

