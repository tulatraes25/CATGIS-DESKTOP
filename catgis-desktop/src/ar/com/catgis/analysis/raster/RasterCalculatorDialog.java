package ar.com.catgis.analysis.raster;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.analysis.raster.RasterCalculatorEngine;

import ar.com.catgis.MapPanel;
import ar.com.catgis.AppContext;
import ar.com.catgis.Main;
import ar.com.catgis.LayersPanel;
import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.NotificationManager;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import ar.com.catgis.data.raster.LocalRasterData;
import java.util.List;
import java.util.Map;

/**
 * Raster Calculator dialog — algebraic operations on raster layers.
 * <p>
 * Expression syntax:
 * <pre>
 *   a * 2 + b                → multiply raster 'a' by 2, add raster 'b'
 *   sin(a) * cos(b)           → pixel-wise sin/cos
 *   sqrt(a)                   → square root
 *   if(a > 100, a, 0)         → condition: keep values > 100, else 0
 *   ndvi(a, b)                → (b - a) / (b + a) for NIR/Red bands
 * </pre>
 * </p>
 */
public class RasterCalculatorDialog extends JDialog {

    private JComboBox<String> rasterACombo, rasterBCombo, rasterCCombo;
    private JTextField expressionField;
    private JProgressBar progressBar;
    private JButton runButton;
    private JCheckBox normalizeCheck, nodataCheck;
    private final Map<String, LocalRasterData> rasterMap = new LinkedHashMap<>();

    public RasterCalculatorDialog() {
        setTitle("Calculadora Raster");
        setSize(700, 540);
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());
        setModal(false);

        JPanel main = new JPanel(new BorderLayout(0, 8));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: raster selectors
        main.add(buildSelectorPanel(), BorderLayout.NORTH);
        // Center: expression + preview
        main.add(buildExpressionPanel(), BorderLayout.CENTER);
        // Bottom: progress + buttons
        main.add(buildBottomPanel(), BorderLayout.SOUTH);

        add(main);

        refreshRasterList();
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new RasterCalculatorDialog().setVisible(true));
    }

    private JPanel buildSelectorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Capas raster"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.insets = new Insets(2, 4, 2, 4);

        JLabel aLabel = new JLabel("Raster A:"); aLabel.setFont(aLabel.getFont().deriveFont(Font.BOLD, 12f));
        gbc.gridy = 0; panel.add(aLabel, gbc);
        rasterACombo = new JComboBox<>(); rasterACombo.setToolTipText("Referenciado como 'a' en la expresion");
        gbc.gridy = 1; panel.add(rasterACombo, gbc);

        JLabel bLabel = new JLabel("Raster B:"); bLabel.setFont(bLabel.getFont().deriveFont(Font.BOLD, 12f));
        gbc.gridy = 2; panel.add(bLabel, gbc);
        rasterBCombo = new JComboBox<>(); rasterBCombo.setToolTipText("Referenciado como 'b' en la expresion");
        gbc.gridy = 3; panel.add(rasterBCombo, gbc);

        JLabel cLabel = new JLabel("Raster C:"); cLabel.setFont(cLabel.getFont().deriveFont(Font.BOLD, 12f));
        gbc.gridy = 4; panel.add(cLabel, gbc);
        rasterCCombo = new JComboBox<>(); rasterCCombo.setToolTipText("Referenciado como 'c' en la expresion");
        gbc.gridy = 5; panel.add(rasterCCombo, gbc);

        return panel;
    }

    private JPanel buildExpressionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Expresion"));

        expressionField = new JTextField("a * 2 + b");
        expressionField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        expressionField.setToolTipText("Ejemplos: a*2+b | sin(a)*cos(b) | if(a>100, a, 0) | ndvi(a, b)");
        panel.add(expressionField, BorderLayout.NORTH);

        // Quick reference
        JTextArea help = new JTextArea("""
                Operadores: + - * /
                Funciones: sin(), cos(), sqrt(), log(), abs(), pow(), floor(), ceil(), round()
                Condicional: if(cond, then, else)
                Especial: ndvi(nir, red), x, y (posicion normalizada)
                Variables: a = Raster A, b = Raster B, c = Raster C
                1 = Raster A (1 banda), 2 = Raster B, etc.
                """);
        help.setEditable(false);
        help.setBackground(new Color(248, 249, 250));
        help.setFont(new Font("Monospaced", Font.PLAIN, 11));
        help.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JScrollPane helpScroll = new JScrollPane(help);
        helpScroll.setPreferredSize(new Dimension(400, 120));
        panel.add(helpScroll, BorderLayout.CENTER);

        // Output options
        JPanel opts = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        normalizeCheck = new JCheckBox("Normalizar salida 0-255", true);
        nodataCheck = new JCheckBox("NoData = 0", true);
        opts.add(normalizeCheck);
        opts.add(nodataCheck);
        panel.add(opts, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        panel.add(progressBar, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        runButton = new JButton("Calcular");
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD, 13f));
        runButton.setBackground(new Color(41, 128, 185));
        runButton.setForeground(Color.WHITE);
        runButton.addActionListener(e -> runCalculation());
        btnPanel.add(runButton);

        JButton closeBtn = new JButton("Cerrar");
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);

        panel.add(btnPanel, BorderLayout.CENTER);
        return panel;
    }

    private void refreshRasterList() {
        rasterMap.clear();
        rasterACombo.removeAllItems();
        rasterBCombo.removeAllItems();
        rasterCCombo.removeAllItems();

        rasterACombo.addItem("(ninguno)");
        rasterBCombo.addItem("(ninguno)");
        rasterCCombo.addItem("(ninguno)");

        Project project = AppContext.project();
        if (project == null) return;

        for (Layer layer : project.getLayers()) {
            if (layer == null) continue;
            LocalRasterData data = AppContext.mapPanel().getRasterData(layer);
            if (data != null) {
                String name = layer.getName();
                rasterMap.put(name, data);
                rasterACombo.addItem(name);
                rasterBCombo.addItem(name);
                rasterCCombo.addItem(name);
            }
        }
    }

    private void runCalculation() {
        String aSel = (String) rasterACombo.getSelectedItem();
        String bSel = (String) rasterBCombo.getSelectedItem();
        String cSel = (String) rasterCCombo.getSelectedItem();
        String expr = expressionField.getText().trim();

        if (aSel == null || "(ninguno)".equals(aSel)) {
            JOptionPane.showMessageDialog(this, "Seleccione al menos Raster A.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (expr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese una expresion.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        runButton.setEnabled(false);

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                List<RasterCalculatorEngine.RasterSource> sources = new ArrayList<>();
                sources.add(toSource(aSel));
                if (bSel != null && !"(ninguno)".equals(bSel)) sources.add(toSource(bSel));
                if (cSel != null && !"(ninguno)".equals(cSel)) sources.add(toSource(cSel));

                BufferedImage result = RasterCalculatorEngine.evaluate(sources, expr);
                if (result == null) throw new RuntimeException("La expresion no produjo resultado.");
                return result;
            }

            @Override
            protected void done() {
                try {
                    BufferedImage result = get();
                    // Save as new raster layer
                    String name = NotificationManager.inputText(RasterCalculatorDialog.this,
                            "Resultado", "Nombre de la capa resultante:", null);
                    if (name == null || name.isBlank()) name = "calc_" + System.currentTimeMillis() % 10000;

                    LocalRasterData src = rasterMap.get(aSel);
                    if (src != null) {
                        LocalRasterData outData = new LocalRasterData(result, src.getEnvelope(),
                                1, src.isGeoreferenced(), src.getSourceCRS());
                        Layer layer = new Layer(name, "", "RASTER");
                        AppContext.project().addLayer(layer);
                        AppContext.addLayer(layer);
                        AppContext.mapPanel().addOrUpdateRasterLayer(layer, outData);
                        CatgisDesktopApp.markProjectDirty();
                        NotificationManager.toastSuccess("Capa '" + name + "' creada correctamente.");
                    }
                } catch (Exception ex) {
                    NotificationManager.error(RasterCalculatorDialog.this, "Error",
                            "Error: " + ex.getMessage());
                } finally {
                    progressBar.setVisible(false);
                    runButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private RasterCalculatorEngine.RasterSource toSource(String name) {
        LocalRasterData data = rasterMap.get(name);
        if (data != null) {
            return new RasterCalculatorEngine.RasterSource(data.getImage(), data.getEnvelope(), name);
        }
        return null;
    }
}
