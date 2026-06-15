package ar.com.catgis;
import ar.com.catgis.renderer.LineSymbolRenderer;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.data.online.OnlineWmsLayer;

import ar.com.catgis.core.model.Project;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import java.awt.Frame;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.TexturePaint;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutEllipse;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutLegend;
import ar.com.catgis.layout.LayoutLine;
import ar.com.catgis.layout.LayoutImage;
import ar.com.catgis.layout.LayoutRectangle;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.MapFrameViewport;
import ar.com.catgis.layout.LayoutController;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutNorthArrow;
import ar.com.catgis.layout.LayoutRenderContext;
import ar.com.catgis.layout.LayoutScaleBar;
import ar.com.catgis.layout.LayoutTable;
import ar.com.catgis.layout.LayoutTemplateManager;
import ar.com.catgis.layout.CanvasDropTarget;
import ar.com.catgis.layout.GuideLine;
import ar.com.catgis.layout.LayoutCartouche;
import ar.com.catgis.layout.LayoutGraticule;
import ar.com.catgis.layout.QgisQptImporter;
import ar.com.catgis.layout.LayoutImagePrintable;
import ar.com.catgis.layout.LayoutInteractionState;
import ar.com.catgis.layout.LayoutElementType;
import ar.com.catgis.layout.LayoutTemplate;
import ar.com.catgis.layout.LegendPlacement;
import ar.com.catgis.layout.MapFrameTool;
import ar.com.catgis.layout.PreviewScaleMode;
import ar.com.catgis.layout.ResizeHandle;
import ar.com.catgis.layout.RectMm;
import ar.com.catgis.layout.RulerRenderer;
import ar.com.catgis.layout.ScaleRule;
import ar.com.catgis.layout.ScaleStyle;
import ar.com.catgis.layout.NorthStyle;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import ar.com.catgis.layout.LayoutSettings;
import ar.com.catgis.layout.LayoutSnapshot;
import ar.com.catgis.layout.MapFrameGeometry;
import ar.com.catgis.layout.LayoutRenderResult;
import ar.com.catgis.layout.FooterRenderResult;
import ar.com.catgis.layout.LayoutLegendEntry;
import ar.com.catgis.layout.LayoutPageRenderer;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.ui.components.layout.PreviewToolbarActions;

public class MapLayoutComposerDialog extends JFrame implements PreviewToolbarActions, ar.com.catgis.layout.LayoutViewContext {

    // Context helpers - prefer AppContext over static globals
    public static Project ctxProject() {
        Project p = AppContext.get().getProject();
        return p != null ? p : AppContext.project();
    }
    public static MapPanel ctxMapPanel() {
        MapPanel m = AppContext.get().getMapPanel();
        return m != null ? m : AppContext.mapPanel();
    }

    public static final DateTimeFormatter FOOTER_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int CATMAP_SPLASH_MILLIS = 1100;
    public static final int PREVIEW_RENDER_DPI = 200;
    public static final double CLEAN_HEADER_HEIGHT_RATIO = 1d / 14d;
    public static final double CLEAN_FOOTER_HEIGHT_RATIO = 1d / 9d;
    public static final int CLEAN_HEADER_MIN_HEIGHT = 72;
    public static final int CLEAN_FOOTER_MIN_HEIGHT = 120;
    private static MapLayoutComposerDialog openInstance;

    final JTextField titleField;
    final JTextField subtitleField;
    final JTextField footerField;
    final JTextField studyField;
    final JTextField cartoucheProjectField;
    final JTextField companyField;
    final JTextField cartographerField;
    final JTextField imageSourceField;
    final JTextField coordinateReferenceField;
    final JTextField legendTitleField;
    final JTextField legendSubtitleField;
    final JTextField logoPathField;
    final JTextField layoutImagePathField;
    final JTextField mapScaleField;
    final JComboBox<LayoutTemplate> templateCombo;
    final JComboBox<PageSizePreset> pageSizeCombo;
    final JComboBox<PageOrientation> orientationCombo;
    final JComboBox<Integer> dpiCombo;
    final JComboBox<LegendPlacement> legendPlacementCombo;
    final JComboBox<ScaleStyle> scaleStyleCombo;
    final JComboBox<ScaleRule> scaleRuleCombo;
    final JComboBox<NorthStyle> northStyleCombo;
    final JCheckBox northCheck;
    final JCheckBox scaleCheck;
    final JCheckBox legendCheck;
    final JCheckBox gridCheck;
    final JCheckBox gridLabelsCheck;
    final JSpinner gridColumnsSpinner;
    final JSpinner gridRowsSpinner;
    final LayoutInteractionState interactionState;
    final LayoutPreviewPanel previewPanel = new LayoutPreviewPanel(this);
    private final JLabel currentMapLabel;
    private final JLabel scaleInfoLabel;
    final JLabel statusLabel;
    final DefaultListModel<CatmapLayoutItem> layoutItemsModel;
    final JList<CatmapLayoutItem> layoutItemsList;
    final DefaultListModel<Layer> projectLayersModel;
    final JList<Layer> projectLayersList;
    private final JLabel projectLayersSummaryLabel;
    private final JLabel projectLayerDetailLabel;
    private final JLabel inspectorTypeValueLabel;
    private final JTextField inspectorLabelField;
    private final JTextField inspectorXField;
    private final JTextField inspectorYField;
    private final JTextField inspectorWidthField;
    private final JTextField inspectorHeightField;
    private final JTextArea inspectorTextArea;
    private final JTextField inspectorImagePathField;
    private final JSpinner inspectorFontSizeSpinner;
    private final JSpinner inspectorLineWidthSpinner;
    private final JCheckBox inspectorBoldCheck;
    private final JCheckBox inspectorItalicCheck;
    private final JCheckBox inspectorVisibleCheck;
    private final JCheckBox inspectorLockedCheck;
    final JComboBox<CatmapLayoutItem.HorizontalAlign> inspectorAlignCombo;
    private final CardLayout catmapElementsCardLayout;
    private final JPanel catmapElementsCardPanel;
    final DefaultTreeModel layoutStructureTreeModel;
    final JTree layoutStructureTree;
    private JScrollPane controlsScrollPane;
    JScrollPane previewScrollPane;
    private JButton selectionToolButton;
    JButton mapPanToolButton;
    private JButton mapZoomToolButton;
    private ar.com.catgis.ui.components.layout.LayoutPreviewToolbar previewToolbar;
    private LayoutSnapshot snapshot;
    private boolean syncingLayoutStructureSelection;
    private final List<CatmapLayoutItem> catmapClipboard = new ArrayList<>();
    final LayoutModel layoutModel = new LayoutModel();
    final LayoutController layoutController = new LayoutController(layoutModel);
    final ar.com.catgis.layout.CanvasRenderer canvasRenderer
            = new ar.com.catgis.layout.CanvasRenderer(layoutModel, new java.awt.Rectangle(0, 0, 800, 600));
    final ar.com.catgis.layout.LayoutSelectionManager selectionManager
            = new ar.com.catgis.layout.LayoutSelectionManager(layoutModel);
    /** @deprecated Use {@link #selectionManager} instead */
    @Deprecated LayoutElement draggingLayoutElement;
    @Deprecated private String copiedElementType = null;
    @Deprecated private String copiedElementJson = null;
    @Deprecated Point dragStartPagePoint;
    @Deprecated java.awt.geom.Rectangle2D.Double dragStartBoundsMm;
    @Deprecated int activeResizeHandleIndex = -1;
    private DefaultListModel<String> elementListModel;
    private JLabel propertiesInfoLabel;
    private JPanel propertiesCardPanel;

    private MapLayoutComposerDialog(Window owner) {
        super("CATMAP - Workspace cartografico");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        interactionState = new LayoutInteractionState();
        layoutController.setInteractionState(interactionState);

        snapshot = layoutController.captureSnapshot();
        layoutController.setSnapshot(snapshot);

        titleField = new JTextField(defaultTitle(), 24);
        subtitleField = new JTextField(defaultSubtitle(), 24);
        footerField = new JTextField(defaultFooter(), 24);
        studyField = new JTextField(ctxProject() != null ? ctxProject().getStudyName() : "", 24);
        cartoucheProjectField = new JTextField(snapshot != null ? snapshot.projectName() : "", 24);
        companyField = new JTextField(ctxProject() != null ? ctxProject().getCompanyName() : "", 24);
        cartographerField = new JTextField(ctxProject() != null ? ctxProject().getCartographerName() : "", 24);
        imageSourceField = new JTextField(ctxProject() != null ? ctxProject().getImageSource() : "", 24);
        coordinateReferenceField = new JTextField(ctxProject() != null ? ctxProject().getCoordinateReference() : "", 24);
        legendTitleField = new JTextField(ctxProject() != null ? ctxProject().getLegendTitle() : "Leyenda", 24);
        legendSubtitleField = new JTextField(ctxProject() != null ? ctxProject().getLegendSubtitle() : "Capas visibles del mapa", 24);
        logoPathField = new JTextField(ctxProject() != null ? ctxProject().getLogoPath() : "", 24);
        logoPathField.setEditable(false);
        layoutImagePathField = new JTextField(ctxProject() != null ? ctxProject().getLayoutImagePath() : "", 24);
        layoutImagePathField.setEditable(false);
        mapScaleField = new JTextField("1:10000", 24);
        templateCombo = new JComboBox<>(LayoutTemplate.values());
        templateCombo.setSelectedItem(interactionState.getTemplate());
        pageSizeCombo = new JComboBox<>(PageSizePreset.values());
        pageSizeCombo.setSelectedItem(PageSizePreset.A4);
        orientationCombo = new JComboBox<>(PageOrientation.values());
        orientationCombo.setSelectedItem(PageOrientation.LANDSCAPE);
        dpiCombo = new JComboBox<>(new Integer[]{150, 200, 300});
        dpiCombo.setSelectedItem(200);
        legendPlacementCombo = new JComboBox<>(LegendPlacement.values());
        scaleStyleCombo = new JComboBox<>(ScaleStyle.values());
        scaleRuleCombo = new JComboBox<>(ScaleRule.values());
        northStyleCombo = new JComboBox<>(NorthStyle.values());
        northStyleCombo.setRenderer((JList<? extends NorthStyle> list, NorthStyle value, int index, boolean isSelected, boolean cellHasFocus) -> {
            JLabel label = (JLabel) new javax.swing.DefaultListCellRenderer().getListCellRendererComponent(
                    list,
                    value != null ? value.toString() : "",
                    index,
                    isSelected,
                    cellHasFocus
            );
            if (value != null) {
                label.setIcon(LayoutPageRenderer.createNorthPreviewIcon(value, 18));
                label.setIconTextGap(8);
            }
            return label;
        });
        northCheck = new JCheckBox("Norte", currentProjectShowNorth());
        scaleCheck = new JCheckBox("Escala grafica", true);
        legendCheck = new JCheckBox("Leyenda", true);
        gridCheck = new JCheckBox("Grilla cartografica", true);
        gridLabelsCheck = new JCheckBox("Etiquetas de grilla", true);
        gridColumnsSpinner = new JSpinner(new SpinnerNumberModel(3, 2, 20, 1));
        gridRowsSpinner = new JSpinner(new SpinnerNumberModel(3, 2, 20, 1));
        layoutItemsModel = new DefaultListModel<>();
        layoutItemsList = new JList<>(layoutItemsModel);
        layoutItemsList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        layoutItemsList.setVisibleRowCount(6);
        layoutItemsList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            DefaultListCellRenderer renderer = new DefaultListCellRenderer();
            CatmapLayoutItem item = value instanceof CatmapLayoutItem ? (CatmapLayoutItem) value : null;
            String label = item != null
                    ? (index + 1) + ". " + kindLabel(item.getKind()) + " | " + safeTrim(item.getLabel().isBlank() ? item.getText() : item.getLabel())
                    : "";
            JLabel cell = (JLabel) renderer.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            if (item != null) {
                cell.setIcon(iconForCatmapKind(item.getKind()));
                String state = (item.isVisible() ? "visible" : "oculto") + (item.isLocked() ? " | bloqueado" : "");
                cell.setToolTipText("Pos " + item.getX() + "," + item.getY() + " | Tam " + item.getWidth() + "x" + item.getHeight() + " | " + state);
                if (!item.isVisible()) {
                    cell.setForeground(new Color(148, 163, 184));
                }
                if (item.isLocked()) {
                    cell.setText(cell.getText() + " [B]");
                }
            }
            return cell;
        });
        projectLayersModel = new DefaultListModel<>();
        projectLayersList = new JList<>(projectLayersModel);
        projectLayersList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        projectLayersList.setVisibleRowCount(12);
        projectLayersList.setFixedCellHeight(48);
        projectLayersList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JPanel cell = new JPanel(new BorderLayout(8, 0));
            cell.setOpaque(true);
            cell.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            cell.setBackground(isSelected ? new Color(232, 242, 255) : Color.WHITE);

            JCheckBox visibleCheck = new JCheckBox();
            visibleCheck.setSelected(value != null && value.isVisible());
            visibleCheck.setEnabled(false);
            visibleCheck.setOpaque(false);

            JLabel label = new JLabel();
            label.setOpaque(false);
            if (value != null) {
                String name = escape(safeTrim(value.getName()).isBlank() ? "Capa sin nombre" : value.getName());
                String detail = escape(projectLayerTypeLabel(value));
                label.setText("<html><b>" + name + "</b><br><span style='color:#64748b'>" + detail + "</span></html>");
                label.setIcon(iconForProjectLayer(value));
                label.setIconTextGap(8);
                label.setToolTipText((value.isVisible() ? "Visible" : "Oculta") + " | Doble clic para editar.");
                if (!value.isVisible()) {
                    label.setForeground(new Color(148, 163, 184));
                }
            }

            cell.add(visibleCheck, BorderLayout.WEST);
            cell.add(label, BorderLayout.CENTER);
            return cell;
        });
        projectLayersSummaryLabel = new JLabel("Capas del mapa: 0");
        projectLayerDetailLabel = new JLabel("<html>Selecciona una capa para controlar visibilidad y simbologia sin salir de CATMAP.</html>");
        projectLayerDetailLabel.setForeground(new Color(88, 98, 112));
        projectLayerDetailLabel.setFont(projectLayerDetailLabel.getFont().deriveFont(Font.PLAIN, 11f));
        inspectorTypeValueLabel = new JLabel("Sin seleccion");
        inspectorLabelField = new JTextField(20);
        inspectorXField = new JTextField(6);
        inspectorYField = new JTextField(6);
        inspectorWidthField = new JTextField(6);
        inspectorHeightField = new JTextField(6);
        inspectorTextArea = new JTextArea(4, 20);
        inspectorTextArea.setLineWrap(true);
        inspectorTextArea.setWrapStyleWord(true);
        inspectorImagePathField = new JTextField(20);
        inspectorImagePathField.setEditable(false);
        inspectorFontSizeSpinner = new JSpinner(new SpinnerNumberModel(18, 8, 144, 1));
        inspectorLineWidthSpinner = new JSpinner(new SpinnerNumberModel(2.0d, 1.0d, 24.0d, 0.5d));
        inspectorBoldCheck = new JCheckBox("Negrita");
        inspectorItalicCheck = new JCheckBox("Cursiva");
        inspectorVisibleCheck = new JCheckBox("Visible", true);
        inspectorLockedCheck = new JCheckBox("Bloqueado", false);
        inspectorAlignCombo = new JComboBox<>(CatmapLayoutItem.HorizontalAlign.values());
        catmapElementsCardLayout = new CardLayout();
        catmapElementsCardPanel = new JPanel(catmapElementsCardLayout);
        layoutStructureTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(LayoutStructureNode.root("CATMAP")));
        layoutStructureTree = new JTree(layoutStructureTreeModel);
        layoutStructureTree.setRootVisible(false);
        layoutStructureTree.setShowsRootHandles(true);
        layoutStructureTree.setRowHeight(22);
        layoutStructureTree.setToggleClickCount(0);
        layoutStructureTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        layoutStructureTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof LayoutStructureNode data) {
                    label.setText(data.label());
                    label.setToolTipText(layoutStructureTooltip(data));
                    label.setIcon(iconForLayoutStructureNode(data));
                    if (!selected && !data.visible()) {
                        label.setForeground(new Color(148, 163, 184));
                    }
                    if (data.locked()) {
                        label.setText(label.getText() + " [B]");
                    }
                }
                return label;
            }
        });
        installDropTarget();
        currentMapLabel = new JLabel();
        scaleInfoLabel = new JLabel("Escala real actual: calculando...");
        statusLabel = new JLabel("CATMAP listo para maquetar, editar y exportar.");

        northStyleCombo.setSelectedItem(currentProjectNorthStyle());

        loadCatmapItemsFromProject();
        loadProjectLayersFromProject();
        // --- Element list panel (WEST) ---
        add(new ar.com.catgis.ui.components.layout.LayoutElementListPanel(
                layoutModel,
                this::refreshElementList,
                this::refreshPropertiesPanel,
                () -> previewPanel.repaint(),
                this::centerOnElement,
                this::addQuickElement,
                this::showTemplatePicker,
                this::undo,
                this::redo,
                () -> { LayoutElement sel = layoutModel.getSelected(); if (sel != null) { duplicateLayoutElement(sel); refreshElementList(); previewPanel.repaint(); } },
                () -> { LayoutElement sel = layoutModel.getSelected(); if (sel != null) { layoutModel.removeElement(sel.getId()); refreshElementList(); previewPanel.repaint(); } },
                this::alignElements),
                BorderLayout.WEST);
        add(buildPreviewContainer(), BorderLayout.CENTER);
        add(new javax.swing.JPanel(), BorderLayout.EAST);
        add(new ar.com.catgis.ui.components.layout.LayoutBottomBar(statusLabel, this::exportImage, this::exportPdf, this::printLayout, this::dispose), BorderLayout.SOUTH);

        updateCurrentMapLabel();
        installListeners();
        installCatmapKeyboardActions();
        applyTemplateDefaults((LayoutTemplate) templateCombo.getSelectedItem(), false);
        applyInitialDocumentDefaults();
        installInitialOpenBehavior();

        WindowLayoutSupport.fitFrameToScreen(this, 1380, 900, 1040, 700);
        setLocationRelativeTo(owner);

        // NOTE: No default LayoutMap is added here because the template
        // (LayoutPageRenderer.drawMapFrame) already renders the live map frame
        // from the current MapPanel view. LayoutMap elements are intended
        // for additional/inset maps only. This avoids dual-map rendering.

        // Legend — placed below the map area with subtle background for contrast
        LayoutLegend legend = new LayoutLegend("main-legend", 15, 148, 267, 40);
        legend.setZOrder(layoutModel.nextZ());
        legend.setAutoHeight(true);
        legend.setShowBackground(true);
        legend.setShowBorder(true);
        legend.setName("Leyenda");
        legend.setTitle("Leyenda");
        populateLegendFromProject(legend);
        layoutModel.addElement(legend);

        // Scale bar — bottom left of page
        LayoutScaleBar scale = new LayoutScaleBar("main-scale", 15, 197, 120, 10);
        scale.setZOrder(layoutModel.nextZ());
        scale.setName("Escala");
        scale.setUnitLabel("m");
        layoutModel.addElement(scale);

        // North arrow — top right of map area
        LayoutNorthArrow north = new LayoutNorthArrow("main-north", 272, 30, 16, 22);
        north.setZOrder(layoutModel.nextZ());
        north.setName("Norte");
        layoutModel.addElement(north);

        // Header: title and subtitle as LayoutElements
        String titleText = defaultTitle();
        if (titleText == null || titleText.isBlank()) titleText = "Mapa";
        LayoutLabel titleEl = new LayoutLabel("header-title", titleText, 12, 10, 270, 16);
        titleEl.setZOrder(layoutModel.nextZ());
        titleEl.setName("Titulo");
        titleEl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleEl.setColor(new Color(0x1B2638));
        layoutModel.addElement(titleEl);

        String subText = defaultSubtitle();
        if (subText == null || subText.isBlank()) subText = "Salida cartografica";
        LayoutLabel subEl = new LayoutLabel("header-subtitle", subText, 12, 29, 270, 12);
        subEl.setZOrder(layoutModel.nextZ());
        subEl.setName("Subtitulo");
        subEl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subEl.setColor(new Color(0x5B6778));
        layoutModel.addElement(subEl);

        // Footer / cartouche data as LayoutLabels
        String company = ctxProject() != null ? ctxProject().getCompanyName() : null;
        if (company != null && !company.isBlank()) {
            LayoutLabel coEl = new LayoutLabel("footer-company", company, 12, 192, 130, 10);
            coEl.setZOrder(layoutModel.nextZ());
            coEl.setName("Empresa");
            coEl.setFont(new Font("SansSerif", Font.PLAIN, 8));
            coEl.setColor(new Color(0x6B7280));
            layoutModel.addElement(coEl);
        }
        String cartographer = ctxProject() != null ? ctxProject().getCartographerName() : null;
        if (cartographer != null && !cartographer.isBlank()) {
            LayoutLabel caEl = new LayoutLabel("footer-cartographer", cartographer, 148, 192, 130, 10);
            caEl.setZOrder(layoutModel.nextZ());
            caEl.setName("Cartografo");
            caEl.setFont(new Font("SansSerif", Font.PLAIN, 8));
            caEl.setColor(new Color(0x6B7280));
            layoutModel.addElement(caEl);
        }
    }

    public void showCartouchePopup(LayoutCartouche cartouche) {
        JDialog popup = new JDialog(this, "Datos cartograficos", true);
        popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout(8,8));
        panel.setBorder(BorderFactory.createEmptyBorder(14,16,12,16));
        panel.setBackground(Color.WHITE);

        JPanel form = new JPanel(new java.awt.GridLayout(0, 2, 4, 6));
        form.setBackground(Color.WHITE);
        java.util.Map<String, JTextField> fields = new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<String, String> e : cartouche.getFields().entrySet()) {
            JLabel lbl = new JLabel(e.getKey()); lbl.setFont(lbl.getFont().deriveFont(11f));
            JTextField tf = new JTextField(e.getValue() != null ? e.getValue() : "", 20);
            form.add(lbl); form.add(tf);
            fields.put(e.getKey(), tf);
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btns.setBackground(Color.WHITE);
        JButton acceptBtn = new JButton("Aceptar"); JButton cancelBtn = new JButton("Cancelar");
        btns.add(acceptBtn); btns.add(cancelBtn);

        acceptBtn.addActionListener(e -> {
            for (java.util.Map.Entry<String, JTextField> fe : fields.entrySet())
                cartouche.setField(fe.getKey(), fe.getValue().getText());
            previewPanel.repaint(); refreshElementList(); popup.dispose();
        });
        cancelBtn.addActionListener(e -> popup.dispose());

        javax.swing.AbstractAction doAccept = new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { acceptBtn.doClick(); } };
        panel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "accept");
        panel.getActionMap().put("accept", doAccept);
        panel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        panel.getActionMap().put("cancel", new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { popup.dispose(); } });

        panel.add(form, BorderLayout.CENTER); panel.add(btns, BorderLayout.SOUTH);
        popup.add(panel); popup.pack(); popup.setResizable(false);
        popup.getRootPane().setDefaultButton(acceptBtn);
        popup.setLocationRelativeTo(this); popup.setVisible(true);
    }

    public void showScalePopup(LayoutScaleBar scale) {
        JDialog popup = new JDialog(this, "Escala grafica", true);
        popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout(8,8));
        panel.setBorder(BorderFactory.createEmptyBorder(14,16,12,16));
        panel.setBackground(Color.WHITE);

        JPanel form = new JPanel(new java.awt.GridLayout(0,2,4,10));
        form.setBackground(Color.WHITE);

        JLabel segLbl = new JLabel("Segmentos"); form.add(segLbl);
        Integer[] segVals = {2,3,4,5,6,8};
        JComboBox<Integer> segCombo = new JComboBox<>(segVals); segCombo.setSelectedItem(scale.getSegments()); form.add(segCombo);

        JLabel colLbl = new JLabel("Color"); form.add(colLbl);
        JButton colorBtn = new JButton(); colorBtn.setBackground(scale.getColor()); colorBtn.setOpaque(true); form.add(colorBtn);
        colorBtn.addActionListener(e -> { Color c = JColorChooser.showDialog(popup,"Color",colorBtn.getBackground()); if(c!=null){scale.setColor(c);colorBtn.setBackground(c);previewPanel.repaint();} });

        JLabel uniLbl = new JLabel("Unidad"); form.add(uniLbl);
        JTextField unitField = new JTextField(scale.getUnitLabel(), 10); form.add(unitField);

        JLabel sclLbl = new JLabel("Escala 1:"); form.add(sclLbl);
        JTextField scaleField = new JTextField(String.valueOf((long)scale.getMapScaleDenominator()), 10);
        scaleField.setToolTipText("Escala real del mapa. Ej: 5000 = 1:5000"); form.add(scaleField);

        JButton acceptBtn = new JButton("Aceptar"); JButton cancelBtn = new JButton("Cancelar");
        acceptBtn.addActionListener(e -> {
            scale.setSegments((Integer)segCombo.getSelectedItem());
            scale.setUnitLabel(unitField.getText().trim());
            try { double s = Double.parseDouble(scaleField.getText()); if(s>0) scale.setMapScaleDenominator(s); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); }
            previewPanel.repaint(); popup.dispose();
        });
        cancelBtn.addActionListener(e -> popup.dispose());

        javax.swing.AbstractAction doAccept = new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { acceptBtn.doClick(); } };
        panel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "accept");
        panel.getActionMap().put("accept", doAccept);
        panel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        panel.getActionMap().put("cancel", new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { popup.dispose(); } });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btns.setBackground(Color.WHITE); btns.add(acceptBtn); btns.add(cancelBtn);
        panel.add(form, BorderLayout.CENTER); panel.add(btns, BorderLayout.SOUTH);
        popup.add(panel); popup.pack(); popup.setResizable(false); popup.setLocationRelativeTo(this); popup.setVisible(true);
    }

    public void showNorthPopup(LayoutNorthArrow north) {
        JDialog popup = new JDialog(this, "Norte", true);
        popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout(8,8));
        panel.setBorder(BorderFactory.createEmptyBorder(14,16,12,16));
        panel.setBackground(Color.WHITE);

        JPanel form = new JPanel(new java.awt.GridLayout(0,2,4,8));
        form.setBackground(Color.WHITE);

        JLabel xLbl = new JLabel("X (mm)"); form.add(xLbl);
        JTextField xField = new JTextField(String.format("%.0f", north.getBoundsMm().x), 8); form.add(xField);
        JLabel yLbl = new JLabel("Y (mm)"); form.add(yLbl);
        JTextField yField = new JTextField(String.format("%.0f", north.getBoundsMm().y), 8); form.add(yField);
        JLabel wLbl = new JLabel("Tamano (mm)"); form.add(wLbl);
        JTextField wField = new JTextField(String.format("%.0f", north.getBoundsMm().width), 8); form.add(wField);

        JButton acceptBtn = new JButton("Aceptar"); JButton cancelBtn = new JButton("Cancelar");
        acceptBtn.addActionListener(e -> {
            try { north.setBoundsMm(Double.parseDouble(xField.getText()), Double.parseDouble(yField.getText()), Double.parseDouble(wField.getText()), Double.parseDouble(wField.getText())); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); }
            previewPanel.repaint(); popup.dispose();
        });
        cancelBtn.addActionListener(e -> popup.dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btns.setBackground(Color.WHITE); btns.add(acceptBtn); btns.add(cancelBtn);
        panel.add(form, BorderLayout.CENTER); panel.add(btns, BorderLayout.SOUTH);
        popup.add(panel); popup.pack(); popup.setResizable(false); popup.setLocationRelativeTo(this); popup.setVisible(true);
    }

    public void showMapPropsPopup(LayoutMap map) {
        JDialog popup = new JDialog(this, "Propiedades del mapa", true);
        popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout(8,8));
        panel.setBorder(BorderFactory.createEmptyBorder(14,16,12,16));
        panel.setBackground(Color.WHITE);

        JPanel form = new JPanel(new java.awt.GridLayout(0,2,4,6));
        form.setBackground(Color.WHITE);

        // Scale
        form.add(new JLabel("Escala 1:"));
        JTextField scaleField = new JTextField(map.getTargetScaleDenominator() > 0 ? String.valueOf((long)map.getTargetScaleDenominator()) : "", 10);
        scaleField.setToolTipText("Escala deseada. Ej: 5000 para 1:5000");
        form.add(scaleField);

        JButton applyScaleBtn = new JButton("Aplicar escala");
        applyScaleBtn.addActionListener(e -> {
            try {
                double s = Double.parseDouble(scaleField.getText());
                if (s > 0) {
                    map.setTargetScaleDenominator(s);
                    map.getViewport().setScaleDenominator(s);
                    applyMapScale(s);
                    previewPanel.repaint();
                }
            } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); }
        });
        form.add(new JLabel()); form.add(applyScaleBtn);

        // Viewport controls
        form.add(new JLabel("--- Vista independiente ---"));
        form.add(new JLabel());

        MapFrameViewport vp = map.getViewport();
        JTextField minXField = new JTextField(String.format("%.1f", vp.getMinX()), 8);
        JTextField minYField = new JTextField(String.format("%.1f", vp.getMinY()), 8);
        JTextField maxXField = new JTextField(String.format("%.1f", vp.getMaxX()), 8);
        JTextField maxYField = new JTextField(String.format("%.1f", vp.getMaxY()), 8);

        form.add(new JLabel("Min X:")); form.add(minXField);
        form.add(new JLabel("Min Y:")); form.add(minYField);
        form.add(new JLabel("Max X:")); form.add(maxXField);
        form.add(new JLabel("Max Y:")); form.add(maxYField);

        JButton applyExtentBtn = new JButton("Aplicar extensiÃ³n");
        applyExtentBtn.addActionListener(e -> {
            try {
                double mnX = Double.parseDouble(minXField.getText());
                double mnY = Double.parseDouble(minYField.getText());
                double mxX = Double.parseDouble(maxXField.getText());
                double mxY = Double.parseDouble(maxYField.getText());
                vp.fitToExtent(mnX, mnY, mxX, mxY);
                map.invalidateRenderCache();
                previewPanel.repaint();
            } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); }
        });
        form.add(new JLabel()); form.add(applyExtentBtn);

        JButton fitMainBtn = new JButton("Copiar vista del mapa principal");
        fitMainBtn.addActionListener(e -> {
            vp.fitFromMainMap();
            map.invalidateRenderCache();
            previewPanel.repaint();
            minXField.setText(String.format("%.1f", vp.getMinX()));
            minYField.setText(String.format("%.1f", vp.getMinY()));
            maxXField.setText(String.format("%.1f", vp.getMaxX()));
            maxYField.setText(String.format("%.1f", vp.getMaxY()));
        });
        form.add(new JLabel()); form.add(fitMainBtn);

        // Grid
        JCheckBox gridChk = new JCheckBox("Mostrar grilla", map.isShowGrid());
        gridChk.addActionListener(e -> { map.setShowGrid(gridChk.isSelected()); previewPanel.repaint(); });
        form.add(new JLabel()); form.add(gridChk);

        // Indicator
        JCheckBox indicatorChk = new JCheckBox("Mostrar indicador (inset)", map.isShowIndicator());
        indicatorChk.addActionListener(e -> { map.setShowIndicator(indicatorChk.isSelected()); map.invalidateRenderCache(); previewPanel.repaint(); });
        form.add(new JLabel()); form.add(indicatorChk);

        JButton closeBtn = new JButton("Cerrar");
        closeBtn.addActionListener(e -> popup.dispose());
        form.add(new JLabel()); form.add(closeBtn);

        panel.add(form, BorderLayout.CENTER);
        popup.add(panel); popup.pack(); popup.setResizable(false); popup.setLocationRelativeTo(this); popup.setVisible(true);
    }

    public void showTemplatePicker() {
        JDialog dialog = new JDialog(this, "Elegir plantilla CATMAP", true);
        dialog.setLayout(new BorderLayout(10, 10));

        DefaultListModel<TemplateChoice> model = new DefaultListModel<>();
        for (TemplateChoice choice : curatedTemplateChoices()) {
            model.addElement(choice);
        }
        JList<TemplateChoice> list = new JList<>(model);
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(14);
        list.setCellRenderer((jl, value, index, isSelected, cellHasFocus) -> {
            JPanel row = new JPanel(new BorderLayout());
            row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            row.setOpaque(true);
            boolean isHeader = value != null && value.key().isEmpty();
            if (isHeader) {
                row.setBackground(new Color(0xF0F2F5));
                JLabel title = new JLabel(value.displayName());
                title.setFont(title.getFont().deriveFont(Font.BOLD, 11f));
                title.setForeground(new Color(0x6B7280));
                row.add(title, BorderLayout.CENTER);
            } else {
                row.setBackground(isSelected ? new Color(232, 242, 255) : Color.WHITE);
                JLabel title = new JLabel(value.displayName());
                title.setFont(title.getFont().deriveFont(isSelected ? Font.BOLD : Font.PLAIN, 12f));
                row.add(title, BorderLayout.CENTER);
            }
            return row;
        });

        JLabel previewTitle = new JLabel("Vista previa de plantilla");
        previewTitle.setFont(previewTitle.getFont().deriveFont(Font.BOLD, 13f));
        JLabel previewLabel = new JLabel("", JLabel.CENTER);
        previewLabel.setPreferredSize(new Dimension(420, 300));
        previewLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 212, 222)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JTextArea help = new JTextArea("ElegÃ­ una plantilla curada. La vista previa muestra el encuadre general, la ubicaciÃ³n de leyenda, cartucho, escala y norte antes de aplicarla.");
        help.setLineWrap(true);
        help.setWrapStyleWord(true);
        help.setEditable(false);
        help.setOpaque(false);
        help.setFont(help.getFont().deriveFont(Font.PLAIN, 11f));

        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            TemplateChoice selected = list.getSelectedValue();
            if (selected != null) {
                previewLabel.setIcon(new ImageIcon(renderTemplatePreview(selected.key(), 400, 280)));
                help.setText(selected.description());
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
                    TemplateChoice sel = list.getSelectedValue();
                    if (sel != null && !sel.key().isEmpty()) {
                        applySelectedTemplateFromDialog(sel, dialog);
                    }
                }
            }
        });

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 10));
        right.add(previewTitle, BorderLayout.NORTH);
        right.add(previewLabel, BorderLayout.CENTER);
        right.add(help, BorderLayout.SOUTH);

        JPanel left = new JPanel(new BorderLayout(6, 6));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 6));
        JLabel leftTitle = new JLabel("Plantillas recomendadas");
        leftTitle.setFont(leftTitle.getFont().deriveFont(Font.BOLD, 13f));
        left.add(leftTitle, BorderLayout.NORTH);
        left.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton apply = new JButton("Aplicar");
        JButton cancel = new JButton("Cancelar");
        apply.addActionListener(e -> {
            TemplateChoice sel = list.getSelectedValue();
            if (sel != null && !sel.key().isEmpty()) applySelectedTemplateFromDialog(sel, dialog);
        });
        cancel.addActionListener(e -> dialog.dispose());
        buttons.add(apply);
        buttons.add(cancel);

        dialog.add(left, BorderLayout.WEST);
        dialog.add(right, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setSize(760, 470);
        dialog.setLocationRelativeTo(this);
        dialog.getRootPane().setDefaultButton(apply);
        if (!model.isEmpty()) {
            list.setSelectedIndex(0);
        }
        dialog.setVisible(true);
    }

    private record TemplateChoice(String key, String displayName, String description) {
        @Override public String toString() { return displayName; }
    }

    private List<TemplateChoice> curatedTemplateChoices() {
        List<TemplateChoice> items = new ArrayList<>();
        java.util.Map<String, String[]> preferred = new java.util.LinkedHashMap<>();
        // A4 section header
        items.add(new TemplateChoice("", "â€” A4 (297Ã—210 mm) â€”", ""));
        // A4 templates
        preferred.put("A4_REFERENCIA", new String[]{"Infraestructura Â· A4 Â· Ubicacion general", "Mapa de ubicacion, leyenda lateral, cartucho."});
        preferred.put("A4_ACCESIBILIDAD", new String[]{"Infraestructura Â· A4 Â· Acceso operativo", "Rutas y caminos con cartucho compacto."});
        preferred.put("A4_EMPLAZAMIENTO", new String[]{"Infraestructura Â· A4 Â· Emplazamiento tecnico", "Planta general de proyecto, leyenda y escala."});
        preferred.put("A4_INFRAESTRUCTURA", new String[]{"Infraestructura Â· A4 Â· Obra civil", "Ductos, locaciones, red lineal."});
        preferred.put("A4_TECNICO", new String[]{"Tecnica Â· A4 Â· Leyenda derecha", "Composicion tecnica con leyenda lateral."});
        preferred.put("A4_TECNICO_INFERIOR", new String[]{"Tecnica Â· A4 Â· Leyenda inferior", "Mapa dominante con leyenda abajo."});
        preferred.put("A4_TECNICO_CARTUCHO", new String[]{"Tecnica Â· A4 Â· Cartucho inferior", "Mapa + cartucho tecnico."});
        preferred.put("A4_TECNICO_LIMPIA", new String[]{"Tecnica Â· A4 Â· Mapa limpio", "Solo mapa, titulo y escala. Sin cartucho."});
        preferred.put("A4_AMBIENTAL", new String[]{"Ambiental Â· A4 Â· Estandar", "Mapas ambientales y de monitoreo."});
        preferred.put("A4_AMBIENTAL_LEYENDA_LATERAL", new String[]{"Ambiental Â· A4 Â· Leyenda lateral", "Ambiental con leyenda a la derecha."});
        preferred.put("A4_AMBIENTAL_SATELITAL", new String[]{"Ambiental Â· A4 Â· Base satelital", "Imagen satelital con overlay ambiental."});
        preferred.put("A4_HIDROLOGIA", new String[]{"Hidrologia Â· A4 Â· General", "Drenaje, cuencas, escorrentia."});
        preferred.put("A4_HIDRO_DRENAJE", new String[]{"Hidrologia Â· A4 Â· Drenaje", "Red de drenaje superficial."});
        preferred.put("A4_TOPOGRAFIA", new String[]{"Topografia Â· A4 Â· Curvas de nivel", "Relieve y curvas con balance visual."});
        preferred.put("A4_CATASTRAL", new String[]{"Catastral Â· A4 Â· Estandar", "Plano parcelario, mapa dominante."});
        preferred.put("A4_PARCELARIO", new String[]{"Catastral Â· A4 Â· Parcelario con tabla", "Parcelas + tabla de datos."});
        preferred.put("A4_MUESTREO", new String[]{"Ambiental Â· A4 Â· Muestreo", "Puntos de muestreo y leyenda clara."});
        preferred.put("A4_SATELITAL", new String[]{"Satelital Â· A4 Â· Estandar", "Imagen satelital con titulo y escala."});
        preferred.put("A4_PERFIL", new String[]{"Perfil Â· A4 Â· Altimetria tecnica", "Traza, progresivas y tabla altimetrica."});
        preferred.put("A4_PERFIL_MAPA", new String[]{"Perfil Â· A4 Â· Mapa + perfil", "Mapa de traza + perfil altimetrico."});
        preferred.put("A4_INSTITUCIONAL", new String[]{"Institucional Â· A4 Â· Presentacion", "Salida institucional sobria."});
        // A3 section header
        items.add(new TemplateChoice("", "â€” A3 (420Ã—297 mm) â€”", ""));
        // A3 templates
        preferred.put("A3_TECNICO", new String[]{"Tecnica Â· A3 Â· General", "Formato grande, mapa amplio con leyenda lateral."});
        preferred.put("A3_AMBIENTAL", new String[]{"Ambiental Â· A3 Â· Estandar", "Informe ambiental con buena superficie de lectura."});
        preferred.put("A3_CATASTRAL", new String[]{"Catastral Â· A3 Â· Estandar", "Plano parcelario A3, mas aire visual."});
        preferred.put("A3_SATELITAL", new String[]{"Satelital Â· A3 Â· Estandar", "Imagen satelital A3."});
        preferred.put("A3_HIDROLOGIA", new String[]{"Hidrologia Â· A3 Â· General", "Cuencas y drenaje en formato grande."});
        preferred.put("A3_TOPOGRAFIA", new String[]{"Topografia Â· A3 Â· General", "Relieve y curvas en A3."});
        preferred.put("A3_PRESENTACION", new String[]{"Institucional Â· A3 Â· Presentacion", "Salida institucional con mapa dominante."});
        java.util.Map<String, String> all = LayoutTemplateManager.getTemplateList();
        for (java.util.Map.Entry<String, String[]> entry : preferred.entrySet()) {
            String key = entry.getKey();
            if (all.containsKey(key)) {
                String[] data = entry.getValue();
                items.add(new TemplateChoice(key, data[0], data[1]));
            }
        }
        return items;
    }

    private void applySelectedTemplateFromDialog(TemplateChoice selected, JDialog dialog) {
        if (selected == null) return;
        String key = selected.key();
        // Auto-set page size based on template
        if (key.startsWith("A3_")) {
            pageSizeCombo.setSelectedItem(PageSizePreset.A3);
            orientationCombo.setSelectedItem(key.contains("VERTICAL") ? PageOrientation.PORTRAIT : PageOrientation.LANDSCAPE);
        } else {
            pageSizeCombo.setSelectedItem(PageSizePreset.A4);
            orientationCombo.setSelectedItem(key.contains("VERTICAL") ? PageOrientation.PORTRAIT : PageOrientation.LANDSCAPE);
        }
        LayoutTemplateManager.applyTemplate(key, layoutModel);
        refreshAll();
        statusLabel.setText("Plantilla aplicada: " + selected.displayName());
        dialog.dispose();
    }

    private BufferedImage renderTemplatePreview(String key, int width, int height) {
        LayoutModel previewModel = new LayoutModel();
        LayoutTemplateManager.applyTemplate(key, previewModel);
        double pageW = key.startsWith("A3_") ? 420d : 297d;
        double pageH = key.startsWith("A3_") ? 297d : 210d;
        if (key.contains("VERTICAL")) {
            double tmp = pageW;
            pageW = pageH;
            pageH = tmp;
        }
        int dpi = 96;
        int renderW = Math.max(640, (int) Math.round(pageW / 25.4d * dpi));
        int renderH = Math.max(480, (int) Math.round(pageH / 25.4d * dpi));
        BufferedImage canvas = new BufferedImage(renderW, renderH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, renderW, renderH);
            g2.setColor(new Color(210, 216, 224));
            g2.drawRect(0, 0, renderW - 1, renderH - 1);
            LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.EXPORT_IMAGE, dpi, pageW, pageH);
            for (LayoutElement el : previewModel.getVisibleElementsSortedByZ()) {
                if (el instanceof LayoutScaleBar) {
                    ((LayoutScaleBar) el).setMapScaleDenominator(Math.max(100, estimateMapScale()));
                }
                el.render(g2, ctx);
            }
        } finally {
            g2.dispose();
        }

        BufferedImage thumb = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = thumb.createGraphics();
        try {
            tg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            tg.setColor(new Color(248, 250, 252));
            tg.fillRect(0, 0, width, height);
            double fit = Math.min(width / (double) canvas.getWidth(), height / (double) canvas.getHeight());
            int drawW = Math.max(1, (int) Math.round(canvas.getWidth() * fit));
            int drawH = Math.max(1, (int) Math.round(canvas.getHeight() * fit));
            int dx = (width - drawW) / 2;
            int dy = (height - drawH) / 2;
            tg.drawImage(canvas, dx, dy, drawW, drawH, null);
        } finally {
            tg.dispose();
        }
        return thumb;
    }

    private void autoComposeLayout() {
        // Show quick-pick menu with variants
        JPopupMenu menu = new JPopupMenu("Auto-componer");
        String[][] opts = {
            {"A4_REFERENCIA", "Infraestructura Â· Ubicacion general", "Mapa + leyenda lateral + cartucho"},
            {"A4_EMPLAZAMIENTO", "Infraestructura Â· Emplazamiento", "Mapa dominante + cartucho inferior"},
            {"A4_TECNICO", "Tecnica Â· Leyenda derecha", "Mapa + leyenda lateral compacta"},
            {"A4_AMBIENTAL", "Ambiental Â· Estandar", "Mapa + leyenda inferior"},
            {"A4_PERFIL", "Perfil Â· Altimetria", "Mapa de traza + tabla progresivas"},
        };
        for (String[] opt : opts) {
            JMenuItem mi = new JMenuItem(opt[1]);
            mi.setToolTipText(opt[2]);
            mi.addActionListener(e -> {
                LayoutTemplateManager.applyTemplate(opt[0], layoutModel);
                refreshElementList(); previewPanel.repaint();
                statusLabel.setText("Auto-composicion: " + opt[1]);
            });
            menu.add(mi);
        }
        menu.addSeparator();
        JMenuItem more = new JMenuItem("Mas plantillas...");
        more.addActionListener(e -> showTemplatePicker());
        menu.add(more);
        menu.show(previewPanel, 200, 100);
    }

    private void installDropTarget() {
        CanvasDropTarget.DropHandler handler = new CanvasDropTarget.DropHandler() {
            public void onImageDropped(java.awt.image.BufferedImage img, double mmX, double mmY) {
                double wMm = img.getWidth() / 200.0 * 25.4;
                double hMm = img.getHeight() / 200.0 * 25.4;
                LayoutImage li = new LayoutImage("img-" + System.currentTimeMillis(), img, mmX, mmY, wMm, hMm);
                li.setZOrder(layoutModel.nextZ());
                li.setName("Imagen " + countOfType("Imagen"));
                layoutModel.addElement(li);
                refreshElementList();
                previewPanel.repaint();
            }
            public void onFileDropped(File file, double mmX, double mmY) {
                String lname = file.getName().toLowerCase();
                if (lname.endsWith(".shp") || lname.endsWith(".geojson") || lname.endsWith(".gpkg")) {
                    boolean yes = NotificationManager.confirm(MapLayoutComposerDialog.this,
                        "Archivo vectorial",
                        "Agregar " + file.getName() + " como capa al proyecto?");
                    if (yes) {
                        NotificationManager.warn(MapLayoutComposerDialog.this, null,
                            "Importacion de capas via drag & drop pendiente.\nUse Proyecto > Agregar capa.");
                    }
                } else {
                    NotificationManager.info(MapLayoutComposerDialog.this,
                        "Formato",
                        "Archivo no soportado: " + file.getName());
                }
            }
        };
        CanvasDropTarget.install(previewPanel, handler, PREVIEW_RENDER_DPI, 1.0,
            () -> previewPanel.lastPageBounds != null ? previewPanel.lastPageBounds.x : 0,
            () -> previewPanel.lastPageBounds != null ? previewPanel.lastPageBounds.y : 0);
    }

    void populateLegendFromProject(LayoutLegend legend) {
        layoutController.populateLegendFromProject(legend);
    }

    private boolean addGraduatedLegendItems(LayoutLegend legend, Layer layer, String gtype) {
        return layoutController.addGraduatedLegendItems(legend, layer, gtype);
    }

    private boolean addCategorizedLegendItems(LayoutLegend legend, Layer layer, String gtype) {
        return layoutController.addCategorizedLegendItems(legend, layer, gtype);
    }

    private Color resolveLayerColor(Layer layer) {
        return layoutController.resolveLayerColor(layer);
    }

    private String resolveGeometryType(Layer layer) {
        return layoutController.resolveGeometryType(layer);
    }

    public static void open() {
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        showCatmapSplashAndOpen(owner, null);
    }

    public static void openWithLayoutImage(File imageFile) {
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        showCatmapSplashAndOpen(owner, imageFile);
    }



    private void installInitialOpenBehavior() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                scheduleInitialControlsReset();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (openInstance == MapLayoutComposerDialog.this) {
                    openInstance = null;
                }
            }
        });
    }

    private void scheduleInitialControlsReset() {
        final int[] remainingPasses = {4};
        Timer timer = new Timer(120, e -> {
            applyInitialDocumentDefaults();
            resetControlsViewportToTop();
            templateCombo.requestFocusInWindow();
            remainingPasses[0]--;
            if (remainingPasses[0] <= 0) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    private void applyInitialDocumentDefaults() {
        if (templateCombo.getSelectedItem() == null) {
            templateCombo.setSelectedItem(LayoutTemplate.CLEAN_CENTERED);
        }
        if (pageSizeCombo.getSelectedItem() == null) {
            pageSizeCombo.setSelectedItem(PageSizePreset.A4);
        }
        if (orientationCombo.getSelectedItem() != PageOrientation.LANDSCAPE) {
            orientationCombo.setSelectedItem(PageOrientation.LANDSCAPE);
        }
        activateSelectionTool();
        previewPanel.revalidate();
        refreshLayoutStructureTree();
        syncLayoutStructureSelection();
        previewPanel.repaint();
    }

    private void resetControlsViewportToTop() {
        controlsScrollPane.getVerticalScrollBar().setValue(0);
        controlsScrollPane.getViewport().setViewPosition(new Point(0, 0));
    }

    private JPanel buildPreviewContainer() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        JPanel northPanel = new JPanel(new BorderLayout(0, 8));
        northPanel.setOpaque(false);
        northPanel.add(new ar.com.catgis.ui.components.layout.CatmapWorkspaceHeader(), BorderLayout.NORTH);
        JPanel toolbarSection = new JPanel(new BorderLayout(0, 4));
        toolbarSection.setOpaque(false);
        previewToolbar = new ar.com.catgis.ui.components.layout.LayoutPreviewToolbar(this, layoutModel, interactionState);
        selectionToolButton = previewToolbar.getSelectionToolButton();
        mapPanToolButton = previewToolbar.getMapPanToolButton();
        mapZoomToolButton = previewToolbar.getMapZoomToolButton();
        toolbarSection.add(previewToolbar, BorderLayout.NORTH);
            JLabel hint = new JLabel("Seleccionar: click para seleccionar, arrastrar para mover | Pan mapa: desplaza el contenido | Zoom mapa: rueda = zoom interno | Rueda sola = zoom de pagina");
        hint.setForeground(new Color(77, 87, 101));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
        toolbarSection.add(hint, BorderLayout.SOUTH);
        northPanel.add(toolbarSection, BorderLayout.SOUTH);
        panel.add(northPanel, BorderLayout.NORTH);
        JPanel workspace = new JPanel(new BorderLayout(12, 0));
        workspace.setOpaque(false);
        previewScrollPane = buildPreviewScrollPane();
        workspace.add(previewScrollPane, BorderLayout.CENTER);
        workspace.add(new ar.com.catgis.ui.components.layout.LayoutProjectLayersSidebar(
                projectLayersSummaryLabel, projectLayersList, projectLayerDetailLabel,
                this::toggleSelectedProjectLayerVisibility,
                this::openSelectedProjectLayerAppearance,
                this::moveSelectedProjectLayer,
                this::refreshSnapshot), BorderLayout.EAST);
        panel.add(workspace, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildPreviewScrollPane() {
        JScrollPane scrollPane = new JScrollPane(previewPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(235, 239, 245));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(28);
        scrollPane.getVerticalScrollBar().setUnitIncrement(28);
        scrollPane.getHorizontalScrollBar().setBlockIncrement(120);
        scrollPane.getVerticalScrollBar().setBlockIncrement(120);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    void refreshPreviewWorkspace() {
        previewPanel.revalidate();
        previewPanel.repaint();
    }






    private void updateActiveWorkToolButtons() {
        if (previewToolbar != null) {
            previewToolbar.updateActiveWorkToolButtons(interactionState);
        }
    }






    private static BufferedImage loadBundledCatmapHero() {
        try (InputStream in = MapLayoutComposerDialog.class.getResourceAsStream("/icons/catmap-start.png")) {
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); }
        try {
            File file = new File("src/icons/catmap-start.png");
            if (file.isFile()) {
                return ImageIO.read(file);
            }
        } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); }
        return null;
    }

    private static void showCatmapSplashAndOpen(Window owner, File imageFile) {
        if (openInstance != null && openInstance.isDisplayable()) {
            openInstance.setState(JFrame.NORMAL);
            openInstance.toFront();
            openInstance.requestFocus();
            if (imageFile != null) {
                openInstance.layoutImagePathField.setText(imageFile.getAbsolutePath());
                openInstance.pushProjectMetadataFromControls();
                openInstance.interactionState.select(LayoutElementType.PROFILE_IMAGE);
                openInstance.refreshLayoutStructureTree();
                openInstance.syncLayoutStructureSelection();
                openInstance.previewPanel.repaint();
                openInstance.statusLabel.setText(I18n.t("Imagen de perfil cargada en el layout. Puedes moverla o redimensionarla."));
            }
            return;
        }
        BufferedImage hero = loadBundledCatmapHero();
        if (hero != null) {
            JDialog splash = new JDialog(owner, Dialog.ModalityType.DOCUMENT_MODAL);
            splash.setUndecorated(true);
            splash.getContentPane().setLayout(new BorderLayout(0, 0));
            splash.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(18, 33, 49), 1, true));

            int maxWidth = 860;
            int maxHeight = 480;
            double scale = Math.min(maxWidth / (double) Math.max(1, hero.getWidth()), maxHeight / (double) Math.max(1, hero.getHeight()));
            scale = Math.min(1d, scale);
            int drawW = Math.max(1, (int) Math.round(hero.getWidth() * scale));
            int drawH = Math.max(1, (int) Math.round(hero.getHeight() * scale));

            JLabel imageLabel = new JLabel(new ImageIcon(hero.getScaledInstance(drawW, drawH, java.awt.Image.SCALE_SMOOTH)));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            splash.add(imageLabel, BorderLayout.CENTER);

            JPanel footer = new JPanel(new BorderLayout(10, 6));
            footer.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
            footer.setBackground(new Color(12, 26, 41));
            JLabel title = new JLabel("CATMAP | Iniciando compositor cartografico");
            title.setForeground(Color.WHITE);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            JLabel subtitle = new JLabel("Preparando layout, mapa vivo, leyenda y herramientas del subprograma...");
            subtitle.setForeground(new Color(167, 223, 238));
            subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
            JPanel textPanel = new JPanel(new BorderLayout(0, 4));
            textPanel.setOpaque(false);
            textPanel.add(title, BorderLayout.NORTH);
            textPanel.add(subtitle, BorderLayout.CENTER);

            JProgressBar progress = new JProgressBar();
            progress.setIndeterminate(true);
            progress.setBorderPainted(false);
            progress.setForeground(new Color(32, 199, 216));
            progress.setBackground(new Color(26, 42, 58));

            footer.add(textPanel, BorderLayout.CENTER);
            footer.add(progress, BorderLayout.SOUTH);
            splash.add(footer, BorderLayout.SOUTH);

            splash.pack();
            splash.setLocationRelativeTo(owner);

            Timer timer = new Timer(CATMAP_SPLASH_MILLIS, e -> splash.dispose());
            timer.setRepeats(false);
            timer.start();
            splash.setVisible(true);
        }

        MapLayoutComposerDialog dialog = new MapLayoutComposerDialog(owner);
        openInstance = dialog;
        if (imageFile != null) {
            dialog.layoutImagePathField.setText(imageFile.getAbsolutePath());
            dialog.pushProjectMetadataFromControls();
            dialog.interactionState.select(LayoutElementType.PROFILE_IMAGE);
            dialog.refreshLayoutStructureTree();
            dialog.syncLayoutStructureSelection();
            dialog.previewPanel.repaint();
            dialog.statusLabel.setText(I18n.t("Imagen de perfil cargada en el layout. Puedes moverla o redimensionarla."));
        }
        dialog.setVisible(true);
    }








    void applyTemplateDefaults(LayoutTemplate template, boolean resetLayoutState) {
        LayoutTemplate resolved = template != null ? template : LayoutTemplate.TECHNICAL_RIGHT;
        if (resetLayoutState) {
            interactionState.resetForTemplate(resolved);
        }
        if (legendPlacementCombo.getSelectedItem() != resolved.defaultLegendPlacement()) {
            legendPlacementCombo.setSelectedItem(resolved.defaultLegendPlacement());
        }
        if (resolved == LayoutTemplate.BOTTOM_REFERENCE) {
            if (northStyleCombo.getSelectedItem() != NorthStyle.CLASSIC) {
                northStyleCombo.setSelectedItem(NorthStyle.CLASSIC);
            }
        } else if (resolved == LayoutTemplate.CLEAN_CENTERED) {
            if (northStyleCombo.getSelectedItem() != NorthStyle.MODERN) {
                northStyleCombo.setSelectedItem(NorthStyle.MODERN);
            }
        }
    }


    private void chooseLogoFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("layout-logo", "Seleccionar logo de empresa");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Imagenes (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file != null && file.isFile()) {
            FileChooserSupport.rememberSelection("layout-logo", chooser);
            logoPathField.setText(file.getAbsolutePath());
            pushProjectMetadataFromControls();
            removeExistingLogoItem();
            CatmapLayoutItem logoItem = new CatmapLayoutItem(CatmapLayoutItem.Kind.IMAGE);
            logoItem.setLabel("Logo de empresa");
            logoItem.setImagePath(file.getAbsolutePath());
            logoItem.setX(Math.max(40, previewPanel.getWidth() / 2 - 60));
            logoItem.setY(40);
            logoItem.setWidth(120);
            logoItem.setHeight(80);
            layoutItemsModel.addElement(logoItem);
            persistCatmapItems();
            refreshLayoutStructureTree();
            previewPanel.repaint();
        }
    }

    private void removeExistingLogoItem() {
        for (int i = layoutItemsModel.size() - 1; i >= 0; i--) {
            CatmapLayoutItem item = layoutItemsModel.get(i);
            if (item != null && "Logo de empresa".equals(item.getLabel())) {
                layoutItemsModel.remove(i);
            }
        }
    }

    private void chooseLayoutImageFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("layout-image", I18n.t("Seleccionar imagen para el layout"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Imagenes (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file != null) {
            FileChooserSupport.rememberSelection("layout-image", chooser);
            layoutImagePathField.setText(file.getAbsolutePath());
            pushProjectMetadataFromControls();
            interactionState.select(LayoutElementType.PROFILE_IMAGE);
            refreshLayoutStructureTree();
            syncLayoutStructureSelection();
            statusLabel.setText(I18n.t("Imagen de perfil cargada en el layout. Puedes moverla o redimensionarla."));
            previewPanel.repaint();
        }
    }

    private void loadCatmapItemsFromProject() {
        layoutItemsModel.clear();
        if (ctxProject() == null) {
            return;
        }
        for (CatmapLayoutItem item : ctxProject().getCatmapItems()) {
            if (item != null) {
                layoutItemsModel.addElement(new CatmapLayoutItem(item));
            }
        }
        refreshLayoutStructureTree();
    }

    private void loadProjectLayersFromProject() {
        Layer selected = projectLayersList != null ? projectLayersList.getSelectedValue() : null;
        projectLayersModel.clear();
        if (ctxProject() != null && ctxProject().getLayers() != null) {
            for (Layer layer : ctxProject().getLayers()) {
                if (layer != null) {
                    projectLayersModel.addElement(layer);
                }
            }
        }
        if (selected != null) {
            projectLayersList.setSelectedValue(selected, true);
        }
        updateProjectLayersSummary();
        refreshProjectLayerDetails();
    }

    private void updateProjectLayersSummary() {
        int total = projectLayersModel.getSize();
        int visibleCount = 0;
        for (int i = 0; i < projectLayersModel.size(); i++) {
            Layer layer = projectLayersModel.get(i);
            if (layer != null && isProjectLayerEffectivelyVisible(layer)) {
                visibleCount++;
            }
        }
        projectLayersSummaryLabel.setText("Capas en CATMAP: " + visibleCount + " visibles de " + total);
    }

    void refreshProjectLayerDetails() {
        Layer layer = projectLayersList.getSelectedValue();
        if (layer == null) {
            projectLayerDetailLabel.setText("<html>Selecciona una capa para controlar visibilidad y simbologia sin salir de CATMAP.</html>");
            return;
        }
        projectLayerDetailLabel.setText("<html><b>" + escape(layer.getName()) + "</b><br>"
                + escape(projectLayerTypeLabel(layer)) + " | "
                + (isProjectLayerEffectivelyVisible(layer) ? "Visible" : "Oculta")
                + "<br>Click en el checkbox de la fila para mostrar/ocultar. Doble clic para editar.</html>");
    }

    private String projectLayerTypeLabel(Layer layer) {
        return LayoutPageRenderer.layerTypeLabel(layer);
    }

    private javax.swing.Icon iconForProjectLayer(Layer layer) {
        if (layer == null) {
            return AppIcons.genericLayerIcon();
        }
        if (layer instanceof OnlineTileLayer) {
            return AppIcons.imageryIcon();
        }
        if (layer instanceof OnlineWmsLayer) {
            return AppIcons.wmsIcon();
        }
        if (layer instanceof RasterLayer) {
            return AppIcons.imageryIcon();
        }
        ShapefileData data = ctxMapPanel() != null ? ctxMapPanel().getShapefileData(layer) : null;
        String geometryFamily = VectorLayerUtils.resolveGeometryFamily(data);
        if ("POINT".equalsIgnoreCase(geometryFamily) || "MULTIPOINT".equalsIgnoreCase(geometryFamily)) {
            return AppIcons.pointIcon();
        }
        if ("LINESTRING".equalsIgnoreCase(geometryFamily) || "MULTILINESTRING".equalsIgnoreCase(geometryFamily)) {
            return AppIcons.lineIcon();
        }
        if ("POLYGON".equalsIgnoreCase(geometryFamily) || "MULTIPOLYGON".equalsIgnoreCase(geometryFamily)) {
            return AppIcons.polygonIcon();
        }
        return AppIcons.genericLayerIcon();
    }

    void toggleProjectLayerVisibility(Layer layer) {
        if (layer == null) {
            return;
        }
        layer.setVisible(!layer.isVisible());
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.refreshLayerList();
        }
        if (ctxMapPanel() != null) {
            ctxMapPanel().refreshLayerVisibility();
            ctxMapPanel().repaint();
        }
        CatgisDesktopApp.markProjectDirty();
        loadProjectLayersFromProject();
        projectLayersList.setSelectedValue(layer, true);
        refreshSnapshot();
        statusLabel.setText(layer.isVisible()
                ? "Capa visible en CATMAP: " + layer.getName()
                : "Capa oculta en CATMAP: " + layer.getName());
    }

    void toggleSelectedProjectLayerVisibility() {
        Layer layer = projectLayersList.getSelectedValue();
        if (layer == null) {
            NotificationManager.warn(this, null, "Selecciona una capa del panel derecho para cambiar su visibilidad.");
            return;
        }
        toggleProjectLayerVisibility(layer);
    }

    void openSelectedProjectLayerAppearance() {
        Layer layer = projectLayersList.getSelectedValue();
        if (layer == null) {
            NotificationManager.warn(this, null, "Selecciona una capa del panel derecho para editar su simbologia.");
            return;
        }
        openProjectLayerAppearance(layer);
    }

    void openProjectLayerAppearance(Layer layer) {
        if (layer == null) {
            return;
        }
        if (layer instanceof RasterLayer) {
            RasterDisplaySettingsDialog.open(this, layer);
        } else if (layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
            NotificationManager.info(
                    this,
                    "CATMAP - Capas online",
                    "En esta ronda las capas online se controlan desde CATMAP por visibilidad y orden.\nLa simbologia detallada sigue sin aplicar como en vector o raster local."
            );
            return;
        } else {
            LayerPropertiesDialog.open(this, layer);
        }
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.refreshLayerList();
        }
        if (ctxMapPanel() != null) {
            ctxMapPanel().refreshLayerVisibility();
            ctxMapPanel().repaint();
        }
        loadProjectLayersFromProject();
        projectLayersList.setSelectedValue(layer, true);
        refreshSnapshot();
        statusLabel.setText("Simbologia actualizada desde CATMAP: " + layer.getName());
    }

    private void moveSelectedProjectLayer(int delta) {
        Layer layer = projectLayersList.getSelectedValue();
        if (layer == null || ctxProject() == null) {
            NotificationManager.warn(this, null, "Selecciona una capa del panel derecho para reordenarla.");
            return;
        }
        List<Layer> orderedLayers = ctxProject().getLayers();
        int index = orderedLayers.indexOf(layer);
        int target = index + delta;
        if (index < 0 || target < 0 || target >= orderedLayers.size()) {
            return;
        }
        Collections.swap(orderedLayers, index, target);
        if (ctxMapPanel() != null) {
            ctxMapPanel().reorderLayers(new ArrayList<>(orderedLayers));
        }
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.refreshLayerList();
        }
        CatgisDesktopApp.markProjectDirty();
        loadProjectLayersFromProject();
        projectLayersList.setSelectedValue(layer, true);
        refreshSnapshot();
        statusLabel.setText("Orden de capas actualizado en CATMAP: " + layer.getName());
    }

    private List<CatmapLayoutItem> copyCatmapItems() {
        List<CatmapLayoutItem> items = new ArrayList<>();
        for (int i = 0; i < layoutItemsModel.size(); i++) {
            CatmapLayoutItem item = layoutItemsModel.get(i);
            if (item != null) {
                items.add(new CatmapLayoutItem(item));
            }
        }
        return items;
    }

    public void persistCatmapItems() {
        layoutController.persistCatmapItems(copyCatmapItems());
    }

    private void addCatmapItem(CatmapLayoutItem.Kind kind) {
        CatmapLayoutItem base = new CatmapLayoutItem(kind);
        CatmapLayoutItem edited = CatmapItemEditorDialog.open(this, "Agregar elemento CATMAP", base);
        if (edited == null) {
            return;
        }
        layoutItemsModel.addElement(edited);
        layoutItemsList.setSelectedValue(edited, true);
        interactionState.selectCustomItem(edited.getId());
        persistCatmapItems();
        statusLabel.setText("Elemento CATMAP agregado al layout.");
        updateCatmapElementsListState();
        syncLayoutStructureSelection();
        previewPanel.repaint();
    }

    private void addCatmapImageItem() {
        addCatmapItem(CatmapLayoutItem.Kind.IMAGE);
    }

    private void promptAndAddCatmapItem() {
        Object selected = JOptionPane.showInputDialog(
                this,
                "Que elemento CATMAP queres insertar primero?",
                "CATMAP - Nuevo elemento",
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{"Texto", "Imagen", "Rectangulo", "Elipse", "Linea"},
                "Texto"
        );
        if (selected == null) {
            return;
        }
        String value = selected.toString();
        switch (value) {
            case "Imagen" -> addCatmapImageItem();
            case "Rectangulo" -> previewPanel.startDrawing("rect");
            case "Elipse" -> previewPanel.startDrawing("ellipse");
            case "Linea" -> previewPanel.startDrawing("line");
            default -> addCatmapItem(CatmapLayoutItem.Kind.TEXT);
        }
    }

    public void activateSelectionTool() {
        interactionState.select(null);
        interactionState.setMapFrameTool(MapFrameTool.MOVE_FRAME);
        selectCatmapItemInList(null);
        syncLayoutStructureSelection();
        statusLabel.setText("Seleccionar activo. Click para seleccionar, arrastrar para mover elementos.");
        updateActiveWorkToolButtons();
        previewPanel.repaint();
    }

    private void activateMapFrameTool() {
        interactionState.select(LayoutElementType.MAP_CONTENT);
        interactionState.setMapFrameTool(MapFrameTool.MOVE_FRAME);
        selectCatmapItemInList(null);
        syncLayoutStructureSelection();
        statusLabel.setText("Mover layout activo sobre el mapa. Arrastra el bloque completo sin cambiar el contenido interno.");
        updateActiveWorkToolButtons();
        previewPanel.repaint();
    }

    public void activateMapPanTool() {
        interactionState.select(LayoutElementType.MAP_CONTENT);
        interactionState.setMapFrameTool(MapFrameTool.PAN);
        selectCatmapItemInList(null);
        syncLayoutStructureSelection();
        statusLabel.setText("Pan del mapa activo. Arrastra dentro del frame para desplazar el contenido interno.");
        updateActiveWorkToolButtons();
        previewPanel.repaint();
    }

    public void activateMapFrameZoomTool() {
        interactionState.select(LayoutElementType.MAP_CONTENT);
        interactionState.setMapFrameTool(MapFrameTool.ZOOM);
        selectCatmapItemInList(null);
        syncLayoutStructureSelection();
        statusLabel.setText("Lupa de mapa activa. Usa la rueda sobre el frame para acercar o alejar el mapa interno.");
        updateActiveWorkToolButtons();
        previewPanel.repaint();
    }

    public void adjustMapZoom(double factor) {
        interactionState.zoomMap(factor);
        statusLabel.setText("Zoom del mapa dentro de CATMAP: " + Math.round(interactionState.getMapZoom() * 100d) + "%");
        previewPanel.repaint();
    }

    public void resetMapFrameView() {
        interactionState.resetMapView();
        statusLabel.setText("Mapa reencuadrado dentro del cuadro.");
        previewPanel.repaint();
    }

    public void adjustPageZoom(double factor) {
        interactionState.zoomPreview(factor);
        statusLabel.setText("Zoom de composicion actualizado.");
        refreshPreviewWorkspace();
    }

    public void fitPageView() {
        interactionState.fitPage();
        statusLabel.setText("Vista del compositor ajustada a pagina.");
        refreshPreviewWorkspace();
    }

    public void fitWidthView() {
        interactionState.fitWidth();
        statusLabel.setText("Vista del compositor ajustada al ancho.");
        refreshPreviewWorkspace();
    }

    public void resetLayoutView() {
        interactionState.resetForTemplate((LayoutTemplate) templateCombo.getSelectedItem());
        applyTemplateDefaults((LayoutTemplate) templateCombo.getSelectedItem(), false);
        syncLayoutStructureSelection();
        statusLabel.setText("Layout restablecido segun la plantilla seleccionada.");
        refreshPreviewWorkspace();
    }

    private void restoreDefaultLayoutElements() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Restaurar posiciones, tamanos, visibilidad y bloqueo de los elementos por defecto de CATMAP?",
                "CATMAP - Restaurar elementos por defecto",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        interactionState.restoreDefaultElementControls();
        northCheck.setSelected(true);
        scaleCheck.setSelected(true);
        legendCheck.setSelected(true);
        refreshLayoutStructureTree();
        syncLayoutStructureSelection();
        statusLabel.setText("Elementos por defecto de CATMAP restaurados.");
        refreshPreviewWorkspace();
    }

    public void configureNorthFromToolbar() {
        JCheckBox visibleCheck = new JCheckBox("Mostrar norte en el layout", northCheck.isSelected());
        JComboBox<NorthStyle> styleCombo = new JComboBox<>(NorthStyle.values());
        styleCombo.setSelectedItem(northStyleCombo.getSelectedItem());
        styleCombo.setRenderer((JList<? extends NorthStyle> list, NorthStyle value, int index, boolean isSelected, boolean cellHasFocus) -> {
            JLabel label = (JLabel) new javax.swing.DefaultListCellRenderer().getListCellRendererComponent(
                    list,
                    value != null ? value.toString() : "",
                    index,
                    isSelected,
                    cellHasFocus
            );
            if (value != null) {
                label.setIcon(LayoutPageRenderer.createNorthPreviewIcon(value, 18));
                label.setIconTextGap(8);
            }
            return label;
        });

        JLabel previewLabel = new JLabel(LayoutPageRenderer.createNorthPreviewIcon((NorthStyle) styleCombo.getSelectedItem(), 88));
        previewLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 228)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.WHITE);
        styleCombo.addActionListener(e -> previewLabel.setIcon(LayoutPageRenderer.createNorthPreviewIcon((NorthStyle) styleCombo.getSelectedItem(), 88)));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        panel.add(visibleCheck, gc);
        gc.gridy++;
        panel.add(new JLabel("Estilo del simbolo:"), gc);
        gc.gridy++;
        panel.add(styleCombo, gc);
        gc.gridy++;
        panel.add(new JLabel("Vista previa:"), gc);
        gc.gridy++;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        panel.add(previewLabel, gc);
        gc.gridy++;
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        JLabel hint = new JLabel("<html>Despues podÃ©s mover y redimensionar el norte directamente desde el layout CATMAP.</html>");
        hint.setForeground(new Color(88, 98, 112));
        panel.add(hint, gc);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "CATMAP - Norte cartografico",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        northCheck.setSelected(visibleCheck.isSelected());
        northStyleCombo.setSelectedItem(styleCombo.getSelectedItem());
        pushCatmapNorthSettingsToProject();
        if (northCheck.isSelected()) {
            interactionState.select(LayoutElementType.NORTH);
            statusLabel.setText("Norte CATMAP actualizado. PodÃ©s moverlo o redimensionarlo desde el layout.");
        } else {
            interactionState.select(null);
            statusLabel.setText("Norte CATMAP oculto en la composicion.");
        }
        refreshLayoutStructureTree();
        syncLayoutStructureSelection();
        previewPanel.repaint();
    }

    public void editSelectedCatmapItem() {
        CatmapLayoutItem selected = getPrimarySelectedCatmapItem();
        if (selected == null) {
            NotificationManager.warn(this, null, "Selecciona un elemento CATMAP para editar.");
            return;
        }
        if (layoutItemsList.getSelectedIndices().length > 1) {
            statusLabel.setText("Editor completo aplicado sobre el elemento CATMAP principal de la seleccion multiple.");
        }
        CatmapLayoutItem edited = CatmapItemEditorDialog.open(this, "Editar elemento CATMAP", selected);
        if (edited == null) {
            return;
        }
        int index = layoutItemsList.getSelectedIndices().length > 0
                ? layoutItemsList.getSelectedIndices()[0]
                : layoutItemsList.getSelectedIndex();
        if (index >= 0) {
            layoutItemsModel.set(index, edited);
            layoutItemsList.setSelectedIndex(index);
            interactionState.selectCustomItem(edited.getId());
            persistCatmapItems();
            statusLabel.setText("Elemento CATMAP actualizado.");
            refreshInspectorFromSelection();
            updateCatmapElementsListState();
            syncLayoutStructureSelection();
            previewPanel.repaint();
        }
    }

    void duplicateSelectedCatmapItem() {
        List<CatmapLayoutItem> selectedItems = getSelectedCatmapItems();
        if (selectedItems.isEmpty()) {
            NotificationManager.warn(this, null, "Selecciona un elemento CATMAP para duplicar.");
            return;
        }
        int[] selectedIndices = layoutItemsList.getSelectedIndices();
        int insertIndex = selectedIndices.length > 0 ? selectedIndices[selectedIndices.length - 1] + 1 : layoutItemsModel.size();
        List<Integer> newIndices = new ArrayList<>();
        CatmapLayoutItem firstCopy = null;
        for (CatmapLayoutItem selected : selectedItems) {
            if (selected == null) {
                continue;
            }
            CatmapLayoutItem copy = new CatmapLayoutItem(selected.getKind());
            copy.setLabel(selected.getLabel() + " copia");
            copy.setText(selected.getText());
            copy.setImagePath(selected.getImagePath());
            copy.setX(selected.getX() + 24);
            copy.setY(selected.getY() + 24);
            copy.setWidth(selected.getWidth());
            copy.setHeight(selected.getHeight());
            copy.setStrokeColor(selected.getStrokeColor());
            copy.setFillColor(selected.getFillColor());
            copy.setTextColor(selected.getTextColor());
            copy.setLineWidth(selected.getLineWidth());
            copy.setFontSize(selected.getFontSize());
            copy.setBold(selected.isBold());
            copy.setItalic(selected.isItalic());
            copy.setAlign(selected.getAlign());
            copy.setVisible(selected.isVisible());
            copy.setLocked(selected.isLocked());
            layoutItemsModel.add(insertIndex, copy);
            newIndices.add(insertIndex);
            if (firstCopy == null) {
                firstCopy = copy;
            }
            insertIndex++;
        }
        if (!newIndices.isEmpty()) {
            layoutItemsList.setSelectedIndices(newIndices.stream().mapToInt(Integer::intValue).toArray());
        }
        if (firstCopy != null) {
            interactionState.selectCustomItem(firstCopy.getId());
        }
        persistCatmapItems();
        statusLabel.setText(selectedItems.size() > 1 ? "Seleccion CATMAP duplicada." : "Elemento CATMAP duplicado.");
        refreshInspectorFromSelection();
        updateCatmapElementsListState();
        syncLayoutStructureSelection();
        previewPanel.repaint();
    }

    private void moveSelectedCatmapItem(int delta) {
        int index = layoutItemsList.getSelectedIndex();
        if (index < 0) {
            NotificationManager.warn(this, null, "Selecciona un elemento CATMAP para reordenar.");
            return;
        }
        int target = index + delta;
        if (target < 0 || target >= layoutItemsModel.size()) {
            return;
        }
        CatmapLayoutItem item = layoutItemsModel.remove(index);
        layoutItemsModel.add(target, item);
        layoutItemsList.setSelectedIndex(target);
        interactionState.selectCustomItem(item.getId());
        persistCatmapItems();
        statusLabel.setText("Orden CATMAP actualizado.");
        refreshInspectorFromSelection();
        updateCatmapElementsListState();
        syncLayoutStructureSelection();
        previewPanel.repaint();
    }

    private void removeSelectedCatmapItem() {
        int[] selectedIndices = layoutItemsList.getSelectedIndices();
        if (selectedIndices.length == 0) {
            NotificationManager.warn(this, null, "Selecciona un elemento CATMAP para quitar.");
            return;
        }
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            layoutItemsModel.remove(selectedIndices[i]);
        }
        layoutItemsList.clearSelection();
        interactionState.select(null);
        persistCatmapItems();
        statusLabel.setText(selectedIndices.length > 1 ? "Elementos CATMAP quitados del layout." : "Elemento CATMAP quitado del layout.");
        refreshInspectorFromSelection();
        updateCatmapElementsListState();
        syncLayoutStructureSelection();
        previewPanel.repaint();
    }

    void deleteSelectedLayoutObject() {
        if (interactionState.getSelectedElement() == LayoutElementType.CATMAP_ITEM || !getSelectedCatmapItems().isEmpty()) {
            removeSelectedCatmapItem();
            return;
        }
        LayoutElementType selected = interactionState.getSelectedElement();
        if (LayoutElementType.isFixed(selected)) {
            interactionState.setElementVisible(selected, false);
            if (selected == LayoutElementType.NORTH) {
                northCheck.setSelected(false);
                pushCatmapNorthSettingsToProject();
            } else if (selected == LayoutElementType.SCALE) {
                scaleCheck.setSelected(false);
            } else if (selected == LayoutElementType.LEGEND) {
                legendCheck.setSelected(false);
            }
            refreshLayoutStructureTree();
            syncLayoutStructureSelection();
            previewPanel.repaint();
            statusLabel.setText(layoutElementLabel(selected) + " eliminado visualmente del layout. PodÃ©s restaurarlo desde 'Restaurar por defecto'.");
        }
    }

    void copySelectedCatmapItemsToClipboard() {
        List<CatmapLayoutItem> selectedItems = getSelectedCatmapItems();
        if (selectedItems.isEmpty()) {
            statusLabel.setText("Selecciona un elemento CATMAP para copiar.");
            return;
        }
        catmapClipboard.clear();
        for (CatmapLayoutItem item : selectedItems) {
            if (item != null) {
                catmapClipboard.add(new CatmapLayoutItem(item));
            }
        }
        statusLabel.setText(catmapClipboard.size() > 1 ? "Elementos CATMAP copiados." : "Elemento CATMAP copiado.");
    }

    void cutSelectedCatmapItemsToClipboard() {
        copySelectedCatmapItemsToClipboard();
        if (!catmapClipboard.isEmpty()) {
            removeSelectedCatmapItem();
            statusLabel.setText("Elemento(s) CATMAP cortados al portapapeles interno.");
        }
    }

    void pasteCatmapItemsFromClipboard() {
        if (catmapClipboard.isEmpty()) {
            statusLabel.setText("No hay elementos CATMAP copiados para pegar.");
            return;
        }
        int insertIndex = Math.max(0, layoutItemsList.getSelectedIndex() + 1);
        if (insertIndex <= 0) {
            insertIndex = layoutItemsModel.size();
        }
        List<Integer> newIndices = new ArrayList<>();
        CatmapLayoutItem firstCopy = null;
        int offset = 24;
        for (CatmapLayoutItem source : catmapClipboard) {
            CatmapLayoutItem copy = cloneCatmapItem(source, " copia", offset);
            layoutItemsModel.add(insertIndex, copy);
            newIndices.add(insertIndex);
            if (firstCopy == null) {
                firstCopy = copy;
            }
            insertIndex++;
            offset += 10;
        }
        if (!newIndices.isEmpty()) {
            layoutItemsList.setSelectedIndices(newIndices.stream().mapToInt(Integer::intValue).toArray());
        }
        if (firstCopy != null) {
            interactionState.selectCustomItem(firstCopy.getId());
        }
        persistCatmapItems();
        refreshInspectorFromSelection();
        updateCatmapElementsListState();
        syncLayoutStructureSelection();
        previewPanel.repaint();
        statusLabel.setText(catmapClipboard.size() > 1 ? "Elementos CATMAP pegados." : "Elemento CATMAP pegado.");
    }

    private CatmapLayoutItem cloneCatmapItem(CatmapLayoutItem source, String labelSuffix, int offset) {
        CatmapLayoutItem copy = new CatmapLayoutItem(source != null ? source.getKind() : CatmapLayoutItem.Kind.TEXT);
        if (source == null) {
            return copy;
        }
        copy.setLabel(source.getLabel() + (labelSuffix != null ? labelSuffix : ""));
        copy.setText(source.getText());
        copy.setImagePath(source.getImagePath());
        copy.setX(source.getX() + offset);
        copy.setY(source.getY() + offset);
        copy.setWidth(source.getWidth());
        copy.setHeight(source.getHeight());
        copy.setStrokeColor(source.getStrokeColor());
        copy.setFillColor(source.getFillColor());
        copy.setTextColor(source.getTextColor());
        copy.setLineWidth(source.getLineWidth());
        copy.setFontSize(source.getFontSize());
        copy.setBold(source.isBold());
        copy.setItalic(source.isItalic());
        copy.setAlign(source.getAlign());
        copy.setVisible(source.isVisible());
        copy.setLocked(source.isLocked());
        return copy;
    }

    private void applyInspectorToSelectedCatmapItem() {
        CatmapLayoutItem item = getPrimarySelectedCatmapItem();
        if (item == null) {
            NotificationManager.warn(this, null, "Selecciona un elemento CATMAP para editar desde el inspector.");
            return;
        }
        try {
            item.setLabel(inspectorLabelField.getText());
            item.setX(Integer.parseInt(inspectorXField.getText().trim()));
            item.setY(Integer.parseInt(inspectorYField.getText().trim()));
            item.setWidth(Integer.parseInt(inspectorWidthField.getText().trim()));
            item.setHeight(Integer.parseInt(inspectorHeightField.getText().trim()));
            item.setFontSize((Integer) inspectorFontSizeSpinner.getValue());
            item.setLineWidth(((Double) inspectorLineWidthSpinner.getValue()).floatValue());
            item.setBold(inspectorBoldCheck.isSelected());
            item.setItalic(inspectorItalicCheck.isSelected());
            item.setAlign((CatmapLayoutItem.HorizontalAlign) inspectorAlignCombo.getSelectedItem());
            item.setVisible(inspectorVisibleCheck.isSelected());
            item.setLocked(inspectorLockedCheck.isSelected());
            if (item.getKind() == CatmapLayoutItem.Kind.TEXT) {
                item.setText(inspectorTextArea.getText());
            }
            layoutItemsList.repaint();
            persistCatmapItems();
            interactionState.selectCustomItem(item.getId());
            statusLabel.setText("Inspector CATMAP aplicado al elemento seleccionado.");
            updateCatmapElementsListState();
            syncLayoutStructureSelection();
            previewPanel.repaint();
        } catch (NumberFormatException ex) {
            NotificationManager.warn(this, null, "X, Y, W y H deben ser numeros enteros validos.");
        }
    }

    private void toggleSelectedCatmapItemVisibility() {
        List<CatmapLayoutItem> items = getSelectedCatmapItems();
        if (items.isEmpty()) {
            NotificationManager.warn(this, null, "Selecciona un elemento CATMAP para cambiar su visibilidad.");
            return;
        }
        boolean makeVisible = items.stream().anyMatch(item -> !item.isVisible());
        for (CatmapLayoutItem item : items) {
            item.setVisible(makeVisible);
        }
        persistCatmapItems();
        refreshInspectorFromSelection();
        layoutItemsList.repaint();
        refreshLayoutStructureTree();
        syncLayoutStructureSelection();
        previewPanel.repaint();
        statusLabel.setText(items.size() > 1
                ? (makeVisible ? "Seleccion CATMAP visible nuevamente." : "Seleccion CATMAP oculta en el layout.")
                : (makeVisible ? "Elemento CATMAP visible nuevamente." : "Elemento CATMAP oculto en el layout."));
    }

    private void toggleSelectedCatmapItemLock() {
        List<CatmapLayoutItem> items = getSelectedCatmapItems();
        if (items.isEmpty()) {
            NotificationManager.warn(this, null, "Selecciona un elemento CATMAP para bloquearlo o liberarlo.");
            return;
        }
        boolean lock = items.stream().anyMatch(item -> !item.isLocked());
        for (CatmapLayoutItem item : items) {
            item.setLocked(lock);
        }
        persistCatmapItems();
        refreshInspectorFromSelection();
        layoutItemsList.repaint();
        refreshLayoutStructureTree();
        syncLayoutStructureSelection();
        previewPanel.repaint();
        statusLabel.setText(items.size() > 1
                ? (lock ? "Seleccion CATMAP bloqueada." : "Seleccion CATMAP liberada.")
                : (lock ? "Elemento CATMAP bloqueado para mover/redimensionar." : "Elemento CATMAP liberado."));
    }

    CatmapLayoutItem getCatmapItemById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (int i = 0; i < layoutItemsModel.size(); i++) {
            CatmapLayoutItem item = layoutItemsModel.get(i);
            if (item != null && id.equals(item.getId())) {
                return item;
            }
        }
        return null;
    }

    private List<CatmapLayoutItem> getSelectedCatmapItems() {
        List<CatmapLayoutItem> items = new ArrayList<>(layoutItemsList.getSelectedValuesList());
        if (!items.isEmpty()) {
            return items;
        }
        LayoutStructureNode data = selectedLayoutStructureNode();
        if (data != null && data.kind() == LayoutStructureNodeKind.CUSTOM_ITEM) {
            CatmapLayoutItem item = getCatmapItemById(data.customItemId());
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private CatmapLayoutItem getPrimarySelectedCatmapItem() {
        List<CatmapLayoutItem> items = getSelectedCatmapItems();
        return items.isEmpty() ? null : items.get(0);
    }

    private List<CatmapLayoutItem> getUnlockedSelectedCatmapItems() {
        List<CatmapLayoutItem> unlocked = new ArrayList<>();
        for (CatmapLayoutItem item : getSelectedCatmapItems()) {
            if (item != null && !item.isLocked()) {
                unlocked.add(item);
            }
        }
        return unlocked;
    }

    void selectCatmapItemInList(String id) {
        if (id == null || id.isBlank()) {
            layoutItemsList.clearSelection();
            refreshInspectorFromSelection();
            return;
        }
        for (int i = 0; i < layoutItemsModel.size(); i++) {
            CatmapLayoutItem item = layoutItemsModel.get(i);
            if (item != null && id.equals(item.getId())) {
                layoutItemsList.setSelectedIndex(i);
                layoutItemsList.ensureIndexIsVisible(i);
                refreshInspectorFromSelection();
                return;
            }
        }
        layoutItemsList.clearSelection();
        refreshInspectorFromSelection();
    }

    void refreshInspectorFromSelection() {
        CatmapLayoutItem item = layoutItemsList.getSelectedValue();
        boolean enabled = item != null;
        setInspectorEnabled(enabled);
        if (item == null) {
            inspectorTypeValueLabel.setText("Sin seleccion");
            inspectorLabelField.setText("");
            inspectorXField.setText("");
            inspectorYField.setText("");
            inspectorWidthField.setText("");
            inspectorHeightField.setText("");
            inspectorTextArea.setText("");
            inspectorImagePathField.setText("");
            syncLayoutStructureSelection();
            return;
        }
        inspectorTypeValueLabel.setText(kindLabel(item.getKind()));
        inspectorLabelField.setText(item.getLabel());
        inspectorXField.setText(String.valueOf(item.getX()));
        inspectorYField.setText(String.valueOf(item.getY()));
        inspectorWidthField.setText(String.valueOf(item.getWidth()));
        inspectorHeightField.setText(String.valueOf(item.getHeight()));
        inspectorTextArea.setText(item.getText());
        inspectorImagePathField.setText(item.getImagePath());
        inspectorFontSizeSpinner.setValue(item.getFontSize());
        inspectorLineWidthSpinner.setValue((double) item.getLineWidth());
        inspectorBoldCheck.setSelected(item.isBold());
        inspectorItalicCheck.setSelected(item.isItalic());
        inspectorVisibleCheck.setSelected(item.isVisible());
        inspectorLockedCheck.setSelected(item.isLocked());
        inspectorAlignCombo.setSelectedItem(item.getAlign());
        syncLayoutStructureSelection();
    }

    private void updateCatmapElementsListState() {
        if (catmapElementsCardPanel == null) {
            return;
        }
        catmapElementsCardLayout.show(catmapElementsCardPanel, layoutItemsModel.isEmpty() ? "empty" : "list");
        refreshLayoutStructureTree();
    }

    private void setInspectorEnabled(boolean enabled) {
        inspectorLabelField.setEnabled(enabled);
        inspectorXField.setEnabled(enabled);
        inspectorYField.setEnabled(enabled);
        inspectorWidthField.setEnabled(enabled);
        inspectorHeightField.setEnabled(enabled);
        inspectorTextArea.setEnabled(enabled);
        inspectorImagePathField.setEnabled(enabled);
        inspectorFontSizeSpinner.setEnabled(enabled);
        inspectorLineWidthSpinner.setEnabled(enabled);
        inspectorBoldCheck.setEnabled(enabled);
        inspectorItalicCheck.setEnabled(enabled);
        inspectorVisibleCheck.setEnabled(enabled);
        inspectorLockedCheck.setEnabled(enabled);
        inspectorAlignCombo.setEnabled(enabled);
    }

    private String kindLabel(CatmapLayoutItem.Kind kind) {
        return switch (kind != null ? kind : CatmapLayoutItem.Kind.TEXT) {
            case TEXT -> "Texto";
            case IMAGE -> "Imagen";
            case RECTANGLE -> "Rectangulo";
            case ELLIPSE -> "Elipse";
            case LINE -> "Linea";
        };
    }

    String layoutElementLabel(LayoutElementType type) {
        if (type == null) {
            return "Elemento";
        }
        return switch (type) {
            case HEADER -> "Encabezado";
            case MAP_CONTENT -> "Mapa principal";
            case LEGEND -> "Leyenda";
            case NORTH -> "Norte";
            case SCALE -> "Escala";
            case CARTOUCHE -> "Datos cartograficos";
            case PROFILE_IMAGE -> "Perfil / imagen";
            case CATMAP_ITEM -> "Elemento CATMAP";
        };
    }

    private javax.swing.Icon iconForCatmapKind(CatmapLayoutItem.Kind kind) {
        return switch (kind != null ? kind : CatmapLayoutItem.Kind.TEXT) {
            case TEXT -> AppIcons.attrEditIcon();
            case IMAGE -> AppIcons.imageryIcon();
            case RECTANGLE -> AppIcons.rectangleIcon();
            case ELLIPSE -> AppIcons.circleIcon();
            case LINE -> AppIcons.lineIcon();
        };
    }

    private boolean isLayoutElementVisible(LayoutElementType type) {
        if (!LayoutElementType.isFixed(type) || !interactionState.isElementVisible(type)) {
            return false;
        }
        return switch (type) {
            case NORTH -> northCheck.isSelected();
            case SCALE -> scaleCheck.isSelected();
            case LEGEND -> legendCheck.isSelected();
            case PROFILE_IMAGE -> !safeTrim(layoutImagePathField.getText()).isBlank();
            default -> true;
        };
    }

    private boolean isLayoutElementLocked(LayoutElementType type) {
        return LayoutElementType.isFixed(type) && interactionState.isElementLocked(type);
    }

    void refreshLayoutStructureTree() {
        if (layoutStructureTreeModel == null) {
            return;
        }
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(LayoutStructureNode.root("CATMAP"));
        DefaultMutableTreeNode documentNode = new DefaultMutableTreeNode(LayoutStructureNode.group("Documento y mapa"));
        documentNode.add(new DefaultMutableTreeNode(LayoutStructureNode.element("Encabezado", LayoutElementType.HEADER, isLayoutElementVisible(LayoutElementType.HEADER), isLayoutElementLocked(LayoutElementType.HEADER), null)));
        documentNode.add(new DefaultMutableTreeNode(LayoutStructureNode.element("Mapa principal", LayoutElementType.MAP_CONTENT, isLayoutElementVisible(LayoutElementType.MAP_CONTENT), isLayoutElementLocked(LayoutElementType.MAP_CONTENT), null)));
        documentNode.add(new DefaultMutableTreeNode(LayoutStructureNode.element("Escala", LayoutElementType.SCALE, isLayoutElementVisible(LayoutElementType.SCALE), isLayoutElementLocked(LayoutElementType.SCALE), null)));
        documentNode.add(new DefaultMutableTreeNode(LayoutStructureNode.element("Datos cartograficos / firma / fecha", LayoutElementType.CARTOUCHE, isLayoutElementVisible(LayoutElementType.CARTOUCHE), isLayoutElementLocked(LayoutElementType.CARTOUCHE), null)));
        documentNode.add(new DefaultMutableTreeNode(LayoutStructureNode.element(
                "Perfil / imagen",
                LayoutElementType.PROFILE_IMAGE,
                isLayoutElementVisible(LayoutElementType.PROFILE_IMAGE),
                isLayoutElementLocked(LayoutElementType.PROFILE_IMAGE),
                null
        )));
        root.add(documentNode);

        DefaultMutableTreeNode referencesNode = new DefaultMutableTreeNode(LayoutStructureNode.group("Referencias"));
        referencesNode.add(new DefaultMutableTreeNode(LayoutStructureNode.element("Leyenda", LayoutElementType.LEGEND, isLayoutElementVisible(LayoutElementType.LEGEND), isLayoutElementLocked(LayoutElementType.LEGEND), null)));
        referencesNode.add(new DefaultMutableTreeNode(LayoutStructureNode.element("Norte", LayoutElementType.NORTH, isLayoutElementVisible(LayoutElementType.NORTH), isLayoutElementLocked(LayoutElementType.NORTH), null)));
        root.add(referencesNode);

        DefaultMutableTreeNode itemsNode = new DefaultMutableTreeNode(LayoutStructureNode.group("Elementos CATMAP"));
        for (int i = 0; i < layoutItemsModel.size(); i++) {
            CatmapLayoutItem item = layoutItemsModel.get(i);
            if (item == null) {
                continue;
            }
            String label = (i + 1) + ". " + kindLabel(item.getKind()) + " | " + safeTrim(item.getLabel().isBlank() ? item.getText() : item.getLabel());
            itemsNode.add(new DefaultMutableTreeNode(LayoutStructureNode.customItem(label, item)));
        }
        root.add(itemsNode);

        syncingLayoutStructureSelection = true;
        try {
            layoutStructureTreeModel.setRoot(root);
            for (int i = 0; i < layoutStructureTree.getRowCount(); i++) {
                layoutStructureTree.expandRow(i);
            }
        } finally {
            syncingLayoutStructureSelection = false;
        }
        syncLayoutStructureSelection();
    }

    public void syncLayoutStructureSelection() {
        if (layoutStructureTree == null || syncingLayoutStructureSelection) {
            return;
        }
        syncingLayoutStructureSelection = true;
        try {
            DefaultMutableTreeNode target = findLayoutStructureNode(
                    interactionState.getSelectedElement(),
                    interactionState.getSelectedCustomItemId()
            );
            if (target == null) {
                layoutStructureTree.clearSelection();
                return;
            }
            TreePath path = new TreePath(target.getPath());
            layoutStructureTree.setSelectionPath(path);
            layoutStructureTree.scrollPathToVisible(path);
        } finally {
            syncingLayoutStructureSelection = false;
        }
    }

    private DefaultMutableTreeNode findLayoutStructureNode(LayoutElementType selectedElement, String selectedCustomItemId) {
        Object rootValue = layoutStructureTreeModel.getRoot();
        if (!(rootValue instanceof DefaultMutableTreeNode root)) {
            return null;
        }
        java.util.Enumeration<?> enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            Object value = enumeration.nextElement();
            if (!(value instanceof DefaultMutableTreeNode node) || !(node.getUserObject() instanceof LayoutStructureNode data)) {
                continue;
            }
            if (selectedElement == LayoutElementType.CATMAP_ITEM
                    && data.kind() == LayoutStructureNodeKind.CUSTOM_ITEM
                    && safeTrim(selectedCustomItemId).equals(data.customItemId())) {
                return node;
            }
            if (selectedElement != null
                    && selectedElement != LayoutElementType.CATMAP_ITEM
                    && data.kind() == LayoutStructureNodeKind.ELEMENT
                    && data.elementType() == selectedElement) {
                return node;
            }
        }
        return null;
    }

    private LayoutStructureNode selectedLayoutStructureNode() {
        Object component = layoutStructureTree.getLastSelectedPathComponent();
        if (component instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof LayoutStructureNode data) {
            return data;
        }
        return null;
    }

    void handleLayoutStructureSelectionChanged() {
        if (syncingLayoutStructureSelection) {
            return;
        }
        LayoutStructureNode data = selectedLayoutStructureNode();
        if (data == null) {
            return;
        }
        syncingLayoutStructureSelection = true;
        try {
            switch (data.kind()) {
                case CUSTOM_ITEM -> {
                    selectCatmapItemInList(data.customItemId());
                    interactionState.selectCustomItem(data.customItemId());
                    statusLabel.setText("Elemento CATMAP seleccionado desde la estructura.");
                }
                case ELEMENT -> {
                    interactionState.select(data.elementType());
                    layoutItemsList.clearSelection();
                    refreshInspectorFromSelection();
                    statusLabel.setText("Elemento del layout seleccionado: " + data.label() + ".");
                }
                default -> {
                    interactionState.select(null);
                    layoutItemsList.clearSelection();
                    refreshInspectorFromSelection();
                }
            }
        } finally {
            syncingLayoutStructureSelection = false;
        }
        previewPanel.repaint();
    }

    void handleLayoutStructureDoubleClick() {
        LayoutStructureNode data = selectedLayoutStructureNode();
        if (data == null) {
            return;
        }
        switch (data.kind()) {
            case CUSTOM_ITEM -> {
                selectCatmapItemInList(data.customItemId());
                editSelectedCatmapItem();
            }
            case ELEMENT -> {
                if (data.elementType() == LayoutElementType.LEGEND) {
                    LayoutElement legend = findElementByType(LayoutLegend.class);
                    if (legend != null) {
                        layoutModel.clearSelection();
                        legend.setSelected(true);
                        previewPanel.openElementProperties(legend);
                        refreshElementList();
                        previewPanel.repaint();
                    } else {
                        openLegendEditor();
                    }
                } else if (data.elementType() == LayoutElementType.NORTH) {
                    configureNorthFromToolbar();
                } else if (data.elementType() == LayoutElementType.PROFILE_IMAGE) {
                    chooseLayoutImageFile();
                } else if (data.elementType() == LayoutElementType.MAP_CONTENT) {
                    activateMapFrameTool();
                } else if (data.elementType() == LayoutElementType.CARTOUCHE) {
                    LayoutElement cartouche = findElementByType(LayoutCartouche.class);
                    if (cartouche != null) {
                        layoutModel.clearSelection();
                        cartouche.setSelected(true);
                        previewPanel.openElementProperties(cartouche);
                        refreshElementList();
                        previewPanel.repaint();
                    }
                } else if (data.elementType() == LayoutElementType.HEADER) {
                    LayoutLabel title = findLayoutLabelByName("Titulo");
                    if (title != null) {
                        layoutModel.clearSelection();
                        title.setSelected(true);
                        previewPanel.openElementProperties(title);
                        refreshElementList();
                        previewPanel.repaint();
                    } else {
                        titleField.requestFocusInWindow();
                        titleField.selectAll();
                        statusLabel.setText("Encabezado listo para editar desde el panel izquierdo.");
                    }
                }
            }
            default -> {
            }
        }
    }

    void handleLayoutItemsListPopup(MouseEvent e) {
        if (!e.isPopupTrigger() && !SwingUtilities.isRightMouseButton(e)) {
            return;
        }
        int index = layoutItemsList.locationToIndex(e.getPoint());
        if (index >= 0) {
            Rectangle cellBounds = layoutItemsList.getCellBounds(index, index);
            if (cellBounds != null && cellBounds.contains(e.getPoint()) && !layoutItemsList.isSelectedIndex(index)) {
                layoutItemsList.setSelectedIndex(index);
            }
        }
        showCatmapContextMenu(layoutItemsList, e.getX(), e.getY());
    }

    void handleLayoutStructurePopup(MouseEvent e) {
        if (!e.isPopupTrigger() && !SwingUtilities.isRightMouseButton(e)) {
            return;
        }
        TreePath path = layoutStructureTree.getPathForLocation(e.getX(), e.getY());
        if (path != null) {
            layoutStructureTree.setSelectionPath(path);
        }
        showCatmapContextMenu(layoutStructureTree, e.getX(), e.getY());
    }

    private void showCatmapContextMenu(Component invoker, int x, int y) {
        LayoutElementType selected = interactionState.getSelectedElement();
        boolean customSelected = selected == LayoutElementType.CATMAP_ITEM || !getSelectedCatmapItems().isEmpty();
        boolean fixedSelected = LayoutElementType.isFixed(selected);

        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        addContextItem(menu, "Editar", customSelected || fixedSelected, this::editSelectedLayoutObject);
        addContextItem(menu, "Propiedades", customSelected || fixedSelected, this::editSelectedLayoutObject);
        menu.addSeparator();
        addContextItem(menu, "Copiar", customSelected, this::copySelectedCatmapItemsToClipboard);
        addContextItem(menu, "Cortar", customSelected, this::cutSelectedCatmapItemsToClipboard);
        addContextItem(menu, "Pegar", !catmapClipboard.isEmpty(), this::pasteCatmapItemsFromClipboard);
        addContextItem(menu, "Duplicar", customSelected, this::duplicateSelectedCatmapItem);
        menu.addSeparator();
        addContextItem(menu, "Visible / oculto", customSelected || fixedSelected, this::toggleSelectedLayoutObjectVisibility);
        addContextItem(menu, "Bloquear / liberar", customSelected || fixedSelected, this::toggleSelectedLayoutObjectLock);
        menu.addSeparator();
        addContextItem(menu, "Traer al frente", customSelected, this::bringSelectedCatmapItemsToFront);
        addContextItem(menu, "Enviar al fondo", customSelected, this::sendSelectedCatmapItemsToBack);
        menu.addSeparator();
        addContextItem(menu, "Eliminar", customSelected || fixedSelected, this::deleteSelectedLayoutObject);
        addContextItem(menu, "Restaurar elementos por defecto", true, this::restoreDefaultLayoutElements);
        menu.show(invoker, x, y);
    }

    private void addContextItem(javax.swing.JPopupMenu menu, String text, boolean enabled, Runnable action) {
        javax.swing.JMenuItem item = new javax.swing.JMenuItem(text);
        item.setEnabled(enabled);
        item.addActionListener(e -> action.run());
        menu.add(item);
    }

    private void installCatmapKeyboardActions() {
        javax.swing.JComponent[] commandTargets = new javax.swing.JComponent[]{
                previewPanel,
                layoutItemsList,
                layoutStructureTree
        };
        for (javax.swing.JComponent target : commandTargets) {
            bindCatmapAction(target, "control C", "catmap-copy", this::copySelectedCatmapItemsToClipboard);
            bindCatmapAction(target, "control X", "catmap-cut", this::cutSelectedCatmapItemsToClipboard);
            bindCatmapAction(target, "control V", "catmap-paste", this::pasteCatmapItemsFromClipboard);
            bindCatmapAction(target, "DELETE", "catmap-delete", this::deleteSelectedLayoutObject);
            bindCatmapAction(target, "control D", "catmap-duplicate", this::duplicateSelectedCatmapItem);
        }
        bindCatmapAction(previewPanel, "LEFT", "catmap-left", () -> nudgeSelectedLayoutObject(-2, 0));
        bindCatmapAction(previewPanel, "RIGHT", "catmap-right", () -> nudgeSelectedLayoutObject(2, 0));
        bindCatmapAction(previewPanel, "UP", "catmap-up", () -> nudgeSelectedLayoutObject(0, -2));
        bindCatmapAction(previewPanel, "DOWN", "catmap-down", () -> nudgeSelectedLayoutObject(0, 2));
        // Override Ctrl+C/V for LayoutElements
        bindCatmapAction(previewPanel, "control C", "catmap-copy-element", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { copiedElementType = sel.getClass().getSimpleName(); copiedElementJson = LayoutTemplateManager.elementToJson(sel); statusLabel.setText("Copiado: " + sel.getName()); }
        });
        bindCatmapAction(previewPanel, "control V", "catmap-paste-element", () -> {
            if (copiedElementJson != null && copiedElementType != null) {
                LayoutElement pasted = LayoutTemplateManager.jsonToElement(copiedElementType, copiedElementJson, 5, 5);
                if (pasted != null) { pasted.setZOrder(layoutModel.nextZ()); pasted.setName(pasted.getName() + " copia"); layoutModel.addElement(pasted); refreshElementList(); previewPanel.repaint(); statusLabel.setText("Pegado: " + pasted.getName()); }
            }
        });
        bindCatmapAction(previewPanel, "control D", "catmap-duplicate-element", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { duplicateLayoutElement(sel); refreshElementList(); previewPanel.repaint(); }
        });
        bindCatmapAction(previewPanel, "shift LEFT", "catmap-shift-left", () -> nudgeSelectedLayoutObject(-12, 0));
        bindCatmapAction(previewPanel, "shift RIGHT", "catmap-shift-right", () -> nudgeSelectedLayoutObject(12, 0));
        bindCatmapAction(previewPanel, "shift UP", "catmap-shift-up", () -> nudgeSelectedLayoutObject(0, -12));
        bindCatmapAction(previewPanel, "ESCAPE", "catmap-escape", () -> {
            if (mapPanToolButton.isSelected()) {
                selectionToolButton.doClick();
                statusLabel.setText("Seleccionar activo. Saliste de la edicion del mapa.");
            }
            if (previewPanel.drawingShape != null) {
                previewPanel.cancelDrawing();
            }
        });
        bindCatmapAction(previewPanel, "DELETE", "catmap-delete-element", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) {
                pushUndo(sel, true);
                layoutModel.removeElement(sel.getId());
                refreshElementList();
                previewPanel.repaint();
                statusLabel.setText("Elemento eliminado. Ctrl+Z para deshacer.");
            }
        });
    }

    private void bindCatmapAction(javax.swing.JComponent component, String keyStroke, String actionKey, Runnable action) {
        component.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(javax.swing.KeyStroke.getKeyStroke(keyStroke), actionKey);
        component.getActionMap().put(actionKey, new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                action.run();
            }
        });
    }

    private void toggleSelectedLayoutStructureVisibility() {
        LayoutStructureNode data = selectedLayoutStructureNode();
        if (data == null) {
            toggleSelectedCatmapItemVisibility();
            return;
        }
        if (data.kind() == LayoutStructureNodeKind.CUSTOM_ITEM) {
            selectCatmapItemInList(data.customItemId());
            toggleSelectedCatmapItemVisibility();
            return;
        }
        if (data.kind() != LayoutStructureNodeKind.ELEMENT) {
            statusLabel.setText("Selecciona un elemento real del layout o un elemento CATMAP para cambiar visibilidad.");
            return;
        }
        toggleLayoutElementVisibility(data.elementType());
        refreshLayoutStructureTree();
        syncLayoutStructureSelection();
        previewPanel.repaint();
    }

    private void toggleSelectedLayoutStructureLock() {
        LayoutStructureNode data = selectedLayoutStructureNode();
        if (data == null) {
            toggleSelectedCatmapItemLock();
            return;
        }
        if (data.kind() == LayoutStructureNodeKind.CUSTOM_ITEM) {
            selectCatmapItemInList(data.customItemId());
            toggleSelectedCatmapItemLock();
            return;
        }
        if (data.kind() == LayoutStructureNodeKind.ELEMENT) {
            toggleLayoutElementLock(data.elementType());
            return;
        }
        statusLabel.setText("Selecciona un elemento real del layout o un elemento CATMAP para bloquear/liberar.");
    }

    private void toggleLayoutElementVisibility(LayoutElementType type) {
        if (!LayoutElementType.isFixed(type)) {
            return;
        }
        boolean makeVisible = !isLayoutElementVisible(type);
        interactionState.setElementVisible(type, makeVisible);
        switch (type) {
            case LEGEND -> legendCheck.setSelected(makeVisible);
            case NORTH -> northCheck.setSelected(makeVisible);
            case SCALE -> scaleCheck.setSelected(makeVisible);
            default -> {
            }
        }
        if (type == LayoutElementType.NORTH) {
            pushCatmapNorthSettingsToProject();
        }
        refreshLayoutStructureTree();
        syncLayoutStructureSelection();
        previewPanel.repaint();
        statusLabel.setText(layoutElementLabel(type) + (makeVisible ? " visible en CATMAP." : " oculto en CATMAP."));
    }

    private void toggleLayoutElementLock(LayoutElementType type) {
        if (!LayoutElementType.isFixed(type)) {
            return;
        }
        boolean lock = !interactionState.isElementLocked(type);
        interactionState.setElementLocked(type, lock);
        refreshLayoutStructureTree();
        syncLayoutStructureSelection();
        previewPanel.repaint();
        statusLabel.setText(layoutElementLabel(type) + (lock ? " bloqueado para mover/redimensionar." : " liberado para edicion visual."));
    }

    private void toggleSelectedLayoutObjectVisibility() {
        if (interactionState.getSelectedElement() == LayoutElementType.CATMAP_ITEM || !getSelectedCatmapItems().isEmpty()) {
            toggleSelectedCatmapItemVisibility();
            return;
        }
        LayoutElementType selected = interactionState.getSelectedElement();
        if (LayoutElementType.isFixed(selected)) {
            toggleLayoutElementVisibility(selected);
        }
    }

    private void toggleSelectedLayoutObjectLock() {
        if (interactionState.getSelectedElement() == LayoutElementType.CATMAP_ITEM || !getSelectedCatmapItems().isEmpty()) {
            toggleSelectedCatmapItemLock();
            return;
        }
        LayoutElementType selected = interactionState.getSelectedElement();
        if (LayoutElementType.isFixed(selected)) {
            toggleLayoutElementLock(selected);
        }
    }

    private void editSelectedLayoutObject() {
        if (interactionState.getSelectedElement() == LayoutElementType.CATMAP_ITEM || !getSelectedCatmapItems().isEmpty()) {
            editSelectedCatmapItem();
            return;
        }
        LayoutElementType selected = interactionState.getSelectedElement();
        if (selected == null) {
            statusLabel.setText("Selecciona un elemento del layout para editar propiedades.");
            return;
        }
        switch (selected) {
            case LEGEND -> openLegendEditor();
            case NORTH -> configureNorthFromToolbar();
            case PROFILE_IMAGE -> chooseLayoutImageFile();
            case MAP_CONTENT -> activateMapFrameTool();
            case HEADER -> {
                titleField.requestFocusInWindow();
                titleField.selectAll();
                statusLabel.setText("Encabezado listo para editar desde el panel lateral.");
            }
            default -> statusLabel.setText(layoutElementLabel(selected) + " se administra desde el panel lateral o con arrastre directo.");
        }
    }

    private void bringSelectedCatmapItemsToFront() {
        moveSelectedCatmapItemsToEdge(true);
    }

    private void sendSelectedCatmapItemsToBack() {
        moveSelectedCatmapItemsToEdge(false);
    }

    private void moveSelectedCatmapItemsToEdge(boolean front) {
        int[] selectedIndices = layoutItemsList.getSelectedIndices();
        if (selectedIndices.length == 0) {
            statusLabel.setText("Traer/enviar al fondo aplica a elementos CATMAP agregados.");
            return;
        }
        List<CatmapLayoutItem> selected = getSelectedCatmapItems();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            layoutItemsModel.remove(selectedIndices[i]);
        }
        List<Integer> newIndices = new ArrayList<>();
        if (front) {
            int start = layoutItemsModel.size();
            for (CatmapLayoutItem item : selected) {
                layoutItemsModel.addElement(item);
                newIndices.add(start++);
            }
        } else {
            int index = 0;
            for (CatmapLayoutItem item : selected) {
                layoutItemsModel.add(index, item);
                newIndices.add(index++);
            }
        }
        layoutItemsList.setSelectedIndices(newIndices.stream().mapToInt(Integer::intValue).toArray());
        persistCatmapItems();
        refreshInspectorFromSelection();
        updateCatmapElementsListState();
        syncLayoutStructureSelection();
        previewPanel.repaint();
        statusLabel.setText(front ? "Elemento(s) CATMAP traidos al frente." : "Elemento(s) CATMAP enviados al fondo.");
    }

    void nudgeSelectedLayoutObject(int dx, int dy) {
        LayoutElementType selected = interactionState.getSelectedElement();
        if (selected == LayoutElementType.CATMAP_ITEM) {
            for (CatmapLayoutItem item : getUnlockedSelectedCatmapItems()) {
                if (item != null && item.isVisible()) {
                    item.setX(item.getX() + dx);
                    item.setY(item.getY() + dy);
                }
            }
            persistCatmapItems();
        } else if (LayoutElementType.isFixed(selected) && !interactionState.isElementLocked(selected)) {
            interactionState.translate(selected, dx, dy);
        }
        refreshInspectorFromSelection();
        syncLayoutStructureSelection();
        previewPanel.repaint();
    }

    private void alignSelectedCatmapItems(AlignmentCommand command) {
        List<CatmapLayoutItem> items = getUnlockedSelectedCatmapItems();
        if (items.isEmpty()) {
            NotificationManager.warn(this, null, "Selecciona al menos un elemento CATMAP desbloqueado para alinear.");
            return;
        }
        Rectangle referenceBounds = resolveAlignmentReferenceBounds(items);
        if (referenceBounds == null) {
            NotificationManager.warn(this, null, "CATMAP todavia no tiene una pagina renderizada para alinear elementos.");
            return;
        }
        for (CatmapLayoutItem item : items) {
            switch (command) {
                case LEFT -> item.setX(referenceBounds.x);
                case CENTER_HORIZONTAL -> item.setX(referenceBounds.x + (referenceBounds.width - item.getWidth()) / 2);
                case RIGHT -> item.setX(referenceBounds.x + referenceBounds.width - item.getWidth());
                case TOP -> item.setY(referenceBounds.y);
                case CENTER_VERTICAL -> item.setY(referenceBounds.y + (referenceBounds.height - item.getHeight()) / 2);
                case BOTTOM -> item.setY(referenceBounds.y + referenceBounds.height - item.getHeight());
            }
        }
        persistCatmapItems();
        refreshInspectorFromSelection();
        layoutItemsList.repaint();
        refreshLayoutStructureTree();
        previewPanel.repaint();
        statusLabel.setText(items.size() > 1
                ? "Seleccion CATMAP alineada: " + command.label() + "."
                : "Elemento CATMAP alineado: " + command.label() + ".");
    }

    private void distributeSelectedCatmapItems(boolean horizontal) {
        List<CatmapLayoutItem> items = getUnlockedSelectedCatmapItems();
        if (items.size() < 3) {
            NotificationManager.warn(this, null, "Selecciona al menos tres elementos CATMAP desbloqueados para distribuir.");
            return;
        }
        items.sort((a, b) -> horizontal
                ? Integer.compare(a.getX(), b.getX())
                : Integer.compare(a.getY(), b.getY()));
        if (horizontal) {
            int spanStart = items.get(0).getX();
            int spanEnd = items.get(items.size() - 1).getX() + items.get(items.size() - 1).getWidth();
            int totalWidth = 0;
            for (CatmapLayoutItem item : items) {
                totalWidth += item.getWidth();
            }
            double gap = (spanEnd - spanStart - totalWidth) / (double) Math.max(1, items.size() - 1);
            double cursor = spanStart;
            for (CatmapLayoutItem item : items) {
                item.setX((int) Math.round(cursor));
                cursor += item.getWidth() + gap;
            }
        } else {
            int spanStart = items.get(0).getY();
            int spanEnd = items.get(items.size() - 1).getY() + items.get(items.size() - 1).getHeight();
            int totalHeight = 0;
            for (CatmapLayoutItem item : items) {
                totalHeight += item.getHeight();
            }
            double gap = (spanEnd - spanStart - totalHeight) / (double) Math.max(1, items.size() - 1);
            double cursor = spanStart;
            for (CatmapLayoutItem item : items) {
                item.setY((int) Math.round(cursor));
                cursor += item.getHeight() + gap;
            }
        }
        persistCatmapItems();
        refreshInspectorFromSelection();
        layoutItemsList.repaint();
        refreshLayoutStructureTree();
        previewPanel.repaint();
        statusLabel.setText(horizontal ? "Seleccion CATMAP distribuida horizontalmente." : "Seleccion CATMAP distribuida verticalmente.");
    }

    private Rectangle resolveAlignmentReferenceBounds(List<CatmapLayoutItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        if (items.size() == 1) {
            LayoutRenderResult renderResult = previewPanel.lastRenderResult;
            if (renderResult == null || renderResult.image() == null) {
                return null;
            }
            return new Rectangle(0, 0, renderResult.image().getWidth(), renderResult.image().getHeight());
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CatmapLayoutItem item : items) {
            minX = Math.min(minX, item.getX());
            minY = Math.min(minY, item.getY());
            maxX = Math.max(maxX, item.getX() + item.getWidth());
            maxY = Math.max(maxY, item.getY() + item.getHeight());
        }
        return new Rectangle(minX, minY, Math.max(1, maxX - minX), Math.max(1, maxY - minY));
    }

    private javax.swing.Icon iconForLayoutStructureNode(LayoutStructureNode data) {
        if (data == null) {
            return AppIcons.projectIcon();
        }
        if (data.kind() == LayoutStructureNodeKind.GROUP || data.kind() == LayoutStructureNodeKind.ROOT) {
            return AppIcons.projectIcon();
        }
        if (data.kind() == LayoutStructureNodeKind.CUSTOM_ITEM) {
            return data.visible() ? iconForCatmapKind(data.itemKind()) : AppIcons.hiddenIcon();
        }
        if (!data.visible()) {
            return AppIcons.hiddenIcon();
        }
        return switch (data.elementType()) {
            case HEADER -> AppIcons.attrEditIcon();
            case MAP_CONTENT -> AppIcons.imageryIcon();
            case LEGEND -> AppIcons.labelsIcon();
            case NORTH -> LayoutPageRenderer.createNorthPreviewIcon(currentNorthStyle(), 18);
            case SCALE -> AppIcons.attrCalculatorIcon();
            case CARTOUCHE -> AppIcons.fieldsIcon();
            case PROFILE_IMAGE -> AppIcons.imageryIcon();
            case CATMAP_ITEM -> iconForCatmapKind(data.itemKind());
        };
    }

    private String layoutStructureTooltip(LayoutStructureNode data) {
        if (data == null) {
            return null;
        }
        return switch (data.kind()) {
            case ROOT, GROUP -> data.label();
            case ELEMENT -> data.visible()
                    ? "Elemento visible del layout."
                    : "Elemento oculto en el layout.";
            case CUSTOM_ITEM -> {
                String state = (data.visible() ? "visible" : "oculto") + (data.locked() ? " | bloqueado" : "");
                yield data.label() + " | " + state;
            }
        };
    }

    private void installListeners() {
        LayoutListenerWiring.wireAllListeners(this);
    }

    void pushProjectMetadataFromControls() {
        if (ctxProject() == null) {
            return;
        }
        Project project = ctxProject();
        boolean changed = false;
        changed |= updateString(project.getStudyName(), studyField.getText(), project::setStudyName);
        changed |= updateString(project.getCompanyName(), companyField.getText(), project::setCompanyName);
        changed |= updateString(project.getCartographerName(), cartographerField.getText(), project::setCartographerName);
        changed |= updateString(project.getImageSource(), imageSourceField.getText(), project::setImageSource);
        changed |= updateString(project.getCoordinateReference(), coordinateReferenceField.getText(), project::setCoordinateReference);
        changed |= updateString(project.getLegendTitle(), legendTitleField.getText(), project::setLegendTitle);
        changed |= updateString(project.getLegendSubtitle(), legendSubtitleField.getText(), project::setLegendSubtitle);
        changed |= updateString(project.getLogoPath(), logoPathField.getText(), project::setLogoPath);
        changed |= updateString(project.getLayoutImagePath(), layoutImagePathField.getText(), project::setLayoutImagePath);
        if (changed) {
            CatgisDesktopApp.markProjectDirty();
        }
    }

    void pushCatmapNorthSettingsToProject() {
        if (ctxProject() == null) {
            return;
        }
        Project project = ctxProject();
        boolean changed = false;
        String northStyle = currentNorthStyle().name();
        if (!safeTrim(project.getCatmapNorthStyle()).equals(northStyle)) {
            project.setCatmapNorthStyle(northStyle);
            changed = true;
        }
        if (project.isCatmapShowNorth() != northCheck.isSelected()) {
            project.setCatmapShowNorth(northCheck.isSelected());
            changed = true;
        }
        if (changed) {
            CatgisDesktopApp.markProjectDirty();
        }
    }

    private NorthStyle currentProjectNorthStyle() {
        if (ctxProject() == null) {
            return NorthStyle.CLASSIC;
        }
        try {
            return NorthStyle.valueOf(safeTrim(ctxProject().getCatmapNorthStyle()));
        } catch (Exception ignored) {
            return NorthStyle.CLASSIC;
        }
    }

    private boolean currentProjectShowNorth() {
        return ctxProject() == null || ctxProject().isCatmapShowNorth();
    }

    private NorthStyle currentNorthStyle() {
        NorthStyle style = (NorthStyle) northStyleCombo.getSelectedItem();
        return style != null ? style : NorthStyle.CLASSIC;
    }

    private boolean updateString(String current, String updated, java.util.function.Consumer<String> setter) {
        String normalized = safeTrim(updated);
        String currentNormalized = safeTrim(current);
        if (!currentNormalized.equals(normalized)) {
            setter.accept(normalized);
            return true;
        }
        return false;
    }

    public void refreshSnapshot() {
        snapshot = layoutController.captureSnapshot();
        layoutController.setSnapshot(snapshot);
        loadProjectLayersFromProject();
        updateCurrentMapLabel();
        statusLabel.setText("Mapa del layout actualizado desde la vista actual.");
        previewPanel.repaint();
    }

    private void updateCurrentMapLabel() {
        currentMapLabel.setText("<html><b>Mapa actual:</b> " + escape(snapshot.projectName()) +
                "<br><b>CRS:</b> " + escape(snapshot.projectCrsLabel()) +
                "<br><b>Capas visibles:</b> " + snapshot.visibleLayers().size() +
                "<br><b>Ancho visible aprox.:</b> " + escape(snapshot.scaleLabel()) + "</html>");
    }

    public void openLegendEditor() {
        CatmapLegendEditorDialog.open(
                this,
                ctxProject(),
                snapshot != null ? snapshot.visibleLayers() : visibleLayers(),
                () -> {
                    if (ctxProject() != null) {
                        legendTitleField.setText(ctxProject().getLegendTitle());
                        legendSubtitleField.setText(ctxProject().getLegendSubtitle());
                    }
                    statusLabel.setText("Leyenda CATMAP actualizada.");
                    previewPanel.repaint();
                }
        );
    }

    public void exportSvg() {
        JFileChooser fc = FileChooserSupport.createChooser("svg-export", "Exportar a SVG");
        fc.setSelectedFile(new java.io.File("layout.svg"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".svg")) file = new File(file.getAbsolutePath() + ".svg");
        try {
            LayoutSettings settings = buildSettings();
            Dimension size = settings.pageSize().pixelSize(settings.orientation(), settings.dpi());
            BufferedImage composited = renderLayout(settings, size);
            // Embed rendered image as base64 PNG in SVG wrapper
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(composited, "PNG", baos);
            String b64 = java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
            double wMm = settings.pageSize().widthMm, hMm = settings.pageSize().heightMm;
            if (settings.orientation() == PageOrientation.LANDSCAPE) { double t = wMm; wMm = hMm; hMm = t; }
            String svg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n"
                + "  width=\"" + wMm + "mm\" height=\"" + hMm + "mm\" viewBox=\"0 0 " + composited.getWidth() + " " + composited.getHeight() + "\">\n"
                + "  <image x=\"0\" y=\"0\" width=\"" + composited.getWidth() + "\" height=\"" + composited.getHeight() + "\"\n"
                + "    xlink:href=\"data:image/png;base64," + b64 + "\"/>\n"
                + "</svg>";
            java.nio.file.Files.write(file.toPath(), svg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            announceExport("SVG exportado", file);
        } catch (Exception ex) {
            showCompositionError("Error al exportar SVG.", ex);
        }
    }

    public void exportImage() {
        JFileChooser chooser = FileChooserSupport.createChooser("layout-export", "Exportar composicion cartografica");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPG (*.jpg, *.jpeg)", "jpg", "jpeg"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        String lower = file.getName().toLowerCase();
        String format = lower.endsWith(".jpg") || lower.endsWith(".jpeg") ? "jpg" : "png";
        if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) {
            file = new File(file.getAbsolutePath() + ".png");
        }
        FileChooserSupport.rememberFile("layout-export", file);

        try {
            LayoutSettings settings = buildSettings();
            BufferedImage image = renderLayout(settings, settings.pageSize().pixelSize(settings.orientation(), settings.dpi()));
            BufferedImage output = image;
            if ("jpg".equalsIgnoreCase(format) && image.getType() != BufferedImage.TYPE_INT_RGB) {
                output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = output.createGraphics();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, output.getWidth(), output.getHeight());
                g2.drawImage(image, 0, 0, null);
                g2.dispose();
            }
            ImageIO.write(output, format, file);
            announceExport("Composicion exportada", file);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo exportar la composicion a imagen", ex);
            showCompositionError("No se pudo exportar la composicion.", ex);
        }
    }

    public void exportPdf() {
        if (snapshot == null) { refreshSnapshot(); }
        if (snapshot == null || snapshot.mapImage() == null) {
            showCompositionError("No se pudo exportar el PDF porque no hay mapa capturado.", null);
            return;
        }
        JFileChooser chooser = FileChooserSupport.createChooser("layout-export", "Exportar composicion a PDF");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) file = new File(file.getAbsolutePath() + ".pdf");
        FileChooserSupport.rememberFile("layout-export", file);
        try {
            LayoutSettings settings = buildSettings();
            Dimension size = settings.pageSize().pixelSize(settings.orientation(), settings.dpi());
            BufferedImage composited = renderLayout(settings, size);
            try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
                org.apache.pdfbox.pdmodel.common.PDRectangle rect = settings.pageSize().toPdfRectangle(settings.orientation());
                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(rect);
                document.addPage(page);
                BufferedImage rgb = new BufferedImage(composited.getWidth(), composited.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D rg = rgb.createGraphics();
                try { rg.setColor(Color.WHITE); rg.fillRect(0, 0, rgb.getWidth(), rgb.getHeight()); rg.drawImage(composited, 0, 0, null); } finally { rg.dispose(); }
                org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pdfImg = org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory.createFromImage(document, rgb);
                try (org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)) {
                    cs.drawImage(pdfImg, 0, 0, rect.getWidth(), rect.getHeight());
                }
                document.save(file);
                // Guardar referencia de coordenadas como sidecar (no es PDF geoespacial ISO 32000)
                if (ctxMapPanel() != null) {
                    try {
                        double mx = ctxMapPanel().getViewMinX(), my = ctxMapPanel().getViewMinY();
                        double mz = ctxMapPanel().getZoomFactor();
                        java.io.File gf = new java.io.File(file.getAbsolutePath() + ".geo.txt");
                        java.nio.file.Files.write(gf.toPath(), ("# Referencia de coordenadas del mapa\n# X=" + mx + " Y=" + my + " Zoom=" + mz + "\n").getBytes());
                    } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); }
                }
            }
            announceExport("Composicion PDF exportada", file);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo exportar la composicion a PDF", ex);
            showCompositionError("No se pudo exportar el PDF.", ex);
        }
    }

    public void printLayout() {
        try {
            LayoutSettings settings = buildSettings();
            BufferedImage image = renderLayout(settings, settings.pageSize().pixelSize(settings.orientation(), Math.min(settings.dpi(), 200)));

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("CATGIS - Composicion cartografica");
            PageFormat format = job.defaultPage();
            format.setOrientation(settings.orientation() == PageOrientation.LANDSCAPE ? PageFormat.LANDSCAPE : PageFormat.PORTRAIT);
            format = job.pageDialog(format);
            job.setPrintable(new LayoutImagePrintable(image), format);
            if (!job.printDialog()) {
                return;
            }
            job.print();
            statusLabel.setText("Composicion enviada a impresion.");
        } catch (PrinterException ex) {
            AppErrorSupport.logFailure("No se pudo imprimir la composicion", ex);
            showCompositionError("No se pudo imprimir la composicion.", ex);
        }
    }

    private void announceExport(String prefix, File file) {
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(prefix + ": " + file.getName());
        }
        statusLabel.setText(prefix + ": " + file.getAbsolutePath());
        NotificationManager.warn(this, null, prefix + " correctamente:\n" + file.getAbsolutePath());
    }

    public void saveLayout() { saveCatmapLayout(); }
    public void loadLayout() { loadCatmapLayout(); }

    public void saveCatmapLayout() {
        JFileChooser fc = FileChooserSupport.createChooser("catmap-save", "Guardar layout CATMAP");
        fc.setSelectedFile(new java.io.File("layout.catmap"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            LayoutSettings s = buildSettings();
            PageSizePreset ps = s.pageSize();
            String ori = s.orientation() == PageOrientation.LANDSCAPE ? "landscape" : "portrait";
            double w = ps.widthMm, h = ps.heightMm;
            if (s.orientation() == PageOrientation.LANDSCAPE) { double t = w; w = h; h = t; }
            ar.com.catgis.layout.LayoutTemplateManager.saveTemplate(fc.getSelectedFile(), layoutModel, w, h, ori);
            statusLabel.setText("Layout guardado: " + fc.getSelectedFile().getName());
        } catch (Exception ex) {
            showCompositionError("Error al guardar layout.", ex);
        }
    }

    public void loadCatmapLayout() {
        JFileChooser fc = FileChooserSupport.createChooser("catmap-load", "Abrir layout CATMAP");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            layoutModel.clearSelection();
            java.util.List<LayoutElement> toRemove = new java.util.ArrayList<>(layoutModel.getElements());
            for (LayoutElement e : toRemove) layoutModel.removeElement(e.getId());
            ar.com.catgis.layout.LayoutTemplateManager.loadTemplate(fc.getSelectedFile(), layoutModel);
            refreshElementList();
            previewPanel.repaint();
            statusLabel.setText("Layout cargado: " + fc.getSelectedFile().getName());
        } catch (Exception ex) {
            showCompositionError("Error al cargar layout.", ex);
        }
    }

    private void showCompositionError(String intro, Throwable ex) {
        AppErrorSupport.showErrorDialog(this, "Composicion", intro, ex);
    }

    public LayoutSettings buildSettings() {
        pushProjectMetadataFromControls();
        PageSizePreset pageSize = (PageSizePreset) pageSizeCombo.getSelectedItem();
        PageOrientation orientation = (PageOrientation) orientationCombo.getSelectedItem();
        Integer dpiValue = (Integer) dpiCombo.getSelectedItem();
        if (pageSize == null) {
            pageSize = PageSizePreset.A4;
        }
        if (orientation == null) {
            orientation = PageOrientation.LANDSCAPE;
        }
        int dpi = dpiValue != null ? dpiValue : 200;
        LegendPlacement legendPlacement = (LegendPlacement) legendPlacementCombo.getSelectedItem();
        ScaleStyle scaleStyle = (ScaleStyle) scaleStyleCombo.getSelectedItem();
        ScaleRule scaleRule = (ScaleRule) scaleRuleCombo.getSelectedItem();
        NorthStyle northStyle = (NorthStyle) northStyleCombo.getSelectedItem();
        LayoutTemplate layoutTemplate = (LayoutTemplate) templateCombo.getSelectedItem();
        return new LayoutSettings(
                safeTrim(titleField.getText()),
                safeTrim(subtitleField.getText()),
                safeTrim(footerField.getText()),
                safeTrim(studyField.getText()),
                safeTrim(cartoucheProjectField.getText()),
                safeTrim(companyField.getText()),
                safeTrim(cartographerField.getText()),
                safeTrim(imageSourceField.getText()),
                safeTrim(coordinateReferenceField.getText()),
                safeTrim(legendTitleField.getText()),
                safeTrim(legendSubtitleField.getText()),
                safeTrim(logoPathField.getText()),
                safeTrim(layoutImagePathField.getText()),
                layoutTemplate != null ? layoutTemplate : LayoutTemplate.TECHNICAL_RIGHT,
                pageSize,
                orientation,
                dpi,
                legendPlacement != null ? legendPlacement : LegendPlacement.RIGHT_PANEL,
                scaleStyle != null ? scaleStyle : ScaleStyle.SEGMENTED_BAR,
                scaleRule != null ? scaleRule : ScaleRule.PREFERRED_CARTOGRAPHY,
                northStyle != null ? northStyle : NorthStyle.CLASSIC,
                northCheck.isSelected(),
                scaleCheck.isSelected(),
                legendCheck.isSelected(),
                gridCheck.isSelected(),
                (Integer) gridColumnsSpinner.getValue(),
                (Integer) gridRowsSpinner.getValue(),
                gridLabelsCheck.isSelected(),
                copyCatmapItems()
        );
    }

    private void applyManualScale() {
        Double targetDenominator = parseScaleDenominator(mapScaleField.getText());
        if (targetDenominator == null || targetDenominator <= 0) {
            NotificationManager.warn(this, "CATMAP - Escala", "Introduce una escala valida. Ejemplo: 1:5000");
            return;
        }

        double currentDenominator = computeCurrentScaleDenominator(buildSettings());
        if (currentDenominator <= 0) {
            NotificationManager.warn(this, "CATMAP - Escala", "No se pudo calcular la escala actual del mapa.");
            return;
        }

        double requiredZoom = interactionState.getMapZoom() * (currentDenominator / targetDenominator);
        interactionState.setMapZoom(requiredZoom);
        mapScaleField.setText(formatScaleDenominator(targetDenominator));
        statusLabel.setText("Escala del frame ajustada a " + formatScaleDenominator(targetDenominator) + ".");
        previewPanel.repaint();
    }

    private Double parseScaleDenominator(String value) {
        return layoutController.parseScaleDenominator(value);
    }

    public void updateScaleUiState(double exactDenominator) {
        String text = exactDenominator > 0 ? formatScaleDenominator(exactDenominator) : "Escala no disponible";
        scaleInfoLabel.setText("Escala real actual: " + text);
        if (!mapScaleField.hasFocus()) {
            setScaleFieldTextPreservingControlsViewport(exactDenominator > 0 ? text : "");
        }
    }

    private void setScaleFieldTextPreservingControlsViewport(String value) {
        Point previousView = controlsScrollPane.getViewport().getViewPosition();
        mapScaleField.setText(value);
        mapScaleField.setCaretPosition(0);
        SwingUtilities.invokeLater(() -> controlsScrollPane.getViewport().setViewPosition(previousView));
    }

    private double computeCurrentScaleDenominator(LayoutSettings settings) {
        return layoutController.computeCurrentScaleDenominator(settings);
    }

    public static String formatScaleDenominator(double denominator) {
        return LayoutController.formatScaleDenominator(denominator);
    }

    public LayoutSnapshot getSnapshot() {
        return layoutController.getSnapshot();
    }

    private boolean hasLayoutElement(Class<? extends LayoutElement> type) {
        return layoutController.hasLayoutElement(type);
    }

    public void syncHardcodedLayoutFlagsFromModel() {
        layoutController.syncHardcodedLayoutFlagsFromModel();
        if (layoutController.hasLayoutElement(LayoutLegend.class)) {
            legendCheck.setSelected(false);
        }
    }

    private BufferedImage renderLayout(LayoutSettings settings, Dimension size) {
        return layoutController.renderLayout(settings, size);
    }

    private LayoutSnapshot captureSnapshot() {
        return layoutController.captureSnapshot();
    }

    private BufferedImage trimOuterWhitespace(BufferedImage image) {
        return layoutController.trimOuterWhitespace(image);
    }

    private boolean isNearWhiteColumn(BufferedImage image, int x) {
        return layoutController.isNearWhiteColumn(image, x);
    }

    private boolean isNearWhiteRow(BufferedImage image, int y) {
        return layoutController.isNearWhiteRow(image, y);
    }

    private boolean isNearWhite(int argb) {
        return layoutController.isNearWhite(argb);
    }

    private List<Layer> visibleLayers() {
        return layoutController.visibleLayers();
    }

    private boolean isProjectLayerEffectivelyVisible(Layer layer) {
        return layoutController.isProjectLayerEffectivelyVisible(layer);
    }

    private String currentProjectName() {
        return layoutController.currentProjectName();
    }

    private String currentProjectCrs() {
        return layoutController.currentProjectCrs();
    }

    private String currentProjectCrsCode() {
        return layoutController.currentProjectCrsCode();
    }

    private String defaultTitle() {
        return layoutController.defaultTitle();
    }

    private String defaultSubtitle() {
        return layoutController.defaultSubtitle();
    }

    private String defaultFooter() {
        return layoutController.defaultFooter();
    }

    private double estimateRepresentativeScaleMeters(int mapPixelWidth) {
        return layoutController.estimateRepresentativeScaleMeters(mapPixelWidth);
    }

    private boolean isGeographic(String projectCrs) {
        return layoutController.isGeographic(projectCrs);
    }

    static String safeTrim(String value) {
        return LayoutController.safeTrim(value);
    }

    public static String formatDistance(double meters) {
        return LayoutController.formatDistance(meters);
    }

    private static String escape(String value) {
        return LayoutController.escape(value);
    }


    private enum LayoutStructureNodeKind {
        ROOT,
        GROUP,
        ELEMENT,
        CUSTOM_ITEM
    }

    private record LayoutStructureNode(LayoutStructureNodeKind kind,
                                       String label,
                                       LayoutElementType elementType,
                                       String customItemId,
                                       CatmapLayoutItem.Kind itemKind,
                                       boolean visible,
                                       boolean locked) {

        private static LayoutStructureNode root(String label) {
            return new LayoutStructureNode(LayoutStructureNodeKind.ROOT, label, null, null, null, true, false);
        }

        private static LayoutStructureNode group(String label) {
            return new LayoutStructureNode(LayoutStructureNodeKind.GROUP, label, null, null, null, true, false);
        }

        private static LayoutStructureNode element(String label, LayoutElementType elementType, boolean visible, boolean locked, CatmapLayoutItem.Kind itemKind) {
            return new LayoutStructureNode(LayoutStructureNodeKind.ELEMENT, label, elementType, null, itemKind, visible, locked);
        }

        private static LayoutStructureNode customItem(String label, CatmapLayoutItem item) {
            return new LayoutStructureNode(
                    LayoutStructureNodeKind.CUSTOM_ITEM,
                    label,
                    LayoutElementType.CATMAP_ITEM,
                    item != null ? item.getId() : null,
                    item != null ? item.getKind() : CatmapLayoutItem.Kind.TEXT,
                    item == null || item.isVisible(),
                    item != null && item.isLocked()
            );
        }
    }

    private enum AlignmentCommand {
        LEFT("izquierda"),
        CENTER_HORIZONTAL("centro horizontal"),
        RIGHT("derecha"),
        TOP("arriba"),
        CENTER_VERTICAL("medio vertical"),
        BOTTOM("abajo");

        private final String label;

        AlignmentCommand(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }

    public void drawLayoutModelOverlay(Graphics2D g2, LayoutSettings settings, int pageX, int pageY, double scale) {
        if (layoutModel.size() == 0) return;
        double dpi = settings.dpi();
        PageSizePreset ps = settings.pageSize();
        double wMm = ps.widthMm;
        double hMm = ps.heightMm;
        if (settings.orientation() == PageOrientation.LANDSCAPE) { double tmp = wMm; wMm = hMm; hMm = tmp; }
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, dpi, wMm, hMm);
        // Render ALL LayoutElements including LayoutMap (supports multiple maps)
        for (LayoutElement el : layoutModel.getVisibleElementsSortedByZ()) {
            if (el instanceof LayoutScaleBar) {
                double mapScale = estimateMapScale();
                // If a LayoutMap exists, use its scale for better accuracy
                for (LayoutElement m : layoutModel.getElements()) {
                    if (m instanceof LayoutMap && ((LayoutMap)m).isOwnExtent()) {
                        // approximate scale from own extent
                        double zoom = ((LayoutMap)m).getOwnZoomFactor();
                        if (zoom > 0) mapScale = Math.max(100, estimateMapScale() * zoom / Math.max(((LayoutMap)m).getOwnZoomFactor(), 1));
                    }
                }
                ((LayoutScaleBar) el).setMapScaleDenominator(Math.max(100, mapScale));
            }
            Graphics2D g2el = (Graphics2D) g2.create();
            try {
                g2el.translate(pageX, pageY);
                g2el.scale(scale, scale);
                el.render(g2el, ctx);
                int px = ctx.mmToPxInt(el.getBoundsMm().x);
                int py = ctx.mmToPxInt(el.getBoundsMm().y);
                int pw = ctx.mmToPxInt(el.getBoundsMm().width);
                int ph = ctx.mmToPxInt(el.getBoundsMm().height);

                // Hover highlight
                if (el == previewPanel.hoveredElement && !el.isLocked()) {
                    g2el.setColor(new Color(51, 136, 255, 30));
                    g2el.fillRect(px, py, pw, ph);
                    g2el.setColor(new Color(51, 136, 255, 100));
                    g2el.setStroke(new java.awt.BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{4f, 3f}, 0f));
                    g2el.drawRect(px, py, pw, ph);
                }

                // Selection visual
                if (el.isSelected()) {
                    g2el.setColor(new Color(0x1976D2));
                    g2el.setStroke(new java.awt.BasicStroke(el.isLocked() ? 1f : 2f));
                    g2el.drawRect(px, py, pw, ph);
                    if (!el.isLocked()) {
                        int hs = 7;
                        int[][] positions = {{px-hs, py-hs}, {px+pw/2-hs/2, py-hs}, {px+pw-hs, py-hs},
                            {px-hs, py+ph/2-hs/2}, {px+pw-hs, py+ph/2-hs/2},
                            {px-hs, py+ph-hs}, {px+pw/2-hs/2, py+ph-hs}, {px+pw-hs, py+ph-hs}};
                        for (int[] p : positions) {
                            g2el.setColor(Color.WHITE);
                            g2el.fillRect(p[0], p[1], hs, hs);
                            g2el.setColor(new Color(0x1976D2));
                            g2el.setStroke(new java.awt.BasicStroke(1.2f));
                            g2el.drawRect(p[0], p[1], hs, hs);
                        }
                        // Small name label above top-left
                        String nm = el.getName();
                        if (nm != null && !nm.isEmpty()) {
                            g2el.setFont(new Font("SansSerif", Font.PLAIN, 8));
                            int lw = g2el.getFontMetrics().stringWidth(nm);
                            g2el.setColor(new Color(0x1976D2));
                            g2el.fillRect(px, py - 14, lw + 6, 14);
                            g2el.setColor(Color.WHITE);
                            g2el.drawString(nm, px + 3, py - 2);
                        }
                    }
                }
            } finally { g2el.dispose(); }
        }
    }

    public RectMm toPageRectMm() {
        if (previewPanel.lastRenderResult == null || previewPanel.lastPageBounds == null || previewPanel.lastPreviewScale <= 0) return null;
        LayoutSettings settings = buildSettings();
        PageSizePreset ps = settings.pageSize();
        double wMm = ps.widthMm;
        double hMm = ps.heightMm;
        if (settings.orientation() == PageOrientation.LANDSCAPE) { double tmp = wMm; wMm = hMm; hMm = tmp; }
        return new RectMm(0, 0, wMm, hMm, 25.4 / PREVIEW_RENDER_DPI);
    }






    private double estimateMapScale() {
        return layoutController.estimateMapScale();
    }


    private String extractNameFromDisplay(String display) {
        return layoutController.extractNameFromDisplay(display);
    }

    public void duplicateLayoutElement(LayoutElement src) {
        layoutController.duplicateLayoutElement(src);
    }

    private void addQuickElement(String type) {
        int n = layoutModel.size() + 1;
        switch (type) {
            case "map": {
                LayoutMap m = new LayoutMap("map-" + System.currentTimeMillis(), 15, 30, 260, 160);
                m.setZOrder(layoutModel.nextZ()); m.setName("Mapa " + countOfType("Mapa"));
                layoutModel.addElement(m); break;
            }
            case "legend": {
                LayoutLegend leg = new LayoutLegend("legend-" + System.currentTimeMillis(), 180, 30, 85, 40);
                leg.setZOrder(layoutModel.nextZ()); leg.setAutoHeight(true);
                leg.setName("Leyenda " + countOfType("Leyenda"));
                layoutModel.addElement(leg); break;
            }
            case "north": {
                LayoutNorthArrow na = new LayoutNorthArrow("north-" + System.currentTimeMillis(), 270, 175, 18, 18);
                na.setZOrder(layoutModel.nextZ()); na.setName("Norte " + countOfType("Norte"));
                layoutModel.addElement(na); break;
            }
            case "scale": {
                LayoutScaleBar sb = new LayoutScaleBar("scale-" + System.currentTimeMillis(), 15, 185, 100, 12);
                sb.setZOrder(layoutModel.nextZ()); sb.setName("Escala " + countOfType("Escala"));
                layoutModel.addElement(sb); break;
            }
            case "text": {
                LayoutLabel lbl = new LayoutLabel("lbl-" + System.currentTimeMillis(), "Texto libre", 30, 40, 150, 20);
                lbl.setZOrder(layoutModel.nextZ()); lbl.setName("Texto " + countOfType("Texto"));
                layoutModel.addElement(lbl); break;
            }
            case "image": {
                javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagenes", "png", "jpg", "jpeg", "gif", "bmp", "tif", "tiff"));
                if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    try {
                        java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(fc.getSelectedFile());
                        if (bi != null) {
                            double wMm = bi.getWidth() / 200.0 * 25.4;
                            double hMm = bi.getHeight() / 200.0 * 25.4;
                            double cx = 297 / 2.0 - wMm / 2;
                            double cy = 210 / 2.0 - hMm / 2;
                            LayoutImage img = new LayoutImage("img-" + System.currentTimeMillis(), bi, Math.max(10, cx), Math.max(10, cy), wMm, hMm);
                            img.setZOrder(layoutModel.nextZ()); img.setName(fc.getSelectedFile().getName());
                            layoutModel.addElement(img); refreshElementList(); previewPanel.repaint();
                        }
                    } catch (Exception ex) { CatgisLogger.warn("Layout interaction error", ex); }
                }
                break;
            }
            case "rect": {
                LayoutRectangle r = new LayoutRectangle("rect-" + System.currentTimeMillis(), 50, 50, 100, 60);
                r.setZOrder(layoutModel.nextZ()); r.setName("Rectangulo " + countOfType("Rectangulo"));
                layoutModel.addElement(r); break;
            }
            case "table": {
                javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos CSV", "csv"));
                if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    try {
                        LayoutTable t = new LayoutTable("table-" + System.currentTimeMillis(), 15, 100, 267, 80);
                        t.loadCsv(fc.getSelectedFile());
                        t.setZOrder(layoutModel.nextZ()); t.setName("Tabla " + countOfType("Tabla"));
                        layoutModel.addElement(t); refreshElementList(); previewPanel.repaint();
                    } catch (Exception ex) { CatgisLogger.warn("Layout interaction error", ex); }
                }
                break;
            }
            case "cartouche": {
                LayoutCartouche c = new LayoutCartouche("cartouche-" + System.currentTimeMillis(), 12, 175, 270, 55);
                c.setZOrder(layoutModel.nextZ()); c.setName("Cartucho " + countOfType("Cartucho"));
                String proj = ctxProject() != null ? ctxProject().getCompanyName() : "";
                if (proj != null) c.setField("Estudio", proj);
                layoutModel.addElement(c); refreshElementList(); previewPanel.repaint();
                break;
            }
            case "graticule": {
                LayoutGraticule gr = new LayoutGraticule("graticule-" + System.currentTimeMillis(), 15, 25, 267, 160);
                gr.setZOrder(layoutModel.nextZ()); gr.setName("Grilla coord. " + countOfType("Grilla"));
                layoutModel.addElement(gr); refreshElementList(); previewPanel.repaint();
                break;
            }
        }
        refreshElementList();
        previewPanel.repaint();
    }

    public int countOfType(String prefix) {
        return layoutController.countOfType(prefix);
    }

    private void centerOnElement(LayoutElement el) {
        if (el == null || previewPanel.lastPageBounds == null) return;
        java.awt.Rectangle pr = previewPanel.lastPageBounds;
        double cx = el.getBoundsMm().x + el.getBoundsMm().width / 2;
        double cy = el.getBoundsMm().y + el.getBoundsMm().height / 2;
        double pxPerMm = PREVIEW_RENDER_DPI / 25.4 * previewPanel.lastPreviewScale;
        int sx = pr.x + (int)(cx * pxPerMm) - previewPanel.getWidth() / 2;
        int sy = pr.y + (int)(cy * pxPerMm) - previewPanel.getHeight() / 2;
        java.awt.Container parent = previewPanel.getParent();
        while (parent != null) {
            if (parent instanceof javax.swing.JScrollPane jsp) {
                javax.swing.JViewport viewport = jsp.getViewport();
                viewport.setViewPosition(new java.awt.Point(Math.max(0, sx), Math.max(0, sy)));
                break;
            }
            parent = parent.getParent();
        }
    }

    private void importQpt() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("QGIS Template (*.qpt)", "qpt"));
        if (fc.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        try {
            QgisQptImporter.ImportResult res = QgisQptImporter.importQpt(fc.getSelectedFile());
            for (LayoutElement el : res.imported) layoutModel.addElement(el);
            for (String skip : res.skipped) NotificationManager.warn(this, "Elemento omitido", skip);
            refreshAll();
            statusLabel.setText("Importado: " + fc.getSelectedFile().getName() + " (" + res.imported.size() + " elementos)");
        } catch (Exception ex) {
            CatgisLogger.warn("Layout import error", ex);
            NotificationManager.error(this, "Error", "Error al importar: " + ex.getMessage());
        }
    }

    private void showTemplateDialog() {
        showTemplatePicker();
    }

    public void startDrawing(String type) {
        previewPanel.startDrawing(type);
    }

    public void repaintPreview() {
        previewPanel.repaint();
    }

    public void openElementProperties(LayoutElement el) {
        editSelectedLayoutObject();
    }

    private LayoutElement findEl(String id) { return layoutController.findEl(id); }

    public void refreshElementList() {
        if (elementListModel == null) return;
        elementListModel.clear();
        List<LayoutElement> elems = new ArrayList<>(layoutModel.getElements());
        java.util.Collections.reverse(elems);
        for (LayoutElement el : elems) {
            String icon = getTypeIcon(el);
            String visDot = el.isVisible() ? "\u25C9" : "\u25CB";
            String lockIcon = el.isLocked() ? " \uD83D\uDD12" : " \uD83D\uDD13";
            String selPrefix = el.isSelected() ? "> " : "  ";
            String atenuado = el.isVisible() ? "" : " (oculto)";
            elementListModel.addElement(selPrefix + visDot + " " + icon + " " + el.getName() + lockIcon + atenuado);
        }
        refreshPropertiesPanel();
    }

    private String getTypeIcon(LayoutElement el) {
        return layoutController.getTypeIcon(el);
    }

    private LayoutElement findElementByType(Class<?> type) {
        return layoutController.findElementByType(type);
    }

    private LayoutLabel findLayoutLabelByName(String expected) {
        return layoutController.findLayoutLabelByName(expected);
    }

    private void applyMapScale(double denominator) {
        try {
            ar.com.catgis.MapPanel mp = ctxMapPanel();
            if (mp != null && denominator > 0) {
                double currentZoom = mp.getZoomFactor();
                double currentDenom = mp.getCurrentScaleDenominator();
                if (currentDenom <= 0) return;
                double newZoom = currentZoom * (currentDenom / denominator);
                double cx = mp.getViewMinX() + (mp.getWidth() / 2.0) / Math.max(currentZoom, 0.000001);
                double cy = mp.getViewMinY() + (mp.getHeight() / 2.0) / Math.max(currentZoom, 0.000001);
                double hw = (mp.getWidth() / 2.0) / Math.max(newZoom, 0.000001);
                double hh = (mp.getHeight() / 2.0) / Math.max(newZoom, 0.000001);
                mp.restoreView(cx - hw, cy - hh, newZoom);
                statusLabel.setText("Escala aplicada: 1:" + String.format("%,.0f", denominator));
            }
        } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); }
    }

    public int hitTestHandle(LayoutElement el, Point pagePoint, RectMm pageRect) {
        double sc = pageRect.pxToMmScale;
        int px = (int)(el.getBoundsMm().x / sc);
        int py = (int)(el.getBoundsMm().y / sc);
        int pw = (int)(el.getBoundsMm().width / sc);
        int ph = (int)(el.getBoundsMm().height / sc);
        int hs = 6, tol = 4;
        int[][] pos = {{px-hs, py-hs}, {px+pw/2-hs/2, py-hs}, {px+pw-hs, py-hs},
            {px-hs, py+ph/2-hs/2}, {px+pw-hs, py+ph/2-hs/2},
            {px-hs, py+ph-hs}, {px+pw/2-hs/2, py+ph-hs}, {px+pw-hs, py+ph-hs}};
        for (int i = 0; i < pos.length; i++)
            if (pagePoint.x >= pos[i][0]-tol && pagePoint.x <= pos[i][0]+hs+tol && pagePoint.y >= pos[i][1]-tol && pagePoint.y <= pos[i][1]+hs+tol) return i;
        return -1;
    }

    void resizeElement(int idx, int dx, int dy) {
        double s = 25.4 / PREVIEW_RENDER_DPI;
        double dmmx = dx * s, dmmy = dy * s;
        double x = dragStartBoundsMm.x, y = dragStartBoundsMm.y, w = dragStartBoundsMm.width, h = dragStartBoundsMm.height;
        if (idx == 0 || idx == 3 || idx == 5) { x += dmmx; w -= dmmx; }
        if (idx == 0 || idx == 1 || idx == 2) { y += dmmy; h -= dmmy; }
        if (idx == 2 || idx == 4 || idx == 7) w += dmmx;
        if (idx == 5 || idx == 6 || idx == 7) h += dmmy;
        if (w < 5) w = 5; if (h < 5) h = 5;
        draggingLayoutElement.setBoundsMm(x, y, w, h);
    }

    public void pushUndo(LayoutElement el, boolean isDelete) {
        layoutController.pushUndo(el, isDelete);
    }

    private void undo() {
        layoutController.undo();
        previewPanel.repaint();
    }

    private void redo() {
        layoutController.redo();
        previewPanel.repaint();
    }

    private LayoutElement findElementById(String id) {
        return layoutController.findElementById(id);
    }

    private void refreshAll() {
        refreshElementList();
        previewPanel.repaint();
    }

    public void alignElements(int mode) {
        layoutController.alignElements(mode);
        refreshAll();
    }

    public void pushUndoGroup(java.util.List<LayoutElement> elements) {
        layoutController.pushUndoGroup(elements);
    }

    public void refreshPropertiesPanel() {
        if (propertiesInfoLabel == null || propertiesCardPanel == null) return;
        LayoutElement sel = layoutModel.getSelected();
        java.awt.CardLayout cl = (java.awt.CardLayout) propertiesCardPanel.getLayout();
        if (sel instanceof LayoutLegend) {
            rebuildLegendCard((LayoutLegend) sel);
            cl.show(propertiesCardPanel, "legend");
        } else if (sel instanceof LayoutMap) {
            rebuildMapCard((LayoutMap) sel);
            cl.show(propertiesCardPanel, "map");
        } else if (sel instanceof LayoutLabel) {
            rebuildLabelCard((LayoutLabel) sel);
            cl.show(propertiesCardPanel, "label");
        } else if (sel instanceof LayoutImage) {
            rebuildShapeCard(sel, "Imagen");
            cl.show(propertiesCardPanel, "shape");
        } else if (sel instanceof LayoutRectangle) {
            rebuildShapeCard(sel, "Rectangulo");
            cl.show(propertiesCardPanel, "shape");
        } else if (sel instanceof LayoutEllipse) {
            rebuildShapeCard(sel, "Elipse");
            cl.show(propertiesCardPanel, "shape");
        } else if (sel instanceof LayoutLine) {
            rebuildShapeCard(sel, "Linea");
            cl.show(propertiesCardPanel, "shape");
        } else if (sel instanceof LayoutNorthArrow) {
            rebuildShapeCard(sel, "Norte");
            cl.show(propertiesCardPanel, "shape");
        } else if (sel instanceof LayoutScaleBar) {
            rebuildShapeCard(sel, "Escala");
            cl.show(propertiesCardPanel, "shape");
        } else if (sel instanceof LayoutTable) {
            rebuildShapeCard(sel, "Tabla");
            cl.show(propertiesCardPanel, "shape");
        } else if (sel == null) {
            propertiesInfoLabel.setText("<html>Sin elemento<br>seleccionado</html>");
            cl.show(propertiesCardPanel, "generic");
        } else if (sel instanceof LayoutMap) {
            propertiesInfoLabel.setText("<html><b>Mapa</b><br>X:" + String.format("%.1f", sel.getBoundsMm().x)
                + " Y:" + String.format("%.1f", sel.getBoundsMm().y)
                + "<br>W:" + String.format("%.1f", sel.getBoundsMm().width)
                + " H:" + String.format("%.1f", sel.getBoundsMm().height)
                + "<br><i>Vista actual del mapa</i></html>");
            cl.show(propertiesCardPanel, "generic");
        } else if (sel instanceof LayoutLabel) {
            LayoutLabel lab = (LayoutLabel) sel;
            propertiesInfoLabel.setText("<html><b>Texto</b><br>\"" + lab.getText()
                + "\"<br>X:" + String.format("%.1f", sel.getBoundsMm().x)
                + " Y:" + String.format("%.1f", sel.getBoundsMm().y)
                + "<br>W:" + String.format("%.1f", sel.getBoundsMm().width)
                + " H:" + String.format("%.1f", sel.getBoundsMm().height) + "</html>");
            cl.show(propertiesCardPanel, "generic");
        } else {
            propertiesInfoLabel.setText("<html><b>" + sel.getClass().getSimpleName() + "</b><br>"
                + "X:" + String.format("%.1f", sel.getBoundsMm().x)
                + " Y:" + String.format("%.1f", sel.getBoundsMm().y) + "</html>");
            cl.show(propertiesCardPanel, "generic");
        }
    }

    private void rebuildLabelCard(LayoutLabel label) {
        if (propertiesCardPanel == null) return;
        JPanel form = new JPanel(new java.awt.GridBagLayout());
        form.setOpaque(false);
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new Insets(1, 2, 1, 2);
        g.anchor = java.awt.GridBagConstraints.WEST;
        g.fill = java.awt.GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int y = 0;

        // Elemento
        sectionLabel(form, g, y, "Elemento"); y++;
        JTextField nameField = field(form, g, y, "Nombre:", label.getName());
        nameField.addActionListener(e -> { label.setName(nameField.getText().trim()); refreshElementList(); previewPanel.repaint(); });
        y++;
        JTextField xField = field(form, g, y, "X (mm):", String.format("%.1f", label.getBoundsMm().x));
        xField.addActionListener(e -> { try { label.setBoundsMm(Double.parseDouble(xField.getText()), label.getBoundsMm().y, label.getBoundsMm().width, label.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField yField = field(form, g, y, "Y (mm):", String.format("%.1f", label.getBoundsMm().y));
        yField.addActionListener(e -> { try { label.setBoundsMm(label.getBoundsMm().x, Double.parseDouble(yField.getText()), label.getBoundsMm().width, label.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField wField = field(form, g, y, "Ancho:", String.format("%.1f", label.getBoundsMm().width));
        wField.addActionListener(e -> { try { label.setBoundsMm(label.getBoundsMm().x, label.getBoundsMm().y, Double.parseDouble(wField.getText()), label.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField hField = field(form, g, y, "Alto:", String.format("%.1f", label.getBoundsMm().height));
        hField.addActionListener(e -> { try { label.setBoundsMm(label.getBoundsMm().x, label.getBoundsMm().y, label.getBoundsMm().width, Double.parseDouble(hField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        y = addBoolRow(form, g, y, "Visible:", label.isVisible(), v -> { label.setVisible(v); refreshElementList(); previewPanel.repaint(); });
        y = addBoolRow(form, g, y, "Bloqueado:", label.isLocked(), v -> { label.setLocked(v); refreshElementList(); previewPanel.repaint(); });

        // Texto (minimo - doble clic abre popup completo)
        y++; sectionLabel(form, g, y, "Texto"); y++;
        JTextField textField = field(form, g, y, "Contenido:", label.getText() != null ? label.getText() : "");
        textField.addActionListener(e -> { label.setText(textField.getText()); previewPanel.repaint(); });
        y++;
        JLabel hintLbl = new JLabel("<html><i>Doble clic en el elemento para<br>editar fuente, tamano y color.</i></html>");
        hintLbl.setFont(hintLbl.getFont().deriveFont(Font.ITALIC, 9f));
        hintLbl.setForeground(new Color(0x888888));
        g.gridx = 0; g.gridy = y; g.gridwidth = 2; form.add(hintLbl, g); y++;

        // Spacer
        g.gridx = 0; g.gridy = y; g.gridwidth = 2; g.weighty = 1;
        form.add(Box.createVerticalGlue(), g);

        propertiesCardPanel.remove(3);
        propertiesCardPanel.add(form, "label", 3);
        propertiesCardPanel.revalidate();
        propertiesCardPanel.repaint();
    }

    private void rebuildShapeCard(LayoutElement el, String typeLabel) {
        if (propertiesCardPanel == null) return;
        JPanel form = new JPanel(new java.awt.GridBagLayout());
        form.setOpaque(false);
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new Insets(1, 2, 1, 2);
        g.anchor = java.awt.GridBagConstraints.WEST;
        g.fill = java.awt.GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int y = 0;

        sectionLabel(form, g, y, "Elemento: " + typeLabel); y++;
        JTextField nameField = field(form, g, y, "Nombre:", el.getName());
        nameField.addActionListener(e -> { el.setName(nameField.getText().trim()); refreshElementList(); previewPanel.repaint(); });
        y++;
        JTextField xField = field(form, g, y, "X (mm):", String.format("%.1f", el.getBoundsMm().x));
        xField.addActionListener(e -> { try { el.setBoundsMm(Double.parseDouble(xField.getText()), el.getBoundsMm().y, el.getBoundsMm().width, el.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField yField = field(form, g, y, "Y (mm):", String.format("%.1f", el.getBoundsMm().y));
        yField.addActionListener(e -> { try { el.setBoundsMm(el.getBoundsMm().x, Double.parseDouble(yField.getText()), el.getBoundsMm().width, el.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField wField = field(form, g, y, "Ancho:", String.format("%.1f", el.getBoundsMm().width));
        wField.addActionListener(e -> { try { el.setBoundsMm(el.getBoundsMm().x, el.getBoundsMm().y, Double.parseDouble(wField.getText()), el.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField hField = field(form, g, y, "Alto:", String.format("%.1f", el.getBoundsMm().height));
        hField.addActionListener(e -> { try { el.setBoundsMm(el.getBoundsMm().x, el.getBoundsMm().y, el.getBoundsMm().width, Double.parseDouble(hField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        y = addBoolRow(form, g, y, "Visible:", el.isVisible(), v -> { el.setVisible(v); refreshElementList(); previewPanel.repaint(); });
        y = addBoolRow(form, g, y, "Bloqueado:", el.isLocked(), v -> { el.setLocked(v); refreshElementList(); previewPanel.repaint(); });

        if (el instanceof LayoutImage) {
            y++; sectionLabel(form, g, y, "Imagen"); y++;
            JLabel infoLabel = new JLabel("<html>Imagen cargada en memoria.<br>Arrastra una nueva para reemplazar.</html>");
            infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 9f));
            g.gridx = 0; g.gridy = y; g.gridwidth = 2; form.add(infoLabel, g); y++;
        }
        if (el instanceof LayoutCartouche) {
            LayoutCartouche lc = (LayoutCartouche) el;
            y++; sectionLabel(form, g, y, "Datos"); y++;
            for (java.util.Map.Entry<String, String> e : new java.util.ArrayList<>(lc.getFields().entrySet())) {
                JTextField tf = field(form, g, y, e.getKey() + ":", e.getValue() != null ? e.getValue() : "");
                final String key = e.getKey();
                tf.addActionListener(ev -> { lc.setField(key, tf.getText()); previewPanel.repaint(); });
                y++;
            }
        }
        if (el instanceof LayoutGraticule) {
            LayoutGraticule lg = (LayoutGraticule) el;
            y++; sectionLabel(form, g, y, "Grilla"); y++;
            JTextField intXField = field(form, g, y, "Intervalo X:", String.format("%.2f", lg.getIntervalX()));
            intXField.addActionListener(ev -> { try { lg.setIntervalX(Double.parseDouble(intXField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } }); y++;
            JTextField intYField = field(form, g, y, "Intervalo Y:", String.format("%.2f", lg.getIntervalY()));
            intYField.addActionListener(ev -> { try { lg.setIntervalY(Double.parseDouble(intYField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } }); y++;
            y = addBoolRow(form, g, y, "Geografico:", lg.isGeographic(), v -> { lg.setGeographic(v); previewPanel.repaint(); });
            y = addBoolRow(form, g, y, "Etiquetas:", lg.isShowLabels(), v -> { lg.setShowLabels(v); previewPanel.repaint(); });
        }
        if (el instanceof LayoutLine) {
            LayoutLine ll = (LayoutLine) el;
            y++; sectionLabel(form, g, y, "Linea"); y++;
            JTextField lwField = field(form, g, y, "Grosor:", String.format("%.1f", ll.getLineWidth()));
            lwField.addActionListener(ev -> { try { ll.setLineWidth(Float.parseFloat(lwField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } }); y++;
            y = addBoolRow(form, g, y, "Punteada:", ll.isDashed(), v -> { ll.setDashed(v); previewPanel.repaint(); });
        }
        if (el instanceof LayoutEllipse || el instanceof LayoutRectangle) {
            y++; sectionLabel(form, g, y, "Estilo"); y++;
            JTextField bwField = field(form, g, y, "Borde:", String.format("%.1f", el instanceof LayoutEllipse ? ((LayoutEllipse)el).getBorderWidth() : 1.5f));
            bwField.addActionListener(ev -> { try { float v = Float.parseFloat(bwField.getText()); if (el instanceof LayoutEllipse) ((LayoutEllipse)el).setBorderWidth(v); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } }); y++;
        }

        g.gridx = 0; g.gridy = y; g.gridwidth = 2; g.weighty = 1;
        form.add(Box.createVerticalGlue(), g);

        propertiesCardPanel.remove(4);
        propertiesCardPanel.add(form, "shape", 4);
        propertiesCardPanel.revalidate();
        propertiesCardPanel.repaint();
    }

    private void rebuildMapCard(LayoutMap map) {
        if (propertiesCardPanel == null) return;
        JPanel form = new JPanel(new java.awt.GridBagLayout());
        form.setOpaque(false);
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new Insets(1, 2, 1, 2);
        g.anchor = java.awt.GridBagConstraints.WEST;
        g.fill = java.awt.GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int y = 0;

        // Elemento
        sectionLabel(form, g, y, "Elemento"); y++;
        JTextField nameField = field(form, g, y, "Nombre:", map.getName());
        nameField.addActionListener(e -> { map.setName(nameField.getText().trim()); refreshElementList(); previewPanel.repaint(); });
        y++;
        JTextField xField = field(form, g, y, "X (mm):", String.format("%.1f", map.getBoundsMm().x));
        xField.addActionListener(e -> { try { map.setBoundsMm(Double.parseDouble(xField.getText()), map.getBoundsMm().y, map.getBoundsMm().width, map.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField yField = field(form, g, y, "Y (mm):", String.format("%.1f", map.getBoundsMm().y));
        yField.addActionListener(e -> { try { map.setBoundsMm(map.getBoundsMm().x, Double.parseDouble(yField.getText()), map.getBoundsMm().width, map.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField wField = field(form, g, y, "Ancho:", String.format("%.1f", map.getBoundsMm().width));
        wField.addActionListener(e -> { try { map.setBoundsMm(map.getBoundsMm().x, map.getBoundsMm().y, Double.parseDouble(wField.getText()), map.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField hField = field(form, g, y, "Alto:", String.format("%.1f", map.getBoundsMm().height));
        hField.addActionListener(e -> { try { map.setBoundsMm(map.getBoundsMm().x, map.getBoundsMm().y, map.getBoundsMm().width, Double.parseDouble(hField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        y = addBoolRow(form, g, y, "Visible:", map.isVisible(), v -> { map.setVisible(v); refreshElementList(); previewPanel.repaint(); });
        y = addBoolRow(form, g, y, "Bloqueado:", map.isLocked(), v -> { map.setLocked(v); refreshElementList(); previewPanel.repaint(); });

        // Snap & grid
        y++; sectionLabel(form, g, y, "Ajuste"); y++;
        y = addBoolRow(form, g, y, "Snap a grid (5mm):", previewPanel.snapToGrid, v -> { previewPanel.snapToGrid = v; previewPanel.repaint(); });
        y = addBoolRow(form, g, y, "Snap a elementos:", previewPanel.snapToElements, v -> { previewPanel.snapToElements = v; previewPanel.repaint(); });

        // Escala
        y++; sectionLabel(form, g, y, "Escala"); y++;
        double scaleDenom = estimateMapScale();
        JLabel scaleLbl = new JLabel("Actual: 1:" + String.format("%,.0f", scaleDenom));
        scaleLbl.setFont(scaleLbl.getFont().deriveFont(Font.PLAIN, 10f));
        g.gridx = 0; g.gridy = y; g.gridwidth = 2; form.add(scaleLbl, g); y++;

        JTextField targetField = field(form, g, y, "Objetivo 1:", map.getTargetScaleDenominator() > 0
                ? String.format("%,.0f", map.getTargetScaleDenominator()) : "");
        targetField.setToolTipText("Escala deseada. Ej: 5000 para 1:5000");
        y++;
        JPanel scaleBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        scaleBtnRow.setOpaque(false);
        JButton applyBtn = new JButton("Aplicar");
        applyBtn.setFont(applyBtn.getFont().deriveFont(Font.PLAIN, 9f));
        applyBtn.setMargin(new Insets(2, 6, 2, 6));
        applyBtn.addActionListener(e -> {
            try {
                double target = Double.parseDouble(targetField.getText().replace(",", ""));
                if (target > 0) {
                    map.setTargetScaleDenominator(target);
                    applyMapScale(target);
                    rebuildMapCard(map);
                    previewPanel.repaint();
                }
            } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); }
        });
        scaleBtnRow.add(applyBtn);
        g.gridx = 0; g.gridy = y; g.gridwidth = 2; form.add(scaleBtnRow, g); y++;

        // Grilla
        y++; sectionLabel(form, g, y, "Grilla"); y++;
        y = addBoolRow(form, g, y, "Mostrar:", gridCheck.isSelected(), v -> { gridCheck.setSelected(v); previewPanel.repaint(); });

        // Grid mode selector
        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        modeRow.setOpaque(false);
        JRadioButton divBtn = new JRadioButton("Divisiones", !map.isGridByDistance());
        JRadioButton distBtn = new JRadioButton("Distancia", map.isGridByDistance());
        ButtonGroup bg = new ButtonGroup(); bg.add(divBtn); bg.add(distBtn);
        divBtn.setFont(divBtn.getFont().deriveFont(Font.PLAIN, 9f)); divBtn.setOpaque(false);
        distBtn.setFont(distBtn.getFont().deriveFont(Font.PLAIN, 9f)); distBtn.setOpaque(false);
        divBtn.addActionListener(e -> { map.setGridByDistance(false); previewPanel.repaint(); });
        distBtn.addActionListener(e -> { map.setGridByDistance(true); previewPanel.repaint(); });
        modeRow.add(divBtn); modeRow.add(distBtn);
        g.gridx = 0; g.gridy = y; g.gridwidth = 2; form.add(modeRow, g); y++;

        if (map.isGridByDistance()) {
            JTextField intXField = field(form, g, y, "Intervalo X:", String.format("%.1f", map.getGridIntervalX()));
            intXField.addActionListener(e -> { try { map.setGridIntervalX(Double.parseDouble(intXField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
            y++;
            JTextField intYField = field(form, g, y, "Intervalo Y:", String.format("%.1f", map.getGridIntervalY()));
            intYField.addActionListener(e -> { try { map.setGridIntervalY(Double.parseDouble(intYField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
            y++;
            JTextField unitField = field(form, g, y, "Unidad:", map.getGridUnit());
            unitField.addActionListener(e -> { map.setGridUnit(unitField.getText().trim()); previewPanel.repaint(); });
            y++;
        } else {
            JTextField colsField = field(form, g, y, "Columnas:", String.valueOf(gridColumnsSpinner.getValue()));
            colsField.addActionListener(e -> { try { gridColumnsSpinner.setValue(Integer.parseInt(colsField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
            y++;
            JTextField rowsField = field(form, g, y, "Filas:", String.valueOf(gridRowsSpinner.getValue()));
            rowsField.addActionListener(e -> { try { gridRowsSpinner.setValue(Integer.parseInt(rowsField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
            y++;
        }
        y = addBoolRow(form, g, y, "Etiquetas:", gridLabelsCheck.isSelected(), v -> { gridLabelsCheck.setSelected(v); previewPanel.repaint(); });

        // Extent
        y++; sectionLabel(form, g, y, "Extent del mapa"); y++;
        y = addBoolRow(form, g, y, "Independiente:", map.isOwnExtent(), v -> {
            map.setOwnExtent(v);
            if (v) { map.captureFromMainMap(); }
            rebuildMapCard(map); previewPanel.repaint();
        });

        if (map.isOwnExtent()) {
            JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
            btnRow2.setOpaque(false);
            JButton capBtn = new JButton("Capturar vista actual");
            capBtn.setFont(capBtn.getFont().deriveFont(Font.PLAIN, 9f));
            capBtn.setMargin(new Insets(2, 6, 2, 6));
            capBtn.addActionListener(e -> { map.captureFromMainMap(); rebuildMapCard(map); previewPanel.repaint(); });
            btnRow2.add(capBtn);

            JButton syncBtn = new JButton("Sincronizar");
            syncBtn.setFont(syncBtn.getFont().deriveFont(Font.PLAIN, 9f));
            syncBtn.setMargin(new Insets(2, 6, 2, 6));
            syncBtn.addActionListener(e -> { map.setOwnExtent(false); rebuildMapCard(map); previewPanel.repaint(); });
            btnRow2.add(syncBtn);
            g.gridx = 0; g.gridy = y; g.gridwidth = 2; form.add(btnRow2, g); y++;

            JLabel extLbl = new JLabel(String.format("X:%.1f Y:%.1f Zoom:%.2f", map.getOwnViewMinX(), map.getOwnViewMinY(), map.getOwnZoomFactor()));
            extLbl.setFont(extLbl.getFont().deriveFont(Font.PLAIN, 9f));
            extLbl.setForeground(new Color(0x1976D2));
            g.gridx = 0; g.gridy = y; g.gridwidth = 2; form.add(extLbl, g); y++;
        }

        // Acciones
        y++; sectionLabel(form, g, y, "Acciones"); y++;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        btnRow.setOpaque(false);
        JButton refBtn = new JButton("Actualizar");
        refBtn.setFont(refBtn.getFont().deriveFont(Font.PLAIN, 9f));
        refBtn.setMargin(new Insets(2, 6, 2, 6));
        refBtn.addActionListener(e -> { previewPanel.repaint(); });
        btnRow.add(refBtn);
        g.gridx = 0; g.gridy = y; g.gridwidth = 2;
        form.add(btnRow, g);
        y++;

        // Spacer
        g.gridx = 0; g.gridy = y; g.gridwidth = 2; g.weighty = 1;
        form.add(Box.createVerticalGlue(), g);

        propertiesCardPanel.remove(2);
        propertiesCardPanel.add(form, "map", 2);
        propertiesCardPanel.revalidate();
        propertiesCardPanel.repaint();
    }

    private void rebuildLegendCard(LayoutLegend legend) {
        if (propertiesCardPanel == null) return;
        JPanel form = new JPanel(new java.awt.GridBagLayout());
        form.setOpaque(false);
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new Insets(1, 2, 1, 2);
        g.anchor = java.awt.GridBagConstraints.WEST;
        g.fill = java.awt.GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int y = 0;

        // Seccion: Elemento
        sectionLabel(form, g, y, "Elemento"); y++;
        JTextField nameField = field(form, g, y, "Nombre:", legend.getName());
        nameField.addActionListener(e -> { legend.setName(nameField.getText().trim()); refreshElementList(); previewPanel.repaint(); });
        y++;
        JTextField xField = field(form, g, y, "X (mm):", String.format("%.1f", legend.getBoundsMm().x));
        xField.addActionListener(e -> { try { legend.setBoundsMm(Double.parseDouble(xField.getText()), legend.getBoundsMm().y, legend.getBoundsMm().width, legend.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField yField = field(form, g, y, "Y (mm):", String.format("%.1f", legend.getBoundsMm().y));
        yField.addActionListener(e -> { try { legend.setBoundsMm(legend.getBoundsMm().x, Double.parseDouble(yField.getText()), legend.getBoundsMm().width, legend.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField wField = field(form, g, y, "Ancho:", String.format("%.1f", legend.getBoundsMm().width));
        wField.addActionListener(e -> { try { legend.setBoundsMm(legend.getBoundsMm().x, legend.getBoundsMm().y, Double.parseDouble(wField.getText()), legend.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField hField = field(form, g, y, "Alto:", String.format("%.1f", legend.getBoundsMm().height));
        hField.addActionListener(e -> { try { legend.setBoundsMm(legend.getBoundsMm().x, legend.getBoundsMm().y, legend.getBoundsMm().width, Double.parseDouble(hField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        y = addBoolRow(form, g, y, "Visible:", legend.isVisible(), v -> { legend.setVisible(v); refreshElementList(); previewPanel.repaint(); });
        y = addBoolRow(form, g, y, "Bloqueado:", legend.isLocked(), v -> { legend.setLocked(v); refreshElementList(); previewPanel.repaint(); });

        // Seccion: Texto
        y++; sectionLabel(form, g, y, "Texto"); y++;
        JTextField titleField = field(form, g, y, "Titulo:", legend.getTitle() != null ? legend.getTitle() : "Leyenda");
        titleField.addActionListener(e -> { legend.setTitle(titleField.getText().trim()); previewPanel.repaint(); });
        y++;
        JTextField titleSizeField = field(form, g, y, "Tamano titulo:", String.valueOf(legend.getTitleFont().getSize()));
        titleSizeField.addActionListener(e -> { try { legend.setTitleFont(legend.getTitleFont().deriveFont((float)Integer.parseInt(titleSizeField.getText()))); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField itemSizeField = field(form, g, y, "Tamano items:", String.valueOf(legend.getItemFont().getSize()));
        itemSizeField.addActionListener(e -> { try { legend.setItemFont(legend.getItemFont().deriveFont((float)Integer.parseInt(itemSizeField.getText()))); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });

        // Seccion: Contenido
        y++; sectionLabel(form, g, y, "Contenido"); y++;
        JPanel capasPanel = new JPanel(new java.awt.GridLayout(0, 1, 0, 1));
        capasPanel.setOpaque(false);
        for (LayoutLegend.LegendItem item : legend.getItems()) {
            JCheckBox cb = new JCheckBox(item.displayName, item.included);
            cb.setFont(cb.getFont().deriveFont(Font.PLAIN, 10f));
            cb.setOpaque(false);
            cb.addActionListener(e -> { item.included = cb.isSelected(); previewPanel.repaint(); });
            capasPanel.add(cb);
        }
        g.gridx = 0; g.gridy = y; g.gridwidth = 2; g.weightx = 1;
        form.add(capasPanel, g);
        y++;

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        btnRow.setOpaque(false);
        JButton updBtn = new JButton("Desde capas");
        updBtn.setFont(updBtn.getFont().deriveFont(Font.PLAIN, 9f));
        updBtn.setMargin(new Insets(2, 6, 2, 6));
        updBtn.addActionListener(e -> { populateLegendFromProject(legend); rebuildLegendCard(legend); previewPanel.repaint(); });
        JButton exclBtn = new JButton("Excluir WMTS");
        exclBtn.setFont(exclBtn.getFont().deriveFont(Font.PLAIN, 9f));
        exclBtn.setMargin(new Insets(2, 6, 2, 6));
        exclBtn.addActionListener(e -> {
            legend.getItems().removeIf(item -> LayoutLegend.isBasemapName(item.displayName));
            rebuildLegendCard(legend); previewPanel.repaint();
        });
        btnRow.add(updBtn); btnRow.add(exclBtn);
        g.gridx = 0; g.gridy = y; g.gridwidth = 2;
        form.add(btnRow, g);
        y++;

        // Seccion: Caja
        y++; sectionLabel(form, g, y, "Caja"); y++;
        y = addBoolRow(form, g, y, "Mostrar fondo:", legend.isShowBackground(), v -> { legend.setShowBackground(v); previewPanel.repaint(); });
        y = addBoolRow(form, g, y, "Mostrar borde:", legend.isShowBorder(), v -> { legend.setShowBorder(v); previewPanel.repaint(); });
        JTextField paddingField = field(form, g, y, "Padding (mm):", String.format("%.1f", legend.getPaddingMm()));
        y++;
        JTextField opacityField = field(form, g, y, "Opacidad:", String.format("%.0f%%", legend.getBgOpacity() * 100));
        opacityField.addActionListener(e -> { try { float v = Float.parseFloat(opacityField.getText().replace("%","")); legend.setBgOpacity(Math.max(0, Math.min(100, v)) / 100f); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;

        // Seccion: Ajuste
        y++; sectionLabel(form, g, y, "Ajuste"); y++;
        y = addBoolRow(form, g, y, "Alto automatico:", legend.isAutoHeight(), v -> { legend.setAutoHeight(v); previewPanel.repaint(); });
        JTextField colsField = field(form, g, y, "Columnas:", String.valueOf(legend.getColumns()));
        colsField.addActionListener(e -> { try { legend.setColumns(Integer.parseInt(colsField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;
        JTextField symSizeField = field(form, g, y, "Tamano simbolo:", String.format("%.1f", legend.getSymbolSizeMm()));
        symSizeField.addActionListener(e -> { try { legend.setSymbolSizeMm(Double.parseDouble(symSizeField.getText())); previewPanel.repaint(); } catch (Exception ignored) { CatgisLogger.warn("MapLayoutComposerDialog: operation failed", ignored); } });
        y++;

        // Spacer
        g.gridx = 0; g.gridy = y; g.gridwidth = 2; g.weighty = 1;
        form.add(Box.createVerticalGlue(), g);

        propertiesCardPanel.remove(1); // remove old legend card
        propertiesCardPanel.add(form, "legend", 1);
        propertiesCardPanel.revalidate();
        propertiesCardPanel.repaint();
    }

    private void sectionLabel(JPanel form, java.awt.GridBagConstraints g, int y, String text) {
        g.gridx = 0; g.gridy = y; g.gridwidth = 2;
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 10f));
        lbl.setForeground(new Color(0x1976D2));
        form.add(lbl, g);
        g.gridwidth = 1;
    }

    private JTextField field(JPanel form, java.awt.GridBagConstraints g, int y, String label, String value) {
        g.gridx = 0; g.gridy = y; g.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 10f));
        form.add(lbl, g);
        g.gridx = 1; g.weightx = 0.7;
        JTextField tf = new JTextField(value);
        tf.setFont(tf.getFont().deriveFont(Font.PLAIN, 10f));
        tf.setMargin(new Insets(1, 3, 1, 3));
        form.add(tf, g);
        return tf;
    }

    private int addBoolRow(JPanel form, java.awt.GridBagConstraints g, int y, String label, boolean initial, java.util.function.Consumer<Boolean> onChange) {
        g.gridx = 0; g.gridy = y; g.weightx = 0.55;
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 10f));
        form.add(lbl, g);
        g.gridx = 1; g.weightx = 0.45;
        JCheckBox cb = new JCheckBox("", initial);
        cb.setOpaque(false);
        cb.addActionListener(e -> onChange.accept(cb.isSelected()));
        form.add(cb, g);
        return y + 1;
    }

    // ── LayoutViewContext implementation ───────────────────────

    @Override public LayoutModel getLayoutModel() { return layoutModel; }

    @Override public LayoutInteractionState getInteractionState() { return interactionState; }

    @Override public void setStatusMessage(String msg) { statusLabel.setText(msg); }

    @Override public void repaintCanvas() { previewPanel.repaint(); }

    @Override public Dimension getPreviewViewportSize() {
        if (previewScrollPane != null) {
            Dimension extent = previewScrollPane.getViewport().getExtentSize();
            if (extent != null && extent.width > 0 && extent.height > 0) return extent;
        }
        if (previewPanel.getParent() instanceof javax.swing.JViewport viewport) {
            Dimension extent = viewport.getExtentSize();
            if (extent != null && extent.width > 0 && extent.height > 0) return extent;
        }
        if (previewPanel.getWidth() > 0 && previewPanel.getHeight() > 0)
            return new Dimension(previewPanel.getWidth(), previewPanel.getHeight());
        return new Dimension(980, 760);
    }

    @Override public boolean isElementLocked(LayoutElementType type) {
        return interactionState.isElementLocked(type);
    }

    @Override public void selectItemInList(String id) { selectCatmapItemInList(id); }

    @Override public CatmapLayoutItem getCatmapItem(String id) { return getCatmapItemById(id); }

    @Override public String elementLabel(LayoutElementType type) { return layoutElementLabel(type); }

    @Override public void populateLegend(LayoutLegend legend) { populateLegendFromProject(legend); }

    @Override public void resizeCanvasElement(int handleIndex, int dx, int dy) { resizeElement(handleIndex, dx, dy); }

    @Override public void showPopupForElement(LayoutElement el) {
        if (el instanceof LayoutCartouche) { showCartouchePopup((LayoutCartouche) el); return; }
        if (el instanceof LayoutScaleBar) { showScalePopup((LayoutScaleBar) el); return; }
        if (el instanceof LayoutNorthArrow) { showNorthPopup((LayoutNorthArrow) el); return; }
        if (el instanceof LayoutMap) { showMapPropsPopup((LayoutMap) el); return; }
        refreshPropertiesPanel();
    }

    @Override public ar.com.catgis.layout.CanvasRenderer getCanvasRenderer() { return canvasRenderer; }

    // ── Deprecated drag state ──────────────────────────────────

    @Override public LayoutElement getDraggingLayoutElement() { return draggingLayoutElement; }
    @Override public void setDraggingLayoutElement(LayoutElement el) { draggingLayoutElement = el; }
    @Override public Point getDragStartPagePoint() { return dragStartPagePoint; }
    @Override public void setDragStartPagePoint(Point p) { dragStartPagePoint = p; }
    @Override public java.awt.geom.Rectangle2D.Double getDragStartBoundsMm() { return dragStartBoundsMm; }
    @Override public void setDragStartBoundsMm(java.awt.geom.Rectangle2D.Double r) { dragStartBoundsMm = r; }
    @Override public int getActiveResizeHandleIndex() { return activeResizeHandleIndex; }
    @Override public void setActiveResizeHandleIndex(int index) { activeResizeHandleIndex = index; }

    // ── Inline editing fields ──────────────────────────────────

    @Override public String getTitleFieldText() { return titleField.getText(); }
    @Override public void setTitleFieldText(String text) { titleField.setText(text); }
    @Override public String getStudyFieldText() { return studyField.getText(); }
    @Override public void setStudyFieldText(String text) { studyField.setText(text); }
    @Override public String getProjectFieldText() { return cartoucheProjectField.getText(); }
    @Override public void setProjectFieldText(String text) { cartoucheProjectField.setText(text); }
    @Override public String getCompanyFieldText() { return companyField.getText(); }
    @Override public void setCompanyFieldText(String text) { companyField.setText(text); }
    @Override public String getCartographerFieldText() { return cartographerField.getText(); }
    @Override public void setCartographerFieldText(String text) { cartographerField.setText(text); }
    @Override public String getSourceFieldText() { return imageSourceField.getText(); }
    @Override public void setSourceFieldText(String text) { imageSourceField.setText(text); }
    @Override public String getCrsFieldText() { return coordinateReferenceField.getText(); }
    @Override public void setCrsFieldText(String text) { coordinateReferenceField.setText(text); }

    /**
     * Release listeners and resources before disposal.
     * Removes MouseListeners from lists/trees to prevent memory leaks
     * after the dialog is closed and garbage-collected.
     */
    @Override
    public void dispose() {
        for (java.awt.event.MouseListener ml : layoutItemsList.getMouseListeners()) {
            layoutItemsList.removeMouseListener(ml);
        }
        for (java.awt.event.MouseListener ml : layoutStructureTree.getMouseListeners()) {
            layoutStructureTree.removeMouseListener(ml);
        }
        for (java.awt.event.MouseListener ml : projectLayersList.getMouseListeners()) {
            projectLayersList.removeMouseListener(ml);
        }
        previewPanel.cleanup();
        super.dispose();
    }
}
