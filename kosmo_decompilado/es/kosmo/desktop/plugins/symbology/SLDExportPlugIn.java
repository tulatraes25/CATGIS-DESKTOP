/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.gui.GUIUtil
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.symbology;

import com.iver.cit.gvsig.gui.GUIUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.widgets.symbology.SLDExportOptionsDialog;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.Icon;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.styling.NamedLayerImpl;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleToSLDTransformer;
import org.saig.core.styling.StyledLayer;
import org.saig.core.styling.StyledLayerDescriptor;
import org.saig.core.styling.StyledLayerDescriptorImpl;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class SLDExportPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(SLDExportPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.simbology.ExportSymbologyToSLDFormatPlugIn.export-simbology-to-sld-format");
    public static final Icon ICON = IconLoader.icon("sld_export.png");
    public static final String SLD_FILE_EXTENSION = "sld";
    public static final String SLD_FILE_DESCRIPTION = I18N.getString("org.saig.jump.plugin.simbology.ExportSymbologyToSLDFormatPlugIn.sld-files");
    protected SLDExportOptionsDialog optionsDialog;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layer selectedLayer = (Layer)context.getLayerNamePanel().getSelectedLayers()[0];
        if (this.optionsDialog == null) {
            this.optionsDialog = new SLDExportOptionsDialog(context.getWorkbenchFrame(), true, this.getName(), I18N.getMessage("es.kosmo.desktop.plugins.symbology.SLDExportPlugIn.Select-the-SLD-export-options-for-the-layer-{0}", new Object[]{selectedLayer.getTitle(LocaleManager.getActiveLocale())}), null);
        } else {
            this.optionsDialog.setDescriptionText(I18N.getMessage("es.kosmo.desktop.plugins.symbology.SLDExportPlugIn.Select-the-SLD-export-options-for-the-layer-{0}", new Object[]{selectedLayer.getTitle(LocaleManager.getActiveLocale())}));
        }
        this.optionsDialog.pack();
        GUIUtil.centreOnWindow((Component)this.optionsDialog);
        this.optionsDialog.setVisible(true);
        return this.optionsDialog.wasOkPressed();
    }

    @Override
    public EnableCheck getCheck() {
        return SLDExportPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayerMustBeActiveCheck());
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        Layer selectedLayer = (Layer)context.getLayerNamePanel().getSelectedLayers()[0];
        Style modelStyle = selectedLayer.getModelStyle();
        File sldFile = this.optionsDialog.getSelectedFile();
        String version = this.optionsDialog.getSelectedVersion();
        boolean exportUOMAttributes = this.optionsDialog.exportUOMAttributes();
        monitor.report(I18N.getMessage("org.saig.jump.plugin.simbology.ExportSymbologyToSLDFormatPlugIn.saving-the-sld-file-{0}", new Object[]{sldFile.getAbsolutePath()}));
        FileOutputStream out = null;
        try {
            try {
                out = new FileOutputStream(sldFile);
                StyledLayerDescriptor descriptor = this.buildDescriptorFromStyle(modelStyle);
                StyleToSLDTransformer transformer = new StyleToSLDTransformer(version, false, exportUOMAttributes);
                transformer.setIndentation(4);
                transformer.transform(descriptor, out);
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                DialogFactory.showErrorDialog(context.getWorkbenchFrame(), String.valueOf(I18N.getMessage("org.saig.jump.plugin.simbology.ExportSymbologyToSLDFormatPlugIn.an-error-have-been-produced-while-saving-the-sld-file-{0}", new Object[]{sldFile.getAbsolutePath()})) + " : " + ex.getMessage(), I18N.getString("org.saig.jump.plugin.simbology.ExportSymbologyToSLDFormatPlugIn.error-while-saving-the-sld-file"));
                if (out != null) {
                    out.close();
                }
                return;
            }
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.SaveLayerSimbology.simbology-saved-in-the-file-{0}", new Object[]{sldFile.getAbsolutePath()}));
    }

    private StyledLayerDescriptor buildDescriptorFromStyle(Style style) {
        StyledLayerDescriptorImpl descriptor = new StyledLayerDescriptorImpl();
        descriptor.setName(style.getName());
        descriptor.setTitle(style.getTitle());
        String abstractStr = style.getAbstract();
        if (StringUtils.isEmpty((String)abstractStr)) {
            abstractStr = "Abstract";
        }
        descriptor.setAbstract(abstractStr);
        NamedLayerImpl namedLayer = new NamedLayerImpl();
        namedLayer.setName(style.getName());
        namedLayer.addStyle(style);
        descriptor.setStyledLayers(new StyledLayer[]{namedLayer});
        return descriptor;
    }
}

