package ar.com.catgis.climate;

/**
 * GRIB2 file loader - NOT SUPPORTED in this version.
 * 
 * GRIB2 support requires the GeoTools gt-grib module with native GDAL
 * bindings, which adds significant complexity and platform dependencies.
 * 
 * Workaround: convert GRIB2 to NetCDF using CDO:
 *   cdo -f nc4 copy input.grib2 output.nc4
 * 
 * Alternative: use CDO from the command line within CATGIS.
 * 
 * Expected when GRIB support is implemented:
 * - org.geotools:gt-grib:34.0 dependency in build.gradle
 * - GribLoader.read() method returning GridCoverage2D
 * - Variable and time step selection dialog
 */
public final class GribLoader {
    private GribLoader() {}
    
    public static String getSupportMessage() {
        return "GRIB2 no está soportado en CATGIS en esta versión.\n\n"
            + "Para usar datos GRIB2:\n"
            + "1. Instalá CDO (Climate Data Operators)\n"
            + "2. Convertí a NetCDF:\n"
            + "   cdo -f nc4 copy datos.grib2 datos.nc4\n"
            + "3. Cargá el archivo .nc4 resultante con 'Datos climáticos (NetCDF)'\n\n"
            + "Integración GRIB nativa está planificada para una versión futura.";
    }
}
