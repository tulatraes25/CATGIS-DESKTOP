/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.saig.jump.lang.I18N;

public class WrappingOptionPane
extends JOptionPane {
    private static final long serialVersionUID = 1L;
    private static int maxCharactersPerLineCount = 72;

    static {
        UIManager.put("OptionPane.yesButtonText", I18N.getString("org.gvsig.crs.CRSI18NConstants.yes"));
        UIManager.put("OptionPane.noButtonText", I18N.getString("org.gvsig.crs.CRSI18NConstants.no"));
        UIManager.put("OptionPane.cancelButtonText", I18N.getString("workbench.ui.OKCancelPanel.cancel"));
        UIManager.put("OptionPane.okButtonText", I18N.getString("workbench.ui.OKCancelPanel.ok"));
    }

    @Override
    public int getMaxCharactersPerLineCount() {
        return maxCharactersPerLineCount;
    }

    @Override
    public JDialog createDialog(Component parentComponent, String title) throws HeadlessException {
        JDialog dialog = super.createDialog(parentComponent, title);
        if (dialog != null && JUMPWorkbench.APP_ICON != null) {
            dialog.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        }
        return dialog;
    }

    @Override
    public JDialog createDialog(String title) throws HeadlessException {
        JDialog dialog = super.createDialog(title);
        if (dialog != null && JUMPWorkbench.APP_ICON != null) {
            dialog.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        }
        return dialog;
    }
}

