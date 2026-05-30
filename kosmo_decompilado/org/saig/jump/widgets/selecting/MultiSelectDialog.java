/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.selecting;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class MultiSelectDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private JLabel sourceLayerLabel;
    private JComboBox sourceLayerComboBox;
    private JLabel targetLayerLabel;
    private JList targetLayerList;
    private JScrollPane layersListScrollPane;
    private JLabel operationLabel;
    private JComboBox operationComboBox;
    private List<Layer> layers;
    private JPanel panel;
    private OKCancelPanel okCancelPanel;
    private boolean exitOk = false;
    public static final String CONTENIDO_EN = I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.contained-in");
    public static final String CONTIENE_A = I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.contains");
    public static final String CUBRE_A = I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.covers");
    public static final String CUBIERTA_POR = I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.covered-by");
    public static final String INTERSECTA_CON = I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.intersects-with");
    public static final String CRUZA_CON = I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.crosses-with");
    public static final String TOCA_CON = I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.touches-with");

    public MultiSelectDialog(JFrame parent, List<Layer> layers, boolean modal) {
        super((Frame)parent, true);
        this.setTitle(I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.layer-crosses"));
        this.layers = layers;
        this.initialize();
    }

    private void initialize() {
        this.panel = new JPanel();
        this.panel.setLayout(new GridBagLayout());
        JLabel textLabel = new JLabel(I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.element-from-the-source-layer-selection-that-meets-the-selected-spatial-operation"));
        JLabel textLabel1 = new JLabel("");
        textLabel.setFont(textLabel.getFont().deriveFont(1));
        FormUtils.addRowInGBL(this.panel, 0, 0, textLabel);
        textLabel1.setFont(textLabel1.getFont().deriveFont(1));
        FormUtils.addRowInGBL(this.panel, 1, 0, textLabel1);
        this.sourceLayerLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.source-layer")) + ":");
        FormUtils.addRowInGBL((JComponent)this.panel, 2, 0, this.sourceLayerLabel, (JComponent)this.getSourceComboBox());
        this.operationLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.spatial-operation")) + ":");
        FormUtils.addRowInGBL((JComponent)this.panel, 3, 0, this.operationLabel, (JComponent)this.getOperationsComboBox());
        this.targetLayerLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.selecting.MultiSelectDialog.layer-to-cross")) + ":");
        FormUtils.addRowInGBL((JComponent)this.panel, 4, 0, this.targetLayerLabel, (JComponent)this.getLayersListScrollPane());
        FormUtils.addRowInGBL(this.panel, 5, 0, this.createOKcancelPanel());
        this.setContentPane(this.panel);
        this.setSize(400, 260);
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setModal(true);
        this.setVisible(true);
    }

    private JComboBox getOperationsComboBox() {
        this.operationComboBox = new JComboBox<String>(new String[]{CUBRE_A, CUBIERTA_POR, INTERSECTA_CON, CRUZA_CON, TOCA_CON});
        this.operationComboBox.setSelectedIndex(0);
        return this.operationComboBox;
    }

    private JComboBox getSourceComboBox() {
        if (this.sourceLayerComboBox == null) {
            this.sourceLayerComboBox = new JComboBox();
        }
        Collections.sort(this.layers);
        for (Layer layer : this.layers) {
            this.sourceLayerComboBox.addItem(layer);
        }
        this.sourceLayerComboBox.setSelectedIndex(0);
        this.sourceLayerComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Layer sourceLayer = (Layer)MultiSelectDialog.this.sourceLayerComboBox.getSelectedItem();
                Object[] targetLayers = new String[MultiSelectDialog.this.layers.size() - 1];
                int j = 0;
                for (Layer layer : MultiSelectDialog.this.layers) {
                    if (layer.equals(sourceLayer)) continue;
                    targetLayers[j] = layer.getName();
                    ++j;
                }
                Arrays.sort(targetLayers);
                MultiSelectDialog.this.targetLayerList.setListData(targetLayers);
            }
        });
        return this.sourceLayerComboBox;
    }

    private JScrollPane getLayersListScrollPane() {
        if (this.layersListScrollPane == null) {
            this.layersListScrollPane = new JScrollPane();
            this.layersListScrollPane.setHorizontalScrollBarPolicy(31);
            this.layersListScrollPane.setSize(200, 200);
            this.layersListScrollPane.setViewportView(this.getTargetList());
            this.layersListScrollPane.setVerticalScrollBarPolicy(22);
        }
        return this.layersListScrollPane;
    }

    private OKCancelPanel createOKcancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        this.okCancelPanel.setLayout(gbPaneOKCancel);
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (MultiSelectDialog.this.okCancelPanel.wasOKPressed()) {
                    MultiSelectDialog.this.exitOk = true;
                } else {
                    MultiSelectDialog.this.exitOk = false;
                }
                MultiSelectDialog.this.setVisible(false);
            }
        });
        return this.okCancelPanel;
    }

    private JList getTargetList() {
        this.targetLayerList = new JList();
        Object[] rest = new Object[this.layers.size() - 1];
        int i = 1;
        while (i < this.layers.size()) {
            rest[i - 1] = this.layers.get(i).getName();
            ++i;
        }
        Arrays.sort(rest);
        this.targetLayerList.setListData(rest);
        return this.targetLayerList;
    }

    public Layer getSourceLayer() {
        return (Layer)this.sourceLayerComboBox.getSelectedItem();
    }

    public Object[] getTargetLayers() {
        return this.targetLayerList.getSelectedValues();
    }

    public boolean isOk() {
        return this.exitOk;
    }

    public String getSelectedOperation() {
        return (String)this.operationComboBox.getSelectedItem();
    }

    public boolean isInputValid() {
        Object[] selectedLayers = this.getTargetLayers();
        return selectedLayers != null && selectedLayers.length != 0;
    }
}

