package ar.com.catgis;

import org.locationtech.jts.geom.Envelope;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * T3/#161: atomic publication of an online raster TIFF together with its
 * {@code .catgis-raster.properties} sidecar.
 *
 * <p>The previous implementation copied the downloaded stream over the target
 * and wrote the sidecar afterwards: a failure in the middle destroyed the user's
 * previous raster or left a TIFF paired with stale metadata. This publisher
 * stages the TIFF, backs up the existing TIFF + sidecar, publishes both and
 * restores the previous pair on any failure.</p>
 */
public final class RasterBundlePublisher {

    @FunctionalInterface
    public interface StagedRasterWriter {
        void write(File stagedTiff) throws Exception;
    }

    /** Test seam: invoked right after the staged TIFF replaces the target. */
    private static volatile Runnable afterTiffMovedForTesting;

    private RasterBundlePublisher() {}

    public static void afterTiffMovedForTesting(Runnable action) {
        afterTiffMovedForTesting = action;
    }

    public static void publish(File target, StagedRasterWriter writer, Envelope envelope,
                        String sourceCrsCode) throws Exception {
        if (target == null || writer == null) {
            throw new IllegalArgumentException("La publicacion raster requiere destino y contenido.");
        }
        Path targetPath = target.getAbsoluteFile().toPath();
        Path directory = targetPath.getParent();
        Files.createDirectories(directory);
        Path staged = Files.createTempFile(directory, ".catgis-raster-", ".tif");
        Path sidecar = RasterSidecarSupport.sidecarFile(target).getAbsoluteFile().toPath();
        Path tiffBackup = null;
        Path sidecarBackup = null;
        boolean tiffPublished = false;
        boolean committed = false;
        try {
            writer.write(staged.toFile());
            if (!Files.isRegularFile(staged) || Files.size(staged) <= 0) {
                throw new IOException("El raster descargado esta vacio.");
            }
            if (ImageIO.read(staged.toFile()) == null) {
                throw new IOException("El archivo descargado no es una imagen raster valida.");
            }
            if (Files.exists(targetPath)) {
                tiffBackup = Files.createTempFile(directory, ".catgis-tif-old-", ".bak");
                Files.move(targetPath, tiffBackup, StandardCopyOption.REPLACE_EXISTING);
            }
            if (Files.exists(sidecar)) {
                sidecarBackup = Files.createTempFile(directory, ".catgis-sidecar-old-", ".bak");
                Files.move(sidecar, sidecarBackup, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(staged, targetPath, StandardCopyOption.REPLACE_EXISTING);
            tiffPublished = true;
            Runnable failure = afterTiffMovedForTesting;
            if (failure != null) {
                failure.run();
            }
            boolean hasEnvelope = envelope != null && !envelope.isNull();
            if (hasEnvelope) {
                RasterSidecarSupport.write(target, envelope, sourceCrsCode);
                RasterSidecarSupport.Metadata written = RasterSidecarSupport.read(target);
                if (written == null) {
                    throw new IOException("No se pudo verificar el sidecar raster publicado.");
                }
            } else {
                // A successful publication without georeference must not keep a
                // stale sidecar from the previous (different) raster.
                Files.deleteIfExists(sidecar);
            }
            committed = true;
        } catch (Exception failure) {
            restore(targetPath, sidecar, tiffPublished, tiffBackup, sidecarBackup);
            throw failure;
        } finally {
            deleteQuietly(staged);
            if (committed) {
                deleteQuietly(tiffBackup);
                deleteQuietly(sidecarBackup);
            }
        }
    }

    private static void restore(Path target, Path sidecar, boolean tiffPublished,
                                Path tiffBackup, Path sidecarBackup) {
        try {
            if (tiffPublished) {
                Files.deleteIfExists(target);
            }
            if (tiffBackup != null && Files.exists(tiffBackup)) {
                Files.move(tiffBackup, target, StandardCopyOption.REPLACE_EXISTING);
            }
            if (sidecarBackup != null && Files.exists(sidecarBackup)) {
                Files.move(sidecarBackup, sidecar, StandardCopyOption.REPLACE_EXISTING);
            } else if (tiffPublished) {
                // Only a publication that already replaced the TIFF may have
                // created a new sidecar; untouched failures keep the old pair.
                Files.deleteIfExists(sidecar);
            }
        } catch (IOException restoreFailure) {
            CatgisLogger.warn("No se pudo restaurar el raster anterior " + target, restoreFailure);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException failure) {
            CatgisLogger.warn("No se pudo limpiar " + path, failure);
        }
    }
}
