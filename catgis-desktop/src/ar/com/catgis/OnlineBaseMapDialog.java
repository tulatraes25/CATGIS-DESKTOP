package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class OnlineBaseMapDialog extends JDialog {

    private final JList<OnlineRasterSource> sourceList;
    private final JTextArea infoArea;
    private final JTextArea credentialsArea;

    private OnlineBaseMapDialog(Window owner) {
        super(owner, "Mapas base online", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        content.add(buildHeader(), BorderLayout.NORTH);

        sourceList = new JList<>(OnlineMapCatalog.getBaseMaps().toArray(new OnlineRasterSource[0]));
        sourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sourceList.setVisibleRowCount(10);
        sourceList.setFixedCellHeight(54);
        sourceList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        sourceList.setBackground(Color.WHITE);
        sourceList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            DefaultListCellRenderer renderer = new DefaultListCellRenderer();
            String text = "";
            if (value != null) {
                text = "<html><div style='padding:4px 0;'>"
                        + "<b>" + escape(value.getName()) + "</b><br>"
                        + "<span style='color:#5c6470;'>" + escape(value.getProvider())
                        + " | Zoom " + value.getMinZoom() + "-" + value.getMaxZoom()
                        + " | " + escape(value.getSourceCRS()) + "</span></div></html>";
            }
            return renderer.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        });
        sourceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshInfo();
            }
        });
        sourceList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    onAdd();
                }
            }
        });
        if (sourceList.getModel().getSize() > 0) {
            sourceList.setSelectedIndex(0);
        }

        infoArea = createReadonlyArea(15, 36);
        credentialsArea = createReadonlyArea(6, 36);
        credentialsArea.setText(
                "Fuentes no incluidas en acceso rapido:\n"
                        + "- Google Satellite / Google Map Tiles API: requiere credenciales y uso segun licencia oficial.\n"
                        + "- Bing Maps / Azure Maps: Microsoft ya deriva este camino a Azure Maps y requiere autenticacion.\n\n"
                        + "CATGIS deja la galeria base con fuentes publicas y mantenibles para evitar integraciones dudosas."
        );

        JPanel detailPanel = new JPanel(new BorderLayout(0, 10));
        detailPanel.setOpaque(false);
        detailPanel.add(wrapSection("Detalle tecnico", new JScrollPane(infoArea)), BorderLayout.CENTER);
        detailPanel.add(wrapSection("Licencia y credenciales", new JScrollPane(credentialsArea)), BorderLayout.SOUTH);

        JScrollPane listScroll = new JScrollPane(sourceList);
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        listScroll.getViewport().setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, wrapSection("Galeria base", listScroll), detailPanel);
        splitPane.setResizeWeight(0.43);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);

        content.add(splitPane, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        refreshInfo();
        setSize(940, 560);
        setMinimumSize(new Dimension(860, 500));
        setLocationRelativeTo(owner);
    }

    public static void open(Window owner) {
        OnlineBaseMapDialog dialog = new OnlineBaseMapDialog(owner);
        dialog.setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JTextArea title = new JTextArea("Mapas base online");
        title.setEditable(false);
        title.setOpaque(false);
        title.setFocusable(false);
        title.setLineWrap(true);
        title.setWrapStyleWord(true);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(23, 37, 84));
        title.setBorder(BorderFactory.createEmptyBorder());

        JTextArea subtitle = new JTextArea(
                "Elegi una base publica lista para usar. Si haces mas zoom que el detalle disponible, CATGIS mantiene la ultima resolucion util en vez de dejar el mapa en error."
        );
        subtitle.setEditable(false);
        subtitle.setOpaque(false);
        subtitle.setFocusable(false);
        subtitle.setLineWrap(true);
        subtitle.setWrapStyleWord(true);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(new Color(87, 96, 112));
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton addButton = new JButton("Agregar");
        addButton.addActionListener(e -> onAdd());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        buttons.add(addButton);
        buttons.add(cancelButton);
        DialogKeyboardSupport.install(this, addButton, this::dispose);
        return buttons;
    }

    private JTextArea createReadonlyArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFocusable(false);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        area.setBackground(new Color(248, 250, 252));
        return area;
    }

    private JPanel wrapSection(String title, JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JTextArea label = new JTextArea(title);
        label.setEditable(false);
        label.setOpaque(false);
        label.setFocusable(false);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setForeground(new Color(34, 51, 84));
        label.setBorder(BorderFactory.createEmptyBorder());

        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void refreshInfo() {
        OnlineRasterSource source = sourceList.getSelectedValue();
        if (source == null) {
            infoArea.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Nombre: ").append(source.getName()).append("\n");
        sb.append("Proveedor: ").append(source.getProvider()).append("\n");
        sb.append("Servicio: ").append(source.getServiceType()).append("\n");
        sb.append("Zoom soportado: ").append(source.getMinZoom()).append(" - ").append(source.getMaxZoom()).append("\n");
        sb.append("CRS de origen: ").append(source.getSourceCRS()).append("\n");
        sb.append("Atribucion: ").append(source.getAttribution()).append("\n");
        if (!source.getTermsUrl().isBlank()) {
            sb.append("Referencia oficial: ").append(source.getTermsUrl()).append("\n");
        }
        sb.append("\nUso esperado:\n");
        sb.append("- fondo base debajo de capas locales y WMS\n");
        sb.append("- zoom fuerte con fallback a la ultima resolucion util\n");
        sb.append("- activacion rapida desde la barra superior\n");
        infoArea.setText(sb.toString());
        infoArea.setCaretPosition(0);
    }

    private void onAdd() {
        OnlineRasterSource source = sourceList.getSelectedValue();
        if (source != null && OnlineBaseMapAction.addBaseMap(source)) {
            dispose();
        }
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
