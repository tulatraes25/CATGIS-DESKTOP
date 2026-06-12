package ar.com.catgis.data.raster;

import ar.com.catgis.CatgisLogger;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.raster.LocalRasterData;

import ar.com.catgis.RasterSidecarSupport;
import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.CoordinateTransformSupport;
import ar.com.catgis.CatgisDesktopApp;
import org.geotools.api.coverage.grid.GridCoverageReader;
import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.processing.Operations;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class RasterCoverageSupport {

    private RasterCoverageSupport() {
    }

    public static GridCoverage2D readCoverage(Layer layer) throws IOException {
        if (layer == null || layer.getPath() == null || layer.getPath().isBlank()) {
            throw new IOException("La capa raster seleccionada no tiene una ruta de archivo valida.");
        }
        File rasterFile = new File(layer.getPath());
        GridCoverage2D coverage;
        try {
            coverage = readCoverage(rasterFile);
        } catch (IOException primaryFailure) {
            coverage = readCoverageUsingLayerCrsFallback(rasterFile, layer, primaryFailure);
        }
        return adaptCoverageToProject(layer, coverage);
    }

    public static GridCoverage2D readCoverageNative(Layer layer) throws IOException {
        if (layer == null || layer.getPath() == null || layer.getPath().isBlank()) {
            throw new IOException("La capa raster seleccionada no tiene una ruta de archivo valida.");
        }
        File rasterFile = new File(layer.getPath());
        try {
            return readCoverage(rasterFile);
        } catch (IOException primaryFailure) {
            return readCoverageUsingLayerCrsFallback(rasterFile, layer, primaryFailure);
        }
    }

    public static GridCoverage2D readCoverage(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("No se encontro el archivo raster indicado.");
        }

        GridCoverageReader reader = null;
        try {
            AbstractGridFormat format = org.geotools.coverage.grid.io.GridFormatFinder.findFormat(file);
            reader = format != null ? format.getReader(file) : null;
            if (reader == null && file.getName().toLowerCase().endsWith(".tif")) {
                reader = new GeoTiffReader(file);
            }
            if (reader == null) {
                throw new IOException("No se pudo crear un lector raster para: " + file.getAbsolutePath());
            }
            GridCoverage2D coverage = (GridCoverage2D) reader.read((GeneralParameterValue[]) null);
            return applySidecarMetadataIfPresent(file, coverage);
        } catch (Exception ex) {
            GridCoverage2D sidecarCoverage = readCoverageFromSidecar(file);
            if (sidecarCoverage != null) {
                return sidecarCoverage;
            }
            if (ex instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("No se pudo leer la cobertura raster.", ex);
        } finally {
            if (reader != null) {
                try {
                    reader.dispose();
                } catch (Exception ignored) { CatgisLogger.warn("RasterCoverageSupport: operation failed", ignored); }
            }
        }
    }

    public static GridCoverage2D applySidecarMetadataIfPresent(File file, GridCoverage2D coverage) {
        if (file == null || coverage == null || coverage.getRenderedImage() == null) {
            return coverage;
        }

        try {
            RasterSidecarSupport.Metadata metadata = RasterSidecarSupport.read(file);
            if (metadata == null || metadata.envelope() == null || metadata.envelope().isNull()) {
                return coverage;
            }
            String sourceCrs = CRSDefinitions.normalizeCode(metadata.sourceCrs());
            if (sourceCrs.isBlank()) {
                sourceCrs = resolveCoverageIdentifier(coverage);
            }
            if (sourceCrs.isBlank()) {
                return coverage;
            }
            CoordinateReferenceSystem crs = CRSDefinitions.decode(sourceCrs, true);
            ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(metadata.envelope(), crs);
            return new GridCoverageFactory().create(file.getName(), coverage.getRenderedImage(), referencedEnvelope);
        } catch (Exception ignored) {
            return coverage;
        }
    }

    private static GridCoverage2D readCoverageFromSidecar(File file) {
        try {
            RasterSidecarSupport.Metadata metadata = RasterSidecarSupport.read(file);
            if (metadata == null || metadata.envelope() == null || metadata.envelope().isNull()) {
                return null;
            }
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return null;
            }
            CoordinateReferenceSystem crs = CRSDefinitions.decode(metadata.sourceCrs(), true);
            ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(metadata.envelope(), crs);
            return new GridCoverageFactory().create(file.getName(), image.getRaster(), referencedEnvelope);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static GridCoverage2D readCoverageUsingLayerCrsFallback(File file,
                                                                    Layer layer,
                                                                    IOException primaryFailure) throws IOException {
        String forcedSourceCrs = layer != null ? CRSDefinitions.normalizeCode(layer.getSourceCRS()) : "";
        String extractedSourceCrs = CRSDefinitions.normalizeCode(readCrsCodeFromTiffMetadata(file));
        String effectiveSourceCrs = !extractedSourceCrs.isBlank() ? extractedSourceCrs : forcedSourceCrs;
        if (effectiveSourceCrs.isBlank()) {
            throw primaryFailure;
        }

        BufferedImage raw = ImageIO.read(file);
        if (raw == null) {
            throw primaryFailure;
        }

        Envelope envelope = readEnvelopeFromTiffMetadata(file, raw.getWidth(), raw.getHeight());
        if (envelope == null || envelope.isNull()) {
            throw primaryFailure;
        }

        try {
            CoordinateReferenceSystem crs = CRSDefinitions.decode(effectiveSourceCrs, true);
            ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(envelope, crs);
            return new GridCoverageFactory().create(file.getName(), raw.getRaster(), referencedEnvelope);
        } catch (Exception ex) {
            throw primaryFailure;
        }
    }

    public static String readCrsCodeFromTiffMetadata(File file) {
        if (file == null) {
            return "";
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            if (input == null) {
                return "";
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return "";
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                IIOMetadata metadata = reader.getImageMetadata(0);
                if (metadata == null) {
                    return "";
                }

                List<Double> directoryValues = new ArrayList<>();
                for (String formatName : metadata.getMetadataFormatNames()) {
                    Node root = metadata.getAsTree(formatName);
                    if (root == null) {
                        continue;
                    }
                    Node geoKeyDirectory = findTiffField(root, 34735);
                    if (geoKeyDirectory != null) {
                        directoryValues.clear();
                        collectNumericValues(geoKeyDirectory, directoryValues);
                        String projected = decodeGeoKeyDirectory(directoryValues, 3072);
                        if (!projected.isBlank()) {
                            return projected;
                        }
                        String geographic = decodeGeoKeyDirectory(directoryValues, 2048);
                        if (!geographic.isBlank()) {
                            return geographic;
                        }
                    }
                }
            } finally {
                reader.dispose();
            }
        } catch (Exception ignored) {
            return "";
        }

        return "";
    }

    private static String decodeGeoKeyDirectory(List<Double> values, int keyId) {
        if (values == null || values.size() < 8) {
            return "";
        }
        int count = values.get(3).intValue();
        int offset = 4;
        for (int i = 0; i < count; i++) {
            int entryIndex = offset + (i * 4);
            if (entryIndex + 3 >= values.size()) {
                break;
            }
            int currentKeyId = values.get(entryIndex).intValue();
            int tiffTagLocation = values.get(entryIndex + 1).intValue();
            int valueOffset = values.get(entryIndex + 3).intValue();
            if (currentKeyId == keyId && tiffTagLocation == 0 && valueOffset > 0) {
                return CRSDefinitions.normalizeCode("EPSG:" + valueOffset);
            }
        }
        return "";
    }

    public static Envelope readEnvelopeFromTiffMetadata(File file, int width, int height) {
        if (file == null || width <= 0 || height <= 0) {
            return null;
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            if (input == null) {
                return null;
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return null;
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                IIOMetadata metadata = reader.getImageMetadata(0);
                if (metadata == null) {
                    return null;
                }

                double[] pixelScale = null;
                double[] tiePoint = null;
                for (String formatName : metadata.getMetadataFormatNames()) {
                    Node root = metadata.getAsTree(formatName);
                    if (root == null) {
                        continue;
                    }
                    if (pixelScale == null) {
                        pixelScale = readTiffDoubleField(root, 33550);
                    }
                    if (tiePoint == null) {
                        tiePoint = readTiffDoubleField(root, 33922);
                    }
                    if (pixelScale != null && tiePoint != null) {
                        break;
                    }
                }

                if (pixelScale == null || pixelScale.length < 2 || tiePoint == null || tiePoint.length < 6) {
                    return null;
                }

                double scaleX = Math.abs(pixelScale[0]);
                double scaleY = Math.abs(pixelScale[1]);
                if (!(scaleX > 0d) || !(scaleY > 0d)) {
                    return null;
                }

                double rasterX = tiePoint[0];
                double rasterY = tiePoint[1];
                double modelX = tiePoint[3];
                double modelY = tiePoint[4];

                double originX = modelX - (rasterX * scaleX);
                double originY = modelY + (rasterY * scaleY);
                return new Envelope(
                        originX,
                        originX + (width * scaleX),
                        originY - (height * scaleY),
                        originY
                );
            } finally {
                reader.dispose();
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private static double[] readTiffDoubleField(Node root, int fieldNumber) {
        Node field = findTiffField(root, fieldNumber);
        if (field == null) {
            return null;
        }

        List<Double> values = new ArrayList<>();
        collectNumericValues(field, values);
        if (values.isEmpty()) {
            return null;
        }

        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private static Node findTiffField(Node node, int fieldNumber) {
        if (node == null) {
            return null;
        }
        if ("TIFFField".equals(node.getNodeName())) {
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                Node numberAttribute = attributes.getNamedItem("number");
                if (numberAttribute != null) {
                    try {
                        if (Integer.parseInt(numberAttribute.getNodeValue()) == fieldNumber) {
                            return node;
                        }
                    } catch (Exception ignored) { CatgisLogger.warn("RasterCoverageSupport: operation failed", ignored); }
                }
            }
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            Node found = findTiffField(child, fieldNumber);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static void collectNumericValues(Node node, List<Double> values) {
        if (node == null) {
            return;
        }
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            Node valueAttribute = attributes.getNamedItem("value");
            if (valueAttribute != null) {
                try {
                    values.add(Double.parseDouble(valueAttribute.getNodeValue()));
                } catch (Exception ignored) { CatgisLogger.warn("RasterCoverageSupport: operation failed", ignored); }
            }
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            collectNumericValues(child, values);
        }
    }

    public static String resolveCoverageCrsCode(GridCoverage2D coverage, Layer fallbackLayer) {
        String coverageCode = resolveCoverageIdentifier(coverage);
        if (!coverageCode.isBlank()) {
            return coverageCode;
        }
        if (fallbackLayer != null && fallbackLayer.getSourceCRS() != null) {
            return CRSDefinitions.normalizeCode(fallbackLayer.getSourceCRS());
        }
        return "";
    }

    public static String resolveOperationalAnalysisCrsCode(GridCoverage2D coverage, Layer fallbackLayer) {
        String projectCode = CatgisDesktopApp.currentProject != null
                ? CRSDefinitions.normalizeCode(CatgisDesktopApp.currentProject.getProjectCRS())
                : "";
        if (!projectCode.isBlank()) {
            return projectCode;
        }
        return resolveCoverageCrsCode(coverage, fallbackLayer);
    }

    public static String resolveOperationalRasterCrs(LocalRasterData data, String projectCode) {
        if (data == null) {
            return "";
        }
        String sourceCode = CRSDefinitions.normalizeCode(data.getSourceCRS());
        if (!sourceCode.isBlank()) {
            return sourceCode;
        }
        String displayCode = CRSDefinitions.normalizeCode(data.getDisplayCRS());
        if (!displayCode.isBlank()) {
            return displayCode;
        }
        String projectCrs = CRSDefinitions.normalizeCode(projectCode);
        if (data.isGeoreferenced() && !projectCrs.isBlank()) {
            return projectCrs;
        }
        return "";
    }

    private static String resolveCoverageIdentifier(GridCoverage2D coverage) {
        if (coverage == null) {
            return "";
        }

        CoordinateReferenceSystem crs = null;
        try {
            crs = coverage.getCoordinateReferenceSystem2D();
        } catch (Exception ignored) { CatgisLogger.warn("RasterCoverageSupport: operation failed", ignored); }
        if (crs == null) {
            try {
                crs = coverage.getCoordinateReferenceSystem();
            } catch (Exception ignored) { CatgisLogger.warn("RasterCoverageSupport: operation failed", ignored); }
        }
        if (crs == null) {
            return "";
        }

        try {
            String identifier = CRS.toSRS(crs, true);
            String normalized = CRSDefinitions.normalizeCode(identifier);
            if (!normalized.isBlank()) {
                return normalized;
            }
        } catch (Exception ignored) { CatgisLogger.warn("RasterCoverageSupport: operation failed", ignored); }

        try {
            String identifier = crs.getName() != null ? crs.getName().toString() : "";
            return CRSDefinitions.normalizeCode(identifier);
        } catch (Exception ignored) {
            return "";
        }
    }

    public static Envelope reprojectEnvelope(Envelope envelope, String sourceCode, String targetCode) throws Exception {
        if (envelope == null || envelope.isNull()) {
            return null;
        }

        String normalizedSource = CRSDefinitions.normalizeCode(sourceCode);
        String normalizedTarget = CRSDefinitions.normalizeCode(targetCode);
        if (normalizedSource.isBlank() || normalizedTarget.isBlank() || normalizedSource.equalsIgnoreCase(normalizedTarget)) {
            return new Envelope(envelope);
        }

        Envelope manualEnvelope = CoordinateTransformSupport.reprojectEnvelope(envelope, normalizedSource, normalizedTarget);
        if (manualEnvelope != null && !manualEnvelope.isNull()) {
            return manualEnvelope;
        }

        try {
            CoordinateReferenceSystem sourceCrs = CRSDefinitions.decode(normalizedSource, true);
            CoordinateReferenceSystem targetCrs = CRSDefinitions.decode(normalizedTarget, true);
            ReferencedEnvelope referenced = new ReferencedEnvelope(
                    envelope.getMinX(),
                    envelope.getMaxX(),
                    envelope.getMinY(),
                    envelope.getMaxY(),
                    sourceCrs
            );
            ReferencedEnvelope transformed = referenced.transform(targetCrs, true);
            return new Envelope(transformed.getMinX(), transformed.getMaxX(), transformed.getMinY(), transformed.getMaxY());
        } catch (Exception ex) {
            throw ex;
        }
    }

    private static GridCoverage2D adaptCoverageToProject(Layer layer, GridCoverage2D coverage) throws IOException {
        if (coverage == null) {
            return null;
        }

        String sourceCode = resolveCoverageCrsCode(coverage, layer);
        String targetCode = CatgisDesktopApp.currentProject != null
                ? CRSDefinitions.normalizeCode(CatgisDesktopApp.currentProject.getProjectCRS())
                : "";
        if (sourceCode.isBlank() || targetCode.isBlank() || sourceCode.equalsIgnoreCase(targetCode)) {
            return coverage;
        }

        GridCoverage2D sourceAwareCoverage = assignCoverageCrsIfNeeded(coverage, sourceCode);
        return reprojectCoverage(sourceAwareCoverage, sourceCode, targetCode);
    }

    private static GridCoverage2D assignCoverageCrsIfNeeded(GridCoverage2D coverage, String sourceCode) throws IOException {
        if (coverage == null || sourceCode == null || sourceCode.isBlank()) {
            return coverage;
        }

        if (!resolveCoverageIdentifier(coverage).isBlank()) {
            return coverage;
        }

        try {
            Envelope envelope = extractCoverageEnvelope(coverage);
            if (envelope == null || envelope.isNull()) {
                return coverage;
            }
            CoordinateReferenceSystem sourceCrs = CRSDefinitions.decode(sourceCode, true);
            ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(envelope, sourceCrs);
            return new GridCoverageFactory().create("catgis-raster", coverage.getRenderedImage(), referencedEnvelope);
        } catch (Exception ex) {
            throw new IOException("No se pudo asignar el CRS operativo de la cobertura raster.", ex);
        }
    }

    public static GridCoverage2D reprojectCoverage(GridCoverage2D coverage, String sourceCode, String targetCode)
            throws IOException {
        if (coverage == null) {
            return null;
        }

        String normalizedSource = CRSDefinitions.normalizeCode(sourceCode);
        String normalizedTarget = CRSDefinitions.normalizeCode(targetCode);
        if (normalizedSource.isBlank() || normalizedTarget.isBlank() || normalizedSource.equalsIgnoreCase(normalizedTarget)) {
            return coverage;
        }

        GridCoverage2D manualCoverage = manualReprojectCoverage(coverage, normalizedSource, normalizedTarget);
        if (manualCoverage != null) {
            return manualCoverage;
        }

        try {
            CoordinateReferenceSystem targetCrs = CRSDefinitions.decode(normalizedTarget, true);
            return (GridCoverage2D) Operations.DEFAULT.resample(coverage, targetCrs);
        } catch (Exception ex) {
            throw new IOException("No se pudo adaptar el DEM al CRS del proyecto.", ex);
        }
    }

    private static Envelope extractCoverageEnvelope(GridCoverage2D coverage) {
        if (coverage == null) {
            return null;
        }
        try {
            if (coverage.getEnvelope2D() != null) {
                return new Envelope(
                        coverage.getEnvelope2D().getMinX(),
                        coverage.getEnvelope2D().getMaxX(),
                        coverage.getEnvelope2D().getMinY(),
                        coverage.getEnvelope2D().getMaxY()
                );
            }
        } catch (Exception ignored) { CatgisLogger.warn("RasterCoverageSupport: operation failed", ignored); }
        return null;
    }

    private static GridCoverage2D manualReprojectCoverage(GridCoverage2D coverage, String sourceCode, String targetCode) throws IOException {
        Envelope sourceEnvelope = extractCoverageEnvelope(coverage);
        if (coverage == null || sourceEnvelope == null || sourceEnvelope.isNull()) {
            return null;
        }

        Envelope targetEnvelope = CoordinateTransformSupport.reprojectEnvelope(sourceEnvelope, sourceCode, targetCode);
        if (targetEnvelope == null || targetEnvelope.isNull()) {
            return null;
        }

        Raster sourceRaster = coverage.getRenderedImage() != null ? coverage.getRenderedImage().getData() : null;
        if (sourceRaster == null) {
            return null;
        }

        int width = Math.max(1, sourceRaster.getWidth());
        int height = Math.max(1, sourceRaster.getHeight());
        int bandCount = Math.max(1, sourceRaster.getNumBands());

        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, bandCount);
        DataBufferFloat dataBuffer = new DataBufferFloat(width * height, bandCount);
        WritableRaster targetRaster = WritableRaster.createWritableRaster(sampleModel, dataBuffer, null);

        double sourceSpanX = sourceEnvelope.getMaxX() - sourceEnvelope.getMinX();
        double sourceSpanY = sourceEnvelope.getMaxY() - sourceEnvelope.getMinY();
        double targetSpanX = targetEnvelope.getMaxX() - targetEnvelope.getMinX();
        double targetSpanY = targetEnvelope.getMaxY() - targetEnvelope.getMinY();
        if (sourceSpanX == 0d || sourceSpanY == 0d || targetSpanX == 0d || targetSpanY == 0d) {
            return null;
        }
        int sourceMinX = sourceRaster.getMinX();
        int sourceMinY = sourceRaster.getMinY();

        boolean anySample = false;
        for (int row = 0; row < height; row++) {
            double targetY = targetEnvelope.getMaxY() - ((row + 0.5d) * targetSpanY / height);
            for (int col = 0; col < width; col++) {
                double targetX = targetEnvelope.getMinX() + ((col + 0.5d) * targetSpanX / width);
                double[] sourcePoint = CoordinateTransformSupport.transformPoint(targetX, targetY, targetCode, sourceCode);
                if (sourcePoint == null || sourcePoint.length < 2) {
                    fillNoData(targetRaster, col, row, bandCount);
                    continue;
                }
                if (sourcePoint[0] < sourceEnvelope.getMinX() || sourcePoint[0] > sourceEnvelope.getMaxX()
                        || sourcePoint[1] < sourceEnvelope.getMinY() || sourcePoint[1] > sourceEnvelope.getMaxY()) {
                    fillNoData(targetRaster, col, row, bandCount);
                    continue;
                }
                int sourceCol = clamp((int) Math.floor(((sourcePoint[0] - sourceEnvelope.getMinX()) / sourceSpanX) * width), 0, width - 1);
                int sourceRow = clamp((int) Math.floor(((sourceEnvelope.getMaxY() - sourcePoint[1]) / sourceSpanY) * height), 0, height - 1);
                for (int band = 0; band < bandCount; band++) {
                    double sample = sourceRaster.getSampleDouble(sourceMinX + sourceCol, sourceMinY + sourceRow, band);
                    targetRaster.setSample(col, row, band, sample);
                }
                anySample = true;
            }
        }

        if (!anySample) {
            return null;
        }

        try {
            CoordinateReferenceSystem targetCrs = CRSDefinitions.decode(targetCode, true);
            ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(targetEnvelope, targetCrs);
            return new GridCoverageFactory().create("catgis-raster-reprojected", targetRaster, referencedEnvelope);
        } catch (Exception ex) {
            throw new IOException("No se pudo construir la cobertura raster reproyectada.", ex);
        }
    }

    private static void fillNoData(WritableRaster raster, int col, int row, int bandCount) {
        for (int band = 0; band < bandCount; band++) {
            raster.setSample(col, row, band, Double.NaN);
        }
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}