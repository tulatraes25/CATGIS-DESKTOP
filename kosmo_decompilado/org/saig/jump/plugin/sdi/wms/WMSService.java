/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.sdi.wms;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.wizard.WizardDialog;
import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.WMService;
import es.kosmo.desktop.plugins.sdi.ISDIService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.saig.jump.lang.I18N;

public class WMSService
implements ISDIService {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.sdi.wms.WMSService.wms-service");
    public static final String DESCRIPTION = I18N.getString("org.saig.jump.plugin.sdi.wms.WMSService.web-map-service");

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
        WMService service = (WMService)wd.getData("SERVICE");
        List<String> layerNames = this.toLayerNames((List)wd.getData("LAYERS"));
        final WMSLayer layer = new WMSLayer(context.getLayerManager(), service, (String)wd.getData("SRS"), layerNames, (String)wd.getData("FORMAT"), (String)wd.getData("TIME"));
        Number transparencyLevel = (Number)wd.getData("TRANSPARENCY");
        layer.setAlpha(transparencyLevel.intValue());
        layer.setInformationFormat(service.getInformationFormat());
        layer.setInformationFeatureCount(service.getInformationFeatureCount());
        layer.setExceptionFormat(service.getExceptionFormat());
        layer.setUseDeclaredCapabilitiesURLs(service.isUseDeclaredCapabilitiesURLs());
        layer.setServiceVersion(service.getVersion());
        layer.setVendorParameters(service.getVendorParameters());
        layer.setAxisOrder(service.getAxisOrder());
        layer.setBasicAuthData(service.getBasicAuthData());
        return new UndoableCommand(this.getName()){

            @Override
            public void execute() {
                Collection<Category> selectedCategories = context.getLayerNamePanel().getSelectedCategories();
                context.getLayerManager().addLayerable(selectedCategories.isEmpty() ? StandardCategoryNames.WORKING : selectedCategories.iterator().next().toString(), layer);
            }

            @Override
            public void unexecute() {
                context.getLayerManager().remove(layer);
            }
        };
    }

    private List<String> toLayerNames(List<MapLayer> mapLayers) {
        ArrayList<String> names = new ArrayList<String>();
        for (MapLayer layer : mapLayers) {
            names.add(layer.getName());
        }
        return names;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(this.getClass())) {
            return false;
        }
        WMSService serv = (WMSService)obj;
        return this.getName().equals(serv.getName()) && this.getDescription().equals(serv.getDescription());
    }

    public int hashCode() {
        int hashCode = 23;
        return hashCode * this.getName().hashCode();
    }
}

