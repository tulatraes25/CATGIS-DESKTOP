/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesOp;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;

public class SnapVerticesToolsOptionsPanel
extends OptionsPanel {
    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel jPanel1 = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JCheckBox insertVerticesCheckBox = new JCheckBox();
    private JPanel jPanel2 = new JPanel();
    private Blackboard blackboard;

    public SnapVerticesToolsOptionsPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        try {
            this.jbInit();
        }
        catch (Exception e) {
            Assert.shouldNeverReachHere((String)e.toString());
        }
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void okPressed() {
        this.blackboard.put(SnapVerticesOp.INSERT_VERTICES_IF_NECESSARY_KEY, this.insertVerticesCheckBox.isSelected());
    }

    @Override
    public void init() {
        this.insertVerticesCheckBox.setSelected(PersistentBlackboardPlugIn.get(this.blackboard).get(SnapVerticesOp.INSERT_VERTICES_IF_NECESSARY_KEY, true));
    }

    private void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.jPanel1.setLayout(this.gridBagLayout1);
        this.insertVerticesCheckBox.setText(I18N.getString("workbench.ui.SnapVerticesToolsOptionsPanel.insert-vertex-if-none-in-segment"));
        this.add((Component)this.jPanel1, "Center");
        this.jPanel1.add((Component)this.insertVerticesCheckBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(10, 10, 10, 10), 0, 0));
        this.jPanel1.add((Component)this.jPanel2, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}

