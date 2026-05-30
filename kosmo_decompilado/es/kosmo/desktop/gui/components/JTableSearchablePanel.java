/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import es.kosmo.desktop.gui.components.AbstractSearchablePanel;
import java.util.List;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableRecordDataSource;

public class JTableSearchablePanel
extends AbstractSearchablePanel {
    private static final long serialVersionUID = 1L;
    protected TableRecordDataSource sourceDS;

    public JTableSearchablePanel(String title, TableRecordDataSource tableDS, String attrName, boolean allowMultipleSelection) {
        this.sourceDS = tableDS;
        this.borderTitle = title;
        this.searchableAttrName = attrName;
        this.initializeGUI();
        if (allowMultipleSelection) {
            this.candidatesFoundList.setSelectionMode(2);
        } else {
            this.candidatesFoundList.setSelectionMode(0);
        }
    }

    public JTableSearchablePanel(String title, TableRecordDataSource tableDS) {
        this.sourceDS = tableDS;
        this.borderTitle = title;
        this.schema = this.sourceDS.getSchema();
        this.initializeGUI();
    }

    @Override
    protected void searchElements(String fieldOrdered, Filter filterToApply) {
        List<Record> candidateRecords = this.sourceDS.getRecords(fieldOrdered, filterToApply);
        this.candidatesFoundListModel.clear();
        for (Record currentRecord : candidateRecords) {
            this.candidatesFoundListModel.addElement(currentRecord);
        }
    }
}

