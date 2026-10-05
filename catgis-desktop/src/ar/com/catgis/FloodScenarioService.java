package ar.com.catgis;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import java.awt.Color;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;

public final class FloodScenarioService {

    public static final String OP_PRELIMINARY_FLOOD = "preliminary_flood";
    private static final int TRANSPARENT_ARGB = new Color(0, 0, 0, 0).getRGB();

    private FloodScenarioService() {
    }

    public static FloodScenarioResult generateScenario(FloodScenarioRequest request) throws Exception {
        validateRequest(request);
        DrainageExtractionService.HydrologyGrid grid = DrainageExtractionService.analyzeHydrologyGrid(
                request.rasterLayer(),
                request.detail(),
                request.conditioning()
        );

        double effectiveRainfallMeters = (request.rainfallMillimeters() / 1000d) * request.runoffCoefficient();
        double minimumDisplayedDepthMeters = request.minimumDisplayedDepthMillimeters() / 1000d;
        double maxAccumulationLog = 1d;
        for (long value : grid.accumulation()) {
            maxAccumulationLog = Math.max(maxAccumulationLog, Math.log1p(Math.max(0L, value)));
        }

        BufferedImage image = new BufferedImage(grid.width(), grid.height(), BufferedImage.TYPE_INT_ARGB);
        double[][] depths = new double[grid.height()][grid.width()];
        double maxDepth = 0d;
        int affectedCells = 0;

        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col)) {
                    image.setRGB(col, row, TRANSPARENT_ARGB);
                    depths[row][col] = Double.NaN;
                    continue;
                }

                int index = grid.linearIndex(row, col);
                double sourceElevation = grid.sourceElevations()[row][col];
                double conditionedElevation = grid.conditionedElevations()[row][col];
                double depressionDepth = Math.max(0d, conditionedElevation - sourceElevation);

                double channelDepth = 0d;
                if (grid.accumulation()[index] >= request.channelThreshold()) {
                    double ratio = Math.log1p(Math.max(0L, grid.accumulation()[index])) / maxAccumulationLog;
                    ratio = Math.max(0d, Math.min(1d, Math.pow(ratio, 0.85d)));
                    channelDepth = effectiveRainfallMeters * ratio * 0.65d;
                }

                double waterDepth = Math.max(Math.min(effectiveRainfallMeters, depressionDepth), channelDepth);
                depths[row][col] = waterDepth;
                maxDepth = Math.max(maxDepth, waterDepth);

                if (waterDepth >= minimumDisplayedDepthMeters) {
                    affectedCells++;
                }
            }
        }

        if (affectedCells == 0 || !(maxDepth > 0d)) {
            throw new IllegalStateException("No se detectaron celdas potencialmente anegables con esos parametros. Prueba con una lluvia mayor, un umbral de red menor o un DEM recortado al sector real.");
        }

        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                double depth = depths[row][col];
                if (!Double.isFinite(depth) || depth < minimumDisplayedDepthMeters) {
                    image.setRGB(col, row, TRANSPARENT_ARGB);
                    continue;
                }
                double ratio = Math.max(0d, Math.min(1d, Math.pow(depth / maxDepth, 0.72d)));
                Color color = interpolateColor(
                        ratio,
                        new Color(223, 246, 255, 130),
                        new Color(119, 206, 255, 165),
                        new Color(37, 99, 235, 205),
                        new Color(8, 47, 107, 230)
                );
                image.setRGB(col, row, color.getRGB());
            }
        }

        String parameterSpec = buildParameterSpec(request);
        String baseName = request.baseName() != null && !request.baseName().isBlank()
                ? request.baseName().trim()
                : request.rasterLayer().getName();
        String rainfallLabel = formatNumber(request.rainfallMillimeters()) + "mm";
        RasterLayer layer = new RasterLayer("Inundacion preliminar - " + baseName + " - " + rainfallLabel,
                request.rasterLayer().getPath() != null ? request.rasterLayer().getPath() : "");
        layer.setVisible(true);
        layer.setSourceName("Escenario preliminar de anegamiento derivado de " + request.rasterLayer().getName()
                + "; lluvia=" + formatNumber(request.rainfallMillimeters())
                + "mm; escorrentia=" + formatPercent(request.runoffCoefficient())
                + "; umbral=" + request.channelThreshold()
                + " celdas");
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setFeatureCount(1);
        layer.setOpacity(0.82f);
        layer.setGrayscale(false);
        layer.setAutoContrast(false);
        layer.setRedBand(0);
        layer.setGreenBand(1);
        layer.setBlueBand(2);
        layer.setRasterMode("derived");
        layer.setDerivedOperation(OP_PRELIMINARY_FLOOD);
        layer.setDerivedParameters(parameterSpec);

        LocalRasterData data = new LocalRasterData(
                image,
                new Envelope(grid.minX(), grid.maxX(), grid.minY(), grid.maxY()),
                Math.max(1, image.getRaster().getNumBands()),
                true,
                grid.sourceCrsCode(),
                "derived",
                grid.sourceCrsCode()
        );

        double affectedArea = grid.cellAreaSquareMeters() > 0d ? affectedCells * grid.cellAreaSquareMeters() : Double.NaN;
        return new FloodScenarioResult(layer, data, affectedArea, maxDepth, affectedCells, depths, request, grid.sourceCrsCode());
    }

    public static LocalRasterData regenerateDerivedRasterData(RasterLayer layer) throws Exception {
        if (layer == null || !layer.isDerivedLayer() || !OP_PRELIMINARY_FLOOD.equalsIgnoreCase(layer.getDerivedOperation())) {
            throw new IllegalArgumentException("La capa raster no corresponde a un escenario preliminar de inundacion.");
        }
        Map<String, String> parameters = parseParameterSpec(layer.getDerivedParameters());
        FloodScenarioRequest request = new FloodScenarioRequest(
                layer,
                parameters.getOrDefault("baseName", layer.getName()),
                parseDetail(parameters.get("detail")),
                parseConditioning(parameters.get("conditioning")),
                parsePositiveInt(parameters.get("threshold"), 30, 2),
                parsePositiveDouble(parameters.get("rainMm"), 50d, 1d),
                parseRatio(parameters.get("runoff"), 0.7d),
                parsePositiveDouble(parameters.get("minDepthMm"), 15d, 0d)
        );
        return generateScenario(request).data();
    }

    public static File exportScenarioDepthGeoTiff(FloodScenarioResult result, File outputFile) throws Exception {
        if (result == null || result.depthMeters() == null || result.data() == null || result.data().getEnvelope() == null) {
            throw new IllegalArgumentException("No hay escenario de inundacion valido para exportar.");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("Debes indicar un archivo de salida GeoTIFF.");
        }

        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new IllegalStateException("No se pudo crear la carpeta de salida del GeoTIFF.");
        }

        int height = result.depthMeters().length;
        int width = height > 0 ? result.depthMeters()[0].length : 0;
        if (width <= 0 || height <= 0) {
            throw new IllegalStateException("El escenario no tiene grilla de profundidades exportable.");
        }

        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double depth = result.depthMeters()[row][col];
                raster.setSample(col, row, 0, Double.isFinite(depth) ? depth : Float.NaN);
            }
        }

        String crsCode = CRSDefinitions.normalizeCode(result.crsCode());
        CoordinateReferenceSystem crs = CRSDefinitions.decode(crsCode, true);
        Envelope envelope = result.data().getEnvelope();
        ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(envelope, crs);
        GridCoverage2D coverage = new GridCoverageFactory().create(
                result.layer() != null ? result.layer().getName() : "flood_scenario",
                raster,
                referencedEnvelope
        );

        GeoTiffWriter writer = new GeoTiffWriter(outputFile);
        try {
            writer.write(coverage, (GeneralParameterValue[]) null);
        } finally {
            writer.dispose();
        }
        RasterSidecarSupport.write(outputFile, envelope, crsCode);
        return outputFile;
    }

    public static List<Double> parseRainfallScenarioList(String rawValue) {
        List<Double> values = new ArrayList<>();
        if (rawValue == null || rawValue.isBlank()) {
            return values;
        }
        String[] parts = rawValue.split("[,;]");
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            double parsed = Double.parseDouble(part.trim().replace(",", "."));
            if (!(parsed > 0d)) {
                throw new IllegalArgumentException("La lluvia debe ser mayor a cero.");
            }
            values.add(parsed);
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Debes indicar al menos un escenario de lluvia valido.");
        }
        return values;
    }

    public static String buildScenarioExportFileName(FloodScenarioResult result) {
        if (result == null || result.request() == null) {
            return "inundacion_preliminar.tif";
        }
        String baseName = result.request().baseName() != null && !result.request().baseName().isBlank()
                ? result.request().baseName().trim()
                : result.layer() != null ? result.layer().getName() : "dem";
        return sanitizeFileStem("inundacion_preliminar_" + baseName + "_" + formatNumber(result.request().rainfallMillimeters()) + "mm") + ".tif";
    }

    private static void validateRequest(FloodScenarioRequest request) {
        if (request == null || !(request.rasterLayer() instanceof RasterLayer)) {
            throw new IllegalArgumentException("Debes indicar un DEM raster valido.");
        }
        if (!(request.rainfallMillimeters() > 0d)) {
            throw new IllegalArgumentException("La lluvia debe ser mayor a cero.");
        }
        if (!(request.runoffCoefficient() > 0d) || request.runoffCoefficient() > 1d) {
            throw new IllegalArgumentException("El coeficiente de escorrentia debe estar entre 0 y 1.");
        }
        if (request.channelThreshold() < 2) {
            throw new IllegalArgumentException("El umbral de acumulacion debe ser 2 o mayor.");
        }
        if (request.minimumDisplayedDepthMillimeters() < 0d) {
            throw new IllegalArgumentException("La profundidad minima visible no puede ser negativa.");
        }
    }

    private static String buildParameterSpec(FloodScenarioRequest request) {
        return "detail=" + request.detail().name()
                + ";conditioning=" + request.conditioning().name()
                + ";threshold=" + request.channelThreshold()
                + ";rainMm=" + formatNumber(request.rainfallMillimeters())
                + ";runoff=" + formatRatio(request.runoffCoefficient())
                + ";minDepthMm=" + formatNumber(request.minimumDisplayedDepthMillimeters())
                + ";baseName=" + sanitizeParameterValue(request.baseName());
    }

    private static Map<String, String> parseParameterSpec(String spec) {
        Map<String, String> values = new LinkedHashMap<>();
        if (spec == null || spec.isBlank()) {
            return values;
        }
        String[] parts = spec.split(";");
        for (String part : parts) {
            if (part == null) {
                continue;
            }
            int separator = part.indexOf('=');
            if (separator <= 0 || separator >= part.length() - 1) {
                continue;
            }
            values.put(part.substring(0, separator).trim(), part.substring(separator + 1).trim());
        }
        return values;
    }

    private static DrainageExtractionService.AnalysisDetail parseDetail(String value) {
        try {
            return value != null && !value.isBlank()
                    ? DrainageExtractionService.AnalysisDetail.valueOf(value.trim())
                    : DrainageExtractionService.AnalysisDetail.BALANCED;
        } catch (Exception ex) {
            return DrainageExtractionService.AnalysisDetail.BALANCED;
        }
    }

    private static DrainageExtractionService.HydrologicConditioning parseConditioning(String value) {
        try {
            return value != null && !value.isBlank()
                    ? DrainageExtractionService.HydrologicConditioning.valueOf(value.trim())
                    : DrainageExtractionService.HydrologicConditioning.ADVANCED;
        } catch (Exception ex) {
            return DrainageExtractionService.HydrologicConditioning.ADVANCED;
        }
    }

    private static int parsePositiveInt(String value, int fallback, int minValue) {
        try {
            int parsed = Integer.parseInt(value != null ? value.trim() : "");
            return Math.max(minValue, parsed);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static double parsePositiveDouble(String value, double fallback, double minValue) {
        try {
            double parsed = Double.parseDouble((value != null ? value.trim() : "").replace(",", "."));
            return Math.max(minValue, parsed);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static double parseRatio(String value, double fallback) {
        try {
            double parsed = Double.parseDouble((value != null ? value.trim() : "").replace(",", "."));
            if (parsed > 1d) {
                parsed = parsed / 100d;
            }
            return Math.max(0.01d, Math.min(1d, parsed));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static String sanitizeParameterValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(';', ' ').replace('=', ' ').trim();
    }

    private static String formatNumber(double value) {
        if (Math.rint(value) == value) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private static String formatRatio(double value) {
        return String.format(Locale.US, "%.4f", Math.max(0d, Math.min(1d, value)));
    }

    private static String formatPercent(double value) {
        return String.format(Locale.US, "%.0f%%", Math.max(0d, Math.min(1d, value)) * 100d);
    }

    private static String sanitizeFileStem(String value) {
        if (value == null || value.isBlank()) {
            return "inundacion_preliminar";
        }
        return value
                .replaceAll("[\\\\/:*?\"<>|]+", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
    }

    private static Color interpolateColor(double ratio, Color... palette) {
        if (palette == null || palette.length == 0) {
            return new Color(37, 99, 235, 160);
        }
        if (palette.length == 1) {
            return palette[0];
        }
        double clamped = Math.max(0d, Math.min(1d, ratio));
        double scaled = clamped * (palette.length - 1);
        int leftIndex = Math.max(0, Math.min(palette.length - 1, (int) Math.floor(scaled)));
        int rightIndex = Math.max(0, Math.min(palette.length - 1, leftIndex + 1));
        double localRatio = scaled - leftIndex;
        Color left = palette[leftIndex];
        Color right = palette[rightIndex];
        int red = (int) Math.round(left.getRed() + ((right.getRed() - left.getRed()) * localRatio));
        int green = (int) Math.round(left.getGreen() + ((right.getGreen() - left.getGreen()) * localRatio));
        int blue = (int) Math.round(left.getBlue() + ((right.getBlue() - left.getBlue()) * localRatio));
        int alpha = (int) Math.round(left.getAlpha() + ((right.getAlpha() - left.getAlpha()) * localRatio));
        return new Color(red, green, blue, alpha);
    }

    public record FloodScenarioRequest(Layer rasterLayer,
                                       String baseName,
                                       DrainageExtractionService.AnalysisDetail detail,
                                       DrainageExtractionService.HydrologicConditioning conditioning,
                                       int channelThreshold,
                                       double rainfallMillimeters,
                                       double runoffCoefficient,
                                       double minimumDisplayedDepthMillimeters) {
    }

    public record FloodScenarioResult(RasterLayer layer,
                                      LocalRasterData data,
                                      double affectedAreaSquareMeters,
                                      double maxDepthMeters,
                                      int affectedCells,
                                      double[][] depthMeters,
                                      FloodScenarioRequest request,
                                      String crsCode) {
    }
}
