/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.xml.Unmarshaller
 */
package org.saig.jump.plugin.utils;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryCollection;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.AbstractLoadProjectPlugIn;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.SaveCategoryPlugIn;
import org.saig.jump.util.LoadXMLMappings;
import org.saig.jump.widgets.summary.SummaryDialog;
import org.saig.jump.widgets.summary.SummaryMessage;

public class LoadCategoryPlugIn
extends AbstractLoadProjectPlugIn
implements ThreadedPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.utils.LoadCategoryPlugIn.load-category")) + "...";
    public static final Icon ICON = IconLoader.icon("category_add.png");
    private File layerGroupFile;
    private JFileChooser fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();

    public File getLayerGroupFile() {
        return this.layerGroupFile;
    }

    public void setLayerGroupFile(File layerGroupFile) {
        this.layerGroupFile = layerGroupFile;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.fileChooser.setDialogTitle(NAME);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        this.fileChooser.addChoosableFileFilter(SaveCategoryPlugIn.LAYER_GROUP_FILE_FILTER);
        this.fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        this.fileChooser.setFileFilter(SaveCategoryPlugIn.LAYER_GROUP_FILE_FILTER);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        if (this.fileChooser.showOpenDialog(context.getWorkbenchFrame()) != 0) {
            return false;
        }
        this.layerGroupFile = this.fileChooser.getSelectedFile();
        return true;
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
        return LoadCategoryPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.LoadCategoryPlugIn.loading-collection-file-{0}", new Object[]{this.layerGroupFile}));
        HashMap<String, ArrayList<SummaryMessage>> messageMap = new HashMap<String, ArrayList<SummaryMessage>>();
        FileReader reader = new FileReader(this.layerGroupFile);
        Mapping mappings = LoadXMLMappings.loadLayerGroupMappings();
        Unmarshaller unmar = new Unmarshaller(mappings);
        unmar.setWhitespacePreserve(true);
        CategoryCollection catCollection = (CategoryCollection)unmar.unmarshal((Reader)reader);
        List<Category> categoryList = catCollection.getCategories();
        for (Category cat : categoryList) {
            ArrayList<SummaryMessage> messageList = new ArrayList<SummaryMessage>();
            this.loadCategory(context.getTask(), cat, monitor, messageList, true);
            if (messageList.size() <= 0) continue;
            messageMap.put(cat.getName(), messageList);
        }
        if (messageMap.size() > 0) {
            this.showErrorSummary(messageMap, I18N.getString("org.saig.jump.plugin.utils.LoadCategoryPlugIn.errors-loading-category-group"));
        }
        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.LoadCategoryPlugIn.category-collection-{0}-loaded-successfully", new Object[]{this.layerGroupFile.getName()}));
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
    }

    @Override
    public void finish(PlugInContext context) {
    }

    @Override
    public Icon getDisabledIcon() {
        return null;
    }

    private void showErrorSummary(HashMap messageMap, String categoryName) {
        SummaryDialog dialog = new SummaryDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("org.saig.jump.plugin.utils.LoadCategoryPlugIn.errors-loading-the-category-{0}", new Object[]{categoryName}), messageMap);
        dialog.setVisible(true);
    }
}

