package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Read-only whole-batch target planning, independent of Swing and GIS codecs.
 * This rejects known collisions before the first callback; it is not a lock,
 * a multi-file transaction or protection against hostile later path changes.
 */
final class BatchOutputPlan {

    record Entry(File input, File output) {}

    private record FileIdentity(FileSystem fileSystem, Object key) {}

    private BatchOutputPlan() {}

    static List<Entry> build(List<File> inputFiles, File outputDirectory) throws IOException {
        List<File> inputs = List.copyOf(inputFiles);
        if (inputs.isEmpty()) {
            return List.of();
        }
        if (outputDirectory == null) {
            throw new IOException("El directorio de salida del lote es obligatorio.");
        }
        Path root = outputDirectory.toPath().toAbsolutePath().toRealPath();
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("La salida del lote debe ser un directorio existente.");
        }

        // Resolve every source before checking any target: an early operation
        // must not overwrite a different source scheduled later in this batch.
        IdentityIndex sourceIndex = new IdentityIndex();
        for (File input : inputs) {
            Path source = input.toPath().toAbsolutePath().toRealPath();
            BasicFileAttributes attributes = Files.readAttributes(
                    source, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (!attributes.isRegularFile()) {
                throw new IOException("La entrada del lote no es un archivo regular: " + input.getName());
            }
            sourceIndex.add(source, attributes);
        }

        List<Entry> plan = new ArrayList<>(inputs.size());
        Set<String> names = new HashSet<>();
        IdentityIndex targetIndex = new IdentityIndex();
        for (int i = 0; i < inputs.size(); i++) {
            String name = inputs.get(i).getName();
            // Deliberately conservative on case-sensitive volumes: a plan
            // with spelling collisions is not portable to Windows/default Mac.
            String portableName = Normalizer.normalize(name, Normalizer.Form.NFC)
                    .toLowerCase(Locale.ROOT);
            if (name.isEmpty() || !names.add(portableName)) {
                throw new IOException("Dos entradas comparten una salida del lote: " + name
                        + ". Use nombres distintos; no se renombraron archivos.");
            }
            Path target = root.resolve(name);
            BasicFileAttributes attributes = readIfPresent(target);
            if (attributes != null && !attributes.isRegularFile()) {
                throw new IOException("La salida del lote es un enlace o no es un archivo regular: " + name);
            }
            if (sourceIndex.matches(target, attributes)) {
                throw new IOException("La salida del lote coincide con una entrada: " + name);
            }
            if (targetIndex.matches(target, attributes)) {
                throw new IOException("Dos salidas del lote identifican el mismo archivo: " + name);
            }
            targetIndex.add(target, attributes);
            // Keep the caller's source basename (including a chosen alias):
            // format-dependent callbacks must not see a different extension.
            plan.add(new Entry(inputs.get(i).getAbsoluteFile(), target.toFile()));
        }
        return List.copyOf(plan);
    }

    private static BasicFileAttributes readIfPresent(Path path) throws IOException {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException absent) {
            return null;
        }
    }

    /** Indexed local-filesystem path; exact fallback for providers without keys. */
    private static final class IdentityIndex {
        private final Set<Path> paths = new HashSet<>();
        private final Set<FileIdentity> keys = new HashSet<>();
        private final List<Path> existing = new ArrayList<>();
        private final List<Path> withoutKeys = new ArrayList<>();

        void add(Path path, BasicFileAttributes attributes) {
            paths.add(path);
            if (attributes == null) {
                return;
            }
            existing.add(path);
            Object key = attributes.fileKey();
            if (key == null) {
                withoutKeys.add(path);
            } else {
                keys.add(new FileIdentity(path.getFileSystem(), key));
            }
        }

        boolean matches(Path path, BasicFileAttributes attributes) throws IOException {
            if (paths.contains(path)) {
                return true;
            }
            if (attributes == null) {
                return false;
            }
            Object key = attributes.fileKey();
            if (key != null && keys.contains(new FileIdentity(path.getFileSystem(), key))) {
                return true;
            }
            // File keys are provider-specific and may be absent. Do not turn
            // an unavailable identity hint into permission to overwrite input.
            List<Path> candidates = key == null ? existing : withoutKeys;
            for (Path candidate : candidates) {
                if (Files.isSameFile(path, candidate)) {
                    return true;
                }
            }
            return false;
        }
    }
}
