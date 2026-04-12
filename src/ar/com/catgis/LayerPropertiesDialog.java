package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class LayerPropertiesDialog extends JDialog {

    private final Layer layer;

    private final JTextField nameField;
    private final JTextField pathField;
    private final JTextField sourceCrsField;
    private final JTextField cadGeorefField;
    private final JTextField cadAdjustmentField;
    private final JCheckBox visibleCheck;
    private final JCheckBox labelsVisibleCheck;
    private final JComboBox<String> labelFieldCombo;
    private final JButton sourceCrsButton;
    private final JButton clearSourceCrsButton;
    private final JButton cadGeorefButton;
    private final JButton cadAdjustmentButton;

    private final JButton fillColorButton;
    private final JButton borderColorButton;
    private final JButton lineColorButton;
    private final JButton pointColorButton;

    private final JSpinner lineWidthSpinner;
    private final JSpinner pointSizeSpinner;
    private final JComboBox<Layer.PointSymbolStyle> pointStyleCombo;
    private final JComboBox<Layer.LineSymbolStyle> lineStyleCombo;
    private final JComboBox<Layer.PolygonFillStyle> polygonStyleCombo;
    private final JCheckBox pointGraphicCheck;
    private final JComboBox<String> pointCategoryCombo;
    private final DefaultListModel<PointSymbolCatalog.Entry> pointCatalogListModel;
    private final JList<PointSymbolCatalog.Entry> pointCatalogList;
    private final JTextField pointGraphicPathField;
    private final JLabel pointGraphicPreviewLabel;
    private final JLabel pointCatalogSelectionLabel;
    private final JLabel symbologyPreviewLabel;
    private final JButton browsePointGraphicButton;
    private final JButton clearPointGraphicButton;

    private final Color fillColor;
    private final Color borderColor;
    private final Color lineColor;
    private final Color pointColor;
    private String selectedSourceCrs;
    private double selectedCadOffsetX;
    private double selectedCadOffsetY;
    private double selectedCadScale;
    private double selectedCadRotationDegrees;
    private String selectedCadGeorefMethod;
    private double selectedCadGeorefM00;
    private double selectedCadGeorefM01;
    private double selectedCadGeorefM02;
    private double selectedCadGeorefM10;
    private double selectedCadGeorefM11;
    private double selectedCadGeorefM12;
    private double selectedCadGeorefResidualMean;
    private double selectedCadGeorefResidualMax;
    private int selectedCadGeorefReferenceCount;
    private int selectedCadGeorefCheckCount;

    public LayerPropertiesDialog(Frame owner, Layer layer) {
        super(owner, "Propiedades de capa", true);
        this.layer = layer;

        this.fillColor = layer.getFillColor();
        this.borderColor = layer.getBorderColor();
        this.lineColor = layer.getLineColor();
        this.pointColor = layer.getPointColor();

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        nameField = new JTextField(layer.getName(), 24);
        pathField = new JTextField(buildPathDisplay(layer), 24);
        pathField.setEditable(false);
        pathField.setToolTipText(layer.getPath());
        selectedSourceCrs = CRSDefinitions.normalizeCode(layer.getSourceCRS());
        selectedCadOffsetX = layer.getCadOffsetX();
        selectedCadOffsetY = layer.getCadOffsetY();
        selectedCadScale = layer.getCadScale();
        selectedCadRotationDegrees = layer.getCadRotationDegrees();
        selectedCadGeorefMethod = layer.getCadGeoreferenceMethod();
        selectedCadGeorefM00 = layer.getCadGeorefM00();
        selectedCadGeorefM01 = layer.getCadGeorefM01();
        selectedCadGeorefM02 = layer.getCadGeorefM02();
        selectedCadGeorefM10 = layer.getCadGeorefM10();
        selectedCadGeorefM11 = layer.getCadGeorefM11();
        selectedCadGeorefM12 = layer.getCadGeorefM12();
        selectedCadGeorefResidualMean = layer.getCadGeorefResidualMean();
        selectedCadGeorefResidualMax = layer.getCadGeorefResidualMax();
        selectedCadGeorefReferenceCount = layer.getCadGeorefReferenceCount();
        selectedCadGeorefCheckCount = layer.getCadGeorefCheckCount();
        sourceCrsField = new JTextField(CadLayerSupport.formatSourceCrsLabel(selectedSourceCrs), 24);
        sourceCrsField.setEditable(false);
        cadGeorefField = new JTextField(CadLayerSupport.buildCadGeoreferenceLabel(layer), 24);
        cadGeorefField.setEditable(false);
        cadAdjustmentField = new JTextField(CadLayerSupport.buildCadAdjustmentLabel(layer), 24);
        cadAdjustmentField.setEditable(false);
        visibleCheck = new JCheckBox("Capa visible", layer.isVisible());
        sourceCrsButton = new JButton("Definir CRS...");
        sourceCrsButton.addActionListener(e -> editCadSourceCrs());
        clearSourceCrsButton = new JButton("Quitar CRS");
        clearSourceCrsButton.addActionListener(e -> {
            selectedSourceCrs = "";
            refreshSourceCrsField();
        });
        cadGeorefButton = new JButton("Georreferenciar...");
        cadGeorefButton.addActionListener(e -> editCadGeoreference());
        cadAdjustmentButton = new JButton("Ajuste CAD...");
        cadAdjustmentButton.addActionListener(e -> editCadPlacement());

        fillColorButton = createColorButton(fillColor, "Color de relleno");
        borderColorButton = createColorButton(borderColor, "Color de borde");
        lineColorButton = createColorButton(lineColor, "Color de linea");
        lineWidthSpinner = new JSpinner(new SpinnerNumberModel((double) layer.getLineWidth(), 0.5, 20.0, 0.5));
        lineStyleCombo = new JComboBox<>(Layer.LineSymbolStyle.values());
        lineStyleCombo.setSelectedItem(layer.getLineSymbolStyle());
        fillColorButton.setToolTipText("Color principal de relleno");
        polygonStyleCombo = new JComboBox<>(Layer.PolygonFillStyle.values());
        polygonStyleCombo.setSelectedItem(layer.getPolygonFillStyle());
        pointColorButton = createColorButton(pointColor, "Color de punto");
        pointSizeSpinner = new JSpinner(new SpinnerNumberModel(layer.getPointSize(), 1, 40, 1));
        pointStyleCombo = new JComboBox<>(Layer.PointSymbolStyle.values());
        pointStyleCombo.setSelectedItem(layer.getPointSymbolStyle());
        pointStyleCombo.setRenderer(new PointStyleRenderer());
        symbologyPreviewLabel = new JLabel();
        symbologyPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        symbologyPreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        symbologyPreviewLabel.setPreferredSize(new Dimension(240, 148));
        symbologyPreviewLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 232)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        symbologyPreviewLabel.setOpaque(true);
        symbologyPreviewLabel.setBackground(new Color(251, 252, 254));

        String currentGraphicReference = layer.getPointGraphicSymbol();
        PointSymbolCatalog.Entry currentCatalogEntry = PointSymbolCatalog.findByReference(currentGraphicReference);

        pointGraphicCheck = new JCheckBox(I18n.t("Usar icono grafico"), layer.hasPointGraphicSymbol());

        pointCategoryCombo = new JComboBox<>();
        pointCategoryCombo.addItem(I18n.t("Todas las categorias"));
        for (String category : PointSymbolCatalog.categories()) {
            pointCategoryCombo.addItem(category);
        }

        pointGraphicPathField = new JTextField(currentCatalogEntry == null ? currentGraphicReference : "", 24);
        pointGraphicPathField.setEditable(false);
        pointGraphicPreviewLabel = new JLabel();
        pointCatalogSelectionLabel = new JLabel();

        pointCatalogListModel = new DefaultListModel<>();
        pointCatalogList = new JList<>(pointCatalogListModel);
        pointCatalogList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        pointCatalogList.setVisibleRowCount(-1);
        pointCatalogList.setFixedCellWidth(148);
        pointCatalogList.setFixedCellHeight(74);
        pointCatalogList.setCellRenderer(new PointCatalogRenderer());
        pointCatalogList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            if (pointCatalogList.getSelectedValue() != null) {
                pointGraphicPathField.setText("");
            }
            updateCatalogSelectionLabel();
            updatePointGraphicPreview();
        });
        JScrollPane pointCatalogScroll = new JScrollPane(pointCatalogList);
        pointCatalogScroll.setPreferredSize(new Dimension(420, 156));
        pointCatalogScroll.getVerticalScrollBar().setUnitIncrement(18);
        pointCategoryCombo.addActionListener(e -> {
            PointSymbolCatalog.Entry preferredEntry = pointCatalogList.getSelectedValue();
            reloadPointCatalogEntries(preferredEntry);
            updateSymbologyPreview();
        });

        browsePointGraphicButton = new JButton(I18n.t("Cargar icono..."));
        browsePointGraphicButton.addActionListener(e -> choosePointGraphicFile());
        clearPointGraphicButton = new JButton(I18n.t("Quitar icono"));
        clearPointGraphicButton.addActionListener(e -> {
            pointGraphicPathField.setText("");
            pointCatalogList.clearSelection();
            pointGraphicPreviewLabel.setIcon(null);
            updateCatalogSelectionLabel();
            pointGraphicPreviewLabel.setText(I18n.t("Sin icono grafico"));
        });
        JPanel pointGraphicFilePanel = new JPanel(new BorderLayout(6, 0));
        pointGraphicFilePanel.setOpaque(false);
        pointGraphicFilePanel.add(pointGraphicPathField, BorderLayout.CENTER);
        JPanel pointGraphicButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pointGraphicButtons.setOpaque(false);
        pointGraphicButtons.add(browsePointGraphicButton);
        pointGraphicButtons.add(clearPointGraphicButton);
        pointGraphicFilePanel.add(pointGraphicButtons, BorderLayout.EAST);
        pointGraphicCheck.addActionListener(e -> updatePointGraphicControls());
        if (currentCatalogEntry != null) {
            pointCategoryCombo.setSelectedItem(currentCatalogEntry.getCategory());
        }
        reloadPointCatalogEntries(currentCatalogEntry);
        updatePointGraphicControls();
        updatePointGraphicPreview();

        JButton categorizedButton = new JButton(I18n.t("Simbologia por campo..."));
        categorizedButton.addActionListener(e -> CategorizedSymbologyDialog.open(this, layer));
        JButton importSldButton = new JButton("Importar SLD...");
        importSldButton.addActionListener(e -> importSldStyle());
        JButton exportSldButton = new JButton("Exportar SLD...");
        exportSldButton.addActionListener(e -> exportSldStyle());

        labelsVisibleCheck = new JCheckBox("Mostrar etiquetas", layer.isLabelsVisible());

        List<String> fields = null;
        ShapefileData data = CatgisDesktopApp.mapPanel != null ? CatgisDesktopApp.mapPanel.getShapefileData(layer) : null;
        if (data != null) {
            fields = data.getAttributeNames();
        }
        labelFieldCombo = new JComboBox<>();
        if (fields != null && !fields.isEmpty()) {
            for (String field : fields) {
                labelFieldCombo.addItem(field);
            }
            if (layer.getLabelField() != null) {
                labelFieldCombo.setSelectedItem(layer.getLabelField());
            }
        } else {
            labelFieldCombo.addItem("(sin campos disponibles)");
            labelFieldCombo.setEnabled(false);
        }
        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildCenterTabs(pointCatalogScroll, pointGraphicFilePanel, categorizedButton, importSldButton, exportSldButton), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton applyButton = new JButton(I18n.t("Aplicar"));
        JButton okButton = new JButton(I18n.t("Aceptar"));
        JButton cancelButton = new JButton(I18n.t("Cancelar"));

        applyButton.addActionListener(e -> applyChanges());
        okButton.addActionListener(e -> {
            if (applyChanges()) {
                dispose();
            }
        });
        cancelButton.addActionListener(e -> dispose());

        buttons.add(applyButton);
        buttons.add(okButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        installPreviewBindings();
        updateSymbologyPreview();
        pack();
        applyInitialDialogSize();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel(layer.getName());
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        title.setForeground(new Color(23, 35, 55));

        JLabel subtitle = new JLabel("<html><b>Tipo:</b> " + escapeHtml(layer.getType()) + " &nbsp;&nbsp; <b>Origen:</b> " + escapeHtml(buildPathDisplay(layer)) + "</html>");
        subtitle.setForeground(new Color(88, 98, 112));

        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);

        JLabel badge = new JLabel(layer.isVisible() ? "Visible" : "Oculta");
        badge.setOpaque(true);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(120, 34));
        badge.setBackground(layer.isVisible() ? new Color(224, 242, 230) : new Color(245, 228, 228));
        badge.setForeground(layer.isVisible() ? new Color(22, 101, 52) : new Color(153, 27, 27));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(layer.isVisible() ? new Color(34, 197, 94) : new Color(239, 68, 68)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        panel.add(text, BorderLayout.CENTER);
        panel.add(badge, BorderLayout.EAST);
        return panel;
    }

    private JTabbedPane buildCenterTabs(JScrollPane pointCatalogScroll,
                                        JPanel pointGraphicFilePanel,
                                        JButton categorizedButton,
                                        JButton importSldButton,
                                        JButton exportSldButton) {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        tabs.addTab("General", buildGeneralTab());
        tabs.addTab("Simbologia", buildSymbologyTab(pointCatalogScroll, pointGraphicFilePanel, categorizedButton, importSldButton, exportSldButton));
        tabs.addTab("Etiquetas", buildLabelsTab());
        return tabs;
    }

    private JPanel buildGeneralTab() {
        JPanel panel = buildCardPanel();
        GridBagConstraints gbc = createFormConstraints();
        int row = 0;
        addSectionTitle(panel, gbc, row++, "Configuracion general");
        addRow(panel, gbc, row++, "Nombre", nameField);
        addRow(panel, gbc, row++, "Ubicacion en disco", pathField);
        if (CadLayerSupport.isCadLayer(layer)) {
            addRow(panel, gbc, row++, "CRS CAD", buildCadCrsEditor());
            addRow(panel, gbc, row++, "Georreferenciacion", buildCadGeorefEditor());
            addRow(panel, gbc, row++, "Ajuste CAD", buildCadAdjustmentEditor());
        }

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(visibleCheck, gbc);

        gbc.gridy = row;
        gbc.weighty = 1;
        panel.add(new JLabel(""), gbc);
        return wrapInScroll(panel);
    }

    private JPanel buildSymbologyTab(JScrollPane pointCatalogScroll,
                                     JPanel pointGraphicFilePanel,
                                     JButton categorizedButton,
                                     JButton importSldButton,
                                     JButton exportSldButton) {
        JPanel content = new JPanel(new BorderLayout(12, 0));
        content.setOpaque(false);

        JPanel editorColumn = new JPanel(new BorderLayout(0, 12));
        editorColumn.setOpaque(false);

        JPanel basicPanel = buildCardPanel();
        GridBagConstraints gbc = createFormConstraints();
        int row = 0;
        addSectionTitle(basicPanel, gbc, row++, isPointLayer() ? "Simbolo puntual" : isLineLayer() ? "Trazo lineal" : "Relleno y borde");
        if (isPolygonLayer()) {
            addRow(basicPanel, gbc, row++, "Relleno", fillColorButton);
            addRow(basicPanel, gbc, row++, "Borde", borderColorButton);
            addRow(basicPanel, gbc, row++, "Grosor de borde", lineWidthSpinner);
            addRow(basicPanel, gbc, row++, "Trama", polygonStyleCombo);
        } else if (isLineLayer()) {
            addRow(basicPanel, gbc, row++, "Color", lineColorButton);
            addRow(basicPanel, gbc, row++, "Grosor", lineWidthSpinner);
            addRow(basicPanel, gbc, row++, "Estilo", lineStyleCombo);
        } else {
            addRow(basicPanel, gbc, row++, "Color", pointColorButton);
            addRow(basicPanel, gbc, row++, "Tamano", pointSizeSpinner);
            addRow(basicPanel, gbc, row++, "Estilo", pointStyleCombo);
        }
        JPanel styleActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        styleActions.setOpaque(false);
        styleActions.add(categorizedButton);
        styleActions.add(importSldButton);
        styleActions.add(exportSldButton);
        addRow(basicPanel, gbc, row++, "Tematica", styleActions);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        basicPanel.add(new JLabel(""), gbc);
        editorColumn.add(basicPanel, BorderLayout.NORTH);

        if (isPointLayer()) {
            JPanel pointPanel = buildCardPanel();
            GridBagConstraints pgc = createFormConstraints();
            int prow = 0;
            addSectionTitle(pointPanel, pgc, prow++, "Iconos y catalogo");
            pgc.gridx = 0;
            pgc.gridy = prow++;
            pgc.gridwidth = 2;
            pointPanel.add(pointGraphicCheck, pgc);
            pgc.gridwidth = 1;
            addRow(pointPanel, pgc, prow++, I18n.t("Categoria"), pointCategoryCombo);
            addRow(pointPanel, pgc, prow++, I18n.t("Seleccion"), pointCatalogSelectionLabel);
            addRow(pointPanel, pgc, prow++, I18n.t("Archivo"), pointGraphicFilePanel);
            addRow(pointPanel, pgc, prow++, I18n.t("Preview icono"), pointGraphicPreviewLabel);
            addRow(pointPanel, pgc, prow++, I18n.t("Biblioteca"), pointCatalogScroll);
            pgc.gridx = 0;
            pgc.gridy = prow;
            pgc.weighty = 1;
            pgc.gridwidth = 2;
            pointPanel.add(new JLabel(""), pgc);
            editorColumn.add(pointPanel, BorderLayout.CENTER);
        }

        JPanel previewCard = buildCardPanel();
        previewCard.setLayout(new BorderLayout(0, 10));
        previewCard.setPreferredSize(new Dimension(286, 0));
        JLabel previewTitle = new JLabel("Vista previa cartografica");
        previewTitle.setFont(previewTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        JLabel previewSummary = new JLabel(buildSymbologySummaryHtml());
        previewSummary.setForeground(new Color(88, 98, 112));
        previewSummary.setVerticalAlignment(SwingConstants.TOP);
        previewCard.add(previewTitle, BorderLayout.NORTH);
        previewCard.add(symbologyPreviewLabel, BorderLayout.CENTER);
        previewCard.add(previewSummary, BorderLayout.SOUTH);

        content.add(editorColumn, BorderLayout.CENTER);
        content.add(previewCard, BorderLayout.EAST);
        return wrapInScroll(content);
    }

    private String buildSymbologySummaryHtml() {
        String family = isPointLayer() ? "puntos" : isLineLayer() ? "lineas" : isPolygonLayer() ? "poligonos" : "geometrias";
        String description = isPointLayer()
                ? "Controla color, tamano, estilo y catalogo de simbolos sin abrir un panel enorme."
                : isLineLayer()
                ? "Ajusta color, grosor y patron lineal con una preview rapida del trazado."
                : "Combina relleno, borde y trama para una lectura tematica mas limpia.";
        return "<html><div style='width:230px;'><b>Modo " + family + "</b><br/>" + description + "</div></html>";
    }

    private JPanel buildLabelsTab() {
        JPanel panel = buildCardPanel();
        GridBagConstraints gbc = createFormConstraints();
        int row = 0;
        addSectionTitle(panel, gbc, row++, "Etiquetas");
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(labelsVisibleCheck, gbc);
        gbc.gridwidth = 1;
        addRow(panel, gbc, row++, "Campo etiqueta", labelFieldCombo);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        panel.add(new JLabel(""), gbc);
        return wrapInScroll(panel);
    }

    private JPanel buildCardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }

    private GridBagConstraints createFormConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private void addSectionTitle(JPanel panel, GridBagConstraints gbc, int row, String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(label, gbc);
        gbc.gridwidth = 1;
    }

    private JPanel wrapInScroll(Component component) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private void installPreviewBindings() {
        lineStyleCombo.addActionListener(e -> updateSymbologyPreview());
        polygonStyleCombo.addActionListener(e -> updateSymbologyPreview());
        pointStyleCombo.addActionListener(e -> updateSymbologyPreview());
        pointGraphicCheck.addActionListener(e -> updateSymbologyPreview());
        pointCatalogList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSymbologyPreview();
            }
        });
        pointSizeSpinner.addChangeListener(e -> updateSymbologyPreview());
        lineWidthSpinner.addChangeListener(e -> updateSymbologyPreview());
    }

    private void updateSymbologyPreview() {
        BufferedImage image = new BufferedImage(200, 120, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setColor(new Color(251, 252, 254));
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2.setColor(new Color(226, 232, 240));
            g2.drawRoundRect(6, 6, image.getWidth() - 12, image.getHeight() - 12, 18, 18);

            if (isPolygonLayer()) {
                Rectangle rect = new Rectangle(34, 28, 132, 64);
                paintPolygonPreview(g2, rect, selectedColor(fillColorButton), selectedColor(borderColorButton),
                        (Layer.PolygonFillStyle) polygonStyleCombo.getSelectedItem());
            } else if (isLineLayer()) {
                g2.setColor(selectedColor(lineColorButton));
                g2.setStroke(buildPreviewStroke());
                Path2D path = new Path2D.Double();
                path.moveTo(24, 84);
                path.curveTo(56, 30, 104, 98, 172, 42);
                g2.draw(path);
            } else {
                String reference = resolvePointGraphicReference();
                ImageIcon icon = PointGraphicSymbolSupport.buildPreviewIcon(reference, 42);
                if (pointGraphicCheck.isSelected() && icon != null) {
                    icon.paintIcon(this, g2, 79, 34);
                } else {
                    BufferedImage preview = buildPointStylePreview((Layer.PointSymbolStyle) pointStyleCombo.getSelectedItem(), selectedColor(pointColorButton));
                    g2.drawImage(preview.getScaledInstance(42, 42, java.awt.Image.SCALE_SMOOTH), 79, 34, null);
                }
            }
        } finally {
            g2.dispose();
        }
        symbologyPreviewLabel.setIcon(new ImageIcon(image));
        symbologyPreviewLabel.setText("<html><div style='text-align:center;'><b>" + escapeHtml(layer.getName()) + "</b><br/>" + escapeHtml(layer.getType()) + "</div></html>");
        symbologyPreviewLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        symbologyPreviewLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
    }

    private java.awt.Stroke buildPreviewStroke() {
        float width = ((Double) lineWidthSpinner.getValue()).floatValue();
        Layer.LineSymbolStyle style = (Layer.LineSymbolStyle) lineStyleCombo.getSelectedItem();
        if (style == Layer.LineSymbolStyle.DASHED) {
            return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{12f, 8f}, 0f);
        }
        if (style == Layer.LineSymbolStyle.DOTTED) {
            return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{2f, 8f}, 0f);
        }
        if (style == Layer.LineSymbolStyle.DASH_DOT) {
            return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{12f, 6f, 2f, 6f}, 0f);
        }
        return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private boolean isPointLayer() {
        String type = layer != null ? layer.getType() : "";
        return type != null && type.toUpperCase().contains("POINT");
    }

    private boolean isLineLayer() {
        String type = layer != null ? layer.getType() : "";
        return type != null && type.toUpperCase().contains("LINE");
    }

    private boolean isPolygonLayer() {
        String type = layer != null ? layer.getType() : "";
        return type != null && type.toUpperCase().contains("POLYGON");
    }

    private void paintPolygonPreview(Graphics2D g2, Rectangle rect, Color fill, Color border, Layer.PolygonFillStyle style) {
        Color safeFill = fill != null ? fill : new Color(120, 170, 255, 120);
        Color safeBorder = border != null ? border : new Color(55, 65, 81);
        if (style == Layer.PolygonFillStyle.SOLID || style == null) {
            g2.setColor(safeFill);
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);
        } else if (style == Layer.PolygonFillStyle.OUTLINE_ONLY) {
            g2.setColor(new Color(255, 255, 255, 0));
        } else {
            g2.setColor(safeFill);
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);
            g2.setColor(safeBorder);
            if (style == Layer.PolygonFillStyle.DIAGONAL_HATCH) {
                for (int i = -rect.height; i < rect.width; i += 10) {
                    g2.drawLine(rect.x + i, rect.y + rect.height, rect.x + i + rect.height, rect.y);
                }
            } else if (style == Layer.PolygonFillStyle.CROSS_HATCH) {
                for (int y = rect.y + 6; y < rect.y + rect.height; y += 10) {
                    g2.drawLine(rect.x + 6, y, rect.x + rect.width - 6, y);
                }
                for (int x = rect.x + 6; x < rect.x + rect.width; x += 10) {
                    g2.drawLine(x, rect.y + 6, x, rect.y + rect.height - 6);
                }
            }
        }
        g2.setColor(safeBorder);
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private JButton createColorButton(Color initial, String title) {
        JButton button = new JButton();
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(120, 30));
        button.setForeground(new Color(30, 41, 59));
        updateColorButtonAppearance(button, initial);
        button.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(this, title, selectedColor(button));
            if (selected != null) {
                updateColorButtonAppearance(button, selected);
                updateSymbologyPreview();
            }
        });
        return button;
    }

    private Color selectedColor(JButton button) {
        Object value = button.getClientProperty("catgis.color");
        return value instanceof Color color ? color : button.getBackground();
    }

    private void updateColorButtonAppearance(JButton button, Color color) {
        Color safe = color != null ? color : Color.WHITE;
        button.setBackground(Color.WHITE);
        button.setText(formatColorText(safe));
        button.setIcon(new ImageIcon(buildColorSwatchIcon(safe)));
        button.setIconTextGap(8);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 228)),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        button.putClientProperty("catgis.color", safe);
    }

    private String formatColorText(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    private BufferedImage buildColorSwatchIcon(Color color) {
        BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setColor(color);
            g2.fillRoundRect(2, 2, 16, 16, 8, 8);
            g2.setColor(new Color(71, 85, 105));
            g2.drawRoundRect(2, 2, 16, 16, 8, 8);
        } finally {
            g2.dispose();
        }
        return image;
    }

    private void applyInitialDialogSize() {
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int targetWidth = Math.min(Math.max(getWidth(), 1080), Math.max(900, screenBounds.width - 70));
        int targetHeight = Math.min(Math.max(getHeight(), 620), Math.max(520, screenBounds.height - 90));
        setMinimumSize(new Dimension(Math.min(targetWidth, 900), Math.min(targetHeight, 520)));
        setSize(targetWidth, targetHeight);
    }

    private class PointStyleRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Layer.PointSymbolStyle style = value instanceof Layer.PointSymbolStyle
                    ? (Layer.PointSymbolStyle) value
                    : Layer.PointSymbolStyle.CIRCLE;
            label.setIcon(new ImageIcon(buildPointStylePreview(style, pointColor)));
            label.setIconTextGap(8);
            return label;
        }
    }

    private class PointCatalogRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof PointSymbolCatalog.Entry entry) {
                label.setText("<html><div style='text-align:center;'><b>" + entry.getLabel()
                        + "</b><br/><span style='font-size:9px;color:#5f6c80;'>" + entry.getCategory() + "</span></div></html>");
                label.setIcon(PointGraphicSymbolSupport.buildPreviewIcon(entry.getReference(), 30));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setVerticalAlignment(JLabel.CENTER);
                label.setHorizontalTextPosition(JLabel.CENTER);
                label.setVerticalTextPosition(JLabel.BOTTOM);
                label.setIconTextGap(8);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(isSelected ? new Color(58, 118, 214) : new Color(214, 220, 228)),
                        BorderFactory.createEmptyBorder(6, 6, 6, 6)
                ));
            }
            return label;
        }
    }

    private BufferedImage buildPointStylePreview(Layer.PointSymbolStyle style, Color color) {
        BufferedImage image = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setColor(color != null ? color : Color.BLUE);
            int x = 11;
            int y = 11;
            int size = 12;
            int half = size / 2;
            switch (style) {
                case SQUARE -> {
                    g2.fillRect(x - half, y - half, size, size);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x - half, y - half, size, size);
                }
                case DIAMOND -> {
                    Path2D diamond = new Path2D.Double();
                    diamond.moveTo(x, y - half);
                    diamond.lineTo(x + half, y);
                    diamond.lineTo(x, y + half);
                    diamond.lineTo(x - half, y);
                    diamond.closePath();
                    g2.fill(diamond);
                    g2.setColor(Color.BLACK);
                    g2.draw(diamond);
                }
                case TRIANGLE -> {
                    Path2D triangle = new Path2D.Double();
                    triangle.moveTo(x, y - half);
                    triangle.lineTo(x + half, y + half);
                    triangle.lineTo(x - half, y + half);
                    triangle.closePath();
                    g2.fill(triangle);
                    g2.setColor(Color.BLACK);
                    g2.draw(triangle);
                }
                case TARGET -> {
                    g2.fillOval(x - half, y - half, size, size);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x - 3, y - 3, 6, 6);
                    g2.setColor(Color.BLACK);
                    g2.drawOval(x - half, y - half, size, size);
                    g2.drawLine(x - half - 2, y, x + half + 2, y);
                    g2.drawLine(x, y - half - 2, x, y + half + 2);
                }
                case PIN -> {
                    Path2D pin = new Path2D.Double();
                    pin.moveTo(x, y + half + 2);
                    pin.lineTo(x + half, y - 1);
                    pin.quadTo(x + half + 1, y - half - 1, x, y - half);
                    pin.quadTo(x - half - 1, y - half - 1, x - half, y - 1);
                    pin.closePath();
                    g2.fill(pin);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x - 2, y - half + 3, 5, 5);
                    g2.setColor(Color.BLACK);
                    g2.draw(pin);
                }
                case FLAG -> {
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawLine(x - 3, y + half, x - 3, y - half);
                    Path2D flag = new Path2D.Double();
                    flag.moveTo(x - 3, y - half + 1);
                    flag.lineTo(x + half, y - half / 2d);
                    flag.lineTo(x - 3, y);
                    flag.closePath();
                    g2.fill(flag);
                }
                case STAR -> {
                    Path2D star = buildStar(x, y, half, Math.max(2, half / 2));
                    g2.fill(star);
                    g2.setColor(Color.BLACK);
                    g2.draw(star);
                }
                case WELL -> {
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D derrick = new Path2D.Double();
                    derrick.moveTo(x, y - half);
                    derrick.lineTo(x + half - 1, y + half);
                    derrick.lineTo(x - half + 1, y + half);
                    derrick.closePath();
                    g2.draw(derrick);
                    g2.drawLine(x - half + 2, y + half, x + half - 2, y + half);
                    g2.drawLine(x - half / 2, y, x + half / 2, y);
                    g2.drawLine(x - half / 2, y, x, y + half);
                    g2.drawLine(x + half / 2, y, x, y + half);
                }
                default -> {
                    g2.fillOval(x - half, y - half, size, size);
                    g2.setColor(Color.BLACK);
                    g2.drawOval(x - half, y - half, size, size);
                }
            }
        } finally {
            g2.dispose();
        }
        return image;
    }

    private Path2D buildStar(double centerX, double centerY, double outerRadius, double innerRadius) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double radius = i % 2 == 0 ? outerRadius : innerRadius;
            double angle = Math.toRadians(-90 + (i * 36));
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + Math.sin(angle) * radius;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }

    private boolean applyChanges() {
        String newName = nameField.getText() != null ? nameField.getText().trim() : "";
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, I18n.t("El nombre de la capa no puede estar vacio."));
            return false;
        }

        String previousSourceCrs = layer.getSourceCRS();
        double previousCadOffsetX = layer.getCadOffsetX();
        double previousCadOffsetY = layer.getCadOffsetY();
        double previousCadScale = layer.getCadScale();
        double previousCadRotation = layer.getCadRotationDegrees();
        String previousCadGeorefMethod = layer.getCadGeoreferenceMethod();
        double previousCadGeorefM00 = layer.getCadGeorefM00();
        double previousCadGeorefM01 = layer.getCadGeorefM01();
        double previousCadGeorefM02 = layer.getCadGeorefM02();
        double previousCadGeorefM10 = layer.getCadGeorefM10();
        double previousCadGeorefM11 = layer.getCadGeorefM11();
        double previousCadGeorefM12 = layer.getCadGeorefM12();
        layer.setName(newName);
        layer.setVisible(visibleCheck.isSelected());
        if (CadLayerSupport.isCadLayer(layer)) {
            layer.setSourceCRS(selectedSourceCrs);
            layer.setCadOffsetX(selectedCadOffsetX);
            layer.setCadOffsetY(selectedCadOffsetY);
            layer.setCadScale(selectedCadScale);
            layer.setCadRotationDegrees(selectedCadRotationDegrees);
            layer.setCadGeoreferenceTransform(
                    selectedCadGeorefMethod,
                    selectedCadGeorefM00,
                    selectedCadGeorefM01,
                    selectedCadGeorefM02,
                    selectedCadGeorefM10,
                    selectedCadGeorefM11,
                    selectedCadGeorefM12
            );
            layer.setCadGeoreferenceDiagnostics(
                    selectedCadGeorefResidualMean,
                    selectedCadGeorefResidualMax,
                    selectedCadGeorefReferenceCount,
                    selectedCadGeorefCheckCount
            );
        }
        applyStyleControlsToLayer(layer);

        CatgisDesktopApp.markProjectDirty();

        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.refreshLayerList();
        }
        if (CatgisDesktopApp.mapPanel != null) {
            if (CadLayerSupport.isCadLayer(layer)
                    && (!safeEquals(CRSDefinitions.normalizeCode(previousSourceCrs), CRSDefinitions.normalizeCode(layer.getSourceCRS()))
                    || Math.abs(previousCadOffsetX - layer.getCadOffsetX()) > 1e-9
                    || Math.abs(previousCadOffsetY - layer.getCadOffsetY()) > 1e-9
                    || Math.abs(previousCadScale - layer.getCadScale()) > 1e-9
                    || Math.abs(previousCadRotation - layer.getCadRotationDegrees()) > 1e-9
                    || !safeEquals(previousCadGeorefMethod, layer.getCadGeoreferenceMethod())
                    || Math.abs(previousCadGeorefM00 - layer.getCadGeorefM00()) > 1e-9
                    || Math.abs(previousCadGeorefM01 - layer.getCadGeorefM01()) > 1e-9
                    || Math.abs(previousCadGeorefM02 - layer.getCadGeorefM02()) > 1e-9
                    || Math.abs(previousCadGeorefM10 - layer.getCadGeorefM10()) > 1e-9
                    || Math.abs(previousCadGeorefM11 - layer.getCadGeorefM11()) > 1e-9
                    || Math.abs(previousCadGeorefM12 - layer.getCadGeorefM12()) > 1e-9)) {
                CatgisDesktopApp.mapPanel.resetView();
            }
            CatgisDesktopApp.mapPanel.repaint();
        }
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.format("Propiedades actualizadas: {0}", layer.getName()));
        }
        return true;
    }

    private void applyStyleControlsToLayer(Layer target) {
        if (target == null) {
            return;
        }
        target.setFillColor(withAlpha(selectedColor(fillColorButton), 120));
        target.setBorderColor(selectedColor(borderColorButton));
        target.setLineColor(selectedColor(lineColorButton));
        target.setLineWidth(((Double) lineWidthSpinner.getValue()).floatValue());
        target.setLineSymbolStyle((Layer.LineSymbolStyle) lineStyleCombo.getSelectedItem());
        target.setPolygonFillStyle((Layer.PolygonFillStyle) polygonStyleCombo.getSelectedItem());
        target.setPointColor(selectedColor(pointColorButton));
        target.setPointSize((Integer) pointSizeSpinner.getValue());
        target.setPointSymbolStyle((Layer.PointSymbolStyle) pointStyleCombo.getSelectedItem());
        target.setPointGraphicSymbol(resolvePointGraphicReference());

        if (labelsVisibleCheck.isSelected() && labelFieldCombo.isEnabled() && labelFieldCombo.getSelectedItem() != null) {
            target.setLabelsVisible(true);
            target.setLabelField(labelFieldCombo.getSelectedItem().toString());
        } else {
            target.setLabelsVisible(false);
            target.setLabelField(null);
        }
    }

    private Layer buildExportSnapshot() {
        Layer snapshot = new Layer(layer.getName(), layer.getPath(), layer.getType());
        snapshot.setVisible(layer.isVisible());
        snapshot.setSourceName(layer.getSourceName());
        snapshot.setFeatureCount(layer.getFeatureCount());
        snapshot.setSourceCRS(layer.getSourceCRS());
        snapshot.setGroupName(layer.getGroupName());
        VectorLayerUtils.copyLayerAppearance(layer, snapshot);
        applyStyleControlsToLayer(snapshot);
        return snapshot;
    }

    private JPanel buildCadCrsEditor() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(sourceCrsField, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        buttons.add(sourceCrsButton);
        buttons.add(clearSourceCrsButton);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildCadAdjustmentEditor() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(cadAdjustmentField, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        buttons.add(cadAdjustmentButton);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildCadGeorefEditor() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(cadGeorefField, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        buttons.add(cadGeorefButton);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private void editCadSourceCrs() {
        CadCrsAssignmentDialog.Result result = CadCrsAssignmentDialog.chooseForLayer(this, layer);
        if (!result.approved()) {
            return;
        }
        selectedSourceCrs = result.sourceCrs();
        refreshSourceCrsField();
    }

    private void refreshSourceCrsField() {
        sourceCrsField.setText(CadLayerSupport.formatSourceCrsLabel(selectedSourceCrs));
    }

    private void editCadGeoreference() {
        Layer snapshot = new Layer(layer.getName(), layer.getPath(), layer.getType());
        snapshot.setCadGeoreferenceTransform(
                selectedCadGeorefMethod,
                selectedCadGeorefM00,
                selectedCadGeorefM01,
                selectedCadGeorefM02,
                selectedCadGeorefM10,
                selectedCadGeorefM11,
                selectedCadGeorefM12
        );
        CadGeoreferenceSupport.Result result = CadGeoreferenceDialog.open((Frame) getOwner(), snapshot);
        if (!result.approved()) {
            return;
        }
        Layer preview = new Layer(layer.getName(), layer.getPath(), layer.getType());
        CadGeoreferenceSupport.applyResultToLayer(preview, result);
        selectedCadGeorefMethod = preview.getCadGeoreferenceMethod();
        selectedCadGeorefM00 = preview.getCadGeorefM00();
        selectedCadGeorefM01 = preview.getCadGeorefM01();
        selectedCadGeorefM02 = preview.getCadGeorefM02();
        selectedCadGeorefM10 = preview.getCadGeorefM10();
        selectedCadGeorefM11 = preview.getCadGeorefM11();
        selectedCadGeorefM12 = preview.getCadGeorefM12();
        selectedCadGeorefResidualMean = preview.getCadGeorefResidualMean();
        selectedCadGeorefResidualMax = preview.getCadGeorefResidualMax();
        selectedCadGeorefReferenceCount = preview.getCadGeorefReferenceCount();
        selectedCadGeorefCheckCount = preview.getCadGeorefCheckCount();
        cadGeorefField.setText(CadGeoreferenceSupport.buildDetailedSummary(preview));
    }

    private void editCadPlacement() {
        Layer snapshot = new Layer(layer.getName(), layer.getPath(), layer.getType());
        snapshot.setCadOffsetX(selectedCadOffsetX);
        snapshot.setCadOffsetY(selectedCadOffsetY);
        snapshot.setCadScale(selectedCadScale);
        snapshot.setCadRotationDegrees(selectedCadRotationDegrees);
        CadPlacementSupport.Result result = CadPlacementDialog.open((Frame) getOwner(), snapshot);
        if (!result.approved()) {
            return;
        }
        selectedCadOffsetX = result.offsetX();
        selectedCadOffsetY = result.offsetY();
        selectedCadScale = result.scale();
        selectedCadRotationDegrees = result.rotationDegrees();
        cadAdjustmentField.setText(String.format(
                java.util.Locale.US,
                "Dx %.3f | Dy %.3f | Esc %.6f | Rot %.3f°",
                selectedCadOffsetX,
                selectedCadOffsetY,
                selectedCadScale,
                selectedCadRotationDegrees
        ));
    }

    private boolean safeEquals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private void importSldStyle() {
        try {
            JFileChooser chooser = FileChooserSupport.createChooser("layer-style-sld-import", "Importar estilo SLD");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Styled Layer Descriptor (*.sld)", "sld"));
            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = chooser.getSelectedFile();
            if (file == null) {
                return;
            }

            ShapefileData data = OpenAttributeTableAction.ensureLayerDataAvailable(layer);
            LayerSldStyleIO.StyleImportResult importResult = LayerSldStyleIO.importStyle(layer, file, data);
            FileChooserSupport.rememberSelection("layer-style-sld-import", chooser);
            syncStyleControlsFromLayer();
            CatgisDesktopApp.markProjectDirty();
            if (CatgisDesktopApp.layersPanel != null) {
                CatgisDesktopApp.layersPanel.refreshLayerList();
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.repaint();
            }
            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("Estilo SLD importado: " + layer.getName());
            }
            JOptionPane.showMessageDialog(
                    this,
                    importResult.categorized()
                            ? "SLD importado como simbologia por atributo.\nGeometria: " + importResult.geometryFamily()
                            + "\nCampo: " + importResult.fieldName()
                            + "\nReglas: " + importResult.ruleCount()
                            : "SLD importado como estilo simple.\nGeometria: " + importResult.geometryFamily()
                            + "\nReglas leidas: " + importResult.ruleCount(),
                    "Importar estilo SLD",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo importar el estilo SLD:\n" + ex.getMessage(),
                    "Importar estilo SLD",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void exportSldStyle() {
        try {
            JFileChooser chooser = FileChooserSupport.createChooser("layer-style-sld-export", "Exportar estilo SLD");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Styled Layer Descriptor (*.sld)", "sld"));
            chooser.setSelectedFile(new File(layer.getName().replaceAll("[^A-Za-z0-9._-]+", "_") + ".sld"));
            int result = chooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = ensureSldExtension(chooser.getSelectedFile());
            if (file == null) {
                return;
            }

            Layer snapshot = buildExportSnapshot();
            ShapefileData data = OpenAttributeTableAction.ensureLayerDataAvailable(layer);
            LayerSldStyleIO.exportStyle(snapshot, file, data);
            FileChooserSupport.rememberSelection("layer-style-sld-export", chooser);
            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("Estilo SLD exportado: " + file.getName());
            }
            JOptionPane.showMessageDialog(
                    this,
                    "Estilo exportado correctamente:\n" + file.getAbsolutePath(),
                    "Exportar estilo SLD",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo exportar el estilo SLD:\n" + ex.getMessage(),
                    "Exportar estilo SLD",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private File ensureSldExtension(File file) {
        if (file == null) {
            return null;
        }
        String path = file.getAbsolutePath();
        return path.toLowerCase().endsWith(".sld") ? file : new File(path + ".sld");
    }

    private void syncStyleControlsFromLayer() {
        updateColorButtonAppearance(fillColorButton, layer.getFillColor());
        updateColorButtonAppearance(borderColorButton, layer.getBorderColor());
        updateColorButtonAppearance(lineColorButton, layer.getLineColor());
        updateColorButtonAppearance(pointColorButton, layer.getPointColor());
        lineWidthSpinner.setValue((double) layer.getLineWidth());
        pointSizeSpinner.setValue(layer.getPointSize());
        lineStyleCombo.setSelectedItem(layer.getLineSymbolStyle());
        polygonStyleCombo.setSelectedItem(layer.getPolygonFillStyle());
        pointStyleCombo.setSelectedItem(layer.getPointSymbolStyle());
        labelsVisibleCheck.setSelected(layer.isLabelsVisible());
        if (layer.getLabelField() != null && labelFieldCombo.isEnabled()) {
            labelFieldCombo.setSelectedItem(layer.getLabelField());
        }

        PointSymbolCatalog.Entry entry = PointSymbolCatalog.findByReference(layer.getPointGraphicSymbol());
        if (entry != null) {
            pointGraphicCheck.setSelected(true);
            pointCategoryCombo.setSelectedItem(entry.getCategory());
            reloadPointCatalogEntries(entry);
            pointGraphicPathField.setText("");
        } else {
            pointGraphicCheck.setSelected(layer.hasPointGraphicSymbol());
            pointGraphicPathField.setText(layer.getPointGraphicSymbol());
            pointCatalogList.clearSelection();
            reloadPointCatalogEntries(null);
        }
        updatePointGraphicControls();
        updateCatalogSelectionLabel();
        updatePointGraphicPreview();
        updateSymbologyPreview();
    }

    private Color withAlpha(Color color, int alpha) {
        if (color == null) {
            return new Color(120, 170, 255, alpha);
        }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private String buildPathDisplay(Layer layer) {
        if (layer == null || layer.getPath() == null || layer.getPath().isBlank()) {
            return I18n.t("Sin archivo asociado");
        }

        String path = layer.getPath().trim();
        String lower = path.toLowerCase();
        boolean looksStored = new java.io.File(path).isAbsolute()
                || new java.io.File(path).exists()
                || lower.endsWith(".shp")
                || lower.endsWith(".geojson")
                || lower.endsWith(".json")
                || lower.endsWith(".gpx")
                || lower.endsWith(".kml")
                || lower.endsWith(".kmz")
                || lower.endsWith(".dxf")
                || lower.endsWith(".dwg")
                || lower.endsWith(".tif")
                || lower.endsWith(".tiff")
                || lower.endsWith(".img")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".bmp")
                || lower.endsWith(".gif");

        return looksStored ? path : I18n.t("Sin archivo asociado");
    }

    private void choosePointGraphicFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("point-graphic-symbol", I18n.t("Cargar icono puntual"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("Iconos graficos (*.svg, *.png, *.jpg, *.jpeg, *.gif, *.bmp)"), "svg", "png", "jpg", "jpeg", "gif", "bmp"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file == null) {
            return;
        }
        pointGraphicCheck.setSelected(true);
        pointCatalogList.clearSelection();
        pointGraphicPathField.setText(file.getAbsolutePath());
        FileChooserSupport.rememberSelection("point-graphic-symbol", chooser);
        updatePointGraphicControls();
        updateCatalogSelectionLabel();
        updatePointGraphicPreview();
    }

    private void updatePointGraphicControls() {
        boolean enabled = pointGraphicCheck.isSelected();
        pointCategoryCombo.setEnabled(enabled);
        pointCatalogList.setEnabled(enabled);
        pointGraphicPathField.setEnabled(enabled);
        pointGraphicPreviewLabel.setEnabled(enabled);
        pointCatalogSelectionLabel.setEnabled(enabled);
        browsePointGraphicButton.setEnabled(enabled);
        clearPointGraphicButton.setEnabled(enabled);
        updateCatalogSelectionLabel();
        updatePointGraphicPreview();
        updateSymbologyPreview();
    }

    private void updatePointGraphicPreview() {
        String reference = resolvePointGraphicReference();
        ImageIcon previewIcon = PointGraphicSymbolSupport.buildPreviewIcon(reference, 28);
        pointGraphicPreviewLabel.setIcon(previewIcon);
        if (!pointGraphicCheck.isSelected()) {
            pointGraphicPreviewLabel.setText(I18n.t("Simbologia geometrica activa"));
        } else if (previewIcon != null) {
            pointGraphicPreviewLabel.setText(reference.startsWith("catalog:")
                    ? I18n.t("Catalogo activo")
                    : I18n.t("Icono cargado"));
        } else if (reference.isBlank()) {
            pointGraphicPreviewLabel.setText(I18n.t("Sin icono grafico"));
        } else {
            pointGraphicPreviewLabel.setText(I18n.t("No se pudo leer el icono"));
        }
    }

    private String resolvePointGraphicReference() {
        if (!pointGraphicCheck.isSelected()) {
            return "";
        }
        String customPath = pointGraphicPathField.getText() != null ? pointGraphicPathField.getText().trim() : "";
        if (!customPath.isBlank()) {
            return customPath;
        }
        PointSymbolCatalog.Entry selectedEntry = pointCatalogList.getSelectedValue();
        if (selectedEntry != null) {
            return selectedEntry.getReference();
        }
        return "";
    }

    private void reloadPointCatalogEntries(PointSymbolCatalog.Entry preferredEntry) {
        pointCatalogListModel.clear();
        String selectedCategory = pointCategoryCombo.getSelectedItem() != null
                ? pointCategoryCombo.getSelectedItem().toString()
                : "";
        List<PointSymbolCatalog.Entry> entries = I18n.t("Todas las categorias").equals(selectedCategory)
                ? PointSymbolCatalog.entries()
                : PointSymbolCatalog.entriesForCategory(selectedCategory);
        for (PointSymbolCatalog.Entry entry : entries) {
            pointCatalogListModel.addElement(entry);
        }

        if (preferredEntry != null && entries.contains(preferredEntry)) {
            pointCatalogList.setSelectedValue(preferredEntry, true);
        } else {
            pointCatalogList.clearSelection();
        }
        updateCatalogSelectionLabel();
    }

    private void updateCatalogSelectionLabel() {
        PointSymbolCatalog.Entry selectedEntry = pointCatalogList.getSelectedValue();
        if (selectedEntry != null) {
            pointCatalogSelectionLabel.setText(selectedEntry.getCategory() + " | " + selectedEntry.getLabel());
        } else if (pointGraphicPathField.getText() != null && !pointGraphicPathField.getText().trim().isEmpty()) {
            pointCatalogSelectionLabel.setText(I18n.t("Icono externo seleccionado"));
        } else {
            pointCatalogSelectionLabel.setText(I18n.t("Sin simbolo de catalogo seleccionado"));
        }
    }

    public static void open(Layer layer) {
        Frame owner = JOptionPane.getFrameForComponent(CatgisDesktopApp.layersPanel);
        LayerPropertiesDialog dialog = new LayerPropertiesDialog(owner, layer);
        dialog.setVisible(true);
    }

    public static void open(Component parent, Layer layer) {
        Window window = parent != null ? javax.swing.SwingUtilities.getWindowAncestor(parent) : null;
        Frame owner = window instanceof Frame ? (Frame) window : JOptionPane.getFrameForComponent(parent);
        LayerPropertiesDialog dialog = new LayerPropertiesDialog(owner, layer);
        if (parent != null) {
            dialog.setLocationRelativeTo(parent);
        }
        dialog.setVisible(true);
    }
}
