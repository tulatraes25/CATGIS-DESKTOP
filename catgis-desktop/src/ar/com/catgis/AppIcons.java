package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.core.model.Layer;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import java.awt.Image;
import java.net.URL;

public final class AppIcons {

    private static final int TB = 24;
    private static final int SM = 16;

    private AppIcons() {}

    private static Icon proSvg(String name) { return loadSvg("icons/professional/" + name + ".svg", TB); }
    private static Icon uiSvg(String name) { return loadSvg("icons/ui/" + name + ".svg", TB); }
    private static Icon proSvg(String name, int size) { return loadSvg("icons/professional/" + name + ".svg", size); }
    private static Icon legacy(String name) { return loadSvg("icons/" + name + ".svg", TB); }

    private static Icon loadSvg(String path, int size) {
        try { return new FlatSVGIcon(path, size, size); } catch (Exception ignored) { CatgisLogger.warn("AppIcons: operation failed", ignored); }
        URL url = AppIcons.class.getResource("/" + path);
        if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
        return new ImageIcon(new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB));
    }

    // Navigation
    public static Icon zoomInIcon() { return uiSvg("zoom-in"); }
    public static Icon zoomOutIcon() { return uiSvg("zoom-out"); }
    public static Icon zoomAllIcon() { return uiSvg("zoom-all"); }
    public static Icon panIcon() { return uiSvg("pan"); }
    public static Icon identifyIcon() { return uiSvg("identify"); }

    // Editing
    public static Icon selectIcon() { return proSvg("editing/select"); }
    public static Icon pointIcon() { return uiSvg("point"); }
    public static Icon lineIcon() { return uiSvg("line"); }
    public static Icon polygonIcon() { return uiSvg("polygon"); }
    public static Icon moveVertexIcon() { return proSvg("editing/vertex-move"); }
    public static Icon addVertexIcon() { return proSvg("editing/vertex-add"); }
    public static Icon removeVertexIcon() { return proSvg("editing/vertex-delete"); }
    public static Icon cutIcon() { return proSvg("editing/cut"); }
    public static Icon deleteIcon() { return proSvg("editing/delete"); }
    public static Icon saveIcon() { return uiSvg("save"); }
    public static Icon cancelIcon() { return proSvg("editing/cancel"); }
    public static Icon undoIcon() { return proSvg("editing/undo"); }
    public static Icon redoIcon() { return proSvg("editing/redo"); }
    public static Icon finishIcon() { return proSvg("editing/confirm"); }
    public static Icon snapIcon() { return proSvg("editing/snap"); }

    // Data
    public static Icon openIcon() { return uiSvg("open-project"); }
    public static Icon addLayerIcon() { return uiSvg("add-layer"); }
    public static Icon osmIcon() { return uiSvg("osm"); }
    public static Icon esriIcon() { return uiSvg("esri"); }
    public static Icon wfsIcon() { return uiSvg("wfs"); }
    public static Icon catserverIcon() { return proSvg("data/catserver"); }
    public static Icon saveProjectIcon() { return uiSvg("save"); }
    public static Icon catmapIcon() { return uiSvg("catmap"); }

    // Topography
    public static Icon demIcon() { return uiSvg("topography"); }
    public static Icon contourIcon() { return proSvg("topography/contours"); }
    public static Icon hydrologyIcon() { return proSvg("topography/hydrology"); }
    public static Icon runoffIcon() { return proSvg("topography/runoff"); }
    public static Icon floodIcon() { return proSvg("topography/flood"); }
    public static Icon profileIcon() { return proSvg("topography/profile"); }
    public static Icon riskIcon() { return proSvg("topography/risk"); }
    public static Icon clipIcon() { return proSvg("topography/clip"); }

    // TOC small
    public static Icon tocPoint() { return proSvg("toc/vector-point", SM); }
    public static Icon tocLine() { return proSvg("toc/vector-line", SM); }
    public static Icon tocPolygon() { return proSvg("toc/vector-polygon", SM); }
    public static Icon tocRaster() { return proSvg("toc/raster", SM); }
    public static Icon tocBaseMap() { return proSvg("toc/basemap", SM); }

    // Legacy compatibility
    public static Icon zoomLayerIcon() { return legacy("zoom-layer"); }
    public static Icon zoomOutLegacy() { return legacy("zoom-out"); }
    public static Icon viewPreviousIcon() { return legacy("view-previous"); }
    public static Icon viewNextIcon() { return legacy("view-next"); }
    public static Icon multiPointIcon() { return legacy("multi-point"); }
    public static Icon holeIcon() { return legacy("hole"); }
    public static Icon distanceIcon() { return legacy("measure-distance"); }
    public static Icon areaIcon() { return legacy("measure-area"); }
    public static Icon projectIcon() { return legacy("project"); }
    public static Icon importTableIcon() { return legacy("import-table"); }
    public static Icon tableIcon() { return legacy("table"); }
    public static Icon crsIcon() { return legacy("crs"); }
    public static Icon exportIcon() { return uiSvg("export"); }
    public static Icon propertiesIcon() { return legacy("properties"); }
    public static Icon labelsIcon() { return legacy("labels"); }
    public static Icon removeIcon() { return legacy("remove"); }
    public static Icon upIcon() { return legacy("up"); }
    public static Icon downIcon() { return legacy("down"); }
    public static Icon visibleIcon() { return legacy("visible"); }
    public static Icon hiddenIcon() { return legacy("hidden"); }
    public static Icon renameIcon() { return legacy("rename"); }
    public static Icon genericLayerIcon() { return legacy("generic-layer"); }
    public static Icon joinVerticesIcon() { return legacy("join-vertices"); }
    public static Icon rectangleIcon() { return legacy("rectangle"); }
    public static Icon circleIcon() { return legacy("circle"); }
    public static Icon circleThreePointsIcon() { return legacy("circle-3p"); }
    public static Icon moveFeatureIcon() { return legacy("move-feature"); }
    public static Icon increaseAreaIcon() { return legacy("increase-area"); }
    public static Icon decreaseAreaIcon() { return legacy("decrease-area"); }
    public static Icon extendLineIcon() { return legacy("extend-line"); }
    public static Icon shortenLineIcon() { return legacy("shorten-line"); }
    public static Icon parallelIcon() { return legacy("parallel"); }
    public static Icon perpendicularIcon() { return legacy("perpendicular"); }
    public static Icon toolboxIcon() { return legacy("toolbox"); }
    public static Icon basemapIcon() { return legacy("basemap"); }
    public static Icon imageryIcon() { return legacy("imagery"); }
    public static Icon wmsIcon() { return legacy("wms"); }
    public static Icon drainageIcon() { return legacy("drainage"); }
    public static Icon terrainAnalysisIcon() { return legacy("terrain-analysis"); }
    public static Icon attrCopyIcon() { return legacy("copy"); }
    public static Icon attrRefreshIcon() { return legacy("refresh"); }
    public static Icon attrEditIcon() { return uiSvg("edit"); }
    public static Icon attrApplyIcon() { return legacy("apply"); }
    public static Icon attrCalculatorIcon() { return legacy("calculator"); }
    public static Icon attrAssignIcon() { return legacy("assign"); }
    public static Icon attrCloseIcon() { return legacy("close"); }
    public static Icon fieldsIcon() { return legacy("fields"); }

    // Menu/UI actions (new coherent set)
    public static Icon exitIcon() { return uiSvg("exit"); }
    public static Icon hillshadeIcon() { return uiSvg("hillshade"); }
    public static Icon slopeIcon() { return uiSvg("slope"); }
    public static Icon aspectIcon() { return uiSvg("aspect"); }
    public static Icon viewshedIcon() { return uiSvg("viewshed"); }
    public static Icon networkIcon() { return uiSvg("network"); }
    public static Icon routeIcon() { return uiSvg("route"); }
    public static Icon hexagonIcon() { return uiSvg("hexagon"); }
    public static Icon spectralIcon() { return uiSvg("spectral"); }
    public static Icon mlIcon() { return uiSvg("ml"); }
    public static Icon batchIcon() { return uiSvg("batch"); }
    public static Icon geoprocessIcon() { return uiSvg("geoprocess"); }
    public static Icon scriptIcon() { return uiSvg("script"); }
    public static Icon downloadIcon() { return uiSvg("download"); }
    public static Icon catalogIcon() { return uiSvg("catalog"); }
    public static Icon kmlIcon() { return uiSvg("kml"); }
    public static Icon styleIcon() { return uiSvg("style"); }
    public static Icon environmentIcon() { return uiSvg("environment"); }
    public static Icon satelliteIcon() { return uiSvg("satellite"); }
    public static Icon settingsIcon() { return uiSvg("settings"); }
    public static Icon topologyIcon() { return uiSvg("topology"); }
    public static Icon cadIcon() { return uiSvg("cad"); }
    public static Icon climateIcon() { return uiSvg("climate"); }
    public static Icon helpIcon() { return uiSvg("help"); }
    public static Icon historyIcon() { return uiSvg("history"); }
    public static Icon languageIcon() { return uiSvg("language"); }
}
