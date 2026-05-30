/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Area
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.core.crs.CrsRepositoryManager;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CrsException;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.UnitsManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.cts.EPSGSelectionDialog;
import org.saig.jump.widgets.tools.measuring.JAvailableAreaUnitsCombobox;
import org.saig.jump.widgets.tools.measuring.JAvailableLengthUnitsCombobox;

public class ConfigDefaultViewOptionsPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ConfigDefaultViewOptionsPanel.class);
    public static final String NAME = I18N.getString("org.saig.jump.widgets.config.ConfigDefaultViewOptionsPanel.views");
    public static final String DEFAULT_PROJECTION_KEY = String.valueOf(ConfigDefaultViewOptionsPanel.class.getName()) + " - DEFAULT PROJECTION KEY";
    public static final String DEFAULT_MAP_UNITS_KEY = String.valueOf(ConfigDefaultViewOptionsPanel.class.getName()) + " - DEFAULT MAP UNITS KEY";
    public static final String DEFAULT_LENGTH_MEASSURE_UNITS_KEY = String.valueOf(ConfigDefaultViewOptionsPanel.class.getName()) + " - DEFAULT LENGTH MEASSURE UNITS KEY";
    public static final String DEFAULT_AREA_MEASSURE_UNITS_KEY = String.valueOf(ConfigDefaultViewOptionsPanel.class.getName()) + " - DEFAULT AREA MEASSURE UNITS KEY";
    private IProjection currentDefaultProjection;
    private Blackboard blackboard;
    private JPanel defaultViewOptionsPanel;
    private JTextField defaultProjectionTextField;
    private JButton changeProjectionButton;
    private JAvailableLengthUnitsCombobox defaultTaskUnitsCombobox;
    private JAvailableLengthUnitsCombobox defaultLengthMeassureUnitsCombobox;
    private JAvailableAreaUnitsCombobox defaultAreaMeassureUnitsCombobox;

    public ConfigDefaultViewOptionsPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 1, 0, this.getViewOptionsPanel());
        FormUtils.addFiller(this, 3, 0);
    }

    private JComponent getViewOptionsPanel() {
        if (this.defaultViewOptionsPanel == null) {
            this.defaultViewOptionsPanel = new JPanel(new GridBagLayout());
            this.defaultViewOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigDefaultViewOptionsPanel.new-view-default-options")));
            JTextArea instructionsTextArea = new JTextArea(I18N.getString("org.saig.jump.widgets.config.ConfigDefaultViewOptionsPanel.The-new-views-default-options-will-be-applied-to-all-the-views-created-since-this-moment-it-will-not-affect-to-views-that-have-been-already-created"));
            instructionsTextArea.setFont(new JLabel().getFont());
            instructionsTextArea.setOpaque(false);
            instructionsTextArea.setToolTipText("");
            instructionsTextArea.setEditable(false);
            instructionsTextArea.setLineWrap(true);
            instructionsTextArea.setWrapStyleWord(true);
            this.defaultProjectionTextField = new JTextField();
            this.defaultProjectionTextField.setToolTipText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Current-projection"));
            this.defaultProjectionTextField.setMinimumSize(new Dimension(100, 20));
            this.defaultProjectionTextField.setPreferredSize(new Dimension(250, 20));
            this.defaultProjectionTextField.setEditable(false);
            this.defaultTaskUnitsCombobox = new JAvailableLengthUnitsCombobox();
            this.defaultTaskUnitsCombobox.setToolTipText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Map-units"));
            this.defaultTaskUnitsCombobox.setMinimumSize(new Dimension(100, 20));
            this.defaultTaskUnitsCombobox.setPreferredSize(new Dimension(250, 20));
            this.defaultLengthMeassureUnitsCombobox = new JAvailableLengthUnitsCombobox();
            this.defaultLengthMeassureUnitsCombobox.setToolTipText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Measurements-units"));
            this.defaultLengthMeassureUnitsCombobox.setMinimumSize(new Dimension(100, 20));
            this.defaultLengthMeassureUnitsCombobox.setPreferredSize(new Dimension(250, 20));
            this.defaultAreaMeassureUnitsCombobox = new JAvailableAreaUnitsCombobox();
            this.defaultAreaMeassureUnitsCombobox.setToolTipText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Area-measurements-units"));
            this.defaultAreaMeassureUnitsCombobox.setMinimumSize(new Dimension(100, 20));
            this.defaultAreaMeassureUnitsCombobox.setPreferredSize(new Dimension(250, 20));
            this.changeProjectionButton = new JButton(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Change"));
            this.changeProjectionButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    EPSGSelectionDialog csDialog = new EPSGSelectionDialog(JUMPWorkbench.getFrameInstance(), true, true, ConfigDefaultViewOptionsPanel.this.currentDefaultProjection);
                    if (csDialog.isOk()) {
                        ConfigDefaultViewOptionsPanel.this.currentDefaultProjection = csDialog.getProjection();
                        ConfigDefaultViewOptionsPanel.this.defaultProjectionTextField.setText(GUITranslationsUtils.getCRSDescription(ConfigDefaultViewOptionsPanel.this.currentDefaultProjection));
                    }
                }
            });
            FormUtils.addRowInGBL((JComponent)this.defaultViewOptionsPanel, 0, 0, (JComponent)instructionsTextArea, true, true);
            FormUtils.addRowInGBL((JComponent)this.defaultViewOptionsPanel, 1, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Current-projection")) + ": ", (JComponent)this.defaultProjectionTextField, false);
            FormUtils.addRowInGBL((JComponent)this.defaultViewOptionsPanel, 1, 2, (JComponent)this.changeProjectionButton, false, true);
            FormUtils.addRowInGBL((JComponent)this.defaultViewOptionsPanel, 2, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Map-units")) + ": ", (JComponent)this.defaultTaskUnitsCombobox, false);
            FormUtils.addRowInGBL((JComponent)this.defaultViewOptionsPanel, 3, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Measurements-units")) + ": ", (JComponent)this.defaultLengthMeassureUnitsCombobox, false);
            FormUtils.addRowInGBL((JComponent)this.defaultViewOptionsPanel, 4, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Area-measurements-units")) + ": ", (JComponent)this.defaultAreaMeassureUnitsCombobox, false);
        }
        return this.defaultViewOptionsPanel;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init() {
        IProjection projection = (IProjection)PersistentBlackboardPlugIn.get(this.blackboard).get(DEFAULT_PROJECTION_KEY);
        String mapUnits = PersistentBlackboardPlugIn.get(this.blackboard).get(DEFAULT_MAP_UNITS_KEY, UnitsManager.DEFAULT_LENGTH_UNIT).toString();
        String lengthMeassureUnits = PersistentBlackboardPlugIn.get(this.blackboard).get(DEFAULT_LENGTH_MEASSURE_UNITS_KEY, UnitsManager.DEFAULT_LENGTH_UNIT).toString();
        String areaMeasureUnits = PersistentBlackboardPlugIn.get(this.blackboard).get(DEFAULT_AREA_MEASSURE_UNITS_KEY, UnitsManager.DEFAULT_AREA_UNIT).toString();
        String initialProjectionName = "";
        if (projection == null) {
            try {
                projection = CrsRepositoryManager.getInstance().getCRS("EPSG:4326");
            }
            catch (CrsException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        if (projection == null) {
            initialProjectionName = EPSGSelectionDialog.NO_SRS_DEFINED;
        } else {
            this.currentDefaultProjection = projection;
            initialProjectionName = GUITranslationsUtils.getCRSDescription(projection);
        }
        this.defaultProjectionTextField.setText(initialProjectionName);
        Unit<Length> mapLengthUnit = UnitsManager.getLengthUnitFromName(mapUnits);
        if (((DefaultComboBoxModel)this.defaultTaskUnitsCombobox.getModel()).getIndexOf(mapLengthUnit) != -1) {
            this.defaultTaskUnitsCombobox.setSelectedItem(mapLengthUnit);
        } else {
            this.defaultTaskUnitsCombobox.setSelectedItem(UnitsManager.DEFAULT_LENGTH_UNIT);
        }
        Unit<Length> userLengthUnit = UnitsManager.getLengthUnitFromName(lengthMeassureUnits);
        if (((DefaultComboBoxModel)this.defaultLengthMeassureUnitsCombobox.getModel()).getIndexOf(userLengthUnit) != -1) {
            this.defaultLengthMeassureUnitsCombobox.setSelectedItem(userLengthUnit);
        } else {
            this.defaultLengthMeassureUnitsCombobox.setSelectedItem(UnitsManager.DEFAULT_LENGTH_UNIT);
        }
        Unit<Area> userAreaUnit = UnitsManager.getAreaUnitFromName(areaMeasureUnits);
        if (((DefaultComboBoxModel)this.defaultAreaMeassureUnitsCombobox.getModel()).getIndexOf(userAreaUnit) != -1) {
            this.defaultAreaMeassureUnitsCombobox.setSelectedItem(userAreaUnit);
        } else {
            this.defaultAreaMeassureUnitsCombobox.setSelectedItem(UnitsManager.DEFAULT_AREA_UNIT);
        }
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(DEFAULT_PROJECTION_KEY, this.currentDefaultProjection);
        PersistentBlackboardPlugIn.get(this.blackboard).put(DEFAULT_MAP_UNITS_KEY, this.defaultTaskUnitsCombobox.getSelectedItem().toString());
        PersistentBlackboardPlugIn.get(this.blackboard).put(DEFAULT_LENGTH_MEASSURE_UNITS_KEY, this.defaultLengthMeassureUnitsCombobox.getSelectedItem().toString());
        PersistentBlackboardPlugIn.get(this.blackboard).put(DEFAULT_AREA_MEASSURE_UNITS_KEY, this.defaultAreaMeassureUnitsCombobox.getSelectedItem().toString());
    }

    @Override
    public String validateInput() {
        return null;
    }
}

