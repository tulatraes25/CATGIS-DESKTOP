/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.control;

import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListException;
import com.vividsolutions.jump.feature.Feature;
import java.awt.event.ActionEvent;
import org.apache.log4j.Logger;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.widgets.tables.management.AdvancedTableManagementPanel;
import org.saig.core.model.data.widgets.tables.management.control.AdvancedDBControlPanel;

public class AdvancedDBDetailControlPanel
extends AdvancedDBControlPanel {
    private static final Logger LOGGER = Logger.getLogger(AdvancedDBDetailControlPanel.class);
    private AdvancedTableManagementPanel masterTablePanel;
    private String masterKeyFieldName;
    private Object masterKeyFieldValue;

    public AdvancedDBDetailControlPanel(AdvancedTableManagementPanel masterTablePanel) {
        this.masterTablePanel = masterTablePanel;
        this.buttonCancel.setVisible(false);
    }

    @Override
    protected void insertButtonActionPerformed(ActionEvent evt) {
        EntityList list = this.table.getDisplayEntityList();
        try {
            Object object = list.getNewEntity();
            this.addLinkField(object);
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

    private void addLinkField(Object object) {
        if (this.masterKeyFieldName != null && this.masterKeyFieldValue != null) {
            if (object instanceof Record) {
                Record record = (Record)object;
                record.setAttribute(this.masterKeyFieldName, this.masterKeyFieldValue);
            } else if (object instanceof Feature) {
                Feature feature = (Feature)object;
                feature.setAttribute(this.masterKeyFieldName, this.masterKeyFieldValue);
            }
        }
    }

    @Override
    public void evaluateButtons() {
        if (this.masterTablePanel.hasOperations()) {
            this.buttonInsert.setEnabled(false);
            this.buttonDelete.setEnabled(false);
            this.buttonCommit.setEnabled(false);
            this.buttonRollback.setEnabled(false);
        } else {
            if (this.masterKeyFieldValue != null) {
                this.buttonInsert.setEnabled(true);
            } else {
                this.buttonInsert.setEnabled(false);
            }
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
    }

    public void setMasterKeyFieldName(String masterKeyFieldName) {
        this.masterKeyFieldName = masterKeyFieldName;
    }

    public void setMasterKeyFieldValue(Object masterKeyFieldValue) {
        this.masterKeyFieldValue = masterKeyFieldValue;
    }
}

