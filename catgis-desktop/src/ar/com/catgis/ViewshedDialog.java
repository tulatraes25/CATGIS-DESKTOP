package ar.com.catgis;

import ar.com.catgis.catmap.AnalysisRaster;
import ar.com.catgis.catmap.ViewshedRaster;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.jts.ReferencedEnvelope;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Product viewshed dialog: pick a DEM, observe from a map coordinate with a
 * radius, and add the visibility result as a map overlay layer.
 */
public class ViewshedDialog extends JDialog {

    private final JComboBox<Layer> rasterCombo;
    private final JTextField observerXField;
    private final JTextField observerYField;
    private final JTextField observerHeightField;
    private final JTextField radiusField;
    private final JLabel statusLabel;
    private SwingWorker<ViewshedRaster.Viewshed, Void> activeWorker;
    private volatile boolean cancelRequested;

    public static void open() {
        List<Layer> rasters = TopographyWorkflowSupport.getAvailableRasterLayers();
        if (rasters.isEmpty()) {
            TopographyWorkflowSupport.showNoRasterMessage();
            return;
        }
        Layer preferred = TopographyWorkflowSupport.resolvePreferredRasterLayer();
        java.awt.Window owner = CatgisDesktopApp.getMainFrameSafe()
                instanceof java.awt.Window window ? window : null;
        ViewshedDialog dialog = new ViewshedDialog(owner, rasters, preferred);
        dialog.setVisible(true);
    }

    public ViewshedDialog(java.awt.Window owner, List<Layer> rasters, Layer preferred) {
        super(owner, I18n.t("Visibilidad desde un observador"),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 8, 5, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0;
        gc.gridy = 0;
        form.add(new JLabel(I18n.t("DEM:")), gc);
        rasterCombo = new JComboBox<>(rasters.toArray(new Layer[0]));
        if (preferred != null) {
            rasterCombo.setSelectedItem(preferred);
        }
        gc.gridx = 1;
        form.add(rasterCombo, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        form.add(new JLabel(I18n.t("Observador X / Y (CRS del mapa):")), gc);
        JPanel coordinates = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        observerXField = new JTextField("", 10);
        observerYField = new JTextField("", 10);
        coordinates.add(observerXField);
        coordinates.add(observerYField);
        gc.gridx = 1;
        form.add(coordinates, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        form.add(new JLabel(I18n.t("Altura del observador (m):")), gc);
        observerHeightField = new JTextField("1.7", 10);
        gc.gridx = 1;
        form.add(observerHeightField, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        form.add(new JLabel(I18n.t("Radio de visibilidad (m):")), gc);
        radiusField = new JTextField("500", 10);
        gc.gridx = 1;
        form.add(radiusField, gc);

        gc.gridx = 0;
        gc.gridy = 4;
        gc.gridwidth = 2;
        statusLabel = new JLabel(" ");
        form.add(statusLabel, gc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(I18n.t("Cancelar"));
        cancelButton.addActionListener(e -> {
            if (activeWorker != null && !activeWorker.isDone()) {
                cancelRequested = true;
                activeWorker.cancel(true);
            } else {
                dispose();
            }
        });
        JButton computeButton = new JButton(I18n.t("Calcular vista"));
        computeButton.addActionListener(e -> startComputation());
        buttons.add(cancelButton);
        buttons.add(computeButton);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setSize(460, 260);
        setLocationRelativeTo(owner);
        prefillObserverFromView();
    }

    private void prefillObserverFromView() {
        try {
            Layer layer = (Layer) rasterCombo.getSelectedItem();
            if (layer instanceof RasterLayer) {
                GridCoverage2D coverage = RasterCoverageSupport.readCoverage(layer);
                if (coverage != null && coverage.getEnvelope2D() != null) {
                    observerXField.setText(String.format(Locale.ROOT, "%.3f",
                            coverage.getEnvelope2D().getCenterX()));
                    observerYField.setText(String.format(Locale.ROOT, "%.3f",
                            coverage.getEnvelope2D().getCenterY()));
                }
            }
        } catch (Exception ignored) {
            // The user can type the coordinates manually.
        }
    }

    private void startComputation() {
        Layer layer = (Layer) rasterCombo.getSelectedItem();
        if (!(layer instanceof RasterLayer)) {
            statusLabel.setText(I18n.t("Selecciona un DEM raster valido."));
            return;
        }
        final double observerX;
        final double observerY;
        final double observerHeight;
        final double radiusMeters;
        try {
            observerX = parse(observerXField.getText());
            observerY = parse(observerYField.getText());
            observerHeight = parse(observerHeightField.getText());
            radiusMeters = parse(radiusField.getText());
            if (observerHeight < 0 || radiusMeters <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException invalid) {
            statusLabel.setText(I18n.t("Revisa las coordenadas, altura y radio."));
            return;
        }
        statusLabel.setText(I18n.t("Calculando visibilidad..."));
        cancelRequested = false;
        activeWorker = new SwingWorker<ViewshedRaster.Viewshed, Void>() {
            private String sourceCrs;
            private ReferencedEnvelope envelope;
            private File sourceFile;

            @Override
            protected ViewshedRaster.Viewshed doInBackground() throws Exception {
                GridCoverage2D coverage = RasterCoverageSupport.readCoverage(layer);
                if (coverage == null || coverage.getRenderedImage() == null) {
                    throw new IllegalStateException("No se pudo leer el DEM.");
                }
                Raster raster = coverage.getRenderedImage().getData();
                int width = raster.getWidth();
                int height = raster.getHeight();
                double[] values = new double[width * height];
                double[] noDataValues = noDataValues(coverage);
                for (int row = 0; row < height; row++) {
                    for (int col = 0; col < width; col++) {
                        double value = raster.getSampleDouble(
                                raster.getMinX() + col, raster.getMinY() + row, 0);
                        values[row * width + col] = isNoData(value, noDataValues)
                                ? Double.NaN : value;
                    }
                }
                envelope = new ReferencedEnvelope(coverage.getEnvelope2D());
                sourceCrs = RasterCoverageSupport.resolveOperationalAnalysisCrsCode(
                        coverage, layer);
                sourceFile = layer.getPath() != null ? new File(layer.getPath()) : null;
                if (cancelRequested) {
                    throw new java.util.concurrent.CancellationException(
                            "Viewshed cancelado.");
                }
                double[] cellSize = cellSizeMeters(coverage, sourceCrs,
                        envelope.getCenterY());
                int observerX = (int) Math.floor(
                        (observerXWorld - envelope.getMinX()) / cellSize[0]);
                int observerY = (int) Math.floor(
                        (envelope.getMaxY() - observerYWorld) / cellSize[1]);
                int radiusCells = (int) Math.max(1,
                        Math.round(radiusMeters / Math.max(cellSize[0], cellSize[1])));
                AnalysisRaster dem = new AnalysisRaster("DEM", "m", width, height,
                        toFloat(values), ar.com.catgis.catmap.TerrainRasterAlgorithms.NO_DATA,
                        envelope.getMinX(), envelope.getMinY(),
                        envelope.getMaxX(), envelope.getMaxY(), sourceCrs);
                return ViewshedRaster.compute(dem, observerX, observerY,
                        observerHeight, radiusCells);
            }

            private double observerXWorld = observerX;
            private double observerYWorld = observerY;

            @Override
            protected void done() {
                if (isCancelled()) {
                    statusLabel.setText(I18n.t("Viewshed cancelado."));
                    dispose();
                    return;
                }
                try {
                    ViewshedRaster.Viewshed viewshed = get();
                    addResultLayer(layer, viewshed);
                    statusLabel.setText(I18n.t("Vista agregada al mapa."));
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setText(I18n.t("No se pudo calcular el viewshed: ")
                            + cause.getMessage());
                }
            }

            private void addResultLayer(Layer demLayer, ViewshedRaster.Viewshed viewshed) {
                BufferedImage overlay = ViewshedRaster.overlayImage(viewshed);
                RasterLayer result = new RasterLayer(
                        "Visibilidad - " + demLayer.getName(),
                        demLayer.getPath() != null ? demLayer.getPath() : "");
                result.setVisible(true);
                result.setSourceName("Viewshed de " + demLayer.getName());
                result.setSourceCRS(sourceCrs);
                result.setFeatureCount(1);
                result.setRasterMode("derived");
                result.setDerivedOperation(TerrainHydrologyAnalysisService.OP_VIEWSHED);
                result.setDerivedParameters("observer="
                        + String.format(Locale.ROOT, "%.3f,%.3f", observerXWorld, observerYWorld)
                        + ";height=" + observerHeight + ";radius=" + radiusMeters);
                result.setOpacity(0.75f);
                result.setRedBand(0);
                result.setGreenBand(1);
                result.setBlueBand(2);
                LocalRasterData data = new LocalRasterData(overlay,
                        new org.locationtech.jts.geom.Envelope(
                                envelope.getMinX(), envelope.getMaxX(),
                                envelope.getMinY(), envelope.getMaxY()),
                        4, true, sourceCrs, "derived", sourceCrs);
                if (AppContext.project() == null) {
                    AppContext.setCurrentProject(new Project(I18n.t("Proyecto actual")));
                }
                AppContext.project().addLayer(result);
                if (CatgisDesktopApp.layersPanel != null) {
                    AppContext.addLayer(result);
                }
                if (AppContext.mapPanel() != null) {
                    AppContext.mapPanel().addOrUpdateRasterLayer(result, data);
                    AppContext.mapPanel().repaint();
                }
                if (sourceFile != null) {
                    AppContext.setStatusMessage("Viewshed generado desde "
                            + sourceFile.getName());
                }
            }
        };
        activeWorker.execute();
    }

    private static float[] toFloat(double[] values) {
        float[] floats = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            floats[i] = Double.isNaN(values[i])
                    ? ar.com.catgis.catmap.TerrainRasterAlgorithms.NO_DATA
                    : (float) values[i];
        }
        return floats;
    }

    private static double parse(String text) {
        return Double.parseDouble(text.trim().replace(",", "."));
    }

    private static double[] noDataValues(GridCoverage2D coverage) {
        try {
            Object dimension = coverage.getSampleDimension(0);
            if (dimension == null) {
                return new double[0];
            }
            Object values = dimension.getClass().getMethod("getNoDataValues").invoke(dimension);
            if (values instanceof double[] doubles) {
                return doubles;
            }
        } catch (Exception ignored) {
            // Fall back to the explicit NaN check.
        }
        return new double[0];
    }

    private static boolean isNoData(double value, double[] noDataValues) {
        if (!Double.isFinite(value)) {
            return true;
        }
        for (double noData : noDataValues) {
            if (Double.isFinite(noData) && Math.abs(value - noData) < 1.0e-9) {
                return true;
            }
        }
        return false;
    }

    /** Metres per map unit and cell size in metres for the observation CRS. */
    private static double[] cellSizeMeters(GridCoverage2D coverage, String crsCode,
                                           double centerLatitude) {
        double widthUnits = coverage.getEnvelope2D().getWidth()
                / coverage.getRenderedImage().getWidth();
        double heightUnits = coverage.getEnvelope2D().getHeight()
                / coverage.getRenderedImage().getHeight();
        double metersPerUnit = 1.0;
        try {
            CoordinateReferenceSystem crs =
                    CRSDefinitions.decode(CRSDefinitions.normalizeCode(crsCode), true);
            if (crs instanceof org.geotools.api.referencing.crs.GeographicCRS) {
                double latitudeFactor = Math.max(0.02,
                        Math.cos(Math.toRadians(centerLatitude)));
                metersPerUnit = 111320.0 * latitudeFactor;
            } else {
                var unit = crs.getCoordinateSystem().getAxis(0).getUnit()
                        .asType(javax.measure.quantity.Length.class);
                metersPerUnit = unit.getConverterTo(
                        org.geotools.measure.Units.METRE).convert(1.0);
            }
        } catch (Exception ignored) {
            // Fall back to metres.
        }
        return new double[]{Math.abs(widthUnits) * metersPerUnit,
                Math.abs(heightUnits) * metersPerUnit};
    }
}
