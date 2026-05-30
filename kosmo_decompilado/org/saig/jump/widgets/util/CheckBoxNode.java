/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.jump.widgets.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.apache.commons.collections.CollectionUtils;

public class CheckBoxNode {
    private Object text;
    private boolean selected;
    private boolean editable;
    private List<CheckBoxNode> childNodes;
    private Icon icon;

    public CheckBoxNode(Object text, Icon icon, boolean selected, boolean editable) {
        this(text, icon, selected, editable, new ArrayList<CheckBoxNode>());
    }

    public CheckBoxNode(Object text, Icon icon, boolean selected, boolean editable, List<CheckBoxNode> childNodes) {
        this.text = text;
        this.icon = icon;
        this.selected = selected;
        this.editable = editable;
        this.childNodes = childNodes;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean newValue) {
        boolean oldValue = this.selected;
        this.selected = newValue;
        if (oldValue != this.selected && !CollectionUtils.isEmpty(this.childNodes)) {
            for (CheckBoxNode currentChild : this.childNodes) {
                if (!currentChild.isEditable()) continue;
                currentChild.setSelected(newValue);
            }
        }
    }

    public Object getText() {
        return this.text;
    }

    public void setText(Object newValue) {
        this.text = newValue;
    }

    public String toString() {
        return this.text.toString();
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public Icon getIcon() {
        return this.icon;
    }
}

