package ar.com.catgis;

import java.io.IOException;

/** Validate existing text-record structure before mutating the active project. */
final class ProjectOpenRecordIntegrity {
    private ProjectOpenRecordIntegrity() {}

    static void requireProjectCrs(String[] fields) throws IOException {
        if (fields == null || fields.length < 2 || fields[1].isBlank()) {
            throw new IOException("El proyecto tiene una definición CRS vacía o incompleta.");
        }
    }

    static void requireMetadata(String[] fields) throws IOException {
        if (fields == null || fields.length < 3 || fields[1].isBlank()) {
            throw new IOException("El proyecto contiene metadatos incompletos.");
        }
    }

    static void requireGroup(String[] fields) throws IOException {
        if (fields == null || fields.length < 2 || fields[1].isBlank()) {
            throw new IOException("El proyecto contiene un grupo sin nombre.");
        }
    }

    static CatmapLayoutItem parseLayoutItem(String line) throws IOException {
        String payload = line.substring("PROJECT_LAYOUT_ITEM|".length()).trim();
        CatmapLayoutItem item = CatmapLayoutItem.decode(payload);
        if (item == null) {
            throw new IOException("El proyecto contiene un elemento CATMAP inválido.");
        }
        return item;
    }

    static CatmapLegendItem parseLegendItem(String line) throws IOException {
        String payload = line.substring("PROJECT_LEGEND_ITEM|".length()).trim();
        CatmapLegendItem item = CatmapLegendItem.decode(payload);
        if (item == null) {
            throw new IOException("El proyecto contiene una leyenda CATMAP inválida.");
        }
        return item;
    }

    static double[] parseView(String line) throws IOException {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 4) {
            throw new IOException("La vista del proyecto debe contener tres números.");
        }
        try {
            double minX = Double.parseDouble(fields[1]);
            double minY = Double.parseDouble(fields[2]);
            double zoom = Double.parseDouble(fields[3]);
            if (!Double.isFinite(minX) || !Double.isFinite(minY)
                    || !Double.isFinite(zoom) || zoom <= 0.0) {
                throw new IOException("La vista del proyecto tiene coordenadas o zoom no finitos.");
            }
            return new double[]{minX, minY, zoom};
        } catch (NumberFormatException invalid) {
            throw new IOException("La vista del proyecto contiene números inválidos.", invalid);
        }
    }

    static void rejectMalformedLayerRecord(String line) throws IOException {
        if (line != null && (line.startsWith("[")
                || line.startsWith("VECTOR|") || line.startsWith("RASTER|")
                || line.startsWith("GEOPACKAGE|") || line.startsWith("SPATIALITE|")
                || line.startsWith("GPX|"))) {
            throw new IOException("El proyecto contiene una capa incompleta o malformada.");
        }
    }
}
