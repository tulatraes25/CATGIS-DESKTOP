/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.utiles.swing.JComboBox
 */
package org.gvsig.crs.gui.panels.wizard;

import com.iver.utiles.swing.JComboBox;
import javax.swing.DefaultCellEditor;

public class ComboBoxEditor
extends DefaultCellEditor {
    private static final long serialVersionUID = 1L;

    public ComboBoxEditor(String[] items) {
        super((javax.swing.JComboBox)new JComboBox((Object[])items));
    }
}

