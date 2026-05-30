/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.buttons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.border.Border;

public class DropDownButton
extends Box {
    private static final long serialVersionUID = 1L;
    private JButton mainButton;
    private JButton dropDownButton;
    private boolean dropDownEnabled = false;
    private boolean mainRunsDefaultMenuOption = true;
    private Icon enabledDownArrow;
    private Icon disDownArrow;
    private DropDownMenu menu;
    private MainButtonListener mainButtonListener = new MainButtonListener();

    public DropDownButton(JButton mainButton) {
        super(0);
        this.menu = new DropDownMenu();
        this.menu.getPopupMenu().addContainerListener(new MenuContainerListener());
        JMenuBar bar = new JMenuBar();
        bar.add(this.menu);
        bar.setMaximumSize(new Dimension(0, 100));
        bar.setMinimumSize(new Dimension(0, 1));
        bar.setPreferredSize(new Dimension(0, 1));
        this.add(bar);
        this.mainButton = mainButton;
        mainButton.addActionListener(this.mainButtonListener);
        mainButton.setBorder(new RightChoppedBorder(mainButton.getBorder(), 2));
        this.add(mainButton);
        this.enabledDownArrow = new SmallDownArrow(null, null);
        this.disDownArrow = new SmallDisabledDownArrow();
        this.dropDownButton = new JButton(this.disDownArrow);
        this.dropDownButton.setDisabledIcon(this.disDownArrow);
        this.dropDownButton.addMouseListener(new DropDownListener());
        this.dropDownButton.setMaximumSize(new Dimension(11, 100));
        this.dropDownButton.setMinimumSize(new Dimension(11, 10));
        this.dropDownButton.setPreferredSize(new Dimension(11, 10));
        this.dropDownButton.setFocusPainted(false);
        this.add(this.dropDownButton);
        this.setEnabled(false);
    }

    public DropDownButton() {
        this(new JButton());
    }

    public DropDownButton(Action a) {
        this(new JButton(a));
    }

    public DropDownButton(Icon icon) {
        this(new JButton(icon));
    }

    public DropDownButton(String text) {
        this(new JButton(text));
    }

    public DropDownButton(String t, Icon i) {
        this(new JButton(t, i));
    }

    public JButton getButton() {
        return this.mainButton;
    }

    public JMenu getMenu() {
        return this.menu;
    }

    @Override
    public void setEnabled(boolean enable) {
        this.mainButton.setEnabled(enable);
        this.dropDownButton.setEnabled(enable);
    }

    @Override
    public boolean isEnabled() {
        return this.mainButton.isEnabled();
    }

    public boolean isEmpty() {
        return this.menu.getItemCount() == 0;
    }

    public void setRunFirstMenuOption(boolean enable) {
        this.mainButton.removeActionListener(this.mainButtonListener);
        this.mainRunsDefaultMenuOption = enable;
        this.setEnabled(!this.mainRunsDefaultMenuOption || !this.isEmpty());
        if (this.mainRunsDefaultMenuOption) {
            this.mainButton.addActionListener(this.mainButtonListener);
        }
    }

    public boolean getRunFirstMenuOption() {
        return this.mainRunsDefaultMenuOption;
    }

    private void setDropDownEnabled(boolean enable) {
        this.dropDownEnabled = enable;
        this.dropDownButton.setIcon(enable ? this.enabledDownArrow : this.disDownArrow);
        if (this.mainRunsDefaultMenuOption) {
            this.setEnabled(enable);
        }
    }

    private class DropDownListener
    extends MouseAdapter {
        boolean pressHidPopup = false;

        private DropDownListener() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (DropDownButton.this.dropDownEnabled && !this.pressHidPopup) {
                DropDownButton.this.menu.doClick(0);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (DropDownButton.this.dropDownEnabled) {
                DropDownButton.this.menu.dispatchMouseEvent(e);
            }
            this.pressHidPopup = !DropDownButton.this.menu.isPopupMenuVisible();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
    }

    private class DropDownMenu
    extends JMenu {
        private static final long serialVersionUID = 1L;

        private DropDownMenu() {
        }

        public void dispatchMouseEvent(MouseEvent e) {
            this.processMouseEvent(e);
        }
    }

    private class MainButtonListener
    implements ActionListener {
        private MainButtonListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem defaultItem;
            if (DropDownButton.this.mainRunsDefaultMenuOption && !DropDownButton.this.isEmpty() && (defaultItem = DropDownButton.this.menu.getItem(0)) != null) {
                defaultItem.doClick(0);
            }
        }
    }

    private class MenuContainerListener
    implements ContainerListener {
        private MenuContainerListener() {
        }

        @Override
        public void componentAdded(ContainerEvent e) {
            DropDownButton.this.setDropDownEnabled(true);
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
            DropDownButton.this.setDropDownEnabled(!DropDownButton.this.isEmpty());
        }
    }

    private class RightChoppedBorder
    implements Border {
        private Border b;
        private int w;

        public RightChoppedBorder(Border b, int width) {
            this.b = b;
            this.w = width;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Shape clipping = g.getClip();
            g.setClip(x, y, width, height);
            this.b.paintBorder(c, g, x, y, width + this.w, height);
            g.setClip(clipping);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            Insets i = this.b.getBorderInsets(c);
            return new Insets(i.top, i.left, i.bottom, i.right - this.w);
        }

        @Override
        public boolean isBorderOpaque() {
            return this.b.isBorderOpaque();
        }
    }

    private static class SmallDisabledDownArrow
    extends SmallDownArrow {
        public SmallDisabledDownArrow() {
            this.arrowColor = new Color(140, 140, 140);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            g.setColor(Color.white);
            g.drawLine(x + 3, y + 2, x + 4, y + 1);
            g.drawLine(x + 3, y + 3, x + 5, y + 1);
        }
    }

    private static class SmallDownArrow
    implements Icon {
        Color arrowColor = Color.black;

        private SmallDownArrow() {
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(this.arrowColor);
            g.drawLine(x, y, x + 4, y);
            g.drawLine(x + 1, y + 1, x + 3, y + 1);
            g.drawLine(x + 2, y + 2, x + 2, y + 2);
        }

        @Override
        public int getIconWidth() {
            return 6;
        }

        @Override
        public int getIconHeight() {
            return 4;
        }

        /* synthetic */ SmallDownArrow(SmallDownArrow smallDownArrow, SmallDownArrow smallDownArrow2) {
            this();
        }
    }
}

