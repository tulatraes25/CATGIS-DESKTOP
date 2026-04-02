package ar.com.catgis;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CRSSelectorDialog extends JDialog {

    private final JTextField txtSearch;
    private final JTextField txtManualEPSG;
    private final DefaultListModel<String> listModel;
    private final JList<String> crsList;
    private final Consumer<String> onSelect;

    public CRSSelectorDialog(Frame owner, String title, String currentCode, Consumer<String> onSelect) {
        super(owner, title, true);
        this.onSelect = onSelect;

        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new BorderLayout(6, 6));

        JPanel searchPanel = new JPanel(new BorderLayout(6, 6));
        searchPanel.add(new JLabel("Buscar CRS (nombre o EPSG):"), BorderLayout.NORTH);
        txtSearch = new JTextField();
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        JPanel manualPanel = new JPanel(new BorderLayout(6, 6));
        manualPanel.add(new JLabel("Ingresar manualmente (ej: EPSG:22182 o 22182):"), BorderLayout.NORTH);
        txtManualEPSG = new JTextField();
        if (currentCode != null && !currentCode.isBlank()) {
            txtManualEPSG.setText(CRSDefinitions.normalizeCode(currentCode));
        }
        manualPanel.add(txtManualEPSG, BorderLayout.CENTER);

        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(manualPanel, BorderLayout.SOUTH);

        listModel = new DefaultListModel<>();
        crsList = new JList<>(listModel);
        crsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(crsList);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApplySelected = new JButton("Aplicar selección");
        JButton btnApplyManual = new JButton("Aplicar manual");
        JButton btnCancel = new JButton("Cancelar");

        btnApplySelected.addActionListener(e -> applySelected());
        btnApplyManual.addActionListener(e -> applyManual());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnApplySelected);
        buttonPanel.add(btnApplyManual);
        buttonPanel.add(btnCancel);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshList();
            }
        });

        refreshList();

        if (currentCode != null && !currentCode.isBlank()) {
            String label = CRSDefinitions.getLabelForCode(currentCode);
            crsList.setSelectedValue(label, true);
        }
    }

    private void refreshList() {
        listModel.clear();

        LinkedHashMap<String, String> filtered = CRSDefinitions.filter(txtSearch.getText());
        for (Map.Entry<String, String> entry : filtered.entrySet()) {
            listModel.addElement(entry.getKey());
        }

        if (!listModel.isEmpty() && crsList.getSelectedIndex() < 0) {
            crsList.setSelectedIndex(0);
        }
    }

    private void applySelected() {
        String selectedLabel = crsList.getSelectedValue();
        if (selectedLabel == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un CRS de la lista.");
            return;
        }

        String code = CRSDefinitions.createCRSMap().get(selectedLabel);
        if (code == null || code.isBlank()) {
            JOptionPane.showMessageDialog(this, "No se pudo determinar el EPSG seleccionado.");
            return;
        }

        if (onSelect != null) {
            onSelect.accept(code);
        }

        dispose();
    }

    private void applyManual() {
        String manual = CRSDefinitions.normalizeCode(txtManualEPSG.getText());

        if (manual.isBlank()) {
            JOptionPane.showMessageDialog(this, "Ingrese un código EPSG.");
            return;
        }

        if (onSelect != null) {
            onSelect.accept(manual);
        }

        dispose();
    }

    public static void open(String title, String currentCode, Consumer<String> onSelect) {
        SwingUtilities.invokeLater(() ->
                new CRSSelectorDialog(null, title, currentCode, onSelect).setVisible(true)
        );
    }
}