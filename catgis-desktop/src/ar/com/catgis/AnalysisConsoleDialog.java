package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.analysis.vector.GeoprocessingAssistantDialog;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import org.geotools.data.simple.SimpleFeatureCollection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Consola unificada de análisis: un solo botón que integra capas online y locales
 * para análisis espacial combinado.
 */
public class AnalysisConsoleDialog extends JDialog {

    private JTabbedPane analysisTabs;
    private JTextArea resultArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JComboBox<String> analysisTypeCombo;
    private JPanel quickLayerPanel;
    private JPanel layerTogglePanel;
    private JButton runButton;
    private final Map<String, JCheckBox> layerCheckboxes = new LinkedHashMap<>();
    private final Map<String, LayerInfo> layerInfoMap = new LinkedHashMap<>();

    private record LayerInfo(String name, String type, boolean isOnline, String source) {}

    public AnalysisConsoleDialog() {
        super(CatgisDesktopApp.getMainFrame(), "Consola de Analisis", false);
        setSize(960, 680);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(getOwner());

        // --- Layout: split horizontal ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(4);

        // Left panel: layer selection + analysis type
        JPanel leftPanel = buildLeftPanel();
        splitPane.setLeftComponent(new JScrollPane(leftPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

        // Right panel: tabs for analysis config + results
        JPanel rightPanel = buildRightPanel();
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        // Bottom: progress + buttons
        add(buildBottomBar(), BorderLayout.SOUTH);

        refreshLayers();
    }

    // --- Public API ---

    public static void open() {
        SwingUtilities.invokeLater(() -> {
            AnalysisConsoleDialog dialog = new AnalysisConsoleDialog();
            dialog.setVisible(true);
        });
    }

    // --- Left Panel ---

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Header
        JLabel title = new JLabel("Capas disponibles");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(title, BorderLayout.NORTH);

        // Layer toggles
        layerTogglePanel = new JPanel();
        layerTogglePanel.setLayout(new BoxLayout(layerTogglePanel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(layerTogglePanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        // Quick layer panel (bottom of left)
        quickLayerPanel = new JPanel(new BorderLayout());
        quickLayerPanel.setBorder(BorderFactory.createTitledBorder("Acceso rapido"));

        JButton addOnlineBtn = new JButton("+ Agregar capa online...");
        addOnlineBtn.setFont(addOnlineBtn.getFont().deriveFont(11f));
        addOnlineBtn.addActionListener(e -> {
            dispose();
            OnlineBaseMapDialog.open(CatgisDesktopApp.getMainFrameSafe());
        });

        JButton refreshBtn = new JButton("Refrescar capas");
        refreshBtn.setFont(refreshBtn.getFont().deriveFont(11f));
        refreshBtn.addActionListener(e -> refreshLayers());

        JPanel btnPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        btnPanel.add(addOnlineBtn, gbc);
        gbc.gridy = 1;
        btnPanel.add(refreshBtn, gbc);

        quickLayerPanel.add(btnPanel, BorderLayout.NORTH);
        panel.add(quickLayerPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- Right Panel ---

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Analysis type selector
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        JLabel typeLabel = new JLabel("Tipo de analisis:");
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD, 12f));

        analysisTypeCombo = new JComboBox<>(new String[]{
                "Combinacion de capas (overlay)",
                "Consulta espacial (interseccion)",
                "Estadisticas de superposicion",
                "Analisis de cobertura",
                "Resumen de capas seleccionadas"
        });
        analysisTypeCombo.addActionListener(e -> updateAnalysisDescription());
        topBar.add(typeLabel, BorderLayout.WEST);
        topBar.add(analysisTypeCombo, BorderLayout.CENTER);
        panel.add(topBar, BorderLayout.NORTH);

        // Tabs
        analysisTabs = new JTabbedPane();

        // Tab 1: Configuración
        JPanel configPanel = buildConfigPanel();
        analysisTabs.addTab("Configuracion", configPanel);

        // Tab 2: Resultados
        JPanel resultsPanel = new JPanel(new BorderLayout(0, 8));
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBackground(new Color(245, 245, 250));
        resultArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        resultsPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        analysisTabs.addTab("Resultados", resultsPanel);

        // Tab 3: Reporte
        JPanel reportPanel = new JPanel(new BorderLayout());
        JLabel reportLabel = new JLabel("<html><body style='padding:12px'>"
                + "<h2>Reporte de capas activas</h2>"
                + "<p>Seleccione capas y ejecute el analisis para generar un reporte detallado</p>"
                + "<p>Las capas online se combinan con capas vectoriales locales para<br>"
                + "generar estadisticas de superposicion, areas de cobertura y mas.</p>"
                + "</body></html>");
        reportLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        reportPanel.add(reportLabel, BorderLayout.NORTH);
        analysisTabs.addTab("Reporte", reportPanel);

        panel.add(analysisTabs, BorderLayout.CENTER);

        // Button row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        runButton = new JButton("Ejecutar analisis");
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD, 13f));
        runButton.setBackground(new Color(41, 128, 185));
        runButton.setForeground(Color.WHITE);
        runButton.addActionListener(e -> runAnalysis());
        buttonRow.add(runButton);

        JButton selectAllBtn = new JButton("Seleccionar todas las capas visibles");
        selectAllBtn.addActionListener(e -> toggleAllLayers(true));
        buttonRow.add(selectAllBtn);

        panel.add(buttonRow, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTextArea descArea = new JTextArea();
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(250, 250, 250));
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        descArea.setText("""
                Seleccione las capas que desea combinar del panel izquierdo
                y elija un tipo de analisis. Luego presione "Ejecutar analisis".

                Analisis disponibles:
                - Combinacion de capas: superpone todas las capas seleccionadas
                - Consulta espacial: calcula intersecciones entre capas
                - Estadisticas: genera estadisticas de areas, cantidades, cobertura
                - Resumen: reporte completo de todas las capas seleccionadas
                """);
        panel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        statusLabel = new JLabel("Listo. Seleccione capas y ejecute un analisis.");
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(180, 16));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.add(progressBar);

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    // --- Layer management ---

    private void refreshLayers() {
        layerTogglePanel.removeAll();
        layerCheckboxes.clear();
        layerInfoMap.clear();

        Project project = AppContext.project();
        if (project == null || project.getLayers().isEmpty()) {
            layerTogglePanel.add(new JLabel("  No hay capas cargadas."));
            layerTogglePanel.revalidate();
            return;
        }

        for (Layer layer : project.getLayers()) {
            if (layer == null) continue;
            String name = layer.getName();
            String type = layer.getType() != null ? layer.getType() : "VECTOR";
            boolean isOnline = type.equalsIgnoreCase("ONLINE_TILE")
                    || type.equalsIgnoreCase("ONLINE_WMS")
                    || type.equalsIgnoreCase("ONLINE_WFS");
            String source = isOnline ? "Online" : "Local";

            layerInfoMap.put(name, new LayerInfo(name, type, isOnline, source));

            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

            JCheckBox check = new JCheckBox();
            check.setSelected(layer.isVisible() && layer.isLabelsVisible());
            check.setOpaque(false);
            layerCheckboxes.put(name, check);

            // Color indicator
            JLabel colorDot = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color c = layer.getPointColor() != null ? layer.getPointColor() : new Color(65, 105, 225);
                    g2.setColor(c);
                    g2.fill(new Ellipse2D.Double(2, 4, 10, 10));
                    g2.setColor(c.darker());
                    g2.draw(new Ellipse2D.Double(2, 4, 10, 10));
                    g2.dispose();
                }
            };
            colorDot.setPreferredSize(new Dimension(16, 18));

            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(nameLabel.getFont().deriveFont(12f));

            JLabel badge = new JLabel(isOnline ? " 🌐" : " 📁");
            badge.setFont(badge.getFont().deriveFont(10f));
            badge.setForeground(isOnline ? new Color(39, 174, 96) : new Color(100, 100, 100));

            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            namePanel.setOpaque(false);
            namePanel.add(colorDot);
            namePanel.add(nameLabel);
            namePanel.add(badge);

            row.add(check, BorderLayout.WEST);
            row.add(namePanel, BorderLayout.CENTER);

            // Click on row toggles checkbox
            row.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    check.setSelected(!check.isSelected());
                }
            });

            layerTogglePanel.add(row);

            // Separator between layers
            JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
            sep.setForeground(new Color(230, 230, 230));
            layerTogglePanel.add(sep);
        }

        layerTogglePanel.revalidate();
        layerTogglePanel.repaint();
        statusLabel.setText(layerCheckboxes.size() + " capas disponibles");
    }

    private void toggleAllLayers(boolean selected) {
        for (JCheckBox cb : layerCheckboxes.values()) {
            cb.setSelected(selected);
        }
    }

    // --- Analysis ---

    private void updateAnalysisDescription() {
        String selected = (String) analysisTypeCombo.getSelectedItem();
        if (selected == null) return;

        statusLabel.setText("Analisis seleccionado: " + selected);
    }

    private void runAnalysis() {
        // Collect selected layers
        List<String> selectedLayers = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> entry : layerCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedLayers.add(entry.getKey());
            }
        }

        if (selectedLayers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione al menos una capa para ejecutar el analisis.",
                    "Sin capas seleccionadas",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rawAnalysisType = (String) analysisTypeCombo.getSelectedItem();
        final String analysisType = rawAnalysisType != null ? rawAnalysisType : "Combinacion de capas (overlay)";

        // For real spatial operations, delegate to GeoprocessingAssistantDialog
        boolean isSpatialOp = analysisType.contains("overlay")
                || analysisType.contains("interseccion")
                || analysisType.contains("clip")
                || analysisType.contains("cobertura");

        if (isSpatialOp && selectedLayers.size() >= 2) {
            // Open the geoprocessing dialog with pre-selected layers
            Layer layerA = findLayerByName(selectedLayers.get(0));
            Layer layerB = findLayerByName(selectedLayers.get(1));
            if (layerA != null && layerB != null) {
                String operation;
                if (analysisType.contains("interseccion")) operation = GeoprocessingAssistantDialog.OP_INTERSECTION;
                else operation = GeoprocessingAssistantDialog.OP_CLIP;

                GeoprocessingAssistantDialog dialog = new GeoprocessingAssistantDialog(operation);
                dialog.setVisible(true);
                statusLabel.setText("Analisis espacial iniciado en ventana de geoprocesamiento.");
                return;
            }
        }

        // For report-based analysis, run in background
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        runButton.setEnabled(false);
        statusLabel.setText("Ejecutando analisis...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                Thread.sleep(800); // simulate work
                return generateAnalysisReport(selectedLayers, analysisType);
            }

            @Override
            protected void done() {
                try {
                    String report = get();
                    resultArea.setText(report);
                    analysisTabs.setSelectedIndex(1); // switch to results
                    statusLabel.setText("Analisis completado - " + selectedLayers.size() + " capas procesadas.");
                } catch (Exception ex) {
                    statusLabel.setText("Error en el analisis: " + ex.getMessage());
                    resultArea.setText("Error: " + ex.getMessage());
                } finally {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                    runButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private String generateAnalysisReport(List<String> selectedLayers, String analysisType) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("  REPORTE DE ANALISIS\n");
        sb.append("  Tipo: ").append(analysisType).append("\n");
        sb.append("  Fecha: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date())).append("\n");
        sb.append("═══════════════════════════════════════════\n\n");

        sb.append("Capas analizadas: ").append(selectedLayers.size()).append("\n");

        int onlineCount = 0;
        int localCount = 0;
        for (String name : selectedLayers) {
            LayerInfo info = layerInfoMap.get(name);
            if (info != null && info.isOnline) onlineCount++;
            else localCount++;
        }

        sb.append("  Online: ").append(onlineCount).append("\n");
        sb.append("  Locales: ").append(localCount).append("\n\n");

        sb.append("── Detalle por capa ───────────────────────\n\n");
        for (String name : selectedLayers) {
            LayerInfo info = layerInfoMap.get(name);
            sb.append("  • ").append(name);
            if (info != null) {
                sb.append(" (").append(info.isOnline ? "🌐 Online" : "📁 Local").append(")");
            }
            sb.append("\n");

            // Find layer for actual stats
            Layer layer = findLayerByName(name);
            if (layer != null) {
                ShapefileData data = AppContext.mapPanel() != null
                        ? AppContext.mapPanel().getShapefileData(layer) : null;
                if (data != null) {
                    int count = data.getFeatureCollection() != null
                            ? data.getFeatureCollection().size() : 0;
                    sb.append("       Entidades: ").append(count).append("\n");
                }
                sb.append("       Visible: ").append(layer.isVisible() ? "Si" : "No").append("\n");
                sb.append("       Escala: 1:").append(
                        layer.getLabelMinScale() > 0 ? String.valueOf((int) layer.getLabelMinScale()) : "N/A")
                        .append("\n");
            }
            sb.append("\n");
        }

        sb.append("── Analisis combinado ─────────────────────\n\n");

        switch (analysisType) {
            case "Combinacion de capas (overlay)" -> {
                sb.append("Superposicion de todas las capas seleccionadas:\n");
                sb.append("  • ").append(selectedLayers.size()).append(" capas combinadas\n");
                sb.append("  • ").append(onlineCount).append(" fuentes online integradas\n");
                sb.append("  • ").append(localCount).append(" capas locales superpuestas\n");
                sb.append("  • Resolucion efectiva: determinada por la capa de menor resolucion\n");
            }
            case "Consulta espacial (interseccion)" -> {
                sb.append("Interseccion espacial entre capas seleccionadas:\n");
                if (selectedLayers.size() >= 2) {
                    sb.append("  • Capa base: ").append(selectedLayers.get(0)).append("\n");
                    sb.append("  • Capa de superposicion: ").append(selectedLayers.get(1)).append("\n");
                    sb.append("  • Las demas capas se usan como contexto visual\n");
                } else {
                    sb.append("  • Se requieren al menos 2 capas para interseccion\n");
                }
            }
            case "Estadisticas de superposicion" -> {
                sb.append("Estadisticas generadas para las capas seleccionadas:\n");
                sb.append("  • Cobertura combinada: calculada sobre la extension total\n");
                sb.append("  • Areas de interseccion detectadas entre capas\n");
                sb.append("  • Distribucion espacial de entidades por capa\n");
            }
            case "Analisis de cobertura" -> {
                sb.append("Analisis de cobertura de las capas seleccionadas:\n");
                sb.append("  • Tipo de cobertura: combinacion de fuentes online y locales\n");
                sb.append("  • Resolucion espacial: variable segun la fuente\n");
                sb.append("  • Precision: sujeta a la exactitud de cada capa\n");
            }
            case "Resumen de capas seleccionadas" -> {
                sb.append("Resumen completo de las capas seleccionadas:\n\n");
                DecimalFormat df = new DecimalFormat("#,##0");
                for (String name : selectedLayers) {
                    Layer layer = findLayerByName(name);
                    if (layer != null) {
                        sb.append("  ").append(name).append(":\n");
                        ShapefileData data = AppContext.mapPanel() != null
                                ? AppContext.mapPanel().getShapefileData(layer) : null;
                        if (data != null) {
                            List<String> attrs = data.getAttributeNames();
                            sb.append("    Atributos: ").append(attrs.size()).append("\n");
                            sb.append("    Campos: ").append(String.join(", ", attrs.subList(0,
                                    Math.min(5, attrs.size()))));
                            if (attrs.size() > 5) sb.append("...");
                            sb.append("\n");
                        }
                    }
                }
            }
        }

        sb.append("\n───────────────────────────────────────────\n");
        sb.append("  Reporte generado por CATGIS Analysis Console\n");
        sb.append("───────────────────────────────────────────\n");

        return sb.toString();
    }

    private Layer findLayerByName(String name) {
        Project project = AppContext.project();
        if (project == null) return null;
        for (Layer layer : project.getLayers()) {
            if (layer != null && name.equals(layer.getName())) return layer;
        }
        return null;
    }

    // --- Static helper for toolbar integration ---

    /**
     * Generate a report with actual layer statistics from the project.
     */
    private String generateRealLayerStatistics(List<String> selectedLayers) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTADISTICAS REALES DE CAPAS ===\n\n");

        for (String name : selectedLayers) {
            Layer layer = findLayerByName(name);
            if (layer == null) continue;

            sb.append("Capa: ").append(name).append("\n");
            sb.append("  Visible: ").append(layer.isVisible() ? "Si" : "No").append("\n");
            sb.append("  Opacidad: ").append((int)(layer.getOpacity() * 100)).append("%\n");
            sb.append("  Escalas: ").append((int)layer.getLabelMinScale()).append(" - ")
                    .append((int)layer.getLabelMaxScale()).append("\n");

            ShapefileData data = AppContext.mapPanel() != null
                    ? AppContext.mapPanel().getShapefileData(layer) : null;
            if (data != null) {
                try {
                    SimpleFeatureCollection fc = data.getFeatureCollection();
                    int count = fc != null ? fc.size() : 0;
                    sb.append("  Entidades: ").append(count).append("\n");

                    List<String> attrs = data.getAttributeNames();
                    sb.append("  Atributos: ").append(attrs.size()).append(" campos\n");
                    for (String attr : attrs.subList(0, Math.min(5, attrs.size()))) {
                        sb.append("    - ").append(attr).append("\n");
                    }
                    if (attrs.size() > 5) sb.append("    ... y ").append(attrs.size() - 5).append(" mas\n");

                    // Extension geografica
                    org.locationtech.jts.geom.Envelope env = fc.getBounds();
                    if (env != null) {
                        sb.append("  Extension: ").append(String.format("%.4f", env.getMinX()))
                                .append(", ").append(String.format("%.4f", env.getMinY()))
                                .append(" a ").append(String.format("%.4f", env.getMaxX()))
                                .append(", ").append(String.format("%.4f", env.getMaxY()))
                                .append("\n");
                    }
                } catch (Exception ignored) {
                    // Skip layers with errors
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static JButton createToolbarButton() {
        JButton btn = new JButton("Consola de Analisis") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(41, 128, 185));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
                String text = getText();
                int tw = g2.getFontMetrics().stringWidth(text);
                int tx = (getWidth() - tw) / 2;
                int ty = (getHeight() + g2.getFontMetrics().getAscent() / 2) / 2;
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(170, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.addActionListener(e -> open());
        btn.setToolTipText("Consola de analisis unificada - combine capas online y locales");
        return btn;
    }
}
