/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.AbstractSaveProjectPlugIn;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.utils.TemporalLayersInLayerGroupDialog;

public class SaveCategoryPlugIn
extends AbstractSaveProjectPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.SaveCategoryPlugIn.save-category");
    public static final Icon ICON = IconLoader.icon("category_save.png");
    public static final String LAYER_GROUP_FILE_EXTENSION = "slg";
    public static final FileFilter LAYER_GROUP_FILE_FILTER = GUIUtil.createFileFilter(I18N.getString("org.saig.jump.plugin.utils.SaveCategoryPlugIn.collections-file"), new String[]{"slg"});
    private File layerGroupFile;
    private JFileChooser fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.fileChooser.setDialogTitle(NAME);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        this.fileChooser.addChoosableFileFilter(LAYER_GROUP_FILE_FILTER);
        this.fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        this.fileChooser.setFileFilter(LAYER_GROUP_FILE_FILTER);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Collection<Category> selectedCategories = context.getLayerNamePanel().getSelectedCategories();
        if (!selectedCategories.isEmpty()) {
            if (this.fileChooser.showSaveDialog(context.getWorkbenchFrame()) != 0) {
                return false;
            }
            this.layerGroupFile = this.fileChooser.getSelectedFile();
            this.layerGroupFile = FileUtil.addValidExtension(this.layerGroupFile, LAYER_GROUP_FILE_EXTENSION);
            return this.checkTemporalLayers(context);
        }
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return SaveCategoryPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.SaveCategoryPlugIn.saving-collection-file-{0}", new Object[]{this.layerGroupFile.getAbsolutePath()}));
        Collection<Category> categories = context.getLayerNamePanel().getSelectedCategories();
        this.saveLayerGroup(categories, this.layerGroupFile);
        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.SaveCategoryPlugIn.collection-saved-in-{0}", new Object[]{this.layerGroupFile.getAbsolutePath()}));
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNCategoriesMustBeSelectedCheck(1));
    }

    public boolean checkTemporalLayers(PlugInContext context) {
        ArrayList<String> temporalLayerNames = new ArrayList<String>();
        Collection<Category> selectedCategories = context.getLayerNamePanel().getSelectedCategories();
        for (Category currentCategory : selectedCategories) {
            List<Layerable> layers = currentCategory.getLayerables();
            for (Layerable currentLayerable : layers) {
                if (!(currentLayerable instanceof Layer) || ((Layer)currentLayerable).hasReadableDataSource()) continue;
                temporalLayerNames.add(currentLayerable.getName());
            }
        }
        if (temporalLayerNames.size() > 0) {
            TemporalLayersInLayerGroupDialog dialog = new TemporalLayersInLayerGroupDialog(JUMPWorkbench.getFrameInstance(), true, temporalLayerNames);
            dialog.setVisible(true);
            return dialog.wasOkPressed();
        }
        return true;
    }
}

