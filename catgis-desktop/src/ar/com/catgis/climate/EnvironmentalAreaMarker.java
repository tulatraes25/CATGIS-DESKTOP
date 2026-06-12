package ar.com.catgis.climate;

import ar.com.catgis.CatgisLogger;
import ar.com.catgis.core.model.Layer;

import ar.com.catgis.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for marking polygon layers as environmental influence areas:
 * <ul>
 *   <li>AID - Área de Influencia Directa</li>
 *   <li>AII - Área de Influencia Indirecta</li>
 *   <li>AMBIENTAL - Otra área ambiental</li>
 * </ul>
 * <p>
 * Metadata is stored in the layer's user data map.
 */
public final class EnvironmentalAreaMarker {

    public enum AreaType {
        AID("Área de Influencia Directa"),
        AII("Área de Influencia Indirecta"),
        AMBIENTAL("Área Ambiental (general)");

        private final String label;
        AreaType(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    private static final String KEY_AREA_TYPE = "environmentalAreaType";
    private static final String KEY_AREA_TYPE_LABEL = "environmentalAreaTypeLabel";

    private EnvironmentalAreaMarker() {}

    /**
     * Mark a layer with an environmental area type.
     */
    public static void markLayer(Layer layer, AreaType type) {
        if (layer == null || type == null) return;
        layer.putUserData(KEY_AREA_TYPE, type.name());
        layer.putUserData(KEY_AREA_TYPE_LABEL, type.getLabel());
        layer.putUserData("environmentalAreaDescription", "");
        CatgisDesktopApp.markProjectDirty();
    }

    /**
     * Clear the environmental area marking from a layer.
     */
    public static void clearMark(Layer layer) {
        if (layer == null) return;
        layer.getUserData().remove(KEY_AREA_TYPE);
        layer.getUserData().remove(KEY_AREA_TYPE_LABEL);
        layer.getUserData().remove("environmentalAreaDescription");
        CatgisDesktopApp.markProjectDirty();
    }

    /**
     * Get the area type of a layer, or null if not marked.
     */
    public static AreaType getAreaType(Layer layer) {
        if (layer == null) return null;
        Object val = layer.getUserData(KEY_AREA_TYPE);
        if (val instanceof String) {
            try {
                return AreaType.valueOf((String) val);
            } catch (Exception ignored) { CatgisLogger.warn("EnvironmentalAreaMarker: operation failed", ignored); }
        }
        return null;
    }

    /**
     * Check if a layer is marked as any environmental area.
     */
    public static boolean isMarked(Layer layer) {
        return getAreaType(layer) != null;
    }

    /**
     * Get the label for a marked layer, or empty string.
     */
    public static String getAreaLabel(Layer layer) {
        if (layer == null) return "";
        Object val = layer.getUserData(KEY_AREA_TYPE_LABEL);
        return val instanceof String ? (String) val : "";
    }

    /**
     * Return all polygon layers that are marked as environmental areas,
     * along with their area type.
     */
    public static List<MarkedArea> getMarkedAreaLayers() {
        List<MarkedArea> result = new ArrayList<>();
        if (CatgisDesktopApp.currentProject == null) return result;

        for (Layer layer : CatgisDesktopApp.currentProject.getLayers()) {
            AreaType type = getAreaType(layer);
            if (type != null) {
                result.add(new MarkedArea(layer, type));
            }
        }
        return result;
    }

    /**
     * Get all polygon layers (unmarked) that could be marked.
     */
    public static List<Layer> getPolygonLayers() {
        List<Layer> polygonLayers = new ArrayList<>();
        if (CatgisDesktopApp.currentProject == null) return polygonLayers;

        for (Layer layer : CatgisDesktopApp.currentProject.getLayers()) {
            if (isPolygonLayer(layer)) {
                polygonLayers.add(layer);
            }
        }
        return polygonLayers;
    }

    private static boolean isPolygonLayer(Layer layer) {
        if (layer instanceof RasterLayer) return false;
        // Check if the shapefile data has polygons
        if (CatgisDesktopApp.mapPanel != null) {
            var data = CatgisDesktopApp.mapPanel.getShapefileData(layer);
            if (data != null && data.getFeatures() != null && !data.getFeatures().isEmpty()) {
                Object geom = data.getFeatures().get(0).getDefaultGeometry();
                if (geom != null) {
                    String geomType = geom.getClass().getSimpleName();
                    if (geomType.contains("Polygon") || geomType.contains("MultiPolygon")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Show the area marking dialog.
     */
    public static void showMarkDialog(Window owner) {
        List<Layer> polygonLayers = getPolygonLayers();
        List<MarkedArea> alreadyMarked = getMarkedAreaLayers();

        if (polygonLayers.isEmpty() && alreadyMarked.isEmpty()) {
            JOptionPane.showMessageDialog(owner,
                    "No hay capas poligonales en el proyecto para marcar como área de influencia.\n\n"
                            + "Cargá o creá una capa de polígonos primero.",
                    "Marcar área de influencia",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(owner) instanceof java.awt.Frame
                ? (java.awt.Frame) SwingUtilities.getWindowAncestor(owner) : null,
                "Marcar áreas de influencia (AID/AII)", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Already marked areas
        if (!alreadyMarked.isEmpty()) {
            gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
            content.add(new JLabel("Áreas ya marcadas:"), gc);
            gc.gridwidth = 1;
            int row = 1;
            for (MarkedArea ma : alreadyMarked) {
                gc.gridx = 0; gc.gridy = row;
                content.add(new JLabel("  " + ma.layer.getName()), gc);
                gc.gridx = 1;
                JLabel typeLabel = new JLabel(ma.type.getLabel());
                typeLabel.setForeground(new Color(0, 100, 0));
                content.add(typeLabel, gc);
                row++;
            }
            gc.gridy = row++;
            gc.gridx = 0; gc.gridwidth = 2;
            content.add(new JSeparator(), gc);
            gc.gridwidth = 1;
        }

        // Polygon layers that can be marked
        if (!polygonLayers.isEmpty()) {
            gc.gridx = 0; gc.gridy = polygonLayers.isEmpty() ? 0 : 10; gc.gridwidth = 2;
            content.add(new JLabel("Capas poligonales disponibles:"), gc);
            gc.gridwidth = 1;

            // For each polygon layer, add a row with buttons
            int row2 = (polygonLayers.isEmpty() ? 1 : 11);
            for (Layer layer : polygonLayers) {
                gc.gridx = 0; gc.gridy = row2; gc.weightx = 1;
                content.add(new JLabel(layer.getName()), gc);
                gc.gridx = 1; gc.weightx = 0;
                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                JButton aidBtn = new JButton("AID");
                JButton aiiBtn = new JButton("AII");
                JButton ambBtn = new JButton("Amb.");
                aidBtn.setToolTipText("Marcar como Área de Influencia Directa");
                aiiBtn.setToolTipText("Marcar como Área de Influencia Indirecta");
                ambBtn.setToolTipText("Marcar como Área Ambiental general");

                Layer currentLayer = layer;
                aidBtn.addActionListener(e -> { markLayer(currentLayer, AreaType.AID); dialog.dispose(); });
                aiiBtn.addActionListener(e -> { markLayer(currentLayer, AreaType.AII); dialog.dispose(); });
                ambBtn.addActionListener(e -> { markLayer(currentLayer, AreaType.AMBIENTAL); dialog.dispose(); });

                btnPanel.add(aidBtn);
                btnPanel.add(aiiBtn);
                btnPanel.add(ambBtn);
                content.add(btnPanel, gc);
                row2++;
            }
        }

        JButton closeBtn = new JButton("Cerrar");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(closeBtn);

        dialog.add(new JScrollPane(content), BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setSize(Math.max(500, dialog.getWidth()), Math.min(500, dialog.getHeight()));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    /**
     * A layer marked as an environmental area.
     */
    public record MarkedArea(Layer layer, AreaType type) {
        public boolean isAID() { return type == AreaType.AID; }
        public boolean isAII() { return type == AreaType.AII; }
        public boolean isAmbiental() { return type == AreaType.AMBIENTAL; }
    }

    /**
     * Show a simple dialog to mark a single layer.
     */
    public static void markSingleLayer(Window owner, Layer layer) {
        if (layer == null) return;
        String[] options = {
                AreaType.AID.getLabel(),
                AreaType.AII.getLabel(),
                AreaType.AMBIENTAL.getLabel(),
                "Quitar marca"
        };
        int choice = JOptionPane.showOptionDialog(owner,
                "Seleccioná el tipo de área para: " + layer.getName(),
                "Marcar área de influencia",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) markLayer(layer, AreaType.AID);
        else if (choice == 1) markLayer(layer, AreaType.AII);
        else if (choice == 2) markLayer(layer, AreaType.AMBIENTAL);
        else if (choice == 3) clearMark(layer);
    }
}
