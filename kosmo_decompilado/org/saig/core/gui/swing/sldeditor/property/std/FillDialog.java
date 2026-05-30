/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.property.FillEditor;
import org.saig.core.gui.swing.sldeditor.property.PropertyEditorFactory;
import org.saig.core.styling.Fill;
import org.saig.jump.lang.I18N;

public class FillDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private FillEditor fillEditor;
    private FeatureSchema schema;
    boolean exitOk = false;

    public FillDialog(Frame parent, boolean modal, FeatureSchema schema, Fill fill) {
        super(parent, modal);
        this.init(fill, schema);
        this.setLocationRelativeTo(parent);
    }

    public FillDialog(Frame parent, boolean modal, FeatureSchema schema) {
        this(parent, modal, schema, null);
    }

    public FillDialog(Dialog parent, boolean modal, FeatureSchema schema, Fill fill) {
        super(parent, modal);
        this.init(fill, schema);
        this.setLocationRelativeTo(parent);
    }

    public FillDialog(Dialog parent, boolean modal, FeatureSchema schema) {
        this(parent, modal, schema, null);
    }

    private void init(Fill fill, FeatureSchema schema) {
        this.schema = schema;
        JPanel commandPanel = new JPanel();
        JButton btnOk = new JButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.FillDialog.ok"));
        JButton btnCancel = new JButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.FillDialog.cancel"));
        JPanel mainPanel = new JPanel();
        this.fillEditor = PropertyEditorFactory.createPropertyEditorFactory().createFillEditor(this.schema);
        commandPanel.setLayout(new FlowLayout(2, 3, 3));
        commandPanel.add(btnOk);
        commandPanel.add(btnCancel);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(this.fillEditor);
        this.fillEditor.setFill(fill);
        mainPanel.add((Component)commandPanel, "South");
        this.setTitle(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.FillDialog.edit-fill"));
        this.setContentPane(mainPanel);
        this.pack();
        btnOk.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FillDialog.this.exitOk = true;
                FillDialog.this.dispose();
            }
        });
        btnCancel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FillDialog.this.exitOk = false;
                FillDialog.this.dispose();
            }
        });
    }

    public boolean exitOk() {
        return this.exitOk;
    }

    public Fill getFill() {
        return this.fillEditor.getFill();
    }
}

