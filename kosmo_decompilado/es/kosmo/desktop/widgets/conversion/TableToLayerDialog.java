/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.conversion;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import es.kosmo.desktop.gui.components.AbstractPlugInOptionsDialog;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.Table;
import org.saig.core.model.feature.Attribute;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.JQueryChooserPanel;

public class TableToLayerDialog
extends AbstractPlugInOptionsDialog {
    private static final long serialVersionUID = 1L;
    protected JPanel mainPanel;
    protected JComboBox tableSelectionComboBox;
    protected DefaultComboBoxModel tableSelectionModel;
    protected JRadioButton toPointRadioButton;
    protected JRadioButton toEnvelopeRadioButton;
    protected JPanel cardLayoutPanel;
    protected JPanel pointLayerOptionsPanel;
    protected JComboBox pointXComboBox;
    protected JComboBox pointYComboBox;
    protected JComboBox pointZComboBox;
    protected JPanel polygonLayerOptionsPanel;
    protected JComboBox envXMaxComboBox;
    protected JComboBox envXMinComboBox;
    protected JComboBox envYMaxComboBox;
    protected JComboBox envYMinComboBox;
    protected JQueryChooserPanel targetLayerQueryChooserPanel;
    protected List<Table> availableTables;
    protected Map<String, String> publicNameToInternalAttributeNamesMap;
    protected CardLayout panelSelectionLayout;
    private static final String NULLVALUE = "----------";
    private static final String POINT_OPTIONS = "POINT_OPTIONS";
    private static final String ENVELOPE_OPTIONS = "ENVELOPE_OPTIONS";

    public TableToLayerDialog(JFrame parent, boolean modal, List<Table> tables) {
        super(parent, modal, I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Convert-table-to-layer"));
        this.availableTables = tables;
        this.fillTableComboBox();
        this.refreshSelectedTable();
    }

    @Override
    protected JPanel getOptionsPanel() {
        if (this.optionsPanel == null) {
            this.optionsPanel = new JPanel(new GridBagLayout());
            this.optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JLabel tableSelectionLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Table")) + ": ");
            JLabel toLabel = new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.to"));
            this.tableSelectionModel = new DefaultComboBoxModel();
            this.tableSelectionComboBox = new JComboBox(this.tableSelectionModel);
            this.tableSelectionComboBox.addActionListener(this);
            this.toPointRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Points-layer"));
            this.toPointRadioButton.addActionListener(this);
            this.toPointRadioButton.setSelected(true);
            this.toEnvelopeRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Rectangles-layer"));
            this.toEnvelopeRadioButton.addActionListener(this);
            ButtonGroup group = new ButtonGroup();
            group.add(this.toPointRadioButton);
            group.add(this.toEnvelopeRadioButton);
            this.targetLayerQueryChooserPanel = new JQueryChooserPanel(I18N.getString("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.Results-layer"), I18N.getString("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.Save-results-layer"), false);
            this.panelSelectionLayout = new CardLayout();
            this.cardLayoutPanel = new JPanel(this.panelSelectionLayout);
            this.cardLayoutPanel.add((Component)this.getPointOptionsPanel(), POINT_OPTIONS);
            this.cardLayoutPanel.add((Component)this.getEnvelopeOptionsPanel(), ENVELOPE_OPTIONS);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 0, 0, tableSelectionLabel, (JComponent)this.tableSelectionComboBox, false);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 0, 30, (JComponent)toLabel, false, true);
            FormUtils.addRowInGBL(this.optionsPanel, 1, 0, this.toPointRadioButton);
            FormUtils.addRowInGBL(this.optionsPanel, 2, 0, this.toEnvelopeRadioButton);
            FormUtils.addRowInGBL(this.optionsPanel, 3, 0, this.targetLayerQueryChooserPanel);
            FormUtils.addRowInGBL(this.optionsPanel, 4, 0, this.cardLayoutPanel);
        }
        return this.optionsPanel;
    }

    protected JPanel getPointOptionsPanel() {
        JPanel toPointPanelT = new JPanel(new GridBagLayout());
        toPointPanelT.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Select-fields")));
        JLabel labelX = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Coordinate-X")) + ":");
        JLabel labelY = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Coordinate-Y")) + ":");
        JLabel labelZ = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Coordinate-Z")) + ":");
        this.pointXComboBox = new JComboBox(new DefaultComboBoxModel());
        this.pointYComboBox = new JComboBox(new DefaultComboBoxModel());
        this.pointZComboBox = new JComboBox(new DefaultComboBoxModel());
        FormUtils.addRowInGBL((JComponent)toPointPanelT, 0, 0, labelX, (JComponent)this.pointXComboBox);
        FormUtils.addRowInGBL((JComponent)toPointPanelT, 1, 0, labelY, (JComponent)this.pointYComboBox);
        FormUtils.addRowInGBL((JComponent)toPointPanelT, 2, 0, labelZ, (JComponent)this.pointZComboBox);
        FormUtils.addFiller(toPointPanelT, 3, 0);
        return toPointPanelT;
    }

    protected JPanel getEnvelopeOptionsPanel() {
        JPanel toEnvelopePanelT = new JPanel(new GridBagLayout());
        toEnvelopePanelT.setMinimumSize(new Dimension(400, 150));
        toEnvelopePanelT.setPreferredSize(new Dimension(400, 150));
        toEnvelopePanelT.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Select-fields")));
        this.envXMaxComboBox = new JComboBox(new DefaultComboBoxModel());
        this.envXMinComboBox = new JComboBox(new DefaultComboBoxModel());
        this.envYMaxComboBox = new JComboBox(new DefaultComboBoxModel());
        this.envYMinComboBox = new JComboBox(new DefaultComboBoxModel());
        FormUtils.addRowInGBL((JComponent)toEnvelopePanelT, 0, 30, new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Max-Y")) + ":"), (JComponent)this.envYMaxComboBox, false);
        FormUtils.addRowInGBL((JComponent)toEnvelopePanelT, 1, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Min-X")) + ":", (JComponent)this.envXMinComboBox, false);
        FormUtils.addFiller(toEnvelopePanelT, 1, 2);
        FormUtils.addRowInGBL((JComponent)toEnvelopePanelT, 1, 60, (JComponent)this.envXMaxComboBox, false, false);
        FormUtils.addRowInGBL((JComponent)toEnvelopePanelT, 1, 61, (JComponent)new JLabel(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Max-X")), false, false);
        FormUtils.addRowInGBL((JComponent)toEnvelopePanelT, 2, 30, new JLabel(" " + I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Min-Y") + ":"), (JComponent)this.envYMinComboBox, false);
        FormUtils.addFiller(toEnvelopePanelT, 3, 0);
        return toEnvelopePanelT;
    }

    public boolean isToPoint() {
        return this.toPointRadioButton.isSelected();
    }

    public Table getSelectedTable() {
        return (Table)this.tableSelectionComboBox.getSelectedItem();
    }

    public String getSelectedPointXName() {
        String xFieldPublicName = (String)this.pointXComboBox.getSelectedItem();
        return this.publicNameToInternalAttributeNamesMap.get(xFieldPublicName);
    }

    public String getSelectedPointYName() {
        String yFieldPublicName = (String)this.pointYComboBox.getSelectedItem();
        return this.publicNameToInternalAttributeNamesMap.get(yFieldPublicName);
    }

    public String getSelectedPointZName() {
        String zFieldPublicName = (String)this.pointZComboBox.getSelectedItem();
        if (zFieldPublicName.equals(NULLVALUE)) {
            return null;
        }
        return this.publicNameToInternalAttributeNamesMap.get(zFieldPublicName);
    }

    public String getSelectedEnvXMaxName() {
        String envXMaxPublicName = (String)this.envXMaxComboBox.getSelectedItem();
        return this.publicNameToInternalAttributeNamesMap.get(envXMaxPublicName);
    }

    public String getSelectedEnvXMinName() {
        String envXMinPublicName = (String)this.envXMinComboBox.getSelectedItem();
        return this.publicNameToInternalAttributeNamesMap.get(envXMinPublicName);
    }

    public String getSelectedEnvYMaxName() {
        String envEnvYMaxPublicName = (String)this.envYMaxComboBox.getSelectedItem();
        return this.publicNameToInternalAttributeNamesMap.get(envEnvYMaxPublicName);
    }

    public String getSelectedEnvYMinName() {
        String envEnvYMinPublicName = (String)this.envYMinComboBox.getSelectedItem();
        return this.publicNameToInternalAttributeNamesMap.get(envEnvYMinPublicName);
    }

    public DataSourceQuery getDataSourceQuery() {
        return this.targetLayerQueryChooserPanel.getDataSourceQuery();
    }

    @Override
    protected boolean isInputValid() {
        if (!this.toPointRadioButton.isSelected() && !this.toEnvelopeRadioButton.isSelected()) {
            DialogFactory.showWarningDialog(this, I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.You-must-select-a-layer-type-to-generate"), I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Information"));
            return false;
        }
        if (this.getSelectedTable() == null) {
            DialogFactory.showWarningDialog(this, I18N.getString("es.kosmo.desktop.widgets.conversion.TableToLayerDialog.Must-select-a-table"), I18N.getString("es.kosmo.desktop.widgets.conversion.TableToLayerDialog.Unselected-table"));
            return false;
        }
        return this.targetLayerQueryChooserPanel.isInputValid();
    }

    @Override
    protected Icon getImageIcon() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.okCancelPanel)) {
            super.actionPerformed(e);
        } else if (e.getSource().equals(this.tableSelectionComboBox)) {
            this.refreshSelectedTable();
        } else if (e.getSource().equals(this.toPointRadioButton)) {
            this.panelSelectionLayout.show(this.cardLayoutPanel, POINT_OPTIONS);
        } else if (e.getSource().equals(this.toEnvelopeRadioButton)) {
            this.panelSelectionLayout.show(this.cardLayoutPanel, ENVELOPE_OPTIONS);
        }
    }

    protected void fillTableComboBox() {
        this.tableSelectionModel.removeAllElements();
        Collections.sort(this.availableTables, new Comparator<Table>(){
            protected Collator collator = Collator.getInstance(LocaleManager.getActiveLocale());

            @Override
            public int compare(Table o1, Table o2) {
                return this.collator.compare(o1.getName(), o2.getName());
            }
        });
        for (Table currentTable : this.availableTables) {
            this.tableSelectionModel.addElement(currentTable);
        }
        this.refreshSelectedTable();
    }

    protected void refreshSelectedTable() {
        if (this.getSelectedTable() != null) {
            Table selectedTable = this.getSelectedTable();
            FeatureSchema schema = selectedTable.getSchema();
            Map<String, Attribute> attrs = schema.getAttributes();
            this.publicNameToInternalAttributeNamesMap = new TreeMap<String, String>();
            for (String attrName : attrs.keySet()) {
                Attribute attr = attrs.get(attrName);
                if (attr.isPrimaryKey() || attr.getType().equals(AttributeType.GEOMETRY) || !AttributeType.areCompatibleTypes(attr.getType(), AttributeType.DOUBLE)) continue;
                this.publicNameToInternalAttributeNamesMap.put(attr.getTitle(LocaleManager.getActiveLocale()), attr.getName());
            }
            ((DefaultComboBoxModel)this.pointXComboBox.getModel()).removeAllElements();
            ((DefaultComboBoxModel)this.pointYComboBox.getModel()).removeAllElements();
            ((DefaultComboBoxModel)this.pointZComboBox.getModel()).removeAllElements();
            this.pointZComboBox.addItem(NULLVALUE);
            ((DefaultComboBoxModel)this.envXMaxComboBox.getModel()).removeAllElements();
            ((DefaultComboBoxModel)this.envXMinComboBox.getModel()).removeAllElements();
            ((DefaultComboBoxModel)this.envYMaxComboBox.getModel()).removeAllElements();
            ((DefaultComboBoxModel)this.envYMinComboBox.getModel()).removeAllElements();
            ArrayList<String> validAttrNames = new ArrayList<String>(this.publicNameToInternalAttributeNamesMap.keySet());
            Collections.sort(validAttrNames, Collator.getInstance(LocaleManager.getActiveLocale()));
            for (String validName : validAttrNames) {
                this.pointXComboBox.addItem(validName);
                this.pointYComboBox.addItem(validName);
                this.pointZComboBox.addItem(validName);
                this.envXMaxComboBox.addItem(validName);
                this.envXMinComboBox.addItem(validName);
                this.envYMaxComboBox.addItem(validName);
                this.envYMinComboBox.addItem(validName);
            }
        }
    }
}

