/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderDialog;
import org.saig.jump.lang.I18N;

public class TableFilterPanel
extends JPanel {
    private JButton filterButton = new JButton(I18N.getString("org.saig.core.model.data.widgets.tables.management.TableFilterPanel.change"));
    private JButton deleteFilterButton;
    private JLabel sqlFilterLabel;
    private JCheckBox enabledFilterCheckBox;
    private SQLFilterBuilderDialog filterBuilderDialog;

    public TableFilterPanel(ActionListener filterButtonListener, ActionListener deleteFilterActionListener, ActionListener enabledFilterActionListener, SQLFilterBuilderDialog filterBuilderDialog) {
        super(new BorderLayout());
        this.filterButton.addActionListener(filterButtonListener);
        this.deleteFilterButton = new JButton(IconLoader.icon("delete_small.gif"));
        this.deleteFilterButton.addActionListener(deleteFilterActionListener);
        this.enabledFilterCheckBox = new JCheckBox();
        this.enabledFilterCheckBox.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.tables.management.TableFilterPanel.activate-desactivate-filter"));
        this.enabledFilterCheckBox.addActionListener(enabledFilterActionListener);
        this.filterBuilderDialog = filterBuilderDialog;
        this.add((Component)this.getLeftPanel(), "West");
        this.add((Component)this.getRightPanel(), "East");
    }

    private JToolBar getLeftPanel() {
        JToolBar leftPanel = new JToolBar();
        leftPanel.setFloatable(false);
        leftPanel.add(this.deleteFilterButton);
        leftPanel.add(this.enabledFilterCheckBox);
        this.sqlFilterLabel = new JLabel();
        Dimension labelDimension = new Dimension(400, 25);
        this.sqlFilterLabel.setPreferredSize(labelDimension);
        this.sqlFilterLabel.setMinimumSize(labelDimension);
        this.sqlFilterLabel.setMaximumSize(labelDimension);
        leftPanel.add(this.sqlFilterLabel);
        return leftPanel;
    }

    private JPanel getRightPanel() {
        JPanel rightPanel = new JPanel(new FlowLayout(2));
        rightPanel.add(this.filterButton);
        return rightPanel;
    }

    public void refresh() {
        if (this.filterBuilderDialog != null && this.filterBuilderDialog.getFilter() != null) {
            this.sqlFilterLabel.setText(this.filterBuilderDialog.getFilter().toString());
        } else {
            this.sqlFilterLabel.setText("");
        }
    }

    public void enabledFilter(boolean enabled) {
        this.enabledFilterCheckBox.setSelected(enabled);
    }

    public boolean isEnabledFilter() {
        return this.enabledFilterCheckBox.isSelected();
    }
}

