package ar.com.catgis;

import org.locationtech.jts.geom.Envelope;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Properties;

final class RasterSidecarSupport {

    private static final String VERSION = "1";
    private static final String EXTENSION = ".catgis-raster.properties";

    private RasterSidecarSupport() {
    }

    static void write(java.io.File rasterFile, Envelope envelope, String sourceCrsCode) throws IOException {
        if (rasterFile == null || envelope == null || envelope.isNull()) {
            return;
        }
        java.io.File sidecar = sidecarFile(rasterFile);
        java.io.File parent = sidecar.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new IOException("No se pudo crear la carpeta de metadata raster.");
        }

        Properties properties = new Properties();
        properties.setProperty("version", VERSION);
        properties.setProperty("sourceCrs", CRSDefinitions.normalizeCode(sourceCrsCode));
        properties.setProperty("minX", Double.toString(envelope.getMinX()));
        properties.setProperty("maxX", Double.toString(envelope.getMaxX()));
        properties.setProperty("minY", Double.toString(envelope.getMinY()));
        properties.setProperty("maxY", Double.toString(envelope.getMaxY()));

        try (OutputStream output = Files.newOutputStream(sidecar.toPath())) {
            properties.store(output, "CATGIS raster sidecar metadata");
        }
    }

    static Metadata read(java.io.File rasterFile) {
        if (rasterFile == null) {
            return null;
        }
        java.io.File sidecar = sidecarFile(rasterFile);
        if (!sidecar.exists() || !sidecar.isFile()) {
            return null;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(sidecar.toPath())) {
            properties.load(input);
        } catch (Exception ex) {
            return null;
        }

        String version = properties.getProperty("version", "").trim();
        if (!VERSION.equals(version)) {
            return null;
        }

        try {
            double minX = Double.parseDouble(properties.getProperty("minX", "NaN"));
            double maxX = Double.parseDouble(properties.getProperty("maxX", "NaN"));
            double minY = Double.parseDouble(properties.getProperty("minY", "NaN"));
            double maxY = Double.parseDouble(properties.getProperty("maxY", "NaN"));
            Envelope envelope = new Envelope(minX, maxX, minY, maxY);
            if (envelope.isNull()) {
                return null;
            }
            String sourceCrs = CRSDefinitions.normalizeCode(properties.getProperty("sourceCrs", ""));
            return new Metadata(envelope, sourceCrs, sidecar);
        } catch (Exception ex) {
            return null;
        }
    }

    static java.io.File sidecarFile(java.io.File rasterFile) {
        String absolutePath = rasterFile.getAbsolutePath();
        return new java.io.File(absolutePath + EXTENSION);
    }

    record Metadata(Envelope envelope, String sourceCrs, java.io.File sidecarFile) {
        @Override
        public String toString() {
            return String.format(Locale.US, "%s [%s]", sidecarFile.getName(), sourceCrs);
        }
    }
}
