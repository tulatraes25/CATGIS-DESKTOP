/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.xml.Unmarshaller
 */
package org.saig.jump.widgets.print.actions;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import javax.swing.JFileChooser;
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LoadXMLMappings;
import org.saig.jump.widgets.print.Page;
import org.saig.jump.widgets.print.PreviewPanel;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.actions.SaveLayout;
import org.saig.jump.widgets.util.DialogFactory;

public class OpenLayout
extends PrintAction {
    private JFileChooser fileChooser;
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.jump.widgets.print.actions.OpenLayout");

    public OpenLayout(PrintLayoutFrame parent) {
        super(parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int res = DialogFactory.showYesNoCancelDialog(this.frame, I18N.getString("org.saig.jump.widgets.print.actions.OpenLayout.do-you-want-to-save-the-current-layout"), I18N.getString("org.saig.jump.widgets.print.actions.OpenLayout.save-current-layout"));
        if (res == 0) {
            SaveLayout save = new SaveLayout(this.frame);
            save.actionPerformed(e);
        } else if (res == 2) {
            return;
        }
        this.fileChooser = new JFileChooser();
        this.fileChooser.setFileSelectionMode(0);
        this.fileChooser.setMultiSelectionEnabled(false);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        this.fileChooser.addChoosableFileFilter(SaveLayout.PRINT_LAYOUT_FILE_FILTER);
        this.fileChooser.setFileFilter(SaveLayout.PRINT_LAYOUT_FILE_FILTER);
        if (this.fileChooser.showOpenDialog(this.frame) == 0) {
            File file = this.fileChooser.getSelectedFile();
            file = FileUtil.addExtensionIfNone(file, "lay");
            FileReader reader = null;
            try {
                reader = new FileReader(file);
            }
            catch (FileNotFoundException e1) {
                LOGGER.error((Object)(String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.OpenLayout.file-not-found")) + ": " + file.toString()));
                DialogFactory.showErrorDialog(this.frame, String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.OpenLayout.file-not-found")) + ": " + file.toString(), I18N.getString("org.saig.jump.widgets.print.actions.OpenLayout.layout-file-read-error"));
                return;
            }
            Page page = null;
            try {
                Mapping mapping = LoadXMLMappings.loadPrintMappings();
                Unmarshaller unmar = new Unmarshaller(mapping);
                page = (Page)unmar.unmarshal((Reader)reader);
            }
            catch (Exception e2) {
                LOGGER.error((Object)(String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.OpenLayout.error-reading-xml-file")) + ": " + file.toString()), (Throwable)e2);
                DialogFactory.showErrorDialog(this.frame, String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.OpenLayout.error-reading-xml-file")) + ": " + file.toString(), I18N.getString("org.saig.jump.widgets.print.actions.OpenLayout.layout-file-read-error"));
                return;
            }
            if (page != null) {
                this.frame.setPage(page);
                this.frame.setPageFormat(page.getPageFormat());
                this.frame.setActiveZoom(page.getActiveZoom());
                page.initPage();
                PreviewPanel preview = new PreviewPanel(this.frame, page);
                this.frame.getPrintLayoutPreviewPanel().setPreview(preview);
                int i = 1;
                while (i <= page.getGraphicElements().size()) {
                    this.frame.setGraphic(page.getGraphicElements().get(i - 1));
                    this.frame.getPage().posGraphicElement(this.frame);
                    ++i;
                }
                page.resize(this.frame.getPageFormat());
            }
            this.frame.setFileName(file.getAbsolutePath());
            this.frame.repaint();
            DialogFactory.showInformationDialog(this.frame, I18N.getMessage("org.saig.jump.widgets.print.actions.OpenLayout.template-{0}-successfully-loaded-from-{1}", new Object[]{this.frame.getName(), file.getAbsolutePath()}), I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.open"));
        }
    }
}

