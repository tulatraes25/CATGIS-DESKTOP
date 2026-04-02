package ar.com.catgis;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.referencing.CRS;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Locale;

public class CoordinateConverterDialog extends JDialog {

    private final JTextField txtX;
    private final JTextField txtY;
    private final JComboBox<String> cmbSource;
    private final JComboBox<String> cmbTarget;
    private final JTextField txtSourceManual;
    private final JTextField txtTargetManual;
    private final JTextArea resultArea;
    private final LinkedHashMap<String, String> crsMap;

    public CoordinateConverterDialog() {
        this(null, null);
    }

    public CoordinateConverterDialog(Double x, Double y) {
        setTitle("Conversor de coordenadas / EPSG");
        setModal(false);
        setSize(650, 460);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        crsMap = CRSDefinitions.createCRSMap();

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 6, 6));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtX = new JTextField();
        txtY = new JTextField();

        if (x != null) {
            txtX.setText(formatNumber(x));
        }
        if (y != null) {
            txtY.setText(formatNumber(y));
        }

        cmbSource = new JComboBox<>(crsMap.keySet().toArray(new String[0]));
        cmbTarget = new JComboBox<>(crsMap.keySet().toArray(new String[0]));

        cmbSource.setSelectedItem(CRSDefinitions.getLabelForCode("EPSG:4326"));
        cmbTarget.setSelectedItem(CRSDefinitions.getLabelForCode("EPSG:32720"));

        txtSourceManual = new JTextField("EPSG:4326");
        txtTargetManual = new JTextField("EPSG:32720");

        formPanel.add(new JLabel("X / Longitud / Este:"));
        formPanel.add(txtX);

        formPanel.add(new JLabel("Y / Latitud / Norte:"));
        formPanel.add(txtY);

        formPanel.add(new JLabel("CRS origen (lista):"));
        formPanel.add(cmbSource);

        formPanel.add(new JLabel("CRS destino (lista):"));
        formPanel.add(cmbTarget);

        formPanel.add(new JLabel("CRS origen manual:"));
        formPanel.add(txtSourceManual);

        formPanel.add(new JLabel("CRS destino manual:"));
        formPanel.add(txtTargetManual);

        formPanel.add(new JLabel("Ayuda:"));
        formPanel.add(new JLabel("Lat/Long: EPSG:4326"));

        JPanel buttonPanel = new JPanel();

        JButton btnConvert = new JButton("Convertir");
        btnConvert.addActionListener(e -> convertCoordinates());

        JButton btnSwap = new JButton("Invertir X/Y");
        btnSwap.addActionListener(e -> swapCoordinates());

        JButton btnUseSelected = new JButton("Usar CRS de listas");
        btnUseSelected.addActionListener(e -> syncManualWithSelected());

        JButton btnClose = new JButton("Cerrar");
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnConvert);
        buttonPanel.add(btnSwap);
        buttonPanel.add(btnUseSelected);
        buttonPanel.add(btnClose);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBorder(BorderFactory.createTitledBorder("Resultado"));
        resultArea.setRows(10);

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(resultArea, BorderLayout.SOUTH);
    }

    public static void openDialog() {
        SwingUtilities.invokeLater(() -> new CoordinateConverterDialog().setVisible(true));
    }

    public static void openDialog(double x, double y) {
        SwingUtilities.invokeLater(() -> new CoordinateConverterDialog(x, y).setVisible(true));
    }

    private void syncManualWithSelected() {
        Object source = cmbSource.getSelectedItem();
        Object target = cmbTarget.getSelectedItem();

        if (source != null) {
            txtSourceManual.setText(crsMap.get(source.toString()));
        }
        if (target != null) {
            txtTargetManual.setText(crsMap.get(target.toString()));
        }
    }

    private void swapCoordinates() {
        String x = txtX.getText();
        String y = txtY.getText();
        txtX.setText(y);
        txtY.setText(x);
    }

    private void convertCoordinates() {
        try {
            double x = parseNumber(txtX.getText());
            double y = parseNumber(txtY.getText());

            String sourceCode = CRSDefinitions.normalizeCode(txtSourceManual.getText());
            String targetCode = CRSDefinitions.normalizeCode(txtTargetManual.getText());

            CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCode, true);
            CoordinateReferenceSystem targetCRS = CRS.decode(targetCode, true);

            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            double[] sourceCoords = new double[]{x, y};
            double[] targetCoords = new double[2];
            transform.transform(sourceCoords, 0, targetCoords, 0, 1);

            double tx = targetCoords[0];
            double ty = targetCoords[1];

            StringBuilder sb = new StringBuilder();
            sb.append("Origen\n");
            sb.append("CRS: ").append(sourceCode).append("\n");
            sb.append("X: ").append(formatNumber(x)).append("\n");
            sb.append("Y: ").append(formatNumber(y)).append("\n");

            if ("EPSG:4326".equalsIgnoreCase(sourceCode)) {
                sb.append("Lon/Lat (DMS): ")
                        .append(toDms(x, false))
                        .append(" / ")
                        .append(toDms(y, true))
                        .append("\n");
            }

            sb.append("\nDestino\n");
            sb.append("CRS: ").append(targetCode).append("\n");
            sb.append("X: ").append(formatNumber(tx)).append("\n");
            sb.append("Y: ").append(formatNumber(ty)).append("\n");

            if ("EPSG:4326".equalsIgnoreCase(targetCode)) {
                sb.append("Lon/Lat (DMS): ")
                        .append(toDms(tx, false))
                        .append(" / ")
                        .append(toDms(ty, true))
                        .append("\n");
            }

            resultArea.setText(sb.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error al convertir coordenadas: " + ex.getMessage(),
                    "Conversor de coordenadas",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private double parseNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Ingrese un valor numérico.");
        }
        return Double.parseDouble(text.trim().replace(",", "."));
    }

    private String formatNumber(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private String toDms(double value, boolean latitude) {
        String hemi;
        if (latitude) {
            hemi = value >= 0 ? "N" : "S";
        } else {
            hemi = value >= 0 ? "E" : "O";
        }

        double abs = Math.abs(value);
        int degrees = (int) abs;
        double minFloat = (abs - degrees) * 60.0;
        int minutes = (int) minFloat;
        double secFloat = (minFloat - minutes) * 60.0;

        return String.format(Locale.US, "%d° %d' %.2f\" %s", degrees, minutes, secFloat, hemi);
    }
}