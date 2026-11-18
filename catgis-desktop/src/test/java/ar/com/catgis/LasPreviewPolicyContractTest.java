package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T3/#176: typed LiDAR preview policy, stratified sample and export guard. */
class LasPreviewPolicyContractTest {

    @TempDir
    Path folder;

    @Test
    void planIsCompleteAtTheLimitAndExplicitPreviewAboveIt() {
        LasPreviewPolicy.Plan complete =
                LasPreviewPolicy.plan(50_000, LasPreviewPolicy.MAX_PREVIEW_POINTS);
        assertFalse(complete.preview());
        assertEquals(50_000, complete.samplePoints());
        assertEquals(1L, complete.stride());

        LasPreviewPolicy.Plan preview =
                LasPreviewPolicy.plan(100_001, LasPreviewPolicy.MAX_PREVIEW_POINTS);
        assertTrue(preview.preview());
        assertEquals(3L, preview.stride());
        assertEquals(33_334, preview.samplePoints());
        assertTrue(preview.message().contains("PARTIAL_PREVIEW"));
        assertTrue(preview.message().contains("100001"));

        LasPreviewPolicy.Plan empty = LasPreviewPolicy.plan(0, LasPreviewPolicy.MAX_PREVIEW_POINTS);
        assertFalse(empty.preview());
        assertEquals(0, empty.samplePoints());
    }

    @Test
    void sampledReadIsStratifiedAndBounded() throws Exception {
        Path file = fixture(100_001);
        LasReader.LasHeader header = LasReader.readHeader(file.toFile());
        List<LasReader.LasPoint> sample =
                LasReader.readSampledPoints(file.toFile(), 50_000, header);

        assertEquals(33_334, sample.size(), "stride sample must stay bounded");
        for (LasReader.LasPoint point : sample) {
            assertTrue(point.x() >= -71.0 && point.x() <= -69.0,
                    "sample must stay inside the declared extent: " + point.x());
            assertTrue(point.y() >= -71.0 && point.y() <= -69.0);
        }
        assertFalse(sample.get(0).x() == sample.get(sample.size() - 1).x(),
                "stratified sample must span the point range, not an identical prefix");
        assertEquals(50_000, LasReader.readPoints(file.toFile(), 50_000).size(),
                "legacy prefix reader keeps its documented bound");
    }

    @Test
    void partialPreviewStateRoundTripsInTheProjectLine() {
        Layer partial = new Layer("LiDAR preview", "/data/big.las", "VECTOR");
        partial.setPartialPreview(true);
        partial.setTotalSourceFeatures(1_000_001);
        partial.setLoadedSourceFeatures(33_334);

        Layer parsed = LoadProjectAction.parseLayer(
                SaveProjectAction.buildLayerLine(partial), true);
        assertTrue(parsed.isPartialPreview());
        assertEquals(1_000_001, parsed.getTotalSourceFeatures());
        assertEquals(33_334, parsed.getLoadedSourceFeatures());

        Layer complete = new Layer("LiDAR complete", "/data/small.las", "VECTOR");
        Layer parsedComplete = LoadProjectAction.parseLayer(
                SaveProjectAction.buildLayerLine(complete), true);
        assertFalse(parsedComplete.isPartialPreview());
        assertEquals(-1L, parsedComplete.getTotalSourceFeatures());
    }

    @Test
    void exportGuardBlocksPartialPreviewButAllowsCompleteData() {
        Layer partial = new Layer("preview", "/data/big.las", "VECTOR");
        partial.setPartialPreview(true);
        partial.setTotalSourceFeatures(90_000);
        partial.setLoadedSourceFeatures(30_000);
        IllegalStateException blocked = assertThrows(IllegalStateException.class,
                () -> LasPreviewPolicy.requireCompleteForExport(partial));
        assertTrue(blocked.getMessage().contains("vista previa"), blocked.getMessage());

        Layer complete = new Layer("complete", "/data/small.las", "VECTOR");
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> LasPreviewPolicy.requireCompleteForExport(complete));
    }

    @Test
    void previewStatusIsVisibleAndProfessionalAnalysisIsBlocked() {
        Layer partial = new Layer("preview", "/data/big.las", "VECTOR");
        partial.setPartialPreview(true);
        partial.setTotalSourceFeatures(90_000);
        partial.setLoadedSourceFeatures(30_000);
        String status = LasPreviewPolicy.statusLabel(partial);
        assertTrue(status.contains("PREVIEW"), status);
        assertTrue(status.contains("30000"), status);
        assertTrue(status.contains("90000"), status);

        IllegalStateException blocked = assertThrows(IllegalStateException.class,
                () -> LasPreviewPolicy.requireCompleteForAnalysis(partial, "validar topologia"));
        assertTrue(blocked.getMessage().contains("validar topologia"), blocked.getMessage());

        Layer complete = new Layer("complete", "/data/small.las", "VECTOR");
        assertEquals("", LasPreviewPolicy.statusLabel(complete));
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> LasPreviewPolicy.requireCompleteForAnalysis(complete, "validar topologia"));
    }

    /** Uncompressed LAS 1.2 with 20-byte point format 0 and a wide declared extent. */
    private Path fixture(long declaredPoints) throws Exception {
        int headerSize = 227;
        int recordSize = 20;
        ByteBuffer bytes = ByteBuffer.allocate(headerSize + (int) (recordSize * declaredPoints))
                .order(ByteOrder.LITTLE_ENDIAN);
        bytes.put(new byte[]{'L', 'A', 'S', 'F'});
        bytes.put(24, (byte) 1);
        bytes.put(25, (byte) 2);
        bytes.putShort(94, (short) headerSize);
        bytes.putInt(96, headerSize);
        bytes.put(104, (byte) 0);
        bytes.putShort(105, (short) recordSize);
        bytes.putInt(107, (int) declaredPoints);
        bytes.putDouble(131, 0.01d);
        bytes.putDouble(139, 0.01d);
        bytes.putDouble(147, 0.1d);
        bytes.putDouble(155, -70d);
        bytes.putDouble(163, -70d);
        bytes.putDouble(171, 5d);
        bytes.putDouble(179, -69d);   // max X
        bytes.putDouble(187, -70d);   // min X
        bytes.putDouble(195, -69d);   // max Y
        bytes.putDouble(203, -70d);   // min Y
        bytes.putDouble(211, 20d);    // max Z
        bytes.putDouble(219, 0d);     // min Z
        bytes.position(headerSize);
        for (long i = 0; i < declaredPoints; i++) {
            int raw = (int) (i % 100);
            bytes.putInt(raw);
            bytes.putInt(raw);
            bytes.putInt(50); // z = 5 + 50*0.1 = 10, inside [0,4]? adjusted below
            bytes.putShort((short) 1);
            bytes.put((byte) 1);
            bytes.put((byte) 2);
            bytes.put((byte) 0);
            bytes.put((byte) 0);
            bytes.putShort((short) 0);
        }
        Path file = folder.resolve("big.las");
        byte[] data = bytes.array();
        Files.write(file, data);
        return file;
    }
}
