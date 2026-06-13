package ar.com.catgis;

import ar.com.catgis.layout.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class LayoutPreviewPanel extends JPanel {
    private final MapLayoutComposerDialog dialog;
    LayoutRenderResult lastRenderResult;
    Rectangle lastPageBounds = new Rectangle();
    double lastPreviewScale = 1d;
    private Point lastDragPagePoint = null;
    private ResizeHandle activeResizeHandle = ResizeHandle.NONE;
    private LayoutElementType activeResizeElement = null;
    private String activeResizeCustomItemId = null;
    private final List<Integer> activeGuideXs = new ArrayList<>();
    private final List<Integer> activeGuideYs = new ArrayList<>();
    private final List<GuideLine> guides = new ArrayList<>();
    private GuideLine draggingGuide = null;
    private double draggingGuideStartMm = 0;
    LayoutElement hoveredElement = null;
    String drawingShape = null;        // "rect", "ellipse", "line" when drawing mode active
    private Point drawingStart = null;          // page-pixel start of the shape
    boolean snapToGrid = true;                   // controlled by UI toggle
    boolean snapToElements = true;               // controlled by UI toggle
    private final JTextField inlineTitleEditor;
    private final JPanel inlineCartoucheEditor;
    private final JTextField inlineCartoucheStudyField;
    private final JTextField inlineCartoucheProjectField;
    private final JTextField inlineCartoucheCompanyField;
    private final JTextField inlineCartoucheCartographerField;
    private final JTextField inlineCartoucheSourceField;
    private final JTextField inlineCartoucheCrsField;

    public LayoutPreviewPanel(MapLayoutComposerDialog dialog) {
        this.dialog = dialog;
        setLayout(null);
        setOpaque(true);
        setFocusable(true);
        setBackground(new Color(155, 160, 170)); // ArcMap-style dark canvas
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        inlineTitleEditor = new JTextField();
        inlineTitleEditor.setVisible(false);
        inlineTitleEditor.addActionListener(e -> commitInlineTitleEdit());
        inlineTitleEditor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                commitInlineTitleEdit();
            }
        });
        add(inlineTitleEditor);
        inlineCartoucheStudyField = new JTextField();
        inlineCartoucheProjectField = new JTextField();
        inlineCartoucheCompanyField = new JTextField();
        inlineCartoucheCartographerField = new JTextField();
        inlineCartoucheSourceField = new JTextField();
        inlineCartoucheCrsField = new JTextField();
        inlineCartoucheEditor = buildInlineCartoucheEditor();
        inlineCartoucheEditor.setVisible(false);
        add(inlineCartoucheEditor);
        installInteraction();
    }

    @Override
    public Dimension getPreferredSize() {
        LayoutSettings settings = dialog.buildSettings();
        if (settings == null) {
            return new Dimension(980, 760);
        }
        Dimension pageSize = settings.pageSize().pixelSize(settings.orientation(), MapLayoutComposerDialog.PREVIEW_RENDER_DPI);
        Dimension viewportSize = resolvePreviewViewportSize();
        int availableWidth = Math.max(80, viewportSize.width - 40);
        int availableHeight = Math.max(80, viewportSize.height - 40);
        double fitPageScale = Math.min(availableWidth / (double) pageSize.width, availableHeight / (double) pageSize.height);
        double fitWidthScale = availableWidth / (double) pageSize.width;
        double scale = Math.max(0.08d, dialog.interactionState.resolvePreviewScale(fitPageScale, fitWidthScale));
        int drawWidth = (int) Math.round(pageSize.width * scale);
        int drawHeight = (int) Math.round(pageSize.height * scale);
        return new Dimension(
                Math.max(viewportSize.width, drawWidth + 80),
                Math.max(viewportSize.height, drawHeight + 80)
        );
    }

    private Dimension resolvePreviewViewportSize() {
        if (dialog.previewScrollPane != null) {
            Dimension extent = dialog.previewScrollPane.getViewport().getExtentSize();
            if (extent != null && extent.width > 0 && extent.height > 0) {
                return extent;
            }
        }
        if (getParent() instanceof javax.swing.JViewport viewport) {
            Dimension extent = viewport.getExtentSize();
            if (extent != null && extent.width > 0 && extent.height > 0) {
                return extent;
            }
        }
        if (getWidth() > 0 && getHeight() > 0) {
            return new Dimension(getWidth(), getHeight());
        }
        return new Dimension(980, 760);
    }

    private JPanel buildInlineCartoucheEditor() {
        JPanel editor = new JPanel(new GridBagLayout());
        editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(37, 99, 235), 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        editor.setBackground(new Color(248, 250, 252));
        editor.setOpaque(true);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 4, 2, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        addInlineCartoucheField(editor, gc, 0, "Estudio", inlineCartoucheStudyField);
        addInlineCartoucheField(editor, gc, 1, "Proyecto", inlineCartoucheProjectField);
        addInlineCartoucheField(editor, gc, 2, "Empresa", inlineCartoucheCompanyField);
        addInlineCartoucheField(editor, gc, 3, "Cartografo", inlineCartoucheCartographerField);
        addInlineCartoucheField(editor, gc, 4, "Fuente", inlineCartoucheSourceField);
        addInlineCartoucheField(editor, gc, 5, "Coord.", inlineCartoucheCrsField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);
        JButton apply = new JButton("Aplicar");
        apply.addActionListener(e -> commitInlineCartoucheEdit());
        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(e -> cancelInlineCartoucheEdit());
        buttons.add(apply);
        buttons.add(cancel);

        gc.gridx = 0;
        gc.gridy = 6;
        gc.gridwidth = 2;
        gc.weightx = 1;
        editor.add(buttons, gc);
        return editor;
    }

    private void addInlineCartoucheField(JPanel editor, GridBagConstraints gc, int row, String label, JTextField field) {
        JLabel fieldLabel = new JLabel(label + ":");
        fieldLabel.setFont(fieldLabel.getFont().deriveFont(Font.BOLD, 11f));
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 1;
        gc.weightx = 0;
        editor.add(fieldLabel, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        field.setColumns(22);
        editor.add(field, gc);
    }

    private void installInteraction() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    // Check if double-click on a LayoutElement -> open contextual editor
                    Point pp = toPagePoint(e.getPoint());
                    RectMm pr = dialog.toPageRectMm();
                    if (pp != null && pr != null && dialog.layoutModel.size() > 0) {
                        double xMm = pr.xMm + pp.x * pr.pxToMmScale;
                        double yMm = pr.yMm + pp.y * pr.pxToMmScale;
                        LayoutElement el = dialog.layoutModel.findTopmostElementAtMm(xMm, yMm);
                        if (el instanceof LayoutMap) {
                            dialog.layoutModel.clearSelection();
                            el.setSelected(true);
                            dialog.mapPanToolButton.doClick();
                            dialog.statusLabel.setText("Editando contenido del mapa: \"" + el.getName() + "\". Esc para salir.");
                            return;
                        }
                        if (el instanceof LayoutLabel) {
                            dialog.layoutModel.clearSelection();
                            el.setSelected(true);
                            dialog.refreshElementList();
                            showTextPopup((LayoutLabel) el);
                            return;
                        }
                        if (el instanceof LayoutLegend) {
                            dialog.layoutModel.clearSelection();
                            el.setSelected(true);
                            dialog.refreshElementList();
                            showLegendPopup((LayoutLegend) el);
                            return;
                        }
                        if (el instanceof LayoutCartouche) {
                            dialog.layoutModel.clearSelection();
                            el.setSelected(true);
                            dialog.refreshElementList();
                            dialog.showCartouchePopup((LayoutCartouche) el);
                            return;
                        }
                    }
                    Point pagePoint = toPagePoint(e.getPoint());
                    if (pagePoint != null && isInsideElement(LayoutElementType.HEADER, pagePoint)) {
                        beginInlineTitleEdit();
                    } else if (pagePoint != null && isInsideElement(LayoutElementType.CARTOUCHE, pagePoint)) {
                        beginInlineCartoucheEdit();
                    } else if (pagePoint != null && isInsideElement(LayoutElementType.LEGEND, pagePoint)) {
                        dialog.openLegendEditor();
                    } else if (pagePoint != null && isInsideElement(LayoutElementType.NORTH, pagePoint)) {
                        dialog.configureNorthFromToolbar();
                    } else if (pagePoint != null && findCustomItemIdAt(pagePoint) != null) {
                        dialog.selectCatmapItemInList(findCustomItemIdAt(pagePoint));
                        dialog.editSelectedCatmapItem();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (SwingUtilities.isRightMouseButton(e)) { handleRightClick(e); return; }
                if (!SwingUtilities.isLeftMouseButton(e)) return;

                // Drawing mode: start drawing
                if (drawingShape != null) {
                    drawingStart = toPagePoint(e.getPoint());
                    return;
                }

                // Guide interaction: drag from ruler or existing guide
                GuideLine.Orientation rulerHit = RulerRenderer.rulerHitTest(e.getX(), e.getY(), 0, 0, getWidth(), getHeight());
                if (rulerHit != null) {
                    LayoutSettings settings = dialog.buildSettings();
                    double pxPerMm = MapLayoutComposerDialog.PREVIEW_RENDER_DPI / 25.4 * lastPreviewScale;
                    double mm = rulerHit == GuideLine.Orientation.VERTICAL
                            ? (e.getX() - lastPageBounds.x) / pxPerMm
                            : (e.getY() - lastPageBounds.y) / pxPerMm;
                    if (mm >= 0) {
                        GuideLine newGuide = new GuideLine("guide-" + System.currentTimeMillis(), mm, rulerHit);
                        guides.add(newGuide);
                        draggingGuide = newGuide;
                        draggingGuideStartMm = mm;
                        repaint();
                        return;
                    }
                }
                // Check for existing guide hit
                {
                    double pxPerMm = MapLayoutComposerDialog.PREVIEW_RENDER_DPI / 25.4 * lastPreviewScale;
                    for (GuideLine guide : guides) {
                        if (guide.containsPx(e.getX(), e.getY(), lastPageBounds.x, lastPageBounds.y, lastPageBounds.width, lastPageBounds.height, MapLayoutComposerDialog.PREVIEW_RENDER_DPI, lastPreviewScale, 6)) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                guides.remove(guide);
                                repaint();
                                return;
                            }
                            draggingGuide = guide;
                            draggingGuideStartMm = guide.mmPos;
                            return;
                        }
                    }
                }

                Point pagePoint = toPagePoint(e.getPoint());
                RectMm pageRect = dialog.toPageRectMm();
                if (pagePoint != null && pageRect != null) {
                double xMm = pageRect.xMm + pagePoint.x * pageRect.pxToMmScale;
                double yMm = pageRect.yMm + pagePoint.y * pageRect.pxToMmScale;
                    LayoutElement clicked = dialog.layoutModel.findElementAtMm(xMm, yMm);
                    if (clicked != null) {
                        // Only allow resize if element is already selected; otherwise select+move first
                        if (clicked.isSelected() && !clicked.isLocked()) {
                            int handleIdx = dialog.hitTestHandle(clicked, pagePoint, pageRect);
                            if (handleIdx >= 0) {
                                dialog.pushUndo(clicked, false);
                                dialog.activeResizeHandleIndex = handleIdx;
                                dialog.draggingLayoutElement = clicked;
                                dialog.dragStartPagePoint = pagePoint;
                                dialog.dragStartBoundsMm = new java.awt.geom.Rectangle2D.Double(
                                    clicked.getBoundsMm().x, clicked.getBoundsMm().y,
                                    clicked.getBoundsMm().width, clicked.getBoundsMm().height);
                                return;
                            }
                        }
                        if (!clicked.isLocked()) {
                            dialog.pushUndo(clicked, false);
                            dialog.layoutModel.clearSelection();
                            clicked.setSelected(true);
                            dialog.draggingLayoutElement = clicked;
                            dialog.dragStartPagePoint = pagePoint;
                            dialog.dragStartBoundsMm = new java.awt.geom.Rectangle2D.Double(
                                clicked.getBoundsMm().x, clicked.getBoundsMm().y,
                                clicked.getBoundsMm().width, clicked.getBoundsMm().height);
                            dialog.activeResizeHandleIndex = -1;
                            dialog.refreshElementList();
                            repaint();
                            return;
                        }
                        dialog.layoutModel.clearSelection();
                        clicked.setSelected(true);
                        dialog.refreshElementList();
                        repaint();
                        return;
                    }
                    dialog.layoutModel.clearSelection();
                }

                if (dialog.interactionState.isMapFramePanToolActive() || dialog.interactionState.isMapFrameZoomToolActive()) {
                    activeResizeHandle = ResizeHandle.NONE;
                    activeResizeElement = null;
                    activeResizeCustomItemId = null;
                    if (pagePoint == null || !isInsideElement(LayoutElementType.MAP_CONTENT, pagePoint)) {
                        lastDragPagePoint = null;
                        setCursor(Cursor.getDefaultCursor());
                        dialog.statusLabel.setText(dialog.interactionState.isMapFramePanToolActive()
                                ? "Pan mapa activo. Haz clic dentro del frame para desplazar solo el contenido interno."
                                : "Lupa mapa activa. Usa la rueda dentro del frame para cambiar el zoom interno.");
                        repaint();
                        return;
                    }
                    dialog.interactionState.select(LayoutElementType.MAP_CONTENT);
                    dialog.selectCatmapItemInList(null);
                    dialog.syncLayoutStructureSelection();
                    lastDragPagePoint = dialog.interactionState.isMapFramePanToolActive() ? pagePoint : null;
                    setCursor(dialog.interactionState.isMapFramePanToolActive()
                            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    dialog.statusLabel.setText(dialog.interactionState.isMapFramePanToolActive()
                            ? "Pan mapa activo. Arrastra para mover solo el contenido interno."
                            : "Lupa mapa activa. Usa la rueda para acercar o alejar el contenido interno.");
                    repaint();
                    return;
                }
                if (pagePoint == null || lastRenderResult == null) {
                    dialog.interactionState.select(null);
                    activeResizeHandle = ResizeHandle.NONE;
                    activeResizeElement = null;
                    activeResizeCustomItemId = null;
                    dialog.selectCatmapItemInList(null);
                    repaint();
                    return;
                }
                ResizeTarget resizeTarget = findResizeTarget(pagePoint);
                if (resizeTarget != null) {
                    if (resizeTarget.customItemId() != null) {
                        dialog.interactionState.selectCustomItem(resizeTarget.customItemId());
                        dialog.selectCatmapItemInList(resizeTarget.customItemId());
                        CatmapLayoutItem item = dialog.getCatmapItemById(resizeTarget.customItemId());
                        if (item != null && item.isLocked()) {
                            lastDragPagePoint = null;
                            activeResizeHandle = ResizeHandle.NONE;
                            activeResizeElement = null;
                            activeResizeCustomItemId = null;
                            dialog.statusLabel.setText("Elemento CATMAP bloqueado. Liberalo desde el inspector para redimensionarlo.");
                            repaint();
                            return;
                        }
                    } else {
                        dialog.interactionState.select(resizeTarget.elementType());
                        dialog.selectCatmapItemInList(null);
                        if (dialog.interactionState.isElementLocked(resizeTarget.elementType())) {
                            lastDragPagePoint = null;
                            activeResizeHandle = ResizeHandle.NONE;
                            activeResizeElement = null;
                            activeResizeCustomItemId = null;
                            dialog.statusLabel.setText(dialog.layoutElementLabel(resizeTarget.elementType()) + " bloqueado. Liberalo desde la estructura para redimensionarlo.");
                            repaint();
                            return;
                        }
                    }
                    activeResizeElement = resizeTarget.elementType();
                    activeResizeCustomItemId = resizeTarget.customItemId();
                    activeResizeHandle = resizeTarget.handle();
                    lastDragPagePoint = pagePoint;
                    setCursor(cursorForHandle(activeResizeHandle));
                    dialog.statusLabel.setText("Redimensionando " + elementLabel(activeResizeElement) + " con el mouse.");
                    repaint();
                    return;
                }
                LayoutElementType hit = findElementAt(pagePoint);
                String customItemId = findCustomItemIdAt(pagePoint);
                if (customItemId != null) {
                    dialog.interactionState.selectCustomItem(customItemId);
                    dialog.selectCatmapItemInList(customItemId);
                    CatmapLayoutItem item = dialog.getCatmapItemById(customItemId);
                    if (item != null && item.isLocked()) {
                        activeResizeHandle = ResizeHandle.NONE;
                        activeResizeElement = null;
                        activeResizeCustomItemId = null;
                        lastDragPagePoint = null;
                        setCursor(Cursor.getDefaultCursor());
                        dialog.statusLabel.setText("Elemento CATMAP bloqueado. PodÃ©s editarlo desde el inspector, pero no moverlo.");
                        repaint();
                        return;
                    }
                } else {
                    dialog.interactionState.select(hit);
                    dialog.selectCatmapItemInList(null);
                    if (dialog.interactionState.isElementLocked(hit)) {
                        activeResizeHandle = ResizeHandle.NONE;
                        activeResizeElement = null;
                        activeResizeCustomItemId = null;
                        lastDragPagePoint = null;
                        setCursor(Cursor.getDefaultCursor());
                        dialog.statusLabel.setText(dialog.layoutElementLabel(hit) + " bloqueado. PodÃ©s seleccionarlo, pero no moverlo.");
                        repaint();
                        return;
                    }
                }
                activeResizeHandle = ResizeHandle.NONE;
                activeResizeElement = null;
                activeResizeCustomItemId = null;
                lastDragPagePoint = hit != null ? pagePoint : null;
                boolean selected = hit != null || customItemId != null;
                lastDragPagePoint = selected ? pagePoint : null;
                setCursor(resolveWorkCursor(pagePoint, selected));
                dialog.statusLabel.setText(resolveSelectionStatus(hit, customItemId, selected));
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (drawingShape != null) {
                    if (drawingStart != null) {
                        Point end = toPagePoint(e.getPoint());
                        if (end != null) finishDrawing(end);
                        else cancelDrawing();
                    } else {
                        cancelDrawing();
                    }
                    return;
                }
                if (draggingGuide != null) {
                    draggingGuide = null;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                    return;
                }
                if (e.isPopupTrigger()) {
                    handleRightClick(e);
                    return;
                }
                if (dialog.draggingLayoutElement != null) {
                    dialog.draggingLayoutElement = null;
                    dialog.dragStartPagePoint = null;
                    dialog.dragStartBoundsMm = null;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }
                lastDragPagePoint = null;
                activeResizeHandle = ResizeHandle.NONE;
                activeResizeElement = null;
                activeResizeCustomItemId = null;
                clearSnapGuides();
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawingShape != null && drawingStart != null) {
                    repaint(); // will draw preview rectangle in paintComponent
                    return;
                }
                if (draggingGuide != null) {
                    LayoutSettings settings = dialog.buildSettings();
                    double mmPerPx = 25.4 / MapLayoutComposerDialog.PREVIEW_RENDER_DPI / lastPreviewScale;
                    if (draggingGuide.orientation == GuideLine.Orientation.VERTICAL) {
                        double delta = (e.getX() - lastPageBounds.x) - (e.getX() - lastPageBounds.x);
                        double newMm = (e.getX() - lastPageBounds.x) * mmPerPx;
                        newMm = Math.max(0, Math.min(newMm, settings.pageSize().widthMm));
                        draggingGuide.mmPos = newMm;
                    } else {
                        double newMm = (e.getY() - lastPageBounds.y) * mmPerPx;
                        newMm = Math.max(0, Math.min(newMm, settings.pageSize().heightMm));
                        draggingGuide.mmPos = newMm;
                    }
                    repaint();
                    return;
                }
                if (dialog.draggingLayoutElement != null && dialog.dragStartPagePoint != null && dialog.dragStartBoundsMm != null) {
                    Point p = toPagePoint(e.getPoint());
                    if (p != null) {
                        RectMm r = dialog.toPageRectMm();
                        if (r != null) {
                            double sc = r.pxToMmScale;
                            if (dialog.activeResizeHandleIndex >= 0) {
                                dialog.resizeElement(dialog.activeResizeHandleIndex, p.x - dialog.dragStartPagePoint.x, p.y - dialog.dragStartPagePoint.y);
            } else {
                double newX = dialog.dragStartBoundsMm.x + (p.x - dialog.dragStartPagePoint.x) * sc;
                double newY = dialog.dragStartBoundsMm.y + (p.y - dialog.dragStartPagePoint.y) * sc;
                // Smart snap to other element edges (conditional)
                if (snapToElements) {
                double snapTol = 2.0;
                for (LayoutElement other : dialog.layoutModel.getElements()) {
                    if (other == dialog.draggingLayoutElement || !other.isVisible()) continue;
                    double ox = other.getBoundsMm().x, oy = other.getBoundsMm().y;
                    double ow = other.getBoundsMm().width, oh = other.getBoundsMm().height;
                    double ex = newX, ey = newY, ew = dialog.dragStartBoundsMm.width, eh = dialog.dragStartBoundsMm.height;
                    if (Math.abs(ex - ox) < snapTol) newX = ox;
                    if (Math.abs(ex + ew - ox - ow) < snapTol) newX = ox + ow - ew;
                    if (Math.abs(ex + ew/2 - ox - ow/2) < snapTol) newX = ox + ow/2 - ew/2;
                    if (Math.abs(ey - oy) < snapTol) newY = oy;
                    if (Math.abs(ey + eh - oy - oh) < snapTol) newY = oy + oh - eh;
                    if (Math.abs(ey + eh/2 - oy - oh/2) < snapTol) newY = oy + oh/2 - eh/2;
                }
                }
                // Snap to grid (5mm, conditional)
                if (snapToGrid) {
                double gridSize = 5.0;
                newX = Math.round(newX / gridSize) * gridSize;
                newY = Math.round(newY / gridSize) * gridSize;
                }
                dialog.draggingLayoutElement.setBoundsMm(newX, newY, dialog.dragStartBoundsMm.width, dialog.dragStartBoundsMm.height);
                            }
                        }
                    }
                    repaint();
                    return;
                }
                if (lastDragPagePoint == null || dialog.interactionState.getSelectedElement() == null) {
                    return;
                }
                if (dialog.interactionState.isMapFrameZoomToolActive()) {
                    return;
                }
                Point pagePoint = toPagePoint(e.getPoint());
                if (pagePoint == null) {
                    return;
                }
                int dx = pagePoint.x - lastDragPagePoint.x;
                int dy = pagePoint.y - lastDragPagePoint.y;
                if (dx == 0 && dy == 0) {
                    return;
                }
                if (activeResizeElement != null && activeResizeHandle != ResizeHandle.NONE) {
                    Rectangle currentBounds = activeResizeElement == LayoutElementType.CATMAP_ITEM
                            ? (lastRenderResult != null ? lastRenderResult.customItemBounds().get(activeResizeCustomItemId) : null)
                            : (lastRenderResult != null ? lastRenderResult.elementBounds().get(activeResizeElement) : null);
                    if (currentBounds != null) {
                        if (activeResizeElement == LayoutElementType.CATMAP_ITEM && activeResizeCustomItemId != null) {
                            resizeCatmapItem(activeResizeCustomItemId, activeResizeHandle, dx, dy, currentBounds);
                        } else {
                            dialog.interactionState.resize(
                                    activeResizeElement,
                                    activeResizeHandle,
                                    dx,
                                    dy,
                                    currentBounds.width,
                                    currentBounds.height,
                                    activeResizeElement == LayoutElementType.MAP_CONTENT ? 260 : 160,
                                    activeResizeElement == LayoutElementType.MAP_CONTENT ? 180 : 100
                            );
                        }
                    }
                } else if (dialog.interactionState.getSelectedElement() == LayoutElementType.MAP_CONTENT && dialog.interactionState.isMapFrameMoveToolActive()) {
                    dialog.interactionState.translate(LayoutElementType.MAP_CONTENT, dx, dy);
                } else if (dialog.interactionState.getSelectedElement() == LayoutElementType.MAP_CONTENT && dialog.interactionState.isMapFramePanToolActive()) {
                    dialog.interactionState.panMap(dx, dy);
                } else if (dialog.interactionState.getSelectedElement() == LayoutElementType.CATMAP_ITEM) {
                    translateCatmapItem(dialog.interactionState.getSelectedCustomItemId(), dx, dy);
                } else if (!dialog.interactionState.isElementLocked(dialog.interactionState.getSelectedElement())) {
                    dialog.interactionState.translate(dialog.interactionState.getSelectedElement(), dx, dy);
                }
                lastDragPagePoint = pagePoint;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point pagePoint = toPagePoint(e.getPoint());
                RectMm pageRect = dialog.toPageRectMm();
                LayoutElement oldHover = hoveredElement;
                hoveredElement = null;
                if (pagePoint != null && pageRect != null && dialog.layoutModel.size() > 0) {
                    double xMm = pageRect.xMm + pagePoint.x * pageRect.pxToMmScale;
                    double yMm = pageRect.yMm + pagePoint.y * pageRect.pxToMmScale;
                    hoveredElement = dialog.layoutModel.findHoverAtMm(xMm, yMm);
                    if (hoveredElement != null) {
                        if (hoveredElement.isLocked()) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        } else {
                            boolean overMapContent = hoveredElement instanceof ar.com.catgis.layout.LayoutMap;
                            if (overMapContent && dialog.interactionState.isMapFrameZoomToolActive()) {
                                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                            } else if (overMapContent && dialog.interactionState.isMapFramePanToolActive()) {
                                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            } else {
                                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            }
                        }
                        setToolTipText(hoveredElement.getName());
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                        setToolTipText(null);
                    }
                } else {
                    setCursor(Cursor.getDefaultCursor());
                    setToolTipText(null);
                }
                if (oldHover != hoveredElement) repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (lastRenderResult == null) {
                    return;
                }
                Point pagePoint = toPagePoint(e.getPoint());
                boolean overMap = pagePoint != null && isInsideElement(LayoutElementType.MAP_CONTENT, pagePoint);
                double factor = e.getWheelRotation() < 0 ? 1.12d : (1d / 1.12d);
                if (overMap && dialog.interactionState.isMapFrameZoomToolActive()) {
                    dialog.interactionState.zoomMap(factor);
                    dialog.statusLabel.setText("Zoom del mapa dentro del layout: " + Math.round(dialog.interactionState.getMapZoom() * 100d) + "%");
                } else {
                    dialog.interactionState.zoomPreview(factor);
                    dialog.statusLabel.setText("Zoom del compositor actualizado para trabajar la maquetacion.");
                }
                revalidate();
                repaint();
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    private void selectElementForPopup(Point panelPoint) {
        Point pagePoint = toPagePoint(panelPoint);
        if (pagePoint == null || lastRenderResult == null) {
            return;
        }
        String customItemId = findCustomItemIdAt(pagePoint);
        if (customItemId != null) {
            dialog.interactionState.selectCustomItem(customItemId);
            dialog.selectCatmapItemInList(customItemId);
            dialog.syncLayoutStructureSelection();
            repaint();
            return;
        }
        LayoutElementType hit = findElementAt(pagePoint);
        if (hit != null) {
            dialog.interactionState.select(hit);
            dialog.selectCatmapItemInList(null);
            dialog.syncLayoutStructureSelection();
            repaint();
        }
    }

    private void beginInlineTitleEdit() {
        if (lastRenderResult == null) {
            return;
        }
        Rectangle headerBounds = lastRenderResult.elementBounds().get(LayoutElementType.HEADER);
        if (headerBounds == null) {
            return;
        }
        int editorX = lastPageBounds.x + (int) Math.round((headerBounds.x + 2) * lastPreviewScale);
        int editorY = lastPageBounds.y + (int) Math.round((headerBounds.y + 6) * lastPreviewScale);
        int editorWidth = Math.max(180, (int) Math.round(Math.min(headerBounds.width * 0.72d, 460) * lastPreviewScale));
        int editorHeight = Math.max(28, (int) Math.round(34 * lastPreviewScale));
        inlineTitleEditor.setText(dialog.titleField.getText());
        inlineTitleEditor.setBounds(editorX, editorY, editorWidth, editorHeight);
        inlineTitleEditor.setVisible(true);
        inlineTitleEditor.requestFocusInWindow();
        inlineTitleEditor.selectAll();
    }

    private void commitInlineTitleEdit() {
        if (!inlineTitleEditor.isVisible()) {
            return;
        }
        String updated = inlineTitleEditor.getText() != null ? inlineTitleEditor.getText().trim() : "";
        if (!updated.isBlank()) {
            dialog.titleField.setText(updated);
        }
        inlineTitleEditor.setVisible(false);
        repaint();
    }

    private void beginInlineCartoucheEdit() {
        if (lastRenderResult == null) {
            return;
        }
        Rectangle cartoucheBounds = lastRenderResult.elementBounds().get(LayoutElementType.CARTOUCHE);
        if (cartoucheBounds == null) {
            return;
        }
        inlineCartoucheStudyField.setText(dialog.studyField.getText());
        inlineCartoucheProjectField.setText(dialog.cartoucheProjectField.getText());
        inlineCartoucheCompanyField.setText(dialog.companyField.getText());
        inlineCartoucheCartographerField.setText(dialog.cartographerField.getText());
        inlineCartoucheSourceField.setText(dialog.imageSourceField.getText());
        inlineCartoucheCrsField.setText(dialog.coordinateReferenceField.getText());
        placeInlineCartoucheEditor(cartoucheBounds);
        inlineCartoucheEditor.setVisible(true);
        inlineCartoucheEditor.requestFocusInWindow();
        inlineCartoucheStudyField.requestFocusInWindow();
        inlineCartoucheStudyField.selectAll();
        dialog.statusLabel.setText("Editando cartucho directamente sobre el layout. Aplicar confirma los cambios.");
    }

    private void placeInlineCartoucheEditor(Rectangle cartoucheBounds) {
        int editorX = lastPageBounds.x + (int) Math.round((cartoucheBounds.x + 4) * lastPreviewScale);
        int editorY = lastPageBounds.y + (int) Math.round((cartoucheBounds.y + 4) * lastPreviewScale);
        int editorWidth = Math.max(300, (int) Math.round(Math.max(cartoucheBounds.width - 8, 420) * lastPreviewScale));
        int editorHeight = Math.max(190, inlineCartoucheEditor.getPreferredSize().height);
        editorWidth = Math.min(editorWidth, Math.max(320, getWidth() - editorX - 24));
        editorHeight = Math.min(editorHeight, Math.max(170, getHeight() - editorY - 24));
        inlineCartoucheEditor.setBounds(editorX, editorY, editorWidth, editorHeight);
    }

    private void commitInlineCartoucheEdit() {
        if (!inlineCartoucheEditor.isVisible()) {
            return;
        }
        dialog.studyField.setText(dialog.safeTrim(inlineCartoucheStudyField.getText()));
        dialog.cartoucheProjectField.setText(dialog.safeTrim(inlineCartoucheProjectField.getText()));
        dialog.companyField.setText(dialog.safeTrim(inlineCartoucheCompanyField.getText()));
        dialog.cartographerField.setText(dialog.safeTrim(inlineCartoucheCartographerField.getText()));
        dialog.imageSourceField.setText(dialog.safeTrim(inlineCartoucheSourceField.getText()));
        dialog.coordinateReferenceField.setText(dialog.safeTrim(inlineCartoucheCrsField.getText()));
        inlineCartoucheEditor.setVisible(false);
        dialog.statusLabel.setText("Datos cartograficos actualizados desde el layout.");
        repaint();
    }

    private void cancelInlineCartoucheEdit() {
        inlineCartoucheEditor.setVisible(false);
        dialog.statusLabel.setText("Edicion directa del cartucho cancelada.");
        repaint();
    }

    private String elementLabel(LayoutElementType type) {
        return switch (type) {
            case HEADER -> "encabezado";
            case MAP_CONTENT -> "mapa";
            case LEGEND -> "leyenda";
            case NORTH -> "norte";
            case SCALE -> "escala";
            case CARTOUCHE -> "cartucho";
            case PROFILE_IMAGE -> I18n.t("imagen del perfil");
            case CATMAP_ITEM -> "elemento CATMAP";
        };
    }

    private Cursor resolveWorkCursor(Point pagePoint, boolean selected) {
        if (!selected) {
            return Cursor.getDefaultCursor();
        }
        boolean overMap = pagePoint != null && isInsideElement(LayoutElementType.MAP_CONTENT, pagePoint);
        if (overMap && dialog.interactionState.getSelectedElement() == LayoutElementType.MAP_CONTENT) {
            if (dialog.interactionState.isMapFrameZoomToolActive()) {
                return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
            }
            if (dialog.interactionState.isMapFramePanToolActive()) {
                return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            }
            if (dialog.interactionState.isMapFrameMoveToolActive()) {
                return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
            }
        }
        return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }

    private String resolveSelectionStatus(LayoutElementType hit, String customItemId, boolean selected) {
        if (!selected) {
            return "Haz clic sobre un elemento del layout para moverlo.";
        }
        if (customItemId != null) {
            return "Elemento CATMAP seleccionado. Arrastralo para reubicarlo o usa el inspector para editarlo.";
        }
        if (hit == LayoutElementType.MAP_CONTENT) {
            if (dialog.interactionState.isMapFrameMoveToolActive()) {
                return "Mover layout activo. Arrastra el bloque completo del mapa sin cambiar el contenido interno.";
            }
            if (dialog.interactionState.isMapFramePanToolActive()) {
                return "Pan mapa activo. Arrastra para mover solo el contenido interno.";
            }
            if (dialog.interactionState.isMapFrameZoomToolActive()) {
                return "Lupa de mapa activa. Usa la rueda para cambiar el zoom interno sin mover el bloque.";
            }
        }
        return "Elemento seleccionado: " + elementLabel(hit) + ". Arrastra con el mouse para reubicar.";
    }

    private LayoutElementType findElementAt(Point pagePoint) {
        if (findCustomItemIdAt(pagePoint) != null) {
            return LayoutElementType.CATMAP_ITEM;
        }
        LayoutElementType[] order = {
                LayoutElementType.PROFILE_IMAGE,
                LayoutElementType.LEGEND,
                LayoutElementType.NORTH,
                LayoutElementType.SCALE,
                LayoutElementType.CARTOUCHE,
                LayoutElementType.HEADER,
                LayoutElementType.MAP_CONTENT
        };
        for (LayoutElementType type : order) {
            if (isInsideElement(type, pagePoint)) {
                return type;
            }
        }
        return null;
    }

    private ResizeTarget findResizeTarget(Point pagePoint) {
        if (lastRenderResult != null) {
            java.util.List<String> ids = new ArrayList<>(lastRenderResult.customItemBounds().keySet());
            for (int i = ids.size() - 1; i >= 0; i--) {
                String id = ids.get(i);
                Rectangle bounds = lastRenderResult.customItemBounds().get(id);
                ResizeHandle handle = bounds != null ? resolveResizeHandle(bounds, pagePoint) : ResizeHandle.NONE;
                if (handle != ResizeHandle.NONE) {
                    return new ResizeTarget(LayoutElementType.CATMAP_ITEM, handle, id);
                }
            }
        }
        for (LayoutElementType type : new LayoutElementType[]{
                LayoutElementType.PROFILE_IMAGE,
                LayoutElementType.LEGEND,
                LayoutElementType.NORTH,
                LayoutElementType.SCALE,
                LayoutElementType.CARTOUCHE,
                LayoutElementType.HEADER,
                LayoutElementType.MAP_CONTENT
        }) {
            if (type == LayoutElementType.MAP_CONTENT && !dialog.interactionState.isMapFrameMoveToolActive()) {
                continue;
            }
            if (dialog.interactionState.isElementLocked(type)) {
                continue;
            }
            Rectangle bounds = lastRenderResult != null ? lastRenderResult.elementBounds().get(type) : null;
            ResizeHandle handle = bounds != null ? resolveResizeHandle(bounds, pagePoint) : ResizeHandle.NONE;
            if (handle != ResizeHandle.NONE) {
                return new ResizeTarget(type, handle, null);
            }
        }
        return null;
    }

    private ResizeHandle resolveResizeHandle(Rectangle bounds, Point point) {
        int tolerance = Math.max(6, (int) Math.round(8d / Math.max(0.4d, lastPreviewScale)));
        boolean left = Math.abs(point.x - bounds.x) <= tolerance;
        boolean right = Math.abs(point.x - (bounds.x + bounds.width)) <= tolerance;
        boolean top = Math.abs(point.y - bounds.y) <= tolerance;
        boolean bottom = Math.abs(point.y - (bounds.y + bounds.height)) <= tolerance;
        boolean insideY = point.y >= bounds.y - tolerance && point.y <= bounds.y + bounds.height + tolerance;
        boolean insideX = point.x >= bounds.x - tolerance && point.x <= bounds.x + bounds.width + tolerance;

        if (left && top) return ResizeHandle.NORTH_WEST;
        if (right && top) return ResizeHandle.NORTH_EAST;
        if (left && bottom) return ResizeHandle.SOUTH_WEST;
        if (right && bottom) return ResizeHandle.SOUTH_EAST;
        if (left && insideY) return ResizeHandle.WEST;
        if (right && insideY) return ResizeHandle.EAST;
        if (top && insideX) return ResizeHandle.NORTH;
        if (bottom && insideX) return ResizeHandle.SOUTH;
        return ResizeHandle.NONE;
    }

    private Cursor cursorForHandle(ResizeHandle handle) {
        return switch (handle) {
            case NORTH -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            case SOUTH -> Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
            case EAST -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            case WEST -> Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            case NORTH_EAST -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            case NORTH_WEST -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            case SOUTH_EAST -> Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
            case SOUTH_WEST -> Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            default -> Cursor.getDefaultCursor();
        };
    }

    private boolean isInsideElement(LayoutElementType type, Point pagePoint) {
        Rectangle bounds = type == LayoutElementType.CATMAP_ITEM && lastRenderResult != null
                ? lastRenderResult.customItemBounds().get(dialog.interactionState.getSelectedCustomItemId())
                : (lastRenderResult != null ? lastRenderResult.elementBounds().get(type) : null);
        return bounds != null && bounds.contains(pagePoint);
    }

    private String findCustomItemIdAt(Point pagePoint) {
        if (pagePoint == null || lastRenderResult == null) {
            return null;
        }
        java.util.List<String> ids = new ArrayList<>(lastRenderResult.customItemBounds().keySet());
        for (int i = ids.size() - 1; i >= 0; i--) {
            String id = ids.get(i);
            Rectangle bounds = lastRenderResult.customItemBounds().get(id);
            if (bounds != null && bounds.contains(pagePoint)) {
                return id;
            }
        }
        return null;
    }

    private void translateCatmapItem(String itemId, int dx, int dy) {
        CatmapLayoutItem item = dialog.getCatmapItemById(itemId);
        if (item == null || item.isLocked() || !item.isVisible()) {
            return;
        }
        Rectangle proposed = new Rectangle(item.getX() + dx, item.getY() + dy, item.getWidth(), item.getHeight());
        SnapResult snapped = snapCatmapRectangle(proposed, itemId);
        applySnapResultToItem(item, snapped);
        dialog.persistCatmapItems();
    }

    private void resizeCatmapItem(String itemId, ResizeHandle handle, int dx, int dy, Rectangle currentBounds) {
        CatmapLayoutItem item = dialog.getCatmapItemById(itemId);
        if (item == null || item.isLocked() || !item.isVisible() || handle == null || handle == ResizeHandle.NONE) {
            return;
        }
        int x = item.getX();
        int y = item.getY();
        int width = Math.max(20, currentBounds.width);
        int height = Math.max(20, currentBounds.height);

        switch (handle) {
            case EAST -> width = Math.max(40, width + dx);
            case SOUTH -> height = Math.max(30, height + dy);
            case SOUTH_EAST -> {
                width = Math.max(40, width + dx);
                height = Math.max(30, height + dy);
            }
            case WEST -> {
                int targetWidth = Math.max(40, width - dx);
                x += width - targetWidth;
                width = targetWidth;
            }
            case NORTH -> {
                int targetHeight = Math.max(30, height - dy);
                y += height - targetHeight;
                height = targetHeight;
            }
            case NORTH_WEST -> {
                int targetWidth = Math.max(40, width - dx);
                int targetHeight = Math.max(30, height - dy);
                x += width - targetWidth;
                y += height - targetHeight;
                width = targetWidth;
                height = targetHeight;
            }
            case NORTH_EAST -> {
                int targetWidth = Math.max(40, width + dx);
                int targetHeight = Math.max(30, height - dy);
                y += height - targetHeight;
                width = targetWidth;
                height = targetHeight;
            }
            case SOUTH_WEST -> {
                int targetWidth = Math.max(40, width - dx);
                int targetHeight = Math.max(30, height + dy);
                x += width - targetWidth;
                width = targetWidth;
                height = targetHeight;
            }
            default -> {
            }
        }
        SnapResult snapped = snapCatmapRectangle(new Rectangle(x, y, width, height), itemId);
        applySnapResultToItem(item, snapped);
        dialog.persistCatmapItems();
    }

    private void applySnapResultToItem(CatmapLayoutItem item, SnapResult snapped) {
        if (item == null || snapped == null) {
            return;
        }
        Rectangle bounds = snapped.bounds();
        item.setX(bounds.x);
        item.setY(bounds.y);
        item.setWidth(bounds.width);
        item.setHeight(bounds.height);
        activeGuideXs.clear();
        activeGuideXs.addAll(snapped.guideXs());
        activeGuideYs.clear();
        activeGuideYs.addAll(snapped.guideYs());
    }

    private void drawPersistentGuides(Graphics2D g2, int pageX, int pageY, double scale, int drawWidth, int drawHeight, LayoutSettings settings) {
        if (guides.isEmpty() || lastRenderResult == null) return;
        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setColor(new Color(0x3388FF));
            copy.setStroke(new BasicStroke(1.0f));
            for (GuideLine guide : guides) {
                guide.render(copy, pageX, pageY, drawWidth, drawHeight, MapLayoutComposerDialog.PREVIEW_RENDER_DPI, scale);
            }
        } finally {
            copy.dispose();
        }
    }

    private void handleRightClick(MouseEvent e) {
        Point pagePoint = toPagePoint(e.getPoint());
        RectMm pageRect = dialog.toPageRectMm();
        LayoutElement rightClicked = null;
        if (pagePoint != null && pageRect != null && dialog.layoutModel.size() > 0) {
        double xMm = pageRect.xMm + pagePoint.x * pageRect.pxToMmScale;
        double yMm = pageRect.yMm + pagePoint.y * pageRect.pxToMmScale;
        rightClicked = dialog.layoutModel.findTopmostElementAtMm(xMm, yMm);
        }
        JPopupMenu menu = new JPopupMenu();
        if (rightClicked != null) {
            boolean selWas = rightClicked.isSelected();
            dialog.layoutModel.clearSelection();
            rightClicked.setSelected(true);
            refresh(null);
            final LayoutElement rce = rightClicked;
            menu.add(item("Propiedades", () -> openElementProperties(rce)));
            menu.addSeparator();
            menu.add(item("Traer al frente", () -> { dialog.layoutModel.moveToFront(rce); refresh(null); }));
            menu.add(item("Enviar atras", () -> { dialog.layoutModel.moveToBack(rce); refresh(null); }));
            menu.add(item("Subir", () -> { dialog.layoutModel.moveUp(rce); refresh(null); }));
            menu.add(item("Bajar", () -> { dialog.layoutModel.moveDown(rce); refresh(null); }));
            menu.addSeparator();
            menu.add(item(rce.isLocked() ? "Desbloquear" : "Bloquear", () -> { rce.setLocked(!rce.isLocked()); refresh(null); }));
            menu.add(item(rce.isVisible() ? "Ocultar" : "Mostrar", () -> { rce.setVisible(!rce.isVisible()); refresh(null); }));
            menu.addSeparator();
            menu.add(item("Duplicar", () -> {
                LayoutElement dup = duplicateElement(rce);
                if (dup != null) refresh(null);
            }));
            menu.add(item("Renombrar...", () -> {
                String newName = JOptionPane.showInputDialog(this, "Nuevo nombre:", rce.getName());
                if (newName != null && !newName.isBlank()) { rce.setName(newName.trim()); refresh(null); }
            }));
            menu.addSeparator();
            menu.add(item("Eliminar", () -> { dialog.layoutModel.removeElement(rce.getId()); refresh(null); }));
            if (rce instanceof LayoutLegend) {
                menu.addSeparator();
                menu.add(item("Actualizar desde capas", () -> {
                    dialog.populateLegendFromProject((LayoutLegend) rce);
                    refresh(null);
                }));
                menu.add(item("Excluir mapas base", () -> {
                    LayoutLegend leg = (LayoutLegend) rce;
                    leg.getItems().removeIf(item -> LayoutLegend.isBasemapName(item.displayName));
                    refresh(null);
                }));
            }
            if (rce instanceof LayoutMap) {
                menu.addSeparator();
                menu.add(item("Actualizar desde vista actual", () -> refresh(null)));
            }
            if (!selWas) {
                rightClicked = rce;
            }
        } else {
            menu.add(item("Pegar", () -> {})).setEnabled(false);
        }
        menu.show(this, e.getX(), e.getY());
    }

    void openElementProperties(LayoutElement el) {
        if (el instanceof LayoutLabel) { showTextPopup((LayoutLabel) el); return; }
        if (el instanceof LayoutLegend) { showLegendPopup((LayoutLegend) el); return; }
        if (el instanceof LayoutCartouche) { dialog.showCartouchePopup((LayoutCartouche) el); return; }
        if (el instanceof LayoutScaleBar) { dialog.showScalePopup((LayoutScaleBar) el); return; }
        if (el instanceof LayoutNorthArrow) { dialog.showNorthPopup((LayoutNorthArrow) el); return; }
        if (el instanceof LayoutMap) { dialog.showMapPropsPopup((LayoutMap) el); return; }
        dialog.refreshPropertiesPanel();
    }

    void showTextPopup(LayoutLabel label) {
        JDialog popup = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Formato de texto", true);
        popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Font originalFont = label.getFont();
        Color originalColor = label.getColor();
        float originalHaloWidth = label.getHaloWidth();
        Color originalHaloColor = label.getHaloColor();
        boolean originalUnderline = label.isUnderlined();
        String originalText = label.getText();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        panel.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 3, 3, 3);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int row = 0;

        JTextField contentField = new JTextField(label.getText() != null ? label.getText() : "", 24);
        addPopupRow(form, g, row++, "Contenido", contentField);

        JComboBox<String> fontCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontCombo.setSelectedItem(originalFont.getFamily());
        addPopupRow(form, g, row++, "Fuente", fontCombo);

        Integer[] sizes = {6, 7, 8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 28, 32, 36, 42, 48, 56, 64, 72};
        JComboBox<Integer> sizeCombo = new JComboBox<>(sizes);
        sizeCombo.setEditable(true);
        sizeCombo.setSelectedItem(originalFont.getSize());
        addPopupRow(form, g, row++, "TamaÃ±o", sizeCombo);

        JPanel styleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        styleRow.setOpaque(false);
        JToggleButton boldBtn = new JToggleButton("N", originalFont.isBold());
        boldBtn.setFont(boldBtn.getFont().deriveFont(Font.BOLD));
        JToggleButton italicBtn = new JToggleButton("K", originalFont.isItalic());
        italicBtn.setFont(italicBtn.getFont().deriveFont(Font.ITALIC));
        JToggleButton underlineBtn = new JToggleButton("S", originalUnderline);
        styleRow.add(boldBtn);
        styleRow.add(italicBtn);
        styleRow.add(underlineBtn);
        addPopupRow(form, g, row++, "Estilo", styleRow);

        JButton colorBtn = colorSwatchButton(label.getColor(), null);
        addPopupRow(form, g, row++, "Color", colorBtn);

        JCheckBox haloCheck = new JCheckBox("Activar halo", originalHaloWidth > 0);
        haloCheck.setOpaque(false);
        Integer[] haloVals = {1, 2, 3, 4, 5};
        JComboBox<Integer> haloWidthCombo = new JComboBox<>(haloVals);
        haloWidthCombo.setSelectedItem(Math.max(1, (int) originalHaloWidth));
        JButton haloColorBtn = colorSwatchButton(originalHaloColor, null);
        JPanel haloRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        haloRow.setOpaque(false);
        haloRow.add(haloCheck);
        haloRow.add(new JLabel("Ancho"));
        haloRow.add(haloWidthCombo);
        haloRow.add(new JLabel("Color"));
        haloRow.add(haloColorBtn);
        addPopupRow(form, g, row++, "Halo", haloRow);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton acceptBtn = new JButton("Aceptar");
        JButton cancelBtn = new JButton("Cancelar");
        buttons.add(acceptBtn);
        buttons.add(cancelBtn);

        Runnable applyChanges = () -> {
            label.setText(contentField.getText());
            int style = Font.PLAIN;
            if (boldBtn.isSelected()) style |= Font.BOLD;
            if (italicBtn.isSelected()) style |= Font.ITALIC;
            int size = parsePopupInt(sizeCombo.getSelectedItem(), originalFont.getSize());
            label.setFont(new Font((String) fontCombo.getSelectedItem(), style, size));
            label.setColor(colorBtn.getBackground());
            label.setUnderlined(underlineBtn.isSelected());
            if (haloCheck.isSelected()) {
                label.setHaloWidth(parsePopupInt(haloWidthCombo.getSelectedItem(), 2));
                label.setHaloColor(haloColorBtn.getBackground());
            } else {
                label.setHaloWidth(0f);
            }
            repaint();
            dialog.refreshElementList();
        };

        acceptBtn.addActionListener(e -> {
            applyChanges.run();
            popup.dispose();
        });
        cancelBtn.addActionListener(e -> {
            label.setFont(originalFont);
            label.setColor(originalColor);
            label.setHaloWidth(originalHaloWidth);
            label.setHaloColor(originalHaloColor);
            label.setUnderlined(originalUnderline);
            label.setText(originalText);
            popup.dispose();
        });

        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        popup.setContentPane(panel);
        popup.pack();
        popup.setResizable(false);
        popup.getRootPane().setDefaultButton(acceptBtn);
        panel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        panel.getActionMap().put("cancel", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { cancelBtn.doClick(); }
        });
        popup.setLocationRelativeTo(this);
        popup.setVisible(true);
    }

    void showLegendPopup(LayoutLegend legend) {
        JDialog popup = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Propiedades de leyenda", true);
        popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        String originalTitle = legend.getTitle();
        Font originalTitleFont = legend.getTitleFont();
        Font originalItemFont = legend.getItemFont();
        Color originalTitleColor = legend.getTitleColor();
        Color originalItemColor = legend.getItemColor();
        boolean originalBg = legend.isShowBackground();
        Color originalBgColor = legend.getBgColor();
        float originalBgOpacity = legend.getBgOpacity();
        boolean originalBorder = legend.isShowBorder();
        Color originalBorderColor = legend.getBorderColor();
        int originalColumns = legend.getColumns();
        boolean originalAutoHeight = legend.isAutoHeight();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        panel.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 3, 3, 3);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int row = 0;

        JTextField titleField = new JTextField(legend.getTitle() != null ? legend.getTitle() : "", 22);
        addPopupRow(form, g, row++, "TÃ­tulo", titleField);

        JComboBox<String> titleFontCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        titleFontCombo.setSelectedItem(legend.getTitleFont().getFamily());
        addPopupRow(form, g, row++, "Fuente tÃ­tulo", titleFontCombo);

        Integer[] titleSizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 28, 32, 36};
        JComboBox<Integer> titleSize = new JComboBox<>(titleSizes);
        titleSize.setEditable(true);
        titleSize.setSelectedItem(legend.getTitleFont().getSize());
        addPopupRow(form, g, row++, "Tam. tÃ­tulo", titleSize);

        JComboBox<String> itemFontCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        itemFontCombo.setSelectedItem(legend.getItemFont().getFamily());
        addPopupRow(form, g, row++, "Fuente Ã­tems", itemFontCombo);

        Integer[] itemSizes = {6, 7, 8, 9, 10, 11, 12, 14, 16, 18, 20};
        JComboBox<Integer> itemSize = new JComboBox<>(itemSizes);
        itemSize.setEditable(true);
        itemSize.setSelectedItem(legend.getItemFont().getSize());
        addPopupRow(form, g, row++, "Tam. Ã­tems", itemSize);

        JButton titleColorBtn = colorSwatchButton(legend.getTitleColor(), null);
        addPopupRow(form, g, row++, "Color tÃ­tulo", titleColorBtn);

        JButton itemColorBtn = colorSwatchButton(legend.getItemColor(), null);
        addPopupRow(form, g, row++, "Color Ã­tems", itemColorBtn);

        JCheckBox bgCheck = new JCheckBox("Mostrar fondo", legend.isShowBackground());
        bgCheck.setOpaque(false);
        JButton bgColorBtn = colorSwatchButton(legend.getBgColor(), null);
        JSpinner opacitySpinner = new JSpinner(new SpinnerNumberModel((int) Math.round(legend.getBgOpacity() * 100d), 0, 100, 5));
        JPanel bgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bgRow.setOpaque(false);
        bgRow.add(bgCheck);
        bgRow.add(new JLabel("Color"));
        bgRow.add(bgColorBtn);
        bgRow.add(new JLabel("Opacidad"));
        bgRow.add(opacitySpinner);
        addPopupRow(form, g, row++, "Fondo", bgRow);

        JCheckBox borderCheck = new JCheckBox("Mostrar borde", legend.isShowBorder());
        borderCheck.setOpaque(false);
        JButton borderColorBtn = colorSwatchButton(legend.getBorderColor(), null);
        JPanel borderRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        borderRow.setOpaque(false);
        borderRow.add(borderCheck);
        borderRow.add(new JLabel("Color"));
        borderRow.add(borderColorBtn);
        addPopupRow(form, g, row++, "Borde", borderRow);

        JComboBox<Integer> colCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        colCombo.setSelectedItem(legend.getColumns());
        addPopupRow(form, g, row++, "Columnas", colCombo);

        JCheckBox autoHeightCheck = new JCheckBox("Auto alto", legend.isAutoHeight());
        autoHeightCheck.setOpaque(false);
        addPopupRow(form, g, row++, "Ajuste", autoHeightCheck);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton refreshBtn = new JButton("Actualizar capas");
        JButton acceptBtn = new JButton("Aceptar");
        JButton cancelBtn = new JButton("Cancelar");
        buttons.add(refreshBtn);
        buttons.add(acceptBtn);
        buttons.add(cancelBtn);

        refreshBtn.addActionListener(e -> {
            dialog.populateLegendFromProject(legend);
            repaint();
        });

        Runnable applyChanges = () -> {
            legend.setTitle(dialog.titleField.getText().trim());
            legend.setTitleFont(new Font((String) titleFontCombo.getSelectedItem(), Font.BOLD, parsePopupInt(titleSize.getSelectedItem(), originalTitleFont.getSize())));
            legend.setItemFont(new Font((String) itemFontCombo.getSelectedItem(), Font.PLAIN, parsePopupInt(itemSize.getSelectedItem(), originalItemFont.getSize())));
            legend.setTitleColor(titleColorBtn.getBackground());
            legend.setItemColor(itemColorBtn.getBackground());
            legend.setShowBackground(bgCheck.isSelected());
            legend.setBgColor(bgColorBtn.getBackground());
            legend.setBgOpacity(((Integer) opacitySpinner.getValue()) / 100f);
            legend.setShowBorder(borderCheck.isSelected());
            legend.setBorderColor(borderColorBtn.getBackground());
            legend.setColumns((Integer) colCombo.getSelectedItem());
            legend.setAutoHeight(autoHeightCheck.isSelected());
            repaint();
            dialog.refreshElementList();
        };

        acceptBtn.addActionListener(e -> {
            applyChanges.run();
            popup.dispose();
        });
        cancelBtn.addActionListener(e -> {
            legend.setTitle(originalTitle);
            legend.setTitleFont(originalTitleFont);
            legend.setItemFont(originalItemFont);
            legend.setTitleColor(originalTitleColor);
            legend.setItemColor(originalItemColor);
            legend.setShowBackground(originalBg);
            legend.setBgColor(originalBgColor);
            legend.setBgOpacity(originalBgOpacity);
            legend.setShowBorder(originalBorder);
            legend.setBorderColor(originalBorderColor);
            legend.setColumns(originalColumns);
            legend.setAutoHeight(originalAutoHeight);
            popup.dispose();
        });

        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        popup.setContentPane(panel);
        popup.pack();
        popup.setResizable(false);
        popup.getRootPane().setDefaultButton(acceptBtn);
        panel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        panel.getActionMap().put("cancel", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { cancelBtn.doClick(); }
        });
        popup.setLocationRelativeTo(this);
        popup.setVisible(true);
    }

    private void addPopupRow(JPanel form, GridBagConstraints g, int row, String label, Component field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.weightx = 0;
        form.add(lbl, g);
        g.gridx = 1;
        g.weightx = 1;
        form.add(field, g);
    }

    private JButton colorSwatchButton(Color initial, java.util.function.Consumer<Color> onColor) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(34, 22));
        colorBtnSet(btn, initial != null ? initial : Color.BLACK);
        btn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Elegir color", btn.getBackground());
            if (chosen != null) {
                colorBtnSet(btn, chosen);
                if (onColor != null) {
                    onColor.accept(chosen);
                }
            }
        });
        return btn;
    }

    private void colorBtnSet(JButton button, Color color) {
        button.setBackground(color);
        button.setOpaque(true);
        button.setBorderPainted(true);
    }

    private int parsePopupInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private LayoutElement duplicateElement(LayoutElement src) {
        dialog.duplicateLayoutElement(src);
        return null;
    }

    private void refresh(Boolean unused) { dialog.refreshElementList(); repaint(); }

    void startDrawing(String shape) {
        drawingShape = shape;
        drawingStart = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        dialog.layoutModel.clearSelection();
        dialog.refreshElementList();
        repaint();
        dialog.statusLabel.setText("Dibujando " + shape + ". Click y arrastra en el canvas. Esc para cancelar.");
    }

    void cancelDrawing() {
        drawingShape = null;
        drawingStart = null;
        setCursor(Cursor.getDefaultCursor());
        repaint();
        dialog.statusLabel.setText("Dibujo cancelado.");
    }

    private void finishDrawing(Point pageEnd) {
        if (drawingStart == null || drawingShape == null) return;
        RectMm pr = dialog.toPageRectMm();
        if (pr == null) return;
        double sc = pr.pxToMmScale;
        double x1 = Math.min(drawingStart.x, pageEnd.x) * sc;
        double y1 = Math.min(drawingStart.y, pageEnd.y) * sc;
        double x2 = Math.max(drawingStart.x, pageEnd.x) * sc;
        double y2 = Math.max(drawingStart.y, pageEnd.y) * sc;
        double w = x2 - x1, h = y2 - y1;
        if (w < 2 && h < 2) { w = 20; h = 15; }

        if ("rect".equals(drawingShape)) {
            LayoutRectangle r = new LayoutRectangle(drawingShape + "-" + System.currentTimeMillis(), x1, y1, w, h);
            r.setZOrder(dialog.layoutModel.nextZ()); r.setName("Rectangulo " + dialog.countOfType("Rectangulo"));
            dialog.layoutModel.addElement(r);
        } else if ("ellipse".equals(drawingShape)) {
            LayoutEllipse e = new LayoutEllipse(drawingShape + "-" + System.currentTimeMillis(), x1, y1, w, h);
            e.setZOrder(dialog.layoutModel.nextZ()); e.setName("Elipse " + dialog.countOfType("Elipse"));
            dialog.layoutModel.addElement(e);
        } else if ("line".equals(drawingShape)) {
            LayoutLine l = new LayoutLine(drawingShape + "-" + System.currentTimeMillis(), x1, y1, x2, y2);
            l.setZOrder(dialog.layoutModel.nextZ()); l.setName("Linea " + dialog.countOfType("Linea"));
            dialog.layoutModel.addElement(l);
        }
        drawingShape = null; drawingStart = null;
        setCursor(Cursor.getDefaultCursor());
        dialog.refreshElementList();
        repaint();
        dialog.statusLabel.setText("Forma creada.");
    }

    private JMenuItem item(String text, Runnable action) {
        JMenuItem mi = new JMenuItem(text);
        mi.addActionListener(e -> action.run());
        return mi;
    }

    private void drawDrawingPreview(Graphics2D g2, int pageX, int pageY, double scale) {
        if (drawingShape == null || drawingStart == null) return;
        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi == null) return;
        Point screenPt = pi.getLocation();
        SwingUtilities.convertPointFromScreen(screenPt, this);
        Point pageEnd = toPagePoint(screenPt);
        if (pageEnd == null) return;
        int x1 = pageX + (int)(Math.min(drawingStart.x, pageEnd.x) * scale);
        int y1 = pageY + (int)(Math.min(drawingStart.y, pageEnd.y) * scale);
        int w = (int)(Math.abs(pageEnd.x - drawingStart.x) * scale);
        int h = (int)(Math.abs(pageEnd.y - drawingStart.y) * scale);
        g2.setColor(new Color(25, 118, 210, 100));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{6f, 4f}, 0f));
        g2.drawRect(x1, y1, Math.max(1, w), Math.max(1, h));
    }

    private void clearSnapGuides() {
        activeGuideXs.clear();
        activeGuideYs.clear();
    }

    private SnapResult snapCatmapRectangle(Rectangle proposed, String movingItemId) {
        if (proposed == null || lastRenderResult == null) {
            return new SnapResult(proposed != null ? new Rectangle(proposed) : new Rectangle(), new ArrayList<>(), new ArrayList<>());
        }
        Rectangle snapped = new Rectangle(proposed);
        List<Integer> guideXs = new ArrayList<>();
        List<Integer> guideYs = new ArrayList<>();
        int tolerance = 8;

        SnapAxisResult xAxis = snapAxis(
                snapped.x,
                snapped.x + snapped.width / 2,
                snapped.x + snapped.width,
                collectSnapCandidates(true, movingItemId),
                tolerance
        );
        snapped.x += xAxis.delta();
        if (xAxis.guide() != null) {
            guideXs.add(xAxis.guide());
        }

        SnapAxisResult yAxis = snapAxis(
                snapped.y,
                snapped.y + snapped.height / 2,
                snapped.y + snapped.height,
                collectSnapCandidates(false, movingItemId),
                tolerance
        );
        snapped.y += yAxis.delta();
        if (yAxis.guide() != null) {
            guideYs.add(yAxis.guide());
        }
        return new SnapResult(snapped, guideXs, guideYs);
    }

    private List<Integer> collectSnapCandidates(boolean horizontal, String movingItemId) {
        List<Integer> candidates = new ArrayList<>();
        if (lastRenderResult == null || lastRenderResult.image() == null) {
            return candidates;
        }
        int max = horizontal ? lastRenderResult.image().getWidth() : lastRenderResult.image().getHeight();
        candidates.add(0);
        candidates.add(max / 2);
        candidates.add(max);

        for (Rectangle bounds : lastRenderResult.elementBounds().values()) {
            if (bounds == null) {
                continue;
            }
            candidates.add(horizontal ? bounds.x : bounds.y);
            candidates.add(horizontal ? bounds.x + bounds.width / 2 : bounds.y + bounds.height / 2);
            candidates.add(horizontal ? bounds.x + bounds.width : bounds.y + bounds.height);
        }
        for (java.util.Map.Entry<String, Rectangle> entry : lastRenderResult.customItemBounds().entrySet()) {
            if (entry.getKey() == null || entry.getKey().equals(movingItemId) || entry.getValue() == null) {
                continue;
            }
            Rectangle bounds = entry.getValue();
            candidates.add(horizontal ? bounds.x : bounds.y);
            candidates.add(horizontal ? bounds.x + bounds.width / 2 : bounds.y + bounds.height / 2);
            candidates.add(horizontal ? bounds.x + bounds.width : bounds.y + bounds.height);
        }
        return candidates;
    }

    private SnapAxisResult snapAxis(int start, int center, int end, List<Integer> candidates, int tolerance) {
        int bestDelta = 0;
        Integer bestGuide = null;
        int bestDistance = tolerance + 1;
        for (Integer candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            int[] deltas = new int[]{candidate - start, candidate - center, candidate - end};
            for (int delta : deltas) {
                int distance = Math.abs(delta);
                if (distance <= tolerance && distance < bestDistance) {
                    bestDistance = distance;
                    bestDelta = delta;
                    bestGuide = candidate;
                }
            }
        }
        return new SnapAxisResult(bestDelta, bestGuide);
    }

    private Point toPagePoint(Point panelPoint) {
        if (lastRenderResult == null || lastPageBounds.width <= 0 || lastPageBounds.height <= 0 || lastPreviewScale <= 0) {
            return null;
        }
        if (!lastPageBounds.contains(panelPoint)) {
            return null;
        }
        int pageX = (int) Math.round((panelPoint.x - lastPageBounds.x) / lastPreviewScale);
        int pageY = (int) Math.round((panelPoint.y - lastPageBounds.y) / lastPreviewScale);
        return new Point(pageX, pageY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        dialog.syncHardcodedLayoutFlagsFromModel();
        LayoutSettings settings = dialog.buildSettings();
        LayoutSnapshot currentSnapshot = dialog.getSnapshot();
        if (settings == null || currentSnapshot == null) return;

        Dimension previewSize = settings.pageSize().pixelSize(settings.orientation(), MapLayoutComposerDialog.PREVIEW_RENDER_DPI);
        lastRenderResult = LayoutPageRenderer.renderResult(
                settings,
                currentSnapshot,
                previewSize.width,
                previewSize.height,
                dialog.interactionState,
                MapLayoutComposerDialog.PREVIEW_RENDER_DPI
        );
        SwingUtilities.invokeLater(() -> dialog.updateScaleUiState(lastRenderResult.exactScaleDenominator()));
        BufferedImage page = lastRenderResult.image();

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            Dimension viewportSize = resolvePreviewViewportSize();
            int availableWidth = Math.max(80, viewportSize.width - 40);
            int availableHeight = Math.max(80, viewportSize.height - 40);
            double fitPageScale = Math.min(availableWidth / (double) page.getWidth(), availableHeight / (double) page.getHeight());
            double fitWidthScale = availableWidth / (double) page.getWidth();
            double scale = dialog.interactionState.resolvePreviewScale(fitPageScale, fitWidthScale);
            scale = Math.max(0.08d, scale);
            int drawWidth = (int) Math.round(page.getWidth() * scale);
            int drawHeight = (int) Math.round(page.getHeight() * scale);
            int rulerSz = RulerRenderer.getRulerSize();
            int x = Math.max(rulerSz + 4, (getWidth() - drawWidth) / 2);
            int y = Math.max(rulerSz + 4, (getHeight() - drawHeight) / 2);

            lastPageBounds = new Rectangle(x, y, drawWidth, drawHeight);
            lastPreviewScale = scale;

            g2.setColor(new Color(200, 205, 212));
            g2.fillRoundRect(x + 10, y + 10, drawWidth, drawHeight, 18, 18);
            // Stronger page shadow (ArcMap-style)
            g2.setColor(new Color(140, 145, 155));
            g2.fillRoundRect(x + 6, y + 6, drawWidth, drawHeight, 14, 14);
            g2.setColor(new Color(170, 175, 185));
            g2.fillRoundRect(x + 4, y + 4, drawWidth, drawHeight, 14, 14);
            // White page
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, drawWidth, drawHeight, 4, 4);
            // Page grid dots (5mm spacing)
            g2.setColor(new Color(210, 215, 225));
            double gridMm = 5;
            double pxPerMmGrid = MapLayoutComposerDialog.PREVIEW_RENDER_DPI / 25.4 * scale;
            for (double gx = 0; gx <= settings.pageSize().widthMm; gx += gridMm) {
                int px = x + (int)(gx * pxPerMmGrid);
                for (double gy = 0; gy <= settings.pageSize().heightMm; gy += gridMm) {
                    int py = y + (int)(gy * pxPerMmGrid);
                    if (px >= x && py >= y && px <= x + drawWidth && py <= y + drawHeight)
                        g2.fillRect(px, py, 2, 2);
                }
            }
            g2.drawImage(page, x, y, drawWidth, drawHeight, null);
            // Subtle page border on dark canvas
            g2.setColor(new Color(200, 205, 212));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, drawWidth, drawHeight, 4, 4);
            dialog.drawLayoutModelOverlay(g2, settings, x, y, scale);
            // Render dynamic layout elements via CanvasRenderer
            dialog.canvasRenderer.setScale(scale);
            dialog.canvasRenderer.render(g2);
            drawSnapGuides(g2, x, y, scale, drawWidth, drawHeight);
            drawPersistentGuides(g2, x, y, scale, drawWidth, drawHeight, settings);
            drawDrawingPreview(g2, x, y, scale);
            RulerRenderer.render(g2, 0, 0, getWidth(), getHeight(), settings.pageSize().widthMm, settings.pageSize().heightMm, MapLayoutComposerDialog.PREVIEW_RENDER_DPI, scale);
            SwingUtilities.invokeLater(() -> dialog.refreshElementList());
            drawSelectionOverlay(g2, x, y, scale);
            if (inlineTitleEditor.isVisible()) {
                Rectangle headerBounds = lastRenderResult.elementBounds().get(LayoutElementType.HEADER);
                if (headerBounds != null) {
                    inlineTitleEditor.setBounds(
                            lastPageBounds.x + (int) Math.round((headerBounds.x + 2) * lastPreviewScale),
                            lastPageBounds.y + (int) Math.round((headerBounds.y + 6) * lastPreviewScale),
                            Math.max(180, (int) Math.round(Math.min(headerBounds.width * 0.72d, 460) * lastPreviewScale)),
                            Math.max(28, (int) Math.round(34 * lastPreviewScale))
                    );
                }
            }
            if (inlineCartoucheEditor.isVisible()) {
                Rectangle cartoucheBounds = lastRenderResult.elementBounds().get(LayoutElementType.CARTOUCHE);
                if (cartoucheBounds != null) {
                    placeInlineCartoucheEditor(cartoucheBounds);
                }
            }

            g2.setColor(new Color(57, 67, 82));
            g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
            String label = settings.pageSize() + " - " + settings.orientation() + " | " + settings.template();
            g2.drawString(label, x + 12, Math.max(18, y - 10));
        } finally {
            g2.dispose();
        }
    }

    private void drawSelectionOverlay(Graphics2D g2, int pageX, int pageY, double scale) {
        LayoutElementType selected = dialog.interactionState.getSelectedElement();
        if (selected == null || lastRenderResult == null) {
            return;
        }
        Rectangle bounds = selected == LayoutElementType.CATMAP_ITEM
                ? lastRenderResult.customItemBounds().get(dialog.interactionState.getSelectedCustomItemId())
                : lastRenderResult.elementBounds().get(selected);
        if (bounds == null) {
            return;
        }
        int x = pageX + (int) Math.round(bounds.x * scale);
        int y = pageY + (int) Math.round(bounds.y * scale);
        int w = Math.max(18, (int) Math.round(bounds.width * scale));
        int h = Math.max(18, (int) Math.round(bounds.height * scale));
        Graphics2D copy = (Graphics2D) g2.create();
        try {
            Color fill = new Color(37, 99, 235, 26);
            Color stroke = new Color(37, 99, 235);
            if (selected == LayoutElementType.MAP_CONTENT && dialog.interactionState.isMapFramePanToolActive()) {
                fill = new Color(16, 185, 129, 28);
                stroke = new Color(5, 150, 105);
            } else if (selected == LayoutElementType.MAP_CONTENT && dialog.interactionState.isMapFrameZoomToolActive()) {
                fill = new Color(245, 158, 11, 28);
                stroke = new Color(217, 119, 6);
            }
            copy.setColor(fill);
            copy.fillRoundRect(x, y, w, h, 12, 12);
            copy.setColor(stroke);
            copy.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{7f, 5f}, 0f));
            copy.drawRoundRect(x, y, w, h, 12, 12);
            boolean showHandles = selected == LayoutElementType.MAP_CONTENT
                    ? dialog.interactionState.isMapFrameMoveToolActive()
                    : (selected == LayoutElementType.HEADER
                    || selected == LayoutElementType.LEGEND
                    || selected == LayoutElementType.NORTH
                    || selected == LayoutElementType.SCALE
                    || selected == LayoutElementType.CARTOUCHE
                    || selected == LayoutElementType.PROFILE_IMAGE
                    || selected == LayoutElementType.CATMAP_ITEM);
            if (showHandles) {
                drawResizeHandle(copy, x, y);
                drawResizeHandle(copy, x + w / 2, y);
                drawResizeHandle(copy, x + w, y);
                drawResizeHandle(copy, x, y + h / 2);
                drawResizeHandle(copy, x + w, y + h / 2);
                drawResizeHandle(copy, x, y + h);
                drawResizeHandle(copy, x + w / 2, y + h);
                drawResizeHandle(copy, x + w, y + h);
            }
        } finally {
            copy.dispose();
        }
    }

    private void drawSnapGuides(Graphics2D g2, int pageX, int pageY, double scale, int drawWidth, int drawHeight) {
        if ((activeGuideXs.isEmpty() && activeGuideYs.isEmpty()) || lastRenderResult == null) {
            return;
        }
        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setColor(new Color(14, 116, 144, 170));
            copy.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{4f, 4f}, 0f));
            for (Integer guideX : activeGuideXs) {
                int x = pageX + (int) Math.round(guideX * scale);
                copy.drawLine(x, pageY, x, pageY + drawHeight);
            }
            for (Integer guideY : activeGuideYs) {
                int y = pageY + (int) Math.round(guideY * scale);
                copy.drawLine(pageX, y, pageX + drawWidth, y);
            }
        } finally {
            copy.dispose();
        }
    }

    private void drawResizeHandle(Graphics2D g2, int centerX, int centerY) {
        int size = 8;
        g2.setColor(Color.WHITE);
        g2.fillRect(centerX - size / 2, centerY - size / 2, size, size);
        g2.setColor(new Color(37, 99, 235));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRect(centerX - size / 2, centerY - size / 2, size, size);
    }

    private record ResizeTarget(LayoutElementType elementType, ResizeHandle handle, String customItemId) {
    }

    private record SnapResult(Rectangle bounds, List<Integer> guideXs, List<Integer> guideYs) {
    }

    private record SnapAxisResult(int delta, Integer guide) {
    }
}