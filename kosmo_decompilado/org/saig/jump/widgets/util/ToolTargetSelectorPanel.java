/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.dao.datasource.memory.CollectionIterator;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class ToolTargetSelectorPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final int ALL_ITEMS = 1;
    public static final int SELECTED_ITEMS = 2;
    public static final int VIEW_ITEMS = 3;
    private static final String DEFAULT_TITLE = I18N.getString("org.saig.jump.widgets.util.ToolTargetSelectorPanel.target-selector");
    private JRadioButton allRadioButton;
    private JRadioButton selectionRadioButton;
    private JRadioButton viewRadioButton;
    private ButtonGroup sourceGroup;
    private int numSelecItems;
    private Layer layer;
    private final SelectionManager sm;
    private String borderTitle;

    public ToolTargetSelectorPanel(Layer layer, SelectionManager sm) {
        this(DEFAULT_TITLE, layer, sm);
    }

    public ToolTargetSelectorPanel(String borderTitle, Layer layer, SelectionManager sm) {
        super(new GridBagLayout());
        this.layer = layer;
        this.sm = sm;
        this.setBorderTitle(borderTitle);
        this.allRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.ToolTargetSelectorPanel.the-whole-layer"));
        this.allRadioButton.setSelected(true);
        this.selectionRadioButton = new JRadioButton();
        this.selectionRadioButton.setSelected(false);
        this.viewRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.ToolTargetSelectorPanel.all-elements-in-view"));
        this.viewRadioButton.setSelected(false);
        this.sourceGroup = new ButtonGroup();
        this.sourceGroup.add(this.allRadioButton);
        this.sourceGroup.add(this.selectionRadioButton);
        this.sourceGroup.add(this.viewRadioButton);
        FormUtils.addRowInGBL(this, 1, 0, this.allRadioButton);
        FormUtils.addRowInGBL(this, 2, 0, this.selectionRadioButton);
        FormUtils.addRowInGBL(this, 3, 0, this.viewRadioButton);
        this.refreshPanel();
    }

    public void refreshPanel() {
        this.numSelecItems = this.sm.getNumFeaturesWithSelectedItems(this.layer);
        this.allRadioButton.setEnabled(true);
        this.selectionRadioButton.setEnabled(true);
        this.viewRadioButton.setEnabled(true);
        if (this.numSelecItems == 0) {
            this.selectionRadioButton.setEnabled(false);
            if (this.selectionRadioButton.isSelected()) {
                this.allRadioButton.setSelected(true);
            }
        }
        this.selectionRadioButton.setText(I18N.getMessage("org.saig.jump.widgets.util.ToolTargetSelectorPanel.all-selected-elements-{0}", new Object[]{this.numSelecItems}));
    }

    public int getSelectedOption() {
        if (this.allRadioButton.isSelected()) {
            return 1;
        }
        if (this.selectionRadioButton.isSelected()) {
            return 2;
        }
        if (this.viewRadioButton.isSelected()) {
            return 3;
        }
        return 1;
    }

    public FeatureIterator getFeaturesToProcess(PlugInContext context) throws Exception {
        FeatureIterator iterator = null;
        int selectedOption = this.getSelectedOption();
        if (selectedOption == 1) {
            iterator = this.layer.getUltimateFeatureCollectionWrapper().iterator();
        } else if (selectedOption == 2) {
            iterator = new CollectionIterator(context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(this.layer));
        } else if (selectedOption == 3) {
            Envelope viewEnv = context.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
            iterator = this.layer.getUltimateFeatureCollectionWrapper().queryIterator(viewEnv);
        }
        return iterator;
    }

    public boolean hasFeaturesToProcess(PlugInContext context) throws Exception {
        boolean hasFeatures = false;
        int selectedOption = this.getSelectedOption();
        switch (selectedOption) {
            case 1: {
                hasFeatures = this.layer.getUltimateFeatureCollectionWrapper().size() > 0;
                break;
            }
            case 2: {
                hasFeatures = context.getLayerViewPanel().getSelectionManager().getFeatureSelection().hasFeaturesWithSelectedItems(this.layer);
                break;
            }
            case 3: {
                Envelope viewEnv = context.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
                hasFeatures = CollectionUtils.isNotEmpty(this.layer.getFeatureCollectionWrapper().query(viewEnv));
                break;
            }
        }
        return hasFeatures;
    }

    public String getBorderTitle() {
        return this.borderTitle;
    }

    public void setBorderTitle(String borderTitle) {
        this.borderTitle = borderTitle;
        this.setBorder(BorderFactory.createTitledBorder(this.borderTitle));
    }

    public Layer getLayer() {
        return this.layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
        this.refreshPanel();
    }
}

