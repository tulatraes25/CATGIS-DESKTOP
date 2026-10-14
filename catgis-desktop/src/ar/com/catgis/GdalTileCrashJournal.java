package ar.com.catgis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Same-target, bounded, versioned recovery journal for an interrupted XYZ
 * directory replacement. Caller MUST own the GdalTileTargetLock while using it.
 *
 * An atomic file rename is not a portable directory fsync or an atomic swap.
 * This recovers ordinary abrupt JVM exits, not arbitrary power-loss ordering.
 */
final class GdalTileCrashJournal {
    private static final String VERSION = "CATGIS_XYZ_JOURNAL_V1";
    private static final String OWNER_VERSION = "CATGIS_XYZ_WORKSPACE_V1";
    private static final String OWNER_NAME = ".catgis-xyz-workspace-owner";
    private static final Pattern WORKSPACE_NAME =
            Pattern.compile("\\.catgis-xyz-stage-[A-Za-z0-9_-]{6,80}");
    private static final int MAX_JOURNAL_BYTES = 512;

    private GdalTileCrashJournal() {}

    static Path journalPath(Path target) throws IOException {
        Path control = GdalTileTargetLock.lockPath(target);
        String name = control.getFileName().toString();
        if (!name.endsWith(".lck")) {
            throw new IOException("Unexpected CATGIS lease identity.");
        }
        return control.resolveSibling(name.substring(0, name.length() - 4) + ".txn");
    }

    private static String identity(Path target) throws IOException {
        return GdalTileTargetLock.lockPath(target).getFileName().toString();
    }

    private static String expectedOwner(Path target) throws IOException {
        return OWNER_VERSION + "\n" + identity(target) + "\n";
    }

    private static void assertWorkspace(Path target, Path workspace)
            throws IOException {
        if (workspace == null || !workspace.isAbsolute()
                || !workspace.getParent().equals(target.getParent())
                || !WORKSPACE_NAME.matcher(workspace.getFileName().toString()).matches()
                || !Files.isDirectory(workspace, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(workspace)) {
            throw new IOException("Untrusted XYZ recovery workspace.");
        }
        Path owner = workspace.resolve(OWNER_NAME);
        if (!Files.isRegularFile(owner, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(owner)
                || Files.size(owner) > 200L
                || !expectedOwner(target).equals(Files.readString(owner))) {
            throw new IOException("Untrusted XYZ workspace ownership marker.");
        }
    }

    private static void forceText(Path file, String text) throws IOException {
        byte[] contents = text.getBytes(StandardCharsets.UTF_8);
        try (FileChannel channel = FileChannel.open(file,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
            ByteBuffer buffer = ByteBuffer.wrap(contents);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true);
        }
    }

    static void prepare(Path target, Path workspace) throws IOException {
        Path journal = journalPath(target);
        if (Files.exists(journal, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Previous XYZ journal must be recovered first.");
        }
        if (!workspace.isAbsolute() || !workspace.getParent().equals(target.getParent())
                || !WORKSPACE_NAME.matcher(workspace.getFileName().toString()).matches()
                || !Files.isDirectory(workspace, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(workspace)) {
            throw new IOException("Invalid private XYZ output workspace.");
        }
        Path owner = workspace.resolve(OWNER_NAME);
        forceText(owner, expectedOwner(target));
        assertWorkspace(target, workspace);
        String contents = VERSION + "\n" + workspace.getFileName() + "\n"
                + identity(target) + "\n";
        if (contents.getBytes(StandardCharsets.UTF_8).length > MAX_JOURNAL_BYTES) {
            throw new IOException("XYZ recovery journal exceeds size limit.");
        }
        Path temp = Files.createTempFile(target.getParent(),
                ".catgis-xyz-intent-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temp,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
                    LinkOption.NOFOLLOW_LINKS)) {
                ByteBuffer bytes = ByteBuffer.wrap(
                        contents.getBytes(StandardCharsets.UTF_8));
                while (bytes.hasRemaining()) {
                    channel.write(bytes);
                }
                channel.force(true);
            }
            // Same-parent atomic intent publication, before old->backup move.
            Files.move(temp, journal, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static Path readWorkspace(Path target, Path journal)
            throws IOException {
        if (!Files.isRegularFile(journal, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(journal)
                || Files.size(journal) > MAX_JOURNAL_BYTES) {
            throw new IOException("Untrusted or oversized XYZ recovery journal.");
        }
        List<String> lines = Files.readAllLines(journal, StandardCharsets.UTF_8);
        if (lines.size() != 3 || !VERSION.equals(lines.get(0))
                || !identity(target).equals(lines.get(2))
                || !WORKSPACE_NAME.matcher(lines.get(1)).matches()) {
            throw new IOException("XYZ recovery journal version or identity mismatch.");
        }
        Path workspace = target.getParent().resolve(lines.get(1));
        if (!workspace.getParent().equals(target.getParent())) {
            throw new IOException("XYZ recovery directory escapes the parent.");
        }
        return workspace;
    }

    /** Run under the persistent target kernel lease, never from Swing EDT. */
    static boolean recover(Path target) throws IOException {
        Path journal = journalPath(target);
        if (!Files.exists(journal, LinkOption.NOFOLLOW_LINKS)) {
            return true;
        }
        Path workspace = readWorkspace(target, journal);
        boolean workspaceExists = Files.exists(workspace, LinkOption.NOFOLLOW_LINKS);
        if (workspaceExists) {
            assertWorkspace(target, workspace);
        }
        Path backup = workspace.resolve("previous");
        Path staged = workspace.resolve("tiles");
        boolean targetExists = Files.exists(target, LinkOption.NOFOLLOW_LINKS);
        boolean backupExists = Files.exists(backup, LinkOption.NOFOLLOW_LINKS);
        boolean stagedExists = Files.exists(staged, LinkOption.NOFOLLOW_LINKS);

        if (targetExists && !GdalTileOutputTransaction.isOwnedCompleteDirectory(target)) {
            // Never overwrite a user-created replacement or malformed file.
            return false;
        }
        if (backupExists && !GdalTileOutputTransaction.isOwnedCompleteDirectory(backup)) {
            return false;
        }
        if (targetExists && backupExists && stagedExists) {
            // Multiple candidates exist; never guess which is the right map.
            return false;
        }

        if (!targetExists && backupExists) {
            // Abrupt JVM exit after old->backup, before staged->target:
            // restore the old map by one same-volume atomic rename.
            Files.move(backup, target, StandardCopyOption.ATOMIC_MOVE);
        } else if (!targetExists) {
            // No safe prior version available. Preserve the journal + stage.
            return false;
        }

        // The visible target is owned and complete. The workspace marker
        // proves this is the exact staging owned by the recorded transaction.
        if (workspaceExists) {
            GdalTileOutputTransaction.deleteTreeNoFollow(workspace);
        }
        Files.delete(journal);
        return true;
    }

    /** Only remove an authenticated journal for the exact caller workspace. */
    static void clear(Path target, Path workspace) throws IOException {
        Path journal = journalPath(target);
        if (!Files.exists(journal, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        Path recordedWorkspace = readWorkspace(target, journal);
        if (!recordedWorkspace.equals(workspace)
                || Files.exists(workspace, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("XYZ recovery workspace not yet safely cleaned.");
        }
        Files.delete(journal);
    }
}
