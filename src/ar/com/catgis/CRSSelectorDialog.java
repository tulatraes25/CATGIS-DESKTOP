package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class CRSSelectorDialog extends JDialog {

    private JTextField searchField;
    private JTextField manualCodeField;
    private JTextArea manualDefinitionArea;
    private DefaultListModel<CRSDefinitions.CrsCatalogEntry> listModel;
    private JList<CRSDefinitions.CrsCatalogEntry> crsList;
    private JLabel loadingLabel;
    private JLabel selectedNameLabel;
    private JLabel selectedCodeLabel;
    private JLabel selectedTypeLabel;
    private JLabel selectedDatumLabel;
    private JLabel selectedUnitLabel;
    private JLabel selectedAreaLabel;
    private JLabel selectedMethodLabel;
    private JTextArea parametersArea;
    private AreaPreviewPanel previewPanel;
    private final Consumer<String> onSelect;

    private List<CRSDefinitions.CrsCatalogEntry> allEntries;
    private final String currentCode;

    public CRSSelectorDialog(Frame owner, String title, String currentCode, Consumer<String> onSelect) {
        super(owner, title, true);
        this.currentCode = CRSDefinitions.normalizeCode(currentCode);
        this.onSelect = onSelect;
        this.allEntries = new ArrayList<>(CRSDefinitions.getFeaturedEntries());

        setSize(1120, 720);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        content.setBackground(new Color(244, 247, 251));

        content.add(buildHeaderPanel(title), BorderLayout.NORTH);
        content.add(buildCenterPanel(), BorderLayout.CENTER);
        content.add(buildFooterPanel(), BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);

        installListeners();
        refreshList();
        preloadCurrentSelection();
        loadFullCatalogAsync();
    }

    private JPanel buildHeaderPanel(String title) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(true);
        header.setBackground(new Color(247, 250, 252));
        header.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setOpaque(false);

        JLabel titleLabel = new JLabel(title != null && !title.isBlank() ? title : "Selector de CRS");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 21f));
        titleLabel.setForeground(new Color(21, 40, 74));

        JLabel subtitleLabel = new JLabel(
                "<html>Selector global de sistemas de referencia con cobertura mundial, "
                        + "panel tecnico, area de uso y carga manual.</html>"
        );
        subtitleLabel.setForeground(new Color(72, 86, 104));
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12.5f));

        left.add(titleLabel, BorderLayout.NORTH);
        left.add(subtitleLabel, BorderLayout.CENTER);

        JPanel searchCard = new JPanel(new BorderLayout(6, 6));
        searchCard.setOpaque(false);
        JLabel searchLabel = new JLabel("Buscar por nombre, EPSG, zona o region");
        searchLabel.setFont(searchLabel.getFont().deriveFont(Font.BOLD, 12f));
        searchLabel.setForeground(new Color(44, 57, 75));

        searchField = new JTextField();
        searchField.setToolTipText("Busca por codigo, nombre, datum, region o zona UTM.");
        searchField.putClientProperty("JTextField.placeholderText", "Ej.: 22182, UTM 19S, Chos Malal, China, Africa");

        loadingLabel = new JLabel("Cargando catalogo mundial EPSG...", SwingConstants.RIGHT);
        loadingLabel.setForeground(new Color(33, 120, 210));
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.BOLD, 11.5f));

        searchCard.add(searchLabel, BorderLayout.NORTH);
        searchCard.add(searchField, BorderLayout.CENTER);
        searchCard.add(loadingLabel, BorderLayout.SOUTH);

        header.add(left, BorderLayout.CENTER);
        header.add(searchCard, BorderLayout.EAST);
        return header;
    }

    private Component buildCenterPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildCatalogPanel(), buildDetailsPanel());
        splitPane.setResizeWeight(0.46d);
        splitPane.setDividerLocation(470);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        return splitPane;
    }

    private JPanel buildCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(createCardBorder());

        JLabel title = new JLabel("Catalogo de CRS");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(new Color(29, 45, 71));

        JLabel hint = new JLabel("Lista moderna con EPSG destacados y catalogo mundial completo");
        hint.setForeground(new Color(91, 103, 121));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11.5f));

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(hint, BorderLayout.SOUTH);

        listModel = new DefaultListModel<>();
        crsList = new JList<>(listModel);
        crsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        crsList.setCellRenderer(new CrsCellRenderer());
        crsList.setFixedCellHeight(58);

        JScrollPane scrollPane = new JScrollPane(crsList);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JPanel topCard = new JPanel(new BorderLayout(8, 8));
        topCard.setOpaque(false);
        topCard.setBorder(createCardBorder());

        JLabel title = new JLabel("Panel tecnico");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(new Color(29, 45, 71));
        topCard.add(title, BorderLayout.NORTH);

        JPanel detailsGrid = new JPanel(new GridBagLayout());
        detailsGrid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(3, 0, 3, 8);
        gc.anchor = GridBagConstraints.NORTHWEST;

        selectedNameLabel = createValueLabel();
        selectedCodeLabel = createValueLabel();
        selectedTypeLabel = createValueLabel();
        selectedDatumLabel = createValueLabel();
        selectedUnitLabel = createValueLabel();
        selectedAreaLabel = createMultilineValueLabel();
        selectedMethodLabel = createMultilineValueLabel();

        addTechnicalField(detailsGrid, gc, "Nombre", selectedNameLabel);
        addTechnicalField(detailsGrid, gc, "Codigo", selectedCodeLabel);
        addTechnicalField(detailsGrid, gc, "Tipo", selectedTypeLabel);
        addTechnicalField(detailsGrid, gc, "Datum", selectedDatumLabel);
        addTechnicalField(detailsGrid, gc, "Unidad", selectedUnitLabel);
        addTechnicalField(detailsGrid, gc, "Area de uso", selectedAreaLabel);
        addTechnicalField(detailsGrid, gc, "Metodo", selectedMethodLabel);

        parametersArea = new JTextArea(5, 24);
        parametersArea.setEditable(false);
        parametersArea.setLineWrap(true);
        parametersArea.setWrapStyleWord(true);
        parametersArea.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 11.5f));
        parametersArea.setBackground(new Color(248, 250, 252));
        parametersArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        gc.gridx = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        JLabel paramsLabel = new JLabel("Parametros");
        paramsLabel.setFont(paramsLabel.getFont().deriveFont(Font.BOLD, 12f));
        paramsLabel.setForeground(new Color(55, 65, 81));
        detailsGrid.add(paramsLabel, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        detailsGrid.add(new JScrollPane(parametersArea), gc);

        topCard.add(detailsGrid, BorderLayout.CENTER);

        JPanel bottomArea = new JPanel(new BorderLayout(8, 8));
        bottomArea.setOpaque(false);

        previewPanel = new AreaPreviewPanel();
        previewPanel.setPreferredSize(new Dimension(320, 230));
        previewPanel.setBorder(createCardBorder());

        JPanel manualCard = new JPanel(new BorderLayout(6, 6));
        manualCard.setOpaque(false);
        manualCard.setBorder(createCardBorder());

        JLabel manualTitle = new JLabel("Carga manual");
        manualTitle.setFont(manualTitle.getFont().deriveFont(Font.BOLD, 15f));
        manualTitle.setForeground(new Color(29, 45, 71));

        JPanel manualFields = new JPanel(new GridBagLayout());
        manualFields.setOpaque(false);
        GridBagConstraints mgc = new GridBagConstraints();
        mgc.gridx = 0;
        mgc.gridy = 0;
        mgc.insets = new Insets(3, 0, 3, 8);
        mgc.anchor = GridBagConstraints.NORTHWEST;

        manualCodeField = new JTextField(currentCode != null ? currentCode : "", 24);
        manualCodeField.putClientProperty("JTextField.placeholderText", "EPSG:22182, EPSG:4490, ESRI:102100...");
        addTechnicalField(manualFields, mgc, "Codigo manual", manualCodeField);

        manualDefinitionArea = new JTextArea(6, 24);
        manualDefinitionArea.setLineWrap(true);
        manualDefinitionArea.setWrapStyleWord(true);
        manualDefinitionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        manualDefinitionArea.setToolTipText("Pega aqui una definicion WKT si necesitas una proyeccion personalizada.");

        mgc.gridx = 0;
        mgc.weightx = 0;
        mgc.fill = GridBagConstraints.NONE;
        JLabel definitionLabel = new JLabel("Definicion WKT");
        definitionLabel.setFont(definitionLabel.getFont().deriveFont(Font.BOLD, 12f));
        definitionLabel.setForeground(new Color(55, 65, 81));
        manualFields.add(definitionLabel, mgc);

        mgc.gridx = 1;
        mgc.weightx = 1;
        mgc.fill = GridBagConstraints.HORIZONTAL;
        manualFields.add(new JScrollPane(manualDefinitionArea), mgc);

        JLabel manualHelp = new JLabel("<html>Admite codigos EPSG/ESRI/OGC y definiciones WKT para casos personalizados.</html>");
        manualHelp.setForeground(new Color(91, 103, 121));
        manualHelp.setFont(manualHelp.getFont().deriveFont(Font.PLAIN, 11.2f));

        manualCard.add(manualTitle, BorderLayout.NORTH);
        manualCard.add(manualFields, BorderLayout.CENTER);
        manualCard.add(manualHelp, BorderLayout.SOUTH);

        bottomArea.add(previewPanel, BorderLayout.CENTER);
        bottomArea.add(manualCard, BorderLayout.SOUTH);

        panel.add(topCard, BorderLayout.NORTH);
        panel.add(bottomArea, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);

        JButton useSelectedButton = new JButton("Aplicar seleccionado");
        useSelectedButton.addActionListener(e -> applySelected());

        JButton useManualButton = new JButton("Aplicar manual");
        useManualButton.addActionListener(e -> applyManual());

        JButton closeButton = new JButton("Cancelar");
        closeButton.addActionListener(e -> dispose());

        panel.add(useSelectedButton);
        panel.add(useManualButton);
        panel.add(closeButton);
        return panel;
    }

    private void installListeners() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshList();
            }
        });

        crsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailsForSelection();
            }
        });

        crsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && crsList.getSelectedValue() != null) {
                    applySelected();
                }
            }
        });
    }

    private void loadFullCatalogAsync() {
        SwingWorker<List<CRSDefinitions.CrsCatalogEntry>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CRSDefinitions.CrsCatalogEntry> doInBackground() {
                return CRSDefinitions.getCatalogEntries();
            }

            @Override
            protected void done() {
                try {
                    allEntries = get();
                } catch (Exception ex) {
                    allEntries = new ArrayList<>(CRSDefinitions.getFeaturedEntries());
                }
                loadingLabel.setText("Catalogo EPSG listo: " + allEntries.size() + " CRS");
                refreshList();
                preloadCurrentSelection();
            }
        };
        worker.execute();
    }

    private void preloadCurrentSelection() {
        if (currentCode == null || currentCode.isBlank()) {
            if (!listModel.isEmpty() && crsList.getSelectedIndex() < 0) {
                crsList.setSelectedIndex(0);
            }
            return;
        }

        for (int i = 0; i < listModel.size(); i++) {
            CRSDefinitions.CrsCatalogEntry entry = listModel.get(i);
            if (entry != null && currentCode.equalsIgnoreCase(entry.code())) {
                crsList.setSelectedIndex(i);
                crsList.ensureIndexIsVisible(i);
                updateDetailsForSelection();
                return;
            }
        }

        manualCodeField.setText(currentCode);
        updateDetails(CRSDefinitions.describe(currentCode));
    }

    private void refreshList() {
        listModel.clear();
        List<CRSDefinitions.CrsCatalogEntry> source = allEntries != null ? allEntries : CRSDefinitions.getFeaturedEntries();
        List<CRSDefinitions.CrsCatalogEntry> filtered = CRSDefinitions.filterEntries(searchField.getText(), source);
        for (CRSDefinitions.CrsCatalogEntry entry : filtered) {
            listModel.addElement(entry);
        }
        if (!listModel.isEmpty() && crsList.getSelectedIndex() < 0) {
            crsList.setSelectedIndex(0);
        }
    }

    private void updateDetailsForSelection() {
        CRSDefinitions.CrsCatalogEntry entry = crsList.getSelectedValue();
        if (entry == null) {
            updateDetails(CRSDefinitions.describe(manualCodeField.getText()));
            return;
        }
        updateDetails(CRSDefinitions.describe(entry.code()));
    }

    private void updateDetails(CRSDefinitions.CrsTechnicalDetails details) {
        selectedNameLabel.setText(details.name());
        selectedCodeLabel.setText(details.code());
        selectedTypeLabel.setText(details.type());
        selectedDatumLabel.setText(details.datum());
        selectedUnitLabel.setText(details.unit());
        selectedAreaLabel.setText("<html>" + escape(details.areaOfUse()) + "</html>");
        selectedMethodLabel.setText("<html>" + escape(details.projectionMethod()) + "</html>");
        parametersArea.setText(details.parameters());
        previewPanel.setDetails(details, resolveCurrentProjectMarker());
    }

    private void applySelected() {
        CRSDefinitions.CrsCatalogEntry entry = crsList.getSelectedValue();
        if (entry == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un CRS de la lista.");
            return;
        }
        applyValue(entry.code());
    }

    private void applyManual() {
        String manualDefinition = manualDefinitionArea.getText() != null ? manualDefinitionArea.getText().trim() : "";
        String value = !manualDefinition.isBlank()
                ? CRSDefinitions.buildManualDefinitionValue(manualDefinition)
                : CRSDefinitions.normalizeCode(manualCodeField.getText());

        if (value.isBlank()) {
            JOptionPane.showMessageDialog(this, "Ingresa un codigo CRS o una definicion WKT.");
            return;
        }
        applyValue(value);
    }

    private void applyValue(String value) {
        try {
            String normalized = CRSDefinitions.normalizeCode(value);
            CRSDefinitions.decode(normalized, true);
            if (onSelect != null) {
                onSelect.accept(normalized);
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo validar el CRS indicado:\n" + ex.getMessage(),
                    "Selector de CRS",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setForeground(new Color(31, 41, 55));
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 12.2f));
        return label;
    }

    private JLabel createMultilineValueLabel() {
        JLabel label = createValueLabel();
        label.setVerticalAlignment(SwingConstants.TOP);
        return label;
    }

    private void addTechnicalField(JPanel panel, GridBagConstraints gc, String title, Component component) {
        JLabel fieldLabel = new JLabel(title);
        fieldLabel.setFont(fieldLabel.getFont().deriveFont(Font.BOLD, 12f));
        fieldLabel.setForeground(new Color(55, 65, 81));

        gc.gridx = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        panel.add(fieldLabel, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gc);
        gc.gridy++;
    }

    private CompoundBorder createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    private double[] resolveCurrentProjectMarker() {
        if (CatgisDesktopApp.currentProject == null || CatgisDesktopApp.mapPanel == null) {
            return null;
        }
        var envelope = CatgisDesktopApp.mapPanel.getCurrentViewEnvelope();
        if (envelope == null || envelope.isNull()) {
            return null;
        }
        double centerX = envelope.getMinX() + (envelope.getWidth() / 2d);
        double centerY = envelope.getMinY() + (envelope.getHeight() / 2d);
        try {
            var source = CRSDefinitions.decode(CatgisDesktopApp.currentProject.getProjectCRS(), true);
            var target = CRSDefinitions.decode("EPSG:4326", true);
            var transform = org.geotools.referencing.CRS.findMathTransform(source, target, true);
            double[] src = new double[]{centerX, centerY};
            double[] dst = new double[2];
            transform.transform(src, 0, dst, 0, 1);
            return dst;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void open(String title, String currentCode, Consumer<String> onSelect) {
        SwingUtilities.invokeLater(() -> {
            Frame owner = CatgisDesktopApp.getMainFrame();
            new CRSSelectorDialog(owner, title, currentCode, onSelect).setVisible(true);
        });
    }

    private static class CrsCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof CRSDefinitions.CrsCatalogEntry entry) {
                String accent = entry.featured() ? "Destacado" : "EPSG";
                label.setText("<html><b>" + entry.code() + "</b> - " + entry.description()
                        + "<br><span style='color:#6b7280;font-size:10px;'>" + accent + "</span></html>");
            }
            label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            return label;
        }
    }

    private static class AreaPreviewPanel extends JPanel {
        private CRSDefinitions.CrsTechnicalDetails details;
        private double[] marker;

        private AreaPreviewPanel() {
            setBackground(Color.WHITE);
        }

        private void setDetails(CRSDefinitions.CrsTechnicalDetails details, double[] marker) {
            this.details = details;
            this.marker = marker;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int pad = 14;
                int w = Math.max(1, getWidth() - (pad * 2));
                int h = Math.max(1, getHeight() - (pad * 2));
                int x = pad;
                int y = pad;

                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(x, y, w, h, 16, 16);

                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(x, y, w, h, 16, 16);

                g2.setColor(new Color(226, 232, 240));
                for (int i = 1; i < 6; i++) {
                    int xx = x + Math.round((w / 6f) * i);
                    int yy = y + Math.round((h / 6f) * i);
                    g2.drawLine(xx, y + 6, xx, y + h - 6);
                    g2.drawLine(x + 6, yy, x + w - 6, yy);
                }

                g2.setColor(new Color(147, 197, 253, 180));
                if (details != null && details.hasBounds()) {
                    int rx1 = lonToX(details.west(), x, w);
                    int rx2 = lonToX(details.east(), x, w);
                    int ry1 = latToY(details.north(), y, h);
                    int ry2 = latToY(details.south(), y, h);
                    int rw = Math.max(8, rx2 - rx1);
                    int rh = Math.max(8, ry2 - ry1);

                    g2.fillRoundRect(rx1, ry1, rw, rh, 10, 10);
                    g2.setColor(new Color(37, 99, 235));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(rx1, ry1, rw, rh, 10, 10);
                } else {
                    g2.setColor(new Color(100, 116, 139));
                    g2.drawString("Sin area de uso reportada", x + 12, y + 24);
                }

                if (marker != null && marker.length >= 2) {
                    int mx = lonToX(marker[0], x, w);
                    int my = latToY(marker[1], y, h);
                    g2.setColor(new Color(220, 38, 38));
                    g2.fillOval(mx - 4, my - 4, 8, 8);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(mx - 8, my - 8, 16, 16);
                }

                g2.setColor(new Color(55, 65, 81));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
                g2.drawString("Area de uso", x + 10, y + 18);
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
                g2.setColor(new Color(91, 103, 121));
                g2.drawString("Planisferio / franja / region EPSG", x + 10, y + h - 10);
            } finally {
                g2.dispose();
            }
        }

        private int lonToX(double lon, int x, int width) {
            double clamped = Math.max(-180d, Math.min(180d, lon));
            return x + (int) Math.round(((clamped + 180d) / 360d) * width);
        }

        private int latToY(double lat, int y, int height) {
            double clamped = Math.max(-90d, Math.min(90d, lat));
            return y + (int) Math.round(((90d - clamped) / 180d) * height);
        }
    }
}
