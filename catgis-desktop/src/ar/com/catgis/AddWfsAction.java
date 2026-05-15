package ar.com.catgis;

public final class AddWfsAction {

    private AddWfsAction() {
    }

    public static void openDialog() {
        AddWfsDialog.open(CatgisDesktopApp.getMainFrameSafe());
    }
}
