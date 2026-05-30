/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class ComponentMover
extends MouseAdapter {
    private Insets dragInsets = new Insets(0, 0, 0, 0);
    private Dimension snapSize = new Dimension(1, 1);
    private Insets edgeInsets = new Insets(0, 0, 0, 0);
    private boolean changeCursor = true;
    private boolean autoLayout = false;
    private Class<?> destinationClass;
    private Component destinationComponent;
    private Component destination;
    private Component source;
    private Point pressed;
    private Point location;
    private Cursor originalCursor;
    private boolean autoscrolls;
    private boolean potentialDrag;

    public ComponentMover() {
    }

    public ComponentMover(Class<?> destinationClass, Component ... components) {
        this.destinationClass = destinationClass;
        this.registerComponent(components);
    }

    public ComponentMover(Component destinationComponent, Component ... components) {
        this.destinationComponent = destinationComponent;
        this.registerComponent(components);
    }

    public boolean isAutoLayout() {
        return this.autoLayout;
    }

    public void setAutoLayout(boolean autoLayout) {
        this.autoLayout = autoLayout;
    }

    public boolean isChangeCursor() {
        return this.changeCursor;
    }

    public void setChangeCursor(boolean changeCursor) {
        this.changeCursor = changeCursor;
    }

    public Insets getDragInsets() {
        return this.dragInsets;
    }

    public void setDragInsets(Insets dragInsets) {
        this.dragInsets = dragInsets;
    }

    public Insets getEdgeInsets() {
        return this.edgeInsets;
    }

    public void setEdgeInsets(Insets edgeInsets) {
        this.edgeInsets = edgeInsets;
    }

    public void deregisterComponent(Component ... components) {
        Component[] componentArray = components;
        int n = components.length;
        int n2 = 0;
        while (n2 < n) {
            Component component = componentArray[n2];
            component.removeMouseListener(this);
            ++n2;
        }
    }

    public void registerComponent(Component ... components) {
        Component[] componentArray = components;
        int n = components.length;
        int n2 = 0;
        while (n2 < n) {
            Component component = componentArray[n2];
            component.addMouseListener(this);
            ++n2;
        }
    }

    public Dimension getSnapSize() {
        return this.snapSize;
    }

    public void setSnapSize(Dimension snapSize) {
        if (snapSize.width < 1 || snapSize.height < 1) {
            throw new IllegalArgumentException("Snap sizes must be greater than 0");
        }
        this.snapSize = snapSize;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.source = e.getComponent();
        int width = this.source.getSize().width - this.dragInsets.left - this.dragInsets.right;
        int height = this.source.getSize().height - this.dragInsets.top - this.dragInsets.bottom;
        Rectangle r = new Rectangle(this.dragInsets.left, this.dragInsets.top, width, height);
        if (r.contains(e.getPoint())) {
            this.setupForDragging(e);
        }
    }

    private void setupForDragging(MouseEvent e) {
        this.source.addMouseMotionListener(this);
        this.potentialDrag = true;
        this.destination = this.destinationComponent != null ? this.destinationComponent : (this.destinationClass == null ? this.source : SwingUtilities.getAncestorOfClass(this.destinationClass, this.source));
        this.pressed = e.getLocationOnScreen();
        this.location = this.destination.getLocation();
        if (this.changeCursor) {
            this.originalCursor = this.source.getCursor();
            this.source.setCursor(Cursor.getPredefinedCursor(13));
        }
        if (this.destination instanceof JComponent) {
            JComponent jc = (JComponent)this.destination;
            this.autoscrolls = jc.getAutoscrolls();
            jc.setAutoscrolls(false);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point dragged = e.getLocationOnScreen();
        int dragX = this.getDragDistance(dragged.x, this.pressed.x, this.snapSize.width);
        int dragY = this.getDragDistance(dragged.y, this.pressed.y, this.snapSize.height);
        int locationX = this.location.x + dragX;
        int locationY = this.location.y + dragY;
        while (locationX < this.edgeInsets.left) {
            locationX += this.snapSize.width;
        }
        while (locationY < this.edgeInsets.top) {
            locationY += this.snapSize.height;
        }
        Dimension d = this.getBoundingSize(this.destination);
        while (locationX + this.destination.getSize().width + this.edgeInsets.right > d.width) {
            locationX -= this.snapSize.width;
        }
        while (locationY + this.destination.getSize().height + this.edgeInsets.bottom > d.height) {
            locationY -= this.snapSize.height;
        }
        this.destination.setLocation(locationX, locationY);
    }

    private int getDragDistance(int larger, int smaller, int snapSize) {
        int drag;
        int halfway = snapSize / 2;
        drag += (drag = larger - smaller) < 0 ? -halfway : halfway;
        drag = drag / snapSize * snapSize;
        return drag;
    }

    private Dimension getBoundingSize(Component source) {
        if (source instanceof Window) {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle bounds = env.getMaximumWindowBounds();
            return new Dimension(bounds.width, bounds.height);
        }
        return source.getParent().getSize();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!this.potentialDrag) {
            return;
        }
        this.source.removeMouseMotionListener(this);
        this.potentialDrag = false;
        if (this.changeCursor) {
            this.source.setCursor(this.originalCursor);
        }
        if (this.destination instanceof JComponent) {
            ((JComponent)this.destination).setAutoscrolls(this.autoscrolls);
        }
        if (this.autoLayout) {
            if (this.destination instanceof JComponent) {
                ((JComponent)this.destination).revalidate();
            } else {
                this.destination.validate();
            }
        }
    }
}

