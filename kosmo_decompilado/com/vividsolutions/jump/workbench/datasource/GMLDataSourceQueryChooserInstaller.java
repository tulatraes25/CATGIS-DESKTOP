/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jump.io.datasource.StandardReaderWriterFileDataSource;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.InstallStandardDataSourceQueryChoosersPlugIn;
import com.vividsolutions.jump.workbench.datasource.LoadFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.FileNamePanel;
import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.saig.jump.lang.I18N;

public class GMLDataSourceQueryChooserInstaller {
    private static final String GML_DESCRIPTION = "GML 2.0";

    public void addSaveGMLFileDataSourceQueryChooser(PlugInContext context, Blackboard blackboard) {
        DataSourceQueryChooserManager.get(blackboard).addSaveDataSourceQueryChooser(new SaveFileDataSourceQueryChooser(StandardReaderWriterFileDataSource.GML.class, GML_DESCRIPTION, InstallStandardDataSourceQueryChoosersPlugIn.extensions(StandardReaderWriterFileDataSource.GML.class), context.getWorkbenchContext()){
            private FileNamePanel templateFileNamePanel;
            {
                this.templateFileNamePanel = GMLDataSourceQueryChooserInstaller.this.createTemplateFileNamePanel(String.valueOf(I18N.getString("workbench.datasource.GMLDataSourceQueryChooserInstaller.output-template")) + ": ", this.getFileChooserPanel().getChooser(), this.context.getErrorHandler());
            }

            @Override
            public boolean isInputValid() {
                return GMLDataSourceQueryChooserInstaller.this.isValid(this.templateFileNamePanel) && super.isInputValid();
            }

            @Override
            protected Map<String, Object> toProperties(File file) {
                HashMap<String, Object> properties = new HashMap<String, Object>(super.toProperties(file));
                properties.put("Output Template File", this.templateFileNamePanel.getSelectedFile().getPath());
                return properties;
            }

            @Override
            protected Component getSouthComponent1() {
                return this.templateFileNamePanel;
            }
        });
    }

    private boolean isValid(FileNamePanel templateFileNamePanel) {
        if (!templateFileNamePanel.isInputValid()) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(templateFileNamePanel), String.valueOf(I18N.getString("workbench.datasource.GMLDataSourceQueryChooserInstaller.template-file")) + ": " + templateFileNamePanel.getValidationError(), I18N.getString("workbench.datasource.GMLDataSourceQueryChooserInstaller.error"), 0);
            return false;
        }
        return true;
    }

    public void addLoadGMLFileDataSourceQueryChooser(PlugInContext context, Blackboard blackboard) {
        DataSourceQueryChooserManager.get(blackboard).addLoadDataSourceQueryChooser(new LoadFileDataSourceQueryChooser(StandardReaderWriterFileDataSource.GML.class, GML_DESCRIPTION, InstallStandardDataSourceQueryChoosersPlugIn.extensions(StandardReaderWriterFileDataSource.GML.class), context.getWorkbenchContext()){
            private FileNamePanel templateFileNamePanel;
            {
                this.templateFileNamePanel = GMLDataSourceQueryChooserInstaller.this.createTemplateFileNamePanel(String.valueOf(I18N.getString("workbench.datasource.GMLDataSourceQueryChooserInstaller.input-template")) + ": ", this.getFileChooserPanel().getChooser(), this.context.getErrorHandler());
            }

            @Override
            protected void addFileFilters(JFileChooser chooser) {
                super.addFileFilters(chooser);
                InstallStandardDataSourceQueryChoosersPlugIn.addCompressedFileFilter(GMLDataSourceQueryChooserInstaller.GML_DESCRIPTION, chooser);
            }

            @Override
            public boolean isInputValid() {
                return GMLDataSourceQueryChooserInstaller.this.isValid(this.templateFileNamePanel) && super.isInputValid();
            }

            @Override
            protected Map<String, Object> toProperties(File file) {
                HashMap<String, Object> properties = new HashMap<String, Object>(super.toProperties(file));
                properties.put("Input Template File", this.templateFileNamePanel.getSelectedFile().getPath());
                return properties;
            }

            @Override
            protected Component getSouthComponent1() {
                return this.templateFileNamePanel;
            }
        });
    }

    private FileNamePanel createTemplateFileNamePanel(String description, final JFileChooser fileChooser, ErrorHandler errorHandler) {
        return new TemplateFileNamePanel(String.valueOf(I18N.getString("workbench.datasource.GMLDataSourceQueryChooserInstaller.input-template")) + ": ", errorHandler){
            private static final long serialVersionUID = 1L;
            {
                super($anonymous0, $anonymous1);
                this.setFileMustExist(true);
            }

            @Override
            protected File getInitialFile() {
                File initialFile = super.getInitialFile();
                if (!(initialFile.exists() || initialFile.getParent() != null && initialFile.getParentFile().exists())) {
                    return fileChooser.getCurrentDirectory();
                }
                return initialFile;
            }
        };
    }

    private class TemplateFileNamePanel
    extends FileNamePanel {
        private static final long serialVersionUID = 1L;

        public TemplateFileNamePanel(String description, ErrorHandler errorHandler) {
            super(errorHandler);
            this.setUpperDescription("");
            this.setLeftDescription(description);
            this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createMatteBorder(4, 4, 4, 4, this.getBackground())));
        }
    }
}

