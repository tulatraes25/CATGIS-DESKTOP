package ar.com.catgis;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class AddLayerDialog extends JDialog {

    private final JComboBox<FormatOption> formatCombo;
    private final JTextField selectedFileField;
    private final JButton browseButton;
    private final JButton acceptButton;
    private final JButton cancelButton;
    private File selectedFile;

    private AddLayerDialog(Window owner) {
        super(owner, "Cargar datos", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel formatLabel = new JLabel("Formato:");
        formatCombo = new JComboBox<>(new FormatOption[]{
                new FormatOption("Fichero shape", "shp"),
                new FormatOption("GeoJSON", "geojson"),
                new FormatOption("KML", "kml"),
                new FormatOption("Fichero de imagen", "image"),
                new FormatOption("Ficheros DXF", "dxf"),
                new FormatOption("Todos los soportados", "all")
        });

        JLabel fileLabel = new JLabel("Archivo:");
        selectedFileField = new JTextField();
        selectedFileField.setEditable(false);

        browseButton = new JButton("Buscar...");
        browseButton.addActionListener(e -> chooseFile());

        JLabel hintLabel = new JLabel("<html><span style='color:#555555'>Selecciona el formato y luego el archivo a incorporar al proyecto actual.</span></html>");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acceptButton = new JButton("Aceptar");
        acceptButton.addActionListener(e -> onAccept());
        cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(acceptButton);
        buttonPanel.add(cancelButton);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        content.add(formatLabel, gc);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1;
        content.add(formatCombo, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        content.add(fileLabel, gc);
        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1;
        content.add(selectedFileField, gc);
        gc.gridx = 2; gc.gridy = 1; gc.weightx = 0;
        content.add(browseButton, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 3; gc.weightx = 1;
        content.add(hintLabel, gc);

        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(acceptButton);
        setResizable(false);
        pack();
        setSize(Math.max(getWidth(), 640), getHeight());
        setLocationRelativeTo(owner);
    }

    public static void open(Window owner) {
        AddLayerDialog dialog = new AddLayerDialog(owner);
        dialog.setVisible(true);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Cargar datos");
        chooser.setAcceptAllFileFilterUsed(false);

        FormatOption selected = (FormatOption) formatCombo.getSelectedItem();
        if (selected == null) {
            selected = new FormatOption("Todos los soportados", "all");
        }

        configureChooserForFormat(chooser, selected);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            selectedFileField.setText(selectedFile != null ? selectedFile.getAbsolutePath() : "");
        }
    }

    private void onAccept() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Primero selecciona un archivo.", "Cargar datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FormatOption selected = (FormatOption) formatCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "No se pudo determinar el formato seleccionado.", "Cargar datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean loaded = OpenFileAction.openSelectedFile(selectedFile, selected.key, this);
        if (loaded) {
            dispose();
        }
    }

    private void configureChooserForFormat(JFileChooser chooser, FormatOption option) {
        switch (option.key) {
            case "shp":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Fichero shape (*.shp)", "shp"));
                break;
            case "geojson":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("GeoJSON (*.geojson, *.json)", "geojson", "json"));
                break;
            case "kml":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("KML (*.kml)", "kml"));
                break;
            case "image":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        "Fichero de imagen (*.tif, *.tiff, *.img, *.jpg, *.jpeg, *.png, *.bmp, *.gif)",
                        "tif", "tiff", "img", "jpg", "jpeg", "png", "bmp", "gif"
                ));
                break;
            case "dxf":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Ficheros DXF (*.dxf)", "dxf"));
                break;
            case "all":
            default:
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        "Todos los archivos GIS soportados",
                        "shp", "geojson", "json", "kml", "tif", "tiff", "img", "jpg", "jpeg", "png", "bmp", "gif", "dxf"
                ));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Fichero shape (*.shp)", "shp"));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("GeoJSON (*.geojson, *.json)", "geojson", "json"));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("KML (*.kml)", "kml"));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        "Fichero de imagen (*.tif, *.tiff, *.img, *.jpg, *.jpeg, *.png, *.bmp, *.gif)",
                        "tif", "tiff", "img", "jpg", "jpeg", "png", "bmp", "gif"
                ));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Ficheros DXF (*.dxf)", "dxf"));
                break;
        }
    }

    private static class FormatOption {
        private final String label;
        private final String key;

        private FormatOption(String label, String key) {
            this.label = label;
            this.key = key;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
