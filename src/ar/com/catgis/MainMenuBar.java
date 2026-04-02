package ar.com.catgis;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
        JMenu menuArchivo = new JMenu("Archivo");
        JMenu menuEdicion = new JMenu("Edicion");
        JMenu menuVista = new JMenu("Vista");
        JMenu menuHerramientas = new JMenu("Herramientas");
        JMenu menuVentana = new JMenu("Ventana");
        JMenu menuProyecto = new JMenu("Proyecto");
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenuItem itemNuevoProyecto = createItem("Nuevo proyecto", createNewProjectIcon());
        itemNuevoProyecto.addActionListener(e -> NewProjectAction.newProject());

        JMenuItem itemAbrirProyecto = createItem("Abrir proyecto", createOpenProjectIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, menuMask));
        itemAbrirProyecto.addActionListener(e -> LoadProjectAction.loadProject());

        JMenuItem itemAgregarCapa = createItem("Agregar capa", createOpenLayerIcon());
        itemAgregarCapa.addActionListener(e -> AddLayerAction.openLayer());

        JMenuItem itemNuevaCapaVectorial = createItem("Nueva capa vectorial", createNewVectorLayerIcon());
        itemNuevaCapaVectorial.addActionListener(e -> NewVectorLayerAction.createNewVectorLayer(null, CatgisDesktopApp.getMainFrameSafe()));

        JMenuItem itemAbrirTabla = createItem("Importar tabla de puntos", AppIcons.importTableIcon());
        itemAbrirTabla.addActionListener(e -> OpenTablePointsAction.openTablePoints());

        JMenuItem itemGuardarProyecto = createItem("Guardar proyecto", AppIcons.saveIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask));
        itemGuardarProyecto.addActionListener(e -> SaveProjectAction.saveProject());

        JMenuItem itemGuardarProyectoComo = createItem("Guardar proyecto como...", AppIcons.attrCopyIcon(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask | InputEvent.SHIFT_DOWN_MASK));
        itemGuardarProyectoComo.addActionListener(e -> SaveProjectAction.saveProjectAs());

        JMenuItem itemSalvarVista = createItem("Salvar vista del mapa", createCameraIcon());
        itemSalvarVista.addActionListener(e -> SaveMapViewAction.saveCurrentView());

        menuArchivo.add(itemNuevoProyecto);
        menuArchivo.add(itemAbrirProyecto);
        menuArchivo.add(itemAgregarCapa);
        menuArchivo.add(itemNuevaCapaVectorial);
        menuArchivo.add(itemAbrirTabla);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardarProyecto);
        menuArchivo.add(itemGuardarProyectoComo);
        menuArchivo.add(itemSalvarVista);

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

        menuVista.add(itemZoomMas);
        menuVista.add(itemZoomMenos);
        menuVista.addSeparator();
        menuVista.add(itemZoomCapa);
        menuVista.add(itemZoomTodo);
        menuVista.addSeparator();
        menuVista.add(itemVistaAnterior);
        menuVista.add(itemVistaSiguiente);

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
        menuHerramientas.add(itemPunto);
        menuHerramientas.add(itemMultiPunto);
        menuHerramientas.add(itemLinea);
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

        menuVentana.add(itemVentanaPrincipal);
        menuVentana.addSeparator();
        menuVentana.add(itemVentanaTabla);
        menuVentana.add(itemVentanaConsultas);
        menuVentana.add(itemVentanaTablasAbiertas);

        menuProyecto.add(itemRenombrarProyecto);
        menuProyecto.add(itemProjectCRS);

        add(menuArchivo);
        add(menuEdicion);
        add(menuVista);
        add(menuHerramientas);
        add(menuVentana);
        add(menuProyecto);
    }

    private JMenuItem createItem(String text, Icon icon) {
        return createItem(text, icon, null);
    }

    private JMenuItem createItem(String text, Icon icon, KeyStroke accelerator) {
        JMenuItem item = new JMenuItem(text, icon);
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        return item;
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
        g.drawString("Σ", 4, 12);
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
}
