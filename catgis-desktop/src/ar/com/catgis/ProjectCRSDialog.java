package ar.com.catgis;

public class ProjectCRSDialog {

    public static void openDialog() {
        String currentCode = "EPSG:4326";

        if (CatgisDesktopApp.currentProject != null && CatgisDesktopApp.currentProject.getProjectCRS() != null) {
            currentCode = CatgisDesktopApp.currentProject.getProjectCRS();
        }

        String finalCurrentCode = currentCode;

        CRSSelectorDialog.open("CRS del proyecto", finalCurrentCode, code -> {
            if (CatgisDesktopApp.currentProject == null) {
                CatgisDesktopApp.currentProject = new Project("Proyecto actual");
            }

            CatgisDesktopApp.currentProject.setProjectCRS(code);
            CatgisDesktopApp.markProjectDirty();

            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage(
                        "CRS del proyecto: " + CRSDefinitions.getLabelForCode(code)
                );
            }

            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.reloadRasterLayersForProjectCRS();
            }

            CatgisDesktopApp.updateWindowTitle();
        });
    }
}
