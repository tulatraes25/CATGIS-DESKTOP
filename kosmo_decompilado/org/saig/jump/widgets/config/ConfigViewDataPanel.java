/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.ApplicationExitListener;
import org.saig.jump.widgets.util.NumberSpinner;

public class ConfigViewDataPanel
extends OptionsPanel
implements ApplicationExitListener {
    public static final String KEY_NUMBER_OF_DECIMALS = "KEY_NUMBER_OF_DECIMALS";
    public static final String KEY_NUMBER_OF_DECIMALS_ENABLED = String.valueOf(ConfigViewDataPanel.class.getName()) + " - NUMBER OF DECIMALS ENABLED";
    public static final String KEY_REMENBER_WINDOW_STATUS_ON_CLOSE_ENABLED = String.valueOf(ConfigViewDataPanel.class.getName()) + " - REMENBER WINDOW STATUS ON CLOSE ENABLED";
    public static final String KEY_ALWAYS_ON_TOP_ENABLED = String.valueOf(ConfigViewDataPanel.class.getName()) + " - ALWAYS ON TOP ENABLED";
    public static final String KEY_MAIN_FRAME_WIDTH = String.valueOf(ConfigViewDataPanel.class.getName()) + " - MAIN FRAME WIDTH";
    public static final String KEY_MAIN_FRAME_HEIGTH = String.valueOf(ConfigViewDataPanel.class.getName()) + " - MAIN FRAME HEIGTH";
    public static final String KEY_MAIN_FRAME_LOCATION_X = String.valueOf(ConfigViewDataPanel.class.getName()) + " - MAIN FRAME LOCATION X";
    public static final String KEY_MAIN_FRAME_LOCATION_Y = String.valueOf(ConfigViewDataPanel.class.getName()) + " - MAIN FRAME LOCATION Y";
    public static final String KEY_MAIN_FRAME_EXTANDED_STATE = String.valueOf(ConfigViewDataPanel.class.getName()) + " - MAIN FRAME EXTANDED STATE";
    public static final int DEFAULT_MAIN_FRAME_WIDTH = 900;
    public static final int DEFAULT_MAIN_FRAME_HEIGTH = 675;
    public static final int DEFAULT_MAIN_FRAME_LOCATION_X = 0;
    public static final int DEFAULT_MAIN_FRAME_LOCATION_Y = 0;
    public static final int DEFAULT_MAIN_FRAME_EXTANDED_STATE = 6;
    public static final boolean DEFAULT_MAIN_FRAME_ALWAYS_ON_TOP = false;
    private static final long serialVersionUID = 1L;
    public static final String NAME = I18N.getString("org.saig.jump.widgets.config.ConfigViewDataPanel.Viewing-options");
    public static final Icon ICON = IconLoader.icon("Eye.gif");
    private NumberSpinner numberOfDecimalsSpinner = null;
    private JCheckBox decimalFilterCheckBox;
    private JCheckBox rememberStatusCheckBox;
    private JCheckBox alwaysOnTopCheckBox;
    private Blackboard blackboard;
    private JPanel numberOfDecimalsPanel;
    private JPanel mainWindowOptionsPanel;

    public ConfigViewDataPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getNumberOfDecimalsPanel());
        FormUtils.addRowInGBL(this, 1, 0, this.getMainWindowOptionsPanel());
        FormUtils.addFiller(this, 2, 0);
        JUMPWorkbench.getFrameInstance().getApplicationExitHandler().addExitListener(this);
    }

    private JPanel getNumberOfDecimalsPanel() {
        if (this.numberOfDecimalsPanel == null) {
            this.numberOfDecimalsPanel = new JPanel(new GridBagLayout());
            this.numberOfDecimalsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigViewDataPanel.Decimal-positions")));
            this.decimalFilterCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.config.ConfigViewDataPanel.Enable-the-decimal-position-filter"));
            this.decimalFilterCheckBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfigViewDataPanel.this.numberOfDecimalsSpinner.setEnabled(ConfigViewDataPanel.this.decimalFilterCheckBox.isSelected());
                }
            });
            this.numberOfDecimalsSpinner = new NumberSpinner(2, 0, 32, 1);
            FormUtils.addRowInGBL((JComponent)this.numberOfDecimalsPanel, 0, 0, (JComponent)this.decimalFilterCheckBox, false, true);
            FormUtils.addRowInGBL((JComponent)this.numberOfDecimalsPanel, 0, 30, (JComponent)this.numberOfDecimalsSpinner, false, true);
            FormUtils.addFiller(this.numberOfDecimalsPanel, 0, 50);
        }
        return this.numberOfDecimalsPanel;
    }

    private JPanel getMainWindowOptionsPanel() {
        if (this.mainWindowOptionsPanel == null) {
            this.mainWindowOptionsPanel = new JPanel(new GridBagLayout());
            this.mainWindowOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "main-window-options")));
            this.rememberStatusCheckBox = new JCheckBox(I18N.getString(this.getClass(), "remember-window-state-on-exit"));
            this.alwaysOnTopCheckBox = new JCheckBox(I18N.getString(this.getClass(), "always-on-top"));
            FormUtils.addRowInGBL((JComponent)this.mainWindowOptionsPanel, 0, 0, (JComponent)this.rememberStatusCheckBox, false, true);
            FormUtils.addRowInGBL((JComponent)this.mainWindowOptionsPanel, 1, 0, (JComponent)this.alwaysOnTopCheckBox, false, true);
            FormUtils.addFiller(this.mainWindowOptionsPanel, 2, 0);
        }
        return this.mainWindowOptionsPanel;
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_NUMBER_OF_DECIMALS_ENABLED, this.decimalFilterCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_NUMBER_OF_DECIMALS, ((Number)this.numberOfDecimalsSpinner.getValue()).intValue());
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_REMENBER_WINDOW_STATUS_ON_CLOSE_ENABLED, this.rememberStatusCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_ALWAYS_ON_TOP_ENABLED, this.alwaysOnTopCheckBox.isSelected());
        JUMPWorkbench.getFrameInstance().setAlwaysOnTop(this.alwaysOnTopCheckBox.isSelected());
    }

    @Override
    public void init() {
        boolean numberOfDecimalEnabled = PersistentBlackboardPlugIn.get(this.blackboard).get(KEY_NUMBER_OF_DECIMALS_ENABLED, false);
        int numbersOfDecimal = PersistentBlackboardPlugIn.get(this.blackboard).get(KEY_NUMBER_OF_DECIMALS, 2);
        boolean rememberStatus = PersistentBlackboardPlugIn.get(this.blackboard).get(KEY_REMENBER_WINDOW_STATUS_ON_CLOSE_ENABLED, false);
        boolean alwaysOnTop = PersistentBlackboardPlugIn.get(this.blackboard).get(KEY_ALWAYS_ON_TOP_ENABLED, false);
        this.decimalFilterCheckBox.setSelected(numberOfDecimalEnabled);
        this.numberOfDecimalsSpinner.setEnabled(numberOfDecimalEnabled);
        this.numberOfDecimalsSpinner.setValue(numbersOfDecimal);
        this.rememberStatusCheckBox.setSelected(rememberStatus);
        this.alwaysOnTopCheckBox.setSelected(alwaysOnTop);
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
    public boolean exitingApplication() {
        WorkbenchFrame frame = JUMPWorkbench.getFrameInstance();
        Dimension dimension = frame.getSize();
        Point location = frame.getLocation();
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_MAIN_FRAME_WIDTH, dimension.width);
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_MAIN_FRAME_HEIGTH, dimension.height);
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_MAIN_FRAME_LOCATION_X, location.x);
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_MAIN_FRAME_LOCATION_Y, location.y);
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_MAIN_FRAME_EXTANDED_STATE, frame.getExtendedState());
        return true;
    }
}

