/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Area
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package org.saig.jump.widgets.print.elements.map;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelListener;
import com.vividsolutions.jump.workbench.ui.ViewportListener;
import java.awt.BasicStroke;
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
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.saig.jump.widgets.dnd.DragAndDropLock;
import org.saig.jump.widgets.dnd.GhostGlassPane;
import org.saig.jump.widgets.print.Conversion;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.PrintStatic;
import org.saig.jump.widgets.print.dnd.GraphicElementTransferHandler;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.GraphicElementsListener;
import org.saig.jump.widgets.print.elements.map.MapProperties;
import org.saig.jump.widgets.print.elements.scale.ScaleFrame;
import org.saig.jump.widgets.print.images.PrintIconLoader;

public class MapFrame
implements GraphicElements,
ViewportListener,
LayerListener {
    private PrintLayoutFrame parent;
    private String name = "";
    private Point topLeftCorner = new Point();
    private Point topRightCorner = new Point();
    private Point bottomLeftCorner = new Point();
    private Point bottomRightCorner = new Point();
    private boolean selected = false;
    private MapFrameOnScreen onScreen;
    private MapFrameForPrint forPrint;
    private MapFrameListener listener;
    private Border defaultBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
    private static double currentScale;
    private boolean resize = false;
    private float lastZoomRotationFactor;
    private double factorResolucion = 4.0;
    private boolean resizing = false;
    private static int k;

    static {
        k = 0;
    }

    public MapFrame(PrintLayoutFrame plf) {
        this.parent = plf;
        this.listener = new MapFrameListener(this, this.parent);
        this.onScreen = new MapFrameOnScreen(this.parent.getTaskFrame().getLayerManager(), JUMPWorkbench.getFrameInstance(), this.parent.getTaskFrame().getLayerViewPanel());
        this.forPrint = new MapFrameForPrint(this.parent.getTaskFrame().getLayerManager(), JUMPWorkbench.getFrameInstance(), this.parent.getTaskFrame().getLayerViewPanel());
        this.setName(this.parent.createName(this.getClass()));
    }

    public MapFrame() {
        this.listener = new MapFrameListener(this, null);
        this.onScreen = new MapFrameOnScreen();
        this.forPrint = new MapFrameForPrint();
    }

    public void updateRepaint(boolean repaint) {
        this.onScreen.setRepaint(repaint, false);
    }

    public float getLastZoomRotationFactor() {
        return this.lastZoomRotationFactor;
    }

    public boolean isRepaint() {
        return this.onScreen.isRepaint();
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    public void changeUnits(double factor, Unit<Length> mapLengthUnits, Unit<Length> userLengthUnits, Unit<Area> userAreaUnits) {
        this.onScreen.setFactor(factor);
        this.onScreen.setMapLengthUnit(mapLengthUnits);
        this.onScreen.setUserLengthUnit(userLengthUnits);
        this.onScreen.setUserAreaUnit(userAreaUnits);
        this.forPrint.setFactor(factor);
        this.forPrint.setMapLengthUnit(mapLengthUnits);
        this.forPrint.setUserLengthUnit(userLengthUnits);
        this.forPrint.setUserAreaUnit(userAreaUnits);
    }

    @Override
    public void setSelected(boolean select) {
        this.selected = select;
        boolean repaint = this.onScreen.repaint;
        this.onScreen.setRepaint(false, false);
        if (this.isSelected()) {
            this.onScreen.setBorder(selectedBorder);
        } else {
            this.onScreen.setBorder(this.defaultBorder);
        }
        this.onScreen.setRepaint(repaint, false);
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
        this.initZoom();
        this.initCornerPoint();
        this.repaint(false);
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
        this.initZoom();
        this.repaint();
    }

    @Override
    public void zoom(float f) {
        this.onScreen.setBounds((int)Math.round(this.forPrint.getBounds().getX() * (double)f), (int)Math.round(this.forPrint.getBounds().getY() * (double)f), (int)Math.round(this.forPrint.getBounds().getWidth() * (double)f), (int)Math.round(this.forPrint.getBounds().getHeight() * (double)f));
        this.initCornerPoint();
        this.initZoom();
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
        this.initZoom();
        this.onScreen.repaint();
    }

    public void initZoom() {
        try {
            Envelope envelope = this.parent.getTaskFrame().getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
            Envelope currentEnvelope = this.onScreen.getViewport().getEnvelopeInModelCoordinates();
            if (!currentEnvelope.equals((Object)envelope)) {
                this.onScreen.setRepaint(false, false);
                this.onScreen.getViewport().zoom(envelope, true);
                this.onScreen.setZoomScheduled(envelope);
                this.forPrint.getViewport().zoom(envelope, false);
                this.onScreen.setRepaint(true, false);
            } else {
                this.onScreen.repaint(false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeZoom() {
        try {
            Envelope envelope = this.parent.getTaskFrame().getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
            Envelope currentEnvelope = this.onScreen.getViewport().getEnvelopeInModelCoordinates();
            if (!currentEnvelope.equals((Object)envelope)) {
                this.onScreen.setZoomScheduled(envelope);
                this.forPrint.getViewport().zoom(envelope, false);
            } else {
                this.onScreen.repaint();
            }
        }
        catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    public void changeZoom(Envelope env) {
        try {
            this.forPrint.getViewport().zoom(env);
            this.onScreen.getViewport().zoom(env);
        }
        catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setBorder(Border border) {
        this.onScreen.setBorder(border);
        this.repaint();
    }

    @Override
    public void repaint() {
        this.onScreen.repaint();
        this.forPrint.repaint();
        this.getScale();
    }

    public void repaint(boolean repaint) {
        this.onScreen.repaint(repaint);
        this.forPrint.repaint();
        this.getScale();
    }

    public double getScale() {
        currentScale = this.forPrint.getScale();
        return currentScale;
    }

    public void changeViewZoom(Envelope newEnvelope) throws NoninvertibleTransformException {
        this.parent.getTaskFrame().getLayerViewPanel().getViewport().zoom(newEnvelope, true);
    }

    @Override
    public void zoomChanged(Envelope modelEnvelope) {
        this.changeZoom();
    }

    public void refreshForFocusGained() {
        if (this.onScreen != null && this.onScreen.associatedLayerViewPanel != null && this.onScreen.associatedLayerViewPanel.getRenderingManager() != null && this.onScreen.associatedLayerViewPanel.getRenderingManager().isRendering()) {
            this.onScreen.getRenderingManager().cancelAll();
        } else if (this.onScreen != null) {
            this.onScreen.renderingFinished();
        }
    }

    public void refreshForFocusLost() {
        if (this.onScreen.getRenderingManager().isRendering()) {
            this.onScreen.getRenderingManager().cancelAll();
        }
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
    public void setGraphicAttributes() {
        this.onScreen.setCursor(Cursor.getPredefinedCursor(13));
        this.onScreen.validate();
        this.setBorder(this.defaultBorder);
    }

    @Override
    public void initGraphicAttributes(PrintLayoutFrame plf) {
        plf.getTaskFrame().getLayerManager().addLayerListener(this);
        plf.getTaskFrame().getLayerViewPanel().getViewport().addListener(this);
        this.onScreen.init(plf.getTaskFrame().getLayerManager(), JUMPWorkbench.getFrameInstance());
        this.onScreen.initialize(plf);
        this.forPrint.init(plf.getTaskFrame().getLayerManager(), JUMPWorkbench.getFrameInstance());
        this.listener.initialize(plf, this.getCornerPoint());
    }

    @Override
    public void setParent(PrintLayoutFrame plf) {
        this.parent = plf;
        this.listener.setFrame(plf);
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

    @Override
    public void featuresChanged(FeatureEvent e) {
    }

    @Override
    public void layerChanged(LayerEvent e) {
    }

    @Override
    public void categoryChanged(CategoryEvent e) {
    }

    @Override
    public void refresh() {
        if (this.parent.getSelectedComponent() != null) {
            this.parent.getSelectedComponent().setSelected(false);
        }
        this.setSelected(true);
        this.parent.setSelectedComponent(this);
        boolean repaint = this.onScreen.repaint;
        this.onScreen.setRepaint(false, false);
        this.repaint();
        this.onScreen.setRepaint(repaint, false);
    }

    @Override
    public PrintLayoutFrame getParent() {
        return this.parent;
    }

    @Override
    public Icon getIcon() {
        return PrintIconLoader.icon("addView.gif");
    }

    @Override
    public void dispose() {
        if (this.parent != null && this.parent.getTaskFrame() != null && this.parent.getTaskFrame().getTask() != null) {
            if (this.parent.getTaskFrame().getLayerManager() != null) {
                this.parent.getTaskFrame().getLayerManager().removeLayerListener(this);
            }
            if (this.parent.getTaskFrame().getLayerViewPanel() != null) {
                this.parent.getTaskFrame().getLayerViewPanel().getViewport().removeListener(this);
            }
        }
        if (this.onScreen != null) {
            this.onScreen.dispose();
            this.onScreen = null;
        }
        if (this.forPrint != null) {
            this.forPrint.dispose();
        }
        if (this.listener != null) {
            this.listener.dispose();
            this.listener = null;
        }
        this.parent = null;
        this.forPrint = null;
    }

    public String toString() {
        return this.getName();
    }

    public void removeListeners(ScaleFrame frame) {
        this.onScreen.getViewport().removeListener(frame);
    }

    @Override
    public void setResizing(boolean resizing) {
        this.resizing = resizing;
    }

    public Unit<Length> getUserLengthUnits() {
        return this.forPrint.getUserLengthUnit();
    }

    private class MapFrameForPrint
    extends LayerViewPanel {
        public MapFrameForPrint() {
        }

        public MapFrameForPrint(LayerManager layerManager, LayerViewPanelContext context, LayerViewPanel viewPanel) {
            super(layerManager, context);
            this.superRepaint();
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);
        }

        @Override
        public void setBounds(Rectangle rect) {
            super.setBounds(rect);
        }

        @Override
        public void repaint() {
        }

        @Override
        public void repaint(boolean repaint) {
        }

        @Override
        public double getScale() {
            Envelope envelope = this.getViewport().getEnvelopeInModelCoordinates();
            double k = Conversion.seventyTwoInch_To_Cm(MapFrame.this.parent.getPageFormat().getWidth()) / (double)MapFrame.this.parent.getPage().getWidth();
            double scale = (envelope.getMaxX() - envelope.getMinX()) * (1.0 / k) * 100.0 / (double)this.getWidth() * this.getFactor();
            return scale;
        }

        public void fixerDimensions() {
            switch (MapFrame.this.parent.getActiveZoom()) {
                case 0: 
                case 3: {
                    super.setBounds((int)Math.round(MapFrame.this.onScreen.getBounds().getX() * MapFrame.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(MapFrame.this.onScreen.getBounds().getY() * MapFrame.this.parent.getPrintScreenHeightAspectRatio()), (int)Math.round(MapFrame.this.onScreen.getBounds().getWidth() * MapFrame.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(MapFrame.this.onScreen.getBounds().getHeight() * MapFrame.this.parent.getPrintScreenHeightAspectRatio()));
                    break;
                }
                case 1: {
                    super.setBounds((int)MapFrame.this.onScreen.getBounds().getX(), (int)MapFrame.this.onScreen.getBounds().getY(), (int)MapFrame.this.onScreen.getBounds().getWidth(), (int)MapFrame.this.onScreen.getBounds().getHeight());
                }
            }
            super.setLocation((int)super.getBounds().getX(), (int)super.getBounds().getY());
        }

        @Override
        public void layerChanged(LayerEvent e) {
        }

        @Override
        protected void this_componentResized(ComponentEvent e) {
        }
    }

    private class MapFrameListener
    extends GraphicElementsListener {
        public MapFrameListener(GraphicElements ge, PrintLayoutFrame plf) {
            super(ge, plf);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            MapFrame.this.onScreen.requestFocusInWindow();
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getClickCount() == 2) {
                    new MapProperties((MapFrame)this.ge);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (MapFrame.this.parent.getSelectedComponent() != null) {
                        MapFrame.this.parent.getSelectedComponent().setSelected(false);
                    }
                    this.ge.setSelected(true);
                    MapFrame.this.parent.setSelectedComponent(this.ge);
                }
                MapFrame.this.repaint(false);
            }
        }

        public void initialize(PrintLayoutFrame plf, Point[] points) {
            this.setFrame(plf);
            this.setCorner(points);
        }
    }

    private class MapFrameOnScreen
    extends LayerViewPanel
    implements DragGestureListener,
    DragSourceListener,
    DragSourceMotionListener,
    LayerViewPanelListener {
        private boolean repaint;
        private boolean check;
        LayerViewPanel associatedLayerViewPanel;
        private Envelope envelopeScheduled;
        private Set renderingsScheduled;
        private GraphicElementTransferHandler graphicElementTransferHandler;
        private LayerViewPanel smallPanel;
        private Image img;
        Envelope lastEnvelopeScheduled;

        public MapFrameOnScreen() {
            this.repaint = true;
            this.renderingsScheduled = new HashSet();
            this.setBorder(MapFrame.this.defaultBorder);
        }

        public void setZoomScheduled(Envelope newEnvelope) {
            this.envelopeScheduled = newEnvelope;
        }

        public double getSmallPanelWidth() {
            if (this.getWidth() == 0) {
                return 0.0;
            }
            return (double)this.getWidth() / MapFrame.this.factorResolucion;
        }

        public double getSmallPanelHeight() {
            if (this.getHeight() == 0) {
                return 0.0;
            }
            return (double)this.getHeight() / MapFrame.this.factorResolucion;
        }

        @Override
        public double getScale() {
            return this.smallPanel.getScale();
        }

        public MapFrameOnScreen(LayerManager layerManager, LayerViewPanelContext context, LayerViewPanel viewPanel) {
            super(layerManager, context);
            this.repaint = true;
            this.renderingsScheduled = new HashSet();
            this.associatedLayerViewPanel = viewPanel;
            this.associatedLayerViewPanel.addRenderingListener(this);
            this.superRepaint();
            super.setBorder(MapFrame.this.defaultBorder);
            this.addMouseListener(MapFrame.this.listener);
            this.addMouseMotionListener(MapFrame.this.listener);
            this.addKeyListener(MapFrame.this.listener);
            this.setFocusable(true);
            this.check = true;
            this.initDragAndDrop();
            this.smallPanel = new LayerViewPanel(layerManager, context);
            this.smallPanel.setSize((int)this.getSmallPanelWidth(), (int)this.getSmallPanelHeight());
            MapFrame.this.listener.setCorner(MapFrame.this.getCornerPoint());
            this.smallPanel.addRenderingListener(new LayerViewPanelListener(){

                @Override
                public void selectionChanged() {
                }

                @Override
                public void cursorPositionChanged(String x, String y) {
                }

                @Override
                public void painted(Graphics graphics) {
                }

                @Override
                public void renderingFinished() {
                    MapFrame.this.getParent().repaint();
                }

                @Override
                public void renderingStarted() {
                }
            });
            this.smallPanel.setViewportInitialized(true);
        }

        @Override
        public void paintComponent(Graphics g) {
            try {
                if (MapFrame.this.resizing) {
                    g.setColor(Color.GRAY);
                    g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
                    return;
                }
                if (this.getSmallPanelHeight() <= 0.0 || this.getSmallPanelWidth() <= 0.0) {
                    return;
                }
                this.img = new BufferedImage((int)this.getSmallPanelWidth(), (int)this.getSmallPanelHeight(), 1);
                this.img.getGraphics().setColor(Color.WHITE);
                this.img.getGraphics().fillRect(0, 0, (int)this.getSmallPanelWidth(), (int)this.getSmallPanelHeight());
                if (this.smallPanel != null) {
                    this.smallPanel.getRenderingManager().copyTo((Graphics2D)this.img.getGraphics());
                    ((Graphics2D)g).drawImage(this.img, 0, 0, this.getWidth(), this.getHeight(), null);
                }
            }
            catch (Throwable t) {
                LOGGER.error((Object)"", t);
                this.context.handleThrowable(t);
            }
            if (MapFrame.this.isSelected()) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            }
        }

        @Override
        public void paint(Graphics g) {
            this.paintComponent(g);
        }

        @Override
        public void repaint() {
            if (MapFrame.this != null && MapFrame.this.getParent() != null) {
                MapFrame.this.getParent().repaint();
            }
        }

        @Override
        public void repaint(boolean repaint) {
            if (MapFrame.this != null && MapFrame.this.getParent() != null) {
                MapFrame.this.getParent().repaint();
            }
        }

        public void initialize(PrintLayoutFrame plf) {
            super.setBorder(MapFrame.this.defaultBorder);
            this.associatedLayerViewPanel = plf.getLayerViewPanel();
            this.associatedLayerViewPanel.addRenderingListener(this);
            this.addMouseListener(MapFrame.this.listener);
            this.addMouseMotionListener(MapFrame.this.listener);
            this.addKeyListener(MapFrame.this.listener);
            this.setFocusable(true);
            this.initDragAndDrop();
            this.smallPanel = new LayerViewPanel();
            this.smallPanel.setSize(MapFrame.this.getScreenWidth(), MapFrame.this.getScreenHeight());
            this.smallPanel.init(this.associatedLayerViewPanel.getLayerManager(), this.associatedLayerViewPanel.getContext());
            this.smallPanel.setSize((int)this.getSmallPanelWidth(), (int)this.getSmallPanelHeight());
            this.smallPanel.addRenderingListener(new LayerViewPanelListener(){

                @Override
                public void selectionChanged() {
                }

                @Override
                public void cursorPositionChanged(String x, String y) {
                }

                @Override
                public void painted(Graphics graphics) {
                }

                @Override
                public void renderingFinished() {
                    MapFrame.this.getParent().repaint();
                }

                @Override
                public void renderingStarted() {
                }
            });
            MapFrame.this.listener.setCorner(MapFrame.this.getCornerPoint());
            this.smallPanel.setViewportInitialized(true);
        }

        private void initDragAndDrop() {
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this, 2, this);
            dragSource.addDragSourceMotionListener(this);
        }

        @Override
        protected void this_componentResized(ComponentEvent e) {
            MapFrame.this.resize = true;
        }

        @Override
        protected void this_mouseReleased(MouseEvent e) {
            super.this_mouseReleased(e);
            Point transformedClickedPoint = this.transformPoint(e.getPoint());
            this.associatedLayerViewPanel.setLastClickedPoint(transformedClickedPoint);
            if (MapFrame.this.resize) {
                MapFrame.this.resize = false;
                if (!this.check) {
                    try {
                        Envelope envelope = MapFrame.this.parent.getTaskFrame().getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
                        this.getViewport().zoom(envelope, true);
                        MapFrame.this.forPrint.getViewport().zoom(envelope, false, false);
                    }
                    catch (NoninvertibleTransformException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    this.check = false;
                }
            }
        }

        private Point transformPoint(Point point) {
            Rectangle mapFrameBounds = this.getBounds();
            Rectangle layerViewPanelBounds = this.associatedLayerViewPanel.getBounds();
            double x = point.getX() * (double)layerViewPanelBounds.width / (double)mapFrameBounds.width;
            double y = point.getY() * (double)layerViewPanelBounds.height / (double)mapFrameBounds.height;
            Point newPoint = new Point((int)x, (int)y);
            return newPoint;
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
            MapFrame.this.onScreen.paint(image.getGraphics());
            this.graphicElementTransferHandler = new GraphicElementTransferHandler(image, MapFrame.this);
            dge.startDrag(Cursor.getDefaultCursor(), this.graphicElementTransferHandler, this);
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
            MapFrame.this.factorResolucion = Math.max((double)width / 1000.0, 1.0);
            super.setBounds(x, y, width, height);
            MapFrame.this.initCornerPoint();
            MapFrame.this.listener.setCorner(MapFrame.this.getCornerPoint());
            if (this.smallPanel != null && !MapFrame.this.resizing) {
                int w = (int)this.getSmallPanelWidth();
                int h = (int)this.getSmallPanelHeight();
                Dimension d = this.smallPanel.getSize();
                if (d.height != h || d.width != w) {
                    this.smallPanel.setSize(w, h);
                    try {
                        this.smallPanel.getViewport().zoom(this.associatedLayerViewPanel.getViewport().getEnvelopeInModelCoordinates());
                    }
                    catch (NoninvertibleTransformException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void setBounds(Rectangle rect) {
            MapFrame.this.factorResolucion = Math.max((double)rect.width / 1000.0, 1.0);
            super.setBounds(rect);
            MapFrame.this.initCornerPoint();
            MapFrame.this.listener.setCorner(MapFrame.this.getCornerPoint());
            if (this.smallPanel != null && !MapFrame.this.resizing) {
                int w = (int)this.getSmallPanelWidth();
                int h = (int)this.getSmallPanelHeight();
                Dimension d = this.smallPanel.getSize();
                if (d.height != h || d.width != w) {
                    this.smallPanel.setSize(w, h);
                    try {
                        this.smallPanel.getViewport().zoom(this.associatedLayerViewPanel.getViewport().getEnvelopeInModelCoordinates());
                    }
                    catch (NoninvertibleTransformException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void setRepaint(boolean repaint, boolean paint) {
            this.repaint = repaint;
            if (paint) {
                this.repaint();
            }
        }

        public boolean isRepaint() {
            return this.repaint;
        }

        @Override
        public void selectionChanged() {
        }

        @Override
        public void cursorPositionChanged(String x, String y) {
        }

        @Override
        public void painted(Graphics graphics) {
        }

        @Override
        public void renderingFinished() {
            if (MapFrame.this.parent == null) {
                return;
            }
            if (!MapFrame.this.parent.isUpdateGraphicElements()) {
                return;
            }
            if (this.envelopeScheduled != null) {
                if (!this.envelopeScheduled.equals((Object)this.lastEnvelopeScheduled)) {
                    try {
                        this.smallPanel.getViewport().zoom(this.envelopeScheduled, true);
                    }
                    catch (NoninvertibleTransformException e) {
                        e.printStackTrace();
                    }
                }
                this.lastEnvelopeScheduled = this.envelopeScheduled;
            } else if (this.renderingsScheduled.size() > 0) {
                Collection<Layerable> renderings = Collections.unmodifiableCollection(this.renderingsScheduled);
                for (Layerable layerable : renderings) {
                    this.smallPanel.getRenderingManager().render(layerable);
                }
            }
        }

        @Override
        public void dispose() {
            super.dispose();
            this.associatedLayerViewPanel.removeRenderingListener(this);
            this.associatedLayerViewPanel = null;
            this.smallPanel.dispose();
            this.smallPanel.removeAllListeners();
            this.smallPanel = null;
            this.removeMouseListener(MapFrame.this.listener);
            this.removeMouseMotionListener(MapFrame.this.listener);
            this.removeKeyListener(MapFrame.this.listener);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.removeDragSourceMotionListener(this);
            if (this.graphicElementTransferHandler != null) {
                this.graphicElementTransferHandler.dispose();
                this.graphicElementTransferHandler = null;
            }
        }

        @Override
        public void layerChanged(LayerEvent e) {
        }

        @Override
        public void renderingStarted() {
        }

        @Override
        public void fireRenderingFinished() {
            this.envelopeScheduled = null;
            this.renderingsScheduled.clear();
        }
    }
}

