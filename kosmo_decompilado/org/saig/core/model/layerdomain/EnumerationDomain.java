/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Element
 *  org.dom4j.tree.DefaultElement
 */
package org.saig.core.model.layerdomain;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.layerdomain.EmptyDomain;
import org.saig.jump.lang.I18N;

public class EnumerationDomain
extends EmptyDomain {
    public static final String XML_TAG = "EnumerationDomain";
    public static final String TABLE_NAME_XML_TAG = "TableName";
    public static final String REREFENCIA_FIELD_XML_TAG = "ReferenceField";
    public static final String SHOW_FIELD_XML_TAG = "ShowField";
    private boolean nullable;
    private String tableName;
    private String referenceField;
    private String showField;
    private Object defaultValue;
    private String friendlyName;

    @Override
    public String getFriendlyName() {
        return this.friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public boolean isNullable() {
        return this.nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getReferenceField() {
        return this.referenceField;
    }

    public void setReferenceField(String referenceField) {
        this.referenceField = referenceField;
    }

    public String getShowField() {
        return this.showField;
    }

    public void setShowField(String showField) {
        this.showField = showField;
    }

    @Override
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean test(Object obj) {
        if (obj == null) {
            return this.isNullable();
        }
        Table table = JUMPWorkbench.getTable(this.tableName);
        if (table == null) {
            return false;
        }
        List<Record> list = table.getByAttribute(new String[]{this.referenceField}, new Object[]{obj});
        return list.size() > 0;
    }

    @Override
    public String getDescription() {
        return I18N.getMessage("org.saig.core.model.layerdomain.EnumerationDomain.The-value-does-not-reference-the-field-{0}-from-the-table-{1}", new Object[]{this.referenceField, this.tableName});
    }

    @Override
    public Element createSAXElement() {
        DefaultElement element = new DefaultElement(XML_TAG);
        element.addAttribute("Nullable", new Boolean(this.isNullable()).toString());
        element.addAttribute(TABLE_NAME_XML_TAG, this.tableName);
        element.addAttribute(REREFENCIA_FIELD_XML_TAG, this.referenceField);
        element.addAttribute(SHOW_FIELD_XML_TAG, this.showField);
        element.addAttribute("DefaultValue", this.defaultValue.toString());
        element.addAttribute("FriendlyName", this.friendlyName);
        this.addActionsToSAXElement((Element)element);
        return element;
    }

    @Override
    public void fillFromElement(Element element) {
        this.setDefaultValue(element.attributeValue("DefaultValue"));
        this.setNullable(new Boolean(element.attributeValue("Nullable")));
        this.setTableName(element.attributeValue(TABLE_NAME_XML_TAG));
        this.setFriendlyName(element.attributeValue("FriendlyName"));
        this.setReferenceField(element.attributeValue(REREFENCIA_FIELD_XML_TAG));
        this.setShowField(element.attributeValue(SHOW_FIELD_XML_TAG));
        this.readActionsFromSAXElement(element);
    }
}

