package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

public class PostgisBrowserDialog extends JDialog {

    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField databaseField;
    private final JTextField schemaField;
    private final JTextField userField;
    private final JPasswordField passwordField;
    private final JCheckBox rememberPasswordCheck;
    private final JButton testButton;
    private final JButton connectButton;
    private JButton addButton;
    private final DefaultListModel<PostgisFeatureTypeInfo> featureModel;
    private final JList<PostgisFeatureTypeInfo> featureList;
    private final JLabel statusLabel;
    private boolean accepted = false;

    private PostgisBrowserDialog(Window owner) {
        super(owner, "Origen de datos PostGIS", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        hostField = new JTextField(18);
        portField = new JTextField("5432", 6);
        databaseField = new JTextField(14);
        schemaField = new JTextField("public", 12);
        userField = new JTextField(12);
        passwordField = new JPasswordField(12);
        rememberPasswordCheck = new JCheckBox("Guardar clave en esta computadora", true);

        applyLastConnection();

        testButton = new JButton("Probar conexion");
        testButton.addActionListener(e -> testConnection());
        connectButton = new JButton("Listar capas");
        connectButton.addActionListener(e -> fetchLayers());

        featureModel = new DefaultListModel<>();
        featureList = new JList<>(featureModel);
        featureList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        featureList.setVisibleRowCount(10);
        featureList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String label = value instanceof PostgisFeatureTypeInfo
                        ? ((PostgisFeatureTypeInfo) value).getDisplayLabel()
                        : String.valueOf(value);
                return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            }
        });
        featureList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                addButton.setEnabled(!featureList.isSelectionEmpty());
                getRootPane().setDefaultButton(addButton.isEnabled() ? addButton : connectButton);
            }
        });

        JTextArea helpArea = new JTextArea();
        helpArea.setEditable(false);
        helpArea.setOpaque(false);
        helpArea.setLineWrap(true);
        helpArea.setWrapStyleWord(true);
        helpArea.setText("Conecta una base PostGIS, lista tablas espaciales y las carga al proyecto como capas vectoriales reales. En esta primera etapa se incorporan en modo lectura para mantener estabilidad.");

        statusLabel = new JLabel("Completa la conexion PostGIS, probala y despues lista las capas espaciales.");

        int row = 0;
        addField(content, gc, row++, "Host:", hostField);
        addField(content, gc, row++, "Puerto:", portField);
        addField(content, gc, row++, "Base:", databaseField);
        addField(content, gc, row++, "Schema:", schemaField);
        addField(content, gc, row++, "Usuario:", userField);
        addField(content, gc, row++, "Clave:", passwordField);

        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        content.add(rememberPasswordCheck, gc);

        JPanel connectButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        connectButtons.add(testButton);
        connectButtons.add(connectButton);
        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        content.add(connectButtons, gc);

        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        content.add(new JScrollPane(featureList), gc);

        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        content.add(helpArea, gc);

        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        content.add(statusLabel, gc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        addButton = new JButton("Agregar al proyecto");
        addButton.setEnabled(false);
        addButton.addActionListener(e -> addSelectedLayers());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        buttons.add(addButton);
        buttons.add(closeButton);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, connectButton, this::dispose);
        setSize(780, 560);
        setLocationRelativeTo(owner);
    }

    public static boolean open(Window owner) {
        PostgisBrowserDialog dialog = new PostgisBrowserDialog(owner);
        dialog.setVisible(true);
        return dialog.accepted;
    }

    private void applyLastConnection() {
        PostgisConnectionInfo last = PostgisConnectionStore.loadLastConnection();
        if (last == null) {
            return;
        }
        hostField.setText(last.getHost());
        portField.setText(String.valueOf(last.getPort()));
        databaseField.setText(last.getDatabase());
        schemaField.setText(last.getSchema());
        userField.setText(last.getUser());
        if (last.getPassword() != null && !last.getPassword().isBlank()) {
            passwordField.setText(last.getPassword());
        }
        rememberPasswordCheck.setSelected(last.isRememberPassword());
    }

    private void testConnection() {
        PostgisConnectionInfo info = buildConnectionInfo(true);
        if (info == null) {
            return;
        }
        setBusy(true, "Probando conexion PostGIS...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                PostgisLoader.testConnection(info);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    setBusy(false, "Conexion PostGIS verificada correctamente.");
                    PostgisConnectionStore.saveLastConnection(info);
                    JOptionPane.showMessageDialog(
                            PostgisBrowserDialog.this,
                            "La conexion PostGIS respondio correctamente.",
                            "PostGIS",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    setBusy(false, "No se pudo conectar a PostGIS.");
                    statusLabel.setText("No se pudo conectar a PostGIS.");
                    JOptionPane.showMessageDialog(
                            PostgisBrowserDialog.this,
                            PostgisErrorSupport.toUserMessage(ex, info),
                            "PostGIS",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void fetchLayers() {
        PostgisConnectionInfo info = buildConnectionInfo(true);
        if (info == null) {
            return;
        }
        setBusy(true, "Consultando tablas espaciales de PostGIS...");
        featureModel.clear();
        addButton.setEnabled(false);

        new SwingWorker<List<PostgisFeatureTypeInfo>, Void>() {
            @Override
            protected List<PostgisFeatureTypeInfo> doInBackground() throws Exception {
                return PostgisLoader.listFeatureTypes(info);
            }

            @Override
            protected void done() {
                try {
                    List<PostgisFeatureTypeInfo> layers = get();
                    setBusy(false, "Listo.");
                    PostgisConnectionStore.saveLastConnection(info);
                    for (PostgisFeatureTypeInfo featureType : layers) {
                        featureModel.addElement(featureType);
                    }
                    if (layers.isEmpty()) {
                        getRootPane().setDefaultButton(connectButton);
                        statusLabel.setText("La conexion esta bien, pero no se encontraron tablas espaciales en el schema indicado.");
                    } else {
                        featureList.setSelectionInterval(0, 0);
                        addButton.setEnabled(true);
                        getRootPane().setDefaultButton(addButton);
                        statusLabel.setText("PostGIS conectado. Selecciona una o varias capas para cargarlas al proyecto en modo lectura.");
                    }
                } catch (Exception ex) {
                    setBusy(false, "No se pudieron listar las capas espaciales.");
                    statusLabel.setText("No se pudieron listar las capas espaciales.");
                    JOptionPane.showMessageDialog(
                            PostgisBrowserDialog.this,
                            PostgisErrorSupport.toUserMessage(ex, info),
                            "PostGIS",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void addSelectedLayers() {
        List<PostgisFeatureTypeInfo> selected = featureList.getSelectedValuesList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos una tabla espacial.", "PostGIS", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PostgisConnectionInfo info = buildConnectionInfo(true);
        if (info == null) {
            return;
        }

        setBusy(true, "Cargando capas PostGIS...");
        new SwingWorker<List<LoadedPostgisLayer>, Void>() {
            @Override
            protected List<LoadedPostgisLayer> doInBackground() throws Exception {
                List<LoadedPostgisLayer> loaded = new ArrayList<>();
                for (PostgisFeatureTypeInfo selectedLayer : selected) {
                    PostgisLayer layer = new PostgisLayer(buildDisplayName(selectedLayer));
                    layer.setConnectionInfo(info);
                    layer.setTypeName(selectedLayer.getTypeName());
                    layer.setTableName(selectedLayer.getTableName());
                    layer.setSchemaName(selectedLayer.getSchemaName());
                    layer.setGeometryTypeLabel(selectedLayer.getGeometryTypeLabel());
                    layer.setSourceCRS(selectedLayer.getCrsCode());
                    layer.setReadOnly(true);
                    ShapefileData data = PostgisLoader.loadLayerData(layer, info);
                    loaded.add(new LoadedPostgisLayer(layer, data));
                }
                return loaded;
            }

            @Override
            protected void done() {
                setBusy(false, "Listo.");
                try {
                    List<LoadedPostgisLayer> loaded = get();
                    if (loaded.isEmpty()) {
                        throw new IllegalStateException("No se pudo cargar ninguna capa PostGIS.");
                    }

                    if (CatgisDesktopApp.currentProject == null) {
                        CatgisDesktopApp.currentProject = new Project("Proyecto actual");
                    }

                    PostgisConnectionStore.saveLastConnection(info);

                    for (LoadedPostgisLayer item : loaded) {
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
                                ? "Capa PostGIS agregada: " + loaded.get(0).layer.getName()
                                : loaded.size() + " capas PostGIS agregadas.");
                    }
                    accepted = true;
                    dispose();
                } catch (Exception ex) {
                    statusLabel.setText("No se pudieron cargar las capas PostGIS.");
                    JOptionPane.showMessageDialog(
                            PostgisBrowserDialog.this,
                            PostgisErrorSupport.toUserMessage(ex, info),
                            "PostGIS",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private PostgisConnectionInfo buildConnectionInfo(boolean requirePassword) {
        PostgisConnectionInfo info = new PostgisConnectionInfo();
        info.setHost(hostField.getText());
        try {
            info.setPort(Integer.parseInt(portField.getText().trim()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "El puerto debe ser un numero valido.", "PostGIS", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        info.setDatabase(databaseField.getText());
        info.setSchema(schemaField.getText());
        info.setUser(userField.getText());
        info.setPassword(new String(passwordField.getPassword()));
        info.setRememberPassword(rememberPasswordCheck.isSelected());

        String validationMessage = PostgisErrorSupport.validateConnectionInfo(info, requirePassword);
        if (!validationMessage.isBlank()) {
            JOptionPane.showMessageDialog(this, validationMessage, "PostGIS", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return info;
    }

    private void addField(JPanel target, GridBagConstraints gc, int row, String label, java.awt.Component field) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        target.add(new JLabel(label), gc);

        gc.gridx = 1;
        gc.gridy = row;
        gc.weightx = 1;
        target.add(field, gc);
    }

    private void setBusy(boolean busy, String status) {
        hostField.setEnabled(!busy);
        portField.setEnabled(!busy);
        databaseField.setEnabled(!busy);
        schemaField.setEnabled(!busy);
        userField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
        rememberPasswordCheck.setEnabled(!busy);
        testButton.setEnabled(!busy);
        connectButton.setEnabled(!busy);
        addButton.setEnabled(!busy && !featureList.isSelectionEmpty());
        statusLabel.setText(status);
    }

    private String buildDisplayName(PostgisFeatureTypeInfo info) {
        if (info == null) {
            return "PostGIS";
        }
        return info.getTableName() != null && !info.getTableName().isBlank()
                ? info.getTableName()
                : info.getTypeName();
    }

    private record LoadedPostgisLayer(PostgisLayer layer, ShapefileData data) {
    }
}
