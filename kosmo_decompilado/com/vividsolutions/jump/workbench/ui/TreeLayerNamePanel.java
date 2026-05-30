/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.CategoryEventType;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.FirableTreeModelWrapper;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelListener;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerTreeCellEditor;
import com.vividsolutions.jump.workbench.ui.LayerTreeCellRenderer;
import com.vividsolutions.jump.workbench.ui.PopupNodeProxy;
import com.vividsolutions.jump.workbench.ui.TreeUtil;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.Style;
import org.saig.jump.widgets.scale.ScalePanel;

public class TreeLayerNamePanel
extends JPanel
implements LayerListener,
LayerNamePanel,
LayerNamePanelProxy,
PopupNodeProxy {
    private static final long serialVersionUID = 1L;
    private Map<Class<?>, JPopupMenu> nodeClassToPopupMenuMap = new HashMap();
    private RenderingManager renderingManager;
    protected JTree tree = new JTree(){
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isPathEditable(TreePath path) {
            if (!this.isEditable()) {
                return false;
            }
            return path.getLastPathComponent() instanceof Layerable || path.getLastPathComponent() instanceof Category || path.getLastPathComponent() instanceof Rule;
        }

        @Override
        public boolean hasBeenExpanded(TreePath path) {
            return super.hasBeenExpanded(path) || !this.getModel().isLeaf(path.getLastPathComponent());
        }
    };
    private LayerTreeCellRenderer cellRenderer;
    private TreeCellEditor cellEditor = new LayerTreeCellEditor(this.tree);
    private Object popupNode;
    private List<LayerNamePanelListener> listeners = new ArrayList<LayerNamePanelListener>();
    private LayerManagerProxy layerManagerProxy;
    private JPanel topPanel;
    private int topPosition = 0;
    private ScalePanel scalePanel;
    private JScrollPane scrollPane = new JScrollPane();
    private FirableTreeModelWrapper firableTreeModelWrapper;
    private TreePath movingTreePath = null;
    private boolean firstTimeDragging = true;
    private WorkbenchContext workbenchContext;

    public TreeLayerNamePanel(LayerManagerProxy layerManagerProxy, TreeModel treeModel, RenderingManager renderingManager, Map<Class<?>, TreeCellRenderer> additionalNodeClassToTreeCellRendererMap, WorkbenchContext context) {
        this.renderingManager = renderingManager;
        this.workbenchContext = context;
        layerManagerProxy.getLayerManager().addLayerListener(this);
        this.layerManagerProxy = layerManagerProxy;
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.firableTreeModelWrapper = new FirableTreeModelWrapper(treeModel);
        this.tree.setModel(this.firableTreeModelWrapper);
        this.cellRenderer = new LayerTreeCellRenderer(renderingManager);
        this.setCellRenderer(additionalNodeClassToTreeCellRendererMap);
        this.tree.getSelectionModel().setSelectionMode(4);
        this.tree.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                TreeLayerNamePanel.this.handleCheckBoxClick(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1) {
                    TreeLayerNamePanel.this.movingTreePath = TreeLayerNamePanel.this.tree.getPathForLocation(e.getX(), e.getY());
                } else {
                    TreeLayerNamePanel.this.movingTreePath = null;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() != 1 || TreeLayerNamePanel.this.movingTreePath == null) {
                    return;
                }
                Object node = TreeLayerNamePanel.this.movingTreePath.getLastPathComponent();
                TreePath tpDestination = TreeLayerNamePanel.this.tree.getClosestPathForLocation(e.getX(), e.getY());
                TreeLayerNamePanel.this.movingTreePath = null;
                TreeLayerNamePanel.this.firstTimeDragging = true;
                if (tpDestination == null) {
                    return;
                }
                TreeLayerNamePanel.this.tree.repaint();
                if (node instanceof Layerable) {
                    Layerable layerable = (Layerable)node;
                    if (!layerable.isEnabled()) {
                        return;
                    }
                    int index = 0;
                    Category cat = null;
                    if (tpDestination.getLastPathComponent() instanceof Layerable) {
                        if (layerable == tpDestination.getLastPathComponent()) {
                            return;
                        }
                        cat = TreeLayerNamePanel.this.getLayerManager().getCategory((Layerable)tpDestination.getLastPathComponent());
                        if (cat.getName().equals(StandardCategoryNames.DISABLED)) {
                            return;
                        }
                        index = TreeLayerNamePanel.this.tree.getModel().getIndexOfChild(tpDestination.getParentPath().getLastPathComponent(), tpDestination.getLastPathComponent());
                    } else if (tpDestination.getLastPathComponent() instanceof Category) {
                        cat = (Category)tpDestination.getLastPathComponent();
                        if (cat.getName().equals(StandardCategoryNames.DISABLED)) {
                            return;
                        }
                        if (cat.contains(layerable)) {
                            return;
                        }
                    } else if (tpDestination.getLastPathComponent() instanceof Rule) {
                        TreePath parentPath = tpDestination.getParentPath();
                        if (layerable == parentPath.getLastPathComponent()) {
                            return;
                        }
                        cat = TreeLayerNamePanel.this.getLayerManager().getCategory((Layerable)parentPath.getLastPathComponent());
                        if (cat.getName().equals(StandardCategoryNames.DISABLED)) {
                            return;
                        }
                        index = TreeLayerNamePanel.this.tree.getModel().getIndexOfChild(parentPath.getParentPath().getLastPathComponent(), parentPath.getLastPathComponent());
                    } else {
                        Assert.shouldNeverReachHere();
                    }
                    TreeLayerNamePanel.this.getLayerManager().remove(layerable, false);
                    TreeLayerNamePanel.this.getLayerManager().addLayerable(cat.getName(), layerable, index);
                    TreeLayerNamePanel.this.getLayerManager().fireLayerChanged(layerable, LayerEventType.METADATA_CHANGED);
                } else if (node instanceof Category) {
                    int destIndex;
                    int srcIndex;
                    Category srcCat = (Category)node;
                    Category destCat = null;
                    if (tpDestination.getLastPathComponent() instanceof Layerable) {
                        destCat = TreeLayerNamePanel.this.getLayerManager().getCategory((Layerable)tpDestination.getLastPathComponent());
                    } else if (tpDestination.getLastPathComponent() instanceof Category) {
                        destCat = (Category)tpDestination.getLastPathComponent();
                    }
                    if (destCat != null && (srcIndex = TreeLayerNamePanel.this.getLayerManager().indexOf(srcCat)) != (destIndex = TreeLayerNamePanel.this.getLayerManager().indexOf(destCat))) {
                        TreeLayerNamePanel.this.getLayerManager().remove(srcCat);
                        TreeLayerNamePanel.this.getLayerManager().addCategory(srcCat, destIndex);
                    }
                }
            }
        });
        this.tree.addMouseMotionListener(new MouseMotionAdapter(){
            int rowNew;
            int rowOld = -1;
            Rectangle dragBar;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (TreeLayerNamePanel.this.movingTreePath == null) {
                    TreeLayerNamePanel.this.firstTimeDragging = true;
                    return;
                }
                this.rowNew = TreeLayerNamePanel.this.tree.getClosestRowForLocation(e.getX(), e.getY());
                this.rowOld = TreeLayerNamePanel.this.tree.getRowForPath(TreeLayerNamePanel.this.movingTreePath);
                if (this.rowNew == this.rowOld) {
                    return;
                }
                TreeLayerNamePanel.this.tree.expandRow(this.rowNew);
                Graphics2D g2 = (Graphics2D)TreeLayerNamePanel.this.tree.getGraphics();
                g2.setColor(Color.RED);
                g2.setXORMode(Color.WHITE);
                if (TreeLayerNamePanel.this.firstTimeDragging) {
                    this.rowOld = this.rowNew;
                    this.dragBar = new Rectangle(0, 0, TreeLayerNamePanel.this.tree.getWidth(), 20);
                    g2.fill(this.dragBar);
                    TreeLayerNamePanel.this.firstTimeDragging = false;
                }
                g2.fill(this.dragBar);
                this.dragBar.setLocation(0, TreeLayerNamePanel.this.tree.getRowBounds((int)this.rowNew).y);
                g2.fill(this.dragBar);
                this.rowOld = this.rowNew;
            }
        });
        this.tree.setCellEditor(this.cellEditor);
        this.tree.setInvokesStopCellEditing(true);
        this.tree.setBackground(this.getBackground());
        this.tree.addTreeSelectionListener(new TreeSelectionListener(){

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreeLayerNamePanel.this.fireLayerSelectionChanged();
            }
        });
        this.tree.getModel().addTreeModelListener(new TreeModelListener(){

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                int i = 0;
                while (i < e.getChildren().length) {
                    TreeUtil.visit(TreeLayerNamePanel.this.tree.getModel(), e.getTreePath().pathByAddingChild(e.getChildren()[i]), new TreeUtil.Visitor(){

                        public void visit(Stack path) {
                            (this).TreeLayerNamePanel.this.tree.makeVisible(new TreePath(path.toArray()));
                        }
                    });
                    ++i;
                }
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
            }
        });
        TreeUtil.expandAll(this.tree, new TreePath(this.tree.getModel().getRoot()));
    }

    public void addPopupMenu(Class<?> nodeClass, JPopupMenu popupMenu) {
        this.nodeClassToPopupMenuMap.put(nodeClass, popupMenu);
    }

    private void setCellRenderer(Map<Class<?>, TreeCellRenderer> additionalNodeClassToTreeCellRendererMap) {
        final Map<Class<?>, TreeCellRenderer> map = this.createNodeClassToTreeCellRendererMap();
        map.putAll(additionalNodeClassToTreeCellRendererMap);
        this.tree.setCellRenderer(new TreeCellRenderer(){
            private DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer(){
                private static final long serialVersionUID = 1L;
                {
                    this.setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
                }
            };

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                return ((TreeCellRenderer)LangUtil.ifNull(CollectionUtil.get(value.getClass(), map), this.defaultRenderer)).getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }
        });
    }

    private Map<Class<?>, TreeCellRenderer> createNodeClassToTreeCellRendererMap() {
        HashMap map = new HashMap();
        map.put(Layer.class, this.cellRenderer);
        map.put(WMSLayer.class, this.cellRenderer);
        map.put(Category.class, this.cellRenderer);
        map.put(TextBalloonLayer.class, this.cellRenderer);
        map.put(Rule.class, this.cellRenderer);
        return map;
    }

    void jbInit() throws Exception {
        this.setLayout(new BorderLayout());
        this.tree.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseReleased(MouseEvent e) {
                TreeLayerNamePanel.this.tree_mouseReleased(e);
            }
        });
        ToolTipManager.sharedInstance().registerComponent(this.tree);
        this.tree.setEditable(true);
        this.tree.setRootVisible(false);
        this.tree.setRowHeight(-1);
        this.scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        this.tree.setShowsRootHandles(true);
        this.scrollPane.setHorizontalScrollBarPolicy(31);
        this.scrollPane.setBorder(BorderFactory.createEtchedBorder());
        this.scrollPane.getViewport().add(this.tree);
        this.scalePanel = new ScalePanel(this.workbenchContext);
        this.scalePanel.setBorder(BorderFactory.createEtchedBorder());
        this.topPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL((JComponent)this.topPanel, this.topPosition++, 0, (JComponent)this.scalePanel, true, true, false);
        this.add((Component)this.topPanel, "North");
        this.add((Component)this.scrollPane, "Center");
    }

    void tree_mouseReleased(MouseEvent e) {
        if (!SwingUtilities.isRightMouseButton(e)) {
            return;
        }
        TreePath popupPath = this.tree.getPathForLocation(e.getX(), e.getY());
        if (popupPath == null) {
            return;
        }
        this.popupNode = popupPath.getLastPathComponent();
        if (!(e.isControlDown() || e.isShiftDown() || this.selectedNodes((Class<T>)Object.class).contains(this.popupNode))) {
            this.tree.getSelectionModel().clearSelection();
        }
        this.tree.getSelectionModel().addSelectionPath(popupPath);
        TreePath[] treePaths = this.tree.getSelectionModel().getSelectionPaths();
        int numObjectsSelected = treePaths.length;
        int numCategories = 0;
        int numWMSLayers = 0;
        int numLayers = 0;
        int numRules = 0;
        int numBalloon = 0;
        if (treePaths != null && numObjectsSelected > 0) {
            int i = 0;
            while (i < numObjectsSelected) {
                Object nodeSelected = treePaths[i].getLastPathComponent();
                if (nodeSelected instanceof Category) {
                    ++numCategories;
                } else if (nodeSelected instanceof WMSLayer) {
                    ++numWMSLayers;
                } else if (nodeSelected instanceof Layer) {
                    ++numLayers;
                } else if (nodeSelected instanceof Rule) {
                    ++numRules;
                } else if (nodeSelected instanceof TextBalloonLayer) {
                    ++numBalloon;
                }
                ++i;
            }
        }
        if (numCategories > 0) {
            this.getPopupMenu(Category.class).show(e.getComponent(), e.getX(), e.getY());
        } else if (numLayers > 0 && numWMSLayers == 0 && numBalloon == 0) {
            this.getPopupMenu(Layer.class).show(e.getComponent(), e.getX(), e.getY());
        } else if (numLayers == 0 && numWMSLayers > 0 && numBalloon == 0) {
            this.getPopupMenu(WMSLayer.class).show(e.getComponent(), e.getX(), e.getY());
        } else if (numLayers == 0 && numWMSLayers == 0 && numBalloon > 0) {
            this.getPopupMenu(TextBalloonLayer.class).show(e.getComponent(), e.getX(), e.getY());
        } else if (numLayers > 0 && numWMSLayers > 0) {
            this.getPopupMenu(Layerable.class).show(e.getComponent(), e.getX(), e.getY());
        } else if (numLayers == 0 && numWMSLayers == 0 && numRules > 0) {
            this.getPopupMenu(Rule.class).show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private JPopupMenu getPopupMenu(Class<?> nodeClass) {
        return (JPopupMenu)CollectionUtil.get(nodeClass, this.nodeClassToPopupMenuMap);
    }

    private void handleCheckBoxClick(MouseEvent e) {
        TreePath path = this.tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        Object node = path.getLastPathComponent();
        if (!(node instanceof Layerable)) {
            return;
        }
        Layerable layerable = (Layerable)node;
        Point layerNodeLocation = this.tree.getUI().getPathBounds(this.tree, path).getLocation();
        this.cellRenderer.getLayerNameRenderer().getTreeCellRendererComponent(this.tree, path.getLastPathComponent(), false, false, false, 0, false);
        Rectangle checkBoxBounds = this.cellRenderer.getLayerNameRenderer().getCheckBoxBounds();
        checkBoxBounds.translate((int)layerNodeLocation.getX(), (int)layerNodeLocation.getY());
        if (checkBoxBounds.contains(e.getPoint())) {
            layerable.setVisible(!layerable.isVisible());
        }
    }

    @Override
    public Layerable[] getSelectedLayers() {
        return TreeLayerNamePanel.selectedLayers(this);
    }

    public static Layerable[] selectedLayers(LayerNamePanel layerNamePanel) {
        return layerNamePanel.selectedNodes(Layerable.class).toArray(new Layerable[0]);
    }

    @Override
    public Collection<Category> getSelectedCategories() {
        return this.selectedNodes((Class<T>)Category.class);
    }

    public <T> Collection<T> selectedNodes(Class<T> c) {
        return TreeLayerNamePanel.selectedNodes(c, this.tree);
    }

    public static <T> Collection<T> selectedNodes(Class<T> c, JTree tree) {
        ArrayList<Object> selectedNodes = new ArrayList<Object>();
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (selectionPaths == null) {
            return new ArrayList();
        }
        int i = 0;
        while (i < selectionPaths.length) {
            Object node = selectionPaths[i].getLastPathComponent();
            if (c.isInstance(node)) {
                selectedNodes.add(node);
            }
            ++i;
        }
        return selectedNodes;
    }

    public Layer findParentLayer(RuleImpl rule) {
        List<Category> categories = this.layerManagerProxy.getLayerManager().getCategories();
        for (Category category : categories) {
            List<Layerable> layerables = category.getLayerables();
            for (Layerable layerable : layerables) {
                FeatureTypeStyle[] featureTypeStyles;
                if (!(layerable instanceof Layer)) continue;
                Layer layer = (Layer)layerable;
                Style style = layer.getModelStyle();
                FeatureTypeStyle[] featureTypeStyleArray = featureTypeStyles = style.getFeatureTypeStyles();
                int n = featureTypeStyles.length;
                int n2 = 0;
                while (n2 < n) {
                    Rule[] rules;
                    FeatureTypeStyle fts = featureTypeStyleArray[n2];
                    Rule[] ruleArray = rules = fts.getRules();
                    int n3 = rules.length;
                    int n4 = 0;
                    while (n4 < n3) {
                        Rule rule2 = ruleArray[n4];
                        if (rule.equals(rule2)) {
                            return layer;
                        }
                        ++n4;
                    }
                    ++n2;
                }
            }
        }
        return null;
    }

    public void setSelectedLayers(Layer[] layers) {
        this.clearLayerSelection();
        int i = 0;
        while (i < layers.length) {
            this.addSelectedLayer(layers[i]);
            ++i;
        }
    }

    public void clearLayerSelection() {
        this.tree.getSelectionModel().clearSelection();
    }

    public void addSelectedLayer(Layer layer) {
        this.tree.addSelectionPath(TreeUtil.findTreePath(layer, this.tree.getModel()));
    }

    @Override
    public void layerChanged(LayerEvent e) {
        TreeModelEvent treeModelEvent = new TreeModelEvent((Object)this, new Object[]{this.tree.getModel().getRoot(), e.getCategory()}, new int[]{e.getLayerableIndex()}, new Object[]{e.getLayerable()});
        if (e.getType() == LayerEventType.ADDED) {
            this.firableTreeModelWrapper.fireTreeNodesInserted(treeModelEvent);
            if (e.getType() == LayerEventType.ADDED && this.getSelectedLayers().length == 0 && e.getLayerable() instanceof Layer) {
                this.addSelectedLayer((Layer)e.getLayerable());
            }
            return;
        }
        if (e.getType() == LayerEventType.REMOVED) {
            this.firableTreeModelWrapper.fireTreeNodesRemoved(treeModelEvent);
            return;
        }
        if (e.getType() == LayerEventType.APPEARANCE_CHANGED) {
            HashMap<TreePath, Boolean> expandedState = this.getExpandedState();
            Collection<Layerable> layerables = this.selectedNodes((Class<T>)Layerable.class);
            this.firableTreeModelWrapper.fireTreeNodesRemoved(treeModelEvent);
            this.firableTreeModelWrapper.fireTreeNodesInserted(treeModelEvent);
            if (treeModelEvent.getChildren() != null && treeModelEvent.getChildren().length > 0) {
                Object[] childs = treeModelEvent.getChildren();
                int i = 0;
                while (i < childs.length) {
                    Layerable layerable;
                    if (childs[i] instanceof Layerable && layerables.contains(layerable = (Layerable)childs[0])) {
                        this.tree.getSelectionModel().addSelectionPath(TreeUtil.findTreePath(layerable, this.tree.getModel()));
                    }
                    ++i;
                }
            }
            this.setExpandedState(expandedState);
            return;
        }
        if (e.getType() == LayerEventType.METADATA_CHANGED) {
            this.firableTreeModelWrapper.fireTreeNodesChanged(treeModelEvent);
            return;
        }
        if (e.getType() == LayerEventType.VISIBILITY_CHANGED) {
            this.firableTreeModelWrapper.fireTreeNodesChanged(treeModelEvent);
            return;
        }
        if (e.getType() == LayerEventType.COMMITED) {
            return;
        }
        Assert.shouldNeverReachHere();
    }

    private void setExpandedState(HashMap<TreePath, Boolean> expandedStateMap) {
        int numRows = this.tree.getRowCount();
        int i = 0;
        while (i < numRows) {
            TreePath node = this.tree.getPathForRow(i);
            Boolean expanded = expandedStateMap.get(node);
            if (expanded != null) {
                if (expanded.booleanValue()) {
                    this.tree.expandPath(node);
                } else {
                    this.tree.collapsePath(node);
                }
            }
            ++i;
        }
    }

    private HashMap<TreePath, Boolean> getExpandedState() {
        HashMap<TreePath, Boolean> expandedStateMap = new HashMap<TreePath, Boolean>();
        int numRows = this.tree.getRowCount();
        int i = 0;
        while (i < numRows) {
            TreePath node = this.tree.getPathForRow(i);
            expandedStateMap.put(node, this.tree.isExpanded(node));
            ++i;
        }
        return expandedStateMap;
    }

    @Override
    public void categoryChanged(CategoryEvent e) {
        TreeModelEvent treeModelEvent = new TreeModelEvent((Object)this, new Object[]{this.tree.getModel().getRoot()}, new int[]{e.getCategoryIndex() + this.indexOfFirstCategoryInTree()}, new Object[]{e.getCategory()});
        if (e.getType() == CategoryEventType.ADDED) {
            this.firableTreeModelWrapper.fireTreeNodesInserted(treeModelEvent);
            return;
        }
        if (e.getType() == CategoryEventType.REMOVED) {
            this.firableTreeModelWrapper.fireTreeNodesRemoved(treeModelEvent);
            return;
        }
        if (e.getType() == CategoryEventType.METADATA_CHANGED) {
            this.firableTreeModelWrapper.fireTreeNodesChanged(treeModelEvent);
            return;
        }
        Assert.shouldNeverReachHere();
    }

    private int indexOfFirstCategoryInTree() {
        int i = 0;
        while (i < this.tree.getModel().getChildCount(this.tree.getModel().getRoot())) {
            if (this.tree.getModel().getChild(this.tree.getModel().getRoot(), i) instanceof Category) {
                return i;
            }
            ++i;
        }
        Assert.shouldNeverReachHere();
        return -1;
    }

    @Override
    public void featuresChanged(FeatureEvent e) {
    }

    @Override
    public void dispose() {
        if (this.layerManagerProxy != null) {
            this.layerManagerProxy.getLayerManager().removeLayerListener(this);
        }
        this.layerManagerProxy = null;
        this.movingTreePath = null;
        this.renderingManager = null;
    }

    public JTree getTree() {
        return this.tree;
    }

    @Override
    public void addListener(LayerNamePanelListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(LayerNamePanelListener listener) {
        this.listeners.remove(listener);
    }

    public void fireLayerSelectionChanged() {
        for (LayerNamePanelListener l : this.listeners) {
            l.layerSelectionChanged();
        }
    }

    @Override
    public LayerManager getLayerManager() {
        return this.layerManagerProxy.getLayerManager();
    }

    public static Layer chooseEditableLayer(LayerNamePanel panel) {
        for (Layerable layerable : Arrays.asList(panel.getSelectedLayers())) {
            Layer layer;
            if (!(layerable instanceof Layer) || !(layer = (Layer)layerable).isEditable()) continue;
            return layer;
        }
        if (panel.getLayerManager().getEditableLayers().isEmpty()) {
            return null;
        }
        return panel.getLayerManager().getEditableLayers().iterator().next();
    }

    @Override
    public Layer chooseEditableLayer() {
        return TreeLayerNamePanel.chooseEditableLayer(this);
    }

    @Override
    public LayerNamePanel getLayerNamePanel() {
        return this;
    }

    protected FirableTreeModelWrapper getFirableTreeModelWrapper() {
        return this.firableTreeModelWrapper;
    }

    @Override
    public Object getPopupNode() {
        return this.popupNode;
    }

    public void zoomChanged(Envelope envelope) {
        this.scalePanel.zoomChanged(envelope);
    }

    public ScalePanel getScalePanel() {
        return this.scalePanel;
    }

    public void setScalePanel(ScalePanel scalePanel) {
        this.scalePanel = scalePanel;
    }

    @Override
    public void saveStatus() {
        TreeModel tm = this.tree.getModel();
        Object root = tm.getRoot();
        TreePath tp = new TreePath(root);
        this.exploraGet(root, tp);
    }

    public void exploraGet(Object nodo, TreePath tp) {
        TreeModel tm = this.tree.getModel();
        int n = tm.getChildCount(nodo);
        int i = 0;
        while (i < n) {
            Object nodoNuevo = tm.getChild(nodo, i);
            TreePath tpNuevo = tp.pathByAddingChild(nodoNuevo);
            if (nodoNuevo instanceof Layer) {
                Layer lay = (Layer)nodoNuevo;
                this.exploraGet(nodoNuevo, tpNuevo);
                lay.setCollapsed(this.tree.isCollapsed(tpNuevo));
            } else if (nodoNuevo instanceof Category) {
                Category cat = (Category)nodoNuevo;
                this.exploraGet(nodoNuevo, tpNuevo);
                cat.setCollapsed(this.tree.isCollapsed(tpNuevo));
            } else {
                this.exploraGet(nodoNuevo, tpNuevo);
            }
            ++i;
        }
    }

    @Override
    public void loadStatus() {
        TreeModel tm = this.tree.getModel();
        Object root = tm.getRoot();
        TreePath tp = new TreePath(root);
        this.exploraSet(root, tp);
    }

    public void exploraSet(Object nodo, TreePath tp) {
        TreeModel tm = this.tree.getModel();
        int n = tm.getChildCount(nodo);
        int i = 0;
        while (i < n) {
            Object nodoNuevo = tm.getChild(nodo, i);
            TreePath tpNuevo = tp.pathByAddingChild(nodoNuevo);
            if (nodoNuevo instanceof Layer) {
                Layer lay = (Layer)nodoNuevo;
                this.exploraSet(nodoNuevo, tpNuevo);
                if (lay.isCollapsed()) {
                    this.tree.collapsePath(tpNuevo);
                }
            } else if (nodoNuevo instanceof Category) {
                Category cat = (Category)nodoNuevo;
                this.exploraSet(nodoNuevo, tpNuevo);
                if (cat.isCollapsed()) {
                    this.tree.collapsePath(tpNuevo);
                } else {
                    this.tree.expandPath(tpNuevo);
                }
            } else {
                this.exploraSet(nodoNuevo, tpNuevo);
            }
            ++i;
        }
    }

    public void addComponentToTopPanel(JComponent component) {
        FormUtils.addRowInGBL(this.topPanel, this.topPosition++, 0, component);
    }
}

