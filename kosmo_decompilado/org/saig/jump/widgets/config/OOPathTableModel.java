/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;
import org.saig.jump.lang.I18N;

public class OOPathTableModel
extends AbstractTableModel {
    public static Icon OK_ICON = IconLoader.icon("apply_co.gif");
    public static Icon ERROR_ICON = IconLoader.icon("error_obj.gif");
    List<String> paths = new ArrayList<String>();

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        }
        return Icon.class;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return I18N.getString("org.saig.jump.widgets.config.OOPathTableModel.Path");
        }
        return I18N.getString("org.saig.jump.widgets.config.OOPathTableModel.Exists");
    }

    @Override
    public int getRowCount() {
        return this.paths.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return this.paths.get(rowIndex);
        }
        File f = new File(this.paths.get(rowIndex));
        if (f.exists()) {
            return OK_ICON;
        }
        return ERROR_ICON;
    }

    public void addPath(String path) {
        this.paths.add(path);
        this.fireTableDataChanged();
    }

    public void removePath(int i) {
        this.paths.remove(i);
        this.fireTableDataChanged();
    }

    public void removeAllPath() {
        this.paths = new ArrayList<String>();
        this.fireTableDataChanged();
    }

    public List<String> getPaths() {
        return this.paths;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}

