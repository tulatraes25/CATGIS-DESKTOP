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

        setSize(760, 560);
        setMinimumSize(new Dimension(700, 520));
        setLocationRelativeTo(owner);
    }

    public static void open() {
        AboutCatgisDialog dialog = new AboutCatgisDialog(CatgisDesktopApp.getMainFrameSafe());
        dialog.setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(214, 221, 232)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        panel.setBackground(new Color(245, 249, 255));

        JLabel iconLabel = new JLabel(CatgisAppInfo.getApplicationIcon(72));
        panel.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(CatgisAppInfo.getApplicationName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(new Color(22, 37, 68));

        JLabel version = new JLabel(I18n.format("Version {0}", CatgisAppInfo.getDisplayVersion()));
        version.setFont(version.getFont().deriveFont(Font.BOLD, 12.5f));
        version.setForeground(new Color(35, 120, 210));

        JLabel tagline = new JLabel(CatgisAppInfo.getTagline());
        tagline.setFont(tagline.getFont().deriveFont(Font.PLAIN, 12.5f));
        tagline.setForeground(new Color(89, 98, 112));

        JLabel author = new JLabel("<html><span style='color:#2e3f5d;'>" + escape(CatgisAppInfo.getAuthorLine()) + "</span></html>");
        author.setFont(author.getFont().deriveFont(Font.PLAIN, 11.5f));

        JLabel collaborator = new JLabel("<html><span style='color:#4b5563;'>" + escape(CatgisAppInfo.getCollaboratorLine()) + "</span></html>");
        collaborator.setFont(collaborator.getFont().deriveFont(Font.PLAIN, 11.2f));

        JLabel revision = new JLabel("<html><span style='color:#4b5563;'>" + escape(CatgisAppInfo.getRevisionCycleLine()) + "</span></html>");
        revision.setFont(revision.getFont().deriveFont(Font.PLAIN, 11.2f));

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(version);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(tagline);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(author);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(collaborator);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(revision);

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
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 224, 230)));
        panel.setBackground(Color.WHITE);

        JButton closeButton = new JButton(I18n.t("Cerrar"));
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
