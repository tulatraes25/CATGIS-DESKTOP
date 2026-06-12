package ar.com.catgis;

import java.io.File;

/**
 * Resolves GDAL executables safely — never falls back to PATH lookup.
 * Requires OSGeo4W or OSGeo4W64 to be installed at standard locations.
 */
final class GdalSupport {

    private static final File OSGEO4W = resolveOsgeoPath("CATGIS_OSGEO4W", "C:\\OSGeo4W\\bin");
    private static final File OSGEO4W64 = resolveOsgeoPath("CATGIS_OSGEO4W64", "C:\\OSGeo4W64\\bin");

    private static File resolveOsgeoPath(String envVar, String defaultPath) {
        String env = System.getenv(envVar);
        if (env != null && !env.isBlank()) {
            return new File(env);
        }
        return new File(defaultPath);
    }

    private GdalSupport() {
    }

    /**
     * Resolves a GDAL executable to its absolute path.
     *
     * @param exeName executable name (e.g. "gdal_translate.exe")
     * @return absolute path to the executable
     * @throws GdalNotAvailableException if GDAL is not found at the expected locations
     */
    static String resolve(String exeName) {
        File candidate = new File(OSGEO4W, exeName);
        if (candidate.exists()) {
            return candidate.getAbsolutePath();
        }
        candidate = new File(OSGEO4W64, exeName);
        if (candidate.exists()) {
            return candidate.getAbsolutePath();
        }
        throw new GdalNotAvailableException(
                "No se encontro " + exeName + " en " + OSGEO4W.getAbsolutePath()
                + " ni en " + OSGEO4W64.getAbsolutePath() + ". "
                + "Instala OSGeo4W para habilitar el procesamiento de imagenes raster."
        );
    }

    static final class GdalNotAvailableException extends RuntimeException {
        GdalNotAvailableException(String message) {
            super(message);
        }
    }
}
