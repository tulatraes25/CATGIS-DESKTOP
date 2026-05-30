/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.legend;

import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.dnd.DragAndDropLock;
import org.saig.jump.widgets.dnd.GhostGlassPane;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.PrintStatic;
import org.saig.jump.widgets.print.dnd.GraphicElementTransferHandler;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.GraphicElementsListener;
import org.saig.jump.widgets.print.elements.legend.LegendProperties;
import org.saig.jump.widgets.print.elements.legend.MapLegend;
import org.saig.jump.widgets.print.elements.legend.PrintFont;
import org.saig.jump.widgets.print.images.PrintIconLoader;

public class LegendFrame
implements GraphicElements,
LayerListener {
    private PrintLayoutFrame parent;
    private String name = "";
    public Point topLeftCorner = new Point();
    public Point topRightCorner = new Point();
    public Point bottomLeftCorner = new Point();
    public Point bottomRightCorner = new Point();
    private boolean select = false;
    private Color borderColor = Color.GRAY;
    private int borderThickness = 1;
    private Border border = BorderFactory.createLineBorder(this.borderColor, this.borderThickness);
    private PrintFont labelMapLegendFont = new PrintFont();
    private LegendFrameOnScreen onScreen;
    private LegendFrameForPrint forPrint;
    private LegendFrameListener listener;
    private List layers;
    private HashMap visibleLegends = new HashMap();

    public LegendFrame(PrintLayoutFrame plf) {
        this.parent = plf;
        this.layers = plf.getTaskFrame().getLayerManager().getVisibleLayerablesForLegend();
        this.buildVisibleLayers();
        this.listener = new LegendFrameListener(this, this.parent);
        this.onScreen = new LegendFrameOnScreen();
        this.forPrint = new LegendFrameForPrint();
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setName(this.parent.createName(this.getClass()));
    }

    public LegendFrame() {
        this.listener = new LegendFrameListener(this, this.parent);
        this.onScreen = new LegendFrameOnScreen();
        this.forPrint = new LegendFrameForPrint();
        this.buildVisibleLayers();
    }

    public void buildVisibleLayers() {
        if (this.layers != null) {
            int i = 0;
            while (i < this.layers.size()) {
                if (this.layers.get(i) instanceof Layer) {
                    Layer layer = (Layer)this.layers.get(i);
                    if (layer.isEnabled()) {
                        Rule[] rules = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules();
                        int j = 0;
                        while (j < rules.length) {
                            Symbolizer[] symbols = rules[j].getSymbolizers();
                            boolean onlyText = true;
                            int k = 0;
                            while (k < symbols.length) {
                                if (!(symbols[k] instanceof TextSymbolizer)) {
                                    onlyText = false;
                                }
                                ++k;
                            }
                            if (!onlyText) {
                                this.visibleLegends.put(layer.getName(), new Boolean(layer.isVisible()));
                            }
                            ++j;
                        }
                    }
                } else if (this.layers.get(i) instanceof WMSLayer) {
                    WMSLayer wmsLayer = (WMSLayer)this.layers.get(i);
                    this.visibleLegends.put(wmsLayer.getName(), new Boolean(wmsLayer.isVisible()));
                }
                ++i;
            }
        }
    }

    public HashMap getVisibleLegends() {
        return this.visibleLegends;
    }

    public void setVisibleLegends(HashMap visibleLegends) {
        this.visibleLegends = visibleLegends;
    }

    @Override
    public JComponent getGraphicElementsOnScreen() {
        return this.onScreen;
    }

    @Override
    public JComponent getGraphicElementsForPrint() {
        return this.forPrint;
    }

    public void print(Graphics g) {
        this.forPrint.print(g);
    }

    public void print(Graphics g, int x, int y) {
        this.forPrint.print(g, x, y);
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
    public void refreshForPrintBounds() {
        this.forPrint.fixerDimensions();
        this.initCornerPoint();
        this.repaint();
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
                this.onScreen.setLabelMapLegendFont(heightOnScreen, newForPrintHeight);
                this.onScreen.setBounds((int)Math.round(oldForPrintBounds.getX() * (double)widthOnScreen / (double)oldForPrintWidth), (int)Math.round(oldForPrintBounds.getY() * (double)heightOnScreen / (double)oldForPrintHeight), (int)Math.round(oldForPrintBounds.getWidth() * (double)widthOnScreen / (double)oldForPrintWidth), (int)Math.round(oldForPrintBounds.getHeight() * (double)heightOnScreen / (double)oldForPrintHeight));
                this.onScreen.setLocation((int)this.onScreen.getBounds().getX(), (int)this.onScreen.getBounds().getY());
                break;
            }
            case 1: {
                this.onScreen.setLabelMapLegendFont();
                this.onScreen.setBounds((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY(), (int)this.forPrint.getBounds().getWidth(), (int)this.forPrint.getBounds().getHeight());
                this.onScreen.setLocation((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY());
            }
        }
        this.initCornerPoint();
        this.repaint();
    }

    @Override
    public void zoom(float f) {
        this.onScreen.setLabelMapLegendFont((int)(f * 100.0f), 100);
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
                this.onScreen.setLabelMapLegendFont(heightOnScreen, newForPrintHeight);
                this.onScreen.setBounds((int)Math.round(this.forPrint.getBounds().getX() * widthFactor), (int)Math.round(this.forPrint.getBounds().getY() * heightFactor), (int)Math.round(this.forPrint.getBounds().getWidth() * widthFactor), (int)Math.round(this.forPrint.getBounds().getHeight() * heightFactor));
                this.onScreen.setLocation((int)this.onScreen.getBounds().getX(), (int)this.onScreen.getBounds().getY());
                break;
            }
            case 1: {
                this.onScreen.setLabelMapLegendFont();
                this.onScreen.setBounds((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY(), (int)this.forPrint.getBounds().getWidth(), (int)this.forPrint.getBounds().getHeight());
                this.onScreen.setLocation((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY());
            }
        }
        this.initCornerPoint();
        this.onScreen.repaint();
    }

    @Override
    public boolean isSelected() {
        return this.select;
    }

    @Override
    public void setSelected(boolean select) {
        this.select = select;
        if (select) {
            int borderSize = Math.max(this.labelMapLegendFont.getBorderThickness(), 1);
            this.onScreen.setBorder(BorderFactory.createLineBorder(Color.RED, borderSize));
        } else {
            this.setBorder(this.labelMapLegendFont.getBorder());
        }
        this.repaint();
    }

    @Override
    public void repaint() {
        this.onScreen.repaintOnScreen();
        this.forPrint.repaintForPrint();
    }

    public PrintFont getLabelMapLegendFont() {
        return this.labelMapLegendFont;
    }

    public void setLabelMapLegendFont(PrintFont labelMapLegendFont) {
        this.labelMapLegendFont = labelMapLegendFont;
        this.border = labelMapLegendFont.getBorder();
        this.onScreen.setLabelMapLegendFont(this.onScreen.getHeight(), this.forPrint.getHeight());
        this.onScreen.setBorder(labelMapLegendFont.getBorder());
        this.onScreen.setBackground(labelMapLegendFont.getBackgroundColor());
        this.onScreen.setOpaque(labelMapLegendFont.isOpaque());
        this.forPrint.setLabelMapLegendFont();
        this.forPrint.setBorder(labelMapLegendFont.getBorder());
        this.forPrint.setBackground(labelMapLegendFont.getBackgroundColor());
        this.forPrint.setOpaque(labelMapLegendFont.isOpaque());
        this.repaint();
    }

    @Override
    public void featuresChanged(FeatureEvent e) {
    }

    @Override
    public void layerChanged(LayerEvent e) {
        LayerEventType type = e.getType();
        if (type.equals(LayerEventType.APPEARANCE_CHANGED) || type.equals(LayerEventType.ADDED) || type.equals(LayerEventType.REMOVED) || type.equals(LayerEventType.VISIBILITY_CHANGED) || type.equals(LayerEventType.METADATA_CHANGED) || type.equals(LayerEventType.COMMITED)) {
            this.updateLayers();
        }
    }

    public void updateLayers() {
        this.layers = this.parent.getTaskFrame().getLayerManager().getVisibleLayerablesForLegend();
        this.onScreen.updateLayers();
        this.forPrint.updateLayers();
        this.repaint();
    }

    @Override
    public void categoryChanged(CategoryEvent e) {
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
        this.layers = this.parent.getTaskFrame().getLayerManager().getVisibleLayerablesForLegend();
        this.parent.getTaskFrame().getLayerManager().addLayerListener(this);
        this.setBorder(this.labelMapLegendFont.getBorder());
        this.forPrint.setValeursSpec();
        this.repaint();
    }

    @Override
    public void initGraphicAttributes(PrintLayoutFrame plf) {
        this.layers = plf.getTaskFrame().getLayerManager().getVisibleLayerablesForLegend();
        this.onScreen.updateLayers();
        this.forPrint.updateLayers();
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

    public void setSelect(boolean sel) {
        this.select = sel;
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

    public Color getBorderColor() {
        return this.borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        this.border = BorderFactory.createLineBorder(borderColor, this.borderThickness);
        this.onScreen.setBorder(this.border);
        this.forPrint.setBorder(this.border);
        this.repaint();
    }

    public int getBorderThickness() {
        return this.borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
        this.border = BorderFactory.createLineBorder(this.borderColor, borderThickness);
        this.onScreen.setBorder(this.border);
        this.forPrint.setBorder(this.border);
        this.repaint();
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

    public Layer getLayer() {
        return null;
    }

    public List getLayers() {
        return this.layers;
    }

    @Override
    public Icon getIcon() {
        return PrintIconLoader.icon("addLegend.gif");
    }

    @Override
    public void dispose() {
        this.layers = null;
        if (this.parent != null && this.parent.getTaskFrame() != null && this.parent.getTaskFrame().getLayerManager() != null) {
            this.parent.getTaskFrame().getLayerManager().removeLayerListener(this);
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

    @Override
    public PrintLayoutFrame getParent() {
        return this.parent;
    }

    public String toString() {
        return I18N.getMessage("org.saig.jump.widgets.print.elements.legend.LegendFrame.legend-{0}", new Object[]{this.getName()});
    }

    @Override
    public void setResizing(boolean resizing) {
    }

    private class LegendFrameForPrint
    extends JPanel {
        public LegendFrameForPrint() {
            this.setOpaque(false);
            this.setLayout(new BoxLayout(this, 1));
            this.buildLayers();
            this.setLabelMapLegendFont(1, 1);
            this.setFocusable(true);
            this.repaint();
        }

        public void buildLayers() {
            this.setOpaque(false);
            if (LegendFrame.this.layers != null) {
                int i = 0;
                while (i < LegendFrame.this.layers.size()) {
                    WMSLayer wmsLayer;
                    Layerable layerable = (Layerable)LegendFrame.this.layers.get(i);
                    if (layerable instanceof Layer) {
                        Layer layer = (Layer)layerable;
                        if (layer.isVisible() && layer.isEnabled()) {
                            Rule[] rules = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules();
                            int j = 0;
                            while (j < rules.length) {
                                Boolean visible;
                                Symbolizer[] symbols = rules[j].getSymbolizers();
                                boolean onlyText = true;
                                int k = 0;
                                while (k < symbols.length) {
                                    if (!(symbols[k] instanceof TextSymbolizer)) {
                                        onlyText = false;
                                    }
                                    ++k;
                                }
                                if (!onlyText && ((visible = (Boolean)LegendFrame.this.visibleLegends.get(layer.getName())) == null || visible.booleanValue())) {
                                    MapLegend element = new MapLegend(layer, symbols, rules[j].getTitle());
                                    element.setPrintFont(LegendFrame.this.labelMapLegendFont);
                                    element.setMaximumSize(new Dimension(this.getWidth(), LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setMinimumSize(new Dimension(this.getWidth(), LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setPreferredSize(new Dimension(this.getWidth(), LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setAlignmentX(0.0f);
                                    this.add(element);
                                    this.add(Box.createRigidArea(new Dimension(0, 5)));
                                }
                                ++j;
                            }
                        }
                    } else if (layerable instanceof WMSLayer && (wmsLayer = (WMSLayer)layerable).isVisible()) {
                        MapLegend element = new MapLegend(wmsLayer, null, null);
                        Icon wmsIcon = element.getSymbol();
                        int height = LegendFrame.this.labelMapLegendFont.getFontSize();
                        if (wmsIcon != null) {
                            height = wmsIcon.getIconHeight();
                        }
                        element.setPrintFont(LegendFrame.this.labelMapLegendFont);
                        element.setMaximumSize(new Dimension(1000, height));
                        element.setMinimumSize(new Dimension(1000, height));
                        element.setPreferredSize(new Dimension(1000, height));
                        element.setAlignmentX(0.0f);
                        this.add(element);
                    }
                    ++i;
                }
            }
        }

        public void setLabelMapLegendFont(int facteur1, int facteur2) {
            this.setOpaque(LegendFrame.this.labelMapLegendFont.isOpaque());
            this.setBackground(LegendFrame.this.labelMapLegendFont.getBackgroundColor());
            int newMaxSize = (int)Math.ceil((double)(LegendFrame.this.labelMapLegendFont.getFontSize() * facteur1) * 1.5 / (double)facteur2);
            int i = 0;
            while (i < this.getComponents().length) {
                Component component = this.getComponent(i);
                if (component instanceof MapLegend) {
                    Icon legendIcon;
                    MapLegend mapLegend = (MapLegend)component;
                    int fontSize = 0;
                    if (facteur2 != 0) {
                        fontSize = LegendFrame.this.labelMapLegendFont.getFontSize() * facteur1 / facteur2;
                    }
                    mapLegend.setPrintFont(new PrintFont(new Font(LegendFrame.this.labelMapLegendFont.getFont().getName(), LegendFrame.this.labelMapLegendFont.getFont().getStyle(), fontSize), LegendFrame.this.labelMapLegendFont.getColor(), LegendFrame.this.labelMapLegendFont.getBackgroundColor(), LegendFrame.this.labelMapLegendFont.getBorder(), LegendFrame.this.labelMapLegendFont.isUnderline(), LegendFrame.this.labelMapLegendFont.isOpaque()));
                    if (mapLegend.isWMSLegend() && (legendIcon = mapLegend.getSymbol()) != null) {
                        int wmsMaxSize = legendIcon.getIconHeight();
                        mapLegend.setMaximumSize(new Dimension(1000, wmsMaxSize));
                        mapLegend.setMinimumSize(new Dimension(1000, wmsMaxSize));
                        mapLegend.setPreferredSize(new Dimension(1000, wmsMaxSize));
                    } else {
                        mapLegend.setMaximumSize(new Dimension(this.getWidth(), newMaxSize));
                        mapLegend.setMinimumSize(new Dimension(this.getWidth(), newMaxSize));
                        mapLegend.setPreferredSize(new Dimension(this.getWidth(), newMaxSize));
                    }
                }
                ++i;
            }
        }

        public void setLabelMapLegendFont() {
            this.setOpaque(LegendFrame.this.labelMapLegendFont.isOpaque());
            this.setBackground(LegendFrame.this.labelMapLegendFont.getBackgroundColor());
            int newMaxSize = (int)Math.ceil((double)LegendFrame.this.labelMapLegendFont.getFontSize() * 2.5);
            int i = 0;
            while (i < this.getComponents().length) {
                Component component = this.getComponent(i);
                if (component instanceof MapLegend) {
                    MapLegend mapLegend = (MapLegend)component;
                    mapLegend.setPrintFont(LegendFrame.this.labelMapLegendFont);
                }
                ++i;
            }
        }

        public void updateLayers() {
            this.removeAll();
            this.setOpaque(false);
            if (LegendFrame.this.layers != null) {
                int i = 0;
                while (i < LegendFrame.this.layers.size()) {
                    WMSLayer wmsLayer;
                    Layerable layerable = (Layerable)LegendFrame.this.layers.get(i);
                    if (layerable instanceof Layer) {
                        Layer layer = (Layer)layerable;
                        if (layer.isVisible()) {
                            Rule[] rules = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules();
                            int j = 0;
                            while (j < rules.length) {
                                Boolean visible;
                                Symbolizer[] symbols = rules[j].getSymbolizers();
                                boolean onlyText = true;
                                int k = 0;
                                while (k < symbols.length) {
                                    if (!(symbols[k] instanceof TextSymbolizer)) {
                                        onlyText = false;
                                    }
                                    ++k;
                                }
                                if (!onlyText && ((visible = (Boolean)LegendFrame.this.visibleLegends.get(layer.getName())) == null || visible.booleanValue())) {
                                    MapLegend element = new MapLegend(layer, symbols, rules[j].getTitle());
                                    element.setPrintFont(LegendFrame.this.labelMapLegendFont);
                                    element.setMaximumSize(new Dimension(this.getWidth(), LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setMinimumSize(new Dimension(this.getWidth(), LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setPreferredSize(new Dimension(this.getWidth(), LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setAlignmentX(0.0f);
                                    this.add(element);
                                    this.add(Box.createRigidArea(new Dimension(0, 5)));
                                }
                                ++j;
                            }
                        }
                    } else if (layerable instanceof WMSLayer && (wmsLayer = (WMSLayer)layerable).isVisible()) {
                        MapLegend element = new MapLegend(wmsLayer, null, null);
                        Icon wmsIcon = element.getSymbol();
                        int height = LegendFrame.this.labelMapLegendFont.getFontSize();
                        if (wmsIcon != null) {
                            height = wmsIcon.getIconHeight();
                        }
                        element.setPrintFont(LegendFrame.this.labelMapLegendFont);
                        element.setMaximumSize(new Dimension(1000, height));
                        element.setMinimumSize(new Dimension(1000, height));
                        element.setPreferredSize(new Dimension(1000, height));
                        element.setAlignmentX(0.0f);
                        this.add(element);
                    }
                    ++i;
                }
            }
            this.setLabelMapLegendFont(LegendFrame.this.onScreen.getHeight(), LegendFrame.this.forPrint.getHeight());
            this.repaint();
        }

        public void repaintOnScreen() {
            if (LegendFrame.this.onScreen.getHeight() > 0 && LegendFrame.this.forPrint.getHeight() > 0) {
                this.setLabelMapLegendFont(LegendFrame.this.onScreen.getHeight(), LegendFrame.this.forPrint.getHeight());
            }
            super.repaint();
        }

        public void fixerDimensions() {
            switch (LegendFrame.this.parent.getActiveZoom()) {
                case 0: 
                case 3: {
                    super.setBounds((int)Math.round(LegendFrame.this.onScreen.getBounds().getX() * LegendFrame.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(LegendFrame.this.onScreen.getBounds().getY() * LegendFrame.this.parent.getPrintScreenHeightAspectRatio()), (int)Math.round(LegendFrame.this.onScreen.getBounds().getWidth() * LegendFrame.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(LegendFrame.this.onScreen.getBounds().getHeight() * LegendFrame.this.parent.getPrintScreenHeightAspectRatio()));
                    break;
                }
                case 1: {
                    super.setBounds((int)LegendFrame.this.onScreen.getBounds().getX(), (int)LegendFrame.this.onScreen.getBounds().getY(), (int)LegendFrame.this.onScreen.getBounds().getWidth(), (int)LegendFrame.this.onScreen.getBounds().getHeight());
                }
            }
            super.setLocation((int)super.getBounds().getX(), (int)super.getBounds().getY());
            super.repaint();
        }

        private void resizeComponents() {
            int locationX = LegendFrame.this.labelMapLegendFont.getBorderThickness();
            int locationY = LegendFrame.this.labelMapLegendFont.getBorderThickness();
            int width = this.getWidth() - LegendFrame.this.labelMapLegendFont.getBorderThickness();
            Component[] components = this.getComponents();
            int i = 0;
            while (i < components.length) {
                Component comp = components[i];
                if (comp instanceof MapLegend) {
                    Icon legendIcon;
                    MapLegend mapLegend = (MapLegend)comp;
                    int height = (int)Math.ceil((double)LegendFrame.this.labelMapLegendFont.getFontSize() * 1.5);
                    if (mapLegend.isWMSLegend() && (legendIcon = mapLegend.getSymbol()) != null) {
                        height = legendIcon.getIconHeight();
                    }
                    comp.setBounds(locationX, locationY, width, height);
                } else {
                    comp.setBounds(locationX, locationY, width, 5);
                }
                locationY += comp.getHeight();
                ++i;
            }
        }

        void setValeursSpec() {
            this.removeAll();
            this.setOpaque(LegendFrame.this.labelMapLegendFont.isOpaque());
            this.setBackground(LegendFrame.this.labelMapLegendFont.getBackgroundColor());
            if (LegendFrame.this.layers != null) {
                int i = 0;
                while (i < LegendFrame.this.layers.size()) {
                    WMSLayer wmsLayer;
                    Layerable layerable = (Layerable)LegendFrame.this.layers.get(i);
                    if (layerable instanceof Layer) {
                        Layer layer = (Layer)layerable;
                        if (layer.isVisible()) {
                            Rule[] rules = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules();
                            int j = 0;
                            while (j < rules.length) {
                                Boolean visible = (Boolean)LegendFrame.this.visibleLegends.get(layer.getName());
                                if (visible == null || visible.booleanValue()) {
                                    MapLegend element = new MapLegend(layer, rules[j].getSymbolizers(), rules[j].getTitle());
                                    element.setMaximumSize(new Dimension(1000, LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setMinimumSize(new Dimension(1000, LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setPreferredSize(new Dimension(1000, LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setAlignmentX(0.0f);
                                    this.add(element);
                                    this.add(Box.createRigidArea(new Dimension(0, 5)));
                                }
                                ++j;
                            }
                        }
                    } else if (layerable instanceof WMSLayer && (wmsLayer = (WMSLayer)layerable).isVisible()) {
                        MapLegend element = new MapLegend(wmsLayer, null, null);
                        Icon wmsIcon = element.getSymbol();
                        int height = LegendFrame.this.labelMapLegendFont.getFontSize();
                        if (wmsIcon != null) {
                            height = wmsIcon.getIconHeight();
                        }
                        element.setPrintFont(LegendFrame.this.labelMapLegendFont);
                        element.setMaximumSize(new Dimension(1000, height));
                        element.setMinimumSize(new Dimension(1000, height));
                        element.setPreferredSize(new Dimension(1000, height));
                        element.setAlignmentX(0.0f);
                        this.add(element);
                    }
                    ++i;
                }
            }
            this.setLabelMapLegendFont();
            this.repaint();
        }

        public void repaintForPrint() {
            if (LegendFrame.this.onScreen.getHeight() > 0 && LegendFrame.this.forPrint.getHeight() > 0) {
                this.setLabelMapLegendFont();
            }
            super.repaint();
        }

        @Override
        public void print(Graphics graphics) {
            RepaintManager currentManager = RepaintManager.currentManager(this);
            boolean doubleBuffered = currentManager.isDoubleBufferingEnabled();
            currentManager.setDoubleBufferingEnabled(false);
            try {
                BufferedImage solucion = new BufferedImage(this.getWidth(), this.getHeight(), 2);
                Graphics2D newGraphics = (Graphics2D)solucion.getGraphics();
                newGraphics.setColor(new Color(0, 0, 0, 0));
                newGraphics.fillRect(0, 0, this.getWidth(), this.getHeight());
                Composite composite = ((Graphics2D)graphics).getComposite();
                ((Graphics2D)graphics).setComposite(AlphaComposite.getInstance(3, 1.0f));
                if (this.isOpaque() || this.getBorder() != null) {
                    super.print(newGraphics);
                }
                this.resizeComponents();
                Component[] components = this.getComponents();
                Component comp = components[0];
                int previousLocationY = comp.getY();
                newGraphics.translate(comp.getX(), comp.getY());
                if (comp instanceof MapLegend) {
                    MapLegend mapLegend = (MapLegend)comp;
                    mapLegend.print(newGraphics);
                }
                int i = 1;
                while (i < components.length) {
                    comp = components[i];
                    if (comp instanceof MapLegend) {
                        MapLegend mapLegend = (MapLegend)comp;
                        newGraphics.translate(0, mapLegend.getY() - previousLocationY);
                        mapLegend.print(newGraphics);
                        previousLocationY = mapLegend.getY();
                    }
                    ++i;
                }
                ((Graphics2D)graphics).drawImage(solucion, null, 0, 0);
                ((Graphics2D)graphics).setComposite(composite);
            }
            finally {
                currentManager.setDoubleBufferingEnabled(doubleBuffered);
            }
        }

        public void print(Graphics graphics, int offsetX, int offsetY) {
            RepaintManager currentManager = RepaintManager.currentManager(this);
            boolean enableDoubleBuffering = currentManager.isDoubleBufferingEnabled();
            try {
                currentManager.setDoubleBufferingEnabled(false);
                Composite composite = ((Graphics2D)graphics).getComposite();
                if (this.isOpaque()) {
                    ((Graphics2D)graphics).setPaint(LegendFrame.this.getLabelMapLegendFont().getBackgroundColor());
                    graphics.fillRect(offsetX, offsetY, LegendFrame.this.getGraphicElementsForPrint().getWidth(), LegendFrame.this.getGraphicElementsForPrint().getHeight());
                }
                if (this.getBorder() != null) {
                    ((Graphics2D)graphics).setStroke(new BasicStroke(LegendFrame.this.getLabelMapLegendFont().getBorderThickness()));
                    ((Graphics2D)graphics).setPaint(LegendFrame.this.getLabelMapLegendFont().getBorderColor());
                    graphics.drawRect(offsetX, offsetY, LegendFrame.this.getGraphicElementsForPrint().getWidth(), LegendFrame.this.getGraphicElementsForPrint().getHeight());
                }
                graphics.clipRect(offsetX + LegendFrame.this.getLabelMapLegendFont().getBorderThickness(), offsetY + LegendFrame.this.getLabelMapLegendFont().getBorderThickness(), LegendFrame.this.getGraphicElementsForPrint().getWidth() - 2 * LegendFrame.this.getLabelMapLegendFont().getBorderThickness(), LegendFrame.this.getGraphicElementsForPrint().getHeight() - 2 * LegendFrame.this.getLabelMapLegendFont().getBorderThickness());
                this.resizeComponents();
                Component[] components = this.getComponents();
                int i = 0;
                while (i < components.length) {
                    Component comp = components[i];
                    if (comp instanceof MapLegend) {
                        Icon icono;
                        MapLegend mapLegend = (MapLegend)comp;
                        if (!mapLegend.isWMSLegend()) {
                            icono = mapLegend.getIcon();
                            String text = mapLegend.getText();
                            Font font = mapLegend.getFont();
                            int x = mapLegend.getX() + offsetX + 1;
                            int y = mapLegend.getY() + offsetY;
                            int iconStartY = (int)Math.round((double)y + ((double)mapLegend.getHeight() - (double)icono.getIconHeight() / 2.0));
                            mapLegend.paintGoodIcon(graphics, x, iconStartY, icono.getIconHeight());
                            ((Graphics2D)graphics).setComposite(AlphaComposite.getInstance(3, 1.0f));
                            graphics.setFont(font);
                            ((Graphics2D)graphics).setPaint(LegendFrame.this.getLabelMapLegendFont().getColor());
                            graphics.drawString(text, x + icono.getIconWidth() + 8, iconStartY + icono.getIconHeight() - 1);
                        } else {
                            icono = mapLegend.getSymbol();
                            int x = mapLegend.getX() + offsetX + 1;
                            int y = mapLegend.getY() + offsetY + 3;
                            mapLegend.paintGoodIcon(graphics, x, y, icono.getIconHeight());
                        }
                    }
                    ++i;
                }
                ((Graphics2D)graphics).setComposite(composite);
            }
            finally {
                currentManager.setDoubleBufferingEnabled(enableDoubleBuffering);
            }
        }

        public void printLegend(Graphics graphics, int offsetX, int offsetY) {
            RepaintManager currentManager = RepaintManager.currentManager(this);
            currentManager.setDoubleBufferingEnabled(false);
            Composite composite = ((Graphics2D)graphics).getComposite();
            if (this.isOpaque()) {
                ((Graphics2D)graphics).setPaint(LegendFrame.this.getLabelMapLegendFont().getBackgroundColor());
                graphics.fillRect(offsetX, offsetY, LegendFrame.this.getGraphicElementsForPrint().getWidth(), LegendFrame.this.getGraphicElementsForPrint().getHeight());
            }
            if (this.getBorder() != null) {
                ((Graphics2D)graphics).setStroke(new BasicStroke(LegendFrame.this.getLabelMapLegendFont().getBorderThickness()));
                ((Graphics2D)graphics).setPaint(LegendFrame.this.getLabelMapLegendFont().getBorderColor());
                graphics.drawRect(offsetX, offsetY, LegendFrame.this.getGraphicElementsForPrint().getWidth(), LegendFrame.this.getGraphicElementsForPrint().getHeight());
            }
            graphics.clipRect(offsetX + LegendFrame.this.getLabelMapLegendFont().getBorderThickness(), offsetY + LegendFrame.this.getLabelMapLegendFont().getBorderThickness(), LegendFrame.this.getGraphicElementsForPrint().getWidth() - 2 * LegendFrame.this.getLabelMapLegendFont().getBorderThickness(), LegendFrame.this.getGraphicElementsForPrint().getHeight() - 2 * LegendFrame.this.getLabelMapLegendFont().getBorderThickness());
            this.resizeComponents();
            Component[] components = this.getComponents();
            int i = 0;
            while (i < components.length) {
                Component comp = components[i];
                if (comp instanceof MapLegend) {
                    MapLegend mapLegend = (MapLegend)comp;
                    Icon icono = mapLegend.getIcon();
                    String text = mapLegend.getText();
                    Font font = mapLegend.getFont();
                    int x = mapLegend.getX() + offsetX + 1;
                    int y = mapLegend.getY() + offsetY;
                    int iconStartY = (int)Math.round((double)y + ((double)mapLegend.getHeight() - (double)icono.getIconHeight() / 2.0));
                    mapLegend.paintGoodIcon(graphics, x, iconStartY, icono.getIconHeight());
                    ((Graphics2D)graphics).setComposite(AlphaComposite.getInstance(3, 1.0f));
                    graphics.setFont(font);
                    ((Graphics2D)graphics).setPaint(LegendFrame.this.getLabelMapLegendFont().getColor());
                    graphics.drawString(text, x + icono.getIconWidth() + 8, iconStartY + icono.getIconHeight() - 1);
                }
                ++i;
            }
            ((Graphics2D)graphics).setComposite(composite);
        }
    }

    private class LegendFrameListener
    extends GraphicElementsListener {
        public LegendFrameListener(GraphicElements ge, PrintLayoutFrame plf) {
            super(ge, plf);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            LegendFrame.this.onScreen.requestFocusInWindow();
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getClickCount() == 2) {
                    new LegendProperties((LegendFrame)this.ge);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (LegendFrame.this.parent.getSelectedComponent() != null) {
                        LegendFrame.this.parent.getSelectedComponent().setSelected(false);
                    }
                    this.ge.setSelected(true);
                    LegendFrame.this.parent.setSelectedComponent(this.ge);
                }
                LegendFrame.this.repaint();
            }
        }
    }

    private class LegendFrameOnScreen
    extends JPanel
    implements DragGestureListener,
    DragSourceListener,
    DragSourceMotionListener {
        public LegendFrameOnScreen() {
            this.setOpaque(false);
            this.setLayout(new BoxLayout(this, 1));
            this.buildLayers();
            this.setLabelMapLegendFont(1, 1);
            this.addMouseListener(LegendFrame.this.listener);
            this.addMouseMotionListener(LegendFrame.this.listener);
            this.addKeyListener(LegendFrame.this.listener);
            this.setFocusable(true);
            this.repaint();
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this, 2, this);
            dragSource.addDragSourceMotionListener(this);
        }

        public void buildLayers() {
            this.setOpaque(false);
            if (LegendFrame.this.layers != null) {
                int i = 0;
                while (i < LegendFrame.this.layers.size()) {
                    WMSLayer wmsLayer;
                    Layerable layerable = (Layerable)LegendFrame.this.layers.get(i);
                    if (layerable instanceof Layer) {
                        Layer layer = (Layer)layerable;
                        if (layer.isVisible() && layer.isEnabled()) {
                            Rule[] rules = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules();
                            int j = 0;
                            while (j < rules.length) {
                                Boolean visible;
                                Symbolizer[] symbols = rules[j].getSymbolizers();
                                boolean onlyText = true;
                                int k = 0;
                                while (k < symbols.length) {
                                    if (!(symbols[k] instanceof TextSymbolizer)) {
                                        onlyText = false;
                                    }
                                    ++k;
                                }
                                if (!onlyText && ((visible = (Boolean)LegendFrame.this.visibleLegends.get(layer.getName())) == null || visible.booleanValue())) {
                                    MapLegend element = new MapLegend(layer, symbols, rules[j].getTitle());
                                    element.setPrintFont(LegendFrame.this.labelMapLegendFont);
                                    element.setMaximumSize(new Dimension(1000, LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setMinimumSize(new Dimension(1000, LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setPreferredSize(new Dimension(1000, LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setAlignmentX(0.0f);
                                    this.add(element);
                                    this.add(Box.createRigidArea(new Dimension(0, 5)));
                                }
                                ++j;
                            }
                        }
                    } else if (layerable instanceof WMSLayer && (wmsLayer = (WMSLayer)layerable).isVisible()) {
                        MapLegend element = new MapLegend(wmsLayer, null, null);
                        Icon wmsIcon = element.getSymbol();
                        int height = LegendFrame.this.labelMapLegendFont.getFontSize();
                        if (wmsIcon != null) {
                            height = wmsIcon.getIconHeight();
                        }
                        element.setPrintFont(LegendFrame.this.labelMapLegendFont);
                        element.setMaximumSize(new Dimension(1000, height));
                        element.setMinimumSize(new Dimension(1000, height));
                        element.setPreferredSize(new Dimension(1000, height));
                        element.setAlignmentX(0.0f);
                        this.add(element);
                        this.add(Box.createRigidArea(new Dimension(0, 5)));
                    }
                    ++i;
                }
            }
        }

        public void dispose() {
            this.removeMouseListener(LegendFrame.this.listener);
            this.removeMouseMotionListener(LegendFrame.this.listener);
            this.removeKeyListener(LegendFrame.this.listener);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.removeDragSourceMotionListener(this);
        }

        public void setLabelMapLegendFont(int facteur1, int facteur2) {
            double f = LegendFrame.this.parent != null ? (double)LegendFrame.this.parent.getZoomValue() : 1.0;
            this.setOpaque(LegendFrame.this.labelMapLegendFont.isOpaque());
            this.setBackground(LegendFrame.this.labelMapLegendFont.getBackgroundColor());
            int newMaxSize = (int)Math.ceil((double)LegendFrame.this.labelMapLegendFont.getFontSize() * f * 1.5);
            int i = 0;
            while (i < this.getComponents().length) {
                Component component = this.getComponent(i);
                if (component instanceof MapLegend) {
                    Icon legendIcon;
                    MapLegend mapLegend = (MapLegend)component;
                    mapLegend.setPrintFont(new PrintFont(new Font(LegendFrame.this.labelMapLegendFont.getFont().getName(), LegendFrame.this.labelMapLegendFont.getFont().getStyle(), (int)((double)LegendFrame.this.labelMapLegendFont.getFontSize() * f)), LegendFrame.this.labelMapLegendFont.getColor(), LegendFrame.this.labelMapLegendFont.getBackgroundColor(), LegendFrame.this.labelMapLegendFont.getBorder(), LegendFrame.this.labelMapLegendFont.isUnderline(), LegendFrame.this.labelMapLegendFont.isOpaque()));
                    if (mapLegend.isWMSLegend() && (legendIcon = mapLegend.getSymbol()) != null) {
                        int wmsMaxSize = legendIcon.getIconHeight();
                        mapLegend.setMaximumSize(new Dimension(1000, wmsMaxSize));
                        mapLegend.setMinimumSize(new Dimension(1000, wmsMaxSize));
                        mapLegend.setPreferredSize(new Dimension(1000, wmsMaxSize));
                    } else {
                        mapLegend.setMaximumSize(new Dimension(1000, newMaxSize));
                        mapLegend.setMinimumSize(new Dimension(1000, newMaxSize));
                        mapLegend.setPreferredSize(new Dimension(1000, newMaxSize));
                    }
                }
                ++i;
            }
        }

        public void setLabelMapLegendFont() {
            this.setOpaque(LegendFrame.this.labelMapLegendFont.isOpaque());
            this.setBackground(LegendFrame.this.labelMapLegendFont.getBackgroundColor());
            int newMaxSize = (int)Math.ceil((double)LegendFrame.this.labelMapLegendFont.getFontSize() * 2.5);
            int i = 0;
            while (i < this.getComponents().length) {
                Component component = this.getComponent(i);
                if (component instanceof MapLegend) {
                    MapLegend mapLegend = (MapLegend)component;
                    mapLegend.setPrintFont(LegendFrame.this.labelMapLegendFont);
                }
                ++i;
            }
        }

        public void updateLayers() {
            this.removeAll();
            this.setOpaque(false);
            if (LegendFrame.this.layers != null) {
                int i = 0;
                while (i < LegendFrame.this.layers.size()) {
                    Boolean visible;
                    WMSLayer wmsLayer;
                    Layerable layerable = (Layerable)LegendFrame.this.layers.get(i);
                    if (layerable instanceof Layer) {
                        Layer layer = (Layer)layerable;
                        if (layer.isVisible()) {
                            Rule[] rules = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules();
                            int j = 0;
                            while (j < rules.length) {
                                Boolean visible2;
                                Symbolizer[] symbols = rules[j].getSymbolizers();
                                boolean onlyText = true;
                                int k = 0;
                                while (k < symbols.length) {
                                    if (!(symbols[k] instanceof TextSymbolizer)) {
                                        onlyText = false;
                                    }
                                    ++k;
                                }
                                if (!onlyText && ((visible2 = (Boolean)LegendFrame.this.visibleLegends.get(layer.getName())) == null || visible2.booleanValue())) {
                                    MapLegend element = new MapLegend(layer, symbols, rules[j].getTitle());
                                    element.setPrintFont(LegendFrame.this.labelMapLegendFont);
                                    element.setMaximumSize(new Dimension(1000, LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setMinimumSize(new Dimension(1000, LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setPreferredSize(new Dimension(1000, LegendFrame.this.labelMapLegendFont.getFontSize()));
                                    element.setAlignmentX(0.0f);
                                    this.add(element);
                                    this.add(Box.createRigidArea(new Dimension(0, 5)));
                                }
                                ++j;
                            }
                        }
                    } else if (layerable instanceof WMSLayer && (wmsLayer = (WMSLayer)layerable).isVisible() && (visible = (Boolean)LegendFrame.this.visibleLegends.get(wmsLayer.getName())) != null && visible.booleanValue()) {
                        MapLegend element = new MapLegend(wmsLayer, null, null);
                        Icon wmsIcon = element.getSymbol();
                        int height = LegendFrame.this.labelMapLegendFont.getFontSize();
                        if (wmsIcon != null) {
                            height = wmsIcon.getIconHeight();
                        }
                        element.setPrintFont(LegendFrame.this.labelMapLegendFont);
                        element.setMaximumSize(new Dimension(1000, height));
                        element.setMinimumSize(new Dimension(1000, height));
                        element.setPreferredSize(new Dimension(1000, height));
                        element.setAlignmentX(0.0f);
                        this.add(element);
                        this.add(Box.createRigidArea(new Dimension(0, 5)));
                    }
                    ++i;
                }
            }
            this.setLabelMapLegendFont(LegendFrame.this.onScreen.getHeight(), LegendFrame.this.forPrint.getHeight());
            this.repaint();
        }

        public void repaintOnScreen() {
            if (LegendFrame.this.onScreen.getHeight() > 0 && LegendFrame.this.forPrint.getHeight() > 0) {
                this.setLabelMapLegendFont(LegendFrame.this.onScreen.getHeight(), LegendFrame.this.forPrint.getHeight());
            }
            super.repaint();
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
            LegendFrame.this.onScreen.paint(graphics2);
            dge.startDrag(Cursor.getDefaultCursor(), new GraphicElementTransferHandler(image, LegendFrame.this), this);
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

