package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class MainToolBar extends JToolBar {

    public MainToolBar() {
        setFloatable(false);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setRollover(true);

        JButton btnAbrirProyecto = createButton(I18n.t("Abrir proyecto"), AppIcons.projectIcon());
        btnAbrirProyecto.addActionListener(e -> LoadProjectAction.loadProject());

        JButton btnAgregarCapa = createButton(I18n.t("Agregar capa al proyecto actual"), createOpenLayerIcon());
        btnAgregarCapa.addActionListener(e -> AddLayerAction.openLayer());

        JButton btnNuevaCapaVectorial = createButton(I18n.t("Crear nueva capa vectorial"), createNewVectorLayerIcon());
        btnNuevaCapaVectorial.addActionListener(e -> NewVectorLayerAction.createNewVectorLayer(null, CatgisDesktopApp.getMainFrameSafe()));

        JButton btnTablaPuntos = createButton(I18n.t("Cargar tabla externa"), AppIcons.importTableIcon());
        btnTablaPuntos.addActionListener(e -> OpenTablePointsAction.openTablePoints());

        JButton btnGuardar = createButton(I18n.t("Guardar proyecto"), AppIcons.saveIcon());
        btnGuardar.addActionListener(e -> SaveProjectAction.saveProject());

        JButton btnGuardarComo = createButton(I18n.t("Guardar proyecto como..."), AppIcons.attrCopyIcon());
        btnGuardarComo.addActionListener(e -> SaveProjectAction.saveProjectAs());

        JButton btnSalvarVista = createButton(I18n.t("Salvar vista del mapa"), createCameraIcon());
        btnSalvarVista.addActionListener(e -> SaveMapViewAction.saveCurrentView());

        JButton btnModulos = createButton(I18n.t("Gestor de modulos"), AppIcons.propertiesIcon());
        btnModulos.addActionListener(e -> ModuleManagerDialog.open());

        JButton btnZoomMas = createButton(I18n.t("Acercar"), AppIcons.zoomInIcon());
        btnZoomMas.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomIn());

        JButton btnZoomMenos = createButton(I18n.t("Alejar"), AppIcons.zoomOutIcon());
        btnZoomMenos.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomOut());

        JButton btnZoomCapa = createButton(I18n.t("Zoom a capa seleccionada"), AppIcons.zoomLayerIcon());
        btnZoomCapa.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomToSelectedLayerPublic());

        JButton btnZoomTodo = createButton(I18n.t("Zoom a todas las capas"), AppIcons.zoomAllIcon());
        btnZoomTodo.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomToAllLayers());

        JButton btnVistaAnterior = createButton(I18n.t("Vista anterior"), AppIcons.viewPreviousIcon());
        btnVistaAnterior.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomPrevious());

        JButton btnVistaSiguiente = createButton(I18n.t("Vista siguiente"), AppIcons.viewNextIcon());
        btnVistaSiguiente.addActionListener(e -> CatgisDesktopApp.mapPanel.zoomNext());

        JButton btnMover = createButton(I18n.t("Desplazar mapa"), AppIcons.panIcon());
        btnMover.addActionListener(e -> CatgisDesktopApp.mapPanel.enablePanMode());

        JButton btnIdentificar = createButton(I18n.t("Consultar entidades"), AppIcons.identifyIcon());
        btnIdentificar.addActionListener(e -> CatgisDesktopApp.mapPanel.enableIdentifyMode());

        JButton btnBuscarCoord = createButton(I18n.t("Buscar por coordenadas"), createSearchXYIcon());
        btnBuscarCoord.addActionListener(e -> GoToCoordinatesDialog.openDialog());

        JButton btnPunto = createButton(I18n.t("Dibujar puntos"), AppIcons.pointIcon());
        btnPunto.addActionListener(e -> CatgisDesktopApp.mapPanel.enableDrawPointMode());

        JButton btnMultiPunto = createButton(I18n.t("Dibujar multipunto"), AppIcons.multiPointIcon());
        btnMultiPunto.addActionListener(e -> CatgisDesktopApp.mapPanel.enableDrawMultiPointMode());

        JButton btnLinea = createButton(I18n.t("Dibujar lineas"), AppIcons.lineIcon());
        btnLinea.addActionListener(e -> CatgisDesktopApp.mapPanel.enableDrawLineMode());

        JButton btnPoligono = createButton(I18n.t("Dibujar poligonos"), AppIcons.polygonIcon());
        btnPoligono.addActionListener(e -> CatgisDesktopApp.mapPanel.enableDrawPolygonMode());

        JButton btnMedirDist = createButton(I18n.t("Medir distancia"), AppIcons.distanceIcon());
        btnMedirDist.addActionListener(e -> CatgisDesktopApp.mapPanel.enableMeasureDistanceMode());

        JButton btnMedirArea = createButton(I18n.t("Medir area"), AppIcons.areaIcon());
        btnMedirArea.addActionListener(e -> CatgisDesktopApp.mapPanel.enableMeasureAreaMode());

        JButton btnTerminar = createButton(I18n.t("Finalizar dibujo o medicion"), AppIcons.finishIcon());
        btnTerminar.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel.isMeasurementActive()) {
                CatgisDesktopApp.mapPanel.finishCurrentMeasurement();
            } else {
                CatgisDesktopApp.mapPanel.closeCurrentDrawingSession();
            }
        });

        JButton btnCancelar = createButton(I18n.t("Cancelar dibujo o medicion"), AppIcons.cancelIcon());
        btnCancelar.addActionListener(e -> {
            if (CatgisDesktopApp.mapPanel.isMeasurementActive()) {
                CatgisDesktopApp.mapPanel.cancelCurrentMeasurement();
            } else {
                CatgisDesktopApp.mapPanel.cancelCurrentDrawing();
            }
        });

        JButton btnTabla = createButton(I18n.t("Abrir tabla de atributos"), AppIcons.tableIcon());
        btnTabla.addActionListener(e -> OpenAttributeTableAction.openAttributeTable());

        JButton btnCRS = createButton(I18n.t("Conversor de coordenadas"), createConverterIcon());
        btnCRS.addActionListener(e -> CoordinateConverterDialog.openDialog());

        JButton btnProjectCRS = createButton(I18n.t("Definir CRS del proyecto"), createProjectCrsIcon());
        btnProjectCRS.addActionListener(e -> ProjectCRSDialog.openDialog());

        // Bloque global de proyecto y datos
        add(createLabeledButton(btnAbrirProyecto, "Abrir"));
        add(createLabeledButton(btnAgregarCapa, "Capa"));
        add(createLabeledButton(btnNuevaCapaVectorial, "Nueva"));
        add(createLabeledButton(btnGuardar, "Guardar"));
        addSeparator();
        // Bloque de navegacion del mapa
        add(createLabeledButton(btnZoomMas, "+"));
        add(createLabeledButton(btnZoomMenos, "-"));
        add(createLabeledButton(btnZoomCapa, "Zoom capa"));
        add(createLabeledButton(btnZoomTodo, "Todo"));
        add(createLabeledButton(btnMover, "Mover"));
        add(createLabeledButton(btnIdentificar, "Info"));
        add(createLabeledButton(btnBuscarCoord, "Buscar"));
        addSeparator();
        // Bloque de dibujo
        add(createLabeledButton(btnPunto, "Punto"));
        add(createLabeledButton(btnLinea, "Linea"));
        add(createLabeledButton(btnPoligono, "Poligono"));
        add(createLabeledButton(btnMedirDist, "Distancia"));
        add(createLabeledButton(btnMedirArea, "Area"));
        add(createLabeledButton(btnTerminar, I18n.t("Terminar")));
        add(createLabeledButton(btnCancelar, I18n.t("Cancelar")));
        addSeparator();
        // Bloque de herramientas
        add(createLabeledButton(btnTabla, "Tabla"));
        add(createLabeledButton(btnProjectCRS, "CRS"));
        add(createLabeledButton(btnCRS, "Coord"));
        add(createLabeledButton(btnModulos, "Modulos"));
        addSeparator();
        // Estilo rapido
        add(createToggleStyleButton());
        addSeparator();
        // Analisis
        add(createAnalysisButton());
    }

    private JButton createButton(String tooltip, Icon icon) {
        JButton button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setMargin(new Insets(4, 4, 4, 4));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 10f));
        button.setPreferredSize(new Dimension(40, 40));
        return button;
    }

    private JButton createLabeledButton(JButton original, String label) {
        JButton labeled = new JButton(label);
        labeled.setIcon(original.getIcon());
        labeled.setToolTipText(original.getToolTipText());
        labeled.setFocusable(false);
        labeled.setVerticalTextPosition(SwingConstants.BOTTOM);
        labeled.setHorizontalTextPosition(SwingConstants.CENTER);
        labeled.setFont(new Font("SansSerif", Font.PLAIN, 9));
        labeled.setMargin(new Insets(2, 4, 2, 4));
        labeled.setPreferredSize(new Dimension(52, 48));
        labeled.setOpaque(true);
        labeled.setContentAreaFilled(true);
        labeled.setBackground(new Color(0xF7F8FA));
        labeled.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        labeled.addActionListener(original.getActionListeners().length > 0 ? original.getActionListeners()[0] : null);

        // Hover shadow effect
        labeled.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                labeled.setBackground(new Color(0xE0E7EE));
                labeled.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xB0BEC5), 1),
                    BorderFactory.createEmptyBorder(1, 3, 1, 3)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                labeled.setBackground(new Color(0xF7F8FA));
                labeled.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            }
        });

        return labeled;
    }

    private JButton createToggleStyleButton() {
        JButton btn = new JButton("Estilo") {
            @Override
            protected void paintComponent(Graphics g) {
                int status = CatgisDesktopApp.quickStylePanelVisible ? 1 : 0;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (status == 1) {
                    g2.setColor(new Color(46, 204, 113));
                } else {
                    g2.setColor(new Color(149, 165, 166));
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
                String text = getText();
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(text);
                int tx = (getWidth() - tw) / 2;
                int ty = (getHeight() + fm.getAscent() / 2) / 2;
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(56, 44));
        btn.setToolTipText("Panel de estilo rapido - toggle");
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.addActionListener(e -> CatgisDesktopApp.toggleQuickStylePanel());
        return btn;
    }

    private JButton createAnalysisButton() {
        JButton btn = new JButton("Analisis") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(41, 128, 185));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
                String text = getText();
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(text);
                int tx = (getWidth() - tw) / 2;
                int ty = (getHeight() + fm.getAscent() / 2) / 2;
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(68, 44));
        btn.setToolTipText("Consola de analisis unificada - combine capas online y locales");
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.addActionListener(e -> AnalysisConsoleDialog.open());
        return btn;
    }

    private Icon createOpenLayerIcon() {
        int w = 18, h = 18;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(54, 114, 196));
        g.fillRoundRect(2, 6, 14, 9, 3, 3);
        g.setColor(new Color(86, 142, 217));
        g.fillRoundRect(3, 4, 6, 4, 2, 2);
        g.setColor(new Color(243, 249, 255));
        g.fillRect(4, 8, 10, 5);
        g.setColor(new Color(34, 139, 34));
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(13, 2, 13, 8);
        g.drawLine(10, 5, 16, 5);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createNewVectorLayerIcon() {
        int w = 18, h = 18;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(45, 105, 185));
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(2, 3, 11, 12, 3, 3);
        g.setColor(new Color(33, 150, 83));
        g.setStroke(new BasicStroke(2.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(5, 11, 7, 8);
        g.drawLine(7, 8, 10, 10);
        g.fillOval(4, 10, 2, 2);
        g.fillOval(6, 7, 2, 2);
        g.fillOval(9, 9, 2, 2);
        g.setColor(new Color(34, 139, 34));
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(14, 5, 14, 15);
        g.drawLine(9, 10, 17, 10);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createCameraIcon() {
        int w = 18, h = 18;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(60, 68, 80));
        g.fillRoundRect(2, 5, 14, 10, 3, 3);
        g.fillRoundRect(5, 3, 4, 3, 2, 2);
        g.setColor(new Color(220, 226, 235));
        g.fillOval(6, 7, 6, 6);
        g.setColor(new Color(90, 120, 170));
        g.drawOval(6, 7, 6, 6);
        g.setColor(new Color(255, 196, 0));
        g.fillOval(13, 7, 2, 2);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createSearchXYIcon() {
        int w = 18, h = 18;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(45, 95, 180));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(2, 2, 8, 8);
        g.drawLine(9, 9, 14, 14);
        g.setColor(new Color(55, 55, 55));
        g.setFont(new Font("SansSerif", Font.BOLD, 7));
        g.drawString("X", 11, 8);
        g.drawString("Y", 11, 15);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createConverterIcon() {
        int w = 18, h = 18;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(65, 105, 225));
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(3, 6, 14, 6);
        g.drawLine(11, 3, 14, 6);
        g.drawLine(11, 9, 14, 6);

        g.setColor(new Color(220, 20, 60));
        g.drawLine(14, 12, 3, 12);
        g.drawLine(6, 9, 3, 12);
        g.drawLine(6, 15, 3, 12);

        g.setColor(new Color(60, 60, 60));
        g.setFont(new Font("SansSerif", Font.BOLD, 6));
        g.drawString("XY", 6, 17);

        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createProjectCrsIcon() {
        int w = 18, h = 18;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(33, 120, 210));
        g.fillRoundRect(2, 3, 14, 12, 4, 4);

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.4f));
        g.drawLine(6, 3, 6, 15);
        g.drawLine(11, 3, 11, 15);
        g.drawLine(2, 7, 16, 7);
        g.drawLine(2, 11, 16, 11);

        g.setColor(new Color(255, 196, 0));
        g.fillOval(11, 2, 5, 5);

        g.dispose();
        return new ImageIcon(img);
    }
}
