package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import org.locationtech.jts.geom.Coordinate;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class CadGeoreferenceDialog extends JDialog {

    private static final int ROW_COUNT = 4;
    private enum CaptureType { NONE, CAD, DESTINATION }

    private final Layer layer;
    private final boolean captureEnabled;
    private final Consumer<CadGeoreferenceSupport.Result> resultHandler;
    private final JRadioButton twoPointRadio;
    private final JRadioButton threePointRadio;
    private final JTextField[][] fields;
    private final JLabel[] rowLabels;
    private final JButton[] captureCadButtons;
    private final JButton[] captureDestinationButtons;
    private final JLabel modeSummaryLabel;
    private final JLabel currentSummaryLabel;
    private final JLabel residualSummaryLabel;
    private final JLabel captureSummaryLabel;

    private int activeCaptureRow = -1;
    private CaptureType activeCaptureType = CaptureType.NONE;
    private boolean finishing = false;
    private CadGeoreferenceSupport.Result result = new CadGeoreferenceSupport.Result(
            false, "", 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, ""
    );

    private CadGeoreferenceDialog(Window owner,
                                  Layer layer,
                                  boolean modalWorkflow,
                                  Consumer<CadGeoreferenceSupport.Result> resultHandler) {
        super(owner, "Georreferenciacion CAD por puntos",
                modalWorkflow ? Dialog.ModalityType.APPLICATION_MODAL : Dialog.ModalityType.MODELESS);
        this.layer = layer;
        this.captureEnabled = !modalWorkflow;
        this.resultHandler = resultHandler;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelAndClose();
            }
        });

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        header.setBackground(new Color(247, 250, 252));
        JLabel title = new JLabel("Georreferenciacion CAD por puntos");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JLabel subtitle = new JLabel("<html>Defini puntos de control CAD y sus coordenadas destino en el CRS del proyecto. "
                + "Con 2 puntos se calcula una transformacion rigida con escala/rotacion. Con 3 puntos se calcula una afin."
                + (captureEnabled
                ? "<br>Captura CAD y destino directamente en el mapa (ambos en CRS del proyecto)."
                : "<br>Desde esta ventana podés cargar coordenadas manuales y usar puntos de control adicionales para verificar el ajuste.")
                + "</html>");
        subtitle.setForeground(new Color(75, 85, 99));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        twoPointRadio = new JRadioButton("2 puntos | rigida con escala/rotacion");
        threePointRadio = new JRadioButton("3 puntos | afin");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(twoPointRadio);
        modeGroup.add(threePointRadio);
        if ("3POINT_AFFINE".equalsIgnoreCase(layer.getCadGeoreferenceMethod())) {
            threePointRadio.setSelected(true);
        } else {
            twoPointRadio.setSelected(true);
        }

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        modePanel.setOpaque(false);
        modePanel.add(twoPointRadio);
        modePanel.add(threePointRadio);

        modeSummaryLabel = new JLabel();
        modeSummaryLabel.setForeground(new Color(75, 85, 99));
        currentSummaryLabel = new JLabel("Actual: " + CadGeoreferenceSupport.buildDetailedSummary(layer));
        currentSummaryLabel.setForeground(new Color(55, 65, 81));
        residualSummaryLabel = new JLabel("Residual: completá puntos obligatorios y, si querés verificar, agregá un punto de control extra.");
        residualSummaryLabel.setForeground(new Color(55, 65, 81));
        captureSummaryLabel = new JLabel(captureEnabled
                ? "Captura mapa: primero CAD y luego destino en cada punto."
                : "Captura mapa: abrí esta herramienta desde el flujo CAD principal para usar captura interactiva.");
        captureSummaryLabel.setForeground(new Color(75, 85, 99));

        fields = new JTextField[ROW_COUNT][4];
        rowLabels = new JLabel[ROW_COUNT];
        captureCadButtons = new JButton[ROW_COUNT];
        captureDestinationButtons = new JButton[ROW_COUNT];

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        String[] headers = {"Punto", "CAD X", "CAD Y", "Destino X", "Destino Y", "Mapa CAD", "Mapa destino"};
        for (int col = 0; col < headers.length; col++) {
            gc.gridx = col;
            gc.gridy = 0;
            gc.weightx = col == 0 || col == 5 || col == 6 ? 0 : 1;
            grid.add(new JLabel(headers[col]), gc);
        }

        for (int row = 0; row < ROW_COUNT; row++) {
            gc.gridy = row + 1;
            gc.gridx = 0;
            gc.weightx = 0;
            rowLabels[row] = new JLabel();
            grid.add(rowLabels[row], gc);
            for (int col = 0; col < 4; col++) {
                gc.gridx = col + 1;
                gc.weightx = 1;
                JTextField field = new JTextField(12);
                fields[row][col] = field;
                grid.add(field, gc);
            }
            gc.gridx = 5;
            gc.weightx = 0;
            JButton captureCadButton = new JButton("Capturar CAD");
            final int captureRow = row;
            captureCadButton.addActionListener(e -> startCadCapture(captureRow));
            captureCadButtons[row] = captureCadButton;
            grid.add(captureCadButton, gc);

            gc.gridx = 6;
            JButton captureDestinationButton = new JButton("Capturar destino");
            captureDestinationButton.addActionListener(e -> startDestinationCapture(captureRow));
            captureDestinationButtons[row] = captureDestinationButton;
            grid.add(captureDestinationButton, gc);
        }

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.add(modePanel, BorderLayout.NORTH);
        center.add(grid, BorderLayout.CENTER);

        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        GridBagConstraints igc = new GridBagConstraints();
        igc.gridx = 0;
        igc.gridy = 0;
        igc.anchor = GridBagConstraints.WEST;
        igc.insets = new Insets(2, 0, 2, 0);
        info.add(modeSummaryLabel, igc);
        igc.gridy++;
        info.add(currentSummaryLabel, igc);
        igc.gridy++;
        info.add(residualSummaryLabel, igc);
        igc.gridy++;
        info.add(captureSummaryLabel, igc);
        center.add(info, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        twoPointRadio.addActionListener(e -> refreshMode());
        threePointRadio.addActionListener(e -> refreshMode());
        refreshMode();

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearGeoref = new JButton("Quitar georreferenciacion");
        clearGeoref.addActionListener(e -> clearAndClose());
        JButton verify = new JButton("Comprobar ajuste");
        verify.addActionListener(e -> previewResiduals());
        JButton apply = new JButton("Aplicar");
        apply.addActionListener(e -> computeAndClose());
        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(e -> cancelAndClose());
        buttons.add(clearGeoref);
        buttons.add(verify);
        buttons.add(cancel);
        buttons.add(apply);
        add(buttons, BorderLayout.SOUTH);

        setMinimumSize(new java.awt.Dimension(captureEnabled ? 1180 : 1000, 470));
        pack();
        setLocationRelativeTo(owner);
    }

    public static CadGeoreferenceSupport.Result open(Frame owner, Layer layer) {
        CadGeoreferenceDialog dialog = new CadGeoreferenceDialog(owner, layer, true, null);
        dialog.setVisible(true);
        return dialog.result;
    }

    public static void openInteractive(Window owner,
                                       Layer layer,
                                       Consumer<CadGeoreferenceSupport.Result> onResult) {
        CadGeoreferenceDialog dialog = new CadGeoreferenceDialog(owner, layer, false, onResult);
        dialog.setVisible(true);
    }

    private void refreshMode() {
        boolean threePoint = threePointRadio.isSelected();
        for (int row = 0; row < ROW_COUNT; row++) {
            boolean required = row < (threePoint ? 3 : 2);
            boolean cadReady = !textOf(fields[row][0]).isBlank() && !textOf(fields[row][1]).isBlank();
            rowLabels[row].setText("P" + (row + 1) + (required ? " requerido" : " control"));
            rowLabels[row].setForeground(required ? new Color(15, 23, 42) : new Color(100, 116, 139));
            boolean enabledForRow = captureEnabled && activeCaptureRow < 0;
            captureCadButtons[row].setEnabled(enabledForRow);
            captureDestinationButtons[row].setEnabled(enabledForRow && cadReady);
            if (!captureEnabled) {
                captureCadButtons[row].setToolTipText("La captura desde mapa se usa desde el flujo CAD principal.");
                captureDestinationButtons[row].setToolTipText("La captura desde mapa se usa desde el flujo CAD principal.");
            } else {
                captureCadButtons[row].setToolTipText("Capturar coordenada CAD sobre el mapa.");
                captureDestinationButtons[row].setToolTipText(cadReady
                        ? "Capturar coordenada destino sobre el mapa."
                        : "Captura primero CAD X/Y de este punto para habilitar destino.");
            }
        }
        modeSummaryLabel.setText(threePoint
                ? "Modo actual: 3 puntos exactos para una transformacion afin. P4 queda como control independiente."
                : "Modo actual: 2 puntos para una transformacion rigida con escala uniforme y rotacion. P3/P4 sirven como control independiente.");
    }

    private void startCadCapture(int row) {
        if (!captureEnabled || AppContext.mapPanel() == null || activeCaptureRow >= 0) {
            return;
        }
        activeCaptureRow = row;
        activeCaptureType = CaptureType.CAD;
        captureSummaryLabel.setText("Capturando CAD de P" + (row + 1) + " en el mapa...");
        refreshMode();
        String startMessage = "Georreferenciacion CAD: haz clic en el mapa para capturar CAD de P" + (row + 1) + ". Usa clic derecho o Esc para cancelar.";
        String successMessage = "Georreferenciacion CAD: CAD P" + (row + 1) + " capturado.";
        String cancelMessage = "Georreferenciacion CAD: captura de CAD P" + (row + 1) + " cancelada.";
        AppContext.mapPanel().startPointCapture(new MapPanel.MapPointCaptureHandler() {
            @Override
            public void onPointCaptured(Coordinate coordinate, String sourceCrs) {
                if (finishing || coordinate == null) {
                    return;
                }
                activeCaptureRow = -1;
                activeCaptureType = CaptureType.NONE;
                double[] cadCoordinates = convertProjectPointToCadCoordinates(coordinate);
                fields[row][0].setText(formatDouble(cadCoordinates[0]));
                fields[row][1].setText(formatDouble(cadCoordinates[1]));
                captureSummaryLabel.setText("CAD P" + (row + 1) + " capturado en " + formatDouble(cadCoordinates[0]) + ", " + formatDouble(cadCoordinates[1]) + ". Ahora pulsa Capturar destino para ese punto.");
                refreshMode();
                previewResidualsSilently();
                bringDialogToFront();
            }

            @Override
            public void onCaptureCanceled() {
                if (finishing) {
                    return;
                }
                activeCaptureRow = -1;
                activeCaptureType = CaptureType.NONE;
                captureSummaryLabel.setText("Captura de CAD P" + (row + 1) + " cancelada.");
                refreshMode();
                bringDialogToFront();
            }
        }, startMessage, successMessage, cancelMessage);
        if (!AppContext.mapPanel().isPointCaptureActive()) {
            activeCaptureRow = -1;
            activeCaptureType = CaptureType.NONE;
            captureSummaryLabel.setText("No se pudo iniciar la captura sobre el mapa. Revisa si hay otra herramienta activa.");
            refreshMode();
        }
    }

    private void startDestinationCapture(int row) {
        if (!captureEnabled || AppContext.mapPanel() == null || activeCaptureRow >= 0) {
            return;
        }
        if (textOf(fields[row][0]).isBlank() || textOf(fields[row][1]).isBlank()) {
            captureSummaryLabel.setText("Primero captura CAD de P" + (row + 1) + " y luego destino.");
            bringDialogToFront();
            return;
        }
        activeCaptureRow = row;
        activeCaptureType = CaptureType.DESTINATION;
        captureSummaryLabel.setText("Capturando destino de P" + (row + 1) + " en el mapa...");
        refreshMode();
        String startMessage = "Georreferenciacion CAD: haz clic en el mapa para capturar destino de P" + (row + 1) + ". Usa clic derecho o Esc para cancelar.";
        String successMessage = "Georreferenciacion CAD: destino P" + (row + 1) + " capturado.";
        String cancelMessage = "Georreferenciacion CAD: captura de destino P" + (row + 1) + " cancelada.";
        AppContext.mapPanel().startPointCapture(new MapPanel.MapPointCaptureHandler() {
            @Override
            public void onPointCaptured(Coordinate coordinate, String sourceCrs) {
                if (finishing || coordinate == null) {
                    return;
                }
                activeCaptureRow = -1;
                activeCaptureType = CaptureType.NONE;
                fields[row][2].setText(formatDouble(coordinate.x));
                fields[row][3].setText(formatDouble(coordinate.y));
                captureSummaryLabel.setText("Destino P" + (row + 1) + " capturado en " + formatDouble(coordinate.x) + ", " + formatDouble(coordinate.y) + ".");
                refreshMode();
                previewResidualsSilently();
                bringDialogToFront();
            }

            @Override
            public void onCaptureCanceled() {
                if (finishing) {
                    return;
                }
                activeCaptureRow = -1;
                activeCaptureType = CaptureType.NONE;
                captureSummaryLabel.setText("Captura de destino P" + (row + 1) + " cancelada.");
                refreshMode();
                bringDialogToFront();
            }
        }, startMessage, successMessage, cancelMessage);
        if (!AppContext.mapPanel().isPointCaptureActive()) {
            activeCaptureRow = -1;
            activeCaptureType = CaptureType.NONE;
            captureSummaryLabel.setText("No se pudo iniciar la captura sobre el mapa. Revisa si hay otra herramienta activa.");
            refreshMode();
        }
    }

    private void bringDialogToFront() {
        SwingUtilities.invokeLater(() -> {
            if (!isDisplayable()) {
                return;
            }
            toFront();
            requestFocus();
        });
    }

    private void previewResiduals() {
        try {
            CadGeoreferenceSupport.Result preview = computeResultFromFields();
            residualSummaryLabel.setText("Residual: " + preview.summary());
            residualSummaryLabel.setForeground(new Color(21, 128, 61));
        } catch (Exception ex) {
            residualSummaryLabel.setText("Residual: " + ex.getMessage());
            residualSummaryLabel.setForeground(new Color(185, 28, 28));
        }
    }

    private void previewResidualsSilently() {
        try {
            CadGeoreferenceSupport.Result preview = computeResultFromFields();
            residualSummaryLabel.setText("Residual: " + preview.summary());
            residualSummaryLabel.setForeground(new Color(21, 128, 61));
        } catch (Exception ignored) { CatgisLogger.warn("CadGeoreferenceDialog: operation failed", ignored); }
    }

    private void computeAndClose() {
        try {
            finishWithResult(computeResultFromFields());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Georreferenciacion CAD",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private CadGeoreferenceSupport.Result computeResultFromFields() {
        List<RowPoints> rows = collectRows();
        if (threePointRadio.isSelected()) {
            if (rows.size() < 3) {
                throw new IllegalArgumentException("Completa los 3 puntos requeridos para una transformacion afin.");
            }
            List<CadGeoreferenceSupport.ControlPointPair> checks = buildCheckPairs(rows, 3);
            return CadGeoreferenceSupport.computeThreePoint(
                    rows.get(0).source(),
                    rows.get(1).source(),
                    rows.get(2).source(),
                    rows.get(0).target(),
                    rows.get(1).target(),
                    rows.get(2).target(),
                    checks
            );
        }
        if (rows.size() < 2) {
            throw new IllegalArgumentException("Completa los 2 puntos requeridos para una transformacion por similitud.");
        }
        List<CadGeoreferenceSupport.ControlPointPair> checks = buildCheckPairs(rows, 2);
        return CadGeoreferenceSupport.computeTwoPoint(
                rows.get(0).source(),
                rows.get(1).source(),
                rows.get(0).target(),
                rows.get(1).target(),
                checks
        );
    }

    private List<RowPoints> collectRows() {
        List<RowPoints> rows = new ArrayList<>();
        int requiredCount = threePointRadio.isSelected() ? 3 : 2;
        for (int row = 0; row < ROW_COUNT; row++) {
            boolean required = row < requiredCount;
            RowPoints points = parseRow(row, required);
            if (points != null) {
                rows.add(points);
            }
        }
        return rows;
    }

    private List<CadGeoreferenceSupport.ControlPointPair> buildCheckPairs(List<RowPoints> rows, int requiredCount) {
        List<CadGeoreferenceSupport.ControlPointPair> checks = new ArrayList<>();
        for (int i = requiredCount; i < rows.size(); i++) {
            RowPoints row = rows.get(i);
            checks.add(new CadGeoreferenceSupport.ControlPointPair(row.source(), row.target()));
        }
        return checks;
    }

    private RowPoints parseRow(int row, boolean required) {
        String cadX = textOf(fields[row][0]);
        String cadY = textOf(fields[row][1]);
        String dstX = textOf(fields[row][2]);
        String dstY = textOf(fields[row][3]);
        boolean anyValue = !cadX.isBlank() || !cadY.isBlank() || !dstX.isBlank() || !dstY.isBlank();
        if (!anyValue) {
            if (required) {
                throw new IllegalArgumentException("Faltan datos en P" + (row + 1) + ".");
            }
            return null;
        }
        if (cadX.isBlank() || cadY.isBlank() || dstX.isBlank() || dstY.isBlank()) {
            throw new IllegalArgumentException("P" + (row + 1) + " esta incompleto. Completa CAD X/Y y Destino X/Y.");
        }
        return new RowPoints(
                new CadGeoreferenceSupport.ControlPoint(parseNumber(cadX, "CAD X de P" + (row + 1)),
                        parseNumber(cadY, "CAD Y de P" + (row + 1))),
                new CadGeoreferenceSupport.ControlPoint(parseNumber(dstX, "Destino X de P" + (row + 1)),
                        parseNumber(dstY, "Destino Y de P" + (row + 1)))
        );
    }

    private String textOf(JTextField field) {
        return field.getText() != null ? field.getText().trim() : "";
    }

    private double parseNumber(String text, String label) {
        try {
            return Double.parseDouble(text.replace(",", "."));
        } catch (Exception ex) {
            throw new IllegalArgumentException(label + " invalido: " + text);
        }
    }

    private void clearAndClose() {
        finishWithResult(new CadGeoreferenceSupport.Result(
                true, "", 1, 0, 0, 0, 1, 0, Double.NaN, Double.NaN, 0, 0, "Sin georreferenciacion por puntos"
        ));
    }

    private void cancelAndClose() {
        finishWithResult(result.approved()
                ? result
                : new CadGeoreferenceSupport.Result(false, "", 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, ""));
    }

    private void finishWithResult(CadGeoreferenceSupport.Result finalResult) {
        if (finishing) {
            return;
        }
        finishing = true;
        result = finalResult;
        if (activeCaptureRow >= 0 && AppContext.mapPanel() != null && AppContext.mapPanel().isPointCaptureActive()) {
            activeCaptureRow = -1;
            activeCaptureType = CaptureType.NONE;
            AppContext.mapPanel().cancelPointCapture();
        }
        if (resultHandler != null) {
            resultHandler.accept(finalResult);
        }
        dispose();
    }

    private String formatDouble(double value) {
        return String.format(Locale.US, "%.3f", value);
    }

    private double[] convertProjectPointToCadCoordinates(Coordinate coordinate) {
        if (coordinate == null) {
            return new double[]{0, 0};
        }
        return new double[]{coordinate.x, coordinate.y};
    }

    private record RowPoints(CadGeoreferenceSupport.ControlPoint source,
                             CadGeoreferenceSupport.ControlPoint target) {
    }
}
