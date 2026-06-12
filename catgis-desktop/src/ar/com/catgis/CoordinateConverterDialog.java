package ar.com.catgis;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
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
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CoordinateConverterDialog extends JDialog {

    private static final DateTimeFormatter RESULT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JTextField txtX;
    private JTextField txtY;
    private JButton btnSource;
    private JButton btnTarget;
    private JTextField txtSourceManual;
    private JTextField txtTargetManual;
    private final JTextArea resultArea;
    private String sourceCode = "EPSG:4326";
    private String targetCode = "EPSG:32720";
    private boolean swapped = false;

    public CoordinateConverterDialog() {
        this(null, null);
    }

    public CoordinateConverterDialog(Double x, Double y) {
        setTitle("Conversor de coordenadas / CRS");
        setModal(false);
        setSize(780, 620);
        setMinimumSize(new java.awt.Dimension(600, 480));
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
        resultArea.setRows(8);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(190, 200, 212)),
                        "Resultado"
                ),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        add(content, BorderLayout.CENTER);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);
        syncButtonsFromCodes();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JLabel title = new JLabel("Conversor t\u00e9cnico de coordenadas");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(21, 40, 74));

        JLabel subtitle = new JLabel(
                "<html>Use el selector CRS moderno para origen y destino, o pegue un c\u00f3digo/definici\u00f3n manual cuando haga falta.</html>"
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

        addSectionHeader(panel, gc, "Coordenadas de entrada");

        txtX = new JTextField(x != null ? formatCoord(x, true) : "", 22);
        txtX.setToolTipText("Ej.: -65.123456 o 450000 (Este)");
        txtY = new JTextField(y != null ? formatCoord(y, true) : "", 22);
        txtY.setToolTipText("Ej.: -42.123456 o 5310000 (Norte)");

        addField(panel, gc, "X / Longitud / Este", txtX);
        addField(panel, gc, "Y / Latitud / Norte", txtY);

        JLabel orderHint = new JLabel("Orden de entrada: X , Y");
        orderHint.setForeground(new Color(100, 110, 128));
        orderHint.setFont(orderHint.getFont().deriveFont(Font.PLAIN, 11f));
        gc.gridx = 1;
        gc.gridwidth = 1;
        gc.insets = new Insets(0, 0, 8, 0);
        panel.add(orderHint, gc);
        gc.gridy++;
        gc.gridx = 0;
        gc.gridwidth = 2;

        addSectionHeader(panel, gc, "CRS");

        btnSource = new JButton();
        btnSource.setToolTipText("Haga clic para seleccionar el CRS origen desde el cat\u00e1logo.");
        btnSource.addActionListener(e -> openSourceSelector());

        btnTarget = new JButton();
        btnTarget.setToolTipText("Haga clic para seleccionar el CRS destino desde el cat\u00e1logo.");
        btnTarget.addActionListener(e -> openTargetSelector());

        addField(panel, gc, "Origen", buildCrsSelector(btnSource));
        addField(panel, gc, "Destino", buildCrsSelector(btnTarget));

        txtSourceManual = new JTextField(sourceCode, 20);
        txtSourceManual.setToolTipText("Opcional: use EPSG:XXXX o pegue una definici\u00f3n WKT/PROJ.");
        txtTargetManual = new JTextField(targetCode, 20);
        txtTargetManual.setToolTipText("Opcional: use EPSG:XXXX o pegue una definici\u00f3n WKT/PROJ.");

        JLabel manualHelp = new JLabel(
                "<html><span style='font-size:11px;color:#5b6779'>Manual avanzado: EPSG:XXXX, WKT o PROJ</span></html>"
        );
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.insets = new Insets(2, 0, 2, 0);
        panel.add(manualHelp, gc);
        gc.gridy++;

        addCrsManualRow(panel, gc, "Origen manual", txtSourceManual);
        addCrsManualRow(panel, gc, "Destino manual", txtTargetManual);

        if (AppContext.project() != null) {
            JLabel projectCrsLabel = new JLabel("CRS del proyecto: " +
                    CRSDefinitions.getLabelForCode(AppContext.project().getProjectCRS()));
            projectCrsLabel.setForeground(new Color(90, 100, 118));
            projectCrsLabel.setFont(projectCrsLabel.getFont().deriveFont(Font.PLAIN, 11f));
            gc.gridx = 0;
            gc.gridwidth = 2;
            gc.insets = new Insets(8, 0, 0, 0);
            panel.add(projectCrsLabel, gc);
            gc.gridy++;
        }

        return panel;
    }

    private JPanel buildCrsSelector(JButton crsButton) {
        JPanel wrapper = new JPanel(new BorderLayout(4, 0));
        wrapper.setOpaque(false);
        crsButton.setHorizontalAlignment(JButton.LEFT);
        wrapper.add(crsButton, BorderLayout.CENTER);
        JButton selectButton = new JButton("Seleccionar\u2026");
        selectButton.setToolTipText("Abrir cat\u00e1logo de CRS.");
        selectButton.setFocusable(false);
        selectButton.setMargin(new Insets(2, 8, 2, 8));
        selectButton.addActionListener(e -> crsButton.doClick());
        wrapper.add(selectButton, BorderLayout.EAST);
        return wrapper;
    }

    private void addCrsManualRow(JPanel panel, GridBagConstraints gc, String label, JTextField field) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(jLabel.getFont().deriveFont(Font.PLAIN, 11f));
        jLabel.setForeground(new Color(100, 112, 128));

        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(2, 0, 2, 8);
        panel.add(jLabel, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gc);
        gc.gridy++;
    }

    private void addSectionHeader(JPanel panel, GridBagConstraints gc, String title) {
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 0, 6, 0);
        JLabel header = new JLabel(title);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
        header.setForeground(new Color(38, 56, 84));
        panel.add(header, gc);
        gc.gridy++;
        gc.insets = new Insets(4, 0, 4, 8);
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);

        JButton btnConvert = new JButton("Convertir");
        btnConvert.setToolTipText("Ejecuta la conversi\u00f3n de coordenadas.");
        btnConvert.addActionListener(e -> convertCoordinates());

        JButton btnSwap = new JButton("Invertir X \u2194 Y");
        btnSwap.setToolTipText("Intercambia los valores X/Y. \u00daselo si carg\u00f3 latitud y longitud al rev\u00e9s.");
        btnSwap.addActionListener(e -> swapCoordinates());

        JButton btnClear = new JButton("Limpiar");
        btnClear.setToolTipText("Borra los campos X, Y y el resultado.");
        btnClear.addActionListener(e -> clearFields());

        JButton btnCopy = new JButton("Copiar resultado");
        btnCopy.setToolTipText("Copia el texto del \u00e1rea Resultado al portapapeles.");
        btnCopy.addActionListener(e -> copyResult());

        JButton btnClose = new JButton("Cerrar");
        btnClose.addActionListener(e -> dispose());

        panel.add(btnConvert);
        panel.add(btnSwap);
        panel.add(btnClear);
        panel.add(btnCopy);
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
        swapped = true;
        SwingUtilities.invokeLater(() -> {
            resultArea.setText(
                    "Se invirtieron X e Y. Verifique que el orden sea correcto antes de convertir.\n"
                    + "X ahora contiene el valor original de Y, e Y el valor original de X."
            );
        });
    }

    private void clearFields() {
        txtX.setText("");
        txtY.setText("");
        resultArea.setText("");
        swapped = false;
    }

    private void copyResult() {
        String text = resultArea.getText();
        if (text == null || text.isBlank()) {
            JOptionPane.showMessageDialog(this, "No hay resultado para copiar.", "Conversor", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Resultado copiado al portapapeles.");
        }
    }

    private void convertCoordinates() {
        String resolvedSource = sourceCode;
        String resolvedTarget = targetCode;

        try {
            String xText = txtX.getText();
            String yText = txtY.getText();

            if (xText == null || xText.trim().isEmpty()) {
                resultArea.setText("Error: el campo X / Longitud / Este no puede estar vac\u00edo.");
                return;
            }
            if (yText == null || yText.trim().isEmpty()) {
                resultArea.setText("Error: el campo Y / Latitud / Norte no puede estar vac\u00edo.");
                return;
            }

            double x = parseNumber(xText);
            double y = parseNumber(yText);

            String manualSource = txtSourceManual.getText();
            String manualTarget = txtTargetManual.getText();
            boolean sourceManual = manualSource != null && !manualSource.trim().isBlank();
            boolean targetManual = manualTarget != null && !manualTarget.trim().isBlank();

            if (sourceManual) {
                resolvedSource = CRSDefinitions.normalizeCode(manualSource);
            }
            if (targetManual) {
                resolvedTarget = CRSDefinitions.normalizeCode(manualTarget);
            }

            if (resolvedSource.equalsIgnoreCase(resolvedTarget)) {
                resultArea.setText(
                        "Advertencia: CRS origen y destino son iguales (" + resolvedSource + ").\n"
                        + "La conversi\u00f3n no produce cambios en los valores.\n\n"
                        + "Origen\n"
                        + "CRS: " + resolvedSource + "\n"
                        + "X: " + formatCoord(x, true) + "\n"
                        + "Y: " + formatCoord(y, true) + "\n\n"
                        + "Destino\n"
                        + "CRS: " + resolvedTarget + "\n"
                        + "X: " + formatCoord(x, true) + "\n"
                        + "Y: " + formatCoord(y, true)
                );
                return;
            }

            CoordinateReferenceSystem sourceCRS;
            CoordinateReferenceSystem targetCRS;
            try {
                sourceCRS = CRSDefinitions.decode(resolvedSource, true);
            } catch (Exception ex) {
                resultArea.setText("Error: no se pudo interpretar el CRS origen.\n"
                        + "C\u00f3digo: " + resolvedSource + "\n"
                        + "Use EPSG:XXXX o una definici\u00f3n WKT/PROJ v\u00e1lida.\n\n"
                        + "Detalle: " + ex.getMessage());
                return;
            }
            try {
                targetCRS = CRSDefinitions.decode(resolvedTarget, true);
            } catch (Exception ex) {
                resultArea.setText("Error: no se pudo interpretar el CRS destino.\n"
                        + "C\u00f3digo: " + resolvedTarget + "\n"
                        + "Use EPSG:XXXX o una definici\u00f3n WKT/PROJ v\u00e1lida.\n\n"
                        + "Detalle: " + ex.getMessage());
                return;
            }

            boolean sourceIsGeographic = sourceCRS instanceof org.geotools.api.referencing.crs.GeographicCRS;
            if (sourceIsGeographic) {
                if (x < -180.0 || x > 180.0) {
                    resultArea.setText("Advertencia: el valor de X (" + formatCoord(x, false) + ") est\u00e1 fuera del rango"
                            + " esperado para longitud (-180 a 180).\n"
                            + "Verifique el orden X,Y. Use \"Invertir X \u2194 Y\" si invirti\u00f3 latitud y longitud.");
                    return;
                }
                if (y < -90.0 || y > 90.0) {
                    resultArea.setText("Advertencia: el valor de Y (" + formatCoord(y, false) + ") est\u00e1 fuera del rango"
                            + " esperado para latitud (-90 a 90).\n"
                            + "Verifique el orden X,Y. Use \"Invertir X \u2194 Y\" si invirti\u00f3 latitud y longitud.");
                    return;
                }
            }

            MathTransform transform = org.geotools.referencing.CRS.findMathTransform(sourceCRS, targetCRS, true);
            double[] sourceCoords = new double[]{x, y};
            double[] targetCoords = new double[2];
            transform.transform(sourceCoords, 0, targetCoords, 0, 1);

            double tx = targetCoords[0];
            double ty = targetCoords[1];
            boolean targetIsGeographic = targetCRS instanceof org.geotools.api.referencing.crs.GeographicCRS;

            StringBuilder sb = new StringBuilder();
            sb.append("Resultado de conversi\u00f3n\n");
            sb.append("────────────────────────────────\n\n");
            sb.append("Entrada:\n");
            sb.append(String.format(Locale.US, "  X / Longitud / Este : %s\n", formatCoord(x, sourceIsGeographic)));
            sb.append(String.format(Locale.US, "  Y / Latitud / Norte  : %s\n", formatCoord(y, sourceIsGeographic)));
            sb.append("  CRS origen          : ").append(CRSDefinitions.getLabelForCode(resolvedSource)).append("\n");
            if (sourceIsGeographic) {
                sb.append("  Lon/Lat (DMS)       : ").append(toDms(x, false)).append(" / ").append(toDms(y, true)).append("\n");
            }
            sb.append("\nSalida:\n");
            sb.append(String.format(Locale.US, "  X / Este / Longitud : %s\n", formatCoord(tx, targetIsGeographic)));
            sb.append(String.format(Locale.US, "  Y / Norte / Latitud : %s\n", formatCoord(ty, targetIsGeographic)));
            sb.append("  CRS destino         : ").append(CRSDefinitions.getLabelForCode(resolvedTarget)).append("\n");
            if (targetIsGeographic) {
                sb.append("  Lon/Lat (DMS)       : ").append(toDms(tx, false)).append(" / ").append(toDms(ty, true)).append("\n");
            }
            sb.append("\nDetalle:\n");
            sb.append("  Fecha/hora          : ").append(RESULT_DATE.format(LocalDateTime.now())).append("\n");
            if (sourceManual) {
                sb.append("  CRS origen manual   : ").append(resolvedSource).append("\n");
            }
            if (targetManual) {
                sb.append("  CRS destino manual  : ").append(resolvedTarget).append("\n");
            }
            if (swapped) {
                sb.append("  Advertencia         : Se us\u00f3 \"Invertir X \u2194 Y\". Verifique el orden.\n");
            }

            sourceCode = resolvedSource;
            targetCode = resolvedTarget;
            txtSourceManual.setText(resolvedSource);
            txtTargetManual.setText(resolvedTarget);
            syncButtonsFromCodes();
            swapped = false;

            resultArea.setText(sb.toString());
            resultArea.setCaretPosition(0);
        } catch (Exception ex) {
            StringBuilder err = new StringBuilder();
            err.append("Error al convertir coordenadas.\n\n");
            err.append("Origen : ").append(CRSDefinitions.getLabelForCode(resolvedSource)).append("\n");
            err.append("Destino: ").append(CRSDefinitions.getLabelForCode(resolvedTarget)).append("\n\n");
            err.append("Detalle: ").append(ex.getMessage());
            resultArea.setText(err.toString());
            AppErrorSupport.logFailure("Error al convertir coordenadas", ex);
        }
    }

    private double parseNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Valor num\u00e9rico requerido.");
        }
        return Double.parseDouble(text.trim().replace(",", "."));
    }

    private String formatCoord(double value, boolean isGeographic) {
        if (isGeographic) {
            return String.format(Locale.US, "%.8f", value);
        }
        return String.format(Locale.US, "%.3f", value);
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
        return String.format(Locale.US, "%d\u00b0 %d' %.2f\" %s", degrees, minutes, secFloat, hemi);
    }
}
