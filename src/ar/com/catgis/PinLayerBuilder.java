package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PinLayerBuilder {

    public static ShapefileData buildFromPins(List<PinMarker> pins, String projectCRS) throws Exception {
        if (pins == null || pins.isEmpty()) {
            throw new RuntimeException("No hay pines para convertir.");
        }

        if (projectCRS == null || projectCRS.isBlank()) {
            projectCRS = "EPSG:4326";
        }

        GeometryFactory geometryFactory = new GeometryFactory();

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("pins_layer");
        typeBuilder.add("the_geom", Point.class);
        typeBuilder.add("id", String.class);
        typeBuilder.add("x", String.class);
        typeBuilder.add("y", String.class);
        typeBuilder.add("lon", String.class);
        typeBuilder.add("lat", String.class);
        typeBuilder.add("lon_dms", String.class);
        typeBuilder.add("lat_dms", String.class);

        var featureType = typeBuilder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

        List<SimpleFeature> features = new ArrayList<>();
        org.locationtech.jts.geom.Envelope envelope = null;

        int fid = 1;

        for (PinMarker pin : pins) {
            Point point = geometryFactory.createPoint(new Coordinate(pin.getX(), pin.getY()));

            double[] geographic = transformPoint(pin.getX(), pin.getY(), projectCRS, "EPSG:4326");

            String lon = "";
            String lat = "";
            String lonDms = "";
            String latDms = "";

            if (geographic != null) {
                lon = formatNumber(geographic[0]);
                lat = formatNumber(geographic[1]);
                lonDms = toDms(geographic[0], false);
                latDms = toDms(geographic[1], true);
            }

            featureBuilder.add(point);
            featureBuilder.add("P" + pin.getId());
            featureBuilder.add(formatNumber(pin.getX()));
            featureBuilder.add(formatNumber(pin.getY()));
            featureBuilder.add(lon);
            featureBuilder.add(lat);
            featureBuilder.add(lonDms);
            featureBuilder.add(latDms);

            SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(fid++));
            features.add(feature);
            featureBuilder.reset();

            if (envelope == null) {
                envelope = new org.locationtech.jts.geom.Envelope(point.getEnvelopeInternal());
            } else {
                envelope.expandToInclude(point.getEnvelopeInternal());
            }
        }

        return new ShapefileData(
                features,
                envelope,
                "Pines temporales",
                features.size(),
                "Pines convertidos a capa | entidades: " + features.size()
        );
    }

    private static double[] transformPoint(double x, double y, String sourceCode, String targetCode) {
        try {
            if (sourceCode == null || sourceCode.isBlank()) {
                return null;
            }
            if (targetCode == null || targetCode.isBlank()) {
                return null;
            }
            if (sourceCode.equalsIgnoreCase(targetCode)) {
                return new double[]{x, y};
            }

            org.geotools.api.referencing.crs.CoordinateReferenceSystem sourceCRS =
                    org.geotools.referencing.CRS.decode(sourceCode, true);
            org.geotools.api.referencing.crs.CoordinateReferenceSystem targetCRS =
                    org.geotools.referencing.CRS.decode(targetCode, true);
            org.geotools.api.referencing.operation.MathTransform transform =
                    org.geotools.referencing.CRS.findMathTransform(sourceCRS, targetCRS, true);

            double[] src = new double[]{x, y};
            double[] dst = new double[2];
            transform.transform(src, 0, dst, 0, 1);
            return dst;
        } catch (Exception ex) {
            return null;
        }
    }

    private static String formatNumber(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private static String toDms(double value, boolean latitude) {
        String hemi;
        if (latitude) {
            hemi = value >= 0 ? "N" : "S";
        } else {
            hemi = value >= 0 ? "E" : "O";
        }

        double abs = Math.abs(value);
        int degrees = (int) abs;
        double minFloat = (abs - degrees) * 60.0;
        int minutes = (int) minFloat;
        double secFloat = (minFloat - minutes) * 60.0;

        return String.format(Locale.US, "%d\u00B0 %d' %.2f\" %s", degrees, minutes, secFloat, hemi);
    }
}
