package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** Fail-closed publisher for vector formats stored in one file. */
final class VectorSingleFilePublisher {
    private static final long MAX_BYTES = 512L * 1024L * 1024L;

    @FunctionalInterface
    interface StagedWriter {
        void write(Path staging) throws Exception;
    }

    @FunctionalInterface
    interface StagedValidator {
        void validate(Path staging) throws Exception;
    }

    @FunctionalInterface
    interface AtomicMover {
        void move(Path staging, Path destination) throws IOException;
    }

    private VectorSingleFilePublisher() {}

    static Path publish(File requested, String suffix,
                        StagedWriter writer, StagedValidator validator) throws Exception {
        return publish(requested, suffix, writer, validator,
                VectorSingleFilePublisher::atomicMove, MAX_BYTES);
    }

    static Path publish(File requested, String suffix,
                        StagedWriter writer, StagedValidator validator,
                        AtomicMover mover, long maxBytes) throws Exception {
        if (requested == null || writer == null || validator == null || mover == null) {
            throw new IOException("La publicacion vectorial requiere destino, escritor y validador.");
        }
        if (suffix == null || !suffix.startsWith(".") || suffix.length() > 16) {
            throw new IOException("La extension temporal vectorial es invalida.");
        }
        if (maxBytes <= 0 || maxBytes > MAX_BYTES) {
            throw new IOException("El limite del archivo vectorial es invalido.");
        }

        Path requestedPath = requested.toPath().toAbsolutePath().normalize();
        Path requestedParent = requestedPath.getParent();
        if (requestedParent == null) {
            throw new IOException("El destino vectorial no tiene carpeta padre.");
        }
        if (!Files.exists(requestedParent)) {
            Files.createDirectories(requestedParent);
        }
        if (!Files.isDirectory(requestedParent)) {
            throw new IOException("La carpeta destino vectorial no es un directorio.");
        }

        Path parent = requestedParent.toRealPath();
        Path destination = parent.resolve(requestedPath.getFileName());
        validateDestination(destination);

        Path staging = Files.createTempFile(parent, ".catgis-vector-", suffix);
        try {
            writer.write(staging);
            validateStageFile(staging, maxBytes);
            validator.validate(staging);
            validateStageFile(staging, maxBytes);
            try (FileChannel channel = FileChannel.open(staging, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            validateDestination(destination);
            try {
                mover.move(staging, destination);
            } catch (AtomicMoveNotSupportedException unsupported) {
                throw new IOException(
                        "El sistema de archivos no permite reemplazo atomico; "
                                + "la capa anterior fue preservada.",
                        unsupported);
            }
            return destination;
        } finally {
            Files.deleteIfExists(staging);
        }
    }

    private static void validateDestination(Path destination) throws IOException {
        if (Files.isSymbolicLink(destination)
                || (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)
                && !Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS))) {
            throw new IOException(
                    "El destino vectorial es un enlace simbolico o no es un archivo regular.");
        }
    }

    private static void validateStageFile(Path staging, long maxBytes) throws IOException {
        if (!Files.isRegularFile(staging, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("El temporal vectorial no es un archivo regular.");
        }
        long size = Files.size(staging);
        if (size <= 0 || size > maxBytes) {
            throw new IOException(
                    "El temporal vectorial esta vacio o excede el limite permitido.");
        }
    }

    private static void atomicMove(Path staging, Path destination) throws IOException {
        Files.move(staging, destination,
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
}
