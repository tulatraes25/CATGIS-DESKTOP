/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.widgets.datasource;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import es.kosmo.desktop.widgets.datasource.ITableSelectionPanel;
import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.jump.lang.I18N;

public class LoadTableDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    public static final Logger LOGGER = Logger.getLogger(LoadTableDialog.class);
    protected JPanel formatSelectionPanel;
    protected JComboBox formatSelectionComboBox;
    protected JPanel cardFormatPanel;
    protected OKCancelPanel okCancelPanel;
    protected boolean cbbInitialized = true;
    protected Map<String, ITableSelectionPanel> datasourceIDToComponentMap = new HashMap<String, ITableSelectionPanel>();
    protected Map<String, String> descriptionToDatasourceIDMap = new HashMap<String, String>();

    public LoadTableDialog(JFrame parent, boolean modal) {
        super((Frame)parent, modal);
        this.setTitle(I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.load-tables"));
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getFormatSelectionPanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getCardFormatPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.getOkCancelPanel());
        this.pack();
    }

    protected JPanel getFormatSelectionPanel() {
        if (this.formatSelectionPanel == null) {
            this.formatSelectionPanel = new JPanel(new GridBagLayout());
            this.formatSelectionPanel.setBorder(BorderFactory.createEtchedBorder());
            JLabel formatSelectionLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.table-type")) + ":");
            this.formatSelectionComboBox = new JComboBox<Object>(this.descriptionToDatasourceIDMap.keySet().toArray());
            this.formatSelectionComboBox.addActionListener(this);
            FormUtils.addRowInGBL((JComponent)this.formatSelectionPanel, 0, 0, formatSelectionLabel, (JComponent)this.formatSelectionComboBox, false);
            FormUtils.addFiller(this.formatSelectionPanel, 1, 0);
        }
        return this.formatSelectionPanel;
    }

    protected JPanel getCardFormatPanel() {
        if (this.cardFormatPanel == null) {
            this.cardFormatPanel = new JPanel(new CardLayout());
        }
        return this.cardFormatPanel;
    }

    protected OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public void refresh() {
        this.refreshCardFormatPanel();
        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.formatSelectionComboBox) && this.cbbInitialized) {
            this.refreshCardFormatPanel();
        } else if (e.getSource().equals(this.okCancelPanel)) {
            if (this.okCancelPanel.wasOKPressed()) {
                if (this.isInputValid()) {
                    this.setVisible(false);
                }
            } else {
                this.setVisible(false);
            }
        }
    }

    protected void refreshCardFormatPanel() {
        String selectedFormatDescription = (String)this.formatSelectionComboBox.getSelectedItem();
        String datasourceID = this.descriptionToDatasourceIDMap.get(selectedFormatDescription);
        CardLayout cl = (CardLayout)this.cardFormatPanel.getLayout();
        cl.show(this.cardFormatPanel, datasourceID);
        this.getSelectedComponent().refresh();
    }

    protected boolean isInputValid() {
        ITableSelectionPanel guiComponent = this.getSelectedComponent();
        return guiComponent.isInputValid();
    }

    private ITableSelectionPanel getSelectedComponent() {
        String selectedFormatDescription = (String)this.formatSelectionComboBox.getSelectedItem();
        String datasourceID = this.descriptionToDatasourceIDMap.get(selectedFormatDescription);
        ITableSelectionPanel guiComponent = this.datasourceIDToComponentMap.get(datasourceID);
        return guiComponent;
    }

    public void registerTableSelectionPanel(ITableSelectionPanel panel) {
        Assert.isTrue((panel != null ? 1 : 0) != 0);
        Assert.isTrue((boolean)StringUtils.isNotEmpty((String)panel.getID()));
        Assert.isTrue((boolean)StringUtils.isNotEmpty((String)panel.getDescription()));
        if (!this.datasourceIDToComponentMap.containsKey(panel.getID())) {
            this.datasourceIDToComponentMap.put(panel.getID(), panel);
            this.descriptionToDatasourceIDMap.put(panel.getDescription(), panel.getID());
            this.cardFormatPanel.add(panel.getComponent(), panel.getID());
            try {
                this.cbbInitialized = false;
                this.formatSelectionComboBox.addItem(panel.getDescription());
            }
            finally {
                this.cbbInitialized = true;
            }
        }
    }

    public void unregisterTableSelectionPanel(ITableSelectionPanel panel) {
        Assert.isTrue((panel != null ? 1 : 0) != 0);
        Assert.isTrue((boolean)StringUtils.isNotEmpty((String)panel.getID()));
        Assert.isTrue((boolean)StringUtils.isNotEmpty((String)panel.getDescription()));
        if (this.datasourceIDToComponentMap.containsKey(panel.getID())) {
            ITableSelectionPanel selectionPanel = this.datasourceIDToComponentMap.get(panel.getID());
            this.cardFormatPanel.remove(selectionPanel.getComponent());
            this.descriptionToDatasourceIDMap.remove(panel.getDescription());
            this.formatSelectionComboBox.removeItem(selectionPanel.getDescription());
        }
    }

    public List<? extends TableRecordDataSource> getTableDataSources() throws Exception {
        return this.getSelectedComponent().getTableDataSources();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
            this.refreshCardFormatPanel();
        }
        super.setVisible(visible);
    }
}

