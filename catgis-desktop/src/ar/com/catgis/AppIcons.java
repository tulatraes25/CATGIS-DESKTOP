package ar.com.catgis;

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
    private static Icon proSvg(String name, int size) { return loadSvg("icons/professional/" + name + ".svg", size); }
    private static Icon legacy(String name) { return loadSvg("icons/" + name + ".svg", TB); }

    private static Icon loadSvg(String path, int size) {
        try { return new FlatSVGIcon(path, size, size); } catch (Throwable ignored) {}
        URL url = AppIcons.class.getResource("/" + path);
        if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
        return new ImageIcon(new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB));
    }

    // Navigation
    public static Icon zoomInIcon() { return proSvg("navigation/zoom-in"); }
    public static Icon zoomOutIcon() { return proSvg("navigation/zoom-out"); }
    public static Icon zoomAllIcon() { return proSvg("navigation/zoom-full"); }
    public static Icon panIcon() { return proSvg("navigation/pan"); }
    public static Icon identifyIcon() { return proSvg("navigation/identify"); }

    // Editing
    public static Icon selectIcon() { return proSvg("editing/select"); }
    public static Icon pointIcon() { return proSvg("editing/point-create"); }
    public static Icon lineIcon() { return proSvg("editing/line-create"); }
    public static Icon polygonIcon() { return proSvg("editing/polygon-create"); }
    public static Icon moveVertexIcon() { return proSvg("editing/vertex-move"); }
    public static Icon addVertexIcon() { return proSvg("editing/vertex-add"); }
    public static Icon removeVertexIcon() { return proSvg("editing/vertex-delete"); }
    public static Icon cutIcon() { return proSvg("editing/cut"); }
    public static Icon deleteIcon() { return proSvg("editing/delete"); }
    public static Icon saveIcon() { return proSvg("editing/save"); }
    public static Icon cancelIcon() { return proSvg("editing/cancel"); }
    public static Icon undoIcon() { return proSvg("editing/undo"); }
    public static Icon redoIcon() { return proSvg("editing/redo"); }
    public static Icon finishIcon() { return proSvg("editing/confirm"); }
    public static Icon snapIcon() { return proSvg("editing/snap"); }

    // Data
    public static Icon openIcon() { return proSvg("data/open"); }
    public static Icon addLayerIcon() { return proSvg("data/add-layer"); }
    public static Icon osmIcon() { return proSvg("data/osm"); }
    public static Icon esriIcon() { return proSvg("data/esri"); }
    public static Icon wfsIcon() { return proSvg("data/wfs"); }
    public static Icon catserverIcon() { return proSvg("data/catserver"); }
    public static Icon saveProjectIcon() { return proSvg("data/save"); }

    // Topography
    public static Icon demIcon() { return proSvg("topography/dem"); }
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
    public static Icon exportIcon() { return legacy("export"); }
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
    public static Icon attrEditIcon() { return legacy("edit"); }
    public static Icon attrApplyIcon() { return legacy("apply"); }
    public static Icon attrCalculatorIcon() { return legacy("calculator"); }
    public static Icon attrAssignIcon() { return legacy("assign"); }
    public static Icon attrCloseIcon() { return legacy("close"); }
    public static Icon fieldsIcon() { return legacy("fields"); }
}
