/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.util.LocalConfiguration
 *  org.exolab.castor.xml.Marshaller
 */
package org.saig.jump.widgets.print.actions;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.Marshaller;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LoadXMLMappings;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.actions.SaveAsLayout;
import org.saig.jump.widgets.util.DialogFactory;

public class SaveLayout
extends PrintAction {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.jump.widgets.print.actions.SaveLayout");
    public static final String PRINT_LAYOUT_EXTENSION = "lay";
    public static final FileFilter PRINT_LAYOUT_FILE_FILTER = GUIUtil.createFileFilter(I18N.getString("org.saig.jump.widgets.print.actions.SaveLayout.print-layout-file"), new String[]{"lay"});

    public SaveLayout(PrintLayoutFrame parent) {
        super(parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.frame.getFileName().equals("")) {
            SaveAsLayout saveAs = new SaveAsLayout(this.frame);
            saveAs.actionPerformed(e);
            return;
        }
        File file = new File(this.frame.getFileName());
        StringWriter stringWriter = new StringWriter();
        try {
            try {
                this.frame.getPage().setPageFormat(this.frame.getPageFormat());
                this.frame.getPage().setActiveZoom(this.frame.getActiveZoom());
                Mapping mapping = LoadXMLMappings.loadPrintMappings();
                Properties properties = LocalConfiguration.getInstance().getProperties();
                properties.setProperty("org.exolab.castor.indent", "true");
                Marshaller marshaller = new Marshaller((Writer)stringWriter);
                marshaller.setMapping(mapping);
                marshaller.marshal((Object)this.frame.getPage());
            }
            catch (Exception e2) {
                LOGGER.error((Object)(String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.SaveLayout.error-writing-xml-file")) + ": " + file.toString()), (Throwable)e2);
                DialogFactory.showErrorDialog(this.frame, String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.SaveLayout.error-writing-xml-file")) + ": " + file.toString(), I18N.getString("org.saig.jump.widgets.print.actions.SaveLayout.layout-file-write-error"));
                stringWriter.flush();
                return;
            }
        }
        finally {
            stringWriter.flush();
        }
        try {
            FileUtil.setContents(file.getAbsolutePath(), stringWriter.toString());
        }
        catch (IOException e1) {
            LOGGER.error((Object)(String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.SaveLayout.error-writing-xml-file")) + ": " + file.toString()), (Throwable)e1);
            DialogFactory.showErrorDialog(this.frame, String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.SaveLayout.error-writing-xml-file")) + ": " + file.toString(), I18N.getString("org.saig.jump.widgets.print.actions.SaveLayout.i-o-file-write-error"));
            return;
        }
        DialogFactory.showInformationDialog(this.frame, I18N.getMessage("org.saig.jump.widgets.print.actions.SaveLayout.template-{0}-successfully-saved-in-{1}", new Object[]{this.frame.getName(), file.getAbsolutePath()}), I18N.getString("org.saig.jump.widgets.print.PrintLayoutToolBar.save"));
    }
}

