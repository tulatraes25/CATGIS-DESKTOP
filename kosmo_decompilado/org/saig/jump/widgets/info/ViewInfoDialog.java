/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Area
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.widgets.info;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarRenderer;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.gui.swing.locale.TranslatableSelectionDialog;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.cts.EPSGSelectionDialog;
import org.saig.jump.widgets.tools.measuring.JAvailableAreaUnitsCombobox;
import org.saig.jump.widgets.tools.measuring.JAvailableLengthUnitsCombobox;
import org.saig.jump.widgets.util.DialogFactory;

public class ViewInfoDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    public static final Logger LOGGER = Logger.getLogger(ViewInfoDialog.class);
    private JTextField textFieldNombre;
    private JTextField textFieldProyeccion;
    private JTextField textFieldTitle;
    private JButton langSelecButton;
    private JCheckBox checkBoxScale;
    private JButton buttonProyeccion;
    private JAvailableLengthUnitsCombobox mapLengthUnitsCombobox;
    private JAvailableLengthUnitsCombobox userLengthUnitsCombobox;
    private JAvailableAreaUnitsCombobox userAreaUnitsCombobox;
    private OKCancelPanel okCancelPanel;
    private String originalTaskName;
    private String originalTaskProjectionName;
    private boolean isScaleBarON;
    private Task taskOrig;
    private TaskFrame taskFrame;
    private LayerViewPanel layerViewPanel;
    private IProjection selectedTaskProjection;

    public ViewInfoDialog(JFrame parent, boolean modal, TaskFrame taskFrame, LayerViewPanel layerViewPanel) {
        super((Frame)parent, modal);
        this.taskFrame = taskFrame;
        this.taskOrig = taskFrame.getTask();
        this.layerViewPanel = layerViewPanel;
        this.originalTaskName = StringUtils.isEmpty((String)this.taskOrig.getName()) ? "" : this.taskOrig.getName();
        this.originalTaskProjectionName = this.taskOrig.getProjection() == null ? EPSGSelectionDialog.NO_SRS_DEFINED : GUITranslationsUtils.getCRSDescription(this.taskOrig.getProjection());
        this.isScaleBarON = ScaleBarRenderer.isEnabled(layerViewPanel);
        this.setTitle(String.valueOf(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.View-properties")) + " - " + this.originalTaskName);
        this.initialize();
        this.pack();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addFiller(mainPanel, 0, 0);
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.createCamposPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.createOKCancelPanel());
        FormUtils.addFiller(mainPanel, 3, 0);
    }

    private boolean isInputValid() {
        boolean valid = true;
        Collection<Layer> editableLayers = this.taskFrame.getLayerManager().getEditableLayers();
        if (CollectionUtils.isNotEmpty(editableLayers)) {
            for (Layer currentLayer : editableLayers) {
                if (!currentLayer.isEditable() || this.selectedTaskProjection == null || currentLayer.getProjection() == null || currentLayer.getProjection().getAbrev().equals(this.selectedTaskProjection.getAbrev())) continue;
                DialogFactory.showWarningDialog(this, I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Editable-layers-can-not-be-reprojected"), I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Error"));
                valid = false;
            }
        }
        return valid;
    }

    private OKCancelPanel createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.setAcceptButtonText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Save-changes"));
        this.okCancelPanel.addActionListener(new ActionListener(){
            boolean error = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (ViewInfoDialog.this.okCancelPanel.wasOKPressed()) {
                    boolean bl = this.error = !ViewInfoDialog.this.isInputValid();
                    if (!this.error) {
                        String string_tmp = ViewInfoDialog.this.textFieldNombre.getText();
                        if (!string_tmp.equals(ViewInfoDialog.this.originalTaskName)) {
                            ViewInfoDialog.this.taskOrig.setName(string_tmp);
                        }
                        if (ViewInfoDialog.this.taskOrig.getProjection() == null && ViewInfoDialog.this.selectedTaskProjection != null || ViewInfoDialog.this.selectedTaskProjection != null && !ViewInfoDialog.this.selectedTaskProjection.getAbrev().equals(ViewInfoDialog.this.taskOrig.getProjection().getAbrev())) {
                            ViewInfoDialog.this.taskOrig.setProjection(ViewInfoDialog.this.selectedTaskProjection);
                            List<Layer> layers = ViewInfoDialog.this.taskFrame.getLayerManager().getLayers();
                            for (Layer currentLayer : layers) {
                                try {
                                    if (currentLayer.getProjection() != null && !currentLayer.getProjection().getAbrev().equals(ViewInfoDialog.this.selectedTaskProjection.getAbrev())) {
                                        currentLayer.setProjection(currentLayer.getProjection(), ViewInfoDialog.this.selectedTaskProjection);
                                        continue;
                                    }
                                    currentLayer.setCoordTrans(null);
                                }
                                catch (Exception e) {
                                    LOGGER.error((Object)"", (Throwable)e);
                                    DialogFactory.showErrorDialog(ViewInfoDialog.this, I18N.getMessage("org.saig.jump.widgets.info.ViewInfoDialog.An-error-has-been-produced-while-changing-the-layer-{0}-spatial-reference-system", new Object[]{currentLayer.getName()}), I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Error-changing-Spatial-Reference-System"));
                                }
                            }
                            if (ViewInfoDialog.this.taskFrame.getLayerViewPanel() != null) {
                                ViewInfoDialog.this.taskFrame.getLayerViewPanel().repaint();
                            }
                        }
                        ViewInfoDialog.this.taskFrame.updateTitle();
                        Unit selectedMapValue = (Unit)ViewInfoDialog.this.mapLengthUnitsCombobox.getSelectedItem();
                        ViewInfoDialog.this.layerViewPanel.setMapLengthUnit((Unit<Length>)selectedMapValue);
                        Unit selectedUserLengthValue = (Unit)ViewInfoDialog.this.userLengthUnitsCombobox.getSelectedItem();
                        ViewInfoDialog.this.layerViewPanel.setUserLengthUnit((Unit<Length>)selectedUserLengthValue);
                        Unit selectedUserAreaValue = (Unit)ViewInfoDialog.this.userAreaUnitsCombobox.getSelectedItem();
                        ViewInfoDialog.this.layerViewPanel.setUserAreaUnit((Unit<Area>)selectedUserAreaValue);
                        ScaleBarRenderer.setEnabled(ViewInfoDialog.this.checkBoxScale.isSelected(), ViewInfoDialog.this.layerViewPanel);
                        ViewInfoDialog.this.layerViewPanel.getRenderingManager().render("SCALE_BAR");
                    }
                } else {
                    this.error = false;
                }
                if (!this.error) {
                    ViewInfoDialog.this.setVisible(false);
                }
            }
        });
        return this.okCancelPanel;
    }

    private JPanel createCamposPanel() {
        JPanel camposPanel = new JPanel(new GridBagLayout());
        this.textFieldNombre = new JTextField(this.originalTaskName);
        this.textFieldNombre.setToolTipText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Layer-name"));
        this.textFieldNombre.setMinimumSize(new Dimension(100, 20));
        this.textFieldNombre.setPreferredSize(new Dimension(250, 20));
        this.textFieldTitle = new JTextField(this.taskOrig.getTitle(LocaleManager.getActiveLocale()));
        this.textFieldTitle.setEditable(false);
        this.langSelecButton = new JButton(IconLoader.icon("country.gif"));
        this.langSelecButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ViewInfoDialog.this.langSelecButtonActionListener();
            }
        });
        this.textFieldProyeccion = new JTextField(this.originalTaskProjectionName);
        this.textFieldProyeccion.setToolTipText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Current-projection"));
        this.textFieldProyeccion.setMinimumSize(new Dimension(100, 20));
        this.textFieldProyeccion.setPreferredSize(new Dimension(250, 20));
        this.textFieldProyeccion.setEditable(false);
        this.checkBoxScale = new JCheckBox();
        this.checkBoxScale.setSelected(this.isScaleBarON);
        this.checkBoxScale.setToolTipText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Mark-to-visualize-scale-bar-in-current-view"));
        this.mapLengthUnitsCombobox = new JAvailableLengthUnitsCombobox();
        this.mapLengthUnitsCombobox.setToolTipText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Map-units"));
        this.mapLengthUnitsCombobox.setMinimumSize(new Dimension(100, 20));
        this.mapLengthUnitsCombobox.setPreferredSize(new Dimension(250, 20));
        this.mapLengthUnitsCombobox.setSelectedItem(this.layerViewPanel.getMapLengthUnit());
        this.userLengthUnitsCombobox = new JAvailableLengthUnitsCombobox();
        this.userLengthUnitsCombobox.setToolTipText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Measurements-units"));
        this.userLengthUnitsCombobox.setMinimumSize(new Dimension(100, 20));
        this.userLengthUnitsCombobox.setPreferredSize(new Dimension(250, 20));
        this.userLengthUnitsCombobox.setSelectedItem(this.layerViewPanel.getUserLengthUnit());
        this.userAreaUnitsCombobox = new JAvailableAreaUnitsCombobox();
        this.userAreaUnitsCombobox.setToolTipText(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Area-measurements-units"));
        this.userAreaUnitsCombobox.setMinimumSize(new Dimension(100, 20));
        this.userAreaUnitsCombobox.setPreferredSize(new Dimension(250, 20));
        this.userAreaUnitsCombobox.setSelectedItem(this.layerViewPanel.getUserAreaUnit());
        this.buttonProyeccion = new JButton(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Change"));
        this.buttonProyeccion.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                EPSGSelectionDialog csDialog = new EPSGSelectionDialog(JUMPWorkbench.getFrameInstance(), true, true, ViewInfoDialog.this.selectedTaskProjection);
                if (csDialog.isOk()) {
                    ViewInfoDialog.this.selectedTaskProjection = csDialog.getProjection();
                    ViewInfoDialog.this.textFieldProyeccion.setText(GUITranslationsUtils.getCRSDescription(ViewInfoDialog.this.selectedTaskProjection));
                }
            }
        });
        FormUtils.addRowInGBL((JComponent)camposPanel, 1, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Name")) + ": ", (JComponent)this.textFieldNombre, false);
        FormUtils.addRowInGBL((JComponent)camposPanel, 2, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Title")) + ":", (JComponent)this.textFieldTitle);
        FormUtils.addRowInGBL((JComponent)camposPanel, 2, 2, (JComponent)this.langSelecButton, false, true);
        FormUtils.addRowInGBL((JComponent)camposPanel, 3, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Current-projection")) + ": ", (JComponent)this.textFieldProyeccion, false);
        FormUtils.addRowInGBL((JComponent)camposPanel, 3, 2, (JComponent)this.buttonProyeccion, false, true);
        FormUtils.addRowInGBL((JComponent)camposPanel, 4, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Map-units")) + ": ", (JComponent)this.mapLengthUnitsCombobox, false);
        FormUtils.addRowInGBL((JComponent)camposPanel, 5, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Measurements-units")) + ": ", (JComponent)this.userLengthUnitsCombobox, false);
        FormUtils.addRowInGBL((JComponent)camposPanel, 6, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Area-measurements-units")) + ": ", (JComponent)this.userAreaUnitsCombobox, false);
        FormUtils.addRowInGBL((JComponent)camposPanel, 7, 0, I18N.getString("org.saig.jump.widgets.info.ViewInfoDialog.Scale-bar"), (JComponent)this.checkBoxScale, false);
        return camposPanel;
    }

    protected void langSelecButtonActionListener() {
        TranslatableSelectionDialog localeSelDia = new TranslatableSelectionDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("org.saig.jump.widgets.info.LayerInfoDialog.Translations-for-the-layer-{0}-title", new Object[]{this.taskOrig.getName()}), this.taskOrig);
        if (localeSelDia.isOk()) {
            this.textFieldTitle.setText(this.taskOrig.getTitle(LocaleManager.getActiveLocale()));
        }
    }
}

