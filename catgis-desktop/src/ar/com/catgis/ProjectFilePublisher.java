package ar.com.catgis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** Fail-closed publication of the CATGIS project document. */
final class ProjectFilePublisher {
    private static final long MAX_PROJECT_BYTES = 64L * 1024L * 1024L;

    @FunctionalInterface
    interface StagedWriter {
        void write(Path staging) throws Exception;
    }

    @FunctionalInterface
    interface AtomicMover {
        void move(Path staging, Path destination) throws IOException;
    }

    private ProjectFilePublisher() {}

    static Path publish(File requested, StagedWriter writer) throws Exception {
        return publish(requested, writer, ProjectFilePublisher::atomicMove, MAX_PROJECT_BYTES);
    }

    static Path publish(File requested, StagedWriter writer,
                        AtomicMover mover, long maxBytes) throws Exception {
        if (requested == null || writer == null || mover == null) {
            throw new IOException("El guardado del proyecto requiere destino, escritor y publicador.");
        }
        if (maxBytes < 64L || maxBytes > MAX_PROJECT_BYTES) {
            throw new IOException("El limite del proyecto CATGIS es invalido.");
        }

        Path requestedPath = requested.toPath().toAbsolutePath().normalize();
        Path requestedParent = requestedPath.getParent();
        if (requestedParent == null || !Files.isDirectory(requestedParent)) {
            throw new IOException("La carpeta destino del proyecto no existe.");
        }
        Path parent = requestedParent.toRealPath();
        Path destination = parent.resolve(requestedPath.getFileName());
        validateDestination(destination);

        Path staging = Files.createTempFile(parent, ".catgis-project-", ".part");
        try {
            writer.write(staging);
            validateStaging(staging, maxBytes);
            try (FileChannel channel = FileChannel.open(staging, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            // Check again immediately before replacing the user's last good project.
            validateDestination(destination);
            try {
                mover.move(staging, destination);
            } catch (AtomicMoveNotSupportedException unsupported) {
                throw new IOException(
                        "El sistema de archivos no permite reemplazo atomico; "
                                + "el proyecto anterior fue preservado.",
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
                    "El destino del proyecto es un enlace simbolico o no es un archivo regular.");
        }
    }

    private static void validateStaging(Path staging, long maxBytes) throws IOException {
        if (!Files.isRegularFile(staging, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("El archivo temporal del proyecto no es regular.");
        }
        long size = Files.size(staging);
        if (size <= 0L || size > maxBytes) {
            throw new IOException("El proyecto temporal esta vacio o excede el limite permitido.");
        }
        try (BufferedReader reader = Files.newBufferedReader(staging, StandardCharsets.UTF_8)) {
            if (!"CATGIS_PROJECT".equals(reader.readLine())) {
                throw new IOException("El proyecto temporal no tiene una cabecera CATGIS valida.");
            }
        }
    }

    private static void atomicMove(Path staging, Path destination) throws IOException {
        Files.move(staging, destination,
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
}
