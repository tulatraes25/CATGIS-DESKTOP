package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.function.Consumer;

public class StatusBar extends JPanel {

    private final JLabel messageDotLabel;
    private final JLabel messageLabel;
    private final JLabel projectCoordinatesLabel;
    private final JLabel geographicDecimalLabel;
    private final JLabel geographicDmsLabel;
    private final JTextField scaleField;
    private final JButton applyScaleButton;
    private Consumer<String> scaleApplyListener;

    public StatusBar() {
        setLayout(new BorderLayout(8, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 216, 224)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        setBackground(new Color(248, 250, 252));
        setPreferredSize(new Dimension(100, 34));
        scaleApplyListener = value -> { };

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftPanel.setOpaque(false);

        messageDotLabel = new JLabel("\u25CF");
        messageDotLabel.setForeground(new Color(32, 158, 82));
        messageDotLabel.setFont(messageDotLabel.getFont().deriveFont(Font.BOLD, 11f));

        messageLabel = new JLabel(I18n.t("Listo"));
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 11.5f));
        messageLabel.setForeground(new Color(45, 55, 72));

        leftPanel.add(messageDotLabel);
        leftPanel.add(messageLabel);

        JPanel scalePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        scalePanel.setOpaque(false);

        JLabel scaleCaptionLabel = createInfoLabel("Escala");
        scaleCaptionLabel.setForeground(new Color(31, 41, 55));

        scaleField = new JTextField("1:-", 10);
        scaleField.setFont(scaleField.getFont().deriveFont(Font.PLAIN, 11.5f));
        scaleField.setPreferredSize(new Dimension(110, 24));
        scaleField.setToolTipText("Escala actual de la vista. Escribe 1:5000 o 5000 y presiona Enter.");
        scaleField.addActionListener(e -> applyScaleInput());

        applyScaleButton = new JButton("Ir");
        applyScaleButton.setFocusable(false);
        applyScaleButton.setMargin(new Insets(2, 8, 2, 8));
        applyScaleButton.setFont(applyScaleButton.getFont().deriveFont(Font.PLAIN, 11f));
        applyScaleButton.setToolTipText("Ajustar la vista principal a la escala indicada.");
        applyScaleButton.addActionListener(e -> applyScaleInput());

        scalePanel.add(scaleCaptionLabel);
        scalePanel.add(scaleField);
        scalePanel.add(applyScaleButton);

        JPanel coordsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        coordsPanel.setOpaque(false);

        projectCoordinatesLabel = createInfoLabel("X: -   Y: -");
        geographicDecimalLabel = createInfoLabel("Lon: -   Lat: -");
        geographicDmsLabel = createInfoLabel("DMS: -");

        coordsPanel.add(projectCoordinatesLabel);
        coordsPanel.add(geographicDecimalLabel);
        coordsPanel.add(geographicDmsLabel);

        add(leftPanel, BorderLayout.WEST);
        add(scalePanel, BorderLayout.CENTER);
        add(coordsPanel, BorderLayout.EAST);
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }

    public void setMessage(String message) {
        String text = message != null && !message.isBlank() ? message : I18n.t("Listo");
        messageLabel.setText(text);

        String lower = text.toLowerCase();
        if (lower.contains("error")) {
            messageDotLabel.setForeground(new Color(220, 38, 38));
        } else if (lower.contains("advert") || lower.contains("aviso") || lower.contains("atenci\u00F3n")) {
            messageDotLabel.setForeground(new Color(234, 179, 8));
        } else {
            messageDotLabel.setForeground(new Color(32, 158, 82));
        }
    }

    public void setProjectCoordinates(String text) {
        projectCoordinatesLabel.setText(text != null ? text : "");
    }

    public void setScaleText(String text) {
        if (!scaleField.hasFocus()) {
            forceScaleText(text);
        }
    }

    public void forceScaleText(String text) {
        scaleField.setText(text != null && !text.isBlank() ? text : "");
        scaleField.setCaretPosition(0);
    }

    public void setScaleToolTip(String tooltip) {
        scaleField.setToolTipText(tooltip != null && !tooltip.isBlank()
                ? tooltip
                : "Escala actual de la vista. Escribe 1:5000 o 5000 y presiona Enter.");
    }

    public void setScaleApplyListener(Consumer<String> listener) {
        scaleApplyListener = listener != null ? listener : value -> { };
    }

    public void setGeographicCoordinates(String text) {
        geographicDecimalLabel.setText(text != null ? text : "");
    }

    public void setGeographicDms(String text) {
        geographicDmsLabel.setText(text != null ? text : "");
    }

    public void clearCoordinates() {
        projectCoordinatesLabel.setText("X: -   Y: -");
        geographicDecimalLabel.setText("Lon: -   Lat: -");
        geographicDmsLabel.setText("DMS: -");
    }

    private void applyScaleInput() {
        if (scaleApplyListener != null) {
            scaleApplyListener.accept(scaleField.getText());
        }
    }
}
