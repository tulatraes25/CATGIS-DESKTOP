package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

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
            if (AppContext.project() == null) {
                AppContext.setCurrentProject(new Project("Proyecto actual"));
            }

            String projectCrs = AppContext.project().getProjectCRS() != null
                    && !AppContext.project().getProjectCRS().isBlank()
                    ? AppContext.project().getProjectCRS()
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

            AppContext.project().addLayer(layer);
            CatgisDesktopApp.markProjectDirty();

            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.addLayer(layer);
                AppContext.selectLayer(layer);
            }

            if (AppContext.mapPanel() != null) {
                if (AppContext.mapPanel().getShapefileData(layer) == null) {
                    AppContext.mapPanel().addOrUpdateShapefileLayer(layer, data);
                }
                AppContext.mapPanel().prepareLayerForEditing(layer);
                AppContext.mapPanel().showOpenedFile(layer.getName());
                AppContext.mapPanel().repaint();
            }

            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Nueva capa vectorial creada: " + layer.getName());
            }

            return layer;
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo crear la nueva capa vectorial " + file.getAbsolutePath(), ex);
            AppErrorSupport.showErrorDialog(
                    owner,
                    "Nueva capa vectorial",
                    "No se pudo crear la nueva capa vectorial.",
                    ex
            );
            return null;
        }
    }
}
