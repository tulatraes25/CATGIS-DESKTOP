package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ToolboxDialog extends JDialog {

    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel descriptionLabel;
    private List<ToolboxAlgorithm> algorithms = new ArrayList<>();

    public ToolboxDialog() {
        setTitle("Toolbox interno de geoprocesamiento");
        setModal(false);
        setSize(880, 520);
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());
        setLayout(new BorderLayout(8, 8));

        model = new DefaultTableModel(new Object[]{"Algoritmo", "Categoria", "Entradas", "Salida"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> updateDescription());

        descriptionLabel = new JLabel("Selecciona un algoritmo para ver su descripcion.");
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        header.add(new JLabel("Base interna de algoritmos para hacer crecer Geoprocesamiento sin duplicar modulos."), BorderLayout.NORTH);
        header.add(descriptionLabel, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel();
        JButton openButton = new JButton("Abrir algoritmo");
        openButton.addActionListener(e -> openSelectedAlgorithm());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(openButton);
        footer.add(closeButton);
        add(footer, BorderLayout.SOUTH);

        reloadAlgorithms();
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new ToolboxDialog().setVisible(true));
    }

    private void reloadAlgorithms() {
        algorithms = new ArrayList<>(ToolboxRegistry.getAlgorithms());
        model.setRowCount(0);
        for (ToolboxAlgorithm algorithm : algorithms) {
            String inputs = algorithm.getInputTypes().stream()
                    .map(ToolboxInputType::getDisplayName)
                    .collect(Collectors.joining(" + "));
            model.addRow(new Object[]{
                    algorithm.getName(),
                    algorithm.getCategory(),
                    inputs,
                    algorithm.getOutputType().getDisplayName()
            });
        }
        if (!algorithms.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
            updateDescription();
        }
    }

    private void updateDescription() {
        ToolboxAlgorithm algorithm = getSelectedAlgorithm();
        if (algorithm == null) {
            descriptionLabel.setText("Selecciona un algoritmo para ver su descripcion.");
            return;
        }
        String params = algorithm.getParameters().stream()
                .map(ToolboxParameter::getLabel)
                .collect(Collectors.joining(", "));
        if (params.isBlank()) {
            params = "sin parametros extra";
        }
        descriptionLabel.setText("<html><b>" + algorithm.getName() + ":</b> " + algorithm.getDescription()
                + " <br/><b>Parametros:</b> " + params + "</html>");
    }

    private void openSelectedAlgorithm() {
        ToolboxAlgorithm algorithm = getSelectedAlgorithm();
        if (algorithm == null) {
            return;
        }
        GeoprocessingAssistantDialog.openForOperation(algorithm.getAssistantOperation());
    }

    private ToolboxAlgorithm getSelectedAlgorithm() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= algorithms.size()) {
            return null;
        }
        return algorithms.get(row);
    }
}
