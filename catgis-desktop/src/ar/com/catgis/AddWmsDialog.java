package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.core.model.Layer;

import org.locationtech.jts.geom.Envelope;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddWmsDialog extends JDialog {

    private final JTextField urlField;
    private final JButton connectButton;
    private final JButton addButton;
    private final DefaultListModel<WmsLayerInfo> layerModel;
    private final JList<WmsLayerInfo> layerList;
    private final JComboBox<String> formatCombo;
    private final JComboBox<String> crsCombo;
    private final JComboBox<Object> styleCombo;
    private final JLabel serviceTitleLabel;
    private final JLabel statusLabel;

    private WmsCapabilities capabilities;

    private AddWmsDialog(Window owner) {
        super(owner, "Agregar WMS", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel urlLabel = new JLabel("URL WMS:");
        urlField = new JTextField("https://ows.terrestris.de/osm/service?");
        connectButton = new JButton("Conectar");
        connectButton.addActionListener(e -> fetchCapabilities());

        serviceTitleLabel = new JLabel("Servicio: -");
        serviceTitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        layerModel = new DefaultListModel<>();
        layerList = new JList<>(layerModel);
        layerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        layerList.setVisibleRowCount(10);
        layerList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String label = value instanceof WmsLayerInfo
                        ? ((WmsLayerInfo) value).getDisplayLabel()
                        : String.valueOf(value);
                return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            }
        });
        layerList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshSelectionState();
            }
        });

        formatCombo = new JComboBox<>();
        crsCombo = new JComboBox<>();
        styleCombo = new JComboBox<>();

        statusLabel = new JLabel("Pegá la URL base del WMS y hacé clic en Conectar.");

        JPanel urlPanel = new JPanel(new BorderLayout(8, 0));
        urlPanel.add(urlField, BorderLayout.CENTER);
        urlPanel.add(connectButton, BorderLayout.EAST);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        content.add(urlLabel, gc);

        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1;
        content.add(urlPanel, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 2;
        gc.weightx = 1;
        content.add(serviceTitleLabel, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;
        content.add(new JScrollPane(layerList), gc);

        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 3;
        gc.gridwidth = 1;
        content.add(new JLabel("Formato:"), gc);
        gc.gridx = 1;
        content.add(formatCombo, gc);

        gc.gridx = 0;
        gc.gridy = 4;
        content.add(new JLabel("CRS:"), gc);
        gc.gridx = 1;
        content.add(crsCombo, gc);

        gc.gridx = 0;
        gc.gridy = 5;
        content.add(new JLabel("Estilo:"), gc);
        gc.gridx = 1;
        content.add(styleCombo, gc);

        gc.gridx = 0;
        gc.gridy = 6;
        gc.gridwidth = 2;
        content.add(statusLabel, gc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        addButton = new JButton("Agregar");
        addButton.setEnabled(false);
        addButton.addActionListener(e -> onAddLayer());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        buttons.add(addButton);
        buttons.add(cancelButton);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, connectButton, this::dispose);
        setSize(760, 560);
        setLocationRelativeTo(owner);
    }

    public static void open(Window owner) {
        AddWmsDialog dialog = new AddWmsDialog(owner);
        dialog.setVisible(true);
    }

    private void fetchCapabilities() {
        String url = urlField.getText() != null ? urlField.getText().trim() : "";
        if (url.isBlank()) {
            JOptionPane.showMessageDialog(this, "Primero ingresá una URL WMS.", "Agregar WMS", JOptionPane.WARNING_MESSAGE);
            return;
        }

        connectButton.setEnabled(false);
        addButton.setEnabled(false);
        statusLabel.setText("Consultando GetCapabilities...");
        layerModel.clear();
        capabilities = null;

        new SwingWorker<WmsCapabilities, Void>() {
            @Override
            protected WmsCapabilities doInBackground() throws Exception {
                return WmsCapabilitiesService.fetchCapabilities(url);
            }

            @Override
            protected void done() {
                connectButton.setEnabled(true);
                try {
                    capabilities = get();
                    serviceTitleLabel.setText("Servicio: " + (capabilities.getServiceTitle().isBlank() ? "(sin titulo)" : capabilities.getServiceTitle()));
                    layerModel.clear();
                    for (WmsLayerInfo layer : capabilities.getLayers()) {
                        layerModel.addElement(layer);
                    }
                    refreshFormats();
                    refreshSelectionState();
                    statusLabel.setText("WMS conectado. Seleccioná una o más capas y agregalas al proyecto.");
                } catch (Exception ex) {
                    capabilities = null;
                    serviceTitleLabel.setText("Servicio: -");
                    formatCombo.setModel(new DefaultComboBoxModel<>());
                    crsCombo.setModel(new DefaultComboBoxModel<>());
                    styleCombo.setModel(new DefaultComboBoxModel<>());
                    statusLabel.setText("No se pudo leer el WMS.");
                    JOptionPane.showMessageDialog(
                            AddWmsDialog.this,
                            "No se pudo consultar el servicio WMS:\n" + ex.getMessage(),
                            "Agregar WMS",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void refreshFormats() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        if (capabilities != null) {
            for (String format : capabilities.getFormats()) {
                model.addElement(format);
            }
            String preferred = choosePreferredFormat(capabilities.getFormats());
            if (preferred != null) {
                model.setSelectedItem(preferred);
            }
        }
        formatCombo.setModel(model);
    }

    private void refreshSelectionState() {
        List<WmsLayerInfo> selected = layerList.getSelectedValuesList();
        addButton.setEnabled(capabilities != null && !selected.isEmpty());
        getRootPane().setDefaultButton(addButton.isEnabled() ? addButton : connectButton);
        refreshCrsOptions(selected);
        refreshStyleOptions(selected);
    }

    private void refreshCrsOptions(List<WmsLayerInfo> selected) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (String crs : chooseCommonCrs(selected)) {
            model.addElement(crs);
        }
        crsCombo.setModel(model);
        String preferred = choosePreferredCrs(selected);
        if (preferred != null) {
            crsCombo.setSelectedItem(preferred);
        }
    }

    private void refreshStyleOptions(List<WmsLayerInfo> selected) {
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
        model.addElement("(predeterminado)");
        if (selected.size() == 1) {
            for (WmsStyleInfo style : selected.get(0).getStyles()) {
                model.addElement(style);
            }
        }
        styleCombo.setModel(model);
        styleCombo.setSelectedIndex(0);
    }

    private List<String> chooseCommonCrs(List<WmsLayerInfo> selected) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (selected == null || selected.isEmpty()) {
            return new ArrayList<>(result);
        }

        result.addAll(selected.get(0).getCrsCodes());
        for (int i = 1; i < selected.size(); i++) {
            result.retainAll(selected.get(i).getCrsCodes());
        }

        if (result.isEmpty()) {
            result.addAll(selected.get(0).getCrsCodes());
        }

        return new ArrayList<>(result);
    }

    private String choosePreferredCrs(List<WmsLayerInfo> selected) {
        List<String> options = chooseCommonCrs(selected);
        if (options.isEmpty()) {
            return null;
        }
        String projectCrs = AppContext.project() != null ? AppContext.project().getProjectCRS() : "";
        if (projectCrs != null && !projectCrs.isBlank()) {
            for (String option : options) {
                if (projectCrs.equalsIgnoreCase(option)) {
                    return option;
                }
            }
        }
        for (String preferred : new String[]{"EPSG:3857", "EPSG:4326", "CRS:84"}) {
            for (String option : options) {
                if (preferred.equalsIgnoreCase(option)) {
                    return option;
                }
            }
        }
        return options.get(0);
    }

    private String choosePreferredFormat(List<String> formats) {
        if (formats == null || formats.isEmpty()) {
            return null;
        }
        for (String preferred : new String[]{"image/png", "image/jpeg", "image/jpg"}) {
            for (String format : formats) {
                if (preferred.equalsIgnoreCase(format)) {
                    return format;
                }
            }
        }
        return formats.get(0);
    }

    private void onAddLayer() {
        List<WmsLayerInfo> selected = layerList.getSelectedValuesList();
        if (selected.isEmpty()) {
            return;
        }

        String requestCrs = (String) crsCombo.getSelectedItem();
        String format = (String) formatCombo.getSelectedItem();
        Object styleSelection = styleCombo.getSelectedItem();
        String styleName = styleSelection instanceof WmsStyleInfo ? ((WmsStyleInfo) styleSelection).getName() : "";

        OnlineWmsLayer layer = new OnlineWmsLayer(buildDisplayName(selected));
        String normalizedServiceUrl = WmsCapabilitiesService.normalizeServiceUrl(urlField.getText());
        layer.setProviderName(serviceHost(normalizedServiceUrl));
        layer.setServiceUrl(normalizedServiceUrl);
        layer.setVersion(capabilities != null ? capabilities.getVersion() : "1.3.0");
        layer.setLayerNames(joinLayerNames(selected));
        layer.setStyleNames(buildStyleNames(selected, styleName));
        layer.setImageFormat(format != null ? format : "image/png");
        layer.setRequestCrs(requestCrs != null ? requestCrs : "");
        layer.setSourceCRS(requestCrs != null ? requestCrs : "");
        layer.setTransparent(true);
        layer.setSourceName(capabilities != null ? capabilities.getServiceTitle() : "WMS");
        layer.setFeatureCount(selected.size());
        layer.setAttribution(capabilities != null ? capabilities.getServiceTitle() : "WMS");
        layer.setTermsUrl(normalizedServiceUrl);
        setLayerExtent(layer, selected, requestCrs);

        if (AppContext.project() == null) {
            AppContext.setCurrentProject(new Project("Proyecto actual"));
        }
        AppContext.project().addLayer(layer);
        CatgisDesktopApp.layersPanel.addLayer(layer);
        CatgisDesktopApp.layersPanel.selectLayer(layer);
        CatgisDesktopApp.mapPanel.addOrUpdateOnlineWmsLayer(layer);
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Capa WMS agregada: " + layer.getName());
        }
        dispose();
    }

    private void setLayerExtent(OnlineWmsLayer layer, List<WmsLayerInfo> selected, String requestCrs) {
        if (layer == null || selected == null || selected.isEmpty()) {
            return;
        }

        Envelope combined = null;
        String crsUsed = requestCrs != null ? requestCrs : "";
        if (requestCrs != null && !requestCrs.isBlank()) {
            for (WmsLayerInfo info : selected) {
                Envelope env = info.getBoundingBoxes().get(requestCrs.toUpperCase(Locale.ROOT));
                if (env != null && !env.isNull()) {
                    combined = combined == null ? new Envelope(env) : union(combined, env);
                }
            }
        }

        if (combined == null) {
            for (WmsLayerInfo info : selected) {
                Envelope env = info.getGeographicBounds();
                if (env != null && !env.isNull()) {
                    combined = combined == null ? new Envelope(env) : union(combined, env);
                }
            }
            crsUsed = "EPSG:4326";
        }

        if (combined != null && !combined.isNull()) {
            layer.setExtent(combined.getMinX(), combined.getMinY(), combined.getMaxX(), combined.getMaxY(), crsUsed);
        }
    }

    private Envelope union(Envelope a, Envelope b) {
        Envelope out = new Envelope(a);
        out.expandToInclude(b);
        return out;
    }

    private String buildDisplayName(List<WmsLayerInfo> selected) {
        if (selected.size() == 1) {
            WmsLayerInfo info = selected.get(0);
            return (info.getTitle() != null && !info.getTitle().isBlank()) ? info.getTitle() : info.getName();
        }
        return (capabilities != null && !capabilities.getServiceTitle().isBlank() ? capabilities.getServiceTitle() : "WMS")
                + " (" + selected.size() + " capas)";
    }

    private String joinLayerNames(List<WmsLayerInfo> selected) {
        List<String> names = new ArrayList<>();
        for (WmsLayerInfo info : selected) {
            if (info.getName() != null && !info.getName().isBlank()) {
                names.add(info.getName().trim());
            }
        }
        return String.join(",", names);
    }

    private String buildStyleNames(List<WmsLayerInfo> selected, String styleName) {
        List<String> styles = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            styles.add(styleName != null ? styleName : "");
        }
        return String.join(",", styles);
    }

    private String serviceHost(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getHost() != null ? uri.getHost() : "WMS";
        } catch (Exception ignored) {
            return "WMS";
        }
    }
}
