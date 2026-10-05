package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CatserverLayerSelectionDialog extends JDialog {

    private final DefaultMutableTreeNode rootNode;
    private final DefaultTreeModel treeModel;
    private final JTree layerTree;
    private final JButton selectSchemaButton;
    private final JButton clearSchemaButton;
    private final JButton loadButton;
    private List<PostgisFeatureTypeInfo> selectedLayers = List.of();

    private CatserverLayerSelectionDialog(Window owner, List<PostgisFeatureTypeInfo> layers) {
        super(owner, "Elegir capas de CATSERVER", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setResizable(true);

        rootNode = new DefaultMutableTreeNode("root");
        treeModel = new DefaultTreeModel(rootNode);
        layerTree = new JTree(treeModel);
        layerTree.setRootVisible(false);
        layerTree.setShowsRootHandles(true);
        layerTree.setRowHeight(0);
        layerTree.setCellRenderer(new SchemaLayerTreeRenderer());
        layerTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        layerTree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        layerTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath path = layerTree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }
                layerTree.setSelectionPath(path);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObject = node.getUserObject();
                if (userObject instanceof LayerNodeState) {
                    toggleLayerNode(node);
                    return;
                }
                if (userObject instanceof SchemaNodeState) {
                    if (layerTree.isExpanded(path)) {
                        layerTree.collapsePath(path);
                    } else {
                        layerTree.expandPath(path);
                    }
                }
            }
        });
        layerTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    toggleSelectedTreeNode();
                    e.consume();
                }
            }
        });
        layerTree.addTreeSelectionListener(e -> {
            updateSchemaButtonsState();
            updateLoadButton();
        });
        layerTree.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "catserver.accept");
        layerTree.getActionMap().put("catserver.accept", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                acceptSelection();
            }
        });

        JButton recommendedButton = new JButton("Marcar recomendadas");
        recommendedButton.addActionListener(e -> applyRecommendedSelection());

        JButton selectAllButton = new JButton("Seleccionar todo");
        selectAllButton.addActionListener(e -> applySelectionToAllLayers(true));

        JButton clearButton = new JButton("Limpiar");
        clearButton.addActionListener(e -> applySelectionToAllLayers(false));

        JButton expandAllButton = new JButton("Expandir todo");
        expandAllButton.addActionListener(e -> expandAllSchemas());
        JButton collapseAllButton = new JButton("Contraer todo");
        collapseAllButton.addActionListener(e -> collapseAllSchemas());

        selectSchemaButton = new JButton("Seleccionar esquema");
        selectSchemaButton.addActionListener(e -> applySelectionToCurrentSchema(true));
        clearSchemaButton = new JButton("Deseleccionar esquema");
        clearSchemaButton.addActionListener(e -> applySelectionToCurrentSchema(false));

        loadButton = new JButton("Cargar seleccionadas");
        loadButton.addActionListener(e -> acceptSelection());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JLabel title = new JLabel("Elegi que capas queres incorporar al proyecto", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(new Color(28, 45, 74));

        JLabel help = new JLabel("<html><div style='color:#556579;'>Marca con tilde solo las capas que quieras cargar ahora. "
                + "Cada esquema se despliega al hacer click y las capas marcadas se ven en negrita.</div></html>");

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        top.setOpaque(true);
        top.setBackground(Color.WHITE);
        top.add(title, BorderLayout.NORTH);
        top.add(help, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(layerTree);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 12, 0, 12),
                BorderFactory.createLineBorder(new Color(220, 226, 236))
        ));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setPreferredSize(new Dimension(760, 380));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(expandAllButton);
        actions.add(collapseAllButton);
        actions.add(selectSchemaButton);
        actions.add(clearSchemaButton);
        actions.add(Box.createHorizontalStrut(12));
        actions.add(recommendedButton);
        actions.add(selectAllButton);
        actions.add(clearButton);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        footer.add(actions, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(loadButton);
        right.add(cancelButton);
        footer.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, loadButton, this::dispose);
        populateTree(layers);
        expandAllSchemas();
        if (rootNode.getChildCount() > 0) {
            DefaultMutableTreeNode first = (DefaultMutableTreeNode) rootNode.getFirstChild();
            if (first != null) {
                layerTree.setSelectionPath(new TreePath(first.getPath()));
            }
        }
        updateSchemaButtonsState();
        updateLoadButton();
        pack();
        WindowLayoutSupport.fitDialogToScreen(this, 880, 660, 760, 520);
        setLocationRelativeTo(owner);
    }

    public static List<PostgisFeatureTypeInfo> choose(Window owner, List<PostgisFeatureTypeInfo> layers) {
        CatserverLayerSelectionDialog dialog = new CatserverLayerSelectionDialog(owner, layers);
        dialog.setVisible(true);
        return dialog.selectedLayers;
    }

    private void populateTree(List<PostgisFeatureTypeInfo> layers) {
        rootNode.removeAllChildren();
        Map<String, List<PostgisFeatureTypeInfo>> grouped = new LinkedHashMap<>();
        if (layers != null) {
            for (PostgisFeatureTypeInfo layer : layers) {
                if (layer == null) {
                    continue;
                }
                String schema = normalizeSchemaName(layer.getSchemaName());
                grouped.computeIfAbsent(schema, ignored -> new ArrayList<>()).add(layer);
            }
        }

        for (Map.Entry<String, List<PostgisFeatureTypeInfo>> entry : grouped.entrySet()) {
            String schemaName = entry.getKey();
            List<PostgisFeatureTypeInfo> schemaLayers = entry.getValue();
            SchemaNodeState schemaState = new SchemaNodeState(schemaName, schemaLayers.size());
            DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(schemaState);
            int selectedCount = 0;
            for (PostgisFeatureTypeInfo layerInfo : schemaLayers) {
                boolean selected = false;
                if (selected) {
                    selectedCount++;
                }
                schemaNode.add(new DefaultMutableTreeNode(new LayerNodeState(layerInfo, selected), false));
            }
            schemaState.selectedCount = selectedCount;
            rootNode.add(schemaNode);
        }
        treeModel.reload();
    }

    private String normalizeSchemaName(String schemaName) {
        if (schemaName == null || schemaName.isBlank()) {
            return "public";
        }
        return schemaName.trim();
    }

    private void toggleSelectedTreeNode() {
        TreePath path = layerTree.getSelectionPath();
        if (path == null) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();
        if (userObject instanceof LayerNodeState) {
            toggleLayerNode(node);
            return;
        }
        if (userObject instanceof SchemaNodeState schemaState) {
            applySelectionToSchemaNode(node, schemaState.selectedCount < schemaState.layerCount);
        }
    }

    private void toggleLayerNode(DefaultMutableTreeNode layerNode) {
        if (layerNode == null || !(layerNode.getUserObject() instanceof LayerNodeState layerState)) {
            return;
        }
        layerState.selected = !layerState.selected;
        treeModel.nodeChanged(layerNode);
        recountSchemaNode((DefaultMutableTreeNode) layerNode.getParent());
        updateSchemaButtonsState();
        updateLoadButton();
    }

    private void recountSchemaNode(DefaultMutableTreeNode schemaNode) {
        if (schemaNode == null || !(schemaNode.getUserObject() instanceof SchemaNodeState schemaState)) {
            return;
        }
        int selectedCount = 0;
        for (int i = 0; i < schemaNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) schemaNode.getChildAt(i);
            if (child.getUserObject() instanceof LayerNodeState layerState && layerState.selected) {
                selectedCount++;
            }
        }
        schemaState.selectedCount = selectedCount;
        treeModel.nodeChanged(schemaNode);
    }

    private void expandAllSchemas() {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode schemaNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            layerTree.expandPath(new TreePath(schemaNode.getPath()));
        }
    }

    private void collapseAllSchemas() {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode schemaNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            layerTree.collapsePath(new TreePath(schemaNode.getPath()));
        }
    }

    private DefaultMutableTreeNode getSelectedSchemaNode() {
        TreePath path = layerTree.getSelectionPath();
        if (path == null) {
            return null;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof SchemaNodeState) {
            return node;
        }
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent != null && parent.getUserObject() instanceof SchemaNodeState) {
            return parent;
        }
        return null;
    }

    private void applySelectionToCurrentSchema(boolean selected) {
        DefaultMutableTreeNode schemaNode = getSelectedSchemaNode();
        if (schemaNode == null) {
            return;
        }
        applySelectionToSchemaNode(schemaNode, selected);
    }

    private void applySelectionToSchemaNode(DefaultMutableTreeNode schemaNode, boolean selected) {
        if (schemaNode == null) {
            return;
        }
        for (int i = 0; i < schemaNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) schemaNode.getChildAt(i);
            if (child.getUserObject() instanceof LayerNodeState layerState) {
                layerState.selected = selected;
                treeModel.nodeChanged(child);
            }
        }
        recountSchemaNode(schemaNode);
        updateSchemaButtonsState();
        updateLoadButton();
    }

    private void applySelectionToAllLayers(boolean selected) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode schemaNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            applySelectionToSchemaNode(schemaNode, selected);
        }
        updateSchemaButtonsState();
        updateLoadButton();
    }

    private void applyRecommendedSelection() {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode schemaNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            for (int j = 0; j < schemaNode.getChildCount(); j++) {
                DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) schemaNode.getChildAt(j);
                if (layerNode.getUserObject() instanceof LayerNodeState layerState) {
                    layerState.selected = layerState.layer != null && layerState.layer.isLoadByDefault();
                    treeModel.nodeChanged(layerNode);
                }
            }
            recountSchemaNode(schemaNode);
        }
        updateSchemaButtonsState();
        updateLoadButton();
    }

    private void updateSchemaButtonsState() {
        DefaultMutableTreeNode schemaNode = getSelectedSchemaNode();
        boolean hasSchema = schemaNode != null;
        selectSchemaButton.setEnabled(hasSchema);
        boolean hasSelectedLayers = false;
        if (schemaNode != null && schemaNode.getUserObject() instanceof SchemaNodeState schemaState) {
            hasSelectedLayers = schemaState.selectedCount > 0;
        }
        clearSchemaButton.setEnabled(hasSchema && hasSelectedLayers);
    }

    private void updateLoadButton() {
        loadButton.setEnabled(!collectCheckedLayers().isEmpty());
        getRootPane().setDefaultButton(loadButton.isEnabled() ? loadButton : null);
    }

    private List<PostgisFeatureTypeInfo> collectCheckedLayers() {
        List<PostgisFeatureTypeInfo> checked = new ArrayList<>();
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode schemaNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            for (int j = 0; j < schemaNode.getChildCount(); j++) {
                DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) schemaNode.getChildAt(j);
                if (layerNode.getUserObject() instanceof LayerNodeState layerState && layerState.selected && layerState.layer != null) {
                    checked.add(layerState.layer);
                }
            }
        }
        return checked;
    }

    private void acceptSelection() {
        selectedLayers = collectCheckedLayers();
        dispose();
    }

    private static final class SchemaNodeState {
        private final String schemaName;
        private final int layerCount;
        private int selectedCount;

        private SchemaNodeState(String schemaName, int layerCount) {
            this.schemaName = schemaName != null ? schemaName : "public";
            this.layerCount = Math.max(0, layerCount);
            this.selectedCount = 0;
        }

        private String title() {
            return schemaName + " (" + layerCount + ")";
        }
    }

    private static final class LayerNodeState {
        private final PostgisFeatureTypeInfo layer;
        private boolean selected;

        private LayerNodeState(PostgisFeatureTypeInfo layer, boolean selected) {
            this.layer = layer;
            this.selected = selected;
        }
    }

    private static final class SchemaLayerTreeRenderer extends JPanel implements TreeCellRenderer {
        private final JCheckBox checkBox;
        private final JLabel titleLabel;
        private final JLabel detailLabel;

        private SchemaLayerTreeRenderer() {
            setLayout(new BorderLayout(8, 0));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

            checkBox = new JCheckBox();
            checkBox.setOpaque(false);

            titleLabel = new JLabel();
            titleLabel.setOpaque(false);

            detailLabel = new JLabel();
            detailLabel.setOpaque(false);
            detailLabel.setForeground(new Color(95, 107, 122));

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.add(titleLabel);
            textPanel.add(detailLabel);

            add(checkBox, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean selected,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            if (userObject instanceof SchemaNodeState schemaState) {
                boolean fullyChecked = schemaState.layerCount > 0 && schemaState.selectedCount == schemaState.layerCount;
                checkBox.setSelected(fullyChecked);
                titleLabel.setText(schemaState.title());
                titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
                detailLabel.setText(schemaState.selectedCount + " de " + schemaState.layerCount + " capas seleccionadas");
            } else if (userObject instanceof LayerNodeState layerState) {
                PostgisFeatureTypeInfo layer = layerState.layer;
                checkBox.setSelected(layerState.selected);
                String name = (layer != null && layer.getDisplayName() != null && !layer.getDisplayName().isBlank())
                        ? layer.getDisplayName()
                        : (layer != null ? layer.getTableName() : "");
                titleLabel.setText(name);
                titleLabel.setFont(titleLabel.getFont().deriveFont(layerState.selected ? Font.BOLD : Font.PLAIN));

                StringBuilder detail = new StringBuilder();
                if (layer != null) {
                    String schema = layer.getSchemaName() != null && !layer.getSchemaName().isBlank()
                            ? layer.getSchemaName()
                            : "public";
                    detail.append(schema).append(".").append(layer.getTableName());
                    if (layer.getGeometryTypeLabel() != null && !layer.getGeometryTypeLabel().isBlank()) {
                        detail.append(" | ").append(layer.getGeometryTypeLabel());
                    }
                    if (layer.getCrsCode() != null && !layer.getCrsCode().isBlank()) {
                        detail.append(" | ").append(layer.getCrsCode());
                    }
                    detail.append(layer.isWritable() ? " | editable" : " | solo lectura");
                    if (layer.isLoadByDefault()) {
                        detail.append(" | recomendada");
                    }
                }
                detailLabel.setText(detail.toString());
            } else {
                checkBox.setSelected(false);
                titleLabel.setText(String.valueOf(userObject));
                titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN));
                detailLabel.setText("");
            }
            Color bg = selected ? new Color(233, 241, 252) : Color.WHITE;
            setBackground(bg);
            return this;
        }
    }
}
