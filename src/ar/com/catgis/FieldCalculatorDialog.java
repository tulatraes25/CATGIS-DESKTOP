package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Locale;

public class FieldCalculatorDialog extends JDialog {

    private final AttributeTableWindow tableWindow;
    private final JTextArea expressionArea;
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

        JLabel subtitle = new JLabel("Doble clic en un campo para elegir cuál se modifica. Después armá la expresión.");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(new Color(95, 105, 120));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        DefaultListModel<FieldItem> fieldModel = new DefaultListModel<>();
        List<Integer> editableIndexes = tableWindow.getEditableColumnIndexes();
        for (Integer idx : editableIndexes) {
            fieldModel.addElement(new FieldItem(
                    idx,
                    tableWindow.getRawColumnNameAt(idx),
                    tableWindow.getDisplayColumnNameAt(idx)
            ));
        }

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

        JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        leftPanel.setBackground(Color.WHITE);

        JLabel leftLabel = new JLabel("Campos (doble clic = modificar, botón Insertar campo = usar en la fórmula):");
        leftLabel.setFont(leftLabel.getFont().deriveFont(Font.BOLD, 12f));
        leftPanel.add(leftLabel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(fieldsList), BorderLayout.CENTER);

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
        exprTop.add(new JLabel("Expresión:"), BorderLayout.WEST);

        JLabel examples = new JLabel("Ej.: =$length   |   =$area/10000   |   =[ancho]*$length   |   =round($length)");
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
                "0", "CE", "Insertar campo", "Área", "Longitud", "Perímetro",
                "=", "X", "Y", "Probar", "", ""
        };

        for (String key : keys) {
            if (key.isEmpty()) {
                keypad.add(new JLabel());
                continue;
            }
            JButton button = new JButton(key);
            switch (key) {
                case "CE":
                    button.addActionListener(e -> expressionArea.setText(""));
                    break;
                case "Insertar campo":
                    button.addActionListener(e -> insertSelectedField());
                    break;
                case "Área":
                    button.addActionListener(e -> insertGeometryShortcut("$area"));
                    break;
                case "Longitud":
                    button.addActionListener(e -> insertGeometryShortcut("$length"));
                    break;
                case "Perímetro":
                    button.addActionListener(e -> insertGeometryShortcut("$perimeter"));
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
        addFunctionButton(functions, "sqrt(");
        addFunctionButton(functions, "pow(");
        addFunctionButton(functions, "min(");
        addFunctionButton(functions, "max(");
        addFunctionButton(functions, "sin(");
        addFunctionButton(functions, "cos(");

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

        targetLabel = new JLabel("<ningún campo seleccionado>");
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

    private void setTargetField(FieldItem item) {
        targetField = item;
        if (item == null) {
            targetLabel.setText("<ningún campo seleccionado>");
            targetLabel.setToolTipText(null);
            return;
        }

        String shown = item.displayName != null && !item.displayName.isBlank() ? item.displayName : item.rawName;
        if (!shown.equals(item.rawName)) {
            shown += " (" + item.rawName + ")";
        }
        targetLabel.setText(shown);
        targetLabel.setToolTipText("Ese es el único campo que se va a modificar.");
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
            JOptionPane.showMessageDialog(this, "Seleccioná una fila en la tabla para probar la expresión.");
            return;
        }

        String expr = expressionArea.getText() != null ? expressionArea.getText().trim() : "";
        if (expr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribí una expresión primero.");
            return;
        }

        try {
            double value = tableWindow.evaluateExpressionForRow(expr, row);
            JOptionPane.showMessageDialog(this,
                    "Resultado para la fila seleccionada: " + trimNumber(value),
                    "Prueba de expresión",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo evaluar la expresión: " + ex.getMessage(),
                    "Calculadora de campos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyCalculation() {
        if (targetField == null) {
            JOptionPane.showMessageDialog(this, "Elegí primero un campo con doble clic.");
            return;
        }

        String expr = expressionArea.getText() != null ? expressionArea.getText().trim() : "";
        if (expr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribí una expresión antes de aplicar.");
            return;
        }

        try {
            int affected = tableWindow.applyFieldCalculation(targetField.index, expr, onlySelectedRowsCheck.isSelected());
            JOptionPane.showMessageDialog(this,
                    "Cálculo aplicado correctamente sobre " + affected + " registro(s).",
                    "Calculadora de campos",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo aplicar el cálculo: " + ex.getMessage(),
                    "Calculadora de campos",
                    JOptionPane.ERROR_MESSAGE);
        }
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
