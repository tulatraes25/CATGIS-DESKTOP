package ar.com.catgis;

import org.geotools.api.coverage.grid.GridCoverageReader;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;

import java.io.File;
import java.io.IOException;

public final class RasterCoverageSupport {

    private RasterCoverageSupport() {
    }

    public static GridCoverage2D readCoverage(Layer layer) throws IOException {
        if (layer == null || layer.getPath() == null || layer.getPath().isBlank()) {
            throw new IOException("La capa raster seleccionada no tiene una ruta de archivo valida.");
        }
        return readCoverage(new File(layer.getPath()));
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
            return (GridCoverage2D) reader.read((GeneralParameterValue[]) null);
        } finally {
            if (reader != null) {
                try {
                    reader.dispose();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static String resolveCoverageCrsCode(GridCoverage2D coverage, Layer fallbackLayer) {
        if (coverage != null && coverage.getCoordinateReferenceSystem2D() != null) {
            try {
                return CRSDefinitions.normalizeCode(CRS.toSRS(coverage.getCoordinateReferenceSystem2D(), true));
            } catch (Exception ignored) {
            }
        }
        if (fallbackLayer != null && fallbackLayer.getSourceCRS() != null) {
            return CRSDefinitions.normalizeCode(fallbackLayer.getSourceCRS());
        }
        return "";
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

        CoordinateReferenceSystem sourceCrs = CRS.decode(normalizedSource, true);
        CoordinateReferenceSystem targetCrs = CRS.decode(normalizedTarget, true);
        ReferencedEnvelope referenced = new ReferencedEnvelope(
                envelope.getMinX(),
                envelope.getMaxX(),
                envelope.getMinY(),
                envelope.getMaxY(),
                sourceCrs
        );
        ReferencedEnvelope transformed = referenced.transform(targetCrs, true);
        return new Envelope(transformed.getMinX(), transformed.getMaxX(), transformed.getMinY(), transformed.getMaxY());
    }
}
