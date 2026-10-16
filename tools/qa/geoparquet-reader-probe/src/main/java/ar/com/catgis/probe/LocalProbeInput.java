package ar.com.catgis.probe;

import org.apache.parquet.io.DelegatingSeekableInputStream;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BooleanSupplier;

/** Local read-only I/O seam; Apache supplies correct partial/direct-buffer reads. */
final class LocalProbeInput implements InputFile {
    private final Path path;
    private final long length;
    private final BooleanSupplier cancelled;

    LocalProbeInput(Path path, BooleanSupplier cancelled) throws IOException {
        this.path = path;
        this.cancelled = cancelled;
        GeoParquetRowsProbe.checkActive(cancelled);
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Probe requires a local regular file, not a link/directory.");
        }
        length = Files.size(path);
        if (length < 12 || length > GeoParquetRowsProbe.MAX_FILE_BYTES) {
            throw new IOException("Probe file exceeds its explicit small-file budget or is truncated.");
        }
    }

    @Override public long getLength() { return length; }

    @Override public SeekableInputStream newStream() throws IOException {
        GeoParquetRowsProbe.checkActive(cancelled);
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
        try {
            if (channel.size() != length) { throw new IOException("Probe input size changed before open."); }
            FilterInputStream input = new FilterInputStream(Channels.newInputStream(channel)) {
                @Override public int read() throws IOException {
                    GeoParquetRowsProbe.checkActive(cancelled);
                    return super.read();
                }
                @Override public int read(byte[] bytes, int offset, int count) throws IOException {
                    GeoParquetRowsProbe.checkActive(cancelled);
                    return super.read(bytes, offset, count);
                }
            };
            return new DelegatingSeekableInputStream(input) {
                @Override public long getPos() throws IOException { return channel.position(); }
                @Override public void seek(long position) throws IOException {
                    GeoParquetRowsProbe.checkActive(cancelled);
                    if (position < 0 || position > length) { throw new IOException("Invalid probe seek."); }
                    channel.position(position);
                }
            };
        } catch (IOException | RuntimeException failure) {
            channel.close();
            throw failure;
        }
    }
}
