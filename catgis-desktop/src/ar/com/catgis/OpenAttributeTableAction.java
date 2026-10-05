package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class OpenAttributeTableAction extends AbstractAction {

    private static final Map<Layer, AttributeTableWindow> OPEN_WINDOWS = new WeakHashMap<>();

    @Override
    public void actionPerformed(ActionEvent e) {
        openAttributeTable();
    }

    public static void openAttributeTable() {
        Layer selectedLayer = AppContext.getSelectedLayer();

        if (selectedLayer == null) {
            NotificationManager.warn(null, null, "No hay una capa seleccionada.");
            return;
        }

        openTable(selectedLayer);
    }

    public static void openFieldsConfig(Layer layer) {
        if (layer == null) {
            NotificationManager.warn(null, null, "No hay una capa seleccionada.");
            return;
        }

        ShapefileData data = ensureDataLoaded(layer);
        if (data == null) {
            NotificationManager.warn(null, null, "La capa seleccionada no tiene estructura de campos disponible.");
            return;
        }

        FieldsConfigDialog.open(layer, data);
    }

    public static AttributeTableWindow openTable(Layer layer) {
        if (layer == null) {
            NotificationManager.warn(null, null, "No hay una capa seleccionada.");
            return null;
        }

        AttributeTableWindow existing = getOpenWindow(layer);
        if (existing != null) {
            existing.toFront();
            existing.requestFocus();
            return existing;
        }

        try {
            ShapefileData data = ensureDataLoaded(layer);

            if (data == null) {
                NotificationManager.warn(null, null, "La capa seleccionada no tiene tabla de atributos disponible.");
                return null;
            }

            List<SimpleFeature> features = data.getFeatures();
            SimpleFeatureType schema = data.getSchema();
            if ((features == null || features.isEmpty()) && schema == null) {
                NotificationManager.warn(null, null, "La capa no tiene tabla de atributos disponible.");
                return null;
            }

            List<String> columnNames = new ArrayList<>();
            List<Object[]> rows = new ArrayList<>();

            if (schema != null) {
                schema.getAttributeDescriptors().forEach(descriptor -> {
                    String name = descriptor.getLocalName();
                    if (!"the_geom".equalsIgnoreCase(name) && !"geom".equalsIgnoreCase(name)) {
                        FieldConfig config = layer.getOrCreateFieldConfig(
                                name,
                                descriptor.getType() != null && descriptor.getType().getBinding() != null
                                        ? descriptor.getType().getBinding().getSimpleName()
                                        : "String"
                        );
                        if (config.isVisible()) {
                            columnNames.add(name);
                        }
                    }
                });
            } else if (features != null && !features.isEmpty()) {
                SimpleFeature firstFeature = features.get(0);
                List<Property> properties = new ArrayList<>(firstFeature.getProperties());

                for (Property property : properties) {
                    String name = property.getName().toString();
                    if (!"the_geom".equalsIgnoreCase(name) && !"geom".equalsIgnoreCase(name)) {
                        FieldConfig config = layer.getOrCreateFieldConfig(name, "");
                        if (config.isVisible()) {
                            columnNames.add(name);
                        }
                    }
                }
            }

            if (features != null) {
                for (SimpleFeature feature : features) {
                    List<Object> row = new ArrayList<>();
                    for (String columnName : columnNames) {
                        row.add(feature.getAttribute(columnName));
                    }
                    rows.add(row.toArray(new Object[0]));
                }
            }

            AttributeTableWindow window = new AttributeTableWindow(layer, data, columnNames, rows);
            OPEN_WINDOWS.put(layer, window);
            window.setVisible(true);
            return window;

        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al abrir la tabla de atributos para " + layer.getName(), ex);
            AppErrorSupport.showErrorDialog(
                    CatgisDesktopApp.getMainFrameSafe(),
                    "Tabla de atributos",
                    "Error al abrir la tabla de atributos.",
                    ex
            );
            return null;
        }
    }


    public static void openFieldCalculatorForSelectedLayer() {
        Layer layer = AppContext.getSelectedLayer();
        if (layer == null) {
            NotificationManager.warn(null, null, "No hay una capa seleccionada.");
            return;
        }
        if (VectorLayerUtils.isReadOnlyVectorLayer(layer)) {
            NotificationManager.warn(null, null, buildReadOnlyMessage(layer));
            return;
        }

        AttributeTableWindow window = openTable(layer);
        if (window != null) {
            window.openFieldCalculator();
        }
    }

    public static void openAssignValueForSelectedLayer() {
        Layer layer = AppContext.getSelectedLayer();
        if (layer == null) {
            NotificationManager.warn(null, null, "No hay una capa seleccionada.");
            return;
        }
        if (VectorLayerUtils.isReadOnlyVectorLayer(layer)) {
            NotificationManager.warn(null, null, buildReadOnlyMessage(layer));
            return;
        }

        AttributeTableWindow window = openTable(layer);
        if (window != null) {
            window.openAssignValueDialog();
        }
    }

    public static void openQueryBuilderForSelectedLayer() {
        Layer layer = AppContext.getSelectedLayer();
        if (layer == null) {
            NotificationManager.warn(null, null, "No hay una capa seleccionada.");
            return;
        }
        QueryBuilderDialog.open(layer);
    }

    static void unregisterWindow(Layer layer, AttributeTableWindow window) {
        if (layer == null || window == null) {
            return;
        }
        AttributeTableWindow existing = OPEN_WINDOWS.get(layer);
        if (existing == window) {
            OPEN_WINDOWS.remove(layer);
        }
    }

    static void syncSelectionFromMap(Layer layer, List<String> featureIds) {
        if (layer == null) {
            return;
        }
        AttributeTableWindow window = OPEN_WINDOWS.get(layer);
        if (window != null && window.isDisplayable()) {
            window.selectFeatureIds(featureIds);
        }
    }

    static void clearSelectionInOpenTables() {
        Collection<AttributeTableWindow> windows = new ArrayList<>(OPEN_WINDOWS.values());
        for (AttributeTableWindow window : windows) {
            if (window != null && window.isDisplayable()) {
                window.clearMapLinkedSelection();
            }
        }
    }

    public static AttributeTableWindow getOpenWindow(Layer layer) {
        if (layer == null) {
            return null;
        }
        AttributeTableWindow window = OPEN_WINDOWS.get(layer);
        return window != null && window.isDisplayable() ? window : null;
    }

    public static void closeOpenWindow(Layer layer) {
        if (layer == null) {
            return;
        }
        AttributeTableWindow window = OPEN_WINDOWS.remove(layer);
        if (window != null && window.isDisplayable()) {
            window.dispose();
        }
    }

    public static void focusOpenTables() {
        Collection<AttributeTableWindow> windows = new ArrayList<>(OPEN_WINDOWS.values());
        boolean focused = false;
        for (AttributeTableWindow window : windows) {
            if (window == null || !window.isDisplayable()) {
                continue;
            }
            window.setState(JFrame.NORMAL);
            window.setVisible(true);
            window.toFront();
            window.requestFocus();
            focused = true;
        }
        if (!focused) {
            NotificationManager.warn(null, null, "No hay tablas de atributos abiertas.");
        }
    }

    public static ShapefileData ensureLayerDataAvailable(Layer layer) {
        return ensureDataLoaded(layer);
    }

    private static ShapefileData ensureDataLoaded(Layer layer) {
        try {
            return LayerVectorDataSupport.ensureDataLoaded(
                    layer,
                    CatgisDesktopApp.getMainFrameSafe(),
                    "Ingresá la clave para abrir la tabla de la capa PostGIS.",
                    true
            );
        } catch (Exception ex) {
            LayerVectorDataSupport.showLoadFailure(
                    layer,
                    CatgisDesktopApp.getMainFrameSafe(),
                    "Tabla de atributos",
                    "No se pudieron cargar los datos de la capa seleccionada.",
                    ex
            );
            return null;
        }
    }

    private static String buildReadOnlyMessage(Layer layer) {
        String reason = VectorLayerUtils.getReadOnlyVectorLayerReason(layer);
        return !reason.isBlank() ? reason : "La capa seleccionada esta en modo lectura.";
    }
}
