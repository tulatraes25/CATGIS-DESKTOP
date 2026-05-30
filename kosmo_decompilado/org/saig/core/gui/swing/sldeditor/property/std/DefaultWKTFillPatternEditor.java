/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.MapUtils
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicFillPattern;
import com.vividsolutions.jump.workbench.ui.renderer.style.FillPatternFactory;
import com.vividsolutions.jump.workbench.ui.renderer.style.WKTFillPattern;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import org.apache.commons.collections.MapUtils;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.gui.swing.sldeditor.property.GraphicEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.ExternalGraphicImpl;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.WKTGraphic;

public class DefaultWKTFillPatternEditor
extends GraphicEditor {
    private static final long serialVersionUID = 1L;
    private Paint[] fillPatterns = new FillPatternFactory().createFillPatterns();
    private JComboBox fillPatternComboBox = new JComboBox(this.fillPatterns){
        private static final long serialVersionUID = 1L;
        {
            this.setMaximumRowCount(24);
            this.setEditable(false);
            this.setRenderer(new ListCellRenderer(){
                private Paint fillPattern;
                private JLabel label = new JLabel(" ");
                private JPanel panel = new JPanel(new BorderLayout()){
                    private static final long serialVersionUID = 1L;
                    {
                        label.setPreferredSize(new Dimension(150, (int)label.getPreferredSize().getHeight()));
                        this.add((Component)label, "Center");
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        ((Graphics2D)g).setPaint(fillPattern);
                        ((Graphics2D)g).fill(new Rectangle2D.Double(0.0, 0.0, this.getWidth(), this.getHeight()));
                    }
                };

                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    this.fillPattern = (Paint)value;
                    this.label.setForeground(UIManager.getColor(isSelected ? "ComboBox.selectionForeground" : "ComboBox.foreground"));
                    this.panel.setBackground(UIManager.getColor(isSelected ? "ComboBox.selectionBackground" : "ComboBox.background"));
                    return this.panel;
                }
            });
        }
    };

    public DefaultWKTFillPatternEditor(FeatureSchema featureType) {
        this(styleBuilder.createGraphic(), featureType);
    }

    public DefaultWKTFillPatternEditor(Graphic graphic, FeatureSchema featureType) {
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.fillPatternComboBox);
    }

    @Override
    public Graphic getGraphic() {
        WKTFillPattern pattern = (WKTFillPattern)this.fillPatternComboBox.getSelectedItem();
        HashMap<String, Object> mapa = pattern.getProperties().getProperties();
        Color color = (Color)mapa.get("COLOR");
        int widthLine = (Integer)mapa.get("LINE WIDTH");
        int extent = (Integer)mapa.get("EXTENT");
        String wktPattern = (String)mapa.get("PATTERN WKT");
        WKTGraphic wktGraphic = new WKTGraphic(widthLine, extent, wktPattern, color);
        ExternalGraphic externalGraphic = styleFactory.createExternalGraphic("", "");
        externalGraphic.setWKTGraphic(wktGraphic);
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("wktFill", wktPattern);
        properties.put("wktFillColor", color);
        externalGraphic.setCustomProperties(properties);
        LiteralExpression graphicOpacity = filterFactory.createLiteralExpression(1.0);
        LiteralExpression size = filterFactory.createLiteralExpression(wktGraphic.getImage().getWidth());
        LiteralExpression rotation = filterFactory.createLiteralExpression(0);
        return styleFactory.createGraphic(new ExternalGraphic[]{externalGraphic}, null, null, graphicOpacity, size, rotation);
    }

    @Override
    public void setGraphic(Graphic graphic) {
        ExternalGraphicImpl extGraphic = (ExternalGraphicImpl)graphic.getExternalGraphics()[0];
        if (MapUtils.isNotEmpty(extGraphic.getCustomProperties())) {
            String fillName = (String)extGraphic.getCustomProperties().get("wktFill");
            this.selectFillInCombobox(fillName);
        }
    }

    public void setColor(Color color) {
        this.updateFillPatternColors(color);
    }

    private void selectFillInCombobox(String fillName) {
        boolean found = false;
        int i = 0;
        while (i < this.fillPatternComboBox.getItemCount() && !found) {
            BasicFillPattern currentPattern;
            if (this.fillPatternComboBox.getItemAt(i) instanceof BasicFillPattern && fillName.equals((currentPattern = (BasicFillPattern)this.fillPatternComboBox.getItemAt(i)).toString())) {
                this.fillPatternComboBox.setSelectedItem(currentPattern);
                found = true;
            }
            ++i;
        }
    }

    private void updateFillPatternColors(Color color) {
        int i = 0;
        while (i < this.fillPatternComboBox.getItemCount()) {
            if (this.fillPatternComboBox.getItemAt(i) instanceof BasicFillPattern) {
                ((BasicFillPattern)this.fillPatternComboBox.getItemAt(i)).setColor(color);
            }
            ++i;
        }
        this.fillPatternComboBox.repaint();
    }

    @Override
    public void setUnitsOfMeasurement(String unitsOfMeasurement) {
    }

    @Override
    public String getUnitsOfMeasurement() {
        return null;
    }
}

