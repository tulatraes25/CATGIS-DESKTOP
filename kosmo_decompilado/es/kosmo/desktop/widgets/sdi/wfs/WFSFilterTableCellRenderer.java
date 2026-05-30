/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn;
import es.kosmo.desktop.widgets.sdi.wfs.WFSFeatureTypeTableModel;
import es.kosmo.desktop.widgets.sdi.wfs.WFSFilterTableCellEditor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import org.saig.core.filter.Filter;
import org.saig.core.filter.visitor.FilterToStringTranslator;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFilterEditor;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;
import org.saig.jump.widgets.query.LayerQueryWizardDialog;

public class WFSFilterTableCellRenderer
extends JPanel
implements TableCellRenderer {
    private static final long serialVersionUID = 1L;
    private JTextField filterStringTextField;
    private JButton filterWizardButton;
    private JButton removeFilterButton;
    private static FilterToStringTranslator translator = new FilterToStringTranslator();
    private Filter currentFilter;
    private WFSFilterTableCellEditor editor;
    private WFSFeatureTypeInfo associatedFeatureTypeInfo;

    public WFSFilterTableCellRenderer(WFSFilterTableCellEditor filterTableCellEditor) {
        this();
        this.editor = filterTableCellEditor;
    }

    public WFSFilterTableCellRenderer() {
        this.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(0, 0, 0));
        Dimension dim = new Dimension(15, 15);
        this.filterStringTextField = new JTextField();
        this.filterStringTextField.setEditable(false);
        this.filterWizardButton = new JButton(DefaultFilterEditor.WIZARD_ICON);
        this.filterWizardButton.setMaximumSize(dim);
        this.filterWizardButton.setPreferredSize(dim);
        this.filterWizardButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WFSFilterTableCellRenderer.this.openFilterDialog();
            }
        });
        this.removeFilterButton = new JButton(DeleteSelectedItemsPlugIn.ICON);
        this.removeFilterButton.setMaximumSize(dim);
        this.removeFilterButton.setPreferredSize(dim);
        this.removeFilterButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                WFSFilterTableCellRenderer.this.removeFilter();
            }
        });
        buttonPanel.add(this.filterWizardButton);
        buttonPanel.add(this.removeFilterButton);
        this.add((Component)this.filterStringTextField, "Center");
        this.add((Component)buttonPanel, "East");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int realRowIndex = table.convertRowIndexToModel(row);
        List infos = (List)((WFSFeatureTypeTableModel)table.getModel()).getFeatureTypeInfosAt(new int[]{realRowIndex});
        this.associatedFeatureTypeInfo = (WFSFeatureTypeInfo)infos.get(0);
        if (value == null) {
            this.filterStringTextField.setText("");
        } else {
            this.filterStringTextField.setText(translator.translateFilter((Filter)value));
        }
        if (isSelected) {
            this.filterStringTextField.setBackground(table.getSelectionBackground());
        } else {
            this.filterStringTextField.setBackground(table.getBackground());
        }
        boolean isEditable = table.isCellEditable(row, column);
        boolean hasFilter = value != null;
        this.filterStringTextField.setEnabled(isEditable);
        this.filterWizardButton.setEnabled(isEditable);
        this.removeFilterButton.setEnabled(isEditable && hasFilter);
        return this;
    }

    private void openFilterDialog() {
        LayerQueryWizardDialog dialog = null;
        Layer layer = this.createDummyLayer();
        dialog = layer != null ? new LayerQueryWizardDialog(JUMPWorkbench.getFrameInstance(), true, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext(), layer) : new LayerQueryWizardDialog(JUMPWorkbench.getFrameInstance(), true, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
        dialog.setFilter(this.currentFilter);
        dialog.setVisible(true);
        if (dialog.exitOk()) {
            this.currentFilter = dialog.getFilter();
        }
        if (this.editor != null) {
            this.editor.stopCellEditing();
        }
    }

    private Layer createDummyLayer() {
        FeatureDataset fc = new FeatureDataset(this.createDummySchema());
        Layer layer = new Layer(this.associatedFeatureTypeInfo.getLocalName(), Color.BLACK, (FeatureCollection)fc, new LayerManager());
        return layer;
    }

    private FeatureSchema createDummySchema() {
        FeatureSchema schema = new FeatureSchema();
        schema.setGeometryType(0);
        schema.addAttribute(this.associatedFeatureTypeInfo.getGeomAttrName().getLocalName(), AttributeType.GEOMETRY);
        for (String currentAttrName : this.associatedFeatureTypeInfo.getAvailableAttributes()) {
            schema.addAttribute(currentAttrName, AttributeType.STRING);
        }
        if (schema.getAttribute(this.associatedFeatureTypeInfo.getPkName()) != null) {
            schema.getAttribute(this.associatedFeatureTypeInfo.getPkName()).setPrimaryKey(true);
        }
        return schema;
    }

    private void removeFilter() {
        this.currentFilter = null;
        if (this.editor != null) {
            this.editor.stopCellEditing();
        }
    }

    public Filter getFilter() {
        return this.currentFilter;
    }

    public void setFilter(Filter filter) {
        this.currentFilter = filter;
    }
}

