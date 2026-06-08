const fs = require('fs');
const path = 'C:/CATGIS/catgis-desktop/src/ar/com/catgis/MapPanel.java';
let src = fs.readFileSync(path, 'utf8');

// ============ 1. Add import for MapRenderer (not needed, same package) ============

// ============ 2. Add renderer field after class declaration ============
src = src.replace(
  'public class MapPanel extends JPanel {\n\n    private final Map<Layer, ShapefileData>',
  'public class MapPanel extends JPanel {\n    private final MapRenderer renderer = new MapRenderer(this);\n\n    private final Map<Layer, ShapefileData>'
);

// ============ 3. Change field visibility to package-private ============
const fieldsToChange = [
  'shapefileLayers', 'rasterLayers', 'onlineTileLayers', 'onlineWmsLayers',
  'mapDecorations', 'rasterStyles', 'rasterDisplayCache',
  'selectionGeometryFactory',
  'onlineResolutionNoticeVisible', 'onlineResolutionNotice',
  'pins',
  'drawingCoordinates', 'pendingDrawingSessionGeometries', 'drawingMode',
  'drawingSessionLayer',
  'drawingContinuationLayer', 'drawingContinuationFeatureId',
  'drawingContinuationBaseCoordinates', 'drawingContinuationFromStart',
  'drawingContinuationEndpointChosen',
  'measurementCoordinates', 'measurementMode',
  'hoverWorldX', 'hoverWorldY',
  'viewMinX', 'viewMinY', 'zoomFactor',
  'layoutRenderMode',
  'selectedLayer', 'selectedFeature',
  'tableSelectionIds',
  'featureEditMode', 'featureEditOperation',
  'featureEditSketchCoordinates',
  'adjacentPolygonSegmentStart', 'adjacentPolygonSegmentEnd',
  'cadReferenceSegmentStart', 'cadReferenceSegmentEnd',
  'cadReferenceFromStart', 'cadReferenceEndpointChosen',
  'openedFileText',
  'selectionBoxActive', 'selectionBoxStartX', 'selectionBoxStartY',
  'selectionBoxEndX', 'selectionBoxEndY',
  'movingSelectedFeatures', 'moveSelectionLastProjectX', 'moveSelectionLastProjectY',
  'selectionFlashGeometry', 'selectionFlashStartedAt',
  'snapPreviewCoordinate', 'snapEnabled',
  'topographicProfileCaptureActive', 'topographicProfileCaptureCoordinates',
  'selectionBoxDragging', 'selectionFlashTimer', 'activeEditVertexIndex',
  'joinTargetVertexIndex', 'globalLabelBoxes',
  'nextPinId', 'activePin', 'drawingSessionDirty',
  'paintComponent'
];

// We don't change static constants - they need special handling

for (const f of fieldsToChange) {
  // Match lines like: private [modifiers] [type...] fieldName =
  // Use word boundaries to avoid partial matches
  const regex = new RegExp('^(\\s+)private\\s+(?:(?:static|final|synchronized)\\s+)*(?:(?:\\w+(?:<[^>]*>)?)\\s+)+' + f + '\\b', 'm');
  src = src.replace(regex, (match, indent) => {
    return match.replace(/private\s+/, '');
  });
}

// Change private static final constants to package-private
const constantsToChange = [
  'EDIT_OP_MOVE_VERTEX', 'EDIT_OP_ADD_VERTEX', 'EDIT_OP_REMOVE_VERTEX',
  'EDIT_OP_JOIN_VERTEX', 'EDIT_OP_ADJACENT_POLYGON', 'EDIT_OP_MOVE_FEATURE',
  'EDIT_OP_CUT', 'EDIT_OP_HOLE', 'EDIT_OP_EXTEND_LINE', 'EDIT_OP_SHORTEN_LINE',
  'EDIT_OP_PARALLEL', 'EDIT_OP_PERPENDICULAR',
  'EDIT_SEGMENT_TOLERANCE_PX', 'EDIT_VERTEX_TOLERANCE_PX',
  'SNAP_TOLERANCE_PX', 'MAX_EDIT_HISTORY', 'CIRCLE_SEGMENTS',
  'SELECTION_BOX_DRAG_THRESHOLD_PX', 'SELECTION_FLASH_DURATION_MS'
];

for (const c of constantsToChange) {
  const regex = new RegExp('^(\\s+)private\\s+static\\s+final\\s+\\w+\\s+' + c + '\\s*=', 'm');
  src = src.replace(regex, (match, indent) => {
    return match.replace(/private\s+/, '');
  });
}

// ============ 4. Change method visibility to public ============
const methodsToChange = [
  'getRenderOrderLayers', 'isLayerEffectivelyVisible',
  'worldToScreenX', 'worldToScreenY',
  'screenToWorldX', 'screenToWorldY',
  'isDrawingActive', 'isMeasurementActive',
  'findPinAtScreen', 'hasFeatureSelection',
  'isHitOnCurrentSelection', 'findEditableVertexIndex',
  'hasValidSnapTarget', 'findSnapPoint',
  'showCopiedMessage', 'refreshStatusBarScale',
  'getSelectionBoxBounds', 'getRasterEnvelope',
  'getOrCreateRasterStyle', 'projectEnvelopeToMercator',
  'reprojectEnvelopeIfNeeded', 'resolveFallbackOnlineTile',
  'pushTileStatusToBar', 'buildWmsGetMapUrl',
  'buildVisibleOnlineAttribution', 'resolveInteractivePreviewCoordinate',
  'buildCircleGeometry', 'buildCircleThreePointGeometry',
  'buildRectangleCoordinates', 'toSourceCoordinate',
  'buildAdjacentPolygonGeometry', 'reprojectGeometryIfNeeded',
  'extractFeatureGeometryCopy', 'buildAdjustedSelectedLineGeometry',
  'extractContinuableLineCoordinates', 'forEachVisibleFeatureGeometry',
  'buildPathFromCoordinates', 'isFeatureVisibleInLayer',
  'findFeatureById', 'sameFeatureId',
  'getEditableDisplayGeometry', 'getEditableVertexCoordinates',
  'isCadLineConstructionMode', 'isFeatureEditSketchMode',
  'isSelectedFeatureLinearOrPolygonal', 'isSelectedFeatureLinear',
  'isSelectedFeaturePolygonal', 'resolveContinuationLineTargetCoordinates',
  'getLabelCoordinate', 'getCachedDisplayImage',
  'toProjectCoordinate', 'buildParallelLineGeometry',
  'buildPerpendicularLineGeometry', 'resolveSelectionFlashCoordinate',
  'zoomToSelectedLayer', 'getEditingLayerRef',
  'isReadOnlyVectorLayer', 'isLayerArmedForEditing',
  'isFeatureEditMode', 'getFeatureEditOperation',
  'isSnapEnabled', 'getCurrentTool',
  'getSelectedLayerRef', 'getSelectedFeatureRef'
];

for (const m of methodsToChange) {
  // Match: private [modifiers] [type] methodName(
  const regex = new RegExp('^(\\s+)private\\s+(?:(?:static|synchronized|final)\\s+)*(?:\\w+(?:<[^>]*>)?(?:\\[\\])?\\s+)+' + m + '\\s*\\(', 'm');
  src = src.replace(regex, (match, indent) => {
    return match.replace(/private\s+/, 'public ');
  });
}

// ============ 5. Modify paintComponent to delegate ============
const paintRegex = /protected void paintComponent\(Graphics g\) \{[\s\S]*?super\.paintComponent\(g\);[\s\S]*?Graphics2D g2 = \(Graphics2D\) g\.create\(\);[\s\S]*?try \{[\s\S]*?\} finally \{[\s\S]*?g2\.dispose\(\);[\s\S]*?\}[\s\S]*?\}/;
const newPaint = `    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            renderer.render(g2, getWidth(), getHeight());
        } finally {
            g2.dispose();
        }
    }`;
src = src.replace(paintRegex, newPaint);

// ============ 6. Modify renderMapViewImage methods to delegate ============
// Replace all 3 renderMapViewImage overloads
const rmv3 = /public BufferedImage renderMapViewImage\(double renderViewMinX, double renderViewMinY, double renderZoomFactor\) \{[\s\S]*?return renderMapViewImage\(renderViewMinX, renderViewMinY, renderZoomFactor, renderWidth, renderHeight\);\s*\}/;
src = src.replace(rmv3, `    public BufferedImage renderMapViewImage(double renderViewMinX, double renderViewMinY, double renderZoomFactor) {
        return renderer.renderMapViewImage(renderViewMinX, renderViewMinY, renderZoomFactor);
    }`);

const rmv5 = /public BufferedImage renderMapViewImage\(double renderViewMinX, double renderViewMinY, double renderZoomFactor, int renderWidth, int renderHeight\) \{[\s\S]*?return renderMapViewImage\(renderViewMinX, renderViewMinY, renderZoomFactor, renderWidth, renderHeight, false\);\s*\}/;
src = src.replace(rmv5, `    public BufferedImage renderMapViewImage(double renderViewMinX, double renderViewMinY, double renderZoomFactor, int renderWidth, int renderHeight) {
        return renderer.renderMapViewImage(renderViewMinX, renderViewMinY, renderZoomFactor, renderWidth, renderHeight, false);
    }`);

const rmv6 = /public BufferedImage renderMapViewImage\(double renderViewMinX, double renderViewMinY, double renderZoomFactor, int renderWidth, int renderHeight, boolean includeDecorations\) \{[\s\S]*?if \(renderZoomFactor <= 0\) \{[\s\S]*?return null;[\s\S]*?\}[\s\S]*?BufferedImage image = new BufferedImage\([\s\S]*?Graphics2D g2 = image\.createGraphics\(\);[\s\S]*?try \{[\s\S]*?paintComponent\(g2\);[\s\S]*?\} finally \{[\s\S]*?g2\.dispose\(\);[\s\S]*?viewMinX = oldViewMinX;[\s\S]*?viewMinY = oldViewMinY;[\s\S]*?zoomFactor = oldZoomFactor;[\s\S]*?layoutRenderMode = oldLayoutRenderMode;[\s\S]*?\}[\s\S]*?\}/;
src = src.replace(rmv6, `    public BufferedImage renderMapViewImage(double renderViewMinX, double renderViewMinY, double renderZoomFactor, int renderWidth, int renderHeight, boolean includeDecorations) {
        return renderer.renderMapViewImage(renderViewMinX, renderViewMinY, renderZoomFactor, renderWidth, renderHeight, includeDecorations);
    }`);

// ============ 7. Remove draw methods and helper methods moved to MapRenderer ============
// These are the methods that were fully moved to MapRenderer
const drawMethodsToRemove = [
  // Draw methods
  'drawPointClusters', 'drawHeatmapOverlay', 'drawSelectionBox', 'drawSelectionFlash',
  'drawRasterLayer', 'drawOnlineTileLayer', 'drawOnlineWmsLayer', 'drawOnlineAttribution',
  'drawPins', 'drawCurrentSketch', 'drawPendingDrawingSessionGeometries', 'drawPendingDrawingGeometry',
  'drawContinuationEndpointHints', 'drawContinuationEndpointHint',
  'drawCurrentMeasurement', 'drawFeatureEditSketch', 'drawTopographicProfileCapture',
  'drawAdjacentPolygonPreview', 'drawAdjacentBaseSegment', 'drawCadOperationPreview',
  'drawSelectedLineEndpointHints', 'drawCadReferenceSegment', 'drawSnapPreview',
  'drawLayer', 'drawGeometry', 'drawGeometryForEditingLayer', 'drawLabelForFeature',
  'drawStyledPoint', 'drawStyledLineString', 'drawStyledPolygon',
  'drawPoint', 'drawLineString', 'drawPolygon',
  'drawSelectedFeature', 'drawAttributeTableSelections', 'drawEditableVertices',
  'drawAllLabels', 'drawResolvedLabel', 'drawLabels',
  'drawTextWithHalo', 'drawLabelWithSettings',
  'drawCursorBadge', 'drawSelectionBadge', 'drawCursorPointer',
  'writeWorldFile', 'getBaseName',
  'drawTemporaryGeometry', 'drawOnlineResolutionNotice',
  // Also remove the static writeWorldFile overload
  'writeWorldFile'
];

// For each method, find the method declaration and its body, and remove it
let lines = src.split('\n');
let removeRanges = [];

for (let i = 0; i < lines.length; i++) {
  const trimmed = lines[i].trim();
  // Match method declarations
  for (const method of drawMethodsToRemove) {
    const re = new RegExp('^(?:public|protected|private|static)\\s+(?:(?:static|final|synchronized)\\s+)?(?:\\w+(?:<[^>]*>)?(?:\\[\\])?\\s+)+' + method.replace('(', '\\(') + '\\s*\\(', '');
    if (re.test(trimmed)) {
      // Skip if it's a record or something else
      // Find the braces
      for (let j = i; j < lines.length; j++) {
        // Check if this is a static method (writeWorldFile has two overloads)
        // We need to be careful: skip if we already removed this range
        if (removeRanges.some(r => r.start === i)) break;
        
        let braceCount = 0;
        let started = false;
        let k = j;
        for (; k < lines.length; k++) {
          for (const ch of lines[k]) {
            if (ch === '{') braceCount++;
            if (ch === '}') braceCount--;
          }
          if (!started && lines[k].includes('{')) started = true;
          if (started && braceCount === 0) break;
        }
        if (started) {
          removeRanges.push({ method, start: i, end: k, line: i + 1 });
          i = k;
          break;
        } else {
          break;
        }
      }
      break;
    }
  }
}

// Sort by start descending
removeRanges.sort((a, b) => b.start - a.start);

// Deduplicate
removeRanges = removeRanges.filter((r, idx) => idx === 0 || r.start !== removeRanges[idx - 1].start);

console.log(`Removing ${removeRanges.length} methods:`);
for (const r of removeRanges) {
  console.log(`  ${r.method} (lines ${r.start + 1}-${r.end + 1})`);
  lines.splice(r.start, r.end - r.start + 1);
}

// ============ 8. Add writeWorldFile(File) delegate ============
// Find the renderMapViewImage methods and add after them
for (let i = 0; i < lines.length; i++) {
  if (lines[i].trim().startsWith('public BufferedImage renderMapViewImage(double renderViewMinX, double renderViewMinY, double renderZoomFactor, int renderWidth, int renderHeight, boolean includeDecorations)')) {
    // Find the closing brace
    let braceCount = 0;
    let started = false;
    let j = i;
    for (; j < lines.length; j++) {
      for (const ch of lines[j]) {
        if (ch === '{') braceCount++;
        if (ch === '}') braceCount--;
      }
      if (!started && lines[j].includes('{')) started = true;
      if (started && braceCount === 0) break;
    }
    lines.splice(j + 1, 0, '',
      '    public void writeWorldFile(File imageFile) throws Exception {',
      '        renderer.writeWorldFile(imageFile);',
      '    }');
    break;
  }
}

// ============ 9. Fix cursor method calls ============
src = lines.join('\n');
src = src.replace(/drawCursorPointer\(g2\);/g, 'renderer.drawCursorPointer(g2);');
src = src.replace(/drawCursorBadge\(g2,/g, 'renderer.drawCursorBadge(g2,');
src = src.replace(/drawSelectionBadge\(g2,/g, 'renderer.drawSelectionBadge(g2,');

// ============ 10. Write ============
fs.writeFileSync(path, src, 'utf8');
console.log('Done! MapPanel.java updated.');
