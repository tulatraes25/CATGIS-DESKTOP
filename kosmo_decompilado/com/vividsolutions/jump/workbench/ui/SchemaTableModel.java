/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.ColumnBasedTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import org.apache.commons.lang.StringUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.util.LocaleManager;
import org.saig.core.util.language.ITranslatable;
import org.saig.jump.lang.I18N;

public class SchemaTableModel
extends ColumnBasedTableModel {
    public static final String FIELD_NAME_COLUMN_NAME = I18N.getString("workbench.ui.SchemaTableModel.field-name");
    public static final String DATA_TYPE_COLUMN_NAME = I18N.getString("workbench.ui.SchemaTableModel.data-type");
    public static final String FIELD_PUBLIC_NAME_COLUMN_NAME = I18N.getString("workbench.ui.SchemaTableModel.public-name");
    public static final String FIELD_VISIBILITY_COLUMN_NAME = I18N.getString("workbench.ui.SchemaTableModel.visibility");
    private List<Field> fields = new ArrayList<Field>();
    private static final int BLANK_ROWS = 30;
    private Layer layer;

    public SchemaTableModel(Layer layer) {
        this.layer = layer;
        FeatureSchema schema = layer.getUltimateFeatureCollectionWrapper().getFeatureSchema();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            Field field = new Field();
            Attribute attribute = schema.getAttribute(i);
            if (!attribute.isPrimaryKey()) {
                field.setName(attribute.getName());
                field.setOldName(attribute.getName());
                field.setType(attribute.getType());
                field.setPublicName(attribute.getPublicName());
                HashMap<Locale, String> translationsClone = new HashMap<Locale, String>();
                Map<Locale, String> origTranslations = attribute.getTitleByLang();
                for (Locale local : origTranslations.keySet()) {
                    translationsClone.put(local, origTranslations.get(local));
                }
                field.setTitleByLang(translationsClone);
                field.setVisibility(attribute.isVisibility());
                field.setPrimaryKey(attribute.isPrimaryKey());
                field.setGeometry(attribute.getType().equals(AttributeType.GEOMETRY));
                field.setOriginalIndex(i);
                if (attribute.isCalculated()) {
                    field.setCalculated(true);
                    field.setRelationAttribute(((AttributeCalculate)attribute).getRelationFieldName());
                }
                this.fields.add(field);
            }
            ++i;
        }
        this.addBlankRows();
        ArrayList<ColumnBasedTableModel.Column> columns = new ArrayList<ColumnBasedTableModel.Column>();
        columns.add(new ColumnBasedTableModel.Column(this, new Attribute(FIELD_NAME_COLUMN_NAME, FIELD_NAME_COLUMN_NAME, true, null), String.class){

            @Override
            public Object getValueAt(int row) {
                return SchemaTableModel.this.get(row).getName();
            }

            @Override
            public void setValueAt(Object value, int row) {
                if ((value == null || ((String)value).length() == 0) && SchemaTableModel.this.get(row).getType() == null) {
                    return;
                }
                SchemaTableModel.this.get(row).setName(((String)value).trim());
                if (SchemaTableModel.this.get(row).getType() == null) {
                    SchemaTableModel.this.get(row).setType(AttributeType.STRING);
                }
                SchemaTableModel.this.fieldsModified(new int[]{row});
            }
        });
        columns.add(new ColumnBasedTableModel.Column(this, new Attribute(DATA_TYPE_COLUMN_NAME, DATA_TYPE_COLUMN_NAME, true, null), AttributeType.class){

            @Override
            public Object getValueAt(int row) {
                return SchemaTableModel.this.get(row).getType();
            }

            @Override
            public void setValueAt(Object value, int row) {
                if (value == null) {
                    return;
                }
                SchemaTableModel.this.get(row).setType((AttributeType)value);
                if (SchemaTableModel.this.get(row).getName() == null) {
                    SchemaTableModel.this.get(row).setName(SchemaTableModel.this.createName());
                }
                SchemaTableModel.this.fieldsModified(new int[]{row});
            }
        });
        columns.add(new ColumnBasedTableModel.Column(this, new Attribute(FIELD_PUBLIC_NAME_COLUMN_NAME, FIELD_PUBLIC_NAME_COLUMN_NAME, true, null), Field.class){

            @Override
            public Object getValueAt(int row) {
                return SchemaTableModel.this.get(row);
            }

            @Override
            public void setValueAt(Object value, int row) {
                SchemaTableModel.this.get(row).setPublicName(((Field)value).getPublicName());
                if (SchemaTableModel.this.get(row).getType() == null) {
                    SchemaTableModel.this.get(row).setType(AttributeType.STRING);
                }
                SchemaTableModel.this.fieldsModified(new int[]{row});
            }
        });
        columns.add(new ColumnBasedTableModel.Column(this, new Attribute(FIELD_VISIBILITY_COLUMN_NAME, FIELD_VISIBILITY_COLUMN_NAME, true, null), Boolean.class){

            @Override
            public Object getValueAt(int row) {
                return new Boolean(SchemaTableModel.this.get(row).getVisibility());
            }

            @Override
            public void setValueAt(Object value, int row) {
                if (value == null) {
                    return;
                }
                SchemaTableModel.this.get(row).setVisibility((Boolean)value);
                if (SchemaTableModel.this.get(row).getType() == null) {
                    SchemaTableModel.this.get(row).setType(AttributeType.BOOLEAN);
                }
                SchemaTableModel.this.fieldsModified(new int[]{row});
            }
        });
        this.setColumns(columns);
    }

    @Override
    public int getRowCount() {
        return this.fields.size();
    }

    public List<Field> getFields() {
        return Collections.unmodifiableList(this.fields);
    }

    public Field get(int row) {
        return this.fields.get(row);
    }

    private void fieldsModified(int[] rows) {
        int i = 0;
        while (i < rows.length) {
            this.fireTableChanged(new TableModelEvent(this, rows[i]));
            this.addBlankRowsIfNecessary(rows[i]);
            ++i;
        }
    }

    private String createName() {
        int i = 1;
        while (this.hasFieldNamed(String.valueOf(I18N.getString("workbench.ui.SchemaTableModel.field")) + i)) {
            ++i;
        }
        return String.valueOf(I18N.getString("workbench.ui.SchemaTableModel.field")) + i;
    }

    private boolean hasFieldNamed(String name) {
        int i = 0;
        while (i < this.getRowCount()) {
            if (this.get(i).getName() != null && this.get(i).getName().equalsIgnoreCase(name.trim())) {
                return true;
            }
            ++i;
        }
        return false;
    }

    private void addBlankRowsIfNecessary(int indexOfModifiedField) {
        if (this.fields.size() - indexOfModifiedField < 30) {
            int firstRow = this.fields.size();
            this.addBlankRows();
            this.fireTableChanged(new TableModelEvent(this, firstRow, this.fields.size() - 1));
        }
    }

    private void addBlankRows() {
        int i = 0;
        while (i < 30) {
            this.fields.add(new Field());
            ++i;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return this.layer.isEditable() && !this.get(row).isPrimaryKey() && !this.get(row).isGeometry() && (!this.layer.isDataBaseDataSource() || this.isColumnEditableForDatabase(column)) && (!this.get(row).isCalculated() || this.isColumnEditableForCalculated(column)) || this.isColumnVisibilityOrPublic(column);
    }

    private boolean isColumnVisibilityOrPublic(int column) {
        Attribute attr = this.getColumn(column).getAttribute();
        return attr.getName().equals(FIELD_VISIBILITY_COLUMN_NAME) || attr.getName().equals(FIELD_PUBLIC_NAME_COLUMN_NAME);
    }

    private boolean isColumnEditableForDatabase(int column) {
        return true;
    }

    private boolean isColumnEditableForCalculated(int column) {
        Attribute attr = this.getColumn(column).getAttribute();
        return !attr.getName().equals(FIELD_NAME_COLUMN_NAME) && !attr.getName().equals(DATA_TYPE_COLUMN_NAME);
    }

    private void removeField(int row) {
        this.removeFields(new int[]{row});
    }

    public void removeFields(int[] rows) {
        for (Integer row : CollectionUtil.reverseSortedSet(rows)) {
            this.fields.remove(row);
            this.fieldsModified(rows);
            this.fireTableChanged(new TableModelEvent(this, row, row, -1, -1));
        }
    }

    public void removeBlankRows() {
        Iterator<Field> i = this.fields.iterator();
        while (i.hasNext()) {
            Field field = i.next();
            if (field.getName() != null) continue;
            i.remove();
        }
    }

    public void insertBlankRow(int location) {
        this.insertField(location, new Field());
    }

    private void insertField(int location, Field field) {
        this.fields.add(location, field);
        this.fireTableChanged(new TableModelEvent(this, location, location, -1, 1));
        this.fieldsModified(new int[]{location});
    }

    public void move(Collection<Field> fieldsToMove, int displacement) {
        for (Field field : fieldsToMove) {
            int index = this.fields.indexOf(field);
            this.removeField(index);
            this.insertField(index + displacement, field);
        }
    }

    public int indexOf(Field field) {
        return this.fields.indexOf(field);
    }

    public static class Field
    implements ITranslatable {
        private String name = null;
        private AttributeType type = null;
        private boolean visibility = true;
        private boolean calculated = false;
        private boolean geometry = false;
        private boolean primaryKey = false;
        private String relationAttributeName = null;
        private String oldName = null;
        private Map<Locale, String> titleByLang = new HashMap<Locale, String>();
        private int originalIndex = -1;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void setType(AttributeType type) {
            this.type = type;
        }

        public AttributeType getType() {
            return this.type;
        }

        public void setOriginalIndex(int originalIndex) {
            this.originalIndex = originalIndex;
        }

        public int getOriginalIndex() {
            return this.originalIndex;
        }

        public String getPublicName() {
            return this.getTitle(LocaleManager.getActiveLocale());
        }

        public void setPublicName(String publicName) {
            this.setTitle(publicName, LocaleManager.getActiveLocale());
        }

        public boolean getVisibility() {
            return this.visibility;
        }

        public void setVisibility(boolean visibility) {
            this.visibility = visibility;
        }

        public boolean isCalculated() {
            return this.calculated;
        }

        public void setCalculated(boolean isCalculated) {
            this.calculated = isCalculated;
        }

        public boolean isPrimaryKey() {
            return this.primaryKey;
        }

        public void setPrimaryKey(boolean isPrimaryKey) {
            this.primaryKey = isPrimaryKey;
        }

        public boolean isGeometry() {
            return this.geometry;
        }

        public void setGeometry(boolean isGeometry) {
            this.geometry = isGeometry;
        }

        public String getOldName() {
            return this.oldName;
        }

        public void setOldName(String oldName) {
            this.oldName = oldName;
        }

        public String getRelationAttributeName() {
            return this.relationAttributeName;
        }

        public void setRelationAttribute(String relationAttributeName) {
            this.relationAttributeName = relationAttributeName;
        }

        @Override
        public String getTitle(Locale locale) {
            if (!this.titleByLang.containsKey(locale)) {
                this.addLocale(locale);
            }
            return this.titleByLang.get(locale);
        }

        @Override
        public String getTitle() {
            Locale locale = LocaleManager.getActiveLocale();
            if (!this.titleByLang.containsKey(locale) || StringUtils.isEmpty((String)this.titleByLang.get(locale))) {
                this.addLocale(locale);
            }
            return this.titleByLang.get(locale);
        }

        @Override
        public void setTitle(String title, Locale locale) {
            this.titleByLang.put(locale, title);
        }

        @Override
        public Map<Locale, String> getTitleByLang() {
            for (Locale locale : LocaleManager.getAvailablesLocales()) {
                if (this.titleByLang.containsKey(locale) && !StringUtils.isEmpty((String)this.titleByLang.get(locale))) continue;
                this.titleByLang.put(locale, this.name);
            }
            return this.titleByLang;
        }

        @Override
        public void setTitleByLang(Map<Locale, String> titleByLang) {
            this.titleByLang = titleByLang;
        }

        @Override
        public void addLocale(Locale locale) {
            this.titleByLang.put(locale, this.name);
        }

        @Override
        public void removeLocale(Locale locale) {
            if (this.titleByLang.containsKey(locale)) {
                this.titleByLang.remove(locale);
            }
        }
    }
}

