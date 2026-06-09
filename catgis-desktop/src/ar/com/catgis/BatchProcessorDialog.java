package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Dialog for batch processing multiple GIS files.
 */
public class BatchProcessorDialog extends JDialog {

    private final JTextField inputDirField;
    private final JTextField outputDirField;
    private final JComboBox<String> operationCombo;
    private final JCheckBox includeSubdirsCheck;
    private final JTextArea fileListArea;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private List<File> currentFiles;

    public BatchProcessorDialog() {
        super((Frame) null, "Procesamiento por Lotes", false);
        setSize(650, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 8, 4, 8));
        JLabel title = new JLabel("Procesamiento por Lotes");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        header.add(title, BorderLayout.NORTH);
        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(4, 8, 4, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Input directory
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Directorio entrada:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        inputDirField = new JTextField(30);
        form.add(inputDirField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        JButton browseInput = new JButton("...");
        browseInput.addActionListener(e -> browseInputDir());
        form.add(browseInput, gbc);

        // Output directory
        gbc.gridy = 1; gbc.gridx = 0;
        form.add(new JLabel("Directorio salida:"), gbc);
        gbc.gridx = 1;
        outputDirField = new JTextField(30);
        form.add(outputDirField, gbc);
        gbc.gridx = 2;
        JButton browseOutput = new JButton("...");
        browseOutput.addActionListener(e -> browseOutputDir());
        form.add(browseOutput, gbc);

        // Operation
        gbc.gridy = 2; gbc.gridx = 0;
        form.add(new JLabel("Operacion:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        operationCombo = new JComboBox<>(new String[]{
                "Copiar archivos",
                "Calcular NDVI (B4/B3)",
                "Calcular NDWI (B2/B4)",
                "Calcular NBR (B4/B5)"
        });
        form.add(operationCombo, gbc);

        // Options
        gbc.gridy = 3; gbc.gridx = 0;
        includeSubdirsCheck = new JCheckBox("Incluir subdirectorios");
        form.add(includeSubdirsCheck, gbc);

        add(form, BorderLayout.NORTH);

        // File list
        fileListArea = new JTextArea();
        fileListArea.setEditable(false);
        fileListArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        fileListArea.setBorder(BorderFactory.createTitledBorder("Archivos encontrados"));
        add(new JScrollPane(fileListArea), BorderLayout.CENTER);

        // Progress
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton scanButton = new JButton("Escanear");
        scanButton.addActionListener(e -> scanDirectory());
        JButton runButton = new JButton("Ejecutar");
        runButton.addActionListener(e -> executeBatch());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(scanButton);
        footer.add(runButton);
        footer.add(closeButton);
        statusLabel = new JLabel("Selecciona un directorio y escanea.");
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new BatchProcessorDialog().setVisible(true));
    }

    private void browseInputDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar directorio de entrada");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            inputDirField.setText(chooser.getSelectedFile().getAbsolutePath());
            scanDirectory();
        }
    }

    private void browseOutputDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar directorio de salida");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputDirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void scanDirectory() {
        String inputPath = inputDirField.getText().trim();
        if (inputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un directorio de entrada.");
            return;
        }

        File inputDir = new File(inputPath);
        if (!inputDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "El directorio no existe.");
            return;
        }

        currentFiles = BatchProcessor.findAllGisFiles(inputDir);
        fileListArea.setText("");
        for (File f : currentFiles) {
            fileListArea.append(f.getName() + " (" + (f.length() / 1024) + " KB)\n");
        }
        statusLabel.setText(currentFiles.size() + " archivos encontrados.");
    }

    private void executeBatch() {
        if (currentFiles == null || currentFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay archivos para procesar. Escanea primero.");
            return;
        }

        String outputPath = outputDirField.getText().trim();
        if (outputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un directorio de salida.");
            return;
        }

        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String operation = (String) operationCombo.getSelectedItem();
        statusLabel.setText("Procesando...");
        statusLabel.setForeground(Color.BLUE);
        progressBar.setValue(0);

        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob(
                operation, currentFiles, outputDir, operation);

        // Execute in background
        new SwingWorker<BatchProcessor.BatchResult, Void>() {
            @Override
            protected BatchProcessor.BatchResult doInBackground() {
                return BatchProcessor.processBatch(job, (input, output) -> {
                    try {
                        switch (operation) {
                            case "Copiar archivos" -> {
                                java.nio.file.Files.copy(input.toPath(), output.toPath(),
                                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            }
                            case "Calcular NDVI (B4/B3)" -> BatchProcessor.applySpectralIndex(input, output, "NDVI", 4, 3);
                            case "Calcular NDWI (B2/B4)" -> BatchProcessor.applySpectralIndex(input, output, "NDWI", 2, 4);
                            case "Calcular NBR (B4/B5)" -> BatchProcessor.applySpectralIndex(input, output, "NBR", 4, 5);
                            default -> throw new RuntimeException("Operacion no soportada: " + operation);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, progress -> SwingUtilities.invokeLater(() -> progressBar.setValue(progress)));
            }

            @Override
            protected void done() {
                try {
                    BatchProcessor.BatchResult result = get();
                    statusLabel.setText("Completado: " + result.success() + " exitosos, " + result.failed() + " fallidos");
                    statusLabel.setForeground(result.failed() == 0 ? new Color(0, 128, 0) : Color.RED);
                    progressBar.setValue(100);

                    if (!result.errors().isEmpty()) {
                        StringBuilder sb = new StringBuilder("Errores:\n");
                        for (String err : result.errors()) {
                            sb.append("  - ").append(err).append("\n");
                        }
                        JOptionPane.showMessageDialog(BatchProcessorDialog.this, sb.toString(),
                                "Errores", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(BatchProcessorDialog.this,
                                "Procesamiento completado exitosamente.\n"
                                        + result.success() + " archivos procesados en " + outputPath,
                                "Exito", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        }.execute();
    }
}
