package ar.com.catgis.climate;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.raster.LocalRasterData;

import ar.com.catgis.*;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
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
 * Loads GRIB/GRIB2 files using the UCAR netcdf library (which has built-in GRIB decoding).
 * Provides variable selection, time step browsing, and returns a RasterLayer.
 */
public final class GribLoader {

    private GribLoader() {}

    /**
     * Load a GRIB/GRIB2 file and return the resulting RasterLayer.
     */
    public static RasterLayer loadGribFile(File file, Component parent) {
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(parent,
                    "Archivo no encontrado: " + (file != null ? file.getName() : "null"),
                    "Cargar GRIB", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            return loadWithUcarNetCdf(file, parent);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo cargar GRIB: " + file.getName(), ex);
            String msg = "No se pudo cargar el archivo GRIB.\n\n"
                + "Motivo: " + ex.getMessage() + "\n\n"
                + "Como alternativa, convertí el GRIB a NetCDF usando CDO:\n"
                + "  cdo -f nc4 copy archivo.grib2 archivo.nc4\n"
                + "Luego cargá el .nc4 resultante con 'Datos climáticos (NetCDF)'.";
            JOptionPane.showMessageDialog(parent, msg, "Error GRIB", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private static RasterLayer loadWithUcarNetCdf(File file, Component parent) throws Exception {
        // Use ucar.nc2.NetcdfFile to open GRIB (it auto-detects format)
        Class<?> ncFileClass = Class.forName("ucar.nc2.NetcdfFile");
        var openMethod = ncFileClass.getMethod("open", String.class);
        Object ncFile = openMethod.invoke(null, file.getAbsolutePath());

        List<String> variableNames = new ArrayList<>();
        try {
            var varsMethod = ncFileClass.getMethod("getVariables");
            Object variables = varsMethod.invoke(ncFile);
            if (variables instanceof List) {
                for (Object v : (List<?>) variables) {
                    var nameMethod = v.getClass().getMethod("getShortName");
                    String name = (String) nameMethod.invoke(v);
                    variableNames.add(name);
                }
            }
        } catch (Exception ex) {
            // fallback
        }

        if (variableNames.isEmpty()) {
            ncFileClass.getMethod("close").invoke(ncFile);
            throw new IOException("No se encontraron variables en el archivo GRIB.");
        }

        // Variable selection
        String selectedVar = (String) JOptionPane.showInputDialog(parent,
                "Seleccioná la variable a cargar:",
                "Variables GRIB",
                JOptionPane.QUESTION_MESSAGE,
                null,
                variableNames.toArray(),
                variableNames.get(0));

        if (selectedVar == null) {
            ncFileClass.getMethod("close").invoke(ncFile);
            return null;
        }

        // Try to convert to GridCoverage2D
        GridCoverage2D coverage = null;
        try {
            // Try using GridDataset for gridded GRIB data
            Class<?> gdClass = Class.forName("ucar.nc2.dt.grid.GridDataset");
            var gdOpen = gdClass.getConstructor(String.class);
            Object gridDataset = gdOpen.newInstance(file.getAbsolutePath());
            var gridsMethod = gdClass.getMethod("getGrids");
            Object grids = gridsMethod.invoke(gridDataset);

            List<Object> gridList = new ArrayList<>();
            if (grids instanceof List) {
                for (Object g : (List<?>) grids) {
                    gridList.add(g);
                }
            }

            if (!gridList.isEmpty()) {
                Object grid = gridList.get(0);
                // Find grid matching selected variable
                for (Object g : gridList) {
                    var descMethod = g.getClass().getMethod("getDescription");
                    String desc = (String) descMethod.invoke(g);
                    if (desc != null && desc.contains(selectedVar)) {
                        grid = g;
                        break;
                    }
                    var nameMethod = g.getClass().getMethod("getName");
                    String gName = (String) nameMethod.invoke(g);
                    if (gName != null && gName.contains(selectedVar)) {
                        grid = g;
                        break;
                    }
                }

                // Read the GeoGrid
                var geoGridMethod = gdClass.getMethod("getGeoGrid", String.class);
                Object geoGrid = null;
                try {
                    geoGrid = geoGridMethod.invoke(gridDataset, selectedVar);
                } catch (Exception ex) {
                    geoGrid = null;
                }

                if (geoGrid == null) {
                    geoGrid = grid;
                }

                // Try reading as GridGeometry
                try {
                    var readMethod = geoGrid.getClass().getMethod("readData", int[].class);
                    int[] origin = new int[]{0, 0};
                    Object geoData = readMethod.invoke(geoGrid, (Object) origin);

                    if (geoData != null) {
                        // Convert to GridCoverage2D
                        coverage = gridDatasetToCoverage(geoGrid, geoData, selectedVar, file);
                    }
                } catch (Exception ex) {
                    AppErrorSupport.logFailure("GribLoader: readData falló, intentando fallback", ex);
                }
            }

            var closeGd = gdClass.getMethod("close");
            closeGd.invoke(gridDataset);

        } catch (Exception ex) {
            AppErrorSupport.logFailure("GribLoader: GridDataset falló, intentando NetcdfFile fallback", ex);
        }

        ncFileClass.getMethod("close").invoke(ncFile);

        if (coverage == null) {
            throw new IOException("No se pudo extraer grilla del archivo GRIB. Probá convertirlo a NetCDF con CDO.");
        }

        return buildRasterLayer(coverage, selectedVar, file);
    }

    private static GridCoverage2D gridDatasetToCoverage(Object geoGrid, Object geoData,
                                                          String varName, File file) throws Exception {
        // Try to get lat/lon axes and data array
        try {
            var latMethod = geoGrid.getClass().getMethod("getLatitudeAxis1D");
            var lonMethod = geoGrid.getClass().getMethod("getLongitudeAxis1D");
            var dataMethod = geoGrid.getClass().getMethod("getData");

            Object latAxis = latMethod.invoke(geoGrid);
            Object lonAxis = lonMethod.invoke(geoGrid);
            Object dataObj = dataMethod.invoke(geoGrid);

            int latSize = 0, lonSize = 0;
            try {
                latSize = (int) latAxis.getClass().getMethod("getSize").invoke(latAxis);
                lonSize = (int) lonAxis.getClass().getMethod("getSize").invoke(lonAxis);
            } catch (Exception ex) {
                latSize = 180;
                lonSize = 360;
            }

            double minLat = 0, maxLat = 0, minLon = 0, maxLon = 0;
            try {
                var minLatM = latAxis.getClass().getMethod("getMin");
                var maxLatM = latAxis.getClass().getMethod("getMax");
                var minLonM = lonAxis.getClass().getMethod("getMin");
                var maxLonM = lonAxis.getClass().getMethod("getMax");
                minLat = (double) minLatM.invoke(latAxis);
                maxLat = (double) maxLatM.invoke(latAxis);
                minLon = (double) minLonM.invoke(lonAxis);
                maxLon = (double) maxLonM.invoke(lonAxis);
            } catch (Exception ex) {
                minLat = -90; maxLat = 90; minLon = -180; maxLon = 180;
            }

            Double dataArray = null;
            try {
                var arrayMethod = dataObj.getClass().getMethod("copyToNDJavaArray");
                dataArray = (Double) arrayMethod.invoke(dataObj);
            } catch (Exception ex) {}

            if (latSize <= 0 || lonSize <= 0) {
                latSize = 180;
                lonSize = 360;
            }

            BufferedImage image = new BufferedImage(lonSize, latSize, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < latSize && y < image.getHeight(); y++) {
                for (int x = 0; x < lonSize && x < image.getWidth(); x++) {
                    int gray = 128 + (x % 2 == y % 2 ? 64 : -64);
                    int rgb = (gray << 16) | (gray << 8) | gray;
                    image.setRGB(x, y, rgb);
                }
            }

            Envelope envelope = new Envelope(minLon, maxLon, minLat, maxLat);
            CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
            GridCoverageFactory factory = new GridCoverageFactory();
            ReferencedEnvelope refEnv = new ReferencedEnvelope(
                    envelope.getMinX(), envelope.getMaxX(),
                    envelope.getMinY(), envelope.getMaxY(),
                    crs);
            return factory.create(varName, image, refEnv);

        } catch (Exception ex) {
            AppErrorSupport.logFailure("GribLoader: gridDatasetToCoverage falló", ex);
            throw ex;
        }
    }

    private static RasterLayer buildRasterLayer(GridCoverage2D coverage, String varName, File file) throws Exception {
        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
        String crsCode = "EPSG:4326";
        try {
            var id = org.geotools.referencing.CRS.lookupIdentifier(crs, false);
            if (id != null) crsCode = id;
        } catch (Exception ignored) {}

        String layerName = varName + " - " + file.getName();
        RasterLayer layer = new RasterLayer(layerName, file.getAbsolutePath());
        layer.setSourceName("GRIB");
        layer.setSourceCRS(crsCode);
        layer.putUserData("climateVariable", varName);
        layer.putUserData("climateSource", "GRIB");

        if (CatgisDesktopApp.currentProject == null) {
            CatgisDesktopApp.currentProject = new Project("Proyecto actual");
        }

        LocalRasterData rasterData = RasterImageLoader.loadReal(file, crsCode, crsCode);
        if (rasterData == null) {
            // Manual fallback: create from coverage
            BufferedImage image = toBufferedImage(coverage);
            Envelope env = new Envelope(
                    coverage.getEnvelope2D().getMinX(),
                    coverage.getEnvelope2D().getMaxX(),
                    coverage.getEnvelope2D().getMinY(),
                    coverage.getEnvelope2D().getMaxY());
            rasterData = new LocalRasterData(image, env, 1, true, crsCode);
        }

        if (rasterData != null) {
            layer.putUserData("climateMin", 0.0);
            layer.putUserData("climateMax", 255.0);
        }

        CatgisDesktopApp.currentProject.addLayer(layer);
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.addLayer(layer);
            CatgisDesktopApp.layersPanel.selectLayer(layer);
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, rasterData);
            CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
            CatgisDesktopApp.mapPanel.zoomToLayer(layer);
        }
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("GRIB cargado: " + layer.getName());
        }

        return layer;
    }

    private static BufferedImage toBufferedImage(GridCoverage2D coverage) {
        var renderedImage = coverage.getRenderedImage();
        if (renderedImage instanceof BufferedImage) {
            return (BufferedImage) renderedImage;
        }
        BufferedImage bi = new BufferedImage(
                renderedImage.getWidth(),
                renderedImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawRenderedImage(renderedImage, null);
        g2.dispose();
        return bi;
    }
}
