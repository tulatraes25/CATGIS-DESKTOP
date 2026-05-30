/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.config.ConfigPlugIn;
import org.saig.jump.widgets.config.HTTPProxySettings;
import org.saig.jump.widgets.config.ProxyAuth;
import org.saig.jump.widgets.util.AbstractWaitDialog;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.validating.GreaterOrEqualThanTextFieldValidator;

public class ConfigNetworkPropertiesPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ConfigNetworkPropertiesPanel.class);
    public static final Icon ICON = IconLoader.icon("preferences-system-network-proxy.png");
    public static final String HTTP_PROXY_SETTINGS_KEY = String.valueOf(ConfigNetworkPropertiesPanel.class.getName()) + " - HTTP PROXY USER KEY";
    public static final Icon SUCCESSFULL_CONNECTION_ICON = IconLoader.icon("internet_connection_ok.png");
    public static final Icon FAILED_CONNECTION_ICON = IconLoader.icon("internet_connection_failed.png");
    public static final String SUCCESSFULL_CONNECTION_LABEL = I18N.getString(ConfigNetworkPropertiesPanel.class, "connected");
    public static final String FAILED_CONNECTION_LABEL = I18N.getString(ConfigNetworkPropertiesPanel.class, "not-connected");
    public static final String PRESS_TO_CHECK_CONNECTION_LABEL = I18N.getString(ConfigNetworkPropertiesPanel.class, "press-the-button-to-check-the-internet-connection-using-provided-parameters");
    public static final String NAME = I18N.getString(ConfigNetworkPropertiesPanel.class, "network-properties");
    private JPanel testConnectionPanel;
    private JButton testConnectionButton;
    private JLabel connectionResultsLabel;
    private JPanel proxySettingsPanel;
    private JCheckBox proxyHTTPEnabledCheckBox;
    private JTextField proxyHostTextField;
    private JTextField proxyPortTextField;
    private JTextField proxyUserTextField;
    private JPasswordField proxyPasswordTextField;
    private JTextField directConnectToTextField;
    protected Blackboard blackboard;

    public ConfigNetworkPropertiesPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getProxySettingsPanel());
        FormUtils.addRowInGBL(this, 1, 0, this.getTestConnectionPanel());
        FormUtils.addFiller(this, 2, 0);
    }

    private JPanel getTestConnectionPanel() {
        if (this.testConnectionPanel == null) {
            this.testConnectionPanel = new JPanel(new GridBagLayout());
            this.testConnectionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "connection-status")));
            JPanel buttonPanel = new JPanel(new FlowLayout());
            this.testConnectionButton = new JButton(I18N.getString(this.getClass(), "check-internet-connection"));
            this.testConnectionButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    String errorMessage = ConfigNetworkPropertiesPanel.this.validateInput();
                    if (StringUtils.isNotEmpty((String)errorMessage)) {
                        DialogFactory.showErrorDialog(ConfigPlugIn.getDialog(), errorMessage, I18N.getString("org.saig.jump.widgets.config.ConfigDialog.Configuration-error"));
                        return;
                    }
                    new AbstractWaitDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(this.getClass(), "checking-internet-connection")){

                        @Override
                        protected void methodToPerform() {
                            try {
                                if (ConfigNetworkPropertiesPanel.this.isConnected()) {
                                    ConfigNetworkPropertiesPanel.this.connectionResultsLabel.setText(SUCCESSFULL_CONNECTION_LABEL);
                                    ConfigNetworkPropertiesPanel.this.connectionResultsLabel.setIcon(SUCCESSFULL_CONNECTION_ICON);
                                } else {
                                    ConfigNetworkPropertiesPanel.this.connectionResultsLabel.setText(FAILED_CONNECTION_LABEL);
                                    ConfigNetworkPropertiesPanel.this.connectionResultsLabel.setIcon(FAILED_CONNECTION_ICON);
                                }
                            }
                            catch (Exception e) {
                                LOGGER.error((Object)"", (Throwable)e);
                            }
                        }
                    }.setVisible(true);
                }
            });
            JPanel connectionResultsPanel = new JPanel(new FlowLayout());
            this.connectionResultsLabel = new JLabel();
            this.connectionResultsLabel.setAlignmentX(0.5f);
            this.connectionResultsLabel.setAlignmentY(0.5f);
            buttonPanel.add(this.testConnectionButton);
            connectionResultsPanel.add(this.connectionResultsLabel);
            FormUtils.addRowInGBL(this.testConnectionPanel, 0, 0, connectionResultsPanel);
            FormUtils.addRowInGBL(this.testConnectionPanel, 1, 0, buttonPanel);
        }
        return this.testConnectionPanel;
    }

    private JPanel getProxySettingsPanel() {
        if (this.proxySettingsPanel == null) {
            this.proxySettingsPanel = new JPanel(new GridBagLayout());
            this.proxySettingsPanel.setBorder(BorderFactory.createTitledBorder(String.valueOf(I18N.getString(this.getClass(), "firewall")) + " / " + I18N.getString(this.getClass(), "proxy")));
            this.proxyHTTPEnabledCheckBox = new JCheckBox(I18N.getString(this.getClass(), "enable-proxy-connection-through-http"));
            this.proxyHTTPEnabledCheckBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfigNetworkPropertiesPanel.this.refreshEditability();
                }
            });
            JLabel proxyHostLabel = new JLabel(I18N.getString(this.getClass(), "proxy-server"));
            this.proxyHostTextField = new JTextField();
            JLabel proxyPortLabel = new JLabel(I18N.getString(this.getClass(), "proxy-port"));
            this.proxyPortTextField = new JTextField();
            this.proxyPortTextField.setInputVerifier(new GreaterOrEqualThanTextFieldValidator((JDialog)ConfigPlugIn.getDialog(), (JComponent)this.proxyPortTextField, 0.0));
            JLabel proxyUserLabel = new JLabel(I18N.getString(this.getClass(), "user"));
            this.proxyUserTextField = new JTextField();
            JLabel proxyPasswordLabel = new JLabel(I18N.getString(this.getClass(), "password"));
            this.proxyPasswordTextField = new JPasswordField();
            JLabel directConnectToLabel = new JLabel(I18N.getString(this.getClass(), "direct-connection"));
            this.directConnectToTextField = new JTextField();
            FormUtils.addRowInGBL(this.proxySettingsPanel, 0, 0, this.proxyHTTPEnabledCheckBox);
            FormUtils.addRowInGBL((JComponent)this.proxySettingsPanel, 1, 0, proxyHostLabel, (JComponent)this.proxyHostTextField);
            FormUtils.addRowInGBL((JComponent)this.proxySettingsPanel, 2, 0, proxyPortLabel, (JComponent)this.proxyPortTextField);
            FormUtils.addRowInGBL((JComponent)this.proxySettingsPanel, 3, 0, proxyUserLabel, (JComponent)this.proxyUserTextField);
            FormUtils.addRowInGBL((JComponent)this.proxySettingsPanel, 4, 0, proxyPasswordLabel, (JComponent)this.proxyPasswordTextField);
            FormUtils.addRowInGBL((JComponent)this.proxySettingsPanel, 5, 0, directConnectToLabel, (JComponent)this.directConnectToTextField);
        }
        return this.proxySettingsPanel;
    }

    protected void refreshEditability() {
        boolean isHTTPProxyEnabled = this.proxyHTTPEnabledCheckBox.isSelected();
        this.proxyHostTextField.setEnabled(isHTTPProxyEnabled);
        this.proxyPortTextField.setEnabled(isHTTPProxyEnabled);
        this.proxyUserTextField.setEnabled(isHTTPProxyEnabled);
        this.proxyPasswordTextField.setEnabled(isHTTPProxyEnabled);
        this.directConnectToTextField.setEnabled(isHTTPProxyEnabled);
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
        this.connectionResultsLabel.setIcon(null);
        this.connectionResultsLabel.setText(PRESS_TO_CHECK_CONNECTION_LABEL);
        HTTPProxySettings settings = (HTTPProxySettings)PersistentBlackboardPlugIn.get(this.blackboard).get(HTTP_PROXY_SETTINGS_KEY);
        this.proxyHTTPEnabledCheckBox.setSelected(settings != null);
        if (settings != null) {
            this.proxyHostTextField.setText(settings.getHost());
            this.proxyPortTextField.setText("" + settings.getPort());
            this.proxyUserTextField.setText(settings.getUserName());
            this.proxyPasswordTextField.setText(settings.getPassword());
            this.directConnectToTextField.setText(settings.getDirectConnectionTo());
        }
        this.refreshEditability();
    }

    @Override
    public void okPressed() {
        HTTPProxySettings settings = this.buildSettingsFromUserParameters();
        this.stablishCurrentNetworkSettings(settings);
        PersistentBlackboardPlugIn.get(this.blackboard).put(HTTP_PROXY_SETTINGS_KEY, settings);
    }

    private void stablishCurrentNetworkSettings(HTTPProxySettings settings) {
        Properties systemSettings = System.getProperties();
        if (settings != null) {
            systemSettings.put("http.proxySet", "true");
            systemSettings.put("http.proxyHost", settings.getHost());
            systemSettings.put("http.proxyPort", "" + settings.getPort());
            if (StringUtils.isNotEmpty((String)settings.getUserName())) {
                systemSettings.put("http.proxyUserName", settings.getUserName());
            }
            if (StringUtils.isNotEmpty((String)settings.getPassword())) {
                systemSettings.put("http.proxyPassword", settings.getPassword());
            }
            if (StringUtils.isNotEmpty((String)settings.getDirectConnectionTo())) {
                systemSettings.put("http.nonProxyHosts", settings.getDirectConnectionTo());
            }
            System.setProperties(systemSettings);
            if (settings.getUserName() != null) {
                Authenticator.setDefault(new ProxyAuth(settings.getUserName(), settings.getPassword()));
            } else {
                Authenticator.setDefault(new ProxyAuth("", ""));
            }
        } else {
            systemSettings.remove("http.proxySet");
            systemSettings.remove("http.proxyHost");
            systemSettings.remove("http.proxyPort");
            systemSettings.remove("http.proxyUserName");
            systemSettings.remove("http.proxyPassword");
            systemSettings.remove("http.nonProxyHosts");
        }
    }

    private HTTPProxySettings buildSettingsFromUserParameters() {
        HTTPProxySettings settings = null;
        if (this.proxyHTTPEnabledCheckBox.isSelected()) {
            settings = new HTTPProxySettings();
            settings.setHost(StringUtils.trim((String)this.proxyHostTextField.getText()));
            settings.setPort(Integer.valueOf(StringUtils.trim((String)this.proxyPortTextField.getText())));
            settings.setUserName(StringUtils.trim((String)this.proxyUserTextField.getText()));
            settings.setPassword(StringUtils.trim((String)new String(this.proxyPasswordTextField.getPassword())));
            settings.setDirectConnectionTo(StringUtils.trim((String)this.directConnectToTextField.getText()));
        }
        return settings;
    }

    @Override
    public String validateInput() {
        String errorMessage = null;
        if (this.proxyHTTPEnabledCheckBox.isSelected()) {
            String host = StringUtils.trim((String)this.proxyHostTextField.getText());
            String port = StringUtils.trim((String)this.proxyPortTextField.getText());
            if (!this.proxyPortTextField.getInputVerifier().verify(this.proxyPortTextField) || StringUtils.isEmpty((String)host) || StringUtils.isEmpty((String)port) || !StringUtil.isNumber(port)) {
                errorMessage = I18N.getString(this.getClass(), "server-or-proxy-port-is-not-correct-check-provided-parameters");
            } else {
                try {
                    StringBuffer strUrl = new StringBuffer();
                    strUrl.append(host.startsWith("http://") ? host.toLowerCase() : "http://" + host.toLowerCase());
                    strUrl.append(StringUtils.isNotEmpty((String)port) ? ":" + port : "");
                    new URL(strUrl.toString());
                }
                catch (MalformedURLException e) {
                    LOGGER.error((Object)e);
                    errorMessage = I18N.getString(this.getClass(), "server-or-proxy-port-is-not-correct-check-provided-parameters");
                }
            }
        }
        return errorMessage;
    }

    private boolean isConnected() {
        Properties systemProperties = System.getProperties();
        Object proxySet = systemProperties.get("http.proxySet");
        Object proxyHost = systemProperties.get("http.proxyHost");
        Object proxyPort = systemProperties.get("http.proxyPort");
        Object proxyUserName = systemProperties.get("http.proxyUserName");
        Object proxyPassword = systemProperties.get("http.proxyPassword");
        Object nonProxyHosts = systemProperties.get("http.nonProxyHosts");
        try {
            HTTPProxySettings settings = this.buildSettingsFromUserParameters();
            this.stablishCurrentNetworkSettings(settings);
            URL url = new URL("http://www.google.com");
            url.openConnection();
            url.openStream();
            return true;
        }
        catch (Exception e) {
            return false;
        }
        finally {
            if (proxySet != null) {
                systemProperties.put("http.proxySet", proxySet);
            } else {
                systemProperties.remove("http.proxySet");
            }
            if (proxyHost != null) {
                systemProperties.put("http.proxyHost", proxyHost);
            } else {
                systemProperties.remove("http.proxyHost");
            }
            if (proxyPort != null) {
                systemProperties.put("http.proxyPort", proxyPort);
            } else {
                systemProperties.remove("http.proxyPort");
            }
            if (proxyUserName != null) {
                systemProperties.put("http.proxyUserName", proxyUserName);
            } else {
                systemProperties.remove("http.proxyUserName");
            }
            if (proxyPassword != null) {
                systemProperties.put("http.proxyPassword", proxyPassword);
            } else {
                systemProperties.remove("http.proxyPassword");
            }
            if (nonProxyHosts != null) {
                systemProperties.put("http.nonProxyHosts", nonProxyHosts);
            } else {
                systemProperties.remove("http.nonProxyHosts");
            }
        }
    }
}

