package ar.com.catgis;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.geotools.geometry.jts.Geometries;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseFormatCompatibilityTest {

    @Test
    void loadsAndReexportsKmz() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-release-kmz");
        Path inputKmz = tempDir.resolve("entrada.kmz");
        Path outputKmz = tempDir.resolve("salida.kmz");

        String kml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <kml xmlns="http://www.opengis.net/kml/2.2">
                  <Document>
                    <Placemark>
                      <name>Pozo A</name>
                      <ExtendedData>
                        <Data name="tipo_pozo"><value>productor</value></Data>
                      </ExtendedData>
                      <Point><coordinates>-68.123,-38.456,0</coordinates></Point>
                    </Placemark>
                    <Placemark>
                      <name>Pozo B</name>
                      <ExtendedData>
                        <Data name="tipo_pozo"><value>inyector</value></Data>
                      </ExtendedData>
                      <Point><coordinates>-68.223,-38.556,0</coordinates></Point>
                    </Placemark>
                  </Document>
                </kml>
                """;

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(inputKmz))) {
            zipOutputStream.putNextEntry(new ZipEntry("doc.kml"));
            zipOutputStream.write(kml.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }

        ShapefileData inputData = KmlLoader.load(inputKmz.toFile());
        Layer layer = new Layer("Pozos KMZ", inputKmz.toString(), "VECTOR");
        boolean saved = ExportVectorLayerAction.saveLayerDataToFile(layer, inputData, outputKmz.toFile(), null, false);
        ShapefileData outputData = KmlLoader.load(outputKmz.toFile());

        assertTrue(saved);
        assertEquals(2, inputData.getFeatureCount());
        assertEquals(2, outputData.getFeatureCount());
        try (ZipFile zipFile = new ZipFile(outputKmz.toFile())) {
            assertNotNull(zipFile.getEntry("doc.kml"));
        }
    }

    @Test
    void loadsAsciiDxfReferenceData() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-release-dxf");
        Path dxfFile = tempDir.resolve("cad-referencia.dxf");

        String dxf = """
                0
                SECTION
                2
                HEADER
                9
                $INSUNITS
                70
                6
                0
                ENDSEC
                0
                SECTION
                2
                ENTITIES
                0
                LINE
                8
                Ductos
                10
                0
                20
                0
                11
                100
                21
                0
                0
                POINT
                8
                Pozos
                10
                25
                20
                10
                0
                TEXT
                8
                Rotulos
                10
                10
                20
                5
                1
                Pozo A
                0
                LWPOLYLINE
                8
                Locacion
                70
                1
                10
                0
                20
                0
                10
                0
                20
                50
                10
                50
                20
                50
                10
                50
                20
                0
                0
                ENDSEC
                0
                EOF
                """;

        Files.writeString(dxfFile, dxf, StandardCharsets.UTF_8);
        ShapefileData data = DxfLoader.load(dxfFile.toFile());

        assertEquals(4, data.getFeatureCount());
        assertTrue(data.getAttributeNames().contains("entity_type"));
        assertTrue(data.getMessage().contains("metros"));
    }

    @Test
    void loadsGeoPackageFeatureLayer() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-release-gpkg");
        Path gpkgFile = tempDir.resolve("pozos.gpkg");

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("pozos");
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
        typeBuilder.add("the_geom", Point.class);
        typeBuilder.add("name", String.class);
        typeBuilder.add("codigo", Integer.class);
        var type = typeBuilder.buildFeatureType();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
        featureBuilder.set("the_geom", ReleaseTestSupport.GEOMETRY_FACTORY.createPoint(new Coordinate(-68.123, -38.456)));
        featureBuilder.set("name", "Pozo GPKG");
        featureBuilder.set("codigo", 11);

        FeatureEntry entry = new FeatureEntry();
        entry.setTableName("pozos");
        entry.setIdentifier("Pozos");
        entry.setDescription("Pozos de prueba");
        entry.setGeometryColumn("the_geom");
        entry.setGeometryType(Geometries.POINT);
        entry.setSrid(4326);

        try (GeoPackage geoPackage = new GeoPackage(gpkgFile.toFile())) {
            geoPackage.init();
            geoPackage.add(entry, new ListFeatureCollection(type, List.of(featureBuilder.buildFeature("pozos.1"))));
        }

        List<GeoPackageFeatureInfo> entries = GeoPackageLoader.listFeatureEntries(gpkgFile.toFile());
        GeoPackageLayer layer = new GeoPackageLayer("Pozos", gpkgFile.toString());
        layer.setTableName("pozos");
        ShapefileData data = GeoPackageLoader.loadLayerData(layer);

        assertEquals(1, entries.size());
        assertEquals(1, data.getFeatureCount());
        assertEquals("EPSG:4326", layer.getSourceCRS());
    }

    @Test
    void resolvesDwgWithAssistedConverterWhenAvailable() throws Exception {
        File converter = DwgImportSupport.detectPreferredCadConverter();
        Assumptions.assumeTrue(converter != null, "No hay convertidor CAD local disponible para esta prueba.");

        Path tempDir = Files.createTempDirectory("catgis-release-dwg");
        Path dwg = tempDir.resolve("planta_cad.dwg");
        Path seedDxf = tempDir.resolve("planta_seed.dxf");

        Files.writeString(seedDxf, """
                0
                SECTION
                2
                HEADER
                9
                $INSUNITS
                70
                6
                0
                ENDSEC
                0
                SECTION
                2
                ENTITIES
                0
                LINE
                8
                Trazas
                10
                0
                20
                0
                11
                25
                21
                25
                0
                TEXT
                8
                Rotulos
                10
                4
                20
                4
                1
                Planta CAD
                0
                ENDSEC
                0
                EOF
                """, StandardCharsets.UTF_8);

        Path conversionOut = tempDir.resolve("seed-out");
        Files.createDirectories(conversionOut);
        Process process = new ProcessBuilder(
                converter.getAbsolutePath(),
                tempDir.toFile().getAbsolutePath(),
                conversionOut.toFile().getAbsolutePath(),
                "ACAD2018",
                "DWG",
                "0",
                "0",
                seedDxf.getFileName().toString()
        ).start();
        int exitCode = process.waitFor();
        Path generatedDwg = conversionOut.resolve("planta_seed.dwg");
        assertEquals(0, exitCode);
        assertTrue(Files.exists(generatedDwg));

        Files.move(generatedDwg, dwg);
        Files.deleteIfExists(seedDxf);

        DwgImportSupport.ResolvedCadReference resolved = DwgImportSupport.resolveDwgReference(dwg.toFile(), null, false);
        ShapefileData data = resolved != null ? DxfLoader.load(resolved.dxfFile()) : null;

        assertNotNull(resolved);
        assertTrue(resolved.autoConverted());
        assertEquals(2, data != null ? data.getFeatureCount() : 0);
    }
}
