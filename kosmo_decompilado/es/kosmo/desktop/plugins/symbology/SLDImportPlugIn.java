/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 *  org.jfree.ui.FilesystemFilter
 */
package es.kosmo.desktop.plugins.symbology;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.plugins.symbology.SLDExportPlugIn;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.jfree.ui.FilesystemFilter;
import org.saig.core.styling.SLDParser;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.StyleFactoryImpl;
import org.saig.core.styling.StyleImpl;
import org.saig.jump.lang.I18N;

public class SLDImportPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.simbology.SLDImportPlugIn.Import-symbology-from-SLD-file");
    public static final Icon ICON = IconLoader.icon("sld_import.png");
    private JFileChooser fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();
    protected static final FileFilter SLD_FILE_FILTER = new FilesystemFilter("sld", SLDExportPlugIn.SLD_FILE_DESCRIPTION);

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.fileChooser.setDialogTitle(NAME);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        this.fileChooser.addChoosableFileFilter(SLD_FILE_FILTER);
        this.fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        this.fileChooser.setFileFilter(SLD_FILE_FILTER);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        if (this.fileChooser.showOpenDialog(context.getWorkbenchFrame()) != 0) {
            return false;
        }
        File sldFile = this.fileChooser.getSelectedFile();
        final Layer selectedLayer = (Layer)context.getLayerNamePanel().getSelectedLayers()[0];
        SLDParser parser = new SLDParser((StyleFactory)new StyleFactoryImpl(), selectedLayer.getFeatureSchema());
        parser.setInput(sldFile);
        Object[] styles = parser.readXML();
        if (!ArrayUtils.isEmpty((Object[])styles)) {
            Object modelStyle = styles[0];
            Style oldStyle = selectedLayer.getModelStyle();
            Style cloneStyle = (Style)((StyleImpl)oldStyle).clone();
            selectedLayer.setModelStyle((Style)modelStyle);
            this.execute(new UndoableCommand(this.getName(), (Style)modelStyle, context, cloneStyle){
                private final /* synthetic */ Style val$modelStyle;
                private final /* synthetic */ PlugInContext val$context;
                private final /* synthetic */ Style val$cloneStyle;
                {
                    this.val$modelStyle = style;
                    this.val$context = plugInContext;
                    this.val$cloneStyle = style2;
                    super($anonymous0);
                }

                @Override
                public void execute() {
                    selectedLayer.setModelStyle(this.val$modelStyle);
                    selectedLayer.fireAppearanceChanged();
                    this.val$context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.simbology.SLDImportPlugIn.The-layer-{0}-symbology-has-been-successfully-loaded", new Object[]{selectedLayer.getName()}));
                }

                @Override
                public void unexecute() {
                    selectedLayer.setModelStyle(this.val$cloneStyle);
                    selectedLayer.fireAppearanceChanged();
                }
            }, context);
        } else {
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.simbology.SLDImportPlugIn.No-styles-could-be-recovered-from-the-SLD-file"));
        }
        return true;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return SLDImportPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory cf = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck solucion = new MultiEnableCheck();
        solucion.add(cf.createTaskWindowMustBeActiveCheck());
        solucion.add(cf.createAtLeastNLayersMustExistCheck(1));
        solucion.add(cf.createExactlyNLayersMustBeSelectedCheck(1));
        return solucion;
    }
}

