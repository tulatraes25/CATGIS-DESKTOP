package ar.com.catgis;
import ar.com.catgis.core.model.Project;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class NewProjectAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        newProject();
    }

    public static void newProject() {
        if (!CatgisDesktopApp.confirmProjectContinuation("crear un proyecto nuevo")) {
            return;
        }

        String inheritedCrs = "EPSG:4326";
        if (CatgisDesktopApp.currentProject != null
                && CatgisDesktopApp.currentProject.getProjectCRS() != null
                && !CatgisDesktopApp.currentProject.getProjectCRS().isBlank()) {
            inheritedCrs = CatgisDesktopApp.currentProject.getProjectCRS();
        }

        Project project = new Project("Proyecto sin nombre");
        project.setProjectCRS(inheritedCrs);

        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.clearLayers();
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.clearAllLayers();
            CatgisDesktopApp.mapPanel.resetView();
            CatgisDesktopApp.mapPanel.repaint();
        }

        CatgisDesktopApp.currentProject = project;
        CatgisDesktopApp.markProjectClean();

        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(
                    "Nuevo proyecto creado | CRS: " + CRSDefinitions.getLabelForCode(project.getProjectCRS())
            );
        }
    }
}
