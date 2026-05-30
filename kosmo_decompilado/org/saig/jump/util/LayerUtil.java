/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.util;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.IQueryable;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.UndoableEditReceiver;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerPanel;
import com.vividsolutions.wms.Capabilities;
import com.vividsolutions.wms.MapLayer;
import es.kosmo.core.styling.visitors.LabelFinderVisitor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsFactory;
import org.gvsig.crs.ICrs;
import org.saig.core.dao.coverage.Coverage;
import org.saig.core.dao.coverage.GridCoverage;
import org.saig.core.dao.coverage.GridCoverageCollection;
import org.saig.core.dao.coverage.ImageDataAccesor;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.MySQLDataSource;
import org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource;
import org.saig.core.dao.datasource.dbdatasource.PostGisDataSource;
import org.saig.core.dao.datasource.filedatasource.AbstractCadDataSource;
import org.saig.core.dao.datasource.filedatasource.cad.CadDataAccesor;
import org.saig.core.dao.datasource.filedatasource.dxf.DXFDataAccesor;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.sdi.wfs.WFSFeatureCollection;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.Style;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.ImageFileConnection;
import org.saig.jump.plugin.simbology.LoadLayerSimbologyPlugIn;

public class LayerUtil {
    private static final Logger LOGGER = Logger.getLogger(LayerUtil.class);
    private static final Icon BLANK_ICON = GUIUtil.resize(IconLoader.icon("blank.png"), 15);
    public static final String POINT_LAYER_NAME_EXTENSION = "_point";
    public static final String LINE_LAYER_NAME_EXTENSION = "_line";
    public static final String POLYGON_LAYER_NAME_EXTENSION = "_polygon";

    public static boolean isPolygonLayer(Layer inputLayer) {
        return inputLayer.getGeometryType() == 5 || inputLayer.getGeometryType() == 4;
    }

    public static boolean isPointLayer(Layer inputLayer) {
        return inputLayer.getGeometryType() == 1 || inputLayer.getGeometryType() == 8;
    }

    public static boolean isLinealLayer(Layer inputLayer) {
        return inputLayer.getGeometryType() == 3 || inputLayer.getGeometryType() == 2;
    }

    public static List<Layerable> orderByIndex(Collection<Layerable> layerables) {
        HashMap<Integer, Layerable> indexes = new HashMap<Integer, Layerable>();
        ArrayList<Layerable> sortedLayerables = new ArrayList<Layerable>();
        for (Layerable element : layerables) {
            indexes.put(LayerUtil.indexOfLayerable(element), element);
        }
        ArrayList keys = new ArrayList(indexes.keySet());
        Collections.sort(keys);
        for (Integer element : keys) {
            sortedLayerables.add((Layerable)indexes.get(element));
        }
        return sortedLayerables;
    }

    public static int indexOfLayerable(Layerable layerable) {
        return layerable.getLayerManager().getCategory(layerable).indexOf(layerable);
    }

    public static boolean sameCategory(Collection<Layerable> layerables) {
        boolean solucion = true;
        if (layerables != null && !layerables.isEmpty()) {
            Iterator<Layerable> iter = layerables.iterator();
            Layerable firstLayerable = iter.next();
            Category cat = firstLayerable.getLayerManager().getCategory(firstLayerable);
            while (iter.hasNext() && solucion) {
                Layerable element = iter.next();
                solucion = cat.equals(element.getLayerManager().getCategory(element));
            }
        } else if (layerables == null) {
            solucion = false;
        }
        return solucion;
    }

    public static boolean hasLabels(Layerable layerable) {
        if (!(layerable instanceof Layer)) {
            return false;
        }
        Layer layer = (Layer)layerable;
        LabelFinderVisitor labelFinder = new LabelFinderVisitor(false);
        labelFinder.visit(layer.getModelStyle());
        return labelFinder.hasLabels();
    }

    public static boolean hasLabelsActive(Layerable layerable) {
        if (!(layerable instanceof Layer)) {
            return false;
        }
        Layer layer = (Layer)layerable;
        LabelFinderVisitor labelFinder = new LabelFinderVisitor(true);
        labelFinder.visit(layer.getModelStyle());
        return labelFinder.hasLabels();
    }

    public static boolean hasLabelsInCurrentStyle(Layerable layerable) {
        if (!(layerable instanceof Layer)) {
            return false;
        }
        Layer layer = (Layer)layerable;
        LabelFinderVisitor labelFinder = new LabelFinderVisitor(false);
        labelFinder.visit(layer.getModelStyle().getSelectedFeatureTypeStyle());
        return labelFinder.hasLabels();
    }

    public static boolean hasLabelsActiveInCurrentStyle(Layerable layerable) {
        if (!(layerable instanceof Layer)) {
            return false;
        }
        Layer layer = (Layer)layerable;
        LabelFinderVisitor labelFinder = new LabelFinderVisitor(true);
        labelFinder.visit(layer.getModelStyle().getSelectedFeatureTypeStyle());
        return labelFinder.hasLabels();
    }

    public static Feature getSampleFeature(Layer layer) {
        List<Feature> listaEjemplos = layer.getFeatureCollectionWrapper().getUltimateWrappee().getFeaturesSamples(1);
        if (listaEjemplos.size() == 0) {
            return null;
        }
        return listaEjemplos.get(0);
    }

    public static boolean checkBD(Layer layer) {
        if (layer == null || layer.getUltimateFeatureCollectionWrapper() == null) {
            return false;
        }
        if (layer.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand) {
            FeatureCollectionOnDemand fcDemmand = (FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper();
            AbstractDataSource dataAccesor = fcDemmand.getDataAccesor();
            return dataAccesor instanceof AbstractJDBCDataSource;
        }
        return false;
    }

    public static boolean isEditable(Layer layer) {
        boolean isEditable = false;
        if (layer != null && layer.getUltimateFeatureCollectionWrapper() != null) {
            if (layer.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand) {
                FeatureCollectionOnDemand fcDemmand = (FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper();
                AbstractDataSource dataAccesor = fcDemmand.getDataAccesor();
                isEditable = dataAccesor instanceof IQueryable;
            } else {
                isEditable = true;
            }
        }
        return isEditable;
    }

    public static boolean isQueryable(Layerable layerable) {
        boolean isQueryable = false;
        if (layerable != null) {
            if (layerable instanceof Layer) {
                Layer layer = (Layer)layerable;
                if (layer.getUltimateFeatureCollectionWrapper() != null) {
                    if (layer.isRaster()) {
                        try {
                            Coverage coverage = ImageFileConnection.getCoverageFromFeatureCollection(layer.getUltimateFeatureCollectionWrapper());
                            isQueryable = coverage.getDataAccesor() instanceof IQueryable;
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)"", (Throwable)e);
                        }
                    } else if (layer.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand) {
                        FeatureCollectionOnDemand fcDemmand = (FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper();
                        AbstractDataSource dataAccesor = fcDemmand.getDataAccesor();
                        isQueryable = dataAccesor instanceof IQueryable;
                    } else {
                        isQueryable = true;
                    }
                }
            } else if (layerable instanceof WMSLayer) {
                WMSLayer wmsLayer = (WMSLayer)layerable;
                try {
                    List<String> layerNames;
                    Capabilities caps = wmsLayer.getService().getCapabilities();
                    if (caps != null && caps.getTopLayer() != null && CollectionUtils.isNotEmpty(layerNames = wmsLayer.getLayerNames())) {
                        int i = 0;
                        while (i < layerNames.size() && !isQueryable) {
                            MapLayer layer = caps.getTopLayer().getMapLayer(layerNames.get(i));
                            if (layer != null) {
                                isQueryable = layer.isQueryable();
                            }
                            ++i;
                        }
                    }
                }
                catch (IOException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
        }
        return isQueryable;
    }

    public static boolean isCADLayer(Layer layer) {
        FeatureCollection fc = layer.getUltimateFeatureCollectionWrapper();
        return fc instanceof FeatureCollectionOnDemand && ((FeatureCollectionOnDemand)fc).getDataAccesor() instanceof AbstractCadDataSource;
    }

    public static boolean isMultiPointLayer(Layer inputLayer) {
        return inputLayer.getGeometryType() == 8;
    }

    public static Icon generateIconForLayerable(Layerable currentLayerable) {
        Icon representingIcon = null;
        if (currentLayerable instanceof Layer) {
            Layer layer = (Layer)currentLayerable;
            Style layerStyle = layer.getModelStyle();
            if (layerStyle != null) {
                Rule[] currentRules = layerStyle.getSelectedFeatureTypeStyle().getRules();
                representingIcon = currentRules.length == 1 ? LegendIconMaker.makeLegendIcon(15, currentRules[0], null) : BLANK_ICON;
            }
        } else if (currentLayerable instanceof WMSLayer) {
            representingIcon = MapLayerPanel.ICON;
        } else if (currentLayerable.isRaster()) {
            representingIcon = LegendIconMaker.ICON_RASTER_SYMBOL;
        }
        return representingIcon;
    }

    public static String getLayerType(Layer layerToUse) {
        FeatureCollection featcol = layerToUse.getUltimateFeatureCollectionWrapper();
        return LayerUtil.getFeatureCollectionType(featcol, layerToUse.isRaster());
    }

    public static String getFeatureCollectionType(FeatureCollection featcol, boolean isRaster) {
        String type = I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Unknown");
        if (isRaster) {
            if (featcol.getFeatures().isEmpty()) {
                type = I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.unknow-raster-image-type");
            } else {
                Feature feature = featcol.getFeatures().get(0);
                Coverage coverage = (Coverage)feature.getAttribute("IMAGE");
                if (coverage instanceof GridCoverageCollection) {
                    GridCoverageCollection gc = (GridCoverageCollection)coverage;
                    GridCoverage gridc = gc.getImageIterator().next();
                    ImageDataAccesor da = gridc.getDataAccesor();
                    String path = da.getImagePath();
                    File f = new File(path);
                    if (da.getType() == 0) {
                        type = "ECW - " + f.getParent();
                    } else if (da.getType() == 1) {
                        type = "MrSid - " + f.getParent();
                    } else if (da.getType() == 999) {
                        type = String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Image")) + " - " + f.getParent();
                    }
                } else {
                    ImageDataAccesor da = coverage.getDataAccesor();
                    String path = da.getImagePath();
                    File f = new File(path);
                    if (da.getType() == 0) {
                        type = "ECW - " + f.getAbsolutePath();
                    } else if (da.getType() == 1) {
                        type = "MrSid - " + f.getAbsolutePath();
                    } else if (da.getType() == 999) {
                        type = String.valueOf(I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Image")) + " - " + f.getAbsolutePath();
                    }
                }
            }
        } else if (featcol instanceof FeatureCollectionOnDemand) {
            FeatureCollectionOnDemand onDemand = (FeatureCollectionOnDemand)featcol;
            AbstractDataSource da = onDemand.getDataAccesor();
            if (da instanceof AbstractJDBCDataSource) {
                AbstractJDBCDataSource jdbc = (AbstractJDBCDataSource)da;
                type = LayerUtil.getAbstractJDBCDataSourceString(jdbc);
            } else if (da instanceof ShapeFileDataSource) {
                ShapeFileDataSource pshda = (ShapeFileDataSource)da;
                type = pshda.getFile().getAbsolutePath();
            } else if (da instanceof DXFDataAccesor) {
                DXFDataAccesor dxfda = (DXFDataAccesor)da;
                type = "DXF - " + dxfda.getFile().getAbsolutePath();
            } else if (da instanceof CadDataAccesor) {
                CadDataAccesor cadda = (CadDataAccesor)da;
                type = String.valueOf(cadda.getCadType()) + " - " + cadda.getFileName();
            }
        } else if (featcol instanceof FeatureDataset) {
            type = I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Data-in-memory");
        } else if (featcol instanceof WFSFeatureCollection) {
            WFSFeatureCollection wfsFC = (WFSFeatureCollection)featcol;
            type = wfsFC.getService().isTransactional() ? I18N.getString("org.saig.jump.util.LayerUtil.transactional-wfs-layer") : I18N.getString("org.saig.jump.util.LayerUtil.wfs-layer");
            type = String.valueOf(type) + " - v." + wfsFC.getInfo().getServiceVersion() + " - " + wfsFC.getService().getBaseWfsURL();
        }
        return type;
    }

    public static String getAbstractJDBCDataSourceString(AbstractJDBCDataSource da) {
        String type = "";
        type = da instanceof MySQLDataSource ? "MySQL - " : (da instanceof PostGisDataSource ? "PostGIS - " : (da instanceof OracleSpatialDataSource ? "Oracle Spatial - " : String.valueOf(I18N.getString(LayerUtil.class, "unknown")) + " - "));
        type = String.valueOf(type) + da.getHostName() + ":" + da.getPort() + "/" + da.getDataBase() + (da.getDataBaseSchema() == null ? "" : "." + da.getDataBaseSchema()) + (da.getTableName() == null ? "" : "." + da.getTableName());
        return type;
    }

    public static boolean isSystemLayer(Layer layer) {
        return layer.getProperty("system_name") != null;
    }

    public static boolean isAppInternalSystemLayer(Layer layer) {
        Object isSystem = layer.getProperty("APP_INTERNAL_SYSTEM_LAYER");
        if (isSystem != null && isSystem instanceof Boolean) {
            return (Boolean)isSystem;
        }
        return false;
    }

    public static void setLayerProjection(Layer layer, String projection) throws CrsException {
        CrsFactory factory = new CrsFactory();
        ICrs crs = factory.getCRS(projection);
        layer.setProjection(crs);
    }

    public static void loadStyleToLayer(Layer layer, File styleFile) {
        UndoableEditReceiver undoableEditReceiver;
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        UndoableEditReceiver undoableEditReceiver2 = undoableEditReceiver = context.getLayerManager() != null ? context.getLayerManager().getUndoableEditReceiver() : null;
        if (undoableEditReceiver != null) {
            undoableEditReceiver.startReceiving();
        }
        try {
            try {
                LoadLayerSimbologyPlugIn loadSim = new LoadLayerSimbologyPlugIn(false);
                loadSim.setLayerSymbologyFile(styleFile);
                loadSim.setSelectedLayer(layer);
                loadSim.run(new DummyTaskMonitor(), JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
            }
            catch (Exception e1) {
                LOGGER.error((Object)"", (Throwable)e1);
                if (undoableEditReceiver != null) {
                    undoableEditReceiver.stopReceiving();
                }
            }
        }
        finally {
            if (undoableEditReceiver != null) {
                undoableEditReceiver.stopReceiving();
            }
        }
    }

    public static ImageDataAccesor getImageDataAccesor(Layer layer) {
        if (layer.isRaster()) {
            return ((Coverage)layer.getFeatureCollectionWrapper().getFeatures().get(0).getAttribute("IMAGE")).getDataAccesor();
        }
        return null;
    }

    public static void setLayerStyles(Layer layer, Symbolizer[] styles) {
        Style modelStyle = layer.getModelStyle();
        FeatureTypeStyle selectedFeatureTypeStyle = modelStyle.getSelectedFeatureTypeStyle();
        RuleImpl ruleImpl = new RuleImpl();
        ruleImpl.setSymbolizers(styles);
        selectedFeatureTypeStyle.setRules(new Rule[]{ruleImpl});
    }

    public static boolean isLabelLayer(Layer layer) {
        Object isLabelLayer = layer.getProperty("label_layer");
        if (isLabelLayer != null && isLabelLayer instanceof Boolean) {
            return (Boolean)isLabelLayer;
        }
        return false;
    }
}

