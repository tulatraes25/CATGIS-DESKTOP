package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FloatingVectorEditToolbar extends JPanel {

    private final JToggleButton btnMove;
    private final JToggleButton btnSelect;
    private final JButton btnZoomSelected;
    private final JButton btnCopy;
    private final JButton btnPaste;
    private final JButton btnDeleteSelection;
    private final JButton btnClearSelection;
    private final JButton btnEditAttributes;
    private final JToggleButton btnPoint;
    private final JToggleButton btnMultiPoint;
    private final JToggleButton btnLine;
    private final JButton btnContinueLine;
    private final JButton btnRectangle;
    private final JToggleButton btnPolygon;
    private final JButton btnMoveFeature;
    private final JToggleButton btnMoveVertex;
    private final JButton btnAddVertex;
    private final JButton btnRemoveVertex;
    private final JButton btnJoinVertices;
    private final JButton btnCut;
    private final JButton btnSplitPolygon;
    private final JButton btnHole;
    private final JButton btnIncreaseArea;
    private final JButton btnDecreaseArea;
    private final JButton btnAdjacentPolygon;
    private final JButton btnMerge;
    private final JButton btnExplode;
    private final JButton btnUndo;
    private final JButton btnRedo;
    private final JButton btnOptions;
    private final JButton btnSaveChanges;
    private final JButton btnFinish;
    private final JButton btnCancel;
    private JPanel pointSectionPanel;
    private JPanel lineSectionPanel;
    private JPanel polygonSectionPanel;
    private JPanel vertexSectionPanel;
    private JLabel pointSectionLabel;
    private JLabel lineSectionLabel;
    private JLabel polygonSectionLabel;
    private JLabel vertexSectionLabel;

    private Point dragOffset;

    public FloatingVectorEditToolbar() {
        setOpaque(true);
        setBackground(new Color(244, 245, 247));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(182, 190, 202)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        setLayout(new BorderLayout(0, 4));
        setPreferredSize(new Dimension(860, 112));

        JPanel header = buildHeader();
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        btnMove = createToggleButton("Mover mapa", AppIcons.panIcon());
        btnMove.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.enablePanMode();
            }
            refreshState();
        });

        btnSelect = createToggleButton("Seleccionar elemento de la capa editable", AppIcons.selectIcon());
        btnSelect.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                Layer layer = getPreferredEditingLayer();
                if (layer != null) {
                    CatgisDesktopApp.mapPanel.prepareLayerForEditing(layer);
                } else {
                    CatgisDesktopApp.mapPanel.enableSelectMode();
                }
            }
            refreshState();
        });

        btnZoomSelected = createActionButton("Zoom al elemento seleccionado", AppIcons.zoomInIcon());
        btnZoomSelected.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                if (CatgisDesktopApp.mapPanel.getSelectedFeatureCount() > 1) {
                    CatgisDesktopApp.mapPanel.zoomToFeatureSelection(
                            CatgisDesktopApp.mapPanel.getSelectedLayerRef(),
                            CatgisDesktopApp.mapPanel.getSelectedFeatureIds()
                    );
                } else {
                    CatgisDesktopApp.mapPanel.zoomToSelectedFeature();
                }
            }
            refreshState();
        });

        btnCopy = createActionButton("Copiar elementos seleccionados", AppIcons.attrCopyIcon());
        btnCopy.addActionListener(e -> {
            if (!ensureFeatureSelection("copiar el elemento")) {
                return;
            }
            CatgisDesktopApp.mapPanel.copySelectedFeatures();
            refreshState();
        });

        btnPaste = createActionButton("Pegar elementos en la capa en edicion", AppIcons.openIcon());
        btnPaste.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                Layer layer = getPreferredEditingLayer();
                if (layer != null) {
                    CatgisDesktopApp.mapPanel.prepareLayerForEditing(layer);
                }
                CatgisDesktopApp.mapPanel.pasteCopiedFeature();
            }
            refreshState();
        });

        btnDeleteSelection = createActionButton("Borrar elemento seleccionado", AppIcons.removeIcon());
        btnDeleteSelection.addActionListener(e -> {
            if (!ensureFeatureSelection("eliminar el elemento")) {
                return;
            }
            CatgisDesktopApp.mapPanel.deleteSelectedFeatures();
            refreshState();
        });

        btnClearSelection = createActionButton("Limpiar seleccion", AppIcons.attrCloseIcon());
        btnClearSelection.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.clearSelectedFeature();
            }
            refreshState();
        });

        btnEditAttributes = createActionButton("Ver o editar atributos", AppIcons.tableIcon());
        btnEditAttributes.addActionListener(e -> {
            Layer layer = getPreferredEditingLayer();
            if (layer == null) {
                JOptionPane.showMessageDialog(this, "Primero selecciona una capa vectorial.");
                return;
            }
            OpenAttributeTableAction.openTable(layer);
            refreshState();
        });

        btnPoint = createToggleButton("Dibujar punto", AppIcons.pointIcon());
        btnPoint.addActionListener(e -> activateDrawMode("POINT"));

        btnMultiPoint = createToggleButton("Dibujar multipunto", AppIcons.multiPointIcon());
        btnMultiPoint.addActionListener(e -> activateDrawMode("MULTIPOINT"));

        btnLine = createToggleButton("Dibujar linea", AppIcons.lineIcon());
        btnLine.addActionListener(e -> activateDrawMode("LINE"));

        btnContinueLine = createActionButton("Continuar edicion de linea", AppIcons.lineIcon());
        btnContinueLine.addActionListener(e -> showPendingTool("Continuar linea", "La base visual ya esta, pero todavia falta el motor para continuar una linea existente desde su ultimo vertice."));

        btnRectangle = createActionButton("Dibujar rectangulo", AppIcons.rectangleIcon());
        btnRectangle.addActionListener(e -> showPendingTool("Dibujar rectangulo", "La herramienta ya esta prevista en la paleta, pero todavia falta el flujo de dos esquinas para crear el rectangulo."));

        btnPolygon = createToggleButton("Dibujar poligono", AppIcons.polygonIcon());
        btnPolygon.addActionListener(e -> activateDrawMode("POLYGON"));

        btnMoveFeature = createActionButton("Mover elementos seleccionados", AppIcons.moveFeatureIcon());
        btnMoveFeature.addActionListener(e -> {
            if (!ensureFeatureSelection("mover los elementos")) {
                return;
            }
            CatgisDesktopApp.mapPanel.activateMoveFeatureMode();
            refreshState();
        });

        btnMoveVertex = createToggleButton("Mover vertice", AppIcons.moveVertexIcon());
        btnMoveVertex.addActionListener(e -> activateMoveVertexMode());

        btnAddVertex = createActionButton("Insertar vertice", AppIcons.addVertexIcon());
        btnAddVertex.addActionListener(e -> activateAddVertexMode());

        btnRemoveVertex = createActionButton("Borrar vertice", AppIcons.removeVertexIcon());
        btnRemoveVertex.addActionListener(e -> activateRemoveVertexMode());

        btnJoinVertices = createActionButton("Unir vertices", AppIcons.joinVerticesIcon());
        btnJoinVertices.addActionListener(e -> showPendingTool("Unir vertices", "Todavia falta la operacion para soldar vertices o llevarlos al vertice seleccionado."));

        btnCut = createActionButton("Dividir linea o cortar geometria", AppIcons.cutIcon());
        btnCut.addActionListener(e -> activateCutFeatureMode());

        btnSplitPolygon = createActionButton("Dividir poligono", AppIcons.cutIcon());
        btnSplitPolygon.addActionListener(e -> activateCutFeatureMode());

        btnHole = createActionButton("Crear agujero", AppIcons.holeIcon());
        btnHole.addActionListener(e -> activateHoleMode());

        btnIncreaseArea = createActionButton("Aumentar superficie", AppIcons.polygonIcon());
        btnIncreaseArea.addActionListener(e -> showPendingTool("Aumentar superficie", "Esta herramienta queda marcada como siguiente etapa junto con disminuir superficie y poligono adyacente."));

        btnDecreaseArea = createActionButton("Disminuir superficie", AppIcons.polygonIcon());
        btnDecreaseArea.addActionListener(e -> showPendingTool("Disminuir superficie", "Esta herramienta queda marcada como siguiente etapa junto con aumentar superficie y poligono adyacente."));

        btnAdjacentPolygon = createActionButton("Generar poligono adyacente", AppIcons.polygonIcon());
        btnAdjacentPolygon.addActionListener(e -> showPendingTool("Poligono adyacente", "La paleta ya reserva esta herramienta, pero todavia falta el motor de construccion adyacente estilo Kosmo."));

        btnMerge = createActionButton("Unir elementos", AppIcons.saveIcon());
        btnMerge.addActionListener(e -> showPendingTool("Unir elementos", "La union de entidades seleccionadas queda como siguiente bloque fuerte de edicion."));

        btnExplode = createActionButton("Explotar entidades seleccionadas", AppIcons.exportIcon());
        btnExplode.addActionListener(e -> showPendingTool("Explotar entidad", "La explosion de multipartes queda visible en la paleta, pero falta implementarla en la capa editable."));

        btnUndo = createActionButton("Deshacer", AppIcons.undoIcon());
        btnUndo.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.undoFeatureEdit();
            }
            refreshState();
        });

        btnRedo = createActionButton("Rehacer", AppIcons.redoIcon());
        btnRedo.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.redoFeatureEdit();
            }
            refreshState();
        });

        btnOptions = createActionButton("Opciones de capa", AppIcons.propertiesIcon());
        btnOptions.addActionListener(e -> {
            Layer layer = getPreferredEditingLayer();
            if (layer == null) {
                JOptionPane.showMessageDialog(this, "Primero selecciona una capa vectorial.");
                return;
            }
            LayerPropertiesDialog.open(layer);
        });

        btnSaveChanges = createActionButton("Salvar cambios", AppIcons.saveIcon());
        btnSaveChanges.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.isFeatureEditMode()) {
                CatgisDesktopApp.mapPanel.saveFeatureEditChanges();
            } else {
                SaveProjectAction.saveProject();
            }
            refreshState();
        });

        btnFinish = createActionButton("Terminar", AppIcons.finishIcon());
        btnFinish.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel == null) {
                return;
            }
            if (CatgisDesktopApp.mapPanel.isFeatureEditMode() || CatgisDesktopApp.mapPanel.getEditingLayerRef() != null) {
                CatgisDesktopApp.mapPanel.finishFeatureEdit();
            } else if (CatgisDesktopApp.mapPanel.isMeasurementActive()) {
                CatgisDesktopApp.mapPanel.finishCurrentMeasurement();
            } else {
                CatgisDesktopApp.mapPanel.finishCurrentDrawing();
            }
            refreshState();
        });

        btnCancel = createActionButton("Cancelar", AppIcons.cancelIcon());
        btnCancel.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel == null) {
                return;
            }
            if (CatgisDesktopApp.mapPanel.isFeatureEditMode() || CatgisDesktopApp.mapPanel.getEditingLayerRef() != null) {
                CatgisDesktopApp.mapPanel.cancelFeatureEdit();
            } else if (CatgisDesktopApp.mapPanel.isMeasurementActive()) {
                CatgisDesktopApp.mapPanel.cancelCurrentMeasurement();
            } else {
                CatgisDesktopApp.mapPanel.cancelCurrentDrawing();
            }
            refreshState();
        });

        addButtons(body);
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        installDragSupport(header);
        refreshState();
    }

    private void addButtons(JPanel body) {
        JPanel strip = new JPanel();
        strip.setOpaque(false);
        strip.setLayout(new BoxLayout(strip, BoxLayout.X_AXIS));
        strip.setAlignmentX(Component.LEFT_ALIGNMENT);

        strip.add(createSection("Seleccion", btnMove, btnSelect, btnZoomSelected, btnCopy, btnPaste, btnDeleteSelection, btnClearSelection, btnEditAttributes, btnMoveFeature));
        strip.add(Box.createHorizontalStrut(4));
        strip.add(createSection("Puntos", btnPoint, btnMultiPoint));
        strip.add(Box.createHorizontalStrut(4));
        strip.add(createSection("Lineas", btnLine, btnContinueLine, btnCut));
        strip.add(Box.createHorizontalStrut(4));
        strip.add(createSection("Vertices", btnMoveVertex, btnAddVertex, btnRemoveVertex, btnJoinVertices));
        strip.add(Box.createHorizontalStrut(4));
        strip.add(createSection("Poligonos", btnRectangle, btnPolygon, btnSplitPolygon, btnHole, btnIncreaseArea, btnDecreaseArea, btnAdjacentPolygon));
        strip.add(Box.createHorizontalStrut(4));
        strip.add(createSection("Sesion", btnUndo, btnRedo, btnMerge, btnExplode, btnOptions, btnSaveChanges, btnFinish, btnCancel));

        body.add(strip);
    }

    public void refreshState() {
        MapPanel map = CatgisDesktopApp.mapPanel;
        if (map == null) {
            return;
        }

        String drawingMode = map.getDrawingMode();
        String currentTool = map.getCurrentTool();
        boolean drawingActive = map.isDrawingActive();
        boolean measurementActive = map.isMeasurementActive();
        boolean interactionActive = drawingActive || measurementActive;
        Layer selectedLayer = map.getSelectedLayerRef();
        Layer editingLayer = map.getEditingLayerRef();
        SimpleFeature selectedFeature = map.getSelectedFeatureRef();
        int selectionCount = map.getSelectedFeatureCount();
        boolean hasVectorSelection = selectionCount > 0;
        boolean hasEditingLayer = editingLayer != null;
        boolean editingActive = map.isFeatureEditMode();
        boolean editDirty = map.hasFeatureEditChanges();
        boolean pointSelection = selectedFeature != null
                && (selectedFeature.getDefaultGeometry() instanceof org.locationtech.jts.geom.Point
                || selectedFeature.getDefaultGeometry() instanceof MultiPoint);
        boolean lineSelection = selectedFeature != null
                && (selectedFeature.getDefaultGeometry() instanceof LineString || selectedFeature.getDefaultGeometry() instanceof MultiLineString);
        boolean polygonSelection = selectedFeature != null
                && (selectedFeature.getDefaultGeometry() instanceof Polygon || selectedFeature.getDefaultGeometry() instanceof MultiPolygon);
        boolean linearOrPolygonalSelection = lineSelection || polygonSelection;
        Layer geometryContextLayer = editingLayer != null ? editingLayer : selectedLayer;
        boolean geometryContextActive = geometryContextLayer != null;
        boolean editingPointLayer = isPointLayer(geometryContextLayer);
        boolean editingLineLayer = isLineLayer(geometryContextLayer);
        boolean editingPolygonLayer = isPolygonLayer(geometryContextLayer);
        boolean editingUnknownLayer = geometryContextActive && !editingPointLayer && !editingLineLayer && !editingPolygonLayer;
        boolean canDrawPoint = !geometryContextActive || editingPointLayer || editingUnknownLayer;
        boolean canDrawLine = !geometryContextActive || editingLineLayer || editingUnknownLayer;
        boolean canDrawPolygon = !geometryContextActive || editingPolygonLayer || editingUnknownLayer;
        boolean lineOrPolygonContext = !geometryContextActive || editingLineLayer || editingPolygonLayer || editingUnknownLayer;

        btnMove.setSelected(!interactionActive && "MOVE".equalsIgnoreCase(currentTool));
        btnSelect.setSelected(!interactionActive && "SELECT".equalsIgnoreCase(currentTool));
        btnPoint.setSelected("POINT".equalsIgnoreCase(drawingMode));
        btnMultiPoint.setSelected("MULTIPOINT".equalsIgnoreCase(drawingMode));
        btnLine.setSelected("LINE".equalsIgnoreCase(drawingMode));
        btnPolygon.setSelected("POLYGON".equalsIgnoreCase(drawingMode));
        btnMoveVertex.setSelected(editingActive && "MOVE_VERTEX".equalsIgnoreCase(map.getFeatureEditOperation()));

        btnZoomSelected.setEnabled(hasVectorSelection);
        btnCopy.setEnabled(hasVectorSelection);
        btnPaste.setEnabled(hasEditingLayer && map.hasCopiedFeature());
        btnDeleteSelection.setEnabled(hasVectorSelection);
        btnClearSelection.setEnabled(hasVectorSelection || hasEditingLayer);
        btnEditAttributes.setEnabled(hasEditingLayer || hasVectorSelection);
        btnPoint.setEnabled(canDrawPoint);
        btnMultiPoint.setEnabled(canDrawPoint);
        btnLine.setEnabled(canDrawLine);
        btnContinueLine.setEnabled(lineSelection);
        btnRectangle.setEnabled(canDrawPolygon);
        btnPolygon.setEnabled(canDrawPolygon);
        btnMoveFeature.setEnabled(hasVectorSelection);
        btnMoveVertex.setEnabled(linearOrPolygonalSelection);
        btnAddVertex.setEnabled(linearOrPolygonalSelection);
        btnRemoveVertex.setEnabled(linearOrPolygonalSelection);
        btnJoinVertices.setEnabled(linearOrPolygonalSelection);
        btnCut.setEnabled(linearOrPolygonalSelection);
        btnSplitPolygon.setEnabled(polygonSelection);
        btnHole.setEnabled(polygonSelection);
        btnIncreaseArea.setEnabled(polygonSelection);
        btnDecreaseArea.setEnabled(polygonSelection);
        btnAdjacentPolygon.setEnabled(polygonSelection);
        btnMerge.setEnabled(hasEditingLayer);
        btnExplode.setEnabled(hasVectorSelection);
        btnUndo.setEnabled(map.canUndoFeatureEdit());
        btnRedo.setEnabled(map.canRedoFeatureEdit());
        btnOptions.setEnabled(hasEditingLayer || hasVectorSelection);
        btnSaveChanges.setEnabled(editingActive ? editDirty : true);
        btnFinish.setEnabled(interactionActive || editingActive || hasEditingLayer);
        btnCancel.setEnabled(interactionActive || editingActive || hasEditingLayer);

        updateSectionHighlight(pointSectionPanel, pointSectionLabel, geometryContextActive && editingPointLayer);
        updateSectionHighlight(lineSectionPanel, lineSectionLabel, geometryContextActive && editingLineLayer);
        updateSectionHighlight(polygonSectionPanel, polygonSectionLabel, geometryContextActive && editingPolygonLayer);
        updateSectionHighlight(vertexSectionPanel, vertexSectionLabel, geometryContextActive && lineOrPolygonContext && !editingPointLayer);
    }

    private boolean isPointLayer(Layer layer) {
        return "POINT".equals(resolveGeometryFamily(layer));
    }

    private boolean isLineLayer(Layer layer) {
        return "LINE".equals(resolveGeometryFamily(layer));
    }

    private boolean isPolygonLayer(Layer layer) {
        return "POLYGON".equals(resolveGeometryFamily(layer));
    }

    private String resolveGeometryFamily(Layer layer) {
        if (layer == null) {
            return "";
        }

        if (CatgisDesktopApp.mapPanel != null) {
            ShapefileData data = CatgisDesktopApp.mapPanel.getShapefileData(layer);
            String familyFromData = resolveGeometryFamily(data);
            if (!familyFromData.isEmpty()) {
                return familyFromData;
            }
        }

        String type = layer != null && layer.getType() != null ? layer.getType().toUpperCase() : "";
        if (type.contains("MULTIPOINT") || type.contains("POINT") || type.contains("PUNTO")) {
            return "POINT";
        }
        if (type.contains("LINE")) {
            return "LINE";
        }
        if (type.contains("POLYGON") || type.contains("POLIG")) {
            return "POLYGON";
        }
        return "";
    }

    private String resolveGeometryFamily(ShapefileData data) {
        if (data == null) {
            return "";
        }

        SimpleFeatureCollection featureCollection = data.getFeatureCollection();
        if (featureCollection != null) {
            SimpleFeatureType schema = featureCollection.getSchema();
            if (schema != null && schema.getGeometryDescriptor() != null) {
                String familyFromBinding = resolveGeometryFamily(schema.getGeometryDescriptor().getType().getBinding());
                if (!familyFromBinding.isEmpty()) {
                    return familyFromBinding;
                }
            }
        }

        if (data.getFeatures() != null) {
            for (SimpleFeature feature : data.getFeatures()) {
                if (feature == null) {
                    continue;
                }
                String familyFromGeometry = resolveGeometryFamily(feature.getDefaultGeometry());
                if (!familyFromGeometry.isEmpty()) {
                    return familyFromGeometry;
                }
            }
        }
        return "";
    }

    private String resolveGeometryFamily(Object geometry) {
        if (geometry instanceof org.locationtech.jts.geom.Point || geometry instanceof MultiPoint) {
            return "POINT";
        }
        if (geometry instanceof LineString || geometry instanceof MultiLineString) {
            return "LINE";
        }
        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            return "POLYGON";
        }
        if (geometry instanceof Geometry) {
            return resolveGeometryFamily(((Geometry) geometry).getClass());
        }
        if (geometry instanceof Class<?>) {
            return resolveGeometryFamily((Class<?>) geometry);
        }
        return "";
    }

    private String resolveGeometryFamily(Class<?> geometryClass) {
        if (geometryClass == null) {
            return "";
        }
        if (org.locationtech.jts.geom.Point.class.isAssignableFrom(geometryClass) || MultiPoint.class.isAssignableFrom(geometryClass)) {
            return "POINT";
        }
        if (LineString.class.isAssignableFrom(geometryClass) || MultiLineString.class.isAssignableFrom(geometryClass)) {
            return "LINE";
        }
        if (Polygon.class.isAssignableFrom(geometryClass) || MultiPolygon.class.isAssignableFrom(geometryClass)) {
            return "POLYGON";
        }
        return "";
    }

    private void activateDrawMode(String mode) {
        if (CatgisDesktopApp.mapPanel == null) {
            return;
        }

        Layer layer = getPreferredEditingLayer();
        if (layer != null) {
            CatgisDesktopApp.mapPanel.prepareLayerForEditing(layer);
        }

        if ("POINT".equalsIgnoreCase(mode)) {
            CatgisDesktopApp.mapPanel.enableDrawPointMode();
        } else if ("MULTIPOINT".equalsIgnoreCase(mode)) {
            CatgisDesktopApp.mapPanel.enableDrawMultiPointMode();
        } else if ("LINE".equalsIgnoreCase(mode)) {
            CatgisDesktopApp.mapPanel.enableDrawLineMode();
        } else if ("POLYGON".equalsIgnoreCase(mode)) {
            CatgisDesktopApp.mapPanel.enableDrawPolygonMode();
        }

        refreshState();
    }

    private Layer getPreferredEditingLayer() {
        if (CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getEditingLayerRef() != null) {
            return CatgisDesktopApp.mapPanel.getEditingLayerRef();
        }
        if (CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedLayerRef() != null) {
            return CatgisDesktopApp.mapPanel.getSelectedLayerRef();
        }
        if (CatgisDesktopApp.layersPanel != null) {
            Layer selected = CatgisDesktopApp.layersPanel.getSelectedLayer();
            if (selected != null && !(selected instanceof RasterLayer)) {
                return selected;
            }
        }
        return null;
    }

    private void activateMoveVertexMode() {
        if (CatgisDesktopApp.mapPanel == null) {
            return;
        }

        Layer layer = CatgisDesktopApp.mapPanel.getSelectedLayerRef();
        SimpleFeature feature = CatgisDesktopApp.mapPanel.getSelectedFeatureRef();
        if (layer == null || feature == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero selecciona una linea o poligono con la flecha de seleccion dentro de la capa editable.",
                    "Edicion vectorial",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshState();
            return;
        }

        CatgisDesktopApp.mapPanel.enableFeatureEdit(layer, feature);
        CatgisDesktopApp.mapPanel.activateMoveVertexMode();
        refreshState();
    }

    private void activateAddVertexMode() {
        if (!ensureFeatureSelection("insertar un vertice")) {
            return;
        }
        CatgisDesktopApp.mapPanel.activateAddVertexMode();
        refreshState();
    }

    private void activateRemoveVertexMode() {
        if (!ensureFeatureSelection("borrar un vertice")) {
            return;
        }
        CatgisDesktopApp.mapPanel.activateRemoveVertexMode();
        refreshState();
    }

    private void activateCutFeatureMode() {
        if (!ensureFeatureSelection("cortar la geometria")) {
            return;
        }
        CatgisDesktopApp.mapPanel.activateCutFeatureMode();
        refreshState();
    }

    private void activateHoleMode() {
        if (!ensureFeatureSelection("crear un agujero")) {
            return;
        }
        CatgisDesktopApp.mapPanel.activateHoleMode();
        refreshState();
    }

    private boolean ensureFeatureSelection(String actionName) {
        if (CatgisDesktopApp.mapPanel == null) {
            return false;
        }
        Layer layer = CatgisDesktopApp.mapPanel.getSelectedLayerRef();
        SimpleFeature feature = CatgisDesktopApp.mapPanel.getSelectedFeatureRef();
        if (layer == null || feature == null) {
            Layer editingLayer = getPreferredEditingLayer();
            if (editingLayer != null) {
                CatgisDesktopApp.mapPanel.prepareLayerForEditing(editingLayer);
            }
            JOptionPane.showMessageDialog(
                    this,
                    "Primero selecciona una entidad de la capa editable con la flecha para " + actionName + ".",
                    "Edicion vectorial",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshState();
            return false;
        }
        CatgisDesktopApp.mapPanel.enableFeatureEdit(layer, feature);
        return true;
    }

    private void showPendingTool(String toolName, String detail) {
        JOptionPane.showMessageDialog(
                this,
                toolName + " todavia esta en construccion.\n\n" + detail,
                "Edicion vectorial",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(new Color(232, 235, 239));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 202, 211)),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        header.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

        JLabel title = new JLabel("Edicion vectorial");
        title.setForeground(new Color(32, 41, 58));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 10.5f));

        JLabel hint = new JLabel("Arrastrar", SwingConstants.RIGHT);
        hint.setForeground(new Color(104, 114, 126));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 9f));

        header.add(title, BorderLayout.WEST);
        header.add(hint, BorderLayout.EAST);
        return header;
    }

    private JPanel createSection(String title, AbstractButton... buttons) {
        JPanel section = new JPanel(new BorderLayout(0, 3));
        section.setOpaque(true);
        section.setBackground(new Color(246, 247, 249));
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 219, 226)),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
        ));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setAlignmentY(Component.TOP_ALIGNMENT);

        JLabel label = new JLabel(title.toUpperCase());
        label.setForeground(new Color(92, 101, 113));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 9.5f));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));

        JPanel grid = new JPanel(new GridLayout(1, 0, 3, 3));
        grid.setOpaque(false);
        for (AbstractButton button : buttons) {
            grid.add(button);
        }

        if ("Puntos".equals(title)) {
            pointSectionPanel = section;
            pointSectionLabel = label;
        } else if ("Lineas".equals(title)) {
            lineSectionPanel = section;
            lineSectionLabel = label;
        } else if ("Poligonos".equals(title)) {
            polygonSectionPanel = section;
            polygonSectionLabel = label;
        } else if ("Vertices".equals(title)) {
            vertexSectionPanel = section;
            vertexSectionLabel = label;
        }

        section.add(label, BorderLayout.NORTH);
        section.add(grid, BorderLayout.CENTER);
        Dimension preferred = section.getPreferredSize();
        section.setMaximumSize(preferred);
        return section;
    }

    private void updateSectionHighlight(JPanel section, JLabel label, boolean active) {
        if (section == null || label == null) {
            return;
        }

        if (active) {
            section.setBackground(new Color(255, 245, 240));
            section.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(211, 101, 86)),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)
            ));
            label.setForeground(new Color(167, 55, 39));
        } else {
            section.setBackground(new Color(246, 247, 249));
            section.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(214, 219, 226)),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)
            ));
            label.setForeground(new Color(92, 101, 113));
        }
    }

    private JToggleButton createToggleButton(String tooltip, javax.swing.Icon icon) {
        JToggleButton button = new JToggleButton(icon);
        styleButton(button, tooltip);
        return button;
    }

    private JButton createActionButton(String tooltip, javax.swing.Icon icon) {
        JButton button = new JButton(icon);
        styleButton(button, tooltip);
        return button;
    }

    private void styleButton(AbstractButton button, String tooltip) {
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setPreferredSize(new Dimension(22, 20));
        button.setBackground(new Color(249, 249, 250));
        button.setBorder(BorderFactory.createLineBorder(new Color(188, 194, 203)));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean pressed = button.getModel().isPressed();
                boolean selected = button instanceof JToggleButton && ((JToggleButton) button).isSelected();
                if (selected) {
                    button.setBackground(new Color(214, 228, 246));
                    button.setBorder(BorderFactory.createLineBorder(new Color(103, 133, 189)));
                } else if (pressed) {
                    button.setBackground(new Color(232, 236, 241));
                    button.setBorder(BorderFactory.createLineBorder(new Color(148, 158, 173)));
                } else {
                    button.setBackground(new Color(249, 249, 250));
                    button.setBorder(BorderFactory.createLineBorder(new Color(188, 194, 203)));
                }
            }
        });
    }

    private void installDragSupport(JPanel header) {
        MouseAdapter dragHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset == null || getParent() == null) {
                    return;
                }

                Point parentPoint = SwingUtilities.convertPoint(header, e.getPoint(), getParent());
                int newX = parentPoint.x - dragOffset.x;
                int newY = parentPoint.y - dragOffset.y;

                Dimension pref = getPreferredSize();
                int parentWidth = getParent().getWidth();
                int parentHeight = getParent().getHeight();

                int maxX = Math.max(12, parentWidth - pref.width - 12);
                int maxY = Math.max(12, parentHeight - pref.height - 12);

                newX = Math.max(12, Math.min(newX, maxX));
                newY = Math.max(12, Math.min(newY, maxY));

                setLocation(newX, newY);
                getParent().repaint();
            }
        };

        header.addMouseListener(dragHandler);
        header.addMouseMotionListener(dragHandler);
    }
}
