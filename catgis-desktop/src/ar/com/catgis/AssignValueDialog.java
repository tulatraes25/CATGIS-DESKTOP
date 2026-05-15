package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AssignValueDialog extends JDialog {

    private final AttributeTableWindow tableWindow;
    private final JList<FieldItem> fieldsList;
    private final JTextField valueField;
    private final JCheckBox onlySelectedRowsCheck;
    private final JLabel targetLabel;

    private FieldItem targetField;

    public AssignValueDialog(AttributeTableWindow tableWindow) {
        super(tableWindow, "Asignar valor a un campo", false);
        this.tableWindow = tableWindow;

        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        header.setBackground(new Color(248, 250, 252));

        JLabel title = new JLabel("Asignar valor a un campo");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(new Color(32, 42, 58));

        JLabel subtitle = new JLabel("Doble clic en un campo para elegir cuál se modifica. Luego escribí el valor a asignar.");
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

        JLabel leftLabel = new JLabel("Campos editables (doble clic para elegir):");
        leftLabel.setFont(leftLabel.getFont().deriveFont(Font.BOLD, 12f));
        leftPanel.add(leftLabel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(fieldsList), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(16, 16, 16, 16)
        ));
        rightPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        rightPanel.add(new JLabel("Campo a modificar:"), gbc);

        targetLabel = new JLabel("Ninguno");
        targetLabel.setFont(targetLabel.getFont().deriveFont(Font.BOLD, 13f));
        targetLabel.setForeground(new Color(37, 99, 235));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        rightPanel.add(targetLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        rightPanel.add(new JLabel("Valor a asignar:"), gbc);

        valueField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        rightPanel.add(valueField, gbc);

        onlySelectedRowsCheck = new JCheckBox("Aplicar solo a filas seleccionadas");
        onlySelectedRowsCheck.setOpaque(false);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1;
        rightPanel.add(onlySelectedRowsCheck, gbc);

        JLabel hint = new JLabel("Ejemplo: op_campo = Puccini   |   yacimiento = Manantiales   |   fecha_rel = 11/05/2010");
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
        hint.setForeground(new Color(95, 105, 120));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        rightPanel.add(hint, gbc);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.42);
        add(splitPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        footer.setBackground(new Color(248, 250, 252));

        JButton applyButton = new JButton("Aplicar");
        JButton closeButton = new JButton("Cerrar");
        applyButton.addActionListener(e -> applyValue());
        closeButton.addActionListener(e -> dispose());
        footer.add(applyButton);
        footer.add(closeButton);
        add(footer, BorderLayout.SOUTH);

        if (!fieldModel.isEmpty()) {
            fieldsList.setSelectedIndex(0);
            setTargetField(fieldModel.get(0));
        }

        setSize(860, 340);
        setMinimumSize(new Dimension(760, 300));
        setLocationRelativeTo(tableWindow);
    }

    public static void open(AttributeTableWindow tableWindow) {
        AssignValueDialog dialog = new AssignValueDialog(tableWindow);
        dialog.setVisible(true);
    }

    private void setTargetField(FieldItem item) {
        this.targetField = item;
        if (item == null) {
            targetLabel.setText("Ninguno");
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

    private void applyValue() {
        if (targetField == null) {
            JOptionPane.showMessageDialog(this, "Elegí primero un campo con doble clic.");
            return;
        }

        String value = valueField.getText() != null ? valueField.getText() : "";

        try {
            int affected = tableWindow.assignConstantValue(targetField.index, value, onlySelectedRowsCheck.isSelected());
            JOptionPane.showMessageDialog(this,
                    "Valor asignado correctamente sobre " + affected + " registro(s).",
                    "Asignar valor a un campo",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo asignar el valor al campo", ex);
            AppErrorSupport.showErrorDialog(
                    this,
                    "Asignar valor a un campo",
                    "No se pudo asignar el valor.",
                    ex
            );
        }
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
