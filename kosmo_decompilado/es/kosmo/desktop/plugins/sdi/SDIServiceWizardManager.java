/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.sdi;

import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;
import es.kosmo.desktop.plugins.sdi.ISDIService;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SDIServiceWizardManager {
    private static SDIServiceWizardManager manager = new SDIServiceWizardManager();
    private Map<ISDIService, WizardPanel[]> serviceToStartingWizardPanelMap = new HashMap<ISDIService, WizardPanel[]>();

    private SDIServiceWizardManager() {
    }

    public static SDIServiceWizardManager getInstance() {
        return manager;
    }

    public void registerService(ISDIService service, WizardPanel[] wizardPanels) {
        this.serviceToStartingWizardPanelMap.put(service, wizardPanels);
    }

    public String getInitialWizardPanelForService(ISDIService service) {
        WizardPanel[] panels = this.serviceToStartingWizardPanelMap.get(service);
        return panels[0].getID();
    }

    public WizardPanel[] getWizardPanelsForService(ISDIService service) {
        return this.serviceToStartingWizardPanelMap.get(service);
    }

    public Set<ISDIService> getRegisteredServices() {
        return this.serviceToStartingWizardPanelMap.keySet();
    }

    public boolean isRegistered(ISDIService service) {
        return this.serviceToStartingWizardPanelMap.keySet().contains(service);
    }

    public void unregisterService(ISDIService service) {
        if (service != null) {
            this.serviceToStartingWizardPanelMap.remove(service);
        }
    }
}

