package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.crs.ProjectedCRS;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.Position2D;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.awt.Color;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public final class DrainageExtractionService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final int[] ROW_OFFSETS = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] COL_OFFSETS = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final double[] STEP_DISTANCES = {
            Math.sqrt(2d), 1d, Math.sqrt(2d),
            1d, 1d,
            Math.sqrt(2d), 1d, Math.sqrt(2d)
    };
    private static final double FLOW_EPSILON = 1e-9;

    private DrainageExtractionService() {
    }

    public static GeneratedDrainageLayer generateDrainage(Layer rasterLayer,
                                                          int accumulationThreshold,
                                                          String outputLayerName,
                                                          AnalysisDetail detail,
                                                          HydrologicConditioning conditioning,
                                                          double minimumBranchLengthMeters,
                                                          CleanupLevel cleanupLevel) throws Exception {
        if (!(rasterLayer instanceof RasterLayer)) {
            throw new IllegalArgumentException("La capa seleccionada no es un DEM raster valido.");
        }
        if (accumulationThreshold < 2) {
            throw new IllegalArgumentException("El umbral de acumulacion debe ser 2 o mayor.");
        }
        if (minimumBranchLengthMeters < 0d) {
            throw new IllegalArgumentException("La longitud minima del ramal no puede ser negativa.");
        }

        GridCoverage2D coverage = RasterCoverageSupport.readCoverage(rasterLayer);
        SampledDem sampled = sampleDem(coverage, rasterLayer, detail != null ? detail : AnalysisDetail.BALANCED);
        if (sampled.validCellCount < 16) {
            throw new IllegalStateException("El DEM no tiene suficientes celdas validas para calcular drenajes.");
        }

        HydrologicConditioning effectiveConditioning =
                conditioning != null ? conditioning : HydrologicConditioning.ROBUST;
        ConditionedDem conditioned = conditionDem(sampled, effectiveConditioning);

        int[] downstream = computeDownstream(
                conditioned.elevations,
                sampled.validMask,
                conditioned.flatRanks,
                sampled.width,
                sampled.height
        );
        long[] accumulation = computeAccumulation(downstream, sampled.validMask, sampled.width, sampled.height);
        CleanupLevel effectiveCleanup = cleanupLevel != null ? cleanupLevel : CleanupLevel.BALANCED;
        ChannelNetwork network = extractChannelNetwork(
                sampled,
                conditioned.flatRanks,
                downstream,
                accumulation,
                accumulationThreshold,
                minimumBranchLengthMeters,
                effectiveCleanup
        );
        if (network.branches.isEmpty()) {
            throw new IllegalStateException("No se generaron escorrentias con ese umbral. Prueba con un valor menor o usa otro DEM.");
        }

        SimpleFeatureType schema = buildOutputSchema(outputLayerName, sampled.sourceCrsCode);
        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        Envelope outputEnvelope = null;
        int featureIndex = 1;

        for (BranchSegment branch : network.branches) {
            LineString line = branch.line;
            if (line == null || line.isEmpty() || line.getNumPoints() < 2) {
                continue;
            }

            long accCells = branch.maxAccumulationCells;
            double accAreaM2 = sampled.cellAreaSquareMeters > 0d ? accCells * sampled.cellAreaSquareMeters : Double.NaN;
            double lengthM = branch.lengthMeters;
            String channelClass = classifyChannel(accCells, accumulationThreshold);

            builder.set("the_geom", line);
            builder.set("drain_id", featureIndex);
            builder.set("channel_class", channelClass);
            builder.set("acc_cells", accCells);
            builder.set("acc_area_m2", Double.isFinite(accAreaM2) ? accAreaM2 : null);
            builder.set("length_m", Double.isFinite(lengthM) ? lengthM : null);
            builder.set("threshold_cells", accumulationThreshold);
            features.add(builder.buildFeature(safeTypeName(outputLayerName) + "." + featureIndex));
            builder.reset();

            if (outputEnvelope == null) {
                outputEnvelope = new Envelope(line.getEnvelopeInternal());
            } else {
                outputEnvelope.expandToInclude(line.getEnvelopeInternal());
            }
            featureIndex++;
        }

        if (features.isEmpty()) {
            throw new IllegalStateException("La red de drenaje calculada no produjo lineas validas.");
        }

        Layer resultLayer = new VectorLayer(
                outputLayerName != null && !outputLayerName.isBlank() ? outputLayerName.trim() : "Escorrentias",
                ""
        );
        resultLayer.setVisible(true);
        resultLayer.setSourceName("Escorrentias derivadas de " + rasterLayer.getName());
        resultLayer.setFeatureCount(features.size());
        resultLayer.setSourceCRS(sampled.sourceCrsCode);
        resultLayer.setLineColor(new Color(21, 101, 192));
        resultLayer.setLineWidth(1.35f);
        resultLayer.setLineSymbolStyle(Layer.LineSymbolStyle.SOLID);
        VectorLayerUtils.populateFieldConfigs(resultLayer, schema);
        applyDrainageSymbology(resultLayer);

        ShapefileData data = new ShapefileData(
                features,
                outputEnvelope,
                resultLayer.getSourceName(),
                features.size(),
                "Red de drenaje generada desde DEM con umbral " + accumulationThreshold
                        + " celdas, detalle " + sampled.detail.label
                        + " y acondicionamiento " + effectiveConditioning.label + ".",
                schema
        );

        return new GeneratedDrainageLayer(
                resultLayer,
                data,
                accumulationThreshold,
                sampled.width,
                sampled.height,
                sampled.cellAreaSquareMeters,
                minimumBranchLengthMeters,
                effectiveCleanup,
                effectiveConditioning
        );
    }

    public static HydrologyGrid analyzeHydrologyGrid(Layer rasterLayer,
                                                     AnalysisDetail detail,
                                                     HydrologicConditioning conditioning) throws Exception {
        if (!(rasterLayer instanceof RasterLayer)) {
            throw new IllegalArgumentException("La capa seleccionada no es un DEM raster valido.");
        }

        GridCoverage2D coverage = RasterCoverageSupport.readCoverage(rasterLayer);
        SampledDem sampled = sampleDem(coverage, rasterLayer, detail != null ? detail : AnalysisDetail.BALANCED);
        if (sampled.validCellCount < 16) {
            throw new IllegalStateException("El DEM no tiene suficientes celdas validas para analisis topohidrologico.");
        }

        HydrologicConditioning effectiveConditioning =
                conditioning != null ? conditioning : HydrologicConditioning.ROBUST;
        ConditionedDem conditioned = conditionDem(sampled, effectiveConditioning);
        int[] downstream = computeDownstream(
                conditioned.elevations,
                sampled.validMask,
                conditioned.flatRanks,
                sampled.width,
                sampled.height
        );
        long[] accumulation = computeAccumulation(downstream, sampled.validMask, sampled.width, sampled.height);
        double[] metricCellSize = estimateCellSizeMeters(
                sampled.minX,
                sampled.maxX,
                sampled.minY,
                sampled.maxY,
                sampled.width,
                sampled.height,
                sampled.sourceCrsCode
        );

        return new HydrologyGrid(
                copyGrid(sampled.elevations),
                copyGrid(conditioned.elevations),
                copyMask(sampled.validMask),
                copyFlatRanks(conditioned.flatRanks),
                java.util.Arrays.copyOf(downstream, downstream.length),
                java.util.Arrays.copyOf(accumulation, accumulation.length),
                sampled.width,
                sampled.height,
                sampled.minX,
                sampled.maxX,
                sampled.minY,
                sampled.maxY,
                sampled.cellWidthWorld,
                sampled.cellHeightWorld,
                metricCellSize[0],
                metricCellSize[1],
                sampled.cellAreaSquareMeters,
                sampled.sourceCrsCode,
                sampled.detail,
                effectiveConditioning
        );
    }

    private static void applyDrainageSymbology(Layer layer) {
        CategorizedSymbology symbology = layer.getLineCategorizedSymbology();
        symbology.setFieldName("channel_class");
        symbology.setLegendTitle("Red de drenaje");
        symbology.setLegendSubtitle("Escorrentias extraidas del DEM");
        symbology.clearRules();

        CategoryStyleRule local = symbology.getOrCreateRule("Local");
        local.setPrimaryColor(new Color(93, 173, 226));
        local.setSecondaryColor(new Color(93, 173, 226));
        local.setLineStyle(Layer.LineSymbolStyle.SOLID);
        local.setLineWidth(1.0f);

        CategoryStyleRule secondary = symbology.getOrCreateRule("Secundaria");
        secondary.setPrimaryColor(new Color(33, 113, 181));
        secondary.setSecondaryColor(new Color(33, 113, 181));
        secondary.setLineStyle(Layer.LineSymbolStyle.SOLID);
        secondary.setLineWidth(1.65f);

        CategoryStyleRule principal = symbology.getOrCreateRule("Principal");
        principal.setPrimaryColor(new Color(8, 48, 107));
        principal.setSecondaryColor(new Color(8, 48, 107));
        principal.setLineStyle(Layer.LineSymbolStyle.SOLID);
        principal.setLineWidth(2.35f);
    }

    private static SampledDem sampleDem(GridCoverage2D coverage, Layer rasterLayer, AnalysisDetail detail) throws Exception {
        if (coverage == null || coverage.getRenderedImage() == null) {
            throw new IllegalStateException("No se pudo leer la cobertura raster del DEM.");
        }
        if (coverage.getEnvelope2D() == null) {
            throw new IllegalStateException("El DEM no tiene una extension espacial valida.");
        }

        RenderedImage rendered = coverage.getRenderedImage();
        Raster raster = rendered.getData();
        int sourceWidth = raster.getWidth();
        int sourceHeight = raster.getHeight();
        if (sourceWidth < 2 || sourceHeight < 2) {
            throw new IllegalStateException("El DEM no tiene suficiente resolucion para analisis de drenaje.");
        }

        int maxGrid = detail != null ? detail.maxGridSize : AnalysisDetail.BALANCED.maxGridSize;
        double scale = Math.max(
                sourceWidth / (double) maxGrid,
                sourceHeight / (double) maxGrid
        );
        if (scale < 1d) {
            scale = 1d;
        }
        int width = Math.max(2, (int) Math.round(sourceWidth / scale));
        int height = Math.max(2, (int) Math.round(sourceHeight / scale));

        double minX = coverage.getEnvelope2D().getMinX();
        double maxX = coverage.getEnvelope2D().getMaxX();
        double minY = coverage.getEnvelope2D().getMinY();
        double maxY = coverage.getEnvelope2D().getMaxY();
        double cellWidthWorld = (maxX - minX) / width;
        double cellHeightWorld = (maxY - minY) / height;
        if (!Double.isFinite(cellWidthWorld) || !Double.isFinite(cellHeightWorld)
                || cellWidthWorld == 0d || cellHeightWorld == 0d) {
            throw new IllegalStateException("No se pudo calcular el tamano de celda del DEM.");
        }

        double[][] elevations = new double[height][width];
        boolean[][] validMask = new boolean[height][width];
        double[] noDataValues = resolveNoDataValues(coverage);
        int validCellCount = 0;

        int rasterMinX = raster.getMinX();
        int rasterMinY = raster.getMinY();

        for (int row = 0; row < height; row++) {
            int srcY = clamp((int) Math.floor(((row + 0.5d) * sourceHeight) / height), 0, sourceHeight - 1);
            for (int col = 0; col < width; col++) {
                int srcX = clamp((int) Math.floor(((col + 0.5d) * sourceWidth) / width), 0, sourceWidth - 1);
                double value = raster.getSampleDouble(rasterMinX + srcX, rasterMinY + srcY, 0);
                if (isValidDemValue(value, noDataValues)) {
                    elevations[row][col] = value;
                    validMask[row][col] = true;
                    validCellCount++;
                } else {
                    elevations[row][col] = Double.NaN;
                    validMask[row][col] = false;
                }
            }
        }

        String sourceCrsCode = RasterCoverageSupport.resolveOperationalAnalysisCrsCode(coverage, rasterLayer);
        double cellAreaSquareMeters = estimateCellAreaSquareMeters(
                minX,
                maxX,
                minY,
                maxY,
                width,
                height,
                sourceCrsCode
        );

        return new SampledDem(
                elevations,
                validMask,
                width,
                height,
                minX,
                maxX,
                minY,
                maxY,
                cellWidthWorld,
                cellHeightWorld,
                cellAreaSquareMeters,
                sourceCrsCode,
                validCellCount,
                detail
        );
    }

    private static double[] resolveNoDataValues(GridCoverage2D coverage) {
        try {
            Object sampleDimension = coverage.getSampleDimension(0);
            if (sampleDimension == null) {
                return new double[0];
            }
            Object values = sampleDimension.getClass().getMethod("getNoDataValues").invoke(sampleDimension);
            if (values instanceof double[] array) {
                return array;
            }
        } catch (Exception ignored) {
        }
        return new double[0];
    }

    private static boolean isValidDemValue(double value, double[] noDataValues) {
        if (!Double.isFinite(value)) {
            return false;
        }
        if (Math.abs(value) > 1e19) {
            return false;
        }
        if (noDataValues != null) {
            for (double noData : noDataValues) {
                if (Double.isFinite(noData) && Math.abs(noData - value) <= 1e-9) {
                    return false;
                }
            }
        }
        return value > -9999d;
    }

    private static ConditionedDem conditionDem(SampledDem sampled,
                                               HydrologicConditioning conditioning) {
        double[][] conditioned = copyGrid(sampled.elevations);
        int[][] flatRanks = null;

        switch (conditioning) {
            case BASIC -> fillStrictSinks(conditioned, sampled.validMask);
            case ROBUST -> {
                priorityFloodFill(conditioned, sampled.validMask);
                flatRanks = computeFlatDrainRanks(conditioned, sampled.validMask);
            }
            case ADVANCED -> {
                priorityFloodFill(conditioned, sampled.validMask);
                int[][] initialRanks = computeFlatDrainRanks(conditioned, sampled.validMask);
                carveBreachedDepressions(conditioned, sampled.elevations, sampled.validMask, initialRanks);
                flatRanks = computeFlatDrainRanks(conditioned, sampled.validMask);
            }
        }

        return new ConditionedDem(conditioned, flatRanks, conditioning);
    }

    private static void fillStrictSinks(double[][] elevations, boolean[][] validMask) {
        if (elevations == null || validMask == null) {
            return;
        }
        int height = elevations.length;
        int width = height > 0 ? elevations[0].length : 0;

        for (int pass = 0; pass < 8; pass++) {
            boolean changed = false;
            for (int row = 1; row < height - 1; row++) {
                for (int col = 1; col < width - 1; col++) {
                    if (!validMask[row][col]) {
                        continue;
                    }
                    double current = elevations[row][col];
                    double minNeighbor = Double.POSITIVE_INFINITY;
                    boolean hasLower = false;
                    int validNeighbors = 0;
                    for (int i = 0; i < ROW_OFFSETS.length; i++) {
                        int nr = row + ROW_OFFSETS[i];
                        int nc = col + COL_OFFSETS[i];
                        if (!validMask[nr][nc]) {
                            continue;
                        }
                        validNeighbors++;
                        double neighbor = elevations[nr][nc];
                        minNeighbor = Math.min(minNeighbor, neighbor);
                        if (neighbor < current - FLOW_EPSILON) {
                            hasLower = true;
                            break;
                        }
                    }
                    if (!hasLower && validNeighbors >= 3 && current < minNeighbor - FLOW_EPSILON) {
                        elevations[row][col] = minNeighbor;
                        changed = true;
                    }
                }
            }
            if (!changed) {
                return;
            }
        }
    }

    private static void priorityFloodFill(double[][] elevations, boolean[][] validMask) {
        if (elevations == null || validMask == null) {
            return;
        }
        int height = elevations.length;
        int width = height > 0 ? elevations[0].length : 0;
        boolean[][] visited = new boolean[height][width];
        PriorityQueue<CellState> queue = new PriorityQueue<>(
                (left, right) -> {
                    int byElevation = Double.compare(left.elevation, right.elevation);
                    if (byElevation != 0) {
                        return byElevation;
                    }
                    return Integer.compare(left.edgeDistance, right.edgeDistance);
                }
        );

        boolean seeded = false;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (!validMask[row][col] || !isHydrologicOutletSeed(row, col, validMask, width, height)) {
                    continue;
                }
                visited[row][col] = true;
                queue.add(new CellState(row, col, elevations[row][col], edgeDistance(row, col, width, height)));
                seeded = true;
            }
        }

        if (!seeded) {
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (!validMask[row][col]) {
                        continue;
                    }
                    visited[row][col] = true;
                    queue.add(new CellState(row, col, elevations[row][col], edgeDistance(row, col, width, height)));
                }
            }
        }

        while (!queue.isEmpty()) {
            CellState current = queue.remove();
            for (int i = 0; i < ROW_OFFSETS.length; i++) {
                int nr = current.row + ROW_OFFSETS[i];
                int nc = current.col + COL_OFFSETS[i];
                if (!isInside(nr, nc, width, height) || !validMask[nr][nc] || visited[nr][nc]) {
                    continue;
                }
                visited[nr][nc] = true;
                double conditionedElevation = Math.max(elevations[nr][nc], current.elevation);
                elevations[nr][nc] = conditionedElevation;
                queue.add(new CellState(nr, nc, conditionedElevation, edgeDistance(nr, nc, width, height)));
            }
        }
    }

    private static void carveBreachedDepressions(double[][] conditioned,
                                                 double[][] original,
                                                 boolean[][] validMask,
                                                 int[][] flatRanks) {
        if (conditioned == null || original == null || validMask == null || flatRanks == null) {
            return;
        }
        int height = conditioned.length;
        int width = height > 0 ? conditioned[0].length : 0;
        if (width == 0) {
            return;
        }

        double minElevation = Double.POSITIVE_INFINITY;
        double maxElevation = Double.NEGATIVE_INFINITY;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (!validMask[row][col]) {
                    continue;
                }
                double value = original[row][col];
                if (Double.isFinite(value)) {
                    minElevation = Math.min(minElevation, value);
                    maxElevation = Math.max(maxElevation, value);
                }
            }
        }
        if (!Double.isFinite(minElevation) || !Double.isFinite(maxElevation)) {
            return;
        }

        double carveStep = computeBreachCarveStep(maxElevation - minElevation);
        boolean[][] visited = new boolean[height][width];
        ArrayDeque<int[]> queue = new ArrayDeque<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (!validMask[row][col] || visited[row][col] || flatRanks[row][col] < 0) {
                    continue;
                }

                double componentElevation = conditioned[row][col];
                List<int[]> component = new ArrayList<>();
                boolean hasRaisedCell = false;
                int maxRank = 0;

                visited[row][col] = true;
                queue.addLast(new int[]{row, col});

                while (!queue.isEmpty()) {
                    int[] cell = queue.removeFirst();
                    int currentRow = cell[0];
                    int currentCol = cell[1];
                    component.add(cell);
                    hasRaisedCell = hasRaisedCell
                            || conditioned[currentRow][currentCol] > (original[currentRow][currentCol] + FLOW_EPSILON);
                    maxRank = Math.max(maxRank, flatRanks[currentRow][currentCol]);

                    for (int i = 0; i < ROW_OFFSETS.length; i++) {
                        int nr = currentRow + ROW_OFFSETS[i];
                        int nc = currentCol + COL_OFFSETS[i];
                        if (!isInside(nr, nc, width, height) || !validMask[nr][nc] || visited[nr][nc]) {
                            continue;
                        }
                        if (flatRanks[nr][nc] < 0) {
                            continue;
                        }
                        if (Math.abs(conditioned[nr][nc] - componentElevation) > FLOW_EPSILON) {
                            continue;
                        }
                        visited[nr][nc] = true;
                        queue.addLast(new int[]{nr, nc});
                    }
                }

                if (!hasRaisedCell || maxRank <= 0) {
                    continue;
                }

                for (int[] cell : component) {
                    int currentRow = cell[0];
                    int currentCol = cell[1];
                    int rank = Math.max(0, flatRanks[currentRow][currentCol]);
                    double carveDepth = (maxRank - rank) * carveStep;
                    if (!(carveDepth > 0d)) {
                        continue;
                    }
                    conditioned[currentRow][currentCol] -= carveDepth;
                }
            }
        }
    }

    private static double computeBreachCarveStep(double elevationRange) {
        if (!Double.isFinite(elevationRange) || elevationRange <= 0d) {
            return 1e-5d;
        }
        return Math.max(1e-5d, Math.min(1e-3d, elevationRange * 1e-6d));
    }

    private static int[][] computeFlatDrainRanks(double[][] elevations, boolean[][] validMask) {
        int height = elevations.length;
        int width = height > 0 ? elevations[0].length : 0;
        int[][] ranks = new int[height][width];
        ArrayDeque<int[]> queue = new ArrayDeque<>();

        for (int row = 0; row < height; row++) {
            java.util.Arrays.fill(ranks[row], -1);
        }

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (!validMask[row][col]) {
                    continue;
                }
                if (isFlatDrainSeed(row, col, elevations, validMask, width, height)) {
                    ranks[row][col] = 0;
                    queue.add(new int[]{row, col});
                }
            }
        }

        while (!queue.isEmpty()) {
            int[] cell = queue.removeFirst();
            int row = cell[0];
            int col = cell[1];
            double elevation = elevations[row][col];

            for (int i = 0; i < ROW_OFFSETS.length; i++) {
                int nr = row + ROW_OFFSETS[i];
                int nc = col + COL_OFFSETS[i];
                if (!isInside(nr, nc, width, height) || !validMask[nr][nc] || ranks[nr][nc] >= 0) {
                    continue;
                }
                if (Math.abs(elevations[nr][nc] - elevation) <= FLOW_EPSILON) {
                    ranks[nr][nc] = ranks[row][col] + 1;
                    queue.addLast(new int[]{nr, nc});
                }
            }
        }

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (validMask[row][col] && ranks[row][col] < 0) {
                    ranks[row][col] = width + height + edgeDistance(row, col, width, height);
                }
            }
        }
        return ranks;
    }

    private static boolean isHydrologicOutletSeed(int row,
                                                  int col,
                                                  boolean[][] validMask,
                                                  int width,
                                                  int height) {
        if (!isInside(row, col, width, height) || !validMask[row][col]) {
            return false;
        }
        if (row == 0 || col == 0 || row == height - 1 || col == width - 1) {
            return true;
        }
        for (int i = 0; i < ROW_OFFSETS.length; i++) {
            int nr = row + ROW_OFFSETS[i];
            int nc = col + COL_OFFSETS[i];
            if (!isInside(nr, nc, width, height) || !validMask[nr][nc]) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFlatDrainSeed(int row,
                                           int col,
                                           double[][] elevations,
                                           boolean[][] validMask,
                                           int width,
                                           int height) {
        if (isHydrologicOutletSeed(row, col, validMask, width, height)) {
            return true;
        }
        double current = elevations[row][col];
        for (int i = 0; i < ROW_OFFSETS.length; i++) {
            int nr = row + ROW_OFFSETS[i];
            int nc = col + COL_OFFSETS[i];
            if (!isInside(nr, nc, width, height) || !validMask[nr][nc]) {
                continue;
            }
            if (elevations[nr][nc] < current - FLOW_EPSILON) {
                return true;
            }
        }
        return false;
    }

    private static int[] computeDownstream(double[][] elevations,
                                           boolean[][] validMask,
                                           int[][] flatRanks,
                                           int width,
                                           int height) {
        int[] downstream = new int[width * height];
        java.util.Arrays.fill(downstream, -1);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (!validMask[row][col]) {
                    continue;
                }

                double current = elevations[row][col];
                double bestSlope = Double.NEGATIVE_INFINITY;
                int bestIndex = -1;
                int currentEdge = edgeDistance(row, col, width, height);
                int currentFlatRank = flatRanks != null ? flatRanks[row][col] : Integer.MAX_VALUE;

                for (int i = 0; i < ROW_OFFSETS.length; i++) {
                    int nr = row + ROW_OFFSETS[i];
                    int nc = col + COL_OFFSETS[i];
                    if (!isInside(nr, nc, width, height) || !validMask[nr][nc]) {
                        continue;
                    }

                    double neighbor = elevations[nr][nc];
                    double drop = current - neighbor;
                    if (drop > FLOW_EPSILON) {
                        double slope = drop / STEP_DISTANCES[i];
                        if (slope > bestSlope) {
                            bestSlope = slope;
                            bestIndex = linearIndex(nr, nc, width);
                        } else if (Math.abs(slope - bestSlope) <= FLOW_EPSILON && bestIndex >= 0) {
                            int neighborRank = flatRanks != null ? flatRanks[nr][nc] : Integer.MAX_VALUE;
                            int bestRank = flatRanks != null ? flatRanks[bestIndex / width][bestIndex % width] : Integer.MAX_VALUE;
                            int neighborEdge = edgeDistance(nr, nc, width, height);
                            int bestEdge = edgeDistance(bestIndex / width, bestIndex % width, width, height);
                            if (neighborRank < bestRank || (neighborRank == bestRank && neighborEdge < bestEdge)) {
                                bestIndex = linearIndex(nr, nc, width);
                            }
                        }
                    }
                }

                if (bestIndex < 0) {
                    int flatRankBest = -1;
                    int flatRankEdge = Integer.MAX_VALUE;
                    for (int i = 0; i < ROW_OFFSETS.length; i++) {
                        int nr = row + ROW_OFFSETS[i];
                        int nc = col + COL_OFFSETS[i];
                        if (!isInside(nr, nc, width, height) || !validMask[nr][nc]) {
                            continue;
                        }
                        double neighbor = elevations[nr][nc];
                        if (Math.abs(neighbor - current) > FLOW_EPSILON) {
                            continue;
                        }
                        int neighborRank = flatRanks != null ? flatRanks[nr][nc] : Integer.MAX_VALUE;
                        int neighborEdge = edgeDistance(nr, nc, width, height);
                        if (neighborRank < currentFlatRank
                                && (flatRankBest < 0
                                || neighborRank < flatRankBest
                                || (neighborRank == flatRankBest && neighborEdge < flatRankEdge))) {
                            bestIndex = linearIndex(nr, nc, width);
                            flatRankBest = neighborRank;
                            flatRankEdge = neighborEdge;
                        }
                    }
                }

                if (bestIndex < 0) {
                    int flatBest = -1;
                    int bestFlatEdge = Integer.MAX_VALUE;
                    for (int i = 0; i < ROW_OFFSETS.length; i++) {
                        int nr = row + ROW_OFFSETS[i];
                        int nc = col + COL_OFFSETS[i];
                        if (!isInside(nr, nc, width, height) || !validMask[nr][nc]) {
                            continue;
                        }
                        double neighbor = elevations[nr][nc];
                        int neighborEdge = edgeDistance(nr, nc, width, height);
                        if (Math.abs(neighbor - current) <= FLOW_EPSILON && neighborEdge < currentEdge && neighborEdge < bestFlatEdge) {
                            flatBest = linearIndex(nr, nc, width);
                            bestFlatEdge = neighborEdge;
                        }
                    }
                    bestIndex = flatBest;
                }

                downstream[linearIndex(row, col, width)] = bestIndex;
            }
        }

        return downstream;
    }

    private static long[] computeAccumulation(int[] downstream,
                                              boolean[][] validMask,
                                              int width,
                                              int height) {
        int total = width * height;
        long[] accumulation = new long[total];
        int[] indegree = new int[total];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int index = linearIndex(row, col, width);
                if (!validMask[row][col]) {
                    continue;
                }
                accumulation[index] = 1L;
                int next = downstream[index];
                if (next >= 0) {
                    indegree[next]++;
                }
            }
        }

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int index = linearIndex(row, col, width);
                if (validMask[row][col] && indegree[index] == 0) {
                    queue.add(index);
                }
            }
        }

        while (!queue.isEmpty()) {
            int index = queue.removeFirst();
            int next = downstream[index];
            if (next < 0) {
                continue;
            }
            accumulation[next] += accumulation[index];
            indegree[next]--;
            if (indegree[next] == 0) {
                queue.add(next);
            }
        }

        return accumulation;
    }

    private static ChannelNetwork extractChannelNetwork(SampledDem sampled,
                                                        int[][] flatRanks,
                                                        int[] downstream,
                                                        long[] accumulation,
                                                        int threshold,
                                                        double minimumBranchLengthMeters,
                                                        CleanupLevel cleanupLevel) {
        int width = sampled.width;
        int height = sampled.height;
        int total = width * height;
        boolean[] active = new boolean[total];
        int[] downstreamActive = new int[total];
        int[] upstreamCount = new int[total];
        java.util.Arrays.fill(downstreamActive, -1);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (!sampled.validMask[row][col]) {
                    continue;
                }
                int index = linearIndex(row, col, width);
                if (accumulation[index] >= threshold
                        && isEligibleChannelStarter(row, col, accumulation[index], threshold, flatRanks, cleanupLevel)) {
                    active[index] = true;
                }
            }
        }

        for (int index = 0; index < total; index++) {
            if (!active[index]) {
                continue;
            }
            int next = followToNextActive(index, downstream, active);
            downstreamActive[index] = next;
            if (next >= 0) {
                upstreamCount[next]++;
            }
        }

        List<BranchSegment> branches = traceChannelBranches(
                sampled,
                flatRanks,
                downstream,
                downstreamActive,
                upstreamCount,
                accumulation,
                active,
                cleanupLevel
        );
        if (branches.isEmpty()) {
            return new ChannelNetwork(List.of());
        }

        List<BranchSegment> cleaned = pruneShortLeafBranches(branches, threshold, minimumBranchLengthMeters, cleanupLevel);
        cleaned = removeIsolatedComponents(cleaned, threshold, minimumBranchLengthMeters, cleanupLevel);
        cleaned = pruneShortLeafBranches(cleaned, threshold, minimumBranchLengthMeters, cleanupLevel);

        return new ChannelNetwork(cleaned);
    }

    private static boolean isEligibleChannelStarter(int row,
                                                    int col,
                                                    long accumulation,
                                                    int threshold,
                                                    int[][] flatRanks,
                                                    CleanupLevel cleanupLevel) {
        if (flatRanks == null) {
            return true;
        }
        int flatRank = flatRanks[row][col];
        if (flatRank <= 0) {
            return true;
        }
        int preservedRank = switch (cleanupLevel) {
            case LIGHT -> 1;
            case BALANCED -> 2;
            case STRONG -> 3;
        };
        if (flatRank <= preservedRank) {
            return true;
        }
        long flatAccumulationThreshold = Math.max(threshold * 3L, Math.round(threshold * cleanupLevel.componentAccumulationFactor));
        return accumulation >= flatAccumulationThreshold;
    }

    private static int followToNextActive(int startIndex, int[] downstream, boolean[] active) {
        int current = downstream[startIndex];
        int guard = 0;
        while (current >= 0 && guard < downstream.length) {
            if (active[current]) {
                return current;
            }
            current = downstream[current];
            guard++;
        }
        return -1;
    }

    private static List<BranchSegment> traceChannelBranches(SampledDem sampled,
                                                            int[][] flatRanks,
                                                            int[] downstream,
                                                            int[] downstreamActive,
                                                            int[] upstreamCount,
                                                            long[] accumulation,
                                                            boolean[] active,
                                                            CleanupLevel cleanupLevel) {
        List<BranchSegment> branches = new ArrayList<>();
        boolean[] traversed = new boolean[downstreamActive.length];
        Set<Integer> startNodes = new HashSet<>();

        for (int index = 0; index < downstreamActive.length; index++) {
            if (!active[index] || downstreamActive[index] < 0) {
                continue;
            }
            if (upstreamCount[index] != 1) {
                startNodes.add(index);
            }
        }

        for (int startIndex : startNodes) {
            BranchSegment branch = traceBranch(
                    startIndex,
                    sampled,
                    flatRanks,
                    downstream,
                    downstreamActive,
                    upstreamCount,
                    accumulation,
                    traversed,
                    cleanupLevel
            );
            if (branch != null) {
                branches.add(branch);
            }
        }

        for (int index = 0; index < downstreamActive.length; index++) {
            if (!active[index] || downstreamActive[index] < 0 || traversed[index]) {
                continue;
            }
            BranchSegment branch = traceBranch(
                    index,
                    sampled,
                    flatRanks,
                    downstream,
                    downstreamActive,
                    upstreamCount,
                    accumulation,
                    traversed,
                    cleanupLevel
            );
            if (branch != null) {
                branches.add(branch);
            }
        }

        return branches;
    }

    private static BranchSegment traceBranch(int startIndex,
                                             SampledDem sampled,
                                             int[][] flatRanks,
                                             int[] downstream,
                                             int[] downstreamActive,
                                             int[] upstreamCount,
                                             long[] accumulation,
                                             boolean[] traversed,
                                             CleanupLevel cleanupLevel) {
        int current = startIndex;
        int guard = 0;
        List<Coordinate> coordinates = new ArrayList<>();
        long sourceAccumulation = accumulation[current];
        long maxAccumulation = accumulation[current];
        int maxFlatRank = flatRanks != null ? flatRanks[current / sampled.width][current % sampled.width] : 0;

        Coordinate startCoordinate = sampled.worldCoordinate(current / sampled.width, current % sampled.width);
        if (startCoordinate == null) {
            return null;
        }
        coordinates.add(startCoordinate);

        while (current >= 0 && guard < downstreamActive.length) {
            traversed[current] = true;
            int next = downstreamActive[current];
            if (next < 0) {
                break;
            }

            PathAppendResult path = appendDownstreamPathCoordinates(current, next, downstream, sampled, flatRanks, coordinates);
            if (!path.reachedTarget()) {
                break;
            }
            maxFlatRank = Math.max(maxFlatRank, path.maxFlatRank());
            maxAccumulation = Math.max(maxAccumulation, accumulation[next]);
            current = next;
            guard++;
            if (upstreamCount[current] != 1) {
                break;
            }
        }

        if (coordinates.size() < 2) {
            return null;
        }

        List<Coordinate> optimized = optimizeBranchCoordinates(coordinates);
        if (optimized.size() < 2) {
            return null;
        }

        LineString line = GEOMETRY_FACTORY.createLineString(optimized.toArray(new Coordinate[0]));
        line = simplifyBranchLine(line, sampled, cleanupLevel);
        if (line == null || line.getNumPoints() < 2) {
            return null;
        }
        double lengthMeters = estimateLengthMeters(line, sampled.sourceCrsCode);
        int endNode = current;
        long outletAccumulation = endNode >= 0 ? accumulation[endNode] : maxAccumulation;
        return new BranchSegment(startIndex, endNode, line, lengthMeters, sourceAccumulation, outletAccumulation, maxAccumulation, maxFlatRank);
    }

    private static PathAppendResult appendDownstreamPathCoordinates(int fromActiveIndex,
                                                                    int toActiveIndex,
                                                                    int[] downstream,
                                                                    SampledDem sampled,
                                                                    int[][] flatRanks,
                                                                    List<Coordinate> coordinates) {
        int current = downstream[fromActiveIndex];
        int guard = 0;
        int maxFlatRank = 0;
        while (current >= 0 && guard < downstream.length) {
            Coordinate coordinate = sampled.worldCoordinate(current / sampled.width, current % sampled.width);
            if (coordinate != null) {
                Coordinate copy = new Coordinate(coordinate);
                if (coordinates.isEmpty() || coordinates.get(coordinates.size() - 1).distance(copy) > 0d) {
                    coordinates.add(copy);
                }
            }
            if (flatRanks != null) {
                maxFlatRank = Math.max(maxFlatRank, flatRanks[current / sampled.width][current % sampled.width]);
            }
            if (current == toActiveIndex) {
                return new PathAppendResult(true, maxFlatRank);
            }
            current = downstream[current];
            guard++;
        }
        return new PathAppendResult(false, maxFlatRank);
    }

    private static List<Coordinate> optimizeBranchCoordinates(List<Coordinate> coordinates) {
        List<Coordinate> cleaned = new ArrayList<>();
        if (coordinates == null) {
            return cleaned;
        }
        for (Coordinate coordinate : coordinates) {
            if (coordinate == null) {
                continue;
            }
            Coordinate copy = new Coordinate(coordinate);
            if (cleaned.isEmpty() || cleaned.get(cleaned.size() - 1).distance(copy) > 0d) {
                cleaned.add(copy);
            }
        }
        if (cleaned.size() < 3) {
            return cleaned;
        }

        List<Coordinate> simplified = new ArrayList<>();
        simplified.add(cleaned.get(0));
        for (int i = 1; i < cleaned.size() - 1; i++) {
            Coordinate prev = simplified.get(simplified.size() - 1);
            Coordinate curr = cleaned.get(i);
            Coordinate next = cleaned.get(i + 1);
            if (!isCollinear(prev, curr, next)) {
                simplified.add(curr);
            }
        }
        simplified.add(cleaned.get(cleaned.size() - 1));
        return simplified;
    }

    private static boolean isCollinear(Coordinate a, Coordinate b, Coordinate c) {
        double cross = ((b.x - a.x) * (c.y - b.y)) - ((b.y - a.y) * (c.x - b.x));
        return Math.abs(cross) <= 1e-9;
    }

    private static LineString simplifyBranchLine(LineString line,
                                                 SampledDem sampled,
                                                 CleanupLevel cleanupLevel) {
        if (line == null || line.isEmpty() || line.getNumPoints() < 3 || cleanupLevel == null) {
            return line;
        }
        double cellSize = Math.max(Math.abs(sampled.cellWidthWorld), Math.abs(sampled.cellHeightWorld));
        if (!(cellSize > 0d)) {
            return line;
        }
        double tolerance = cellSize * cleanupLevel.simplifyFactor;
        Geometry simplified = DouglasPeuckerSimplifier.simplify(line, tolerance);
        if (simplified instanceof LineString simplifiedLine && simplifiedLine.getNumPoints() >= 2) {
            return simplifiedLine;
        }
        return line;
    }

    private static List<BranchSegment> pruneShortLeafBranches(List<BranchSegment> branches,
                                                              int threshold,
                                                              double minimumBranchLengthMeters,
                                                              CleanupLevel cleanupLevel) {
        if (branches.isEmpty()) {
            return branches;
        }
        List<BranchSegment> working = new ArrayList<>(branches);
        boolean changed;
        double effectiveMinLength = Math.max(0d, minimumBranchLengthMeters * cleanupLevel.branchLengthFactor);
        long effectiveAccumulation = Math.max(threshold, Math.round(threshold * cleanupLevel.leafAccumulationFactor));

        do {
            changed = false;
            Map<Integer, Integer> nodeDegree = buildNodeDegree(working);
            List<BranchSegment> kept = new ArrayList<>();

            for (BranchSegment branch : working) {
                int startDegree = nodeDegree.getOrDefault(branch.startNode, 0);
                int endDegree = nodeDegree.getOrDefault(branch.endNode, 0);
                boolean leafAtStart = startDegree <= 1;
                boolean leafAtEnd = endDegree <= 1;
                boolean leaf = leafAtStart || leafAtEnd;
                long leafAccumulation = Long.MAX_VALUE;
                if (leafAtStart) {
                    leafAccumulation = Math.min(leafAccumulation, branch.sourceAccumulationCells);
                }
                if (leafAtEnd) {
                    leafAccumulation = Math.min(leafAccumulation, branch.outletAccumulationCells);
                }
                if (leafAccumulation == Long.MAX_VALUE) {
                    leafAccumulation = branch.sourceAccumulationCells;
                }
                if (leaf
                        && branch.lengthMeters < effectiveMinLength * flatLengthFactor(branch)
                        && leafAccumulation < effectiveAccumulation) {
                    changed = true;
                    continue;
                }
                kept.add(branch);
            }

            working = kept;
        } while (changed && !working.isEmpty());

        return working;
    }

    private static List<BranchSegment> removeIsolatedComponents(List<BranchSegment> branches,
                                                                int threshold,
                                                                double minimumBranchLengthMeters,
                                                                CleanupLevel cleanupLevel) {
        if (branches.isEmpty()) {
            return branches;
        }

        Map<Integer, List<Integer>> branchByNode = new HashMap<>();
        for (int i = 0; i < branches.size(); i++) {
            BranchSegment branch = branches.get(i);
            branchByNode.computeIfAbsent(branch.startNode, key -> new ArrayList<>()).add(i);
            branchByNode.computeIfAbsent(branch.endNode, key -> new ArrayList<>()).add(i);
        }

        boolean[] visited = new boolean[branches.size()];
        Set<Integer> removeIndexes = new HashSet<>();
        double effectiveComponentLength = Math.max(
                minimumBranchLengthMeters * cleanupLevel.componentLengthFactor,
                minimumBranchLengthMeters
        );
        long effectiveComponentAccumulation = Math.max(threshold, Math.round(threshold * cleanupLevel.componentAccumulationFactor));

        for (int i = 0; i < branches.size(); i++) {
            if (visited[i]) {
                continue;
            }

            ArrayDeque<Integer> queue = new ArrayDeque<>();
            List<Integer> component = new ArrayList<>();
            queue.add(i);
            visited[i] = true;
            double totalLength = 0d;
            long maxAccumulation = 0L;
            int maxNodeDegree = 0;
            int maxFlatRank = 0;

            while (!queue.isEmpty()) {
                int branchIndex = queue.removeFirst();
                component.add(branchIndex);
                BranchSegment branch = branches.get(branchIndex);
                totalLength += branch.lengthMeters;
                maxAccumulation = Math.max(maxAccumulation, branch.maxAccumulationCells);
                maxFlatRank = Math.max(maxFlatRank, branch.maxFlatRank);

                for (int node : new int[]{branch.startNode, branch.endNode}) {
                    maxNodeDegree = Math.max(maxNodeDegree, branchByNode.getOrDefault(node, List.of()).size());
                    for (int linkedIndex : branchByNode.getOrDefault(node, List.of())) {
                        if (!visited[linkedIndex]) {
                            visited[linkedIndex] = true;
                            queue.add(linkedIndex);
                        }
                    }
                }
            }

            if (maxNodeDegree <= 2
                    && totalLength < effectiveComponentLength * (maxFlatRank > 0 ? 1.6d : 1.0d)
                    && maxAccumulation < effectiveComponentAccumulation) {
                removeIndexes.addAll(component);
            }
        }

        if (removeIndexes.isEmpty()) {
            return branches;
        }

        List<BranchSegment> kept = new ArrayList<>();
        for (int i = 0; i < branches.size(); i++) {
            if (!removeIndexes.contains(i)) {
                kept.add(branches.get(i));
            }
        }
        return kept;
    }

    private static Map<Integer, Integer> buildNodeDegree(List<BranchSegment> branches) {
        Map<Integer, Integer> degree = new HashMap<>();
        for (BranchSegment branch : branches) {
            degree.merge(branch.startNode, 1, Integer::sum);
            degree.merge(branch.endNode, 1, Integer::sum);
        }
        return degree;
    }

    private static double flatLengthFactor(BranchSegment branch) {
        if (branch.maxFlatRank >= 4) {
            return 2.4d;
        }
        if (branch.maxFlatRank > 0) {
            return 1.6d;
        }
        return 1.0d;
    }

    private static double estimateLengthMeters(LineString line, String sourceCrsCode) {
        if (line == null || line.isEmpty()) {
            return Double.NaN;
        }
        try {
            String metricCrs = resolveMetricCrs(sourceCrsCode);
            if (metricCrs.equalsIgnoreCase(sourceCrsCode)) {
                return line.getLength();
            }
            MathTransform transform = buildTransform(sourceCrsCode, metricCrs);
            LineString metricLine = (LineString) JTS.transform(line, transform);
            return metricLine.getLength();
        } catch (Exception ex) {
            return line.getLength();
        }
    }

    private static double estimateCellAreaSquareMeters(double minX,
                                                       double maxX,
                                                       double minY,
                                                       double maxY,
                                                       int width,
                                                       int height,
                                                       String sourceCrsCode) {
        try {
            String metricCrs = resolveMetricCrs(sourceCrsCode);
            if (metricCrs.equalsIgnoreCase(sourceCrsCode)) {
                return Math.abs((maxX - minX) / width) * Math.abs((maxY - minY) / height);
            }

            MathTransform transform = buildTransform(sourceCrsCode, metricCrs);
            Coordinate a = transformCoordinate(new Coordinate(minX, minY), transform);
            Coordinate b = transformCoordinate(new Coordinate(maxX, minY), transform);
            Coordinate c = transformCoordinate(new Coordinate(minX, maxY), transform);
            double gridWidthM = a.distance(b);
            double gridHeightM = a.distance(c);
            return (gridWidthM / width) * (gridHeightM / height);
        } catch (Exception ex) {
            return Double.NaN;
        }
    }

    private static double[] estimateCellSizeMeters(double minX,
                                                   double maxX,
                                                   double minY,
                                                   double maxY,
                                                   int width,
                                                   int height,
                                                   String sourceCrsCode) {
        try {
            String metricCrs = resolveMetricCrs(sourceCrsCode);
            if (metricCrs.equalsIgnoreCase(sourceCrsCode)) {
                return new double[]{
                        Math.abs((maxX - minX) / width),
                        Math.abs((maxY - minY) / height)
                };
            }

            MathTransform transform = buildTransform(sourceCrsCode, metricCrs);
            Coordinate a = transformCoordinate(new Coordinate(minX, minY), transform);
            Coordinate b = transformCoordinate(new Coordinate(maxX, minY), transform);
            Coordinate c = transformCoordinate(new Coordinate(minX, maxY), transform);
            return new double[]{
                    a.distance(b) / Math.max(1, width),
                    a.distance(c) / Math.max(1, height)
            };
        } catch (Exception ex) {
            double inferred = Double.isFinite((maxX - minX) / Math.max(1, width))
                    ? Math.abs((maxX - minX) / Math.max(1, width))
                    : 1d;
            return new double[]{inferred, inferred};
        }
    }

    private static String classifyChannel(long accCells, int threshold) {
        if (accCells >= (long) threshold * 8L) {
            return "Principal";
        }
        if (accCells >= (long) threshold * 3L) {
            return "Secundaria";
        }
        return "Local";
    }

    private static SimpleFeatureType buildOutputSchema(String outputLayerName, String sourceCrsCode) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(outputLayerName));
        if (sourceCrsCode != null && !sourceCrsCode.isBlank()) {
            try {
                builder.setCRS(CRSDefinitions.decode(sourceCrsCode, true));
            } catch (Exception ignored) {
            }
        }
        builder.add("the_geom", LineString.class);
        builder.add("drain_id", Integer.class);
        builder.add("channel_class", String.class);
        builder.add("acc_cells", Long.class);
        builder.add("acc_area_m2", Double.class);
        builder.add("length_m", Double.class);
        builder.add("threshold_cells", Integer.class);
        return builder.buildFeatureType();
    }

    private static MathTransform buildTransform(String sourceCode, String targetCode) throws Exception {
        CoordinateReferenceSystem source = CRSDefinitions.decode(normalizeCode(sourceCode, "EPSG:4326"), true);
        CoordinateReferenceSystem target = CRSDefinitions.decode(normalizeCode(targetCode, "EPSG:3857"), true);
        return CRS.findMathTransform(source, target, true);
    }

    private static Coordinate transformCoordinate(Coordinate source, MathTransform transform) throws Exception {
        Position2D src = new Position2D(source.x, source.y);
        Position2D dst = new Position2D();
        transform.transform(src, dst);
        return new Coordinate(dst.x, dst.y);
    }

    private static String resolveMetricCrs(String sourceCode) {
        try {
            CoordinateReferenceSystem sourceCrs = CRSDefinitions.decode(normalizeCode(sourceCode, "EPSG:4326"), true);
            if (sourceCrs instanceof ProjectedCRS) {
                return normalizeCode(CRS.toSRS(sourceCrs, true), "EPSG:3857");
            }
        } catch (Exception ignored) {
        }
        return "EPSG:3857";
    }

    private static String normalizeCode(String code, String fallback) {
        String normalized = CRSDefinitions.normalizeCode(code);
        if (normalized == null || normalized.isBlank()) {
            return fallback;
        }
        return normalized;
    }

    private static String safeTypeName(String text) {
        String base = text != null && !text.isBlank() ? text.trim() : "escorrentias";
        String normalized = base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+", "_");
        normalized = normalized.replaceAll("_+", "_");
        normalized = normalized.replaceAll("^_+", "");
        normalized = normalized.replaceAll("_+$", "");
        if (normalized.isBlank()) {
            normalized = "escorrentias";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "d_" + normalized;
        }
        return normalized;
    }

    private static double[][] copyGrid(double[][] source) {
        double[][] copy = new double[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = java.util.Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }

    private static boolean[][] copyMask(boolean[][] source) {
        boolean[][] copy = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = java.util.Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }

    private static int[][] copyFlatRanks(int[][] source) {
        if (source == null) {
            return null;
        }
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = java.util.Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }

    private static int edgeDistance(int row, int col, int width, int height) {
        return Math.min(Math.min(row, height - 1 - row), Math.min(col, width - 1 - col));
    }

    private static boolean isInside(int row, int col, int width, int height) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    private static int linearIndex(int row, int col, int width) {
        return row * width + col;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String coordinateKey(Coordinate coordinate) {
        if (coordinate == null) {
            return "";
        }
        return String.format(Locale.US, "%.6f|%.6f", coordinate.x, coordinate.y);
    }

    public enum AnalysisDetail {
        FAST("Rapida", 256),
        BALANCED("Equilibrada", 512),
        DETAILED("Detallada", 768);

        private final String label;
        private final int maxGridSize;

        AnalysisDetail(String label, int maxGridSize) {
            this.label = label;
            this.maxGridSize = maxGridSize;
        }

        @Override
        public String toString() {
            return label + " (" + maxGridSize + ")";
        }
    }

    public enum HydrologicConditioning {
        BASIC("Basico"),
        ROBUST("Robusto (priority flood + flats)"),
        ADVANCED("Avanzado (breach + priority flood + flats)");

        private final String label;

        HydrologicConditioning(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum CleanupLevel {
        LIGHT("Suave", 0.75d, 1.6d, 4.0d, 1.4d, 0.45d),
        BALANCED("Equilibrado", 1.0d, 2.2d, 5.0d, 1.9d, 0.75d),
        STRONG("Fuerte", 1.35d, 3.0d, 6.0d, 2.6d, 1.1d);

        private final String label;
        private final double branchLengthFactor;
        private final double componentLengthFactor;
        private final double leafAccumulationFactor;
        private final double componentAccumulationFactor;
        private final double simplifyFactor;

        CleanupLevel(String label,
                     double branchLengthFactor,
                     double componentLengthFactor,
                     double leafAccumulationFactor,
                     double componentAccumulationFactor,
                     double simplifyFactor) {
            this.label = label;
            this.branchLengthFactor = branchLengthFactor;
            this.componentLengthFactor = componentLengthFactor;
            this.leafAccumulationFactor = leafAccumulationFactor;
            this.componentAccumulationFactor = componentAccumulationFactor;
            this.simplifyFactor = simplifyFactor;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public record GeneratedDrainageLayer(Layer layer,
                                         ShapefileData data,
                                         int thresholdCells,
                                         int analysisWidth,
                                         int analysisHeight,
                                         double cellAreaSquareMeters,
                                         double minimumBranchLengthMeters,
                                         CleanupLevel cleanupLevel,
                                         HydrologicConditioning conditioningMode) {
    }

    public record HydrologyGrid(double[][] sourceElevations,
                                double[][] conditionedElevations,
                                boolean[][] validMask,
                                int[][] flatRanks,
                                int[] downstream,
                                long[] accumulation,
                                int width,
                                int height,
                                double minX,
                                double maxX,
                                double minY,
                                double maxY,
                                double cellWidthWorld,
                                double cellHeightWorld,
                                double cellWidthMeters,
                                double cellHeightMeters,
                                double cellAreaSquareMeters,
                                String sourceCrsCode,
                                AnalysisDetail detail,
                                HydrologicConditioning conditioningMode) {

        public boolean isValidCell(int row, int col) {
            return row >= 0 && row < height
                    && col >= 0 && col < width
                    && validMask[row][col];
        }

        public int linearIndex(int row, int col) {
            return row * width + col;
        }

        public Coordinate worldCoordinate(int row, int col) {
            if (!isValidCell(row, col)) {
                return null;
            }
            double x = minX + ((col + 0.5d) * cellWidthWorld);
            double y = maxY - ((row + 0.5d) * cellHeightWorld);
            return new Coordinate(x, y);
        }

        public int rowFromWorldY(double y) {
            double raw = (maxY - y) / Math.max(1e-9d, cellHeightWorld);
            return clamp((int) Math.floor(raw), 0, Math.max(0, height - 1));
        }

        public int colFromWorldX(double x) {
            double raw = (x - minX) / Math.max(1e-9d, cellWidthWorld);
            return clamp((int) Math.floor(raw), 0, Math.max(0, width - 1));
        }

        public Envelope envelope() {
            return new Envelope(minX, maxX, minY, maxY);
        }
    }

    private record ChannelNetwork(List<BranchSegment> branches) {
    }

    private record ConditionedDem(double[][] elevations,
                                  int[][] flatRanks,
                                  HydrologicConditioning conditioningMode) {
    }

    private record BranchSegment(int startNode,
                                 int endNode,
                                 LineString line,
                                 double lengthMeters,
                                 long sourceAccumulationCells,
                                 long outletAccumulationCells,
                                 long maxAccumulationCells,
                                 int maxFlatRank) {
    }

    private record PathAppendResult(boolean reachedTarget,
                                    int maxFlatRank) {
    }

    private record CellState(int row,
                             int col,
                             double elevation,
                             int edgeDistance) {
    }

    private record SampledDem(double[][] elevations,
                              boolean[][] validMask,
                              int width,
                              int height,
                              double minX,
                              double maxX,
                              double minY,
                              double maxY,
                              double cellWidthWorld,
                              double cellHeightWorld,
                              double cellAreaSquareMeters,
                              String sourceCrsCode,
                              int validCellCount,
                              AnalysisDetail detail) {

        Coordinate worldCoordinate(int row, int col) {
            if (!isInside(row, col, width, height) || !validMask[row][col]) {
                return null;
            }
            double x = minX + ((col + 0.5d) * cellWidthWorld);
            double y = maxY - ((row + 0.5d) * cellHeightWorld);
            return new Coordinate(x, y);
        }
    }
}
