package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.core.model.Layer;

import org.locationtech.jts.geom.Envelope;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OnlineDemDownloadDialog extends JDialog {

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.ROOT);

    private final JComboBox<OnlineDemProvider> providerCombo;
    private final JComboBox<OnlineDemDatasetOption> datasetCombo;
    private final JComboBox<PublicDemDetailLevel> detailLevelCombo;
    private final JLabel apiKeyLabel;
    private final JPasswordField apiKeyField;
    private final JTextField southField;
    private final JTextField northField;
    private final JTextField westField;
    private final JTextField eastField;
    private final JTextField outputField;
    private final JLabel noteLabel;
    private final JLabel summaryLabel;

    public OnlineDemDownloadDialog(Frame owner) {
        super(owner, I18n.t("DEM online..."), true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        providerCombo = new JComboBox<>(OnlineDemProvider.values());
        datasetCombo = new JComboBox<>();
        detailLevelCombo = new JComboBox<>(PublicDemDetailLevel.values());
        apiKeyLabel = new JLabel(I18n.t("API key de OpenTopography:"));
        apiKeyField = new JPasswordField(OpenTopographySettings.getApiKey(), 28);
        southField = new JTextField(12);
        northField = new JTextField(12);
        westField = new JTextField(12);
        eastField = new JTextField(12);
        outputField = new JTextField(26);
        noteLabel = new JLabel();
        summaryLabel = new JLabel();

        providerCombo.setSelectedItem(OnlineDemProvider.PUBLIC_TERRAIN_TILES);
        detailLevelCombo.setSelectedItem(PublicDemDetailLevel.BALANCED);
        providerCombo.addActionListener(e -> refreshProviderState());
        datasetCombo.addActionListener(e -> {
            updateDatasetNote();
            outputField.setText(defaultOutputFile().getAbsolutePath());
            refreshDownloadSummary();
        });
        detailLevelCombo.addActionListener(e -> refreshDownloadSummary());
        installBboxListeners();

        refreshProviderState();
        outputField.setText(defaultOutputFile().getAbsolutePath());

        add(WindowLayoutSupport.createVerticalScrollPane(buildForm(), 760, 500), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadCurrentViewEnvelope();
        pack();
        WindowLayoutSupport.fitDialogToScreen(this, 840, 650, 740, 520);
        setLocationRelativeTo(owner);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        panel.add(new JLabel(I18n.t("Fuente DEM:")), gc);
        gc.gridy++;
        panel.add(providerCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Dataset DEM:")), gc);
        gc.gridy++;
        panel.add(datasetCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Detalle DEM publico:")), gc);
        gc.gridy++;
        panel.add(detailLevelCombo, gc);

        gc.gridy++;
        panel.add(apiKeyLabel, gc);
        gc.gridy++;
        panel.add(apiKeyField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Area en EPSG:4326 (sur, norte, oeste, este):")), gc);
        gc.gridy++;
        JPanel bboxPanel = new JPanel(new GridBagLayout());
        GridBagConstraints bc = new GridBagConstraints();
        bc.insets = new Insets(2, 2, 2, 2);
        bc.gridx = 0;
        bc.gridy = 0;
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.weightx = 1;
        bboxPanel.add(new JLabel(I18n.t("Sur")), bc);
        bc.gridx = 1;
        bboxPanel.add(new JLabel(I18n.t("Norte")), bc);
        bc.gridx = 2;
        bboxPanel.add(new JLabel(I18n.t("Oeste")), bc);
        bc.gridx = 3;
        bboxPanel.add(new JLabel(I18n.t("Este")), bc);
        bc.gridy = 1;
        bc.gridx = 0;
        bboxPanel.add(southField, bc);
        bc.gridx = 1;
        bboxPanel.add(northField, bc);
        bc.gridx = 2;
        bboxPanel.add(westField, bc);
        bc.gridx = 3;
        bboxPanel.add(eastField, bc);
        panel.add(bboxPanel, gc);

        gc.gridy++;
        JButton currentViewButton = new JButton(I18n.t("Usar vista actual"));
        currentViewButton.addActionListener(e -> {
            loadCurrentViewEnvelope();
            refreshDownloadSummary();
        });
        panel.add(currentViewButton, gc);

        gc.gridy++;
        summaryLabel.setVerticalAlignment(JLabel.TOP);
        JScrollPane summaryScrollPane = new JScrollPane(summaryLabel);
        summaryScrollPane.setBorder(BorderFactory.createTitledBorder(I18n.t("Resumen de descarga")));
        panel.add(summaryScrollPane, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Guardar DEM como:")), gc);
        gc.gridy++;
        JPanel outputPanel = new JPanel(new BorderLayout(6, 0));
        outputPanel.add(outputField, BorderLayout.CENTER);
        JButton browseButton = new JButton(I18n.t("Buscar..."));
        browseButton.addActionListener(e -> chooseOutputFile());
        outputPanel.add(browseButton, BorderLayout.EAST);
        panel.add(outputPanel, gc);

        gc.gridy++;
        noteLabel.setVerticalAlignment(JLabel.TOP);
        JScrollPane scrollPane = new JScrollPane(noteLabel);
        scrollPane.setBorder(BorderFactory.createTitledBorder(I18n.t("Nota tecnica")));
        panel.add(scrollPane, gc);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(I18n.t("Cancelar"));
        cancelButton.addActionListener(e -> dispose());

        JButton downloadButton = new JButton(I18n.t("Descargar e incorporar"));
        downloadButton.addActionListener(e -> startDownload());

        panel.add(cancelButton);
        panel.add(downloadButton);
        DialogKeyboardSupport.install(this, downloadButton, this::dispose);
        return panel;
    }

    private void refreshProviderState() {
        OnlineDemProvider provider = (OnlineDemProvider) providerCombo.getSelectedItem();
        OnlineDemDatasetOption[] options = provider != null ? provider.getDatasets() : new OnlineDemDatasetOption[0];
        datasetCombo.setModel(new DefaultComboBoxModel<>(options));

        boolean needsApiKey = provider != null && provider.requiresApiKey();
        apiKeyLabel.setVisible(needsApiKey);
        apiKeyField.setVisible(needsApiKey);
        detailLevelCombo.setEnabled(provider == OnlineDemProvider.PUBLIC_TERRAIN_TILES);

        updateDatasetNote();
        outputField.setText(defaultOutputFile().getAbsolutePath());
        refreshDownloadSummary();
        revalidate();
        repaint();
    }

    private void loadCurrentViewEnvelope() {
        try {
            Envelope current = AppContext.mapPanel() != null ? AppContext.mapPanel().getCurrentViewEnvelope() : null;
            String projectCrs = AppContext.project() != null ? AppContext.project().getProjectCRS() : "EPSG:4326";
            Envelope latLon = RasterCoverageSupport.reprojectEnvelope(current, projectCrs, "EPSG:4326");
            if (latLon == null) {
                return;
            }
            southField.setText(formatCoord(latLon.getMinY()));
            northField.setText(formatCoord(latLon.getMaxY()));
            westField.setText(formatCoord(latLon.getMinX()));
            eastField.setText(formatCoord(latLon.getMaxX()));
            refreshDownloadSummary();
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo leer la vista actual para el DEM online", ex);
            showDemError(I18n.t("No se pudo leer la vista actual para el DEM online."), ex);
        }
    }

    private void chooseOutputFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("dem-online-output", I18n.t("Guardar DEM online como"));
        chooser.setSelectedFile(defaultOutputFile());
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null && !file.getName().toLowerCase(Locale.ROOT).endsWith(".tif")) {
                File parent = file.getParentFile();
                file = parent != null ? new File(parent, file.getName() + ".tif") : new File(file.getName() + ".tif");
            }
            outputField.setText(file != null ? file.getAbsolutePath() : "");
            FileChooserSupport.rememberFile("dem-online-output", file);
        }
    }

    private void startDownload() {
        OnlineDemProvider provider = (OnlineDemProvider) providerCombo.getSelectedItem();
        OnlineDemDatasetOption dataset = (OnlineDemDatasetOption) datasetCombo.getSelectedItem();
        PublicDemDetailLevel detailLevel = (PublicDemDetailLevel) detailLevelCombo.getSelectedItem();
        String apiKey = new String(apiKeyField.getPassword()).trim();
        String outputText = outputField.getText().trim();
        if (provider == null || dataset == null) {
            NotificationManager.warn(this, null, I18n.t("Debes elegir una fuente y un dataset DEM."));
            return;
        }
        if (outputText.isBlank()) {
            NotificationManager.warn(this, null, I18n.t("Debes indicar un archivo de salida para el DEM."));
            return;
        }
        File outputFile = new File(outputText);

        double south;
        double north;
        double west;
        double east;
        try {
            south = parseCoord(southField.getText());
            north = parseCoord(northField.getText());
            west = parseCoord(westField.getText());
            east = parseCoord(eastField.getText());
        } catch (Exception ex) {
            NotificationManager.warn(this, null, I18n.t("Revisa el area del DEM online. Los cuatro valores deben ser numericos y validos."));
            return;
        }

        Envelope bbox = new Envelope(west, east, south, north);
        if (bbox.isNull() || north <= south || east <= west) {
            NotificationManager.warn(this, null, I18n.t("El area del DEM online no es valida. Verifica sur/norte/oeste/este."));
            return;
        }

        if (provider.requiresApiKey() && apiKey.isBlank()) {
            NotificationManager.warn(this, null, I18n.t("Debes ingresar una API key de OpenTopography para descargar el DEM."));
            return;
        }
        if (provider.requiresApiKey()) {
            OpenTopographySettings.setApiKey(apiKey);
        }

        if (provider == OnlineDemProvider.PUBLIC_TERRAIN_TILES) {
            try {
                PublicTerrainTilesDemService.PlanSummary summary = PublicTerrainTilesDemService.estimatePlan(bbox, detailLevel);
                if (summary.tileCount() > 42) {
                    NotificationManager.warn(this, null, I18n.t("El area elegida sigue siendo demasiado grande para el DEM publico. Acerca la vista o usa un area mas chica."));
                    return;
                }
            } catch (Exception ex) {
                AppErrorSupport.logFailure("No se pudo estimar la descarga DEM publica", ex);
                showDemError(I18n.t("No se pudo estimar la descarga DEM publica."), ex);
                return;
            }
        }

        new SwingWorker<DownloadedRasterResult, Void>() {
            @Override
            protected DownloadedRasterResult doInBackground() throws Exception {
                return switch (provider) {
                    case PUBLIC_TERRAIN_TILES -> {
                        TerrainTilesDataset terrainDataset = (TerrainTilesDataset) dataset;
                        PublicTerrainTilesDemService.FileDownloadResult result =
                                PublicTerrainTilesDemService.download(terrainDataset, bbox, detailLevel, outputFile);
                        yield new DownloadedRasterResult(result.file(), terrainDataset.getDisplayName(), terrainDataset.getSourceLabel(), terrainDataset.getSourceCrsCode());
                    }
                    case OPEN_TOPOGRAPHY -> {
                        OpenTopographyDataset openTopographyDataset = (OpenTopographyDataset) dataset;
                        OpenTopographyDemService.FileDownloadResult result =
                                OpenTopographyDemService.download(openTopographyDataset, bbox, "EPSG:4326", apiKey, outputFile);
                        yield new DownloadedRasterResult(result.file(), openTopographyDataset.getDisplayName(), openTopographyDataset.getSourceLabel(), openTopographyDataset.getSourceCrsCode());
                    }
                };
            }

            @Override
            protected void done() {
                try {
                    DownloadedRasterResult result = get();
                    addRasterLayer(result.file(), result.displayName(), result.sourceLabel(), result.sourceCrsCode());
                    dispose();
                } catch (Exception ex) {
                    AppErrorSupport.logFailure("No se pudo descargar el DEM online", ex);
                    showDemError(I18n.t("No se pudo descargar el DEM online."), ex);
                }
            }
        }.execute();
    }

    private void showDemError(String intro, Throwable ex) {
        AppErrorSupport.showErrorDialog(this, I18n.t("DEM online"), intro, ex);
    }

    private void addRasterLayer(File file, String datasetName, String sourceLabel, String sourceCrsCode) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException(I18n.t("El archivo descargado del DEM no existe."));
        }
        if (AppContext.project() == null) {
            AppContext.setCurrentProject(new Project(I18n.t("Proyecto actual")));
        }

        String layerName = datasetName + " - " + file.getName();
        RasterLayer layer = new RasterLayer(layerName, file.getAbsolutePath());
        layer.setSourceName(sourceLabel);

        String projectCrs = AppContext.project().getProjectCRS() != null && !AppContext.project().getProjectCRS().isBlank()
                ? AppContext.project().getProjectCRS()
                : sourceCrsCode;
        LocalRasterData rasterData = RasterImageLoader.loadReal(file, projectCrs, sourceCrsCode);
        layer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(rasterData, projectCrs));

        AppContext.project().addLayer(layer);
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.addLayer(layer);
            AppContext.selectLayer(layer);
        }
        if (AppContext.mapPanel() != null) {
            AppContext.mapPanel().addOrUpdateRasterLayer(layer, rasterData);
            AppContext.mapPanel().showOpenedFile(layer.getName());
            AppContext.mapPanel().zoomToLayer(layer);
        }
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(I18n.t("DEM online incorporado: ") + layer.getName());
        }
    }

    private void updateDatasetNote() {
        OnlineDemProvider provider = (OnlineDemProvider) providerCombo.getSelectedItem();
        OnlineDemDatasetOption dataset = (OnlineDemDatasetOption) datasetCombo.getSelectedItem();
        if (provider == null || dataset == null) {
            noteLabel.setText("");
            return;
        }

        String html;
        if (provider == OnlineDemProvider.PUBLIC_TERRAIN_TILES) {
            html = "<html><div style='padding:8px;width:540px'>"
                    + "<b>" + dataset.getDisplayName() + "</b><br>"
                    + I18n.t("Fuente operativa en esta ronda:") + " " + dataset.getSourceLabel() + "<br><br>"
                    + I18n.t("No requiere API key. CATGIS descarga tiles publicos de elevacion, arma un GeoTIFF DEM local y lo incorpora listo para curvas, perfiles e hidrologia base.") + "<br><br>"
                    + I18n.t("El GeoTIFF fuente se descarga en EPSG:3857, pero CATGIS lo incorpora para trabajar en el CRS operativo del proyecto actual.") + "<br><br>"
                    + I18n.t("Esta opcion prioriza simplicidad operativa y acceso sin clave. OpenTopography sigue disponible como alternativa global avanzada cuando necesites sus datasets especificos.")
                    + "</div></html>";
        } else {
            html = "<html><div style='padding:8px;width:540px'>"
                    + "<b>" + dataset.getDisplayName() + "</b><br>"
                    + I18n.t("Fuente operativa en esta ronda:") + " " + dataset.getSourceLabel() + "<br><br>"
                    + I18n.t("Esta descarga usa la API global de OpenTopography y guarda un GeoTIFF local listo para incorporar al proyecto.") + "<br><br>"
                    + I18n.t("Conviene para datasets globales concretos o cuando necesites una fuente DEM distinta a Terrain Tiles.") + "<br><br>"
                    + I18n.t("La clave se mantiene como configuracion propia del usuario. No se embebe en CATGIS porque no es una buena practica para distribucion general.")
                    + "</div></html>";
        }
        noteLabel.setText(html);
    }

    private void refreshDownloadSummary() {
        OnlineDemProvider provider = (OnlineDemProvider) providerCombo.getSelectedItem();
        if (provider == null) {
            summaryLabel.setText("");
            return;
        }

        Envelope bbox = parseEnvelopeOrNull();
        if (bbox == null) {
            summaryLabel.setText("<html><div style='padding:8px;width:520px'>"
                    + I18n.t("Define un area valida para calcular el resumen de descarga del DEM.")
                    + "</div></html>");
            return;
        }

        if (provider == OnlineDemProvider.PUBLIC_TERRAIN_TILES) {
            try {
                PublicDemDetailLevel detailLevel = (PublicDemDetailLevel) detailLevelCombo.getSelectedItem();
                PublicTerrainTilesDemService.PlanSummary summary = PublicTerrainTilesDemService.estimatePlan(bbox, detailLevel);
                String warning = "";
                if (summary.nearTileLimit() || summary.nearOutputLimit()) {
                    warning = "<br><br><b>" + I18n.t("Aviso:") + "</b> " + I18n.t("El area ya esta cerca del limite practico del flujo DEM publico. Si el resultado queda pobre, recorta mas la vista o baja el detalle.");
                }
                summaryLabel.setText("<html><div style='padding:8px;width:520px'>"
                        + "<b>" + I18n.t("Fuente:") + "</b> Terrain Tiles / Terrarium<br>"
                        + "<b>" + I18n.t("Nivel de detalle:") + "</b> " + detailLevel + "<br>"
                        + "<b>" + I18n.t("Zoom estimado:") + "</b> " + summary.zoom() + "<br>"
                        + "<b>" + I18n.t("Tiles estimados:") + "</b> " + summary.tileCount() + "<br>"
                        + "<b>" + I18n.t("Tamano DEM estimado:") + "</b> " + summary.width() + " x " + summary.height() + " px<br>"
                        + "<b>" + I18n.t("Resolucion aproximada:") + "</b> " + String.format(Locale.US, "%.1f m/pixel", summary.estimatedResolutionMeters()) + "<br>"
                        + "<b>" + I18n.t("CRS de salida:") + "</b> EPSG:3857"
                        + warning
                        + "</div></html>");
            } catch (Exception ex) {
                summaryLabel.setText("<html><div style='padding:8px;width:520px'><b>"
                        + I18n.t("No se pudo estimar la descarga DEM publica.")
                        + "</b><br>" + ex.getMessage() + "</div></html>");
            }
            return;
        }

        summaryLabel.setText("<html><div style='padding:8px;width:520px'>"
                + "<b>" + I18n.t("Fuente:") + "</b> OpenTopography<br>"
                + I18n.t("Esta fuente prioriza datasets globales avanzados. En esta ronda el detalle exacto depende del dataset elegido y del servicio remoto.") + "<br>"
                + "<b>" + I18n.t("Formato de salida:") + "</b> GeoTIFF"
                + "</div></html>");
    }

    private Envelope parseEnvelopeOrNull() {
        try {
            double south = parseCoord(southField.getText());
            double north = parseCoord(northField.getText());
            double west = parseCoord(westField.getText());
            double east = parseCoord(eastField.getText());
            Envelope bbox = new Envelope(west, east, south, north);
            if (bbox.isNull() || north <= south || east <= west) {
                return null;
            }
            return bbox;
        } catch (Exception ex) {
            return null;
        }
    }

    private void installBboxListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshDownloadSummary();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshDownloadSummary();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshDownloadSummary();
            }
        };
        southField.getDocument().addDocumentListener(listener);
        northField.getDocument().addDocumentListener(listener);
        westField.getDocument().addDocumentListener(listener);
        eastField.getDocument().addDocumentListener(listener);
    }

    private static String describeThrowable(Throwable throwable) {
        Throwable current = throwable;
        Throwable deepest = throwable;
        while (current != null) {
            deepest = current;
            current = current.getCause();
        }
        if (deepest == null) {
            return I18n.t("Error sin detalle disponible.");
        }
        String message = deepest.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }
        return deepest.getClass().getSimpleName();
    }

    private File defaultOutputFile() {
        OnlineDemDatasetOption dataset = (OnlineDemDatasetOption) datasetCombo.getSelectedItem();
        String datasetCode = dataset != null ? dataset.getOutputCode().toLowerCase(Locale.ROOT) : "dem";
        return FileChooserSupport.resolveSuggestedFile(
                "dem-online-output",
                new File(datasetCode + "_" + FILE_STAMP.format(LocalDateTime.now()) + ".tif")
        );
    }

    private static String formatCoord(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private static double parseCoord(String text) {
        return Double.parseDouble(text.trim().replace(",", "."));
    }

    public static void open() {
        Frame owner = JOptionPane.getFrameForComponent(CatgisDesktopApp.getMainFrameSafe());
        OnlineDemDownloadDialog dialog = new OnlineDemDownloadDialog(owner);
        dialog.setVisible(true);
    }

    private record DownloadedRasterResult(File file, String displayName, String sourceLabel, String sourceCrsCode) {
    }
}
