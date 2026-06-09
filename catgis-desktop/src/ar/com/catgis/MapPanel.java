package ar.com.catgis;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.data.online.OnlineTileCache;

import ar.com.catgis.core.model.Project;

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
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.awt.image.BufferedImage;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.core.model.LayerGroup;
import ar.com.catgis.core.model.GradientFill;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.online.OnlineRasterSource;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.renderer.PolygonSymbolRenderer;
import ar.com.catgis.renderer.LineSymbolRenderer;
import ar.com.catgis.renderer.MapDecorationRenderer;
import ar.com.catgis.renderer.labels.LabelExpressionEngine;
import ar.com.catgis.renderer.labels.LabelPlacementEngine;

public class MapPanel extends JPanel {

    final Map<Layer, ShapefileData> shapefileLayers = new LinkedHashMap<>();
    final Map<Layer, LocalRasterData> rasterLayers = new LinkedHashMap<>();
    final Map<Layer, OnlineRasterSource> onlineTileLayers = new LinkedHashMap<>();
    final Map<Layer, OnlineWmsLayer> onlineWmsLayers = new LinkedHashMap<>();
    final Map<Layer, LocalRasterData> getRasterLayers() { return rasterLayers; }
    final Map<Layer, ShapefileData> getShapefileLayers() { return shapefileLayers; }
    final Map<Layer, OnlineRasterSource> getOnlineTileLayers() { return onlineTileLayers; }
    final Map<Layer, OnlineWmsLayer> getOnlineWmsLayers() { return onlineWmsLayers; }
    final MapDecorationRenderer mapDecorations = new MapDecorationRenderer();
    final Map<Layer, RasterStyle> rasterStyles = new LinkedHashMap<>();
    final Map<Layer, CachedRasterDisplay> rasterDisplayCache = new LinkedHashMap<>();
    private final GeometryFactory selectionGeometryFactory = new GeometryFactory();
    boolean onlineResolutionNoticeVisible = false;
    String onlineResolutionNotice = "";

    final List<PinMarker> pins = new ArrayList<>();
    int nextPinId = 1;
    PinMarker activePin = null;

    private final PinManager pinManager = new PinManager(this);

    final List<Coordinate> drawingCoordinates = new ArrayList<>();
    final List<Geometry> pendingDrawingSessionGeometries = new ArrayList<>();
    String drawingMode = null;
    Layer drawingSessionLayer = null;
    boolean drawingSessionDirty = false;
    Layer drawingContinuationLayer = null;
    String drawingContinuationFeatureId = null;
    Coordinate[] drawingContinuationBaseCoordinates = null;
    boolean drawingContinuationFromStart = false;
    boolean drawingContinuationEndpointChosen = false;

    final List<Coordinate> measurementCoordinates = new ArrayList<>();
    String measurementMode = null;

    double hoverWorldX = Double.NaN;
    double hoverWorldY = Double.NaN;

    // Extracted components (architecture refactoring)
    final MapViewController viewController = new MapViewController();
    private final SelectionManager selectionManager = new SelectionManager();
    private final MeasurementTool measurementTool = new MeasurementTool();
    private final FeatureRenderer featureRenderer = new FeatureRenderer(viewController);
    private final EditingEngine editingEngine = new EditingEngine();
    final LayerManager layerManager = new LayerManager(this);
    private final MapUtilities utilities = new MapUtilities(this);
    private final DrawingToolManager drawingToolManager;

    // Legacy fields - kept as thin delegates to viewController
    double viewMinX = 0;
    double viewMinY = 0;
    double zoomFactor = 1.0;
    private final List<ViewState> viewHistory = new ArrayList<>();
    private int viewHistoryIndex = -1;
    private boolean navigatingViewHistory = false;
    double dragStartViewMinX = 0;
    double dragStartViewMinY = 0;

    boolean dragging = false;
    boolean draggingPin = false;
    boolean temporaryMiddlePanActive = false;
    private boolean temporaryMiddlePanMoved = false;
    boolean layoutRenderMode = false;
    int lastMouseX;
    int lastMouseY;

    String currentTool = "MOVE";

    Layer selectedLayer;
    SimpleFeature selectedFeature;
    final Map<Layer, List<String>> tableSelectionIds = new LinkedHashMap<>();
    Layer activeVectorEditingLayer = null;
    private SimpleFeature copiedFeature = null;
    private final List<SimpleFeature> copiedFeatures = new ArrayList<>();
    boolean featureEditMode = false;
    private Geometry featureEditOriginalGeometry = null;
    private boolean featureEditDirty = false;
    String featureEditOperation = EDIT_OP_MOVE_VERTEX;
    final List<Coordinate> featureEditSketchCoordinates = new ArrayList<>();
    private final Deque<LayerEditSnapshot> editUndoStack = new ArrayDeque<>();
    private final Deque<LayerEditSnapshot> editRedoStack = new ArrayDeque<>();
    int activeEditVertexIndex = -1;
    int joinTargetVertexIndex = -1;
    Coordinate adjacentPolygonSegmentStart = null;
    Coordinate adjacentPolygonSegmentEnd = null;
    Coordinate cadReferenceSegmentStart = null;
    Coordinate cadReferenceSegmentEnd = null;
    private boolean cadReferenceFromStart = false;
    boolean cadReferenceEndpointChosen = false;
    static final int EDIT_VERTEX_TOLERANCE_PX = 10;
    private static final int MAX_EDIT_HISTORY = 20;
    static final String EDIT_OP_MOVE_VERTEX = "MOVE_VERTEX";
    static final String EDIT_OP_ADD_VERTEX = "ADD_VERTEX";
    static final String EDIT_OP_REMOVE_VERTEX = "REMOVE_VERTEX";
    static final String EDIT_OP_JOIN_VERTEX = "JOIN_VERTEX";
    static final String EDIT_OP_ADJACENT_POLYGON = "ADJACENT_POLYGON";
    static final String EDIT_OP_MOVE_FEATURE = "MOVE_FEATURE";
    static final String EDIT_OP_CUT = "CUT_FEATURE";
    static final String EDIT_OP_HOLE = "DIG_HOLE";
    static final String EDIT_OP_EXTEND_LINE = "EXTEND_LINE";
    static final String EDIT_OP_SHORTEN_LINE = "SHORTEN_LINE";
    static final String EDIT_OP_PARALLEL = "PARALLEL_LINE";
    static final String EDIT_OP_PERPENDICULAR = "PERPENDICULAR_LINE";
    private static final double EDIT_SEGMENT_TOLERANCE_PX = 22.0;
    static final int SELECTION_BOX_DRAG_THRESHOLD_PX = 6;
    static final int SELECTION_FLASH_DURATION_MS = 420;
    static final double SNAP_TOLERANCE_PX = 14.0;
    static final int CIRCLE_SEGMENTS = 48;

    String openedFileText = "Sin archivo cargado";
    boolean selectionBoxActive = false;
    boolean selectionBoxDragging = false;
    boolean suppressNextSelectClick = false;
    int selectionBoxStartX = -1;
    int selectionBoxStartY = -1;
    int selectionBoxEndX = -1;
    int selectionBoxEndY = -1;
    boolean movingSelectedFeatures = false;
    double moveSelectionLastProjectX = Double.NaN;
    double moveSelectionLastProjectY = Double.NaN;
    Geometry selectionFlashGeometry = null;
    long selectionFlashStartedAt = 0L;
    boolean topographicProfileCaptureActive = false;
    final List<Coordinate> topographicProfileCaptureCoordinates = new ArrayList<>();
    private TopographicProfileCaptureHandler topographicProfileCaptureHandler = null;
    boolean pointCaptureActive = false;
    private MapPointCaptureHandler pointCaptureHandler = null;
    private String pointCaptureStartMessage = I18n.t("Pour point: haz clic sobre el mapa para indicar el outlet. Usa clic derecho o Esc para cancelar.");
    private String pointCaptureSuccessMessage = I18n.t("Pour point capturado.");
    private String pointCaptureCancelMessage = I18n.t("Captura de pour point cancelada.");
    boolean cadPlacementDragActive = false;
    private Layer cadPlacementDragLayer = null;
    private CadPlacementDragHandler cadPlacementDragHandler = null;
    boolean cadPlacementDragStarted = false;
    private boolean cadPlacementDragMoved = false;
    private double cadPlacementDragStartX = Double.NaN;
    private double cadPlacementDragStartY = Double.NaN;
    private double cadPlacementDragOriginalOffsetX = 0d;
    private double cadPlacementDragOriginalOffsetY = 0d;
    private String cadPlacementDragStartMessage = I18n.t("Arrastre CAD activo: clic izquierdo y arrastra para mover. Suelta para aplicar. Usa clic derecho o Esc para cancelar.");
    private String cadPlacementDragSuccessMessage = I18n.t("Arrastre CAD aplicado.");
    private String cadPlacementDragCancelMessage = I18n.t("Arrastre CAD cancelado.");
    boolean snapEnabled = true;
    Coordinate snapPreviewCoordinate = null;
    final Timer selectionFlashTimer;

    public MapPanel() {
        setBackground(Color.WHITE);

        // Initialize extracted components
        drawingToolManager = new DrawingToolManager(this);
        viewController.setRepaintCallback(this::repaint);
        viewController.setScaleUpdateCallback(this::refreshStatusBarScale);

        configureKeyboardShortcuts();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshStatusBarScale();
            }
        });
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
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    beginTemporaryMiddlePan(e);
                    return;
                }
                if (cadPlacementDragActive) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        cancelCadPlacementDrag();
                        return;
                    }
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        beginCadPlacementDrag(e);
                    }
                    return;
                }
                if (pointCaptureActive) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        cancelPointCapture();
                    }
                    return;
                }
                if (topographicProfileCaptureActive) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        cancelTopographicProfileCapture();
                    }
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (isDrawingActive() || isMeasurementActive()) {
                        showMapPopup(e);
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
                        || EDIT_OP_REMOVE_VERTEX.equals(featureEditOperation)
                        || EDIT_OP_JOIN_VERTEX.equals(featureEditOperation))) {
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
                if (temporaryMiddlePanActive && SwingUtilities.isMiddleMouseButton(e)) {
                    finishTemporaryMiddlePan();
                    return;
                }
                if (cadPlacementDragActive) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        finishCadPlacementDrag();
                    }
                    return;
                }
                if (pointCaptureActive) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    return;
                }
                if (topographicProfileCaptureActive) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    return;
                }
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
                        } else if (featureEditMode && EDIT_OP_JOIN_VERTEX.equals(featureEditOperation)) {
                            joinVerticesFromSelection(boxBounds);
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
                    refreshStatusBarScale();
                }

                dragging = false;

                applyCursorForCurrentMode();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateStatusCoordinates(e.getX(), e.getY());

                updateHoverAndSnap(e.getX(), e.getY());

                if (temporaryMiddlePanActive) {
                    dragViewTemporarily(e);
                    return;
                }

                if (topographicProfileCaptureActive) {
                    repaint();
                    return;
                }

                if (cadPlacementDragActive && cadPlacementDragStarted) {
                    updateCadPlacementDrag(e);
                    return;
                }

                if (draggingPin && activePin != null) {
                    activePin.setX(screenToWorldX(e.getX()));
                    activePin.setY(screenToWorldY(e.getY()));
                    repaint();
                    return;
                }

                if (activeEditVertexIndex >= 0 && featureEditMode && EDIT_OP_MOVE_VERTEX.equals(featureEditOperation)) {
                    Coordinate targetCoordinate = resolveInteractiveCoordinate(e.getX(), e.getY(), true);
                    moveSelectedVertex(targetCoordinate.x, targetCoordinate.y, activeEditVertexIndex);
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

                updateHoverAndSnap(e.getX(), e.getY());

                if (cadPlacementDragActive) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    repaint();
                    return;
                }

                if (pointCaptureActive) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    repaint();
                    return;
                }

                if (topographicProfileCaptureActive) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    repaint();
                    return;
                }

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
                        || EDIT_OP_JOIN_VERTEX.equals(featureEditOperation)
                        || EDIT_OP_ADJACENT_POLYGON.equals(featureEditOperation)
                        || EDIT_OP_CUT.equals(featureEditOperation)
                        || EDIT_OP_EXTEND_LINE.equals(featureEditOperation)
                        || EDIT_OP_SHORTEN_LINE.equals(featureEditOperation)
                        || EDIT_OP_PARALLEL.equals(featureEditOperation)
                        || EDIT_OP_PERPENDICULAR.equals(featureEditOperation))) {
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
                snapPreviewCoordinate = null;
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

                if (pointCaptureActive) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        cancelPointCapture();
                        return;
                    }
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        Coordinate coordinate = resolveInteractiveCoordinate(e.getX(), e.getY(), false);
                        finishPointCapture(coordinate);
                    }
                    return;
                }

                if (cadPlacementDragActive) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        cancelCadPlacementDrag();
                    }
                    return;
                }

                if (topographicProfileCaptureActive) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        if (topographicProfileCaptureCoordinates.size() >= 2) {
                            finishTopographicProfileCapture();
                        } else {
                            cancelTopographicProfileCapture();
                        }
                        return;
                    }
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        Coordinate coordinate = resolveInteractiveCoordinate(e.getX(), e.getY(), false);
                        topographicProfileCaptureCoordinates.add(coordinate);
                        if (CatgisDesktopApp.statusBar != null) {
                            if (topographicProfileCaptureCoordinates.size() == 1) {
                                CatgisDesktopApp.statusBar.setMessage(I18n.t("Perfil topografico: agrega otro punto para comenzar el trazado."));
                            } else {
                                CatgisDesktopApp.statusBar.setMessage(I18n.t("Perfil topografico: agrega mas vertices o clic derecho para terminar el trazado."));
                            }
                        }
                        repaint();
                    }
                    return;
                }

                if (SwingUtilities.isRightMouseButton(e) || draggingPin) {
                    return;
                }

                if (featureEditMode && SwingUtilities.isLeftMouseButton(e) && handleFeatureEditClick(e)) {
                    return;
                }

                if (isDrawingActive() && SwingUtilities.isLeftMouseButton(e)) {
                    Coordinate c = resolveInteractiveCoordinate(e.getX(), e.getY(), false);

                    if ("POINT".equalsIgnoreCase(drawingMode) || "MULTIPOINT".equalsIgnoreCase(drawingMode)) {
                        drawingCoordinates.add(c);
                        repaint();
                        return;
                    }

                    if ("CIRCLE".equalsIgnoreCase(drawingMode)) {
                        appendDrawingCoordinateIfNeeded(c);
                        if (drawingCoordinates.size() >= 2) {
                            finishCurrentDrawing();
                        } else {
                            repaint();
                        }
                        return;
                    }

                    if ("CIRCLE_3P".equalsIgnoreCase(drawingMode)) {
                        appendDrawingCoordinateIfNeeded(c);
                        if (drawingCoordinates.size() >= 3) {
                            finishCurrentDrawing();
                        } else {
                            repaint();
                        }
                        return;
                    }

                    if ("CONTINUE_LINE".equalsIgnoreCase(drawingMode) && !drawingContinuationEndpointChosen) {
                        chooseContinuationEndpoint(e.getX(), e.getY());
                        return;
                    }

                    if ("RECTANGLE".equalsIgnoreCase(drawingMode)) {
                        appendDrawingCoordinateIfNeeded(c);
                        if (drawingCoordinates.size() >= 2) {
                            finishCurrentDrawing();
                        } else {
                            repaint();
                        }
                        return;
                    }

                    if (e.getClickCount() >= 2) {
                        appendDrawingCoordinateIfNeeded(c);
                        if (!drawingCoordinates.isEmpty()) {
                            finishCurrentDrawing();
                        }
                        return;
                    }

                    appendDrawingCoordinateIfNeeded(c);
                    repaint();
                    return;
                }

                if (isMeasurementActive() && SwingUtilities.isLeftMouseButton(e)) {
                    Coordinate c = resolveInteractiveCoordinate(e.getX(), e.getY(), false);

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
        int shortcutMask;
        try {
            shortcutMask = java.awt.GraphicsEnvironment.isHeadless()
                    ? InputEvent.CTRL_DOWN_MASK
                    : Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        } catch (HeadlessException ex) {
            shortcutMask = InputEvent.CTRL_DOWN_MASK;
        }

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelSketchOrMeasurement");
        getActionMap().put("cancelSketchOrMeasurement", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cadPlacementDragActive) {
                    cancelCadPlacementDrag();
                } else if (pointCaptureActive) {
                    cancelPointCapture();
                } else if (topographicProfileCaptureActive) {
                    cancelTopographicProfileCapture();
                } else if (featureEditMode && !featureEditSketchCoordinates.isEmpty()) {
                    featureEditSketchCoordinates.clear();
                    repaint();
                    showCopiedMessage("Boceto de ediciÃ³n cancelado.");
                } else if (isDrawingActive()) {
                    cancelCurrentDrawing();
                    showCopiedMessage("Dibujo cancelado.");
                } else if (isMeasurementActive()) {
                    cancelCurrentMeasurement();
                    showCopiedMessage("MediciÃ³n cancelada.");
                }
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutMask), "copySelectedFeatures");
        getActionMap().put("copySelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedFeatures();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutMask), "cutSelectedFeatures");
        getActionMap().put("cutSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cutSelectedFeatures();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutMask), "pasteSelectedFeatures");
        getActionMap().put("pasteSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Layer editingLayer = getEditingLayerRef();
                if (editingLayer == null && selectedLayer != null && !(selectedLayer instanceof RasterLayer)) {
                    prepareLayerForEditing(selectedLayer);
                }
                pasteCopiedFeatures();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "copySelectionToEditingLayer"
        );
        getActionMap().put("copySelectionToEditingLayer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedFeaturesToEditingLayer();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelectedFeatures");
        getActionMap().put("deleteSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedFeatures();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutMask), "undoFeatureEdit");
        getActionMap().put("undoFeatureEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoFeatureEdit();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcutMask), "redoFeatureEdit");
        getActionMap().put("redoFeatureEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redoFeatureEdit();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcutMask), "saveFeatureEditChanges");
        getActionMap().put("saveFeatureEditChanges", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFeatureEditChanges();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, shortcutMask), "finishFeatureEdit");
        getActionMap().put("finishFeatureEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finishFeatureEdit();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcutMask), "moveSelectedFeatures");
        getActionMap().put("moveSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activateMoveFeatureMode();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcutMask), "cutSelectedGeometry");
        getActionMap().put("cutSelectedGeometry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activateCutFeatureMode();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_J, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "mergeSelectedFeatures"
        );
        getActionMap().put("mergeSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mergeSelectedFeatures();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "explodeSelectedFeatures"
        );
        getActionMap().put("explodeSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                explodeSelectedFeatures();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_U, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "joinSelectedVertices"
        );
        getActionMap().put("joinSelectedVertices", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activateJoinVerticesMode();
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

    void rememberCurrentView() {
        rememberViewState(viewMinX, viewMinY, zoomFactor);
    }

    void rememberViewState(double minX, double minY, double zoom) {
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
        if (!viewController.canZoomPrevious()) return;
        viewController.zoomPrevious();
        syncViewFromController();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Vista anterior restaurada.");
        }
    }

    public void zoomNext() {
        if (!viewController.canZoomNext()) return;
        viewController.zoomNext();
        syncViewFromController();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Vista siguiente restaurada.");
        }
    }

    private void zoomToSelectedLayer() {
        Layer layer = (CatgisDesktopApp.layersPanel != null) ? CatgisDesktopApp.layersPanel.getSelectedLayer() : null;
        if (layer == null) {
            JOptionPane.showMessageDialog(this, "Primero seleccionÃ¡ una capa en el panel de capas.");
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
        applyCursorForCurrentMode();
        CatgisDesktopApp.syncFloatingVectorEditToolbar();
    }

    void applyCursorForCurrentMode() {
        if (temporaryMiddlePanActive) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return;
        }
        if (cadPlacementDragActive) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return;
        }
        if (pointCaptureActive || topographicProfileCaptureActive || isDrawingActive() || isMeasurementActive()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if ("MOVE".equalsIgnoreCase(currentTool)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else if ("IDENTIFY".equalsIgnoreCase(currentTool)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if ("SELECT".equalsIgnoreCase(currentTool)) {
            setCursor(resolveFeatureEditCursor());
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    void beginTemporaryMiddlePan(MouseEvent e) {
        temporaryMiddlePanActive = true;
        temporaryMiddlePanMoved = false;
        dragStartViewMinX = viewMinX;
        dragStartViewMinY = viewMinY;
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    void dragViewTemporarily(MouseEvent e) {
        int dx = e.getX() - lastMouseX;
        int dy = e.getY() - lastMouseY;
        if (dx != 0 || dy != 0) {
            viewMinX -= dx / zoomFactor;
            viewMinY += dy / zoomFactor;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            temporaryMiddlePanMoved = true;
        }
        repaint();
    }

    void finishTemporaryMiddlePan() {
        if (temporaryMiddlePanMoved) {
            rememberViewState(dragStartViewMinX, dragStartViewMinY, zoomFactor);
            rememberCurrentView();
        }
        temporaryMiddlePanActive = false;
        temporaryMiddlePanMoved = false;
        refreshStatusBarScale();
        applyCursorForCurrentMode();
    }

    void refreshEditingUi() {
        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        if (CatgisDesktopApp.layersPanel != null) {
            javax.swing.SwingUtilities.invokeLater(() -> CatgisDesktopApp.layersPanel.refreshLayerList());
        }
        repaint();
    }

    void updateHoverAndSnap(int screenX, int screenY) {
        hoverWorldX = screenToWorldX(screenX);
        hoverWorldY = screenToWorldY(screenY);

        if (snapEnabled && (isDrawingActive() || isMeasurementActive() || featureEditMode)) {
            snapPreviewCoordinate = findNearestSnapCoordinate(screenX, screenY, shouldExcludeSelectedFeatureFromSnap());
        } else {
            snapPreviewCoordinate = null;
        }
    }

    private boolean shouldExcludeSelectedFeatureFromSnap() {
        return featureEditMode
                && EDIT_OP_MOVE_VERTEX.equals(featureEditOperation)
                && activeEditVertexIndex >= 0;
    }

    Coordinate resolveInteractivePreviewCoordinate() {
        if (snapPreviewCoordinate != null) {
            return new Coordinate(snapPreviewCoordinate);
        }
        if (Double.isNaN(hoverWorldX) || Double.isNaN(hoverWorldY)) {
            return null;
        }
        return new Coordinate(hoverWorldX, hoverWorldY);
    }

    Coordinate resolveInteractiveCoordinate(int screenX, int screenY, boolean excludeSelectedFeature) {
        if (!snapEnabled) {
            return new Coordinate(screenToWorldX(screenX), screenToWorldY(screenY));
        }
        Coordinate snapped = findNearestSnapCoordinate(screenX, screenY, excludeSelectedFeature);
        if (snapped != null) {
            return new Coordinate(snapped);
        }
        return new Coordinate(screenToWorldX(screenX), screenToWorldY(screenY));
    }

    private Coordinate findNearestSnapCoordinate(int screenX, int screenY, boolean excludeSelectedFeature) {
        if (!snapEnabled) {
            return null;
        }
        SnapTarget bestTarget = null;
        Coordinate target = new Coordinate(screenToWorldX(screenX), screenToWorldY(screenY));
        for (Layer layer : getSnapCandidateLayers()) {
            if (layer == null || !layerManager.isLayerEffectivelyVisible(layer)) {
                continue;
            }

            ShapefileData data = getShapefileData(layer);
            if (data == null || data.getFeatures() == null) {
                continue;
            }

            for (SimpleFeature feature : data.getFeatures()) {
                if (feature == null) {
                    continue;
                }
                if (!isFeatureVisibleInLayer(layer, feature)) {
                    continue;
                }
                if (excludeSelectedFeature && layer == selectedLayer && sameFeatureId(feature, selectedFeature != null ? selectedFeature.getID() : null)) {
                    continue;
                }

                Object geomObj = feature.getDefaultGeometry();
                if (!(geomObj instanceof Geometry geometry)) {
                    continue;
                }

                Geometry displayGeometry = reprojectGeometryIfNeeded(layer, geometry);
                if (displayGeometry == null || displayGeometry.isEmpty()) {
                    continue;
                }

                SnapTarget candidate = findNearestSnapTarget(displayGeometry, target, screenX, screenY);
                if (candidate != null && (bestTarget == null || candidate.distance < bestTarget.distance)) {
                    bestTarget = candidate;
                }
            }
        }
        return bestTarget != null ? bestTarget.coordinate : null;
    }

    private SnapTarget findNearestSnapTarget(Geometry displayGeometry, Coordinate target, int screenX, int screenY) {
        if (displayGeometry == null || displayGeometry.isEmpty() || target == null) {
            return null;
        }

        SnapTarget bestTarget = null;
        for (Coordinate coordinate : displayGeometry.getCoordinates()) {
            if (coordinate == null) {
                continue;
            }

            int vx = worldToScreenX(coordinate.x);
            int vy = worldToScreenY(coordinate.y);
            double distance = Math.hypot(screenX - vx, screenY - vy);
            if (distance > SNAP_TOLERANCE_PX) {
                continue;
            }

            if (bestTarget == null || distance < bestTarget.distance) {
                bestTarget = new SnapTarget(new Coordinate(coordinate), distance);
            }
        }

        if (displayGeometry instanceof LineString
                || displayGeometry instanceof MultiLineString
                || displayGeometry instanceof Polygon
                || displayGeometry instanceof MultiPolygon) {
            LineSplitProjection projection = findEditableSegmentProjection(displayGeometry, target, screenX, screenY, SNAP_TOLERANCE_PX);
            if (projection != null && projection.projected != null
                    && (bestTarget == null || projection.distance < bestTarget.distance)) {
                bestTarget = new SnapTarget(new Coordinate(projection.projected), projection.distance);
            }
        }

        return bestTarget;
    }

    private List<Layer> getSnapCandidateLayers() {
        List<Layer> candidates = new ArrayList<>();
        if (activeVectorEditingLayer != null && shapefileLayers.containsKey(activeVectorEditingLayer)) {
            candidates.add(activeVectorEditingLayer);
            return candidates;
        }
        if (selectedLayer != null && shapefileLayers.containsKey(selectedLayer)) {
            candidates.add(selectedLayer);
            return candidates;
        }
        for (Layer layer : layerManager.getRenderOrderLayers()) {
            if (layer != null && shapefileLayers.containsKey(layer) && layerManager.isLayerEffectivelyVisible(layer)) {
                candidates.add(layer);
            }
        }
        return candidates;
    }

    Cursor resolveFeatureEditCursor() {
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
        if (EDIT_OP_JOIN_VERTEX.equals(featureEditOperation)) {
            return createToolCursor("U", new Color(37, 99, 235), new Color(219, 234, 254), new Color(255, 255, 255));
        }
        if (EDIT_OP_ADJACENT_POLYGON.equals(featureEditOperation)) {
            return createToolCursor("A", new Color(22, 101, 52), new Color(220, 252, 231), new Color(255, 255, 255));
        }
        if (EDIT_OP_CUT.equals(featureEditOperation)) {
            return createScissorCursor();
        }
        if (EDIT_OP_EXTEND_LINE.equals(featureEditOperation)) {
            return createToolCursor("E", new Color(21, 128, 61), new Color(220, 252, 231), new Color(255, 255, 255));
        }
        if (EDIT_OP_SHORTEN_LINE.equals(featureEditOperation)) {
            return createToolCursor("A", new Color(185, 28, 28), new Color(254, 226, 226), new Color(255, 255, 255));
        }
        if (EDIT_OP_PARALLEL.equals(featureEditOperation)) {
            return createToolCursor("P", new Color(29, 78, 216), new Color(219, 234, 254), new Color(255, 255, 255));
        }
        if (EDIT_OP_PERPENDICULAR.equals(featureEditOperation)) {
            return createToolCursor("T", new Color(79, 70, 229), new Color(224, 231, 255), new Color(255, 255, 255));
        }
        if (EDIT_OP_HOLE.equals(featureEditOperation)) {
            return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        }
        return createSelectionCursor();
    }

    Rectangle getSelectionBoxBounds() {
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

    public boolean isReadOnlyVectorLayer(Layer layer) {
        return VectorLayerUtils.isReadOnlyVectorLayer(layer);
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

    public boolean isSnapEnabled() {
        return snapEnabled;
    }

    public void setSnapEnabled(boolean snapEnabled) {
        this.snapEnabled = snapEnabled;
        if (!snapEnabled) {
            snapPreviewCoordinate = null;
        } else if (!Double.isNaN(hoverWorldX) && !Double.isNaN(hoverWorldY)) {
            snapPreviewCoordinate = findNearestSnapCoordinate(
                    worldToScreenX(hoverWorldX),
                    worldToScreenY(hoverWorldY),
                    shouldExcludeSelectedFeatureFromSnap()
            );
        }
        repaint();
        refreshEditingUi();
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

    public int getSelectedFeatureCount(Layer layer) {
        return getSelectedFeatureIdsForLayer(layer).size();
    }

    public boolean canCopySelectedFeaturesFromLayerToEditingLayer(Layer sourceLayer) {
        Layer targetLayer = getEditingLayerRef();
        if (sourceLayer == null || targetLayer == null || sourceLayer == targetLayer
                || sourceLayer instanceof RasterLayer || targetLayer instanceof RasterLayer
                || isReadOnlyVectorLayer(targetLayer)) {
            return false;
        }

        ShapefileData sourceData = getShapefileData(sourceLayer);
        ShapefileData targetData = getShapefileData(targetLayer);
        if (sourceData == null || sourceData.getSchema() == null
                || targetData == null || targetData.getSchema() == null
                || getSelectedFeatureIdsForLayer(sourceLayer).isEmpty()) {
            return false;
        }

        String sourceFamily = resolveGeometryFamily(sourceData.getSchema());
        String targetFamily = resolveGeometryFamily(targetData.getSchema());
        return !sourceFamily.isBlank() && sourceFamily.equals(targetFamily);
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

    private void clearAdjacentPolygonState() {
        adjacentPolygonSegmentStart = null;
        adjacentPolygonSegmentEnd = null;
    }

    void clearCadConstructionState() {
        cadReferenceSegmentStart = null;
        cadReferenceSegmentEnd = null;
        cadReferenceFromStart = false;
        cadReferenceEndpointChosen = false;
    }

    private boolean confirmPendingFeatureEdit(String nextActionDescription) {
        if (!hasFeatureEditChanges() || selectedFeature == null) {
            return true;
        }

        String layerName = selectedLayer != null ? selectedLayer.getName() : "la capa actual";
        Object[] options = {"Guardar", "Descartar", "Cancelar"};
        String message = "Hay cambios sin guardar en la entidad en ediciÃ³n de " + layerName + ".\n"
                + "Â¿QuerÃ©s guardarlos antes de " + nextActionDescription + "?";
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Cambios en ediciÃ³n",
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
            joinTargetVertexIndex = -1;
            clearAdjacentPolygonState();
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

    boolean applyFeatureSelection(Layer layer,
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
            if (promptOnDirty && !confirmPendingFeatureEdit("cambiar la selecciÃ³n")) {
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

        if (promptOnDirty && !confirmPendingFeatureEdit("cambiar la selecciÃ³n")) {
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
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
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
        if (isReadOnlyVectorLayer(layer)) {
            JOptionPane.showMessageDialog(this, getReadOnlyLayerMessage(layer));
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
            joinTargetVertexIndex = -1;
            clearAdjacentPolygonState();
            clearCadConstructionState();
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
        if (isReadOnlyVectorLayer(layer)) {
            JOptionPane.showMessageDialog(this, getReadOnlyLayerMessage(layer));
            return;
        }
        boolean sameSelection = layer == selectedLayer && feature == selectedFeature && featureEditMode;
        if (!sameSelection && !confirmPendingFeatureEdit("cambiar de entidad en ediciÃ³n")) {
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
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
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
            JOptionPane.showMessageDialog(this, "Mover vÃ©rtices sÃ³lo funciona sobre lÃ­neas o polÃ­gonos.");
            return;
        }
        featureEditOperation = EDIT_OP_MOVE_VERTEX;
        featureEditSketchCoordinates.clear();
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo mover vÃ©rtice activo.");
        refreshEditingUi();
    }

    public void activateMoveFeatureMode() {
        if (!hasFeatureSelection()) {
            return;
        }
        if (isReadOnlyVectorLayer(selectedLayer)) {
            JOptionPane.showMessageDialog(this, getReadOnlyLayerMessage(selectedLayer));
            return;
        }
        featureEditOperation = EDIT_OP_MOVE_FEATURE;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo mover elementos activo. ArrastrÃ¡ una entidad seleccionada.");
        refreshEditingUi();
    }

    public void activateAddVertexMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeatureLinearOrPolygonal()) {
            JOptionPane.showMessageDialog(this, "Agregar vÃ©rtices sÃ³lo funciona sobre lÃ­neas o polÃ­gonos.");
            return;
        }
        featureEditOperation = EDIT_OP_ADD_VERTEX;
        featureEditSketchCoordinates.clear();
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo agregar vÃ©rtice activo. HacÃ© clic o arrastrÃ¡ una caja sobre un tramo.");
        refreshEditingUi();
    }

    public void activateRemoveVertexMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeatureLinearOrPolygonal()) {
            JOptionPane.showMessageDialog(this, "Eliminar vÃ©rtices sÃ³lo funciona sobre lÃ­neas o polÃ­gonos.");
            return;
        }
        featureEditOperation = EDIT_OP_REMOVE_VERTEX;
        featureEditSketchCoordinates.clear();
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo eliminar vÃ©rtice activo. HacÃ© clic o arrastrÃ¡ una caja para quitar uno o varios vÃ©rtices.");
        refreshEditingUi();
    }

    public void activateJoinVerticesMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeatureLinearOrPolygonal()) {
            JOptionPane.showMessageDialog(this, "Unir vÃ©rtices sÃ³lo funciona sobre lÃ­neas o polÃ­gonos.");
            return;
        }
        featureEditOperation = EDIT_OP_JOIN_VERTEX;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo unir vÃ©rtices activo. ElegÃ­ un vÃ©rtice base y despuÃ©s otro vÃ©rtice o un rectÃ¡ngulo.");
        refreshEditingUi();
    }

    public void activateCutFeatureMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeatureLinearOrPolygonal()) {
            JOptionPane.showMessageDialog(this, "Cortar geometrÃ­a sÃ³lo funciona sobre lÃ­neas o polÃ­gonos.");
            return;
        }
        featureEditOperation = EDIT_OP_CUT;
        featureEditSketchCoordinates.clear();
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        String hint = isSelectedFeaturePolygonal()
                ? "Modo cortar activo. DibujÃ¡ la lÃ­nea de corte y terminÃ¡ con doble clic."
                : "Modo cortar activo. HacÃ© clic sobre la lÃ­nea en el punto donde querÃ©s cortarla.";
        showCopiedMessage(hint);
        refreshEditingUi();
    }

    public void activateHoleMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeaturePolygonal()) {
            JOptionPane.showMessageDialog(this, "La opciÃ³n agujero solo funciona sobre polÃ­gonos.");
            return;
        }
        featureEditOperation = EDIT_OP_HOLE;
        featureEditSketchCoordinates.clear();
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo agujero activo. DibujÃ¡ el polÃ­gono interior y terminÃ¡ con doble clic.");
        refreshEditingUi();
    }

    public void activateAdjacentPolygonMode() {
        if (!featureEditMode) {
            return;
        }
        if (!isSelectedFeaturePolygonal()) {
            JOptionPane.showMessageDialog(this, "Poligono adyacente solo funciona sobre poligonos.");
            return;
        }
        featureEditOperation = EDIT_OP_ADJACENT_POLYGON;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo poligono adyacente activo. Elegi un borde del poligono y despues un punto exterior para definir el nuevo lateral.");
        refreshEditingUi();
    }

    public void activateExtendLineMode() {
        if (!ensureSelectedLineReadyForCad("extender la linea")) {
            return;
        }
        featureEditOperation = EDIT_OP_EXTEND_LINE;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo extender linea activo. Hace clic sobre un extremo y despues marca hasta donde extender.");
        refreshEditingUi();
    }

    public void activateShortenLineMode() {
        if (!ensureSelectedLineReadyForCad("acortar la linea")) {
            return;
        }
        featureEditOperation = EDIT_OP_SHORTEN_LINE;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo acortar linea activo. Hace clic sobre un extremo y despues marca el nuevo fin.");
        refreshEditingUi();
    }

    public void activateParallelLineMode() {
        if (!ensureSelectedLineReadyForCad("crear una paralela")) {
            return;
        }
        if (resolveCadLineTargetLayer() == null) {
            JOptionPane.showMessageDialog(this, "Necesitas una capa de lineas compatible para guardar la paralela.");
            return;
        }
        featureEditOperation = EDIT_OP_PARALLEL;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo paralela activo. Elegi un tramo base y despues marca el desplazamiento lateral.");
        refreshEditingUi();
    }

    public void activatePerpendicularLineMode() {
        if (!ensureSelectedLineReadyForCad("crear una perpendicular")) {
            return;
        }
        if (resolveCadLineTargetLayer() == null) {
            JOptionPane.showMessageDialog(this, "Necesitas una capa de lineas compatible para guardar la perpendicular.");
            return;
        }
        featureEditOperation = EDIT_OP_PERPENDICULAR;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        setTool("SELECT");
        showCopiedMessage("Modo perpendicular activo. Elegi un tramo base y despues define la perpendicular con un segundo clic.");
        refreshEditingUi();
    }

    public void increaseSelectedPolygonArea() {
        adjustSelectedPolygonArea(true);
    }

    public void decreaseSelectedPolygonArea() {
        adjustSelectedPolygonArea(false);
    }

    private void adjustSelectedPolygonArea(boolean increase) {
        if (!featureEditMode || selectedFeature == null || selectedLayer == null) {
            return;
        }
        if (!isSelectedFeaturePolygonal()) {
            JOptionPane.showMessageDialog(this, "Esta herramienta solo funciona sobre poligonos.");
            return;
        }

        String actionName = increase ? "aumentar" : "disminuir";
        String unitHint = getPolygonSurfaceDistanceHint(selectedLayer);
        String input = JOptionPane.showInputDialog(
                this,
                "Distancia para " + actionName + " superficie (" + unitHint + "):",
                "5"
        );
        if (input == null) {
            return;
        }

        double distance = parsePositiveDistance(input);
        if (!(distance > 0d)) {
            JOptionPane.showMessageDialog(this, "Ingresa una distancia positiva valida.");
            return;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return;
        }

        Geometry updated = buildBufferedPolygonGeometry(geometry, selectedLayer, increase ? distance : -distance);
        if (updated == null || updated.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se pudo ajustar la superficie con esa distancia.");
            return;
        }

        updateSelectedFeatureGeometry(updated, increase ? "Superficie aumentada." : "Superficie disminuida.");
    }

    public void finishFeatureEdit() {
        if (!featureEditMode && activeVectorEditingLayer == null) {
            return;
        }
        if (!confirmPendingFeatureEdit("salir de la ediciÃ³n")) {
            return;
        }

        activeVectorEditingLayer = null;
        featureEditMode = false;
        featureEditOperation = EDIT_OP_MOVE_VERTEX;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
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
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Cambios geomÃ©tricos guardados en la sesiÃ³n del proyecto.");
        }
        refreshEditingUi();
        return true;
    }

    public void cancelFeatureEdit() {
        if (!confirmPendingFeatureEdit("cancelar la ediciÃ³n")) {
            return;
        }
        restoreFeatureEditOriginalGeometry();
        activeVectorEditingLayer = null;
        featureEditMode = false;
        featureEditOperation = EDIT_OP_MOVE_VERTEX;
        featureEditSketchCoordinates.clear();
        activeEditVertexIndex = -1;
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
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
        drawingToolManager.enableDrawPointMode();
    }

    public void enableDrawMultiPointMode() {
        drawingToolManager.enableDrawMultiPointMode();
    }

    public void enableDrawLineMode() {
        drawingToolManager.enableDrawLineMode();
    }

    public void enableContinueLineMode() {
        drawingToolManager.enableContinueLineMode();
    }

    public void enableDrawRectangleMode() {
        drawingToolManager.enableDrawRectangleMode();
    }

    public void enableDrawCircleMode() {
        drawingToolManager.enableDrawCircleMode();
    }

    public void enableDrawCircleThreePointMode() {
        drawingToolManager.enableDrawCircleThreePointMode();
    }

    public void enableDrawPolygonMode() {
        drawingToolManager.enableDrawPolygonMode();
    }

    public void enableMeasureDistanceMode() {
        cancelCurrentDrawing();
        measurementMode = "DISTANCE";
        measurementCoordinates.clear();
        setTool("MEASURE");
        showCopiedMessage("Modo medir distancia activo. Clic para vÃ©rtices. Doble clic o clic derecho para terminar. Escape para cancelar.");
        repaint();
    }

    public void enableMeasureAreaMode() {
        cancelCurrentDrawing();
        measurementMode = "AREA";
        measurementCoordinates.clear();
        setTool("MEASURE");
        showCopiedMessage("Modo medir Ã¡rea activo. Clic para vÃ©rtices. Doble clic o clic derecho para terminar. Escape para cancelar.");
        repaint();
    }

    public boolean isMeasurementActive() {
        return measurementMode != null && !measurementMode.isBlank();
    }

    public String getMeasurementMode() {
        return measurementMode;
    }

    public void cancelCurrentDrawing() {
        drawingToolManager.cancelCurrentDrawing();
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
                    JOptionPane.showMessageDialog(this, "Para medir distancia necesit\u00E1s al menos 2 v\u00E9rtices.");
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
                        "Medici\u00F3n de distancia",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else if ("AREA".equalsIgnoreCase(measurementMode)) {
                if (measurementCoordinates.size() < 3) {
                    JOptionPane.showMessageDialog(this, "Para medir \u00E1rea necesit\u00E1s al menos 3 v\u00E9rtices.");
                    return;
                }

                Geometry metricPolygon = buildMeasurementPolygonInMeters(measurementCoordinates, projectCRS);
                if (metricPolygon == null) {
                    JOptionPane.showMessageDialog(this, "No se pudo calcular el \u00E1rea.");
                    return;
                }

                double areaMeters = metricPolygon.getArea();
                double perimeterMeters = metricPolygon.getLength();

                JOptionPane.showMessageDialog(
                        this,
                        "\u00C1rea: " + formatArea(areaMeters) + "\n" +
                                "Per\u00EDmetro: " + formatDistance(perimeterMeters),
                        "Medici\u00F3n de \u00E1rea",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } finally {
            cancelCurrentMeasurement();
            CatgisDesktopApp.syncFloatingVectorEditToolbar();
        }
    }

    private Geometry buildMeasurementLineInMeters(List<Coordinate> coordinates, String sourceCRSCode) {
        return MapMeasurementUtils.buildLineInMeters(coordinates, sourceCRSCode);
    }

    private Geometry buildMeasurementPolygonInMeters(List<Coordinate> coordinates, String sourceCRSCode) {
        return MapMeasurementUtils.buildPolygonInMeters(coordinates, sourceCRSCode);
    }

    private Geometry reprojectGeometryToMetric(Geometry geometry, String sourceCRSCode) {
        return MapMeasurementUtils.reprojectToMetric(geometry, sourceCRSCode);
    }

    String chooseMetricCRSForMeasurement(String sourceCRSCode) {
        return MapMeasurementUtils.chooseMetricCRS(sourceCRSCode);
    }

    private String formatDistance(double meters) {
        return MapGeometryUtils.formatDistance(meters);
    }

    private String formatArea(double squareMeters) {
        return MapGeometryUtils.formatArea(squareMeters);
    }

    public void finishCurrentDrawing() {
        drawingToolManager.finishCurrentDrawing();
    }

    public void closeCurrentDrawingSession() {
        drawingToolManager.closeCurrentDrawingSession();
    }

    void appendDrawingCoordinateIfNeeded(Coordinate coordinate) {
        drawingToolManager.appendDrawingCoordinateIfNeeded(coordinate);
    }

    boolean saveVectorLayerNow(Layer layer) {
        if (layer == null) {
            return true;
        }

        ShapefileData data = getShapefileData(layer);
        if (!ExportVectorLayerAction.hasExportableVectorData(data)) {
            return true;
        }

        if (ExportVectorLayerAction.hasSupportedVectorPath(layer)) {
            return ExportVectorLayerAction.saveLayerToCurrentPath(layer, this, false);
        }

        File exported = ExportVectorLayerAction.exportLayerWithDialog(
                layer,
                data,
                this,
                "Guardar capa vectorial",
                false
        );
        return exported != null;
    }

    private Layer resolveDrawingTargetLayer() {
        return drawingToolManager.resolveDrawingTargetLayer();
    }

    private boolean isCompatibleDrawingTarget(Layer layer, String mode) {
        return drawingToolManager.isCompatibleDrawingTarget(layer, mode);
    }

    private String resolveDrawingGeometryFamily(String mode) {
        return drawingToolManager.resolveDrawingGeometryFamily(mode);
    }

    private String resolveLayerGeometryFamily(SimpleFeatureType schema) {
        return drawingToolManager.resolveLayerGeometryFamily(schema);
    }

    private boolean appendCurrentDrawingToLayer(Layer layer) {
        return drawingToolManager.appendCurrentDrawingToLayer(layer);
    }

    private boolean appendGeometriesToLayer(Layer layer, List<Geometry> newGeometries, String successMessage) {
        return drawingToolManager.appendGeometriesToLayer(layer, newGeometries, successMessage);
    }

    private List<Geometry> buildDrawingGeometriesForLayer(SimpleFeatureType targetType) {
        return drawingToolManager.buildDrawingGeometriesForLayer(targetType);
    }

    void chooseContinuationEndpoint(int screenX, int screenY) {
        drawingToolManager.chooseContinuationEndpoint(screenX, screenY);
    }

    private Geometry buildContinuationLineGeometry() {
        return drawingToolManager.buildContinuationLineGeometry();
    }

    private Geometry buildRectangleGeometry(List<Coordinate> coordinates) {
        return drawingToolManager.buildRectangleGeometry(coordinates);
    }

    Geometry buildCircleGeometry(List<Coordinate> coordinates) {
        return drawingToolManager.buildCircleGeometry(coordinates);
    }

    Geometry buildCircleThreePointGeometry(List<Coordinate> coordinates) {
        return drawingToolManager.buildCircleThreePointGeometry(coordinates);
    }

    private Geometry buildCirclePolygon(Coordinate center, double radius) {
        return drawingToolManager.buildCirclePolygon(center, radius);
    }

    private Coordinate computeCircumcenter(Coordinate a, Coordinate b, Coordinate c) {
        return drawingToolManager.computeCircumcenter(a, b, c);
    }

    List<Coordinate> buildRectangleCoordinates(List<Coordinate> coordinates) {
        return drawingToolManager.buildRectangleCoordinates(coordinates);
    }

    Coordinate[] extractContinuableLineCoordinates(Geometry geometry) {
        return drawingToolManager.extractContinuableLineCoordinates(geometry);
    }

    Coordinate[] cloneCoordinates(Coordinate[] coordinates) {
        return drawingToolManager.cloneCoordinates(coordinates);
    }

    static Coordinate[] reverseCoordinates(Coordinate[] coordinates) {
        return MapGeometryUtils.reverseCoordinates(coordinates);
    }

    SimpleFeature buildNewFeatureForLayer(ShapefileData targetData, Geometry geometry, List<SimpleFeature> existingFeatures) {
        if (targetData == null || targetData.getSchema() == null || geometry == null) {
            return null;
        }

        SimpleFeatureType targetType = targetData.getSchema();
        Geometry adaptedGeometry = adaptGeometryForFeatureSchema(geometry, targetType);
        if (adaptedGeometry == null || adaptedGeometry.isEmpty()) {
            return null;
        }

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(targetType);
        for (int i = 0; i < targetType.getAttributeCount(); i++) {
            String attrName = targetType.getDescriptor(i).getLocalName();
            if (targetType.getDescriptor(i).equals(targetType.getGeometryDescriptor())) {
                builder.add(adaptedGeometry);
            } else {
                builder.add(defaultValueForAttribute(targetType, attrName));
            }
        }

        return builder.buildFeature(buildNextFeatureId(existingFeatures));
    }

    private SimpleFeature buildDerivedFeatureForLayer(ShapefileData targetData,
                                                      Geometry geometry,
                                                      List<SimpleFeature> existingFeatures,
                                                      SimpleFeature sourceFeature) {
        if (targetData == null || targetData.getSchema() == null || geometry == null) {
            return null;
        }

        Geometry adaptedGeometry = adaptGeometryForFeatureSchema(geometry, targetData.getSchema());
        if (adaptedGeometry == null || adaptedGeometry.isEmpty()) {
            return null;
        }

        if (sourceFeature != null && sourceFeature.getFeatureType() != null
                && targetData.getSchema().equals(sourceFeature.getFeatureType())) {
            return cloneFeature(sourceFeature, adaptedGeometry, buildNextFeatureId(existingFeatures));
        }

        return buildNewFeatureForLayer(targetData, adaptedGeometry, existingFeatures);
    }

    private Object defaultValueForAttribute(SimpleFeatureType featureType, String attributeName) {
        if (featureType == null || attributeName == null || featureType.getDescriptor(attributeName) == null) {
            return null;
        }

        Class<?> binding = featureType.getDescriptor(attributeName).getType() != null
                ? featureType.getDescriptor(attributeName).getType().getBinding()
                : null;

        if (binding == null) {
            return null;
        }
        if (String.class.isAssignableFrom(binding)) {
            return "";
        }
        return null;
    }

    public boolean isDrawingActive() {
        return drawingMode != null && !drawingMode.isBlank();
    }

    public String getDrawingMode() {
        return drawingMode;
    }

    public void zoomIn() {
        viewController.rememberCurrentView();
        viewController.zoomIn();
        syncViewFromController();
    }

    public void zoomOut() {
        viewController.rememberCurrentView();
        viewController.zoomOut();
        syncViewFromController();
    }

    public void applyRequestedScale(String input) {
        Double targetDenominator = parseScaleDenominator(input);
        if (targetDenominator == null || targetDenominator <= 0d) {
            JOptionPane.showMessageDialog(
                    this,
                    "Introduce una escala valida. Ejemplo: 1:5000",
                    "Escala de vista",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        double currentDenominator = getCurrentScaleDenominator();
        if (currentDenominator <= 0d) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo calcular la escala actual de la vista.",
                    "Escala de vista",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        rememberCurrentView();
        double factor = currentDenominator / targetDenominator;
        applyZoom(factor, Math.max(1, getWidth() / 2), Math.max(1, getHeight() / 2));
        rememberCurrentView();
        if (CatgisDesktopApp.statusBar != null) {
            String scaleText = formatScaleDenominator(targetDenominator);
            CatgisDesktopApp.statusBar.forceScaleText(scaleText);
            CatgisDesktopApp.statusBar.setMessage("Escala de vista ajustada a " + scaleText + ".");
        }
    }

    public void refreshStatusBarScale() {
        if (CatgisDesktopApp.statusBar == null) {
            return;
        }
        double denominator = getCurrentScaleDenominator();
        CatgisDesktopApp.statusBar.setScaleText(formatScaleDenominator(denominator));
        CatgisDesktopApp.statusBar.setScaleToolTip(buildScaleTooltip(denominator));
    }

    public double getCurrentScaleDenominator() {
        if (zoomFactor <= 0d || getWidth() <= 0 || !hasLoadedMapContent()) {
            return 0d;
        }

        int screenDpi = safeScreenDpi();
        double metersPerProjectUnit = estimateMetersPerProjectUnit();
        if (metersPerProjectUnit <= 0d) {
            return 0d;
        }

        double groundMetersPerPixel = metersPerProjectUnit / zoomFactor;
        double screenMetersPerPixel = 0.0254d / Math.max(1, screenDpi);
        double denominator = groundMetersPerPixel / screenMetersPerPixel;
        if (Double.isFinite(denominator) && denominator > 0d) {
            return denominator;
        }
        return 0d;
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

        String savedCrs = layer.getSourceCRS();
        if (savedCrs == null || savedCrs.isBlank()) {
            String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
            String operationalCrs = RasterCoverageSupport.resolveOperationalRasterCrs(data, projectCrs);
            if (data.isGeoreferenced() && operationalCrs != null && !operationalCrs.isBlank()) {
                layer.setSourceCRS(operationalCrs);
            }
        }

        boolean wasEmpty = shapefileLayers.isEmpty() && rasterLayers.isEmpty() && onlineTileLayers.isEmpty() && onlineWmsLayers.isEmpty();
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

    public void addOrUpdateOnlineTileLayer(OnlineTileLayer layer) {
        if (layer == null) {
            return;
        }

        OnlineRasterSource source = OnlineMapCatalog.getById(layer.getSourceId());
        if (source == null) {
            source = layer.toSourceDescriptor();
        }

        boolean wasEmpty = shapefileLayers.isEmpty() && rasterLayers.isEmpty() && onlineTileLayers.isEmpty() && onlineWmsLayers.isEmpty();
        onlineTileLayers.put(layer, source);

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

    public void addOrUpdateOnlineWmsLayer(OnlineWmsLayer layer) {
        if (layer == null) {
            return;
        }

        boolean wasEmpty = shapefileLayers.isEmpty() && rasterLayers.isEmpty() && onlineTileLayers.isEmpty() && onlineWmsLayers.isEmpty();
        onlineWmsLayers.put(layer, layer);

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

    public void startTopographicProfileCapture(TopographicProfileCaptureHandler handler) {
        if (handler == null) {
            return;
        }
        if (isDrawingActive() || isMeasurementActive() || pointCaptureActive || cadPlacementDragActive) {
            JOptionPane.showMessageDialog(this, I18n.t("Termina o cancela el dibujo/medicion actual antes de capturar un perfil."));
            return;
        }
        topographicProfileCaptureHandler = handler;
        topographicProfileCaptureActive = true;
        topographicProfileCaptureCoordinates.clear();
        requestFocusInWindow();
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.t("Perfil topografico: haz clics sobre el mapa para dibujar la linea. Usa clic derecho para terminar o Esc para cancelar."));
        }
        repaint();
    }

    public void startCadPlacementDrag(Layer layer,
                                      CadPlacementDragHandler handler,
                                      String startMessage,
                                      String successMessage,
                                      String cancelMessage) {
        if (layer == null || !CadLayerSupport.isCadLayer(layer) || handler == null) {
            return;
        }
        if (isDrawingActive() || isMeasurementActive() || pointCaptureActive || topographicProfileCaptureActive || cadPlacementDragActive) {
            JOptionPane.showMessageDialog(this, I18n.t("Termina o cancela la captura, dibujo o medicion actual antes de arrastrar la referencia CAD."));
            return;
        }
        cadPlacementDragLayer = layer;
        cadPlacementDragHandler = handler;
        cadPlacementDragActive = true;
        cadPlacementDragStarted = false;
        cadPlacementDragMoved = false;
        cadPlacementDragStartX = Double.NaN;
        cadPlacementDragStartY = Double.NaN;
        cadPlacementDragOriginalOffsetX = layer.getCadOffsetX();
        cadPlacementDragOriginalOffsetY = layer.getCadOffsetY();
        cadPlacementDragStartMessage = startMessage != null && !startMessage.isBlank()
                ? startMessage
                : I18n.t("Arrastre CAD activo: clic izquierdo y arrastra para mover. Suelta para aplicar. Usa clic derecho o Esc para cancelar.");
        cadPlacementDragSuccessMessage = successMessage != null && !successMessage.isBlank()
                ? successMessage
                : I18n.t("Arrastre CAD aplicado.");
        cadPlacementDragCancelMessage = cancelMessage != null && !cancelMessage.isBlank()
                ? cancelMessage
                : I18n.t("Arrastre CAD cancelado.");
        requestFocusInWindow();
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(cadPlacementDragStartMessage);
        }
        repaint();
    }

    public boolean isCadPlacementDragActive() {
        return cadPlacementDragActive;
    }

    public void cancelCadPlacementDrag() {
        if (!cadPlacementDragActive) {
            return;
        }
        Layer layer = cadPlacementDragLayer;
        if (layer != null) {
            layer.setCadOffsetX(cadPlacementDragOriginalOffsetX);
            layer.setCadOffsetY(cadPlacementDragOriginalOffsetY);
        }
        CadPlacementDragHandler handler = cadPlacementDragHandler;
        cadPlacementDragActive = false;
        cadPlacementDragStarted = false;
        cadPlacementDragMoved = false;
        cadPlacementDragStartX = Double.NaN;
        cadPlacementDragStartY = Double.NaN;
        cadPlacementDragLayer = null;
        cadPlacementDragHandler = null;
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(cadPlacementDragCancelMessage);
        }
        applyCursorForCurrentMode();
        repaint();
        if (handler != null) {
            handler.onDragCanceled();
        }
    }

    public void startPointCapture(MapPointCaptureHandler handler) {
        startPointCapture(
                handler,
                I18n.t("Pour point: haz clic sobre el mapa para indicar el outlet. Usa clic derecho o Esc para cancelar."),
                I18n.t("Pour point capturado."),
                I18n.t("Captura de pour point cancelada.")
        );
    }

    public void startPointCapture(MapPointCaptureHandler handler,
                                  String startMessage,
                                  String successMessage,
                                  String cancelMessage) {
        if (handler == null) {
            return;
        }
        if (isDrawingActive() || isMeasurementActive() || topographicProfileCaptureActive || cadPlacementDragActive) {
            JOptionPane.showMessageDialog(this, I18n.t("Termina o cancela la captura, dibujo o medicion actual antes de capturar un punto en el mapa."));
            return;
        }
        pointCaptureHandler = handler;
        pointCaptureActive = true;
        pointCaptureStartMessage = startMessage != null && !startMessage.isBlank()
                ? startMessage
                : I18n.t("Pour point: haz clic sobre el mapa para indicar el outlet. Usa clic derecho o Esc para cancelar.");
        pointCaptureSuccessMessage = successMessage != null && !successMessage.isBlank()
                ? successMessage
                : I18n.t("Pour point capturado.");
        pointCaptureCancelMessage = cancelMessage != null && !cancelMessage.isBlank()
                ? cancelMessage
                : I18n.t("Captura de pour point cancelada.");
        requestFocusInWindow();
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(pointCaptureStartMessage);
        }
        repaint();
    }

    public void cancelPointCapture() {
        MapPointCaptureHandler handler = pointCaptureHandler;
        pointCaptureActive = false;
        pointCaptureHandler = null;
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(pointCaptureCancelMessage);
        }
        repaint();
        if (handler != null) {
            handler.onCaptureCanceled();
        }
    }

    public boolean isPointCaptureActive() {
        return pointCaptureActive;
    }

    void finishPointCapture(Coordinate coordinate) {
        if (!pointCaptureActive || coordinate == null) {
            return;
        }
        MapPointCaptureHandler handler = pointCaptureHandler;
        String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "EPSG:4326";
        pointCaptureActive = false;
        pointCaptureHandler = null;
        repaint();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(pointCaptureSuccessMessage);
        }
        if (handler != null) {
            handler.onPointCaptured(coordinate, projectCrs);
        }
    }

    void beginCadPlacementDrag(MouseEvent e) {
        if (!cadPlacementDragActive || cadPlacementDragLayer == null || !SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        Coordinate coordinate = resolveInteractiveCoordinate(e.getX(), e.getY(), false);
        cadPlacementDragStarted = true;
        cadPlacementDragMoved = false;
        cadPlacementDragStartX = coordinate.x;
        cadPlacementDragStartY = coordinate.y;
        cadPlacementDragOriginalOffsetX = cadPlacementDragLayer.getCadOffsetX();
        cadPlacementDragOriginalOffsetY = cadPlacementDragLayer.getCadOffsetY();
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    void updateCadPlacementDrag(MouseEvent e) {
        if (!cadPlacementDragActive || !cadPlacementDragStarted || cadPlacementDragLayer == null) {
            return;
        }
        Coordinate coordinate = resolveInteractiveCoordinate(e.getX(), e.getY(), true);
        double dx = coordinate.x - cadPlacementDragStartX;
        double dy = coordinate.y - cadPlacementDragStartY;
        cadPlacementDragLayer.setCadOffsetX(cadPlacementDragOriginalOffsetX + dx);
        cadPlacementDragLayer.setCadOffsetY(cadPlacementDragOriginalOffsetY + dy);
        cadPlacementDragMoved = cadPlacementDragMoved || Math.abs(dx) > 1e-9 || Math.abs(dy) > 1e-9;
        repaint();
    }

    void finishCadPlacementDrag() {
        if (!cadPlacementDragActive) {
            return;
        }
        if (!cadPlacementDragStarted) {
            return;
        }
        Layer layer = cadPlacementDragLayer;
        CadPlacementDragHandler handler = cadPlacementDragHandler;
        boolean moved = cadPlacementDragMoved;
        double offsetX = layer != null ? layer.getCadOffsetX() : 0d;
        double offsetY = layer != null ? layer.getCadOffsetY() : 0d;

        cadPlacementDragActive = false;
        cadPlacementDragStarted = false;
        cadPlacementDragMoved = false;
        cadPlacementDragStartX = Double.NaN;
        cadPlacementDragStartY = Double.NaN;
        cadPlacementDragLayer = null;
        cadPlacementDragHandler = null;

        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(moved ? cadPlacementDragSuccessMessage : cadPlacementDragCancelMessage);
        }
        applyCursorForCurrentMode();
        repaint();
        if (handler != null) {
            if (moved) {
                handler.onDragApplied(offsetX, offsetY);
            } else {
                handler.onDragCanceled();
            }
        }
    }

    public void cancelTopographicProfileCapture() {
        TopographicProfileCaptureHandler handler = topographicProfileCaptureHandler;
        topographicProfileCaptureActive = false;
        topographicProfileCaptureCoordinates.clear();
        topographicProfileCaptureHandler = null;
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.t("Captura de perfil topografico cancelada."));
        }
        repaint();
        if (handler != null) {
            handler.onCaptureCanceled();
        }
    }

    public boolean isTopographicProfileCaptureActive() {
        return topographicProfileCaptureActive;
    }

    void finishTopographicProfileCapture() {
        if (!topographicProfileCaptureActive || topographicProfileCaptureCoordinates.size() < 2) {
            return;
        }
        LineString line = TopographicProfileService.buildLineFromProjectCoordinates(topographicProfileCaptureCoordinates);
        TopographicProfileCaptureHandler handler = topographicProfileCaptureHandler;
        String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "EPSG:4326";
        topographicProfileCaptureActive = false;
        topographicProfileCaptureCoordinates.clear();
        topographicProfileCaptureHandler = null;
        repaint();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.t("Linea de perfil capturada."));
        }
        if (handler != null && line != null) {
            handler.onLineCaptured(line, projectCrs);
        }
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

    public void addLayer(Layer layer) {
        layerManager.addLayer(layer);
    }

    public void removeLayer(Layer layer) {
        layerManager.removeLayer(layer);
    }

    public void removeLayers(Collection<Layer> layers) {
        if (layers == null || layers.isEmpty()) {
            return;
        }

        boolean hasRemainingLayersBeforeFit = false;
        for (Layer layer : new ArrayList<>(layers)) {
            if (layer == null) {
                continue;
            }
            cleanupLayerReferencesOnRemoval(layer);
            shapefileLayers.remove(layer);
            rasterLayers.remove(layer);
            onlineTileLayers.remove(layer);
            onlineWmsLayers.remove(layer);
            rasterStyles.remove(layer);
            invalidateRasterDisplay(layer);
        }

        hasRemainingLayersBeforeFit = !shapefileLayers.isEmpty()
                || !rasterLayers.isEmpty()
                || !onlineTileLayers.isEmpty()
                || !onlineWmsLayers.isEmpty();

        if (hasRemainingLayersBeforeFit) {
            fitToAllLayers();
        }

        repaint();
    }

    /* package-private */ void cleanupLayerReferencesOnRemoval(Layer layer) {
        tableSelectionIds.remove(layer);

        if (selectedLayer == layer) {
            selectedLayer = null;
            selectedFeature = null;
        }

        if (drawingSessionLayer == layer || drawingContinuationLayer == layer) {
            cancelCurrentDrawing();
        }

        if (activeVectorEditingLayer == layer) {
            activeVectorEditingLayer = null;
            featureEditMode = false;
            featureEditOperation = EDIT_OP_MOVE_VERTEX;
            featureEditSketchCoordinates.clear();
            activeEditVertexIndex = -1;
            joinTargetVertexIndex = -1;
            clearAdjacentPolygonState();
            clearCadConstructionState();
            featureEditOriginalGeometry = null;
            featureEditDirty = false;
            editUndoStack.clear();
            editRedoStack.clear();
        }

        refreshEditingUi();
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
        onlineTileLayers.clear();
        onlineWmsLayers.clear();
        rasterStyles.clear();
        rasterDisplayCache.clear();
        selectedLayer = null;
        selectedFeature = null;
        clearAllPins();
        cancelCurrentDrawing();
        cancelCurrentMeasurement();
        refreshStatusBarScale();
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

    public double getViewMinX() { return viewController.getViewMinX(); }
    public double getViewMinY() { return viewController.getViewMinY(); }
    public double getZoomFactor() { return viewController.getZoomFactor(); }

    // --- Extracted component accessors ---
    public MapViewController getViewController() { return viewController; }
    public SelectionManager getSelectionManager() { return selectionManager; }
    public MeasurementTool getMeasurementTool() { return measurementTool; }
    public FeatureRenderer getFeatureRenderer() { return featureRenderer; }
    public EditingEngine getEditingEngine() { return editingEngine; }

    // --- Sync methods ---
    private void syncViewFromController() {
        viewMinX = viewController.getViewMinX();
        viewMinY = viewController.getViewMinY();
        zoomFactor = viewController.getZoomFactor();
        refreshStatusBarScale();
        repaint();
    }

    private void syncViewToController() {
        viewController.setViewMinX(viewMinX);
        viewController.setViewMinY(viewMinY);
        viewController.setZoomFactor(zoomFactor);
        viewController.setPanelSize(getWidth(), getHeight());
    }

    public Envelope getCurrentViewEnvelope() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return null;
        }
        return new Envelope(
                screenToWorldX(0),
                screenToWorldX(getWidth()),
                screenToWorldY(getHeight()),
                screenToWorldY(0)
        );
    }

    public void restoreView(double viewMinX, double viewMinY, double zoomFactor) {
        if (zoomFactor <= 0) {
            return;
        }

        this.viewMinX = viewMinX;
        this.viewMinY = viewMinY;
        this.zoomFactor = zoomFactor;
        refreshStatusBarScale();
        repaint();
    }

    public void restoreViewOrReset(double savedViewMinX, double savedViewMinY, double savedZoomFactor, boolean hasSavedView) {
        if (hasSavedView) {
            restoreView(savedViewMinX, savedViewMinY, savedZoomFactor);
        } else {
            resetView();
            return;
        }

        Envelope current = getCurrentViewEnvelope();
        Envelope global = getGlobalEnvelope();
        if (global == null || global.isNull()) {
            repaint();
            return;
        }

        if (current == null
                || current.isNull()
                || !current.intersects(global)
                || current.getWidth() > global.getWidth() * 500
                || current.getHeight() > global.getHeight() * 500) {
            fitToEnvelope(global);
            rememberCurrentView();
            repaint();
        }
    }

    public BufferedImage renderMapViewImage(double renderViewMinX, double renderViewMinY, double renderZoomFactor) {
        int renderWidth = Math.max(1, getWidth());
        int renderHeight = Math.max(1, getHeight());
        if (renderWidth <= 1 || renderHeight <= 1) {
            renderWidth = 1200;
            renderHeight = 800;
        }
        return renderMapViewImage(renderViewMinX, renderViewMinY, renderZoomFactor, renderWidth, renderHeight);
    }

    public BufferedImage renderMapViewImage(double renderViewMinX, double renderViewMinY, double renderZoomFactor, int renderWidth, int renderHeight) {
        return renderMapViewImage(renderViewMinX, renderViewMinY, renderZoomFactor, renderWidth, renderHeight, false);
    }

    public BufferedImage renderMapViewImage(double renderViewMinX, double renderViewMinY, double renderZoomFactor, int renderWidth, int renderHeight, boolean includeDecorations) {
        if (renderZoomFactor <= 0) {
            return null;
        }

        renderWidth = Math.max(1, renderWidth);
        renderHeight = Math.max(1, renderHeight);

        double oldViewMinX = viewMinX;
        double oldViewMinY = viewMinY;
        double oldZoomFactor = zoomFactor;
        boolean oldLayoutRenderMode = layoutRenderMode;
        int oldWidth = getWidth();
        int oldHeight = getHeight();

        BufferedImage image = new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            if (oldWidth <= 1 || oldHeight <= 1) {
                setSize(renderWidth, renderHeight);
            }
            viewMinX = renderViewMinX;
            viewMinY = renderViewMinY;
            zoomFactor = renderZoomFactor;
            layoutRenderMode = !includeDecorations;
            paintComponent(g2);
            if (includeDecorations && mapDecorations != null) {
                String crsDesc = CatgisDesktopApp.currentProject != null
                        ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
                mapDecorations.render(g2, renderWidth, renderHeight,
                        getCurrentViewEnvelope(),
                        getCurrentScaleDenominator(),
                        crsDesc);
            }
            return image;
        } finally {
            g2.dispose();
            viewMinX = oldViewMinX;
            viewMinY = oldViewMinY;
            zoomFactor = oldZoomFactor;
            layoutRenderMode = oldLayoutRenderMode;
            if (oldWidth <= 1 || oldHeight <= 1) {
                setSize(oldWidth, oldHeight);
            }
        }
    }

    public void zoomToLayer(Layer layer) {
        if (layer == null) {
            return;
        }

        ShapefileData data = shapefileLayers.get(layer);
        LocalRasterData raster = rasterLayers.get(layer);
        OnlineRasterSource online = onlineTileLayers.get(layer);
        OnlineWmsLayer onlineWms = onlineWmsLayers.get(layer);
        if (data == null && raster == null && online == null && onlineWms == null) {
            JOptionPane.showMessageDialog(this, "La capa no tiene datos cargados en el mapa.");
            return;
        }

        layer.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            rememberCurrentView();
            Envelope env = data != null
                    ? getLayerEnvelope(layer, data)
                    : (raster != null
                    ? getRasterEnvelope(layer, raster)
                    : (online != null ? getOnlineLayerEnvelope((OnlineTileLayer) layer) : getOnlineWmsEnvelope(onlineWms)));

            if (env == null || env.isNull()) {
                JOptionPane.showMessageDialog(this, "No se pudo calcular la extensiÃ³n de la capa.");
                return;
            }

            fitToEnvelope(env);
            rememberCurrentView();
            repaint();
        });
    }

    public void moveLayerUp(Layer layer) {
        layerManager.moveLayerUp(layer);
    }

    public void moveLayerDown(Layer layer) {
        layerManager.moveLayerDown(layer);
    }

    public void reorderLayers(List<Layer> orderedLayers) {
        layerManager.reorderLayers(orderedLayers);
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
        sb.append("Visible: ").append(layer.isVisible() ? "SÃ­" : "No").append("\n");
        sb.append("SourceName: ").append(layer.getSourceName() != null ? layer.getSourceName() : "-").append("\n");
        sb.append("FeatureCount: ").append(layer.getFeatureCount()).append("\n");
        sb.append("CRS origen: ").append(layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank() ? layer.getSourceCRS() : "desconocido").append("\n");
        sb.append("CRS proyecto: ").append(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "-").append("\n");
        LocalRasterData rasterDataInfo = rasterLayers.get(layer);
        if (rasterDataInfo != null) {
            sb.append("Raster: SÃ­\n");
            sb.append("Bandas: ").append(rasterDataInfo.getBandCount()).append("\n");
            sb.append("Georreferenciado: ").append(rasterDataInfo.isGeoreferenced() ? "SÃ­" : "No").append("\n");
        }
        sb.append("Etiquetas: ").append(layer.isLabelsVisible() ? "SÃ­" : "No").append("\n");
        sb.append("Campo etiqueta: ").append(layer.getLabelField() != null ? layer.getLabelField() : "-").append("\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "InformaciÃ³n de capa", JOptionPane.INFORMATION_MESSAGE);
    }

    boolean isFeatureVisibleInLayer(Layer layer, SimpleFeature feature) {
        return CadLayerSupport.isCadFeatureVisible(layer, feature);
    }

    Geometry reprojectGeometryIfNeeded(Layer layer, Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return geometry;
        }

        try {
            String sourceCode = layer != null ? layer.getSourceCRS() : "";
            String targetCode = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";

            Geometry resolvedGeometry = geometry;
            if (sourceCode == null || sourceCode.isBlank()) {
                return CadPlacementSupport.applyPlacement(layer, geometry);
            }
            if (targetCode == null || targetCode.isBlank()) {
                return CadPlacementSupport.applyPlacement(layer, geometry);
            }
            if (!sourceCode.equalsIgnoreCase(targetCode)) {
                Geometry manualGeometry = CoordinateTransformSupport.reprojectGeometry(geometry, sourceCode, targetCode);
                if (manualGeometry != null) {
                    resolvedGeometry = manualGeometry;
                } else {
                    CoordinateReferenceSystem sourceCRS = CRSDefinitions.decode(sourceCode, true);
                    CoordinateReferenceSystem targetCRS = CRSDefinitions.decode(targetCode, true);
                    MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
                    resolvedGeometry = JTS.transform(geometry, transform);
                }
            }
            return CadPlacementSupport.applyPlacement(layer, resolvedGeometry);
        } catch (Exception ex) {
            return CadPlacementSupport.applyPlacement(layer, geometry);
        }
    }

    Envelope reprojectEnvelopeIfNeeded(Layer layer, Envelope env) {
        String sourceCode = layer != null ? layer.getSourceCRS() : "";
        String targetCode = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        return CadPlacementSupport.applyPlacement(layer, reprojectEnvelopeIfNeeded(env, sourceCode, targetCode));
    }

    Envelope reprojectEnvelopeIfNeeded(Envelope env, String sourceCode, String targetCode) {
        return utilities.reprojectEnvelopeIfNeeded(env, sourceCode, targetCode);
    }

    double[] transformPoint(double x, double y, String sourceCode, String targetCode) {
        return utilities.transformPoint(x, y, sourceCode, targetCode);
    }

    void showMapPopup(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (isDrawingActive()) {
            JPopupMenu popupMenu = new JPopupMenu();

            if (!drawingCoordinates.isEmpty()) {
                JMenuItem finishEntityItem = new JMenuItem("Cerrar entidad actual");
                finishEntityItem.addActionListener(ev -> finishCurrentDrawing());
                popupMenu.add(finishEntityItem);
            }

            JMenuItem closeItem = new JMenuItem("Cerrar dibujo...");
            closeItem.addActionListener(ev -> closeCurrentDrawingSession());
            popupMenu.add(closeItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar dibujo");
            cancelItem.addActionListener(ev -> cancelCurrentDrawing());
            popupMenu.add(cancelItem);

            popupMenu.show(this, x, y);
            return;
        }

        if (isMeasurementActive()) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem finishItem = new JMenuItem("Terminar mediciÃ³n");
            finishItem.addActionListener(ev -> finishCurrentMeasurement());
            popupMenu.add(finishItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar mediciÃ³n");
            cancelItem.addActionListener(ev -> cancelCurrentMeasurement());
            popupMenu.add(cancelItem);

            popupMenu.show(this, x, y);
            return;
        }

        if (featureEditMode && isFeatureEditSketchMode() && !featureEditSketchCoordinates.isEmpty()) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem finishItem = new JMenuItem("Aplicar ediciÃ³n");
            finishItem.addActionListener(ev -> applyFeatureEditSketchOperationEnhanced());
            popupMenu.add(finishItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar boceto");
            cancelItem.addActionListener(ev -> {
                featureEditSketchCoordinates.clear();
                repaint();
                showCopiedMessage("Boceto de ediciÃ³n cancelado.");
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

        JMenuItem addPinItem = new JMenuItem("Agregar pin aquÃ­");
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
        boolean readOnlyLayer = isReadOnlyVectorLayer(layer);

        JMenuItem editItem = new JMenuItem(readOnlyLayer ? "Capa en solo lectura" : "Editar vector");
        editItem.setEnabled(!readOnlyLayer);
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

        if (linearOrPolygonal && !readOnlyLayer) {
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

        if (!readOnlyLayer && (geometry instanceof Polygon || geometry instanceof MultiPolygon)) {
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

        JMenuItem attrItem = new JMenuItem(readOnlyLayer ? "Ver atributos" : "Editar atributos");
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
        pinManager.showPinPopup(e, pin);
    }

    PinMarker addPin(double x, double y) {
        return pinManager.addPin(x, y);
    }

    void removePin(PinMarker pin) {
        pinManager.removePin(pin);
    }

    void clearAllPins() {
        pinManager.clearAllPins();
    }

    void convertPinsToLayer() {
        pinManager.convertPinsToLayer();
    }

    PinMarker findPinAtScreen(int mouseX, int mouseY) {
        return pinManager.findPinAtScreen(mouseX, mouseY);
    }

    void showPinDialog(PinMarker pin) {
        pinManager.showPinDialog(pin);
    }

    void copyPinLatLon(PinMarker pin, boolean dms) {
        pinManager.copyPinLatLon(pin, dms);
    }

    void updateStatusCoordinates(int screenX, int screenY) {
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

        // Update map decorations cursor coordinate
        if (mapDecorations != null) {
            mapDecorations.setCursorCoordinate(geographicText);
        }
    }

    void showCoordinateDialog(int screenX, int screenY) {
        utilities.showCoordinateDialog(screenX, screenY);
    }

    String toDms(double value, boolean latitude) {
        return MapUtilities.toDms(value, latitude);
    }

    String formatNumber(double value) {
        return MapUtilities.formatNumber(value);
    }

    private String formatScaleDenominator(double denominator) {
        if (denominator <= 0d) {
            return "";
        }
        return "1:" + new DecimalFormat("#,##0").format(Math.round(denominator));
    }

    private String buildScaleTooltip(double denominator) {
        if (denominator <= 0d) {
            return "Escala no disponible hasta que haya una vista cartografica valida.";
        }
        if (isGeographicProjectCrs()) {
            return "Escala actual aproximada para la vista. En CRS geograficos se estima segun la latitud central.";
        }
        return "Escala actual de la vista principal. Escribe 1:5000 o 5000 y presiona Enter para ajustarla.";
    }

    private Double parseScaleDenominator(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
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
            return denominator > 0d ? denominator : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean hasLoadedMapContent() {
        return !shapefileLayers.isEmpty()
                || !rasterLayers.isEmpty()
                || !onlineTileLayers.isEmpty()
                || !onlineWmsLayers.isEmpty();
    }

    private int safeScreenDpi() {
        try {
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            return dpi > 0 ? dpi : 96;
        } catch (Exception ex) {
            return 96;
        }
    }

    private double estimateMetersPerProjectUnit() {
        String projectCrs = CatgisDesktopApp.currentProject != null
                ? CRSDefinitions.normalizeCode(CatgisDesktopApp.currentProject.getProjectCRS())
                : "";
        if (projectCrs == null || projectCrs.isBlank()) {
            return 0d;
        }
        if (isGeographicProjectCrs()) {
            double centerX = viewMinX + (Math.max(1, getWidth()) / (2d * Math.max(zoomFactor, 0.000001d)));
            double centerY = viewMinY + (Math.max(1, getHeight()) / (2d * Math.max(zoomFactor, 0.000001d)));
            double[] geographic = transformPoint(centerX, centerY, projectCrs, "EPSG:4326");
            double centerLat = geographic != null && geographic.length >= 2 ? geographic[1] : centerY;
            double metersPerDegreeLon = 111320d * Math.cos(Math.toRadians(centerLat));
            return Math.max(1d, Math.abs(metersPerDegreeLon));
        }
        return 1d;
    }

    private boolean isGeographicProjectCrs() {
        String projectCrs = CatgisDesktopApp.currentProject != null
                ? CRSDefinitions.normalizeCode(CatgisDesktopApp.currentProject.getProjectCRS())
                : "";
        return "EPSG:4326".equalsIgnoreCase(projectCrs)
                || "EPSG:4258".equalsIgnoreCase(projectCrs)
                || "EPSG:4269".equalsIgnoreCase(projectCrs)
                || "EPSG:4674".equalsIgnoreCase(projectCrs)
                || "EPSG:4190".equalsIgnoreCase(projectCrs)
                || "EPSG:4221".equalsIgnoreCase(projectCrs);
    }

    void copyToClipboard(String text) {
        MapUtilities.copyToClipboard(text);
    }

    public void showCopiedMessage(String message) {
        utilities.showCopiedMessage(message);
    }

    /* package-private */ void fitToAllLayers() {
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
                JOptionPane.showMessageDialog(this, "No hay capas cargadas para calcular la extensiÃ³n.");
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
        refreshStatusBarScale();
    }

    private Envelope getLayerEnvelope(Layer layer, ShapefileData data) {
        return utilities.getLayerEnvelope(layer, data);
    }

    private Envelope buildCadDisplayEnvelope(Layer layer, ShapefileData data) {
        return utilities.buildCadDisplayEnvelope(layer, data);
    }

    private double percentile(List<Double> values, double quantile) {
        if (values == null || values.isEmpty()) {
            return 0d;
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        if (sorted.size() == 1) {
            return sorted.get(0);
        }
        double clamped = Math.max(0d, Math.min(1d, quantile));
        double index = clamped * (sorted.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) {
            return sorted.get(lower);
        }
        double fraction = index - lower;
        return sorted.get(lower) + ((sorted.get(upper) - sorted.get(lower)) * fraction);
    }

    private Envelope getGlobalEnvelope() {
        return utilities.getGlobalEnvelope();
    }

    private Envelope getOnlineLayerEnvelope(OnlineTileLayer layer) {
        if (layer == null) {
            return null;
        }
        Envelope world = new Envelope(OnlineMapUtils.WEB_MERCATOR_WORLD);
        return reprojectEnvelopeIfNeeded(world, "EPSG:3857",
                CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "");
    }

    private Envelope getOnlineWmsEnvelope(OnlineWmsLayer layer) {
        if (layer == null) {
            return null;
        }
        if (Double.isNaN(layer.getExtentMinX()) || Double.isNaN(layer.getExtentMinY())
                || Double.isNaN(layer.getExtentMaxX()) || Double.isNaN(layer.getExtentMaxY())) {
            Envelope world = new Envelope(OnlineMapUtils.WEB_MERCATOR_WORLD);
            return reprojectEnvelopeIfNeeded(world, "EPSG:3857",
                    CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "");
        }
        Envelope env = new Envelope(layer.getExtentMinX(), layer.getExtentMaxX(), layer.getExtentMinY(), layer.getExtentMaxY());
        return reprojectEnvelopeIfNeeded(env, layer.getExtentCrs(),
                CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "");
    }

    void handleZoom(MouseWheelEvent e) {
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

        refreshStatusBarScale();
        repaint();
    }

    void identifyFeature(int screenX, int screenY) {
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
            showCopiedMessage("No se identificÃ³ ninguna entidad.");
            JOptionPane.showMessageDialog(this, "No se identificÃ³ ninguna entidad.");
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
        showCopiedMessage(hits.size() + " entidades identificadas. ElegÃ­ una en la ventana de resultados.");
        IdentifyResultsDialog.open(this, hits);
    }

    void selectFeatureForEditing(int screenX, int screenY, boolean additiveSelection) {
        List<IdentifyResultItem> hits = collectSelectableResults(screenX, screenY);

        if (hits.isEmpty()) {
            if (!additiveSelection) {
                clearSelectedFeature();
                showCopiedMessage("No se seleccionÃ³ ninguna entidad.");
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
                : "Entidad seleccionada para ediciÃ³n: " + targetLayer.getName();
        applyFeatureSelection(targetLayer, selectionIds, !additiveSelection, true, true, message);
    }

    void selectFeatureForEditing(Rectangle selectionBounds, boolean additiveSelection) {
        List<IdentifyResultItem> hits = collectSelectableResults(selectionBounds);

        if (hits.isEmpty()) {
            if (!additiveSelection) {
                clearSelectedFeature();
                showCopiedMessage("No se encontrÃ³ ninguna entidad dentro del rectÃ¡ngulo.");
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

    List<IdentifyResultItem> collectIdentifyResults(int screenX, int screenY) {
        List<IdentifyResultItem> hits = new ArrayList<>();

        double tolerancePixels = 6.0;
        double toleranceWorld = tolerancePixels / zoomFactor;

        double worldX = screenToWorldX(screenX);
        double worldY = screenToWorldY(screenY);

        Point clickPoint = new org.locationtech.jts.geom.GeometryFactory()
                .createPoint(new Coordinate(worldX, worldY));

        collectPointProbeHits(layerManager.getHitTestLayers(false), clickPoint, toleranceWorld, hits, "Error durante identificacion puntual sobre capa ");

        return hits;
    }

    private List<IdentifyResultItem> collectSelectableResults(int screenX, int screenY) {
        List<IdentifyResultItem> hits = new ArrayList<>();

        double tolerancePixels = 6.0;
        double toleranceWorld = tolerancePixels / zoomFactor;

        double worldX = screenToWorldX(screenX);
        double worldY = screenToWorldY(screenY);

        Point clickPoint = selectionGeometryFactory.createPoint(new Coordinate(worldX, worldY));

        collectPointProbeHits(layerManager.getHitTestLayers(true), clickPoint, toleranceWorld, hits, "Error durante seleccion puntual sobre capa ");

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

        forEachVisibleFeatureGeometry(layerManager.getHitTestLayers(true), "Error durante seleccion por rectangulo sobre capa ", (layer, featureGeometry) -> {
            if (featureGeometry.geometry().intersects(selectionArea) || selectionArea.contains(featureGeometry.geometry())) {
                hits.add(new IdentifyResultItem(layer, featureGeometry.feature(), featureGeometry.geometry()));
            }
        });

        sortSelectableHits(hits, selectionArea.getCentroid());
        return hits;
    }

    private void collectPointProbeHits(List<Layer> layers,
                                       Point clickPoint,
                                       double toleranceWorld,
                                       List<IdentifyResultItem> hits,
                                       String failurePrefix) {
        forEachVisibleFeatureGeometry(layers, failurePrefix, (layer, featureGeometry) -> {
            Geometry geometry = featureGeometry.geometry();
            boolean hit = geometry instanceof Point || geometry instanceof MultiPoint
                    ? geometry.isWithinDistance(clickPoint, toleranceWorld)
                    : geometry.buffer(toleranceWorld).contains(clickPoint);
            if (hit) {
                hits.add(new IdentifyResultItem(layer, featureGeometry.feature(), geometry));
            }
        });
    }

    void forEachVisibleFeatureGeometry(List<Layer> layers,
                                               String failurePrefix,
                                               BiConsumer<Layer, FeatureGeometryRef> consumer) {
        if (layers == null || consumer == null) {
            return;
        }
        for (Layer layer : layers) {
            if (!layerManager.isLayerEffectivelyVisible(layer)) {
                continue;
            }

            ShapefileData data = shapefileLayers.get(layer);
            if (data == null || data.getFeatureCollection() == null) {
                continue;
            }

            try (FeatureIterator<SimpleFeature> iterator = data.getFeatureCollection().features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    if (!isFeatureVisibleInLayer(layer, feature)) {
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

                    consumer.accept(layer, new FeatureGeometryRef(feature, geometry));
                }
            } catch (Exception ex) {
                AppErrorSupport.logFailure((failurePrefix != null ? failurePrefix : "Error de capa ") + layer.getName(), ex);
            }
        }
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

    boolean isHitOnCurrentSelection(int screenX, int screenY) {
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
        return layerManager.getHitTestLayers(preferEditingLayer);
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
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        movingSelectedFeatures = false;
        moveSelectionLastProjectX = Double.NaN;
        moveSelectionLastProjectY = Double.NaN;
        editUndoStack.clear();
        editRedoStack.clear();
        refreshEditingUi();
    }

    public void clearSelectedFeature() {
        if (!confirmPendingFeatureEdit("limpiar la selecciÃ³n")) {
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

    public boolean cutSelectedFeatures() {
        if (selectedLayer == null) {
            return false;
        }

        List<String> selectedIds = getSelectedFeatureIdsForLayer(selectedLayer);
        if (selectedIds.isEmpty()) {
            return false;
        }

        copySelectedFeatures();
        boolean deleted = deleteSelectedFeatures();
        if (deleted) {
            showCopiedMessage(selectedIds.size() == 1 ? "Entidad cortada." : selectedIds.size() + " entidades cortadas.");
        }
        return deleted;
    }

    public void copySelectedFeatures() {
        captureSelectedFeaturesForCopy(false);
    }

    public boolean copySelectedFeaturesToEditingLayer() {
        if (selectedLayer == null) {
            return false;
        }

        Layer targetLayer = getEditingLayerRef();
        if (targetLayer == null && selectedLayer != null && !(selectedLayer instanceof RasterLayer)) {
            prepareLayerForEditing(selectedLayer);
            targetLayer = getEditingLayerRef();
        }
        if (targetLayer == null) {
            return false;
        }
        if (isReadOnlyVectorLayer(targetLayer)) {
            JOptionPane.showMessageDialog(this, getReadOnlyLayerMessage(targetLayer));
            return false;
        }

        captureSelectedFeaturesForCopy(true);
        if (!hasCopiedFeature()) {
            return false;
        }

        return pasteCopiedFeatures();
    }

    public boolean copySelectedFeaturesFromLayerToEditingLayer(Layer sourceLayer) {
        if (sourceLayer == null || sourceLayer instanceof RasterLayer) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una capa vectorial con entidades seleccionadas.",
                    "Copiar a capa en edicion",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        Layer targetLayer = getEditingLayerRef();
        if (targetLayer == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay una capa vectorial en edicion activa. Primero activa Editar vector en la capa destino.",
                    "Copiar a capa en edicion",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if (sourceLayer == targetLayer) {
            JOptionPane.showMessageDialog(this,
                    "La capa seleccionada ya es la capa en edicion. Usa copiar/pegar normal para duplicar dentro de la misma capa.",
                    "Copiar a capa en edicion",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if (isReadOnlyVectorLayer(targetLayer)) {
            JOptionPane.showMessageDialog(this, getReadOnlyLayerMessage(targetLayer));
            return false;
        }

        ShapefileData sourceData = getShapefileData(sourceLayer);
        ShapefileData targetData = getShapefileData(targetLayer);
        if (sourceData == null || sourceData.getSchema() == null || targetData == null || targetData.getSchema() == null) {
            JOptionPane.showMessageDialog(this,
                    "La capa fuente o destino no tiene esquema vectorial disponible.",
                    "Copiar a capa en edicion",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        String sourceFamily = resolveGeometryFamily(sourceData.getSchema());
        String targetFamily = resolveGeometryFamily(targetData.getSchema());
        if (sourceFamily.isBlank() || !sourceFamily.equals(targetFamily)) {
            JOptionPane.showMessageDialog(this,
                    "La geometria no es compatible. Fuente: " + sourceFamily + " | Destino: " + targetFamily + ".",
                    "Copiar a capa en edicion",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        List<String> selectedIds = getSelectedFeatureIdsForLayer(sourceLayer);
        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay entidades seleccionadas en la capa fuente.",
                    "Copiar a capa en edicion",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        List<SimpleFeature> targetFeatures = new ArrayList<>(targetData.getFeatures());
        List<String> pastedIds = new ArrayList<>();
        for (SimpleFeature sourceFeature : collectSelectedFeatures(sourceData.getFeatures(), selectedIds)) {
            SimpleFeature pasted = buildPastedFeature(sourceFeature, targetLayer, targetFeatures, false);
            if (pasted == null) {
                continue;
            }
            targetFeatures.add(pasted);
            pastedIds.add(pasted.getID());
        }

        if (pastedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron copiar las entidades seleccionadas a la capa en edicion.",
                    "Copiar a capa en edicion",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        pushUndoSnapshot(targetLayer, null);
        replaceLayerFeatures(targetLayer, targetFeatures, pastedIds.get(0), pastedIds.size() == 1, null);
        applyFeatureSelection(targetLayer, pastedIds, pastedIds.size() == 1, true, false,
                pastedIds.size() == 1
                        ? "Entidad copiada a la capa en edicion."
                        : pastedIds.size() + " entidades copiadas a la capa en edicion.");
        return true;
    }

    private void captureSelectedFeaturesForCopy(boolean silent) {
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
        if (!silent) {
            showCopiedMessage(copiedFeatures.size() == 1 ? "Entidad copiada." : copiedFeatures.size() + " entidades copiadas.");
        }
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
        if (isReadOnlyVectorLayer(targetLayer)) {
            JOptionPane.showMessageDialog(this, getReadOnlyLayerMessage(targetLayer));
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
            SimpleFeature pasted = buildPastedFeature(sourceFeature, targetLayer, features, true);
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

    public boolean canMergeSelectedFeatures() {
        if (selectedLayer == null || isReadOnlyVectorLayer(selectedLayer) || getSelectedFeatureCount() < 2) {
            return false;
        }

        ShapefileData data = getShapefileData(selectedLayer);
        String family = resolveGeometryFamily(data != null ? data.getSchema() : null);
        return "LINE".equals(family) || "POLYGON".equals(family);
    }

    public boolean mergeSelectedFeatures() {
        if (!canMergeSelectedFeatures()) {
            return false;
        }

        ShapefileData data = getShapefileData(selectedLayer);
        List<String> selectedIds = getSelectedFeatureIdsForLayer(selectedLayer);
        if (data == null || selectedIds.size() < 2) {
            return false;
        }

        List<SimpleFeature> selectedFeatures = collectSelectedFeatures(data.getFeatures(), selectedIds);
        if (selectedFeatures.size() < 2) {
            return false;
        }

        String family = resolveGeometryFamily(data.getSchema());
        Geometry mergedGeometry = buildMergedGeometry(selectedFeatures, family);
        if (mergedGeometry == null || mergedGeometry.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudieron unir las entidades seleccionadas.",
                    "Unir elementos",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return false;
        }

        List<SimpleFeature> replacementFeatures = buildFeaturesForMergedGeometry(selectedFeatures.get(0), mergedGeometry, data.getSchema());
        if (replacementFeatures.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "La geometria resultante no es compatible con la capa actual.",
                    "Unir elementos",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return false;
        }

        pushUndoSnapshotForSelectedLayer();
        List<SimpleFeature> updatedFeatures = replaceFeaturesBySelection(data.getFeatures(), selectedIds, replacementFeatures);
        List<String> resultIds = extractFeatureIds(replacementFeatures);
        replaceLayerFeatures(selectedLayer, updatedFeatures, resultIds.get(0), resultIds.size() == 1, null);
        applyFeatureSelection(
                selectedLayer,
                resultIds,
                resultIds.size() == 1,
                true,
                false,
                resultIds.size() == 1 ? "Elementos unidos." : resultIds.size() + " entidades resultantes tras unir."
        );
        return true;
    }

    public boolean canExplodeSelectedFeatures() {
        if (selectedLayer == null || isReadOnlyVectorLayer(selectedLayer)) {
            return false;
        }

        ShapefileData data = getShapefileData(selectedLayer);
        List<String> selectedIds = getSelectedFeatureIdsForLayer(selectedLayer);
        if (data == null || selectedIds.isEmpty()) {
            return false;
        }

        for (SimpleFeature feature : collectSelectedFeatures(data.getFeatures(), selectedIds)) {
            if (geometryPartCount(extractFeatureGeometryCopy(feature)) > 1) {
                return true;
            }
        }
        return false;
    }

    public boolean explodeSelectedFeatures() {
        if (selectedLayer == null) {
            return false;
        }
        if (isReadOnlyVectorLayer(selectedLayer)) {
            JOptionPane.showMessageDialog(this, getReadOnlyLayerMessage(selectedLayer));
            return false;
        }

        ShapefileData data = getShapefileData(selectedLayer);
        List<String> selectedIds = getSelectedFeatureIdsForLayer(selectedLayer);
        if (data == null || selectedIds.isEmpty()) {
            return false;
        }

        List<SimpleFeature> updatedFeatures = new ArrayList<>();
        List<String> resultIds = new ArrayList<>();
        boolean changed = false;
        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null) {
                continue;
            }

            if (!selectedIds.contains(feature.getID())) {
                updatedFeatures.add(feature);
                continue;
            }

            List<Geometry> parts = collectGeometryParts(extractFeatureGeometryCopy(feature));
            if (parts.size() <= 1) {
                updatedFeatures.add(feature);
                resultIds.add(feature.getID());
                continue;
            }

            List<SimpleFeature> replacementFeatures = buildReplacementFeatures(feature, parts);
            if (replacementFeatures.isEmpty()) {
                updatedFeatures.add(feature);
                resultIds.add(feature.getID());
                continue;
            }

            updatedFeatures.addAll(replacementFeatures);
            resultIds.addAll(extractFeatureIds(replacementFeatures));
            changed = true;
        }

        if (!changed) {
            JOptionPane.showMessageDialog(
                    this,
                    "La seleccion no contiene entidades multiparte para explotar.",
                    "Explotar entidades",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return false;
        }

        pushUndoSnapshotForSelectedLayer();
        replaceLayerFeatures(selectedLayer, updatedFeatures, resultIds.get(0), resultIds.size() == 1, null);
        applyFeatureSelection(
                selectedLayer,
                resultIds,
                resultIds.size() == 1,
                true,
                false,
                resultIds.size() == 1 ? "Entidad explotada." : resultIds.size() + " entidades resultantes tras explotar."
        );
        return true;
    }

    public boolean deleteSelectedFeatures() {
        if (selectedLayer == null) {
            return false;
        }
        if (isReadOnlyVectorLayer(selectedLayer)) {
            JOptionPane.showMessageDialog(this, getReadOnlyLayerMessage(selectedLayer));
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

    private String getReadOnlyLayerMessage(Layer layer) {
        String reason = VectorLayerUtils.getReadOnlyVectorLayerReason(layer);
        return !reason.isBlank() ? reason : "La capa seleccionada esta en modo lectura.";
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

    public void zoomToGeometry(Geometry geometry, String sourceCrs) {
        Geometry displayGeometry = adaptExternalGeometryToProject(geometry, sourceCrs);
        if (displayGeometry == null || displayGeometry.isEmpty()) {
            return;
        }

        Envelope env = displayGeometry.getEnvelopeInternal();
        if (env == null || env.isNull()) {
            return;
        }

        double expandX = Math.max(env.getWidth() * 0.15, 10.0 / Math.max(zoomFactor, 0.000001));
        double expandY = Math.max(env.getHeight() * 0.15, 10.0 / Math.max(zoomFactor, 0.000001));
        env.expandBy(expandX, expandY);
        fitToEnvelope(env);
        flashGeometry(displayGeometry, null);
        repaint();
    }

    public void flashGeometry(Geometry geometry, String sourceCrs) {
        Geometry displayGeometry = adaptExternalGeometryToProject(geometry, sourceCrs);
        if (displayGeometry == null || displayGeometry.isEmpty()) {
            return;
        }
        selectionFlashGeometry = displayGeometry;
        selectionFlashStartedAt = System.currentTimeMillis();
        if (!selectionFlashTimer.isRunning()) {
            selectionFlashTimer.start();
        }
        repaint();
    }

    private Geometry adaptExternalGeometryToProject(Geometry geometry, String sourceCrs) {
        if (geometry == null || geometry.isEmpty()) {
            return geometry;
        }
        String targetCrs = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        if (sourceCrs == null || sourceCrs.isBlank() || targetCrs == null || targetCrs.isBlank()
                || sourceCrs.equalsIgnoreCase(targetCrs)) {
            return geometry.copy();
        }
        Geometry reprojected = reprojectGeometry(geometry, sourceCrs, targetCrs);
        return reprojected != null ? reprojected : geometry.copy();
    }

    void showFeatureInfo(SimpleFeature feature, Layer layer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Capa: ").append(layer.getName()).append("\n\n");

        feature.getProperties().forEach(property -> {
            String name = property.getName().toString();
            Object value = property.getValue();
            if (!"the_geom".equalsIgnoreCase(name)) {
                sb.append(name).append(": ").append(value).append("\n");
            }
        });

        showScrollableInfoDialog("Identificar entidad", sb.toString());
    }

    private void showScrollableInfoDialog(String title, String content) {
        JTextArea infoArea = new JTextArea(content != null ? content : "");
        infoArea.setEditable(false);
        infoArea.setLineWrap(false);
        infoArea.setWrapStyleWord(false);
        infoArea.setCaretPosition(0);
        infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Math.max(12, infoArea.getFont().getSize())));

        JScrollPane scrollPane = new JScrollPane(
                infoArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);

        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int preferredWidth = Math.max(540, Math.min(940, screenSize.width - 140));
        int preferredHeight = Math.max(320, Math.min(620, screenSize.height - 180));
        scrollPane.setPreferredSize(new java.awt.Dimension(preferredWidth, preferredHeight));

        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        onlineResolutionNoticeVisible = false;
        onlineResolutionNotice = "";

        if (shapefileLayers.isEmpty() && rasterLayers.isEmpty() && onlineTileLayers.isEmpty() && onlineWmsLayers.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString(openedFileText, 20, 30);
        }

        for (Layer layer : layerManager.getRenderOrderLayers()) {
            if (layer == null || !layerManager.isLayerEffectivelyVisible(layer)) {
                continue;
            }

            OnlineRasterSource onlineSource = onlineTileLayers.get(layer);
            if (layer instanceof OnlineTileLayer && onlineSource != null) {
                drawOnlineTileLayer(g2, (OnlineTileLayer) layer, onlineSource);
                continue;
            }

            OnlineWmsLayer wmsLayer = onlineWmsLayers.get(layer);
            if (wmsLayer != null) {
                drawOnlineWmsLayer(g2, wmsLayer);
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
            }
        }

        // Batch label rendering with collision detection
        drawAllLabels(g2);

        // Heatmap overlay for point layers
        drawHeatmapOverlay(g2);

        // Point clusters
        drawPointClusters(g2);

        if (!layoutRenderMode) {
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
            drawTopographicProfileCapture(g2);
            drawSnapPreview(g2);
            drawSelectionBox(g2);
        }
        if (!layoutRenderMode) {
            drawOnlineAttribution(g2);
            // Map decorations on screen
            if (mapDecorations != null) {
                String crsDesc = CatgisDesktopApp.currentProject != null
                        ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
                mapDecorations.render(g2, getWidth(), getHeight(),
                        getCurrentViewEnvelope(),
                        getCurrentScaleDenominator(),
                        crsDesc);
            }
        }
        } finally {
            g2.dispose();
        }
    }

    private void drawPointClusters(Graphics2D g2) {
        OnlineLayerRenderer.drawPointClusters(this, g2);
    }

    /**
     * Write a world file (TFW/PGW/JGW) for a map view export.
     * The world file georeferences the exported image.
     */
    public void writeWorldFile(File imageFile) throws Exception {
        writeWorldFile(imageFile, viewMinX, viewMinY, zoomFactor, getWidth(), getHeight());
    }

    /**
     * Write a world file with explicit view parameters.
     */
    public static void writeWorldFile(File imageFile, double viewMinX, double viewMinY,
                                       double zoomFactor, int imgWidth, int imgHeight) throws Exception {
        if (zoomFactor <= 0 || imgWidth <= 0 || imgHeight <= 0) return;
        double pixelSizeX = 1.0 / zoomFactor;
        double pixelSizeY = pixelSizeX;
        double upperLeftX = viewMinX;
        double upperLeftY = viewMinY + imgHeight * pixelSizeY;
        WorldFileSupport.writeWorldFile(imageFile, new WorldFileSupport.WorldFileParams(
                pixelSizeX, 0.0, 0.0, -pixelSizeY, upperLeftX, upperLeftY));
    }

    private static String getBaseName(String fileName) {
        return MapGeometryUtils.getBaseName(fileName);
    }

    private void drawHeatmapOverlay(Graphics2D g2) {
        OnlineLayerRenderer.drawHeatmapOverlay(this, g2);
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

    public List<Layer> getRenderOrderLayers() {
        return layerManager.getRenderOrderLayers();
    }

    public boolean isLayerEffectivelyVisible(Layer layer) {
        return layerManager.isLayerEffectivelyVisible(layer);
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

    Coordinate resolveSelectionFlashCoordinate(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }

        try {
            Point focusPoint = geometry.getInteriorPoint();
            if (focusPoint != null && !focusPoint.isEmpty()) {
                return focusPoint.getCoordinate();
            }
        } catch (Exception ignored) { CatgisLogger.warn("Error al obtener punto interior de geometria", ignored); }

        try {
            Point centroid = geometry.getCentroid();
            if (centroid != null && !centroid.isEmpty()) {
                return centroid.getCoordinate();
            }
        } catch (Exception ignored) { CatgisLogger.warn("Error al obtener centroide de geometria", ignored); }

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

    private void drawOnlineTileLayer(Graphics2D g2, OnlineTileLayer layer, OnlineRasterSource source) {
        OnlineLayerRenderer.drawOnlineTileLayer(this, g2, layer, source);
    }

    OnlineTileFallback resolveFallbackOnlineTile(OnlineRasterSource source, int zoom, int x, int y) {
        return OnlineLayerRenderer.resolveFallbackOnlineTile(this, source, zoom, x, y);
    }

    private void drawOnlineWmsLayer(Graphics2D g2, OnlineWmsLayer layer) {
        OnlineLayerRenderer.drawOnlineWmsLayer(this, g2, layer);
    }

    String buildWmsGetMapUrl(OnlineWmsLayer layer) {
        return OnlineLayerRenderer.buildWmsGetMapUrl(this, layer);
    }

    private Envelope projectEnvelopeToCrs(Envelope projectEnvelope, String sourceCrs, String targetCrs) {
        return OnlineLayerRenderer.projectEnvelopeToCrs(this, projectEnvelope, sourceCrs, targetCrs);
    }

    private String buildWmsBbox(Envelope env, String requestCrs, String version) {
        return OnlineLayerRenderer.buildWmsBbox(env, requestCrs, version);
    }

    private String formatNumberForRequest(double value) {
        return OnlineLayerRenderer.formatNumberForRequest(value);
    }

    private String urlEncode(String text) {
        return OnlineLayerRenderer.urlEncode(text);
    }

    Envelope projectEnvelopeToMercator(Envelope projectEnvelope, String projectCRS) {
        return OnlineLayerRenderer.projectEnvelopeToMercator(this, projectEnvelope, projectCRS);
    }

    private void drawOnlineAttribution(Graphics2D g2) {
        OnlineLayerRenderer.drawOnlineAttribution(this, g2);
    }

    String buildVisibleOnlineAttribution() {
        return OnlineLayerRenderer.buildVisibleOnlineAttribution(this);
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

    BufferedImage getCachedDisplayImage(Layer layer, LocalRasterData data, RasterStyle style) {
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

    /* package-private */ void invalidateRasterDisplay(Layer layer) {
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

    Envelope getRasterEnvelope(Layer layer, LocalRasterData data) {
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
        if (layer instanceof RasterLayer rasterLayer && rasterLayer.isDerivedLayer()) {
            try {
                if (ProRasterDerivedService.supportsOperation(rasterLayer.getDerivedOperation())) {
                    return ProRasterDerivedService.regenerateDerivedRasterData(rasterLayer);
                }
                return TerrainHydrologyAnalysisService.regenerateDerivedRasterData(rasterLayer);
            } catch (Exception ex) {
                throw new java.io.IOException("No se pudo regenerar el raster derivado.", ex);
            }
        }

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
        layerManager.moveRasterUp(layer);
    }

    private void moveRasterDown(Layer layer) {
        layerManager.moveRasterDown(layer);
    }

    public static class RasterStyle {
        public float opacity = 1.0f;
        public boolean grayscale = false;
        public boolean autoContrast = true;
        public int redBand = 0;
        public int greenBand = 1;
        public int blueBand = 2;
        /** Custom colormap for single-band climate/environmental data. If non-null, used in place of grayscale band rendering. */
        public Color[] customColorMap = null;
        /** Min value for custom colormap stretch (e.g., -10 for temperature). */
        public double colorMapMin = 0;
        /** Max value for custom colormap stretch (e.g., 45 for temperature). */
        public double colorMapMax = 100;
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
        if (!isDrawingActive()) {
            return;
        }

        drawPendingDrawingSessionGeometries(g2);

        if ("CONTINUE_LINE".equalsIgnoreCase(drawingMode) && !drawingContinuationEndpointChosen) {
            drawContinuationEndpointHints(g2);
            return;
        }

        Coordinate previewCoordinate = resolveInteractivePreviewCoordinate();

        if ("POINT".equalsIgnoreCase(drawingMode) || "MULTIPOINT".equalsIgnoreCase(drawingMode)) {
            for (Coordinate c : drawingCoordinates) {
                int x = worldToScreenX(c.x);
                int y = worldToScreenY(c.y);
                g2.setColor(Color.MAGENTA);
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawOval(x - 5, y - 5, 10, 10);
            }

            if (previewCoordinate != null) {
                int x = worldToScreenX(previewCoordinate.x);
                int y = worldToScreenY(previewCoordinate.y);
                g2.setColor(new Color(255, 0, 255, 120));
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawOval(x - 5, y - 5, 10, 10);
            }
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(drawingCoordinates);
        if (previewCoordinate != null) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }

        if ("CIRCLE".equalsIgnoreCase(drawingMode) || "CIRCLE_3P".equalsIgnoreCase(drawingMode)) {
            Geometry previewGeometry = "CIRCLE".equalsIgnoreCase(drawingMode)
                    ? buildCircleGeometry(tempCoords)
                    : buildCircleThreePointGeometry(tempCoords);
            if (previewGeometry != null) {
                drawPendingDrawingGeometry(g2, previewGeometry);
                return;
            }
        }

        if ("RECTANGLE".equalsIgnoreCase(drawingMode)) {
            tempCoords = buildRectangleCoordinates(tempCoords);
        }

        if (tempCoords.isEmpty()) {
            return;
        }

        drawTemporaryGeometry(
                g2,
                tempCoords,
                "RECTANGLE".equalsIgnoreCase(drawingMode) ? "POLYGON" : drawingMode,
                Color.MAGENTA,
                new Color(255, 0, 255, 40)
        );
    }

    private void drawPendingDrawingSessionGeometries(Graphics2D g2) {
        if (pendingDrawingSessionGeometries.isEmpty()) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            for (Geometry geometry : pendingDrawingSessionGeometries) {
                drawPendingDrawingGeometry(copy, geometry);
            }
        } finally {
            copy.dispose();
        }
    }

    private void drawPendingDrawingGeometry(Graphics2D g2, Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        if (geometry instanceof Point) {
            drawPoint(g2, (Point) geometry, Color.MAGENTA, 10);
            return;
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint multiPoint = (MultiPoint) geometry;
            for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
                Geometry child = multiPoint.getGeometryN(i);
                if (child instanceof Point) {
                    drawPoint(g2, (Point) child, Color.MAGENTA, 10);
                }
            }
            return;
        }

        if (geometry instanceof LineString) {
            drawLineString(g2, (LineString) geometry, Color.MAGENTA, 2.2f);
            return;
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multiLineString = (MultiLineString) geometry;
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                Geometry child = multiLineString.getGeometryN(i);
                if (child instanceof LineString) {
                    drawLineString(g2, (LineString) child, Color.MAGENTA, 2.2f);
                }
            }
            return;
        }

        if (geometry instanceof Polygon) {
            drawPolygon(g2, (Polygon) geometry, new Color(255, 0, 255, 40), Color.MAGENTA, 2f);
            return;
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Geometry child = multiPolygon.getGeometryN(i);
                if (child instanceof Polygon) {
                    drawPolygon(g2, (Polygon) child, new Color(255, 0, 255, 40), Color.MAGENTA, 2f);
                }
            }
        }
    }

    private void drawContinuationEndpointHints(Graphics2D g2) {
        if (!"CONTINUE_LINE".equalsIgnoreCase(drawingMode)
                || drawingContinuationBaseCoordinates == null
                || drawingContinuationBaseCoordinates.length < 2
                || drawingContinuationLayer == null) {
            return;
        }

        Coordinate start = toProjectCoordinate(drawingContinuationBaseCoordinates[0], drawingContinuationLayer);
        Coordinate end = toProjectCoordinate(
                drawingContinuationBaseCoordinates[drawingContinuationBaseCoordinates.length - 1],
                drawingContinuationLayer
        );
        if (start == null || end == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawContinuationEndpointHint(copy, start, "A");
            drawContinuationEndpointHint(copy, end, "B");
        } finally {
            copy.dispose();
        }
    }

    private void drawContinuationEndpointHint(Graphics2D g2, Coordinate coordinate, String label) {
        int x = worldToScreenX(coordinate.x);
        int y = worldToScreenY(coordinate.y);

        g2.setColor(new Color(37, 99, 235, 58));
        g2.fillOval(x - 12, y - 12, 24, 24);
        g2.setColor(new Color(29, 78, 216));
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawOval(x - 9, y - 9, 18, 18);
        g2.setColor(Color.WHITE);
        g2.fillOval(x - 4, y - 4, 8, 8);
        g2.setColor(new Color(29, 78, 216));
        g2.drawString(label, x + 10, y - 10);
    }

    private void drawCurrentMeasurement(Graphics2D g2) {
        if (!isMeasurementActive() || measurementCoordinates.isEmpty()) {
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(measurementCoordinates);
        Coordinate previewCoordinate = resolveInteractivePreviewCoordinate();
        if (previewCoordinate != null) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }

        drawTemporaryGeometry(g2, tempCoords, measurementMode, Color.CYAN, new Color(0, 255, 255, 40));
    }

    private void drawFeatureEditSketch(Graphics2D g2) {
        if (!featureEditMode) {
            return;
        }

        if (isCadLineConstructionMode()) {
            drawCadOperationPreview(g2);
            return;
        }

        if (EDIT_OP_ADJACENT_POLYGON.equals(featureEditOperation)) {
            drawAdjacentPolygonPreview(g2);
            return;
        }

        if (featureEditSketchCoordinates.isEmpty()) {
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(featureEditSketchCoordinates);
        Coordinate previewCoordinate = resolveInteractivePreviewCoordinate();
        if (previewCoordinate != null && isFeatureEditSketchMode()) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }

        String mode = EDIT_OP_HOLE.equals(featureEditOperation) ? "POLYGON" : "LINE";
        drawTemporaryGeometry(g2, tempCoords, mode, new Color(14, 116, 144), new Color(14, 165, 233, 48));
    }

    private void drawTopographicProfileCapture(Graphics2D g2) {
        if (!topographicProfileCaptureActive) {
            return;
        }
        List<Coordinate> tempCoords = new ArrayList<>(topographicProfileCaptureCoordinates);
        Coordinate previewCoordinate = resolveInteractivePreviewCoordinate();
        if (previewCoordinate != null && !tempCoords.isEmpty()) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }
        if (tempCoords.isEmpty()) {
            return;
        }
        drawTemporaryGeometry(g2, tempCoords, "LINE", new Color(180, 83, 9), new Color(245, 158, 11, 48));
    }

    private void drawAdjacentPolygonPreview(Graphics2D g2) {
        if (selectedFeature == null || selectedLayer == null
                || adjacentPolygonSegmentStart == null || adjacentPolygonSegmentEnd == null) {
            return;
        }

        drawAdjacentBaseSegment(g2);

        Coordinate previewCoordinate = resolveInteractivePreviewCoordinate();
        if (previewCoordinate == null) {
            return;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry sourceGeometry)) {
            return;
        }

        Coordinate sourcePreview = toSourceCoordinate(previewCoordinate.x, previewCoordinate.y, selectedLayer);
        Geometry previewGeometry = buildAdjacentPolygonGeometry(
                sourceGeometry,
                adjacentPolygonSegmentStart,
                adjacentPolygonSegmentEnd,
                sourcePreview
        );
        if (previewGeometry == null || previewGeometry.isEmpty()) {
            return;
        }

        Geometry displayGeometry = reprojectGeometryIfNeeded(selectedLayer, previewGeometry);
        if (!(displayGeometry instanceof Polygon previewPolygon)) {
            return;
        }

        drawPolygon(g2, previewPolygon, new Color(34, 197, 94, 56), new Color(21, 128, 61), 2f);
    }

    private void drawAdjacentBaseSegment(Graphics2D g2) {
        if (adjacentPolygonSegmentStart == null || adjacentPolygonSegmentEnd == null || selectedLayer == null) {
            return;
        }

        GeometryFactory factory = new GeometryFactory();
        LineString baseSegment = factory.createLineString(new Coordinate[]{
                new Coordinate(adjacentPolygonSegmentStart),
                new Coordinate(adjacentPolygonSegmentEnd)
        });
        Geometry displaySegment = reprojectGeometryIfNeeded(selectedLayer, baseSegment);
        if (!(displaySegment instanceof LineString lineString)) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            drawLineString(copy, lineString, new Color(21, 128, 61), 3.2f);
        } finally {
            copy.dispose();
        }
    }

    private void drawCadOperationPreview(Graphics2D g2) {
        if (selectedFeature == null || selectedLayer == null) {
            return;
        }

        if ((EDIT_OP_EXTEND_LINE.equals(featureEditOperation) || EDIT_OP_SHORTEN_LINE.equals(featureEditOperation))
                && !cadReferenceEndpointChosen) {
            drawSelectedLineEndpointHints(g2);
            return;
        }

        if ((EDIT_OP_PARALLEL.equals(featureEditOperation) || EDIT_OP_PERPENDICULAR.equals(featureEditOperation))
                && cadReferenceSegmentStart != null && cadReferenceSegmentEnd != null) {
            drawCadReferenceSegment(g2);
        }

        Coordinate previewCoordinate = resolveInteractivePreviewCoordinate();
        if (previewCoordinate == null) {
            return;
        }

        Coordinate sourcePreview = toSourceCoordinate(previewCoordinate.x, previewCoordinate.y, selectedLayer);
        Geometry previewGeometry = null;
        Geometry selectedGeometry = extractFeatureGeometryCopy(selectedFeature);
        if (selectedGeometry == null || sourcePreview == null) {
            return;
        }

        if (EDIT_OP_EXTEND_LINE.equals(featureEditOperation)) {
            previewGeometry = buildAdjustedSelectedLineGeometry(selectedGeometry, sourcePreview, true);
        } else if (EDIT_OP_SHORTEN_LINE.equals(featureEditOperation)) {
            previewGeometry = buildAdjustedSelectedLineGeometry(selectedGeometry, sourcePreview, false);
        } else if (EDIT_OP_PARALLEL.equals(featureEditOperation) && cadReferenceSegmentStart != null && cadReferenceSegmentEnd != null) {
            previewGeometry = buildParallelLineGeometry(cadReferenceSegmentStart, cadReferenceSegmentEnd, sourcePreview);
        } else if (EDIT_OP_PERPENDICULAR.equals(featureEditOperation) && cadReferenceSegmentStart != null && cadReferenceSegmentEnd != null) {
            previewGeometry = buildPerpendicularLineGeometry(cadReferenceSegmentStart, cadReferenceSegmentEnd, sourcePreview);
        }

        if (previewGeometry == null || previewGeometry.isEmpty()) {
            return;
        }

        Geometry displayGeometry = reprojectGeometryIfNeeded(selectedLayer, previewGeometry);
        if (displayGeometry == null || displayGeometry.isEmpty()) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (displayGeometry instanceof LineString lineString) {
                drawLineString(copy, lineString, new Color(22, 163, 74), 2.6f);
            } else if (displayGeometry instanceof MultiLineString multiLineString) {
                for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                    Geometry child = multiLineString.getGeometryN(i);
                    if (child instanceof LineString lineString) {
                        drawLineString(copy, lineString, new Color(22, 163, 74), 2.6f);
                    }
                }
            }
        } finally {
            copy.dispose();
        }
    }

    private void drawSelectedLineEndpointHints(Graphics2D g2) {
        Geometry geometry = extractFeatureGeometryCopy(selectedFeature);
        Coordinate[] baseCoordinates = extractContinuableLineCoordinates(geometry);
        if (baseCoordinates == null || baseCoordinates.length < 2) {
            return;
        }

        Coordinate start = toProjectCoordinate(baseCoordinates[0], selectedLayer);
        Coordinate end = toProjectCoordinate(baseCoordinates[baseCoordinates.length - 1], selectedLayer);
        if (start == null || end == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawContinuationEndpointHint(copy, start, "A");
            drawContinuationEndpointHint(copy, end, "B");
        } finally {
            copy.dispose();
        }
    }

    private void drawCadReferenceSegment(Graphics2D g2) {
        if (cadReferenceSegmentStart == null || cadReferenceSegmentEnd == null || selectedLayer == null) {
            return;
        }

        Coordinate displayStart = toProjectCoordinate(cadReferenceSegmentStart, selectedLayer);
        Coordinate displayEnd = toProjectCoordinate(cadReferenceSegmentEnd, selectedLayer);
        if (displayStart == null || displayEnd == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(new Color(59, 130, 246, 220));
            copy.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            copy.drawLine(
                    worldToScreenX(displayStart.x),
                    worldToScreenY(displayStart.y),
                    worldToScreenX(displayEnd.x),
                    worldToScreenY(displayEnd.y)
            );
        } finally {
            copy.dispose();
        }
    }

    private void drawSnapPreview(Graphics2D g2) {
        if (!snapEnabled || snapPreviewCoordinate == null || !(isDrawingActive() || isMeasurementActive() || featureEditMode)) {
            return;
        }

        int x = worldToScreenX(snapPreviewCoordinate.x);
        int y = worldToScreenY(snapPreviewCoordinate.y);
        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(new Color(16, 185, 129, 52));
            copy.fillOval(x - 9, y - 9, 18, 18);
            copy.setColor(new Color(5, 150, 105, 220));
            copy.setStroke(new BasicStroke(2f));
            copy.drawOval(x - 7, y - 7, 14, 14);
            copy.drawLine(x - 10, y, x + 10, y);
            copy.drawLine(x, y - 10, x, y + 10);
            copy.setColor(Color.WHITE);
            copy.setStroke(new BasicStroke(1.2f));
            copy.drawOval(x - 3, y - 3, 6, 6);
        } finally {
            copy.dispose();
        }
    }

    private void drawLayer(Graphics2D g2, Layer layer, ShapefileData data) {
        if (data == null || data.getFeatureCollection() == null) {
            return;
        }

        float opacity = layer.getOpacity();
        Graphics2D vecG2 = opacity >= 1.0f ? g2 : (Graphics2D) g2.create();
        if (opacity < 1.0f) {
            vecG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.1f, opacity)));
        }

        try {
            boolean editingLayer = isLayerArmedForEditing(layer);
            forEachVisibleFeatureGeometry(List.of(layer), "Error al dibujar la capa ", (currentLayer, featureGeometry) -> {
                if (editingLayer) {
                    drawGeometryForEditingLayer(vecG2, featureGeometry.geometry(), currentLayer);
                } else {
                    drawGeometry(vecG2, featureGeometry.geometry(), currentLayer, featureGeometry.feature());
                }
            });
        } finally {
            if (opacity < 1.0f) vecG2.dispose();
        }
    }

    private void drawGeometry(Graphics2D g2, Geometry geometry, Layer layer, SimpleFeature feature) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        if ((geometry instanceof Point || geometry instanceof MultiPoint)
                && layer.isClusteringEnabled()) {
            // Individual points are suppressed when clustering is active
            return;
        }

        if (geometry instanceof Point) {
            drawStyledPoint(g2, (Point) geometry, layer, feature);
            drawLabelForFeature(g2, layer, feature, worldToScreenX(((Point)geometry).getX()), worldToScreenY(((Point)geometry).getY()));
            return;
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Point) {
                    drawStyledPoint(g2, (Point) g, layer, feature);
                    drawLabelForFeature(g2, layer, feature, worldToScreenX(((Point)g).getX()), worldToScreenY(((Point)g).getY()));
                }
            }
            return;
        }

        if (geometry instanceof LineString) {
            drawStyledLineString(g2, (LineString) geometry, layer, feature);
            // Label at line midpoint
            Coordinate[] lc = ((LineString)geometry).getCoordinates();
            if (lc.length >= 2) { int mi = lc.length / 2; drawLabelForFeature(g2, layer, feature, worldToScreenX(lc[mi].x), worldToScreenY(lc[mi].y)); }
            return;
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString ml = (MultiLineString) geometry;
            for (int i = 0; i < ml.getNumGeometries(); i++) {
                Geometry g = ml.getGeometryN(i);
                if (g instanceof LineString) {
                    drawStyledLineString(g2, (LineString) g, layer, feature);
                }
            }
            return;
        }

        if (geometry instanceof Polygon) {
            drawStyledPolygon(g2, (Polygon) geometry, layer, feature);
            // Label at polygon interior point
            try {
                org.locationtech.jts.geom.Point ip = geometry.getInteriorPoint();
                if (ip != null) drawLabelForFeature(g2, layer, feature, worldToScreenX(ip.getX()), worldToScreenY(ip.getY()));
            } catch (Exception ignored) {}
            return;
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Polygon) {
                    drawStyledPolygon(g2, (Polygon) g, layer, feature);
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

    private void drawLabelForFeature(Graphics2D g2, Layer layer, SimpleFeature feature, int x, int y) {
        // Labels are now rendered in batch via drawAllLabels() with collision detection.
        // This method is intentionally a no-op to avoid double rendering.
    }

    private void drawStyledPoint(Graphics2D g2, Point point, Layer layer, SimpleFeature feature) {
        int x = worldToScreenX(point.getX());
        int y = worldToScreenY(point.getY());
        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "point");
        int size = ar.com.catgis.LayerRenderHelper.resolveProportionalSize(layer, feature, categoryRule != null ? categoryRule.getPointSize() : layer.getPointSize());
        Color color = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getPointColor();

        // Use catalog symbol if available
        String catId = categoryRule != null ? categoryRule.getCatalogSymbolId() : layer.getCatalogSymbolId();
        if (catId != null && !catId.isEmpty() && !"circle".equals(catId)) {
            PointSymbolCatalog.render(g2, catId, x, y, size + 4, color, color.darker(), 1.2f);
            return;
        }

        if (categoryRule == null && PointGraphicSymbolSupport.paintLayerSymbol(g2, layer, x, y, Math.max(14, size + 6))) {
            return;
        }
        Layer.PointSymbolStyle style = categoryRule != null ? categoryRule.getPointSymbolStyle() : layer.getPointSymbolStyle();
        if (style == null) {
            style = Layer.PointSymbolStyle.CIRCLE;
        }
        PointSymbolRenderer.paint(g2, style, x, y, size, color, Color.BLACK);
    }

    private void drawStyledLineString(Graphics2D g2, LineString line, Layer layer, SimpleFeature feature) {
        Coordinate[] coords = line.getCoordinates();
        if (coords.length < 2) {
            return;
        }

        Path2D path = new Path2D.Double();
        path.moveTo(worldToScreenX(coords[0].x), worldToScreenY(coords[0].y));
        for (int i = 1; i < coords.length; i++) {
            path.lineTo(worldToScreenX(coords[i].x), worldToScreenY(coords[i].y));
        }

        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "line");
        Color lineColor = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getLineColor();
        Layer.LineSymbolStyle lineStyle = categoryRule != null ? categoryRule.getLineStyle() : layer.getLineSymbolStyle();
        float lineWidth = categoryRule != null ? categoryRule.getLineWidth() : layer.getLineWidth();
        g2.setColor(lineColor);
        g2.setStroke(LineSymbolRenderer.buildStroke(lineStyle, lineWidth));
        g2.draw(path);
    }

    private void drawStyledPolygon(Graphics2D g2, Polygon polygon, Layer layer, SimpleFeature feature) {
        Path2D exteriorPath = buildPathFromCoordinates(polygon.getExteriorRing().getCoordinates());
        if (exteriorPath == null) {
            return;
        }

        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "polygon");
        Paint oldPaint = g2.getPaint();
        Layer.PolygonFillStyle fillStyle = categoryRule != null ? categoryRule.getPolygonFillStyle() : layer.getPolygonFillStyle();
        Color fc = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getFillColor();
        Color bc = categoryRule != null ? (categoryRule.getSecondaryColor() != null ? categoryRule.getSecondaryColor() : fc.darker()) : layer.getLineColor();

        // Gradient fill support
        GradientFill gradient = layer.getGradientFill();
        if (gradient != null && categoryRule == null) {
            Paint gp = gradient.createPaint(exteriorPath.getBounds2D());
            g2.setPaint(gp);
            g2.fill(exteriorPath);
        } else {
            g2.setPaint(PolygonSymbolRenderer.buildPaint(fillStyle, fc, bc, 12));
            if (fillStyle != Layer.PolygonFillStyle.OUTLINE_ONLY) {
                g2.fill(exteriorPath);
            }
        }

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Path2D holePath = buildPathFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.setColor(getBackground());
                g2.fill(holePath);
            }
        }

        g2.setPaint(oldPaint);
        Color borderColor = categoryRule != null && categoryRule.getSecondaryColor() != null
                ? categoryRule.getSecondaryColor()
                : layer.getBorderColor();
        float borderWidth = categoryRule != null ? categoryRule.getLineWidth() : layer.getLineWidth();
        Layer.LineSymbolStyle borderStyle = categoryRule != null ? categoryRule.getLineStyle() : layer.getLineSymbolStyle();
        g2.setColor(borderColor);
        g2.setStroke(LineSymbolRenderer.buildStroke(borderStyle, borderWidth));
        g2.draw(exteriorPath);

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Path2D holePath = buildPathFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.draw(holePath);
            }
        }
    }

    // resolveCategoryRule, resolveGraduatedRule, resolveBestRule, resolveProportionalSize
    // moved to LayerRenderHelper for shared use

    private Path2D buildStarPath(double centerX, double centerY, double outerRadius, double innerRadius) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double radius = i % 2 == 0 ? outerRadius : innerRadius;
            double angle = Math.toRadians(-90 + (i * 36));
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + Math.sin(angle) * radius;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }

    private void drawPoint(Graphics2D g2, Point point, Color color, int size) {
        OnlineLayerRenderer.drawPoint(this, g2, point, color, size);
    }

    private void drawLineString(Graphics2D g2, LineString line, Color color, float width) {
        OnlineLayerRenderer.drawLineString(this, g2, line, color, width);
    }

    private void drawPolygon(Graphics2D g2, Polygon polygon, Color fillColor, Color borderColor, float borderWidth) {
        drawPolygon(g2, polygon, fillColor, borderColor, borderWidth, null);
    }

    private void drawPolygon(Graphics2D g2, Polygon polygon, Color fillColor, Color borderColor, float borderWidth, GradientFill gradientFill) {
        OnlineLayerRenderer.drawPolygon(this, g2, polygon, fillColor, borderColor, borderWidth, gradientFill);
    }

    Path2D buildPathFromCoordinates(Coordinate[] coords) {
        return OnlineLayerRenderer.buildPathFromCoordinates(this, coords);
    }

    private void drawSelectedFeature(Graphics2D g2, SimpleFeature feature, Layer layer) {
        if (!isFeatureVisibleInLayer(layer, feature)) {
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
            if (layer == null || !layerManager.isLayerEffectivelyVisible(layer)) {
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
            boolean activeMoveVertex = i == activeEditVertexIndex;
            boolean joinTarget = EDIT_OP_JOIN_VERTEX.equals(featureEditOperation) && i == joinTargetVertexIndex;
            int size = activeMoveVertex ? 12 : (joinTarget ? 14 : 10);

            if (activeMoveVertex) {
                g2.setColor(new Color(255, 102, 0, 240));
            } else if (joinTarget) {
                g2.setColor(new Color(37, 99, 235, 235));
            } else {
                g2.setColor(new Color(220, 38, 38, 220));
            }
            g2.fillOval(x - size / 2, y - size / 2, size, size);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x - size / 2, y - size / 2, size, size);
        }
    }

    Geometry getEditableDisplayGeometry(SimpleFeature feature, Layer layer) {
        if (feature == null || !isFeatureVisibleInLayer(layer, feature)) {
            return null;
        }

        Object geomObj = feature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return null;
        }

        Geometry geometry = reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
        return geometry != null && !geometry.isEmpty() ? geometry : null;
    }

    Coordinate[] getEditableVertexCoordinates(Geometry geometry) {
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

    boolean isFeatureEditSketchMode() {
        return EDIT_OP_CUT.equals(featureEditOperation)
                || EDIT_OP_HOLE.equals(featureEditOperation)
                || EDIT_OP_ADJACENT_POLYGON.equals(featureEditOperation);
    }

    boolean isCadLineConstructionMode() {
        return EDIT_OP_EXTEND_LINE.equals(featureEditOperation)
                || EDIT_OP_SHORTEN_LINE.equals(featureEditOperation)
                || EDIT_OP_PARALLEL.equals(featureEditOperation)
                || EDIT_OP_PERPENDICULAR.equals(featureEditOperation);
    }

    private boolean isSelectedFeatureLinear() {
        if (selectedFeature == null) {
            return false;
        }
        Object geomObj = selectedFeature.getDefaultGeometry();
        return geomObj instanceof LineString || geomObj instanceof MultiLineString;
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

    private boolean ensureSelectedLineReadyForCad(String actionName) {
        if (!featureEditMode && selectedLayer != null && selectedFeature != null) {
            enableFeatureEdit(selectedLayer, selectedFeature);
        }
        if (!featureEditMode || !isSelectedFeatureLinear()) {
            JOptionPane.showMessageDialog(this, "Primero selecciona una sola linea valida para " + actionName + ".");
            return false;
        }
        return true;
    }

    boolean handleFeatureEditClick(MouseEvent e) {
        if (selectedFeature == null || selectedLayer == null) {
            return false;
        }

        if (EDIT_OP_ADD_VERTEX.equals(featureEditOperation)) {
            return addVertexToSelectedGeometry(e.getX(), e.getY());
        }

        if (EDIT_OP_REMOVE_VERTEX.equals(featureEditOperation)) {
            return removeVertexFromSelectedGeometry(e.getX(), e.getY());
        }

        if (EDIT_OP_JOIN_VERTEX.equals(featureEditOperation)) {
            return joinVerticesFromClick(e.getX(), e.getY());
        }

        if (EDIT_OP_ADJACENT_POLYGON.equals(featureEditOperation)) {
            return handleAdjacentPolygonClick(e.getX(), e.getY());
        }

        if (EDIT_OP_EXTEND_LINE.equals(featureEditOperation)) {
            return handleExtendOrShortenLineClick(e.getX(), e.getY(), true);
        }

        if (EDIT_OP_SHORTEN_LINE.equals(featureEditOperation)) {
            return handleExtendOrShortenLineClick(e.getX(), e.getY(), false);
        }

        if (EDIT_OP_PARALLEL.equals(featureEditOperation)) {
            return handleParallelLineClick(e.getX(), e.getY());
        }

        if (EDIT_OP_PERPENDICULAR.equals(featureEditOperation)) {
            return handlePerpendicularLineClick(e.getX(), e.getY());
        }

        if (EDIT_OP_CUT.equals(featureEditOperation)) {
            if (isSelectedFeaturePolygonal()) {
                featureEditSketchCoordinates.add(resolveInteractiveCoordinate(e.getX(), e.getY(), false));
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
            featureEditSketchCoordinates.add(resolveInteractiveCoordinate(e.getX(), e.getY(), false));
            if (e.getClickCount() >= 2 && featureEditSketchCoordinates.size() >= 3) {
                applyFeatureEditSketchOperationEnhanced();
            } else {
                repaint();
            }
            return true;
        }

        return false;
    }

    boolean addVertexToSelectedGeometry(int screenX, int screenY) {
        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return false;
        }

        Geometry displayGeometry = getEditableDisplayGeometry(selectedFeature, selectedLayer);
        Coordinate displayTarget = new Coordinate(screenToWorldX(screenX), screenToWorldY(screenY));
        LineSplitProjection projection = findEditableSegmentProjection(displayGeometry, displayTarget, screenX, screenY, EDIT_SEGMENT_TOLERANCE_PX);
        if (projection == null || projection.segmentIndex < 0 || projection.projected == null) {
            showCopiedMessage("No se encontrÃ³ un tramo cercano para agregar el vÃ©rtice.");
            return true;
        }

        Coordinate sourceCoordinate = toSourceCoordinate(projection.projected.x, projection.projected.y, selectedLayer);
        Geometry updated = buildGeometryWithAddedVertex((Geometry) geomObj, projection.segmentIndex, sourceCoordinate);
        if (updated == null) {
            showCopiedMessage("No se pudo agregar el vÃ©rtice en esa geometrÃ­a.");
            return true;
        }

        updateSelectedFeatureGeometry(updated, "VÃ©rtice agregado.");
        return true;
    }

    private boolean removeVertexFromSelectedGeometry(int screenX, int screenY) {
        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return false;
        }

        int vertexIndex = findEditableVertexIndex(screenX, screenY);
        if (vertexIndex < 0) {
            showCopiedMessage("No se encontrÃ³ un vÃ©rtice cercano para eliminar.");
            return true;
        }

        Geometry updated = buildGeometryWithRemovedVertex((Geometry) geomObj, vertexIndex);
        if (updated == null) {
            showCopiedMessage("No se pudo eliminar ese vÃ©rtice.");
            return true;
        }

        updateSelectedFeatureGeometry(updated, "VÃ©rtice eliminado.");
        return true;
    }

    boolean removeVerticesFromSelectedGeometry(Rectangle selectionBounds) {
        if (selectionBounds == null || selectedFeature == null) {
            return false;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        List<Integer> vertexIndexes = collectEditableVertexIndexes(selectionBounds);
        if (vertexIndexes.isEmpty()) {
            showCopiedMessage("No se encontrÃ³ ningÃºn vÃ©rtice dentro del rectÃ¡ngulo.");
            return true;
        }

        Geometry updated = buildGeometryWithRemovedVertices(geometry, vertexIndexes);
        if (updated == null) {
            showCopiedMessage("No se pudieron eliminar esos vÃ©rtices.");
            return true;
        }

        String message = vertexIndexes.size() == 1
                ? "VÃ©rtice eliminado."
                : vertexIndexes.size() + " vÃ©rtices eliminados.";
        updateSelectedFeatureGeometry(updated, message);
        return true;
    }

    private boolean joinVerticesFromClick(int screenX, int screenY) {
        if (selectedFeature == null || selectedLayer == null) {
            return false;
        }

        int vertexIndex = findEditableVertexIndex(screenX, screenY);
        if (vertexIndex < 0) {
            showCopiedMessage("No se encontrÃ³ un vÃ©rtice cercano para unir.");
            return true;
        }

        if (joinTargetVertexIndex < 0 || joinTargetVertexIndex == vertexIndex) {
            joinTargetVertexIndex = vertexIndex;
            showCopiedMessage("VÃ©rtice base seleccionado. ElegÃ­ otro vÃ©rtice o arrastrÃ¡ un rectÃ¡ngulo para unirlos.");
            repaint();
            return true;
        }

        List<Integer> vertexIndexes = new ArrayList<>();
        vertexIndexes.add(vertexIndex);
        return joinVerticesIntoTarget(joinTargetVertexIndex, vertexIndexes, false);
    }

    boolean joinVerticesFromSelection(Rectangle selectionBounds) {
        if (selectionBounds == null || selectedFeature == null) {
            return false;
        }

        List<Integer> vertexIndexes = collectEditableVertexIndexes(selectionBounds);
        if (vertexIndexes.isEmpty()) {
            showCopiedMessage("No se encontrÃ³ ningÃºn vÃ©rtice dentro del rectÃ¡ngulo.");
            return true;
        }

        int targetIndex = joinTargetVertexIndex;
        if (targetIndex < 0) {
            targetIndex = vertexIndexes.remove(0);
            if (vertexIndexes.isEmpty()) {
                joinTargetVertexIndex = targetIndex;
                showCopiedMessage("VÃ©rtice base seleccionado. Ahora marcÃ¡ otros vÃ©rtices para unirlos.");
                repaint();
                return true;
            }
        } else {
            vertexIndexes.remove(Integer.valueOf(targetIndex));
        }

        return joinVerticesIntoTarget(targetIndex, vertexIndexes, true);
    }

    private boolean joinVerticesIntoTarget(int targetIndex, List<Integer> vertexIndexes, boolean fromSelection) {
        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        if (vertexIndexes == null || vertexIndexes.isEmpty()) {
            showCopiedMessage(fromSelection
                    ? "MarcÃ¡ al menos otro vÃ©rtice para unirlo al vÃ©rtice base."
                    : "ElegÃ­ otro vÃ©rtice distinto del vÃ©rtice base.");
            return true;
        }

        Geometry updated = buildGeometryWithJoinedVertices(geometry, targetIndex, vertexIndexes);
        if (updated == null) {
            showCopiedMessage("No se pudieron unir esos vÃ©rtices. ProbÃ¡ dentro del mismo tramo o polÃ­gono.");
            return true;
        }

        joinTargetVertexIndex = -1;
        updateSelectedFeatureGeometry(updated, vertexIndexes.size() == 1 ? "VÃ©rtices unidos." : "VÃ©rtices unidos al vÃ©rtice base.");
        return true;
    }

    private boolean handleExtendOrShortenLineClick(int screenX, int screenY, boolean extend) {
        if (!isSelectedFeatureLinear()) {
            showCopiedMessage("La herramienta solo funciona sobre lineas.");
            return true;
        }

        Geometry geometry = extractFeatureGeometryCopy(selectedFeature);
        Coordinate[] baseCoordinates = extractContinuableLineCoordinates(geometry);
        if (baseCoordinates == null || baseCoordinates.length < 2) {
            showCopiedMessage("La linea seleccionada no es continua o no se puede editar con esta herramienta.");
            return true;
        }

        if (!cadReferenceEndpointChosen) {
            chooseCadReferenceEndpoint(screenX, screenY, baseCoordinates);
            return true;
        }

        Coordinate targetCoordinate = resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceCoordinate = toSourceCoordinate(targetCoordinate.x, targetCoordinate.y, selectedLayer);
        Geometry updated = buildAdjustedSelectedLineGeometry(geometry, sourceCoordinate, extend);
        if (updated == null) {
            showCopiedMessage(extend
                    ? "El punto elegido no permite extender la linea en ese sentido."
                    : "El punto elegido no permite acortar la linea sin volverla invalida.");
            return true;
        }

        clearCadConstructionState();
        updateSelectedFeatureGeometry(updated, extend ? "Linea extendida." : "Linea acortada.");
        return true;
    }

    private void chooseCadReferenceEndpoint(int screenX, int screenY, Coordinate[] baseCoordinates) {
        Coordinate start = toProjectCoordinate(baseCoordinates[0], selectedLayer);
        Coordinate end = toProjectCoordinate(baseCoordinates[baseCoordinates.length - 1], selectedLayer);
        if (start == null || end == null) {
            showCopiedMessage("No se pudieron ubicar los extremos de la linea.");
            return;
        }

        int startX = worldToScreenX(start.x);
        int startY = worldToScreenY(start.y);
        int endX = worldToScreenX(end.x);
        int endY = worldToScreenY(end.y);
        double startDistance = Math.hypot(screenX - startX, screenY - startY);
        double endDistance = Math.hypot(screenX - endX, screenY - endY);
        double tolerancePx = Math.max(EDIT_VERTEX_TOLERANCE_PX + 6, 16);

        if (startDistance > tolerancePx && endDistance > tolerancePx) {
            showCopiedMessage("Hace clic sobre uno de los extremos resaltados para elegir desde donde modificar.");
            return;
        }

        cadReferenceFromStart = startDistance <= endDistance;
        cadReferenceEndpointChosen = true;
        cadReferenceSegmentStart = cadReferenceFromStart ? new Coordinate(baseCoordinates[0]) : new Coordinate(baseCoordinates[baseCoordinates.length - 2]);
        cadReferenceSegmentEnd = cadReferenceFromStart ? new Coordinate(baseCoordinates[1]) : new Coordinate(baseCoordinates[baseCoordinates.length - 1]);
        showCopiedMessage(cadReferenceFromStart
                ? "Extremo inicial elegido. Ahora marca el nuevo largo sobre esa direccion."
                : "Extremo final elegido. Ahora marca el nuevo largo sobre esa direccion.");
        repaint();
    }

    Geometry buildAdjustedSelectedLineGeometry(Geometry geometry, Coordinate targetCoordinate, boolean extend) {
        if (geometry == null || targetCoordinate == null) {
            return null;
        }

        Coordinate[] baseCoordinates = extractContinuableLineCoordinates(geometry);
        if (baseCoordinates == null || baseCoordinates.length < 2) {
            return null;
        }

        Coordinate[] updatedCoordinates = cloneCoordinates(baseCoordinates);
        int endpointIndex = cadReferenceFromStart ? 0 : updatedCoordinates.length - 1;
        int anchorIndex = cadReferenceFromStart ? 1 : updatedCoordinates.length - 2;
        Coordinate endpoint = updatedCoordinates[endpointIndex];
        Coordinate anchor = updatedCoordinates[anchorIndex];
        double dx = endpoint.x - anchor.x;
        double dy = endpoint.y - anchor.y;
        double lengthSquared = (dx * dx) + (dy * dy);
        if (lengthSquared <= 0.0000001) {
            return null;
        }

        double factor = ((targetCoordinate.x - endpoint.x) * dx + (targetCoordinate.y - endpoint.y) * dy) / lengthSquared;
        if (extend) {
            if (factor <= 0.02) {
                return null;
            }
        } else if (factor >= -0.02 || factor <= -0.98) {
            return null;
        }

        updatedCoordinates[endpointIndex] = new Coordinate(
                endpoint.x + (dx * factor),
                endpoint.y + (dy * factor)
        );
        GeometryFactory factory = geometry.getFactory() != null ? geometry.getFactory() : new GeometryFactory();
        return factory.createLineString(updatedCoordinates);
    }

    private boolean handleParallelLineClick(int screenX, int screenY) {
        if (!isSelectedFeatureLinear()) {
            showCopiedMessage("Paralela solo funciona tomando una linea como referencia.");
            return true;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        if (!cadReferenceEndpointChosen) {
            if (!chooseCadReferenceSegment(screenX, screenY, geometry)) {
                showCopiedMessage("No se encontro un tramo cercano para tomar como referencia.");
            }
            return true;
        }

        Coordinate targetCoordinate = resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceCoordinate = toSourceCoordinate(targetCoordinate.x, targetCoordinate.y, selectedLayer);
        Geometry derived = buildParallelLineGeometry(cadReferenceSegmentStart, cadReferenceSegmentEnd, sourceCoordinate);
        if (derived == null) {
            showCopiedMessage("No se pudo construir la paralela con ese desplazamiento.");
            return true;
        }

        return appendCadDerivedLine(derived, "Linea paralela creada.");
    }

    private boolean handlePerpendicularLineClick(int screenX, int screenY) {
        if (!isSelectedFeatureLinear()) {
            showCopiedMessage("Perpendicular solo funciona tomando una linea como referencia.");
            return true;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        if (!cadReferenceEndpointChosen) {
            if (!chooseCadReferenceSegment(screenX, screenY, geometry)) {
                showCopiedMessage("No se encontro un tramo cercano para tomar como referencia.");
            }
            return true;
        }

        Coordinate targetCoordinate = resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceCoordinate = toSourceCoordinate(targetCoordinate.x, targetCoordinate.y, selectedLayer);
        Geometry derived = buildPerpendicularLineGeometry(cadReferenceSegmentStart, cadReferenceSegmentEnd, sourceCoordinate);
        if (derived == null) {
            showCopiedMessage("No se pudo construir la perpendicular con ese punto.");
            return true;
        }

        return appendCadDerivedLine(derived, "Linea perpendicular creada.");
    }

    private boolean chooseCadReferenceSegment(int screenX, int screenY, Geometry sourceGeometry) {
        Geometry displayGeometry = getEditableDisplayGeometry(selectedFeature, selectedLayer);
        Coordinate displayTarget = new Coordinate(screenToWorldX(screenX), screenToWorldY(screenY));
        LineSplitProjection projection = findEditableSegmentProjection(
                displayGeometry,
                displayTarget,
                screenX,
                screenY,
                EDIT_SEGMENT_TOLERANCE_PX
        );
        if (projection == null || projection.segmentIndex < 0) {
            return false;
        }

        Coordinate[] sourceSegment = getEditableSegmentCoordinates(sourceGeometry, projection.segmentIndex);
        if (sourceSegment == null || sourceSegment.length < 2) {
            return false;
        }

        cadReferenceSegmentStart = sourceSegment[0];
        cadReferenceSegmentEnd = sourceSegment[1];
        cadReferenceEndpointChosen = true;
        showCopiedMessage("Tramo base seleccionado. Ahora completa la herramienta con un segundo clic.");
        repaint();
        return true;
    }

    Geometry buildParallelLineGeometry(Coordinate segmentStart, Coordinate segmentEnd, Coordinate sideCoordinate) {
        if (segmentStart == null || segmentEnd == null || sideCoordinate == null) {
            return null;
        }

        double dx = segmentEnd.x - segmentStart.x;
        double dy = segmentEnd.y - segmentStart.y;
        double length = Math.hypot(dx, dy);
        if (length <= 0.0000001) {
            return null;
        }

        double nx = -dy / length;
        double ny = dx / length;
        Coordinate midpoint = new Coordinate(
                (segmentStart.x + segmentEnd.x) / 2.0,
                (segmentStart.y + segmentEnd.y) / 2.0
        );
        double offset = ((sideCoordinate.x - midpoint.x) * nx) + ((sideCoordinate.y - midpoint.y) * ny);
        if (Math.abs(offset) <= 0.0000001) {
            return null;
        }

        GeometryFactory factory = new GeometryFactory();
        return factory.createLineString(new Coordinate[]{
                new Coordinate(segmentStart.x + (nx * offset), segmentStart.y + (ny * offset)),
                new Coordinate(segmentEnd.x + (nx * offset), segmentEnd.y + (ny * offset))
        });
    }

    Geometry buildPerpendicularLineGeometry(Coordinate segmentStart, Coordinate segmentEnd, Coordinate targetCoordinate) {
        if (segmentStart == null || segmentEnd == null || targetCoordinate == null) {
            return null;
        }

        double dx = segmentEnd.x - segmentStart.x;
        double dy = segmentEnd.y - segmentStart.y;
        double lengthSquared = (dx * dx) + (dy * dy);
        if (lengthSquared <= 0.0000001) {
            return null;
        }

        double factor = ((targetCoordinate.x - segmentStart.x) * dx + (targetCoordinate.y - segmentStart.y) * dy) / lengthSquared;
        Coordinate foot = new Coordinate(segmentStart.x + (dx * factor), segmentStart.y + (dy * factor));
        if (foot.distance(targetCoordinate) <= 0.0000001) {
            return null;
        }

        GeometryFactory factory = new GeometryFactory();
        return factory.createLineString(new Coordinate[]{foot, new Coordinate(targetCoordinate)});
    }

    private boolean appendCadDerivedLine(Geometry geometry, String successMessage) {
        Layer targetLayer = resolveCadLineTargetLayer();
        if (targetLayer == null || geometry == null || geometry.isEmpty()) {
            return false;
        }
        clearCadConstructionState();
        return appendGeometriesToLayer(targetLayer, List.of(geometry), successMessage);
    }

    private Layer resolveCadLineTargetLayer() {
        if (activeVectorEditingLayer != null) {
            ShapefileData editingData = getShapefileData(activeVectorEditingLayer);
            if ("LINE".equals(resolveGeometryFamily(editingData != null ? editingData.getSchema() : null))) {
                return activeVectorEditingLayer;
            }
        }
        if (selectedLayer != null) {
            ShapefileData selectedData = getShapefileData(selectedLayer);
            if ("LINE".equals(resolveGeometryFamily(selectedData != null ? selectedData.getSchema() : null))) {
                return selectedLayer;
            }
        }
        return null;
    }

    private boolean handleAdjacentPolygonClick(int screenX, int screenY) {
        if (selectedFeature == null || selectedLayer == null) {
            return false;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        if (adjacentPolygonSegmentStart == null || adjacentPolygonSegmentEnd == null) {
            Geometry displayGeometry = getEditableDisplayGeometry(selectedFeature, selectedLayer);
            Coordinate displayTarget = new Coordinate(screenToWorldX(screenX), screenToWorldY(screenY));
            LineSplitProjection projection = findEditableSegmentProjection(
                    displayGeometry,
                    displayTarget,
                    screenX,
                    screenY,
                    EDIT_SEGMENT_TOLERANCE_PX
            );
            if (projection == null || projection.segmentIndex < 0) {
                showCopiedMessage("No se encontro un borde cercano para construir el poligono adyacente.");
                return true;
            }

            Coordinate[] sourceSegment = getEditableSegmentCoordinates(geometry, projection.segmentIndex);
            if (sourceSegment == null) {
                showCopiedMessage("No se pudo identificar el borde base del poligono.");
                return true;
            }

            adjacentPolygonSegmentStart = sourceSegment[0];
            adjacentPolygonSegmentEnd = sourceSegment[1];
            showCopiedMessage("Borde base seleccionado. Ahora hace clic afuera del poligono para definir el ancho del adyacente.");
            repaint();
            return true;
        }

        Coordinate displayTarget = resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceTarget = toSourceCoordinate(displayTarget.x, displayTarget.y, selectedLayer);
        Geometry adjacentGeometry = buildAdjacentPolygonGeometry(
                geometry,
                adjacentPolygonSegmentStart,
                adjacentPolygonSegmentEnd,
                sourceTarget
        );
        if (adjacentGeometry == null || adjacentGeometry.isEmpty()) {
            showCopiedMessage("No se pudo construir el poligono adyacente con ese punto. Proba con un clic mas afuera del borde.");
            return true;
        }

        return appendAdjacentPolygonToSelectedLayer(adjacentGeometry);
    }

    private boolean cutSelectedGeometryAtClick(int screenX, int screenY) {
        if (selectedFeature == null || selectedLayer == null) {
            return false;
        }

        Object geomObj = selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        Coordinate targetCoordinate = resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceCoordinate = toSourceCoordinate(targetCoordinate.x, targetCoordinate.y, selectedLayer);
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

    void applyFeatureEditSketchOperationEnhanced() {
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

        Coordinate targetCoordinate = resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceCoordinate = toSourceCoordinate(targetCoordinate.x, targetCoordinate.y, selectedLayer);
        Geometry updated = buildCutGeometryAtPoint((Geometry) geomObj, sourceCoordinate);
        if (updated == null) {
            showCopiedMessage("No se pudo cortar la lÃ­nea en ese punto.");
            return true;
        }

        updateSelectedFeatureGeometry(updated, "GeometrÃ­a cortada.");
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
            message = "GeometrÃ­a cortada.";
        } else if (EDIT_OP_HOLE.equals(featureEditOperation)) {
            updated = buildGeometryWithHole(sourceGeometry, sourceSketch);
            message = "Agujero creado.";
        }

        if (updated == null) {
            showCopiedMessage("No se pudo aplicar la ediciÃ³n geomÃ©trica.");
            return;
        }

        updateSelectedFeatureGeometry(updated, message);
    }

    int findEditableVertexIndex(int screenX, int screenY) {
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

    void moveSelectedVertex(double projectX, double projectY, int vertexIndex) {
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

    void moveSelectedFeatures(double projectX, double projectY) {
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

    void updateSelectedFeatureGeometry(Geometry updated, String statusMessage) {
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
                || EDIT_OP_REMOVE_VERTEX.equals(featureEditOperation)
                || EDIT_OP_JOIN_VERTEX.equals(featureEditOperation)
                || EDIT_OP_EXTEND_LINE.equals(featureEditOperation)
                || EDIT_OP_SHORTEN_LINE.equals(featureEditOperation);
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

    void replaceLayerFeatures(Layer layer, List<SimpleFeature> features, String selectedFeatureId, boolean keepEditMode, String statusMessage) {
        if (layer == null) {
            return;
        }

        ShapefileData currentData = getShapefileData(layer);
        String sourceName = currentData != null ? currentData.getSourceName() : layer.getName();
        String message = currentData != null ? currentData.getMessage() : "Edicion vectorial";
        Envelope envelope = computeEnvelope(features);
        SimpleFeatureType schema = currentData != null ? currentData.getSchema() : null;

        ShapefileData newData = new ShapefileData(
                features,
                envelope,
                sourceName,
                features != null ? features.size() : 0,
                message,
                schema
        );
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
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
        clearCadConstructionState();
        featureEditOperation = nextOperation;
        layer.setFeatureCount(newData.getFeatureCount());

        CatgisDesktopApp.markProjectDirty();
        if (statusMessage != null && !statusMessage.isBlank()) {
            showCopiedMessage(statusMessage);
        }
        refreshEditingUi();
    }

    void pushUndoSnapshotForSelectedLayer() {
        if (selectedLayer == null) {
            return;
        }
        pushUndoSnapshot(selectedLayer, selectedFeature != null ? selectedFeature.getID() : null);
    }

    void pushUndoSnapshot(Layer layer, String selectedFeatureId) {
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
                data.getSchema(),
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
                snapshot.message,
                snapshot.schema
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
        joinTargetVertexIndex = -1;
        clearAdjacentPolygonState();
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

    SimpleFeature findFeatureById(List<SimpleFeature> features, String featureId) {
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

    boolean sameFeatureId(SimpleFeature feature, SimpleFeature otherFeature) {
        return feature != null && otherFeature != null && sameFeatureId(feature, otherFeature.getID());
    }

    boolean sameFeatureId(SimpleFeature feature, String featureId) {
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
        if (MultiPoint.class.isAssignableFrom(binding) && geometry instanceof Point point) {
            return factory.createMultiPoint(new Point[]{(Point) point.copy()});
        }
        return geometry;
    }

    private SimpleFeature buildPastedFeature(SimpleFeature sourceFeature,
                                             Layer targetLayer,
                                             List<SimpleFeature> existingFeatures,
                                             boolean offsetGeometry) {
        if (sourceFeature == null || targetLayer == null) {
            return null;
        }

        List<SimpleFeature> targetFeatures = existingFeatures != null ? existingFeatures : new ArrayList<>();
        ShapefileData targetData = getShapefileData(targetLayer);
        SimpleFeatureType targetType = targetData != null ? targetData.getSchema() : null;
        if (targetType == null && !targetFeatures.isEmpty()) {
            targetType = targetFeatures.get(0).getFeatureType();
        }
        if (targetType == null) {
            return null;
        }

        Geometry pastedGeometry = extractFeatureGeometryCopy(sourceFeature);
        if (pastedGeometry == null) {
            return null;
        }

        if (offsetGeometry) {
            offsetGeometryForPaste(pastedGeometry);
        }
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

    private List<SimpleFeature> collectSelectedFeatures(List<SimpleFeature> features, List<String> selectedIds) {
        List<SimpleFeature> selected = new ArrayList<>();
        if (features == null || selectedIds == null || selectedIds.isEmpty()) {
            return selected;
        }

        LinkedHashSet<String> orderedIds = new LinkedHashSet<>(selectedIds);
        for (SimpleFeature feature : features) {
            if (feature != null && orderedIds.contains(feature.getID())) {
                selected.add(feature);
            }
        }
        return selected;
    }

    private List<String> extractFeatureIds(List<SimpleFeature> features) {
        List<String> ids = new ArrayList<>();
        if (features == null) {
            return ids;
        }

        for (SimpleFeature feature : features) {
            if (feature != null && feature.getID() != null && !feature.getID().isBlank()) {
                ids.add(feature.getID());
            }
        }
        return ids;
    }

    private List<SimpleFeature> replaceFeaturesBySelection(List<SimpleFeature> sourceFeatures,
                                                           List<String> selectedIds,
                                                           List<SimpleFeature> replacementFeatures) {
        List<SimpleFeature> updated = new ArrayList<>();
        if (sourceFeatures == null) {
            updated.addAll(replacementFeatures);
            return updated;
        }

        LinkedHashSet<String> idsToReplace = new LinkedHashSet<>(selectedIds);
        boolean inserted = false;
        for (SimpleFeature feature : sourceFeatures) {
            if (feature == null) {
                continue;
            }
            if (idsToReplace.contains(feature.getID())) {
                if (!inserted) {
                    updated.addAll(replacementFeatures);
                    inserted = true;
                }
                continue;
            }
            updated.add(feature);
        }

        if (!inserted) {
            updated.addAll(replacementFeatures);
        }
        return updated;
    }

    private List<SimpleFeature> buildFeaturesForMergedGeometry(SimpleFeature sourceFeature,
                                                               Geometry mergedGeometry,
                                                               SimpleFeatureType targetType) {
        if (sourceFeature == null || mergedGeometry == null) {
            return new ArrayList<>();
        }

        Class<?> binding = targetType != null && targetType.getGeometryDescriptor() != null
                ? targetType.getGeometryDescriptor().getType().getBinding()
                : null;
        List<Geometry> parts;
        if (binding != null && (binding.isInstance(mergedGeometry)
                || MultiLineString.class.isAssignableFrom(binding)
                || MultiPolygon.class.isAssignableFrom(binding)
                || MultiPoint.class.isAssignableFrom(binding))) {
            parts = List.of(mergedGeometry);
        } else {
            parts = collectGeometryParts(mergedGeometry);
        }
        return buildReplacementFeatures(sourceFeature, parts);
    }

    private Geometry buildMergedGeometry(List<SimpleFeature> selectedFeatures, String family) {
        List<Geometry> geometries = new ArrayList<>();
        for (SimpleFeature feature : selectedFeatures) {
            Geometry geometry = extractFeatureGeometryCopy(feature);
            if (geometry != null && !geometry.isEmpty()) {
                geometries.add(geometry);
            }
        }

        if (geometries.size() < 2) {
            return null;
        }

        GeometryFactory factory = geometries.get(0).getFactory();
        if ("LINE".equals(family)) {
            Geometry unioned = UnaryUnionOp.union(geometries);
            LineMerger merger = new LineMerger();
            merger.add(unioned);
            Collection<?> mergedLines = merger.getMergedLineStrings();
            List<LineString> lines = new ArrayList<>();
            for (Object candidate : mergedLines) {
                if (candidate instanceof LineString lineString && !lineString.isEmpty()) {
                    lines.add((LineString) lineString.copy());
                }
            }

            if (lines.isEmpty()) {
                List<Geometry> parts = collectGeometryParts(unioned);
                for (Geometry part : parts) {
                    if (part instanceof LineString lineString && !lineString.isEmpty()) {
                        lines.add((LineString) lineString.copy());
                    }
                }
            }

            if (lines.isEmpty()) {
                return unioned;
            }
            if (lines.size() == 1) {
                return lines.get(0);
            }
            return factory.createMultiLineString(lines.toArray(new LineString[0]));
        }

        if ("POLYGON".equals(family)) {
            Geometry unioned = UnaryUnionOp.union(geometries);
            if (unioned == null || unioned.isEmpty()) {
                return null;
            }
            Geometry cleaned = unioned.buffer(0);
            return cleaned == null || cleaned.isEmpty() ? unioned : cleaned;
        }

        return null;
    }

    private String resolveGeometryFamily(SimpleFeatureType featureType) {
        if (featureType == null || featureType.getGeometryDescriptor() == null) {
            return "";
        }

        Class<?> binding = featureType.getGeometryDescriptor().getType().getBinding();
        if (binding == null) {
            return "";
        }
        if (Point.class.isAssignableFrom(binding) || MultiPoint.class.isAssignableFrom(binding)) {
            return "POINT";
        }
        if (LineString.class.isAssignableFrom(binding) || MultiLineString.class.isAssignableFrom(binding)) {
            return "LINE";
        }
        if (Polygon.class.isAssignableFrom(binding) || MultiPolygon.class.isAssignableFrom(binding)) {
            return "POLYGON";
        }
        return "";
    }

    private int geometryPartCount(Geometry geometry) {
        return collectGeometryParts(geometry).size();
    }

    private void offsetGeometryForPaste(Geometry geometry) {
        MapGeometryUtils.offsetGeometryForPaste(geometry);
    }

    private Geometry translateGeometry(Geometry geometry, double dx, double dy) {
        return MapGeometryUtils.translateGeometry(geometry, dx, dy);
    }

    private String buildNextFeatureId(List<SimpleFeature> features) {
        return MapGeometryUtils.buildNextFeatureId(features);
    }

    private List<Geometry> collectGeometryParts(Geometry geometry) {
        return MapGeometryUtils.collectGeometryParts(geometry);
    }

    Geometry extractFeatureGeometryCopy(SimpleFeature feature) {
        return MapGeometryUtils.extractFeatureGeometryCopy(feature);
    }

    Coordinate toSourceCoordinate(double projectX, double projectY, Layer layer) {
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

    Coordinate toProjectCoordinate(Coordinate sourceCoordinate, Layer layer) {
        if (sourceCoordinate == null) {
            return null;
        }

        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        String sourceCRS = layer != null ? layer.getSourceCRS() : "";
        if (projectCRS == null || projectCRS.isBlank() || sourceCRS == null || sourceCRS.isBlank()
                || projectCRS.equalsIgnoreCase(sourceCRS)) {
            return new Coordinate(sourceCoordinate);
        }

        double[] projected = transformPoint(sourceCoordinate.x, sourceCoordinate.y, sourceCRS, projectCRS);
        if (projected == null || projected.length < 2) {
            return new Coordinate(sourceCoordinate);
        }
        return new Coordinate(projected[0], projected[1]);
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

    private String getPolygonSurfaceDistanceHint(Layer layer) {
        String sourceCode = layer != null ? layer.getSourceCRS() : "";
        if (sourceCode == null || sourceCode.isBlank()) {
            return "unidades de la capa";
        }
        String metricCode = chooseMetricCRSForMeasurement(sourceCode);
        if (sourceCode.equalsIgnoreCase(metricCode)) {
            return "metros";
        }
        return "metros";
    }

    private double parsePositiveDistance(String input) {
        if (input == null) {
            return Double.NaN;
        }
        String normalized = input.trim().replace(',', '.');
        if (normalized.isEmpty()) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    private Geometry buildBufferedPolygonGeometry(Geometry geometry, Layer layer, double distance) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        if (!(geometry instanceof Polygon) && !(geometry instanceof MultiPolygon)) {
            return null;
        }

        String sourceCode = layer != null ? layer.getSourceCRS() : "";
        GeometryFactory factory = geometry.getFactory();
        Geometry working = (Geometry) geometry.copy();
        String metricCode = chooseMetricCRSForMeasurement(sourceCode);
        boolean reprojectBack = sourceCode != null
                && !sourceCode.isBlank()
                && metricCode != null
                && !metricCode.isBlank()
                && !sourceCode.equalsIgnoreCase(metricCode);

        if (reprojectBack) {
            working = reprojectGeometry(working, sourceCode, metricCode);
        }
        if (working == null || working.isEmpty()) {
            return null;
        }

        Geometry buffered;
        try {
            buffered = working.buffer(distance);
        } catch (Exception ex) {
            return null;
        }
        if (buffered == null || buffered.isEmpty()) {
            return null;
        }

        if (reprojectBack) {
            buffered = reprojectGeometry(buffered, metricCode, sourceCode);
        }

        return normalizePolygonalGeometry(buffered, factory);
    }

    private Coordinate[] getEditableSegmentCoordinates(Geometry geometry, int segmentIndex) {
        if (geometry == null || segmentIndex < 0) {
            return null;
        }

        if (geometry instanceof LineString lineString) {
            return getSegmentCoordinates(lineString.getCoordinates(), segmentIndex);
        }
        if (geometry instanceof MultiLineString multiLineString) {
            int offset = 0;
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                Coordinate[] coords = ((LineString) multiLineString.getGeometryN(i)).getCoordinates();
                int segments = Math.max(0, coords.length - 1);
                if (segmentIndex >= offset && segmentIndex < offset + segments) {
                    return getSegmentCoordinates(coords, segmentIndex - offset);
                }
                offset += segments;
            }
            return null;
        }
        if (geometry instanceof Polygon polygon) {
            return getSegmentCoordinates(polygon.getExteriorRing().getCoordinates(), segmentIndex);
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            int offset = 0;
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Coordinate[] coords = ((Polygon) multiPolygon.getGeometryN(i)).getExteriorRing().getCoordinates();
                int segments = Math.max(0, coords.length - 1);
                if (segmentIndex >= offset && segmentIndex < offset + segments) {
                    return getSegmentCoordinates(coords, segmentIndex - offset);
                }
                offset += segments;
            }
        }

        return null;
    }

    private Coordinate[] getSegmentCoordinates(Coordinate[] coordinates, int segmentIndex) {
        if (coordinates == null || segmentIndex < 0 || segmentIndex >= coordinates.length - 1) {
            return null;
        }
        return new Coordinate[]{
                new Coordinate(coordinates[segmentIndex]),
                new Coordinate(coordinates[segmentIndex + 1])
        };
    }

    Geometry buildAdjacentPolygonGeometry(Geometry sourceGeometry,
                                                  Coordinate segmentStart,
                                                  Coordinate segmentEnd,
                                                  Coordinate sideCoordinate) {
        if (sourceGeometry == null || segmentStart == null || segmentEnd == null || sideCoordinate == null) {
            return null;
        }

        double dx = segmentEnd.x - segmentStart.x;
        double dy = segmentEnd.y - segmentStart.y;
        double length = Math.hypot(dx, dy);
        if (length <= 0.0000001) {
            return null;
        }

        double nx1 = -dy / length;
        double ny1 = dx / length;
        double nx2 = dy / length;
        double ny2 = -dx / length;

        Coordinate midpoint = new Coordinate(
                (segmentStart.x + segmentEnd.x) / 2.0,
                (segmentStart.y + segmentEnd.y) / 2.0
        );
        double dot1 = ((sideCoordinate.x - midpoint.x) * nx1) + ((sideCoordinate.y - midpoint.y) * ny1);
        double dot2 = ((sideCoordinate.x - midpoint.x) * nx2) + ((sideCoordinate.y - midpoint.y) * ny2);

        double nx = Math.abs(dot1) >= Math.abs(dot2) ? nx1 : nx2;
        double ny = Math.abs(dot1) >= Math.abs(dot2) ? ny1 : ny2;
        double distance = Math.max(Math.abs(dot1), Math.abs(dot2));
        if (distance <= 0.0000001) {
            return null;
        }

        GeometryFactory factory = sourceGeometry.getFactory() != null ? sourceGeometry.getFactory() : new GeometryFactory();
        Polygon candidate = buildAdjacentPolygonAlongSegment(factory, segmentStart, segmentEnd, nx, ny, distance);
        if (candidate == null) {
            return null;
        }

        Point interiorPoint = candidate.getInteriorPoint();
        if (interiorPoint != null && sourceGeometry.covers(interiorPoint)) {
            candidate = buildAdjacentPolygonAlongSegment(factory, segmentStart, segmentEnd, -nx, -ny, distance);
            if (candidate == null) {
                return null;
            }
        }

        if (candidate.getArea() <= 0.0) {
            return null;
        }

        try {
            Geometry overlap = sourceGeometry.intersection(candidate);
            if (overlap != null && overlap.getArea() > 0.0000001) {
                return null;
            }
        } catch (Exception ignored) { CatgisLogger.warn("Error al verificar solapamiento de geometrias", ignored); }

        return candidate;
    }

    private Polygon buildAdjacentPolygonAlongSegment(GeometryFactory factory,
                                                     Coordinate segmentStart,
                                                     Coordinate segmentEnd,
                                                     double nx,
                                                     double ny,
                                                     double distance) {
        Coordinate offsetStart = new Coordinate(
                segmentStart.x + (nx * distance),
                segmentStart.y + (ny * distance)
        );
        Coordinate offsetEnd = new Coordinate(
                segmentEnd.x + (nx * distance),
                segmentEnd.y + (ny * distance)
        );

        Coordinate[] shell = normalizeRingCoordinates(new Coordinate[]{
                new Coordinate(segmentStart),
                new Coordinate(segmentEnd),
                offsetEnd,
                offsetStart,
                new Coordinate(segmentStart)
        });
        if (shell == null) {
            return null;
        }
        return factory.createPolygon(factory.createLinearRing(shell), null);
    }

    private boolean appendAdjacentPolygonToSelectedLayer(Geometry adjacentGeometry) {
        if (selectedLayer == null || selectedFeature == null || adjacentGeometry == null || adjacentGeometry.isEmpty()) {
            return false;
        }

        ShapefileData targetData = getShapefileData(selectedLayer);
        if (targetData == null || targetData.getSchema() == null) {
            JOptionPane.showMessageDialog(this, "La capa editable no tiene esquema vectorial disponible.");
            return true;
        }

        pushUndoSnapshotForSelectedLayer();
        List<SimpleFeature> features = new ArrayList<>(targetData.getFeatures());
        SimpleFeature createdFeature = buildDerivedFeatureForLayer(targetData, adjacentGeometry, features, selectedFeature);
        if (createdFeature == null) {
            showCopiedMessage("No se pudo crear el poligono adyacente dentro de la capa.");
            return true;
        }

        String keepFeatureId = selectedFeature.getID();
        features.add(createdFeature);
        replaceLayerFeatures(selectedLayer, features, keepFeatureId, true, "Poligono adyacente creado.");
        featureEditOperation = EDIT_OP_ADJACENT_POLYGON;
        clearAdjacentPolygonState();
        if (selectedFeature != null) {
            startSelectionFlash(selectedLayer, selectedFeature);
        }
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Poligono adyacente creado. Elegi otro borde o cambia de herramienta.");
        }
        refreshEditingUi();
        return true;
    }

    private Geometry reprojectGeometry(Geometry geometry, String sourceCode, String targetCode) {
        try {
            if (geometry == null || geometry.isEmpty()) {
                return geometry;
            }
            if (sourceCode == null || sourceCode.isBlank() || targetCode == null || targetCode.isBlank()) {
                return geometry;
            }
            if (sourceCode.equalsIgnoreCase(targetCode)) {
                return geometry;
            }

            CoordinateReferenceSystem sourceCRS = CRSDefinitions.decode(sourceCode, true);
            CoordinateReferenceSystem targetCRS = CRSDefinitions.decode(targetCode, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            return JTS.transform(geometry, transform);
        } catch (Exception ex) {
            return null;
        }
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

    LineSplitProjection findEditableSegmentProjection(Geometry geometry, Coordinate target, int screenX, int screenY, double maxDistancePx) {
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

    LineSplitProjection projectLineSegmentProjection(LineString line, Coordinate target, int screenX, int screenY, double maxDistancePx, int baseIndex) {
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

    private Geometry buildGeometryWithJoinedVertices(Geometry geometry, int targetVertexIndex, List<Integer> vertexIndexes) {
        if (geometry == null || vertexIndexes == null || vertexIndexes.isEmpty()) {
            return null;
        }

        if (geometry instanceof LineString line) {
            return buildLineStringWithJoinedVertices(line, targetVertexIndex, vertexIndexes);
        }
        if (geometry instanceof Polygon polygon) {
            return buildPolygonWithJoinedVertices(polygon, targetVertexIndex, vertexIndexes);
        }
        if (geometry instanceof MultiLineString multiLine) {
            return buildMultiLineStringWithJoinedVertices(multiLine, targetVertexIndex, vertexIndexes);
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            return buildMultiPolygonWithJoinedVertices(multiPolygon, targetVertexIndex, vertexIndexes);
        }

        return null;
    }

    private Geometry buildLineStringWithJoinedVertices(LineString line, int targetVertexIndex, Collection<Integer> joinIndexes) {
        Coordinate[] coords = copyCoordinates(line.getCoordinates());
        if (coords.length < 2 || targetVertexIndex < 0 || targetVertexIndex >= coords.length) {
            return null;
        }

        Coordinate anchor = new Coordinate(coords[targetVertexIndex]);
        boolean changed = false;
        for (Integer joinIndex : joinIndexes) {
            if (joinIndex == null || joinIndex < 0 || joinIndex >= coords.length || joinIndex == targetVertexIndex) {
                continue;
            }
            coords[joinIndex] = new Coordinate(anchor);
            changed = true;
        }
        if (!changed) {
            return null;
        }

        Coordinate[] normalized = collapseDuplicateLineCoordinates(coords);
        if (normalized == null || normalized.length < 2) {
            return null;
        }
        return line.getFactory().createLineString(normalized);
    }

    private Geometry buildPolygonWithJoinedVertices(Polygon polygon, int targetVertexIndex, Collection<Integer> joinIndexes) {
        Coordinate[] shell = copyCoordinates(polygon.getExteriorRing().getCoordinates());
        int visibleVertices = Math.max(0, shell.length - 1);
        if (visibleVertices < 3 || targetVertexIndex < 0 || targetVertexIndex >= visibleVertices) {
            return null;
        }

        Coordinate anchor = new Coordinate(shell[targetVertexIndex]);
        boolean changed = false;
        for (Integer joinIndex : joinIndexes) {
            if (joinIndex == null || joinIndex < 0 || joinIndex >= visibleVertices || joinIndex == targetVertexIndex) {
                continue;
            }
            shell[joinIndex] = new Coordinate(anchor);
            changed = true;
        }
        if (!changed) {
            return null;
        }

        Coordinate[] normalizedShell = normalizeRingCoordinates(shell);
        if (normalizedShell == null) {
            return null;
        }
        return polygon.getFactory().createPolygon(
                polygon.getFactory().createLinearRing(normalizedShell),
                copyInteriorRings(polygon.getFactory(), polygon)
        );
    }

    private Geometry buildMultiLineStringWithJoinedVertices(MultiLineString multi, int targetVertexIndex, Collection<Integer> joinIndexes) {
        int targetPart = -1;
        int targetLocalIndex = -1;
        int offset = 0;
        for (int i = 0; i < multi.getNumGeometries(); i++) {
            LineString line = (LineString) multi.getGeometryN(i);
            int vertexCount = line.getCoordinates().length;
            if (targetVertexIndex >= offset && targetVertexIndex < offset + vertexCount) {
                targetPart = i;
                targetLocalIndex = targetVertexIndex - offset;
                break;
            }
            offset += vertexCount;
        }
        if (targetPart < 0) {
            return null;
        }

        LineString[] parts = new LineString[multi.getNumGeometries()];
        offset = 0;
        for (int i = 0; i < multi.getNumGeometries(); i++) {
            LineString line = (LineString) multi.getGeometryN(i);
            int vertexCount = line.getCoordinates().length;
            if (i == targetPart) {
                List<Integer> localIndexes = new ArrayList<>();
                for (Integer joinIndex : joinIndexes) {
                    if (joinIndex != null && joinIndex >= offset && joinIndex < offset + vertexCount) {
                        localIndexes.add(joinIndex - offset);
                    }
                }
                Geometry updated = buildLineStringWithJoinedVertices(line, targetLocalIndex, localIndexes);
                if (!(updated instanceof LineString updatedLine)) {
                    return null;
                }
                parts[i] = updatedLine;
            } else {
                parts[i] = (LineString) line.copy();
            }
            offset += vertexCount;
        }
        return multi.getFactory().createMultiLineString(parts);
    }

    private Geometry buildMultiPolygonWithJoinedVertices(MultiPolygon multi, int targetVertexIndex, Collection<Integer> joinIndexes) {
        int targetPart = -1;
        int targetLocalIndex = -1;
        int offset = 0;
        for (int i = 0; i < multi.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) multi.getGeometryN(i);
            int visibleVertices = Math.max(0, polygon.getExteriorRing().getCoordinates().length - 1);
            if (targetVertexIndex >= offset && targetVertexIndex < offset + visibleVertices) {
                targetPart = i;
                targetLocalIndex = targetVertexIndex - offset;
                break;
            }
            offset += visibleVertices;
        }
        if (targetPart < 0) {
            return null;
        }

        Polygon[] parts = new Polygon[multi.getNumGeometries()];
        offset = 0;
        for (int i = 0; i < multi.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) multi.getGeometryN(i);
            int visibleVertices = Math.max(0, polygon.getExteriorRing().getCoordinates().length - 1);
            if (i == targetPart) {
                List<Integer> localIndexes = new ArrayList<>();
                for (Integer joinIndex : joinIndexes) {
                    if (joinIndex != null && joinIndex >= offset && joinIndex < offset + visibleVertices) {
                        localIndexes.add(joinIndex - offset);
                    }
                }
                Geometry updated = buildPolygonWithJoinedVertices(polygon, targetLocalIndex, localIndexes);
                if (!(updated instanceof Polygon updatedPolygon)) {
                    return null;
                }
                parts[i] = updatedPolygon;
            } else {
                parts[i] = (Polygon) polygon.copy();
            }
            offset += visibleVertices;
        }
        return multi.getFactory().createMultiPolygon(parts);
    }

    static Coordinate[] collapseDuplicateLineCoordinates(Coordinate[] coords) {
        return MapGeometryUtils.collapseDuplicateLineCoordinates(coords);
    }

    private Coordinate[] normalizeRingCoordinates(Coordinate[] shell) {
        return MapGeometryUtils.normalizeRingCoordinates(shell);
    }

    private Coordinate[] copyCoordinates(Coordinate[] coords) {
        return MapGeometryUtils.copyCoordinates(coords);
    }

    private Coordinate[] insertCoordinate(Coordinate[] coords, int insertIndex, Coordinate coordinate) {
        return MapGeometryUtils.insertCoordinate(coords, insertIndex, coordinate);
    }

    private Coordinate[] removeCoordinate(Coordinate[] coords, int removeIndex) {
        return MapGeometryUtils.removeCoordinate(coords, removeIndex);
    }

    private Coordinate[] removeRingCoordinate(Coordinate[] shell, int removeVisibleIndex) {
        return MapGeometryUtils.removeRingCoordinate(shell, removeVisibleIndex);
    }

    private LinearRing[] copyInteriorRings(GeometryFactory factory, Polygon polygon) {
        return MapGeometryUtils.copyInteriorRings(factory, polygon);
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

    LineSplitProjection projectCoordinateOntoLine(LineString line, Coordinate target) {
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
        return MapGeometryUtils.buildPolygonFromCoordinates(coordinates, factory);
    }

    private Geometry normalizePolygonalGeometry(Geometry geometry, GeometryFactory factory) {
        return MapGeometryUtils.normalizePolygonalGeometry(geometry, factory);
    }

    private Geometry assemblePolygons(List<Polygon> polygons, GeometryFactory factory) {
        return MapGeometryUtils.assemblePolygons(polygons, factory);
    }

    private List<Polygon> collectPolygons(Geometry geometry) {
        return MapGeometryUtils.collectPolygons(geometry);
    }

    final java.util.List<int[]> globalLabelBoxes = new ArrayList<>();

    private void drawAllLabels(Graphics2D g2) {
        globalLabelBoxes.clear();
        Object prevHint = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (Layer layer : layerManager.getRenderOrderLayers()) {
            if (layer == null || !layerManager.isLayerEffectivelyVisible(layer)) continue;
            if (!layer.isLabelsVisible()) continue;
            if (layer.getLabelField() == null || layer.getLabelField().isBlank()) continue;
            if (!layer.isLabelVisibleAtScale(getCurrentScaleDenominator())) continue;

            ShapefileData shapeData = shapefileLayers.get(layer);
            if (shapeData == null) continue;
            SimpleFeatureCollection collection = shapeData.getFeatureCollection();
            if (collection == null) continue;

            // Resolve expression or field for label text
            String labelExpr = layer.getLabelExpression();
            boolean useExpression = (labelExpr != null && !labelExpr.isBlank());
            String labelField = useExpression ? null : layer.getLabelField();

            // Collect all label candidates for this layer
            List<Object[]> candidates = new ArrayList<>();
            forEachVisibleFeatureGeometry(List.of(layer), "", (currentLayer, featureGeometry) -> {
                String text;
                if (useExpression) {
                    text = LabelExpressionEngine.evaluate(labelExpr, featureGeometry.feature());
                } else {
                    Object attrValue = featureGeometry.feature().getAttribute(labelField);
                    text = attrValue != null ? String.valueOf(attrValue).trim() : "";
                }
                if (text == null || text.isEmpty()) return;

                Coordinate coord = getLabelCoordinate(featureGeometry.geometry());
                if (coord == null) return;

                int sx = worldToScreenX(coord.x);
                int sy = worldToScreenY(coord.y);
                String geomType = LabelPlacementEngine.resolveGeometryType(featureGeometry.geometry().getClass());
                candidates.add(new Object[]{text, sx, sy, geomType, currentLayer.getLabelPriority()});
            });

            if (candidates.isEmpty()) continue;

            // Resolve placements with collision detection
            List<LabelPlacementEngine.ResolvedLabel> resolved =
                    LabelPlacementEngine.resolveLabels(g2, layer, candidates, globalLabelBoxes);

            // Render resolved labels
            for (LabelPlacementEngine.ResolvedLabel rl : resolved) {
                drawResolvedLabel(g2, rl);
            }
        }

        if (prevHint != null) g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, prevHint);
    }

    private void drawResolvedLabel(Graphics2D g2, LabelPlacementEngine.ResolvedLabel rl) {
        Layer layer = rl.layer();
        Font font;
        Color textColor = Color.BLACK;
        Color haloColor = Color.WHITE;
        float haloWidth = 2f;
        boolean haloEnabled = true;
        boolean underline = false;
        boolean bgEnabled = false;
        Color bgColor = new Color(255, 255, 255, 180);

        if (layer != null) {
            int style = Font.PLAIN;
            if (layer.isLabelBold()) style |= Font.BOLD;
            if (layer.isLabelItalic()) style |= Font.ITALIC;
            font = new Font(layer.getLabelFontFamily(), style, layer.getLabelFontSize());
            textColor = layer.getLabelColor();
            haloColor = layer.getLabelHaloColor();
            haloWidth = layer.getLabelHaloWidth();
            haloEnabled = layer.isLabelHaloEnabled();
            underline = layer.isLabelUnderline();
            bgEnabled = layer.isLabelBackgroundEnabled();
            bgColor = layer.getLabelBackgroundColor();
        } else {
            font = g2.getFont();
        }

        g2.setFont(font);
        int lx = rl.drawX();
        int ly = rl.drawY();
        int tw = rl.textWidth();
        int th = rl.textHeight();
        FontMetrics fm = g2.getFontMetrics();

        // Background
        if (bgEnabled && bgColor.getAlpha() > 0) {
            g2.setColor(bgColor);
            g2.fillRoundRect(lx - 4, ly - fm.getAscent() - 2, tw + 8, th + 4, 6, 6);
        }

        // Halo
        if (haloEnabled && haloColor.getAlpha() > 0) {
            g2.setColor(haloColor);
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (dx != 0 || dy != 0)
                        g2.drawString(rl.text(), lx + dx * haloWidth, ly + dy * haloWidth);
        }

        // Text
        g2.setColor(textColor);
        g2.drawString(rl.text(), lx, ly);

        // Underline
        if (underline) {
            g2.setColor(textColor);
            g2.drawLine(lx, ly + 2, lx + tw, ly + 2);
        }
    }

    private void drawLabels(Graphics2D g2, Layer layer, ShapefileData data) {
        if (layer == null || data == null) return;
        if (!layerManager.isLayerEffectivelyVisible(layer)) return;
        if (!layer.isLabelsVisible()) return;
        if (!layer.isLabelVisibleAtScale(getCurrentScaleDenominator())) return;

        String labelExpr = layer.getLabelExpression();
        boolean useExpression = (labelExpr != null && !labelExpr.isBlank());
        String labelField = useExpression ? null : layer.getLabelField();

        if (!useExpression && (labelField == null || labelField.isBlank())) return;

        SimpleFeatureCollection collection = data.getFeatureCollection();
        if (collection == null) return;

        Object prevHint = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        forEachVisibleFeatureGeometry(List.of(layer), "Error al dibujar etiquetas para la capa ", (currentLayer, featureGeometry) -> {
            String text;
            if (useExpression) {
                text = LabelExpressionEngine.evaluate(labelExpr, featureGeometry.feature());
            } else {
                Object attrValue = featureGeometry.feature().getAttribute(labelField);
                text = attrValue != null ? String.valueOf(attrValue).trim() : "";
            }
            if (text == null || text.isEmpty()) return;

            Coordinate labelCoordinate = getLabelCoordinate(featureGeometry.geometry());
            if (labelCoordinate == null) return;

            int x = worldToScreenX(labelCoordinate.x);
            int y = worldToScreenY(labelCoordinate.y);
            drawLabelWithSettings(g2, text, x, y, currentLayer);
        });

        if (prevHint != null) g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, prevHint);
    }

    record FeatureGeometryRef(SimpleFeature feature, Geometry geometry) {
    }

    Coordinate getLabelCoordinate(Geometry geometry) {
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
        drawLabelWithSettings(g2, text, x, y, null);
    }

    private void drawLabelWithSettings(Graphics2D g2, String text, int x, int y, Layer layer) {
        Font font;
        Color textColor = Color.BLACK;
        Color haloColor = Color.WHITE;
        float haloWidth = 2f;
        boolean haloEnabled = true;
        boolean underline = false;
        boolean bgEnabled = false;
        Color bgColor = new Color(255, 255, 255, 180);
        int offX = 0, offY = 0;

        if (layer != null) {
            int style = Font.PLAIN;
            if (layer.isLabelBold()) style |= Font.BOLD;
            if (layer.isLabelItalic()) style |= Font.ITALIC;
            font = new Font(layer.getLabelFontFamily(), style, layer.getLabelFontSize());
            textColor = layer.getLabelColor();
            haloColor = layer.getLabelHaloColor();
            haloWidth = layer.getLabelHaloWidth();
            haloEnabled = layer.isLabelHaloEnabled();
            underline = layer.isLabelUnderline();
            bgEnabled = layer.isLabelBackgroundEnabled();
            bgColor = layer.getLabelBackgroundColor();
            offX = layer.getLabelOffsetX();
            offY = layer.getLabelOffsetY();
        } else {
            font = g2.getFont();
        }

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();
        int drawX = x + offX - tw / 2;
        int drawY = y + offY;

        // Background
        if (bgEnabled && bgColor.getAlpha() > 0) {
            g2.setColor(bgColor);
            g2.fillRoundRect(drawX - 4, drawY - fm.getAscent() - 2, tw + 8, th + 4, 6, 6);
        }

        // Halo
        if (haloEnabled && haloColor.getAlpha() > 0) {
            g2.setColor(haloColor);
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (dx != 0 || dy != 0)
                        g2.drawString(text, drawX + dx * haloWidth, drawY + dy * haloWidth);
        }

        // Text
        g2.setColor(textColor);
        g2.drawString(text, drawX, drawY);

        // Underline
        if (underline) {
            g2.setColor(textColor);
            g2.drawLine(drawX, drawY + 2, drawX + tw, drawY + 2);
        }
    }

    private void drawTemporaryGeometry(Graphics2D g2, List<Coordinate> tempCoords, String mode, Color lineColor, Color fillColor) {
        OnlineLayerRenderer.drawTemporaryGeometry(this, g2, tempCoords, mode, lineColor, fillColor);
    }

    public int worldToScreenX(double worldX) {
        return (int) Math.round((worldX - viewMinX) * zoomFactor);
    }

    public int worldToScreenY(double worldY) {
        return (int) Math.round(getHeight() - ((worldY - viewMinY) * zoomFactor));
    }

    double screenToWorldX(int screenX) {
        return viewMinX + (screenX / zoomFactor);
    }

    double screenToWorldY(int screenY) {
        return viewMinY + ((getHeight() - screenY) / zoomFactor);
    }

    static class LineSplitProjection {
        final int segmentIndex;
        final Coordinate projected;
        final double distance;

        LineSplitProjection(int segmentIndex, Coordinate projected, double distance) {
            this.segmentIndex = segmentIndex;
            this.projected = projected;
            this.distance = distance;
        }
    }

    private static class SnapTarget {
        private final Coordinate coordinate;
        private final double distance;

        private SnapTarget(Coordinate coordinate, double distance) {
            this.coordinate = coordinate;
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
        private final SimpleFeatureType schema;
        private final String selectedFeatureId;

        private LayerEditSnapshot(Layer layer,
                                  List<SimpleFeature> features,
                                  String sourceName,
                                  String message,
                                  SimpleFeatureType schema,
                                  String selectedFeatureId) {
            this.layer = layer;
            this.features = features != null ? features : new ArrayList<>();
            this.sourceName = sourceName;
            this.message = message;
            this.schema = schema;
            this.selectedFeatureId = selectedFeatureId;
        }
    }

    private void drawOnlineResolutionNotice(Graphics2D g2) {
        if (!onlineResolutionNoticeVisible || onlineResolutionNotice == null || onlineResolutionNotice.isBlank()) {
            return;
        }

        Font font = g2.getFont().deriveFont(Font.BOLD, 11f);
        FontMetrics metrics = g2.getFontMetrics(font);
        int padding = 8;
        int textWidth = metrics.stringWidth(onlineResolutionNotice);
        int textHeight = metrics.getHeight();
        int boxWidth = textWidth + padding * 2;
        int boxHeight = textHeight + 6;
        int x = 10;
        int y = 10;

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setFont(font);
            copy.setColor(new Color(255, 248, 230, 230));
            copy.fillRoundRect(x, y, boxWidth, boxHeight, 12, 12);
            copy.setColor(new Color(196, 138, 17, 220));
            copy.drawRoundRect(x, y, boxWidth, boxHeight, 12, 12);
            copy.setColor(new Color(120, 53, 15));
            copy.drawString(onlineResolutionNotice, x + padding, y + metrics.getAscent() + 3);
        } finally {
            copy.dispose();
        }
    }

    void pushTileStatusToBar() {
        OnlineLayerRenderer.pushTileStatusToBar(this);
    }

    static final class OnlineTileFallback {
        final BufferedImage image;
        final int srcX;
        final int srcY;
        final int srcW;
        final int srcH;

        OnlineTileFallback(BufferedImage image, int srcX, int srcY, int srcW, int srcH) {
            this.image = image;
            this.srcX = srcX;
            this.srcY = srcY;
            this.srcW = srcW;
            this.srcH = srcH;
        }
    }

    public interface TopographicProfileCaptureHandler {
        void onLineCaptured(LineString line, String sourceCrs);

        void onCaptureCanceled();
    }

    public interface MapPointCaptureHandler {
        void onPointCaptured(Coordinate coordinate, String sourceCrs);

        void onCaptureCanceled();
    }

    public interface CadPlacementDragHandler {
        void onDragApplied(double offsetX, double offsetY);

        void onDragCanceled();
    }

}




