/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.xml.Unmarshaller
 */
package org.saig.jump.plugin.simbology;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerSimbology;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.AbstractLoadProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import org.apache.commons.lang.StringUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterAttributeExtractor;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleImpl;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.simbology.SaveLayerSimbologyPlugIn;
import org.saig.jump.util.LoadXMLMappings;
import org.saig.jump.widgets.config.ConfigPathPanel;
import org.saig.jump.widgets.util.DialogFactory;

public class LoadLayerSimbologyPlugIn
extends AbstractLoadProjectPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.load-simbology");
    public static final Icon ICON = IconLoader.icon("loadSimbology.png");
    private File layerSymbologyFile;
    private Layer selectedLayer;
    private JFileChooser fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();
    private boolean warnUser;

    public LoadLayerSimbologyPlugIn() {
        this.warnUser = true;
    }

    public LoadLayerSimbologyPlugIn(boolean warnUser) {
        this.warnUser = warnUser;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.fileChooser.setDialogTitle(NAME);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        this.fileChooser.addChoosableFileFilter(SaveLayerSimbologyPlugIn.LAYER_SIMBOLOGY_FILE_FILTER);
        this.fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        this.fileChooser.setFileFilter(SaveLayerSimbologyPlugIn.LAYER_SIMBOLOGY_FILE_FILTER);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        String defaultPath = (String)PersistentBlackboardPlugIn.get(context.getWorkbenchContext().getBlackboard()).get(ConfigPathPanel.LOAD_SIMBOLOGY_PATH_KEY);
        if (StringUtils.isNotEmpty((String)defaultPath)) {
            this.fileChooser.setCurrentDirectory(new File(defaultPath));
        }
        if (this.fileChooser.showOpenDialog(context.getWorkbenchFrame()) != 0) {
            return false;
        }
        this.selectedLayer = (Layer)context.getLayerNamePanel().getSelectedLayers()[0];
        this.layerSymbologyFile = this.fileChooser.getSelectedFile();
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
        return LoadLayerSimbologyPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public void run(TaskMonitor monitor, final PlugInContext context) throws Exception {
        monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.loading-symbology-file-{0}", new Object[]{this.layerSymbologyFile.getAbsolutePath()}));
        FileReader reader = new FileReader(this.layerSymbologyFile);
        Mapping mappings = LoadXMLMappings.loadLayerSimbologyMappings();
        Unmarshaller unmar = new Unmarshaller(mappings);
        unmar.setClassLoader(context.getWorkbenchContext().getWorkbench().getPlugInManager().getClassLoader());
        unmar.setWhitespacePreserve(true);
        final LayerSimbology simbology = (LayerSimbology)unmar.unmarshal((Reader)reader);
        if (!this.checkGeometryType(this.selectedLayer.getGeometryType(), simbology.getGeometryType())) {
            String errorMessage = String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.the-simbology-stored-is-{0}-type-and-the-layer-is-{1}-type", new Object[]{GUITranslationsUtils.getGeometryName(new Integer(simbology.getGeometryType())), GUITranslationsUtils.getGeometryName(new Integer(this.selectedLayer.getGeometryType()))})) + ".\n" + I18N.getString("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.the-operation-can-not-be-executed");
            DialogFactory.showErrorDialog(context.getWorkbenchFrame(), errorMessage, NAME);
            return;
        }
        if (!this.checkModelStyle(simbology.getModelStyle(), this.selectedLayer)) {
            return;
        }
        Style oldStyle = this.selectedLayer.getModelStyle();
        final Style cloneStyle = (Style)((StyleImpl)oldStyle).clone();
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() {
                LoadLayerSimbologyPlugIn.this.selectedLayer.setModelStyle(simbology.getModelStyle());
                LoadLayerSimbologyPlugIn.this.selectedLayer.setStyles(simbology.getJumpStyles());
                context.getLayerManager().fireLayerChanged(LoadLayerSimbologyPlugIn.this.selectedLayer, LayerEventType.APPEARANCE_CHANGED);
            }

            @Override
            public void unexecute() {
                LoadLayerSimbologyPlugIn.this.selectedLayer.setModelStyle(cloneStyle);
                context.getLayerManager().fireLayerChanged(LoadLayerSimbologyPlugIn.this.selectedLayer, LayerEventType.APPEARANCE_CHANGED);
            }
        }, context);
        if (this.warnUser) {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.symbology-{0}-loaded-successfully", new Object[]{this.layerSymbologyFile.getAbsolutePath()}));
        }
    }

    private boolean checkGeometryType(int layerGeometryType, int simbologyGeometryType) {
        boolean compatibles = false;
        switch (layerGeometryType) {
            case 1: 
            case 8: {
                compatibles = simbologyGeometryType == 1 || simbologyGeometryType == 8;
                break;
            }
            case 2: 
            case 3: {
                compatibles = simbologyGeometryType == 3 || simbologyGeometryType == 2;
                break;
            }
            case 4: 
            case 5: {
                compatibles = simbologyGeometryType == 5 || simbologyGeometryType == 4;
                break;
            }
            default: {
                compatibles = layerGeometryType == simbologyGeometryType;
            }
        }
        return compatibles;
    }

    private boolean checkModelStyle(Style modelStyle, Layer selectedLayer) {
        FeatureTypeStyle[] ftStyles = modelStyle.getFeatureTypeStyles();
        FilterAttributeExtractor attExtractor = new FilterAttributeExtractor();
        FeatureSchema schema = selectedLayer.getFeatureSchema();
        HashSet<String> notFoundAttNames = new HashSet<String>();
        int i = 0;
        while (i < ftStyles.length) {
            FeatureTypeStyle fts = ftStyles[i];
            Rule[] rules = fts.getRules();
            int j = 0;
            while (j < rules.length) {
                Rule rule = rules[j];
                Filter filter = rule.getFilter();
                attExtractor.visit(filter);
                Set<String> attNames = attExtractor.getAttributeNameSet();
                for (String name : attNames) {
                    if (schema.hasAttribute(name)) continue;
                    notFoundAttNames.add(name);
                }
                ++j;
            }
            ++i;
        }
        if (notFoundAttNames.size() > 0) {
            String notFound = "";
            for (String attrName : notFoundAttNames) {
                notFound = String.valueOf(notFound) + "<" + attrName + "> - ";
            }
            notFound = notFound.substring(0, notFound.length() - 2);
            String errorMessage = String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.the-simbology-that-you-want-to-load-uses-fields-that-are-not-in-the-layer-schema", new Object[]{selectedLayer.getName()})) + ":\n " + notFound + "\n" + I18N.getString("org.saig.jump.plugin.utils.LoadLayerSimbologyPlugIn.the-operation-can-not-be-executed");
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), errorMessage, NAME);
            return false;
        }
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayerMustBeActiveCheck());
    }

    @Override
    public void finish(PlugInContext context) {
    }

    @Override
    public Icon getDisabledIcon() {
        return null;
    }

    public File getLayerSymbologyFile() {
        return this.layerSymbologyFile;
    }

    public void setLayerSymbologyFile(File layerSymbologyFile) {
        this.layerSymbologyFile = layerSymbologyFile;
    }

    public Layer getSelectedLayer() {
        return this.selectedLayer;
    }

    public void setSelectedLayer(Layer selectedLayer) {
        this.selectedLayer = selectedLayer;
    }
}

