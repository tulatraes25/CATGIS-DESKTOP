package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.LayerGroup;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PostgisBrowserDialog extends JDialog {

    private final JTextField hostField = new JTextField(18);
    private final JTextField portField = new JTextField("5432", 6);
    private final JTextField databaseField = new JTextField(14);
    private final JComboBox<String> schemaCombo = new JComboBox<>();
    private final JTextField userField = new JTextField(12);
    private final JPasswordField passwordField = new JPasswordField(12);
    private final JCheckBox rememberPasswordCheck = new JCheckBox(I18n.t("Guardar clave en esta computadora"), true);
    private final JCheckBox loadEditableCheck = new JCheckBox(I18n.t("Cargar como capa editable si la tabla permite escritura"), true);
    private final JButton testButton = new JButton(I18n.t("Probar conexion"));
    private final JButton connectButton = new JButton(I18n.t("Conectar y listar capas"));
    private final JButton addButton = new JButton(I18n.t("Agregar al proyecto"));
    private final JButton selectSchemaButton = new JButton(I18n.t("Marcar esquema"));
    private final JButton clearSchemaButton = new JButton(I18n.t("Limpiar esquema"));
    private final JButton selectAllButton = new JButton(I18n.t("Marcar todo"));
    private final JButton invertVisibleButton = new JButton(I18n.t("Invertir visibles"));
    private final JLabel statusLabel = new JLabel(I18n.t("Configura conexion, probala y lista capas."));
    private final boolean catserverMode;

    private final LayerTableModel tableModel = new LayerTableModel();
    private final JTable layerTable = new JTable(tableModel);
    private final List<LayerRow> allRows = new ArrayList<>();
    private boolean busy = false;
    private boolean schemaUpdating = false;
    private boolean accepted = false;

    private PostgisBrowserDialog(Window owner, boolean catserverMode) {
        super(owner, I18n.t(catserverMode ? "Conectar CATSERVER" : "Conexion PostGIS"), ModalityType.APPLICATION_MODAL);
        this.catserverMode = catserverMode;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        rememberPasswordCheck.setOpaque(false);
        loadEditableCheck.setOpaque(false);
        schemaCombo.setEditable(false);
        setSchemaOptions(List.of(), "");
        applyLastConnection();
        if (this.catserverMode) {
            if (databaseField.getText() == null || databaseField.getText().isBlank()) {
                databaseField.setText("catserver");
            }
            schemaCombo.setEnabled(false);
        }

        layerTable.setFillsViewportHeight(true);
        layerTable.setRowHeight(24);
        layerTable.getColumnModel().getColumn(0).setMaxWidth(46);
        layerTable.getColumnModel().getColumn(0).setMinWidth(46);
        tableModel.onSelectionChanged(this::updateButtonsState);

        testButton.addActionListener(e -> testConnection());
        connectButton.addActionListener(e -> listLayers());
        addButton.addActionListener(e -> addSelectedLayers());
        selectSchemaButton.addActionListener(e -> selectBySchema(true));
        clearSchemaButton.addActionListener(e -> selectBySchema(false));
        selectAllButton.addActionListener(e -> selectAll(true));
        invertVisibleButton.addActionListener(e -> invertVisible());
        schemaCombo.addActionListener(e -> {
            if (!schemaUpdating && !busy && !this.catserverMode) {
                refreshVisibleRows();
            }
        });

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        content.add(buildConnectionPanel(), BorderLayout.NORTH);
        content.add(buildLayersPanel(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, connectButton, this::dispose);
        getRootPane().setDefaultButton(connectButton);
        pack();
        WindowLayoutSupport.fitDialogToScreen(this, 980, 760, 860, 620);
        setLocationRelativeTo(owner);
    }

    public static boolean open(Window owner) { PostgisBrowserDialog d = new PostgisBrowserDialog(owner, false); d.setVisible(true); return d.accepted; }
    public static boolean openCatserver(Window owner) { PostgisBrowserDialog d = new PostgisBrowserDialog(owner, true); d.setVisible(true); return d.accepted; }

    private JPanel buildConnectionPanel() {
        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        int r = 0;
        addField(fields, gc, r++, I18n.t("Host:"), hostField);
        addField(fields, gc, r++, I18n.t("Puerto:"), portField);
        addField(fields, gc, r++, I18n.t("Base:"), databaseField);
        addField(fields, gc, r++, I18n.t(catserverMode ? "Schema (filtro):" : "Esquema:"), schemaCombo);
        addField(fields, gc, r++, I18n.t("Usuario:"), userField);
        addField(fields, gc, r++, I18n.t("Clave:"), passwordField);
        gc.gridx = 0; gc.gridy = r++; gc.gridwidth = 2; gc.weightx = 1; fields.add(rememberPasswordCheck, gc);
        JPanel a = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); a.add(testButton); a.add(connectButton);
        gc.gridy = r++; fields.add(a, gc);
        gc.gridy = r; fields.add(loadEditableCheck, gc);

        JPanel container = new JPanel(new BorderLayout(0, 8));
        container.setBorder(BorderFactory.createTitledBorder(I18n.t(catserverMode ? "Conexion CATSERVER" : "Conexion PostGIS")));
        if (catserverMode) {
            container.add(buildCatserverHeader(), BorderLayout.NORTH);
        }
        container.add(fields, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildCatserverHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBorder(BorderFactory.createEmptyBorder(2, 2, 4, 2));

        JLabel imageLabel = new JLabel();
        ImageIcon banner = loadCatserverBannerIcon();
        if (banner != null) {
            imageLabel.setIcon(banner);
            imageLabel.setPreferredSize(new Dimension(banner.getIconWidth(), banner.getIconHeight()));
        } else {
            imageLabel.setText(I18n.t("CATSERVER"));
            imageLabel.setPreferredSize(new Dimension(220, 120));
        }

        JLabel text = new JLabel(
                "<html><b>" + I18n.t("CATSERVER") + "</b><br><br>"
                        + I18n.t("Acceso directo para conectar servidores PostgreSQL/PostGIS y listar capas espaciales para incorporarlas al proyecto.")
                        + "</html>"
        );
        header.add(imageLabel, BorderLayout.WEST);
        header.add(text, BorderLayout.CENTER);
        return header;
    }

    private ImageIcon loadCatserverBannerIcon() {
        URL url = getClass().getResource("/help/assets/catserver-connect.png");
        if (url == null) {
            return null;
        }
        try {
            ImageIcon raw = new ImageIcon(url);
            if (raw.getIconWidth() <= 0 || raw.getIconHeight() <= 0) {
                return null;
            }
            int targetWidth = 260;
            int targetHeight = 150;
            Image scaled = raw.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ignored) {
            return null;
        }
    }

    private JPanel buildLayersPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createTitledBorder(I18n.t("Capas disponibles")));
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        if (!catserverMode) {
            tools.add(selectSchemaButton);
            tools.add(clearSchemaButton);
        }
        tools.add(selectAllButton);
        tools.add(invertVisibleButton);
        p.add(tools, BorderLayout.NORTH);
        p.add(new JScrollPane(layerTable), BorderLayout.CENTER);
        p.add(statusLabel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        addButton.setEnabled(false);
        JButton close = new JButton(I18n.t("Cerrar"));
        close.addActionListener(e -> dispose());
        p.add(addButton); p.add(close);
        return p;
    }

    private void addField(JPanel p, GridBagConstraints gc, int row, String label, java.awt.Component c) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.gridwidth = 1; p.add(new JLabel(label), gc);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1; p.add(c, gc);
    }

    private void applyLastConnection() {
        PostgisConnectionInfo last = PostgisConnectionStore.loadLastConnection();
        if (last == null) return;
        hostField.setText(last.getHost()); portField.setText(String.valueOf(last.getPort())); databaseField.setText(last.getDatabase());
        userField.setText(last.getUser()); rememberPasswordCheck.setSelected(last.isRememberPassword());
        if (last.getPassword() != null && !last.getPassword().isBlank()) passwordField.setText(last.getPassword());
        setSchemaOptions(List.of(), last.getSchema());
    }

    private void testConnection() {
        PostgisConnectionInfo info = buildInfo(true); if (info == null) return;
        setBusy(true, I18n.t(catserverMode ? "Probando conexion CATSERVER..." : "Probando conexion..."));
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception { PostgisLoader.testConnection(info); return null; }
            protected void done() {
                try {
                    get();
                    PostgisConnectionStore.saveLastConnection(info);
                    setBusy(false, I18n.t(catserverMode ? "Conexion CATSERVER OK." : "Conexion OK."));
                    JOptionPane.showMessageDialog(
                            PostgisBrowserDialog.this,
                            I18n.t(catserverMode ? "Conexion CATSERVER OK." : "Conexion PostGIS OK."),
                            I18n.t(catserverMode ? "CATSERVER" : "PostGIS"),
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    setBusy(false, I18n.t("Error de conexion."));
                    JOptionPane.showMessageDialog(
                            PostgisBrowserDialog.this,
                            PostgisErrorSupport.toUserMessage(ex, info),
                            I18n.t(catserverMode ? "CATSERVER" : "PostGIS"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void listLayers() {
        PostgisConnectionInfo info = buildInfo(true); if (info == null) return;
        setBusy(true, I18n.t(catserverMode ? "Listando capas CATSERVER..." : "Listando capas espaciales..."));
        allRows.clear(); tableModel.setRows(List.of());
        new SwingWorker<List<PostgisFeatureTypeInfo>, Void>() {
            protected List<PostgisFeatureTypeInfo> doInBackground() throws Exception {
                PostgisConnectionInfo q = info.copy();
                if (catserverMode) {
                    q.setSchema("");
                    return PostgisLoader.listCatserverFeatureTypes(q, false);
                }
                q.setSchema("");
                return PostgisLoader.listFeatureTypes(q, true);
            }
            protected void done() {
                try {
                    List<PostgisFeatureTypeInfo> layers = get();
                    for (PostgisFeatureTypeInfo l : layers) if (l != null) allRows.add(new LayerRow(l, false));
                    if (catserverMode) {
                        setSchemaOptions(List.of(), "");
                    } else {
                        setSchemaOptions(extractSchemas(layers), info.getSchema());
                    }
                    refreshVisibleRows();
                    PostgisConnectionStore.saveLastConnection(info);
                    setBusy(false, I18n.t(layers.isEmpty() ? "Sin capas." : "Capas listadas."));
                    if (layers.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                PostgisBrowserDialog.this,
                                I18n.t("No se encontraron capas espaciales.\nRevisa permisos y esquema."),
                                I18n.t(catserverMode ? "CATSERVER" : "PostGIS"),
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    allRows.clear(); tableModel.setRows(List.of()); setBusy(false, I18n.t("No se pudieron listar capas."));
                    JOptionPane.showMessageDialog(
                            PostgisBrowserDialog.this,
                            PostgisErrorSupport.toUserMessage(ex, info),
                            I18n.t(catserverMode ? "CATSERVER" : "PostGIS"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void addSelectedLayers() {
        List<PostgisFeatureTypeInfo> selected = selectedLayers(); if (selected.isEmpty()) return;
        PostgisConnectionInfo info = buildInfo(true); if (info == null) return;
        boolean editableRequested = loadEditableCheck.isSelected();
        setBusy(true, I18n.t(catserverMode ? "Cargando capas CATSERVER..." : "Cargando capas..."));
        new SwingWorker<List<LoadedPostgisLayer>, Void>() {
            protected List<LoadedPostgisLayer> doInBackground() throws Exception {
                List<LoadedPostgisLayer> loaded = new ArrayList<>();
                for (PostgisFeatureTypeInfo f : selected) {
                    PostgisConnectionInfo li = info.copy(); if (f.getSchemaName() != null && !f.getSchemaName().isBlank()) li.setSchema(f.getSchemaName());
                    PostgisLayer layer = new PostgisLayer((f.getDisplayName() != null && !f.getDisplayName().isBlank()) ? f.getDisplayName() : f.getTableName());
                    layer.setConnectionInfo(li); layer.setTypeName(f.getTypeName()); layer.setTableName(f.getTableName()); layer.setSchemaName(f.getSchemaName());
                    layer.setGeometryTypeLabel(f.getGeometryTypeLabel()); layer.setSourceCRS(f.getCrsCode()); layer.setReadOnly(!(editableRequested && f.isWritable()));
                    loaded.add(new LoadedPostgisLayer(layer, PostgisLoader.loadLayerData(layer, li)));
                }
                return loaded;
            }
            protected void done() {
                setBusy(false, I18n.t("Listo"));
                try {
                    List<LoadedPostgisLayer> loaded = get();
                    if (AppContext.project() == null) AppContext.setCurrentProject(new Project("Proyecto actual"));
                    LayerGroup catserverGroup = null;
                    if (catserverMode || isCatserverConnection(info)) {
                        catserverGroup = AppContext.project().getLayerGroup("CATSERVER");
                        if (catserverGroup == null) {
                            catserverGroup = AppContext.project().addLayerGroup("CATSERVER");
                        }
                        catserverGroup.setVisible(true);
                        catserverGroup.setExpanded(true);
                    }
                    for (LoadedPostgisLayer it : loaded) {
                        it.layer.setFeatureCount(it.data.getFeatureCount());
                        if (catserverGroup != null) {
                            AppContext.project().assignLayerToGroup(it.layer, catserverGroup.getName());
                        }
                        AppContext.project().addLayer(it.layer);
                        if (CatgisDesktopApp.layersPanel != null) { AppContext.addLayer(it.layer); AppContext.selectLayer(it.layer); }
                        if (CatgisDesktopApp.mapPanel != null) { CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(it.layer, it.data); CatgisDesktopApp.mapPanel.showOpenedFile(it.layer.getName()); }
                    }
                    PostgisConnectionStore.saveLastConnection(info);
                    CatgisDesktopApp.markProjectDirty();
                    accepted = true;
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            PostgisBrowserDialog.this,
                            PostgisErrorSupport.toUserMessage(ex, info),
                            I18n.t(catserverMode ? "CATSERVER" : "PostGIS"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private PostgisConnectionInfo buildInfo(boolean requirePassword) {
        PostgisConnectionInfo i = new PostgisConnectionInfo();
        i.setHost(hostField.getText());
        try { i.setPort(Integer.parseInt(portField.getText().trim())); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, I18n.t("Puerto invalido."), I18n.t(catserverMode ? "CATSERVER" : "PostGIS"), JOptionPane.WARNING_MESSAGE); return null; }
        i.setDatabase(databaseField.getText()); i.setSchema(selectedSchema()); i.setUser(userField.getText());
        i.setPassword(new String(passwordField.getPassword())); i.setRememberPassword(rememberPasswordCheck.isSelected());
        String validation = PostgisErrorSupport.validateConnectionInfo(i, requirePassword);
        if (!validation.isBlank()) { JOptionPane.showMessageDialog(this, validation, I18n.t(catserverMode ? "CATSERVER" : "PostGIS"), JOptionPane.WARNING_MESSAGE); return null; }
        return i;
    }

    private void setSchemaOptions(Iterable<String> schemas, String preferred) {
        String selected = preferred != null ? preferred.trim() : "";
        if (selected.isBlank()) selected = selectedSchema();
        schemaUpdating = true;
        try {
            DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>(); m.addElement(allSchemasLabel());
            for (String s : schemas) if (s != null && !s.trim().isBlank()) m.addElement(s.trim());
            Object previous = schemaCombo.getSelectedItem();
            schemaCombo.setModel(m);
            schemaCombo.setSelectedItem(selected.isBlank() ? allSchemasLabel() : selected);
            if (schemaCombo.getSelectedIndex() < 0 && previous != null) {
                schemaCombo.setSelectedItem(previous);
            }
        } finally { schemaUpdating = false; }
    }

    private String selectedSchema() {
        Object s = schemaCombo.getSelectedItem(); if (s == null) return "";
        String v = String.valueOf(s).trim(); return (v.isBlank() || allSchemasLabel().equalsIgnoreCase(v)) ? "" : v;
    }

    private String allSchemasLabel() {
        return I18n.t("(Todos los esquemas)");
    }

    private Set<String> extractSchemas(List<PostgisFeatureTypeInfo> layers) {
        Set<String> set = new LinkedHashSet<>();
        for (PostgisFeatureTypeInfo l : layers) if (l != null) set.add(normalizeSchema(l.getSchemaName()));
        return set;
    }

    private String normalizeSchema(String s) { return (s == null || s.isBlank()) ? "public" : s.trim(); }

    private void refreshVisibleRows() {
        if (catserverMode) {
            tableModel.setRows(new ArrayList<>(allRows));
            updateButtonsState();
            return;
        }
        String schema = selectedSchema();
        List<LayerRow> visible = new ArrayList<>();
        for (LayerRow r : allRows) if (schema.isBlank() || schema.equalsIgnoreCase(normalizeSchema(r.layer.getSchemaName()))) visible.add(r);
        tableModel.setRows(visible);
        updateButtonsState();
    }

    private void selectBySchema(boolean selected) {
        String schema = selectedSchema();
        for (LayerRow r : allRows) if (schema.isBlank() || schema.equalsIgnoreCase(normalizeSchema(r.layer.getSchemaName()))) r.selected = selected;
        tableModel.fireTableDataChanged(); updateButtonsState();
    }

    private void selectAll(boolean selected) { for (LayerRow r : allRows) r.selected = selected; tableModel.fireTableDataChanged(); updateButtonsState(); }
    private void invertVisible() { for (LayerRow r : tableModel.getRows()) r.selected = !r.selected; tableModel.fireTableDataChanged(); updateButtonsState(); }

    private List<PostgisFeatureTypeInfo> selectedLayers() {
        List<PostgisFeatureTypeInfo> out = new ArrayList<>();
        for (LayerRow r : allRows) if (r.selected && r.layer != null) out.add(r.layer);
        return out;
    }

    private void setBusy(boolean value, String status) {
        busy = value; boolean en = !value;
        hostField.setEnabled(en); portField.setEnabled(en); databaseField.setEnabled(en); schemaCombo.setEnabled(en && !catserverMode);
        userField.setEnabled(en); passwordField.setEnabled(en); rememberPasswordCheck.setEnabled(en); loadEditableCheck.setEnabled(en);
        testButton.setEnabled(en); connectButton.setEnabled(en); layerTable.setEnabled(en);
        statusLabel.setText(status); updateButtonsState();
    }

    private void updateButtonsState() {
        boolean hasRows = !allRows.isEmpty();
        boolean hasVisible = tableModel.getRowCount() > 0;
        boolean hasSelection = !selectedLayers().isEmpty();
        selectSchemaButton.setEnabled(!busy && !catserverMode && hasVisible);
        clearSchemaButton.setEnabled(!busy && !catserverMode && hasVisible);
        selectAllButton.setEnabled(!busy && hasRows);
        invertVisibleButton.setEnabled(!busy && hasVisible);
        addButton.setEnabled(!busy && hasSelection);
        getRootPane().setDefaultButton(addButton.isEnabled() ? addButton : connectButton);
    }

    private static final class LayerRow {
        private final PostgisFeatureTypeInfo layer;
        private boolean selected;
        private LayerRow(PostgisFeatureTypeInfo layer, boolean selected) { this.layer = layer; this.selected = selected; }
    }

    private static final class LayerTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Sel.", "Capa", "Tabla", "Geom", "CRS", "Estado"};
        private final List<LayerRow> rows = new ArrayList<>();
        private Runnable selectionChanged = () -> {};
        void onSelectionChanged(Runnable r) { selectionChanged = r != null ? r : () -> {}; }
        List<LayerRow> getRows() { return new ArrayList<>(rows); }
        void setRows(List<LayerRow> src) { rows.clear(); if (src != null) rows.addAll(src); fireTableDataChanged(); selectionChanged.run(); }
        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { return COLUMNS.length; }
        public String getColumnName(int c) { return I18n.t(COLUMNS[c]); }
        public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        public boolean isCellEditable(int r, int c) { return c == 0; }
        public Object getValueAt(int r, int c) {
            LayerRow row = rows.get(r); PostgisFeatureTypeInfo l = row.layer;
            return switch (c) {
                case 0 -> row.selected;
                case 1 -> (l.getDisplayName() != null && !l.getDisplayName().isBlank()) ? l.getDisplayName() : l.getTableName();
                case 2 -> normalize(l.getSchemaName()) + "." + l.getTableName();
                case 3 -> l.getGeometryTypeLabel();
                case 4 -> l.getCrsCode();
                case 5 -> l.isWritable() ? I18n.t("editable") : I18n.t("solo lectura");
                default -> "";
            };
        }
        public void setValueAt(Object v, int r, int c) { if (c == 0) { rows.get(r).selected = Boolean.TRUE.equals(v); fireTableRowsUpdated(r, r); selectionChanged.run(); } }
        private static String normalize(String s) { return (s == null || s.isBlank()) ? "public" : s.trim(); }
    }

    private record LoadedPostgisLayer(PostgisLayer layer, ShapefileData data) {}

    private boolean isCatserverConnection(PostgisConnectionInfo info) {
        if (info == null) {
            return false;
        }
        String database = info.getDatabase();
        return database != null && "catserver".equalsIgnoreCase(database.trim());
    }
}
