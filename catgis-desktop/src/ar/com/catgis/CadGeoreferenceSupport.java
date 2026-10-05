package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CadGeoreferenceSupport {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private CadGeoreferenceSupport() {
    }

    public static Result computeTwoPoint(ControlPoint source1,
                                         ControlPoint source2,
                                         ControlPoint target1,
                                         ControlPoint target2) {
        return computeTwoPoint(source1, source2, target1, target2, List.of());
    }

    public static Result computeTwoPoint(ControlPoint source1,
                                         ControlPoint source2,
                                         ControlPoint target1,
                                         ControlPoint target2,
                                         List<ControlPointPair> checkPoints) {
        validateControlPoint(source1, "Punto CAD 1");
        validateControlPoint(source2, "Punto CAD 2");
        validateControlPoint(target1, "Punto destino 1");
        validateControlPoint(target2, "Punto destino 2");

        double ux = source2.x() - source1.x();
        double uy = source2.y() - source1.y();
        double vx = target2.x() - target1.x();
        double vy = target2.y() - target1.y();

        double sourceLength = Math.hypot(ux, uy);
        double targetLength = Math.hypot(vx, vy);
        if (sourceLength < 1e-9 || targetLength < 1e-9) {
            throw new IllegalArgumentException("Los puntos de control deben estar separados.");
        }

        double scale = targetLength / sourceLength;
        double rotation = Math.atan2(vy, vx) - Math.atan2(uy, ux);
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);

        double m00 = scale * cos;
        double m01 = -scale * sin;
        double m10 = scale * sin;
        double m11 = scale * cos;
        double m02 = target1.x() - (m00 * source1.x() + m01 * source1.y());
        double m12 = target1.y() - (m10 * source1.x() + m11 * source1.y());

        return buildResult(
                true,
                "2POINT_SIMILARITY",
                m00, m01, m02, m10, m11, m12,
                2,
                checkPoints,
                "Transformacion por 2 puntos (rigida con escala/rotacion)"
        );
    }

    public static Result computeThreePoint(ControlPoint source1,
                                           ControlPoint source2,
                                           ControlPoint source3,
                                           ControlPoint target1,
                                           ControlPoint target2,
                                           ControlPoint target3) {
        return computeThreePoint(source1, source2, source3, target1, target2, target3, List.of());
    }

    public static Result computeThreePoint(ControlPoint source1,
                                           ControlPoint source2,
                                           ControlPoint source3,
                                           ControlPoint target1,
                                           ControlPoint target2,
                                           ControlPoint target3,
                                           List<ControlPointPair> checkPoints) {
        validateControlPoint(source1, "Punto CAD 1");
        validateControlPoint(source2, "Punto CAD 2");
        validateControlPoint(source3, "Punto CAD 3");
        validateControlPoint(target1, "Punto destino 1");
        validateControlPoint(target2, "Punto destino 2");
        validateControlPoint(target3, "Punto destino 3");

        double[][] sourceMatrix = {
                {source1.x(), source1.y(), 1d},
                {source2.x(), source2.y(), 1d},
                {source3.x(), source3.y(), 1d}
        };
        double det = determinant3x3(sourceMatrix);
        if (Math.abs(det) < 1e-9) {
            throw new IllegalArgumentException("Los 3 puntos CAD no pueden ser colineales.");
        }
        double[][] inverse = invert3x3(sourceMatrix, det);
        double[] xVector = {target1.x(), target2.x(), target3.x()};
        double[] yVector = {target1.y(), target2.y(), target3.y()};

        double m00 = dot(inverse[0], xVector);
        double m01 = dot(inverse[1], xVector);
        double m02 = dot(inverse[2], xVector);
        double m10 = dot(inverse[0], yVector);
        double m11 = dot(inverse[1], yVector);
        double m12 = dot(inverse[2], yVector);

        return buildResult(
                true,
                "3POINT_AFFINE",
                m00, m01, m02, m10, m11, m12,
                3,
                checkPoints,
                "Transformacion afin por 3 puntos"
        );
    }

    public static void applyResultToLayer(Layer layer, Result result) {
        if (layer == null || result == null || !result.approved()) {
            return;
        }
        if (result.method() == null || result.method().isBlank()) {
            layer.clearCadGeoreference();
            return;
        }
        layer.setCadGeoreferenceTransform(
                result.method(),
                result.m00(),
                result.m01(),
                result.m02(),
                result.m10(),
                result.m11(),
                result.m12()
        );
        layer.setCadGeoreferenceDiagnostics(
                result.residualMean(),
                result.residualMax(),
                result.referencePointCount(),
                result.checkPointCount()
        );
    }

    public static Geometry applyGeoreference(Layer layer, Geometry geometry) {
        if (geometry == null || geometry.isEmpty() || layer == null || !CadLayerSupport.isCadLayer(layer) || !layer.hasCadGeoreference()) {
            return geometry;
        }
        AffineTransformation transformation = new AffineTransformation(
                layer.getCadGeorefM00(),
                layer.getCadGeorefM01(),
                layer.getCadGeorefM02(),
                layer.getCadGeorefM10(),
                layer.getCadGeorefM11(),
                layer.getCadGeorefM12()
        );
        return transformation.transform(geometry);
    }

    public static Envelope applyGeoreference(Layer layer, Envelope envelope) {
        if (envelope == null || envelope.isNull() || layer == null || !CadLayerSupport.isCadLayer(layer) || !layer.hasCadGeoreference()) {
            return envelope;
        }
        Geometry transformed = applyGeoreference(layer, GEOMETRY_FACTORY.toGeometry(envelope));
        return transformed != null ? transformed.getEnvelopeInternal() : envelope;
    }

    public static String buildSummary(Layer layer) {
        if (layer == null || !CadLayerSupport.isCadLayer(layer)) {
            return "No aplica";
        }
        if (!layer.hasCadGeoreference()) {
            return "Sin georreferenciacion por puntos";
        }
        String method = layer.getCadGeoreferenceMethod();
        if ("3POINT_AFFINE".equalsIgnoreCase(method)) {
            return "3 puntos | afin";
        }
        if ("2POINT_SIMILARITY".equalsIgnoreCase(method)) {
            return "2 puntos | rigida con escala/rotacion";
        }
        return method != null && !method.isBlank() ? method : "Georreferenciado";
    }

    public static String buildDetailedSummary(Layer layer) {
        String base = buildSummary(layer);
        if (layer == null || !CadLayerSupport.isCadLayer(layer) || !layer.hasCadGeoreference()) {
            return base;
        }
        String residual = buildResidualSummary(layer);
        return residual.isBlank() ? base : base + " | " + residual;
    }

    public static String buildResidualSummary(Layer layer) {
        if (layer == null || !CadLayerSupport.isCadLayer(layer) || !layer.hasCadGeoreference()) {
            return "";
        }
        if (!layer.hasCadGeoreferenceResidualCheck()) {
            int referenceCount = layer.getCadGeorefReferenceCount();
            return referenceCount > 0
                    ? "ajuste exacto con " + referenceCount + " puntos"
                    : "sin punto de control independiente";
        }
        return String.format(
                Locale.US,
                "control %d | media %.3f | max %.3f",
                layer.getCadGeorefCheckCount(),
                layer.getCadGeorefResidualMean(),
                layer.getCadGeorefResidualMax()
        );
    }

    public static ControlPoint applyTransform(Result result, ControlPoint sourcePoint) {
        validateControlPoint(sourcePoint, "Punto CAD");
        if (result == null || !result.approved()) {
            throw new IllegalArgumentException("Transformacion CAD invalida.");
        }
        double x = result.m00() * sourcePoint.x() + result.m01() * sourcePoint.y() + result.m02();
        double y = result.m10() * sourcePoint.x() + result.m11() * sourcePoint.y() + result.m12();
        return new ControlPoint(x, y);
    }

    private static Result buildResult(boolean approved,
                                      String method,
                                      double m00,
                                      double m01,
                                      double m02,
                                      double m10,
                                      double m11,
                                      double m12,
                                      int referencePointCount,
                                      List<ControlPointPair> checkPoints,
                                      String baseSummary) {
        ResidualStats residualStats = computeResidualStats(m00, m01, m02, m10, m11, m12, checkPoints);
        String summary = baseSummary;
        if (residualStats.checkPointCount() > 0) {
            summary += String.format(
                    Locale.US,
                    " | control %d | max %.3f",
                    residualStats.checkPointCount(),
                    residualStats.maxResidual()
            );
        }
        return new Result(
                approved,
                method,
                m00,
                m01,
                m02,
                m10,
                m11,
                m12,
                residualStats.meanResidual(),
                residualStats.maxResidual(),
                referencePointCount,
                residualStats.checkPointCount(),
                summary
        );
    }

    private static ResidualStats computeResidualStats(double m00,
                                                      double m01,
                                                      double m02,
                                                      double m10,
                                                      double m11,
                                                      double m12,
                                                      List<ControlPointPair> checkPoints) {
        if (checkPoints == null || checkPoints.isEmpty()) {
            return new ResidualStats(0d, 0d, 0);
        }
        double total = 0d;
        double max = 0d;
        int count = 0;
        for (ControlPointPair pair : checkPoints) {
            if (pair == null) {
                continue;
            }
            validateControlPoint(pair.source(), "Punto CAD de control");
            validateControlPoint(pair.target(), "Punto destino de control");
            double tx = m00 * pair.source().x() + m01 * pair.source().y() + m02;
            double ty = m10 * pair.source().x() + m11 * pair.source().y() + m12;
            double residual = Math.hypot(pair.target().x() - tx, pair.target().y() - ty);
            total += residual;
            max = Math.max(max, residual);
            count++;
        }
        if (count == 0) {
            return new ResidualStats(0d, 0d, 0);
        }
        return new ResidualStats(total / count, max, count);
    }

    private static void validateControlPoint(ControlPoint point, String label) {
        if (point == null || !Double.isFinite(point.x()) || !Double.isFinite(point.y())) {
            throw new IllegalArgumentException(label + " incompleto.");
        }
    }

    private static double determinant3x3(double[][] matrix) {
        return matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1])
                - matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0])
                + matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]);
    }

    private static double[][] invert3x3(double[][] m, double det) {
        double[][] inverse = new double[3][3];
        inverse[0][0] = (m[1][1] * m[2][2] - m[1][2] * m[2][1]) / det;
        inverse[0][1] = (m[0][2] * m[2][1] - m[0][1] * m[2][2]) / det;
        inverse[0][2] = (m[0][1] * m[1][2] - m[0][2] * m[1][1]) / det;
        inverse[1][0] = (m[1][2] * m[2][0] - m[1][0] * m[2][2]) / det;
        inverse[1][1] = (m[0][0] * m[2][2] - m[0][2] * m[2][0]) / det;
        inverse[1][2] = (m[0][2] * m[1][0] - m[0][0] * m[1][2]) / det;
        inverse[2][0] = (m[1][0] * m[2][1] - m[1][1] * m[2][0]) / det;
        inverse[2][1] = (m[0][1] * m[2][0] - m[0][0] * m[2][1]) / det;
        inverse[2][2] = (m[0][0] * m[1][1] - m[0][1] * m[1][0]) / det;
        return inverse;
    }

    private static double dot(double[] row, double[] vector) {
        return row[0] * vector[0] + row[1] * vector[1] + row[2] * vector[2];
    }

    public record ControlPoint(double x, double y) {
    }

    public record ControlPointPair(ControlPoint source, ControlPoint target) {
    }

    public record Result(boolean approved,
                         String method,
                         double m00,
                         double m01,
                         double m02,
                         double m10,
                         double m11,
                         double m12,
                         double residualMean,
                         double residualMax,
                         int referencePointCount,
                         int checkPointCount,
                         String summary) {
    }

    private record ResidualStats(double meanResidual, double maxResidual, int checkPointCount) {
    }
}
