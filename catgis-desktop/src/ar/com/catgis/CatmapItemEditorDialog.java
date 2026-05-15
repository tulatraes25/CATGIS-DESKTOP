package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;

public class CatmapItemEditorDialog extends JDialog {

    private final CatmapLayoutItem workingCopy;
    private final JTextField labelField;
    private final JTextArea textArea;
    private final JTextField imagePathField;
    private final JSpinner fontSizeSpinner;
    private final JSpinner lineWidthSpinner;
    private final JCheckBox boldCheck;
    private final JCheckBox italicCheck;
    private final JComboBox<CatmapLayoutItem.HorizontalAlign> alignCombo;
    private final JButton strokeColorButton;
    private final JButton fillColorButton;
    private final JButton textColorButton;
    private final JPanel textPanel;
    private final JPanel imagePanel;
    private final JPanel shapePanel;
    private CatmapLayoutItem result;

    private CatmapItemEditorDialog(Window owner, String title, CatmapLayoutItem item) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.workingCopy = new CatmapLayoutItem(item);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        labelField = new JTextField(workingCopy.getLabel(), 24);
        textArea = new JTextArea(5, 24);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(workingCopy.getText());
        imagePathField = new JTextField(workingCopy.getImagePath(), 24);
        imagePathField.setEditable(false);
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(workingCopy.getFontSize(), 8, 144, 1));
        lineWidthSpinner = new JSpinner(new SpinnerNumberModel((double) workingCopy.getLineWidth(), 1d, 24d, 0.5d));
        boldCheck = new JCheckBox("Negrita", workingCopy.isBold());
        italicCheck = new JCheckBox("Cursiva", workingCopy.isItalic());
        alignCombo = new JComboBox<>(CatmapLayoutItem.HorizontalAlign.values());
        alignCombo.setSelectedItem(workingCopy.getAlign());

        strokeColorButton = createColorButton("Trazo", workingCopy.getStrokeColor());
        fillColorButton = createColorButton("Relleno", workingCopy.getFillColor());
        textColorButton = createColorButton("Texto", workingCopy.getTextColor());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        addField(form, gc, "Etiqueta", labelField);
        gc.gridy += 2;

        textPanel = buildTextPanel();
        imagePanel = buildImagePanel();
        shapePanel = buildShapePanel();

        form.add(textPanel, gc);
        form.add(imagePanel, gc);
        form.add(shapePanel, gc);
        updateVisiblePanels();

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton acceptButton = new JButton("Aceptar");
        acceptButton.addActionListener(e -> onAccept());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        buttons.add(acceptButton);
        buttons.add(cancelButton);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, acceptButton, this::dispose);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    public static CatmapLayoutItem open(Window owner, String title, CatmapLayoutItem item) {
        CatmapItemEditorDialog dialog = new CatmapItemEditorDialog(owner, title, item);
        dialog.setVisible(true);
        return dialog.result;
    }

    private JPanel buildTextPanel() {
        JPanel panel = buildSectionPanel("Texto");
        GridBagConstraints gc = baseConstraints();
        addField(panel, gc, "Contenido", new JScrollPane(textArea));
        gc.gridy += 2;
        addField(panel, gc, "Tamano", fontSizeSpinner);
        gc.gridy += 2;
        addField(panel, gc, "Alineacion", alignCombo);
        gc.gridy += 2;
        JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        stylePanel.setOpaque(false);
        stylePanel.add(boldCheck);
        stylePanel.add(italicCheck);
        panel.add(stylePanel, gc);
        gc.gridy++;
        addField(panel, gc, "Color de texto", textColorButton);
        gc.gridy += 2;
        addField(panel, gc, "Fondo", fillColorButton);
        return panel;
    }

    private JPanel buildImagePanel() {
        JPanel panel = buildSectionPanel("Imagen");
        GridBagConstraints gc = baseConstraints();
        addField(panel, gc, "Archivo", imagePathField);
        gc.gridy += 2;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        JButton chooseButton = new JButton("Buscar imagen...");
        chooseButton.addActionListener(e -> chooseImageFile());
        JButton clearButton = new JButton("Quitar");
        clearButton.addActionListener(e -> imagePathField.setText(""));
        buttons.add(chooseButton);
        buttons.add(clearButton);
        panel.add(buttons, gc);
        return panel;
    }

    private JPanel buildShapePanel() {
        JPanel panel = buildSectionPanel("Estilo");
        GridBagConstraints gc = baseConstraints();
        addField(panel, gc, "Trazo", strokeColorButton);
        gc.gridy += 2;
        if (workingCopy.getKind() != CatmapLayoutItem.Kind.LINE) {
            addField(panel, gc, "Relleno", fillColorButton);
            gc.gridy += 2;
        }
        addField(panel, gc, "Grosor", lineWidthSpinner);
        return panel;
    }

    private JPanel buildSectionPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        return panel;
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        return gc;
    }

    private void addField(JPanel panel, GridBagConstraints gc, String label, Component component) {
        panel.add(new JLabel(label + ":"), gc);
        gc.gridy++;
        panel.add(component, gc);
    }

    private JButton createColorButton(String text, Color initial) {
        JButton button = new JButton(text);
        applyColorPreview(button, initial);
        button.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, text, button.getBackground());
            if (chosen != null) {
                applyColorPreview(button, chosen);
            }
        });
        return button;
    }

    private void applyColorPreview(JButton button, Color color) {
        Color resolved = color != null ? color : Color.WHITE;
        button.setBackground(resolved);
        button.setForeground(contrastColor(resolved));
        button.putClientProperty("catgis.color", resolved);
    }

    private Color contrastColor(Color color) {
        int brightness = (int) ((color.getRed() * 0.299) + (color.getGreen() * 0.587) + (color.getBlue() * 0.114));
        return brightness < 140 ? Color.WHITE : new Color(17, 24, 39);
    }

    private Color buttonColor(JButton button) {
        Object value = button.getClientProperty("catgis.color");
        return value instanceof Color ? (Color) value : button.getBackground();
    }

    private void updateVisiblePanels() {
        boolean text = workingCopy.getKind() == CatmapLayoutItem.Kind.TEXT;
        boolean image = workingCopy.getKind() == CatmapLayoutItem.Kind.IMAGE;
        boolean shape = !text && !image;
        textPanel.setVisible(text);
        imagePanel.setVisible(image);
        shapePanel.setVisible(shape);
    }

    private void chooseImageFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("catmap-image", "Seleccionar imagen CATMAP");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Imagenes (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            FileChooserSupport.rememberSelection("catmap-image", chooser);
            File file = chooser.getSelectedFile();
            imagePathField.setText(file.getAbsolutePath());
            if (workingCopy.getLabel().isBlank() || "Imagen".equalsIgnoreCase(workingCopy.getLabel())) {
                workingCopy.setLabel(file.getName());
                labelField.setText(file.getName());
            }
        }
    }

    private void onAccept() {
        workingCopy.setLabel(labelField.getText());
        switch (workingCopy.getKind()) {
            case TEXT -> {
                workingCopy.setText(textArea.getText());
                workingCopy.setFontSize((Integer) fontSizeSpinner.getValue());
                workingCopy.setBold(boldCheck.isSelected());
                workingCopy.setItalic(italicCheck.isSelected());
                workingCopy.setAlign((CatmapLayoutItem.HorizontalAlign) alignCombo.getSelectedItem());
                workingCopy.setTextColor(buttonColor(textColorButton));
                workingCopy.setFillColor(buttonColor(fillColorButton));
            }
            case IMAGE -> workingCopy.setImagePath(imagePathField.getText());
            case RECTANGLE, ELLIPSE, LINE -> {
                workingCopy.setStrokeColor(buttonColor(strokeColorButton));
                if (workingCopy.getKind() != CatmapLayoutItem.Kind.LINE) {
                    workingCopy.setFillColor(buttonColor(fillColorButton));
                }
                Number width = (Number) lineWidthSpinner.getValue();
                workingCopy.setLineWidth(width != null ? width.floatValue() : 2f);
            }
        }
        result = workingCopy;
        dispose();
    }
}
