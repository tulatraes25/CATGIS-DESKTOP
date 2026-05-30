/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.widgets.sdi.wms;

import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerPanel;
import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.MapStyle;
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
import java.io.IOException;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;

public class ChangeWMSStylePanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ChangeWMSStylePanel.class);
    private WMSLayer wmsLayer;
    private JTree treeStyle;
    private JLabel legendLabel;
    private WMSCellRenderer wmsCellRenderer;

    public ChangeWMSStylePanel(WMSLayer wmsLayer) {
        this.wmsLayer = wmsLayer;
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new FlowLayout());
        String title = "";
        DefaultMutableTreeNode raiz = null;
        try {
            title = this.wmsLayer.getService().getCapabilities().getTitle();
            raiz = new DefaultMutableTreeNode(title);
        }
        catch (IOException e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
        List<String> layerNames = this.wmsLayer.getLayerNames();
        for (String layerName : layerNames) {
            MapLayer mapLayer = null;
            try {
                mapLayer = this.wmsLayer.getService().getCapabilities().getTopLayer().getMapLayer(layerName);
            }
            catch (IOException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            DefaultMutableTreeNode mapNode = new DefaultMutableTreeNode(mapLayer);
            this.createNodeStyles(mapNode, mapLayer.getStyles());
            raiz.add(mapNode);
        }
        DefaultTreeModel modeloArbol = new DefaultTreeModel(raiz);
        this.treeStyle = new JTree(modeloArbol);
        this.wmsCellRenderer = new WMSCellRenderer(title);
        this.treeStyle.setCellRenderer(this.wmsCellRenderer);
        this.treeStyle.getSelectionModel().setSelectionMode(1);
        this.treeStyle.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                ChangeWMSStylePanel.this.handleCheckBoxClick(e);
            }
        });
        this.treeStyle.addTreeSelectionListener(new TreeSelectionListener(){

            @Override
            public void valueChanged(TreeSelectionEvent arg0) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)ChangeWMSStylePanel.this.treeStyle.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                Object nodeInfo = node.getUserObject();
                if (nodeInfo instanceof MapStyle) {
                    MapStyle style = (MapStyle)nodeInfo;
                    Icon legendIcon = style.getLegendIcon();
                    if (legendIcon != null) {
                        ChangeWMSStylePanel.this.legendLabel.setIcon(legendIcon);
                    } else {
                        ChangeWMSStylePanel.this.legendLabel.setIcon(null);
                    }
                }
            }
        });
        JScrollPane panelScro = new JScrollPane();
        panelScro.getViewport().add(this.treeStyle);
        panelScro.setSize(new Dimension(200, 300));
        panelScro.setMinimumSize(new Dimension(200, 300));
        panelScro.setPreferredSize(new Dimension(200, 300));
        this.add(panelScro);
        this.legendLabel = new JLabel();
        this.legendLabel.setBackground(Color.white);
        this.legendLabel.setHorizontalAlignment(2);
        this.legendLabel.setVerticalAlignment(1);
        JScrollPane panelScro1 = new JScrollPane();
        panelScro1.getViewport().add(this.legendLabel);
        panelScro1.setSize(new Dimension(200, 300));
        panelScro1.setMinimumSize(new Dimension(200, 300));
        panelScro1.setPreferredSize(new Dimension(200, 300));
        this.add(panelScro1);
    }

    private void createNodeStyles(DefaultMutableTreeNode mapNode, List<MapStyle> styles) {
        for (MapStyle style : styles) {
            mapNode.add(new DefaultMutableTreeNode(style));
        }
    }

    private void handleCheckBoxClick(MouseEvent e) {
        TreePath path = this.treeStyle.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        if (!(node.getUserObject() instanceof MapStyle)) {
            return;
        }
        MapStyle style = (MapStyle)node.getUserObject();
        Point layerNodeLocation = this.treeStyle.getUI().getPathBounds(this.treeStyle, path).getLocation();
        this.wmsCellRenderer.getStyleRenderer().getTreeCellRendererComponent(this.treeStyle, path.getLastPathComponent(), false, false, false, 0, false);
        Rectangle checkBoxBounds = this.wmsCellRenderer.getStyleRenderer().getCheckBoxBounds();
        checkBoxBounds.translate((int)layerNodeLocation.getX(), (int)layerNodeLocation.getY());
        if (checkBoxBounds.contains(e.getPoint())) {
            boolean activate;
            boolean bl = activate = !style.isSelected();
            if (activate) {
                style.fireStyleChanged();
            } else {
                style.setSelected(activate, true);
            }
            this.treeStyle.repaint();
        }
    }

    private class MapLayerRenderer
    extends JLabel
    implements TreeCellRenderer {
        private static final long serialVersionUID = 1L;
        private boolean seleccionado;

        @Override
        public Component getTreeCellRendererComponent(JTree arbol, Object valor, boolean seleccionado, boolean expandido, boolean rama, int fila, boolean conFoco) {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode)valor;
            MapLayer node = (MapLayer)nodo.getUserObject();
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

    private class StyleRenderer
    extends JPanel
    implements ListCellRenderer,
    TreeCellRenderer {
        private static final long serialVersionUID = 1L;
        protected JCheckBox checkBox = new JCheckBox();
        GridBagLayout gridBagLayout = new GridBagLayout();
        protected JLabel label = new JLabel();
        private DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();
        private JLabel progressIconLabel = new JLabel(MapLayerPanel.ICON);
        private Font font = new JLabel().getFont();

        public StyleRenderer() {
            try {
                this.jbInit();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
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
            MapStyle style = (MapStyle)value;
            this.label.setText(style.getName());
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
            this.checkBox.setSelected(style.isSelected());
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
            MapStyle mapStyle = (MapStyle)((DefaultMutableTreeNode)value).getUserObject();
            this.getListCellRendererComponent(this.list(tree), mapStyle, -1, selected, hasFocus);
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
            this.label.setText("Layer Name Goes Here");
            this.checkBox.setOpaque(false);
            this.add((Component)this.progressIconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 2), 0, 0));
            this.add((Component)this.checkBox, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 5), 0, 0));
            this.add((Component)this.label, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
        }
    }

    private class WMSCellRenderer
    implements TreeCellRenderer {
        private StyleRenderer styleRenderer;
        private MapLayerRenderer mapLayerRenderer;
        private String title;
        private JLabel rootRendererComponent;

        public WMSCellRenderer(String title) {
            this.styleRenderer = new StyleRenderer();
            this.mapLayerRenderer = new MapLayerRenderer();
            this.rootRendererComponent = new JLabel();
            this.title = title;
            this.rootRendererComponent.setText(title);
            this.styleRenderer.setCheckBoxVisible(true);
        }

        public StyleRenderer getStyleRenderer() {
            return this.styleRenderer;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object node = ((DefaultMutableTreeNode)value).getUserObject();
            if (node instanceof MapStyle) {
                return this.styleRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }
            if (node instanceof MapLayer) {
                return this.mapLayerRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
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
}

