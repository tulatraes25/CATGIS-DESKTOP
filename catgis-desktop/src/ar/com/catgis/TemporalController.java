package ar.com.catgis;

import ar.com.catgis.core.model.Layer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Temporal Controller for time-series layer visualization.
 * Shows/hides layers based on time step selection.
 */
public class TemporalController extends JPanel {

    public record TemporalStep(String name, int layerIndex, long timestamp) {}

    private final JSlider timeSlider;
    private final JLabel timeLabel;
    private final JLabel rangeLabel;
    private final List<TemporalStep> steps;
    private int currentStep = 0;
    private javax.swing.event.ChangeListener timeChangeListener;
    private boolean playing = false;

    public TemporalController() {
        setLayout(new BorderLayout(8, 4));
        setBorder(BorderFactory.createTitledBorder("Controlador Temporal"));

        steps = new ArrayList<>();

        timeSlider = new JSlider(0, 0, 0);
        timeSlider.setMajorTickSpacing(1);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        timeSlider.addChangeListener(e -> onStepChanged());

        timeLabel = new JLabel("--:--:--");
        timeLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));

        rangeLabel = new JLabel("Sin datos temporales");
        rangeLabel.setForeground(Color.GRAY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton playBtn = new JButton("▶ Play");
        playBtn.addActionListener(e -> playAnimation());
        JButton pauseBtn = new JButton("⏸ Pause");
        pauseBtn.addActionListener(e -> playing = false);
        JButton stepBackBtn = new JButton("◀");
        stepBackBtn.addActionListener(e -> { if (currentStep > 0) { currentStep--; timeSlider.setValue(currentStep); } });
        JButton stepForwardBtn = new JButton("▶");
        stepForwardBtn.addActionListener(e -> { if (currentStep < steps.size() - 1) { currentStep++; timeSlider.setValue(currentStep); } });

        controls.add(playBtn);
        controls.add(pauseBtn);
        controls.add(stepBackBtn);
        controls.add(stepForwardBtn);
        controls.add(Box.createHorizontalStrut(10));
        controls.add(timeLabel);

        add(controls, BorderLayout.NORTH);
        add(timeSlider, BorderLayout.CENTER);
        add(rangeLabel, BorderLayout.SOUTH);
    }

    /**
     * Configure temporal steps from layer list.
     * Each layer becomes one time step.
     */
    public void setLayers(List<Layer> layers) {
        steps.clear();
        for (int i = 0; i < layers.size(); i++) {
            steps.add(new TemporalStep(layers.get(i).getName(), i, System.currentTimeMillis() + i * 86400000L));
        }
        timeSlider.setMaximum(Math.max(0, steps.size() - 1));
        timeSlider.setValue(0);
        currentStep = 0;
        updateTimeDisplay();
        applyVisibility();
    }

    public void addStep(String name, int layerIndex) {
        steps.add(new TemporalStep(name, layerIndex, System.currentTimeMillis() + steps.size() * 86400000L));
        timeSlider.setMaximum(steps.size() - 1);
        updateTimeDisplay();
    }

    public void setTimeChangeListener(javax.swing.event.ChangeListener listener) {
        this.timeChangeListener = listener;
    }

    public int getCurrentStepIndex() { return currentStep; }
    public TemporalStep getCurrentStep() {
        if (currentStep >= 0 && currentStep < steps.size()) return steps.get(currentStep);
        return null;
    }

    private void onStepChanged() {
        currentStep = timeSlider.getValue();
        updateTimeDisplay();
        applyVisibility();
        if (timeChangeListener != null) timeChangeListener.stateChanged(null);
    }

    private void updateTimeDisplay() {
        if (steps.isEmpty()) {
            timeLabel.setText("--:--:--");
            rangeLabel.setText("Sin datos temporales");
            return;
        }
        TemporalStep current = steps.get(currentStep);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        timeLabel.setText(sdf.format(new java.util.Date(current.timestamp())));
        rangeLabel.setText("Paso " + (currentStep + 1) + " de " + steps.size()
                + " | " + current.name());
    }

    /**
     * Apply visibility: show only the layer for current step, hide others.
     */
    private void applyVisibility() {
        if (CatgisDesktopApp.mapPanel == null || steps.isEmpty()) return;
        TemporalStep step = steps.get(currentStep);
        if (step == null) return;

        for (Layer layer : CatgisDesktopApp.mapPanel.getRenderOrderLayers()) {
            boolean visible = false;
            for (TemporalStep s : steps) {
                if (s.layerIndex() >= 0 && s.layerIndex() < CatgisDesktopApp.mapPanel.getRenderOrderLayers().size()) {
                    Layer stepLayer = CatgisDesktopApp.mapPanel.getRenderOrderLayers().get(s.layerIndex());
                    if (stepLayer == layer && s == step) {
                        visible = true;
                        break;
                    }
                }
            }
            layer.setVisible(visible);
        }
        CatgisDesktopApp.mapPanel.repaint();
    }

    private void playAnimation() {
        if (playing) return;
        playing = true;
        new Thread(() -> {
            while (playing && currentStep < steps.size() - 1) {
                currentStep++;
                SwingUtilities.invokeLater(() -> timeSlider.setValue(currentStep));
                try { Thread.sleep(800); } catch (InterruptedException e) { break; }
            }
            playing = false;
        }).start();
    }
}
