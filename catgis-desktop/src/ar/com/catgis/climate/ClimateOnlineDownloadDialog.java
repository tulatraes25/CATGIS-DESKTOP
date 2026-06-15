package ar.com.catgis.climate;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;

import ar.com.catgis.AppContext;
import ar.com.catgis.AppErrorSupport;
import ar.com.catgis.AppIcons;
import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.CatgisLogger;
import ar.com.catgis.DialogKeyboardSupport;
import ar.com.catgis.FileChooserSupport;
import ar.com.catgis.I18n;
import ar.com.catgis.NotificationManager;
import ar.com.catgis.RasterImageLoader;
import ar.com.catgis.RasterLayer;
import ar.com.catgis.WindowLayoutSupport;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dialog for downloading climate data from online providers.
 * Follows the exact same UI pattern as {@link OnlineDemDownloadDialog} and {@link OnlineSoilDownloadDialog}.
 */
public class ClimateOnlineDownloadDialog extends JDialog {

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.ROOT);

    private final JComboBox<OnlineClimateProvider> providerCombo;
    private final JComboBox<ClimateDatasetOption> datasetCombo;
    private final JLabel apiKeyLabel;
    private final JPasswordField apiKeyField;
    private final JTextField southField;
    private final JTextField northField;
    private final JTextField westField;
    private final JTextField eastField;
    private final JTextField outputField;
    private final JLabel noteLabel;
    private final JLabel summaryLabel;

    // Open-Meteo date range fields
    private final JLabel startDateLabel;
    private final JTextField startDateField;
    private final JLabel endDateLabel;
    private final JTextField endDateField;

    // Period presets
    private final JComboBox<String> periodPresetCombo;
    private static final String[] PERIOD_PRESETS = {
        "Personalizado", "Último mes", "Último año", "Normal climática"
    };

    public ClimateOnlineDownloadDialog(Frame owner) {
        super(owner, I18n.t("Clima online..."), true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        providerCombo = new JComboBox<>(OnlineClimateProvider.values());
        datasetCombo = new JComboBox<>();
        apiKeyLabel = new JLabel(I18n.t("API key:"));
        apiKeyField = new JPasswordField(ClimateSettings.getWorldCleanApiKey(), 28);
        southField = new JTextField(12);
        northField = new JTextField(12);
        westField = new JTextField(12);
        eastField = new JTextField(12);
        outputField = new JTextField(26);
        noteLabel = new JLabel();
        summaryLabel = new JLabel();
        startDateLabel = new JLabel(I18n.t("Fecha inicio (YYYY-MM-DD):"));
        startDateField = new JTextField(12);
        endDateLabel = new JLabel(I18n.t("Fecha fin (YYYY-MM-DD):"));
        endDateField = new JTextField(12);

        // Period preset combo
        periodPresetCombo = new JComboBox<>(PERIOD_PRESETS);
        periodPresetCombo.addActionListener(e -> updatePeriodFromPreset());

        // Default date range: last 30 days
        LocalDate defaultEnd = LocalDate.now().minusDays(1);
        LocalDate defaultStart = defaultEnd.minusDays(30);
        startDateField.setText(defaultStart.format(DateTimeFormatter.ISO_LOCAL_DATE));
        endDateField.setText(defaultEnd.format(DateTimeFormatter.ISO_LOCAL_DATE));

        providerCombo.setSelectedItem(OnlineClimateProvider.WORLDCLIM);
        providerCombo.addActionListener(e -> refreshProviderState());
        datasetCombo.addActionListener(e -> {
            updateDatasetNote();
            outputField.setText(defaultOutputFile().getAbsolutePath());
            refreshDownloadSummary();
        });
        installBboxListeners();

        refreshProviderState();
        outputField.setText(defaultOutputFile().getAbsolutePath());

        add(WindowLayoutSupport.createVerticalScrollPane(buildForm(), 760, 500), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadCurrentViewEnvelope();
        pack();
        WindowLayoutSupport.fitDialogToScreen(this, 840, 680, 740, 560);
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

        panel.add(new JLabel(I18n.t("Fuente climática:")), gc);
        gc.gridy++;
        panel.add(providerCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Variable climática:")), gc);
        gc.gridy++;
        panel.add(datasetCombo, gc);

        gc.gridy++;
        panel.add(apiKeyLabel, gc);
        gc.gridy++;
        panel.add(apiKeyField, gc);

        // Period preset
        gc.gridy++;
        panel.add(new JLabel("Período:"), gc);
        gc.gridy++;
        panel.add(periodPresetCombo, gc);

        // Open-Meteo date range
        gc.gridy++;
        panel.add(startDateLabel, gc);
        gc.gridy++;
        panel.add(startDateField, gc);
        gc.gridy++;
        panel.add(endDateLabel, gc);
        gc.gridy++;
        panel.add(endDateField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Área en EPSG:4326 (sur, norte, oeste, este):")), gc);
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
        panel.add(new JLabel(I18n.t("Guardar datos climáticos como:")), gc);
        gc.gridy++;
        JPanel outputPanel = new JPanel(new BorderLayout(6, 0));
        outputPanel.add(outputField, BorderLayout.CENTER);
        JButton browseButton = new JButton(I18n.t("Buscar..."));
        browseButton.addActionListener(e -> chooseOutputFile());
        outputPanel.add(browseButton, BorderLayout.EAST);
        panel.add(outputPanel, gc);

        gc.gridy++;
        noteLabel.setVerticalAlignment(JLabel.TOP);
        JScrollPane noteScrollPane = new JScrollPane(noteLabel);
        noteScrollPane.setBorder(BorderFactory.createTitledBorder(I18n.t("Nota técnica")));
        panel.add(noteScrollPane, gc);

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
        OnlineClimateProvider provider = (OnlineClimateProvider) providerCombo.getSelectedItem();
        ClimateDatasetOption[] options = provider != null ? provider.getDatasets() : new ClimateDatasetOption[0];
        datasetCombo.setModel(new DefaultComboBoxModel<>(options));

        boolean needsApiKey = provider != null && provider.requiresApiKey();
        apiKeyLabel.setVisible(needsApiKey);
        apiKeyField.setVisible(needsApiKey);

        // Date fields: visible for Open-Meteo (time series), hidden for WorldClim (static climatology)
        boolean isOpenMeteo = provider == OnlineClimateProvider.OPEN_METEO;
        startDateLabel.setVisible(isOpenMeteo);
        startDateField.setVisible(isOpenMeteo);
        endDateLabel.setVisible(isOpenMeteo);
        endDateField.setVisible(isOpenMeteo);

        // Preset: show all options for Open-Meteo, only "Personalizado" for WorldClim (static)
        periodPresetCombo.removeAllItems();
        if (isOpenMeteo) {
            for (String p : PERIOD_PRESETS) periodPresetCombo.addItem(p);
        } else {
            periodPresetCombo.addItem("Personalizado");
        }
        periodPresetCombo.setSelectedItem("Personalizado");

        updateDatasetNote();
        outputField.setText(defaultOutputFile().getAbsolutePath());
        refreshDownloadSummary();
        revalidate();
        repaint();
    }

    /**
     * Update date fields based on the selected period preset.
     */
    private void updatePeriodFromPreset() {
        String preset = (String) periodPresetCombo.getSelectedItem();
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDate start;
        boolean showDates;

        switch (preset != null ? preset : "Personalizado") {
            case "Último mes":
                start = end.minusDays(30);
                showDates = false;
                break;
            case "Último año":
                start = end.minusDays(365);
                showDates = false;
                break;
            case "Normal climática":
                start = LocalDate.of(1970, 1, 1);
                end = LocalDate.of(2000, 12, 31);
                showDates = false;
                break;
            default: // Personalizado
                start = end.minusDays(30);
                showDates = true;
                break;
        }

        startDateField.setText(start.format(DateTimeFormatter.ISO_LOCAL_DATE));
        endDateField.setText(end.format(DateTimeFormatter.ISO_LOCAL_DATE));
        startDateLabel.setVisible(showDates);
        startDateField.setVisible(showDates);
        endDateLabel.setVisible(showDates);
        endDateField.setVisible(showDates);

        refreshDownloadSummary();
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
            AppErrorSupport.logFailure("No se pudo leer la vista actual para clima online", ex);
            showClimateError(I18n.t("No se pudo leer la vista actual para clima online."), ex);
        }
    }

    private void chooseOutputFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("climate-online-output", I18n.t("Guardar datos climáticos como"));
        chooser.setSelectedFile(defaultOutputFile());
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null && !file.getName().toLowerCase(Locale.ROOT).endsWith(".tif")) {
                File parent = file.getParentFile();
                file = parent != null ? new File(parent, file.getName() + ".tif") : new File(file.getName() + ".tif");
            }
            outputField.setText(file != null ? file.getAbsolutePath() : "");
            FileChooserSupport.rememberFile("climate-online-output", file);
        }
    }

    private void startDownload() {
        OnlineClimateProvider provider = (OnlineClimateProvider) providerCombo.getSelectedItem();
        ClimateDatasetOption dataset = (ClimateDatasetOption) datasetCombo.getSelectedItem();
        String apiKey = new String(apiKeyField.getPassword()).trim();
        String outputText = outputField.getText().trim();

        if (provider == null || dataset == null) {
            NotificationManager.warn(this, null, I18n.t("Debes elegir una fuente y una variable climática."));
            return;
        }
        if (provider == OnlineClimateProvider.WORLDCLIM) {
            NotificationManager.warn(this, I18n.t("WorldClim no disponible"),
                    I18n.t("El servidor de WorldClim (biogeo.ucar.edu) no responde. "
                            + "Use Open-Meteo como alternativa gratuita."));
            CatgisLogger.warn("Clima online: WorldClim bloqueado (servidor caído)", null);
            return;
        }
        if (outputText.isBlank()) {
            NotificationManager.warn(this, null, I18n.t("Debes indicar un archivo de salida para los datos climáticos."));
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
            NotificationManager.warn(this, null, I18n.t("Revisa el área climática. Los cuatro valores deben ser numéricos y válidos."));
            return;
        }

        Envelope bbox = new Envelope(west, east, south, north);
        if (bbox.isNull() || north <= south || east <= west) {
            NotificationManager.warn(this, null, I18n.t("El área de clima no es válida. Verifica sur/norte/oeste/este."));
            return;
        }

        // Parse dates for Open-Meteo (must be effectively final for inner class)
        final LocalDate[] startDate = {null};
        final LocalDate[] endDate = {null};
        if (provider == OnlineClimateProvider.OPEN_METEO) {
            try {
                startDate[0] = LocalDate.parse(startDateField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                endDate[0] = LocalDate.parse(endDateField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                if (startDate[0].isAfter(endDate[0])) {
                    NotificationManager.warn(this, null, I18n.t("La fecha de inicio debe ser anterior a la fecha de fin."));
                    return;
                }
            } catch (Exception ex) {
                NotificationManager.warn(this, null, I18n.t("Las fechas deben estar en formato YYYY-MM-DD (ej: 2024-01-01)."));
                return;
            }
        }

        if (provider.requiresApiKey() && apiKey.isBlank()) {
            NotificationManager.warn(this, null, I18n.t("Esta fuente requiere una API key."));
            return;
        }
        if (provider.requiresApiKey()) {
            ClimateSettings.setWorldCleanApiKey(apiKey);
        }

        new SwingWorker<DownloadedClimateResult, Void>() {
            @Override
            protected DownloadedClimateResult doInBackground() throws Exception {
                ClimateOnlineDownloadService.FileDownloadResult result =
                        ClimateOnlineDownloadService.download(dataset, bbox, outputFile, startDate[0], endDate[0]);
                return new DownloadedClimateResult(
                        result.file(),
                        dataset.getDisplayName(),
                        dataset.getSourceLabel(),
                        dataset.getSourceCrsCode(),
                        dataset
                );
            }

            @Override
            protected void done() {
                try {
                    DownloadedClimateResult result = get();
                    addRasterLayer(result.file(), result.displayName(), result.sourceLabel(), result.sourceCrsCode(), result.dataset());
                    dispose();
                } catch (Exception ex) {
                    AppErrorSupport.logFailure("No se pudieron descargar los datos climáticos online", ex);
                    showClimateError(I18n.t("No se pudieron descargar los datos climáticos."), ex);
                }
            }
        }.execute();
    }

    private void showClimateError(String intro, Throwable ex) {
        AppErrorSupport.showErrorDialog(this, I18n.t("Clima online"), intro, ex);
    }

    private Layer findLayerByName(String name) {
        if (AppContext.project() == null) return null;
        for (Layer l : AppContext.project().getLayers()) {
            if (name.equals(l.getName())) return l;
        }
        return null;
    }

    private void addRasterLayer(File file, String datasetName, String sourceLabel, String sourceCrsCode,
                                ClimateDatasetOption dataset) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException(I18n.t("El archivo descargado de clima no existe."));
        }
        if (AppContext.project() == null) {
            AppContext.setCurrentProject(new Project(I18n.t("Proyecto actual")));
        }

        // Build unique layer name with timestamp to avoid duplicates
        String providerLabel = sourceLabel != null ? " — " + sourceLabel : "";
        String layerName = datasetName + providerLabel;

        // Remove any existing layer with the same name (replace on re-download)
        Layer existing = findLayerByName(layerName);
        if (existing != null) {
            AppContext.project().removeLayer(existing);
            CatgisLogger.debug("Clima online: reemplazando capa existente '" + layerName + "'");
        }

        RasterLayer layer = new RasterLayer(layerName, file.getAbsolutePath());
        layer.setSourceName(sourceLabel);

        // Store climate metadata in layer user data
        layer.putUserData("climateVariable", dataset.getApiVariableCode());
        layer.putUserData("climateSource", dataset.getSourceLabel());
        layer.putUserData("climateDataset", dataset.getOutputCode());

        // Store period metadata
        String start = startDateField.getText().trim();
        String end = endDateField.getText().trim();
        layer.putUserData("climatePeriodStart", start);
        layer.putUserData("climatePeriodEnd", end);
        String preset = (String) periodPresetCombo.getSelectedItem();
        String periodLabel = preset != null && !"Personalizado".equals(preset) ? preset : start + " a " + end;
        layer.putUserData("climatePeriodLabel", periodLabel);
        layer.putUserData("climateAggregation", "mensual");

        String projectCrs = AppContext.project().getProjectCRS() != null && !AppContext.project().getProjectCRS().isBlank()
                ? AppContext.project().getProjectCRS()
                : sourceCrsCode;
        LocalRasterData rasterData = RasterImageLoader.loadReal(file, projectCrs, sourceCrsCode);
        String layerOperationalCrs = CRSDefinitions.normalizeCode(rasterData.getDisplayCRS());
        if (layerOperationalCrs.isBlank()) {
            layerOperationalCrs = RasterCoverageSupport.resolveOperationalRasterCrs(rasterData, projectCrs);
        }
        layer.setSourceCRS(layerOperationalCrs);

        CatgisLogger.debug("Clima online: capa=" + layerName
                + " CRS=" + layerOperationalCrs
                + " bands=" + rasterData.getBandCount()
                + " env=" + rasterData.getEnvelope()
                + " pixels=" + rasterData.getImage().getWidth() + "x" + rasterData.getImage().getHeight());

        AppContext.project().addLayer(layer);
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.addLayer(layer);
            AppContext.selectLayer(layer);
        }
        if (AppContext.mapPanel() != null) {
            AppContext.mapPanel().addOrUpdateRasterLayer(layer, rasterData);
            AppContext.mapPanel().showOpenedFile(layer.getName());
            AppContext.mapPanel().zoomToLayer(layer);
            AppContext.mapPanel().repaint();
        }
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(I18n.t("Capa climática incorporada: ") + layer.getName());
        }
    }

    private void updateDatasetNote() {
        OnlineClimateProvider provider = (OnlineClimateProvider) providerCombo.getSelectedItem();
        ClimateDatasetOption dataset = (ClimateDatasetOption) datasetCombo.getSelectedItem();
        if (provider == null || dataset == null) {
            noteLabel.setText("");
            return;
        }

        String html;
        if (provider == OnlineClimateProvider.WORLDCLIM) {
            html = "<html><div style='padding:8px;width:560px'>"
                    + "<b>" + dataset.getDisplayName() + "</b><br>"
                    + I18n.t("Fuente:") + " " + dataset.getSourceLabel() + "<br><br>"
                    + dataset.getTechnicalSummary() + "<br><br>"
                    + I18n.t("WorldClim v2.1 provee climatologías históricas (1970-2000) a 30 arcseg (~1 km). CATGIS descarga el tile global y recorta el área elegida.") + "<br><br>"
                    + I18n.t("No requiere API key. Los datos son de acceso público bajo la licencia de WorldClim.")
                    + "</div></html>";
        } else if (provider == OnlineClimateProvider.OPEN_METEO) {
            html = "<html><div style='padding:8px;width:560px'>"
                    + "<b>" + dataset.getDisplayName() + "</b><br>"
                    + I18n.t("Fuente:") + " " + dataset.getSourceLabel() + "<br><br>"
                    + dataset.getTechnicalSummary() + "<br><br>"
                    + I18n.t("Open-Meteo provee datos históricos de estaciones meteorológicas. CATGIS consulta puntos de grilla y construye un raster interpolando los resultados.") + "<br><br>"
                    + I18n.t("No requiere API key. Los datos son de uso gratuito bajo los términos de Open-Meteo.")
                    + "</div></html>";
        } else {
            html = "<html><div style='padding:8px;width:560px'>"
                    + "<b>" + dataset.getDisplayName() + "</b><br>"
                    + I18n.t("Fuente:") + " " + dataset.getSourceLabel() + "<br><br>"
                    + dataset.getTechnicalSummary()
                    + "</div></html>";
        }
        noteLabel.setText(html);
    }

    private void refreshDownloadSummary() {
        OnlineClimateProvider provider = (OnlineClimateProvider) providerCombo.getSelectedItem();
        ClimateDatasetOption dataset = (ClimateDatasetOption) datasetCombo.getSelectedItem();
        if (provider == null || dataset == null) {
            summaryLabel.setText("");
            return;
        }

        Envelope bbox = parseEnvelopeOrNull();
        if (bbox == null) {
            summaryLabel.setText("<html><div style='padding:8px;width:520px'>"
                    + I18n.t("Define un área válida para calcular el resumen de descarga climática.")
                    + "</div></html>");
            return;
        }

        String projectCrs = AppContext.project() != null && AppContext.project().getProjectCRS() != null
                && !AppContext.project().getProjectCRS().isBlank()
                ? AppContext.project().getProjectCRS()
                : "EPSG:4326";

        StringBuilder sb = new StringBuilder();
        sb.append("<html><div style='padding:8px;width:520px'>");
        sb.append("<b>").append(I18n.t("Fuente:")).append("</b> ").append(dataset.getSourceLabel()).append("<br>");
        sb.append("<b>").append(I18n.t("Variable:")).append("</b> ").append(dataset.getDisplayName()).append("<br>");
        sb.append("<b>").append(I18n.t("CRS de descarga:")).append("</b> ").append(dataset.getSourceCrsCode()).append("<br>");
        sb.append("<b>").append(I18n.t("CRS operativo del proyecto:")).append("</b> ").append(projectCrs).append("<br>");

        // Period info for all providers
        String preset = (String) periodPresetCombo.getSelectedItem();
        sb.append("<b>Preset de período:</b> ").append(preset != null ? preset : "Personalizado").append("<br>");
        String start = startDateField.getText().trim();
        String end = endDateField.getText().trim();
        if (!start.isEmpty() && !end.isEmpty()) {
            if (provider == OnlineClimateProvider.WORLDCLIM) {
                sb.append("<b>Climatología:</b> ").append(start).append(" a ").append(end).append("<br>");
            } else {
                sb.append("<b>Período:</b> ").append(start).append(" a ").append(end).append("<br>");
            }
        }

        sb.append("<br>");
        if (provider == OnlineClimateProvider.WORLDCLIM) {
            sb.append(I18n.t("CATGIS descarga el tile global de WorldClim y lo recorta al área elegida."));
        } else if (provider == OnlineClimateProvider.OPEN_METEO) {
            sb.append(I18n.t("CATGIS consulta la API histórica de Open-Meteo en puntos de grilla y construye un raster interpolado."));
        }
        sb.append("</div></html>");

        summaryLabel.setText(sb.toString());
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
        ClimateDatasetOption dataset = (ClimateDatasetOption) datasetCombo.getSelectedItem();
        String datasetCode = dataset != null ? dataset.getOutputCode().toLowerCase(Locale.ROOT) : "climate";
        return FileChooserSupport.resolveSuggestedFile(
                "climate-online-output",
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
        ClimateOnlineDownloadDialog dialog = new ClimateOnlineDownloadDialog(owner);
        dialog.setVisible(true);
    }

    private record DownloadedClimateResult(File file,
                                           String displayName,
                                           String sourceLabel,
                                           String sourceCrsCode,
                                           ClimateDatasetOption dataset) {
    }
}