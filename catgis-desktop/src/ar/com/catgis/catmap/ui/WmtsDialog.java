package ar.com.catgis.catmap.ui;

import ar.com.catgis.catmap.WmtsSupport;

import ar.com.catgis.OnlineTileLayer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/**
 * "Agregar servicio WMTS" dialog for CATMAP standalone. Builds a tile template
 * from a RESTful or KVP WMTS service and registers it through the online-tile
 * pipeline (T15).
 */
public final class WmtsDialog extends JDialog {

    private final JTextField serviceField = new JTextField(34);
    private final JTextField layerField = new JTextField(18);
    private final JTextField styleField = new JTextField(WmtsSupport.DEFAULT_STYLE, 10);
    private final JTextField matrixSetField =
            new JTextField(WmtsSupport.DEFAULT_MATRIX_SET, 14);
    private final JTextField extensionField = new JTextField(WmtsSupport.DEFAULT_EXTENSION, 6);
    private final JTextField nameField = new JTextField("WMTS", 18);
    private final JTextField minZoomField = new JTextField("0", 4);
    private final JTextField maxZoomField = new JTextField("18", 4);
    private final JCheckBox kvpCheck = new JCheckBox("Usar plantilla KVP GetTile (en vez de REST)");
    private final JLabel previewLabel = new JLabel(" ");
    private final JLabel errorLabel = new JLabel(" ");

    private WmtsDialog(Window owner) {
        super(owner, "Agregar servicio WMTS",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, gc, 0, "URL del servicio WMTS:", serviceField);
        addRow(form, gc, 1, "Capa (layer):", layerField);
        addRow(form, gc, 2, "Estilo:", styleField);
        addRow(form, gc, 3, "TileMatrixSet:", matrixSetField);
        addRow(form, gc, 4, "Formato (png/jpeg):", extensionField);
        addRow(form, gc, 5, "Nombre de la capa:", nameField);
        addRow(form, gc, 6, "Zoom minimo / maximo:",
                flow(minZoomField, new JLabel(" a "), maxZoomField));
        gc.gridx = 1;
        gc.gridy = 7;
        form.add(kvpCheck, gc);
        gc.gridx = 0;
        gc.gridy = 8;
        gc.gridwidth = 2;
        errorLabel.setForeground(new java.awt.Color(0xB42318));
        form.add(errorLabel, gc);
        gc.gridy = 9;
        previewLabel.setForeground(new java.awt.Color(0x4A5568));
        form.add(previewLabel, gc);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(event -> dispose());
        JButton accept = new JButton("Agregar");
        accept.addActionListener(event -> addLayer());
        bottom.add(cancel);
        bottom.add(accept);

        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        serviceField.getDocument().addDocumentListener(
                new SimpleDocumentListener(this::refreshPreview));
        layerField.getDocument().addDocumentListener(
                new SimpleDocumentListener(this::refreshPreview));
        kvpCheck.addActionListener(event -> refreshPreview());
        setPreferredSize(new Dimension(620, 380));
        pack();
        setLocationRelativeTo(owner);
        refreshPreview();
    }

    public static void open(Window owner) {
        WmtsDialog dialog = new WmtsDialog(owner);
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }

    private void refreshPreview() {
        try {
            WmtsSupport.Request request = currentRequest();
            if (!WmtsSupport.isTemplate(serviceField.getText())
                    && (request.layer() == null || request.layer().isBlank())) {
                previewLabel.setText("Ingresa la capa WMTS para ver la plantilla.");
                errorLabel.setText(" ");
                return;
            }
            String template = WmtsSupport.buildTemplate(request, !kvpCheck.isSelected());
            String problem = WmtsSupport.validateTemplate(template);
            errorLabel.setText(problem == null ? " " : problem);
            previewLabel.setText(problem == null
                    ? "Plantilla: " + abbreviate(template) : " ");
        } catch (RuntimeException invalid) {
            errorLabel.setText(invalid.getMessage());
            previewLabel.setText(" ");
        }
    }

    private void addLayer() {
        try {
            WmtsSupport.Request request = currentRequest();
            String template = WmtsSupport.buildTemplate(request, !kvpCheck.isSelected());
            String problem = WmtsSupport.validateTemplate(template);
            if (problem != null) {
                errorLabel.setText(problem);
                return;
            }
            OnlineTileLayer layer = new OnlineTileLayer(
                    nameField.getText().isBlank() ? request.layer() : nameField.getText().trim());
            layer.setSourceId("wmts-" + Integer.toHexString(template.hashCode()));
            layer.setProviderName("WMTS");
            layer.setUrlTemplate(template);
            layer.setMinZoom(request.minZoom());
            layer.setMaxZoom(request.maxZoom());
            layer.setAttribution("WMTS: " + request.layer());
            if (ar.com.catgis.AppContext.project() != null) {
                ar.com.catgis.AppContext.project().addLayer(layer);
            }
            if (ar.com.catgis.CatgisDesktopApp.layersPanel != null) {
                ar.com.catgis.AppContext.addLayer(layer);
                ar.com.catgis.AppContext.selectLayer(layer);
            }
            if (ar.com.catgis.AppContext.mapPanel() != null) {
                ar.com.catgis.AppContext.mapPanel().addOrUpdateOnlineTileLayer(layer);
                ar.com.catgis.AppContext.mapPanel().zoomToLayer(layer);
                ar.com.catgis.AppContext.mapPanel().refreshMap();
            }
            ar.com.catgis.CatgisDesktopApp.markProjectDirty();
            ar.com.catgis.NotificationManager.toastSuccess(
                    "WMTS agregado: " + layer.getName());
            dispose();
        } catch (RuntimeException invalid) {
            errorLabel.setText(invalid.getMessage());
        }
    }

    private WmtsSupport.Request currentRequest() {
        return new WmtsSupport.Request(
                serviceField.getText(),
                layerField.getText(),
                styleField.getText(),
                matrixSetField.getText(),
                extensionField.getText(),
                parseZoom(minZoomField.getText(), 0),
                WmtsSupport.clampMaxZoom(
                        parseZoom(minZoomField.getText(), 0),
                        parseZoom(maxZoomField.getText(), 18)));
    }

    private static int parseZoom(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (RuntimeException invalid) {
            return fallback;
        }
    }

    private static void addRow(JPanel form, GridBagConstraints gc, int row,
                               String label, java.awt.Component field) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 1;
        gc.weightx = 0;
        form.add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(field, gc);
    }

    private static JPanel flow(java.awt.Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panel.setOpaque(false);
        for (java.awt.Component component : components) {
            panel.add(component);
        }
        return panel;
    }

    private static String abbreviate(String value) {
        return value.length() <= 90 ? value : value.substring(0, 87) + "...";
    }

    /** Minimal document listener to keep the template preview updated. */
    private static final class SimpleDocumentListener
            implements javax.swing.event.DocumentListener {
        private final Runnable onChange;

        private SimpleDocumentListener(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent event) {
            onChange.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent event) {
            onChange.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent event) {
            onChange.run();
        }
    }
}
