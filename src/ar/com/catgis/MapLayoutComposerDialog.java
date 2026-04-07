package ar.com.catgis;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class MapLayoutComposerDialog extends JDialog {

    private static final DateTimeFormatter FOOTER_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JTextField titleField;
    private final JTextField subtitleField;
    private final JTextField footerField;
    private final JTextField studyField;
    private final JTextField companyField;
    private final JTextField cartographerField;
    private final JTextField imageSourceField;
    private final JTextField coordinateReferenceField;
    private final JTextField legendTitleField;
    private final JTextField legendSubtitleField;
    private final JTextField logoPathField;
    private final JTextField layoutImagePathField;
    private final JComboBox<LayoutTemplate> templateCombo;
    private final JComboBox<PageSizePreset> pageSizeCombo;
    private final JComboBox<PageOrientation> orientationCombo;
    private final JComboBox<Integer> dpiCombo;
    private final JComboBox<LegendPlacement> legendPlacementCombo;
    private final JComboBox<ScaleStyle> scaleStyleCombo;
    private final JComboBox<ScaleRule> scaleRuleCombo;
    private final JComboBox<NorthStyle> northStyleCombo;
    private final JCheckBox northCheck;
    private final JCheckBox scaleCheck;
    private final JCheckBox legendCheck;
    private final JCheckBox gridCheck;
    private final JCheckBox gridLabelsCheck;
    private final JSpinner gridColumnsSpinner;
    private final JSpinner gridRowsSpinner;
    private final LayoutInteractionState interactionState;
    private final LayoutPreviewPanel previewPanel;
    private final JLabel currentMapLabel;
    private final JLabel statusLabel;
    private LayoutSnapshot snapshot;

    private MapLayoutComposerDialog(Window owner) {
        super(owner, "Composicion cartografica", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        snapshot = captureSnapshot();
        interactionState = new LayoutInteractionState();

        titleField = new JTextField(defaultTitle(), 24);
        subtitleField = new JTextField(defaultSubtitle(), 24);
        footerField = new JTextField(defaultFooter(), 24);
        studyField = new JTextField(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getStudyName() : "", 24);
        companyField = new JTextField(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getCompanyName() : "", 24);
        cartographerField = new JTextField(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getCartographerName() : "", 24);
        imageSourceField = new JTextField(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getImageSource() : "", 24);
        coordinateReferenceField = new JTextField(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getCoordinateReference() : "", 24);
        legendTitleField = new JTextField(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getLegendTitle() : "Leyenda", 24);
        legendSubtitleField = new JTextField(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getLegendSubtitle() : "Capas visibles del mapa", 24);
        logoPathField = new JTextField(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getLogoPath() : "", 24);
        logoPathField.setEditable(false);
        layoutImagePathField = new JTextField(CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getLayoutImagePath() : "", 24);
        layoutImagePathField.setEditable(false);
        templateCombo = new JComboBox<>(LayoutTemplate.values());
        templateCombo.setSelectedItem(interactionState.getTemplate());
        pageSizeCombo = new JComboBox<>(PageSizePreset.values());
        orientationCombo = new JComboBox<>(PageOrientation.values());
        dpiCombo = new JComboBox<>(new Integer[]{150, 200, 300});
        dpiCombo.setSelectedItem(200);
        legendPlacementCombo = new JComboBox<>(LegendPlacement.values());
        scaleStyleCombo = new JComboBox<>(ScaleStyle.values());
        scaleRuleCombo = new JComboBox<>(ScaleRule.values());
        northStyleCombo = new JComboBox<>(NorthStyle.values());
        northCheck = new JCheckBox("Norte", true);
        scaleCheck = new JCheckBox("Escala grafica", true);
        legendCheck = new JCheckBox("Leyenda", true);
        gridCheck = new JCheckBox("Grilla cartografica", true);
        gridLabelsCheck = new JCheckBox("Etiquetas de grilla", true);
        gridColumnsSpinner = new JSpinner(new SpinnerNumberModel(3, 2, 20, 1));
        gridRowsSpinner = new JSpinner(new SpinnerNumberModel(3, 2, 20, 1));
        previewPanel = new LayoutPreviewPanel();
        currentMapLabel = new JLabel();
        statusLabel = new JLabel("Layout listo para exportar o imprimir.");

        add(buildControlsScrollPane(), BorderLayout.WEST);
        add(buildPreviewContainer(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        updateCurrentMapLabel();
        installListeners();
        applyTemplateDefaults((LayoutTemplate) templateCombo.getSelectedItem(), false);

        setSize(1380, 900);
        setMinimumSize(new Dimension(1120, 760));
        setLocationRelativeTo(owner);
    }

    public static void open() {
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        MapLayoutComposerDialog dialog = new MapLayoutComposerDialog(owner);
        dialog.setVisible(true);
    }

    public static void openWithLayoutImage(File imageFile) {
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        MapLayoutComposerDialog dialog = new MapLayoutComposerDialog(owner);
        if (imageFile != null) {
            dialog.layoutImagePathField.setText(imageFile.getAbsolutePath());
            dialog.pushProjectMetadataFromControls();
            dialog.interactionState.select(LayoutElementType.PROFILE_IMAGE);
            dialog.previewPanel.repaint();
            dialog.statusLabel.setText(I18n.t("Imagen de perfil cargada en el layout. Puedes moverla o redimensionarla."));
        }
        dialog.setVisible(true);
    }

    private JScrollPane buildControlsScrollPane() {
        JScrollPane scrollPane = new JScrollPane(buildControlsPanel());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(378, 100));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private JPanel buildPreviewContainer() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(buildPreviewToolbar(), BorderLayout.NORTH);
        panel.add(previewPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPreviewToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton exportImageButton = new JButton("Exportar imagen");
        exportImageButton.addActionListener(e -> exportImage());

        JButton exportPdfButton = new JButton("Exportar PDF");
        exportPdfButton.addActionListener(e -> exportPdf());

        JButton printButton = new JButton("Imprimir...");
        printButton.addActionListener(e -> printLayout());

        JButton fitPageButton = new JButton("Ajustar pagina");
        fitPageButton.addActionListener(e -> {
            interactionState.fitPage();
            statusLabel.setText("Vista del compositor ajustada a pagina.");
            previewPanel.repaint();
        });

        JButton fitWidthButton = new JButton("Ajustar ancho");
        fitWidthButton.addActionListener(e -> {
            interactionState.fitWidth();
            statusLabel.setText("Vista del compositor ajustada al ancho.");
            previewPanel.repaint();
        });

        JButton zoomOutButton = new JButton("Zoom -");
        zoomOutButton.addActionListener(e -> {
            interactionState.zoomPreview(1d / 1.15d);
            statusLabel.setText("Zoom de composicion reducido.");
            previewPanel.repaint();
        });

        JButton zoomInButton = new JButton("Zoom +");
        zoomInButton.addActionListener(e -> {
            interactionState.zoomPreview(1.15d);
            statusLabel.setText("Zoom de composicion ampliado.");
            previewPanel.repaint();
        });

        JButton mapFitButton = new JButton("Encajar mapa");
        mapFitButton.addActionListener(e -> {
            interactionState.resetMapView();
            statusLabel.setText("Mapa reencuadrado dentro del cuadro.");
            previewPanel.repaint();
        });

        JButton resetLayoutButton = new JButton("Restablecer layout");
        resetLayoutButton.addActionListener(e -> {
            interactionState.resetForTemplate((LayoutTemplate) templateCombo.getSelectedItem());
            applyTemplateDefaults((LayoutTemplate) templateCombo.getSelectedItem(), false);
            statusLabel.setText("Layout restablecido segun la plantilla seleccionada.");
            previewPanel.repaint();
        });

        JButton loadLayoutImageButton = new JButton("Imagen adicional...");
        loadLayoutImageButton.addActionListener(e -> chooseLayoutImageFile());

        toolbar.add(exportImageButton);
        toolbar.add(exportPdfButton);
        toolbar.add(printButton);
        toolbar.add(loadLayoutImageButton);
        toolbar.add(fitPageButton);
        toolbar.add(fitWidthButton);
        toolbar.add(zoomOutButton);
        toolbar.add(zoomInButton);
        toolbar.add(mapFitButton);
        toolbar.add(resetLayoutButton);
        return toolbar;
    }

    private JPanel buildControlsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setPreferredSize(new Dimension(352, 1260));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JLabel header = new JLabel("Compositor cartografico");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        header.setForeground(new Color(27, 38, 56));
        panel.add(header, gc);

        gc.gridy++;
        JLabel info = new JLabel("<html>Salida cartografica del mapa actual con cartucho, grilla, escala, norte, leyenda y exportacion.</html>");
        info.setFont(info.getFont().deriveFont(Font.PLAIN, 11.5f));
        info.setForeground(new Color(88, 98, 112));
        panel.add(info, gc);

        gc.gridy++;
        addField(panel, gc, "Titulo", titleField);
        gc.gridy += 2;
        addField(panel, gc, "Subtitulo", subtitleField);
        gc.gridy += 2;
        addField(panel, gc, "Pie o referencia", footerField);
        gc.gridy += 2;

        addSection(panel, gc, "Pagina y escala");
        gc.gridy++;
        addField(panel, gc, "Plantilla de layout", templateCombo);
        gc.gridy += 2;
        addField(panel, gc, "Tamano de pagina", pageSizeCombo);
        gc.gridy += 2;
        addField(panel, gc, "Orientacion", orientationCombo);
        gc.gridy += 2;
        addField(panel, gc, "Resolucion de salida", dpiCombo);
        gc.gridy += 2;
        addField(panel, gc, "Tipo de escala", scaleStyleCombo);
        gc.gridy += 2;
        addField(panel, gc, "Regla de escala", scaleRuleCombo);
        gc.gridy += 2;
        panel.add(scaleCheck, gc);
        gc.gridy++;

        addSection(panel, gc, "Grilla cartografica");
        gc.gridy++;
        panel.add(gridCheck, gc);
        gc.gridy++;
        addField(panel, gc, "Columnas", gridColumnsSpinner);
        gc.gridy += 2;
        addField(panel, gc, "Filas", gridRowsSpinner);
        gc.gridy += 2;
        panel.add(gridLabelsCheck, gc);
        gc.gridy++;

        addSection(panel, gc, "Leyenda y norte");
        gc.gridy++;
        addField(panel, gc, "Ubicacion de leyenda", legendPlacementCombo);
        gc.gridy += 2;
        addField(panel, gc, "Titulo de leyenda", legendTitleField);
        gc.gridy += 2;
        addField(panel, gc, "Subtitulo de leyenda", legendSubtitleField);
        gc.gridy += 2;
        addField(panel, gc, "Simbolo de norte", northStyleCombo);
        gc.gridy += 2;
        panel.add(northCheck, gc);
        gc.gridy++;
        panel.add(legendCheck, gc);
        gc.gridy++;

        addSection(panel, gc, "Cartucho y metadatos");
        gc.gridy++;
        addField(panel, gc, "Nombre del estudio", studyField);
        gc.gridy += 2;
        addField(panel, gc, "Empresa", companyField);
        gc.gridy += 2;
        addField(panel, gc, "Cartografo", cartographerField);
        gc.gridy += 2;
        addField(panel, gc, "Origen de la imagen", imageSourceField);
        gc.gridy += 2;
        addField(panel, gc, "Coordenadas / referencia", coordinateReferenceField);
        gc.gridy += 2;
        addLogoSelector(panel, gc);
        gc.gridy += 2;
        addLayoutImageSelector(panel, gc);
        gc.gridy += 2;

        addSection(panel, gc, "Estado del mapa");
        gc.gridy++;
        gc.insets = new Insets(10, 6, 4, 6);
        currentMapLabel.setForeground(new Color(63, 74, 88));
        panel.add(currentMapLabel, gc);
        gc.gridy++;
        gc.insets = new Insets(10, 6, 6, 6);
        JButton refreshButton = new JButton("Actualizar mapa", AppIcons.attrRefreshIcon());
        refreshButton.addActionListener(e -> refreshSnapshot());
        panel.add(refreshButton, gc);
        gc.gridy++;
        gc.weighty = 1;
        panel.add(new JLabel(""), gc);

        return panel;
    }

    private void addSection(JPanel panel, GridBagConstraints gc, String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(new Color(27, 38, 56));
        panel.add(label, gc);
    }

    private void addField(JPanel panel, GridBagConstraints gc, String labelText, java.awt.Component field) {
        panel.add(new JLabel(labelText + ":"), gc);
        gc.gridy++;
        panel.add(field, gc);
    }

    private void addLogoSelector(JPanel panel, GridBagConstraints gc) {
        panel.add(new JLabel("Logo de empresa:"), gc);
        gc.gridy++;
        panel.add(logoPathField, gc);
        gc.gridy++;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        JButton chooseButton = new JButton("Cargar logo...");
        chooseButton.addActionListener(e -> chooseLogoFile());
        JButton clearButton = new JButton("Quitar logo");
        clearButton.addActionListener(e -> {
            logoPathField.setText("");
            pushProjectMetadataFromControls();
            previewPanel.repaint();
        });
        buttons.add(chooseButton);
        buttons.add(clearButton);
        panel.add(buttons, gc);
    }

    private void addLayoutImageSelector(JPanel panel, GridBagConstraints gc) {
        panel.add(new JLabel(I18n.t("Imagen de perfil / grafico:")), gc);
        gc.gridy++;
        panel.add(layoutImagePathField, gc);
        gc.gridy++;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        JButton chooseButton = new JButton(I18n.t("Cargar imagen..."));
        chooseButton.addActionListener(e -> chooseLayoutImageFile());
        JButton clearButton = new JButton(I18n.t("Quitar imagen"));
        clearButton.addActionListener(e -> {
            layoutImagePathField.setText("");
            pushProjectMetadataFromControls();
            previewPanel.repaint();
        });
        buttons.add(chooseButton);
        buttons.add(clearButton);
        panel.add(buttons, gc);
    }

    private void applyTemplateDefaults(LayoutTemplate template, boolean resetLayoutState) {
        LayoutTemplate resolved = template != null ? template : LayoutTemplate.TECHNICAL_RIGHT;
        if (resetLayoutState) {
            interactionState.resetForTemplate(resolved);
        }
        if (legendPlacementCombo.getSelectedItem() != resolved.defaultLegendPlacement()) {
            legendPlacementCombo.setSelectedItem(resolved.defaultLegendPlacement());
        }
        if (resolved == LayoutTemplate.BOTTOM_REFERENCE) {
            if (northStyleCombo.getSelectedItem() != NorthStyle.CLASSIC) {
                northStyleCombo.setSelectedItem(NorthStyle.CLASSIC);
            }
        } else if (resolved == LayoutTemplate.CLEAN_CENTERED) {
            if (northStyleCombo.getSelectedItem() != NorthStyle.MODERN) {
                northStyleCombo.setSelectedItem(NorthStyle.MODERN);
            }
        }
    }

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
        bottom.setOpaque(false);

        statusLabel.setForeground(new Color(77, 86, 100));
        bottom.add(statusLabel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton exportImageButton = new JButton("Exportar imagen", AppIcons.exportIcon());
        exportImageButton.addActionListener(e -> exportImage());
        JButton exportPdfButton = new JButton("Exportar PDF", AppIcons.saveIcon());
        exportPdfButton.addActionListener(e -> exportPdf());
        JButton printButton = new JButton("Imprimir...", AppIcons.projectIcon());
        printButton.addActionListener(e -> printLayout());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        buttons.add(exportImageButton);
        buttons.add(exportPdfButton);
        buttons.add(printButton);
        buttons.add(closeButton);
        bottom.add(buttons, BorderLayout.EAST);
        return bottom;
    }

    private void chooseLogoFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("layout-logo", "Seleccionar logo de empresa");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Imagenes (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file != null) {
            FileChooserSupport.rememberSelection("layout-logo", chooser);
            logoPathField.setText(file.getAbsolutePath());
            pushProjectMetadataFromControls();
            previewPanel.repaint();
        }
    }

    private void chooseLayoutImageFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("layout-image", I18n.t("Seleccionar imagen para el layout"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Imagenes (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file != null) {
            FileChooserSupport.rememberSelection("layout-image", chooser);
            layoutImagePathField.setText(file.getAbsolutePath());
            pushProjectMetadataFromControls();
            interactionState.select(LayoutElementType.PROFILE_IMAGE);
            statusLabel.setText(I18n.t("Imagen de perfil cargada en el layout. Puedes moverla o redimensionarla."));
            previewPanel.repaint();
        }
    }

    private void installListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleUpdate();
            }

            private void handleUpdate() {
                pushProjectMetadataFromControls();
                previewPanel.repaint();
            }
        };
        titleField.getDocument().addDocumentListener(listener);
        subtitleField.getDocument().addDocumentListener(listener);
        footerField.getDocument().addDocumentListener(listener);
        studyField.getDocument().addDocumentListener(listener);
        companyField.getDocument().addDocumentListener(listener);
        cartographerField.getDocument().addDocumentListener(listener);
        imageSourceField.getDocument().addDocumentListener(listener);
        coordinateReferenceField.getDocument().addDocumentListener(listener);
        legendTitleField.getDocument().addDocumentListener(listener);
        legendSubtitleField.getDocument().addDocumentListener(listener);
        templateCombo.addActionListener(e -> {
            LayoutTemplate template = (LayoutTemplate) templateCombo.getSelectedItem();
            interactionState.setTemplate(template);
            applyTemplateDefaults(template, true);
            statusLabel.setText("Plantilla activa: " + (template != null ? template.toString() : "Tecnica"));
            previewPanel.repaint();
        });
        pageSizeCombo.addActionListener(e -> previewPanel.repaint());
        orientationCombo.addActionListener(e -> previewPanel.repaint());
        dpiCombo.addActionListener(e -> previewPanel.repaint());
        legendPlacementCombo.addActionListener(e -> previewPanel.repaint());
        scaleStyleCombo.addActionListener(e -> previewPanel.repaint());
        scaleRuleCombo.addActionListener(e -> previewPanel.repaint());
        northStyleCombo.addActionListener(e -> previewPanel.repaint());
        northCheck.addActionListener(e -> previewPanel.repaint());
        scaleCheck.addActionListener(e -> previewPanel.repaint());
        legendCheck.addActionListener(e -> previewPanel.repaint());
        gridCheck.addActionListener(e -> previewPanel.repaint());
        gridLabelsCheck.addActionListener(e -> previewPanel.repaint());
        gridColumnsSpinner.addChangeListener(e -> previewPanel.repaint());
        gridRowsSpinner.addChangeListener(e -> previewPanel.repaint());
    }

    private void pushProjectMetadataFromControls() {
        if (CatgisDesktopApp.currentProject == null) {
            return;
        }
        Project project = CatgisDesktopApp.currentProject;
        boolean changed = false;
        changed |= updateString(project.getStudyName(), studyField.getText(), project::setStudyName);
        changed |= updateString(project.getCompanyName(), companyField.getText(), project::setCompanyName);
        changed |= updateString(project.getCartographerName(), cartographerField.getText(), project::setCartographerName);
        changed |= updateString(project.getImageSource(), imageSourceField.getText(), project::setImageSource);
        changed |= updateString(project.getCoordinateReference(), coordinateReferenceField.getText(), project::setCoordinateReference);
        changed |= updateString(project.getLegendTitle(), legendTitleField.getText(), project::setLegendTitle);
        changed |= updateString(project.getLegendSubtitle(), legendSubtitleField.getText(), project::setLegendSubtitle);
        changed |= updateString(project.getLogoPath(), logoPathField.getText(), project::setLogoPath);
        changed |= updateString(project.getLayoutImagePath(), layoutImagePathField.getText(), project::setLayoutImagePath);
        if (changed) {
            CatgisDesktopApp.markProjectDirty();
        }
    }

    private boolean updateString(String current, String updated, java.util.function.Consumer<String> setter) {
        String normalized = safeTrim(updated);
        String currentNormalized = safeTrim(current);
        if (!currentNormalized.equals(normalized)) {
            setter.accept(normalized);
            return true;
        }
        return false;
    }

    private void refreshSnapshot() {
        snapshot = captureSnapshot();
        updateCurrentMapLabel();
        statusLabel.setText("Mapa del layout actualizado desde la vista actual.");
        previewPanel.repaint();
    }

    private void updateCurrentMapLabel() {
        currentMapLabel.setText("<html><b>Mapa actual:</b> " + escape(snapshot.projectName()) +
                "<br><b>CRS:</b> " + escape(snapshot.projectCrsLabel()) +
                "<br><b>Capas visibles:</b> " + snapshot.visibleLayers().size() +
                "<br><b>Escala de referencia:</b> " + escape(snapshot.scaleLabel()) + "</html>");
    }

    private void exportImage() {
        JFileChooser chooser = FileChooserSupport.createChooser("layout-export", "Exportar composicion cartografica");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPG (*.jpg, *.jpeg)", "jpg", "jpeg"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        String lower = file.getName().toLowerCase();
        String format = lower.endsWith(".jpg") || lower.endsWith(".jpeg") ? "jpg" : "png";
        if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) {
            file = new File(file.getAbsolutePath() + ".png");
        }
        FileChooserSupport.rememberFile("layout-export", file);

        try {
            LayoutSettings settings = buildSettings();
            BufferedImage image = renderLayout(settings, settings.pageSize().pixelSize(settings.orientation(), settings.dpi()));
            BufferedImage output = image;
            if ("jpg".equalsIgnoreCase(format) && image.getType() != BufferedImage.TYPE_INT_RGB) {
                output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = output.createGraphics();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, output.getWidth(), output.getHeight());
                g2.drawImage(image, 0, 0, null);
                g2.dispose();
            }
            ImageIO.write(output, format, file);
            announceExport("Composicion exportada", file);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo exportar la composicion:\n" + ex.getMessage(), "Composicion", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPdf() {
        JFileChooser chooser = FileChooserSupport.createChooser("layout-export", "Exportar composicion a PDF");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }
        FileChooserSupport.rememberFile("layout-export", file);

        try {
            LayoutRenderer.exportPdf(buildSettings(), snapshot, file, interactionState);
            announceExport("Composicion PDF exportada", file);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo exportar el PDF:\n" + ex.getMessage(), "Composicion", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printLayout() {
        try {
            LayoutSettings settings = buildSettings();
            BufferedImage image = renderLayout(settings, settings.pageSize().pixelSize(settings.orientation(), Math.min(settings.dpi(), 200)));

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("CATGIS - Composicion cartografica");
            PageFormat format = job.defaultPage();
            format.setOrientation(settings.orientation() == PageOrientation.LANDSCAPE ? PageFormat.LANDSCAPE : PageFormat.PORTRAIT);
            format = job.pageDialog(format);
            job.setPrintable(new ImagePrintable(image), format);
            if (!job.printDialog()) {
                return;
            }
            job.print();
            statusLabel.setText("Composicion enviada a impresion.");
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo imprimir la composicion:\n" + ex.getMessage(), "Composicion", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void announceExport(String prefix, File file) {
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(prefix + ": " + file.getName());
        }
        statusLabel.setText(prefix + ": " + file.getAbsolutePath());
        JOptionPane.showMessageDialog(this, prefix + " correctamente:\n" + file.getAbsolutePath());
    }

    private LayoutSettings buildSettings() {
        pushProjectMetadataFromControls();
        PageSizePreset pageSize = (PageSizePreset) pageSizeCombo.getSelectedItem();
        PageOrientation orientation = (PageOrientation) orientationCombo.getSelectedItem();
        Integer dpiValue = (Integer) dpiCombo.getSelectedItem();
        if (pageSize == null) {
            pageSize = PageSizePreset.A4;
        }
        if (orientation == null) {
            orientation = PageOrientation.LANDSCAPE;
        }
        int dpi = dpiValue != null ? dpiValue : 200;
        LegendPlacement legendPlacement = (LegendPlacement) legendPlacementCombo.getSelectedItem();
        ScaleStyle scaleStyle = (ScaleStyle) scaleStyleCombo.getSelectedItem();
        ScaleRule scaleRule = (ScaleRule) scaleRuleCombo.getSelectedItem();
        NorthStyle northStyle = (NorthStyle) northStyleCombo.getSelectedItem();
        LayoutTemplate layoutTemplate = (LayoutTemplate) templateCombo.getSelectedItem();
        return new LayoutSettings(
                safeTrim(titleField.getText()),
                safeTrim(subtitleField.getText()),
                safeTrim(footerField.getText()),
                safeTrim(studyField.getText()),
                safeTrim(companyField.getText()),
                safeTrim(cartographerField.getText()),
                safeTrim(imageSourceField.getText()),
                safeTrim(coordinateReferenceField.getText()),
                safeTrim(legendTitleField.getText()),
                safeTrim(legendSubtitleField.getText()),
                safeTrim(logoPathField.getText()),
                safeTrim(layoutImagePathField.getText()),
                layoutTemplate != null ? layoutTemplate : LayoutTemplate.TECHNICAL_RIGHT,
                pageSize,
                orientation,
                dpi,
                legendPlacement != null ? legendPlacement : LegendPlacement.RIGHT_PANEL,
                scaleStyle != null ? scaleStyle : ScaleStyle.SEGMENTED_BAR,
                scaleRule != null ? scaleRule : ScaleRule.PREFERRED_CARTOGRAPHY,
                northStyle != null ? northStyle : NorthStyle.CLASSIC,
                northCheck.isSelected(),
                scaleCheck.isSelected(),
                legendCheck.isSelected(),
                gridCheck.isSelected(),
                (Integer) gridColumnsSpinner.getValue(),
                (Integer) gridRowsSpinner.getValue(),
                gridLabelsCheck.isSelected()
        );
    }

    private LayoutSnapshot getSnapshot() {
        return snapshot;
    }

    private BufferedImage renderLayout(LayoutSettings settings, Dimension size) {
        return LayoutRenderer.render(settings, snapshot, size.width, size.height, interactionState);
    }

    private LayoutSnapshot captureSnapshot() {
        if (CatgisDesktopApp.mapPanel == null) {
            return new LayoutSnapshot(
                    new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB),
                    visibleLayers(),
                    currentProjectName(),
                    currentProjectCrs(),
                    "Escala no disponible",
                    0
            );
        }

        int mapWidth = Math.max(CatgisDesktopApp.mapPanel.getWidth(), 1200);
        int mapHeight = Math.max(CatgisDesktopApp.mapPanel.getHeight(), 800);
        BufferedImage mapImage = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = mapImage.createGraphics();
        try {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, mapWidth, mapHeight);
            CatgisDesktopApp.mapPanel.paint(g2);
        } finally {
            g2.dispose();
        }
        mapImage = trimOuterWhitespace(mapImage);

        double scaleMeters = estimateRepresentativeScaleMeters(mapWidth);
        return new LayoutSnapshot(
                mapImage,
                visibleLayers(),
                currentProjectName(),
                currentProjectCrs(),
                formatDistance(scaleMeters),
                scaleMeters
        );
    }

    private BufferedImage trimOuterWhitespace(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= 2 || height <= 2) {
            return image;
        }

        int left = 0;
        int right = width - 1;
        int top = 0;
        int bottom = height - 1;

        while (left < right && isNearWhiteColumn(image, left)) {
            left++;
        }
        while (right > left && isNearWhiteColumn(image, right)) {
            right--;
        }
        while (top < bottom && isNearWhiteRow(image, top)) {
            top++;
        }
        while (bottom > top && isNearWhiteRow(image, bottom)) {
            bottom--;
        }

        if (left <= 0 && right >= width - 1 && top <= 0 && bottom >= height - 1) {
            return image;
        }
        int croppedWidth = Math.max(1, right - left + 1);
        int croppedHeight = Math.max(1, bottom - top + 1);
        if (croppedWidth < width / 4 || croppedHeight < height / 4) {
            return image;
        }
        BufferedImage cropped = image.getSubimage(left, top, croppedWidth, croppedHeight);
        BufferedImage copy = new BufferedImage(croppedWidth, croppedHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D copyGraphics = copy.createGraphics();
        try {
            copyGraphics.drawImage(cropped, 0, 0, null);
        } finally {
            copyGraphics.dispose();
        }
        return copy;
    }

    private boolean isNearWhiteColumn(BufferedImage image, int x) {
        for (int y = 0; y < image.getHeight(); y += Math.max(1, image.getHeight() / 120)) {
            if (!isNearWhite(image.getRGB(x, y))) {
                return false;
            }
        }
        return true;
    }

    private boolean isNearWhiteRow(BufferedImage image, int y) {
        for (int x = 0; x < image.getWidth(); x += Math.max(1, image.getWidth() / 160)) {
            if (!isNearWhite(image.getRGB(x, y))) {
                return false;
            }
        }
        return true;
    }

    private boolean isNearWhite(int argb) {
        int alpha = (argb >> 24) & 0xFF;
        if (alpha < 10) {
            return true;
        }
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        return red >= 245 && green >= 245 && blue >= 245;
    }

    private List<Layer> visibleLayers() {
        List<Layer> visible = new ArrayList<>();
        if (CatgisDesktopApp.currentProject == null || CatgisDesktopApp.currentProject.getLayers() == null) {
            return visible;
        }
        for (Layer layer : CatgisDesktopApp.currentProject.getLayers()) {
            if (layer != null && layer.isVisible()) {
                visible.add(layer);
            }
        }
        return visible;
    }

    private String currentProjectName() {
        if (CatgisDesktopApp.currentProject == null || CatgisDesktopApp.currentProject.getName() == null || CatgisDesktopApp.currentProject.getName().isBlank()) {
            return "Proyecto actual";
        }
        return CatgisDesktopApp.currentProject.getName();
    }

    private String currentProjectCrs() {
        String code = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        return CRSDefinitions.getLabelForCode(code != null ? code : "");
    }

    private String defaultTitle() {
        return currentProjectName();
    }

    private String defaultSubtitle() {
        return "Vista cartografica generada desde la vista actual";
    }

    private String defaultFooter() {
        return "Generado en CATGIS | " + FOOTER_DATE.format(LocalDateTime.now());
    }

    private double estimateRepresentativeScaleMeters(int mapPixelWidth) {
        if (CatgisDesktopApp.mapPanel == null || mapPixelWidth <= 0) {
            return 0;
        }

        double zoomFactor = Math.max(CatgisDesktopApp.mapPanel.getZoomFactor(), 0.000001d);
        double projectWidth = Math.max(1d, mapPixelWidth / zoomFactor);
        String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        if (projectCrs == null) {
            projectCrs = "";
        }
        projectCrs = CRSDefinitions.normalizeCode(projectCrs);

        if (isGeographic(projectCrs)) {
            double centerLat = CatgisDesktopApp.mapPanel.getViewMinY()
                    + (Math.max(1, CatgisDesktopApp.mapPanel.getHeight()) / 2d) / zoomFactor;
            double metersPerDegreeLon = 111320d * Math.cos(Math.toRadians(centerLat));
            metersPerDegreeLon = Math.max(1d, Math.abs(metersPerDegreeLon));
            return projectWidth * metersPerDegreeLon;
        }
        return projectWidth;
    }

    private boolean isGeographic(String projectCrs) {
        return "EPSG:4326".equalsIgnoreCase(projectCrs)
                || "EPSG:4258".equalsIgnoreCase(projectCrs)
                || "EPSG:4269".equalsIgnoreCase(projectCrs)
                || "EPSG:4674".equalsIgnoreCase(projectCrs)
                || "EPSG:4190".equalsIgnoreCase(projectCrs)
                || "EPSG:4221".equalsIgnoreCase(projectCrs);
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String formatDistance(double meters) {
        if (meters <= 0) {
            return "Escala no disponible";
        }
        if (meters >= 1000d) {
            return new DecimalFormat("#,##0.## km").format(meters / 1000d);
        }
        return new DecimalFormat("#,##0 m").format(meters);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private enum LayoutTemplate {
        TECHNICAL_RIGHT("Tecnica - leyenda derecha", LegendPlacement.RIGHT_PANEL),
        BOTTOM_REFERENCE("Referencia inferior", LegendPlacement.BOTTOM_PANEL),
        CLEAN_CENTERED("Limpia centrada", LegendPlacement.MAP_BOTTOM_RIGHT),
        STRONG_CARTOUCHE("Cartucho tecnico", LegendPlacement.RIGHT_PANEL);

        private final String label;
        private final LegendPlacement defaultLegendPlacement;

        LayoutTemplate(String label, LegendPlacement defaultLegendPlacement) {
            this.label = label;
            this.defaultLegendPlacement = defaultLegendPlacement;
        }

        LegendPlacement defaultLegendPlacement() {
            return defaultLegendPlacement;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum PageSizePreset {
        A3("A3", 297d, 420d),
        A4("A4", 210d, 297d),
        A5("A5", 148d, 210d),
        LETTER("Carta", 216d, 279d),
        LEGAL("Legal", 216d, 356d),
        TABLOID("Tabloide", 279d, 432d);

        private final String label;
        private final double widthMm;
        private final double heightMm;

        PageSizePreset(String label, double widthMm, double heightMm) {
            this.label = label;
            this.widthMm = widthMm;
            this.heightMm = heightMm;
        }

        Dimension pixelSize(PageOrientation orientation, int dpi) {
            double widthInches = (orientation == PageOrientation.LANDSCAPE ? heightMm : widthMm) / 25.4d;
            double heightInches = (orientation == PageOrientation.LANDSCAPE ? widthMm : heightMm) / 25.4d;
            int width = Math.max(600, (int) Math.round(widthInches * dpi));
            int height = Math.max(600, (int) Math.round(heightInches * dpi));
            return new Dimension(width, height);
        }

        PDRectangle toPdfRectangle(PageOrientation orientation) {
            float widthPoints = (float) ((orientation == PageOrientation.LANDSCAPE ? heightMm : widthMm) / 25.4d * 72d);
            float heightPoints = (float) ((orientation == PageOrientation.LANDSCAPE ? widthMm : heightMm) / 25.4d * 72d);
            return new PDRectangle(widthPoints, heightPoints);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum PreviewScaleMode {
        FIT_PAGE,
        FIT_WIDTH,
        CUSTOM
    }

    private enum LayoutElementType {
        HEADER,
        MAP_CONTENT,
        LEGEND,
        NORTH,
        SCALE,
        CARTOUCHE,
        PROFILE_IMAGE
    }

    private enum ResizeHandle {
        NONE,
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NORTH_EAST,
        NORTH_WEST,
        SOUTH_EAST,
        SOUTH_WEST
    }

    private enum PageOrientation {
        PORTRAIT("Vertical"),
        LANDSCAPE("Apaisado");

        private final String label;

        PageOrientation(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum LegendPlacement {
        RIGHT_PANEL("Panel derecho"),
        BOTTOM_PANEL("Franja inferior"),
        MAP_TOP_RIGHT("Dentro del mapa - arriba derecha"),
        MAP_BOTTOM_RIGHT("Dentro del mapa - abajo derecha"),
        MAP_BOTTOM_LEFT("Dentro del mapa - abajo izquierda");

        private final String label;

        LegendPlacement(String label) {
            this.label = label;
        }

        boolean isInsideMap() {
            return this == MAP_TOP_RIGHT || this == MAP_BOTTOM_RIGHT || this == MAP_BOTTOM_LEFT;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum ScaleStyle {
        SEGMENTED_BAR("Barra segmentada"),
        SIMPLE_BAR("Barra simple"),
        NUMERIC("Escala numerica");

        private final String label;

        ScaleStyle(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum ScaleRule {
        PREFERRED_CARTOGRAPHY("Preferidas 500/1000/2000/5000/10000", new double[]{250d, 500d, 1000d, 2000d, 5000d, 10000d, 20000d, 50000d, 100000d}),
        ENGINEERING("Ingenieria 1-2-5", null),
        LARGE_AREA("Grandes areas 1000/2500/5000/10000", new double[]{500d, 1000d, 2500d, 5000d, 10000d, 25000d, 50000d, 100000d, 250000d});

        private final String label;
        private final double[] preferredValues;

        ScaleRule(String label, double[] preferredValues) {
            this.label = label;
            this.preferredValues = preferredValues;
        }

        double roundValue(double rawValue) {
            if (rawValue <= 0) {
                return 0;
            }
            if (preferredValues != null && preferredValues.length > 0) {
                double best = preferredValues[0];
                double bestDistance = Math.abs(preferredValues[0] - rawValue);
                for (double preferredValue : preferredValues) {
                    double distance = Math.abs(preferredValue - rawValue);
                    if (distance < bestDistance) {
                        best = preferredValue;
                        bestDistance = distance;
                    }
                }
                return best;
            }

            double exponent = Math.pow(10, Math.floor(Math.log10(rawValue)));
            double normalized = rawValue / exponent;
            double rounded;
            if (normalized < 1.5d) {
                rounded = 1d;
            } else if (normalized < 3.5d) {
                rounded = 2d;
            } else if (normalized < 7.5d) {
                rounded = 5d;
            } else {
                rounded = 10d;
            }
            return rounded * exponent;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum NorthStyle {
        SIMPLE("Simple"),
        CLASSIC("Clasico"),
        MODERN("Moderno");

        private final String label;

        NorthStyle(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private record LayoutSettings(String title,
                                  String subtitle,
                                  String footer,
                                  String studyName,
                                  String companyName,
                                  String cartographerName,
                                  String imageSource,
                                  String coordinateReference,
                                  String legendTitle,
                                  String legendSubtitle,
                                  String logoPath,
                                  String layoutImagePath,
                                  LayoutTemplate template,
                                  PageSizePreset pageSize,
                                  PageOrientation orientation,
                                  int dpi,
                                  LegendPlacement legendPlacement,
                                  ScaleStyle scaleStyle,
                                  ScaleRule scaleRule,
                                  NorthStyle northStyle,
                                  boolean showNorth,
                                  boolean showScale,
                                  boolean showLegend,
                                  boolean showGrid,
                                  int gridColumns,
                                  int gridRows,
                                  boolean showGridLabels) {
    }

    private record LayoutSnapshot(BufferedImage mapImage,
                                  List<Layer> visibleLayers,
                                  String projectName,
                                  String projectCrsLabel,
                                  String scaleLabel,
                                  double representativeMeters) {
    }

    private record MapFrameGeometry(Rectangle frameBounds, Rectangle imageBounds) {
    }

    private record LegendItem(String label, String subtitle, Layer layer, CategoryStyleRule categoryRule, String geometryType) {
    }

    private record LayoutRenderResult(BufferedImage image, EnumMap<LayoutElementType, Rectangle> elementBounds) {
    }

    private record FooterRenderResult(Rectangle cartoucheBounds, Rectangle profileImageBounds) {
    }

    private static class LayoutInteractionState {
        private LayoutTemplate template = LayoutTemplate.TECHNICAL_RIGHT;
        private PreviewScaleMode previewScaleMode = PreviewScaleMode.FIT_PAGE;
        private double customPreviewZoom = 1d;
        private double mapZoom = 1d;
        private double mapOffsetX = 0d;
        private double mapOffsetY = 0d;
        private LayoutElementType selectedElement = null;
        private final EnumMap<LayoutElementType, Point> elementOffsets = new EnumMap<>(LayoutElementType.class);
        private final EnumMap<LayoutElementType, Dimension> elementSizeAdjustments = new EnumMap<>(LayoutElementType.class);

        LayoutTemplate getTemplate() {
            return template;
        }

        void setTemplate(LayoutTemplate template) {
            this.template = template != null ? template : LayoutTemplate.TECHNICAL_RIGHT;
        }

        void resetForTemplate(LayoutTemplate template) {
            setTemplate(template);
            previewScaleMode = PreviewScaleMode.FIT_PAGE;
            customPreviewZoom = 1d;
            resetMapView();
            elementOffsets.clear();
            elementSizeAdjustments.clear();
            selectedElement = null;
        }

        void fitPage() {
            previewScaleMode = PreviewScaleMode.FIT_PAGE;
            customPreviewZoom = 1d;
        }

        void fitWidth() {
            previewScaleMode = PreviewScaleMode.FIT_WIDTH;
            customPreviewZoom = 1d;
        }

        void zoomPreview(double factor) {
            previewScaleMode = PreviewScaleMode.CUSTOM;
            customPreviewZoom = Math.max(0.35d, Math.min(6d, customPreviewZoom * factor));
        }

        double resolvePreviewScale(double fitPageScale, double fitWidthScale) {
            return switch (previewScaleMode) {
                case FIT_WIDTH -> Math.max(0.08d, fitWidthScale);
                case CUSTOM -> Math.max(0.08d, fitPageScale * customPreviewZoom);
                default -> Math.max(0.08d, fitPageScale);
            };
        }

        void select(LayoutElementType elementType) {
            selectedElement = elementType;
        }

        LayoutElementType getSelectedElement() {
            return selectedElement;
        }

        Point getOffset(LayoutElementType elementType) {
            Point point = elementOffsets.get(elementType);
            return point != null ? point : new Point();
        }

        Dimension getSizeAdjustment(LayoutElementType elementType) {
            Dimension dimension = elementSizeAdjustments.get(elementType);
            return dimension != null ? dimension : new Dimension();
        }

        void translate(LayoutElementType elementType, int dx, int dy) {
            if (elementType == null) {
                return;
            }
            Point offset = elementOffsets.computeIfAbsent(elementType, key -> new Point());
            offset.translate(dx, dy);
        }

        void resize(LayoutElementType elementType,
                    ResizeHandle handle,
                    int dx,
                    int dy,
                    int currentWidth,
                    int currentHeight,
                    int minWidth,
                    int minHeight) {
            if (elementType == null || handle == null || handle == ResizeHandle.NONE) {
                return;
            }
            Point offset = elementOffsets.computeIfAbsent(elementType, key -> new Point());
            Dimension size = elementSizeAdjustments.computeIfAbsent(elementType, key -> new Dimension());
            int actualWidth = currentWidth;
            int actualHeight = currentHeight;
            int baseWidth = actualWidth - size.width;
            int baseHeight = actualHeight - size.height;

            switch (handle) {
                case EAST -> {
                    int targetWidth = Math.max(minWidth, actualWidth + dx);
                    size.width = targetWidth - baseWidth;
                }
                case SOUTH -> {
                    int targetHeight = Math.max(minHeight, actualHeight + dy);
                    size.height = targetHeight - baseHeight;
                }
                case SOUTH_EAST -> {
                    int targetWidth = Math.max(minWidth, actualWidth + dx);
                    int targetHeight = Math.max(minHeight, actualHeight + dy);
                    size.width = targetWidth - baseWidth;
                    size.height = targetHeight - baseHeight;
                }
                case WEST -> {
                    int targetWidth = Math.max(minWidth, actualWidth - dx);
                    int shift = actualWidth - targetWidth;
                    offset.translate(shift, 0);
                    size.width = targetWidth - baseWidth;
                }
                case NORTH -> {
                    int targetHeight = Math.max(minHeight, actualHeight - dy);
                    int shift = actualHeight - targetHeight;
                    offset.translate(0, shift);
                    size.height = targetHeight - baseHeight;
                }
                case NORTH_WEST -> {
                    int targetWidth = Math.max(minWidth, actualWidth - dx);
                    int targetHeight = Math.max(minHeight, actualHeight - dy);
                    offset.translate(actualWidth - targetWidth, actualHeight - targetHeight);
                    size.width = targetWidth - baseWidth;
                    size.height = targetHeight - baseHeight;
                }
                case NORTH_EAST -> {
                    int targetWidth = Math.max(minWidth, actualWidth + dx);
                    int targetHeight = Math.max(minHeight, actualHeight - dy);
                    offset.translate(0, actualHeight - targetHeight);
                    size.width = targetWidth - baseWidth;
                    size.height = targetHeight - baseHeight;
                }
                case SOUTH_WEST -> {
                    int targetWidth = Math.max(minWidth, actualWidth - dx);
                    int targetHeight = Math.max(minHeight, actualHeight + dy);
                    offset.translate(actualWidth - targetWidth, 0);
                    size.width = targetWidth - baseWidth;
                    size.height = targetHeight - baseHeight;
                }
                default -> {
                }
            }
        }

        boolean hasCustomSize(LayoutElementType elementType) {
            Dimension size = elementSizeAdjustments.get(elementType);
            return size != null && (size.width != 0 || size.height != 0);
        }

        void zoomMap(double factor) {
            mapZoom = Math.max(0.6d, Math.min(5d, mapZoom * factor));
        }

        void panMap(double dx, double dy) {
            mapOffsetX += dx;
            mapOffsetY += dy;
        }

        void resetMapView() {
            mapZoom = 1d;
            mapOffsetX = 0d;
            mapOffsetY = 0d;
        }

        double getMapZoom() {
            return mapZoom;
        }

        double getMapOffsetX() {
            return mapOffsetX;
        }

        double getMapOffsetY() {
            return mapOffsetY;
        }
    }

    private class LayoutPreviewPanel extends JPanel {
        private LayoutRenderResult lastRenderResult;
        private Rectangle lastPageBounds = new Rectangle();
        private double lastPreviewScale = 1d;
        private Point lastDragPagePoint = null;
        private ResizeHandle activeResizeHandle = ResizeHandle.NONE;
        private LayoutElementType activeResizeElement = null;
        private final JTextField inlineTitleEditor;

        private LayoutPreviewPanel() {
            setLayout(null);
            setOpaque(true);
            setBackground(new Color(240, 243, 247));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 224, 230)),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));
            inlineTitleEditor = new JTextField();
            inlineTitleEditor.setVisible(false);
            inlineTitleEditor.addActionListener(e -> commitInlineTitleEdit());
            inlineTitleEditor.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    commitInlineTitleEdit();
                }
            });
            add(inlineTitleEditor);
            installInteraction();
        }

        private void installInteraction() {
            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        Point pagePoint = toPagePoint(e.getPoint());
                        if (pagePoint != null && isInsideElement(LayoutElementType.HEADER, pagePoint)) {
                            beginInlineTitleEdit();
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    commitInlineTitleEdit();
                    Point pagePoint = toPagePoint(e.getPoint());
                    if (pagePoint == null || lastRenderResult == null) {
                        interactionState.select(null);
                        activeResizeHandle = ResizeHandle.NONE;
                        activeResizeElement = null;
                        repaint();
                        return;
                    }
                    ResizeTarget resizeTarget = findResizeTarget(pagePoint);
                    if (resizeTarget != null) {
                        interactionState.select(resizeTarget.elementType());
                        activeResizeElement = resizeTarget.elementType();
                        activeResizeHandle = resizeTarget.handle();
                        lastDragPagePoint = pagePoint;
                        setCursor(cursorForHandle(activeResizeHandle));
                        statusLabel.setText("Redimensionando " + elementLabel(activeResizeElement) + " con el mouse.");
                        repaint();
                        return;
                    }
                    LayoutElementType hit = findElementAt(pagePoint);
                    interactionState.select(hit);
                    activeResizeHandle = ResizeHandle.NONE;
                    activeResizeElement = null;
                    lastDragPagePoint = hit != null ? pagePoint : null;
                    setCursor(hit != null ? Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) : Cursor.getDefaultCursor());
                    statusLabel.setText(hit != null
                            ? "Elemento seleccionado: " + elementLabel(hit) + ". Arrastra con el mouse para reubicar."
                            : "Haz clic sobre un elemento del layout para moverlo.");
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    lastDragPagePoint = null;
                    activeResizeHandle = ResizeHandle.NONE;
                    activeResizeElement = null;
                    setCursor(Cursor.getDefaultCursor());
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (lastDragPagePoint == null || interactionState.getSelectedElement() == null) {
                        return;
                    }
                    Point pagePoint = toPagePoint(e.getPoint());
                    if (pagePoint == null) {
                        return;
                    }
                    int dx = pagePoint.x - lastDragPagePoint.x;
                    int dy = pagePoint.y - lastDragPagePoint.y;
                    if (dx == 0 && dy == 0) {
                        return;
                    }
                    if (activeResizeElement != null && activeResizeHandle != ResizeHandle.NONE) {
                        Rectangle currentBounds = lastRenderResult != null ? lastRenderResult.elementBounds().get(activeResizeElement) : null;
                        if (currentBounds != null) {
                            interactionState.resize(
                                    activeResizeElement,
                                    activeResizeHandle,
                                    dx,
                                    dy,
                                    currentBounds.width,
                                    currentBounds.height,
                                    activeResizeElement == LayoutElementType.MAP_CONTENT ? 260 : 160,
                                    activeResizeElement == LayoutElementType.MAP_CONTENT ? 180 : 100
                            );
                        }
                    } else if (interactionState.getSelectedElement() == LayoutElementType.MAP_CONTENT) {
                        interactionState.panMap(dx, dy);
                    } else {
                        interactionState.translate(interactionState.getSelectedElement(), dx, dy);
                    }
                    lastDragPagePoint = pagePoint;
                    repaint();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    Point pagePoint = toPagePoint(e.getPoint());
                    ResizeTarget resizeTarget = pagePoint != null ? findResizeTarget(pagePoint) : null;
                    if (resizeTarget != null) {
                        setCursor(cursorForHandle(resizeTarget.handle()));
                        return;
                    }
                    if (pagePoint != null && findElementAt(pagePoint) != null) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (lastRenderResult == null) {
                        return;
                    }
                    Point pagePoint = toPagePoint(e.getPoint());
                    boolean overMap = pagePoint != null && isInsideElement(LayoutElementType.MAP_CONTENT, pagePoint);
                    double factor = e.getWheelRotation() < 0 ? 1.12d : (1d / 1.12d);
                    if (overMap) {
                        interactionState.zoomMap(factor);
                        statusLabel.setText("Zoom del mapa dentro del layout: " + Math.round(interactionState.getMapZoom() * 100d) + "%");
                    } else {
                        interactionState.zoomPreview(factor);
                        statusLabel.setText("Zoom del compositor actualizado para trabajar la maquetacion.");
                    }
                    repaint();
                }
            };
            addMouseListener(adapter);
            addMouseMotionListener(adapter);
            addMouseWheelListener(adapter);
        }

        private void beginInlineTitleEdit() {
            if (lastRenderResult == null) {
                return;
            }
            Rectangle headerBounds = lastRenderResult.elementBounds().get(LayoutElementType.HEADER);
            if (headerBounds == null) {
                return;
            }
            int editorX = lastPageBounds.x + (int) Math.round((headerBounds.x + 2) * lastPreviewScale);
            int editorY = lastPageBounds.y + (int) Math.round((headerBounds.y + 6) * lastPreviewScale);
            int editorWidth = Math.max(180, (int) Math.round(Math.min(headerBounds.width * 0.72d, 460) * lastPreviewScale));
            int editorHeight = Math.max(28, (int) Math.round(34 * lastPreviewScale));
            inlineTitleEditor.setText(titleField.getText());
            inlineTitleEditor.setBounds(editorX, editorY, editorWidth, editorHeight);
            inlineTitleEditor.setVisible(true);
            inlineTitleEditor.requestFocusInWindow();
            inlineTitleEditor.selectAll();
        }

        private void commitInlineTitleEdit() {
            if (!inlineTitleEditor.isVisible()) {
                return;
            }
            String updated = inlineTitleEditor.getText() != null ? inlineTitleEditor.getText().trim() : "";
            if (!updated.isBlank()) {
                titleField.setText(updated);
            }
            inlineTitleEditor.setVisible(false);
            repaint();
        }

        private String elementLabel(LayoutElementType type) {
            return switch (type) {
                case HEADER -> "encabezado";
                case MAP_CONTENT -> "mapa";
                case LEGEND -> "leyenda";
                case NORTH -> "norte";
                case SCALE -> "escala";
                case CARTOUCHE -> "cartucho";
                case PROFILE_IMAGE -> I18n.t("imagen del perfil");
            };
        }

        private LayoutElementType findElementAt(Point pagePoint) {
            LayoutElementType[] order = {
                    LayoutElementType.PROFILE_IMAGE,
                    LayoutElementType.LEGEND,
                    LayoutElementType.NORTH,
                    LayoutElementType.SCALE,
                    LayoutElementType.CARTOUCHE,
                    LayoutElementType.HEADER,
                    LayoutElementType.MAP_CONTENT
            };
            for (LayoutElementType type : order) {
                if (isInsideElement(type, pagePoint)) {
                    return type;
                }
            }
            return null;
        }

        private ResizeTarget findResizeTarget(Point pagePoint) {
            for (LayoutElementType type : new LayoutElementType[]{LayoutElementType.PROFILE_IMAGE, LayoutElementType.LEGEND, LayoutElementType.CARTOUCHE, LayoutElementType.MAP_CONTENT}) {
                Rectangle bounds = lastRenderResult != null ? lastRenderResult.elementBounds().get(type) : null;
                ResizeHandle handle = bounds != null ? resolveResizeHandle(bounds, pagePoint) : ResizeHandle.NONE;
                if (handle != ResizeHandle.NONE) {
                    return new ResizeTarget(type, handle);
                }
            }
            return null;
        }

        private ResizeHandle resolveResizeHandle(Rectangle bounds, Point point) {
            int tolerance = Math.max(6, (int) Math.round(8d / Math.max(0.4d, lastPreviewScale)));
            boolean left = Math.abs(point.x - bounds.x) <= tolerance;
            boolean right = Math.abs(point.x - (bounds.x + bounds.width)) <= tolerance;
            boolean top = Math.abs(point.y - bounds.y) <= tolerance;
            boolean bottom = Math.abs(point.y - (bounds.y + bounds.height)) <= tolerance;
            boolean insideY = point.y >= bounds.y - tolerance && point.y <= bounds.y + bounds.height + tolerance;
            boolean insideX = point.x >= bounds.x - tolerance && point.x <= bounds.x + bounds.width + tolerance;

            if (left && top) return ResizeHandle.NORTH_WEST;
            if (right && top) return ResizeHandle.NORTH_EAST;
            if (left && bottom) return ResizeHandle.SOUTH_WEST;
            if (right && bottom) return ResizeHandle.SOUTH_EAST;
            if (left && insideY) return ResizeHandle.WEST;
            if (right && insideY) return ResizeHandle.EAST;
            if (top && insideX) return ResizeHandle.NORTH;
            if (bottom && insideX) return ResizeHandle.SOUTH;
            return ResizeHandle.NONE;
        }

        private Cursor cursorForHandle(ResizeHandle handle) {
            return switch (handle) {
                case NORTH -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                case SOUTH -> Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                case EAST -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                case WEST -> Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                case NORTH_EAST -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                case NORTH_WEST -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                case SOUTH_EAST -> Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                case SOUTH_WEST -> Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                default -> Cursor.getDefaultCursor();
            };
        }

        private boolean isInsideElement(LayoutElementType type, Point pagePoint) {
            Rectangle bounds = lastRenderResult != null ? lastRenderResult.elementBounds().get(type) : null;
            return bounds != null && bounds.contains(pagePoint);
        }

        private Point toPagePoint(Point panelPoint) {
            if (lastRenderResult == null || lastPageBounds.width <= 0 || lastPageBounds.height <= 0 || lastPreviewScale <= 0) {
                return null;
            }
            if (!lastPageBounds.contains(panelPoint)) {
                return null;
            }
            int pageX = (int) Math.round((panelPoint.x - lastPageBounds.x) / lastPreviewScale);
            int pageY = (int) Math.round((panelPoint.y - lastPageBounds.y) / lastPreviewScale);
            return new Point(pageX, pageY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            LayoutSettings settings = buildSettings();
            LayoutSnapshot currentSnapshot = getSnapshot();
            if (settings == null || currentSnapshot == null) {
                return;
            }

            Dimension previewSize = settings.pageSize().pixelSize(settings.orientation(), 110);
            lastRenderResult = LayoutRenderer.renderResult(settings, currentSnapshot, previewSize.width, previewSize.height, interactionState);
            BufferedImage page = lastRenderResult.image();

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int availableWidth = Math.max(80, getWidth() - 40);
                int availableHeight = Math.max(80, getHeight() - 40);
                double fitPageScale = Math.min(availableWidth / (double) page.getWidth(), availableHeight / (double) page.getHeight());
                double fitWidthScale = availableWidth / (double) page.getWidth();
                double scale = interactionState.resolvePreviewScale(fitPageScale, fitWidthScale);
                scale = Math.max(0.08d, scale);
                int drawWidth = (int) Math.round(page.getWidth() * scale);
                int drawHeight = (int) Math.round(page.getHeight() * scale);
                int x = (getWidth() - drawWidth) / 2;
                int y = Math.max(10, (getHeight() - drawHeight) / 2);

                lastPageBounds = new Rectangle(x, y, drawWidth, drawHeight);
                lastPreviewScale = scale;

                g2.setColor(new Color(0, 0, 0, 26));
                g2.fillRoundRect(x + 10, y + 10, drawWidth, drawHeight, 18, 18);
                g2.drawImage(page, x, y, drawWidth, drawHeight, null);
                drawSelectionOverlay(g2, x, y, scale);
                if (inlineTitleEditor.isVisible()) {
                    Rectangle headerBounds = lastRenderResult.elementBounds().get(LayoutElementType.HEADER);
                    if (headerBounds != null) {
                        inlineTitleEditor.setBounds(
                                lastPageBounds.x + (int) Math.round((headerBounds.x + 2) * lastPreviewScale),
                                lastPageBounds.y + (int) Math.round((headerBounds.y + 6) * lastPreviewScale),
                                Math.max(180, (int) Math.round(Math.min(headerBounds.width * 0.72d, 460) * lastPreviewScale)),
                                Math.max(28, (int) Math.round(34 * lastPreviewScale))
                        );
                    }
                }

                g2.setColor(new Color(57, 67, 82));
                g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                String label = settings.pageSize() + " - " + settings.orientation() + " | " + settings.template();
                g2.drawString(label, x + 12, Math.max(18, y - 10));
            } finally {
                g2.dispose();
            }
        }

        private void drawSelectionOverlay(Graphics2D g2, int pageX, int pageY, double scale) {
            LayoutElementType selected = interactionState.getSelectedElement();
            if (selected == null || lastRenderResult == null) {
                return;
            }
            Rectangle bounds = lastRenderResult.elementBounds().get(selected);
            if (bounds == null) {
                return;
            }
            int x = pageX + (int) Math.round(bounds.x * scale);
            int y = pageY + (int) Math.round(bounds.y * scale);
            int w = Math.max(18, (int) Math.round(bounds.width * scale));
            int h = Math.max(18, (int) Math.round(bounds.height * scale));
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(new Color(37, 99, 235, 26));
                copy.fillRoundRect(x, y, w, h, 12, 12);
                copy.setColor(new Color(37, 99, 235));
                copy.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{7f, 5f}, 0f));
                copy.drawRoundRect(x, y, w, h, 12, 12);
                if (selected == LayoutElementType.MAP_CONTENT || selected == LayoutElementType.LEGEND || selected == LayoutElementType.CARTOUCHE || selected == LayoutElementType.PROFILE_IMAGE) {
                    drawResizeHandle(copy, x, y);
                    drawResizeHandle(copy, x + w / 2, y);
                    drawResizeHandle(copy, x + w, y);
                    drawResizeHandle(copy, x, y + h / 2);
                    drawResizeHandle(copy, x + w, y + h / 2);
                    drawResizeHandle(copy, x, y + h);
                    drawResizeHandle(copy, x + w / 2, y + h);
                    drawResizeHandle(copy, x + w, y + h);
                }
            } finally {
                copy.dispose();
            }
        }

        private void drawResizeHandle(Graphics2D g2, int centerX, int centerY) {
            int size = 8;
            g2.setColor(Color.WHITE);
            g2.fillRect(centerX - size / 2, centerY - size / 2, size, size);
            g2.setColor(new Color(37, 99, 235));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRect(centerX - size / 2, centerY - size / 2, size, size);
        }

        private record ResizeTarget(LayoutElementType elementType, ResizeHandle handle) {
        }
    }

    private static class LayoutRenderer {

        private LayoutRenderer() {
        }

        private static BufferedImage render(LayoutSettings settings, LayoutSnapshot snapshot, int width, int height, LayoutInteractionState interactionState) {
            return renderResult(settings, snapshot, width, height, interactionState).image();
        }

        private static LayoutRenderResult renderResult(LayoutSettings settings, LayoutSnapshot snapshot, int width, int height, LayoutInteractionState interactionState) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            EnumMap<LayoutElementType, Rectangle> elementBounds = new EnumMap<>(LayoutElementType.class);
            BufferedImage layoutImage = loadImageAsset(settings.layoutImagePath());
            Graphics2D g2 = image.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, width, height);

                int margin = Math.max(40, width / 36);
                int headerHeight = Math.max(112, height / 8);
                int footerHeight = Math.max(150, height / 6);
                if (settings.template() == LayoutTemplate.CLEAN_CENTERED) {
                    headerHeight = Math.max(96, height / 10);
                    footerHeight = Math.max(128, height / 7);
                } else if (settings.template() == LayoutTemplate.STRONG_CARTOUCHE) {
                    footerHeight = Math.max(180, height / 5);
                } else if (settings.template() == LayoutTemplate.BOTTOM_REFERENCE) {
                    headerHeight = Math.max(108, height / 9);
                }
                if (layoutImage != null) {
                    footerHeight = Math.max(220, footerHeight + 70);
                }
                boolean legendOutsideRight = settings.showLegend() && settings.legendPlacement() == LegendPlacement.RIGHT_PANEL;
                boolean legendBottom = settings.showLegend() && settings.legendPlacement() == LegendPlacement.BOTTOM_PANEL;
                int legendWidth = legendOutsideRight ? Math.max(260, width / 5) : 0;
                int legendHeight = legendBottom ? Math.max(140, height / 5) : 0;
                int gap = settings.showLegend() ? Math.max(18, width / 60) : 0;
                int mapX = margin;
                int mapY = margin + headerHeight;
                int mapW = Math.max(200, width - (margin * 2) - legendWidth - gap);
                int mapH = Math.max(220, height - mapY - footerHeight - margin - (legendBottom ? legendHeight + gap : 0));

                Rectangle headerBounds = applyElementAdjustment(new Rectangle(margin, margin, width - (margin * 2), headerHeight - 14), interactionState, LayoutElementType.HEADER);
                drawHeader(g2, settings, snapshot, headerBounds);
                elementBounds.put(LayoutElementType.HEADER, new Rectangle(headerBounds));

                Rectangle requestedMapBounds = applyElementAdjustment(new Rectangle(mapX, mapY, mapW, mapH), interactionState, LayoutElementType.MAP_CONTENT);
                if (!interactionState.hasCustomSize(LayoutElementType.MAP_CONTENT)) {
                    requestedMapBounds = optimizeMapFrame(requestedMapBounds, snapshot.mapImage(), settings.template());
                }
                MapFrameGeometry mapFrame = drawMapFrame(g2, snapshot.mapImage(), requestedMapBounds, interactionState);
                elementBounds.put(LayoutElementType.MAP_CONTENT, new Rectangle(mapFrame.frameBounds()));
                if (settings.showGrid()) {
                    drawGrid(g2, settings, mapFrame);
                }

                if (settings.showNorth()) {
                    Rectangle northBounds = applyElementAdjustment(new Rectangle(
                            mapFrame.frameBounds().x + mapFrame.frameBounds().width - 92,
                            mapFrame.frameBounds().y + 20,
                            Math.max(54, width / 22),
                            Math.max(54, width / 22)
                    ), interactionState, LayoutElementType.NORTH);
                    drawNorthArrow(g2, settings.northStyle(), northBounds.x, northBounds.y, northBounds.width);
                    elementBounds.put(LayoutElementType.NORTH, northBounds);
                }
                if (settings.showScale()) {
                    Rectangle scaleBounds = applyElementAdjustment(new Rectangle(
                            mapFrame.frameBounds().x + 8,
                            mapFrame.frameBounds().y + mapFrame.frameBounds().height - 74,
                            Math.min(280, mapFrame.frameBounds().width / 2),
                            54
                    ), interactionState, LayoutElementType.SCALE);
                    drawScaleBar(g2, settings, snapshot, mapFrame, scaleBounds.x + 14, scaleBounds.y + 18);
                    elementBounds.put(LayoutElementType.SCALE, scaleBounds);
                }
                if (settings.showLegend()) {
                    Rectangle legendBounds = resolveLegendBounds(settings, width, margin, gap, legendWidth, legendHeight, mapFrame, mapFrame.frameBounds().y + mapFrame.frameBounds().height);
                    legendBounds = applyElementAdjustment(legendBounds, interactionState, LayoutElementType.LEGEND);
                    drawLegend(g2, settings, snapshot.visibleLayers(), legendBounds.x, legendBounds.y, legendBounds.width, legendBounds.height);
                    elementBounds.put(LayoutElementType.LEGEND, legendBounds);
                }

                FooterRenderResult footerResult = drawFooter(g2, settings, snapshot, width, height, margin, footerHeight, mapFrame, interactionState, layoutImage);
                elementBounds.put(LayoutElementType.CARTOUCHE, footerResult.cartoucheBounds());
                if (footerResult.profileImageBounds() != null) {
                    elementBounds.put(LayoutElementType.PROFILE_IMAGE, footerResult.profileImageBounds());
                }
            } finally {
                g2.dispose();
            }
            return new LayoutRenderResult(image, elementBounds);
        }

        private static void exportPdf(LayoutSettings settings, LayoutSnapshot snapshot, File file, LayoutInteractionState interactionState) throws Exception {
            try (PDDocument document = new PDDocument()) {
                PDRectangle rectangle = settings.pageSize().toPdfRectangle(settings.orientation());
                PDPage page = new PDPage(rectangle);
                document.addPage(page);

                Dimension size = settings.pageSize().pixelSize(settings.orientation(), settings.dpi());
                BufferedImage layout = render(settings, snapshot, size.width, size.height, interactionState);
                PDImageXObject pdfImage = LosslessFactory.createFromImage(document, layout);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.drawImage(pdfImage, 0, 0, rectangle.getWidth(), rectangle.getHeight());
                }
                document.save(file);
            }
        }

        private static Rectangle resolveLegendBounds(LayoutSettings settings, int width, int margin, int gap, int legendWidth, int legendHeight, MapFrameGeometry mapFrame, int bottomY) {
            return switch (settings.legendPlacement()) {
                case BOTTOM_PANEL -> new Rectangle(margin, bottomY + gap, width - (margin * 2), legendHeight);
                case MAP_TOP_RIGHT -> new Rectangle(
                        mapFrame.frameBounds().x + mapFrame.frameBounds().width - Math.max(220, mapFrame.frameBounds().width / 3),
                        mapFrame.frameBounds().y + 18,
                        Math.max(220, mapFrame.frameBounds().width / 3),
                        Math.max(150, mapFrame.frameBounds().height / 3)
                );
                case MAP_BOTTOM_RIGHT -> new Rectangle(
                        mapFrame.frameBounds().x + mapFrame.frameBounds().width - Math.max(220, mapFrame.frameBounds().width / 3),
                        mapFrame.frameBounds().y + mapFrame.frameBounds().height - Math.max(150, mapFrame.frameBounds().height / 3) - 18,
                        Math.max(220, mapFrame.frameBounds().width / 3),
                        Math.max(150, mapFrame.frameBounds().height / 3)
                );
                case MAP_BOTTOM_LEFT -> new Rectangle(
                        mapFrame.frameBounds().x + 18,
                        mapFrame.frameBounds().y + mapFrame.frameBounds().height - Math.max(150, mapFrame.frameBounds().height / 3) - 18,
                        Math.max(220, mapFrame.frameBounds().width / 3),
                        Math.max(150, mapFrame.frameBounds().height / 3)
                );
                default -> new Rectangle(
                        mapFrame.frameBounds().x + mapFrame.frameBounds().width + gap,
                        mapFrame.frameBounds().y,
                        legendWidth,
                        mapFrame.frameBounds().height
                );
            };
        }

        private static Rectangle applyOffset(Rectangle source, Point offset) {
            Rectangle result = new Rectangle(source);
            if (offset != null) {
                result.translate(offset.x, offset.y);
            }
            return result;
        }

        private static Rectangle applyElementAdjustment(Rectangle source, LayoutInteractionState interactionState, LayoutElementType type) {
            Rectangle result = new Rectangle(source);
            if (interactionState == null || type == null) {
                return result;
            }
            Point offset = interactionState.getOffset(type);
            Dimension sizeAdjustment = interactionState.getSizeAdjustment(type);
            result.translate(offset.x, offset.y);
            result.width = Math.max(80, result.width + sizeAdjustment.width);
            result.height = Math.max(60, result.height + sizeAdjustment.height);
            return result;
        }

        private static Rectangle optimizeMapFrame(Rectangle availableBounds, BufferedImage mapImage, LayoutTemplate template) {
            if (mapImage == null || availableBounds.width <= 0 || availableBounds.height <= 0) {
                return new Rectangle(availableBounds);
            }
            double mapAspect = mapImage.getWidth() / (double) Math.max(1, mapImage.getHeight());
            double availableAspect = availableBounds.getWidth() / Math.max(1d, availableBounds.getHeight());
            Rectangle adjusted = new Rectangle(availableBounds);

            if (Math.abs(mapAspect - availableAspect) < 0.02d) {
                return adjusted;
            }

            if (mapAspect > availableAspect) {
                int targetHeight = Math.max(180, (int) Math.round(adjusted.width / mapAspect));
                if (targetHeight < adjusted.height) {
                    int anchorY = template == LayoutTemplate.CLEAN_CENTERED
                            ? adjusted.y + Math.max(0, (adjusted.height - targetHeight) / 2)
                            : adjusted.y;
                    adjusted = new Rectangle(adjusted.x, anchorY, adjusted.width, targetHeight);
                }
            } else {
                int targetWidth = Math.max(220, (int) Math.round(adjusted.height * mapAspect));
                if (targetWidth < adjusted.width) {
                    adjusted = new Rectangle(
                            adjusted.x + Math.max(0, (adjusted.width - targetWidth) / 2),
                            adjusted.y,
                            targetWidth,
                            adjusted.height
                    );
                }
            }
            return adjusted;
        }

        private static void drawHeader(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, Rectangle bounds) {
            int titleY = bounds.y + Math.max(30, bounds.height / 4);
            g2.setColor(new Color(27, 38, 56));
            g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(28, bounds.width / 34)));
            String title = !settings.title().isBlank() ? settings.title() : snapshot.projectName();
            g2.drawString(title, bounds.x, titleY);

            g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(15, bounds.width / 90)));
            g2.setColor(new Color(91, 103, 120));
            String subtitle = !settings.subtitle().isBlank() ? settings.subtitle() : "Salida cartografica del proyecto actual";
            g2.drawString(subtitle, bounds.x, titleY + 30);

            g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(13, bounds.width / 100)));
            g2.setColor(new Color(105, 114, 126));
            String meta = snapshot.projectName() + " | " + snapshot.projectCrsLabel();
            g2.drawString(meta, bounds.x, titleY + 56);

            int chipX = bounds.x;
            int chipY = titleY + 70;
            drawChip(g2, chipX, chipY, "Capas visibles: " + snapshot.visibleLayers().size());
            drawChip(g2, chipX + 164, chipY, settings.pageSize() + " - " + settings.orientation());
            drawChip(g2, chipX + 328, chipY, settings.dpi() + " dpi");
        }

        private static void drawChip(Graphics2D g2, int x, int y, String text) {
            Font chipFont = new Font("SansSerif", Font.BOLD, 11);
            g2.setFont(chipFont);
            java.awt.FontMetrics metrics = g2.getFontMetrics();
            int width = metrics.stringWidth(text) + 18;
            g2.setColor(new Color(239, 244, 251));
            g2.fillRoundRect(x, y, width, 24, 12, 12);
            g2.setColor(new Color(197, 210, 227));
            g2.drawRoundRect(x, y, width, 24, 12, 12);
            g2.setColor(new Color(58, 71, 90));
            g2.drawString(text, x + 9, y + 16);
        }

        private static MapFrameGeometry drawMapFrame(Graphics2D g2, BufferedImage mapImage, Rectangle requestedBounds, LayoutInteractionState interactionState) {
            int x = requestedBounds.x;
            int y = requestedBounds.y;
            int w = requestedBounds.width;
            int h = requestedBounds.height;
            g2.setColor(new Color(249, 250, 252));
            g2.fillRoundRect(x, y, w, h, 18, 18);
            g2.setColor(new Color(195, 204, 216));
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(x, y, w, h, 18, 18);

            int innerPadding = Math.max(16, Math.min(w, h) / 40);
            int contentX = x + innerPadding;
            int contentY = y + innerPadding;
            int contentW = w - (innerPadding * 2);
            int contentH = h - (innerPadding * 2);

            g2.setColor(new Color(255, 255, 255));
            g2.fillRect(contentX, contentY, contentW, contentH);

            double scale = Math.min(contentW / (double) Math.max(1, mapImage.getWidth()), contentH / (double) Math.max(1, mapImage.getHeight()));
            scale *= interactionState != null ? interactionState.getMapZoom() : 1d;
            int drawW = (int) Math.round(mapImage.getWidth() * scale);
            int drawH = (int) Math.round(mapImage.getHeight() * scale);
            int drawX = contentX + (contentW - drawW) / 2 + (int) Math.round(interactionState != null ? interactionState.getMapOffsetX() : 0d);
            int drawY = contentY + (contentH - drawH) / 2 + (int) Math.round(interactionState != null ? interactionState.getMapOffsetY() : 0d);
            Graphics2D imageGraphics = (Graphics2D) g2.create();
            try {
                imageGraphics.setClip(contentX, contentY, contentW, contentH);
                imageGraphics.drawImage(mapImage, drawX, drawY, drawW, drawH, null);
            } finally {
                imageGraphics.dispose();
            }

            g2.setColor(new Color(157, 169, 184));
            g2.drawRect(contentX, contentY, contentW, contentH);

            g2.setColor(new Color(103, 112, 124, 88));
            g2.drawLine(contentX + contentW / 2, contentY, contentX + contentW / 2, contentY + contentH);
            g2.drawLine(contentX, contentY + contentH / 2, contentX + contentW, contentY + contentH / 2);

            return new MapFrameGeometry(new Rectangle(contentX, contentY, contentW, contentH), new Rectangle(drawX, drawY, drawW, drawH));
        }

        private static void drawGrid(Graphics2D g2, LayoutSettings settings, MapFrameGeometry mapFrame) {
            int cols = Math.max(2, settings.gridColumns());
            int rows = Math.max(2, settings.gridRows());
            Rectangle bounds = mapFrame.imageBounds();
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(new Color(37, 99, 235, 110));
                copy.setStroke(new BasicStroke(1.1f));
                for (int col = 1; col < cols; col++) {
                    int x = bounds.x + (int) Math.round(bounds.width * (col / (double) cols));
                    copy.drawLine(x, bounds.y, x, bounds.y + bounds.height);
                }
                for (int row = 1; row < rows; row++) {
                    int y = bounds.y + (int) Math.round(bounds.height * (row / (double) rows));
                    copy.drawLine(bounds.x, y, bounds.x + bounds.width, y);
                }

                if (settings.showGridLabels()) {
                    copy.setFont(new Font("SansSerif", Font.BOLD, 11));
                    copy.setColor(new Color(29, 78, 216));
                    for (int col = 0; col < cols; col++) {
                        int centerX = bounds.x + (int) Math.round(bounds.width * ((col + 0.5d) / cols));
                        copy.drawString(letterLabel(col + 1), centerX - 4, bounds.y - 6);
                    }
                    for (int row = 0; row < rows; row++) {
                        int centerY = bounds.y + (int) Math.round(bounds.height * ((row + 0.5d) / rows));
                        copy.drawString(String.valueOf(row + 1), bounds.x - 18, centerY + 4);
                    }
                }
            } finally {
                copy.dispose();
            }
        }

        private static String letterLabel(int index) {
            int value = Math.max(1, index);
            StringBuilder label = new StringBuilder();
            while (value > 0) {
                value--;
                label.insert(0, (char) ('A' + (value % 26)));
                value /= 26;
            }
            return label.toString();
        }

        private static void drawNorthArrow(Graphics2D g2, NorthStyle style, int x, int y, int size) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                NorthStyle resolved = style != null ? style : NorthStyle.CLASSIC;
                int centerX = x + size / 2;
                int top = y + 10;
                int bottom = y + size - 12;

                if (resolved == NorthStyle.SIMPLE) {
                    copy.setColor(new Color(255, 255, 255, 220));
                    copy.fillRoundRect(x + 6, y + 4, size - 12, size - 8, 14, 14);
                    copy.setColor(new Color(34, 44, 60));
                    copy.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, size / 3)));
                    copy.drawString("N", centerX - 5, y + 18);
                    copy.setStroke(new BasicStroke(2f));
                    copy.drawLine(centerX, y + 22, centerX, bottom);
                    copy.drawLine(centerX, y + 22, centerX - 8, y + 34);
                    copy.drawLine(centerX, y + 22, centerX + 8, y + 34);
                    return;
                }

                copy.setColor(new Color(255, 255, 255, 226));
                copy.fill(new Ellipse2D.Double(x, y, size, size));
                copy.setColor(new Color(207, 214, 224));
                copy.draw(new Ellipse2D.Double(x, y, size, size));

                Path2D arrow = new Path2D.Double();
                arrow.moveTo(centerX, top);
                arrow.lineTo(centerX + size / 7d, bottom - size / 5d);
                arrow.lineTo(centerX, bottom - size / 3d);
                arrow.lineTo(centerX - size / 7d, bottom - size / 5d);
                arrow.closePath();

                if (resolved == NorthStyle.MODERN) {
                    copy.setColor(new Color(15, 23, 42));
                    copy.fill(arrow);
                    copy.setColor(new Color(96, 165, 250));
                    copy.fillPolygon(new int[]{centerX, centerX + 6, centerX}, new int[]{top + 6, bottom - 10, bottom - 18}, 3);
                } else {
                    copy.setPaint(new GradientPaint(x, y, new Color(22, 94, 188), x + size, y + size, new Color(8, 54, 117)));
                    copy.fill(arrow);
                }
                copy.setColor(new Color(28, 38, 54));
                copy.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, size / 3)));
                copy.drawString("N", centerX - 6, top - 2);
            } finally {
                copy.dispose();
            }
        }

        private static void drawScaleBar(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, MapFrameGeometry mapFrame, int x, int y) {
            if (snapshot.representativeMeters() <= 0 || mapFrame.imageBounds().width <= 0) {
                return;
            }

            double metersPerPixel = snapshot.representativeMeters() / Math.max(1d, mapFrame.imageBounds().width);
            double targetMeters = metersPerPixel * Math.min(180d, mapFrame.imageBounds().width / 3d);
            double roundedMeters = settings.scaleRule().roundValue(targetMeters);
            int barWidth = (int) Math.max(72, Math.round(roundedMeters / metersPerPixel));
            int segmentCount = barWidth >= 160 ? 4 : 2;
            int segmentWidth = Math.max(1, barWidth / segmentCount);

            double exactDenominator = estimateScaleDenominator(snapshot, mapFrame, settings);
            double roundedDenominator = settings.scaleRule().roundValue(exactDenominator);
            String scaleText = roundedDenominator > 0
                    ? "1:" + new DecimalFormat("#,##0").format(roundedDenominator)
                    : snapshot.scaleLabel();

            if (settings.scaleStyle() == ScaleStyle.NUMERIC) {
                g2.setColor(new Color(255, 255, 255, 218));
                g2.fillRoundRect(x - 12, y - 26, 138, 36, 14, 14);
                g2.setColor(new Color(70, 80, 96));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString(scaleText, x, y - 3);
                return;
            }

            g2.setColor(new Color(255, 255, 255, 218));
            g2.fillRoundRect(x - 12, y - 34, barWidth + 110, 50, 14, 14);
            g2.setColor(new Color(70, 80, 96));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(scaleText, x, y - 14);

            if (settings.scaleStyle() == ScaleStyle.SIMPLE_BAR) {
                g2.setColor(Color.BLACK);
                g2.fillRect(x, y + 2, barWidth, 7);
                g2.drawString("0", x - 4, y + 28);
                g2.drawString(formatDistance(roundedMeters), x + barWidth - 12, y + 28);
                return;
            }

            for (int i = 0; i < segmentCount; i++) {
                g2.setColor(i % 2 == 0 ? Color.BLACK : Color.WHITE);
                int segmentX = x + (i * segmentWidth);
                int width = i == segmentCount - 1 ? barWidth - (segmentWidth * i) : segmentWidth;
                g2.fillRect(segmentX, y, width, 12);
                g2.setColor(Color.BLACK);
                g2.drawRect(segmentX, y, width, 12);
            }
            g2.drawString("0", x - 4, y + 28);
            g2.drawString(formatDistance(roundedMeters), x + barWidth - 12, y + 28);
        }

        private static void drawLegend(Graphics2D g2, LayoutSettings settings, List<Layer> layers, int x, int y, int width, int height) {
            g2.setColor(new Color(250, 252, 255));
            g2.fillRoundRect(x, y, width, height, 18, 18);
            g2.setColor(new Color(210, 216, 224));
            g2.drawRoundRect(x, y, width, height, 18, 18);

            g2.setColor(new Color(26, 36, 52));
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            String legendTitle = !settings.legendTitle().isBlank() ? settings.legendTitle() : "Leyenda";
            g2.drawString(legendTitle, x + 18, y + 28);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(new Color(103, 114, 128));
            String legendSubtitle = !settings.legendSubtitle().isBlank() ? settings.legendSubtitle() : "Capas visibles del mapa";
            g2.drawString(legendSubtitle, x + 18, y + 46);

            List<LegendItem> items = buildLegendItems(layers);
            if (items.isEmpty()) {
                g2.drawString("No hay capas visibles.", x + 18, y + 68);
                return;
            }

            int itemY = y + 76;
            int count = 0;
            for (LegendItem item : items) {
                if (itemY > y + height - 46) {
                    int remaining = Math.max(0, items.size() - count);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    g2.setColor(new Color(108, 116, 128));
                    g2.drawString("+" + remaining + " items mas...", x + 18, y + height - 18);
                    break;
                }
                drawLegendItem(g2, item, x + 18, itemY, width - 36);
                itemY += 30;
                count++;
            }
        }

        private static List<LegendItem> buildLegendItems(List<Layer> layers) {
            List<LegendItem> items = new ArrayList<>();
            if (layers == null) {
                return items;
            }
            for (Layer layer : layers) {
                if (layer == null) {
                    continue;
                }
                if (layer.getLineCategorizedSymbology().isConfigured()) {
                    for (CategoryStyleRule rule : layer.getLineCategorizedSymbology().getRules().values()) {
                        items.add(new LegendItem(rule.getValue(), layer.getName(), layer, rule, "LINE"));
                    }
                    continue;
                }
                if (layer.getPolygonCategorizedSymbology().isConfigured()) {
                    for (CategoryStyleRule rule : layer.getPolygonCategorizedSymbology().getRules().values()) {
                        items.add(new LegendItem(rule.getValue(), layer.getName(), layer, rule, "POLYGON"));
                    }
                    continue;
                }
                items.add(new LegendItem(layer.getName(), layerTypeLabel(layer), layer, null, layerTypeLabel(layer)));
            }
            return items;
        }

        private static void drawLegendItem(Graphics2D g2, LegendItem item, int x, int y, int availableWidth) {
            Layer layer = item.layer();
            if (layer instanceof RasterLayer || layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
                g2.setPaint(new GradientPaint(x, y, new Color(96, 165, 250), x + 18, y + 18, new Color(59, 130, 246)));
                g2.fillRect(x, y - 12, 20, 16);
                g2.setColor(new Color(30, 41, 59));
                g2.drawRect(x, y - 12, 20, 16);
            } else if ("LINE".equalsIgnoreCase(item.geometryType())) {
                drawLineSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("POLYGON".equalsIgnoreCase(item.geometryType())) {
                drawPolygonSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if (layer.getType() != null && layer.getType().toUpperCase().contains("POINT")) {
                drawPointSymbolPreview(g2, layer, x, y);
            } else if (layer.getType() != null && layer.getType().toUpperCase().contains("LINE")) {
                drawLineSymbolPreview(g2, layer, x, y, null);
            } else {
                drawPolygonSymbolPreview(g2, layer, x, y, null);
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(37, 45, 58));
            String name = item.label() != null ? item.label() : "Capa";
            int labelWidth = Math.max(60, availableWidth - 30);
            if (name.length() > 34) {
                name = name.substring(0, 31) + "...";
            }
            g2.drawString(name, x + 30, y - 1);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(107, 114, 128));
            String detail = item.subtitle() != null ? item.subtitle() : layerTypeLabel(layer);
            if (detail.length() > labelWidth / 6) {
                detail = detail.substring(0, Math.max(0, labelWidth / 6 - 3)) + "...";
            }
            g2.drawString(detail, x + 30, y + 13);
        }

        private static FooterRenderResult drawFooter(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, int width, int height, int margin, int footerHeight, MapFrameGeometry mapFrame, LayoutInteractionState interactionState, BufferedImage layoutImage) {
            int top = height - margin - footerHeight;
            g2.setColor(new Color(222, 227, 234));
            g2.drawLine(margin, top, width - margin, top);

            int baseCartoucheWidth = switch (settings.template()) {
                case CLEAN_CENTERED -> Math.min(width / 2, 420);
                case STRONG_CARTOUCHE -> Math.min((int) (width * 0.58d), 660);
                default -> Math.min(width / 2, 520);
            };
            Rectangle cartoucheBounds = applyElementAdjustment(
                    new Rectangle(margin, top + 14, baseCartoucheWidth, footerHeight - 24),
                    interactionState,
                    LayoutElementType.CARTOUCHE
            );
            drawCartouche(g2, settings, snapshot, cartoucheBounds);

            g2.setColor(new Color(37, 45, 58));
            g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, width / 108)));
            String footer = !settings.footer().isBlank() ? settings.footer() : "Generado desde CATGIS Desktop";
            int infoX = cartoucheBounds.x + cartoucheBounds.width + 26;
            g2.drawString(footer, infoX, top + 34);

            g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(12, width / 115)));
            g2.setColor(new Color(99, 110, 124));
            String reference = "Proyecto: " + snapshot.projectName() + " | CRS: " + snapshot.projectCrsLabel();
            g2.drawString(reference, infoX, top + 54);

            java.awt.FontMetrics metrics = g2.getFontMetrics();
            String generation = "Fecha de salida: " + FOOTER_DATE.format(LocalDateTime.now());
            g2.drawString(generation, width - margin - metrics.stringWidth(generation), top + 34);

            String scale = settings.showScale() ? "Escala tecnica: " + snapshot.scaleLabel() : "Escala grafica oculta";
            String mapArea = "Mapa: " + mapFrame.frameBounds().width + " x " + mapFrame.frameBounds().height + " px";
            g2.drawString(scale, width - margin - metrics.stringWidth(scale), top + 54);
            g2.drawString(mapArea, width - margin - metrics.stringWidth(mapArea), top + 74);
            Rectangle profileImageBounds = null;
            if (layoutImage != null) {
                Rectangle baseImageBounds = new Rectangle(
                        infoX,
                        top + 86,
                        Math.max(200, width - margin - infoX),
                        Math.max(110, footerHeight - 104)
                );
                profileImageBounds = applyElementAdjustment(baseImageBounds, interactionState, LayoutElementType.PROFILE_IMAGE);
                drawLayoutImage(g2, profileImageBounds, layoutImage);
            }
            return new FooterRenderResult(cartoucheBounds, profileImageBounds);
        }

        private static void drawCartouche(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, Rectangle bounds) {
            g2.setColor(new Color(248, 250, 253));
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);
            g2.setColor(new Color(201, 210, 222));
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);

            int contentX = bounds.x + 16;
            int contentY = bounds.y + 18;
            int textX = contentX;
            BufferedImage logoImage = loadImageAsset(settings.logoPath());
            if (logoImage != null) {
                int logoBoxW = 84;
                int logoBoxH = Math.min(bounds.height - 28, 72);
                int logoX = contentX;
                int logoY = bounds.y + 16;
                double scale = Math.min(logoBoxW / (double) logoImage.getWidth(), logoBoxH / (double) logoImage.getHeight());
                int drawW = Math.max(1, (int) Math.round(logoImage.getWidth() * scale));
                int drawH = Math.max(1, (int) Math.round(logoImage.getHeight() * scale));
                g2.drawImage(logoImage, logoX, logoY + (logoBoxH - drawH) / 2, drawW, drawH, null);
                textX += logoBoxW + 14;
            }

            g2.setColor(new Color(27, 38, 56));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.drawString("Cartucho del mapa", textX, contentY);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(86, 96, 110));
            int rowY = contentY + 20;
            drawCartoucheRow(g2, "Estudio", blankOr(settings.studyName(), snapshot.projectName()), textX, rowY);
            rowY += 16;
            drawCartoucheRow(g2, "Proyecto", snapshot.projectName(), textX, rowY);
            rowY += 16;
            drawCartoucheRow(g2, "Empresa", blankOr(settings.companyName(), "No especificada"), textX, rowY);
            rowY += 16;
            drawCartoucheRow(g2, "Cartografo", blankOr(settings.cartographerName(), "No especificado"), textX, rowY);
            rowY += 16;
            drawCartoucheRow(g2, "Fuente", blankOr(settings.imageSource(), "Vista actual del proyecto"), textX, rowY);
            rowY += 16;
            drawCartoucheRow(g2, "Coord.", blankOr(settings.coordinateReference(), snapshot.projectCrsLabel()), textX, rowY);
        }

        private static void drawCartoucheRow(Graphics2D g2, String label, String value, int x, int y) {
            g2.setColor(new Color(28, 38, 54));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(label + ":", x, y);
            g2.setColor(new Color(86, 96, 110));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString(clipText(value, 38), x + 62, y);
        }

        private static void drawLayoutImage(Graphics2D g2, Rectangle bounds, BufferedImage image) {
            if (bounds == null || image == null) {
                return;
            }
            g2.setColor(new Color(248, 250, 253));
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);
            g2.setColor(new Color(201, 210, 222));
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);

            g2.setColor(new Color(27, 38, 56));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("Perfil / imagen cartografica", bounds.x + 14, bounds.y + 18);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(96, 105, 118));
            g2.drawString("Movelo o redimensionalo desde el layout", bounds.x + 14, bounds.y + 31);

            int innerX = bounds.x + 12;
            int innerY = bounds.y + 40;
            int innerW = Math.max(40, bounds.width - 24);
            int innerH = Math.max(40, bounds.height - 52);
            g2.setColor(Color.WHITE);
            g2.fillRect(innerX, innerY, innerW, innerH);
            g2.setColor(new Color(214, 220, 228));
            g2.drawRect(innerX, innerY, innerW, innerH);

            double scale = Math.min(innerW / (double) Math.max(1, image.getWidth()), innerH / (double) Math.max(1, image.getHeight()));
            int drawW = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int drawH = Math.max(1, (int) Math.round(image.getHeight() * scale));
            int drawX = innerX + (innerW - drawW) / 2;
            int drawY = innerY + (innerH - drawH) / 2;
            g2.drawImage(image, drawX, drawY, drawW, drawH, null);
        }

        private static BufferedImage loadImageAsset(String path) {
            if (path == null || path.isBlank()) {
                return null;
            }
            try {
                File file = new File(path);
                if (!file.isFile()) {
                    return null;
                }
                return ImageIO.read(file);
            } catch (Exception ex) {
                return null;
            }
        }

        private static String blankOr(String primary, String fallback) {
            return primary != null && !primary.isBlank() ? primary : fallback;
        }

        private static String clipText(String value, int max) {
            if (value == null) {
                return "";
            }
            return value.length() > max ? value.substring(0, Math.max(0, max - 3)) + "..." : value;
        }

        private static Color colorOr(Color color, Color fallback) {
            return color != null ? color : fallback;
        }

        private static double estimateScaleDenominator(LayoutSnapshot snapshot, MapFrameGeometry mapFrame, LayoutSettings settings) {
            if (snapshot.representativeMeters() <= 0 || snapshot.mapImage().getWidth() <= 0 || settings.dpi() <= 0) {
                return 0;
            }
            double shownGroundMeters = snapshot.representativeMeters() * (mapFrame.imageBounds().width / (double) Math.max(1, snapshot.mapImage().getWidth()));
            double mapWidthMetersOnPaper = (mapFrame.frameBounds().width / (double) settings.dpi()) * 0.0254d;
            if (mapWidthMetersOnPaper <= 0) {
                return 0;
            }
            return shownGroundMeters / mapWidthMetersOnPaper;
        }

        private static void drawPointSymbolPreview(Graphics2D g2, Layer layer, int x, int y) {
            Color color = colorOr(layer.getPointColor(), new Color(59, 130, 246));
            int left = x + 3;
            int top = y - 11;
            int size = 12;
            Layer.PointSymbolStyle style = layer.getPointSymbolStyle();
            if (style == null) {
                style = Layer.PointSymbolStyle.CIRCLE;
            }
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(color);
                switch (style) {
                    case SQUARE -> copy.fillRect(left, top, size, size);
                    case DIAMOND -> {
                        Path2D diamond = new Path2D.Double();
                        diamond.moveTo(left + (size / 2d), top);
                        diamond.lineTo(left + size, top + (size / 2d));
                        diamond.lineTo(left + (size / 2d), top + size);
                        diamond.lineTo(left, top + (size / 2d));
                        diamond.closePath();
                        copy.fill(diamond);
                        copy.setColor(new Color(33, 33, 33));
                        copy.draw(diamond);
                        return;
                    }
                    case TRIANGLE -> {
                        Path2D triangle = new Path2D.Double();
                        triangle.moveTo(left + (size / 2d), top);
                        triangle.lineTo(left + size, top + size);
                        triangle.lineTo(left, top + size);
                        triangle.closePath();
                        copy.fill(triangle);
                        copy.setColor(new Color(33, 33, 33));
                        copy.draw(triangle);
                        return;
                    }
                    case TARGET -> {
                        copy.fillOval(left, top, size, size);
                        copy.setColor(Color.WHITE);
                        copy.fillOval(left + 3, top + 3, 6, 6);
                        copy.setColor(new Color(33, 33, 33));
                        copy.drawOval(left, top, size, size);
                        copy.drawLine(left - 1, top + (size / 2), left + size + 1, top + (size / 2));
                        copy.drawLine(left + (size / 2), top - 1, left + (size / 2), top + size + 1);
                        return;
                    }
                    case PIN -> {
                        Path2D pin = new Path2D.Double();
                        pin.moveTo(left + size / 2d, top + size + 2);
                        pin.lineTo(left + size, top + 4);
                        pin.quadTo(left + size + 1, top - 2, left + size / 2d, top);
                        pin.quadTo(left - 1, top - 2, left, top + 4);
                        pin.closePath();
                        copy.fill(pin);
                        copy.setColor(Color.WHITE);
                        copy.fillOval(left + 3, top + 3, 5, 5);
                        copy.setColor(new Color(33, 33, 33));
                        copy.draw(pin);
                        return;
                    }
                    case FLAG -> {
                        copy.setStroke(new BasicStroke(1.5f));
                        copy.drawLine(left + 2, top + 12, left + 2, top);
                        Path2D flag = new Path2D.Double();
                        flag.moveTo(left + 2, top + 1);
                        flag.lineTo(left + 10, top + 3);
                        flag.lineTo(left + 2, top + 6);
                        flag.closePath();
                        copy.fill(flag);
                        return;
                    }
                    case STAR -> {
                        Path2D star = buildStar(left + 6, top + 6, 6, 3);
                        copy.fill(star);
                        copy.setColor(new Color(33, 33, 33));
                        copy.draw(star);
                        return;
                    }
                    case WELL -> {
                        copy.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        Path2D derrick = new Path2D.Double();
                        derrick.moveTo(left + (size / 2d), top);
                        derrick.lineTo(left + size - 1, top + size);
                        derrick.lineTo(left + 1, top + size);
                        derrick.closePath();
                        copy.draw(derrick);
                        copy.drawLine(left + 2, top + size, left + size - 2, top + size);
                        copy.drawLine(left + 3, top + 6, left + size - 3, top + 6);
                        copy.drawLine(left + 3, top + size - 1, left + size / 2, top + 6);
                        copy.drawLine(left + size - 3, top + size - 1, left + size / 2, top + 6);
                        return;
                    }
                    default -> copy.fillOval(left, top, size, size);
                }
                copy.setColor(new Color(33, 33, 33));
                if (style == Layer.PointSymbolStyle.SQUARE) {
                    copy.drawRect(left, top, size, size);
                } else {
                    copy.drawOval(left, top, size, size);
                }
            } finally {
                copy.dispose();
            }
        }

        private static void drawLineSymbolPreview(Graphics2D g2, Layer layer, int x, int y, CategoryStyleRule categoryRule) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(colorOr(categoryRule != null ? categoryRule.getPrimaryColor() : layer.getLineColor(), new Color(16, 185, 129)));
                float previewWidth = categoryRule != null ? categoryRule.getLineWidth() : layer.getLineWidth();
                copy.setStroke(buildLineStroke(Math.max(1.8f, previewWidth), categoryRule != null ? categoryRule.getLineStyle() : layer.getLineSymbolStyle()));
                copy.drawLine(x, y - 4, x + 20, y - 4);
            } finally {
                copy.dispose();
            }
        }

        private static void drawPolygonSymbolPreview(Graphics2D g2, Layer layer, int x, int y, CategoryStyleRule categoryRule) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                Rectangle bounds = new Rectangle(x, y - 12, 20, 16);
                Layer.PolygonFillStyle style = categoryRule != null
                        ? categoryRule.getPolygonFillStyle()
                        : layer.getPolygonFillStyle() != null ? layer.getPolygonFillStyle() : Layer.PolygonFillStyle.SOLID;
                if (style != Layer.PolygonFillStyle.OUTLINE_ONLY) {
                    copy.setPaint(buildPolygonPreviewPaint(layer, bounds, categoryRule));
                    copy.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }
                copy.setPaint(null);
                copy.setColor(colorOr(categoryRule != null ? categoryRule.getSecondaryColor() : layer.getBorderColor(), new Color(146, 64, 14)));
                copy.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            } finally {
                copy.dispose();
            }
        }

        private static Paint buildPolygonPreviewPaint(Layer layer, Rectangle bounds, CategoryStyleRule categoryRule) {
            Color fill = colorOr(categoryRule != null ? categoryRule.getPrimaryColor() : layer.getFillColor(), new Color(251, 191, 36));
            Layer.PolygonFillStyle style = categoryRule != null
                    ? categoryRule.getPolygonFillStyle()
                    : layer.getPolygonFillStyle() != null ? layer.getPolygonFillStyle() : Layer.PolygonFillStyle.SOLID;
            if (style == Layer.PolygonFillStyle.SOLID) {
                return fill;
            }

            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            try {
                g.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 60));
                g.fillRect(0, 0, 10, 10);
                g.setColor(colorOr(categoryRule != null ? categoryRule.getSecondaryColor() : layer.getBorderColor(), new Color(146, 64, 14)));
                switch (style) {
                    case DIAGONAL_HATCH -> {
                        g.drawLine(-2, 9, 9, -2);
                        g.drawLine(2, 11, 11, 2);
                    }
                    case CROSS_HATCH -> {
                        g.drawLine(0, 5, 10, 5);
                        g.drawLine(5, 0, 5, 10);
                    }
                    case DOTS -> {
                        g.fillOval(2, 2, 2, 2);
                        g.fillOval(6, 6, 2, 2);
                    }
                    default -> {
                    }
                }
            } finally {
                g.dispose();
            }
            return new java.awt.TexturePaint(img, bounds);
        }

        private static BasicStroke buildLineStroke(float width, Layer.LineSymbolStyle style) {
            Layer.LineSymbolStyle resolved = style != null ? style : Layer.LineSymbolStyle.SOLID;
            return switch (resolved) {
                case DASHED -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{8f, 5f}, 0f);
                case DOTTED -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{2f, 5f}, 0f);
                case DASH_DOT -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{9f, 4f, 2f, 4f}, 0f);
                default -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            };
        }

        private static Path2D buildStar(double cx, double cy, double outer, double inner) {
            Path2D path = new Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double radius = i % 2 == 0 ? outer : inner;
                double angle = Math.toRadians(-90 + (i * 36));
                double x = cx + Math.cos(angle) * radius;
                double y = cy + Math.sin(angle) * radius;
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            path.closePath();
            return path;
        }

        private static String layerTypeLabel(Layer layer) {
            if (layer instanceof OnlineTileLayer) {
                return "Mapa base online";
            }
            if (layer instanceof OnlineWmsLayer) {
                return "WMS";
            }
            if (layer instanceof RasterLayer) {
                return TopographyWorkflowSupport.isDemLikeRaster(layer) ? "DEM raster" : "Raster";
            }
            if (layer instanceof OnlineWfsLayer) {
                return "WFS";
            }
            if (layer instanceof PostgisLayer) {
                return "PostGIS";
            }
            if (layer instanceof GeoPackageLayer) {
                return "GeoPackage";
            }
            String type = layer.getType();
            if (type == null || type.isBlank()) {
                return "Vectorial";
            }
            return type;
        }

        private static double chooseRoundedDistance(double targetMeters) {
            if (targetMeters <= 0) {
                return 0;
            }
            double exponent = Math.pow(10, Math.floor(Math.log10(targetMeters)));
            double normalized = targetMeters / exponent;
            double rounded;
            if (normalized < 1.5d) {
                rounded = 1d;
            } else if (normalized < 3.5d) {
                rounded = 2d;
            } else if (normalized < 7.5d) {
                rounded = 5d;
            } else {
                rounded = 10d;
            }
            return rounded * exponent;
        }
    }

    private static class ImagePrintable implements Printable {
        private final BufferedImage image;

        private ImagePrintable(BufferedImage image) {
            this.image = image;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0 || image == null) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                double availableWidth = pageFormat.getImageableWidth();
                double availableHeight = pageFormat.getImageableHeight();
                double scale = Math.min(availableWidth / image.getWidth(), availableHeight / image.getHeight());
                AffineTransform transform = new AffineTransform();
                transform.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                transform.scale(scale, scale);
                g2.drawImage(image, transform, null);
                return PAGE_EXISTS;
            } finally {
                g2.dispose();
            }
        }
    }
}
