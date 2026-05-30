/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.datasource;

import java.awt.Component;
import java.util.List;
import org.saig.core.model.data.dao.TableRecordDataSource;

public interface ITableSelectionPanel {
    public String getID();

    public String getDescription();

    public void refresh();

    public Component getComponent();

    public List<? extends TableRecordDataSource> getTableDataSources() throws Exception;

    public boolean isInputValid();
}

