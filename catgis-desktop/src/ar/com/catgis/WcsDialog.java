package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.concurrent.CancellationException;
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
    private final JButton downloadButton;
    private final JButton cancelButton;
    private SwingWorker<?, ?> activeWcsWorker;
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
        statusLabel = new JLabel("Conecta a un servidor WCS para comenzar.");
        downloadButton = new JButton("Descargar");
        downloadButton.addActionListener(e -> downloadCoverage());
        cancelButton = new JButton("Cancelar descarga");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> {
            if (activeWcsWorker != null) {
                cancelButton.setEnabled(false);
                activeWcsWorker.cancel(true);
                statusLabel.setText("Cancelando descarga WCS...");
            }
        });
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(downloadButton);
        footer.add(cancelButton);
        footer.add(closeButton);
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new WcsDialog().setVisible(true));
    }

    private void connectToServer() {
        String url = serviceUrlField.getText().trim();
        if (url.isEmpty()) {
            NotificationManager.warn(this, null, "Ingresa la URL del servidor WCS.");
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
                    NotificationManager.error(WcsDialog.this,
                            "Error",
                            "Error al conectar:\n" + e.getMessage());
                }
            }
        }.execute();
    }

    // JUnit can exercise chooser semantics without constructing a JDialog.
    static boolean wantsRasterImport(int choice) {
        return choice == 0 || choice == 1;
    }

    private void finishWcsWork() {
        activeWcsWorker = null;
        downloadButton.setEnabled(true);
        cancelButton.setEnabled(false);
    }

    private void downloadCoverage() {
        if (activeWcsWorker != null) {
            return; // One WCS operation per dialog, including raster decoding.
        }
        int idx = coverageList.getSelectedIndex();
        if (idx < 0 || coverages == null || idx >= coverages.size()) {
            NotificationManager.warn(this, null, "Selecciona una cobertura.");
            return;
        }

        WcsClient.WcsCoverage coverage = coverages.get(idx);
        String url = serviceUrlField.getText().trim();
        String bbox = bboxField.getText().trim();
        String crs = crsField.getText().trim();
        try {
            WcsSpatialPreflight.validate(bbox, crs);
        } catch (java.io.IOException invalidSpatial) {
            statusLabel.setText(invalidSpatial.getMessage());
            statusLabel.setForeground(Color.RED);
            NotificationManager.warn(this, null, invalidSpatial.getMessage());
            if (invalidSpatial.getMessage().contains("BBOX")
                    || invalidSpatial.getMessage().contains("EPSG:4326")) {
                bboxField.requestFocusInWindow();
            } else {
                crsField.requestFocusInWindow();
            }
            return;
        }
        WcsDimensionsPreflight.Checked requested;
        try {
            requested = WcsDimensionsPreflight.validate(
                    widthField.getText(), heightField.getText());
        } catch (IllegalArgumentException invalid) {
            // Fail before the file chooser or any HTTP work. Never request
            // a silent 512x512 substitute for invalid user-entered dimensions.
            statusLabel.setText(invalid.getMessage());
            statusLabel.setForeground(Color.RED);
            NotificationManager.warn(this, null, invalid.getMessage());
            if (invalid.getMessage().contains("alto")) {
                heightField.requestFocusInWindow();
            } else {
                widthField.requestFocusInWindow();
            }
            return;
        }

        long maxDownloadBytes;
        try {
            maxDownloadBytes = WcsClient.configuredWcsByteLimit();
        } catch (java.io.IOException invalidBudget) {
            statusLabel.setText("Presupuesto WCS mal configurado.");
            statusLabel.setForeground(Color.RED);
            NotificationManager.error(this, "Configuracion WCS",
                    invalidBudget.getMessage());
            return;
        }
        if (WcsDimensionsPreflight.requiresConfirmation(requested, maxDownloadBytes)) {
            int decision = JOptionPane.showConfirmDialog(this,
                    WcsDimensionsPreflight.warning(requested, maxDownloadBytes),
                    "WCS - Confirmar descarga extensa",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (decision != JOptionPane.YES_OPTION) {
                statusLabel.setText("Descarga WCS cancelada antes de crear archivos.");
                statusLabel.setForeground(Color.GRAY);
                return;
            }
        }
        int width = requested.width();
        int height = requested.height();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar cobertura como GeoTIFF");
        chooser.setSelectedFile(new File(coverage.name().replaceAll("[^a-zA-Z0-9_]", "_") + ".tif"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File outputFile = chooser.getSelectedFile();
        downloadButton.setEnabled(false);
        cancelButton.setEnabled(true);
        statusLabel.setText("Descargando " + coverage.name() + "...");
        statusLabel.setForeground(Color.BLUE);

        SwingWorker<Void, Void> download = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                WcsClient.downloadCoverageToFile(url, coverage.name(), bbox, crs, width, height, outputFile);
                return null;
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    statusLabel.setText("Descarga cancelada, archivo anterior preservado.");
                    statusLabel.setForeground(Color.GRAY);
                    finishWcsWork();
                    return;
                }
                try {
                    get();
                    if (!isDisplayable()) {
                        finishWcsWork();
                        return;
                    }
                    statusLabel.setText("Descargado: " + outputFile.getName());
                    statusLabel.setForeground(new Color(0, 128, 0));
                    // Keep the file saved. Resolve import mode on EDT, then
                    // offload GeoTIFF decode in a *different* background worker.
                    beginRasterImport(outputFile, coverage.name());
                } catch (CancellationException cancelled) {
                    statusLabel.setText("Descarga cancelada.");
                    finishWcsWork();
                } catch (Exception failed) {
                    statusLabel.setText("Error: " + failed.getMessage());
                    statusLabel.setForeground(Color.RED);
                    finishWcsWork();
                    NotificationManager.error(WcsDialog.this,
                            "Error", "Error al descargar:\n" + failed.getMessage());
                }
            }
        };
        activeWcsWorker = download;
        download.execute();
    }

    private void beginRasterImport(File file, String coverageName) {
        // A valid downloaded TIFF must remain on disk even if the user
        // declines the optional project import.
        ar.com.catgis.core.model.Project project = AppContext.project();
        if (project == null) {
            statusLabel.setText("GeoTIFF guardado; no hay proyecto activo.");
            finishWcsWork();
            return;
        }
        String projectCRS = project.getProjectCRS();
        String[] choices = {"Preview (2048px, rapido)", "Full (8192px, lento)"};
        int choice = JOptionPane.showOptionDialog(this,
                "Que resolucion queres para la capa cargada?",
                "Resolucion", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, choices, choices[0]);
        if (!wantsRasterImport(choice)) {
            statusLabel.setText("GeoTIFF guardado sin agregar una capa.");
            finishWcsWork();
            return;
        }

        cancelButton.setEnabled(false); // Native raster decode may not be interruptible.
        statusLabel.setText("Procesando raster fuera de la interfaz...");
        WcsRasterAsyncLoad<ar.com.catgis.data.raster.LocalRasterData> rasterWorker =
                new WcsRasterAsyncLoad<>(
                        () -> choice == 1
                                ? RasterImageLoader.loadReal(file, projectCRS, null)
                                : RasterImageLoader.loadPreview(file, projectCRS, null),
                        data -> {
                            try {
                                if (!isDisplayable() || AppContext.project() != project) {
                                    data.dispose();
                                    statusLabel.setText("GeoTIFF guardado; el proyecto cambio.");
                                    return;
                                }
                                registerRasterLayer(project, file, coverageName, projectCRS, data);
                                statusLabel.setText("Raster agregado: " + file.getName());
                                statusLabel.setForeground(new Color(0, 128, 0));
                                NotificationManager.info(WcsDialog.this, "Descarga completada",
                                        "GeoTIFF guardado y agregado como capa:\n"
                                                + file.getAbsolutePath());
                            } catch (RuntimeException failed) {
                                data.dispose();
                                CatgisLogger.warn("Failed to register downloaded WCS raster", failed);
                                statusLabel.setText("GeoTIFF guardado; error al agregar la capa.");
                                statusLabel.setForeground(Color.RED);
                                NotificationManager.error(WcsDialog.this, "Error de capa",
                                        "El archivo se guardo, pero la capa no pudo agregarse:\n"
                                                + failed.getMessage());
                            } finally {
                                finishWcsWork();
                            }
                        },
                        failed -> {
                            CatgisLogger.warn("Failed to decode downloaded WCS raster", failed);
                            statusLabel.setText("GeoTIFF guardado; no se pudo leer la capa.");
                            statusLabel.setForeground(Color.RED);
                            finishWcsWork();
                            if (isDisplayable()) {
                                NotificationManager.error(WcsDialog.this, "Error de raster",
                                        "El archivo esta guardado, pero no pudo cargarse:\n"
                                                + failed.getMessage());
                            }
                        },
                        () -> {
                            statusLabel.setText("Carga cancelada, GeoTIFF preservado.");
                            finishWcsWork();
                        },
                        data -> {
                            if (data != null) {
                                data.dispose();
                            }
                        });
        activeWcsWorker = rasterWorker;
        rasterWorker.execute();
    }

    /**
     * EDT-only commit: map and project registration use an explicit
     * compensating rollback if anything fails. The heavyweight GeoTIFF
     * decode is already finished when this method is called.
     */
    private static void registerRasterLayer(ar.com.catgis.core.model.Project project,
                                            File file, String coverageName, String projectCRS,
                                            ar.com.catgis.data.raster.LocalRasterData rasterData) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("WCS layer publication must run on Swing EDT.");
        }
        MapPanel map = AppContext.mapPanel();
        if (map == null || project != AppContext.project()) {
            throw new IllegalStateException("No active WCS map/project for layer registration.");
        }
        RasterLayer layer = new RasterLayer(coverageName, file.getAbsolutePath());
        layer.setVisible(true);
        layer.setSourceName(coverageName);
        layer.setFeatureCount(1);
        layer.setSourceCRS(ar.com.catgis.data.raster.RasterCoverageSupport
                .resolveOperationalRasterCrs(rasterData, projectCRS));
        layer.setRasterMode(rasterData.getRasterMode());
        try {
            map.addOrUpdateRasterLayer(layer, rasterData);
            project.addLayer(layer);
            AppContext.addLayer(layer);
            map.zoomToLayer(layer);
            CatgisDesktopApp.markProjectDirty();
        } catch (RuntimeException failure) {
            map.removeLayer(layer);
            project.removeLayer(layer);
            AppContext.refreshLayerList();
            throw failure;
        }
    }

    @Override
    public void dispose() {
        SwingWorker<?, ?> active = activeWcsWorker;
        if (active != null) {
            active.cancel(true);
        }
        super.dispose();
    }
}
