package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for ML classification using real vector layer attributes.
 * Reads numeric fields from selected layer, trains classifier, shows results.
 */
public class SmileClassificationDialog extends JDialog {

    private final JComboBox<String> layerCombo;
    private final JComboBox<String> featureFieldCombo;
    private final JComboBox<String> labelFieldCombo;
    private final JComboBox<String> algorithmCombo;
    private final JSlider trainTestSlider;
    private final JTextArea resultArea;
    private final JLabel statusLabel;

    public SmileClassificationDialog() {
        super((Frame) null, "Clasificacion ML", false);
        setSize(650, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 8, 4, 8));
        JLabel title = new JLabel("Clasificacion por Machine Learning");
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

        // Layer
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Capa:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        layerCombo = new JComboBox<>();
        refreshLayers();
        layerCombo.addActionListener(e -> refreshFields());
        form.add(layerCombo, gbc);

        // Feature field
        gbc.gridy = 1; gbc.gridx = 0;
        form.add(new JLabel("Campo features:"), gbc);
        gbc.gridx = 1;
        featureFieldCombo = new JComboBox<>();
        form.add(featureFieldCombo, gbc);

        // Label field
        gbc.gridy = 2; gbc.gridx = 0;
        form.add(new JLabel("Campo clase:"), gbc);
        gbc.gridx = 1;
        labelFieldCombo = new JComboBox<>();
        form.add(labelFieldCombo, gbc);

        // Algorithm
        gbc.gridy = 3; gbc.gridx = 0;
        form.add(new JLabel("Algoritmo:"), gbc);
        gbc.gridx = 1;
        String[] algos = new String[SmileClassifierService.Algorithm.values().length];
        for (int i = 0; i < algos.length; i++) {
            algos[i] = SmileClassifierService.Algorithm.values()[i].displayName();
        }
        algorithmCombo = new JComboBox<>(algos);
        form.add(algorithmCombo, gbc);

        // Train/test split
        gbc.gridy = 4; gbc.gridx = 0;
        form.add(new JLabel("Train/Test:"), gbc);
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
        statusLabel = new JLabel("Selecciona una capa y campos.");
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new SmileClassificationDialog().setVisible(true));
    }

    private void refreshLayers() {
        layerCombo.removeAllItems();
        if (AppContext.mapPanel() == null) return;
        for (Layer layer : AppContext.mapPanel().getRenderOrderLayers()) {
            ShapefileData data = ar.com.catgis.data.vector.VectorLayerUtils.ensureVectorData(layer);
            if (data != null && data.getFeatures() != null && !data.getFeatures().isEmpty()) {
                layerCombo.addItem(layer.getName());
            }
        }
    }

    private void refreshFields() {
        featureFieldCombo.removeAllItems();
        labelFieldCombo.removeAllItems();
        String layerName = (String) layerCombo.getSelectedItem();
        if (layerName == null) return;

        Layer targetLayer = findLayer(layerName);
        if (targetLayer == null) return;

        ShapefileData data = ar.com.catgis.data.vector.VectorLayerUtils.ensureVectorData(targetLayer);
        if (data == null || data.getSchema() == null) return;

        for (AttributeDescriptor desc : data.getSchema().getAttributeDescriptors()) {
            if (desc instanceof GeometryDescriptor) continue;
            Class<?> binding = desc.getType().getBinding();
            if (binding != null && (Number.class.isAssignableFrom(binding)
                    || int.class.equals(binding) || double.class.equals(binding)
                    || float.class.equals(binding) || long.class.equals(binding))) {
                featureFieldCombo.addItem(desc.getLocalName());
                labelFieldCombo.addItem(desc.getLocalName());
            }
        }
    }

    private Layer findLayer(String name) {
        if (AppContext.mapPanel() == null) return null;
        for (Layer layer : AppContext.mapPanel().getRenderOrderLayers()) {
            if (layer.getName().equals(name)) return layer;
        }
        return null;
    }

    private void runClassification() {
        int algoIdx = algorithmCombo.getSelectedIndex();
        if (algoIdx < 0) {
            NotificationManager.warn(this, null, "Selecciona un algoritmo.");
            return;
        }

        String layerName = (String) layerCombo.getSelectedItem();
        String featureField = (String) featureFieldCombo.getSelectedItem();
        String labelField = (String) labelFieldCombo.getSelectedItem();

        if (layerName == null || featureField == null || labelField == null) {
            NotificationManager.warn(this, null, "Selecciona capa y campos.");
            return;
        }

        if (featureField.equals(labelField)) {
            NotificationManager.warn(this, null, "El campo de features y el campo de clase deben ser diferentes.");
            return;
        }

        Layer targetLayer = findLayer(layerName);
        if (targetLayer == null) return;

        ShapefileData data = ar.com.catgis.data.vector.VectorLayerUtils.ensureVectorData(targetLayer);
        if (data == null || data.getFeatures() == null) return;

        SmileClassifierService.Algorithm algorithm = SmileClassifierService.Algorithm.values()[algoIdx];

        // Extract real data from features
        List<SimpleFeature> features = new ArrayList<>(data.getFeatures());

        // First pass: enumerate unique string labels to consecutive integers
        java.util.Map<String, Integer> labelMap = new java.util.LinkedHashMap<>();
        int nextLabel = 0;
        for (SimpleFeature f : features) {
            Object labelVal = f.getAttribute(labelField);
            if (labelVal == null) continue;
            String labelStr = String.valueOf(labelVal).trim();
            if (!labelMap.containsKey(labelStr)) {
                labelMap.put(labelStr, nextLabel++);
            }
        }
        if (labelMap.isEmpty()) {
            NotificationManager.warn(this, null, "No se encontraron valores validos en el campo de clase.");
            return;
        }

        double[][] featureMatrix = new double[features.size()][1];
        int[] labels = new int[features.size()];
        int validCount = 0;

        for (int i = 0; i < features.size(); i++) {
            SimpleFeature f = features.get(i);
            Object featureVal = f.getAttribute(featureField);
            Object labelVal = f.getAttribute(labelField);

            if (featureVal == null || labelVal == null) continue;

            double fv;
            try { fv = Double.parseDouble(String.valueOf(featureVal)); }
            catch (NumberFormatException e) { continue; }

            int lv;
            String labelStr = String.valueOf(labelVal).trim();
            if (labelMap.containsKey(labelStr)) {
                lv = labelMap.get(labelStr);
            } else {
                continue; // Skip unknown labels
            }

            featureMatrix[validCount] = new double[]{fv};
            labels[validCount] = lv;
            validCount++;
        }

        if (validCount < 10) {
            NotificationManager.warn(this,
                    "Datos insuficientes",
                    "Solo se pudieron extraer " + validCount + " muestras validas. Se necesitan al menos 10.");
            return;
        }

        // Trim arrays
        int finalValidCount = validCount;
        double[][] finalFeatures = new double[validCount][1];
        int[] finalLabels = new int[validCount];
        System.arraycopy(featureMatrix, 0, finalFeatures, 0, validCount);
        System.arraycopy(labels, 0, finalLabels, 0, validCount);

        statusLabel.setText("Entrenando " + algorithm.displayName() + "...");
        statusLabel.setForeground(Color.BLUE);

        new SwingWorker<SmileClassifierService.ClassificationResult, Void>() {
            @Override
            protected SmileClassifierService.ClassificationResult doInBackground() {
                double ratio = trainTestSlider.getValue() / 100.0;
                return SmileClassifierService.classify(finalFeatures, finalLabels, algorithm, ratio);
            }

            @Override
            protected void done() {
                try {
                    SmileClassifierService.ClassificationResult result = get();
                    resultArea.setText("=== Resultados de Clasificacion ===\n\n");
                    resultArea.append("Capa: " + layerName + "\n");
                    resultArea.append("Muestras totales: " + features.size() + "\n");
                    resultArea.append("Muestras validas: " + finalValidCount + "\n");
                    resultArea.append("Campo features: " + featureField + "\n");
                    resultArea.append("Campo clase: " + labelField + "\n");
                    resultArea.append("Algoritmo: " + result.algorithmUsed() + "\n");
                    resultArea.append("Tiempo entrenamiento: " + result.trainingTimeMs() + " ms\n");
                    resultArea.append("Tiempo prediccion: " + result.predictionTimeMs() + " ms\n");
                    resultArea.append("Muestras de test: " + result.predictions().length + "\n");
                    resultArea.append("Correctas: " + result.correctCount() + "\n");
                    resultArea.append(String.format("Accuracy: %.2f%%\n", result.accuracy()));
                    resultArea.append("\nDistribucion de predicciones:\n");
                    int maxLabel = 0;
                    for (int l : finalLabels) if (l > maxLabel) maxLabel = l;
                    int[] counts = new int[maxLabel + 1];
                    for (int p : result.predictions()) {
                        if (p >= 0 && p < counts.length) counts[p]++;
                    }
                    for (int i = 0; i < counts.length; i++) {
                        resultArea.append("  Clase " + i + ": " + counts[i] + " muestras\n");
                    }

                    statusLabel.setText("Completado. Accuracy: " + String.format("%.2f%%", result.accuracy()));
                    statusLabel.setForeground(result.accuracy() > 70 ? new Color(0, 128, 0) : Color.ORANGE);
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    resultArea.setText("Error:\n" + e.getMessage());
                }
            }
        }.execute();
    }
}
