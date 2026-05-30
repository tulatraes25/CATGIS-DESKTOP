/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.operations;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import org.saig.core.context.GenericContext;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.tables.management.definition.TableDef;
import org.saig.core.model.data.widgets.tables.management.operations.LayerManager;
import org.saig.core.model.data.widgets.tables.management.operations.Manager;
import org.saig.core.model.data.widgets.tables.management.operations.TableDataSourceManager;
import org.saig.core.model.data.widgets.tables.management.operations.TableManager;

public class ManagerFactory {
    public static Manager getManager(String tableName, TableDef tableDef) {
        Table table = JUMPWorkbench.getTable(tableName);
        Layer layer = JUMPWorkbench.getLayer(tableName);
        Manager manager = null;
        manager = table != null ? new TableManager(tableName, tableDef) : (layer != null ? new LayerManager(tableName, tableDef) : new TableDataSourceManager(tableName, GenericContext.getGenericContext().getPrimaryKey(tableName), tableDef));
        return manager;
    }

    public static Manager getManager(Layer layer, TableDef tableDef) {
        LayerManager manager = null;
        manager = new LayerManager(layer, tableDef);
        return manager;
    }
}

