package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.List;

public class AboutCatgisDialog extends JDialog {

    private AboutCatgisDialog(Window owner) {
        super(owner, I18n.t("Acerca de CATGIS"), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        setIconImages(AppBranding.getApplicationIconImages());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContentTabs(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        WindowLayoutSupport.fitDialogToScreen(this, 760, 560, 680, 500);
        setLocationRelativeTo(owner);
    }

    public static void open() {
        AboutCatgisDialog dialog = new AboutCatgisDialog(CatgisDesktopApp.getMainFrameSafe());
        dialog.setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), getWidth(), 0, new Color(30, 58, 138));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Gold bar at bottom
                g2.setColor(new Color(217, 164, 47));
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel iconLabel = new JLabel(CatgisAppInfo.getApplicationIcon(72));
        panel.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(CatgisAppInfo.getApplicationName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        title.setForeground(Color.WHITE);

        JLabel version = new JLabel(I18n.format("Version {0}", CatgisAppInfo.getDisplayVersion()));
        version.setFont(version.getFont().deriveFont(Font.BOLD, 13f));
        version.setForeground(new Color(217, 164, 47));

        JLabel tagline = new JLabel(CatgisAppInfo.getTagline());
        tagline.setFont(tagline.getFont().deriveFont(Font.PLAIN, 12.5f));
        tagline.setForeground(new Color(148, 163, 184));

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(version);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(tagline);

        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    private JTabbedPane buildContentTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(I18n.t("Resumen"), createScrollHtmlPane(buildSummaryHtml()));
        tabs.addTab(I18n.t("Tecnologia"), createScrollHtmlPane(buildTechnologyHtml()));
        tabs.addTab(I18n.t("Complementos"), createScrollHtmlPane(buildComplementsHtml()));
        tabs.addTab(I18n.t("Creditos"), createScrollHtmlPane(buildCreditsHtml()));
        return tabs;
    }

    private JScrollPane createScrollHtmlPane(String html) {
        JEditorPane pane = new JEditorPane("text/html", html);
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        pane.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(pane);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        panel.setBackground(new Color(248, 250, 252));

        JButton closeButton = new JButton(I18n.t("Cerrar"));
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeButton.setBackground(new Color(59, 130, 246));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);
        return panel;
    }

    private String buildSummaryHtml() {
        return wrapHtml(
                section(I18n.t("Identidad"), List.of(
                        CatgisAppInfo.getAuthorLine(),
                        CatgisAppInfo.getCollaboratorLine(),
                        CatgisAppInfo.getStatusLine(),
                        CatgisAppInfo.getRevisionCycleLine(),
                        CatgisAppInfo.getFocusLine(),
                        CatgisAppInfo.getProfessionalNote()
                )) +
                section(I18n.t("Descripcion"), List.of(
                        I18n.t("CATGIS Desktop es una aplicacion GIS de escritorio orientada a cargar, organizar, editar y analizar capas geograficas dentro de un flujo unificado."),
                        I18n.t("Actualmente integra trabajo vectorial, raster, servicios web geograficos, mapas base online, geoprocesamiento inicial y composicion cartografica."),
                        I18n.t("El objetivo del producto es dar una base profesional y mantenible para trabajo GIS tecnico diario.")
                )) +
                section(I18n.t("Estado del programa"), List.of(
                        I18n.format("Version visible: {0}", CatgisAppInfo.getDisplayVersion()),
                        "Version tecnica base: " + CatgisAppInfo.getBaseVersion(),
                        CatgisAppInfo.getBetaFinalNote(),
                        I18n.t("Interfaz Swing con branding propio e iconografia integrada."),
                        I18n.t("Desarrollo en evolucion continua con foco en usabilidad, estabilidad y crecimiento modular.")
                ))
        );
    }

    private String buildTechnologyHtml() {
        return wrapHtml(
                section(I18n.t("Tecnologias principales"), CatgisAppInfo.getTechnologyLines()) +
                section(I18n.t("Capacidades tecnicas actuales"), List.of(
                        I18n.t("Edicion vectorial con snapping y bloque CAD."),
                        I18n.t("Carga local y remota: SHP, CSV, KML, GeoPackage, WMS, WFS y PostGIS."),
                        I18n.t("Mapas base online y compositor cartografico con exportacion e impresion.")
                ))
        );
    }

    private String buildComplementsHtml() {
        return wrapHtml(
                section(I18n.t("Complementos y bloques integrados"), CatgisAppInfo.getComplementLines()) +
                section(I18n.t("Enfoque del proyecto"), List.of(
                        I18n.t("Unificar en una misma aplicacion tareas que suelen repartirse entre varios programas."),
                        I18n.t("Mantener una base profesional, clara y escalable para seguir creciendo."),
                        I18n.t("Mejorar progresivamente la calidad cartografica, conectividad espacial y experiencia de usuario.")
                ))
        );
    }

    private String buildCreditsHtml() {
        return wrapHtml(
                section(I18n.t("Autor y direccion del proyecto"), CatgisAppInfo.getCreditsLines()) +
                section(I18n.t("Complementos, librerias y programas de soporte"), List.of(
                        I18n.t("GeoTools BOM 34, GT Main, GT Swing, GT Shapefile, GT GeoJSON y GT Referencing."),
                        I18n.t("GT XSD KML, GT GeoPackage, GT WFS NG, GT GeoTIFF, GT ArcGrid y GT Process Raster."),
                        I18n.t("ImageIO-Ext / GDAL para ampliar la lectura raster dentro del flujo actual."),
                        I18n.t("PostgreSQL JDBC para conexion espacial a bases PostGIS."),
                        I18n.t("Apache PDFBox para exportacion PDF y Apache POI para hojas de calculo."),
                        I18n.t("FlatLaf, FlatLaf Extras, IntelliJ Themes y JSVG para la experiencia visual."),
                        I18n.t("Log4j 2 para logging de ejecucion y soporte tecnico."),
                        I18n.t("Manual actualizado 2026 y HelpCenter integrado como ayuda embebida del producto.")
                ))
        );
    }

    private String section(String title, List<String> items) {
        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(escape(title)).append("</h2><ul>");
        for (String item : items) {
            html.append("<li>").append(escape(item)).append("</li>");
        }
        html.append("</ul>");
        return html.toString();
    }

    private String wrapHtml(String body) {
        return "<html><body style='font-family:sans-serif; font-size:13px; color:#263248; padding:14px 18px;'>"
                + body
                + "</body></html>";
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
