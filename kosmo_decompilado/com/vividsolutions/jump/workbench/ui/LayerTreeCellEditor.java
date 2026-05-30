/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;
import org.apache.commons.lang.StringUtils;
import org.saig.core.styling.Rule;
import org.saig.core.util.LocaleManager;
import org.saig.core.util.language.ITranslatable;

public class LayerTreeCellEditor
implements TreeCellEditor {
    private JTextField textField = new JTextField();
    private DefaultCellEditor editor = new DefaultCellEditor(this.textField);
    private JTree tree;
    private int row;

    public LayerTreeCellEditor(JTree tree) {
        this.tree = tree;
        this.textField.setFont(new JLabel().getFont());
    }

    private void changeWidthUsing(JTree tree, int row) {
        int width = (int)((double)tree.getWidth() - tree.getUI().getPathBounds(tree, tree.getUI().getPathForRow(tree, row)).getLocation().getX());
        this.textField.setPreferredSize(new Dimension(width, (int)this.textField.getPreferredSize().getHeight()));
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        this.row = row;
        this.changeWidthUsing(tree, row);
        if (value instanceof Layerable) {
            boolean ok = ((Layerable)value).isEnabled();
            if (value instanceof Layer) {
                ok &= !((Layer)value).isInternal();
            }
            value = ((Layerable)value).getTitle(LocaleManager.getActiveLocale());
            this.textField.setEditable(ok);
        } else {
            this.textField.setEditable(true);
        }
        if (value instanceof Rule) {
            value = ((Rule)value).getTitle();
        }
        if (value instanceof Category) {
            value = ((Category)value).getTitle(LocaleManager.getActiveLocale());
        }
        return this.editor.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }

    @Override
    public Object getCellEditorValue() {
        return this.getValidCellEditorValue();
    }

    private Object getValidCellEditorValue() {
        String newName;
        Object pathObj = this.tree.getPathForRow(this.row).getLastPathComponent();
        Object candidateEditorValue = this.editor.getCellEditorValue();
        if (pathObj instanceof Category) {
            Category category = (Category)pathObj;
            newName = StringUtils.trimToEmpty((String)((String)candidateEditorValue));
            if (newName.isEmpty()) {
                return category.getName();
            }
            for (ITranslatable iTranslatable : category.getLayerManager().getCategories()) {
                if (!newName.equals(((Category)iTranslatable).getName())) continue;
                return category.getName();
            }
        }
        if (pathObj instanceof Layerable) {
            Layerable layerable = (Layerable)pathObj;
            newName = StringUtils.trimToEmpty((String)((String)candidateEditorValue));
            if (newName.isEmpty()) {
                return layerable.getName();
            }
            for (ITranslatable iTranslatable : layerable.getLayerManager().getLayerables(Layerable.class)) {
                if (!newName.equals(((Layerable)iTranslatable).getName())) continue;
                return layerable.getName();
            }
        }
        if (pathObj instanceof Rule) {
            String newName2 = StringUtils.trimToEmpty((String)((String)candidateEditorValue));
            return newName2;
        }
        return candidateEditorValue;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent == null) {
            return true;
        }
        MouseEvent e = (MouseEvent)anEvent;
        if (SwingUtilities.isRightMouseButton(e)) {
            return false;
        }
        return this.editor.isCellEditable(anEvent);
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    @Override
    public boolean stopCellEditing() {
        return this.editor.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        this.editor.cancelCellEditing();
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        this.editor.addCellEditorListener(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        this.editor.removeCellEditorListener(l);
    }
}

