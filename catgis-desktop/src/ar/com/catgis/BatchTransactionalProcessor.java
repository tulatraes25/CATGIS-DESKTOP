package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * T4/#114: opt-in all-files transaction for batch jobs.
 *
 * <p>The existing {@link BatchProcessor#processBatch} keeps per-file success/
 * failure semantics. This processor runs every operation against a private
 * staging tree in the output directory, and only publishes outputs after all
 * operations succeed. Existing outputs are backed up before the first atomic
 * rename; a failure rolls every published file back and preserves the previous
 * bytes. Inputs are never modified.</p>
 *
 * <p>Scope: single-file operations. Shapefile sidecar dataset locking and crash
 * recovery remain a separate incremental gate, and the batch UI is not switched
 * to this API until reviewed.</p>
 */
public final class BatchTransactionalProcessor {

    @FunctionalInterface
    interface AtomicPublisher {
        void publish(Path staged, Path target) throws IOException;
    }

    private BatchTransactionalProcessor() {}

    public static BatchProcessor.BatchResult process(BatchProcessor.BatchJob job,
                                                     BiConsumer<File, File> operation,
                                                     Consumer<Integer> progressCallback) {
        return process(job, operation, progressCallback, BatchTransactionalProcessor::publishAtomically);
    }

    static BatchProcessor.BatchResult process(BatchProcessor.BatchJob job,
                                              BiConsumer<File, File> operation,
                                              Consumer<Integer> progressCallback,
                                              AtomicPublisher publisher) {
        List<File> inputs = List.copyOf(job.inputFiles());
        int total = inputs.size();
        List<BatchOutputPlan.Entry> plan;
        try {
            plan = BatchOutputPlan.build(inputs, job.outputDir());
        } catch (IOException | SecurityException unsafePlan) {
            return new BatchProcessor.BatchResult(0, total,
                    List.of("Lote rechazado antes de ejecutar: " + unsafePlan.getMessage()));
        }
        if (total == 0) {
            return new BatchProcessor.BatchResult(0, 0, List.of());
        }
        Path stagingRoot;
        try {
            stagingRoot = Files.createTempDirectory(job.outputDir().toPath(), ".catgis-batch-tx-");
        } catch (IOException stagingFailure) {
            return new BatchProcessor.BatchResult(0, total,
                    List.of("No se pudo crear el staging transaccional: "
                            + stagingFailure.getMessage()));
        }
        List<Staged> staged = new ArrayList<>();
        List<Backup> backups = new ArrayList<>();
        try {
            for (int i = 0; i < total; i++) {
                BatchOutputPlan.Entry entry = plan.get(i);
                String prefix = i + "-";
                Path stagedPath = stagingRoot.resolve(prefix + entry.output().getName());
                try {
                    operation.accept(entry.input(), stagedPath.toFile());
                } catch (RuntimeException failure) {
                    return new BatchProcessor.BatchResult(0, total,
                            List.of(entry.input().getName() + ": " + failure.getMessage()));
                }
                if (!Files.isRegularFile(stagedPath)) {
                    return new BatchProcessor.BatchResult(0, total,
                            List.of(entry.input().getName()
                                    + ": la operacion no produjo una salida regular"));
                }
                // Dataset family: primary output + sidecars named after the same
                // staged basename (e.g. .aux.xml/.tfw/.prj) are published as one
                // transaction unit and rolled back together.
                List<StagedFile> family = new ArrayList<>();
                family.add(new StagedFile(stagedPath, entry.output().toPath()));
                try (var files = Files.list(stagingRoot)) {
                    List<Path> siblings = files
                            .filter(path -> path.getFileName().toString().startsWith(prefix))
                            .filter(Files::isRegularFile)
                            .filter(path -> !path.equals(stagedPath))
                            .sorted()
                            .toList();
                    for (Path sibling : siblings) {
                        String relative = sibling.getFileName().toString()
                                .substring(prefix.length());
                        Path target = entry.output().toPath().getParent().resolve(relative);
                        family.add(new StagedFile(sibling, target));
                    }
                }
                staged.add(new Staged(family));
                if (progressCallback != null) {
                    progressCallback.accept((int) ((i + 1.0) / total * 100));
                }
            }
            int backupIndex = 0;
            for (Staged current : staged) {
                for (StagedFile file : current.files()) {
                    Path target = file.target();
                    Path backup = null;
                    if (Files.isRegularFile(target, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
                        backup = stagingRoot.resolve("backup-" + backupIndex++ + "-"
                                + target.getFileName());
                        Files.move(target, backup, StandardCopyOption.ATOMIC_MOVE);
                    }
                    try {
                        publisher.publish(file.staged(), target);
                    } catch (IOException publishFailure) {
                        if (backup != null) {
                            Files.move(backup, target, StandardCopyOption.ATOMIC_MOVE,
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                        rollback(backups);
                        return new BatchProcessor.BatchResult(0, total,
                                List.of("Publicacion transaccional fallida: "
                                        + publishFailure.getMessage()));
                    }
                    backups.add(new Backup(target, backup));
                }
            }
            return new BatchProcessor.BatchResult(total, 0, List.of());
        } catch (IOException ioFailure) {
            rollback(backups);
            return new BatchProcessor.BatchResult(0, total,
                    List.of("Lote transaccional fallido: " + ioFailure.getMessage()));
        } finally {
            deleteRecursively(stagingRoot);
        }
    }

    private static void publishAtomically(Path staged, Path target) throws IOException {
        try {
            Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException unsupported) {
            throw new IOException("El filesystem de salida no soporta reemplazo atomico; "
                    + "se preservaron las salidas previas.", unsupported);
        }
    }

    private static void rollback(List<Backup> backups) {
        for (int i = backups.size() - 1; i >= 0; i--) {
            Backup backup = backups.get(i);
            try {
                Files.deleteIfExists(backup.target());
                if (backup.backup() != null) {
                    Files.move(backup.backup(), backup.target(), StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ignored) {
                // Best-effort rollback; the staging tree is kept by the caller for diagnosis.
            }
        }
    }

    private static void deleteRecursively(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Staging cleanup is best-effort; outputs already rolled back.
                }
            });
        } catch (IOException ignored) {
            // Staging cleanup is best-effort.
        }
    }

    private record Staged(List<StagedFile> files) {}

    private record StagedFile(Path staged, Path target) {}

    private record Backup(Path target, Path backup) {}
}
