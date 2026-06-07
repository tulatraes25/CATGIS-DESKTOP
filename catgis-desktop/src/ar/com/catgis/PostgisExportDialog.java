package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

public class PostgisExportDialog extends JDialog {

    private final Layer sourceLayer;
    private final ShapefileData sourceData;
    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField databaseField;
    private final JTextField schemaField;
    private final JTextField userField;
    private final JPasswordField passwordField;
    private final JCheckBox rememberPasswordCheck;
    private final JTextField tableField;
    private final JComboBox<PostgisTableWriteMode> modeCombo;
    private final JCheckBox addToProjectCheck;
    private final JButton testButton;
    private final JButton exportButton;
    private final JLabel statusLabel;

    private PostgisExportDialog(Window owner, Layer sourceLayer, ShapefileData sourceData) {
        super(owner, "Enviar capa a CATSERVER", ModalityType.APPLICATION_MODAL);
        this.sourceLayer = sourceLayer;
        this.sourceData = sourceData;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField layerField = new JTextField(sourceLayer != null ? sourceLayer.getName() : "");
        layerField.setEditable(false);

        hostField = new JTextField(18);
        portField = new JTextField("5432", 6);
        databaseField = new JTextField(14);
        schemaField = new JTextField("public", 12);
        userField = new JTextField(12);
        passwordField = new JPasswordField(12);
        rememberPasswordCheck = new JCheckBox("Guardar clave en esta computadora", true);
        tableField = new JTextField(PostgisWriteService.suggestTableName(sourceLayer != null ? sourceLayer.getName() : ""), 18);

        modeCombo = new JComboBox<>(new PostgisTableWriteMode[]{
                PostgisTableWriteMode.CREATE_NEW,
                PostgisTableWriteMode.REPLACE_TABLE,
                PostgisTableWriteMode.APPEND_RECORDS
        });
        addToProjectCheck = new JCheckBox("Agregar el resultado al proyecto como capa CATSERVER editable", true);

        applyLastConnection();

        testButton = new JButton("Probar conexión");
        testButton.addActionListener(e -> testConnection());
        exportButton = new JButton("Enviar a CATSERVER");
        exportButton.addActionListener(e -> exportLayer());

        JTextArea helpArea = new JTextArea();
        helpArea.setEditable(false);
        helpArea.setOpaque(false);
        helpArea.setLineWrap(true);
        helpArea.setWrapStyleWord(true);
        helpArea.setText("Usá este flujo para enviar una capa vectorial local o derivada hacia CATSERVER. "
                + "CATSERVER reutiliza el motor PostgreSQL/PostGIS de CATGIS y permite crear una tabla nueva, reemplazarla o anexar registros.");

        statusLabel = new JLabel("Completa la conexión, elegí schema, tabla y modo de escritura.");

        int row = 0;
        addField(form, gc, row++, "Capa origen:", layerField);
        addField(form, gc, row++, "Host:", hostField);
        addField(form, gc, row++, "Puerto:", portField);
        addField(form, gc, row++, "Base:", databaseField);
        addField(form, gc, row++, "Schema:", schemaField);
        addField(form, gc, row++, "Usuario:", userField);
        addField(form, gc, row++, "Clave:", passwordField);
        addField(form, gc, row++, "Tabla destino:", tableField);
        addField(form, gc, row++, "Modo:", modeCombo);

        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        form.add(rememberPasswordCheck, gc);

        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        form.add(addToProjectCheck, gc);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actionPanel.add(testButton);
        actionPanel.add(exportButton);
        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        form.add(actionPanel, gc);

        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        form.add(helpArea, gc);

        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        form.add(statusLabel, gc);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton closeButton = new JButton("Cancelar");
        closeButton.addActionListener(e -> dispose());
        footer.add(closeButton);

        add(WindowLayoutSupport.createVerticalScrollPane(form, 760, 480), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, exportButton, this::dispose);
        pack();
        WindowLayoutSupport.fitDialogToScreen(this, 840, 620, 720, 520);
        setLocationRelativeTo(owner);
    }

    public static void open(Window owner, Layer sourceLayer) {
        if (sourceLayer == null) {
            JOptionPane.showMessageDialog(owner, "Seleccioná una capa vectorial para enviar a CATSERVER.");
            return;
        }
        if (sourceLayer instanceof RasterLayer) {
            JOptionPane.showMessageDialog(owner, "La capa seleccionada no es vectorial.");
            return;
        }
        ShapefileData data = OpenAttributeTableAction.ensureLayerDataAvailable(sourceLayer);
        if (!ExportVectorLayerAction.hasExportableVectorData(data)) {
            JOptionPane.showMessageDialog(owner, "La capa no tiene datos vectoriales disponibles para enviar a CATSERVER.");
            return;
        }
        PostgisExportDialog dialog = new PostgisExportDialog(owner, sourceLayer, data);
        dialog.setVisible(true);
    }

    private void applyLastConnection() {
        PostgisConnectionInfo last = PostgisConnectionStore.loadProfileConnection("catserver", PostgisConnectionPreset.catserver().getDefaults());
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
        PostgisConnectionInfo info = buildConnectionInfo();
        if (info == null) {
            return;
        }
        setBusy(true, "Probando conexión CATSERVER...");
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
                    PostgisConnectionStore.saveProfileConnection("catserver", info);
                    setBusy(false, "Conexión CATSERVER verificada correctamente.");
                    JOptionPane.showMessageDialog(PostgisExportDialog.this, "La conexión CATSERVER respondió correctamente.", "CATSERVER", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    setBusy(false, "No se pudo conectar a CATSERVER.");
                    JOptionPane.showMessageDialog(PostgisExportDialog.this, PostgisErrorSupport.toUserMessage(ex, info), "CATSERVER", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void exportLayer() {
        PostgisConnectionInfo info = buildConnectionInfo();
        if (info == null) {
            return;
        }

        String schema = schemaField.getText() != null ? schemaField.getText().trim() : "";
        String table = tableField.getText() != null ? tableField.getText().trim() : "";
        PostgisTableWriteMode mode = (PostgisTableWriteMode) modeCombo.getSelectedItem();
        if (mode == null) {
            mode = PostgisTableWriteMode.CREATE_NEW;
        }

        setBusy(true, "Enviando capa a CATSERVER...");
        PostgisTableWriteMode finalMode = mode;
        new SwingWorker<PostgisWriteService.WriteResult, Void>() {
            @Override
            protected PostgisWriteService.WriteResult doInBackground() throws Exception {
                return PostgisWriteService.writeLayer(new PostgisWriteService.WriteRequest(
                        sourceLayer,
                        sourceData,
                        info,
                        schema,
                        table,
                        finalMode,
                        true
                ));
            }

            @Override
            protected void done() {
                setBusy(false, "Listo.");
                try {
                    PostgisWriteService.WriteResult result = get();
                    PostgisConnectionStore.saveProfileConnection("catserver", info);
                    if (addToProjectCheck.isSelected()) {
                        addResultToProject(result);
                    }
                    if (CatgisDesktopApp.statusBar != null) {
                        CatgisDesktopApp.statusBar.setMessage("Capa enviada a CATSERVER: " + result.layer().getSchemaName() + "." + result.layer().getTableName());
                    }
                    JOptionPane.showMessageDialog(
                            PostgisExportDialog.this,
                            "Capa enviada correctamente a CATSERVER.\n\n"
                                    + "Tabla: " + result.layer().getSchemaName() + "." + result.layer().getTableName() + "\n"
                                    + "Entidades: " + result.writtenFeatureCount(),
                            "CATSERVER",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    dispose();
                } catch (Exception ex) {
                    statusLabel.setText("No se pudo completar la escritura en CATSERVER.");
                    JOptionPane.showMessageDialog(PostgisExportDialog.this, PostgisErrorSupport.toUserMessage(ex, info), "CATSERVER", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void addResultToProject(PostgisWriteService.WriteResult result) {
        if (result == null) {
            return;
        }
        if (CatgisDesktopApp.currentProject == null) {
            CatgisDesktopApp.currentProject = new Project("Proyecto actual");
        }
        CatgisDesktopApp.currentProject.addLayer(result.layer());
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.addLayer(result.layer());
            CatgisDesktopApp.layersPanel.selectLayer(result.layer());
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(result.layer(), result.data());
            CatgisDesktopApp.mapPanel.showOpenedFile(result.layer().getName());
        }
        CatgisDesktopApp.markProjectDirty();
    }

    private PostgisConnectionInfo buildConnectionInfo() {
        PostgisConnectionInfo info = new PostgisConnectionInfo();
        info.setHost(hostField.getText());
        try {
            info.setPort(Integer.parseInt(portField.getText().trim()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "El puerto debe ser un número válido.", "CATSERVER", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        info.setDatabase(databaseField.getText());
        info.setSchema(schemaField.getText());
        info.setUser(userField.getText());
        info.setPassword(new String(passwordField.getPassword()));
        info.setRememberPassword(rememberPasswordCheck.isSelected());

        String validationMessage = PostgisErrorSupport.validateConnectionInfo(info, true);
        if (!validationMessage.isBlank()) {
            JOptionPane.showMessageDialog(this, validationMessage, "CATSERVER", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        String schemaValidation = PostgisWriteService.validateIdentifier(schemaField.getText(), "El schema");
        if (!schemaValidation.isBlank()) {
            JOptionPane.showMessageDialog(this, schemaValidation, "CATSERVER", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        String tableValidation = PostgisWriteService.validateIdentifier(tableField.getText(), "El nombre de tabla");
        if (!tableValidation.isBlank()) {
            JOptionPane.showMessageDialog(this, tableValidation, "CATSERVER", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return info;
    }

    private void setBusy(boolean busy, String status) {
        hostField.setEnabled(!busy);
        portField.setEnabled(!busy);
        databaseField.setEnabled(!busy);
        schemaField.setEnabled(!busy);
        userField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
        rememberPasswordCheck.setEnabled(!busy);
        tableField.setEnabled(!busy);
        modeCombo.setEnabled(!busy);
        addToProjectCheck.setEnabled(!busy);
        testButton.setEnabled(!busy);
        exportButton.setEnabled(!busy);
        statusLabel.setText(status);
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
}
