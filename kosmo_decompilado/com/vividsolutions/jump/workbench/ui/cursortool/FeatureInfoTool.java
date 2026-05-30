/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.wms.FeatureInfoRequest;
import es.kosmo.desktop.utils.DesktopUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;
import org.saig.jump.widgets.info.FeatureInfoDialog;
import sun.misc.BASE64Encoder;

public class FeatureInfoTool
extends SpecifyFeaturesTool {
    private static final Logger LOGGER = Logger.getLogger(FeatureInfoTool.class);
    public static final String VISIBLE_LAYERS_KEY = String.valueOf(FeatureInfoTool.class.getName()) + " - VISIBLE_LAYERS";
    public static final String CONFIG_INFO_KEY = String.valueOf(FeatureInfoTool.class.getName()) + " - CONFIG_INFO";
    public static final String NAME = I18N.getString("workbench.ui.cursortool.FeatureInfoTool.name");
    public static final Icon ICON = IconLoader.icon("Info.gif");
    public static final Cursor CURSOR = FeatureInfoTool.createCursor(IconLoader.icon("InfoCursor.gif").getImage());
    private FeatureInfoDialog dialog;

    public FeatureInfoTool() {
        this.setColor(Color.magenta);
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.cancelGesture();
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
    public Cursor getCursor() {
        return CURSOR;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        hasSelectedFeatures = false;
        hasWMS = false;
        infoFrame = this.getTaskFrame().getInfoFrame();
        if (!this.wasShiftPressed()) {
            infoFrame.getModel().clear();
        }
        allVisibles = PersistentBlackboardPlugIn.get(this.getWorkbench().getContext()).get(FeatureInfoTool.VISIBLE_LAYERS_KEY, true);
        layers = null;
        layersList = null;
        if (!allVisibles) {
            layers = this.getWorkbench().getContext().getLayerNamePanel().getSelectedLayers();
        } else {
            layersList = this.getWorkbench().getContext().getLayerManager().getVisibleLayers(true);
            layersList.addAll(this.getWorkbench().getContext().getLayerManager().getVisibleWMSLayers());
            layers = layersList.toArray();
        }
        map = this.layerToSpecifiedFeaturesMap(layers);
        isTable = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(FeatureInfoTool.CONFIG_INFO_KEY, true);
        if (isTable || layers == null || layers.length <= 0) ** GOTO lbl39
        i = 0;
        while (i < layers.length) {
            if (layers[i] instanceof WMSLayer && LayerUtil.isQueryable((WMSLayer)layers[i])) {
                this.processWMSLayer((WMSLayer)layers[i]);
                hasWMS = true;
            }
            ++i;
        }
        if (map.size() > 0) {
            if (this.dialog == null) {
                this.dialog = new FeatureInfoDialog(JUMPWorkbench.getFrameInstance(), false, map, layers, this.getWorkbench().getContext().getLayerViewPanel());
            } else {
                this.dialog.refresh(map, layers);
                if (!this.dialog.isVisible()) {
                    this.dialog.setVisible(true);
                }
            }
        } else {
            if (this.dialog != null) {
                this.dialog.setVisible(false);
            }
            this.getWorkbench().getFrame().warnUser(I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool.It-does-not-exist-information-in-the-selected-point"));
            return;
lbl39:
            // 1 sources

            i = 0;
            while (i < layers.length) {
                if (layers[i] instanceof WMSLayer) {
                    if (LayerUtil.isQueryable((WMSLayer)layers[i])) {
                        this.processWMSLayer((WMSLayer)layers[i]);
                        hasWMS = true;
                    }
                } else {
                    layer = (Layer)layers[i];
                    if (!layer.isRaster() && (features = map.get(layer)) != null) {
                        hasSelectedFeatures = true;
                        infoFrame.getModel().add(layer, features);
                    }
                }
                ++i;
            }
            if (hasSelectedFeatures) {
                dim = ((AttributeTab)infoFrame.getAttributeTab()).getTableSize();
                parentBounds = this.getWorkbench().getFrame().getDesktopPane().getBounds();
                infoHeight = 266;
                desiredInfoWidth = Math.min(parentBounds.width, dim.width + 57);
                if (desiredInfoWidth <= (toolbarWidth = (int)((AttributeTab)infoFrame.getAttributeTab()).getToolBar().getPreferredSize().getWidth())) {
                    infoFrame.setSize(toolbarWidth + 50, infoHeight);
                } else {
                    infoFrame.setSize(desiredInfoWidth, infoHeight);
                }
                infoFrame.setLocation(0, Math.max(0, parentBounds.height - infoHeight));
                infoFrame.surface();
            } else if (!hasWMS) {
                this.getWorkbench().getFrame().warnUser(I18N.getString("workbench.ui.cursortool.FeatureInfoTool.you-have-not-clicked-over-a-layer-element"));
            }
        }
    }

    private void processWMSLayer(WMSLayer wmsLayer) throws Exception {
        Point2D source = this.getSource();
        FeatureInfoRequest ir = wmsLayer.createFeatureInfoRequest((int)source.getX(), (int)source.getY(), this.getWorkbench().getContext().getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates(), this.getWorkbench().getContext().getLayerViewPanel().getWidth(), this.getWorkbench().getContext().getLayerViewPanel().getHeight());
        String infoFormat = ir.getFormat();
        URL infoURL = ir.getURL();
        if (StringUtils.contains((String)infoFormat, (String)"html")) {
            DesktopUtils.browse(infoURL);
        } else {
            try {
                String type;
                URLConnection conn = infoURL.openConnection();
                if (ir.getService().getBasicAuthData() != null) {
                    String userPassword = String.valueOf(ir.getService().getBasicAuthData().getUserName()) + ":" + ir.getService().getBasicAuthData().getPassword();
                    String encoding = new BASE64Encoder().encode(userPassword.getBytes());
                    conn.setRequestProperty("Authorization", "Basic " + encoding);
                }
                if (StringUtils.isEmpty((String)(type = conn.getContentType()))) {
                    LOGGER.warn((Object)I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool.incorrect-answer-type"));
                } else if (StringUtils.contains((String)type, (String)"html")) {
                    DesktopUtils.browse(infoURL);
                } else if (StringUtils.contains((String)type, (String)"image")) {
                    BufferedImage bi = ImageIO.read(conn.getInputStream());
                    if (bi == null) {
                        throw new MalformedURLException(I18N.getString("com.vividsolutions.wms.MapRequest.The-WMS-server-sent-an-invalid-image"));
                    }
                } else {
                    String inputLine;
                    File temporalFile = FileUtil.createTemporalFile("featInfoTemp", this.getFileExtensionFromContentType(type));
                    FileWriter writer = new FileWriter(temporalFile);
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((inputLine = in.readLine()) != null) {
                        writer.write(inputLine);
                    }
                    in.close();
                    writer.close();
                    DesktopUtils.open(temporalFile);
                }
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                this.getPanel().getContext().warnUser(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool.erroneous-information-request")) + ex.getMessage());
            }
        }
    }

    private String getFileExtensionFromContentType(String type) {
        if (StringUtils.containsIgnoreCase((String)type, (String)"gml")) {
            return "gml";
        }
        if (StringUtils.containsIgnoreCase((String)type, (String)"xml")) {
            return "xml";
        }
        if (StringUtils.containsIgnoreCase((String)type, (String)"plain")) {
            return "txt";
        }
        return "txt";
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck(tool).add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createSelectedLayersWithPrimaryKeyCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1)).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                boolean allVisibles = PersistentBlackboardPlugIn.get(workbenchContext).get(VISIBLE_LAYERS_KEY, true);
                EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
                if (!allVisibles) {
                    return new MultiEnableCheck().add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createAtLeastNSelectedLayerablesMustBeQueryable(1)).check(component);
                }
                return new MultiEnableCheck().add(checkFactory.createAtLeastNVisibleLayersMustNotBeRasterCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createAtLeastNVisibleLayerablesMustBeQueryable(1)).check(component);
            }
        });
    }
}

