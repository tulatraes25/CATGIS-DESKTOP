package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * T1/#149: isolated, versioned recovery snapshots for unsaved project work.
 *
 * <p>Snapshots live outside the project directory ({@code ~/.catgis/recovery}
 * by default, overridable with {@code -Dcatgis.recovery.root=...}), are written
 * atomically (temp dir + move), carry a SHA-256 manifest and include the
 * in-memory vector layers as Shapefile sidecars so unsaved edits survive a
 * crash. Restoration is non-destructive: the snapshot is opened as a normal
 * project and the user's original file is never modified. Corrupt or edited
 * snapshots fail closed instead of restoring partial data.</p>
 */
public final class ProjectRecoveryJournal {

    public static final int MAX_SNAPSHOTS = 5;
    public static final String ROOT_PROPERTY = "catgis.recovery.root";

    private static final long AUTO_SNAPSHOT_DELAY_MS = 60_000L;
    private static final long AUTO_SNAPSHOT_INTERVAL_MS = 60_000L;
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static volatile long lastAutoSnapshotAt;
    private static javax.swing.Timer autoTimer;

    private ProjectRecoveryJournal() {}

    /** A manifest-backed snapshot ready to be opened. */
    public record RecoverableSnapshot(Path directory, Path projectFile, long timestamp,
                                      String projectName, int layerCount) {}

    /** Enables periodic autosnapshots for dirty projects (EDT timer, headless no-op). */
    public static synchronized void onProjectDirty() {
        if (GraphicsEnvironment.isHeadless() || autoTimer != null) {
            return;
        }
        autoTimer = new javax.swing.Timer((int) AUTO_SNAPSHOT_INTERVAL_MS,
                event -> snapshotCurrentQuietly());
        autoTimer.setRepeats(true);
        autoTimer.start();
    }

    private static void snapshotCurrentQuietly() {
        Project project = AppContext.project();
        MapPanel panel = AppContext.mapPanel();
        if (project == null || !project.isModified() || panel == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastAutoSnapshotAt < AUTO_SNAPSHOT_DELAY_MS) {
            return;
        }
        lastAutoSnapshotAt = now;
        Map<Layer, ShapefileData> data = new LinkedHashMap<>();
        for (Layer layer : project.getLayers()) {
            ShapefileData shapefile = panel.getShapefileData(layer);
            if (ExportVectorLayerAction.hasExportableVectorData(shapefile)) {
                data.put(layer, shapefile);
            }
        }
        try {
            snapshot(project, data);
        } catch (Exception failure) {
            CatgisLogger.warn("No se pudo crear el snapshot de recuperacion", failure);
        }
    }

    public static Path root() {
        String override = System.getProperty(ROOT_PROPERTY);
        if (override != null && !override.isBlank()) {
            return Path.of(override).toAbsolutePath();
        }
        return Path.of(System.getProperty("user.home"), ".catgis", "recovery");
    }

    /**
     * Writes one snapshot for {@code project}. {@code inMemory} supplies the
     * unsaved vector data per layer; layers without data keep their on-disk path.
     */
    public static Path snapshot(Project project, Map<Layer, ShapefileData> inMemory)
            throws Exception {
        if (project == null) {
            throw new IllegalArgumentException("La recuperacion requiere un proyecto.");
        }
        Path root = root();
        Files.createDirectories(root);
        long timestamp = System.currentTimeMillis();
        long sequence = SEQUENCE.incrementAndGet();
        Path identityDirectory = root.resolve(identity(project));
        Files.createDirectories(identityDirectory);
        Path destination = identityDirectory.resolve(
                "snapshot-" + timestamp + "-" + String.format("%06d", sequence));
        Files.createDirectories(destination);
        boolean committed = false;
        try {
            Path dataDirectory = destination.resolve("data");
            Files.createDirectories(dataDirectory);
            List<String> layerLines = new ArrayList<>();
            int dataIndex = 0;
            for (Layer layer : project.getLayers()) {
                if (layer == null) {
                    continue;
                }
                String line = SaveProjectAction.buildLayerLine(layer);
                ShapefileData data = inMemory != null ? inMemory.get(layer) : null;
                if (ExportVectorLayerAction.hasExportableVectorData(data)) {
                    File sidecar = dataDirectory.resolve("capa-" + dataIndex++ + ".shp").toFile();
                    String originalPath = layer.getPath();
                    String originalName = layer.getSourceName();
                    int originalCount = layer.getFeatureCount();
                    try {
                        boolean written = ExportVectorLayerAction.saveLayerDataToFile(
                                layer, data, sidecar, null, false);
                        if (!written) {
                            throw new IOException("No se pudo serializar la capa \""
                                    + layer.getName() + "\" para recuperacion.");
                        }
                    } finally {
                        layer.setPath(originalPath);
                        layer.setSourceName(originalName);
                        layer.setFeatureCount(originalCount);
                    }
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 3) {
                        parts[0] = "[VECTOR]";
                        parts[2] = ProjectRecordCodec.escape(sidecar.getAbsolutePath());
                        line = String.join("|", parts);
                    }
                }
                layerLines.add(line);
            }
            String text = renderProject(project, layerLines);
            Path projectFile = destination.resolve("proyecto.catgis");
            Files.writeString(projectFile, text, StandardCharsets.UTF_8);
            Properties manifest = new Properties();
            manifest.setProperty("timestamp", Long.toString(timestamp));
            manifest.setProperty("projectName", safe(project.getName()));
            manifest.setProperty("originalProjectFile",
                    project.getProjectFile() != null ? project.getProjectFile().getAbsolutePath() : "");
            manifest.setProperty("layerCount", Integer.toString(project.getLayers() != null
                    ? project.getLayers().size() : 0));
            manifest.setProperty("dataFiles", Integer.toString(dataIndex));
            manifest.setProperty("sha256", sha256(projectFile));
            Path manifestFile = destination.resolve("manifest.properties");
            manifest.store(Files.newBufferedWriter(manifestFile, StandardCharsets.UTF_8),
                    "CATGIS recovery snapshot");
            committed = true;
            prune(identityDirectory);
            return projectFile;
        } finally {
            if (!committed) {
                deleteRecursively(destination);
            }
        }
    }
    /** Newest valid snapshot across all identities, or null when none exists. */
    public static RecoverableSnapshot latestRecoverable() {
        Path root = root();
        if (!Files.isDirectory(root)) {
            return null;
        }
        List<RecoverableSnapshot> valid = new ArrayList<>();
        try (DirectoryStream<Path> identities = Files.newDirectoryStream(root)) {
            for (Path identity : identities) {
                if (!Files.isDirectory(identity)) {
                    continue;
                }
                try (DirectoryStream<Path> snapshots = Files.newDirectoryStream(identity, "snapshot-*")) {
                    for (Path snapshot : snapshots) {
                        RecoverableSnapshot parsed = readManifest(snapshot);
                        if (parsed != null) {
                            valid.add(parsed);
                        }
                    }
                }
            }
        } catch (IOException failure) {
            CatgisLogger.warn("No se pudo listar los snapshots de recuperacion", failure);
            return null;
        }
        return valid.stream().max(Comparator.comparingLong(RecoverableSnapshot::timestamp))
                .orElse(null);
    }

    /** Validates a snapshot and returns the project file to open. */
    public static Path requireRestorable(RecoverableSnapshot snapshot) throws IOException {
        if (snapshot == null) {
            throw new IOException("No hay snapshot de recuperacion.");
        }
        RecoverableSnapshot validated = readManifest(snapshot.directory());
        if (validated == null || !validated.projectFile().equals(snapshot.projectFile())) {
            throw new IOException("El snapshot de recuperacion esta incompleto o corrupto.");
        }
        return validated.projectFile();
    }

    /** Removes recovery snapshots for a saved/opened project. */
    public static void discardFor(Project project) {
        if (project == null) {
            return;
        }
        deleteRecursively(root().resolve(identity(project)));
    }

    private static RecoverableSnapshot readManifest(Path directory) {
        Path manifestFile = directory.resolve("manifest.properties");
        Path projectFile = directory.resolve("proyecto.catgis");
        if (!Files.isRegularFile(manifestFile) || !Files.isRegularFile(projectFile)) {
            return null;
        }
        try {
            Properties manifest = new Properties();
            try (var reader = Files.newBufferedReader(manifestFile, StandardCharsets.UTF_8)) {
                manifest.load(reader);
            }
            String expected = manifest.getProperty("sha256", "");
            if (!expected.equalsIgnoreCase(sha256(projectFile))) {
                return null;
            }
            long timestamp = Long.parseLong(manifest.getProperty("timestamp", "0"));
            String name = manifest.getProperty("projectName", "");
            int layers = Integer.parseInt(manifest.getProperty("layerCount", "0"));
            return new RecoverableSnapshot(directory, projectFile, timestamp, name, layers);
        } catch (Exception invalid) {
            return null;
        }
    }

    private static String renderProject(Project project, List<String> layerLines) {
        StringBuilder text = new StringBuilder();
        text.append("CATGIS_PROJECT\n");
        text.append(ProjectRecordCodec.FORMAT_LINE).append('\n');
        text.append("PROJECT_CRS|").append(ProjectRecordCodec.escape(project.getProjectCRS()))
                .append('\n');
        appendMeta(text, "STUDY_NAME", project.getStudyName());
        appendMeta(text, "COMPANY_NAME", project.getCompanyName());
        appendMeta(text, "CARTOGRAPHER_NAME", project.getCartographerName());
        appendMeta(text, "IMAGE_SOURCE", project.getImageSource());
        appendMeta(text, "COORDINATE_REFERENCE", project.getCoordinateReference());
        appendMeta(text, "LEGEND_TITLE", project.getLegendTitle());
        appendMeta(text, "LEGEND_SUBTITLE", project.getLegendSubtitle());
        appendMeta(text, "LOGO_PATH", project.getLogoPath());
        appendMeta(text, "LAYOUT_IMAGE_PATH", project.getLayoutImagePath());
        appendMeta(text, "CATMAP_NORTH_STYLE", project.getCatmapNorthStyle());
        text.append("PROJECT_META|CATMAP_SHOW_NORTH|").append(project.isCatmapShowNorth())
                .append('\n');
        for (String line : layerLines) {
            text.append(line).append('\n');
        }
        return text.toString();
    }

    private static void appendMeta(StringBuilder text, String key, String value) {
        if (value != null && !value.isBlank()) {
            text.append("PROJECT_META|").append(key).append('|')
                    .append(ProjectRecordCodec.escape(value)).append('\n');
        }
    }

    private static void prune(Path identityDirectory) throws IOException {
        List<Path> snapshots = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(identityDirectory, "snapshot-*")) {
            for (Path snapshot : stream) {
                if (Files.isDirectory(snapshot)) {
                    snapshots.add(snapshot);
                }
            }
        }
        snapshots.sort(Comparator.comparing(Path::getFileName).reversed());
        for (int index = MAX_SNAPSHOTS; index < snapshots.size(); index++) {
            deleteRecursively(snapshots.get(index));
        }
    }

    private static String identity(Project project) {
        String key = project.getProjectFile() != null
                ? project.getProjectFile().getAbsolutePath()
                : "untitled:" + safe(project.getName());
        return sha256Text(key).substring(0, 16);
    }

    private static String sha256(Path file) throws IOException {
        try {
            return sha256Text(Files.readString(file, StandardCharsets.UTF_8));
        } catch (Exception failure) {
            throw new IOException("No se pudo verificar el snapshot " + file, failure);
        }
    }

    private static String sha256Text(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) {
            throw new IllegalStateException("SHA-256 no disponible", impossible);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void deleteRecursively(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (var paths = Files.walk(path)) {
            for (Path entry : paths.sorted(Comparator.reverseOrder()).toList()) {
                try {
                    Files.deleteIfExists(entry);
                } catch (IOException ignored) {
                    // best effort cleanup
                }
            }
        } catch (IOException ignored) {
            // best effort cleanup
        }
    }
}
