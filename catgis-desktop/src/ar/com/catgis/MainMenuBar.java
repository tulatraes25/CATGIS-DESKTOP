package ar.com.catgis;

import ar.com.catgis.TopologyValidator;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import java.io.File;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class MainMenuBar extends JMenuBar {

    public MainMenuBar() {
        JMenu menuArchivo = new JMenu(I18n.t("Archivo"));
        JMenu menuVer = new JMenu(I18n.t("Ver"));
        JMenu menuDatos = new JMenu(I18n.t("Datos"));
        JMenu menuEdicion = new JMenu(I18n.t("Edicion"));
        JMenu menuAnalisis = new JMenu(I18n.t("Analisis"));
        JMenu menuSalida = new JMenu(I18n.t("Salida"));
        JMenu menuHerramientas = new JMenu(I18n.t("Herramientas"));
        JMenu menuAyuda = new JMenu(I18n.t("Ayuda"));
        JMenu menuCad = new JMenu("CAD");
        JMenu menuModulos = new ModulesMenu();
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenuItem itemNuevoProyecto = createItem("Nuevo proyecto", createNewProjectIcon());
        itemNuevoProyecto.addActionListener(e -> NewProjectAction.newProject());

        JMenuItem itemAbrirProyecto = createItem("Abrir proyecto", createOpenProjectIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, menuMask));
        itemAbrirProyecto.addActionListener(e -> LoadProjectAction.loadProject());

        JMenuItem itemAgregarCapa = createItem("Agregar capa", createOpenLayerIcon());
        itemAgregarCapa.addActionListener(e -> AddLayerAction.openLayer());

        JMenuItem itemDemOnline = createItem("DEM online...", AppIcons.propertiesIcon());
        itemDemOnline.addActionListener(e -> OnlineDemDownloadDialog.open());

        JMenuItem itemCargarDem = createItem("Cargar datos DEM...", AppIcons.openIcon());
        itemCargarDem.addActionListener(e -> DemLocalLoadAction.openDialog());

        JMenuItem itemEscorrentias = createItem("Generar escorrentias...", AppIcons.drainageIcon());
        itemEscorrentias.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            DrainageExtractionDialog.open();
        });

        JMenuItem itemAnalisisHidro = createItem("Analisis topohidrologico...", AppIcons.terrainAnalysisIcon());
        itemAnalisisHidro.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            TerrainHydrologyAnalysisDialog.open();
        });

        JMenuItem itemCuencaOutlet = createItem("Cuenca desde outlet...", AppIcons.pointIcon());
        itemCuencaOutlet.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            BasinFromOutletDialog.open();
        });

        JMenuItem itemNuevaCapaVectorial = createItem("Nueva capa vectorial", createNewVectorLayerIcon());
        itemNuevaCapaVectorial.addActionListener(e -> NewVectorLayerAction.createNewVectorLayer(null, CatgisDesktopApp.getMainFrameSafe()));

        JMenuItem itemAbrirTabla = createItem("Cargar tabla externa", AppIcons.importTableIcon());
        itemAbrirTabla.addActionListener(e -> OpenTablePointsAction.openTablePoints());

        JMenuItem itemCatserver = createItem("Conectar CATSERVER...", AppIcons.projectIcon());
        itemCatserver.setToolTipText(I18n.t("Conectar CATSERVER a cualquier servidor PostgreSQL/PostGIS."));
        itemCatserver.addActionListener(e -> PostgisDataSourceAction.openCatserverBrowser());

        JMenuItem itemGuardarProyecto = createItem("Guardar proyecto", AppIcons.saveIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask));
        itemGuardarProyecto.addActionListener(e -> SaveProjectAction.saveProject());

        JMenuItem itemGuardarProyectoComo = createItem("Guardar proyecto como...", AppIcons.attrCopyIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemGuardarProyectoComo.addActionListener(e -> SaveProjectAction.saveProjectAs());

        JMenuItem itemSalvarVista = createItem("Salvar vista del mapa", createCameraIcon());
        itemSalvarVista.addActionListener(e -> SaveMapViewAction.saveCurrentView());

        JMenuItem itemOsm = createItem("OpenStreetMap", AppIcons.basemapIcon());
        itemOsm.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_OSM));

        JMenuItem itemEsri = createItem("Esri World Imagery", AppIcons.imageryIcon());
        itemEsri.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_WORLD_IMAGERY));

        JMenuItem itemWfs = createItem("Agregar WFS...", AppIcons.tableIcon());
        itemWfs.addActionListener(e -> AddWfsAction.openDialog());

        JMenuItem itemSuelosOnline = createItem("Suelos online...", AppIcons.basemapIcon());
        itemSuelosOnline.addActionListener(e -> OnlineSoilDownloadDialog.open());

        JMenuItem itemClimaOnline = createItem("Clima online...", AppIcons.propertiesIcon());
        itemClimaOnline.addActionListener(e -> ar.com.catgis.climate.ClimateOnlineDownloadDialog.open());

        JMenuItem itemRecortarDem = createItem("Recortar DEM...", AppIcons.cutIcon());
        itemRecortarDem.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            DemClipDialog.open();
        });

        JMenuItem itemCurvasNivel = createItem("Curvas de nivel...", AppIcons.lineIcon());
        itemCurvasNivel.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            ContourGenerationDialog.open();
        });

        JMenuItem itemInundacion = createItem("Inundacion preliminar...", AppIcons.areaIcon());
        itemInundacion.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            FloodScenarioDialog.open();
        });

        JMenuItem itemRiesgoBooleano = createItem("Riesgo booleano preliminar...", AppIcons.terrainAnalysisIcon());
        itemRiesgoBooleano.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().size() < 2) {
                JOptionPane.showMessageDialog(
                        CatgisDesktopApp.getMainFrameSafe(),
                        I18n.t("Necesitas al menos un DEM y un raster de suelos cargados para generar riesgo booleano preliminar.")
                );
                return;
            }
            BooleanRiskDialog.open();
        });

        JMenuItem itemPerfilTopografico = createItem("Perfil topografico...", AppIcons.distanceIcon());
        itemPerfilTopografico.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            TopographicProfileDialog.open();
        });

        menuArchivo.add(itemNuevoProyecto);
        menuArchivo.add(itemAbrirProyecto);
        // Recent files submenu
        JMenu recentMenu = new JMenu("Proyectos recientes");
        loadRecentFiles(recentMenu);
        menuArchivo.add(recentMenu);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardarProyecto);
        menuArchivo.add(itemGuardarProyectoComo);
        menuArchivo.add(itemSalvarVista);

        JMenuItem itemCompositorCartografico = createItem("Abrir CATMAP...", AppIcons.projectIcon());
        itemCompositorCartografico.addActionListener(e -> MapLayoutComposerDialog.open());

        JMenuItem itemSimbologiaCapa = createItem("Simbologia de capa seleccionada...", AppIcons.propertiesIcon());
        itemSimbologiaCapa.addActionListener(e -> {
            Layer layer = CatgisDesktopApp.layersPanel != null ? CatgisDesktopApp.layersPanel.getSelectedLayer() : null;
            if (layer == null) {
                JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), I18n.t("Selecciona una capa para editar su simbologia."));
                return;
            }
            LayerPropertiesDialog.open(layer);
        });

        JMenuItem itemTematicaCampo = createItem("Simbologia por campo...", AppIcons.propertiesIcon());
        itemTematicaCampo.addActionListener(e -> {
            Layer layer = CatgisDesktopApp.layersPanel != null ? CatgisDesktopApp.layersPanel.getSelectedLayer() : null;
            if (layer == null) {
                JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), I18n.t("Selecciona una capa vectorial."));
                return;
            }
            CategorizedSymbologyDialog.open(layer);
        });

        menuDatos.add(itemAgregarCapa);
        menuDatos.add(itemNuevaCapaVectorial);
        menuDatos.add(itemAbrirTabla);
        menuDatos.addSeparator();
        menuDatos.add(itemCatserver);
        menuDatos.add(itemWfs);
        menuDatos.addSeparator();
        menuDatos.add(itemOsm);
        menuDatos.add(itemEsri);
        menuDatos.addSeparator();
        menuDatos.add(itemDemOnline);
        menuDatos.add(itemCargarDem);
        menuDatos.add(itemSuelosOnline);
        menuDatos.add(itemClimaOnline);

        menuAnalisis.add(itemRecortarDem);
        menuAnalisis.add(itemCurvasNivel);
        menuAnalisis.add(itemEscorrentias);
        menuAnalisis.add(itemAnalisisHidro);
        menuAnalisis.add(itemCuencaOutlet);
        menuAnalisis.add(itemInundacion);
        menuAnalisis.add(itemRiesgoBooleano);
        menuAnalisis.add(itemPerfilTopografico);
        menuAnalisis.addSeparator();
        // Topology validation
        JMenuItem itemTopologyNoGaps = createItem("Validar gaps entre polígonos", null);
        itemTopologyNoGaps.addActionListener(e -> runTopologyValidation("NO_GAPS"));
        menuAnalisis.add(itemTopologyNoGaps);
        JMenuItem itemTopologyNoOverlaps = createItem("Validar superposiciones", null);
        itemTopologyNoOverlaps.addActionListener(e -> runTopologyValidation("NO_OVERLAPS"));
        menuAnalisis.add(itemTopologyNoOverlaps);
        JMenuItem itemTopologyNoSelfInt = createItem("Validar geometrías inválidas", null);
        itemTopologyNoSelfInt.addActionListener(e -> runTopologyValidation("NO_SELF_INTERSECTION"));
        menuAnalisis.add(itemTopologyNoSelfInt);
        JMenuItem itemTopologyLineConn = createItem("Validar conectividad de líneas", null);
        itemTopologyLineConn.addActionListener(e -> runTopologyValidation("NO_DANGLES"));
        menuAnalisis.add(itemTopologyLineConn);
        menuAnalisis.addSeparator();

        JMenuItem itemGeoAnalyzer = createItem("Analizador GEO (un clic)...", AppIcons.terrainAnalysisIcon());
        itemGeoAnalyzer.addActionListener(e ->
            ar.com.catgis.climate.UnifiedAnalysisDialog.open(MainMenuBar.this));
        menuAnalisis.add(itemGeoAnalyzer);

        menuSalida.add(itemCompositorCartografico);
        menuSalida.add(itemSimbologiaCapa);
        menuSalida.add(itemTematicaCampo);

        JMenuItem itemCortar = createItem("Cortar selección", AppIcons.cutIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_X, menuMask));
        itemCortar.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.cutSelectedFeatures();
            }
        });

        JMenuItem itemCopiar = createItem("Copiar selección", AppIcons.attrCopyIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_C, menuMask));
        itemCopiar.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.copySelectedFeatures();
            }
        });

        JMenuItem itemPegar = createItem("Pegar en capa editable", AppIcons.openIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_V, menuMask));
        itemPegar.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                Layer layer = getPreferredVectorLayer();
                if (layer != null) {
                    CatgisDesktopApp.mapPanel.prepareLayerForEditing(layer);
                }
                CatgisDesktopApp.mapPanel.pasteCopiedFeatures();
            }
        });

        JMenuItem itemCopiarACapaEditable = createItem("Copiar selección a capa editable", AppIcons.attrAssignIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_V, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemCopiarACapaEditable.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                Layer layer = getPreferredVectorLayer();
                if (layer != null) {
                    CatgisDesktopApp.mapPanel.prepareLayerForEditing(layer);
                }
                CatgisDesktopApp.mapPanel.copySelectedFeaturesToEditingLayer();
            }
        });

        JMenuItem itemBorrar = createItem("Borrar selección", AppIcons.removeIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        itemBorrar.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.deleteSelectedFeatures();
            }
        });

        JMenuItem itemDeshacer = createItem("Deshacer", AppIcons.undoIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuMask));
        itemDeshacer.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.undoFeatureEdit();
            }
        });

        JMenuItem itemRehacer = createItem("Rehacer", AppIcons.redoIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, menuMask));
        itemRehacer.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.redoFeatureEdit();
            }
        });

        JMenuItem itemMoverSeleccion = createItem("Mover selección", AppIcons.moveFeatureIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_M, menuMask));
        itemMoverSeleccion.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateMoveFeatureMode();
            }
        });

        JMenuItem itemCortarGeometria = createItem("Cortar geometría", AppIcons.cutIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_K, menuMask));
        itemCortarGeometria.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateCutFeatureMode();
            }
        });

        JMenuItem itemUnirVertices = createItem("Unir vértices", AppIcons.joinVerticesIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_U, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemUnirVertices.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateJoinVerticesMode();
            }
        });

        JMenuItem itemGuardarEdicion = createItem("Guardar cambios de edición", AppIcons.saveIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_G, menuMask));
        itemGuardarEdicion.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.saveFeatureEditChanges();
            }
        });

        JMenuItem itemTerminarEdicion = createItem("Terminar edición", AppIcons.finishIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK));
        itemTerminarEdicion.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.finishFeatureEdit();
            }
        });

        JMenuItem itemCancelarEdicion = createItem("Cancelar edición", AppIcons.cancelIcon());
        itemCancelarEdicion.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.cancelFeatureEdit();
            }
        });

        JMenuItem itemUnirElementos = createItem("Unir elementos seleccionados", AppIcons.saveIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_J, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemUnirElementos.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.mergeSelectedFeatures();
            }
        });

        JMenuItem itemExplotar = createItem("Explotar entidades seleccionadas", AppIcons.exportIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_E, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemExplotar.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.explodeSelectedFeatures();
            }
        });

        JMenuItem itemContinuarLinea = createItem("Continuar linea", AppIcons.lineIcon());
        itemContinuarLinea.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.enableContinueLineMode();
            }
        });

        JMenuItem itemRectangulo = createItem("Rectangulo", AppIcons.rectangleIcon());
        itemRectangulo.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.enableDrawRectangleMode();
            }
        });

        JMenuItem itemCirculo = createItem("Circulo", AppIcons.circleIcon());
        itemCirculo.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.enableDrawCircleMode();
            }
        });

        JMenuItem itemCirculo3P = createItem("Circulo por 3 puntos", AppIcons.circleThreePointsIcon());
        itemCirculo3P.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.enableDrawCircleThreePointMode();
            }
        });

        JMenuItem itemExtenderLinea = createItem("Extender linea", AppIcons.extendLineIcon());
        itemExtenderLinea.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateExtendLineMode();
            }
        });

        JMenuItem itemAcortarLinea = createItem("Acortar linea", AppIcons.shortenLineIcon());
        itemAcortarLinea.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateShortenLineMode();
            }
        });

        JMenuItem itemParalela = createItem("Paralela / desplazamiento lateral", AppIcons.parallelIcon());
        itemParalela.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateParallelLineMode();
            }
        });

        JMenuItem itemPerpendicular = createItem("Perpendicular", AppIcons.perpendicularIcon());
        itemPerpendicular.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activatePerpendicularLineMode();
            }
        });

        JMenuItem itemCadIntegration = createItem("Integracion DWG / CAD...", AppIcons.propertiesIcon());
        itemCadIntegration.addActionListener(e -> CadIntegrationDialog.open());

        JMenuItem itemCadGeoref = createItem("Georreferenciar capa CAD seleccionada...", AppIcons.crsIcon());
        itemCadGeoref.addActionListener(e -> {
            Layer layer = CatgisDesktopApp.layersPanel != null ? CatgisDesktopApp.layersPanel.getSelectedLayer() : null;
            CadWorkflowSupport.openGeoreferenceWorkflow(CatgisDesktopApp.getMainFrameSafe(), layer);
        });

        JMenuItem itemCadInternalLayers = createItem("Capas internas CAD...", AppIcons.tableIcon());
        itemCadInternalLayers.addActionListener(e -> {
            Layer layer = CatgisDesktopApp.layersPanel != null ? CatgisDesktopApp.layersPanel.getSelectedLayer() : null;
            CadWorkflowSupport.openCadInternalLayers(CatgisDesktopApp.getMainFrameSafe(), layer);
        });

        menuCad.add(itemContinuarLinea);
        menuCad.add(itemExtenderLinea);
        menuCad.add(itemAcortarLinea);
        menuCad.add(itemParalela);
        menuCad.add(itemPerpendicular);
        menuCad.addSeparator();
        menuCad.add(itemRectangulo);
        menuCad.add(itemCirculo);
        menuCad.add(itemCirculo3P);
        menuCad.addSeparator();
        menuCad.add(itemCadGeoref);
        menuCad.add(itemCadInternalLayers);
        menuCad.add(itemCadIntegration);

        menuEdicion.add(itemCortar);
        menuEdicion.add(itemCopiar);
        menuEdicion.add(itemCopiarACapaEditable);
        menuEdicion.add(itemPegar);
        menuEdicion.add(itemBorrar);
        menuEdicion.addSeparator();
        menuEdicion.add(itemDeshacer);
        menuEdicion.add(itemRehacer);
        menuEdicion.addSeparator();
        menuEdicion.add(itemMoverSeleccion);
        menuEdicion.add(itemCortarGeometria);
        menuEdicion.add(itemUnirVertices);
        menuEdicion.add(menuCad);
        menuEdicion.add(itemUnirElementos);
        menuEdicion.add(itemExplotar);
        menuEdicion.addSeparator();
        menuEdicion.add(itemGuardarEdicion);
        menuEdicion.add(itemTerminarEdicion);
        menuEdicion.add(itemCancelarEdicion);

        JMenuItem itemZoomMas = createItem("Zoom +", AppIcons.zoomInIcon());
        itemZoomMas.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomIn());

        JMenuItem itemZoomMenos = createItem("Zoom -", AppIcons.zoomOutIcon());
        itemZoomMenos.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomOut());

        JMenuItem itemZoomCapa = createItem("Zoom a capa seleccionada", AppIcons.zoomLayerIcon());
        itemZoomCapa.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomToSelectedLayerPublic());

        JMenuItem itemZoomTodo = createItem("Zoom a todas las capas", AppIcons.zoomAllIcon());
        itemZoomTodo.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomToAllLayers());

        JMenuItem itemVistaAnterior = createItem("Vista anterior", AppIcons.viewPreviousIcon());
        itemVistaAnterior.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomPrevious());

        JMenuItem itemVistaSiguiente = createItem("Vista siguiente", AppIcons.viewNextIcon());
        itemVistaSiguiente.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomNext());

        menuVer.add(itemZoomMas);
        menuVer.add(itemZoomMenos);
        menuVer.addSeparator();
        menuVer.add(itemZoomCapa);
        menuVer.add(itemZoomTodo);
        menuVer.addSeparator();
        menuVer.add(itemVistaAnterior);
        menuVer.add(itemVistaSiguiente);

        JMenuItem itemMover = createItem("Mover", AppIcons.panIcon());
        itemMover.addActionListener(e -> CatgisDesktopApp.mapPanel.enablePanMode());

        JMenuItem itemIdentificar = createItem("Identificar", AppIcons.identifyIcon());
        itemIdentificar.addActionListener(e -> CatgisDesktopApp.mapPanel.enableIdentifyMode());

        JMenuItem itemBuscarCoord = createItem("Buscar por coordenadas", createSearchXYIcon());
        itemBuscarCoord.addActionListener(e -> GoToCoordinatesDialog.openDialog());

        JMenuItem itemPunto = createItem("Dibujar punto", AppIcons.pointIcon());
        itemPunto.addActionListener(e -> CatgisDesktopApp.mapPanel.enableDrawPointMode());

        JMenuItem itemMultiPunto = createItem("Dibujar multipunto", AppIcons.multiPointIcon());
        itemMultiPunto.addActionListener(e -> CatgisDesktopApp.mapPanel.enableDrawMultiPointMode());

        JMenuItem itemLinea = createItem("Dibujar línea", AppIcons.lineIcon());
        itemLinea.addActionListener(e -> CatgisDesktopApp.mapPanel.enableDrawLineMode());

        JMenuItem itemPoligono = createItem("Dibujar polígono", AppIcons.polygonIcon());
        itemPoligono.addActionListener(e -> CatgisDesktopApp.mapPanel.enableDrawPolygonMode());

        JMenuItem itemMedirDist = createItem("Medir distancia", AppIcons.distanceIcon());
        itemMedirDist.addActionListener(e -> CatgisDesktopApp.mapPanel.enableMeasureDistanceMode());

        JMenuItem itemMedirArea = createItem("Medir área", AppIcons.areaIcon());
        itemMedirArea.addActionListener(e -> CatgisDesktopApp.mapPanel.enableMeasureAreaMode());

        JMenuItem itemTerminar = createItem("Terminar", AppIcons.finishIcon());
        itemTerminar.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel.isMeasurementActive()) {
                CatgisDesktopApp.mapPanel.finishCurrentMeasurement();
            } else {
                CatgisDesktopApp.mapPanel.closeCurrentDrawingSession();
            }
        });

        JMenuItem itemCancelar = createItem("Cancelar", AppIcons.cancelIcon());
        itemCancelar.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel.isMeasurementActive()) {
                CatgisDesktopApp.mapPanel.cancelCurrentMeasurement();
            } else {
                CatgisDesktopApp.mapPanel.cancelCurrentDrawing();
            }
        });

        JMenuItem itemTabla = createItem("Tabla de atributos", AppIcons.tableIcon());
        itemTabla.addActionListener(e -> OpenAttributeTableAction.openAttributeTable());

        JMenuItem itemCalculadoraCampos = createItem("Calculadora de campos", createCalculatorIcon());
        itemCalculadoraCampos.addActionListener(e -> OpenAttributeTableAction.openFieldCalculatorForSelectedLayer());

        JMenuItem itemAsignarValor = createItem("Asignar valor a un campo", createAssignValueIcon());
        itemAsignarValor.addActionListener(e -> OpenAttributeTableAction.openAssignValueForSelectedLayer());

        JMenuItem itemConversor = createItem("Conversor CRS / EPSG", createConverterIcon());
        itemConversor.addActionListener(e -> CoordinateConverterDialog.openDialog());

        JMenuItem itemConsultas = createItem("Constructor de consultas", AppIcons.identifyIcon());
        itemConsultas.addActionListener(e -> OpenAttributeTableAction.openQueryBuilderForSelectedLayer());

        JMenuItem itemProjectCRS = createItem("CRS del proyecto", createProjectCrsIcon());
        itemProjectCRS.addActionListener(e -> ProjectCRSDialog.openDialog());

        JMenuItem itemRenombrarProyecto = createItem("Renombrar proyecto", AppIcons.renameIcon());
        itemRenombrarProyecto.addActionListener(e -> CatgisDesktopApp.renameCurrentProject());

        menuHerramientas.add(itemMover);
        menuHerramientas.add(itemIdentificar);
        menuHerramientas.add(itemBuscarCoord);
        menuHerramientas.addSeparator();
        JMenuItem itemRectanguloDibujo = createItem("Dibujar rectangulo", AppIcons.rectangleIcon());
        itemRectanguloDibujo.addActionListener(e -> CatgisDesktopApp.mapPanel.enableDrawRectangleMode());

        menuHerramientas.add(itemPunto);
        menuHerramientas.add(itemMultiPunto);
        menuHerramientas.add(itemLinea);
        menuHerramientas.add(itemRectanguloDibujo);
        menuHerramientas.add(itemPoligono);
        menuHerramientas.addSeparator();
        menuHerramientas.add(itemMedirDist);
        menuHerramientas.add(itemMedirArea);
        menuHerramientas.addSeparator();
        menuHerramientas.add(itemTerminar);
        menuHerramientas.add(itemCancelar);
        menuHerramientas.addSeparator();
        menuHerramientas.add(itemTabla);
        menuHerramientas.add(itemConsultas);
        menuHerramientas.add(itemCalculadoraCampos);
        menuHerramientas.add(itemAsignarValor);
        menuHerramientas.add(itemConversor);
        menuHerramientas.addSeparator();
        menuHerramientas.add(itemProjectCRS);
        menuHerramientas.add(itemRenombrarProyecto);
        menuHerramientas.addSeparator();
        menuHerramientas.add(menuModulos);
        menuHerramientas.addSeparator();
        // Scripting
        JMenu menuScripting = new JMenu("Scripting");
        JMenuItem itemRunScript = createItem("Ejecutar script Python...", null);
        itemRunScript.addActionListener(e -> runPythonScript());
        menuScripting.add(itemRunScript);
        menuHerramientas.add(menuScripting);

        JMenuItem itemVentanaPrincipal = createItem("Traer CATGIS al frente", AppIcons.projectIcon());
        itemVentanaPrincipal.addActionListener(e -> {
            javax.swing.JFrame frame = CatgisDesktopApp.getMainFrame();
            if (frame != null) {
                frame.setState(javax.swing.JFrame.NORMAL);
                frame.setVisible(true);
                frame.toFront();
                frame.requestFocus();
            }
        });

        JMenuItem itemVentanaTabla = createItem("Tabla de atributos de la capa seleccionada", AppIcons.tableIcon());
        itemVentanaTabla.addActionListener(e -> OpenAttributeTableAction.openAttributeTable());

        JMenuItem itemVentanaConsultas = createItem("Constructor de consultas de la capa seleccionada", AppIcons.identifyIcon());
        itemVentanaConsultas.addActionListener(e -> OpenAttributeTableAction.openQueryBuilderForSelectedLayer());

        JMenuItem itemVentanaTablasAbiertas = createItem("Traer tablas abiertas al frente", AppIcons.tableIcon());
        itemVentanaTablasAbiertas.addActionListener(e -> OpenAttributeTableAction.focusOpenTables());

        menuVer.addSeparator();
        menuVer.add(itemVentanaPrincipal);
        menuVer.addSeparator();
        menuVer.add(itemVentanaTabla);
        menuVer.add(itemVentanaConsultas);
        menuVer.add(itemVentanaTablasAbiertas);

        JMenuItem itemPanelAyuda = createItem("Panel de ayuda", createHelpIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        itemPanelAyuda.addActionListener(e -> HelpCenterDialog.open());

        JMenuItem itemAcercaDe = createItem("Acerca de CATGIS", createAboutIcon());
        itemAcercaDe.addActionListener(e -> AboutCatgisDialog.open());

        menuAyuda.add(itemPanelAyuda);
        menuAyuda.addSeparator();
        menuAyuda.add(buildLanguageMenu());
        menuAyuda.addSeparator();
        menuAyuda.add(itemAcercaDe);

        add(menuArchivo);
        add(menuVer);
        add(menuDatos);
        add(menuEdicion);
        add(menuAnalisis);
        add(menuSalida);
        add(menuHerramientas);
        add(menuAyuda);
    }

    // --- Recent files ---
    private static final int MAX_RECENT = 10;
    private static final java.util.List<String> recentFiles = new java.util.ArrayList<>();

    private void loadRecentFiles(JMenu menu) {
        menu.removeAll();
        recentFiles.clear();
        try {
            java.io.File recentFile = new java.io.File(System.getProperty("user.home"), ".catgis-recent");
            if (recentFile.exists()) {
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(recentFile));
                String line;
                while ((line = br.readLine()) != null && recentFiles.size() < MAX_RECENT) {
                    if (!line.trim().isEmpty()) recentFiles.add(line.trim());
                }
                br.close();
            }
        } catch (Exception ignored) {}
        if (recentFiles.isEmpty()) {
            JMenuItem empty = new JMenuItem("(vacio)");
            empty.setEnabled(false);
            menu.add(empty);
        } else {
            for (String path : recentFiles) {
                java.io.File f = new java.io.File(path);
                JMenuItem item = new JMenuItem(f.getName());
                item.setToolTipText(path);
                item.addActionListener(e -> {
                    if (CatgisDesktopApp.confirmProjectContinuation("abrir proyecto reciente")) {
                        LoadProjectAction.loadProjectFile(f);
                    }
                });
                menu.add(item);
            }
            menu.addSeparator();
            JMenuItem clearItem = new JMenuItem("Limpiar recientes");
            clearItem.addActionListener(e -> {
                recentFiles.clear();
                saveRecentFiles();
                menu.removeAll();
                JMenuItem empty = new JMenuItem("(vacio)");
                empty.setEnabled(false);
                menu.add(empty);
            });
            menu.add(clearItem);
        }
    }

    public static void addRecentFile(String path) {
        recentFiles.remove(path);
        recentFiles.add(0, path);
        while (recentFiles.size() > MAX_RECENT) recentFiles.remove(recentFiles.size() - 1);
        saveRecentFiles();
    }

    private static void saveRecentFiles() {
        try {
            java.io.File recentFile = new java.io.File(System.getProperty("user.home"), ".catgis-recent");
            java.io.PrintWriter pw = new java.io.PrintWriter(recentFile);
            for (String path : recentFiles) pw.println(path);
            pw.close();
        } catch (Exception ignored) {}
    }

    private JMenuItem createItem(String text, Icon icon) {
        return createItem(text, icon, null);
    }

    private JMenuItem createItem(String text, Icon icon, KeyStroke accelerator) {
        JMenuItem item = new JMenuItem(I18n.t(text), icon);
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        return item;
    }

    private void runPythonScript() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Python scripts (*.py)", "py"));
        if (chooser.showOpenDialog(CatgisDesktopApp.getMainFrameSafe()) == JFileChooser.APPROVE_OPTION) {
            File script = chooser.getSelectedFile();
            ar.com.catgis.scripting.ScriptEngine.ScriptResult result = ar.com.catgis.scripting.ScriptEngine.executeScript(script);
            String msg = result.success()
                    ? "Script ejecutado correctamente.\n\nSalida:\n" + result.output()
                    : "Error al ejecutar script:\n" + result.error() + "\n\nSalida:\n" + result.output();
            JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), msg,
                    result.success() ? "Script ejecutado" : "Error en script",
                    result.success() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runTopologyValidation(String rule) {
        Layer layer = CatgisDesktopApp.layersPanel != null ? CatgisDesktopApp.layersPanel.getSelectedLayer() : null;
        if (layer == null) {
            JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(),
                    I18n.t("Selecciona una capa para validar topología."));
            return;
        }

        java.util.List<org.geotools.api.feature.simple.SimpleFeature> features = new java.util.ArrayList<>();
        ShapefileData data = CatgisDesktopApp.mapPanel != null ? CatgisDesktopApp.mapPanel.getShapefileData(layer) : null;
        if (data != null && data.getFeatureCollection() != null) {
            org.geotools.data.simple.SimpleFeatureCollection fc = data.getFeatureCollection();
            try (org.geotools.feature.FeatureIterator<org.geotools.api.feature.simple.SimpleFeature> it = fc.features()) {
                while (it.hasNext()) { features.add(it.next()); }
            }
        }

        if (features.isEmpty()) {
            JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(),
                    I18n.t("La capa no tiene features para validar."));
            return;
        }

        TopologyValidator.TopologyResult result = switch (rule) {
            case "NO_GAPS" -> TopologyValidator.validateNoGaps(features, 1e-6);
            case "NO_OVERLAPS" -> TopologyValidator.validateNoOverlaps(features);
            case "NO_SELF_INTERSECTION" -> TopologyValidator.validateNoSelfIntersections(features);
            case "NO_DANGLES" -> TopologyValidator.validateLineConnectivity(features, 1e-6);
            default -> new TopologyValidator.TopologyResult(true, new java.util.ArrayList<>());
        };

        String msg = result.valid()
                ? I18n.t("Validación OK: sin problemas de topología encontrados.")
                : I18n.t("Problemas encontrados: ") + result.issues().size() + "\n\n"
                + result.issues().stream().limit(5).map(i -> "- " + i.message()).reduce("", (a, b) -> a + "\n" + b);
        JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), msg,
                I18n.t("Validación de topología"),
                result.valid() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private JMenu buildLanguageMenu() {
        JMenu languageMenu = new JMenu(I18n.languageMenuLabel());

        JRadioButtonMenuItem spanishItem = new JRadioButtonMenuItem(I18n.languageSelectionLabel(I18n.Language.SPANISH));
        JRadioButtonMenuItem englishItem = new JRadioButtonMenuItem(I18n.languageSelectionLabel(I18n.Language.ENGLISH));
        ButtonGroup group = new ButtonGroup();
        group.add(spanishItem);
        group.add(englishItem);

        spanishItem.setSelected(I18n.getCurrentLanguage() == I18n.Language.SPANISH);
        englishItem.setSelected(I18n.getCurrentLanguage() == I18n.Language.ENGLISH);

        spanishItem.addActionListener(e -> updateLanguage(I18n.Language.SPANISH));
        englishItem.addActionListener(e -> updateLanguage(I18n.Language.ENGLISH));

        languageMenu.add(spanishItem);
        languageMenu.add(englishItem);
        return languageMenu;
    }

    private void updateLanguage(I18n.Language language) {
        if (I18n.getCurrentLanguage() == language) {
            return;
        }
        I18n.setLanguage(language);
        JOptionPane.showMessageDialog(
                CatgisDesktopApp.getMainFrameSafe(),
                I18n.t("Idioma actualizado. Reinicia CATGIS para ver el cambio en toda la interfaz."),
                I18n.languageMenuLabel(),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private Layer getPreferredVectorLayer() {
        if (CatgisDesktopApp.mapPanel != null) {
            Layer editingLayer = CatgisDesktopApp.mapPanel.getEditingLayerRef();
            if (editingLayer != null) {
                return editingLayer;
            }
        }

        Layer selectedLayer = CatgisDesktopApp.layersPanel != null ? CatgisDesktopApp.layersPanel.getSelectedLayer() : null;
        if (selectedLayer != null && !(selectedLayer instanceof RasterLayer)) {
            return selectedLayer;
        }
        return null;
    }

    private Icon createNewProjectIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(104, 70, 193));
        g.fillRoundRect(2, 2, 10, 12, 3, 3);
        g.setColor(new Color(230, 224, 252));
        g.fillRect(4, 5, 6, 1);
        g.fillRect(4, 8, 6, 1);
        g.fillRect(4, 11, 4, 1);
        g.setColor(new Color(34, 139, 34));
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(12, 5, 12, 13);
        g.drawLine(8, 9, 15, 9);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createOpenProjectIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(104, 70, 193));
        g.fillRoundRect(2, 5, 9, 8, 3, 3);
        g.setColor(new Color(137, 106, 219));
        g.fillRoundRect(3, 3, 5, 3, 2, 2);
        g.setColor(new Color(238, 233, 255));
        g.fillRect(4, 7, 5, 4);
        g.setColor(new Color(41, 121, 255));
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(10, 8, 14, 8);
        g.drawLine(12, 6, 14, 8);
        g.drawLine(12, 10, 14, 8);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createOpenLayerIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(54, 114, 196));
        g.fillRoundRect(2, 6, 12, 8, 3, 3);
        g.setColor(new Color(86, 142, 217));
        g.fillRoundRect(3, 4, 5, 3, 2, 2);
        g.setColor(new Color(243, 249, 255));
        g.fillRect(4, 8, 8, 4);
        g.setColor(new Color(34, 139, 34));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(12, 3, 12, 8);
        g.drawLine(9, 5, 15, 5);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createNewVectorLayerIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(45, 105, 185));
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(2, 2, 9, 11, 3, 3);
        g.setColor(new Color(33, 150, 83));
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(4, 10, 6, 7);
        g.drawLine(6, 7, 8, 9);
        g.fillOval(3, 9, 2, 2);
        g.fillOval(5, 6, 2, 2);
        g.fillOval(7, 8, 2, 2);
        g.setColor(new Color(34, 139, 34));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(12, 5, 12, 14);
        g.drawLine(8, 10, 15, 10);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createCameraIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(60, 68, 80));
        g.fillRoundRect(2, 5, 12, 8, 3, 3);
        g.fillRoundRect(4, 3, 4, 3, 2, 2);
        g.setColor(new Color(220, 226, 235));
        g.fillOval(5, 6, 5, 5);
        g.setColor(new Color(90, 120, 170));
        g.drawOval(5, 6, 5, 5);
        g.setColor(new Color(255, 196, 0));
        g.fillOval(11, 6, 2, 2);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createSearchXYIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(45, 95, 180));
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(2, 2, 7, 7);
        g.drawLine(8, 8, 13, 13);
        g.setColor(new Color(55, 55, 55));
        g.setFont(new Font("SansSerif", Font.BOLD, 6));
        g.drawString("X", 10, 7);
        g.drawString("Y", 10, 13);
        g.dispose();
        return new ImageIcon(img);
    }


    private Icon createCalculatorIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(139, 92, 246));
        g.fillRoundRect(2, 2, 12, 12, 3, 3);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("=", 5, 12);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createAssignValueIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(14, 165, 233));
        g.fillRoundRect(2, 2, 12, 12, 3, 3);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.drawString("Aa", 3, 11);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createConverterIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(65, 105, 225));
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(3, 5, 13, 5);
        g.drawLine(10, 3, 13, 5);
        g.drawLine(10, 7, 13, 5);
        g.setColor(new Color(220, 20, 60));
        g.drawLine(13, 11, 3, 11);
        g.drawLine(6, 9, 3, 11);
        g.drawLine(6, 13, 3, 11);
        g.setColor(new Color(60, 60, 60));
        g.setFont(new Font("SansSerif", Font.BOLD, 5));
        g.drawString("XY", 5, 15);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createProjectCrsIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(33, 120, 210));
        g.fillRoundRect(2, 3, 12, 10, 4, 4);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(5, 3, 5, 13);
        g.drawLine(10, 3, 10, 13);
        g.drawLine(2, 6, 14, 6);
        g.drawLine(2, 10, 14, 10);
        g.setColor(new Color(255, 196, 0));
        g.fillOval(10, 2, 4, 4);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createHelpIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(33, 120, 210));
        g.fillRoundRect(2, 2, 12, 12, 4, 4);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("?", 5, 12);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createAboutIcon() {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(20, 96, 182));
        g.fillOval(2, 2, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.drawString("i", 6, 12);
        g.dispose();
        return new ImageIcon(img);
    }
}

