package ar.com.catgis;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.processing.Operations;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;

public class RasterImageLoader {

    public static final String MODE_PREVIEW = "preview";
    public static final String MODE_VIRTUAL = "virtual";
    public static final String MODE_REAL = "real";

    private static final int PREVIEW_SIZE = 2048;
    private static final int VIRTUAL_SIZE = 4096;
    private static final int REAL_SIZE = 8192;

    private static final File CACHE_DIR = new File(System.getProperty("java.io.tmpdir"), "catgis-raster-cache");

    private RasterImageLoader() {
    }

    public static LocalRasterData load(File file) throws IOException {
        return loadPreview(file);
    }

    public static LocalRasterData loadPreview(File file) throws IOException {
        return loadPreview(file, null, null);
    }

    public static LocalRasterData loadPreview(File file, String projectCRS, String forcedSourceCRS) throws IOException {
        return loadInternal(file, MODE_PREVIEW, projectCRS, forcedSourceCRS);
    }

    public static LocalRasterData loadVirtual(File file) throws IOException {
        return loadVirtual(file, null, null);
    }

    public static LocalRasterData loadVirtual(File file, String projectCRS, String forcedSourceCRS) throws IOException {
        return loadInternal(file, MODE_VIRTUAL, projectCRS, forcedSourceCRS);
    }

    public static LocalRasterData loadHighResolution(File file) throws IOException {
        return loadReal(file);
    }

    public static LocalRasterData loadReal(File file) throws IOException {
        return loadReal(file, null, null);
    }

    public static LocalRasterData loadReal(File file, String projectCRS, String forcedSourceCRS) throws IOException {
        return loadInternal(file, MODE_REAL, projectCRS, forcedSourceCRS);
    }

    public static LocalRasterData loadRealViewport(File file, Envelope requestEnv, int viewportW, int viewportH) throws IOException {
        if (file == null) {
            throw new IOException("Archivo raster nulo.");
        }
        if (!file.exists()) {
            throw new IOException("No existe el archivo: " + file.getAbsolutePath());
        }

        String lowerName = file.getName().toLowerCase(Locale.ROOT);

        int targetSize = Math.max(REAL_SIZE, Math.max(viewportW, viewportH));
        if (targetSize <= 0) {
            targetSize = REAL_SIZE;
        }
        targetSize = Math.min(targetSize, 12000);

        if (isImg(lowerName)) {
            File cacheTif = getOrCreateImgCache(file, targetSize, MODE_REAL + "_viewport");
            LocalRasterData data = loadWithGeoToolsSafe(cacheTif, targetSize, file.getAbsolutePath(), MODE_REAL, null, null);
            if (requestEnv != null) {
                return new LocalRasterData(
                        data.getImage(),
                        requestEnv,
                        data.getBandCount(),
                        data.isGeoreferenced(),
                        data.getSourceCRS(),
                        MODE_REAL,
                        data.getDisplayCRS()
                );
            }
            return data;
        }

        LocalRasterData data = loadWithGeoToolsSafe(file, targetSize, file.getAbsolutePath(), MODE_REAL, null, null);
        if (requestEnv != null) {
            return new LocalRasterData(
                    data.getImage(),
                    requestEnv,
                    data.getBandCount(),
                    data.isGeoreferenced(),
                    data.getSourceCRS(),
                    MODE_REAL,
                    data.getDisplayCRS()
            );
        }
        return data;
    }

    private static LocalRasterData loadInternal(File file, String mode, String projectCRS, String forcedSourceCRS) throws IOException {
        if (file == null) {
            throw new IOException("Archivo raster nulo.");
        }
        if (!file.exists()) {
            throw new IOException("No existe el archivo: " + file.getAbsolutePath());
        }

        String lowerName = file.getName().toLowerCase(Locale.ROOT);
        int displaySize = getDisplaySizeForMode(mode);

        if (isImg(lowerName)) {
            try {
                LocalRasterData direct = loadWithGeoTools(file, displaySize, mode, projectCRS, forcedSourceCRS);
                if (direct != null) {
                    return direct;
                }
            } catch (Exception ignored) {
            }

            File cacheTif = getOrCreateImgCache(file, displaySize, mode);
            return loadWithGeoToolsSafe(cacheTif, displaySize, file.getAbsolutePath(), mode, projectCRS, forcedSourceCRS);
        }

        if (isGeoToolsRaster(lowerName)) {
            return loadWithGeoToolsSafe(file, displaySize, file.getAbsolutePath(), mode, projectCRS, forcedSourceCRS);
        }

        BufferedImage raw = ImageIO.read(file);
        if (raw == null) {
            throw new IOException("No se pudo leer el archivo raster: " + file.getAbsolutePath());
        }

        int bandCount = raw.getRaster().getNumBands();
        BufferedImage displayImage = prepareDisplayImage(raw, displaySize);
        Envelope env = new Envelope(0, raw.getWidth(), 0, raw.getHeight());

        return new LocalRasterData(displayImage, env, bandCount, false, "", mode, "");
    }

    private static int getDisplaySizeForMode(String mode) {
        if (MODE_REAL.equalsIgnoreCase(mode)) {
            return REAL_SIZE;
        }
        if (MODE_VIRTUAL.equalsIgnoreCase(mode)) {
            return VIRTUAL_SIZE;
        }
        return PREVIEW_SIZE;
    }

    private static LocalRasterData loadWithGeoToolsSafe(
            File fileToRead,
            int targetDisplaySize,
            String originalPath,
            String mode,
            String projectCRS,
            String forcedSourceCRS) throws IOException {
        try {
            LocalRasterData data = loadWithGeoTools(fileToRead, targetDisplaySize, mode, projectCRS, forcedSourceCRS);
            if (data != null) {
                return data;
            }
        } catch (Exception ex) {
            throw new IOException(buildDiagnosticMessage(originalPath, fileToRead, ex), ex);
        }
        throw new IOException("No se pudo leer el archivo raster:\n" + originalPath);
    }

    private static LocalRasterData loadWithGeoTools(
            File file,
            int targetDisplaySize,
            String mode,
            String projectCRS,
            String forcedSourceCRS) throws Exception {
        AbstractGridFormat format = GridFormatFinder.findFormat(file);

        if (format == null) {
            throw new IOException("GeoTools no encontro formato para: " + file.getName());
        }

        String formatClass = format.getClass().getName().toLowerCase(Locale.ROOT);
        if (formatClass.contains("unknown")) {
            throw new IOException("GeoTools devolvio formato desconocido para: " + file.getName()
                    + " [" + format.getClass().getName() + "]");
        }

        AbstractGridCoverage2DReader reader = null;
        try {
            reader = (AbstractGridCoverage2DReader) format.getReader(file);
            if (reader == null) {
                throw new IOException("GeoTools encontro formato pero no pudo crear reader para: " + file.getName()
                        + " [" + format.getClass().getName() + "]");
            }

            GridCoverage2D coverage = (GridCoverage2D) reader.read((org.geotools.api.parameter.GeneralParameterValue[]) null);
            if (coverage == null || coverage.getRenderedImage() == null) {
                throw new IOException("GeoTools creo reader pero no devolvio cobertura valida para: " + file.getName());
            }

            BufferedImage originalRaw = renderedToBuffered(coverage);
            Envelope originalEnvelope = extractEnvelopeByReflection(coverage, originalRaw);
            String extractedSourceCRS = extractSourceCRS(coverage, reader);
            String requestedSourceCRS = CRSDefinitions.normalizeCode(forcedSourceCRS);
            String effectiveSourceCRS = pickUsableSourceCRS(requestedSourceCRS, extractedSourceCRS);
            String targetCRS = CRSDefinitions.normalizeCode(projectCRS);
            GridCoverage2D sourceAwareCoverage = assignSourceCRSIfNeeded(coverage, originalEnvelope, effectiveSourceCRS);
            GridCoverage2D displayCoverage = reprojectCoverageIfNeeded(sourceAwareCoverage, effectiveSourceCRS, targetCRS);

            BufferedImage raw = renderedToBuffered(displayCoverage);
            int bandCount = displayCoverage.getRenderedImage() != null && displayCoverage.getRenderedImage().getSampleModel() != null
                    ? displayCoverage.getRenderedImage().getSampleModel().getNumBands()
                    : raw.getRaster().getNumBands();
            BufferedImage displayImage = prepareDisplayImage(raw, targetDisplaySize);
            Envelope env = extractEnvelopeByReflection(displayCoverage, raw);
            String displayCRS = displayCoverage != sourceAwareCoverage && !targetCRS.isBlank()
                    ? targetCRS
                    : effectiveSourceCRS;

            return new LocalRasterData(displayImage, env, bandCount, true, effectiveSourceCRS, mode, displayCRS);
        } finally {
            try {
                if (reader != null) {
                    reader.dispose();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static boolean isImg(String name) {
        return name.endsWith(".img");
    }

    private static boolean isTiff(String name) {
        return name.endsWith(".tif") || name.endsWith(".tiff");
    }

    private static boolean isGeoToolsRaster(String name) {
        return isTiff(name)
                || name.endsWith(".asc")
                || name.endsWith(".adf")
                || name.endsWith(".grd")
                || name.endsWith(".bil")
                || name.endsWith(".flt");
    }

    private static File getOrCreateImgCache(File imgFile, int outsize, String mode) throws IOException {
        if (!CACHE_DIR.exists() && !CACHE_DIR.mkdirs() && !CACHE_DIR.exists()) {
            throw new IOException("No se pudo crear directorio de cache raster: " + CACHE_DIR.getAbsolutePath());
        }

        String key = buildCacheKey(imgFile) + "_" + mode;
        File tif = new File(CACHE_DIR, key + ".tif");

        if (tif.exists() && tif.length() > 0L && tif.lastModified() >= imgFile.lastModified()) {
            return tif;
        }

        File tmp = new File(CACHE_DIR, key + ".tmp.tif");
        if (tmp.exists()) {
            tmp.delete();
        }

        String gdalTranslate = findExecutable("gdal_translate.exe", "gdal_translate");
        ProcessBuilder pb = new ProcessBuilder(
                gdalTranslate,
                "-of", "GTiff",
                "-outsize", Integer.toString(outsize), "0",
                "-co", "COMPRESS=LZW",
                "-co", "TILED=YES",
                imgFile.getAbsolutePath(),
                tmp.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        Process p;
        String output;
        try {
            p = pb.start();
            output = readAll(p.getInputStream());
            int code = p.waitFor();
            if (code != 0) {
                throw new IOException("gdal_translate devolvio codigo " + code + ". Salida:\n" + output);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Conversion raster interrumpida.", ie);
        }

        if (!tmp.exists() || tmp.length() == 0L) {
            throw new IOException("gdal_translate termino pero no genero el TIFF esperado.");
        }

        if (tif.exists() && !tif.delete()) {
            throw new IOException("No se pudo reemplazar el cache raster: " + tif.getAbsolutePath());
        }
        if (!tmp.renameTo(tif)) {
            throw new IOException("No se pudo renombrar el TIFF temporal a cache final.");
        }

        buildOverviewsIfPossible(tif);
        tif.setLastModified(Math.max(tif.lastModified(), imgFile.lastModified()));
        return tif;
    }

    private static void buildOverviewsIfPossible(File tif) {
        try {
            String gdalAddo = findExecutable("gdaladdo.exe", "gdaladdo");
            ProcessBuilder pb = new ProcessBuilder(
                    gdalAddo,
                    "-r", "average",
                    tif.getAbsolutePath(),
                    "2", "4", "8", "16"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            readAll(p.getInputStream());
            p.waitFor();
        } catch (Exception ignored) {
        }
    }

    private static String findExecutable(String exeName, String fallback) {
        File osgeo = new File("C:\\OSGeo4W\\bin\\" + exeName);
        if (osgeo.exists()) {
            return osgeo.getAbsolutePath();
        }
        File osgeo64 = new File("C:\\OSGeo4W64\\bin\\" + exeName);
        if (osgeo64.exists()) {
            return osgeo64.getAbsolutePath();
        }
        return fallback;
    }

    private static String buildCacheKey(File file) throws IOException {
        String base = file.getAbsolutePath() + "|" + file.length() + "|" + file.lastModified();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("img_cache_");
            for (int i = 0; i < 12 && i < digest.length; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IOException("No se pudo generar clave de cache raster.", ex);
        }
    }

    private static String buildDiagnosticMessage(String originalPath, File fileRead, Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append("No se pudo leer el archivo raster:\n");
        sb.append(originalPath);

        if (fileRead != null && !originalPath.equals(fileRead.getAbsolutePath())) {
            sb.append("\n\nArchivo usado internamente:\n");
            sb.append(fileRead.getAbsolutePath());
        }

        if (ex != null) {
            sb.append("\n\nDetalle: ");
            sb.append(ex.getClass().getSimpleName());
            if (ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
                sb.append(": ").append(ex.getMessage().trim());
            }
        }
        return sb.toString();
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private static Envelope extractEnvelopeByReflection(GridCoverage2D coverage, BufferedImage raw) {
        try {
            Object ge = coverage.getEnvelope();
            if (ge != null) {
                java.lang.reflect.Method getMinimum = ge.getClass().getMethod("getMinimum", int.class);
                java.lang.reflect.Method getMaximum = ge.getClass().getMethod("getMaximum", int.class);

                double minX = ((Number) getMinimum.invoke(ge, 0)).doubleValue();
                double maxX = ((Number) getMaximum.invoke(ge, 0)).doubleValue();
                double minY = ((Number) getMinimum.invoke(ge, 1)).doubleValue();
                double maxY = ((Number) getMaximum.invoke(ge, 1)).doubleValue();

                return new Envelope(minX, maxX, minY, maxY);
            }
        } catch (Exception ignored) {
        }

        return new Envelope(0, raw.getWidth(), 0, raw.getHeight());
    }

    private static String extractSourceCRS(GridCoverage2D coverage, AbstractGridCoverage2DReader reader) {
        CoordinateReferenceSystem crs = null;

        try {
            crs = coverage != null ? coverage.getCoordinateReferenceSystem2D() : null;
        } catch (Exception ignored) {
        }

        try {
            if (crs == null && coverage != null) {
                crs = coverage.getCoordinateReferenceSystem();
            }
        } catch (Exception ignored) {
        }

        try {
            if (crs == null && reader != null) {
                crs = reader.getCoordinateReferenceSystem();
            }
        } catch (Exception ignored) {
        }

        if (crs == null) {
            return "";
        }

        try {
            String code = CRS.lookupIdentifier(crs, true);
            if (isResolvableCode(code)) {
                return CRSDefinitions.normalizeCode(code);
            }
        } catch (Exception ignored) {
        }

        try {
            String code = CRS.toSRS(crs, true);
            if (isResolvableCode(code)) {
                return CRSDefinitions.normalizeCode(code);
            }
        } catch (Exception ignored) {
        }

        return "";
    }

    private static GridCoverage2D reprojectCoverageIfNeeded(GridCoverage2D coverage, String sourceCRSCode, String targetCRSCode)
            throws IOException {
        if (coverage == null) {
            return null;
        }

        String source = CRSDefinitions.normalizeCode(sourceCRSCode);
        String target = CRSDefinitions.normalizeCode(targetCRSCode);

        if (!isResolvableCode(source) || !isResolvableCode(target) || source.equalsIgnoreCase(target)) {
            return coverage;
        }

        try {
            CoordinateReferenceSystem targetCRS = CRS.decode(target, true);
            return (GridCoverage2D) Operations.DEFAULT.resample(coverage, targetCRS);
        } catch (Exception ex) {
            return coverage;
        }
    }

    private static GridCoverage2D assignSourceCRSIfNeeded(GridCoverage2D coverage, Envelope envelope, String sourceCRSCode)
            throws IOException {
        if (coverage == null || envelope == null || envelope.isNull() || !isResolvableCode(sourceCRSCode)) {
            return coverage;
        }

        try {
            CoordinateReferenceSystem sourceCRS = CRS.decode(CRSDefinitions.normalizeCode(sourceCRSCode), true);
            ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(envelope, sourceCRS);
            GridCoverageFactory factory = new GridCoverageFactory();
            return factory.create("catgis-raster", coverage.getRenderedImage(), referencedEnvelope);
        } catch (Exception ex) {
            throw new IOException("No se pudo asignar el CRS del raster.", ex);
        }
    }

    private static String pickUsableSourceCRS(String requestedSourceCRS, String extractedSourceCRS) {
        if (isResolvableCode(requestedSourceCRS)) {
            return CRSDefinitions.normalizeCode(requestedSourceCRS);
        }
        if (isResolvableCode(extractedSourceCRS)) {
            return CRSDefinitions.normalizeCode(extractedSourceCRS);
        }
        return "";
    }

    private static boolean isResolvableCode(String code) {
        String normalized = CRSDefinitions.normalizeCode(code);
        if (normalized.isBlank()) {
            return false;
        }

        try {
            CRS.decode(normalized, true);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static BufferedImage renderedToBuffered(GridCoverage2D coverage) {
        RenderedImage ri = coverage.getRenderedImage();

        if (ri instanceof BufferedImage) {
            return (BufferedImage) ri;
        }

        try {
            BufferedImage out = new BufferedImage(ri.getWidth(), ri.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = out.createGraphics();
            try {
                g.drawRenderedImage(ri, new AffineTransform());
            } finally {
                g.dispose();
            }
            return out;
        } catch (RuntimeException ex) {
            return renderRasterFallback(ri);
        }
    }

    private static BufferedImage renderRasterFallback(RenderedImage image) {
        Raster raster = image.getData();
        int width = raster.getWidth();
        int height = raster.getHeight();
        int numBands = Math.max(1, raster.getNumBands());
        int usedBands = Math.min(numBands, 3);
        double[] mins = new double[usedBands];
        double[] maxs = new double[usedBands];
        for (int i = 0; i < usedBands; i++) {
            mins[i] = Double.POSITIVE_INFINITY;
            maxs[i] = Double.NEGATIVE_INFINITY;
        }

        int stepX = Math.max(1, width / 512);
        int stepY = Math.max(1, height / 512);
        for (int y = 0; y < height; y += stepY) {
            for (int x = 0; x < width; x += stepX) {
                for (int b = 0; b < usedBands; b++) {
                    double value = raster.getSampleDouble(x, y, b);
                    if (Double.isNaN(value) || Double.isInfinite(value)) {
                        continue;
                    }
                    if (value < mins[b]) mins[b] = value;
                    if (value > maxs[b]) maxs[b] = value;
                }
            }
        }

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r;
                int g;
                int b;
                if (numBands == 1) {
                    int v = stretchDouble(raster.getSampleDouble(x, y, 0), mins[0], maxs[0]);
                    r = g = b = v;
                } else {
                    r = stretchDouble(raster.getSampleDouble(x, y, 0), mins[0], maxs[0]);
                    g = stretchDouble(raster.getSampleDouble(x, y, Math.min(1, usedBands - 1)), mins[Math.min(1, usedBands - 1)], maxs[Math.min(1, usedBands - 1)]);
                    b = stretchDouble(raster.getSampleDouble(x, y, Math.min(2, usedBands - 1)), mins[Math.min(2, usedBands - 1)], maxs[Math.min(2, usedBands - 1)]);
                }
                out.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    private static BufferedImage prepareDisplayImage(BufferedImage raw, int maxDisplaySize) {
        BufferedImage display = toDisplayImage(raw);
        return downscaleIfNeeded(display, maxDisplaySize);
    }

    private static BufferedImage downscaleIfNeeded(BufferedImage src, int maxSize) {
        int width = src.getWidth();
        int height = src.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return src;
        }

        double scale = Math.min((double) maxSize / width, (double) maxSize / height);
        int newW = Math.max(1, (int) Math.round(width * scale));
        int newH = Math.max(1, (int) Math.round(height * scale));

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, newW, newH, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    private static BufferedImage toDisplayImage(BufferedImage raw) {
        Raster raster = raw.getRaster();
        int width = raw.getWidth();
        int height = raw.getHeight();
        int numBands = raster.getNumBands();
        int dataType = raster.getDataBuffer().getDataType();

        if (dataType == DataBuffer.TYPE_BYTE && (numBands == 1 || numBands >= 3)) {
            BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb;
                    if (numBands == 1) {
                        int v = raster.getSample(x, y, 0);
                        rgb = (v << 16) | (v << 8) | v;
                    } else {
                        int r = raster.getSample(x, y, 0);
                        int g = raster.getSample(x, y, 1);
                        int b = raster.getSample(x, y, 2);
                        rgb = (r << 16) | (g << 8) | b;
                    }
                    copy.setRGB(x, y, rgb);
                }
            }

            return copy;
        }

        int usedBands = Math.min(numBands, 3);
        int[] mins = new int[usedBands];
        int[] maxs = new int[usedBands];
        for (int i = 0; i < usedBands; i++) {
            mins[i] = Integer.MAX_VALUE;
            maxs[i] = Integer.MIN_VALUE;
        }

        int stepX = Math.max(1, width / 512);
        int stepY = Math.max(1, height / 512);

        for (int y = 0; y < height; y += stepY) {
            for (int x = 0; x < width; x += stepX) {
                for (int b = 0; b < usedBands; b++) {
                    int v = raster.getSample(x, y, b);
                    if (v < mins[b]) mins[b] = v;
                    if (v > maxs[b]) maxs[b] = v;
                }
            }
        }

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r;
                int g;
                int b;

                if (numBands == 1) {
                    int v = stretch(raster.getSample(x, y, 0), mins[0], maxs[0]);
                    r = g = b = v;
                } else {
                    r = stretch(raster.getSample(x, y, 0), mins[0], maxs[0]);
                    g = stretch(raster.getSample(x, y, 1), mins[Math.min(1, usedBands - 1)], maxs[Math.min(1, usedBands - 1)]);
                    b = stretch(raster.getSample(x, y, 2), mins[Math.min(2, usedBands - 1)], maxs[Math.min(2, usedBands - 1)]);
                }

                int rgb = (r << 16) | (g << 8) | b;
                out.setRGB(x, y, rgb);
            }
        }

        return out;
    }

    private static int stretch(int value, int min, int max) {
        if (max <= min) {
            return 0;
        }

        double scaled = (value - min) * 255.0 / (max - min);
        if (scaled < 0) scaled = 0;
        if (scaled > 255) scaled = 255;
        return (int) Math.round(scaled);
    }

    private static int stretchDouble(double value, double min, double max) {
        if (!Double.isFinite(value) || !Double.isFinite(min) || !Double.isFinite(max) || max <= min) {
            return 0;
        }
        double scaled = (value - min) * 255.0 / (max - min);
        if (scaled < 0) scaled = 0;
        if (scaled > 255) scaled = 255;
        return (int) Math.round(scaled);
    }
}
