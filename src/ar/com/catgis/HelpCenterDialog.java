package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.net.URL;

public class HelpCenterDialog extends JDialog {

    private static HelpCenterDialog instance;

    private final List<HelpTopic> topics;
    private final JList<HelpTopic> topicsList;
    private final JEditorPane contentPane;

    private HelpCenterDialog(Window owner) {
        super(owner, I18n.t("Panel de ayuda"), ModalityType.MODELESS);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        WindowLayoutSupport.fitDialogToScreen(this, 980, 680, 820, 560);
        setLocationRelativeTo(owner);
        setIconImages(AppBranding.getApplicationIconImages());

        topics = buildTopics();
        topicsList = new JList<>(topics.toArray(new HelpTopic[0]));
        contentPane = new JEditorPane("text/html", "");
        contentPane.setEditable(false);
        contentPane.setOpaque(false);
        contentPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        contentPane.setFont(new Font("SansSerif", Font.PLAIN, 13));
        contentPane.addHyperlinkListener(this::handleHyperlinkActivation);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        if (!topics.isEmpty()) {
            topicsList.setSelectedIndex(0);
            updateTopicContent(topics.get(0));
        }
    }

    public static void open() {
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        if (instance == null || !instance.isDisplayable()) {
            instance = new HelpCenterDialog(owner);
        }
        instance.setLocationRelativeTo(owner);
        instance.setVisible(true);
        instance.toFront();
        instance.requestFocus();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(214, 221, 232)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        panel.setBackground(new Color(244, 248, 255));

        JLabel iconLabel = new JLabel(CatgisAppInfo.getApplicationIcon(68));
        panel.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(I18n.t("Centro de ayuda CATGIS"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 23f));
        title.setForeground(new Color(24, 39, 72));

        JLabel subtitle = new JLabel(I18n.t("Guía rápida integrada para cargar datos, editar, consultar y producir mapas."));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12.5f));
        subtitle.setForeground(new Color(91, 102, 116));

        JLabel note = new JLabel(I18n.t("Incluye flujos básicos, bloques clave de la interfaz y rutas cortas para trabajar más rápido."));
        note.setFont(note.getFont().deriveFont(Font.BOLD, 11.5f));
        note.setForeground(new Color(35, 120, 210));

        JLabel release = new JLabel(CatgisAppInfo.getDisplayVersion());
        release.setFont(release.getFont().deriveFont(Font.BOLD, 11.5f));
        release.setForeground(new Color(24, 92, 180));

        JLabel collaborator = new JLabel(CatgisAppInfo.getCollaboratorLine());
        collaborator.setFont(collaborator.getFont().deriveFont(Font.PLAIN, 11.3f));
        collaborator.setForeground(new Color(91, 102, 116));

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(note);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(release);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(collaborator);

        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    private Component buildBody() {
        topicsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topicsList.setFixedCellHeight(58);
        topicsList.setCellRenderer(new HelpTopicRenderer());
        topicsList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        topicsList.setBackground(new Color(248, 250, 253));
        topicsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                HelpTopic topic = topicsList.getSelectedValue();
                if (topic != null) {
                    updateTopicContent(topic);
                }
            }
        });

        JScrollPane topicsScroll = new JScrollPane(topicsList);
        topicsScroll.setBorder(BorderFactory.createEmptyBorder());
        topicsScroll.getViewport().setBackground(new Color(248, 250, 253));
        topicsScroll.setPreferredSize(new Dimension(270, 100));

        JScrollPane contentScroll = new JScrollPane(contentPane);
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.getViewport().setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, topicsScroll, contentScroll);
        splitPane.setDividerLocation(270);
        splitPane.setResizeWeight(0.0);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return splitPane;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        panel.setBackground(Color.WHITE);

        JLabel footerLabel = new JLabel(I18n.t("Ayuda integrada para orientarte sin salir de la aplicación."));
        footerLabel.setForeground(new Color(96, 106, 118));
        footerLabel.setFont(footerLabel.getFont().deriveFont(Font.PLAIN, 11.5f));
        panel.add(footerLabel, BorderLayout.WEST);

        JButton closeButton = new JButton(I18n.t("Cerrar"));
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton, BorderLayout.EAST);
        return panel;
    }

    private void updateTopicContent(HelpTopic topic) {
        HTMLEditorKit editorKit = new HTMLEditorKit();
        HTMLDocument document = (HTMLDocument) editorKit.createDefaultDocument();
        URL helpBase = HelpCenterDialog.class.getResource("/help/");
        if (helpBase != null) {
            document.setBase(helpBase);
        }
        contentPane.setEditorKit(editorKit);
        contentPane.setDocument(document);
        contentPane.setText(buildTopicHtml(topic));
        contentPane.setCaretPosition(0);
    }

    private String buildTopicHtml(HelpTopic topic) {
        if (topic.rawHtml()) {
            return topic.html();
        }
        return "<html><body style='font-family:sans-serif; font-size:13px; color:#243146; padding:18px 22px;'>"
                + "<div style='color:#2478d2; font-size:11px; font-weight:bold; letter-spacing:0.3px;'>" + escape(I18n.t("GUÍA RÁPIDA")) + "</div>"
                + "<h1 style='margin-top:6px; margin-bottom:4px; font-size:25px; color:#162544;'>" + escape(topic.title()) + "</h1>"
                + "<div style='margin-bottom:18px; color:#617084; font-size:13px;'>" + escape(topic.subtitle()) + "</div>"
                + topic.html()
                + "</body></html>";
    }

    private List<HelpTopic> buildTopics() {
        return List.of(
                new HelpTopic(
                        "Manual profesional 2026 final",
                        "Versión oficial embebida en PDF y DOCX, accesible desde el HelpCenter.",
                        loadHelpHtmlResource("/help/CATGIS_Manual_Profesional_2026_FINAL.html"),
                        true
                ),
                new HelpTopic(
                        "Guía integrada 2026",
                        "Versión HTML de consulta rápida con glosario de iconos y flujos recomendados.",
                        loadHelpHtmlResource("/help/CATGIS_Manual_Integrado_2026.html"),
                        true
                ),
                new HelpTopic(
                        "Estado de esta beta final",
                        "Versión visible, ciclo de revisiones y créditos de la etapa de cierre.",
                        section("Versión y cierre de release",
                                bulletList(
                                        "Versión visible actual: " + CatgisAppInfo.getDisplayVersion(),
                                        "Versión técnica base: " + CatgisAppInfo.getBaseVersion(),
                                        CatgisAppInfo.getRevisionCycleLine(),
                                        CatgisAppInfo.getCollaboratorLine(),
                                        CatgisAppInfo.getStatusLine()
                                ),
                                paragraph(CatgisAppInfo.getBetaFinalNote())
                        ),
                        false
                ),
                new HelpTopic(
                        I18n.t("Qué es CATGIS"),
                        I18n.t("Resumen del enfoque de la aplicación y sus bloques principales."),
                        section(I18n.t("Vision general"),
                                paragraph(I18n.t("CATGIS Desktop es una aplicacion GIS de escritorio orientada a trabajo tecnico diario: carga de datos, edicion vectorial, servicios remotos, analisis visual y composicion cartografica.")),
                                bulletList(
                                        I18n.t("Gestor de capas a la izquierda con orden visual claro: arriba = frente, abajo = fondo."),
                                        I18n.t("Mapa central para explorar, editar, medir y revisar informacion."),
                                        I18n.t("Bloques visibles para mapas finales y conexiones online."),
                                        I18n.t("Base modular mantenible para seguir creciendo.")
                                ),
                                paragraph(I18n.t("La idea es que puedas concentrar en una misma herramienta tareas que normalmente se reparten entre varios programas."))
                        ),
                        false
                ),
                new HelpTopic(
                        I18n.t("Cargar capas y proyectos"),
                        I18n.t("Como abrir informacion local, remota y proyectos .catgis."),
                        section(I18n.t("Carga de datos"),
                                bulletList(
                                        I18n.t("Archivo > Nuevo proyecto crea un espacio limpio de trabajo."),
                                        I18n.t("Archivo > Abrir proyecto recupera un .catgis con sus capas, orden y configuraciones."),
                                        I18n.t("Archivo > Agregar capa permite incorporar datos vectoriales, raster y otros formatos soportados.")
                                ),
                                section(I18n.t("Fuentes disponibles"),
                                        bulletList(
                                                I18n.t("Locales: SHP, CSV, KML, GeoPackage y raster."),
                                                I18n.t("Servicios: WMS, WFS y PostGIS en la etapa actual."),
                                                I18n.t("Mapas base online: OpenStreetMap y Esri World Imagery.")
                                        )
                                )
                        ),
                        false
                ),
                new HelpTopic(
                        I18n.t("Orden y gestion de capas"),
                        I18n.t("Como organizar lo que se ve en el mapa y en que orden se dibuja."),
                        section(I18n.t("Gestor de proyecto"),
                                paragraph(I18n.t("El panel izquierdo es la referencia visual principal del proyecto. Lo que esta mas arriba se dibuja por encima; lo que esta mas abajo queda al fondo.")),
                                bulletList(
                                        I18n.t("Activa o desactiva capas desde sus checks."),
                                        I18n.t("Reordena capas para controlar el apilamiento visual."),
                                        I18n.t("Selecciona una capa para abrir su tabla, simbologia o consultas."),
                                        I18n.t("Mantiene el orden al guardar y reabrir el proyecto.")
                                )
                        ),
                        false
                ),
                new HelpTopic(
                        I18n.t("Edicion vectorial y snapping"),
                        I18n.t("Flujo basico para dibujar, ajustar y guardar geometria."),
                        section(I18n.t("Edicion"),
                                bulletList(
                                        I18n.t("Herramientas > Dibujar punto, linea, rectangulo o poligono para crear nuevas entidades."),
                                        I18n.t("Edicion incluye copiar, pegar, borrar, deshacer, rehacer, unir vertices y cortar geometria."),
                                        I18n.t("CAD concentra operaciones como continuar linea, paralela, perpendicular, circulo y rectangulo.")
                                ),
                                section(I18n.t("Snapping"),
                                        paragraph(I18n.t("El boton SNAP de la interfaz permite ajustar geometria a vertices y segmentos para dibujar con precision.")),
                                        bulletList(
                                                I18n.t("Activalo antes de editar si necesitas exactitud geometrica."),
                                                I18n.t("Combinalo con las herramientas CAD para una construccion mas precisa."),
                                                I18n.t("Guarda la edicion cuando termines para llevar los cambios a disco.")
                                        )
                                )
                        ),
                        false
                ),
                new HelpTopic(
                        I18n.t("Datos remotos y conexiones online"),
                        I18n.t("Resumen del trabajo con mapas base y servicios geograficos."),
                        section(I18n.t("Mapas base online"),
                                bulletList(
                                        I18n.t("La franja de conexiones online permite agregar rapidamente OSM, Esri y otros accesos visibles."),
                                        I18n.t("Estos mapas sirven como contexto visual y se integran con el resto de capas del proyecto.")
                                ),
                                section(I18n.t("Servicios geograficos"),
                                        bulletList(
                                                I18n.t("WMS: consulta capacidades, elige capas y agregalas como imagen remota real."),
                                                I18n.t("WFS: carga feature types como capas vectoriales en modo lectura."),
                                                I18n.t("GeoPackage y PostGIS: fuentes estructuradas para seguir ampliando el trabajo GIS.")
                                        )
                                )
                        ),
                        false
                ),
                new HelpTopic(
                        I18n.t("Composicion cartografica"),
                        I18n.t("Como pasar del mapa de trabajo a un mapa final exportable."),
                        section(I18n.t("Mapas finales"),
                                bulletList(
                                        I18n.t("Usa Cartografia > Abrir compositor cartografico o el bloque Mapas finales."),
                                        I18n.t("Configura formato de hoja, orientacion, leyenda, escala, norte, cartucho y logo."),
                                        I18n.t("Puedes mover y redimensionar elementos para maquetar mejor la salida.")
                                ),
                                section(I18n.t("Salida"),
                                        bulletList(
                                                I18n.t("Exporta a imagen o PDF desde el compositor."),
                                                I18n.t("Imprime directamente desde la misma ventana."),
                                                I18n.t("Ajusta el layout para aprovechar mejor el espacio del mapa.")
                                        )
                                )
                        ),
                        false
                ),
                new HelpTopic(
                        I18n.t("Flujo rapido y atajos"),
                        I18n.t("Una hoja de ruta corta para trabajar mas rapido dentro de CATGIS."),
                        section(I18n.t("Flujo recomendado"),
                                bulletList(
                                        I18n.t("1. Crea o abre un proyecto."),
                                        I18n.t("2. Carga capas locales o remotas."),
                                        I18n.t("3. Ordena visualmente el gestor de proyecto."),
                                        I18n.t("4. Edita geometria con snapping si hace falta."),
                                        I18n.t("5. Revisa tabla de atributos y consultas."),
                                        I18n.t("6. Abre el compositor para producir el mapa final.")
                                ),
                                section(I18n.t("Atajos utiles"),
                                        bulletList(
                                                I18n.t("Ctrl + O abre proyecto."),
                                                I18n.t("Ctrl + S guarda proyecto."),
                                                I18n.t("Ctrl + X / Ctrl + C / Ctrl + V para cortar, copiar y pegar seleccion."),
                                                I18n.t("Delete borra la seleccion."),
                                                I18n.t("F1 abre este panel de ayuda.")
                                        )
                                )
                        ),
                        false
                )
        );
    }

    private String loadHelpHtmlResource(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return buildMissingManualHtml("No se definio un recurso de ayuda para esta seccion.");
        }
        try (InputStream in = HelpCenterDialog.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return buildMissingManualHtml("No se encontro el recurso de ayuda: " + resourcePath);
            }
            byte[] bytes = in.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return buildMissingManualHtml("No se pudo cargar el recurso de ayuda: " + resourcePath + ". " + ex.getMessage());
        }
    }

    private String buildMissingManualHtml(String reason) {
        return "<html><body style='font-family:sans-serif; font-size:13px; color:#243146; padding:18px 22px;'>"
                + "<h1 style='margin-top:4px; margin-bottom:10px; font-size:25px; color:#162544;'>Manual integrado no disponible</h1>"
                + "<p style='line-height:1.58; margin:8px 0 10px 0;'>"
                + escape(reason)
                + "</p>"
                + "<p style='line-height:1.58; margin:8px 0 10px 0;'>"
                + escape("La ayuda rapida sigue disponible en las demas secciones del HelpCenter.")
                + "</p>"
                + "</body></html>";
    }

    private void handleHyperlinkActivation(HyperlinkEvent event) {
        if (event == null || event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
            return;
        }
        String description = event.getDescription();
        if (description != null && description.startsWith("catgis-help:")) {
            openSpecialHelpLink(description.substring("catgis-help:".length()));
            return;
        }
        if (event.getURL() != null) {
            openExternalUrl(event.getURL());
        }
    }

    private void openSpecialHelpLink(String action) {
        if (action == null || action.isBlank()) {
            return;
        }
        switch (action) {
            case "manual-final-pdf" ->
                    openBundledHelpDocument("/help/docs/CATGIS_Manual_Profesional_2026_FINAL.pdf",
                            "CATGIS_Manual_Profesional_2026_FINAL.pdf");
            case "manual-final-docx" ->
                    openBundledHelpDocument("/help/docs/CATGIS_Manual_Profesional_2026_FINAL.docx",
                            "CATGIS_Manual_Profesional_2026_FINAL.docx");
            default -> showHelpError("No se reconoció el enlace solicitado: " + action);
        }
    }

    private void openExternalUrl(URL url) {
        try {
            if (url == null) {
                return;
            }
            if (!Desktop.isDesktopSupported()) {
                showHelpError("La apertura externa no está disponible en este entorno.");
                return;
            }
            Desktop.getDesktop().browse(url.toURI());
        } catch (Exception ex) {
            showHelpError("No se pudo abrir el enlace externo. " + ex.getMessage());
        }
    }

    private void openBundledHelpDocument(String resourcePath, String fileName) {
        try (InputStream in = HelpCenterDialog.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                showHelpError("No se encontró el documento embebido: " + fileName);
                return;
            }
            Path helpTempDir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "catgis-helpcenter"));
            Path target = helpTempDir.resolve(fileName);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            File file = target.toFile();
            file.deleteOnExit();

            if (!Desktop.isDesktopSupported()) {
                showHelpError("El documento fue extraído en: " + file.getAbsolutePath());
                return;
            }
            Desktop.getDesktop().open(file);
        } catch (Exception ex) {
            showHelpError("No se pudo abrir el documento solicitado. " + ex.getMessage());
        }
    }

    private void showHelpError(String message) {
        JOptionPane.showMessageDialog(this, message, "Centro de ayuda CATGIS", JOptionPane.INFORMATION_MESSAGE);
    }

    private String section(String title, String... blocks) {
        StringBuilder html = new StringBuilder();
        html.append("<h2 style='margin-top:16px; margin-bottom:6px; color:#162544;'>")
                .append(escape(title))
                .append("</h2>");
        for (String block : blocks) {
            html.append(block);
        }
        return html.toString();
    }

    private String paragraph(String text) {
        return "<p style='line-height:1.58; margin:8px 0 10px 0;'>" + escape(text) + "</p>";
    }

    private String bulletList(String... items) {
        StringBuilder html = new StringBuilder("<ul style='margin-top:8px; margin-bottom:10px; padding-left:18px; line-height:1.55;'>");
        for (String item : items) {
            html.append("<li style='margin-bottom:6px;'>").append(escape(item)).append("</li>");
        }
        html.append("</ul>");
        return html.toString();
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private record HelpTopic(String title, String subtitle, String html, boolean rawHtml) {
        @Override
        public String toString() {
            return title;
        }
    }

    private static final class HelpTopicRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof HelpTopic topic) {
                label.setText("<html><div style='font-weight:bold; margin-bottom:2px;'>" + escapeForHtml(topic.title())
                        + "</div><div style='font-size:10px; color:" + (isSelected ? "#dceeff" : "#6b7482") + ";'>"
                        + escapeForHtml(topic.subtitle()) + "</div></html>");
            }
            label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            if (isSelected) {
                label.setBackground(new Color(35, 120, 210));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(new Color(248, 250, 253));
                label.setForeground(new Color(34, 44, 62));
            }
            return label;
        }

        private static String escapeForHtml(String text) {
            if (text == null) {
                return "";
            }
            return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
