package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.awt.image.BufferedImage;

public class MapPanel extends JPanel {

    private final Map<Layer, ShapefileData> shapefileLayers = new LinkedHashMap<>();
    private final Map<Layer, LocalRasterData> rasterLayers = new LinkedHashMap<>();
    private final Map<Layer, RasterStyle> rasterStyles = new LinkedHashMap<>();
    private final Map<Layer, CachedRasterDisplay> rasterDisplayCache = new LinkedHashMap<>();
    private final GeometryFactory selectionGeometryFactory = new GeometryFactory();

    private final List<PinMarker> pins = new ArrayList<>();
    private int nextPinId = 1;
    private PinMarker activePin = null;

    private final List<Coordinate> drawingCoordinates = new ArrayList<>();
    private String drawingMode = null;

    private final List<Coordinate> measurementCoordinates = new ArrayList<>();
    private String measurementMode = null;

    private double hoverWorldX = Double.NaN;
    private double hoverWorldY = Double.NaN;

    private double viewMinX = 0;
    private double viewMinY = 0;
    private double zoomFactor = 1.0;

    private final List<ViewState> viewHistory = new ArrayList<>();
    private int viewHistoryIndex = -1;
    private boolean navigatingViewHistory = false;
    private double dragStartViewMinX = 0;
    private double dragStartViewMinY = 0;

    private boolean dragging = false;
    private boolean draggingPin = false;
    private int lastMouseX;
    private int lastMouseY;

    private String currentTool = "MOVE";

    private Layer selectedLayer;
    private SimpleFeature selectedFeature;
    private final Map<Layer, List<String>> tableSelectionIds = new LinkedHashMap<>();
    private Layer activeVectorEditingLayer = null;
    private SimpleFeature copiedFeature = null;
    private final List<SimpleFeature> copiedFeatures = new ArrayList<>();
    private boolean featureEditMode = false;
    private Geometry featureEditOriginalGeometry = null;
    private boolean featureEditDirty = false;
    private String featureEditOperation = EDIT_OP_MOVE_VERTEX;
    private final List<Coordinate> featureEditSketchCoordinates = new ArrayList<>();
    private final Deque<LayerEditSnapshot> editUndoStack = new ArrayDeque<>();
    private final Deque<LayerEditSnapshot> editRedoStack = new ArrayDeque<>();
    private int activeEditVertexIndex = -1;
    private static final int EDIT_VERTEX_TOLERANCE_PX = 10;
    private static final int MAX_EDIT_HISTORY = 20;
    private static final String EDIT_OP_MOVE_VERTEX = "MOVE_VERTEX";
    private static final String EDIT_OP_ADD_VERTEX = "ADD_VERTEX";
    private static final String EDIT_OP_REMOVE_VERTEX = "REMOVE_VERTEX";
    private static final String EDIT_OP_MOVE_FEATURE = "MOVE_FEATURE";
    private static final String EDIT_OP_CUT = "CUT_FEATURE";
    private static final String EDIT_OP_HOLE = "DIG_HOLE";
    private static final double EDIT_SEGMENT_TOLERANCE_PX = 22.0;
    private static final int SELECTION_BOX_DRAG_THRESHOLD_PX = 6;
    private static final int SELECTION_FLASH_DURATION_MS = 420;

    private String openedFileText = "Sin archivo cargado";
    private boolean selectionBoxActive = false;
    private boolean selectionBoxDragging = false;
    private boolean suppressNextSelectClick = false;
    private int selectionBoxStartX = -1;
    private int selectionBoxStartY = -1;
    private int selectionBoxEndX = -1;
    private int selectionBoxEndY = -1;
    private boolean movingSelectedFeatures = false;
    private double moveSelectionLastProjectX = Double.NaN;
    private double moveSelectionLastProjectY = Double.NaN;
    private Geometry selectionFlashGeometry = null;
    private long selectionFlashStartedAt = 0L;
    private final Timer selectionFlashTimer;

    public MapPanel() {
        setBackground(Color.WHITE);
        configureKeyboardShortcuts();
        selectionFlashTimer = new Timer(33, e -> {
            if (selectionFlashGeometry == null
                    || (System.currentTimeMillis() - selectionFlashStartedAt) > SELECTION_FLASH_DURATION_MS) {
                ((Timer) e.getSource()).stop();
                selectionFlashGeometry = null;
            }
            repaint();
        });
        selectionFlashTimer.setRepeats(true);

        MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (isDrawingActive()) {
                        if (!drawingCoordinates.isEmpty()) {
                            finishCurrentDrawing();
                        }
                        return;
                    }
                    if (isMeasurementActive()) {
                        if (!measurementCoordinates.isEmpty()) {
                            finishCurrentMeasurement();
                        }
                        return;
                    }
                }

                if (featureEditMode && EDIT_OP_MOVE_VERTEX.equals(featureEditOperation) && SwingUtilities.isLeftMouseButton(e)) {
                    int vertexIndex = findEditableVertexIndex(e.getX(), e.getY());
                    if (vertexIndex >= 0) {
                        pushUndoSnapshotForSelectedLayer();
                        activeEditVertexIndex = vertexIndex;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }

                if (SwingUtilities.isLeftMouseButton(e)
                        && EDIT_OP_MOVE_FEATURE.equals(featureEditOperation)
                        && hasFeatureSelection()
                        && isHitOnCurrentSelection(e.getX(), e.getY())) {
                    pushUndoSnapshotForSelectedLayer();
                    movingSelectedFeatures = true;
                    moveSelectionLastProjectX = screenToWorldX(e.getX());
                    moveSelectionLastProjectY = screenToWorldY(e.getY());
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }

                if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    showMapPopup(e);
                    return;
                }

                if (isDrawingActive() || isMeasurementActive()) {
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)
                        && "SELECT".equalsIgnoreCase(currentTool)
                        && (!featureEditMode
                        || EDIT_OP_MOVE_VERTEX.equals(featureEditOperation)
                        || EDIT_OP_ADD_VERTEX.equals(featureEditOperation)
                        || EDIT_OP_REMOVE_VERTEX.equals(featureEditOperation))) {
                    selectionBoxActive = true;
                    selectionBoxDragging = false;
                    selectionBoxStartX = e.getX();
                    selectionBoxStartY = e.getY();
                    selectionBoxEndX = e.getX();
                    selectionBoxEndY = e.getY();
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                    PinMarker pin = findPinAtScreen(e.getX(), e.getY());
                    if (pin != null) {
                        activePin = pin;
                        draggingPin = true;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }

                if ("MOVE".equalsIgnoreCase(currentTool)) {
                    dragging = true;
                    dragStartViewMinX = viewMinX;
                    dragStartViewMinY = viewMinY;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    if (!isDrawingActive() && !isMeasurementActive()) {
                        showMapPopup(e);
                    }
                    return;
                }

                if (draggingPin) {
                    draggingPin = false;
                    if (activePin != null) {
                        showCopiedMessage("Pin P" + activePin.getId() + " movido.");
                    }
                }

                if (activeEditVertexIndex >= 0) {
                    activeEditVertexIndex = -1;
                    CatgisDesktopApp.markProjectDirty();
                    showCopiedMessage("Vertice actualizado.");
                    refreshEditingUi();
                }

                if (movingSelectedFeatures) {
                    movingSelectedFeatures = false;
                    moveSelectionLastProjectX = Double.NaN;
                    moveSelectionLastProjectY = Double.NaN;
                    CatgisDesktopApp.markProjectDirty();
                    showCopiedMessage(getSelectedFeatureCount() <= 1
                            ? "Entidad movida."
                            : getSelectedFeatureCount() + " entidades movidas.");
                    refreshEditingUi();
                }

                if (selectionBoxActive && "SELECT".equalsIgnoreCase(currentTool)) {
                    boolean shouldSelectByBox = selectionBoxDragging;
                    Rectangle boxBounds = getSelectionBoxBounds();
                    selectionBoxActive = false;
                    selectionBoxDragging = false;
                    if (shouldSelectByBox) {
                        if (featureEditMode && EDIT_OP_REMOVE_VERTEX.equals(featureEditOperation)) {
                            removeVerticesFromSelectedGeometry(boxBounds);
                        } else if (featureEditMode && EDIT_OP_ADD_VERTEX.equals(featureEditOperation)) {
                            addVertexToSelectedGeometry(boxBounds.x + (boxBounds.width / 2), boxBounds.y + (boxBounds.height / 2));
                        } else {
                            selectFeatureForEditing(boxBounds, e.isControlDown());
                        }
                        suppressNextSelectClick = true;
                    }
                    repaint();
                }

                if (dragging && ("MOVE".equalsIgnoreCase(currentTool))) {
                    rememberViewState(dragStartViewMinX, dragStartViewMinY, zoomFactor);
                    rememberCurrentView();
                }

                dragging = false;

                if (isDrawingActive() || isMeasurementActive()) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else if ("MOVE".equalsIgnoreCase(currentTool)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else if ("IDENTIFY".equalsIgnoreCase(currentTool)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else if ("SELECT".equalsIgnoreCase(currentTool)) {
                    setCursor(featureEditMode ? resolveFeatureEditCursor() : Cursor.getDefaultCursor());
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateStatusCoordinates(e.getX(), e.getY());

                hoverWorldX = screenToWorldX(e.getX());
                hoverWorldY = screenToWorldY(e.getY());

                if (draggingPin && activePin != null) {
                    activePin.setX(screenToWorldX(e.getX()));
                    activePin.setY(screenToWorldY(e.getY()));
                    repaint();
                    return;
                }

                if (activeEditVertexIndex >= 0 && featureEditMode && EDIT_OP_MOVE_VERTEX.equals(featureEditOperation)) {
                    moveSelectedVertex(screenToWorldX(e.getX()), screenToWorldY(e.getY()), activeEditVertexIndex);
                    repaint();
                    return;
                }

                if (movingSelectedFeatures && EDIT_OP_MOVE_FEATURE.equals(featureEditOperation)) {
                    moveSelectedFeatures(screenToWorldX(e.getX()), screenToWorldY(e.getY()));
                    repaint();
                    return;
                }

                if (selectionBoxActive && "SELECT".equalsIgnoreCase(currentTool)) {
                    selectionBoxEndX = e.getX();
                    selectionBoxEndY = e.getY();
                    selectionBoxDragging = Math.abs(selectionBoxEndX - selectionBoxStartX) >= SELECTION_BOX_DRAG_THRESHOLD_PX
                            || Math.abs(selectionBoxEndY - selectionBoxStartY) >= SELECTION_BOX_DRAG_THRESHOLD_PX;
                    repaint();
                    return;
                }

                if (!dragging || !"MOVE".equalsIgnoreCase(currentTool) || isDrawingActive() || isMeasurementActive()) {
                    repaint();
                    return;
                }

                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;

                viewMinX -= dx / zoomFactor;
                viewMinY += dy / zoomFactor;

                lastMouseX = e.getX();
                lastMouseY = e.getY();

                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateStatusCoordinates(e.getX(), e.getY());

                hoverWorldX = screenToWorldX(e.getX());
                hoverWorldY = screenToWorldY(e.getY());

                if (isDrawingActive() || isMeasurementActive()) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    repaint();
                    return;
                }

                PinMarker pin = findPinAtScreen(e.getX(), e.getY());
                if (pin != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else if (featureEditMode && EDIT_OP_MOVE_VERTEX.equals(featureEditOperation) && findEditableVertexIndex(e.getX(), e.getY()) >= 0) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else if (EDIT_OP_MOVE_FEATURE.equals(featureEditOperation) && hasFeatureSelection() && isHitOnCurrentSelection(e.getX(), e.getY())) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else if (featureEditMode && (EDIT_OP_MOVE_VERTEX.equals(featureEditOperation)
                        || EDIT_OP_ADD_VERTEX.equals(featureEditOperation)
                        || EDIT_OP_REMOVE_VERTEX.equals(featureEditOperation)
                        || EDIT_OP_CUT.equals(featureEditOperation))) {
                    setCursor(resolveFeatureEditCursor());
                } else if (EDIT_OP_MOVE_FEATURE.equals(featureEditOperation) && hasFeatureSelection()) {
                    setCursor(resolveFeatureEditCursor());
                } else if (featureEditMode && isFeatureEditSketchMode()) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else if ("MOVE".equalsIgnoreCase(currentTool)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else if ("IDENTIFY".equalsIgnoreCase(currentTool)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverWorldX = Double.NaN;
                hoverWorldY = Double.NaN;
                if (CatgisDesktopApp.statusBar != null) {
                    CatgisDesktopApp.statusBar.clearCoordinates();
                }
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (suppressNextSelectClick) {
                    suppressNextSelectClick = false;
                    return;
                }

                if (SwingUtilities.isRightMouseButton(e) || draggingPin) {
                    return;
                }

                if (featureEditMode && SwingUtilities.isLeftMouseButton(e) && handleFeatureEditClick(e)) {
                    return;
                }

                if (isDrawingActive() && SwingUtilities.isLeftMouseButton(e)) {
                    Coordinate c = new Coordinate(screenToWorldX(e.getX()), screenToWorldY(e.getY()));

                    if ("POINT".equalsIgnoreCase(drawingMode) || "MULTIPOINT".equalsIgnoreCase(drawingMode)) {
                        drawingCoordinates.add(c);
                        repaint();
                        return;
                    }

                    if (e.getClickCount() >= 2) {
                        if (!drawingCoordinates.isEmpty()) {
                            finishCurrentDrawing();
                        }
                        return;
                    }

                    drawingCoordinates.add(c);
                    repaint();
                    return;
                }

                if (isMeasurementActive() && SwingUtilities.isLeftMouseButton(e)) {
                    Coordinate c = new Coordinate(screenToWorldX(e.getX()), screenToWorldY(e.getY()));

                    if (e.getClickCount() >= 2) {
                        if (!measurementCoordinates.isEmpty()) {
                            finishCurrentMeasurement();
                        }
                        return;
                    }

                    measurementCoordinates.add(c);
                    repaint();
                    return;
                }

                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    showCoordinateDialog(e.getX(), e.getY());
                    return;
                }

                if ("IDENTIFY".equalsIgnoreCase(currentTool)) {
                    identifyFeature(e.getX(), e.getY());
                } else if ("SELECT".equalsIgnoreCase(currentTool)) {
                    selectFeatureForEditing(e.getX(), e.getY(), e.isControlDown());
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                handleZoom(e);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    private void configureKeyboardShortcuts() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelSketchOrMeasurement");
        getActionMap().put("cancelSketchOrMeasurement", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (featureEditMode && !featureEditSketchCoordinates.isEmpty()) {
                    featureEditSketchCoordinates.clear();
                    repaint();
                    showCopiedMessage("Boceto de edición cancelado.");
                } else if (isDrawingActive()) {
                    cancelCurrentDrawing();
                    showCopiedMessage("Dibujo cancelado.");
                } else if (isMeasurementActive()) {
                    cancelCurrentMeasurement();
                    showCopiedMessage("Medición cancelada.");
                }
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK), "zoomPreviousView");
        getActionMap().put("zoomPreviousView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomPrevious();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK), "zoomNextView");
        getActionMap().put("zoomNextView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomNext();
            }
        });
    }

    private void rememberCurrentView() {
        rememberViewState(viewMinX, viewMinY, zoomFactor);
    }

    private void rememberViewState(double minX, double minY, double zoom) {
        if (navigatingViewHistory || zoom <= 0) {
            return;
        }

        ViewState state = new ViewState(minX, minY, zoom);

        if (viewHistoryIndex >= 0 && viewHistoryIndex < viewHistory.size()) {
            ViewState current = viewHistory.get(viewHistoryIndex);
            if (current.isSameAs(state)) {
                return;
            }
        }

        while (viewHistory.size() > viewHistoryIndex + 1) {
            viewHistory.remove(viewHistory.size() - 1);
        }

        viewHistory.add(state);
        viewHistoryIndex = viewHistory.size() - 1;
    }

    public boolean canZoomPrevious() {
        return viewHistoryIndex > 0;
    }

    public boolean canZoomNext() {
        return viewHistoryIndex >= 0 && viewHistoryIndex < viewHistory.size() - 1;
    }

    public void zoomPrevious() {
        if (!canZoomPrevious()) {
            return;
        }

        navigatingViewHistory = true;
        try {
            viewHistoryIndex--;
            ViewState state = viewHistory.get(viewHistoryIndex);
            restoreView(state.viewMinX, state.viewMinY, state.zoomFactor);
        } finally {
            navigatingViewHistory = false;
        }

        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Vista anterior restaurada.");
        }
    }

    public void zoomNext() {
        if (!canZoomNext()) {
            return;
        }

        navigatingViewHistory = true;
        try {
            viewHistoryIndex++;
            ViewState state = viewHistory.get(viewHistoryIndex);
            restoreView(state.viewMinX, state.viewMinY, state.zoomFactor);
        } finally {
            navigatingViewHistory = false;
        }

        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Vista siguiente restaurada.");
        }
    }

    private void zoomToSelectedLayer() {
        Layer layer = (CatgisDesktopApp.layersPanel != null) ? CatgisDesktopApp.layersPanel.getSelectedLayer() : null;
        if (layer == null) {
            JOptionPane.showMessageDialog(this, "Primero seleccioná una capa en el panel de capas.");
            return;
        }
        zoomToLayer(layer);
    }

    public void zoomToSelectedLayerPublic() {
        zoomToSelectedLayer();
    }

    public void rememberCurrentViewPublic() {
        rememberCurrentView();
    }

    public void setTool(String tool) {
        this.currentTool = tool;

        if (isDrawingActive() || isMeasurementActive()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if ("MOVE".equalsIgnoreCase(tool)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else if ("IDENTIFY".equalsIgnoreCase(tool)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if ("SELECT".equalsIgnoreCase(tool)) {
            setCursor(resolveFeatureEditCursor());
        } else {
            setCursor(Cursor.getDefaultCursor());
        }

        CatgisDesktopApp.syncFloatingVectorEditToolbar();
    }

    private void refreshEditingUi() {
        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.refreshLayerList();
        }
        repaint();
    }

    private Cursor resolveFeatureEditCursor() {
        if (!featureEditMode && !(EDIT_OP_MOVE_FEATURE.equals(featureEditOperation) && hasFeatureSelection())) {
            return Cursor.getDefaultCursor();
        }

        if (EDIT_OP_MOVE_FEATURE.equals(featureEditOperation)) {
            return createSelectionCursor();
        }
        if (EDIT_OP_ADD_VERTEX.equals(featureEditOperation)) {
            return createToolCursor("+", new Color(15, 118, 110), new Color(224, 242, 241), new Color(255, 255, 255));
        }
        if (EDIT_OP_REMOVE_VERTEX.equals(featureEditOperation)) {
            return createToolCursor("-", new Color(185, 28, 28), new Color(254, 226, 226), new Color(255, 255, 255));
        }
        if (EDIT_OP_CUT.equals(featureEditOperation)) {
            return createScissorCursor();
        }
        if (EDIT_OP_HOLE.equals(featureEditOperation)) {
            return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        }
        return createSelectionCursor();
    }

    private Rectangle getSelectionBoxBounds() {
        int x = Math.min(selectionBoxStartX, selectionBoxEndX);
        int y = Math.min(selectionBoxStartY, selectionBoxEndY);
        int width = Math.abs(selectionBoxEndX - selectionBoxStartX);
        int height = Math.abs(selectionBoxEndY - selectionBoxStartY);
        return new Rectangle(x, y, width, height);
    }

    private Cursor createBadgeCursor(String symbol, Color ink, Color badgeFill) {
        return createToolCursor(symbol, ink, badgeFill, Color.WHITE);
    }

    private Cursor createToolCursor(String symbol, Color ink, Color badgeFill, Color halo) {
        try {
            int size = 32;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawCursorPointer(g2);

            drawCursorBadge(g2, halo, badgeFill, ink);

            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tx = 15 + ((12 - fm.stringWidth(symbol)) / 2);
            int ty = 15 + (((12 - fm.getHeight()) / 2) + fm.getAscent()) - 1;
            g2.drawString(symbol, tx, ty);
            g2.dispose();
            return Toolkit.getDefaultToolkit().createCustomCursor(image, new java.awt.Point(2, 1), "catgis-" + symbol);
        } catch (Exception ex) {
            return Cursor.getDefaultCursor();
        }
    }

    private Cursor createScissorCursor() {
        try {
            int size = 32;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawCursorPointer(g2);

            drawCursorBadge(g2, new Color(219, 234, 254), new Color(219, 234, 254), new Color(35, 76, 155));
            g2.setColor(new Color(35, 76, 155));
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(16, 16, 4, 4);
            g2.drawOval(21, 20, 4, 4);
            g2.drawLine(18, 18, 25, 14);
            g2.drawLine(23, 22, 26, 25);
            g2.drawLine(20, 18, 22, 21);
            g2.drawLine(23, 17, 20, 22);
            g2.dispose();

            return Toolkit.getDefaultToolkit().createCustomCursor(image, new java.awt.Point(2, 1), "catgis-cut");
        } catch (Exception ex) {
            return Cursor.getDefaultCursor();
        }
    }

    private Cursor createSelectionCursor() {
        try {
            int size = 32;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawCursorPointer(g2);
            drawCursorBadge(g2, new Color(219, 234, 254), new Color(219, 234, 254), new Color(30, 64, 175));
            drawSelectionBadge(g2, new Color(30, 64, 175));
            g2.dispose();
            return Toolkit.getDefaultToolkit().createCustomCursor(image, new java.awt.Point(2, 1), "catgis-select-edit");
        } catch (Exception ex) {
            return Cursor.getDefaultCursor();
        }
    }

    private void drawCursorBadge(Graphics2D g2, Color halo, Color badgeFill, Color stroke) {
        g2.setColor(halo);
        g2.fillOval(13, 13, 16, 16);
        g2.setColor(badgeFill);
        g2.fillOval(14, 14, 14, 14);
        g2.setColor(stroke);
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawOval(14, 14, 14, 14);
    }

    private void drawSelectionBadge(Graphics2D g2, Color ink) {
        Object geomObj = selectedFeature != null ? selectedFeature.getDefaultGeometry() : null;
        g2.setColor(ink);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (geomObj instanceof Point || geomObj instanceof MultiPoint) {
            g2.fillOval(18, 18, 4, 4);
            return;
        }
        if (geomObj instanceof Polygon || geomObj instanceof MultiPolygon) {
            g2.drawRect(17, 17, 6, 6);
            return;
        }
        g2.drawLine(17, 23, 24, 17);
    }

    private void drawCursorPointer(Graphics2D g2) {
        Path2D.Double outline = new Path2D.Double();
        outline.moveTo(2, 2);
        outline.lineTo(2, 22);
        outline.lineTo(7, 17);
        outline.lineTo(10, 26);
        outline.lineTo(13, 24);
        outline.lineTo(10, 16);
        outline.lineTo(16, 16);
        outline.closePath();

        g2.setColor(new Color(255, 255, 255, 245));
        g2.fill(outline);
        g2.setColor(new Color(30, 30, 34));
        g2.setStroke(new BasicStroke(1.1f));
        g2.draw(outline);
        g2.drawLine(6, 18, 9, 24);
    }

    public String getCurrentTool() {
        return currentTool;
    }

    public Layer getSelectedLayerRef() {
        return selectedLayer;
    }

    public SimpleFeature getSelectedFeatureRef() {
        return selectedFeature;
    }

    public Layer getEditingLayerRef() {
        return activeVectorEditingLayer;
    }

    public boolean isLayerArmedForEditing(Layer layer) {
        return layer != null && layer == activeVectorEditingLayer;
    }

    public boolean isFeatureEditMode() {
        return featureEditMode;
    }

    public boolean hasFeatureEditChanges() {
        return featureEditMode && featureEditDirty;
    }

    public String getFeatureEditOperation() {
        return featureEditOperation;
    }

    public boolean canUndoFeatureEdit() {
        return !editUndoStack.isEmpty() && getEditingLayerRef() != null;
    }

    public boolean canRedoFeatureEdit() {
        return !editRedoStack.isEmpty() && getEditingLayerRef() != null;
    }

    public boolean hasCopiedFeature() {
        return copiedFeature != null || !copiedFeatures.isEmpty();
    }

    public boolean hasFeatureSelection() {
        return getSelectedFeatureCount() > 0;
    }

    public int getSelectedFeatureCount() {
        return getSelectedFeatureIdsForLayer(selectedLayer).size();
    }

    public List<String> getSelectedFeatureIds() {
        return getSelectedFeatureIdsForLayer(selectedLayer);
    }

    private List<String> getSelectedFeatureIdsForLayer(Layer layer) {
        if (layer == null) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> ids = new LinkedHashSet<>();
        List<String> storedIds = tableSelectionIds.get(layer);
        if (storedIds != null) {
            for (String id : storedIds) {
                if (id != null && !id.isBlank()) {
                    ids.add(id);
                }
            }
        }
        if (layer == selectedLayer && selectedFeature != null && selectedFeature.getID() != null) {
            ids.add(selectedFeature.getID());
        }
        return new ArrayList<>(ids);
    }

    private void restoreFeatureEditOriginalGeometry() {
        if (selectedFeature != null && featureEditOriginalGeometry != null) {
            selectedFeature.setDefaultGeometry(featureEditOriginalGeometry.copy());
            repaint();
        }
    }

    private boolean confirmPendingFeatureEdit(String nextActionDescription) {
        if (!hasFeatureEditChanges() || selectedFeature == null) {
            return true;
        }

        String layerName = selectedLayer != null ? selectedLayer.getName() : "la capa actual";
        Object[] options = {"Guardar", "Descartar", "Cancelar"};
        String message = "Hay cambios sin guardar en la entidad en edición de " + layerName + ".\n"
                + "¿Querés guardarlos antes de " + nextActionDescription + "?";
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Cambios en edición",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            return saveFeatureEditChanges();
        }
        if (choice == 1) {
            restoreFeatureEditOriginalGeometry();
            featureEditDirty = false;
            featureEditSketchCoordinates.clear();
            activeEditVertexIndex = -1;
            editUndoStack.clear();
            editRedoStack.clear();
            refreshEditingUi();
            return true;
        }
        return false;
    }

    private List<String> normalizeSelectionIds(Layer layer, List<String> featureIds) {
        List<String> normalized = new ArrayList<>();
        if (layer == null || featureIds == null || featureIds.isEmpty()) {
            return normalized;
        }

        ShapefileData data = getShapefileData(layer);
        if (data == null || data.getFeatures() == null) {
            return normalized;
        }

        LinkedHashSet<String> orderedIds = new LinkedHashSet<>(featureIds);
        for (String featureId : orderedIds) {
            if (findFeatureById(data.getFeatures(), featureId) != null) {
                normalized.add(featureId);
            }
        }
        return normalized;
    }

    private boolean applyFeatureSelection(Layer layer,
                                          List<String> featureIds,
                                          boolean activateEditForSingle,
                                          boolean syncOpenTables,
                                          boolean promptOnDirty,
                                          String statusMessage) {
        if (layer == null) {
            return false;
        }

        List<String> normalizedIds = normalizeSelectionIds(layer, featureIds);
        if (normalizedIds.isEmpty()) {
            if (promptOnDirty && !confirmPendingFeatureEdit("cambiar la selección")) {
                return false;
            }
            clearSelectedFeatureInternal(syncOpenTables);
            if (statusMessage != null && !statusMessage.isBlank()) {
                showCopiedMessage(statusMessage);
            }
            return true;
        }

        ShapefileData data = getShapefileData(layer);
        SimpleFeature primaryFeature = data != null ? findFeatureById(data.getFeatures(), normalizedIds.get(0)) : null;
        if (activateEditForSingle && normalizedIds.size() == 1 && primaryFeature != null) {
            enableFeatureEdit(layer, primaryFeature);
            if (statusMessage != null && !statusMessage.isBlank()) {
                showCopiedMessage(statusMessage);
            }
            return true;
        }

        if (promptOnDirty && !confirmPendingFeatureEdit("cambiar la selección")) {
            return false;
        }

        selectionFlashGeometry = null;
        selectionFlashTimer.stop();
        tableSelectionIds.clear();
        tableSelectionIds.put(layer, new ArrayList<>(normalizedIds));
        selectedLayer = layer;
        selectedFeature = primaryFeature;
        featureEditMode = false;
        featureEditOriginalGeometry = null;
        featureEditDirty = false;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        editUndoStack.clear();
        editRedoStack.clear();
        if (!EDIT_OP_MOVE_FEATURE.equals(featureEditOperation)) {
            featureEditOperation = EDIT_OP_MOVE_VERTEX;
        }

        if (syncOpenTables) {
            OpenAttributeTableAction.syncSelectionFromMap(layer, normalizedIds);
        }
        if (primaryFeature != null) {
            startSelectionFlash(layer, primaryFeature);
        }
        if (statusMessage != null && !statusMessage.isBlank()) {
            showCopiedMessage(statusMessage);
        }
        refreshEditingUi();
        return true;
    }

    private List<String> mergeSelectionIds(Layer layer, List<String> currentIds, List<String> candidateIds, boolean toggleSingleCandidate) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (currentIds != null) {
            merged.addAll(currentIds);
        }
        if (candidateIds == null || candidateIds.isEmpty()) {
            return new ArrayList<>(merged);
        }

        if (toggleSingleCandidate && candidateIds.size() == 1) {
            String candidate = candidateIds.get(0);
            if (merged.contains(candidate)) {
                merged.remove(candidate);
            } else {
                merged.add(candidate);
            }
        } else {
            merged.addAll(candidateIds);
        }
        return new ArrayList<>(merged);
    }

    public void prepareLayerForEditing(Layer layer) {
        if (layer == null) {
            return;
        }
        if ((featureEditMode || featureEditDirty)
                && (layer != activeVectorEditingLayer || selectedLayer != layer)
                && !confirmPendingFeatureEdit("cambiar de capa editable")) {
            return;
        }
        activeVectorEditingLayer = layer;
        if (selectedLayer != layer) {
            selectedLayer = layer;
            selectedFeature = null;
            featureEditMode = false;
            featureEditOriginalGeometry = null;
            featureEditDirty = false;
            featureEditOperation = EDIT_OP_MOVE_VERTEX;
            featureEditSketchCoordinates.clear();
            activeEditVertexIndex = -1;
            editUndoStack.clear();
            editRedoStack.clear();
        }
        setTool("SELECT");
        refreshEditingUi();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Capa lista para edicion: " + layer.getName() + ". Selecciona una entidad con la flecha.");
        }
    }

    public void enableFeatureEdit(Layer layer, SimpleFeature feature) {
        boolean sameSelection = layer == selectedLayer && feature == selectedFeature && featureEditMode;
        if (!sameSelection && !confirmPendingFeatureEdit("cambiar de entidad en edición")) {
            return;
        }
        activeVectorEditingLayer = layer;
        selectedLayer = layer;
        selectedFeature = feature;
        if (layer != null && feature != null) {
            tableSelectionIds.clear();
            tableSelectionIds.put(layer, new ArrayList<>(List.of(feature.getID())));
            OpenAttributeTableAction.syncSelectionFromMap(layer, List.of(feature.getID()));
        }
        featureEditMode = layer != null && feature != null;
        if (!sameSelection) {
            featureEditOriginalGeometry = extractFeatureGeometryCopy(feature);
            featureEditDirty = false;
            featureEditOperation = EDIT_OP_MOVE_VERTEX;
            featureEditSketchCoordinates.clear();
            editUndoStack.clear();
            editRedoStack.clear();
        }
        activeEditVertexIndex = -1;
        setTool("SELECT");
        if (featureEditMode && CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Edicion activa en rojo. Arrastra o modifica los vertices de la entidad seleccionada.");
        }
        if (featureEditMode) {
            startSelectionFlash(layer, feature);
        }
        refreshEditingUi();
    }

    public void enablePanMode() {
        cancelCurrentDrawing();
        cancelCurrentMeasurement();
        setTool("MOVE");
    }

    public void enableIdentifyMode() {
        cancelCurrentDrawing();
        cancelCurrentMeasurement();
        setTool("IDENTIFY");
    }

    public void enableSelectMode() {
        cancelCurrentDrawing();
        cancelCurrentMeasurement();
        setTool("SELECT");
    }

    public void activateMoveVertexMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeatureLinearOrPolygonal()) {
            JOptionPane.showMessageDialog(this, "Mover vértices sólo funciona sobre líneas o polígonos.");
            return;
        }
        featureEditOperation = EDIT_OP_MOVE_VERTEX;
        featureEditSketchCoordinates.clear();
        setTool("SELECT");
        showCopiedMessage("Modo mover vértice activo.");
        refreshEditingUi();
    }

    public void activateMoveFeatureMode() {
        if (!hasFeatureSelection()) {
            return;
        }
        featureEditOperation = EDIT_OP_MOVE_FEATURE;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        setTool("SELECT");
        showCopiedMessage("Modo mover elementos activo. Arrastrá una entidad seleccionada.");
        refreshEditingUi();
    }

    public void activateAddVertexMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeatureLinearOrPolygonal()) {
            JOptionPane.showMessageDialog(this, "Agregar vértices sólo funciona sobre líneas o polígonos.");
            return;
        }
        featureEditOperation = EDIT_OP_ADD_VERTEX;
        featureEditSketchCoordinates.clear();
        setTool("SELECT");
        showCopiedMessage("Modo agregar vértice activo. Hacé clic o arrastrá una caja sobre un tramo.");
        refreshEditingUi();
    }

    public void activateRemoveVertexMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeatureLinearOrPolygonal()) {
            JOptionPane.showMessageDialog(this, "Eliminar vértices sólo funciona sobre líneas o polígonos.");
            return;
        }
        featureEditOperation = EDIT_OP_REMOVE_VERTEX;
        featureEditSketchCoordinates.clear();
        setTool("SELECT");
        showCopiedMessage("Modo eliminar vértice activo. Hacé clic o arrastrá una caja para quitar uno o varios vértices.");
        refreshEditingUi();
    }

    public void activateCutFeatureMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeatureLinearOrPolygonal()) {
            JOptionPane.showMessageDialog(this, "Cortar geometría sólo funciona sobre líneas o polígonos.");
            return;
        }
        featureEditOperation = EDIT_OP_CUT;
        featureEditSketchCoordinates.clear();
        setTool("SELECT");
        String hint = isSelectedFeaturePolygonal()
                ? "Modo cortar activo. Dibujá la línea de corte y terminá con doble clic."
                : "Modo cortar activo. Hacé clic sobre la línea en el punto donde querés cortarla.";
        showCopiedMessage(hint);
        refreshEditingUi();
    }

    public void activateHoleMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeaturePolygonal()) {
            JOptionPane.showMessageDialog(this, "La opción agujero solo funciona sobre polígonos.");
            return;
        }
        featureEditOperation = EDIT_OP_HOLE;
        featureEditSketchCoordinates.clear();
        setTool("SELECT");
        showCopiedMessage("Modo agujero activo. Dibujá el polígono interior y terminá con doble clic.");
        refreshEditingUi();
    }

    public void finishFeatureEdit() {
        if (!featureEditMode && activeVectorEditingLayer == null) {
            return;
        }
        if (!confirmPendingFeatureEdit("salir de la edición")) {
            return;
        }

        activeVectorEditingLayer = null;
        featureEditMode = false;
        featureEditOperation = EDIT_OP_MOVE_VERTEX;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        featureEditOriginalGeometry = null;
        featureEditDirty = false;
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Edicion finalizada.");
        }
        editUndoStack.clear();
        editRedoStack.clear();
        refreshEditingUi();
    }

    public boolean saveFeatureEditChanges() {
        if (!featureEditMode || selectedFeature == null) {
            return false;
        }

        featureEditOriginalGeometry = extractFeatureGeometryCopy(selectedFeature);
        featureEditDirty = false;
        featureEditSketchCoordinates.clear();
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Cambios geométricos guardados en la sesión del proyecto.");
        }
        refreshEditingUi();
        return true;
    }

    public void cancelFeatureEdit() {
        if (!confirmPendingFeatureEdit("cancelar la edición")) {
            return;
        }
        restoreFeatureEditOriginalGeometry();
        activeVectorEditingLayer = null;
        featureEditMode = false;
        featureEditOperation = EDIT_OP_MOVE_VERTEX;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        featureEditOriginalGeometry = null;
        featureEditDirty = false;
        editUndoStack.clear();
        editRedoStack.clear();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Edicion cancelada.");
        }
        refreshEditingUi();
    }

    public void enableDrawPointMode() {
        cancelCurrentMeasurement();
        drawingMode = "POINT";
        drawingCoordinates.clear();
        setTool("DRAW");
        showCopiedMessage("Modo dibujo puntos activo. Hacé varios clics. Clic derecho para terminar y Escape para cancelar.");
        repaint();
    }

    public void enableDrawMultiPointMode() {
        cancelCurrentMeasurement();
        drawingMode = "MULTIPOINT";
        drawingCoordinates.clear();
        setTool("DRAW");
        showCopiedMessage("Modo dibujo multipunto activo. Hacé varios clics. Clic derecho para terminar y Escape para cancelar.");
        repaint();
    }

    public void enableDrawLineMode() {
        cancelCurrentMeasurement();
        drawingMode = "LINE";
        drawingCoordinates.clear();
        setTool("DRAW");
        showCopiedMessage("Modo dibujo línea activo. Clic para vértices. Doble clic o clic derecho para terminar. Escape para cancelar.");
        repaint();
    }

    public void enableDrawPolygonMode() {
        cancelCurrentMeasurement();
        drawingMode = "POLYGON";
        drawingCoordinates.clear();
        setTool("DRAW");
        showCopiedMessage("Modo dibujo polígono activo. Clic para vértices. Doble clic o clic derecho para terminar. Escape para cancelar.");
        repaint();
    }

    public void enableMeasureDistanceMode() {
        cancelCurrentDrawing();
        measurementMode = "DISTANCE";
        measurementCoordinates.clear();
        setTool("MEASURE");
        showCopiedMessage("Modo medir distancia activo. Clic para vértices. Doble clic o clic derecho para terminar. Escape para cancelar.");
        repaint();
    }

    public void enableMeasureAreaMode() {
        cancelCurrentDrawing();
        measurementMode = "AREA";
        measurementCoordinates.clear();
        setTool("MEASURE");
        showCopiedMessage("Modo medir área activo. Clic para vértices. Doble clic o clic derecho para terminar. Escape para cancelar.");
        repaint();
    }

    public boolean isMeasurementActive() {
        return measurementMode != null && !measurementMode.isBlank();
    }

    public String getMeasurementMode() {
        return measurementMode;
    }

    public void cancelCurrentDrawing() {
        drawingMode = null;
        drawingCoordinates.clear();
        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        repaint();
    }

    public void cancelCurrentMeasurement() {
        measurementMode = null;
        measurementCoordinates.clear();
        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        repaint();
    }

    public void finishCurrentMeasurement() {
        if (!isMeasurementActive()) {
            return;
        }

        try {
            String projectCRS = (CatgisDesktopApp.currentProject != null &&
                    CatgisDesktopApp.currentProject.getProjectCRS() != null &&
                    !CatgisDesktopApp.currentProject.getProjectCRS().isBlank())
                    ? CatgisDesktopApp.currentProject.getProjectCRS()
                    : "EPSG:4326";

            if ("DISTANCE".equalsIgnoreCase(measurementMode)) {
                if (measurementCoordinates.size() < 2) {
                    JOptionPane.showMessageDialog(this, "Para medir distancia necesitás al menos 2 vértices.");
                    return;
                }

                Geometry metricLine = buildMeasurementLineInMeters(measurementCoordinates, projectCRS);
                if (metricLine == null) {
                    JOptionPane.showMessageDialog(this, "No se pudo calcular la distancia.");
                    return;
                }

                double totalMeters = metricLine.getLength();

                JOptionPane.showMessageDialog(
                        this,
                        "Distancia total: " + formatDistance(totalMeters),
                        "Medición de distancia",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else if ("AREA".equalsIgnoreCase(measurementMode)) {
                if (measurementCoordinates.size() < 3) {
                    JOptionPane.showMessageDialog(this, "Para medir área necesitás al menos 3 vértices.");
                    return;
                }

                Geometry metricPolygon = buildMeasurementPolygonInMeters(measurementCoordinates, projectCRS);
                if (metricPolygon == null) {
                    JOptionPane.showMessageDialog(this, "No se pudo calcular el área.");
                    return;
                }

                double areaMeters = metricPolygon.getArea();
                double perimeterMeters = metricPolygon.getLength();

                JOptionPane.showMessageDialog(
                        this,
                        "Área: " + formatArea(areaMeters) + "\n" +
                                "Perímetro: " + formatDistance(perimeterMeters),
                        "Medición de área",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } finally {
            cancelCurrentMeasurement();
            CatgisDesktopApp.syncFloatingVectorEditToolbar();
        }
    }

    private Geometry buildMeasurementLineInMeters(List<Coordinate> coordinates, String sourceCRSCode) {
        try {
            if (coordinates == null || coordinates.size() < 2) {
                return null;
            }

            org.locationtech.jts.geom.GeometryFactory gf = new org.locationtech.jts.geom.GeometryFactory();
            org.locationtech.jts.geom.LineString line =
                    gf.createLineString(coordinates.toArray(new Coordinate[0]));

            return reprojectGeometryToMetric(line, sourceCRSCode);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Geometry buildMeasurementPolygonInMeters(List<Coordinate> coordinates, String sourceCRSCode) {
        try {
            Geometry polygon = DrawFeatureBuilder.buildPolygon(coordinates);
            if (polygon == null) {
                return null;
            }

            return reprojectGeometryToMetric(polygon, sourceCRSCode);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Geometry reprojectGeometryToMetric(Geometry geometry, String sourceCRSCode) {
        try {
            if (geometry == null || geometry.isEmpty()) {
                return geometry;
            }

            String sourceCode = (sourceCRSCode != null && !sourceCRSCode.isBlank())
                    ? sourceCRSCode
                    : "EPSG:4326";

            String targetMetricCode = chooseMetricCRSForMeasurement(sourceCode);

            if (sourceCode.equalsIgnoreCase(targetMetricCode)) {
                return geometry;
            }

            CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCode, true);
            CoordinateReferenceSystem targetCRS = CRS.decode(targetMetricCode, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            return JTS.transform(geometry, transform);
        } catch (Exception ex) {
            ex.printStackTrace();
            return geometry;
        }
    }

    private String chooseMetricCRSForMeasurement(String sourceCRSCode) {
        if (sourceCRSCode == null || sourceCRSCode.isBlank()) {
            return "EPSG:3857";
        }

        String code = sourceCRSCode.trim().toUpperCase(Locale.ROOT);

        if (code.equals("EPSG:4326")) {
            return "EPSG:3857";
        }

        if (code.startsWith("EPSG:327") || code.startsWith("EPSG:326")) {
            return code;
        }

        if (code.startsWith("EPSG:221") || code.startsWith("EPSG:534") || code.startsWith("EPSG:248")) {
            return code;
        }

        return "EPSG:3857";
    }

    private String formatDistance(double meters) {
        if (meters >= 1000.0) {
            return String.format(Locale.US, "%.3f km", meters / 1000.0);
        }
        return String.format(Locale.US, "%.2f m", meters);
    }

    private String formatArea(double squareMeters) {
        if (squareMeters >= 10000.0) {
            return String.format(Locale.US, "%.3f ha", squareMeters / 10000.0);
        }
        return String.format(Locale.US, "%.2f m²", squareMeters);
    }

    public void finishCurrentDrawing() {
        if (!isDrawingActive()) {
            return;
        }

        try {
            Geometry geometry = null;
            String baseName = "Dibujo_" + System.currentTimeMillis();
            ShapefileData data;

            if ("POINT".equalsIgnoreCase(drawingMode)) {
                if (drawingCoordinates.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Para crear puntos necesitás hacer clic en el mapa.");
                    return;
                }

                data = DrawFeatureBuilder.buildPointLayer(drawingCoordinates, baseName);
            } else if ("MULTIPOINT".equalsIgnoreCase(drawingMode)) {
                if (drawingCoordinates.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Para crear un multipunto necesitás hacer clic en el mapa.");
                    return;
                }

                data = DrawFeatureBuilder.buildMultiPointLayer(drawingCoordinates, baseName);
            } else if ("LINE".equalsIgnoreCase(drawingMode)) {
                geometry = DrawFeatureBuilder.buildLine(drawingCoordinates);
                if (geometry == null) {
                    JOptionPane.showMessageDialog(this, "Para una línea necesitás al menos 2 vértices.");
                    return;
                }
                data = DrawFeatureBuilder.buildSingleGeometryLayer(geometry, baseName);
            } else if ("POLYGON".equalsIgnoreCase(drawingMode)) {
                geometry = DrawFeatureBuilder.buildPolygon(drawingCoordinates);
                if (geometry == null) {
                    JOptionPane.showMessageDialog(this, "Para un polígono necesitás al menos 3 vértices.");
                    return;
                }
                data = DrawFeatureBuilder.buildSingleGeometryLayer(geometry, baseName);
            } else {
                return;
            }

            String projectCRS = (CatgisDesktopApp.currentProject != null &&
                    CatgisDesktopApp.currentProject.getProjectCRS() != null &&
                    !CatgisDesktopApp.currentProject.getProjectCRS().isBlank())
                    ? CatgisDesktopApp.currentProject.getProjectCRS()
                    : "EPSG:4326";

            Layer layer = new Layer(baseName, baseName, "VECTOR");
            layer.setVisible(true);
            layer.setSourceName(data.getSourceName());
            layer.setFeatureCount(data.getFeatureCount());
            layer.setSourceCRS(projectCRS);
            layer.setLabelsVisible(true);
            layer.setLabelField("tipo");

            if (CatgisDesktopApp.currentProject == null) {
                CatgisDesktopApp.currentProject = new Project("Proyecto actual");
            }

            CatgisDesktopApp.currentProject.addLayer(layer);
            CatgisDesktopApp.markProjectDirty();
            CatgisDesktopApp.layersPanel.addLayer(layer);
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
            CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());

            drawingMode = null;
            drawingCoordinates.clear();
            CatgisDesktopApp.syncFloatingVectorEditToolbar();
            repaint();

            JOptionPane.showMessageDialog(this, "Dibujo convertido en capa correctamente.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al convertir dibujo en capa: " + ex.getMessage());
        }
    }

    public boolean isDrawingActive() {
        return drawingMode != null && !drawingMode.isBlank();
    }

    public String getDrawingMode() {
        return drawingMode;
    }

    public void zoomIn() {
        rememberCurrentView();
        applyZoom(1.2, getWidth() / 2, getHeight() / 2);
        rememberCurrentView();
    }

    public void zoomOut() {
        rememberCurrentView();
        applyZoom(1.0 / 1.2, getWidth() / 2, getHeight() / 2);
        rememberCurrentView();
    }

    public void showShapefile(Layer layer, ShapefileData data) {
        if (layer == null || data == null) {
            return;
        }

        boolean wasEmpty = shapefileLayers.isEmpty();
        shapefileLayers.put(layer, data);

        if (wasEmpty) {
            SwingUtilities.invokeLater(() -> {
                fitToAllLayers();
                rememberCurrentView();
                repaint();
            });
        } else {
            repaint();
        }
    }

    public void addOrUpdateShapefileLayer(Layer layer, ShapefileData data) {
        if (layer == null || data == null) {
            return;
        }

        boolean wasEmpty = shapefileLayers.isEmpty();
        shapefileLayers.put(layer, data);

        if (wasEmpty) {
            SwingUtilities.invokeLater(() -> {
                fitToAllLayers();
                rememberCurrentView();
                repaint();
            });
        } else {
            repaint();
        }
    }

    public void addOrUpdateRasterLayer(Layer layer, LocalRasterData data) {
        if (layer == null || data == null) {
            return;
        }

        boolean wasEmpty = shapefileLayers.isEmpty() && rasterLayers.isEmpty();
        rasterLayers.put(layer, data);
        invalidateRasterDisplay(layer);
        getOrCreateRasterStyle(layer, Math.max(1, data.getBandCount()));

        if (wasEmpty) {
            SwingUtilities.invokeLater(() -> {
                fitToAllLayers();
                rememberCurrentView();
                repaint();
            });
        } else {
            repaint();
        }
    }

    public LocalRasterData getRasterData(Layer layer) {
        return rasterLayers.get(layer);
    }

    public RasterStyle getOrCreateRasterStyle(Layer layer, int bandCount) {
        RasterStyle style = rasterStyles.get(layer);
        if (style == null) {
            style = new RasterStyle();
            if (layer instanceof RasterLayer) {
                RasterLayer rasterLayer = (RasterLayer) layer;
                style.opacity = rasterLayer.getOpacity();
                style.grayscale = rasterLayer.isGrayscale();
                style.autoContrast = rasterLayer.isAutoContrast();
                style.redBand = rasterLayer.getRedBand();
                style.greenBand = rasterLayer.getGreenBand();
                style.blueBand = rasterLayer.getBlueBand();
            } else {
                style.redBand = 0;
                style.greenBand = Math.min(1, Math.max(0, bandCount - 1));
                style.blueBand = Math.min(2, Math.max(0, bandCount - 1));
            }
            rasterStyles.put(layer, style);
        }
        style.redBand = Math.max(0, Math.min(style.redBand, Math.max(0, bandCount - 1)));
        style.greenBand = Math.max(0, Math.min(style.greenBand, Math.max(0, bandCount - 1)));
        style.blueBand = Math.max(0, Math.min(style.blueBand, Math.max(0, bandCount - 1)));
        return style;
    }

    public void applyRasterStyle(Layer layer, float opacity, boolean grayscale, boolean autoContrast, int redBand, int greenBand, int blueBand) {
        LocalRasterData data = rasterLayers.get(layer);
        int bandCount = data != null ? Math.max(1, data.getBandCount()) : 1;
        RasterStyle style = getOrCreateRasterStyle(layer, bandCount);
        style.opacity = Math.max(0f, Math.min(1f, opacity));
        style.grayscale = grayscale;
        style.autoContrast = autoContrast;
        style.redBand = Math.max(0, Math.min(redBand, bandCount - 1));
        style.greenBand = Math.max(0, Math.min(greenBand, bandCount - 1));
        style.blueBand = Math.max(0, Math.min(blueBand, bandCount - 1));
        if (layer instanceof RasterLayer) {
            RasterLayer rasterLayer = (RasterLayer) layer;
            rasterLayer.setOpacity(style.opacity);
            rasterLayer.setGrayscale(style.grayscale);
            rasterLayer.setAutoContrast(style.autoContrast);
            rasterLayer.setRedBand(style.redBand);
            rasterLayer.setGreenBand(style.greenBand);
            rasterLayer.setBlueBand(style.blueBand);
        }
        invalidateRasterDisplay(layer);
        CatgisDesktopApp.markProjectDirty();
        repaint();
    }

    public void removeLayer(Layer layer) {
        if (layer == null) {
            return;
        }

        shapefileLayers.remove(layer);
        rasterLayers.remove(layer);
        rasterStyles.remove(layer);
        invalidateRasterDisplay(layer);

        if (selectedLayer == layer) {
            selectedLayer = null;
            selectedFeature = null;
        }

        if (!shapefileLayers.isEmpty() || !rasterLayers.isEmpty()) {
            fitToAllLayers();
        }

        repaint();
    }

    public void refreshMap() {
        repaint();
    }

    public void refreshLayerVisibility() {
        repaint();
    }

    public void clearAllLayers() {
        shapefileLayers.clear();
        rasterLayers.clear();
        rasterStyles.clear();
        rasterDisplayCache.clear();
        selectedLayer = null;
        selectedFeature = null;
        clearAllPins();
        cancelCurrentDrawing();
        cancelCurrentMeasurement();
        repaint();
    }

    public void resetView() {
        SwingUtilities.invokeLater(() -> {
            rememberCurrentView();
            fitToAllLayers();
            rememberCurrentView();
            repaint();
        });
    }

    public double getViewMinX() {
        return viewMinX;
    }

    public double getViewMinY() {
        return viewMinY;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public void restoreView(double viewMinX, double viewMinY, double zoomFactor) {
        if (zoomFactor <= 0) {
            return;
        }

        this.viewMinX = viewMinX;
        this.viewMinY = viewMinY;
        this.zoomFactor = zoomFactor;
        repaint();
    }

    public void zoomToLayer(Layer layer) {
        if (layer == null) {
            return;
        }

        ShapefileData data = shapefileLayers.get(layer);
        LocalRasterData raster = rasterLayers.get(layer);
        if (data == null && raster == null) {
            JOptionPane.showMessageDialog(this, "La capa no tiene datos cargados en el mapa.");
            return;
        }

        layer.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            rememberCurrentView();
            Envelope env = data != null ? getLayerEnvelope(layer, data) : getRasterEnvelope(layer, raster);

            if (env == null || env.isNull()) {
                JOptionPane.showMessageDialog(this, "No se pudo calcular la extensión de la capa.");
                return;
            }

            fitToEnvelope(env);
            rememberCurrentView();
            repaint();
        });
    }

    public void moveLayerUp(Layer layer) {
        if (layer == null) {
            return;
        }
        if (rasterLayers.containsKey(layer)) {
            moveRasterUp(layer);
            return;
        }
        if (!shapefileLayers.containsKey(layer)) {
            return;
        }

        Layer[] keys = shapefileLayers.keySet().toArray(new Layer[0]);

        for (int i = 1; i < keys.length; i++) {
            if (keys[i] == layer) {
                Layer previous = keys[i - 1];
                ShapefileData currentData = shapefileLayers.get(layer);
                ShapefileData previousData = shapefileLayers.get(previous);

                LinkedHashMap<Layer, ShapefileData> reordered = new LinkedHashMap<>();

                for (int j = 0; j < keys.length; j++) {
                    if (j == i - 1) {
                        reordered.put(layer, currentData);
                        reordered.put(previous, previousData);
                        j++;
                    } else {
                        reordered.put(keys[j], shapefileLayers.get(keys[j]));
                    }
                }

                shapefileLayers.clear();
                shapefileLayers.putAll(reordered);
                repaint();
                return;
            }
        }
    }

    public void moveLayerDown(Layer layer) {
        if (layer == null) {
            return;
        }
        if (rasterLayers.containsKey(layer)) {
            moveRasterDown(layer);
            return;
        }
        if (!shapefileLayers.containsKey(layer)) {
            return;
        }

        Layer[] keys = shapefileLayers.keySet().toArray(new Layer[0]);

        for (int i = 0; i < keys.length - 1; i++) {
            if (keys[i] == layer) {
                Layer next = keys[i + 1];
                ShapefileData currentData = shapefileLayers.get(layer);
                ShapefileData nextData = shapefileLayers.get(next);

                LinkedHashMap<Layer, ShapefileData> reordered = new LinkedHashMap<>();

                for (int j = 0; j < keys.length; j++) {
                    if (j == i) {
                        reordered.put(next, nextData);
                        reordered.put(layer, currentData);
                        j++;
                    } else {
                        reordered.put(keys[j], shapefileLayers.get(keys[j]));
                    }
                }

                shapefileLayers.clear();
                shapefileLayers.putAll(reordered);
                repaint();
                return;
            }
        }
    }

    public void reorderLayers(List<Layer> orderedLayers) {
        if (orderedLayers == null || orderedLayers.isEmpty()) {
            return;
        }

        LinkedHashMap<Layer, ShapefileData> reorderedVectors = new LinkedHashMap<>();
        LinkedHashMap<Layer, LocalRasterData> reorderedRasters = new LinkedHashMap<>();

        for (Layer layer : orderedLayers) {
            if (layer == null) {
                continue;
            }
            if (shapefileLayers.containsKey(layer)) {
                reorderedVectors.put(layer, shapefileLayers.get(layer));
            }
            if (rasterLayers.containsKey(layer)) {
                reorderedRasters.put(layer, rasterLayers.get(layer));
            }
        }

        for (Map.Entry<Layer, ShapefileData> entry : shapefileLayers.entrySet()) {
            reorderedVectors.putIfAbsent(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Layer, LocalRasterData> entry : rasterLayers.entrySet()) {
            reorderedRasters.putIfAbsent(entry.getKey(), entry.getValue());
        }

        shapefileLayers.clear();
        shapefileLayers.putAll(reorderedVectors);
        rasterLayers.clear();
        rasterLayers.putAll(reorderedRasters);
        repaint();
    }

    public ShapefileData getShapefileData(Layer layer) {
        return shapefileLayers.get(layer);
    }

    public void setLayerLabeling(Layer layer, boolean visible, String labelField) {
        if (layer == null) {
            return;
        }

        layer.setLabelsVisible(visible);
        layer.setLabelField(labelField);
        repaint();
    }

    public void showOpenedFile(String text) {
        openedFileText = text != null ? text : "";
        repaint();
    }

    public void showLayerInfo(Layer layer) {
        if (layer == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Nombre: ").append(layer.getName()).append("\n");
        sb.append("Ruta: ").append(layer.getPath()).append("\n");
        sb.append("Tipo: ").append(layer.getType()).append("\n");
        sb.append("Visible: ").append(layer.isVisible() ? "Sí" : "No").append("\n");
        sb.append("SourceName: ").append(layer.getSourceName() != null ? layer.getSourceName() : "-").append("\n");
        sb.append("FeatureCount: ").append(layer.getFeatureCount()).append("\n");
        sb.append("CRS origen: ").append(layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank() ? layer.getSourceCRS() : "desconocido").append("\n");
        sb.append("CRS proyecto: ").append(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "-").append("\n");
        LocalRasterData rasterDataInfo = rasterLayers.get(layer);
        if (rasterDataInfo != null) {
            sb.append("Raster: Sí\n");
            sb.append("Bandas: ").append(rasterDataInfo.getBandCount()).append("\n");
            sb.append("Georreferenciado: ").append(rasterDataInfo.isGeoreferenced() ? "Sí" : "No").append("\n");
        }
        sb.append("Etiquetas: ").append(layer.isLabelsVisible() ? "Sí" : "No").append("\n");
        sb.append("Campo etiqueta: ").append(layer.getLabelField() != null ? layer.getLabelField() : "-").append("\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "Información de capa", JOptionPane.INFORMATION_MESSAGE);
    }

    private Geometry reprojectGeometryIfNeeded(Layer layer, Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return geometry;
        }

        try {
            String sourceCode = layer != null ? layer.getSourceCRS() : "";
            String targetCode = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";

            if (sourceCode == null || sourceCode.isBlank()) {
                return geometry;
            }
            if (targetCode == null || targetCode.isBlank()) {
                return geometry;
            }
            if (sourceCode.equalsIgnoreCase(targetCode)) {
                return geometry;
            }

            CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCode, true);
            CoordinateReferenceSystem targetCRS = CRS.decode(targetCode, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            return JTS.transform(geometry, transform);
        } catch (Exception ex) {
            return geometry;
        }
    }

    private Envelope reprojectEnvelopeIfNeeded(Layer layer, Envelope env) {
        String sourceCode = layer != null ? layer.getSourceCRS() : "";
        String targetCode = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        return reprojectEnvelopeIfNeeded(env, sourceCode, targetCode);
    }

    private Envelope reprojectEnvelopeIfNeeded(Envelope env, String sourceCode, String targetCode) {
        if (env == null || env.isNull()) {
            return env;
        }

        try {
            if (sourceCode == null || sourceCode.isBlank()) {
                return env;
            }
            if (targetCode == null || targetCode.isBlank()) {
                return env;
            }
            if (sourceCode.equalsIgnoreCase(targetCode)) {
                return env;
            }

            CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCode, true);
            CoordinateReferenceSystem targetCRS = CRS.decode(targetCode, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            return JTS.transform(env, null, transform, 10);
        } catch (Exception ex) {
            return env;
        }
    }

    private double[] transformPoint(double x, double y, String sourceCode, String targetCode) {
        try {
            if (sourceCode == null || sourceCode.isBlank()) {
                return null;
            }
            if (targetCode == null || targetCode.isBlank()) {
                return null;
            }
            if (sourceCode.equalsIgnoreCase(targetCode)) {
                return new double[]{x, y};
            }

            CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCode, true);
            CoordinateReferenceSystem targetCRS = CRS.decode(targetCode, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            double[] src = new double[]{x, y};
            double[] dst = new double[2];
            transform.transform(src, 0, dst, 0, 1);
            return dst;
        } catch (Exception ex) {
            return null;
        }
    }

    private void showMapPopup(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (isDrawingActive()) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem finishItem = new JMenuItem("Terminar dibujo");
            finishItem.addActionListener(ev -> finishCurrentDrawing());
            popupMenu.add(finishItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar dibujo");
            cancelItem.addActionListener(ev -> cancelCurrentDrawing());
            popupMenu.add(cancelItem);

            popupMenu.show(this, x, y);
            return;
        }

        if (isMeasurementActive()) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem finishItem = new JMenuItem("Terminar medición");
            finishItem.addActionListener(ev -> finishCurrentMeasurement());
            popupMenu.add(finishItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar medición");
            cancelItem.addActionListener(ev -> cancelCurrentMeasurement());
            popupMenu.add(cancelItem);

            popupMenu.show(this, x, y);
            return;
        }

        if (featureEditMode && isFeatureEditSketchMode() && !featureEditSketchCoordinates.isEmpty()) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem finishItem = new JMenuItem("Aplicar edición");
            finishItem.addActionListener(ev -> applyFeatureEditSketchOperationEnhanced());
            popupMenu.add(finishItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar boceto");
            cancelItem.addActionListener(ev -> {
                featureEditSketchCoordinates.clear();
                repaint();
                showCopiedMessage("Boceto de edición cancelado.");
            });
            popupMenu.add(cancelItem);

            popupMenu.show(this, x, y);
            return;
        }

        PinMarker clickedPin = findPinAtScreen(x, y);
        if (clickedPin != null) {
            showPinPopup(e, clickedPin);
            return;
        }

        List<IdentifyResultItem> vectorHits = collectIdentifyResults(x, y);
        if (!vectorHits.isEmpty()) {
            IdentifyResultItem hit = vectorHits.get(0);
            highlightIdentifiedFeature(hit.getLayer(), hit.getFeature());
            showFeaturePopup(e, hit);
            return;
        }

        double projectX = screenToWorldX(x);
        double projectY = screenToWorldY(y);
        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        double[] geographic = transformPoint(projectX, projectY, projectCRS, "EPSG:4326");

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem addPinItem = new JMenuItem("Agregar pin aquí");
        addPinItem.addActionListener(ev -> {
            PinMarker pin = addPin(projectX, projectY);
            showCopiedMessage("Pin P" + pin.getId() + " agregado.");
        });
        popupMenu.add(addPinItem);

        JMenuItem copyProjectItem = new JMenuItem("Copiar coordenada proyecto (X/Y)");
        copyProjectItem.addActionListener(ev -> {
            String text = formatNumber(projectX) + ", " + formatNumber(projectY);
            copyToClipboard(text);
            showCopiedMessage("Coordenada del proyecto copiada.");
        });
        popupMenu.add(copyProjectItem);

        JMenuItem copyLatLonItem = new JMenuItem("Copiar Lat/Long");
        copyLatLonItem.addActionListener(ev -> {
            if (geographic == null) {
                JOptionPane.showMessageDialog(this, "No se pudo transformar a EPSG:4326.");
                return;
            }
            String text = formatNumber(geographic[0]) + ", " + formatNumber(geographic[1]);
            copyToClipboard(text);
            showCopiedMessage("Lat/Long copiada.");
        });
        popupMenu.add(copyLatLonItem);

        JMenuItem copyDmsItem = new JMenuItem("Copiar Lat/Long DMS");
        copyDmsItem.addActionListener(ev -> {
            if (geographic == null) {
                JOptionPane.showMessageDialog(this, "No se pudo transformar a EPSG:4326.");
                return;
            }
            String text = toDms(geographic[0], false) + " , " + toDms(geographic[1], true);
            copyToClipboard(text);
            showCopiedMessage("Lat/Long DMS copiada.");
        });
        popupMenu.add(copyDmsItem);

        JMenuItem showItem = new JMenuItem("Ver coordenadas del punto");
        showItem.addActionListener(ev -> showCoordinateDialog(x, y));
        popupMenu.add(showItem);

        if (!pins.isEmpty()) {
            popupMenu.addSeparator();

            JMenuItem convertPinsItem = new JMenuItem("Convertir pines en capa");
            convertPinsItem.addActionListener(ev -> convertPinsToLayer());
            popupMenu.add(convertPinsItem);

            JMenuItem clearPinsItem = new JMenuItem("Borrar todos los pines");
            clearPinsItem.addActionListener(ev -> {
                clearAllPins();
                showCopiedMessage("Todos los pines fueron eliminados.");
            });
            popupMenu.add(clearPinsItem);
        }

        popupMenu.show(this, x, y);
    }

    private void showFeaturePopup(MouseEvent e, IdentifyResultItem hit) {
        if (hit == null || hit.getLayer() == null || hit.getFeature() == null) {
            return;
        }

        Layer layer = hit.getLayer();
        SimpleFeature feature = hit.getFeature();

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Editar vector");
        editItem.addActionListener(ev -> {
            enableFeatureEdit(layer, feature);
            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("Edicion vectorial lista para: " + layer.getName());
            }
            CatgisDesktopApp.syncFloatingVectorEditToolbar();
        });
        popupMenu.add(editItem);

        Object geometry = feature.getDefaultGeometry();
        boolean linearOrPolygonal = geometry instanceof LineString
                || geometry instanceof MultiLineString
                || geometry instanceof Polygon
                || geometry instanceof MultiPolygon;

        if (linearOrPolygonal) {
            JMenuItem moveVertexItem = new JMenuItem("Mover vertices");
            moveVertexItem.addActionListener(ev -> {
                enableFeatureEdit(layer, feature);
                activateMoveVertexMode();
            });
            popupMenu.add(moveVertexItem);

            JMenuItem addVertexItem = new JMenuItem("Agregar vertice");
            addVertexItem.addActionListener(ev -> {
                enableFeatureEdit(layer, feature);
                activateAddVertexMode();
            });
            popupMenu.add(addVertexItem);

            JMenuItem removeVertexItem = new JMenuItem("Eliminar vertice");
            removeVertexItem.addActionListener(ev -> {
                enableFeatureEdit(layer, feature);
                activateRemoveVertexMode();
            });
            popupMenu.add(removeVertexItem);

            JMenuItem cutItem = new JMenuItem("Cortar geometria");
            cutItem.addActionListener(ev -> {
                enableFeatureEdit(layer, feature);
                activateCutFeatureMode();
            });
            popupMenu.add(cutItem);
        }

        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            JMenuItem holeItem = new JMenuItem("Crear agujero");
            holeItem.addActionListener(ev -> {
                enableFeatureEdit(layer, feature);
                activateHoleMode();
            });
            popupMenu.add(holeItem);
        }

        popupMenu.addSeparator();

        JMenuItem infoItem = new JMenuItem("Ver informacion");
        infoItem.addActionListener(ev -> showFeatureInfo(feature, layer));
        popupMenu.add(infoItem);

        JMenuItem zoomItem = new JMenuItem("Zoom a entidad");
        zoomItem.addActionListener(ev -> zoomToFeature(feature, layer));
        popupMenu.add(zoomItem);

        JMenuItem attrItem = new JMenuItem("Editar atributos");
        attrItem.addActionListener(ev -> OpenAttributeTableAction.openTable(layer));
        popupMenu.add(attrItem);

        JMenuItem propertiesItem = new JMenuItem("Opciones de capa");
        propertiesItem.addActionListener(ev -> LayerPropertiesDialog.open(layer));
        popupMenu.add(propertiesItem);

        JMenuItem clearItem = new JMenuItem("Limpiar seleccion");
        clearItem.addActionListener(ev -> clearSelectedFeature());
        popupMenu.add(clearItem);

        popupMenu.show(this, e.getX(), e.getY());
    }

    private void showPinPopup(MouseEvent e, PinMarker pin) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem showPinItem = new JMenuItem("Ver coordenadas de P" + pin.getId());
        showPinItem.addActionListener(ev -> showPinDialog(pin));
        popupMenu.add(showPinItem);

        JMenuItem copyPinProjectItem = new JMenuItem("Copiar coordenada de P" + pin.getId() + " (X/Y)");
        copyPinProjectItem.addActionListener(ev -> {
            String text = formatNumber(pin.getX()) + ", " + formatNumber(pin.getY());
            copyToClipboard(text);
            showCopiedMessage("Coordenada de P" + pin.getId() + " copiada.");
        });
        popupMenu.add(copyPinProjectItem);

        JMenuItem copyPinLatLonItem = new JMenuItem("Copiar Lat/Long de P" + pin.getId());
        copyPinLatLonItem.addActionListener(ev -> copyPinLatLon(pin, false));
        popupMenu.add(copyPinLatLonItem);

        JMenuItem copyPinDmsItem = new JMenuItem("Copiar Lat/Long DMS de P" + pin.getId());
        copyPinDmsItem.addActionListener(ev -> copyPinLatLon(pin, true));
        popupMenu.add(copyPinDmsItem);

        JMenuItem removePinItem = new JMenuItem("Borrar P" + pin.getId());
        removePinItem.addActionListener(ev -> {
            removePin(pin);
            showCopiedMessage("Pin P" + pin.getId() + " eliminado.");
        });
        popupMenu.add(removePinItem);

        if (!pins.isEmpty()) {
            popupMenu.addSeparator();

            JMenuItem convertPinsItem = new JMenuItem("Convertir pines en capa");
            convertPinsItem.addActionListener(ev -> convertPinsToLayer());
            popupMenu.add(convertPinsItem);

            JMenuItem clearPinsItem = new JMenuItem("Borrar todos los pines");
            clearPinsItem.addActionListener(ev -> {
                clearAllPins();
                showCopiedMessage("Todos los pines fueron eliminados.");
            });
            popupMenu.add(clearPinsItem);
        }

        popupMenu.show(this, e.getX(), e.getY());
    }

    private PinMarker addPin(double x, double y) {
        PinMarker pin = new PinMarker(nextPinId++, x, y);
        pins.add(pin);
        activePin = pin;
        repaint();
        return pin;
    }

    private void removePin(PinMarker pin) {
        pins.remove(pin);
        if (activePin == pin) {
            activePin = null;
        }
        repaint();
    }

    private void clearAllPins() {
        pins.clear();
        activePin = null;
        repaint();
    }

    private void convertPinsToLayer() {
        if (pins.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay pines para convertir.");
            return;
        }

        try {
            String projectCRS = (CatgisDesktopApp.currentProject != null &&
                    CatgisDesktopApp.currentProject.getProjectCRS() != null &&
                    !CatgisDesktopApp.currentProject.getProjectCRS().isBlank())
                    ? CatgisDesktopApp.currentProject.getProjectCRS()
                    : "EPSG:4326";

            ShapefileData data = PinLayerBuilder.buildFromPins(pins, projectCRS);

            String layerName = "Pines_" + System.currentTimeMillis();
            Layer layer = new Layer(layerName, layerName, "VECTOR");
            layer.setVisible(true);
            layer.setSourceName(data.getSourceName());
            layer.setFeatureCount(data.getFeatureCount());
            layer.setSourceCRS(projectCRS);
            layer.setLabelsVisible(true);
            layer.setLabelField("id");

            if (CatgisDesktopApp.currentProject == null) {
                CatgisDesktopApp.currentProject = new Project("Proyecto actual");
            }

            CatgisDesktopApp.currentProject.addLayer(layer);
            CatgisDesktopApp.markProjectDirty();
            CatgisDesktopApp.layersPanel.addLayer(layer);
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
            CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
            CatgisDesktopApp.mapPanel.repaint();

            JOptionPane.showMessageDialog(this, "Pines convertidos a capa correctamente.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al convertir pines en capa: " + ex.getMessage());
        }
    }

    private PinMarker findPinAtScreen(int mouseX, int mouseY) {
        for (int i = pins.size() - 1; i >= 0; i--) {
            PinMarker pin = pins.get(i);
            int pinScreenX = worldToScreenX(pin.getX());
            int pinScreenY = worldToScreenY(pin.getY());

            int dx = mouseX - pinScreenX;
            int dy = mouseY - pinScreenY;

            int tolerance = 10;
            if ((dx * dx + dy * dy) <= (tolerance * tolerance)) {
                return pin;
            }
        }
        return null;
    }

    private void showPinDialog(PinMarker pin) {
        if (pin == null) {
            JOptionPane.showMessageDialog(this, "No hay pin seleccionado.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Coordenadas del pin P").append(pin.getId()).append("\n\n");

        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        if (projectCRS != null && !projectCRS.isBlank()) {
            sb.append("CRS proyecto: ").append(projectCRS).append("\n");
        }

        sb.append("X: ").append(formatNumber(pin.getX())).append("\n");
        sb.append("Y: ").append(formatNumber(pin.getY())).append("\n");

        double[] geographic = transformPoint(pin.getX(), pin.getY(), projectCRS, "EPSG:4326");
        if (geographic != null) {
            sb.append("\nEPSG:4326\n");
            sb.append("Lon: ").append(formatNumber(geographic[0])).append("\n");
            sb.append("Lat: ").append(formatNumber(geographic[1])).append("\n");
            sb.append("Lon DMS: ").append(toDms(geographic[0], false)).append("\n");
            sb.append("Lat DMS: ").append(toDms(geographic[1], true)).append("\n");
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Pin P" + pin.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void copyPinLatLon(PinMarker pin, boolean dms) {
        if (pin == null) {
            JOptionPane.showMessageDialog(this, "No hay pin seleccionado.");
            return;
        }

        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        double[] geographic = transformPoint(pin.getX(), pin.getY(), projectCRS, "EPSG:4326");

        if (geographic == null) {
            JOptionPane.showMessageDialog(this, "No se pudo transformar el pin a EPSG:4326.");
            return;
        }

        String text;
        if (dms) {
            text = toDms(geographic[0], false) + " , " + toDms(geographic[1], true);
            showCopiedMessage("Lat/Long DMS de P" + pin.getId() + " copiada.");
        } else {
            text = formatNumber(geographic[0]) + ", " + formatNumber(geographic[1]);
            showCopiedMessage("Lat/Long de P" + pin.getId() + " copiada.");
        }

        copyToClipboard(text);
    }

    private void updateStatusCoordinates(int screenX, int screenY) {
        if (CatgisDesktopApp.statusBar == null) {
            return;
        }

        double worldX = screenToWorldX(screenX);
        double worldY = screenToWorldY(screenY);

        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        String projectText = "Proyecto: X: " + formatNumber(worldX) + "   Y: " + formatNumber(worldY);

        if (projectCRS != null && !projectCRS.isBlank()) {
            projectText += "   [" + projectCRS + "]";
        }

        CatgisDesktopApp.statusBar.setProjectCoordinates(projectText);

        String geographicText = "Lat/Long: Lon: -   Lat: -";
        String dmsText = "DMS: Lon: -   Lat: -";

        double[] geographic = transformPoint(worldX, worldY, projectCRS, "EPSG:4326");

        if (geographic != null) {
            geographicText = "Lat/Long: Lon: " + formatNumber(geographic[0])
                    + "   Lat: " + formatNumber(geographic[1])
                    + "   [EPSG:4326]";

            dmsText = "DMS: Lon: " + toDms(geographic[0], false)
                    + "   Lat: " + toDms(geographic[1], true);
        }

        CatgisDesktopApp.statusBar.setGeographicCoordinates(geographicText);
        CatgisDesktopApp.statusBar.setGeographicDms(dmsText);
    }

    private void showCoordinateDialog(int screenX, int screenY) {
        double worldX = screenToWorldX(screenX);
        double worldY = screenToWorldY(screenY);

        StringBuilder sb = new StringBuilder();
        sb.append("Coordenadas del punto\n\n");

        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        if (projectCRS != null && !projectCRS.isBlank()) {
            sb.append("CRS proyecto: ").append(projectCRS).append("\n");
        }

        sb.append("X: ").append(formatNumber(worldX)).append("\n");
        sb.append("Y: ").append(formatNumber(worldY)).append("\n");

        double[] geographic = transformPoint(worldX, worldY, projectCRS, "EPSG:4326");
        if (geographic != null) {
            sb.append("\nEPSG:4326\n");
            sb.append("Lon: ").append(formatNumber(geographic[0])).append("\n");
            sb.append("Lat: ").append(formatNumber(geographic[1])).append("\n");
            sb.append("Lon DMS: ").append(toDms(geographic[0], false)).append("\n");
            sb.append("Lat DMS: ").append(toDms(geographic[1], true)).append("\n");
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Visor de coordenadas", JOptionPane.INFORMATION_MESSAGE);
    }

    private String toDms(double value, boolean latitude) {
        String hemi;
        if (latitude) {
            hemi = value >= 0 ? "N" : "S";
        } else {
            hemi = value >= 0 ? "E" : "O";
        }

        double abs = Math.abs(value);
        int degrees = (int) abs;
        double minFloat = (abs - degrees) * 60.0;
        int minutes = (int) minFloat;
        double secFloat = (minFloat - minutes) * 60.0;

        return String.format(Locale.US, "%d° %d' %.2f\" %s", degrees, minutes, secFloat, hemi);
    }

    private String formatNumber(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    private void showCopiedMessage(String message) {
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(message);
        }
    }

    private void fitToAllLayers() {
        Envelope global = getGlobalEnvelope();

        if (global == null || global.isNull()) {
            repaint();
            return;
        }

        fitToEnvelope(global);
    }

    public void zoomToAllLayers() {
        SwingUtilities.invokeLater(() -> {
            rememberCurrentView();
            Envelope global = getGlobalEnvelope();

            if (global == null || global.isNull()) {
                JOptionPane.showMessageDialog(this, "No hay capas cargadas para calcular la extensión.");
                return;
            }

            fitToEnvelope(global);
            rememberCurrentView();
            repaint();

            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("Zoom a todas las capas aplicado.");
            }
        });
    }

    private void fitToEnvelope(Envelope env) {
        if (env == null || env.isNull()) {
            return;
        }

        double width = env.getWidth();
        double height = env.getHeight();

        if (width <= 0) {
            width = 10;
        }
        if (height <= 0) {
            height = 10;
        }

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (panelWidth <= 0) {
            panelWidth = 800;
        }
        if (panelHeight <= 0) {
            panelHeight = 600;
        }

        double scaleX = (panelWidth * 0.85) / width;
        double scaleY = (panelHeight * 0.85) / height;
        zoomFactor = Math.min(scaleX, scaleY);

        if (zoomFactor <= 0 || Double.isInfinite(zoomFactor) || Double.isNaN(zoomFactor)) {
            zoomFactor = 1.0;
        }

        double extraWorldWidth = panelWidth / zoomFactor - width;
        double extraWorldHeight = panelHeight / zoomFactor - height;

        viewMinX = env.getMinX() - extraWorldWidth / 2.0;
        viewMinY = env.getMinY() - extraWorldHeight / 2.0;
    }

    private Envelope getLayerEnvelope(Layer layer, ShapefileData data) {
        if (data == null) {
            return null;
        }

        Envelope env = null;

        if (data.getFeatureCollection() != null) {
            try {
                env = data.getFeatureCollection().getBounds();
            } catch (Exception ignored) {
            }
        }

        if ((env == null || env.isNull()) && data.getEnvelope() != null) {
            env = new Envelope(data.getEnvelope());
        }

        if ((env == null || env.isNull()) && data.getFeatures() != null) {
            for (SimpleFeature feature : data.getFeatures()) {
                if (feature == null) {
                    continue;
                }

                Object geomObj = feature.getDefaultGeometry();
                if (geomObj instanceof Geometry) {
                    Geometry geometry = (Geometry) geomObj;
                    if (!geometry.isEmpty()) {
                        if (env == null) {
                            env = new Envelope(geometry.getEnvelopeInternal());
                        } else {
                            env.expandToInclude(geometry.getEnvelopeInternal());
                        }
                    }
                }
            }
        }

        return reprojectEnvelopeIfNeeded(layer, env);
    }

    private Envelope getGlobalEnvelope() {
        Envelope global = null;

        for (Map.Entry<Layer, LocalRasterData> entry : rasterLayers.entrySet()) {
            Envelope env = getRasterEnvelope(entry.getKey(), entry.getValue());
            if (env == null || env.isNull()) {
                continue;
            }
            if (global == null) {
                global = new Envelope(env);
            } else {
                global.expandToInclude(env);
            }
        }

        for (Map.Entry<Layer, ShapefileData> entry : shapefileLayers.entrySet()) {
            Layer layer = entry.getKey();
            ShapefileData data = entry.getValue();
            Envelope env = getLayerEnvelope(layer, data);

            if (env == null || env.isNull()) {
                continue;
            }

            if (global == null) {
                global = new Envelope(env);
            } else {
                global.expandToInclude(env);
            }
        }

        return global;
    }

    private void handleZoom(MouseWheelEvent e) {
        double factor = e.getWheelRotation() < 0 ? 1.2 : (1.0 / 1.2);
        rememberCurrentView();
        applyZoom(factor, e.getX(), e.getY());
        rememberCurrentView();
    }

    private void applyZoom(double factor, int anchorX, int anchorY) {
        if (zoomFactor <= 0) {
            zoomFactor = 1.0;
        }

        double worldXBefore = screenToWorldX(anchorX);
        double worldYBefore = screenToWorldY(anchorY);

        zoomFactor *= factor;

        if (zoomFactor < 0.000001) {
            zoomFactor = 0.000001;
        }

        double worldXAfter = screenToWorldX(anchorX);
        double worldYAfter = screenToWorldY(anchorY);

        viewMinX += (worldXBefore - worldXAfter);
        viewMinY += (worldYBefore - worldYAfter);

        repaint();
    }

    private void identifyFeature(int screenX, int screenY) {
        List<IdentifyResultItem> hits = collectIdentifyResults(screenX, screenY);
        if (activeVectorEditingLayer != null) {
            List<IdentifyResultItem> filteredHits = new ArrayList<>();
            for (IdentifyResultItem item : hits) {
                if (item != null && item.getLayer() == activeVectorEditingLayer) {
                    filteredHits.add(item);
                }
            }
            hits = filteredHits;
        }

        if (hits.isEmpty()) {
            clearSelectedFeature();
            showCopiedMessage("No se identificó ninguna entidad.");
            JOptionPane.showMessageDialog(this, "No se identificó ninguna entidad.");
            repaint();
            return;
        }

        if (hits.size() == 1) {
            IdentifyResultItem hit = hits.get(0);
            highlightIdentifiedFeature(hit.getLayer(), hit.getFeature());
            showCopiedMessage("1 entidad identificada en capa: " + (hit.getLayer() != null ? hit.getLayer().getName() : "-"));
            showFeatureInfo(hit.getFeature(), hit.getLayer());
            return;
        }

        highlightIdentifiedFeature(hits.get(0).getLayer(), hits.get(0).getFeature());
        showCopiedMessage(hits.size() + " entidades identificadas. Elegí una en la ventana de resultados.");
        IdentifyResultsDialog.open(this, hits);
    }

    private void selectFeatureForEditing(int screenX, int screenY, boolean additiveSelection) {
        List<IdentifyResultItem> hits = collectSelectableResults(screenX, screenY);

        if (hits.isEmpty()) {
            if (!additiveSelection) {
                clearSelectedFeature();
                showCopiedMessage("No se seleccionó ninguna entidad.");
            }
            repaint();
            return;
        }

        IdentifyResultItem hit = hits.get(0);
        Layer targetLayer = hit.getLayer();
        String featureId = hit.getFeature() != null ? hit.getFeature().getID() : null;
        if (targetLayer == null || featureId == null) {
            return;
        }

        List<String> selectionIds = additiveSelection
                ? mergeSelectionIds(targetLayer, getSelectedFeatureIdsForLayer(targetLayer), List.of(featureId), true)
                : List.of(featureId);

        String message = additiveSelection
                ? (selectionIds.size() == 1
                ? "1 entidad seleccionada."
                : selectionIds.size() + " entidades seleccionadas.")
                : "Entidad seleccionada para edición: " + targetLayer.getName();
        applyFeatureSelection(targetLayer, selectionIds, !additiveSelection, true, true, message);
    }

    private void selectFeatureForEditing(Rectangle selectionBounds, boolean additiveSelection) {
        List<IdentifyResultItem> hits = collectSelectableResults(selectionBounds);

        if (hits.isEmpty()) {
            if (!additiveSelection) {
                clearSelectedFeature();
                showCopiedMessage("No se encontró ninguna entidad dentro del rectángulo.");
            }
            repaint();
            return;
        }

        Layer targetLayer = hits.get(0).getLayer();
        List<String> selectionIds = new ArrayList<>();
        for (IdentifyResultItem item : hits) {
            if (item != null && item.getLayer() == targetLayer && item.getFeature() != null && item.getFeature().getID() != null) {
                selectionIds.add(item.getFeature().getID());
            }
        }
        if (additiveSelection) {
            selectionIds = mergeSelectionIds(targetLayer, getSelectedFeatureIdsForLayer(targetLayer), selectionIds, false);
        }

        String message = selectionIds.size() == 1
                ? "Entidad seleccionada con ventana de captura."
                : selectionIds.size() + " entidades seleccionadas con ventana de captura.";
        applyFeatureSelection(targetLayer, selectionIds, !additiveSelection && selectionIds.size() == 1, true, true, message);
    }

    private List<IdentifyResultItem> collectIdentifyResults(int screenX, int screenY) {
        List<IdentifyResultItem> hits = new ArrayList<>();

        double tolerancePixels = 6.0;
        double toleranceWorld = tolerancePixels / zoomFactor;

        double worldX = screenToWorldX(screenX);
        double worldY = screenToWorldY(screenY);

        Point clickPoint = new org.locationtech.jts.geom.GeometryFactory()
                .createPoint(new Coordinate(worldX, worldY));

        for (Layer layer : getHitTestLayers(false)) {

            if (!layer.isVisible()) {
                continue;
            }

            ShapefileData data = shapefileLayers.get(layer);
            if (data == null || data.getFeatureCollection() == null) {
                continue;
            }

            try (FeatureIterator<SimpleFeature> iterator = data.getFeatureCollection().features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    Object geomObj = feature.getDefaultGeometry();

                    if (!(geomObj instanceof Geometry)) {
                        continue;
                    }

                    Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
                    if (geometry == null) {
                        continue;
                    }

                    boolean hit;
                    if (geometry instanceof Point || geometry instanceof MultiPoint) {
                        hit = geometry.isWithinDistance(clickPoint, toleranceWorld);
                    } else {
                        hit = geometry.buffer(toleranceWorld).contains(clickPoint);
                    }

                    if (hit) {
                        hits.add(new IdentifyResultItem(layer, feature, geometry));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return hits;
    }

    private List<IdentifyResultItem> collectSelectableResults(int screenX, int screenY) {
        List<IdentifyResultItem> hits = new ArrayList<>();

        double tolerancePixels = 6.0;
        double toleranceWorld = tolerancePixels / zoomFactor;

        double worldX = screenToWorldX(screenX);
        double worldY = screenToWorldY(screenY);

        Point clickPoint = selectionGeometryFactory.createPoint(new Coordinate(worldX, worldY));

        for (Layer layer : getHitTestLayers(true)) {
            if (!layer.isVisible()) {
                continue;
            }

            ShapefileData data = shapefileLayers.get(layer);
            if (data == null || data.getFeatureCollection() == null) {
                continue;
            }

            try (FeatureIterator<SimpleFeature> iterator = data.getFeatureCollection().features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    Object geomObj = feature.getDefaultGeometry();

                    if (!(geomObj instanceof Geometry)) {
                        continue;
                    }

                    Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
                    if (geometry == null) {
                        continue;
                    }

                    boolean hit;
                    if (geometry instanceof Point || geometry instanceof MultiPoint) {
                        hit = geometry.isWithinDistance(clickPoint, toleranceWorld);
                    } else {
                        hit = geometry.buffer(toleranceWorld).contains(clickPoint);
                    }

                    if (hit) {
                        hits.add(new IdentifyResultItem(layer, feature, geometry));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        sortSelectableHits(hits, clickPoint);
        return hits;
    }

    private List<IdentifyResultItem> collectSelectableResults(Rectangle selectionBounds) {
        List<IdentifyResultItem> hits = new ArrayList<>();
        if (selectionBounds == null || selectionBounds.width <= 0 || selectionBounds.height <= 0) {
            return hits;
        }

        double minWorldX = screenToWorldX(selectionBounds.x);
        double maxWorldX = screenToWorldX(selectionBounds.x + selectionBounds.width);
        double maxWorldY = screenToWorldY(selectionBounds.y);
        double minWorldY = screenToWorldY(selectionBounds.y + selectionBounds.height);
        Envelope envelope = new Envelope(
                Math.min(minWorldX, maxWorldX),
                Math.max(minWorldX, maxWorldX),
                Math.min(minWorldY, maxWorldY),
                Math.max(minWorldY, maxWorldY)
        );
        Geometry selectionArea = selectionGeometryFactory.toGeometry(envelope);

        for (Layer layer : getHitTestLayers(true)) {
            if (!layer.isVisible()) {
                continue;
            }

            ShapefileData data = shapefileLayers.get(layer);
            if (data == null || data.getFeatureCollection() == null) {
                continue;
            }

            try (FeatureIterator<SimpleFeature> iterator = data.getFeatureCollection().features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    Object geomObj = feature.getDefaultGeometry();

                    if (!(geomObj instanceof Geometry)) {
                        continue;
                    }

                    Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
                    if (geometry == null || geometry.isEmpty()) {
                        continue;
                    }

                    if (geometry.intersects(selectionArea) || selectionArea.contains(geometry)) {
                        hits.add(new IdentifyResultItem(layer, feature, geometry));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        sortSelectableHits(hits, selectionArea.getCentroid());
        return hits;
    }

    private void sortSelectableHits(List<IdentifyResultItem> hits, Geometry referenceGeometry) {
        if (hits == null || hits.size() < 2 || referenceGeometry == null) {
            return;
        }
        hits.sort((left, right) -> Double.compare(
                selectableHitDistance(left, referenceGeometry),
                selectableHitDistance(right, referenceGeometry)
        ));
    }

    private double selectableHitDistance(IdentifyResultItem item, Geometry referenceGeometry) {
        if (item == null || item.getGeometry() == null || referenceGeometry == null) {
            return Double.MAX_VALUE;
        }

        double distance = item.getGeometry().distance(referenceGeometry);
        if (selectedLayer != null
                && selectedFeature != null
                && item.getLayer() == selectedLayer
                && sameFeatureId(item.getFeature(), selectedFeature)) {
            distance += 0.000001d;
        }
        return distance;
    }

    private boolean isHitOnCurrentSelection(int screenX, int screenY) {
        if (selectedLayer == null || !hasFeatureSelection()) {
            return false;
        }
        List<String> selectedIds = getSelectedFeatureIdsForLayer(selectedLayer);
        if (selectedIds.isEmpty()) {
            return false;
        }

        List<IdentifyResultItem> hits = collectSelectableResults(screenX, screenY);
        for (IdentifyResultItem hit : hits) {
            if (hit != null
                    && hit.getLayer() == selectedLayer
                    && hit.getFeature() != null
                    && selectedIds.contains(hit.getFeature().getID())) {
                return true;
            }
        }
        return false;
    }

    private List<Layer> getHitTestLayers(boolean preferEditingLayer) {
        List<Layer> orderedLayers = new ArrayList<>();
        if (preferEditingLayer
                && activeVectorEditingLayer != null
                && shapefileLayers.containsKey(activeVectorEditingLayer)
                && activeVectorEditingLayer.isVisible()) {
            orderedLayers.add(activeVectorEditingLayer);
            return orderedLayers;
        }

        List<Layer> renderOrder = getRenderOrderLayers();
        for (int i = renderOrder.size() - 1; i >= 0; i--) {
            Layer layer = renderOrder.get(i);
            if (shapefileLayers.containsKey(layer)) {
                orderedLayers.add(layer);
            }
        }
        return orderedLayers;
    }

    public void highlightIdentifiedFeature(Layer layer, SimpleFeature feature) {
        selectedLayer = layer;
        selectedFeature = feature;
        if (layer != null && feature != null) {
            tableSelectionIds.clear();
            tableSelectionIds.put(layer, new ArrayList<>(List.of(feature.getID())));
            OpenAttributeTableAction.syncSelectionFromMap(layer, List.of(feature.getID()));
            startSelectionFlash(layer, feature);
        }
        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        repaint();
    }

    private void clearSelectedFeatureInternal(boolean syncOpenTables) {
        selectionFlashGeometry = null;
        selectionFlashTimer.stop();
        tableSelectionIds.clear();
        if (syncOpenTables) {
            OpenAttributeTableAction.clearSelectionInOpenTables();
        }
        selectedLayer = activeVectorEditingLayer;
        selectedFeature = null;
        featureEditMode = false;
        featureEditOperation = EDIT_OP_MOVE_VERTEX;
        featureEditSketchCoordinates.clear();
        featureEditOriginalGeometry = null;
        featureEditDirty = false;
        activeEditVertexIndex = -1;
        movingSelectedFeatures = false;
        moveSelectionLastProjectX = Double.NaN;
        moveSelectionLastProjectY = Double.NaN;
        editUndoStack.clear();
        editRedoStack.clear();
        refreshEditingUi();
    }

    public void clearSelectedFeature() {
        if (!confirmPendingFeatureEdit("limpiar la selección")) {
            return;
        }
        clearSelectedFeatureInternal(true);
    }

    public void syncSelectionFromAttributeTable(Layer layer, List<String> featureIds) {
        if (layer == null) {
            return;
        }

        tableSelectionIds.clear();
        if (featureIds == null || featureIds.isEmpty()) {
            if (!featureEditMode && layer == selectedLayer) {
                selectedFeature = null;
            }
        } else {
            tableSelectionIds.put(layer, new ArrayList<>(featureIds));
            if (!featureEditMode) {
                selectedLayer = layer;
                ShapefileData data = getShapefileData(layer);
                selectedFeature = data != null ? findFeatureById(data.getFeatures(), featureIds.get(0)) : null;
                if (selectedFeature != null) {
                    startSelectionFlash(layer, selectedFeature);
                }
            }
        }

        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        repaint();
    }

    public void zoomToSelectedFeature() {
        if (selectedFeature != null && selectedLayer != null) {
            zoomToFeature(selectedFeature, selectedLayer);
        }
    }

    public void zoomToFeatureSelection(Layer layer, List<String> featureIds) {
        if (layer == null || featureIds == null || featureIds.isEmpty()) {
            return;
        }

        ShapefileData data = getShapefileData(layer);
        if (data == null || data.getFeatures() == null || data.getFeatures().isEmpty()) {
            return;
        }

        Envelope selectionEnvelope = null;
        for (String featureId : featureIds) {
            SimpleFeature feature = findFeatureById(data.getFeatures(), featureId);
            if (feature == null) {
                continue;
            }
            Object geomObj = feature.getDefaultGeometry();
            if (!(geomObj instanceof Geometry)) {
                continue;
            }
            Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }

            if (selectionEnvelope == null) {
                selectionEnvelope = new Envelope(geometry.getEnvelopeInternal());
            } else {
                selectionEnvelope.expandToInclude(geometry.getEnvelopeInternal());
            }
        }

        if (selectionEnvelope == null || selectionEnvelope.isNull()) {
            return;
        }

        double expandX = Math.max(selectionEnvelope.getWidth() * 0.12, 10.0 / Math.max(zoomFactor, 0.000001));
        double expandY = Math.max(selectionEnvelope.getHeight() * 0.12, 10.0 / Math.max(zoomFactor, 0.000001));
        selectionEnvelope.expandBy(expandX, expandY);
        fitToEnvelope(selectionEnvelope);
        repaint();
    }

    public void copySelectedFeature() {
        copySelectedFeatures();
    }

    public void copySelectedFeatures() {
        if (selectedLayer == null) {
            return;
        }

        ShapefileData data = getShapefileData(selectedLayer);
        List<String> selectedIds = getSelectedFeatureIdsForLayer(selectedLayer);
        if (data == null || selectedIds.isEmpty()) {
            return;
        }

        copiedFeatures.clear();
        for (String featureId : selectedIds) {
            SimpleFeature feature = findFeatureById(data.getFeatures(), featureId);
            if (feature != null) {
                copiedFeatures.add(cloneFeature(feature, extractFeatureGeometryCopy(feature), feature.getID()));
            }
        }
        copiedFeature = copiedFeatures.isEmpty() ? null : copiedFeatures.get(0);
        showCopiedMessage(copiedFeatures.size() == 1 ? "Entidad copiada." : copiedFeatures.size() + " entidades copiadas.");
        refreshEditingUi();
    }

    public boolean pasteCopiedFeature() {
        return pasteCopiedFeatures();
    }

    public boolean pasteCopiedFeatures() {
        Layer targetLayer = getEditingLayerRef();
        if ((!hasCopiedFeature()) || targetLayer == null) {
            return false;
        }

        ShapefileData targetData = getShapefileData(targetLayer);
        if (targetData == null) {
            return false;
        }

        List<SimpleFeature> sources = copiedFeatures.isEmpty() && copiedFeature != null
                ? List.of(copiedFeature)
                : new ArrayList<>(copiedFeatures);
        List<SimpleFeature> features = new ArrayList<>(targetData.getFeatures());
        List<String> pastedIds = new ArrayList<>();
        for (SimpleFeature sourceFeature : sources) {
            SimpleFeature pasted = buildPastedFeature(sourceFeature, targetLayer, features);
            if (pasted == null) {
                continue;
            }
            features.add(pasted);
            pastedIds.add(pasted.getID());
        }

        if (pastedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por ahora solo se puede pegar en una capa compatible con la estructura de los elementos copiados.",
                    "Pegar entidades",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        pushUndoSnapshot(targetLayer, null);
        replaceLayerFeatures(targetLayer, features, pastedIds.get(0), pastedIds.size() == 1, null);
        applyFeatureSelection(targetLayer, pastedIds, pastedIds.size() == 1, true, false,
                pastedIds.size() == 1 ? "Entidad pegada." : pastedIds.size() + " entidades pegadas.");
        return true;
    }

    public boolean deleteSelectedFeature() {
        return deleteSelectedFeatures();
    }

    public boolean deleteSelectedFeatures() {
        if (selectedLayer == null) {
            return false;
        }

        ShapefileData data = getShapefileData(selectedLayer);
        List<String> selectedIds = getSelectedFeatureIdsForLayer(selectedLayer);
        if (data == null || selectedIds.isEmpty()) {
            return false;
        }

        pushUndoSnapshotForSelectedLayer();

        List<SimpleFeature> features = new ArrayList<>();
        for (SimpleFeature feature : data.getFeatures()) {
            if (feature != null && !selectedIds.contains(feature.getID())) {
                features.add(feature);
            }
        }

        replaceLayerFeatures(selectedLayer, features, null, false,
                selectedIds.size() == 1 ? "Entidad eliminada." : selectedIds.size() + " entidades eliminadas.");
        return true;
    }

    public void undoFeatureEdit() {
        if (!canUndoFeatureEdit()) {
            return;
        }
        Layer layer = getEditingLayerRef();
        if (layer == null) {
            return;
        }

        editRedoStack.push(captureLayerSnapshot(layer, selectedFeature != null ? selectedFeature.getID() : null));
        restoreLayerSnapshot(editUndoStack.pop(), "Deshacer aplicado.");
    }

    public void redoFeatureEdit() {
        if (!canRedoFeatureEdit()) {
            return;
        }
        Layer layer = getEditingLayerRef();
        if (layer == null) {
            return;
        }

        editUndoStack.push(captureLayerSnapshot(layer, selectedFeature != null ? selectedFeature.getID() : null));
        restoreLayerSnapshot(editRedoStack.pop(), "Rehacer aplicado.");
    }

    public void zoomToFeature(SimpleFeature feature, Layer layer) {
        if (feature == null) {
            return;
        }

        Object geomObj = feature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return;
        }

        Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        Envelope env = geometry.getEnvelopeInternal();
        if (env == null || env.isNull()) {
            return;
        }

        double expandX = Math.max(env.getWidth() * 0.15, 10.0 / Math.max(zoomFactor, 0.000001));
        double expandY = Math.max(env.getHeight() * 0.15, 10.0 / Math.max(zoomFactor, 0.000001));
        env.expandBy(expandX, expandY);

        fitToEnvelope(env);
        highlightIdentifiedFeature(layer, feature);
    }

    private void showFeatureInfo(SimpleFeature feature, Layer layer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(layer.getName()).append("\n\n");

        feature.getProperties().forEach(property -> {
            String name = property.getName().toString();
            Object value = property.getValue();
            if (!"the_geom".equalsIgnoreCase(name)) {
                sb.append(name).append(": ").append(value).append("\n");
            }
        });

        JOptionPane.showMessageDialog(this, sb.toString(), "Identificar entidad", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (shapefileLayers.isEmpty() && rasterLayers.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString(openedFileText, 20, 30);
        }

        for (Layer layer : getRenderOrderLayers()) {
            if (layer == null || !layer.isVisible()) {
                continue;
            }

            LocalRasterData rasterData = rasterLayers.get(layer);
            if (rasterData != null) {
                drawRasterLayer(g2, layer, rasterData);
                continue;
            }

            ShapefileData shapeData = shapefileLayers.get(layer);
            if (shapeData != null) {
                drawLayer(g2, layer, shapeData);
                drawLabels(g2, layer, shapeData);
            }
        }

        drawAttributeTableSelections(g2);

        if (selectedLayer != null && selectedFeature != null) {
            drawSelectedFeature(g2, selectedFeature, selectedLayer);
            if (featureEditMode) {
                drawEditableVertices(g2, selectedFeature, selectedLayer);
            }
        }

        drawSelectionFlash(g2);
        drawPins(g2);
        drawCurrentSketch(g2);
        drawCurrentMeasurement(g2);
        drawFeatureEditSketch(g2);
        drawSelectionBox(g2);

        g2.dispose();
    }

    private void drawSelectionBox(Graphics2D g2) {
        if (!selectionBoxActive || !selectionBoxDragging) {
            return;
        }

        Rectangle bounds = getSelectionBoxBounds();
        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setColor(new Color(59, 130, 246, 38));
            copy.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            copy.setColor(new Color(37, 99, 235, 210));
            copy.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 4f}, 0f));
            copy.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        } finally {
            copy.dispose();
        }
    }

    private List<Layer> getRenderOrderLayers() {
        List<Layer> ordered = new ArrayList<>();

        if (CatgisDesktopApp.currentProject != null && CatgisDesktopApp.currentProject.getLayers() != null) {
            for (Layer layer : CatgisDesktopApp.currentProject.getLayers()) {
                if (layer != null
                        && (shapefileLayers.containsKey(layer) || rasterLayers.containsKey(layer))
                        && !ordered.contains(layer)) {
                    ordered.add(layer);
                }
            }
        }

        for (Layer layer : rasterLayers.keySet()) {
            if (layer != null && !ordered.contains(layer)) {
                ordered.add(layer);
            }
        }
        for (Layer layer : shapefileLayers.keySet()) {
            if (layer != null && !ordered.contains(layer)) {
                ordered.add(layer);
            }
        }
        return ordered;
    }

    private void drawSelectionFlash(Graphics2D g2) {
        if (selectionFlashGeometry == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - selectionFlashStartedAt;
        if (elapsed >= SELECTION_FLASH_DURATION_MS) {
            selectionFlashGeometry = null;
            selectionFlashTimer.stop();
            return;
        }

        Coordinate focus = resolveSelectionFlashCoordinate(selectionFlashGeometry);
        if (focus == null) {
            return;
        }

        float progress = elapsed / (float) SELECTION_FLASH_DURATION_MS;
        float baseAlpha = 1f - progress;
        int x = worldToScreenX(focus.x);
        int y = worldToScreenY(focus.y);

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.12f, baseAlpha * 0.42f)));
            copy.setColor(new Color(255, 215, 0));
            int glow = Math.round(10f + (progress * 30f));
            copy.fillOval(x - (glow / 2), y - (glow / 2), glow, glow);

            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.08f, baseAlpha * 0.75f)));
            copy.setColor(new Color(255, 225, 90));
            copy.setStroke(new BasicStroke(2.4f));
            int ringA = Math.round(18f + (progress * 34f));
            int ringB = Math.round(8f + (progress * 18f));
            copy.drawOval(x - (ringA / 2), y - (ringA / 2), ringA, ringA);
            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.04f, baseAlpha * 0.5f)));
            copy.drawOval(x - (ringB / 2), y - (ringB / 2), ringB, ringB);
        } finally {
            copy.dispose();
        }
    }

    private Coordinate resolveSelectionFlashCoordinate(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }

        try {
            Point focusPoint = geometry.getInteriorPoint();
            if (focusPoint != null && !focusPoint.isEmpty()) {
                return focusPoint.getCoordinate();
            }
        } catch (Exception ignored) {
        }

        try {
            Point centroid = geometry.getCentroid();
            if (centroid != null && !centroid.isEmpty()) {
                return centroid.getCoordinate();
            }
        } catch (Exception ignored) {
        }

        Envelope envelope = geometry.getEnvelopeInternal();
        return envelope != null ? envelope.centre() : null;
    }

    private void startSelectionFlash(Layer layer, SimpleFeature feature) {
        if (layer == null || feature == null) {
            return;
        }

        Object geomObj = feature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return;
        }

        Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        selectionFlashGeometry = geometry;
        selectionFlashStartedAt = System.currentTimeMillis();
        if (!selectionFlashTimer.isRunning()) {
            selectionFlashTimer.start();
        }
        repaint();
    }

    private void drawRasterLayer(Graphics2D g2, Layer layer, LocalRasterData data) {
        if (data == null || data.getImage() == null) {
            return;
        }

        Envelope env = getRasterEnvelope(layer, data);
        if (env == null || env.isNull()) {
            return;
        }

        int x1 = worldToScreenX(env.getMinX());
        int y1 = worldToScreenY(env.getMaxY());
        int x2 = worldToScreenX(env.getMaxX());
        int y2 = worldToScreenY(env.getMinY());

        int drawX = Math.min(x1, x2);
        int drawY = Math.min(y1, y2);
        int drawW = Math.abs(x2 - x1);
        int drawH = Math.abs(y2 - y1);

        if (drawW <= 1 || drawH <= 1) {
            return;
        }

        RasterStyle style = getOrCreateRasterStyle(layer, Math.max(1, data.getBandCount()));
        BufferedImage display = getCachedDisplayImage(layer, data, style);
        if (display == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, style.opacity));
            copy.drawImage(display, drawX, drawY, drawW, drawH, null);
        } finally {
            copy.dispose();
        }
    }

    private BufferedImage buildDisplayImage(LocalRasterData data, RasterStyle style) {
        BufferedImage src = data.getImage();
        if (src == null) {
            return null;
        }

        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int bands = Math.min(Math.max(1, data.getBandCount()), Math.max(1, src.getRaster().getNumBands()));
        int[] mins = new int[bands];
        int[] maxs = new int[bands];
        for (int i = 0; i < bands; i++) {
            mins[i] = Integer.MAX_VALUE;
            maxs[i] = Integer.MIN_VALUE;
        }

        if (style.autoContrast) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int b = 0; b < bands; b++) {
                        int sample = src.getRaster().getSample(x, y, b);
                        if (sample < mins[b]) mins[b] = sample;
                        if (sample > maxs[b]) maxs[b] = sample;
                    }
                }
            }
            for (int b = 0; b < bands; b++) {
                if (mins[b] == Integer.MAX_VALUE) mins[b] = 0;
                if (maxs[b] <= mins[b]) maxs[b] = mins[b] + 1;
            }
        } else {
            for (int b = 0; b < bands; b++) { mins[b] = 0; maxs[b] = 255; }
        }

        int rb = Math.min(style.redBand, bands - 1);
        int gb = Math.min(style.greenBand, bands - 1);
        int bb = Math.min(style.blueBand, bands - 1);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r, g, b;
                if (bands == 1) {
                    int v = src.getRaster().getSample(x, y, 0);
                    int vv = scaleSample(v, mins[0], maxs[0], style.autoContrast);
                    r = g = b = vv;
                } else {
                    r = scaleSample(src.getRaster().getSample(x, y, rb), mins[rb], maxs[rb], style.autoContrast);
                    g = scaleSample(src.getRaster().getSample(x, y, gb), mins[gb], maxs[gb], style.autoContrast);
                    b = scaleSample(src.getRaster().getSample(x, y, bb), mins[bb], maxs[bb], style.autoContrast);
                }
                if (style.grayscale) {
                    int gray = (r + g + b) / 3;
                    r = g = b = gray;
                }
                int argb = (255 << 24) | (r << 16) | (g << 8) | b;
                out.setRGB(x, y, argb);
            }
        }
        return out;
    }

    private BufferedImage getCachedDisplayImage(Layer layer, LocalRasterData data, RasterStyle style) {
        CachedRasterDisplay cached = rasterDisplayCache.get(layer);
        if (cached != null && cached.matches(data, style)) {
            return cached.image;
        }

        BufferedImage display = buildDisplayImage(data, style);
        if (display == null) {
            rasterDisplayCache.remove(layer);
            return null;
        }

        rasterDisplayCache.put(layer, new CachedRasterDisplay(data, style, display));
        return display;
    }

    private void invalidateRasterDisplay(Layer layer) {
        if (layer != null) {
            rasterDisplayCache.remove(layer);
        }
    }

    private int scaleSample(int value, int min, int max, boolean auto) {
        if (!auto) {
            return Math.max(0, Math.min(255, value));
        }
        if (max <= min) {
            return 0;
        }
        double scaled = (value - min) * 255.0 / (max - min);
        return (int) Math.max(0, Math.min(255, Math.round(scaled)));
    }

    private Envelope getRasterEnvelope(Layer layer, LocalRasterData data) {
        if (data == null || data.getEnvelope() == null) {
            return null;
        }
        String sourceCode = data.getDisplayCRS();
        if (sourceCode == null || sourceCode.isBlank()) {
            sourceCode = layer != null ? layer.getSourceCRS() : "";
        }
        String targetCode = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        return reprojectEnvelopeIfNeeded(new Envelope(data.getEnvelope()), sourceCode, targetCode);
    }

    public void reloadRasterLayersForProjectCRS() {
        if (rasterLayers.isEmpty()) {
            resetView();
            repaint();
            return;
        }

        LinkedHashMap<Layer, LocalRasterData> reloaded = new LinkedHashMap<>();

        for (Map.Entry<Layer, LocalRasterData> entry : rasterLayers.entrySet()) {
            Layer layer = entry.getKey();
            if (layer == null || layer.getPath() == null || layer.getPath().isBlank()) {
                if (entry.getValue() != null) {
                    reloaded.put(layer, entry.getValue());
                }
                continue;
            }

            File rasterFile = new File(layer.getPath());
            if (!rasterFile.exists()) {
                if (entry.getValue() != null) {
                    reloaded.put(layer, entry.getValue());
                }
                continue;
            }

            try {
                reloaded.put(layer, loadRasterForCurrentProject(layer, rasterFile));
            } catch (Exception ex) {
                if (entry.getValue() != null) {
                    reloaded.put(layer, entry.getValue());
                }
            }
        }

        rasterLayers.clear();
        rasterLayers.putAll(reloaded);
        rasterDisplayCache.clear();
        resetView();
        repaint();
    }

    private LocalRasterData loadRasterForCurrentProject(Layer layer, File rasterFile) throws java.io.IOException {
        String mode = RasterImageLoader.MODE_PREVIEW;
        if (layer instanceof RasterLayer) {
            mode = ((RasterLayer) layer).getRasterMode();
        }

        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        String sourceCRS = layer != null ? layer.getSourceCRS() : "";

        if (RasterImageLoader.MODE_REAL.equalsIgnoreCase(mode)) {
            return RasterImageLoader.loadReal(rasterFile, projectCRS, sourceCRS);
        }
        if (RasterImageLoader.MODE_VIRTUAL.equalsIgnoreCase(mode)) {
            return RasterImageLoader.loadVirtual(rasterFile, projectCRS, sourceCRS);
        }
        return RasterImageLoader.loadPreview(rasterFile, projectCRS, sourceCRS);
    }

    private void moveRasterUp(Layer layer) {
        Layer[] keys = rasterLayers.keySet().toArray(new Layer[0]);
        for (int i = 1; i < keys.length; i++) {
            if (keys[i] == layer) {
                Layer previous = keys[i - 1];
                LocalRasterData currentData = rasterLayers.get(layer);
                LocalRasterData previousData = rasterLayers.get(previous);
                LinkedHashMap<Layer, LocalRasterData> reordered = new LinkedHashMap<>();
                for (int j = 0; j < keys.length; j++) {
                    if (j == i - 1) {
                        reordered.put(layer, currentData);
                        reordered.put(previous, previousData);
                        j++;
                    } else {
                        reordered.put(keys[j], rasterLayers.get(keys[j]));
                    }
                }
                rasterLayers.clear();
                rasterLayers.putAll(reordered);
                repaint();
                return;
            }
        }
    }

    private void moveRasterDown(Layer layer) {
        Layer[] keys = rasterLayers.keySet().toArray(new Layer[0]);
        for (int i = 0; i < keys.length - 1; i++) {
            if (keys[i] == layer) {
                Layer next = keys[i + 1];
                LocalRasterData currentData = rasterLayers.get(layer);
                LocalRasterData nextData = rasterLayers.get(next);
                LinkedHashMap<Layer, LocalRasterData> reordered = new LinkedHashMap<>();
                for (int j = 0; j < keys.length; j++) {
                    if (j == i) {
                        reordered.put(next, nextData);
                        reordered.put(layer, currentData);
                        j++;
                    } else {
                        reordered.put(keys[j], rasterLayers.get(keys[j]));
                    }
                }
                rasterLayers.clear();
                rasterLayers.putAll(reordered);
                repaint();
                return;
            }
        }
    }

    public static class RasterStyle {
        public float opacity = 1.0f;
        public boolean grayscale = false;
        public boolean autoContrast = true;
        public int redBand = 0;
        public int greenBand = 1;
        public int blueBand = 2;
    }

    private static class CachedRasterDisplay {
        private final LocalRasterData sourceData;
        private final boolean grayscale;
        private final boolean autoContrast;
        private final int redBand;
        private final int greenBand;
        private final int blueBand;
        private final BufferedImage image;

        private CachedRasterDisplay(LocalRasterData sourceData, RasterStyle style, BufferedImage image) {
            this.sourceData = sourceData;
            this.grayscale = style != null && style.grayscale;
            this.autoContrast = style == null || style.autoContrast;
            this.redBand = style != null ? style.redBand : 0;
            this.greenBand = style != null ? style.greenBand : 1;
            this.blueBand = style != null ? style.blueBand : 2;
            this.image = image;
        }

        private boolean matches(LocalRasterData data, RasterStyle style) {
            if (data != sourceData || style == null) {
                return false;
            }
            return grayscale == style.grayscale
                    && autoContrast == style.autoContrast
                    && redBand == style.redBand
                    && greenBand == style.greenBand
                    && blueBand == style.blueBand;
        }
    }

    private void drawPins(Graphics2D g2) {
        for (PinMarker pin : pins) {
            int x = worldToScreenX(pin.getX());
            int y = worldToScreenY(pin.getY());

            g2.setColor(Color.RED);
            g2.fillOval(x - 6, y - 6, 12, 12);

            g2.setColor(Color.WHITE);
            g2.fillOval(x - 3, y - 3, 6, 6);

            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x, y + 6, x, y + 16);

            String label = "P" + pin.getId();
            g2.setColor(Color.BLACK);
            g2.drawString(label, x + 8, y - 8);
        }
    }

    private void drawCurrentSketch(Graphics2D g2) {
        if (!isDrawingActive() || drawingCoordinates.isEmpty()) {
            return;
        }

        if ("POINT".equalsIgnoreCase(drawingMode) || "MULTIPOINT".equalsIgnoreCase(drawingMode)) {
            for (Coordinate c : drawingCoordinates) {
                int x = worldToScreenX(c.x);
                int y = worldToScreenY(c.y);
                g2.setColor(Color.MAGENTA);
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawOval(x - 5, y - 5, 10, 10);
            }

            if (!Double.isNaN(hoverWorldX) && !Double.isNaN(hoverWorldY)) {
                int x = worldToScreenX(hoverWorldX);
                int y = worldToScreenY(hoverWorldY);
                g2.setColor(new Color(255, 0, 255, 120));
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawOval(x - 5, y - 5, 10, 10);
            }
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(drawingCoordinates);
        if (!Double.isNaN(hoverWorldX) && !Double.isNaN(hoverWorldY)) {
            tempCoords.add(new Coordinate(hoverWorldX, hoverWorldY));
        }

        drawTemporaryGeometry(g2, tempCoords, drawingMode, Color.MAGENTA, new Color(255, 0, 255, 40));
    }

    private void drawCurrentMeasurement(Graphics2D g2) {
        if (!isMeasurementActive() || measurementCoordinates.isEmpty()) {
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(measurementCoordinates);
        if (!Double.isNaN(hoverWorldX) && !Double.isNaN(hoverWorldY)) {
            tempCoords.add(new Coordinate(hoverWorldX, hoverWorldY));
        }

        drawTemporaryGeometry(g2, tempCoords, measurementMode, Color.CYAN, new Color(0, 255, 255, 40));
    }

    private void drawFeatureEditSketch(Graphics2D g2) {
        if (!featureEditMode || featureEditSketchCoordinates.isEmpty()) {
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(featureEditSketchCoordinates);
        if (!Double.isNaN(hoverWorldX) && !Double.isNaN(hoverWorldY) && isFeatureEditSketchMode()) {
            tempCoords.add(new Coordinate(hoverWorldX, hoverWorldY));
        }

        String mode = EDIT_OP_HOLE.equals(featureEditOperation) ? "POLYGON" : "LINE";
        drawTemporaryGeometry(g2, tempCoords, mode, new Color(14, 116, 144), new Color(14, 165, 233, 48));
    }

    private void drawLayer(Graphics2D g2, Layer layer, ShapefileData data) {
        if (data == null || data.getFeatureCollection() == null) {
            return;
        }

        boolean editingLayer = isLayerArmedForEditing(layer);

        try (FeatureIterator<SimpleFeature> iterator = data.getFeatureCollection().features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Object geomObj = feature.getDefaultGeometry();

                if (!(geomObj instanceof Geometry)) {
                    continue;
                }

                Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
                if (editingLayer) {
                    drawGeometryForEditingLayer(g2, geometry, layer);
                } else {
                    drawGeometry(g2, geometry, layer);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void drawGeometry(Graphics2D g2, Geometry geometry, Layer layer) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        if (geometry instanceof Point) {
            drawPoint(g2, (Point) geometry, layer.getPointColor(), layer.getPointSize());
            return;
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Point) {
                    drawPoint(g2, (Point) g, layer.getPointColor(), layer.getPointSize());
                }
            }
            return;
        }

        if (geometry instanceof LineString) {
            drawLineString(g2, (LineString) geometry, layer.getLineColor(), layer.getLineWidth());
            return;
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString ml = (MultiLineString) geometry;
            for (int i = 0; i < ml.getNumGeometries(); i++) {
                Geometry g = ml.getGeometryN(i);
                if (g instanceof LineString) {
                    drawLineString(g2, (LineString) g, layer.getLineColor(), layer.getLineWidth());
                }
            }
            return;
        }

        if (geometry instanceof Polygon) {
            drawPolygon(g2, (Polygon) geometry, layer.getFillColor(), layer.getBorderColor(), layer.getLineWidth());
            return;
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Polygon) {
                    drawPolygon(g2, (Polygon) g, layer.getFillColor(), layer.getBorderColor(), layer.getLineWidth());
                }
            }
            return;
        }

        Point centroid = geometry.getCentroid();
        if (centroid != null) {
            drawPoint(g2, centroid, layer.getPointColor(), Math.max(4, layer.getPointSize() - 2));
        }
    }

    private void drawGeometryForEditingLayer(Graphics2D g2, Geometry geometry, Layer layer) {
        Color strongRed = new Color(220, 38, 38);
        Color softRed = new Color(248, 113, 113, 70);

        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        if (geometry instanceof Point) {
            drawPoint(g2, (Point) geometry, strongRed, Math.max(layer.getPointSize() + 1, 9));
            return;
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Point) {
                    drawPoint(g2, (Point) g, strongRed, Math.max(layer.getPointSize() + 1, 9));
                }
            }
            return;
        }

        if (geometry instanceof LineString) {
            drawLineString(g2, (LineString) geometry, strongRed, Math.max(layer.getLineWidth(), 2.2f));
            return;
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString ml = (MultiLineString) geometry;
            for (int i = 0; i < ml.getNumGeometries(); i++) {
                Geometry g = ml.getGeometryN(i);
                if (g instanceof LineString) {
                    drawLineString(g2, (LineString) g, strongRed, Math.max(layer.getLineWidth(), 2.2f));
                }
            }
            return;
        }

        if (geometry instanceof Polygon) {
            drawPolygon(g2, (Polygon) geometry, softRed, strongRed, Math.max(layer.getLineWidth(), 2.2f));
            return;
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Polygon) {
                    drawPolygon(g2, (Polygon) g, softRed, strongRed, Math.max(layer.getLineWidth(), 2.2f));
                }
            }
        }
    }

    private void drawPoint(Graphics2D g2, Point point, Color color, int size) {
        int x = worldToScreenX(point.getX());
        int y = worldToScreenY(point.getY());

        g2.setColor(color);
        g2.fillOval(x - size / 2, y - size / 2, size, size);

        g2.setColor(Color.BLACK);
        g2.drawOval(x - size / 2, y - size / 2, size, size);
    }

    private void drawLineString(Graphics2D g2, LineString line, Color color, float width) {
        Coordinate[] coords = line.getCoordinates();
        if (coords.length < 2) {
            return;
        }

        Path2D path = new Path2D.Double();
        path.moveTo(worldToScreenX(coords[0].x), worldToScreenY(coords[0].y));

        for (int i = 1; i < coords.length; i++) {
            path.lineTo(worldToScreenX(coords[i].x), worldToScreenY(coords[i].y));
        }

        g2.setColor(color);
        g2.setStroke(new BasicStroke(width));
        g2.draw(path);
    }

    private void drawPolygon(Graphics2D g2, Polygon polygon, Color fillColor, Color borderColor, float borderWidth) {
        Path2D exteriorPath = buildPathFromCoordinates(polygon.getExteriorRing().getCoordinates());
        if (exteriorPath == null) {
            return;
        }

        g2.setColor(fillColor);
        g2.fill(exteriorPath);

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Path2D holePath = buildPathFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.setColor(getBackground());
                g2.fill(holePath);
            }
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.draw(exteriorPath);

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Path2D holePath = buildPathFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.draw(holePath);
            }
        }
    }

    private Path2D buildPathFromCoordinates(Coordinate[] coords) {
        if (coords == null || coords.length == 0) {
            return null;
        }

        Path2D path = new Path2D.Double();
        path.moveTo(worldToScreenX(coords[0].x), worldToScreenY(coords[0].y));

        for (int i = 1; i < coords.length; i++) {
            path.lineTo(worldToScreenX(coords[i].x), worldToScreenY(coords[i].y));
        }

        path.closePath();
        return path;
    }

    private void drawSelectedFeature(Graphics2D g2, SimpleFeature feature, Layer layer) {
        Object geomObj = feature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return;
        }

        Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        boolean editing = featureEditMode && layer == selectedLayer && feature == selectedFeature;
        Color haloColor = editing ? new Color(185, 28, 28, 210) : new Color(0, 170, 255, 180);
        Color selectionColor = editing ? new Color(239, 68, 68) : Color.YELLOW;

        if (geometry instanceof Point) {
            drawPoint(g2, (Point) geometry, haloColor, 18);
            drawPoint(g2, (Point) geometry, selectionColor, 10);
            return;
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Point) {
                    drawPoint(g2, (Point) g, haloColor, 18);
                    drawPoint(g2, (Point) g, selectionColor, 10);
                }
            }
            return;
        }

        if (geometry instanceof LineString) {
            drawLineString(g2, (LineString) geometry, haloColor, 6f);
            drawLineString(g2, (LineString) geometry, selectionColor, 3f);
            return;
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString ml = (MultiLineString) geometry;
            for (int i = 0; i < ml.getNumGeometries(); i++) {
                Geometry g = ml.getGeometryN(i);
                if (g instanceof LineString) {
                    drawLineString(g2, (LineString) g, haloColor, 6f);
                    drawLineString(g2, (LineString) g, selectionColor, 3f);
                }
            }
            return;
        }

        if (geometry instanceof Polygon) {
            Color outerFill = editing ? new Color(248, 113, 113, 46) : new Color(0, 170, 255, 45);
            Color innerFill = editing ? new Color(254, 202, 202, 74) : new Color(255, 255, 0, 65);
            drawPolygon(g2, (Polygon) geometry, outerFill, haloColor, 5f);
            drawPolygon(g2, (Polygon) geometry, innerFill, selectionColor, 2.5f);
            return;
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Polygon) {
                    Color outerFill = editing ? new Color(248, 113, 113, 46) : new Color(0, 170, 255, 45);
                    Color innerFill = editing ? new Color(254, 202, 202, 74) : new Color(255, 255, 0, 65);
                    drawPolygon(g2, (Polygon) g, outerFill, haloColor, 5f);
                    drawPolygon(g2, (Polygon) g, innerFill, selectionColor, 2.5f);
                }
            }
        }
    }

    private void drawAttributeTableSelections(Graphics2D g2) {
        if (tableSelectionIds.isEmpty()) {
            return;
        }

        for (Map.Entry<Layer, List<String>> entry : tableSelectionIds.entrySet()) {
            Layer layer = entry.getKey();
            if (layer == null || !layer.isVisible()) {
                continue;
            }

            ShapefileData data = getShapefileData(layer);
            if (data == null || data.getFeatures() == null) {
                continue;
            }

            List<String> ids = entry.getValue();
            if (ids == null || ids.isEmpty()) {
                continue;
            }

            for (String featureId : ids) {
                SimpleFeature feature = findFeatureById(data.getFeatures(), featureId);
                if (feature == null) {
                    continue;
                }
                if (layer == selectedLayer && selectedFeature != null && sameFeatureId(selectedFeature, featureId)) {
                    continue;
                }
                drawSelectedFeature(g2, feature, layer);
            }
        }
    }

    private void drawEditableVertices(Graphics2D g2, SimpleFeature feature, Layer layer) {
        Geometry geometry = getEditableDisplayGeometry(feature, layer);
        if (geometry == null) {
            return;
        }

        Coordinate[] vertices = getEditableVertexCoordinates(geometry);
        if (vertices == null || vertices.length == 0) {
            return;
        }

        for (int i = 0; i < vertices.length; i++) {
            Coordinate c = vertices[i];
            if (c == null) {
                continue;
            }

            int x = worldToScreenX(c.x);
            int y = worldToScreenY(c.y);
            int size = (i == activeEditVertexIndex) ? 12 : 10;

            g2.setColor(i == activeEditVertexIndex ? new Color(255, 102, 0, 240) : new Color(220, 38, 38, 220));
            g2.fillOval(x - size / 2, y - size / 2, size, size);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x - size / 2, y - size / 2, size, size);
        }
    }

    private Geometry getEditableDisplayGeometry(SimpleFeature feature, Layer layer) {
        if (feature == null) {
            return null;
        }

        Object geomObj = feature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return null;
        }

        Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
        return geometry != null && !geometry.isEmpty() ? geometry : null;
    }

    private Coordinate[] getEditableVertexCoordinates(Geometry geometry) {
        List<Coordinate> vertices = new ArrayList<>();

        if (geometry instanceof LineString) {
            for (Coordinate coordinate : ((LineString) geometry).getCoordinates()) {
                vertices.add(new Coordinate(coordinate));
            }
        } else if (geometry instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                for (Coordinate coordinate : ((LineString) multi.getGeometryN(i)).getCoordinates()) {
                    vertices.add(new Coordinate(coordinate));
                }
            }
        } else if (geometry instanceof Polygon) {
            appendVisibleRingVertices(vertices, ((Polygon) geometry).getExteriorRing().getCoordinates());
        } else if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                appendVisibleRingVertices(vertices, ((Polygon) multi.getGeometryN(i)).getExteriorRing().getCoordinates());
            }
        }

        return vertices.toArray(new Coordinate[0]);
    }

    private void appendVisibleRingVertices(List<Coordinate> vertices, Coordinate[] ringCoordinates) {
        if (ringCoordinates == null || ringCoordinates.length == 0) {
            return;
        }
        int limit = Math.max(0, ringCoordinates.length - 1);
        for (int i = 0; i < limit; i++) {
            vertices.add(new Coordinate(ringCoordinates[i]));
        }
    }

    private boolean isFeatureEditSketchMode() {
        return EDIT_OP_CUT.equals(featureEditOperation) || EDIT_OP_HOLE.equals(featureEditOperation);
    }

    private boolean isSelectedFeaturePolygonal() {
        if (selectedFeature == null) {
            return false;
        }
        Object geomObj = selectedFeature.getDefaultGeometry();
        return geomObj instanceof Polygon || geomObj instanceof MultiPolygon;
    }

    private boolean isSelectedFeatureLinearOrPolygonal() {
        if (selectedFeature == null) {
            return false;
        }
        Object geomObj = selectedFeature.getDefaultGeometry();
        return geomObj instanceof LineString
                || geomObj instanceof MultiLineString
                || geomObj instanceof Polygon
                || geomObj instanceof MultiPolygon;
    }

    private boolean handleFeatureEditClick(MouseEvent e) {
        if (selectedFeature == null || selectedLayer == null) {
            return false;
        }

        if (EDIT_OP_ADD_VERTEX.equals(featureEditOperation)) {
            return addVertexToSelectedGeometry(e.getX(), e.getY());
        }

        if (EDIT_OP_REMOVE_VERTEX.equals(featureEditOperation)) {
            return removeVertexFromSelectedGeometry(e.getX(), e.getY());
        }

        if (EDIT_OP_CUT.equals(featureEditOperation)) {
            if (isSelectedFeaturePolygonal()) {
                featureEditSketchCoordinates.add(new Coordinate(screenToWorldX(e.getX()), screenToWorldY(e.getY())));
                if (e.getClickCount() >= 2 && featureEditSketchCoordinates.size() >= 2) {
                    applyFeatureEditSketchOperationEnhanced();
                } else {
                    repaint();
                }
                return true;
            }
            return cutSelectedGeometryAtClick(e.getX(), e.getY());
        }

        if (EDIT_OP_HOLE.equals(featureEditOperation)) {
            featureEditSketchCoordinates.add(new Coordinate(screenToWorldX(e.getX()), screenToWorldY(e.getY())));
            if (e.getClickCount() >= 2 && featureEditSketchCoordinates.size() >= 3) {
                applyFeatureEditSketchOperationEnhanced();
            } else {
                repaint();
            }
            return true;
        }

        return false;
    }

    private boolean addVertexToSelectedGeometry(int screenX, int screenY) {
        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return false;
        }

        Geometry displayGeometry = getEditableDisplayGeometry(selectedFeature, selectedLayer);
        Coordinate displayTarget = new Coordinate(screenToWorldX(screenX), screenToWorldY(screenY));
        LineSplitProjection projection = findEditableSegmentProjection(displayGeometry, displayTarget, screenX, screenY, EDIT_SEGMENT_TOLERANCE_PX);
        if (projection == null || projection.segmentIndex < 0 || projection.projected == null) {
            showCopiedMessage("No se encontró un tramo cercano para agregar el vértice.");
            return true;
        }

        Coordinate sourceCoordinate = toSourceCoordinate(projection.projected.x, projection.projected.y, selectedLayer);
        Geometry updated = buildGeometryWithAddedVertex((Geometry) geomObj, projection.segmentIndex, sourceCoordinate);
        if (updated == null) {
            showCopiedMessage("No se pudo agregar el vértice en esa geometría.");
            return true;
        }

        updateSelectedFeatureGeometry(updated, "Vértice agregado.");
        return true;
    }

    private boolean removeVertexFromSelectedGeometry(int screenX, int screenY) {
        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return false;
        }

        int vertexIndex = findEditableVertexIndex(screenX, screenY);
        if (vertexIndex < 0) {
            showCopiedMessage("No se encontró un vértice cercano para eliminar.");
            return true;
        }

        Geometry updated = buildGeometryWithRemovedVertex((Geometry) geomObj, vertexIndex);
        if (updated == null) {
            showCopiedMessage("No se pudo eliminar ese vértice.");
            return true;
        }

        updateSelectedFeatureGeometry(updated, "Vértice eliminado.");
        return true;
    }

    private boolean removeVerticesFromSelectedGeometry(Rectangle selectionBounds) {
        if (selectionBounds == null || selectedFeature == null) {
            return false;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        List<Integer> vertexIndexes = collectEditableVertexIndexes(selectionBounds);
        if (vertexIndexes.isEmpty()) {
            showCopiedMessage("No se encontró ningún vértice dentro del rectángulo.");
            return true;
        }

        Geometry updated = buildGeometryWithRemovedVertices(geometry, vertexIndexes);
        if (updated == null) {
            showCopiedMessage("No se pudieron eliminar esos vértices.");
            return true;
        }

        String message = vertexIndexes.size() == 1
                ? "Vértice eliminado."
                : vertexIndexes.size() + " vértices eliminados.";
        updateSelectedFeatureGeometry(updated, message);
        return true;
    }

    private boolean cutSelectedGeometryAtClick(int screenX, int screenY) {
        if (selectedFeature == null || selectedLayer == null) {
            return false;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        Coordinate sourceCoordinate = toSourceCoordinate(screenToWorldX(screenX), screenToWorldY(screenY), selectedLayer);
        Geometry updated = buildCutGeometryAtPoint(geometry, sourceCoordinate);
        if (updated == null) {
            showCopiedMessage("No se pudo cortar la linea en ese punto.");
            return true;
        }

        List<Geometry> parts = collectGeometryParts(updated);
        if (parts.size() >= 2) {
            pushUndoSnapshotForSelectedLayer();
            replaceSelectedFeatureWithGeometries(parts, "Geometria cortada.");
            return true;
        }

        updateSelectedFeatureGeometry(updated, "Geometria cortada.");
        return true;
    }

    private void applyFeatureEditSketchOperationEnhanced() {
        if (selectedFeature == null || selectedLayer == null || featureEditSketchCoordinates.isEmpty()) {
            return;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry sourceGeometry)) {
            return;
        }

        List<Coordinate> sourceSketch = toSourceCoordinates(featureEditSketchCoordinates, selectedLayer);
        Geometry updated = null;
        String message = null;

        if (EDIT_OP_CUT.equals(featureEditOperation)) {
            updated = buildCutGeometryWithSketch(sourceGeometry, sourceSketch);
            message = "Geometria cortada.";
        } else if (EDIT_OP_HOLE.equals(featureEditOperation)) {
            updated = buildGeometryWithHole(sourceGeometry, sourceSketch);
            message = "Agujero creado.";
        }

        if (updated == null) {
            showCopiedMessage("No se pudo aplicar la edicion geometrica.");
            return;
        }

        if (EDIT_OP_CUT.equals(featureEditOperation)) {
            List<Geometry> parts = collectGeometryParts(updated);
            if (parts.size() >= 2) {
                pushUndoSnapshotForSelectedLayer();
                replaceSelectedFeatureWithGeometries(parts, message);
                return;
            }
        }

        updateSelectedFeatureGeometry(updated, message);
    }

    private boolean cutSelectedLineAtClick(int screenX, int screenY) {
        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return false;
        }

        Coordinate sourceCoordinate = toSourceCoordinate(screenToWorldX(screenX), screenToWorldY(screenY), selectedLayer);
        Geometry updated = buildCutGeometryAtPoint((Geometry) geomObj, sourceCoordinate);
        if (updated == null) {
            showCopiedMessage("No se pudo cortar la línea en ese punto.");
            return true;
        }

        updateSelectedFeatureGeometry(updated, "Geometría cortada.");
        return true;
    }

    private void applyFeatureEditSketchOperation() {
        if (selectedFeature == null || selectedLayer == null || featureEditSketchCoordinates.isEmpty()) {
            return;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return;
        }

        Geometry sourceGeometry = (Geometry) geomObj;
        List<Coordinate> sourceSketch = toSourceCoordinates(featureEditSketchCoordinates, selectedLayer);
        Geometry updated = null;
        String message = null;

        if (EDIT_OP_CUT.equals(featureEditOperation)) {
            updated = buildCutGeometryWithSketch(sourceGeometry, sourceSketch);
            message = "Geometría cortada.";
        } else if (EDIT_OP_HOLE.equals(featureEditOperation)) {
            updated = buildGeometryWithHole(sourceGeometry, sourceSketch);
            message = "Agujero creado.";
        }

        if (updated == null) {
            showCopiedMessage("No se pudo aplicar la edición geométrica.");
            return;
        }

        updateSelectedFeatureGeometry(updated, message);
    }

    private int findEditableVertexIndex(int screenX, int screenY) {
        if (!featureEditMode || selectedFeature == null || selectedLayer == null) {
            return -1;
        }

        Geometry geometry = getEditableDisplayGeometry(selectedFeature, selectedLayer);
        Coordinate[] vertices = getEditableVertexCoordinates(geometry);
        if (vertices == null || vertices.length == 0) {
            return -1;
        }

        for (int i = 0; i < vertices.length; i++) {
            Coordinate c = vertices[i];
            int vx = worldToScreenX(c.x);
            int vy = worldToScreenY(c.y);
            double distance = Math.hypot(screenX - vx, screenY - vy);
            if (distance <= EDIT_VERTEX_TOLERANCE_PX) {
                return i;
            }
        }

        return -1;
    }

    private List<Integer> collectEditableVertexIndexes(Rectangle selectionBounds) {
        List<Integer> indexes = new ArrayList<>();
        if (!featureEditMode || selectedFeature == null || selectedLayer == null || selectionBounds == null) {
            return indexes;
        }

        Geometry geometry = getEditableDisplayGeometry(selectedFeature, selectedLayer);
        Coordinate[] vertices = getEditableVertexCoordinates(geometry);
        if (vertices == null || vertices.length == 0) {
            return indexes;
        }

        Rectangle expanded = new Rectangle(
                selectionBounds.x - 2,
                selectionBounds.y - 2,
                selectionBounds.width + 4,
                selectionBounds.height + 4
        );
        for (int i = 0; i < vertices.length; i++) {
            Coordinate c = vertices[i];
            if (c == null) {
                continue;
            }
            int vx = worldToScreenX(c.x);
            int vy = worldToScreenY(c.y);
            if (expanded.contains(vx, vy)) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    private void moveSelectedVertex(double projectX, double projectY, int vertexIndex) {
        if (selectedFeature == null || selectedLayer == null || vertexIndex < 0) {
            return;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return;
        }

        Geometry sourceGeometry = (Geometry) geomObj;
        Coordinate sourceCoordinate = toSourceCoordinate(projectX, projectY, selectedLayer);

        Geometry updated = buildGeometryWithMovedVertex(sourceGeometry, vertexIndex, sourceCoordinate);
        if (updated == null) {
            return;
        }

        selectedFeature.setDefaultGeometry(updated);
        if (selectedLayer.getFeatureCount() <= 0) {
            selectedLayer.setFeatureCount(1);
        }
        featureEditDirty = true;
        CatgisDesktopApp.markProjectDirty();
        repaint();
    }

    private void moveSelectedFeatures(double projectX, double projectY) {
        if (selectedLayer == null) {
            return;
        }

        List<String> selectedIds = getSelectedFeatureIdsForLayer(selectedLayer);
        if (selectedIds.isEmpty()) {
            return;
        }

        ShapefileData data = getShapefileData(selectedLayer);
        if (data == null || data.getFeatures() == null) {
            return;
        }

        Coordinate previousSource = toSourceCoordinate(moveSelectionLastProjectX, moveSelectionLastProjectY, selectedLayer);
        Coordinate currentSource = toSourceCoordinate(projectX, projectY, selectedLayer);
        if (previousSource == null || currentSource == null) {
            return;
        }

        double dx = currentSource.x - previousSource.x;
        double dy = currentSource.y - previousSource.y;
        if (Math.abs(dx) < 0.0000001 && Math.abs(dy) < 0.0000001) {
            return;
        }

        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null || feature.getID() == null || !selectedIds.contains(feature.getID())) {
                continue;
            }
            Object geomObj = feature.getDefaultGeometry();
            if (!(geomObj instanceof Geometry geometry)) {
                continue;
            }
            feature.setDefaultGeometry(translateGeometry(geometry, dx, dy));
        }

        moveSelectionLastProjectX = projectX;
        moveSelectionLastProjectY = projectY;
        featureEditDirty = true;
        CatgisDesktopApp.markProjectDirty();
    }

    private void updateSelectedFeatureGeometry(Geometry updated, String statusMessage) {
        if (updated == null || selectedFeature == null || selectedLayer == null) {
            return;
        }

        pushUndoSnapshotForSelectedLayer();
        List<Geometry> replacementParts = new ArrayList<>();
        replacementParts.add(updated);
        replaceSelectedFeatureWithGeometries(replacementParts, statusMessage);
    }

    private boolean shouldPreserveFeatureEditOperation() {
        return EDIT_OP_ADD_VERTEX.equals(featureEditOperation)
                || EDIT_OP_REMOVE_VERTEX.equals(featureEditOperation);
    }

    private void replaceSelectedFeatureWithGeometries(List<Geometry> replacementParts, String statusMessage) {
        if (selectedLayer == null || selectedFeature == null || replacementParts == null || replacementParts.isEmpty()) {
            return;
        }

        ShapefileData data = getShapefileData(selectedLayer);
        if (data == null) {
            return;
        }

        List<SimpleFeature> features = new ArrayList<>();
        List<SimpleFeature> replacementFeatures = buildReplacementFeatures(selectedFeature, replacementParts);
        if (replacementFeatures.isEmpty()) {
            return;
        }

        boolean replaced = false;
        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null) {
                continue;
            }
            if (feature == selectedFeature || sameFeatureId(feature, selectedFeature)) {
                features.addAll(replacementFeatures);
                replaced = true;
            } else {
                features.add(feature);
            }
        }

        if (!replaced) {
            features = new ArrayList<>(data.getFeatures());
            int selectedIndex = features.indexOf(selectedFeature);
            if (selectedIndex >= 0) {
                features.remove(selectedIndex);
                features.addAll(selectedIndex, replacementFeatures);
                replaced = true;
            }
        }

        if (!replaced) {
            showCopiedMessage("No se pudo reemplazar la entidad original dentro de la capa.");
            return;
        }

        String finalMessage = statusMessage;
        if (replacementFeatures.size() >= 2) {
            finalMessage = (statusMessage != null && !statusMessage.isBlank() ? statusMessage + " " : "")
                    + "(" + replacementFeatures.size() + " tramos)";
        }
        replaceLayerFeatures(selectedLayer, features, replacementFeatures.get(0).getID(), true, finalMessage);
    }

    private void replaceLayerFeatures(Layer layer, List<SimpleFeature> features, String selectedFeatureId, boolean keepEditMode, String statusMessage) {
        if (layer == null) {
            return;
        }

        ShapefileData currentData = getShapefileData(layer);
        String sourceName = currentData != null ? currentData.getSourceName() : layer.getName();
        String message = currentData != null ? currentData.getMessage() : "Edicion vectorial";
        Envelope envelope = computeEnvelope(features);

        ShapefileData newData = new ShapefileData(features, envelope, sourceName, features != null ? features.size() : 0, message);
        addOrUpdateShapefileLayer(layer, newData);

        String nextOperation = shouldPreserveFeatureEditOperation() ? featureEditOperation : EDIT_OP_MOVE_VERTEX;
        activeVectorEditingLayer = keepEditMode || activeVectorEditingLayer == layer ? layer : activeVectorEditingLayer;
        selectedLayer = layer;
        selectedFeature = findFeatureById(newData.getFeatures(), selectedFeatureId);
        if (selectedFeatureId != null && !selectedFeatureId.isBlank()) {
            tableSelectionIds.put(layer, new ArrayList<>(List.of(selectedFeatureId)));
            OpenAttributeTableAction.syncSelectionFromMap(layer, List.of(selectedFeatureId));
        } else {
            tableSelectionIds.remove(layer);
            OpenAttributeTableAction.clearSelectionInOpenTables();
        }
        featureEditMode = keepEditMode && selectedFeature != null;
        featureEditOriginalGeometry = extractFeatureGeometryCopy(selectedFeature);
        featureEditDirty = true;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        featureEditOperation = nextOperation;
        layer.setFeatureCount(newData.getFeatureCount());

        CatgisDesktopApp.markProjectDirty();
        if (statusMessage != null && !statusMessage.isBlank()) {
            showCopiedMessage(statusMessage);
        }
        refreshEditingUi();
    }

    private void pushUndoSnapshotForSelectedLayer() {
        if (selectedLayer == null) {
            return;
        }
        pushUndoSnapshot(selectedLayer, selectedFeature != null ? selectedFeature.getID() : null);
    }

    private void pushUndoSnapshot(Layer layer, String selectedFeatureId) {
        LayerEditSnapshot snapshot = captureLayerSnapshot(layer, selectedFeatureId);
        if (snapshot == null) {
            return;
        }
        editUndoStack.push(snapshot);
        while (editUndoStack.size() > MAX_EDIT_HISTORY) {
            editUndoStack.removeLast();
        }
        editRedoStack.clear();
    }

    private LayerEditSnapshot captureLayerSnapshot(Layer layer, String selectedFeatureId) {
        if (layer == null) {
            return null;
        }
        ShapefileData data = getShapefileData(layer);
        if (data == null) {
            return null;
        }
        return new LayerEditSnapshot(
                layer,
                cloneFeatureList(data.getFeatures()),
                data.getSourceName(),
                data.getMessage(),
                selectedFeatureId
        );
    }

    private void restoreLayerSnapshot(LayerEditSnapshot snapshot, String statusMessage) {
        if (snapshot == null || snapshot.layer == null) {
            return;
        }

        ShapefileData restoredData = new ShapefileData(
                snapshot.features,
                computeEnvelope(snapshot.features),
                snapshot.sourceName,
                snapshot.features.size(),
                snapshot.message
        );
        addOrUpdateShapefileLayer(snapshot.layer, restoredData);

        activeVectorEditingLayer = snapshot.layer;
        selectedLayer = snapshot.layer;
        selectedFeature = findFeatureById(restoredData.getFeatures(), snapshot.selectedFeatureId);
        if (snapshot.selectedFeatureId != null && !snapshot.selectedFeatureId.isBlank()) {
            tableSelectionIds.put(snapshot.layer, new ArrayList<>(List.of(snapshot.selectedFeatureId)));
            OpenAttributeTableAction.syncSelectionFromMap(snapshot.layer, List.of(snapshot.selectedFeatureId));
        } else {
            tableSelectionIds.remove(snapshot.layer);
            OpenAttributeTableAction.clearSelectionInOpenTables();
        }
        featureEditMode = selectedFeature != null;
        featureEditOriginalGeometry = extractFeatureGeometryCopy(selectedFeature);
        featureEditDirty = true;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        featureEditOperation = EDIT_OP_MOVE_VERTEX;
        snapshot.layer.setFeatureCount(restoredData.getFeatureCount());

        CatgisDesktopApp.markProjectDirty();
        if (statusMessage != null && !statusMessage.isBlank()) {
            showCopiedMessage(statusMessage);
        }
        refreshEditingUi();
    }

    private Envelope computeEnvelope(List<SimpleFeature> features) {
        Envelope envelope = new Envelope();
        if (features == null) {
            return envelope;
        }
        for (SimpleFeature feature : features) {
            if (feature == null) {
                continue;
            }
            Object geomObj = feature.getDefaultGeometry();
            if (geomObj instanceof Geometry) {
                envelope.expandToInclude(((Geometry) geomObj).getEnvelopeInternal());
            }
        }
        return envelope;
    }

    private List<SimpleFeature> buildReplacementFeatures(SimpleFeature sourceFeature, List<Geometry> replacementParts) {
        List<SimpleFeature> features = new ArrayList<>();
        if (sourceFeature == null || replacementParts == null) {
            return features;
        }
        int index = 0;
        for (Geometry part : replacementParts) {
            Geometry adapted = adaptGeometryForFeatureSchema(part, sourceFeature.getFeatureType());
            if (adapted == null || adapted.isEmpty()) {
                continue;
            }
            String featureId = index == 0 ? sourceFeature.getID() : sourceFeature.getID() + "_part_" + index;
            features.add(cloneFeature(sourceFeature, adapted, featureId));
            index++;
        }
        return features;
    }

    private List<SimpleFeature> cloneFeatureList(List<SimpleFeature> features) {
        List<SimpleFeature> clones = new ArrayList<>();
        if (features == null) {
            return clones;
        }
        for (SimpleFeature feature : features) {
            if (feature != null) {
                clones.add(cloneFeature(feature, extractFeatureGeometryCopy(feature), feature.getID()));
            }
        }
        return clones;
    }

    private SimpleFeature cloneFeature(SimpleFeature sourceFeature, Geometry geometry, String featureId) {
        if (sourceFeature == null) {
            return null;
        }

        SimpleFeatureType featureType = sourceFeature.getFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        int attributeCount = sourceFeature.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            Object value = sourceFeature.getAttribute(i);
            if (value instanceof Geometry) {
                builder.add(geometry != null ? geometry : ((Geometry) value).copy());
            } else {
                builder.add(value);
            }
        }
        return builder.buildFeature(featureId != null ? featureId : sourceFeature.getID());
    }

    private SimpleFeature findFeatureById(List<SimpleFeature> features, String featureId) {
        if (features == null || featureId == null) {
            return null;
        }
        for (SimpleFeature feature : features) {
            if (feature != null && sameFeatureId(feature, featureId)) {
                return feature;
            }
        }
        return null;
    }

    private boolean sameFeatureId(SimpleFeature feature, SimpleFeature otherFeature) {
        return feature != null && otherFeature != null && sameFeatureId(feature, otherFeature.getID());
    }

    private boolean sameFeatureId(SimpleFeature feature, String featureId) {
        return feature != null && featureId != null && featureId.equals(feature.getID());
    }

    private Geometry adaptGeometryForFeatureSchema(Geometry geometry, SimpleFeatureType featureType) {
        if (geometry == null || featureType == null || featureType.getGeometryDescriptor() == null) {
            return null;
        }

        Class<?> binding = featureType.getGeometryDescriptor().getType().getBinding();
        if (binding == null || binding.isInstance(geometry)) {
            return geometry;
        }

        GeometryFactory factory = geometry.getFactory();
        if (LineString.class.isAssignableFrom(binding) && geometry instanceof MultiLineString multiLine && multiLine.getNumGeometries() > 0) {
            return (Geometry) multiLine.getGeometryN(0).copy();
        }
        if (MultiLineString.class.isAssignableFrom(binding) && geometry instanceof LineString lineString) {
            return factory.createMultiLineString(new LineString[]{(LineString) lineString.copy()});
        }
        if (Polygon.class.isAssignableFrom(binding) && geometry instanceof MultiPolygon multiPolygon && multiPolygon.getNumGeometries() > 0) {
            return (Geometry) multiPolygon.getGeometryN(0).copy();
        }
        if (MultiPolygon.class.isAssignableFrom(binding) && geometry instanceof Polygon polygon) {
            return factory.createMultiPolygon(new Polygon[]{(Polygon) polygon.copy()});
        }
        if (Point.class.isAssignableFrom(binding) && geometry instanceof MultiPoint multiPoint && multiPoint.getNumGeometries() > 0) {
            return (Geometry) multiPoint.getGeometryN(0).copy();
        }
        return geometry;
    }

    private SimpleFeature buildPastedFeature(SimpleFeature sourceFeature, Layer targetLayer, List<SimpleFeature> existingFeatures) {
        if (sourceFeature == null || targetLayer == null) {
            return null;
        }

        List<SimpleFeature> targetFeatures = existingFeatures != null ? existingFeatures : new ArrayList<>();
        SimpleFeature schemaSample = !targetFeatures.isEmpty() ? targetFeatures.get(0) : sourceFeature;
        if (schemaSample == null) {
            return null;
        }

        Geometry pastedGeometry = extractFeatureGeometryCopy(sourceFeature);
        if (pastedGeometry == null) {
            return null;
        }

        offsetGeometryForPaste(pastedGeometry);
        SimpleFeatureType targetType = schemaSample.getFeatureType();
        Geometry adaptedGeometry = adaptGeometryForFeatureSchema(pastedGeometry, targetType);
        if (adaptedGeometry == null) {
            return null;
        }

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(targetType);
        int attributeCount = targetType.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            String attrName = targetType.getDescriptor(i).getLocalName();
            Object sourceValue = null;
            if (targetType.getDescriptor(i).equals(targetType.getGeometryDescriptor())) {
                sourceValue = adaptedGeometry;
            } else if (sourceFeature.getFeatureType().getDescriptor(attrName) != null) {
                sourceValue = sourceFeature.getAttribute(attrName);
            }
            builder.add(sourceValue);
        }

        return builder.buildFeature(buildNextFeatureId(targetFeatures));
    }

    private void offsetGeometryForPaste(Geometry geometry) {
        if (geometry == null) {
            return;
        }
        Envelope env = geometry.getEnvelopeInternal();
        double dx = Math.max(1.0, Math.max(env.getWidth(), 1.0) * 0.03);
        double dy = Math.max(1.0, Math.max(env.getHeight(), 1.0) * 0.03);
        for (Coordinate coordinate : geometry.getCoordinates()) {
            if (coordinate != null) {
                coordinate.x += dx;
                coordinate.y += dy;
            }
        }
        geometry.geometryChanged();
    }

    private Geometry translateGeometry(Geometry geometry, double dx, double dy) {
        if (geometry == null) {
            return null;
        }
        Geometry translated = (Geometry) geometry.copy();
        for (Coordinate coordinate : translated.getCoordinates()) {
            if (coordinate != null) {
                coordinate.x += dx;
                coordinate.y += dy;
            }
        }
        translated.geometryChanged();
        return translated;
    }

    private String buildNextFeatureId(List<SimpleFeature> features) {
        long maxSuffix = 0L;
        if (features != null) {
            for (SimpleFeature feature : features) {
                if (feature == null || feature.getID() == null) {
                    continue;
                }
                String digits = feature.getID().replaceAll("\\D+", "");
                if (!digits.isEmpty()) {
                    try {
                        maxSuffix = Math.max(maxSuffix, Long.parseLong(digits));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return "catgis." + (maxSuffix + 1);
    }

    private List<Geometry> collectGeometryParts(Geometry geometry) {
        List<Geometry> parts = new ArrayList<>();
        if (geometry == null || geometry.isEmpty()) {
            return parts;
        }
        if (geometry instanceof MultiLineString multiLine) {
            for (int i = 0; i < multiLine.getNumGeometries(); i++) {
                Geometry part = multiLine.getGeometryN(i);
                if (part != null && !part.isEmpty()) {
                    parts.add((Geometry) part.copy());
                }
            }
            return parts;
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Geometry part = multiPolygon.getGeometryN(i);
                if (part != null && !part.isEmpty()) {
                    parts.add((Geometry) part.copy());
                }
            }
            return parts;
        }

        parts.add((Geometry) geometry.copy());
        return parts;
    }

    private Geometry extractFeatureGeometryCopy(SimpleFeature feature) {
        if (feature == null) {
            return null;
        }
        Object geomObj = feature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return null;
        }
        return ((Geometry) geomObj).copy();
    }

    private Coordinate toSourceCoordinate(double projectX, double projectY, Layer layer) {
        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        String sourceCRS = layer != null ? layer.getSourceCRS() : "";

        if (projectCRS == null || projectCRS.isBlank() || sourceCRS == null || sourceCRS.isBlank()
                || projectCRS.equalsIgnoreCase(sourceCRS)) {
            return new Coordinate(projectX, projectY);
        }

        double[] source = transformPoint(projectX, projectY, projectCRS, sourceCRS);
        if (source == null || source.length < 2) {
            return new Coordinate(projectX, projectY);
        }
        return new Coordinate(source[0], source[1]);
    }

    private List<Coordinate> toSourceCoordinates(List<Coordinate> projectCoordinates, Layer layer) {
        List<Coordinate> out = new ArrayList<>();
        if (projectCoordinates == null) {
            return out;
        }
        for (Coordinate coordinate : projectCoordinates) {
            out.add(toSourceCoordinate(coordinate.x, coordinate.y, layer));
        }
        return out;
    }

    private int findEditableSegmentIndex(Geometry geometry, int screenX, int screenY) {
        if (geometry == null) {
            return -1;
        }

        if (geometry instanceof LineString) {
            return findNearestSegmentInCoordinates(((LineString) geometry).getCoordinates(), screenX, screenY, 0);
        }
        if (geometry instanceof MultiLineString) {
            int offset = 0;
            int bestIndex = -1;
            double bestDistance = Double.MAX_VALUE;
            MultiLineString multi = (MultiLineString) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Coordinate[] coords = ((LineString) multi.getGeometryN(i)).getCoordinates();
                int local = findNearestSegmentInCoordinates(coords, screenX, screenY, offset);
                if (local >= 0) {
                    double distance = distanceToSegmentIndex(coords, screenX, screenY, local - offset);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestIndex = local;
                    }
                }
                offset += Math.max(0, coords.length - 1);
            }
            return bestDistance <= 14.0 ? bestIndex : -1;
        }
        if (geometry instanceof Polygon) {
            return findNearestSegmentInCoordinates(((Polygon) geometry).getExteriorRing().getCoordinates(), screenX, screenY, 0);
        }
        if (geometry instanceof MultiPolygon) {
            int offset = 0;
            int bestIndex = -1;
            double bestDistance = Double.MAX_VALUE;
            MultiPolygon multi = (MultiPolygon) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Coordinate[] coords = ((Polygon) multi.getGeometryN(i)).getExteriorRing().getCoordinates();
                int local = findNearestSegmentInCoordinates(coords, screenX, screenY, offset);
                if (local >= 0) {
                    double distance = distanceToSegmentIndex(coords, screenX, screenY, local - offset);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestIndex = local;
                    }
                }
                offset += Math.max(0, coords.length - 1);
            }
            return bestDistance <= 14.0 ? bestIndex : -1;
        }

        return -1;
    }

    private LineSplitProjection findEditableSegmentProjection(Geometry geometry, Coordinate target, int screenX, int screenY, double maxDistancePx) {
        if (geometry == null || target == null) {
            return null;
        }

        if (geometry instanceof LineString) {
            return projectLineSegmentProjection((LineString) geometry, target, screenX, screenY, maxDistancePx, 0);
        }
        if (geometry instanceof MultiLineString) {
            int offset = 0;
            LineSplitProjection best = null;
            MultiLineString multi = (MultiLineString) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                LineString line = (LineString) multi.getGeometryN(i);
                LineSplitProjection candidate = projectLineSegmentProjection(line, target, screenX, screenY, maxDistancePx, offset);
                if (candidate != null && (best == null || candidate.distance < best.distance)) {
                    best = candidate;
                }
                offset += Math.max(0, line.getCoordinates().length - 1);
            }
            return best;
        }
        if (geometry instanceof Polygon) {
            LineString ring = ((Polygon) geometry).getExteriorRing();
            return projectLineSegmentProjection(ring, target, screenX, screenY, maxDistancePx, 0);
        }
        if (geometry instanceof MultiPolygon) {
            int offset = 0;
            LineSplitProjection best = null;
            MultiPolygon multi = (MultiPolygon) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                LineString ring = polygon.getExteriorRing();
                LineSplitProjection candidate = projectLineSegmentProjection(ring, target, screenX, screenY, maxDistancePx, offset);
                if (candidate != null && (best == null || candidate.distance < best.distance)) {
                    best = candidate;
                }
                offset += Math.max(0, ring.getCoordinates().length - 1);
            }
            return best;
        }

        return null;
    }

    private LineSplitProjection projectLineSegmentProjection(LineString line, Coordinate target, int screenX, int screenY, double maxDistancePx, int baseIndex) {
        LineSplitProjection projection = projectCoordinateOntoLine(line, target);
        if (projection == null || projection.projected == null) {
            return null;
        }

        double distancePx = Math.hypot(worldToScreenX(projection.projected.x) - screenX, worldToScreenY(projection.projected.y) - screenY);
        if (distancePx > maxDistancePx) {
            return null;
        }

        return new LineSplitProjection(baseIndex + projection.segmentIndex, projection.projected, distancePx);
    }

    private int findNearestSegmentInCoordinates(Coordinate[] coords, int screenX, int screenY, int baseIndex) {
        if (coords == null || coords.length < 2) {
            return -1;
        }

        double bestDistance = Double.MAX_VALUE;
        int bestIndex = -1;
        for (int i = 0; i < coords.length - 1; i++) {
            double distance = pointToSegmentDistance(
                    screenX, screenY,
                    worldToScreenX(coords[i].x), worldToScreenY(coords[i].y),
                    worldToScreenX(coords[i + 1].x), worldToScreenY(coords[i + 1].y)
            );
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = baseIndex + i;
            }
        }
        return bestDistance <= 14.0 ? bestIndex : -1;
    }

    private double distanceToSegmentIndex(Coordinate[] coords, int screenX, int screenY, int localIndex) {
        if (coords == null || localIndex < 0 || localIndex >= coords.length - 1) {
            return Double.MAX_VALUE;
        }
        return pointToSegmentDistance(
                screenX, screenY,
                worldToScreenX(coords[localIndex].x), worldToScreenY(coords[localIndex].y),
                worldToScreenX(coords[localIndex + 1].x), worldToScreenY(coords[localIndex + 1].y)
        );
    }

    private double pointToSegmentDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (dx == 0 && dy == 0) {
            return Math.hypot(px - x1, py - y1);
        }
        double t = ((px - x1) * dx + (py - y1) * dy) / ((dx * dx) + (dy * dy));
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + (t * dx);
        double projY = y1 + (t * dy);
        return Math.hypot(px - projX, py - projY);
    }

    private Geometry buildGeometryWithMovedVertex(Geometry geometry, int vertexIndex, Coordinate newCoordinate) {
        if (geometry instanceof LineString) {
            Coordinate[] coords = geometry.getCoordinates().clone();
            if (vertexIndex >= coords.length) {
                return null;
            }
            coords[vertexIndex] = new Coordinate(newCoordinate);
            return geometry.getFactory().createLineString(coords);
        }

        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Coordinate[] shellCoords = polygon.getExteriorRing().getCoordinates().clone();
            if (shellCoords.length <= 3 || vertexIndex >= shellCoords.length - 1) {
                return null;
            }

            shellCoords[vertexIndex] = new Coordinate(newCoordinate);
            if (vertexIndex == 0) {
                shellCoords[shellCoords.length - 1] = new Coordinate(newCoordinate);
            }

            LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                holes[i] = geometry.getFactory().createLinearRing(polygon.getInteriorRingN(i).getCoordinates());
            }

            return geometry.getFactory().createPolygon(
                    geometry.getFactory().createLinearRing(shellCoords),
                    holes
            );
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geometry;
            LineString[] lines = new LineString[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                LineString line = (LineString) multi.getGeometryN(i);
                Coordinate[] coords = line.getCoordinates().clone();
                if (vertexIndex >= offset && vertexIndex < offset + coords.length) {
                    coords[vertexIndex - offset] = new Coordinate(newCoordinate);
                }
                lines[i] = geometry.getFactory().createLineString(coords);
                offset += coords.length;
            }
            return geometry.getFactory().createMultiLineString(lines);
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            Polygon[] polygons = new Polygon[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                Coordinate[] shellCoords = polygon.getExteriorRing().getCoordinates().clone();
                int visibleVertices = Math.max(0, shellCoords.length - 1);
                if (vertexIndex >= offset && vertexIndex < offset + visibleVertices) {
                    int localIndex = vertexIndex - offset;
                    shellCoords[localIndex] = new Coordinate(newCoordinate);
                    if (localIndex == 0) {
                        shellCoords[shellCoords.length - 1] = new Coordinate(newCoordinate);
                    }
                }
                polygons[i] = geometry.getFactory().createPolygon(
                        geometry.getFactory().createLinearRing(shellCoords),
                        copyInteriorRings(geometry.getFactory(), polygon)
                );
                offset += visibleVertices;
            }
            return geometry.getFactory().createMultiPolygon(polygons);
        }

        return null;
    }

    private Geometry buildGeometryWithAddedVertex(Geometry geometry, int segmentIndex, Coordinate newCoordinate) {
        if (geometry instanceof LineString) {
            return geometry.getFactory().createLineString(insertCoordinate(((LineString) geometry).getCoordinates(), segmentIndex + 1, newCoordinate));
        }

        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Coordinate[] shell = insertCoordinate(polygon.getExteriorRing().getCoordinates(), segmentIndex + 1, newCoordinate);
            return geometry.getFactory().createPolygon(
                    geometry.getFactory().createLinearRing(shell),
                    copyInteriorRings(geometry.getFactory(), polygon)
            );
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geometry;
            LineString[] lines = new LineString[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                LineString line = (LineString) multi.getGeometryN(i);
                Coordinate[] coords = line.getCoordinates();
                int segments = Math.max(0, coords.length - 1);
                if (segmentIndex >= offset && segmentIndex < offset + segments) {
                    coords = insertCoordinate(coords, (segmentIndex - offset) + 1, newCoordinate);
                }
                lines[i] = geometry.getFactory().createLineString(coords);
                offset += segments;
            }
            return geometry.getFactory().createMultiLineString(lines);
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            Polygon[] polygons = new Polygon[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                Coordinate[] shell = polygon.getExteriorRing().getCoordinates();
                int segments = Math.max(0, shell.length - 1);
                if (segmentIndex >= offset && segmentIndex < offset + segments) {
                    shell = insertCoordinate(shell, (segmentIndex - offset) + 1, newCoordinate);
                }
                polygons[i] = geometry.getFactory().createPolygon(
                        geometry.getFactory().createLinearRing(shell),
                        copyInteriorRings(geometry.getFactory(), polygon)
                );
                offset += segments;
            }
            return geometry.getFactory().createMultiPolygon(polygons);
        }

        return null;
    }

    private Geometry buildGeometryWithRemovedVertex(Geometry geometry, int vertexIndex) {
        if (geometry instanceof LineString) {
            Coordinate[] coords = ((LineString) geometry).getCoordinates();
            if (coords.length <= 2 || vertexIndex >= coords.length) {
                return null;
            }
            return geometry.getFactory().createLineString(removeCoordinate(coords, vertexIndex));
        }

        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Coordinate[] shell = polygon.getExteriorRing().getCoordinates();
            if (shell.length <= 4 || vertexIndex >= shell.length - 1) {
                return null;
            }
            shell = removeRingCoordinate(shell, vertexIndex);
            return geometry.getFactory().createPolygon(
                    geometry.getFactory().createLinearRing(shell),
                    copyInteriorRings(geometry.getFactory(), polygon)
            );
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geometry;
            LineString[] lines = new LineString[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Coordinate[] coords = ((LineString) multi.getGeometryN(i)).getCoordinates();
                if (vertexIndex >= offset && vertexIndex < offset + coords.length) {
                    if (coords.length <= 2) {
                        return null;
                    }
                    coords = removeCoordinate(coords, vertexIndex - offset);
                }
                lines[i] = geometry.getFactory().createLineString(coords);
                offset += coords.length;
            }
            return geometry.getFactory().createMultiLineString(lines);
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            Polygon[] polygons = new Polygon[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                Coordinate[] shell = polygon.getExteriorRing().getCoordinates();
                int visibleVertices = Math.max(0, shell.length - 1);
                if (vertexIndex >= offset && vertexIndex < offset + visibleVertices) {
                    if (shell.length <= 4) {
                        return null;
                    }
                    shell = removeRingCoordinate(shell, vertexIndex - offset);
                }
                polygons[i] = geometry.getFactory().createPolygon(
                        geometry.getFactory().createLinearRing(shell),
                        copyInteriorRings(geometry.getFactory(), polygon)
                );
                offset += visibleVertices;
            }
            return geometry.getFactory().createMultiPolygon(polygons);
        }

        return null;
    }

    private Geometry buildGeometryWithRemovedVertices(Geometry geometry, List<Integer> vertexIndexes) {
        if (geometry == null || vertexIndexes == null || vertexIndexes.isEmpty()) {
            return geometry;
        }

        List<Integer> sortedIndexes = new ArrayList<>(vertexIndexes);
        sortedIndexes.sort((a, b) -> Integer.compare(b, a));

        Geometry updated = geometry;
        for (Integer index : sortedIndexes) {
            if (index == null) {
                continue;
            }
            updated = buildGeometryWithRemovedVertex(updated, index);
            if (updated == null) {
                return null;
            }
        }
        return updated;
    }

    private Coordinate[] insertCoordinate(Coordinate[] coords, int insertIndex, Coordinate coordinate) {
        Coordinate[] out = new Coordinate[coords.length + 1];
        for (int i = 0, j = 0; i < out.length; i++) {
            if (i == insertIndex) {
                out[i] = new Coordinate(coordinate);
            } else {
                out[i] = new Coordinate(coords[j++]);
            }
        }
        return out;
    }

    private Coordinate[] removeCoordinate(Coordinate[] coords, int removeIndex) {
        Coordinate[] out = new Coordinate[coords.length - 1];
        for (int i = 0, j = 0; i < coords.length; i++) {
            if (i == removeIndex) {
                continue;
            }
            out[j++] = new Coordinate(coords[i]);
        }
        return out;
    }

    private Coordinate[] removeRingCoordinate(Coordinate[] shell, int removeVisibleIndex) {
        Coordinate[] visible = new Coordinate[shell.length - 1];
        for (int i = 0; i < visible.length; i++) {
            visible[i] = new Coordinate(shell[i]);
        }
        Coordinate[] reduced = removeCoordinate(visible, removeVisibleIndex);
        Coordinate[] closed = new Coordinate[reduced.length + 1];
        for (int i = 0; i < reduced.length; i++) {
            closed[i] = new Coordinate(reduced[i]);
        }
        closed[closed.length - 1] = new Coordinate(reduced[0]);
        return closed;
    }

    private LinearRing[] copyInteriorRings(GeometryFactory factory, Polygon polygon) {
        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            holes[i] = factory.createLinearRing(polygon.getInteriorRingN(i).getCoordinates());
        }
        return holes;
    }

    private Geometry buildCutGeometryAtPoint(Geometry geometry, Coordinate coordinate) {
        if (geometry instanceof LineString) {
            return splitLineStringAtCoordinate((LineString) geometry, coordinate);
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geometry;
            List<LineString> parts = new ArrayList<>();
            boolean splitDone = false;
            int closestIndex = -1;
            double closestDistance = Double.MAX_VALUE;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                LineString line = (LineString) multi.getGeometryN(i);
                LineSplitProjection projection = projectCoordinateOntoLine(line, coordinate);
                if (projection != null && projection.distance < closestDistance) {
                    closestDistance = projection.distance;
                    closestIndex = i;
                }
            }
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                LineString line = (LineString) multi.getGeometryN(i);
                if (!splitDone && i == closestIndex) {
                    Geometry split = splitLineStringAtCoordinate(line, coordinate);
                    if (split instanceof MultiLineString) {
                        MultiLineString splitMulti = (MultiLineString) split;
                        for (int j = 0; j < splitMulti.getNumGeometries(); j++) {
                            parts.add((LineString) splitMulti.getGeometryN(j));
                        }
                        splitDone = true;
                        continue;
                    }
                }
                parts.add(line);
            }
            if (splitDone) {
                return geometry.getFactory().createMultiLineString(parts.toArray(new LineString[0]));
            }
        }

        return null;
    }

    private Geometry splitLineStringAtCoordinate(LineString line, Coordinate coordinate) {
        if (line == null || line.getNumPoints() < 2) {
            return null;
        }

        LineSplitProjection projection = projectCoordinateOntoLine(line, coordinate);
        if (projection == null || projection.segmentIndex < 0 || projection.projected == null) {
            return null;
        }

        Coordinate[] coords = line.getCoordinates();
        double tolerance = Math.max(1e-8, Math.max(line.getLength() * 0.00001, 0.0000001));
        if (projection.projected.distance(coords[0]) <= tolerance
                || projection.projected.distance(coords[coords.length - 1]) <= tolerance) {
            return null;
        }

        List<Coordinate> firstCoords = new ArrayList<>();
        for (int i = 0; i <= projection.segmentIndex; i++) {
            appendCoordinateIfNeeded(firstCoords, coords[i], tolerance);
        }
        appendCoordinateIfNeeded(firstCoords, projection.projected, tolerance);

        List<Coordinate> secondCoords = new ArrayList<>();
        appendCoordinateIfNeeded(secondCoords, projection.projected, tolerance);
        for (int i = projection.segmentIndex + 1; i < coords.length; i++) {
            appendCoordinateIfNeeded(secondCoords, coords[i], tolerance);
        }

        if (firstCoords.size() < 2 || secondCoords.size() < 2) {
            return null;
        }

        LineString first = line.getFactory().createLineString(firstCoords.toArray(new Coordinate[0]));
        LineString second = line.getFactory().createLineString(secondCoords.toArray(new Coordinate[0]));
        if (first.getLength() <= tolerance || second.getLength() <= tolerance) {
            return null;
        }

        return line.getFactory().createMultiLineString(new LineString[]{first, second});
    }

    private LineSplitProjection projectCoordinateOntoLine(LineString line, Coordinate target) {
        if (line == null || target == null) {
            return null;
        }

        Coordinate[] coords = line.getCoordinates();
        if (coords == null || coords.length < 2) {
            return null;
        }

        int bestSegment = -1;
        Coordinate bestProjected = null;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < coords.length - 1; i++) {
            Coordinate projected = projectCoordinateOntoSegment(coords[i], coords[i + 1], target);
            double distance = projected.distance(target);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestProjected = projected;
                bestSegment = i;
            }
        }

        if (bestSegment < 0 || bestProjected == null) {
            return null;
        }
        return new LineSplitProjection(bestSegment, bestProjected, bestDistance);
    }

    private Coordinate projectCoordinateOntoSegment(Coordinate a, Coordinate b, Coordinate target) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        if (Math.abs(dx) < 0.0000001 && Math.abs(dy) < 0.0000001) {
            return new Coordinate(a);
        }

        double t = ((target.x - a.x) * dx + (target.y - a.y) * dy) / ((dx * dx) + (dy * dy));
        t = Math.max(0.0, Math.min(1.0, t));
        return new Coordinate(a.x + (t * dx), a.y + (t * dy));
    }

    private void appendCoordinateIfNeeded(List<Coordinate> coordinates, Coordinate candidate, double tolerance) {
        if (coordinates == null || candidate == null) {
            return;
        }
        if (coordinates.isEmpty()) {
            coordinates.add(new Coordinate(candidate));
            return;
        }
        Coordinate last = coordinates.get(coordinates.size() - 1);
        if (last.distance(candidate) > tolerance) {
            coordinates.add(new Coordinate(candidate));
        }
    }

    private Geometry buildCutGeometryWithSketch(Geometry geometry, List<Coordinate> sketchCoordinates) {
        if (geometry == null || sketchCoordinates == null || sketchCoordinates.size() < 2) {
            return null;
        }

        if (geometry instanceof Polygon) {
            return splitPolygonWithBlade((Polygon) geometry, sketchCoordinates);
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                Geometry split = splitPolygonWithBlade(polygon, sketchCoordinates);
                if (split != null) {
                    List<Polygon> splitPolygons = collectPolygons(split);
                    if (!splitPolygons.isEmpty()) {
                        List<Polygon> all = new ArrayList<>();
                        for (int j = 0; j < i; j++) {
                            all.add((Polygon) multi.getGeometryN(j));
                        }
                        all.addAll(splitPolygons);
                        for (int j = i + 1; j < multi.getNumGeometries(); j++) {
                            all.add((Polygon) multi.getGeometryN(j));
                        }
                        return geometry.getFactory().createMultiPolygon(all.toArray(new Polygon[0]));
                    }
                }
            }
        }

        return null;
    }

    private Geometry splitPolygonWithBlade(Polygon polygon, List<Coordinate> sketchCoordinates) {
        try {
            GeometryFactory factory = polygon.getFactory();
            LineString blade = factory.createLineString(sketchCoordinates.toArray(new Coordinate[0]));
            Geometry noded = polygon.getBoundary().union(blade);
            Polygonizer polygonizer = new Polygonizer();
            polygonizer.add(noded);

            List<Polygon> parts = new ArrayList<>();
            for (Object object : polygonizer.getPolygons()) {
                if (object instanceof Polygon) {
                    Polygon candidate = (Polygon) object;
                    Point interiorPoint = candidate.getInteriorPoint();
                    if (interiorPoint != null && polygon.covers(interiorPoint)) {
                        parts.add(candidate);
                    }
                }
            }

            if (parts.size() < 2) {
                return null;
            }

            return assemblePolygons(parts, factory);
        } catch (Exception ex) {
            return null;
        }
    }

    private Geometry buildGeometryWithHole(Geometry geometry, List<Coordinate> sketchCoordinates) {
        if (geometry == null || sketchCoordinates == null || sketchCoordinates.size() < 3) {
            return null;
        }

        GeometryFactory factory = geometry.getFactory();
        Polygon holePolygon = buildPolygonFromCoordinates(sketchCoordinates, factory);
        if (holePolygon == null) {
            return null;
        }

        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            if (!polygon.covers(holePolygon)) {
                return null;
            }
            return normalizePolygonalGeometry(polygon.difference(holePolygon), factory);
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                if (polygon.covers(holePolygon)) {
                    Geometry diff = normalizePolygonalGeometry(polygon.difference(holePolygon), factory);
                    List<Polygon> pieces = collectPolygons(diff);
                    if (pieces.isEmpty()) {
                        return null;
                    }
                    List<Polygon> all = new ArrayList<>();
                    for (int j = 0; j < i; j++) {
                        all.add((Polygon) multi.getGeometryN(j));
                    }
                    all.addAll(pieces);
                    for (int j = i + 1; j < multi.getNumGeometries(); j++) {
                        all.add((Polygon) multi.getGeometryN(j));
                    }
                    return factory.createMultiPolygon(all.toArray(new Polygon[0]));
                }
            }
        }

        return null;
    }

    private Polygon buildPolygonFromCoordinates(List<Coordinate> coordinates, GeometryFactory factory) {
        if (coordinates == null || coordinates.size() < 3) {
            return null;
        }
        Coordinate[] shell = new Coordinate[coordinates.size() + 1];
        for (int i = 0; i < coordinates.size(); i++) {
            shell[i] = new Coordinate(coordinates.get(i));
        }
        shell[shell.length - 1] = new Coordinate(coordinates.get(0));
        return factory.createPolygon(factory.createLinearRing(shell), null);
    }

    private Geometry normalizePolygonalGeometry(Geometry geometry, GeometryFactory factory) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            return geometry;
        }
        List<Polygon> polygons = collectPolygons(geometry);
        if (polygons.isEmpty()) {
            return null;
        }
        return assemblePolygons(polygons, factory);
    }

    private Geometry assemblePolygons(List<Polygon> polygons, GeometryFactory factory) {
        if (polygons == null || polygons.isEmpty()) {
            return null;
        }
        if (polygons.size() == 1) {
            return polygons.get(0);
        }
        return factory.createMultiPolygon(polygons.toArray(new Polygon[0]));
    }

    private List<Polygon> collectPolygons(Geometry geometry) {
        List<Polygon> polygons = new ArrayList<>();
        if (geometry == null || geometry.isEmpty()) {
            return polygons;
        }
        if (geometry instanceof Polygon) {
            polygons.add((Polygon) geometry);
            return polygons;
        }
        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                polygons.add((Polygon) multi.getGeometryN(i));
            }
            return polygons;
        }
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            polygons.addAll(collectPolygons(geometry.getGeometryN(i)));
        }
        return polygons;
    }

    private void drawLabels(Graphics2D g2, Layer layer, ShapefileData data) {
        if (layer == null || data == null) {
            return;
        }

        if (!layer.isVisible()) {
            return;
        }

        if (!layer.isLabelsVisible()) {
            return;
        }

        if (layer.getLabelField() == null || layer.getLabelField().isBlank()) {
            return;
        }

        SimpleFeatureCollection collection = data.getFeatureCollection();
        if (collection == null) {
            return;
        }

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);

        try (FeatureIterator<SimpleFeature> iterator = collection.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();

                Object attrValue = feature.getAttribute(layer.getLabelField());
                if (attrValue == null) {
                    continue;
                }

                String text = String.valueOf(attrValue).trim();
                if (text.isEmpty()) {
                    continue;
                }

                Object geomObj = feature.getDefaultGeometry();
                if (!(geomObj instanceof Geometry)) {
                    continue;
                }

                Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
                Coordinate labelCoordinate = getLabelCoordinate(geometry);
                if (labelCoordinate == null) {
                    continue;
                }

                int x = worldToScreenX(labelCoordinate.x);
                int y = worldToScreenY(labelCoordinate.y);

                drawTextWithHalo(g2, text, x, y);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Coordinate getLabelCoordinate(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }

        if (geometry instanceof Point) {
            return ((Point) geometry).getCoordinate();
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) geometry;
            if (mp.getNumGeometries() > 0 && mp.getGeometryN(0) instanceof Point) {
                return ((Point) mp.getGeometryN(0)).getCoordinate();
            }
        }

        if (geometry instanceof Polygon) {
            Point p = ((Polygon) geometry).getInteriorPoint();
            if (p != null) {
                return p.getCoordinate();
            }
            return geometry.getCentroid().getCoordinate();
        }

        if (geometry instanceof MultiPolygon) {
            Point p = geometry.getInteriorPoint();
            if (p != null) {
                return p.getCoordinate();
            }
            return geometry.getCentroid().getCoordinate();
        }

        if (geometry instanceof LineString || geometry instanceof MultiLineString) {
            Point p = geometry.getCentroid();
            if (p != null) {
                return p.getCoordinate();
            }
        }

        Point centroid = geometry.getCentroid();
        if (centroid != null) {
            return centroid.getCoordinate();
        }

        return null;
    }

    private void drawTextWithHalo(Graphics2D g2, String text, int x, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        int drawX = x - (textWidth / 2);
        int drawY = y - 4;

        g2.setColor(Color.WHITE);
        g2.drawString(text, drawX - 1, drawY - 1);
        g2.drawString(text, drawX + 1, drawY - 1);
        g2.drawString(text, drawX - 1, drawY + 1);
        g2.drawString(text, drawX + 1, drawY + 1);

        g2.setColor(Color.BLACK);
        g2.drawString(text, drawX, drawY);
    }

    private void drawTemporaryGeometry(Graphics2D g2, List<Coordinate> tempCoords, String mode, Color lineColor, Color fillColor) {
        if (tempCoords.isEmpty()) {
            return;
        }

        if (tempCoords.size() < 2) {
            Coordinate c = tempCoords.get(0);
            int x = worldToScreenX(c.x);
            int y = worldToScreenY(c.y);
            g2.setColor(lineColor);
            g2.fillOval(x - 4, y - 4, 8, 8);
            return;
        }

        Path2D path = new Path2D.Double();
        Coordinate first = tempCoords.get(0);
        path.moveTo(worldToScreenX(first.x), worldToScreenY(first.y));

        for (int i = 1; i < tempCoords.size(); i++) {
            Coordinate c = tempCoords.get(i);
            path.lineTo(worldToScreenX(c.x), worldToScreenY(c.y));
        }

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(lineColor);

        if (("POLYGON".equalsIgnoreCase(mode) || "AREA".equalsIgnoreCase(mode)) && tempCoords.size() >= 3) {
            g2.setColor(fillColor);
            g2.fill(path);
            g2.setColor(lineColor);
        }

        g2.draw(path);

        for (Coordinate c : tempCoords) {
            int x = worldToScreenX(c.x);
            int y = worldToScreenY(c.y);
            g2.fillOval(x - 4, y - 4, 8, 8);
        }
    }

    private int worldToScreenX(double worldX) {
        return (int) Math.round((worldX - viewMinX) * zoomFactor);
    }

    private int worldToScreenY(double worldY) {
        return (int) Math.round(getHeight() - ((worldY - viewMinY) * zoomFactor));
    }

    private double screenToWorldX(int screenX) {
        return viewMinX + (screenX / zoomFactor);
    }

    private double screenToWorldY(int screenY) {
        return viewMinY + ((getHeight() - screenY) / zoomFactor);
    }

    private static class LineSplitProjection {
        private final int segmentIndex;
        private final Coordinate projected;
        private final double distance;

        private LineSplitProjection(int segmentIndex, Coordinate projected, double distance) {
            this.segmentIndex = segmentIndex;
            this.projected = projected;
            this.distance = distance;
        }
    }

    private static class ViewState {
        private final double viewMinX;
        private final double viewMinY;
        private final double zoomFactor;

        private ViewState(double viewMinX, double viewMinY, double zoomFactor) {
            this.viewMinX = viewMinX;
            this.viewMinY = viewMinY;
            this.zoomFactor = zoomFactor;
        }

        private boolean isSameAs(ViewState other) {
            if (other == null) {
                return false;
            }
            return Math.abs(viewMinX - other.viewMinX) < 0.0000001
                    && Math.abs(viewMinY - other.viewMinY) < 0.0000001
                    && Math.abs(zoomFactor - other.zoomFactor) < 0.0000001;
        }
    }

    private static class LayerEditSnapshot {
        private final Layer layer;
        private final List<SimpleFeature> features;
        private final String sourceName;
        private final String message;
        private final String selectedFeatureId;

        private LayerEditSnapshot(Layer layer, List<SimpleFeature> features, String sourceName, String message, String selectedFeatureId) {
            this.layer = layer;
            this.features = features != null ? features : new ArrayList<>();
            this.sourceName = sourceName;
            this.message = message;
            this.selectedFeatureId = selectedFeatureId;
        }
    }

}
