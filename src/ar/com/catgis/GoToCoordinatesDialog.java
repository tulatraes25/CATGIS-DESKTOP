package ar.com.catgis;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

public class GoToCoordinatesDialog extends JDialog {

    private final JTextField planarXField = new JTextField();
    private final JTextField planarYField = new JTextField();
    private final JTextField planarCrsField = new JTextField();

    private final JTextField decimalLonField = new JTextField();
    private final JTextField decimalLatField = new JTextField();

    private final JTextField latDegField = new JTextField();
    private final JTextField latMinField = new JTextField();
    private final JTextField latSecField = new JTextField();
    private final JComboBox<String> latHemCombo = new JComboBox<>(new String[]{"N", "S"});

    private final JTextField lonDegField = new JTextField();
    private final JTextField lonMinField = new JTextField();
    private final JTextField lonSecField = new JTextField();
    private final JComboBox<String> lonHemCombo = new JComboBox<>(new String[]{"E", "O"});

    private final JTabbedPane tabs = new JTabbedPane();

    private GoToCoordinatesDialog(Window owner) {
        super(owner, "Buscar por coordenadas", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        String projectCrs = "EPSG:4326";
        if (CatgisDesktopApp.currentProject != null
                && CatgisDesktopApp.currentProject.getProjectCRS() != null
                && !CatgisDesktopApp.currentProject.getProjectCRS().isBlank()) {
            projectCrs = CatgisDesktopApp.currentProject.getProjectCRS();
        }
        planarCrsField.setText(projectCrs);

        tabs.addTab("Planas", buildPlanarPanel());
        tabs.addTab("Decimal", buildDecimalPanel());
        tabs.addTab("GMS", buildDmsPanel());

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        JLabel title = new JLabel("Buscar por coordenadas");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 16f));

        JLabel subtitle = new JLabel("<html><span style='color:#555555'>CentrÃ¡ el mapa ingresando coordenadas planas, geogrÃ¡ficas en decimal o en grados, minutos y segundos.</span></html>");
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        north.add(title);
        north.add(Box.createVerticalStrut(4));
        north.add(subtitle);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        center.add(tabs, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        JButton goButton = new JButton("Ir");
        goButton.addActionListener(e -> goToCoordinates());
        JButton centerButton = new JButton("Centrar");
        centerButton.addActionListener(e -> goToCoordinates());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        buttons.add(centerButton);
        buttons.add(goButton);
        buttons.add(cancelButton);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        setSize(560, 340);
        setLocationRelativeTo(owner);
    }

    public static void openDialog() {
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        GoToCoordinatesDialog dialog = new GoToCoordinatesDialog(owner);
        dialog.setVisible(true);
    }

    private JPanel buildPlanarPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(new JLabel("X:"));
        panel.add(planarXField);
        panel.add(new JLabel("Y:"));
        panel.add(planarYField);
        panel.add(new JLabel("CRS de ingreso (EPSG):"));
        panel.add(planarCrsField);
        return panel;
    }

    private JPanel buildDecimalPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(new JLabel("Longitud:"));
        panel.add(decimalLonField);
        panel.add(new JLabel("Latitud:"));
        panel.add(decimalLatField);
        return panel;
    }

    private JPanel buildDmsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel latPanel = new JPanel(new GridLayout(1, 8, 6, 6));
        latPanel.add(new JLabel("Lat"));
        latPanel.add(latDegField);
        latPanel.add(new JLabel("\u00B0"));
        latPanel.add(latMinField);
        latPanel.add(new JLabel("'"));
        latPanel.add(latSecField);
        latPanel.add(new JLabel("\""));
        latPanel.add(latHemCombo);

        JPanel lonPanel = new JPanel(new GridLayout(1, 8, 6, 6));
        lonPanel.add(new JLabel("Lon"));
        lonPanel.add(lonDegField);
        lonPanel.add(new JLabel("\u00B0"));
        lonPanel.add(lonMinField);
        lonPanel.add(new JLabel("'"));
        lonPanel.add(lonSecField);
        lonPanel.add(new JLabel("\""));
        lonPanel.add(lonHemCombo);

        panel.add(latPanel);
        panel.add(lonPanel);
        return panel;
    }

    private void goToCoordinates() {
        try {
            double[] projectXY;

            if (tabs.getSelectedIndex() == 0) {
                double x = parseNumber(planarXField.getText(), "X");
                double y = parseNumber(planarYField.getText(), "Y");
                String sourceCrs = planarCrsField.getText().trim();
                if (sourceCrs.isEmpty()) {
                    throw new IllegalArgumentException("IngresÃ¡ el CRS de las coordenadas planas.");
                }
                projectXY = transformToProject(x, y, sourceCrs);

            } else if (tabs.getSelectedIndex() == 1) {
                double lon = parseNumber(decimalLonField.getText(), "Longitud");
                double lat = parseNumber(decimalLatField.getText(), "Latitud");
                projectXY = transformToProject(lon, lat, "EPSG:4326");

            } else {
                double lat = dmsToDecimal(
                        parseNumber(latDegField.getText(), "Grados de latitud"),
                        parseNumber(latMinField.getText(), "Minutos de latitud"),
                        parseNumber(latSecField.getText(), "Segundos de latitud"),
                        (String) latHemCombo.getSelectedItem()
                );
                double lon = dmsToDecimal(
                        parseNumber(lonDegField.getText(), "Grados de longitud"),
                        parseNumber(lonMinField.getText(), "Minutos de longitud"),
                        parseNumber(lonSecField.getText(), "Segundos de longitud"),
                        (String) lonHemCombo.getSelectedItem()
                );
                projectXY = transformToProject(lon, lat, "EPSG:4326");
            }

            centerMap(projectXY[0], projectXY[1]);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Buscar por coordenadas", JOptionPane.WARNING_MESSAGE);
        }
    }

    private double parseNumber(String text, String label) {
        String clean = text != null ? text.trim().replace(",", ".") : "";
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("IngresÃ¡ " + label + ".");
        }
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor invÃ¡lido para " + label + ".");
        }
    }

    private double dmsToDecimal(double degrees, double minutes, double seconds, String hemisphere) {
        double decimal = Math.abs(degrees) + (minutes / 60.0) + (seconds / 3600.0);
        if ("S".equalsIgnoreCase(hemisphere) || "O".equalsIgnoreCase(hemisphere) || "W".equalsIgnoreCase(hemisphere)) {
            decimal *= -1.0;
        }
        return decimal;
    }

    private double[] transformToProject(double x, double y, String sourceCrsCode) throws Exception {
        String targetCode = "EPSG:4326";
        if (CatgisDesktopApp.currentProject != null
                && CatgisDesktopApp.currentProject.getProjectCRS() != null
                && !CatgisDesktopApp.currentProject.getProjectCRS().isBlank()) {
            targetCode = CatgisDesktopApp.currentProject.getProjectCRS();
        }

        if (sourceCrsCode.equalsIgnoreCase(targetCode)) {
            return new double[]{x, y};
        }

        CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCrsCode, true);
        CoordinateReferenceSystem targetCRS = CRS.decode(targetCode, true);
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

        GeometryFactory gf = new GeometryFactory();
        Point point = gf.createPoint(new Coordinate(x, y));
        Point out = (Point) JTS.transform(point, transform);

        return new double[]{out.getX(), out.getY()};
    }

    private void centerMap(double x, double y) {
        if (CatgisDesktopApp.mapPanel == null) {
            return;
        }

        double zoom = CatgisDesktopApp.mapPanel.getZoomFactor();
        int panelWidth = Math.max(CatgisDesktopApp.mapPanel.getWidth(), 800);
        int panelHeight = Math.max(CatgisDesktopApp.mapPanel.getHeight(), 600);

        double viewMinX = x - (panelWidth / (2.0 * zoom));
        double viewMinY = y - (panelHeight / (2.0 * zoom));

        CatgisDesktopApp.mapPanel.restoreView(viewMinX, viewMinY, zoom);
        CatgisDesktopApp.mapPanel.repaint();

        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(String.format("Mapa centrado en X: %.3f  Y: %.3f", x, y));
        }
    }
}
