/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.plugin.utils.hiperlink;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.HiperLink;
import com.vividsolutions.jump.feature.HiperLinkCompound;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.hiperlink.HiperLinkValue;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class HiperLinkCursorTool
extends FeatureInfoTool {
    public static final String VISIBLE_LAYERS_KEY = String.valueOf(HiperLinkCursorTool.class.getName()) + " - VISIBLE_LAYERS";
    public static final String USE_INTERNAL_VIEWER_FOR_IMAGES = String.valueOf(HiperLinkCursorTool.class.getName()) + " - USE_INTERNAL_VIEWER_FOR_IMAGES";
    public static final String RELATIVE_DIRECTORY_PATH = String.valueOf(HiperLinkCursorTool.class.getName()) + " - RELATIVE_DIRECTORY_PATH";
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool.hiperlink");
    public static final ImageIcon ICON = IconLoader.icon("rayo.gif");
    public static final Cursor CURSOR = HiperLinkCursorTool.createCursor(IconLoader.icon("HiperlinkCursor.gif").getImage());
    public static final String TIF = "tif";
    public static final String TIFF = "tiff";
    public static final String JPG = "jpg";
    public static final String JPEG = "jpeg";
    public static final String PTIF = "ptif";
    public static final String PTIFF = "ptiff";
    public static final String BMP = "bmp";
    public static final String PNG = "png";
    public static final String GIF = "gif";
    private static Set<String> validImageExtensions;

    public HiperLinkCursorTool() {
        validImageExtensions = new TreeSet<String>();
        validImageExtensions.add(TIF);
        validImageExtensions.add(TIFF);
        validImageExtensions.add(JPG);
        validImageExtensions.add(JPEG);
        validImageExtensions.add(PTIF);
        validImageExtensions.add(PTIFF);
        validImageExtensions.add(BMP);
        validImageExtensions.add(PNG);
        validImageExtensions.add(GIF);
    }

    public static boolean isValidImage(String imagePath) {
        String ext = FileUtil.getExtension(imagePath);
        return validImageExtensions.contains(ext.toLowerCase());
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

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        Object[] layers = new Object[]{};
        boolean allVisibles = PersistentBlackboardPlugIn.get(this.getWorkbench().getContext()).get(VISIBLE_LAYERS_KEY, true);
        boolean showImagesInInternalViewer = PersistentBlackboardPlugIn.get(this.getWorkbench().getContext()).get(USE_INTERNAL_VIEWER_FOR_IMAGES, true);
        String relativeDirectoryPath = (String)PersistentBlackboardPlugIn.get(this.getWorkbench().getContext()).get(RELATIVE_DIRECTORY_PATH, null);
        layers = !allVisibles ? this.getWorkbench().getContext().getLayerNamePanel().getSelectedLayers() : this.getWorkbench().getContext().getLayerManager().getVisibleLayerables().toArray();
        if (ArrayUtils.isEmpty((Object[])layers)) {
            return;
        }
        ArrayList<HiperLinkValue> hiperlinks = new ArrayList<HiperLinkValue>();
        String onlyOneLayerName = null;
        int contLayerNotEmpty = 0;
        int i = 0;
        while (i < layers.length) {
            Map<Layer, Collection<Feature>> map;
            Collection<Feature> features;
            Layer layer;
            Object obj = layers[i];
            if (obj instanceof Layer && !(layer = (Layer)obj).isRaster() && layer.getHiperLink() != null && !CollectionUtils.isEmpty(features = (map = this.layerToSpecifiedFeaturesMap(new Object[]{layer})).get(layer))) {
                HiperLink hiperLink = layer.getHiperLink();
                ArrayList<HiperLinkValue> hpLinkValuesTemp = new ArrayList<HiperLinkValue>();
                for (Feature element : features) {
                    hpLinkValuesTemp.addAll(this.loadHiperLink(hiperLink, element));
                }
                if (!hpLinkValuesTemp.isEmpty()) {
                    onlyOneLayerName = layer.getName();
                    ++contLayerNotEmpty;
                }
                hiperlinks.addAll(hpLinkValuesTemp);
            }
            ++i;
        }
        if (hiperlinks.size() == 1) {
            HiperLinkNavigatorDialog.openHiperLinkWindow((HiperLinkValue)hiperlinks.get(0), showImagesInInternalViewer, relativeDirectoryPath);
        } else if (hiperlinks.size() > 1) {
            String lastLayerName = onlyOneLayerName;
            int mode = 2;
            mode = contLayerNotEmpty == 1 ? HiperLinkNavigatorDialog.ONLY_ONE_LAYER_MODE : (allVisibles ? HiperLinkNavigatorDialog.VISIBLE_LAYERS_MODE : HiperLinkNavigatorDialog.SELECTED_LAYERS_MODE);
            HiperLinkNavigatorDialog hiperlinkDialog = new HiperLinkNavigatorDialog(JUMPWorkbench.getFrameInstance(), false, lastLayerName, hiperlinks, mode, showImagesInInternalViewer, relativeDirectoryPath);
            GUIUtil.centreOnScreen(hiperlinkDialog);
            hiperlinkDialog.setVisible(true);
        } else {
            DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool.Selected-features-has-no-hiperlinks"), I18N.getString("org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool.No-results-were-found"));
        }
    }

    private List<HiperLinkValue> loadHiperLink(HiperLink hiperLink, Feature selectedFeature) {
        ArrayList<HiperLinkValue> result = new ArrayList<HiperLinkValue>();
        if (hiperLink instanceof HiperLinkCompound) {
            HiperLinkCompound hpCompound = (HiperLinkCompound)hiperLink;
            Object sourceKey = selectedFeature.getAttribute(hpCompound.getKeyFieldSource());
            Table table = hpCompound.getTable();
            List<Record> records = table.getByAttribute(new String[]{hpCompound.getKeyFieldTarget()}, new Object[]{sourceKey});
            for (Record element : records) {
                Object value = element.getAttribute(hpCompound.getFieldWithHiperLink());
                Object description = null;
                if (hpCompound.getFieldDescription() != null) {
                    description = element.getAttribute(hpCompound.getFieldDescription());
                }
                if (value == null) continue;
                result.add(new HiperLinkValue((String)description, (String)value));
            }
        } else {
            String value = (String)selectedFeature.getAttribute(hiperLink.getFieldWithHiperLink());
            if (StringUtils.isNotEmpty((String)value)) {
                String description = "";
                if (hiperLink.getFieldDescription() != null) {
                    description = (String)selectedFeature.getAttribute(hiperLink.getFieldDescription());
                }
                result.add(new HiperLinkValue(description, value));
            }
        }
        return result;
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck(tool);
        check.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
        check.add(checkFactory.createSelectedLayersWithPrimaryKeyCheck());
        check.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        check.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                boolean allVisibles = PersistentBlackboardPlugIn.get(workbenchContext).get(VISIBLE_LAYERS_KEY, true);
                EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
                if (!allVisibles) {
                    return new MultiEnableCheck().add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createAtLeastNSelectedLayerablesWithHiperLinkCheck(1)).check(component);
                }
                return new MultiEnableCheck().add(checkFactory.createAtLeastNVisibleLayersMustNotBeRasterCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createAtLeastNVisibleLayerablesWithHiperLinkCheck(1)).check(component);
            }
        });
        return check;
    }
}

