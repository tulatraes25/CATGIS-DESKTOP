/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.dnd.DragAndDropLock;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.elements.GraphicElements;

public class GraphicElementsListener
implements KeyListener,
MouseListener,
MouseMotionListener {
    protected GraphicElements ge;
    private int RESIZE_TOLERANCE = 5;
    private Point topLeftCorner = new Point();
    private Point topRightCorner = new Point();
    private Point bottomLeftCorner = new Point();
    private Point bottomRightCorner = new Point();
    private PrintLayoutFrame frame;
    private Point sourcePoint = new Point();
    private Point endPoint = new Point();
    private JPopupMenu contextMenu = new JPopupMenu();
    private JMenuItem backMenuItem = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.elements.GraphicElementsListener.move-down"));
    private JMenuItem frontMenuItem = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.elements.GraphicElementsListener.move-up"));
    private JMenuItem topMenuItem = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.elements.GraphicElementsListener.move-to-the-front"));
    private JMenuItem bottomMenuItem = new JMenuItem(I18N.getString("org.saig.jump.widgets.print.elements.GraphicElementsListener.move-to-the-back"));

    public GraphicElementsListener(GraphicElements ge, PrintLayoutFrame plf) {
        this.ge = ge;
        this.frame = plf;
        this.setCorner(ge.getCornerPoint());
        this.contextMenu.add(this.backMenuItem);
        this.contextMenu.add(this.frontMenuItem);
        this.contextMenu.add(this.topMenuItem);
        this.contextMenu.add(this.bottomMenuItem);
        this.backMenuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                GraphicElementsListener.this.moveBack();
            }
        });
        this.frontMenuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                GraphicElementsListener.this.moveFront();
            }
        });
        this.topMenuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                GraphicElementsListener.this.moveTop();
            }
        });
        this.bottomMenuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                GraphicElementsListener.this.moveBottom();
            }
        });
    }

    private int getElementIndex() {
        boolean found = false;
        int i = 0;
        Iterator<GraphicElements> it = this.frame.getGraphicElements().iterator();
        while (!found && it.hasNext()) {
            GraphicElements e = it.next();
            if (e.equals(this.ge)) {
                found = true;
                continue;
            }
            ++i;
        }
        if (found) {
            return i;
        }
        return -1;
    }

    private void moveBack() {
        int i = this.getElementIndex();
        if (i == 0) {
            return;
        }
        List<GraphicElements> frameList = this.frame.getGraphicElements();
        frameList.remove(i);
        frameList.add(i - 1, this.ge);
        if (this.frame.getElementsViewerDialog() != null) {
            this.frame.getElementsViewerDialog().orderChanged();
        } else {
            this.frame.orderLayerableElements();
        }
    }

    private void moveFront() {
        List<GraphicElements> frameList;
        int i = this.getElementIndex();
        if (i == (frameList = this.frame.getGraphicElements()).size() - 1) {
            return;
        }
        frameList.remove(i);
        frameList.add(i + 1, this.ge);
        if (this.frame.getElementsViewerDialog() != null) {
            this.frame.getElementsViewerDialog().orderChanged();
        } else {
            this.frame.orderLayerableElements();
        }
    }

    private void moveTop() {
        int i = this.getElementIndex();
        List<GraphicElements> frameList = this.frame.getGraphicElements();
        frameList.remove(i);
        frameList.add(frameList.size(), this.ge);
        if (this.frame.getElementsViewerDialog() != null) {
            this.frame.getElementsViewerDialog().orderChanged();
        } else {
            this.frame.orderLayerableElements();
        }
    }

    public void dispose() {
        this.frame = null;
        this.ge = null;
    }

    private void moveBottom() {
        int i = this.getElementIndex();
        List<GraphicElements> frameList = this.frame.getGraphicElements();
        frameList.remove(i);
        frameList.add(0, this.ge);
        if (this.frame.getElementsViewerDialog() != null) {
            this.frame.getElementsViewerDialog().orderChanged();
        } else {
            this.frame.orderLayerableElements();
        }
    }

    public void setCorner(Point[] points) {
        this.topLeftCorner = points[0];
        this.topRightCorner = points[1];
        this.bottomLeftCorner = points[2];
        this.bottomRightCorner = points[3];
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.ge.initCornerPoint();
        this.setCorner(this.ge.getCornerPoint());
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (this.frame.getSelectedComponent() != null) {
                this.frame.getSelectedComponent().setSelected(false);
            }
            this.ge.setSelected(true);
            this.frame.setSelectedComponent(this.ge);
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            this.contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.sourcePoint = new Point((int)e.getPoint().getX(), (int)e.getPoint().getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.ge.setResizing(false);
        this.ge.getGraphicElementsOnScreen().setBounds(this.ge.getGraphicElementsOnScreen().getBounds());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        switch (this.ge.getGraphicElementsOnScreen().getCursor().getType()) {
            case 13: {
                this.mouseDragged(e, false);
                break;
            }
            default: {
                this.mouseDragged(e, true);
            }
        }
    }

    public void mouseDragged(MouseEvent e, boolean resizing) {
        this.ge.setResizing(resizing);
        this.endPoint = new Point((int)e.getPoint().getX(), (int)e.getPoint().getY());
        switch (this.ge.getGraphicElementsOnScreen().getCursor().getType()) {
            case 13: {
                this.ge.initCornerPoint();
                this.setCorner(this.ge.getCornerPoint());
                return;
            }
            case 6: {
                this.ge.getGraphicElementsOnScreen().setBounds((int)(this.ge.getGraphicElementsOnScreen().getBounds().getX() + e.getPoint().getX()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getY() + e.getPoint().getY()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - e.getPoint().getX()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() - e.getPoint().getY()));
                break;
            }
            case 7: {
                if (this.topRightCorner.getX() < e.getPoint().getX()) {
                    if (this.topRightCorner.getY() < e.getPoint().getY()) {
                        this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getY() + e.getPoint().getY()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() + (e.getPoint().getX() - this.topRightCorner.getX())), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() - e.getPoint().getY()));
                        break;
                    }
                    this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getY() + e.getPoint().getY()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() + (e.getPoint().getX() - this.topRightCorner.getX())), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() - e.getPoint().getY()));
                    break;
                }
                if (this.topRightCorner.getY() < e.getPoint().getY()) {
                    this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getY() + e.getPoint().getY()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - (this.topRightCorner.getX() - e.getPoint().getX())), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() - e.getPoint().getY()));
                    break;
                }
                this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getY() + e.getPoint().getY()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - (this.topRightCorner.getX() - e.getPoint().getX())), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() - e.getPoint().getY()));
                break;
            }
            case 4: {
                if (this.bottomLeftCorner.getX() < e.getPoint().getX()) {
                    if (this.bottomLeftCorner.getY() < e.getPoint().getY()) {
                        this.ge.getGraphicElementsOnScreen().setBounds((int)(this.ge.getGraphicElementsOnScreen().getBounds().getX() + e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - e.getPoint().getX()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() + (e.getPoint().getY() - this.bottomLeftCorner.getY())));
                        break;
                    }
                    this.ge.getGraphicElementsOnScreen().setBounds((int)(this.ge.getGraphicElementsOnScreen().getBounds().getX() + e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - e.getPoint().getX()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() - (this.bottomLeftCorner.getY() - e.getPoint().getY())));
                    break;
                }
                if (this.bottomLeftCorner.getY() < e.getPoint().getY()) {
                    this.ge.getGraphicElementsOnScreen().setBounds((int)(this.ge.getGraphicElementsOnScreen().getBounds().getX() + e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - e.getPoint().getX()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() + (e.getPoint().getY() - this.bottomLeftCorner.getY())));
                    break;
                }
                this.ge.getGraphicElementsOnScreen().setBounds((int)(this.ge.getGraphicElementsOnScreen().getBounds().getX() + e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - e.getPoint().getX()), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() - (this.bottomLeftCorner.getY() - e.getPoint().getY())));
                break;
            }
            case 8: {
                this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getY() + e.getPoint().getY()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getWidth(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() - e.getPoint().getY()));
                break;
            }
            case 9: {
                if (this.bottomLeftCorner.getY() < e.getPoint().getY()) {
                    this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)this.ge.getGraphicElementsOnScreen().getBounds().getWidth(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() + (e.getPoint().getY() - this.bottomLeftCorner.getY())));
                    break;
                }
                this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)this.ge.getGraphicElementsOnScreen().getBounds().getWidth(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getHeight() - (this.bottomLeftCorner.getY() - e.getPoint().getY())));
                break;
            }
            case 10: {
                if (this.bottomLeftCorner.getX() < e.getPoint().getX()) {
                    if (this.bottomLeftCorner.getY() < e.getPoint().getY()) {
                        this.ge.getGraphicElementsOnScreen().setBounds((int)(this.ge.getGraphicElementsOnScreen().getBounds().getX() + e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getHeight());
                        break;
                    }
                    this.ge.getGraphicElementsOnScreen().setBounds((int)(this.ge.getGraphicElementsOnScreen().getBounds().getX() + e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getHeight());
                    break;
                }
                if (this.bottomLeftCorner.getY() < e.getPoint().getY()) {
                    this.ge.getGraphicElementsOnScreen().setBounds((int)(this.ge.getGraphicElementsOnScreen().getBounds().getX() + e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getHeight());
                    break;
                }
                this.ge.getGraphicElementsOnScreen().setBounds((int)(this.ge.getGraphicElementsOnScreen().getBounds().getX() + e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - e.getPoint().getX()), (int)this.ge.getGraphicElementsOnScreen().getBounds().getHeight());
                break;
            }
            case 11: {
                if (this.topRightCorner.getX() < e.getPoint().getX()) {
                    if (this.topRightCorner.getY() < e.getPoint().getY()) {
                        this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() + (e.getPoint().getX() - this.topRightCorner.getX())), (int)this.ge.getGraphicElementsOnScreen().getBounds().getHeight());
                        break;
                    }
                    this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() + (e.getPoint().getX() - this.topRightCorner.getX())), (int)this.ge.getGraphicElementsOnScreen().getBounds().getHeight());
                    break;
                }
                if (this.topRightCorner.getY() < e.getPoint().getY()) {
                    this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - (this.topRightCorner.getX() - e.getPoint().getX())), (int)this.ge.getGraphicElementsOnScreen().getBounds().getHeight());
                    break;
                }
                this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)(this.ge.getGraphicElementsOnScreen().getBounds().getWidth() - (this.topRightCorner.getX() - e.getPoint().getX())), (int)this.ge.getGraphicElementsOnScreen().getBounds().getHeight());
                break;
            }
            case 5: {
                this.ge.getGraphicElementsOnScreen().setBounds((int)this.ge.getGraphicElementsOnScreen().getBounds().getX(), (int)this.ge.getGraphicElementsOnScreen().getBounds().getY(), (int)e.getPoint().getX(), (int)e.getPoint().getY());
            }
        }
        this.ge.initCornerPoint();
        this.setCorner(this.ge.getCornerPoint());
        this.ge.refreshForPrintBounds();
        this.ge.refresh();
    }

    public void mouseExit(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getPoint().distance(this.topLeftCorner.getX(), this.topLeftCorner.getY()) < (double)this.RESIZE_TOLERANCE) {
            DragAndDropLock.setResizing(true);
            this.ge.getGraphicElementsOnScreen().setCursor(Cursor.getPredefinedCursor(6));
        } else if (e.getPoint().distance(this.topRightCorner.getX(), this.topRightCorner.getY()) < (double)this.RESIZE_TOLERANCE) {
            DragAndDropLock.setResizing(true);
            this.ge.getGraphicElementsOnScreen().setCursor(Cursor.getPredefinedCursor(7));
        } else if (e.getPoint().distance(this.bottomLeftCorner.getX(), this.bottomLeftCorner.getY()) < (double)this.RESIZE_TOLERANCE) {
            DragAndDropLock.setResizing(true);
            this.ge.getGraphicElementsOnScreen().setCursor(Cursor.getPredefinedCursor(4));
        } else if (e.getPoint().distance(this.bottomRightCorner.getX(), this.bottomRightCorner.getY()) < (double)this.RESIZE_TOLERANCE) {
            DragAndDropLock.setResizing(true);
            this.ge.getGraphicElementsOnScreen().setCursor(Cursor.getPredefinedCursor(5));
        } else if (e.getPoint().y > this.topRightCorner.y && e.getPoint().y < this.bottomRightCorner.y && Math.abs(e.getPoint().x - this.topRightCorner.x) < this.RESIZE_TOLERANCE) {
            DragAndDropLock.setResizing(true);
            this.ge.getGraphicElementsOnScreen().setCursor(Cursor.getPredefinedCursor(11));
        } else if (e.getPoint().y > this.topLeftCorner.y && e.getPoint().y < this.bottomLeftCorner.y && Math.abs(e.getPoint().x - this.topLeftCorner.x) < this.RESIZE_TOLERANCE) {
            DragAndDropLock.setResizing(true);
            this.ge.getGraphicElementsOnScreen().setCursor(Cursor.getPredefinedCursor(10));
        } else if (e.getPoint().x > this.topLeftCorner.x && e.getPoint().x < this.topRightCorner.x && Math.abs(e.getPoint().y - this.topRightCorner.y) < this.RESIZE_TOLERANCE) {
            DragAndDropLock.setResizing(true);
            this.ge.getGraphicElementsOnScreen().setCursor(Cursor.getPredefinedCursor(8));
        } else if (e.getPoint().x > this.bottomLeftCorner.x && e.getPoint().x < this.bottomRightCorner.x && Math.abs(e.getPoint().y - this.bottomRightCorner.y) < this.RESIZE_TOLERANCE) {
            DragAndDropLock.setResizing(true);
            this.ge.getGraphicElementsOnScreen().setCursor(Cursor.getPredefinedCursor(9));
        } else {
            DragAndDropLock.setResizing(false);
            this.ge.getGraphicElementsOnScreen().setCursor(Cursor.getPredefinedCursor(13));
        }
    }

    public void setFrame(PrintLayoutFrame plf) {
        this.frame = plf;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == 27) {
            if (this.frame.getSelectedComponent() != null) {
                this.frame.getSelectedComponent().setSelected(false);
                this.frame.setSelectedComponent(null);
            }
        } else if (keyCode == 37) {
            Rectangle bounds = this.ge.getGraphicElementsOnScreen().getBounds();
            Rectangle newBounds = new Rectangle(bounds.x - 1, bounds.y, bounds.width, bounds.height);
            this.ge.getGraphicElementsOnScreen().setBounds(newBounds);
            this.ge.refreshForPrintBounds();
        } else if (keyCode == 38) {
            Rectangle bounds = this.ge.getGraphicElementsOnScreen().getBounds();
            Rectangle newBounds = new Rectangle(bounds.x, bounds.y - 1, bounds.width, bounds.height);
            this.ge.getGraphicElementsOnScreen().setBounds(newBounds);
            this.ge.refreshForPrintBounds();
        } else if (keyCode == 39) {
            Rectangle bounds = this.ge.getGraphicElementsOnScreen().getBounds();
            Rectangle newBounds = new Rectangle(bounds.x + 1, bounds.y, bounds.width, bounds.height);
            this.ge.getGraphicElementsOnScreen().setBounds(newBounds);
            this.ge.refreshForPrintBounds();
        } else if (keyCode == 40) {
            Rectangle bounds = this.ge.getGraphicElementsOnScreen().getBounds();
            Rectangle newBounds = new Rectangle(bounds.x, bounds.y + 1, bounds.width, bounds.height);
            this.ge.getGraphicElementsOnScreen().setBounds(newBounds);
            this.ge.refreshForPrintBounds();
        } else if (keyCode == 127 && this.frame.getSelectedComponent() != null) {
            this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage().remove(this.frame.getSelectedComponent());
            this.frame.setSelectedComponent(null);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}

