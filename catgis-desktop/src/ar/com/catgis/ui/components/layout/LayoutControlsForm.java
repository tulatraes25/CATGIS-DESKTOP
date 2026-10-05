package ar.com.catgis.ui.components.layout;

import ar.com.catgis.AppIcons;
import ar.com.catgis.CatmapLayoutItem;
import ar.com.catgis.I18n;
import ar.com.catgis.layout.LegendPlacement;
import ar.com.catgis.layout.NorthStyle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Scrollable controls form with all document, grid, legend, metadata, and CATMAP element sections.
 * Extracted from {@code buildScrollableControlsPanel()} and {@code addCatmapElementsSection()}.
 */
public class LayoutControlsForm extends JPanel {

    private static void addSection(JPanel panel, GridBagConstraints gc, String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(new Color(27, 38, 56));
        panel.add(label, gc);
    }

    private static void addField(JPanel panel, GridBagConstraints gc, String labelText, java.awt.Component field) {
        panel.add(new JLabel(labelText + ":"), gc);
        gc.gridy++;
        panel.add(field, gc);
    }

    @SuppressWarnings("unchecked")
    public LayoutControlsForm(
            // Document fields
            JTextField titleField,
            JTextField subtitleField,
            JTextField footerField,
            // Grid fields
            JCheckBox gridCheck,
            JSpinner gridColumnsSpinner,
            JSpinner gridRowsSpinner,
            JCheckBox gridLabelsCheck,
            // Legend/north fields
            JComboBox<LegendPlacement> legendPlacementCombo,
            JTextField legendTitleField,
            JTextField legendSubtitleField,
            JComboBox<NorthStyle> northStyleCombo,
            JCheckBox northCheck,
            JCheckBox legendCheck,
            // Metadata fields
            JTextField studyField,
            JTextField companyField,
            JTextField cartographerField,
            JTextField imageSourceField,
            JTextField coordinateReferenceField,
            JTextField logoPathField,
            JTextField layoutImagePathField,
            // Callbacks for logo/image selectors
            Runnable onChooseLogo,
            Runnable onClearLogo,
            Runnable onChooseLayoutImage,
            Runnable onClearLayoutImage,
            // CATMAP elements section
            LayoutStructureTreePanel structureTreePanel,
            Consumer<CatmapLayoutItem.Kind> onAddCatmapItem,
            Runnable onAddCatmapImageItem,
            Runnable onDuplicateCatmapItem,
            Runnable onRemoveCatmapItem,
            Runnable onRestoreDefaults,
            JList<CatmapLayoutItem> layoutItemsList,
            JPanel catmapElementsCardPanel,  // already has CardLayout in caller
            // Inspector
            LayoutInspectorPanel inspectorPanel,
            // Map state
            JLabel currentMapLabel,
            Runnable onRefreshSnapshot) {

        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setPreferredSize(new Dimension(352, 1180));
        setBackground(Color.WHITE);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        addSection(this, gc, "Documento cartografico");
        gc.gridy++;
        addField(this, gc, "Titulo", titleField);
        gc.gridy += 2;
        addField(this, gc, "Subtitulo", subtitleField);
        gc.gridy += 2;
        addField(this, gc, "Pie o referencia", footerField);
        gc.gridy += 2;

        addSection(this, gc, "Grilla cartografica");
        gc.gridy++;
        add(gridCheck, gc);
        gc.gridy++;
        addField(this, gc, "Columnas", gridColumnsSpinner);
        gc.gridy += 2;
        addField(this, gc, "Filas", gridRowsSpinner);
        gc.gridy += 2;
        add(gridLabelsCheck, gc);
        gc.gridy++;

        addSection(this, gc, "Leyenda y norte");
        gc.gridy++;
        addField(this, gc, "Ubicacion de leyenda", legendPlacementCombo);
        gc.gridy += 2;
        addField(this, gc, "Titulo de leyenda", legendTitleField);
        gc.gridy += 2;
        addField(this, gc, "Subtitulo de leyenda", legendSubtitleField);
        gc.gridy += 2;
        addField(this, gc, "Simbolo de norte", northStyleCombo);
        gc.gridy += 2;
        add(northCheck, gc);
        gc.gridy++;
        add(legendCheck, gc);
        gc.gridy++;

        addSection(this, gc, "Datos cartograficos y metadatos");
        gc.gridy++;
        addField(this, gc, "Nombre del estudio", studyField);
        gc.gridy += 2;
        addField(this, gc, "Empresa", companyField);
        gc.gridy += 2;
        addField(this, gc, "Cartografo", cartographerField);
        gc.gridy += 2;
        addField(this, gc, "Origen de la imagen", imageSourceField);
        gc.gridy += 2;
        addField(this, gc, "Coordenadas / referencia", coordinateReferenceField);
        gc.gridy += 2;

        // Logo selector
        add(new JLabel("Logo de empresa:"), gc);
        gc.gridy++;
        add(logoPathField, gc);
        gc.gridy++;
        JPanel logoButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logoButtons.setOpaque(false);
        JButton chooseLogoBtn = new JButton("Cargar logo...");
        chooseLogoBtn.addActionListener(e -> onChooseLogo.run());
        JButton clearLogoBtn = new JButton("Quitar logo");
        clearLogoBtn.addActionListener(e -> onClearLogo.run());
        logoButtons.add(chooseLogoBtn);
        logoButtons.add(clearLogoBtn);
        add(logoButtons, gc);
        gc.gridy += 2;

        // Layout image selector
        add(new JLabel(I18n.t("Perfil / grafico anclado:")), gc);
        gc.gridy++;
        add(layoutImagePathField, gc);
        gc.gridy++;
        JPanel imageButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        imageButtons.setOpaque(false);
        JButton chooseImageBtn = new JButton(I18n.t("Cargar imagen..."));
        chooseImageBtn.addActionListener(e -> onChooseLayoutImage.run());
        JButton clearImageBtn = new JButton(I18n.t("Quitar imagen"));
        clearImageBtn.addActionListener(e -> onClearLayoutImage.run());
        imageButtons.add(chooseImageBtn);
        imageButtons.add(clearImageBtn);
        add(imageButtons, gc);
        gc.gridy += 2;

        // CATMAP elements section
        addSection(this, gc, "Elementos CATMAP");
        gc.gridy++;

        JLabel tip = new JLabel("<html>Administra los elementos visibles del layout. Los predeterminados se editan desde el mapa o desde la lista.</html>");
        tip.setFont(tip.getFont().deriveFont(Font.PLAIN, 11f));
        tip.setForeground(new Color(88, 98, 112));
        add(tip, gc);
        gc.gridy++;

        add(structureTreePanel, gc);
        gc.gridy++;

        JPanel buttons = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        buttons.setOpaque(false);

        JButton addTextButton = new JButton("Texto");
        addTextButton.addActionListener(e -> onAddCatmapItem.accept(CatmapLayoutItem.Kind.TEXT));
        JButton addImageButton = new JButton("Imagen");
        addImageButton.addActionListener(e -> onAddCatmapImageItem.run());
        JButton addRectButton = new JButton("Rectangulo");
        addRectButton.addActionListener(e -> onAddCatmapItem.accept(CatmapLayoutItem.Kind.RECTANGLE));
        JButton addEllipseButton = new JButton("Elipse");
        addEllipseButton.addActionListener(e -> onAddCatmapItem.accept(CatmapLayoutItem.Kind.ELLIPSE));
        JButton addLineButton = new JButton("Linea");
        addLineButton.addActionListener(e -> onAddCatmapItem.accept(CatmapLayoutItem.Kind.LINE));
        JButton duplicateButton = new JButton("Duplicar");
        duplicateButton.addActionListener(e -> onDuplicateCatmapItem.run());
        JButton removeButton = new JButton("Quitar");
        removeButton.addActionListener(e -> onRemoveCatmapItem.run());
        JButton restoreButton = new JButton("Restaurar base");
        restoreButton.addActionListener(e -> onRestoreDefaults.run());
        buttons.add(addTextButton);
        buttons.add(addImageButton);
        buttons.add(addRectButton);
        buttons.add(addEllipseButton);
        buttons.add(addLineButton);
        buttons.add(duplicateButton);
        buttons.add(removeButton);
        buttons.add(restoreButton);
        add(buttons, gc);
        gc.gridy++;

        JLabel stackLabel = new JLabel("Elementos agregados");
        stackLabel.setFont(stackLabel.getFont().deriveFont(Font.BOLD, 12f));
        stackLabel.setForeground(new Color(63, 74, 88));
        add(stackLabel, gc);
        gc.gridy++;

        JScrollPane scrollPane = new JScrollPane(layoutItemsList);
        scrollPane.setPreferredSize(new Dimension(280, 104));
        add(scrollPane, gc);
        gc.gridy++;

        add(inspectorPanel, gc);
        gc.gridy++;

        // Map state section
        addSection(this, gc, "Estado del mapa");
        gc.gridy++;
        gc.insets = new Insets(10, 6, 4, 6);
        currentMapLabel.setForeground(new Color(63, 74, 88));
        add(currentMapLabel, gc);
        gc.gridy++;
        gc.insets = new Insets(10, 6, 6, 6);
        JButton refreshButton = new JButton("Actualizar mapa", AppIcons.attrRefreshIcon());
        refreshButton.addActionListener(e -> onRefreshSnapshot.run());
        add(refreshButton, gc);
        gc.gridy++;
        gc.weighty = 1;
        add(new JLabel(""), gc);
    }
}
