package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class ExportReprojectedLayerDialog extends JDialog {

    private final JComboBox<Layer> layerCombo;
    private final JComboBox<String> formatCombo;
    private final JButton crsButton;
    private String targetCrs;

    public ExportReprojectedLayerDialog() {
        setTitle("Exportar capa reproyectada");
        setModal(false);
        setSize(560, 240);
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());
        setLayout(new BorderLayout(8, 8));

        layerCombo = new JComboBox<>(VectorLayerUtils.getVectorLayers().toArray(new Layer[0]));
        formatCombo = new JComboBox<>(ExportVectorLayerAction.getSupportedVectorFormats());
        targetCrs = CatgisDesktopApp.currentProject != null && CatgisDesktopApp.currentProject.getProjectCRS() != null
                ? CatgisDesktopApp.currentProject.getProjectCRS()
                : "EPSG:4326";

        crsButton = new JButton(CRSDefinitions.getLabelForCode(targetCrs));
        crsButton.addActionListener(e -> CRSSelectorDialog.open("CRS destino", targetCrs, code -> {
            if (code != null && !code.isBlank()) {
                targetCrs = code;
                crsButton.setText(CRSDefinitions.getLabelForCode(code));
            }
        }));

        JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        form.add(new JLabel("Capa vectorial:"));
        form.add(layerCombo);
        form.add(new JLabel("Formato de salida:"));
        form.add(formatCombo);
        form.add(new JLabel("CRS destino:"));
        form.add(crsButton);
        add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        JButton exportButton = new JButton("Exportar");
        exportButton.addActionListener(e -> exportLayer());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(exportButton);
        footer.add(closeButton);
        add(footer, BorderLayout.SOUTH);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new ExportReprojectedLayerDialog().setVisible(true));
    }

    private void exportLayer() {
        Layer layer = layerCombo.getItemAt(layerCombo.getSelectedIndex());
        if (layer == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una capa vectorial.");
            return;
        }

        ShapefileData data = VectorLayerUtils.ensureVectorData(layer);
        if (data == null) {
            JOptionPane.showMessageDialog(this, "La capa seleccionada no tiene datos vectoriales disponibles.");
            return;
        }

        String format = String.valueOf(formatCombo.getSelectedItem());
        ExportVectorLayerAction.exportLayerWithOptions(layer, data, this, "Exportar capa reproyectada", true, format, targetCrs);
    }
}
