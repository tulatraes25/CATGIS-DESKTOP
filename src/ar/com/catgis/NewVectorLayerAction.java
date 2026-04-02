package ar.com.catgis;

import org.locationtech.jts.geom.Geometry;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

public class NewVectorLayerAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        createNewVectorLayer(null, CatgisDesktopApp.getMainFrameSafe());
    }

    public static Layer createNewVectorLayer(String geometryHint, Component parent) {
        Window owner = parent instanceof Window
                ? (Window) parent
                : (Window) CatgisDesktopApp.getMainFrameSafe();

        NewVectorLayerDialog.Result result = NewVectorLayerDialog.open(owner, geometryHint);
        if (result == null) {
            return null;
        }

        File file = result.getFile();
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    owner,
                    "El archivo ya existe.\nQueres reemplazarlo?\n\n" + file.getAbsolutePath(),
                    "Nueva capa vectorial",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return null;
            }
        }

        try {
            if (CatgisDesktopApp.currentProject == null) {
                CatgisDesktopApp.currentProject = new Project("Proyecto actual");
            }

            String projectCrs = CatgisDesktopApp.currentProject.getProjectCRS() != null
                    && !CatgisDesktopApp.currentProject.getProjectCRS().isBlank()
                    ? CatgisDesktopApp.currentProject.getProjectCRS()
                    : "EPSG:4326";

            Class<? extends Geometry> geometryClass = DrawFeatureBuilder.resolveGeometryClass(result.getGeometryKind());
            ShapefileData data = DrawFeatureBuilder.buildEmptyLayer(
                    result.getLayerName(),
                    geometryClass,
                    result.getFieldConfigs(),
                    projectCrs
            );

            Layer layer = new Layer(result.getLayerName(), file.getAbsolutePath(), "VECTOR");
            layer.setVisible(true);
            layer.setSourceName(file.getName());
            layer.setFeatureCount(0);
            layer.setSourceCRS(projectCrs);

            for (FieldConfig config : result.getFieldConfigs()) {
                if (config == null) {
                    continue;
                }
                FieldConfig layerConfig = layer.getOrCreateFieldConfig(config.getFieldName(), config.getTypeName());
                layerConfig.setPublicName(config.getPublicName());
                layerConfig.setVisible(config.isVisible());
                layerConfig.setEditable(config.isEditable());
                layerConfig.setLength(config.getLength());
                layerConfig.setPrecision(config.getPrecision());
            }

            boolean saved = ExportVectorLayerAction.saveLayerDataToFile(layer, data, file, owner, false);
            if (!saved) {
                return null;
            }

            CatgisDesktopApp.currentProject.addLayer(layer);
            CatgisDesktopApp.markProjectDirty();

            if (CatgisDesktopApp.layersPanel != null) {
                CatgisDesktopApp.layersPanel.addLayer(layer);
                CatgisDesktopApp.layersPanel.selectLayer(layer);
            }

            if (CatgisDesktopApp.mapPanel != null) {
                if (CatgisDesktopApp.mapPanel.getShapefileData(layer) == null) {
                    CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
                }
                CatgisDesktopApp.mapPanel.prepareLayerForEditing(layer);
                CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
                CatgisDesktopApp.mapPanel.repaint();
            }

            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("Nueva capa vectorial creada: " + layer.getName());
            }

            return layer;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    owner,
                    "No se pudo crear la nueva capa vectorial: " + ex.getMessage(),
                    "Nueva capa vectorial",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }
}
