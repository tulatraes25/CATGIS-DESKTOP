/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.core.mdiManager.DialogStackSupport;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class DialogManager {
    private static DialogStackSupport dialogStack = new DialogStackSupport(null);

    public static JDialog addJDialog(IWindow p, String title) {
        JDialog dlg = DialogManager.getJDialog(p, true, title);
        GUIUtil.centreOnScreen(dlg);
        dialogStack.pushDialog(dlg);
        dlg.setVisible(true);
        return dlg;
    }

    public static JDialog addJDialog(IWindow p, String title, boolean modal, boolean visible) {
        JDialog dlg = DialogManager.getJDialog(p, modal, title);
        GUIUtil.centreOnScreen(dlg);
        dialogStack.pushDialog(dlg);
        dlg.setVisible(visible);
        return dlg;
    }

    public static void closeJDialog(IWindow p) {
        JDialog dlg = dialogStack.popDialog();
        dlg.dispose();
    }

    private static JDialog getJDialog(IWindow p, boolean modal, String title) {
        JDialog nuevo = new JDialog(JUMPWorkbench.getFrameInstance());
        nuevo.setContentPane((JPanel)((Object)p));
        nuevo.setTitle(title);
        nuevo.setModal(modal);
        nuevo.pack();
        return nuevo;
    }
}

