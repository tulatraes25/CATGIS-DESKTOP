package ar.com.catgis.probe;

import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;

/**
 * Read-only local validation/cancellation around the verified official adapter.
 * No custom channel-to-stream read implementation. Preflight is not protection
 * against a hostile path replacement between validation and the official open.
 */
final class LocalProbeInput implements InputFile {
    private final Path path;
    private final long length;
    private final BooleanSupplier cancelled;

    LocalProbeInput(Path path, BooleanSupplier cancelled) throws IOException {
        this.path = path.toAbsolutePath();
        this.cancelled = cancelled;
        GeoParquetRowsProbe.checkActive(cancelled);
        verifyRegularFile();
        length = Files.size(this.path);
        if (length < 12 || length > GeoParquetRowsProbe.MAX_FILE_BYTES) {
            throw new IOException("Probe file exceeds its explicit small-file budget or is truncated.");
        }
    }

    @Override public long getLength() { return length; }

    private void verifyRegularFile() throws IOException {
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Probe requires a local regular file, not a link/directory.");
        }
    }

    @Override public SeekableInputStream newStream() throws IOException {
        GeoParquetRowsProbe.checkActive(cancelled);
        verifyRegularFile();
        InputFile official = HadoopInputFile.fromPath(
                new org.apache.hadoop.fs.Path(path.toUri()), new Configuration(false));
        if (official.getLength() != length) {
            throw new IOException("Probe input size changed before open.");
        }
        SeekableInputStream delegate = official.newStream();
        try {
            GeoParquetRowsProbe.checkActive(cancelled);
            verifyRegularFile();
            if (Files.size(path) != length) { throw new IOException("Probe input size changed during open."); }
            return new SeekableInputStream() {
                private void active() throws IOException { GeoParquetRowsProbe.checkActive(cancelled); }
                @Override public int read() throws IOException {
                    active();
                    return delegate.read();
                }
                @Override public int read(byte[] bytes) throws IOException {
                    active();
                    return delegate.read(bytes);
                }
                @Override public int read(byte[] bytes, int offset, int count) throws IOException {
                    active();
                    return delegate.read(bytes, offset, count);
                }
                @Override public int read(ByteBuffer bytes) throws IOException {
                    active();
                    return delegate.read(bytes);
                }
                @Override public void readFully(byte[] bytes) throws IOException {
                    active();
                    delegate.readFully(bytes);
                }
                @Override public void readFully(byte[] bytes, int offset, int count) throws IOException {
                    active();
                    delegate.readFully(bytes, offset, count);
                }
                @Override public void readFully(ByteBuffer bytes) throws IOException {
                    active();
                    delegate.readFully(bytes);
                }
                @Override public long getPos() throws IOException { return delegate.getPos(); }
                @Override public void seek(long position) throws IOException {
                    active();
                    if (position < 0 || position > length) { throw new IOException("Invalid probe seek."); }
                    delegate.seek(position);
                }
                @Override public long skip(long count) throws IOException {
                    active();
                    return delegate.skip(count);
                }
                @Override public int available() throws IOException { return delegate.available(); }
                // Closing must work even after cancellation or interruption.
                @Override public void close() throws IOException { delegate.close(); }
            };
        } catch (IOException | RuntimeException failure) {
            try { delegate.close(); } catch (IOException closing) { failure.addSuppressed(closing); }
            throw failure;
        }
    }
}
