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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FloatingVectorEditToolbar extends JPanel {

    private final JToggleButton btnMove;
    private final JToggleButton btnSelect;
    private final JToggleButton btnSnap;
    private final JButton btnZoomSelected;
    private final JButton btnCopy;
    private final JButton btnCopyToEditingLayer;
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

    public FloatingVectorEditToolbar() {
        setOpaque(true);
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(214, 220, 228)),
                BorderFactory.createEmptyBorder(3, 0, 0, 0)
        ));
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(100, 46));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

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

        btnSnap = createSnapToggleButton();
        btnSnap.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.setSnapEnabled(btnSnap.isSelected());
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

        btnCopyToEditingLayer = createActionButton("Copiar elementos seleccionados a la capa en edicion", AppIcons.attrAssignIcon());
        btnCopyToEditingLayer.addActionListener(e -> {
            if (!ensureFeatureSelection("copiar a la capa editable")) {
                return;
            }
            Layer layer = getPreferredEditingLayer();
            if (layer != null) {
                CatgisDesktopApp.mapPanel.prepareLayerForEditing(layer);
            }
            CatgisDesktopApp.mapPanel.copySelectedFeaturesToEditingLayer();
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

        btnIncreaseArea = createActionButton("Aumentar superficie", AppIcons.increaseAreaIcon());
        btnIncreaseArea.addActionListener(e -> {
            if (!ensureFeatureSelection("aumentar la superficie")) {
                return;
            }
            CatgisDesktopApp.mapPanel.increaseSelectedPolygonArea();
            refreshState();
        });

        btnDecreaseArea = createActionButton("Disminuir superficie", AppIcons.decreaseAreaIcon());
        btnDecreaseArea.addActionListener(e -> {
            if (!ensureFeatureSelection("disminuir la superficie")) {
                return;
            }
            CatgisDesktopApp.mapPanel.decreaseSelectedPolygonArea();
            refreshState();
        });

        btnAdjacentPolygon = createActionButton("Generar poligono adyacente", AppIcons.polygonIcon());
        btnAdjacentPolygon.addActionListener(e -> showPendingTool("Poligono adyacente", "La paleta ya reserva esta herramienta, pero todavia falta el motor de construccion adyacente estilo Kosmo."));

        btnMerge = createActionButton("Unir elementos", AppIcons.saveIcon());
        btnMerge.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.mergeSelectedFeatures();
            }
            refreshState();
        });

        btnExplode = createActionButton("Explotar entidades seleccionadas", AppIcons.exportIcon());
        btnExplode.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.explodeSelectedFeatures();
            }
            refreshState();
        });

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
            if (CatgisDesktopApp.mapPanel.isDrawingActive()) {
                CatgisDesktopApp.mapPanel.closeCurrentDrawingSession();
            } else if (CatgisDesktopApp.mapPanel.isMeasurementActive()) {
                CatgisDesktopApp.mapPanel.finishCurrentMeasurement();
            } else if (CatgisDesktopApp.mapPanel.isFeatureEditMode() || CatgisDesktopApp.mapPanel.getEditingLayerRef() != null) {
                CatgisDesktopApp.mapPanel.finishFeatureEdit();
            } else {
                CatgisDesktopApp.mapPanel.closeCurrentDrawingSession();
            }
            refreshState();
        });

        btnCancel = createActionButton("Cancelar", AppIcons.cancelIcon());
        btnCancel.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel == null) {
                return;
            }
            if (CatgisDesktopApp.mapPanel.isDrawingActive()) {
                CatgisDesktopApp.mapPanel.cancelCurrentDrawing();
            } else if (CatgisDesktopApp.mapPanel.isMeasurementActive()) {
                CatgisDesktopApp.mapPanel.cancelCurrentMeasurement();
            } else if (CatgisDesktopApp.mapPanel.isFeatureEditMode() || CatgisDesktopApp.mapPanel.getEditingLayerRef() != null) {
                CatgisDesktopApp.mapPanel.cancelFeatureEdit();
            } else {
                CatgisDesktopApp.mapPanel.cancelCurrentDrawing();
            }
            refreshState();
        });

        addButtons(body);
        add(body, BorderLayout.CENTER);
        refreshState();
    }

    private void addButtons(JPanel body) {
        JPanel strip = new JPanel();
        strip.setOpaque(false);
        strip.setLayout(new BoxLayout(strip, BoxLayout.X_AXIS));
        strip.setAlignmentX(Component.LEFT_ALIGNMENT);

        strip.add(createSection("Seleccion", btnMove, btnSelect, btnSnap, btnZoomSelected, btnCopy, btnCopyToEditingLayer, btnPaste, btnDeleteSelection, btnClearSelection, btnEditAttributes, btnMoveFeature));
        strip.add(Box.createHorizontalStrut(2));
        strip.add(createSection("Puntos", btnPoint, btnMultiPoint));
        strip.add(Box.createHorizontalStrut(2));
        strip.add(createSection("Lineas", btnLine, btnContinueLine, btnCut));
        strip.add(Box.createHorizontalStrut(2));
        strip.add(createSection("Vertices", btnMoveVertex, btnAddVertex, btnRemoveVertex, btnJoinVertices));
        strip.add(Box.createHorizontalStrut(2));
        strip.add(createSection("Poligonos", btnRectangle, btnPolygon, btnSplitPolygon, btnHole, btnIncreaseArea, btnDecreaseArea, btnAdjacentPolygon));
        strip.add(Box.createHorizontalStrut(2));
        strip.add(createSection("Sesion", btnUndo, btnRedo, btnMerge, btnExplode, btnOptions, btnSaveChanges, btnFinish, btnCancel));

        body.add(strip, BorderLayout.CENTER);
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
        boolean snapEnabled = map.isSnapEnabled();
        boolean hasVectorSelection = selectionCount > 0;
        boolean hasEditingLayer = editingLayer != null;
        boolean hasEditableTarget = getPreferredEditingLayer() != null;
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
        btnSnap.setSelected(snapEnabled);
        btnSnap.setToolTipText(snapEnabled ? "Snapping activo" : "Snapping desactivado");
        btnPoint.setSelected("POINT".equalsIgnoreCase(drawingMode));
        btnMultiPoint.setSelected("MULTIPOINT".equalsIgnoreCase(drawingMode));
        btnLine.setSelected("LINE".equalsIgnoreCase(drawingMode));
        btnPolygon.setSelected("POLYGON".equalsIgnoreCase(drawingMode));
        btnMoveVertex.setSelected(editingActive && "MOVE_VERTEX".equalsIgnoreCase(map.getFeatureEditOperation()));

        btnZoomSelected.setEnabled(hasVectorSelection);
        btnCopy.setEnabled(hasVectorSelection);
        btnCopyToEditingLayer.setEnabled(hasVectorSelection && hasEditableTarget);
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
        btnMerge.setEnabled(map.canMergeSelectedFeatures());
        btnExplode.setEnabled(map.canExplodeSelectedFeatures());
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

    private JPanel createSection(String title, AbstractButton... buttons) {
        JPanel section = new JPanel(new BorderLayout(0, 2));
        section.setOpaque(true);
        section.setBackground(new Color(246, 247, 249));
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 219, 226)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setAlignmentY(Component.TOP_ALIGNMENT);

        JLabel label = new JLabel(title.toUpperCase());
        label.setForeground(new Color(92, 101, 113));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 8.7f));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel grid = new JPanel(new GridLayout(1, 0, 2, 2));
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
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            label.setForeground(new Color(167, 55, 39));
        } else {
            section.setBackground(new Color(246, 247, 249));
            section.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(214, 219, 226)),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            label.setForeground(new Color(92, 101, 113));
        }
    }

    private JToggleButton createToggleButton(String tooltip, javax.swing.Icon icon) {
        JToggleButton button = new JToggleButton(icon);
        styleButton(button, tooltip);
        return button;
    }

    private JToggleButton createSnapToggleButton() {
        JToggleButton button = new JToggleButton("SNAP");
        button.setFont(button.getFont().deriveFont(Font.BOLD, 9.5f));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        styleSnapButton(button);
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
        button.setPreferredSize(new Dimension(21, 20));
        button.setMinimumSize(new Dimension(21, 20));
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

    private void styleSnapButton(JToggleButton button) {
        button.setToolTipText("Snapping activo");
        button.setFocusable(false);
        button.setMargin(new Insets(0, 3, 0, 3));
        button.setPreferredSize(new Dimension(40, 20));
        button.setMinimumSize(new Dimension(40, 20));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean selected = button.isSelected();
                boolean pressed = button.getModel().isPressed();
                if (selected) {
                    button.setBackground(pressed ? new Color(34, 197, 94) : new Color(22, 163, 74));
                    button.setForeground(Color.WHITE);
                    button.setBorder(BorderFactory.createLineBorder(new Color(21, 128, 61)));
                } else {
                    button.setBackground(pressed ? new Color(248, 113, 113) : new Color(220, 38, 38));
                    button.setForeground(Color.WHITE);
                    button.setBorder(BorderFactory.createLineBorder(new Color(153, 27, 27)));
                }
            }
        });
        button.setSelected(CatgisDesktopApp.mapPanel == null || CatgisDesktopApp.mapPanel.isSnapEnabled());
    }

}
