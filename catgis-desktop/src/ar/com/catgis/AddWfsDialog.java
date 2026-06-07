package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

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
import java.util.ArrayList;
import java.util.List;

public class AddWfsDialog extends JDialog {

    private final JTextField urlField;
    private final JButton connectButton;
    private final JButton addButton;
    private final DefaultListModel<WfsFeatureTypeInfo> featureTypeModel;
    private final JList<WfsFeatureTypeInfo> featureTypeList;
    private final JComboBox<String> crsCombo;
    private final JLabel serviceTitleLabel;
    private final JLabel statusLabel;

    private WfsCapabilities capabilities;

    private AddWfsDialog(Window owner) {
        super(owner, "Agregar WFS", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel urlLabel = new JLabel("URL WFS:");
        urlField = new JTextField("https://demo.mapserver.org/cgi-bin/wfs?");
        connectButton = new JButton("Conectar");
        connectButton.addActionListener(e -> fetchCapabilities());

        serviceTitleLabel = new JLabel("Servicio: -");
        serviceTitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        featureTypeModel = new DefaultListModel<>();
        featureTypeList = new JList<>(featureTypeModel);
        featureTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        featureTypeList.setVisibleRowCount(10);
        featureTypeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String label = value instanceof WfsFeatureTypeInfo
                        ? ((WfsFeatureTypeInfo) value).getDisplayLabel()
                        : String.valueOf(value);
                return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            }
        });
        featureTypeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshSelectionState();
            }
        });

        crsCombo = new JComboBox<>();
        statusLabel = new JLabel("Pegá la URL base del WFS y hacé clic en Conectar.");

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
        content.add(serviceTitleLabel, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;
        content.add(new JScrollPane(featureTypeList), gc);

        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 3;
        gc.gridwidth = 1;
        content.add(new JLabel("CRS:"), gc);

        gc.gridx = 1;
        content.add(crsCombo, gc);

        gc.gridx = 0;
        gc.gridy = 4;
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
        setSize(760, 540);
        setLocationRelativeTo(owner);
    }

    public static void open(Window owner) {
        AddWfsDialog dialog = new AddWfsDialog(owner);
        dialog.setVisible(true);
    }

    private void fetchCapabilities() {
        String url = urlField.getText() != null ? urlField.getText().trim() : "";
        if (url.isBlank()) {
            JOptionPane.showMessageDialog(this, "Primero ingresá una URL WFS.", "Agregar WFS", JOptionPane.WARNING_MESSAGE);
            return;
        }

        connectButton.setEnabled(false);
        addButton.setEnabled(false);
        statusLabel.setText("Consultando GetCapabilities...");
        featureTypeModel.clear();
        capabilities = null;

        new SwingWorker<WfsCapabilities, Void>() {
            @Override
            protected WfsCapabilities doInBackground() throws Exception {
                return WfsCapabilitiesService.fetchCapabilities(url);
            }

            @Override
            protected void done() {
                connectButton.setEnabled(true);
                try {
                    capabilities = get();
                    serviceTitleLabel.setText("Servicio: " + (capabilities.getServiceTitle().isBlank() ? "(sin titulo)" : capabilities.getServiceTitle()));
                    featureTypeModel.clear();
                    for (WfsFeatureTypeInfo info : capabilities.getFeatureTypes()) {
                        featureTypeModel.addElement(info);
                    }
                    refreshSelectionState();
                    statusLabel.setText("WFS conectado. Seleccioná una capa y agregala al proyecto en modo lectura.");
                } catch (Exception ex) {
                    capabilities = null;
                    serviceTitleLabel.setText("Servicio: -");
                    crsCombo.setModel(new DefaultComboBoxModel<>());
                    statusLabel.setText("No se pudo leer el WFS.");
                    JOptionPane.showMessageDialog(
                            AddWfsDialog.this,
                            "No se pudo consultar el servicio WFS:\n" + ex.getMessage(),
                            "Agregar WFS",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void refreshSelectionState() {
        WfsFeatureTypeInfo selected = featureTypeList.getSelectedValue();
        addButton.setEnabled(capabilities != null && selected != null);
        getRootPane().setDefaultButton(addButton.isEnabled() ? addButton : connectButton);
        refreshCrsOptions(selected);
    }

    private void refreshCrsOptions(WfsFeatureTypeInfo selected) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        if (selected != null) {
            List<String> crsCodes = selected.getCrsCodes();
            if (crsCodes.isEmpty() && selected.getDefaultCrs() != null && !selected.getDefaultCrs().isBlank()) {
                crsCodes = new ArrayList<>(List.of(selected.getDefaultCrs()));
            }
            for (String crs : crsCodes) {
                model.addElement(crs);
            }
            String preferred = choosePreferredCrs(selected);
            if (preferred != null) {
                model.setSelectedItem(preferred);
            }
        }
        crsCombo.setModel(model);
    }

    private String choosePreferredCrs(WfsFeatureTypeInfo selected) {
        if (selected == null) {
            return null;
        }
        List<String> options = selected.getCrsCodes();
        if (options.isEmpty()) {
            return selected.getDefaultCrs();
        }
        String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        if (projectCrs != null && !projectCrs.isBlank()) {
            for (String option : options) {
                if (projectCrs.equalsIgnoreCase(option)) {
                    return option;
                }
            }
        }
        if (selected.getDefaultCrs() != null && !selected.getDefaultCrs().isBlank()) {
            for (String option : options) {
                if (selected.getDefaultCrs().equalsIgnoreCase(option)) {
                    return option;
                }
            }
            return selected.getDefaultCrs();
        }
        return options.get(0);
    }

    private void onAddLayer() {
        WfsFeatureTypeInfo selected = featureTypeList.getSelectedValue();
        if (selected == null) {
            return;
        }

        String requestCrs = (String) crsCombo.getSelectedItem();
        String normalizedServiceUrl = WfsCapabilitiesService.normalizeServiceUrl(urlField.getText());

        connectButton.setEnabled(false);
        addButton.setEnabled(false);
        statusLabel.setText("Cargando entidades WFS...");

        new SwingWorker<LoadedWfsLayer, Void>() {
            @Override
            protected LoadedWfsLayer doInBackground() throws Exception {
                OnlineWfsLayer layer = new OnlineWfsLayer(buildDisplayName(selected));
                layer.setProviderName(WfsCapabilitiesService.resolveProviderName(normalizedServiceUrl));
                layer.setServiceUrl(normalizedServiceUrl);
                layer.setTypeName(selected.getName());
                layer.setTypeTitle(selected.getTitle());
                layer.setRequestCrs(requestCrs != null ? requestCrs : selected.getDefaultCrs());
                layer.setSourceCRS(layer.getRequestCrs());
                layer.setVersion(capabilities != null ? capabilities.getVersion() : "2.0.0");
                layer.setSourceName(capabilities != null ? capabilities.getServiceTitle() : layer.getName());
                layer.setReadOnly(true);

                ShapefileData data = WfsFeatureLoader.loadLayerData(layer);
                layer.setFeatureCount(data != null ? data.getFeatureCount() : 0);
                return new LoadedWfsLayer(layer, data);
            }

            @Override
            protected void done() {
                try {
                    LoadedWfsLayer loaded = get();
                    if (loaded == null || loaded.layer == null || loaded.data == null) {
                        throw new IllegalStateException("No se pudo cargar la capa WFS.");
                    }

                    if (CatgisDesktopApp.currentProject == null) {
                        CatgisDesktopApp.currentProject = new Project("Proyecto actual");
                    }

                    CatgisDesktopApp.currentProject.addLayer(loaded.layer);
                    CatgisDesktopApp.layersPanel.addLayer(loaded.layer);
                    CatgisDesktopApp.layersPanel.selectLayer(loaded.layer);
                    CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(loaded.layer, loaded.data);
                    CatgisDesktopApp.markProjectDirty();
                    if (CatgisDesktopApp.statusBar != null) {
                        CatgisDesktopApp.statusBar.setMessage("Capa WFS agregada en modo lectura: " + loaded.layer.getName());
                    }
                    dispose();
                } catch (Exception ex) {
                    connectButton.setEnabled(true);
                    addButton.setEnabled(true);
                    statusLabel.setText("No se pudo cargar la capa WFS.");
                    JOptionPane.showMessageDialog(
                            AddWfsDialog.this,
                            "No se pudo cargar la capa WFS:\n" + ex.getMessage(),
                            "Agregar WFS",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private String buildDisplayName(WfsFeatureTypeInfo selected) {
        if (selected == null) {
            return "WFS";
        }
        return selected.getTitle() != null && !selected.getTitle().isBlank() ? selected.getTitle() : selected.getName();
    }

    private record LoadedWfsLayer(OnlineWfsLayer layer, ShapefileData data) {
    }
}
