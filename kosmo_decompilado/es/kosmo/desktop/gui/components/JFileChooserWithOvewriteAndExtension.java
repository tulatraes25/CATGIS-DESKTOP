/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.io.File;
import javax.swing.JFileChooser;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class JFileChooserWithOvewriteAndExtension
extends JFileChooser {
    private static final long serialVersionUID = 1L;
    private String defaultExtension;

    public JFileChooserWithOvewriteAndExtension(String defaultExtension) {
        this.defaultExtension = defaultExtension;
    }

    @Override
    public void approveSelection() {
        int response;
        if (GUIUtil.selectedFiles(this).length != 1) {
            return;
        }
        File selectedFile = GUIUtil.selectedFiles(this)[0];
        if (this.defaultExtension != null) {
            selectedFile = FileUtil.addExtensionIfNone(selectedFile, this.defaultExtension);
            this.setSelectedFile(selectedFile);
        }
        if (selectedFile.exists() && !selectedFile.isFile()) {
            return;
        }
        if (selectedFile.exists() && (response = DialogFactory.showYesNoDialog(this, I18N.getMessage("workbench.ui.GUIUtil.the-file-{0}-already-exists-do-you-want-to-replace-the-existing-file", new Object[]{selectedFile.getName()}), I18N.getString("org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn.Overwrite-existing-files"))) != 0) {
            return;
        }
        super.approveSelection();
    }
}

