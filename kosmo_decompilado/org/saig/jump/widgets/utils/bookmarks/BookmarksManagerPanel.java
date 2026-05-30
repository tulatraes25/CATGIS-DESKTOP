/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.utils.bookmarks;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.FirableTreeModelWrapper;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.TitledPopupMenu;
import com.vividsolutions.jump.workbench.ui.TreeLayerNamePanel;
import com.vividsolutions.jump.workbench.ui.TreeUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.NoninvertibleTransformException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.bookmark.BookmarkCategory;
import org.saig.core.model.bookmark.BookmarkCategoryEvent;
import org.saig.core.model.bookmark.BookmarkCategoryEventType;
import org.saig.core.model.bookmark.BookmarkEvent;
import org.saig.core.model.bookmark.BookmarkEventType;
import org.saig.core.model.bookmark.BookmarkManager;
import org.saig.core.model.bookmark.IBookmark;
import org.saig.core.model.bookmark.IBookmarkListener;
import org.saig.core.model.bookmark.TemporalBookmark;
import org.saig.core.model.data.Table;
import org.saig.core.util.DateFormatManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.utils.bookmarks.BookmarkTreeCellEditor;
import org.saig.jump.widgets.utils.bookmarks.BookmarkTreeModel;
import org.saig.jump.widgets.utils.bookmarks.EditBookmarkDialog;

public class BookmarksManagerPanel
extends JPanel
implements IBookmarkListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(BookmarksManagerPanel.class);
    private JScrollPane bookmarkTreeScrollPane;
    private JTree bookmarkTree;
    private FirableTreeModelWrapper firableTreeModelWrapper;
    public static final Icon BOOKMARK_CATEGORY_ICON = IconLoader.icon("world.png");
    public static final Icon BOOKMARK_ICON = IconLoader.icon("book_addresses.png");
    public static final Icon BOOKMARK_TIME_ICON = IconLoader.icon("book_addresses_time.png");
    public static final Icon BOOKMARK_ADD_ICON = IconLoader.icon("bookmark_add.png");
    public static final Icon BOOKMARK_ADD_TIME_ICON = IconLoader.icon("bookmark_add_time.png");
    public static final Icon BOOKMARK_DELETE_ICON = IconLoader.icon("bookmark_delete.png");
    public static final Icon BOOKMARK_EDIT_ICON = IconLoader.icon("bookmark_edit.png");
    public static final Icon BOOKMARK_GO_ICON = IconLoader.icon("bookmark_go.png");
    public static final Icon BOOKMARK_GO_TO_CURRENT_DATE_ICON = IconLoader.icon("bookmark_current_date.png");
    public static final Icon BOOKMARK_CATEGORY_ADD_ICON = IconLoader.icon("bookmark_category_add.png");
    public static final Icon BOOKMARK_CATEGORY_DELETE_ICON = IconLoader.icon("bookmark_category_delete.png");
    public static final Icon BOOKMARK_MOVE_TO_FIRST_ICON = IconLoader.icon("VCRUp.gif");
    public static final Icon BOOKMARK_MOVE_TO_LAST_ICON = IconLoader.icon("VCRDown.gif");
    private JPanel treeButtonsPanel;
    private WorkbenchToolBar firstToolbar;
    private WorkbenchToolBar secondToolbar;
    private TreePath movingTreePath = null;
    private boolean firstTimeDragging = true;
    private PlugIn addNewBookmarkPlugIn = new AbstractPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.add-new-bookmark");
        }

        @Override
        public Icon getIcon() {
            return BOOKMARK_ADD_ICON;
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            BookmarksManagerPanel.this.addNewBookmark();
            return true;
        }
    };
    private PlugIn addNewTemporalBookmarkPlugIn = new AbstractPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.Add-a-new-spatial-temporal-bookmark");
        }

        @Override
        public Icon getIcon() {
            return BOOKMARK_ADD_TIME_ICON;
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            BookmarksManagerPanel.this.addNewTemporalBookmark();
            return true;
        }
    };
    private PlugIn addNewBookmarkCategoryPlugIn = new AbstractPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.add-new-category");
        }

        @Override
        public Icon getIcon() {
            return BOOKMARK_CATEGORY_ADD_ICON;
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            BookmarksManagerPanel.this.addNewBookmarkCategory();
            return true;
        }
    };
    private PlugIn editSelectedBookmarkPlugIn = new AbstractPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.edit-selected-bookmark-properties");
        }

        @Override
        public Icon getIcon() {
            return BOOKMARK_EDIT_ICON;
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            BookmarksManagerPanel.this.editSelectedBookmark();
            return true;
        }

        @Override
        public EnableCheck getCheck() {
            return new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    int selectedBookmarks = TreeLayerNamePanel.selectedNodes(IBookmark.class, BookmarksManagerPanel.this.bookmarkTree).size();
                    if (selectedBookmarks != 1) {
                        return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.Only-one-bookmark-must-be-selected");
                    }
                    return null;
                }
            };
        }
    };
    private PlugIn removeSelectedCategoriesPlugIn = new AbstractPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.remove-selected-categories");
        }

        @Override
        public Icon getIcon() {
            return BOOKMARK_CATEGORY_DELETE_ICON;
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            BookmarksManagerPanel.this.removeSelectedCategories();
            return true;
        }

        @Override
        public EnableCheck getCheck() {
            return new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    int selectedCategories = TreeLayerNamePanel.selectedNodes(BookmarkCategory.class, BookmarksManagerPanel.this.bookmarkTree).size();
                    if (selectedCategories == 0) {
                        return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.At-least-one-bookmark-category-must-be-selected");
                    }
                    return null;
                }
            };
        }
    };
    private PlugIn removeSelectedBookmarksPlugIn = new AbstractPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.remove-selected-bookmarks");
        }

        @Override
        public Icon getIcon() {
            return BOOKMARK_DELETE_ICON;
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            BookmarksManagerPanel.this.removeSelectedBookmarks();
            return true;
        }

        @Override
        public EnableCheck getCheck() {
            return new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    int selectedBookmarks = TreeLayerNamePanel.selectedNodes(IBookmark.class, BookmarksManagerPanel.this.bookmarkTree).size();
                    if (selectedBookmarks == 0) {
                        return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.At-least-one-bookmark-must-be-selected");
                    }
                    return null;
                }
            };
        }
    };
    private PlugIn goToSelectedBookmarkPlugIn = new AbstractPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.go-to-the-selected-bookmark");
        }

        @Override
        public Icon getIcon() {
            return BOOKMARK_GO_ICON;
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            BookmarksManagerPanel.this.goToSelectedBookmark();
            return true;
        }

        @Override
        public EnableCheck getCheck() {
            return new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    Layer editableLayer;
                    Collection<Layer> editableLayers;
                    Collection<IBookmark> selectedBookmarks = TreeLayerNamePanel.selectedNodes(IBookmark.class, BookmarksManagerPanel.this.bookmarkTree);
                    if (selectedBookmarks.size() != 1) {
                        return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.Only-one-bookmark-must-be-selected");
                    }
                    IBookmark selectedBookmark = selectedBookmarks.iterator().next();
                    if (selectedBookmark.getLocalization() == null) {
                        return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.The-selected-bookmark-must-have-an-associated-localization");
                    }
                    if (selectedBookmark instanceof TemporalBookmark && ((TemporalBookmark)selectedBookmark).getTimemark() != null && (editableLayers = JUMPWorkbench.getFrameInstance().getContext().getLayerManager().getEditableLayers()).size() == 1 && !(editableLayer = editableLayers.iterator().next()).isVersionable()) {
                        return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.The-editable-layer-must-have-its-time-variable-enabled");
                    }
                    return null;
                }
            };
        }
    };
    private PlugIn goToCurrentDatePlugIn = new AbstractPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.Jump-to-the-current-date");
        }

        @Override
        public Icon getIcon() {
            return BOOKMARK_GO_TO_CURRENT_DATE_ICON;
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            BookmarksManagerPanel.this.goToCurrentDate();
            return true;
        }
    };
    private MoveSelectedBookmarksToCategoryPlugIn moveSelectedBookmarksToCategoryPlugIn = new MoveSelectedBookmarksToCategoryPlugIn();
    private PlugIn moveSelectedBookmarksToFirst = new AbstractPlugIn(){

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            List selectedBookmarks = (List)TreeLayerNamePanel.selectedNodes(IBookmark.class, BookmarksManagerPanel.this.bookmarkTree);
            BookmarkManager bm = BookmarkManager.getInstance();
            Collections.reverse(selectedBookmarks);
            for (IBookmark currentBookmark : selectedBookmarks) {
                BookmarkCategory currentCat = bm.getCategory(currentBookmark);
                bm.removeBookmark(bm.getCategory(currentBookmark), currentBookmark);
                bm.addBookmark(currentCat, currentBookmark, 0);
            }
            BookmarksManagerPanel.this.bookmarkTree.getSelectionModel().clearSelection();
            for (IBookmark currentBookmark : selectedBookmarks) {
                BookmarksManagerPanel.this.bookmarkTree.addSelectionPath(TreeUtil.findTreePath(currentBookmark, BookmarksManagerPanel.this.bookmarkTree.getModel()));
            }
            return true;
        }

        @Override
        public EnableCheck getCheck() {
            return new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    List selectedBookmarks = (List)TreeLayerNamePanel.selectedNodes(IBookmark.class, BookmarksManagerPanel.this.bookmarkTree);
                    BookmarkManager bm = BookmarkManager.getInstance();
                    for (IBookmark currentBookmark : selectedBookmarks) {
                        BookmarkCategory currentCat = bm.getCategory(currentBookmark);
                        if (currentCat.indexOf(currentBookmark) == 0) continue;
                        return null;
                    }
                    return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.All-the-selected-bookmarks-are-already-at-the-beginning-of-theirs-category");
                }
            };
        }

        @Override
        public Icon getIcon() {
            return BOOKMARK_MOVE_TO_FIRST_ICON;
        }

        @Override
        public String getName() {
            return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.Move-to-the-beginning");
        }
    };
    private PlugIn moveSelectedBookmarksToLast = new AbstractPlugIn(){

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            List selectedBookmarks = (List)TreeLayerNamePanel.selectedNodes(IBookmark.class, BookmarksManagerPanel.this.bookmarkTree);
            BookmarkManager bm = BookmarkManager.getInstance();
            for (IBookmark currentBookmark : selectedBookmarks) {
                BookmarkCategory currentCat = bm.getCategory(currentBookmark);
                int lastPos = currentCat.size() - 1;
                bm.removeBookmark(bm.getCategory(currentBookmark), currentBookmark);
                bm.addBookmark(currentCat, currentBookmark, lastPos);
            }
            BookmarksManagerPanel.this.bookmarkTree.getSelectionModel().clearSelection();
            for (IBookmark currentBookmark : selectedBookmarks) {
                BookmarksManagerPanel.this.bookmarkTree.addSelectionPath(TreeUtil.findTreePath(currentBookmark, BookmarksManagerPanel.this.bookmarkTree.getModel()));
            }
            return true;
        }

        @Override
        public EnableCheck getCheck() {
            return new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    List selectedBookmarks = (List)TreeLayerNamePanel.selectedNodes(IBookmark.class, BookmarksManagerPanel.this.bookmarkTree);
                    BookmarkManager bm = BookmarkManager.getInstance();
                    for (IBookmark currentBookmark : selectedBookmarks) {
                        BookmarkCategory currentCat = bm.getCategory(currentBookmark);
                        if (currentCat.indexOf(currentBookmark) == currentCat.size() - 1) continue;
                        return null;
                    }
                    return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.All-the-selected-bookmarks-are-already-at-the-end-of-theirs-category");
                }
            };
        }

        @Override
        public Icon getIcon() {
            return BOOKMARK_MOVE_TO_LAST_ICON;
        }

        @Override
        public String getName() {
            return I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.Move-to-the-end");
        }
    };
    private Object popupNode;
    private Map<Class<?>, JPopupMenu> nodeClassToPopupMenuMap = new HashMap();
    private TitledPopupMenu bookmarkCategoryPopupMenu = new TitledPopupMenu(){
        private static final long serialVersionUID = 1L;
        {
            this.addPopupMenuListener(new PopupMenuListener(){

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    int size = TreeLayerNamePanel.selectedNodes(BookmarkCategory.class, BookmarksManagerPanel.this.bookmarkTree).size();
                    this.setTitle(size != 1 ? "(" + size + " " + I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.selected-categories") + ")" : TreeLayerNamePanel.selectedNodes(BookmarkCategory.class, BookmarksManagerPanel.this.bookmarkTree).iterator().next().getName());
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    };
    private TitledPopupMenu bookmarkPopupMenu = new TitledPopupMenu(){
        private static final long serialVersionUID = 1L;
        {
            this.addPopupMenuListener(new PopupMenuListener(){

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    int size = TreeLayerNamePanel.selectedNodes(IBookmark.class, BookmarksManagerPanel.this.bookmarkTree).size();
                    this.setTitle(size != 1 ? "(" + size + " " + I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.bookmarks-selected") + ")" : TreeLayerNamePanel.selectedNodes(IBookmark.class, BookmarksManagerPanel.this.bookmarkTree).iterator().next().getName());
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    };

    public BookmarksManagerPanel() {
        this.setLayout(new BorderLayout());
        this.initialize();
        BookmarkManager.getInstance().addBookmarkListener(this);
        this.firstToolbar.updateEnabledState();
        this.secondToolbar.updateEnabledState();
    }

    protected void goToCurrentDate() {
        LayerViewPanel panel = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel();
        if (panel != null) {
            List<Table> tables = JUMPWorkbench.getFrameInstance().getContext().getDataManager().getRealTables();
            for (Table currentTable : tables) {
                if (!currentTable.isVersionable()) continue;
                currentTable.setVersionableViewDate(null);
            }
            List<Layer> layers = panel.getLayerManager().getLayers();
            for (Layer currentLayer : layers) {
                if (!currentLayer.isVersionable()) continue;
                currentLayer.setVersionableViewDate(null);
                currentLayer.fireAppearanceChanged();
            }
        }
    }

    private void initialize() {
        this.add((Component)this.getBookmarkTreeScrollPanel(), "Center");
        this.add((Component)this.getTreeButtonsPanel(), "North");
        this.buildPopupMenus();
    }

    private void buildPopupMenus() {
        FeatureInstaller featureInstaller = new FeatureInstaller(JUMPWorkbench.getFrameInstance().getContext());
        this.addPopupMenu(BookmarkCategory.class, this.bookmarkCategoryPopupMenu);
        featureInstaller.addPopupMenuItem(this.bookmarkCategoryPopupMenu, this.addNewBookmarkPlugIn, false);
        featureInstaller.addPopupMenuItem(this.bookmarkCategoryPopupMenu, this.addNewTemporalBookmarkPlugIn, false);
        featureInstaller.addPopupMenuItem(this.bookmarkCategoryPopupMenu, this.addNewBookmarkCategoryPlugIn, false);
        this.bookmarkCategoryPopupMenu.addSeparator();
        featureInstaller.addPopupMenuItem(this.bookmarkCategoryPopupMenu, this.removeSelectedCategoriesPlugIn, false);
        this.addPopupMenu(IBookmark.class, this.bookmarkPopupMenu);
        featureInstaller.addPopupMenuItem(this.bookmarkPopupMenu, this.goToSelectedBookmarkPlugIn, false);
        this.bookmarkPopupMenu.addSeparator();
        featureInstaller.addPopupMenuItem(this.bookmarkPopupMenu, this.editSelectedBookmarkPlugIn, false);
        this.bookmarkPopupMenu.addSeparator();
        featureInstaller.addPopupMenuItem(this.bookmarkPopupMenu, this.removeSelectedBookmarksPlugIn, false);
        this.bookmarkPopupMenu.addSeparator();
        featureInstaller.addPopupMenuItem(this.bookmarkPopupMenu, this.moveSelectedBookmarksToFirst, false);
        this.bookmarkPopupMenu.add(this.moveSelectedBookmarksToCategoryPlugIn.getMenu());
        featureInstaller.addPopupMenuItem(this.bookmarkPopupMenu, this.moveSelectedBookmarksToLast, false);
    }

    private JScrollPane getBookmarkTreeScrollPanel() {
        if (this.bookmarkTreeScrollPane == null) {
            this.bookmarkTree = this.buildBookmarksTree();
            this.bookmarkTreeScrollPane = new JScrollPane(this.bookmarkTree, 20, 31);
            this.bookmarkTreeScrollPane.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.bookmarks")));
        }
        return this.bookmarkTreeScrollPane;
    }

    protected JTree buildBookmarksTree() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Raiz");
        this.bookmarkTree = new JTree(top){
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isPathEditable(TreePath path) {
                if (!this.isEditable()) {
                    return false;
                }
                return path.getLastPathComponent() instanceof IBookmark || path.getLastPathComponent() instanceof BookmarkCategory;
            }

            @Override
            public boolean hasBeenExpanded(TreePath path) {
                return super.hasBeenExpanded(path) || !this.getModel().isLeaf(path.getLastPathComponent());
            }
        };
        this.bookmarkTree.setModel(new BookmarkTreeModel());
        ToolTipManager.sharedInstance().registerComponent(this.bookmarkTree);
        this.bookmarkTree.setEditable(true);
        Collection<BookmarkCategory> categories = BookmarkManager.getInstance().getCategories();
        for (BookmarkCategory currentCategory : categories) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(currentCategory);
            for (IBookmark currentBookmark : currentCategory.getBookmarks()) {
                DefaultMutableTreeNode bookmarkNode = new DefaultMutableTreeNode(currentBookmark);
                categoryNode.add(bookmarkNode);
            }
            top.add(categoryNode);
        }
        this.bookmarkTree.setCellRenderer(this.getTreeCellRenderer());
        this.bookmarkTree.setRowHeight(-1);
        this.bookmarkTree.setAutoscrolls(true);
        this.bookmarkTree.setScrollsOnExpand(true);
        this.bookmarkTree.setShowsRootHandles(true);
        this.bookmarkTree.addMouseListener(this.getTreeListener());
        this.bookmarkTree.addTreeSelectionListener(this.getTreeSelectionListener());
        this.bookmarkTree.setRootVisible(false);
        this.firableTreeModelWrapper = new FirableTreeModelWrapper(this.bookmarkTree.getModel());
        this.bookmarkTree.setModel(this.firableTreeModelWrapper);
        this.bookmarkTree.setCellEditor(new BookmarkTreeCellEditor());
        this.bookmarkTree.setInvokesStopCellEditing(true);
        this.bookmarkTree.setBackground(this.getBackground());
        this.bookmarkTree.getModel().addTreeModelListener(new TreeModelListener(){

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                int i = 0;
                while (i < e.getChildren().length) {
                    TreeUtil.visit(BookmarksManagerPanel.this.bookmarkTree.getModel(), e.getTreePath().pathByAddingChild(e.getChildren()[i]), new TreeUtil.Visitor(){

                        @Override
                        public void visit(Stack<Object> path) {
                            BookmarksManagerPanel.this.bookmarkTree.makeVisible(new TreePath(path.toArray()));
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
        TreeUtil.expandAll(this.bookmarkTree, new TreePath(this.bookmarkTree.getModel().getRoot()));
        this.bookmarkTree.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1) {
                    BookmarksManagerPanel.this.movingTreePath = BookmarksManagerPanel.this.bookmarkTree.getPathForLocation(e.getX(), e.getY());
                } else {
                    BookmarksManagerPanel.this.movingTreePath = null;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                BookmarksManagerPanel.this.tree_mouseReleased(e);
                if (e.getButton() != 1 || BookmarksManagerPanel.this.movingTreePath == null) {
                    return;
                }
                Object node = BookmarksManagerPanel.this.movingTreePath.getLastPathComponent();
                TreePath tpDestination = BookmarksManagerPanel.this.bookmarkTree.getClosestPathForLocation(e.getX(), e.getY());
                BookmarksManagerPanel.this.movingTreePath = null;
                BookmarksManagerPanel.this.firstTimeDragging = true;
                if (tpDestination == null) {
                    return;
                }
                BookmarksManagerPanel.this.bookmarkTree.repaint();
                BookmarkManager bm = BookmarkManager.getInstance();
                if (node instanceof IBookmark) {
                    IBookmark bookmark = (IBookmark)node;
                    int index = 0;
                    BookmarkCategory cat = null;
                    BookmarkCategory sourceCat = bm.getCategory(bookmark);
                    if (tpDestination.getLastPathComponent() instanceof IBookmark) {
                        if (bookmark == tpDestination.getLastPathComponent()) {
                            return;
                        }
                        cat = bm.getCategory((IBookmark)tpDestination.getLastPathComponent());
                        index = BookmarksManagerPanel.this.bookmarkTree.getModel().getIndexOfChild(cat, tpDestination.getLastPathComponent());
                    } else if (tpDestination.getLastPathComponent() instanceof BookmarkCategory) {
                        cat = (BookmarkCategory)tpDestination.getLastPathComponent();
                        if (cat.indexOf(bookmark) != -1) {
                            return;
                        }
                    } else {
                        Assert.shouldNeverReachHere();
                    }
                    bm.removeBookmark(sourceCat, bookmark);
                    bm.addBookmark(cat, bookmark, index);
                } else if (node instanceof BookmarkCategory) {
                    int destIndex;
                    int srcIndex;
                    BookmarkCategory srcCat = (BookmarkCategory)node;
                    BookmarkCategory destCat = null;
                    if (tpDestination.getLastPathComponent() instanceof IBookmark) {
                        destCat = (BookmarkCategory)tpDestination.getParentPath().getLastPathComponent();
                    } else if (tpDestination.getLastPathComponent() instanceof BookmarkCategory) {
                        destCat = (BookmarkCategory)tpDestination.getLastPathComponent();
                    }
                    if (destCat != null && (srcIndex = BookmarksManagerPanel.this.bookmarkTree.getModel().getIndexOfChild(BookmarksManagerPanel.this.bookmarkTree.getModel().getRoot(), srcCat)) != (destIndex = BookmarksManagerPanel.this.bookmarkTree.getModel().getIndexOfChild(BookmarksManagerPanel.this.bookmarkTree.getModel().getRoot(), destCat))) {
                        bm.removeCategory(srcCat);
                        bm.addCategory(srcCat, destIndex);
                    }
                }
                BookmarksManagerPanel.this.bookmarkTree.repaint();
            }
        });
        this.bookmarkTree.addMouseMotionListener(new MouseMotionAdapter(){
            int rowNew;
            int rowOld = -1;
            Rectangle dragBar;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (BookmarksManagerPanel.this.movingTreePath == null) {
                    BookmarksManagerPanel.this.firstTimeDragging = true;
                    return;
                }
                this.rowNew = BookmarksManagerPanel.this.bookmarkTree.getClosestRowForLocation(e.getX(), e.getY());
                this.rowOld = BookmarksManagerPanel.this.bookmarkTree.getRowForPath(BookmarksManagerPanel.this.movingTreePath);
                if (this.rowNew == this.rowOld) {
                    return;
                }
                BookmarksManagerPanel.this.bookmarkTree.expandRow(this.rowNew);
                Graphics2D g2 = (Graphics2D)BookmarksManagerPanel.this.bookmarkTree.getGraphics();
                g2.setColor(Color.RED);
                g2.setXORMode(Color.WHITE);
                if (BookmarksManagerPanel.this.firstTimeDragging) {
                    this.rowOld = this.rowNew;
                    this.dragBar = new Rectangle(0, 0, BookmarksManagerPanel.this.bookmarkTree.getWidth(), 20);
                    g2.fill(this.dragBar);
                    BookmarksManagerPanel.this.firstTimeDragging = false;
                }
                g2.fill(this.dragBar);
                this.dragBar.setLocation(0, ((BookmarksManagerPanel)BookmarksManagerPanel.this).bookmarkTree.getRowBounds((int)this.rowNew).y);
                g2.fill(this.dragBar);
                this.rowOld = this.rowNew;
            }
        });
        return this.bookmarkTree;
    }

    protected TreeSelectionListener getTreeSelectionListener() {
        return new TreeSelectionListener(){

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                BookmarksManagerPanel.this.firstToolbar.updateEnabledState();
                BookmarksManagerPanel.this.secondToolbar.updateEnabledState();
            }
        };
    }

    private JPanel getTreeButtonsPanel() {
        if (this.treeButtonsPanel == null) {
            this.treeButtonsPanel = new JPanel(new GridLayout(2, 1));
            WorkbenchContext wc = JUMPWorkbench.getFrameInstance().getContext();
            this.firstToolbar = new WorkbenchToolBar(null);
            this.firstToolbar.setFloatable(false);
            this.firstToolbar.add(Box.createHorizontalGlue());
            this.firstToolbar.addPlugIn(this.addNewBookmarkPlugIn, wc);
            this.firstToolbar.addPlugIn(this.addNewTemporalBookmarkPlugIn, wc);
            this.firstToolbar.addPlugIn(this.addNewBookmarkCategoryPlugIn, wc);
            this.firstToolbar.addSeparator();
            this.firstToolbar.addPlugIn(this.editSelectedBookmarkPlugIn, wc);
            this.firstToolbar.addSeparator();
            this.firstToolbar.addPlugIn(this.removeSelectedBookmarksPlugIn, wc);
            this.firstToolbar.addPlugIn(this.removeSelectedCategoriesPlugIn, wc);
            this.firstToolbar.add(Box.createHorizontalGlue());
            this.secondToolbar = new WorkbenchToolBar(null);
            this.secondToolbar.setLayout(new FlowLayout(1, 1, 1));
            this.secondToolbar.setFloatable(false);
            this.secondToolbar.addPlugIn(this.goToSelectedBookmarkPlugIn, wc);
            this.secondToolbar.addPlugIn(this.goToCurrentDatePlugIn, wc);
            this.treeButtonsPanel.add(this.firstToolbar);
            this.treeButtonsPanel.add(this.secondToolbar);
        }
        return this.treeButtonsPanel;
    }

    protected void addNewBookmarkCategory() {
        BookmarkManager.getInstance().addCategory(null);
    }

    protected void editSelectedBookmark() {
        Collection<IBookmark> selectedBookmarks = TreeLayerNamePanel.selectedNodes(IBookmark.class, this.bookmarkTree);
        IBookmark bookmark = selectedBookmarks.iterator().next();
        this.editBookmark(bookmark);
    }

    protected void editBookmark(IBookmark bookmark) {
        EditBookmarkDialog dialog = new EditBookmarkDialog(JUMPWorkbench.getFrameInstance(), true, bookmark);
        dialog.pack();
        GUIUtil.centreOnScreen(dialog);
        dialog.setVisible(true);
        if (dialog.wasOkPressed()) {
            BookmarkManager.getInstance().fireBookmarkChanged(bookmark, BookmarkEventType.METADATA_CHANGED);
        }
    }

    protected void goToSelectedBookmark() {
        Collection<IBookmark> selectedBookmarks = TreeLayerNamePanel.selectedNodes(IBookmark.class, this.bookmarkTree);
        IBookmark bookmark = selectedBookmarks.iterator().next();
        LayerViewPanel panel = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel();
        if (panel != null) {
            Geometry geom = bookmark.getLocalization();
            if (bookmark instanceof TemporalBookmark) {
                Timestamp timemark = ((TemporalBookmark)bookmark).getTimemark();
                List<Layer> layers = panel.getLayerManager().getLayers();
                for (Layer currentLayer : layers) {
                    if (!currentLayer.isVersionable()) continue;
                    currentLayer.setVersionableViewDate(timemark);
                }
                List<Table> tables = JUMPWorkbench.getFrameInstance().getContext().getDataManager().getRealTables();
                for (Table currentTable : tables) {
                    if (!currentTable.isVersionable()) continue;
                    currentTable.setVersionableViewDate(timemark);
                }
                if (geom == null) {
                    panel.repaint();
                }
            }
            if (geom != null) {
                try {
                    panel.getViewport().zoom(geom.getEnvelopeInternal());
                }
                catch (NoninvertibleTransformException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
        }
    }

    protected void removeSelectedBookmarks() {
        Collection<IBookmark> selectedBookmarks = TreeLayerNamePanel.selectedNodes(IBookmark.class, this.bookmarkTree);
        for (IBookmark currentBookmark : selectedBookmarks) {
            BookmarkCategory category = BookmarkManager.getInstance().getCategory(currentBookmark);
            if (category == null) continue;
            BookmarkManager.getInstance().removeBookmark(category.getName(), currentBookmark);
        }
    }

    protected void removeSelectedCategories() {
        Collection<BookmarkCategory> selectedCategories = TreeLayerNamePanel.selectedNodes(BookmarkCategory.class, this.bookmarkTree);
        for (BookmarkCategory currentCategory : selectedCategories) {
            BookmarkManager.getInstance().removeCategory(currentCategory);
        }
    }

    protected void addNewBookmark() {
        Collection<BookmarkCategory> selectedCategories = TreeLayerNamePanel.selectedNodes(BookmarkCategory.class, this.bookmarkTree);
        BookmarkCategory category = BookmarkCategory.DEFAULT_CATEGORY;
        if (!selectedCategories.isEmpty()) {
            category = selectedCategories.iterator().next();
        }
        IBookmark bookmark = BookmarkManager.getInstance().addBookmark(category.getName());
        this.editBookmark(bookmark);
    }

    protected void addNewTemporalBookmark() {
        Collection<BookmarkCategory> selectedCategories = TreeLayerNamePanel.selectedNodes(BookmarkCategory.class, this.bookmarkTree);
        BookmarkCategory category = BookmarkCategory.DEFAULT_CATEGORY;
        if (!selectedCategories.isEmpty()) {
            category = selectedCategories.iterator().next();
        }
        IBookmark bookmark = BookmarkManager.getInstance().addTemporalBookmark(category.getName());
        this.editBookmark(bookmark);
    }

    protected MouseListener getTreeListener() {
        return new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent me) {
                super.mouseClicked(me);
                JTree arbol = (JTree)me.getSource();
                TreePath path = arbol.getSelectionPath();
                Point clickedPoint = me.getPoint();
                if (path != null && arbol.getRowForLocation(clickedPoint.x, clickedPoint.y) == arbol.getRowForPath(path)) {
                    Object selectedNode = path.getLastPathComponent();
                    boolean cfr_ignored_0 = selectedNode instanceof IBookmark;
                }
            }
        };
    }

    protected TreeCellRenderer getTreeCellRenderer() {
        return new DefaultTreeCellRenderer(){
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (leaf && value instanceof IBookmark) {
                    IBookmark bookmark = (IBookmark)value;
                    String name = bookmark.getName();
                    String description = "<HTML><b>" + name + "</b><br>" + bookmark.getDescription() + "</HTML>";
                    if (value instanceof TemporalBookmark) {
                        this.setIcon(BOOKMARK_TIME_ICON);
                        Timestamp timemark = ((TemporalBookmark)bookmark).getTimemark();
                        description = timemark != null ? "<HTML><b>" + name + " - " + DateFormatManager.getDateTimeFormat().format(timemark) + "</b><br>" + bookmark.getDescription() + "</HTML>" : "<HTML><b>" + name + " - " + I18N.getString("org.saig.jump.widgets.utils.bookmarks.BookmarksManagerPanel.Current-date") + "</b><br>" + bookmark.getDescription() + "</HTML>";
                    } else {
                        this.setIcon(BOOKMARK_ICON);
                    }
                    this.setBackgroundNonSelectionColor(this.getBackground());
                    this.setToolTipText(description);
                    JPanel bookmarkPanel = new JPanel(new GridBagLayout());
                    ToolTipManager.sharedInstance().registerComponent(bookmarkPanel);
                    bookmarkPanel.setToolTipText(description);
                    JTextArea descriptionLabel = new JTextArea(bookmark.getDescription());
                    descriptionLabel.setFont(this.getFont().deriveFont(2, 10.0f));
                    descriptionLabel.setBackground(this.getBackground());
                    descriptionLabel.setToolTipText(description);
                    bookmarkPanel.setBackground(this.getBackground());
                    bookmarkPanel.setForeground(this.getForeground());
                    FormUtils.addRowInGBL((JComponent)bookmarkPanel, 0, 0, (JComponent)this, false, false);
                    if (description != null && !description.isEmpty()) {
                        FormUtils.addRowInGBL((JComponent)bookmarkPanel, 1, 0, (JComponent)descriptionLabel, false, false);
                    }
                    return bookmarkPanel;
                }
                this.setIcon(BOOKMARK_CATEGORY_ICON);
                this.setToolTipText(this.getText());
                return this;
            }
        };
    }

    @Override
    public void bookmarkCategoryChanged(BookmarkCategoryEvent event) {
        TreeModelEvent treeModelEvent = new TreeModelEvent((Object)this, new Object[]{this.bookmarkTree.getModel().getRoot()}, new int[]{event.getBookmarkCategoryIndex()}, new Object[]{event.getCategory()});
        if (event.getType() == BookmarkCategoryEventType.ADDED) {
            this.firableTreeModelWrapper.fireTreeNodesInserted(treeModelEvent);
            return;
        }
        if (event.getType() == BookmarkCategoryEventType.REMOVED) {
            this.firableTreeModelWrapper.fireTreeNodesRemoved(treeModelEvent);
            return;
        }
        if (event.getType() == BookmarkCategoryEventType.METADATA_CHANGED) {
            this.firableTreeModelWrapper.fireTreeNodesChanged(treeModelEvent);
            return;
        }
        Assert.shouldNeverReachHere();
    }

    @Override
    public void bookmarkChanged(BookmarkEvent event) {
        TreeModelEvent treeModelEvent = new TreeModelEvent((Object)this, new Object[]{this.bookmarkTree.getModel().getRoot(), event.getCategory()}, new int[]{event.getBookmarkIndex()}, new Object[]{event.getBookmark()});
        if (event.getType() == BookmarkEventType.ADDED) {
            this.firableTreeModelWrapper.fireTreeNodesInserted(treeModelEvent);
            return;
        }
        if (event.getType() == BookmarkEventType.REMOVED) {
            this.firableTreeModelWrapper.fireTreeNodesRemoved(treeModelEvent);
            return;
        }
        if (event.getType() == BookmarkEventType.METADATA_CHANGED) {
            this.firableTreeModelWrapper.fireTreeNodesChanged(treeModelEvent);
            return;
        }
        Assert.shouldNeverReachHere();
    }

    protected void tree_mouseReleased(MouseEvent e) {
        if (!SwingUtilities.isRightMouseButton(e)) {
            return;
        }
        TreePath popupPath = this.bookmarkTree.getPathForLocation(e.getX(), e.getY());
        if (popupPath == null) {
            return;
        }
        this.popupNode = popupPath.getLastPathComponent();
        if (!(e.isControlDown() || e.isShiftDown() || TreeLayerNamePanel.selectedNodes(Object.class, this.bookmarkTree).contains(this.popupNode))) {
            this.bookmarkTree.getSelectionModel().clearSelection();
        }
        this.bookmarkTree.getSelectionModel().addSelectionPath(popupPath);
        TreePath[] treePaths = this.bookmarkTree.getSelectionModel().getSelectionPaths();
        int numObjectsSelected = treePaths.length;
        int numCategories = 0;
        int numBookmarks = 0;
        if (numObjectsSelected > 0) {
            int i = 0;
            while (i < numObjectsSelected) {
                Object nodeSelected = treePaths[i].getLastPathComponent();
                if (nodeSelected instanceof BookmarkCategory) {
                    ++numCategories;
                } else if (nodeSelected instanceof IBookmark) {
                    ++numBookmarks;
                }
                ++i;
            }
        }
        if (numCategories > 0) {
            this.getPopupMenu(BookmarkCategory.class).show(e.getComponent(), e.getX(), e.getY());
        } else if (numBookmarks > 0) {
            this.getPopupMenu(IBookmark.class).show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private JPopupMenu getPopupMenu(Class<?> nodeClass) {
        return (JPopupMenu)CollectionUtil.get(nodeClass, this.nodeClassToPopupMenuMap);
    }

    public void addPopupMenu(Class<?> nodeClass, JPopupMenu popupMenu) {
        this.nodeClassToPopupMenuMap.put(nodeClass, popupMenu);
    }

    private class MoveSelectedBookmarksToCategoryPlugIn
    extends AbstractPlugIn {
        public final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.utils.MoveLayerableToCategoryPlugIn.Move-to-the-category")) + " ...";
        public final Icon ICON = IconLoader.icon("blank.png");

        private MoveSelectedBookmarksToCategoryPlugIn() {
        }

        @Override
        public String getName() {
            return this.NAME;
        }

        @Override
        public Icon getIcon() {
            return this.ICON;
        }

        public JMenu getMenu() {
            JMenu moveLayerableToCategoryMenu = new JMenu(this.NAME);
            moveLayerableToCategoryMenu.setIcon(GUIUtil.toSmallIcon(this.ICON));
            moveLayerableToCategoryMenu.addMenuListener(new MenuListener(){

                @Override
                public void menuCanceled(MenuEvent e) {
                }

                @Override
                public void menuDeselected(MenuEvent e) {
                }

                @Override
                public void menuSelected(MenuEvent e) {
                    JMenu source = (JMenu)e.getSource();
                    source.removeAll();
                    BookmarkManager bm = BookmarkManager.getInstance();
                    for (BookmarkCategory currentCategory : bm.getCategories()) {
                        JMenuItem item = new JMenuItem(currentCategory.getName());
                        source.add(item);
                        item.addActionListener(new ActionListener(){

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                MoveSelectedBookmarksToCategoryPlugIn.this.moveSelectedBookmarksToCategory(((JMenuItem)e.getSource()).getText());
                            }
                        });
                    }
                }
            });
            return moveLayerableToCategoryMenu;
        }

        void moveSelectedBookmarksToCategory(String categoryName) {
            List selectedBookmarks = (List)TreeLayerNamePanel.selectedNodes(IBookmark.class, BookmarksManagerPanel.this.bookmarkTree);
            BookmarkManager bm = BookmarkManager.getInstance();
            Collections.reverse(selectedBookmarks);
            BookmarkCategory destCat = bm.getCategory(categoryName);
            for (IBookmark currentBookmark : selectedBookmarks) {
                bm.removeBookmark(bm.getCategory(currentBookmark), currentBookmark);
                bm.addBookmark(destCat, currentBookmark, 0);
            }
            BookmarksManagerPanel.this.bookmarkTree.getSelectionModel().clearSelection();
            for (IBookmark currentBookmark : selectedBookmarks) {
                BookmarksManagerPanel.this.bookmarkTree.addSelectionPath(TreeUtil.findTreePath(currentBookmark, BookmarksManagerPanel.this.bookmarkTree.getModel()));
            }
        }

        @Override
        public Icon getDisabledIcon() {
            return null;
        }

        @Override
        public EnableCheck getCheck() {
            return null;
        }
    }
}

