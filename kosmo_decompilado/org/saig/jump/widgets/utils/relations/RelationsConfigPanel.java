/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.utils.relations;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.Table;
import org.saig.core.model.project.ProjectManagerFrame;
import org.saig.core.model.relations.Relation;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.MyListCellRenderer;
import org.saig.jump.widgets.utils.relations.RelationConfigDialog;

public class RelationsConfigPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private JScrollPane relationListScrollPane;
    private JList relationList;
    private JPanel actionPanel;
    private JButton addRelationButton;
    private JButton removeRelationButton;
    private JButton viewRelationButton;
    private Layer layer;
    private Table table;
    private LayerManager layerManager;
    private WorkbenchContext context;

    public RelationsConfigPanel(WorkbenchContext context, LayerManager layerManager, Layer layer) {
        super(new GridBagLayout());
        this.layerManager = layerManager;
        this.context = context;
        this.layer = layer;
        this.initialize();
    }

    public RelationsConfigPanel(WorkbenchContext context, LayerManager layerManager, Table table) {
        super(new GridBagLayout());
        this.layerManager = layerManager;
        this.context = context;
        this.table = table;
        this.initialize();
    }

    private void initialize() {
        FormUtils.addRowInGBL(this, 1, 0, this.getTaskListScrollPane());
        FormUtils.addRowInGBL(this, 2, 0, this.getActionPanel());
    }

    private JScrollPane getTaskListScrollPane() {
        if (this.relationListScrollPane == null) {
            this.relationListScrollPane = new JScrollPane();
            this.relationListScrollPane.setHorizontalScrollBarPolicy(31);
            this.relationListScrollPane.setMinimumSize(new Dimension(200, 300));
            this.relationListScrollPane.setPreferredSize(new Dimension(200, 300));
            this.relationListScrollPane.setViewportView(this.getRelationList());
            this.relationListScrollPane.setVerticalScrollBarPolicy(22);
        }
        return this.relationListScrollPane;
    }

    private JList getRelationList() {
        this.relationList = new JList();
        this.relationList.setToolTipText(I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.layer-associated-relations"));
        Collection<Relation<?>> relations = null;
        relations = this.layer != null ? this.layer.getAllRelations() : this.table.getAllRelations();
        this.relationList.setListData(relations.toArray());
        this.relationList.setCellRenderer(new MyListCellRenderer());
        this.relationList.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseReleased(MouseEvent e) {
                if (RelationsConfigPanel.this.relationList.getSelectedValue() != null) {
                    RelationsConfigPanel.this.viewRelationButton.setEnabled(true);
                    RelationsConfigPanel.this.removeRelationButton.setEnabled(true);
                } else {
                    RelationsConfigPanel.this.viewRelationButton.setEnabled(false);
                    RelationsConfigPanel.this.removeRelationButton.setEnabled(false);
                }
                if (!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
            }
        });
        return this.relationList;
    }

    private JPanel getActionPanel() {
        this.actionPanel = new JPanel();
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.setVgap(5);
        gridLayout1.setHgap(5);
        this.actionPanel.setLayout(gridLayout1);
        this.addRelationButton = new JButton();
        this.addRelationButton.setText(I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.new"));
        this.addRelationButton.setToolTipText(I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.add-a-new-relation"));
        this.addRelationButton.setIcon(IconLoader.icon("newTask.gif"));
        this.addRelationButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.addRelationButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!RelationsConfigPanel.this.checkValidParameters()) {
                    return;
                }
                RelationConfigDialog rcDialog = null;
                rcDialog = RelationsConfigPanel.this.layer != null ? new RelationConfigDialog((JFrame)JUMPWorkbench.getFrameInstance(), true, RelationsConfigPanel.this.layerManager, RelationsConfigPanel.this.context.getDataManager(), null, RelationsConfigPanel.this.layer) : new RelationConfigDialog((JFrame)JUMPWorkbench.getFrameInstance(), true, RelationsConfigPanel.this.layerManager, RelationsConfigPanel.this.context.getDataManager(), null, RelationsConfigPanel.this.table);
                GUIUtil.centreOnScreen(rcDialog);
                rcDialog.setVisible(true);
                RelationsConfigPanel.this.relationList.removeAll();
                Collection<Relation<?>> relations = null;
                relations = RelationsConfigPanel.this.layer != null ? RelationsConfigPanel.this.layer.getAllRelations() : RelationsConfigPanel.this.table.getAllRelations();
                RelationsConfigPanel.this.relationList.setListData(relations.toArray());
            }
        });
        this.removeRelationButton = new JButton();
        this.removeRelationButton.setText(I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.delete"));
        this.removeRelationButton.setToolTipText(I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.delete-the-selected-relations"));
        this.removeRelationButton.setIcon(IconLoader.icon("error_obj.gif"));
        this.removeRelationButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.removeRelationButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] selectedValues = RelationsConfigPanel.this.relationList.getSelectedValues();
                ArrayList relationsRemove = new ArrayList();
                int i = 0;
                while (i < selectedValues.length) {
                    relationsRemove.add((Relation)selectedValues[i]);
                    ++i;
                }
                Collection<Relation<?>> relations = null;
                if (RelationsConfigPanel.this.layer != null) {
                    RelationsConfigPanel.this.layer.removeAllRelations(relationsRemove);
                    relations = RelationsConfigPanel.this.layer.getAllRelations();
                } else {
                    RelationsConfigPanel.this.table.removeAllRelations(relationsRemove);
                    relations = RelationsConfigPanel.this.table.getAllRelations();
                    RelationsConfigPanel.this.table.fireTableChanged();
                }
                RelationsConfigPanel.this.relationList.removeAll();
                RelationsConfigPanel.this.relationList.setListData(relations.toArray());
            }
        });
        this.viewRelationButton = new JButton();
        this.viewRelationButton.setText(I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.edit"));
        this.viewRelationButton.setToolTipText(I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.edit-selected-relations"));
        this.viewRelationButton.setIcon(IconLoader.icon("view.gif"));
        this.viewRelationButton.setMargin(ProjectManagerFrame.BUTTON_INSETS);
        this.viewRelationButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] selectedValues = RelationsConfigPanel.this.relationList.getSelectedValues();
                int i = 0;
                while (i < selectedValues.length) {
                    Relation relation = (Relation)selectedValues[i];
                    RelationConfigDialog rcDialog = null;
                    rcDialog = RelationsConfigPanel.this.layer != null ? new RelationConfigDialog((JFrame)JUMPWorkbench.getFrameInstance(), true, RelationsConfigPanel.this.layerManager, RelationsConfigPanel.this.context.getDataManager(), relation, RelationsConfigPanel.this.layer) : new RelationConfigDialog((JFrame)JUMPWorkbench.getFrameInstance(), true, RelationsConfigPanel.this.layerManager, RelationsConfigPanel.this.context.getDataManager(), relation, RelationsConfigPanel.this.table);
                    GUIUtil.centreOnScreen(rcDialog);
                    rcDialog.setVisible(true);
                    ++i;
                }
                RelationsConfigPanel.this.relationList.removeAll();
                Collection<Relation<?>> relations = null;
                relations = RelationsConfigPanel.this.layer != null ? RelationsConfigPanel.this.layer.getAllRelations() : RelationsConfigPanel.this.table.getAllRelations();
                RelationsConfigPanel.this.relationList.setListData(relations.toArray());
            }
        });
        this.actionPanel.add((Component)this.addRelationButton, null);
        this.actionPanel.add((Component)this.removeRelationButton, null);
        this.actionPanel.add((Component)this.viewRelationButton, null);
        return this.actionPanel;
    }

    @Override
    public void repaint() {
        if (this.addRelationButton != null) {
            if (this.context.getTaskManager().size() == 0) {
                this.addRelationButton.setEnabled(false);
            } else {
                this.addRelationButton.setEnabled(true);
            }
        }
        if (this.viewRelationButton != null) {
            if (this.relationList.getSelectedValue() != null) {
                this.viewRelationButton.setEnabled(true);
                this.removeRelationButton.setEnabled(true);
            } else {
                this.viewRelationButton.setEnabled(false);
                this.removeRelationButton.setEnabled(false);
            }
        }
        super.repaint();
    }

    public boolean checkValidParameters() {
        boolean ok = false;
        int numTables = this.context.getDataManager().getTables().size();
        int numLayers = this.layerManager.getLayers().size();
        if (this.layer != null) {
            boolean bl = ok = numTables > 0 || numLayers > 1;
            if (!ok) {
                DialogFactory.showErrorDialog(this, I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.it-must-exist-at-least-a-table-or-layer-different-to-this-in-order-tocreate-a-relation"), I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.errror-creating-new-relation"));
            }
        } else if (this.table != null) {
            boolean bl = ok = numTables > 1 || numLayers > 0;
            if (!ok) {
                DialogFactory.showErrorDialog(this, I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.it-must-exist-at-least-a-layer-or-table-different-to-this-in-order-tocreate-a-relation"), I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigPanel.errror-creating-new-relation"));
            }
        }
        return ok;
    }
}

