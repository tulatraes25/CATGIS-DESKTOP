package ar.com.catgis;

import org.apache.parquet.format.ColumnChunk;
import org.apache.parquet.format.ColumnMetaData;
import org.apache.parquet.format.CompressionCodec;
import org.apache.parquet.format.ConvertedType;
import org.apache.parquet.format.DictionaryPageHeader;
import org.apache.parquet.format.Encoding;
import org.apache.parquet.format.FileMetaData;
import org.apache.parquet.format.LogicalType;
import org.apache.parquet.format.PageHeader;
import org.apache.parquet.format.PageType;
import org.apache.parquet.format.RowGroup;
import org.apache.parquet.format.StringType;
import org.apache.parquet.format.Type;
import org.apache.parquet.format.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeoParquetEmptyColumnContractTest {
    @TempDir Path temp;

    @Test
    void zeroRowsAllowAnEmptyDictionaryWithoutInventingADataPageOrRows() throws Exception {
        byte[] dictionary = emptyDictionary();
        ColumnMetaData column = column(0, 0, dictionary.length).setDictionary_page_offset(4);
        FileMetaData footer = footer(column, 0);
        Path file = write(footer, dictionary);
        byte[] original = Files.readAllBytes(file);
        assertEquals(0, GeoParquetReader.readMetadata(file.toFile()).rowCount());
        assertArrayEquals(original, Files.readAllBytes(file));

        Path pageFree = write(footer(column(0, 0, 0), 0), new byte[0]);
        assertEquals(0, GeoParquetReader.readMetadata(pageFree.toFile()).rowCount());
    }

    @Test
    void zeroOffsetCannotConcealADataPageForNonzeroRowsOrValues() throws Exception {
        byte[] dictionary = emptyDictionary();
        for (long[] counts : List.of(new long[] {1, 1}, new long[] {0, 1}, new long[] {1, 0})) {
            ColumnMetaData column = column(counts[1], 0, dictionary.length).setDictionary_page_offset(4);
            Path invalid = write(footer(column, counts[0]), dictionary);
            assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(invalid.toFile()));
        }
    }

    @Test
    void emptyColumnsStillRejectImpossibleDictionaryOffsetsAndUndeclaredOrOverflowingPages() throws Exception {
        byte[] dictionary = emptyDictionary();
        for (long offset : new long[] {-1, 0, 2, 4L + dictionary.length + 1, Long.MAX_VALUE}) {
            ColumnMetaData column = column(0, 0, dictionary.length).setDictionary_page_offset(offset);
            Path invalid = write(footer(column, 0), dictionary);
            assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(invalid.toFile()));
        }
        ColumnMetaData tooLarge = column(0, 0, Long.MAX_VALUE).setDictionary_page_offset(4);
        Path overflow = write(footer(tooLarge, 0), dictionary);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(overflow.toFile()));
        Path noPages = write(footer(column(0, 0, 1), 0), new byte[0]);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(noPages.toFile()));
    }

    @Test
    void textAnnotationsCannotBeSilentlyReinterpretedAsWkbButAttributeAnnotationsRemainLegal() throws Exception {
        FileMetaData footer = GeoParquetTestFixtures.footer(GeoParquetTestFixtures.GEO);
        footer.getSchema().get(2).setConverted_type(ConvertedType.UTF8);
        Path ordinaryAttribute = GeoParquetTestFixtures.write(temp.resolve("attributes.parquet"), footer);
        assertEquals(0, GeoParquetReader.readMetadata(ordinaryAttribute.toFile()).rowCount());

        footer.getSchema().get(1).setConverted_type(ConvertedType.UTF8);
        Path convertedText = GeoParquetTestFixtures.write(temp.resolve("converted.parquet"), footer);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(convertedText.toFile()));
        footer.getSchema().get(1).unsetConverted_type();
        footer.getSchema().get(1).setLogicalType(LogicalType.STRING(new StringType()));
        Path logicalText = GeoParquetTestFixtures.write(temp.resolve("logical.parquet"), footer);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(logicalText.toFile()));
    }

    private static byte[] emptyDictionary() throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        PageHeader page = new PageHeader(PageType.DICTIONARY_PAGE, 0, 0)
                .setDictionary_page_header(new DictionaryPageHeader(0, Encoding.PLAIN));
        Util.writePageHeader(page, body);
        return body.toByteArray();
    }

    private static ColumnMetaData column(long values, long offset, long size) {
        return new ColumnMetaData().setType(Type.BYTE_ARRAY).setEncodings(List.of(Encoding.PLAIN))
                .setPath_in_schema(List.of("shape")).setCodec(CompressionCodec.UNCOMPRESSED)
                .setNum_values(values).setTotal_compressed_size(size).setTotal_uncompressed_size(size)
                .setData_page_offset(offset);
    }

    private static FileMetaData footer(ColumnMetaData column, long rows) {
        FileMetaData footer = GeoParquetTestFixtures.footer(GeoParquetTestFixtures.GEO);
        footer.setSchema(new ArrayList<>(footer.getSchema().subList(0, 2)));
        footer.getSchema().get(0).setNum_children(1);
        footer.setNum_rows(rows);
        ColumnChunk chunk = new ColumnChunk().setFile_offset(4).setMeta_data(column);
        footer.setRow_groups(List.of(new RowGroup(List.of(chunk), column.getTotal_uncompressed_size(), rows)));
        return footer;
    }

    private Path write(FileMetaData metadata, byte[] body) throws IOException {
        ByteArrayOutputStream footer = new ByteArrayOutputStream();
        Util.writeFileMetaData(metadata, footer);
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        file.write("PAR1".getBytes(StandardCharsets.US_ASCII));
        file.write(body);
        file.write(footer.toByteArray());
        file.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(footer.size()).array());
        file.write("PAR1".getBytes(StandardCharsets.US_ASCII));
        Path path = temp.resolve("case.parquet");
        Files.write(path, file.toByteArray());
        return path;
    }
}
