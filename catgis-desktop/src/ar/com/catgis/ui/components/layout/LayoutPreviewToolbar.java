package ar.com.catgis.ui.components.layout;

import ar.com.catgis.AppIcons;
import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutImage;
import ar.com.catgis.layout.LayoutInteractionState;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutLegend;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutNorthArrow;
import ar.com.catgis.layout.LayoutScaleBar;
import java.awt.FlowLayout;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Preview toolbar with ~45 buttons in 9 groups.
 * Extracted from {@code buildPreviewToolbar()}.
 */
public class LayoutPreviewToolbar extends JPanel {

    private final JButton selectionToolButton;
    private final JButton mapPanToolButton;
    private final JButton mapZoomToolButton;

    public LayoutPreviewToolbar(
            PreviewToolbarActions actions,
            LayoutModel layoutModel,
            LayoutInteractionState interactionState) {
        super(new FlowLayout(FlowLayout.LEFT, 10, 0));
        setOpaque(false);

        // --- Documento ---
        add(LayoutToolbarFactory.buildToolbarGroup("Documento",
                LayoutToolbarFactory.createToolbarButton("Guardar", AppIcons.saveIcon(), "Guardar layout (.catmap)", actions::saveLayout),
                LayoutToolbarFactory.createToolbarButton("Abrir", AppIcons.openIcon(), "Abrir layout (.catmap)", actions::loadLayout),
                LayoutToolbarFactory.createToolbarButton("Exportar", AppIcons.exportIcon(), "Exportar a PDF (un clic)", actions::exportPdf),
                LayoutToolbarFactory.createToolbarButton("Exportar PNG", AppIcons.exportIcon(), "Exportar a imagen PNG", actions::exportImage),
                LayoutToolbarFactory.createToolbarButton("SVG", null, "Exportar a SVG vectorial", actions::exportSvg),
                LayoutToolbarFactory.createToolbarButton("Imprimir", AppIcons.projectIcon(), "Imprimir layout", actions::printLayout),
                LayoutToolbarFactory.createToolbarButton("Plantillas...", null, "Abrir galeria de plantillas con vista previa.", actions::showTemplatePicker)
        ));

        // --- Trabajo ---
        selectionToolButton = LayoutToolbarFactory.createToolbarButton("Seleccionar", AppIcons.moveFeatureIcon(),
                "Selecciona y mueve elementos del layout. Modo por defecto.", actions::activateSelectionTool);
        mapPanToolButton = LayoutToolbarFactory.createToolbarButton("Pan mapa", AppIcons.panIcon(),
                "Desplaza el contenido del mapa sin mover el marco.", actions::activateMapPanTool);
        mapZoomToolButton = LayoutToolbarFactory.createToolbarButton("Zoom mapa", AppIcons.zoomInIcon(),
                "Zoom del contenido del mapa con la rueda del mouse.", actions::activateMapFrameZoomTool);

        add(LayoutToolbarFactory.buildToolbarGroup("Trabajo",
                selectionToolButton,
                mapPanToolButton,
                mapZoomToolButton
        ));

        // --- Insertar ---
        add(LayoutToolbarFactory.buildToolbarGroup("Insertar",
                LayoutToolbarFactory.createToolbarButton("Mapa", AppIcons.genericLayerIcon(), "Inserta un map frame vivo sincronizado.", () -> {
                    LayoutMap map = new LayoutMap("map-" + System.currentTimeMillis(), 15, 25, 267, 145);
                    map.setZOrder(layoutModel.nextZ());
                    map.setName("Mapa " + (layoutModel.size() + 1));
                    map.setFrameColor(new java.awt.Color(0x4A5568));
                    map.setFrameWidth(0.8f);
                    map.setShowGrid(false);
                    layoutModel.addElement(map);
                    actions.refreshElementList();
                    actions.repaintPreview();
                }),
                LayoutToolbarFactory.createToolbarButton("Leyenda", AppIcons.tableIcon(), "Inserta una leyenda.", () -> {
                    LayoutLegend leg = new LayoutLegend("leg-" + System.currentTimeMillis(), 155, 55, 75, 40);
                    leg.setZOrder(layoutModel.nextZ());
                    leg.setAutoHeight(true);
                    leg.setName("Leyenda " + (layoutModel.size() + 1));
                    layoutModel.addElement(leg);
                    actions.refreshElementList();
                    actions.repaintPreview();
                }),
                LayoutToolbarFactory.createToolbarButton("Escala", null, "Inserta una barra de escala.", () -> {
                    LayoutScaleBar sb = new LayoutScaleBar("scale-" + System.currentTimeMillis(), 145, 175, 95, 10);
                    sb.setZOrder(layoutModel.nextZ());
                    sb.setName("Escala " + (layoutModel.size() + 1));
                    layoutModel.addElement(sb);
                    actions.refreshElementList();
                    actions.repaintPreview();
                }),
                LayoutToolbarFactory.createToolbarButton("Norte", null, "Inserta una flecha de norte.", () -> {
                    LayoutNorthArrow na = new LayoutNorthArrow("north-" + System.currentTimeMillis(), 250, 30, 16, 16);
                    na.setZOrder(layoutModel.nextZ());
                    na.setName("Norte " + (layoutModel.size() + 1));
                    layoutModel.addElement(na);
                    actions.refreshElementList();
                    actions.repaintPreview();
                }),
                LayoutToolbarFactory.createToolbarButton("Texto", AppIcons.attrEditIcon(), "Inserta un texto libre en el layout.", () -> {
                    LayoutLabel lbl = new LayoutLabel("lbl-" + System.currentTimeMillis(), "Texto libre", 60, 60, 160, 24);
                    lbl.setZOrder(layoutModel.nextZ()); lbl.setName("Texto " + (layoutModel.size() + 1));
                    layoutModel.addElement(lbl); actions.refreshElementList(); actions.repaintPreview();
                }),
                LayoutToolbarFactory.createToolbarButton("Texto Dinamico", null, "Inserta texto auto-actualizable ({date}, {project}, {crs}, {scale})", () -> {
                    LayoutLabel lbl = new LayoutLabel("dlbl-" + System.currentTimeMillis(), "{date}", 60, 60, 160, 24);
                    lbl.setZOrder(layoutModel.nextZ());
                    lbl.setName("Texto dinamico " + (layoutModel.size() + 1));
                    lbl.setDynamicExpression("{date}");
                    lbl.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
                    lbl.setColor(new java.awt.Color(0x6B7280));
                    layoutModel.addElement(lbl);
                    actions.refreshElementList();
                    actions.repaintPreview();
                }),
                LayoutToolbarFactory.createToolbarButton("Imagen", AppIcons.imageryIcon(), "Inserta una imagen desde archivo.", () -> {
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("Imagenes", "png", "jpg", "jpeg", "gif", "bmp"));
                    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        try {
                            java.awt.image.BufferedImage bi = ImageIO.read(fc.getSelectedFile());
                            if (bi != null) {
                                double w = bi.getWidth() / 200.0 * 25.4, h = bi.getHeight() / 200.0 * 25.4;
                                LayoutImage img = new LayoutImage("img-" + System.currentTimeMillis(), bi, 50, 50, w, h);
                                img.setZOrder(layoutModel.nextZ()); img.setName(fc.getSelectedFile().getName());
                                layoutModel.addElement(img); actions.refreshElementList(); actions.repaintPreview();
                            }
                        } catch (Exception ex) {
                            ar.com.catgis.CatgisLogger.warn("Layout interaction error", ex);
                        }
                    }
                }),
                LayoutToolbarFactory.createToolbarButton("Rectangulo", AppIcons.rectangleIcon(), "Dibujar rectangulo. Click y arrastrar en el canvas.",
                        () -> actions.startDrawing("rect")),
                LayoutToolbarFactory.createToolbarButton("Elipse", AppIcons.circleIcon(), "Dibujar elipse. Click y arrastrar en el canvas.",
                        () -> actions.startDrawing("ellipse")),
                LayoutToolbarFactory.createToolbarButton("Linea", AppIcons.lineIcon(), "Dibujar linea. Click y arrastrar en el canvas.",
                        () -> actions.startDrawing("line"))
        ));

        // --- Editar ---
        add(LayoutToolbarFactory.buildToolbarGroup("Editar",
                LayoutToolbarFactory.createToolbarButton("Editar", AppIcons.propertiesIcon(), "Editar elemento seleccionado.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) actions.openElementProperties(sel);
                }),
                LayoutToolbarFactory.createToolbarButton("Duplicar", AppIcons.attrCopyIcon(), "Duplicar seleccionado.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { actions.duplicateLayoutElement(sel); actions.refreshElementList(); actions.repaintPreview(); }
                }),
                LayoutToolbarFactory.createToolbarButton("Subir", AppIcons.upIcon(), "Subir en orden visual.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { layoutModel.moveUp(sel); actions.refreshElementList(); actions.repaintPreview(); }
                }),
                LayoutToolbarFactory.createToolbarButton("Bajar", AppIcons.downIcon(), "Bajar en orden visual.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { layoutModel.moveDown(sel); actions.refreshElementList(); actions.repaintPreview(); }
                }),
                LayoutToolbarFactory.createToolbarButton("Agrupar", null, "Agrupar elementos (Ctrl+G).", () -> {
                    List<LayoutElement> selList = layoutModel.getElements().stream()
                            .filter(LayoutElement::isSelected)
                            .collect(Collectors.toList());
                    if (layoutModel.groupElements(selList) != null) {
                        actions.refreshElementList(); actions.repaintPreview();
                    }
                }),
                LayoutToolbarFactory.createToolbarButton("Desagrupar", null, "Desagrupar (Ctrl+Shift+G).", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null && sel.getGroupId() != null) {
                        layoutModel.ungroupElements(sel.getGroupId());
                        actions.refreshElementList(); actions.repaintPreview();
                    }
                }),
                LayoutToolbarFactory.createToolbarButton("Quitar", AppIcons.removeIcon(), "Eliminar seleccionado.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { layoutModel.removeElement(sel.getId()); actions.refreshElementList(); actions.repaintPreview(); }
                })
        ));

        // --- Alinear ---
        add(LayoutToolbarFactory.buildToolbarGroup("Alinear",
                LayoutToolbarFactory.createToolbarButton("Izquierda", null, "Alinear izquierda.", () -> actions.alignElements(0)),
                LayoutToolbarFactory.createToolbarButton("Centro", null, "Centrar horizontal.", () -> actions.alignElements(1)),
                LayoutToolbarFactory.createToolbarButton("Derecha", null, "Alinear derecha.", () -> actions.alignElements(2)),
                LayoutToolbarFactory.createToolbarButton("Arriba", null, "Alinear arriba.", () -> actions.alignElements(3)),
                LayoutToolbarFactory.createToolbarButton("Medio", null, "Centrar vertical.", () -> actions.alignElements(4)),
                LayoutToolbarFactory.createToolbarButton("Abajo", null, "Alinear abajo.", () -> actions.alignElements(5))
        ));

        // --- Organizar ---
        add(LayoutToolbarFactory.buildToolbarGroup("Organizar",
                LayoutToolbarFactory.createToolbarButton("Visible", AppIcons.visibleIcon(), "Mostrar/ocultar seleccionado.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { sel.setVisible(!sel.isVisible()); actions.refreshElementList(); actions.repaintPreview(); }
                }),
                LayoutToolbarFactory.createToolbarButton("Bloquear", null, "Bloquear/desbloquear seleccionado.", () -> {
                    LayoutElement sel = layoutModel.getSelected();
                    if (sel != null) { sel.setLocked(!sel.isLocked()); actions.refreshElementList(); actions.repaintPreview(); }
                })
        ));

        // --- Leyenda ---
        add(LayoutToolbarFactory.buildToolbarGroup("Leyenda",
                LayoutToolbarFactory.createToolbarButton("Leyenda", AppIcons.labelsIcon(), "Selecciona y edita la leyenda.", () -> {
                    LayoutElement leg = findElementByType(layoutModel, LayoutLegend.class);
                    if (leg != null) { layoutModel.clearSelection(); leg.setSelected(true); actions.openElementProperties(leg); actions.refreshElementList(); actions.repaintPreview(); }
                }),
                LayoutToolbarFactory.createToolbarButton("Norte", AppIcons.crsIcon(), "Selecciona y edita el norte.", () -> {
                    LayoutElement north = findElementByType(layoutModel, LayoutNorthArrow.class);
                    if (north != null) { layoutModel.clearSelection(); north.setSelected(true); actions.openElementProperties(north); actions.refreshElementList(); actions.repaintPreview(); }
                })
        ));

        // --- Mapa ---
        add(LayoutToolbarFactory.buildToolbarGroup("Mapa",
                LayoutToolbarFactory.createToolbarButton("Zoom -", AppIcons.zoomOutIcon(), "Alejar contenido del mapa.", () -> actions.adjustMapZoom(1d / 1.12d)),
                LayoutToolbarFactory.createToolbarButton("Zoom +", AppIcons.zoomInIcon(), "Acercar contenido del mapa.", () -> actions.adjustMapZoom(1.12d)),
                LayoutToolbarFactory.createToolbarButton("Reencuadrar", AppIcons.zoomAllIcon(), "Restaurar encuadre original del mapa.", actions::resetMapFrameView),
                LayoutToolbarFactory.createToolbarButton("Actualizar", AppIcons.attrRefreshIcon(), "Recapturar snapshot del mapa.", actions::refreshSnapshot)
        ));

        // --- Pagina ---
        add(LayoutToolbarFactory.buildToolbarGroup("Pagina",
                LayoutToolbarFactory.createToolbarButton("Zoom -", AppIcons.zoomOutIcon(), "Alejar vista de pagina.", () -> actions.adjustPageZoom(1d / 1.15d)),
                LayoutToolbarFactory.createToolbarButton("Zoom +", AppIcons.zoomInIcon(), "Acercar vista de pagina.", () -> actions.adjustPageZoom(1.15d)),
                LayoutToolbarFactory.createToolbarButton("Ajustar", AppIcons.zoomAllIcon(), "Ajustar pagina completa.", actions::fitPageView),
                LayoutToolbarFactory.createToolbarButton("Ajustar ancho", AppIcons.zoomLayerIcon(), "Ajustar al ancho del panel.", actions::fitWidthView),
                LayoutToolbarFactory.createToolbarButton("Reset", AppIcons.undoIcon(), "Restaurar vista por defecto.", actions::resetLayoutView)
        ));

        updateActiveWorkToolButtons(interactionState);
    }

    public JButton getSelectionToolButton() { return selectionToolButton; }
    public JButton getMapPanToolButton() { return mapPanToolButton; }
    public JButton getMapZoomToolButton() { return mapZoomToolButton; }

    public void updateActiveWorkToolButtons(LayoutInteractionState interactionState) {
        LayoutToolbarFactory.styleWorkToolButton(selectionToolButton, interactionState.isMapFrameMoveToolActive());
        LayoutToolbarFactory.styleWorkToolButton(mapPanToolButton, interactionState.isMapFramePanToolActive());
        LayoutToolbarFactory.styleWorkToolButton(mapZoomToolButton, interactionState.isMapFrameZoomToolActive());
    }

    private static LayoutElement findElementByType(LayoutModel model, Class<?> type) {
        for (LayoutElement e : model.getElements()) {
            if (type.isInstance(e)) return e;
        }
        return null;
    }
}
