/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.sdi.wfs;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.wizard.WizardDialog;
import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import es.kosmo.desktop.plugins.sdi.ISDIService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;
import org.saig.core.model.sdi.wfs.WFSLayer;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.sdi.wfs.WfsLayerBuilder;
import org.saig.jump.widgets.summary.SummaryDialog;
import org.saig.jump.widgets.summary.SummaryMessage;

public class WFSService
implements ISDIService {
    private static final Logger LOGGER = Logger.getLogger(WFSService.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.sdi.wfs.WFSService.WFS-service");
    public static final String DESCRIPTION = I18N.getString("org.saig.jump.plugin.sdi.wfs.WFSService.web-feature-service");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public UndoableCommand loadResults(final PlugInContext context, WizardDialog wd, TaskMonitor monitor) {
        monitor.allowCancellationRequests();
        Object dfc = null;
        AbstractWFSWrapper wrapper = null;
        LayerManager layerManager = context.getLayerManager();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.sdi.wfs.WFSService.loading-selected-WFS-layers")) + "...");
        final ArrayList<WFSLayer> layersToLoad = new ArrayList<WFSLayer>();
        wrapper = (AbstractWFSWrapper)wd.getData("WFS_SERVICE");
        List selectedFeatureTypesInfos = (List)wd.getData("SELECTED_FEATURE_TYPES");
        ArrayList<SummaryMessage> errorMessages = new ArrayList<SummaryMessage>();
        TreeMap<String, List<SummaryMessage>> messageMap = new TreeMap<String, List<SummaryMessage>>();
        int cont = 1;
        int numLayers = selectedFeatureTypesInfos.size();
        Iterator itInfos = selectedFeatureTypesInfos.iterator();
        while (!monitor.isCancelRequested() && itInfos.hasNext()) {
            WFSFeatureTypeInfo currentInfo = (WFSFeatureTypeInfo)itInfos.next();
            monitor.report(cont++, numLayers, I18N.getMessage("org.saig.jump.plugin.sdi.wfs.WFSService.layer-{0}", new Object[]{currentInfo.getTitle()}));
            try {
                WfsLayerBuilder wfsLayerBuilder = new WfsLayerBuilder();
                List<WFSLayer> createWfsLayers = wfsLayerBuilder.createWfsLayers(wrapper, currentInfo, layerManager);
                layersToLoad.addAll(createWfsLayers);
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                errorMessages.add(this.buildWFSLayerErrorMessage(currentInfo.getLocalName(), ex));
            }
        }
        if (!monitor.isCancelRequested() && errorMessages.size() > 0) {
            messageMap.put(I18N.getString("org.saig.jump.plugin.sdi.wfs.WFSService.non-loaded-WFS-layers"), errorMessages);
            this.showErrorSummary(messageMap);
        }
        if (!monitor.isCancelRequested() && layersToLoad.size() > 0) {
            return new UndoableCommand(this.getName()){

                @Override
                public void execute() {
                    Collection<Category> selectedCategories = context.getLayerNamePanel().getSelectedCategories();
                    for (WFSLayer layer : layersToLoad) {
                        context.getLayerManager().addLayerable(selectedCategories.isEmpty() ? StandardCategoryNames.WORKING : selectedCategories.iterator().next().toString(), layer);
                    }
                }

                @Override
                public void unexecute() {
                    for (WFSLayer layer : layersToLoad) {
                        context.getLayerManager().remove(layer);
                    }
                }
            };
        }
        return null;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(WFSService.class)) {
            return false;
        }
        WFSService serv = (WFSService)obj;
        return this.getName().equals(serv.getName()) && this.getDescription().equals(serv.getDescription());
    }

    public int hashCode() {
        int hashCode = 23;
        return hashCode * this.getName().hashCode();
    }

    private void showErrorSummary(Map<String, List<SummaryMessage>> messageMap) {
        SummaryDialog dialog = new SummaryDialog(JUMPWorkbench.getFrameInstance(), true, "Errores cargando capas WFS", messageMap);
        dialog.setVisible(true);
    }

    protected SummaryMessage buildWFSLayerErrorMessage(String wfsLayerName, Exception e) {
        String basicMessage = I18N.getMessage("org.saig.jump.plugin.sdi.wfs.WFSService.the-WFS-layer-{0}-could-not-be-loaded", new Object[]{wfsLayerName});
        String extendedMessage = String.valueOf(I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.error-produced")) + ":\n" + e.getMessage();
        SummaryMessage message = new SummaryMessage(basicMessage, extendedMessage, 2);
        return message;
    }
}

