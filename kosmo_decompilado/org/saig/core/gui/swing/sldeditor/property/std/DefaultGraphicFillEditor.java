/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.property.GraphicEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class DefaultGraphicFillEditor
extends GraphicEditor {
    private static final long serialVersionUID = 1L;
    private JButton btnGraphic;
    private JLabel exampleLabel;
    private Graphic graphic;
    private Dimension iconDimension;
    private FeatureSchema featureType;
    private boolean polygonSymbolizer = false;

    public DefaultGraphicFillEditor(Dimension d, boolean polygonSymbolizer, FeatureSchema featureType) {
        this(styleBuilder.createGraphic(), d, polygonSymbolizer, featureType);
    }

    public DefaultGraphicFillEditor(Graphic g, Dimension d, boolean polygonSymbolizer, FeatureSchema featureType) {
        this.featureType = featureType;
        this.polygonSymbolizer = polygonSymbolizer;
        this.setAlignmentX(0.0f);
        this.iconDimension = d;
        this.setPreferredSize(this.iconDimension);
        this.setMinimumSize(this.iconDimension);
        this.exampleLabel = new JLabel();
        this.exampleLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.btnGraphic = new JButton("...");
        this.btnGraphic.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicFillEditor.select-graphic"));
        this.btnGraphic.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent event) {
                GraphicDialog gd = new GraphicDialog(DefaultGraphicFillEditor.this, DefaultGraphicFillEditor.this.graphic);
                gd.setTitle(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicFillEditor.edit-graphic-fill"));
                gd.setVisible(true);
                if (gd.exitOk) {
                    DefaultGraphicFillEditor.this.setGraphic(gd.getGraphic());
                }
            }
        });
        this.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 17;
        this.add((Component)this.exampleLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 17;
        this.add((Component)this.btnGraphic, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.fill = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        this.add((Component)new JLabel(), gridBagConstraints);
        this.setGraphic(g);
    }

    @Override
    public void setGraphic(Graphic graphic) {
        this.graphic = graphic;
        Symbolizer symbolizer = null;
        if (this.polygonSymbolizer) {
            Fill fill = styleBuilder.createFill();
            fill.setGraphicFill(graphic);
            symbolizer = styleBuilder.createPolygonSymbolizer(styleBuilder.createStroke(), fill);
        } else {
            Stroke stroke = styleBuilder.createStroke();
            stroke.setGraphicStroke(graphic);
            symbolizer = styleBuilder.createLineSymbolizer(stroke);
        }
        Icon graphicIcon = LegendIconMaker.makeLegendIcon(this.iconDimension.width, this.iconDimension.width, Color.WHITE, new Symbolizer[]{symbolizer}, null, true);
        this.exampleLabel.setIcon(graphicIcon);
    }

    @Override
    public Graphic getGraphic() {
        return this.graphic;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.exampleLabel.setEnabled(enabled);
        this.btnGraphic.setEnabled(enabled);
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultGraphicFillEditor(new Dimension(50, 50), true, null));
    }

    @Override
    public void setUnitsOfMeasurement(String unitsOfMeasurement) {
    }

    @Override
    public String getUnitsOfMeasurement() {
        return null;
    }

    private class GraphicDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;
        private GraphicEditor graphicEditor;
        public boolean exitOk;

        public GraphicDialog(Component parent, Graphic g) {
            super(JOptionPane.getFrameForComponent(parent), true);
            this.exitOk = false;
            JPanel content = new JPanel();
            JPanel command = new JPanel();
            JButton okbtnGraphic = new JButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicFillEditor.ok"));
            JButton cancelbtnGraphic = new JButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicFillEditor.cancel"));
            command.setLayout(new FlowLayout(2, 3, 3));
            command.add(okbtnGraphic);
            command.add(cancelbtnGraphic);
            this.graphicEditor = propertyEditorFactory.createGraphicEditor(DefaultGraphicFillEditor.this.featureType);
            this.graphicEditor.setGraphic(g);
            content.setLayout(new BorderLayout());
            content.add(this.graphicEditor);
            content.add((Component)command, "South");
            okbtnGraphic.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent event) {
                    GraphicDialog.this.exitOk = true;
                    GraphicDialog.this.dispose();
                }
            });
            cancelbtnGraphic.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent event) {
                    GraphicDialog.this.exitOk = false;
                    GraphicDialog.this.dispose();
                }
            });
            this.setContentPane(content);
            this.pack();
            this.setLocationRelativeTo(parent);
        }

        public Graphic getGraphic() {
            return this.graphicEditor.getGraphic();
        }
    }
}

