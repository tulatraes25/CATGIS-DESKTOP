/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.utils.bookmarks;

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
import org.saig.core.model.bookmark.IBookmark;

public class BookmarkTreeCellEditor
implements TreeCellEditor {
    private JTextField textField = new JTextField();
    private DefaultCellEditor editor = new DefaultCellEditor(this.textField);

    public BookmarkTreeCellEditor() {
        this.textField.setFont(new JLabel().getFont());
    }

    private void changeWidthUsing(JTree tree, int row) {
        int width = (int)((double)tree.getWidth() - tree.getUI().getPathBounds(tree, tree.getUI().getPathForRow(tree, row)).getLocation().getX());
        this.textField.setPreferredSize(new Dimension(width, (int)this.textField.getPreferredSize().getHeight()));
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        this.changeWidthUsing(tree, row);
        if (value instanceof IBookmark) {
            this.textField.setEditable(true);
        }
        return this.editor.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }

    @Override
    public Object getCellEditorValue() {
        return this.editor.getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent && SwingUtilities.isRightMouseButton((MouseEvent)anEvent)) {
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

