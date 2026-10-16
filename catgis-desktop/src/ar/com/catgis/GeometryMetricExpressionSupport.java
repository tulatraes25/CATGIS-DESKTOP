package ar.com.catgis;

final class GeometryMetricExpressionSupport {
    private GeometryMetricExpressionSupport() {}

    static double requireFinite(double value, String token) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    "No se puede calcular " + token
                            + " sin un CRS de origen válido para la capa.");
        }
        return value;
    }
}
