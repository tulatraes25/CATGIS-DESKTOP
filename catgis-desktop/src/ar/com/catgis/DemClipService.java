package ar.com.catgis;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;

public final class DemClipService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private DemClipService() {
    }

    public static ClipResult clipDem(Layer rasterLayer,
                                     Envelope requestedEnvelope,
                                     Geometry maskGeometry,
                                     String outputLayerName,
                                     File outputFile) throws Exception {
        if (!(rasterLayer instanceof RasterLayer)) {
            throw new IllegalArgumentException("Debes elegir un DEM raster valido.");
        }
        if (requestedEnvelope == null || requestedEnvelope.isNull()) {
            throw new IllegalArgumentException("Debes indicar un area de recorte valida.");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("Debes indicar un archivo de salida para el DEM recortado.");
        }

        GridCoverage2D coverage = RasterCoverageSupport.readCoverage(rasterLayer);
        if (coverage == null || coverage.getRenderedImage() == null || coverage.getEnvelope2D() == null) {
            throw new IllegalStateException("No se pudo leer la cobertura raster del DEM.");
        }

        Envelope sourceEnvelope = new Envelope(
                coverage.getEnvelope2D().getMinX(),
                coverage.getEnvelope2D().getMaxX(),
                coverage.getEnvelope2D().getMinY(),
                coverage.getEnvelope2D().getMaxY()
        );
        Envelope intersection = sourceEnvelope.intersection(requestedEnvelope);
        if (intersection == null || intersection.isNull() || intersection.getWidth() <= 0d || intersection.getHeight() <= 0d) {
            throw new IllegalStateException("El area de recorte no intersecta el DEM seleccionado.");
        }

        Raster sourceRaster = coverage.getRenderedImage().getData();
        int sourceWidth = Math.max(1, sourceRaster.getWidth());
        int sourceHeight = Math.max(1, sourceRaster.getHeight());
        int sourceMinX = sourceRaster.getMinX();
        int sourceMinY = sourceRaster.getMinY();
        int bandCount = Math.max(1, sourceRaster.getNumBands());

        double cellWidth = sourceEnvelope.getWidth() / sourceWidth;
        double cellHeight = sourceEnvelope.getHeight() / sourceHeight;
        if (!(cellWidth > 0d) || !(cellHeight > 0d)) {
            throw new IllegalStateException("No se pudo calcular el tamano de pixel del DEM.");
        }

        int startCol = clamp((int) Math.floor((intersection.getMinX() - sourceEnvelope.getMinX()) / cellWidth), 0, sourceWidth - 1);
        int endCol = clamp((int) Math.ceil((intersection.getMaxX() - sourceEnvelope.getMinX()) / cellWidth), startCol + 1, sourceWidth);
        int startRow = clamp((int) Math.floor((sourceEnvelope.getMaxY() - intersection.getMaxY()) / cellHeight), 0, sourceHeight - 1);
        int endRow = clamp((int) Math.ceil((sourceEnvelope.getMaxY() - intersection.getMinY()) / cellHeight), startRow + 1, sourceHeight);

        int clippedWidth = Math.max(1, endCol - startCol);
        int clippedHeight = Math.max(1, endRow - startRow);
        Envelope clippedEnvelope = new Envelope(
                sourceEnvelope.getMinX() + (startCol * cellWidth),
                sourceEnvelope.getMinX() + (endCol * cellWidth),
                sourceEnvelope.getMaxY() - (endRow * cellHeight),
                sourceEnvelope.getMaxY() - (startRow * cellHeight)
        );

        Geometry effectiveMask = null;
        if (maskGeometry != null && !maskGeometry.isEmpty()) {
            effectiveMask = maskGeometry.intersection(GEOMETRY_FACTORY.toGeometry(clippedEnvelope));
            if (effectiveMask == null || effectiveMask.isEmpty()) {
                throw new IllegalStateException("La mascara poligonal no intersecta el DEM dentro del area de recorte.");
            }
        }

        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, clippedWidth, clippedHeight, bandCount);
        DataBufferFloat buffer = new DataBufferFloat(clippedWidth * clippedHeight, bandCount);
        WritableRaster clippedRaster = WritableRaster.createWritableRaster(sampleModel, buffer, null);

        int validPixels = 0;
        for (int row = 0; row < clippedHeight; row++) {
            double worldY = clippedEnvelope.getMaxY() - ((row + 0.5d) * cellHeight);
            int sourceRow = startRow + row;
            for (int col = 0; col < clippedWidth; col++) {
                double worldX = clippedEnvelope.getMinX() + ((col + 0.5d) * cellWidth);
                if (!isInsideMask(effectiveMask, worldX, worldY)) {
                    fillNoData(clippedRaster, col, row, bandCount);
                    continue;
                }
                int sourceCol = startCol + col;
                boolean validSample = false;
                for (int band = 0; band < bandCount; band++) {
                    double sample = sourceRaster.getSampleDouble(sourceMinX + sourceCol, sourceMinY + sourceRow, band);
                    clippedRaster.setSample(col, row, band, sample);
                    validSample = validSample || Double.isFinite(sample);
                }
                if (validSample) {
                    validPixels++;
                }
            }
        }

        if (validPixels == 0) {
            throw new IllegalStateException("El recorte dejo el DEM sin pixeles validos. Ajusta el area o la mascara.");
        }

        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new IllegalStateException("No se pudo crear la carpeta de salida para el DEM recortado.");
        }

        String targetCrs = RasterCoverageSupport.resolveOperationalAnalysisCrsCode(coverage, rasterLayer);
        CoordinateReferenceSystem crs = CRSDefinitions.decode(targetCrs, true);
        ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(clippedEnvelope, crs);
        GridCoverage2D clippedCoverage = new GridCoverageFactory().create(
                outputLayerName != null && !outputLayerName.isBlank() ? outputLayerName.trim() : "dem_clip",
                clippedRaster,
                referencedEnvelope
        );

        GeoTiffWriter writer = new GeoTiffWriter(outputFile);
        try {
            writer.write(clippedCoverage, (GeneralParameterValue[]) null);
        } finally {
            writer.dispose();
        }
        RasterSidecarSupport.write(outputFile, clippedEnvelope, targetCrs);

        String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : targetCrs;
        LocalRasterData rasterData = RasterImageLoader.loadReal(outputFile, projectCrs, targetCrs);
        RasterLayer resultLayer = new RasterLayer(
                outputLayerName != null && !outputLayerName.isBlank() ? outputLayerName.trim() : "DEM recortado",
                outputFile.getAbsolutePath()
        );
        resultLayer.setVisible(true);
        resultLayer.setSourceName("DEM recortado desde " + rasterLayer.getName());
        resultLayer.setFeatureCount(1);
        resultLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(rasterData, projectCrs));
        resultLayer.setRasterMode(rasterData.getRasterMode());

        return new ClipResult(
                resultLayer,
                rasterData,
                outputFile,
                clippedEnvelope,
                clippedWidth,
                clippedHeight,
                cellWidth,
                cellHeight,
                validPixels
        );
    }

    private static boolean isInsideMask(Geometry mask, double x, double y) {
        if (mask == null) {
            return true;
        }
        Point point = GEOMETRY_FACTORY.createPoint(new org.locationtech.jts.geom.Coordinate(x, y));
        return mask.covers(point);
    }

    private static void fillNoData(WritableRaster raster, int col, int row, int bandCount) {
        for (int band = 0; band < bandCount; band++) {
            raster.setSample(col, row, band, Float.NaN);
        }
    }

    private static int clamp(int value, int min, int maxExclusive) {
        if (value < min) {
            return min;
        }
        if (value > maxExclusive) {
            return maxExclusive;
        }
        return value;
    }

    public record ClipResult(RasterLayer layer,
                             LocalRasterData rasterData,
                             File outputFile,
                             Envelope envelope,
                             int width,
                             int height,
                             double pixelWidthWorld,
                             double pixelHeightWorld,
                             int validPixels) {
    }
}
