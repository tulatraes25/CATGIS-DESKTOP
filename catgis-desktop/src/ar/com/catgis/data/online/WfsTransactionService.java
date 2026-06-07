package ar.com.catgis.data.online;

import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.service.EventBus;
import ar.com.catgis.service.EventBus.EventType;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.filter.Filter;
import ar.com.catgis.WfsCapabilitiesService;
import org.geotools.data.DataUtilities;
import org.geotools.filter.text.ecql.ECQL;
import org.locationtech.jts.geom.Geometry;

import java.util.HashMap;
import java.util.Map;

/**
 * WFS-T (Transactional) support — Insert, Update, Delete on WFS layers.
 */
public final class WfsTransactionService {

    private static final String PARAM_URL = "WFSDataStoreFactory:GET_CAPABILITIES_URL";
    private static final String PARAM_TIMEOUT = "WFSDataStoreFactory:TIMEOUT";

    private WfsTransactionService() {}

    private static DataStore openStore(Layer layer) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_URL, WfsCapabilitiesService.buildCapabilitiesUrl(layer.getPath()));
        params.put(PARAM_TIMEOUT, 30000);
        DataStore store = DataStoreFinder.getDataStore(params);
        if (store == null) throw new Exception("No se pudo conectar al servidor WFS.");
        return store;
    }

    /** Resolve the feature type name from a WFS layer. */
    private static String resolveTypeName(Layer layer, DataStore store) throws Exception {
        String[] names = store.getTypeNames();
        if (names == null || names.length == 0) throw new Exception("No hay tipos en el WFS.");
        String layerName = layer.getName();
        for (String n : names) {
            if (layerName.contains(n) || n.contains(layerName)) return n;
        }
        return names[0];
    }

    public static boolean supportsTransactions(Layer layer) {
        DataStore store = null;
        try {
            store = openStore(layer);
            String t = resolveTypeName(layer, store);
            return store.getFeatureSource(t) instanceof SimpleFeatureStore;
        } catch (Exception e) {
            return false;
        } finally {
            if (store != null) {
                try { store.dispose(); } catch (Exception ignored) {}
            }
        }
    }

    public static boolean deleteFeatures(Layer layer, String ecqlFilter) {
        return execute(layer, store -> {
            String typeName = resolveTypeName(layer, store);
            Object fs = store.getFeatureSource(typeName);
            if (!(fs instanceof SimpleFeatureStore sfs)) {
                throw new Exception("El servidor no soporta transacciones (solo lectura).");
            }
            sfs.setTransaction(Transaction.AUTO_COMMIT);
            Filter filter = ECQL.toFilter(ecqlFilter);
            sfs.removeFeatures(filter);
        });
    }

    public static boolean insertFeature(Layer layer, SimpleFeature feature) {
        return execute(layer, store -> {
            String typeName = resolveTypeName(layer, store);
            Object fs = store.getFeatureSource(typeName);
            if (!(fs instanceof SimpleFeatureStore sfs))
                throw new Exception("Servidor solo lectura.");
            sfs.setTransaction(Transaction.AUTO_COMMIT);
            sfs.addFeatures(DataUtilities.collection(feature));
        });
    }

    @FunctionalInterface
    private interface WfsOp { void run(DataStore store) throws Exception; }

    private static boolean execute(Layer layer, WfsOp op) {
        DataStore store = null;
        try {
            store = openStore(layer);
            op.run(store);
            EventBus.emit(EventType.STATUS_MESSAGE, "WFS-T OK: " + layer.getName());
            return true;
        } catch (Exception e) {
            EventBus.emit(EventType.STATUS_MESSAGE, "WFS-T error: " + e.getMessage());
            return false;
        } finally {
            if (store != null) {
                try { store.dispose(); } catch (Exception ignored) {}
            }
        }
    }
}
