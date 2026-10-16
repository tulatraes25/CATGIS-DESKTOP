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
import org.apache.parquet.schema.MessageTypeParser;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.HexFormat;

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
        System.out.println("SCALAR_CONTROL_WKB=" + HexFormat.of().formatHex(literal)
                + " XY=" + control + " DOUBLE_BITS=" + Long.toHexString(Double.doubleToRawLongBits(scalar.getDouble(0, 0))));
        String fixtures = System.getenv("CATGIS_GEOPARQUET_FIXTURES");
        assertNotNull(fixtures);
        Group point = readFirst(Path.of(fixtures).resolve("multiple.parquet"));
        byte[] actualWkb = point.getBinary("center", 0).getBytes();
        Point actual = (Point) new WKBReader().read(actualWkb);
        Group number = readFirst(Path.of(fixtures).resolve("typed-snappy.parquet"));
        double value = number.getDouble("amplitude", 0);
        System.out.println("OFFICIAL_FILE_ADAPTER_WKB=" + HexFormat.of().formatHex(actualWkb)
                + " XY=" + actual + " DOUBLE_BITS=" + Long.toHexString(Double.doubleToRawLongBits(value)));
        assertAll(
                () -> assertEquals(0.5, control.getX()),
                () -> assertEquals(Double.doubleToRawLongBits(-0.0), Double.doubleToRawLongBits(scalar.getDouble(0, 0))),
                () -> assertArrayEquals(literal, actualWkb),
                () -> assertEquals(0.5, actual.getX()),
                () -> assertEquals(Double.doubleToRawLongBits(-0.0), Double.doubleToRawLongBits(value)));
    }

    private static Group readFirst(Path path) throws Exception {
        // Official Hadoop local-file adapter is independent of LocalProbeInput.
        var input = HadoopInputFile.fromPath(new org.apache.hadoop.fs.Path(path.toUri()), new Configuration(false));
        var options = ParquetReadOptions.builder(new PlainParquetConfiguration())
                .usePageChecksumVerification(true).build();
        try (var reader = ParquetFileReader.open(input, options)) {
            var schema = reader.getFooter().getFileMetaData().getSchema();
            try (var page = reader.readNextRowGroup()) {
                assertNotNull(page);
                return new ColumnIOFactory().getColumnIO(schema)
                        .getRecordReader(page, new GroupRecordConverter(schema)).read();
            }
        } finally {
            options.getCodecFactory().release();
        }
    }
}
