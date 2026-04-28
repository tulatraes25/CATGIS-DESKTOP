package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GeoprocessingAssistantDialog extends JDialog {

    public static final String OP_BUFFER = "Buffer";
    public static final String OP_DISSOLVE = "Dissolve";
    public static final String OP_CLIP = "Clip";
    public static final String OP_INTERSECTION = "Interseccion";
    public static final String OP_MERGE = "Merge";
    public static final String OP_DIFFERENCE = "Diferencia";
    public static final String OP_SPATIAL_JOIN = "Spatial Join";
    public static final String OP_UNION = "Union geometrica";

    private static final String PARAMETER_TEXT_CARD = "text";
    private static final String PARAMETER_SPATIAL_JOIN_CARD = "spatial-join";
    private static final String SPATIAL_JOIN_MODE_FIRST = "Primera coincidencia";
    private static final String SPATIAL_JOIN_MODE_SUMMARY = "Resumen util";
    private static final String FIELD_JOIN_COUNT = "join_count";
    private static final String FIELD_JOIN_MODE = "join_mode";
    private static final String FIELD_SRC_A_COUNT = "src_a_cnt";
    private static final String FIELD_SRC_B_COUNT = "src_b_cnt";
    private static final String FIELD_OVERLAY_TYPE = "ov_type";

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final JComboBox<String> comboOperation;
    private final JComboBox<LayerOption> comboLayerA;
    private final JComboBox<LayerOption> comboLayerB;
    private final JTextField txtParameter;
    private final JComboBox<String> comboSpatialJoinMode;
    private final JPanel parameterCardPanel;
    private final CardLayout parameterCardLayout;
    private final JTextField txtOutput;
    private final JLabel lblLayerA;
    private final JLabel lblLayerB;
    private final JLabel lblParameter;
    private final JLabel lblCompatibility;
    private final JTextArea txtDescription;

    public GeoprocessingAssistantDialog(String initialOperation) {
        setTitle("Asistente de geoprocesamiento");
        setModal(false);
        setSize(860, 560);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());
        setLayout(new BorderLayout(10, 10));

        comboOperation = new JComboBox<>(new String[]{
                OP_BUFFER,
                OP_DISSOLVE,
                OP_CLIP,
                OP_INTERSECTION,
                OP_MERGE,
                OP_DIFFERENCE,
                OP_SPATIAL_JOIN,
                OP_UNION
        });
        comboLayerA = new JComboBox<>();
        comboLayerB = new JComboBox<>();
        txtParameter = new JTextField();
        comboSpatialJoinMode = new JComboBox<>(new String[]{SPATIAL_JOIN_MODE_SUMMARY, SPATIAL_JOIN_MODE_FIRST});
        parameterCardLayout = new CardLayout();
        parameterCardPanel = new JPanel(parameterCardLayout);
        parameterCardPanel.add(txtParameter, PARAMETER_TEXT_CARD);
        parameterCardPanel.add(comboSpatialJoinMode, PARAMETER_SPATIAL_JOIN_CARD);
        txtOutput = new JTextField();
        lblLayerA = new JLabel("Capa A:");
        lblLayerB = new JLabel("Capa B:");
        lblParameter = new JLabel("Parametro:");
        lblCompatibility = new JLabel(" ");
        txtDescription = new JTextArea();

        txtDescription.setEditable(false);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setOpaque(false);
        txtDescription.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel north = new JPanel(new BorderLayout());
        north.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JLabel title = new JLabel("Asistente de geoprocesamiento");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        north.add(title, BorderLayout.NORTH);
        north.add(txtDescription, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, gbc, row++, "Operacion:", comboOperation);
        addRow(form, gbc, row++, lblLayerA, comboLayerA);
        addRow(form, gbc, row++, lblLayerB, comboLayerB);
        addRow(form, gbc, row++, lblParameter, parameterCardPanel);
        addRow(form, gbc, row++, "Salida:", txtOutput);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        form.add(lblCompatibility, gbc);

        add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        JButton refreshButton = new JButton("Actualizar capas");
        refreshButton.addActionListener(e -> reloadLayerOptions());
        JButton executeButton = new JButton("Ejecutar");
        executeButton.addActionListener(e -> executeOperation());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(refreshButton);
        footer.add(executeButton);
        footer.add(closeButton);
        add(footer, BorderLayout.SOUTH);

        comboOperation.addActionListener(e -> refreshOperationUi());
        comboLayerA.addActionListener(e -> refreshOperationUi());
        comboLayerB.addActionListener(e -> refreshOperationUi());

        reloadLayerOptions();
        if (initialOperation != null && !initialOperation.isBlank()) {
            comboOperation.setSelectedItem(initialOperation);
        }
        refreshOperationUi();
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new GeoprocessingAssistantDialog(null).setVisible(true));
    }

    public static void openForOperation(String operation) {
        SwingUtilities.invokeLater(() -> new GeoprocessingAssistantDialog(operation).setVisible(true));
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        addRow(panel, gbc, row, new JLabel(label), field);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, JLabel label, Component field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private void reloadLayerOptions() {
        List<LayerOption> options = new ArrayList<>();
        for (Layer layer : VectorLayerUtils.getVectorLayers()) {
            ShapefileData data = VectorLayerUtils.ensureVectorData(layer);
            if (data == null || data.getSchema() == null) {
                continue;
            }
            options.add(new LayerOption(layer, data, VectorLayerUtils.resolveGeometryFamily(data), VectorLayerUtils.pickLayerCrs(layer, data)));
        }

        LayerOption previousA = (LayerOption) comboLayerA.getSelectedItem();
        LayerOption previousB = (LayerOption) comboLayerB.getSelectedItem();

        comboLayerA.removeAllItems();
        comboLayerB.removeAllItems();
        for (LayerOption option : options) {
            comboLayerA.addItem(option);
            comboLayerB.addItem(option);
        }

        restoreLayerSelection(comboLayerA, options, previousA, 0);
        restoreLayerSelection(comboLayerB, options, previousB, options.size() > 1 ? 1 : 0);
        refreshOperationUi();
    }

    private void restoreLayerSelection(JComboBox<LayerOption> combo,
                                       List<LayerOption> options,
                                       LayerOption previous,
                                       int fallbackIndex) {
        if (options.isEmpty()) {
            return;
        }

        if (previous != null) {
            for (LayerOption option : options) {
                if (option.layer == previous.layer) {
                    combo.setSelectedItem(option);
                    return;
                }
            }
        }

        combo.setSelectedIndex(Math.max(0, Math.min(fallbackIndex, options.size() - 1)));
    }

    private void refreshOperationUi() {
        String operation = getSelectedOperation();
        LayerOption layerA = getLayerOption(comboLayerA);
        LayerOption layerB = getLayerOption(comboLayerB);

        boolean needsLayerB = needsSecondLayer(operation);
        boolean needsParameter = needsParameter(operation);

        comboLayerB.setEnabled(needsLayerB);
        lblLayerB.setEnabled(needsLayerB);
        parameterCardPanel.setVisible(needsParameter);
        txtParameter.setEnabled(needsParameter);
        comboSpatialJoinMode.setEnabled(needsParameter);
        lblParameter.setEnabled(needsParameter);

        if (OP_BUFFER.equals(operation)) {
            lblParameter.setText("Distancia:");
            parameterCardLayout.show(parameterCardPanel, PARAMETER_TEXT_CARD);
            if (txtParameter.getText().isBlank()) {
                txtParameter.setText("100");
            }
        } else if (OP_DISSOLVE.equals(operation)) {
            lblParameter.setText("Campo agrupador:");
            parameterCardLayout.show(parameterCardPanel, PARAMETER_TEXT_CARD);
        } else if (OP_SPATIAL_JOIN.equals(operation)) {
            lblParameter.setText("Modo:");
            parameterCardLayout.show(parameterCardPanel, PARAMETER_SPATIAL_JOIN_CARD);
        } else {
            lblParameter.setText("Parametro:");
            parameterCardLayout.show(parameterCardPanel, PARAMETER_TEXT_CARD);
            if (!needsParameter) {
                txtParameter.setText("");
            }
        }

        txtDescription.setText(resolveOperationDescription(operation));
        lblCompatibility.setText(resolveCompatibilityMessage(operation, layerA, layerB));

        if (txtOutput.getText().isBlank() || txtOutput.getText().startsWith("resultado_")) {
            txtOutput.setText(buildDefaultOutputName(operation, layerA, layerB));
        }
    }

    private String getSelectedOperation() {
        Object selected = comboOperation.getSelectedItem();
        return selected != null ? selected.toString() : OP_BUFFER;
    }

    private LayerOption getLayerOption(JComboBox<LayerOption> combo) {
        Object selected = combo.getSelectedItem();
        return selected instanceof LayerOption ? (LayerOption) selected : null;
    }

    private boolean needsSecondLayer(String operation) {
        return OP_CLIP.equals(operation)
                || OP_INTERSECTION.equals(operation)
                || OP_MERGE.equals(operation)
                || OP_DIFFERENCE.equals(operation)
                || OP_SPATIAL_JOIN.equals(operation)
                || OP_UNION.equals(operation);
    }

    private boolean needsParameter(String operation) {
        return OP_BUFFER.equals(operation) || OP_DISSOLVE.equals(operation) || OP_SPATIAL_JOIN.equals(operation);
    }

    private String resolveOperationDescription(String operation) {
        if (OP_BUFFER.equals(operation)) {
            return "Genera areas de influencia alrededor de cada geometria de la capa A. La salida conserva los atributos de origen y produce geometria poligonal.";
        }
        if (OP_DISSOLVE.equals(operation)) {
            return "Agrega entidades por union geometrica. Si indicas un campo agrupador, crea una geometria por cada valor comun. No es lo mismo que Merge.";
        }
        if (OP_CLIP.equals(operation)) {
            return "Recorta la capa A usando una mascara poligonal B. Mantiene los atributos de la capa A. No mezcla tablas.";
        }
        if (OP_INTERSECTION.equals(operation)) {
            return "Calcula la interseccion entre dos capas poligonales. La salida combina atributos de A y B con prefijos para evitar ambiguedades.";
        }
        if (OP_MERGE.equals(operation)) {
            return "Combina entidades de dos capas del mismo tipo geometrico en una sola capa nueva. No hace overlay ni disuelve geometria.";
        }
        if (OP_DIFFERENCE.equals(operation)) {
            return "Resta una mascara poligonal B sobre la capa A. Mantiene los atributos de A y conserva el tipo geometrico cuando es posible.";
        }
        if (OP_SPATIAL_JOIN.equals(operation)) {
            return "Conserva la geometria y atributos de A. Puede copiar la primera coincidencia de B o resumir multiples coincidencias de B con conteo, sumatoria, promedio, minimo, maximo y lista de valores.";
        }
        return "Genera una union poligonal real entre A y B, parte las geometrias donde se superponen y conserva atributos de ambas capas.";
    }

    private String resolveCompatibilityMessage(String operation, LayerOption layerA, LayerOption layerB) {
        if (layerA == null) {
            return "Selecciona una capa vectorial de entrada para continuar.";
        }

        if (OP_BUFFER.equals(operation)) {
            return "Compatible con puntos, lineas y poligonos.";
        }
        if (OP_DISSOLVE.equals(operation)) {
            return "Compatible con cualquier capa vectorial. El campo agrupador es opcional.";
        }
        if (OP_CLIP.equals(operation) || OP_DIFFERENCE.equals(operation)) {
            return "La capa B debe ser poligonal. La capa A puede ser punto, linea o poligono.";
        }
        if (OP_INTERSECTION.equals(operation) || OP_UNION.equals(operation)) {
            return "En esta etapa la operacion requiere dos capas poligonales.";
        }
        if (OP_MERGE.equals(operation)) {
            if (layerB == null) {
                return "Selecciona la segunda capa. Merge requiere dos capas del mismo tipo geometrico.";
            }
            return Objects.equals(layerA.family, layerB.family)
                    ? "Las capas son compatibles para Merge."
                    : "Merge requiere dos capas del mismo tipo geometrico.";
        }
        if (OP_SPATIAL_JOIN.equals(operation)) {
            return "Spatial Join conserva la geometria de A y agrega atributos desde B en modo primera coincidencia o resumen con agregacion numerica y de texto.";
        }
        return "Union geometrica requiere dos capas poligonales y genera piezas de overlay con atributos de A y B.";
    }

    private String buildDefaultOutputName(String operation, LayerOption layerA, LayerOption layerB) {
        String base = "resultado";
        if (layerA != null && layerA.layer != null && layerA.layer.getName() != null && !layerA.layer.getName().isBlank()) {
            base = safeName(layerA.layer.getName());
        }
        String op = safeName(operation).toLowerCase(Locale.ROOT);
        if (layerB != null && needsSecondLayer(operation)) {
            return op + "_" + base + "_" + safeName(layerB.layer.getName());
        }
        return op + "_" + base;
    }

    private void executeOperation() {
        String operation = getSelectedOperation();
        LayerOption layerA = getLayerOption(comboLayerA);
        LayerOption layerB = getLayerOption(comboLayerB);
        String outputName = txtOutput.getText() != null ? txtOutput.getText().trim() : "";

        if (layerA == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una capa A para ejecutar la operacion.");
            return;
        }
        if (needsSecondLayer(operation) && layerB == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una capa B para ejecutar la operacion.");
            return;
        }
        if (outputName.isBlank()) {
            JOptionPane.showMessageDialog(this, "Indica un nombre de salida.");
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            ShapefileData result;
            switch (operation) {
                case OP_BUFFER:
                    double distance = parseDistance(txtParameter.getText());
                    result = bufferLayer(layerA, distance, outputName);
                    break;
                case OP_DISSOLVE:
                    result = dissolveLayer(layerA, txtParameter.getText(), outputName);
                    break;
                case OP_CLIP:
                    result = clipLayer(layerA, layerB, outputName);
                    break;
                case OP_INTERSECTION:
                    result = intersectionLayers(layerA, layerB, outputName);
                    break;
                case OP_MERGE:
                    result = mergeLayers(layerA, layerB, outputName);
                    break;
                case OP_DIFFERENCE:
                    result = differenceLayer(layerA, layerB, outputName);
                    break;
                case OP_SPATIAL_JOIN:
                    result = spatialJoin(layerA, layerB, outputName, getSpatialJoinMode());
                    break;
                case OP_UNION:
                    result = unionLayers(layerA, layerB, outputName);
                    break;
                default:
                    throw new IllegalStateException("Operacion no soportada: " + operation);
            }

            if (result == null || result.getFeatureCollection() == null) {
                JOptionPane.showMessageDialog(this, "No se obtuvo un resultado valido.");
                return;
            }

            String outputCrs = VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data);
            VectorLayerUtils.addResultLayer(outputName, result, layerA.layer, outputCrs, "");
            JOptionPane.showMessageDialog(this, "Proceso completado. Se agrego la capa de resultado al proyecto.");
        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al ejecutar la operacion de geoprocesamiento " + operation, ex);
            AppErrorSupport.showErrorDialog(this, "Geoprocesamiento", "Error al ejecutar la operacion.", ex);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private double parseDistance(String text) {
        String value = text != null ? text.trim().replace(',', '.') : "";
        if (value.isBlank()) {
            throw new IllegalArgumentException("Debes indicar una distancia numerica.");
        }
        double parsed = Double.parseDouble(value);
        if (parsed == 0d) {
            throw new IllegalArgumentException("La distancia no puede ser 0.");
        }
        return parsed;
    }

    private String getSpatialJoinMode() {
        Object selected = comboSpatialJoinMode.getSelectedItem();
        return selected != null ? selected.toString() : SPATIAL_JOIN_MODE_SUMMARY;
    }

    private ShapefileData bufferLayer(LayerOption layerA, double distance, String outputName) throws Exception {
        SimpleFeatureType sourceType = layerA.data.getSchema();
        SimpleFeatureType resultType = buildSchemaFromSource(
                outputName,
                VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data),
                defaultGeometryBindingForFamily("POLYGON", true),
                sourceType
        );
        List<SimpleFeature> features = new ArrayList<>();
        int index = 1;
        for (SimpleFeature sourceFeature : layerA.data.getFeatures()) {
            Geometry geometry = geometryOf(sourceFeature);
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }
            Geometry buffered = adaptGeometryToFeatureType(geometry.buffer(distance), resultType);
            if (buffered == null || buffered.isEmpty()) {
                continue;
            }
            features.add(copyFeatureWithGeometry(resultType, sourceFeature, buffered, outputName + "." + index++));
        }
        return buildResultData(outputName, features, resultType);
    }

    private ShapefileData dissolveLayer(LayerOption layerA, String fieldName, String outputName) throws Exception {
        String trimmedField = fieldName != null ? fieldName.trim() : "";
        AttributeDescriptor groupDescriptor = findAttributeDescriptor(layerA.data.getSchema(), trimmedField);

        Map<Object, List<Geometry>> grouped = new LinkedHashMap<>();
        for (SimpleFeature feature : layerA.data.getFeatures()) {
            Geometry geometry = geometryOf(feature);
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }
            Object key = groupDescriptor != null ? feature.getAttribute(groupDescriptor.getLocalName()) : "__all__";
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(geometry);
        }

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(outputName));
        applyCrs(builder, VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data));
        builder.add("the_geom", defaultGeometryBindingForFamily(layerA.family, true));
        if (groupDescriptor != null) {
            builder.add(groupDescriptor.getLocalName(), groupDescriptor.getType().getBinding());
        }
        builder.add("conteo", Integer.class);
        SimpleFeatureType resultType = builder.buildFeatureType();

        List<SimpleFeature> resultFeatures = new ArrayList<>();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(resultType);
        int index = 1;
        for (Map.Entry<Object, List<Geometry>> entry : grouped.entrySet()) {
            Geometry union = adaptGeometryToFeatureType(UnaryUnionOp.union(entry.getValue()), resultType);
            if (union == null || union.isEmpty()) {
                continue;
            }
            featureBuilder.set("the_geom", union);
            if (groupDescriptor != null) {
                featureBuilder.set(groupDescriptor.getLocalName(), entry.getKey());
            }
            featureBuilder.set("conteo", entry.getValue().size());
            resultFeatures.add(featureBuilder.buildFeature(outputName + "." + index++));
            featureBuilder.reset();
        }

        return buildResultData(outputName, resultFeatures, resultType);
    }

    private ShapefileData clipLayer(LayerOption layerA, LayerOption layerB, String outputName) throws Exception {
        ensurePolygonLayer(layerB, "Clip");
        Geometry mask = buildUnionGeometry(layerB, VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data));
        if (mask == null || mask.isEmpty()) {
            throw new IllegalArgumentException("La capa mascara no tiene geometria util.");
        }

        String family = VectorLayerUtils.resolveGeometryFamily(layerA.data);
        SimpleFeatureType resultType = buildSchemaFromSource(
                outputName,
                VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data),
                defaultGeometryBindingForFamily(family, true),
                layerA.data.getSchema()
        );
        List<SimpleFeature> resultFeatures = new ArrayList<>();
        int index = 1;

        for (SimpleFeature feature : layerA.data.getFeatures()) {
            Geometry geometry = geometryOf(feature);
            if (geometry == null || geometry.isEmpty() || !geometry.intersects(mask)) {
                continue;
            }
            Geometry clipped = normalizeGeometryToFamily(geometry.intersection(mask), family);
            if (clipped == null || clipped.isEmpty()) {
                continue;
            }
            resultFeatures.add(copyFeatureWithGeometry(resultType, feature, clipped, outputName + "." + index++));
        }

        return buildResultData(outputName, resultFeatures, resultType);
    }

    private ShapefileData differenceLayer(LayerOption layerA, LayerOption layerB, String outputName) throws Exception {
        ensurePolygonLayer(layerB, "Diferencia");
        Geometry mask = buildUnionGeometry(layerB, VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data));
        if (mask == null || mask.isEmpty()) {
            throw new IllegalArgumentException("La capa mascara no tiene geometria util.");
        }

        String family = VectorLayerUtils.resolveGeometryFamily(layerA.data);
        SimpleFeatureType resultType = buildSchemaFromSource(
                outputName,
                VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data),
                defaultGeometryBindingForFamily(family, true),
                layerA.data.getSchema()
        );
        List<SimpleFeature> resultFeatures = new ArrayList<>();
        int index = 1;

        for (SimpleFeature feature : layerA.data.getFeatures()) {
            Geometry geometry = geometryOf(feature);
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }
            Geometry diff = normalizeGeometryToFamily(geometry.difference(mask), family);
            if (diff == null || diff.isEmpty()) {
                continue;
            }
            resultFeatures.add(copyFeatureWithGeometry(resultType, feature, diff, outputName + "." + index++));
        }

        return buildResultData(outputName, resultFeatures, resultType);
    }

    private ShapefileData intersectionLayers(LayerOption layerA, LayerOption layerB, String outputName) throws Exception {
        ensurePolygonLayer(layerA, "Interseccion");
        ensurePolygonLayer(layerB, "Interseccion");

        Map<String, String> aFieldMap = new LinkedHashMap<>();
        Map<String, String> bFieldMap = new LinkedHashMap<>();
        SimpleFeatureType resultType = buildCombinedSchema(
                outputName,
                VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data),
                defaultGeometryBindingForFamily("POLYGON", true),
                layerA.data.getSchema(),
                "a",
                aFieldMap,
                layerB.data.getSchema(),
                "b",
                bFieldMap,
                false
        );

        List<SimpleFeature> resultFeatures = new ArrayList<>();
        int index = 1;
        String targetCrs = VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data);

        for (SimpleFeature featureA : layerA.data.getFeatures()) {
            Geometry geometryA = geometryOf(featureA);
            if (geometryA == null || geometryA.isEmpty()) {
                continue;
            }
            for (SimpleFeature featureB : layerB.data.getFeatures()) {
                Geometry geometryB = reprojectGeometry(geometryOf(featureB), layerB.crsCode, targetCrs);
                if (geometryB == null || geometryB.isEmpty() || !geometryA.intersects(geometryB)) {
                    continue;
                }
                Geometry intersection = normalizeGeometryToFamily(geometryA.intersection(geometryB), "POLYGON");
                if (intersection == null || intersection.isEmpty()) {
                    continue;
                }

                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(resultType);
                builder.set("the_geom", adaptGeometryToFeatureType(intersection, resultType));
                fillPrefixedAttributes(builder, aFieldMap, featureA);
                fillPrefixedAttributes(builder, bFieldMap, featureB);
                resultFeatures.add(builder.buildFeature(outputName + "." + index++));
            }
        }

        return buildResultData(outputName, resultFeatures, resultType);
    }

    private ShapefileData mergeLayers(LayerOption layerA, LayerOption layerB, String outputName) throws Exception {
        if (!Objects.equals(layerA.family, layerB.family)) {
            throw new IllegalArgumentException("Merge requiere dos capas del mismo tipo geometrico.");
        }

        Map<String, String> aFieldMap = new LinkedHashMap<>();
        Map<String, String> bFieldMap = new LinkedHashMap<>();
        SimpleFeatureType resultType = buildCombinedSchema(
                outputName,
                VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data),
                defaultGeometryBindingForFamily(layerA.family, true),
                layerA.data.getSchema(),
                "a",
                aFieldMap,
                layerB.data.getSchema(),
                "b",
                bFieldMap,
                false
        );

        List<SimpleFeature> resultFeatures = new ArrayList<>();
        int index = 1;
        String targetCrs = VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data);

        for (SimpleFeature featureA : layerA.data.getFeatures()) {
            Geometry geometry = geometryOf(featureA);
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(resultType);
            builder.set("the_geom", adaptGeometryToFeatureType(geometry.copy(), resultType));
            fillPrefixedAttributes(builder, aFieldMap, featureA);
            resultFeatures.add(builder.buildFeature(outputName + "." + index++));
        }

        for (SimpleFeature featureB : layerB.data.getFeatures()) {
            Geometry geometry = reprojectGeometry(geometryOf(featureB), layerB.crsCode, targetCrs);
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(resultType);
            builder.set("the_geom", adaptGeometryToFeatureType(geometry, resultType));
            fillPrefixedAttributes(builder, bFieldMap, featureB);
            resultFeatures.add(builder.buildFeature(outputName + "." + index++));
        }

        return buildResultData(outputName, resultFeatures, resultType);
    }

    private ShapefileData spatialJoin(LayerOption layerA, LayerOption layerB, String outputName, String joinMode) throws Exception {
        String targetCrs = VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data);
        boolean summaryMode = SPATIAL_JOIN_MODE_SUMMARY.equalsIgnoreCase(joinMode);
        Map<String, SpatialJoinFieldSpec> joinFieldSpecs = new LinkedHashMap<>();
        SimpleFeatureType resultType = buildSpatialJoinSchema(
                outputName,
                targetCrs,
                defaultGeometryBindingForFamily(layerA.family, true),
                layerA.data.getSchema(),
                layerB.data.getSchema(),
                joinFieldSpecs,
                summaryMode
        );

        List<FeatureGeometryRef> referencesB = collectFeatureGeometryRefs(layerB, targetCrs);
        List<SimpleFeature> resultFeatures = new ArrayList<>();
        int index = 1;

        for (SimpleFeature featureA : layerA.data.getFeatures()) {
            Geometry geometryA = geometryOf(featureA);
            if (geometryA == null || geometryA.isEmpty()) {
                continue;
            }

            List<FeatureGeometryRef> matches = findMatchingFeatureRefs(geometryA, referencesB);
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(resultType);
            builder.set("the_geom", adaptGeometryToFeatureType(geometryA.copy(), resultType));
            fillDirectAttributes(builder, layerA.data.getSchema(), featureA);
            populateSpatialJoinAttributes(builder, joinFieldSpecs, matches, summaryMode);
            builder.set(FIELD_JOIN_COUNT, matches.size());
            builder.set(FIELD_JOIN_MODE, summaryMode ? "SUMMARY" : "FIRST");
            resultFeatures.add(builder.buildFeature(outputName + "." + index++));
        }

        return buildResultData(outputName, resultFeatures, resultType);
    }

    private ShapefileData unionLayers(LayerOption layerA, LayerOption layerB, String outputName) throws Exception {
        ensurePolygonLayer(layerA, "Union geometrica");
        ensurePolygonLayer(layerB, "Union geometrica");

        String targetCrs = VectorLayerUtils.pickLayerCrs(layerA.layer, layerA.data);
        Map<String, String> aFieldMap = new LinkedHashMap<>();
        Map<String, String> bFieldMap = new LinkedHashMap<>();
        SimpleFeatureType resultType = buildUnionOverlaySchema(
                outputName,
                targetCrs,
                layerA.data.getSchema(),
                aFieldMap,
                layerB.data.getSchema(),
                bFieldMap
        );

        List<FeatureGeometryRef> referencesA = collectFeatureGeometryRefs(layerA, targetCrs);
        List<FeatureGeometryRef> referencesB = collectFeatureGeometryRefs(layerB, targetCrs);
        List<Polygon> overlayPieces = collectUnionOverlayPieces(referencesA, referencesB);
        if (overlayPieces.isEmpty()) {
            throw new IllegalArgumentException("La union no genero geometrias utiles.");
        }

        List<SimpleFeature> resultFeatures = new ArrayList<>();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(resultType);
        int index = 1;
        for (Polygon polygon : overlayPieces) {
            if (polygon == null || polygon.isEmpty()) {
                continue;
            }

            List<FeatureGeometryRef> matchesA = findMatchingFeatureRefs(polygon, referencesA);
            List<FeatureGeometryRef> matchesB = findMatchingFeatureRefs(polygon, referencesB);
            if (matchesA.isEmpty() && matchesB.isEmpty()) {
                continue;
            }

            featureBuilder.reset();
            featureBuilder.set("the_geom", adaptGeometryToFeatureType(polygon, resultType));
            if (!matchesA.isEmpty()) {
                fillPrefixedAttributes(featureBuilder, aFieldMap, matchesA.get(0).feature());
            }
            if (!matchesB.isEmpty()) {
                fillPrefixedAttributes(featureBuilder, bFieldMap, matchesB.get(0).feature());
            }
            featureBuilder.set(FIELD_SRC_A_COUNT, matchesA.size());
            featureBuilder.set(FIELD_SRC_B_COUNT, matchesB.size());
            featureBuilder.set(FIELD_OVERLAY_TYPE, resolveOverlayType(matchesA.size(), matchesB.size()));
            resultFeatures.add(featureBuilder.buildFeature(outputName + "." + index++));
        }

        return buildResultData(outputName, resultFeatures, resultType);
    }

    private void ensurePolygonLayer(LayerOption option, String operationName) {
        if (option == null || !"POLYGON".equals(option.family)) {
            throw new IllegalArgumentException(operationName + " requiere una capa poligonal.");
        }
    }

    private Geometry buildUnionGeometry(LayerOption layer, String targetCrs) throws Exception {
        return UnaryUnionOp.union(collectLayerGeometries(layer, targetCrs));
    }

    private List<Geometry> collectLayerGeometries(LayerOption option, String targetCrs) throws Exception {
        List<Geometry> geometries = new ArrayList<>();
        if (option == null || option.data == null || option.data.getFeatures() == null) {
            return geometries;
        }
        for (SimpleFeature feature : option.data.getFeatures()) {
            Geometry geometry = reprojectGeometry(geometryOf(feature), option.crsCode, targetCrs);
            if (geometry != null && !geometry.isEmpty()) {
                geometries.add(geometry);
            }
        }
        return geometries;
    }

    private List<FeatureGeometryRef> collectFeatureGeometryRefs(LayerOption option, String targetCrs) throws Exception {
        List<FeatureGeometryRef> refs = new ArrayList<>();
        if (option == null || option.data == null || option.data.getFeatures() == null) {
            return refs;
        }
        for (SimpleFeature feature : option.data.getFeatures()) {
            Geometry geometry = reprojectGeometry(geometryOf(feature), option.crsCode, targetCrs);
            if (geometry != null && !geometry.isEmpty()) {
                refs.add(new FeatureGeometryRef(feature, geometry));
            }
        }
        return refs;
    }

    private List<FeatureGeometryRef> findMatchingFeatureRefs(Geometry targetGeometry, List<FeatureGeometryRef> candidates) {
        List<FeatureGeometryRef> matches = new ArrayList<>();
        if (targetGeometry == null || targetGeometry.isEmpty() || candidates == null || candidates.isEmpty()) {
            return matches;
        }

        Point interiorPoint = targetGeometry.getInteriorPoint();
        for (FeatureGeometryRef candidate : candidates) {
            if (candidate == null || candidate.geometry() == null || candidate.geometry().isEmpty()) {
                continue;
            }
            Geometry candidateGeometry = candidate.geometry();
            if (!targetGeometry.getEnvelopeInternal().intersects(candidateGeometry.getEnvelopeInternal())) {
                continue;
            }
            if (interiorPoint != null && candidateGeometry.covers(interiorPoint)) {
                matches.add(candidate);
                continue;
            }

            if (targetGeometry.getDimension() == 0) {
                if (candidateGeometry.intersects(targetGeometry)) {
                    matches.add(candidate);
                }
                continue;
            }

            try {
                Geometry overlap = candidateGeometry.intersection(targetGeometry);
                if (overlap == null || overlap.isEmpty()) {
                    continue;
                }
                if ((targetGeometry.getDimension() >= 2 && overlap.getArea() > 1e-9d)
                        || (targetGeometry.getDimension() == 1 && overlap.getLength() > 1e-9d)) {
                    matches.add(candidate);
                }
            } catch (Exception ignored) {
            }
        }
        return matches;
    }

    private SimpleFeatureType buildSpatialJoinSchema(String outputName,
                                                     String crsCode,
                                                     Class<? extends Geometry> geometryClass,
                                                     SimpleFeatureType typeA,
                                                     SimpleFeatureType typeB,
                                                     Map<String, SpatialJoinFieldSpec> joinFieldSpecs,
                                                     boolean summaryMode) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(outputName));
        applyCrs(builder, crsCode);
        builder.add("the_geom", geometryClass);

        Set<String> usedNames = new LinkedHashSet<>();
        usedNames.add("the_geom");
        usedNames.add(FIELD_JOIN_COUNT);
        usedNames.add(FIELD_JOIN_MODE);

        if (typeA != null) {
            for (AttributeDescriptor descriptor : typeA.getAttributeDescriptors()) {
                if (descriptor instanceof GeometryDescriptor) {
                    continue;
                }
                String outputNameField = uniqueFieldName(descriptor.getLocalName(), usedNames);
                builder.add(outputNameField, descriptor.getType().getBinding());
            }
        }

        if (typeB != null) {
            for (AttributeDescriptor descriptor : typeB.getAttributeDescriptors()) {
                if (descriptor instanceof GeometryDescriptor) {
                    continue;
                }
                Class<?> binding = descriptor.getType() != null ? descriptor.getType().getBinding() : String.class;
                boolean numeric = isNumericBinding(binding);
                if (summaryMode && numeric) {
                    addSpatialJoinField(builder, usedNames, joinFieldSpecs, descriptor.getLocalName(), "sum_", Double.class, true, SpatialJoinAggregation.SUM);
                    addSpatialJoinField(builder, usedNames, joinFieldSpecs, descriptor.getLocalName(), "avg_", Double.class, true, SpatialJoinAggregation.AVG);
                    addSpatialJoinField(builder, usedNames, joinFieldSpecs, descriptor.getLocalName(), "min_", Double.class, true, SpatialJoinAggregation.MIN);
                    addSpatialJoinField(builder, usedNames, joinFieldSpecs, descriptor.getLocalName(), "max_", Double.class, true, SpatialJoinAggregation.MAX);
                } else if (summaryMode) {
                    addSpatialJoinField(builder, usedNames, joinFieldSpecs, descriptor.getLocalName(), "vals_", String.class, false, SpatialJoinAggregation.VALUES);
                } else {
                    Class<?> outputBinding = binding != null ? binding : String.class;
                    addSpatialJoinField(builder, usedNames, joinFieldSpecs, descriptor.getLocalName(), "j_", outputBinding, numeric, SpatialJoinAggregation.FIRST);
                }
            }
        }

        builder.add(FIELD_JOIN_COUNT, Integer.class);
        builder.add(FIELD_JOIN_MODE, String.class);
        return builder.buildFeatureType();
    }

    private void fillDirectAttributes(SimpleFeatureBuilder builder, SimpleFeatureType type, SimpleFeature feature) {
        if (builder == null || type == null || feature == null) {
            return;
        }
        for (AttributeDescriptor descriptor : type.getAttributeDescriptors()) {
            if (descriptor instanceof GeometryDescriptor) {
                continue;
            }
            builder.set(descriptor.getLocalName(), feature.getAttribute(descriptor.getLocalName()));
        }
    }

    private void populateSpatialJoinAttributes(SimpleFeatureBuilder builder,
                                               Map<String, SpatialJoinFieldSpec> joinFieldSpecs,
                                               List<FeatureGeometryRef> matches,
                                               boolean summaryMode) {
        if (builder == null || joinFieldSpecs == null || joinFieldSpecs.isEmpty()) {
            return;
        }

        for (Map.Entry<String, SpatialJoinFieldSpec> entry : joinFieldSpecs.entrySet()) {
            String targetField = entry.getKey();
            SpatialJoinFieldSpec spec = entry.getValue();
            Object value = summaryMode
                    ? summarizeJoinValues(matches, spec)
                    : firstJoinValue(matches, spec);
            builder.set(targetField, value);
        }
    }

    private Object firstJoinValue(List<FeatureGeometryRef> matches, SpatialJoinFieldSpec spec) {
        if (matches == null || matches.isEmpty() || spec == null) {
            return null;
        }
        return matches.get(0).feature().getAttribute(spec.sourceField());
    }

    private Object summarizeJoinValues(List<FeatureGeometryRef> matches, SpatialJoinFieldSpec spec) {
        if (matches == null || matches.isEmpty() || spec == null) {
            return null;
        }
        if (spec.aggregation() == SpatialJoinAggregation.VALUES) {
            LinkedHashSet<String> values = new LinkedHashSet<>();
            for (FeatureGeometryRef match : matches) {
                Object value = match.feature().getAttribute(spec.sourceField());
                if (value != null && !String.valueOf(value).isBlank()) {
                    values.add(String.valueOf(value));
                }
            }
            return values.isEmpty() ? null : String.join(" | ", values);
        }

        double sum = 0d;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        int count = 0;
        for (FeatureGeometryRef match : matches) {
            Object value = match.feature().getAttribute(spec.sourceField());
            Double numericValue = extractNumericValue(value);
            if (numericValue == null) {
                continue;
            }
            double current = numericValue;
            sum += current;
            min = Math.min(min, current);
            max = Math.max(max, current);
            count++;
        }
        if (count == 0) {
            return null;
        }

        return switch (spec.aggregation()) {
            case SUM -> sum;
            case AVG -> sum / count;
            case MIN -> min;
            case MAX -> max;
            default -> null;
        };
    }

    private void addSpatialJoinField(SimpleFeatureTypeBuilder builder,
                                     Set<String> usedNames,
                                     Map<String, SpatialJoinFieldSpec> joinFieldSpecs,
                                     String sourceField,
                                     String prefix,
                                     Class<?> outputBinding,
                                     boolean numeric,
                                     SpatialJoinAggregation aggregation) {
        String outputNameField = uniqueFieldName(prefix + sourceField, usedNames, 10);
        builder.add(outputNameField, outputBinding);
        joinFieldSpecs.put(outputNameField, new SpatialJoinFieldSpec(sourceField, outputBinding, numeric, aggregation));
    }

    private Double extractNumericValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value).trim().replace(',', '.'));
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private SimpleFeatureType buildUnionOverlaySchema(String outputName,
                                                      String crsCode,
                                                      SimpleFeatureType typeA,
                                                      Map<String, String> mapA,
                                                      SimpleFeatureType typeB,
                                                      Map<String, String> mapB) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(outputName));
        applyCrs(builder, crsCode);
        builder.add("the_geom", defaultGeometryBindingForFamily("POLYGON", false));

        Set<String> usedNames = new LinkedHashSet<>();
        usedNames.add("the_geom");
        usedNames.add(FIELD_SRC_A_COUNT);
        usedNames.add(FIELD_SRC_B_COUNT);
        usedNames.add(FIELD_OVERLAY_TYPE);
        addAttributesWithPrefix(builder, typeA, "a", usedNames, mapA);
        addAttributesWithPrefix(builder, typeB, "b", usedNames, mapB);
        builder.add(FIELD_SRC_A_COUNT, Integer.class);
        builder.add(FIELD_SRC_B_COUNT, Integer.class);
        builder.add(FIELD_OVERLAY_TYPE, String.class);
        return builder.buildFeatureType();
    }

    private List<Polygon> collectUnionOverlayPieces(List<FeatureGeometryRef> referencesA,
                                                    List<FeatureGeometryRef> referencesB) {
        List<Polygon> polygons = new ArrayList<>();
        List<Geometry> boundaries = new ArrayList<>();

        for (FeatureGeometryRef ref : referencesA) {
            Geometry geometry = ref.geometry();
            if (geometry != null && !geometry.isEmpty()) {
                boundaries.add(geometry.getBoundary());
            }
        }
        for (FeatureGeometryRef ref : referencesB) {
            Geometry geometry = ref.geometry();
            if (geometry != null && !geometry.isEmpty()) {
                boundaries.add(geometry.getBoundary());
            }
        }

        if (!boundaries.isEmpty()) {
            try {
                Geometry noded = UnaryUnionOp.union(boundaries);
                Polygonizer polygonizer = new Polygonizer();
                polygonizer.add(noded);
                for (Object candidate : polygonizer.getPolygons()) {
                    if (candidate instanceof Polygon polygon) {
                        Polygon cleaned = cleanOverlayPolygon(polygon);
                        if (cleaned != null && !cleaned.isEmpty()) {
                            polygons.add(cleaned);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (polygons.isEmpty()) {
            List<Geometry> all = new ArrayList<>();
            for (FeatureGeometryRef ref : referencesA) {
                all.add(ref.geometry());
            }
            for (FeatureGeometryRef ref : referencesB) {
                all.add(ref.geometry());
            }
            Geometry union = UnaryUnionOp.union(all);
            Geometry normalized = normalizeGeometryToFamily(union, "POLYGON");
            collectPolygons(normalized, polygons);
        }

        return polygons;
    }

    private Polygon cleanOverlayPolygon(Polygon polygon) {
        if (polygon == null || polygon.isEmpty()) {
            return null;
        }
        try {
            Geometry cleaned = polygon.buffer(0);
            Geometry normalized = normalizeGeometryToFamily(cleaned, "POLYGON");
            if (normalized instanceof Polygon result) {
                return result;
            }
            List<Polygon> parts = new ArrayList<>();
            collectPolygons(normalized, parts);
            if (!parts.isEmpty()) {
                return parts.get(0);
            }
        } catch (Exception ignored) {
        }
        return polygon;
    }

    private String resolveOverlayType(int countA, int countB) {
        if (countA > 0 && countB > 0) {
            return "A+B";
        }
        if (countA > 0) {
            return "A";
        }
        if (countB > 0) {
            return "B";
        }
        return "";
    }

    private boolean isNumericBinding(Class<?> binding) {
        return binding != null
                && (Number.class.isAssignableFrom(binding)
                || int.class.equals(binding)
                || long.class.equals(binding)
                || float.class.equals(binding)
                || double.class.equals(binding)
                || short.class.equals(binding));
    }

    private SimpleFeatureType buildSchemaFromSource(String outputName,
                                                    String crsCode,
                                                    Class<? extends Geometry> geometryClass,
                                                    SimpleFeatureType sourceType) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(outputName));
        applyCrs(builder, crsCode);
        builder.add("the_geom", geometryClass);
        if (sourceType != null) {
            for (AttributeDescriptor descriptor : sourceType.getAttributeDescriptors()) {
                if (descriptor instanceof GeometryDescriptor) {
                    continue;
                }
                builder.add(descriptor.getLocalName(), descriptor.getType().getBinding());
            }
        }
        return builder.buildFeatureType();
    }

    private SimpleFeatureType buildCombinedSchema(String outputName,
                                                  String crsCode,
                                                  Class<? extends Geometry> geometryClass,
                                                  SimpleFeatureType typeA,
                                                  String prefixA,
                                                  Map<String, String> mapA,
                                                  SimpleFeatureType typeB,
                                                  String prefixB,
                                                  Map<String, String> mapB,
                                                  boolean includeJoinCount) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(outputName));
        applyCrs(builder, crsCode);
        builder.add("the_geom", geometryClass);

        Set<String> usedNames = new LinkedHashSet<>();
        usedNames.add("the_geom");

        addAttributesWithPrefix(builder, typeA, prefixA, usedNames, mapA);
        addAttributesWithPrefix(builder, typeB, prefixB, usedNames, mapB);

        if (includeJoinCount) {
            builder.add("join_count", Integer.class);
        }

        return builder.buildFeatureType();
    }

    private void addAttributesWithPrefix(SimpleFeatureTypeBuilder builder,
                                         SimpleFeatureType sourceType,
                                         String prefix,
                                         Set<String> usedNames,
                                         Map<String, String> targetMap) {
        if (sourceType == null) {
            return;
        }
        for (AttributeDescriptor descriptor : sourceType.getAttributeDescriptors()) {
            if (descriptor instanceof GeometryDescriptor) {
                continue;
            }
            String outputName = uniqueFieldName(prefix + "_" + descriptor.getLocalName(), usedNames, 10);
            builder.add(outputName, descriptor.getType().getBinding());
            targetMap.put(outputName, descriptor.getLocalName());
        }
    }

    private String uniqueFieldName(String base, Set<String> usedNames) {
        return uniqueFieldName(base, usedNames, 24);
    }

    private String uniqueFieldName(String base, Set<String> usedNames, int maxLength) {
        String safe = safeFieldName(base, maxLength);
        String candidate = safe;
        int suffix = 2;
        while (usedNames.contains(candidate)) {
            String suffixText = "_" + suffix++;
            int allowedBaseLength = Math.max(1, maxLength - suffixText.length());
            candidate = safeFieldName(base, allowedBaseLength) + suffixText;
        }
        usedNames.add(candidate);
        return candidate;
    }

    private void fillPrefixedAttributes(SimpleFeatureBuilder builder,
                                        Map<String, String> fieldMap,
                                        SimpleFeature sourceFeature) {
        if (builder == null || fieldMap == null || sourceFeature == null) {
            return;
        }
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            builder.set(entry.getKey(), sourceFeature.getAttribute(entry.getValue()));
        }
    }

    private SimpleFeature copyFeatureWithGeometry(SimpleFeatureType targetType,
                                                  SimpleFeature sourceFeature,
                                                  Geometry geometry,
                                                  String featureId) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(targetType);
        builder.set("the_geom", adaptGeometryToFeatureType(geometry, targetType));
        if (sourceFeature != null && targetType != null) {
            for (AttributeDescriptor descriptor : targetType.getAttributeDescriptors()) {
                if (descriptor instanceof GeometryDescriptor) {
                    continue;
                }
                builder.set(descriptor.getLocalName(), sourceFeature.getAttribute(descriptor.getLocalName()));
            }
        }
        return builder.buildFeature(featureId);
    }

    private ShapefileData buildResultData(String outputName,
                                          List<SimpleFeature> features,
                                          SimpleFeatureType resultType) {
        SimpleFeatureType normalizedType = resolveNormalizedResultType(outputName, resultType, features);
        List<SimpleFeature> normalizedFeatures = rebuildFeaturesWithTargetType(features, normalizedType);

        Geometry envelopeGeometry = null;
        for (SimpleFeature feature : normalizedFeatures) {
            Geometry geometry = geometryOf(feature);
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }
            envelopeGeometry = envelopeGeometry == null ? geometry.getEnvelope() : envelopeGeometry.union(geometry.getEnvelope());
        }

        org.locationtech.jts.geom.Envelope envelope = null;
        if (envelopeGeometry != null) {
            envelope = envelopeGeometry.getEnvelopeInternal();
        } else if (resultType != null) {
            envelope = new org.locationtech.jts.geom.Envelope();
        }

        return new ShapefileData(
                normalizedFeatures,
                envelope,
                outputName,
                normalizedFeatures.size(),
                "Resultado de geoprocesamiento",
                normalizedType
        );
    }

    @SuppressWarnings("unchecked")
    private SimpleFeatureType resolveNormalizedResultType(String outputName,
                                                          SimpleFeatureType resultType,
                                                          List<SimpleFeature> features) {
        if (resultType == null || resultType.getGeometryDescriptor() == null || resultType.getGeometryDescriptor().getType() == null) {
            return resultType;
        }

        Class<? extends Geometry> currentBinding = Geometry.class;
        Class<?> rawBinding = resultType.getGeometryDescriptor().getType().getBinding();
        if (rawBinding != null && Geometry.class.isAssignableFrom(rawBinding)) {
            currentBinding = (Class<? extends Geometry>) rawBinding;
        }

        Class<? extends Geometry> resolvedBinding = VectorLayerUtils.resolveConcreteGeometryBinding(resultType, features);
        if (resolvedBinding == null || Geometry.class.equals(resolvedBinding)) {
            resolvedBinding = currentBinding;
        }

        if (resolvedBinding == null || resolvedBinding.equals(currentBinding)) {
            return resultType;
        }

        try {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName(safeTypeName(outputName));
            if (resultType.getCoordinateReferenceSystem() != null) {
                builder.setCRS(resultType.getCoordinateReferenceSystem());
            }
            builder.add(resultType.getGeometryDescriptor().getLocalName(), resolvedBinding);
            for (AttributeDescriptor descriptor : resultType.getAttributeDescriptors()) {
                if (descriptor instanceof GeometryDescriptor) {
                    continue;
                }
                builder.add(descriptor.getLocalName(), descriptor.getType().getBinding());
            }
            return builder.buildFeatureType();
        } catch (Exception ex) {
            return resultType;
        }
    }

    private List<SimpleFeature> rebuildFeaturesWithTargetType(List<SimpleFeature> sourceFeatures, SimpleFeatureType targetType) {
        if (sourceFeatures == null || targetType == null) {
            return sourceFeatures != null ? sourceFeatures : new ArrayList<>();
        }

        List<SimpleFeature> rebuilt = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(targetType);

        for (SimpleFeature feature : sourceFeatures) {
            if (feature == null) {
                continue;
            }

            builder.reset();
            builder.set(targetType.getGeometryDescriptor().getLocalName(),
                    adaptGeometryToFeatureType(geometryOf(feature), targetType));

            for (AttributeDescriptor descriptor : targetType.getAttributeDescriptors()) {
                if (descriptor instanceof GeometryDescriptor) {
                    continue;
                }
                builder.set(descriptor.getLocalName(), feature.getAttribute(descriptor.getLocalName()));
            }

            rebuilt.add(builder.buildFeature(feature.getID()));
        }

        return rebuilt;
    }

    private AttributeDescriptor findAttributeDescriptor(SimpleFeatureType type, String fieldName) {
        if (type == null || fieldName == null || fieldName.isBlank()) {
            return null;
        }
        for (AttributeDescriptor descriptor : type.getAttributeDescriptors()) {
            if (descriptor instanceof GeometryDescriptor) {
                continue;
            }
            if (descriptor.getLocalName().equalsIgnoreCase(fieldName.trim())) {
                return descriptor;
            }
        }
        return null;
    }

    private Geometry geometryOf(SimpleFeature feature) {
        if (feature == null) {
            return null;
        }
        Object geometry = feature.getDefaultGeometry();
        return geometry instanceof Geometry ? (Geometry) geometry : null;
    }

    @SuppressWarnings("unchecked")
    private Geometry adaptGeometryToFeatureType(Geometry geometry, SimpleFeatureType featureType) {
        if (geometry == null || featureType == null || featureType.getGeometryDescriptor() == null || featureType.getGeometryDescriptor().getType() == null) {
            return geometry;
        }
        Class<?> binding = featureType.getGeometryDescriptor().getType().getBinding();
        if (binding == null || !Geometry.class.isAssignableFrom(binding)) {
            return geometry;
        }
        return VectorLayerUtils.normalizeGeometryForBinding(geometry, (Class<? extends Geometry>) binding);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Geometry> defaultGeometryBindingForFamily(String family, boolean preferMulti) {
        if (family == null) {
            return Geometry.class;
        }
        switch (family.toUpperCase(Locale.ROOT)) {
            case "POINT":
                return preferMulti ? MultiPoint.class : Point.class;
            case "LINE":
                return preferMulti ? MultiLineString.class : LineString.class;
            case "POLYGON":
                return preferMulti ? MultiPolygon.class : Polygon.class;
            default:
                return Geometry.class;
        }
    }

    private Geometry reprojectGeometry(Geometry geometry, String sourceCrs, String targetCrs) throws Exception {
        if (geometry == null) {
            return null;
        }
        String normalizedSource = CRSDefinitions.normalizeCode(sourceCrs);
        String normalizedTarget = CRSDefinitions.normalizeCode(targetCrs);
        if (normalizedSource.isBlank() || normalizedTarget.isBlank() || normalizedSource.equalsIgnoreCase(normalizedTarget)) {
            return geometry.copy();
        }

        CoordinateReferenceSystem source = CRSDefinitions.decode(normalizedSource, true);
        CoordinateReferenceSystem target = CRSDefinitions.decode(normalizedTarget, true);
        MathTransform transform = CRS.findMathTransform(source, target, true);
        return JTS.transform(geometry, transform);
    }

    private Geometry normalizeGeometryToFamily(Geometry geometry, String family) {
        if (geometry == null || geometry.isEmpty() || family == null || family.isBlank()) {
            return geometry;
        }

        Geometry working = geometry;
        if ("POLYGON".equalsIgnoreCase(family)) {
            try {
                working = geometry.buffer(0);
            } catch (Exception ignored) {
                working = geometry;
            }
        }

        switch (family.toUpperCase(Locale.ROOT)) {
            case "POINT":
                List<Point> points = new ArrayList<>();
                collectPoints(working, points);
                if (points.isEmpty()) {
                    return null;
                }
                if (points.size() == 1) {
                    return points.get(0);
                }
                return GEOMETRY_FACTORY.createMultiPoint(points.toArray(new Point[0]));
            case "LINE":
                List<LineString> lines = new ArrayList<>();
                collectLines(working, lines);
                if (lines.isEmpty()) {
                    return null;
                }
                if (lines.size() == 1) {
                    return lines.get(0);
                }
                return GEOMETRY_FACTORY.createMultiLineString(lines.toArray(new LineString[0]));
            case "POLYGON":
                List<Polygon> polygons = new ArrayList<>();
                collectPolygons(working, polygons);
                if (polygons.isEmpty()) {
                    return null;
                }
                if (polygons.size() == 1) {
                    return polygons.get(0);
                }
                return GEOMETRY_FACTORY.createMultiPolygon(polygons.toArray(new Polygon[0]));
            default:
                return working;
        }
    }

    private void collectPoints(Geometry geometry, List<Point> points) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }
        if (geometry instanceof Point) {
            points.add((Point) geometry);
            return;
        }
        if (geometry instanceof MultiPoint) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Geometry part = geometry.getGeometryN(i);
                if (part instanceof Point) {
                    points.add((Point) part);
                }
            }
            return;
        }
        if (geometry instanceof GeometryCollection) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                collectPoints(geometry.getGeometryN(i), points);
            }
        }
    }

    private void collectLines(Geometry geometry, List<LineString> lines) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }
        if (geometry instanceof LineString) {
            lines.add((LineString) geometry);
            return;
        }
        if (geometry instanceof MultiLineString) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Geometry part = geometry.getGeometryN(i);
                if (part instanceof LineString) {
                    lines.add((LineString) part);
                }
            }
            return;
        }
        if (geometry instanceof GeometryCollection) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                collectLines(geometry.getGeometryN(i), lines);
            }
        }
    }

    private void collectPolygons(Geometry geometry, List<Polygon> polygons) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }
        if (geometry instanceof Polygon) {
            polygons.add((Polygon) geometry);
            return;
        }
        if (geometry instanceof MultiPolygon) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Geometry part = geometry.getGeometryN(i);
                if (part instanceof Polygon) {
                    polygons.add((Polygon) part);
                }
            }
            return;
        }
        if (geometry instanceof GeometryCollection) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                collectPolygons(geometry.getGeometryN(i), polygons);
            }
        }
    }

    private List<Polygon> toPolygonList(Geometry geometry) {
        List<Polygon> polygons = new ArrayList<>();
        if (geometry == null || geometry.isEmpty()) {
            return polygons;
        }
        if (geometry instanceof Polygon) {
            polygons.add((Polygon) geometry);
            return polygons;
        }
        org.locationtech.jts.geom.Envelope envelope = geometry.getEnvelopeInternal();
        if (!envelope.isNull()) {
            Coordinate[] coordinates = new Coordinate[]{
                    new Coordinate(envelope.getMinX(), envelope.getMinY()),
                    new Coordinate(envelope.getMaxX(), envelope.getMinY()),
                    new Coordinate(envelope.getMaxX(), envelope.getMaxY()),
                    new Coordinate(envelope.getMinX(), envelope.getMaxY()),
                    new Coordinate(envelope.getMinX(), envelope.getMinY())
            };
            polygons.add(GEOMETRY_FACTORY.createPolygon(coordinates));
        }
        return polygons;
    }

    private void applyCrs(SimpleFeatureTypeBuilder builder, String crsCode) {
        try {
            String normalized = CRSDefinitions.normalizeCode(crsCode);
            if (!normalized.isBlank()) {
                builder.setCRS(CRSDefinitions.decode(normalized, true));
            }
        } catch (Exception ignored) {
        }
    }

    private String safeFieldName(String text) {
        return safeFieldName(text, 24);
    }

    private String safeFieldName(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "campo";
        }
        String safe = text.trim().replaceAll("[^A-Za-z0-9_]+", "_");
        while (safe.contains("__")) {
            safe = safe.replace("__", "_");
        }
        if (safe.isBlank()) {
            safe = "campo";
        }
        if (Character.isDigit(safe.charAt(0))) {
            safe = "f_" + safe;
        }
        int lengthLimit = Math.max(1, maxLength);
        if (safe.length() > lengthLimit) {
            safe = safe.substring(0, lengthLimit);
        }
        return safe;
    }

    private String safeTypeName(String text) {
        if (text == null || text.isBlank()) {
            return "resultado";
        }
        String safe = text.trim().replaceAll("[^A-Za-z0-9_]+", "_");
        if (safe.isBlank()) {
            safe = "resultado";
        }
        if (Character.isDigit(safe.charAt(0))) {
            safe = "r_" + safe;
        }
        return safe;
    }

    private String safeName(String text) {
        if (text == null || text.isBlank()) {
            return "resultado";
        }
        return text.trim().replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    private static final class LayerOption {
        private final Layer layer;
        private final ShapefileData data;
        private final String family;
        private final String crsCode;

        private LayerOption(Layer layer, ShapefileData data, String family, String crsCode) {
            this.layer = layer;
            this.data = data;
            this.family = family != null ? family : "";
            this.crsCode = crsCode != null ? crsCode : "";
        }

        @Override
        public String toString() {
            String familyLabel;
            switch (family) {
                case "POINT":
                    familyLabel = "Puntos";
                    break;
                case "LINE":
                    familyLabel = "Lineas";
                    break;
                case "POLYGON":
                    familyLabel = "Poligonos";
                    break;
                default:
                    familyLabel = "Vector";
                    break;
            }
            String crs = crsCode != null && !crsCode.isBlank() ? crsCode : "Sin CRS";
            return layer.getName() + " | " + familyLabel + " | " + crs;
        }
    }

    private record FeatureGeometryRef(SimpleFeature feature, Geometry geometry) {
    }

    private enum SpatialJoinAggregation {
        FIRST,
        VALUES,
        SUM,
        AVG,
        MIN,
        MAX
    }

    private record SpatialJoinFieldSpec(String sourceField,
                                        Class<?> outputBinding,
                                        boolean numeric,
                                        SpatialJoinAggregation aggregation) {
    }
}
