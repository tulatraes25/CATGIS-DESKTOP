package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;\nimport java.util.concurrent.CancellationException;

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
    private final JButton scanButton;
    private final JButton runButton;
    private List<File> currentFiles = List.of();
    private SwingWorker<List<File>, Void> scanWorker;
    private int scanGeneration;
    private String scannedInputPath = "";
    private boolean scannedIncludeSubdirs;

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
                "Calcular NBR (B4/B5)",
                "Reclasificar raster"
        });
        form.add(operationCombo, gbc);

        // Options
        gbc.gridy = 3; gbc.gridx = 0;
        includeSubdirsCheck = new JCheckBox("Incluir subdirectorios");
        form.add(includeSubdirsCheck, gbc);

        // Form and file preview share the body; the title keeps the NORTH region.

        // File list
        fileListArea = new JTextArea();
        fileListArea.setEditable(false);
        fileListArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        fileListArea.setBorder(BorderFactory.createTitledBorder("Archivos encontrados"));
        JPanel bodyPanel = new JPanel(new BorderLayout(8, 8));
        bodyPanel.add(form, BorderLayout.NORTH);
        bodyPanel.add(new JScrollPane(fileListArea), BorderLayout.CENTER);
        add(bodyPanel, BorderLayout.CENTER);

        // Progress and controls coexist in the footer; neither can replace the other.
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scanButton = new JButton("Escanear");
        scanButton.addActionListener(e -> scanDirectory());
        runButton = new JButton("Ejecutar");
        runButton.setEnabled(false);
        runButton.addActionListener(e -> executeBatch());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(scanButton);
        footer.add(runButton);
        footer.add(closeButton);
        statusLabel = new JLabel("Selecciona un directorio y escanea.");
        footer.add(statusLabel);
        JPanel southPanel = new JPanel(new BorderLayout(4, 4));
        southPanel.add(progressPanel, BorderLayout.NORTH);
        southPanel.add(footer, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        includeSubdirsCheck.addActionListener(e -> {
            if (!inputDirField.getText().trim().isEmpty()) {
                scanDirectory();
            }
        });
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
            currentFiles = List.of();
            runButton.setEnabled(false);
            NotificationManager.warn(this, null, "Selecciona un directorio de entrada.");
            return;
        }

        File inputDir = new File(inputPath);
        if (!inputDir.isDirectory()) {
            currentFiles = List.of();
            runButton.setEnabled(false);
            NotificationManager.warn(this, null, "El directorio no existe.");
            return;
        }

        int generation = ++scanGeneration;
        if (scanWorker != null && !scanWorker.isDone()) {
            scanWorker.cancel(true);
        }
        boolean includeSubdirs = includeSubdirsCheck.isSelected();
        String normalizedPath = inputDir.toPath().toAbsolutePath().normalize().toString();
        currentFiles = List.of();
        scannedInputPath = "";
        runButton.setEnabled(false);
        scanButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setText(includeSubdirs ? "Escaneando directorio y subdirectorios..." : "Escaneando directorio...");

        scanWorker = new SwingWorker<>() {
            @Override
            protected List<File> doInBackground() throws Exception {
                return BatchFileDiscovery.find(inputDir, includeSubdirs);
            }

            @Override
            protected void done() {
                if (generation != scanGeneration) {
                    return;
                }
                scanButton.setEnabled(true);
                progressBar.setIndeterminate(false);
                try {
                    List<File> found = get();
                    currentFiles = found;
                    scannedInputPath = normalizedPath;
                    scannedIncludeSubdirs = includeSubdirs;
                    fileListArea.setText("");
                    java.nio.file.Path root = inputDir.toPath().toAbsolutePath().normalize();
                    for (File file : found) {
                        java.nio.file.Path absolute = file.toPath().toAbsolutePath().normalize();
                        String display;
                        try {
                            display = root.relativize(absolute).toString();
                        } catch (IllegalArgumentException unrelatedRoot) {
                            display = file.getName();
                        }
                        fileListArea.append(display + " (" + (file.length() / 1024) + " KB)\n");
                    }
                    runButton.setEnabled(!found.isEmpty());
                    statusLabel.setForeground(found.isEmpty() ? Color.DARK_GRAY : new Color(0, 128, 0));
                    statusLabel.setText(found.size() + " archivos encontrados.");
                } catch (CancellationException cancelled) {
                    currentFiles = List.of();
                    statusLabel.setForeground(Color.DARK_GRAY);
                    statusLabel.setText("Escaneo cancelado.");
                } catch (Exception failure) {
                    currentFiles = List.of();
                    runButton.setEnabled(false);
                    Throwable cause = failure.getCause() != null ? failure.getCause() : failure;
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("No se pudo completar el escaneo.");
                    NotificationManager.warn(BatchProcessorDialog.this,
                            "Error de escaneo",
                            cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
                }
            }
        };
        scanWorker.execute();
    }

    private void executeBatch() {
        String inputPath = inputDirField.getText().trim();
        String normalizedInput = inputPath.isEmpty() ? ""
                : new File(inputPath).toPath().toAbsolutePath().normalize().toString();
        if (currentFiles.isEmpty() || !normalizedInput.equals(scannedInputPath)
                || includeSubdirsCheck.isSelected() != scannedIncludeSubdirs) {
            runButton.setEnabled(false);
            NotificationManager.warn(this, null,
                    "El directorio u opciones cambiaron. Escanea nuevamente antes de ejecutar.");
            return;
        }

        String outputPath = outputDirField.getText().trim();
        if (outputPath.isEmpty()) {
            NotificationManager.warn(this, null, "Selecciona un directorio de salida.");
            return;
        }

        File outputDir = new File(outputPath);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            NotificationManager.warn(this, null, "No se pudo crear el directorio de salida.");
            return;
        }
        if (!outputDir.isDirectory()) {
            NotificationManager.warn(this, null, "La salida seleccionada no es un directorio.");
            return;
        }

        String operation = (String) operationCombo.getSelectedItem();
        runButton.setEnabled(false);
        scanButton.setEnabled(false);
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
                            case "Reclasificar raster" -> BatchProcessor.applyReclassify(input, output, "0-128=1;128-256=2");
                            default -> throw new RuntimeException("Operacion no soportada: " + operation);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error en operacion batch '" + operation + "' sobre " + input.getName(), e);
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
                    scanButton.setEnabled(true);
                    runButton.setEnabled(!currentFiles.isEmpty()
                            && normalizedInput.equals(scannedInputPath)
                            && includeSubdirsCheck.isSelected() == scannedIncludeSubdirs);

                    if (!result.errors().isEmpty()) {
                        StringBuilder sb = new StringBuilder("Errores:\n");
                        for (String err : result.errors()) {
                            sb.append("  - ").append(err).append("\n");
                        }
                        NotificationManager.warn(BatchProcessorDialog.this, "Errores", sb.toString());
                    } else {
                        NotificationManager.info(BatchProcessorDialog.this,
                                "Exito",
                                "Procesamiento completado exitosamente.\n"
                                        + result.success() + " archivos procesados en " + outputPath);
                    }
                } catch (Exception e) {
                    scanButton.setEnabled(true);
                    runButton.setEnabled(!currentFiles.isEmpty()
                            && normalizedInput.equals(scannedInputPath)
                            && includeSubdirsCheck.isSelected() == scannedIncludeSubdirs);
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        }.execute();
    }
}
