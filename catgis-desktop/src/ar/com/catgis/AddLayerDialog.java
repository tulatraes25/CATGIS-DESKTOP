package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

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
    private File[] selectedFiles = new File[0];

    private AddLayerDialog(Window owner) {
        super(owner, I18n.t("Cargar datos"), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel formatLabel = new JLabel(I18n.t("Formato:"));
        formatCombo = new JComboBox<>(new FormatOption[]{
                new FormatOption(I18n.t("Fichero shape"), "shp"),
                new FormatOption(I18n.t("GeoPackage"), "gpkg"),
                new FormatOption(I18n.t("GeoJSON"), "geojson"),
                new FormatOption(I18n.t("GPX"), "gpx"),
                new FormatOption(I18n.t("KML / KMZ"), "kml"),
                new FormatOption(I18n.t("Fichero de imagen"), "image"),
                new FormatOption(I18n.t("CAD DWG / DXF"), "dxf"),
                new FormatOption(I18n.t("Datos climáticos (NetCDF)"), "netcdf"),
                new FormatOption(I18n.t("Todos los soportados"), "all")
        });

        JLabel fileLabel = new JLabel(I18n.t("Archivo:"));
        selectedFileField = new JTextField();
        selectedFileField.setEditable(false);

        browseButton = new JButton(I18n.t("Buscar..."));
        browseButton.addActionListener(e -> chooseFile());

        JLabel hintLabel = new JLabel("<html><span style='color:#555555'>" + I18n.t("Selecciona el formato y luego el archivo a incorporar al proyecto actual.") + "</span></html>");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acceptButton = new JButton(I18n.t("Aceptar"));
        acceptButton.addActionListener(e -> onAccept());
        cancelButton = new JButton(I18n.t("Cancelar"));
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

        DialogKeyboardSupport.install(this, acceptButton, this::dispose);
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
        JFileChooser chooser = FileChooserSupport.createChooser("open-layer-data", "Cargar datos");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(true);

        FormatOption selected = (FormatOption) formatCombo.getSelectedItem();
        if (selected == null) {
            selected = new FormatOption(I18n.t("Todos los soportados"), "all");
        }

        configureChooserForFormat(chooser, selected);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            if (files == null || files.length == 0) {
                File file = chooser.getSelectedFile();
                files = file != null ? new File[]{file} : new File[0];
            }
            if (files.length > 0) {
                selectedFiles = files;
                FileChooserSupport.rememberSelection("open-layer-data", chooser);
                selectedFileField.setText(buildSelectionLabel(files));
            }
        }
    }

    private void onAccept() {
        if (selectedFiles.length == 0) {
            JOptionPane.showMessageDialog(this, I18n.t("Primero selecciona uno o varios archivos."), I18n.t("Cargar datos"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        FormatOption selected = (FormatOption) formatCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, I18n.t("No se pudo determinar el formato seleccionado."), I18n.t("Cargar datos"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean loaded = OpenFileAction.openSelectedFiles(selectedFiles, selected.key, this);
        if (loaded) {
            dispose();
        }
    }

    private String buildSelectionLabel(File[] files) {
        if (files == null || files.length == 0) {
            return "";
        }
        if (files.length == 1) {
            return files[0].getAbsolutePath();
        }
        return I18n.format("{0} archivos seleccionados | {1}", files.length, files[0].getName());
    }

    private void configureChooserForFormat(JFileChooser chooser, FormatOption option) {
        switch (option.key) {
            case "shp":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("Fichero shape (*.shp)"), "shp"));
                break;
            case "gpkg":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("GeoPackage (*.gpkg)"), "gpkg"));
                break;
            case "geojson":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("GeoJSON (*.geojson, *.json)"), "geojson", "json"));
                break;
            case "gpx":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("GPX (*.gpx)"), "gpx"));
                break;
            case "kml":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("KML / KMZ (*.kml, *.kmz)"), "kml", "kmz"));
                break;
            case "image":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        I18n.t("Fichero raster (*.tif, *.tiff, *.img, *.asc, *.jpg, *.jpeg, *.png, *.bmp, *.gif)"),
                        "tif", "tiff", "img", "asc", "jpg", "jpeg", "png", "bmp", "gif"
                ));
                break;
            case "dxf":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("CAD DWG / DXF (*.dwg, *.dxf)"), "dwg", "dxf"));
                break;
            case "netcdf":
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("Datos climáticos NetCDF (*.nc, *.nc4)"), "nc", "nc4"));
                break;
            case "all":
            default:
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        I18n.t("Todos los archivos GIS soportados"),
                        "shp", "gpkg", "geojson", "json", "gpx", "kml", "kmz", "tif", "tiff", "img", "asc", "jpg", "jpeg", "png", "bmp", "gif", "dwg", "dxf"
                ));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("Fichero shape (*.shp)"), "shp"));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("GeoPackage (*.gpkg)"), "gpkg"));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("GeoJSON (*.geojson, *.json)"), "geojson", "json"));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("GPX (*.gpx)"), "gpx"));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("KML / KMZ (*.kml, *.kmz)"), "kml", "kmz"));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        I18n.t("Fichero raster (*.tif, *.tiff, *.img, *.asc, *.jpg, *.jpeg, *.png, *.bmp, *.gif)"),
                        "tif", "tiff", "img", "asc", "jpg", "jpeg", "png", "bmp", "gif"
                ));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(I18n.t("CAD DWG / DXF (*.dwg, *.dxf)"), "dwg", "dxf"));
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
