/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.ColorChooserPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.SegmentSelectionRenderer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.config.ConfigPlugIn;
import org.saig.jump.tools.editing.SelectSegmentsTool;
import org.saig.jump.widgets.config.SelectVertexSymbolizer;
import org.saig.jump.widgets.util.NumberSpinner;

public class ConfigSegmentsSelectionPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    public static final double FILL_TRANSPARENCY_MIN_VALUE = 0.0;
    public static final double FILL_TRANSPARENCY_MAX_VALUE = 1.0;
    public static final double FILL_TRANSPARENCY_STEP_SIZE = 0.1;
    public static final String NAME = "Selecci\u00f3n de segmentos";
    private JPanel selectionStylePanel;
    protected JLabel lineLabel;
    protected ColorChooserPanel lineColorChooserPanel = new ColorChooserPanel();
    protected JCheckBox fillCheckbox;
    protected ColorChooserPanel fillColorChooserPanel = new ColorChooserPanel();
    protected NumberSpinner transparencySpinner;
    private Blackboard blackboard;
    private SelectVertexSymbolizer selectVertexSymbolizer;
    private Symbolizer pointSymbolizer = null;
    private JLabel setVertexSymbol;

    public ConfigSegmentsSelectionPanel(Blackboard b) {
        this.blackboard = b;
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getSelectionStylePanel());
        FormUtils.addFiller(this, 1, 0);
    }

    public JPanel getSelectionStylePanel() {
        if (this.selectionStylePanel == null) {
            this.selectionStylePanel = new JPanel(new GridBagLayout());
            this.selectionStylePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "segments-selection-style")));
            Dimension dimension = new Dimension(50, 30);
            this.lineLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.config.ConfigSelectionPanel.Line-color")) + ":");
            this.lineColorChooserPanel.setMinimumSize(dimension);
            this.lineColorChooserPanel.setPreferredSize(dimension);
            this.lineColorChooserPanel.setMaximumSize(dimension);
            this.lineColorChooserPanel.setAlpha(255);
            this.fillCheckbox = new JCheckBox(String.valueOf(I18N.getString("org.saig.jump.widgets.config.ConfigSelectionPanel.Fill-color")) + ":");
            this.fillCheckbox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfigSegmentsSelectionPanel.this.fillColorChooserPanel.setEnabled(ConfigSegmentsSelectionPanel.this.fillCheckbox.isSelected());
                }
            });
            this.fillColorChooserPanel.setMinimumSize(dimension);
            this.fillColorChooserPanel.setPreferredSize(dimension);
            this.fillColorChooserPanel.setMaximumSize(dimension);
            this.fillColorChooserPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                }
            });
            this.transparencySpinner = new NumberSpinner(1.0, 0.0, 1.0, 0.1);
            this.transparencySpinner.addChangeListener(new ChangeListener(){

                @Override
                public void stateChanged(ChangeEvent ce) {
                    int alphaValue = (int)(ConfigSegmentsSelectionPanel.this.transparencySpinner.getDoubleValue() * 255.0);
                    ConfigSegmentsSelectionPanel.this.fillColorChooserPanel.setAlpha(alphaValue);
                    Color fillColor = ConfigSegmentsSelectionPanel.this.fillColorChooserPanel.getColor();
                }
            });
            JLabel vertexStyleLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.config.ConfigSelectionPanel.vertex-style")) + ":");
            this.setVertexSymbol = new JLabel();
            JPanel vertexStylePanel = new JPanel(new FlowLayout());
            JButton pointSymbolSelection = new JButton(I18N.getString("org.saig.jump.widgets.config.ConfigSelectionPanel.select-style"));
            this.selectVertexSymbolizer = new SelectVertexSymbolizer(ConfigPlugIn.getDialog(), true);
            pointSymbolSelection.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    GUIUtil.centreOnWindow(ConfigSegmentsSelectionPanel.this.selectVertexSymbolizer);
                    ConfigSegmentsSelectionPanel.this.selectVertexSymbolizer.setVisible(true);
                    if (ConfigSegmentsSelectionPanel.this.selectVertexSymbolizer.wasOkPressed()) {
                        ConfigSegmentsSelectionPanel.this.pointSymbolizer = ConfigSegmentsSelectionPanel.this.selectVertexSymbolizer.getSymbolizer();
                        if (ConfigSegmentsSelectionPanel.this.pointSymbolizer != null) {
                            ConfigSegmentsSelectionPanel.this.setVertexSymbol.setIcon(LegendIconMaker.makeLegendIcon(50, 50, new Color(0, 0, 0, 0), new Symbolizer[]{ConfigSegmentsSelectionPanel.this.pointSymbolizer}, null, false));
                        } else {
                            ConfigSegmentsSelectionPanel.this.setVertexSymbol.setIcon(null);
                        }
                    }
                }
            });
            vertexStylePanel.add(this.setVertexSymbol);
            vertexStylePanel.add(pointSymbolSelection);
            FormUtils.addRowInGBL((JComponent)this.selectionStylePanel, 0, 0, (JComponent)this.lineLabel, false, false);
            FormUtils.addRowInGBL((JComponent)this.selectionStylePanel, 0, 1, (JComponent)this.lineColorChooserPanel, false, false);
            FormUtils.addFiller(this.selectionStylePanel, 0, 2);
            FormUtils.addRowInGBL((JComponent)this.selectionStylePanel, 1, 0, (JComponent)this.fillCheckbox, false, false);
            FormUtils.addRowInGBL((JComponent)this.selectionStylePanel, 1, 1, (JComponent)this.fillColorChooserPanel, false, false);
            FormUtils.addFiller(this.selectionStylePanel, 1, 2);
            FormUtils.addRowInGBL((JComponent)this.selectionStylePanel, 2, 0, new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.config.ConfigSelectionPanel.Fill-transparency")) + ":"), (JComponent)this.transparencySpinner);
            FormUtils.addRowInGBL((JComponent)this.selectionStylePanel, 3, 0, vertexStyleLabel, (JComponent)vertexStylePanel);
        }
        return this.selectionStylePanel;
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(SegmentSelectionRenderer.SELECTION_LINE_COLOR, this.lineColorChooserPanel.getColor());
        if (this.fillCheckbox.isSelected()) {
            PersistentBlackboardPlugIn.get(this.blackboard).put(SegmentSelectionRenderer.SELECTION_FILL_COLOR, this.fillColorChooserPanel.getColor());
        } else {
            PersistentBlackboardPlugIn.get(this.blackboard).put(SegmentSelectionRenderer.SELECTION_FILL_COLOR, null);
        }
        PersistentBlackboardPlugIn.get(this.blackboard).put(SegmentSelectionRenderer.SELECTION_FILL_TRANSPARENCY, this.transparencySpinner.getValue());
        PersistentBlackboardPlugIn.get(this.blackboard).put(SegmentSelectionRenderer.SELECTION_VERTEX_DEFAULT_SYMBOL, this.pointSymbolizer);
        LayerViewPanel panel = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel();
        if (panel != null && panel.getRenderingManager() != null) {
            panel.getRenderingManager().rebootSegmentsSelectionRenderer();
            panel.getRenderingManager().render("SELECTED_SEGMENTS");
        }
    }

    @Override
    public void init() {
        Color lineColor = (Color)PersistentBlackboardPlugIn.get(this.blackboard).get(SegmentSelectionRenderer.SELECTION_LINE_COLOR, SegmentSelectionRenderer.SELECTION_LINE_DEFAULT_COLOR);
        this.lineColorChooserPanel.setColor(lineColor);
        Color fillColor = (Color)PersistentBlackboardPlugIn.get(this.blackboard).get(SegmentSelectionRenderer.SELECTION_FILL_COLOR, SegmentSelectionRenderer.SELECTION_FILL_DEFAULT_COLOR);
        this.transparencySpinner.setValue(new Double(PersistentBlackboardPlugIn.get(this.blackboard).get(SegmentSelectionRenderer.SELECTION_FILL_TRANSPARENCY, 1.0)));
        this.fillCheckbox.setSelected(fillColor != null);
        this.fillColorChooserPanel.setEnabled(this.fillCheckbox.isSelected());
        if (fillColor != null) {
            this.fillColorChooserPanel.setColor(fillColor);
        } else {
            this.fillColorChooserPanel.setColor(ColorChooserPanel.DISABLED_COLOR);
        }
        this.fillColorChooserPanel.setAlpha((int)(this.transparencySpinner.getDoubleValue() * 255.0));
        this.pointSymbolizer = (Symbolizer)PersistentBlackboardPlugIn.get(this.blackboard).get(SegmentSelectionRenderer.SELECTION_VERTEX_DEFAULT_SYMBOL, SLDEditor.styleBuilder.createPointSymbolizer());
        if (this.pointSymbolizer != null) {
            this.setVertexSymbol.setIcon(LegendIconMaker.makeLegendIcon(50, 50, new Color(0, 0, 0, 0), new Symbolizer[]{this.pointSymbolizer}, null, false));
        }
        this.selectVertexSymbolizer.setSymbolizer(this.pointSymbolizer);
    }

    @Override
    public Icon getIcon() {
        return GUIUtil.toSmallIcon(SelectSegmentsTool.ICON);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

