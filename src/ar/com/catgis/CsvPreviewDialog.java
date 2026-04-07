package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.Map;
import java.util.Vector;

public class CsvPreviewDialog extends JDialog {

    public CsvPreviewDialog(File file, TablePointData data) {
        setTitle("Vista previa CSV");
        setModal(false);
        setSize(780, 420);
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());
        setLayout(new BorderLayout(8, 8));

        JLabel header = new JLabel("Archivo: " + (file != null ? file.getAbsolutePath() : "-"));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(header, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel();
        if (data != null) {
            for (String column : data.getColumns()) {
                model.addColumn(column);
            }
            for (Map<String, String> row : data.getRows()) {
                Vector<String> values = new Vector<>();
                for (String column : data.getColumns()) {
                    values.add(row != null ? row.getOrDefault(column, "") : "");
                }
                model.addRow(values);
            }
        }

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(22);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(740, 320));
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        add(closeButton, BorderLayout.SOUTH);
    }

    public static void open(File file, TablePointData data) {
        CsvPreviewDialog dialog = new CsvPreviewDialog(file, data);
        dialog.setVisible(true);
    }
}
