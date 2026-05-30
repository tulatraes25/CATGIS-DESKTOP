/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.widgets.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.widgets.datasource.ITableSelectionPanel;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.SelectFilePanel;

public abstract class AbstractFileBasedDBTableSelectionPanel
extends JPanel
implements ActionListener,
ITableSelectionPanel {
    private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(AbstractFileBasedDBTableSelectionPanel.class);
    protected SelectFilePanel filePanel;
    protected JButton connectButton;
    protected JPanel connectionPanel;
    protected JTextField userTextField;
    protected JPasswordField passwordTextField;
    protected JPanel tableSelectionPanel;
    protected TableRecordDataSource datasource;

    public AbstractFileBasedDBTableSelectionPanel() {
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getConnectionPanel());
        FormUtils.addRowInGBL(this, 1, 0, this.getTableSelectionPanel());
        FormUtils.addFiller(this, 2, 0);
    }

    protected JPanel getConnectionPanel() {
        if (this.connectionPanel == null) {
            this.connectionPanel = new JPanel(new GridBagLayout());
            this.connectionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.connection-properties")));
            JLabel fileSelectionLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.datasource.AbstractFileBasedDBTableSelectionPanel.File")) + ":");
            this.filePanel = new SelectFilePanel(this.getDescription(), this.getFileExtensions(), true);
            JLabel userLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.user")) + ":");
            this.userTextField = new JTextField();
            JLabel passwordLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.password")) + ":");
            this.passwordTextField = new JPasswordField();
            this.passwordTextField.setFont(this.userTextField.getFont());
            JPanel buttonPanel = new JPanel(new FlowLayout());
            this.connectButton = new JButton(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.connect"), IconLoader.icon("database_connect.png"));
            this.connectButton.addActionListener(this);
            buttonPanel.add(this.connectButton);
            FormUtils.addRowInGBL((JComponent)this.connectionPanel, 0, 0, fileSelectionLabel, (JComponent)this.filePanel);
            FormUtils.addRowInGBL((JComponent)this.connectionPanel, 1, 0, userLabel, (JComponent)this.userTextField);
            FormUtils.addRowInGBL((JComponent)this.connectionPanel, 2, 0, passwordLabel, (JComponent)this.passwordTextField);
            FormUtils.addRowInGBL(this.connectionPanel, 3, 0, buttonPanel);
            FormUtils.addFiller(this.connectionPanel, 10, 0);
        }
        return this.connectionPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.connectButton)) {
            String connectionStatus = this.checkConnection();
            if (StringUtils.isEmpty((String)connectionStatus)) {
                this.refreshTableSelectionPanel(true);
            } else {
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.the-connection-with-the-database-can-not-be-established")) + ".\n" + I18N.getString("org.saig.core.model.data.widgets.MDBDialog.please-you-should-revise-the-connection-parameters") + ": " + connectionStatus, I18N.getString("org.saig.core.model.data.widgets.MDBDialog.connection-error"));
                this.refreshTableSelectionPanel(false);
                return;
            }
        }
    }

    protected abstract void refreshTableSelectionPanel(boolean var1);

    protected abstract JPanel getTableSelectionPanel();

    protected abstract String checkConnection();

    protected abstract String[] getFileExtensions();
}

