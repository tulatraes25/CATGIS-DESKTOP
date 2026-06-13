package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.LayerGroup;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.data.vector.VectorLayerUtils;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.util.List;

public class LayerContextMenuBuilder {

    private final LayersPanel panel;

    public LayerContextMenuBuilder(LayersPanel panel) {
        this.panel = panel;
    }

    public void showLayerPopup(MouseEvent e, Layer selectedLayer) {
        if (selectedLayer == null) {
            return;
        }

        JPopupMenu popupMenu = panel.isRasterLayer(selectedLayer)
                ? buildRasterPopup(selectedLayer)
                : buildVectorPopup(selectedLayer);

        popupMenu.show(panel.layerList, e.getX(), e.getY());
    }

    public void showGroupPopup(MouseEvent e, LayerGroup selectedGroup) {
        if (selectedGroup == null) {
            return;
        }

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem titleItem = panel.createMenuItem(selectedGroup.getName(), AppIcons.openIcon());
        titleItem.setEnabled(false);
        popupMenu.add(titleItem);
        popupMenu.addSeparator();

        JMenuItem toggleVisibilityItem = panel.createMenuItem(
                selectedGroup.isVisible() ? "Ocultar grupo" : "Mostrar grupo",
                selectedGroup.isVisible() ? AppIcons.hiddenIcon() : AppIcons.visibleIcon()
        );
        toggleVisibilityItem.addActionListener(ev -> {
            selectedGroup.setVisible(!selectedGroup.isVisible());
            CatgisDesktopApp.markProjectDirty();
            panel.refreshLayerList();
            AppContext.mapPanel().repaint();
        });
        popupMenu.add(toggleVisibilityItem);

        JMenuItem expandItem = panel.createMenuItem(
                selectedGroup.isExpanded() ? "Contraer grupo" : "Expandir grupo",
                selectedGroup.isExpanded() ? AppIcons.downIcon() : AppIcons.openIcon()
        );
        expandItem.addActionListener(ev -> {
            selectedGroup.setExpanded(!selectedGroup.isExpanded());
            CatgisDesktopApp.markProjectDirty();
            panel.refreshLayerList();
        });
        popupMenu.add(expandItem);

        JMenuItem renameItem = panel.createMenuItem("Renombrar grupo", AppIcons.renameIcon());
        renameItem.addActionListener(ev -> panel.renameGroup(selectedGroup));
        popupMenu.add(renameItem);

        JMenuItem moveUpItem = panel.createMenuItem("Subir grupo", AppIcons.upIcon());
        moveUpItem.addActionListener(ev -> panel.moveGroup(selectedGroup, -1));
        popupMenu.add(moveUpItem);

        JMenuItem moveDownItem = panel.createMenuItem("Bajar grupo", AppIcons.downIcon());
        moveDownItem.addActionListener(ev -> panel.moveGroup(selectedGroup, 1));
        popupMenu.add(moveDownItem);

        popupMenu.addSeparator();

        JMenuItem removeItem = panel.createMenuItem("Quitar grupo (mantener capas)", AppIcons.removeIcon());
        removeItem.addActionListener(ev -> panel.removeGroup(selectedGroup));
        popupMenu.add(removeItem);

        popupMenu.show(panel.layerList, e.getX(), e.getY());
    }

    public void showEmptyAreaPopup(MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem newGroupItem = panel.createMenuItem("Nuevo grupo", AppIcons.openIcon());
        newGroupItem.addActionListener(ev -> panel.createNewGroupFromSelection());
        popupMenu.add(newGroupItem);
        popupMenu.show(panel.layerList, e.getX(), e.getY());
    }

    public JPopupMenu buildVectorPopup(Layer selectedLayer) {
        JPopupMenu popupMenu = new JPopupMenu();
        boolean readOnlyVector = VectorLayerUtils.isReadOnlyVectorLayer(selectedLayer);

        JMenuItem propertiesItem = panel.createMenuItem(selectedLayer.getName(), AppIcons.imageryIcon());
        propertiesItem.setEnabled(false);
        popupMenu.add(propertiesItem);
        popupMenu.addSeparator();

        addCommonTopItems(popupMenu, selectedLayer, "Ocultar capa", "Mostrar capa");

        JMenuItem displayItem = panel.createMenuItem("Ajustes de visualizacion...", AppIcons.attrEditIcon());
        displayItem.addActionListener(ev -> panel.openVectorDisplaySettings(selectedLayer));
        popupMenu.add(displayItem);

        JMenuItem editItem = panel.createMenuItem(readOnlyVector ? "Capa en solo lectura" : "Editar vector", AppIcons.attrEditIcon());
        editItem.setEnabled(!readOnlyVector);
        editItem.addActionListener(ev -> panel.openVectorEditing(selectedLayer));
        popupMenu.add(editItem);

        JMenuItem copyToEditingItem = panel.createMenuItem("Copiar seleccionadas a capa en edicion", AppIcons.attrCopyIcon());
        boolean canCopyToEditing = AppContext.mapPanel() != null
                && AppContext.mapPanel().canCopySelectedFeaturesFromLayerToEditingLayer(selectedLayer);
        copyToEditingItem.setEnabled(canCopyToEditing);
        copyToEditingItem.addActionListener(ev -> {
            if (AppContext.mapPanel() != null
                    && AppContext.mapPanel().copySelectedFeaturesFromLayerToEditingLayer(selectedLayer)) {
                panel.refreshLayerList();
                AppContext.mapPanel().repaint();
            }
        });
        popupMenu.add(copyToEditingItem);

        JMenu simbologiaMenu = new JMenu("Simbologia");
        simbologiaMenu.setIcon(AppIcons.propertiesIcon());

        JMenuItem styleItem = panel.createMenuItem("Cambiar estilo...", AppIcons.propertiesIcon());
        styleItem.addActionListener(ev -> LayerPropertiesDialog.open(selectedLayer));
        simbologiaMenu.add(styleItem);

        JMenuItem labelsItem = panel.createMenuItem("Etiquetas...", AppIcons.labelsIcon());
        labelsItem.addActionListener(ev -> LayerPropertiesDialog.open(selectedLayer));
        simbologiaMenu.add(labelsItem);

        JMenuItem clearLabelsItem = panel.createMenuItem("Quitar etiquetas", AppIcons.labelsIcon());
        clearLabelsItem.addActionListener(ev -> {
            selectedLayer.setLabelsVisible(false);
            selectedLayer.setLabelField(null);
            panel.refreshLayerList();
            AppContext.mapPanel().repaint();
        });
        simbologiaMenu.add(clearLabelsItem);

        popupMenu.add(simbologiaMenu);

        // Heatmap toggle (point layers only)
        JCheckBoxMenuItem heatmapItem = new JCheckBoxMenuItem("Mapa de calor (heatmap)", selectedLayer.isHeatmapEnabled());
        heatmapItem.addActionListener(ev -> {
            selectedLayer.setHeatmapEnabled(!selectedLayer.isHeatmapEnabled());
            AppContext.mapPanel().repaint();
        });
        popupMenu.add(heatmapItem);

        JCheckBoxMenuItem clusterItem = new JCheckBoxMenuItem("Agrupar puntos (clustering)", selectedLayer.isClusteringEnabled());
        clusterItem.addActionListener(ev -> {
            selectedLayer.setClusteringEnabled(!selectedLayer.isClusteringEnabled());
            AppContext.mapPanel().repaint();
        });
        popupMenu.add(clusterItem);

        // Environmental area marking
        JMenuItem envMarkItem = panel.createMenuItem("Marcar como área de influencia...", AppIcons.propertiesIcon());
        envMarkItem.addActionListener(ev -> {
            ar.com.catgis.climate.EnvironmentalAreaMarker.markSingleLayer(
                SwingUtilities.getWindowAncestor(panel), selectedLayer);
            ar.com.catgis.climate.EnvironmentalAreaMarker.showMarkDialog(
                SwingUtilities.getWindowAncestor(panel));
        });
        popupMenu.add(envMarkItem);
        popupMenu.addSeparator();

        JMenu advancedMenu = new JMenu("Configuracion avanzada");
        advancedMenu.setIcon(AppIcons.crsIcon());

        JMenuItem propertiesRealItem = panel.createMenuItem("Propiedades", AppIcons.propertiesIcon());
        propertiesRealItem.addActionListener(ev -> LayerPropertiesDialog.open(selectedLayer));
        advancedMenu.add(propertiesRealItem);

        JMenuItem viewCRSItem = panel.createMenuItem("Ver CRS de capa", AppIcons.crsIcon());
        viewCRSItem.addActionListener(ev -> panel.showLayerCRS(selectedLayer));
        advancedMenu.add(viewCRSItem);

        JMenuItem setCRSItem = panel.createMenuItem("Definir CRS de capa", AppIcons.crsIcon());
        setCRSItem.addActionListener(ev -> panel.defineLayerCRS(selectedLayer));
        advancedMenu.add(setCRSItem);

        if (CadLayerSupport.isCadLayer(selectedLayer)) {
            JMenuItem cadGeorefItem = panel.createMenuItem("Georreferenciar CAD por puntos...", AppIcons.crsIcon());
            cadGeorefItem.addActionListener(ev -> panel.georeferenceCadLayer(selectedLayer));
            advancedMenu.add(cadGeorefItem);

            JMenuItem cadPlacementItem = panel.createMenuItem("Ajuste CAD...", AppIcons.moveFeatureIcon());
            cadPlacementItem.addActionListener(ev -> panel.editCadPlacement(selectedLayer));
            advancedMenu.add(cadPlacementItem);

            JMenuItem cadDragPlacementItem = panel.createMenuItem("Arrastrar CAD en mapa...", AppIcons.moveFeatureIcon());
            cadDragPlacementItem.addActionListener(ev -> CadWorkflowSupport.openCadDragPlacementWorkflow(panel, selectedLayer));
            advancedMenu.add(cadDragPlacementItem);

            JMenuItem cadInternalLayersItem = panel.createMenuItem("Capas internas CAD...", AppIcons.tableIcon());
            cadInternalLayersItem.addActionListener(ev -> CadWorkflowSupport.openCadInternalLayers(panel, selectedLayer));
            advancedMenu.add(cadInternalLayersItem);

            JMenuItem cadDiagItem = panel.createMenuItem("Diagnostico DWG/CAD...", AppIcons.propertiesIcon());
            cadDiagItem.addActionListener(ev -> CadIntegrationDialog.open());
            advancedMenu.add(cadDiagItem);

            JMenuItem exportCadAdjustedItem = panel.createMenuItem("Exportar CAD georreferenciado...", AppIcons.exportIcon());
            exportCadAdjustedItem.addActionListener(ev -> ExportVectorLayerAction.exportLayer(selectedLayer));
            advancedMenu.add(exportCadAdjustedItem);
        }

        JMenuItem exportItem = panel.createMenuItem("Exportar capa", AppIcons.exportIcon());
        exportItem.addActionListener(ev -> ExportVectorLayerAction.exportLayer(selectedLayer));
        advancedMenu.add(exportItem);

        if (selectedLayer != null && "VECTOR".equalsIgnoreCase(selectedLayer.getType())) {
            JMenuItem exportDxfItem = panel.createMenuItem("Exportar a DXF...", AppIcons.exportIcon());
            exportDxfItem.addActionListener(ev -> DxfExportEngine.exportLayerWithDialog(null, selectedLayer));
            advancedMenu.add(exportDxfItem);
        }

        JMenuItem sendPostgisItem = panel.createMenuItem("Enviar a CATSERVER...", AppIcons.tableIcon());
        sendPostgisItem.addActionListener(ev -> PostgisDataSourceAction.exportLayerToPostgis(selectedLayer));
        advancedMenu.add(sendPostgisItem);

        if (selectedLayer instanceof OnlineWfsLayer) {
            JMenuItem infoItem = panel.createMenuItem("Informacion del servicio...", AppIcons.propertiesIcon());
            infoItem.addActionListener(ev -> panel.showOnlineWfsInfo((OnlineWfsLayer) selectedLayer));
            advancedMenu.add(infoItem);
        } else if (selectedLayer instanceof PostgisLayer) {
            if (!readOnlyVector) {
                JMenuItem savePostgisItem = panel.createMenuItem("Guardar cambios en CATSERVER", AppIcons.saveIcon());
                savePostgisItem.addActionListener(ev -> PostgisDataSourceAction.savePostgisLayerChanges((PostgisLayer) selectedLayer));
                advancedMenu.add(savePostgisItem);
            }
            JMenuItem infoItem = panel.createMenuItem("Informacion de la conexion...", AppIcons.propertiesIcon());
            infoItem.addActionListener(ev -> panel.showPostgisInfo((PostgisLayer) selectedLayer));
            advancedMenu.add(infoItem);
        } else if (selectedLayer instanceof GeoPackageLayer) {
            JMenuItem infoItem = panel.createMenuItem("Informacion GeoPackage...", AppIcons.propertiesIcon());
            infoItem.addActionListener(ev -> panel.showGeoPackageInfo((GeoPackageLayer) selectedLayer));
            advancedMenu.add(infoItem);
        }

        JMenuItem renameItem = panel.createMenuItem("Renombrar", AppIcons.renameIcon());
        renameItem.addActionListener(ev -> panel.renameLayer(selectedLayer));
        advancedMenu.add(renameItem);

        popupMenu.add(advancedMenu);
        popupMenu.addSeparator();

        JMenuItem fieldsItem = panel.createMenuItem("Ver/Editar campos...", AppIcons.fieldsIcon());
        fieldsItem.addActionListener(ev -> OpenAttributeTableAction.openFieldsConfig(selectedLayer));
        popupMenu.add(fieldsItem);

        JMenuItem attributeTableItem = panel.createMenuItem("Ver/Editar atributos...", AppIcons.tableIcon());
        attributeTableItem.addActionListener(ev -> OpenAttributeTableAction.openTable(selectedLayer));
        popupMenu.add(attributeTableItem);

        JMenuItem queryBuilderItem = panel.createMenuItem("Constructor de consultas...", AppIcons.identifyIcon());
        queryBuilderItem.addActionListener(ev -> QueryBuilderDialog.open(selectedLayer));
        popupMenu.add(queryBuilderItem);

        popupMenu.addSeparator();
        addCommonBottomItems(popupMenu, selectedLayer);
        return popupMenu;
    }

    public JPopupMenu buildRasterPopup(Layer selectedLayer) {
        if (selectedLayer instanceof OnlineTileLayer || selectedLayer instanceof OnlineWmsLayer) {
            return buildOnlineRasterPopup(selectedLayer);
        }

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem propertiesItem = panel.createMenuItem(selectedLayer.getName(), AppIcons.propertiesIcon());
        propertiesItem.setEnabled(false);
        popupMenu.add(propertiesItem);
        popupMenu.addSeparator();

        addCommonTopItems(popupMenu, selectedLayer, "Ocultar raster", "Mostrar raster");

        JMenuItem rasterInfoItem = panel.createMenuItem("Informacion raster...", AppIcons.identifyIcon());
        rasterInfoItem.addActionListener(ev -> panel.showRasterInfo(selectedLayer));
        popupMenu.add(rasterInfoItem);

        if (selectedLayer instanceof RasterLayer rasterLayer && panel.hasProRasterMetadata(rasterLayer)) {
            JMenuItem proInfoItem = panel.createMenuItem("Informacion raster avanzada...", AppIcons.imageryIcon());
            proInfoItem.addActionListener(ev -> panel.showProRasterInfo(rasterLayer));
            popupMenu.add(proInfoItem);

            JMenuItem exportProReportItem = panel.createMenuItem("Exportar ficha raster...", AppIcons.exportIcon());
            exportProReportItem.addActionListener(ev -> panel.exportProRasterReport(rasterLayer));
            popupMenu.add(exportProReportItem);
        }

        JMenuItem displayItem = panel.createMenuItem("Ajustes de visualizacion...", AppIcons.attrEditIcon());
        displayItem.addActionListener(ev -> panel.openRasterDisplaySettings(selectedLayer));
        popupMenu.add(displayItem);

        boolean derivedRaster = panel.isDerivedRasterLayer(selectedLayer);
        if (selectedLayer instanceof RasterLayer rasterLayer && panel.hasProRasterMetadata(rasterLayer) && !derivedRaster) {
            JMenu proOutputsMenu = new JMenu("Salidas raster avanzadas");
            proOutputsMenu.setIcon(AppIcons.imageryIcon());

            JMenuItem thematicItem = panel.createMenuItem("Generar mapa tematico raster", AppIcons.imageryIcon());
            thematicItem.setEnabled(ProRasterDerivedService.supportsThematicOutput(rasterLayer));
            thematicItem.addActionListener(ev -> panel.generateProDerivedRaster(rasterLayer, false));
            proOutputsMenu.add(thematicItem);

            JMenuItem qaItem = panel.createMenuItem(ProRasterDerivedService.qaMenuLabel(rasterLayer), AppIcons.propertiesIcon());
            qaItem.addActionListener(ev -> panel.generateProDerivedRaster(rasterLayer, true));
            proOutputsMenu.add(qaItem);

            if (ProRasterDerivedService.supportsLandsatQaPixelMasks(rasterLayer)) {
                JMenu landsatMasksMenu = new JMenu("Mascaras Landsat QA_PIXEL");
                landsatMasksMenu.setIcon(AppIcons.propertiesIcon());

                JMenuItem cloudsMaskItem = panel.createMenuItem(
                        ProRasterDerivedService.landsatQaMaskMenuLabel(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_CLOUDS),
                        AppIcons.propertiesIcon()
                );
                cloudsMaskItem.addActionListener(ev -> panel.generateProDerivedRaster(rasterLayer, ProRasterDerivedService.OP_PRO_MASK_LANDSAT_CLOUDS));
                landsatMasksMenu.add(cloudsMaskItem);

                JMenuItem shadowMaskItem = panel.createMenuItem(
                        ProRasterDerivedService.landsatQaMaskMenuLabel(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SHADOW),
                        AppIcons.propertiesIcon()
                );
                shadowMaskItem.addActionListener(ev -> panel.generateProDerivedRaster(rasterLayer, ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SHADOW));
                landsatMasksMenu.add(shadowMaskItem);

                JMenuItem snowMaskItem = panel.createMenuItem(
                        ProRasterDerivedService.landsatQaMaskMenuLabel(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SNOW),
                        AppIcons.propertiesIcon()
                );
                snowMaskItem.addActionListener(ev -> panel.generateProDerivedRaster(rasterLayer, ProRasterDerivedService.OP_PRO_MASK_LANDSAT_SNOW));
                landsatMasksMenu.add(snowMaskItem);

                JMenuItem waterMaskItem = panel.createMenuItem(
                        ProRasterDerivedService.landsatQaMaskMenuLabel(ProRasterDerivedService.OP_PRO_MASK_LANDSAT_WATER),
                        AppIcons.propertiesIcon()
                );
                waterMaskItem.addActionListener(ev -> panel.generateProDerivedRaster(rasterLayer, ProRasterDerivedService.OP_PRO_MASK_LANDSAT_WATER));
                landsatMasksMenu.add(waterMaskItem);

                proOutputsMenu.add(landsatMasksMenu);
            }

            JMenuItem compareItem = panel.createMenuItem("Comparar con otra fecha...", AppIcons.attrRefreshIcon());
            compareItem.addActionListener(ev -> panel.compareProRasterWithAnotherDate(rasterLayer));
            compareItem.setEnabled(panel.findComparableProLayers(rasterLayer).size() > 0);
            proOutputsMenu.add(compareItem);

            popupMenu.add(proOutputsMenu);
        }

        String currentMode = panel.getRasterMode(selectedLayer);
        JMenuItem currentModeItem = panel.createMenuItem("Modo actual: " + panel.getRasterModeLabel(currentMode), AppIcons.zoomLayerIcon());
        currentModeItem.setEnabled(false);
        popupMenu.add(currentModeItem);

        JMenuItem quickItem = panel.createMenuItem("Vista rapida", AppIcons.zoomOutIcon());
        quickItem.setEnabled(!derivedRaster && !RasterImageLoader.MODE_PREVIEW.equalsIgnoreCase(currentMode));
        quickItem.addActionListener(ev -> panel.reloadRasterMode(selectedLayer, RasterImageLoader.MODE_PREVIEW));
        popupMenu.add(quickItem);

        JMenuItem virtualItem = panel.createMenuItem("Zoom virtual", AppIcons.zoomLayerIcon());
        virtualItem.setEnabled(!derivedRaster && !RasterImageLoader.MODE_VIRTUAL.equalsIgnoreCase(currentMode));
        virtualItem.addActionListener(ev -> panel.reloadRasterMode(selectedLayer, RasterImageLoader.MODE_VIRTUAL));
        popupMenu.add(virtualItem);

        JMenuItem realItem = panel.createMenuItem("Zoom real", AppIcons.zoomInIcon());
        realItem.setEnabled(!derivedRaster && !RasterImageLoader.MODE_REAL.equalsIgnoreCase(currentMode));
        realItem.addActionListener(ev -> panel.reloadRasterMode(selectedLayer, RasterImageLoader.MODE_REAL));
        popupMenu.add(realItem);

        JMenuItem contourItem = panel.createMenuItem(I18n.t("Generar curvas de nivel..."), AppIcons.propertiesIcon());
        contourItem.addActionListener(ev -> ContourGenerationDialog.open(selectedLayer));
        popupMenu.add(contourItem);

        JMenuItem drainageItem = panel.createMenuItem(I18n.t("Generar escorrentias..."), AppIcons.drainageIcon());
        drainageItem.addActionListener(ev -> DrainageExtractionDialog.open(selectedLayer));
        popupMenu.add(drainageItem);

        JMenuItem terrainAnalysisItem = panel.createMenuItem(I18n.t("Analisis topohidrologico..."), AppIcons.terrainAnalysisIcon());
        terrainAnalysisItem.addActionListener(ev -> TerrainHydrologyAnalysisDialog.open(selectedLayer));
        popupMenu.add(terrainAnalysisItem);

        JMenuItem basinOutletItem = panel.createMenuItem(I18n.t("Cuenca desde outlet..."), AppIcons.pointIcon());
        basinOutletItem.addActionListener(ev -> BasinFromOutletDialog.open(selectedLayer));
        popupMenu.add(basinOutletItem);

        popupMenu.addSeparator();

        // Climate visualization
        JMenu climateMenu = new JMenu("Clima y ambiente");
        climateMenu.setIcon(AppIcons.imageryIcon());

        JMenuItem climateSymbologyItem = panel.createMenuItem("Aplicar simbología climática...", AppIcons.attrEditIcon());
        climateSymbologyItem.setEnabled(selectedLayer instanceof RasterLayer);
        climateSymbologyItem.addActionListener(ev -> {
            if (selectedLayer instanceof RasterLayer rl) {
                ar.com.catgis.climate.ClimateVisualizationDialog.open(
                    SwingUtilities.getWindowAncestor(panel), rl);
            }
        });
        climateMenu.add(climateSymbologyItem);

        JMenuItem areaAnalysisItem = panel.createMenuItem("Análisis climático por áreas (AID/AII)...", AppIcons.propertiesIcon());
        areaAnalysisItem.addActionListener(ev -> {
            ar.com.catgis.climate.ClimateAreaAnalysisDialog.open(
                SwingUtilities.getWindowAncestor(panel));
        });
        climateMenu.add(areaAnalysisItem);

        JMenuItem windRoseItem = panel.createMenuItem("Rosa de los vientos...", AppIcons.attrRefreshIcon());
        windRoseItem.addActionListener(ev -> {
            ar.com.catgis.climate.WindRoseDialog.open(
                SwingUtilities.getWindowAncestor(panel));
        });
        climateMenu.add(windRoseItem);

        popupMenu.add(climateMenu);
        popupMenu.addSeparator();

        JMenu advancedMenu = new JMenu("Configuracion avanzada");
        advancedMenu.setIcon(AppIcons.toolboxIcon());

        JMenuItem viewCRSItem = panel.createMenuItem("Ver CRS de capa", AppIcons.crsIcon());
        viewCRSItem.addActionListener(ev -> panel.showLayerCRS(selectedLayer));
        advancedMenu.add(viewCRSItem);

        JMenuItem setCRSItem = panel.createMenuItem("Definir CRS de capa", AppIcons.crsIcon());
        setCRSItem.addActionListener(ev -> panel.defineLayerCRS(selectedLayer));
        advancedMenu.add(setCRSItem);

        JMenuItem renameItem = panel.createMenuItem("Renombrar", AppIcons.renameIcon());
        renameItem.addActionListener(ev -> panel.renameLayer(selectedLayer));
        advancedMenu.add(renameItem);

        popupMenu.add(advancedMenu);
        popupMenu.addSeparator();
        addCommonBottomItems(popupMenu, selectedLayer);
        return popupMenu;
    }

    public JPopupMenu buildOnlineRasterPopup(Layer selectedLayer) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem propertiesItem = panel.createMenuItem(selectedLayer.getName(), AppIcons.imageryIcon());
        propertiesItem.setEnabled(false);
        popupMenu.add(propertiesItem);
        popupMenu.addSeparator();

        addCommonTopItems(popupMenu, selectedLayer, "Ocultar capa online", "Mostrar capa online");

        JMenuItem infoItem = panel.createMenuItem("Informacion de servicio...", AppIcons.identifyIcon());
        infoItem.addActionListener(ev -> panel.showRasterInfo(selectedLayer));
        popupMenu.add(infoItem);

        JMenu advancedMenu = new JMenu("Configuracion");
        advancedMenu.setIcon(AppIcons.toolboxIcon());

        JMenuItem renameItem = panel.createMenuItem("Renombrar", AppIcons.renameIcon());
        renameItem.addActionListener(ev -> panel.renameLayer(selectedLayer));
        advancedMenu.add(renameItem);

        popupMenu.add(advancedMenu);
        popupMenu.addSeparator();
        addCommonBottomItems(popupMenu, selectedLayer);
        return popupMenu;
    }

    private void addCommonTopItems(JPopupMenu popupMenu, Layer selectedLayer, String hideText, String showText) {
        JMenuItem zoomToLayerItem = panel.createMenuItem("Zoom a la capa", AppIcons.zoomLayerIcon());
        zoomToLayerItem.addActionListener(ev -> AppContext.mapPanel().zoomToLayer(selectedLayer));
        popupMenu.add(zoomToLayerItem);

        JMenuItem toggleVisibilityItem = panel.createMenuItem(
                selectedLayer.isVisible() ? hideText : showText,
                selectedLayer.isVisible() ? AppIcons.hiddenIcon() : AppIcons.visibleIcon()
        );
        toggleVisibilityItem.addActionListener(ev -> {
            selectedLayer.setVisible(!selectedLayer.isVisible());
            CatgisDesktopApp.markProjectDirty();
            panel.refreshLayerList();
            AppContext.mapPanel().repaint();
        });
        popupMenu.add(toggleVisibilityItem);
    }

    private void addCommonBottomItems(JPopupMenu popupMenu, Layer selectedLayer) {
        List<Layer> popupTargetLayers = panel.resolvePopupTargetLayers(selectedLayer);
        boolean multipleSelection = popupTargetLayers.size() > 1;

        JMenuItem newGroupItem = panel.createMenuItem(
                multipleSelection ? I18n.format("Crear grupo con seleccion ({0})", popupTargetLayers.size()) : "Crear grupo con capa",
                AppIcons.openIcon()
        );
        newGroupItem.addActionListener(ev -> panel.createNewGroupForLayers(popupTargetLayers));
        popupMenu.add(newGroupItem);

        JMenu moveToGroupMenu = new JMenu("Mover a grupo");
        moveToGroupMenu.setIcon(AppIcons.openIcon());
        panel.populateMoveToGroupMenu(moveToGroupMenu, popupTargetLayers);
        popupMenu.add(moveToGroupMenu);

        JMenuItem ungroupItem = panel.createMenuItem("Sacar del grupo", AppIcons.removeIcon());
        ungroupItem.setEnabled(popupTargetLayers.stream().anyMatch(Layer::isInGroup));
        ungroupItem.addActionListener(ev -> panel.removeLayersFromGroup(popupTargetLayers));
        popupMenu.add(ungroupItem);

        popupMenu.addSeparator();

        JMenuItem moveUpItem = panel.createMenuItem("Subir", AppIcons.upIcon());
        moveUpItem.addActionListener(ev -> panel.moveLayerUp(selectedLayer));
        moveUpItem.setEnabled(!multipleSelection);
        popupMenu.add(moveUpItem);

        JMenuItem moveDownItem = panel.createMenuItem("Bajar", AppIcons.downIcon());
        moveDownItem.addActionListener(ev -> panel.moveLayerDown(selectedLayer));
        moveDownItem.setEnabled(!multipleSelection);
        popupMenu.add(moveDownItem);

        String removeLabel = multipleSelection
                ? I18n.format("Quitar seleccionadas ({0})", popupTargetLayers.size())
                : I18n.t("Quitar");
        JMenuItem removeItem = panel.createMenuItem(removeLabel, AppIcons.removeIcon());
        removeItem.addActionListener(ev -> panel.removeLayersFromProject(popupTargetLayers));
        popupMenu.add(removeItem);
    }
}
