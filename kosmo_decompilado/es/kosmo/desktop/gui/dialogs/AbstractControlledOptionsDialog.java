/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.dialogs;

import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import es.kosmo.desktop.gui.dialogs.AbstractOptionsDialog;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;

public abstract class AbstractControlledOptionsDialog
extends AbstractOptionsDialog {
    private static final long serialVersionUID = 1L;

    protected AbstractControlledOptionsDialog(JFrame parent, boolean modal) {
        super(parent, modal);
    }

    protected AbstractControlledOptionsDialog(JDialog parent, boolean modal) {
        super(parent, modal);
    }

    public AbstractControlledOptionsDialog(JFrame parent, boolean modal, String toolName, String toolDescription, String toolImagePath) {
        super(parent, modal, toolName, toolDescription, toolImagePath);
    }

    public AbstractControlledOptionsDialog(JDialog parent, boolean modal, String toolName, String toolDescription, String toolImagePath) {
        super(parent, modal, toolName, toolDescription, toolImagePath);
    }

    @Override
    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.setOpaque(false);
        }
        return this.okCancelPanel;
    }

    @Override
    public boolean isInputValid() {
        return true;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}

