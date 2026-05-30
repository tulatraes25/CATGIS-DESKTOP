/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.wms;

import com.vividsolutions.jump.workbench.WorkbenchException;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerPanel;
import com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel;
import com.vividsolutions.jump.workbench.ui.wizard.AbstractWizardPanel;
import com.vividsolutions.wms.WMService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Collection;
import java.util.Map;
import org.saig.jump.lang.I18N;

public class MapLayerWizardPanel
extends AbstractWizardPanel {
    private static final long serialVersionUID = 1L;
    public static final String LAYERS_KEY = "LAYERS";
    public static final String COMMON_SRS_LIST_KEY = "COMMON_SRS_LIST";
    public static final String INITIAL_LAYER_NAMES_KEY = "INITIAL_LAYER_NAMES";
    public static final String NO_COMMON_SRS_MESSAGE = I18N.getString("ui.plugin.wms.MapLayerWizardPanel.the-chosen-layers-do-not-have-a-common-epsg-coordinate-reference-system");
    private MapLayerPanel addRemovePanel = new MapLayerPanel();
    private BorderLayout borderLayout1 = new BorderLayout();

    public MapLayerWizardPanel() {
        try {
            this.jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getInstructions() {
        return I18N.getString("ui.plugin.wms.MapLayerWizardPanel.please-choose-the-wms-layers-that-should-appear-on-the-image");
    }

    @Override
    public void exitingToRight() throws WorkbenchException {
        this.dataMap.put(LAYERS_KEY, this.addRemovePanel.getChosenMapLayers());
        if (this.addRemovePanel.commonSRSList().isEmpty()) {
            throw new WorkbenchException(NO_COMMON_SRS_MESSAGE);
        }
        this.dataMap.put(COMMON_SRS_LIST_KEY, this.addRemovePanel.commonSRSList());
    }

    @Override
    public void enteredFromLeft(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
        this.addRemovePanel.init((WMService)dataMap.get("SERVICE"), (Collection)dataMap.get(INITIAL_LAYER_NAMES_KEY));
    }

    @Override
    public String getTitle() {
        return I18N.getString("ui.plugin.wms.MapLayerWizardPanel.choose-wms-layers");
    }

    @Override
    public String getID() {
        return this.getClass().getName();
    }

    @Override
    public boolean isInputValid() {
        return !this.addRemovePanel.getChosenMapLayers().isEmpty();
    }

    @Override
    public String getNextID() {
        return SRSWizardPanel.class.getName();
    }

    private void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.add((Component)this.addRemovePanel, "Center");
    }

    @Override
    public boolean isPanelOk() {
        return true;
    }

    @Override
    public void add(InputChangedListener listener) {
        this.addRemovePanel.add(listener);
    }

    @Override
    public void remove(InputChangedListener listener) {
        this.addRemovePanel.remove(listener);
    }
}

