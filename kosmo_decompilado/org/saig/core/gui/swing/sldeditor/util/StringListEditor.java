/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.util;

import java.awt.GridBagLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import org.saig.core.filter.Expression;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class StringListEditor
extends JComponent
implements SLDEditor {
    JComboBox combo;

    public StringListEditor(String[] values) {
        this.setLayout(new GridBagLayout());
        this.combo = new JComboBox<String>(values);
        this.combo.setEditable(false);
        this.combo.setMinimumSize(FormUtils.getComboDimension());
        if (this.combo.getPreferredSize().width < this.combo.getMinimumSize().width) {
            this.combo.setPreferredSize(this.combo.getMinimumSize());
        }
        FormUtils.addSingleRowWestComponent(this, 0, this.combo);
    }

    public void setExpression(Expression e) {
        this.combo.setSelectedItem(e.toString());
    }

    public Expression getExpression() {
        return styleBuilder.literalExpression((String)this.combo.getSelectedItem());
    }

    public static void main(String[] args) {
        FormUtils.show(new StringListEditor(new String[]{I18N.getString("org.saig.core.gui.swing.sldeditor.util.StringListEditor.one"), I18N.getString("org.saig.core.gui.swing.sldeditor.util.StringListEditor.two"), I18N.getString("org.saig.core.gui.swing.sldeditor.util.StringListEditor.three")}));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.combo.setEnabled(enabled);
    }
}

