package ar.com.catgis;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Immutable, already-quantized WGS84 request values; no I/O or mutable GIS state. */
final class OpenTopographyRequestBounds {
    private final String west;
    private final String east;
    private final String south;
    private final String north;

    private OpenTopographyRequestBounds(String west, String east, String south, String north) {
        this.west = west;
        this.east = east;
        this.south = south;
        this.north = north;
    }

    static OpenTopographyRequestBounds of(double west, double east, double south, double north) {
        requireFiniteArea(west, east, south, north);
        if (west < -180d || east > 180d || south < -90d || north > 90d) {
            throw new IllegalArgumentException("El area DEM debe estar dentro de longitudes [-180,180] "
                    + "y latitudes [-90,90]. No se recortaron las coordenadas.");
        }
        // DecimalFormat is mutable and not thread-safe. Preserve the existing
        // US/six-decimal/HALF_EVEN wire convention with an instance per request.
        DecimalFormat format = new DecimalFormat("0.000000", DecimalFormatSymbols.getInstance(Locale.US));
        OpenTopographyRequestBounds result = new OpenTopographyRequestBounds(
                format.format(west), format.format(east), format.format(south), format.format(north));
        if (result.eastValue() <= result.westValue() || result.northValue() <= result.southValue()) {
            throw new IllegalArgumentException("El area DEM se reduce a ancho o alto cero "
                    + "con la precision de seis decimales. Seleccione un area mayor.");
        }
        return result;
    }

    /** Also protects projected source coordinates before any reprojection. */
    static void requireFiniteArea(double west, double east, double south, double north) {
        if (!Double.isFinite(west) || !Double.isFinite(east)
                || !Double.isFinite(south) || !Double.isFinite(north)
                || east <= west || north <= south
                || !Double.isFinite(east - west) || !Double.isFinite(north - south)) {
            throw new IllegalArgumentException("El area DEM requiere coordenadas finitas "
                    + "con ancho y alto positivos y finitos.");
        }
    }

    String westText() { return west; }
    String eastText() { return east; }
    String southText() { return south; }
    String northText() { return north; }
    double westValue() { return Double.parseDouble(west); }
    double eastValue() { return Double.parseDouble(east); }
    double southValue() { return Double.parseDouble(south); }
    double northValue() { return Double.parseDouble(north); }
}
