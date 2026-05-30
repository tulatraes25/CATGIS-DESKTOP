/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.saig.core.model.data.RecordSelectionManager;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.DataManagerPanel;
import org.saig.core.model.data.widgets.ViewTableFrame;

public class DataManager {
    private List<ViewTableFrame> guiTables = new ArrayList<ViewTableFrame>();
    private DataManagerPanel dataManagerPanel;
    private RecordSelectionManager recordSelectionManager = new RecordSelectionManager();

    public void addTable(ViewTableFrame frame) {
        JUMPWorkbench.getFrameInstance().getContext().getDataManager().getRecordSelectionManager().addSelectionListener(frame);
        if (frame.getTable().isEnabled()) {
            JUMPWorkbench.getFrameInstance().addInternalFrame(frame, false, true, frame.getTable().isVisible());
        } else {
            JUMPWorkbench.getFrameInstance().addInternalFrame(frame, false, true, false);
        }
        if (!this.guiTables.contains(frame)) {
            this.guiTables.add(frame);
            Collections.sort(this.guiTables);
            this.getDataManagerPanel().refresh();
        }
    }

    public void remove(ViewTableFrame tableGUI) {
        tableGUI.dispose();
        JUMPWorkbench.getFrameInstance().removeInternalFrame(tableGUI);
        this.guiTables.remove(tableGUI);
    }

    public int indexOf(ViewTableFrame tableGUI) {
        return this.guiTables.indexOf(tableGUI);
    }

    public Iterator<ViewTableFrame> iterator() {
        return this.getTables().iterator();
    }

    public Table getTable(String name) {
        Iterator<ViewTableFrame> i = this.iterator();
        while (i.hasNext()) {
            ViewTableFrame tableGUI = i.next();
            if (!tableGUI.getTable().getName().equals(name)) continue;
            return tableGUI.getTable();
        }
        return null;
    }

    public ViewTableFrame getTable(int index) {
        return this.getTables().get(index);
    }

    public int size() {
        return this.getTables().size();
    }

    public List<Table> getRealTables() {
        ArrayList<Table> tables = new ArrayList<Table>();
        for (ViewTableFrame element : this.guiTables) {
            tables.add(element.getTable());
        }
        return tables;
    }

    public List<ViewTableFrame> getTables() {
        return this.guiTables;
    }

    public void clear() {
        int i = 0;
        while (i < this.guiTables.size()) {
            ViewTableFrame element = this.guiTables.get(i);
            element.dispose();
            JUMPWorkbench.getFrameInstance().removeInternalFrame(element);
            ++i;
        }
        this.guiTables.clear();
        this.recordSelectionManager.clearSelection(false);
        this.getDataManagerPanel().refresh();
    }

    public DataManagerPanel getDataManagerPanel() {
        if (this.dataManagerPanel == null) {
            this.dataManagerPanel = new DataManagerPanel();
        }
        return this.dataManagerPanel;
    }

    public RecordSelectionManager getRecordSelectionManager() {
        return this.recordSelectionManager;
    }

    public void replace(ViewTableFrame oldFrame, ViewTableFrame newFrame) {
        int index = this.indexOf(oldFrame);
        if (index >= 0) {
            this.guiTables.set(index, newFrame);
        }
    }
}

