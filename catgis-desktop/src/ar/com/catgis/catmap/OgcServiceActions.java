package ar.com.catgis.catmap;

import ar.com.catgis.NotificationManager;
import ar.com.catgis.PostgisBrowserDialog;
import ar.com.catgis.AddWfsDialog;
import ar.com.catgis.AddWmsDialog;

import java.awt.Window;

/**
 * Standalone OGC/PostGIS entry points: opens the existing CATGIS dialogs against
 * the CATMAP workspace. PostGIS remains optional and is only reachable on demand.
 */
public final class OgcServiceActions {

    private OgcServiceActions() {}

    public static void openWms(Window owner) {
        AddWmsDialog.open(owner);
    }

    public static void openWfs(Window owner) {
        AddWfsDialog.open(owner);
    }

    /** Returns true when the user added at least one PostGIS layer. */
    public static boolean openPostgis(Window owner) {
        return PostgisBrowserDialog.open(owner);
    }

    /** WMTS is not implemented; report it honestly instead of failing silently. */
    public static void reportWmtsUnavailable(Window owner) {
        NotificationManager.warn(owner, "Servicios OGC",
                OgcServiceSupport.unsupportedMessage(OgcServiceSupport.Protocol.WMTS));
    }
}
