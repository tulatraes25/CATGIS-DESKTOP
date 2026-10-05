package ar.com.catgis;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.core.model.Layer;

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
import java.util.Locale;

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
                AppContext.project() != null ? AppContext.project().getProjectCRS() : "EPSG:4326"
        );

        GridCoverage2D coverage = RasterCoverageSupport.readCoverage(rasterLayer);
        String rasterCrsCode = RasterCoverageSupport.resolveOperationalAnalysisCrsCode(coverage, rasterLayer);
        if (rasterCrsCode == null || rasterCrsCode.isBlank()) {
            throw new IllegalStateException("No se pudo determinar el CRS del raster DEM.");
        }

        CoordinateReferenceSystem rasterCrs = CRSDefinitions.decode(normalizeCode(rasterCrsCode, "EPSG:4326"), true);
        MathTransform sourceToRaster = buildTransform(normalizedSourceCrs, rasterCrsCode);
        MathTransform sourceToMetric = buildTransform(normalizedSourceCrs, resolveMetricCrs(normalizedSourceCrs, sourceLine));
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

    public static double estimateLineDistanceMeters(Geometry sourceLineGeometry, String sourceLineCrs) {
        try {
            LineString line = normalizeLine(sourceLineGeometry);
            if (line == null || line.isEmpty() || line.getNumPoints() < 2) {
                return 0d;
            }
            String normalizedSourceCrs = normalizeCode(
                    sourceLineCrs,
                    AppContext.project() != null ? AppContext.project().getProjectCRS() : "EPSG:4326"
            );
            String metricCode = resolveMetricCrs(normalizedSourceCrs, line);
            MathTransform toMetric = buildTransform(normalizedSourceCrs, metricCode);
            Geometry metricGeometry = org.geotools.geometry.jts.JTS.transform(line, toMetric);
            return metricGeometry != null ? Math.abs(metricGeometry.getLength()) : Math.abs(line.getLength());
        } catch (Exception ignored) {
            return sourceLineGeometry != null ? Math.abs(sourceLineGeometry.getLength()) : 0d;
        }
    }

    private static String resolveMetricCrs(String sourceCode, Geometry geometry) {
        try {
            CoordinateReferenceSystem sourceCrs = CRSDefinitions.decode(normalizeCode(sourceCode, "EPSG:4326"), true);
            String projectCode = AppContext.project() != null
                    ? CRSDefinitions.normalizeCode(AppContext.project().getProjectCRS())
                    : "";
            if (projectCode != null && !projectCode.isBlank()) {
                CoordinateReferenceSystem projectCrs = CRSDefinitions.decode(projectCode, true);
                if (isProjectedMetric(projectCrs)) {
                    return projectCode;
                }
            }
            if (isProjectedMetric(sourceCrs)) {
                return normalizeCode(sourceCode, "EPSG:3857");
            }
        } catch (Exception ignored) { CatgisLogger.warn("TopographicProfileService: operation failed", ignored); }
        return "EPSG:3857";
    }

    private static MathTransform buildTransform(String sourceCode, String targetCode) throws Exception {
        CoordinateReferenceSystem source = CRSDefinitions.decode(normalizeCode(sourceCode, "EPSG:4326"), true);
        CoordinateReferenceSystem target = CRSDefinitions.decode(normalizeCode(targetCode, "EPSG:4326"), true);
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

    private static boolean isProjectedMetric(CoordinateReferenceSystem crs) {
        if (!(crs instanceof ProjectedCRS)) {
            return false;
        }
        try {
            String unit0 = String.valueOf(crs.getCoordinateSystem().getAxis(0).getUnit()).toLowerCase(Locale.ROOT);
            String unit1 = String.valueOf(crs.getCoordinateSystem().getAxis(1).getUnit()).toLowerCase(Locale.ROOT);
            return looksMetric(unit0) && looksMetric(unit1);
        } catch (Exception ex) {
            return true;
        }
    }

    private static boolean looksMetric(String unitText) {
        if (unitText == null) {
            return false;
        }
        String normalized = unitText.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("metre")
                || normalized.contains("meter")
                || normalized.equals("m")
                || normalized.contains("9001");
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

    /**
     * Generate a 3D profile with elevation data.
     * Returns a list of 3D coordinates (x=distance, y=elevation, z=0).
     */
    public static List<Coordinate> generateProfile3D(ProfileResult result) {
        if (result == null || result.samples() == null) return new ArrayList<>();
        List<Coordinate> coords = new ArrayList<>();
        for (ProfileSample sample : result.samples()) {
            if (sample.valid()) {
                coords.add(new Coordinate(sample.distanceMeters(), sample.elevation(), 0));
            }
        }
        return coords;
    }

    /**
     * Compute slope profile from elevation samples.
     * Returns slope in degrees at each sample point.
     */
    public static double[] computeSlopeProfile(ProfileResult result) {
        if (result == null || result.samples() == null || result.samples().size() < 2) return new double[0];
        List<ProfileSample> samples = result.samples();
        double[] slopes = new double[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            if (i == 0) {
                slopes[i] = 0;
            } else if (samples.get(i).valid() && samples.get(i - 1).valid()) {
                double dx = samples.get(i).distanceMeters() - samples.get(i - 1).distanceMeters();
                double dy = samples.get(i).elevation() - samples.get(i - 1).elevation();
                slopes[i] = dx > 0 ? Math.toDegrees(Math.atan(Math.abs(dy) / dx)) : 0;
            }
        }
        return slopes;
    }

    /**
     * Compute slope angle at each sample point along the profile.
     * Returns the angle of the profile curve in degrees (0=horizontal, 90=vertical).
     * NOTE: This is NOT geographic aspect (N/E/S/W orientation). It is the angle
     * of the elevation profile curve in the cross-section plane.
     */
    public static double[] computeProfileSlopeAngle(ProfileResult result) {
        if (result == null || result.samples() == null || result.samples().size() < 2) return new double[0];
        List<ProfileSample> samples = result.samples();
        double[] aspects = new double[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            if (i == 0 || !samples.get(i).valid() || !samples.get(i - 1).valid()) {
                aspects[i] = 0;
            } else {
                double dx = samples.get(i).distanceMeters() - samples.get(i - 1).distanceMeters();
                double dy = samples.get(i).elevation() - samples.get(i - 1).elevation();
                aspects[i] = dx > 0 ? Math.toDegrees(Math.atan2(dy, dx)) : 0;
            }
        }
        return aspects;
    }

    // ─── Hypsometric curve ───────────────────────────────────────────

    /**
     * A single point on a hypsometric curve.
     */
    public record HypsometricPoint(double elevation, double areaFraction) {}

    /**
     * Compute hypsometric curve from a DEM raster.
     * Returns elevation-area pairs sorted from highest to lowest elevation.
     *
     * @param elevationData flat array of elevation values (NaN = no data)
     * @param numBins       number of elevation bins (e.g. 50)
     * @return sorted list of (elevation, cumulative area fraction) points
     */
    public static List<HypsometricPoint> computeHypsometricCurve(double[] elevationData, int numBins) {
        if (elevationData == null || elevationData.length == 0) return List.of();

        // Collect valid elevations
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        int validCount = 0;
        for (double v : elevationData) {
            if (Double.isNaN(v) || Double.isInfinite(v)) continue;
            if (v < min) min = v;
            if (v > max) max = v;
            validCount++;
        }
        if (validCount == 0 || min >= max) return List.of();

        // Build histogram
        double binWidth = (max - min) / numBins;
        int[] histogram = new int[numBins];
        for (double v : elevationData) {
            if (Double.isNaN(v) || Double.isInfinite(v)) continue;
            int bin = (int) ((v - min) / binWidth);
            if (bin >= numBins) bin = numBins - 1;
            if (bin < 0) bin = 0;
            histogram[bin]++;
        }

        // Cumulative area from highest to lowest
        List<HypsometricPoint> curve = new ArrayList<>();
        double cumArea = 0;
        for (int i = numBins - 1; i >= 0; i--) {
            cumArea += histogram[i];
            double elevation = min + (i + 0.5) * binWidth;
            curve.add(new HypsometricPoint(elevation, cumArea / validCount));
        }

        return curve;
    }

    /**
     * Compute hypsometric integral (area under the curve, 0..1).
     * Values near 0.5 indicate mature/equilibrium landscape;
     * near 1.0 indicate young/active uplift; near 0.0 indicate old/eroded.
     */
    public static double hypsometricIntegral(List<HypsometricPoint> curve) {
        if (curve == null || curve.size() < 2) return Double.NaN;
        double minElev = curve.get(curve.size() - 1).elevation();
        double maxElev = curve.get(0).elevation();
        double elevRange = maxElev - minElev;
        if (elevRange <= 0) return Double.NaN;

        // Normalize elevations to 0..1 and compute area under curve
        // Curve is sorted highest→lowest, areaFraction starts small and grows to 1.0
        double area = 0;
        for (int i = 1; i < curve.size(); i++) {
            double dx = curve.get(i).areaFraction() - curve.get(i - 1).areaFraction();
            double y1 = (curve.get(i - 1).elevation() - minElev) / elevRange;
            double y2 = (curve.get(i).elevation() - minElev) / elevRange;
            area += dx * (y1 + y2) / 2.0;
        }
        return area;
    }
}
