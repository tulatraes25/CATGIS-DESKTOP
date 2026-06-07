package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.core.model.Layer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

public class CatmapLegendEditorDialog extends JDialog {

    private final Project project;
    private final List<Layer> visibleLayers;
    private final JTextField titleField;
    private final JTextField subtitleField;
    private final DefaultListModel<CatmapLegendItem> legendModel;
    private final JList<CatmapLegendItem> legendList;
    private Runnable applyCallback;

    private CatmapLegendEditorDialog(Window owner, Project project, List<Layer> visibleLayers) {
        super(owner, "CATMAP - Editar leyenda", ModalityType.APPLICATION_MODAL);
        this.project = project;
        this.visibleLayers = visibleLayers != null ? new ArrayList<>(visibleLayers) : new ArrayList<>();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        titleField = new JTextField(project != null ? project.getLegendTitle() : "Leyenda", 28);
        subtitleField = new JTextField(project != null ? project.getLegendSubtitle() : "Capas visibles del mapa", 28);

        legendModel = new DefaultListModel<>();
        legendList = new JList<>(legendModel);
        legendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        legendList.setFixedCellHeight(52);
        legendList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 6, 5, 6));
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            row.setOpaque(true);

            CatmapLegendItem item = value instanceof CatmapLegendItem ? (CatmapLegendItem) value : null;
            JCheckBox check = new JCheckBox();
            check.setSelected(item == null || item.isVisible());
            check.setEnabled(false);
            check.setOpaque(false);

            JLabel label = (JLabel) new DefaultListCellRenderer().getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus
            );
            String title = item != null && !item.getLabel().isBlank() ? item.getLabel() : "Item de leyenda";
            String subtitle = item != null ? item.getSubtitle() : "";
            String state = item != null && item.isVisible() ? "Aparece en leyenda" : "Oculto en leyenda";
            label.setText("<html><b>" + escape(title) + "</b><br/><span style='color:#5f6c80;font-size:9px;'>"
                    + escape(subtitle) + " | " + state + "</span></html>");
            label.setOpaque(false);
            if (item != null && !item.isVisible() && !isSelected) {
                label.setForeground(new Color(120, 128, 140));
            }
            row.add(check, BorderLayout.WEST);
            row.add(label, BorderLayout.CENTER);
            return row;
        });
        legendList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                int index = legendList.locationToIndex(e.getPoint());
                if (index >= 0 && e.getX() <= 34) {
                    legendList.setSelectedIndex(index);
                    toggleSelectedVisibility();
                    return;
                }
                if (e.getClickCount() == 2) {
                    editSelectedLegendTexts();
                }
            }
        });

        resetLegendModel();
        if (!legendModel.isEmpty()) {
            legendList.setSelectedIndex(0);
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        addField(form, gc, "Titulo", titleField);
        gc.gridy += 2;
        addField(form, gc, "Subtitulo", subtitleField);
        gc.gridy += 2;

        JLabel hint = new JLabel("<html>Marcá con tilde solo lo que querés mostrar en la leyenda. La capa puede seguir visible en el mapa aunque su item esté oculto acá.</html>");
        hint.setForeground(new Color(88, 98, 112));
        form.add(hint, gc);
        gc.gridy++;

        JScrollPane scrollPane = new JScrollPane(legendList);
        scrollPane.setPreferredSize(new java.awt.Dimension(420, 240));
        form.add(scrollPane, gc);

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton upButton = new JButton("Subir");
        upButton.addActionListener(e -> moveSelected(-1));
        JButton downButton = new JButton("Bajar");
        downButton.addActionListener(e -> moveSelected(1));
        JButton toggleButton = new JButton("Mostrar/Ocultar");
        toggleButton.addActionListener(e -> toggleSelectedVisibility());
        JButton editTextButton = new JButton("Renombrar...");
        editTextButton.addActionListener(e -> editSelectedLegendTexts());
        JButton resetButton = new JButton("Restaurar auto");
        resetButton.addActionListener(e -> resetLegendModel());
        leftButtons.add(upButton);
        leftButtons.add(downButton);
        leftButtons.add(toggleButton);
        leftButtons.add(editTextButton);
        leftButtons.add(resetButton);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton symbologyButton = new JButton("Editar simbologia...");
        symbologyButton.addActionListener(e -> openLayerSymbology());
        JButton thematicButton = new JButton("Tematica...");
        thematicButton.addActionListener(e -> openLayerThematic());
        rightButtons.add(symbologyButton);
        rightButtons.add(thematicButton);

        JPanel commandPanel = new JPanel(new BorderLayout());
        commandPanel.add(leftButtons, BorderLayout.WEST);
        commandPanel.add(rightButtons, BorderLayout.EAST);

        JPanel footer = new JPanel(new BorderLayout());
        footer.add(commandPanel, BorderLayout.WEST);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton applyButton = new JButton("Aplicar");
        applyButton.addActionListener(e -> applyAndClose());
        JButton cancelButton = new JButton("Cerrar");
        cancelButton.addActionListener(e -> dispose());
        actionButtons.add(applyButton);
        actionButtons.add(cancelButton);
        footer.add(actionButtons, BorderLayout.EAST);

        add(form, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, applyButton, this::dispose);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    public static void open(Window owner, Project project, List<Layer> visibleLayers, Runnable onApply) {
        CatmapLegendEditorDialog dialog = new CatmapLegendEditorDialog(owner, project, visibleLayers);
        dialog.applyCallback = onApply;
        dialog.setVisible(true);
    }

    private void addField(JPanel panel, GridBagConstraints gc, String labelText, java.awt.Component field) {
        panel.add(new JLabel(labelText + ":"), gc);
        gc.gridy++;
        panel.add(field, gc);
    }

    private void resetLegendModel() {
        legendModel.clear();
        List<CatmapLegendItem> merged = CatmapLegendSupport.mergeEntries(
                CatmapLegendSupport.buildAutomaticEntries(visibleLayers),
                project != null ? project.getCatmapLegendItems() : null
        );
        for (CatmapLegendItem item : merged) {
            if (item != null) {
                legendModel.addElement(new CatmapLegendItem(item));
            }
        }
        if (!legendModel.isEmpty()) {
            legendList.setSelectedIndex(Math.min(Math.max(legendList.getSelectedIndex(), 0), legendModel.size() - 1));
        }
    }

    private void moveSelected(int delta) {
        int index = legendList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un item de la leyenda.");
            return;
        }
        int target = index + delta;
        if (target < 0 || target >= legendModel.size()) {
            return;
        }
        CatmapLegendItem item = legendModel.remove(index);
        legendModel.add(target, item);
        legendList.setSelectedIndex(target);
        legendList.ensureIndexIsVisible(target);
    }

    private void toggleSelectedVisibility() {
        CatmapLegendItem item = legendList.getSelectedValue();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un item de la leyenda.");
            return;
        }
        item.setVisible(!item.isVisible());
        legendList.repaint();
    }

    private void editSelectedLegendTexts() {
        CatmapLegendItem item = legendList.getSelectedValue();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un item de la leyenda.");
            return;
        }
        JTextField labelEditor = new JTextField(item.getLabel(), 26);
        JTextField subtitleEditor = new JTextField(item.getSubtitle(), 26);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        panel.add(new JLabel("Nombre del item:"), gc);
        gc.gridy++;
        panel.add(labelEditor, gc);
        gc.gridy++;
        panel.add(new JLabel("Detalle / subtitulo:"), gc);
        gc.gridy++;
        panel.add(subtitleEditor, gc);
        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Editar item de leyenda",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            item.setLabel(labelEditor.getText());
            item.setSubtitle(subtitleEditor.getText());
            legendList.repaint();
        }
    }

    private Layer resolveSelectedLayer() {
        CatmapLegendItem item = legendList.getSelectedValue();
        if (item == null) {
            return null;
        }
        for (Layer layer : visibleLayers) {
            if (layer == null) {
                continue;
            }
            if (layer.getPointCategorizedSymbology().isConfigured()) {
                for (CategoryStyleRule rule : layer.getPointCategorizedSymbology().getRules().values()) {
                    if (item.getKey().equals(CatmapLegendSupport.buildKey(layer, rule, "POINT"))) {
                        return layer;
                    }
                }
            }
            if (layer.getLineCategorizedSymbology().isConfigured()) {
                for (CategoryStyleRule rule : layer.getLineCategorizedSymbology().getRules().values()) {
                    if (item.getKey().equals(CatmapLegendSupport.buildKey(layer, rule, "LINE"))) {
                        return layer;
                    }
                }
            }
            if (layer.getPolygonCategorizedSymbology().isConfigured()) {
                for (CategoryStyleRule rule : layer.getPolygonCategorizedSymbology().getRules().values()) {
                    if (item.getKey().equals(CatmapLegendSupport.buildKey(layer, rule, "POLYGON"))) {
                        return layer;
                    }
                }
            }
            if (item.getKey().equals(CatmapLegendSupport.buildKey(layer, null, CatmapLegendSupport.resolveLegendGeometryType(layer)))) {
                return layer;
            }
        }
        return null;
    }

    private void openLayerSymbology() {
        Layer layer = resolveSelectedLayer();
        if (layer == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un item vinculado a una capa.");
            return;
        }
        LayerPropertiesDialog.open(layer);
        legendList.repaint();
        if (applyCallback != null) {
            applyCallback.run();
        }
    }

    private void openLayerThematic() {
        Layer layer = resolveSelectedLayer();
        if (layer == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un item vinculado a una capa.");
            return;
        }
        CategorizedSymbologyDialog.open(layer);
        resetLegendModel();
        if (applyCallback != null) {
            applyCallback.run();
        }
    }

    private List<CatmapLegendItem> copyLegendItems() {
        List<CatmapLegendItem> items = new ArrayList<>();
        for (int i = 0; i < legendModel.size(); i++) {
            CatmapLegendItem item = legendModel.get(i);
            if (item != null) {
                items.add(new CatmapLegendItem(item));
            }
        }
        return items;
    }

    private void applyAndClose() {
        if (project != null) {
            project.setLegendTitle(titleField.getText());
            project.setLegendSubtitle(subtitleField.getText());
            project.setCatmapLegendItems(copyLegendItems());
            CatgisDesktopApp.markProjectDirty();
        }
        if (applyCallback != null) {
            applyCallback.run();
        }
        dispose();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
