package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

public class LayerPropertiesDialog extends JDialog {

    private final Layer layer;

    private final JTextField nameField;
    private final JTextField pathField;
    private final JCheckBox visibleCheck;
    private final JCheckBox labelsVisibleCheck;
    private final JComboBox<String> labelFieldCombo;

    private final JButton fillColorButton;
    private final JButton borderColorButton;
    private final JButton lineColorButton;
    private final JButton pointColorButton;

    private final JSpinner lineWidthSpinner;
    private final JSpinner pointSizeSpinner;

    private Color fillColor;
    private Color borderColor;
    private Color lineColor;
    private Color pointColor;

    public LayerPropertiesDialog(Frame owner, Layer layer) {
        super(owner, "Propiedades de capa", true);
        this.layer = layer;

        this.fillColor = layer.getFillColor();
        this.borderColor = layer.getBorderColor();
        this.lineColor = layer.getLineColor();
        this.pointColor = layer.getPointColor();

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        JLabel title = new JLabel("Configuración general");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(title, gbc);
        row++;

        gbc.gridwidth = 1;
        addRow(formPanel, gbc, row++, "Nombre", nameField = new JTextField(layer.getName(), 24));
        pathField = new JTextField(buildPathDisplay(layer), 24);
        pathField.setEditable(false);
        pathField.setToolTipText(layer.getPath());
        addRow(formPanel, gbc, row++, "Ubicación en disco", pathField);

        visibleCheck = new JCheckBox("Capa visible", layer.isVisible());
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(visibleCheck, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        row++;

        JLabel styleTitle = new JLabel("Simbología");
        styleTitle.setFont(styleTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(styleTitle, gbc);
        row++;
        gbc.gridwidth = 1;

        fillColorButton = createColorButton(fillColor, "Color de relleno");
        addRow(formPanel, gbc, row++, "Relleno", fillColorButton);

        borderColorButton = createColorButton(borderColor, "Color de borde");
        addRow(formPanel, gbc, row++, "Borde", borderColorButton);

        lineColorButton = createColorButton(lineColor, "Color de línea");
        addRow(formPanel, gbc, row++, "Línea", lineColorButton);

        lineWidthSpinner = new JSpinner(new SpinnerNumberModel((double) layer.getLineWidth(), 0.5, 20.0, 0.5));
        addRow(formPanel, gbc, row++, "Grosor línea", lineWidthSpinner);

        pointColorButton = createColorButton(pointColor, "Color de punto");
        addRow(formPanel, gbc, row++, "Punto", pointColorButton);

        pointSizeSpinner = new JSpinner(new SpinnerNumberModel(layer.getPointSize(), 1, 40, 1));
        addRow(formPanel, gbc, row++, "Tamaño punto", pointSizeSpinner);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        row++;

        JLabel labelsTitle = new JLabel("Etiquetas");
        labelsTitle.setFont(labelsTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(labelsTitle, gbc);
        row++;
        gbc.gridwidth = 1;

        labelsVisibleCheck = new JCheckBox("Mostrar etiquetas", layer.isLabelsVisible());
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(labelsVisibleCheck, gbc);
        row++;

        List<String> fields = null;
        ShapefileData data = CatgisDesktopApp.mapPanel != null ? CatgisDesktopApp.mapPanel.getShapefileData(layer) : null;
        if (data != null) {
            fields = data.getAttributeNames();
        }
        labelFieldCombo = new JComboBox<>();
        if (fields != null && !fields.isEmpty()) {
            for (String field : fields) {
                labelFieldCombo.addItem(field);
            }
            if (layer.getLabelField() != null) {
                labelFieldCombo.setSelectedItem(layer.getLabelField());
            }
        } else {
            labelFieldCombo.addItem("(sin campos disponibles)");
            labelFieldCombo.setEnabled(false);
        }
        addRow(formPanel, gbc, row++, "Campo etiqueta", labelFieldCombo);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton applyButton = new JButton("Aplicar");
        JButton okButton = new JButton("Aceptar");
        JButton cancelButton = new JButton("Cancelar");

        applyButton.addActionListener(e -> applyChanges());
        okButton.addActionListener(e -> {
            if (applyChanges()) {
                dispose();
            }
        });
        cancelButton.addActionListener(e -> dispose());

        buttons.add(applyButton);
        buttons.add(okButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setSize(Math.max(getWidth(), 540), Math.max(getHeight(), 520));
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
    }

    private JButton createColorButton(Color initial, String title) {
        JButton button = new JButton("Elegir...");
        button.setBackground(initial);
        button.setOpaque(true);
        button.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(this, title, button.getBackground());
            if (selected != null) {
                button.setBackground(selected);
            }
        });
        return button;
    }

    private boolean applyChanges() {
        String newName = nameField.getText() != null ? nameField.getText().trim() : "";
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la capa no puede estar vacío.");
            return false;
        }

        layer.setName(newName);
        layer.setVisible(visibleCheck.isSelected());

        layer.setFillColor(withAlpha(fillColorButton.getBackground(), 120));
        layer.setBorderColor(borderColorButton.getBackground());
        layer.setLineColor(lineColorButton.getBackground());
        layer.setLineWidth(((Double) lineWidthSpinner.getValue()).floatValue());
        layer.setPointColor(pointColorButton.getBackground());
        layer.setPointSize((Integer) pointSizeSpinner.getValue());

        if (labelsVisibleCheck.isSelected() && labelFieldCombo.isEnabled() && labelFieldCombo.getSelectedItem() != null) {
            layer.setLabelsVisible(true);
            layer.setLabelField(labelFieldCombo.getSelectedItem().toString());
        } else {
            layer.setLabelsVisible(false);
            layer.setLabelField(null);
        }

        CatgisDesktopApp.markProjectDirty();

        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.refreshLayerList();
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.repaint();
        }
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Propiedades actualizadas: " + layer.getName());
        }
        return true;
    }

    private Color withAlpha(Color color, int alpha) {
        if (color == null) {
            return new Color(120, 170, 255, alpha);
        }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private String buildPathDisplay(Layer layer) {
        if (layer == null || layer.getPath() == null || layer.getPath().isBlank()) {
            return "Sin archivo asociado";
        }

        String path = layer.getPath().trim();
        String lower = path.toLowerCase();
        boolean looksStored = new java.io.File(path).isAbsolute()
                || new java.io.File(path).exists()
                || lower.endsWith(".shp")
                || lower.endsWith(".geojson")
                || lower.endsWith(".json")
                || lower.endsWith(".kml")
                || lower.endsWith(".tif")
                || lower.endsWith(".tiff")
                || lower.endsWith(".img")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".bmp")
                || lower.endsWith(".gif");

        return looksStored ? path : "Sin archivo asociado";
    }

    public static void open(Layer layer) {
        Frame owner = JOptionPane.getFrameForComponent(CatgisDesktopApp.layersPanel);
        LayerPropertiesDialog dialog = new LayerPropertiesDialog(owner, layer);
        dialog.setVisible(true);
    }
}
