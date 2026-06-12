package ar.com.catgis;
import ar.com.catgis.data.vector.VectorLayerUtils;

import ar.com.catgis.core.model.Project;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.LayerGroup;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.online.OnlineRasterSource;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.renderer.PolygonSymbolRenderer;
import ar.com.catgis.renderer.LineSymbolRenderer;
import ar.com.catgis.renderer.MapDecorationRenderer;
import ar.com.catgis.renderer.decorations.FeatureDecoratorRenderer;
import ar.com.catgis.renderer.labels.LabelExpressionEngine;
import ar.com.catgis.renderer.labels.LabelPlacementEngine;

public class LayersPanel extends JPanel {

    private final DefaultListModel<Object> model;
    private final JList<Object> layerList;
    private final JButton newGroupButton;
    private final JTextField layerFilterField;
    private final List<Object> unfilteredItems = new ArrayList<>();
    private final JButton scrollTopButton;
    private final JButton scrollBottomButton;
    private final JScrollPane layerScrollPane;
    private final JLabel emptyStateLabel;
    private java.awt.CardLayout tocCardLayout;
    private int dragSourceIndex = -1;
    private int dragTargetInsertIndex = -1;
    private boolean dragReorderActive = false;

    public LayersPanel() {
        setLayout(new BorderLayout());

        model = new DefaultListModel<>();
        layerList = new JList<>(model);
        layerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        layerList.setCellRenderer(new LayerCellRenderer());
        layerList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                CatgisDesktopApp.syncFloatingVectorEditToolbar();
                CatgisDesktopApp.syncProInterpretationToolbar();
                // Emit layer selection event
                Layer selected = getSelectedLayer();
                if (selected != null) {
                    ar.com.catgis.service.EventBus.emit(
                        ar.com.catgis.service.EventBus.EventType.LAYER_SELECTED, selected);
                }
            }
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        topBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
        newGroupButton = new JButton(I18n.t("Grupo"), AppIcons.openIcon());
        newGroupButton.setToolTipText(I18n.t("Crear nuevo grupo de capas"));
        newGroupButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
        newGroupButton.addActionListener(e -> createNewGroupFromSelection());
        topBar.add(newGroupButton);

        JButton zoomBtn = new JButton(I18n.t("Zoom"));
        zoomBtn.setToolTipText(I18n.t("Hacer zoom a la capa seleccionada"));
        zoomBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
        zoomBtn.addActionListener(e -> zoomToSelectedLayer());
        topBar.add(zoomBtn);

        JButton propsBtn = new JButton(I18n.t("Propiedades"));
        propsBtn.setToolTipText(I18n.t("Abrir propiedades de la capa seleccionada"));
        propsBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
        propsBtn.addActionListener(e -> openPropertiesForSelectedLayer());
        topBar.add(propsBtn);

        JButton removeBtn = new JButton(I18n.t("Quitar"));
        removeBtn.setToolTipText(I18n.t("Quitar la capa seleccionada del proyecto"));
        removeBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
        removeBtn.addActionListener(e -> removeSelectedLayers());
        topBar.add(removeBtn);

        scrollTopButton = new JButton(AppIcons.upIcon());
        scrollTopButton.setToolTipText(I18n.t("Ir al inicio"));
        scrollTopButton.addActionListener(e -> scrollToTop());
        topBar.add(scrollTopButton);
        scrollBottomButton = new JButton(AppIcons.downIcon());
        scrollBottomButton.setToolTipText(I18n.t("Ir al final"));
        scrollBottomButton.addActionListener(e -> scrollToBottom());
        topBar.add(scrollBottomButton);

        // Layer search / filter
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        layerFilterField = new JTextField();
        layerFilterField.putClientProperty("JTextField.placeholderText", "Filtrar capas...");
        layerFilterField.setFont(new Font("SansSerif", Font.PLAIN, 11));
        layerFilterField.setToolTipText("Filtrar capas por nombre (presione Escape para limpiar)");

        // Debounced filter: wait 300ms after typing stops
        Timer filterTimer = new Timer(300, e -> applyLayerFilter());
        filterTimer.setRepeats(false);
        layerFilterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTimer.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTimer.restart(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTimer.restart(); }
        });
        layerFilterField.registerKeyboardAction(
                e -> { layerFilterField.setText(""); applyLayerFilter(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_FOCUSED
        );

        searchPanel.add(layerFilterField, BorderLayout.CENTER);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topBar, BorderLayout.NORTH);
        northPanel.add(searchPanel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);

        layerScrollPane = new JScrollPane(layerList);
        layerScrollPane.setBorder(BorderFactory.createEmptyBorder());
        layerScrollPane.getVerticalScrollBar().setUnitIncrement(18);

        emptyStateLabel = new JLabel(I18n.t("Arrastra capas aqui o usa el menu Datos"), SwingConstants.CENTER);
        emptyStateLabel.setForeground(new Color(160, 160, 170));
        emptyStateLabel.setFont(emptyStateLabel.getFont().deriveFont(Font.PLAIN, 13f));
        emptyStateLabel.setVisible(model.isEmpty());

        JPanel centerPanel = new JPanel(new java.awt.CardLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(layerScrollPane, "layers");
        centerPanel.add(emptyStateLabel, "empty");
        tocCardLayout = (java.awt.CardLayout) centerPanel.getLayout();
        add(centerPanel, BorderLayout.CENTER);

        MouseAdapter layerMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = layerList.locationToIndex(e.getPoint());
                if (index < 0) {
                    if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {
                        showEmptyAreaPopup(e);
                    }
                    return;
                }

                Rectangle bounds = layerList.getCellBounds(index, index);
                if (bounds == null || !bounds.contains(e.getPoint())) {
                    if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {
                        showEmptyAreaPopup(e);
                    }
                    return;
                }

                Object selectedValue = model.get(index);
                Layer selectedLayer = asLayer(selectedValue);
                LayerGroup selectedGroup = asGroup(selectedValue);

                if (e.getButton() == MouseEvent.BUTTON1 && isVisibilityToggleHit(e, bounds, selectedValue)) {
                    dragSourceIndex = -1;
                    dragTargetInsertIndex = -1;
                    dragReorderActive = false;
                    if (!layerList.isSelectedIndex(index)) {
                        layerList.setSelectedIndex(index);
                    }
                    if (selectedGroup != null) {
                        selectedGroup.setVisible(!selectedGroup.isVisible());
                    } else if (selectedLayer != null) {
                        selectedLayer.setVisible(!selectedLayer.isVisible());
                    } else {
                        return;
                    }
                    CatgisDesktopApp.markProjectDirty();
                    refreshLayerList();
                    CatgisDesktopApp.mapPanel.repaint();

                    if (CatgisDesktopApp.statusBar != null) {
                        if (selectedGroup != null) {
                            AppContext.setStatusMessage(
                                    "Grupo " + (selectedGroup.isVisible() ? "visible" : "oculto") + ": " + selectedGroup.getName()
                            );
                        } else {
                            AppContext.setStatusMessage(
                                    "Capa " + (selectedLayer.isVisible() ? "visible" : "oculta") + ": " + selectedLayer.getName()
                            );
                        }
                    }
                    return;
                }

                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    if (selectedGroup != null) {
                        selectedGroup.setExpanded(!selectedGroup.isExpanded());
                        CatgisDesktopApp.markProjectDirty();
                        refreshLayerList();
                    } else if (selectedLayer != null) {
                        CatgisDesktopApp.mapPanel.zoomToLayer(selectedLayer);
                    }
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {
                    updateSelectionForPopup(index);
                    if (selectedGroup != null) {
                        showGroupPopup(e, selectedGroup);
                    } else if (selectedLayer != null) {
                        showLayerPopup(e, selectedLayer);
                    } else {
                        showEmptyAreaPopup(e);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragSourceIndex = -1;
                dragTargetInsertIndex = -1;
                dragReorderActive = false;
                if (e.isPopupTrigger()) {
                    int index = layerList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Rectangle bounds = layerList.getCellBounds(index, index);
                        if (bounds == null || !bounds.contains(e.getPoint())) {
                            showEmptyAreaPopup(e);
                            return;
                        }
                        updateSelectionForPopup(index);
                        Object value = model.get(index);
                        Layer layer = asLayer(value);
                        LayerGroup group = asGroup(value);
                        if (group != null) {
                            showGroupPopup(e, group);
                        } else if (layer != null) {
                            showLayerPopup(e, layer);
                        }
                    } else {
                        showEmptyAreaPopup(e);
                    }
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON1) {
                    int index = layerList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Rectangle bounds = layerList.getCellBounds(index, index);
                        boolean selectionModifier = e.isControlDown() || e.isShiftDown() || e.isMetaDown();
                        Object rowValue = model.get(index);
                        if (bounds != null && !isVisibilityToggleHit(e, bounds, rowValue) && !selectionModifier && asLayer(rowValue) != null) {
                            dragSourceIndex = index;
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragReorderActive && dragSourceIndex >= 0) {
                    int dropIndex = dragTargetInsertIndex >= 0
                            ? dragTargetInsertIndex
                            : resolveDropInsertIndex(e.getPoint());
                    if (dropIndex >= 0) {
                        handleLayerDrop(dragSourceIndex, e.getPoint(), dropIndex);
                    }
                    dragSourceIndex = -1;
                    dragTargetInsertIndex = -1;
                    dragReorderActive = false;
                    layerList.setCursor(java.awt.Cursor.getDefaultCursor());
                    return;
                }

                dragSourceIndex = -1;
                dragTargetInsertIndex = -1;
                dragReorderActive = false;
                layerList.setCursor(java.awt.Cursor.getDefaultCursor());
                if (e.isPopupTrigger()) {
                    int index = layerList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Rectangle bounds = layerList.getCellBounds(index, index);
                        if (bounds == null || !bounds.contains(e.getPoint())) {
                            showEmptyAreaPopup(e);
                            return;
                        }
                        updateSelectionForPopup(index);
                        Object value = model.get(index);
                        Layer layer = asLayer(value);
                        LayerGroup group = asGroup(value);
                        if (group != null) {
                            showGroupPopup(e, group);
                        } else if (layer != null) {
                            showLayerPopup(e, layer);
                        }
                    } else {
                        showEmptyAreaPopup(e);
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragSourceIndex >= 0) {
                    dragReorderActive = true;
                    dragTargetInsertIndex = resolveDropInsertIndex(e.getPoint());
                    layerList.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
                }
            }
        };
        layerList.addMouseListener(layerMouseAdapter);
        layerList.addMouseMotionListener(layerMouseAdapter);
        configureKeyboardShortcuts();
    }

    public void addLayer(Layer layer) {
        refreshLayerList();
    }

    public void removeLayer(Layer layer) {
        refreshLayerList();
    }

    public void clearLayers() {
        model.clear();
        unfilteredItems.clear();
    }

    public Layer getSelectedLayer() {
        Layer direct = asLayer(layerList.getSelectedValue());
        if (direct != null) {
            return direct;
        }
        for (Object value : layerList.getSelectedValuesList()) {
            Layer layer = asLayer(value);
            if (layer != null) {
                return layer;
            }
        }
        return null;
    }

    public List<Layer> getSelectedLayers() {
        List<Layer> selected = new ArrayList<>();
        for (Object value : layerList.getSelectedValuesList()) {
            Layer layer = asLayer(value);
            if (layer != null) {
                selected.add(layer);
            }
        }
        return selected;
    }

    public RasterLayer getSelectedRasterLayer() {
        Layer selected = getSelectedLayer();
        return selected instanceof RasterLayer rasterLayer ? rasterLayer : null;
    }

    public boolean canRunSelectedProThematic() {
        RasterLayer rasterLayer = getSelectedRasterLayer();
        return isQuickInterpretationSource(rasterLayer) && ProRasterDerivedService.supportsThematicOutput(rasterLayer);
    }

    public boolean canRunSelectedProQa() {
        return isQuickInterpretationSource(getSelectedRasterLayer());
    }

    public boolean canRunSelectedLandsatQaMask(String operation) {
        RasterLayer rasterLayer = getSelectedRasterLayer();
        return isQuickInterpretationSource(rasterLayer)
                && isSupportedLandsatQuickMask(operation)
                && ProRasterDerivedService.supportsLandsatQaPixelMasks(rasterLayer);
    }

    public boolean canRunSelectedProComparison() {
        RasterLayer rasterLayer = getSelectedRasterLayer();
        return isQuickInterpretationSource(rasterLayer) && !findComparableProLayers(rasterLayer).isEmpty();
    }

    public String describeSelectedProInterpretationContext() {
        Layer selectedLayer = getSelectedLayer();
        if (selectedLayer == null) {
            return "Selecciona un raster Pro Landsat o satelital";
        }
        if (!(selectedLayer instanceof RasterLayer rasterLayer)) {
            return "Seleccion actual: " + selectedLayer.getName() + " | sin interpretacion raster Pro";
        }
        if (!hasProRasterMetadata(rasterLayer)) {
            return "Raster seleccionado: " + rasterLayer.getName() + " | sin metadata Pro";
        }
        if (rasterLayer.isDerivedLayer()) {
            return "Salida raster derivada: " + rasterLayer.getName();
        }
        String variable = nonBlank(rasterLayer.getProVariableName(), "sin variable");
        String dataset = nonBlank(rasterLayer.getProDatasetRef(), "sin dataset");
        return rasterLayer.getName() + " | " + variable + " | " + dataset;
    }

    public void runSelectedProThematic() {
        RasterLayer rasterLayer = requireSelectedQuickInterpretationSource("mapa tematico raster");
        if (rasterLayer == null) {
            return;
        }
        if (!ProRasterDerivedService.supportsThematicOutput(rasterLayer)) {
            showQuickInterpretationWarning("La capa seleccionada no tiene un preset tematico Pro compatible.");
            return;
        }
        generateProDerivedRaster(rasterLayer, (String) null);
    }

    public void runSelectedProQa() {
        RasterLayer rasterLayer = requireSelectedQuickInterpretationSource("QA Pro");
        if (rasterLayer == null) {
            return;
        }
        generateProDerivedRaster(rasterLayer, true);
    }

    public void runSelectedLandsatQaMask(String operation) {
        RasterLayer rasterLayer = requireSelectedQuickInterpretationSource("mascara Landsat");
        if (rasterLayer == null) {
            return;
        }
        if (!isSupportedLandsatQuickMask(operation) || !ProRasterDerivedService.supportsLandsatQaPixelMasks(rasterLayer)) {
            showQuickInterpretationWarning("La capa seleccionada no dispone de QA_PIXEL Landsat para generar esta mascara.");
            return;
        }
        generateProDerivedRaster(rasterLayer, operation);
    }

    public void runSelectedProComparison() {
        RasterLayer rasterLayer = requireSelectedQuickInterpretationSource("comparacion temporal raster");
        if (rasterLayer == null) {
            return;
        }
        if (findComparableProLayers(rasterLayer).isEmpty()) {
            showQuickInterpretationWarning(
                    "No hay otra capa Pro compatible en el proyecto para comparar esta variable.\n\n"
                            + "CATGIS exige misma variable, misma familia fuente y fechas distintas."
            );
            return;
        }
        compareProRasterWithAnotherDate(rasterLayer);
    }

    private RasterLayer requireSelectedQuickInterpretationSource(String actionLabel) {
        RasterLayer rasterLayer = getSelectedRasterLayer();
        if (rasterLayer == null) {
            showQuickInterpretationWarning("Selecciona primero un raster Pro para ejecutar " + actionLabel + ".");
            return null;
        }
        if (!hasProRasterMetadata(rasterLayer)) {
            showQuickInterpretationWarning("La capa seleccionada no tiene metadata Pro suficiente para ejecutar " + actionLabel + ".");
            return null;
        }
        if (rasterLayer.isDerivedLayer()) {
            showQuickInterpretationWarning("Selecciona la capa fuente Pro, no una salida derivada, para ejecutar " + actionLabel + ".");
            return null;
        }
        return rasterLayer;
    }

    private boolean isQuickInterpretationSource(RasterLayer rasterLayer) {
        return rasterLayer != null && !rasterLayer.isDerivedLayer() && hasProRasterMetadata(rasterLayer);
    }

    private boolean isSupportedLandsatQuickMask(String operation) {
        String normalized = operation != null ? operation.trim().toLowerCase(java.util.Locale.ROOT) : "";
        return ProRasterDerivedService.OP_PRO_MASK_LANDSAT_CLOUDS.equals(normalized)
                || ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SHADOW.equals(normalized)
                || ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SNOW.equals(normalized)
                || ProRasterDerivedService.OP_PRO_MASK_LANDSAT_WATER.equals(normalized);
    }

    private void showQuickInterpretationWarning(String message) {
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(message);
        }
        JOptionPane.showMessageDialog(
                this,
                message,
                "Interpretacion Pro",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void selectLayer(Layer layer) {
        if (layer == null) {
            layerList.clearSelection();
            return;
        }
        int index = model.indexOf(layer);
        if (index >= 0) {
            layerList.setSelectedIndex(index);
            layerList.ensureIndexIsVisible(index);
        }
    }

    public void refreshLayerList() {
        List<Object> previousSelection = new ArrayList<>(layerList.getSelectedValuesList());
        model.clear();
        unfilteredItems.clear();
        Project project = AppContext.project();
        if (project == null) {
            layerList.repaint();
            CatgisDesktopApp.syncProInterpretationToolbar();
            return;
        }

        for (Layer layer : project.getUngroupedLayers()) {
            if (layer != null) {
                unfilteredItems.add(layer);
            }
        }

        for (LayerGroup group : project.getLayerGroups()) {
            if (group == null) {
                continue;
            }
            unfilteredItems.add(group);
            if (group.isExpanded()) {
                for (Layer layer : project.getLayersForGroup(group.getName())) {
                    if (layer != null) {
                        unfilteredItems.add(layer);
                    }
                }
            }
        }

        // Apply any active filter
        applyLayerFilterImpl();

        restoreSelection(previousSelection);
        layerList.repaint();
        if (tocCardLayout != null) {
            tocCardLayout.show((JPanel) emptyStateLabel.getParent(), model.isEmpty() ? "empty" : "layers");
        }
        CatgisDesktopApp.syncProInterpretationToolbar();
    }

    // --- Layer filter methods ---

    private void applyLayerFilter() {
        SwingUtilities.invokeLater(this::applyLayerFilterImpl);
    }

    private void applyLayerFilterImpl() {
        String query = layerFilterField.getText().trim().toLowerCase();
        model.clear();

        if (query.isEmpty()) {
            for (Object item : unfilteredItems) {
                model.addElement(item);
            }
            updateEmptyState();
            return;
        }

        for (Object item : unfilteredItems) {
            String name = getItemName(item);
            if (name != null && name.toLowerCase().contains(query)) {
                model.addElement(item);
            }
        }
        updateEmptyState();
    }

    private String getItemName(Object item) {
        if (item instanceof Layer layer) {
            return layer.getName();
        }
        if (item instanceof LayerGroup group) {
            return group.getName();
        }
        return null;
    }

    private void updateEmptyState() {
        if (tocCardLayout != null) {
            tocCardLayout.show((JPanel) emptyStateLabel.getParent(), model.isEmpty() ? "empty" : "layers");
        }
    }

    // --- End filter methods ---

    private void configureKeyboardShortcuts() {
        layerList.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                "removeSelectedProjectLayers"
        );
        layerList.getActionMap().put("removeSelectedProjectLayers", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedLayersFromList();
            }
        });
        layerList.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
                "scrollLayersToTop"
        );
        layerList.getActionMap().put("scrollLayersToTop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isEmpty()) {
                    layerList.setSelectedIndex(0);
                    scrollToTop();
                }
            }
        });
        layerList.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
                "scrollLayersToBottom"
        );
        layerList.getActionMap().put("scrollLayersToBottom", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isEmpty()) {
                    layerList.setSelectedIndex(model.getSize() - 1);
                    scrollToBottom();
                }
            }
        });
    }

    private void scrollToTop() {
        SwingUtilities.invokeLater(() -> {
            if (!model.isEmpty()) {
                layerList.ensureIndexIsVisible(0);
            }
            if (layerScrollPane != null) {
                layerScrollPane.getVerticalScrollBar().setValue(layerScrollPane.getVerticalScrollBar().getMinimum());
            }
        });
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            if (!model.isEmpty()) {
                layerList.ensureIndexIsVisible(model.getSize() - 1);
            }
            if (layerScrollPane != null) {
                layerScrollPane.getVerticalScrollBar().setValue(layerScrollPane.getVerticalScrollBar().getMaximum());
            }
        });
    }

    private void zoomToSelectedLayer() {
        Object sel = layerList.getSelectedValue();
        if (sel instanceof Layer layer) {
            CatgisDesktopApp.mapPanel.zoomToLayer(layer);
        }
    }

    private void openPropertiesForSelectedLayer() {
        Object sel = layerList.getSelectedValue();
        if (sel instanceof Layer layer) {
            LayerPropertiesDialog.open(layer);
        }
    }

    private void removeSelectedLayers() {
        List<Object> selected = layerList.getSelectedValuesList();
        if (selected.isEmpty()) return;
        StringBuilder names = new StringBuilder();
        for (Object obj : selected) {
            if (obj instanceof Layer l) names.append("- ").append(l.getName()).append("\n");
        }
        int opt = JOptionPane.showConfirmDialog(this,
                I18n.t("Quitar las siguientes capas?\n") + names,
                I18n.t("Quitar capas"), JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;
        for (Object obj : selected) {
            if (obj instanceof Layer l) {
                CatgisDesktopApp.mapPanel.removeLayers(List.of(l));
                if (AppContext.project() != null) {
                    AppContext.project().removeLayer(l);
                }
            }
        }
        refreshLayerList();
        CatgisDesktopApp.markProjectDirty();
    }

    private boolean isVisibilityToggleHit(MouseEvent e, Rectangle bounds, Object rowValue) {
        int relativeX = e.getX() - bounds.x;
        int minX = 8;
        int maxX = 28;
        Layer layer = asLayer(rowValue);
        if (layer != null && layer.isInGroup()) {
            minX += 22;
            maxX += 22;
        }
        return relativeX >= minX && relativeX <= maxX;
    }

    private void updateSelectionForPopup(int index) {
        if (index < 0) {
            return;
        }
        if (!layerList.isSelectedIndex(index)) {
            layerList.setSelectedIndex(index);
        }
    }

    private void showLayerPopup(MouseEvent e, Layer selectedLayer) {
        if (selectedLayer == null) {
            return;
        }

        JPopupMenu popupMenu = isRasterLayer(selectedLayer)
                ? buildRasterPopup(selectedLayer)
                : buildVectorPopup(selectedLayer);

        popupMenu.show(layerList, e.getX(), e.getY());
    }

    private void showGroupPopup(MouseEvent e, LayerGroup selectedGroup) {
        if (selectedGroup == null) {
            return;
        }

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem titleItem = createMenuItem(selectedGroup.getName(), AppIcons.openIcon());
        titleItem.setEnabled(false);
        popupMenu.add(titleItem);
        popupMenu.addSeparator();

        JMenuItem toggleVisibilityItem = createMenuItem(
                selectedGroup.isVisible() ? "Ocultar grupo" : "Mostrar grupo",
                selectedGroup.isVisible() ? AppIcons.hiddenIcon() : AppIcons.visibleIcon()
        );
        toggleVisibilityItem.addActionListener(ev -> {
            selectedGroup.setVisible(!selectedGroup.isVisible());
            CatgisDesktopApp.markProjectDirty();
            refreshLayerList();
            CatgisDesktopApp.mapPanel.repaint();
        });
        popupMenu.add(toggleVisibilityItem);

        JMenuItem expandItem = createMenuItem(
                selectedGroup.isExpanded() ? "Contraer grupo" : "Expandir grupo",
                selectedGroup.isExpanded() ? AppIcons.downIcon() : AppIcons.openIcon()
        );
        expandItem.addActionListener(ev -> {
            selectedGroup.setExpanded(!selectedGroup.isExpanded());
            CatgisDesktopApp.markProjectDirty();
            refreshLayerList();
        });
        popupMenu.add(expandItem);

        JMenuItem renameItem = createMenuItem("Renombrar grupo", AppIcons.renameIcon());
        renameItem.addActionListener(ev -> renameGroup(selectedGroup));
        popupMenu.add(renameItem);

        JMenuItem moveUpItem = createMenuItem("Subir grupo", AppIcons.upIcon());
        moveUpItem.addActionListener(ev -> moveGroup(selectedGroup, -1));
        popupMenu.add(moveUpItem);

        JMenuItem moveDownItem = createMenuItem("Bajar grupo", AppIcons.downIcon());
        moveDownItem.addActionListener(ev -> moveGroup(selectedGroup, 1));
        popupMenu.add(moveDownItem);

        popupMenu.addSeparator();

        JMenuItem removeItem = createMenuItem("Quitar grupo (mantener capas)", AppIcons.removeIcon());
        removeItem.addActionListener(ev -> removeGroup(selectedGroup));
        popupMenu.add(removeItem);

        popupMenu.show(layerList, e.getX(), e.getY());
    }

    private void showEmptyAreaPopup(MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem newGroupItem = createMenuItem("Nuevo grupo", AppIcons.openIcon());
        newGroupItem.addActionListener(ev -> createNewGroupFromSelection());
        popupMenu.add(newGroupItem);
        popupMenu.show(layerList, e.getX(), e.getY());
    }

    private JPopupMenu buildVectorPopup(Layer selectedLayer) {
        JPopupMenu popupMenu = new JPopupMenu();
        boolean readOnlyVector = VectorLayerUtils.isReadOnlyVectorLayer(selectedLayer);

        JMenuItem propertiesItem = createMenuItem(selectedLayer.getName(), AppIcons.imageryIcon());
        propertiesItem.setEnabled(false);
        popupMenu.add(propertiesItem);
        popupMenu.addSeparator();

        addCommonTopItems(popupMenu, selectedLayer, "Ocultar capa", "Mostrar capa");

        JMenuItem displayItem = createMenuItem("Ajustes de visualizacion...", AppIcons.attrEditIcon());
        displayItem.addActionListener(ev -> openVectorDisplaySettings(selectedLayer));
        popupMenu.add(displayItem);

        JMenuItem editItem = createMenuItem(readOnlyVector ? "Capa en solo lectura" : "Editar vector", AppIcons.attrEditIcon());
        editItem.setEnabled(!readOnlyVector);
        editItem.addActionListener(ev -> openVectorEditing(selectedLayer));
        popupMenu.add(editItem);

        JMenuItem copyToEditingItem = createMenuItem("Copiar seleccionadas a capa en edicion", AppIcons.attrCopyIcon());
        boolean canCopyToEditing = CatgisDesktopApp.mapPanel != null
                && CatgisDesktopApp.mapPanel.canCopySelectedFeaturesFromLayerToEditingLayer(selectedLayer);
        copyToEditingItem.setEnabled(canCopyToEditing);
        copyToEditingItem.addActionListener(ev -> {
            if (CatgisDesktopApp.mapPanel != null
                    && CatgisDesktopApp.mapPanel.copySelectedFeaturesFromLayerToEditingLayer(selectedLayer)) {
                refreshLayerList();
                CatgisDesktopApp.mapPanel.repaint();
            }
        });
        popupMenu.add(copyToEditingItem);

        JMenu simbologiaMenu = new JMenu("Simbologia");
        simbologiaMenu.setIcon(AppIcons.propertiesIcon());

        JMenuItem styleItem = createMenuItem("Cambiar estilo...", AppIcons.propertiesIcon());
        styleItem.addActionListener(ev -> LayerPropertiesDialog.open(selectedLayer));
        simbologiaMenu.add(styleItem);

        JMenuItem labelsItem = createMenuItem("Etiquetas...", AppIcons.labelsIcon());
        labelsItem.addActionListener(ev -> LayerPropertiesDialog.open(selectedLayer));
        simbologiaMenu.add(labelsItem);

        JMenuItem clearLabelsItem = createMenuItem("Quitar etiquetas", AppIcons.labelsIcon());
        clearLabelsItem.addActionListener(ev -> {
            selectedLayer.setLabelsVisible(false);
            selectedLayer.setLabelField(null);
            refreshLayerList();
            CatgisDesktopApp.mapPanel.repaint();
        });
        simbologiaMenu.add(clearLabelsItem);

        popupMenu.add(simbologiaMenu);

        // Heatmap toggle (point layers only)
        JCheckBoxMenuItem heatmapItem = new JCheckBoxMenuItem("Mapa de calor (heatmap)", selectedLayer.isHeatmapEnabled());
        heatmapItem.addActionListener(ev -> {
            selectedLayer.setHeatmapEnabled(!selectedLayer.isHeatmapEnabled());
            CatgisDesktopApp.mapPanel.repaint();
        });
        popupMenu.add(heatmapItem);

        JCheckBoxMenuItem clusterItem = new JCheckBoxMenuItem("Agrupar puntos (clustering)", selectedLayer.isClusteringEnabled());
        clusterItem.addActionListener(ev -> {
            selectedLayer.setClusteringEnabled(!selectedLayer.isClusteringEnabled());
            CatgisDesktopApp.mapPanel.repaint();
        });
        popupMenu.add(clusterItem);

        // Environmental area marking
        JMenuItem envMarkItem = createMenuItem("Marcar como área de influencia...", AppIcons.propertiesIcon());
        envMarkItem.addActionListener(ev -> {
            ar.com.catgis.climate.EnvironmentalAreaMarker.markSingleLayer(
                SwingUtilities.getWindowAncestor(this), selectedLayer);
            ar.com.catgis.climate.EnvironmentalAreaMarker.showMarkDialog(
                SwingUtilities.getWindowAncestor(this));
        });
        popupMenu.add(envMarkItem);
        popupMenu.addSeparator();

        JMenu advancedMenu = new JMenu("Configuracion avanzada");
        advancedMenu.setIcon(AppIcons.crsIcon());

        JMenuItem propertiesRealItem = createMenuItem("Propiedades", AppIcons.propertiesIcon());
        propertiesRealItem.addActionListener(ev -> LayerPropertiesDialog.open(selectedLayer));
        advancedMenu.add(propertiesRealItem);

        JMenuItem viewCRSItem = createMenuItem("Ver CRS de capa", AppIcons.crsIcon());
        viewCRSItem.addActionListener(ev -> showLayerCRS(selectedLayer));
        advancedMenu.add(viewCRSItem);

        JMenuItem setCRSItem = createMenuItem("Definir CRS de capa", AppIcons.crsIcon());
        setCRSItem.addActionListener(ev -> defineLayerCRS(selectedLayer));
        advancedMenu.add(setCRSItem);

        if (CadLayerSupport.isCadLayer(selectedLayer)) {
            JMenuItem cadGeorefItem = createMenuItem("Georreferenciar CAD por puntos...", AppIcons.crsIcon());
            cadGeorefItem.addActionListener(ev -> georeferenceCadLayer(selectedLayer));
            advancedMenu.add(cadGeorefItem);

            JMenuItem cadPlacementItem = createMenuItem("Ajuste CAD...", AppIcons.moveFeatureIcon());
            cadPlacementItem.addActionListener(ev -> editCadPlacement(selectedLayer));
            advancedMenu.add(cadPlacementItem);

            JMenuItem cadDragPlacementItem = createMenuItem("Arrastrar CAD en mapa...", AppIcons.moveFeatureIcon());
            cadDragPlacementItem.addActionListener(ev -> CadWorkflowSupport.openCadDragPlacementWorkflow(this, selectedLayer));
            advancedMenu.add(cadDragPlacementItem);

            JMenuItem cadInternalLayersItem = createMenuItem("Capas internas CAD...", AppIcons.tableIcon());
            cadInternalLayersItem.addActionListener(ev -> CadWorkflowSupport.openCadInternalLayers(this, selectedLayer));
            advancedMenu.add(cadInternalLayersItem);

            JMenuItem cadDiagItem = createMenuItem("Diagnostico DWG/CAD...", AppIcons.propertiesIcon());
            cadDiagItem.addActionListener(ev -> CadIntegrationDialog.open());
            advancedMenu.add(cadDiagItem);

            JMenuItem exportCadAdjustedItem = createMenuItem("Exportar CAD georreferenciado...", AppIcons.exportIcon());
            exportCadAdjustedItem.addActionListener(ev -> ExportVectorLayerAction.exportLayer(selectedLayer));
            advancedMenu.add(exportCadAdjustedItem);
        }

        JMenuItem exportItem = createMenuItem("Exportar capa", AppIcons.exportIcon());
        exportItem.addActionListener(ev -> ExportVectorLayerAction.exportLayer(selectedLayer));
        advancedMenu.add(exportItem);

        if (selectedLayer != null && "VECTOR".equalsIgnoreCase(selectedLayer.getType())) {
            JMenuItem exportDxfItem = createMenuItem("Exportar a DXF...", AppIcons.exportIcon());
            exportDxfItem.addActionListener(ev -> DxfExportEngine.exportLayerWithDialog(null, selectedLayer));
            advancedMenu.add(exportDxfItem);
        }

        JMenuItem sendPostgisItem = createMenuItem("Enviar a CATSERVER...", AppIcons.tableIcon());
        sendPostgisItem.addActionListener(ev -> PostgisDataSourceAction.exportLayerToPostgis(selectedLayer));
        advancedMenu.add(sendPostgisItem);

        if (selectedLayer instanceof OnlineWfsLayer) {
            JMenuItem infoItem = createMenuItem("Informacion del servicio...", AppIcons.propertiesIcon());
            infoItem.addActionListener(ev -> showOnlineWfsInfo((OnlineWfsLayer) selectedLayer));
            advancedMenu.add(infoItem);
        } else if (selectedLayer instanceof PostgisLayer) {
            if (!readOnlyVector) {
                JMenuItem savePostgisItem = createMenuItem("Guardar cambios en CATSERVER", AppIcons.saveIcon());
                savePostgisItem.addActionListener(ev -> PostgisDataSourceAction.savePostgisLayerChanges((PostgisLayer) selectedLayer));
                advancedMenu.add(savePostgisItem);
            }
            JMenuItem infoItem = createMenuItem("Informacion de la conexion...", AppIcons.propertiesIcon());
            infoItem.addActionListener(ev -> showPostgisInfo((PostgisLayer) selectedLayer));
            advancedMenu.add(infoItem);
        } else if (selectedLayer instanceof GeoPackageLayer) {
            JMenuItem infoItem = createMenuItem("Informacion GeoPackage...", AppIcons.propertiesIcon());
            infoItem.addActionListener(ev -> showGeoPackageInfo((GeoPackageLayer) selectedLayer));
            advancedMenu.add(infoItem);
        }

        JMenuItem renameItem = createMenuItem("Renombrar", AppIcons.renameIcon());
        renameItem.addActionListener(ev -> renameLayer(selectedLayer));
        advancedMenu.add(renameItem);

        popupMenu.add(advancedMenu);
        popupMenu.addSeparator();

        JMenuItem fieldsItem = createMenuItem("Ver/Editar campos...", AppIcons.fieldsIcon());
        fieldsItem.addActionListener(ev -> OpenAttributeTableAction.openFieldsConfig(selectedLayer));
        popupMenu.add(fieldsItem);

        JMenuItem attributeTableItem = createMenuItem("Ver/Editar atributos...", AppIcons.tableIcon());
        attributeTableItem.addActionListener(ev -> OpenAttributeTableAction.openTable(selectedLayer));
        popupMenu.add(attributeTableItem);

        JMenuItem queryBuilderItem = createMenuItem("Constructor de consultas...", AppIcons.identifyIcon());
        queryBuilderItem.addActionListener(ev -> QueryBuilderDialog.open(selectedLayer));
        popupMenu.add(queryBuilderItem);

        popupMenu.addSeparator();
        addCommonBottomItems(popupMenu, selectedLayer);
        return popupMenu;
    }

    private JPopupMenu buildRasterPopup(Layer selectedLayer) {
        if (selectedLayer instanceof OnlineTileLayer || selectedLayer instanceof OnlineWmsLayer) {
            return buildOnlineRasterPopup(selectedLayer);
        }

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem propertiesItem = createMenuItem(selectedLayer.getName(), AppIcons.propertiesIcon());
        propertiesItem.setEnabled(false);
        popupMenu.add(propertiesItem);
        popupMenu.addSeparator();

        addCommonTopItems(popupMenu, selectedLayer, "Ocultar raster", "Mostrar raster");

        JMenuItem rasterInfoItem = createMenuItem("Informacion raster...", AppIcons.identifyIcon());
        rasterInfoItem.addActionListener(ev -> showRasterInfo(selectedLayer));
        popupMenu.add(rasterInfoItem);

        if (selectedLayer instanceof RasterLayer rasterLayer && hasProRasterMetadata(rasterLayer)) {
            JMenuItem proInfoItem = createMenuItem("Informacion raster avanzada...", AppIcons.imageryIcon());
            proInfoItem.addActionListener(ev -> showProRasterInfo(rasterLayer));
            popupMenu.add(proInfoItem);

            JMenuItem exportProReportItem = createMenuItem("Exportar ficha raster...", AppIcons.exportIcon());
            exportProReportItem.addActionListener(ev -> exportProRasterReport(rasterLayer));
            popupMenu.add(exportProReportItem);
        }

        JMenuItem displayItem = createMenuItem("Ajustes de visualizacion...", AppIcons.attrEditIcon());
        displayItem.addActionListener(ev -> openRasterDisplaySettings(selectedLayer));
        popupMenu.add(displayItem);

        boolean derivedRaster = isDerivedRasterLayer(selectedLayer);
        if (selectedLayer instanceof RasterLayer rasterLayer && hasProRasterMetadata(rasterLayer) && !derivedRaster) {
            JMenu proOutputsMenu = new JMenu("Salidas raster avanzadas");
            proOutputsMenu.setIcon(AppIcons.imageryIcon());

            JMenuItem thematicItem = createMenuItem("Generar mapa tematico raster", AppIcons.imageryIcon());
            thematicItem.setEnabled(ProRasterDerivedService.supportsThematicOutput(rasterLayer));
            thematicItem.addActionListener(ev -> generateProDerivedRaster(rasterLayer, false));
            proOutputsMenu.add(thematicItem);

            JMenuItem qaItem = createMenuItem(ProRasterDerivedService.qaMenuLabel(rasterLayer), AppIcons.propertiesIcon());
            qaItem.addActionListener(ev -> generateProDerivedRaster(rasterLayer, true));
            proOutputsMenu.add(qaItem);

            if (ProRasterDerivedService.supportsLandsatQaPixelMasks(rasterLayer)) {
                JMenu landsatMasksMenu = new JMenu("Mascaras Landsat QA_PIXEL");
                landsatMasksMenu.setIcon(AppIcons.propertiesIcon());

                JMenuItem cloudsMaskItem = createMenuItem(
                        ProRasterDerivedService.landsatQaMaskMenuLabel(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_CLOUDS),
                        AppIcons.propertiesIcon()
                );
                cloudsMaskItem.addActionListener(ev -> generateProDerivedRaster(rasterLayer, ProRasterDerivedService.OP_PRO_MASK_LANDSAT_CLOUDS));
                landsatMasksMenu.add(cloudsMaskItem);

                JMenuItem shadowMaskItem = createMenuItem(
                        ProRasterDerivedService.landsatQaMaskMenuLabel(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SHADOW),
                        AppIcons.propertiesIcon()
                );
                shadowMaskItem.addActionListener(ev -> generateProDerivedRaster(rasterLayer, ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SHADOW));
                landsatMasksMenu.add(shadowMaskItem);

                JMenuItem snowMaskItem = createMenuItem(
                        ProRasterDerivedService.landsatQaMaskMenuLabel(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SNOW),
                        AppIcons.propertiesIcon()
                );
                snowMaskItem.addActionListener(ev -> generateProDerivedRaster(rasterLayer, ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SNOW));
                landsatMasksMenu.add(snowMaskItem);

                JMenuItem waterMaskItem = createMenuItem(
                        ProRasterDerivedService.landsatQaMaskMenuLabel(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_WATER),
                        AppIcons.propertiesIcon()
                );
                waterMaskItem.addActionListener(ev -> generateProDerivedRaster(rasterLayer, ProRasterDerivedService.OP_PRO_MASK_LANDSAT_WATER));
                landsatMasksMenu.add(waterMaskItem);

                proOutputsMenu.add(landsatMasksMenu);
            }

            JMenuItem compareItem = createMenuItem("Comparar con otra fecha...", AppIcons.attrRefreshIcon());
            compareItem.addActionListener(ev -> compareProRasterWithAnotherDate(rasterLayer));
            compareItem.setEnabled(findComparableProLayers(rasterLayer).size() > 0);
            proOutputsMenu.add(compareItem);

            popupMenu.add(proOutputsMenu);
        }

        String currentMode = getRasterMode(selectedLayer);
        JMenuItem currentModeItem = createMenuItem("Modo actual: " + getRasterModeLabel(currentMode), AppIcons.zoomLayerIcon());
        currentModeItem.setEnabled(false);
        popupMenu.add(currentModeItem);

        JMenuItem quickItem = createMenuItem("Vista rapida", AppIcons.zoomOutIcon());
        quickItem.setEnabled(!derivedRaster && !RasterImageLoader.MODE_PREVIEW.equalsIgnoreCase(currentMode));
        quickItem.addActionListener(ev -> reloadRasterMode(selectedLayer, RasterImageLoader.MODE_PREVIEW));
        popupMenu.add(quickItem);

        JMenuItem virtualItem = createMenuItem("Zoom virtual", AppIcons.zoomLayerIcon());
        virtualItem.setEnabled(!derivedRaster && !RasterImageLoader.MODE_VIRTUAL.equalsIgnoreCase(currentMode));
        virtualItem.addActionListener(ev -> reloadRasterMode(selectedLayer, RasterImageLoader.MODE_VIRTUAL));
        popupMenu.add(virtualItem);

        JMenuItem realItem = createMenuItem("Zoom real", AppIcons.zoomInIcon());
        realItem.setEnabled(!derivedRaster && !RasterImageLoader.MODE_REAL.equalsIgnoreCase(currentMode));
        realItem.addActionListener(ev -> reloadRasterMode(selectedLayer, RasterImageLoader.MODE_REAL));
        popupMenu.add(realItem);

        JMenuItem contourItem = createMenuItem(I18n.t("Generar curvas de nivel..."), AppIcons.propertiesIcon());
        contourItem.addActionListener(ev -> ContourGenerationDialog.open(selectedLayer));
        popupMenu.add(contourItem);

        JMenuItem drainageItem = createMenuItem(I18n.t("Generar escorrentias..."), AppIcons.drainageIcon());
        drainageItem.addActionListener(ev -> DrainageExtractionDialog.open(selectedLayer));
        popupMenu.add(drainageItem);

        JMenuItem terrainAnalysisItem = createMenuItem(I18n.t("Analisis topohidrologico..."), AppIcons.terrainAnalysisIcon());
        terrainAnalysisItem.addActionListener(ev -> TerrainHydrologyAnalysisDialog.open(selectedLayer));
        popupMenu.add(terrainAnalysisItem);

        JMenuItem basinOutletItem = createMenuItem(I18n.t("Cuenca desde outlet..."), AppIcons.pointIcon());
        basinOutletItem.addActionListener(ev -> BasinFromOutletDialog.open(selectedLayer));
        popupMenu.add(basinOutletItem);

        popupMenu.addSeparator();

        // Climate visualization
        JMenu climateMenu = new JMenu("Clima y ambiente");
        climateMenu.setIcon(AppIcons.imageryIcon());

        JMenuItem climateSymbologyItem = createMenuItem("Aplicar simbología climática...", AppIcons.attrEditIcon());
        climateSymbologyItem.setEnabled(selectedLayer instanceof RasterLayer);
        climateSymbologyItem.addActionListener(ev -> {
            if (selectedLayer instanceof RasterLayer rl) {
                ar.com.catgis.climate.ClimateVisualizationDialog.open(
                    SwingUtilities.getWindowAncestor(this), rl);
            }
        });
        climateMenu.add(climateSymbologyItem);

        JMenuItem areaAnalysisItem = createMenuItem("Análisis climático por áreas (AID/AII)...", AppIcons.propertiesIcon());
        areaAnalysisItem.addActionListener(ev -> {
            ar.com.catgis.climate.ClimateAreaAnalysisDialog.open(
                SwingUtilities.getWindowAncestor(this));
        });
        climateMenu.add(areaAnalysisItem);

        JMenuItem windRoseItem = createMenuItem("Rosa de los vientos...", AppIcons.attrRefreshIcon());
        windRoseItem.addActionListener(ev -> {
            ar.com.catgis.climate.WindRoseDialog.open(
                SwingUtilities.getWindowAncestor(this));
        });
        climateMenu.add(windRoseItem);

        popupMenu.add(climateMenu);
        popupMenu.addSeparator();

        JMenu advancedMenu = new JMenu("Configuracion avanzada");
        advancedMenu.setIcon(AppIcons.toolboxIcon());

        JMenuItem viewCRSItem = createMenuItem("Ver CRS de capa", AppIcons.crsIcon());
        viewCRSItem.addActionListener(ev -> showLayerCRS(selectedLayer));
        advancedMenu.add(viewCRSItem);

        JMenuItem setCRSItem = createMenuItem("Definir CRS de capa", AppIcons.crsIcon());
        setCRSItem.addActionListener(ev -> defineLayerCRS(selectedLayer));
        advancedMenu.add(setCRSItem);

        JMenuItem renameItem = createMenuItem("Renombrar", AppIcons.renameIcon());
        renameItem.addActionListener(ev -> renameLayer(selectedLayer));
        advancedMenu.add(renameItem);

        popupMenu.add(advancedMenu);
        popupMenu.addSeparator();
        addCommonBottomItems(popupMenu, selectedLayer);
        return popupMenu;
    }

    private JPopupMenu buildOnlineRasterPopup(Layer selectedLayer) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem propertiesItem = createMenuItem(selectedLayer.getName(), AppIcons.imageryIcon());
        propertiesItem.setEnabled(false);
        popupMenu.add(propertiesItem);
        popupMenu.addSeparator();

        addCommonTopItems(popupMenu, selectedLayer, "Ocultar capa online", "Mostrar capa online");

        JMenuItem infoItem = createMenuItem("Informacion de servicio...", AppIcons.identifyIcon());
        infoItem.addActionListener(ev -> showRasterInfo(selectedLayer));
        popupMenu.add(infoItem);

        JMenu advancedMenu = new JMenu("Configuracion");
        advancedMenu.setIcon(AppIcons.toolboxIcon());

        JMenuItem renameItem = createMenuItem("Renombrar", AppIcons.renameIcon());
        renameItem.addActionListener(ev -> renameLayer(selectedLayer));
        advancedMenu.add(renameItem);

        popupMenu.add(advancedMenu);
        popupMenu.addSeparator();
        addCommonBottomItems(popupMenu, selectedLayer);
        return popupMenu;
    }

    private void addCommonTopItems(JPopupMenu popupMenu, Layer selectedLayer, String hideText, String showText) {
        JMenuItem zoomToLayerItem = createMenuItem("Zoom a la capa", AppIcons.zoomLayerIcon());
        zoomToLayerItem.addActionListener(ev -> CatgisDesktopApp.mapPanel.zoomToLayer(selectedLayer));
        popupMenu.add(zoomToLayerItem);

        JMenuItem toggleVisibilityItem = createMenuItem(
                selectedLayer.isVisible() ? hideText : showText,
                selectedLayer.isVisible() ? AppIcons.hiddenIcon() : AppIcons.visibleIcon()
        );
        toggleVisibilityItem.addActionListener(ev -> {
            selectedLayer.setVisible(!selectedLayer.isVisible());
            CatgisDesktopApp.markProjectDirty();
            refreshLayerList();
            CatgisDesktopApp.mapPanel.repaint();
        });
        popupMenu.add(toggleVisibilityItem);
    }

    private void addCommonBottomItems(JPopupMenu popupMenu, Layer selectedLayer) {
        List<Layer> popupTargetLayers = resolvePopupTargetLayers(selectedLayer);
        boolean multipleSelection = popupTargetLayers.size() > 1;

        JMenuItem newGroupItem = createMenuItem(
                multipleSelection ? I18n.format("Crear grupo con seleccion ({0})", popupTargetLayers.size()) : "Crear grupo con capa",
                AppIcons.openIcon()
        );
        newGroupItem.addActionListener(ev -> createNewGroupForLayers(popupTargetLayers));
        popupMenu.add(newGroupItem);

        JMenu moveToGroupMenu = new JMenu("Mover a grupo");
        moveToGroupMenu.setIcon(AppIcons.openIcon());
        populateMoveToGroupMenu(moveToGroupMenu, popupTargetLayers);
        popupMenu.add(moveToGroupMenu);

        JMenuItem ungroupItem = createMenuItem("Sacar del grupo", AppIcons.removeIcon());
        ungroupItem.setEnabled(popupTargetLayers.stream().anyMatch(Layer::isInGroup));
        ungroupItem.addActionListener(ev -> removeLayersFromGroup(popupTargetLayers));
        popupMenu.add(ungroupItem);

        popupMenu.addSeparator();

        JMenuItem moveUpItem = createMenuItem("Subir", AppIcons.upIcon());
        moveUpItem.addActionListener(ev -> moveLayerUp(selectedLayer));
        moveUpItem.setEnabled(!multipleSelection);
        popupMenu.add(moveUpItem);

        JMenuItem moveDownItem = createMenuItem("Bajar", AppIcons.downIcon());
        moveDownItem.addActionListener(ev -> moveLayerDown(selectedLayer));
        moveDownItem.setEnabled(!multipleSelection);
        popupMenu.add(moveDownItem);

        String removeLabel = multipleSelection
                ? I18n.format("Quitar seleccionadas ({0})", popupTargetLayers.size())
                : I18n.t("Quitar");
        JMenuItem removeItem = createMenuItem(removeLabel, AppIcons.removeIcon());
        removeItem.addActionListener(ev -> removeLayersFromProject(popupTargetLayers));
        popupMenu.add(removeItem);
    }

    private JMenuItem createMenuItem(String text, javax.swing.Icon icon) {
        return new JMenuItem(text, icon);
    }

    private Layer asLayer(Object value) {
        return value instanceof Layer ? (Layer) value : null;
    }

    private LayerGroup asGroup(Object value) {
        return value instanceof LayerGroup ? (LayerGroup) value : null;
    }

    private void restoreSelection(List<Object> previousSelection) {
        if (previousSelection == null || previousSelection.isEmpty()) {
            return;
        }

        List<Integer> indices = new ArrayList<>();
        for (Object selected : previousSelection) {
            for (int i = 0; i < model.size(); i++) {
                Object current = model.get(i);
                if (selected instanceof Layer layer && current == layer) {
                    indices.add(i);
                    break;
                }
                if (selected instanceof LayerGroup group && current instanceof LayerGroup currentGroup
                        && group.getName().equalsIgnoreCase(currentGroup.getName())) {
                    indices.add(i);
                    break;
                }
            }
        }
        if (!indices.isEmpty()) {
            int[] values = indices.stream().mapToInt(Integer::intValue).toArray();
            layerList.setSelectedIndices(values);
        }
    }

    private void createNewGroupFromSelection() {
        createNewGroupForLayers(getSelectedLayers());
    }

    private void createNewGroupForLayers(List<Layer> layers) {
        if (AppContext.project() == null) {
            return;
        }
        String name = JOptionPane.showInputDialog(this, "Nombre del grupo:", "Nuevo grupo");
        if (name == null) {
            return;
        }
        String trimmed = name.trim();
        if (trimmed.isBlank()) {
            JOptionPane.showMessageDialog(this, "Ingresa un nombre valido para el grupo.", "Grupos de capas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LayerGroup group = AppContext.project().addLayerGroup(trimmed);
        if (layers != null) {
            for (Layer layer : layers) {
                AppContext.project().assignLayerToGroup(layer, group.getName());
            }
        }
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
    }

    private void populateMoveToGroupMenu(JMenu menu, List<Layer> layers) {
        if (AppContext.project() == null) {
            menu.setEnabled(false);
            return;
        }
        if (AppContext.project().getLayerGroups().isEmpty()) {
            JMenuItem empty = createMenuItem("No hay grupos todavia", AppIcons.openIcon());
            empty.setEnabled(false);
            menu.add(empty);
            return;
        }
        for (LayerGroup group : AppContext.project().getLayerGroups()) {
            JMenuItem item = createMenuItem(group.getName(), AppIcons.openIcon());
            item.addActionListener(ev -> moveLayersToGroup(layers, group.getName()));
            menu.add(item);
        }
    }

    private void moveLayersToGroup(List<Layer> layers, String groupName) {
        if (AppContext.project() == null || layers == null || layers.isEmpty()) {
            return;
        }
        for (Layer layer : layers) {
            AppContext.project().assignLayerToGroup(layer, groupName);
        }
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
        CatgisDesktopApp.mapPanel.repaint();
    }

    private void removeLayersFromGroup(List<Layer> layers) {
        if (AppContext.project() == null || layers == null || layers.isEmpty()) {
            return;
        }
        for (Layer layer : layers) {
            if (layer != null) {
                layer.setGroupName("");
            }
        }
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
    }

    private void renameGroup(LayerGroup group) {
        if (group == null || AppContext.project() == null) {
            return;
        }
        String newName = JOptionPane.showInputDialog(this, "Nuevo nombre del grupo:", group.getName());
        if (newName == null) {
            return;
        }
        if (newName.trim().isBlank()) {
            JOptionPane.showMessageDialog(this, "Ingresa un nombre valido para el grupo.", "Grupos de capas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AppContext.project().renameLayerGroup(group.getName(), newName.trim());
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
    }

    private void removeGroup(LayerGroup group) {
        if (group == null || AppContext.project() == null) {
            return;
        }
        int option = JOptionPane.showConfirmDialog(
                this,
                "Se quitara el grupo \"" + group.getName() + "\".\nLas capas quedaran sueltas en el proyecto.\n\nQueres continuar?",
                "Quitar grupo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        AppContext.project().removeLayerGroup(group.getName(), true);
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
    }

    private void moveGroup(LayerGroup group, int delta) {
        if (group == null || AppContext.project() == null) {
            return;
        }
        List<LayerGroup> groups = new ArrayList<>(AppContext.project().getLayerGroups());
        int index = groups.indexOf(group);
        int target = index + delta;
        if (index < 0 || target < 0 || target >= groups.size()) {
            return;
        }
        groups.remove(index);
        groups.add(target, group);
        List<String> orderedNames = new ArrayList<>();
        for (LayerGroup value : groups) {
            if (value != null) {
                orderedNames.add(value.getName());
            }
        }
        AppContext.project().setLayerGroupOrder(orderedNames);
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
    }

    private boolean isRasterLayer(Layer layer) {
        if (layer == null) {
            return false;
        }
        if (layer instanceof RasterLayer) {
            return true;
        }

        String type = safeUpper(layer.getType());
        if (type.contains("RASTER") || type.contains("IMAGE") || type.contains("IMAGEN")) {
            return true;
        }

        String path = layer.getPath();
        if (path == null) {
            return false;
        }
        String p = path.toLowerCase();
        return p.endsWith(".tif") || p.endsWith(".tiff") || p.endsWith(".jpg") || p.endsWith(".jpeg")
                || p.endsWith(".png") || p.endsWith(".bmp") || p.endsWith(".gif")
                || p.endsWith(".img") || p.endsWith(".ecw");
    }

    private void openVectorEditing(Layer layer) {
        if (layer == null || isRasterLayer(layer)) {
            return;
        }

        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.prepareLayerForEditing(layer);
            CatgisDesktopApp.mapPanel.zoomToLayer(layer);
        }

        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        EditingToolsWindow.showWindow();

        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(
                    "Editando capa: " + layer.getName()
            );
        }
    }

    private void showLayerCRS(Layer layer) {
        String source = (layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank())
                ? CRSDefinitions.getLabelForCode(layer.getSourceCRS())
                : "Desconocido";

        String project = (AppContext.project() != null)
                ? CRSDefinitions.getLabelForCode(AppContext.project().getProjectCRS())
                : "-";

        StringBuilder text = new StringBuilder();
        text.append("CRS de la capa: ").append(source).append("\n");
        text.append("CRS del proyecto: ").append(project);
        if (CadLayerSupport.isCadLayer(layer)) {
            text.append("\nGeorreferenciacion CAD: ").append(CadGeoreferenceSupport.buildDetailedSummary(layer));
            text.append("\nAjuste CAD: ").append(CadPlacementSupport.buildPlacementSummary(layer));
            text.append("\nCapas internas CAD: ").append(CadLayerSupport.buildCadInternalLayerFilterLabel(layer));
        }
        showScrollableInfoDialog("CRS de capa", text.toString());
    }

    private void showScrollableInfoDialog(String title, String content) {
        JTextArea infoArea = new JTextArea(content != null ? content : "");
        infoArea.setEditable(false);
        infoArea.setLineWrap(false);
        infoArea.setWrapStyleWord(false);
        infoArea.setCaretPosition(0);
        infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Math.max(12, infoArea.getFont().getSize())));
        infoArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(
                infoArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);

        Dimension screenSize = getToolkit() != null ? getToolkit().getScreenSize() : new Dimension(1366, 768);
        int preferredWidth = Math.max(520, Math.min(920, screenSize.width - 140));
        int preferredHeight = Math.max(280, Math.min(560, screenSize.height - 180));
        scrollPane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void defineLayerCRS(Layer layer) {
        if (CadLayerSupport.isCadLayer(layer)) {
            CadCrsAssignmentDialog.Result result = CadCrsAssignmentDialog.chooseForLayer(this, layer);
            if (result.selectorRequested()) {
                String chosenCode = CRSSelectorDialog.chooseBlocking(
                        CatgisDesktopApp.getMainFrameSafe(),
                        "Seleccionar CRS para CAD",
                        result.sourceCrs()
                );
                if (chosenCode == null || chosenCode.isBlank()) {
                    return;
                }
                applyLayerCrsChange(layer, chosenCode);
                return;
            }
            if (!result.approved()) {
                return;
            }
            applyLayerCrsChange(layer, result.sourceCrs());
            return;
        }
        CRSSelectorDialog.open("Definir CRS de capa", layer.getSourceCRS(), code -> {
            applyLayerCrsChange(layer, code);
        });
    }

    private void editCadPlacement(Layer layer) {
        java.awt.Frame owner = CatgisDesktopApp.getMainFrame();
        CadPlacementSupport.Result result = CadPlacementDialog.open(owner, layer);
        if (!result.approved()) {
            return;
        }
        layer.setCadOffsetX(result.offsetX());
        layer.setCadOffsetY(result.offsetY());
        layer.setCadScale(result.scale());
        layer.setCadRotationDegrees(result.rotationDegrees());
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
        CatgisDesktopApp.mapPanel.resetView();
        CatgisDesktopApp.mapPanel.repaint();
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Ajuste CAD actualizado: " + layer.getName() + " -> " + CadPlacementSupport.buildPlacementSummary(layer));
        }
    }

    private void georeferenceCadLayer(Layer layer) {
        CadWorkflowSupport.openGeoreferenceWorkflow(this, layer);
    }

    private void applyLayerCrsChange(Layer layer, String code) {
        layer.setSourceCRS(code);
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();

        if (isRasterLayer(layer)) {
            reloadRasterMode(layer, getRasterMode(layer), true, true);
        } else {
            CatgisDesktopApp.mapPanel.resetView();
            CatgisDesktopApp.mapPanel.repaint();
        }

        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(
                    "CRS de capa actualizado: " + layer.getName() + " -> " + CadLayerSupport.formatSourceCrsLabel(code)
            );
        }
    }

    private void renameLayer(Layer layer) {
        String newName = JOptionPane.showInputDialog(this, "Nuevo nombre para la capa:", layer.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            layer.setName(newName.trim());
            CatgisDesktopApp.markProjectDirty();
            refreshLayerList();
            CatgisDesktopApp.mapPanel.repaint();
        }
    }

    private void moveLayerUp(Layer layer) {
        moveLayerWithinGroup(layer, -1);
    }

    private void moveLayerDown(Layer layer) {
        moveLayerWithinGroup(layer, 1);
    }

    private void moveLayerWithinGroup(Layer layer, int delta) {
        if (layer == null || AppContext.project() == null) {
            return;
        }
        List<Layer> ordered = new ArrayList<>(AppContext.project().getLayers());
        List<Layer> siblings = new ArrayList<>();
        for (Layer candidate : ordered) {
            if (candidate == null) {
                continue;
            }
            if (sameGroup(candidate, layer)) {
                siblings.add(candidate);
            }
        }
        int siblingIndex = siblings.indexOf(layer);
        int targetIndex = siblingIndex + delta;
        if (siblingIndex < 0 || targetIndex < 0 || targetIndex >= siblings.size()) {
            return;
        }
        Layer target = siblings.get(targetIndex);
        moveLayerNearTarget(layer, target, delta > 0);
    }

    private void moveLayerNearTarget(Layer layer, Layer target, boolean afterTarget) {
        if (layer == null || target == null || AppContext.project() == null || layer == target) {
            return;
        }
        List<Layer> ordered = new ArrayList<>(AppContext.project().getLayers());
        if (!ordered.remove(layer)) {
            return;
        }
        int targetIndex = ordered.indexOf(target);
        if (targetIndex < 0) {
            ordered.add(layer);
        } else {
            ordered.add(afterTarget ? targetIndex + 1 : targetIndex, layer);
        }
        AppContext.project().setLayerOrder(ordered);
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.reorderLayers(ordered);
        }
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
        selectLayer(layer);
    }

    private boolean sameGroup(Layer a, Layer b) {
        String groupA = a != null ? a.getGroupName() : "";
        String groupB = b != null ? b.getGroupName() : "";
        return groupA.equalsIgnoreCase(groupB);
    }

    private int resolveDropInsertIndex(java.awt.Point point) {
        if (model.isEmpty()) {
            return -1;
        }

        Rectangle firstBounds = layerList.getCellBounds(0, 0);
        if (firstBounds != null && point.y < firstBounds.y) {
            return 0;
        }

        Rectangle lastBounds = layerList.getCellBounds(model.size() - 1, model.size() - 1);
        if (lastBounds != null && point.y > lastBounds.y + lastBounds.height) {
            return model.size();
        }

        int index = layerList.locationToIndex(point);
        if (index < 0) {
            return model.size();
        }

        Rectangle bounds = layerList.getCellBounds(index, index);
        if (bounds == null) {
            return index;
        }

        if (point.y > bounds.y + (bounds.height / 2)) {
            return Math.min(index + 1, model.size());
        }
        return Math.max(0, index);
    }

    private void handleLayerDrop(int sourceIndex, java.awt.Point point, int insertIndex) {
        if (sourceIndex < 0 || sourceIndex >= model.size()) {
            return;
        }

        if (layerList.getSelectedIndices().length > 1) {
            return;
        }

        Layer sourceLayer = asLayer(model.get(sourceIndex));
        if (sourceLayer == null) {
            return;
        }

        if (insertIndex == sourceIndex || insertIndex == sourceIndex + 1) {
            return;
        }

        Object directTarget = resolveDropTarget(point);
        if (directTarget instanceof LayerGroup targetGroup) {
            moveLayerIntoGroup(sourceLayer, targetGroup);
            return;
        }
        if (directTarget instanceof Layer targetLayer) {
            moveLayerToMatchTarget(sourceLayer, targetLayer, insertIndex > sourceIndex);
            return;
        }

        Layer targetLayer = null;
        int normalizedInsert = Math.max(0, Math.min(insertIndex, model.size() - 1));
        for (int probe = normalizedInsert; probe >= 0; probe--) {
            targetLayer = asLayer(model.get(probe));
            if (targetLayer != null) {
                break;
            }
        }
        if (targetLayer != null && sourceLayer != targetLayer) {
            moveLayerToMatchTarget(sourceLayer, targetLayer, insertIndex > sourceIndex);
            return;
        }

        if (point != null && isDropBeforeAllRows(point)) {
            sourceLayer.setGroupName("");
            moveLayerToTopOfUngrouped(sourceLayer);
        }
    }

    private Object resolveDropTarget(java.awt.Point point) {
        if (point == null) {
            return null;
        }
        int index = layerList.locationToIndex(point);
        if (index < 0 || index >= model.size()) {
            return null;
        }
        Rectangle bounds = layerList.getCellBounds(index, index);
        if (bounds == null || !bounds.contains(point)) {
            return null;
        }
        return model.get(index);
    }

    private boolean isDropBeforeAllRows(java.awt.Point point) {
        if (point == null || model.isEmpty()) {
            return false;
        }
        Rectangle firstBounds = layerList.getCellBounds(0, 0);
        return firstBounds != null && point.y < firstBounds.y;
    }

    private void moveLayerToMatchTarget(Layer sourceLayer, Layer targetLayer, boolean afterTarget) {
        if (sourceLayer == null || targetLayer == null) {
            return;
        }
        sourceLayer.setGroupName(targetLayer.getGroupName());
        moveLayerNearTarget(sourceLayer, targetLayer, afterTarget);
    }

    private void moveLayerIntoGroup(Layer sourceLayer, LayerGroup targetGroup) {
        if (sourceLayer == null || targetGroup == null || AppContext.project() == null) {
            return;
        }

        sourceLayer.setGroupName(targetGroup.getName());
        targetGroup.setExpanded(true);

        List<Layer> groupLayers = AppContext.project().getLayersForGroup(targetGroup.getName());
        Layer anchor = null;
        for (Layer layer : groupLayers) {
            if (layer != null && layer != sourceLayer) {
                anchor = layer;
            }
        }

        if (anchor != null) {
            moveLayerNearTarget(sourceLayer, anchor, true);
            return;
        }

        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
        selectLayer(sourceLayer);
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.repaint();
        }
    }

    private void moveLayerToTopOfUngrouped(Layer sourceLayer) {
        if (sourceLayer == null || AppContext.project() == null) {
            return;
        }
        List<Layer> ordered = new ArrayList<>(AppContext.project().getLayers());
        if (!ordered.remove(sourceLayer)) {
            return;
        }
        ordered.add(0, sourceLayer);
        AppContext.project().setLayerOrder(ordered);
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.reorderLayers(ordered);
        }
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
        selectLayer(sourceLayer);
    }

    private void applyLayerOrderFromModel() {
        List<Layer> orderedLayers = new java.util.ArrayList<>();
        for (int i = 0; i < model.size(); i++) {
            Layer layer = asLayer(model.getElementAt(i));
            if (layer != null && !orderedLayers.contains(layer)) {
                orderedLayers.add(layer);
            }
        }

        if (AppContext.project() != null) {
            AppContext.project().setLayerOrder(orderedLayers);
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.reorderLayers(orderedLayers);
        }

        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
    }

    private void removeSelectedLayer(Layer layer) {
        if (layer == null) {
            return;
        }
        removeLayersFromProject(List.of(layer));
    }

    private List<Layer> resolvePopupTargetLayers(Layer selectedLayer) {
        List<Layer> selectedLayers = getOrderedSelectedLayers();
        if (selectedLayer != null && selectedLayers.size() > 1 && selectedLayers.contains(selectedLayer)) {
            return selectedLayers;
        }
        return selectedLayer == null ? List.of() : List.of(selectedLayer);
    }

    private List<Layer> getOrderedSelectedLayers() {
        List<Layer> ordered = new ArrayList<>();
        for (int index : layerList.getSelectedIndices()) {
            if (index >= 0 && index < model.size()) {
                Layer layer = asLayer(model.getElementAt(index));
                if (layer != null) {
                    ordered.add(layer);
                }
            }
        }
        return ordered;
    }

    private void removeSelectedLayersFromList() {
        removeLayersFromProject(getOrderedSelectedLayers());
    }

    private void removeLayersFromProject(Collection<Layer> layersToRemove) {
        removeLayersFromProject(layersToRemove, true);
    }

    private void removeLayersFromProject(Collection<Layer> layersToRemove, boolean askForConfirmation) {
        List<Layer> orderedLayers = new ArrayList<>();
        for (Layer layer : layersToRemove) {
            if (layer != null && model.contains(layer) && !orderedLayers.contains(layer)) {
                orderedLayers.add(layer);
            }
        }
        if (orderedLayers.isEmpty()) {
            return;
        }

        if (askForConfirmation && !confirmLayerRemoval(orderedLayers)) {
            return;
        }

        int[] selectedIndices = layerList.getSelectedIndices();
        int fallbackIndex = selectedIndices.length == 0 ? model.indexOf(orderedLayers.get(0)) : selectedIndices[0];

        for (Layer layer : orderedLayers) {
            model.removeElement(layer);
            if (AppContext.project() != null) {
                AppContext.project().removeLayer(layer);
            }
            OpenAttributeTableAction.closeOpenWindow(layer);
        }

        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.removeLayers(orderedLayers);
        }

        restoreSelectionAfterRemoval(fallbackIndex);
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();

        if (CatgisDesktopApp.statusBar != null) {
            if (orderedLayers.size() == 1) {
                AppContext.setStatusMessage(I18n.format("Capa quitada: {0}", orderedLayers.get(0).getName()));
            } else {
                AppContext.setStatusMessage(I18n.format("{0} capas quitadas del proyecto.", orderedLayers.size()));
            }
        }
    }

    private boolean confirmLayerRemoval(List<Layer> layersToRemove) {
        boolean removesEditingLayer = false;
        if (CatgisDesktopApp.mapPanel != null) {
            Layer editingLayer = CatgisDesktopApp.mapPanel.getEditingLayerRef();
            removesEditingLayer = editingLayer != null && layersToRemove.contains(editingLayer);
        }

        if (layersToRemove.size() == 1 && !removesEditingLayer) {
            return true;
        }

        String message;
        if (layersToRemove.size() == 1) {
            message = I18n.t("La capa seleccionada esta en edicion activa. Si la quitas, CATGIS cerrara esa sesion de edicion.\n\nQueres continuar?");
        } else if (removesEditingLayer) {
            message = I18n.format("Vas a quitar {0} capas, incluida una capa en edicion activa.\nCATGIS cerrara esa sesion de edicion y quitara las capas del proyecto.\n\nQueres continuar?", layersToRemove.size());
        } else {
            message = I18n.format("Vas a quitar {0} capas del proyecto.\n\nQueres continuar?", layersToRemove.size());
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                I18n.t("Quitar capas"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return option == JOptionPane.YES_OPTION;
    }

    private void restoreSelectionAfterRemoval(int fallbackIndex) {
        if (model.isEmpty()) {
            layerList.clearSelection();
            return;
        }
        int normalized = Math.max(0, Math.min(fallbackIndex, model.size() - 1));
        layerList.setSelectedIndex(normalized);
        layerList.ensureIndexIsVisible(normalized);
    }

    private void showRasterInfo(Layer layer) {
        if (layer instanceof OnlineTileLayer) {
            showOnlineTileInfo((OnlineTileLayer) layer);
            return;
        }
        if (layer instanceof OnlineWmsLayer) {
            showOnlineWmsInfo((OnlineWmsLayer) layer);
            return;
        }
        if (layer instanceof OnlineWfsLayer) {
            showOnlineWfsInfo((OnlineWfsLayer) layer);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(layer.getName()).append("\n");
        sb.append("Tipo: Raster\n");
        sb.append("Ruta: ").append(layer.getPath() != null ? layer.getPath() : "-").append("\n");
        sb.append("CRS: ").append(layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank()
                ? CRSDefinitions.getLabelForCode(layer.getSourceCRS()) : "Desconocido").append("\n");
        sb.append("Modo: ").append(getRasterModeLabel(getRasterMode(layer))).append("\n");
        if (isDerivedRasterLayer(layer) && layer instanceof RasterLayer rasterLayer) {
            sb.append("Derivado: ").append(rasterLayer.getDerivedOperation()).append("\n");
        }
        if (layer instanceof RasterLayer rasterLayer && hasProRasterMetadata(rasterLayer)) {
            appendProRasterInfo(sb, rasterLayer, false);
        }

        Object raster = getRasterDataFromMapPanel(layer);
        if (raster != null) {
            appendIfPresent(sb, raster, "getWidth", "Ancho: ", " px");
            appendIfPresent(sb, raster, "getHeight", "Alto: ", " px");
            appendIfPresent(sb, raster, "getBandCount", "Bandas: ", "");
            appendIfPresent(sb, raster, "isGeoreferenced", "Georreferenciado: ", "", true);
            appendEnvelopeIfPresent(sb, raster);
        } else {
            sb.append("Informacion raster adicional: no disponible en memoria.\n");
        }

        showScrollableInfoDialog("Informacion raster", sb.toString());
    }

    private void showProRasterInfo(RasterLayer rasterLayer) {
        if (rasterLayer == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(rasterLayer.getName()).append("\n");
        sb.append("Ruta raster: ").append(rasterLayer.getPath() != null ? rasterLayer.getPath() : "-").append("\n");
        appendProRasterInfo(sb, rasterLayer, true);
        showScrollableInfoDialog("Informacion raster avanzada", sb.toString());
    }

    private void exportProRasterReport(RasterLayer rasterLayer) {
        if (rasterLayer == null) {
            return;
        }

        JFileChooser chooser = FileChooserSupport.createChooser("pro-raster-report", "Exportar ficha raster");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Markdown (*.md)", "md"));
        chooser.setSelectedFile(FileChooserSupport.resolveSuggestedFile(
                "pro-raster-report",
                new File(ProRasterReportService.buildSuggestedFileName(rasterLayer))
        ));

        int result = chooser.showSaveDialog(CatgisDesktopApp.getMainFrameSafe());
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File targetFile = chooser.getSelectedFile();
        if (targetFile == null) {
            return;
        }
        File outputFile = ProRasterReportService.ensureMarkdownExtension(targetFile);
        FileChooserSupport.rememberFile("pro-raster-report", outputFile);

        if (outputFile.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    CatgisDesktopApp.getMainFrameSafe(),
                    "El archivo ya existe.\nDesea reemplazarlo?\n\n" + outputFile.getAbsolutePath(),
                    "Exportar ficha raster",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            LocalRasterData rasterData = CatgisDesktopApp.mapPanel != null
                    ? CatgisDesktopApp.mapPanel.getRasterData(rasterLayer)
                    : null;
            String projectName = AppContext.project() != null
                    ? nonBlank(AppContext.project().getName(), "Proyecto CATGIS")
                    : "Proyecto CATGIS";
            File saved = ProRasterReportService.exportMarkdownReport(rasterLayer, rasterData, projectName, outputFile);
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Ficha raster exportada: " + saved.getName());
            }
            JOptionPane.showMessageDialog(
                    CatgisDesktopApp.getMainFrameSafe(),
                    "Ficha raster exportada correctamente:\n" + saved.getAbsolutePath(),
                    "CATGIS",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    CatgisDesktopApp.getMainFrameSafe(),
                    "No se pudo exportar la ficha raster:\n" + ex.getMessage(),
                    "CATGIS",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void appendProRasterInfo(StringBuilder sb, RasterLayer rasterLayer, boolean detailed) {
        if (sb == null || rasterLayer == null) {
            return;
        }
        ProMetadataSidecarSupport.Metadata metadata = resolveProMetadata(rasterLayer);
        String effectiveMaturity = !rasterLayer.getProMaturityLevel().isBlank()
                ? rasterLayer.getProMaturityLevel()
                : metadata != null ? metadata.maturity() : "";
        sb.append("Dataset raster: ").append(nonBlank(rasterLayer.getProDatasetRef(), "-")).append("\n");
        sb.append("Variable Pro: ").append(nonBlank(rasterLayer.getProVariableName(), "-")).append("\n");
        if (!rasterLayer.getProAcquisitionStart().isBlank()) {
            sb.append("Tiempo Pro: ").append(rasterLayer.getProAcquisitionStart()).append("\n");
        }
        if (!effectiveMaturity.isBlank()) {
            sb.append("Madurez Pro: ").append(effectiveMaturity).append("\n");
            sb.append("Clasificacion metodologica: ")
                    .append(ProOceanColorPresetSupport.methodologyLabel(effectiveMaturity))
                    .append("\n");
        }
        if (!rasterLayer.getProMetadataSidecarPath().isBlank()) {
            sb.append("Sidecar Pro: ").append(rasterLayer.getProMetadataSidecarPath()).append("\n");
        }
        if (!rasterLayer.getProJobRef().isBlank()) {
            sb.append("Job Pro: ").append(rasterLayer.getProJobRef()).append("\n");
        }
        if (metadata == null) {
            if (detailed && !effectiveMaturity.isBlank()) {
                sb.append("Alcance: ").append(ProOceanColorPresetSupport.methodologyDescription(effectiveMaturity)).append("\n");
            }
            return;
        }
        sb.append("Preset tematico: ")
                .append(ProOceanColorPresetSupport.resolve(
                        metadata.variable(),
                        metadata.qualityPreset(),
                        metadata.flagsApplied(),
                        metadata.recipe(),
                        effectiveMaturity
                ).presetLabel())
                .append("\n");
        if (metadata.dataset() != null && !metadata.dataset().getProvider().isBlank()) {
            sb.append("Proveedor: ").append(metadata.dataset().getProvider()).append("\n");
        }
        if (metadata.dataset() != null && !metadata.dataset().getFamily().isBlank()) {
            sb.append("Familia: ").append(metadata.dataset().getFamily()).append("\n");
        }
        if (metadata.dataset() != null && !metadata.dataset().getPlatform().isBlank()) {
            sb.append("Plataforma: ").append(metadata.dataset().getPlatform()).append("\n");
        }
        if (metadata.dataset() != null && !metadata.dataset().getInstrument().isBlank()) {
            sb.append("Instrumento: ").append(metadata.dataset().getInstrument()).append("\n");
        }
        if (metadata.variable() != null && !metadata.variable().getLongName().isBlank()) {
            sb.append("Descripcion: ").append(metadata.variable().getLongName()).append("\n");
        }
        if (metadata.variable() != null && !metadata.variable().getUnits().isBlank()) {
            sb.append("Unidades: ").append(metadata.variable().getUnits()).append("\n");
        }
        if (metadata.variable() != null && !metadata.variable().getStandardName().isBlank()) {
            sb.append("standard_name: ").append(metadata.variable().getStandardName()).append("\n");
        }
        if (metadata.variable() != null && !metadata.variable().getDimensions().isEmpty()) {
            sb.append("Dimensiones: ").append(String.join(", ", metadata.variable().getDimensions())).append("\n");
        }
        if (metadata.variable() != null && metadata.variable().getScaleFactor() != null) {
            sb.append("scale_factor: ").append(metadata.variable().getScaleFactor()).append("\n");
        }
        if (metadata.variable() != null && metadata.variable().getAddOffset() != null) {
            sb.append("add_offset: ").append(metadata.variable().getAddOffset()).append("\n");
        }
        if (metadata.variable() != null && metadata.variable().getValidMin() != null) {
            sb.append("valid_min: ").append(metadata.variable().getValidMin()).append("\n");
        }
        if (metadata.variable() != null && metadata.variable().getValidMax() != null) {
            sb.append("valid_max: ").append(metadata.variable().getValidMax()).append("\n");
        }
        if (metadata.variable() != null && !metadata.variable().getQaDescriptor().isBlank()) {
            sb.append("QA descriptor: ").append(metadata.variable().getQaDescriptor()).append("\n");
        }
        String expectedQa = ProRasterDerivedService.describeExpectedQa(metadata);
        if (!expectedQa.isBlank()) {
            sb.append("QA prevista: ").append(expectedQa).append("\n");
        }
        if (metadata.variable() != null && !metadata.variable().getBandFamily().isBlank()) {
            sb.append("Familia tematica: ").append(metadata.variable().getBandFamily()).append("\n");
        }
        if (metadata.qualityPreset() != null && !metadata.qualityPreset().isBlank()) {
            sb.append("QA preset: ").append(metadata.qualityPreset()).append("\n");
        }
        if (!metadata.flagsApplied().isEmpty()) {
            sb.append("Flags QA: ").append(String.join(", ", metadata.flagsApplied())).append("\n");
        }
        if (detailed && metadata.recipe() != null && !metadata.recipe().isBlank()) {
            sb.append("Receta: ").append(metadata.recipe()).append("\n");
        }
        if (rasterLayer.isDerivedLayer() && ProRasterDerivedService.isQaOperation(rasterLayer.getDerivedOperation())) {
            java.util.Map<String, String> parameters = ProRasterDerivedService.parseParameterSpec(rasterLayer.getDerivedParameters());
            String qaLabel = parameters.getOrDefault("qaCriteriaLabel", "");
            if (!qaLabel.isBlank()) {
                sb.append("QA aplicada: ").append(qaLabel).append("\n");
            }
            String qaSummary = parameters.getOrDefault("qaCriteriaSummary", "");
            if (!qaSummary.isBlank()) {
                sb.append("Criterio QA: ").append(qaSummary).append("\n");
            }
            String qaMode = parameters.getOrDefault("qaValueMode", "");
            if (!qaMode.isBlank()) {
                sb.append("Modo QA: ").append(ProRasterDerivedService.qaValueModeLabel(qaMode)).append("\n");
            }
            String qaBits = parameters.getOrDefault("qaBits", "");
            String qaTargetLabel = parameters.getOrDefault("qaTargetLabel", "");
            if (!qaTargetLabel.isBlank()) {
                sb.append("Mascara QA: ").append(qaTargetLabel).append("\n");
            }
            String qaTargetBits = parameters.getOrDefault("qaTargetBits", "");
            if (!qaBits.isBlank() && (qaTargetBits.isBlank() || !qaBits.equalsIgnoreCase(qaTargetBits))) {
                sb.append("Bits QA: ").append(qaBits).append("\n");
            }
            if (!qaTargetBits.isBlank()) {
                sb.append("Bits objetivo: ").append(qaTargetBits).append("\n");
            }
            String qaRejectBits = parameters.getOrDefault("qaRejectBits", "");
            if (!qaRejectBits.isBlank()) {
                sb.append("Bits excluidos: ").append(qaRejectBits).append("\n");
            }
            String qaCompanionPath = parameters.getOrDefault("qaCompanionPath", "");
            if (!qaCompanionPath.isBlank()) {
                sb.append("Raster QA companero: ").append(qaCompanionPath).append("\n");
            }
        }
        if (rasterLayer.isDerivedLayer() && ProRasterDerivedService.OP_PRO_COMPARE_DELTA.equalsIgnoreCase(rasterLayer.getDerivedOperation())) {
            java.util.Map<String, String> parameters = ProRasterDerivedService.parseParameterSpec(rasterLayer.getDerivedParameters());
            String compareName = parameters.getOrDefault("compareName", "");
            String compareDate = parameters.getOrDefault("compareAcquisition", "");
            if (!compareName.isBlank()) {
                sb.append("Comparada contra: ").append(compareName).append("\n");
            }
            if (!compareDate.isBlank()) {
                sb.append("Fecha comparada: ").append(compareDate).append("\n");
            }
            String compareDataset = parameters.getOrDefault("compareDatasetRef", "");
            if (!compareDataset.isBlank()) {
                sb.append("Dataset comparado: ").append(compareDataset).append("\n");
            }
        }
        if (detailed && !effectiveMaturity.isBlank()) {
            sb.append("Alcance: ").append(ProOceanColorPresetSupport.methodologyDescription(effectiveMaturity)).append("\n");
        }
    }

    private boolean hasProRasterMetadata(RasterLayer rasterLayer) {
        return false;
    }

    private ProMetadataSidecarSupport.Metadata resolveProMetadata(RasterLayer rasterLayer) {
        if (rasterLayer == null) {
            return null;
        }
        if (rasterLayer.getProMetadataSidecarPath() != null && !rasterLayer.getProMetadataSidecarPath().isBlank()) {
            ProMetadataSidecarSupport.Metadata metadata = ProMetadataSidecarSupport.readSidecar(new File(rasterLayer.getProMetadataSidecarPath()));
            if (metadata != null) {
                return metadata;
            }
        }
        if (rasterLayer.getPath() != null && !rasterLayer.getPath().isBlank()) {
            return ProMetadataSidecarSupport.read(new File(rasterLayer.getPath()));
        }
        return null;
    }

    private String nonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private void openVectorDisplaySettings(Layer layer) {
        try {
            Class<?> dialogClass = Class.forName("ar.com.catgis.RasterDisplaySettingsDialog");
            Constructor<?> ctor = dialogClass.getConstructor(java.awt.Component.class, Layer.class);
            ctor.newInstance(this, layer);
            return;
        } catch (Exception ignored) { CatgisLogger.warn("LayersPanel: operation failed", ignored); }
        JOptionPane.showMessageDialog(
                this,
                "No se pudo abrir el dialogo de ajustes de visualizacion.",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void openRasterDisplaySettings(Layer layer) {
        if (!isRasterLayer(layer)) {
            JOptionPane.showMessageDialog(
                    this,
                    "La capa seleccionada no es un raster.",
                    "Ajustes de visualizacion",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            Class<?> dialogClass = Class.forName("ar.com.catgis.RasterDisplaySettingsDialog");
            Constructor<?> ctor = null;
            try {
                ctor = dialogClass.getConstructor(java.awt.Component.class, Layer.class);
                ctor.newInstance(this, layer);
                return;
            } catch (Exception ignored) { CatgisLogger.warn("LayersPanel: operation failed", ignored); }
            try {
                ctor = dialogClass.getConstructor(Layer.class);
                ctor.newInstance(layer);
                return;
            } catch (Exception ignored) { CatgisLogger.warn("LayersPanel: operation failed", ignored); }
            Method openMethod = null;
            try {
                openMethod = dialogClass.getMethod("open", java.awt.Component.class, Layer.class);
                openMethod.invoke(null, this, layer);
                return;
            } catch (Exception ignored) { CatgisLogger.warn("LayersPanel: operation failed", ignored); }
            try {
                openMethod = dialogClass.getMethod("open", Layer.class);
                openMethod.invoke(null, layer);
                return;
            } catch (Exception ignored) { CatgisLogger.warn("LayersPanel: operation failed", ignored); }
        } catch (Exception ignored) { CatgisLogger.warn("LayersPanel: operation failed", ignored); }

        JOptionPane.showMessageDialog(
                this,
                "Proximo paso sugerido para raster:\n"
                        + "- contraste automatico\n"
                        + "- min/max manual\n"
                        + "- bandas RGB\n"
                        + "- escala de grises\n"
                        + "- opacidad",
                "Ajustes de visualizacion",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String getRasterMode(Layer layer) {
        if (isDerivedRasterLayer(layer)) {
            return "derived";
        }
        if (layer instanceof RasterLayer) {
            return ((RasterLayer) layer).getRasterMode();
        }
        Object raster = getRasterDataFromMapPanel(layer);
        if (raster != null) {
            try {
                Method m = raster.getClass().getMethod("getRasterMode");
                Object value = m.invoke(raster);
                if (value instanceof String) {
                    return (String) value;
                }
            } catch (Exception ignored) { CatgisLogger.warn("LayersPanel: operation failed", ignored); }
        }
        return RasterImageLoader.MODE_PREVIEW;
    }

    private String getRasterModeLabel(String mode) {
        if ("derived".equalsIgnoreCase(mode)) {
            return "Derivado";
        }
        if (RasterImageLoader.MODE_REAL.equalsIgnoreCase(mode)) {
            return "Zoom real";
        }
        if (RasterImageLoader.MODE_VIRTUAL.equalsIgnoreCase(mode)) {
            return "Zoom virtual";
        }
        return "Vista rapida";
    }

    private void reloadRasterMode(Layer layer, String mode) {
        reloadRasterMode(layer, mode, false, false);
    }

    private void reloadRasterMode(Layer layer, String mode, boolean forceReload, boolean resetViewAfterLoad) {
        if (layer == null || !isRasterLayer(layer)) {
            return;
        }
        if (isDerivedRasterLayer(layer)) {
            JOptionPane.showMessageDialog(
                    this,
                    "La capa raster seleccionada es derivada del DEM y no cambia entre Vista rapida / Virtual / Real.\nVolvela a generar desde el analisis topohidrologico si queres recalcularla.",
                    "Raster derivado",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String currentMode = getRasterMode(layer);
        if (!forceReload && currentMode != null && currentMode.equalsIgnoreCase(mode)) {
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage(
                        "La capa ya esta en modo " + getRasterModeLabel(currentMode) + ": " + layer.getName()
                );
            }
            return;
        }

        String path = layer.getPath();
        if (path == null || path.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "La capa no tiene una ruta de archivo válida.",
                    "Raster",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        File rasterFile = new File(path);
        if (!rasterFile.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se encontró el archivo raster original:\n" + rasterFile.getAbsolutePath(),
                    "Raster",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        final JDialog progressDialog = createRasterProgressDialog(
                "Cargando raster en modo: " + getRasterModeLabel(mode) + "..."
        );

        SwingWorker<LocalRasterData, Void> worker = new SwingWorker<>() {
            @Override
            protected LocalRasterData doInBackground() throws Exception {
                String projectCRS = AppContext.project() != null ? AppContext.project().getProjectCRS() : "";
                String sourceCRS = layer.getSourceCRS();
                if (RasterImageLoader.MODE_REAL.equalsIgnoreCase(mode)) {
                    return RasterImageLoader.loadReal(rasterFile, projectCRS, sourceCRS);
                }
                if (RasterImageLoader.MODE_VIRTUAL.equalsIgnoreCase(mode)) {
                    return RasterImageLoader.loadVirtual(rasterFile, projectCRS, sourceCRS);
                }
                return RasterImageLoader.loadPreview(rasterFile, projectCRS, sourceCRS);
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    LocalRasterData data = get();

                    if (layer instanceof RasterLayer) {
                        ((RasterLayer) layer).setRasterMode(data.getRasterMode());
                    }

                    CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, data);
                    CatgisDesktopApp.markProjectDirty();
                    refreshLayerList();
                    if (resetViewAfterLoad) {
                        CatgisDesktopApp.mapPanel.resetView();
                    }
                    CatgisDesktopApp.mapPanel.repaint();

                    String msg = "Raster recargado en modo " + getRasterModeLabel(data.getRasterMode()) + ": " + layer.getName();

                    if (CatgisDesktopApp.statusBar != null) {
                        AppContext.setStatusMessage(msg);
                    }

                    JOptionPane.showMessageDialog(
                            LayersPanel.this,
                            msg,
                            "Raster",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            LayersPanel.this,
                            "No se pudo recargar el raster:\n" + ex.getMessage(),
                            "Raster",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    private boolean isDerivedRasterLayer(Layer layer) {
        return layer instanceof RasterLayer rasterLayer && rasterLayer.isDerivedLayer();
    }

    private void generateProDerivedRaster(RasterLayer sourceLayer, boolean qaMask) {
        generateProDerivedRaster(
                sourceLayer,
                qaMask
                        ? ProRasterDerivedService.defaultQaOperation(sourceLayer)
                        : null
        );
    }

    private void generateProDerivedRaster(RasterLayer sourceLayer, String explicitOperation) {
        if (sourceLayer == null) {
            return;
        }
        boolean thematic = explicitOperation == null || explicitOperation.isBlank();
        String actionLabel = thematic
                ? "Generando mapa tematico raster..."
                : ProRasterDerivedService.describeOperation(explicitOperation).replace("Mascara", "Generando mascara") + "...";
        final JDialog progressDialog = createRasterProgressDialog(actionLabel);
        SwingWorker<ProRasterDerivedService.GeneratedRasterLayer, Void> worker = new SwingWorker<>() {
            @Override
            protected ProRasterDerivedService.GeneratedRasterLayer doInBackground() throws Exception {
                if (thematic) {
                    return ProRasterDerivedService.generateThematicLayer(sourceLayer);
                }
                if (ProRasterDerivedService.OP_PRO_QA_BASIC_MASK.equalsIgnoreCase(explicitOperation)
                        || ProRasterDerivedService.OP_PRO_QA_NASA_OCEANCOLOR_L3M.equalsIgnoreCase(explicitOperation)
                        || ProRasterDerivedService.OP_PRO_QA_LANDSAT_L2SP.equalsIgnoreCase(explicitOperation)) {
                    return ProRasterDerivedService.generateQaLayer(sourceLayer);
                }
                return ProRasterDerivedService.generateLandsatQaPixelMask(sourceLayer, explicitOperation);
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    ProRasterDerivedService.GeneratedRasterLayer generated = get();
                    addGeneratedProRaster(generated);
                    RasterLayer layer = generated.layer();
                    String methodology = ProOceanColorPresetSupport.methodologyLabel(layer.getProMaturityLevel());
                    String message = (!thematic ? ProRasterDerivedService.describeOperation(generated.operation()) + ": " : "Mapa tematico raster generado: ")
                            + layer.getName()
                            + "\nClasificacion metodologica: " + methodology;
                    if (CatgisDesktopApp.statusBar != null) {
                        AppContext.setStatusMessage(message.replace('\n', ' '));
                    }
                    JOptionPane.showMessageDialog(
                            LayersPanel.this,
                            message,
                            "CATGIS",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            LayersPanel.this,
                            "No se pudo generar la salida raster:\n" + ex.getMessage(),
                            "CATGIS",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
        progressDialog.setVisible(true);
    }

    private void compareProRasterWithAnotherDate(RasterLayer sourceLayer) {
        if (sourceLayer == null) {
            return;
        }
        List<RasterLayer> candidates = findComparableProLayers(sourceLayer);
        if (candidates.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No hay otra capa Pro compatible en el proyecto para comparar esta variable.\n\n"
                            + "CATGIS exige misma variable, misma familia fuente y fechas distintas.",
                    "CATGIS",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        java.util.Map<String, RasterLayer> options = new java.util.LinkedHashMap<>();
        for (RasterLayer candidate : candidates) {
            String label = candidate.getName()
                    + " | "
                    + ProRasterDerivedService.formatAcquisitionLabel(nonBlank(candidate.getProAcquisitionStart(), "sin fecha"))
                    + " | "
                    + nonBlank(candidate.getProDatasetRef(), "sin dataset");
            options.put(label, candidate);
        }

        Object selected = JOptionPane.showInputDialog(
                this,
                "Selecciona la segunda fecha para comparar con " + sourceLayer.getName() + ":",
                "Comparacion temporal raster",
                JOptionPane.PLAIN_MESSAGE,
                AppIcons.attrRefreshIcon(),
                options.keySet().toArray(),
                options.keySet().iterator().next()
        );
        if (!(selected instanceof String selectedLabel) || !options.containsKey(selectedLabel)) {
            return;
        }

        RasterLayer comparisonLayer = options.get(selectedLabel);
        final JDialog progressDialog = createRasterProgressDialog("Generando comparacion temporal raster...");
        SwingWorker<ProRasterDerivedService.GeneratedRasterLayer, Void> worker = new SwingWorker<>() {
            @Override
            protected ProRasterDerivedService.GeneratedRasterLayer doInBackground() throws Exception {
                return ProRasterDerivedService.generateComparisonLayer(sourceLayer, comparisonLayer);
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    ProRasterDerivedService.GeneratedRasterLayer generated = get();
                    addGeneratedProRaster(generated);
                    RasterLayer layer = generated.layer();
                    String message = "Comparacion temporal raster generada: "
                            + layer.getName()
                            + "\nBase: " + ProRasterDerivedService.formatAcquisitionLabel(sourceLayer.getProAcquisitionStart())
                            + "\nContra: " + comparisonLayer.getName()
                            + " | " + ProRasterDerivedService.formatAcquisitionLabel(comparisonLayer.getProAcquisitionStart())
                            + "\nClasificacion metodologica: "
                            + ProOceanColorPresetSupport.methodologyLabel(layer.getProMaturityLevel());
                    if (CatgisDesktopApp.statusBar != null) {
                        AppContext.setStatusMessage(message.replace('\n', ' '));
                    }
                    JOptionPane.showMessageDialog(
                            LayersPanel.this,
                            message,
                            "CATGIS",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            LayersPanel.this,
                            "No se pudo generar la comparacion temporal raster:\n" + ex.getMessage(),
                            "CATGIS",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
        progressDialog.setVisible(true);
    }

    private List<RasterLayer> findComparableProLayers(RasterLayer sourceLayer) {
        List<RasterLayer> candidates = new ArrayList<>();
        if (sourceLayer == null || AppContext.project() == null) {
            return candidates;
        }
        String variable = nonBlank(sourceLayer.getProVariableName(), "");
        for (Layer layer : AppContext.project().getLayers()) {
            if (!(layer instanceof RasterLayer rasterLayer)) {
                continue;
            }
            if (rasterLayer == sourceLayer || rasterLayer.isDerivedLayer() || !hasProRasterMetadata(rasterLayer)) {
                continue;
            }
            if (!variable.isBlank() && !variable.equalsIgnoreCase(nonBlank(rasterLayer.getProVariableName(), ""))) {
                continue;
            }
            ProRasterDerivedService.ComparisonCompatibility compatibility = ProRasterDerivedService.evaluateComparisonCompatibility(sourceLayer, rasterLayer);
            if (!compatibility.compatible()) {
                continue;
            }
            candidates.add(rasterLayer);
        }
        candidates.sort(java.util.Comparator.comparing(raster -> nonBlank(raster.getProAcquisitionStart(), raster.getName()), String.CASE_INSENSITIVE_ORDER));
        return candidates;
    }

    private void addGeneratedProRaster(ProRasterDerivedService.GeneratedRasterLayer generated) {
        if (generated == null || generated.layer() == null || generated.data() == null) {
            return;
        }
        if (AppContext.project() == null) {
            AppContext.setCurrentProject(new Project("Proyecto CATGIS"));
        }
        AppContext.project().addLayer(generated.layer());
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.addLayer(generated.layer());
            AppContext.refreshLayerList();
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(generated.layer(), generated.data());
            CatgisDesktopApp.mapPanel.repaint();
        }
    }

    private JDialog createRasterProgressDialog(String message) {
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Procesando raster", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel label = new JLabel(message);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private Object getRasterDataFromMapPanel(Layer layer) {
        if (CatgisDesktopApp.mapPanel == null || layer == null) {
            return null;
        }
        String[] candidateMethods = new String[] {
                "getRasterData",
                "getRasterLayerData",
                "getLocalRasterData",
                "findRasterData"
        };
        for (String name : candidateMethods) {
            try {
                Method m = CatgisDesktopApp.mapPanel.getClass().getMethod(name, Layer.class);
                return m.invoke(CatgisDesktopApp.mapPanel, layer);
            } catch (Exception ignored) { CatgisLogger.warn("LayersPanel: operation failed", ignored); }
        }
        return null;
    }

    private void appendIfPresent(StringBuilder sb, Object target, String methodName, String label, String suffix) {
        appendIfPresent(sb, target, methodName, label, suffix, false);
    }

    private void appendIfPresent(StringBuilder sb, Object target, String methodName, String label, String suffix, boolean yesNo) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object value = m.invoke(target);
            if (value != null) {
                String text;
                if (yesNo && value instanceof Boolean) {
                    text = ((Boolean) value) ? "Si" : "No";
                } else {
                    text = String.valueOf(value);
                }
                sb.append(label).append(text).append(suffix).append("\n");
            }
        } catch (Exception ignored) { CatgisLogger.warn("LayersPanel: operation failed", ignored); }
    }

    private void appendEnvelopeIfPresent(StringBuilder sb, Object target) {
        try {
            Method m = target.getClass().getMethod("getEnvelope");
            Object envelope = m.invoke(target);
            if (envelope == null) {
                return;
            }
            double minX = invokeDouble(envelope, "getMinX");
            double minY = invokeDouble(envelope, "getMinY");
            double maxX = invokeDouble(envelope, "getMaxX");
            double maxY = invokeDouble(envelope, "getMaxY");
            sb.append("Extension: [")
                    .append(formatNumber(minX)).append(", ")
                    .append(formatNumber(minY)).append("] - [")
                    .append(formatNumber(maxX)).append(", ")
                    .append(formatNumber(maxY)).append("]\n");
        } catch (Exception ignored) { CatgisLogger.warn("LayersPanel: operation failed", ignored); }
    }

    private void showOnlineTileInfo(OnlineTileLayer layer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(layer.getName()).append("\n");
        sb.append("Tipo: Mapa base online\n");
        sb.append("Proveedor: ").append(layer.getProviderName() != null && !layer.getProviderName().isBlank() ? layer.getProviderName() : "-").append("\n");
        sb.append("Servicio: ").append(layer.getServiceType()).append("\n");
        sb.append("CRS origen: ").append(layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank() ? layer.getSourceCRS() : "-").append("\n");
        sb.append("Zoom soportado: ").append(layer.getMinZoom()).append(" - ").append(layer.getMaxZoom()).append("\n");
        sb.append("Plantilla URL: ").append(layer.getUrlTemplate() != null && !layer.getUrlTemplate().isBlank() ? layer.getUrlTemplate() : "-").append("\n");
        sb.append("Atribucion: ").append(layer.getAttribution() != null && !layer.getAttribution().isBlank() ? layer.getAttribution() : "-").append("\n");
        if (layer.getTermsUrl() != null && !layer.getTermsUrl().isBlank()) {
            sb.append("Referencia: ").append(layer.getTermsUrl()).append("\n");
        }
        showScrollableInfoDialog("Informacion mapa base online", sb.toString());
    }

    private void showOnlineWmsInfo(OnlineWmsLayer layer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(layer.getName()).append("\n");
        sb.append("Tipo: WMS remoto\n");
        sb.append("Proveedor: ").append(layer.getProviderName() != null && !layer.getProviderName().isBlank() ? layer.getProviderName() : "-").append("\n");
        sb.append("Servicio: ").append(layer.getServiceUrl() != null && !layer.getServiceUrl().isBlank() ? layer.getServiceUrl() : "-").append("\n");
        sb.append("Capas: ").append(layer.getLayerNames() != null && !layer.getLayerNames().isBlank() ? layer.getLayerNames() : "-").append("\n");
        sb.append("CRS pedido: ").append(layer.getRequestCrs() != null && !layer.getRequestCrs().isBlank() ? layer.getRequestCrs() : "-").append("\n");
        sb.append("Formato: ").append(layer.getImageFormat() != null && !layer.getImageFormat().isBlank() ? layer.getImageFormat() : "-").append("\n");
        sb.append("Version WMS: ").append(layer.getVersion() != null && !layer.getVersion().isBlank() ? layer.getVersion() : "-").append("\n");
        if (layer.getStyleNames() != null && !layer.getStyleNames().isBlank()) {
            sb.append("Estilos: ").append(layer.getStyleNames()).append("\n");
        }
        if (layer.getAttribution() != null && !layer.getAttribution().isBlank()) {
            sb.append("Atribucion: ").append(layer.getAttribution()).append("\n");
        }
        showScrollableInfoDialog("Informacion WMS", sb.toString());
    }

    private void showOnlineWfsInfo(OnlineWfsLayer layer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(layer.getName()).append("\n");
        sb.append("Tipo: WFS remoto\n");
        sb.append("Proveedor: ").append(layer.getProviderName() != null && !layer.getProviderName().isBlank() ? layer.getProviderName() : "-").append("\n");
        sb.append("Servicio: ").append(layer.getServiceUrl() != null && !layer.getServiceUrl().isBlank() ? layer.getServiceUrl() : "-").append("\n");
        sb.append("Feature type: ").append(layer.getTypeName() != null && !layer.getTypeName().isBlank() ? layer.getTypeName() : "-").append("\n");
        if (layer.getTypeTitle() != null && !layer.getTypeTitle().isBlank()) {
            sb.append("Titulo: ").append(layer.getTypeTitle()).append("\n");
        }
        sb.append("CRS pedido: ").append(layer.getRequestCrs() != null && !layer.getRequestCrs().isBlank() ? layer.getRequestCrs() : "-").append("\n");
        sb.append("Version WFS: ").append(layer.getVersion() != null && !layer.getVersion().isBlank() ? layer.getVersion() : "-").append("\n");
        sb.append("Modo: ").append(layer.isReadOnly() ? "Solo lectura" : "Editable").append("\n");
        sb.append("Elementos cargados: ").append(layer.getFeatureCount()).append("\n");
        showScrollableInfoDialog("Informacion WFS", sb.toString());
    }

    private void showPostgisInfo(PostgisLayer layer) {
        String geometryLabel = layer.getGeometryTypeLabel();
        if (geometryLabel == null || geometryLabel.isBlank()) {
            geometryLabel = layer.getType() != null ? layer.getType() : "-";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(layer.getName()).append("\n");
        sb.append("Tipo: CATSERVER / PostgreSQL-PostGIS\n");
        sb.append("Conexion: ").append(layer.getHost() != null && !layer.getHost().isBlank() ? layer.getHost() : "-").append(":").append(layer.getPort()).append("\n");
        sb.append("Base: ").append(layer.getDatabaseName() != null && !layer.getDatabaseName().isBlank() ? layer.getDatabaseName() : "-").append("\n");
        sb.append("Schema: ").append(layer.getSchemaName() != null && !layer.getSchemaName().isBlank() ? layer.getSchemaName() : "-").append("\n");
        sb.append("Usuario: ").append(layer.getUserName() != null && !layer.getUserName().isBlank() ? layer.getUserName() : "-").append("\n");
        sb.append("Tabla/feature type: ").append(layer.getTypeName() != null && !layer.getTypeName().isBlank() ? layer.getTypeName() : "-").append("\n");
        sb.append("Geometria: ").append(geometryLabel).append("\n");
        sb.append("CRS: ").append(layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank() ? layer.getSourceCRS() : "-").append("\n");
        sb.append("Modo: ").append(layer.isReadOnly() ? "Solo lectura" : "Editable").append("\n");
        sb.append("Elementos cargados: ").append(layer.getFeatureCount()).append("\n");
        showScrollableInfoDialog("Informacion de la conexion", sb.toString());
    }

    private void showGeoPackageInfo(GeoPackageLayer layer) {
        String geometryLabel = layer.getGeometryTypeLabel();
        if (geometryLabel == null || geometryLabel.isBlank()) {
            geometryLabel = layer.getType() != null ? layer.getType() : "-";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(layer.getName()).append("\n");
        sb.append("Tipo: GeoPackage\n");
        sb.append("Archivo: ").append(layer.getPath() != null && !layer.getPath().isBlank() ? layer.getPath() : "-").append("\n");
        sb.append("Tabla interna: ").append(layer.getTableName() != null && !layer.getTableName().isBlank() ? layer.getTableName() : "-").append("\n");
        if (layer.getIdentifier() != null && !layer.getIdentifier().isBlank()) {
            sb.append("Identificador: ").append(layer.getIdentifier()).append("\n");
        }
        if (layer.getDescription() != null && !layer.getDescription().isBlank()) {
            sb.append("Descripcion: ").append(layer.getDescription()).append("\n");
        }
        sb.append("Geometria: ").append(geometryLabel).append("\n");
        sb.append("CRS: ").append(layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank() ? layer.getSourceCRS() : "-").append("\n");
        sb.append("Modo: ").append(layer.isReadOnly() ? "Solo lectura" : "Editable").append("\n");
        sb.append("Elementos cargados: ").append(layer.getFeatureCount()).append("\n");
        showScrollableInfoDialog("Informacion GeoPackage", sb.toString());
    }

    private double invokeDouble(Object target, String methodName) throws Exception {
        Method m = target.getClass().getMethod(methodName);
        Object value = m.invoke(target);
        return value instanceof Number ? ((Number) value).doubleValue() : Double.NaN;
    }

    private String formatNumber(double value) {
        DecimalFormat df = new DecimalFormat("0.###");
        return df.format(value);
    }

    private String safeUpper(String s) {
        return s == null ? "" : s.toUpperCase();
    }

    private class LayerCellRenderer extends DefaultListCellRenderer {
        private final JPanel panel = new JPanel(new BorderLayout(8, 4));
        private final JCheckBox visibleCheck = new JCheckBox();
        private final JLabel iconLabel = new JLabel();
        private final JLabel nameLabel = new JLabel();
        private final JLabel metaLabel = new JLabel();
        private final JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 6));
        private final JPanel centerPanel = new JPanel(new BorderLayout(2, 2));
        private final Icon pointLayerIcon = createPointLayerIcon();
        private final Icon lineLayerIcon = createLineLayerIcon();
        private final Icon polygonLayerIcon = createPolygonLayerIcon();
        private final Icon rasterLayerIcon = createRasterLayerIcon();

        public LayerCellRenderer() {
            visibleCheck.setOpaque(false);
            visibleCheck.setFocusable(false);
            visibleCheck.setPreferredSize(new Dimension(18, 18));

            iconLabel.setPreferredSize(new Dimension(20, 20));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
            metaLabel.setFont(metaLabel.getFont().deriveFont(Font.PLAIN, 11f));

            leftPanel.setOpaque(false);
            centerPanel.setOpaque(false);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));

            leftPanel.add(visibleCheck);
            leftPanel.add(iconLabel);
            centerPanel.add(nameLabel, BorderLayout.NORTH);
            centerPanel.add(metaLabel, BorderLayout.SOUTH);
            panel.add(leftPanel, BorderLayout.WEST);
            panel.add(centerPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof LayerGroup group) {
                boolean groupVisible = group.isVisible();
                int memberCount = AppContext.project() != null
                        ? AppContext.project().getLayersForGroup(group.getName()).size()
                        : 0;

                Color bg = isSelected ? new Color(217, 231, 251) : new Color(236, 244, 253);
                Color fg = new Color(30, 38, 52);
                Color metaFg = new Color(95, 106, 122);
                Color accent = isSelected ? new Color(53, 105, 189) : new Color(138, 171, 214);

                panel.setBackground(bg);
                leftPanel.setBackground(bg);
                centerPanel.setBackground(bg);

                visibleCheck.setSelected(groupVisible);
                iconLabel.setIcon(AppIcons.openIcon());
                nameLabel.setText((group.isExpanded() ? "▾ " : "▸ ") + group.getName());
                nameLabel.setForeground(fg);
                metaLabel.setText("Grupo | " + memberCount + " capas" + (groupVisible ? "" : " | Oculto"));
                metaLabel.setForeground(metaFg);

                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(1, 6, 1, 1, accent),
                                BorderFactory.createLineBorder(isSelected ? new Color(113, 147, 201) : new Color(196, 210, 229))
                        ),
                        BorderFactory.createEmptyBorder(6, 8, 6, 6)
                ));
                return panel;
            }

            Layer layer = (Layer) value;
            boolean missingCrs = hasMissingCRS(layer);
            boolean editingLayer = CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.isLayerArmedForEditing(layer);
            boolean effectiveVisible = AppContext.project() == null
                    ? layer.isVisible()
                    : AppContext.project().isLayerEffectivelyVisible(layer);
            boolean layerInGroup = layer.isInGroup();

            Color bg = editingLayer
                    ? new Color(255, 239, 239)
                    : (isSelected ? new Color(220, 235, 255) : (layerInGroup ? new Color(248, 251, 255) : Color.WHITE));
            Color fg = editingLayer ? new Color(170, 24, 24) : new Color(30, 30, 30);
            Color metaFg = missingCrs
                    ? new Color(170, 70, 20)
                    : (editingLayer
                    ? new Color(185, 48, 48)
                    : (isSelected ? new Color(50, 70, 100) : new Color(110, 110, 110)));

            panel.setBackground(bg);
            leftPanel.setBackground(bg);
            centerPanel.setBackground(bg);

            visibleCheck.setSelected(layer.isVisible());
            iconLabel.setIcon(resolveLayerIcon(layer));
            nameLabel.setText((layerInGroup ? "  - " : "") + layer.getName());
            nameLabel.setForeground(effectiveVisible ? fg : new Color(120, 120, 120));
            String metaText = buildMetaText(layer);
            if (layerInGroup && layer.getGroupName() != null && !layer.getGroupName().isBlank()) {
                metaText = "Carpeta: " + layer.getGroupName() + " | " + metaText;
            }
            metaLabel.setText(metaText);
            metaLabel.setForeground(metaFg);

            if (layerInGroup) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(1, 14, 1, 1, isSelected ? new Color(78, 125, 198) : new Color(184, 206, 240)),
                                BorderFactory.createLineBorder(editingLayer
                                        ? new Color(220, 90, 90)
                                        : (isSelected ? new Color(120, 160, 220) : new Color(220, 228, 239)))
                        ),
                        BorderFactory.createEmptyBorder(5, 14, 5, 6)
                ));
            } else {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(editingLayer
                                ? new Color(220, 90, 90)
                                : (isSelected ? new Color(120, 160, 220) : new Color(230, 230, 230))),
                        BorderFactory.createEmptyBorder(5, 6, 5, 6)
                ));
            }

            return panel;
        }

        private String buildMetaText(Layer layer) {
            String crsInfo = formatCRSInfo(layer);
            if (layer instanceof OnlineTileLayer) {
                OnlineTileLayer online = (OnlineTileLayer) layer;
                String hidden = buildVisibilitySuffix(layer);
                return "Mapa base online | " + (online.getProviderName() != null && !online.getProviderName().isBlank() ? online.getProviderName() : layer.getName())
                        + " | " + crsInfo + hidden;
            }
            if (layer instanceof OnlineWmsLayer) {
                OnlineWmsLayer wms = (OnlineWmsLayer) layer;
                String hidden = buildVisibilitySuffix(layer);
                return "WMS | " + (wms.getProviderName() != null && !wms.getProviderName().isBlank() ? wms.getProviderName() : layer.getName())
                        + " | " + crsInfo + hidden;
            }
            if (layer instanceof OnlineWfsLayer) {
                OnlineWfsLayer wfs = (OnlineWfsLayer) layer;
                String hidden = buildVisibilitySuffix(layer);
                return "WFS | " + resolveGeometryTypeLabel(layer) + " | " + layer.getFeatureCount()
                        + " elementos | " + crsInfo + " | Solo lectura"
                        + (wfs.getProviderName() != null && !wfs.getProviderName().isBlank() ? " | " + wfs.getProviderName() : "")
                        + hidden;
            }
            if (layer instanceof GeoPackageLayer) {
                GeoPackageLayer geoPackage = (GeoPackageLayer) layer;
                String hidden = buildVisibilitySuffix(layer);
                return "GeoPackage | " + resolveGeometryTypeLabel(layer) + " | " + layer.getFeatureCount()
                        + " elementos | " + crsInfo + " | Solo lectura"
                        + (geoPackage.getTableName() != null && !geoPackage.getTableName().isBlank() ? " | " + geoPackage.getTableName() : "")
                        + hidden;
            }
            if (isRasterLayer(layer)) {
                String hidden = buildVisibilitySuffix(layer);
                if (layer instanceof RasterLayer rasterLayer && hasProRasterMetadata(rasterLayer)) {
                    String variable = rasterLayer.getProVariableName() != null && !rasterLayer.getProVariableName().isBlank()
                            ? rasterLayer.getProVariableName()
                            : "variable";
                    String time = rasterLayer.getProAcquisitionStart() != null && !rasterLayer.getProAcquisitionStart().isBlank()
                            ? " | " + rasterLayer.getProAcquisitionStart()
                            : "";
                    return "Raster Pro | " + variable + time + " | " + crsInfo + hidden;
                }
                return "Raster | " + crsInfo + hidden;
            }
            if (CadLayerSupport.isCadLayer(layer)) {
                String hidden = buildVisibilitySuffix(layer);
                return resolveGeometryTypeLabel(layer) + " CAD | " + layer.getFeatureCount() + " elementos | "
                        + crsInfo + " | " + CadGeoreferenceSupport.buildDetailedSummary(layer)
                        + " | " + CadPlacementSupport.buildPlacementSummary(layer)
                        + " | " + CadLayerSupport.buildCadInternalLayerFilterLabel(layer) + hidden;
            }

            String type = resolveGeometryTypeLabel(layer);
            int count = layer.getFeatureCount();
            String labelInfo = layer.isLabelsVisible()
                    ? " | Etiquetas: " + (layer.getLabelField() != null ? layer.getLabelField() : "Si")
                    : "";
            String hidden = buildVisibilitySuffix(layer);
            String editing = (CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.isLayerArmedForEditing(layer))
                    ? " | En edicion"
                    : "";
            return type + " | " + count + " elementos | " + crsInfo + editing + labelInfo + hidden;
        }

        private String buildVisibilitySuffix(Layer layer) {
            if (layer == null) {
                return "";
            }
            if (!layer.isVisible()) {
                return " | Oculta";
            }
            if (AppContext.project() != null && !AppContext.project().isLayerEffectivelyVisible(layer)) {
                return " | Oculta por grupo";
            }
            return "";
        }

        private String formatCRSInfo(Layer layer) {
            String crs = layer.getSourceCRS();
            if (crs == null || crs.isBlank()) {
                return "Sin CRS definido";
            }
            return crs;
        }

        private boolean hasMissingCRS(Layer layer) {
            return layer == null || layer.getSourceCRS() == null || layer.getSourceCRS().isBlank();
        }

        private javax.swing.Icon resolveLayerIcon(Layer layer) {
            if (isRasterLayer(layer)) {
                return rasterLayerIcon;
            }
            String type = resolveGeometryTypeLabel(layer);
            if ("PUNTO".equals(type)) {
                return pointLayerIcon;
            }
            if ("LINEA".equals(type)) {
                return lineLayerIcon;
            }
            if ("POLIGONO".equals(type)) {
                return polygonLayerIcon;
            }
            return AppIcons.genericLayerIcon();
        }

        private String resolveGeometryTypeLabel(Layer layer) {
            if (layer == null) {
                return "-";
            }
            if (isRasterLayer(layer)) {
                return "Raster";
            }

            String geometryType = resolveGeometryTypeFromData(layer);
            if (geometryType != null) {
                return geometryType;
            }

            if (layer instanceof GeoPackageLayer) {
                String geometryLabel = ((GeoPackageLayer) layer).getGeometryTypeLabel();
                if (geometryLabel != null && !geometryLabel.isBlank()) {
                    String upper = geometryLabel.toUpperCase();
                    if (upper.contains("POINT") || upper.contains("PUNTO")) {
                        return "PUNTO";
                    }
                    if (upper.contains("LINE")) {
                        return "LINEA";
                    }
                    if (upper.contains("POLYGON") || upper.contains("POLIG")) {
                        return "POLIGONO";
                    }
                }
            }

            if (layer instanceof PostgisLayer) {
                String geometryLabel = ((PostgisLayer) layer).getGeometryTypeLabel();
                if (geometryLabel != null && !geometryLabel.isBlank()) {
                    String upper = geometryLabel.toUpperCase();
                    if (upper.contains("POINT") || upper.contains("PUNTO")) {
                        return "PUNTO";
                    }
                    if (upper.contains("LINE")) {
                        return "LINEA";
                    }
                    if (upper.contains("POLYGON") || upper.contains("POLIG")) {
                        return "POLIGONO";
                    }
                }
            }

            String rawType = layer.getType() != null ? layer.getType().toUpperCase() : "";
            if (rawType.contains("POINT") || rawType.contains("PUNTO")) {
                return "PUNTO";
            }
            if (rawType.contains("LINE")) {
                return "LINEA";
            }
            if (rawType.contains("POLYGON") || rawType.contains("POLIG")) {
                return "POLIGONO";
            }
            return layer.getType() != null ? layer.getType() : "-";
        }

        private String resolveGeometryTypeFromData(Layer layer) {
            if (layer == null || CatgisDesktopApp.mapPanel == null) {
                return null;
            }

            ShapefileData data = CatgisDesktopApp.mapPanel.getShapefileData(layer);
            if (data == null) {
                return null;
            }

            SimpleFeatureCollection featureCollection = data.getFeatureCollection();
            if (featureCollection != null) {
                SimpleFeatureType schema = featureCollection.getSchema();
                if (schema != null && schema.getGeometryDescriptor() != null) {
                    String byBinding = resolveGeometryTypeFromBinding(
                            schema.getGeometryDescriptor().getType().getBinding()
                    );
                    if (byBinding != null) {
                        return byBinding;
                    }
                }
            }

            List<SimpleFeature> features = data.getFeatures();
            if (features != null) {
                for (SimpleFeature feature : features) {
                    if (feature == null) {
                        continue;
                    }
                    Object geometry = feature.getDefaultGeometry();
                    if (geometry instanceof Geometry) {
                        String byGeometry = resolveGeometryTypeFromGeometry((Geometry) geometry);
                        if (byGeometry != null) {
                            return byGeometry;
                        }
                    }
                }
            }
            return null;
        }

        private String resolveGeometryTypeFromBinding(Class<?> binding) {
            if (binding == null) {
                return null;
            }
            if (Point.class.isAssignableFrom(binding) || MultiPoint.class.isAssignableFrom(binding)) {
                return "PUNTO";
            }
            if (LineString.class.isAssignableFrom(binding) || MultiLineString.class.isAssignableFrom(binding)) {
                return "LINEA";
            }
            if (Polygon.class.isAssignableFrom(binding) || MultiPolygon.class.isAssignableFrom(binding)) {
                return "POLIGONO";
            }
            return null;
        }

        private String resolveGeometryTypeFromGeometry(Geometry geometry) {
            if (geometry == null) {
                return null;
            }
            if (geometry instanceof Point || geometry instanceof MultiPoint) {
                return "PUNTO";
            }
            if (geometry instanceof LineString || geometry instanceof MultiLineString) {
                return "LINEA";
            }
            if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
                return "POLIGONO";
            }
            return null;
        }

        private Icon createPointLayerIcon() {
            BufferedImage img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(37, 99, 235));
            g.fillOval(6, 6, 6, 6);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1.4f));
            g.drawOval(6, 6, 6, 6);
            g.dispose();
            return new ImageIcon(img);
        }

        private Icon createLineLayerIcon() {
            BufferedImage img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(22, 163, 74));
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(3, 12, 7, 8);
            g.drawLine(7, 8, 11, 10);
            g.drawLine(11, 10, 15, 5);
            g.dispose();
            return new ImageIcon(img);
        }

        private Icon createPolygonLayerIcon() {
            BufferedImage img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(251, 191, 36, 170));
            g.fillRoundRect(4, 4, 10, 10, 2, 2);
            g.setColor(new Color(180, 83, 9));
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(4, 4, 10, 10, 2, 2);
            g.dispose();
            return new ImageIcon(img);
        }

        private Icon createRasterLayerIcon() {
            BufferedImage img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(226, 232, 240));
            g.fillRoundRect(2, 3, 14, 12, 2, 2);
            g.setColor(new Color(100, 116, 139));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(2, 3, 14, 12, 2, 2);
            g.setColor(new Color(59, 130, 246));
            g.drawLine(4, 11, 7, 8);
            g.drawLine(7, 8, 10, 10);
            g.drawLine(10, 10, 13, 6);
            g.dispose();
            return new ImageIcon(img);
        }
    }
}