package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GeoPackageDataSourceDialog extends JDialog {

    private final JTextField fileField;
    private final JButton browseButton;
    private final JButton connectButton;
    private final JButton addButton;
    private final DefaultListModel<GeoPackageFeatureInfo> featureModel;
    private final JList<GeoPackageFeatureInfo> featureList;
    private final JLabel statusLabel;
    private final JTextArea helpArea;

    private File selectedFile;
    private boolean accepted = false;

    private GeoPackageDataSourceDialog(Window owner, File initialFile) {
        super(owner, "Origen de datos GeoPackage", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel fileLabel = new JLabel("Archivo GeoPackage:");
        fileField = new JTextField();
        fileField.setEditable(false);
        browseButton = new JButton("Buscar...");
        browseButton.addActionListener(e -> chooseFile());
        connectButton = new JButton("Inspeccionar");
        connectButton.addActionListener(e -> inspectGeoPackage());

        featureModel = new DefaultListModel<>();
        featureList = new JList<>(featureModel);
        featureList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        featureList.setVisibleRowCount(8);
        featureList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String label = value instanceof GeoPackageFeatureInfo
                        ? ((GeoPackageFeatureInfo) value).getDisplayLabel()
                        : String.valueOf(value);
                return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            }
        });

        helpArea = new JTextArea();
        helpArea.setEditable(false);
        helpArea.setOpaque(false);
        helpArea.setLineWrap(true);
        helpArea.setWrapStyleWord(true);
        helpArea.setText("Carga capas vectoriales desde un GeoPackage sin salir de CATGIS. En esta primera etapa se cargan en modo lectura para preservar estabilidad y compatibilidad con el resto del proyecto.");

        statusLabel = new JLabel("Elegí un archivo .gpkg y hacé clic en Inspeccionar.");

        JPanel fileRow = new JPanel(new BorderLayout(8, 0));
        fileRow.add(fileField, BorderLayout.CENTER);
        JPanel fileButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        fileButtons.add(browseButton);
        fileButtons.add(connectButton);
        fileRow.add(fileButtons, BorderLayout.EAST);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        content.add(fileLabel, gc);

        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1;
        content.add(fileRow, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 2;
        gc.weightx = 1;
        content.add(new JScrollPane(featureList), gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.gridwidth = 2;
        content.add(helpArea, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        gc.gridwidth = 2;
        content.add(statusLabel, gc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        addButton = new JButton("Agregar al proyecto");
        addButton.addActionListener(e -> addSelectedLayers());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        buttons.add(addButton);
        buttons.add(closeButton);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, connectButton, this::dispose);
        setSize(760, 470);
        setLocationRelativeTo(owner);

        if (initialFile != null) {
            selectedFile = initialFile;
            fileField.setText(initialFile.getAbsolutePath());
            inspectGeoPackage();
        }
    }

    public static boolean open(Window owner, File initialFile) {
        GeoPackageDataSourceDialog dialog = new GeoPackageDataSourceDialog(owner, initialFile);
        dialog.setVisible(true);
        return dialog.accepted;
    }

    private void chooseFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("gpkg-open", "Seleccionar GeoPackage");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("GeoPackage (*.gpkg)", "gpkg"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            FileChooserSupport.rememberSelection("gpkg-open", chooser);
            fileField.setText(selectedFile != null ? selectedFile.getAbsolutePath() : "");
        }
    }

    private void inspectGeoPackage() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Primero elegí un archivo GeoPackage.", "GeoPackage", JOptionPane.WARNING_MESSAGE);
            return;
        }

        statusLabel.setText("Inspeccionando GeoPackage...");
        browseButton.setEnabled(false);
        connectButton.setEnabled(false);
        addButton.setEnabled(false);

        new SwingWorker<List<GeoPackageFeatureInfo>, Void>() {
            @Override
            protected List<GeoPackageFeatureInfo> doInBackground() throws Exception {
                return GeoPackageLoader.listFeatureEntries(selectedFile);
            }

            @Override
            protected void done() {
                browseButton.setEnabled(true);
                connectButton.setEnabled(true);
                try {
                    List<GeoPackageFeatureInfo> entries = get();
                    featureModel.clear();
                    for (GeoPackageFeatureInfo entry : entries) {
                        featureModel.addElement(entry);
                    }
                    if (!entries.isEmpty()) {
                        featureList.setSelectionInterval(0, 0);
                        addButton.setEnabled(true);
                        getRootPane().setDefaultButton(addButton);
                        statusLabel.setText("GeoPackage inspeccionado. Seleccioná una o varias capas para cargarlas.");
                    } else {
                        addButton.setEnabled(false);
                        getRootPane().setDefaultButton(connectButton);
                        statusLabel.setText("El GeoPackage no contiene capas vectoriales.");
                    }
                } catch (Exception ex) {
                    featureModel.clear();
                    addButton.setEnabled(false);
                    getRootPane().setDefaultButton(connectButton);
                    statusLabel.setText("No se pudo leer el GeoPackage.");
                    JOptionPane.showMessageDialog(
                            GeoPackageDataSourceDialog.this,
                            "No se pudo inspeccionar el GeoPackage:\n" + ex.getMessage(),
                            "GeoPackage",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void addSelectedLayers() {
        List<GeoPackageFeatureInfo> selected = featureList.getSelectedValuesList();
        if (selectedFile == null || selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccioná al menos una capa del GeoPackage.", "GeoPackage", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        statusLabel.setText("Cargando capas GeoPackage...");
        addButton.setEnabled(false);
        connectButton.setEnabled(false);
        browseButton.setEnabled(false);

        new SwingWorker<List<LoadedGeoPackageLayer>, Void>() {
            @Override
            protected List<LoadedGeoPackageLayer> doInBackground() throws Exception {
                List<LoadedGeoPackageLayer> loaded = new ArrayList<>();
                for (GeoPackageFeatureInfo info : selected) {
                    GeoPackageLayer layer = new GeoPackageLayer(buildDisplayName(info), selectedFile.getAbsolutePath());
                    layer.setTableName(info.getTableName());
                    layer.setIdentifier(info.getIdentifier());
                    layer.setDescription(info.getDescription());
                    layer.setGeometryTypeLabel(info.getGeometryTypeLabel());
                    layer.setSourceName(selectedFile.getName());
                    layer.setSourceCRS(info.getCrsCode());
                    layer.setReadOnly(true);
                    ShapefileData data = GeoPackageLoader.loadLayerData(layer);
                    loaded.add(new LoadedGeoPackageLayer(layer, data));
                }
                return loaded;
            }

            @Override
            protected void done() {
                addButton.setEnabled(true);
                connectButton.setEnabled(true);
                browseButton.setEnabled(true);
                getRootPane().setDefaultButton(addButton);
                try {
                    List<LoadedGeoPackageLayer> loaded = get();
                    if (loaded.isEmpty()) {
                        throw new IllegalStateException("No se pudo cargar ninguna capa del GeoPackage.");
                    }

                    if (CatgisDesktopApp.currentProject == null) {
                        CatgisDesktopApp.currentProject = new Project("Proyecto actual");
                    }

                    for (LoadedGeoPackageLayer item : loaded) {
                        item.layer.setFeatureCount(item.data.getFeatureCount());
                        if (item.layer.getSourceCRS() == null || item.layer.getSourceCRS().isBlank()) {
                            item.layer.setSourceCRS(item.data.getSchema() != null && item.data.getSchema().getCoordinateReferenceSystem() != null
                                    ? org.geotools.referencing.CRS.toSRS(item.data.getSchema().getCoordinateReferenceSystem(), true)
                                    : "");
                        }
                        CatgisDesktopApp.currentProject.addLayer(item.layer);
                        if (CatgisDesktopApp.layersPanel != null) {
                            CatgisDesktopApp.layersPanel.addLayer(item.layer);
                            CatgisDesktopApp.layersPanel.selectLayer(item.layer);
                        }
                        if (CatgisDesktopApp.mapPanel != null) {
                            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(item.layer, item.data);
                            CatgisDesktopApp.mapPanel.showOpenedFile(item.layer.getName());
                        }
                    }

                    CatgisDesktopApp.markProjectDirty();
                    if (CatgisDesktopApp.statusBar != null) {
                        CatgisDesktopApp.statusBar.setMessage(loaded.size() == 1
                                ? "Capa GeoPackage agregada: " + loaded.get(0).layer.getName()
                                : loaded.size() + " capas GeoPackage agregadas.");
                    }
                    accepted = true;
                    dispose();
                } catch (Exception ex) {
                    statusLabel.setText("No se pudieron cargar las capas del GeoPackage.");
                    JOptionPane.showMessageDialog(
                            GeoPackageDataSourceDialog.this,
                            "No se pudieron cargar las capas del GeoPackage:\n" + ex.getMessage(),
                            "GeoPackage",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private String buildDisplayName(GeoPackageFeatureInfo info) {
        if (info == null) {
            return selectedFile != null ? selectedFile.getName() : "GeoPackage";
        }
        return info.getIdentifier() != null && !info.getIdentifier().isBlank()
                ? info.getIdentifier()
                : info.getTableName();
    }

    private record LoadedGeoPackageLayer(GeoPackageLayer layer, ShapefileData data) {
    }
}
