package ar.com.catgis;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.crs.ProjectedCRS;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.Position2D;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.operation.linemerge.LineMerger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.awt.geom.Point2D;

public final class TopographicProfileService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private TopographicProfileService() {
    }

    public static ProfileResult generateProfile(Layer rasterLayer,
                                                Geometry sourceLineGeometry,
                                                String sourceLineCrs,
                                                int requestedSampleCount) throws Exception {
        if (!(rasterLayer instanceof RasterLayer)) {
            throw new IllegalArgumentException("La capa DEM seleccionada no es raster.");
        }
        LineString sourceLine = normalizeLine(sourceLineGeometry);
        if (sourceLine == null || sourceLine.isEmpty() || sourceLine.getNumPoints() < 2) {
            throw new IllegalArgumentException("Debes indicar una linea valida para el perfil topografico.");
        }

        String normalizedSourceCrs = normalizeCode(
                sourceLineCrs,
                CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "EPSG:4326"
        );

        GridCoverage2D coverage = RasterCoverageSupport.readCoverage(rasterLayer);
        String rasterCrsCode = RasterCoverageSupport.resolveCoverageCrsCode(coverage, rasterLayer);
        if (rasterCrsCode == null || rasterCrsCode.isBlank()) {
            throw new IllegalStateException("No se pudo determinar el CRS del raster DEM.");
        }

        CoordinateReferenceSystem rasterCrs = CRS.decode(normalizeCode(rasterCrsCode, "EPSG:4326"), true);
        MathTransform sourceToRaster = buildTransform(normalizedSourceCrs, rasterCrsCode);
        MathTransform sourceToMetric = buildTransform(normalizedSourceCrs, resolveMetricCrs(normalizedSourceCrs));
        Geometry rasterLine = org.geotools.geometry.jts.JTS.transform(sourceLine, sourceToRaster);
        LineString clippedRasterLine = clipLineToCoverage(rasterLine, coverage);
        if (clippedRasterLine == null || clippedRasterLine.isEmpty() || clippedRasterLine.getNumPoints() < 2) {
            throw new IllegalStateException("La linea seleccionada no intersecta el DEM activo. Usa otra linea o descarga/carga un DEM que cubra ese sector.");
        }
        MathTransform rasterToSource = sourceToRaster.inverse();
        LineString sourceLineForProfile = (LineString) org.geotools.geometry.jts.JTS.transform(clippedRasterLine, rasterToSource);

        LengthIndexedLine sourceIndex = new LengthIndexedLine(sourceLineForProfile);
        int sampleCount = Math.max(32, Math.min(1200, requestedSampleCount));

        List<ProfileSample> samples = new ArrayList<>();
        Coordinate previousMetric = null;
        double cumulativeDistance = 0d;
        double minElevation = Double.POSITIVE_INFINITY;
        double maxElevation = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < sampleCount; i++) {
            double fraction = sampleCount == 1 ? 0d : (double) i / (double) (sampleCount - 1);
            double lineIndex = sourceLineForProfile.getLength() * fraction;
            Coordinate sourceCoordinate = sourceIndex.extractPoint(lineIndex);
            Coordinate rasterCoordinate = transformCoordinate(sourceCoordinate, sourceToRaster);
            Coordinate metricCoordinate = transformCoordinate(sourceCoordinate, sourceToMetric);

            if (previousMetric != null && metricCoordinate != null) {
                cumulativeDistance += metricCoordinate.distance(previousMetric);
            }
            previousMetric = metricCoordinate != null ? metricCoordinate : previousMetric;

            Double elevation = evaluateElevation(coverage, rasterCoordinate);
            boolean valid = elevation != null && Double.isFinite(elevation);
            if (valid) {
                minElevation = Math.min(minElevation, elevation);
                maxElevation = Math.max(maxElevation, elevation);
            }
            samples.add(new ProfileSample(cumulativeDistance, valid ? elevation : Double.NaN, sourceCoordinate, valid));
        }

        long validCount = samples.stream().filter(ProfileSample::valid).count();
        if (validCount < 2) {
            throw new IllegalStateException("El DEM no devolvio suficientes cotas validas sobre la linea elegida. Verifica que la linea cruce el raster y que ambos tengan el CRS correcto.");
        }

        return new ProfileResult(
                rasterLayer,
                sourceLineForProfile,
                normalizedSourceCrs,
                samples,
                minElevation,
                maxElevation,
                cumulativeDistance,
                (int) validCount
        );
    }

    private static String resolveMetricCrs(String sourceCode) {
        try {
            CoordinateReferenceSystem sourceCrs = CRS.decode(normalizeCode(sourceCode, "EPSG:4326"), true);
            if (sourceCrs instanceof ProjectedCRS) {
                return normalizeCode(CRS.toSRS(sourceCrs, true), "EPSG:3857");
            }
        } catch (Exception ignored) {
        }
        return "EPSG:3857";
    }

    private static MathTransform buildTransform(String sourceCode, String targetCode) throws Exception {
        CoordinateReferenceSystem source = CRS.decode(normalizeCode(sourceCode, "EPSG:4326"), true);
        CoordinateReferenceSystem target = CRS.decode(normalizeCode(targetCode, "EPSG:4326"), true);
        return CRS.findMathTransform(source, target, true);
    }

    private static Coordinate transformCoordinate(Coordinate source, MathTransform transform) throws Exception {
        if (source == null) {
            return null;
        }
        Position2D src = new Position2D(source.x, source.y);
        Position2D dst = new Position2D();
        transform.transform(src, dst);
        return new Coordinate(dst.x, dst.y);
    }

    private static Double evaluateElevation(GridCoverage2D coverage, Coordinate rasterCoordinate) {
        if (coverage == null || rasterCoordinate == null) {
            return null;
        }
        try {
            double[] values = coverage.evaluate(
                    new Point2D.Double(rasterCoordinate.x, rasterCoordinate.y),
                    new double[Math.max(1, coverage.getNumSampleDimensions())]
            );
            if (values == null || values.length == 0) {
                return null;
            }
            double value = values[0];
            return Double.isFinite(value) ? value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static LineString normalizeLine(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        if (geometry instanceof LineString lineString) {
            return (LineString) lineString.copy();
        }
        if (geometry instanceof MultiLineString multiLineString) {
            LineMerger merger = new LineMerger();
            merger.add(multiLineString);
            return chooseLongestLine(merger.getMergedLineStrings());
        }
        if (geometry instanceof GeometryCollection collection) {
            LineMerger merger = new LineMerger();
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                Geometry child = collection.getGeometryN(i);
                if (child instanceof LineString || child instanceof MultiLineString) {
                    merger.add(child);
                }
            }
            return chooseLongestLine(merger.getMergedLineStrings());
        }
        return null;
    }

    private static LineString clipLineToCoverage(Geometry rasterGeometry, GridCoverage2D coverage) {
        if (rasterGeometry == null || rasterGeometry.isEmpty() || coverage == null || coverage.getEnvelope2D() == null) {
            return null;
        }
        Envelope envelope = new Envelope(
                coverage.getEnvelope2D().getMinX(),
                coverage.getEnvelope2D().getMaxX(),
                coverage.getEnvelope2D().getMinY(),
                coverage.getEnvelope2D().getMaxY()
        );
        Geometry coverageGeometry = GEOMETRY_FACTORY.toGeometry(envelope);
        Geometry clipped = rasterGeometry.intersection(coverageGeometry);
        return normalizeLine(clipped);
    }

    private static LineString chooseLongestLine(Collection<?> mergedLines) {
        LineString longest = null;
        double bestLength = -1d;
        for (Object candidate : mergedLines) {
            if (!(candidate instanceof LineString lineString) || lineString.isEmpty()) {
                continue;
            }
            if (lineString.getLength() > bestLength) {
                bestLength = lineString.getLength();
                longest = lineString;
            }
        }
        return longest != null ? (LineString) longest.copy() : null;
    }

    public static LineString buildLineFromProjectCoordinates(Coordinate start, Coordinate end) {
        if (start == null || end == null) {
            return null;
        }
        return GEOMETRY_FACTORY.createLineString(new Coordinate[]{new Coordinate(start), new Coordinate(end)});
    }

    public static LineString buildLineFromProjectCoordinates(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            return null;
        }
        List<Coordinate> valid = new ArrayList<>();
        Coordinate previous = null;
        for (Coordinate coordinate : coordinates) {
            if (coordinate == null) {
                continue;
            }
            Coordinate copy = new Coordinate(coordinate);
            if (previous == null || previous.distance(copy) > 0d) {
                valid.add(copy);
                previous = copy;
            }
        }
        if (valid.size() < 2) {
            return null;
        }
        return GEOMETRY_FACTORY.createLineString(valid.toArray(new Coordinate[0]));
    }

    private static String normalizeCode(String code, String fallback) {
        String normalized = CRSDefinitions.normalizeCode(code);
        if (normalized == null || normalized.isBlank()) {
            normalized = fallback;
        }
        return normalized;
    }

    public record ProfileSample(double distanceMeters, double elevation, Coordinate sourceCoordinate, boolean valid) {
    }

    public record ProfileResult(Layer rasterLayer,
                                LineString sourceLine,
                                String sourceLineCrs,
                                List<ProfileSample> samples,
                                double minElevation,
                                double maxElevation,
                                double totalDistanceMeters,
                                int validSampleCount) {
    }
}
