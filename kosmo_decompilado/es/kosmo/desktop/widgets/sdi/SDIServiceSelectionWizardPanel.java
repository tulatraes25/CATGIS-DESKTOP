/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.sdi;

import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.wizard.AbstractWizardPanel;
import com.vividsolutions.jump.workbench.ui.wizard.WizardDialog;
import es.kosmo.desktop.plugins.sdi.ISDIService;
import es.kosmo.desktop.plugins.sdi.LoadSDIServicePlugIn;
import es.kosmo.desktop.plugins.sdi.SDIServiceWizardManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.sdi.wms.WMSService;

public class SDIServiceSelectionWizardPanel
extends AbstractWizardPanel {
    private static final long serialVersionUID = 1L;
    private static final String TITLE = I18N.getString("org.saig.jump.widgets.sdi.SDIServiceSelectionWizardPanel.select-sdi-service-type");
    private static final String INSTRUCTIONS = I18N.getString("org.saig.jump.widgets.sdi.SDIServiceSelectionWizardPanel.select-the-sdi-service-type-to-add");
    public static final String SELECTED_SERVICE_KEY = String.valueOf(SDIServiceSelectionWizardPanel.class.getName()) + " - Selected Service";
    public static final String CURRENT_VIEW_SRS_NAME = String.valueOf(SDIServiceSelectionWizardPanel.class.getName()) + " - Current view srs name";
    private ISDIService selectedService = null;
    private JPanel servicesPanel;
    private ButtonGroup servicesGroup = new ButtonGroup();
    private JPanel decorationPanel;

    public SDIServiceSelectionWizardPanel() {
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(750, 750));
        this.add((Component)this.getDecorationPanel(), "West");
        this.add((Component)this.getServicesPanel(), "Center");
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public String getInstructions() {
        return INSTRUCTIONS;
    }

    @Override
    public void enteredFromLeft(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
        Set<ISDIService> availableServices = SDIServiceWizardManager.getInstance().getRegisteredServices();
        AbstractButton defaultRadioButton = null;
        for (ISDIService service : availableServices) {
            JRadioButton radio = this.generateServiceRadioButton(service);
            if (service.getClass().equals(WMSService.class)) {
                defaultRadioButton = radio;
            }
            this.servicesGroup.add(radio);
            this.servicesPanel.add((Component)radio, null);
        }
        if (defaultRadioButton == null) {
            defaultRadioButton = (JRadioButton)this.servicesGroup.getElements().nextElement();
        }
        defaultRadioButton.doClick();
    }

    @Override
    public void exitingToRight() throws Exception {
        this.dataMap.put(SELECTED_SERVICE_KEY, this.selectedService);
        ((WizardDialog)this.getRootPane().getParent()).setTitle(String.valueOf(LoadSDIServicePlugIn.WIZARD_SDI_DEFAULT_TITLE) + " - " + this.selectedService.getName());
    }

    @Override
    public String getID() {
        return this.getClass().getName();
    }

    @Override
    public String getNextID() {
        if (this.selectedService != null) {
            return SDIServiceWizardManager.getInstance().getInitialWizardPanelForService(this.selectedService);
        }
        return null;
    }

    @Override
    public boolean isInputValid() {
        return this.servicesGroup.getSelection() != null;
    }

    @Override
    public boolean isPanelOk() {
        return this.selectedService != null;
    }

    @Override
    public void add(InputChangedListener listener) {
        if (listener instanceof WizardDialog) {
            ((WizardDialog)listener).setTitle(LoadSDIServicePlugIn.WIZARD_SDI_DEFAULT_TITLE);
        }
        this.inputChangedFirer.add(listener);
    }

    private JPanel getServicesPanel() {
        if (this.servicesPanel == null) {
            this.servicesPanel = new JPanel();
            this.servicesPanel.setLayout(new BoxLayout(this.servicesPanel, 1));
            this.servicesPanel.setPreferredSize(new Dimension(100, 300));
        }
        return this.servicesPanel;
    }

    private JPanel getDecorationPanel() {
        if (this.decorationPanel == null) {
            this.decorationPanel = new JPanel();
            this.decorationPanel.setLayout(new BorderLayout());
            this.decorationPanel.setPreferredSize(new Dimension(190, 300));
            JLabel decorationLabel = new JLabel(IconLoader.icon("toolImages/earth.png"));
            this.decorationPanel.add((Component)decorationLabel, "West");
        }
        return this.decorationPanel;
    }

    private JRadioButton generateServiceRadioButton(final ISDIService service) {
        final JRadioButton serviceRadioButton = new JRadioButton();
        serviceRadioButton.setText(service.getName());
        serviceRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (serviceRadioButton.isSelected()) {
                    SDIServiceSelectionWizardPanel.this.selectedService = service;
                }
            }
        });
        return serviceRadioButton;
    }
}

