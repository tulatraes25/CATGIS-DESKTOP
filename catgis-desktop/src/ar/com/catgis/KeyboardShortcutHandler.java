package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

class KeyboardShortcutHandler {

    private final MapPanel map;

    KeyboardShortcutHandler(MapPanel map) {
        this.map = map;
    }

    void configureKeyboardShortcuts() {
        int shortcutMask;
        try {
            shortcutMask = java.awt.GraphicsEnvironment.isHeadless()
                    ? InputEvent.CTRL_DOWN_MASK
                    : Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        } catch (HeadlessException ex) {
            shortcutMask = InputEvent.CTRL_DOWN_MASK;
        }

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelSketchOrMeasurement");
        map.getActionMap().put("cancelSketchOrMeasurement", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (map.cadPlacementDragActive) {
                    map.cancelCadPlacementDrag();
                } else if (map.pointCaptureActive) {
                    map.cancelPointCapture();
                } else if (map.topographicProfileCaptureActive) {
                    map.cancelTopographicProfileCapture();
                } else if (map.featureEditMode && !map.featureEditSketchCoordinates.isEmpty()) {
                    map.featureEditSketchCoordinates.clear();
                    map.repaint();
                    map.showCopiedMessage("Boceto de edici\u00f3n cancelado.");
                } else if (map.isDrawingActive()) {
                    map.cancelCurrentDrawing();
                    map.showCopiedMessage("Dibujo cancelado.");
                } else if (map.isMeasurementActive()) {
                    map.cancelCurrentMeasurement();
                    map.showCopiedMessage("Medición cancelada.");
                }
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutMask), "copySelectedFeatures");
        map.getActionMap().put("copySelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.copySelectedFeatures();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutMask), "cutSelectedFeatures");
        map.getActionMap().put("cutSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.cutSelectedFeatures();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutMask), "pasteSelectedFeatures");
        map.getActionMap().put("pasteSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Layer editingLayer = map.getEditingLayerRef();
                if (editingLayer == null && map.selectedLayer != null && !(map.selectedLayer instanceof RasterLayer)) {
                    map.prepareLayerForEditing(map.selectedLayer);
                }
                map.pasteCopiedFeatures();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "copySelectionToEditingLayer"
        );
        map.getActionMap().put("copySelectionToEditingLayer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.copySelectedFeaturesToEditingLayer();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelectedFeatures");
        map.getActionMap().put("deleteSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.deleteSelectedFeatures();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutMask), "undoFeatureEdit");
        map.getActionMap().put("undoFeatureEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.undoFeatureEdit();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcutMask), "redoFeatureEdit");
        map.getActionMap().put("redoFeatureEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.redoFeatureEdit();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcutMask), "saveFeatureEditChanges");
        map.getActionMap().put("saveFeatureEditChanges", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.saveFeatureEditChanges();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, shortcutMask), "finishFeatureEdit");
        map.getActionMap().put("finishFeatureEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.finishFeatureEdit();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcutMask), "moveSelectedFeatures");
        map.getActionMap().put("moveSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.activateMoveFeatureMode();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcutMask), "cutSelectedGeometry");
        map.getActionMap().put("cutSelectedGeometry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.activateCutFeatureMode();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_J, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "mergeSelectedFeatures"
        );
        map.getActionMap().put("mergeSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.mergeSelectedFeatures();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "explodeSelectedFeatures"
        );
        map.getActionMap().put("explodeSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.explodeSelectedFeatures();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_U, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "joinSelectedVertices"
        );
        map.getActionMap().put("joinSelectedVertices", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.activateJoinVerticesMode();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK), "zoomPreviousView");
        map.getActionMap().put("zoomPreviousView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.zoomPrevious();
            }
        });

        map.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK), "zoomNextView");
        map.getActionMap().put("zoomNextView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map.zoomNext();
            }
        });
    }
}
