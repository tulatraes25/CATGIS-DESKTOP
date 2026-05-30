/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooser;
import com.vividsolutions.jump.workbench.datasource.FileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.datasource.JDBCPropertiesPanel;
import org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel;

public class DataSourceQueryChooserDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private CardLayout cardLayout = new CardLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private JPanel mainPanel = new JPanel(this.cardLayout);
    private JPanel formatPanel = new JPanel();
    private JComboBox formatComboBox = new JComboBox();
    private JLabel formatLabel = new JLabel(){
        private static final long serialVersionUID = 1L;
        {
            this.setDisplayedMnemonic('F');
            this.setLabelFor(DataSourceQueryChooserDialog.this.formatComboBox);
        }
    };
    private Map<Component, String> componentToNameMap = new HashMap<Component, String>();
    private OKCancelPanel okCancelPanel = new OKCancelPanel();

    public DataSourceQueryChooserDialog(Collection<DataSourceQueryChooser> dataSourceQueryChoosers, Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        this.init(dataSourceQueryChoosers);
        try {
            this.jbInit();
            this.pack();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                DataSourceQueryChooserDialog.this.okCancelPanel.setOKPressed(false);
            }
        });
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                DataSourceQueryChooserDialog.this.okCancelPanel.setOKPressed(false);
            }
        });
        this.formatComboBox.setSelectedItem(this.formatComboBox.getItemAt(0));
    }

    private void init(Collection<DataSourceQueryChooser> dataSourceQueryChoosers) {
        HashSet<Component> components = new HashSet<Component>();
        for (DataSourceQueryChooser chooser : dataSourceQueryChoosers) {
            this.formatComboBox.addItem(chooser);
            components.add(chooser.getComponent());
        }
        int j = 0;
        for (Component component : components) {
            this.componentToNameMap.put(component, "card" + ++j);
            this.mainPanel.add(component, this.name(component));
        }
    }

    private String name(Component component) {
        return this.componentToNameMap.get(component);
    }

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(this.borderLayout2);
        this.formatPanel.setLayout(new GridBagLayout());
        this.formatPanel.setBorder(BorderFactory.createTitledBorder(""));
        this.formatLabel.setText(String.valueOf(I18N.getString("workbench.datasource.DataSourceQueryChooserDialog.format")) + ": ");
        this.formatComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DataSourceQueryChooserDialog.this.formatComboBox_actionPerformed(e);
            }
        });
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DataSourceQueryChooserDialog.this.okCancelPanel_actionPerformed(e);
            }
        });
        this.getContentPane().add((Component)this.mainPanel, "Center");
        this.getContentPane().add((Component)this.formatPanel, "North");
        this.getContentPane().add((Component)this.okCancelPanel, "South");
        FormUtils.addRowInGBL((JComponent)this.formatPanel, 0, 0, this.formatLabel, (JComponent)this.formatComboBox, false);
        FormUtils.addFiller(this.formatPanel, 0, 30);
    }

    public boolean wasOKPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    void formatComboBox_actionPerformed(ActionEvent e) {
        Component component = this.getCurrentChooser().getComponent();
        if (component instanceof JDBCPropertiesPanel) {
            ((JDBCPropertiesPanel)component).refresh();
        } else if (component instanceof JDBCSavePropertiesPanel) {
            ((JDBCSavePropertiesPanel)component).refresh();
        } else if (component instanceof FileDataSourceQueryChooser.FileChooserPanel) {
            ((FileDataSourceQueryChooser.FileChooserPanel)component).getChooser().rescanCurrentDirectory();
        }
        this.cardLayout.show(this.mainPanel, this.name(this.getCurrentChooser().getComponent()));
    }

    public DataSourceQueryChooser getCurrentChooser() {
        return (DataSourceQueryChooser)this.formatComboBox.getSelectedItem();
    }

    void okCancelPanel_actionPerformed(ActionEvent e) {
        if (!this.okCancelPanel.wasOKPressed() || this.getCurrentChooser().isInputValid()) {
            this.setVisible(false);
        }
    }

    public String getSelectedFormat() {
        return this.formatComboBox.getSelectedItem().toString();
    }

    public void setSelectedFormat(String format) {
        int i = 0;
        while (i < this.formatComboBox.getItemCount()) {
            DataSourceQueryChooser chooser = (DataSourceQueryChooser)this.formatComboBox.getItemAt(i);
            if (chooser.toString().equals(format)) {
                this.formatComboBox.setSelectedIndex(i);
                return;
            }
            ++i;
        }
    }

    public void refreshPath() {
        if (this.getCurrentChooser() instanceof FileDataSourceQueryChooser) {
            FileDataSourceQueryChooser currentChooser = (FileDataSourceQueryChooser)this.getCurrentChooser();
            currentChooser.refreshPath();
        }
    }
}

