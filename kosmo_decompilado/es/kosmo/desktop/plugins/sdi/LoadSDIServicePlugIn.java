/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.sdi;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerWizardPanel;
import com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel;
import com.vividsolutions.jump.workbench.ui.wizard.WizardDialog;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;
import es.kosmo.desktop.plugins.sdi.ISDIService;
import es.kosmo.desktop.plugins.sdi.SDIServiceWizardManager;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import es.kosmo.desktop.widgets.sdi.SDIServiceSelectionWizardPanel;
import es.kosmo.desktop.widgets.sdi.wfs.WFSAttributeSelectionWizardPanel;
import es.kosmo.desktop.widgets.sdi.wfs.WFSFeatureTypeSelectionWizardPanel;
import es.kosmo.desktop.widgets.sdi.wfs.WFSOptionsWizardPanel;
import es.kosmo.desktop.widgets.sdi.wfs.WFSURLWizardPanel;
import es.kosmo.desktop.widgets.sdi.wms.WMSURLWizardPanel;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.sdi.wfs.WFSService;
import org.saig.jump.plugin.sdi.wms.WMSService;

public class LoadSDIServicePlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.AddWMSQueryPlugIn.Load-IDE-Service")) + "...";
    public static final Icon ICON = IconLoader.icon("ide.png");
    public static final String WIZARD_SDI_DEFAULT_TITLE = I18N.getString("org.saig.jump.plugin.sdi.LoadSDIServicePlugIn.add-new-sdi-service");
    private WizardDialog wd;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.wd = new WizardDialog((Frame)context.getWorkbenchFrame(), WIZARD_SDI_DEFAULT_TITLE, context.getErrorHandler());
        this.registerDefaultSDIServices(this.wd);
        this.wd.init(this.getAllServicePanels());
        this.wd.setData(SDIServiceSelectionWizardPanel.CURRENT_VIEW_SRS_NAME, GUITranslationsUtils.getCRSDescription(context.getTask().getProjection()));
        GUIUtil.centreOnWindow(this.wd);
        this.wd.setVisible(true);
        return this.wd.wasFinishPressed();
    }

    private WizardPanel[] getAllServicePanels() {
        ArrayList<WizardPanel> wizardPanels = new ArrayList<WizardPanel>();
        wizardPanels.add(new SDIServiceSelectionWizardPanel());
        Set<ISDIService> registeredServices = SDIServiceWizardManager.getInstance().getRegisteredServices();
        for (ISDIService currentService : registeredServices) {
            wizardPanels.addAll(Arrays.asList(SDIServiceWizardManager.getInstance().getWizardPanelsForService(currentService)));
        }
        return wizardPanels.toArray(new WizardPanel[0]);
    }

    private void registerDefaultSDIServices(WizardDialog wd) {
        WMSService wmsService = new WMSService();
        WizardPanel[] panels = new WizardPanel[]{new WMSURLWizardPanel(), new MapLayerWizardPanel(), new SRSWizardPanel(wd)};
        SDIServiceWizardManager.getInstance().registerService(wmsService, panels);
        WFSService wfsService = new WFSService();
        WizardPanel[] wfsPanels = new WizardPanel[]{new WFSURLWizardPanel(), new WFSFeatureTypeSelectionWizardPanel(), new WFSAttributeSelectionWizardPanel(), new WFSOptionsWizardPanel()};
        SDIServiceWizardManager.getInstance().registerService(wfsService, wfsPanels);
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
        return LoadSDIServicePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createTaskWindowMustBeActiveCheck());
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        ISDIService service = (ISDIService)this.wd.getData(SDIServiceSelectionWizardPanel.SELECTED_SERVICE_KEY);
        monitor.report(String.valueOf(I18N.getMessage("org.saig.jump.plugin.sdi.LoadSDIServicePlugIn.loading-{0}", new Object[]{service.getName()})) + "...");
        this.execute(service.loadResults(context, this.wd, monitor), context);
    }
}

