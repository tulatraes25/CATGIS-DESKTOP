package ar.com.catgis;

import org.locationtech.jts.geom.Envelope;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Properties;

public final class RasterSidecarSupport {

    private static final String VERSION = "1";
    private static final String EXTENSION = ".catgis-raster.properties";
    // Version 1 contains only a few scalar fields. Bound bytes BEFORE parsing.
    static final int MAX_SIDECAR_BYTES = 64 * 1024;

    private RasterSidecarSupport() {
    }

    public static void write(java.io.File rasterFile, Envelope envelope, String sourceCrsCode) throws IOException {
        if (rasterFile == null || envelope == null || envelope.isNull()) {
            return;
        }
        // Snapshot and validate before mkdirs or truncating any previous sidecar.
        // JTS may reorder constructor arguments; serialized min/max are checked
        // independently in read() before constructing that Envelope.
        double minX = envelope.getMinX();
        double maxX = envelope.getMaxX();
        double minY = envelope.getMinY();
        double maxY = envelope.getMaxY();
        requireFiniteExtent(minX, maxX, minY, maxY);
        String sourceCrs = requireDeclaredCrs(sourceCrsCode);

        Properties properties = new Properties();
        properties.setProperty("version", VERSION);
        properties.setProperty("sourceCrs", sourceCrs);
        properties.setProperty("minX", Double.toString(minX));
        properties.setProperty("maxX", Double.toString(maxX));
        properties.setProperty("minY", Double.toString(minY));
        properties.setProperty("maxY", Double.toString(maxY));

        // Validate encoded size too: every successfully written sidecar must
        // satisfy the reader's byte budget, including Properties escaping.
        java.io.ByteArrayOutputStream encoded = new java.io.ByteArrayOutputStream();
        properties.store(encoded, "CATGIS raster sidecar metadata");
        byte[] bytes = encoded.toByteArray();
        if (bytes.length > MAX_SIDECAR_BYTES) {
            throw new IOException("La metadata raster excede el limite de 64 KiB.");
        }
        java.io.File sidecar = sidecarFile(rasterFile);
        java.io.File parent = sidecar.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new IOException("No se pudo crear la carpeta de metadata raster.");
        }
        // Atomic publication of raster plus sidecar is a separate contract.
        try (OutputStream output = Files.newOutputStream(sidecar.toPath())) {
            output.write(bytes);
        }
    }

    public static Metadata read(java.io.File rasterFile) {
        if (rasterFile == null) {
            return null;
        }
        java.io.File sidecar = sidecarFile(rasterFile);
        if (!sidecar.exists() || !sidecar.isFile()) {
            return null;
        }

        try {
            byte[] bytes;
            try (InputStream input = Files.newInputStream(sidecar.toPath())) {
                bytes = input.readNBytes(MAX_SIDECAR_BYTES + 1);
            }
            if (bytes.length == 0 || bytes.length > MAX_SIDECAR_BYTES) {
                return null;
            }
            Properties properties = new Properties();
            try (InputStream input = new ByteArrayInputStream(bytes)) {
                // Retain the existing Properties byte encoding and escaping.
                properties.load(input);
            }
            if (!VERSION.equals(properties.getProperty("version", "").trim())) {
                return null;
            }
            double minX = Double.parseDouble(properties.getProperty("minX", "NaN"));
            double maxX = Double.parseDouble(properties.getProperty("maxX", "NaN"));
            double minY = Double.parseDouble(properties.getProperty("minY", "NaN"));
            double maxY = Double.parseDouble(properties.getProperty("maxY", "NaN"));
            requireFiniteExtent(minX, maxX, minY, maxY);
            String sourceCrs = requireDeclaredCrs(properties.getProperty("sourceCrs"));
            return new Metadata(new Envelope(minX, maxX, minY, maxY), sourceCrs, sidecar);
        } catch (Exception ex) {
            // Existing callers treat malformed/unavailable optional metadata as
            // absent. Do not replace unknown values by plausible spatial facts.
            return null;
        }
    }

    private static void requireFiniteExtent(double minX, double maxX, double minY, double maxY)
            throws IOException {
        if (!Double.isFinite(minX) || !Double.isFinite(maxX)
                || !Double.isFinite(minY) || !Double.isFinite(maxY)
                || maxX <= minX || maxY <= minY
                || !Double.isFinite(maxX - minX) || !Double.isFinite(maxY - minY)) {
            throw new IOException("La metadata raster requiere limites finitos, ordenados "
                    + "y una extension positiva y finita.");
        }
    }

    private static String requireDeclaredCrs(String sourceCrs) throws IOException {
        if (sourceCrs == null || sourceCrs.isBlank() || sourceCrs.length() > MAX_SIDECAR_BYTES) {
            throw new IOException("La metadata raster requiere un CRS declarado dentro del limite permitido.");
        }
        // Normalization is not CRS decoding or proof of georeferencing. Never
        // call it with absent values: that API deliberately defaults to WGS84.
        return CRSDefinitions.normalizeCode(sourceCrs);
    }

    static java.io.File sidecarFile(java.io.File rasterFile) {
        String absolutePath = rasterFile.getAbsolutePath();
        return new java.io.File(absolutePath + EXTENSION);
    }

    public record Metadata(Envelope envelope, String sourceCrs, java.io.File sidecarFile) {
        @Override
        public String toString() {
            return String.format(Locale.US, "%s [%s]", sidecarFile.getName(), sourceCrs);
        }
    }
}
