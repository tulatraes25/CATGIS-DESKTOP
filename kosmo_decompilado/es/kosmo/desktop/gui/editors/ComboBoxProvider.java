/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdesktop.swingx.renderer.CellContext
 *  org.jdesktop.swingx.renderer.ComponentProvider
 */
package es.kosmo.desktop.gui.editors;

import java.awt.Color;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;

public class ComboBoxProvider
extends ComponentProvider<JComboBox> {
    private static final long serialVersionUID = 1L;
    private JComboBox box;

    public ComboBoxProvider(ComboBoxModel model) {
        this.box.setModel(model);
    }

    protected void configureState(CellContext context) {
        this.box.setForeground(Color.BLACK);
    }

    protected JComboBox createRendererComponent() {
        this.box = new JComboBox();
        this.box.setForeground(Color.BLACK);
        return this.box;
    }

    protected void format(CellContext context) {
        this.box.setForeground(Color.BLACK);
        ((JComboBox)this.rendererComponent).setSelectedItem(context.getValue());
    }
}

