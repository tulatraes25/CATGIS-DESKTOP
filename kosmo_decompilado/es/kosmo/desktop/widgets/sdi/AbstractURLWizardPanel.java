/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.deegree.framework.xml.XMLFragment
 *  org.dom4j.Document
 *  org.dom4j.DocumentException
 *  org.dom4j.Element
 *  org.dom4j.io.OutputFormat
 *  org.dom4j.io.SAXReader
 *  org.dom4j.io.XMLWriter
 */
package es.kosmo.desktop.widgets.sdi;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.addremove.ButtonCustomAddRemovePanel;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveList;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.wizard.AbstractWizardPanel;
import es.kosmo.core.model.sdi.BasicAuthentificationData;
import es.kosmo.desktop.images.DesktopIconLoader;
import es.kosmo.desktop.plugins.sdi.ISDIService;
import es.kosmo.desktop.widgets.sdi.SDIServiceXMLPersistence;
import es.kosmo.desktop.widgets.sdi.ServerInfo;
import es.kosmo.desktop.widgets.sdi.ShowServerCapabilitiesDialog;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.deegree.framework.xml.XMLFragment;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.xml.sax.SAXException;

public abstract class AbstractURLWizardPanel
extends AbstractWizardPanel {
    private static final long serialVersionUID = 1L;
    public static final Logger LOGGER = Logger.getLogger(AbstractURLWizardPanel.class);
    public static final String NO_DESCRIPTION_ASSOCIATED = I18N.getString("org.saig.jump.widgets.sdi.AbstractURLWizardPanel.no-description-associated");
    public static final String URL_KEY = "SERVER_URL";
    public static final String VERSION_KEY = "SERVER_VERSION";
    public static final String SERVICE_KEY = "SERVICE";
    public static final String FORMATS_KEY = "FORMATS";
    protected JTextField urlTextField = new JTextField();
    protected Map<String, ServerInfo> serverByName = new TreeMap<String, ServerInfo>();
    protected JTextArea serverDescriptionTextArea = new JTextArea();
    private JButton connectButton;
    private JButton addButton;
    private JButton deleteButton;
    private JButton capabilitiesButton;
    protected ISDIService service;
    protected ButtonCustomAddRemovePanel<String> addRemovePanel = new ButtonCustomAddRemovePanel(true);
    protected DefaultAddRemoveList<String> availableServersList;
    protected DefaultListModel availableServersListModel;
    protected DefaultAddRemoveList<String> favouriteServersList;
    protected DefaultListModel favouriteServersListModel;
    protected String selectedServerName;
    protected JLabel connectedToLabel;
    protected String serviceVersion;
    protected ButtonGroup versionsButtonGroup;
    public static final Icon ADD_SERVER_ICON = IconLoader.icon("add.png");
    public static final Icon REMOVE_SERVER_ICON = IconLoader.icon("delete_small.gif");
    public static final Icon CONNECT_TO_SERVER_ICON = IconLoader.icon("world_go.png");
    public static final Icon SERVER_CAPABILITIES_ICON = IconLoader.icon("page_white_text.png");
    protected boolean isConnected = false;
    protected JPanel serverXMLFileSelectionPanel;
    protected JComboBox serverXMLFileSelectionCombobox;
    protected JPanel basicAuthentificationPanel;
    protected JTextField userTextField;
    protected JPasswordField passwordTextField;
    protected BasicAuthentificationData basicAuthData;

    public AbstractURLWizardPanel() {
        try {
            this.initialize();
            this.initializeServerXMLFiles();
            this.xmlToServerList((File)this.serverXMLFileSelectionCombobox.getSelectedItem());
            this.refreshGUI();
        }
        catch (Exception ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
    }

    private void initializeServerXMLFiles() {
        File baseDir = new File(this.getXMLFilesBaseDirectoryPath());
        if (baseDir.exists() && baseDir.canRead()) {
            File[] serverXMLFiles = baseDir.listFiles(new FileFilter(){

                @Override
                public boolean accept(File pathname) {
                    return Pattern.matches(".*\\.(xml)", pathname.getAbsolutePath());
                }
            });
            DefaultComboBoxModel<File> comboBoxModel = new DefaultComboBoxModel<File>();
            File[] fileArray = serverXMLFiles;
            int n = serverXMLFiles.length;
            int n2 = 0;
            while (n2 < n) {
                File xmlFile = fileArray[n2];
                comboBoxModel.addElement(xmlFile);
                ++n2;
            }
            this.serverXMLFileSelectionCombobox.setModel(comboBoxModel);
        }
    }

    private void xmlToServerList(File xmlFile) {
        try {
            FileInputStream is = new FileInputStream(xmlFile);
            Iterator<ServerInfo> it = SDIServiceXMLPersistence.readServers(is).iterator();
            this.serverByName.clear();
            while (it.hasNext()) {
                ServerInfo serv = it.next();
                this.serverByName.put(serv.getName(), serv);
            }
            ((InputStream)is).close();
            this.availableServersListModel.clear();
            this.favouriteServersListModel.clear();
            for (String serverName : this.serverByName.keySet()) {
                ServerInfo serverInfo = this.serverByName.get(serverName);
                if (serverInfo.isFavourite()) {
                    this.favouriteServersListModel.addElement(serverInfo.getName());
                    continue;
                }
                this.availableServersListModel.addElement(serverInfo.getName());
            }
            this.addRemovePanel.repaint();
        }
        catch (FileNotFoundException e) {
            LOGGER.error((Object)I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.The-file-{0}-that-contains-the-server-list-can-not-be-found", new Object[]{(File)this.serverXMLFileSelectionCombobox.getSelectedItem()}));
        }
        catch (IOException e) {
            LOGGER.error((Object)I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.An-error-has-been-produced-while-closing-the-file-{0}", new Object[]{(File)this.serverXMLFileSelectionCombobox.getSelectedItem()}));
        }
    }

    public abstract String getXMLFilesBaseDirectoryPath();

    private void initialize() throws Exception {
        this.setLayout(new GridBagLayout());
        this.availableServersListModel = new DefaultListModel();
        this.favouriteServersListModel = new DefaultListModel();
        this.availableServersList = new DefaultAddRemoveList(this.availableServersListModel);
        this.favouriteServersList = new DefaultAddRemoveList(this.favouriteServersListModel);
        this.addRemovePanel.setLeftList(this.availableServersList);
        this.addRemovePanel.setRightList(this.favouriteServersList);
        this.addRemovePanel.setLeftLabel(new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Available-servers")));
        this.addRemovePanel.setRightLabel(new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Favourite-servers")));
        this.availableServersList.getList().setSelectedValue(null, false);
        this.favouriteServersList.getList().setSelectedValue(null, false);
        this.availableServersList.add(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                List<String> selectedServers = AbstractURLWizardPanel.this.availableServersList.getSelectedItems();
                if (!selectedServers.isEmpty()) {
                    AbstractURLWizardPanel.this.selectedServerName = selectedServers.get(0);
                    if (AbstractURLWizardPanel.this.serverByName.containsKey(AbstractURLWizardPanel.this.selectedServerName)) {
                        ServerInfo info = AbstractURLWizardPanel.this.serverByName.get(AbstractURLWizardPanel.this.selectedServerName);
                        AbstractURLWizardPanel.this.urlTextField.setText(info.getUrl());
                        AbstractURLWizardPanel.this.urlTextField.setCaretPosition(0);
                        AbstractURLWizardPanel.this.serverDescriptionTextArea.setText(info.getDescription());
                        AbstractURLWizardPanel.this.serverDescriptionTextArea.setCaretPosition(0);
                        if (AbstractURLWizardPanel.this.getVersionSelectionPanel() != null) {
                            AbstractURLWizardPanel.this.setSelectedVersion(info.getVersion());
                        }
                        AbstractURLWizardPanel.this.disconnectFromCurrentService();
                    }
                }
                AbstractURLWizardPanel.this.addRemovePanel.getRightList().setSelectedItems(new Vector());
                AbstractURLWizardPanel.this.refreshGUI();
            }
        });
        this.favouriteServersList.add(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                List<String> selectedServers = AbstractURLWizardPanel.this.favouriteServersList.getSelectedItems();
                if (!selectedServers.isEmpty()) {
                    AbstractURLWizardPanel.this.selectedServerName = selectedServers.get(0);
                    if (AbstractURLWizardPanel.this.serverByName.containsKey(AbstractURLWizardPanel.this.selectedServerName)) {
                        ServerInfo info = AbstractURLWizardPanel.this.serverByName.get(AbstractURLWizardPanel.this.selectedServerName);
                        AbstractURLWizardPanel.this.urlTextField.setText(info.getUrl());
                        AbstractURLWizardPanel.this.urlTextField.setCaretPosition(0);
                        AbstractURLWizardPanel.this.serverDescriptionTextArea.setText(info.getDescription());
                        AbstractURLWizardPanel.this.serverDescriptionTextArea.setCaretPosition(0);
                        if (AbstractURLWizardPanel.this.getVersionSelectionPanel() != null) {
                            AbstractURLWizardPanel.this.setSelectedVersion(info.getVersion());
                        }
                        AbstractURLWizardPanel.this.disconnectFromCurrentService();
                    }
                }
                AbstractURLWizardPanel.this.addRemovePanel.getLeftList().setSelectedItems(new Vector());
                AbstractURLWizardPanel.this.refreshGUI();
            }
        });
        this.addRemovePanel.getAllToRightButton().removeActionListener(this.addRemovePanel.getAllToRightButton().getActionListeners()[0]);
        this.addRemovePanel.getToRightButton().removeActionListener(this.addRemovePanel.getToRightButton().getActionListeners()[0]);
        this.addRemovePanel.getAllToLeftButton().removeActionListener(this.addRemovePanel.getAllToLeftButton().getActionListeners()[0]);
        this.addRemovePanel.getToLeftButton().removeActionListener(this.addRemovePanel.getToLeftButton().getActionListeners()[0]);
        this.addRemovePanel.getToRightButton().addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> selectedServers = AbstractURLWizardPanel.this.availableServersList.getSelectedItems();
                if (!selectedServers.isEmpty()) {
                    int i = 0;
                    while (i < selectedServers.size()) {
                        if (AbstractURLWizardPanel.this.serverByName.containsKey(selectedServers.get(i))) {
                            ServerInfo info = AbstractURLWizardPanel.this.serverByName.get(selectedServers.get(i));
                            info.setFavourite(true);
                        }
                        ++i;
                    }
                    AbstractURLWizardPanel.this.addRemovePanel.addButton_actionPerformed(e);
                    AbstractURLWizardPanel.this.serverListToXML((File)AbstractURLWizardPanel.this.serverXMLFileSelectionCombobox.getSelectedItem());
                }
            }
        });
        this.addRemovePanel.getToLeftButton().addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> selectedServers = AbstractURLWizardPanel.this.favouriteServersList.getSelectedItems();
                if (!selectedServers.isEmpty()) {
                    int i = 0;
                    while (i < selectedServers.size()) {
                        if (AbstractURLWizardPanel.this.serverByName.containsKey(selectedServers.get(i))) {
                            ServerInfo info = AbstractURLWizardPanel.this.serverByName.get(selectedServers.get(i));
                            info.setFavourite(false);
                        }
                        ++i;
                    }
                    AbstractURLWizardPanel.this.addRemovePanel.removeButton_actionPerformed(e);
                    AbstractURLWizardPanel.this.serverListToXML((File)AbstractURLWizardPanel.this.serverXMLFileSelectionCombobox.getSelectedItem());
                }
            }
        });
        this.addRemovePanel.getAllToRightButton().addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                List selectedServers = AbstractURLWizardPanel.this.addRemovePanel.getLeftItems();
                if (!selectedServers.isEmpty()) {
                    int i = 0;
                    while (i < selectedServers.size()) {
                        if (AbstractURLWizardPanel.this.serverByName.containsKey(selectedServers.get(i))) {
                            ServerInfo info = AbstractURLWizardPanel.this.serverByName.get(selectedServers.get(i));
                            info.setFavourite(true);
                        }
                        ++i;
                    }
                    AbstractURLWizardPanel.this.addRemovePanel.addAllButton_actionPerformed(e);
                    AbstractURLWizardPanel.this.serverListToXML((File)AbstractURLWizardPanel.this.serverXMLFileSelectionCombobox.getSelectedItem());
                }
            }
        });
        this.addRemovePanel.getAllToLeftButton().addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                List selectedServers = AbstractURLWizardPanel.this.addRemovePanel.getRightItems();
                if (!selectedServers.isEmpty()) {
                    int i = 0;
                    while (i < selectedServers.size()) {
                        if (AbstractURLWizardPanel.this.serverByName.containsKey(selectedServers.get(i))) {
                            ServerInfo info = AbstractURLWizardPanel.this.serverByName.get(selectedServers.get(i));
                            info.setFavourite(false);
                        }
                        ++i;
                    }
                    AbstractURLWizardPanel.this.addRemovePanel.removeAllButton_actionPerformed(e);
                    AbstractURLWizardPanel.this.serverListToXML((File)AbstractURLWizardPanel.this.serverXMLFileSelectionCombobox.getSelectedItem());
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(this.serverDescriptionTextArea, 22, 31);
        this.serverDescriptionTextArea.setEditable(false);
        this.serverDescriptionTextArea.setFont(new JLabel().getFont());
        this.serverDescriptionTextArea.setLineWrap(true);
        this.serverDescriptionTextArea.setWrapStyleWord(true);
        this.serverDescriptionTextArea.setColumns(80);
        this.serverDescriptionTextArea.setRows(3);
        scrollPane.setMinimumSize(new Dimension(80, 100));
        this.connectButton = new JButton(I18N.getString("ui.plugin.wms.URLWizardPanel.connectButton"));
        this.connectButton.setToolTipText(I18N.getString("org.saig.jump.widgets.sdi.AbstractURLWizardPanel.Connect-with-the-selected-server"));
        this.connectButton.setIcon(CONNECT_TO_SERVER_ICON);
        this.connectButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractURLWizardPanel.this.connectToServer();
            }
        });
        this.capabilitiesButton = new JButton(I18N.getString("org.saig.jump.widgets.sdi.AbstractURLWizardPanel.capabilities"));
        this.capabilitiesButton.setToolTipText(I18N.getString("org.saig.jump.widgets.sdi.AbstractURLWizardPanel.shows-the-xml-capabilities-file-of-server"));
        this.capabilitiesButton.setIcon(SERVER_CAPABILITIES_ICON);
        this.capabilitiesButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractURLWizardPanel.this.showServerCapabilities();
            }
        });
        this.addButton = new JButton(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Add"));
        this.addButton.setToolTipText(I18N.getString("org.saig.jump.widgets.sdi.AbstractURLWizardPanel.Try-to-connect-to-the-server-that-it-is-in-the-textfield-Direction-and-if-it-success-adds-it-to-the-list"));
        this.addButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractURLWizardPanel.this.addServerToList();
            }
        });
        this.deleteButton = new JButton(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Remove"));
        this.deleteButton.setToolTipText(I18N.getString("org.saig.jump.widgets.sdi.AbstractURLWizardPanel.Removes-the-selected-server-from-the-list-if-a-connection-was-stablished-it-will-be-interrupted"));
        this.deleteButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractURLWizardPanel.this.removeServerFromList();
            }
        });
        this.connectedToLabel = new JLabel();
        this.urlTextField.setPreferredSize(new Dimension(325, 20));
        this.urlTextField.setMinimumSize(new Dimension(325, 20));
        this.urlTextField.setEditable(true);
        JPanel botonera = new JPanel(new FlowLayout());
        this.addButton.setIcon(ADD_SERVER_ICON);
        this.deleteButton.setIcon(REMOVE_SERVER_ICON);
        this.capabilitiesButton.setIcon(SERVER_CAPABILITIES_ICON);
        int maxHeight = -1;
        int maxWidth = -1;
        maxHeight = Math.max(this.addButton.getPreferredSize().height, maxHeight);
        maxHeight = Math.max(this.deleteButton.getPreferredSize().height, maxHeight);
        maxHeight = Math.max(this.capabilitiesButton.getPreferredSize().height, maxHeight);
        maxWidth = Math.max(this.addButton.getPreferredSize().width, maxWidth);
        maxWidth = Math.max(this.deleteButton.getPreferredSize().width, maxWidth);
        maxWidth = Math.max(this.capabilitiesButton.getPreferredSize().width, maxWidth);
        Dimension maxDimension = new Dimension(maxWidth, maxHeight);
        this.capabilitiesButton.setPreferredSize(new Dimension(maxDimension));
        this.addButton.setPreferredSize(new Dimension(maxDimension));
        this.deleteButton.setPreferredSize(new Dimension(maxDimension));
        botonera.setMinimumSize(new Dimension(150, maxHeight + 10));
        botonera.setPreferredSize(new Dimension(150, maxHeight + 10));
        botonera.add(this.capabilitiesButton);
        botonera.add(this.addButton);
        botonera.add(this.deleteButton);
        this.basicAuthentificationPanel = new JPanel(new FlowLayout());
        JLabel userLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialog.User")) + ":");
        this.userTextField = new JTextField();
        this.userTextField.setPreferredSize(new Dimension(150, 20));
        this.userTextField.setMinimumSize(new Dimension(150, 20));
        JLabel passwordLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.gui.dialogs.LoginDialog.Password")) + ":");
        this.passwordTextField = new JPasswordField();
        this.passwordTextField.setPreferredSize(new Dimension(150, 20));
        this.passwordTextField.setMinimumSize(new Dimension(150, 20));
        FormUtils.addRowInGBL((JComponent)this.basicAuthentificationPanel, 0, 0, userLabel, (JComponent)this.userTextField, false);
        FormUtils.addRowInGBL((JComponent)this.basicAuthentificationPanel, 0, 30, passwordLabel, (JComponent)this.passwordTextField, false);
        int row = 0;
        FormUtils.addRowInGBL((JComponent)this, row++, 0, (JComponent)this.getServerXMLFileSelectionPanel(), true, false);
        FormUtils.addRowInGBL(this, row++, 0, this.addRemovePanel);
        FormUtils.addRowInGBL((JComponent)this, row, 0, new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.URL")) + ": "), (JComponent)this.urlTextField);
        FormUtils.addRowInGBL(this, row++, 40, this.connectButton);
        if (this.getVersionSelectionPanel() != null) {
            FormUtils.addRowInGBL((JComponent)this, row++, 0, (JComponent)this.getVersionSelectionPanel(), true, false);
        }
        FormUtils.addRowInGBL(this, row++, 0, this.basicAuthentificationPanel);
        FormUtils.addRowInGBL(this, row++, 0, this.connectedToLabel);
        FormUtils.addRowInGBL(this, row++, 0, scrollPane);
        FormUtils.addRowInGBL(this, row++, 0, botonera);
        FormUtils.addFiller(this, row++, 0);
    }

    @Override
    public abstract String getInstructions();

    @Override
    public void exitingToRight() throws Exception {
        this.dataMap.put(URL_KEY, this.selectedServerName);
        this.dataMap.put(SERVICE_KEY, this.service);
    }

    @Override
    public void enteredFromLeft(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public abstract String getTitle();

    @Override
    public abstract String getID();

    @Override
    public abstract String getNextID();

    @Override
    public abstract boolean isInputValid();

    @Override
    public abstract boolean isPanelOk();

    public abstract JPanel getVersionSelectionPanel();

    protected abstract void connectToServer();

    protected abstract void addServerToList();

    protected abstract String getServiceCapabilities();

    protected void showServerCapabilities() {
        String capabilities = this.getServiceCapabilities();
        XMLFragment doc = new XMLFragment();
        try {
            doc.load((Reader)new StringReader(capabilities), "http://www.systemid.org");
            capabilities = doc.getAsPrettyString();
        }
        catch (SAXException sAXException) {
        }
        catch (IOException iOException) {
            // empty catch block
        }
        ShowServerCapabilitiesDialog dialog = new ShowServerCapabilitiesDialog((JDialog)this.getRootPane().getParent(), true, String.valueOf(I18N.getString("org.saig.jump.widgets.sdi.AbstractURLWizardPanel.server-capabilities")) + " - " + this.urlTextField.getText().trim());
        GUIUtil.centre(dialog, this.getRootPane().getParent());
        dialog.setCapabilities(capabilities);
        dialog.setVisible(true);
    }

    protected void removeServerFromList() {
        block18: {
            try {
                if (this.selectedServerName == null) break block18;
                if (this.serverByName.containsKey(this.selectedServerName)) {
                    ServerInfo info;
                    this.serverByName.remove(this.selectedServerName);
                    try {
                        SAXReader xmlReader = new SAXReader();
                        File file = (File)this.serverXMLFileSelectionCombobox.getSelectedItem();
                        Document doc = xmlReader.read(file);
                        Element root = doc.getRootElement();
                        List list = root.elements();
                        int i = 0;
                        while (i < list.size()) {
                            Element el = (Element)list.get(i);
                            String nombre = el.element("name").getText();
                            if (nombre.equals(this.selectedServerName)) {
                                root.remove(el);
                                break;
                            }
                            ++i;
                        }
                        FileOutputStream out = new FileOutputStream((File)this.serverXMLFileSelectionCombobox.getSelectedItem());
                        OutputFormat format = OutputFormat.createPrettyPrint();
                        format.setEncoding("ISO-8859-1");
                        XMLWriter xmlWriter = new XMLWriter((OutputStream)out, format);
                        xmlWriter.write(doc);
                        xmlWriter.flush();
                        out.close();
                        xmlWriter.close();
                    }
                    catch (DocumentException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.An-error-has-been-produced-while-reading-the-XML-file-{0}", new Object[]{(File)this.serverXMLFileSelectionCombobox.getSelectedItem()}), I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Error"));
                    }
                    catch (IOException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.The-file-{0}-could-not-be-processed", new Object[]{(File)this.serverXMLFileSelectionCombobox.getSelectedItem()}), I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Error"));
                    }
                    if (this.addRemovePanel.getLeftItems().contains(this.selectedServerName)) {
                        this.addRemovePanel.getLeftList().getModel().remove(this.selectedServerName);
                        if (this.service != null) {
                            this.service = null;
                            this.isConnected = false;
                        }
                        if (this.addRemovePanel.getLeftList().getModel().getItems().size() > 0) {
                            ((DefaultAddRemoveList)this.addRemovePanel.getLeftList()).getList().setSelectedIndex(0);
                            this.selectedServerName = (String)((DefaultAddRemoveList)this.addRemovePanel.getLeftList()).getList().getSelectedValue();
                            info = this.serverByName.get(this.selectedServerName);
                            this.serverDescriptionTextArea.setText(info.getDescription());
                            this.serverDescriptionTextArea.setCaretPosition(0);
                            this.urlTextField.setText(info.getUrl());
                            this.urlTextField.setCaretPosition(0);
                        } else {
                            this.serverDescriptionTextArea.setText("");
                            this.urlTextField.setText("");
                            this.selectedServerName = "";
                        }
                    } else if (this.addRemovePanel.getRightList().getModel().getItems().contains(this.selectedServerName)) {
                        this.addRemovePanel.getRightList().getModel().remove(this.selectedServerName);
                        if (this.service != null) {
                            this.service = null;
                            this.isConnected = false;
                        }
                        if (this.addRemovePanel.getRightList().getModel().getItems().size() > 0) {
                            ((DefaultAddRemoveList)this.addRemovePanel.getRightList()).getList().setSelectedIndex(0);
                            this.selectedServerName = (String)((DefaultAddRemoveList)this.addRemovePanel.getRightList()).getList().getSelectedValue();
                            info = this.serverByName.get(this.selectedServerName);
                            this.serverDescriptionTextArea.setText(info.getDescription());
                            this.urlTextField.setText(info.getUrl());
                        } else {
                            this.serverDescriptionTextArea.setText("");
                            this.urlTextField.setText("");
                            this.selectedServerName = "";
                        }
                    }
                }
                this.inputChangedFirer.fire();
            }
            finally {
                this.refreshGUI();
            }
        }
    }

    public void serverListToXML(File xmlFile) {
        try {
            SDIServiceXMLPersistence.writeServers(this.serverByName.values(), xmlFile);
        }
        catch (Exception e) {
            LOGGER.error((Object)e);
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.The-file-{0}-could-not-be-processed", new Object[]{xmlFile}), I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel.Error"));
        }
    }

    protected String fixUrlForService(String url) {
        String fixedURL = url.trim();
        if (fixedURL.indexOf("?") == -1) {
            fixedURL = String.valueOf(fixedURL) + "?";
        } else if (!fixedURL.endsWith("?") && !fixedURL.endsWith("&")) {
            fixedURL = String.valueOf(fixedURL) + "&";
        }
        return fixedURL;
    }

    protected JPanel createVersionButtons(String[] versions) {
        JPanel p = new JPanel();
        p.add(new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.sdi.AbstractURLWizardPanel.version")) + ":"));
        this.versionsButtonGroup = new ButtonGroup();
        int i = 0;
        while (i < versions.length) {
            final JRadioButton b = new JRadioButton(versions[i]);
            b.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractURLWizardPanel.this.serviceVersion = b.getText();
                }
            });
            this.versionsButtonGroup.add(b);
            if (i == 0) {
                b.doClick();
            }
            p.add(b);
            ++i;
        }
        return p;
    }

    protected void setSelectedVersion(String version) {
        if (this.versionsButtonGroup != null) {
            Enumeration<AbstractButton> buttons = this.versionsButtonGroup.getElements();
            if (StringUtils.isEmpty((String)version)) {
                buttons.nextElement().doClick();
            } else {
                boolean enc = false;
                while (buttons.hasMoreElements() && !enc) {
                    AbstractButton currentButton = buttons.nextElement();
                    if (!currentButton.getText().equals(version)) continue;
                    currentButton.doClick();
                    enc = true;
                }
            }
        }
    }

    protected void refreshGUI() {
        this.capabilitiesButton.setEnabled(this.isConnected);
        this.addButton.setEnabled(!StringUtils.isEmpty((String)this.urlTextField.getText()));
        this.deleteButton.setEnabled(this.availableServersList.getSelectedItems().size() > 0 || this.favouriteServersList.getSelectedItems().size() > 0);
        if (!this.isConnected) {
            this.connectedToLabel.setText(I18N.getString("org.saig.jump.widgets.sdi.AbstractURLWizardPanel.You-are-not-connected-to-any-server"));
        }
        this.inputChangedFirer.fire();
    }

    protected void disconnectFromCurrentService() {
        this.isConnected = false;
    }

    public JPanel getServerXMLFileSelectionPanel() {
        if (this.serverXMLFileSelectionPanel == null) {
            this.serverXMLFileSelectionPanel = new JPanel(new GridBagLayout());
            JLabel serverXMLFileSelectionLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.sdi.AbstractURLWizardPanel.Servers-file")) + ":");
            this.serverXMLFileSelectionCombobox = new JComboBox();
            this.serverXMLFileSelectionCombobox.setRenderer(new DefaultListCellRenderer(){
                private static final long serialVersionUID = 1L;

                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
                    File f = (File)value;
                    this.setText(String.valueOf(f.getName()) + " - (" + f.getPath() + ")");
                    this.setIcon(DesktopIconLoader.icon("html.png"));
                    this.setToolTipText(f.getAbsolutePath());
                    return this;
                }
            });
            this.serverXMLFileSelectionCombobox.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent event) {
                    if (event.getStateChange() == 1) {
                        AbstractURLWizardPanel.this.xmlToServerList((File)event.getItem());
                    } else if (event.getStateChange() == 2) {
                        AbstractURLWizardPanel.this.serverListToXML((File)event.getItem());
                    }
                }
            });
            FormUtils.addRowInGBL((JComponent)this.serverXMLFileSelectionPanel, 0, 0, serverXMLFileSelectionLabel, (JComponent)this.serverXMLFileSelectionCombobox, true);
        }
        return this.serverXMLFileSelectionPanel;
    }
}

