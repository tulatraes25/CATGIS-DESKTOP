package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for ML classification using Smile library.
 * Allows selecting features, algorithm, and viewing results.
 */
public class SmileClassificationDialog extends JDialog {

    private final JComboBox<String> algorithmCombo;
    private final JComboBox<String> featureFieldCombo;
    private final JComboBox<String> labelFieldCombo;
    private final JSlider trainTestSlider;
    private final JTextArea resultArea;
    private final JLabel statusLabel;

    public SmileClassificationDialog() {
        super((Frame) null, "Clasificacion ML - Smile", false);
        setSize(650, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 8, 4, 8));
        JLabel title = new JLabel("Clasificacion por Machine Learning");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        header.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Selecciona atributos y algoritmo para clasificar features");
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(4, 8, 4, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Algorithm
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Algoritmo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        String[] algos = new String[SmileClassifierService.Algorithm.values().length];
        for (int i = 0; i < algos.length; i++) {
            algos[i] = SmileClassifierService.Algorithm.values()[i].displayName();
        }
        algorithmCombo = new JComboBox<>(algos);
        form.add(algorithmCombo, gbc);

        // Feature field
        gbc.gridy = 1; gbc.gridx = 0;
        form.add(new JLabel("Campo features:"), gbc);
        gbc.gridx = 1;
        featureFieldCombo = new JComboBox<>(new String[]{"(Selecciona capa primero)"});
        form.add(featureFieldCombo, gbc);

        // Label field
        gbc.gridy = 2; gbc.gridx = 0;
        form.add(new JLabel("Campo clase:"), gbc);
        gbc.gridx = 1;
        labelFieldCombo = new JComboBox<>(new String[]{"(Selecciona capa primero)"});
        form.add(labelFieldCombo, gbc);

        // Train/test split
        gbc.gridy = 3; gbc.gridx = 0;
        form.add(new JLabel("Train/Test split:"), gbc);
        gbc.gridx = 1;
        trainTestSlider = new JSlider(50, 90, 80);
        trainTestSlider.setMajorTickSpacing(10);
        trainTestSlider.setPaintTicks(true);
        trainTestSlider.setPaintLabels(true);
        form.add(trainTestSlider, gbc);

        add(form, BorderLayout.NORTH);

        // Results
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createTitledBorder("Resultados"));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton runButton = new JButton("Ejecutar clasificacion");
        runButton.addActionListener(e -> runClassification());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(runButton);
        footer.add(closeButton);
        statusLabel = new JLabel("Selecciona un algoritmo y campos.");
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new SmileClassificationDialog().setVisible(true));
    }

    private void runClassification() {
        int algoIdx = algorithmCombo.getSelectedIndex();
        if (algoIdx < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un algoritmo.");
            return;
        }

        SmileClassifierService.Algorithm algorithm =
                SmileClassifierService.Algorithm.values()[algoIdx];

        // Generate synthetic data for demo (in production, read from raster/layer)
        int n = 1000;
        int numFeatures = 4;
        double[][] features = new double[n][numFeatures];
        int[] labels = new int[n];
        java.util.Random rng = new java.util.Random(42);

        for (int i = 0; i < n; i++) {
            int label = rng.nextInt(3);
            labels[i] = label;
            for (int j = 0; j < numFeatures; j++) {
                features[i][j] = label * 10 + rng.nextGaussian() * 3;
            }
        }

        statusLabel.setText("Entrenando " + algorithm.displayName() + "...");
        statusLabel.setForeground(Color.BLUE);

        new SwingWorker<SmileClassifierService.ClassificationResult, Void>() {
            @Override
            protected SmileClassifierService.ClassificationResult doInBackground() {
                double ratio = trainTestSlider.getValue() / 100.0;
                return SmileClassifierService.classify(features, labels, algorithm, ratio);
            }

            @Override
            protected void done() {
                try {
                    SmileClassifierService.ClassificationResult result = get();
                    resultArea.setText("=== Resultados de Clasificacion ===\n\n");
                    resultArea.append("Algoritmo: " + result.algorithmUsed() + "\n");
                    resultArea.append("Tiempo entrenamiento: " + result.trainingTimeMs() + " ms\n");
                    resultArea.append("Tiempo prediccion: " + result.predictionTimeMs() + " ms\n");
                    resultArea.append("Muestras de test: " + result.predictions().length + "\n");
                    resultArea.append("Correctas: " + result.correctCount() + "\n");
                    resultArea.append(String.format("Accuracy: %.2f%%\n", result.accuracy()));
                    resultArea.append("\nDistribucion de predicciones:\n");
                    int[] counts = new int[5];
                    for (int p : result.predictions()) {
                        if (p >= 0 && p < counts.length) counts[p]++;
                    }
                    for (int i = 0; i < counts.length; i++) {
                        resultArea.append("  Clase " + i + ": " + counts[i] + " muestras\n");
                    }

                    statusLabel.setText("Clasificacion completada. Accuracy: " +
                            String.format("%.2f%%", result.accuracy()));
                    statusLabel.setForeground(result.accuracy() > 80 ? new Color(0, 128, 0) : Color.ORANGE);
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    resultArea.setText("Error:\n" + e.getMessage());
                }
            }
        }.execute();
    }
}
