/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.workbench.ui.plugin.wms;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveListModel;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemovePanel;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.TreeAddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.TreeAddRemoveListModel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerTreeModel;
import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.WMService;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.lang.I18N;

public class MapLayerPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final Icon CATEGORY_ICON = GUIUtil.resize(IconLoader.icon("world_folder.png"), 13);
    public static final ImageIcon ICON = GUIUtil.resize(IconLoader.icon("world.png"), 13);
    public static final ImageIcon WRONG_ICON = GUIUtil.resize(IconLoader.icon("error_world.png"), 13);
    private InputChangedFirer inputChangedFirer = new InputChangedFirer();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private AddRemovePanel<MapLayerTreeModel.LayerNode> addRemovePanel = new AddRemovePanel(true);
    private JCheckBox checkBox = new JCheckBox(I18N.getString("ui.plugin.wms.MapLayerPanel.sort"), true);

    public MapLayerPanel() {
        try {
            this.jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.initAddRemovePanel();
    }

    public List<MapLayer> getChosenMapLayers() {
        ArrayList<MapLayer> mapLayers = new ArrayList<MapLayer>();
        for (MapLayerTreeModel.LayerNode node : this.addRemovePanel.getRightItems()) {
            Assert.isTrue((node.getLayer().getName() != null ? 1 : 0) != 0);
            mapLayers.add(node.getLayer());
        }
        return mapLayers;
    }

    private void setRendererText(JLabel renderer, MapLayer layer) {
        renderer.setText(String.valueOf(layer.getTitle()) + " [" + StringUtil.limitLength(StringUtils.join(layer.getFullSRSList(), (String)", "), 30) + "]");
    }

    void jbInit() throws Exception {
        this.addRemovePanel.setRightText(I18N.getString("ui.plugin.wms.MapLayerPanel.chosen-layers"));
        this.setLayout(this.gridBagLayout1);
        this.add(this.addRemovePanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
    }

    public void add(InputChangedListener listener) {
        this.inputChangedFirer.add(listener);
    }

    public void remove(InputChangedListener listener) {
        this.inputChangedFirer.remove(listener);
    }

    private void addIfOnList(MapLayer layer, AddRemoveListModel<MapLayerTreeModel.LayerNode> model, String name) {
        if (name.equals(layer.getName())) {
            model.add(new MapLayerTreeModel.LayerNode(layer, null));
        }
        for (MapLayer child : layer.getSubLayerList()) {
            this.addIfOnList(child, model, name);
        }
    }

    public List<String> commonSRSList() {
        List<MapLayer> mapLayers = this.getChosenMapLayers();
        if (mapLayers.isEmpty()) {
            return new ArrayList<String>();
        }
        ArrayList<String> commonSRSList = new ArrayList<String>(mapLayers.get(0).getFullSRSList());
        for (MapLayer layer : mapLayers) {
            commonSRSList.retainAll(layer.getFullSRSList());
        }
        return commonSRSList;
    }

    private void initAddRemovePanel() {
        TreeAddRemoveList<MapLayerTreeModel.LayerNode> leftList = new TreeAddRemoveList<MapLayerTreeModel.LayerNode>(){
            private static final long serialVersionUID = 1L;

            @Override
            public List<MapLayerTreeModel.LayerNode> getSelectedItems() {
                ArrayList<MapLayerTreeModel.LayerNode> selectedItems = new ArrayList<MapLayerTreeModel.LayerNode>(super.getSelectedItems());
                Iterator i = selectedItems.iterator();
                while (i.hasNext()) {
                    MapLayerTreeModel.LayerNode node = (MapLayerTreeModel.LayerNode)i.next();
                    if (!node.isContainer()) continue;
                    i.remove();
                }
                return selectedItems;
            }
        };
        this.addRemovePanel.setLeftList((AddRemoveList<MapLayerTreeModel.LayerNode>)leftList);
        leftList.getTree().setCellRenderer(new DefaultTreeCellRenderer(){
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel component = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (!(value instanceof MapLayerTreeModel.LayerNode)) {
                    return component;
                }
                MapLayer layer = ((MapLayerTreeModel.LayerNode)value).getLayer();
                MapLayerPanel.this.setRendererText(component, layer);
                if (layer.getName() == null) {
                    if (layer.getParent() != null) {
                        component.setIcon(CATEGORY_ICON);
                    } else if (expanded) {
                        component.setIcon(UIManager.getIcon("Tree.openIcon"));
                    } else {
                        component.setIcon(UIManager.getIcon("Tree.closedIcon"));
                    }
                } else {
                    component.setIcon(ICON);
                }
                return component;
            }
        });
        DefaultAddRemoveList rightList = new DefaultAddRemoveList(new DefaultListModel(){
            private static final long serialVersionUID = 1L;

            public void addElement(Object obj) {
                if (this.contains(obj)) {
                    return;
                }
                super.addElement(obj);
            }
        });
        this.addRemovePanel.setRightList(rightList);
        rightList.getList().setCellRenderer(new DefaultListCellRenderer(){
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel component = (JLabel)super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
                MapLayer layer = ((MapLayerTreeModel.LayerNode)value).getLayer();
                MapLayerPanel.this.setRendererText(component, layer);
                component.setIcon(ICON);
                return component;
            }
        });
        leftList.add(new InputChangedListener(){

            @Override
            public void inputChanged() {
                MapLayerPanel.this.inputChangedFirer.fire();
            }
        });
        rightList.add(new InputChangedListener(){

            @Override
            public void inputChanged() {
                MapLayerPanel.this.inputChangedFirer.fire();
            }
        });
        rightList.getList().getModel().addListDataListener(new ListDataListener(){

            @Override
            public void intervalAdded(ListDataEvent e) {
                MapLayerPanel.this.inputChangedFirer.fire();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                MapLayerPanel.this.inputChangedFirer.fire();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                MapLayerPanel.this.inputChangedFirer.fire();
            }
        });
        JPanel leftLabelPanel = new JPanel();
        leftLabelPanel.setLayout(new GridBagLayout());
        leftLabelPanel.add((Component)new JLabel(I18N.getString("ui.plugin.wms.MapLayerPanel.available-layers")), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        leftLabelPanel.add((Component)new JPanel(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 17, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.checkBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                MapLayerPanel.this.setSorted(MapLayerPanel.this.checkBox.isSelected());
            }
        });
        leftLabelPanel.add((Component)this.checkBox, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.addRemovePanel.setLeftLabel(leftLabelPanel);
    }

    private void setSorted(boolean isSorted) {
        TreeAddRemoveList tarl = (TreeAddRemoveList)this.addRemovePanel.getLeftList();
        TreeAddRemoveListModel tarlm = (TreeAddRemoveListModel)tarl.getModel();
        MapLayerTreeModel mltm = (MapLayerTreeModel)tarlm.getTreeModel();
        mltm.setSorted(isSorted);
    }

    public void init(WMService service, Collection<String> initialChosenMapLayers) {
        final MapLayerTreeModel treeModel = new MapLayerTreeModel(service.getCapabilities().getTopLayer());
        treeModel.setSorted(this.checkBox.isSelected());
        TreeAddRemoveListModel<MapLayerTreeModel.LayerNode> treeAddRemoveListModel = new TreeAddRemoveListModel<MapLayerTreeModel.LayerNode>((TreeModel)treeModel){

            @Override
            public List<MapLayerTreeModel.LayerNode> getItems() {
                ArrayList<MapLayerTreeModel.LayerNode> items = new ArrayList<MapLayerTreeModel.LayerNode>(MapLayerPanel.this.items((MapLayerTreeModel.LayerNode)treeModel.getRoot()));
                Iterator i = items.iterator();
                while (i.hasNext()) {
                    MapLayerTreeModel.LayerNode node = (MapLayerTreeModel.LayerNode)i.next();
                    if (!node.isContainer()) continue;
                    i.remove();
                }
                return items;
            }
        };
        ((TreeAddRemoveList)this.addRemovePanel.getLeftList()).setModel(treeAddRemoveListModel);
        if (initialChosenMapLayers != null) {
            for (String choosedMapLayer : initialChosenMapLayers) {
                this.addIfOnList(service.getCapabilities().getTopLayer(), this.addRemovePanel.getRightList().getModel(), choosedMapLayer);
            }
        } else {
            this.addRemovePanel.getRightList().getModel().setItems(new ArrayList());
        }
        this.addRemovePanel.updateEnabled();
    }

    private List<MapLayerTreeModel.LayerNode> items(MapLayerTreeModel.LayerNode node) {
        ArrayList<MapLayerTreeModel.LayerNode> items = new ArrayList<MapLayerTreeModel.LayerNode>();
        items.add(node);
        Enumeration<MapLayerTreeModel.LayerNode> e = node.children();
        while (e.hasMoreElements()) {
            MapLayerTreeModel.LayerNode child = e.nextElement();
            items.addAll(this.items(child));
        }
        items.removeAll(this.addRemovePanel.getRightItems());
        return items;
    }
}

