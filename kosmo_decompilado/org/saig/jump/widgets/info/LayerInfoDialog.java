/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.widgets.info;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.HiperLinkCompound;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.AbstractLayerable;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.wms.WMService;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.gui.swing.locale.TranslatableSelectionDialog;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;
import org.saig.jump.widgets.cts.EPSGSelectionDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class LayerInfoDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(LayerInfoDialog.class);
    private JTextField textFieldNombre;
    private JTextField textFieldTitle;
    private JTextField textFieldProyeccion;
    private JTextField textFieldXMin;
    private JTextField textFieldYMin;
    private JTextField textFieldXMax;
    private JTextField textFieldYMax;
    private JTextField textFieldNumElem;
    private JTextField textFieldTipo;
    private JTextField textFieldWMSFormat;
    private JTextField textFieldWMSTime;
    private JButton langSelecButton;
    private JTextArea textAreaOrigenDatos;
    private JButton buttonProyeccion;
    private OKCancelPanel okCancelPanel;
    private JCheckBox checkBoxEnMemoria;
    private JCheckBox checkBoxActiva;
    private JCheckBox checkBoxWMSTransp;
    private Layerable originalLayer;
    private String originalName = "";
    private boolean isInternal = false;
    private String origenInicial = "";
    private String startingProjectionString = "";
    private String xmaxInicial = "";
    private String xminInicial = "";
    private String ymaxInicial = "";
    private String yminInicial = "";
    private boolean isActiveON;
    private boolean isMemoryON;
    protected int sizeInicial = 0;
    protected IProjection dialogProjection;
    protected Layer layer = null;
    protected WMSLayer wmsLayer = null;
    protected boolean isWMS = false;
    protected PlugInContext context;

    public LayerInfoDialog(JFrame parent, boolean modal, Layerable layerable, PlugInContext context, boolean isWMS) {
        super((Frame)parent, modal);
        this.originalLayer = layerable;
        this.context = context;
        this.isWMS = isWMS;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        DecimalFormat formatter = (DecimalFormat)nf;
        formatter.applyPattern("##0.##");
        String name = null;
        Envelope env = null;
        String type = null;
        if (isWMS) {
            this.wmsLayer = (WMSLayer)this.originalLayer;
            name = this.wmsLayer.getName();
            env = this.wmsLayer.getFullEnvelope();
            type = "WMS - " + this.wmsLayer.getServerURL();
            this.startingProjectionString = this.wmsLayer.getSrs() == null ? EPSGSelectionDialog.NO_SRS_DEFINED : GUITranslationsUtils.getName(this.wmsLayer.getSrs());
            this.isMemoryON = true;
            this.isActiveON = this.wmsLayer.isEnabled();
        } else {
            this.layer = (Layer)this.originalLayer;
            name = this.layer.getName();
            this.isInternal = this.layer.isInternal();
            env = this.layer.getTransformedEnvelope();
            try {
                this.sizeInicial = this.layer.getUltimateFeatureCollectionWrapper().size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.sizeInicial = -1;
            }
            type = LayerUtil.getLayerType(this.layer);
            this.startingProjectionString = this.layer.getProjection() == null ? EPSGSelectionDialog.NO_SRS_DEFINED : GUITranslationsUtils.getCRSDescription(this.layer.getProjection());
            this.isMemoryON = this.layer.isMemory();
            this.isActiveON = this.layer.isEnabled();
        }
        this.originalName = name;
        this.origenInicial = type;
        if (env != null) {
            this.xmaxInicial = formatter.format(env.getMaxX());
            this.xminInicial = formatter.format(env.getMinX());
            this.ymaxInicial = formatter.format(env.getMaxY());
            this.yminInicial = formatter.format(env.getMinY());
        }
        this.setTitle(I18N.getMessage("org.saig.jump.widgets.info.LayerInfoDialog.Layer-{0}-properties", new Object[]{this.originalName}));
        this.setContentPane(this.getMainPanel(layerable));
        this.pack();
    }

    private JPanel getMainPanel(Layerable layerable) {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        FormUtils.addFiller(mainPanel, 0, 0);
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.createCamposPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.createExtensionPanel());
        FormUtils.addRowInGBL(mainPanel, 3, 0, this.createCamposPanel2());
        if (this.isWMS) {
            FormUtils.addRowInGBL(mainPanel, 4, 0, this.createWMSPanel());
        }
        FormUtils.addRowInGBL(mainPanel, 5, 0, this.createOKCancelPanel(layerable));
        FormUtils.addFiller(mainPanel, 6, 0);
        return mainPanel;
    }

    private JComponent createWMSPanel() {
        JPanel wmsPanel = new JPanel(new GridBagLayout());
        wmsPanel.setBorder(BorderFactory.createTitledBorder("WMS"));
        this.textFieldWMSFormat = new JTextField();
        this.textFieldWMSFormat.setText(this.wmsLayer.getFormat());
        this.textFieldWMSFormat.setEditable(false);
        this.textFieldWMSTime = new JTextField();
        this.textFieldWMSTime.setText(this.wmsLayer.getTime());
        this.textFieldWMSTime.setEditable(false);
        this.checkBoxWMSTransp = new JCheckBox();
        this.checkBoxWMSTransp.setSelected(this.wmsLayer.isTransparent());
        this.checkBoxWMSTransp.setEnabled(false);
        FormUtils.addRowInGBL((JComponent)wmsPanel, 0, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Image-format")) + ": ", (JComponent)this.textFieldWMSFormat);
        FormUtils.addRowInGBL((JComponent)wmsPanel, 1, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Time")) + ": ", (JComponent)this.textFieldWMSTime);
        FormUtils.addRowInGBL((JComponent)wmsPanel, 2, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Transparency")) + ": ", (JComponent)this.checkBoxWMSTransp);
        return wmsPanel;
    }

    private JPanel createExtensionPanel() {
        JPanel extensionPanel = new JPanel(new GridBagLayout());
        extensionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Envelope")));
        this.textFieldXMin = new JTextField();
        this.textFieldXMin.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Coordinate-X-minimum-value"));
        this.textFieldXMin.setMinimumSize(new Dimension(75, 20));
        this.textFieldXMin.setPreferredSize(new Dimension(90, 20));
        this.textFieldXMin.setText(this.xminInicial);
        this.textFieldXMin.setEditable(false);
        this.textFieldYMin = new JTextField();
        this.textFieldYMin.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Coordinate-Y-minimum-value"));
        this.textFieldYMin.setMinimumSize(new Dimension(75, 20));
        this.textFieldYMin.setPreferredSize(new Dimension(90, 20));
        this.textFieldYMin.setText(this.yminInicial);
        this.textFieldYMin.setEditable(false);
        this.textFieldXMax = new JTextField();
        this.textFieldXMax.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Coordinate-X-maximum-value"));
        this.textFieldXMax.setMinimumSize(new Dimension(75, 20));
        this.textFieldXMax.setPreferredSize(new Dimension(90, 20));
        this.textFieldXMax.setText(this.xmaxInicial);
        this.textFieldXMax.setEditable(false);
        this.textFieldYMax = new JTextField();
        this.textFieldYMax.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Coordinate-Y-maximum-value"));
        this.textFieldYMax.setMinimumSize(new Dimension(75, 20));
        this.textFieldYMax.setPreferredSize(new Dimension(90, 20));
        this.textFieldYMax.setText(this.ymaxInicial);
        this.textFieldYMax.setEditable(false);
        FormUtils.addRowInGBL((JComponent)extensionPanel, 0, 30, new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Max-Y")) + ":"), (JComponent)this.textFieldYMax, false);
        FormUtils.addRowInGBL((JComponent)extensionPanel, 1, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Min-X")) + ":", (JComponent)this.textFieldXMin, false);
        FormUtils.addFiller(extensionPanel, 1, 2);
        FormUtils.addRowInGBL((JComponent)extensionPanel, 1, 60, (JComponent)this.textFieldXMax, false, false);
        FormUtils.addRowInGBL((JComponent)extensionPanel, 1, 61, (JComponent)new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Max-X")) + ":"), false, false);
        FormUtils.addRowInGBL((JComponent)extensionPanel, 2, 30, new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Min-Y")) + ":"), (JComponent)this.textFieldYMin, false);
        return extensionPanel;
    }

    private JPanel createCamposPanel2() {
        int codTipo;
        JPanel camposPanel2 = new JPanel(new GridBagLayout());
        this.textFieldNumElem = new JTextField();
        this.textFieldNumElem.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Number-of-elements"));
        this.textFieldNumElem.setMinimumSize(new Dimension(50, 20));
        this.textFieldNumElem.setPreferredSize(new Dimension(80, 20));
        this.textFieldNumElem.setText("" + this.sizeInicial);
        this.textFieldNumElem.setEditable(false);
        String nombreTipo = this.isWMS ? new String("WMS") : (this.layer.isRaster() ? new String(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Raster-image")) : ((codTipo = this.layer.getGeometryType()) == 3 ? new String(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.LineString")) : (codTipo == 2 ? new String(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.MultiLineString")) : (codTipo == 8 ? new String(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.MultiPoint")) : (codTipo == 4 ? new String(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.MultiPolygon")) : (codTipo == 1 ? new String(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Point")) : (codTipo == 5 ? new String(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Polygon")) : new String(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Unknown")))))))));
        this.textFieldTipo = new JTextField(nombreTipo);
        this.textFieldTipo.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Type-of-elements"));
        this.textFieldTipo.setPreferredSize(new Dimension(100, 20));
        this.textFieldTipo.setEditable(false);
        this.checkBoxEnMemoria = new JCheckBox();
        this.checkBoxEnMemoria.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Mark-to-load-layer-into-memory"));
        if (this.isWMS) {
            this.checkBoxEnMemoria.setVisible(false);
            this.textFieldNumElem.setVisible(false);
        } else {
            this.checkBoxEnMemoria.setVisible(true);
            this.textFieldNumElem.setVisible(true);
            if (!this.layer.isMemory() && this.layer.getUltimateFeatureCollectionWrapper() instanceof FeatureDataset) {
                this.checkBoxEnMemoria.setSelected(true);
                this.checkBoxEnMemoria.setEnabled(false);
            } else {
                this.checkBoxEnMemoria.setSelected(this.isMemoryON);
                this.checkBoxEnMemoria.setEnabled(!this.isInternal);
            }
        }
        this.checkBoxActiva = new JCheckBox();
        this.checkBoxActiva.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Enable-disable-layer"));
        this.checkBoxActiva.setSelected(this.isActiveON);
        this.checkBoxActiva.setEnabled(!this.isInternal);
        if (this.isWMS) {
            FormUtils.addRowInGBL((JComponent)camposPanel2, 0, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Layer-type")) + ": ", (JComponent)this.textFieldTipo, false);
            FormUtils.addRowInGBL((JComponent)camposPanel2, 0, 2, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Active")) + ": ", (JComponent)this.checkBoxActiva);
        } else {
            FormUtils.addRowInGBL((JComponent)camposPanel2, 0, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Number-of-elements")) + ": ", (JComponent)this.textFieldNumElem, false);
            FormUtils.addRowInGBL((JComponent)camposPanel2, 0, 2, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Layer-type")) + ": ", (JComponent)this.textFieldTipo, false);
            FormUtils.addRowInGBL((JComponent)camposPanel2, 1, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Load-into-memory")) + ": ", (JComponent)this.checkBoxEnMemoria);
            FormUtils.addRowInGBL((JComponent)camposPanel2, 1, 2, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Active")) + ": ", (JComponent)this.checkBoxActiva);
        }
        return camposPanel2;
    }

    private JPanel createCamposPanel() {
        JPanel camposPanel = new JPanel(new GridBagLayout());
        this.textFieldNombre = new JTextField();
        this.textFieldNombre.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Layer-name"));
        this.textFieldNombre.setMinimumSize(new Dimension(100, 20));
        this.textFieldNombre.setPreferredSize(new Dimension(250, 20));
        this.textFieldNombre.setText(this.originalName);
        this.textFieldNombre.setEditable(!this.isInternal);
        this.textFieldProyeccion = new JTextField();
        this.textFieldProyeccion.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Projection"));
        this.textFieldProyeccion.setMinimumSize(new Dimension(100, 20));
        this.textFieldProyeccion.setPreferredSize(new Dimension(250, 20));
        this.textFieldProyeccion.setText(this.startingProjectionString);
        this.textFieldProyeccion.setEditable(false);
        this.textAreaOrigenDatos = new JTextArea();
        this.textAreaOrigenDatos.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Layer-datasource"));
        this.textAreaOrigenDatos.setRows(2);
        this.textAreaOrigenDatos.setLineWrap(true);
        this.textAreaOrigenDatos.setFont(this.textFieldNombre.getFont());
        this.textAreaOrigenDatos.setText(this.origenInicial);
        this.textAreaOrigenDatos.setEditable(false);
        this.textAreaOrigenDatos.setBackground(this.textFieldProyeccion.getBackground());
        JScrollPane areaScrollPane = new JScrollPane(this.textAreaOrigenDatos);
        areaScrollPane.setVerticalScrollBarPolicy(20);
        areaScrollPane.setMinimumSize(new Dimension(250, 40));
        areaScrollPane.setPreferredSize(new Dimension(250, 40));
        this.buttonProyeccion = new JButton(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Choose"));
        this.buttonProyeccion.setEnabled(true);
        this.buttonProyeccion.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Change-Spatial-Reference-System"));
        if (this.isWMS) {
            this.buttonProyeccion.setEnabled(false);
            this.buttonProyeccion.setVisible(false);
        } else if (this.layer.isEditable()) {
            this.buttonProyeccion.setEnabled(false);
            this.buttonProyeccion.setToolTipText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Editable-layers-can-not-be-reprojected"));
        }
        this.buttonProyeccion.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                EPSGSelectionDialog csDialog = new EPSGSelectionDialog(JUMPWorkbench.getFrameInstance(), true, false, LayerInfoDialog.this.dialogProjection);
                if (csDialog.isOk()) {
                    LayerInfoDialog.this.dialogProjection = csDialog.getProjection();
                    LayerInfoDialog.this.textFieldProyeccion.setText(GUITranslationsUtils.getCRSDescription(LayerInfoDialog.this.dialogProjection));
                }
            }
        });
        String layerTitle = null;
        if (this.layer != null) {
            layerTitle = this.layer.getTitle(LocaleManager.getActiveLocale());
        } else if (this.wmsLayer != null) {
            layerTitle = this.wmsLayer.getTitle(LocaleManager.getActiveLocale());
        }
        this.textFieldTitle = new JTextField(layerTitle);
        this.textFieldTitle.setEditable(false);
        this.langSelecButton = new JButton(IconLoader.icon("country.gif"));
        this.langSelecButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                LayerInfoDialog.this.langSelecButtonActionListener();
            }
        });
        FormUtils.addRowInGBL((JComponent)camposPanel, 0, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Name")) + ": ", (JComponent)this.textFieldNombre);
        FormUtils.addRowInGBL((JComponent)camposPanel, 1, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Title")) + ":", (JComponent)this.textFieldTitle);
        FormUtils.addRowInGBL((JComponent)camposPanel, 1, 2, (JComponent)this.langSelecButton, false, true);
        FormUtils.addRowInGBL((JComponent)camposPanel, 3, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Datasource")) + ": ", (JComponent)areaScrollPane);
        FormUtils.addRowInGBL((JComponent)camposPanel, 4, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Projection")) + ": ", (JComponent)this.textFieldProyeccion);
        FormUtils.addRowInGBL((JComponent)camposPanel, 4, 2, (JComponent)this.buttonProyeccion, false, true);
        return camposPanel;
    }

    private JPanel createOKCancelPanel(final Layerable layerable) {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.setAcceptButtonText(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Save-changes"));
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean error = false;
                if (LayerInfoDialog.this.okCancelPanel.wasOKPressed()) {
                    String string_tmp = StringUtils.trimToEmpty((String)LayerInfoDialog.this.textFieldNombre.getText());
                    boolean existe = false;
                    Iterator<? extends Layerable> it = layerable.getLayerManager().getLayerables(Layerable.class).iterator();
                    while (it.hasNext() && !existe) {
                        Layerable layTmp = it.next();
                        if (layerable == layTmp || !string_tmp.equals(layTmp.getName())) continue;
                        existe = true;
                    }
                    if (string_tmp.isEmpty()) {
                        error = true;
                        DialogFactory.showInformationDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.The-new-layer-name-can-not-be-empty"), I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Invalid-name"));
                        LayerInfoDialog.this.textFieldNombre.setText(layerable.getName());
                        LayerInfoDialog.this.textFieldNombre.selectAll();
                        LayerInfoDialog.this.textFieldNombre.requestFocus();
                    } else if (existe) {
                        error = true;
                        DialogFactory.showInformationDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.The-new-name-already-exists-for-other-layer"), I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Invalid-name"));
                        LayerInfoDialog.this.textFieldNombre.setText(string_tmp);
                        LayerInfoDialog.this.textFieldNombre.selectAll();
                        LayerInfoDialog.this.textFieldNombre.requestFocus();
                    } else if (LayerInfoDialog.this.isWMS) {
                        if (!string_tmp.equals(LayerInfoDialog.this.originalName)) {
                            LayerInfoDialog.this.wmsLayer.setName(string_tmp);
                        }
                        boolean newState = LayerInfoDialog.this.checkBoxActiva.isSelected();
                        if (LayerInfoDialog.this.isActiveON != newState) {
                            LayerInfoDialog.this.setActive(newState);
                        }
                    } else {
                        if (!string_tmp.equals(LayerInfoDialog.this.originalName)) {
                            LayerInfoDialog.this.layer.setName(string_tmp);
                        }
                        try {
                            LayerInfoDialog.this.layer.setProjection(LayerInfoDialog.this.dialogProjection);
                            LayerInfoDialog.this.layer.fireAppearanceChanged();
                        }
                        catch (Exception e) {
                            DialogFactory.showErrorDialog(LayerInfoDialog.this, I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.An-error-has-been-produced-while-changing-spatial-reference-system"), I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Error-changing-Spatial-Reference-System"));
                            LOGGER.error((Object)"", (Throwable)e);
                        }
                        boolean newState = LayerInfoDialog.this.checkBoxEnMemoria.isSelected();
                        if (LayerInfoDialog.this.isMemoryON != newState) {
                            LayerInfoDialog.this.layer.setMemory(newState);
                        }
                        newState = LayerInfoDialog.this.checkBoxActiva.isSelected();
                        if (LayerInfoDialog.this.isActiveON != newState) {
                            LayerInfoDialog.this.setActive(newState);
                        }
                    }
                } else {
                    error = false;
                }
                if (!error) {
                    LayerInfoDialog.this.setVisible(false);
                    LayerInfoDialog.this.dispose();
                }
            }
        });
        return this.okCancelPanel;
    }

    protected void setActive(boolean newState) {
        Layerable obj = this.originalLayer;
        if (obj instanceof Layer) {
            this.layer.setEnabled(newState);
            if (newState) {
                try {
                    this.layer.setFeatureCollection(this.executeQuery(this.layer.getDataSourceQuery().getQuery(), this.layer.getDataSourceQuery().getDataSource(), this.layer.getProjection())[0]);
                }
                catch (Exception e1) {
                    LOGGER.error((Object)"", (Throwable)e1);
                }
                FeatureSchema schema = this.layer.getFeatureSchema();
                Map<String, Map<Locale, String>> attributesTranslations = this.layer.getAttributeTranslationsMap();
                Map<String, Boolean> attributesVisibilities = this.layer.getAttributeVisibility();
                int i = 0;
                while (i < schema.getAttributeCount()) {
                    String name = schema.getAttributeName(i);
                    if (attributesTranslations.containsKey(name)) {
                        schema.changeTranslations(name, attributesTranslations.get(name));
                    }
                    if (attributesVisibilities.containsKey(name)) {
                        schema.changeVisibility(name, attributesVisibilities.get(name));
                    }
                    ++i;
                }
                this.layer.setFeatureCollectionModified(false);
                List<Layer> layers = this.context.getTask().getLayerManager().getLayers();
                for (Layer iLayer : layers) {
                    if (!iLayer.isEnabled()) continue;
                    ArrayList<LayerRelation> addRelations = new ArrayList<LayerRelation>();
                    Collection<Relation<?>> relations = iLayer.getAllRelations();
                    for (Relation<?> relation : relations) {
                        LayerRelation layerRelation;
                        if (!(relation instanceof LayerRelation) || !(layerRelation = (LayerRelation)relation).getTargetLayer().equals(this.layer)) continue;
                        addRelations.add(layerRelation);
                    }
                    for (LayerRelation element : addRelations) {
                        try {
                            element.fillValues();
                        }
                        catch (Exception e1) {
                            LOGGER.error((Object)"", (Throwable)e1);
                        }
                        iLayer.addRelation(element);
                    }
                }
                Map<String, Relation<?>> relations = this.layer.getRelations();
                this.layer.setRelations(relations);
                if (this.layer.hashiperLink() && this.layer.getHiperLink() instanceof HiperLinkCompound) {
                    HiperLinkCompound hiper = (HiperLinkCompound)this.layer.getHiperLink();
                    hiper.setTable(this.context.getWorkbenchContext().getDataManager().getTable(hiper.getTable().getName()));
                }
                LayerManager layerManager = this.layer.getLayerManager();
                layerManager.remove(this.layer, false);
                layerManager.addLayerable(this.layer.getOldCategoryName(), this.layer, this.layer.getOldCategoryIndex());
                layerManager.fireLayerChanged(this.layer, LayerEventType.METADATA_CHANGED);
            } else {
                LayerManager layerManager = this.layer.getLayerManager();
                String categoryName = layerManager.getCategory(this.layer).getName();
                int index = layerManager.getCategory(this.layer).indexOf(this.layer);
                layerManager.remove(this.layer, false);
                this.layer.setOldCategoryIndex(index);
                this.layer.setOldCategoryName(categoryName);
                FeatureDataset emptyFC = new FeatureDataset(this.layer.getUltimateFeatureCollectionWrapper().getFeatureSchema());
                this.layer.setFeatureCollection(emptyFC);
                layerManager.addLayerable(StandardCategoryNames.DISABLED, this.layer);
                layerManager.fireLayerChanged(this.layer, LayerEventType.METADATA_CHANGED);
            }
        } else {
            WMSLayer layer = (WMSLayer)obj;
            layer.setEnabled(newState);
            if (newState) {
                try {
                    WMService service = new WMService(layer.getServerURL());
                    service.initialize();
                    if (!service.isInitialized()) {
                        throw new Exception(String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.An-error-has-been-produced-while-connect-to-server")) + service.getServerUrl());
                    }
                    layer.setService(service);
                }
                catch (Exception e1) {
                    LOGGER.error((Object)"", (Throwable)e1);
                }
                LayerManager layerManager = layer.getLayerManager();
                layerManager.remove(layer, false);
                layerManager.addLayerable(layer.getOldCategoryName(), layer, layer.getOldCategoryIndex());
                layerManager.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
            } else {
                LayerManager layerManager = layer.getLayerManager();
                String categoryName = layerManager.getCategory(layer).getName();
                int index = layerManager.getCategory(layer).indexOf(layer);
                layerManager.remove(layer, false);
                layer.setOldCategoryIndex(index);
                layer.setOldCategoryName(categoryName);
                layerManager.addLayerable(StandardCategoryNames.DISABLED, layer);
                layerManager.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
            }
        }
    }

    private FeatureCollection[] executeQuery(String query, DataSource dataSource, IProjection proj) throws Exception {
        Connection connection = dataSource.getConnection();
        try {
            FeatureCollection[] featureCollectionArray = connection.executeQuery(query, proj);
            return featureCollectionArray;
        }
        finally {
            connection.close();
        }
    }

    protected void langSelecButtonActionListener() {
        String layerName = this.isWMS ? this.wmsLayer.getTitle(LocaleManager.getActiveLocale()) : this.layer.getTitle(LocaleManager.getActiveLocale());
        AbstractLayerable layerObj = this.isWMS ? this.wmsLayer : this.layer;
        TranslatableSelectionDialog localeSelDia = new TranslatableSelectionDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("org.saig.jump.widgets.info.LayerInfoDialog.Translations-for-the-layer-{0}-title", new Object[]{layerName}), layerObj);
        if (localeSelDia.isOk()) {
            this.textFieldTitle.setText(layerObj.getTitle(LocaleManager.getActiveLocale()));
        }
    }
}

