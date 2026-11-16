package ar.com.catgis.catmap;

import ar.com.catgis.CRSDefinitions;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.api.parameter.GeneralParameterValue;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Analytical raster pipeline: floating point values, NoData and unit are kept
 * end-to-end (no 8-bit truncation). Exports GeoTIFF and ESRI ASCII grid with
 * atomic publication and independent re-open validation.
 */
public final class AnalysisRaster {

    /** Summary statistics over valid (non-NoData) cells. */
    public record Stats(String name, String unit, int width, int height,
                        long validCount, long noDataCount,
                        double min, double max, double mean, double standardDeviation) {}

    private final String name;
    private final String unit;
    private final int width;
    private final int height;
    private final float[] values;
    private final float noData;
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;
    private final String crsCode;

    public AnalysisRaster(String name, String unit, int width, int height, float[] values,
                          float noData, double minX, double minY, double maxX, double maxY,
                          String crsCode) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("AnalysisRaster requires positive dimensions.");
        }
        if (values == null || values.length != width * height) {
            throw new IllegalArgumentException("AnalysisRaster values do not match its size.");
        }
        if (!(maxX > minX) || !(maxY > minY)
                || !Double.isFinite(minX) || !Double.isFinite(minY)
                || !Double.isFinite(maxX) || !Double.isFinite(maxY)) {
            throw new IllegalArgumentException("AnalysisRaster extent is invalid.");
        }
        if (crsCode == null || crsCode.isBlank()) {
            throw new IllegalArgumentException("AnalysisRaster requires an explicit CRS.");
        }
        this.name = name == null || name.isBlank() ? "analisis" : name.trim();
        this.unit = unit == null ? "" : unit.trim();
        this.width = width;
        this.height = height;
        this.values = values.clone();
        this.noData = noData;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.crsCode = crsCode.trim();
    }

    /** Empty raster with the same geometry (used by the terrain algorithms). */
    public AnalysisRaster emptyLike(String newName, String newUnit) {
        float[] blank = new float[width * height];
        java.util.Arrays.fill(blank, noData);
        return new AnalysisRaster(newName, newUnit, width, height, blank, noData,
                minX, minY, maxX, maxY, crsCode);
    }

    public String name() {
        return name;
    }

    public String unit() {
        return unit;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public float noData() {
        return noData;
    }

    public String crsCode() {
        return crsCode;
    }

    public double minX() {
        return minX;
    }

    public double minY() {
        return minY;
    }

    public double maxX() {
        return maxX;
    }

    public double maxY() {
        return maxY;
    }

    public double cellSizeX() {
        return (maxX - minX) / width;
    }

    public double cellSizeY() {
        return (maxY - minY) / height;
    }

    public boolean isNoData(int x, int y) {
        return Float.isNaN(values[index(x, y)]) || values[index(x, y)] == noData;
    }

    public float valueAt(int x, int y) {
        return values[index(x, y)];
    }

    public float[] copyValues() {
        return values.clone();
    }

    public Stats stats() {
        long valid = 0;
        long invalid = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0;
        for (float value : values) {
            if (Float.isNaN(value) || value == noData) {
                invalid++;
                continue;
            }
            valid++;
            min = Math.min(min, value);
            max = Math.max(max, value);
            sum += value;
        }
        double mean = valid == 0 ? Double.NaN : sum / valid;
        double variance = 0;
        if (valid > 0) {
            for (float value : values) {
                if (Float.isNaN(value) || value == noData) {
                    continue;
                }
                double delta = value - mean;
                variance += delta * delta;
            }
            variance /= valid;
        }
        return new Stats(name, unit, width, height, valid, invalid,
                valid == 0 ? Double.NaN : min, valid == 0 ? Double.NaN : max,
                mean, valid == 0 ? Double.NaN : Math.sqrt(variance));
    }

    /** Georeferenced GeoTIFF preserving float values and NoData. */
    public void exportGeoTiff(File file) throws IOException {
        Objects.requireNonNull(file, "AnalysisRaster GeoTIFF destination");
        try {
            CatmapAtomicExport.publish(file,
                    staged -> writeGeoTiff(staged.toFile()),
                    staged -> verifyGeoTiff(staged));
        } catch (IOException io) {
            throw io;
        } catch (Exception failure) {
            throw new IOException("AnalysisRaster GeoTIFF export failed.", failure);
        }
    }

    /** ESRI ASCII grid preserving float values and NoData. */
    public void exportAsciiGrid(File file) throws IOException {
        Objects.requireNonNull(file, "AnalysisRaster ASCII destination");
        try {
            CatmapAtomicExport.publish(file,
                    staged -> writeAsciiGrid(staged),
                    AnalysisRaster::verifyAsciiGrid);
        } catch (IOException io) {
            throw io;
        } catch (Exception failure) {
            throw new IOException("AnalysisRaster ASCII export failed.", failure);
        }
    }

    private void writeGeoTiff(File file) throws IOException {
        BandedSampleModel sampleModel = new BandedSampleModel(
                java.awt.image.DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.setSample(x, y, 0, values[index(x, y)]);
            }
        }
        CoordinateReferenceSystem crs;
        try {
            crs = CRSDefinitions.decode(CRSDefinitions.normalizeCode(crsCode), true);
        } catch (Exception invalid) {
            throw new IOException("AnalysisRaster CRS is not decodable: " + crsCode, invalid);
        }
        ReferencedEnvelope envelope = new ReferencedEnvelope(minX, maxX, minY, maxY, crs);
        GridCoverage2D coverage = new GridCoverageFactory().create(name, raster, envelope);
        try {
            GeoTiffWriter writer = new GeoTiffWriter(file);
            try {
                writer.write(coverage, (GeneralParameterValue[]) null);
            } catch (IOException io) {
                throw io;
            } catch (Exception failure) {
                throw new IOException("AnalysisRaster GeoTIFF could not be written.", failure);
            } finally {
                writer.dispose();
            }
        } finally {
            coverage.dispose(true);
        }
    }

    private void writeAsciiGrid(Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("ncols " + width + "\n");
            writer.write("nrows " + height + "\n");
            writer.write("xllcorner " + format(minX) + "\n");
            writer.write("yllcorner " + format(minY) + "\n");
            writer.write("cellsize " + format(cellSizeX()) + "\n");
            writer.write("NODATA_value " + format(noData) + "\n");
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (x > 0) {
                        writer.write(' ');
                    }
                    writer.write(format(values[index(x, y)]));
                }
                writer.write('\n');
            }
        }
    }

    private void verifyGeoTiff(Path staged) throws IOException {
        GeoTiffReader reader = null;
        GridCoverage2D coverage = null;
        java.io.InputStream input = null;
        try {
            input = Files.newInputStream(staged);
            reader = new GeoTiffReader(input);
            coverage = reader.read(null);
            if (coverage == null) {
                throw new IOException("AnalysisRaster GeoTIFF is unreadable.");
            }
            if (!org.geotools.referencing.CRS.equalsIgnoreMetadata(
                    reader.getCoordinateReferenceSystem(),
                    CRSDefinitions.decode(CRSDefinitions.normalizeCode(crsCode), true))) {
                throw new IOException("AnalysisRaster GeoTIFF lost the CRS.");
            }
            ReferencedEnvelope actual = new ReferencedEnvelope(
                    coverage.getEnvelope2D(), reader.getCoordinateReferenceSystem());
            double toleranceX = Math.max(1.0e-6, Math.abs(maxX - minX) * 1.0e-6);
            double toleranceY = Math.max(1.0e-6, Math.abs(maxY - minY) * 1.0e-6);
            if (Math.abs(actual.getMinX() - minX) > toleranceX
                    || Math.abs(actual.getMaxX() - maxX) > toleranceX
                    || Math.abs(actual.getMinY() - minY) > toleranceY
                    || Math.abs(actual.getMaxY() - maxY) > toleranceY) {
                throw new IOException("AnalysisRaster GeoTIFF lost its extent.");
            }
            if (coverage.getRenderedImage().getWidth() != width
                    || coverage.getRenderedImage().getHeight() != height) {
                throw new IOException("AnalysisRaster GeoTIFF has unexpected dimensions.");
            }
        } catch (IOException io) {
            throw io;
        } catch (Exception failure) {
            throw new IOException("AnalysisRaster GeoTIFF validation failed.", failure);
        } finally {
            if (coverage != null) {
                coverage.dispose(true);
            }
            if (reader != null) {
                reader.dispose();
            }
            if (input != null) {
                input.close();
            }
        }
    }

    private static void verifyAsciiGrid(Path staged) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(staged, StandardCharsets.UTF_8)) {
            int ncols = -1;
            int nrows = -1;
            for (int header = 0; header < 6; header++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("AnalysisRaster ASCII grid header is incomplete.");
                }
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 2) {
                    throw new IOException("AnalysisRaster ASCII grid header is malformed.");
                }
                switch (parts[0].toLowerCase(Locale.ROOT)) {
                    case "ncols" -> ncols = Integer.parseInt(parts[1]);
                    case "nrows" -> nrows = Integer.parseInt(parts[1]);
                    default -> {
                        // xllcorner/yllcorner/cellsize/NODATA_value are validated by count below.
                    }
                }
            }
            if (ncols <= 0 || nrows <= 0) {
                throw new IOException("AnalysisRaster ASCII grid lacks dimensions.");
            }
            long count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    count += line.trim().split("\\s+").length;
                }
            }
            if (count != (long) ncols * nrows) {
                throw new IOException("AnalysisRaster ASCII grid lost values: " + count);
            }
        } catch (NumberFormatException invalid) {
            throw new IOException("AnalysisRaster ASCII grid has invalid numbers.", invalid);
        }
    }

    private int index(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("cell outside AnalysisRaster: " + x + "," + y);
        }
        return y * width + x;
    }

    private static String format(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1.0e9) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.6f", value);
    }

    private static String format(float value) {
        if (Float.isNaN(value)) {
            return "NaN";
        }
        if (value == Math.rint(value) && Math.abs(value) < 1.0e9) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.6f", value);
    }
}
