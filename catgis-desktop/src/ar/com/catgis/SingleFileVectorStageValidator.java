package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

/** Independent reopen validation for staged single-file vector exports. */
final class SingleFileVectorStageValidator {
    private SingleFileVectorStageValidator() {}

    static void validate(File stagedFile, int expectedFeatures) throws Exception {
        if (stagedFile == null) {
            throw new IOException("El archivo vectorial temporal no existe.");
        }
        String lower = stagedFile.getName().toLowerCase(Locale.ROOT);
        if (lower.endsWith(".geojson") || lower.endsWith(".json")) {
            ShapefileData reloaded = GeoJsonLoader.load(stagedFile);
            if (reloaded == null || reloaded.getFeatureCount() != expectedFeatures) {
                throw new IOException(
                        "El GeoJSON temporal no conserva la cantidad de entidades.");
            }
            return;
        }
        if (lower.endsWith(".dxf")) {
            if (expectedFeatures == 0) {
                String text = Files.readString(stagedFile.toPath(), StandardCharsets.UTF_8);
                if (!text.contains("ENTITIES") || !text.contains("EOF")) {
                    throw new IOException("El DXF temporal esta incompleto.");
                }
                return;
            }
            ShapefileData reloaded = DxfLoader.load(stagedFile);
            if (reloaded == null || reloaded.getFeatureCount() <= 0) {
                throw new IOException("El DXF temporal no contiene geometria valida.");
            }
            return;
        }
        if (lower.endsWith(".kml") || lower.endsWith(".kmz")) {
            ShapefileData reloaded = KmlLoader.load(stagedFile);
            if (reloaded == null
                    || (expectedFeatures > 0 && reloaded.getFeatureCount() <= 0)) {
                throw new IOException("El KML/KMZ temporal no contiene geometria valida.");
            }
            return;
        }
        if (lower.endsWith(".gpx")) {
            GpxImportResult reloaded = GpxLoader.load(stagedFile);
            if (reloaded == null
                    || (expectedFeatures > 0 && !reloaded.hasAnyData())) {
                throw new IOException("El GPX temporal no contiene geometria valida.");
            }
            return;
        }
        throw new IOException(
                "No existe un validador de reapertura para " + stagedFile.getName());
    }
}
