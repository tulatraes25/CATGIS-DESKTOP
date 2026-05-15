package ar.com.catgis;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import java.awt.Image;
import java.net.URL;

public final class AppIcons {

    private static final int SIZE = 18;

    private AppIcons() {
    }

    public static Icon attrCopyIcon() { return svg("copy"); }
    public static Icon attrRefreshIcon() { return svg("refresh"); }
    public static Icon attrEditIcon() { return svg("edit"); }
    public static Icon attrApplyIcon() { return svg("apply"); }
    public static Icon attrCalculatorIcon() { return svg("calculator"); }
    public static Icon attrAssignIcon() { return svg("assign"); }
    public static Icon attrCloseIcon() { return svg("close"); }
    public static Icon fieldsIcon() { return svg("fields"); }

    public static Icon openIcon() { return svg("open"); }
    public static Icon saveIcon() { return svg("save"); }
    public static Icon projectIcon() { return svg("project"); }
    public static Icon importTableIcon() { return svg("import-table"); }
    public static Icon zoomInIcon() { return svg("zoom-in"); }
    public static Icon zoomOutIcon() { return svg("zoom-out"); }
    public static Icon zoomLayerIcon() { return svg("zoom-layer"); }
    public static Icon zoomAllIcon() { return svg("zoom-all"); }
    public static Icon viewPreviousIcon() { return svg("view-previous"); }
    public static Icon viewNextIcon() { return svg("view-next"); }
    public static Icon panIcon() { return svg("pan"); }
    public static Icon identifyIcon() { return svg("identify"); }
    public static Icon selectIcon() { return svg("select"); }
    public static Icon pointIcon() { return svg("point"); }
    public static Icon multiPointIcon() { return svg("multi-point"); }
    public static Icon lineIcon() { return svg("line"); }
    public static Icon polygonIcon() { return svg("polygon"); }
    public static Icon cutIcon() { return svg("cut"); }
    public static Icon holeIcon() { return svg("hole"); }
    public static Icon distanceIcon() { return svg("measure-distance"); }
    public static Icon areaIcon() { return svg("measure-area"); }
    public static Icon finishIcon() { return svg("finish"); }
    public static Icon cancelIcon() { return svg("cancel"); }
    public static Icon tableIcon() { return svg("table"); }
    public static Icon crsIcon() { return svg("crs"); }
    public static Icon exportIcon() { return svg("export"); }
    public static Icon propertiesIcon() { return svg("properties"); }
    public static Icon labelsIcon() { return svg("labels"); }
    public static Icon removeIcon() { return svg("remove"); }
    public static Icon upIcon() { return svg("up"); }
    public static Icon downIcon() { return svg("down"); }
    public static Icon undoIcon() { return svg("undo"); }
    public static Icon redoIcon() { return svg("redo"); }
    public static Icon visibleIcon() { return svg("visible"); }
    public static Icon hiddenIcon() { return svg("hidden"); }
    public static Icon renameIcon() { return svg("rename"); }
    public static Icon genericLayerIcon() { return svg("generic-layer"); }
    public static Icon moveVertexIcon() { return svg("move-vertex"); }
    public static Icon addVertexIcon() { return svg("add-vertex"); }
    public static Icon removeVertexIcon() { return svg("remove-vertex"); }
    public static Icon joinVerticesIcon() { return svg("join-vertices"); }
    public static Icon rectangleIcon() { return svg("rectangle"); }
    public static Icon circleIcon() { return svg("circle"); }
    public static Icon circleThreePointsIcon() { return svg("circle-3p"); }
    public static Icon moveFeatureIcon() { return svg("move-feature"); }
    public static Icon increaseAreaIcon() { return svg("increase-area"); }
    public static Icon decreaseAreaIcon() { return svg("decrease-area"); }
    public static Icon extendLineIcon() { return svg("extend-line"); }
    public static Icon shortenLineIcon() { return svg("shorten-line"); }
    public static Icon parallelIcon() { return svg("parallel"); }
    public static Icon perpendicularIcon() { return svg("perpendicular"); }
    public static Icon toolboxIcon() { return svg("toolbox"); }
    public static Icon basemapIcon() { return svg("basemap"); }
    public static Icon imageryIcon() { return svg("imagery"); }
    public static Icon wmsIcon() { return svg("wms"); }
    public static Icon drainageIcon() { return svg("drainage"); }
    public static Icon terrainAnalysisIcon() { return svg("terrain-analysis"); }

    private static Icon svg(String name) {
        String resourcePath = "icons/" + name + ".svg";
        try {
            return new FlatSVGIcon(resourcePath, SIZE, SIZE);
        } catch (Throwable ignored) {
            URL url = AppIcons.class.getResource("/" + resourcePath);
            if (url != null) {
                return new ImageIcon(url);
            }
            Icon fallback = UIManager.getIcon("Tree.leafIcon");
            if (fallback != null) {
                return fallback;
            }
            return new ImageIcon(new java.awt.image.BufferedImage(SIZE, SIZE, java.awt.image.BufferedImage.TYPE_INT_ARGB).getScaledInstance(SIZE, SIZE, Image.SCALE_SMOOTH));
        }
    }
}
