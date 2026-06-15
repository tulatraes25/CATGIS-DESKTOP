package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;

import ar.com.catgis.TopologyValidator;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import java.io.File;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

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
import ar.com.catgis.core.model.Layer;

public class MainMenuBar extends JMenuBar {

    public MainMenuBar() {
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // =====================================================================
        // 1. ARCHIVO
        // =====================================================================
        JMenu menuArchivo = new JMenu(I18n.t("Archivo"));

        JMenuItem itemNuevoProyecto = createItem("Nuevo proyecto", createNewProjectIcon());
        itemNuevoProyecto.addActionListener(e -> NewProjectAction.newProject());

        JMenuItem itemAbrirProyecto = createItem("Abrir proyecto", createOpenProjectIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, menuMask));
        itemAbrirProyecto.addActionListener(e -> LoadProjectAction.loadProject());

        JMenuItem itemGuardarProyecto = createItem("Guardar proyecto", AppIcons.saveIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask));
        itemGuardarProyecto.addActionListener(e -> SaveProjectAction.saveProject());

        JMenuItem itemGuardarProyectoComo = createItem("Guardar proyecto como...", AppIcons.attrCopyIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemGuardarProyectoComo.addActionListener(e -> SaveProjectAction.saveProjectAs());

        JMenu recentMenu = new JMenu("Proyectos recientes");
        loadRecentFiles(recentMenu);

        JMenuItem itemSalvarVista = createItem("Salvar vista del mapa", createCameraIcon());
        itemSalvarVista.addActionListener(e -> SaveMapViewAction.saveCurrentView());

        JMenuItem itemSalir = createItem("Salir", null);
        itemSalir.addActionListener(e -> System.exit(0));

        menuArchivo.add(itemNuevoProyecto);
        menuArchivo.add(itemAbrirProyecto);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardarProyecto);
        menuArchivo.add(itemGuardarProyectoComo);
        menuArchivo.addSeparator();
        menuArchivo.add(recentMenu);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalvarVista);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        // =====================================================================
        // 2. EDITAR
        // =====================================================================
        JMenu menuEditar = new JMenu(I18n.t("Editar"));

        JMenuItem itemCortar = createItem("Cortar", AppIcons.cutIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_X, menuMask));
        itemCortar.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().cutSelectedFeatures();
            }
        });

        JMenuItem itemCopiar = createItem("Copiar", AppIcons.attrCopyIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_C, menuMask));
        itemCopiar.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().copySelectedFeatures();
            }
        });

        JMenuItem itemPegar = createItem("Pegar", AppIcons.openIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_V, menuMask));
        itemPegar.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                Layer layer = getPreferredVectorLayer();
                if (layer != null) {
                    AppContext.mapPanel().prepareLayerForEditing(layer);
                }
                AppContext.mapPanel().pasteCopiedFeatures();
            }
        });

        JMenuItem itemBorrar = createItem("Borrar", AppIcons.removeIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        itemBorrar.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().deleteSelectedFeatures();
            }
        });

        JMenuItem itemDeshacer = createItem("Deshacer", AppIcons.undoIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuMask));
        itemDeshacer.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().undoFeatureEdit();
            }
        });

        JMenuItem itemRehacer = createItem("Rehacer", AppIcons.redoIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, menuMask));
        itemRehacer.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().redoFeatureEdit();
            }
        });

        JMenuItem itemMoverSeleccion = createItem("Mover selección", AppIcons.moveFeatureIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_M, menuMask));
        itemMoverSeleccion.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().activateMoveFeatureMode();
            }
        });

        JMenuItem itemCortarGeometria = createItem("Cortar geometría", AppIcons.cutIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_K, menuMask));
        itemCortarGeometria.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().activateCutFeatureMode();
            }
        });

        JMenuItem itemUnirVertices = createItem("Unir vértices", AppIcons.joinVerticesIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_U, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemUnirVertices.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().activateJoinVerticesMode();
            }
        });

        JMenuItem itemUnirElementos = createItem("Unir elementos", AppIcons.saveIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_J, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemUnirElementos.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().mergeSelectedFeatures();
            }
        });

        JMenuItem itemExplotar = createItem("Explotar entidades", AppIcons.exportIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_E, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemExplotar.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().explodeSelectedFeatures();
            }
        });

        JMenuItem itemCopiarACapaEditable = createItem("Copiar a editable", AppIcons.attrAssignIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_V, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemCopiarACapaEditable.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                Layer layer = getPreferredVectorLayer();
                if (layer != null) {
                    AppContext.mapPanel().prepareLayerForEditing(layer);
                }
                AppContext.mapPanel().copySelectedFeaturesToEditingLayer();
            }
        });

        JMenuItem itemPegarEnEditable = createItem("Pegar en editable", AppIcons.openIcon());
        itemPegarEnEditable.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                Layer layer = getPreferredVectorLayer();
                if (layer != null) {
                    AppContext.mapPanel().prepareLayerForEditing(layer);
                }
                AppContext.mapPanel().pasteCopiedFeatures();
            }
        });

        JMenuItem itemGuardarEdicion = createItem("Guardar cambios", AppIcons.saveIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_G, menuMask));
        itemGuardarEdicion.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().saveFeatureEditChanges();
            }
        });

        JMenuItem itemTerminarEdicion = createItem("Terminar edición", AppIcons.finishIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK));
        itemTerminarEdicion.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().finishFeatureEdit();
            }
        });

        JMenuItem itemCancelarEdicion = createItem("Cancelar edición", AppIcons.cancelIcon());
        itemCancelarEdicion.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().cancelFeatureEdit();
            }
        });

        // CAD submenu
        JMenu menuCad = new JMenu("CAD");

        JMenuItem itemContinuarLinea = createItem("Continuar linea", AppIcons.lineIcon());
        itemContinuarLinea.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().enableContinueLineMode();
            }
        });

        JMenuItem itemExtenderLinea = createItem("Extender linea", AppIcons.extendLineIcon());
        itemExtenderLinea.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().activateExtendLineMode();
            }
        });

        JMenuItem itemAcortarLinea = createItem("Acortar linea", AppIcons.shortenLineIcon());
        itemAcortarLinea.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().activateShortenLineMode();
            }
        });

        JMenuItem itemParalela = createItem("Paralela / desplazamiento lateral", AppIcons.parallelIcon());
        itemParalela.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().activateParallelLineMode();
            }
        });

        JMenuItem itemPerpendicular = createItem("Perpendicular", AppIcons.perpendicularIcon());
        itemPerpendicular.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().activatePerpendicularLineMode();
            }
        });

        JMenuItem itemRectanguloCad = createItem("Rectangulo", AppIcons.rectangleIcon());
        itemRectanguloCad.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().enableDrawRectangleMode();
            }
        });

        JMenuItem itemCirculo = createItem("Circulo", AppIcons.circleIcon());
        itemCirculo.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().enableDrawCircleMode();
            }
        });

        JMenuItem itemCirculo3P = createItem("Circulo por 3 puntos", AppIcons.circleThreePointsIcon());
        itemCirculo3P.addActionListener(e -> {
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().enableDrawCircleThreePointMode();
            }
        });

        JMenuItem itemCadGeoref = createItem("Georreferenciar capa CAD seleccionada...", AppIcons.crsIcon());
        itemCadGeoref.addActionListener(e -> {
            Layer layer = CatgisDesktopApp.layersPanel != null ? AppContext.getSelectedLayer() : null;
            CadWorkflowSupport.openGeoreferenceWorkflow(CatgisDesktopApp.getMainFrameSafe(), layer);
        });

        JMenuItem itemCadInternalLayers = createItem("Capas internas CAD...", AppIcons.tableIcon());
        itemCadInternalLayers.addActionListener(e -> {
            Layer layer = CatgisDesktopApp.layersPanel != null ? AppContext.getSelectedLayer() : null;
            CadWorkflowSupport.openCadInternalLayers(CatgisDesktopApp.getMainFrameSafe(), layer);
        });

        JMenuItem itemCadIntegration = createItem("Integracion DWG / CAD...", AppIcons.propertiesIcon());
        itemCadIntegration.addActionListener(e -> CadIntegrationDialog.open());

        menuCad.add(itemContinuarLinea);
        menuCad.add(itemExtenderLinea);
        menuCad.add(itemAcortarLinea);
        menuCad.add(itemParalela);
        menuCad.add(itemPerpendicular);
        menuCad.addSeparator();
        menuCad.add(itemRectanguloCad);
        menuCad.add(itemCirculo);
        menuCad.add(itemCirculo3P);
        menuCad.addSeparator();
        menuCad.add(itemCadGeoref);
        menuCad.add(itemCadInternalLayers);
        menuCad.add(itemCadIntegration);

        menuEditar.add(itemCortar);
        menuEditar.add(itemCopiar);
        menuEditar.add(itemPegar);
        menuEditar.add(itemBorrar);
        menuEditar.addSeparator();
        menuEditar.add(itemDeshacer);
        menuEditar.add(itemRehacer);
        menuEditar.addSeparator();
        menuEditar.add(itemMoverSeleccion);
        menuEditar.add(itemCortarGeometria);
        menuEditar.add(itemUnirVertices);
        menuEditar.add(itemUnirElementos);
        menuEditar.add(itemExplotar);
        menuEditar.addSeparator();
        menuEditar.add(itemCopiarACapaEditable);
        menuEditar.add(itemPegarEnEditable);
        menuEditar.addSeparator();
        menuEditar.add(itemGuardarEdicion);
        menuEditar.add(itemTerminarEdicion);
        menuEditar.add(itemCancelarEdicion);
        menuEditar.addSeparator();
        menuEditar.add(menuCad);

        // =====================================================================
        // 3. CAPAS
        // =====================================================================
        JMenu menuCapas = new JMenu(I18n.t("Capas"));

        JMenuItem itemAgregarCapa = createItem("Agregar capa", createOpenLayerIcon());
        itemAgregarCapa.addActionListener(e -> AddLayerAction.openLayer());

        JMenuItem itemQuitarCapa = createItem("Quitar capa", AppIcons.removeIcon());
        itemQuitarCapa.addActionListener(e -> {
            Layer layer = AppContext.getSelectedLayer();
            if (layer != null && AppContext.mapPanel() != null) {
                AppContext.mapPanel().removeLayer(layer);
            } else {
                NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null,
                        I18n.t("Selecciona una capa para quitar."));
            }
        });

        JMenuItem itemSubirCapa = createItem("Subir capa", AppIcons.upIcon());
        itemSubirCapa.addActionListener(e -> {
            Layer layer = AppContext.getSelectedLayer();
            if (layer != null && AppContext.mapPanel() != null) {
                AppContext.mapPanel().moveLayerUp(layer);
            }
        });

        JMenuItem itemBajarCapa = createItem("Bajar capa", AppIcons.downIcon());
        itemBajarCapa.addActionListener(e -> {
            Layer layer = AppContext.getSelectedLayer();
            if (layer != null && AppContext.mapPanel() != null) {
                AppContext.mapPanel().moveLayerDown(layer);
            }
        });

        JMenuItem itemPropiedadesCapa = createItem("Propiedades de capa...", AppIcons.propertiesIcon());
        itemPropiedadesCapa.addActionListener(e -> {
            Layer layer = CatgisDesktopApp.layersPanel != null ? AppContext.getSelectedLayer() : null;
            if (layer == null) {
                NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null,
                        I18n.t("Selecciona una capa para ver sus propiedades."));
                return;
            }
            LayerPropertiesDialog.open(layer);
        });

        JMenuItem itemNuevaCapaVectorial = createItem("Nueva capa vectorial", createNewVectorLayerIcon());
        itemNuevaCapaVectorial.addActionListener(e -> NewVectorLayerAction.createNewVectorLayer(null, CatgisDesktopApp.getMainFrameSafe()));

        JMenuItem itemAbrirTabla = createItem("Cargar tabla externa", AppIcons.importTableIcon());
        itemAbrirTabla.addActionListener(e -> OpenTablePointsAction.openTablePoints());

        JMenuItem itemRenombrarProyecto = createItem("Renombrar proyecto", AppIcons.renameIcon());
        itemRenombrarProyecto.addActionListener(e -> CatgisDesktopApp.renameCurrentProject());

        JMenuItem itemZoomMas = createItem("Zoom +", AppIcons.zoomInIcon());
        itemZoomMas.addActionListener(e -> AppContext.mapPanel().zoomIn());

        JMenuItem itemZoomMenos = createItem("Zoom -", AppIcons.zoomOutIcon());
        itemZoomMenos.addActionListener(e -> AppContext.mapPanel().zoomOut());

        JMenuItem itemZoomCapa = createItem("Zoom a capa seleccionada", AppIcons.zoomLayerIcon());
        itemZoomCapa.addActionListener(e -> AppContext.mapPanel().zoomToSelectedLayerPublic());

        JMenuItem itemZoomTodo = createItem("Zoom a todas las capas", AppIcons.zoomAllIcon());
        itemZoomTodo.addActionListener(e -> AppContext.mapPanel().zoomToAllLayers());

        JMenuItem itemVistaAnterior = createItem("Vista anterior", AppIcons.viewPreviousIcon());
        itemVistaAnterior.addActionListener(e -> AppContext.mapPanel().zoomPrevious());

        JMenuItem itemVistaSiguiente = createItem("Vista siguiente", AppIcons.viewNextIcon());
        itemVistaSiguiente.addActionListener(e -> AppContext.mapPanel().zoomNext());

        JMenuItem itemMover = createItem("Mover", AppIcons.panIcon());
        itemMover.addActionListener(e -> AppContext.mapPanel().enablePanMode());

        JMenuItem itemIdentificar = createItem("Identificar", AppIcons.identifyIcon());
        itemIdentificar.addActionListener(e -> AppContext.mapPanel().enableIdentifyMode());

        JMenuItem itemBuscarCoord = createItem("Buscar coordenadas", createSearchXYIcon());
        itemBuscarCoord.addActionListener(e -> GoToCoordinatesDialog.openDialog());

        JMenuItem itemPunto = createItem("Dibujar punto", AppIcons.pointIcon());
        itemPunto.addActionListener(e -> AppContext.mapPanel().enableDrawPointMode());

        JMenuItem itemLinea = createItem("Dibujar línea", AppIcons.lineIcon());
        itemLinea.addActionListener(e -> AppContext.mapPanel().enableDrawLineMode());

        JMenuItem itemPoligono = createItem("Dibujar polígono", AppIcons.polygonIcon());
        itemPoligono.addActionListener(e -> AppContext.mapPanel().enableDrawPolygonMode());

        JMenuItem itemMedirDist = createItem("Medir distancia", AppIcons.distanceIcon());
        itemMedirDist.addActionListener(e -> AppContext.mapPanel().enableMeasureDistanceMode());

        JMenuItem itemMedirArea = createItem("Medir área", AppIcons.areaIcon());
        itemMedirArea.addActionListener(e -> AppContext.mapPanel().enableMeasureAreaMode());

        JMenuItem itemTerminar = createItem("Terminar", AppIcons.finishIcon());
        itemTerminar.addActionListener(e -> {
            if (AppContext.mapPanel().isMeasurementActive()) {
                AppContext.mapPanel().finishCurrentMeasurement();
            } else {
                AppContext.mapPanel().closeCurrentDrawingSession();
            }
        });

        JMenuItem itemCancelar = createItem("Cancelar", AppIcons.cancelIcon());
        itemCancelar.addActionListener(e -> {
            if (AppContext.mapPanel().isMeasurementActive()) {
                AppContext.mapPanel().cancelCurrentMeasurement();
            } else {
                AppContext.mapPanel().cancelCurrentDrawing();
            }
        });

        JMenuItem itemTabla = createItem("Tabla de atributos", AppIcons.tableIcon());
        itemTabla.addActionListener(e -> OpenAttributeTableAction.openAttributeTable());

        JMenuItem itemConsultas = createItem("Constructor de consultas", AppIcons.identifyIcon());
        itemConsultas.addActionListener(e -> OpenAttributeTableAction.openQueryBuilderForSelectedLayer());

        JMenuItem itemCalculadoraCampos = createItem("Calculadora de campos", createCalculatorIcon());
        itemCalculadoraCampos.addActionListener(e -> OpenAttributeTableAction.openFieldCalculatorForSelectedLayer());

        JMenuItem itemAsignarValor = createItem("Asignar valor a un campo", createAssignValueIcon());
        itemAsignarValor.addActionListener(e -> OpenAttributeTableAction.openAssignValueForSelectedLayer());

        JMenuItem itemConversor = createItem("Conversor CRS / EPSG", createConverterIcon());
        itemConversor.addActionListener(e -> CoordinateConverterDialog.openDialog());

        JMenuItem itemProjectCRS = createItem("CRS del proyecto", createProjectCrsIcon());
        itemProjectCRS.addActionListener(e -> ProjectCRSDialog.openDialog());

        JMenuItem itemCustomCrs = createItem("Definir CRS personalizado...", null);
        itemCustomCrs.addActionListener(e -> CustomCrsDialog.open());

        menuCapas.add(itemAgregarCapa);
        menuCapas.add(itemQuitarCapa);
        menuCapas.add(itemSubirCapa);
        menuCapas.add(itemBajarCapa);
        menuCapas.add(itemPropiedadesCapa);
        menuCapas.addSeparator();
        menuCapas.add(itemNuevaCapaVectorial);
        menuCapas.add(itemAbrirTabla);
        menuCapas.add(itemRenombrarProyecto);
        menuCapas.addSeparator();
        menuCapas.add(itemZoomMas);
        menuCapas.add(itemZoomMenos);
        menuCapas.add(itemZoomCapa);
        menuCapas.add(itemZoomTodo);
        menuCapas.addSeparator();
        menuCapas.add(itemVistaAnterior);
        menuCapas.add(itemVistaSiguiente);
        menuCapas.addSeparator();
        menuCapas.add(itemMover);
        menuCapas.add(itemIdentificar);
        menuCapas.add(itemBuscarCoord);
        menuCapas.addSeparator();
        menuCapas.add(itemPunto);
        menuCapas.add(itemLinea);
        menuCapas.add(itemPoligono);
        menuCapas.addSeparator();
        menuCapas.add(itemMedirDist);
        menuCapas.add(itemMedirArea);
        menuCapas.add(itemTerminar);
        menuCapas.add(itemCancelar);
        menuCapas.addSeparator();
        menuCapas.add(itemTabla);
        menuCapas.add(itemConsultas);
        menuCapas.add(itemCalculadoraCampos);
        menuCapas.add(itemAsignarValor);
        menuCapas.addSeparator();
        menuCapas.add(itemConversor);
        menuCapas.add(itemProjectCRS);
        menuCapas.add(itemCustomCrs);

        // =====================================================================
        // 4. ANALISIS (with 6 submenus)
        // =====================================================================
        JMenu menuAnalisis = new JMenu(I18n.t("Análisis"));

        // 4a. Relieve
        JMenu menuRelieve = new JMenu(I18n.t("Relieve"));

        JMenuItem itemPerfilTopografico = createItem("Perfil topográfico...", AppIcons.distanceIcon());
        itemPerfilTopografico.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            TopographicProfileDialog.open();
        });

        JMenuItem itemCurvasNivel = createItem("Curvas de nivel...", AppIcons.lineIcon());
        itemCurvasNivel.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            ContourGenerationDialog.open();
        });

        JMenuItem itemHillshade = createItem("Hillshade / Sombreado...", null);
        itemHillshade.addActionListener(e -> HillshadeDialog.open());

        JMenuItem itemPendiente = createItem("Pendiente...", null);
        itemPendiente.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            TerrainHydrologyAnalysisDialog.open();
        });

        JMenuItem itemAspecto = createItem("Aspecto...", null);
        itemAspecto.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            TerrainHydrologyAnalysisDialog.open();
        });

        JMenuItem itemVisibilidad = createItem("Visibilidad / Viewshed...", null);
        itemVisibilidad.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            TerrainHydrologyAnalysisDialog.open();
        });

        JMenuItem itemRecortarDem = createItem("Recortar DEM...", AppIcons.cutIcon());
        itemRecortarDem.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            DemClipDialog.open();
        });

        JMenuItem itemWhitebox = createItem("WhiteboxTools...", null);
        itemWhitebox.addActionListener(e -> WhiteboxToolDialog.open());

        JMenuItem itemProRaster = createItem("Pro Raster...", null);
        itemProRaster.addActionListener(e -> {
            NotificationManager.info(CatgisDesktopApp.getMainFrameSafe(), "Pro Raster",
                    "Usa el menu contextual sobre una capa raster Pro para generar productos derivados.");
        });

        menuRelieve.add(itemPerfilTopografico);
        menuRelieve.add(itemCurvasNivel);
        menuRelieve.add(itemHillshade);
        menuRelieve.addSeparator();
        menuRelieve.add(itemPendiente);
        menuRelieve.add(itemAspecto);
        menuRelieve.add(itemVisibilidad);
        menuRelieve.addSeparator();
        menuRelieve.add(itemRecortarDem);
        menuRelieve.add(itemWhitebox);
        menuRelieve.addSeparator();
        menuRelieve.add(itemProRaster);

        // 4b. Drenaje
        JMenu menuDrenaje = new JMenu(I18n.t("Drenaje"));

        JMenuItem itemEscorrentias = createItem("Generar escorrentías...", AppIcons.drainageIcon());
        itemEscorrentias.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            DrainageExtractionDialog.open();
        });

        JMenuItem itemAnalisisHidro = createItem("Análisis topohidrológico...", AppIcons.terrainAnalysisIcon());
        itemAnalisisHidro.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            TerrainHydrologyAnalysisDialog.open();
        });

        JMenuItem itemCuencaOutlet = createItem("Cuencas...", AppIcons.pointIcon());
        itemCuencaOutlet.setToolTipText("Delimitar cuenca desde punto de salida — Requiere DEM raster + GDAL");
        itemCuencaOutlet.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            BasinFromOutletDialog.open();
        });

        JMenuItem itemInundacion = createItem("Inundación preliminar...", AppIcons.areaIcon());
        itemInundacion.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
                TopographyWorkflowSupport.showNoRasterMessage();
                return;
            }
            FloodScenarioDialog.open();
        });

        menuDrenaje.add(itemEscorrentias);
        menuDrenaje.add(itemAnalisisHidro);
        menuDrenaje.add(itemCuencaOutlet);
        menuDrenaje.add(itemInundacion);

        // 4c. Ambiente
        JMenu menuAmbiente = new JMenu(I18n.t("Ambiente"));

        JMenuItem itemClimaOnline = createItem("Clima online...", AppIcons.propertiesIcon());
        itemClimaOnline.addActionListener(e -> ar.com.catgis.climate.ClimateOnlineDownloadDialog.open());

        JMenuItem itemSuelosOnline = createItem("Suelos online...", AppIcons.basemapIcon());
        itemSuelosOnline.addActionListener(e -> OnlineSoilDownloadDialog.open());

        JMenuItem itemGeoAnalyzer = createItem("Análisis unificado GEO...", AppIcons.terrainAnalysisIcon());
        itemGeoAnalyzer.addActionListener(e ->
            ar.com.catgis.climate.UnifiedAnalysisDialog.open(MainMenuBar.this));

        JMenuItem itemRiesgoBooleano = createItem("Riesgo booleano preliminar...", AppIcons.terrainAnalysisIcon());
        itemRiesgoBooleano.addActionListener(e -> {
            if (TopographyWorkflowSupport.getAvailableRasterLayers().size() < 2) {
                NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null,
                        I18n.t("Necesitas al menos un DEM y un raster de suelos cargados para generar riesgo booleano preliminar."));
                return;
            }
            BooleanRiskDialog.open();
        });

        menuAmbiente.add(itemClimaOnline);
        menuAmbiente.add(itemSuelosOnline);
        menuAmbiente.add(itemGeoAnalyzer);
        menuAmbiente.addSeparator();
        menuAmbiente.add(itemRiesgoBooleano);

        // 4d. Redes
        JMenu menuRedes = new JMenu(I18n.t("Redes"));

        JMenuItem itemNetwork = createItem("Análisis de red...", null);
        itemNetwork.addActionListener(e -> NetworkAnalysisDialog.open());

        JMenuItem itemPgRouting = createItem("pgRouting (PostGIS)...", null);
        itemPgRouting.setToolTipText("Ruteo sobre red de calles — Requiere PostGIS + tabla de ruteo");
        itemPgRouting.addActionListener(e -> PgRoutingDialog.open());

        JMenuItem itemH3Binning = createItem("H3 hexagonal binning...", null);
        itemH3Binning.setToolTipText("Indexación espacial H3 de Uber — Experimental");
        itemH3Binning.addActionListener(e -> H3BinningDialog.open());

        menuRedes.add(itemNetwork);
        menuRedes.add(itemPgRouting);
        menuRedes.add(itemH3Binning);

        // 4e. Teledetección
        JMenu menuTeledeteccion = new JMenu(I18n.t("Teledetección"));

        JMenuItem itemSpectral = createItem("Índices (NDVI, NDWI, EVI...)", null);
        itemSpectral.setToolTipText("Calcular índices espectrales desde bandas raster");
        itemSpectral.addActionListener(e -> SpectralIndexDialog.open());

        JMenuItem itemSmileML = createItem("Clasificación ML (Smile)...", null);
        itemSmileML.setToolTipText("Clasificación supervisada con Machine Learning — Experimental");
        itemSmileML.addActionListener(e -> SmileClassificationDialog.open());

        JMenuItem itemRasterCalc = createItem("Calculadora raster...", null);
        itemRasterCalc.addActionListener(e -> ar.com.catgis.analysis.raster.RasterCalculatorDialog.open());

        JMenuItem itemBatch = createItem("Procesamiento por lotes...", null);
        itemBatch.addActionListener(e -> BatchProcessorDialog.open());

        menuTeledeteccion.add(itemSpectral);
        menuTeledeteccion.add(itemSmileML);
        menuTeledeteccion.add(itemRasterCalc);
        menuTeledeteccion.add(itemBatch);

        // 4f. Avanzado
        JMenu menuAvanzado = new JMenu(I18n.t("Avanzado"));

        // Topology
        JMenu menuTopologia = new JMenu(I18n.t("Topología"));
        JMenuItem itemTopologyNoGaps = createItem("Validar gaps entre polígonos", null);
        itemTopologyNoGaps.addActionListener(e -> runTopologyValidation("NO_GAPS"));
        menuTopologia.add(itemTopologyNoGaps);
        JMenuItem itemTopologyNoOverlaps = createItem("Validar superposiciones", null);
        itemTopologyNoOverlaps.addActionListener(e -> runTopologyValidation("NO_OVERLAPS"));
        menuTopologia.add(itemTopologyNoOverlaps);
        JMenuItem itemTopologyNoSelfInt = createItem("Validar geometrías inválidas", null);
        itemTopologyNoSelfInt.addActionListener(e -> runTopologyValidation("NO_SELF_INTERSECTION"));
        menuTopologia.add(itemTopologyNoSelfInt);
        JMenuItem itemTopologyLineConn = createItem("Validar conectividad de líneas", null);
        itemTopologyLineConn.addActionListener(e -> runTopologyValidation("NO_DANGLES"));
        menuTopologia.add(itemTopologyLineConn);

        JMenuItem itemGeoprocesamiento = createItem("Geoprocesamiento...", null);
        itemGeoprocesamiento.addActionListener(e -> ar.com.catgis.analysis.vector.GeoprocessingAssistantDialog.open());

        JMenuItem itemExportPostgis = createItem("Exportar capa a PostGIS...", AppIcons.exportIcon());
        itemExportPostgis.addActionListener(e -> PostgisDataSourceAction.exportSelectedLayerToPostgis());

        // Scripting (moved from old Herramientas)
        JMenu menuScripting = new JMenu("Scripting");
        JMenuItem itemRunScript = createItem("Ejecutar script Python...", null);
        itemRunScript.addActionListener(e -> runPythonScript());
        menuScripting.add(itemRunScript);

        // Modules (Plugins)
        JMenu menuModulos = new ModulesMenu();

        JMenuItem itemCatserver = createItem("Conectar CATSERVER...", AppIcons.projectIcon());
        itemCatserver.setToolTipText(I18n.t("Conectar CATSERVER a cualquier servidor PostgreSQL/PostGIS."));
        itemCatserver.addActionListener(e -> PostgisDataSourceAction.openCatserverBrowser());

        menuAvanzado.add(menuTopologia);
        menuAvanzado.add(itemGeoprocesamiento);
        menuAvanzado.addSeparator();
        menuAvanzado.add(itemCadIntegration);
        menuAvanzado.add(itemCatserver);
        menuAvanzado.add(itemExportPostgis);
        menuAvanzado.addSeparator();
        menuAvanzado.add(menuScripting);
        menuAvanzado.add(menuModulos);

        menuAnalisis.add(menuRelieve);
        menuAnalisis.add(menuDrenaje);
        menuAnalisis.add(menuAmbiente);
        menuAnalisis.add(menuRedes);
        menuAnalisis.add(menuTeledeteccion);
        menuAnalisis.add(menuAvanzado);

        // =====================================================================
        // 5. ONLINE
        // =====================================================================
        JMenu menuOnline = new JMenu(I18n.t("Online"));

        JMenuItem itemOsm = createItem("OpenStreetMap", AppIcons.basemapIcon());
        itemOsm.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_OSM));

        JMenuItem itemEsri = createItem("Esri World Imagery", AppIcons.imageryIcon());
        itemEsri.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_WORLD_IMAGERY));

        JMenuItem itemEsriTopo = createItem("Esri World Topo", AppIcons.basemapIcon());
        itemEsriTopo.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_WORLD_TOPO));

        JMenuItem itemEsriStreet = createItem("Esri World Street", AppIcons.basemapIcon());
        itemEsriStreet.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_WORLD_STREET));

        JMenuItem itemEsriGray = createItem("Esri Light Gray Canvas", AppIcons.basemapIcon());
        itemEsriGray.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_LIGHT_GRAY));

        JMenuItem itemEsriNatGeo = createItem("Esri NatGeo World Map", AppIcons.basemapIcon());
        itemEsriNatGeo.addActionListener(e -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_NATGEO));

        JMenuItem itemWms = createItem("Agregar WMS...", AppIcons.basemapIcon());
        itemWms.addActionListener(e -> AddWmsAction.openDialog());

        JMenuItem itemWfs = createItem("Agregar WFS...", AppIcons.tableIcon());
        itemWfs.addActionListener(e -> AddWfsAction.openDialog());

        JMenuItem itemDemOnline = createItem("DEM online...", AppIcons.propertiesIcon());
        itemDemOnline.addActionListener(e -> OnlineDemDownloadDialog.open());

        JMenuItem itemWcs = createItem("WCS — Descargar coberturas...", null);
        itemWcs.setToolTipText("Web Coverage Service — Requiere internet — Experimental");
        itemWcs.addActionListener(e -> WcsDialog.open());

        JMenuItem itemStac = createItem("STAC — Catálogo de assets...", null);
        itemStac.setToolTipText("SpatioTemporal Asset Catalog — Requiere internet — Experimental");
        itemStac.addActionListener(e -> StacDialog.open());

        menuOnline.add(itemOsm);
        menuOnline.add(itemEsri);
        menuOnline.add(itemEsriTopo);
        menuOnline.add(itemEsriStreet);
        menuOnline.add(itemEsriGray);
        menuOnline.add(itemEsriNatGeo);
        menuOnline.addSeparator();
        menuOnline.add(itemWms);
        menuOnline.add(itemWfs);
        menuOnline.add(itemWcs);
        menuOnline.add(itemStac);
        menuOnline.addSeparator();
        menuOnline.add(itemDemOnline);
        menuOnline.add(itemSuelosOnline);
        menuOnline.add(itemClimaOnline);
        menuOnline.addSeparator();

        // Climate visualization submenu
        JMenu menuClimaVisual = new JMenu("Clima (satélite)");
        menuClimaVisual.setToolTipText("Capas climáticas de visualización — datos satelitales NASA");

        JMenuItem itemNasaViirs = createItem("NASA VIIRS — Color real", AppIcons.imageryIcon());
        itemNasaViirs.setToolTipText("Imagen satelital diaria (Suomi-NPP) — Gratis, sin API key");
        itemNasaViirs.addActionListener(e ->
            OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_NASA_GIBS_VIIRS));

        JMenuItem itemNasaModisSst = createItem("NASA MODIS — Temp. del mar", AppIcons.imageryIcon());
        itemNasaModisSst.setToolTipText("Temperatura superficial del mar (MODIS Aqua) — Gratis, sin API key");
        itemNasaModisSst.addActionListener(e ->
            OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_NASA_GIBS_MODIS_AQUA));

        menuClimaVisual.add(itemNasaViirs);
        menuClimaVisual.add(itemNasaModisSst);
        menuOnline.add(menuClimaVisual);

        // =====================================================================
        // 6. MAPA FINAL
        // =====================================================================
        JMenu menuMapaFinal = new JMenu(I18n.t("Mapa Final"));

        JMenuItem itemCompositorCartografico = createItem("Abrir CATMAP...", AppIcons.projectIcon());
        itemCompositorCartografico.addActionListener(e -> MapLayoutComposerDialog.open());

        JMenuItem itemSimbologiaCapa = createItem("Simbología de capa...", AppIcons.propertiesIcon());
        itemSimbologiaCapa.addActionListener(e -> {
            Layer layer = CatgisDesktopApp.layersPanel != null ? AppContext.getSelectedLayer() : null;
            if (layer == null) {
                NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null,
                        I18n.t("Selecciona una capa para editar su simbología."));
                return;
            }
            LayerPropertiesDialog.open(layer);
        });

        JMenuItem itemTematicaCampo = createItem("Simbología por campo...", AppIcons.propertiesIcon());
        itemTematicaCampo.addActionListener(e -> {
            Layer layer = CatgisDesktopApp.layersPanel != null ? AppContext.getSelectedLayer() : null;
            if (layer == null) {
                NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null,
                        I18n.t("Selecciona una capa vectorial."));
                return;
            }
            CategorizedSymbologyDialog.open(layer);
        });

        JMenuItem itemEtiquetas = createItem("Etiquetas...", AppIcons.labelsIcon());
        itemEtiquetas.addActionListener(e -> {
            Layer layer = CatgisDesktopApp.layersPanel != null ? AppContext.getSelectedLayer() : null;
            if (layer == null) {
                NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null,
                        I18n.t("Selecciona una capa para configurar etiquetas."));
                return;
            }
            LayerPropertiesDialog.open(layer);
        });

        JMenuItem itemExportKml = createItem("Exportar capa a KML...", null);
        itemExportKml.addActionListener(e -> {
            Layer layer = AppContext.mapPanel() != null
                    ? AppContext.mapPanel().getSelectedLayerRef()
                    : null;
            if (layer != null) KmlExportEngine.exportLayerWithDialog(MainMenuBar.this, layer);
        });

        JMenuItem itemExportSld = createItem("Exportar capa a SLD...", null);
        itemExportSld.addActionListener(e -> {
            Layer layer = AppContext.mapPanel() != null
                    ? AppContext.mapPanel().getSelectedLayerRef()
                    : null;
            if (layer != null) {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new java.io.File(layer.getName() + ".sld"));
                if (chooser.showSaveDialog(MainMenuBar.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        ar.com.catgis.sld.SldSupport.exportToFile(layer, chooser.getSelectedFile());
                    } catch (Exception ex) {
                        NotificationManager.error(MainMenuBar.this,
                                "Error", "Error al exportar SLD: " + ex.getMessage());
                    }
                }
            }
        });

        menuMapaFinal.add(itemCompositorCartografico);
        menuMapaFinal.addSeparator();
        menuMapaFinal.add(itemSimbologiaCapa);
        menuMapaFinal.add(itemTematicaCampo);
        menuMapaFinal.add(itemEtiquetas);
        menuMapaFinal.addSeparator();
        menuMapaFinal.add(itemExportKml);
        menuMapaFinal.add(itemExportSld);

        // =====================================================================
        // 7. AYUDA
        // =====================================================================
        JMenu menuAyuda = new JMenu(I18n.t("Ayuda"));

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

        // =====================================================================
        // Add menus to bar
        // =====================================================================
        add(menuArchivo);
        add(menuEditar);
        add(menuCapas);
        add(menuAnalisis);
        add(menuOnline);
        add(menuMapaFinal);
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
        } catch (Exception ignored) { CatgisLogger.warn("MainMenuBar: operation failed", ignored); }
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
        } catch (Exception ignored) { CatgisLogger.warn("MainMenuBar: operation failed", ignored); }
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
            if (result.success()) {
                NotificationManager.info(CatgisDesktopApp.getMainFrameSafe(), "Script ejecutado", msg);
            } else {
                NotificationManager.error(CatgisDesktopApp.getMainFrameSafe(), "Error en script", msg);
            }
        }
    }

    private void runTopologyValidation(String rule) {
        Layer layer = CatgisDesktopApp.layersPanel != null ? AppContext.getSelectedLayer() : null;
        if (layer == null) {
            NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null,
                    I18n.t("Selecciona una capa para validar topología."));
            return;
        }

        java.util.List<org.geotools.api.feature.simple.SimpleFeature> features = new java.util.ArrayList<>();
        ShapefileData data = AppContext.mapPanel() != null ? AppContext.mapPanel().getShapefileData(layer) : null;
        if (data != null && data.getFeatureCollection() != null) {
            org.geotools.data.simple.SimpleFeatureCollection fc = data.getFeatureCollection();
            try (org.geotools.feature.FeatureIterator<org.geotools.api.feature.simple.SimpleFeature> it = fc.features()) {
                while (it.hasNext()) { features.add(it.next()); }
            }
        }

        if (features.isEmpty()) {
            NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null,
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

        String msg2 = result.valid()
                ? I18n.t("Validación OK: sin problemas de topología encontrados.")
                : I18n.t("Problemas encontrados: ") + result.issues().size() + "\n\n"
                + result.issues().stream().limit(5).map(i -> "- " + i.message()).reduce("", (a, b) -> a + "\n" + b);
        if (result.valid()) {
            NotificationManager.info(CatgisDesktopApp.getMainFrameSafe(), I18n.t("Validación de topología"), msg2);
        } else {
            NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), I18n.t("Validación de topología"), msg2);
        }
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
        NotificationManager.info(
                CatgisDesktopApp.getMainFrameSafe(),
                I18n.languageMenuLabel(),
                I18n.t("Idioma actualizado. Reinicia CATGIS para ver el cambio en toda la interfaz."));
    }

    private Layer getPreferredVectorLayer() {
        if (AppContext.mapPanel() != null) {
            Layer editingLayer = AppContext.mapPanel().getEditingLayerRef();
            if (editingLayer != null) {
                return editingLayer;
            }
        }

        Layer selectedLayer = CatgisDesktopApp.layersPanel != null ? AppContext.getSelectedLayer() : null;
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
