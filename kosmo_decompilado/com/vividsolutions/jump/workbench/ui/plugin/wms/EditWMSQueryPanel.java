/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin.wms;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.TransparencyPanel;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerPanel;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerWizardPanel;
import com.vividsolutions.wms.MapImageFormatChooser;
import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.WMService;
import es.kosmo.core.crs.CrsAxisOrder;
import es.kosmo.core.crs.CrsRepositoryManager;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.NumberSpinner;
import org.saig.jump.widgets.util.validating.DateTextFieldValidator;

public class EditWMSQueryPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = Logger.getLogger(EditWMSQueryPanel.class);
    private WMService service;
    private JLabel urlLabel;
    private JTextArea urlTextArea;
    private MapLayerPanel mapLayerPanel;
    private JLabel srsLabel;
    private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
    private JComboBox srsComboBox = new JComboBox();
    private JLabel formatLabel;
    private JComboBox formatComboBox = new JComboBox();
    private JLabel transparencyLabel;
    private JCheckBox transpCB = new JCheckBox();
    private JLabel transparencyLevelLabel;
    private TransparencyPanel transparencyLevelPanel;
    private JLabel timeLabel;
    private JTextField timeText;
    private static final String TIME_NO_VALID_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.TIME_NO_VALID_MESSAGE");
    private JLabel informationFormatsLabel;
    private JComboBox informationFormatsComboBox;
    private JLabel infoFeatureCountLabel;
    private NumberSpinner infoFeatureCountNumberSpinner;
    private JLabel exceptionFormatsLabel;
    private JComboBox exceptionFormatsComboBox;
    private JLabel useDeclaredCapabilitiesURLLabel;
    private JCheckBox useDeclaredCapabilitiesURLCheckBox;
    private JCheckBox vendorParametersCheckBox;
    private JTextField vendorParametersTextField;
    private Map<String, String> srsNameToCodeMap;
    private JLabel axisOrderLabel;
    private JTextField crsAxisOrderTextField;
    private JCheckBox forceAxisOrderToLongLatCheckBox;
    private EnableCheck[] enableChecks = new EnableCheck[]{new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return EditWMSQueryPanel.this.mapLayerPanel.getChosenMapLayers().isEmpty() ? I18N.getString("ui.plugin.wms.EditWMSQueryPanel.at-least-one-wms-must-be-chosen") : null;
        }
    }, new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return EditWMSQueryPanel.this.srsComboBox.getSelectedItem() == null ? MapLayerWizardPanel.NO_COMMON_SRS_MESSAGE : null;
        }
    }, new EnableCheck(){

        @Override
        public String check(JComponent component) {
            boolean isOK = true;
            if (!EditWMSQueryPanel.this.timeText.getText().trim().equals("")) {
                isOK = EditWMSQueryPanel.this.timeText.getInputVerifier().verify(EditWMSQueryPanel.this.timeText);
            }
            return !isOK ? TIME_NO_VALID_MESSAGE : null;
        }
    }};
    private JDialog dialog;
    private String lastSelectedSRS;

    public EditWMSQueryPanel(WMService service, List<String> initialChosenMapLayers, String initialSRS, String time, String initialFormat, int alpha, String initialInformationFormat, String initialExceptionFormat, boolean useDeclaredCapabilitiesURLs, JDialog dialog) {
        this.dialog = dialog;
        this.service = service;
        try {
            this.initialize();
            String url = service.getServerUrl();
            if (url.endsWith("?") || url.endsWith("&")) {
                url = url.substring(0, url.length() - 1);
            }
            this.urlTextArea.setText(url);
            this.urlTextArea.setCaretPosition(0);
            this.mapLayerPanel.init(service, initialChosenMapLayers);
            this.updateSRSComboBox();
            this.updateImageFormatComboBox();
            this.updateInformationFormatComboBox();
            this.infoFeatureCountNumberSpinner.setValue(service.getInformationFeatureCount());
            this.updateExceptionFormatComboBox();
            String srsName = GUITranslationsUtils.getName(initialSRS);
            this.srsComboBox.setSelectedItem(srsName);
            this.formatComboBox.setSelectedItem(initialFormat);
            this.informationFormatsComboBox.setSelectedItem(initialInformationFormat);
            this.exceptionFormatsComboBox.setSelectedItem(initialExceptionFormat);
            this.mapLayerPanel.add(new InputChangedListener(){

                @Override
                public void inputChanged() {
                    EditWMSQueryPanel.this.updateSRSComboBox();
                    EditWMSQueryPanel.this.updateImageFormatComboBox();
                    EditWMSQueryPanel.this.updateInformationFormatComboBox();
                    EditWMSQueryPanel.this.updateExceptionFormatComboBox();
                }
            });
            if (this.isTransparencySupported(service.getVersion(), initialFormat)) {
                this.transpCB.setEnabled(true);
                this.transpCB.setSelected(service.isTransparent());
            } else {
                this.transpCB.setSelected(false);
                this.transpCB.setEnabled(false);
            }
            this.transpCB.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == 1) {
                        EditWMSQueryPanel.this.setTransparent(true);
                    } else if (e.getStateChange() == 2) {
                        EditWMSQueryPanel.this.setTransparent(false);
                    }
                }
            });
            this.transparencyLevelPanel.getSlider().setValue(255 - alpha);
            this.setTime(time);
            String serverURL = service.getServerUrl();
            String getMapURL = service.getGetMapUrl();
            String getFeatureInfoURL = service.getGetFeatureInfoUrl();
            if (getMapURL != null && !getMapURL.equals(serverURL) || getFeatureInfoURL != null && !getFeatureInfoURL.equals(serverURL)) {
                this.useDeclaredCapabilitiesURLLabel.setEnabled(true);
                this.useDeclaredCapabilitiesURLCheckBox.setEnabled(true);
            }
            this.useDeclaredCapabilitiesURLCheckBox.setSelected(useDeclaredCapabilitiesURLs);
            boolean hasVendorParameters = StringUtils.isNotEmpty((String)service.getVendorParameters());
            this.vendorParametersCheckBox.setSelected(hasVendorParameters);
            this.vendorParametersTextField.setEnabled(hasVendorParameters);
            if (hasVendorParameters) {
                this.vendorParametersTextField.setText(service.getVendorParameters());
            }
        }
        catch (Exception ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
    }

    private boolean isTransparencySupported(String version, String formato) {
        if (version.equals("1.0.0")) {
            return StringUtils.contains((String)formato, (String)MapImageFormatChooser.IMAGE_FORMATS[0][0]) || StringUtils.contains((String)formato, (String)MapImageFormatChooser.IMAGE_FORMATS[0][1]);
        }
        if (version.equals("1.1.0") || version.equals("1.1.1") || version.startsWith("1.3")) {
            return StringUtils.contains((String)formato, (String)MapImageFormatChooser.IMAGE_FORMATS[1][0]) || StringUtils.contains((String)formato, (String)MapImageFormatChooser.IMAGE_FORMATS[1][1]);
        }
        return false;
    }

    private void updateImageFormatComboBox() {
        DefaultComboBoxModel<Object> imageFormatsCBModel = new DefaultComboBoxModel<Object>();
        Object[] formatos = this.service.getCapabilities().getMapFormats();
        Arrays.sort(formatos);
        Object selectedImageFormat = this.formatComboBox.getSelectedItem();
        imageFormatsCBModel.removeAllElements();
        int i = 0;
        while (i < formatos.length) {
            imageFormatsCBModel.addElement(formatos[i]);
            ++i;
        }
        this.formatComboBox.setModel(imageFormatsCBModel);
        if (selectedImageFormat != null) {
            this.formatComboBox.setSelectedItem(selectedImageFormat);
        }
    }

    private void updateInformationFormatComboBox() {
        List<String> infoFormats = this.service.getCapabilities().getInfoFormats();
        Collections.sort(infoFormats);
        Object selectedInformationFormat = this.informationFormatsComboBox.getSelectedItem();
        this.informationFormatsComboBox.removeAllItems();
        Iterator<String> itInfoFormats = infoFormats.iterator();
        while (itInfoFormats.hasNext()) {
            this.informationFormatsComboBox.addItem(itInfoFormats.next());
        }
        if (selectedInformationFormat != null) {
            this.informationFormatsComboBox.setSelectedItem(selectedInformationFormat);
        }
    }

    private void updateExceptionFormatComboBox() {
        List<String> exceptionFormats = this.service.getCapabilities().getExceptionFormats();
        Collections.sort(exceptionFormats);
        Object selectedExceptionFormat = this.exceptionFormatsComboBox.getSelectedItem();
        this.exceptionFormatsComboBox.removeAllItems();
        Iterator<String> itExceptionFormats = exceptionFormats.iterator();
        while (itExceptionFormats.hasNext()) {
            this.exceptionFormatsComboBox.addItem(itExceptionFormats.next());
        }
        if (selectedExceptionFormat != null) {
            this.exceptionFormatsComboBox.setSelectedItem(selectedExceptionFormat);
        }
    }

    public String getSRS() {
        String selectedSrs = (String)this.srsComboBox.getSelectedItem();
        List<String> srsList = this.mapLayerPanel.commonSRSList();
        Iterator<String> it = srsList.iterator();
        boolean encontrado = false;
        while (it.hasNext() && !encontrado) {
            String tmp = it.next();
            if (!selectedSrs.equals(GUITranslationsUtils.getName(tmp))) continue;
            selectedSrs = tmp;
            encontrado = true;
        }
        return selectedSrs;
    }

    public void setTime(String time) {
        if (time != null) {
            this.timeText.setText(time);
        }
    }

    public String getTime() {
        String time = this.timeText.getText().trim();
        if (time.equals("")) {
            return null;
        }
        return time;
    }

    public String getFormat() {
        return (String)this.formatComboBox.getSelectedItem();
    }

    private void updateSRSComboBox() {
        this.lastSelectedSRS = (String)this.srsComboBox.getSelectedItem();
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<String>();
        this.srsNameToCodeMap = new HashMap<String, String>();
        Vector<String> vector = new Vector<String>();
        for (String commonSRS : this.mapLayerPanel.commonSRSList()) {
            String srsName = GUITranslationsUtils.getName(commonSRS);
            this.srsNameToCodeMap.put(srsName, commonSRS);
            vector.add(srsName);
        }
        Collections.sort(vector);
        for (String srsName : vector) {
            comboBoxModel.addElement(srsName);
        }
        this.srsComboBox.setModel(comboBoxModel);
        this.srsComboBox.setSelectedItem(this.lastSelectedSRS);
        if (this.srsComboBox.getSelectedItem() == null && this.srsComboBox.getItemCount() > 0) {
            this.srsComboBox.setSelectedIndex(0);
        }
    }

    private void initialize() throws Exception {
        this.setLayout(new GridBagLayout());
        this.mapLayerPanel = new MapLayerPanel();
        this.mapLayerPanel.setMinimumSize(new Dimension(400, 180));
        this.mapLayerPanel.setPreferredSize(new Dimension(400, 180));
        this.formatLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.Image-formats")) + ":");
        this.formatComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                String formato = (String)EditWMSQueryPanel.this.formatComboBox.getSelectedItem();
                if (EditWMSQueryPanel.this.service.getVersion().equals("1.0.0")) {
                    if (formato.equals(MapImageFormatChooser.IMAGE_FORMATS[0][0]) || formato.equals(MapImageFormatChooser.IMAGE_FORMATS[0][1])) {
                        EditWMSQueryPanel.this.transpCB.setEnabled(true);
                        EditWMSQueryPanel.this.transpCB.setSelected(true);
                    } else {
                        EditWMSQueryPanel.this.transpCB.setSelected(false);
                        EditWMSQueryPanel.this.transpCB.setEnabled(false);
                    }
                } else if (EditWMSQueryPanel.this.service.getVersion().equals("1.1.0") || EditWMSQueryPanel.this.service.getVersion().equals("1.1.1")) {
                    if (formato.equals(MapImageFormatChooser.IMAGE_FORMATS[1][0]) || formato.equals(MapImageFormatChooser.IMAGE_FORMATS[1][1])) {
                        EditWMSQueryPanel.this.transpCB.setEnabled(true);
                        EditWMSQueryPanel.this.transpCB.setSelected(true);
                    } else {
                        EditWMSQueryPanel.this.transpCB.setSelected(false);
                        EditWMSQueryPanel.this.transpCB.setEnabled(false);
                    }
                }
            }
        });
        this.formatComboBox.setMinimumSize(new Dimension(300, 21));
        this.srsLabel = new JLabel(String.valueOf(I18N.getString("ui.plugin.wms.EditWMSQueryPanel.coordinate-reference-system")) + ":");
        this.srsComboBox.setMinimumSize(new Dimension(300, 21));
        this.srsComboBox.setToolTipText("");
        this.srsComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                CrsAxisOrder axisOrder = EditWMSQueryPanel.this.getAxisOrderForSelectedSRS();
                EditWMSQueryPanel.this.setAxisOrder(axisOrder);
            }
        });
        this.transparencyLabel = new JLabel(String.valueOf(I18N.getString("ui.plugin.wms.EditWMSQueryPanel.transparency")) + ":");
        this.transparencyLevelLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.transparency-level")) + " (" + I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.in-client") + ")" + ":");
        this.transparencyLevelPanel = new TransparencyPanel();
        this.urlLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.URL")) + ":");
        this.urlTextArea = new JTextArea();
        this.urlTextArea.setColumns(50);
        this.urlTextArea.setRows(1);
        this.urlTextArea.setLineWrap(true);
        this.urlTextArea.setFont(this.urlLabel.getFont());
        this.urlTextArea.setEditable(false);
        this.urlTextArea.setBackground(this.urlLabel.getBackground());
        JScrollPane areaScrollPane = new JScrollPane(this.urlTextArea);
        areaScrollPane.setVerticalScrollBarPolicy(22);
        this.timeLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.time")) + ":");
        this.timeText = new JTextField();
        this.timeText.setInputVerifier(new DateTextFieldValidator(this.dialog, (JComponent)this.timeText, "yyyy-MM-dd"));
        this.informationFormatsLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.information-format")) + ":");
        this.informationFormatsComboBox = new JComboBox();
        this.infoFeatureCountLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel.Feature-count")) + ":");
        this.infoFeatureCountNumberSpinner = new NumberSpinner(1, 1, 9999, 1);
        this.exceptionFormatsLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.exceptions-format")) + ":");
        this.exceptionFormatsComboBox = new JComboBox();
        this.useDeclaredCapabilitiesURLLabel = new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.use-declared-capabilities-urls")) + ":");
        this.useDeclaredCapabilitiesURLCheckBox = new JCheckBox();
        this.vendorParametersCheckBox = new JCheckBox(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.Vendor-parameters")) + ":");
        this.vendorParametersCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                EditWMSQueryPanel.this.vendorParametersTextField.setEnabled(EditWMSQueryPanel.this.vendorParametersCheckBox.isSelected());
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
                if (EditWMSQueryPanel.this.forceAxisOrderToLongLatCheckBox.isSelected()) {
                    EditWMSQueryPanel.this.setAxisOrder(CrsAxisOrder.EAST_NORTH);
                } else {
                    CrsAxisOrder axisOrder = EditWMSQueryPanel.this.getAxisOrderForSelectedSRS();
                    EditWMSQueryPanel.this.setAxisOrder(axisOrder);
                }
            }
        });
        int row = 0;
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.urlLabel, (JComponent)areaScrollPane, true);
        FormUtils.addRowInGBL(this, row++, 0, this.mapLayerPanel);
        FormUtils.addRowInGBL(this, row++, 0, FormUtils.getTitleLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.basics")));
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.srsLabel, (JComponent)this.srsComboBox, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.formatLabel, (JComponent)this.formatComboBox, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.transparencyLabel, (JComponent)this.transpCB);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.informationFormatsLabel, (JComponent)this.informationFormatsComboBox, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.infoFeatureCountLabel, (JComponent)this.infoFeatureCountNumberSpinner, false);
        FormUtils.addRowInGBL(this, row++, 0, FormUtils.getTitleLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel.advaced")));
        if (this.service.getVersion().startsWith("1.3")) {
            FormUtils.addRowInGBL((JComponent)this, row++, 0, this.axisOrderLabel, (JComponent)this.crsAxisOrderTextField);
            FormUtils.addRowInGBL((JComponent)this, row++, 0, (JComponent)this.forceAxisOrderToLongLatCheckBox, true, true);
        }
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.timeLabel, (JComponent)this.timeText, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.transparencyLevelLabel, (JComponent)this.transparencyLevelPanel, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.exceptionFormatsLabel, (JComponent)this.exceptionFormatsComboBox, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.useDeclaredCapabilitiesURLLabel, (JComponent)this.useDeclaredCapabilitiesURLCheckBox, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.vendorParametersCheckBox, (JComponent)this.vendorParametersTextField);
        FormUtils.addFiller(this, row++, 0);
    }

    public List<MapLayer> getChosenMapLayers() {
        return this.mapLayerPanel.getChosenMapLayers();
    }

    public EnableCheck[] getEnableChecks() {
        return this.enableChecks;
    }

    private void setTransparent(boolean transp) {
        this.service.setTransparent(transp);
    }

    public boolean isTransparent() {
        return this.transpCB.isSelected();
    }

    public int getAlpha() {
        return 255 - this.transparencyLevelPanel.getSlider().getValue();
    }

    public String getInformationFormat() {
        return (String)this.informationFormatsComboBox.getSelectedItem();
    }

    public String getExceptionFormat() {
        return (String)this.exceptionFormatsComboBox.getSelectedItem();
    }

    public boolean getUseDeclaredCapabilitiesURLs() {
        return this.useDeclaredCapabilitiesURLCheckBox.isEnabled() && this.useDeclaredCapabilitiesURLCheckBox.isSelected();
    }

    public String getVendorParameters() {
        return this.vendorParametersCheckBox.isSelected() ? this.vendorParametersTextField.getText().trim() : null;
    }

    protected void setAxisOrder(CrsAxisOrder axisOrder) {
        this.crsAxisOrderTextField.setText(GUITranslationsUtils.getAxisOrderDescription(axisOrder));
    }

    protected CrsAxisOrder getAxisOrderForSelectedSRS() {
        CrsAxisOrder axisOrder = CrsAxisOrder.EAST_NORTH;
        if (!this.forceAxisOrderToLongLatCheckBox.isSelected()) {
            try {
                if (this.srsComboBox.getSelectedItem() != null) {
                    axisOrder = CrsRepositoryManager.getInstance().getAxisOrder(this.srsNameToCodeMap.get(this.srsComboBox.getSelectedItem()));
                }
            }
            catch (Exception e1) {
                LOGGER.error((Object)e1);
            }
        }
        return axisOrder;
    }

    public CrsAxisOrder getAxisOrder() {
        CrsAxisOrder axisOrder = CrsAxisOrder.EAST_NORTH;
        if (!this.forceAxisOrderToLongLatCheckBox.isSelected()) {
            axisOrder = this.getAxisOrderForSelectedSRS();
        }
        return axisOrder;
    }

    public int getInformationFeatureCount() {
        return this.infoFeatureCountNumberSpinner.getIntValue();
    }
}

