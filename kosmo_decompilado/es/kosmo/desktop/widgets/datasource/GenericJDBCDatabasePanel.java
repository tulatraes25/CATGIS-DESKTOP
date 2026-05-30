/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.widgets.datasource;

import es.kosmo.desktop.widgets.datasource.ITableSelectionPanel;
import java.awt.Component;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.datasource.JDBCPropertiesPanel;

public class GenericJDBCDatabasePanel
implements ITableSelectionPanel {
    private static final Logger LOGGER = Logger.getLogger(GenericJDBCDatabasePanel.class);
    private JDBCPropertiesPanel jdbcPanel;

    @Override
    public Component getComponent() {
        if (this.jdbcPanel == null) {
            this.jdbcPanel = new JDBCPropertiesPanel(false);
        }
        return this.jdbcPanel;
    }

    @Override
    public String getDescription() {
        return I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.databases");
    }

    @Override
    public String getID() {
        return I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.databases");
    }

    @Override
    public List<? extends TableRecordDataSource> getTableDataSources() throws Exception {
        this.jdbcPanel.initializeTable();
        return this.jdbcPanel.getTableDataSources();
    }

    @Override
    public void refresh() {
        this.jdbcPanel.refresh();
    }

    @Override
    public boolean isInputValid() {
        try {
            return this.getTableDataSources().size() > 0;
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            return false;
        }
    }
}

