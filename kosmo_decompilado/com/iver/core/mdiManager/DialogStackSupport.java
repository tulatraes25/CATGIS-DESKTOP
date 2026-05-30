/*
 * Decompiled with CFR 0.152.
 */
package com.iver.core.mdiManager;

import com.iver.andami.ui.mdiFrame.MDIFrame;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;

public class DialogStackSupport {
    private List<JDialog> dialogStack = new ArrayList<JDialog>(0);
    private List<Cursor> dialogCursors = new ArrayList<Cursor>(0);

    public DialogStackSupport(MDIFrame frame) {
    }

    public void pushDialog(JDialog dlg) {
        this.dialogStack.add(dlg);
    }

    public JDialog popDialog() {
        return this.dialogStack.remove(this.dialogStack.size() - 1);
    }

    public void setWaitCursor() {
        this.dialogCursors.clear();
        for (JDialog dlg : this.dialogStack) {
            this.dialogCursors.add(dlg.getCursor());
            dlg.setCursor(Cursor.getPredefinedCursor(3));
            dlg.getGlassPane().setVisible(true);
        }
    }

    public void restoreCursor() {
        for (JDialog dlg : this.dialogStack) {
            dlg.setCursor(null);
            dlg.getGlassPane().setVisible(false);
        }
    }
}

