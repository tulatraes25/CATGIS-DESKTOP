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

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

public class LayersPanel extends JPanel {

    private final DefaultListModel<Layer> model;
    private final JList<Layer> layerList;
    private int dragSourceIndex = -1;
    private int dragTargetInsertIndex = -1;
    private boolean dragReorderActive = false;

    public LayersPanel() {
        setLayout(new BorderLayout());

        model = new DefaultListModel<>();
        layerList = new JList<>(model);
        layerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        layerList.setCellRenderer(new LayerCellRenderer());

        JScrollPane scrollPane = new JScrollPane(layerList);
        add(scrollPane, BorderLayout.CENTER);

        MouseAdapter layerMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = layerList.locationToIndex(e.getPoint());
                if (index < 0) {
                    return;
                }

                Rectangle bounds = layerList.getCellBounds(index, index);
                if (bounds == null) {
                    return;
                }

                layerList.setSelectedIndex(index);
                Layer selectedLayer = model.get(index);

                int relativeX = e.getX() - bounds.x;
                if (e.getButton() == MouseEvent.BUTTON1 && relativeX >= 8 && relativeX <= 28) {
                    dragSourceIndex = -1;
                    dragTargetInsertIndex = -1;
                    dragReorderActive = false;
                    selectedLayer.setVisible(!selectedLayer.isVisible());
                    refreshLayerList();
                    CatgisDesktopApp.mapPanel.repaint();

                    if (CatgisDesktopApp.statusBar != null) {
                        CatgisDesktopApp.statusBar.setMessage(
                                "Capa " + (selectedLayer.isVisible() ? "visible" : "oculta") + ": " + selectedLayer.getName()
                        );
                    }
                    return;
                }

                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    CatgisDesktopApp.mapPanel.zoomToLayer(selectedLayer);
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {
                    showLayerPopup(e, selectedLayer);
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
                        layerList.setSelectedIndex(index);
                        showLayerPopup(e, model.get(index));
                    }
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON1) {
                    int index = layerList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Rectangle bounds = layerList.getCellBounds(index, index);
                        int relativeX = bounds != null ? e.getX() - bounds.x : -1;
                        if (!(relativeX >= 8 && relativeX <= 28)) {
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
                        reorderLayerByDrag(dragSourceIndex, dropIndex);
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
                        layerList.setSelectedIndex(index);
                        showLayerPopup(e, model.get(index));
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
    }

    public void addLayer(Layer layer) {
        model.addElement(layer);
        refreshLayerList();
    }

    public void removeLayer(Layer layer) {
        model.removeElement(layer);
        refreshLayerList();
    }

    public void clearLayers() {
        model.clear();
        refreshLayerList();
    }

    public Layer getSelectedLayer() {
        return layerList.getSelectedValue();
    }

    public void refreshLayerList() {
        layerList.repaint();
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

    private JPopupMenu buildVectorPopup(Layer selectedLayer) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem propertiesItem = createMenuItem(selectedLayer.getName(), AppIcons.propertiesIcon());
        propertiesItem.setEnabled(false);
        popupMenu.add(propertiesItem);
        popupMenu.addSeparator();

        addCommonTopItems(popupMenu, selectedLayer, "Ocultar capa", "Mostrar capa");

        JMenuItem editItem = createMenuItem("Editar vector", AppIcons.attrEditIcon());
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

        JMenuItem exportItem = createMenuItem("Exportar capa", AppIcons.exportIcon());
        exportItem.addActionListener(ev -> ExportVectorLayerAction.exportLayer(selectedLayer));
        advancedMenu.add(exportItem);

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
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem propertiesItem = createMenuItem(selectedLayer.getName(), AppIcons.propertiesIcon());
        propertiesItem.setEnabled(false);
        popupMenu.add(propertiesItem);
        popupMenu.addSeparator();

        addCommonTopItems(popupMenu, selectedLayer, "Ocultar raster", "Mostrar raster");

        JMenuItem rasterInfoItem = createMenuItem("Informacion raster...", AppIcons.propertiesIcon());
        rasterInfoItem.addActionListener(ev -> showRasterInfo(selectedLayer));
        popupMenu.add(rasterInfoItem);

        JMenuItem displayItem = createMenuItem("Ajustes de visualizacion...", AppIcons.propertiesIcon());
        displayItem.addActionListener(ev -> openRasterDisplaySettings(selectedLayer));
        popupMenu.add(displayItem);

        String currentMode = getRasterMode(selectedLayer);
        JMenuItem currentModeItem = createMenuItem("Modo actual: " + getRasterModeLabel(currentMode), AppIcons.propertiesIcon());
        currentModeItem.setEnabled(false);
        popupMenu.add(currentModeItem);

        JMenuItem quickItem = createMenuItem("Vista rapida", AppIcons.propertiesIcon());
        quickItem.setEnabled(!RasterImageLoader.MODE_PREVIEW.equalsIgnoreCase(currentMode));
        quickItem.addActionListener(ev -> reloadRasterMode(selectedLayer, RasterImageLoader.MODE_PREVIEW));
        popupMenu.add(quickItem);

        JMenuItem virtualItem = createMenuItem("Zoom virtual", AppIcons.propertiesIcon());
        virtualItem.setEnabled(!RasterImageLoader.MODE_VIRTUAL.equalsIgnoreCase(currentMode));
        virtualItem.addActionListener(ev -> reloadRasterMode(selectedLayer, RasterImageLoader.MODE_VIRTUAL));
        popupMenu.add(virtualItem);

        JMenuItem realItem = createMenuItem("Zoom real", AppIcons.propertiesIcon());
        realItem.setEnabled(!RasterImageLoader.MODE_REAL.equalsIgnoreCase(currentMode));
        realItem.addActionListener(ev -> reloadRasterMode(selectedLayer, RasterImageLoader.MODE_REAL));
        popupMenu.add(realItem);

        JMenu advancedMenu = new JMenu("Configuracion avanzada");
        advancedMenu.setIcon(AppIcons.crsIcon());

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
        JMenuItem moveUpItem = createMenuItem("Subir", AppIcons.upIcon());
        moveUpItem.addActionListener(ev -> moveLayerUp(selectedLayer));
        popupMenu.add(moveUpItem);

        JMenuItem moveDownItem = createMenuItem("Bajar", AppIcons.downIcon());
        moveDownItem.addActionListener(ev -> moveLayerDown(selectedLayer));
        popupMenu.add(moveDownItem);

        JMenuItem removeItem = createMenuItem("Quitar", AppIcons.removeIcon());
        removeItem.addActionListener(ev -> removeSelectedLayer(selectedLayer));
        popupMenu.add(removeItem);
    }

    private JMenuItem createMenuItem(String text, javax.swing.Icon icon) {
        return new JMenuItem(text, icon);
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

        String text = "CRS de la capa: " + source + "\nCRS del proyecto: " + project;
        JOptionPane.showMessageDialog(this, text, "CRS de capa", JOptionPane.INFORMATION_MESSAGE);
    }

    private void defineLayerCRS(Layer layer) {
        CRSSelectorDialog.open("Definir CRS de capa", layer.getSourceCRS(), code -> {
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
                        "CRS de capa actualizado: " + layer.getName() + " -> " + CRSDefinitions.getLabelForCode(code)
                );
            }
        });
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
        int index = model.indexOf(layer);
        if (index > 0) {
            model.remove(index);
            model.add(index - 1, layer);
            layerList.setSelectedIndex(index - 1);
            applyLayerOrderFromModel();
        }
    }

    private void moveLayerDown(Layer layer) {
        int index = model.indexOf(layer);
        if (index >= 0 && index < model.size() - 1) {
            model.remove(index);
            model.add(index + 1, layer);
            layerList.setSelectedIndex(index + 1);
            applyLayerOrderFromModel();
        }
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

    private void reorderLayerByDrag(int sourceIndex, int insertIndex) {
        if (sourceIndex < 0 || sourceIndex >= model.size()) {
            return;
        }

        if (insertIndex == sourceIndex || insertIndex == sourceIndex + 1) {
            return;
        }

        Layer layer = model.get(sourceIndex);
        if (layer == null) {
            return;
        }

        model.remove(sourceIndex);
        if (insertIndex > sourceIndex) {
            insertIndex--;
        }
        insertIndex = Math.max(0, Math.min(insertIndex, model.size()));
        model.add(insertIndex, layer);
        layerList.setSelectedIndex(insertIndex);
        applyLayerOrderFromModel();
    }

    private void applyLayerOrderFromModel() {
        List<Layer> orderedLayers = new java.util.ArrayList<>();
        for (int i = 0; i < model.size(); i++) {
            orderedLayers.add(model.getElementAt(i));
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

        model.removeElement(layer);

        if (CatgisDesktopApp.currentProject != null) {
            CatgisDesktopApp.currentProject.getLayers().remove(layer);
        }

        CatgisDesktopApp.mapPanel.removeLayer(layer);
        CatgisDesktopApp.markProjectDirty();
        refreshLayerList();
    }

    private void showRasterInfo(Layer layer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(layer.getName()).append("\n");
        sb.append("Tipo: Raster\n");
        sb.append("Ruta: ").append(layer.getPath() != null ? layer.getPath() : "-").append("\n");
        sb.append("CRS: ").append(layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank()
                ? CRSDefinitions.getLabelForCode(layer.getSourceCRS()) : "Desconocido").append("\n");
        sb.append("Modo: ").append(getRasterModeLabel(getRasterMode(layer))).append("\n");

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
            Layer layer = (Layer) value;
            boolean missingCrs = hasMissingCRS(layer);
            boolean editingLayer = CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.isLayerArmedForEditing(layer);

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
            nameLabel.setText(layer.getName());
            nameLabel.setForeground(fg);
            metaLabel.setText(buildMetaText(layer));
            metaLabel.setForeground(metaFg);

            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(editingLayer
                            ? new Color(220, 90, 90)
                            : (isSelected ? new Color(120, 160, 220) : new Color(230, 230, 230))),
                    BorderFactory.createEmptyBorder(5, 6, 5, 6)
            ));

            return panel;
        }

        private String buildMetaText(Layer layer) {
            String crsInfo = formatCRSInfo(layer);
            if (isRasterLayer(layer)) {
                String hidden = layer.isVisible() ? "" : " | Oculta";
                return "Raster | " + crsInfo + hidden;
            }

            String type = resolveGeometryTypeLabel(layer);
            int count = layer.getFeatureCount();
            String labelInfo = layer.isLabelsVisible()
                    ? " | Etiquetas: " + (layer.getLabelField() != null ? layer.getLabelField() : "Si")
                    : "";
            String hidden = layer.isVisible() ? "" : " | Oculta";
            String editing = (CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.isLayerArmedForEditing(layer))
                    ? " | En edicion"
                    : "";
            return type + " | " + count + " elementos | " + crsInfo + editing + labelInfo + hidden;
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
