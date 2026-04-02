package ar.com.catgis;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class SaveProjectAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        saveProject();
    }

    public static boolean saveProject() {
        if (CatgisDesktopApp.currentProject == null) {
            JOptionPane.showMessageDialog(null, "No hay proyecto actual.");
            return false;
        }

        File targetFile = CatgisDesktopApp.currentProject.getProjectFile();
        if (targetFile == null) {
            return saveProjectAs();
        }

        return saveProjectToFile(targetFile);
    }

    public static boolean saveProjectAs() {
        if (CatgisDesktopApp.currentProject == null) {
            JOptionPane.showMessageDialog(null, "No hay proyecto actual.");
            return false;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar proyecto CATGIS como");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Proyectos CATGIS (*.catgis)", "catgis"));
        chooser.setSelectedFile(buildSuggestedFile());

        int result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".catgis")) {
            file = new File(file.getAbsolutePath() + ".catgis");
        }

        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    null,
                    "El archivo ya existe.\n¿Querés reemplazarlo?\n\n" + file.getAbsolutePath(),
                    "Guardar proyecto",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        return saveProjectToFile(file);
    }

    private static boolean saveProjectToFile(File file) {
        if (!persistVectorLayers()) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("CATGIS_PROJECT");
            writer.newLine();

            writer.write("PROJECT_CRS|" + safe(CatgisDesktopApp.currentProject.getProjectCRS()));
            writer.newLine();

            writer.write("VIEW|"
                    + CatgisDesktopApp.mapPanel.getViewMinX() + "|"
                    + CatgisDesktopApp.mapPanel.getViewMinY() + "|"
                    + CatgisDesktopApp.mapPanel.getZoomFactor());
            writer.newLine();

            for (Layer layer : CatgisDesktopApp.currentProject.getLayers()) {
                String line = buildLayerLine(layer);
                writer.write(line);
                writer.newLine();
            }

            CatgisDesktopApp.currentProject.setProjectFile(file);
            CatgisDesktopApp.currentProject.setName(stripExtension(file.getName()));
            CatgisDesktopApp.markProjectClean();

            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("Proyecto guardado: " + file.getAbsolutePath());
            }

            JOptionPane.showMessageDialog(null, "Proyecto guardado correctamente.");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar proyecto: " + ex.getMessage());
            return false;
        }
    }

    private static String buildLayerLine(Layer layer) {
        String type = safe(layer.getType());
        String name = safe(layer.getName());
        String path = safe(layer.getPath());
        String visible = String.valueOf(layer.isVisible());
        String labelsVisible = String.valueOf(layer.isLabelsVisible());
        String labelField = safe(layer.getLabelField());
        String sourceCRS = safe(layer.getSourceCRS());

        String fillColor = colorToText(layer.getFillColor());
        String borderColor = colorToText(layer.getBorderColor());
        String lineColor = colorToText(layer.getLineColor());
        String lineWidth = String.valueOf(layer.getLineWidth());
        String pointColor = colorToText(layer.getPointColor());
        String pointSize = String.valueOf(layer.getPointSize());

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(type).append("]")
                .append("|").append(name)
                .append("|").append(path)
                .append("|").append(visible)
                .append("|").append(labelsVisible)
                .append("|").append(labelField)
                .append("|").append(fillColor)
                .append("|").append(borderColor)
                .append("|").append(lineColor)
                .append("|").append(lineWidth)
                .append("|").append(pointColor)
                .append("|").append(pointSize)
                .append("|").append(sourceCRS);

        if (layer instanceof RasterLayer) {
            RasterLayer raster = (RasterLayer) layer;
            sb.append("|").append(raster.getOpacity())
              .append("|").append(raster.isGrayscale())
              .append("|").append(raster.isAutoContrast())
              .append("|").append(raster.getRedBand())
              .append("|").append(raster.getGreenBand())
              .append("|").append(raster.getBlueBand())
              .append("|").append(safe(raster.getRasterMode()));
        }

        return sb.toString();
    }

    private static File buildSuggestedFile() {
        File currentFile = CatgisDesktopApp.currentProject.getProjectFile();
        if (currentFile != null) {
            return currentFile;
        }

        String projectName = CatgisDesktopApp.currentProject.getName();
        if (projectName == null || projectName.isBlank()) {
            projectName = "proyecto";
        }

        String safeName = projectName.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (safeName.isEmpty()) {
            safeName = "proyecto";
        }
        return new File(safeName + ".catgis");
    }

    private static String stripExtension(String name) {
        if (name == null || name.isBlank()) {
            return "Proyecto sin nombre";
        }

        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            return name.substring(0, idx);
        }
        return name;
    }

    private static String colorToText(java.awt.Color color) {
        if (color == null) {
            return "";
        }
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha();
    }

    private static boolean persistVectorLayers() {
        if (CatgisDesktopApp.currentProject == null || CatgisDesktopApp.mapPanel == null) {
            return true;
        }

        for (Layer layer : CatgisDesktopApp.currentProject.getLayers()) {
            if (layer == null || layer instanceof RasterLayer) {
                continue;
            }

            ShapefileData data = CatgisDesktopApp.mapPanel.getShapefileData(layer);
            if (!ExportVectorLayerAction.hasExportableVectorData(data)) {
                continue;
            }

            boolean ok;
            if (ExportVectorLayerAction.hasSupportedVectorPath(layer)) {
                ok = ExportVectorLayerAction.saveLayerToCurrentPath(layer, CatgisDesktopApp.getMainFrameSafe(), false);
            } else {
                JOptionPane.showMessageDialog(
                        CatgisDesktopApp.getMainFrameSafe(),
                        "La capa \"" + layer.getName() + "\" no tiene archivo asociado.\nElegí dónde guardarla antes de guardar el proyecto.",
                        "Guardar capa vectorial",
                        JOptionPane.INFORMATION_MESSAGE
                );
                File exported = ExportVectorLayerAction.exportLayerWithDialog(
                        layer,
                        data,
                        CatgisDesktopApp.getMainFrameSafe(),
                        "Guardar capa vectorial",
                        false
                );
                ok = exported != null;
            }

            if (!ok) {
                return false;
            }
        }

        return true;
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("|", " ");
    }
}
