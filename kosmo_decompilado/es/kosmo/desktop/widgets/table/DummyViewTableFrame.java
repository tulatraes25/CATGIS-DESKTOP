/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.table;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.ViewTableFrame;

public class DummyViewTableFrame
extends ViewTableFrame {
    private static final long serialVersionUID = 1L;
    protected Table table;

    public DummyViewTableFrame(Table table, PlugInContext context) {
        this.table = table;
        this.updateTitle(table);
    }

    @Override
    public Table getTable() {
        return this.table;
    }
}

