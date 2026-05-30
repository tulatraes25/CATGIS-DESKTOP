/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.postgresql.util.PSQLException
 */
package org.saig.core.model.data.widgets.tables.management.control;

import com.pcauto.gui.table.EntityJTable;
import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityTable;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.widgets.tables.management.AdvancedTableManagementPanel;
import org.saig.core.model.data.widgets.tables.management.EntityTableListener;
import org.saig.core.model.data.widgets.tables.management.RecordAttributesFormDialog;
import org.saig.core.model.data.widgets.tables.management.control.AdvancedControlPanel;
import org.saig.core.model.data.widgets.tables.management.operations.MandatoryFieldsException;
import org.saig.core.model.data.widgets.tables.management.operations.OperationsManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class AdvancedDBControlPanel
extends JPanel
implements AdvancedControlPanel {
    private static final Logger LOGGER = Logger.getLogger(AdvancedDBControlPanel.class);
    protected JButton buttonInsert;
    protected JButton buttonDelete;
    protected JButton buttonCommit;
    protected JButton buttonRollback;
    protected JButton buttonCancel;
    protected EntityTable table = null;
    protected OperationsManager manager;
    protected AdvancedTableManagementPanel tablePanel;
    protected boolean editing = false;

    public AdvancedDBControlPanel() {
        this.initComponents();
    }

    @Override
    public void setTablePanel(AdvancedTableManagementPanel tablePanel) {
        this.tablePanel = tablePanel;
        EntityTable newTable = tablePanel.getMainTable();
        if (this.table == newTable) {
            return;
        }
        this.table = newTable;
        newTable.addEntityTableListener(new EntityTableListener(){

            @Override
            public void lastRowReachedEventFired() {
                AdvancedDBControlPanel.this.insertButtonActionPerformed(null);
            }

            @Override
            public void orderByColumnEventFired(int col, boolean ascending) {
            }
        });
        EntityJTable jTtable = (EntityJTable)this.table.getScrollPane().getViewport().getComponent(0);
        if (jTtable.getSelectionModel() != null) {
            jTtable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    AdvancedDBControlPanel.this.commitButtonActionPerformed(null);
                    AdvancedDBControlPanel.this.evaluateButtons();
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
        JToolBar panelOperationsToolBar = new JToolBar();
        panelOperationsToolBar.setFloatable(false);
        this.buttonInsert = new JButton();
        this.buttonDelete = new JButton();
        this.buttonCommit = new JButton();
        this.buttonRollback = new JButton();
        this.buttonCancel = new JButton();
        this.buttonInsert.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.insert"));
        this.buttonInsert.setIcon(IconLoader.icon("navegacion_add.png"));
        this.buttonInsert.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                AdvancedDBControlPanel.this.insertButtonActionPerformed(evt);
                AdvancedDBControlPanel.this.postEvent();
            }
        });
        panelOperationsToolBar.add(this.buttonInsert);
        this.buttonDelete.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.delete"));
        this.buttonDelete.setEnabled(false);
        this.buttonDelete.setIcon(IconLoader.icon("navegacion_delete.png"));
        this.buttonDelete.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                AdvancedDBControlPanel.this.deleteButtonActionPerformed(evt);
                AdvancedDBControlPanel.this.postEvent();
            }
        });
        panelOperationsToolBar.add(this.buttonDelete);
        this.buttonCommit.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.save-changes"));
        this.buttonCommit.setEnabled(false);
        this.buttonCommit.setIcon(IconLoader.icon("navegacion_commit.png"));
        this.buttonCommit.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                AdvancedDBControlPanel.this.commitButtonActionPerformed(evt);
                AdvancedDBControlPanel.this.postEvent();
            }
        });
        panelOperationsToolBar.add(this.buttonCommit);
        this.buttonRollback.setToolTipText(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.undo"));
        this.buttonRollback.setEnabled(false);
        this.buttonRollback.setIcon(IconLoader.icon("navegacion_rollback.png"));
        this.buttonRollback.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                AdvancedDBControlPanel.this.rollbackButtonActionPerformed(evt);
                AdvancedDBControlPanel.this.postEvent();
            }
        });
        panelOperationsToolBar.add(this.buttonRollback);
        JPanel panelCancel = new JPanel();
        this.buttonCancel.setText(I18N.getString("org.saig.core.model.data.widgets.tables.management.control.DBControlPanel.close"));
        this.buttonCancel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                AdvancedDBControlPanel.this.cancelButtonActionPerformed(evt);
                AdvancedDBControlPanel.this.postEvent();
            }
        });
        FormUtils.addRowInGBL((JComponent)this, 0, 0, (JComponent)panelOperationsToolBar, false, false, false);
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
            this.setEditing(false);
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
        this.tablePanel.pagesNavigationEventFired(10);
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected void insertButtonActionPerformed(ActionEvent evt) {
        int selectedIndex;
        block16: {
            EntityList list = this.table.getDisplayEntityList();
            selectedIndex = this.table.getDisplaySelectionModel().getMaxSelectionIndex();
            if (selectedIndex < 0) {
                selectedIndex = 0;
            }
            try {
                Object object = list.getNewEntity();
                if (!(object instanceof Record)) {
                    throw new ObjectIsNotRecordInstanceException(I18N.getString(this.getClass(), "the-object-that-was-intended-to-be-inserted-into-the-table-was-not-a-record"));
                }
                RecordAttributesFormDialog newRecordDialog = new RecordAttributesFormDialog(((Record)object).getSchema());
                newRecordDialog.setVisible(true);
                if (!newRecordDialog.wasOkPressed()) {
                    this.manager.clearOperations();
                    return;
                }
                this.fillAttributes((Record)object, newRecordDialog.getValues());
                list.insertEntity(selectedIndex, object);
                this.manager.addInsert(object);
                this.manager.doOperations();
            }
            catch (EntityListException e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.manager.clearOperations();
                break block16;
            }
            catch (ObjectIsNotRecordInstanceException recEx) {
                LOGGER.error((Object)"", (Throwable)recEx);
                DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(this.getClass(), "could-not-create-a-new-record-for-the-table-watch-kosmo-log"), I18N.getString(this.getClass(), "error"));
                break block16;
                catch (PSQLException psqle) {
                    LOGGER.error((Object)"", (Throwable)psqle);
                    DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(this.getClass(), "could-not-save-the-new-record-check-data-keys-and-other-not-null-fields"), I18N.getString(this.getClass(), "error"));
                    this.manager.clearOperations();
                    break block16;
                    {
                        catch (Throwable throwable) {
                            throw throwable;
                        }
                    }
                    catch (Exception ex) {
                        LOGGER.error((Object)"", (Throwable)ex);
                        this.manager.clearOperations();
                        break block16;
                    }
                }
                finally {
                    this.manager.clearOperations();
                }
            }
            this.manager.clearOperations();
        }
        this.table.getDisplaySelectionModel().clearSelection();
        this.table.getDisplaySelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
        this.tablePanel.pagesNavigationEventFired(9);
    }

    private void fillAttributes(Record rec, Map<String, Object> values) {
        for (String attrName : values.keySet()) {
            rec.setAttribute(attrName, values.get(attrName));
        }
    }

    private void rollbackButtonActionPerformed(ActionEvent evt) {
        this.manager.clearOperations();
        this.setEditing(false);
        this.evaluateButtons();
        this.tablePanel.loadData(true);
        if (this.tablePanel.isShowingFormView()) {
            this.tablePanel.refreshFormRecord();
        }
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
        if (this.manager.hasOperations() || this.editing) {
            this.buttonCommit.setEnabled(true);
            this.buttonRollback.setEnabled(true);
        } else {
            this.buttonCommit.setEnabled(false);
            this.buttonRollback.setEnabled(false);
        }
    }

    @Override
    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    @Override
    public boolean isEditing() {
        return this.editing;
    }

    @Override
    public void setManager(OperationsManager manager) {
        this.manager = manager;
    }

    @Override
    public JButton getButtonCancel() {
        return this.buttonCancel;
    }

    private class ObjectIsNotRecordInstanceException
    extends Exception {
        public ObjectIsNotRecordInstanceException(String message) {
            super(message);
        }
    }
}

