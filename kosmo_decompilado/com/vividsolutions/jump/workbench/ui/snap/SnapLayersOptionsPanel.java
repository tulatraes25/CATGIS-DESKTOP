/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesTool;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel;
import com.vividsolutions.jump.workbench.ui.snap.VisiblePointsAndLinesCache;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.CheckBoxJListSelectionPanel;

public class SnapLayersOptionsPanel
extends OptionsPanel
implements ActionListener {
    private static final long serialVersionUID = 1L;
    public static final String SPECIFIC_LAYERS_TO_SNAP_KEY = String.valueOf(SnapLayersOptionsPanel.class.getName()) + " - " + "SPECIFIC LAYERS TO SNAP";
    public static final String SNAP_TO_SPECIFIC_LAYERS_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO SPECIFIC LAYERS";
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapLayersOptionsPanel.Snap-to-layers");
    private JPanel snapOptionsPanel;
    private JRadioButton allLayersRadioButton;
    private JRadioButton specificLayersRadioButton;
    private ButtonGroup radioButtonGroup;
    private CheckBoxJListSelectionPanel selectionPanel;

    public SnapLayersOptionsPanel() {
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        this.add((Component)this.getRadioButtonPanel(), "North");
        this.selectionPanel = new CheckBoxJListSelectionPanel(new ArrayList(), I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapLayersOptionsPanel.Available-layers"), new Dimension(300, 270), true, true);
        this.selectionPanel.setEnabled(false);
        this.add((Component)this.selectionPanel, "Center");
    }

    public JPanel getRadioButtonPanel() {
        if (this.snapOptionsPanel == null) {
            this.snapOptionsPanel = new JPanel(new GridBagLayout());
            this.allLayersRadioButton = new JRadioButton(I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapLayersOptionsPanel.Snap-to-all-the-visible-layers"));
            this.allLayersRadioButton.setSelected(true);
            this.allLayersRadioButton.addActionListener(this);
            this.specificLayersRadioButton = new JRadioButton(I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapLayersOptionsPanel.Guide-to-specific-layers"));
            this.specificLayersRadioButton.addActionListener(this);
            this.radioButtonGroup = new ButtonGroup();
            this.radioButtonGroup.add(this.allLayersRadioButton);
            this.radioButtonGroup.add(this.specificLayersRadioButton);
            FormUtils.addRowInGBL(this.snapOptionsPanel, 0, 0, this.allLayersRadioButton);
            FormUtils.addRowInGBL(this.snapOptionsPanel, 1, 0, this.specificLayersRadioButton);
        }
        return this.snapOptionsPanel;
    }

    @Override
    public Icon getIcon() {
        return GUIUtil.toSmallIcon(SnapVerticesTool.ICON);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init() {
        HashSet<String> layerNames = new HashSet<String>();
        for (TaskFrame objTaskFrame : JUMPWorkbench.getFrameInstance().getContext().getTaskManager().getTasks()) {
            if (objTaskFrame == null || !(objTaskFrame instanceof TaskFrame)) continue;
            TaskFrame taskFrame = objTaskFrame;
            for (Layer objLayer : taskFrame.getLayerManager().getLayers()) {
                Layer layer;
                if (objLayer == null || !(objLayer instanceof Layer) || (layer = objLayer).isRaster()) continue;
                layerNames.add(layer.getName());
            }
        }
        this.selectionPanel.setListObjects(layerNames);
        ArrayList layersToSnapList = null;
        Object value = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(SPECIFIC_LAYERS_TO_SNAP_KEY);
        layersToSnapList = value == null || !(value instanceof ArrayList) ? new ArrayList() : (ArrayList)value;
        this.selectionPanel.setSelectedObjects(layersToSnapList);
        if (PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_SPECIFIC_LAYERS_KEY, false)) {
            this.specificLayersRadioButton.setSelected(true);
        } else {
            this.allLayersRadioButton.setSelected(true);
        }
        this.selectionPanel.setEnabled(this.specificLayersRadioButton.isSelected());
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_SPECIFIC_LAYERS_KEY, this.specificLayersRadioButton.isSelected());
        if (this.specificLayersRadioButton.isSelected()) {
            ArrayList<String> layersToSnapList = new ArrayList<String>();
            for (Object obj : this.selectionPanel.getSelectedObjects()) {
                layersToSnapList.add(obj.toString());
            }
            PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).put(SPECIFIC_LAYERS_TO_SNAP_KEY, layersToSnapList);
        }
        for (TaskFrame objTaskFrame : JUMPWorkbench.getFrameInstance().getContext().getTaskManager().getTasks()) {
            if (objTaskFrame == null || !(objTaskFrame instanceof TaskFrame)) continue;
            TaskFrame taskFrame = objTaskFrame;
            VisiblePointsAndLinesCache.instance(taskFrame.getLayerViewPanel()).invalidate();
        }
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.allLayersRadioButton || e.getSource() == this.specificLayersRadioButton) {
            this.selectionPanel.setEnabled(this.specificLayersRadioButton.isSelected());
        }
    }
}

