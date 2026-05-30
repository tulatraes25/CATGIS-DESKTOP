/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.jump.widgets.print.elements.scale;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.ViewportListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.dnd.DragAndDropLock;
import org.saig.jump.widgets.dnd.GhostGlassPane;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.dnd.GraphicElementTransferHandler;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.GraphicElementsListener;
import org.saig.jump.widgets.print.elements.map.MapFrame;
import org.saig.jump.widgets.print.elements.scale.MapScale;
import org.saig.jump.widgets.print.elements.scale.ScaleProperties;
import org.saig.jump.widgets.print.images.PrintIconLoader;

public class ScaleFrame
implements GraphicElements,
ViewportListener {
    private PrintLayoutFrame parent;
    private MapFrame associatedMapFrame;
    private String associatedMapFrameName = "";
    private String name = "";
    public Point topLeftCorner = new Point();
    public Point topRightCorner = new Point();
    public Point bottomLeftCorner = new Point();
    public Point bottomRightCorner = new Point();
    private boolean select = false;
    private Color borderColor = Color.GRAY;
    private int borderThickness = 1;
    private Border border = BorderFactory.createLineBorder(this.borderColor, this.borderThickness);
    private ScaleFrameOnScreen onScreen;
    private ScaleFrameForPrint forPrint;
    private ScaleFrameListener listener;

    public ScaleFrame(PrintLayoutFrame plf) {
        this.parent = plf;
        this.associatedMapFrame = plf.getMapElement();
        LayerViewPanel onScreenViewPanel = null;
        LayerViewPanel forPrintScreenViewPanel = null;
        if (this.associatedMapFrame != null) {
            this.associatedMapFrameName = this.associatedMapFrame.getName();
            onScreenViewPanel = (LayerViewPanel)this.associatedMapFrame.getGraphicElementsOnScreen();
            forPrintScreenViewPanel = (LayerViewPanel)this.associatedMapFrame.getGraphicElementsForPrint();
        }
        this.listener = new ScaleFrameListener(this, this.parent);
        this.onScreen = new ScaleFrameOnScreen(onScreenViewPanel);
        this.forPrint = new ScaleFrameForPrint(forPrintScreenViewPanel);
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setName(this.parent.createName(this.getClass()));
        this.initGraphicAttributes(this.parent);
    }

    public ScaleFrame() {
        this.listener = new ScaleFrameListener(this, this.parent);
        this.onScreen = new ScaleFrameOnScreen();
        this.forPrint = new ScaleFrameForPrint();
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    public MapScale getMapScale() {
        return (MapScale)this.forPrint.getComponent(0);
    }

    @Override
    public boolean isSelected() {
        return this.select;
    }

    @Override
    public void setSelected(boolean select) {
        this.select = select;
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

    public Border getBorder() {
        return this.border;
    }

    @Override
    public void setBorder(Border border) {
        this.border = border;
        if (border != null) {
            this.borderThickness = ((LineBorder)this.getBorder()).getThickness();
            this.borderColor = ((LineBorder)this.getBorder()).getLineColor();
        }
        this.onScreen.setBorder(border);
        this.forPrint.setBorder(border);
        this.repaint();
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
        this.repaint();
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
    public void setGraphicAttributes() {
    }

    @Override
    public void initGraphicAttributes(PrintLayoutFrame plf) {
        if (this.associatedMapFrame == null) {
            this.associatedMapFrame = this.associatedMapFrameName.equals("") ? plf.getMapElement() : plf.getMapFrameByName(this.associatedMapFrameName);
        }
        if (this.associatedMapFrame != null) {
            this.associatedMapFrameName = this.associatedMapFrame.getName();
            ((LayerViewPanel)this.associatedMapFrame.getGraphicElementsOnScreen()).getViewport().addListener(this);
        }
        this.onScreen.initialize();
        this.forPrint.initialize();
    }

    @Override
    public void zoomChanged(Envelope modelEnvelope) {
        this.initZoom();
    }

    public MapScale getMapScaleOnScreen() {
        return (MapScale)this.onScreen.getComponent(0);
    }

    public void initZoom() {
        this.onScreen.updateZoom(this.parent.getLayerViewPanel());
        this.forPrint.updateZoom(this.parent.getLayerViewPanel());
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
        return PrintIconLoader.icon("addScale.gif");
    }

    @Override
    public void dispose() {
        if (this.associatedMapFrame != null && (LayerViewPanel)this.associatedMapFrame.getGraphicElementsOnScreen() != null && ((LayerViewPanel)this.associatedMapFrame.getGraphicElementsOnScreen()).getViewport() != null) {
            ((LayerViewPanel)this.associatedMapFrame.getGraphicElementsOnScreen()).getViewport().removeListener(this);
            this.associatedMapFrame = null;
        }
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

    public String getAssociatedMapFrameName() {
        return this.associatedMapFrameName;
    }

    public void setAssociatedMapFrameName(String associatedMapFrameName) {
        this.associatedMapFrameName = associatedMapFrameName;
    }

    @Override
    public PrintLayoutFrame getParent() {
        return this.parent;
    }

    public MapFrame getAssociatedMapFrame() {
        return this.associatedMapFrame;
    }

    public void setAssociatedMapFrame(MapFrame associatedMapFrame) {
        this.associatedMapFrame = associatedMapFrame;
    }

    public void refreshAssociatedMapFrame(MapFrame mapFrame) {
        if (this.associatedMapFrameName.equals(mapFrame.getName())) {
            return;
        }
        this.associatedMapFrame.removeListeners(this);
        this.setAssociatedMapFrame(mapFrame);
        this.setAssociatedMapFrameName(mapFrame.getName());
        ((LayerViewPanel)this.associatedMapFrame.getGraphicElementsOnScreen()).getViewport().addListener(this);
        this.onScreen.removeAll();
        MapScale element = new MapScale((LayerViewPanel)this.associatedMapFrame.getGraphicElementsOnScreen());
        element.setMaximumSize(new Dimension(1000, 25));
        element.setAlignmentX(0.0f);
        element.setAlignmentY(0.5f);
        this.onScreen.add((Component)element, "Center");
        this.onScreen.validate();
        this.repaint();
    }

    public String toString() {
        return I18N.getMessage("org.saig.jump.widgets.print.elements.scale.ScaleFrame.scale-{0}", new Object[]{this.getName()});
    }

    @Override
    public void setResizing(boolean resizing) {
    }

    private class ScaleFrameForPrint
    extends JPanel {
        public ScaleFrameForPrint() {
            this.setOpaque(false);
            this.setLayout(new BorderLayout());
        }

        public ScaleFrameForPrint(LayerViewPanel panel) {
            this.setOpaque(false);
            this.setLayout(new BorderLayout());
            MapScale element = new MapScale(panel);
            element.setMaximumSize(new Dimension(1000, 25));
            element.setAlignmentX(0.0f);
            element.setAlignmentY(0.5f);
            this.add((Component)element, "Center");
        }

        public void fixerDimensions() {
            switch (ScaleFrame.this.parent.getActiveZoom()) {
                case 0: 
                case 3: {
                    super.setBounds((int)Math.round(ScaleFrame.this.onScreen.getBounds().getX() * ScaleFrame.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(ScaleFrame.this.onScreen.getBounds().getY() * ScaleFrame.this.parent.getPrintScreenHeightAspectRatio()), (int)Math.round(ScaleFrame.this.onScreen.getBounds().getWidth() * ScaleFrame.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(ScaleFrame.this.onScreen.getBounds().getHeight() * ScaleFrame.this.parent.getPrintScreenHeightAspectRatio()));
                    break;
                }
                case 1: {
                    super.setBounds((int)ScaleFrame.this.onScreen.getBounds().getX(), (int)ScaleFrame.this.onScreen.getBounds().getY(), (int)ScaleFrame.this.onScreen.getBounds().getWidth(), (int)ScaleFrame.this.onScreen.getBounds().getHeight());
                }
            }
            super.setLocation((int)super.getBounds().getX(), (int)super.getBounds().getY());
            super.repaint();
        }

        public void updateZoom(LayerViewPanel panel) {
            this.repaint();
        }

        public void initialize() {
            if (ScaleFrame.this.associatedMapFrame != null) {
                MapScale element = new MapScale((LayerViewPanel)ScaleFrame.this.associatedMapFrame.getGraphicElementsForPrint());
                element.setMaximumSize(new Dimension(1000, 25));
                element.setAlignmentX(0.0f);
                element.setAlignmentY(0.5f);
                this.add((Component)element, "Center");
            }
        }
    }

    private class ScaleFrameListener
    extends GraphicElementsListener {
        public ScaleFrameListener(GraphicElements ge, PrintLayoutFrame plf) {
            super(ge, plf);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            ScaleFrame.this.onScreen.requestFocusInWindow();
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getClickCount() == 2) {
                    new ScaleProperties((ScaleFrame)this.ge);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (ScaleFrame.this.parent.getSelectedComponent() != null) {
                        ScaleFrame.this.parent.getSelectedComponent().setSelected(false);
                    }
                    this.ge.setSelected(true);
                    ScaleFrame.this.parent.setSelectedComponent(this.ge);
                }
                ScaleFrame.this.repaint();
            }
        }
    }

    private class ScaleFrameOnScreen
    extends JPanel
    implements DragGestureListener,
    DragSourceListener,
    DragSourceMotionListener {
        public ScaleFrameOnScreen(LayerViewPanel panel) {
            this.setOpaque(false);
            BorderLayout layout = new BorderLayout();
            this.setLayout(layout);
            MapScale element = new MapScale(panel);
            element.setMaximumSize(new Dimension(1000, 25));
            element.setAlignmentX(0.0f);
            element.setAlignmentY(0.5f);
            this.add((Component)element, "Center");
            this.addMouseListener(ScaleFrame.this.listener);
            this.addMouseMotionListener(ScaleFrame.this.listener);
            this.addKeyListener(ScaleFrame.this.listener);
            this.setFocusable(true);
            this.repaint();
        }

        public ScaleFrameOnScreen() {
            this.setOpaque(false);
            BorderLayout layout = new BorderLayout();
            this.setLayout(layout);
            this.addMouseListener(ScaleFrame.this.listener);
            this.addMouseMotionListener(ScaleFrame.this.listener);
            this.addKeyListener(ScaleFrame.this.listener);
            this.setFocusable(true);
            this.repaint();
        }

        public void dispose() {
            this.removeMouseListener(ScaleFrame.this.listener);
            this.removeMouseMotionListener(ScaleFrame.this.listener);
            this.removeKeyListener(ScaleFrame.this.listener);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.removeDragSourceMotionListener(this);
        }

        private void initDragAndDrop() {
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this, 2, this);
            dragSource.addDragSourceMotionListener(this);
        }

        public void initialize() {
            if (ScaleFrame.this.associatedMapFrame != null) {
                MapScale element = new MapScale((LayerViewPanel)ScaleFrame.this.associatedMapFrame.getGraphicElementsOnScreen());
                element.setMaximumSize(new Dimension(1000, 25));
                element.setAlignmentX(0.0f);
                element.setAlignmentY(0.5f);
                this.add((Component)element, "Center");
            }
            this.initDragAndDrop();
        }

        public void updateZoom(LayerViewPanel panel) {
            this.repaint();
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
            ScaleFrame.this.getMapScaleOnScreen().paint(graphics2);
            dge.startDrag(Cursor.getDefaultCursor(), new GraphicElementTransferHandler(image, ScaleFrame.this), this);
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

