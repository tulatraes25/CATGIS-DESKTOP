package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T5/#113: coverage against an explicit study area (AOI), with numeric oracles. */
class TopologyCoverageAoiContractTest {

    private static final String SQUARE = "POLYGON((0 0,2 0,2 2,0 2,0 0))";
    private static final String L_BOTTOM = "POLYGON((0 0,2 0,2 1,0 1,0 0))";
    private static final String L_TOP = "POLYGON((0 1,1 1,1 2,0 2,0 1))";

    @Test
    void incompleteCoverageReportsTheExactMissingAreaInsideTheAoi() throws Exception {
        ShapefileData coverage = data(List.of(wkt(L_BOTTOM), wkt(L_TOP)), "EPSG:3857");
        ShapefileData aoi = data(List.of(wkt(SQUARE)), "EPSG:3857");
        Layer layer = new VectorLayer("coverage", "/tmp/coverage.shp");
        layer.setSourceCRS("EPSG:3857");

        TopologyValidationOptions options = new TopologyValidationOptions();
        options.setCoverageWithinStudyArea(true);
        options.setStudyArea(aoi);
        options.setStudyAreaCrs("EPSG:3857");
        options.setCoverageAreaThreshold(0.01);

        List<TopologyCheckResult> results =
                TopologyValidationService.validateLayer(layer, coverage, options);
        List<TopologyCheckResult> coverageIssues = results.stream()
                .filter(r -> "Cobertura AOI".equals(r.getCheckType()))
                .toList();
        assertEquals(1, coverageIssues.size(), results.toString());
        TopologyCheckResult issue = coverageIssues.get(0);
        assertTrue(issue.hasFocusGeometry());
        assertEquals(1.0, issue.getFocusGeometry().getArea(), 1e-9);
        assertTrue(issue.getDetail().contains("1.0"), issue.getDetail());
    }

    @Test
    void completeCoverageInsideAoiProducesNoCoverageIssue() throws Exception {
        ShapefileData coverage = data(List.of(wkt(SQUARE)), "EPSG:3857");
        ShapefileData aoi = data(List.of(wkt(SQUARE)), "EPSG:3857");
        Layer layer = new VectorLayer("coverage", "/tmp/coverage.shp");
        layer.setSourceCRS("EPSG:3857");

        TopologyValidationOptions options = new TopologyValidationOptions();
        options.setCoverageWithinStudyArea(true);
        options.setStudyArea(aoi);
        options.setStudyAreaCrs("EPSG:3857");
        options.setCoverageAreaThreshold(0.01);

        List<TopologyCheckResult> results =
                TopologyValidationService.validateLayer(layer, coverage, options);
        assertTrue(results.stream().noneMatch(r -> "Cobertura AOI".equals(r.getCheckType())),
                results.toString());
    }

    @Test
    void mismatchedAoiCrsFailsClosedInsteadOfComparingWrongUnits() throws Exception {
        ShapefileData coverage = data(List.of(wkt(SQUARE)), "EPSG:3857");
        ShapefileData aoi = data(List.of(wkt(SQUARE)), "EPSG:4326");
        Layer layer = new VectorLayer("coverage", "/tmp/coverage.shp");
        layer.setSourceCRS("EPSG:3857");

        TopologyValidationOptions options = new TopologyValidationOptions();
        options.setCoverageWithinStudyArea(true);
        options.setStudyArea(aoi);
        options.setStudyAreaCrs("EPSG:4326");

        List<TopologyCheckResult> results =
                TopologyValidationService.validateLayer(layer, coverage, options);
        TopologyCheckResult issue = results.stream()
                .filter(r -> "Cobertura AOI".equals(r.getCheckType()))
                .findFirst().orElseThrow();
        assertEquals("Error", issue.getSeverity());
        assertTrue(issue.getDetail().contains("reproyecte"), issue.getDetail());
        assertFalse(issue.hasFocusGeometry());
    }

    @Test
    void missingAoiIsReportedAsNotEvaluated() throws Exception {
        ShapefileData coverage = data(List.of(wkt(SQUARE)), "EPSG:3857");
        Layer layer = new VectorLayer("coverage", "/tmp/coverage.shp");
        layer.setSourceCRS("EPSG:3857");
        TopologyValidationOptions options = new TopologyValidationOptions();
        options.setCoverageWithinStudyArea(true);

        List<TopologyCheckResult> results =
                TopologyValidationService.validateLayer(layer, coverage, options);
        assertTrue(results.stream().anyMatch(r -> "Cobertura AOI".equals(r.getCheckType())
                        && r.getDetail().contains("area de estudio")),
                results.toString());
    }

    private static Geometry wkt(String text) throws Exception {
        return new WKTReader().read(text);
    }

    private static ShapefileData data(List<Geometry> geometries, String crsCode) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("aoi_layer");
        builder.setCRS(CRS.decode(crsCode, true));
        builder.add("the_geom", Polygon.class);
        SimpleFeatureType schema = builder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(schema);
        List<SimpleFeature> features = new ArrayList<>();
        Envelope envelope = new Envelope();
        for (int i = 0; i < geometries.size(); i++) {
            featureBuilder.reset();
            featureBuilder.set("the_geom", geometries.get(i));
            features.add(featureBuilder.buildFeature("aoi." + i));
            envelope.expandToInclude(geometries.get(i).getEnvelopeInternal());
        }
        return new ShapefileData(features, envelope, "aoi", features.size(), "ok", schema);
    }
}
