/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.legend;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.elements.BorderPanel;
import org.saig.jump.widgets.print.elements.FontPanel;
import org.saig.jump.widgets.print.elements.TextPreviewPanel;
import org.saig.jump.widgets.print.elements.TramePanel;
import org.saig.jump.widgets.print.elements.legend.LegendFrame;
import org.saig.jump.widgets.print.elements.legend.PrintFont;

public class LegendProperties
extends JFrame {
    private JTabbedPane mainJTabbedPane = new JTabbedPane();
    private JPanel legendElementsPanel = new JPanel();
    private JScrollPane layersJSPane = null;
    private JLabel previewLabel = new JLabel();
    private MyLayerTableModel layerTableModel = null;
    private FontPanel labelLegendPanel;
    private BorderPanel legendBorderPanel;
    private TramePanel legendTramePanel;
    private TextPreviewPanel textPreviewPanel;
    private OKCancelPanel okCancelPanel = new OKCancelPanel();
    private LegendFrame lf = null;

    public LegendProperties(final LegendFrame lf) {
        this.lf = lf;
        this.setName(I18N.getString("org.saig.jump.widgets.print.elements.legend.LegendProperties.legend-properties"));
        this.setTitle(I18N.getString("org.saig.jump.widgets.print.elements.legend.LegendProperties.legend-properties"));
        this.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        this.previewLabel = this.createPreviewLabel(lf.getLabelMapLegendFont());
        this.labelLegendPanel = new FontPanel(this, this.previewLabel);
        this.legendBorderPanel = new BorderPanel(this, this.previewLabel);
        this.legendTramePanel = new TramePanel(this, this.previewLabel);
        this.textPreviewPanel = new TextPreviewPanel(this.previewLabel);
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (LegendProperties.this.okCancelPanel.wasOKPressed()) {
                    PrintFont police = LegendProperties.this.createPolice(LegendProperties.this.previewLabel);
                    lf.setLabelMapLegendFont(police);
                    lf.setBorder(LegendProperties.this.previewLabel.getBorder());
                    LegendProperties.this.changeMapLegends();
                    LegendProperties.this.termine();
                } else {
                    LegendProperties.this.termine();
                }
            }
        });
        this.legendElementsPanel.setLayout(new GridLayout(4, 1));
        this.legendElementsPanel.add(this.labelLegendPanel);
        this.legendElementsPanel.add(this.legendBorderPanel);
        this.legendElementsPanel.add(this.legendTramePanel);
        this.legendElementsPanel.add(this.textPreviewPanel);
        this.mainJTabbedPane.addTab(I18N.getString("org.saig.jump.widgets.print.elements.legend.LegendProperties.style"), this.legendElementsPanel);
        HashMap mapLegends = lf.getVisibleLegends();
        if (mapLegends.size() == 0) {
            lf.buildVisibleLayers();
            mapLegends = lf.getVisibleLegends();
        }
        Object[][] table = new Object[mapLegends.size()][2];
        int i = 0;
        for (String ml : mapLegends.keySet()) {
            table[i][0] = mapLegends.get(ml);
            table[i][1] = ml;
            ++i;
        }
        String[] columnNames = new String[]{I18N.getString("org.saig.jump.widgets.print.elements.legend.LegendProperties.visible"), I18N.getString("org.saig.jump.widgets.print.elements.legend.LegendProperties.name")};
        this.layerTableModel = new MyLayerTableModel(table, columnNames);
        this.layersJSPane = new JScrollPane(new JTable(this.layerTableModel));
        this.mainJTabbedPane.addTab(I18N.getString("org.saig.jump.widgets.print.elements.legend.LegendProperties.layers"), this.layersJSPane);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add((Component)this.mainJTabbedPane, "North");
        this.getContentPane().add((Component)this.okCancelPanel, "South");
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    public void changeMapLegends() {
        HashMap mapLegends = this.lf.getVisibleLegends();
        int i = 0;
        for (String ml : mapLegends.keySet()) {
            Boolean visible = (Boolean)this.layerTableModel.getValueAt(i, 0);
            mapLegends.put(ml, visible);
            ++i;
        }
        this.lf.updateLayers();
    }

    private PrintFont createPolice(JLabel label) {
        PrintFont police = new PrintFont();
        police.setFont(label.getFont());
        police.setColor(label.getForeground());
        police.setUnderline(label.getText().lastIndexOf("<html><u>") != -1 || label.getText().lastIndexOf("<HTML><U>") != -1);
        if (label.isOpaque()) {
            police.setBackgroundColor(label.getBackground());
        }
        police.setBorder(label.getBorder());
        police.setOpaque(label.isOpaque());
        return police;
    }

    private JLabel createPreviewLabel(PrintFont police) {
        JLabel previewLabel = new JLabel(I18N.getString("org.saig.jump.widgets.print.elements.legend.LegendProperties.preview"));
        previewLabel.setFont(police.getFont());
        previewLabel.setForeground(police.getColor());
        previewLabel.setBackground(police.getBackgroundColor());
        previewLabel.setBorder(police.getBorder());
        if (police.isUnderline()) {
            previewLabel.setText("<html><u>" + previewLabel.getText() + "</u></html>");
        }
        previewLabel.setOpaque(police.isOpaque());
        previewLabel.setVerticalAlignment(0);
        previewLabel.setHorizontalAlignment(0);
        return previewLabel;
    }

    private void termine() {
        this.dispose();
    }

    class MyLayerTableModel
    extends AbstractTableModel {
        private String[] columnNames;
        private Object[][] data;

        public MyLayerTableModel(Object[][] data, String[] columnName) {
            this.columnNames = columnName;
            this.data = data;
        }

        @Override
        public int getColumnCount() {
            return this.columnNames.length;
        }

        @Override
        public int getRowCount() {
            return this.data.length;
        }

        @Override
        public String getColumnName(int col) {
            return this.columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return this.data[row][col];
        }

        public Class getColumnClass(int c) {
            return this.getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col < 1;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            this.data[row][col] = value;
            this.fireTableCellUpdated(row, col);
        }
    }
}

