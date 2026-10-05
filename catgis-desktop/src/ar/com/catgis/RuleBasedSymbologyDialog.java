package ar.com.catgis;

import ar.com.catgis.core.model.Layer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for editing rule-based symbology rules.
 * <p>
 * Provides a list-based UI to manage RuleBasedStyleRule instances with their
 * expression filters, styles, scale ranges, and nesting.
 */
public class RuleBasedSymbologyDialog extends JDialog {

    private final Layer layer;
    private final boolean pointMode;
    private final boolean polygonMode;
    private final RuleBasedSymbology workingCopy;

    private final DefaultListModel<RuleBasedStyleRule> listModel = new DefaultListModel<>();
    private final JList<RuleBasedStyleRule> ruleList;
    private final JButton addButton = new JButton("Agregar regla");
    private final JButton removeButton = new JButton("Eliminar");
    private final JButton editButton = new JButton("Editar...");
    private final JButton addChildButton = new JButton("Agregar hija");
    private final JButton upButton = new JButton("\u25B2");
    private final JButton downButton = new JButton("\u25BC");
    private final JCheckBox enabledCheck = new JCheckBox("Habilitar simbología por reglas");

    public RuleBasedSymbologyDialog(Window owner, Layer layer, String geomType) {
        super(owner, "Simbología por reglas", ModalityType.APPLICATION_MODAL);
        this.layer = layer;
        this.pointMode = "point".equals(geomType);
        this.polygonMode = "polygon".equals(geomType) || "polygon".equals(geomType);

        RuleBasedSymbology original = getOriginalSymbology();
        this.workingCopy = new RuleBasedSymbology();
        this.workingCopy.setEnabled(original.isEnabled());
        this.workingCopy.setDescription(original.getDescription());
        for (RuleBasedStyleRule rule : original.getRules()) {
            this.workingCopy.addRule(rule.copy());
        }

        setLayout(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(700, 500));

        // Top: enable checkbox + description
        JPanel topPanel = new JPanel(new BorderLayout(6, 6));
        topPanel.setBorder(new EmptyBorder(8, 8, 4, 8));
        enabledCheck.setSelected(workingCopy.isEnabled());
        topPanel.add(enabledCheck, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Center: rule list
        ruleList = new JList<>(listModel);
        ruleList.setCellRenderer(new RuleListRenderer());
        ruleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshList();
        JScrollPane scroll = new JScrollPane(ruleList);
        scroll.setBorder(BorderFactory.createTitledBorder("Reglas"));
        add(scroll, BorderLayout.CENTER);

        // Bottom: buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        buttonPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

        addButton.addActionListener(e -> addRule());
        removeButton.addActionListener(e -> removeRule());
        editButton.addActionListener(e -> editRule());
        addChildButton.addActionListener(e -> addChildRule());
        upButton.addActionListener(e -> moveRule(-1));
        downButton.addActionListener(e -> moveRule(1));

        JButton okButton = new JButton("Aceptar");
        okButton.addActionListener(e -> { applyChanges(); dispose(); });
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(addChildButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createHorizontalStrut(12));
        buttonPanel.add(upButton);
        buttonPanel.add(downButton);
        buttonPanel.add(Box.createHorizontalStrut(12));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    // ─── Rule CRUD ──────────────────────────────────────────────────────

    private void addRule() {
        RuleBasedStyleRule rule = new RuleBasedStyleRule("Nueva regla");
        rule.setPrimaryColor(randomColor());
        if (editRuleProperties(rule, null)) {
            workingCopy.addRule(rule);
            refreshList();
            ruleList.setSelectedValue(rule, true);
        }
    }

    private void addChildRule() {
        RuleBasedStyleRule parent = ruleList.getSelectedValue();
        if (parent == null) {
            NotificationManager.warn(this, null, "Seleccione una regla padre primero.");
            return;
        }
        RuleBasedStyleRule child = new RuleBasedStyleRule("Sub-regla");
        child.setPrimaryColor(randomColor());
        if (editRuleProperties(child, parent)) {
            parent.addChild(child);
            refreshList();
        }
    }

    private void removeRule() {
        int idx = ruleList.getSelectedIndex();
        if (idx < 0) return;
        RuleBasedStyleRule rule = listModel.getElementAt(idx);
        if (rule.getParentRule() != null) {
            rule.getParentRule().removeChild(rule);
        } else {
            workingCopy.removeRule(rule);
        }
        refreshList();
    }

    private void editRule() {
        RuleBasedStyleRule rule = ruleList.getSelectedValue();
        if (rule == null) return;
        RuleBasedStyleRule parent = findParent(rule, workingCopy.getRules());
        editRuleProperties(rule, parent);
        refreshList();
    }

    private void moveRule(int direction) {
        RuleBasedStyleRule rule = ruleList.getSelectedValue();
        if (rule == null) return;
        List<RuleBasedStyleRule> container = findContainer(rule);
        if (container == null) return;
        int idx = container.indexOf(rule);
        int target = idx + direction;
        if (target < 0 || target >= container.size()) return;
        container.remove(idx);
        container.add(target, rule);
        refreshList();
        ruleList.setSelectedValue(rule, true);
    }

    // ─── Rule Editor Dialog ─────────────────────────────────────────────

    private boolean editRuleProperties(RuleBasedStyleRule rule, RuleBasedStyleRule parent) {
        JTextField descField = new JTextField(rule.getDescription(), 25);
        JTextField filterField = new JTextField(rule.getFilterExpression(), 25);
        JSpinner scaleMinSpinner = new JSpinner(new SpinnerNumberModel(rule.getScaleMin(), 0, 1e9, 1000));
        JSpinner scaleMaxSpinner = new JSpinner(new SpinnerNumberModel(rule.getScaleMax(), 0, 1e9, 1000));
        JCheckBox elseCheck = new JCheckBox("Regla 'else' (captura todo lo no cubierto)", rule.isElseRule());

        JButton colorButton = new JButton();
        colorButton.setBackground(rule.getPrimaryColor());
        colorButton.setOpaque(true);
        colorButton.setPreferredSize(new Dimension(40, 24));
        colorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Color", rule.getPrimaryColor());
            if (c != null) { rule.setPrimaryColor(c); colorButton.setBackground(c); }
        });

        // Point-specific: symbol style and size
        JComboBox<Layer.PointSymbolStyle> pointStyleCombo = null;
        JSpinner pointSizeSpinner = null;
        if (pointMode) {
            pointStyleCombo = new JComboBox<>(Layer.PointSymbolStyle.values());
            pointStyleCombo.setSelectedItem(rule.getPointSymbolStyle());
            pointSizeSpinner = new JSpinner(new SpinnerNumberModel(rule.getPointSize(), 2, 80, 1));
        }

        // Line-specific: line style and width
        JComboBox<Layer.LineSymbolStyle> lineStyleCombo = null;
        JSpinner lineWidthSpinner = null;
        if (!pointMode && !polygonMode) {
            lineStyleCombo = new JComboBox<>(Layer.LineSymbolStyle.values());
            lineStyleCombo.setSelectedItem(rule.getLineStyle());
            lineWidthSpinner = new JSpinner(new SpinnerNumberModel((double) rule.getLineWidth(), 0.5, 20.0, 0.5));
        }

        // Polygon-specific: fill style
        JComboBox<Layer.PolygonFillStyle> polyStyleCombo = null;
        if (polygonMode) {
            polyStyleCombo = new JComboBox<>(Layer.PolygonFillStyle.values());
            polyStyleCombo.setSelectedItem(rule.getPolygonFillStyle());
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        c.gridy = row++;
        panel.add(new JLabel("Descripción:"), c);
        c.gridx = 1; panel.add(descField, c); c.gridx = 0;

        c.gridy = row++;
        panel.add(new JLabel("Expresión de filtro:"), c);
        c.gridx = 1; panel.add(filterField, c); c.gridx = 0;

        c.gridy = row++;
        panel.add(new JLabel("Color:"), c);
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        colorPanel.add(colorButton);
        c.gridx = 1; panel.add(colorPanel, c); c.gridx = 0;

        if (pointStyleCombo != null) {
            c.gridy = row++;
            panel.add(new JLabel("Estilo punto:"), c);
            c.gridx = 1; panel.add(pointStyleCombo, c); c.gridx = 0;

            c.gridy = row++;
            panel.add(new JLabel("Tamaño punto:"), c);
            c.gridx = 1; panel.add(pointSizeSpinner, c); c.gridx = 0;
        }

        if (lineStyleCombo != null) {
            c.gridy = row++;
            panel.add(new JLabel("Estilo línea:"), c);
            c.gridx = 1; panel.add(lineStyleCombo, c); c.gridx = 0;

            c.gridy = row++;
            panel.add(new JLabel("Grosor línea:"), c);
            c.gridx = 1; panel.add(lineWidthSpinner, c); c.gridx = 0;
        }

        if (polyStyleCombo != null) {
            c.gridy = row++;
            panel.add(new JLabel("Relleno polígono:"), c);
            c.gridx = 1; panel.add(polyStyleCombo, c); c.gridx = 0;
        }

        c.gridy = row++;
        panel.add(new JLabel("Escala mínima (1:):"), c);
        c.gridx = 1; panel.add(scaleMinSpinner, c); c.gridx = 0;

        c.gridy = row++;
        panel.add(new JLabel("Escala máxima (1:):"), c);
        c.gridx = 1; panel.add(scaleMaxSpinner, c); c.gridx = 0;

        c.gridy = row++;
        c.gridwidth = 2; panel.add(elseCheck, c); c.gridwidth = 1;

        int result = JOptionPane.showConfirmDialog(this, panel,
                parent != null ? "Editar sub-regla" : "Editar regla",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            rule.setDescription(descField.getText().trim());
            rule.setFilterExpression(filterField.getText().trim());
            rule.setScaleMin((Double) scaleMinSpinner.getValue());
            rule.setScaleMax((Double) scaleMaxSpinner.getValue());
            rule.setElseRule(elseCheck.isSelected());

            if (pointStyleCombo != null) {
                rule.setPointSymbolStyle((Layer.PointSymbolStyle) pointStyleCombo.getSelectedItem());
                rule.setPointSize((Integer) pointSizeSpinner.getValue());
            }
            if (lineStyleCombo != null) {
                rule.setLineStyle((Layer.LineSymbolStyle) lineStyleCombo.getSelectedItem());
                rule.setLineWidth(((Double) lineWidthSpinner.getValue()).floatValue());
            }
            if (polyStyleCombo != null) {
                rule.setPolygonFillStyle((Layer.PolygonFillStyle) polyStyleCombo.getSelectedItem());
            }
            return true;
        }
        return false;
    }

    // ─── Helpers ────────────────────────────────────────────────────────

    private RuleBasedSymbology getOriginalSymbology() {
        if (pointMode) return layer.getPointRuleBasedSymbology();
        if (polygonMode) return layer.getPolygonRuleBasedSymbology();
        return layer.getLineRuleBasedSymbology();
    }

    private void applyChanges() {
        RuleBasedSymbology target = getOriginalSymbology();
        target.setEnabled(enabledCheck.isSelected());
        target.getRules().clear();
        for (RuleBasedStyleRule rule : workingCopy.getRules()) {
            target.addRule(rule.copy());
        }
    }

    private void refreshList() {
        listModel.clear();
        addRulesToModel(workingCopy.getRules(), 0);
    }

    private void addRulesToModel(List<RuleBasedStyleRule> rules, int depth) {
        for (RuleBasedStyleRule rule : rules) {
            listModel.addElement(rule);
            if (rule.hasChildren()) {
                addRulesToModel(rule.getChildren(), depth + 1);
            }
        }
    }

    private RuleBasedStyleRule findParent(RuleBasedStyleRule target, List<RuleBasedStyleRule> rules) {
        for (RuleBasedStyleRule rule : rules) {
            if (rule.getChildren().contains(target)) return rule;
            RuleBasedStyleRule found = findParent(target, rule.getChildren());
            if (found != null) return found;
        }
        return null;
    }

    private List<RuleBasedStyleRule> findContainer(RuleBasedStyleRule rule) {
        if (workingCopy.getRules().contains(rule)) return workingCopy.getRules();
        return findChildContainer(rule, workingCopy.getRules());
    }

    private List<RuleBasedStyleRule> findChildContainer(RuleBasedStyleRule target, List<RuleBasedStyleRule> rules) {
        for (RuleBasedStyleRule rule : rules) {
            if (rule.getChildren().contains(target)) return rule.getChildren();
            List<RuleBasedStyleRule> found = findChildContainer(target, rule.getChildren());
            if (found != null) return found;
        }
        return null;
    }

    private Color randomColor() {
        float hue = (float) Math.random();
        return Color.getHSBColor(hue, 0.6f, 0.8f);
    }

    // ─── List Renderer ──────────────────────────────────────────────────

    private static class RuleListRenderer extends JPanel implements ListCellRenderer<RuleBasedStyleRule> {
        private final JLabel colorLabel = new JLabel();
        private final JLabel descLabel = new JLabel();
        private final JLabel filterLabel = new JLabel();
        private final JLabel depthLabel = new JLabel();
        private final JPanel textPanel = new JPanel(new BorderLayout(4, 0));

        RuleListRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBorder(new EmptyBorder(3, 3, 3, 3));
            colorLabel.setPreferredSize(new Dimension(24, 24));
            colorLabel.setOpaque(true);
            descLabel.setFont(descLabel.getFont().deriveFont(Font.BOLD));
            filterLabel.setFont(filterLabel.getFont().deriveFont(Font.ITALIC, 10f));
            filterLabel.setForeground(Color.GRAY);
            add(colorLabel);
            add(Box.createHorizontalStrut(6));
            textPanel.setOpaque(false);
            textPanel.add(descLabel, BorderLayout.WEST);
            textPanel.add(filterLabel, BorderLayout.CENTER);
            textPanel.add(depthLabel, BorderLayout.EAST);
            add(textPanel);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends RuleBasedStyleRule> list,
                                                      RuleBasedStyleRule value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            colorLabel.setBackground(value.getPrimaryColor());
            colorLabel.setBorder(BorderFactory.createLineBorder(value.getPrimaryColor().darker()));
            descLabel.setText(value.getDescription());
            String filter = value.getFilterExpression();
            if (filter != null && !filter.isBlank()) {
                filterLabel.setText("  " + filter);
                filterLabel.setVisible(true);
            } else {
                filterLabel.setVisible(false);
            }
            if (value.isElseRule()) {
                descLabel.setText(descLabel.getText() + " [ELSE]");
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                textPanel.setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                textPanel.setBackground(list.getBackground());
            }
            setEnabled(list.isEnabled());
            return this;
        }
    }
}
