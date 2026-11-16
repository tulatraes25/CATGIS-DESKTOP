package ar.com.catgis.catmap.ui;

import ar.com.catgis.catmap.TemplateApplicationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

/**
 * CATMAP standalone template gallery: browse the registry by category, apply a
 * template, or import a QGIS {@code .qpt}. The dialog only returns the
 * selection; {@link TemplateApplicationService} performs the change.
 */
public final class TemplateGalleryDialog {

    private TemplateGalleryDialog() {}

    /** Show the gallery; returns the selection or {@code null} when cancelled. */
    public static TemplateApplicationService.TemplateSelection show(Component parent) {
        Map<String, List<TemplateApplicationService.TemplateEntry>> grouped =
                TemplateApplicationService.byCategory();
        if (grouped.isEmpty()) {
            return null;
        }
        JDialog dialog = new JDialog(
                parent instanceof java.awt.Frame frame ? frame : null,
                "Plantillas CATMAP", true);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] categories = grouped.keySet().toArray(new String[0]);
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        JList<String> templateList = new JList<>();
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(templateList);
        scroll.setPreferredSize(new Dimension(320, 220));
        JLabel description = new JLabel(" ");

        Runnable refreshList = () -> {
            String category = (String) categoryCombo.getSelectedItem();
            List<TemplateApplicationService.TemplateEntry> entries = grouped.get(category);
            String[] labels = entries == null ? new String[0]
                    : entries.stream().map(TemplateApplicationService.TemplateEntry::displayName)
                            .toArray(String[]::new);
            templateList.setListData(labels);
            if (labels.length > 0) {
                templateList.setSelectedIndex(0);
            }
            description.setText(category == null ? " " : category + " — " + labels.length
                    + " plantillas");
        };
        categoryCombo.addActionListener(event -> refreshList.run());
        refreshList.run();

        TemplateApplicationService.TemplateSelection[] selection =
                new TemplateApplicationService.TemplateSelection[1];
        JButton apply = new JButton("Aplicar");
        apply.addActionListener(event -> {
            String category = (String) categoryCombo.getSelectedItem();
            List<TemplateApplicationService.TemplateEntry> entries = grouped.get(category);
            int index = templateList.getSelectedIndex();
            if (entries == null || index < 0 || index >= entries.size()) {
                return;
            }
            selection[0] = new TemplateApplicationService.TemplateSelection(
                    entries.get(index).key(), null);
            dialog.dispose();
        });
        JButton importQpt = new JButton("Importar QPT...");
        importQpt.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Plantilla QGIS (*.qpt)", "qpt"));
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                selection[0] = new TemplateApplicationService.TemplateSelection(
                        null, chooser.getSelectedFile());
                dialog.dispose();
            }
        });
        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(event -> dialog.dispose());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Categoria:"));
        header.add(categoryCombo);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(importQpt);
        buttons.add(cancel);
        buttons.add(apply);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.add(description, BorderLayout.PAGE_END);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return selection[0];
    }
}
