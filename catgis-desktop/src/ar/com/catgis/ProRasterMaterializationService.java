package ar.com.catgis;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

final class ProRasterMaterializationService {

    private static final File CACHE_DIR = resolveCacheDirectory();

    private ProRasterMaterializationService() {
    }

    static boolean isMaterializationSupported() {
        try {
            GdalSupport.resolve("gdal_translate.exe");
            return true;
        } catch (GdalSupport.GdalNotAvailableException e) {
            return false;
        }
    }

    static MaterializedRaster materialize(ProDatasetOpenService.Entry entry) throws IOException {
        return materialize(entry, ProJobMonitor.noop());
    }

    static MaterializedRaster materialize(ProDatasetOpenService.Entry entry, ProJobMonitor monitor) throws IOException {
        ProJobMonitor effectiveMonitor = monitor != null ? monitor : ProJobMonitor.noop();
        effectiveMonitor.checkCanceled();
        if (entry == null) {
            throw new IOException("No se pudo materializar la variable Pro: entrada nula.");
        }
        if (entry.rasterFile() != null && entry.rasterFile().exists()) {
            effectiveMonitor.report("Reutilizando raster Pro existente: " + entry.variableLabel());
            File sidecar = entry.sidecarFile() != null ? entry.sidecarFile() : ProMetadataSidecarSupport.sidecarFile(entry.rasterFile());
            ensureSidecar(entry, entry.rasterFile());
            return new MaterializedRaster(entry.rasterFile(), sidecar.exists() ? sidecar : null, buildJobRef(buildCacheKey(entry)), false);
        }
        if (entry.sourceFile() == null || !entry.sourceFile().exists()) {
            throw new IOException("No existe el origen Pro para materializar la variable seleccionada.");
        }
        String sourceExpression = resolveSourceExpression(entry);
        if (sourceExpression.isBlank()) {
            throw new IOException("La variable Pro no tiene una expresion fuente materializable.");
        }

        String executable = GdalSupport.resolve("gdal_translate.exe");
        if (!CACHE_DIR.exists() && !CACHE_DIR.mkdirs() && !CACHE_DIR.exists()) {
            throw new IOException("No se pudo crear el cache Pro: " + CACHE_DIR.getAbsolutePath());
        }

        String cacheKey = buildCacheKey(entry);
        File outputFile = resolveOutputFile(entry, cacheKey);
        File sidecarFile = ProMetadataSidecarSupport.sidecarFile(outputFile);
        if (isReusable(outputFile, sidecarFile, entry.sourceFile())) {
            effectiveMonitor.report("Reutilizando raster Pro gestionado: " + outputFile.getName());
            ensureSidecar(entry, outputFile);
            return new MaterializedRaster(outputFile, sidecarFile.exists() ? sidecarFile : null, buildJobRef(cacheKey), false);
        }

        File tempFile = new File(CACHE_DIR, outputFile.getName() + ".tmp.tif");
        if (tempFile.exists() && !tempFile.delete()) {
            throw new IOException("No se pudo limpiar el temporal Pro anterior: " + tempFile.getAbsolutePath());
        }

        ProcessBuilder builder = new ProcessBuilder();
        List<String> command = new java.util.ArrayList<>();
        command.add(executable);
        command.add("-of");
        command.add("GTiff");
        command.add("-co");
        command.add("COMPRESS=LZW");
        command.add("-co");
        command.add("TILED=YES");
        if (entry.variable() != null && entry.variable().getNodata() != null) {
            command.add("-a_nodata");
            command.add(Double.toString(entry.variable().getNodata()));
        }
        command.add(sourceExpression);
        command.add(tempFile.getAbsolutePath());
        builder.command(command);
        builder.redirectErrorStream(true);

        Process process = null;
        ByteArrayOutputStream processOutput = new ByteArrayOutputStream();
        Thread collector = null;
        try {
            effectiveMonitor.report("Materializando con GDAL: " + entry.variableLabel());
            process = builder.start();
            collector = startOutputCollector(process.getInputStream(), processOutput);
            while (true) {
                effectiveMonitor.checkCanceled();
                if (process.waitFor(250, TimeUnit.MILLISECONDS)) {
                    break;
                }
            }
            String output = awaitCollectedOutput(collector, processOutput);
            int code = process.exitValue();
            if (code != 0) {
                throw new IOException("gdal_translate devolvio codigo " + code + ". Salida:\n" + output);
            }
        } catch (InterruptedIOException ex) {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            cleanupTempFile(tempFile);
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            cleanupTempFile(tempFile);
            throw new IOException("La materializacion Pro fue interrumpida.", ex);
        } catch (IOException ex) {
            cleanupTempFile(tempFile);
            throw ex;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        if (!tempFile.exists() || tempFile.length() == 0L) {
            throw new IOException("gdal_translate termino pero no genero un GeoTIFF utilizable.");
        }
        if (outputFile.exists() && !outputFile.delete()) {
            throw new IOException("No se pudo reemplazar el raster Pro gestionado: " + outputFile.getAbsolutePath());
        }
        if (!tempFile.renameTo(outputFile)) {
            throw new IOException("No se pudo renombrar el raster Pro temporal a su destino final.");
        }
        outputFile.setLastModified(Math.max(outputFile.lastModified(), entry.sourceFile().lastModified()));
        effectiveMonitor.report("Sidecar Pro actualizado para: " + entry.variableLabel());
        File writtenSidecar = ensureSidecar(entry, outputFile);
        return new MaterializedRaster(outputFile, writtenSidecar, buildJobRef(cacheKey), true);
    }

    private static File ensureSidecar(ProDatasetOpenService.Entry entry, File outputFile) throws IOException {
        ProMetadataSidecarSupport.Metadata metadata = new ProMetadataSidecarSupport.Metadata(
                copyDataset(entry.dataset()),
                copyVariable(entry.variable()),
                entry.qualityPreset(),
                entry.flagsApplied() != null ? entry.flagsApplied() : List.of(),
                buildRecipe(entry),
                entry.maturity(),
                null
        );
        return ProMetadataSidecarSupport.write(outputFile, metadata);
    }

    private static String buildRecipe(ProDatasetOpenService.Entry entry) {
        if (entry == null) {
            return "pro_materialized";
        }
        if (entry.recipe() != null && !entry.recipe().isBlank()) {
            if (entry.requiresMaterialization()) {
                return entry.recipe() + " | gdal_translate_materialized";
            }
            return entry.recipe();
        }
        return entry.requiresMaterialization() ? "gdal_translate_materialized" : "pro_materialized";
    }

    private static String resolveSourceExpression(ProDatasetOpenService.Entry entry) {
        if (entry.gdalSubdatasetName() != null && !entry.gdalSubdatasetName().isBlank()) {
            return entry.gdalSubdatasetName().trim();
        }
        return entry.sourceFile() != null ? entry.sourceFile().getAbsolutePath() : "";
    }

    private static boolean isReusable(File outputFile, File sidecarFile, File sourceFile) {
        if (outputFile == null || !outputFile.exists() || outputFile.length() == 0L || sourceFile == null || !sourceFile.exists()) {
            return false;
        }
        if (outputFile.lastModified() < sourceFile.lastModified()) {
            return false;
        }
        return sidecarFile == null || sidecarFile.exists();
    }

    private static File resolveOutputFile(ProDatasetOpenService.Entry entry, String cacheKey) {
        String baseName = sanitizeFileToken(entry.variableLabel());
        if (baseName.isBlank()) {
            baseName = "pro_variable";
        }
        return new File(CACHE_DIR, baseName + "_" + cacheKey.substring(0, Math.min(12, cacheKey.length())) + ".tif");
    }

    private static String buildCacheKey(ProDatasetOpenService.Entry entry) throws IOException {
        StringBuilder raw = new StringBuilder();
        if (entry.sourceFile() != null) {
            raw.append(entry.sourceFile().getAbsolutePath()).append('|')
                    .append(entry.sourceFile().length()).append('|')
                    .append(entry.sourceFile().lastModified()).append('|');
        }
        raw.append(entry.gdalSubdatasetName()).append('|')
                .append(entry.datasetRef()).append('|')
                .append(entry.variableLabel());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : bytes) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IOException("No se pudo generar la clave de cache Pro.", ex);
        }
    }

    private static String buildJobRef(String cacheKey) {
        return "gdal-materialize-" + cacheKey.substring(0, Math.min(12, cacheKey.length()));
    }

    private static String readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = stream.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    private static Thread startOutputCollector(InputStream stream, ByteArrayOutputStream output) {
        Thread collector = new Thread(() -> {
            try (InputStream effectiveStream = stream) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = effectiveStream.read(buffer)) >= 0) {
                    synchronized (output) {
                        output.write(buffer, 0, read);
                    }
                }
            } catch (Exception ignored) {
            }
        }, "catgis-pro-gdal-output");
        collector.setDaemon(true);
        collector.start();
        return collector;
    }

    private static String awaitCollectedOutput(Thread collector, ByteArrayOutputStream output) throws IOException {
        if (collector != null) {
            try {
                collector.join(2000L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("No se pudo completar la lectura de salida de GDAL.", ex);
            }
        }
        synchronized (output) {
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    private static void cleanupTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    private static File resolveCacheDirectory() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            return new File(localAppData, "CATGIS\\pro-cache");
        }
        return new File(System.getProperty("java.io.tmpdir", "."), "catgis-pro-cache");
    }

    private static String sanitizeFileToken(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim()
                .replaceAll("[^a-zA-Z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^[._-]+", "")
                .replaceAll("[._-]+$", "")
                .toLowerCase(Locale.ROOT);
    }

    private static ProDatasetDescriptor copyDataset(ProDatasetDescriptor source) {
        ProDatasetDescriptor copy = new ProDatasetDescriptor();
        if (source == null) {
            return copy;
        }
        copy.setDatasetId(source.getDatasetId());
        copy.setFamily(source.getFamily());
        copy.setProvider(source.getProvider());
        copy.setPlatform(source.getPlatform());
        copy.setInstrument(source.getInstrument());
        copy.setProcessingLevel(source.getProcessingLevel());
        copy.setAcquisitionStart(source.getAcquisitionStart());
        copy.setAcquisitionEnd(source.getAcquisitionEnd());
        return copy;
    }

    private static ProVariableDescriptor copyVariable(ProVariableDescriptor source) {
        ProVariableDescriptor copy = new ProVariableDescriptor();
        if (source == null) {
            return copy;
        }
        copy.setName(source.getName());
        copy.setLongName(source.getLongName());
        copy.setStandardName(source.getStandardName());
        copy.setUnits(source.getUnits());
        copy.setDimensions(source.getDimensions());
        copy.setNodata(source.getNodata());
        copy.setScaleFactor(source.getScaleFactor());
        copy.setAddOffset(source.getAddOffset());
        copy.setValidMin(source.getValidMin());
        copy.setValidMax(source.getValidMax());
        copy.setQaDescriptor(source.getQaDescriptor());
        copy.setBandFamily(source.getBandFamily());
        return copy;
    }

    record MaterializedRaster(File rasterFile, File sidecarFile, String jobRef, boolean createdNow) {
    }
}
