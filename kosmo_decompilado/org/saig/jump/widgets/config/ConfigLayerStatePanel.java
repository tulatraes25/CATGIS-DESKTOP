/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class ConfigLayerStatePanel
extends JPanel {
    private WorkbenchContext context;
    private JTree projectTree;
    private ViewCellRenderer taskCellRenderer;
    private Hashtable layerState = new Hashtable();
    private Hashtable layerTask = new Hashtable();
    private Hashtable layerCategory = new Hashtable();

    public ConfigLayerStatePanel(WorkbenchContext context) {
        this.context = context;
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new FlowLayout());
        String title = this.context.getProject().getName();
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode(title);
        List<TaskFrame> tasks = this.context.getTaskManager().getTasks();
        Iterator<TaskFrame> iter = tasks.iterator();
        while (iter.hasNext()) {
            Task task = iter.next().getTask();
            DefaultMutableTreeNode taskNode = new DefaultMutableTreeNode(task);
            this.createLayerNodes(taskNode, task.getLayerManager().getLayers());
            this.createLayerNodes(taskNode, task.getLayerManager().getLayerables(WMSLayer.class));
            raiz.add(taskNode);
        }
        DefaultTreeModel modeloArbol = new DefaultTreeModel(raiz);
        this.projectTree = new JTree(modeloArbol);
        this.taskCellRenderer = new ViewCellRenderer(title);
        this.projectTree.setCellRenderer(this.taskCellRenderer);
        this.projectTree.getSelectionModel().setSelectionMode(1);
        this.projectTree.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                ConfigLayerStatePanel.this.handleCheckBoxClick(e);
            }
        });
        JScrollPane panelScro = new JScrollPane();
        panelScro.getViewport().add(this.projectTree);
        panelScro.setSize(new Dimension(200, 300));
        panelScro.setMinimumSize(new Dimension(200, 300));
        panelScro.setPreferredSize(new Dimension(200, 300));
        this.add(panelScro);
    }

    private void createLayerNodes(DefaultMutableTreeNode mapNode, List layers) {
        for (Layerable layer : layers) {
            this.layerState.put(layer, layer.isEnabled());
            this.layerTask.put(layer, mapNode.getUserObject());
            mapNode.add(new DefaultMutableTreeNode(layer));
        }
    }

    private void handleCheckBoxClick(MouseEvent e) {
        TreePath path = this.projectTree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        if (!(node.getUserObject() instanceof Layerable)) {
            return;
        }
        Layerable layer = (Layerable)node.getUserObject();
        Point layerNodeLocation = this.projectTree.getUI().getPathBounds(this.projectTree, path).getLocation();
        this.taskCellRenderer.getStyleRenderer().getTreeCellRendererComponent(this.projectTree, path.getLastPathComponent(), false, false, false, 0, false);
        Rectangle checkBoxBounds = this.taskCellRenderer.getStyleRenderer().getCheckBoxBounds();
        checkBoxBounds.translate((int)layerNodeLocation.getX(), (int)layerNodeLocation.getY());
        if (checkBoxBounds.contains(e.getPoint())) {
            Boolean state = (Boolean)this.layerState.get(layer);
            state = state == false;
            this.layerState.put(layer, state);
            this.projectTree.repaint();
        }
    }

    public Hashtable getLayerState() {
        return this.layerState;
    }

    public void setLayerState(Hashtable layerState) {
        this.layerState = layerState;
    }

    public Hashtable getLayerTask() {
        return this.layerTask;
    }

    public void setLayerTask(Hashtable layerTask) {
        this.layerTask = layerTask;
    }

    private class LayerRenderer
    extends JPanel
    implements ListCellRenderer,
    TreeCellRenderer {
        protected JCheckBox checkBox = new JCheckBox();
        GridBagLayout gridBagLayout = new GridBagLayout();
        protected JLabel label = new JLabel();
        private DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();
        private Font font = new JLabel().getFont();

        public LayerRenderer() {
            try {
                this.jbInit();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public JLabel getLabel() {
            return this.label;
        }

        public Rectangle getCheckBoxBounds() {
            int i = this.gridBagLayout.getConstraints((Component)this.checkBox).gridx;
            int x = 0;
            int j = 0;
            while (j < i) {
                x += this.getColumnWidth(j);
                ++j;
            }
            return new Rectangle(x, 0, this.getColumnWidth(i), this.getRowHeight());
        }

        protected int getColumnWidth(int i) {
            this.validateTree();
            return this.gridBagLayout.getLayoutDimensions()[0][i];
        }

        protected int getRowHeight() {
            this.validateTree();
            return this.gridBagLayout.getLayoutDimensions()[1][0];
        }

        public void setCheckBoxVisible(boolean checkBoxVisible) {
            this.checkBox.setVisible(checkBoxVisible);
        }

        @Override
        public void setBounds(int x, int y, int w, int h) {
            super.setBounds(x, y, w, h);
            this.validate();
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return this.defaultListCellRenderer.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
            }
            Layerable layer = (Layerable)value;
            this.label.setText(layer.getName());
            if (isSelected) {
                this.label.setForeground(list.getSelectionForeground());
                this.label.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
                this.setBackground(list.getSelectionBackground());
            } else {
                this.label.setForeground(list.getForeground());
                this.label.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
                this.setBackground(list.getBackground());
            }
            Boolean enabled = (Boolean)ConfigLayerStatePanel.this.layerState.get(layer);
            this.checkBox.setSelected(enabled);
            this.label.setFont(this.font);
            return this;
        }

        private JList list(JTree tree) {
            JList list = new JList();
            list.setForeground(tree.getForeground());
            list.setBackground(tree.getBackground());
            list.setSelectionForeground(UIManager.getColor("Tree.selectionForeground"));
            list.setSelectionBackground(UIManager.getColor("Tree.selectionBackground"));
            return list;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Layerable layer = (Layerable)((DefaultMutableTreeNode)value).getUserObject();
            this.getListCellRendererComponent(this.list(tree), layer, -1, selected, hasFocus);
            if (selected) {
                this.label.setForeground(UIManager.getColor("Tree.selectionForeground"));
                this.label.setBackground(UIManager.getColor("Tree.selectionBackground"));
                this.setForeground(UIManager.getColor("Tree.selectionForeground"));
                this.setBackground(UIManager.getColor("Tree.selectionBackground"));
            } else {
                this.label.setForeground(tree.getForeground());
                this.label.setBackground(tree.getBackground());
                this.setForeground(tree.getForeground());
                this.setBackground(tree.getBackground());
            }
            return this;
        }

        void jbInit() throws Exception {
            this.checkBox.setVisible(false);
            this.setLayout(this.gridBagLayout);
            this.label.setOpaque(false);
            this.label.setText("");
            this.checkBox.setOpaque(false);
            this.add((Component)this.checkBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 2), 0, 0));
            this.add((Component)this.label, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 5), 0, 0));
        }
    }

    private class ViewCellRenderer
    implements TreeCellRenderer {
        private LayerRenderer layerRenderer;
        private ViewRenderer viewRenderer;
        private String title;
        private JLabel rootRendererComponent;

        public ViewCellRenderer(String title) {
            this.layerRenderer = new LayerRenderer();
            this.viewRenderer = new ViewRenderer();
            this.rootRendererComponent = new JLabel();
            this.title = title;
            this.rootRendererComponent.setText(title);
            this.layerRenderer.setCheckBoxVisible(true);
        }

        public LayerRenderer getStyleRenderer() {
            return this.layerRenderer;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object node = ((DefaultMutableTreeNode)value).getUserObject();
            if (node instanceof Layerable) {
                return this.layerRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }
            if (node instanceof Task) {
                return this.viewRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }
            this.rootRendererComponent.setText((String)node);
            Font newFont = this.rootRendererComponent.getFont().deriveFont(1);
            this.rootRendererComponent.setFont(newFont);
            if (expanded) {
                this.rootRendererComponent.setIcon(UIManager.getIcon("Tree.openIcon"));
            } else {
                this.rootRendererComponent.setIcon(UIManager.getIcon("Tree.closedIcon"));
            }
            return this.rootRendererComponent;
        }
    }

    private class ViewRenderer
    extends JLabel
    implements TreeCellRenderer {
        private boolean seleccionado;

        @Override
        public Component getTreeCellRendererComponent(JTree arbol, Object valor, boolean seleccionado, boolean expandido, boolean rama, int fila, boolean conFoco) {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode)valor;
            Task node = (Task)nodo.getUserObject();
            this.seleccionado = seleccionado;
            if (!seleccionado) {
                this.setForeground(Color.black);
            } else {
                this.setForeground(Color.white);
            }
            this.setText(node.getName());
            if (expandido) {
                this.setIcon(UIManager.getIcon("Tree.openIcon"));
            } else {
                this.setIcon(UIManager.getIcon("Tree.closedIcon"));
            }
            return this;
        }

        @Override
        public void paint(Graphics g) {
            Color color = this.seleccionado ? Color.red : Color.white;
            g.setColor(color);
            g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            super.paint(g);
        }
    }
}

