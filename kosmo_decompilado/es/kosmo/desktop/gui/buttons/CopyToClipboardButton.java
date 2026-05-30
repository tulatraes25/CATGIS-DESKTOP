/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.desktop.gui.buttons;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.DummyClipboardOwner;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;

public class CopyToClipboardButton
extends JButton {
    private static final long serialVersionUID = 1L;
    protected JTextField textField;

    public CopyToClipboardButton(JTextField linkedTextField, String tooltipText) {
        this.textField = linkedTextField;
        this.setIcon(IconLoader.icon("copy.gif"));
        this.setToolTipText(tooltipText);
    }

    protected String getValue() {
        return StringUtils.trim((String)this.textField.getText());
    }

    public void copyToClipboard() {
        String value = this.getValue();
        if (!StringUtils.isEmpty((String)value)) {
            StringSelection selection = new StringSelection(this.getValue());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, new DummyClipboardOwner());
        }
    }
}

