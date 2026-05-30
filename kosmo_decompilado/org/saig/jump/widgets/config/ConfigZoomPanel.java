/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.NumberSpinner;

public class ConfigZoomPanel
extends OptionsPanel {
    public static final String PERCENT_OF_BORDER_ON_ZOOMED_VIEW_KEY = String.valueOf(ConfigZoomPanel.class.getName()) + " - PERCENT OF BORDER ON ZOOMED VIEW";
    public static final String KEEP_CURSOR_LOCATION_ON_WHEEL_ZOOM_KEY = String.valueOf(ConfigZoomPanel.class.getName()) + " - KEEP CURSOR LOCATION ON WHEEL ZOOM";
    public static final String ZOOM_FACTOR_KEY = String.valueOf(ConfigZoomPanel.class.getName()) + " - ZOOM FACTOR";
    public static final int DEFAULT_PERCENT_OF_BORDER_ON_ZOOMED_VIEW = 5;
    public static final double DEFAULT_ZOOM_FACTOR = 2.0;
    private static final long serialVersionUID = 1L;
    public static final String NAME = I18N.getString(ConfigZoomPanel.class, "zoom-options");
    public static final Icon ICON = GUIUtil.toSmallIcon(IconLoader.icon("ZoomToLayer.gif"));
    private NumberSpinner percentOfBorderOnZoomSpinner;
    private JRadioButton underCursorWheelZoomRadioButton;
    private JRadioButton centerOfViewWheelZoomRadioButton;
    private NumberSpinner zoomFactorSpinner;
    private Blackboard blackboard;

    public ConfigZoomPanel(Blackboard blackboard) {
        this.setLayout(new GridBagLayout());
        this.blackboard = blackboard;
        JPanel borderDistancePanel = new JPanel(new FlowLayout(0));
        borderDistancePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigZoomPanel.distance-to-border")));
        JLabel percentOfBorderOnZoomLabel = new JLabel(String.valueOf(I18N.getString(ConfigZoomPanel.class, "average-percentage-to-view-border-when-zoom-performed")) + ":");
        this.percentOfBorderOnZoomSpinner = new NumberSpinner(5, 0, 99, 1);
        borderDistancePanel.add(percentOfBorderOnZoomLabel);
        borderDistancePanel.add(this.percentOfBorderOnZoomSpinner);
        JPanel mouseWheelBehaviourPanel = new JPanel(new GridBagLayout());
        mouseWheelBehaviourPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigZoomPanel.Mouse-wheel-behaviour")));
        this.underCursorWheelZoomRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigZoomPanel.Zoom-keeping-the-point-situated-under-the-cursor"));
        this.centerOfViewWheelZoomRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigZoomPanel.Zoom-keeping-the-center-point-of-the-current-envelope-as-center"));
        this.underCursorWheelZoomRadioButton.setSelected(true);
        ButtonGroup mouseWheelBehaviourButtonGroup = new ButtonGroup();
        mouseWheelBehaviourButtonGroup.add(this.underCursorWheelZoomRadioButton);
        mouseWheelBehaviourButtonGroup.add(this.centerOfViewWheelZoomRadioButton);
        FormUtils.addRowInGBL(mouseWheelBehaviourPanel, 0, 0, this.underCursorWheelZoomRadioButton);
        FormUtils.addRowInGBL(mouseWheelBehaviourPanel, 1, 0, this.centerOfViewWheelZoomRadioButton);
        JPanel zoomFactorPanel = new JPanel(new FlowLayout(0));
        zoomFactorPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(ConfigZoomPanel.class, "zoom-factor")));
        JLabel zoomFactorLabel = new JLabel(String.valueOf(I18N.getString(ConfigZoomPanel.class, "magnification-level")) + ":");
        this.zoomFactorSpinner = new NumberSpinner(2.0, 1.0, 50.0, 0.2);
        zoomFactorPanel.add(zoomFactorLabel);
        zoomFactorPanel.add(this.zoomFactorSpinner);
        FormUtils.addRowInGBL(this, 0, 0, borderDistancePanel);
        FormUtils.addRowInGBL(this, 1, 0, mouseWheelBehaviourPanel);
        FormUtils.addRowInGBL(this, 2, 0, zoomFactorPanel);
        FormUtils.addFiller(this, 3, 0);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init() {
        int percentOfBorder = PersistentBlackboardPlugIn.get(this.blackboard).get(PERCENT_OF_BORDER_ON_ZOOMED_VIEW_KEY, 5);
        boolean keepLocationAtCursor = PersistentBlackboardPlugIn.get(this.blackboard).get(KEEP_CURSOR_LOCATION_ON_WHEEL_ZOOM_KEY, false);
        double zoomFactor = PersistentBlackboardPlugIn.get(this.blackboard).get(ZOOM_FACTOR_KEY, 2.0);
        this.percentOfBorderOnZoomSpinner.setValue(percentOfBorder);
        if (keepLocationAtCursor) {
            this.underCursorWheelZoomRadioButton.setSelected(true);
        } else {
            this.centerOfViewWheelZoomRadioButton.setSelected(true);
        }
        this.zoomFactorSpinner.setValue(zoomFactor);
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(PERCENT_OF_BORDER_ON_ZOOMED_VIEW_KEY, ((Number)this.percentOfBorderOnZoomSpinner.getValue()).intValue());
        boolean keepLocationAtCenterOnWheelZoom = this.centerOfViewWheelZoomRadioButton.isSelected();
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEEP_CURSOR_LOCATION_ON_WHEEL_ZOOM_KEY, !keepLocationAtCenterOnWheelZoom);
        PersistentBlackboardPlugIn.get(this.blackboard).put(ZOOM_FACTOR_KEY, this.zoomFactorSpinner.getValue());
    }

    @Override
    public String validateInput() {
        return null;
    }

    public static final double getExtentFraction() {
        Blackboard blackboard = JUMPWorkbench.getFrameInstance().getContext().getBlackboard();
        int percentOfBorder = PersistentBlackboardPlugIn.get(blackboard).get(PERCENT_OF_BORDER_ON_ZOOMED_VIEW_KEY, 5);
        return (double)percentOfBorder / 100.0;
    }

    public static final double getZoomFactor() {
        Blackboard blackboard = JUMPWorkbench.getFrameInstance().getContext().getBlackboard();
        double zoomFactor = PersistentBlackboardPlugIn.get(blackboard).get(ZOOM_FACTOR_KEY, 2.0);
        return zoomFactor;
    }
}

