package ar.com.catgis;

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

public class OnlineSoilDownloadDialog extends JDialog {

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.ROOT);

    private final JComboBox<OnlineSoilProvider> providerCombo;
    private final JComboBox<OnlineSoilDatasetOption> datasetCombo;
    private final JTextField southField;
    private final JTextField northField;
    private final JTextField westField;
    private final JTextField eastField;
    private final JTextField outputField;
    private final JLabel noteLabel;
    private final JLabel summaryLabel;

    public OnlineSoilDownloadDialog(Frame owner) {
        super(owner, I18n.t("Suelos online..."), true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        providerCombo = new JComboBox<>(OnlineSoilProvider.values());
        datasetCombo = new JComboBox<>();
        southField = new JTextField(12);
        northField = new JTextField(12);
        westField = new JTextField(12);
        eastField = new JTextField(12);
        outputField = new JTextField(26);
        noteLabel = new JLabel();
        summaryLabel = new JLabel();

        providerCombo.setSelectedItem(OnlineSoilProvider.SOILGRIDS);
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
        WindowLayoutSupport.fitDialogToScreen(this, 840, 640, 740, 520);
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

        panel.add(new JLabel(I18n.t("Fuente de suelos:")), gc);
        gc.gridy++;
        panel.add(providerCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Dataset de suelos:")), gc);
        gc.gridy++;
        panel.add(datasetCombo, gc);

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
        panel.add(new JLabel(I18n.t("Guardar mapa de suelos como:")), gc);
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
        noteScrollPane.setBorder(BorderFactory.createTitledBorder(I18n.t("Nota tecnica")));
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
        OnlineSoilProvider provider = (OnlineSoilProvider) providerCombo.getSelectedItem();
        OnlineSoilDatasetOption[] options = provider != null ? provider.getDatasets() : new OnlineSoilDatasetOption[0];
        datasetCombo.setModel(new DefaultComboBoxModel<>(options));
        updateDatasetNote();
        outputField.setText(defaultOutputFile().getAbsolutePath());
        refreshDownloadSummary();
        revalidate();
        repaint();
    }

    private void loadCurrentViewEnvelope() {
        try {
            Envelope current = CatgisDesktopApp.mapPanel != null ? CatgisDesktopApp.mapPanel.getCurrentViewEnvelope() : null;
            String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "EPSG:4326";
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
            AppErrorSupport.logFailure("No se pudo leer la vista actual para suelos online", ex);
            showSoilError(I18n.t("No se pudo leer la vista actual para suelos online."), ex);
        }
    }

    private void chooseOutputFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("soil-online-output", I18n.t("Guardar mapa de suelos como"));
        chooser.setSelectedFile(defaultOutputFile());
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null && !file.getName().toLowerCase(Locale.ROOT).endsWith(".tif")) {
                File parent = file.getParentFile();
                file = parent != null ? new File(parent, file.getName() + ".tif") : new File(file.getName() + ".tif");
            }
            outputField.setText(file != null ? file.getAbsolutePath() : "");
            FileChooserSupport.rememberFile("soil-online-output", file);
        }
    }

    private void startDownload() {
        OnlineSoilProvider provider = (OnlineSoilProvider) providerCombo.getSelectedItem();
        OnlineSoilDatasetOption dataset = (OnlineSoilDatasetOption) datasetCombo.getSelectedItem();
        String outputText = outputField.getText().trim();
        if (provider == null || dataset == null) {
            JOptionPane.showMessageDialog(this, I18n.t("Debes elegir una fuente y un dataset de suelos."));
            return;
        }
        if (outputText.isBlank()) {
            JOptionPane.showMessageDialog(this, I18n.t("Debes indicar un archivo de salida para el mapa de suelos."));
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
            JOptionPane.showMessageDialog(this, I18n.t("Revisa el area de suelos online. Los cuatro valores deben ser numericos y validos."));
            return;
        }

        Envelope bbox = new Envelope(west, east, south, north);
        if (bbox.isNull() || north <= south || east <= west) {
            JOptionPane.showMessageDialog(this, I18n.t("El area del mapa de suelos no es valida. Verifica sur/norte/oeste/este."));
            return;
        }

        new SwingWorker<DownloadedSoilResult, Void>() {
            @Override
            protected DownloadedSoilResult doInBackground() throws Exception {
                return switch (provider) {
                    case SOILGRIDS -> {
                        SoilGridsDataset soilDataset = (SoilGridsDataset) dataset;
                        SoilGridsDownloadService.FileDownloadResult result =
                                SoilGridsDownloadService.download(soilDataset, bbox, "EPSG:4326", outputFile);
                        yield new DownloadedSoilResult(result.file(), soilDataset.getDisplayName(), soilDataset.getSourceLabel(), soilDataset.getSourceCrsCode(), soilDataset);
                    }
                };
            }

            @Override
            protected void done() {
                try {
                    DownloadedSoilResult result = get();
                    addRasterLayer(result.file(), result.displayName(), result.sourceLabel(), result.sourceCrsCode());
                    dispose();
                } catch (Exception ex) {
                    AppErrorSupport.logFailure("No se pudo descargar el mapa de suelos online", ex);
                    showSoilError(I18n.t("No se pudo descargar el mapa de suelos."), ex);
                }
            }
        }.execute();
    }

    private void showSoilError(String intro, Throwable ex) {
        AppErrorSupport.showErrorDialog(this, I18n.t("Suelos online"), intro, ex);
    }

    private void addRasterLayer(File file, String datasetName, String sourceLabel, String sourceCrsCode) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException(I18n.t("El archivo descargado del mapa de suelos no existe."));
        }
        if (CatgisDesktopApp.currentProject == null) {
            CatgisDesktopApp.currentProject = new Project(I18n.t("Proyecto actual"));
        }

        String layerName = datasetName + " - " + file.getName();
        RasterLayer layer = new RasterLayer(layerName, file.getAbsolutePath());
        layer.setSourceName(sourceLabel);

        String projectCrs = CatgisDesktopApp.currentProject.getProjectCRS() != null && !CatgisDesktopApp.currentProject.getProjectCRS().isBlank()
                ? CatgisDesktopApp.currentProject.getProjectCRS()
                : sourceCrsCode;
        LocalRasterData rasterData = RasterImageLoader.loadReal(file, projectCrs, sourceCrsCode);
        String layerOperationalCrs = CRSDefinitions.normalizeCode(rasterData.getDisplayCRS());
        if (layerOperationalCrs.isBlank()) {
            layerOperationalCrs = RasterCoverageSupport.resolveOperationalRasterCrs(rasterData, projectCrs);
        }
        layer.setSourceCRS(layerOperationalCrs);

        CatgisDesktopApp.currentProject.addLayer(layer);
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.addLayer(layer);
            CatgisDesktopApp.layersPanel.selectLayer(layer);
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, rasterData);
            CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
            CatgisDesktopApp.mapPanel.zoomToLayer(layer);
        }
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.t("Mapa de suelos incorporado: ") + layer.getName());
        }
    }

    private void updateDatasetNote() {
        OnlineSoilDatasetOption dataset = (OnlineSoilDatasetOption) datasetCombo.getSelectedItem();
        if (dataset == null) {
            noteLabel.setText("");
            return;
        }
        String html = "<html><div style='padding:8px;width:560px'>"
                + "<b>" + dataset.getDisplayName() + "</b><br>"
                + I18n.t("Fuente operativa en esta ronda:") + " " + dataset.getSourceLabel() + "<br><br>"
                + dataset.getTechnicalSummary() + "<br><br>"
                + dataset.getResolutionSummary() + "<br><br>"
                + dataset.getRiskUseHint() + "<br><br>"
                + I18n.t("La capa descargada se incorpora como raster real al proyecto y queda lista para cruzarse despues con pendiente u otros analisis territoriales preliminares.") + "<br><br>"
                + I18n.t("No reemplaza cartografia edafologica de detalle ni trabajo parcelario.")
                + "</div></html>";
        noteLabel.setText(html);
    }

    private void refreshDownloadSummary() {
        OnlineSoilDatasetOption dataset = (OnlineSoilDatasetOption) datasetCombo.getSelectedItem();
        Envelope bbox = parseEnvelopeOrNull();
        if (dataset == null) {
            summaryLabel.setText("");
            return;
        }
        if (bbox == null) {
            summaryLabel.setText("<html><div style='padding:8px;width:520px'>"
                    + I18n.t("Define un area valida para calcular el resumen del mapa de suelos.")
                    + "</div></html>");
            return;
        }

        String projectCrs = CatgisDesktopApp.currentProject != null && CatgisDesktopApp.currentProject.getProjectCRS() != null
                && !CatgisDesktopApp.currentProject.getProjectCRS().isBlank()
                ? CatgisDesktopApp.currentProject.getProjectCRS()
                : "EPSG:4326";
        summaryLabel.setText("<html><div style='padding:8px;width:520px'>"
                + "<b>" + I18n.t("Fuente:") + "</b> " + dataset.getSourceLabel() + "<br>"
                + "<b>" + I18n.t("Dataset:") + "</b> " + dataset.getDisplayName() + "<br>"
                + "<b>" + I18n.t("Resolucion aproximada:") + "</b> 250 m/pixel<br>"
                + "<b>" + I18n.t("CRS de descarga:") + "</b> " + dataset.getSourceCrsCode() + "<br>"
                + "<b>" + I18n.t("CRS operativo del proyecto:") + "</b> " + projectCrs + "<br><br>"
                + I18n.t("CATGIS descarga un recorte raster del area elegida y lo incorpora listo para analisis territorial preliminar, cruce con pendiente y evaluaciones booleanas futuras.")
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
        OnlineSoilDatasetOption dataset = (OnlineSoilDatasetOption) datasetCombo.getSelectedItem();
        String datasetCode = dataset != null ? dataset.getOutputCode().toLowerCase(Locale.ROOT) : "soil";
        return FileChooserSupport.resolveSuggestedFile(
                "soil-online-output",
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
        OnlineSoilDownloadDialog dialog = new OnlineSoilDownloadDialog(owner);
        dialog.setVisible(true);
    }

    private record DownloadedSoilResult(File file,
                                        String displayName,
                                        String sourceLabel,
                                        String sourceCrsCode,
                                        OnlineSoilDatasetOption dataset) {
    }
}
