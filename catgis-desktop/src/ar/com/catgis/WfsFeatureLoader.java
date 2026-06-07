package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.api.data.DataStoreFinder;
import org.locationtech.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WfsFeatureLoader {

    private static final String PARAM_GET_CAPABILITIES_URL = "WFSDataStoreFactory:GET_CAPABILITIES_URL";
    private static final String PARAM_TIMEOUT = "WFSDataStoreFactory:TIMEOUT";

    private WfsFeatureLoader() {
    }

    public static ShapefileData loadLayerData(OnlineWfsLayer layer) throws Exception {
        if (layer == null) {
            return null;
        }

        String capabilitiesUrl = WfsCapabilitiesService.buildCapabilitiesUrl(layer.getServiceUrl());
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_GET_CAPABILITIES_URL, capabilitiesUrl);
        params.put(PARAM_TIMEOUT, 12000);

        DataStore store = DataStoreFinder.getDataStore(params);
        if (store == null) {
            throw new IllegalArgumentException("No se pudo crear la conexion WFS para ese servicio.");
        }

        try {
            String typeName = layer.getTypeName();
            if (typeName == null || typeName.isBlank()) {
                String[] available = store.getTypeNames();
                if (available == null || available.length == 0) {
                    throw new IllegalArgumentException("El servicio WFS no ofrece capas vectoriales disponibles.");
                }
                typeName = available[0];
                layer.setTypeName(typeName);
            }

            SimpleFeatureSource source = store.getFeatureSource(typeName);
            Query query = new Query(typeName);
            String requestCrs = CRSDefinitions.normalizeCode(layer.getRequestCrs());
            if (!requestCrs.isBlank()) {
                query.setCoordinateSystemReproject(CRSDefinitions.decode(requestCrs, true));
            }

            org.geotools.data.simple.SimpleFeatureCollection featureCollection = source.getFeatures(query);
            SimpleFeatureType schema = featureCollection.getSchema();
            List<SimpleFeature> features = new ArrayList<>();
            Envelope envelope = null;

            try {
                ReferencedEnvelope bounds = featureCollection.getBounds();
                if (bounds != null && !bounds.isEmpty()) {
                    envelope = new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
                }
            } catch (Exception ignored) {
            }

            try (FeatureIterator<SimpleFeature> it = featureCollection.features()) {
                while (it.hasNext()) {
                    SimpleFeature feature = it.next();
                    features.add(feature);
                    if (envelope == null) {
                        Object geometry = feature.getDefaultGeometry();
                        if (geometry instanceof org.locationtech.jts.geom.Geometry jts && !jts.isEmpty()) {
                            envelope = new Envelope(jts.getEnvelopeInternal());
                        }
                    }
                }
            }

            if (layer.getSourceCRS() == null || layer.getSourceCRS().isBlank()) {
                layer.setSourceCRS(resolveLayerCrs(requestCrs, schema));
            }

            return new ShapefileData(
                    features,
                    envelope,
                    layer.getSourceName() != null && !layer.getSourceName().isBlank() ? layer.getSourceName() : layer.getName(),
                    features.size(),
                    "WFS cargado correctamente en modo lectura.",
                    schema
            );
        } finally {
            store.dispose();
        }
    }

    private static String resolveLayerCrs(String requested, SimpleFeatureType schema) {
        if (requested != null && !requested.isBlank()) {
            return requested;
        }
        if (schema == null || schema.getCoordinateReferenceSystem() == null) {
            return "";
        }
        try {
            return CRSDefinitions.normalizeCode(CRS.toSRS(schema.getCoordinateReferenceSystem(), true));
        } catch (Exception ignored) {
            return "";
        }
    }
}
