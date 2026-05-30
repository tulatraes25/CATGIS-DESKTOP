/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class MenuScroller {
    private JPopupMenu menu;
    private Component[] menuItems;
    private MenuScrollItem upItem;
    private MenuScrollItem downItem;
    private final MenuScrollListener menuListener = new MenuScrollListener();
    private int scrollCount;
    private int interval;
    private int topFixedCount;
    private int bottomFixedCount;
    private int firstIndex = 0;
    private int keepVisibleIndex = -1;

    public static MenuScroller setScrollerFor(JMenu menu) {
        return new MenuScroller(menu);
    }

    public static MenuScroller setScrollerFor(JPopupMenu menu) {
        return new MenuScroller(menu);
    }

    public static MenuScroller setScrollerFor(JMenu menu, int scrollCount) {
        return new MenuScroller(menu, scrollCount);
    }

    public static MenuScroller setScrollerFor(JPopupMenu menu, int scrollCount) {
        return new MenuScroller(menu, scrollCount);
    }

    public static MenuScroller setScrollerFor(JMenu menu, int scrollCount, int interval) {
        return new MenuScroller(menu, scrollCount, interval);
    }

    public static MenuScroller setScrollerFor(JPopupMenu menu, int scrollCount, int interval) {
        return new MenuScroller(menu, scrollCount, interval);
    }

    public static MenuScroller setScrollerFor(JMenu menu, int scrollCount, int interval, int topFixedCount, int bottomFixedCount) {
        return new MenuScroller(menu, scrollCount, interval, topFixedCount, bottomFixedCount);
    }

    public static MenuScroller setScrollerFor(JPopupMenu menu, int scrollCount, int interval, int topFixedCount, int bottomFixedCount) {
        return new MenuScroller(menu, scrollCount, interval, topFixedCount, bottomFixedCount);
    }

    public MenuScroller(JMenu menu) {
        this(menu, 15);
    }

    public MenuScroller(JPopupMenu menu) {
        this(menu, 15);
    }

    public MenuScroller(JMenu menu, int scrollCount) {
        this(menu, scrollCount, 150);
    }

    public MenuScroller(JPopupMenu menu, int scrollCount) {
        this(menu, scrollCount, 150);
    }

    public MenuScroller(JMenu menu, int scrollCount, int interval) {
        this(menu, scrollCount, interval, 0, 0);
    }

    public MenuScroller(JPopupMenu menu, int scrollCount, int interval) {
        this(menu, scrollCount, interval, 0, 0);
    }

    public MenuScroller(JMenu menu, int scrollCount, int interval, int topFixedCount, int bottomFixedCount) {
        this(menu.getPopupMenu(), scrollCount, interval, topFixedCount, bottomFixedCount);
    }

    public MenuScroller(JPopupMenu menu, int scrollCount, int interval, int topFixedCount, int bottomFixedCount) {
        if (scrollCount <= 0 || interval <= 0) {
            throw new IllegalArgumentException("scrollCount and interval must be greater than 0");
        }
        if (topFixedCount < 0 || bottomFixedCount < 0) {
            throw new IllegalArgumentException("topFixedCount and bottomFixedCount cannot be negative");
        }
        this.upItem = new MenuScrollItem(MenuIcon.UP, -1);
        this.downItem = new MenuScrollItem(MenuIcon.DOWN, 1);
        this.setScrollCount(scrollCount);
        this.setInterval(interval);
        this.setTopFixedCount(topFixedCount);
        this.setBottomFixedCount(bottomFixedCount);
        this.menu = menu;
        menu.addPopupMenuListener(this.menuListener);
    }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("interval must be greater than 0");
        }
        this.upItem.setInterval(interval);
        this.downItem.setInterval(interval);
        this.interval = interval;
    }

    public int getscrollCount() {
        return this.scrollCount;
    }

    public void setScrollCount(int scrollCount) {
        if (scrollCount <= 0) {
            throw new IllegalArgumentException("scrollCount must be greater than 0");
        }
        this.scrollCount = scrollCount;
        MenuSelectionManager.defaultManager().clearSelectedPath();
    }

    public int getTopFixedCount() {
        return this.topFixedCount;
    }

    public void setTopFixedCount(int topFixedCount) {
        this.firstIndex = this.firstIndex <= topFixedCount ? topFixedCount : (this.firstIndex += topFixedCount - this.topFixedCount);
        this.topFixedCount = topFixedCount;
    }

    public int getBottomFixedCount() {
        return this.bottomFixedCount;
    }

    public void setBottomFixedCount(int bottomFixedCount) {
        this.bottomFixedCount = bottomFixedCount;
    }

    public void keepVisible(JMenuItem item) {
        int index;
        this.keepVisibleIndex = item == null ? -1 : (index = this.menu.getComponentIndex(item));
    }

    public void keepVisible(int index) {
        this.keepVisibleIndex = index;
    }

    public void dispose() {
        if (this.menu != null) {
            this.menu.removePopupMenuListener(this.menuListener);
            this.menu = null;
        }
    }

    public void finalize() throws Throwable {
        this.dispose();
    }

    private void refreshMenu() {
        if (this.menuItems != null && this.menuItems.length > 0) {
            this.firstIndex = Math.max(this.topFixedCount, this.firstIndex);
            this.firstIndex = Math.min(this.menuItems.length - this.bottomFixedCount - this.scrollCount, this.firstIndex);
            this.upItem.setEnabled(this.firstIndex > this.topFixedCount);
            this.downItem.setEnabled(this.firstIndex + this.scrollCount < this.menuItems.length - this.bottomFixedCount);
            this.menu.removeAll();
            int i = 0;
            while (i < this.topFixedCount) {
                this.menu.add(this.menuItems[i]);
                ++i;
            }
            if (this.topFixedCount > 0) {
                this.menu.addSeparator();
            }
            this.menu.add(this.upItem);
            i = this.firstIndex;
            while (i < this.scrollCount + this.firstIndex) {
                this.menu.add(this.menuItems[i]);
                ++i;
            }
            this.menu.add(this.downItem);
            if (this.bottomFixedCount > 0) {
                this.menu.addSeparator();
            }
            i = this.menuItems.length - this.bottomFixedCount;
            while (i < this.menuItems.length) {
                this.menu.add(this.menuItems[i]);
                ++i;
            }
            JComponent parent = (JComponent)this.upItem.getParent();
            parent.revalidate();
            parent.repaint();
        }
    }

    private static enum MenuIcon implements Icon
    {
        UP(9, 1, 9),
        DOWN(1, 9, 1);

        final int[] xPoints = new int[]{1, 5, 9};
        final int[] yPoints;

        private MenuIcon(int ... yPoints) {
            this.yPoints = yPoints;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Dimension size = c.getSize();
            Graphics g2 = g.create(size.width / 2 - 5, size.height / 2 - 5, 10, 10);
            g2.setColor(Color.GRAY);
            g2.drawPolygon(this.xPoints, this.yPoints, 3);
            if (c.isEnabled()) {
                g2.setColor(Color.BLACK);
                g2.fillPolygon(this.xPoints, this.yPoints, 3);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 0;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
    }

    private class MenuScrollItem
    extends JMenuItem
    implements ChangeListener {
        private static final long serialVersionUID = 1L;
        private MenuScrollTimer timer;

        public MenuScrollItem(MenuIcon icon, int increment) {
            this.setIcon(icon);
            this.setDisabledIcon(icon);
            this.timer = new MenuScrollTimer(increment, MenuScroller.this.interval);
            this.addChangeListener(this);
        }

        public void setInterval(int interval) {
            this.timer.setDelay(interval);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (this.isArmed() && !this.timer.isRunning()) {
                this.timer.start();
            }
            if (!this.isArmed() && this.timer.isRunning()) {
                this.timer.stop();
            }
        }
    }

    private class MenuScrollListener
    implements PopupMenuListener {
        private MenuScrollListener() {
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            this.setMenuItems();
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            this.restoreMenuItems();
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            this.restoreMenuItems();
        }

        private void setMenuItems() {
            MenuScroller.this.menuItems = MenuScroller.this.menu.getComponents();
            if (MenuScroller.this.keepVisibleIndex >= MenuScroller.this.topFixedCount && MenuScroller.this.keepVisibleIndex <= MenuScroller.this.menuItems.length - MenuScroller.this.bottomFixedCount && (MenuScroller.this.keepVisibleIndex > MenuScroller.this.firstIndex + MenuScroller.this.scrollCount || MenuScroller.this.keepVisibleIndex < MenuScroller.this.firstIndex)) {
                MenuScroller.this.firstIndex = Math.min(MenuScroller.this.firstIndex, MenuScroller.this.keepVisibleIndex);
                MenuScroller.this.firstIndex = Math.max(MenuScroller.this.firstIndex, MenuScroller.this.keepVisibleIndex - MenuScroller.this.scrollCount + 1);
            }
            if (MenuScroller.this.menuItems.length > MenuScroller.this.topFixedCount + MenuScroller.this.scrollCount + MenuScroller.this.bottomFixedCount) {
                MenuScroller.this.refreshMenu();
            }
        }

        private void restoreMenuItems() {
            MenuScroller.this.menu.removeAll();
            Component[] componentArray = MenuScroller.this.menuItems;
            int n = componentArray.length;
            int n2 = 0;
            while (n2 < n) {
                Component component = componentArray[n2];
                MenuScroller.this.menu.add(component);
                ++n2;
            }
        }
    }

    private class MenuScrollTimer
    extends Timer {
        private static final long serialVersionUID = 1L;

        public MenuScrollTimer(final int increment, int interval) {
            super(interval, new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    MenuScroller menuScroller = MenuScroller.this;
                    menuScroller.firstIndex = menuScroller.firstIndex + increment;
                    MenuScroller.this.refreshMenu();
                }
            });
        }
    }
}

