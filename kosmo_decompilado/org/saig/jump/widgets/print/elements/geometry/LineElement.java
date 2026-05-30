/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.geometry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.dnd.DragAndDropLock;
import org.saig.jump.widgets.dnd.GhostGlassPane;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.dnd.GraphicElementTransferHandler;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.geometry.LineProperties;
import org.saig.jump.widgets.print.images.PrintIconLoader;

public class LineElement
implements GraphicElements {
    private PrintLayoutFrame parent;
    private boolean selected = false;
    private LineElementListener listener;
    private LineElementOnScreen onScreen;
    private LineElementForPrint forPrint;
    private String name;
    private Color lineColor = Color.BLACK;
    private float lineWidth = 2.0f;
    private float x1;
    private float x2;
    private float y1;
    private float y2;
    private static int last_id = 0;
    private int id;
    private boolean nuevo;

    public LineElement() {
        this.listener = new LineElementListener(this, this.parent);
        this.onScreen = new LineElementOnScreen();
        this.forPrint = new LineElementForPrint();
        this.id = last_id++;
        this.nuevo = false;
    }

    public LineElement(PrintLayoutFrame plf) {
        this.parent = plf;
        this.listener = new LineElementListener(this, this.parent);
        this.onScreen = new LineElementOnScreen();
        this.forPrint = new LineElementForPrint();
        this.setName(this.parent.createName(this.getClass()));
        this.x1 = 0.0f;
        this.y1 = 0.0f;
        this.x2 = 0.0f;
        this.y2 = 0.0f;
        this.id = last_id++;
        this.nuevo = true;
    }

    public float getX1() {
        return this.x1;
    }

    public void setX1(float x) {
        this.x1 = x;
    }

    public float getX2() {
        return this.x2;
    }

    public void setX2(float x) {
        this.x2 = x;
    }

    public float getY1() {
        return this.y1;
    }

    public void setY1(float x) {
        this.y1 = x;
    }

    public float getY2() {
        return this.y2;
    }

    public void setY2(float x) {
        this.y2 = x;
    }

    public Color getLineColor() {
        return this.lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public void adaptSize() {
        this.onScreen.adaptSize();
        this.forPrint.adaptSize();
    }

    public float getLineWidth() {
        return this.lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    public String toString() {
        return String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryFrame.line")) + "-" + this.id;
    }

    @Override
    public void setSelected(boolean select) {
        this.selected = select;
    }

    @Override
    public JComponent getGraphicElementsOnScreen() {
        return this.onScreen;
    }

    @Override
    public JComponent getGraphicElementsForPrint() {
        return this.forPrint;
    }

    @Override
    public void initCornerPoint() {
    }

    @Override
    public void setBorder(Border border) {
        this.onScreen.setBorder(border);
    }

    @Override
    public Point[] getCornerPoint() {
        return null;
    }

    @Override
    public void fixerDimensions(int x, int y, int w, int h, int facteur1, int facteur2) {
        this.onScreen.setBounds(x, y, w, h);
        this.onScreen.setLocation(x, y);
        this.forPrint.fixerDimensions();
        this.initCornerPoint();
        this.repaint();
    }

    @Override
    public void repaint() {
        this.onScreen.repaint();
        this.forPrint.repaint();
    }

    @Override
    public void refreshForPrintBounds() {
        this.forPrint.fixerDimensions();
    }

    @Override
    public void resize(int newForPrintWidth, int oldForPrintWidth, int newForPrintHeight, int oldForPrintHeight, int widthOnScreen, int heightOnScreen) {
        Rectangle oldForPrintBounds = (Rectangle)this.forPrint.getBounds().clone();
        this.forPrint.setBounds((int)Math.round(oldForPrintBounds.getX() * (double)newForPrintWidth / (double)oldForPrintWidth), (int)Math.round(oldForPrintBounds.getY() * (double)newForPrintHeight / (double)oldForPrintHeight), (int)Math.round(oldForPrintBounds.getWidth() * (double)newForPrintWidth / (double)oldForPrintWidth), (int)Math.round(oldForPrintBounds.getHeight() * (double)newForPrintHeight / (double)oldForPrintHeight));
        this.forPrint.setLocation((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY());
        switch (this.parent.getActiveZoom()) {
            case 0: {
                this.onScreen.setBounds((int)Math.round(oldForPrintBounds.getX() * (double)widthOnScreen / (double)oldForPrintWidth), (int)Math.round(oldForPrintBounds.getY() * (double)heightOnScreen / (double)oldForPrintHeight), (int)Math.round(oldForPrintBounds.getWidth() * (double)widthOnScreen / (double)oldForPrintWidth), (int)Math.round(oldForPrintBounds.getHeight() * (double)heightOnScreen / (double)oldForPrintHeight));
                this.onScreen.setLocation((int)this.onScreen.getBounds().getX(), (int)this.onScreen.getBounds().getY());
                break;
            }
            case 1: {
                this.onScreen.setBounds((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY(), (int)this.forPrint.getBounds().getWidth(), (int)this.forPrint.getBounds().getHeight());
                this.onScreen.setLocation((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY());
            }
        }
        this.initCornerPoint();
        this.onScreen.repaint();
    }

    public void print(Graphics g, int x, int y) {
        this.forPrint.print(g, x, y);
    }

    @Override
    public void zoom(int newForPrintWidth, int oldForPrintWidth, int newForPrintHeight, int oldForPrintHeight, int widthOnScreen, int heightOnScreen) {
        switch (this.parent.getActiveZoom()) {
            case 0: {
                double widthFactor = (double)widthOnScreen / (double)newForPrintWidth;
                double heightFactor = (double)heightOnScreen / (double)newForPrintHeight;
                this.onScreen.setBounds((int)Math.round(this.forPrint.getBounds().getX() * widthFactor), (int)Math.round(this.forPrint.getBounds().getY() * heightFactor), (int)Math.round(this.forPrint.getBounds().getWidth() * widthFactor), (int)Math.round(this.forPrint.getBounds().getHeight() * heightFactor));
                this.onScreen.setLocation((int)this.onScreen.getBounds().getX(), (int)this.onScreen.getBounds().getY());
                break;
            }
            case 1: {
                this.onScreen.setBounds((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY(), (int)this.forPrint.getBounds().getWidth(), (int)this.forPrint.getBounds().getHeight());
                this.onScreen.setLocation((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY());
            }
        }
        this.initCornerPoint();
        this.onScreen.repaint();
    }

    @Override
    public void zoom(float f) {
        this.onScreen.setBounds((int)Math.round(this.forPrint.getBounds().getX() * (double)f), (int)Math.round(this.forPrint.getBounds().getY() * (double)f), (int)Math.round(this.forPrint.getBounds().getWidth() * (double)f), (int)Math.round(this.forPrint.getBounds().getHeight() * (double)f));
        this.initCornerPoint();
        this.onScreen.repaint();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        this.onScreen.setToolTipText(this.getName());
    }

    @Override
    public void setParent(PrintLayoutFrame plf) {
        this.parent = plf;
        this.listener.setFrame(plf);
    }

    @Override
    public PrintLayoutFrame getParent() {
        return this.parent;
    }

    @Override
    public void setGraphicAttributes() {
        this.setBorder(null);
        this.onScreen.setCursor(Cursor.getPredefinedCursor(13));
        this.onScreen.validate();
    }

    @Override
    public void initGraphicAttributes(PrintLayoutFrame plf) {
    }

    @Override
    public void refresh() {
        if (this.parent.getSelectedComponent() != null) {
            this.parent.getSelectedComponent().setSelected(false);
        }
        this.setSelected(true);
        this.parent.setSelectedComponent(this);
        this.repaint();
    }

    @Override
    public void dispose() {
        this.parent = null;
        this.forPrint = null;
        if (this.onScreen != null) {
            this.onScreen.dispose();
            this.onScreen = null;
        }
        if (this.listener != null) {
            this.listener.dispose();
            this.listener = null;
        }
    }

    @Override
    public Icon getIcon() {
        return PrintIconLoader.icon("addNorth.gif");
    }

    @Override
    public int getPrintHeight() {
        return this.forPrint.getHeight();
    }

    public void setPrintHeight(int h) {
        int w = this.forPrint.getWidth();
        Dimension d = new Dimension();
        d.setSize(w, h);
        this.forPrint.setSize(d);
    }

    @Override
    public int getPrintWidth() {
        return this.forPrint.getWidth();
    }

    public void setPrintWidth(int w) {
        int h = this.forPrint.getHeight();
        Dimension d = new Dimension();
        d.setSize(w, h);
        this.forPrint.setSize(d);
    }

    @Override
    public int getPrintX() {
        return this.forPrint.getX();
    }

    public void setPrintX(int x) {
        int y = this.forPrint.getY();
        Point p = new Point();
        p.setLocation(x, y);
        this.forPrint.setLocation(p);
    }

    @Override
    public int getPrintY() {
        return this.forPrint.getY();
    }

    public void setPrintY(int y) {
        int x = this.forPrint.getX();
        Point p = new Point();
        p.setLocation(x, y);
        this.forPrint.setLocation(p);
    }

    public int getScreenHeight() {
        return this.onScreen.getHeight();
    }

    public void setScreenHeight(int h) {
        int w = this.onScreen.getWidth();
        Dimension d = new Dimension();
        d.setSize(w, h);
        this.onScreen.setSize(d);
    }

    public int getScreenWidth() {
        return this.onScreen.getWidth();
    }

    public void setScreenWidth(int w) {
        int h = this.onScreen.getHeight();
        Dimension d = new Dimension();
        d.setSize(w, h);
        this.onScreen.setSize(d);
    }

    public int getScreenX() {
        return this.onScreen.getX();
    }

    public void setScreenX(int x) {
        int y = this.onScreen.getY();
        Point p = new Point();
        p.setLocation(x, y);
        this.onScreen.setLocation(p);
    }

    public int getScreenY() {
        return this.onScreen.getY();
    }

    public void setScreenY(int y) {
        int x = this.onScreen.getX();
        Point p = new Point();
        p.setLocation(x, y);
        this.onScreen.setLocation(p);
    }

    @Override
    public void setResizing(boolean resizing) {
    }

    private class LineElementForPrint
    extends JComponent {
        public void adaptSize() {
            Rectangle r = this.getBounds();
            this.setBounds(r);
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            Rectangle r = null;
            r = LineElement.this.parent != null ? LineElement.this.parent.getPage().getPageForPrint().getBounds() : new Rectangle(x, y, width, height);
            r.x = 0;
            r.y = 0;
            super.setBounds(r.x, r.y, r.width, r.height);
        }

        public void fixerDimensions() {
            switch (LineElement.this.parent.getActiveZoom()) {
                case 0: 
                case 3: {
                    super.setBounds((int)Math.round(LineElement.this.onScreen.getBounds().getX() * LineElement.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(LineElement.this.onScreen.getBounds().getY() * LineElement.this.parent.getPrintScreenHeightAspectRatio()), (int)Math.round(LineElement.this.onScreen.getBounds().getWidth() * LineElement.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(LineElement.this.onScreen.getBounds().getHeight() * LineElement.this.parent.getPrintScreenHeightAspectRatio()));
                    break;
                }
                case 1: {
                    super.setBounds((int)LineElement.this.onScreen.getBounds().getX(), (int)LineElement.this.onScreen.getBounds().getY(), (int)LineElement.this.onScreen.getBounds().getWidth(), (int)LineElement.this.onScreen.getBounds().getHeight());
                }
            }
            super.setLocation((int)super.getBounds().getX(), (int)super.getBounds().getY());
            super.repaint();
        }

        public void print(Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g;
            g2.translate(x, y);
            g2.setStroke(new BasicStroke(LineElement.this.lineWidth));
            Line2D.Float geom = null;
            geom = new Line2D.Float(LineElement.this.x1, LineElement.this.y1, LineElement.this.x2, LineElement.this.y2);
            g2.setColor(LineElement.this.getLineColor());
            g2.draw(geom);
            g2.translate(-x, -y);
        }
    }

    private class LineElementListener
    implements KeyListener,
    MouseListener,
    MouseMotionListener {
        LineElement ge;
        PrintLayoutFrame plf;
        boolean draggin1;
        boolean draggin2;

        public LineElementListener(LineElement ge, PrintLayoutFrame plf) {
            this.ge = ge;
            this.plf = plf;
        }

        public void setFrame(PrintLayoutFrame plf) {
            this.plf = plf;
        }

        public void dispose() {
            this.plf = null;
            this.ge = null;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            LineElement.this.onScreen.requestFocusInWindow();
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getClickCount() == 2) {
                    new LineProperties(this.ge);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (LineElement.this.parent.getSelectedComponent() != null) {
                        LineElement.this.parent.getSelectedComponent().setSelected(false);
                    }
                    this.ge.setSelected(true);
                    LineElement.this.parent.setSelectedComponent(this.ge);
                }
                LineElement.this.repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            float x = e.getX();
            float y = e.getY();
            float f = LineElement.this.parent.getZoomValue();
            float d1 = (LineElement.this.x1 * f - x) * (LineElement.this.x1 * f - x) + (LineElement.this.y1 * f - y) * (LineElement.this.y1 * f - y);
            float d2 = (LineElement.this.x2 * f - x) * (LineElement.this.x2 * f - x) + (LineElement.this.y2 * f - y) * (LineElement.this.y2 * f - y);
            if (d1 < 100.0f || this.draggin1) {
                this.draggin1 = true;
                DragAndDropLock.setResizing(true);
                LineElement.this.x1 = x / f;
                LineElement.this.y1 = y / f;
                LineElement.this.repaint();
            } else if (d2 < 100.0f || this.draggin2) {
                this.draggin2 = true;
                DragAndDropLock.setResizing(true);
                LineElement.this.x2 = x / f;
                LineElement.this.y2 = y / f;
                LineElement.this.repaint();
            }
            this.adjustLine(LineElement.this.parent.getPage().getWidthWithoutMargin(), LineElement.this.parent.getPage().getHeightWithoutMargin());
        }

        private void adjustLine(float width, float height) {
            if (LineElement.this.x1 > width) {
                LineElement.this.x1 = width;
            }
            if (LineElement.this.x2 > width) {
                LineElement.this.x2 = width;
            }
            if (LineElement.this.x1 < 0.0f) {
                LineElement.this.x1 = 0.0f;
            }
            if (LineElement.this.x2 < 0.0f) {
                LineElement.this.x2 = 0.0f;
            }
            if (LineElement.this.y1 > height) {
                LineElement.this.y1 = height;
            }
            if (LineElement.this.y2 > height) {
                LineElement.this.y2 = height;
            }
            if (LineElement.this.y1 < 0.0f) {
                LineElement.this.y1 = 0.0f;
            }
            if (LineElement.this.y2 < 0.0f) {
                LineElement.this.y2 = 0.0f;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            float x = e.getX();
            float y = e.getY();
            float f = LineElement.this.parent.getZoomValue();
            float d1 = (LineElement.this.x1 * f - x) * (LineElement.this.x1 * f - x) + (LineElement.this.y1 * f - y) * (LineElement.this.y1 * f - y);
            float d2 = (LineElement.this.x2 * f - x) * (LineElement.this.x2 * f - x) + (LineElement.this.y2 * f - y) * (LineElement.this.y2 * f - y);
            if (d1 < 100.0f) {
                LineElement.this.onScreen.setCursor(Cursor.getPredefinedCursor(12));
            } else if (d2 < 100.0f) {
                LineElement.this.onScreen.setCursor(Cursor.getPredefinedCursor(12));
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent arg0) {
        }

        @Override
        public void keyTyped(KeyEvent arg0) {
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (LineElement.this.nuevo) {
                float x = e.getX();
                float y = e.getY();
                float f = LineElement.this.parent.getZoomValue();
                DragAndDropLock.setResizing(true);
                LineElement.this.x1 = x / f;
                LineElement.this.y1 = y / f;
                LineElement.this.x2 = LineElement.this.x1;
                LineElement.this.y2 = LineElement.this.y1;
                LineElement.this.nuevo = false;
                LineElement.this.repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent arg0) {
            this.draggin1 = false;
            this.draggin2 = false;
        }
    }

    private class LineElementOnScreen
    extends JComponent
    implements DragGestureListener,
    DragSourceListener,
    DragSourceMotionListener {
        public LineElementOnScreen() {
            this.setOpaque(false);
            this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            this.addMouseListener(LineElement.this.listener);
            this.addMouseMotionListener(LineElement.this.listener);
            this.addKeyListener(LineElement.this.listener);
            this.setFocusable(true);
        }

        public void dispose() {
            this.removeMouseListener(LineElement.this.listener);
            this.removeMouseMotionListener(LineElement.this.listener);
            this.removeKeyListener(LineElement.this.listener);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.removeDragSourceMotionListener(this);
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            Rectangle r = null;
            r = LineElement.this.parent != null ? LineElement.this.parent.getPage().getPageDrawOnScreen().getBounds() : new Rectangle(x, y, width, height);
            r.x = 0;
            r.y = 0;
            super.setBounds(r.x, r.y, r.width, r.height);
        }

        @Override
        public boolean contains(int x, int y) {
            if (LineElement.this.nuevo) {
                return true;
            }
            float f = LineElement.this.parent.getZoomValue();
            float d1 = (LineElement.this.x1 * f - (float)x) * (LineElement.this.x1 * f - (float)x) + (LineElement.this.y1 * f - (float)y) * (LineElement.this.y1 * f - (float)y);
            float d2 = (LineElement.this.x2 * f - (float)x) * (LineElement.this.x2 * f - (float)x) + (LineElement.this.y2 * f - (float)y) * (LineElement.this.y2 * f - (float)y);
            if (d1 < 100.0f) {
                return true;
            }
            return d2 < 100.0f;
        }

        public void adaptSize() {
            Rectangle r = this.getBounds();
            this.setBounds(r);
        }

        @Override
        public void dragEnter(DragSourceDragEvent e) {
            DragSourceContext context = e.getDragSourceContext();
            int myaction = e.getDropAction();
            if ((myaction & 2) != 0) {
                context.setCursor(DragSource.DefaultMoveDrop);
            } else {
                context.setCursor(DragSource.DefaultMoveNoDrop);
            }
        }

        @Override
        public void dragOver(DragSourceDragEvent arg0) {
        }

        @Override
        public void dropActionChanged(DragSourceDragEvent arg0) {
        }

        @Override
        public void dragDropEnd(DragSourceDropEvent dsde) {
            if (!DragAndDropLock.isDragAndDropStarted()) {
                return;
            }
            DragAndDropLock.setDragAndDropStarted(false);
            GhostGlassPane glassPane = (GhostGlassPane)SwingUtilities.getRootPane(this).getGlassPane();
            Point p = (Point)dsde.getLocation().clone();
            SwingUtilities.convertPointFromScreen(p, glassPane);
            if (!dsde.getDropSuccess()) {
                DragAndDropLock.setLocked(false);
                DragAndDropLock.setDragAndDropStarted(false);
                glassPane.setImage(null);
                glassPane.setVisible(false);
            } else {
                glassPane.setPoint(p);
                glassPane.repaint(glassPane.getRepaintRect());
            }
        }

        @Override
        public void paint(Graphics g) {
            Line2D.Float geom;
            super.paint(g);
            Graphics2D g2 = (Graphics2D)g;
            float f = LineElement.this.parent.getZoomValue();
            float lineWidth = LineElement.this.lineWidth * f;
            if (LineElement.this.selected) {
                g2.setStroke(new BasicStroke(lineWidth + 1.0f));
                g2.setColor(Color.RED);
                geom = null;
                geom = new Line2D.Float(LineElement.this.x1 * f, LineElement.this.y1 * f, LineElement.this.x2 * f, LineElement.this.y2 * f);
                g2.draw(geom);
            }
            g2.setStroke(new BasicStroke(lineWidth));
            geom = null;
            geom = new Line2D.Float(LineElement.this.x1 * f, LineElement.this.y1 * f, LineElement.this.x2 * f, LineElement.this.y2 * f);
            g2.setColor(LineElement.this.getLineColor());
            g2.draw(geom);
            g2.setColor(Color.RED);
            g2.fillOval((int)(LineElement.this.x1 * f) - 1, (int)(LineElement.this.y1 * f) - 1, 3, 3);
            g2.fillOval((int)(LineElement.this.x2 * f) - 1, (int)(LineElement.this.y2 * f) - 1, 3, 3);
        }

        @Override
        public void dragExit(DragSourceEvent dse) {
            DragSourceContext context = dse.getDragSourceContext();
            context.setCursor(DragSource.DefaultMoveNoDrop);
        }

        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            if (DragAndDropLock.isLocked() || DragAndDropLock.isResizing()) {
                DragAndDropLock.setDragAndDropStarted(false);
                return;
            }
            DragAndDropLock.setLocked(true);
            DragAndDropLock.setDragAndDropStarted(true);
            BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), 1);
            Graphics2D graphics2 = (Graphics2D)image.getGraphics();
            graphics2.setColor(Color.WHITE);
            graphics2.fillRect(0, 0, this.getWidth(), this.getHeight());
            LineElement.this.onScreen.paint(graphics2);
            dge.startDrag(Cursor.getDefaultCursor(), new GraphicElementTransferHandler(image, LineElement.this), this);
            GhostGlassPane glassPane = (GhostGlassPane)SwingUtilities.getRootPane(this).getGlassPane();
            glassPane.setVisible(true);
            Point p = (Point)dge.getDragOrigin().clone();
            SwingUtilities.convertPointToScreen(p, this);
            SwingUtilities.convertPointFromScreen(p, glassPane);
            glassPane.setPoint(p);
            glassPane.setImage(image);
            glassPane.repaint();
        }

        @Override
        public void dragMouseMoved(DragSourceDragEvent dsde) {
            if (!DragAndDropLock.isDragAndDropStarted()) {
                return;
            }
            if (SwingUtilities.getRootPane(this) != null && SwingUtilities.getRootPane(this).getGlassPane() != null) {
                GhostGlassPane glassPane = (GhostGlassPane)SwingUtilities.getRootPane(this).getGlassPane();
                Point p = (Point)dsde.getLocation().clone();
                SwingUtilities.convertPointFromScreen(p, glassPane);
                glassPane.setPoint(p);
                glassPane.repaint(glassPane.getRepaintRect());
            }
        }
    }
}

