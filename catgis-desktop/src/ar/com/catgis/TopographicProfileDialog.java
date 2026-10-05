package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JCheckBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

public class TopographicProfileDialog extends JDialog {

    private final JComboBox<Layer> rasterCombo;
    private final JTextField sampleCountField;
    private final JSpinner xGridLinesSpinner;
    private final JSpinner yGridLinesSpinner;
    private final JCheckBox smoothProfileCheck;
    private final JComboBox<ProfileSmoothPreset> smoothingPresetCombo;
    private final JButton lineColorButton;
    private final JLabel lineSummaryLabel;
    private final JLabel statsLabel;
    private final ProfileChartPanel chartPanel;
    private final JButton exportButton;
    private final JButton addToLayoutButton;

    private Geometry profileLineGeometry;
    private String profileLineCrs;
    private String profileLineDescription;
    private boolean captureActive;
    private TopographicProfileService.ProfileResult currentProfileResult;
    private ProfileChartStyle chartStyle;
    private File lastExportedProfileFile;

    public TopographicProfileDialog(java.awt.Frame owner, Layer initialRaster) {
        super(owner, I18n.t("Perfil topografico"), false);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        DefaultComboBoxModel<Layer> rasterModel = new DefaultComboBoxModel<>();
        for (Layer layer : TopographyWorkflowSupport.getAvailableRasterLayers()) {
            rasterModel.addElement(layer);
        }
        rasterCombo = new JComboBox<>(rasterModel);
        if (initialRaster != null) {
            rasterCombo.setSelectedItem(initialRaster);
        }

        sampleCountField = new JTextField("200", 12);
        chartStyle = ProfileChartStyle.defaultStyle();
        xGridLinesSpinner = new JSpinner(new SpinnerNumberModel(chartStyle.xGridLines(), 1, 10, 1));
        yGridLinesSpinner = new JSpinner(new SpinnerNumberModel(chartStyle.yGridLines(), 1, 5, 1));
        smoothProfileCheck = new JCheckBox(I18n.t("Suavizar grafico (solo visual)"), chartStyle.smoothCurve());
        smoothingPresetCombo = new JComboBox<>(ProfileSmoothPreset.values());
        smoothingPresetCombo.setSelectedItem(ProfileSmoothPreset.fromPasses(chartStyle.smoothingPasses()));
        lineColorButton = new JButton();
        lineSummaryLabel = new JLabel(I18n.t("Todavia no hay una linea de perfil definida."));
        lineSummaryLabel.setForeground(new Color(76, 85, 99));
        statsLabel = new JLabel(I18n.t("Genera un perfil para ver distancia, elevacion minima y maxima."));
        statsLabel.setForeground(new Color(76, 85, 99));
        chartPanel = new ProfileChartPanel(chartStyle);
        exportButton = new JButton(I18n.t("Exportar perfil PNG..."));
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> exportProfileImage());
        addToLayoutButton = new JButton(I18n.t("Agregar al compositor..."));
        addToLayoutButton.setEnabled(false);
        addToLayoutButton.addActionListener(e -> exportProfileToComposer());
        configureColorButton(chartStyle.lineColor());
        xGridLinesSpinner.addChangeListener(e -> updateChartStyleFromControls());
        yGridLinesSpinner.addChangeListener(e -> updateChartStyleFromControls());
        smoothProfileCheck.addActionListener(e -> updateChartStyleFromControls());
        smoothingPresetCombo.addActionListener(e -> updateChartStyleFromControls());

        add(buildContent(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadSelectedLineIfAvailable(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelCaptureIfNeeded();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                cancelCaptureIfNeeded();
            }
        });

        setSize(920, 620);
        setMinimumSize(new Dimension(780, 520));
        setLocationRelativeTo(owner);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(12, 0));
        content.add(buildLeftPanel(), BorderLayout.WEST);

        JPanel chartWrapper = new JPanel(new BorderLayout(0, 8));
        chartWrapper.setOpaque(false);
        chartWrapper.add(statsLabel, BorderLayout.NORTH);
        chartWrapper.add(chartPanel, BorderLayout.CENTER);
        content.add(chartWrapper, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(280, 100));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        panel.add(new JLabel(I18n.t("Raster DEM:")), gc);
        gc.gridy++;
        panel.add(rasterCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Muestras del perfil:")), gc);
        gc.gridy++;
        panel.add(sampleCountField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Lineas eje X:")), gc);
        gc.gridy++;
        panel.add(xGridLinesSpinner, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Lineas eje Y:")), gc);
        gc.gridy++;
        panel.add(yGridLinesSpinner, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Color de linea:")), gc);
        gc.gridy++;
        panel.add(lineColorButton, gc);

        gc.gridy++;
        panel.add(smoothProfileCheck, gc);
        gc.gridy++;
        panel.add(smoothingPresetCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Trazado del perfil:")), gc);
        gc.gridy++;

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actionPanel.setOpaque(false);
        JButton selectedLineButton = new JButton(I18n.t("Usar linea seleccionada"));
        selectedLineButton.addActionListener(e -> loadSelectedLineIfAvailable(true));
        JButton captureButton = new JButton(I18n.t("Capturar polilinea"));
        captureButton.addActionListener(e -> startMapCapture());
        actionPanel.add(selectedLineButton);
        actionPanel.add(captureButton);
        panel.add(actionPanel, gc);

        gc.gridy++;
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createTitledBorder(I18n.t("Linea activa")));
        infoPanel.add(lineSummaryLabel, BorderLayout.CENTER);
        panel.add(infoPanel, gc);

        gc.gridy++;
        JLabel helpLabel = new JLabel("<html><div style='width:240px'>"
                + I18n.t("Puedes usar una linea ya seleccionada en el mapa o capturar una polilinea directamente sobre la vista actual. Termina con clic derecho o Esc para cancelar.")
                + "<br><br>" + I18n.t("El suavizado solo mejora la lectura visual del grafico exportado o mostrado. No altera las cotas del DEM ni los valores del perfil.")
                + "</div></html>");
        helpLabel.setForeground(new Color(97, 106, 120));
        panel.add(helpLabel, gc);

        gc.gridy++;
        gc.weighty = 1;
        panel.add(new JPanel(), gc);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton(I18n.t("Cerrar"));
        closeButton.addActionListener(e -> {
            cancelCaptureIfNeeded();
            dispose();
        });

        JButton generateButton = new JButton(I18n.t("Generar perfil"));
        generateButton.addActionListener(e -> startProfileGeneration());

        panel.add(exportButton);
        panel.add(addToLayoutButton);
        panel.add(closeButton);
        panel.add(generateButton);
        DialogKeyboardSupport.install(this, generateButton, () -> {
            cancelCaptureIfNeeded();
            dispose();
        });
        return panel;
    }

    private void loadSelectedLineIfAvailable(boolean showFeedback) {
        TopographyWorkflowSupport.SelectedProfileLine selectedLine = TopographyWorkflowSupport.resolveSelectedProfileLine();
        if (selectedLine == null) {
            if (showFeedback) {
                NotificationManager.warn(this, null, I18n.t("Selecciona una linea vectorial en el mapa para reutilizarla en el perfil topografico."));
            }
            return;
        }
        profileLineGeometry = selectedLine.geometry();
        profileLineCrs = selectedLine.sourceCrs();
        profileLineDescription = selectedLine.description();
        captureActive = false;
        currentProfileResult = null;
        exportButton.setEnabled(false);
        addToLayoutButton.setEnabled(false);
        chartPanel.setProfileResult(null);
        statsLabel.setText(I18n.t("Genera un perfil para ver distancia, elevacion minima y maxima."));
        updateLineSummary();
    }

    private void startMapCapture() {
        if (AppContext.mapPanel() == null) {
            NotificationManager.warn(this, null, I18n.t("No hay un mapa activo para capturar el perfil."));
            return;
        }
        captureActive = true;
        currentProfileResult = null;
        exportButton.setEnabled(false);
        addToLayoutButton.setEnabled(false);
        chartPanel.setProfileResult(null);
        statsLabel.setText(I18n.t("Genera un perfil para ver distancia, elevacion minima y maxima."));
        lineSummaryLabel.setText(I18n.t("Capturando polilinea en el mapa..."));
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(I18n.t("Perfil topografico: haz clics sobre el mapa para dibujar la linea. Usa clic derecho para terminar o Esc para cancelar."));
        }
        AppContext.mapPanel().startTopographicProfileCapture(new TopographicProfileCaptureHandler() {
            @Override
            public void onLineCaptured(LineString line, String sourceCrs) {
                profileLineGeometry = line;
                profileLineCrs = sourceCrs;
                profileLineDescription = I18n.t("Linea capturada en la vista actual");
                captureActive = false;
                currentProfileResult = null;
                SwingUtilities.invokeLater(() -> {
                    exportButton.setEnabled(false);
                    addToLayoutButton.setEnabled(false);
                    chartPanel.setProfileResult(null);
                    statsLabel.setText(I18n.t("Genera un perfil para ver distancia, elevacion minima y maxima."));
                    updateLineSummary();
                    toFront();
                    requestFocus();
                });
            }

            @Override
            public void onCaptureCanceled() {
                captureActive = false;
                SwingUtilities.invokeLater(TopographicProfileDialog.this::updateLineSummary);
            }
        });
    }

    private void cancelCaptureIfNeeded() {
        if (captureActive && AppContext.mapPanel() != null) {
            AppContext.mapPanel().cancelTopographicProfileCapture();
            captureActive = false;
        }
    }

    private void updateLineSummary() {
        if (captureActive) {
            lineSummaryLabel.setText(I18n.t("Capturando polilinea en el mapa..."));
            return;
        }
        if (profileLineGeometry == null) {
            lineSummaryLabel.setText(I18n.t("Todavia no hay una linea de perfil definida."));
            return;
        }
        int vertexCount = profileLineGeometry.getCoordinates() != null ? profileLineGeometry.getCoordinates().length : 0;
        double distanceMeters = TopographicProfileService.estimateLineDistanceMeters(profileLineGeometry, profileLineCrs);
        lineSummaryLabel.setText("<html><div style='width:230px'><b>"
                + (profileLineDescription != null ? profileLineDescription : I18n.t("Perfil topografico"))
                + "</b><br>" + I18n.t("Longitud estimada: ")
                + formatDistance(distanceMeters)
                + "<br>" + I18n.t("CRS de referencia: ")
                + (profileLineCrs != null && !profileLineCrs.isBlank() ? profileLineCrs : "--")
                + "<br>" + I18n.t("Vertices del perfil: ") + vertexCount
                + "</div></html>");
    }

    private void startProfileGeneration() {
        Layer rasterLayer = (Layer) rasterCombo.getSelectedItem();
        if (!(rasterLayer instanceof RasterLayer)) {
            NotificationManager.warn(this, null, I18n.t("Selecciona un raster DEM para generar el perfil."));
            return;
        }
        if (profileLineGeometry == null) {
            NotificationManager.warn(this, null, I18n.t("Debes elegir una linea seleccionada o capturar una polilinea para el perfil."));
            return;
        }

        final int sampleCount;
        try {
            sampleCount = Integer.parseInt(sampleCountField.getText().trim());
            if (sampleCount < 16) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            NotificationManager.warn(this, null, I18n.t("La cantidad de muestras del perfil debe ser un numero entero mayor o igual a 16."));
            return;
        }

        new SwingWorker<TopographicProfileService.ProfileResult, Void>() {
            @Override
            protected TopographicProfileService.ProfileResult doInBackground() throws Exception {
                return TopographicProfileService.generateProfile(rasterLayer, profileLineGeometry, profileLineCrs, sampleCount);
            }

            @Override
            protected void done() {
                try {
                    TopographicProfileService.ProfileResult result = get();
                    currentProfileResult = result;
                    chartPanel.setProfileResult(result);
                    statsLabel.setText(buildStatsHtml(result));
                    exportButton.setEnabled(true);
                    addToLayoutButton.setEnabled(true);
                    if (CatgisDesktopApp.statusBar != null) {
                        AppContext.setStatusMessage(I18n.t("Perfil topografico generado para ") + rasterLayer.getName());
                    }
                } catch (Exception ex) {
                    NotificationManager.error(
                            TopographicProfileDialog.this,
                            I18n.t("Perfil topografico"),
                            I18n.t("No se pudo generar el perfil topografico:") + "\n" + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage())
                    );
                }
            }
        }.execute();
    }

    private void exportProfileImage() {
        if (currentProfileResult == null) {
            NotificationManager.warn(this, null, I18n.t("Primero debes generar un perfil topografico para exportarlo."));
            return;
        }

        File output = chooseProfileOutputFile("export-topographic-profile", I18n.t("Exportar perfil topografico como imagen"));
        if (output == null) {
            return;
        }
        saveProfileImage(output, false);
    }

    private void exportProfileToComposer() {
        if (currentProfileResult == null) {
            NotificationManager.warn(this, null, I18n.t("Primero debes generar un perfil topografico para incorporarlo al layout."));
            return;
        }
        File output = lastExportedProfileFile != null ? lastExportedProfileFile : chooseProfileOutputFile("export-topographic-profile-layout", I18n.t("Guardar perfil para el compositor"));
        if (output == null) {
            return;
        }
        saveProfileImage(output, true);
    }

    private File chooseProfileOutputFile(String chooserKey, String title) {
        JFileChooser chooser = FileChooserSupport.createChooser(chooserKey, title);
        chooser.setSelectedFile(FileChooserSupport.resolveSuggestedFile(
                chooserKey,
                new File("perfil_topografico_" + currentProfileResult.rasterLayer().getName().replaceAll("[^a-zA-Z0-9_-]+", "_") + ".png")
        ));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File output = chooser.getSelectedFile();
        if (output == null) {
            return null;
        }
        if (!output.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
            File parent = output.getParentFile();
            output = parent != null ? new File(parent, output.getName() + ".png") : new File(output.getName() + ".png");
        }
        return output;
    }

    private void saveProfileImage(File output, boolean openComposerAfterExport) {
        try {
            BufferedImage image = buildProfileExportImage(1280, 720);
            ImageIO.write(image, "png", output);
            lastExportedProfileFile = output;
            FileChooserSupport.rememberFile("export-topographic-profile", output);
            FileChooserSupport.rememberFile("export-topographic-profile-layout", output);
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage(I18n.t("Perfil topografico exportado: ") + output.getName());
            }
            if (openComposerAfterExport) {
                if (AppContext.project() != null) {
                    AppContext.project().setLayoutImagePath(output.getAbsolutePath());
                    CatgisDesktopApp.markProjectDirty();
                }
                MapLayoutComposerDialog.openWithLayoutImage(output);
            } else {
                NotificationManager.warn(this, null, I18n.t("Perfil topografico exportado correctamente.") + "\n" + output.getAbsolutePath());
            }
        } catch (Exception ex) {
            NotificationManager.error(
                    this,
                    I18n.t("Perfil topografico"),
                    I18n.t("No se pudo exportar el perfil topografico:") + "\n" + ex.getMessage()
            );
        }
    }

    private BufferedImage buildProfileExportImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());

            g2.setColor(new Color(25, 38, 59));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22f));
            g2.drawString(I18n.t("Perfil topografico"), 28, 36);

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 13f));
            g2.setColor(new Color(76, 85, 99));
            g2.drawString(currentProfileResult.rasterLayer().getName(), 28, 58);
            g2.drawString(stripHtml(buildStatsHtml(currentProfileResult)), 28, 78);

            ProfileChartPanel exportChart = new ProfileChartPanel(chartStyle);
            exportChart.setProfileResult(currentProfileResult);
            exportChart.setSize(image.getWidth() - 52, image.getHeight() - 126);

            Graphics2D chartGraphics = (Graphics2D) g2.create(26, 100, image.getWidth() - 52, image.getHeight() - 126);
            try {
                exportChart.paint(chartGraphics);
            } finally {
                chartGraphics.dispose();
            }
        } finally {
            g2.dispose();
        }
        return image;
    }

    private void configureColorButton(Color color) {
        Color resolved = color != null ? color : ProfileChartStyle.defaultStyle().lineColor();
        lineColorButton.setText(String.format(Locale.US, "#%02X%02X%02X", resolved.getRed(), resolved.getGreen(), resolved.getBlue()));
        lineColorButton.setBackground(resolved);
        lineColorButton.setForeground(contrastColor(resolved));
        lineColorButton.setFocusPainted(false);
        lineColorButton.setOpaque(true);
        lineColorButton.addActionListener(e -> chooseProfileLineColor());
    }

    private void chooseProfileLineColor() {
        Color selected = JColorChooser.showDialog(this, I18n.t("Elegir color de la linea del perfil"), chartStyle.lineColor());
        if (selected == null) {
            return;
        }
        chartStyle = chartStyle.withLineColor(selected);
        lineColorButton.setText(String.format(Locale.US, "#%02X%02X%02X", selected.getRed(), selected.getGreen(), selected.getBlue()));
        lineColorButton.setBackground(selected);
        lineColorButton.setForeground(contrastColor(selected));
        chartPanel.setChartStyle(chartStyle);
        chartPanel.repaint();
    }

    private void updateChartStyleFromControls() {
        chartStyle = chartStyle.withGridLines(
                ((Number) xGridLinesSpinner.getValue()).intValue(),
                ((Number) yGridLinesSpinner.getValue()).intValue()
        );
        ProfileSmoothPreset preset = (ProfileSmoothPreset) smoothingPresetCombo.getSelectedItem();
        chartStyle = chartStyle.withSmoothing(
                smoothProfileCheck.isSelected(),
                preset != null ? preset.passes() : ProfileSmoothPreset.MEDIA.passes()
        );
        chartPanel.setChartStyle(chartStyle);
        chartPanel.repaint();
    }

    private Color contrastColor(Color color) {
        int brightness = (color.getRed() * 299) + (color.getGreen() * 587) + (color.getBlue() * 114);
        return brightness >= 140000 ? new Color(33, 37, 41) : Color.WHITE;
    }

    private static String buildStatsHtml(TopographicProfileService.ProfileResult result) {
        double distanceKm = result.totalDistanceMeters() / 1000d;
        Double startElevation = firstValidElevation(result);
        Double endElevation = lastValidElevation(result);
        double relief = result.maxElevation() - result.minElevation();
        double gain = calculateElevationGain(result);
        double loss = calculateElevationLoss(result);
        return "<html><b>" + I18n.t("DEM:") + "</b> " + result.rasterLayer().getName()
                + " | <b>" + I18n.t("Distancia:") + "</b> " + String.format(Locale.US, "%.2f km", distanceKm)
                + " | <b>" + I18n.t("Min:") + "</b> " + String.format(Locale.US, "%.2f m", result.minElevation())
                + " | <b>" + I18n.t("Max:") + "</b> " + String.format(Locale.US, "%.2f m", result.maxElevation())
                + " | <b>" + I18n.t("Relieve:") + "</b> " + String.format(Locale.US, "%.2f m", relief)
                + " | <b>" + I18n.t("Subida acumulada:") + "</b> " + String.format(Locale.US, "%.2f m", gain)
                + " | <b>" + I18n.t("Bajada acumulada:") + "</b> " + String.format(Locale.US, "%.2f m", loss)
                + " | <b>" + I18n.t("Inicio:") + "</b> " + formatElevation(startElevation)
                + " | <b>" + I18n.t("Fin:") + "</b> " + formatElevation(endElevation)
                + " | <b>" + I18n.t("Muestras validas:") + "</b> " + result.validSampleCount()
                + "</html>";
    }

    private static double calculateElevationGain(TopographicProfileService.ProfileResult result) {
        if (result == null || result.samples() == null) {
            return 0d;
        }
        double gain = 0d;
        Double previous = null;
        for (TopographicProfileService.ProfileSample sample : result.samples()) {
            if (sample == null || !sample.valid() || !Double.isFinite(sample.elevation())) {
                continue;
            }
            if (previous != null && sample.elevation() > previous) {
                gain += sample.elevation() - previous;
            }
            previous = sample.elevation();
        }
        return gain;
    }

    private static double calculateElevationLoss(TopographicProfileService.ProfileResult result) {
        if (result == null || result.samples() == null) {
            return 0d;
        }
        double loss = 0d;
        Double previous = null;
        for (TopographicProfileService.ProfileSample sample : result.samples()) {
            if (sample == null || !sample.valid() || !Double.isFinite(sample.elevation())) {
                continue;
            }
            if (previous != null && sample.elevation() < previous) {
                loss += previous - sample.elevation();
            }
            previous = sample.elevation();
        }
        return loss;
    }

    private static Double firstValidElevation(TopographicProfileService.ProfileResult result) {
        if (result == null || result.samples() == null) {
            return null;
        }
        for (TopographicProfileService.ProfileSample sample : result.samples()) {
            if (sample != null && sample.valid()) {
                return sample.elevation();
            }
        }
        return null;
    }

    private static Double lastValidElevation(TopographicProfileService.ProfileResult result) {
        if (result == null || result.samples() == null) {
            return null;
        }
        for (int i = result.samples().size() - 1; i >= 0; i--) {
            TopographicProfileService.ProfileSample sample = result.samples().get(i);
            if (sample != null && sample.valid()) {
                return sample.elevation();
            }
        }
        return null;
    }

    private static String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("(?i)<br\\s*/?>", " | ")
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .trim();
    }

    private static String formatElevation(Double elevation) {
        if (elevation == null || !Double.isFinite(elevation)) {
            return "--";
        }
        return String.format(Locale.US, "%.2f m", elevation);
    }

    private static String formatDistance(double meters) {
        if (!Double.isFinite(meters)) {
            return "--";
        }
        if (meters >= 1000d) {
            return String.format(Locale.US, "%.2f km", meters / 1000d);
        }
        return String.format(Locale.US, "%.0f m", meters);
    }

    public static void open() {
        if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
            TopographyWorkflowSupport.showNoRasterMessage();
            return;
        }
        java.awt.Frame owner = JOptionPane.getFrameForComponent(CatgisDesktopApp.getMainFrameSafe());
        TopographicProfileDialog dialog = new TopographicProfileDialog(owner, TopographyWorkflowSupport.resolvePreferredRasterLayer());
        dialog.setVisible(true);
    }

    private record ProfileChartStyle(int xGridLines, int yGridLines, Color lineColor, boolean smoothCurve, int smoothingPasses) {
        private static ProfileChartStyle defaultStyle() {
            return new ProfileChartStyle(6, 4, new Color(21, 101, 192), true, ProfileSmoothPreset.MEDIA.passes());
        }

        private ProfileChartStyle withLineColor(Color updatedLineColor) {
            return new ProfileChartStyle(xGridLines, yGridLines, updatedLineColor != null ? updatedLineColor : lineColor, smoothCurve, smoothingPasses);
        }

        private ProfileChartStyle withGridLines(int updatedXGridLines, int updatedYGridLines) {
            return new ProfileChartStyle(
                    Math.max(1, Math.min(10, updatedXGridLines)),
                    Math.max(1, Math.min(5, updatedYGridLines)),
                    lineColor,
                    smoothCurve,
                    smoothingPasses
            );
        }

        private ProfileChartStyle withSmoothing(boolean enabled, int passes) {
            return new ProfileChartStyle(
                    xGridLines,
                    yGridLines,
                    lineColor,
                    enabled,
                    Math.max(1, Math.min(4, passes))
            );
        }

        private Color fillColor() {
            Color base = lineColor != null ? lineColor : new Color(21, 101, 192);
            return new Color(base.getRed(), base.getGreen(), base.getBlue(), 52);
        }
    }

    private enum ProfileSmoothPreset {
        LIGERA("Suavidad ligera", 1),
        MEDIA("Suavidad media", 2),
        FUERTE("Suavidad fuerte", 3);

        private final String label;
        private final int passes;

        ProfileSmoothPreset(String label, int passes) {
            this.label = label;
            this.passes = passes;
        }

        private int passes() {
            return passes;
        }

        private static ProfileSmoothPreset fromPasses(int passes) {
            for (ProfileSmoothPreset preset : values()) {
                if (preset.passes == passes) {
                    return preset;
                }
            }
            return MEDIA;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class ProfileChartPanel extends JPanel {

        private TopographicProfileService.ProfileResult profileResult;
        private ProfileChartStyle chartStyle;

        ProfileChartPanel(ProfileChartStyle chartStyle) {
            this.chartStyle = chartStyle != null ? chartStyle : ProfileChartStyle.defaultStyle();
            setOpaque(true);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 224, 230)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        }

        void setChartStyle(ProfileChartStyle chartStyle) {
            this.chartStyle = chartStyle != null ? chartStyle : ProfileChartStyle.defaultStyle();
            repaint();
        }

        void setProfileResult(TopographicProfileService.ProfileResult profileResult) {
            this.profileResult = profileResult;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            try {
                if (profileResult == null || profileResult.samples() == null || profileResult.samples().isEmpty()) {
                    drawEmptyState(g2, I18n.t("Genera un perfil para visualizar el grafico de elevacion."));
                    return;
                }

                List<TopographicProfileService.ProfileSample> validSamples = new ArrayList<>();
                for (TopographicProfileService.ProfileSample sample : profileResult.samples()) {
                    if (sample != null && sample.valid()) {
                        validSamples.add(sample);
                    }
                }
                if (validSamples.size() < 2) {
                    drawEmptyState(g2, I18n.t("No hay suficientes muestras validas para dibujar el perfil."));
                    return;
                }

                int width = getWidth();
                int height = getHeight();
                int left = 72;
                int right = width - 18;
                int top = 20;
                int bottom = height - 60;

                double maxDistance = Math.max(1d, profileResult.totalDistanceMeters());
                double minElevation = profileResult.minElevation();
                double maxElevation = profileResult.maxElevation();
                double range = Math.max(1d, maxElevation - minElevation);
                double paddedMin = minElevation - (range * 0.08d);
                double paddedMax = maxElevation + (range * 0.08d);
                double paddedRange = Math.max(1d, paddedMax - paddedMin);

                g2.setColor(new Color(245, 248, 252));
                g2.fillRoundRect(left, top, Math.max(1, right - left), Math.max(1, bottom - top), 18, 18);
                g2.setColor(new Color(216, 223, 232));
                g2.drawRoundRect(left, top, Math.max(1, right - left), Math.max(1, bottom - top), 18, 18);

                g2.setColor(new Color(140, 149, 161));
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
                g2.drawString(String.format(Locale.US, "%.0f m", paddedMax), 8, top + 6);
                g2.drawString(String.format(Locale.US, "%.0f m", paddedMin), 8, bottom);

                int xGridLines = chartStyle != null ? Math.max(1, Math.min(10, chartStyle.xGridLines())) : 5;
                int yGridLines = chartStyle != null ? Math.max(1, Math.min(5, chartStyle.yGridLines())) : 3;
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10.5f));
                g2.setColor(new Color(189, 198, 210));
                for (int i = 1; i <= yGridLines; i++) {
                    double factor = i / (double) (yGridLines + 1);
                    int gridY = bottom - (int) Math.round((bottom - top) * factor);
                    g2.drawLine(left, gridY, right, gridY);
                    double elevation = paddedMin + (paddedRange * factor);
                    g2.setColor(new Color(123, 133, 147));
                    g2.drawString(String.format(Locale.US, "%.1f m", elevation), 8, gridY + 4);
                    g2.setColor(new Color(189, 198, 210));
                }
                for (int i = 1; i <= xGridLines; i++) {
                    double factor = i / (double) (xGridLines + 1);
                    int gridX = left + (int) Math.round((right - left) * factor);
                    g2.drawLine(gridX, top, gridX, bottom);
                    g2.setColor(new Color(123, 133, 147));
                    g2.drawString(formatDistanceLabel(maxDistance * factor), gridX - 18, bottom + 18);
                    g2.setColor(new Color(189, 198, 210));
                }

                g2.setColor(new Color(76, 85, 99));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawLine(left, bottom, right, bottom);
                g2.drawLine(left, top, left, bottom);

                Path2D profilePath = new Path2D.Double();
                List<List<Point2D.Double>> segments = new ArrayList<>();
                List<Point2D.Double> currentSegment = new ArrayList<>();
                Point2D.Double minPoint = null;
                Point2D.Double maxPoint = null;
                double minPointElevation = Double.POSITIVE_INFINITY;
                double maxPointElevation = Double.NEGATIVE_INFINITY;

                for (TopographicProfileService.ProfileSample sample : profileResult.samples()) {
                    if (sample == null || !sample.valid()) {
                        if (currentSegment.size() >= 2) {
                            segments.add(currentSegment);
                        }
                        currentSegment = new ArrayList<>();
                        continue;
                    }

                    double x = left + ((sample.distanceMeters() / maxDistance) * (right - left));
                    double y = bottom - (((sample.elevation() - paddedMin) / paddedRange) * (bottom - top));
                    Point2D.Double point = new Point2D.Double(x, y);
                    currentSegment.add(point);
                    if (sample.elevation() < minPointElevation) {
                        minPointElevation = sample.elevation();
                        minPoint = point;
                    }
                    if (sample.elevation() > maxPointElevation) {
                        maxPointElevation = sample.elevation();
                        maxPoint = point;
                    }
                }
                if (currentSegment.size() >= 2) {
                    segments.add(currentSegment);
                }

                g2.setColor(chartStyle != null ? chartStyle.fillColor() : new Color(139, 92, 246, 42));
                for (List<Point2D.Double> segment : segments) {
                    List<Point2D.Double> renderedPoints = renderPoints(segment);
                    if (renderedPoints.size() < 2) {
                        continue;
                    }
                    Path2D fillPath = buildAreaPath(renderedPoints, bottom);
                    g2.fill(fillPath);
                    Path2D segmentPath = buildLinePath(renderedPoints);
                    profilePath.append(segmentPath, false);
                }
                g2.setColor(chartStyle != null && chartStyle.lineColor() != null ? chartStyle.lineColor() : new Color(67, 56, 202));
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(profilePath);

                if (maxPoint != null) {
                    drawValueMarker(g2, maxPoint, String.format(Locale.US, "Max %.1f m", maxPointElevation), new Color(22, 101, 52));
                }
                if (minPoint != null) {
                    drawValueMarker(g2, minPoint, String.format(Locale.US, "Min %.1f m", minPointElevation), new Color(30, 64, 175));
                }

                g2.setColor(new Color(76, 85, 99));
                g2.drawString(I18n.t("Distancia"), right - 54, height - 12);
                g2.drawString(I18n.t("Elevacion"), 8, 16);
                g2.drawString(String.format(Locale.US, "%.2f km", profileResult.totalDistanceMeters() / 1000d), right - 58, bottom + 18);
            } finally {
                g2.dispose();
            }
        }

        private void drawEmptyState(Graphics2D g2, String text) {
            g2.setColor(new Color(94, 104, 118));
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14f));
            int width = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, Math.max(18, (getWidth() - width) / 2), Math.max(30, getHeight() / 2));
        }

        private List<Point2D.Double> renderPoints(List<Point2D.Double> originalPoints) {
            if (originalPoints == null || originalPoints.size() < 3 || chartStyle == null || !chartStyle.smoothCurve()) {
                return originalPoints != null ? originalPoints : List.of();
            }
            List<Point2D.Double> points = new ArrayList<>(originalPoints.size());
            for (Point2D.Double point : originalPoints) {
                points.add(new Point2D.Double(point.x, point.y));
            }
            for (int pass = 0; pass < chartStyle.smoothingPasses(); pass++) {
                List<Point2D.Double> next = new ArrayList<>(points.size());
                next.add(new Point2D.Double(points.get(0).x, points.get(0).y));
                for (int i = 1; i < points.size() - 1; i++) {
                    Point2D.Double previous = points.get(i - 1);
                    Point2D.Double current = points.get(i);
                    Point2D.Double following = points.get(i + 1);
                    double smoothedY = (previous.y + current.y + following.y) / 3d;
                    next.add(new Point2D.Double(current.x, smoothedY));
                }
                Point2D.Double last = points.get(points.size() - 1);
                next.add(new Point2D.Double(last.x, last.y));
                points = next;
            }
            return points;
        }

        private Path2D buildLinePath(List<Point2D.Double> points) {
            Path2D path = new Path2D.Double();
            if (points == null || points.isEmpty()) {
                return path;
            }
            Point2D.Double first = points.get(0);
            path.moveTo(first.x, first.y);
            for (int i = 1; i < points.size(); i++) {
                Point2D.Double point = points.get(i);
                path.lineTo(point.x, point.y);
            }
            return path;
        }

        private Path2D buildAreaPath(List<Point2D.Double> points, int bottom) {
            Path2D area = buildLinePath(points);
            if (points == null || points.isEmpty()) {
                return area;
            }
            Point2D.Double last = points.get(points.size() - 1);
            Point2D.Double first = points.get(0);
            area.lineTo(last.x, bottom);
            area.lineTo(first.x, bottom);
            area.closePath();
            return area;
        }

        private void drawValueMarker(Graphics2D g2, Point2D.Double point, String label, Color accent) {
            if (point == null || label == null || label.isBlank()) {
                return;
            }
            int radius = 4;
            g2.setColor(Color.WHITE);
            g2.fillOval((int) Math.round(point.x) - radius - 1, (int) Math.round(point.y) - radius - 1, (radius * 2) + 2, (radius * 2) + 2);
            g2.setColor(accent);
            g2.fillOval((int) Math.round(point.x) - radius, (int) Math.round(point.y) - radius, radius * 2, radius * 2);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            int boxX = (int) Math.round(Math.max(10, Math.min(getWidth() - labelWidth - 18, point.x - (labelWidth / 2.0) - 8)));
            int boxY = (int) Math.round(Math.max(10, point.y - 28));
            g2.setColor(new Color(255, 255, 255, 236));
            g2.fillRoundRect(boxX, boxY, labelWidth + 16, 20, 12, 12);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
            g2.drawRoundRect(boxX, boxY, labelWidth + 16, 20, 12, 12);
            g2.setColor(new Color(33, 37, 41));
            g2.drawString(label, boxX + 8, boxY + 14);
        }

        private String formatDistanceLabel(double distanceMeters) {
            if (!Double.isFinite(distanceMeters)) {
                return "--";
            }
            if (distanceMeters >= 1000d) {
                return String.format(Locale.US, "%.2f km", distanceMeters / 1000d);
            }
            return String.format(Locale.US, "%.0f m", distanceMeters);
        }
    }
}

