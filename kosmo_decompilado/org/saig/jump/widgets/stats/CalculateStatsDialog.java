/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.stats;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemovePanel;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveList;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.stats.CalculateStatsPlugIn;
import org.saig.jump.plugin.stats.StatsOperatorsFactory;
import org.saig.jump.widgets.config.ConfigPathPanel;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.SelectFilePanel;

public class CalculateStatsDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    public static final Logger LOGGER = Logger.getLogger(CalculateStatsDialog.class);
    private static final String SELECT_FILE_PANEL_DESCRIPTION_TEXT = I18N.getString(CalculateStatsDialog.class, "target-file");
    private static final String GROUP_BY_PANEL_BORDER_TEXT = I18N.getString(CalculateStatsDialog.class, "group-by");
    private static final String ONLY_SELECTED_CHECKBOX_TEXT = I18N.getString(CalculateStatsDialog.class, "calculate-only-from-selected-elements");
    private static final String OPTIONS_TEXT = I18N.getString(CalculateStatsDialog.class, "options");
    private static final String STATS_SELECTOR_PANEL_BORDER_TEXT = I18N.getString(CalculateStatsDialog.class, "statistics-to-calculate");
    private static final String AVAILABLE_FIELDS = I18N.getString(CalculateStatsDialog.class, "available-fields");
    private static final String SELECTED_FIELDS = I18N.getString(CalculateStatsDialog.class, "selected-fields");
    private static final String FIELD_LIST_LABEL_TEXT = I18N.getString(CalculateStatsDialog.class, "fields");
    private static final String OPPS_LIST_LABEL_TEXT = I18N.getString(CalculateStatsDialog.class, "operations");
    private static final String STATS_LIST_LABEL_TEXT = I18N.getString(CalculateStatsDialog.class, "statistics");
    private static final Dimension LIST_PREFERRED_SIZE = new Dimension(120, 165);
    private static final String STATS_FILE_EXTENSION = "dbf";
    private static final String STATS_TEMP_FILE_NAME = I18N.getString(CalculateStatsDialog.class, "stats");
    private SelectFilePanel selectFilePanel;
    private AddRemovePanel<String> addRemovePanel;
    private JScrollPane fieldsScrollPane;
    private JScrollPane oppsScrollPane;
    private JScrollPane statsScrollPane;
    private JList fieldsJList;
    private JList oppsJList;
    private JList statsJList;
    private JButton addStatButton;
    private JButton removeStatButton;
    private OKCancelPanel okCancelPanel;
    private JCheckBox onlySelectedCheckBox;
    private int numSelectedElements = -1;
    private Layer selectedLayer;
    private final String selectedcolumnName;
    private File file;
    private boolean isOkExit;

    public CalculateStatsDialog(JFrame parent, boolean modal, Layer layer, String columnName, int selectedElements) {
        super((Frame)parent, modal);
        this.selectedLayer = layer;
        this.selectedcolumnName = columnName;
        this.numSelectedElements = selectedElements;
        this.setTitle(String.valueOf(CalculateStatsPlugIn.NAME) + " - " + layer.getName());
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        JPanel northPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(northPanel, 1, 0, this.getGroupByPanel());
        JPanel centerPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(centerPanel, 1, 0, this.getStatsSelectorPanel());
        JPanel southPanel = new JPanel(new GridBagLayout());
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(BorderFactory.createTitledBorder(OPTIONS_TEXT));
        this.onlySelectedCheckBox = new JCheckBox(String.valueOf(ONLY_SELECTED_CHECKBOX_TEXT) + " (" + this.numSelectedElements + ")");
        this.selectFilePanel = new SelectFilePanel(SELECT_FILE_PANEL_DESCRIPTION_TEXT, new String[]{STATS_FILE_EXTENSION}, false);
        this.selectFilePanel.setBorder(BorderFactory.createTitledBorder(SELECT_FILE_PANEL_DESCRIPTION_TEXT));
        String defaultPath = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext().getBlackboard()).get(ConfigPathPanel.DATA_SAVE_PATH_KEY);
        this.selectFilePanel.setSelectedPath(FileUtil.createUniqueFileName(defaultPath, STATS_TEMP_FILE_NAME, STATS_FILE_EXTENSION));
        FormUtils.addRowInGBL(southPanel, 1, 0, this.selectFilePanel);
        if (this.numSelectedElements > 0) {
            FormUtils.addRowInGBL(southPanel, 2, 0, optionsPanel);
            FormUtils.addRowInGBL(optionsPanel, 1, 0, this.onlySelectedCheckBox);
        }
        FormUtils.addRowInGBL(southPanel, 3, 0, this.getOkCancelPanel());
        FormUtils.addFiller(southPanel, 4, 0);
        FormUtils.addRowInGBL(mainPanel, 1, 0, northPanel);
        FormUtils.addRowInGBL(mainPanel, 2, 0, centerPanel);
        FormUtils.addRowInGBL(mainPanel, 3, 0, southPanel);
        FormUtils.addFiller(mainPanel, 4, 0);
        return mainPanel;
    }

    private JPanel getGroupByPanel() {
        JPanel groupByPanel = new JPanel(new GridBagLayout());
        groupByPanel.setBorder(BorderFactory.createTitledBorder(GROUP_BY_PANEL_BORDER_TEXT));
        this.addRemovePanel = new AddRemovePanel(true);
        DefaultListModel listModelIzquierdo = new DefaultListModel();
        DefaultListModel listModelDerecho = new DefaultListModel();
        DefaultAddRemoveList listaIzquierda = new DefaultAddRemoveList(listModelIzquierdo);
        DefaultAddRemoveList listaDerecha = new DefaultAddRemoveList(listModelDerecho);
        this.addRemovePanel.setLeftList(listaIzquierda);
        this.addRemovePanel.setRightList(listaDerecha);
        this.addRemovePanel.setLeftLabel(new JLabel(AVAILABLE_FIELDS));
        this.addRemovePanel.setRightLabel(new JLabel(SELECTED_FIELDS));
        FeatureSchema fs = this.selectedLayer.getFeatureSchema();
        List<String> fieldsNames = fs.getPublicNames();
        fieldsNames.remove(fs.getPublicName(fs.getGeometryIndex()));
        if (this.selectedcolumnName != null) {
            fieldsNames.remove(this.selectedcolumnName);
            this.addRemovePanel.getRightList().getModel().add(this.selectedcolumnName);
        }
        Collections.sort(fieldsNames);
        this.addRemovePanel.getLeftList().getModel().setItems(fieldsNames);
        FormUtils.addRowInGBL(groupByPanel, 1, 0, this.addRemovePanel);
        FormUtils.addFiller(groupByPanel, 2, 0);
        return groupByPanel;
    }

    private JPanel getStatsSelectorPanel() {
        JPanel statsSelectorPanel = new JPanel(new GridBagLayout());
        statsSelectorPanel.setBorder(BorderFactory.createTitledBorder(STATS_SELECTOR_PANEL_BORDER_TEXT));
        FeatureSchema fs = this.selectedLayer.getFeatureSchema();
        List<String> fieldsNames = fs.getPublicNames();
        fieldsNames.remove(fs.getPublicName(fs.getGeometryIndex()));
        Collections.sort(fieldsNames);
        this.fieldsJList = new JList<Object>(fieldsNames.toArray());
        this.fieldsJList.setSelectionMode(0);
        this.fieldsJList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                String fieldName = (String)CalculateStatsDialog.this.fieldsJList.getSelectedValue();
                if (fieldName != null) {
                    List<String> opps = StatsOperatorsFactory.getInstance().getOperatorsFor(CalculateStatsDialog.this.selectedLayer.getFeatureSchema().getAttributeType(fieldName));
                    CalculateStatsDialog.this.oppsJList.setListData(opps.toArray(new String[0]));
                } else {
                    CalculateStatsDialog.this.oppsJList.setListData(new String[0]);
                }
            }
        });
        this.fieldsScrollPane = new JScrollPane();
        this.fieldsScrollPane.setHorizontalScrollBarPolicy(31);
        this.fieldsScrollPane.setMinimumSize(LIST_PREFERRED_SIZE);
        this.fieldsScrollPane.setPreferredSize(LIST_PREFERRED_SIZE);
        this.fieldsScrollPane.setViewportView(this.fieldsJList);
        this.fieldsScrollPane.setVerticalScrollBarPolicy(22);
        this.oppsJList = new JList();
        this.oppsJList.setCellRenderer(new OperatorsListCellRenderer());
        this.oppsJList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object[] selectedValues = CalculateStatsDialog.this.oppsJList.getSelectedValues();
                CalculateStatsDialog.this.addStatButton.setEnabled(selectedValues.length > 0);
            }
        });
        this.oppsScrollPane = new JScrollPane();
        this.oppsScrollPane.setHorizontalScrollBarPolicy(31);
        this.oppsScrollPane.setMinimumSize(LIST_PREFERRED_SIZE);
        this.oppsScrollPane.setPreferredSize(LIST_PREFERRED_SIZE);
        this.oppsScrollPane.setViewportView(this.oppsJList);
        this.oppsScrollPane.setVerticalScrollBarPolicy(22);
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        this.addStatButton = new JButton(I18N.getString(this.getClass(), "add-statistic"), IconLoader.icon("add.png"));
        this.addStatButton.setEnabled(false);
        this.addStatButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] selectedValues = CalculateStatsDialog.this.oppsJList.getSelectedValues();
                int i = 0;
                while (i < selectedValues.length) {
                    ((DefaultListModel)CalculateStatsDialog.this.statsJList.getModel()).addElement(new StatPair((String)CalculateStatsDialog.this.fieldsJList.getSelectedValue(), (String)selectedValues[i]));
                    ++i;
                }
            }
        });
        this.removeStatButton = new JButton(I18N.getString(this.getClass(), "remove-statistic"), IconLoader.icon("delete_small.gif"));
        this.removeStatButton.setEnabled(false);
        this.removeStatButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] values = CalculateStatsDialog.this.statsJList.getSelectedValues();
                int i = 0;
                while (i < values.length) {
                    ((DefaultListModel)CalculateStatsDialog.this.statsJList.getModel()).removeElement(values[i]);
                    ++i;
                }
            }
        });
        FormUtils.addRowInGBL(buttonPanel, 1, 0, this.addStatButton);
        FormUtils.addRowInGBL(buttonPanel, 2, 0, this.removeStatButton);
        this.statsJList = new JList(new DefaultListModel());
        this.statsJList.setCellRenderer(new StatsListCellRenderer());
        this.statsJList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object[] values = CalculateStatsDialog.this.statsJList.getSelectedValues();
                CalculateStatsDialog.this.removeStatButton.setEnabled(values.length > 0);
            }
        });
        this.statsScrollPane = new JScrollPane();
        this.statsScrollPane.setHorizontalScrollBarPolicy(31);
        this.statsScrollPane.setMinimumSize(LIST_PREFERRED_SIZE);
        this.statsScrollPane.setPreferredSize(LIST_PREFERRED_SIZE);
        this.statsScrollPane.setViewportView(this.statsJList);
        this.statsScrollPane.setVerticalScrollBarPolicy(22);
        FormUtils.addRowInGBL((JComponent)statsSelectorPanel, 1, 0, (JComponent)new JLabel(FIELD_LIST_LABEL_TEXT), false, false, true);
        FormUtils.addRowInGBL((JComponent)statsSelectorPanel, 1, 1, (JComponent)new JLabel(OPPS_LIST_LABEL_TEXT), false, false, true);
        FormUtils.addRowInGBL((JComponent)statsSelectorPanel, 1, 3, (JComponent)new JLabel(STATS_LIST_LABEL_TEXT), false, true, true);
        FormUtils.addRowInGBL((JComponent)statsSelectorPanel, 2, 0, (JComponent)this.fieldsScrollPane, false, false, true);
        FormUtils.addRowInGBL((JComponent)statsSelectorPanel, 2, 1, (JComponent)this.oppsScrollPane, false, false, true);
        FormUtils.addRowInGBL((JComponent)statsSelectorPanel, 2, 2, (JComponent)buttonPanel, false, false, true);
        FormUtils.addRowInGBL((JComponent)statsSelectorPanel, 2, 3, (JComponent)this.statsScrollPane, false, true, true);
        return statsSelectorPanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    CalculateStatsDialog.this.isOkExit = false;
                    if (CalculateStatsDialog.this.okCancelPanel.wasOKPressed()) {
                        if (CalculateStatsDialog.this.isInputValid()) {
                            CalculateStatsDialog.this.isOkExit = true;
                            CalculateStatsDialog.this.setVisible(false);
                        } else {
                            CalculateStatsDialog.this.isOkExit = false;
                        }
                    } else {
                        CalculateStatsDialog.this.isOkExit = false;
                        CalculateStatsDialog.this.setVisible(false);
                    }
                }
            });
        }
        return this.okCancelPanel;
    }

    protected final boolean isInputValid() {
        if (this.statsJList.getModel().getSize() < 1) {
            DialogFactory.showInformationDialog(this, I18N.getString(this.getClass(), "at-least-one-statistic-must-be-selected"), I18N.getString(this.getClass(), "warning"));
            return false;
        }
        String path = this.selectFilePanel.getSelectedPath();
        this.file = new File(path);
        if (!FileUtil.canOverwrite(this.selectFilePanel, this.file)) {
            this.file = null;
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        this.selectedLayer = null;
    }

    public final String getFilePath() {
        return this.selectFilePanel.getSelectedPath();
    }

    public final Map<String, Set<String>> getOperatorsByFieldMap() {
        TreeMap<String, Set<String>> operatorsByFieldMap = new TreeMap<String, Set<String>>();
        DefaultListModel model = (DefaultListModel)this.statsJList.getModel();
        int i = 0;
        while (i < model.getSize()) {
            StatPair statPair = (StatPair)model.get(i);
            TreeSet<String> set = (TreeSet<String>)operatorsByFieldMap.get(statPair.getFieldName());
            if (set == null) {
                set = new TreeSet<String>();
                operatorsByFieldMap.put(statPair.getFieldName(), set);
            }
            if (statPair.getOperatorID() == "OP_ALL") {
                List<String> opps = StatsOperatorsFactory.getInstance().getOperatorsFor(this.selectedLayer.getFeatureSchema().getAttributeType(statPair.getFieldName()));
                set.addAll(opps);
                set.remove("OP_ALL");
            } else {
                set.add(statPair.getOperatorID());
            }
            ++i;
        }
        return operatorsByFieldMap;
    }

    public final List<String> getGroupByFields() {
        ArrayList<String> groupByFields = new ArrayList<String>(this.addRemovePanel.getRightItems());
        return groupByFields;
    }

    public final boolean isOkExit() {
        return this.isOkExit;
    }

    public boolean useOnlySelected() {
        return this.onlySelectedCheckBox.isVisible() && this.onlySelectedCheckBox.isSelected();
    }

    private static class OperatorsListCellRenderer
    extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        private OperatorsListCellRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
            this.setText(StatsOperatorsFactory.getInstance().getOperatorName((String)value));
            return this;
        }
    }

    public static class StatPair {
        private String field;
        private String opp;

        public StatPair(String fieldName, String operatorID) {
            this.field = fieldName;
            this.opp = operatorID;
        }

        public final String getFieldName() {
            return this.field;
        }

        public final String getOperatorID() {
            return this.opp;
        }
    }

    private static class StatsListCellRenderer
    extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        private StatsListCellRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
            StatPair statPair = (StatPair)value;
            this.setText(String.valueOf(statPair.getFieldName()) + "." + StatsOperatorsFactory.getInstance().getOperatorName(statPair.getOperatorID()));
            return this;
        }
    }
}

