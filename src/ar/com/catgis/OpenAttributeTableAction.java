package ar.com.catgis;

import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.Method;
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
        Layer selectedLayer = CatgisDesktopApp.layersPanel.getSelectedLayer();

        if (selectedLayer == null) {
            JOptionPane.showMessageDialog(null, "No hay una capa seleccionada.");
            return;
        }

        openTable(selectedLayer);
    }

    public static void openFieldsConfig(Layer layer) {
        if (layer == null) {
            JOptionPane.showMessageDialog(null, "No hay una capa seleccionada.");
            return;
        }

        ShapefileData data = ensureDataLoaded(layer);
        if (data == null) {
            JOptionPane.showMessageDialog(null, "La capa seleccionada no tiene estructura de campos disponible.");
            return;
        }

        FieldsConfigDialog.open(layer, data);
    }

    public static AttributeTableWindow openTable(Layer layer) {
        if (layer == null) {
            JOptionPane.showMessageDialog(null, "No hay una capa seleccionada.");
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
                JOptionPane.showMessageDialog(null, "La capa seleccionada no tiene tabla de atributos disponible.");
                return null;
            }

            List<SimpleFeature> features = data.getFeatures();
            SimpleFeatureType schema = data.getSchema();
            if ((features == null || features.isEmpty()) && schema == null) {
                JOptionPane.showMessageDialog(null, "La capa no tiene tabla de atributos disponible.");
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
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al abrir la tabla de atributos: " + ex.getMessage(),
                    "Tabla de atributos",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }


    public static void openFieldCalculatorForSelectedLayer() {
        Layer layer = CatgisDesktopApp.layersPanel.getSelectedLayer();
        if (layer == null) {
            JOptionPane.showMessageDialog(null, "No hay una capa seleccionada.");
            return;
        }

        AttributeTableWindow window = openTable(layer);
        if (window != null) {
            window.openFieldCalculator();
        }
    }

    public static void openAssignValueForSelectedLayer() {
        Layer layer = CatgisDesktopApp.layersPanel.getSelectedLayer();
        if (layer == null) {
            JOptionPane.showMessageDialog(null, "No hay una capa seleccionada.");
            return;
        }

        AttributeTableWindow window = openTable(layer);
        if (window != null) {
            window.openAssignValueDialog();
        }
    }

    public static void openQueryBuilderForSelectedLayer() {
        Layer layer = CatgisDesktopApp.layersPanel.getSelectedLayer();
        if (layer == null) {
            JOptionPane.showMessageDialog(null, "No hay una capa seleccionada.");
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
            JOptionPane.showMessageDialog(null, "No hay tablas de atributos abiertas.");
        }
    }

    public static ShapefileData ensureLayerDataAvailable(Layer layer) {
        return ensureDataLoaded(layer);
    }

    private static ShapefileData ensureDataLoaded(Layer layer) {
        ShapefileData data = CatgisDesktopApp.mapPanel.getShapefileData(layer);

        if (data == null) {
            try {
                String path = layer.getPath() != null ? layer.getPath().toLowerCase() : "";

                if (path.endsWith(".shp")) {
                    data = invokeLoader(ShapefileLoader.class, layer.getPath(),
                            new String[]{"load", "loadShapefile", "open", "openShapefile", "read", "readShapefile"});
                } else if (path.endsWith(".geojson") || path.endsWith(".json")) {
                    data = invokeLoader(GeoJsonLoader.class, layer.getPath(),
                            new String[]{"load", "loadGeoJson", "open", "read"});
                } else if (path.endsWith(".kml")) {
                    data = invokeLoader(KmlLoader.class, layer.getPath(),
                            new String[]{"load", "loadKml", "open", "read"});
                }

                if (data != null) {
                    layer.setSourceName(data.getSourceName());
                    layer.setFeatureCount(data.getFeatureCount());
                    CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return data;
    }

    private static ShapefileData invokeLoader(Class<?> clazz, String path, String[] methodNames) throws Exception {
        if (path == null || path.isBlank()) {
            return null;
        }

        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        for (String methodName : methodNames) {
            try {
                Method m = clazz.getMethod(methodName, String.class);
                Object result = m.invoke(null, path);
                if (result instanceof ShapefileData) {
                    return (ShapefileData) result;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        for (String methodName : methodNames) {
            try {
                Method m = clazz.getMethod(methodName, File.class);
                Object result = m.invoke(null, file);
                if (result instanceof ShapefileData) {
                    return (ShapefileData) result;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        return null;
    }
}
