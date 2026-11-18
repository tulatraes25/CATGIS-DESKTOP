package ar.com.catgis;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * T3/#175: bounded frame reader for FlatGeobuf entity windows.
 *
 * <p>The loader used to allocate one direct buffer for the whole feature payload.
 * This reader streams the size-prefixed feature frames in bounded windows:
 * at most one frame is resident, short FileChannel reads are completed
 * explicitly, truncated/oversized frames fail closed and interruption cancels the
 * import without returning a partial success.</p>
 */
final class FlatGeobufFrameReader {

    static final int MAX_FRAME_BYTES = 16 * 1024 * 1024;

    @FunctionalInterface
    interface FrameConsumer {
        void accept(byte[] frame, int index) throws IOException;
    }

    private FlatGeobufFrameReader() {}

    /**
     * Reads every frame in {@code [dataOffset, dataOffset + dataBytes)}.
     *
     * @param declaredFeatures header count, or -1 when unknown; when non-negative
     *                         the decoded count must match exactly
     * @return decoded frame count
     */
    static int stream(FileChannel channel, long dataOffset, long dataBytes,
                      long declaredFeatures, FrameConsumer consumer) throws IOException {
        if (channel == null || consumer == null) {
            throw new IllegalArgumentException("FlatGeobuf requiere canal y consumidor.");
        }
        if (dataOffset < 0 || dataBytes < 0) {
            throw new IOException("Rango de datos FlatGeobuf invalido.");
        }
        // Check cancellation before any interruptible channel operation (NIO would
        // otherwise close the channel and report ClosedByInterruptException).
        checkInterrupted(0);
        channel.position(dataOffset);
        ByteBuffer sizeBuffer = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        byte[] frame = null;
        long consumed = 0;
        int decoded = 0;
        while (consumed < dataBytes) {
            checkInterrupted(decoded);
            if (dataBytes - consumed < Integer.BYTES) {
                throw new IOException("FlatGeobuf truncado: tamano de entidad incompleto "
                        + "en el indice " + decoded + ".");
            }
            sizeBuffer.clear();
            readFully(channel, sizeBuffer, "tamano de entidad FlatGeobuf");
            consumed += Integer.BYTES;
            int frameSize = sizeBuffer.getInt(0);
            if (frameSize <= 0) {
                throw new IOException("FlatGeobuf corrupto: tamano de entidad invalido ("
                        + frameSize + ") en el indice " + decoded + ".");
            }
            if (frameSize > MAX_FRAME_BYTES) {
                throw new IOException("FlatGeobuf rechazado: entidad de " + frameSize
                        + " bytes supera el maximo por frame (" + MAX_FRAME_BYTES + ").");
            }
            if (frameSize > dataBytes - consumed) {
                throw new IOException("FlatGeobuf truncado: la ultima entidad (indice "
                        + decoded + ") declara " + frameSize + " bytes y solo quedan "
                        + (dataBytes - consumed) + ".");
            }
            if (frame == null || frame.length < frameSize) {
                frame = new byte[frameSize];
            }
            ByteBuffer frameBuffer = ByteBuffer.wrap(frame, 0, frameSize)
                    .order(ByteOrder.LITTLE_ENDIAN);
            readFully(channel, frameBuffer, "entidad FlatGeobuf " + decoded);
            consumed += frameSize;
            byte[] payload = frame.length == frameSize ? frame : java.util.Arrays.copyOf(frame, frameSize);
            consumer.accept(payload, decoded);
            decoded++;
            checkInterrupted(decoded);
        }
        if (declaredFeatures >= 0 && decoded != declaredFeatures) {
            throw new IOException("Conteo FlatGeobuf incongruente: declarado "
                    + declaredFeatures + " leido " + decoded + ".");
        }
        return decoded;
    }

    private static void readFully(FileChannel channel, ByteBuffer buffer, String label)
            throws IOException {
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read < 0) {
                throw new IOException("FlatGeobuf truncado: fin de archivo inesperado en " + label + ".");
            }
            if (read == 0) {
                throw new IOException("FlatGeobuf: lectura vacia inesperada en " + label + ".");
            }
        }
        buffer.flip();
    }

    private static void checkInterrupted(int decoded) throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException(
                    "Importacion FlatGeobuf cancelada tras " + decoded + " entidades.");
        }
    }
}
