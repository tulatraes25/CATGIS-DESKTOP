/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.io.FMEGMLReader;
import com.vividsolutions.jump.io.FMEGMLWriter;
import com.vividsolutions.jump.io.JMLReader;
import com.vividsolutions.jump.io.JMLWriter;
import com.vividsolutions.jump.io.JUMPReader;
import com.vividsolutions.jump.io.JUMPWriter;
import com.vividsolutions.jump.io.WKTReader;
import com.vividsolutions.jump.io.WKTWriter;
import com.vividsolutions.jump.io.datasource.StandardReaderWriterFileDataSource;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.GMLDataSourceQueryChooserInstaller;
import com.vividsolutions.jump.workbench.datasource.LoadFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import javax.swing.JFileChooser;
import org.saig.jump.lang.I18N;

public class InstallStandardDataSourceQueryChoosersPlugIn
extends AbstractPlugIn {
    private void addFileDataSourceQueryChoosers(JUMPReader reader, JUMPWriter writer, String description, Class<?> readerWriterDataSourceClass, WorkbenchContext context) {
        DataSourceQueryChooserManager.get(context.getBlackboard()).addLoadDataSourceQueryChooser(new LoadFileDataSourceQueryChooser(readerWriterDataSourceClass, description, InstallStandardDataSourceQueryChoosersPlugIn.extensions(readerWriterDataSourceClass), context){

            @Override
            protected void addFileFilters(JFileChooser chooser) {
                super.addFileFilters(chooser);
                InstallStandardDataSourceQueryChoosersPlugIn.addCompressedFileFilter(this.description, chooser);
            }
        }).addSaveDataSourceQueryChooser(new SaveFileDataSourceQueryChooser(readerWriterDataSourceClass, description, InstallStandardDataSourceQueryChoosersPlugIn.extensions(readerWriterDataSourceClass), context));
    }

    public static String[] extensions(Class<?> readerWriterDataSourceClass) {
        try {
            return ((StandardReaderWriterFileDataSource)readerWriterDataSourceClass.newInstance()).getExtensions();
        }
        catch (Exception e) {
            Assert.shouldNeverReachHere((String)e.toString());
            return null;
        }
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        context.getWorkbenchContext().getWorkbench();
        Blackboard blackboard = JUMPWorkbench.getBlackboard();
        this.addFileDataSourceQueryChoosers(new JMLReader(), new JMLWriter(), "JUMP GML", StandardReaderWriterFileDataSource.JML.class, context.getWorkbenchContext());
        new GMLDataSourceQueryChooserInstaller().addLoadGMLFileDataSourceQueryChooser(context, blackboard);
        new GMLDataSourceQueryChooserInstaller().addSaveGMLFileDataSourceQueryChooser(context, blackboard);
        this.addFileDataSourceQueryChoosers(new WKTReader(), new WKTWriter(), "WKT", StandardReaderWriterFileDataSource.WKT.class, context.getWorkbenchContext());
        this.addFileDataSourceQueryChoosers(new FMEGMLReader(), new FMEGMLWriter(), "FME GML", StandardReaderWriterFileDataSource.FMEGML.class, context.getWorkbenchContext());
    }

    public static void addCompressedFileFilter(String description, JFileChooser chooser) {
        chooser.addChoosableFileFilter(GUIUtil.createFileFilter(I18N.getMessage("workbench.datasource.InstallStandardDataSourceQueryChoosersPlugIn.compressed-{0}", new Object[]{description}), new String[]{"zip", "gz"}));
    }
}

