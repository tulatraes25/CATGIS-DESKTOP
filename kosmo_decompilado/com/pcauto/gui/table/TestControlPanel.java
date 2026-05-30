/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityTable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.saig.jump.widgets.util.DialogFactory;

public class TestControlPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private JButton addRow;
    private JButton deleteRowButton;
    private JButton editButton;
    private JButton insertRowButton;
    private JButton moveDownButton;
    private JButton moveUpButton;
    private EntityTable table = null;
    private EntityList list = null;
    private boolean moveUp = true;
    private boolean moveDown = true;
    private boolean add = true;
    private boolean insert = true;
    private boolean delete = true;
    private boolean edit = false;

    public TestControlPanel() {
        this.initComponents();
        this.resetButtons();
    }

    public TestControlPanel(EntityList newList, EntityTable newTable) {
        this.list = newList;
        this.table = newTable;
        this.initComponents();
    }

    public void setEntityList(EntityList newList) {
        this.list = newList;
    }

    public EntityList getEntityList() {
        return this.list;
    }

    public void setEntityTable(EntityTable newTable) {
        if (this.table == newTable) {
            return;
        }
        this.table = newTable;
        if (this.table.getSelectionModel() != null) {
            this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    TestControlPanel.this.evaluateButtons();
                }
            });
        }
    }

    public EntityTable getEntityTable() {
        return this.table;
    }

    public void addButton(JComponent newButton) {
        this.add(newButton);
        this.resetButtons();
    }

    public void setMoveUp(boolean visible) {
        this.moveUp = visible;
        this.resetButtons();
        this.moveUpButton.setVisible(visible);
    }

    public void setMoveDown(boolean visible) {
        this.moveDown = visible;
        this.resetButtons();
        this.moveDownButton.setVisible(visible);
    }

    public void setAdd(boolean visible) {
        this.add = visible;
        this.resetButtons();
        this.addRow.setVisible(visible);
    }

    public void setInsert(boolean visible) {
        this.insert = visible;
        this.resetButtons();
        this.insertRowButton.setVisible(visible);
    }

    public void setDelete(boolean visible) {
        this.delete = visible;
        this.resetButtons();
        this.deleteRowButton.setVisible(visible);
    }

    public boolean isMoveUp() {
        return this.moveUp;
    }

    public boolean isMoveDown() {
        return this.moveDown;
    }

    public boolean isAdd() {
        return this.add;
    }

    public boolean isInsert() {
        return this.insert;
    }

    public boolean isDelete() {
        return this.delete;
    }

    public boolean isEdit() {
        return this.edit;
    }

    public void setEdit(boolean visible) {
        this.edit = visible;
        this.editButton.setVisible(visible);
    }

    public void resetButtons() {
        int buttonMargin = 5;
        int maxWidth = 0;
        int maxHeight = 5;
        Component[] comp = this.getComponents();
        int i = 0;
        while (i < this.getComponentCount()) {
            if (comp[i].isVisible()) {
                if (comp[i].getPreferredSize().getWidth() > (double)maxWidth) {
                    maxWidth = (int)comp[i].getPreferredSize().getWidth();
                }
                maxHeight = (int)((double)maxHeight + (comp[i].getPreferredSize().getHeight() + 5.0));
            }
            ++i;
        }
        this.setPreferredSize(new Dimension(maxWidth + 10, maxHeight + 5));
        this.setMinimumSize(new Dimension(maxWidth + 10, maxHeight + 5));
    }

    private void initComponents() {
        this.moveUpButton = new JButton();
        this.moveDownButton = new JButton();
        this.addRow = new JButton();
        this.insertRowButton = new JButton();
        this.deleteRowButton = new JButton();
        this.editButton = new JButton();
        this.editButton.setVisible(false);
        this.setPreferredSize(new Dimension(130, 200));
        this.setMinimumSize(new Dimension(130, 200));
        this.moveUpButton.setText("Move Up");
        this.moveUpButton.setPreferredSize(new Dimension(120, 30));
        this.moveUpButton.setMaximumSize(new Dimension(120, 30));
        this.moveUpButton.setMinimumSize(new Dimension(120, 30));
        this.moveUpButton.setEnabled(false);
        this.moveUpButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TestControlPanel.this.moveUpButtonActionPerformed(evt);
            }
        });
        this.add(this.moveUpButton);
        this.moveDownButton.setText("Move Down");
        this.moveDownButton.setPreferredSize(new Dimension(120, 30));
        this.moveDownButton.setMaximumSize(new Dimension(120, 30));
        this.moveDownButton.setMinimumSize(new Dimension(120, 30));
        this.moveDownButton.setEnabled(false);
        this.moveDownButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TestControlPanel.this.moveDownButtonActionPerformed(evt);
            }
        });
        this.add(this.moveDownButton);
        this.addRow.setPreferredSize(new Dimension(120, 30));
        this.addRow.setText("Add");
        this.addRow.setMaximumSize(new Dimension(120, 30));
        this.addRow.setMinimumSize(new Dimension(120, 30));
        this.addRow.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TestControlPanel.this.addRowActionPerformed(evt);
            }
        });
        this.add(this.addRow);
        this.insertRowButton.setPreferredSize(new Dimension(120, 30));
        this.insertRowButton.setText("Insert");
        this.insertRowButton.setMaximumSize(new Dimension(120, 30));
        this.insertRowButton.setMinimumSize(new Dimension(120, 30));
        this.insertRowButton.setEnabled(false);
        this.insertRowButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TestControlPanel.this.insertRowButtonActionPerformed(evt);
            }
        });
        this.add(this.insertRowButton);
        this.deleteRowButton.setPreferredSize(new Dimension(120, 30));
        this.deleteRowButton.setText("Delete");
        this.deleteRowButton.setMaximumSize(new Dimension(120, 30));
        this.deleteRowButton.setMinimumSize(new Dimension(120, 30));
        this.deleteRowButton.setEnabled(false);
        this.deleteRowButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TestControlPanel.this.deleteRowButtonActionPerformed(evt);
            }
        });
        this.add(this.deleteRowButton);
        this.editButton.setText("Edit");
        this.editButton.setPreferredSize(new Dimension(120, 30));
        this.editButton.setMaximumSize(new Dimension(120, 30));
        this.editButton.setMinimumSize(new Dimension(120, 30));
        this.editButton.setEnabled(false);
        this.editButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TestControlPanel.this.editButtonActionPerformed(evt);
            }
        });
        this.add(this.editButton);
    }

    private void editButtonActionPerformed(ActionEvent evt) {
        EntityList list = this.table.getDisplayEntityList();
        if (this.table.getDisplaySelectionModel().getMinSelectionIndex() != this.table.getDisplaySelectionModel().getMaxSelectionIndex()) {
            DialogFactory.showWarningDialog(null, "Multiple Records Selected", "");
        }
    }

    private void insertRowButtonActionPerformed(ActionEvent evt) {
        if (this.table.getSelectionModel().isSelectionEmpty()) {
            return;
        }
        EntityList list = this.table.getDisplayEntityList();
        int insertRow = this.table.getDisplaySelectionModel().getMinSelectionIndex();
        try {
            list.insertEntity(insertRow, list.getNewEntity());
        }
        catch (EntityListException e) {
            DialogFactory.showWarningDialog(null, e.getMessage(), "");
        }
        this.table.getDisplaySelectionModel().clearSelection();
        this.table.getDisplaySelectionModel().setSelectionInterval(insertRow, insertRow);
    }

    private void deleteRowButtonActionPerformed(ActionEvent evt) {
        if (this.table.getSelectionModel().isSelectionEmpty()) {
            return;
        }
        int max = this.table.getDisplaySelectionModel().getMaxSelectionIndex();
        int min = this.table.getDisplaySelectionModel().getMinSelectionIndex();
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        int min_save = min - 1;
        if (min_save < 0) {
            min_save = 0;
        }
        EntityList list = this.table.getDisplayEntityList();
        try {
            int i = max;
            while (i >= min) {
                list.removeEntity(i);
                --i;
            }
        }
        catch (EntityListException e) {
            DialogFactory.showWarningDialog(null, e.getMessage(), "");
        }
        this.table.getDisplaySelectionModel().removeSelectionInterval(min, max);
        this.table.getDisplaySelectionModel().setSelectionInterval(min_save, min_save);
    }

    private void addRowActionPerformed(ActionEvent evt) {
        EntityList list = this.table.getDisplayEntityList();
        try {
            list.addEntity(list.getNewEntity());
        }
        catch (EntityListException e) {
            DialogFactory.showWarningDialog(null, e.getMessage(), "");
        }
        int lastRow = list.getCount();
        this.table.getDisplaySelectionModel().clearSelection();
        this.table.getDisplaySelectionModel().setSelectionInterval(lastRow, lastRow);
    }

    private void moveDownButtonActionPerformed(ActionEvent evt) {
        if (this.table.getSelectionModel().isSelectionEmpty()) {
            return;
        }
        int min = this.table.getDisplaySelectionModel().getMinSelectionIndex();
        int max = this.table.getDisplaySelectionModel().getMaxSelectionIndex();
        EntityList list = this.table.getDisplayEntityList();
        try {
            if (list.moveEntity(min, max, min + 1)) {
                this.table.getDisplaySelectionModel().setSelectionInterval(min + 1, max + 1);
            }
        }
        catch (EntityListException e) {
            DialogFactory.showWarningDialog(null, e.getMessage(), "");
        }
    }

    private void moveUpButtonActionPerformed(ActionEvent evt) {
        if (this.table.getSelectionModel().isSelectionEmpty()) {
            return;
        }
        int min = this.table.getDisplaySelectionModel().getMinSelectionIndex();
        int max = this.table.getDisplaySelectionModel().getMaxSelectionIndex();
        EntityList list = this.table.getDisplayEntityList();
        try {
            if (list.moveEntity(min, max, min - 1)) {
                this.table.getDisplaySelectionModel().setSelectionInterval(min - 1, max - 1);
            }
        }
        catch (EntityListException e) {
            DialogFactory.showWarningDialog(null, e.getMessage(), "");
        }
    }

    private void evaluateButtons() {
        if (this.table.getSelectionModel().isSelectionEmpty()) {
            return;
        }
        this.insertRowButton.setEnabled(true);
        this.deleteRowButton.setEnabled(true);
        this.editButton.setEnabled(true);
        if (this.list.getCount() - 1 > this.table.getDisplaySelectionModel().getMinSelectionIndex()) {
            this.moveDownButton.setEnabled(true);
        } else {
            this.moveDownButton.setEnabled(false);
        }
        if (this.table.getDisplaySelectionModel().getMinSelectionIndex() == 0) {
            this.moveUpButton.setEnabled(false);
        } else {
            this.moveUpButton.setEnabled(true);
        }
    }
}

