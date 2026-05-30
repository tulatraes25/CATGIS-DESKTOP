/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.px.dxf.AcadColor
 */
package org.saig.jump.plugin.simbology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.AbstractSaveProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.px.dxf.AcadColor;
import org.saig.core.dao.datasource.filedatasource.AbstractCadDataSource;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.MathExpressionImpl;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.FeatureTypeStyleImpl;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Halo;
import org.saig.core.styling.PointPlacement;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleBuilder;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.StyleImpl;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;
import org.saig.jump.widgets.config.ConfigPathPanel;
import org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog;

public class SaveLayerSimbologyPlugIn
extends AbstractSaveProjectPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(SaveLayerSimbologyPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.SaveLayerSimbology.save-simbology");
    public static final Icon ICON = IconLoader.icon("saveSimbology.png");
    public static final String LAYER_SIMBOLOGY_FILE_EXTENSION = "sls";
    public static final FileFilter LAYER_SIMBOLOGY_FILE_FILTER = GUIUtil.createFileFilter(I18N.getString("org.saig.jump.plugin.utils.SaveLayerSimbology.Simbology-file"), new String[]{"sls"});
    private File layerSymbologyFile;
    private List<com.vividsolutions.jump.workbench.ui.renderer.style.Style> jumpStyles = null;
    private Style modelStyle = null;
    private int geometryType;
    private boolean isCADLayer;
    private String selectedLayerName;
    private static final double DEFAULT_WIDTH = 1.0;
    private static final String CAD_STYLE = I18N.getString("org.saig.jump.plugin.utils.SaveLayerSimbologyPlugIn.cad-style");
    private JFileChooser fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
    private String selectedDefaultStyleName;
    private boolean textForPoints = false;
    private boolean textForLines = false;
    private boolean textForPolygons = false;
    private boolean isSaveForShape = false;
    private boolean areSolid = false;
    private SaveCADLayerSimbologyDialog dialog;
    protected static Font baseFont = new JLabel().getFont();

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.fileChooser.setDialogTitle(NAME);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        this.fileChooser.addChoosableFileFilter(LAYER_SIMBOLOGY_FILE_FILTER);
        this.fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        this.fileChooser.setFileFilter(LAYER_SIMBOLOGY_FILE_FILTER);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        String defaultPath = (String)PersistentBlackboardPlugIn.get(context.getWorkbenchContext().getBlackboard()).get(ConfigPathPanel.SAVE_SIMBOLOGY_PATH_KEY);
        if (StringUtils.isNotEmpty((String)defaultPath)) {
            this.fileChooser.setCurrentDirectory(new File(defaultPath));
        }
        Layer selectedLayer = (Layer)context.getLayerNamePanel().getSelectedLayers()[0];
        if (this.fileChooser.showSaveDialog(context.getWorkbenchFrame()) != 0) {
            return false;
        }
        this.layerSymbologyFile = new File(FileUtil.addValidExtension(this.fileChooser.getSelectedFile().getAbsolutePath(), LAYER_SIMBOLOGY_FILE_EXTENSION));
        this.jumpStyles = selectedLayer.getStyles();
        this.modelStyle = selectedLayer.getModelStyle();
        this.geometryType = selectedLayer.getGeometryType();
        this.isCADLayer = LayerUtil.isCADLayer(selectedLayer);
        this.selectedLayerName = selectedLayer.getName();
        if (this.isCADLayer) {
            if (this.dialog == null) {
                this.dialog = new SaveCADLayerSimbologyDialog(context.getWorkbenchFrame(), true);
            }
            GUIUtil.centreOnScreen(this.dialog);
            this.dialog.setVisible(true);
            this.selectedDefaultStyleName = this.dialog.getSelectedFeatureTypeStyleName();
            this.isSaveForShape = this.dialog.isSaveForShape();
            return this.dialog.wasOkPressed();
        }
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
        return SaveLayerSimbologyPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.SaveLayerSimbology.saving-the-simbology-file-{0}", new Object[]{this.layerSymbologyFile.getAbsolutePath()}));
        if (this.isCADLayer) {
            Layer selectedLayer = JUMPWorkbench.getLayer(this.selectedLayerName);
            Style[] layerStyles = this.createSimbologyMapFromCADLayer(selectedLayer);
            String baseFileName = FileUtil.nameWithoutExtension(this.layerSymbologyFile.getAbsolutePath());
            if (layerStyles[0] != null) {
                this.saveSymbology(this.jumpStyles, layerStyles[0], 1, new File(FileUtil.addValidExtension(String.valueOf(baseFileName) + "_point", LAYER_SIMBOLOGY_FILE_EXTENSION)));
            }
            if (layerStyles[1] != null) {
                this.saveSymbology(this.jumpStyles, layerStyles[1], 3, new File(FileUtil.addValidExtension(String.valueOf(baseFileName) + "_line", LAYER_SIMBOLOGY_FILE_EXTENSION)));
            }
            if (layerStyles[2] != null) {
                this.saveSymbology(this.jumpStyles, layerStyles[2], 5, new File(FileUtil.addValidExtension(String.valueOf(baseFileName) + "_polygon", LAYER_SIMBOLOGY_FILE_EXTENSION)));
            }
        } else {
            this.saveSymbology(this.jumpStyles, this.modelStyle, this.geometryType, this.layerSymbologyFile);
        }
        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.SaveLayerSimbology.simbology-saved-in-the-file-{0}", new Object[]{this.layerSymbologyFile.getAbsolutePath()}));
    }

    private Style[] createSimbologyMapFromCADLayer(Layer selectedCADLayer) throws Exception {
        FeatureIterator it = null;
        Style[] simbologyArray = null;
        TreeSet[] colorRulesArray = new TreeSet[]{new TreeSet(), new TreeSet(), new TreeSet()};
        TreeSet[] layerRulesArray = new TreeSet[]{new TreeSet(), new TreeSet(), new TreeSet()};
        TreeSet[] layerAndColorRulesArray = new TreeSet[]{new TreeSet(), new TreeSet(), new TreeSet()};
        try {
            it = selectedCADLayer.getUltimateFeatureCollectionWrapper().iterator();
            while (it.hasNext()) {
                Feature currentFeature = it.next();
                String fShape = (String)currentFeature.getAttribute("FShape");
                String layerName = (String)currentFeature.getAttribute("Layer");
                String entity = (String)currentFeature.getAttribute("Entity");
                Number color = (Number)currentFeature.getAttribute("Color");
                if (this.isPointFeature(currentFeature)) {
                    colorRulesArray[0].add(color);
                    layerRulesArray[0].add(layerName);
                    layerAndColorRulesArray[0].add(new LayerColorRelation(layerName, color));
                    this.textForPoints = this.textForPoints || entity.equals("Text");
                    continue;
                }
                if (this.isLinealFeature(currentFeature)) {
                    colorRulesArray[1].add(color);
                    layerRulesArray[1].add(layerName);
                    layerAndColorRulesArray[1].add(new LayerColorRelation(layerName, color));
                    this.textForLines = this.textForLines || entity.equals("Text");
                    continue;
                }
                if (this.isPoligonalFeature(currentFeature)) {
                    colorRulesArray[2].add(color);
                    layerRulesArray[2].add(layerName);
                    layerAndColorRulesArray[2].add(new LayerColorRelation(layerName, color));
                    this.textForPolygons = this.textForPolygons || entity.equals("Text");
                    this.areSolid = this.areSolid || entity.equals("Solid");
                    continue;
                }
                LOGGER.warn((Object)(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.SaveLayerSimbologyPlugIn.unsupported-fshape-type")) + fShape));
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        Hashtable<String, Set<Integer>> layerToColor = ((AbstractCadDataSource)((FeatureCollectionOnDemand)selectedCADLayer.getUltimateFeatureCollectionWrapper()).getDataAccesor()).getLayerToColor();
        Style pointStyle = this.generateStyle(colorRulesArray[0], layerRulesArray[0], layerAndColorRulesArray[0], layerToColor, 1);
        Style lineStyle = this.generateStyle(colorRulesArray[1], layerRulesArray[1], layerAndColorRulesArray[1], layerToColor, 3);
        Style polygonStyle = this.generateStyle(colorRulesArray[2], layerRulesArray[2], layerAndColorRulesArray[2], layerToColor, 5);
        simbologyArray = new Style[]{pointStyle, lineStyle, polygonStyle};
        return simbologyArray;
    }

    private boolean isPointFeature(Feature currentFeature) {
        Geometry geometry = currentFeature.getGeometry();
        return geometry != null && (geometry instanceof Point || geometry instanceof MultiPoint);
    }

    private boolean isLinealFeature(Feature currentFeature) {
        Geometry geometry = currentFeature.getGeometry();
        return geometry != null && (geometry instanceof LineString || geometry instanceof MultiLineString);
    }

    private boolean isPoligonalFeature(Feature currentFeature) {
        Geometry geometry = currentFeature.getGeometry();
        return geometry != null && (geometry instanceof Polygon || geometry instanceof MultiPolygon);
    }

    private Style generateStyle(Set colorSet, Set layerSet, Set layerAndColorSet, Hashtable layerToColor, int geometryType) throws Exception {
        StyleImpl modelStyle = null;
        StyleBuilder styleBuilder = new StyleBuilder();
        boolean fillSymbolizers = this.areSolid && geometryType == 5;
        ArrayList<Rule> colorRuleList = new ArrayList<Rule>();
        for (Number currentColor : colorSet) {
            Color borderColor = AcadColor.getColor((int)currentColor.intValue());
            Color fillColor = null;
            if (fillSymbolizers) {
                fillColor = AcadColor.getColor((int)currentColor.intValue());
            }
            Symbolizer symb = styleBuilder.buildSymbolizer(borderColor, fillColor, 1.0, geometryType);
            Rule rule = styleBuilder.createRule(symb);
            rule.setName(currentColor.toString());
            rule.setTitle(currentColor.toString());
            rule.setFilter(styleBuilder.buildFilterFromValue("Color", currentColor));
            colorRuleList.add(rule);
        }
        ArrayList<Rule> layerRuleList = new ArrayList<Rule>();
        for (String currentLayer : layerSet) {
            Set colors = (Set)layerToColor.get(currentLayer);
            Integer auxColor = (Integer)colors.iterator().next();
            Color borderColor = AcadColor.getColor((int)auxColor);
            Color fillColor = null;
            if (fillSymbolizers) {
                fillColor = AcadColor.getColor((int)auxColor);
            }
            Symbolizer symb = styleBuilder.buildSymbolizer(borderColor, fillColor, 1.0, geometryType);
            Rule rule = styleBuilder.createRule(symb);
            rule.setName(currentLayer);
            rule.setTitle(currentLayer);
            rule.setFilter(styleBuilder.buildFilterFromValue("Layer", currentLayer));
            layerRuleList.add(rule);
        }
        ArrayList<Rule> layerNameAndColorRuleList = new ArrayList<Rule>();
        for (LayerColorRelation currentLayerColorRelation : layerAndColorSet) {
            String layerName = currentLayerColorRelation.getLayerName();
            Number colorNumber = currentLayerColorRelation.getColor();
            Color borderColor = AcadColor.getColor((int)colorNumber.intValue());
            Color fillColor = null;
            if (fillSymbolizers) {
                fillColor = AcadColor.getColor((int)colorNumber.intValue());
            }
            Symbolizer symb = styleBuilder.buildSymbolizer(borderColor, fillColor, 1.0, geometryType);
            Rule rule = styleBuilder.createRule(symb);
            rule.setName(String.valueOf(layerName) + " - " + colorNumber);
            rule.setTitle(String.valueOf(layerName) + " - " + colorNumber);
            Filter layerNameFilter = styleBuilder.buildFilterFromValue("Layer", layerName);
            Filter colorFilter = styleBuilder.buildFilterFromValue("Color", colorNumber);
            rule.setFilter(layerNameFilter.and(colorFilter));
            layerNameAndColorRuleList.add(rule);
        }
        if (colorRuleList.isEmpty() && layerRuleList.isEmpty() && layerNameAndColorRuleList.isEmpty()) {
            return null;
        }
        if (geometryType == 1 && this.textForPoints || geometryType == 3 && this.textForLines || geometryType == 5 && this.textForPolygons) {
            TextSymbolizer symb = this.buildTextSymbolyzer(styleBuilder, this.textForLines);
            Rule rule = styleBuilder.createRule(symb);
            rule.setName(I18N.getString("org.saig.jump.plugin.utils.SaveLayerSimbologyPlugIn.label"));
            rule.setTitle(I18N.getString("org.saig.jump.plugin.utils.SaveLayerSimbologyPlugIn.label"));
            colorRuleList.add(rule);
            layerRuleList.add(rule);
            layerNameAndColorRuleList.add(rule);
        }
        Rule[] colorRules = new Rule[colorRuleList.size()];
        FeatureTypeStyleImpl colorTypeStyle = new FeatureTypeStyleImpl(colorRuleList.toArray(colorRules));
        colorTypeStyle.setName(SaveCADLayerSimbologyDialog.TRUE_COLOR_OPTION);
        colorTypeStyle.setTitle(SaveCADLayerSimbologyDialog.TRUE_COLOR_OPTION);
        Rule[] layerRules = new Rule[layerRuleList.size()];
        FeatureTypeStyleImpl layerTypeStyle = new FeatureTypeStyleImpl(layerRuleList.toArray(layerRules));
        layerTypeStyle.setName(SaveCADLayerSimbologyDialog.LAYER_OPTION);
        layerTypeStyle.setTitle(SaveCADLayerSimbologyDialog.LAYER_OPTION);
        Rule[] layerNameAndColorRules = new Rule[layerNameAndColorRuleList.size()];
        FeatureTypeStyleImpl layerAndColorTypeStyle = new FeatureTypeStyleImpl(layerNameAndColorRuleList.toArray(layerNameAndColorRules));
        layerAndColorTypeStyle.setName(SaveCADLayerSimbologyDialog.LAYER_COLOR_OPTION);
        layerAndColorTypeStyle.setTitle(SaveCADLayerSimbologyDialog.LAYER_COLOR_OPTION);
        modelStyle = new StyleImpl();
        modelStyle.setName(CAD_STYLE);
        modelStyle.setTitle(CAD_STYLE);
        modelStyle.addFeatureTypeStyle(colorTypeStyle);
        modelStyle.addFeatureTypeStyle(layerTypeStyle);
        modelStyle.addFeatureTypeStyle(layerAndColorTypeStyle);
        modelStyle.setSelectedFeatureTypeStyle(this.selectedDefaultStyleName);
        return modelStyle;
    }

    public TextSymbolizer buildTextSymbolyzer(StyleBuilder styleBuilder, boolean isLineLayer) throws Exception {
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        StyleFactory factory = StyleFactory.createStyleFactory();
        Font jumpFont = baseFont.deriveFont(new AffineTransform());
        LiteralExpression fontFamily = filterFactory.createLiteralExpression(jumpFont.getFamily());
        boolean isBold = jumpFont.getStyle() == 1 || jumpFont.getStyle() > 2;
        boolean isItalic = jumpFont.getStyle() == 2 || jumpFont.getStyle() > 2;
        LiteralExpression fontWeight = null;
        fontWeight = isBold ? filterFactory.createLiteralExpression("bold") : filterFactory.createLiteralExpression("normal");
        LiteralExpression fontStyle = null;
        fontStyle = isItalic ? filterFactory.createLiteralExpression("italic") : filterFactory.createLiteralExpression("normal");
        LiteralExpression fontSize = filterFactory.createLiteralExpression(3);
        org.saig.core.styling.Font modelFont = factory.createFont(fontFamily, fontStyle, fontWeight, fontSize);
        Halo halo = null;
        LiteralExpression textColor = filterFactory.createLiteralExpression(Layer.decodeColor(Color.BLACK));
        Fill fillText = factory.createFill(textColor);
        Displacement offset = factory.createDisplacement(filterFactory.createLiteralExpression(0), filterFactory.createLiteralExpression(0));
        String rotationFieldName = "RotationText";
        if (this.isSaveForShape) {
            rotationFieldName = rotationFieldName.substring(0, 11);
        }
        AttributeExpression rotationExpression = filterFactory.createAttributeExpression(rotationFieldName);
        MathExpressionImpl mathExpr = new MathExpressionImpl(107);
        mathExpr.addLeftValue(new LiteralExpressionImpl(-1));
        mathExpr.addRightValue(rotationExpression);
        AnchorPoint anchorPoint = factory.createAnchorPoint(filterFactory.createLiteralExpression(0), filterFactory.createLiteralExpression(0));
        PointPlacement labelPlac = factory.createPointPlacement(anchorPoint, offset, mathExpr);
        AttributeExpression label = filterFactory.createAttributeExpression("Text");
        TextSymbolizer ts = factory.createTextSymbolizer(fillText, new org.saig.core.styling.Font[]{modelFont}, halo, label, labelPlac, null);
        ts.setHeightAttribute(filterFactory.createAttributeExpression("HeightText"));
        return ts;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayerMustBeActiveCheck());
    }

    private class LayerColorRelation
    implements Comparable<LayerColorRelation> {
        private String layerName;
        private Number color;

        public LayerColorRelation(String layerName, Number color) {
            this.layerName = layerName;
            this.color = color;
        }

        public String getLayerName() {
            return this.layerName;
        }

        public void setLayerName(String layerName) {
            this.layerName = layerName;
        }

        public Number getColor() {
            return this.color;
        }

        public void setColor(Number color) {
            this.color = color;
        }

        @Override
        public int compareTo(LayerColorRelation o) {
            LayerColorRelation lcr = o;
            int comp = this.getLayerName().compareTo(lcr.getLayerName());
            if (comp == 0) {
                int otherColorValue;
                int colorValue = this.getColor().intValue();
                if (colorValue < (otherColorValue = lcr.getColor().intValue())) {
                    comp = -1;
                } else if (colorValue > otherColorValue) {
                    comp = 1;
                }
            }
            return comp;
        }
    }
}

