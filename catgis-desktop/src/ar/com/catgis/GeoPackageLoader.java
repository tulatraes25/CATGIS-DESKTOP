package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.data.SimpleFeatureReader;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class GeoPackageLoader {

    private GeoPackageLoader() {
    }

    public static List<GeoPackageFeatureInfo> listFeatureEntries(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("No existe el archivo GeoPackage seleccionado.");
        }

        List<GeoPackageFeatureInfo> entries = new ArrayList<>();
        try (GeoPackage geoPackage = new GeoPackage(file)) {
            geoPackage.init();
            for (FeatureEntry entry : geoPackage.features()) {
                if (entry == null) {
                    continue;
                }
                entries.add(new GeoPackageFeatureInfo(
                        entry.getTableName(),
                        entry.getIdentifier(),
                        entry.getDescription(),
                        entry.getGeometryType() != null ? String.valueOf(entry.getGeometryType()) : "",
                        entry.getSrid() > 0 ? "EPSG:" + entry.getSrid() : ""
                ));
            }
        }
        return entries;
    }

    public static ShapefileData loadLayerData(GeoPackageLayer layer) throws Exception {
        if (layer == null) {
            return null;
        }

        File file = layer.getPath() != null && !layer.getPath().isBlank() ? new File(layer.getPath()) : null;
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("No existe el archivo GeoPackage asociado a la capa.");
        }
        if (layer.getTableName() == null || layer.getTableName().isBlank()) {
            throw new IllegalArgumentException("No se definio la tabla o capa interna del GeoPackage.");
        }

        try (GeoPackage geoPackage = new GeoPackage(file)) {
            geoPackage.init();
            FeatureEntry entry = geoPackage.feature(layer.getTableName());
            if (entry == null) {
                throw new IllegalArgumentException("La capa interna \"" + layer.getTableName() + "\" ya no existe en el GeoPackage.");
            }

            List<SimpleFeature> features = new ArrayList<>();
            SimpleFeatureType schema = null;
            Envelope envelope = null;

            if (entry.getBounds() != null && !entry.getBounds().isEmpty()) {
                envelope = new Envelope(
                        entry.getBounds().getMinX(),
                        entry.getBounds().getMaxX(),
                        entry.getBounds().getMinY(),
                        entry.getBounds().getMaxY()
                );
            }

            try (SimpleFeatureReader reader = geoPackage.reader(entry, Filter.INCLUDE, null)) {
                while (reader.hasNext()) {
                    SimpleFeature feature = reader.next();
                    if (feature == null) {
                        continue;
                    }
                    schema = feature.getFeatureType();
                    SimpleFeature detached = SimpleFeatureBuilder.copy(feature);
                    features.add(detached);
                    if (envelope == null) {
                        Object geomObj = detached.getDefaultGeometry();
                        if (geomObj instanceof Geometry geometry && !geometry.isEmpty()) {
                            envelope = new Envelope(geometry.getEnvelopeInternal());
                        }
                    }
                }
            }

            if ((layer.getSourceCRS() == null || layer.getSourceCRS().isBlank())) {
                layer.setSourceCRS(resolveLayerCrs(entry, schema));
            }
            if (layer.getIdentifier() == null || layer.getIdentifier().isBlank()) {
                layer.setIdentifier(entry.getIdentifier());
            }
            if (layer.getDescription() == null || layer.getDescription().isBlank()) {
                layer.setDescription(entry.getDescription());
            }
            if (layer.getGeometryTypeLabel() == null || layer.getGeometryTypeLabel().isBlank()) {
                layer.setGeometryTypeLabel(entry.getGeometryType() != null ? String.valueOf(entry.getGeometryType()) : "");
            }
            if (layer.getSourceName() == null || layer.getSourceName().isBlank()) {
                layer.setSourceName(file.getName());
            }

            return new ShapefileData(
                    features,
                    envelope,
                    file.getName() + " :: " + layer.getTableName(),
                    features.size(),
                    "GeoPackage cargado correctamente en modo lectura.",
                    schema
            );
        }
    }

    private static String resolveLayerCrs(FeatureEntry entry, SimpleFeatureType schema) {
        if (schema != null && schema.getCoordinateReferenceSystem() != null) {
            try {
                return CRSDefinitions.normalizeCode(org.geotools.referencing.CRS.toSRS(schema.getCoordinateReferenceSystem(), true));
            } catch (Exception ignored) { CatgisLogger.warn("GeoPackageLoader: operation failed", ignored); }
        }
        if (entry != null && entry.getSrid() > 0) {
            return CRSDefinitions.normalizeCode("EPSG:" + entry.getSrid());
        }
        return "";
    }
}
