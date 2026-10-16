package ar.com.catgis.probe;

import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.conf.PlainParquetConfiguration;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.schema.MessageTypeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.InputStreamInStream;
import org.locationtech.jts.io.WKBReader;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Independent I/O/representation controls; never alter a failing expected value. */
class ScalarBoundaryDiagnosticsTest {
    @Test
    void rawScalarAndOfficialFileAdapterPreserveBothDisputedValues() throws Exception {
        byte[] literal = ByteBuffer.allocate(21).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 1).putInt(1).putDouble(0.5).putDouble(0.5).array();
        Point control = (Point) new WKBReader().read(literal);
        Group scalar = new SimpleGroupFactory(MessageTypeParser.parseMessageType(
                "message control { required double amplitude; }")).newGroup();
        scalar.add("amplitude", -0.0);
        long expectedBits = Double.doubleToRawLongBits(-0.0);
        System.out.println("SCALAR_CONTROL_WKB=" + HexFormat.of().formatHex(literal)
                + " XY=" + control + " DOUBLE_BITS=" + Long.toHexString(Double.doubleToRawLongBits(scalar.getDouble(0, 0))));
        String fixtures = System.getenv("CATGIS_GEOPARQUET_FIXTURES");
        assertNotNull(fixtures);
        List<Executable> assertions = new ArrayList<>();
        assertions.add(() -> assertEquals(0.5, control.getX()));
        assertions.add(() -> assertEquals(expectedBits, Double.doubleToRawLongBits(scalar.getDouble(0, 0))));
        for (boolean official : new boolean[] {true, false}) {
            for (boolean probeOptions : new boolean[] {false, true}) {
                String route = (official ? "OFFICIAL" : "LOCAL") + (probeOptions ? "_PROBE_OPTIONS" : "_DEFAULT_OPTIONS");
                byte[] wkb = (byte[]) readInsidePage(Path.of(fixtures).resolve("multiple.parquet"),
                        "center", true, official, probeOptions);
                long bits = (Long) readInsidePage(Path.of(fixtures).resolve("typed-snappy.parquet"),
                        "amplitude", false, official, probeOptions);
                Point fromBytes = (Point) new WKBReader().read(wkb);
                Point fromStream = (Point) new WKBReader().read(new InputStreamInStream(new ByteArrayInputStream(wkb)));
                System.out.println("LIVE_PAGE_CONTROL=" + route + " WKB=" + HexFormat.of().formatHex(wkb)
                        + " BYTE_XY=" + fromBytes + " STREAM_XY=" + fromStream + " DOUBLE_BITS=" + Long.toHexString(bits));
                assertions.add(() -> assertArrayEquals(literal, wkb, route + " exact owned WKB"));
                assertions.add(() -> assertEquals(0.5, fromBytes.getX(), route + " byte-array X"));
                assertions.add(() -> assertEquals(0.5, fromStream.getX(), route + " stream X"));
                assertions.add(() -> assertEquals(expectedBits, bits, route + " raw double bits"));
            }
        }
        assertAll(assertions);
    }

    private static Object readInsidePage(Path path, String column, boolean binary,
                                         boolean official, boolean probeOptions) throws Exception {
        InputFile input = official
                ? HadoopInputFile.fromPath(new org.apache.hadoop.fs.Path(path.toUri()), new Configuration(false))
                : new LocalProbeInput(path, () -> false);
        var builder = ParquetReadOptions.builder(new PlainParquetConfiguration()).usePageChecksumVerification(true);
        if (probeOptions) {
            builder.withMaxAllocationInBytes((int) GeoParquetRowsProbe.MAX_ROW_GROUP_BYTES)
                    .useStatsFilter(false).useDictionaryFilter(false).useRecordFilter(false)
                    .useColumnIndexFilter(false).useBloomFilter(false);
        }
        var options = builder.build();
        try (var reader = ParquetFileReader.open(input, options)) {
            var schema = reader.getFooter().getFileMetaData().getSchema();
            try (var page = reader.readNextRowGroup()) {
                assertNotNull(page);
                Group row = new ColumnIOFactory().getColumnIO(schema)
                        .getRecordReader(page, new GroupRecordConverter(schema)).read();
                assertNotNull(row);
                // Never return a Group/Binary borrowed from a page which this
                // method closes. Own the bytes or primitive bits before close.
                if (binary) {
                    byte[] owned = row.getBinary(column, 0).getBytes().clone();
                    System.out.println("BEFORE_PAGE_CLOSE column=" + column + " WKB=" + HexFormat.of().formatHex(owned));
                    return owned;
                }
                long bits = Double.doubleToRawLongBits(row.getDouble(column, 0));
                System.out.println("BEFORE_PAGE_CLOSE column=" + column + " DOUBLE_BITS=" + Long.toHexString(bits));
                return bits;
            }
        } finally {
            options.getCodecFactory().release();
        }
    }
}
