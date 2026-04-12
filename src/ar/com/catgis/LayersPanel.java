package ar.com.catgis;

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
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
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

public class LayersPanel extends JPanel {

    private final DefaultListModel<Object> model;
    private final JList<Object> layerList;
    private final JButton newGroupButton;
    private int dragSourceIndex = -1;
    private int dragTargetInsertIndex = -1;
    private boolean dragReorderActive = false;

    public LayersPanel() {
        setLayout(new BorderLayout());

        model = new DefaultListModel<>();
        layerList = new JList<>(model);
        layerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        layerList.setCellRenderer(new LayerCellRenderer());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        topBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
        newGroupButton = new JButton("Nuevo grupo", AppIcons.openIcon());
        newGroupButton.addActionListener(e -> createNewGroupFromSelection());
        topBar.add(newGroupButton);
        add(topBar, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(layerList);
        add(scrollPane, BorderLayout.CENTER);

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

                if (e.getButton() == MouseEvent.BUTTON1 && isVisibilityToggleHit(e, bounds)) {
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
                            CatgisDesktopApp.statusBar.setMessage(
                                    "Grupo " + (selectedGroup.isVisible() ? "visible" : "oculto") + ": " + selectedGroup.getName()
                            );
                        } else {
                            CatgisDesktopApp.statusBar.setMessage(
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
                        if (bounds != null && !isVisibilityToggleHit(e, bounds) && !selectionModifier && asLayer(model.get(index)) != null) {
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
        Project project = CatgisDesktopApp.currentProject;
        if (project == null) {
            layerList.repaint();
            return;
        }

        for (Layer layer : project.getUngroupedLayers()) {
            if (layer != null) {
                model.addElement(layer);
            }
        }

        for (LayerGroup group : project.getLayerGroups()) {
            if (group == null) {
                continue;
            }
            model.addElement(group);
            if (group.isExpanded()) {
                for (Layer layer : project.getLayersForGroup(group.getName())) {
                    if (layer != null) {
                        model.addElement(layer);
                    }
                }
            }
        }

        restoreSelection(previousSelection);
        layerList.repaint();
    }

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
    }

    private boolean isVisibilityToggleHit(MouseEvent e, Rectangle bounds) {
        int relativeX = e.getX() - bounds.x;
        return relativeX >= 8 && relativeX <= 28;
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

        JMenuItem editItem = createMenuItem(readOnlyVector ? "Capa en solo lectura" : "Editar vector", AppIcons.attrEditIcon());
        editItem.setEnabled(!readOnlyVector);
        editItem.addActionListener(ev -> openVectorEditing(selectedLayer));
        popupMenu.add(editItem);

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

            JMenuItem cadInternalLayersItem = createMenuItem("Capas internas CAD...", AppIcons.tableIcon());
            cadInternalLayersItem.addActionListener(ev -> CadWorkflowSupport.openCadInternalLayers(this, selectedLayer));
            advancedMenu.add(cadInternalLayersItem);

            JMenuItem cadDiagItem = createMenuItem("Diagnostico DWG/CAD...", AppIcons.propertiesIcon());
            cadDiagItem.addActionListener(ev -> CadIntegrationDialog.open());
            advancedMenu.add(cadDiagItem);
        }

        JMenuItem exportItem = createMenuItem("Exportar capa", AppIcons.exportIcon());
        exportItem.addActionListener(ev -> ExportVectorLayerAction.exportLayer(selectedLayer));
        advancedMenu.add(exportItem);

        if (selectedLayer instanceof OnlineWfsLayer) {
            JMenuItem infoItem = createMenuItem("Informacion del servicio...", AppIcons.propertiesIcon());
            infoItem.addActionListener(ev -> showOnlineWfsInfo((OnlineWfsLayer) selectedLayer));
            advancedMenu.add(infoItem);
        } else if (selectedLayer instanceof PostgisLayer) {
            JMenuItem infoItem = createMenuItem("Informacion PostGIS...", AppIcons.propertiesIcon());
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

        JMenuItem displayItem = createMenuItem("Ajustes de visualizacion...", AppIcons.attrEditIcon());
        displayItem.addActionListener(ev -> openRasterDisplaySettings(selectedLayer));
        popupMenu.add(displayItem);

        boolean derivedRaster = isDerivedRasterLayer(selectedLayer);
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
        if (CatgisDesktopApp.currentProject == null) {
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
        LayerGroup group = CatgisDesktopApp.currentProject.addLayerGroup(trimmed);
        if (layers != null) {
            for (Layer layer : layers) {
                CatgisDesktopApp.currentProject.assignLayerToGroup(layer, group.getName());
            }
        }
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
    }

    private void populateMoveToGroupMenu(JMenu menu, List<Layer> layers) {
        if (CatgisDesktopApp.currentProject == null) {
            menu.setEnabled(false);
            return;
        }
        if (CatgisDesktopApp.currentProject.getLayerGroups().isEmpty()) {
            JMenuItem empty = createMenuItem("No hay grupos todavia", AppIcons.openIcon());
            empty.setEnabled(false);
            menu.add(empty);
            return;
        }
        for (LayerGroup group : CatgisDesktopApp.currentProject.getLayerGroups()) {
            JMenuItem item = createMenuItem(group.getName(), AppIcons.openIcon());
            item.addActionListener(ev -> moveLayersToGroup(layers, group.getName()));
            menu.add(item);
        }
    }

    private void moveLayersToGroup(List<Layer> layers, String groupName) {
        if (CatgisDesktopApp.currentProject == null || layers == null || layers.isEmpty()) {
            return;
        }
        for (Layer layer : layers) {
            CatgisDesktopApp.currentProject.assignLayerToGroup(layer, groupName);
        }
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
        CatgisDesktopApp.mapPanel.repaint();
    }

    private void removeLayersFromGroup(List<Layer> layers) {
        if (CatgisDesktopApp.currentProject == null || layers == null || layers.isEmpty()) {
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
        if (group == null || CatgisDesktopApp.currentProject == null) {
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
        CatgisDesktopApp.currentProject.renameLayerGroup(group.getName(), newName.trim());
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
    }

    private void removeGroup(LayerGroup group) {
        if (group == null || CatgisDesktopApp.currentProject == null) {
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
        CatgisDesktopApp.currentProject.removeLayerGroup(group.getName(), true);
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
    }

    private void moveGroup(LayerGroup group, int delta) {
        if (group == null || CatgisDesktopApp.currentProject == null) {
            return;
        }
        List<LayerGroup> groups = new ArrayList<>(CatgisDesktopApp.currentProject.getLayerGroups());
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
        CatgisDesktopApp.currentProject.setLayerGroupOrder(orderedNames);
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

        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(
                    "Edicion vectorial preparada para la capa: " + layer.getName()
            );
        }
    }

    private void showLayerCRS(Layer layer) {
        String source = (layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank())
                ? CRSDefinitions.getLabelForCode(layer.getSourceCRS())
                : "Desconocido";

        String project = (CatgisDesktopApp.currentProject != null)
                ? CRSDefinitions.getLabelForCode(CatgisDesktopApp.currentProject.getProjectCRS())
                : "-";

        StringBuilder text = new StringBuilder();
        text.append("CRS de la capa: ").append(source).append("\n");
        text.append("CRS del proyecto: ").append(project);
        if (CadLayerSupport.isCadLayer(layer)) {
            text.append("\nGeorreferenciacion CAD: ").append(CadGeoreferenceSupport.buildDetailedSummary(layer));
            text.append("\nAjuste CAD: ").append(CadPlacementSupport.buildPlacementSummary(layer));
            text.append("\nCapas internas CAD: ").append(CadLayerSupport.buildCadInternalLayerFilterLabel(layer));
        }
        JOptionPane.showMessageDialog(this, text.toString(), "CRS de capa", JOptionPane.INFORMATION_MESSAGE);
    }

    private void defineLayerCRS(Layer layer) {
        if (CadLayerSupport.isCadLayer(layer)) {
            CadCrsAssignmentDialog.Result result = CadCrsAssignmentDialog.chooseForLayer(this, layer);
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
            CatgisDesktopApp.statusBar.setMessage("Ajuste CAD actualizado: " + layer.getName() + " -> " + CadPlacementSupport.buildPlacementSummary(layer));
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
            CatgisDesktopApp.statusBar.setMessage(
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
        if (layer == null || CatgisDesktopApp.currentProject == null) {
            return;
        }
        List<Layer> ordered = new ArrayList<>(CatgisDesktopApp.currentProject.getLayers());
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
        if (layer == null || target == null || CatgisDesktopApp.currentProject == null || layer == target) {
            return;
        }
        List<Layer> ordered = new ArrayList<>(CatgisDesktopApp.currentProject.getLayers());
        if (!ordered.remove(layer)) {
            return;
        }
        int targetIndex = ordered.indexOf(target);
        if (targetIndex < 0) {
            ordered.add(layer);
        } else {
            ordered.add(afterTarget ? targetIndex + 1 : targetIndex, layer);
        }
        CatgisDesktopApp.currentProject.setLayerOrder(ordered);
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
        if (sourceLayer == null || targetGroup == null || CatgisDesktopApp.currentProject == null) {
            return;
        }

        sourceLayer.setGroupName(targetGroup.getName());
        targetGroup.setExpanded(true);

        List<Layer> groupLayers = CatgisDesktopApp.currentProject.getLayersForGroup(targetGroup.getName());
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
        if (sourceLayer == null || CatgisDesktopApp.currentProject == null) {
            return;
        }
        List<Layer> ordered = new ArrayList<>(CatgisDesktopApp.currentProject.getLayers());
        if (!ordered.remove(sourceLayer)) {
            return;
        }
        ordered.add(0, sourceLayer);
        CatgisDesktopApp.currentProject.setLayerOrder(ordered);
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

        if (CatgisDesktopApp.currentProject != null) {
            CatgisDesktopApp.currentProject.setLayerOrder(orderedLayers);
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
            if (CatgisDesktopApp.currentProject != null) {
                CatgisDesktopApp.currentProject.removeLayer(layer);
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
                CatgisDesktopApp.statusBar.setMessage(I18n.format("Capa quitada: {0}", orderedLayers.get(0).getName()));
            } else {
                CatgisDesktopApp.statusBar.setMessage(I18n.format("{0} capas quitadas del proyecto.", orderedLayers.size()));
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

        JOptionPane.showMessageDialog(this, sb.toString(), "Informacion raster", JOptionPane.INFORMATION_MESSAGE);
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
            } catch (NoSuchMethodException ignored) {
            }
            try {
                ctor = dialogClass.getConstructor(Layer.class);
                ctor.newInstance(layer);
                return;
            } catch (NoSuchMethodException ignored) {
            }
            Method openMethod = null;
            try {
                openMethod = dialogClass.getMethod("open", java.awt.Component.class, Layer.class);
                openMethod.invoke(null, this, layer);
                return;
            } catch (NoSuchMethodException ignored) {
            }
            try {
                openMethod = dialogClass.getMethod("open", Layer.class);
                openMethod.invoke(null, layer);
                return;
            } catch (NoSuchMethodException ignored) {
            }
        } catch (Exception ignored) {
        }

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
            } catch (Exception ignored) {
            }
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
                CatgisDesktopApp.statusBar.setMessage(
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
                String projectCRS = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
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
                        CatgisDesktopApp.statusBar.setMessage(msg);
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
            } catch (Exception ignored) {
            }
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
        } catch (Exception ignored) {
        }
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
        } catch (Exception ignored) {
        }
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
        JOptionPane.showMessageDialog(this, sb.toString(), "Informacion mapa base online", JOptionPane.INFORMATION_MESSAGE);
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
        JOptionPane.showMessageDialog(this, sb.toString(), "Informacion WMS", JOptionPane.INFORMATION_MESSAGE);
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
        JOptionPane.showMessageDialog(this, sb.toString(), "Informacion WFS", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showPostgisInfo(PostgisLayer layer) {
        String geometryLabel = layer.getGeometryTypeLabel();
        if (geometryLabel == null || geometryLabel.isBlank()) {
            geometryLabel = layer.getType() != null ? layer.getType() : "-";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(layer.getName()).append("\n");
        sb.append("Tipo: PostGIS\n");
        sb.append("Conexion: ").append(layer.getHost() != null && !layer.getHost().isBlank() ? layer.getHost() : "-").append(":").append(layer.getPort()).append("\n");
        sb.append("Base: ").append(layer.getDatabaseName() != null && !layer.getDatabaseName().isBlank() ? layer.getDatabaseName() : "-").append("\n");
        sb.append("Schema: ").append(layer.getSchemaName() != null && !layer.getSchemaName().isBlank() ? layer.getSchemaName() : "-").append("\n");
        sb.append("Usuario: ").append(layer.getUserName() != null && !layer.getUserName().isBlank() ? layer.getUserName() : "-").append("\n");
        sb.append("Tabla/feature type: ").append(layer.getTypeName() != null && !layer.getTypeName().isBlank() ? layer.getTypeName() : "-").append("\n");
        sb.append("Geometria: ").append(geometryLabel).append("\n");
        sb.append("CRS: ").append(layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank() ? layer.getSourceCRS() : "-").append("\n");
        sb.append("Modo: ").append(layer.isReadOnly() ? "Solo lectura" : "Editable").append("\n");
        sb.append("Elementos cargados: ").append(layer.getFeatureCount()).append("\n");
        JOptionPane.showMessageDialog(this, sb.toString(), "Informacion PostGIS", JOptionPane.INFORMATION_MESSAGE);
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
        JOptionPane.showMessageDialog(this, sb.toString(), "Informacion GeoPackage", JOptionPane.INFORMATION_MESSAGE);
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
                int memberCount = CatgisDesktopApp.currentProject != null
                        ? CatgisDesktopApp.currentProject.getLayersForGroup(group.getName()).size()
                        : 0;

                Color bg = isSelected ? new Color(225, 235, 248) : new Color(246, 248, 252);
                Color fg = new Color(30, 38, 52);
                Color metaFg = new Color(95, 106, 122);

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
                        BorderFactory.createLineBorder(isSelected ? new Color(120, 160, 220) : new Color(214, 220, 228)),
                        BorderFactory.createEmptyBorder(5, 6, 5, 6)
                ));
                return panel;
            }

            Layer layer = (Layer) value;
            boolean missingCrs = hasMissingCRS(layer);
            boolean editingLayer = CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.isLayerArmedForEditing(layer);
            boolean effectiveVisible = CatgisDesktopApp.currentProject == null
                    ? layer.isVisible()
                    : CatgisDesktopApp.currentProject.isLayerEffectivelyVisible(layer);

            Color bg = editingLayer
                    ? new Color(255, 239, 239)
                    : (isSelected ? new Color(220, 235, 255) : Color.WHITE);
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
            nameLabel.setText((layer.isInGroup() ? "   " : "") + layer.getName());
            nameLabel.setForeground(effectiveVisible ? fg : new Color(120, 120, 120));
            metaLabel.setText(buildMetaText(layer));
            metaLabel.setForeground(metaFg);

            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(editingLayer
                            ? new Color(220, 90, 90)
                            : (isSelected ? new Color(120, 160, 220) : new Color(230, 230, 230))),
                    BorderFactory.createEmptyBorder(5, layer.isInGroup() ? 18 : 6, 5, 6)
            ));

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
            if (CatgisDesktopApp.currentProject != null && !CatgisDesktopApp.currentProject.isLayerEffectivelyVisible(layer)) {
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
