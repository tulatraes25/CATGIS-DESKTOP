package ar.com.catgis.ui.dialogs;

import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.AppContext;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.online.WfsTransactionService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import ar.com.catgis.NotificationManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Dialog for editing WFS-T (Transactional) layers.
 * Allows insert/update/delete of features on WFS servers.
 */
public class WfsTransactionDialog extends JDialog {

    private final JComboBox<Layer> layerCombo;
    private final JTextField filterField;
    private final JTextField valueField;
    private final JTextArea logArea;

    public WfsTransactionDialog() {
        setTitle("Edicion WFS-T");
        setSize(600, 480);
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());

        JPanel main = new JPanel(new BorderLayout(0, 8));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Layer selector
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.insets = new Insets(2, 2, 2, 2);

        JLabel layerLabel = new JLabel("Capa WFS:");
        layerLabel.setFont(layerLabel.getFont().deriveFont(Font.BOLD, 12f));
        gbc.gridy = 0; top.add(layerLabel, gbc);
        layerCombo = new JComboBox<>();
        layerCombo.setToolTipText("Capas WFS que soportan transacciones");
        gbc.gridy = 1; top.add(layerCombo, gbc);

        JLabel filterLabel = new JLabel("Filtro ECQL (ej: gml_id = 'feature.1'):");
        filterLabel.setFont(filterLabel.getFont().deriveFont(Font.PLAIN, 11f));
        gbc.gridy = 2; top.add(filterLabel, gbc);
        filterField = new JTextField();
        filterField.setToolTipText("ECQL filter e.g. gml_id='feature.123'");
        gbc.gridy = 3; top.add(filterField, gbc);

        JLabel valueLabel = new JLabel("Nuevo valor (opcional):");
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, 11f));
        gbc.gridy = 4; top.add(valueLabel, gbc);
        valueField = new JTextField();
        gbc.gridy = 5; top.add(valueField, gbc);

        main.add(top, BorderLayout.NORTH);

        // Operations
        JPanel ops = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton checkBtn = new JButton("Verificar soporte WFS-T");
        checkBtn.addActionListener(e -> checkWfsSupport());
        ops.add(checkBtn);

        JButton deleteBtn = new JButton("Eliminar features (filtro)");
        deleteBtn.addActionListener(e -> deleteFeatures());
        ops.add(deleteBtn);

        main.add(ops, BorderLayout.CENTER);

        // Log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(248, 249, 250));
        logArea.append("=== WFS-T Transaction Console ===\n");
        logArea.append("Seleccione una capa WFS y ejecute operaciones.\n");
        main.add(new JScrollPane(logArea), BorderLayout.SOUTH);

        add(main);

        refreshLayers();
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new WfsTransactionDialog().setVisible(true));
    }

    private void refreshLayers() {
        layerCombo.removeAllItems();
        if (AppContext.project() == null) return;
        for (Layer layer : AppContext.project().getLayers()) {
            if (layer != null && layer.getPath() != null && layer.getPath().toLowerCase().contains("wfs")) {
                layerCombo.addItem(layer);
            }
        }
    }

    private void checkWfsSupport() {
        Layer layer = (Layer) layerCombo.getSelectedItem();
        if (layer == null) { log("Seleccione una capa WFS."); return; }
        boolean supported = WfsTransactionService.supportsTransactions(layer);
        log(layer.getName() + ": WFS-T " + (supported ? "SOPORTADO" : "NO SOPORTADO (solo lectura)"));
    }

    private void deleteFeatures() {
        Layer layer = (Layer) layerCombo.getSelectedItem();
        if (layer == null) { log("Seleccione una capa."); return; }
        String filter = filterField.getText().trim();
        if (filter.isEmpty()) {
            NotificationManager.warn(this, "Filtro requerido", "Ingrese un filtro ECQL.");
            return;
        }
        log("Eliminando features de " + layer.getName() + " con filtro: " + filter);
        boolean ok = WfsTransactionService.deleteFeatures(layer, filter);
        log(ok ? "OK: Features eliminados." : "ERROR: Ver filtro y conexion.");
    }

    private void log(String msg) {
        logArea.append("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + msg + "\n");
    }
}
