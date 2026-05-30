/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin.wms;

import com.vividsolutions.jump.workbench.ui.TransparencyPanel;
import com.vividsolutions.jump.workbench.ui.wizard.AbstractWizardPanel;
import com.vividsolutions.wms.MapImageFormatChooser;
import com.vividsolutions.wms.WMService;
import es.kosmo.core.crs.CrsAxisOrder;
import es.kosmo.core.crs.CrsRepositoryManager;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import es.kosmo.desktop.widgets.sdi.SDIServiceSelectionWizardPanel;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.NumberSpinner;
import org.saig.jump.widgets.util.validating.DateTextFieldValidator;

public class SRSWizardPanel
extends AbstractWizardPanel {
    private static final long serialVersionUID = 1L;
    public static final String SRS_KEY = "SRS";
    public static final String TIME_KEY = "TIME";
    public static final String FORMAT_KEY = "FORMAT";
    public static final String TRANSPARENCY_LEVEL_KEY = "TRANSPARENCY";
    public static final String DEFAULT_INFORMATION_FORMAT = "text/html";
    public static final String DEFAULT_EXCEPTION_FORMAT = "application/vnd.ogc.se_xml";
    private static final Logger LOGGER = Logger.getLogger(SRSWizardPanel.class);
    private JLabel srsLabel;
    private DefaultComboBoxModel comboBoxModel;
    private JComboBox srsComboBox;
    private JLabel timeLabel;
    private JTextField timeText;
    private JDialog parent;
    private JLabel imageFormatLabel;
    private JComboBox imageFormats;
    private DefaultComboBoxModel imageFormatsCBModel;
    private JLabel transpLabel;
    private JCheckBox transpCB;
    private JLabel transpLevelLabel;
    private TransparencyPanel transpLevelPanel;
    private JLabel infoFormatsLabel;
    private JComboBox infoFormatsComboBox;
    private JLabel infoFeatureCountLabel;
    private NumberSpinner infoFeatureCountNumberSpinner;
    private JLabel exceptionFormatsLabel;
    private JComboBox exceptionFormatsComboBox;
    private WMService service;
    private Map<String, String> srsNameToCodeMap;
    private JLabel useDeclaredCapabilitiesURLLabel;
    private JCheckBox useDeclaredCapabilitiesURLCheckBox;
    private JCheckBox vendorParametersCheckBox;
    private JTextField vendorParametersTextField;
    private JLabel axisOrderLabel;
    private JTextField crsAxisOrderTextField;
    private JCheckBox forceAxisOrderToLongLatCheckBox;

    public SRSWizardPanel(JDialog parent) {
        this.parent = parent;
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
    }

    @Override
    public String getInstructions() {
        return I18N.getString("ui.plugin.wms.SRSWizardPanel.the-layers-you-chosen-support-more-than-one-coordinate-reference");
    }

    private void jbInit() throws Exception {
        this.setLayout(new GridBagLayout());
        this.srsLabel = new JLabel(I18N.getString("ui.plugin.wms.SRSWizardPanel.coordinate-reference-system"));
        this.srsComboBox = new JComboBox();
        this.srsComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                CrsAxisOrder axisOrder = SRSWizardPanel.this.getAxisOrderForSelectedSRS();
                SRSWizardPanel.this.setAxisOrder(axisOrder);
            }
        });
        this.timeLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.time")) + ":");
        this.timeText = new JTextField(10);
        this.timeText.setInputVerifier(new DateTextFieldValidator(this.parent, (JComponent)this.timeText, "yyyy-MM-dd"));
        this.imageFormatLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.Image-formats")) + ":");
        this.imageFormats = new JComboBox();
        this.imageFormats.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                String formato = (String)SRSWizardPanel.this.imageFormats.getSelectedItem();
                if (SRSWizardPanel.this.service.getVersion().equals("1.0.0")) {
                    if (StringUtils.contains((String)formato, (String)MapImageFormatChooser.IMAGE_FORMATS[0][0]) || StringUtils.contains((String)formato, (String)MapImageFormatChooser.IMAGE_FORMATS[0][1])) {
                        SRSWizardPanel.this.transpCB.setEnabled(true);
                        SRSWizardPanel.this.transpCB.setSelected(true);
                    } else {
                        SRSWizardPanel.this.transpCB.setSelected(false);
                        SRSWizardPanel.this.transpCB.setEnabled(false);
                    }
                } else if (SRSWizardPanel.this.service.getVersion().equals("1.1.0") || SRSWizardPanel.this.service.getVersion().equals("1.1.1") || SRSWizardPanel.this.service.getVersion().startsWith("1.3")) {
                    if (StringUtils.contains((String)formato, (String)MapImageFormatChooser.IMAGE_FORMATS[1][0]) || StringUtils.contains((String)formato, (String)MapImageFormatChooser.IMAGE_FORMATS[1][1])) {
                        SRSWizardPanel.this.transpCB.setEnabled(true);
                        SRSWizardPanel.this.transpCB.setSelected(true);
                    } else {
                        SRSWizardPanel.this.transpCB.setSelected(false);
                        SRSWizardPanel.this.transpCB.setEnabled(false);
                    }
                }
            }
        });
        this.transpCB = new JCheckBox();
        this.transpLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.Use-transparency")) + ":");
        this.transpLevelLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.transparency-level")) + " (" + I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.in-client") + "):");
        this.transpLevelPanel = new TransparencyPanel();
        this.transpLevelPanel.getSlider().setValue(0);
        this.infoFormatsLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.information-format")) + ":");
        this.infoFormatsComboBox = new JComboBox();
        this.infoFeatureCountLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.Feature-count")) + ":");
        this.infoFeatureCountNumberSpinner = new NumberSpinner(1, 1, 9999, 1);
        this.exceptionFormatsLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.exceptions-format")) + ":");
        this.exceptionFormatsComboBox = new JComboBox();
        this.useDeclaredCapabilitiesURLLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.use-declared-capabilities-urls")) + ":");
        this.useDeclaredCapabilitiesURLCheckBox = new JCheckBox();
        this.vendorParametersCheckBox = new JCheckBox(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.Vendor-parameters")) + ":");
        this.vendorParametersCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SRSWizardPanel.this.vendorParametersTextField.setEnabled(SRSWizardPanel.this.vendorParametersCheckBox.isSelected());
            }
        });
        this.vendorParametersTextField = new JTextField();
        this.vendorParametersTextField.setEnabled(false);
        this.axisOrderLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.Coordinate-axis")) + ":");
        this.crsAxisOrderTextField = new JTextField();
        this.crsAxisOrderTextField.setEnabled(false);
        this.forceAxisOrderToLongLatCheckBox = new JCheckBox(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.Force-coordinate-axis-to-longitude-latitude")) + " (E/N)");
        this.forceAxisOrderToLongLatCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (SRSWizardPanel.this.forceAxisOrderToLongLatCheckBox.isSelected()) {
                    SRSWizardPanel.this.setAxisOrder(CrsAxisOrder.EAST_NORTH);
                } else {
                    CrsAxisOrder axisOrder = SRSWizardPanel.this.getAxisOrderForSelectedSRS();
                    SRSWizardPanel.this.setAxisOrder(axisOrder);
                }
            }
        });
        int row = 0;
        FormUtils.addRowInGBL(this, row++, 0, FormUtils.getTitleLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.basics")));
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.srsLabel, (JComponent)this.srsComboBox, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.imageFormatLabel, (JComponent)this.imageFormats, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.transpLabel, (JComponent)this.transpCB);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.infoFormatsLabel, (JComponent)this.infoFormatsComboBox, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.infoFeatureCountLabel, (JComponent)this.infoFeatureCountNumberSpinner, false);
        FormUtils.addRowInGBL(this, row++, 0, new JLabel(" "));
        FormUtils.addRowInGBL(this, row++, 0, FormUtils.getTitleLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.advanced")));
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.axisOrderLabel, (JComponent)this.crsAxisOrderTextField, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, (JComponent)this.forceAxisOrderToLongLatCheckBox, true, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.timeLabel, (JComponent)this.timeText, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.transpLevelLabel, (JComponent)this.transpLevelPanel, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.exceptionFormatsLabel, (JComponent)this.exceptionFormatsComboBox, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.useDeclaredCapabilitiesURLLabel, (JComponent)this.useDeclaredCapabilitiesURLCheckBox);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.vendorParametersCheckBox, (JComponent)this.vendorParametersTextField);
        FormUtils.addFiller(this, row++, 0);
    }

    protected void setAxisOrder(CrsAxisOrder axisOrder) {
        this.crsAxisOrderTextField.setText(GUITranslationsUtils.getAxisOrderDescription(axisOrder));
    }

    protected CrsAxisOrder getAxisOrderForSelectedSRS() {
        CrsAxisOrder axisOrder = CrsAxisOrder.EAST_NORTH;
        if (!this.forceAxisOrderToLongLatCheckBox.isSelected()) {
            try {
                axisOrder = CrsRepositoryManager.getInstance().getAxisOrder(this.srsNameToCodeMap.get(this.srsComboBox.getSelectedItem()));
            }
            catch (Exception e1) {
                LOGGER.error((Object)e1);
            }
        }
        return axisOrder;
    }

    @Override
    public void exitingToRight() {
        String srsCode = this.srsNameToCodeMap.get(this.srsComboBox.getSelectedItem());
        this.dataMap.put(SRS_KEY, srsCode);
        if (!this.timeText.getText().trim().equals("")) {
            this.dataMap.put(TIME_KEY, this.timeText.getText());
        }
        this.dataMap.put(FORMAT_KEY, this.imageFormats.getSelectedItem());
        this.dataMap.put(TRANSPARENCY_LEVEL_KEY, 255 - this.transpLevelPanel.getSlider().getValue());
        this.service.setFormat((String)this.imageFormats.getSelectedItem());
        this.service.setTransparent(this.transpCB.isSelected());
        this.service.setInformationFormat((String)this.infoFormatsComboBox.getSelectedItem());
        this.service.setInformationFeatureCount(this.infoFeatureCountNumberSpinner.getIntValue());
        this.service.setExceptionFormat((String)this.exceptionFormatsComboBox.getSelectedItem());
        this.service.setUseDeclaredCapabilitiesURLs(this.useDeclaredCapabilitiesURLCheckBox.isEnabled() && this.useDeclaredCapabilitiesURLCheckBox.isSelected());
        if (this.vendorParametersCheckBox.isSelected() && !StringUtils.isEmpty((String)this.vendorParametersTextField.getText())) {
            this.service.setVendorParameters(this.vendorParametersTextField.getText().trim());
        }
        try {
            if (this.forceAxisOrderToLongLatCheckBox.isSelected()) {
                this.service.setAxisOrder(CrsAxisOrder.EAST_NORTH);
            } else {
                this.service.setAxisOrder(this.getAxisOrderForSelectedSRS());
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)e);
            this.service.setAxisOrder(CrsAxisOrder.EAST_NORTH);
        }
    }

    private List<String> getCommonSrsList() {
        return (List)this.dataMap.get("COMMON_SRS_LIST");
    }

    @Override
    public void enteredFromLeft(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
        this.service = (WMService)dataMap.get("SERVICE");
        String viewSRSName = (String)dataMap.get(SDIServiceSelectionWizardPanel.CURRENT_VIEW_SRS_NAME);
        Vector<String> vector = new Vector<String>();
        this.srsNameToCodeMap = new HashMap<String, String>();
        this.comboBoxModel = new DefaultComboBoxModel();
        for (String srs : this.getCommonSrsList()) {
            String srsName = GUITranslationsUtils.getName(srs);
            this.srsNameToCodeMap.put(srsName, srs);
            vector.addElement(srsName);
        }
        Collections.sort(vector);
        for (String srsName : vector) {
            this.comboBoxModel.addElement(srsName);
        }
        this.srsComboBox.setModel(this.comboBoxModel);
        if (viewSRSName != null) {
            this.srsComboBox.setSelectedItem(viewSRSName);
        }
        this.imageFormatsCBModel = new DefaultComboBoxModel();
        Object[] formatos = this.service.getCapabilities().getMapFormats();
        Arrays.sort(formatos);
        int pngIndex = -1;
        int gifIndex = -1;
        int i = 0;
        while (i < formatos.length && pngIndex == -1) {
            if (((String)formatos[i]).contains("png")) {
                pngIndex = i;
            } else if (((String)formatos[i]).contains("gif")) {
                gifIndex = i;
            }
            this.imageFormatsCBModel.addElement(formatos[i]);
            ++i;
        }
        this.imageFormats.setModel(this.imageFormatsCBModel);
        if (formatos.length > 0) {
            int index = 0;
            if (pngIndex != -1) {
                index = pngIndex;
            } else if (gifIndex != -1) {
                index = gifIndex;
            }
            this.imageFormats.setSelectedIndex(index);
        }
        List<String> availableInfoFormats = this.service.getCapabilities().getInfoFormats();
        Collections.sort(availableInfoFormats);
        this.infoFormatsComboBox.removeAllItems();
        Iterator<String> itInfoFormats = availableInfoFormats.iterator();
        while (itInfoFormats.hasNext()) {
            this.infoFormatsComboBox.addItem(itInfoFormats.next());
        }
        this.infoFormatsComboBox.setSelectedItem(DEFAULT_INFORMATION_FORMAT);
        List<String> availableExceptionFormats = this.service.getCapabilities().getExceptionFormats();
        Collections.sort(availableExceptionFormats);
        this.exceptionFormatsComboBox.removeAllItems();
        Iterator<String> itExcFormats = availableExceptionFormats.iterator();
        while (itExcFormats.hasNext()) {
            this.exceptionFormatsComboBox.addItem(itExcFormats.next());
        }
        this.exceptionFormatsComboBox.setSelectedItem(DEFAULT_EXCEPTION_FORMAT);
        String serverURL = this.service.getServerUrl();
        String getMapURL = this.service.getGetMapUrl();
        String getFeatureInfoURL = this.service.getGetFeatureInfoUrl();
        if (getMapURL != null && !getMapURL.equals(serverURL) || getFeatureInfoURL != null && !getFeatureInfoURL.equals(serverURL)) {
            this.useDeclaredCapabilitiesURLLabel.setEnabled(true);
            this.useDeclaredCapabilitiesURLCheckBox.setEnabled(true);
            this.useDeclaredCapabilitiesURLCheckBox.setSelected(true);
        } else {
            this.useDeclaredCapabilitiesURLLabel.setEnabled(false);
            this.useDeclaredCapabilitiesURLCheckBox.setEnabled(false);
        }
        boolean showAxisOrderOptions = this.service.getVersion().startsWith("1.3");
        this.axisOrderLabel.setVisible(showAxisOrderOptions);
        this.crsAxisOrderTextField.setVisible(showAxisOrderOptions);
        this.forceAxisOrderToLongLatCheckBox.setVisible(showAxisOrderOptions);
        this.setAxisOrder(this.getAxisOrderForSelectedSRS());
    }

    @Override
    public String getTitle() {
        return I18N.getString("ui.plugin.wms.SRSWizardPanel.select-coordinate-reference-system");
    }

    @Override
    public String getID() {
        return this.getClass().getName();
    }

    @Override
    public boolean isInputValid() {
        return this.isPanelOk();
    }

    @Override
    public String getNextID() {
        return null;
    }

    @Override
    public boolean isPanelOk() {
        if (!this.timeText.getText().trim().equals("")) {
            return this.timeText.getInputVerifier().verify(this.timeText);
        }
        return true;
    }
}

