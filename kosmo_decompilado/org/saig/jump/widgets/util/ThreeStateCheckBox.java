/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;

public class ThreeStateCheckBox
extends JCheckBox {
    private static final long serialVersionUID = 1L;
    public static final State NOT_SELECTED = new State();
    public static final State SELECTED = new State();
    public static final State DONT_CARE = new State();
    private final ThreeStateDecorator model;

    public ThreeStateCheckBox(String text, Icon icon, State initial) {
        super(text, icon);
        super.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                ThreeStateCheckBox.this.grabFocus();
                ThreeStateCheckBox.this.model.nextState();
                ThreeStateCheckBox.this.fireActionPerformed(new ActionEvent(ThreeStateCheckBox.this, 1001, ""));
            }
        });
        ActionMapUIResource map = new ActionMapUIResource();
        map.put("pressed", new AbstractAction(){
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                ThreeStateCheckBox.this.grabFocus();
                ThreeStateCheckBox.this.model.nextState();
            }
        });
        map.put("released", null);
        SwingUtilities.replaceUIActionMap(this, map);
        this.model = new ThreeStateDecorator(this.getModel());
        this.setModel(this.model);
        this.setState(initial);
    }

    @Override
    public void paintComponent(Graphics g) {
        this.model.beforePaint();
        super.paintComponent(g);
        this.model.afterPaint();
    }

    public ThreeStateCheckBox(String text, State initial) {
        this(text, null, initial);
    }

    public ThreeStateCheckBox(String text) {
        this(text, DONT_CARE);
    }

    public ThreeStateCheckBox() {
        this((String)null);
    }

    @Override
    public void addMouseListener(MouseListener l) {
    }

    public void setState(State state) {
        this.model.setState(state);
    }

    public State getState() {
        return this.model.getState();
    }

    @Override
    public void setSelected(boolean b) {
        if (b) {
            this.setState(SELECTED);
        } else {
            this.setState(NOT_SELECTED);
        }
    }

    public static class State {
        private State() {
        }
    }

    private class ThreeStateDecorator
    implements ButtonModel {
        private final ButtonModel other;
        private boolean isPaint = false;

        private ThreeStateDecorator(ButtonModel other) {
            this.other = other;
        }

        private void setState(State state) {
            if (state == NOT_SELECTED) {
                this.other.setArmed(false);
                this.setPressed(false);
                this.setSelected(false);
            } else if (state == SELECTED) {
                this.other.setArmed(false);
                this.setPressed(false);
                this.setSelected(true);
            } else {
                this.other.setArmed(true);
                this.setPressed(false);
                this.setSelected(true);
            }
        }

        private State getState() {
            if (this.isSelected() && !this.isArmed()) {
                return SELECTED;
            }
            if (this.isSelected() && this.isArmed()) {
                return DONT_CARE;
            }
            return NOT_SELECTED;
        }

        @Override
        public boolean isEnabled() {
            return this.isPaint && this.getState() == DONT_CARE ? false : this.other.isEnabled();
        }

        public void beforePaint() {
            this.isPaint = true;
        }

        public void afterPaint() {
            this.isPaint = false;
        }

        private void nextState() {
            State current = this.getState();
            if (current == NOT_SELECTED) {
                this.setState(SELECTED);
            } else if (current == SELECTED) {
                this.setState(NOT_SELECTED);
            } else if (current == DONT_CARE) {
                this.setState(SELECTED);
            }
        }

        @Override
        public void setArmed(boolean b) {
        }

        @Override
        public void setEnabled(boolean b) {
            ThreeStateCheckBox.this.setFocusable(b);
            this.other.setEnabled(b);
        }

        @Override
        public boolean isArmed() {
            return this.other.isArmed();
        }

        @Override
        public boolean isSelected() {
            return this.other.isSelected();
        }

        @Override
        public boolean isPressed() {
            return this.other.isPressed();
        }

        @Override
        public boolean isRollover() {
            return this.other.isRollover();
        }

        @Override
        public void setSelected(boolean b) {
            this.other.setSelected(b);
        }

        @Override
        public void setPressed(boolean b) {
            this.other.setPressed(b);
        }

        @Override
        public void setRollover(boolean b) {
            this.other.setRollover(b);
        }

        @Override
        public void setMnemonic(int key) {
            this.other.setMnemonic(key);
        }

        @Override
        public int getMnemonic() {
            return this.other.getMnemonic();
        }

        @Override
        public void setActionCommand(String s) {
            this.other.setActionCommand(s);
        }

        @Override
        public String getActionCommand() {
            return this.other.getActionCommand();
        }

        @Override
        public void setGroup(ButtonGroup group) {
            this.other.setGroup(group);
        }

        @Override
        public void addActionListener(ActionListener l) {
            this.other.addActionListener(l);
        }

        @Override
        public void removeActionListener(ActionListener l) {
            this.other.removeActionListener(l);
        }

        @Override
        public void addItemListener(ItemListener l) {
            this.other.addItemListener(l);
        }

        @Override
        public void removeItemListener(ItemListener l) {
            this.other.removeItemListener(l);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            this.other.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            this.other.removeChangeListener(l);
        }

        @Override
        public Object[] getSelectedObjects() {
            return this.other.getSelectedObjects();
        }
    }
}

