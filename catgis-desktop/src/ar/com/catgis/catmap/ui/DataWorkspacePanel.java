package ar.com.catgis.catmap.ui;

import ar.com.catgis.catmap.workspace.DataWorkspaceModel;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.DefaultListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Data workspace panel (REQ-PROD-10): project layers with symbology, plus the
 * latest jobs/results. Replaces per-file modal stacks with one persistent view.
 */
public final class DataWorkspacePanel extends JPanel {

    private final DataWorkspaceModel model = new DataWorkspaceModel();
    private final DefaultListModel<DataWorkspaceModel.LayerRow> layerModel =
            new DefaultListModel<>();
    private final DefaultListModel<DataWorkspaceModel.JobResult> jobModel =
            new DefaultListModel<>();
    private final JLabel status = new JLabel(" ");

    public DataWorkspacePanel() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("Proyecto y datos");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
        JButton refresh = new JButton("Refrescar");
        refresh.setFont(refresh.getFont().deriveFont(Font.PLAIN, 10f));
        refresh.addActionListener(event -> refresh());
        header.add(title);
        header.add(refresh);
        add(header, BorderLayout.NORTH);

        JList<DataWorkspaceModel.LayerRow> layers = new JList<>(layerModel);
        layers.setCellRenderer(layerRenderer());
        JList<DataWorkspaceModel.JobResult> jobs = new JList<>(jobModel);
        jobs.setCellRenderer(jobRenderer());
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(layers), new JScrollPane(jobs));
        split.setResizeWeight(0.65);
        split.setPreferredSize(new Dimension(0, 140));
        add(split, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        refresh();
    }

    /** Rebuild rows from the current project (EDT-safe, no per-file dialogs). */
    public void refresh() {
        List<DataWorkspaceModel.LayerRow> rows =
                DataWorkspaceModel.rows(ar.com.catgis.AppContext.project());
        layerModel.clear();
        for (DataWorkspaceModel.LayerRow row : rows) {
            layerModel.addElement(row);
        }
        jobModel.clear();
        for (DataWorkspaceModel.JobResult job : model.jobs()) {
            jobModel.addElement(job);
        }
        status.setText(rows.isEmpty()
                ? "Sin capas en el proyecto (abre datos u OGC/PostGIS)."
                : rows.size() + " capa(s) · " + model.jobCount() + " trabajo(s)");
    }

    /** Record a job/result and refresh the list. */
    public void recordJob(String kind, String statusText, String detail) {
        model.addJob(kind, statusText, detail);
        refresh();
    }

    public DataWorkspaceModel model() {
        return model;
    }

    private static ListCellRenderer<? super DataWorkspaceModel.LayerRow> layerRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focused) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focused);
                if (value instanceof DataWorkspaceModel.LayerRow row) {
                    label.setText(row.describe());
                    label.setIcon(new SwatchIcon(row.symbol()));
                }
                return label;
            }
        };
    }

    private static ListCellRenderer<? super DataWorkspaceModel.JobResult> jobRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focused) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focused);
                if (value instanceof DataWorkspaceModel.JobResult job) {
                    label.setText(job.describe());
                    if ("ERROR".equalsIgnoreCase(job.status())) {
                        label.setForeground(new Color(0xB42318));
                    }
                }
                return label;
            }
        };
    }

    /** Small square showing the deterministic layer symbology colour. */
    private static final class SwatchIcon implements javax.swing.Icon {
        private final Color color;

        private SwatchIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(x, y + 2, 10, 10, 3, 3);
                g2.setColor(new Color(0x374151));
                g2.drawRoundRect(x, y + 2, 10, 10, 3, 3);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }
    }
}
