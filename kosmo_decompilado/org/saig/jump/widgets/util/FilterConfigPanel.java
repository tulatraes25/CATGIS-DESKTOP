/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.saig.core.filter.Filter;
import org.saig.core.filter.visitor.FilterToStringTranslator;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFilterEditor;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.query.LayerQueryWizardDialog;

public class FilterConfigPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTextField filterStringTextField;
    private JScrollPane filterScrollPane;
    private JTextArea filterTextArea;
    private JButton filterWizardButton;
    private JButton removeFilterButton;
    private static FilterToStringTranslator translator = new FilterToStringTranslator();
    private Filter currentFilter;
    private String associatedLayerName;
    private boolean editable;
    private boolean hasTextArea;
    protected InputChangedFirer inputChangedFirer = new InputChangedFirer();

    public FilterConfigPanel() {
        this((String)null, true);
    }

    public FilterConfigPanel(String layerName) {
        this(layerName, true);
    }

    public FilterConfigPanel(String layerName, boolean isEditable) {
        this(layerName, isEditable, null, false);
    }

    public FilterConfigPanel(String layerName, boolean isEditable, Filter layerFilter, boolean hasTextArea) {
        this.setLayout(new BorderLayout());
        this.associatedLayerName = layerName;
        this.editable = isEditable;
        this.hasTextArea = hasTextArea;
        JPanel buttonPanel = new JPanel(new FlowLayout(0, 0, 0));
        Dimension dim = new Dimension(20, 20);
        if (hasTextArea) {
            this.filterScrollPane = new JScrollPane(22, 31);
            this.filterScrollPane.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.util.FilterConfigPanel.Filter")));
            this.filterTextArea = new JTextArea();
            this.filterTextArea.setLineWrap(true);
            this.filterTextArea.setWrapStyleWord(true);
            this.filterTextArea.setColumns(30);
            this.filterTextArea.setRows(5);
            this.filterTextArea.setEnabled(false);
            JLabel label = new JLabel();
            this.filterTextArea.setFont(label.getFont());
            this.filterTextArea.revalidate();
            this.filterScrollPane.setViewportView(this.filterTextArea);
        } else {
            this.filterStringTextField = new JTextField();
            this.filterStringTextField.setEditable(false);
        }
        this.filterWizardButton = new JButton(GUIUtil.toSmallIcon(DefaultFilterEditor.WIZARD_ICON));
        this.filterWizardButton.setMaximumSize(dim);
        this.filterWizardButton.setPreferredSize(dim);
        this.filterWizardButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FilterConfigPanel.this.openFilterDialog();
            }
        });
        this.removeFilterButton = new JButton(GUIUtil.toSmallIcon(DeleteSelectedItemsPlugIn.ICON));
        this.removeFilterButton.setMaximumSize(dim);
        this.removeFilterButton.setPreferredSize(dim);
        this.removeFilterButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FilterConfigPanel.this.removeFilter();
            }
        });
        buttonPanel.add(this.filterWizardButton);
        buttonPanel.add(this.removeFilterButton);
        if (hasTextArea) {
            this.add((Component)this.filterScrollPane, "Center");
        } else {
            this.add((Component)this.filterStringTextField, "Center");
        }
        this.add((Component)buttonPanel, "East");
        this.setFilter(layerFilter);
    }

    private void openFilterDialog() {
        LayerQueryWizardDialog dialog = null;
        Layer layer = JUMPWorkbench.getLayer(this.associatedLayerName);
        if (layer != null) {
            dialog = new LayerQueryWizardDialog(JUMPWorkbench.getFrameInstance(), true, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext(), layer);
            dialog.setFilter(layer.getLayerFilter());
        } else {
            dialog = new LayerQueryWizardDialog(JUMPWorkbench.getFrameInstance(), true, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
        }
        dialog.setVisible(true);
        if (dialog.exitOk() && dialog.getFilter() != null) {
            this.setFilter(dialog.getFilter());
            this.inputChangedFirer.fire();
        }
    }

    private void removeFilter() {
        this.setFilter(null);
        this.inputChangedFirer.fire();
    }

    public Filter getFilter() {
        return this.currentFilter;
    }

    public void setFilter(Filter filter) {
        boolean hasFilter;
        this.currentFilter = filter;
        this.updateToolTip();
        boolean bl = hasFilter = this.currentFilter != null;
        if (this.hasTextArea) {
            this.filterTextArea.setEditable(this.editable);
        } else {
            this.filterStringTextField.setEnabled(this.editable);
        }
        this.filterWizardButton.setEnabled(this.editable);
        this.removeFilterButton.setEnabled(this.editable && hasFilter);
    }

    private void updateToolTip() {
        String tip = "<HTML><B>";
        tip = this.associatedLayerName == null ? String.valueOf(tip) + I18N.getString(this.getClass(), "filter") + ":</B> " : String.valueOf(tip) + I18N.getString(this.getClass(), "layer-filter") + this.associatedLayerName + ":</B> ";
        if (this.currentFilter == null) {
            if (this.hasTextArea) {
                this.filterTextArea.setText("");
            } else {
                this.filterStringTextField.setText("");
            }
            tip = String.valueOf(tip) + I18N.getString(this.getClass(), "no-assigned-filter");
        } else {
            String filterText = translator.translateFilter(this.currentFilter);
            if (this.hasTextArea) {
                this.filterTextArea.setText(filterText);
            } else {
                this.filterStringTextField.setText(filterText);
            }
            tip = String.valueOf(tip) + filterText;
        }
        if (this.hasTextArea) {
            this.filterTextArea.setToolTipText(tip);
        } else {
            this.filterStringTextField.setToolTipText(tip);
        }
    }

    public String getAssociatedLayerName() {
        return this.associatedLayerName;
    }

    public void setAssociatedLayerName(String layerName) {
        this.associatedLayerName = layerName;
        this.updateToolTip();
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        this.setFilter(this.currentFilter);
    }

    public void addChangeListener(InputChangedListener listener) {
        this.inputChangedFirer.add(listener);
    }

    public void removeChangeListener(InputChangedListener listener) {
        this.inputChangedFirer.remove(listener);
    }
}

