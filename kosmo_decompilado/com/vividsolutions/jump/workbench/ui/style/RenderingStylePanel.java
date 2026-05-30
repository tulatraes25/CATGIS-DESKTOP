/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.io.ParseException
 *  com.vividsolutions.jts.io.WKTReader
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.style.BasicStylePanel;
import com.vividsolutions.jump.workbench.ui.style.StylePanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.core.renderer.RenderingHintsManager;
import org.saig.jump.lang.I18N;

public class RenderingStylePanel
extends BasicStylePanel
implements StylePanel {
    private Layer layer;
    private JPanel previewPanel = new JPanel(){
        private LayerViewPanel dummyLayerViewPanel;
        private Viewport viewport;
        private Feature feature;
        {
            this.setBackground(Color.white);
            this.setBorder(BorderFactory.createLoweredBevelBorder());
            this.setMaximumSize(new Dimension(200, 40));
            this.setMinimumSize(new Dimension(200, 40));
            this.setPreferredSize(new Dimension(200, 40));
            this.dummyLayerViewPanel = new LayerViewPanel(new LayerManager(), new LayerViewPanelContext(){

                @Override
                public void setStatusMessage(String message) {
                }

                @Override
                public void warnUser(String warning) {
                }

                @Override
                public void handleThrowable(Throwable t) {
                }
            });
            this.viewport = new Viewport(this.dummyLayerViewPanel){
                private AffineTransform transform;
                {
                    this.transform = new AffineTransform();
                }

                @Override
                public Envelope getEnvelopeInModelCoordinates() {
                    return new Envelope(0.0, 200.0, 0.0, 40.0);
                }

                @Override
                public AffineTransform getModelToViewTransform() {
                    return this.transform;
                }

                @Override
                public Point2D toViewPoint(Coordinate modelCoordinate) {
                    return new Point2D.Double(modelCoordinate.x, modelCoordinate.y);
                }
            };
            this.feature = this.createFeature();
        }

        private void paint(Style style, Graphics2D g) {
            Stroke originalStroke = g.getStroke();
            try {
                try {
                    style.paint(this.feature, g, this.viewport);
                }
                catch (Exception exception) {
                    g.setStroke(originalStroke);
                }
            }
            finally {
                g.setStroke(originalStroke);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            ((Graphics2D)g).setRenderingHints(RenderingHintsManager.getRenderingHints());
            this.paint(RenderingStylePanel.this.getBasicStyle(), (Graphics2D)g);
        }

        private Feature createFeature() {
            try {
                return FeatureUtil.toFeature(new WKTReader().read("POLYGON ((-200 80, 100 20, 400 -40, 400 80, -200 80))"), new FeatureSchema(){
                    private static final long serialVersionUID = -8627306219650589202L;
                    {
                        this.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
                    }
                });
            }
            catch (ParseException e) {
                Assert.shouldNeverReachHere();
                return null;
            }
        }
    };

    public RenderingStylePanel() {
    }

    public RenderingStylePanel(Blackboard blackboard, Layer layer) {
        super(blackboard, 21);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(new Integer(5), new JLabel("5"));
        labelTable.put(new Integer(10), new JLabel("10"));
        labelTable.put(new Integer(15), new JLabel("15"));
        labelTable.put(new Integer(20), new JLabel("20"));
        boolean isLine = layer.getUltimateFeatureCollectionWrapper().getFeatureSchema().getGeometryType() == 3 || layer.getUltimateFeatureCollectionWrapper().getFeatureSchema().getGeometryType() == 2;
        boolean isPoint = layer.getGeometryType() == 1 || layer.getGeometryType() == 8;
        this.setBasicStyle(layer.getBasicStyle(), isLine, layer.isRaster(), isPoint);
        try {
            this.jbInit();
            this.updateControls();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.setLayer(layer);
    }

    @Override
    public void updateControls() {
        super.updateControls();
        if (this.previewPanel == null) {
            return;
        }
        this.previewPanel.repaint();
    }

    @Override
    public String getTitle() {
        return I18N.getString("workbench.ui.style.RenderingStylePanel.rendering");
    }

    private void setLayer(Layer layer) {
        this.layer = layer;
        this.setSynchronizingLineColor(layer.isSynchronizingLineColor());
    }

    @Override
    void jbInit() throws Exception {
        if (this.previewPanel == null) {
            super.jbInit();
            return;
        }
        this.centerPanel.add((Component)new JLabel(I18N.getString("workbench.ui.style.RenderingStylePanel.preview")), new GridBagConstraints(0, 40, 3, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 0, 2), 0, 0));
        this.centerPanel.add((Component)this.previewPanel, new GridBagConstraints(0, 45, 3, 1, 0.0, 0.0, 17, 0, new Insets(0, 10, 2, 2), 0, 0));
    }

    @Override
    public Style updateStyles() {
        this.layer.removeStyle(this.layer.getBasicStyle());
        BasicStyle bsStyle = this.getBasicStyle();
        this.layer.addStyle(this.getBasicStyle());
        this.layer.setSynchronizingLineColor(this.synchronizeCheckBox.isSelected());
        return bsStyle;
    }

    void showVerticesCheckBox_actionPerformed(ActionEvent e) {
        this.updateControls();
    }

    @Override
    public String validateInput() {
        return null;
    }
}

