package ar.com.catgis.climate;

import ar.com.catgis.CatgisLogger;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.raster.LocalRasterData;

import ar.com.catgis.*;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads NetCDF files (.nc, .nc4) containing gridded climate variables.
 * Uses GeoTools' gt-netcdf module when available, with ucar-netcdf fallback.
 */
public final class NetCdfLoader {

    private NetCdfLoader() {}

    /**
     * Try to load a NetCDF file and return the resulting RasterLayer.
     * Shows variable and time step selection dialogs.
     * Returns null if user cancels or loading fails.
     */
    public static RasterLayer loadNetCdfFile(File file, Component parent) {
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(parent,
                    "Archivo no encontrado: " + (file != null ? file.getName() : "null"),
                    "Cargar NetCDF", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            // Try GeoTools gt-netcdf first
            return loadWithGeoToolsNetCdf(file, parent);
        } catch (Exception gtEx) {
            // Fall back to ucar-netcdf library
            return loadWithUcarNetCdf(file, parent);
        }
    }

    /**
     * Load NetCDF using GeoTools' gt-netcdf module (GridFormatFinder).
     */
    private static RasterLayer loadWithGeoToolsNetCdf(File file, Component parent) {
        try {
            AbstractGridFormat format = (AbstractGridFormat)
                    Class.forName("org.geotools.gce.netcdf.NetCDFFormat")
                            .getDeclaredField("INSTANCE")
                            .get(null);
            var reader = format.getReader(file);
            if (reader == null) {
                return null; // fall through to ucar
            }

            var coverage = reader.read((org.geotools.api.parameter.GeneralParameterValue[]) null);
            if (coverage == null) return null;

            BufferedImage image = toBufferedImage((GridCoverage2D) coverage);
            if (image == null) return null;

            Envelope envelope = extractEnvelope((GridCoverage2D) coverage);
            if (envelope == null || envelope.isNull()) {
                envelope = new Envelope(0, image.getWidth(), 0, image.getHeight());
            }

            String crsCode = "";
            try {
                CoordinateReferenceSystem crs = ((GridCoverage2D) coverage).getCoordinateReferenceSystem2D();
                if (crs != null) crsCode = CRS.toSRS(crs, true);
            } catch (Exception ignored) { CatgisLogger.warn("NetCdfLoader: operation failed", ignored); }

            return createRasterLayer(file, image, envelope, crsCode);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Load NetCDF using the ucar-netcdf library directly.
     * Reads variable data, resamples to image, creates RasterLayer.
     */
    private static RasterLayer loadWithUcarNetCdf(File file, Component parent) {
        try {
            Class<?> ncFileClass = Class.forName("ucar.nc2.NetcdfFile");
            Object ncFile = ncFileClass.getMethod("open", String.class).invoke(null, file.getAbsolutePath());

            // List available variables
            List<VariableInfo> variables = listVariables(ncFile, ncFileClass);
            if (variables.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "No se encontraron variables en el archivo NetCDF.",
                        "Cargar NetCDF", JOptionPane.WARNING_MESSAGE);
                ncFileClass.getMethod("close").invoke(ncFile);
                return null;
            }

            // Select variable
            VariableInfo selectedVar = selectVariableDialog(parent, variables);
            if (selectedVar == null) {
                ncFileClass.getMethod("close").invoke(ncFile);
                return null;
            }

            // Check for time dimension and select time step
            int timeStep = 0;
            List<Integer> timeSteps = getTimeSteps(ncFile, ncFileClass, selectedVar);
            if (timeSteps.size() > 1) {
                Integer selectedTime = selectTimeStepDialog(parent, timeSteps);
                if (selectedTime == null) {
                    ncFileClass.getMethod("close").invoke(ncFile);
                    return null;
                }
                timeStep = selectedTime;
            }

            // Read variable data as float array
            float[] data = readVariableData(ncFile, ncFileClass, selectedVar, timeStep);
            if (data == null) {
                JOptionPane.showMessageDialog(parent,
                        "No se pudieron leer los datos de la variable: " + selectedVar.name,
                        "Cargar NetCDF", JOptionPane.ERROR_MESSAGE);
                ncFileClass.getMethod("close").invoke(ncFile);
                return null;
            }

            // Get dimensions
            int[] dims = getVariableDimensions(ncFile, ncFileClass, selectedVar, timeStep);
            int width = dims[1]; // lon/x dimension
            int height = dims[0]; // lat/y dimension

            // Try to read lat/lon arrays for georeferencing
            double minLon = -180, maxLon = 180, minLat = -90, maxLat = 90;
            try {
                double[] lats = readDoubleArray(ncFile, ncFileClass, "lat", "latitude", "y", "Latitude");
                double[] lons = readDoubleArray(ncFile, ncFileClass, "lon", "longitude", "x", "Longitude");
                if (lons != null && lons.length > 0) {
                    minLon = lons[0];
                    maxLon = lons[lons.length - 1];
                    // Handle 0-360 to -180-180
                    if (maxLon > 180) { minLon -= 180; maxLon -= 180; }
                }
                if (lats != null && lats.length > 0) {
                    minLat = Math.min(lats[0], lats[lats.length - 1]);
                    maxLat = Math.max(lats[0], lats[lats.length - 1]);
                }
            } catch (Exception ignored) { CatgisLogger.warn("NetCdfLoader: operation failed", ignored); }

            // Determine CRS
            String crsCode = "EPSG:4326";

            // Find min/max for image stretching
            float minVal = Float.POSITIVE_INFINITY;
            float maxVal = Float.NEGATIVE_INFINITY;
            for (float v : data) {
                if (Float.isFinite(v) && !isNoData(selectedVar, v)) {
                    if (v < minVal) minVal = v;
                    if (v > maxVal) maxVal = v;
                }
            }
            if (minVal >= maxVal) { minVal = 0; maxVal = 100; }

            // Build image
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = y * width + x;
                    float val = idx < data.length ? data[idx] : Float.NaN;
                    int gray = 0;
                    if (Float.isFinite(val) && !isNoData(selectedVar, val)) {
                        float normalized = (val - minVal) / (maxVal - minVal);
                        if (normalized < 0) normalized = 0;
                        if (normalized > 1) normalized = 1;
                        gray = (int) (normalized * 255);
                    }
                    int rgb = (gray << 16) | (gray << 8) | gray;
                    image.setRGB(x, y, rgb);
                }
            }

            Envelope envelope = new Envelope(minLon, maxLon, minLat, maxLat);
            RasterLayer layer = createRasterLayer(file, image, envelope, crsCode);

            // Store climate metadata
            ClimateColormaps.ClimateVariable suggested = ClimateColormaps.getSuggestedVariable(selectedVar.name);
            layer.putUserData("climateVariable", selectedVar.name);
            layer.putUserData("climateVariableType", suggested.name());
            layer.putUserData("climateUnits", selectedVar.units);
            layer.putUserData("climateMin", minVal);
            layer.putUserData("climateMax", maxVal);
            layer.putUserData("climateTimeStep", timeStep);
            layer.putUserData("climateSource", "NetCDF (" + file.getName() + ")");

            ncFileClass.getMethod("close").invoke(ncFile);
            return layer;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "Error al cargar NetCDF: " + e.getMessage() + "\n\n"
                            + "Verifica que el archivo .nc sea valido y contenga variables grid 2D.",
                    "Cargar NetCDF", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // ---- Helper methods for ucar-netcdf library access via reflection ----

    private static class VariableInfo {
        final String name;
        final String units;
        final String description;
        final int rank;
        VariableInfo(String name, String units, String description, int rank) {
            this.name = name;
            this.units = units != null ? units : "";
            this.description = description != null ? description : "";
            this.rank = rank;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<VariableInfo> listVariables(Object ncFile, Class<?> ncFileClass) throws Exception {
        List<VariableInfo> vars = new ArrayList<>();
        try {
            Object varList = ncFileClass.getMethod("getVariables").invoke(ncFile);
            if (varList instanceof List) {
                for (Object v : (List<Object>) varList) {
                    Class<?> vClass = v.getClass();
                    String name = (String) vClass.getMethod("getShortName").invoke(v);
                    String units = "";
                    String desc = "";
                    int rank = 0;
                    try { units = (String) vClass.getMethod("getUnitsString").invoke(v); } catch (Exception ignored) { CatgisLogger.warn("NetCdfLoader: operation failed", ignored); }
                    try { desc = (String) vClass.getMethod("getDescription").invoke(v); } catch (Exception ignored) { CatgisLogger.warn("NetCdfLoader: operation failed", ignored); }
                    try { rank = ((int[]) vClass.getMethod("getShape").invoke(v)).length; } catch (Exception ignored) { CatgisLogger.warn("NetCdfLoader: operation failed", ignored); }
                    if (!name.isEmpty() && rank >= 2) {
                        vars.add(new VariableInfo(name, units, desc, rank));
                    }
                }
            }
        } catch (Exception e) {
            // Method signature may differ across versions
            try {
                Object varList = ncFileClass.getMethod("getVariables").invoke(ncFile);
                if (varList instanceof List) {
                    for (Object v : (List<Object>) varList) {
                        String name = v.getClass().getMethod("getName").invoke(v).toString();
                        vars.add(new VariableInfo(name, "", "", 2));
                    }
                }
            } catch (Exception e2) {
                // Cannot list variables, return empty
            }
        }
        return vars;
    }

    private static VariableInfo selectVariableDialog(Component parent, List<VariableInfo> variables) {
        String[] names = new String[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            VariableInfo v = variables.get(i);
            names[i] = v.name + " (" + v.units + ")" + (v.description.isEmpty() ? "" : " - " + v.description);
        }

        String selected = (String) JOptionPane.showInputDialog(parent,
                "Selecciona la variable climática a cargar:",
                "Cargar NetCDF - Variable",
                JOptionPane.QUESTION_MESSAGE,
                null, names, names[0]);

        if (selected == null) return null;
        for (int i = 0; i < variables.size(); i++) {
            if (names[i].equals(selected)) return variables.get(i);
        }
        return variables.get(0);
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> getTimeSteps(Object ncFile, Class<?> ncFileClass, VariableInfo var) throws Exception {
        List<Integer> steps = new ArrayList<>();
        try {
            Object v = findVariable(ncFile, ncFileClass, var.name);
            if (v == null) return steps;
            int[] shape = (int[]) v.getClass().getMethod("getShape").invoke(v);
            if (shape.length >= 3 && shape[0] > 1) {
                for (int i = 0; i < shape[0]; i++) steps.add(i);
            } else {
                steps.add(0);
            }
        } catch (Exception e) {
            steps.add(0);
        }
        return steps;
    }

    private static Integer selectTimeStepDialog(Component parent, List<Integer> steps) {
        String[] labels = new String[steps.size()];
        for (int i = 0; i < steps.size(); i++) {
            labels[i] = "Paso temporal " + (steps.get(i) + 1) + " / " + steps.size();
        }
        String selected = (String) JOptionPane.showInputDialog(parent,
                "El archivo contiene múltiples pasos temporales.\nSelecciona el paso a cargar:",
                "Cargar NetCDF - Paso temporal",
                JOptionPane.QUESTION_MESSAGE,
                null, labels, labels[0]);
        if (selected == null) return null;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(selected)) return steps.get(i);
        }
        return steps.get(0);
    }

    private static Object findVariable(Object ncFile, Class<?> ncFileClass, String name) throws Exception {
        try {
            return ncFileClass.getMethod("findVariable", String.class).invoke(ncFile, name);
        } catch (Exception e) {
            try {
                for (Object v : (List<Object>) ncFileClass.getMethod("getVariables").invoke(ncFile)) {
                    String vn = v.getClass().getMethod("getShortName").invoke(v).toString();
                    if (vn.equals(name)) return v;
                }
            } catch (Exception ignored) { CatgisLogger.warn("NetCdfLoader: operation failed", ignored); }
            return null;
        }
    }

    private static float[] readVariableData(Object ncFile, Class<?> ncFileClass, VariableInfo var, int timeStep) {
        try {
            Object v = findVariable(ncFile, ncFileClass, var.name);
            if (v == null) return null;
            int[] shape = (int[]) v.getClass().getMethod("getShape").invoke(v);

            int total = 1;
            for (int s : shape) total *= s;

            Object array;
            if (shape.length >= 3) {
                // 3D+ variable: slice at time step
                int[] origin = new int[shape.length];
                int[] size = new int[shape.length];
                for (int i = 0; i < shape.length; i++) {
                    size[i] = shape[i];
                }
                size[0] = 1;
                origin[0] = timeStep;
                array = v.getClass().getMethod("read", int[].class, int[].class).invoke(v, origin, size);
            } else {
                array = v.getClass().getMethod("read").invoke(v);
            }

            // Convert to float array
            return convertToFloatArray(array);
        } catch (Exception e) {
            return null;
        }
    }

    private static int[] getVariableDimensions(Object ncFile, Class<?> ncFileClass, VariableInfo var, int timeStep) throws Exception {
        Object v = findVariable(ncFile, ncFileClass, var.name);
        if (v == null) return new int[]{1, 1};
        int[] shape = (int[]) v.getClass().getMethod("getShape").invoke(v);
        if (shape.length == 2) return shape;
        if (shape.length >= 3) {
            return new int[]{shape[shape.length - 2], shape[shape.length - 1]};
        }
        return new int[]{1, 1};
    }

    private static float[] convertToFloatArray(Object array) {
        try {
            // Try direct cast to Array
            if (array instanceof ucar.ma2.Array) {
                ucar.ma2.Array ma2Array = (ucar.ma2.Array) array;
                return (float[]) ma2Array.copyTo1DJavaArray();
            }
            // Try reflection
            java.lang.reflect.Method copyTo1d = array.getClass().getMethod("copyTo1DJavaArray");
            Object raw = copyTo1d.invoke(array);
            if (raw instanceof float[]) return (float[]) raw;
            if (raw instanceof double[]) {
                double[] doubles = (double[]) raw;
                float[] floats = new float[doubles.length];
                for (int i = 0; i < doubles.length; i++) floats[i] = (float) doubles[i];
                return floats;
            }
            if (raw instanceof int[]) {
                int[] ints = (int[]) raw;
                float[] floats = new float[ints.length];
                for (int i = 0; i < ints.length; i++) floats[i] = ints[i];
                return floats;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static double[] readDoubleArray(Object ncFile, Class<?> ncFileClass, String... names) {
        for (String name : names) {
            try {
                Object v = ncFileClass.getMethod("findVariable", String.class).invoke(ncFile, name);
                if (v == null) continue;
                Object array = v.getClass().getMethod("read").invoke(v);
                float[] values = convertToFloatArray(array);
                if (values == null || values.length == 0) continue;
                double[] result = new double[values.length];
                for (int i = 0; i < values.length; i++) result[i] = values[i];
                return result;
            } catch (Exception ignored) { CatgisLogger.warn("NetCdfLoader: operation failed", ignored); }
        }
        return null;
    }

    private static boolean isNoData(VariableInfo var, float value) {
        return Float.isNaN(value) || Float.isInfinite(value)
                || (var.name.contains("precip") && value < -999)
                || value < -9e10f; // typical NetCDF fill values
    }

    // ---- Common helper methods ----

    private static RasterLayer createRasterLayer(File file, BufferedImage image, Envelope envelope, String crsCode) {
        String projectCRS = AppContext.project() != null
                ? AppContext.project().getProjectCRS() : "";

        LocalRasterData rasterData = new LocalRasterData(image, envelope, 1, true,
                crsCode, "preview",
                !crsCode.isBlank() && !projectCRS.isBlank() ? projectCRS : crsCode);

        RasterLayer layer = new RasterLayer(file.getName(), file.getAbsolutePath());
        layer.setVisible(true);
        layer.setSourceName("Datos climáticos");
        layer.setFeatureCount(1);
        layer.setSourceCRS(crsCode);
        layer.setRasterMode("preview");

        // Add to project
        if (AppContext.project() == null) {
            AppContext.setCurrentProject(new Project("Proyecto actual"));
        }
        AppContext.project().addLayer(layer);
        CatgisDesktopApp.markProjectDirty();
        AppContext.addLayer(layer);
        CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, rasterData);
        CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
        CatgisDesktopApp.mapPanel.zoomToLayer(layer);
        return layer;
    }

    private static BufferedImage toBufferedImage(GridCoverage2D coverage) {
        var ri = coverage.getRenderedImage();
        if (ri instanceof BufferedImage) return (BufferedImage) ri;
        try {
            BufferedImage out = new BufferedImage(ri.getWidth(), ri.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = out.createGraphics();
            g.drawRenderedImage(ri, new java.awt.geom.AffineTransform());
            g.dispose();
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    private static Envelope extractEnvelope(GridCoverage2D coverage) {
        try {
            Object ge = coverage.getEnvelope();
            if (ge != null) {
                java.lang.reflect.Method getMinimum = ge.getClass().getMethod("getMinimum", int.class);
                java.lang.reflect.Method getMaximum = ge.getClass().getMethod("getMaximum", int.class);
                double minX = ((Number) getMinimum.invoke(ge, 0)).doubleValue();
                double maxX = ((Number) getMaximum.invoke(ge, 0)).doubleValue();
                double minY = ((Number) getMinimum.invoke(ge, 1)).doubleValue();
                double maxY = ((Number) getMaximum.invoke(ge, 1)).doubleValue();
                return new Envelope(minX, maxX, minY, maxY);
            }
        } catch (Exception ignored) { CatgisLogger.warn("NetCdfLoader: operation failed", ignored); }
        return null;
    }

    /**
     * GRIB support is not fully implemented.
     * GRIB2 files require the GeoTools grib module which has complex dependencies.
     * See GribLoader.java for more details.
     */
    public static boolean supportsGrib() {
        return false;
    }

    /**
     * Check if a file looks like a NetCDF file by extension.
     */
    public static boolean isNetCdfFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".nc") || lower.endsWith(".nc4") || lower.endsWith(".cdf");
    }

    /**
     * Check if a file looks like a GRIB file by extension.
     */
    public static boolean isGribFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".grib") || lower.endsWith(".grib2") || lower.endsWith(".grb") || lower.endsWith(".grb2");
    }
}
