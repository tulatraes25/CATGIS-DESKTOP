package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Temporal Controller for time-series raster visualization.
 * Provides a time slider to navigate through temporal data.
 */
public class TemporalController extends JPanel {

    public record TemporalLayer(String name, long timestamp, int index) {}

    private final JSlider timeSlider;
    private final JLabel timeLabel;
    private final JLabel rangeLabel;
    private final List<TemporalLayer> layers;
    private int currentIndex = 0;
    private ChangeListener timeChangeListener;

    public TemporalController() {
        setLayout(new BorderLayout(8, 4));
        setBorder(BorderFactory.createTitledBorder("Controlador Temporal"));

        layers = new ArrayList<>();

        timeSlider = new JSlider(0, 0, 0);
        timeSlider.setMajorTickSpacing(1);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        timeSlider.addChangeListener(e -> onTimeChanged());

        timeLabel = new JLabel("--:--:--");
        timeLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));

        rangeLabel = new JLabel("Sin datos temporales");
        rangeLabel.setForeground(Color.GRAY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton playBtn = new JButton("▶ Play");
        playBtn.addActionListener(e -> playAnimation());
        JButton pauseBtn = new JButton("⏸ Pause");
        pauseBtn.addActionListener(e -> pauseAnimation());
        JButton stepBackBtn = new JButton("◀");
        stepBackBtn.addActionListener(e -> stepBack());
        JButton stepForwardBtn = new JButton("▶");
        stepForwardBtn.addActionListener(e -> stepForward());

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

    public void setTemporalLayers(List<String> layerNames) {
        layers.clear();
        for (int i = 0; i < layerNames.size(); i++) {
            layers.add(new TemporalLayer(layerNames.get(i), System.currentTimeMillis() + i * 86400000L, i));
        }
        timeSlider.setMaximum(Math.max(0, layers.size() - 1));
        timeSlider.setValue(0);
        currentIndex = 0;
        updateTimeDisplay();
    }

    public void addLayer(String name, long timestamp) {
        layers.add(new TemporalLayer(name, timestamp, layers.size()));
        timeSlider.setMaximum(layers.size() - 1);
        updateTimeDisplay();
    }

    public void setTimeChangeListener(ChangeListener listener) {
        this.timeChangeListener = listener;
    }

    public int getCurrentIndex() { return currentIndex; }
    public TemporalLayer getCurrentLayer() {
        if (currentIndex >= 0 && currentIndex < layers.size()) return layers.get(currentIndex);
        return null;
    }

    private void onTimeChanged() {
        currentIndex = timeSlider.getValue();
        updateTimeDisplay();
        if (timeChangeListener != null) timeChangeListener.stateChanged(null);
    }

    private void updateTimeDisplay() {
        if (layers.isEmpty()) {
            timeLabel.setText("--:--:--");
            rangeLabel.setText("Sin datos temporales");
            return;
        }
        TemporalLayer current = layers.get(currentIndex);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        timeLabel.setText(sdf.format(new java.util.Date(current.timestamp())));
        rangeLabel.setText("Paso " + (currentIndex + 1) + " de " + layers.size()
                + " | " + current.name());
    }

    private boolean playing = false;

    private void playAnimation() {
        if (playing) return;
        playing = true;
        new Thread(() -> {
            while (playing && currentIndex < layers.size() - 1) {
                currentIndex++;
                SwingUtilities.invokeLater(() -> timeSlider.setValue(currentIndex));
                try { Thread.sleep(500); } catch (InterruptedException e) { break; }
            }
            playing = false;
        }).start();
    }

    private void pauseAnimation() { playing = false; }

    private void stepBack() {
        if (currentIndex > 0) { currentIndex--; timeSlider.setValue(currentIndex); }
    }

    private void stepForward() {
        if (currentIndex < layers.size() - 1) { currentIndex++; timeSlider.setValue(currentIndex); }
    }
}
