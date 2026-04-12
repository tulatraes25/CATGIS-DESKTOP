package ar.com.catgis;

import org.geotools.api.referencing.operation.MathTransform;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;

public class CoordinateConverterDialog extends JDialog {

    private JTextField txtX;
    private JTextField txtY;
    private JButton btnSource;
    private JButton btnTarget;
    private JTextField txtSourceManual;
    private JTextField txtTargetManual;
    private final JTextArea resultArea;
    private String sourceCode = "EPSG:4326";
    private String targetCode = "EPSG:32720";

    public CoordinateConverterDialog() {
        this(null, null);
    }

    public CoordinateConverterDialog(Double x, Double y) {
        setTitle("Conversor de coordenadas / CRS");
        setModal(false);
        setSize(760, 560);
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());
        setLayout(new BorderLayout(8, 8));

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.setBackground(new Color(245, 247, 250));

        content.add(buildHeader(), BorderLayout.NORTH);
        content.add(buildForm(x, y), BorderLayout.CENTER);
        content.add(buildFooter(), BorderLayout.SOUTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setRows(10);
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Resultado"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        add(content, BorderLayout.CENTER);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);
        syncButtonsFromCodes();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JLabel title = new JLabel("Conversor tecnico de coordenadas");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(21, 40, 74));

        JLabel subtitle = new JLabel(
                "<html>Usa el selector CRS moderno para origen y destino, o pega un codigo/definicion manual cuando haga falta.</html>"
        );
        subtitle.setForeground(new Color(72, 86, 104));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12.5f));

        panel.add(title, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildForm(Double x, Double y) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(4, 0, 4, 8);
        gc.anchor = GridBagConstraints.WEST;

        txtX = new JTextField(x != null ? formatNumber(x) : "", 24);
        txtY = new JTextField(y != null ? formatNumber(y) : "", 24);

        btnSource = new JButton();
        btnSource.addActionListener(e -> openSourceSelector());
        btnTarget = new JButton();
        btnTarget.addActionListener(e -> openTargetSelector());

        txtSourceManual = new JTextField(sourceCode, 24);
        txtTargetManual = new JTextField(targetCode, 24);

        addField(panel, gc, "X / Longitud / Este", txtX);
        addField(panel, gc, "Y / Latitud / Norte", txtY);
        addField(panel, gc, "CRS origen", btnSource);
        addField(panel, gc, "CRS destino", btnTarget);
        addField(panel, gc, "CRS origen manual", txtSourceManual);
        addField(panel, gc, "CRS destino manual", txtTargetManual);

        JLabel help = new JLabel("<html>Ejemplos: <b>EPSG:4326</b>, <b>EPSG:22182</b>, <b>EPSG:4490</b> o una definicion WKT.</html>");
        help.setForeground(new Color(91, 103, 121));
        help.setFont(help.getFont().deriveFont(Font.PLAIN, 11.2f));
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.insets = new Insets(8, 0, 0, 0);
        panel.add(help, gc);

        return panel;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);

        JButton btnConvert = new JButton("Convertir");
        btnConvert.addActionListener(e -> convertCoordinates());

        JButton btnSwap = new JButton("Invertir X/Y");
        btnSwap.addActionListener(e -> swapCoordinates());

        JButton btnClose = new JButton("Cerrar");
        btnClose.addActionListener(e -> dispose());

        panel.add(btnConvert);
        panel.add(btnSwap);
        panel.add(btnClose);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gc, String label, java.awt.Component component) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD, 12f));
        jLabel.setForeground(new Color(55, 65, 81));

        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        panel.add(jLabel, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gc);
        gc.gridy++;
    }

    public static void openDialog() {
        SwingUtilities.invokeLater(() -> new CoordinateConverterDialog().setVisible(true));
    }

    public static void openDialog(double x, double y) {
        SwingUtilities.invokeLater(() -> new CoordinateConverterDialog(x, y).setVisible(true));
    }

    private void openSourceSelector() {
        CRSSelectorDialog.open("CRS origen", sourceCode, code -> {
            sourceCode = code;
            txtSourceManual.setText(code);
            syncButtonsFromCodes();
        });
    }

    private void openTargetSelector() {
        CRSSelectorDialog.open("CRS destino", targetCode, code -> {
            targetCode = code;
            txtTargetManual.setText(code);
            syncButtonsFromCodes();
        });
    }

    private void syncButtonsFromCodes() {
        btnSource.setText(CRSDefinitions.getLabelForCode(sourceCode));
        btnTarget.setText(CRSDefinitions.getLabelForCode(targetCode));
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

            sourceCode = CRSDefinitions.normalizeCode(txtSourceManual.getText());
            targetCode = CRSDefinitions.normalizeCode(txtTargetManual.getText());
            syncButtonsFromCodes();

            var sourceCRS = CRSDefinitions.decode(sourceCode, true);
            var targetCRS = CRSDefinitions.decode(targetCode, true);
            MathTransform transform = org.geotools.referencing.CRS.findMathTransform(sourceCRS, targetCRS, true);

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
            throw new RuntimeException("Ingresa un valor numerico.");
        }
        return Double.parseDouble(text.trim().replace(",", "."));
    }

    private String formatNumber(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private String toDms(double value, boolean latitude) {
        String hemi = latitude ? (value >= 0 ? "N" : "S") : (value >= 0 ? "E" : "O");
        double abs = Math.abs(value);
        int degrees = (int) abs;
        double minFloat = (abs - degrees) * 60.0;
        int minutes = (int) minFloat;
        double secFloat = (minFloat - minutes) * 60.0;
        return String.format(Locale.US, "%d° %d' %.2f\" %s", degrees, minutes, secFloat, hemi);
    }
}
