/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.north;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
import org.saig.jump.widgets.print.elements.north.NorthProperties;
import org.saig.jump.widgets.print.images.PrintIconLoader;
import org.saig.jump.widgets.print.images.norths.PredefinedNorthLoader;

public class NorthFrame
implements GraphicElements {
    public static final String DEFAULT_IMAGE = "North.gif";
    private PrintLayoutFrame parent;
    private String name = "";
    private Point topLeftCorner = new Point();
    private Point topRightCorner = new Point();
    private Point bottomLeftCorner = new Point();
    private Point bottomRightCorner = new Point();
    private ImageIcon northSymbol;
    private double aspectRatio = 0.0;
    private boolean selected = false;
    private NorthFrameOnScreen onScreen;
    private NorthFrameForPrint forPrint;
    private NorthFrameListener listener;
    private float lastZoomRotationFactor;

    public NorthFrame() {
        this.northSymbol = NorthFrame.createImageIcon(DEFAULT_IMAGE, DEFAULT_IMAGE);
        if (this.northSymbol != null) {
            this.aspectRatio = (double)this.northSymbol.getIconHeight() / (double)this.northSymbol.getIconWidth();
        }
        this.listener = new NorthFrameListener(this, this.parent);
        this.onScreen = new NorthFrameOnScreen();
        this.forPrint = new NorthFrameForPrint();
    }

    public NorthFrame(PrintLayoutFrame plf) {
        this.parent = plf;
        this.northSymbol = NorthFrame.createImageIcon(DEFAULT_IMAGE, DEFAULT_IMAGE);
        if (this.northSymbol != null) {
            this.aspectRatio = (double)this.northSymbol.getIconHeight() / (double)this.northSymbol.getIconWidth();
        }
        this.listener = new NorthFrameListener(this, this.parent);
        this.onScreen = new NorthFrameOnScreen();
        this.forPrint = new NorthFrameForPrint();
        this.setName(this.parent.createName(this.getClass()));
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    public float getLastZoomRotationFactor() {
        return this.lastZoomRotationFactor;
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
    public Point[] getCornerPoint() {
        Point[] points = new Point[]{this.topLeftCorner, this.topRightCorner, this.bottomLeftCorner, this.bottomRightCorner};
        return points;
    }

    @Override
    public void fixerDimensions(int x, int y, int w, int h, int facteur1, int facteur2) {
        this.onScreen.setBounds(x, y, w, h);
        this.onScreen.setLocation(x, y);
        this.forPrint.fixerDimensions();
        this.resizeIcon();
        this.initCornerPoint();
        this.repaint();
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
        this.resizeIcon();
        this.initCornerPoint();
        this.onScreen.resizeIcon();
        this.onScreen.repaint();
    }

    @Override
    public void zoom(float f) {
        this.onScreen.setBounds((int)Math.round(this.forPrint.getBounds().getX() * (double)f), (int)Math.round(this.forPrint.getBounds().getY() * (double)f), (int)Math.round(this.forPrint.getBounds().getWidth() * (double)f), (int)Math.round(this.forPrint.getBounds().getHeight() * (double)f));
        this.initCornerPoint();
        this.onScreen.repaint();
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
        this.onScreen.resizeIcon();
        this.onScreen.repaint();
    }

    private static ImageIcon createImageIcon(String path, String description) {
        URL imgURL = PrintIconLoader.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        }
        System.err.println(String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.north.NorthFrame.couldnt-find-file")) + ": " + path);
        return null;
    }

    private void resizeIcon() {
        Image icon = null;
        Image iconPrint = null;
        int newOnScreenHeight = 0;
        int newOnScreenWidth = 0;
        if (this.onScreen.getHeight() != 0 && this.onScreen.getWidth() != 0) {
            if (this.aspectRatio == 1.0) {
                int min;
                newOnScreenHeight = min = Math.min(this.onScreen.getHeight(), this.onScreen.getWidth());
                newOnScreenWidth = min;
            } else {
                int maxHeight = (int)((double)this.onScreen.getWidth() * this.aspectRatio);
                int maxWidth = (int)((double)this.onScreen.getHeight() / this.aspectRatio);
                if (maxHeight > this.onScreen.getHeight()) {
                    newOnScreenHeight = this.onScreen.getHeight();
                    newOnScreenWidth = maxWidth;
                } else if (maxWidth > this.onScreen.getWidth()) {
                    newOnScreenHeight = maxHeight;
                    newOnScreenWidth = this.onScreen.getWidth();
                } else {
                    newOnScreenHeight = this.onScreen.getHeight();
                    newOnScreenWidth = this.onScreen.getWidth();
                }
            }
            if (newOnScreenHeight > 0 && newOnScreenWidth > 0) {
                icon = this.northSymbol.getImage().getScaledInstance(newOnScreenWidth, newOnScreenHeight, 4);
            }
            if (icon != null) {
                this.onScreen.setIcon(new ImageIcon(icon));
            }
        }
        int newOnPrintHeight = 0;
        int newOnPrintWidth = 0;
        if (this.forPrint.getHeight() != 0 && this.forPrint.getWidth() != 0) {
            if (this.aspectRatio == 1.0) {
                int min;
                newOnPrintHeight = min = Math.min(this.forPrint.getHeight(), this.forPrint.getWidth());
                newOnScreenWidth = min;
            } else {
                int maxHeight = (int)((double)this.forPrint.getWidth() * this.aspectRatio);
                int maxWidth = (int)((double)this.forPrint.getHeight() / this.aspectRatio);
                if (maxHeight > this.forPrint.getHeight()) {
                    newOnPrintHeight = this.forPrint.getHeight();
                    newOnPrintWidth = maxWidth;
                } else if (maxWidth > this.forPrint.getWidth()) {
                    newOnPrintHeight = maxHeight;
                    newOnPrintWidth = this.forPrint.getWidth();
                } else {
                    newOnPrintHeight = this.forPrint.getHeight();
                    newOnPrintWidth = this.forPrint.getWidth();
                }
                if (newOnPrintHeight > 0 && newOnPrintWidth > 0) {
                    iconPrint = this.northSymbol.getImage().getScaledInstance(newOnPrintWidth, newOnPrintHeight, 4);
                }
            }
            if (iconPrint != null) {
                this.forPrint.setIcon(new ImageIcon(iconPrint));
            }
        }
    }

    @Override
    public void repaint() {
        this.resizeIcon();
        this.onScreen.repaint();
        this.forPrint.repaint();
    }

    @Override
    public void setBorder(Border border) {
        this.onScreen.setBorder(border);
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
    public void setGraphicAttributes() {
        this.setBorder(null);
        this.onScreen.setCursor(Cursor.getPredefinedCursor(13));
        this.onScreen.validate();
    }

    @Override
    public void initGraphicAttributes(PrintLayoutFrame plf) {
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
        return this.bottomLeftCorner.y;
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

    public ImageIcon getNorthSymbol() {
        return this.northSymbol;
    }

    public void setNorthSymbol(ImageIcon northSymbol) {
        this.northSymbol = PredefinedNorthLoader.icon(northSymbol.getDescription());
        this.northSymbol.setDescription(northSymbol.getDescription());
        this.onScreen.setIcon(northSymbol);
        this.forPrint.setIcon(northSymbol);
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
    public Icon getIcon() {
        return PrintIconLoader.icon("addNorth.gif");
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
    public PrintLayoutFrame getParent() {
        return this.parent;
    }

    public String toString() {
        return I18N.getMessage("org.saig.jump.widgets.print.elements.north.NorthFrame.north-{0}", new Object[]{this.getName()});
    }

    @Override
    public void setResizing(boolean resizing) {
    }

    private class NorthFrameForPrint
    extends JLabel {
        public NorthFrameForPrint() {
            this.setIcon(NorthFrame.this.northSymbol);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }

        public void fixerDimensions() {
            switch (NorthFrame.this.parent.getActiveZoom()) {
                case 0: 
                case 3: {
                    super.setBounds((int)Math.round(NorthFrame.this.onScreen.getBounds().getX() * NorthFrame.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(NorthFrame.this.onScreen.getBounds().getY() * NorthFrame.this.parent.getPrintScreenHeightAspectRatio()), (int)Math.round(NorthFrame.this.onScreen.getBounds().getWidth() * NorthFrame.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(NorthFrame.this.onScreen.getBounds().getHeight() * NorthFrame.this.parent.getPrintScreenHeightAspectRatio()));
                    break;
                }
                case 1: {
                    super.setBounds((int)NorthFrame.this.onScreen.getBounds().getX(), (int)NorthFrame.this.onScreen.getBounds().getY(), (int)NorthFrame.this.onScreen.getBounds().getWidth(), (int)NorthFrame.this.onScreen.getBounds().getHeight());
                }
            }
            super.setLocation((int)super.getBounds().getX(), (int)super.getBounds().getY());
            this.resizeIcon();
            super.repaint();
        }

        public void resizeIcon() {
            Image iconPrint = null;
            int newOnPrintHeight = 0;
            int newOnPrintWidth = 0;
            if (NorthFrame.this.forPrint.getHeight() != 0 && NorthFrame.this.forPrint.getWidth() != 0) {
                if (NorthFrame.this.aspectRatio == 1.0) {
                    int min;
                    newOnPrintHeight = min = Math.min(NorthFrame.this.forPrint.getHeight(), NorthFrame.this.forPrint.getWidth());
                    newOnPrintWidth = min;
                } else {
                    int maxHeight = (int)((double)NorthFrame.this.forPrint.getWidth() * NorthFrame.this.aspectRatio);
                    int maxWidth = (int)((double)NorthFrame.this.forPrint.getHeight() / NorthFrame.this.aspectRatio);
                    if (maxHeight > NorthFrame.this.forPrint.getHeight()) {
                        newOnPrintHeight = NorthFrame.this.forPrint.getHeight();
                        newOnPrintWidth = maxWidth;
                    } else if (maxWidth > NorthFrame.this.forPrint.getWidth()) {
                        newOnPrintHeight = maxHeight;
                        newOnPrintWidth = NorthFrame.this.forPrint.getWidth();
                    } else {
                        newOnPrintHeight = NorthFrame.this.forPrint.getHeight();
                        newOnPrintWidth = NorthFrame.this.forPrint.getWidth();
                    }
                }
                if (newOnPrintHeight > 0 && newOnPrintWidth > 0) {
                    iconPrint = NorthFrame.this.northSymbol.getImage().getScaledInstance(newOnPrintWidth, newOnPrintHeight, 4);
                }
                if (iconPrint != null) {
                    NorthFrame.this.forPrint.setIcon(new ImageIcon(iconPrint));
                }
            }
        }
    }

    private class NorthFrameListener
    extends GraphicElementsListener {
        public NorthFrameListener(GraphicElements ge, PrintLayoutFrame plf) {
            super(ge, plf);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            NorthFrame.this.onScreen.requestFocusInWindow();
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getClickCount() == 2) {
                    new NorthProperties((NorthFrame)this.ge);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (NorthFrame.this.parent.getSelectedComponent() != null) {
                        NorthFrame.this.parent.getSelectedComponent().setSelected(false);
                    }
                    this.ge.setSelected(true);
                    NorthFrame.this.parent.setSelectedComponent(this.ge);
                }
                NorthFrame.this.repaint();
            }
        }
    }

    private class NorthFrameOnScreen
    extends JLabel
    implements DragGestureListener,
    DragSourceListener,
    DragSourceMotionListener {
        public NorthFrameOnScreen() {
            this.setIcon(NorthFrame.this.northSymbol);
            this.setHorizontalAlignment(0);
            this.setVerticalAlignment(0);
            this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            this.addMouseListener(NorthFrame.this.listener);
            this.addMouseMotionListener(NorthFrame.this.listener);
            this.addKeyListener(NorthFrame.this.listener);
            this.setFocusable(true);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this, 2, this);
            dragSource.addDragSourceMotionListener(this);
        }

        public void dispose() {
            this.removeMouseListener(NorthFrame.this.listener);
            this.removeMouseMotionListener(NorthFrame.this.listener);
            this.removeKeyListener(NorthFrame.this.listener);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.removeDragSourceMotionListener(this);
        }

        public void resizeIcon() {
            Image icon = null;
            int newOnScreenHeight = 0;
            int newOnScreenWidth = 0;
            if (NorthFrame.this.onScreen.getHeight() != 0 && NorthFrame.this.onScreen.getWidth() != 0) {
                if (NorthFrame.this.aspectRatio == 1.0) {
                    int min;
                    newOnScreenHeight = min = Math.min(NorthFrame.this.onScreen.getHeight(), NorthFrame.this.onScreen.getWidth());
                    newOnScreenWidth = min;
                } else {
                    int maxHeight = (int)((double)NorthFrame.this.onScreen.getWidth() * NorthFrame.this.aspectRatio);
                    int maxWidth = (int)((double)NorthFrame.this.onScreen.getHeight() / NorthFrame.this.aspectRatio);
                    if (maxHeight > NorthFrame.this.onScreen.getHeight()) {
                        newOnScreenHeight = NorthFrame.this.onScreen.getHeight();
                        newOnScreenWidth = maxWidth;
                    } else if (maxWidth > NorthFrame.this.onScreen.getWidth()) {
                        newOnScreenHeight = maxHeight;
                        newOnScreenWidth = NorthFrame.this.onScreen.getWidth();
                    } else {
                        newOnScreenHeight = NorthFrame.this.onScreen.getHeight();
                        newOnScreenWidth = NorthFrame.this.onScreen.getWidth();
                    }
                }
                if (newOnScreenHeight > 0 && newOnScreenWidth > 0) {
                    icon = NorthFrame.this.northSymbol.getImage().getScaledInstance(newOnScreenWidth, newOnScreenHeight, 4);
                }
                if (icon != null) {
                    NorthFrame.this.onScreen.setIcon(new ImageIcon(icon));
                }
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
            NorthFrame.this.onScreen.paint(graphics2);
            dge.startDrag(Cursor.getDefaultCursor(), new GraphicElementTransferHandler(image, NorthFrame.this), this);
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

        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);
            this.resizeIcon();
            if (this.getGraphics() != null) {
                this.paint(this.getGraphics());
            }
        }

        @Override
        public void setBounds(Rectangle rect) {
            super.setBounds(rect);
            this.resizeIcon();
            if (this.getGraphics() != null) {
                this.paint(this.getGraphics());
            }
        }
    }
}

