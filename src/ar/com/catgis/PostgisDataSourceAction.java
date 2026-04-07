package ar.com.catgis;

public final class PostgisDataSourceAction {

    private PostgisDataSourceAction() {
    }

    public static void openPostgisBrowser() {
        PostgisBrowserDialog.open(CatgisDesktopApp.getMainFrameSafe());
    }
}
