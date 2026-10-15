package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BatchTemplateAllocationContractTest {
    @Test
    void oversizedReportedMapIsRejectedBeforeAnyEntryCopy() {
        Map<String, String> oversized = new AbstractMap<>() {
            @Override public int size() { return BatchTemplateCodec.MAX_SETTINGS + 1; }
            @Override public Set<Entry<String, String>> entrySet() {
                throw new AssertionError("The size limit must be checked BEFORE copying entries.");
            }
        };
        var template = new BatchProcessor.BatchTemplate("bounded", "", "copy", "", false, oversized);
        assertThrows(IllegalArgumentException.class, template::toJson);
    }

    @Test
    void staleReportedSizeCannotMakeSnapshotIterationUnbounded() {
        AtomicInteger visited = new AtomicInteger();
        Map<String, String> changing = new AbstractMap<>() {
            @Override public int size() { return 0; }
            @Override public Set<Entry<String, String>> entrySet() {
                return new AbstractSet<>() {
                    @Override public int size() { return BatchTemplateCodec.MAX_SETTINGS + 100; }
                    @Override public Iterator<Entry<String, String>> iterator() {
                        return IntStream.range(0, size()).mapToObj(index -> {
                            visited.incrementAndGet();
                            return Map.entry("k" + index, "v");
                        }).iterator();
                    }
                };
            }
        };
        var template = new BatchProcessor.BatchTemplate("bounded", "", "copy", "", false, changing);
        assertThrows(IllegalArgumentException.class, template::toJson);
        assertEquals(BatchTemplateCodec.MAX_SETTINGS + 1, visited.get());
    }
}
