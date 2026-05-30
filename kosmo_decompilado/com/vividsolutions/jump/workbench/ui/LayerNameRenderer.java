/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringEscapeUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerPanel;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.saig.core.model.sdi.wfs.WFSLayer;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;
import org.saig.jump.widgets.config.ConfigTooltipPanel;
import org.saig.jump.widgets.cts.EPSGSelectionDialog;

public class LayerNameRenderer
extends JPanel
implements ListCellRenderer,
TreeCellRenderer {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LayerNameRenderer.class);
    private static final Color UNSELECTED_EDITABLE_FONT_COLOR = Color.red;
    private static final Color SELECTED_EDITABLE_FONT_COLOR = Color.yellow;
    public static final String USE_CLOCK_ANIMATION_KEY = String.valueOf(LayerNameRenderer.class.getName()) + " - USE CLOCK ANIMATION";
    private static final String LAYER_NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.LayerNameRenderer.name");
    private static final String LAYER_DATASOURCE = I18N.getString("com.vividsolutions.jump.workbench.ui.LayerNameRenderer.datasource");
    private static final String LAYER_PROJECTION = I18N.getString("com.vividsolutions.jump.workbench.ui.LayerNameRenderer.projection");
    private static final String LAYER_SIZE = I18N.getString("com.vividsolutions.jump.workbench.ui.LayerNameRenderer.number-of-elements");
    private static final String ERROR_MESSAGE = "Error";
    protected JCheckBox checkBox = new JCheckBox();
    GridBagLayout gridBagLayout = new GridBagLayout();
    protected JLabel label = new JLabel();
    private boolean indicatingEditability = false;
    private boolean indicatingProgress = false;
    private static int progressIconSize = 13;
    private Icon[] progressIcons = new Icon[]{GUIUtil.resize(IconLoader.icon("ClockN.gif"), progressIconSize), GUIUtil.resize(IconLoader.icon("ClockNE.gif"), progressIconSize), GUIUtil.resize(IconLoader.icon("ClockE.gif"), progressIconSize), GUIUtil.resize(IconLoader.icon("ClockSE.gif"), progressIconSize), GUIUtil.resize(IconLoader.icon("ClockS.gif"), progressIconSize), GUIUtil.resize(IconLoader.icon("ClockSW.gif"), progressIconSize), GUIUtil.resize(IconLoader.icon("ClockW.gif"), progressIconSize), GUIUtil.resize(IconLoader.icon("ClockNW.gif"), progressIconSize)};
    private Icon clearProgressIcon = GUIUtil.resize(IconLoader.icon("Clear.gif"), progressIconSize);
    private String PROGRESS_ICON_KEY = "PROGRESS_ICON";
    private DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();
    private RenderingManager renderingManager;
    private JLabel progressIconLabel = new JLabel();
    private Font font = new JLabel().getFont();
    private Font editableFont = this.font.deriveFont(1);
    private JLabel iconLabel = new JLabel(MapLayerPanel.ICON);
    private static final Icon TEXT_BALLON_ICON = GUIUtil.resize(IconLoader.icon("bocata.png"), progressIconSize);
    private static final Icon ARCIMS_ICON = GUIUtil.resize(IconLoader.icon("esrilogo.png"), progressIconSize);
    private static final Icon WFS_ICON = GUIUtil.resize(IconLoader.icon("wfs.png"), progressIconSize);
    private static final Icon WFS_TRANSACTIONAL_ICON = GUIUtil.resize(IconLoader.icon("wfs_transactional.png"), progressIconSize);

    public LayerNameRenderer() {
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setIndicatingEditability(boolean indicatingEditability) {
        this.indicatingEditability = indicatingEditability;
    }

    public void setIndicatingProgress(boolean indicatingProgress, RenderingManager renderingManager) {
        this.indicatingProgress = indicatingProgress;
        this.renderingManager = renderingManager;
    }

    public JLabel getLabel() {
        return this.label;
    }

    public JLabel getIconLabel() {
        return this.iconLabel;
    }

    public Rectangle getCheckBoxBounds() {
        int i = this.gridBagLayout.getConstraints((Component)this.checkBox).gridx;
        int x = 0;
        int j = 0;
        while (j < i) {
            x += this.getColumnWidth(j);
            ++j;
        }
        return new Rectangle(x, 0, this.getColumnWidth(i), this.getRowHeight());
    }

    protected int getColumnWidth(int i) {
        this.validateTree();
        return this.gridBagLayout.getLayoutDimensions()[0][i];
    }

    protected int getRowHeight() {
        this.validateTree();
        return this.gridBagLayout.getLayoutDimensions()[1][0];
    }

    public void setCheckBoxVisible(boolean checkBoxVisible) {
        this.checkBox.setVisible(checkBoxVisible);
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        this.validate();
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return this.defaultListCellRenderer.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
        }
        Layerable layerable = (Layerable)value;
        this.label.setText(layerable.getTitle(LocaleManager.getActiveLocale()));
        this.setToolTipText(this.generateToolTipText(layerable));
        this.checkBox.setSelected(layerable.isVisible());
        this.checkBox.setVisible(true);
        this.label.setEnabled(true);
        if (layerable.isEnabled()) {
            if (isSelected) {
                this.label.setForeground(list.getSelectionForeground());
                this.label.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
                this.setBackground(list.getSelectionBackground());
            } else {
                this.label.setForeground(list.getForeground());
                this.label.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
                this.setBackground(list.getBackground());
            }
            if (this.indicatingEditability) {
                if (layerable instanceof Layer && ((Layer)layerable).isEditable()) {
                    this.label.setFont(this.editableFont);
                    this.label.setForeground(isSelected ? SELECTED_EDITABLE_FONT_COLOR : UNSELECTED_EDITABLE_FONT_COLOR);
                } else {
                    this.label.setFont(this.font);
                }
            }
            if (layerable instanceof WMSLayer) {
                WMSLayer wmsLayer = (WMSLayer)layerable;
                if (wmsLayer.isLastImageWrong()) {
                    this.iconLabel.setIcon(MapLayerPanel.WRONG_ICON);
                } else {
                    this.iconLabel.setIcon(MapLayerPanel.ICON);
                }
                this.iconLabel.setVisible(true);
            } else if (layerable instanceof TextBalloonLayer) {
                this.iconLabel.setIcon(TEXT_BALLON_ICON);
                this.iconLabel.setVisible(true);
            } else if (layerable instanceof WFSLayer) {
                if (((WFSLayer)layerable).isTransactional()) {
                    this.iconLabel.setIcon(WFS_TRANSACTIONAL_ICON);
                } else {
                    this.iconLabel.setIcon(WFS_ICON);
                }
                this.iconLabel.setVisible(true);
            } else {
                this.iconLabel.setVisible(false);
            }
            if (layerable instanceof WMSLayer && this.indicatingProgress && this.renderingManager.getRenderer(layerable) != null && this.renderingManager.getRenderer(layerable).isRendering()) {
                layerable.getBlackboard().put(this.PROGRESS_ICON_KEY, layerable.getBlackboard().get(this.PROGRESS_ICON_KEY, 0) + 1);
                if (layerable.getBlackboard().getInt(this.PROGRESS_ICON_KEY) > this.progressIcons.length - 1) {
                    layerable.getBlackboard().put(this.PROGRESS_ICON_KEY, 0);
                }
                this.progressIconLabel.setIcon(this.progressIcons[layerable.getBlackboard().getInt(this.PROGRESS_ICON_KEY)]);
            } else {
                this.progressIconLabel.setIcon(this.clearProgressIcon);
            }
        } else {
            if (layerable instanceof WMSLayer) {
                WMSLayer wmsLayer = (WMSLayer)layerable;
                if (wmsLayer.isLastImageWrong()) {
                    this.iconLabel.setIcon(MapLayerPanel.WRONG_ICON);
                } else {
                    this.iconLabel.setIcon(MapLayerPanel.ICON);
                }
                this.iconLabel.setVisible(true);
            } else if (layerable instanceof TextBalloonLayer) {
                this.iconLabel.setIcon(TEXT_BALLON_ICON);
            } else {
                this.iconLabel.setVisible(false);
            }
            this.checkBox.setVisible(false);
            this.label.setEnabled(false);
        }
        return this;
    }

    private String generateToolTipText(Layerable layerable) {
        String tooltip = layerable.getName();
        boolean layerTooltipsOn = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigTooltipPanel.LAYER_TOOLTIPS_ON, true);
        if (layerTooltipsOn) {
            if (layerable instanceof Layer) {
                Layer layer = (Layer)layerable;
                String name = layer.getTitle();
                int size = -1;
                try {
                    size = layer.getUltimateFeatureCollectionWrapper().size();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                String type = LayerUtil.getLayerType(layer);
                String proyeccion = layer.getProjection() == null ? EPSGSelectionDialog.NO_SRS_DEFINED : GUITranslationsUtils.getCRSDescription(layer.getProjection());
                tooltip = "<HTML><b>" + LAYER_NAME + ": </b>" + name + "<br>" + "<b>" + LAYER_DATASOURCE + ": </b>" + type + "<br>" + "<b>" + LAYER_PROJECTION + ": </b>" + proyeccion + "<br>" + "<b>" + LAYER_SIZE + ": </b>" + size + "</HTML>";
            } else if (layerable instanceof WMSLayer) {
                WMSLayer layer = (WMSLayer)layerable;
                String name = layer.getTitle();
                String type = "WMS - v." + layer.getServiceVersion() + " - " + layer.getServerURL();
                String proyeccion = layer.getSrs() == null ? EPSGSelectionDialog.NO_SRS_DEFINED : GUITranslationsUtils.getName(layer.getSrs());
                tooltip = "<HTML><BODY>";
                tooltip = String.valueOf(tooltip) + "<DIV style=\"width: 500px; text-justification: justify;\">";
                tooltip = String.valueOf(tooltip) + "<b>" + LAYER_NAME + ": </b>" + name + "<br>" + "<b>" + LAYER_DATASOURCE + ": </b>" + type + "<br>" + "<b>" + LAYER_PROJECTION + ": </b> " + proyeccion + "<br>";
                if (layer.isLastImageWrong()) {
                    tooltip = String.valueOf(tooltip) + "<b>Error: </b> " + StringEscapeUtils.escapeHtml((String)layer.getLastExceptionMessage());
                }
                tooltip = String.valueOf(tooltip) + "</DIV></BODY></HTML>";
            }
        }
        return tooltip;
    }

    private JList list(JTree tree) {
        JList list = new JList();
        list.setForeground(tree.getForeground());
        list.setBackground(tree.getBackground());
        list.setSelectionForeground(UIManager.getColor("Tree.selectionForeground"));
        list.setSelectionBackground(UIManager.getColor("Tree.selectionBackground"));
        return list;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Layerable layerable = (Layerable)value;
        this.getListCellRendererComponent(this.list(tree), layerable, -1, selected, hasFocus);
        if (selected) {
            this.label.setForeground(UIManager.getColor("Tree.selectionForeground"));
            this.label.setBackground(UIManager.getColor("Tree.selectionBackground"));
            this.setForeground(UIManager.getColor("Tree.selectionForeground"));
            this.setBackground(UIManager.getColor("Tree.selectionBackground"));
        } else {
            this.label.setForeground(tree.getForeground());
            this.label.setBackground(tree.getBackground());
            this.setForeground(tree.getForeground());
            this.setBackground(tree.getBackground());
        }
        if (this.indicatingEditability && layerable instanceof Layer && ((Layer)layerable).isEditable()) {
            this.label.setForeground(selected ? SELECTED_EDITABLE_FONT_COLOR : UNSELECTED_EDITABLE_FONT_COLOR);
        }
        return this;
    }

    void jbInit() throws Exception {
        this.checkBox.setVisible(false);
        this.setLayout(this.gridBagLayout);
        this.label.setOpaque(false);
        this.label.setText("Layer Name Goes Here");
        this.checkBox.setOpaque(false);
        this.add((Component)this.progressIconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 2), 0, 0));
        this.add((Component)this.iconLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 2), 0, 0));
        this.add((Component)this.checkBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 5), 0, 0));
        this.add((Component)this.label, new GridBagConstraints(4, 0, 1, 1, 1.0, 0.0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
    }
}

