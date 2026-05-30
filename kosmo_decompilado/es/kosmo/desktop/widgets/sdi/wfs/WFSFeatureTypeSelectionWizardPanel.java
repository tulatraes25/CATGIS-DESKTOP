/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.ogcwebservices.wfs.capabilities.FormatType
 *  org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.addremove.ButtonCustomAddRemovePanel;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveListModel;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import com.vividsolutions.jump.workbench.ui.wizard.AbstractWizardPanel;
import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import es.kosmo.desktop.widgets.sdi.wfs.WFSAttributeSelectionWizardPanel;
import es.kosmo.desktop.widgets.sdi.wfs.WFSFeatureTypeDescription;
import es.kosmo.desktop.widgets.sdi.wfs.WFSFeatureTypeDescriptionListCellRenderer;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URI;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;

public class WFSFeatureTypeSelectionWizardPanel
extends AbstractWizardPanel
implements InputChangedListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(WFSFeatureTypeSelectionWizardPanel.class);
    public static final String SELECTED_FEATURE_TYPES_KEY = "SELECTED_FEATURE_TYPES";
    private AbstractWFSWrapper wfsService;
    protected ButtonCustomAddRemovePanel<WFSFeatureTypeDescription> addRemovePanel;

    public WFSFeatureTypeSelectionWizardPanel() {
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());
        this.addRemovePanel = new ButtonCustomAddRemovePanel(false);
        this.addRemovePanel.setLeftText(I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeSelectionWizardPanel.available-feature-types"));
        this.addRemovePanel.setRightText(I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeSelectionWizardPanel.feature-types-to-load"));
        ((DefaultAddRemoveList)this.addRemovePanel.getLeftList()).getList().setCellRenderer(new WFSFeatureTypeDescriptionListCellRenderer());
        ((DefaultAddRemoveList)this.addRemovePanel.getRightList()).getList().setCellRenderer(new WFSFeatureTypeDescriptionListCellRenderer());
        ((DefaultAddRemoveListModel)this.addRemovePanel.getLeftList().getModel()).setSorted(true);
        ((DefaultAddRemoveListModel)this.addRemovePanel.getRightList().getModel()).setSorted(true);
        this.addRemovePanel.add(this);
        this.addRemovePanel.setMinimumSize(new Dimension(500, 400));
        this.addRemovePanel.setPreferredSize(new Dimension(500, 400));
        FormUtils.addRowInGBL(this, 1, 0, this.addRemovePanel);
        FormUtils.addFiller(this, 2, 0);
    }

    @Override
    public String getID() {
        return this.getClass().getName();
    }

    @Override
    public String getNextID() {
        return WFSAttributeSelectionWizardPanel.class.getName();
    }

    @Override
    public String getInstructions() {
        return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeSelectionWizardPanel.select-what-feature-types-you-want-to-load");
    }

    @Override
    public String getTitle() {
        return I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeSelectionWizardPanel.select-feature-types");
    }

    @Override
    public void enteredFromLeft(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
        this.wfsService = (AbstractWFSWrapper)dataMap.get("WFS_SERVICE");
        ((DefaultListModel)((DefaultAddRemoveListModel)this.addRemovePanel.getLeftList().getModel()).getListModel()).clear();
        ((DefaultListModel)((DefaultAddRemoveListModel)this.addRemovePanel.getRightList().getModel()).getListModel()).clear();
        try {
            String[] featTypeNames = this.wfsService.getFeatureTypes();
            Arrays.sort(featTypeNames, Collator.getInstance(LocaleManager.getActiveLocale()));
            ArrayList<WFSFeatureTypeDescription> wfsFeatureTypeDescriptionList = new ArrayList<WFSFeatureTypeDescription>(featTypeNames.length);
            int i = 0;
            while (i < featTypeNames.length) {
                String currentName = featTypeNames[i];
                WFSFeatureType ft = this.wfsService.getFeatureTypeByName(currentName);
                WFSFeatureTypeDescription ftDesc = new WFSFeatureTypeDescription(ft);
                wfsFeatureTypeDescriptionList.add(ftDesc);
                ++i;
            }
            this.addRemovePanel.getLeftList().getModel().setItems(wfsFeatureTypeDescriptionList);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    public void exitingToRight() throws Exception {
        final TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
        final List selectedFeatureTypes = this.addRemovePanel.getRightItems();
        final ArrayList infos = new ArrayList();
        progressDialog.allowCancellationRequests();
        progressDialog.setTitle(I18N.getString("ui.plugin.wms.URLWizardPanel.connecting"));
        progressDialog.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            try {
                                String loadSchema = String.valueOf(I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeSelectionWizardPanel.Loading-feature-type-attribute-schemas")) + "...";
                                progressDialog.report(loadSchema);
                                LOGGER.info((Object)loadSchema);
                                int total = selectedFeatureTypes.size();
                                int num = 0;
                                Iterator iterator = selectedFeatureTypes.iterator();
                                while (!progressDialog.isCancelRequested() && iterator.hasNext()) {
                                    WFSFeatureTypeDescription currentFeatTypeDescription = (WFSFeatureTypeDescription)iterator.next();
                                    progressDialog.report(num++, total, I18N.getMessage("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeSelectionWizardPanel.Feature-type-{0}", new Object[]{currentFeatTypeDescription.getTitle()}));
                                    LOGGER.info((Object)I18N.getMessage("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeSelectionWizardPanel.Loading-attribute-schema-for-the-feature-type-{0}", new Object[]{currentFeatTypeDescription.getTitle()}));
                                    WFSFeatureTypeInfo info = WFSFeatureTypeSelectionWizardPanel.this.loadSelectedFeatureType(currentFeatTypeDescription.getName());
                                    infos.add(info);
                                }
                                if (progressDialog.isCancelRequested()) {
                                    infos.clear();
                                }
                            }
                            catch (Exception e) {
                                LOGGER.error((Object)"", (Throwable)e);
                                progressDialog.setExceptionMessage(e.getMessage());
                                progressDialog.setVisible(false);
                                return;
                            }
                        }
                        finally {
                            progressDialog.setVisible(false);
                        }
                    }
                }).start();
            }
        });
        GUIUtil.centre(progressDialog, this.getRootPane().getParent());
        progressDialog.setVisible(true);
        this.dataMap.put(SELECTED_FEATURE_TYPES_KEY, infos);
    }

    @Override
    public boolean isInputValid() {
        return !this.addRemovePanel.getRightItems().isEmpty();
    }

    @Override
    public boolean isPanelOk() {
        return !this.addRemovePanel.getRightItems().isEmpty();
    }

    protected WFSFeatureTypeInfo loadSelectedFeatureType(String selectedFeatureTypeName) {
        String baseDefaultPkName;
        WFSFeatureType ft = this.wfsService.getFeatureTypeByName(selectedFeatureTypeName);
        Object[] attributeNames = this.wfsService.getProperties(selectedFeatureTypeName);
        Arrays.sort(attributeNames);
        ArrayList<String> attributeNamesList = new ArrayList<String>();
        Object[] geoProperties = this.wfsService.getGeometryProperties(selectedFeatureTypeName);
        ArrayList<QualifiedName> geoPropertiesList = new ArrayList<QualifiedName>();
        if (attributeNames != null) {
            CollectionUtils.addAll(attributeNamesList, (Object[])attributeNames);
        }
        if (geoProperties != null) {
            CollectionUtils.addAll(geoPropertiesList, (Object[])geoProperties);
        }
        WFSFeatureTypeInfo info = new WFSFeatureTypeInfo(ft.getName(), attributeNamesList, geoPropertiesList, ft.getTitle());
        info.setEnabled(CollectionUtils.isNotEmpty(geoPropertiesList));
        Object[] formats = ft.getOutputFormats();
        if (ArrayUtils.isEmpty((Object[])formats)) {
            formats = new FormatType[]{this.wfsService.getServiceVersion().equals("1.0.0") ? new FormatType(null, null, null, "GML2") : new FormatType(null, null, null, "text/xml; subtype=gml/3.1.1")};
        }
        info.setAvailableFormats((FormatType[])formats);
        if (formats != null && formats.length == 1) {
            info.setSelectedFormat((FormatType)formats[0]);
        }
        URI defaultSRS = ft.getDefaultSRS();
        Object[] otherSRSs = ft.getOtherSrs();
        TreeSet<Object> srs = new TreeSet<Object>();
        if (defaultSRS != null) {
            srs.add(defaultSRS);
        }
        if (!ArrayUtils.isEmpty((Object[])otherSRSs)) {
            int i = 0;
            while (i < otherSRSs.length) {
                srs.add(otherSRSs[i]);
                ++i;
            }
        }
        URI[] uriArray = new URI[srs.size()];
        info.setAvailableSRS(srs.toArray(uriArray));
        if (defaultSRS != null) {
            info.setSelectedSRS(defaultSRS);
        } else if (uriArray != null && uriArray.length == 1) {
            info.setSelectedSRS(uriArray[0]);
        }
        String candidatePkName = baseDefaultPkName = "gid";
        int cont = 1;
        while (attributeNamesList.contains(candidatePkName)) {
            candidatePkName = String.valueOf(baseDefaultPkName) + "_" + cont++;
        }
        info.setPkName(candidatePkName);
        return info;
    }

    @Override
    public void inputChanged() {
        this.inputChangedFirer.fire();
    }
}

