package ar.com.catgis;

import java.awt.Component;
import java.awt.Window;
import java.io.File;

public final class GeoPackageDataSourceAction {

    private GeoPackageDataSourceAction() {
    }

    public static boolean openGeoPackageDataSource() {
        return GeoPackageDataSourceDialog.open(CatgisDesktopApp.getMainFrameSafe(), null);
    }

    public static boolean openGeoPackageDataSource(File initialFile, Component parent) {
        Window owner = parent instanceof Window ? (Window) parent : CatgisDesktopApp.getMainFrameSafe();
        return GeoPackageDataSourceDialog.open(owner, initialFile);
    }
}
