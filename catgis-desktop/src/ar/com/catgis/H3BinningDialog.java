package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for H3 hexagonal binning of point layers.
 */
public class H3BinningDialog extends JDialog {

    private final JComboBox<String> resolutionCombo;
    private final JLabel statusLabel;

    public H3BinningDialog(Frame owner) {
        super(owner, "H3 Hexagonal Binning", true);
        setSize(350, 180);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0; gbc.gridx = 0;
        form.add(new JLabel("Resolution (0=coarse, 15=fine):"), gbc);
        gbc.gridx = 1;
        resolutionCombo = new JComboBox<>(new String[]{"5", "6", "7", "8", "9", "10", "11"});
        resolutionCombo.setSelectedIndex(3); // default 8
        form.add(resolutionCombo, gbc);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Generate");
        statusLabel = new JLabel("");
        buttons.add(statusLabel);
        buttons.add(okButton);
        add(buttons, BorderLayout.SOUTH);

        okButton.addActionListener(e -> {
            okButton.setEnabled(false);
            statusLabel.setText("Processing...");
            SwingUtilities.invokeLater(this::execute);
        });
    }

    private void execute() {
        try {
            Layer layer = AppContext.mapPanel() != null
                    ? AppContext.mapPanel().getSelectedLayerRef()
                    : null;
            if (layer == null) {
                statusLabel.setText("No layer selected.");
                return;
            }

            ShapefileData data = AppContext.mapPanel().getShapefileData(layer);
            if (data == null || data.getFeatures().isEmpty()) {
                statusLabel.setText("Layer has no features.");
                return;
            }

            int resolution = Integer.parseInt((String) resolutionCombo.getSelectedItem());
            List<SimpleFeature> points = data.getFeatures();
            List<H3Service.HexBin> bins = H3Service.hexBin(points, resolution);

            if (bins.isEmpty()) {
                statusLabel.setText("No bins generated.");
                return;
            }

            // Create a new polygon layer from hex bins
            SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
            tb.setName(layer.getName() + "_h3_r" + resolution);
            tb.add("the_geom", org.locationtech.jts.geom.Polygon.class);
            tb.add("count", Integer.class);
            tb.add("h3_index", String.class);
            var schema = tb.buildFeatureType();

            SimpleFeatureBuilder fb = new SimpleFeatureBuilder(schema);
            List<SimpleFeature> features = new ArrayList<>();
            GeometryFactory gf = new GeometryFactory();

            for (H3Service.HexBin bin : bins) {
                fb.reset();
                org.locationtech.jts.geom.Polygon boundary = H3Service.cellToBoundary(bin.hexIndex());
                fb.set("the_geom", boundary != null ? boundary
                        : gf.createPoint(new Coordinate(bin.centerLng(), bin.centerLat())));
                fb.set("count", bin.count());
                fb.set("h3_index", bin.hexIndex());
                features.add(fb.buildFeature(bin.hexIndex()));
            }

            Envelope envelope = new Envelope();
            for (SimpleFeature f : features) {
                org.locationtech.jts.geom.Geometry g =
                        (org.locationtech.jts.geom.Geometry) f.getDefaultGeometry();
                if (g != null) envelope.expandToInclude(g.getEnvelopeInternal());
            }

            int featureCount = features.size();
            ShapefileData result = new ShapefileData(features, envelope,
                    layer.getName() + "_h3", featureCount,
                    "H3 binning (r=" + resolution + ", " + featureCount + " cells)", schema);

            String resultName = layer.getName() + "_h3_r" + resolution;
            Layer resultLayer = new Layer(resultName, "", "SHAPEFILE");
            AppContext.mapPanel().showShapefile(resultLayer, result);

            statusLabel.setText(featureCount + " hex bins created.");
            dispose();
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    public static void open() {
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(
                AppContext.mapPanel());
        if (owner instanceof Frame f) {
            new H3BinningDialog(f).setVisible(true);
        } else if (owner instanceof Dialog d) {
            new H3BinningDialog((Frame) javax.swing.SwingUtilities.getWindowAncestor(d)).setVisible(true);
        }
    }
}
