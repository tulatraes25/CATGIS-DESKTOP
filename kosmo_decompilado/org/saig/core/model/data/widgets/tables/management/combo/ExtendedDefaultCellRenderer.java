/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.combo;

import com.vividsolutions.jump.feature.Feature;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableCellRenderer;
import org.saig.core.gui.swing.dataComponents.layers.JLayerComboBox;
import org.saig.core.gui.swing.dataComponents.tables.JTableWithDataSourceComboBox;
import org.saig.core.model.data.Record;

public class ExtendedDefaultCellRenderer
extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;
    private JTableWithDataSourceComboBox tableCombo;
    private JLayerComboBox layerCombo;
    private String field;
    private final boolean useCache;
    private Map<Object, String> textByValueMap = null;

    public ExtendedDefaultCellRenderer(JTableWithDataSourceComboBox combo, String field) {
        this(combo, field, false);
    }

    public ExtendedDefaultCellRenderer(JTableWithDataSourceComboBox combo, String field, boolean useCache) {
        this.tableCombo = combo;
        this.field = field;
        this.useCache = useCache;
        if (useCache) {
            this.textByValueMap = new HashMap<Object, String>();
        }
    }

    public ExtendedDefaultCellRenderer(JLayerComboBox combo, String field) {
        this(combo, field, false);
    }

    public ExtendedDefaultCellRenderer(JLayerComboBox combo, String field, boolean useCache) {
        this.layerCombo = combo;
        this.field = field;
        this.useCache = useCache;
        if (useCache) {
            this.textByValueMap = new HashMap<Object, String>();
        }
    }

    @Override
    public void setValue(Object value) {
        String text = null;
        if (this.useCache && (text = this.textByValueMap.get(value)) != null) {
            this.setText(text);
            return;
        }
        Comparable<Record> data = null;
        data = this.tableCombo != null ? this.tableCombo.getValueByKey(value) : this.layerCombo.getValueByKey(value);
        if (data instanceof Record) {
            Comparable<Record> record = data;
            text = (String)((Record)record).getAttribute(this.field);
        } else if (data instanceof Feature) {
            Feature feature = (Feature)data;
            text = (String)feature.getAttribute(this.field);
        } else {
            text = "";
        }
        this.setText(text);
        if (this.useCache) {
            this.textByValueMap.put(value, text);
        }
    }
}

