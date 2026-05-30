/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.filter;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelHelpPanel;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Dialog;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class SQLFilterBuilderAddValueToListDialog
extends JDialog {
    private OKCancelHelpPanel okCancelPanel;
    private AttributeType type;
    private JComponent valueComponent;

    public SQLFilterBuilderAddValueToListDialog(JDialog parent, AttributeType type) {
        super((Dialog)parent, true);
        this.setTitle(I18N.getString(this.getClass(), "new-value"));
        this.setResizable(false);
        this.type = type;
        JPanel mainPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getValueComponent());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getOKCancelPanel());
        this.setContentPane(mainPanel);
        this.pack();
        GUIUtil.centreOnScreen(this);
    }

    private OKCancelPanel getOKCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelHelpPanel(SQLFilterBuilderAddValueToListDialog.class.getName());
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    SQLFilterBuilderAddValueToListDialog.this.setVisible(false);
                }
            });
        }
        return this.okCancelPanel;
    }

    private JComponent getValueComponent() {
        this.valueComponent = AttributeType.isNumeric(this.type) ? new JSpinner(new SpinnerNumberModel()) : (AttributeType.isDate(this.type) ? new JSpinner(new SpinnerDateModel()) : new JTextField());
        this.valueComponent.addKeyListener(new KeyAdapter(){

            @Override
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == 10) {
                    SQLFilterBuilderAddValueToListDialog.this.okCancelPanel.setOKPressed(true);
                    SQLFilterBuilderAddValueToListDialog.this.setVisible(false);
                }
            }
        });
        return this.valueComponent;
    }

    public boolean wasOKPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public Object getValue() {
        Object value = AttributeType.isNumeric(this.type) ? ((JSpinner)this.valueComponent).getValue() : (AttributeType.isDate(this.type) ? ((JSpinner)this.valueComponent).getValue() : ((JTextField)this.valueComponent).getText());
        return value;
    }
}

