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

    /** WMTS: opens the tile-template dialog (REST or KVP GetTile). */
    public static void openWmts(Window owner) {
        ar.com.catgis.catmap.ui.WmtsDialog.open(owner);
    }

    /** Report a malformed OGC request without pretending support. */
    public static void reportUnsupported(Window owner, OgcServiceSupport.Protocol protocol) {
        NotificationManager.warn(owner, "Servicios OGC",
                OgcServiceSupport.unsupportedMessage(protocol));
    }
}
