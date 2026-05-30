/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import de.latlon.deejump.wfs.client.WFServiceWrapper_1_0_0;
import de.latlon.deejump.wfs.client.WFServiceWrapper_1_1_0;
import es.kosmo.core.model.sdi.BasicAuthentificationData;
import es.kosmo.desktop.widgets.sdi.AbstractURLWizardPanel;
import es.kosmo.desktop.widgets.sdi.ServerInfo;
import es.kosmo.desktop.widgets.sdi.wfs.WFSFeatureTypeSelectionWizardPanel;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Map;
import java.util.Vector;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class WFSURLWizardPanel
extends AbstractURLWizardPanel {
    private static final long serialVersionUID = 1L;
    public static final String WFS_SERVICE_KEY = "WFS_SERVICE";
    protected AbstractWFSWrapper wfsService;
    private JPanel versionSelectionPanel;
    protected static final String WFS_SERVERS_XML_FILES_BASE_PATH = "resources/sdi/wfs/";

    @Override
    public String getID() {
        return this.getClass().getName();
    }

    @Override
    public String getNextID() {
        return WFSFeatureTypeSelectionWizardPanel.class.getName();
    }

    @Override
    public String getInstructions() {
        return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSURLWizardPanel.insert-the-WFS-server-URL");
    }

    @Override
    protected void addServerToList() {
        this.connectToServer();
        if (this.wfsService != null && this.wfsService.isInitialized()) {
            String serverName = this.wfsService.getServerTitle();
            String serverAbstract = this.wfsService.getServerAbstract();
            if (StringUtils.isEmpty((String)serverName)) {
                serverName = this.wfsService.getBaseWfsURL();
            }
            if (StringUtils.isEmpty((String)serverAbstract)) {
                serverAbstract = NO_DESCRIPTION_ASSOCIATED;
            }
            this.serverDescriptionTextArea.setText(serverAbstract);
            this.serverDescriptionTextArea.setCaretPosition(0);
            if (this.serverByName.containsKey(serverName)) {
                ServerInfo info = (ServerInfo)this.serverByName.get(serverName);
                if (!info.getUrl().equals(this.wfsService.getServerTitle()) || !info.getVersion().equals(this.wfsService.getServiceVersion())) {
                    this.addServ(this.wfsService, true);
                }
            } else {
                this.addServ(this.wfsService, false);
            }
            this.inputChangedFirer.fire();
        }
    }

    private void addServ(AbstractWFSWrapper service, boolean changeName) {
        String serverName = this.wfsService.getServerTitle();
        String serverAbstract = this.wfsService.getServerAbstract();
        if (StringUtils.isEmpty((String)serverName)) {
            serverName = this.wfsService.getBaseWfsURL();
        }
        if (StringUtils.isEmpty((String)serverAbstract)) {
            serverAbstract = NO_DESCRIPTION_ASSOCIATED;
        }
        ServerInfo nuevoServ = new ServerInfo(serverName, service.getBaseWfsURL(), serverAbstract, service.getServiceVersion(), false);
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
    protected void connectToServer() {
        if (this.selectedServerName != null || !StringUtils.isEmpty((String)this.urlTextField.getText())) {
            String url = this.fixUrlForService(this.urlTextField.getText().trim());
            try {
                try {
                    this.basicAuthData = StringUtils.isNotEmpty((String)this.userTextField.getText()) ? new BasicAuthentificationData(this.userTextField.getText().trim(), new String(this.passwordTextField.getPassword())) : null;
                    this.wfsService = "1.1.0".equals(this.serviceVersion) ? new WFServiceWrapper_1_1_0(this.basicAuthData, url) : new WFServiceWrapper_1_0_0(this.basicAuthData, url);
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
                                            progressDialog.report(String.valueOf(I18N.getMessage("org.saig.jump.widgets.sdi.wfs.WFSURLWizardPanel.Connecting-to-the-WFS-server-{0}", new Object[]{(this).WFSURLWizardPanel.this.wfsService.getBaseWfsURL()})) + "...");
                                            (this).WFSURLWizardPanel.this.wfsService.initialize();
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
                    if (!this.wfsService.isInitialized()) {
                        DialogFactory.showErrorDialog(this, String.valueOf(I18N.getMessage("org.saig.jump.widgets.sdi.wfs.WFSURLWizardPanel.an-error-has-been-produced-while-connecting-with-the-server-{0}", new Object[]{this.selectedServerName})) + ": " + progressDialog.getExceptionMessage(), I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSURLWizardPanel.error-while-connecting-with-the-WFS-server"));
                        this.wfsService = null;
                        this.isConnected = false;
                    } else {
                        this.connectedToLabel.setText(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Connected-to-{0}", new Object[]{this.wfsService.getBaseWfsURL()}));
                        String serverAbstract = this.wfsService.getServerAbstract();
                        if (StringUtils.isEmpty((String)serverAbstract)) {
                            serverAbstract = NO_DESCRIPTION_ASSOCIATED;
                        }
                        this.serverDescriptionTextArea.setText(serverAbstract);
                        this.serverDescriptionTextArea.setCaretPosition(0);
                        this.isConnected = true;
                    }
                }
                catch (Exception e) {
                    DialogFactory.showErrorDialog(this, String.valueOf(I18N.getMessage("org.saig.jump.widgets.sdi.wfs.WFSURLWizardPanel.an-error-has-been-produced-while-connecting-with-the-server-{0}", new Object[]{this.selectedServerName})) + ": " + e.getMessage(), I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSURLWizardPanel.error-while-connecting-with-the-WFS-server"));
                    this.wfsService = null;
                    this.isConnected = false;
                    this.refreshGUI();
                    return;
                }
            }
            finally {
                this.refreshGUI();
            }
            this.inputChangedFirer.fire();
        }
    }

    @Override
    public String getXMLFilesBaseDirectoryPath() {
        return WFS_SERVERS_XML_FILES_BASE_PATH;
    }

    @Override
    public boolean isInputValid() {
        return this.wfsService != null && this.wfsService.isInitialized() && this.wfsService.getFeatureTypes() != null;
    }

    @Override
    public boolean isPanelOk() {
        return this.wfsService != null && this.wfsService.isInitialized() && this.wfsService.getFeatureTypes() != null;
    }

    @Override
    public void enteredFromLeft(Map<String, Object> dataMap) {
        super.enteredFromLeft(dataMap);
        this.wfsService = null;
        this.isConnected = false;
        this.addRemovePanel.getLeftList().setSelectedItems(new Vector());
        this.addRemovePanel.getRightList().setSelectedItems(new Vector());
        this.urlTextField.setText("");
        this.serverDescriptionTextArea.setText("");
        this.refreshGUI();
    }

    @Override
    public void exitingToRight() throws Exception {
        super.exitingToRight();
        this.dataMap.put(WFS_SERVICE_KEY, this.wfsService);
        this.dataMap.put("SERVER_VERSION", this.serviceVersion);
    }

    @Override
    public JPanel getVersionSelectionPanel() {
        if (this.versionSelectionPanel == null) {
            this.versionSelectionPanel = this.createVersionButtons(new String[]{"1.0.0", "1.1.0"});
        }
        return this.versionSelectionPanel;
    }

    @Override
    public String getTitle() {
        return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSURLWizardPanel.select-the-WFS-server-URL");
    }

    @Override
    protected String getServiceCapabilities() {
        if (this.wfsService != null) {
            return this.wfsService.getCapabilitesAsString();
        }
        return null;
    }

    @Override
    protected void disconnectFromCurrentService() {
        super.disconnectFromCurrentService();
        this.wfsService = null;
    }
}

