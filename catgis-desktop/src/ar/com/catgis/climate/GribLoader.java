package ar.com.catgis.climate;

import ar.com.catgis.NotificationManager;
import ar.com.catgis.RasterLayer;

import java.awt.Component;
import java.io.File;

/**
 * Explicit dependency gate for GRIB/GRIB2 files.
 *
 * The previous reflective decoder manufactured a grayscale checkerboard even
 * when the UCAR raster values could not be converted. Such an image must never
 * be exposed to users as scientific meteorological data.
 *
 * GRIB import stays disabled until a real-file, real-cell and CRS/time-axis
 * decoding contract is certified.
 */
public final class GribLoader {

    private GribLoader() {
    }

    public static boolean isCertified() {
        return false;
    }

    /**
     * Do not return a synthetic raster layer for a genuine GRIB file.
     * Users may convert GRIB to NetCDF externally and load that format.
     */
    public static RasterLayer loadGribFile(File file, Component parent) {
        if (file == null || !file.isFile()) {
            NotificationManager.error(parent, "Cargar GRIB",
                    "Archivo no encontrado: " + (file != null ? file.getName() : "null"));
            return null;
        }

        NotificationManager.warn(parent, "GRIB / GRIB2 no disponible",
                "La importacion directa de GRIB esta deshabilitada porque todavia no existe "
                        + "una validacion de valores, coordenadas y pasos temporales reales. "
                        + "No se generara ninguna capa simulada. "
                        + "Como alternativa, converti el archivo a NetCDF con una herramienta "
                        + "cientifica (por ejemplo, CDO) y carga el NetCDF.");
        return null;
    }
}
