package ar.com.catgis;

import org.apache.parquet.format.FieldRepetitionType;
import org.apache.parquet.format.FileMetaData;
import org.apache.parquet.format.KeyValue;
import org.apache.parquet.format.SchemaElement;
import org.apache.parquet.format.Type;
import org.apache.parquet.format.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Genuine empty Parquet files need a real Thrift footer, but no data pages. */
public final class GeoParquetTestFixtures {
    public static final String GEO = "{\"version\":\"1.1.0\",\"primary_column\":\"shape\","
            + "\"columns\":{\"shape\":{\"encoding\":\"WKB\",\"geometry_types\":[]}}}";

    private GeoParquetTestFixtures() {}

    public static FileMetaData footer(String geo) {
        List<SchemaElement> schema = new ArrayList<>();
        schema.add(new SchemaElement("test_schema").setNum_children(2));
        schema.add(new SchemaElement("shape").setType(Type.BYTE_ARRAY)
                .setRepetition_type(FieldRepetitionType.OPTIONAL));
        schema.add(new SchemaElement("nombre").setType(Type.BYTE_ARRAY)
                .setRepetition_type(FieldRepetitionType.OPTIONAL));
        FileMetaData footer = new FileMetaData(1, schema, 0, new ArrayList<>());
        if (geo != null) {
            footer.setKey_value_metadata(new ArrayList<>(List.of(new KeyValue("geo").setValue(geo))));
        }
        return footer;
    }

    public static Path empty(Path target) throws IOException {
        return write(target, footer(GEO));
    }

    public static Path write(Path target, FileMetaData metadata) throws IOException {
        ByteArrayOutputStream footer = new ByteArrayOutputStream();
        Util.writeFileMetaData(metadata, footer);
        return wrap(target, footer.toByteArray());
    }

    static Path wrap(Path target, byte[] footer) throws IOException {
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        file.write("PAR1".getBytes(StandardCharsets.US_ASCII));
        file.write(footer);
        file.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(footer.length).array());
        file.write("PAR1".getBytes(StandardCharsets.US_ASCII));
        Files.write(target, file.toByteArray());
        return target;
    }
}
