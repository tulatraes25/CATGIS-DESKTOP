package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Geometry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class IdentifyResultsDialog extends JDialog {

    private final MapPanel mapPanel;
    private final List<IdentifyResultItem> results;
    private final JTable table;
    private final JTextArea detailArea;
    private final JLabel statusLabel;

    public IdentifyResultsDialog(Window owner, MapPanel mapPanel, List<IdentifyResultItem> results) {
        super(owner, buildTitle(results), ModalityType.MODELESS);
        this.mapPanel = mapPanel;
        this.results = results != null ? results : new ArrayList<>();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        statusLabel = new JLabel(buildStatusText(this.results));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
        add(statusLabel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Capa", "Feature ID", "Geometría", "Resumen"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (IdentifyResultItem item : this.results) {
            model.addRow(new Object[]{
                    item.getLayer() != null ? item.getLayer().getName() : "-",
                    item.getFeature() != null ? item.getFeature().getID() : "-",
                    item.getGeometryType(),
                    item.getSummary()
            });
        }

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);
        configureTableColumns();

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(false);
        detailArea.setWrapStyleWord(false);
        detailArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailArea.setBorder(BorderFactory.createTitledBorder("Atributos de la entidad"));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Entidades encontradas"));

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setPreferredSize(new Dimension(560, 230));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailScroll);
        splitPane.setResizeWeight(0.58);
        add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnZoom = new JButton("Zoom a entidad");
        btnZoom.addActionListener(e -> zoomToSelected());

        JButton btnVer = new JButton("Ver ficha");
        btnVer.addActionListener(e -> showSelectedFeatureInfo());

        JButton btnCopiar = new JButton("Copiar atributos");
        btnCopiar.addActionListener(e -> copySelectedAttributes());

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        buttonPanel.add(btnZoom);
        buttonPanel.add(btnVer);
        buttonPanel.add(btnCopiar);
        buttonPanel.add(btnCerrar);

        add(buttonPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelection();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    zoomToSelected();
                    showSelectedFeatureInfo();
                }
            }
        });

        setPreferredSize(new Dimension(940, 560));
        pack();
        setLocationRelativeTo(owner);

        if (!this.results.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
            updateSelection();
        }
    }

    public static void open(MapPanel mapPanel, List<IdentifyResultItem> results) {
        Window owner = SwingUtilities.getWindowAncestor(mapPanel);
        IdentifyResultsDialog dialog = new IdentifyResultsDialog(owner, mapPanel, results);
        dialog.setVisible(true);
    }

    private static String buildTitle(List<IdentifyResultItem> results) {
        int count = results != null ? results.size() : 0;
        return "Identificación - " + count + " resultado" + (count == 1 ? "" : "s");
    }

    private static String buildStatusText(List<IdentifyResultItem> results) {
        int count = results != null ? results.size() : 0;
        return "Se encontraron " + count + " entidad" + (count == 1 ? "" : "es") +
                " bajo el clic. Elegí una fila para ver sus atributos.";
    }

    private void configureTableColumns() {
        TableColumnModel columns = table.getColumnModel();
        if (columns.getColumnCount() >= 4) {
            columns.getColumn(0).setPreferredWidth(180);
            columns.getColumn(1).setPreferredWidth(220);
            columns.getColumn(2).setPreferredWidth(110);
            columns.getColumn(3).setPreferredWidth(420);
        }
    }

    private int getSelectedModelRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return -1;
        }
        return table.convertRowIndexToModel(viewRow);
    }

    private void updateSelection() {
        int row = getSelectedModelRow();
        if (row < 0 || row >= results.size()) {
            detailArea.setText("");
            if (mapPanel != null) {
                mapPanel.clearSelectedFeature();
            }
            return;
        }

        IdentifyResultItem item = results.get(row);
        detailArea.setText(item.buildDetailText());
        detailArea.setCaretPosition(0);

        if (mapPanel != null) {
            mapPanel.highlightIdentifiedFeature(item.getLayer(), item.getFeature());
        }
    }

    private void zoomToSelected() {
        int row = getSelectedModelRow();
        if (row < 0 || row >= results.size() || mapPanel == null) {
            return;
        }

        IdentifyResultItem item = results.get(row);
        mapPanel.zoomToFeature(item.getFeature(), item.getLayer());
    }

    private void copySelectedAttributes() {
        int row = getSelectedModelRow();
        if (row < 0 || row >= results.size()) {
            return;
        }

        IdentifyResultItem item = results.get(row);
        String text = item.buildDetailText();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);

        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Atributos copiados al portapapeles.");
        }
    }

    private void showSelectedFeatureInfo() {
        int row = getSelectedModelRow();
        if (row < 0 || row >= results.size()) {
            return;
        }

        IdentifyResultItem item = results.get(row);
        JFrame owner = null;
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            owner = (JFrame) window;
        }

        javax.swing.JOptionPane.showMessageDialog(
                owner != null ? owner : this,
                item.buildDetailText(),
                "Identificar entidad",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
    }
}

class IdentifyResultItem {
    private final Layer layer;
    private final SimpleFeature feature;
    private final Geometry geometry;

    public IdentifyResultItem(Layer layer, SimpleFeature feature, Geometry geometry) {
        this.layer = layer;
        this.feature = feature;
        this.geometry = geometry;
    }

    public Layer getLayer() {
        return layer;
    }

    public SimpleFeature getFeature() {
        return feature;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getGeometryType() {
        if (geometry == null) {
            return "-";
        }
        return geometry.getGeometryType();
    }

    public String getSummary() {
        if (feature == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Property property : feature.getProperties()) {
            String name = property.getName().toString();
            if ("the_geom".equalsIgnoreCase(name) || "geom".equalsIgnoreCase(name)) {
                continue;
            }

            Object value = property.getValue();
            if (value == null) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(name).append("=").append(value);
            count++;
            if (count >= 3) {
                break;
            }
        }

        return sb.toString();
    }

    public String buildDetailText() {
        StringBuilder sb = new StringBuilder();

        sb.append("Capa: ")
                .append(layer != null ? layer.getName() : "-")
                .append("\n");

        sb.append("Feature ID: ")
                .append(feature != null ? feature.getID() : "-")
                .append("\n");

        sb.append("Geometría: ")
                .append(getGeometryType())
                .append("\n\n");

        if (feature != null) {
            for (Property property : feature.getProperties()) {
                String name = property.getName().toString();
                if ("the_geom".equalsIgnoreCase(name) || "geom".equalsIgnoreCase(name)) {
                    continue;
                }

                Object value = property.getValue();
                sb.append(name)
                        .append(" = ")
                        .append(value != null ? value : "null")
                        .append("\n");
            }
        }

        return sb.toString();
    }
}
