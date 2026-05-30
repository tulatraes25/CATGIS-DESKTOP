/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Icon;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.saig.jump.widgets.util.validating.WantsValidationStatus;

public abstract class AbstractValidator
extends InputVerifier
implements KeyListener {
    public static final Icon ICON = IconLoader.icon("error_obj.gif");
    protected JDialog popup;
    protected Object parent;
    protected JLabel messageLabel;
    protected JLabel image;
    protected Point point;
    protected Dimension cDim;
    protected Color color = new Color(243, 255, 159);

    private AbstractValidator() {
    }

    protected AbstractValidator(JComponent c, String message) {
        this();
        c.addKeyListener(this);
        this.messageLabel = new JLabel(String.valueOf(message) + " ");
        this.image = new JLabel(ICON);
    }

    public AbstractValidator(JDialog parent, JComponent c, String message) {
        this(c, message);
        this.parent = parent;
        this.popup = new JDialog(parent);
        this.initComponents();
    }

    public AbstractValidator(JFrame parent, JComponent c, String message) {
        this(c, message);
        this.parent = parent;
        this.popup = new JDialog(parent);
        this.initComponents();
    }

    protected abstract boolean validationCriteria(JComponent var1);

    @Override
    public boolean verify(JComponent c) {
        if (!this.validationCriteria(c)) {
            if (this.parent instanceof WantsValidationStatus) {
                ((WantsValidationStatus)this.parent).validateFailed();
            }
            c.setBackground(Color.PINK);
            this.popup.setSize(0, 0);
            this.popup.setLocationRelativeTo(c);
            this.point = this.popup.getLocation();
            this.cDim = c.getSize();
            this.popup.setLocation(this.point.x - (int)this.cDim.getWidth() / 2, this.point.y + (int)this.cDim.getHeight() / 2);
            this.popup.pack();
            this.popup.setVisible(true);
            return false;
        }
        c.setBackground(Color.WHITE);
        this.popup.setVisible(false);
        if (this.parent instanceof WantsValidationStatus) {
            ((WantsValidationStatus)this.parent).validatePassed();
        }
        return true;
    }

    protected void setMessage(String message) {
        this.messageLabel.setText(message);
    }

    public String getMessage() {
        return this.messageLabel.getText();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        this.popup.setVisible(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    protected void initComponents() {
        this.popup.getContentPane().setLayout(new FlowLayout());
        this.popup.setUndecorated(true);
        this.popup.getContentPane().setBackground(this.color);
        this.popup.getContentPane().add(this.image);
        this.popup.getContentPane().add(this.messageLabel);
        this.popup.setFocusableWindowState(false);
    }
}

