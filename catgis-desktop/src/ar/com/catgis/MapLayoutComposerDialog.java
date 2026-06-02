package ar.com.catgis;

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
import ar.com.catgis.layout.RulerRenderer;

public class MapLayoutComposerDialog extends JFrame {

    // Context helpers - prefer AppContext over static globals
    private static Project ctxProject() {
        Project p = AppContext.get().getProject();
        return p != null ? p : ctxProject();
    }
    private static MapPanel ctxMapPanel() {
        MapPanel m = AppContext.get().getMapPanel();
        return m != null ? m : ctxMapPanel();
    }

    private static final DateTimeFormatter FOOTER_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int CATMAP_SPLASH_MILLIS = 1100;
    private static final int PREVIEW_RENDER_DPI = 200;
    private static final double CLEAN_HEADER_HEIGHT_RATIO = 1d / 14d;
    private static final double CLEAN_FOOTER_HEIGHT_RATIO = 1d / 9d;
    private static final int CLEAN_HEADER_MIN_HEIGHT = 72;
    private static final int CLEAN_FOOTER_MIN_HEIGHT = 120;
    private static MapLayoutComposerDialog openInstance;

    private final JTextField titleField;
    private final JTextField subtitleField;
    private final JTextField footerField;
    private final JTextField studyField;
    private final JTextField cartoucheProjectField;
    private final JTextField companyField;
    private final JTextField cartographerField;
    private final JTextField imageSourceField;
    private final JTextField coordinateReferenceField;
    private final JTextField legendTitleField;
    private final JTextField legendSubtitleField;
    private final JTextField logoPathField;
    private final JTextField layoutImagePathField;
    private final JTextField mapScaleField;
    private final JComboBox<LayoutTemplate> templateCombo;
    private final JComboBox<PageSizePreset> pageSizeCombo;
    private final JComboBox<PageOrientation> orientationCombo;
    private final JComboBox<Integer> dpiCombo;
    private final JComboBox<LegendPlacement> legendPlacementCombo;
    private final JComboBox<ScaleStyle> scaleStyleCombo;
    private final JComboBox<ScaleRule> scaleRuleCombo;
    private final JComboBox<NorthStyle> northStyleCombo;
    private final JCheckBox northCheck;
    private final JCheckBox scaleCheck;
    private final JCheckBox legendCheck;
    private final JCheckBox gridCheck;
    private final JCheckBox gridLabelsCheck;
    private final JSpinner gridColumnsSpinner;
    private final JSpinner gridRowsSpinner;
    private final LayoutInteractionState interactionState;
    private final LayoutPreviewPanel previewPanel;
    private final JLabel currentMapLabel;
    private final JLabel scaleInfoLabel;
    private final JLabel statusLabel;
    private final DefaultListModel<CatmapLayoutItem> layoutItemsModel;
    private final JList<CatmapLayoutItem> layoutItemsList;
    private final DefaultListModel<Layer> projectLayersModel;
    private final JList<Layer> projectLayersList;
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
    private final JComboBox<CatmapLayoutItem.HorizontalAlign> inspectorAlignCombo;
    private final CardLayout catmapElementsCardLayout;
    private final JPanel catmapElementsCardPanel;
    private final DefaultTreeModel layoutStructureTreeModel;
    private final JTree layoutStructureTree;
    private JScrollPane controlsScrollPane;
    private JScrollPane previewScrollPane;
    private JButton selectionToolButton;
    private JButton mapPanToolButton;
    private JButton mapZoomToolButton;
    private LayoutSnapshot snapshot;
    private boolean syncingLayoutStructureSelection;
    private final List<CatmapLayoutItem> catmapClipboard = new ArrayList<>();
    private final LayoutModel layoutModel = new LayoutModel();
    private LayoutElement draggingLayoutElement;
    private String copiedElementType = null;
    private String copiedElementJson = null;
    private Point dragStartPagePoint;
    private java.awt.geom.Rectangle2D.Double dragStartBoundsMm;
    private int activeResizeHandleIndex = -1;
    private DefaultListModel<String> elementListModel;
    private JLabel propertiesInfoLabel;
    private JPanel propertiesCardPanel;
    private final java.util.ArrayDeque<LayoutUndoEntry> undoStack = new java.util.ArrayDeque<>();
    private final java.util.ArrayDeque<LayoutUndoEntry> redoStack = new java.util.ArrayDeque<>();

    private MapLayoutComposerDialog(Window owner) {
        super("CATMAP - Workspace cartografico");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        snapshot = captureSnapshot();
        interactionState = new LayoutInteractionState();

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
                label.setIcon(LayoutRenderer.createNorthPreviewIcon(value, 18));
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
        previewPanel = new LayoutPreviewPanel();
        installDropTarget();
        currentMapLabel = new JLabel();
        scaleInfoLabel = new JLabel("Escala real actual: calculando...");
        statusLabel = new JLabel("CATMAP listo para maquetar, editar y exportar.");

        northStyleCombo.setSelectedItem(currentProjectNorthStyle());

        loadCatmapItemsFromProject();
        loadProjectLayersFromProject();
        JPanel controlsSidebar = buildControlsSidebar();
        add(buildElementListPanel(), BorderLayout.WEST);
        add(buildPreviewContainer(), BorderLayout.CENTER);
        add(buildPropertiesPanel(), BorderLayout.EAST);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        updateCurrentMapLabel();
        installListeners();
        installCatmapKeyboardActions();
        applyTemplateDefaults((LayoutTemplate) templateCombo.getSelectedItem(), false);
        applyInitialDocumentDefaults();
        installInitialOpenBehavior();

        WindowLayoutSupport.fitFrameToScreen(this, 1380, 900, 1040, 700);
        setLocationRelativeTo(owner);

        LayoutMap mapEl = new LayoutMap("main-map", 15, 25, 267, 160);
        mapEl.setZOrder(layoutModel.nextZ());
        mapEl.setName("Mapa principal");
        layoutModel.addElement(mapEl);

        LayoutLegend legend = new LayoutLegend("main-legend", 155, 55, 75, 40);
        legend.setZOrder(layoutModel.nextZ());
        legend.setAutoHeight(true);
        legend.setShowBackground(false);
        legend.setShowBorder(false);
        legend.setName("Leyenda");
        legend.setTitle("Leyenda");
        populateLegendFromProject(legend);
        layoutModel.addElement(legend);

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

    private void autoComposeLayout() {
        java.util.List<LayoutElement> toRemove = new java.util.ArrayList<>(layoutModel.getElements());
        for (LayoutElement e : toRemove) layoutModel.removeElement(e.getId());

        LayoutTemplateManager.applyTemplate("A4_REFERENCIA", layoutModel);

        // Auto-fill cartouche from project metadata
        for (LayoutElement el : layoutModel.getElements()) {
            if (el instanceof LayoutCartouche) {
                LayoutCartouche lc = (LayoutCartouche) el;
                Project p = ctxProject();
                if (p != null) {
                    if (p.getCompanyName() != null) lc.setField("Estudio", p.getCompanyName());
                    if (p.getCartographerName() != null) lc.setField("Cartografo", p.getCartographerName());
                    lc.setField("Fuente", "Elaboracion propia");
                    lc.setField("Coord.", p.getProjectCRS() != null ? p.getProjectCRS() : "WGS 84 / UTM");
                }
            }
            if (el instanceof LayoutLegend && ctxProject() != null) {
                populateLegendFromProject((LayoutLegend) el);
            }
        }

        refreshElementList();
        previewPanel.repaint();
        statusLabel.setText("Layout profesional creado. Datos del proyecto cargados automaticamente. ¡Exporta en un clic!");
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
                    int opt = javax.swing.JOptionPane.showConfirmDialog(MapLayoutComposerDialog.this,
                        "Agregar " + file.getName() + " como capa al proyecto?",
                        "Archivo vectorial", javax.swing.JOptionPane.YES_NO_OPTION);
                    if (opt == javax.swing.JOptionPane.YES_OPTION) {
                        javax.swing.JOptionPane.showMessageDialog(MapLayoutComposerDialog.this,
                            "Importacion de capas via drag & drop pendiente.\nUse Proyecto > Agregar capa.");
                    }
                } else {
                    javax.swing.JOptionPane.showMessageDialog(MapLayoutComposerDialog.this,
                        "Archivo no soportado: " + file.getName(),
                        "Formato", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        CanvasDropTarget.install(previewPanel, handler, PREVIEW_RENDER_DPI, 1.0,
            () -> previewPanel.lastPageBounds != null ? previewPanel.lastPageBounds.x : 0,
            () -> previewPanel.lastPageBounds != null ? previewPanel.lastPageBounds.y : 0);
    }

    private void populateLegendFromProject(LayoutLegend legend) {
        legend.getItems().clear();
        if (ctxProject() == null || ctxProject().getLayers() == null) return;
        for (Layer layer : ctxProject().getLayers()) {
            if (layer == null || !layer.isVisible()) continue;
            String name = layer.getName();
            if (name == null) continue;
            if (LayoutLegend.isBasemapName(name)) continue;
            if (layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) continue;
            Color c = resolveLayerColor(layer);
            String gtype = resolveGeometryType(layer);
            legend.getItems().add(new LayoutLegend.LegendItem(name, c, gtype));
        }
    }

    private Color resolveLayerColor(Layer layer) {
        if (layer.getPointColor() != null && !layer.getPointColor().equals(Color.BLUE)) return layer.getPointColor();
        if (layer.getLineColor() != null && !layer.getLineColor().equals(Color.RED)) return layer.getLineColor();
        if (layer.getFillColor() != null) return layer.getFillColor();
        return new Color(0x1976D2);
    }

    private String resolveGeometryType(Layer layer) {
        ShapefileData data = ctxMapPanel() != null ? ctxMapPanel().getShapefileData(layer) : null;
        if (data == null) return "VECTOR";
        String family = VectorLayerUtils.resolveGeometryFamily(data);
        return family != null ? family : "VECTOR";
    }

    public static void open() {
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        showCatmapSplashAndOpen(owner, null);
    }

    public static void openWithLayoutImage(File imageFile) {
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        showCatmapSplashAndOpen(owner, imageFile);
    }

    private JPanel buildControlsSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 10));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(378, 100));
        sidebar.add(buildFixedControlsHeaderPanel(), BorderLayout.NORTH);
        controlsScrollPane = buildControlsScrollPane();
        sidebar.add(controlsScrollPane, BorderLayout.CENTER);
        return sidebar;
    }

    private JScrollPane buildControlsScrollPane() {
        JScrollPane scrollPane = new JScrollPane(buildScrollableControlsPanel());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
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
        northPanel.add(buildCatmapWorkspaceHeader(), BorderLayout.NORTH);
        JPanel toolbarSection = new JPanel(new BorderLayout(0, 4));
        toolbarSection.setOpaque(false);
        toolbarSection.add(buildPreviewToolbar(), BorderLayout.NORTH);
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
        workspace.add(buildProjectLayersSidebar(), BorderLayout.EAST);
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

    private void refreshPreviewWorkspace() {
        previewPanel.revalidate();
        previewPanel.repaint();
    }

    private JPanel buildProjectLayersSidebar() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(320, 100));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("Capas del mapa");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(new Color(27, 38, 56));

        JLabel subtitle = new JLabel("<html>Control de visibilidad, orden y simbologia sin salir de CATMAP.</html>");
        subtitle.setForeground(new Color(88, 98, 112));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11.5f));

        projectLayersSummaryLabel.setForeground(new Color(63, 74, 88));
        projectLayersSummaryLabel.setFont(projectLayersSummaryLabel.getFont().deriveFont(Font.BOLD, 11.5f));

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        GridBagConstraints hc = new GridBagConstraints();
        hc.gridx = 0;
        hc.gridy = 0;
        hc.weightx = 1;
        hc.fill = GridBagConstraints.HORIZONTAL;
        hc.anchor = GridBagConstraints.WEST;
        hc.insets = new Insets(0, 0, 4, 0);
        header.add(title, hc);
        hc.gridy++;
        header.add(subtitle, hc);
        hc.gridy++;
        hc.insets = new Insets(6, 0, 0, 0);
        header.add(projectLayersSummaryLabel, hc);

        JScrollPane scrollPane = new JScrollPane(projectLayersList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scrollPane.setPreferredSize(new Dimension(290, 360));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel buttons = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        buttons.setOpaque(false);
        JButton visibilityButton = new JButton("Visible", AppIcons.visibleIcon());
        visibilityButton.addActionListener(e -> toggleSelectedProjectLayerVisibility());
        JButton propertiesButton = new JButton("Simbologia...", AppIcons.propertiesIcon());
        propertiesButton.addActionListener(e -> openSelectedProjectLayerAppearance());
        JButton upButton = new JButton("Subir", AppIcons.upIcon());
        upButton.addActionListener(e -> moveSelectedProjectLayer(-1));
        JButton downButton = new JButton("Bajar", AppIcons.downIcon());
        downButton.addActionListener(e -> moveSelectedProjectLayer(1));
        JButton refreshButton = new JButton("Refrescar", AppIcons.attrRefreshIcon());
        refreshButton.addActionListener(e -> refreshSnapshot());
        buttons.add(visibilityButton);
        buttons.add(propertiesButton);
        buttons.add(upButton);
        buttons.add(downButton);
        buttons.add(refreshButton);

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);
        footer.add(projectLayerDetailLabel, BorderLayout.CENTER);
        footer.add(buttons, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPreviewToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        toolbar.add(buildToolbarGroup("Documento",
                createToolbarButton("Guardar", AppIcons.saveIcon(), "Guardar layout (.catmap)", this::saveCatmapLayout),
                createToolbarButton("Abrir", AppIcons.openIcon(), "Abrir layout (.catmap)", this::loadCatmapLayout),
                createToolbarButton("Exportar", AppIcons.exportIcon(), "Exportar a PDF (un clic)", this::exportPdf),
                createToolbarButton("Exportar PNG", AppIcons.exportIcon(), "Exportar a imagen PNG", this::exportImage),
                createToolbarButton("SVG", null, "Exportar a SVG vectorial", this::exportSvg),
                createToolbarButton("Imprimir", AppIcons.projectIcon(), "Imprimir layout", this::printLayout),
                createToolbarButton("Auto-componer", null, "Crear layout completo automaticamente (mapa+leyenda+escala+norte+titulo). Un solo clic.", this::autoComposeLayout)
        ));

        toolbar.add(buildToolbarGroup("Trabajo",
                selectionToolButton = createToolbarButton("Seleccionar", AppIcons.moveFeatureIcon(), "Selecciona y mueve elementos del layout. Modo por defecto.", this::activateSelectionTool),
                mapPanToolButton = createToolbarButton("Pan mapa", AppIcons.panIcon(), "Desplaza el contenido del mapa sin mover el marco.", this::activateMapPanTool),
                mapZoomToolButton = createToolbarButton("Zoom mapa", AppIcons.zoomInIcon(), "Zoom del contenido del mapa con la rueda del mouse.", this::activateMapFrameZoomTool)
        ));

        toolbar.add(buildToolbarGroup("Insertar",
                createToolbarButton("Texto", AppIcons.attrEditIcon(), "Inserta un texto libre en el layout.", () -> {
                    LayoutLabel lbl = new LayoutLabel("lbl-" + System.currentTimeMillis(), "Texto libre", 60, 60, 160, 24);
                    lbl.setZOrder(layoutModel.nextZ()); lbl.setName("Texto " + (layoutModel.size() + 1));
                    layoutModel.addElement(lbl); refreshElementList(); previewPanel.repaint();
                }),
                createToolbarButton("Imagen", AppIcons.imageryIcon(), "Inserta una imagen desde archivo.", () -> {
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("Imagenes", "png", "jpg", "jpeg", "gif", "bmp"));
                    if (fc.showOpenDialog(MapLayoutComposerDialog.this) == JFileChooser.APPROVE_OPTION) {
                        try { java.awt.image.BufferedImage bi = ImageIO.read(fc.getSelectedFile());
                        if (bi != null) { double w = bi.getWidth()/200.0*25.4, h = bi.getHeight()/200.0*25.4;
                        LayoutImage img = new LayoutImage("img-" + System.currentTimeMillis(), bi, 50, 50, w, h);
                        img.setZOrder(layoutModel.nextZ()); img.setName(fc.getSelectedFile().getName());
                        layoutModel.addElement(img); refreshElementList(); previewPanel.repaint(); }
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                }),
                createToolbarButton("Rectangulo", AppIcons.rectangleIcon(), "Dibujar rectangulo. Click y arrastrar en el canvas.", () -> previewPanel.startDrawing("rect")),
                createToolbarButton("Elipse", AppIcons.circleIcon(), "Dibujar elipse. Click y arrastrar en el canvas.", () -> previewPanel.startDrawing("ellipse")),
                createToolbarButton("Linea", AppIcons.lineIcon(), "Dibujar linea. Click y arrastrar en el canvas.", () -> previewPanel.startDrawing("line"))
        ));

        toolbar.add(buildToolbarGroup("Editar",
                createToolbarButton("Editar", AppIcons.propertiesIcon(), "Editar elemento seleccionado.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) previewPanel.openElementProperties(sel);
                }),
                createToolbarButton("Duplicar", AppIcons.attrCopyIcon(), "Duplicar seleccionado.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { duplicateLayoutElement(sel); refreshElementList(); previewPanel.repaint(); }
                }),
                createToolbarButton("Subir", AppIcons.upIcon(), "Subir en orden visual.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { layoutModel.moveUp(sel); refreshElementList(); previewPanel.repaint(); }
                }),
                createToolbarButton("Bajar", AppIcons.downIcon(), "Bajar en orden visual.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { layoutModel.moveDown(sel); refreshElementList(); previewPanel.repaint(); }
                }),
                createToolbarButton("Quitar", AppIcons.removeIcon(), "Eliminar seleccionado.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { layoutModel.removeElement(sel.getId()); refreshElementList(); previewPanel.repaint(); }
                })
        ));

        toolbar.add(buildToolbarGroup("Alinear",
                createToolbarButton("Izquierda", null, "Alinear izquierda.", () -> alignElements(0)),
                createToolbarButton("Centro", null, "Centrar horizontal.", () -> alignElements(1)),
                createToolbarButton("Derecha", null, "Alinear derecha.", () -> alignElements(2)),
                createToolbarButton("Arriba", null, "Alinear arriba.", () -> alignElements(3)),
                createToolbarButton("Medio", null, "Centrar vertical.", () -> alignElements(4)),
                createToolbarButton("Abajo", null, "Alinear abajo.", () -> alignElements(5))
        ));

        toolbar.add(buildToolbarGroup("Organizar",
                createToolbarButton("Visible", AppIcons.visibleIcon(), "Mostrar/ocultar seleccionado.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { sel.setVisible(!sel.isVisible()); refreshElementList(); previewPanel.repaint(); }
                }),
                createToolbarButton("Bloquear", null, "Bloquear/desbloquear seleccionado.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { sel.setLocked(!sel.isLocked()); refreshElementList(); previewPanel.repaint(); }
                })
        ));

        toolbar.add(buildToolbarGroup("Leyenda",
                createToolbarButton("Leyenda", AppIcons.labelsIcon(), "Selecciona y edita la leyenda.", () -> {
                    LayoutElement leg = findElementByType(LayoutLegend.class);
                    if (leg != null) { layoutModel.clearSelection(); leg.setSelected(true); previewPanel.openElementProperties(leg); refreshElementList(); previewPanel.repaint(); }
                }),
                createToolbarButton("Norte", AppIcons.crsIcon(), "Selecciona y edita el norte.", () -> {
                    LayoutElement north = findElementByType(LayoutNorthArrow.class);
                    if (north != null) { layoutModel.clearSelection(); north.setSelected(true); previewPanel.openElementProperties(north); refreshElementList(); previewPanel.repaint(); }
                })
        ));

        toolbar.add(buildToolbarGroup("Mapa",
                createToolbarButton("Zoom -", AppIcons.zoomOutIcon(), "Alejar contenido del mapa.", () -> adjustMapZoom(1d / 1.12d)),
                createToolbarButton("Zoom +", AppIcons.zoomInIcon(), "Acercar contenido del mapa.", () -> adjustMapZoom(1.12d)),
                createToolbarButton("Reencuadrar", AppIcons.zoomAllIcon(), "Restaurar encuadre original del mapa.", this::resetMapFrameView),
                createToolbarButton("Actualizar", AppIcons.attrRefreshIcon(), "Recapturar snapshot del mapa.", this::refreshSnapshot)
        ));

        toolbar.add(buildToolbarGroup("Pagina",
                createToolbarButton("Zoom -", AppIcons.zoomOutIcon(), "Alejar vista de pagina.", () -> adjustPageZoom(1d / 1.15d)),
                createToolbarButton("Zoom +", AppIcons.zoomInIcon(), "Acercar vista de pagina.", () -> adjustPageZoom(1.15d)),
                createToolbarButton("Ajustar", AppIcons.zoomAllIcon(), "Ajustar pagina completa.", this::fitPageView),
                createToolbarButton("Ajustar ancho", AppIcons.zoomLayerIcon(), "Ajustar al ancho del panel.", this::fitWidthView),
                createToolbarButton("Reset", AppIcons.undoIcon(), "Restaurar vista por defecto.", this::resetLayoutView)
        ));
        updateActiveWorkToolButtons();
        return toolbar;
    }

    private JPanel buildCatmapWorkspaceHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 2));
        header.setOpaque(false);

        JLabel title = new JLabel("CATMAP Workspace");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(new Color(19, 31, 48));

        JLabel subtitle = new JLabel("Subprograma cartografico para maquetacion, mapa vivo y salida final");
        subtitle.setForeground(new Color(77, 87, 101));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildToolbarGroup(String title, JButton... buttons) {
        JPanel group = new JPanel(new BorderLayout(0, 4));
        group.setOpaque(false);
        group.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 225, 233)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JLabel label = new JLabel(title);
        label.setForeground(new Color(76, 85, 97));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        group.add(label, BorderLayout.NORTH);

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder());
        bar.setRollover(true);
        for (JButton button : buttons) {
            if (button != null) {
                bar.add(button);
            }
        }
        group.add(bar, BorderLayout.CENTER);
        return group;
    }

    private JButton createToolbarButton(String text, javax.swing.Icon icon, String toolTip, Runnable action) {
        JButton button = new JButton(text, icon);
        button.setFocusable(false);
        button.setHorizontalTextPosition(JButton.CENTER);
        button.setVerticalTextPosition(JButton.BOTTOM);
        button.setMargin(new Insets(4, 6, 4, 6));
        button.setToolTipText(toolTip);
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
        return button;
    }

    private void updateActiveWorkToolButtons() {
        styleWorkToolButton(selectionToolButton, interactionState.isMapFrameMoveToolActive());
        styleWorkToolButton(mapPanToolButton, interactionState.isMapFramePanToolActive());
        styleWorkToolButton(mapZoomToolButton, interactionState.isMapFrameZoomToolActive());
    }

    private void styleWorkToolButton(JButton button, boolean active) {
        if (button == null) {
            return;
        }
        if (active) {
            button.setBackground(new Color(221, 235, 255));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(66, 133, 244)),
                    BorderFactory.createEmptyBorder(2, 4, 2, 4)
            ));
        } else {
            button.setBackground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 218, 228)),
                    BorderFactory.createEmptyBorder(2, 4, 2, 4)
            ));
        }
    }

    private JPanel buildFixedControlsHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JLabel header = new JLabel("CATMAP");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        header.setForeground(new Color(27, 38, 56));
        panel.add(header, gc);

        gc.gridy++;
        JLabel info = new JLabel("<html>Modulo cartografico de CATGIS para maquetar, componer y exportar mapas con control visual real.</html>");
        info.setFont(info.getFont().deriveFont(Font.PLAIN, 11.5f));
        info.setForeground(new Color(88, 98, 112));
        panel.add(info, gc);

        gc.gridy++;
        addSection(panel, gc, "Pagina y escala");
        gc.gridy++;
        addField(panel, gc, "Plantilla de layout", templateCombo);
        gc.gridy += 2;
        addField(panel, gc, "Tamano de pagina", pageSizeCombo);
        gc.gridy += 2;
        addField(panel, gc, "Orientacion", orientationCombo);
        gc.gridy += 2;
        addField(panel, gc, "Resolucion de salida", dpiCombo);
        gc.gridy += 2;
        addField(panel, gc, "Tipo de escala", scaleStyleCombo);
        gc.gridy += 2;
        addField(panel, gc, "Regla de escala", scaleRuleCombo);
        gc.gridy += 2;
        addField(panel, gc, "Escala objetivo (1:n)", buildScaleEditorPanel());
        gc.gridy += 2;
        scaleInfoLabel.setForeground(new Color(63, 74, 88));
        scaleInfoLabel.setFont(scaleInfoLabel.getFont().deriveFont(Font.PLAIN, 11.5f));
        panel.add(scaleInfoLabel, gc);
        gc.gridy++;
        panel.add(scaleCheck, gc);
        gc.gridy++;
        panel.add(new JLabel(""), gc);
        return panel;
    }

    private JPanel buildScrollableControlsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.setPreferredSize(new Dimension(352, 1180));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        addSection(panel, gc, "Documento cartografico");
        gc.gridy++;
        addField(panel, gc, "Titulo", titleField);
        gc.gridy += 2;
        addField(panel, gc, "Subtitulo", subtitleField);
        gc.gridy += 2;
        addField(panel, gc, "Pie o referencia", footerField);
        gc.gridy += 2;

        addSection(panel, gc, "Grilla cartografica");
        gc.gridy++;
        panel.add(gridCheck, gc);
        gc.gridy++;
        addField(panel, gc, "Columnas", gridColumnsSpinner);
        gc.gridy += 2;
        addField(panel, gc, "Filas", gridRowsSpinner);
        gc.gridy += 2;
        panel.add(gridLabelsCheck, gc);
        gc.gridy++;

        addSection(panel, gc, "Leyenda y norte");
        gc.gridy++;
        addField(panel, gc, "Ubicacion de leyenda", legendPlacementCombo);
        gc.gridy += 2;
        addField(panel, gc, "Titulo de leyenda", legendTitleField);
        gc.gridy += 2;
        addField(panel, gc, "Subtitulo de leyenda", legendSubtitleField);
        gc.gridy += 2;
        addField(panel, gc, "Simbolo de norte", northStyleCombo);
        gc.gridy += 2;
        panel.add(northCheck, gc);
        gc.gridy++;
        panel.add(legendCheck, gc);
        gc.gridy++;

        addSection(panel, gc, "Datos cartograficos y metadatos");
        gc.gridy++;
        addField(panel, gc, "Nombre del estudio", studyField);
        gc.gridy += 2;
        addField(panel, gc, "Empresa", companyField);
        gc.gridy += 2;
        addField(panel, gc, "Cartografo", cartographerField);
        gc.gridy += 2;
        addField(panel, gc, "Origen de la imagen", imageSourceField);
        gc.gridy += 2;
        addField(panel, gc, "Coordenadas / referencia", coordinateReferenceField);
        gc.gridy += 2;
        addLogoSelector(panel, gc);
        gc.gridy += 2;
        addLayoutImageSelector(panel, gc);
        gc.gridy += 2;

        addSection(panel, gc, "Elementos CATMAP");
        gc.gridy++;
        addCatmapElementsSection(panel, gc);
        gc.gridy += 2;

        addSection(panel, gc, "Estado del mapa");
        gc.gridy++;
        gc.insets = new Insets(10, 6, 4, 6);
        currentMapLabel.setForeground(new Color(63, 74, 88));
        panel.add(currentMapLabel, gc);
        gc.gridy++;
        gc.insets = new Insets(10, 6, 6, 6);
        JButton refreshButton = new JButton("Actualizar mapa", AppIcons.attrRefreshIcon());
        refreshButton.addActionListener(e -> refreshSnapshot());
        panel.add(refreshButton, gc);
        gc.gridy++;
        gc.weighty = 1;
        panel.add(new JLabel(""), gc);

        return panel;
    }

    private void addSection(JPanel panel, GridBagConstraints gc, String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(new Color(27, 38, 56));
        panel.add(label, gc);
    }

    private JPanel buildScaleEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        mapScaleField.setToolTipText("Introduce la escala objetivo del frame de mapa. Ejemplo: 1:5000");
        mapScaleField.addActionListener(e -> applyManualScale());
        JButton applyButton = new JButton("Aplicar");
        applyButton.setToolTipText("Ajusta el mapa dentro de CATMAP a la escala indicada.");
        applyButton.addActionListener(e -> applyManualScale());
        panel.add(mapScaleField, BorderLayout.CENTER);
        panel.add(applyButton, BorderLayout.EAST);
        return panel;
    }

    private static BufferedImage loadBundledCatmapHero() {
        try (InputStream in = MapLayoutComposerDialog.class.getResourceAsStream("/icons/catmap-start.png")) {
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (Exception ignored) {
        }
        try {
            File file = new File("src/icons/catmap-start.png");
            if (file.isFile()) {
                return ImageIO.read(file);
            }
        } catch (Exception ignored) {
        }
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

    private void addField(JPanel panel, GridBagConstraints gc, String labelText, java.awt.Component field) {
        panel.add(new JLabel(labelText + ":"), gc);
        gc.gridy++;
        panel.add(field, gc);
    }

    private void addLogoSelector(JPanel panel, GridBagConstraints gc) {
        panel.add(new JLabel("Logo de empresa:"), gc);
        gc.gridy++;
        panel.add(logoPathField, gc);
        gc.gridy++;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        JButton chooseButton = new JButton("Cargar logo...");
        chooseButton.addActionListener(e -> chooseLogoFile());
        JButton clearButton = new JButton("Quitar logo");
        clearButton.addActionListener(e -> {
            logoPathField.setText("");
            pushProjectMetadataFromControls();
            removeExistingLogoItem();
            persistCatmapItems();
            refreshLayoutStructureTree();
            previewPanel.repaint();
        });
        buttons.add(chooseButton);
        buttons.add(clearButton);
        panel.add(buttons, gc);
    }

    private void addLayoutImageSelector(JPanel panel, GridBagConstraints gc) {
        panel.add(new JLabel(I18n.t("Perfil / grafico anclado:")), gc);
        gc.gridy++;
        panel.add(layoutImagePathField, gc);
        gc.gridy++;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        JButton chooseButton = new JButton(I18n.t("Cargar imagen..."));
        chooseButton.addActionListener(e -> chooseLayoutImageFile());
        JButton clearButton = new JButton(I18n.t("Quitar imagen"));
        clearButton.addActionListener(e -> {
            layoutImagePathField.setText("");
            pushProjectMetadataFromControls();
            refreshLayoutStructureTree();
            previewPanel.repaint();
        });
        buttons.add(chooseButton);
        buttons.add(clearButton);
        panel.add(buttons, gc);
    }

    private void addCatmapElementsSection(JPanel panel, GridBagConstraints gc) {
        JLabel tip = new JLabel("<html>Administra los elementos visibles del layout. Los predeterminados se editan desde el mapa o desde la lista.</html>");
        tip.setFont(tip.getFont().deriveFont(Font.PLAIN, 11f));
        tip.setForeground(new Color(88, 98, 112));
        panel.add(tip, gc);
        gc.gridy++;

        JPanel structurePanel = buildLayoutStructurePanel();
        panel.add(structurePanel, gc);
        gc.gridy++;

        JPanel buttons = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        buttons.setOpaque(false);

        JButton addTextButton = new JButton("Texto");
        addTextButton.addActionListener(e -> addCatmapItem(CatmapLayoutItem.Kind.TEXT));

        JButton addImageButton = new JButton("Imagen");
        addImageButton.addActionListener(e -> addCatmapImageItem());

        JButton addRectButton = new JButton("Rectangulo");
        addRectButton.addActionListener(e -> addCatmapItem(CatmapLayoutItem.Kind.RECTANGLE));

        JButton addEllipseButton = new JButton("Elipse");
        addEllipseButton.addActionListener(e -> addCatmapItem(CatmapLayoutItem.Kind.ELLIPSE));

        JButton addLineButton = new JButton("Linea");
        addLineButton.addActionListener(e -> addCatmapItem(CatmapLayoutItem.Kind.LINE));

        JButton duplicateButton = new JButton("Duplicar");
        duplicateButton.addActionListener(e -> duplicateSelectedCatmapItem());

        JButton removeButton = new JButton("Quitar");
        removeButton.addActionListener(e -> removeSelectedCatmapItem());

        JButton restoreButton = new JButton("Restaurar base");
        restoreButton.addActionListener(e -> restoreDefaultLayoutElements());

        buttons.add(addTextButton);
        buttons.add(addImageButton);
        buttons.add(addRectButton);
        buttons.add(addEllipseButton);
        buttons.add(addLineButton);
        buttons.add(duplicateButton);
        buttons.add(removeButton);
        buttons.add(restoreButton);
        panel.add(buttons, gc);
        gc.gridy++;

        JLabel stackLabel = new JLabel("Elementos agregados");
        stackLabel.setFont(stackLabel.getFont().deriveFont(Font.BOLD, 12f));
        stackLabel.setForeground(new Color(63, 74, 88));
        panel.add(stackLabel, gc);
        gc.gridy++;

        JScrollPane scrollPane = new JScrollPane(layoutItemsList);
        scrollPane.setPreferredSize(new Dimension(280, 104));
        JPanel emptyState = new JPanel(new GridBagLayout());
        emptyState.setOpaque(true);
        emptyState.setBackground(new Color(248, 250, 253));
        emptyState.setPreferredSize(new Dimension(280, 56));
        emptyState.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JPanel emptyContent = new JPanel(new BorderLayout(8, 0));
        emptyContent.setOpaque(false);
        JLabel emptyIcon = new JLabel(AppIcons.attrEditIcon());
        emptyIcon.setHorizontalAlignment(JLabel.CENTER);
        JLabel emptyLabel = new JLabel("<html><b>Sin elementos agregados.</b><br>Usa los botones de arriba si necesitás texto, imagen o figuras.</html>");
        emptyLabel.setForeground(new Color(88, 98, 112));
        emptyLabel.setHorizontalAlignment(JLabel.LEFT);
        emptyContent.add(emptyIcon, BorderLayout.WEST);
        emptyContent.add(emptyLabel, BorderLayout.CENTER);
        emptyState.setToolTipText("Doble clic para agregar el primer elemento CATMAP.");
        emptyState.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        emptyState.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
                    promptAndAddCatmapItem();
                }
            }
        });
        emptyState.add(emptyContent);
        catmapElementsCardPanel.setOpaque(false);
        catmapElementsCardPanel.setPreferredSize(new Dimension(296, 66));
        catmapElementsCardPanel.add(scrollPane, "list");
        catmapElementsCardPanel.add(emptyState, "empty");
        panel.add(catmapElementsCardPanel, gc);
        gc.gridy++;

        JPanel inspectorPanel = buildCatmapInspectorPanel();
        panel.add(inspectorPanel, gc);
        gc.gridy++;

    }

    private JPanel buildLayoutStructurePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Elementos del layout"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JScrollPane treeScroll = new JScrollPane(layoutStructureTree);
        treeScroll.setPreferredSize(new Dimension(286, 122));
        panel.add(treeScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new java.awt.GridLayout(1, 3, 6, 0));
        actions.setOpaque(false);
        JButton editButton = new JButton("Editar");
        editButton.addActionListener(e -> handleLayoutStructureDoubleClick());
        JButton visibilityButton = new JButton("Mostrar");
        visibilityButton.addActionListener(e -> toggleSelectedLayoutStructureVisibility());
        JButton lockButton = new JButton("Bloq");
        lockButton.addActionListener(e -> toggleSelectedLayoutStructureLock());
        actions.add(editButton);
        actions.add(visibilityButton);
        actions.add(lockButton);

        JLabel hint = new JLabel("<html>Doble clic edita el elemento seleccionado. Mostrar/Bloq controlan visibilidad y movimiento.</html>");
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
        hint.setForeground(new Color(88, 98, 112));
        JPanel footer = new JPanel(new BorderLayout(0, 6));
        footer.setOpaque(false);
        footer.add(actions, BorderLayout.NORTH);
        footer.add(hint, BorderLayout.SOUTH);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCatmapInspectorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Inspector CATMAP"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        addField(panel, gc, "Tipo", inspectorTypeValueLabel);
        gc.gridy += 2;
        addField(panel, gc, "Etiqueta", inspectorLabelField);
        gc.gridy += 2;

        JPanel positionRow = new JPanel(new GridBagLayout());
        positionRow.setOpaque(false);
        GridBagConstraints pc = new GridBagConstraints();
        pc.insets = new Insets(0, 0, 0, 6);
        pc.gridx = 0;
        pc.gridy = 0;
        pc.fill = GridBagConstraints.HORIZONTAL;
        pc.weightx = 1;
        positionRow.add(labeledMiniField("X", inspectorXField), pc);
        pc.gridx++;
        positionRow.add(labeledMiniField("Y", inspectorYField), pc);
        pc.gridx++;
        positionRow.add(labeledMiniField("W", inspectorWidthField), pc);
        pc.gridx++;
        pc.insets = new Insets(0, 0, 0, 0);
        positionRow.add(labeledMiniField("H", inspectorHeightField), pc);
        addField(panel, gc, "Pos / tam", positionRow);
        gc.gridy += 2;

        addField(panel, gc, "Texto", new JScrollPane(inspectorTextArea));
        gc.gridy += 2;
        addField(panel, gc, "Imagen", inspectorImagePathField);
        gc.gridy += 2;
        addField(panel, gc, "Tam. fuente", inspectorFontSizeSpinner);
        gc.gridy += 2;
        addField(panel, gc, "Grosor", inspectorLineWidthSpinner);
        gc.gridy += 2;
        addField(panel, gc, "Alineacion", inspectorAlignCombo);
        gc.gridy += 2;

        JPanel styleChecks = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        styleChecks.setOpaque(false);
        styleChecks.add(inspectorBoldCheck);
        styleChecks.add(inspectorItalicCheck);
        panel.add(styleChecks, gc);
        gc.gridy++;

        JPanel stateChecks = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        stateChecks.setOpaque(false);
        stateChecks.add(inspectorVisibleCheck);
        stateChecks.add(inspectorLockedCheck);
        panel.add(stateChecks, gc);
        gc.gridy++;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);
        JButton applyButton = new JButton("Aplicar inspector");
        applyButton.addActionListener(e -> applyInspectorToSelectedCatmapItem());
        JButton advancedButton = new JButton("Editor completo...");
        advancedButton.addActionListener(e -> editSelectedCatmapItem());
        JButton toggleVisibilityButton = new JButton("Mostrar/ocultar");
        toggleVisibilityButton.addActionListener(e -> toggleSelectedCatmapItemVisibility());
        JButton toggleLockButton = new JButton("Bloquear/liberar");
        toggleLockButton.addActionListener(e -> toggleSelectedCatmapItemLock());
        actions.add(applyButton);
        actions.add(advancedButton);
        actions.add(toggleVisibilityButton);
        actions.add(toggleLockButton);
        panel.add(actions, gc);

        setInspectorEnabled(false);
        refreshInspectorFromSelection();
        updateCatmapElementsListState();
        return panel;
    }

    private JPanel labeledMiniField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setOpaque(false);
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void applyTemplateDefaults(LayoutTemplate template, boolean resetLayoutState) {
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

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
        bottom.setOpaque(false);

        statusLabel.setForeground(new Color(77, 86, 100));
        bottom.add(statusLabel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton exportImageButton = new JButton("Exportar imagen", AppIcons.exportIcon());
        exportImageButton.addActionListener(e -> exportImage());
        JButton exportPdfButton = new JButton("Exportar PDF", AppIcons.saveIcon());
        exportPdfButton.addActionListener(e -> exportPdf());
        JButton printButton = new JButton("Imprimir...", AppIcons.projectIcon());
        printButton.addActionListener(e -> printLayout());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        buttons.add(exportImageButton);
        buttons.add(exportPdfButton);
        buttons.add(printButton);
        buttons.add(closeButton);
        bottom.add(buttons, BorderLayout.EAST);
        return bottom;
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

    private void refreshProjectLayerDetails() {
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
        return LayoutRenderer.layerTypeLabel(layer);
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

    private void toggleProjectLayerVisibility(Layer layer) {
        if (layer == null) {
            return;
        }
        layer.setVisible(!layer.isVisible());
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.refreshLayerList();
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

    private void toggleSelectedProjectLayerVisibility() {
        Layer layer = projectLayersList.getSelectedValue();
        if (layer == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una capa del panel derecho para cambiar su visibilidad.");
            return;
        }
        toggleProjectLayerVisibility(layer);
    }

    private void openSelectedProjectLayerAppearance() {
        Layer layer = projectLayersList.getSelectedValue();
        if (layer == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una capa del panel derecho para editar su simbologia.");
            return;
        }
        openProjectLayerAppearance(layer);
    }

    private void openProjectLayerAppearance(Layer layer) {
        if (layer == null) {
            return;
        }
        if (layer instanceof RasterLayer) {
            RasterDisplaySettingsDialog.open(this, layer);
        } else if (layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
            JOptionPane.showMessageDialog(
                    this,
                    "En esta ronda las capas online se controlan desde CATMAP por visibilidad y orden.\nLa simbologia detallada sigue sin aplicar como en vector o raster local.",
                    "CATMAP - Capas online",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        } else {
            LayerPropertiesDialog.open(this, layer);
        }
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.refreshLayerList();
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
            JOptionPane.showMessageDialog(this, "Selecciona una capa del panel derecho para reordenarla.");
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
            CatgisDesktopApp.layersPanel.refreshLayerList();
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

    private void persistCatmapItems() {
        if (ctxProject() == null) {
            return;
        }
        ctxProject().setCatmapItems(copyCatmapItems());
        CatgisDesktopApp.markProjectDirty();
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

    private void activateSelectionTool() {
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

    private void activateMapPanTool() {
        interactionState.select(LayoutElementType.MAP_CONTENT);
        interactionState.setMapFrameTool(MapFrameTool.PAN);
        selectCatmapItemInList(null);
        syncLayoutStructureSelection();
        statusLabel.setText("Pan del mapa activo. Arrastra dentro del frame para desplazar el contenido interno.");
        updateActiveWorkToolButtons();
        previewPanel.repaint();
    }

    private void activateMapFrameZoomTool() {
        interactionState.select(LayoutElementType.MAP_CONTENT);
        interactionState.setMapFrameTool(MapFrameTool.ZOOM);
        selectCatmapItemInList(null);
        syncLayoutStructureSelection();
        statusLabel.setText("Lupa de mapa activa. Usa la rueda sobre el frame para acercar o alejar el mapa interno.");
        updateActiveWorkToolButtons();
        previewPanel.repaint();
    }

    private void adjustMapZoom(double factor) {
        interactionState.zoomMap(factor);
        statusLabel.setText("Zoom del mapa dentro de CATMAP: " + Math.round(interactionState.getMapZoom() * 100d) + "%");
        previewPanel.repaint();
    }

    private void resetMapFrameView() {
        interactionState.resetMapView();
        statusLabel.setText("Mapa reencuadrado dentro del cuadro.");
        previewPanel.repaint();
    }

    private void adjustPageZoom(double factor) {
        interactionState.zoomPreview(factor);
        statusLabel.setText("Zoom de composicion actualizado.");
        refreshPreviewWorkspace();
    }

    private void fitPageView() {
        interactionState.fitPage();
        statusLabel.setText("Vista del compositor ajustada a pagina.");
        refreshPreviewWorkspace();
    }

    private void fitWidthView() {
        interactionState.fitWidth();
        statusLabel.setText("Vista del compositor ajustada al ancho.");
        refreshPreviewWorkspace();
    }

    private void resetLayoutView() {
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

    private void configureNorthFromToolbar() {
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
                label.setIcon(LayoutRenderer.createNorthPreviewIcon(value, 18));
                label.setIconTextGap(8);
            }
            return label;
        });

        JLabel previewLabel = new JLabel(LayoutRenderer.createNorthPreviewIcon((NorthStyle) styleCombo.getSelectedItem(), 88));
        previewLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 228)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.WHITE);
        styleCombo.addActionListener(e -> previewLabel.setIcon(LayoutRenderer.createNorthPreviewIcon((NorthStyle) styleCombo.getSelectedItem(), 88)));

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
        JLabel hint = new JLabel("<html>Despues podés mover y redimensionar el norte directamente desde el layout CATMAP.</html>");
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
            statusLabel.setText("Norte CATMAP actualizado. Podés moverlo o redimensionarlo desde el layout.");
        } else {
            interactionState.select(null);
            statusLabel.setText("Norte CATMAP oculto en la composicion.");
        }
        refreshLayoutStructureTree();
        syncLayoutStructureSelection();
        previewPanel.repaint();
    }

    private void editSelectedCatmapItem() {
        CatmapLayoutItem selected = getPrimarySelectedCatmapItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un elemento CATMAP para editar.");
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

    private void duplicateSelectedCatmapItem() {
        List<CatmapLayoutItem> selectedItems = getSelectedCatmapItems();
        if (selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un elemento CATMAP para duplicar.");
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
            JOptionPane.showMessageDialog(this, "Selecciona un elemento CATMAP para reordenar.");
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
            JOptionPane.showMessageDialog(this, "Selecciona un elemento CATMAP para quitar.");
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

    private void deleteSelectedLayoutObject() {
        if (interactionState.getSelectedElement() == LayoutElementType.CATMAP_ITEM || !getSelectedCatmapItems().isEmpty()) {
            removeSelectedCatmapItem();
            return;
        }
        LayoutElementType selected = interactionState.getSelectedElement();
        if (isFixedLayoutElement(selected)) {
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
            statusLabel.setText(layoutElementLabel(selected) + " eliminado visualmente del layout. Podés restaurarlo desde 'Restaurar por defecto'.");
        }
    }

    private void copySelectedCatmapItemsToClipboard() {
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

    private void cutSelectedCatmapItemsToClipboard() {
        copySelectedCatmapItemsToClipboard();
        if (!catmapClipboard.isEmpty()) {
            removeSelectedCatmapItem();
            statusLabel.setText("Elemento(s) CATMAP cortados al portapapeles interno.");
        }
    }

    private void pasteCatmapItemsFromClipboard() {
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
            JOptionPane.showMessageDialog(this, "Selecciona un elemento CATMAP para editar desde el inspector.");
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
            JOptionPane.showMessageDialog(this, "X, Y, W y H deben ser numeros enteros validos.");
        }
    }

    private void toggleSelectedCatmapItemVisibility() {
        List<CatmapLayoutItem> items = getSelectedCatmapItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un elemento CATMAP para cambiar su visibilidad.");
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
            JOptionPane.showMessageDialog(this, "Selecciona un elemento CATMAP para bloquearlo o liberarlo.");
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

    private CatmapLayoutItem getCatmapItemById(String id) {
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

    private void selectCatmapItemInList(String id) {
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

    private void refreshInspectorFromSelection() {
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

    private String layoutElementLabel(LayoutElementType type) {
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
        if (!isFixedLayoutElement(type) || !interactionState.isElementVisible(type)) {
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
        return isFixedLayoutElement(type) && interactionState.isElementLocked(type);
    }

    private void refreshLayoutStructureTree() {
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

    private void syncLayoutStructureSelection() {
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

    private void handleLayoutStructureSelectionChanged() {
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

    private void handleLayoutStructureDoubleClick() {
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
                    openLegendEditor();
                } else if (data.elementType() == LayoutElementType.NORTH) {
                    configureNorthFromToolbar();
                } else if (data.elementType() == LayoutElementType.PROFILE_IMAGE) {
                    chooseLayoutImageFile();
                } else if (data.elementType() == LayoutElementType.MAP_CONTENT) {
                    activateMapFrameTool();
                } else if (data.elementType() == LayoutElementType.HEADER) {
                    titleField.requestFocusInWindow();
                    titleField.selectAll();
                    statusLabel.setText("Encabezado listo para editar desde el panel izquierdo.");
                }
            }
            default -> {
            }
        }
    }

    private void handleLayoutItemsListPopup(MouseEvent e) {
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

    private void handleLayoutStructurePopup(MouseEvent e) {
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
        boolean fixedSelected = isFixedLayoutElement(selected);

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
        if (!isFixedLayoutElement(type)) {
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
        if (!isFixedLayoutElement(type)) {
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
        if (isFixedLayoutElement(selected)) {
            toggleLayoutElementVisibility(selected);
        }
    }

    private void toggleSelectedLayoutObjectLock() {
        if (interactionState.getSelectedElement() == LayoutElementType.CATMAP_ITEM || !getSelectedCatmapItems().isEmpty()) {
            toggleSelectedCatmapItemLock();
            return;
        }
        LayoutElementType selected = interactionState.getSelectedElement();
        if (isFixedLayoutElement(selected)) {
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

    private void nudgeSelectedLayoutObject(int dx, int dy) {
        LayoutElementType selected = interactionState.getSelectedElement();
        if (selected == LayoutElementType.CATMAP_ITEM) {
            for (CatmapLayoutItem item : getUnlockedSelectedCatmapItems()) {
                if (item != null && item.isVisible()) {
                    item.setX(item.getX() + dx);
                    item.setY(item.getY() + dy);
                }
            }
            persistCatmapItems();
        } else if (isFixedLayoutElement(selected) && !interactionState.isElementLocked(selected)) {
            interactionState.translate(selected, dx, dy);
        }
        refreshInspectorFromSelection();
        syncLayoutStructureSelection();
        previewPanel.repaint();
    }

    private void alignSelectedCatmapItems(AlignmentCommand command) {
        List<CatmapLayoutItem> items = getUnlockedSelectedCatmapItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos un elemento CATMAP desbloqueado para alinear.");
            return;
        }
        Rectangle referenceBounds = resolveAlignmentReferenceBounds(items);
        if (referenceBounds == null) {
            JOptionPane.showMessageDialog(this, "CATMAP todavia no tiene una pagina renderizada para alinear elementos.");
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
            JOptionPane.showMessageDialog(this, "Selecciona al menos tres elementos CATMAP desbloqueados para distribuir.");
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
            case NORTH -> LayoutRenderer.createNorthPreviewIcon(currentNorthStyle(), 18);
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
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleUpdate();
            }

            private void handleUpdate() {
                pushProjectMetadataFromControls();
                previewPanel.repaint();
            }
        };
        titleField.getDocument().addDocumentListener(listener);
        subtitleField.getDocument().addDocumentListener(listener);
        footerField.getDocument().addDocumentListener(listener);
        studyField.getDocument().addDocumentListener(listener);
        companyField.getDocument().addDocumentListener(listener);
        cartographerField.getDocument().addDocumentListener(listener);
        imageSourceField.getDocument().addDocumentListener(listener);
        coordinateReferenceField.getDocument().addDocumentListener(listener);
        legendTitleField.getDocument().addDocumentListener(listener);
        legendSubtitleField.getDocument().addDocumentListener(listener);
        templateCombo.addActionListener(e -> {
            LayoutTemplate template = (LayoutTemplate) templateCombo.getSelectedItem();
            interactionState.setTemplate(template);
            applyTemplateDefaults(template, true);
            statusLabel.setText("Plantilla activa: " + (template != null ? template.toString() : "Tecnica"));
            refreshPreviewWorkspace();
        });
        pageSizeCombo.addActionListener(e -> refreshPreviewWorkspace());
        orientationCombo.addActionListener(e -> refreshPreviewWorkspace());
        dpiCombo.addActionListener(e -> refreshPreviewWorkspace());
        legendPlacementCombo.addActionListener(e -> previewPanel.repaint());
        scaleStyleCombo.addActionListener(e -> previewPanel.repaint());
        scaleRuleCombo.addActionListener(e -> previewPanel.repaint());
        northStyleCombo.addActionListener(e -> previewPanel.repaint());
        northStyleCombo.addActionListener(e -> pushCatmapNorthSettingsToProject());
        northCheck.addActionListener(e -> {
            pushCatmapNorthSettingsToProject();
            refreshLayoutStructureTree();
            previewPanel.repaint();
        });
        scaleCheck.addActionListener(e -> {
            refreshLayoutStructureTree();
            previewPanel.repaint();
        });
        legendCheck.addActionListener(e -> {
            refreshLayoutStructureTree();
            previewPanel.repaint();
        });
        gridCheck.addActionListener(e -> previewPanel.repaint());
        gridLabelsCheck.addActionListener(e -> previewPanel.repaint());
        gridColumnsSpinner.addChangeListener(e -> previewPanel.repaint());
        gridRowsSpinner.addChangeListener(e -> previewPanel.repaint());
        layoutItemsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            List<CatmapLayoutItem> selectedItems = layoutItemsList.getSelectedValuesList();
            CatmapLayoutItem selected = !selectedItems.isEmpty() ? selectedItems.get(0) : null;
            refreshInspectorFromSelection();
            if (selected != null) {
                interactionState.selectCustomItem(selected.getId());
                statusLabel.setText(selectedItems.size() > 1
                        ? selectedItems.size() + " elementos CATMAP seleccionados. Podés alinear, distribuir o editar el principal."
                        : "Elemento CATMAP seleccionado. Arrastralo o redimensionalo desde el layout.");
            } else if (interactionState.getSelectedElement() == LayoutElementType.CATMAP_ITEM) {
                interactionState.select(null);
            }
            previewPanel.repaint();
        });
        layoutItemsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && layoutItemsList.getSelectedValue() != null) {
                    editSelectedCatmapItem();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleLayoutItemsListPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleLayoutItemsListPopup(e);
            }
        });
        layoutStructureTree.addTreeSelectionListener(e -> handleLayoutStructureSelectionChanged());
        layoutStructureTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
                    handleLayoutStructureDoubleClick();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleLayoutStructurePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleLayoutStructurePopup(e);
            }
        });
        projectLayersList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            Layer selectedLayer = projectLayersList.getSelectedValue();
            refreshProjectLayerDetails();
            if (selectedLayer != null) {
                statusLabel.setText("Capa de proyecto seleccionada en CATMAP: " + selectedLayer.getName());
            }
        });
        projectLayersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = projectLayersList.locationToIndex(e.getPoint());
                if (index < 0) {
                    return;
                }
                Rectangle cellBounds = projectLayersList.getCellBounds(index, index);
                if (cellBounds == null || !cellBounds.contains(e.getPoint())) {
                    return;
                }
                Layer layer = projectLayersModel.get(index);
                if (layer == null) {
                    return;
                }
                projectLayersList.setSelectedIndex(index);
                int relativeX = e.getPoint().x - cellBounds.x;
                if (SwingUtilities.isLeftMouseButton(e) && relativeX <= 28) {
                    toggleProjectLayerVisibility(layer);
                    return;
                }
                if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openProjectLayerAppearance(layer);
                }
            }
        });
        projectLayersList.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("SPACE"), "toggle-layer-visible");
        projectLayersList.getActionMap().put("toggle-layer-visible", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                toggleSelectedProjectLayerVisibility();
            }
        });
        projectLayersList.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "edit-layer-appearance");
        projectLayersList.getActionMap().put("edit-layer-appearance", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openSelectedProjectLayerAppearance();
            }
        });
    }

    private void pushProjectMetadataFromControls() {
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

    private void pushCatmapNorthSettingsToProject() {
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

    private void refreshSnapshot() {
        snapshot = captureSnapshot();
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

    private void openLegendEditor() {
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

    private void exportSvg() {
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

    private void exportImage() {
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

    private void exportPdf() {
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
                    } catch (Exception ignored) {}
                }
            }
            announceExport("Composicion PDF exportada", file);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo exportar la composicion a PDF", ex);
            showCompositionError("No se pudo exportar el PDF.", ex);
        }
    }

    private void printLayout() {
        try {
            LayoutSettings settings = buildSettings();
            BufferedImage image = renderLayout(settings, settings.pageSize().pixelSize(settings.orientation(), Math.min(settings.dpi(), 200)));

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("CATGIS - Composicion cartografica");
            PageFormat format = job.defaultPage();
            format.setOrientation(settings.orientation() == PageOrientation.LANDSCAPE ? PageFormat.LANDSCAPE : PageFormat.PORTRAIT);
            format = job.pageDialog(format);
            job.setPrintable(new ImagePrintable(image), format);
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
            CatgisDesktopApp.statusBar.setMessage(prefix + ": " + file.getName());
        }
        statusLabel.setText(prefix + ": " + file.getAbsolutePath());
        JOptionPane.showMessageDialog(this, prefix + " correctamente:\n" + file.getAbsolutePath());
    }

    private void saveCatmapLayout() {
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

    private void loadCatmapLayout() {
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

    private LayoutSettings buildSettings() {
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
            JOptionPane.showMessageDialog(this, "Introduce una escala valida. Ejemplo: 1:5000", "CATMAP - Escala", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double currentDenominator = computeCurrentScaleDenominator(buildSettings());
        if (currentDenominator <= 0) {
            JOptionPane.showMessageDialog(this, "No se pudo calcular la escala actual del mapa.", "CATMAP - Escala", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double requiredZoom = interactionState.getMapZoom() * (currentDenominator / targetDenominator);
        interactionState.setMapZoom(requiredZoom);
        mapScaleField.setText(formatScaleDenominator(targetDenominator));
        statusLabel.setText("Escala del frame ajustada a " + formatScaleDenominator(targetDenominator) + ".");
        previewPanel.repaint();
    }

    private Double parseScaleDenominator(String value) {
        String text = safeTrim(value);
        if (text.isBlank()) {
            return null;
        }
        int colonIndex = text.indexOf(':');
        if (colonIndex >= 0 && colonIndex < text.length() - 1) {
            text = text.substring(colonIndex + 1);
        }
        text = text.replaceAll("[^0-9]", "");
        if (text.isBlank()) {
            return null;
        }
        try {
            double denominator = Double.parseDouble(text);
            return denominator > 0 ? denominator : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void updateScaleUiState(double exactDenominator) {
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
        if (settings == null || snapshot == null) {
            return 0d;
        }
        Dimension previewSize = settings.pageSize().pixelSize(settings.orientation(), PREVIEW_RENDER_DPI);
        LayoutRenderResult result = LayoutRenderer.renderResult(
                settings,
                snapshot,
                previewSize.width,
                previewSize.height,
                interactionState,
                PREVIEW_RENDER_DPI
        );
        return result.exactScaleDenominator();
    }

    private static String formatScaleDenominator(double denominator) {
        if (denominator <= 0) {
            return "Escala no disponible";
        }
        return "1:" + new DecimalFormat("#,##0").format(Math.round(denominator));
    }

    private LayoutSnapshot getSnapshot() {
        return snapshot;
    }

    private BufferedImage renderLayout(LayoutSettings settings, Dimension size) {
        // Auto-disable hardcoded elements that exist in LayoutModel
        for (LayoutElement el : layoutModel.getElements()) {
            if (el instanceof LayoutLegend) legendCheck.setSelected(false);
            if (el instanceof LayoutNorthArrow) interactionState.setElementVisible(LayoutElementType.NORTH, false);
            if (el instanceof LayoutScaleBar) interactionState.setElementVisible(LayoutElementType.SCALE, false);
            if (el instanceof LayoutCartouche) interactionState.setElementVisible(LayoutElementType.CARTOUCHE, false);
            if (el instanceof LayoutMap) interactionState.setElementVisible(LayoutElementType.MAP_CONTENT, false);
        }
        BufferedImage base = LayoutRenderer.render(settings, snapshot, size.width, size.height, interactionState, settings.dpi());
        if (layoutModel.size() > 0) {
            Graphics2D g2 = base.createGraphics();
            try {
                PageSizePreset ps = settings.pageSize();
                double wMm = ps.widthMm, hMm = ps.heightMm;
                if (settings.orientation() == PageOrientation.LANDSCAPE) { double tmp = wMm; wMm = hMm; hMm = tmp; }
                LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.EXPORT_IMAGE, settings.dpi(), wMm, hMm);
                for (LayoutElement el : layoutModel.getVisibleElementsSortedByZ()) {
                    if (el instanceof LayoutMap) continue;
                    if (el instanceof LayoutScaleBar) {
                        ((LayoutScaleBar) el).setMapScaleDenominator(Math.max(100, estimateMapScale()));
                    }
                    el.render(g2, ctx);
                }
            } finally { g2.dispose(); }
        }
        return base;
    }

    private LayoutSnapshot captureSnapshot() {
        if (ctxMapPanel() == null) {
            return new LayoutSnapshot(
                    new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB),
                    visibleLayers(),
                    currentProjectName(),
                    currentProjectCrs(),
                    currentProjectCrsCode(),
                    "Escala no disponible",
                    0,
                    0,
                    0,
                    1d,
                    1200,
                    800
            );
        }

        int mapWidth = Math.max(ctxMapPanel().getWidth(), 1200);
        int mapHeight = Math.max(ctxMapPanel().getHeight(), 800);
        BufferedImage mapImage = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = mapImage.createGraphics();
        try {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, mapWidth, mapHeight);
            ctxMapPanel().paint(g2);
        } finally {
            g2.dispose();
        }
        mapImage = trimOuterWhitespace(mapImage);

        double scaleMeters = estimateRepresentativeScaleMeters(mapWidth);
        return new LayoutSnapshot(
                mapImage,
                visibleLayers(),
                currentProjectName(),
                currentProjectCrs(),
                currentProjectCrsCode(),
                formatDistance(scaleMeters),
                scaleMeters,
                ctxMapPanel().getViewMinX(),
                ctxMapPanel().getViewMinY(),
                Math.max(ctxMapPanel().getZoomFactor(), 0.000001d),
                Math.max(1, ctxMapPanel().getWidth()),
                Math.max(1, ctxMapPanel().getHeight())
        );
    }

    private BufferedImage trimOuterWhitespace(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= 2 || height <= 2) {
            return image;
        }

        int left = 0;
        int right = width - 1;
        int top = 0;
        int bottom = height - 1;

        while (left < right && isNearWhiteColumn(image, left)) {
            left++;
        }
        while (right > left && isNearWhiteColumn(image, right)) {
            right--;
        }
        while (top < bottom && isNearWhiteRow(image, top)) {
            top++;
        }
        while (bottom > top && isNearWhiteRow(image, bottom)) {
            bottom--;
        }

        if (left <= 0 && right >= width - 1 && top <= 0 && bottom >= height - 1) {
            return image;
        }
        int croppedWidth = Math.max(1, right - left + 1);
        int croppedHeight = Math.max(1, bottom - top + 1);
        if (croppedWidth < width / 4 || croppedHeight < height / 4) {
            return image;
        }
        BufferedImage cropped = image.getSubimage(left, top, croppedWidth, croppedHeight);
        BufferedImage copy = new BufferedImage(croppedWidth, croppedHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D copyGraphics = copy.createGraphics();
        try {
            copyGraphics.drawImage(cropped, 0, 0, null);
        } finally {
            copyGraphics.dispose();
        }
        return copy;
    }

    private boolean isNearWhiteColumn(BufferedImage image, int x) {
        for (int y = 0; y < image.getHeight(); y += Math.max(1, image.getHeight() / 120)) {
            if (!isNearWhite(image.getRGB(x, y))) {
                return false;
            }
        }
        return true;
    }

    private boolean isNearWhiteRow(BufferedImage image, int y) {
        for (int x = 0; x < image.getWidth(); x += Math.max(1, image.getWidth() / 160)) {
            if (!isNearWhite(image.getRGB(x, y))) {
                return false;
            }
        }
        return true;
    }

    private boolean isNearWhite(int argb) {
        int alpha = (argb >> 24) & 0xFF;
        if (alpha < 10) {
            return true;
        }
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        return red >= 245 && green >= 245 && blue >= 245;
    }

    private List<Layer> visibleLayers() {
        List<Layer> visible = new ArrayList<>();
        if (ctxProject() == null || ctxProject().getLayers() == null) {
            return visible;
        }
        for (Layer layer : ctxProject().getLayers()) {
            if (layer != null && isProjectLayerEffectivelyVisible(layer)) {
                visible.add(layer);
            }
        }
        return visible;
    }

    private boolean isProjectLayerEffectivelyVisible(Layer layer) {
        if (layer == null) {
            return false;
        }
        if (ctxProject() != null) {
            return ctxProject().isLayerEffectivelyVisible(layer);
        }
        return layer.isVisible();
    }

    private String currentProjectName() {
        if (ctxProject() == null || ctxProject().getName() == null || ctxProject().getName().isBlank()) {
            return "Proyecto actual";
        }
        return ctxProject().getName();
    }

    private String currentProjectCrs() {
        return CRSDefinitions.getLabelForCode(currentProjectCrsCode());
    }

    private String currentProjectCrsCode() {
        String code = ctxProject() != null ? ctxProject().getProjectCRS() : "";
        return CRSDefinitions.normalizeCode(code != null ? code : "");
    }

    private String defaultTitle() {
        return currentProjectName();
    }

    private String defaultSubtitle() {
        return "Vista cartografica generada desde la vista actual";
    }

    private String defaultFooter() {
        return "Generado en CATGIS | " + FOOTER_DATE.format(LocalDateTime.now());
    }

    private double estimateRepresentativeScaleMeters(int mapPixelWidth) {
        if (ctxMapPanel() == null || mapPixelWidth <= 0) {
            return 0;
        }

        double zoomFactor = Math.max(ctxMapPanel().getZoomFactor(), 0.000001d);
        double projectWidth = Math.max(1d, mapPixelWidth / zoomFactor);
        String projectCrs = ctxProject() != null ? ctxProject().getProjectCRS() : "";
        if (projectCrs == null) {
            projectCrs = "";
        }
        projectCrs = CRSDefinitions.normalizeCode(projectCrs);

        if (isGeographic(projectCrs)) {
            double centerLat = ctxMapPanel().getViewMinY()
                    + (Math.max(1, ctxMapPanel().getHeight()) / 2d) / zoomFactor;
            double metersPerDegreeLon = 111320d * Math.cos(Math.toRadians(centerLat));
            metersPerDegreeLon = Math.max(1d, Math.abs(metersPerDegreeLon));
            return projectWidth * metersPerDegreeLon;
        }
        return projectWidth;
    }

    private boolean isGeographic(String projectCrs) {
        return "EPSG:4326".equalsIgnoreCase(projectCrs)
                || "EPSG:4258".equalsIgnoreCase(projectCrs)
                || "EPSG:4269".equalsIgnoreCase(projectCrs)
                || "EPSG:4674".equalsIgnoreCase(projectCrs)
                || "EPSG:4190".equalsIgnoreCase(projectCrs)
                || "EPSG:4221".equalsIgnoreCase(projectCrs);
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String formatDistance(double meters) {
        if (meters <= 0) {
            return "Escala no disponible";
        }
        if (meters >= 1000d) {
            return new DecimalFormat("#,##0.## km").format(meters / 1000d);
        }
        return new DecimalFormat("#,##0 m").format(meters);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    enum LayoutTemplate {
        TECHNICAL_RIGHT("Tecnica - leyenda derecha", LegendPlacement.RIGHT_PANEL),
        BOTTOM_REFERENCE("Referencia inferior", LegendPlacement.BOTTOM_PANEL),
        CLEAN_CENTERED("Limpia centrada", LegendPlacement.MAP_BOTTOM_RIGHT),
        STRONG_CARTOUCHE("Datos cartograficos enfatizados", LegendPlacement.RIGHT_PANEL);

        private final String label;
        private final LegendPlacement defaultLegendPlacement;

        LayoutTemplate(String label, LegendPlacement defaultLegendPlacement) {
            this.label = label;
            this.defaultLegendPlacement = defaultLegendPlacement;
        }

        LegendPlacement defaultLegendPlacement() {
            return defaultLegendPlacement;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    enum PageSizePreset {
        A3("A3", 297d, 420d),
        A4("A4", 210d, 297d),
        A5("A5", 148d, 210d),
        LETTER("Carta", 216d, 279d),
        LEGAL("Legal", 216d, 356d),
        TABLOID("Tabloide", 279d, 432d);

        private final String label;
        private final double widthMm;
        private final double heightMm;

        PageSizePreset(String label, double widthMm, double heightMm) {
            this.label = label;
            this.widthMm = widthMm;
            this.heightMm = heightMm;
        }

        Dimension pixelSize(PageOrientation orientation, int dpi) {
            double widthInches = (orientation == PageOrientation.LANDSCAPE ? heightMm : widthMm) / 25.4d;
            double heightInches = (orientation == PageOrientation.LANDSCAPE ? widthMm : heightMm) / 25.4d;
            int width = Math.max(600, (int) Math.round(widthInches * dpi));
            int height = Math.max(600, (int) Math.round(heightInches * dpi));
            return new Dimension(width, height);
        }

        PDRectangle toPdfRectangle(PageOrientation orientation) {
            float widthPoints = (float) ((orientation == PageOrientation.LANDSCAPE ? heightMm : widthMm) / 25.4d * 72d);
            float heightPoints = (float) ((orientation == PageOrientation.LANDSCAPE ? widthMm : heightMm) / 25.4d * 72d);
            return new PDRectangle(widthPoints, heightPoints);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum PreviewScaleMode {
        FIT_PAGE,
        FIT_WIDTH,
        CUSTOM
    }

    private enum MapFrameTool {
        MOVE_FRAME,
        PAN,
        ZOOM
    }

    private enum LayoutElementType {
        HEADER,
        MAP_CONTENT,
        LEGEND,
        NORTH,
        SCALE,
        CARTOUCHE,
        PROFILE_IMAGE,
        CATMAP_ITEM
    }

    private static boolean isFixedLayoutElement(LayoutElementType type) {
        return type != null && type != LayoutElementType.CATMAP_ITEM;
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

    private enum ResizeHandle {
        NONE,
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NORTH_EAST,
        NORTH_WEST,
        SOUTH_EAST,
        SOUTH_WEST
    }

    enum PageOrientation {
        LANDSCAPE("Horizontal"),
        PORTRAIT("Vertical");

        private final String label;

        PageOrientation(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum LegendPlacement {
        RIGHT_PANEL("Panel derecho"),
        BOTTOM_PANEL("Franja inferior"),
        MAP_TOP_RIGHT("Dentro del mapa - arriba derecha"),
        MAP_BOTTOM_RIGHT("Dentro del mapa - abajo derecha"),
        MAP_BOTTOM_LEFT("Dentro del mapa - abajo izquierda");

        private final String label;

        LegendPlacement(String label) {
            this.label = label;
        }

        boolean isInsideMap() {
            return this == MAP_TOP_RIGHT || this == MAP_BOTTOM_RIGHT || this == MAP_BOTTOM_LEFT;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum ScaleStyle {
        SEGMENTED_BAR("Barra segmentada"),
        SIMPLE_BAR("Barra simple"),
        NUMERIC("Escala numerica");

        private final String label;

        ScaleStyle(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum ScaleRule {
        PREFERRED_CARTOGRAPHY("Preferidas 500/1000/2000/5000/10000", new double[]{250d, 500d, 1000d, 2000d, 5000d, 10000d, 20000d, 50000d, 100000d}),
        ENGINEERING("Ingenieria 1-2-5", null),
        LARGE_AREA("Grandes areas 1000/2500/5000/10000", new double[]{500d, 1000d, 2500d, 5000d, 10000d, 25000d, 50000d, 100000d, 250000d});

        private final String label;
        private final double[] preferredValues;

        ScaleRule(String label, double[] preferredValues) {
            this.label = label;
            this.preferredValues = preferredValues;
        }

        double roundValue(double rawValue) {
            if (rawValue <= 0) {
                return 0;
            }
            if (preferredValues != null && preferredValues.length > 0) {
                double best = preferredValues[0];
                double bestDistance = Math.abs(preferredValues[0] - rawValue);
                for (double preferredValue : preferredValues) {
                    double distance = Math.abs(preferredValue - rawValue);
                    if (distance < bestDistance) {
                        best = preferredValue;
                        bestDistance = distance;
                    }
                }
                return best;
            }

            double exponent = Math.pow(10, Math.floor(Math.log10(rawValue)));
            double normalized = rawValue / exponent;
            double rounded;
            if (normalized < 1.5d) {
                rounded = 1d;
            } else if (normalized < 3.5d) {
                rounded = 2d;
            } else if (normalized < 7.5d) {
                rounded = 5d;
            } else {
                rounded = 10d;
            }
            return rounded * exponent;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum NorthStyle {
        SIMPLE("Simple"),
        CLASSIC("Clasico"),
        MODERN("Moderno"),
        ROSE("Rosa"),
        TECHNICAL("Tecnico");

        private final String label;

        NorthStyle(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private record LayoutSettings(String title,
                                  String subtitle,
                                  String footer,
                                  String studyName,
                                  String cartoucheProjectName,
                                  String companyName,
                                  String cartographerName,
                                  String imageSource,
                                  String coordinateReference,
                                  String legendTitle,
                                  String legendSubtitle,
                                  String logoPath,
                                  String layoutImagePath,
                                  LayoutTemplate template,
                                  PageSizePreset pageSize,
                                  PageOrientation orientation,
                                  int dpi,
                                  LegendPlacement legendPlacement,
                                  ScaleStyle scaleStyle,
                                  ScaleRule scaleRule,
                                  NorthStyle northStyle,
                                  boolean showNorth,
                                  boolean showScale,
                                  boolean showLegend,
                                  boolean showGrid,
                                  int gridColumns,
                                  int gridRows,
                                  boolean showGridLabels,
                                  List<CatmapLayoutItem> catmapItems) {
    }

    private record LayoutSnapshot(BufferedImage mapImage,
                                  List<Layer> visibleLayers,
                                  String projectName,
                                  String projectCrsLabel,
                                  String projectCrsCode,
                                  String scaleLabel,
                                  double representativeMeters,
                                  double baseViewMinX,
                                  double baseViewMinY,
                                  double baseZoomFactor,
                                  int basePixelWidth,
                                  int basePixelHeight) {
    }

    private record MapFrameGeometry(Rectangle frameBounds, Rectangle imageBounds, double shownGroundMeters) {
    }

    private record LegendItem(String key, String label, String subtitle, Layer layer, CategoryStyleRule categoryRule, String geometryType) {
    }

    private record LayoutRenderResult(BufferedImage image,
                                      EnumMap<LayoutElementType, Rectangle> elementBounds,
                                      java.util.Map<String, Rectangle> customItemBounds,
                                      double exactScaleDenominator) {
    }

    private record FooterRenderResult(Rectangle cartoucheBounds, Rectangle profileImageBounds) {
    }

    static class LayoutInteractionState {
        private LayoutTemplate template = LayoutTemplate.TECHNICAL_RIGHT;
        private PreviewScaleMode previewScaleMode = PreviewScaleMode.FIT_PAGE;
        private double customPreviewZoom = 1d;
        private double mapZoom = 1d;
        private double mapOffsetX = 0d;
        private double mapOffsetY = 0d;
        private MapFrameTool mapFrameTool = MapFrameTool.MOVE_FRAME;
        private LayoutElementType selectedElement = null;
        private String selectedCustomItemId = null;
        private final EnumMap<LayoutElementType, Point> elementOffsets = new EnumMap<>(LayoutElementType.class);
        private final EnumMap<LayoutElementType, Dimension> elementSizeAdjustments = new EnumMap<>(LayoutElementType.class);
        private final java.util.EnumSet<LayoutElementType> hiddenElements = java.util.EnumSet.noneOf(LayoutElementType.class);
        private final java.util.EnumSet<LayoutElementType> lockedElements = java.util.EnumSet.noneOf(LayoutElementType.class);

        LayoutTemplate getTemplate() {
            return template;
        }

        void setTemplate(LayoutTemplate template) {
            this.template = template != null ? template : LayoutTemplate.TECHNICAL_RIGHT;
        }

        void resetForTemplate(LayoutTemplate template) {
            setTemplate(template);
            previewScaleMode = PreviewScaleMode.FIT_PAGE;
            customPreviewZoom = 1d;
            resetMapView();
            elementOffsets.clear();
            elementSizeAdjustments.clear();
            hiddenElements.clear();
            lockedElements.clear();
            mapFrameTool = MapFrameTool.MOVE_FRAME;
            selectedElement = null;
            selectedCustomItemId = null;
        }

        void fitPage() {
            previewScaleMode = PreviewScaleMode.FIT_PAGE;
            customPreviewZoom = 1d;
        }

        void fitWidth() {
            previewScaleMode = PreviewScaleMode.FIT_WIDTH;
            customPreviewZoom = 1d;
        }

        void zoomPreview(double factor) {
            previewScaleMode = PreviewScaleMode.CUSTOM;
            customPreviewZoom = Math.max(0.35d, Math.min(12d, customPreviewZoom * factor));
        }

        double resolvePreviewScale(double fitPageScale, double fitWidthScale) {
            return switch (previewScaleMode) {
                case FIT_WIDTH -> Math.max(0.08d, fitWidthScale);
                case CUSTOM -> Math.max(0.08d, fitPageScale * customPreviewZoom);
                default -> Math.max(0.08d, fitPageScale);
            };
        }

        void select(LayoutElementType elementType) {
            selectedElement = elementType;
            if (elementType != LayoutElementType.CATMAP_ITEM) {
                selectedCustomItemId = null;
            }
        }

        void selectCustomItem(String itemId) {
            selectedElement = itemId != null && !itemId.isBlank() ? LayoutElementType.CATMAP_ITEM : null;
            selectedCustomItemId = itemId != null ? itemId.trim() : null;
        }

        LayoutElementType getSelectedElement() {
            return selectedElement;
        }

        String getSelectedCustomItemId() {
            return selectedCustomItemId;
        }

        void setMapFrameTool(MapFrameTool mapFrameTool) {
            this.mapFrameTool = mapFrameTool != null ? mapFrameTool : MapFrameTool.MOVE_FRAME;
        }

        boolean isMapFrameMoveToolActive() {
            return mapFrameTool == MapFrameTool.MOVE_FRAME;
        }

        boolean isMapFramePanToolActive() {
            return mapFrameTool == MapFrameTool.PAN;
        }

        boolean isMapFrameZoomToolActive() {
            return mapFrameTool == MapFrameTool.ZOOM;
        }

        Point getOffset(LayoutElementType elementType) {
            Point point = elementOffsets.get(elementType);
            return point != null ? point : new Point();
        }

        Dimension getSizeAdjustment(LayoutElementType elementType) {
            Dimension dimension = elementSizeAdjustments.get(elementType);
            return dimension != null ? dimension : new Dimension();
        }

        boolean isElementVisible(LayoutElementType elementType) {
            return elementType != null && !hiddenElements.contains(elementType);
        }

        void setElementVisible(LayoutElementType elementType, boolean visible) {
            if (!isFixedLayoutElement(elementType)) {
                return;
            }
            if (visible) {
                hiddenElements.remove(elementType);
            } else {
                hiddenElements.add(elementType);
            }
        }

        boolean isElementLocked(LayoutElementType elementType) {
            return elementType != null && lockedElements.contains(elementType);
        }

        void setElementLocked(LayoutElementType elementType, boolean locked) {
            if (!isFixedLayoutElement(elementType)) {
                return;
            }
            if (locked) {
                lockedElements.add(elementType);
            } else {
                lockedElements.remove(elementType);
            }
        }

        void restoreDefaultElementControls() {
            hiddenElements.clear();
            lockedElements.clear();
            elementOffsets.clear();
            elementSizeAdjustments.clear();
            resetMapView();
        }

        void translate(LayoutElementType elementType, int dx, int dy) {
            if (elementType == null) {
                return;
            }
            Point offset = elementOffsets.computeIfAbsent(elementType, key -> new Point());
            offset.translate(dx, dy);
        }

        void resize(LayoutElementType elementType,
                    ResizeHandle handle,
                    int dx,
                    int dy,
                    int currentWidth,
                    int currentHeight,
                    int minWidth,
                    int minHeight) {
            if (elementType == null || handle == null || handle == ResizeHandle.NONE) {
                return;
            }
            Point offset = elementOffsets.computeIfAbsent(elementType, key -> new Point());
            Dimension size = elementSizeAdjustments.computeIfAbsent(elementType, key -> new Dimension());
            int actualWidth = currentWidth;
            int actualHeight = currentHeight;
            int baseWidth = actualWidth - size.width;
            int baseHeight = actualHeight - size.height;

            switch (handle) {
                case EAST -> {
                    int targetWidth = Math.max(minWidth, actualWidth + dx);
                    size.width = targetWidth - baseWidth;
                }
                case SOUTH -> {
                    int targetHeight = Math.max(minHeight, actualHeight + dy);
                    size.height = targetHeight - baseHeight;
                }
                case SOUTH_EAST -> {
                    int targetWidth = Math.max(minWidth, actualWidth + dx);
                    int targetHeight = Math.max(minHeight, actualHeight + dy);
                    size.width = targetWidth - baseWidth;
                    size.height = targetHeight - baseHeight;
                }
                case WEST -> {
                    int targetWidth = Math.max(minWidth, actualWidth - dx);
                    int shift = actualWidth - targetWidth;
                    offset.translate(shift, 0);
                    size.width = targetWidth - baseWidth;
                }
                case NORTH -> {
                    int targetHeight = Math.max(minHeight, actualHeight - dy);
                    int shift = actualHeight - targetHeight;
                    offset.translate(0, shift);
                    size.height = targetHeight - baseHeight;
                }
                case NORTH_WEST -> {
                    int targetWidth = Math.max(minWidth, actualWidth - dx);
                    int targetHeight = Math.max(minHeight, actualHeight - dy);
                    offset.translate(actualWidth - targetWidth, actualHeight - targetHeight);
                    size.width = targetWidth - baseWidth;
                    size.height = targetHeight - baseHeight;
                }
                case NORTH_EAST -> {
                    int targetWidth = Math.max(minWidth, actualWidth + dx);
                    int targetHeight = Math.max(minHeight, actualHeight - dy);
                    offset.translate(0, actualHeight - targetHeight);
                    size.width = targetWidth - baseWidth;
                    size.height = targetHeight - baseHeight;
                }
                case SOUTH_WEST -> {
                    int targetWidth = Math.max(minWidth, actualWidth - dx);
                    int targetHeight = Math.max(minHeight, actualHeight + dy);
                    offset.translate(actualWidth - targetWidth, 0);
                    size.width = targetWidth - baseWidth;
                    size.height = targetHeight - baseHeight;
                }
                default -> {
                }
            }
        }

        boolean hasCustomSize(LayoutElementType elementType) {
            Dimension size = elementSizeAdjustments.get(elementType);
            return size != null && (size.width != 0 || size.height != 0);
        }

        void zoomMap(double factor) {
            if (factor <= 0 || Double.isNaN(factor) || Double.isInfinite(factor)) {
                return;
            }
            setMapZoom(mapZoom * factor);
        }

        void setMapZoom(double zoom) {
            if (zoom <= 0 || Double.isNaN(zoom) || Double.isInfinite(zoom)) {
                return;
            }
            mapZoom = Math.max(0.02d, Math.min(250d, zoom));
        }

        void panMap(double dx, double dy) {
            mapOffsetX += dx;
            mapOffsetY += dy;
        }

        void resetMapView() {
            mapZoom = 1d;
            mapOffsetX = 0d;
            mapOffsetY = 0d;
        }

        double getMapZoom() {
            return mapZoom;
        }

        double getMapOffsetX() {
            return mapOffsetX;
        }

        double getMapOffsetY() {
            return mapOffsetY;
        }
    }

    private class LayoutPreviewPanel extends JPanel {
        private LayoutRenderResult lastRenderResult;
        private Rectangle lastPageBounds = new Rectangle();
        private double lastPreviewScale = 1d;
        private Point lastDragPagePoint = null;
        private ResizeHandle activeResizeHandle = ResizeHandle.NONE;
        private LayoutElementType activeResizeElement = null;
        private String activeResizeCustomItemId = null;
        private final List<Integer> activeGuideXs = new ArrayList<>();
        private final List<Integer> activeGuideYs = new ArrayList<>();
        private final List<GuideLine> guides = new ArrayList<>();
        private GuideLine draggingGuide = null;
        private double draggingGuideStartMm = 0;
        private LayoutElement hoveredElement = null;
        private String drawingShape = null;        // "rect", "ellipse", "line" when drawing mode active
        private Point drawingStart = null;          // page-pixel start of the shape
        boolean snapToGrid = true;                   // controlled by UI toggle
        boolean snapToElements = true;               // controlled by UI toggle
        private final JTextField inlineTitleEditor;
        private final JPanel inlineCartoucheEditor;
        private final JTextField inlineCartoucheStudyField;
        private final JTextField inlineCartoucheProjectField;
        private final JTextField inlineCartoucheCompanyField;
        private final JTextField inlineCartoucheCartographerField;
        private final JTextField inlineCartoucheSourceField;
        private final JTextField inlineCartoucheCrsField;

        private LayoutPreviewPanel() {
            setLayout(null);
            setOpaque(true);
            setFocusable(true);
            setBackground(new Color(155, 160, 170)); // ArcMap-style dark canvas
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 224, 230)),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));
            inlineTitleEditor = new JTextField();
            inlineTitleEditor.setVisible(false);
            inlineTitleEditor.addActionListener(e -> commitInlineTitleEdit());
            inlineTitleEditor.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    commitInlineTitleEdit();
                }
            });
            add(inlineTitleEditor);
            inlineCartoucheStudyField = new JTextField();
            inlineCartoucheProjectField = new JTextField();
            inlineCartoucheCompanyField = new JTextField();
            inlineCartoucheCartographerField = new JTextField();
            inlineCartoucheSourceField = new JTextField();
            inlineCartoucheCrsField = new JTextField();
            inlineCartoucheEditor = buildInlineCartoucheEditor();
            inlineCartoucheEditor.setVisible(false);
            add(inlineCartoucheEditor);
            installInteraction();
        }

        @Override
        public Dimension getPreferredSize() {
            LayoutSettings settings = buildSettings();
            if (settings == null) {
                return new Dimension(980, 760);
            }
            Dimension pageSize = settings.pageSize().pixelSize(settings.orientation(), PREVIEW_RENDER_DPI);
            Dimension viewportSize = resolvePreviewViewportSize();
            int availableWidth = Math.max(80, viewportSize.width - 40);
            int availableHeight = Math.max(80, viewportSize.height - 40);
            double fitPageScale = Math.min(availableWidth / (double) pageSize.width, availableHeight / (double) pageSize.height);
            double fitWidthScale = availableWidth / (double) pageSize.width;
            double scale = Math.max(0.08d, interactionState.resolvePreviewScale(fitPageScale, fitWidthScale));
            int drawWidth = (int) Math.round(pageSize.width * scale);
            int drawHeight = (int) Math.round(pageSize.height * scale);
            return new Dimension(
                    Math.max(viewportSize.width, drawWidth + 80),
                    Math.max(viewportSize.height, drawHeight + 80)
            );
        }

        private Dimension resolvePreviewViewportSize() {
            if (previewScrollPane != null) {
                Dimension extent = previewScrollPane.getViewport().getExtentSize();
                if (extent != null && extent.width > 0 && extent.height > 0) {
                    return extent;
                }
            }
            if (getParent() instanceof javax.swing.JViewport viewport) {
                Dimension extent = viewport.getExtentSize();
                if (extent != null && extent.width > 0 && extent.height > 0) {
                    return extent;
                }
            }
            if (getWidth() > 0 && getHeight() > 0) {
                return new Dimension(getWidth(), getHeight());
            }
            return new Dimension(980, 760);
        }

        private JPanel buildInlineCartoucheEditor() {
            JPanel editor = new JPanel(new GridBagLayout());
            editor.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(37, 99, 235), 2),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            editor.setBackground(new Color(248, 250, 252));
            editor.setOpaque(true);

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(2, 4, 2, 4);
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;
            addInlineCartoucheField(editor, gc, 0, "Estudio", inlineCartoucheStudyField);
            addInlineCartoucheField(editor, gc, 1, "Proyecto", inlineCartoucheProjectField);
            addInlineCartoucheField(editor, gc, 2, "Empresa", inlineCartoucheCompanyField);
            addInlineCartoucheField(editor, gc, 3, "Cartografo", inlineCartoucheCartographerField);
            addInlineCartoucheField(editor, gc, 4, "Fuente", inlineCartoucheSourceField);
            addInlineCartoucheField(editor, gc, 5, "Coord.", inlineCartoucheCrsField);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            buttons.setOpaque(false);
            JButton apply = new JButton("Aplicar");
            apply.addActionListener(e -> commitInlineCartoucheEdit());
            JButton cancel = new JButton("Cancelar");
            cancel.addActionListener(e -> cancelInlineCartoucheEdit());
            buttons.add(apply);
            buttons.add(cancel);

            gc.gridx = 0;
            gc.gridy = 6;
            gc.gridwidth = 2;
            gc.weightx = 1;
            editor.add(buttons, gc);
            return editor;
        }

        private void addInlineCartoucheField(JPanel editor, GridBagConstraints gc, int row, String label, JTextField field) {
            JLabel fieldLabel = new JLabel(label + ":");
            fieldLabel.setFont(fieldLabel.getFont().deriveFont(Font.BOLD, 11f));
            gc.gridx = 0;
            gc.gridy = row;
            gc.gridwidth = 1;
            gc.weightx = 0;
            editor.add(fieldLabel, gc);

            gc.gridx = 1;
            gc.weightx = 1;
            field.setColumns(22);
            editor.add(field, gc);
        }

        private void installInteraction() {
            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        // Check if double-click on a LayoutMap -> activate map content editing
                        Point pp = toPagePoint(e.getPoint());
                        RectMm pr = toPageRectMm();
                        if (pp != null && pr != null && layoutModel.size() > 0) {
                    double xMm = pr.xMm + pp.x * pr.pxToMmScale;
                    double yMm = pr.yMm + pp.y * pr.pxToMmScale;
                            LayoutElement el = layoutModel.findTopmostElementAtMm(xMm, yMm);
                            if (el instanceof LayoutMap) {
                                layoutModel.clearSelection();
                                el.setSelected(true);
                                mapPanToolButton.doClick();
                                statusLabel.setText("Editando contenido del mapa: \"" + el.getName() + "\". Esc para salir.");
                                return;
                            }
                        }
                        Point pagePoint = toPagePoint(e.getPoint());
                        if (pagePoint != null && isInsideElement(LayoutElementType.HEADER, pagePoint)) {
                            beginInlineTitleEdit();
                        } else if (pagePoint != null && isInsideElement(LayoutElementType.CARTOUCHE, pagePoint)) {
                            beginInlineCartoucheEdit();
                        } else if (pagePoint != null && isInsideElement(LayoutElementType.LEGEND, pagePoint)) {
                            openLegendEditor();
                        } else if (pagePoint != null && isInsideElement(LayoutElementType.NORTH, pagePoint)) {
                            configureNorthFromToolbar();
                        } else if (pagePoint != null && findCustomItemIdAt(pagePoint) != null) {
                            selectCatmapItemInList(findCustomItemIdAt(pagePoint));
                            editSelectedCatmapItem();
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    if (SwingUtilities.isRightMouseButton(e)) { handleRightClick(e); return; }
                    if (!SwingUtilities.isLeftMouseButton(e)) return;

                    // Drawing mode: start drawing
                    if (drawingShape != null) {
                        drawingStart = toPagePoint(e.getPoint());
                        return;
                    }

                    // Guide interaction: drag from ruler or existing guide
                    GuideLine.Orientation rulerHit = RulerRenderer.rulerHitTest(e.getX(), e.getY(), 0, 0, getWidth(), getHeight());
                    if (rulerHit != null) {
                        LayoutSettings settings = buildSettings();
                        double pxPerMm = PREVIEW_RENDER_DPI / 25.4 * lastPreviewScale;
                        double mm = rulerHit == GuideLine.Orientation.VERTICAL
                                ? (e.getX() - lastPageBounds.x) / pxPerMm
                                : (e.getY() - lastPageBounds.y) / pxPerMm;
                        if (mm >= 0) {
                            GuideLine newGuide = new GuideLine("guide-" + System.currentTimeMillis(), mm, rulerHit);
                            guides.add(newGuide);
                            draggingGuide = newGuide;
                            draggingGuideStartMm = mm;
                            repaint();
                            return;
                        }
                    }
                    // Check for existing guide hit
                    {
                        double pxPerMm = PREVIEW_RENDER_DPI / 25.4 * lastPreviewScale;
                        for (GuideLine guide : guides) {
                            if (guide.containsPx(e.getX(), e.getY(), lastPageBounds.x, lastPageBounds.y, lastPageBounds.width, lastPageBounds.height, PREVIEW_RENDER_DPI, lastPreviewScale, 6)) {
                                if (SwingUtilities.isRightMouseButton(e)) {
                                    guides.remove(guide);
                                    repaint();
                                    return;
                                }
                                draggingGuide = guide;
                                draggingGuideStartMm = guide.mmPos;
                                return;
                            }
                        }
                    }

                    Point pagePoint = toPagePoint(e.getPoint());
                    RectMm pageRect = toPageRectMm();
                    if (pagePoint != null && pageRect != null) {
                    double xMm = pageRect.xMm + pagePoint.x * pageRect.pxToMmScale;
                    double yMm = pageRect.yMm + pagePoint.y * pageRect.pxToMmScale;
                        LayoutElement clicked = layoutModel.findElementAtMm(xMm, yMm);
                        if (clicked != null) {
                            // Only allow resize if element is already selected; otherwise select+move first
                            if (clicked.isSelected() && !clicked.isLocked()) {
                                int handleIdx = hitTestHandle(clicked, pagePoint, pageRect);
                                if (handleIdx >= 0) {
                                    pushUndo(clicked, false);
                                    activeResizeHandleIndex = handleIdx;
                                    draggingLayoutElement = clicked;
                                    dragStartPagePoint = pagePoint;
                                    dragStartBoundsMm = new java.awt.geom.Rectangle2D.Double(
                                        clicked.getBoundsMm().x, clicked.getBoundsMm().y,
                                        clicked.getBoundsMm().width, clicked.getBoundsMm().height);
                                    return;
                                }
                            }
                            if (!clicked.isLocked()) {
                                pushUndo(clicked, false);
                                layoutModel.clearSelection();
                                clicked.setSelected(true);
                                draggingLayoutElement = clicked;
                                dragStartPagePoint = pagePoint;
                                dragStartBoundsMm = new java.awt.geom.Rectangle2D.Double(
                                    clicked.getBoundsMm().x, clicked.getBoundsMm().y,
                                    clicked.getBoundsMm().width, clicked.getBoundsMm().height);
                                activeResizeHandleIndex = -1;
                                refreshElementList();
                                repaint();
                                return;
                            }
                            layoutModel.clearSelection();
                            clicked.setSelected(true);
                            refreshElementList();
                            repaint();
                            return;
                        }
                        layoutModel.clearSelection();
                    }

                    if (interactionState.isMapFramePanToolActive() || interactionState.isMapFrameZoomToolActive()) {
                        activeResizeHandle = ResizeHandle.NONE;
                        activeResizeElement = null;
                        activeResizeCustomItemId = null;
                        if (pagePoint == null || !isInsideElement(LayoutElementType.MAP_CONTENT, pagePoint)) {
                            lastDragPagePoint = null;
                            setCursor(Cursor.getDefaultCursor());
                            statusLabel.setText(interactionState.isMapFramePanToolActive()
                                    ? "Pan mapa activo. Haz clic dentro del frame para desplazar solo el contenido interno."
                                    : "Lupa mapa activa. Usa la rueda dentro del frame para cambiar el zoom interno.");
                            repaint();
                            return;
                        }
                        interactionState.select(LayoutElementType.MAP_CONTENT);
                        selectCatmapItemInList(null);
                        syncLayoutStructureSelection();
                        lastDragPagePoint = interactionState.isMapFramePanToolActive() ? pagePoint : null;
                        setCursor(interactionState.isMapFramePanToolActive()
                                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                                : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                        statusLabel.setText(interactionState.isMapFramePanToolActive()
                                ? "Pan mapa activo. Arrastra para mover solo el contenido interno."
                                : "Lupa mapa activa. Usa la rueda para acercar o alejar el contenido interno.");
                        repaint();
                        return;
                    }
                    if (pagePoint == null || lastRenderResult == null) {
                        interactionState.select(null);
                        activeResizeHandle = ResizeHandle.NONE;
                        activeResizeElement = null;
                        activeResizeCustomItemId = null;
                        selectCatmapItemInList(null);
                        repaint();
                        return;
                    }
                    ResizeTarget resizeTarget = findResizeTarget(pagePoint);
                    if (resizeTarget != null) {
                        if (resizeTarget.customItemId() != null) {
                            interactionState.selectCustomItem(resizeTarget.customItemId());
                            selectCatmapItemInList(resizeTarget.customItemId());
                            CatmapLayoutItem item = getCatmapItemById(resizeTarget.customItemId());
                            if (item != null && item.isLocked()) {
                                lastDragPagePoint = null;
                                activeResizeHandle = ResizeHandle.NONE;
                                activeResizeElement = null;
                                activeResizeCustomItemId = null;
                                statusLabel.setText("Elemento CATMAP bloqueado. Liberalo desde el inspector para redimensionarlo.");
                                repaint();
                                return;
                            }
                        } else {
                            interactionState.select(resizeTarget.elementType());
                            selectCatmapItemInList(null);
                            if (interactionState.isElementLocked(resizeTarget.elementType())) {
                                lastDragPagePoint = null;
                                activeResizeHandle = ResizeHandle.NONE;
                                activeResizeElement = null;
                                activeResizeCustomItemId = null;
                                statusLabel.setText(layoutElementLabel(resizeTarget.elementType()) + " bloqueado. Liberalo desde la estructura para redimensionarlo.");
                                repaint();
                                return;
                            }
                        }
                        activeResizeElement = resizeTarget.elementType();
                        activeResizeCustomItemId = resizeTarget.customItemId();
                        activeResizeHandle = resizeTarget.handle();
                        lastDragPagePoint = pagePoint;
                        setCursor(cursorForHandle(activeResizeHandle));
                        statusLabel.setText("Redimensionando " + elementLabel(activeResizeElement) + " con el mouse.");
                        repaint();
                        return;
                    }
                    LayoutElementType hit = findElementAt(pagePoint);
                    String customItemId = findCustomItemIdAt(pagePoint);
                    if (customItemId != null) {
                        interactionState.selectCustomItem(customItemId);
                        selectCatmapItemInList(customItemId);
                        CatmapLayoutItem item = getCatmapItemById(customItemId);
                        if (item != null && item.isLocked()) {
                            activeResizeHandle = ResizeHandle.NONE;
                            activeResizeElement = null;
                            activeResizeCustomItemId = null;
                            lastDragPagePoint = null;
                            setCursor(Cursor.getDefaultCursor());
                            statusLabel.setText("Elemento CATMAP bloqueado. Podés editarlo desde el inspector, pero no moverlo.");
                            repaint();
                            return;
                        }
                    } else {
                        interactionState.select(hit);
                        selectCatmapItemInList(null);
                        if (interactionState.isElementLocked(hit)) {
                            activeResizeHandle = ResizeHandle.NONE;
                            activeResizeElement = null;
                            activeResizeCustomItemId = null;
                            lastDragPagePoint = null;
                            setCursor(Cursor.getDefaultCursor());
                            statusLabel.setText(layoutElementLabel(hit) + " bloqueado. Podés seleccionarlo, pero no moverlo.");
                            repaint();
                            return;
                        }
                    }
                    activeResizeHandle = ResizeHandle.NONE;
                    activeResizeElement = null;
                    activeResizeCustomItemId = null;
                    lastDragPagePoint = hit != null ? pagePoint : null;
                    boolean selected = hit != null || customItemId != null;
                    lastDragPagePoint = selected ? pagePoint : null;
                    setCursor(resolveWorkCursor(pagePoint, selected));
                    statusLabel.setText(resolveSelectionStatus(hit, customItemId, selected));
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (drawingShape != null) {
                        if (drawingStart != null) {
                            Point end = toPagePoint(e.getPoint());
                            if (end != null) finishDrawing(end);
                            else cancelDrawing();
                        } else {
                            cancelDrawing();
                        }
                        return;
                    }
                    if (draggingGuide != null) {
                        draggingGuide = null;
                        setCursor(Cursor.getDefaultCursor());
                        repaint();
                        return;
                    }
                    if (e.isPopupTrigger()) {
                        handleRightClick(e);
                        return;
                    }
                    if (draggingLayoutElement != null) {
                        draggingLayoutElement = null;
                        dragStartPagePoint = null;
                        dragStartBoundsMm = null;
                        setCursor(Cursor.getDefaultCursor());
                        repaint();
                    }
                    lastDragPagePoint = null;
                    activeResizeHandle = ResizeHandle.NONE;
                    activeResizeElement = null;
                    activeResizeCustomItemId = null;
                    clearSnapGuides();
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (drawingShape != null && drawingStart != null) {
                        repaint(); // will draw preview rectangle in paintComponent
                        return;
                    }
                    if (draggingGuide != null) {
                        LayoutSettings settings = buildSettings();
                        double mmPerPx = 25.4 / PREVIEW_RENDER_DPI / lastPreviewScale;
                        if (draggingGuide.orientation == GuideLine.Orientation.VERTICAL) {
                            double delta = (e.getX() - lastPageBounds.x) - (e.getX() - lastPageBounds.x);
                            double newMm = (e.getX() - lastPageBounds.x) * mmPerPx;
                            newMm = Math.max(0, Math.min(newMm, settings.pageSize().widthMm));
                            draggingGuide.mmPos = newMm;
                        } else {
                            double newMm = (e.getY() - lastPageBounds.y) * mmPerPx;
                            newMm = Math.max(0, Math.min(newMm, settings.pageSize().heightMm));
                            draggingGuide.mmPos = newMm;
                        }
                        repaint();
                        return;
                    }
                    if (draggingLayoutElement != null && dragStartPagePoint != null && dragStartBoundsMm != null) {
                        Point p = toPagePoint(e.getPoint());
                        if (p != null) {
                            RectMm r = toPageRectMm();
                            if (r != null) {
                                double sc = r.pxToMmScale;
                                if (activeResizeHandleIndex >= 0) {
                                    resizeElement(activeResizeHandleIndex, p.x - dragStartPagePoint.x, p.y - dragStartPagePoint.y);
                } else {
                    double newX = dragStartBoundsMm.x + (p.x - dragStartPagePoint.x) * sc;
                    double newY = dragStartBoundsMm.y + (p.y - dragStartPagePoint.y) * sc;
                    // Smart snap to other element edges (conditional)
                    if (snapToElements) {
                    double snapTol = 2.0;
                    for (LayoutElement other : layoutModel.getElements()) {
                        if (other == draggingLayoutElement || !other.isVisible()) continue;
                        double ox = other.getBoundsMm().x, oy = other.getBoundsMm().y;
                        double ow = other.getBoundsMm().width, oh = other.getBoundsMm().height;
                        double ex = newX, ey = newY, ew = dragStartBoundsMm.width, eh = dragStartBoundsMm.height;
                        if (Math.abs(ex - ox) < snapTol) newX = ox;
                        if (Math.abs(ex + ew - ox - ow) < snapTol) newX = ox + ow - ew;
                        if (Math.abs(ex + ew/2 - ox - ow/2) < snapTol) newX = ox + ow/2 - ew/2;
                        if (Math.abs(ey - oy) < snapTol) newY = oy;
                        if (Math.abs(ey + eh - oy - oh) < snapTol) newY = oy + oh - eh;
                        if (Math.abs(ey + eh/2 - oy - oh/2) < snapTol) newY = oy + oh/2 - eh/2;
                    }
                    }
                    // Snap to grid (5mm, conditional)
                    if (snapToGrid) {
                    double gridSize = 5.0;
                    newX = Math.round(newX / gridSize) * gridSize;
                    newY = Math.round(newY / gridSize) * gridSize;
                    }
                    draggingLayoutElement.setBoundsMm(newX, newY, dragStartBoundsMm.width, dragStartBoundsMm.height);
                                }
                            }
                        }
                        repaint();
                        return;
                    }
                    if (lastDragPagePoint == null || interactionState.getSelectedElement() == null) {
                        return;
                    }
                    if (interactionState.isMapFrameZoomToolActive()) {
                        return;
                    }
                    Point pagePoint = toPagePoint(e.getPoint());
                    if (pagePoint == null) {
                        return;
                    }
                    int dx = pagePoint.x - lastDragPagePoint.x;
                    int dy = pagePoint.y - lastDragPagePoint.y;
                    if (dx == 0 && dy == 0) {
                        return;
                    }
                    if (activeResizeElement != null && activeResizeHandle != ResizeHandle.NONE) {
                        Rectangle currentBounds = activeResizeElement == LayoutElementType.CATMAP_ITEM
                                ? (lastRenderResult != null ? lastRenderResult.customItemBounds().get(activeResizeCustomItemId) : null)
                                : (lastRenderResult != null ? lastRenderResult.elementBounds().get(activeResizeElement) : null);
                        if (currentBounds != null) {
                            if (activeResizeElement == LayoutElementType.CATMAP_ITEM && activeResizeCustomItemId != null) {
                                resizeCatmapItem(activeResizeCustomItemId, activeResizeHandle, dx, dy, currentBounds);
                            } else {
                                interactionState.resize(
                                        activeResizeElement,
                                        activeResizeHandle,
                                        dx,
                                        dy,
                                        currentBounds.width,
                                        currentBounds.height,
                                        activeResizeElement == LayoutElementType.MAP_CONTENT ? 260 : 160,
                                        activeResizeElement == LayoutElementType.MAP_CONTENT ? 180 : 100
                                );
                            }
                        }
                    } else if (interactionState.getSelectedElement() == LayoutElementType.MAP_CONTENT && interactionState.isMapFrameMoveToolActive()) {
                        interactionState.translate(LayoutElementType.MAP_CONTENT, dx, dy);
                    } else if (interactionState.getSelectedElement() == LayoutElementType.MAP_CONTENT && interactionState.isMapFramePanToolActive()) {
                        interactionState.panMap(dx, dy);
                    } else if (interactionState.getSelectedElement() == LayoutElementType.CATMAP_ITEM) {
                        translateCatmapItem(interactionState.getSelectedCustomItemId(), dx, dy);
                    } else if (!interactionState.isElementLocked(interactionState.getSelectedElement())) {
                        interactionState.translate(interactionState.getSelectedElement(), dx, dy);
                    }
                    lastDragPagePoint = pagePoint;
                    repaint();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    Point pagePoint = toPagePoint(e.getPoint());
                    RectMm pageRect = toPageRectMm();
                    LayoutElement oldHover = hoveredElement;
                    hoveredElement = null;
                    if (pagePoint != null && pageRect != null && layoutModel.size() > 0) {
                        double xMm = pageRect.xMm + pagePoint.x * pageRect.pxToMmScale;
                        double yMm = pageRect.yMm + pagePoint.y * pageRect.pxToMmScale;
                        hoveredElement = layoutModel.findHoverAtMm(xMm, yMm);
                        if (hoveredElement != null) {
                            if (hoveredElement.isLocked()) {
                                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            } else {
                                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            }
                            setToolTipText(hoveredElement.getName());
                        } else {
                            setCursor(Cursor.getDefaultCursor());
                            setToolTipText(null);
                        }
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                        setToolTipText(null);
                    }
                    if (oldHover != hoveredElement) repaint();
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (lastRenderResult == null) {
                        return;
                    }
                    Point pagePoint = toPagePoint(e.getPoint());
                    boolean overMap = pagePoint != null && isInsideElement(LayoutElementType.MAP_CONTENT, pagePoint);
                    double factor = e.getWheelRotation() < 0 ? 1.12d : (1d / 1.12d);
                    if (overMap && interactionState.isMapFrameZoomToolActive()) {
                        interactionState.zoomMap(factor);
                        statusLabel.setText("Zoom del mapa dentro del layout: " + Math.round(interactionState.getMapZoom() * 100d) + "%");
                    } else {
                        interactionState.zoomPreview(factor);
                        statusLabel.setText("Zoom del compositor actualizado para trabajar la maquetacion.");
                    }
                    revalidate();
                    repaint();
                }
            };
            addMouseListener(adapter);
            addMouseMotionListener(adapter);
            addMouseWheelListener(adapter);
        }

        private void selectElementForPopup(Point panelPoint) {
            Point pagePoint = toPagePoint(panelPoint);
            if (pagePoint == null || lastRenderResult == null) {
                return;
            }
            String customItemId = findCustomItemIdAt(pagePoint);
            if (customItemId != null) {
                interactionState.selectCustomItem(customItemId);
                selectCatmapItemInList(customItemId);
                syncLayoutStructureSelection();
                repaint();
                return;
            }
            LayoutElementType hit = findElementAt(pagePoint);
            if (hit != null) {
                interactionState.select(hit);
                selectCatmapItemInList(null);
                syncLayoutStructureSelection();
                repaint();
            }
        }

        private void beginInlineTitleEdit() {
            if (lastRenderResult == null) {
                return;
            }
            Rectangle headerBounds = lastRenderResult.elementBounds().get(LayoutElementType.HEADER);
            if (headerBounds == null) {
                return;
            }
            int editorX = lastPageBounds.x + (int) Math.round((headerBounds.x + 2) * lastPreviewScale);
            int editorY = lastPageBounds.y + (int) Math.round((headerBounds.y + 6) * lastPreviewScale);
            int editorWidth = Math.max(180, (int) Math.round(Math.min(headerBounds.width * 0.72d, 460) * lastPreviewScale));
            int editorHeight = Math.max(28, (int) Math.round(34 * lastPreviewScale));
            inlineTitleEditor.setText(titleField.getText());
            inlineTitleEditor.setBounds(editorX, editorY, editorWidth, editorHeight);
            inlineTitleEditor.setVisible(true);
            inlineTitleEditor.requestFocusInWindow();
            inlineTitleEditor.selectAll();
        }

        private void commitInlineTitleEdit() {
            if (!inlineTitleEditor.isVisible()) {
                return;
            }
            String updated = inlineTitleEditor.getText() != null ? inlineTitleEditor.getText().trim() : "";
            if (!updated.isBlank()) {
                titleField.setText(updated);
            }
            inlineTitleEditor.setVisible(false);
            repaint();
        }

        private void beginInlineCartoucheEdit() {
            if (lastRenderResult == null) {
                return;
            }
            Rectangle cartoucheBounds = lastRenderResult.elementBounds().get(LayoutElementType.CARTOUCHE);
            if (cartoucheBounds == null) {
                return;
            }
            inlineCartoucheStudyField.setText(studyField.getText());
            inlineCartoucheProjectField.setText(cartoucheProjectField.getText());
            inlineCartoucheCompanyField.setText(companyField.getText());
            inlineCartoucheCartographerField.setText(cartographerField.getText());
            inlineCartoucheSourceField.setText(imageSourceField.getText());
            inlineCartoucheCrsField.setText(coordinateReferenceField.getText());
            placeInlineCartoucheEditor(cartoucheBounds);
            inlineCartoucheEditor.setVisible(true);
            inlineCartoucheEditor.requestFocusInWindow();
            inlineCartoucheStudyField.requestFocusInWindow();
            inlineCartoucheStudyField.selectAll();
            statusLabel.setText("Editando cartucho directamente sobre el layout. Aplicar confirma los cambios.");
        }

        private void placeInlineCartoucheEditor(Rectangle cartoucheBounds) {
            int editorX = lastPageBounds.x + (int) Math.round((cartoucheBounds.x + 4) * lastPreviewScale);
            int editorY = lastPageBounds.y + (int) Math.round((cartoucheBounds.y + 4) * lastPreviewScale);
            int editorWidth = Math.max(300, (int) Math.round(Math.max(cartoucheBounds.width - 8, 420) * lastPreviewScale));
            int editorHeight = Math.max(190, inlineCartoucheEditor.getPreferredSize().height);
            editorWidth = Math.min(editorWidth, Math.max(320, getWidth() - editorX - 24));
            editorHeight = Math.min(editorHeight, Math.max(170, getHeight() - editorY - 24));
            inlineCartoucheEditor.setBounds(editorX, editorY, editorWidth, editorHeight);
        }

        private void commitInlineCartoucheEdit() {
            if (!inlineCartoucheEditor.isVisible()) {
                return;
            }
            studyField.setText(safeTrim(inlineCartoucheStudyField.getText()));
            cartoucheProjectField.setText(safeTrim(inlineCartoucheProjectField.getText()));
            companyField.setText(safeTrim(inlineCartoucheCompanyField.getText()));
            cartographerField.setText(safeTrim(inlineCartoucheCartographerField.getText()));
            imageSourceField.setText(safeTrim(inlineCartoucheSourceField.getText()));
            coordinateReferenceField.setText(safeTrim(inlineCartoucheCrsField.getText()));
            inlineCartoucheEditor.setVisible(false);
            statusLabel.setText("Datos cartograficos actualizados desde el layout.");
            repaint();
        }

        private void cancelInlineCartoucheEdit() {
            inlineCartoucheEditor.setVisible(false);
            statusLabel.setText("Edicion directa del cartucho cancelada.");
            repaint();
        }

        private String elementLabel(LayoutElementType type) {
            return switch (type) {
                case HEADER -> "encabezado";
                case MAP_CONTENT -> "mapa";
                case LEGEND -> "leyenda";
                case NORTH -> "norte";
                case SCALE -> "escala";
                case CARTOUCHE -> "cartucho";
                case PROFILE_IMAGE -> I18n.t("imagen del perfil");
                case CATMAP_ITEM -> "elemento CATMAP";
            };
        }

        private Cursor resolveWorkCursor(Point pagePoint, boolean selected) {
            if (!selected) {
                return Cursor.getDefaultCursor();
            }
            boolean overMap = pagePoint != null && isInsideElement(LayoutElementType.MAP_CONTENT, pagePoint);
            if (overMap && interactionState.getSelectedElement() == LayoutElementType.MAP_CONTENT) {
                if (interactionState.isMapFrameZoomToolActive()) {
                    return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
                }
                if (interactionState.isMapFramePanToolActive()) {
                    return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                }
                if (interactionState.isMapFrameMoveToolActive()) {
                    return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
                }
            }
            return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        }

        private String resolveSelectionStatus(LayoutElementType hit, String customItemId, boolean selected) {
            if (!selected) {
                return "Haz clic sobre un elemento del layout para moverlo.";
            }
            if (customItemId != null) {
                return "Elemento CATMAP seleccionado. Arrastralo para reubicarlo o usa el inspector para editarlo.";
            }
            if (hit == LayoutElementType.MAP_CONTENT) {
                if (interactionState.isMapFrameMoveToolActive()) {
                    return "Mover layout activo. Arrastra el bloque completo del mapa sin cambiar el contenido interno.";
                }
                if (interactionState.isMapFramePanToolActive()) {
                    return "Pan mapa activo. Arrastra para mover solo el contenido interno.";
                }
                if (interactionState.isMapFrameZoomToolActive()) {
                    return "Lupa de mapa activa. Usa la rueda para cambiar el zoom interno sin mover el bloque.";
                }
            }
            return "Elemento seleccionado: " + elementLabel(hit) + ". Arrastra con el mouse para reubicar.";
        }

        private LayoutElementType findElementAt(Point pagePoint) {
            if (findCustomItemIdAt(pagePoint) != null) {
                return LayoutElementType.CATMAP_ITEM;
            }
            LayoutElementType[] order = {
                    LayoutElementType.PROFILE_IMAGE,
                    LayoutElementType.LEGEND,
                    LayoutElementType.NORTH,
                    LayoutElementType.SCALE,
                    LayoutElementType.CARTOUCHE,
                    LayoutElementType.HEADER,
                    LayoutElementType.MAP_CONTENT
            };
            for (LayoutElementType type : order) {
                if (isInsideElement(type, pagePoint)) {
                    return type;
                }
            }
            return null;
        }

        private ResizeTarget findResizeTarget(Point pagePoint) {
            if (lastRenderResult != null) {
                java.util.List<String> ids = new ArrayList<>(lastRenderResult.customItemBounds().keySet());
                for (int i = ids.size() - 1; i >= 0; i--) {
                    String id = ids.get(i);
                    Rectangle bounds = lastRenderResult.customItemBounds().get(id);
                    ResizeHandle handle = bounds != null ? resolveResizeHandle(bounds, pagePoint) : ResizeHandle.NONE;
                    if (handle != ResizeHandle.NONE) {
                        return new ResizeTarget(LayoutElementType.CATMAP_ITEM, handle, id);
                    }
                }
            }
            for (LayoutElementType type : new LayoutElementType[]{
                    LayoutElementType.PROFILE_IMAGE,
                    LayoutElementType.LEGEND,
                    LayoutElementType.NORTH,
                    LayoutElementType.SCALE,
                    LayoutElementType.CARTOUCHE,
                    LayoutElementType.HEADER,
                    LayoutElementType.MAP_CONTENT
            }) {
                if (type == LayoutElementType.MAP_CONTENT && !interactionState.isMapFrameMoveToolActive()) {
                    continue;
                }
                if (interactionState.isElementLocked(type)) {
                    continue;
                }
                Rectangle bounds = lastRenderResult != null ? lastRenderResult.elementBounds().get(type) : null;
                ResizeHandle handle = bounds != null ? resolveResizeHandle(bounds, pagePoint) : ResizeHandle.NONE;
                if (handle != ResizeHandle.NONE) {
                    return new ResizeTarget(type, handle, null);
                }
            }
            return null;
        }

        private ResizeHandle resolveResizeHandle(Rectangle bounds, Point point) {
            int tolerance = Math.max(6, (int) Math.round(8d / Math.max(0.4d, lastPreviewScale)));
            boolean left = Math.abs(point.x - bounds.x) <= tolerance;
            boolean right = Math.abs(point.x - (bounds.x + bounds.width)) <= tolerance;
            boolean top = Math.abs(point.y - bounds.y) <= tolerance;
            boolean bottom = Math.abs(point.y - (bounds.y + bounds.height)) <= tolerance;
            boolean insideY = point.y >= bounds.y - tolerance && point.y <= bounds.y + bounds.height + tolerance;
            boolean insideX = point.x >= bounds.x - tolerance && point.x <= bounds.x + bounds.width + tolerance;

            if (left && top) return ResizeHandle.NORTH_WEST;
            if (right && top) return ResizeHandle.NORTH_EAST;
            if (left && bottom) return ResizeHandle.SOUTH_WEST;
            if (right && bottom) return ResizeHandle.SOUTH_EAST;
            if (left && insideY) return ResizeHandle.WEST;
            if (right && insideY) return ResizeHandle.EAST;
            if (top && insideX) return ResizeHandle.NORTH;
            if (bottom && insideX) return ResizeHandle.SOUTH;
            return ResizeHandle.NONE;
        }

        private Cursor cursorForHandle(ResizeHandle handle) {
            return switch (handle) {
                case NORTH -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                case SOUTH -> Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                case EAST -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                case WEST -> Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                case NORTH_EAST -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                case NORTH_WEST -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                case SOUTH_EAST -> Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                case SOUTH_WEST -> Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                default -> Cursor.getDefaultCursor();
            };
        }

        private boolean isInsideElement(LayoutElementType type, Point pagePoint) {
            Rectangle bounds = type == LayoutElementType.CATMAP_ITEM && lastRenderResult != null
                    ? lastRenderResult.customItemBounds().get(interactionState.getSelectedCustomItemId())
                    : (lastRenderResult != null ? lastRenderResult.elementBounds().get(type) : null);
            return bounds != null && bounds.contains(pagePoint);
        }

        private String findCustomItemIdAt(Point pagePoint) {
            if (pagePoint == null || lastRenderResult == null) {
                return null;
            }
            java.util.List<String> ids = new ArrayList<>(lastRenderResult.customItemBounds().keySet());
            for (int i = ids.size() - 1; i >= 0; i--) {
                String id = ids.get(i);
                Rectangle bounds = lastRenderResult.customItemBounds().get(id);
                if (bounds != null && bounds.contains(pagePoint)) {
                    return id;
                }
            }
            return null;
        }

        private void translateCatmapItem(String itemId, int dx, int dy) {
            CatmapLayoutItem item = getCatmapItemById(itemId);
            if (item == null || item.isLocked() || !item.isVisible()) {
                return;
            }
            Rectangle proposed = new Rectangle(item.getX() + dx, item.getY() + dy, item.getWidth(), item.getHeight());
            SnapResult snapped = snapCatmapRectangle(proposed, itemId);
            applySnapResultToItem(item, snapped);
            persistCatmapItems();
        }

        private void resizeCatmapItem(String itemId, ResizeHandle handle, int dx, int dy, Rectangle currentBounds) {
            CatmapLayoutItem item = getCatmapItemById(itemId);
            if (item == null || item.isLocked() || !item.isVisible() || handle == null || handle == ResizeHandle.NONE) {
                return;
            }
            int x = item.getX();
            int y = item.getY();
            int width = Math.max(20, currentBounds.width);
            int height = Math.max(20, currentBounds.height);

            switch (handle) {
                case EAST -> width = Math.max(40, width + dx);
                case SOUTH -> height = Math.max(30, height + dy);
                case SOUTH_EAST -> {
                    width = Math.max(40, width + dx);
                    height = Math.max(30, height + dy);
                }
                case WEST -> {
                    int targetWidth = Math.max(40, width - dx);
                    x += width - targetWidth;
                    width = targetWidth;
                }
                case NORTH -> {
                    int targetHeight = Math.max(30, height - dy);
                    y += height - targetHeight;
                    height = targetHeight;
                }
                case NORTH_WEST -> {
                    int targetWidth = Math.max(40, width - dx);
                    int targetHeight = Math.max(30, height - dy);
                    x += width - targetWidth;
                    y += height - targetHeight;
                    width = targetWidth;
                    height = targetHeight;
                }
                case NORTH_EAST -> {
                    int targetWidth = Math.max(40, width + dx);
                    int targetHeight = Math.max(30, height - dy);
                    y += height - targetHeight;
                    width = targetWidth;
                    height = targetHeight;
                }
                case SOUTH_WEST -> {
                    int targetWidth = Math.max(40, width - dx);
                    int targetHeight = Math.max(30, height + dy);
                    x += width - targetWidth;
                    width = targetWidth;
                    height = targetHeight;
                }
                default -> {
                }
            }
            SnapResult snapped = snapCatmapRectangle(new Rectangle(x, y, width, height), itemId);
            applySnapResultToItem(item, snapped);
            persistCatmapItems();
        }

        private void applySnapResultToItem(CatmapLayoutItem item, SnapResult snapped) {
            if (item == null || snapped == null) {
                return;
            }
            Rectangle bounds = snapped.bounds();
            item.setX(bounds.x);
            item.setY(bounds.y);
            item.setWidth(bounds.width);
            item.setHeight(bounds.height);
            activeGuideXs.clear();
            activeGuideXs.addAll(snapped.guideXs());
            activeGuideYs.clear();
            activeGuideYs.addAll(snapped.guideYs());
        }

        private void drawPersistentGuides(Graphics2D g2, int pageX, int pageY, double scale, int drawWidth, int drawHeight, LayoutSettings settings) {
            if (guides.isEmpty() || lastRenderResult == null) return;
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(new Color(0x3388FF));
                copy.setStroke(new BasicStroke(1.0f));
                for (GuideLine guide : guides) {
                    guide.render(copy, pageX, pageY, drawWidth, drawHeight, PREVIEW_RENDER_DPI, scale);
                }
            } finally {
                copy.dispose();
            }
        }

        private void handleRightClick(MouseEvent e) {
            Point pagePoint = toPagePoint(e.getPoint());
            RectMm pageRect = toPageRectMm();
            LayoutElement rightClicked = null;
            if (pagePoint != null && pageRect != null && layoutModel.size() > 0) {
            double xMm = pageRect.xMm + pagePoint.x * pageRect.pxToMmScale;
            double yMm = pageRect.yMm + pagePoint.y * pageRect.pxToMmScale;
            rightClicked = layoutModel.findTopmostElementAtMm(xMm, yMm);
            }
            JPopupMenu menu = new JPopupMenu();
            if (rightClicked != null) {
                boolean selWas = rightClicked.isSelected();
                layoutModel.clearSelection();
                rightClicked.setSelected(true);
                refresh(null);
                final LayoutElement rce = rightClicked;
                menu.add(item("Propiedades", () -> openElementProperties(rce)));
                menu.addSeparator();
                menu.add(item("Traer al frente", () -> { layoutModel.moveToFront(rce); refresh(null); }));
                menu.add(item("Enviar atras", () -> { layoutModel.moveToBack(rce); refresh(null); }));
                menu.add(item("Subir", () -> { layoutModel.moveUp(rce); refresh(null); }));
                menu.add(item("Bajar", () -> { layoutModel.moveDown(rce); refresh(null); }));
                menu.addSeparator();
                menu.add(item(rce.isLocked() ? "Desbloquear" : "Bloquear", () -> { rce.setLocked(!rce.isLocked()); refresh(null); }));
                menu.add(item(rce.isVisible() ? "Ocultar" : "Mostrar", () -> { rce.setVisible(!rce.isVisible()); refresh(null); }));
                menu.addSeparator();
                menu.add(item("Duplicar", () -> {
                    LayoutElement dup = duplicateElement(rce);
                    if (dup != null) refresh(null);
                }));
                menu.add(item("Renombrar...", () -> {
                    String newName = JOptionPane.showInputDialog(LayoutPreviewPanel.this, "Nuevo nombre:", rce.getName());
                    if (newName != null && !newName.isBlank()) { rce.setName(newName.trim()); refresh(null); }
                }));
                menu.addSeparator();
                menu.add(item("Eliminar", () -> { layoutModel.removeElement(rce.getId()); refresh(null); }));
                if (rce instanceof LayoutLegend) {
                    menu.addSeparator();
                    menu.add(item("Actualizar desde capas", () -> {
                        populateLegendFromProject((LayoutLegend) rce);
                        refresh(null);
                    }));
                    menu.add(item("Excluir mapas base", () -> {
                        LayoutLegend leg = (LayoutLegend) rce;
                        leg.getItems().removeIf(item -> LayoutLegend.isBasemapName(item.displayName));
                        refresh(null);
                    }));
                }
                if (rce instanceof LayoutMap) {
                    menu.addSeparator();
                    menu.add(item("Actualizar desde vista actual", () -> refresh(null)));
                }
                if (!selWas) {
                    rightClicked = rce;
                }
            } else {
                menu.add(item("Pegar", () -> {})).setEnabled(false);
            }
            menu.show(LayoutPreviewPanel.this, e.getX(), e.getY());
        }

        private void openElementProperties(LayoutElement el) {
            if (el instanceof LayoutLabel) { showTextPopup((LayoutLabel) el); return; }
            if (el instanceof LayoutLegend) { showLegendPopup((LayoutLegend) el); return; }
            refreshPropertiesPanel();
        }

        private void showTextPopup(LayoutLabel label) {
            JDialog popup = new JDialog(SwingUtilities.getWindowAncestor(LayoutPreviewPanel.this));
            popup.setUndecorated(true);
            JPanel panel = new JPanel(new GridLayout(0, 2, 4, 4));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x1976D2), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            panel.setBackground(Color.WHITE);

            Font f = label.getFont();
            JComboBox<String> fontCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            fontCombo.setSelectedItem(f.getFamily());
            fontCombo.addActionListener(e -> { label.setFont(new Font((String)fontCombo.getSelectedItem(), f.getStyle(), f.getSize())); panel.repaint(); previewPanel.repaint(); });
            panel.add(new JLabel("Fuente:")); panel.add(fontCombo);

            JSpinner sizeSpin = new JSpinner(new SpinnerNumberModel(f.getSize(), 6, 72, 1));
            sizeSpin.addChangeListener(e -> { label.setFont(label.getFont().deriveFont((float)(Integer)sizeSpin.getValue())); previewPanel.repaint(); });
            panel.add(new JLabel("Tamano:")); panel.add(sizeSpin);

            JToggleButton boldBtn = new JToggleButton("B", f.isBold());
            boldBtn.setFont(boldBtn.getFont().deriveFont(Font.BOLD));
            boldBtn.addActionListener(e -> { label.setFont(label.getFont().deriveFont(boldBtn.isSelected() ? label.getFont().getStyle() | Font.BOLD : label.getFont().getStyle() & ~Font.BOLD)); previewPanel.repaint(); });
            JToggleButton italicBtn = new JToggleButton("I", f.isItalic());
            italicBtn.setFont(italicBtn.getFont().deriveFont(Font.ITALIC));
            italicBtn.addActionListener(e -> { label.setFont(label.getFont().deriveFont(italicBtn.isSelected() ? label.getFont().getStyle() | Font.ITALIC : label.getFont().getStyle() & ~Font.ITALIC)); previewPanel.repaint(); });
            JPanel styleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0)); styleRow.setOpaque(false);
            styleRow.add(boldBtn); styleRow.add(italicBtn);
            panel.add(new JLabel("Estilo:")); panel.add(styleRow);

            JButton colorBtn = new JButton("...");
            colorBtn.setBackground(label.getColor()); colorBtn.setOpaque(true);
            colorBtn.addActionListener(e -> {
                Color c = JColorChooser.showDialog(popup, "Color", label.getColor());
                if (c != null) { label.setColor(c); colorBtn.setBackground(c); previewPanel.repaint(); }
            });
            panel.add(new JLabel("Color:")); panel.add(colorBtn);

            JButton closeBtn = new JButton("Cerrar");
            closeBtn.addActionListener(e -> popup.dispose());
            panel.add(new JLabel()); panel.add(closeBtn);

            popup.add(panel); popup.pack();
            Point p = LayoutPreviewPanel.this.getLocationOnScreen();
            java.awt.Rectangle pb = lastPageBounds;
            double px = pb.x + label.getBoundsMm().x * lastPreviewScale * PREVIEW_RENDER_DPI / 25.4;
            double py = pb.y + label.getBoundsMm().y * lastPreviewScale * PREVIEW_RENDER_DPI / 25.4;
            popup.setLocation(p.x + (int)px, p.y + (int)py + 20);
            popup.setVisible(true);
        }

        private void showLegendPopup(LayoutLegend legend) {
            JDialog popup = new JDialog(SwingUtilities.getWindowAncestor(LayoutPreviewPanel.this));
            popup.setUndecorated(true);
            JPanel panel = new JPanel(new GridLayout(0, 2, 4, 4));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x1976D2), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            panel.setBackground(Color.WHITE);

            JTextField titleField = new JTextField(legend.getTitle() != null ? legend.getTitle() : "", 15);
            titleField.addActionListener(e -> { legend.setTitle(titleField.getText().trim()); previewPanel.repaint(); });
            panel.add(new JLabel("Titulo:")); panel.add(titleField);

            JSpinner titleSize = new JSpinner(new SpinnerNumberModel((int)legend.getTitleFont().getSize(), 8, 36, 1));
            titleSize.addChangeListener(e -> { legend.setTitleFont(legend.getTitleFont().deriveFont((float)(Integer)titleSize.getValue())); previewPanel.repaint(); });
            panel.add(new JLabel("Tam. titulo:")); panel.add(titleSize);

            JSpinner itemSize = new JSpinner(new SpinnerNumberModel((int)legend.getItemFont().getSize(), 6, 24, 1));
            itemSize.addChangeListener(e -> { legend.setItemFont(legend.getItemFont().deriveFont((float)(Integer)itemSize.getValue())); previewPanel.repaint(); });
            panel.add(new JLabel("Tam. items:")); panel.add(itemSize);

            JCheckBox bgCheck = new JCheckBox("", legend.isShowBackground());
            bgCheck.addActionListener(e -> { legend.setShowBackground(bgCheck.isSelected()); previewPanel.repaint(); });
            panel.add(new JLabel("Fondo:")); panel.add(bgCheck);

            JCheckBox brdCheck = new JCheckBox("", legend.isShowBorder());
            brdCheck.addActionListener(e -> { legend.setShowBorder(brdCheck.isSelected()); previewPanel.repaint(); });
            panel.add(new JLabel("Borde:")); panel.add(brdCheck);

            JCheckBox autoCheck = new JCheckBox("", legend.isAutoHeight());
            autoCheck.addActionListener(e -> { legend.setAutoHeight(autoCheck.isSelected()); previewPanel.repaint(); });
            panel.add(new JLabel("Auto alto:")); panel.add(autoCheck);

            JButton updBtn = new JButton("Actualizar capas");
            updBtn.addActionListener(e -> { populateLegendFromProject(legend); previewPanel.repaint(); });
            panel.add(new JLabel()); panel.add(updBtn);

            JButton closeBtn = new JButton("Cerrar");
            closeBtn.addActionListener(e -> popup.dispose());
            panel.add(new JLabel()); panel.add(closeBtn);

            popup.add(panel); popup.pack();
            Point p = LayoutPreviewPanel.this.getLocationOnScreen();
            java.awt.Rectangle pb = lastPageBounds;
            double px = pb.x + legend.getBoundsMm().x * lastPreviewScale * PREVIEW_RENDER_DPI / 25.4;
            double py = pb.y + legend.getBoundsMm().y * lastPreviewScale * PREVIEW_RENDER_DPI / 25.4;
            popup.setLocation(p.x + (int)px, p.y + (int)py + 20);
            popup.setVisible(true);
        }

        private LayoutElement duplicateElement(LayoutElement src) {
            duplicateLayoutElement(src);
            return null;
        }

        private void refresh(Boolean unused) { refreshElementList(); previewPanel.repaint(); }

        private void startDrawing(String shape) {
            drawingShape = shape;
            drawingStart = null;
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            layoutModel.clearSelection();
            refreshElementList();
            repaint();
            statusLabel.setText("Dibujando " + shape + ". Click y arrastra en el canvas. Esc para cancelar.");
        }

        private void cancelDrawing() {
            drawingShape = null;
            drawingStart = null;
            setCursor(Cursor.getDefaultCursor());
            repaint();
            statusLabel.setText("Dibujo cancelado.");
        }

        private void finishDrawing(Point pageEnd) {
            if (drawingStart == null || drawingShape == null) return;
            RectMm pr = toPageRectMm();
            if (pr == null) return;
            double sc = pr.pxToMmScale;
            double x1 = Math.min(drawingStart.x, pageEnd.x) * sc;
            double y1 = Math.min(drawingStart.y, pageEnd.y) * sc;
            double x2 = Math.max(drawingStart.x, pageEnd.x) * sc;
            double y2 = Math.max(drawingStart.y, pageEnd.y) * sc;
            double w = x2 - x1, h = y2 - y1;
            if (w < 2 && h < 2) { w = 20; h = 15; }

            if ("rect".equals(drawingShape)) {
                LayoutRectangle r = new LayoutRectangle(drawingShape + "-" + System.currentTimeMillis(), x1, y1, w, h);
                r.setZOrder(layoutModel.nextZ()); r.setName("Rectangulo " + countOfType("Rectangulo"));
                layoutModel.addElement(r);
            } else if ("ellipse".equals(drawingShape)) {
                LayoutEllipse e = new LayoutEllipse(drawingShape + "-" + System.currentTimeMillis(), x1, y1, w, h);
                e.setZOrder(layoutModel.nextZ()); e.setName("Elipse " + countOfType("Elipse"));
                layoutModel.addElement(e);
            } else if ("line".equals(drawingShape)) {
                LayoutLine l = new LayoutLine(drawingShape + "-" + System.currentTimeMillis(), x1, y1, x2, y2);
                l.setZOrder(layoutModel.nextZ()); l.setName("Linea " + countOfType("Linea"));
                layoutModel.addElement(l);
            }
            drawingShape = null; drawingStart = null;
            setCursor(Cursor.getDefaultCursor());
            refreshElementList();
            repaint();
            statusLabel.setText("Forma creada.");
        }

        private JMenuItem item(String text, Runnable action) {
            JMenuItem mi = new JMenuItem(text);
            mi.addActionListener(e -> action.run());
            return mi;
        }

        private void drawDrawingPreview(Graphics2D g2, int pageX, int pageY, double scale) {
            if (drawingShape == null || drawingStart == null) return;
            PointerInfo pi = MouseInfo.getPointerInfo();
            if (pi == null) return;
            Point screenPt = pi.getLocation();
            SwingUtilities.convertPointFromScreen(screenPt, LayoutPreviewPanel.this);
            Point pageEnd = toPagePoint(screenPt);
            if (pageEnd == null) return;
            int x1 = pageX + (int)(Math.min(drawingStart.x, pageEnd.x) * scale);
            int y1 = pageY + (int)(Math.min(drawingStart.y, pageEnd.y) * scale);
            int w = (int)(Math.abs(pageEnd.x - drawingStart.x) * scale);
            int h = (int)(Math.abs(pageEnd.y - drawingStart.y) * scale);
            g2.setColor(new Color(25, 118, 210, 100));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{6f, 4f}, 0f));
            g2.drawRect(x1, y1, Math.max(1, w), Math.max(1, h));
        }

        private void clearSnapGuides() {
            activeGuideXs.clear();
            activeGuideYs.clear();
        }

        private SnapResult snapCatmapRectangle(Rectangle proposed, String movingItemId) {
            if (proposed == null || lastRenderResult == null) {
                return new SnapResult(proposed != null ? new Rectangle(proposed) : new Rectangle(), new ArrayList<>(), new ArrayList<>());
            }
            Rectangle snapped = new Rectangle(proposed);
            List<Integer> guideXs = new ArrayList<>();
            List<Integer> guideYs = new ArrayList<>();
            int tolerance = 8;

            SnapAxisResult xAxis = snapAxis(
                    snapped.x,
                    snapped.x + snapped.width / 2,
                    snapped.x + snapped.width,
                    collectSnapCandidates(true, movingItemId),
                    tolerance
            );
            snapped.x += xAxis.delta();
            if (xAxis.guide() != null) {
                guideXs.add(xAxis.guide());
            }

            SnapAxisResult yAxis = snapAxis(
                    snapped.y,
                    snapped.y + snapped.height / 2,
                    snapped.y + snapped.height,
                    collectSnapCandidates(false, movingItemId),
                    tolerance
            );
            snapped.y += yAxis.delta();
            if (yAxis.guide() != null) {
                guideYs.add(yAxis.guide());
            }
            return new SnapResult(snapped, guideXs, guideYs);
        }

        private List<Integer> collectSnapCandidates(boolean horizontal, String movingItemId) {
            List<Integer> candidates = new ArrayList<>();
            if (lastRenderResult == null || lastRenderResult.image() == null) {
                return candidates;
            }
            int max = horizontal ? lastRenderResult.image().getWidth() : lastRenderResult.image().getHeight();
            candidates.add(0);
            candidates.add(max / 2);
            candidates.add(max);

            for (Rectangle bounds : lastRenderResult.elementBounds().values()) {
                if (bounds == null) {
                    continue;
                }
                candidates.add(horizontal ? bounds.x : bounds.y);
                candidates.add(horizontal ? bounds.x + bounds.width / 2 : bounds.y + bounds.height / 2);
                candidates.add(horizontal ? bounds.x + bounds.width : bounds.y + bounds.height);
            }
            for (java.util.Map.Entry<String, Rectangle> entry : lastRenderResult.customItemBounds().entrySet()) {
                if (entry.getKey() == null || entry.getKey().equals(movingItemId) || entry.getValue() == null) {
                    continue;
                }
                Rectangle bounds = entry.getValue();
                candidates.add(horizontal ? bounds.x : bounds.y);
                candidates.add(horizontal ? bounds.x + bounds.width / 2 : bounds.y + bounds.height / 2);
                candidates.add(horizontal ? bounds.x + bounds.width : bounds.y + bounds.height);
            }
            return candidates;
        }

        private SnapAxisResult snapAxis(int start, int center, int end, List<Integer> candidates, int tolerance) {
            int bestDelta = 0;
            Integer bestGuide = null;
            int bestDistance = tolerance + 1;
            for (Integer candidate : candidates) {
                if (candidate == null) {
                    continue;
                }
                int[] deltas = new int[]{candidate - start, candidate - center, candidate - end};
                for (int delta : deltas) {
                    int distance = Math.abs(delta);
                    if (distance <= tolerance && distance < bestDistance) {
                        bestDistance = distance;
                        bestDelta = delta;
                        bestGuide = candidate;
                    }
                }
            }
            return new SnapAxisResult(bestDelta, bestGuide);
        }

        private Point toPagePoint(Point panelPoint) {
            if (lastRenderResult == null || lastPageBounds.width <= 0 || lastPageBounds.height <= 0 || lastPreviewScale <= 0) {
                return null;
            }
            if (!lastPageBounds.contains(panelPoint)) {
                return null;
            }
            int pageX = (int) Math.round((panelPoint.x - lastPageBounds.x) / lastPreviewScale);
            int pageY = (int) Math.round((panelPoint.y - lastPageBounds.y) / lastPreviewScale);
            return new Point(pageX, pageY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Disable hardcoded elements when LayoutModel has equivalents (WYSIWYG)
            boolean hasModelLegend = false, hasModelHeader = false, hasModelCartouche = false;
            boolean hasModelNorth = false, hasModelScale = false;
            for (LayoutElement el : layoutModel.getElements()) {
                if (el instanceof LayoutLegend) hasModelLegend = true;
                if (el instanceof LayoutNorthArrow) hasModelNorth = true;
                if (el instanceof LayoutScaleBar) hasModelScale = true;
                if (el instanceof LayoutCartouche) hasModelCartouche = true;
                if (el instanceof LayoutLabel) {
                    String n = el.getName();
                    if (n != null && (n.equals("Titulo") || n.startsWith("Titulo ") || n.equals("Subtitulo")))
                        hasModelHeader = true;
                    if (n != null && (n.equals("Empresa") || n.equals("Cartografo") || n.equals("Estudio") || n.startsWith("Datos")))
                        hasModelCartouche = true;
                }
            }
            if (hasModelLegend) legendCheck.setSelected(false);
            if (hasModelHeader) interactionState.setElementVisible(LayoutElementType.HEADER, false);
            if (hasModelNorth) interactionState.setElementVisible(LayoutElementType.NORTH, false);
            if (hasModelScale) interactionState.setElementVisible(LayoutElementType.SCALE, false);
            if (hasModelCartouche) interactionState.setElementVisible(LayoutElementType.CARTOUCHE, false);
            // Auto-disable template MAP_CONTENT when model has LayoutMap elements
            boolean hasModelMap = false;
            for (LayoutElement el : layoutModel.getElements()) {
                if (el instanceof LayoutMap) { hasModelMap = true; break; }
            }
            if (hasModelMap) interactionState.setElementVisible(LayoutElementType.MAP_CONTENT, false);
            LayoutSettings settings = buildSettings();
            LayoutSnapshot currentSnapshot = getSnapshot();
            if (settings == null || currentSnapshot == null) return;

            Dimension previewSize = settings.pageSize().pixelSize(settings.orientation(), PREVIEW_RENDER_DPI);
            lastRenderResult = LayoutRenderer.renderResult(
                    settings,
                    currentSnapshot,
                    previewSize.width,
                    previewSize.height,
                    interactionState,
                    PREVIEW_RENDER_DPI
            );
            SwingUtilities.invokeLater(() -> updateScaleUiState(lastRenderResult.exactScaleDenominator()));
            BufferedImage page = lastRenderResult.image();

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Dimension viewportSize = resolvePreviewViewportSize();
                int availableWidth = Math.max(80, viewportSize.width - 40);
                int availableHeight = Math.max(80, viewportSize.height - 40);
                double fitPageScale = Math.min(availableWidth / (double) page.getWidth(), availableHeight / (double) page.getHeight());
                double fitWidthScale = availableWidth / (double) page.getWidth();
                double scale = interactionState.resolvePreviewScale(fitPageScale, fitWidthScale);
                scale = Math.max(0.08d, scale);
                int drawWidth = (int) Math.round(page.getWidth() * scale);
                int drawHeight = (int) Math.round(page.getHeight() * scale);
                int rulerSz = RulerRenderer.getRulerSize();
                int x = Math.max(rulerSz + 4, (getWidth() - drawWidth) / 2);
                int y = Math.max(rulerSz + 4, (getHeight() - drawHeight) / 2);

                lastPageBounds = new Rectangle(x, y, drawWidth, drawHeight);
                lastPreviewScale = scale;

                g2.setColor(new Color(200, 205, 212));
                g2.fillRoundRect(x + 10, y + 10, drawWidth, drawHeight, 18, 18);
                // Stronger page shadow (ArcMap-style)
                g2.setColor(new Color(140, 145, 155));
                g2.fillRoundRect(x + 6, y + 6, drawWidth, drawHeight, 14, 14);
                g2.setColor(new Color(170, 175, 185));
                g2.fillRoundRect(x + 4, y + 4, drawWidth, drawHeight, 14, 14);
                // White page
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, drawWidth, drawHeight, 4, 4);
                // Page grid dots (5mm spacing)
                g2.setColor(new Color(210, 215, 225));
                double gridMm = 5;
                double pxPerMmGrid = PREVIEW_RENDER_DPI / 25.4 * scale;
                for (double gx = 0; gx <= settings.pageSize().widthMm; gx += gridMm) {
                    int px = x + (int)(gx * pxPerMmGrid);
                    for (double gy = 0; gy <= settings.pageSize().heightMm; gy += gridMm) {
                        int py = y + (int)(gy * pxPerMmGrid);
                        if (px >= x && py >= y && px <= x + drawWidth && py <= y + drawHeight)
                            g2.fillRect(px, py, 2, 2);
                    }
                }
                g2.drawImage(page, x, y, drawWidth, drawHeight, null);
                // Subtle page border on dark canvas
                g2.setColor(new Color(200, 205, 212));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(x, y, drawWidth, drawHeight, 4, 4);
                drawLayoutModelOverlay(g2, settings, x, y, scale);
                drawSnapGuides(g2, x, y, scale, drawWidth, drawHeight);
                drawPersistentGuides(g2, x, y, scale, drawWidth, drawHeight, settings);
                drawDrawingPreview(g2, x, y, scale);
                RulerRenderer.render(g2, 0, 0, getWidth(), getHeight(), settings.pageSize().widthMm, settings.pageSize().heightMm, PREVIEW_RENDER_DPI, scale);
                SwingUtilities.invokeLater(() -> refreshElementList());
                drawSelectionOverlay(g2, x, y, scale);
                if (inlineTitleEditor.isVisible()) {
                    Rectangle headerBounds = lastRenderResult.elementBounds().get(LayoutElementType.HEADER);
                    if (headerBounds != null) {
                        inlineTitleEditor.setBounds(
                                lastPageBounds.x + (int) Math.round((headerBounds.x + 2) * lastPreviewScale),
                                lastPageBounds.y + (int) Math.round((headerBounds.y + 6) * lastPreviewScale),
                                Math.max(180, (int) Math.round(Math.min(headerBounds.width * 0.72d, 460) * lastPreviewScale)),
                                Math.max(28, (int) Math.round(34 * lastPreviewScale))
                        );
                    }
                }
                if (inlineCartoucheEditor.isVisible()) {
                    Rectangle cartoucheBounds = lastRenderResult.elementBounds().get(LayoutElementType.CARTOUCHE);
                    if (cartoucheBounds != null) {
                        placeInlineCartoucheEditor(cartoucheBounds);
                    }
                }

                g2.setColor(new Color(57, 67, 82));
                g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                String label = settings.pageSize() + " - " + settings.orientation() + " | " + settings.template();
                g2.drawString(label, x + 12, Math.max(18, y - 10));
            } finally {
                g2.dispose();
            }
        }

        private void drawSelectionOverlay(Graphics2D g2, int pageX, int pageY, double scale) {
            LayoutElementType selected = interactionState.getSelectedElement();
            if (selected == null || lastRenderResult == null) {
                return;
            }
            Rectangle bounds = selected == LayoutElementType.CATMAP_ITEM
                    ? lastRenderResult.customItemBounds().get(interactionState.getSelectedCustomItemId())
                    : lastRenderResult.elementBounds().get(selected);
            if (bounds == null) {
                return;
            }
            int x = pageX + (int) Math.round(bounds.x * scale);
            int y = pageY + (int) Math.round(bounds.y * scale);
            int w = Math.max(18, (int) Math.round(bounds.width * scale));
            int h = Math.max(18, (int) Math.round(bounds.height * scale));
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                Color fill = new Color(37, 99, 235, 26);
                Color stroke = new Color(37, 99, 235);
                if (selected == LayoutElementType.MAP_CONTENT && interactionState.isMapFramePanToolActive()) {
                    fill = new Color(16, 185, 129, 28);
                    stroke = new Color(5, 150, 105);
                } else if (selected == LayoutElementType.MAP_CONTENT && interactionState.isMapFrameZoomToolActive()) {
                    fill = new Color(245, 158, 11, 28);
                    stroke = new Color(217, 119, 6);
                }
                copy.setColor(fill);
                copy.fillRoundRect(x, y, w, h, 12, 12);
                copy.setColor(stroke);
                copy.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{7f, 5f}, 0f));
                copy.drawRoundRect(x, y, w, h, 12, 12);
                boolean showHandles = selected == LayoutElementType.MAP_CONTENT
                        ? interactionState.isMapFrameMoveToolActive()
                        : (selected == LayoutElementType.HEADER
                        || selected == LayoutElementType.LEGEND
                        || selected == LayoutElementType.NORTH
                        || selected == LayoutElementType.SCALE
                        || selected == LayoutElementType.CARTOUCHE
                        || selected == LayoutElementType.PROFILE_IMAGE
                        || selected == LayoutElementType.CATMAP_ITEM);
                if (showHandles) {
                    drawResizeHandle(copy, x, y);
                    drawResizeHandle(copy, x + w / 2, y);
                    drawResizeHandle(copy, x + w, y);
                    drawResizeHandle(copy, x, y + h / 2);
                    drawResizeHandle(copy, x + w, y + h / 2);
                    drawResizeHandle(copy, x, y + h);
                    drawResizeHandle(copy, x + w / 2, y + h);
                    drawResizeHandle(copy, x + w, y + h);
                }
            } finally {
                copy.dispose();
            }
        }

        private void drawSnapGuides(Graphics2D g2, int pageX, int pageY, double scale, int drawWidth, int drawHeight) {
            if ((activeGuideXs.isEmpty() && activeGuideYs.isEmpty()) || lastRenderResult == null) {
                return;
            }
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(new Color(14, 116, 144, 170));
                copy.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{4f, 4f}, 0f));
                for (Integer guideX : activeGuideXs) {
                    int x = pageX + (int) Math.round(guideX * scale);
                    copy.drawLine(x, pageY, x, pageY + drawHeight);
                }
                for (Integer guideY : activeGuideYs) {
                    int y = pageY + (int) Math.round(guideY * scale);
                    copy.drawLine(pageX, y, pageX + drawWidth, y);
                }
            } finally {
                copy.dispose();
            }
        }

        private void drawResizeHandle(Graphics2D g2, int centerX, int centerY) {
            int size = 8;
            g2.setColor(Color.WHITE);
            g2.fillRect(centerX - size / 2, centerY - size / 2, size, size);
            g2.setColor(new Color(37, 99, 235));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRect(centerX - size / 2, centerY - size / 2, size, size);
        }

        private record ResizeTarget(LayoutElementType elementType, ResizeHandle handle, String customItemId) {
        }

        private record SnapResult(Rectangle bounds, List<Integer> guideXs, List<Integer> guideYs) {
        }

        private record SnapAxisResult(int delta, Integer guide) {
        }
    }

    static class LayoutRenderer {

        private LayoutRenderer() {
        }

        private static BufferedImage render(LayoutSettings settings, LayoutSnapshot snapshot, int width, int height, LayoutInteractionState interactionState, int renderDpi) {
            return renderResult(settings, snapshot, width, height, interactionState, renderDpi).image();
        }

        private static boolean isRenderableElementVisible(LayoutInteractionState interactionState, LayoutElementType type) {
            return !isFixedLayoutElement(type) || interactionState == null || interactionState.isElementVisible(type);
        }

        private static LayoutRenderResult renderResult(LayoutSettings settings, LayoutSnapshot snapshot, int width, int height, LayoutInteractionState interactionState, int renderDpi) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            EnumMap<LayoutElementType, Rectangle> elementBounds = new EnumMap<>(LayoutElementType.class);
            java.util.LinkedHashMap<String, Rectangle> customItemBounds = new java.util.LinkedHashMap<>();
            BufferedImage layoutImage = loadImageAsset(settings.layoutImagePath());
            MapFrameGeometry mapFrame = null;
            Graphics2D g2 = image.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, width, height);

                int margin = Math.max(40, width / 36);
                int headerHeight = Math.max(112, height / 8);
                int footerHeight = Math.max(150, height / 6);
                if (settings.template() == LayoutTemplate.CLEAN_CENTERED) {
                    headerHeight = Math.max(CLEAN_HEADER_MIN_HEIGHT, (int) Math.round(height * CLEAN_HEADER_HEIGHT_RATIO));
                    footerHeight = Math.max(CLEAN_FOOTER_MIN_HEIGHT, (int) Math.round(height * CLEAN_FOOTER_HEIGHT_RATIO));
                } else if (settings.template() == LayoutTemplate.STRONG_CARTOUCHE) {
                    footerHeight = Math.max(180, height / 5);
                } else if (settings.template() == LayoutTemplate.BOTTOM_REFERENCE) {
                    headerHeight = Math.max(108, height / 9);
                }
                if (layoutImage != null) {
                    footerHeight = Math.max(220, footerHeight + 70);
                }
                boolean showHeader = isRenderableElementVisible(interactionState, LayoutElementType.HEADER);
                boolean showMapContent = isRenderableElementVisible(interactionState, LayoutElementType.MAP_CONTENT);
                boolean showNorth = settings.showNorth() && isRenderableElementVisible(interactionState, LayoutElementType.NORTH);
                boolean showScale = settings.showScale() && isRenderableElementVisible(interactionState, LayoutElementType.SCALE);
                boolean showLegend = settings.showLegend() && isRenderableElementVisible(interactionState, LayoutElementType.LEGEND);
                boolean legendOutsideRight = showLegend && settings.legendPlacement() == LegendPlacement.RIGHT_PANEL;
                boolean legendBottom = showLegend && settings.legendPlacement() == LegendPlacement.BOTTOM_PANEL;
                int legendWidth = legendOutsideRight ? Math.max(260, width / 5) : 0;
                int legendHeight = legendBottom ? Math.max(140, height / 5) : 0;
                int gap = showLegend ? Math.max(18, width / 60) : 0;
                int mapX = margin;
                int mapY = margin + headerHeight;
                int mapW = Math.max(200, width - (margin * 2) - legendWidth - gap);
                int mapH = Math.max(220, height - mapY - footerHeight - margin - (legendBottom ? legendHeight + gap : 0));

                Rectangle headerBounds = applyElementAdjustment(new Rectangle(margin, margin, width - (margin * 2), headerHeight - 14), interactionState, LayoutElementType.HEADER);
                if (showHeader) {
                    drawHeader(g2, settings, snapshot, headerBounds);
                    elementBounds.put(LayoutElementType.HEADER, new Rectangle(headerBounds));
                }

                Rectangle requestedMapBounds = applyElementAdjustment(new Rectangle(mapX, mapY, mapW, mapH), interactionState, LayoutElementType.MAP_CONTENT);
                if (!interactionState.hasCustomSize(LayoutElementType.MAP_CONTENT)) {
                    requestedMapBounds = optimizeMapFrame(requestedMapBounds, snapshot.mapImage(), settings.template());
                }
                if (showMapContent) {
                    mapFrame = drawMapFrame(g2, snapshot, requestedMapBounds, interactionState);
                    elementBounds.put(LayoutElementType.MAP_CONTENT, new Rectangle(mapFrame.frameBounds()));
                } else {
                    Rectangle hiddenMapBounds = new Rectangle(requestedMapBounds);
                    mapFrame = new MapFrameGeometry(hiddenMapBounds, hiddenMapBounds, 0d);
                }
                if (showMapContent && settings.showGrid()) {
                    drawGrid(g2, settings, mapFrame);
                }

                if (showNorth) {
                    Rectangle northBounds = applyElementAdjustment(new Rectangle(
                            mapFrame.frameBounds().x + mapFrame.frameBounds().width - 92,
                            mapFrame.frameBounds().y + 20,
                            Math.max(54, width / 22),
                            Math.max(54, width / 22)
                    ), interactionState, LayoutElementType.NORTH);
                    int northSize = Math.max(32, Math.min(northBounds.width, northBounds.height));
                    Rectangle northVisualBounds = new Rectangle(northBounds.x, northBounds.y, northSize, northSize);
                    drawNorthArrow(g2, settings.northStyle(), northVisualBounds.x, northVisualBounds.y, northVisualBounds.width);
                    elementBounds.put(LayoutElementType.NORTH, northVisualBounds);
                }
                if (showScale && showMapContent) {
                    int scaleMaxW = settings.template() == LayoutTemplate.CLEAN_CENTERED
                            ? Math.min(160, mapFrame.frameBounds().width / 5)
                            : Math.min(240, mapFrame.frameBounds().width / 3);
                    Rectangle scaleBounds = applyElementAdjustment(new Rectangle(
                            mapFrame.frameBounds().x + 8,
                            mapFrame.frameBounds().y + mapFrame.frameBounds().height - 74,
                            scaleMaxW,
                            54
                    ), interactionState, LayoutElementType.SCALE);
                    drawScaleBar(g2, settings, snapshot, mapFrame, scaleBounds.x + 14, scaleBounds.y + 18, renderDpi);
                    elementBounds.put(LayoutElementType.SCALE, scaleBounds);
                }
                if (showLegend) {
                    Rectangle legendBounds = resolveLegendBounds(settings, width, margin, gap, legendWidth, legendHeight, mapFrame, mapFrame.frameBounds().y + mapFrame.frameBounds().height);
                    legendBounds = applyElementAdjustment(legendBounds, interactionState, LayoutElementType.LEGEND);
                    drawLegend(g2, settings, snapshot.visibleLayers(), legendBounds.x, legendBounds.y, legendBounds.width, legendBounds.height, interactionState);
                    elementBounds.put(LayoutElementType.LEGEND, legendBounds);
                }

                FooterRenderResult footerResult = drawFooter(g2, settings, snapshot, width, height, margin, footerHeight, mapFrame, interactionState, layoutImage, renderDpi);
                if (footerResult.cartoucheBounds() != null) {
                    elementBounds.put(LayoutElementType.CARTOUCHE, footerResult.cartoucheBounds());
                }
                if (footerResult.profileImageBounds() != null) {
                    elementBounds.put(LayoutElementType.PROFILE_IMAGE, footerResult.profileImageBounds());
                }
                drawCatmapItems(g2, settings.catmapItems(), customItemBounds);
            } finally {
                g2.dispose();
            }
            double exactScaleDenominator = estimateScaleDenominator(mapFrame, renderDpi);
            return new LayoutRenderResult(image, elementBounds, customItemBounds, exactScaleDenominator);
        }

        private static void exportPdf(LayoutSettings settings, LayoutSnapshot snapshot, File file, LayoutInteractionState interactionState) throws Exception {
            try (PDDocument document = new PDDocument()) {
                PDRectangle rectangle = settings.pageSize().toPdfRectangle(settings.orientation());
                PDPage page = new PDPage(rectangle);
                document.addPage(page);

                Dimension size = settings.pageSize().pixelSize(settings.orientation(), settings.dpi());
                BufferedImage layoutArgb = render(settings, snapshot, size.width, size.height, interactionState, settings.dpi());
                BufferedImage layout = new BufferedImage(layoutArgb.getWidth(), layoutArgb.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D rgbG2 = layout.createGraphics();
                try {
                    rgbG2.setColor(Color.WHITE);
                    rgbG2.fillRect(0, 0, layout.getWidth(), layout.getHeight());
                    rgbG2.drawImage(layoutArgb, 0, 0, null);
                } finally {
                    rgbG2.dispose();
                }
                PDImageXObject pdfImage = LosslessFactory.createFromImage(document, layout);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.drawImage(pdfImage, 0, 0, rectangle.getWidth(), rectangle.getHeight());
                }
                document.save(file);
            }
        }

        private static Rectangle resolveLegendBounds(LayoutSettings settings, int width, int margin, int gap, int legendWidth, int legendHeight, MapFrameGeometry mapFrame, int bottomY) {
            return switch (settings.legendPlacement()) {
                case BOTTOM_PANEL -> new Rectangle(margin, bottomY + gap, width - (margin * 2), legendHeight);
                case MAP_TOP_RIGHT -> new Rectangle(
                        mapFrame.frameBounds().x + mapFrame.frameBounds().width - Math.max(180, mapFrame.frameBounds().width / 4),
                        mapFrame.frameBounds().y + 18,
                        Math.max(180, mapFrame.frameBounds().width / 4),
                        Math.max(120, mapFrame.frameBounds().height / 4)
                );
                case MAP_BOTTOM_RIGHT -> {
                    int maxW = Math.max(180, mapFrame.frameBounds().width / 4);
                    int maxH = Math.max(120, mapFrame.frameBounds().height / 4);
                    if (settings.template() == LayoutTemplate.CLEAN_CENTERED) {
                        maxW = Math.max(maxW, mapFrame.frameBounds().width / 5);
                        maxH = Math.max(maxH, mapFrame.frameBounds().height / 4);
                    }
                    yield new Rectangle(
                            mapFrame.frameBounds().x + mapFrame.frameBounds().width - maxW - 12,
                            mapFrame.frameBounds().y + mapFrame.frameBounds().height - maxH - 12,
                            maxW,
                            maxH
                    );
                }
                case MAP_BOTTOM_LEFT -> new Rectangle(
                        mapFrame.frameBounds().x + 18,
                        mapFrame.frameBounds().y + mapFrame.frameBounds().height - Math.max(120, mapFrame.frameBounds().height / 4) - 18,
                        Math.max(180, mapFrame.frameBounds().width / 4),
                        Math.max(120, mapFrame.frameBounds().height / 4)
                );
                default -> new Rectangle(
                        mapFrame.frameBounds().x + mapFrame.frameBounds().width + gap,
                        mapFrame.frameBounds().y,
                        legendWidth,
                        mapFrame.frameBounds().height
                );
            };
        }

        private static Rectangle applyOffset(Rectangle source, Point offset) {
            Rectangle result = new Rectangle(source);
            if (offset != null) {
                result.translate(offset.x, offset.y);
            }
            return result;
        }

        private static Rectangle applyElementAdjustment(Rectangle source, LayoutInteractionState interactionState, LayoutElementType type) {
            Rectangle result = new Rectangle(source);
            if (interactionState == null || type == null) {
                return result;
            }
            Point offset = interactionState.getOffset(type);
            Dimension sizeAdjustment = interactionState.getSizeAdjustment(type);
            result.translate(offset.x, offset.y);
            int minWidth = switch (type) {
                case NORTH -> 32;
                case SCALE -> 96;
                case HEADER -> 180;
                case MAP_CONTENT -> 260;
                default -> 80;
            };
            int minHeight = switch (type) {
                case NORTH -> 32;
                case SCALE -> 34;
                case HEADER -> 56;
                case MAP_CONTENT -> 180;
                default -> 60;
            };
            result.width = Math.max(minWidth, result.width + sizeAdjustment.width);
            result.height = Math.max(minHeight, result.height + sizeAdjustment.height);
            return result;
        }

        private static Rectangle optimizeMapFrame(Rectangle availableBounds, BufferedImage mapImage, LayoutTemplate template) {
            if (template == LayoutTemplate.CLEAN_CENTERED) {
                return new Rectangle(availableBounds);
            }
            if (mapImage == null || availableBounds.width <= 0 || availableBounds.height <= 0) {
                return new Rectangle(availableBounds);
            }
            double mapAspect = mapImage.getWidth() / (double) Math.max(1, mapImage.getHeight());
            double availableAspect = availableBounds.getWidth() / Math.max(1d, availableBounds.getHeight());
            Rectangle adjusted = new Rectangle(availableBounds);

            if (Math.abs(mapAspect - availableAspect) < 0.02d) {
                return adjusted;
            }

            if (mapAspect > availableAspect) {
                int targetHeight = Math.max(180, (int) Math.round(adjusted.width / mapAspect));
                if (targetHeight < adjusted.height) {
                    int anchorY = template == LayoutTemplate.CLEAN_CENTERED
                            ? adjusted.y + Math.max(0, (adjusted.height - targetHeight) / 2)
                            : adjusted.y;
                    adjusted = new Rectangle(adjusted.x, anchorY, adjusted.width, targetHeight);
                }
            } else {
                int targetWidth = Math.max(220, (int) Math.round(adjusted.height * mapAspect));
                if (targetWidth < adjusted.width) {
                    adjusted = new Rectangle(
                            adjusted.x + Math.max(0, (adjusted.width - targetWidth) / 2),
                            adjusted.y,
                            targetWidth,
                            adjusted.height
                    );
                }
            }
            return adjusted;
        }

        private static void drawHeader(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, Rectangle bounds) {
            boolean cleanTemplate = settings.template() == LayoutTemplate.CLEAN_CENTERED;
            int titleFontSize = cleanTemplate ? Math.max(22, bounds.width / 48) : Math.max(28, bounds.width / 34);
            int subtitleFontSize = cleanTemplate ? Math.max(12, bounds.width / 110) : Math.max(15, bounds.width / 90);
            int metaFontSize = cleanTemplate ? Math.max(11, bounds.width / 120) : Math.max(13, bounds.width / 100);
            int titleY = bounds.y + Math.max(22, bounds.height / 5);
            int rowGap = cleanTemplate ? 24 : 30;

            g2.setColor(new Color(27, 38, 56));
            g2.setFont(new Font("SansSerif", Font.BOLD, titleFontSize));
            String title = !settings.title().isBlank() ? settings.title() : snapshot.projectName();
            java.awt.FontMetrics titleMetrics = g2.getFontMetrics();
            if (cleanTemplate && titleMetrics.stringWidth(title) > bounds.width - 10) {
                title = clipText(title, (bounds.width - 10) / Math.max(1, (int) Math.round(titleMetrics.stringWidth("W"))));
            }
            g2.drawString(title, bounds.x, titleY);

            if (cleanTemplate) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, metaFontSize));
                g2.setColor(new Color(105, 114, 126));
                String meta = snapshot.projectName() + " | " + snapshot.projectCrsLabel();
                g2.drawString(meta, bounds.x, titleY + rowGap);
            } else {
                g2.setFont(new Font("SansSerif", Font.PLAIN, subtitleFontSize));
                g2.setColor(new Color(91, 103, 120));
                String subtitle = !settings.subtitle().isBlank() ? settings.subtitle() : "Salida cartografica del proyecto actual";
                g2.drawString(subtitle, bounds.x, titleY + rowGap);

                g2.setFont(new Font("SansSerif", Font.PLAIN, metaFontSize));
                g2.setColor(new Color(105, 114, 126));
                String meta = snapshot.projectName() + " | " + snapshot.projectCrsLabel();
                g2.drawString(meta, bounds.x, titleY + rowGap * 2);
            }

        }

        private static void drawChip(Graphics2D g2, int x, int y, String text) {
            Font chipFont = new Font("SansSerif", Font.BOLD, 11);
            g2.setFont(chipFont);
            java.awt.FontMetrics metrics = g2.getFontMetrics();
            int width = metrics.stringWidth(text) + 18;
            g2.setColor(new Color(239, 244, 251));
            g2.fillRoundRect(x, y, width, 24, 12, 12);
            g2.setColor(new Color(197, 210, 227));
            g2.drawRoundRect(x, y, width, 24, 12, 12);
            g2.setColor(new Color(58, 71, 90));
            g2.drawString(text, x + 9, y + 16);
        }

        private static MapFrameGeometry drawMapFrame(Graphics2D g2, LayoutSnapshot snapshot, Rectangle requestedBounds, LayoutInteractionState interactionState) {
            int x = requestedBounds.x;
            int y = requestedBounds.y;
            int w = requestedBounds.width;
            int h = requestedBounds.height;
            g2.setColor(new Color(255, 255, 255));
            g2.fillRoundRect(x, y, w, h, 12, 12);
            g2.setColor(new Color(180, 190, 204));
            g2.setStroke(new BasicStroke(0.7f));
            g2.drawRoundRect(x, y, w, h, 12, 12);

            int innerPadding = Math.max(12, Math.min(w, h) / 50);
            int contentX = x + innerPadding;
            int contentY = y + innerPadding;
            int contentW = w - (innerPadding * 2);
            int contentH = h - (innerPadding * 2);

            g2.setColor(new Color(255, 255, 255));
            g2.fillRect(contentX, contentY, contentW, contentH);

            BufferedImage mapImage = snapshot != null ? snapshot.mapImage() : null;
            double shownGroundMeters = snapshot != null ? snapshot.representativeMeters() : 0d;
            if (snapshot != null && ctxMapPanel() != null && snapshot.basePixelWidth() > 0 && snapshot.basePixelHeight() > 0) {
                double zoomMultiplier = interactionState != null ? Math.max(0.02d, interactionState.getMapZoom()) : 1d;
                double targetZoom = Math.max(0.000001d, snapshot.baseZoomFactor() * zoomMultiplier);
                double baseWorldWidth = snapshot.basePixelWidth() / Math.max(snapshot.baseZoomFactor(), 0.000001d);
                double baseWorldHeight = snapshot.basePixelHeight() / Math.max(snapshot.baseZoomFactor(), 0.000001d);
                double baseCenterX = snapshot.baseViewMinX() + (baseWorldWidth / 2d);
                double baseCenterY = snapshot.baseViewMinY() + (baseWorldHeight / 2d);
                double fitToFrameScale = Math.min(contentW / (double) Math.max(1, snapshot.basePixelWidth()),
                        contentH / (double) Math.max(1, snapshot.basePixelHeight()));
                fitToFrameScale = Math.max(0.000001d, fitToFrameScale);
                double offsetWorldX = (interactionState != null ? interactionState.getMapOffsetX() : 0d) / (fitToFrameScale * targetZoom);
                double offsetWorldY = (interactionState != null ? interactionState.getMapOffsetY() : 0d) / (fitToFrameScale * targetZoom);
                double currentWorldWidth = snapshot.basePixelWidth() / targetZoom;
                double currentWorldHeight = snapshot.basePixelHeight() / targetZoom;
                double viewCenterX = baseCenterX - offsetWorldX;
                double viewCenterY = baseCenterY + offsetWorldY;
                double viewMinX = viewCenterX - (currentWorldWidth / 2d);
                double viewMinY = viewCenterY - (currentWorldHeight / 2d);
                BufferedImage rendered = ctxMapPanel().renderMapViewImage(viewMinX, viewMinY, targetZoom);
                if (rendered != null) {
                    mapImage = rendered;
                }
                shownGroundMeters = convertWorldWidthToMeters(snapshot, currentWorldWidth, viewCenterY);
            }
            if (mapImage == null) {
                mapImage = new BufferedImage(Math.max(1, contentW), Math.max(1, contentH), BufferedImage.TYPE_INT_ARGB);
            }
            double scale = Math.max(contentW / (double) Math.max(1, mapImage.getWidth()), contentH / (double) Math.max(1, mapImage.getHeight()));
            int drawW = (int) Math.round(mapImage.getWidth() * scale);
            int drawH = (int) Math.round(mapImage.getHeight() * scale);
            int drawX = contentX + (contentW - drawW) / 2;
            int drawY = contentY + (contentH - drawH) / 2;
            double visibleGroundMeters = shownGroundMeters;
            if (drawW > 0 && contentW > 0 && shownGroundMeters > 0) {
                visibleGroundMeters = shownGroundMeters * (contentW / (double) drawW);
            }
            Graphics2D imageGraphics = (Graphics2D) g2.create();
            try {
                imageGraphics.setClip(contentX, contentY, contentW, contentH);
                imageGraphics.drawImage(mapImage, drawX, drawY, drawW, drawH, null);
            } finally {
                imageGraphics.dispose();
            }

            g2.setColor(new Color(157, 169, 184));
            g2.drawRect(contentX, contentY, contentW, contentH);

            g2.setColor(new Color(103, 112, 124, 88));
            g2.drawLine(contentX + contentW / 2, contentY, contentX + contentW / 2, contentY + contentH);
            g2.drawLine(contentX, contentY + contentH / 2, contentX + contentW, contentY + contentH / 2);

            return new MapFrameGeometry(new Rectangle(contentX, contentY, contentW, contentH), new Rectangle(contentX, contentY, contentW, contentH), visibleGroundMeters);
        }

        private static void drawGrid(Graphics2D g2, LayoutSettings settings, MapFrameGeometry mapFrame) {
            int cols = Math.max(2, settings.gridColumns());
            int rows = Math.max(2, settings.gridRows());
            Rectangle bounds = mapFrame.imageBounds();
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(new Color(37, 99, 235, 110));
                copy.setStroke(new BasicStroke(1.1f));
                for (int col = 1; col < cols; col++) {
                    int x = bounds.x + (int) Math.round(bounds.width * (col / (double) cols));
                    copy.drawLine(x, bounds.y, x, bounds.y + bounds.height);
                }
                for (int row = 1; row < rows; row++) {
                    int y = bounds.y + (int) Math.round(bounds.height * (row / (double) rows));
                    copy.drawLine(bounds.x, y, bounds.x + bounds.width, y);
                }

                if (settings.showGridLabels()) {
                    copy.setFont(new Font("SansSerif", Font.BOLD, 11));
                    copy.setColor(new Color(29, 78, 216));
                    for (int col = 0; col < cols; col++) {
                        int centerX = bounds.x + (int) Math.round(bounds.width * ((col + 0.5d) / cols));
                        copy.drawString(letterLabel(col + 1), centerX - 4, bounds.y - 6);
                    }
                    for (int row = 0; row < rows; row++) {
                        int centerY = bounds.y + (int) Math.round(bounds.height * ((row + 0.5d) / rows));
                        copy.drawString(String.valueOf(row + 1), bounds.x - 18, centerY + 4);
                    }
                }
            } finally {
                copy.dispose();
            }
        }

        private static String letterLabel(int index) {
            int value = Math.max(1, index);
            StringBuilder label = new StringBuilder();
            while (value > 0) {
                value--;
                label.insert(0, (char) ('A' + (value % 26)));
                value /= 26;
            }
            return label.toString();
        }

        private static void drawNorthArrow(Graphics2D g2, NorthStyle style, int x, int y, int size) {
            paintNorthSymbol(g2, style, x, y, size);
        }

    private static ImageIcon createNorthPreviewIcon(NorthStyle style, int size) {
        int iconSize = Math.max(18, size);
        BufferedImage image = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintNorthSymbol(g2, style, 0, 0, iconSize);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(image);
    }

    private static void paintNorthSymbol(Graphics2D g2, NorthStyle style, int x, int y, int size) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                NorthStyle resolved = style != null ? style : NorthStyle.CLASSIC;
                int centerX = x + size / 2;
                int top = y + 10;
                int bottom = y + size - 12;

                if (resolved == NorthStyle.SIMPLE) {
                    copy.setColor(new Color(255, 255, 255, 220));
                    copy.fillRoundRect(x + 6, y + 4, size - 12, size - 8, 14, 14);
                    copy.setColor(new Color(34, 44, 60));
                    copy.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, size / 3)));
                    copy.drawString("N", centerX - 5, y + 18);
                    copy.setStroke(new BasicStroke(2f));
                    copy.drawLine(centerX, y + 22, centerX, bottom);
                    copy.drawLine(centerX, y + 22, centerX - 8, y + 34);
                    copy.drawLine(centerX, y + 22, centerX + 8, y + 34);
                    return;
                }

                if (resolved == NorthStyle.TECHNICAL) {
                    copy.setColor(new Color(255, 255, 255, 232));
                    copy.fillRoundRect(x + 3, y + 3, size - 6, size - 6, 12, 12);
                    copy.setColor(new Color(196, 205, 216));
                    copy.drawRoundRect(x + 3, y + 3, size - 6, size - 6, 12, 12);
                    copy.setColor(new Color(31, 41, 55));
                    copy.setStroke(new BasicStroke(Math.max(1.8f, size / 20f)));
                    copy.drawLine(centerX, y + 12, centerX, bottom - 2);
                    copy.drawLine(x + 14, y + size / 2, x + size - 14, y + size / 2);
                    Path2D head = new Path2D.Double();
                    head.moveTo(centerX, y + 8);
                    head.lineTo(centerX + size / 9d, y + size / 3d);
                    head.lineTo(centerX - size / 9d, y + size / 3d);
                    head.closePath();
                    copy.setColor(new Color(14, 116, 144));
                    copy.fill(head);
                    copy.setColor(new Color(31, 41, 55));
                    copy.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, size / 4)));
                    copy.drawString("N", centerX - 5, y + 18);
                    return;
                }

                copy.setColor(new Color(255, 255, 255, 226));
                copy.fill(new Ellipse2D.Double(x, y, size, size));
                copy.setColor(new Color(207, 214, 224));
                copy.draw(new Ellipse2D.Double(x, y, size, size));

                if (resolved == NorthStyle.ROSE) {
                    copy.setColor(new Color(241, 245, 249));
                    copy.fill(new Ellipse2D.Double(x + size * 0.16, y + size * 0.16, size * 0.68, size * 0.68));
                    copy.setColor(new Color(207, 214, 224));
                    copy.draw(new Ellipse2D.Double(x + size * 0.16, y + size * 0.16, size * 0.68, size * 0.68));
                    Path2D northNeedle = new Path2D.Double();
                    northNeedle.moveTo(centerX, top);
                    northNeedle.lineTo(centerX + size / 9d, centerX);
                    northNeedle.lineTo(centerX, bottom - size / 4d);
                    northNeedle.lineTo(centerX - size / 9d, centerX);
                    northNeedle.closePath();
                    Path2D southNeedle = new Path2D.Double();
                    southNeedle.moveTo(centerX, bottom);
                    southNeedle.lineTo(centerX + size / 10d, centerX);
                    southNeedle.lineTo(centerX, top + size / 3d);
                    southNeedle.lineTo(centerX - size / 10d, centerX);
                    southNeedle.closePath();
                    copy.setColor(new Color(15, 23, 42));
                    copy.fill(northNeedle);
                    copy.setColor(new Color(245, 158, 11));
                    copy.fill(southNeedle);
                    copy.setColor(new Color(14, 116, 144));
                    copy.fillPolygon(
                            new int[]{centerX, x + size - 12, centerX, x + 12},
                            new int[]{y + size / 2, y + size / 2, y + size / 2 + 6, y + size / 2},
                            4
                    );
                    copy.setColor(new Color(28, 38, 54));
                    copy.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, size / 3)));
                    copy.drawString("N", centerX - 6, top - 2);
                    return;
                }

                Path2D arrow = new Path2D.Double();
                arrow.moveTo(centerX, top);
                arrow.lineTo(centerX + size / 7d, bottom - size / 5d);
                arrow.lineTo(centerX, bottom - size / 3d);
                arrow.lineTo(centerX - size / 7d, bottom - size / 5d);
                arrow.closePath();

                if (resolved == NorthStyle.MODERN) {
                    copy.setColor(new Color(15, 23, 42));
                    copy.fill(arrow);
                    copy.setColor(new Color(96, 165, 250));
                    copy.fillPolygon(new int[]{centerX, centerX + 6, centerX}, new int[]{top + 6, bottom - 10, bottom - 18}, 3);
                } else {
                    copy.setPaint(new GradientPaint(x, y, new Color(22, 94, 188), x + size, y + size, new Color(8, 54, 117)));
                    copy.fill(arrow);
                }
                copy.setColor(new Color(28, 38, 54));
                copy.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, size / 3)));
                copy.drawString("N", centerX - 6, top - 2);
            } finally {
                copy.dispose();
            }
    }

        private static void drawScaleBar(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, MapFrameGeometry mapFrame, int x, int y, int renderDpi) {
            if (mapFrame.shownGroundMeters() <= 0 || mapFrame.imageBounds().width <= 0) {
                return;
            }

            double metersPerPixel = mapFrame.shownGroundMeters() / Math.max(1d, mapFrame.imageBounds().width);
            double maxMeters = metersPerPixel * Math.min(drawScaleBarMaxMetricMeters(mapFrame), mapFrame.imageBounds().width * 0.28d);
            double roundedMeters = settings.scaleRule().roundValue(maxMeters);
            int barWidth = (int) Math.max(72, Math.round(roundedMeters / metersPerPixel));
            int maxBarPx = (int) (mapFrame.imageBounds().width * 0.30d);
            barWidth = Math.min(barWidth, maxBarPx);
            int segmentCount = barWidth >= 160 ? 4 : 2;
            int segmentWidth = Math.max(1, barWidth / segmentCount);

            double exactDenominator = estimateScaleDenominator(mapFrame, renderDpi);
            String scaleText = exactDenominator > 0
                    ? formatScaleDenominator(exactDenominator)
                    : snapshot.scaleLabel();

            if (settings.scaleStyle() == ScaleStyle.NUMERIC) {
                g2.setColor(new Color(255, 255, 255, 218));
                g2.fillRoundRect(x - 12, y - 26, 138, 36, 14, 14);
                g2.setColor(new Color(70, 80, 96));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString(scaleText, x, y - 3);
                return;
            }

            g2.setColor(new Color(255, 255, 255, 218));
            g2.fillRoundRect(x - 12, y - 34, barWidth + 110, 50, 14, 14);
            g2.setColor(new Color(70, 80, 96));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(scaleText, x, y - 14);

            if (settings.scaleStyle() == ScaleStyle.SIMPLE_BAR) {
                g2.setColor(Color.BLACK);
                g2.fillRect(x, y + 2, barWidth, 7);
                g2.drawString("0", x - 4, y + 28);
                g2.drawString(formatDistance(roundedMeters), x + barWidth - 12, y + 28);
                return;
            }

            for (int i = 0; i < segmentCount; i++) {
                g2.setColor(i % 2 == 0 ? Color.BLACK : Color.WHITE);
                int segmentX = x + (i * segmentWidth);
                int width = i == segmentCount - 1 ? barWidth - (segmentWidth * i) : segmentWidth;
                g2.fillRect(segmentX, y, width, 12);
                g2.setColor(Color.BLACK);
                g2.drawRect(segmentX, y, width, 12);
            }
            g2.drawString("0", x - 4, y + 28);
            g2.drawString(formatDistance(roundedMeters), x + barWidth - 12, y + 28);
        }

        private static double drawScaleBarMaxMetricMeters(MapFrameGeometry mapFrame) {
            if (mapFrame == null || mapFrame.imageBounds().width <= 0) {
                return 120d;
            }
            double aspectRatio = mapFrame.imageBounds().width / Math.max(1d, mapFrame.imageBounds().height);
            if (aspectRatio > 1.6d) {
                return 96d;
            }
            if (aspectRatio < 1.1d) {
                return 150d;
            }
            return 120d;
        }

        private static void drawLegend(Graphics2D g2, LayoutSettings settings, List<Layer> layers, int x, int y, int width, int height, LayoutInteractionState interactionState) {
            List<LegendItem> items = buildLegendItems(layers);

            boolean userResized = interactionState != null && !interactionState.getSizeAdjustment(LayoutElementType.LEGEND).equals(new java.awt.Dimension(0, 0));

            int fontSizeTitle;
            int fontSizeSub;
            int fontSizeItem;
            int itemH;
            int headerH;
            int padBottom;
            int padX;

            if (userResized) {
                fontSizeTitle = Math.max(12, Math.min(22, height / 8));
                fontSizeSub = Math.max(10, Math.min(16, height / 12));
                fontSizeItem = Math.max(10, Math.min(16, height / 12));
                padX = Math.max(10, Math.min(20, width / 18));
                headerH = Math.max(40, height / 4);
                itemH = Math.max(22, Math.min(40, (height - headerH - 14) / Math.max(1, items.size())));
                padBottom = Math.max(10, height / 16);
            } else {
                fontSizeTitle = 14;
                fontSizeSub = 12;
                fontSizeItem = 12;
                padX = 14;
                headerH = 56;
                itemH = 28;
                padBottom = 16;
                int needed = headerH + (items.size() * itemH) + padBottom;
                if (needed < height) {
                    int diff = height - needed;
                    y += diff / 2;
                    height = needed;
                }
            }

            if (items.isEmpty()) {
                height = Math.max(56, headerH + padBottom);
            }

            g2.setColor(new Color(250, 252, 255));
            g2.fillRoundRect(x, y, width, height, 14, 14);
            g2.setColor(new Color(210, 216, 224));
            g2.drawRoundRect(x, y, width, height, 14, 14);

            g2.setColor(new Color(26, 36, 52));
            g2.setFont(new Font("SansSerif", Font.BOLD, fontSizeTitle));
            String legendTitle = !settings.legendTitle().isBlank() ? settings.legendTitle() : "Referencias";
            g2.drawString(legendTitle, x + padX, y + fontSizeTitle + 6);

            g2.setFont(new Font("SansSerif", Font.PLAIN, fontSizeSub));
            g2.setColor(new Color(103, 114, 128));
            String legendSubtitle = !settings.legendSubtitle().isBlank() ? settings.legendSubtitle() : "Capas del mapa";
            g2.drawString(legendSubtitle, x + padX, y + fontSizeTitle + fontSizeSub + 12);

            if (items.isEmpty()) {
                g2.drawString("Sin capas para mostrar.", x + padX, y + headerH);
                return;
            }

            int itemY = y + headerH;
            int count = 0;
            for (LegendItem item : items) {
                if (itemY + itemH > y + height - padBottom) {
                    int remaining = Math.max(0, items.size() - count);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, fontSizeSub));
                    g2.setColor(new Color(108, 116, 128));
                    g2.drawString("+" + remaining + " mas", x + padX, y + height - 6);
                    break;
                }
                drawLegendItemScaled(g2, item, x + padX, itemY, width - (padX * 2), fontSizeItem, userResized);
                itemY += itemH;
                count++;
            }
        }

        private static void drawLegendItemScaled(Graphics2D g2, LegendItem item, int x, int y, int availableWidth, int fontSize, boolean scaled) {
            Layer layer = item.layer();
            ShapefileData data = ctxMapPanel() != null ? ctxMapPanel().getShapefileData(layer) : null;
            String geometryFamily = VectorLayerUtils.resolveGeometryFamily(data);

            int symSize = scaled ? Math.max(12, fontSize + 4) : 20;

            if (layer instanceof RasterLayer || layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
                g2.setPaint(new GradientPaint(x, y, new Color(96, 165, 250), x + symSize, y + symSize, new Color(59, 130, 246)));
                g2.fillRect(x, y - symSize/2, symSize, symSize/2 + 4);
                g2.setColor(new Color(30, 41, 59));
                g2.drawRect(x, y - symSize/2, symSize, symSize/2 + 4);
            } else if ("POINT".equalsIgnoreCase(item.geometryType())) {
                drawPointSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("LINE".equalsIgnoreCase(item.geometryType())) {
                drawLineSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("POLYGON".equalsIgnoreCase(item.geometryType())) {
                drawPolygonSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("POINT".equalsIgnoreCase(geometryFamily)) {
                drawPointSymbolPreview(g2, layer, x, y, null);
            } else if ("LINE".equalsIgnoreCase(geometryFamily)) {
                drawLineSymbolPreview(g2, layer, x, y, null);
            } else {
                drawPolygonSymbolPreview(g2, layer, x, y, null);
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            g2.setColor(new Color(37, 45, 58));
            String name = item.label() != null ? item.label() : "Capa";
            int maxChars = Math.max(8, (availableWidth - 30) / (fontSize / 2));
            if (name.length() > maxChars) {
                name = name.substring(0, Math.max(1, maxChars - 3)) + "...";
            }
            g2.drawString(name, x + 32, y + (fontSize / 3));

            g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(9, fontSize - 1)));
            g2.setColor(new Color(107, 114, 128));
            String detail = item.subtitle() != null ? item.subtitle() : layerTypeLabel(layer);
            if (detail.length() > maxChars) {
                detail = detail.substring(0, Math.max(1, maxChars - 3)) + "...";
            }
            g2.drawString(detail, x + 32, y + fontSize + 2);
        }

        private static List<LegendItem> buildLegendItems(List<Layer> layers) {
            List<LegendItem> automaticItems = buildAutomaticLegendItems(layers);
            if (automaticItems.isEmpty()) {
                return automaticItems;
            }

            List<CatmapLegendItem> automaticEntries = new ArrayList<>();
            java.util.Map<String, LegendItem> automaticByKey = new java.util.LinkedHashMap<>();
            for (LegendItem item : automaticItems) {
                automaticEntries.add(new CatmapLegendItem(
                        item.key(),
                        item.label(),
                        item.subtitle(),
                        CatmapLegendSupport.isLegendVisibleByDefault(item.layer())
                ));
                automaticByKey.put(item.key(), item);
            }

            List<CatmapLegendItem> configuredEntries = CatmapLegendSupport.mergeEntries(
                    automaticEntries,
                    ctxProject() != null ? ctxProject().getCatmapLegendItems() : null
            );

            List<LegendItem> mergedItems = new ArrayList<>();
            for (CatmapLegendItem configured : configuredEntries) {
                if (configured == null || !configured.isVisible()) {
                    continue;
                }
                LegendItem automatic = automaticByKey.get(configured.getKey());
                if (automatic == null) {
                    continue;
                }
                mergedItems.add(new LegendItem(
                        automatic.key(),
                        !configured.getLabel().isBlank() ? configured.getLabel() : automatic.label(),
                        !configured.getSubtitle().isBlank() ? configured.getSubtitle() : automatic.subtitle(),
                        automatic.layer(),
                        automatic.categoryRule(),
                        automatic.geometryType()
                ));
            }
            return mergedItems;
        }

        private static List<LegendItem> buildAutomaticLegendItems(List<Layer> layers) {
            List<LegendItem> items = new ArrayList<>();
            if (layers == null) {
                return items;
            }
            for (Layer layer : layers) {
                if (layer == null) {
                    continue;
                }
                if (layer.getPointCategorizedSymbology().isConfigured()) {
                    for (CategoryStyleRule rule : layer.getPointCategorizedSymbology().getRules().values()) {
                        items.add(new LegendItem(
                                CatmapLegendSupport.buildKey(layer, rule, "POINT"),
                                rule.getValue(),
                                layer.getName(),
                                layer,
                                rule,
                                "POINT"
                        ));
                    }
                    continue;
                }
                if (layer.getLineCategorizedSymbology().isConfigured()) {
                    for (CategoryStyleRule rule : layer.getLineCategorizedSymbology().getRules().values()) {
                        items.add(new LegendItem(
                                CatmapLegendSupport.buildKey(layer, rule, "LINE"),
                                rule.getValue(),
                                layer.getName(),
                                layer,
                                rule,
                                "LINE"
                        ));
                    }
                    continue;
                }
                if (layer.getPolygonCategorizedSymbology().isConfigured()) {
                    for (CategoryStyleRule rule : layer.getPolygonCategorizedSymbology().getRules().values()) {
                        items.add(new LegendItem(
                                CatmapLegendSupport.buildKey(layer, rule, "POLYGON"),
                                rule.getValue(),
                                layer.getName(),
                                layer,
                                rule,
                                "POLYGON"
                        ));
                    }
                    continue;
                }
                String geometryType = CatmapLegendSupport.resolveLegendGeometryType(layer);
                items.add(new LegendItem(
                        CatmapLegendSupport.buildKey(layer, null, geometryType),
                        layer.getName(),
                        CatmapLegendSupport.resolveLayerTypeLabel(layer),
                        layer,
                        null,
                        geometryType
                ));
            }
            return items;
        }

        private static void drawLegendItem(Graphics2D g2, LegendItem item, int x, int y, int availableWidth) {
            Layer layer = item.layer();
            ShapefileData data = ctxMapPanel() != null ? ctxMapPanel().getShapefileData(layer) : null;
            String geometryFamily = VectorLayerUtils.resolveGeometryFamily(data);
            if (layer instanceof RasterLayer || layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
                g2.setPaint(new GradientPaint(x, y, new Color(96, 165, 250), x + 18, y + 18, new Color(59, 130, 246)));
                g2.fillRect(x, y - 12, 20, 16);
                g2.setColor(new Color(30, 41, 59));
                g2.drawRect(x, y - 12, 20, 16);
            } else if ("POINT".equalsIgnoreCase(item.geometryType())) {
                drawPointSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("LINE".equalsIgnoreCase(item.geometryType())) {
                drawLineSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("POLYGON".equalsIgnoreCase(item.geometryType())) {
                drawPolygonSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("POINT".equalsIgnoreCase(geometryFamily)) {
                drawPointSymbolPreview(g2, layer, x, y, null);
            } else if ("LINE".equalsIgnoreCase(geometryFamily)) {
                drawLineSymbolPreview(g2, layer, x, y, null);
            } else {
                drawPolygonSymbolPreview(g2, layer, x, y, null);
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(37, 45, 58));
            String name = item.label() != null ? item.label() : "Capa";
            int labelWidth = Math.max(60, availableWidth - 30);
            if (name.length() > 34) {
                name = name.substring(0, 31) + "...";
            }
            g2.drawString(name, x + 30, y - 1);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(107, 114, 128));
            String detail = item.subtitle() != null ? item.subtitle() : layerTypeLabel(layer);
            if (detail.length() > labelWidth / 6) {
                detail = detail.substring(0, Math.max(0, labelWidth / 6 - 3)) + "...";
            }
            g2.drawString(detail, x + 30, y + 13);
        }

        private static FooterRenderResult drawFooter(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, int width, int height, int margin, int footerHeight, MapFrameGeometry mapFrame, LayoutInteractionState interactionState, BufferedImage layoutImage, int renderDpi) {
            int top = height - margin - footerHeight;
            boolean showCartouche = isRenderableElementVisible(interactionState, LayoutElementType.CARTOUCHE);
            boolean showProfileImage = layoutImage != null && isRenderableElementVisible(interactionState, LayoutElementType.PROFILE_IMAGE);

            boolean cleanTemplate = settings.template() == LayoutTemplate.CLEAN_CENTERED;

            if (showCartouche || showProfileImage) {
                g2.setColor(new Color(200, 208, 218));
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawLine(margin, top, width - margin, top);
                g2.setStroke(new BasicStroke(1.0f));
            }

            if (cleanTemplate && showCartouche) {
                java.awt.FontMetrics baseMetrics = g2.getFontMetrics(g2.getFont().deriveFont(Font.PLAIN, Math.max(11, width / 130)));
                int lineHeight = baseMetrics.getHeight() + 2;

                int logoAreaW = 0;
                BufferedImage logoImage = loadImageAsset(settings.logoPath());
                if (logoImage != null) {
                    int logoMaxH = footerHeight - 18;
                    double s = Math.min(1d, logoMaxH / (double) Math.max(1, logoImage.getHeight()));
                    int logoW = Math.max(1, (int) Math.round(logoImage.getWidth() * s));
                    int logoH = Math.max(1, (int) Math.round(logoImage.getHeight() * s));
                    int logoX = width - margin - logoW;
                    int logoY = top + (footerHeight - logoH) / 2;
                    g2.drawImage(logoImage, logoX, logoY, logoW, logoH, null);
                    logoAreaW = logoW + 14;
                }

                g2.setColor(new Color(27, 38, 56));
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, width / 120)));
                g2.drawString("Datos cartograficos", margin, top + 18);

                g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(11, width / 130)));
                int rowY = top + 20 + lineHeight;

                int col1x = margin;
                int availW = (width - (margin * 2) - logoAreaW);
                int colW3 = availW / 3;
                int col2x = margin + colW3;
                int col3x = margin + colW3 * 2;

                String projName = blankOr(settings.cartoucheProjectName(), snapshot.projectName());
                drawCompactFooterRow(g2, "Estudio", blankOr(settings.studyName(), projName), col1x, rowY, colW3 - 6);
                drawCompactFooterRow(g2, "Proyecto", projName, col2x, rowY, colW3 - 6);
                String genText = "Fecha: " + FOOTER_DATE.format(LocalDateTime.now());
                drawCompactFooterRow(g2, genText, "", col3x, rowY, colW3 - 6);
                rowY += lineHeight + 2;

                drawCompactFooterRow(g2, "Empresa", blankOr(settings.companyName(), "No especificada"), col1x, rowY, colW3 - 6);
                drawCompactFooterRow(g2, "Cartografo", blankOr(settings.cartographerName(), "No especificado"), col2x, rowY, colW3 - 6);
                double exactDenominator = estimateScaleDenominator(mapFrame, renderDpi);
                String scaleText = settings.showScale()
                        ? "Escala: " + (exactDenominator > 0 ? formatScaleDenominator(exactDenominator) : snapshot.scaleLabel())
                        : "Escala: —";
                drawCompactFooterRow(g2, scaleText, "", col3x, rowY, colW3 - 6);
                rowY += lineHeight + 2;

                drawCompactFooterRow(g2, "Fuente", blankOr(settings.imageSource(), "Vista actual del proyecto"), col1x, rowY, colW3 - 6);
                drawCompactFooterRow(g2, "CRS", blankOr(settings.coordinateReference(), snapshot.projectCrsLabel()), col2x, rowY, colW3 - 6);
                drawCompactFooterRow(g2, "Generado en CATGIS Desktop", "", col3x, rowY, colW3 - 6);

                return new FooterRenderResult(
                        new Rectangle(margin, top + 4, width - (margin * 2), footerHeight - 12),
                        null
                );
            }

            int baseCartoucheWidth = switch (settings.template()) {
                case CLEAN_CENTERED -> Math.min(width / 2, 420);
                case STRONG_CARTOUCHE -> Math.min((int) (width * 0.58d), 660);
                default -> Math.min(width / 2, 520);
            };
            Rectangle cartoucheBounds = showCartouche ? applyElementAdjustment(
                    new Rectangle(margin, top + 14, baseCartoucheWidth, footerHeight - 24),
                    interactionState,
                    LayoutElementType.CARTOUCHE
            ) : null;
            if (showCartouche) {
                drawCartouche(g2, settings, snapshot, cartoucheBounds);
            }

            int infoX = cartoucheBounds != null ? cartoucheBounds.x + cartoucheBounds.width + 26 : margin;
            if (showCartouche) {
                g2.setColor(new Color(37, 45, 58));
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, width / 108)));
                String footer = !settings.footer().isBlank() ? settings.footer() : "Generado desde CATGIS Desktop";
                g2.drawString(footer, infoX, top + 34);

                g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(12, width / 115)));
                g2.setColor(new Color(99, 110, 124));
                String reference = "Proyecto: " + snapshot.projectName() + " | CRS: " + snapshot.projectCrsLabel();
                g2.drawString(reference, infoX, top + 54);

                java.awt.FontMetrics metrics = g2.getFontMetrics();
                String generation = "Fecha de salida: " + FOOTER_DATE.format(LocalDateTime.now());
                g2.drawString(generation, width - margin - metrics.stringWidth(generation), top + 34);

                double exactDenominator = estimateScaleDenominator(mapFrame, renderDpi);
                String scale = settings.showScale()
                        ? "Escala tecnica: " + (exactDenominator > 0 ? formatScaleDenominator(exactDenominator) : snapshot.scaleLabel())
                        : "Escala grafica oculta";
                g2.drawString(scale, width - margin - metrics.stringWidth(scale), top + 54);
            }
            Rectangle profileImageBounds = null;
            if (showProfileImage) {
                Rectangle baseImageBounds = new Rectangle(
                        infoX,
                        top + 86,
                        Math.max(200, width - margin - infoX),
                        Math.max(110, footerHeight - 104)
                );
                profileImageBounds = applyElementAdjustment(baseImageBounds, interactionState, LayoutElementType.PROFILE_IMAGE);
                drawLayoutImage(g2, profileImageBounds, layoutImage);
            }
            return new FooterRenderResult(cartoucheBounds, profileImageBounds);
        }

        private static void drawCompactFooterRow(Graphics2D g2, String label, String value, int x, int y, int maxWidth) {
            g2.setColor(new Color(58, 68, 84));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD));
            String line;
            if (value != null && !value.isBlank()) {
                line = label + ": " + clipText(value, Math.max(8, maxWidth / 8));
            } else {
                line = label;
            }
            java.awt.FontMetrics fm = g2.getFontMetrics();
            if (fm.stringWidth(line) > maxWidth) {
                line = clipText(line, Math.max(4, (int) Math.round(maxWidth / (fm.stringWidth("W") + 1))));
            }
            g2.drawString(line, x, y + fm.getAscent());
        }

        private static void drawCartouche(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, Rectangle bounds) {
            g2.setColor(new Color(248, 250, 253));
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);
            g2.setColor(new Color(201, 210, 222));
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);

            java.awt.Shape clip = g2.getClip();
            g2.clip(new java.awt.geom.RoundRectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18));

            int contentX = bounds.x + 16;
            int contentY = bounds.y + 18;
            int textX = contentX;
            BufferedImage logoImage = loadImageAsset(settings.logoPath());
            if (logoImage != null) {
                int logoBoxW = 84;
                int logoBoxH = Math.min(bounds.height - 28, 72);
                int logoX = contentX;
                int logoY = bounds.y + 16;
                double scale = Math.min(logoBoxW / (double) logoImage.getWidth(), logoBoxH / (double) logoImage.getHeight());
                int drawW = Math.max(1, (int) Math.round(logoImage.getWidth() * scale));
                int drawH = Math.max(1, (int) Math.round(logoImage.getHeight() * scale));
                g2.drawImage(logoImage, logoX, logoY + (logoBoxH - drawH) / 2, drawW, drawH, null);
                textX += logoBoxW + 14;
            }

            int fontTitleSize = bounds.height >= 160 ? 13 : 11;
            int fontRowSize = bounds.height >= 160 ? 11 : 10;
            int rowSpacing = bounds.height >= 160 ? 16 : 13;

            g2.setColor(new Color(27, 38, 56));
            g2.setFont(new Font("SansSerif", Font.BOLD, fontTitleSize));
            g2.drawString("Datos cartograficos", textX, contentY);

            g2.setFont(new Font("SansSerif", Font.PLAIN, fontRowSize));
            g2.setColor(new Color(86, 96, 110));
            int rowY = contentY + 20;
            drawCartoucheRowScaled(g2, "Estudio", blankOr(settings.studyName(), snapshot.projectName()), textX, rowY, fontRowSize);
            rowY += rowSpacing;
            drawCartoucheRowScaled(g2, "Proyecto", blankOr(settings.cartoucheProjectName(), snapshot.projectName()), textX, rowY, fontRowSize);
            rowY += rowSpacing;
            drawCartoucheRowScaled(g2, "Empresa", blankOr(settings.companyName(), "No especificada"), textX, rowY, fontRowSize);
            rowY += rowSpacing;
            drawCartoucheRowScaled(g2, "Cartografo", blankOr(settings.cartographerName(), "No especificado"), textX, rowY, fontRowSize);
            rowY += rowSpacing;
            drawCartoucheRowScaled(g2, "Fuente", blankOr(settings.imageSource(), "Vista actual del proyecto"), textX, rowY, fontRowSize);
            rowY += rowSpacing;
            drawCartoucheRowScaled(g2, "Coord.", blankOr(settings.coordinateReference(), snapshot.projectCrsLabel()), textX, rowY, fontRowSize);

            g2.setClip(clip);
        }

        private static void drawCartoucheRowScaled(Graphics2D g2, String label, String value, int x, int y, int fontSize) {
            g2.setColor(new Color(28, 38, 54));
            g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            g2.drawString(label + ":", x, y);
            g2.setColor(new Color(86, 96, 110));
            g2.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
            int maxChars = fontSize >= 11 ? 38 : 30;
            int offsetX = fontSize >= 11 ? 62 : 54;
            g2.drawString(clipText(value, maxChars), x + offsetX, y);
        }

        private static void drawCartoucheRow(Graphics2D g2, String label, String value, int x, int y) {
            drawCartoucheRowScaled(g2, label, value, x, y, 11);
        }

        private static void drawLayoutImage(Graphics2D g2, Rectangle bounds, BufferedImage image) {
            if (bounds == null || image == null) {
                return;
            }
            g2.setColor(new Color(248, 250, 253));
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);
            g2.setColor(new Color(201, 210, 222));
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);

            g2.setColor(new Color(27, 38, 56));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("Perfil / imagen cartografica", bounds.x + 14, bounds.y + 18);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(96, 105, 118));
            g2.drawString("Movelo o redimensionalo desde el layout", bounds.x + 14, bounds.y + 31);

            int innerX = bounds.x + 12;
            int innerY = bounds.y + 40;
            int innerW = Math.max(40, bounds.width - 24);
            int innerH = Math.max(40, bounds.height - 52);
            g2.setColor(Color.WHITE);
            g2.fillRect(innerX, innerY, innerW, innerH);
            g2.setColor(new Color(214, 220, 228));
            g2.drawRect(innerX, innerY, innerW, innerH);

            double scale = Math.min(innerW / (double) Math.max(1, image.getWidth()), innerH / (double) Math.max(1, image.getHeight()));
            int drawW = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int drawH = Math.max(1, (int) Math.round(image.getHeight() * scale));
            int drawX = innerX + (innerW - drawW) / 2;
            int drawY = innerY + (innerH - drawH) / 2;
            g2.drawImage(image, drawX, drawY, drawW, drawH, null);
        }

        private static void drawCatmapItems(Graphics2D g2, List<CatmapLayoutItem> items, java.util.Map<String, Rectangle> customItemBounds) {
            if (items == null || items.isEmpty()) {
                return;
            }
            for (CatmapLayoutItem item : items) {
                if (item == null || !item.isVisible()) {
                    continue;
                }
                Rectangle bounds = new Rectangle(item.getX(), item.getY(), Math.max(24, item.getWidth()), Math.max(24, item.getHeight()));
                customItemBounds.put(item.getId(), bounds);
                switch (item.getKind()) {
                    case TEXT -> drawCatmapText(g2, item, bounds);
                    case IMAGE -> drawCatmapImage(g2, item, bounds);
                    case RECTANGLE -> drawCatmapRectangle(g2, item, bounds);
                    case ELLIPSE -> drawCatmapEllipse(g2, item, bounds);
                    case LINE -> drawCatmapLine(g2, item, bounds);
                }
                if (item.isLocked()) {
                    drawCatmapLockBadge(g2, bounds);
                }
            }
        }

        private static void drawCatmapLockBadge(Graphics2D g2, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                int badgeSize = 18;
                int badgeX = bounds.x + Math.max(4, bounds.width - badgeSize - 4);
                int badgeY = bounds.y + 4;
                copy.setColor(new Color(30, 41, 59, 210));
                copy.fillRoundRect(badgeX, badgeY, badgeSize, badgeSize, 8, 8);
                copy.setColor(Color.WHITE);
                copy.setFont(new Font("SansSerif", Font.BOLD, 10));
                FontMetrics metrics = copy.getFontMetrics();
                String text = "B";
                int tx = badgeX + (badgeSize - metrics.stringWidth(text)) / 2;
                int ty = badgeY + ((badgeSize - metrics.getHeight()) / 2) + metrics.getAscent();
                copy.drawString(text, tx, ty);
            } finally {
                copy.dispose();
            }
        }

        private static void drawCatmapText(Graphics2D g2, CatmapLayoutItem item, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                Color fill = item.getFillColor();
                if (fill.getAlpha() > 0) {
                    copy.setColor(fill);
                    copy.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
                }
                copy.setColor(item.getTextColor());
                int style = Font.PLAIN;
                if (item.isBold()) {
                    style |= Font.BOLD;
                }
                if (item.isItalic()) {
                    style |= Font.ITALIC;
                }
                copy.setFont(new Font("SansSerif", style, item.getFontSize()));
                copy.setClip(bounds.x + 6, bounds.y + 6, Math.max(12, bounds.width - 12), Math.max(12, bounds.height - 12));
                FontMetrics metrics = copy.getFontMetrics();
                List<String> lines = wrapText(item.getText().isBlank() ? item.getLabel() : item.getText(), metrics, Math.max(40, bounds.width - 12));
                int lineHeight = metrics.getHeight();
                int textY = bounds.y + 8 + metrics.getAscent();
                for (String line : lines) {
                    int drawX = switch (item.getAlign()) {
                        case CENTER -> bounds.x + Math.max(6, (bounds.width - metrics.stringWidth(line)) / 2);
                        case RIGHT -> bounds.x + Math.max(6, bounds.width - metrics.stringWidth(line) - 8);
                        default -> bounds.x + 8;
                    };
                    copy.drawString(line, drawX, textY);
                    textY += lineHeight;
                    if (textY > bounds.y + bounds.height - 4) {
                        break;
                    }
                }
            } finally {
                copy.dispose();
            }
        }

        private static void drawCatmapImage(Graphics2D g2, CatmapLayoutItem item, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(new Color(248, 250, 253));
                copy.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
                copy.setColor(new Color(203, 213, 225));
                copy.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
                BufferedImage image = loadImageAsset(item.getImagePath());
                if (image == null) {
                    copy.setColor(new Color(100, 116, 139));
                    copy.setFont(new Font("SansSerif", Font.BOLD, 12));
                    copy.drawString(item.getLabel().isBlank() ? "Imagen" : item.getLabel(), bounds.x + 10, bounds.y + 18);
                    copy.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    copy.drawString("Selecciona un archivo valido", bounds.x + 10, bounds.y + 34);
                    return;
                }
                int innerBoxX = bounds.x + 8;
                int innerBoxY = bounds.y + 8;
                int innerBoxW = Math.max(1, bounds.width - 16);
                int innerBoxH = Math.max(1, bounds.height - 16);
                double scaleValue = Math.min(innerBoxW / (double) Math.max(1, image.getWidth()), innerBoxH / (double) Math.max(1, image.getHeight()));
                int drawW = Math.max(1, (int) Math.round(image.getWidth() * scaleValue));
                int drawH = Math.max(1, (int) Math.round(image.getHeight() * scaleValue));
                int drawX = innerBoxX + Math.max(0, (innerBoxW - drawW) / 2);
                int drawY = innerBoxY + Math.max(0, (innerBoxH - drawH) / 2);
                copy.drawImage(image, drawX, drawY, drawW, drawH, null);
            } finally {
                copy.dispose();
            }
        }

        private static void drawCatmapRectangle(Graphics2D g2, CatmapLayoutItem item, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(item.getFillColor());
                if (item.getFillColor().getAlpha() > 0) {
                    copy.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }
                copy.setColor(item.getStrokeColor());
                copy.setStroke(new BasicStroke(item.getLineWidth()));
                copy.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            } finally {
                copy.dispose();
            }
        }

        private static void drawCatmapEllipse(Graphics2D g2, CatmapLayoutItem item, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                Ellipse2D ellipse = new Ellipse2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
                copy.setColor(item.getFillColor());
                if (item.getFillColor().getAlpha() > 0) {
                    copy.fill(ellipse);
                }
                copy.setColor(item.getStrokeColor());
                copy.setStroke(new BasicStroke(item.getLineWidth()));
                copy.draw(ellipse);
            } finally {
                copy.dispose();
            }
        }

        private static void drawCatmapLine(Graphics2D g2, CatmapLayoutItem item, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(item.getStrokeColor());
                copy.setStroke(new BasicStroke(item.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                copy.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
            } finally {
                copy.dispose();
            }
        }

        private static List<String> wrapText(String text, FontMetrics metrics, int maxWidth) {
            List<String> lines = new ArrayList<>();
            String content = text != null ? text : "";
            for (String paragraph : content.split("\\R", -1)) {
                String current = "";
                for (String word : paragraph.split(" ")) {
                    if (word.isBlank()) {
                        continue;
                    }
                    String candidate = current.isBlank() ? word : current + " " + word;
                    if (!current.isBlank() && metrics.stringWidth(candidate) > maxWidth) {
                        lines.add(current);
                        current = word;
                    } else {
                        current = candidate;
                    }
                }
                if (!current.isBlank()) {
                    lines.add(current);
                } else if (paragraph.isBlank()) {
                    lines.add("");
                }
            }
            if (lines.isEmpty()) {
                lines.add("");
            }
            return lines;
        }

        private static BufferedImage loadImageAsset(String path) {
            if (path == null || path.isBlank()) {
                return null;
            }
            try {
                File file = new File(path);
                if (!file.isFile()) {
                    return null;
                }
                return ImageIO.read(file);
            } catch (Exception ex) {
                return null;
            }
        }

        private static String blankOr(String primary, String fallback) {
            return primary != null && !primary.isBlank() ? primary : fallback;
        }

        private static String clipText(String value, int max) {
            if (value == null) {
                return "";
            }
            return value.length() > max ? value.substring(0, Math.max(0, max - 3)) + "..." : value;
        }

        private static Color colorOr(Color color, Color fallback) {
            return color != null ? color : fallback;
        }

        private static double estimateScaleDenominator(MapFrameGeometry mapFrame, int renderDpi) {
            if (mapFrame == null || mapFrame.shownGroundMeters() <= 0 || renderDpi <= 0) {
                return 0;
            }
            double shownGroundMeters = mapFrame.shownGroundMeters();
            double mapWidthMetersOnPaper = (mapFrame.imageBounds().width / (double) renderDpi) * 0.0254d;
            if (mapWidthMetersOnPaper <= 0) {
                return 0;
            }
            return shownGroundMeters / mapWidthMetersOnPaper;
        }

        private static double convertWorldWidthToMeters(LayoutSnapshot snapshot, double worldWidthUnits, double centerY) {
            if (snapshot == null || worldWidthUnits <= 0) {
                return 0d;
            }
            String projectCrs = CRSDefinitions.normalizeCode(snapshot.projectCrsCode());
            if (isGeographicCrs(projectCrs)) {
                double metersPerDegreeLon = 111320d * Math.cos(Math.toRadians(centerY));
                metersPerDegreeLon = Math.max(0.0001d, Math.abs(metersPerDegreeLon));
                return worldWidthUnits * metersPerDegreeLon;
            }
            return worldWidthUnits;
        }

        private static boolean isGeographicCrs(String projectCrs) {
            return "EPSG:4326".equalsIgnoreCase(projectCrs)
                    || "EPSG:4258".equalsIgnoreCase(projectCrs)
                    || "EPSG:4269".equalsIgnoreCase(projectCrs)
                    || "EPSG:4674".equalsIgnoreCase(projectCrs)
                    || "EPSG:4190".equalsIgnoreCase(projectCrs)
                    || "EPSG:4221".equalsIgnoreCase(projectCrs);
        }

        private static void drawPointSymbolPreview(Graphics2D g2, Layer layer, int x, int y, CategoryStyleRule categoryRule) {
            Color color = colorOr(categoryRule != null ? categoryRule.getPrimaryColor() : layer.getPointColor(), new Color(59, 130, 246));
            int left = x + 3;
            int top = y - 11;
            int size = Math.max(12, categoryRule != null ? categoryRule.getPointSize() + 2 : 12);
            String catId = categoryRule != null ? categoryRule.getCatalogSymbolId() : layer.getCatalogSymbolId();
            if (catId != null && !catId.isEmpty() && !"circle".equals(catId)) {
                PointSymbolCatalog.render(g2, catId, left + size/2, top + size/2, size + 2, color, color.darker(), 1.2f);
                return;
            }
            if (categoryRule == null && PointGraphicSymbolSupport.paintLayerSymbol(g2, layer, left + (size / 2), top + (size / 2), 18)) {
                return;
            }
            Layer.PointSymbolStyle style = categoryRule != null ? categoryRule.getPointSymbolStyle() : layer.getPointSymbolStyle();
            if (style == null) {
                style = Layer.PointSymbolStyle.CIRCLE;
            }
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(color);
                switch (style) {
                    case SQUARE -> copy.fillRect(left, top, size, size);
                    case DIAMOND -> {
                        Path2D diamond = new Path2D.Double();
                        diamond.moveTo(left + (size / 2d), top);
                        diamond.lineTo(left + size, top + (size / 2d));
                        diamond.lineTo(left + (size / 2d), top + size);
                        diamond.lineTo(left, top + (size / 2d));
                        diamond.closePath();
                        copy.fill(diamond);
                        copy.setColor(new Color(33, 33, 33));
                        copy.draw(diamond);
                        return;
                    }
                    case TRIANGLE -> {
                        Path2D triangle = new Path2D.Double();
                        triangle.moveTo(left + (size / 2d), top);
                        triangle.lineTo(left + size, top + size);
                        triangle.lineTo(left, top + size);
                        triangle.closePath();
                        copy.fill(triangle);
                        copy.setColor(new Color(33, 33, 33));
                        copy.draw(triangle);
                        return;
                    }
                    case TARGET -> {
                        copy.fillOval(left, top, size, size);
                        copy.setColor(Color.WHITE);
                        copy.fillOval(left + 3, top + 3, 6, 6);
                        copy.setColor(new Color(33, 33, 33));
                        copy.drawOval(left, top, size, size);
                        copy.drawLine(left - 1, top + (size / 2), left + size + 1, top + (size / 2));
                        copy.drawLine(left + (size / 2), top - 1, left + (size / 2), top + size + 1);
                        return;
                    }
                    case PIN -> {
                        Path2D pin = new Path2D.Double();
                        pin.moveTo(left + size / 2d, top + size + 2);
                        pin.lineTo(left + size, top + 4);
                        pin.quadTo(left + size + 1, top - 2, left + size / 2d, top);
                        pin.quadTo(left - 1, top - 2, left, top + 4);
                        pin.closePath();
                        copy.fill(pin);
                        copy.setColor(Color.WHITE);
                        copy.fillOval(left + 3, top + 3, 5, 5);
                        copy.setColor(new Color(33, 33, 33));
                        copy.draw(pin);
                        return;
                    }
                    case FLAG -> {
                        copy.setStroke(new BasicStroke(1.5f));
                        copy.drawLine(left + 2, top + 12, left + 2, top);
                        Path2D flag = new Path2D.Double();
                        flag.moveTo(left + 2, top + 1);
                        flag.lineTo(left + 10, top + 3);
                        flag.lineTo(left + 2, top + 6);
                        flag.closePath();
                        copy.fill(flag);
                        return;
                    }
                    case STAR -> {
                        Path2D star = buildStar(left + 6, top + 6, 6, 3);
                        copy.fill(star);
                        copy.setColor(new Color(33, 33, 33));
                        copy.draw(star);
                        return;
                    }
                    case WELL -> {
                        copy.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        Path2D derrick = new Path2D.Double();
                        derrick.moveTo(left + (size / 2d), top);
                        derrick.lineTo(left + size - 1, top + size);
                        derrick.lineTo(left + 1, top + size);
                        derrick.closePath();
                        copy.draw(derrick);
                        copy.drawLine(left + 2, top + size, left + size - 2, top + size);
                        copy.drawLine(left + 3, top + 6, left + size - 3, top + 6);
                        copy.drawLine(left + 3, top + size - 1, left + size / 2, top + 6);
                        copy.drawLine(left + size - 3, top + size - 1, left + size / 2, top + 6);
                        return;
                    }
                    default -> copy.fillOval(left, top, size, size);
                }
                copy.setColor(new Color(33, 33, 33));
                if (style == Layer.PointSymbolStyle.SQUARE) {
                    copy.drawRect(left, top, size, size);
                } else {
                    copy.drawOval(left, top, size, size);
                }
            } finally {
                copy.dispose();
            }
        }

        private static void drawLineSymbolPreview(Graphics2D g2, Layer layer, int x, int y, CategoryStyleRule categoryRule) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(colorOr(categoryRule != null ? categoryRule.getPrimaryColor() : layer.getLineColor(), new Color(16, 185, 129)));
                float previewWidth = categoryRule != null ? categoryRule.getLineWidth() : layer.getLineWidth();
                copy.setStroke(buildLineStroke(Math.max(1.8f, previewWidth), categoryRule != null ? categoryRule.getLineStyle() : layer.getLineSymbolStyle()));
                copy.drawLine(x, y - 4, x + 20, y - 4);
            } finally {
                copy.dispose();
            }
        }

        private static void drawPolygonSymbolPreview(Graphics2D g2, Layer layer, int x, int y, CategoryStyleRule categoryRule) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                Rectangle bounds = new Rectangle(x, y - 12, 20, 16);
                Layer.PolygonFillStyle style = categoryRule != null
                        ? categoryRule.getPolygonFillStyle()
                        : layer.getPolygonFillStyle() != null ? layer.getPolygonFillStyle() : Layer.PolygonFillStyle.SOLID;
                if (style != Layer.PolygonFillStyle.OUTLINE_ONLY) {
                    copy.setPaint(buildPolygonPreviewPaint(layer, bounds, categoryRule));
                    copy.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }
                copy.setPaint(null);
                copy.setColor(colorOr(categoryRule != null ? categoryRule.getSecondaryColor() : layer.getBorderColor(), new Color(146, 64, 14)));
                copy.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            } finally {
                copy.dispose();
            }
        }

        private static Paint buildPolygonPreviewPaint(Layer layer, Rectangle bounds, CategoryStyleRule categoryRule) {
            Color fill = colorOr(categoryRule != null ? categoryRule.getPrimaryColor() : layer.getFillColor(), new Color(251, 191, 36));
            Layer.PolygonFillStyle style = categoryRule != null
                    ? categoryRule.getPolygonFillStyle()
                    : layer.getPolygonFillStyle() != null ? layer.getPolygonFillStyle() : Layer.PolygonFillStyle.SOLID;
            if (style == Layer.PolygonFillStyle.SOLID) {
                return fill;
            }

            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            try {
                g.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 60));
                g.fillRect(0, 0, 10, 10);
                g.setColor(colorOr(categoryRule != null ? categoryRule.getSecondaryColor() : layer.getBorderColor(), new Color(146, 64, 14)));
                switch (style) {
                    case DIAGONAL_HATCH -> {
                        g.drawLine(-2, 9, 9, -2);
                        g.drawLine(2, 11, 11, 2);
                    }
                    case CROSS_HATCH -> {
                        g.drawLine(0, 5, 10, 5);
                        g.drawLine(5, 0, 5, 10);
                    }
                    case DOTS -> {
                        g.fillOval(2, 2, 2, 2);
                        g.fillOval(6, 6, 2, 2);
                    }
                    default -> {
                    }
                }
            } finally {
                g.dispose();
            }
            return new java.awt.TexturePaint(img, bounds);
        }

        private static BasicStroke buildLineStroke(float width, Layer.LineSymbolStyle style) {
            Layer.LineSymbolStyle resolved = style != null ? style : Layer.LineSymbolStyle.SOLID;
            return switch (resolved) {
                case DASHED -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{8f, 5f}, 0f);
                case DOTTED -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{2f, 5f}, 0f);
                case DASH_DOT -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{9f, 4f, 2f, 4f}, 0f);
                default -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            };
        }

        private static Path2D buildStar(double cx, double cy, double outer, double inner) {
            Path2D path = new Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double radius = i % 2 == 0 ? outer : inner;
                double angle = Math.toRadians(-90 + (i * 36));
                double x = cx + Math.cos(angle) * radius;
                double y = cy + Math.sin(angle) * radius;
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            path.closePath();
            return path;
        }

        private static String layerTypeLabel(Layer layer) {
            if (layer instanceof OnlineTileLayer) {
                return "Mapa base online";
            }
            if (layer instanceof OnlineWmsLayer) {
                return "WMS";
            }
            if (layer instanceof RasterLayer) {
                return TopographyWorkflowSupport.isDemLikeRaster(layer) ? "DEM raster" : "Raster";
            }
            if (layer instanceof OnlineWfsLayer) {
                return "WFS";
            }
            if (layer instanceof PostgisLayer) {
                return "PostGIS";
            }
            if (layer instanceof GeoPackageLayer) {
                return "GeoPackage";
            }
            if (layer instanceof GpxLayer gpxLayer) {
                return "GPX " + gpxLayer.getContentKind().getLabel();
            }
            String geometryFamily = VectorLayerUtils.resolveGeometryFamily(
                    ctxMapPanel() != null ? ctxMapPanel().getShapefileData(layer) : null
            );
            if ("POINT".equalsIgnoreCase(geometryFamily)) {
                return "Punto";
            }
            if ("LINE".equalsIgnoreCase(geometryFamily)) {
                return "Linea";
            }
            if ("POLYGON".equalsIgnoreCase(geometryFamily)) {
                return "Poligono";
            }
            String type = layer.getType();
            if (type == null || type.isBlank()) {
                return "Vectorial";
            }
            return type;
        }

        private static double chooseRoundedDistance(double targetMeters) {
            if (targetMeters <= 0) {
                return 0;
            }
            double exponent = Math.pow(10, Math.floor(Math.log10(targetMeters)));
            double normalized = targetMeters / exponent;
            double rounded;
            if (normalized < 1.5d) {
                rounded = 1d;
            } else if (normalized < 3.5d) {
                rounded = 2d;
            } else if (normalized < 7.5d) {
                rounded = 5d;
            } else {
                rounded = 10d;
            }
            return rounded * exponent;
        }
    }

    private static class ImagePrintable implements Printable {
        private final BufferedImage image;

        private ImagePrintable(BufferedImage image) {
            this.image = image;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0 || image == null) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                double availableWidth = pageFormat.getImageableWidth();
                double availableHeight = pageFormat.getImageableHeight();
                double scale = Math.min(availableWidth / image.getWidth(), availableHeight / image.getHeight());
                AffineTransform transform = new AffineTransform();
                transform.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                transform.scale(scale, scale);
                g2.drawImage(image, transform, null);
                return PAGE_EXISTS;
            } finally {
                g2.dispose();
            }
        }
    }

    private void drawLayoutModelOverlay(Graphics2D g2, LayoutSettings settings, int pageX, int pageY, double scale) {
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

    private RectMm toPageRectMm() {
        if (previewPanel.lastRenderResult == null || previewPanel.lastPageBounds == null || previewPanel.lastPreviewScale <= 0) return null;
        LayoutSettings settings = buildSettings();
        PageSizePreset ps = settings.pageSize();
        double wMm = ps.widthMm;
        double hMm = ps.heightMm;
        if (settings.orientation() == PageOrientation.LANDSCAPE) { double tmp = wMm; wMm = hMm; hMm = tmp; }
        return new RectMm(0, 0, wMm, hMm, 25.4 / PREVIEW_RENDER_DPI);
    }

    private static class RectMm { final double xMm, yMm, wMm, hMm, pxToMmScale; RectMm(double x, double y, double w, double h, double s) { xMm = x; yMm = y; wMm = w; hMm = h; pxToMmScale = s; } }

    private JPanel buildPropertiesPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setBackground(new Color(0xF7F8FA));
        panel.setPreferredSize(new Dimension(230, 100));
        JLabel hdr = new JLabel("Propiedades");
        hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 11f));
        hdr.setForeground(new Color(0x333333));
        panel.add(hdr, BorderLayout.NORTH);

        // Page section
        JPanel pageSection = new JPanel(new BorderLayout(2, 2));
        pageSection.setOpaque(false);
        JLabel pageLbl = new JLabel("Pagina");
        pageLbl.setFont(pageLbl.getFont().deriveFont(Font.BOLD, 10f));
        pageSection.add(pageLbl, BorderLayout.NORTH);
        JPanel pageControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        pageControls.setOpaque(false);
        pageControls.add(new JLabel("Tamano:"));
        pageSizeCombo.setPreferredSize(new Dimension(120, 22));
        pageControls.add(pageSizeCombo);
        pageControls.add(orientationCombo);
        pageSection.add(pageControls, BorderLayout.CENTER);

        // Template section
        JPanel tmplRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        tmplRow.setOpaque(false);
        tmplRow.add(new JLabel("Plantilla:"));
        templateCombo.setPreferredSize(new Dimension(150, 22));
        tmplRow.add(templateCombo);

        JPanel headerArea = new JPanel(new BorderLayout(0, 2));
        headerArea.setOpaque(false);
        headerArea.add(pageSection, BorderLayout.NORTH);
        headerArea.add(tmplRow, BorderLayout.SOUTH);
        panel.add(headerArea, BorderLayout.NORTH);

        JSeparator sep = new JSeparator();
        headerArea.add(sep, BorderLayout.SOUTH);

        // Card panel for element properties
        propertiesCardPanel = new JPanel(new java.awt.CardLayout());
        propertiesCardPanel.setOpaque(false);

        // Generic card
        propertiesInfoLabel = new JLabel("<html>Sin elemento<br>seleccionado</html>");
        propertiesInfoLabel.setFont(propertiesInfoLabel.getFont().deriveFont(Font.PLAIN, 10f));
        propertiesInfoLabel.setForeground(new Color(0x888888));
        JPanel genericCard = new JPanel(new BorderLayout());
        genericCard.setOpaque(false);
        genericCard.add(propertiesInfoLabel, BorderLayout.NORTH);
        propertiesCardPanel.add(genericCard, "generic");

        // Legend card (built lazily)
        propertiesCardPanel.add(new JLabel("."), "legend");
        // Map card (built lazily)
        propertiesCardPanel.add(new JLabel("."), "map");
        // Label card (built lazily)
        propertiesCardPanel.add(new JLabel("."), "label");
        // Shape card (built lazily - for Image, Rectangle, Ellipse, Line, North, Scale, Table)
        propertiesCardPanel.add(new JLabel("."), "shape");

        JScrollPane sp = new JScrollPane(propertiesCardPanel);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildElementListPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.setBackground(new Color(0xF7F8FA));
        panel.setPreferredSize(new Dimension(185, 100));
        JLabel hdr = new JLabel("Elementos del layout");
        hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 11f));
        hdr.setForeground(new Color(0x333333));

        elementListModel = new DefaultListModel<>();
        JList<String> list = new JList<>(elementListModel);
        list.setFont(list.getFont().deriveFont(Font.PLAIN, 11f));
        list.setFixedCellHeight(26);
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list.setSelectionBackground(new Color(0xDBEAFE));
        list.setSelectionForeground(new Color(0x1E3A5F));
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = list.getSelectedValue();
                if (sel == null) return;
                layoutModel.clearSelection();
                for (LayoutElement el : layoutModel.getElements()) {
                    if (el.getName().equals(extractNameFromDisplay(sel))) {
                        el.setSelected(true);
                        break;
                    }
                }
                refreshPropertiesPanel();
                previewPanel.repaint();
            }
        });
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = list.getSelectedValue();
                    if (sel == null) return;
                    for (LayoutElement el : layoutModel.getElements()) {
                        if (el.getName().equals(extractNameFromDisplay(sel))) {
                            layoutModel.clearSelection();
                            el.setSelected(true);
                            centerOnElement(el);
                            refreshPropertiesPanel();
                            previewPanel.repaint();
                            break;
                        }
                    }
                }
            }
        });

        // Section: Agregar
        JLabel addSec = new JLabel("Agregar");
        addSec.setFont(addSec.getFont().deriveFont(Font.BOLD, 9f));
        addSec.setForeground(new Color(0x1976D2));

        JPopupMenu addMenu = new JPopupMenu();
        addMenu.add(menuItem("Mapa", "map"));
        addMenu.add(menuItem("Leyenda", "legend"));
        addMenu.add(menuItem("Escala grafica", "scale"));
        addMenu.add(menuItem("Norte", "north"));
        addMenu.add(menuItem("Texto", "text"));
        addMenu.add(menuItem("Imagen / Logo", "image"));
        addMenu.add(menuItem("Cartucho", "cartouche"));
        addMenu.add(menuItem("Grilla coord.", "graticule"));
        addMenu.addSeparator();
        addMenu.add(menuItem("Rectangulo", "rect"));
        addMenu.add(menuItem("Tabla (CSV)", "table"));

        JButton addBtn = new JButton("+ Elemento \u25BE");
        addBtn.setFont(addBtn.getFont().deriveFont(Font.PLAIN, 10f));
        addBtn.setMargin(new Insets(3, 8, 3, 8));
        addBtn.addActionListener(e -> addMenu.show(addBtn, 0, addBtn.getHeight()));

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        actionBar.setOpaque(false);
        actionBar.add(addBtn);
        actionBar.add(miniBtn("Duplicar", "Duplicar seleccionado (Ctrl+D)", e -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { duplicateLayoutElement(sel); refreshElementList(); previewPanel.repaint(); }
        }));
        JButton delBtn2 = miniBtn("Eliminar", "Eliminar seleccionado (Supr)", e -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { layoutModel.removeElement(sel.getId()); refreshElementList(); previewPanel.repaint(); }
        });
        delBtn2.setForeground(new Color(0xCC3333));
        actionBar.add(delBtn2);
        actionBar.add(Box.createHorizontalStrut(2));
        actionBar.add(miniBtn("\u21A9", "Deshacer (Ctrl+Z)", e -> undo()));
        actionBar.add(miniBtn("\u21AA", "Rehacer (Ctrl+Y)", e -> redo()));

        // Section: Organizar
        JLabel orgSec = new JLabel("Organizar");
        orgSec.setFont(orgSec.getFont().deriveFont(Font.BOLD, 9f));
        orgSec.setForeground(new Color(0x1976D2));

        JPopupMenu alignMenu = new JPopupMenu();
        String[] alignLabels = {"Izquierda", "Centro horizontal", "Derecha", "Arriba", "Medio vertical", "Abajo"};
        for (int i = 0; i < 6; i++) {
            final int mode = i;
            alignMenu.add(menuItem(alignLabels[i], () -> alignElements(mode)));
        }
        JButton alignBtn = new JButton("Alinear \u25BE");
        alignBtn.setFont(alignBtn.getFont().deriveFont(Font.PLAIN, 10f));
        alignBtn.setMargin(new Insets(3, 6, 3, 6));
        alignBtn.addActionListener(e -> alignMenu.show(alignBtn, 0, alignBtn.getHeight()));

        JPopupMenu orderMenu = new JPopupMenu();
        orderMenu.add(menuItem("Traer al frente", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { layoutModel.moveToFront(sel); refreshElementList(); previewPanel.repaint(); }
        }));
        orderMenu.add(menuItem("Enviar atras", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { layoutModel.moveToBack(sel); refreshElementList(); previewPanel.repaint(); }
        }));
        orderMenu.add(menuItem("Subir uno", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { layoutModel.moveUp(sel); refreshElementList(); previewPanel.repaint(); }
        }));
        orderMenu.add(menuItem("Bajar uno", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { layoutModel.moveDown(sel); refreshElementList(); previewPanel.repaint(); }
        }));
        JButton orderBtn = new JButton("Orden \u25BE");
        orderBtn.setFont(orderBtn.getFont().deriveFont(Font.PLAIN, 10f));
        orderBtn.setMargin(new Insets(3, 6, 3, 6));
        orderBtn.addActionListener(e -> orderMenu.show(orderBtn, 0, orderBtn.getHeight()));

        JPanel orgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        orgRow.setOpaque(false);
        orgRow.add(alignBtn);
        orgRow.add(orderBtn);

        // Section: Estado
        JLabel stSec = new JLabel("Estado");
        stSec.setFont(stSec.getFont().deriveFont(Font.BOLD, 9f));
        stSec.setForeground(new Color(0x1976D2));

        JPanel toggleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        toggleBar.setOpaque(false);
        toggleBar.add(miniBtn("Visible", "Mostrar/Ocultar seleccionado", e -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { sel.setVisible(!sel.isVisible()); refreshElementList(); previewPanel.repaint(); }
        }));
        toggleBar.add(miniBtn("Bloquear", "Bloquear/Desbloquear seleccionado", e -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { sel.setLocked(!sel.isLocked()); refreshElementList(); previewPanel.repaint(); }
        }));

        // Assemble top area with sections
        JPanel sectionsPanel = new JPanel();
        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.Y_AXIS));
        sectionsPanel.setOpaque(false);
        JPanel addPanel = wrapSection(addSec, actionBar);
        JPanel orgPanel = wrapSection(orgSec, orgRow);
        JPanel stPanel = wrapSection(stSec, toggleBar);
        sectionsPanel.add(addPanel);
        sectionsPanel.add(Box.createVerticalStrut(2));
        sectionsPanel.add(orgPanel);
        sectionsPanel.add(Box.createVerticalStrut(2));
        sectionsPanel.add(stPanel);

        JPanel northWrap = new JPanel(new BorderLayout(0, 3));
        northWrap.setOpaque(false);
        northWrap.add(hdr, BorderLayout.NORTH);
        northWrap.add(sectionsPanel, BorderLayout.SOUTH);

        panel.add(northWrap, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE0E0E0)));
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel wrapSection(JLabel title, JPanel content) {
        JPanel p = new JPanel(new BorderLayout(0, 1));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        p.add(title, BorderLayout.NORTH);
        p.add(content, BorderLayout.SOUTH);
        return p;
    }

    private JMenuItem menuItem(String text, String type) {
        JMenuItem mi = new JMenuItem(text);
        mi.addActionListener(e -> addQuickElement(type));
        return mi;
    }

    private JMenuItem menuItem(String text, Runnable action) {
        JMenuItem mi = new JMenuItem(text);
        mi.addActionListener(e -> action.run());
        return mi;
    }

    private double estimateMapScale() {
        try {
            ar.com.catgis.MapPanel mp = ctxMapPanel();
            if (mp != null) {
                double denom = mp.getCurrentScaleDenominator();
                if (denom > 0) return denom;
            }
        } catch (Exception ignored) {}
        return 10000;
    }

    private JButton miniBtn(String text, String tip, java.awt.event.ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 10f));
        b.setMargin(new java.awt.Insets(2, 6, 2, 6));
        b.setToolTipText(tip);
        b.addActionListener(al);
        b.setFocusPainted(false);
        return b;
    }

    private String extractNameFromDisplay(String display) {
        if (display == null) return "";
        String s = display.replaceAll("^\u25C9 |^\u25CB |\uD83D\uDD12 |\uD83D\uDD13 |^\\> |^  ", "");
        s = s.replaceAll("\\s*\\(oculto\\)|\\s*\\(bloq\\)", "");
        return s.trim();
    }

    private void duplicateLayoutElement(LayoutElement src) {
        if (src instanceof LayoutMap) { LayoutMap m = new LayoutMap("map-" + System.currentTimeMillis(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); m.setZOrder(layoutModel.nextZ()); m.setName(src.getName() + " copia"); layoutModel.addElement(m); return; }
        if (src instanceof LayoutLegend) { LayoutLegend l = new LayoutLegend("legend-" + System.currentTimeMillis(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); l.setZOrder(layoutModel.nextZ()); l.setName(src.getName() + " copia"); l.setAutoHeight(true); l.setItems(((LayoutLegend)src).getItems()); layoutModel.addElement(l); return; }
        if (src instanceof LayoutNorthArrow) { LayoutNorthArrow n = new LayoutNorthArrow("north-" + System.currentTimeMillis(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); n.setZOrder(layoutModel.nextZ()); n.setName(src.getName() + " copia"); layoutModel.addElement(n); return; }
        if (src instanceof LayoutScaleBar) { LayoutScaleBar s = new LayoutScaleBar("scale-" + System.currentTimeMillis(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); s.setZOrder(layoutModel.nextZ()); s.setName(src.getName() + " copia"); layoutModel.addElement(s); return; }
        if (src instanceof LayoutLabel) { LayoutLabel l = new LayoutLabel("lbl-" + System.currentTimeMillis(), ((LayoutLabel)src).getText(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); l.setZOrder(layoutModel.nextZ()); l.setName(src.getName() + " copia"); layoutModel.addElement(l); return; }
        if (src instanceof LayoutImage) { LayoutImage i = new LayoutImage("img-" + System.currentTimeMillis(), null, src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); i.setZOrder(layoutModel.nextZ()); i.setName(src.getName() + " copia"); layoutModel.addElement(i); return; }
        if (src instanceof LayoutTable) { LayoutTable t = new LayoutTable("table-" + System.currentTimeMillis(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); t.setZOrder(layoutModel.nextZ()); t.setName(src.getName() + " copia"); layoutModel.addElement(t); return; }
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
                    } catch (Exception ex) { ex.printStackTrace(); }
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
                    } catch (Exception ex) { ex.printStackTrace(); }
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

    private int countOfType(String prefix) {
        int c = 1;
        for (LayoutElement e : layoutModel.getElements()) {
            if (e.getName() != null && e.getName().startsWith(prefix)) {
                try { int v = Integer.parseInt(e.getName().substring(prefix.length()).trim()); c = Math.max(c, v + 1); } catch (Exception ignored) { c++; }
            }
        }
        return c;
    }

    private void centerOnElement(LayoutElement el) {
        if (el == null || previewPanel.lastPageBounds == null) return;
        java.awt.Rectangle pr = previewPanel.lastPageBounds;
        double cx = el.getBoundsMm().x + el.getBoundsMm().width / 2;
        double cy = el.getBoundsMm().y + el.getBoundsMm().height / 2;
        double pxPerMm = PREVIEW_RENDER_DPI / 25.4 * previewPanel.lastPreviewScale;
        int sx = pr.x + (int)(cx * pxPerMm) - previewPanel.getWidth() / 2;
        int sy = pr.y + (int)(cy * pxPerMm) - previewPanel.getHeight() / 2;
    }

    private void importQpt() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("QGIS Template (*.qpt)", "qpt"));
        if (fc.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        try {
            QgisQptImporter.ImportResult res = QgisQptImporter.importQpt(fc.getSelectedFile());
            for (LayoutElement el : res.imported) layoutModel.addElement(el);
            for (String skip : res.skipped) javax.swing.JOptionPane.showMessageDialog(this, skip, "Elemento omitido", javax.swing.JOptionPane.WARNING_MESSAGE);
            refreshAll();
            statusLabel.setText("Importado: " + fc.getSelectedFile().getName() + " (" + res.imported.size() + " elementos)");
        } catch (Exception ex) {
            ex.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, "Error al importar: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTemplateDialog() {
        java.util.Map<String, String> tmpl = LayoutTemplateManager.getTemplateList();
        String[] ks = tmpl.keySet().toArray(new String[0]), ns = tmpl.values().toArray(new String[0]);
        String ch = (String) javax.swing.JOptionPane.showInputDialog(this, "Plantilla:", "CATMAP", javax.swing.JOptionPane.QUESTION_MESSAGE, null, ns, ns[0]);
        if (ch == null) return;
        for (int i = 0; i < ns.length; i++) if (ns[i].equals(ch)) { LayoutTemplateManager.applyTemplate(ks[i], layoutModel); refreshAll(); statusLabel.setText("Plantilla: " + ch); return; }
    }

    private LayoutElement findEl(String id) { for (LayoutElement e : layoutModel.getElements()) if (e.getId().equals(id)) return e; return null; }

    private void refreshElementList() {
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
        if (el instanceof LayoutMap) return "\uD83D\uDDFA";
        if (el instanceof LayoutLegend) return "\uD83D\uDCCB";
        if (el instanceof LayoutNorthArrow) return "\uD83E\uDDED";
        if (el instanceof LayoutScaleBar) return "\uD83D\uDCCF";
        if (el instanceof LayoutImage) return "\uD83D\uDDBC";
        if (el instanceof LayoutEllipse) return "\u2B55";
        if (el instanceof LayoutLine) return "\u2795";
        if (el instanceof LayoutRectangle) return "\u25AD";
        if (el instanceof LayoutTable) return "\uD83D\uDCCA";
        if (el instanceof LayoutCartouche) return "\uD83D\uDCC4";
        if (el instanceof LayoutGraticule) return "\uD83D\uDCC8";
        if (el instanceof LayoutLabel) return "\uD83D\uDCDD";
        return "\u25A1";
    }

    private LayoutElement findElementByType(Class<?> type) {
        for (LayoutElement el : layoutModel.getElements()) if (type.isInstance(el)) return el;
        return null;
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
        } catch (Exception ignored) {}
    }

    private int hitTestHandle(LayoutElement el, Point pagePoint, RectMm pageRect) {
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

    private void resizeElement(int idx, int dx, int dy) {
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

    private static class LayoutUndoEntry {
        final String elId; final double x, y, w, h; final int zOrder; final boolean visible; final boolean locked;
        final boolean isDelete; final String typeName; final String serializedProps;
        LayoutUndoEntry(LayoutElement el, boolean isDelete) {
            this.elId = el.getId(); this.isDelete = isDelete;
            this.x = el.getBoundsMm().x; this.y = el.getBoundsMm().y; this.w = el.getBoundsMm().width; this.h = el.getBoundsMm().height;
            this.zOrder = el.getZOrder(); this.visible = el.isVisible(); this.locked = el.isLocked();
            this.typeName = el.getClass().getSimpleName();
            this.serializedProps = serializeProps(el);
        }
    }

    private static String serializeProps(LayoutElement el) {
        if (el instanceof LayoutLabel) return ((LayoutLabel)el).getText();
        if (el instanceof LayoutLegend) return ((LayoutLegend)el).getTitle();
        return "";
    }

    private void pushUndo(LayoutElement el, boolean isDelete) {
        undoStack.push(new LayoutUndoEntry(el, isDelete));
        redoStack.clear();
    }

    private void undo() {
        if (undoStack.isEmpty()) return;
        LayoutUndoEntry entry = undoStack.pop();
        redoStack.push(snapshotForRedo(entry.elId));
        if (entry.isDelete) {
            restoreElement(entry);
        } else {
            LayoutElement el = findElementById(entry.elId);
            if (el != null) {
                redoStack.pop();
                redoStack.push(new LayoutUndoEntry(el, true));
                layoutModel.removeElement(entry.elId);
            }
        }
        refreshAll();
    }

    private void redo() {
        if (redoStack.isEmpty()) return;
        LayoutUndoEntry entry = redoStack.pop();
        undoStack.push(snapshotForRedo(entry.elId));
        if (entry.isDelete) {
            LayoutElement el = findElementById(entry.elId);
            if (el != null) {
                undoStack.pop();
                undoStack.push(new LayoutUndoEntry(el, true));
                layoutModel.removeElement(entry.elId);
            }
        } else {
            restoreElement(entry);
        }
        refreshAll();
    }

    private LayoutUndoEntry snapshotForRedo(String id) {
        LayoutElement el = findElementById(id);
        return el != null ? new LayoutUndoEntry(el, false) : null;
    }

    private LayoutElement findElementById(String id) {
        for (LayoutElement el : layoutModel.getElements()) if (el.getId().equals(id)) return el;
        return null;
    }

    private void restoreElement(LayoutUndoEntry e) {
        LayoutElement existing = findElementById(e.elId);
        if (existing == null) {
            switch (e.typeName) {
                case "LayoutLabel": existing = new LayoutLabel(e.elId, e.serializedProps, e.x, e.y, e.w, e.h); break;
                case "LayoutLegend": existing = new LayoutLegend(e.elId, e.x, e.y, e.w, e.h); ((LayoutLegend)existing).setTitle(e.serializedProps); break;
                case "LayoutMap": existing = new LayoutMap(e.elId, e.x, e.y, e.w, e.h); break;
                case "LayoutScaleBar": existing = new LayoutScaleBar(e.elId, e.x, e.y, e.w, e.h); break;
                case "LayoutNorthArrow": existing = new LayoutNorthArrow(e.elId, e.x, e.y, e.w, e.h); break;
                default: return;
            }
            layoutModel.addElement(existing);
        }
        if (existing != null) {
            existing.setBoundsMm(e.x, e.y, e.w, e.h);
            existing.setZOrder(e.zOrder);
            existing.setVisible(e.visible);
            existing.setLocked(e.locked);
        }
    }

    private void refreshAll() {
        refreshElementList();
        previewPanel.repaint();
    }

    private void alignElements(int mode) {
        java.util.List<LayoutElement> sel = new java.util.ArrayList<>();
        for (LayoutElement el : layoutModel.getElements()) if (el.isSelected() && !el.isLocked()) sel.add(el);
        if (sel.size() < 2) return;
        pushUndoGroup(sel);
        double minX = sel.stream().mapToDouble(e -> e.getBoundsMm().x).min().orElse(0);
        double maxX = sel.stream().mapToDouble(e -> e.getBoundsMm().x + e.getBoundsMm().width).max().orElse(0);
        double minY = sel.stream().mapToDouble(e -> e.getBoundsMm().y).min().orElse(0);
        double maxY = sel.stream().mapToDouble(e -> e.getBoundsMm().y + e.getBoundsMm().height).max().orElse(0);
        for (LayoutElement el : sel) {
            double x = el.getBoundsMm().x, y = el.getBoundsMm().y, w = el.getBoundsMm().width, h = el.getBoundsMm().height;
            switch (mode) {
                case 0: x = minX; break;
                case 1: x = (minX + maxX - w) / 2; break;
                case 2: x = maxX - w; break;
                case 3: y = minY; break;
                case 4: y = (minY + maxY - h) / 2; break;
                case 5: y = maxY - h; break;
            }
            el.setBoundsMm(x, y, w, h);
        }
        refreshAll();
    }

    private void pushUndoGroup(java.util.List<LayoutElement> elements) {
        for (LayoutElement el : elements) undoStack.push(new LayoutUndoEntry(el, false));
        redoStack.clear();
    }

    private void refreshPropertiesPanel() {
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
        xField.addActionListener(e -> { try { label.setBoundsMm(Double.parseDouble(xField.getText()), label.getBoundsMm().y, label.getBoundsMm().width, label.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField yField = field(form, g, y, "Y (mm):", String.format("%.1f", label.getBoundsMm().y));
        yField.addActionListener(e -> { try { label.setBoundsMm(label.getBoundsMm().x, Double.parseDouble(yField.getText()), label.getBoundsMm().width, label.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField wField = field(form, g, y, "Ancho:", String.format("%.1f", label.getBoundsMm().width));
        wField.addActionListener(e -> { try { label.setBoundsMm(label.getBoundsMm().x, label.getBoundsMm().y, Double.parseDouble(wField.getText()), label.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField hField = field(form, g, y, "Alto:", String.format("%.1f", label.getBoundsMm().height));
        hField.addActionListener(e -> { try { label.setBoundsMm(label.getBoundsMm().x, label.getBoundsMm().y, label.getBoundsMm().width, Double.parseDouble(hField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        y = addBoolRow(form, g, y, "Visible:", label.isVisible(), v -> { label.setVisible(v); refreshElementList(); previewPanel.repaint(); });
        y = addBoolRow(form, g, y, "Bloqueado:", label.isLocked(), v -> { label.setLocked(v); refreshElementList(); previewPanel.repaint(); });

        // Texto
        y++; sectionLabel(form, g, y, "Texto"); y++;
        JTextField textField = field(form, g, y, "Contenido:", label.getText() != null ? label.getText() : "");
        textField.addActionListener(e -> { label.setText(textField.getText()); previewPanel.repaint(); });
        y++;
        Font f = label.getFont();
        JTextField fontField = field(form, g, y, "Fuente:", f.getFamily());
        y++;
        JTextField sizeField = field(form, g, y, "Tamano:", String.valueOf(f.getSize()));
        sizeField.addActionListener(e -> { try { label.setFont(label.getFont().deriveFont((float)Integer.parseInt(sizeField.getText()))); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        java.awt.Font currentFont = label.getFont();
        y = addBoolRow(form, g, y, "Negrita:", currentFont.isBold(), v -> { label.setFont(label.getFont().deriveFont(v ? Font.BOLD : Font.PLAIN)); previewPanel.repaint(); });

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
        xField.addActionListener(e -> { try { el.setBoundsMm(Double.parseDouble(xField.getText()), el.getBoundsMm().y, el.getBoundsMm().width, el.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField yField = field(form, g, y, "Y (mm):", String.format("%.1f", el.getBoundsMm().y));
        yField.addActionListener(e -> { try { el.setBoundsMm(el.getBoundsMm().x, Double.parseDouble(yField.getText()), el.getBoundsMm().width, el.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField wField = field(form, g, y, "Ancho:", String.format("%.1f", el.getBoundsMm().width));
        wField.addActionListener(e -> { try { el.setBoundsMm(el.getBoundsMm().x, el.getBoundsMm().y, Double.parseDouble(wField.getText()), el.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField hField = field(form, g, y, "Alto:", String.format("%.1f", el.getBoundsMm().height));
        hField.addActionListener(e -> { try { el.setBoundsMm(el.getBoundsMm().x, el.getBoundsMm().y, el.getBoundsMm().width, Double.parseDouble(hField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} });
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
            intXField.addActionListener(ev -> { try { lg.setIntervalX(Double.parseDouble(intXField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} }); y++;
            JTextField intYField = field(form, g, y, "Intervalo Y:", String.format("%.2f", lg.getIntervalY()));
            intYField.addActionListener(ev -> { try { lg.setIntervalY(Double.parseDouble(intYField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} }); y++;
            y = addBoolRow(form, g, y, "Geografico:", lg.isGeographic(), v -> { lg.setGeographic(v); previewPanel.repaint(); });
            y = addBoolRow(form, g, y, "Etiquetas:", lg.isShowLabels(), v -> { lg.setShowLabels(v); previewPanel.repaint(); });
        }
        if (el instanceof LayoutLine) {
            LayoutLine ll = (LayoutLine) el;
            y++; sectionLabel(form, g, y, "Linea"); y++;
            JTextField lwField = field(form, g, y, "Grosor:", String.format("%.1f", ll.getLineWidth()));
            lwField.addActionListener(ev -> { try { ll.setLineWidth(Float.parseFloat(lwField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} }); y++;
            y = addBoolRow(form, g, y, "Punteada:", ll.isDashed(), v -> { ll.setDashed(v); previewPanel.repaint(); });
        }
        if (el instanceof LayoutEllipse || el instanceof LayoutRectangle) {
            y++; sectionLabel(form, g, y, "Estilo"); y++;
            JTextField bwField = field(form, g, y, "Borde:", String.format("%.1f", el instanceof LayoutEllipse ? ((LayoutEllipse)el).getBorderWidth() : 1.5f));
            bwField.addActionListener(ev -> { try { float v = Float.parseFloat(bwField.getText()); if (el instanceof LayoutEllipse) ((LayoutEllipse)el).setBorderWidth(v); previewPanel.repaint(); } catch (Exception ignored) {} }); y++;
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
        xField.addActionListener(e -> { try { map.setBoundsMm(Double.parseDouble(xField.getText()), map.getBoundsMm().y, map.getBoundsMm().width, map.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField yField = field(form, g, y, "Y (mm):", String.format("%.1f", map.getBoundsMm().y));
        yField.addActionListener(e -> { try { map.setBoundsMm(map.getBoundsMm().x, Double.parseDouble(yField.getText()), map.getBoundsMm().width, map.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField wField = field(form, g, y, "Ancho:", String.format("%.1f", map.getBoundsMm().width));
        wField.addActionListener(e -> { try { map.setBoundsMm(map.getBoundsMm().x, map.getBoundsMm().y, Double.parseDouble(wField.getText()), map.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField hField = field(form, g, y, "Alto:", String.format("%.1f", map.getBoundsMm().height));
        hField.addActionListener(e -> { try { map.setBoundsMm(map.getBoundsMm().x, map.getBoundsMm().y, map.getBoundsMm().width, Double.parseDouble(hField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} });
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
            } catch (Exception ignored) {}
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
            intXField.addActionListener(e -> { try { map.setGridIntervalX(Double.parseDouble(intXField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} });
            y++;
            JTextField intYField = field(form, g, y, "Intervalo Y:", String.format("%.1f", map.getGridIntervalY()));
            intYField.addActionListener(e -> { try { map.setGridIntervalY(Double.parseDouble(intYField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} });
            y++;
            JTextField unitField = field(form, g, y, "Unidad:", map.getGridUnit());
            unitField.addActionListener(e -> { map.setGridUnit(unitField.getText().trim()); previewPanel.repaint(); });
            y++;
        } else {
            JTextField colsField = field(form, g, y, "Columnas:", String.valueOf(gridColumnsSpinner.getValue()));
            colsField.addActionListener(e -> { try { gridColumnsSpinner.setValue(Integer.parseInt(colsField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} });
            y++;
            JTextField rowsField = field(form, g, y, "Filas:", String.valueOf(gridRowsSpinner.getValue()));
            rowsField.addActionListener(e -> { try { gridRowsSpinner.setValue(Integer.parseInt(rowsField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} });
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
        xField.addActionListener(e -> { try { legend.setBoundsMm(Double.parseDouble(xField.getText()), legend.getBoundsMm().y, legend.getBoundsMm().width, legend.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField yField = field(form, g, y, "Y (mm):", String.format("%.1f", legend.getBoundsMm().y));
        yField.addActionListener(e -> { try { legend.setBoundsMm(legend.getBoundsMm().x, Double.parseDouble(yField.getText()), legend.getBoundsMm().width, legend.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField wField = field(form, g, y, "Ancho:", String.format("%.1f", legend.getBoundsMm().width));
        wField.addActionListener(e -> { try { legend.setBoundsMm(legend.getBoundsMm().x, legend.getBoundsMm().y, Double.parseDouble(wField.getText()), legend.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField hField = field(form, g, y, "Alto:", String.format("%.1f", legend.getBoundsMm().height));
        hField.addActionListener(e -> { try { legend.setBoundsMm(legend.getBoundsMm().x, legend.getBoundsMm().y, legend.getBoundsMm().width, Double.parseDouble(hField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        y = addBoolRow(form, g, y, "Visible:", legend.isVisible(), v -> { legend.setVisible(v); refreshElementList(); previewPanel.repaint(); });
        y = addBoolRow(form, g, y, "Bloqueado:", legend.isLocked(), v -> { legend.setLocked(v); refreshElementList(); previewPanel.repaint(); });

        // Seccion: Texto
        y++; sectionLabel(form, g, y, "Texto"); y++;
        JTextField titleField = field(form, g, y, "Titulo:", legend.getTitle() != null ? legend.getTitle() : "Leyenda");
        titleField.addActionListener(e -> { legend.setTitle(titleField.getText().trim()); previewPanel.repaint(); });
        y++;
        JTextField titleSizeField = field(form, g, y, "Tamano titulo:", String.valueOf(legend.getTitleFont().getSize()));
        titleSizeField.addActionListener(e -> { try { legend.setTitleFont(legend.getTitleFont().deriveFont((float)Integer.parseInt(titleSizeField.getText()))); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField itemSizeField = field(form, g, y, "Tamano items:", String.valueOf(legend.getItemFont().getSize()));
        itemSizeField.addActionListener(e -> { try { legend.setItemFont(legend.getItemFont().deriveFont((float)Integer.parseInt(itemSizeField.getText()))); previewPanel.repaint(); } catch (Exception ignored) {} });

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
        opacityField.addActionListener(e -> { try { float v = Float.parseFloat(opacityField.getText().replace("%","")); legend.setBgOpacity(Math.max(0, Math.min(100, v)) / 100f); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;

        // Seccion: Ajuste
        y++; sectionLabel(form, g, y, "Ajuste"); y++;
        y = addBoolRow(form, g, y, "Alto automatico:", legend.isAutoHeight(), v -> { legend.setAutoHeight(v); previewPanel.repaint(); });
        JTextField colsField = field(form, g, y, "Columnas:", String.valueOf(legend.getColumns()));
        colsField.addActionListener(e -> { try { legend.setColumns(Integer.parseInt(colsField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} });
        y++;
        JTextField symSizeField = field(form, g, y, "Tamano simbolo:", String.format("%.1f", legend.getSymbolSizeMm()));
        symSizeField.addActionListener(e -> { try { legend.setSymbolSizeMm(Double.parseDouble(symSizeField.getText())); previewPanel.repaint(); } catch (Exception ignored) {} });
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
}
