/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.jump.lang.I18N;

public class JoinDataDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private JComboBox sourceLayerComboBox;
    private JList sourceLayerFieldList;
    private JComboBox sourceTableComboBox;
    private JList sourceTableFieldList;
    private JList fieldList;
    private List layers;
    private List tables;
    private JPanel panel;
    private JCheckBox allFields;
    private OKCancelPanel okCancelPanel;
    private boolean exitOk;
    private Vector fieldLayerElements;
    private Vector fieldDataElements;
    private JFrame parent;

    public JoinDataDialog(JFrame parent, WorkbenchContext context, boolean modal) {
        super((Frame)parent, modal);
        this.parent = parent;
        this.fieldLayerElements = new Vector();
        this.fieldDataElements = new Vector();
        this.layers = context.getLayerManager().getLayers();
        this.tables = context.getDataManager().getTables();
        this.setTitle(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.join-layers-and-tables"));
        this.panel = new JPanel();
        this.panel.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this.panel, 0, 0, this.getDataPanel());
        FormUtils.addRowInGBL(this.panel, 1, 0, this.getFieldsPanel());
        FormUtils.addRowInGBL(this.panel, 2, 0, this.createOKcancelPanel());
        this.setContentPane(this.panel);
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private JPanel getFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        JScrollPane fieldScrollPane = new JScrollPane();
        fieldScrollPane.setHorizontalScrollBarPolicy(31);
        fieldScrollPane.setPreferredSize(new Dimension(200, 100));
        fieldScrollPane.setVerticalScrollBarPolicy(22);
        fieldScrollPane.setViewportView(this.getFieldList());
        fieldsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.fields-to-include-in-the-layer")));
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, 0));
        fieldsPanel.add(fieldScrollPane);
        fieldsPanel.add(this.getALLFieldsPanel());
        fieldsPanel.setPreferredSize(new Dimension(200, 100));
        return fieldsPanel;
    }

    private JPanel getALLFieldsPanel() {
        JPanel allFieldsPanel = new JPanel();
        allFieldsPanel.setLayout(new GridBagLayout());
        JLabel label = new JLabel(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.add-all-the-fields-to-the-layer"));
        this.allFields = new JCheckBox();
        FormUtils.addRowInGBL((JComponent)allFieldsPanel, 0, 0, label, (JComponent)this.allFields);
        return allFieldsPanel;
    }

    private JList getFieldList() {
        this.fieldList = new JList();
        this.fieldList.setPreferredSize(new Dimension(200, 200));
        if (this.tables.size() > 0) {
            List<String> attributesNames = ((ViewTableFrame)this.tables.get(0)).getTable().getSchema().getAttributeNames();
            Object[] datos = new Object[attributesNames.size()];
            int i = 0;
            for (String field : attributesNames) {
                datos[i] = field;
                ++i;
            }
            this.fieldList.setListData(datos);
        }
        return this.fieldList;
    }

    private JPanel getDataPanel() {
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(dataPanel, 0, 0, this.getLayerTablePanel());
        FormUtils.addRowInGBL(dataPanel, 1, 0, this.getAddRemoveFieldPanel());
        return dataPanel;
    }

    private JPanel getAddRemoveFieldPanel() {
        GridLayout innerLayout = new GridLayout();
        innerLayout.setHgap(5);
        innerLayout.setVgap(5);
        JPanel innerPanel = new JPanel(innerLayout);
        JButton addFieldButton = new JButton(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.add-field"));
        addFieldButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                FieldJoinDialog dialog = new FieldJoinDialog(JoinDataDialog.this.parent, (Layer)JoinDataDialog.this.sourceLayerComboBox.getSelectedItem(), ((ViewTableFrame)JoinDataDialog.this.sourceTableComboBox.getSelectedItem()).getTable());
                if (dialog.exitOk) {
                    String selectedFieldLayer = dialog.getFieldLayer();
                    String selectedFieldTable = dialog.getFieldTable();
                    if (JoinDataDialog.this.isAdd(JoinDataDialog.this.fieldLayerElements, selectedFieldLayer) || JoinDataDialog.this.isAdd(JoinDataDialog.this.fieldDataElements, selectedFieldTable)) {
                        return;
                    }
                    JoinDataDialog.this.fieldDataElements.add(selectedFieldTable);
                    JoinDataDialog.this.fieldLayerElements.add(selectedFieldLayer);
                    JoinDataDialog.this.sourceLayerFieldList.setListData(JoinDataDialog.this.fieldLayerElements);
                    JoinDataDialog.this.sourceTableFieldList.setListData(JoinDataDialog.this.fieldDataElements);
                }
            }
        });
        JButton removeFieldButton = new JButton(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.remove-field"));
        removeFieldButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                int[] selectObjects = JoinDataDialog.this.sourceLayerFieldList.getSelectedIndices();
                int cont = 0;
                int i = 0;
                while (i < selectObjects.length) {
                    JoinDataDialog.this.fieldLayerElements.remove(selectObjects[i] - cont);
                    ++cont;
                    ++i;
                }
                cont = 0;
                int[] selectTableObjects = JoinDataDialog.this.sourceTableFieldList.getSelectedIndices();
                int i2 = 0;
                while (i2 < selectTableObjects.length) {
                    JoinDataDialog.this.fieldDataElements.remove(selectTableObjects[i2] - cont);
                    ++cont;
                    ++i2;
                }
                JoinDataDialog.this.panel.repaint();
            }
        });
        innerPanel.add(addFieldButton);
        innerPanel.add(removeFieldButton);
        JPanel addRemoveFieldPanel = new JPanel();
        addRemoveFieldPanel.setLayout(new FlowLayout());
        addRemoveFieldPanel.add(innerPanel);
        return addRemoveFieldPanel;
    }

    private boolean isAdd(Vector vector, String comp) {
        int i = 0;
        while (i < vector.size()) {
            if (((String)vector.get(i)).equals(comp)) {
                return true;
            }
            ++i;
        }
        return false;
    }

    private JPanel getLayerTablePanel() {
        JPanel layerTablePanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(layerTablePanel, 0);
        layerTablePanel.setLayout(boxLayout);
        layerTablePanel.add(this.getSourceLayerPanel());
        layerTablePanel.add(this.getSourceTablePanel());
        return layerTablePanel;
    }

    private JPanel getSourceLayerPanel() {
        JPanel sourceLayerPanel = new JPanel();
        sourceLayerPanel.setLayout(new GridBagLayout());
        sourceLayerPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.layers")));
        JLabel sourceLayerLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.source-layer")) + ":");
        FormUtils.addRowInGBL((JComponent)sourceLayerPanel, 0, 0, sourceLayerLabel, (JComponent)this.getLayerSourceComboBox());
        FormUtils.addRowInGBL(sourceLayerPanel, 1, 0, this.getSourcelayerFieldScrollPane());
        return sourceLayerPanel;
    }

    private JPanel getSourceTablePanel() {
        JPanel sourcetablePanel = new JPanel();
        sourcetablePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.tables")));
        sourcetablePanel.setLayout(new GridBagLayout());
        JLabel sourceTableLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.table")) + ":");
        FormUtils.addRowInGBL((JComponent)sourcetablePanel, 0, 0, sourceTableLabel, (JComponent)this.getSourceTableComboBox());
        FormUtils.addRowInGBL(sourcetablePanel, 1, 0, this.getSourceTableFieldScrollPane());
        return sourcetablePanel;
    }

    private JComboBox getLayerSourceComboBox() {
        if (this.sourceLayerComboBox == null) {
            this.sourceLayerComboBox = new JComboBox();
        }
        for (Layer layer : this.layers) {
            this.sourceLayerComboBox.addItem(layer);
        }
        this.sourceLayerComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JoinDataDialog.this.fieldDataElements.removeAllElements();
                JoinDataDialog.this.fieldLayerElements.removeAllElements();
                JoinDataDialog.this.panel.repaint();
            }
        });
        return this.sourceLayerComboBox;
    }

    private JComboBox getSourceTableComboBox() {
        if (this.sourceTableComboBox == null) {
            this.sourceTableComboBox = new JComboBox();
        }
        for (ViewTableFrame element : this.tables) {
            this.sourceTableComboBox.addItem(element);
        }
        this.sourceTableComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JoinDataDialog.this.fieldDataElements.removeAllElements();
                JoinDataDialog.this.fieldLayerElements.removeAllElements();
                Table table = (Table)JoinDataDialog.this.sourceTableComboBox.getSelectedItem();
                List<String> attributesNames = table.getSchema().getAttributeNames();
                Object[] values = new Object[attributesNames.size()];
                int cont = 0;
                Iterator<String> iter = attributesNames.iterator();
                while (iter.hasNext()) {
                    values[cont] = iter.next();
                    ++cont;
                }
                JoinDataDialog.this.fieldList.setListData(values);
                JoinDataDialog.this.panel.repaint();
            }
        });
        return this.sourceTableComboBox;
    }

    private OKCancelPanel createOKcancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        this.okCancelPanel.setLayout(gbPaneOKCancel);
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (JoinDataDialog.this.okCancelPanel.wasOKPressed()) {
                    JoinDataDialog.this.exitOk = true;
                } else {
                    JoinDataDialog.this.exitOk = false;
                }
                JoinDataDialog.this.setVisible(false);
            }
        });
        return this.okCancelPanel;
    }

    private JScrollPane getSourcelayerFieldScrollPane() {
        JScrollPane sourceLayerFieldScrollPane = new JScrollPane();
        sourceLayerFieldScrollPane.setHorizontalScrollBarPolicy(31);
        sourceLayerFieldScrollPane.setSize(200, 200);
        sourceLayerFieldScrollPane.setViewportView(this.getSourcelayerFieldList());
        sourceLayerFieldScrollPane.setVerticalScrollBarPolicy(22);
        return sourceLayerFieldScrollPane;
    }

    private JList getSourcelayerFieldList() {
        this.sourceLayerFieldList = new JList();
        this.sourceLayerFieldList.setListData(this.fieldLayerElements);
        this.sourceLayerFieldList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (JoinDataDialog.this.sourceLayerFieldList.hasFocus() && !arg0.getValueIsAdjusting()) {
                    int[] selectIndices = JoinDataDialog.this.sourceLayerFieldList.getSelectedIndices();
                    JoinDataDialog.this.sourceTableFieldList.setSelectedIndices(selectIndices);
                }
            }
        });
        return this.sourceLayerFieldList;
    }

    private JScrollPane getSourceTableFieldScrollPane() {
        JScrollPane sourceTableFieldScrollPane = new JScrollPane();
        sourceTableFieldScrollPane.setHorizontalScrollBarPolicy(31);
        sourceTableFieldScrollPane.setSize(200, 200);
        sourceTableFieldScrollPane.setViewportView(this.getSourceTableFieldList());
        sourceTableFieldScrollPane.setVerticalScrollBarPolicy(22);
        return sourceTableFieldScrollPane;
    }

    private JList getSourceTableFieldList() {
        this.sourceTableFieldList = new JList();
        this.sourceTableFieldList.setListData(this.fieldDataElements);
        this.sourceTableFieldList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (JoinDataDialog.this.sourceTableFieldList.hasFocus() && !arg0.getValueIsAdjusting()) {
                    int[] selectIndices = JoinDataDialog.this.sourceTableFieldList.getSelectedIndices();
                    JoinDataDialog.this.sourceLayerFieldList.setSelectedIndices(selectIndices);
                }
            }
        });
        return this.sourceTableFieldList;
    }

    public Layer getSourceLayer() {
        return (Layer)this.sourceLayerComboBox.getSelectedItem();
    }

    public Table getTable() {
        return ((ViewTableFrame)this.sourceTableComboBox.getSelectedItem()).getTable();
    }

    public boolean isOk() {
        return this.exitOk;
    }

    public Vector getFieldsLayer() {
        return this.fieldLayerElements;
    }

    public Vector getFieldsTable() {
        return this.fieldDataElements;
    }

    public Object[] getFields() {
        return this.fieldList.getSelectedValues();
    }

    public boolean isAllFieldsSelected() {
        return this.allFields.isSelected();
    }

    class FieldJoinDialog
    extends JDialog {
        private boolean exitOk;
        private JLabel sourceLayerFieldLabel;
        private JComboBox sourceLayerFieldComboBox;
        private JLabel sourceTableFieldLabel;
        private JComboBox sourceTableFieldComboBox;

        private FieldJoinDialog(JFrame parent, Layer layer, Table table) {
            super((Frame)parent, true);
            this.exitOk = false;
            this.setTitle(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.field-correspondency"));
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            this.sourceLayerFieldLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.layer-fields")) + ":");
            FormUtils.addRowInGBL((JComponent)panel, 0, 0, this.sourceLayerFieldLabel, (JComponent)this.getSourceLayerFieldComboBox(layer));
            this.sourceTableFieldLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.JoinDataDialog.table-fields")) + ":");
            FormUtils.addRowInGBL((JComponent)panel, 1, 0, this.sourceTableFieldLabel, (JComponent)this.getSourceTableFieldComboBox(table));
            FormUtils.addRowInGBL(panel, 2, 0, this.createOKcancelPanel());
            this.setContentPane(panel);
            this.pack();
            GUIUtil.centreOnScreen(this);
            this.setModal(true);
            this.setVisible(true);
        }

        private JComboBox getSourceLayerFieldComboBox(Layer sourceLayer) {
            if (this.sourceLayerFieldComboBox == null) {
                this.sourceLayerFieldComboBox = new JComboBox();
            }
            List<String> attributesNames = sourceLayer.getFeatureCollectionWrapper().getUltimateWrappee().getFeatureSchema().getAttributeNames();
            for (String field : attributesNames) {
                this.sourceLayerFieldComboBox.addItem(field);
            }
            return this.sourceLayerFieldComboBox;
        }

        private JComboBox getSourceTableFieldComboBox(Table table) {
            if (this.sourceTableFieldComboBox == null) {
                this.sourceTableFieldComboBox = new JComboBox();
            }
            List<String> attributesNames = table.getSchema().getAttributeNames();
            for (String field : attributesNames) {
                this.sourceTableFieldComboBox.addItem(field);
            }
            return this.sourceTableFieldComboBox;
        }

        private OKCancelPanel createOKcancelPanel() {
            final OKCancelPanel okCancelPanel = new OKCancelPanel();
            GridBagLayout gbPaneOKCancel = new GridBagLayout();
            okCancelPanel.setLayout(gbPaneOKCancel);
            okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (okCancelPanel.wasOKPressed()) {
                        FieldJoinDialog.this.exitOk = true;
                    } else {
                        FieldJoinDialog.this.exitOk = false;
                    }
                    FieldJoinDialog.this.setVisible(false);
                }
            });
            return okCancelPanel;
        }

        private String getFieldLayer() {
            return (String)this.sourceLayerFieldComboBox.getSelectedItem();
        }

        private String getFieldTable() {
            return (String)this.sourceTableFieldComboBox.getSelectedItem();
        }
    }
}

