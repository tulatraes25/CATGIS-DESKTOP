package ar.com.catgis;
import ar.com.catgis.core.model.Project;

public class ProjectCRSDialog {

    public static void openDialog() {
        String currentCode = "EPSG:4326";

        if (AppContext.project() != null && AppContext.project().getProjectCRS() != null) {
            currentCode = AppContext.project().getProjectCRS();
        }

        String finalCurrentCode = currentCode;

        CRSSelectorDialog.open("CRS del proyecto", finalCurrentCode, code -> {
            if (AppContext.project() == null) {
                AppContext.setCurrentProject(new Project("Proyecto actual"));
            }

            AppContext.project().setProjectCRS(code);
            CatgisDesktopApp.markProjectDirty();

            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage(
                        "CRS del proyecto: " + CRSDefinitions.getLabelForCode(code)
                );
            }

            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().reloadRasterLayersForProjectCRS();
            }

            CatgisDesktopApp.updateWindowTitle();
        });
    }
}
