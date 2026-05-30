/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.model.feature.FeatureCollection
 *  org.deegree.ogcwebservices.wfs.capabilities.FormatType
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import com.vividsolutions.jump.workbench.ui.wizard.AbstractWizardPanel;
import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import de.latlon.deejump.wfs.client.WFSClientHelper;
import es.kosmo.desktop.widgets.sdi.SDIServiceSelectionWizardPanel;
import es.kosmo.desktop.widgets.sdi.wfs.WFSAttributeComboboxEditor;
import es.kosmo.desktop.widgets.sdi.wfs.WFSFeatureTypeTableModel;
import es.kosmo.desktop.widgets.sdi.wfs.WFSFilterTableCellEditor;
import es.kosmo.desktop.widgets.sdi.wfs.WFSFilterTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.saig.core.filter.Filter;
import org.saig.core.filter.visitor.FilterToStringTranslator;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.NumberSpinner;

public class WFSOptionsWizardPanel
extends AbstractWizardPanel
implements TableModelListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(WFSOptionsWizardPanel.class);
    public static final String DEFAULT_OUTPUT_FORMAT_GML3_1_1 = "text/xml; subtype=gml/3.1.1";
    public static final String DEFAULT_OUTPUT_FORMAT_GML2 = "GML2";
    public static final String MAX_NUMBER_OF_ELEMENTS_KEY = "MAX_NUMBER_OF_ELEMENTS";
    private JTable featureTypesTable;
    private JPanel connectionPropertiesPanel;
    private NumberSpinner maximumNumberOfElementsSpinner;
    private JPanel testNumberOfFeaturesPanel;
    private JPanel attributePropertiesPanel;
    private AbstractWFSWrapper wfsService;
    private List<WFSFeatureTypeInfo> selectedFeatureTypesInfos;

    public WFSOptionsWizardPanel() {
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getAttributePropertiesPanel());
        FormUtils.addRowInGBL(this, 1, 0, this.getConnectionPropertiesPanel());
        FormUtils.addFiller(this, 2, 0);
    }

    private JPanel getConnectionPropertiesPanel() {
        if (this.connectionPropertiesPanel == null) {
            this.connectionPropertiesPanel = new JPanel(new GridBagLayout());
            this.connectionPropertiesPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.connection-properties")));
            JLabel maximumNumberOfElementsLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.maximum-number-of-features")) + ":");
            this.maximumNumberOfElementsSpinner = new NumberSpinner(1000, 1, Integer.MAX_VALUE, 1);
            JButton testNumberOfFeaturesButton = new JButton(I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.Check-number-of-returned-features"));
            testNumberOfFeaturesButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    WFSOptionsWizardPanel.this.testNumberOfFeatures();
                }
            });
            this.testNumberOfFeaturesPanel = new JPanel(new FlowLayout());
            this.testNumberOfFeaturesPanel.add(testNumberOfFeaturesButton);
            FormUtils.addRowInGBL((JComponent)this.connectionPropertiesPanel, 0, 0, maximumNumberOfElementsLabel, (JComponent)this.maximumNumberOfElementsSpinner, false);
            FormUtils.addFiller(this.connectionPropertiesPanel, 0, 30);
            FormUtils.addRowInGBL(this.connectionPropertiesPanel, 1, 0, this.testNumberOfFeaturesPanel);
        }
        return this.connectionPropertiesPanel;
    }

    private JPanel getAttributePropertiesPanel() {
        if (this.attributePropertiesPanel == null) {
            this.attributePropertiesPanel = new JPanel(new BorderLayout());
            this.attributePropertiesPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.selected-feature-types")));
            GUIUtil.chooseGoodColumnWidths(this.getFeatureTypesTable());
            JScrollPane tableScrollPane = new JScrollPane(22, 30);
            int dim = this.featureTypesTable.getTableHeader().getPreferredSize().width;
            tableScrollPane.setPreferredSize(new Dimension(dim + 180, 250));
            tableScrollPane.setMinimumSize(new Dimension(dim + 180, 250));
            tableScrollPane.getViewport().add((Component)this.featureTypesTable, null);
            tableScrollPane.getVerticalScrollBar().setUnitIncrement(this.featureTypesTable.getRowHeight());
            this.attributePropertiesPanel.add((Component)this.featureTypesTable.getTableHeader(), "North");
            this.attributePropertiesPanel.add((Component)tableScrollPane, "Center");
        }
        return this.attributePropertiesPanel;
    }

    protected void testNumberOfFeatures() {
        final TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
        final HashMap<WFSFeatureTypeInfo, String> nofMap = new HashMap<WFSFeatureTypeInfo, String>();
        progressDialog.setTitle(I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.Check-number-of-returned-features"));
        progressDialog.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            try {
                                progressDialog.report(String.valueOf(I18N.getMessage("org.saig.jump.widgets.sdi.wfs.WFSURLWizardPanel.Connecting-to-the-WFS-server-{0}", new Object[]{WFSOptionsWizardPanel.this.wfsService.getBaseWfsURL()})) + "...");
                                int total = WFSOptionsWizardPanel.this.selectedFeatureTypesInfos.size();
                                int cont = 0;
                                for (WFSFeatureTypeInfo info : WFSOptionsWizardPanel.this.selectedFeatureTypesInfos) {
                                    progressDialog.report(cont++, total, info.getPrettyString());
                                    info.setServiceVersion(WFSOptionsWizardPanel.this.wfsService.getServiceVersion());
                                    String request = info.buildRequest(true);
                                    String s = WFSClientHelper.createResponsefromWFS(WFSOptionsWizardPanel.this.wfsService.getGetFeatureURL(), request, WFSOptionsWizardPanel.this.wfsService.getBasicAuthData());
                                    StringReader sr = new StringReader(s);
                                    GMLFeatureCollectionDocument gfDoc = new GMLFeatureCollectionDocument();
                                    FeatureCollection newFeatCollec = null;
                                    gfDoc.load(sr, "http://www.deegree.org");
                                    newFeatCollec = gfDoc.parse();
                                    String nof = newFeatCollec.getAttribute("numberOfFeatures");
                                    nofMap.put(info, nof);
                                }
                            }
                            catch (Exception e) {
                                LOGGER.error((Object)"", (Throwable)e);
                                progressDialog.setExceptionMessage(e.getMessage());
                                progressDialog.setVisible(false);
                                return;
                            }
                        }
                        finally {
                            progressDialog.setVisible(false);
                        }
                    }
                }).start();
            }
        });
        GUIUtil.centre(progressDialog, this.getRootPane().getParent());
        progressDialog.setVisible(true);
        this.showResults(nofMap);
    }

    private void showResults(Map<WFSFeatureTypeInfo, String> nofMap) {
        StringBuilder sb = new StringBuilder();
        for (WFSFeatureTypeInfo info : nofMap.keySet()) {
            sb.append("> " + info.getTitle() + " [" + info.getLocalName() + "] -> " + (StringUtils.isNotEmpty((String)nofMap.get(info)) ? nofMap.get(info) : "?") + "\n");
        }
        if (StringUtils.isNotEmpty((String)sb.toString())) {
            DialogFactory.showInformationDialog(this.getRootPane().getParent(), sb.toString(), I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.Check-number-of-returned-features"));
        }
    }

    @Override
    public String getID() {
        return this.getClass().getName();
    }

    @Override
    public String getNextID() {
        return null;
    }

    @Override
    public String getTitle() {
        return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.service-and-feature-type-configuration-options");
    }

    @Override
    public String getInstructions() {
        return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.select-the-parameters-that-must-be-applied-to-the-server-connection-and-for-each-of-the-feature-types-that-you-want-to-add-as-a-new-layer");
    }

    @Override
    public void enteredFromLeft(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
        this.wfsService = (AbstractWFSWrapper)dataMap.get("WFS_SERVICE");
        List serviceInfos = (List)dataMap.get("SELECTED_FEATURE_TYPES");
        this.selectedFeatureTypesInfos = new ArrayList<WFSFeatureTypeInfo>();
        for (WFSFeatureTypeInfo info : serviceInfos) {
            if (!info.isEnabled()) continue;
            this.selectedFeatureTypesInfos.add(info);
        }
        for (WFSFeatureTypeInfo currentInfo : this.selectedFeatureTypesInfos) {
            this.setDefaultValuesIfAvailables(currentInfo);
        }
        ((WFSFeatureTypeTableModel)this.getFeatureTypesTable().getModel()).setFeatTypeInfos(this.selectedFeatureTypesInfos);
        ((WFSFeatureTypeTableModel)this.featureTypesTable.getModel()).fireTableDataChanged();
        this.testNumberOfFeaturesPanel.setVisible(!"1.0.0".equals(this.wfsService.getServiceVersion()));
    }

    private void setDefaultValuesIfAvailables(WFSFeatureTypeInfo currentInfo) {
        FormatType gml3_1_1 = null;
        FormatType gml2 = null;
        FormatType[] formats = currentInfo.getAvailableFormats();
        int i = 0;
        while (i < formats.length && gml3_1_1 == null) {
            FormatType format = formats[i];
            if (format.getValue().equals(DEFAULT_OUTPUT_FORMAT_GML3_1_1)) {
                gml3_1_1 = format;
            } else if (format.getValue().equals(DEFAULT_OUTPUT_FORMAT_GML2)) {
                gml2 = format;
            }
            ++i;
        }
        if (gml3_1_1 != null) {
            currentInfo.setSelectedFormat(gml3_1_1);
        } else if (gml2 != null) {
            currentInfo.setSelectedFormat(gml2);
        }
        URI[] availableSRS = currentInfo.getAvailableSRS();
        boolean matchFound = false;
        String viewSRSName = (String)this.dataMap.get(SDIServiceSelectionWizardPanel.CURRENT_VIEW_SRS_NAME);
        int i2 = 0;
        while (i2 < availableSRS.length && !matchFound) {
            URI currentSRS = availableSRS[i2];
            String srsName = currentSRS.toString();
            if (viewSRSName.indexOf(srsName) != -1) {
                currentInfo.setSelectedSRS(currentSRS);
                matchFound = true;
            }
            ++i2;
        }
        List<QualifiedName> geometryAttrs = currentInfo.getGeometryAttributes();
        if (CollectionUtils.isNotEmpty(geometryAttrs) && geometryAttrs.size() == 1) {
            currentInfo.setGeomAttrName(geometryAttrs.get(0));
        }
    }

    @Override
    public void exitingToRight() throws Exception {
        for (WFSFeatureTypeInfo currentInfo : this.selectedFeatureTypesInfos) {
            currentInfo.setNumMaxFeatures(this.maximumNumberOfElementsSpinner.getIntValue());
            currentInfo.setServiceVersion(this.wfsService.getServiceVersion());
        }
        this.dataMap.put("SELECTED_FEATURE_TYPES", this.selectedFeatureTypesInfos);
    }

    @Override
    public boolean isInputValid() {
        return CollectionUtils.isNotEmpty(this.selectedFeatureTypesInfos) && this.checkFeatureTypeInfos();
    }

    @Override
    public boolean isPanelOk() {
        return CollectionUtils.isNotEmpty(this.selectedFeatureTypesInfos) && this.checkFeatureTypeInfos();
    }

    private boolean checkFeatureTypeInfos() {
        boolean ok = true;
        Iterator<WFSFeatureTypeInfo> itInfos = this.selectedFeatureTypesInfos.iterator();
        while (itInfos.hasNext() && ok) {
            WFSFeatureTypeInfo currentInfo = itInfos.next();
            boolean bl = ok = !StringUtils.isEmpty((String)currentInfo.getPkName()) && currentInfo.getGeomAttrName() != null && currentInfo.getSelectedFormat() != null && currentInfo.getSelectedSRS() != null;
        }
        return ok;
    }

    private JTable getFeatureTypesTable() {
        if (this.featureTypesTable == null) {
            this.featureTypesTable = new JTable(new WFSFeatureTypeTableModel()){
                private static final long serialVersionUID = 1L;
                private FilterToStringTranslator translator;
                {
                    this.translator = new FilterToStringTranslator();
                }

                @Override
                public String getToolTipText(MouseEvent e) {
                    String tip = null;
                    Point p = e.getPoint();
                    int rowIndex = this.rowAtPoint(p);
                    int colIndex = this.columnAtPoint(p);
                    if (rowIndex == -1 || colIndex == -1) {
                        return super.getToolTipText();
                    }
                    int realRowIndex = this.convertRowIndexToModel(rowIndex);
                    int realColumnIndex = this.convertColumnIndexToModel(colIndex);
                    Object value = null;
                    switch (realColumnIndex) {
                        case 0: {
                            tip = "<HTML><B>" + I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.feature-type") + ":</B> " + this.getValueAt(realRowIndex, realColumnIndex) + "</HTML>";
                            break;
                        }
                        case 1: {
                            value = this.getValueAt(realRowIndex, realColumnIndex);
                            if (value == null) break;
                            tip = "<HTML><B>" + I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.attribute-name-corresponding-to-the-geometric-field") + ":</B> " + value;
                            break;
                        }
                        case 2: {
                            value = this.getValueAt(realRowIndex, realColumnIndex);
                            tip = "<HTML><B>" + I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.data-format-for-the-feature-type") + ":</B> " + ((FormatType)value).getValue();
                            break;
                        }
                        case 3: {
                            value = this.getValueAt(realRowIndex, realColumnIndex);
                            tip = "<HTML><B>" + I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.spatial-reference-system") + ":</B> " + value;
                            break;
                        }
                        case 4: {
                            value = this.getValueAt(realRowIndex, realColumnIndex);
                            tip = "<HTML><B>" + I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.filter-to-apply-over-the-feature-type") + ":</B> ";
                            if (value != null) {
                                tip = String.valueOf(tip) + this.translator.translateFilter((Filter)value);
                                break;
                            }
                            tip = String.valueOf(tip) + I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSOptionsWizardPanel.no-filter-assigned");
                            break;
                        }
                        default: {
                            tip = super.getToolTipText(e);
                        }
                    }
                    return tip;
                }

                @Override
                protected JTableHeader createDefaultTableHeader() {
                    return new JTableHeader(this.columnModel){
                        private static final long serialVersionUID = 1L;

                        @Override
                        public String getToolTipText(MouseEvent e) {
                            String tip = null;
                            Point p = e.getPoint();
                            int index = this.columnModel.getColumnIndexAtX(p.x);
                            int realIndex = this.columnModel.getColumn(index).getModelIndex();
                            tip = super.getToolTipText(e);
                            return tip;
                        }
                    };
                }

                @Override
                public void tableChanged(TableModelEvent e) {
                    if (this.isEditing()) {
                        TableCellEditor _editor = this.getCellEditor();
                        _editor.cancelCellEditing();
                    }
                    super.tableChanged(e);
                }
            };
            this.featureTypesTable.getTableHeader().setReorderingAllowed(false);
            ((DefaultTableCellRenderer)this.featureTypesTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(0);
            this.featureTypesTable.setDefaultRenderer(Filter.class, new WFSFilterTableCellRenderer());
            this.featureTypesTable.getModel().addTableModelListener(this);
            this.buildTableParameters(this.featureTypesTable);
        }
        return this.featureTypesTable;
    }

    private void buildTableParameters(JTable table) {
        TableColumn geomColumn = table.getColumnModel().getColumn(1);
        geomColumn.setCellEditor(new WFSAttributeComboboxEditor());
        TableColumn formatColumn = table.getColumnModel().getColumn(2);
        formatColumn.setCellEditor(new WFSAttributeComboboxEditor());
        formatColumn.setCellRenderer(new DefaultTableCellRenderer(){
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof FormatType) {
                    this.setText(((FormatType)value).getValue());
                }
                return this;
            }
        });
        TableColumn srsColumn = table.getColumnModel().getColumn(3);
        srsColumn.setCellEditor(new WFSAttributeComboboxEditor());
        TableColumn filterColumn = table.getColumnModel().getColumn(4);
        filterColumn.setCellEditor(new WFSFilterTableCellEditor(table));
        TableRowSorter<WFSFeatureTypeTableModel> sorter = new TableRowSorter<WFSFeatureTypeTableModel>((WFSFeatureTypeTableModel)table.getModel());
        table.setRowSorter(sorter);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        this.inputChangedFirer.fire();
    }
}

