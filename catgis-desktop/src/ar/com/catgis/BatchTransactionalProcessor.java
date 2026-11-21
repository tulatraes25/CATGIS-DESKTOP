package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
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
        return process(job, operation, progressCallback, () -> false,
                BatchTransactionalProcessor::publishAtomically);
    }

    public static BatchProcessor.BatchResult process(BatchProcessor.BatchJob job,
                                                     BiConsumer<File, File> operation,
                                                     Consumer<Integer> progressCallback,
                                                     java.util.function.BooleanSupplier cancelled) {
        return process(job, operation, progressCallback, cancelled,
                BatchTransactionalProcessor::publishAtomically);
    }

    private static final String JOURNAL_PREFIX = ".catgis-batch-tx-journal-";
    private static final String JOURNAL_HEADER = "CATGIS_BATCH_TX|1|";

    static BatchProcessor.BatchResult process(BatchProcessor.BatchJob job,
                                              BiConsumer<File, File> operation,
                                              Consumer<Integer> progressCallback,
                                              java.util.function.BooleanSupplier cancelled,
                                              AtomicPublisher publisher) {
        try {
            recoverInterruptedTransactions(job.outputDir());
        } catch (IOException recoveryFailure) {
            return new BatchProcessor.BatchResult(0,
                    job.inputFiles() == null ? 0 : job.inputFiles().size(),
                    List.of("No se pudo recuperar un lote interrumpido: "
                            + recoveryFailure.getMessage()));
        }
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
                if (isCancelled(cancelled)) {
                    return cancelled(total);
                }
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
            if (isCancelled(cancelled)) {
                return cancelled(total);
            }
            int backupIndex = 0;
            Path journal = createJournal(stagingRoot, job.outputDir().toPath());
            try {
                for (Staged current : staged) {
                    for (StagedFile file : current.files()) {
                        Path target = file.target();
                        Path backup = null;
                        if (Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
                            backup = stagingRoot.resolve("backup-" + backupIndex++ + "-"
                                    + target.getFileName());
                            appendJournal(journal, "B|" + target + "|" + backup);
                            Files.move(target, backup, StandardCopyOption.ATOMIC_MOVE);
                        } else {
                            appendJournal(journal, "B|" + target + "|-");
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
                        appendJournal(journal, "P|" + target);
                        backups.add(new Backup(target, backup));
                    }
                }
                return new BatchProcessor.BatchResult(total, 0, List.of());
            } finally {
                Files.deleteIfExists(journal);
            }
        } catch (IOException ioFailure) {
            rollback(backups);
            return new BatchProcessor.BatchResult(0, total,
                    List.of("Lote transaccional fallido: " + ioFailure.getMessage()));
        } finally {
            deleteRecursively(stagingRoot);
        }
    }

    private static boolean isCancelled(java.util.function.BooleanSupplier cancelled) {
        return Thread.currentThread().isInterrupted()
                || (cancelled != null && cancelled.getAsBoolean());
    }

    private static BatchProcessor.BatchResult cancelled(int total) {
        return new BatchProcessor.BatchResult(0, total,
                List.of("Lote cancelado; no se publico ninguna salida y las previas quedaron intactas."));
    }

    private static Path createJournal(Path stagingRoot, Path outputDir) throws IOException {
        Path journal = outputDir.resolve(JOURNAL_PREFIX + java.util.UUID.randomUUID());
        String header = JOURNAL_HEADER + stagingRoot.toAbsolutePath().normalize() + "\n";
        try (FileChannel channel = FileChannel.open(journal,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            channel.write(java.nio.ByteBuffer.wrap(header.getBytes(StandardCharsets.UTF_8)));
            channel.force(true);
        }
        return journal;
    }

    private static void appendJournal(Path journal, String line) throws IOException {
        try (FileChannel channel = FileChannel.open(journal,
                StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            channel.write(java.nio.ByteBuffer.wrap((line + "\n")
                    .getBytes(StandardCharsets.UTF_8)));
            channel.force(true);
        }
    }

    /**
     * Recover batches interrupted between backup and publication. The journal is
     * written before each destructive move, so recovery restores previous bytes
     * (or removes a new partial output) and always deletes the private staging.
     * Only journals inside the requested output directory are considered.
     */
    public static int recoverInterruptedTransactions(File outputDir) throws IOException {
        if (outputDir == null || !outputDir.isDirectory()) {
            return 0;
        }
        Path root = outputDir.toPath().toAbsolutePath().normalize();
        int recovered = 0;
        try (var entries = Files.list(root)) {
            for (Path journal : entries
                    .filter(path -> path.getFileName().toString().startsWith(JOURNAL_PREFIX))
                    .filter(Files::isRegularFile)
                    .toList()) {
                recoverJournal(journal, root);
                recovered++;
            }
        }
        return recovered;
    }

    private static void recoverJournal(Path journal, Path root) throws IOException {
        List<String> lines = Files.readAllLines(journal, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.get(0).startsWith(JOURNAL_HEADER)) {
            throw new IOException("Journal de lote invalido: " + journal.getFileName());
        }
        Path staging = Path.of(lines.get(0).substring(JOURNAL_HEADER.length()))
                .toAbsolutePath().normalize();
        if (!staging.startsWith(root)) {
            throw new IOException("Journal de lote apunta fuera del directorio de salida.");
        }
        java.util.LinkedHashMap<Path, Path> backups = new java.util.LinkedHashMap<>();
        for (String line : lines.subList(1, lines.size())) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 2 || !"B".equals(parts[0])) {
                continue; // P lines only confirm a publication; B carries the restore plan.
            }
            Path target = Path.of(parts[1]).toAbsolutePath().normalize();
            if (!target.startsWith(root)) {
                throw new IOException("Journal de lote contiene un destino fuera de la salida.");
            }
            Path backup = "-".equals(parts[2]) ? null
                    : Path.of(parts[2]).toAbsolutePath().normalize();
            if (backup != null && !backup.startsWith(staging)) {
                throw new IOException("Journal de lote contiene un backup fuera del staging.");
            }
            backups.put(target, backup);
        }
        for (var entry : backups.entrySet()) {
            Path target = entry.getKey();
            Path backup = entry.getValue();
            if (backup != null && Files.isRegularFile(backup, LinkOption.NOFOLLOW_LINKS)) {
                Files.deleteIfExists(target);
                Files.move(backup, target, StandardCopyOption.ATOMIC_MOVE);
            } else if (backup == null) {
                Files.deleteIfExists(target);
            }
        }
        deleteRecursively(staging);
        Files.deleteIfExists(journal);
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
