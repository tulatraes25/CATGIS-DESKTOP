package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Locale;

public class FieldCalculatorDialog extends JDialog {

    private final AttributeTableWindow tableWindow;
    private final JTextArea expressionArea;
    private final DefaultListModel<FieldItem> fieldModel;
    private final JList<FieldItem> fieldsList;
    private final JCheckBox onlySelectedRowsCheck;
    private final JLabel targetLabel;

    private FieldItem targetField;

    public FieldCalculatorDialog(AttributeTableWindow tableWindow) {
        super(tableWindow, "Calculadora de campos", false);
        this.tableWindow = tableWindow;

        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        header.setBackground(new Color(248, 250, 252));

        JLabel title = new JLabel("Calculadora de campos");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(new Color(32, 42, 58));

        JLabel subtitle = new JLabel("Crea o elige un campo destino. Soporta calculos numericos y texto util con metricas en metros y m2.");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(new Color(95, 105, 120));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        fieldModel = new DefaultListModel<>();
        fieldsList = new JList<>(fieldModel);
        fieldsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fieldsList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setTargetField(fieldsList.getSelectedValue());
                }
            }
        });
        reloadEditableFields();

        JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        leftPanel.setBackground(Color.WHITE);

        JLabel leftLabel = new JLabel("Campos editables:");
        leftLabel.setFont(leftLabel.getFont().deriveFont(Font.BOLD, 12f));
        leftPanel.add(leftLabel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(fieldsList), BorderLayout.CENTER);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftActions.setOpaque(false);
        JButton newFieldButton = new JButton("Nuevo campo...");
        JButton insertFieldButton = new JButton("Insertar campo");
        newFieldButton.addActionListener(e -> createField());
        insertFieldButton.addActionListener(e -> insertSelectedField());
        leftActions.add(newFieldButton);
        leftActions.add(insertFieldButton);
        leftPanel.add(leftActions, BorderLayout.SOUTH);

        JPanel exprPanel = new JPanel(new BorderLayout(6, 6));
        exprPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        exprPanel.setBackground(Color.WHITE);

        expressionArea = new JTextArea(8, 40);
        expressionArea.setLineWrap(true);
        expressionArea.setWrapStyleWord(true);
        expressionArea.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel exprTop = new JPanel(new BorderLayout(6, 4));
        exprTop.setOpaque(false);
        exprTop.add(new JLabel("Expresion:"), BorderLayout.WEST);

        JLabel examples = new JLabel("Ej.: =$length_km | =round($area_m2) | =concat(upper([nombre]), ' - ', trim([sector]))");
        examples.setForeground(new Color(95, 105, 120));
        examples.setFont(examples.getFont().deriveFont(Font.PLAIN, 11f));
        exprTop.add(examples, BorderLayout.SOUTH);

        exprPanel.add(exprTop, BorderLayout.NORTH);
        exprPanel.add(new JScrollPane(expressionArea), BorderLayout.CENTER);

        JPanel keypad = new JPanel(new GridLayout(5, 6, 6, 6));
        String[] keys = {
                "7", "8", "9", "/", "(", ")",
                "4", "5", "6", "*", "+", "-",
                "1", "2", "3", ".", "%", "^",
                "0", "CE", "Area m2", "Area ha", "Longitud m", "Longitud km",
                "=", "X", "Y", "Perimetro m", "Perimetro km", "Probar"
        };

        for (String key : keys) {
            JButton button = new JButton(key);
            switch (key) {
                case "CE":
                    button.addActionListener(e -> expressionArea.setText(""));
                    break;
                case "Area m2":
                    button.addActionListener(e -> insertGeometryShortcut("$area_m2"));
                    break;
                case "Area ha":
                    button.addActionListener(e -> insertGeometryShortcut("$area_ha"));
                    break;
                case "Longitud m":
                    button.addActionListener(e -> insertGeometryShortcut("$length_m"));
                    break;
                case "Longitud km":
                    button.addActionListener(e -> insertGeometryShortcut("$length_km"));
                    break;
                case "Perimetro m":
                    button.addActionListener(e -> insertGeometryShortcut("$perimeter_m"));
                    break;
                case "Perimetro km":
                    button.addActionListener(e -> insertGeometryShortcut("$perimeter_km"));
                    break;
                case "X":
                    button.addActionListener(e -> insertGeometryShortcut("$x"));
                    break;
                case "Y":
                    button.addActionListener(e -> insertGeometryShortcut("$y"));
                    break;
                case "Probar":
                    button.addActionListener(e -> testExpression());
                    break;
                case "=":
                    button.addActionListener(e -> insertEquals());
                    break;
                default:
                    button.addActionListener(e -> insertKey(key));
                    break;
            }
            keypad.add(button);
        }

        JPanel functions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        functions.setOpaque(false);
        addFunctionButton(functions, "abs(");
        addFunctionButton(functions, "round(");
        addFunctionButton(functions, "floor(");
        addFunctionButton(functions, "ceil(");
        addFunctionButton(functions, "sqrt(");
        addFunctionButton(functions, "pow(");
        addFunctionButton(functions, "min(");
        addFunctionButton(functions, "max(");
        addFunctionButton(functions, "sin(");
        addFunctionButton(functions, "cos(");
        addFunctionButton(functions, "log10(");
        addFunctionButton(functions, "concat(");
        addFunctionButton(functions, "upper(");
        addFunctionButton(functions, "lower(");
        addFunctionButton(functions, "trim(");
        addFunctionButton(functions, "replace(");
        addFunctionButton(functions, "len(");
        addFunctionButton(functions, "coalesce(");

        JPanel exprSouth = new JPanel(new BorderLayout(0, 6));
        exprSouth.setOpaque(false);
        exprSouth.add(keypad, BorderLayout.CENTER);
        exprSouth.add(functions, BorderLayout.SOUTH);
        exprPanel.add(exprSouth, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, exprPanel);
        splitPane.setResizeWeight(0.28);
        add(splitPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        footer.setBackground(new Color(248, 250, 252));

        JPanel footerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        footerLeft.setOpaque(false);
        footerLeft.add(new JLabel("Modificando:"));

        targetLabel = new JLabel("<ningun campo seleccionado>");
        targetLabel.setFont(targetLabel.getFont().deriveFont(Font.BOLD, 13f));
        targetLabel.setForeground(new Color(37, 99, 235));
        targetLabel.setOpaque(true);
        targetLabel.setBackground(new Color(239, 246, 255));
        targetLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254)),
                new EmptyBorder(4, 8, 4, 8)
        ));
        footerLeft.add(targetLabel);

        onlySelectedRowsCheck = new JCheckBox("Aplicar solo a filas seleccionadas");
        onlySelectedRowsCheck.setOpaque(false);
        footerLeft.add(onlySelectedRowsCheck);

        JPanel footerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footerRight.setOpaque(false);
        JButton applyButton = new JButton("Aplicar");
        JButton closeButton = new JButton("Cerrar");
        applyButton.addActionListener(e -> applyCalculation());
        closeButton.addActionListener(e -> dispose());
        footerRight.add(applyButton);
        footerRight.add(closeButton);

        footer.add(footerLeft, BorderLayout.WEST);
        footer.add(footerRight, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        setSize(1280, 760);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(tableWindow);
    }

    public static void open(AttributeTableWindow tableWindow) {
        FieldCalculatorDialog dialog = new FieldCalculatorDialog(tableWindow);
        dialog.setVisible(true);
    }

    private void addFunctionButton(JPanel panel, String token) {
        JButton button = new JButton(token.substring(0, token.length() - 1));
        button.addActionListener(e -> insertText(token));
        panel.add(button);
    }

    private void reloadEditableFields() {
        fieldModel.clear();
        List<Integer> editableIndexes = tableWindow.getEditableColumnIndexes();
        for (Integer idx : editableIndexes) {
            fieldModel.addElement(new FieldItem(
                    idx,
                    tableWindow.getRawColumnNameAt(idx),
                    tableWindow.getDisplayColumnNameAt(idx)
            ));
        }
    }

    private void createField() {
        JTextField nameField = new JTextField(18);
        JTextField aliasField = new JTextField(18);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                "Double", "Integer", "Long", "Float", "String", "Boolean", "Date", "Timestamp"
        });
        typeCombo.setSelectedItem("Double");

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Nombre interno"));
        panel.add(nameField);
        panel.add(new JLabel("Alias / nombre visible"));
        panel.add(aliasField);
        panel.add(new JLabel("Tipo"));
        panel.add(typeCombo);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Crear campo nuevo",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int targetIndex = tableWindow.createField(
                    nameField.getText(),
                    String.valueOf(typeCombo.getSelectedItem()),
                    aliasField.getText()
            );
            reloadEditableFields();
            for (int i = 0; i < fieldModel.size(); i++) {
                FieldItem item = fieldModel.getElementAt(i);
                if (item.index == targetIndex) {
                    fieldsList.setSelectedValue(item, true);
                    setTargetField(item);
                    break;
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Campo creado. Ya podes usarlo como destino en la calculadora.",
                    "Calculadora de campos",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo crear el campo: " + ex.getMessage(),
                    "Calculadora de campos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setTargetField(FieldItem item) {
        targetField = item;
        if (item == null) {
            targetLabel.setText("<ningun campo seleccionado>");
            targetLabel.setToolTipText(null);
            return;
        }

        String shown = item.displayName != null && !item.displayName.isBlank() ? item.displayName : item.rawName;
        if (!shown.equals(item.rawName)) {
            shown += " (" + item.rawName + ")";
        }
        targetLabel.setText(shown);
        targetLabel.setToolTipText("Ese es el unico campo que se va a modificar.");
    }

    private void insertSelectedField() {
        FieldItem item = fieldsList.getSelectedValue();
        if (item != null) {
            insertText("[" + item.rawName + "]");
        }
    }

    private void insertGeometryShortcut(String token) {
        String expr = expressionArea.getText() != null ? expressionArea.getText().trim() : "";
        if (expr.isEmpty()) {
            expressionArea.setText("=" + token);
        } else {
            insertText(token);
        }
        expressionArea.requestFocusInWindow();
    }

    private void insertEquals() {
        String expr = expressionArea.getText() != null ? expressionArea.getText() : "";
        if (expr.isBlank()) {
            expressionArea.setText("=");
        } else {
            insertText("=");
        }
        expressionArea.requestFocusInWindow();
    }

    private void insertKey(String key) {
        if ("+-*/%^".contains(key)) {
            insertText(" " + key + " ");
        } else {
            insertText(key);
        }
    }

    private void insertText(String text) {
        expressionArea.insert(text, expressionArea.getCaretPosition());
        expressionArea.requestFocusInWindow();
    }

    private void testExpression() {
        int row = tableWindow.getSelectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila en la tabla para probar la expresion.");
            return;
        }

        String expr = expressionArea.getText() != null ? expressionArea.getText().trim() : "";
        if (expr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe una expresion primero.");
            return;
        }

        try {
            int targetIndex = targetField != null ? targetField.index : -1;
            Object value = tableWindow.evaluateExpressionPreview(expr, row, targetIndex);
            JOptionPane.showMessageDialog(this,
                    "Resultado para la fila seleccionada: " + trimValue(value),
                    "Prueba de expresion",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo evaluar la expresion: " + ex.getMessage(),
                    "Calculadora de campos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyCalculation() {
        if (targetField == null) {
            JOptionPane.showMessageDialog(this, "Elige primero un campo destino.");
            return;
        }

        String expr = expressionArea.getText() != null ? expressionArea.getText().trim() : "";
        if (expr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe una expresion antes de aplicar.");
            return;
        }

        try {
            int affected = tableWindow.applyFieldCalculation(targetField.index, expr, onlySelectedRowsCheck.isSelected());
            JOptionPane.showMessageDialog(this,
                    "Calculo aplicado correctamente sobre " + affected + " registro(s).",
                    "Calculadora de campos",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo aplicar el calculo: " + ex.getMessage(),
                    "Calculadora de campos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String trimValue(Object value) {
        if (value == null) {
            return "<null>";
        }
        if (!(value instanceof Number number)) {
            return String.valueOf(value);
        }
        double numeric = number.doubleValue();
        if (Math.rint(numeric) == numeric) {
            return String.valueOf((long) numeric);
        }
        return String.format(Locale.US, "%s", numeric);
    }

    private String trimNumber(double value) {
        if (Math.rint(value) == value) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.US, "%s", value);
    }

    private static class FieldItem {
        final int index;
        final String rawName;
        final String displayName;

        FieldItem(int index, String rawName, String displayName) {
            this.index = index;
            this.rawName = rawName;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            String shown = displayName != null && !displayName.isBlank() ? displayName : rawName;
            if (!shown.equals(rawName)) {
                shown += " (" + rawName + ")";
            }
            return shown;
        }
    }
}
