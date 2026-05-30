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
import java.awt.Shape;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.dnd.DragAndDropLock;
import org.saig.jump.widgets.dnd.GhostGlassPane;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.PrintStatic;
import org.saig.jump.widgets.print.dnd.GraphicElementTransferHandler;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.GraphicElementsListener;
import org.saig.jump.widgets.print.elements.geometry.GeometryProperties;
import org.saig.jump.widgets.print.images.PrintIconLoader;

public class GeometryFrame
implements GraphicElements {
    public static final int RECTANGLE = 1;
    public static final int ELLIPSE = 2;
    public static final int LINE = 3;
    public static final int SQUARE = 4;
    public static final int CIRCLE = 5;
    private PrintLayoutFrame parent;
    private boolean selected = false;
    private GeometryFrameListener listener;
    private GeometryFrameOnScreen onScreen;
    private GeometryFrameForPrint forPrint;
    private Point topLeftCorner = new Point();
    private Point topRightCorner = new Point();
    private Point bottomLeftCorner = new Point();
    private Point bottomRightCorner = new Point();
    private String name;
    private boolean opaque = false;
    private Color lineColor = Color.BLACK;
    private Color background = Color.BLUE;
    private float lineWidth = 2.0f;
    private int type = 1;
    private float x1;
    private float x2;
    private float y1;
    private float y2;
    private boolean nuevo;
    private static int last_id = 0;
    private int id;

    public GeometryFrame() {
        this.listener = new GeometryFrameListener(this, this.parent);
        this.onScreen = new GeometryFrameOnScreen();
        this.forPrint = new GeometryFrameForPrint();
        this.nuevo = false;
        this.id = last_id++;
    }

    public GeometryFrame(PrintLayoutFrame plf, int type) {
        this.parent = plf;
        this.type = type;
        this.listener = new GeometryFrameListener(this, this.parent);
        this.onScreen = new GeometryFrameOnScreen();
        this.forPrint = new GeometryFrameForPrint();
        this.setName(this.parent.createName(this.getClass()));
        this.x1 = 10.0f;
        this.y1 = 10.0f;
        this.x2 = 20.0f;
        this.y2 = 20.0f;
        this.nuevo = true;
        this.id = last_id++;
    }

    public boolean isOpaque() {
        return this.opaque;
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

    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }

    public Color getLineColor() {
        return this.lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public Color getBackground() {
        return this.background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
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
        switch (this.type) {
            case 4: {
                return String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryFrame.square")) + "-" + this.id;
            }
            case 1: {
                return String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryFrame.rectangle")) + "-" + this.id;
            }
            case 5: {
                return String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryFrame.circle")) + "-" + this.id;
            }
            case 2: {
                return String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryFrame.ellipse")) + "-" + this.id;
            }
            case 3: {
                return String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryFrame.line")) + "-" + this.id;
            }
        }
        return String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryFrame.geometry")) + "-" + this.id;
    }

    @Override
    public void setSelected(boolean select) {
        this.selected = select;
        if (this.isSelected()) {
            this.onScreen.setBorder(selectedBorder);
        } else {
            this.onScreen.setBorder(null);
        }
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
        this.topLeftCorner = new Point(0, 0);
        this.topRightCorner = new Point((int)this.onScreen.getBounds().getWidth(), 0);
        this.bottomLeftCorner = new Point(0, (int)this.onScreen.getBounds().getHeight());
        this.bottomRightCorner = new Point((int)this.onScreen.getBounds().getWidth(), (int)this.onScreen.getBounds().getHeight());
    }

    @Override
    public void setBorder(Border border) {
        this.onScreen.setBorder(border);
    }

    @Override
    public Point[] getCornerPoint() {
        Point[] points = new Point[]{this.topLeftCorner, this.topRightCorner, this.bottomLeftCorner, this.bottomRightCorner};
        return points;
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
            case 3: {
                Rectangle rect = this.forPrint.getBounds();
                double f = this.parent.getZoomValue();
                this.onScreen.setBounds((int)((double)rect.x * f), (int)((double)rect.y * f), (int)((double)rect.width * f), (int)((double)rect.height * f));
                break;
            }
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

    public int getTopLeftCornerX() {
        return this.topLeftCorner.x;
    }

    public void setTopLeftCornerX(int n) {
        this.topLeftCorner.x = n;
    }

    public int getTopLeftCornerY() {
        return this.topLeftCorner.y;
    }

    public void setTopLeftCornerY(int n) {
        this.topLeftCorner.y = n;
    }

    public int getTopRightCornerX() {
        return this.topRightCorner.x;
    }

    public void setTopRightCornerX(int n) {
        this.topRightCorner.x = n;
    }

    public int getTopRightCornerY() {
        return this.topRightCorner.y;
    }

    public void setTopRightCornerY(int n) {
        this.topRightCorner.y = n;
    }

    public int getBottomLeftCornerX() {
        return this.bottomLeftCorner.x;
    }

    public void setBottomLeftCornerX(int n) {
        this.bottomLeftCorner.x = n;
    }

    public int getBottomLeftCornerY() {
        return this.bottomRightCorner.y;
    }

    public void setBottomLeftCornerY(int n) {
        this.bottomLeftCorner.y = n;
    }

    public int getBottomRightCornerX() {
        return this.bottomRightCorner.x;
    }

    public void setBottomRightCornerX(int n) {
        this.bottomRightCorner.x = n;
    }

    public int getBottomRightCornerY() {
        return this.bottomRightCorner.y;
    }

    public void setBottomRightCornerY(int n) {
        this.bottomRightCorner.y = n;
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

    private class GeometryFrameForPrint
    extends JPanel {
        public void adaptSize() {
            Rectangle r = this.getBounds();
            this.setBounds(r);
        }

        public void fixerDimensions() {
            switch (GeometryFrame.this.parent.getActiveZoom()) {
                case 0: 
                case 3: {
                    super.setBounds((int)Math.round(GeometryFrame.this.onScreen.getBounds().getX() * GeometryFrame.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(GeometryFrame.this.onScreen.getBounds().getY() * GeometryFrame.this.parent.getPrintScreenHeightAspectRatio()), (int)Math.round(GeometryFrame.this.onScreen.getBounds().getWidth() * GeometryFrame.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(GeometryFrame.this.onScreen.getBounds().getHeight() * GeometryFrame.this.parent.getPrintScreenHeightAspectRatio()));
                    break;
                }
                case 1: {
                    super.setBounds((int)GeometryFrame.this.onScreen.getBounds().getX(), (int)GeometryFrame.this.onScreen.getBounds().getY(), (int)GeometryFrame.this.onScreen.getBounds().getWidth(), (int)GeometryFrame.this.onScreen.getBounds().getHeight());
                }
            }
            super.setLocation((int)super.getBounds().getX(), (int)super.getBounds().getY());
            super.repaint();
        }

        private void adjustLine(int width, int height) {
            if (GeometryFrame.this.nuevo) {
                return;
            }
            if (GeometryFrame.this.x1 > (float)width) {
                GeometryFrame.this.x1 = width;
            }
            if (GeometryFrame.this.x2 > (float)width) {
                GeometryFrame.this.x2 = width;
            }
            if (GeometryFrame.this.x1 < 0.0f) {
                GeometryFrame.this.x1 = 0.0f;
            }
            if (GeometryFrame.this.x2 < 0.0f) {
                GeometryFrame.this.x2 = 0.0f;
            }
            if (GeometryFrame.this.y1 > (float)height) {
                GeometryFrame.this.y1 = height;
            }
            if (GeometryFrame.this.y2 > (float)height) {
                GeometryFrame.this.y2 = height;
            }
            if (GeometryFrame.this.y1 < 0.0f) {
                GeometryFrame.this.y1 = 0.0f;
            }
            if (GeometryFrame.this.y2 < 0.0f) {
                GeometryFrame.this.y2 = 0.0f;
            }
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            this.adjustLine(width, height);
            if (GeometryFrame.this.type == 4 || GeometryFrame.this.type == 5) {
                Rectangle old = this.getBounds();
                int w = old.width != width ? width : (old.height != height ? height : Math.min(width, height));
                super.setBounds(x, y, w, w);
            } else {
                super.setBounds(x, y, width, height);
            }
        }

        public void print(Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g;
            g2.translate(x, y);
            g2.setStroke(new BasicStroke(GeometryFrame.this.lineWidth));
            float correction = GeometryFrame.this.lineWidth / 2.0f;
            Shape geom = null;
            switch (GeometryFrame.this.type) {
                case 1: 
                case 4: {
                    geom = new Rectangle2D.Float(0.0f + correction, 0.0f + correction, (float)this.getWidth() - correction * 2.0f, (float)this.getHeight() - correction * 2.0f);
                    break;
                }
                case 2: 
                case 5: {
                    geom = new Ellipse2D.Float(0.0f + correction, 0.0f + correction, (float)this.getWidth() - correction * 2.0f, (float)this.getHeight() - correction * 2.0f);
                    break;
                }
                case 3: {
                    geom = new Line2D.Float(GeometryFrame.this.x1, GeometryFrame.this.y1, GeometryFrame.this.x2, GeometryFrame.this.y2);
                }
            }
            if (GeometryFrame.this.isOpaque()) {
                g2.setColor(GeometryFrame.this.getBackground());
                g2.fill(geom);
            }
            g2.setColor(GeometryFrame.this.getLineColor());
            g2.draw(geom);
            g2.translate(-x, -y);
        }
    }

    private class GeometryFrameListener
    extends GraphicElementsListener {
        public GeometryFrameListener(GraphicElements ge, PrintLayoutFrame plf) {
            super(ge, plf);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            GeometryFrame.this.onScreen.requestFocusInWindow();
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getClickCount() == 2) {
                    new GeometryProperties((GeometryFrame)this.ge);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (GeometryFrame.this.parent.getSelectedComponent() != null) {
                        GeometryFrame.this.parent.getSelectedComponent().setSelected(false);
                    }
                    this.ge.setSelected(true);
                    GeometryFrame.this.parent.setSelectedComponent(this.ge);
                }
                GeometryFrame.this.repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            float x = e.getX();
            float y = e.getY();
            float f = GeometryFrame.this.parent.getZoomValue();
            float d1 = (GeometryFrame.this.x1 * f - x) * (GeometryFrame.this.x1 * f - x) + (GeometryFrame.this.y1 * f - y) * (GeometryFrame.this.y1 * f - y);
            float d2 = (GeometryFrame.this.x2 * f - x) * (GeometryFrame.this.x2 * f - x) + (GeometryFrame.this.y2 * f - y) * (GeometryFrame.this.y2 * f - y);
            if (d1 < 100.0f) {
                GeometryFrame.this.nuevo = false;
                DragAndDropLock.setResizing(true);
                GeometryFrame.this.x1 = x / f;
                GeometryFrame.this.y1 = y / f;
                GeometryFrame.this.repaint();
            } else if (d2 < 100.0f) {
                GeometryFrame.this.nuevo = false;
                DragAndDropLock.setResizing(true);
                GeometryFrame.this.x2 = x / f;
                GeometryFrame.this.y2 = y / f;
                GeometryFrame.this.repaint();
            } else {
                super.mouseDragged(e);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            float x = e.getX();
            float y = e.getY();
            float f = GeometryFrame.this.parent.getZoomValue();
            float d1 = (GeometryFrame.this.x1 * f - x) * (GeometryFrame.this.x1 * f - x) + (GeometryFrame.this.y1 * f - y) * (GeometryFrame.this.y1 * f - y);
            float d2 = (GeometryFrame.this.x2 * f - x) * (GeometryFrame.this.x2 * f - x) + (GeometryFrame.this.y2 * f - y) * (GeometryFrame.this.y2 * f - y);
            if (d1 < 100.0f) {
                GeometryFrame.this.onScreen.setCursor(Cursor.getPredefinedCursor(12));
            } else if (d2 < 100.0f) {
                GeometryFrame.this.onScreen.setCursor(Cursor.getPredefinedCursor(12));
            } else {
                super.mouseMoved(e);
            }
        }
    }

    private class GeometryFrameOnScreen
    extends JPanel
    implements DragGestureListener,
    DragSourceListener,
    DragSourceMotionListener {
        public GeometryFrameOnScreen() {
            this.setOpaque(false);
            this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            this.addMouseListener(GeometryFrame.this.listener);
            this.addMouseMotionListener(GeometryFrame.this.listener);
            this.addKeyListener(GeometryFrame.this.listener);
            this.setFocusable(true);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this, 2, this);
            dragSource.addDragSourceMotionListener(this);
        }

        public void adaptSize() {
            Rectangle r = this.getBounds();
            this.setBounds(r);
        }

        public void dispose() {
            this.removeMouseListener(GeometryFrame.this.listener);
            this.removeMouseMotionListener(GeometryFrame.this.listener);
            this.removeKeyListener(GeometryFrame.this.listener);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.removeDragSourceMotionListener(this);
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            if (GeometryFrame.this.type == 4 || GeometryFrame.this.type == 5) {
                Rectangle old = this.getBounds();
                int w = old.width != width ? width : (old.height != height ? height : Math.min(width, height));
                super.setBounds(x, y, w, w);
            } else {
                super.setBounds(x, y, width, height);
            }
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
            Graphics2D g2 = (Graphics2D)g;
            float f = GeometryFrame.this.parent.getZoomValue();
            float lineWidth = GeometryFrame.this.lineWidth * f;
            g2.setStroke(new BasicStroke(lineWidth));
            float correction = lineWidth / 2.0f;
            Shape geom = null;
            switch (GeometryFrame.this.type) {
                case 1: 
                case 4: {
                    geom = new Rectangle2D.Float(0.0f + correction, 0.0f + correction, (float)this.getWidth() - correction * 2.0f, (float)this.getHeight() - correction * 2.0f);
                    break;
                }
                case 2: 
                case 5: {
                    geom = new Ellipse2D.Float(0.0f + correction, 0.0f + correction, (float)this.getWidth() - correction * 2.0f, (float)this.getHeight() - correction * 2.0f);
                    break;
                }
                case 3: {
                    geom = new Line2D.Float(GeometryFrame.this.x1 * f, GeometryFrame.this.y1 * f, GeometryFrame.this.x2 * f, GeometryFrame.this.y2 * f);
                }
            }
            if (GeometryFrame.this.isOpaque()) {
                g2.setColor(GeometryFrame.this.getBackground());
                g2.fill(geom);
            }
            g2.setColor(GeometryFrame.this.getLineColor());
            g2.draw(geom);
            if (GeometryFrame.this.type == 3) {
                g2.setColor(Color.RED);
                g2.fillOval((int)(GeometryFrame.this.x1 * f) - 1, (int)(GeometryFrame.this.y1 * f) - 1, 3, 3);
                g2.fillOval((int)(GeometryFrame.this.x2 * f) - 1, (int)(GeometryFrame.this.y2 * f) - 1, 3, 3);
            }
            if (GeometryFrame.this.isSelected()) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(lineWidth));
                g2.drawRect(0, 0, this.getWidth(), this.getHeight());
            }
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
            GeometryFrame.this.onScreen.paint(graphics2);
            dge.startDrag(Cursor.getDefaultCursor(), new GraphicElementTransferHandler(image, GeometryFrame.this), this);
            GhostGlassPane glassPane = (GhostGlassPane)SwingUtilities.getRootPane(this).getGlassPane();
            glassPane.setVisible(true);
            Point panelMiddlePoint = new Point(this.getBounds().width / 2, this.getBounds().height / 2);
            SwingUtilities.convertPointToScreen(panelMiddlePoint, this);
            Point p = (Point)dge.getDragOrigin().clone();
            SwingUtilities.convertPointToScreen(p, this);
            PrintStatic.xtrans = panelMiddlePoint.x - p.x;
            PrintStatic.ytrans = panelMiddlePoint.y - p.y;
            SwingUtilities.convertPointFromScreen(p, glassPane);
            p.translate(PrintStatic.xtrans, PrintStatic.ytrans);
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
                p.translate(PrintStatic.xtrans, PrintStatic.ytrans);
                glassPane.setPoint(p);
                glassPane.repaint(glassPane.getRepaintRect());
            }
        }
    }
}

