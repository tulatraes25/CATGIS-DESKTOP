package ar.com.catgis;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.AffineTransformation;

public final class CadPlacementSupport {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private CadPlacementSupport() {
    }

    public static Geometry applyPlacement(Layer layer, Geometry geometry) {
        if (geometry == null || geometry.isEmpty() || layer == null || !CadLayerSupport.isCadLayer(layer)) {
            return geometry;
        }

        Geometry resolvedGeometry = CadGeoreferenceSupport.applyGeoreference(layer, geometry);
        if (!layer.hasCadPlacementAdjustment()) {
            return resolvedGeometry;
        }

        Envelope envelope = resolvedGeometry.getEnvelopeInternal();
        double centerX = envelope.getMinX() + (envelope.getWidth() / 2d);
        double centerY = envelope.getMinY() + (envelope.getHeight() / 2d);

        AffineTransformation transformation = new AffineTransformation();
        double scale = layer.getCadScale();
        if (Math.abs(scale - 1d) > 1e-9) {
            transformation.translate(-centerX, -centerY);
            transformation.scale(scale, scale);
            transformation.translate(centerX, centerY);
        }

        double radians = Math.toRadians(layer.getCadRotationDegrees());
        if (Math.abs(radians) > 1e-9) {
            transformation.compose(AffineTransformation.rotationInstance(radians, centerX, centerY));
        }

        if (Math.abs(layer.getCadOffsetX()) > 1e-9 || Math.abs(layer.getCadOffsetY()) > 1e-9) {
            transformation.compose(AffineTransformation.translationInstance(layer.getCadOffsetX(), layer.getCadOffsetY()));
        }

        return transformation.transform(resolvedGeometry);
    }

    public static Envelope applyPlacement(Layer layer, Envelope envelope) {
        if (envelope == null || envelope.isNull() || layer == null || !CadLayerSupport.isCadLayer(layer)) {
            return envelope;
        }
        Geometry transformed = applyPlacement(layer, GEOMETRY_FACTORY.toGeometry(envelope));
        return transformed != null ? transformed.getEnvelopeInternal() : envelope;
    }

    public static String buildPlacementSummary(Layer layer) {
        if (layer == null || !CadLayerSupport.isCadLayer(layer)) {
            return "No aplica";
        }
        if (!layer.hasCadPlacementAdjustment()) {
            return "Sin ajuste fino";
        }
        return String.format(
                java.util.Locale.US,
                "Dx %.3f | Dy %.3f | Esc %.6f | Rot %.3f°",
                layer.getCadOffsetX(),
                layer.getCadOffsetY(),
                layer.getCadScale(),
                layer.getCadRotationDegrees()
        );
    }

    public record Result(boolean approved, double offsetX, double offsetY, double scale, double rotationDegrees) {
    }
}
