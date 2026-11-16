package ar.com.catgis.catmap;

import ar.com.catgis.AppContext;
import ar.com.catgis.CsvTableReader;
import ar.com.catgis.OpenFileAction;
import ar.com.catgis.TablePointData;
import ar.com.catgis.TablePointLayerBuilder;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Quick map for CATMAP standalone: open a data file without a previous project.
 * Vector/raster formats reuse the hardened CATGIS open flow; CSV is turned into
 * a point layer with an explicit CRS (inferred only for obvious lat/lon columns).
 */
public final class QuickMapService {

    private static final Set<String> VECTOR_EXTENSIONS = Set.of(
            "shp", "geojson", "json", "kml", "kmz", "gpkg", "spatialite", "sl3", "sl4",
            "fgb", "gpx", "dxf", "dwg", "parquet", "geoparquet");
    private static final Set<String> RASTER_EXTENSIONS = Set.of(
            "tif", "tiff", "img", "png", "jpg", "jpeg", "bmp", "gif",
            "nc", "nc4", "grib", "grb", "grb2");
    private static final Set<String> X_NAMES = Set.of(
            "x", "lon", "lng", "long", "longitud", "este", "easting", "coord_x", "xcoord");
    private static final Set<String> Y_NAMES = Set.of(
            "y", "lat", "latitud", "norte", "northing", "coord_y", "ycoord");
    private static final Set<String> LATLON_MARKERS = Set.of(
            "lat", "latitud", "lon", "lng", "long", "longitud");

    private QuickMapService() {}

    /** Human-readable summary of a bulk quick-map load (non-modal toast). */
    public static String bulkSummary(int loaded, int failed) {
        if (failed <= 0) {
            return loaded + " conjunto(s) de datos cargados";
        }
        if (loaded <= 0) {
            return "No se pudo cargar ningun conjunto (" + failed + " con error)";
        }
        return loaded + " conjunto(s) cargados, " + failed + " con error";
    }

    /** A CSV converted into a point layer, ready to be added to a project. */
    public record CsvPointLayer(Layer layer, ShapefileData data) {}

    public static boolean canOpen(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String extension = extension(file);
        return "csv".equals(extension)
                || VECTOR_EXTENSIONS.contains(extension)
                || RASTER_EXTENSIONS.contains(extension);
    }

    /**
     * Open a data file directly into CATMAP. Unsupported files return false;
     * corrupt supported files throw so the UI can report the real problem.
     */
    public static boolean open(File file, Component parent, String csvCrs) throws IOException {
        if (!canOpen(file)) {
            return false;
        }
        if (!"csv".equals(extension(file))) {
            return OpenFileAction.openSelectedFile(file, null, parent);
        }
        CsvPointLayer csv = parseCsvPoints(file, csvCrs);
        installCsvLayer(csv);
        return true;
    }

    /**
     * Headless CSV parsing: detects X/Y columns, resolves the CRS (explicit or
     * inferred from lat/lon column names) and builds a point layer.
     */
    public static CsvPointLayer parseCsvPoints(File file, String csvCrs) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IOException("El archivo CSV no existe.");
        }
        TablePointData table;
        try {
            table = CsvTableReader.read(file);
        } catch (Exception invalid) {
            throw new IOException("No se pudo leer el CSV: " + file.getName(), invalid);
        }
        List<String> columns = table.getColumns();
        String xColumn = findColumn(columns, X_NAMES);
        String yColumn = findColumn(columns, Y_NAMES);
        if (xColumn == null || yColumn == null) {
            throw new IOException("El CSV no tiene columnas X/Y reconocibles "
                    + "(x/lon/longitud y y/lat/latitud).");
        }
        String crs = resolveCrs(csvCrs, columns);
        Layer layer = new Layer(file.getName(), file.getAbsolutePath(), "VECTOR");
        try {
            ShapefileData data = TablePointLayerBuilder.build(table, xColumn, yColumn, crs, null);
            layer.setVisible(true);
            layer.setSourceName(file.getName());
            layer.setFeatureCount(data.getFeatureCount());
            layer.setSourceCRS(crs);
            return new CsvPointLayer(layer, data);
        } catch (Exception invalid) {
            throw new IOException("No se pudieron crear puntos desde el CSV: "
                    + file.getName(), invalid);
        }
    }

    /** Apply the CSV layer to the live CATMAP workspace. */
    public static void installCsvLayer(CsvPointLayer csv) {
        if (csv == null) {
            return;
        }
        Project project = AppContext.project();
        if (project == null) {
            project = new Project("Mapa rápido");
            AppContext.setCurrentProject(project);
        }
        project.setProjectCRS(csv.layer().getSourceCRS());
        project.addLayer(csv.layer());
        AppContext.addLayer(csv.layer());
        if (AppContext.mapPanel() != null) {
            AppContext.mapPanel().addOrUpdateShapefileLayer(csv.layer(), csv.data());
            AppContext.mapPanel().resetView();
            AppContext.mapPanel().repaint();
        }
    }

    static String resolveCrs(String requestedCrs, List<String> columns) throws IOException {
        if (requestedCrs != null && !requestedCrs.isBlank()) {
            return requestedCrs.trim().toUpperCase(Locale.ROOT);
        }
        for (String column : columns) {
            String normalized = normalize(column);
            if (LATLON_MARKERS.contains(normalized)) {
                return "EPSG:4326";
            }
        }
        throw new IOException("El CSV no declara CRS y sus columnas no son lat/lon: "
                + "indique el CRS explícitamente.");
    }

    static String findColumn(List<String> columns, Set<String> candidates) {
        for (String column : columns) {
            String normalized = normalize(column);
            if (candidates.contains(normalized)) {
                return column;
            }
        }
        return null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < decomposed.length(); i++) {
            char c = decomposed.charAt(i);
            if (Character.getType(c) != Character.NON_SPACING_MARK) {
                out.append(Character.toLowerCase(c));
            }
        }
        return out.toString();
    }

    private static String extension(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : "";
    }
}
