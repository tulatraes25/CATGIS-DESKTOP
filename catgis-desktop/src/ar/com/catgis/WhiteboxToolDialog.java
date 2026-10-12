package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog for selecting and running WhiteboxTools geoprocessing tools.
 */
public class WhiteboxToolDialog extends JDialog {

    private final JComboBox<String> categoryCombo;
    private final JList<String> toolList;
    private final JTextArea descriptionArea;
    private final JPanel paramsPanel;
    private final JLabel statusLabel;
    private final JButton runButton;
    private String detectedExecutable;
    private List<WhiteboxToolRegistry.ToolInfo> currentTools;
    private WhiteboxToolRegistry.ToolInfo selectedTool;

    public WhiteboxToolDialog() {
        super((Frame) null, "Herramientas WhiteboxTools", false);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 8, 4, 8));
        JLabel title = new JLabel("Herramientas de analisis geoespacial");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        header.add(title, BorderLayout.NORTH);
        JLabel availabilityLabel = new JLabel("Comprobando instalacion de WhiteboxTools...");
        availabilityLabel.setBorder(new EmptyBorder(4, 0, 4, 0));
        header.add(availabilityLabel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Left panel: category + tool list
        JPanel leftPanel = new JPanel(new BorderLayout(4, 4));
        leftPanel.setBorder(new EmptyBorder(4, 8, 4, 4));

        List<String> categories = WhiteboxToolRegistry.getCategories();
        categoryCombo = new JComboBox<>(categories.toArray(new String[0]));
        categoryCombo.addActionListener(e -> refreshToolList());
        leftPanel.add(categoryCombo, BorderLayout.NORTH);

        currentTools = WhiteboxToolRegistry.getTools();
        String[] toolNames = currentTools.stream().map(WhiteboxToolRegistry.ToolInfo::name).toArray(String[]::new);
        toolList = new JList<>(toolNames);
        toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolList.addListSelectionListener(e -> onToolSelected());
        leftPanel.add(new JScrollPane(toolList), BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);

        // Right panel: description + params + run
        JPanel rightPanel = new JPanel(new BorderLayout(4, 4));
        rightPanel.setBorder(new EmptyBorder(4, 4, 4, 8));

        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        rightPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        paramsPanel = new JPanel(new GridBagLayout());
        paramsPanel.setBorder(BorderFactory.createTitledBorder("Parametros"));
        rightPanel.add(paramsPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        runButton = new JButton("Ejecutar");
        runButton.setEnabled(false);
        runButton.addActionListener(e -> executeTool());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(runButton);
        footer.add(closeButton);
        statusLabel = new JLabel(" ");
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);

        if (!currentTools.isEmpty()) {
            toolList.setSelectedIndex(0);
        }

        // Executable detection can invoke an external process; never run it on EDT.
        ToolAvailabilityProbe.start(ExternalToolService::findWhiteboxTools, executable -> {
            detectedExecutable = executable;
            boolean available = executable != null && !executable.isBlank();
            runButton.setEnabled(available);
            availabilityLabel.setText(available
                    ? "WhiteboxTools detectado. Podes ejecutar las herramientas."
                    : "WhiteboxTools no detectado. Instalalo y agregalo al PATH.");
        });
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new WhiteboxToolDialog().setVisible(true));
    }

    private void refreshToolList() {
        String category = (String) categoryCombo.getSelectedItem();
        if (category == null) return;
        currentTools = WhiteboxToolRegistry.getToolsByCategory(category);
        String[] toolNames = currentTools.stream().map(WhiteboxToolRegistry.ToolInfo::name).toArray(String[]::new);
        toolList.setListData(toolNames);
        if (toolNames.length > 0) toolList.setSelectedIndex(0);
    }

    private void onToolSelected() {
        int idx = toolList.getSelectedIndex();
        if (idx < 0 || idx >= currentTools.size()) return;
        selectedTool = currentTools.get(idx);
        descriptionArea.setText(selectedTool.description() + "\n\nCategoria: " + selectedTool.category()
                + "\nComando: " + selectedTool.id());
        rebuildParamsPanel();
    }

    private void rebuildParamsPanel() {
        paramsPanel.removeAll();
        if (selectedTool == null) return;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;
        for (WhiteboxToolRegistry.ParamInfo param : selectedTool.params()) {
            gbc.gridy = row;
            gbc.gridx = 0;
            gbc.weightx = 0;
            JLabel label = new JLabel(param.label() + (param.required() ? " *" : "") + ":");
            paramsPanel.add(label, gbc);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            JTextField field = new JTextField(20);
            field.setName(param.name());
            paramsPanel.add(field, gbc);
            row++;
        }
        paramsPanel.revalidate();
        paramsPanel.repaint();
    }

    private void executeTool() {
        WhiteboxToolRegistry.ToolInfo tool = selectedTool;
        if (tool == null || detectedExecutable == null) {
            return;
        }

        Map<String, String> values = new LinkedHashMap<>();
        for (Component comp : paramsPanel.getComponents()) {
            if (comp instanceof JTextField field && field.getName() != null) {
                values.put(field.getName(), field.getText());
            }
        }

        String[] command;
        try {
            command = WhiteboxToolExecutionSupport.buildCommand(detectedExecutable, tool, values);
        } catch (IllegalArgumentException invalid) {
            NotificationManager.warn(this, null, invalid.getMessage());
            return;
        }

        runButton.setEnabled(false);
        statusLabel.setText("Ejecutando " + tool.name() + "...");
        statusLabel.setForeground(Color.BLUE);

        ExternalToolService.executeAsync(command)
                .whenComplete((result, failure) -> SwingUtilities.invokeLater(() -> {
                    runButton.setEnabled(detectedExecutable != null);
                    if (failure != null || result == null || !result.success()) {
                        statusLabel.setText("Error en la ejecucion");
                        statusLabel.setForeground(Color.RED);
                        String detail = failure != null ? failure.getMessage()
                                : result != null ? result.error() : "Resultado vacio.";
                        NotificationManager.error(this,
                                "Error", "Error al ejecutar la herramienta:\n" + detail);
                        return;
                    }

                    statusLabel.setText("Completado exitosamente");
                    statusLabel.setForeground(new Color(0, 128, 0));
                    NotificationManager.info(this,
                            "Exito", "Herramienta ejecutada correctamente.\n\n" + result.output());
                }));
    }
}
