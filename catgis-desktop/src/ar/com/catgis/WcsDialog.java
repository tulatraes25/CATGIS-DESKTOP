package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Dialog for connecting to WCS servers and downloading raster coverages.
 */
public class WcsDialog extends JDialog {

    private final JTextField serviceUrlField;
    private final JList<String> coverageList;
    private final JTextField bboxField;
    private final JTextField crsField;
    private final JTextField widthField;
    private final JTextField heightField;
    private final JLabel statusLabel;
    private List<WcsClient.WcsCoverage> coverages;

    public WcsDialog() {
        super((Frame) null, "WCS - Web Coverage Service", false);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 8, 4, 8));
        JLabel title = new JLabel("WCS - Web Coverage Service");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        header.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Conecta a un servidor WCS para descargar coberturas raster");
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(4, 8, 4, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Service URL
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("URL del servicio:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        serviceUrlField = new JTextField("https://example.com/wcs");
        form.add(serviceUrlField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        JButton connectButton = new JButton("Conectar");
        connectButton.addActionListener(e -> connectToServer());
        form.add(connectButton, gbc);

        // Coverage list
        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 3; gbc.weightx = 1.0;
        coverageList = new JList<>(new String[]{"(Conecta a un servidor primero)"});
        form.add(new JScrollPane(coverageList), gbc);

        // Parameters
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        form.add(new JLabel("BBOX:"), gbc);
        gbc.gridx = 1;
        bboxField = new JTextField("-180,-90,180,90");
        form.add(bboxField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        form.add(new JLabel("CRS:"), gbc);
        gbc.gridx = 1;
        crsField = new JTextField("EPSG:4326");
        form.add(crsField, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        form.add(new JLabel("Ancho/Alto:"), gbc);
        gbc.gridx = 1;
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        widthField = new JTextField("512", 6);
        heightField = new JTextField("512", 6);
        sizePanel.add(widthField);
        sizePanel.add(new JLabel("x"));
        sizePanel.add(heightField);
        form.add(sizePanel, gbc);

        add(form, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton downloadButton = new JButton("Descargar");
        downloadButton.addActionListener(e -> downloadCoverage());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(downloadButton);
        footer.add(closeButton);
        statusLabel = new JLabel("Conecta a un servidor WCS para comenzar.");
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new WcsDialog().setVisible(true));
    }

    private void connectToServer() {
        String url = serviceUrlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa la URL del servidor WCS.");
            return;
        }

        statusLabel.setText("Conectando...");
        statusLabel.setForeground(Color.BLUE);

        new SwingWorker<List<WcsClient.WcsCoverage>, Void>() {
            @Override
            protected List<WcsClient.WcsCoverage> doInBackground() throws Exception {
                return WcsClient.getCoverages(url);
            }

            @Override
            protected void done() {
                try {
                    coverages = get();
                    String[] names = coverages.stream()
                            .map(c -> c.name() + " — " + c.title())
                            .toArray(String[]::new);
                    coverageList.setListData(names);
                    statusLabel.setText(coverages.size() + " coberturas encontradas.");
                    statusLabel.setForeground(new Color(0, 128, 0));
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(WcsDialog.this,
                            "Error al conectar:\n" + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void downloadCoverage() {
        int idx = coverageList.getSelectedIndex();
        if (idx < 0 || coverages == null || idx >= coverages.size()) {
            JOptionPane.showMessageDialog(this, "Selecciona una cobertura.");
            return;
        }

        WcsClient.WcsCoverage coverage = coverages.get(idx);
        String url = serviceUrlField.getText().trim();
        String bbox = bboxField.getText().trim();
        String crs = crsField.getText().trim();
        int width, height;
        try {
            width = Integer.parseInt(widthField.getText().trim());
            height = Integer.parseInt(heightField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Dimensiones invalidas.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar cobertura como GeoTIFF");
        chooser.setSelectedFile(new File(coverage.name().replaceAll("[^a-zA-Z0-9_]", "_") + ".tif"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        statusLabel.setText("Descargando " + coverage.name() + "...");
        statusLabel.setForeground(Color.BLUE);

        File outputFile = chooser.getSelectedFile();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                WcsClient.downloadCoverageToFile(url, coverage.name(), bbox, crs, width, height, outputFile);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Descargado: " + outputFile.getName());
                    statusLabel.setForeground(new Color(0, 128, 0));

                    // Load as raster layer
                    boolean loaded = loadAsRasterLayer(outputFile, coverage.name());

                    JOptionPane.showMessageDialog(WcsDialog.this,
                            "Cobertura descargada" + (loaded ? " y agregada como capa" : "") + ".\n\n"
                                    + "Archivo: " + outputFile.getAbsolutePath()
                                    + "\nTamanio: " + (outputFile.length() / 1024) + " KB",
                            "Descarga completada", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(WcsDialog.this,
                            "Error al descargar:\n" + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private boolean loadAsRasterLayer(File file, String coverageName) {
        try {
            if (AppContext.project() == null) return false;
            String projectCRS = AppContext.project().getProjectCRS();

            // Ask user for resolution
            String[] options = {"Preview (2048px, rapido)", "Full (8192px, lento)"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Que resolucion queres para la capa cargada?",
                    "Resolucion", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            ar.com.catgis.data.raster.LocalRasterData rasterData;
            if (choice == 1) {
                rasterData = ar.com.catgis.RasterImageLoader.loadReal(file, projectCRS, null);
            } else {
                rasterData = ar.com.catgis.RasterImageLoader.loadPreview(file, projectCRS, null);
            }

            RasterLayer layer = new RasterLayer(coverageName, file.getAbsolutePath());
            layer.setVisible(true);
            layer.setSourceName(coverageName);
            layer.setFeatureCount(1);
            layer.setSourceCRS(ar.com.catgis.data.raster.RasterCoverageSupport
                    .resolveOperationalRasterCrs(rasterData, projectCRS));
            layer.setRasterMode(rasterData.getRasterMode());

            AppContext.project().addLayer(layer);
            CatgisDesktopApp.markProjectDirty();
            AppContext.addLayer(layer);
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, rasterData);
            CatgisDesktopApp.mapPanel.zoomToLayer(layer);
            return true;
        } catch (Exception e) {
            CatgisLogger.warn("Failed to load WCS coverage as layer", e);
            return false;
        }
    }
}
