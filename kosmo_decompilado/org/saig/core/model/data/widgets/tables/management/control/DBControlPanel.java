/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.control;

import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityTable;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.widgets.tables.management.TableManagementPanel;
import org.saig.core.model.data.widgets.tables.management.control.ControlPanel;
import org.saig.core.model.data.widgets.tables.management.operations.MandatoryFieldsException;
import org.saig.core.model.data.widgets.tables.management.operations.OperationsManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class DBControlPanel
extends JPanel
implements ControlPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DBControlPanel.class);
    protected JButton buttonInsert;
    protected JButton buttonDelete;
    protected JButton buttonCommit;
    protected JButton buttonRollback;
    protected JButton buttonCancel;
    protected EntityTable table = null;
    protected OperationsManager manager;
    protected TableManagementPanel tablePanel;

    public DBControlPanel() {
        this.initComponents();
    }

    @Override
    public void setTablePanel(TableManagementPanel tablePanel) {
        this.tablePanel = tablePanel;
        EntityTable newTable = tablePanel.getMainTable();
        if (this.table == newTable) {
            return;
        }
        this.table = newTable;
        if (this.table.getSelectionModel() != null) {
            this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    DBControlPanel.this.evaluateButtons();
                }
            });
        }
    }

    @Override
    public void addButton(JComponent newButton) {
        this.add(newButton);
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        JPanel panelOperations = new JPanel();
        panelOperations.setLayout(new FlowLayout());
        panelOperations.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.operations")));
        this.buttonInsert = new JButton();
        this.buttonDelete = new JButton();
        this.buttonCommit = new JButton();
        this.buttonRollback = new JButton();
        this.buttonCancel = new JButton();
        Dimension dimension = new Dimension(120, 30);
        this.buttonInsert.setPreferredSize(dimension);
        this.buttonInsert.setText(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.insert"));
        this.buttonInsert.setMaximumSize(dimension);
        this.buttonInsert.setMinimumSize(dimension);
        this.buttonInsert.setIcon(IconLoader.icon("Plus.gif"));
        this.buttonInsert.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                DBControlPanel.this.insertButtonActionPerformed(evt);
                DBControlPanel.this.postEvent();
            }
        });
        panelOperations.add(this.buttonInsert);
        this.buttonDelete.setPreferredSize(dimension);
        this.buttonDelete.setText(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.delete"));
        this.buttonDelete.setMaximumSize(dimension);
        this.buttonDelete.setMinimumSize(dimension);
        this.buttonDelete.setEnabled(false);
        this.buttonDelete.setIcon(IconLoader.icon("Delete.gif"));
        this.buttonDelete.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                DBControlPanel.this.deleteButtonActionPerformed(evt);
                DBControlPanel.this.postEvent();
            }
        });
        panelOperations.add(this.buttonDelete);
        this.buttonCommit.setText(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.save-changes"));
        this.buttonCommit.setPreferredSize(new Dimension(140, 30));
        this.buttonCommit.setMaximumSize(new Dimension(140, 30));
        this.buttonCommit.setMinimumSize(new Dimension(140, 30));
        this.buttonCommit.setEnabled(false);
        this.buttonCommit.setIcon(IconLoader.icon("Save.gif"));
        this.buttonCommit.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                DBControlPanel.this.commitButtonActionPerformed(evt);
                DBControlPanel.this.postEvent();
            }
        });
        panelOperations.add(this.buttonCommit);
        this.buttonRollback.setText(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.undo"));
        this.buttonRollback.setPreferredSize(dimension);
        this.buttonRollback.setMaximumSize(dimension);
        this.buttonRollback.setMinimumSize(dimension);
        this.buttonRollback.setEnabled(false);
        this.buttonRollback.setIcon(IconLoader.icon("Undo.gif"));
        this.buttonRollback.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                DBControlPanel.this.rollbackButtonActionPerformed(evt);
                DBControlPanel.this.postEvent();
            }
        });
        panelOperations.add(this.buttonRollback);
        JPanel panelCancel = new JPanel();
        this.buttonCancel.setText(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.close"));
        this.buttonCancel.setPreferredSize(dimension);
        this.buttonCancel.setMaximumSize(dimension);
        this.buttonCancel.setMinimumSize(dimension);
        this.buttonCancel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                DBControlPanel.this.cancelButtonActionPerformed(evt);
                DBControlPanel.this.postEvent();
            }
        });
        FormUtils.addRowInGBL(panelCancel, 0, 0, this.buttonCancel);
        FormUtils.addRowInGBL(this, 0, 0, panelOperations);
        FormUtils.addRowInGBL(this, 1, 0, panelCancel);
    }

    protected void postEvent() {
    }

    @Override
    public void addActionListenerToButtons(ActionListener listener) {
        this.buttonInsert.addActionListener(listener);
        this.buttonDelete.addActionListener(listener);
        this.buttonCommit.addActionListener(listener);
        this.buttonRollback.addActionListener(listener);
        this.buttonCancel.addActionListener(listener);
    }

    private void commitButtonActionPerformed(ActionEvent evt) {
        try {
            this.manager.doOperations();
            this.evaluateButtons();
            this.tablePanel.repaint();
        }
        catch (MandatoryFieldsException e) {
            DialogFactory.showInformationDialog(this, String.valueOf(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.the-next-fields-are-mandatory-and-must-be-filled")) + e.getMandatoryFields(), I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.mandatory-fields-missed"));
        }
        catch (Exception ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
    }

    private void deleteButtonActionPerformed(ActionEvent evt) {
        if (this.table.getSelectionModel().isSelectionEmpty()) {
            return;
        }
        int max = this.table.getDisplaySelectionModel().getMaxSelectionIndex();
        int min = this.table.getDisplaySelectionModel().getMinSelectionIndex();
        int min_save = min - 1;
        if (min_save < 0) {
            min_save = 0;
        }
        EntityList list = this.table.getDisplayEntityList();
        try {
            int i = max;
            while (i >= min) {
                this.manager.addDelete(list.getEntity(i));
                list.removeEntity(i);
                --i;
            }
        }
        catch (EntityListException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.table.getDisplaySelectionModel().removeSelectionInterval(min, max);
        this.table.getDisplaySelectionModel().setSelectionInterval(min_save, min_save);
    }

    protected void insertButtonActionPerformed(ActionEvent evt) {
        EntityList list = this.table.getDisplayEntityList();
        try {
            Object object = list.getNewEntity();
            list.addEntity(object);
            this.manager.addInsert(object);
        }
        catch (EntityListException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        int lastRow = list.getCount();
        this.table.getDisplaySelectionModel().clearSelection();
        this.table.getDisplaySelectionModel().setSelectionInterval(lastRow, lastRow);
    }

    private void rollbackButtonActionPerformed(ActionEvent evt) {
        this.manager.clearOperations();
        this.evaluateButtons();
        this.tablePanel.loadData();
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        if (this.manager.hasOperations()) {
            int optionSelected = DialogFactory.showYesNoDialog(this, I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.there-are-operations-that-are-not-finished-do-you-want-to-discard-them"), I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.pending-operations"));
            if (optionSelected == 0) {
                this.tablePanel.getParent().getParent().getParent().getParent().setVisible(false);
            }
        } else {
            this.tablePanel.getParent().getParent().getParent().getParent().setVisible(false);
        }
    }

    @Override
    public void evaluateButtons() {
        if (this.table.getSelectionModel().isSelectionEmpty()) {
            this.buttonDelete.setEnabled(false);
        } else {
            this.buttonDelete.setEnabled(true);
        }
        if (this.manager.hasOperations()) {
            this.buttonCommit.setEnabled(true);
            this.buttonRollback.setEnabled(true);
        } else {
            this.buttonCommit.setEnabled(false);
            this.buttonRollback.setEnabled(false);
        }
    }

    @Override
    public void setManager(OperationsManager manager) {
        this.manager = manager;
    }
}

