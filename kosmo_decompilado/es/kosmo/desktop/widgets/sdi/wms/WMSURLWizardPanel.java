/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.desktop.widgets.sdi.wms;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerWizardPanel;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import com.vividsolutions.wms.WMService;
import es.kosmo.core.model.sdi.BasicAuthentificationData;
import es.kosmo.desktop.widgets.sdi.AbstractURLWizardPanel;
import es.kosmo.desktop.widgets.sdi.ServerInfo;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class WMSURLWizardPanel
extends AbstractURLWizardPanel {
    private static final long serialVersionUID = 1L;
    public static final String WMS_SERVICE_KEY = "WMS_SERVICE";
    public static final String NO_VERSION_SELECTED = I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Unspecified");
    protected WMService wmsService;
    private JPanel versionSelectionPanel;
    protected static final String WMS_SERVERS_XML_FILES_BASE_PATH = "resources/sdi/wms/";

    @Override
    public String getTitle() {
        return I18N.getString("ui.plugin.wms.URLWizardPanel.select-uniform-resource-locator-url");
    }

    @Override
    public String getID() {
        return this.getClass().getName();
    }

    @Override
    public String getNextID() {
        return MapLayerWizardPanel.class.getName();
    }

    @Override
    public String getInstructions() {
        return I18N.getString("ui.plugin.wms.URLWizardPanel.please-enter-the-url-of-the-wms-server");
    }

    @Override
    public boolean isInputValid() {
        return this.wmsService != null && this.wmsService.getCapabilities() != null;
    }

    @Override
    public boolean isPanelOk() {
        return this.wmsService != null && this.wmsService.getCapabilities() != null;
    }

    @Override
    protected void addServerToList() {
        this.connectToServer();
        if (this.wmsService != null && this.wmsService.isInitialized()) {
            String serverName = this.wmsService.getTitle();
            String serverAbstract = this.wmsService.getCapabilities().getDescription();
            if (StringUtils.isEmpty((String)serverAbstract)) {
                serverAbstract = AbstractURLWizardPanel.NO_DESCRIPTION_ASSOCIATED;
            }
            this.serverDescriptionTextArea.setText(serverAbstract);
            this.serverDescriptionTextArea.setCaretPosition(0);
            if (this.serverByName.containsKey(serverName)) {
                ServerInfo info = (ServerInfo)this.serverByName.get(serverName);
                if (!info.getUrl().equals(this.wmsService.getServerUrl()) || !info.getVersion().equals(this.wmsService.getVersion())) {
                    this.addServ(this.wmsService, true);
                }
            } else {
                this.addServ(this.wmsService, false);
            }
            this.inputChangedFirer.fire();
        }
    }

    @Override
    protected void connectToServer() {
        if (this.selectedServerName != null || !this.urlTextField.getText().equals("")) {
            try {
                String url = this.fixUrlForService(this.urlTextField.getText().trim());
                this.basicAuthData = StringUtils.isNotEmpty((String)this.userTextField.getText()) ? new BasicAuthentificationData(this.userTextField.getText().trim(), new String(this.passwordTextField.getPassword())) : null;
                this.wmsService = new WMService(url);
                this.wmsService.setBasicAuthData(this.basicAuthData);
                if (!NO_VERSION_SELECTED.equals(this.serviceVersion)) {
                    this.wmsService.setWmsVersion(this.serviceVersion);
                }
                final TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
                progressDialog.setTitle(I18N.getString("ui.plugin.wms.URLWizardPanel.connecting"));
                progressDialog.addComponentListener(new ComponentAdapter(){

                    @Override
                    public void componentShown(ComponentEvent e) {
                        new Thread(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    try {
                                        progressDialog.report(String.valueOf(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Connecting-to-the-WMS-server-{0}", new Object[]{(this).WMSURLWizardPanel.this.wmsService.getServerUrl()})) + "...");
                                        (this).WMSURLWizardPanel.this.wmsService.initialize();
                                        WMSURLWizardPanel.this.selectVersionButton((this).WMSURLWizardPanel.this.wmsService.getVersion());
                                    }
                                    catch (Exception e) {
                                        LOGGER.error((Object)"", (Throwable)e);
                                        progressDialog.setExceptionMessage(e.getMessage());
                                        progressDialog.setVisible(false);
                                        return;
                                    }
                                }
                                finally {
                                    progressDialog.setVisible(false);
                                }
                            }
                        }).start();
                    }
                });
                GUIUtil.centre(progressDialog, this.getRootPane().getParent());
                progressDialog.setVisible(true);
                if (!this.wmsService.isInitialized()) {
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("ui.plugin.wms.URLWizardPanel.connectingError")) + " " + this.wmsService.getServerUrl() + ": \n" + progressDialog.getExceptionMessage(), I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Error"));
                    this.connectedToLabel.setText(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.You-are-not-connected-to-any-WMS-server"));
                    return;
                }
                try {
                    this.connectedToLabel.setText(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Connected-to-{0}", new Object[]{this.wmsService.getCapabilities().getTitle()}));
                    this.wmsService.setTitle(this.wmsService.getCapabilities().getTitle());
                    String serverAbstract = this.wmsService.getCapabilities().getDescription();
                    if (StringUtils.isEmpty((String)serverAbstract)) {
                        serverAbstract = NO_DESCRIPTION_ASSOCIATED;
                    }
                    this.serverDescriptionTextArea.setText(serverAbstract);
                    this.serverDescriptionTextArea.setCaretPosition(0);
                    this.isConnected = true;
                }
                catch (Exception e) {
                    DialogFactory.showErrorDialog(this, String.valueOf(I18N.getMessage("org.saig.jump.widgets.sdi.wfs.WFSURLWizardPanel.an-error-has-been-produced-while-connecting-with-the-server-{0}", new Object[]{this.selectedServerName})) + ": " + e.getMessage(), I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSURLWizardPanel.error-while-connecting-with-the-WFS-server"));
                    this.wmsService = null;
                    this.isConnected = false;
                    return;
                }
            }
            finally {
                this.refreshGUI();
            }
            this.inputChangedFirer.fire();
        }
    }

    private void selectVersionButton(String version) {
        if (!version.equals(this.serviceVersion)) {
            Enumeration<AbstractButton> buttons = this.versionsButtonGroup.getElements();
            boolean found = false;
            while (buttons.hasMoreElements() && !found) {
                AbstractButton currentButton = buttons.nextElement();
                if (!currentButton.getText().equals(version)) continue;
                currentButton.doClick();
                found = true;
            }
        }
    }

    private void addServ(WMService service, boolean changeName) {
        String serverAbstract = service.getCapabilities().getDescription();
        if (StringUtils.isEmpty((String)serverAbstract)) {
            serverAbstract = NO_DESCRIPTION_ASSOCIATED;
        }
        ServerInfo nuevoServ = new ServerInfo(service.getTitle(), service.getServerUrl(), serverAbstract, service.getVersion(), false);
        if (changeName) {
            String nombreBase = String.valueOf(nuevoServ.getName()) + "(";
            String nombreServ = nuevoServ.getName();
            int cont = 1;
            while (this.serverByName.containsKey(nombreServ)) {
                nombreServ = String.valueOf(nombreBase) + cont++ + ")";
            }
            nuevoServ.setName(nombreServ);
        }
        this.serverByName.put(nuevoServ.getName(), nuevoServ);
        this.availableServersList.getModel().add(nuevoServ.getName());
        this.availableServersList.getList().setSelectedIndex(this.availableServersList.getList().getModel().getSize() - 1);
        this.selectedServerName = nuevoServ.getName();
        this.urlTextField.setText(nuevoServ.getUrl());
        this.serverDescriptionTextArea.setText(nuevoServ.getDescription());
        this.serverListToXML((File)this.serverXMLFileSelectionCombobox.getSelectedItem());
    }

    @Override
    public String getXMLFilesBaseDirectoryPath() {
        return WMS_SERVERS_XML_FILES_BASE_PATH;
    }

    @Override
    protected String getServiceCapabilities() {
        if (this.wmsService != null) {
            return this.wmsService.getCapabilities().getCapabilitiesAsString();
        }
        return null;
    }

    @Override
    public void enteredFromLeft(Map<String, Object> dataMap) {
        super.enteredFromLeft(dataMap);
        this.wmsService = null;
        this.isConnected = false;
        this.addRemovePanel.getLeftList().setSelectedItems(new Vector());
        this.addRemovePanel.getRightList().setSelectedItems(new Vector());
        this.urlTextField.setText("");
        this.serverDescriptionTextArea.setText("");
        this.refreshGUI();
    }

    @Override
    public void exitingToRight() throws Exception {
        this.dataMap.put("SERVER_URL", this.selectedServerName);
        this.dataMap.put("SERVICE", this.wmsService);
        String[] formatos = this.wmsService.getCapabilities().getMapFormats();
        this.dataMap.put("FORMATS", formatos);
        this.dataMap.put("INITIAL_LAYER_NAMES", null);
        this.dataMap.put("SERVER_VERSION", this.serviceVersion);
    }

    @Override
    protected void disconnectFromCurrentService() {
        super.disconnectFromCurrentService();
        this.wmsService = null;
    }

    @Override
    public JPanel getVersionSelectionPanel() {
        if (this.versionSelectionPanel == null) {
            this.versionSelectionPanel = this.createVersionButtons(new String[]{NO_VERSION_SELECTED, "1.0.0", "1.1.0", "1.1.1", "1.3.0"});
        }
        return this.versionSelectionPanel;
    }
}

