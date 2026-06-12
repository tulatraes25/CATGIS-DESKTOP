package ar.com.catgis;
import ar.com.catgis.data.vector.VectorLayerUtils;

import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import ar.com.catgis.core.model.Layer;

public class AttributeTableWindow extends JFrame {

    private final Layer layer;
    private ShapefileData data;
    private final List<String> rawColumnNames;
    private final JTable table;
    private final DefaultTableModel model;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JLabel statusLabel;
    private final JLabel countLabel;
    private final JTextField searchField;

    private boolean editMode = false;
    private boolean selectionSyncInProgress = false;
    private final JButton editButton;
    private final JButton applyButton;
    private final JButton calculatorButton;
    private final JButton assignValueButton;
    private final List<Class<?>> columnTypes = new ArrayList<>();

    public AttributeTableWindow(Layer layer, ShapefileData data, List<String> columnNames, List<Object[]> rows) {
        this.layer = layer;
        this.data = data;
        this.rawColumnNames = new ArrayList<>(columnNames);

        setTitle("Tabla de atributos - " + (layer != null ? layer.getName() : "Capa"));
        setSize(540, 220);
        setMinimumSize(new Dimension(460, 190));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(4, 4));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setBorder(new EmptyBorder(4, 4, 4, 4));

        statusLabel = new JLabel("Modo lectura. Activá edición solo cuando necesites modificar atributos.");
        statusLabel.setForeground(new Color(65, 75, 90));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 10f));

        statusLabel.setText("Modo lectura. Activa edicion solo si vas a cambiar datos.");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 9f));

        JPanel topPanel = buildTopPanel();
        add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return editMode && isColumnEditableByConfig(column);
            }
        };

        resolveColumnTypes();

        for (String rawName : this.rawColumnNames) {
            model.addColumn(resolveDisplayName(rawName));
        }

        if (rows != null) {
            for (Object[] row : rows) {
                model.addRow(row);
            }
        }

        table = new JTable(model);
        configureTableAppearance();
        configureEditors();
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(18);
        installTableContextMenu();

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (selectionSyncInProgress || e.getValueIsAdjusting()) {
                    return;
                }
                syncSelectionToMap();
            }
        });

        searchField = new JTextField();
        searchField.setFont(searchField.getFont().deriveFont(Font.PLAIN, 10f));
        searchField.setMargin(new Insets(0, 3, 0, 3));
        searchField.setPreferredSize(new Dimension(120, 20));
        searchField.addActionListener(e -> applyFilter(sorter));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(sorter); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(sorter); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(sorter); }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(3, 3));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        centerPanel.setBackground(Color.WHITE);

        JPanel filterPanel = new JPanel(new BorderLayout(3, 0));
        filterPanel.setOpaque(false);
        JLabel filterLabel = new JLabel("Buscar:");
        filterLabel.setFont(filterLabel.getFont().deriveFont(Font.BOLD, 10f));
        countLabel = new JLabel();
        countLabel.setForeground(new Color(90, 100, 115));
        countLabel.setFont(countLabel.getFont().deriveFont(Font.PLAIN, 9f));
        updateCountLabel();

        filterPanel.add(filterLabel, BorderLayout.WEST);
        filterPanel.add(searchField, BorderLayout.CENTER);
        filterPanel.add(countLabel, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(3, 3));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 0, 1));
        bottomPanel.setBackground(getBackground());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        buttonPanel.setOpaque(false);

        JButton copyButton = createIconButton(AppIcons.attrCopyIcon(), "Copiar fila", new Color(59, 130, 246));
        JButton refreshButton = createIconButton(AppIcons.attrRefreshIcon(), "Recargar", new Color(100, 116, 139));
        editButton = createIconButton(AppIcons.attrEditIcon(), "Habilitar edición", new Color(234, 179, 8));
        applyButton = createIconButton(AppIcons.attrApplyIcon(), "Aplicar cambios", new Color(16, 185, 129));
        JButton queryButton = createIconButton(AppIcons.identifyIcon(), "Constructor de consultas", new Color(37, 99, 235));
        calculatorButton = createIconButton(AppIcons.attrCalculatorIcon(), "Calculadora de campos", new Color(139, 92, 246));
        assignValueButton = createIconButton(AppIcons.attrAssignIcon(), "Asignar valor a un campo", new Color(14, 165, 233));
        JButton closeButton = createIconButton(AppIcons.attrCloseIcon(), "Cerrar", new Color(107, 114, 128));

        JButton exportCsvButton = createIconButton(AppIcons.exportIcon(), "Exportar a CSV", new Color(16, 185, 129));
        exportCsvButton.addActionListener(e -> exportToCsv());

        copyButton.addActionListener(e -> copySelectedRows());
        refreshButton.addActionListener(e -> reloadFromFeatures());
        editButton.addActionListener(e -> toggleEditMode());
        applyButton.addActionListener(e -> applyChanges());
        queryButton.addActionListener(e -> QueryBuilderDialog.open(layer));
        calculatorButton.addActionListener(e -> openFieldCalculator());
        assignValueButton.addActionListener(e -> openAssignValueDialog());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(copyButton);
        buttonPanel.add(exportCsvButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(editButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(queryButton);
        buttonPanel.add(calculatorButton);
        buttonPanel.add(assignValueButton);
        buttonPanel.add(closeButton);

        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        updateEditControls();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                table.requestFocusInWindow();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                OpenAttributeTableAction.unregisterWindow(layer, AttributeTableWindow.this);
            }
        });
    }

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(4, 2));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 1));
        topPanel.setBackground(getBackground());

        String layerName = layer != null ? layer.getName() : "Capa";
        String featureCount = data != null ? String.valueOf(data.getFeatureCount()) : "-";
        JLabel subTitle = new JLabel(layerName + " | " + featureCount + " registros");
        subTitle.setForeground(new Color(70, 80, 95));
        subTitle.setFont(subTitle.getFont().deriveFont(Font.BOLD, 10f));

        topPanel.add(subTitle, BorderLayout.WEST);
        return topPanel;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.setToolTipText(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setMargin(new Insets(8, 12, 8, 12));
        return button;
    }

    private JButton createIconButton(Icon icon, String tooltip, Color color) {
        JButton button = new JButton(icon);
        button.setFocusable(false);
        button.setToolTipText(tooltip);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        button.setContentAreaFilled(true);
        button.setPreferredSize(new Dimension(32, 28));
        button.setMargin(new Insets(2, 4, 2, 4));
        return button;
    }

    private JButton createToolButton(String glyph, String tooltip, Color color) {
        JButton button = new JButton(createGlyphIcon(glyph));
        button.setFocusable(false);
        button.setToolTipText(tooltip);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(34, 30));
        button.setMargin(new Insets(4, 4, 4, 4));
        return button;
    }

    private Icon createGlyphIcon(String glyph) {
        int w = 18;
        int h = 18;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        Font font = new Font("SansSerif", Font.BOLD, "≡".equals(glyph) ? 13 : 14);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int x = Math.max(0, (w - fm.stringWidth(glyph)) / 2);
        int y = ((h - fm.getHeight()) / 2) + fm.getAscent() - 1;
        g2.drawString(glyph, x, y);
        g2.dispose();
        return new ImageIcon(img);
    }

    private void resolveColumnTypes() {
        columnTypes.clear();
        SimpleFeatureType schema = data != null && data.getFeatureCollection() != null
                ? data.getFeatureCollection().getSchema()
                : null;

        for (String attributeName : rawColumnNames) {
            columnTypes.add(resolveConfiguredOrSchemaType(schema, attributeName));
        }
    }

    private Class<?> resolveConfiguredOrSchemaType(SimpleFeatureType schema, String attributeName) {
        if (layer != null) {
            FieldConfig config = layer.getOrCreateFieldConfig(attributeName, "");
            String configured = config.getTypeName();
            if (configured != null && !configured.isBlank()) {
                switch (FieldConfig.normalizeTypeName(configured)) {
                    case "String": return String.class;
                    case "Integer": return Integer.class;
                    case "Long": return Long.class;
                    case "Float": return Float.class;
                    case "Double": return Double.class;
                    case "Boolean": return Boolean.class;
                    case "Date": return java.util.Date.class;
                    case "Timestamp": return java.sql.Timestamp.class;
                }
            }
        }
        return getAttributeBinding(schema, attributeName);
    }

    private String resolveDisplayName(String rawName) {
        if (layer == null) {
            return rawName;
        }
        FieldConfig config = layer.getOrCreateFieldConfig(rawName, "");
        return config.getPublicName();
    }

    private boolean isColumnEditableByConfig(int column) {
        if (layer == null || column < 0 || column >= rawColumnNames.size()) {
            return false;
        }
        FieldConfig config = layer.getOrCreateFieldConfig(rawColumnNames.get(column), "");
        return config.isEditable();
    }



    void openFieldCalculator() {
        if (isReadOnlyLayer()) {
            JOptionPane.showMessageDialog(this, getReadOnlyMessage());
            return;
        }
        FieldCalculatorDialog.open(this);
    }

    void openAssignValueDialog() {
        if (isReadOnlyLayer()) {
            JOptionPane.showMessageDialog(this, getReadOnlyMessage());
            return;
        }
        AssignValueDialog.open(this);
    }

    private boolean isReadOnlyLayer() {
        return VectorLayerUtils.isReadOnlyVectorLayer(layer);
    }

    private String getReadOnlyMessage() {
        String reason = VectorLayerUtils.getReadOnlyVectorLayerReason(layer);
        return !reason.isBlank() ? reason : "La capa seleccionada esta en modo lectura.";
    }

    int getColumnCountSafe() {
        return rawColumnNames.size();
    }

    String getRawColumnNameAt(int index) {
        return rawColumnNames.get(index);
    }

    String getDisplayColumnNameAt(int index) {
        return model.getColumnName(index);
    }

    Object getValueAtModel(int row, int column) {
        return model.getValueAt(row, column);
    }

    int getSelectedModelRow() {
        int viewRow = table.getSelectedRow();
        return viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;
    }

    List<Integer> getSelectedModelRows() {
        int[] selectedRows = table.getSelectedRows();
        List<Integer> modelRows = new ArrayList<>();
        if (selectedRows == null) {
            return modelRows;
        }
        for (int viewRow : selectedRows) {
            if (viewRow >= 0) {
                modelRows.add(table.convertRowIndexToModel(viewRow));
            }
        }
        return modelRows;
    }

    void selectFeatureIds(List<String> featureIds) {
        selectionSyncInProgress = true;
        try {
            table.clearSelection();
            if (featureIds == null || featureIds.isEmpty() || data == null || data.getFeatures() == null) {
                return;
            }

            int firstVisibleRow = -1;
            for (int modelRow = 0; modelRow < data.getFeatures().size(); modelRow++) {
                SimpleFeature feature = data.getFeatures().get(modelRow);
                if (feature == null || feature.getID() == null || !featureIds.contains(feature.getID())) {
                    continue;
                }

                int viewRow = table.convertRowIndexToView(modelRow);
                if (viewRow < 0) {
                    continue;
                }

                table.addRowSelectionInterval(viewRow, viewRow);
                if (firstVisibleRow < 0) {
                    firstVisibleRow = viewRow;
                }
            }

            if (firstVisibleRow >= 0) {
                Rectangle rowBounds = table.getCellRect(firstVisibleRow, 0, true);
                table.scrollRectToVisible(rowBounds);
            }
        } finally {
            selectionSyncInProgress = false;
        }
    }

    void clearMapLinkedSelection() {
        selectionSyncInProgress = true;
        try {
            table.clearSelection();
        } finally {
            selectionSyncInProgress = false;
        }
    }

    private void syncSelectionToMap() {
        if (CatgisDesktopApp.mapPanel == null || layer == null || data == null) {
            return;
        }

        List<String> featureIds = new ArrayList<>();
        for (Integer modelRow : getSelectedModelRows()) {
            if (modelRow == null || modelRow < 0 || data.getFeatures() == null || modelRow >= data.getFeatures().size()) {
                continue;
            }
            SimpleFeature feature = data.getFeatures().get(modelRow);
            if (feature != null && feature.getID() != null) {
                featureIds.add(feature.getID());
            }
        }

        CatgisDesktopApp.mapPanel.syncSelectionFromAttributeTable(layer, featureIds);
    }

    List<Integer> getEditableColumnIndexes() {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < rawColumnNames.size(); i++) {
            if (isColumnEditableByConfig(i)) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    int createField(String requestedName, String requestedTypeName, String requestedPublicName) throws Exception {
        if (isReadOnlyLayer()) {
            throw new IllegalArgumentException(getReadOnlyMessage());
        }
        if (data == null || data.getSchema() == null) {
            throw new IllegalArgumentException("La capa no tiene esquema disponible para crear un campo.");
        }

        String fieldName = VectorAttributeSupport.buildUniqueFieldName(data, requestedName);
        if (fieldName.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un nombre de campo valido.");
        }

        String typeName = FieldConfig.normalizeTypeName(requestedTypeName);
        ShapefileData rebuilt = VectorAttributeSupport.addField(data, fieldName, typeName);

        FieldConfig config = layer.getOrCreateFieldConfig(fieldName, typeName);
        config.setPublicName(requestedPublicName != null && !requestedPublicName.isBlank() ? requestedPublicName : fieldName);
        config.setVisible(true);
        config.setEditable(true);

        replaceTableData(rebuilt);
        rawColumnNames.clear();
        rawColumnNames.addAll(VectorAttributeSupport.resolveVisibleAttributeNames(layer, rebuilt));
        rebuildVisibleColumns();

        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, rebuilt);
            CatgisDesktopApp.mapPanel.refreshMap();
        }
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.refreshLayerList();
        }
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Campo creado: " + fieldName);
        }
        CatgisDesktopApp.markProjectDirty();
        statusLabel.setText("Campo nuevo listo para calcular: " + config.getPublicName());

        return rawColumnNames.indexOf(fieldName);
    }

    String resolveExpressionForRow(String expression, int row) {
        String resolved = expression != null ? expression.trim() : "";
        if (resolved.startsWith("=")) {
            resolved = resolved.substring(1).trim();
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[([^\\]]+)]");
        java.util.regex.Matcher matcher = pattern.matcher(resolved);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String rawName = matcher.group(1).trim();
            int col = rawColumnNames.indexOf(rawName);
            if (col < 0) {
                throw new IllegalArgumentException("No existe el campo '" + rawName + "'.");
            }
            Object value = model.getValueAt(row, col);
            String replacement = toNumericLiteral(value, rawName, row);
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        resolved = sb.toString();

        return replaceGeometryTokens(resolved, row);
    }

    double evaluateExpressionForRow(String expression, int row) {
        return new BasicExpressionParser(resolveExpressionForRow(expression, row)).parse();
    }

    Object evaluateExpressionPreview(String expression, int row, int targetColumn) {
        Class<?> targetType = (targetColumn >= 0 && targetColumn < columnTypes.size())
                ? columnTypes.get(targetColumn)
                : Object.class;
        return evaluateExpressionValueForRow(expression, row, targetType);
    }

    private String replaceGeometryTokens(String expression, int row) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$[A-Za-z_][A-Za-z0-9_]*");
        java.util.regex.Matcher matcher = pattern.matcher(expression);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String token = matcher.group();
            double value = getGeometryTokenValue(token, row);
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(formatDoubleLiteral(value)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String toNumericLiteral(Object value, String fieldName, int row) {
        if (value == null) {
            return "0";
        }
        if (value instanceof Number) {
            return formatDoubleLiteral(((Number) value).doubleValue());
        }

        String text = String.valueOf(value).trim().replace(',', '.');
        if (text.isEmpty()) {
            return "0";
        }

        try {
            return formatDoubleLiteral(Double.parseDouble(text));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El campo '" + fieldName + "' no es numérico en la fila " + (row + 1) + ".");
        }
    }

    private String formatDoubleLiteral(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        return String.valueOf(value);
    }

    private double getGeometryTokenValue(String token, int row) {
        if (data == null || data.getFeatures() == null || row < 0 || row >= data.getFeatures().size()) {
            return 0d;
        }

        SimpleFeature feature = data.getFeatures().get(row);
        if (feature == null) {
            return 0d;
        }

        Object geometryObj = feature.getDefaultGeometry();
        if (!(geometryObj instanceof Geometry)) {
            return 0d;
        }

        Geometry geometry = (Geometry) geometryObj;
        if (geometry == null || geometry.isEmpty()) {
            return 0d;
        }

        switch (token.toLowerCase()) {
            case "$area":
            case "$area_m2":
                return VectorMeasurementSupport.resolveAreaSquareMeters(layer, geometry);
            case "$area_ha":
                return VectorMeasurementSupport.resolveAreaSquareMeters(layer, geometry) / 10000d;
            case "$area_km2":
                return VectorMeasurementSupport.resolveAreaSquareMeters(layer, geometry) / 1_000_000d;
            case "$length":
            case "$length_m":
                return VectorMeasurementSupport.resolveLengthMeters(layer, geometry);
            case "$length_km":
                return VectorMeasurementSupport.resolveLengthMeters(layer, geometry) / 1000d;
            case "$perimeter":
            case "$perimeter_m":
                return VectorMeasurementSupport.resolvePerimeterMeters(layer, geometry);
            case "$perimeter_km":
                return VectorMeasurementSupport.resolvePerimeterMeters(layer, geometry) / 1000d;
            case "$x":
                if (geometry instanceof Point) {
                    return ((Point) geometry).getX();
                }
                return geometry.getCentroid() != null ? geometry.getCentroid().getX() : 0d;
            case "$y":
                if (geometry instanceof Point) {
                    return ((Point) geometry).getY();
                }
                return geometry.getCentroid() != null ? geometry.getCentroid().getY() : 0d;
            default:
                throw new IllegalArgumentException("Token geometrico no soportado: " + token);
        }
    }

    private Object evaluateExpressionValueForRow(String expression, int row, Class<?> targetType) {
        String expr = expression != null ? expression.trim() : "";
        if (expr.isEmpty()) {
            return null;
        }

        if (shouldUseTextEvaluation(expr, targetType)) {
            try {
                Object evaluated = new TextExpressionParser(expr, row).parseValue();
                return convertEvaluatedValue(evaluated, targetType);
            } catch (IllegalArgumentException ex) {
                if (!canFallbackToNumeric(expr)) {
                    throw ex;
                }
            }
        }

        double result = evaluateExpressionForRow(expr, row);
        return convertCalculatedResult(result, targetType);
    }

    private boolean shouldUseTextEvaluation(String expression, Class<?> targetType) {
        String expr = expression != null ? expression.trim() : "";
        if (expr.startsWith("=")) {
            expr = expr.substring(1).trim();
        }
        String lower = expr.toLowerCase(java.util.Locale.ROOT);
        return targetType == String.class
                || targetType == Object.class
                || expr.contains("'")
                || expr.contains("\"")
                || lower.contains("concat(")
                || lower.contains("upper(")
                || lower.contains("lower(")
                || lower.contains("trim(")
                || lower.contains("replace(")
                || lower.contains("len(")
                || lower.contains("coalesce(")
                || lower.contains("str(");
    }

    private boolean canFallbackToNumeric(String expression) {
        String expr = expression != null ? expression.trim() : "";
        if (expr.startsWith("=")) {
            expr = expr.substring(1).trim();
        }
        String lower = expr.toLowerCase(java.util.Locale.ROOT);
        return !expr.contains("'")
                && !expr.contains("\"")
                && !lower.contains("concat(")
                && !lower.contains("upper(")
                && !lower.contains("lower(")
                && !lower.contains("trim(")
                && !lower.contains("replace(")
                && !lower.contains("len(")
                && !lower.contains("coalesce(")
                && !lower.contains("str(");
    }

    private Object convertEvaluatedValue(Object rawValue, Class<?> targetType) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Number number) {
            return convertCalculatedResult(number.doubleValue(), targetType);
        }
        if (rawValue instanceof Boolean) {
            if (targetType == Boolean.class || targetType == boolean.class) {
                return rawValue;
            }
            if (targetType == null || targetType == Object.class || targetType == String.class) {
                return String.valueOf(rawValue);
            }
            return convertValue(String.valueOf(rawValue), targetType);
        }
        if (targetType == null || targetType == Object.class || targetType == String.class) {
            return String.valueOf(rawValue);
        }
        return convertValue(String.valueOf(rawValue), targetType);
    }

    private Object resolveFieldValue(String rawName, int row) {
        int col = rawColumnNames.indexOf(rawName);
        if (col < 0) {
            throw new IllegalArgumentException("No existe el campo '" + rawName + "'.");
        }
        return model.getValueAt(row, col);
    }

    int applyFieldCalculation(int targetColumn, String expression, boolean onlySelectedRow) {
        if (targetColumn < 0 || targetColumn >= rawColumnNames.size()) {
            throw new IllegalArgumentException("Campo destino inválido.");
        }
        if (!isColumnEditableByConfig(targetColumn)) {
            throw new IllegalArgumentException("El campo destino no está marcado como editable.");
        }

        String expr = expression != null ? expression.trim() : "";
        if (expr.isEmpty()) {
            throw new IllegalArgumentException("La expresión está vacía.");
        }

        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        List<Integer> rows = new ArrayList<>();
        if (onlySelectedRow) {
            rows.addAll(getSelectedModelRows());
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("No hay filas seleccionadas.");
            }
        } else {
            for (int row = 0; row < model.getRowCount(); row++) {
                rows.add(row);
            }
        }

        for (Integer row : rows) {
            Object converted = evaluateExpressionValueForRow(expr, row, columnTypes.get(targetColumn));
            model.setValueAt(converted, row, targetColumn);
        }

        if (!editMode) {
            editMode = true;
            updateEditControls();
        }

        applyChanges();
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Cálculo aplicado sobre el campo: " + model.getColumnName(targetColumn));
        }
        statusLabel.setText("Calculadora de campos aplicada sobre: " + model.getColumnName(targetColumn));
        return rows.size();
    }

    int assignConstantValue(int targetColumn, String value, boolean onlySelectedRows) {
        if (targetColumn < 0 || targetColumn >= rawColumnNames.size()) {
            throw new IllegalArgumentException("Campo destino inválido.");
        }
        if (!isColumnEditableByConfig(targetColumn)) {
            throw new IllegalArgumentException("El campo destino no está marcado como editable.");
        }

        List<Integer> rows = new ArrayList<>();
        if (onlySelectedRows) {
            rows.addAll(getSelectedModelRows());
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("No hay filas seleccionadas.");
            }
        } else {
            for (int row = 0; row < model.getRowCount(); row++) {
                rows.add(row);
            }
        }

        Object parsedValue = convertValue(value, columnTypes.get(targetColumn));
        for (Integer row : rows) {
            model.setValueAt(parsedValue, row, targetColumn);
        }

        if (!editMode) {
            editMode = true;
            updateEditControls();
        }

        applyChanges();
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Valor asignado sobre el campo: " + model.getColumnName(targetColumn));
        }
        statusLabel.setText("Valor asignado sobre: " + model.getColumnName(targetColumn));
        return rows.size();
    }

    private Object convertCalculatedResult(double value, Class<?> targetType) {
        if (targetType == null || targetType == Object.class || targetType == String.class) {
            if (Math.rint(value) == value) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }
        if (targetType == Integer.class || targetType == int.class) {
            return (int) Math.round(value);
        }
        if (targetType == Long.class || targetType == long.class) {
            return (long) Math.round(value);
        }
        if (targetType == Float.class || targetType == float.class) {
            return (float) value;
        }
        if (targetType == Double.class || targetType == double.class) {
            return value;
        }
        if (targetType == Short.class || targetType == short.class) {
            return (short) Math.round(value);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return value != 0d;
        }
        return String.valueOf(value);
    }

    private void configureEditors() {
        for (int i = 0; i < columnTypes.size(); i++) {
            Class<?> type = columnTypes.get(i);
            if (isDateType(type)) {
                TableColumn column = table.getColumnModel().getColumn(i);
                column.setCellEditor(new DateCellEditor(type));
                column.setPreferredWidth(180);
            }
        }
    }

    private void configureTableAppearance() {
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 10f));
        header.setBackground(new Color(236, 236, 236));
        header.setForeground(new Color(30, 30, 30));
        header.setPreferredSize(new Dimension(header.getWidth(), 19));

        table.setFont(new Font("Dialog", Font.PLAIN, 10));
        table.setGridColor(new Color(214, 214, 214));
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(200, 220, 250));
        table.setSelectionForeground(new Color(20, 30, 45));
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            private final SimpleDateFormat displayDate = new SimpleDateFormat("dd/MM/yyyy");
            private final SimpleDateFormat displayDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Object shownValue = value;

                if (value instanceof java.sql.Date) {
                    shownValue = displayDate.format((java.sql.Date) value);
                } else if (value instanceof java.util.Date) {
                    shownValue = displayDateTime.format((java.util.Date) value);
                }

                Component c = super.getTableCellRendererComponent(table, shownValue, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 251, 253));
                    c.setForeground(new Color(32, 42, 58));
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(new EmptyBorder(0, 2, 0, 2));
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            table.getColumnModel().getColumn(i).setPreferredWidth(96);
        }
    }

    private void toggleEditMode() {
        if (isReadOnlyLayer()) {
            JOptionPane.showMessageDialog(this, getReadOnlyMessage());
            return;
        }
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        editMode = !editMode;
        table.repaint();
        updateEditControls();

        if (editMode) {
            statusLabel.setText("Modo edición activo. Solo se editan los campos habilitados en Ver/Editar campos.");
        } else {
            statusLabel.setText("Modo lectura activo. La tabla quedó bloqueada para edición.");
        }
    }

    private void updateEditControls() {
        if (isReadOnlyLayer()) {
            editMode = false;
            editButton.setToolTipText(getReadOnlyMessage());
            editButton.setBackground(new Color(107, 114, 128));
            editButton.setEnabled(false);
            applyButton.setEnabled(false);
            applyButton.setBackground(new Color(156, 163, 175));
            calculatorButton.setEnabled(false);
            assignValueButton.setEnabled(false);
            statusLabel.setText(getReadOnlyMessage());
            return;
        }
        editButton.setEnabled(true);
        calculatorButton.setEnabled(true);
        assignValueButton.setEnabled(true);
        if (editMode) {
            editButton.setToolTipText("Bloquear edición");
            editButton.setBackground(new Color(217, 119, 6));
            applyButton.setEnabled(true);
            applyButton.setBackground(new Color(16, 185, 129));
        } else {
            editButton.setToolTipText("Habilitar edición");
            editButton.setBackground(new Color(234, 179, 8));
            applyButton.setEnabled(false);
            applyButton.setBackground(new Color(156, 163, 175));
        }
    }

    private void applyFilter(TableRowSorter<DefaultTableModel> sorter) {
        String text = searchField.getText();
        if (text == null || text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text.trim())));
        }
        updateCountLabel();
    }

    private void updateCountLabel() {
        if (countLabel != null) {
            countLabel.setText("Filas visibles: " + table.getRowCount() + " / " + model.getRowCount());
            countLabel.setText(table.getRowCount() + "/" + model.getRowCount());
        }
    }

    private void installTableContextMenu() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTableContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTableContextMenu(e);
                }
            }
        });
    }

    private void showTableContextMenu(MouseEvent e) {
        int viewRow = table.rowAtPoint(e.getPoint());
        if (viewRow >= 0 && !table.isRowSelected(viewRow)) {
            table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
            syncSelectionToMap();
        }

        int selectedCount = getSelectedModelRows().size();
        JPopupMenu popup = new JPopupMenu();

        JMenuItem copyItem = new JMenuItem(selectedCount > 1 ? "Copiar filas seleccionadas" : "Copiar fila", AppIcons.attrCopyIcon());
        copyItem.setEnabled(selectedCount > 0);
        copyItem.addActionListener(ev -> copySelectedRows());
        popup.add(copyItem);

        JMenuItem copyToEditingItem = new JMenuItem("Copiar y pegar en capa en edicion", AppIcons.attrCopyIcon());
        boolean canCopyToEditing = selectedCount > 0
                && CatgisDesktopApp.mapPanel != null
                && CatgisDesktopApp.mapPanel.canCopySelectedFeaturesFromLayerToEditingLayer(layer);
        copyToEditingItem.setEnabled(canCopyToEditing);
        copyToEditingItem.addActionListener(ev -> copySelectedRowsToEditingLayer());
        popup.add(copyToEditingItem);

        popup.show(table, e.getX(), e.getY());
    }

    private void copySelectedRows() {
        List<Integer> selectedRows = getSelectedModelRows();
        if (!selectedRows.isEmpty()) {
            StringBuilder multi = new StringBuilder();
            for (int r = 0; r < selectedRows.size(); r++) {
                if (r > 0) {
                    multi.append(System.lineSeparator());
                }
                int row = selectedRows.get(r);
                for (int i = 0; i < rawColumnNames.size(); i++) {
                    if (i > 0) multi.append("\t");
                    Object value = model.getValueAt(row, i);
                    multi.append(value != null ? value : "");
                }
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(multi.toString()), null);
            statusLabel.setText(selectedRows.size() == 1 ? "Fila copiada al portapapeles." : selectedRows.size() + " filas copiadas al portapapeles.");
            return;
        }
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            statusLabel.setText("Seleccioná una fila para copiarla.");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rawColumnNames.size(); i++) {
            if (i > 0) sb.append("\t");
            Object value = model.getValueAt(row, i);
            sb.append(value != null ? value : "");
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
        statusLabel.setText("Fila copiada al portapapeles.");
    }

    private void exportToCsv() {
        JFileChooser chooser = FileChooserSupport.createChooser("export-csv", "Exportar atributos a CSV");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV (*.csv)", "csv"));
        chooser.setSelectedFile(new java.io.File(layer.getName().replaceAll("[^A-Za-z0-9._-]+", "_") + ".csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) file = new java.io.File(file.getAbsolutePath() + ".csv");
        try {
            java.io.PrintWriter pw = new java.io.PrintWriter(file, "UTF-8");
            // Header
            for (int i = 0; i < rawColumnNames.size(); i++) {
                if (i > 0) pw.print(",");
                pw.print("\"" + rawColumnNames.get(i).replace("\"", "\"\"") + "\"");
            }
            pw.println();
            // Rows
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int i = 0; i < model.getColumnCount(); i++) {
                    if (i > 0) pw.print(",");
                    Object value = model.getValueAt(r, i);
                    String str = value != null ? value.toString() : "";
                    pw.print("\"" + str.replace("\"", "\"\"") + "\"");
                }
                pw.println();
            }
            pw.close();
            statusLabel.setText("Exportado: " + file.getName());
            JOptionPane.showMessageDialog(this, "CSV exportado:\n" + file.getAbsolutePath(), "Exportar CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copySelectedRowsToEditingLayer() {
        if (CatgisDesktopApp.mapPanel == null) {
            return;
        }
        syncSelectionToMap();
        boolean copied = CatgisDesktopApp.mapPanel.copySelectedFeaturesFromLayerToEditingLayer(layer);
        if (copied) {
            statusLabel.setText("Seleccion copiada a la capa en edicion.");
            AttributeTableWindow editingWindow = OpenAttributeTableAction.getOpenWindow(CatgisDesktopApp.mapPanel.getEditingLayerRef());
            if (editingWindow != null && editingWindow != this) {
                editingWindow.reloadFromFeatures();
            }
        }
    }

    private void reloadFromFeatures() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        List<SimpleFeature> features = data != null ? data.getFeatures() : null;
        if (features == null) {
            statusLabel.setText("No se pudieron recargar las entidades.");
            return;
        }
        rebuildVisibleColumns();

        editMode = false;
        updateEditControls();
        updateCountLabel();
        statusLabel.setText("Tabla recargada desde la capa en memoria.");
    }

    private void applyChanges() {
        if (isReadOnlyLayer()) {
            JOptionPane.showMessageDialog(this, getReadOnlyMessage());
            return;
        }
        if (!editMode) {
            JOptionPane.showMessageDialog(this, "Primero habilitá la edición para poder aplicar cambios.");
            return;
        }

        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        if (data == null || data.getFeatures() == null) {
            JOptionPane.showMessageDialog(this, "No se encontró la capa en memoria para aplicar cambios.");
            return;
        }

        List<SimpleFeature> features = data.getFeatures();
        if (features.size() != model.getRowCount()) {
            JOptionPane.showMessageDialog(this,
                    "La cantidad de filas ya no coincide con la capa en memoria. Cerrá y volvé a abrir la tabla.");
            return;
        }

        try {
            for (int row = 0; row < model.getRowCount(); row++) {
                SimpleFeature feature = features.get(row);

                for (int col = 0; col < rawColumnNames.size(); col++) {
                    if (!isColumnEditableByConfig(col)) {
                        continue;
                    }
                    String attributeName = rawColumnNames.get(col);
                    Object rawValue = model.getValueAt(row, col);
                    Class<?> targetType = columnTypes.get(col);
                    Object parsedValue = convertValue(rawValue, targetType);
                    feature.setAttribute(attributeName, parsedValue);
                }
            }

            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.refreshMap();
            }
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.repaintLayers();
            }
            CatgisDesktopApp.markProjectDirty();
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Cambios aplicados en la tabla de atributos.");
            }

            editMode = false;
            updateEditControls();
            statusLabel.setText("Cambios aplicados correctamente en la capa cargada en CATGIS.");

            JOptionPane.showMessageDialog(this,
                    "Cambios aplicados correctamente en la capa cargada en CATGIS.\n\n" +
                            "Solo se modificaron los campos marcados como editables.");
        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al aplicar cambios en la tabla de atributos", ex);
            AppErrorSupport.showErrorDialog(this, "Tabla de atributos", "Error al aplicar cambios.", ex);
        }
    }

    private Class<?> getAttributeBinding(SimpleFeatureType schema, String attributeName) {
        if (schema == null || attributeName == null || attributeName.isBlank()) {
            return String.class;
        }

        try {
            if (schema.getDescriptor(attributeName) != null && schema.getDescriptor(attributeName).getType() != null) {
                Class<?> binding = schema.getDescriptor(attributeName).getType().getBinding();
                if (binding != null) {
                    return binding;
                }
            }
        } catch (Exception ignored) { CatgisLogger.warn("AttributeTableWindow: operation failed", ignored); }

        return String.class;
    }

    private boolean isDateType(Class<?> type) {
        return type != null && (
                java.util.Date.class.isAssignableFrom(type) ||
                java.sql.Date.class.isAssignableFrom(type) ||
                java.sql.Timestamp.class.isAssignableFrom(type)
        );
    }

    private Object convertValue(Object rawValue, Class<?> targetType) {
        if (rawValue == null) {
            return null;
        }

        if (isDateType(targetType)) {
            if (rawValue instanceof Date) {
                if (targetType == java.sql.Date.class) {
                    return new java.sql.Date(((Date) rawValue).getTime());
                }
                if (targetType == Timestamp.class) {
                    return new Timestamp(((Date) rawValue).getTime());
                }
                return rawValue;
            }

            String text = String.valueOf(rawValue).trim();
            if (text.isEmpty()) {
                return null;
            }

            Date parsed = parseDate(text);
            if (parsed == null) {
                throw new IllegalArgumentException("No se pudo interpretar la fecha '" + text + "'.");
            }

            if (targetType == java.sql.Date.class) {
                return new java.sql.Date(parsed.getTime());
            }
            if (targetType == Timestamp.class) {
                return new Timestamp(parsed.getTime());
            }
            return parsed;
        }

        String text = String.valueOf(rawValue).trim();
        if (text.isEmpty()) {
            return null;
        }

        if (targetType == null || targetType == Object.class || targetType == String.class) {
            return text;
        }

        try {
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(text);
            }
            if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(text);
            }
            if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(text.replace(',', '.'));
            }
            if (targetType == Float.class || targetType == float.class) {
                return Float.parseFloat(text.replace(',', '.'));
            }
            if (targetType == Short.class || targetType == short.class) {
                return Short.parseShort(text);
            }
            if (targetType == Boolean.class || targetType == boolean.class) {
                return text.equalsIgnoreCase("true") || text.equalsIgnoreCase("si") || text.equalsIgnoreCase("sí") || text.equals("1");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "No se pudo convertir el valor '" + text + "' al tipo " + targetType.getSimpleName());
        }

        return text;
    }

    private Date parseDate(String text) {
        String[] patterns = {
                "dd/MM/yyyy",
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "yyyy/MM/dd",
                "dd/MM/yyyy HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(pattern);
                fmt.setLenient(false);
                return fmt.parse(text);
            } catch (Exception ignored) { CatgisLogger.warn("AttributeTableWindow: operation failed", ignored); }
        }
        return null;
    }

    private void replaceTableData(ShapefileData newData) {
        this.data = newData;
        if (layer != null && newData != null) {
            layer.setSourceName(newData.getSourceName());
            layer.setFeatureCount(newData.getFeatureCount());
        }
    }

    private void rebuildVisibleColumns() {
        resolveColumnTypes();
        model.setRowCount(0);
        model.setColumnCount(0);

        for (String rawName : rawColumnNames) {
            model.addColumn(resolveDisplayName(rawName));
        }

        List<SimpleFeature> features = data != null ? data.getFeatures() : null;
        if (features != null) {
            for (SimpleFeature feature : features) {
                List<Object> row = new ArrayList<>();
                for (String columnName : rawColumnNames) {
                    row.add(feature.getAttribute(columnName));
                }
                model.addRow(row.toArray(new Object[0]));
            }
        }

        table.setRowSorter(sorter);
        configureTableAppearance();
        configureEditors();
        updateCountLabel();
    }



    static class BasicExpressionParser {
        private final String input;
        private int pos = -1;
        private int ch;

        BasicExpressionParser(String input) {
            this.input = input != null ? input : "";
        }

        double parse() {
            nextChar();
            double x = parseExpression();
            skipSpaces();
            if (pos < input.length()) {
                throw new IllegalArgumentException("Token inválido cerca de: " + input.substring(pos));
            }
            return x;
        }

        private void nextChar() {
            ch = (++pos < input.length()) ? input.charAt(pos) : -1;
        }

        private boolean eat(int charToEat) {
            skipSpaces();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }
        private void skipSpaces() {
            while (ch != -1 && Character.isWhitespace((char) ch)) {
                nextChar();
            }
        }

        private double parseExpression() {
            double x = parseTerm();
            while (true) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }

        private double parseTerm() {
            double x = parsePower();
            while (true) {
                if (eat('*')) x *= parsePower();
                else if (eat('/')) {
                    double divisor = parsePower();
                    if (divisor == 0d) throw new IllegalArgumentException("División por cero.");
                    x /= divisor;
                } else if (eat('%')) {
                    double divisor = parsePower();
                    if (divisor == 0d) throw new IllegalArgumentException("División por cero.");
                    x %= divisor;
                } else return x;
            }
        }

        private double parsePower() {
            double x = parseFactor();
            while (eat('^')) {
                x = Math.pow(x, parseFactor());
            }
            return x;
        }

        private double parseFactor() {
            skipSpaces();
            if (eat('+')) return parseFactor();
            if (eat('-')) return -parseFactor();

            double x;
            int startPos = this.pos;

            if (eat('(')) {
                x = parseExpression();
                if (!eat(')')) throw new IllegalArgumentException("Falta cerrar paréntesis.");
                return x;
            }

            if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                return Double.parseDouble(input.substring(startPos, this.pos));
            }

            if (isIdentifierStart(ch)) {
                while (isIdentifierPart(ch)) nextChar();
                String name = input.substring(startPos, this.pos).toLowerCase();

                if ("pi".equals(name)) {
                    return Math.PI;
                }
                if ("e".equals(name)) {
                    return Math.E;
                }

                if (!eat('(')) {
                    throw new IllegalArgumentException("Función inválida: " + name);
                }

                java.util.List<Double> args = new java.util.ArrayList<>();
                if (!eat(')')) {
                    do {
                        args.add(parseExpression());
                    } while (eat(','));
                    if (!eat(')')) throw new IllegalArgumentException("Falta cerrar paréntesis en la función " + name + ".");
                }
                return applyFunction(name, args);
            }

            throw new IllegalArgumentException("Expresión inválida.");
        }

        private boolean isIdentifierStart(int c) {
            return Character.isLetter(c) || c == '_';
        }

        private boolean isIdentifierPart(int c) {
            return Character.isLetterOrDigit(c) || c == '_';
        }

        private double applyFunction(String name, java.util.List<Double> args) {
            switch (name) {
                case "abs": requireArgs(name, args, 1); return Math.abs(args.get(0));
                case "round": requireArgs(name, args, 1); return Math.rint(args.get(0));
                case "floor": requireArgs(name, args, 1); return Math.floor(args.get(0));
                case "ceil": requireArgs(name, args, 1); return Math.ceil(args.get(0));
                case "sqrt": requireArgs(name, args, 1); return Math.sqrt(args.get(0));
                case "sin": requireArgs(name, args, 1); return Math.sin(args.get(0));
                case "cos": requireArgs(name, args, 1); return Math.cos(args.get(0));
                case "tan": requireArgs(name, args, 1); return Math.tan(args.get(0));
                case "asin": requireArgs(name, args, 1); return Math.asin(args.get(0));
                case "acos": requireArgs(name, args, 1); return Math.acos(args.get(0));
                case "atan": requireArgs(name, args, 1); return Math.atan(args.get(0));
                case "ln": requireArgs(name, args, 1); return Math.log(args.get(0));
                case "log10": requireArgs(name, args, 1); return Math.log10(args.get(0));
                case "exp": requireArgs(name, args, 1); return Math.exp(args.get(0));
                case "pow": requireArgs(name, args, 2); return Math.pow(args.get(0), args.get(1));
                case "min": requireArgs(name, args, 2); return Math.min(args.get(0), args.get(1));
                case "max": requireArgs(name, args, 2); return Math.max(args.get(0), args.get(1));
                default: throw new IllegalArgumentException("Función no soportada: " + name);
            }
        }

        private void requireArgs(String name, java.util.List<Double> args, int expected) {
            if (args.size() != expected) {
                throw new IllegalArgumentException("La función " + name + " requiere " + expected + " parámetro(s).");
            }
        }
    }

    private class TextExpressionParser {
        private final String input;
        private final int row;
        private int pos = -1;
        private int ch;

        TextExpressionParser(String input, int row) {
            String normalized = input != null ? input.trim() : "";
            if (normalized.startsWith("=")) {
                normalized = normalized.substring(1).trim();
            }
            this.input = normalized;
            this.row = row;
        }

        Object parseValue() {
            nextChar();
            Object value = parseExpressionValue();
            skipSpaces();
            if (pos < input.length()) {
                throw new IllegalArgumentException("Expresion de texto invalida cerca de: " + input.substring(pos));
            }
            return value;
        }

        private void nextChar() {
            ch = (++pos < input.length()) ? input.charAt(pos) : -1;
        }

        private boolean eat(int charToEat) {
            skipSpaces();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        private void skipSpaces() {
            while (ch != -1 && Character.isWhitespace((char) ch)) {
                nextChar();
            }
        }

        private Object parseExpressionValue() {
            skipSpaces();

            if (eat('(')) {
                Object value = parseExpressionValue();
                if (!eat(')')) {
                    throw new IllegalArgumentException("Falta cerrar parentesis en expresion de texto.");
                }
                return value;
            }

            if (ch == '\'' || ch == '"') {
                return parseQuotedString();
            }

            if (ch == '[') {
                return parseFieldReference();
            }

            if (ch == '$') {
                return parseGeometryReference();
            }

            if (ch == '-' || ch == '+' || (ch >= '0' && ch <= '9') || ch == '.') {
                return parseNumberLiteral();
            }

            if (isIdentifierStart(ch)) {
                String name = parseIdentifier().toLowerCase(java.util.Locale.ROOT);
                if ("true".equals(name)) {
                    return Boolean.TRUE;
                }
                if ("false".equals(name)) {
                    return Boolean.FALSE;
                }
                if ("null".equals(name)) {
                    return null;
                }
                if (!eat('(')) {
                    throw new IllegalArgumentException("Funcion de texto invalida: " + name);
                }
                java.util.List<Object> args = new java.util.ArrayList<>();
                if (!eat(')')) {
                    do {
                        args.add(parseExpressionValue());
                    } while (eat(','));
                    if (!eat(')')) {
                        throw new IllegalArgumentException("Falta cerrar parentesis en la funcion " + name + ".");
                    }
                }
                return applyTextFunction(name, args);
            }

            throw new IllegalArgumentException("Expresion de texto invalida.");
        }

        private String parseQuotedString() {
            int quote = ch;
            nextChar();
            StringBuilder sb = new StringBuilder();
            while (ch != -1 && ch != quote) {
                if (ch == '\\') {
                    nextChar();
                    if (ch == -1) {
                        break;
                    }
                }
                sb.append((char) ch);
                nextChar();
            }
            if (ch != quote) {
                throw new IllegalArgumentException("Falta cerrar comillas en la expresion.");
            }
            nextChar();
            return sb.toString();
        }

        private Object parseFieldReference() {
            if (!eat('[')) {
                throw new IllegalArgumentException("Referencia de campo invalida.");
            }
            StringBuilder sb = new StringBuilder();
            while (ch != -1 && ch != ']') {
                sb.append((char) ch);
                nextChar();
            }
            if (!eat(']')) {
                throw new IllegalArgumentException("Falta cerrar ] en la referencia de campo.");
            }
            return resolveFieldValue(sb.toString().trim(), row);
        }

        private double parseGeometryReference() {
            StringBuilder sb = new StringBuilder();
            while (ch == '$' || Character.isLetterOrDigit(ch) || ch == '_') {
                sb.append((char) ch);
                nextChar();
            }
            return getGeometryTokenValue(sb.toString(), row);
        }

        private Double parseNumberLiteral() {
            int startPos = this.pos;
            if (ch == '+' || ch == '-') {
                nextChar();
            }
            while ((ch >= '0' && ch <= '9') || ch == '.') {
                nextChar();
            }
            return Double.parseDouble(input.substring(startPos, this.pos));
        }

        private String parseIdentifier() {
            StringBuilder sb = new StringBuilder();
            while (isIdentifierPart(ch)) {
                sb.append((char) ch);
                nextChar();
            }
            return sb.toString();
        }

        private boolean isIdentifierStart(int c) {
            return Character.isLetter(c) || c == '_';
        }

        private boolean isIdentifierPart(int c) {
            return Character.isLetterOrDigit(c) || c == '_';
        }

        private Object applyTextFunction(String name, java.util.List<Object> args) {
            switch (name) {
                case "concat":
                    StringBuilder concat = new StringBuilder();
                    for (Object arg : args) {
                        concat.append(asText(arg));
                    }
                    return concat.toString();
                case "upper":
                    requireTextArgs(name, args, 1);
                    return asText(args.get(0)).toUpperCase(java.util.Locale.ROOT);
                case "lower":
                    requireTextArgs(name, args, 1);
                    return asText(args.get(0)).toLowerCase(java.util.Locale.ROOT);
                case "trim":
                    requireTextArgs(name, args, 1);
                    return asText(args.get(0)).trim();
                case "replace":
                    requireTextArgs(name, args, 3);
                    return asText(args.get(0)).replace(asText(args.get(1)), asText(args.get(2)));
                case "len":
                    requireTextArgs(name, args, 1);
                    return (double) asText(args.get(0)).length();
                case "coalesce":
                    if (args.isEmpty()) {
                        throw new IllegalArgumentException("La funcion coalesce requiere al menos un parametro.");
                    }
                    for (Object arg : args) {
                        String text = asText(arg);
                        if (!text.isBlank()) {
                            return text;
                        }
                    }
                    return "";
                case "str":
                    requireTextArgs(name, args, 1);
                    return asText(args.get(0));
                default:
                    throw new IllegalArgumentException("Funcion de texto no soportada: " + name);
            }
        }

        private void requireTextArgs(String name, java.util.List<Object> args, int expected) {
            if (args.size() != expected) {
                throw new IllegalArgumentException("La funcion " + name + " requiere " + expected + " parametro(s).");
            }
        }

        private String asText(Object value) {
            return value != null ? String.valueOf(value) : "";
        }
    }

    private class DateCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new BorderLayout(4, 0));
        private final JFormattedTextField field = new JFormattedTextField();
        private final JButton button = new JButton("...");
        private final Class<?> targetType;

        DateCellEditor(Class<?> targetType) {
            this.targetType = targetType;
            field.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            button.setMargin(new Insets(2, 6, 2, 6));

            button.addActionListener(e -> {
                Date current = null;
                Object currentValue = getCellEditorValue();
                if (currentValue instanceof Date) {
                    current = (Date) currentValue;
                } else {
                    current = parseDate(field.getText());
                }

                Date selected = DatePickerDialog.showDialog(field, current);
                if (selected != null) {
                    if (targetType == java.sql.Date.class) {
                        field.setValue(new java.sql.Date(selected.getTime()));
                    } else if (targetType == Timestamp.class) {
                        field.setValue(new Timestamp(selected.getTime()));
                    } else {
                        field.setValue(selected);
                    }
                    field.setText(new SimpleDateFormat("dd/MM/yyyy").format(selected));
                }
            });

            panel.add(field, BorderLayout.CENTER);
            panel.add(button, BorderLayout.EAST);
        }

        @Override
        public Object getCellEditorValue() {
            Object value = field.getValue();
            if (value instanceof Date) {
                return value;
            }

            String text = field.getText() != null ? field.getText().trim() : "";
            if (text.isEmpty()) {
                return null;
            }

            Date parsed = parseDate(text);
            if (parsed == null) {
                return text;
            }

            if (targetType == java.sql.Date.class) {
                return new java.sql.Date(parsed.getTime());
            }
            if (targetType == Timestamp.class) {
                return new Timestamp(parsed.getTime());
            }
            return parsed;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof Date) {
                field.setValue(value);
                field.setText(new SimpleDateFormat("dd/MM/yyyy").format((Date) value));
            } else {
                field.setValue(value);
                field.setText(value != null ? String.valueOf(value) : "");
            }
            return panel;
        }
    }
}