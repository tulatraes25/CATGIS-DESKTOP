package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

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

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.Icon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private final JButton btnExtendLine;
    private final JButton btnShortenLine;
    private final JButton btnParallel;
    private final JButton btnPerpendicular;
    private final JButton btnRectangle;
    private final JButton btnCircle;
    private final JButton btnCircle3P;
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
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().enablePanMode();
            }
            refreshState();
        });

        btnSelect = createToggleButton("Seleccionar elemento de la capa editable", AppIcons.selectIcon());
        btnSelect.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                Layer layer = getPreferredEditingLayer();
                if (layer != null) {
                    AppContext.mapPanel().prepareLayerForEditing(layer);
                } else {
                    AppContext.mapPanel().enableSelectMode();
                }
            }
            refreshState();
        });

        btnSnap = createSnapToggleButton();
        btnSnap.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().setSnapEnabled(btnSnap.isSelected());
            }
            refreshState();
        });

        btnZoomSelected = createActionButton("Zoom al elemento seleccionado", AppIcons.zoomInIcon());
        btnZoomSelected.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                if (AppContext.mapPanel().getSelectedFeatureCount() > 1) {
                    AppContext.mapPanel().zoomToFeatureSelection(
                            AppContext.mapPanel().getSelectedLayerRef(),
                            AppContext.mapPanel().getSelectedFeatureIds()
                    );
                } else {
                    AppContext.mapPanel().zoomToSelectedFeature();
                }
            }
            refreshState();
        });

        btnCopy = createActionButton("Copiar elementos seleccionados", AppIcons.attrCopyIcon());
        btnCopy.addActionListener(e -> {
            if (!ensureFeatureSelection("copiar el elemento")) {
                return;
            }
            AppContext.mapPanel().copySelectedFeatures();
            refreshState();
        });

        btnCopyToEditingLayer = createActionButton("Copiar elementos seleccionados a la capa en edicion", AppIcons.attrAssignIcon());
        btnCopyToEditingLayer.addActionListener(e -> {
            if (!ensureFeatureSelection("copiar a la capa editable")) {
                return;
            }
            Layer layer = getPreferredEditingLayer();
            if (layer != null) {
                AppContext.mapPanel().prepareLayerForEditing(layer);
            }
            AppContext.mapPanel().copySelectedFeaturesToEditingLayer();
            refreshState();
        });

        btnPaste = createActionButton("Pegar elementos en la capa en edicion", AppIcons.openIcon());
        btnPaste.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                Layer layer = getPreferredEditingLayer();
                if (layer != null) {
                    AppContext.mapPanel().prepareLayerForEditing(layer);
                }
                AppContext.mapPanel().pasteCopiedFeature();
            }
            refreshState();
        });

        btnDeleteSelection = createActionButton("Borrar elemento seleccionado", AppIcons.removeIcon());
        btnDeleteSelection.addActionListener(e -> {
            if (!ensureFeatureSelection("eliminar el elemento")) {
                return;
            }
            AppContext.mapPanel().deleteSelectedFeatures();
            refreshState();
        });

        btnClearSelection = createActionButton("Limpiar seleccion", AppIcons.attrCloseIcon());
        btnClearSelection.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().clearSelectedFeature();
            }
            refreshState();
        });

        btnEditAttributes = createActionButton("Ver o editar atributos", AppIcons.tableIcon());
        btnEditAttributes.addActionListener(e -> {
            Layer layer = getPreferredEditingLayer();
            if (layer == null) {
                NotificationManager.warn(this, null, "Primero selecciona una capa vectorial.");
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
        btnContinueLine.addActionListener(e -> {
            if (!ensureFeatureSelection("continuar la linea")) {
                return;
            }
            AppContext.mapPanel().enableContinueLineMode();
            refreshState();
        });

        btnExtendLine = createActionButton("Extender linea", AppIcons.extendLineIcon());
        btnExtendLine.addActionListener(e -> {
            if (!ensureFeatureSelection("extender la linea")) {
                return;
            }
            AppContext.mapPanel().activateExtendLineMode();
            refreshState();
        });

        btnShortenLine = createActionButton("Acortar linea", AppIcons.shortenLineIcon());
        btnShortenLine.addActionListener(e -> {
            if (!ensureFeatureSelection("acortar la linea")) {
                return;
            }
            AppContext.mapPanel().activateShortenLineMode();
            refreshState();
        });

        btnParallel = createActionButton("Linea paralela / desplazamiento lateral", AppIcons.parallelIcon());
        btnParallel.addActionListener(e -> {
            if (!ensureFeatureSelection("crear una paralela")) {
                return;
            }
            AppContext.mapPanel().activateParallelLineMode();
            refreshState();
        });

        btnPerpendicular = createActionButton("Linea perpendicular", AppIcons.perpendicularIcon());
        btnPerpendicular.addActionListener(e -> {
            if (!ensureFeatureSelection("crear una perpendicular")) {
                return;
            }
            AppContext.mapPanel().activatePerpendicularLineMode();
            refreshState();
        });

        btnRectangle = createActionButton("Dibujar rectangulo", AppIcons.rectangleIcon());
        btnRectangle.addActionListener(e -> activateDrawMode("RECTANGLE"));

        btnCircle = createActionButton("Dibujar circulo", AppIcons.circleIcon());
        btnCircle.addActionListener(e -> activateDrawMode("CIRCLE"));

        btnCircle3P = createActionButton("Dibujar circulo por 3 puntos", AppIcons.circleThreePointsIcon());
        btnCircle3P.addActionListener(e -> activateDrawMode("CIRCLE_3P"));

        btnPolygon = createToggleButton("Dibujar poligono", AppIcons.polygonIcon());
        btnPolygon.addActionListener(e -> activateDrawMode("POLYGON"));

        btnMoveFeature = createActionButton("Mover elementos seleccionados", AppIcons.moveFeatureIcon());
        btnMoveFeature.addActionListener(e -> {
            if (!ensureFeatureSelection("mover los elementos")) {
                return;
            }
            AppContext.mapPanel().activateMoveFeatureMode();
            refreshState();
        });

        btnMoveVertex = createToggleButton("Mover vertice", AppIcons.moveVertexIcon());
        btnMoveVertex.addActionListener(e -> activateMoveVertexMode());

        btnAddVertex = createActionButton("Insertar vertice", AppIcons.addVertexIcon());
        btnAddVertex.addActionListener(e -> activateAddVertexMode());

        btnRemoveVertex = createActionButton("Borrar vertice", AppIcons.removeVertexIcon());
        btnRemoveVertex.addActionListener(e -> activateRemoveVertexMode());

        btnJoinVertices = createActionButton("Unir vertices", AppIcons.joinVerticesIcon());
        btnJoinVertices.addActionListener(e -> activateJoinVerticesMode());

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
            AppContext.mapPanel().increaseSelectedPolygonArea();
            refreshState();
        });

        btnDecreaseArea = createActionButton("Disminuir superficie", AppIcons.decreaseAreaIcon());
        btnDecreaseArea.addActionListener(e -> {
            if (!ensureFeatureSelection("disminuir la superficie")) {
                return;
            }
            AppContext.mapPanel().decreaseSelectedPolygonArea();
            refreshState();
        });

        btnAdjacentPolygon = createActionButton("Generar poligono adyacente", AppIcons.polygonIcon());
        btnAdjacentPolygon.addActionListener(e -> activateAdjacentPolygonMode());

        btnMerge = createActionButton("Unir elementos", AppIcons.saveIcon());
        btnMerge.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().mergeSelectedFeatures();
            }
            refreshState();
        });

        btnExplode = createActionButton("Explotar entidades seleccionadas", AppIcons.exportIcon());
        btnExplode.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().explodeSelectedFeatures();
            }
            refreshState();
        });

        btnUndo = createActionButton("Deshacer", AppIcons.undoIcon());
        btnUndo.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().undoFeatureEdit();
            }
            refreshState();
        });

        btnRedo = createActionButton("Rehacer", AppIcons.redoIcon());
        btnRedo.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().redoFeatureEdit();
            }
            refreshState();
        });

        btnOptions = createActionButton("Opciones de capa", AppIcons.propertiesIcon());
        btnOptions.addActionListener(e -> {
            Layer layer = getPreferredEditingLayer();
            if (layer == null) {
                NotificationManager.warn(this, null, "Primero selecciona una capa vectorial.");
                return;
            }
            LayerPropertiesDialog.open(layer);
        });

        btnSaveChanges = createActionButton("Salvar cambios", AppIcons.saveIcon());
        btnSaveChanges.addActionListener(e -> {
            if (AppContext.mapPanel() != null && AppContext.mapPanel().isFeatureEditMode()) {
                AppContext.mapPanel().saveFeatureEditChanges();
            } else {
                SaveProjectAction.saveProject();
            }
            refreshState();
        });

        btnFinish = createActionButton("Terminar", AppIcons.finishIcon());
        btnFinish.addActionListener(e -> {
            if (AppContext.mapPanel() == null) {
                return;
            }
            if (AppContext.mapPanel().isDrawingActive()) {
                AppContext.mapPanel().closeCurrentDrawingSession();
            } else if (AppContext.mapPanel().isMeasurementActive()) {
                AppContext.mapPanel().finishCurrentMeasurement();
            } else if (AppContext.mapPanel().isFeatureEditMode() || AppContext.mapPanel().getEditingLayerRef() != null) {
                AppContext.mapPanel().finishFeatureEdit();
            } else {
                AppContext.mapPanel().closeCurrentDrawingSession();
            }
            refreshState();
        });

        btnCancel = createActionButton("Cancelar", AppIcons.cancelIcon());
        btnCancel.addActionListener(e -> {
            if (AppContext.mapPanel() == null) {
                return;
            }
            if (AppContext.mapPanel().isDrawingActive()) {
                AppContext.mapPanel().cancelCurrentDrawing();
            } else if (AppContext.mapPanel().isMeasurementActive()) {
                AppContext.mapPanel().cancelCurrentMeasurement();
            } else if (AppContext.mapPanel().isFeatureEditMode() || AppContext.mapPanel().getEditingLayerRef() != null) {
                AppContext.mapPanel().cancelFeatureEdit();
            } else {
                AppContext.mapPanel().cancelCurrentDrawing();
            }
            refreshState();
        });

        addButtons(body);
        add(body, BorderLayout.CENTER);
        refreshState();
    }

    private void addButtons(JPanel body) {
        JPanel strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        strip.setOpaque(false);
        strip.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));

        addToolBtn(strip, btnSelect);
        addToolBtn(strip, btnMove);
        addToolBtn(strip, btnSnap);
        addToolBtn(strip, btnEditAttributes);
        addToolBtn(strip, btnZoomSelected);
        addSep(strip);

        addDropButton(strip, "Crear \u25BC", btnPoint, btnMultiPoint, btnLine, btnRectangle, btnCircle, btnPolygon);
        addSep(strip);

        addToolBtn(strip, btnMoveVertex);
        addToolBtn(strip, btnAddVertex);
        addToolBtn(strip, btnRemoveVertex);
        addToolBtn(strip, btnJoinVertices);
        addSep(strip);

        addDropButton(strip, "Editar \u25BC", btnMoveFeature, btnCut, btnContinueLine, btnExtendLine,
                btnShortenLine, btnParallel, btnPerpendicular, btnSplitPolygon, btnHole,
                btnIncreaseArea, btnDecreaseArea, btnAdjacentPolygon, btnCircle3P,
                btnCopy, btnPaste, btnDeleteSelection, btnClearSelection, btnCopyToEditingLayer, btnMerge, btnExplode);
        addSep(strip);

        addToolBtn(strip, btnUndo);
        addToolBtn(strip, btnRedo);
        addSep(strip);

        addToolBtn(strip, btnSaveChanges);
        addToolBtn(strip, btnFinish);
        addToolBtn(strip, btnCancel);

        body.add(strip, BorderLayout.CENTER);
    }

    private void addToolBtn(JPanel strip, AbstractButton btn) {
        if (btn == null) return;
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setText("");
        btn.setPreferredSize(new Dimension(32, 32));
        strip.add(btn);
    }

    private void addDropButton(JPanel strip, String label, AbstractButton... buttons) {
        JButton dropBtn = new JButton(label);
        dropBtn.setFont(dropBtn.getFont().deriveFont(Font.PLAIN, 10f));
        dropBtn.setFocusable(false);
        dropBtn.setMargin(new Insets(2, 6, 2, 6));
        dropBtn.setContentAreaFilled(false);
        dropBtn.setBorderPainted(false);
        dropBtn.setOpaque(false);
        dropBtn.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            for (AbstractButton btn : buttons) {
                if (btn == null) continue;
                JMenuItem item = new JMenuItem(btn.getToolTipText() != null ? btn.getToolTipText() : "");
                item.addActionListener(ev -> btn.doClick());
                menu.add(item);
            }
            menu.show(dropBtn, 0, dropBtn.getHeight());
        });
        dropBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { dropBtn.setOpaque(true); dropBtn.setBackground(new Color(0xE0E0E0)); }
            public void mouseExited(MouseEvent e) { dropBtn.setOpaque(false); dropBtn.repaint(); }
        });
        strip.add(dropBtn);
    }

    private void addSep(JPanel strip) {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        sep.setForeground(new Color(0xCCCCCC));
        sep.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        strip.add(sep);
    }

    public void refreshState() {
        MapPanel map = AppContext.mapPanel();
        if (map == null) {
            return;
        }
        boolean cadModuleEnabled = ModuleRegistry.isModuleEnabled(ModuleRegistry.MODULE_CAD);
        if (!cadModuleEnabled) {
            setCadControlsEnabled(false);
            updateSectionHighlight(pointSectionPanel, pointSectionLabel, false);
            updateSectionHighlight(lineSectionPanel, lineSectionLabel, false);
            updateSectionHighlight(polygonSectionPanel, polygonSectionLabel, false);
            updateSectionHighlight(vertexSectionPanel, vertexSectionLabel, false);
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
        boolean selectedLayerReadOnly = map.isReadOnlyVectorLayer(selectedLayer);
        boolean editingLayerReadOnly = map.isReadOnlyVectorLayer(editingLayer);
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
        if (map.isReadOnlyVectorLayer(geometryContextLayer)) {
            geometryContextLayer = null;
        }
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
        btnPaste.setEnabled(hasEditingLayer && !editingLayerReadOnly && map.hasCopiedFeature());
        btnDeleteSelection.setEnabled(hasVectorSelection && !selectedLayerReadOnly);
        btnClearSelection.setEnabled(hasVectorSelection || hasEditingLayer);
        btnEditAttributes.setEnabled(hasEditingLayer || hasVectorSelection);
        btnPoint.setEnabled(canDrawPoint);
        btnMultiPoint.setEnabled(canDrawPoint);
        btnLine.setEnabled(canDrawLine);
        btnContinueLine.setEnabled(lineSelection && !selectedLayerReadOnly);
        btnExtendLine.setEnabled(lineSelection && !selectedLayerReadOnly);
        btnShortenLine.setEnabled(lineSelection && !selectedLayerReadOnly);
        btnParallel.setEnabled(lineSelection && !selectedLayerReadOnly);
        btnPerpendicular.setEnabled(lineSelection && !selectedLayerReadOnly);
        btnRectangle.setEnabled(canDrawPolygon);
        btnCircle.setEnabled(canDrawPolygon);
        btnCircle3P.setEnabled(canDrawPolygon);
        btnPolygon.setEnabled(canDrawPolygon);
        btnMoveFeature.setEnabled(hasVectorSelection && !selectedLayerReadOnly);
        btnMoveVertex.setEnabled(linearOrPolygonalSelection && !selectedLayerReadOnly);
        btnAddVertex.setEnabled(linearOrPolygonalSelection && !selectedLayerReadOnly);
        btnRemoveVertex.setEnabled(linearOrPolygonalSelection && !selectedLayerReadOnly);
        btnJoinVertices.setEnabled(linearOrPolygonalSelection && !selectedLayerReadOnly);
        btnCut.setEnabled(linearOrPolygonalSelection && !selectedLayerReadOnly);
        btnSplitPolygon.setEnabled(polygonSelection && !selectedLayerReadOnly);
        btnHole.setEnabled(polygonSelection && !selectedLayerReadOnly);
        btnIncreaseArea.setEnabled(polygonSelection && !selectedLayerReadOnly);
        btnDecreaseArea.setEnabled(polygonSelection && !selectedLayerReadOnly);
        btnAdjacentPolygon.setEnabled(polygonSelection && !selectedLayerReadOnly);
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

    private void setCadControlsEnabled(boolean enabled) {
        btnMove.setEnabled(enabled);
        btnSelect.setEnabled(enabled);
        btnSnap.setEnabled(enabled);
        btnZoomSelected.setEnabled(enabled);
        btnCopy.setEnabled(enabled);
        btnCopyToEditingLayer.setEnabled(enabled);
        btnPaste.setEnabled(enabled);
        btnDeleteSelection.setEnabled(enabled);
        btnClearSelection.setEnabled(enabled);
        btnEditAttributes.setEnabled(enabled);
        btnPoint.setEnabled(enabled);
        btnMultiPoint.setEnabled(enabled);
        btnLine.setEnabled(enabled);
        btnContinueLine.setEnabled(enabled);
        btnExtendLine.setEnabled(enabled);
        btnShortenLine.setEnabled(enabled);
        btnParallel.setEnabled(enabled);
        btnPerpendicular.setEnabled(enabled);
        btnRectangle.setEnabled(enabled);
        btnCircle.setEnabled(enabled);
        btnCircle3P.setEnabled(enabled);
        btnPolygon.setEnabled(enabled);
        btnMoveFeature.setEnabled(enabled);
        btnMoveVertex.setEnabled(enabled);
        btnAddVertex.setEnabled(enabled);
        btnRemoveVertex.setEnabled(enabled);
        btnJoinVertices.setEnabled(enabled);
        btnCut.setEnabled(enabled);
        btnSplitPolygon.setEnabled(enabled);
        btnHole.setEnabled(enabled);
        btnIncreaseArea.setEnabled(enabled);
        btnDecreaseArea.setEnabled(enabled);
        btnAdjacentPolygon.setEnabled(enabled);
        btnMerge.setEnabled(enabled);
        btnExplode.setEnabled(enabled);
        btnUndo.setEnabled(enabled);
        btnRedo.setEnabled(enabled);
        btnOptions.setEnabled(enabled);
        btnSaveChanges.setEnabled(enabled);
        btnFinish.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
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

        if (AppContext.mapPanel() != null) {
            ShapefileData data = AppContext.mapPanel().getShapefileData(layer);
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
        if (AppContext.mapPanel() == null) {
            return;
        }

        if ("POINT".equalsIgnoreCase(mode)) {
            AppContext.mapPanel().enableDrawPointMode();
        } else if ("MULTIPOINT".equalsIgnoreCase(mode)) {
            AppContext.mapPanel().enableDrawMultiPointMode();
        } else if ("LINE".equalsIgnoreCase(mode)) {
            AppContext.mapPanel().enableDrawLineMode();
        } else if ("CIRCLE".equalsIgnoreCase(mode)) {
            AppContext.mapPanel().enableDrawCircleMode();
        } else if ("CIRCLE_3P".equalsIgnoreCase(mode)) {
            AppContext.mapPanel().enableDrawCircleThreePointMode();
        } else if ("RECTANGLE".equalsIgnoreCase(mode)) {
            AppContext.mapPanel().enableDrawRectangleMode();
        } else if ("POLYGON".equalsIgnoreCase(mode)) {
            AppContext.mapPanel().enableDrawPolygonMode();
        }

        refreshState();
    }

    private Layer getPreferredEditingLayer() {
        if (AppContext.mapPanel() != null && AppContext.mapPanel().getEditingLayerRef() != null) {
            Layer editing = AppContext.mapPanel().getEditingLayerRef();
            if (!AppContext.mapPanel().isReadOnlyVectorLayer(editing)) {
                return editing;
            }
        }
        if (AppContext.mapPanel() != null && AppContext.mapPanel().getSelectedLayerRef() != null) {
            Layer selected = AppContext.mapPanel().getSelectedLayerRef();
            if (!AppContext.mapPanel().isReadOnlyVectorLayer(selected)) {
                return selected;
            }
        }
        if (CatgisDesktopApp.layersPanel != null) {
            Layer selected = AppContext.getSelectedLayer();
            if (selected != null && !(selected instanceof RasterLayer)
                    && (AppContext.mapPanel() == null || !AppContext.mapPanel().isReadOnlyVectorLayer(selected))) {
                return selected;
            }
        }
        return null;
    }

    private void activateMoveVertexMode() {
        if (AppContext.mapPanel() == null) {
            return;
        }

        Layer layer = AppContext.mapPanel().getSelectedLayerRef();
        SimpleFeature feature = AppContext.mapPanel().getSelectedFeatureRef();
        if (layer == null || feature == null) {
            NotificationManager.info(
                    this,
                    "Edicion vectorial",
                    "Primero selecciona una linea o poligono con la flecha de seleccion dentro de la capa editable.");
            refreshState();
            return;
        }

        AppContext.mapPanel().enableFeatureEdit(layer, feature);
        AppContext.mapPanel().activateMoveVertexMode();
        refreshState();
    }

    private void activateAddVertexMode() {
        if (!ensureFeatureSelection("insertar un vertice")) {
            return;
        }
        AppContext.mapPanel().activateAddVertexMode();
        refreshState();
    }

    private void activateRemoveVertexMode() {
        if (!ensureFeatureSelection("borrar un vertice")) {
            return;
        }
        AppContext.mapPanel().activateRemoveVertexMode();
        refreshState();
    }

    private void activateJoinVerticesMode() {
        if (!ensureFeatureSelection("unir vertices")) {
            return;
        }
        AppContext.mapPanel().activateJoinVerticesMode();
        refreshState();
    }

    private void activateCutFeatureMode() {
        if (!ensureFeatureSelection("cortar la geometria")) {
            return;
        }
        AppContext.mapPanel().activateCutFeatureMode();
        refreshState();
    }

    private void activateHoleMode() {
        if (!ensureFeatureSelection("crear un agujero")) {
            return;
        }
        AppContext.mapPanel().activateHoleMode();
        refreshState();
    }

    private void activateAdjacentPolygonMode() {
        if (!ensureFeatureSelection("generar un poligono adyacente")) {
            return;
        }
        AppContext.mapPanel().activateAdjacentPolygonMode();
        refreshState();
    }

    private boolean ensureFeatureSelection(String actionName) {
        if (AppContext.mapPanel() == null) {
            return false;
        }
        Layer layer = AppContext.mapPanel().getSelectedLayerRef();
        SimpleFeature feature = AppContext.mapPanel().getSelectedFeatureRef();
        if (layer == null || feature == null) {
            Layer editingLayer = getPreferredEditingLayer();
            if (editingLayer != null) {
                AppContext.mapPanel().prepareLayerForEditing(editingLayer);
            }
            NotificationManager.info(
                    this,
                    "Edicion vectorial",
                    "Primero selecciona una entidad de la capa editable con la flecha para " + actionName + ".");
            refreshState();
            return false;
        }
        AppContext.mapPanel().enableFeatureEdit(layer, feature);
        return true;
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
        Icon icon = button.getIcon();
        if (icon != null) {
            button.setDisabledIcon(icon);
            button.setPressedIcon(icon);
            if (button instanceof JToggleButton toggle) {
                toggle.setDisabledSelectedIcon(icon);
            }
        }
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
        button.setSelected(AppContext.mapPanel() == null || AppContext.mapPanel().isSnapEnabled());
    }

    public static void triggerDrawPoint() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnPoint); }
    public static void triggerDrawMultiPoint() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnMultiPoint); }
    public static void triggerDrawLine() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnLine); }
    public static void triggerDrawRectangle() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnRectangle); }
    public static void triggerDrawPolygon() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnPolygon); }
    public static void triggerMoveVertex() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnMoveVertex); }
    public static void triggerAddVertex() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnAddVertex); }
    public static void triggerRemoveVertex() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnRemoveVertex); }
    public static void triggerJoinVertices() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnJoinVertices); }
    public static void triggerCut() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnCut); }
    public static void triggerMoveFeature() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnMoveFeature); }
    public static void triggerDeleteSelection() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnDeleteSelection); }
    public static void triggerSaveChanges() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnSaveChanges); }
    public static void triggerCancel() { clickStaticBtn(CatgisDesktopApp.floatingVectorEditToolbar.btnCancel); }

    private static void clickStaticBtn(AbstractButton btn) {
        if (btn != null && btn.isEnabled()) btn.doClick();
    }
}
