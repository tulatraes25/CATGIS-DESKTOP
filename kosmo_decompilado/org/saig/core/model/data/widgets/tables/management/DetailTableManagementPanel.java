/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management;

import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityTableColumn;
import com.pcauto.gui.table.ProxyEntityList;
import com.vividsolutions.jump.feature.Feature;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import org.apache.log4j.Logger;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.widgets.tables.management.TableManagementPanel;
import org.saig.core.model.data.widgets.tables.management.control.ControlPanel;
import org.saig.core.model.data.widgets.tables.management.control.DBDetailControlPanel;
import org.saig.core.model.data.widgets.tables.management.definition.TableDef;
import org.saig.jump.lang.I18N;

public class DetailTableManagementPanel
extends TableManagementPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DetailTableManagementPanel.class);
    private TableManagementPanel masterPanel;
    private String masterKeyFieldName;
    private Object masterKeyFieldValue;

    public DetailTableManagementPanel(String tableName, ControlPanel control, TableManagementPanel masterPanel, String linkField, TableDef tableDef) throws Exception {
        super(tableName, control, tableDef);
        this.mainTable.setBorder(BorderFactory.createTitledBorder(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.tables.management.DetailTableManagementPanel.master-data-table")) + tableName));
        this.masterPanel = masterPanel;
        this.masterKeyFieldName = linkField;
        ((DBDetailControlPanel)control).setMasterKeyFieldName(linkField);
        control.evaluateButtons();
        EntityTableColumn column = this.columnModel.getByName(this.masterKeyFieldName);
        int pos = this.columnModel.getColumnIndex(column);
        this.columnModel.setHidden(pos, true);
        this.resizeColumns();
        this.mainTable.setColumnModel(this.columnModel);
        masterPanel.getMainTable().addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent evt) {
                DetailTableManagementPanel.this.updateData(evt);
                DetailTableManagementPanel.this.updateActivation();
            }

            @Override
            public void mouseClicked(MouseEvent evt) {
                DetailTableManagementPanel.this.updateData(evt);
                DetailTableManagementPanel.this.updateActivation();
            }
        });
        masterPanel.controlPanel.addActionListenerToButtons(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                DetailTableManagementPanel.this.updateActivation();
            }
        });
    }

    private void updateActivation() {
        this.controlPanel.evaluateButtons();
        if (this.masterPanel.hasOperations()) {
            this.mainTable.setReadOnly(true);
        } else {
            this.mainTable.setReadOnly(false);
        }
    }

    private void updateData(MouseEvent evt) {
        Point p = evt.getPoint();
        Object entitySelected = this.masterPanel.getMainTable().getEntityAtPoint(p);
        this.masterKeyFieldValue = this.getLinkField(entitySelected);
        ((DBDetailControlPanel)this.controlPanel).setMasterKeyFieldValue(this.masterKeyFieldValue);
        this.loadData();
    }

    private Object getLinkField(Object entitySelected) {
        Object fieldValue = null;
        if (entitySelected instanceof Record) {
            fieldValue = ((Record)entitySelected).getAttribute(this.masterKeyFieldName);
        } else if (entitySelected instanceof Feature) {
            fieldValue = ((Feature)entitySelected).getAttribute(this.masterKeyFieldName);
        }
        return fieldValue;
    }

    @Override
    public void loadData() {
        ProxyEntityList testTableEntityList = new ProxyEntityList();
        Object defaultEntity = this.getDefaultEntity();
        testTableEntityList.setDefaultEntity(defaultEntity);
        List list = new ArrayList();
        if (this.masterKeyFieldValue != null) {
            try {
                Filter filter = (Filter)ExpressionBuilder.parse(String.valueOf(this.masterKeyFieldName) + "=" + this.masterKeyFieldValue.toString());
                list = this.manager.getDataList(this.masterKeyFieldName, filter);
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
            }
        }
        Iterator it = list.iterator();
        try {
            while (it.hasNext()) {
                Object record = it.next();
                testTableEntityList.addEntity(record);
            }
        }
        catch (EntityListException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.mainTable.setEntityList(testTableEntityList);
    }

    public void setMasterKeyFieldValue(Object masterKeyFieldValue) {
        this.masterKeyFieldValue = masterKeyFieldValue;
    }
}

