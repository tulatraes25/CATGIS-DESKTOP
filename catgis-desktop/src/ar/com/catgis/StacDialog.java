package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Dialog for browsing and downloading data from STAC catalogs.
 */
public class StacDialog extends JDialog {

    private final JTextField apiUrlField;
    private final JTextField bboxField;
    private final JTextField datetimeField;
    private final JList<String> collectionList;
    private final JList<String> itemList;
    private final JLabel statusLabel;
    private List<StacClient.StacCollection> collections;
    private List<StacClient.StacItem> items;

    public StacDialog() {
        super((Frame) null, "STAC - SpatioTemporal Asset Catalog", false);
        setSize(750, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 8, 4, 8));
        JLabel title = new JLabel("STAC - SpatioTemporal Asset Catalog");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        header.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Descubre y descarga datasets geoespaciales desde catalogos STAC");
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Top form
        JPanel topForm = new JPanel(new GridBagLayout());
        topForm.setBorder(new EmptyBorder(4, 8, 4, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0;
        topForm.add(new JLabel("API URL:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        apiUrlField = new JTextField("https://planetarycomputer.microsoft.com/api/stac/v1");
        topForm.add(apiUrlField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        JButton connectBtn = new JButton("Conectar");
        connectBtn.addActionListener(e -> loadCollections());
        topForm.add(connectBtn, gbc);

        gbc.gridy = 1; gbc.gridx = 0;
        topForm.add(new JLabel("BBOX:"), gbc);
        gbc.gridx = 1;
        bboxField = new JTextField("-70,-35,-65,-30");
        topForm.add(bboxField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        topForm.add(new JLabel("Fecha:"), gbc);
        gbc.gridx = 1;
        datetimeField = new JTextField("2024-01-01T00:00:00Z/2024-12-31T23:59:59Z");
        topForm.add(datetimeField, gbc);

        add(topForm, BorderLayout.NORTH);

        // Center: split collections and items
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 4, 4));
        centerPanel.setBorder(new EmptyBorder(4, 8, 4, 8));

        collectionList = new JList<>(new String[]{"(Conecta primero)"});
        collectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        collectionList.addListSelectionListener(e -> onCollectionSelected());
        JPanel collPanel = new JPanel(new BorderLayout());
        collPanel.setBorder(BorderFactory.createTitledBorder("Colecciones"));
        collPanel.add(new JScrollPane(collectionList), BorderLayout.CENTER);
        centerPanel.add(collPanel);

        itemList = new JList<>(new String[]{"(Selecciona una coleccion)"});
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBorder(BorderFactory.createTitledBorder("Items"));
        itemPanel.add(new JScrollPane(itemList), BorderLayout.CENTER);
        centerPanel.add(itemPanel);

        add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton downloadBtn = new JButton("Descargar seleccionado");
        downloadBtn.addActionListener(e -> downloadSelected());
        JButton closeBtn = new JButton("Cerrar");
        closeBtn.addActionListener(e -> dispose());
        footer.add(downloadBtn);
        footer.add(closeBtn);
        statusLabel = new JLabel("Conecta a un catalogo STAC para comenzar.");
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new StacDialog().setVisible(true));
    }

    private void loadCollections() {
        String url = apiUrlField.getText().trim();
        if (url.isEmpty()) return;
        statusLabel.setText("Cargando colecciones...");
        statusLabel.setForeground(Color.BLUE);

        new SwingWorker<List<StacClient.StacCollection>, Void>() {
            @Override protected List<StacClient.StacCollection> doInBackground() throws Exception {
                return StacClient.getCollections(url);
            }
            @Override protected void done() {
                try {
                    collections = get();
                    String[] names = collections.stream()
                            .map(c -> c.id() + " — " + c.title())
                            .toArray(String[]::new);
                    collectionList.setListData(names);
                    statusLabel.setText(collections.size() + " colecciones.");
                    statusLabel.setForeground(new Color(0, 128, 0));
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        }.execute();
    }

    private void onCollectionSelected() {
        int idx = collectionList.getSelectedIndex();
        if (idx < 0 || collections == null || idx >= collections.size()) return;

        StacClient.StacCollection coll = collections.get(idx);
        String url = apiUrlField.getText().trim();
        String bbox = bboxField.getText().trim();
        String datetime = datetimeField.getText().trim();

        statusLabel.setText("Buscando items en " + coll.id() + "...");
        statusLabel.setForeground(Color.BLUE);

        new SwingWorker<List<StacClient.StacItem>, Void>() {
            @Override protected List<StacClient.StacItem> doInBackground() throws Exception {
                return StacClient.searchItems(url, coll.id(), bbox, datetime);
            }
            @Override protected void done() {
                try {
                    items = get();
                    String[] names = items.stream()
                            .map(i -> i.id())
                            .toArray(String[]::new);
                    itemList.setListData(names);
                    statusLabel.setText(items.size() + " items encontrados.");
                    statusLabel.setForeground(new Color(0, 128, 0));
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        }.execute();
    }

    private void downloadSelected() {
        int idx = itemList.getSelectedIndex();
        if (idx < 0 || items == null || idx >= items.size()) {
            JOptionPane.showMessageDialog(this, "Selecciona un item para descargar.");
            return;
        }

        StacClient.StacItem item = items.get(idx);
        if (item.assetUrls() == null || item.assetUrls().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El item no tiene assets descargables.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar asset STAC");
        chooser.setSelectedFile(new File(item.id().replaceAll("[^a-zA-Z0-9_]", "_") + ".tif"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String assetUrl = item.assetUrls().get(0);
        File outputFile = chooser.getSelectedFile();

        statusLabel.setText("Descargando " + item.id() + "...");
        statusLabel.setForeground(Color.BLUE);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                StacClient.downloadAsset(assetUrl, outputFile);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    statusLabel.setText("Descargado: " + outputFile.getName());
                    statusLabel.setForeground(new Color(0, 128, 0));
                    JOptionPane.showMessageDialog(StacDialog.this,
                            "Asset descargado:\n" + outputFile.getAbsolutePath(),
                            "Exito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        }.execute();
    }
}
