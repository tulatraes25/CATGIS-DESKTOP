package ar.com.catgis;

public final class AddWmsAction {

    private AddWmsAction() {
    }

    public static void openDialog() {
        AddWmsDialog.open(CatgisDesktopApp.getMainFrameSafe());
    }
}
