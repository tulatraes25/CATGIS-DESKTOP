package ar.com.catgis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Interprocess lease for one canonical XYZ output pathname. The sibling lock
 * file is intentionally permanent: unlinking a locked file would let another
 * JVM lock a different inode and publish concurrently.
 *
 * This is a process-level mutex, not power-loss recovery. Recovery journaling
 * is a separate A0.7 gate tracked in issue #73.
 */
final class GdalTileTargetLock implements AutoCloseable {

    private static final byte[] MAGIC =
            "CATGIS_XYZ_TARGET_LOCK_V1\n".getBytes(StandardCharsets.US_ASCII);

    private final FileChannel channel;
    private final FileLock held;

    private GdalTileTargetLock(FileChannel channel, FileLock held) {
        this.channel = channel;
        this.held = held;
    }

    static Path lockPath(Path canonicalTarget) throws IOException {
        return lockPath(canonicalTarget, System.getProperty("os.name", ""));
    }

    // Test seam: Windows and default macOS are case-insensitive. A hash based
    // on a caller's casing could otherwise lock DIFFERENT files for one target.
    static Path lockPath(Path canonicalTarget, String osName) throws IOException {
        if (canonicalTarget == null || !canonicalTarget.isAbsolute()
                || canonicalTarget.getFileName() == null
                || canonicalTarget.getParent() == null) {
            throw new IOException("Canonical absolute XYZ target is required.");
        }
        String identity = canonicalTarget.toString();
        String platform = osName == null ? "" : osName.toLowerCase(Locale.ROOT);
        if (platform.startsWith("windows") || platform.contains("mac")
                || platform.contains("darwin")) {
            // On case-sensitive macOS volumes this deliberately over-locks
            // distinct case variants: safe conservatism over a collision race.
            identity = identity.toLowerCase(Locale.ROOT);
        }
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(
                    identity.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException missingSha256) {
            throw new IOException("Required lock identity algorithm unavailable.", missingSha256);
        }
        return canonicalTarget.getParent().resolve(
                ".catgis-xyz-lease-" + HexFormat.of().formatHex(digest) + ".lck");
    }

    static GdalTileTargetLock tryAcquire(Path canonicalTarget) throws IOException {
        Path lockPath = lockPath(canonicalTarget);
        if (!Files.isDirectory(lockPath.getParent(), LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(lockPath)) {
            throw new IOException("XYZ lease parent unavailable or lock path is a symlink.");
        }
        FileChannel channel = null;
        FileLock held = null;
        boolean transferred = false;
        try {
            // NOFOLLOW_LINKS prevents a substituted lock symlink from silently
            // redirecting our file descriptor outside the canonical parent.
            channel = FileChannel.open(lockPath,
                    StandardOpenOption.CREATE, StandardOpenOption.READ,
                    StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
            if (!Files.isRegularFile(lockPath, LinkOption.NOFOLLOW_LINKS)
                    || Files.isSymbolicLink(lockPath)) {
                throw new IOException("XYZ lease path is not a regular file.");
            }
            try {
                held = channel.tryLock();
            } catch (OverlappingFileLockException alreadyHeldHere) {
                return null;
            }
            if (held == null) {
                return null;
            }
            assertMarker(channel);
            GdalTileTargetLock lease = new GdalTileTargetLock(channel, held);
            transferred = true;
            return lease;
        } finally {
            if (!transferred) {
                try {
                    if (held != null && held.isValid()) {
                        held.release();
                    }
                } finally {
                    if (channel != null) {
                        channel.close();
                    }
                }
            }
        }
    }

    private static void assertMarker(FileChannel channel) throws IOException {
        long length = channel.size();
        if (length == 0L) {
            channel.position(0L);
            ByteBuffer stamp = ByteBuffer.wrap(MAGIC);
            while (stamp.hasRemaining()) {
                channel.write(stamp);
            }
            channel.force(true);
            return;
        }
        if (length != MAGIC.length) {
            throw new IOException("Unrecognized or modified CATGIS XYZ lease file.");
        }
        ByteBuffer stored = ByteBuffer.allocate(MAGIC.length);
        while (stored.hasRemaining()) {
            int read = channel.read(stored);
            if (read < 0) {
                throw new IOException("Truncated CATGIS XYZ lease marker.");
            }
        }
        byte[] bytes = stored.array();
        if (!java.util.Arrays.equals(MAGIC, bytes)) {
            throw new IOException("Invalid CATGIS XYZ lease marker; refusing to use it.");
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (held.isValid()) {
                held.release();
            }
        } finally {
            channel.close();
        }
    }
}
