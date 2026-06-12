package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Configures keyboard shortcuts for MapPanel.
 * Extracted from MapPanel to reduce its scope.
 */
public class KeyboardConfig {
    
    private final MapPanel panel;
    
    public KeyboardConfig(MapPanel panel) {
        this.panel = panel;
    }
    
    public void configure() {
        int shortcutMask;
        try {
            shortcutMask = java.awt.GraphicsEnvironment.isHeadless()
                    ? InputEvent.CTRL_DOWN_MASK
                    : Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        } catch (HeadlessException ex) {
            shortcutMask = InputEvent.CTRL_DOWN_MASK;
        }

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelSketchOrMeasurement");
        panel.getActionMap().put("cancelSketchOrMeasurement", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panel.cadEngine.cadPlacementDragActive) {
                    panel.cancelCadPlacementDrag();
                } else if (panel.cadEngine.pointCaptureActive) {
                    panel.cancelPointCapture();
                } else if (panel.topographicProfileCaptureActive) {
                    panel.cancelTopographicProfileCapture();
                } else if (panel.featureEditMode && !panel.featureEditSketchCoordinates.isEmpty()) {
                    panel.featureEditSketchCoordinates.clear();
                    panel.repaint();
                    panel.showCopiedMessage("Boceto de edición cancelado.");
                } else if (panel.isDrawingActive()) {
                    panel.cancelCurrentDrawing();
                    panel.showCopiedMessage("Dibujo cancelado.");
                } else if (panel.isMeasurementActive()) {
                    panel.cancelCurrentMeasurement();
                    panel.showCopiedMessage("Medición cancelada.");
                }
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutMask), "copySelectedFeatures");
        panel.getActionMap().put("copySelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.copySelectedFeatures();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutMask), "cutSelectedFeatures");
        panel.getActionMap().put("cutSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.cutSelectedFeatures();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutMask), "pasteSelectedFeatures");
        panel.getActionMap().put("pasteSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Layer editingLayer = panel.getEditingLayerRef();
                if (editingLayer == null && panel.selectedLayer != null && !(panel.selectedLayer instanceof RasterLayer)) {
                    panel.prepareLayerForEditing(panel.selectedLayer);
                }
                panel.pasteCopiedFeatures();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "copySelectionToEditingLayer"
        );
        panel.getActionMap().put("copySelectionToEditingLayer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.copySelectedFeaturesToEditingLayer();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelectedFeatures");
        panel.getActionMap().put("deleteSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.deleteSelectedFeatures();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutMask), "undoFeatureEdit");
        panel.getActionMap().put("undoFeatureEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.undoFeatureEdit();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcutMask), "redoFeatureEdit");
        panel.getActionMap().put("redoFeatureEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.redoFeatureEdit();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcutMask), "saveFeatureEditChanges");
        panel.getActionMap().put("saveFeatureEditChanges", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.saveFeatureEditChanges();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, shortcutMask), "finishFeatureEdit");
        panel.getActionMap().put("finishFeatureEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.finishFeatureEdit();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcutMask), "moveSelectedFeatures");
        panel.getActionMap().put("moveSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.activateMoveFeatureMode();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcutMask), "cutSelectedGeometry");
        panel.getActionMap().put("cutSelectedGeometry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.activateCutFeatureMode();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_J, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "mergeSelectedFeatures"
        );
        panel.getActionMap().put("mergeSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.mergeSelectedFeatures();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "explodeSelectedFeatures"
        );
        panel.getActionMap().put("explodeSelectedFeatures", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.explodeSelectedFeatures();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_U, shortcutMask | InputEvent.SHIFT_DOWN_MASK),
                "joinSelectedVertices"
        );
        panel.getActionMap().put("joinSelectedVertices", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.activateJoinVerticesMode();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK), "zoomPreviousView");
        panel.getActionMap().put("zoomPreviousView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.zoomPrevious();
            }
        });

        panel.getInputMap(MapPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK), "zoomNextView");
        panel.getActionMap().put("zoomNextView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.zoomNext();
            }
        });
    }
}
