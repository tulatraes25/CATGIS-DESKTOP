package ar.com.catgis.catmap;

import ar.com.catgis.NotificationManager;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Safe exit for CATMAP: when the layout has unsaved changes the user decides
 * whether to save, discard or cancel. Pure decision helpers are testable.
 */
public final class ExitGuard {

    public enum Choice { SAVE_AND_EXIT, EXIT_WITHOUT_SAVING, CANCEL }

    private ExitGuard() {}

    /** Dirty documents always ask before closing. */
    public static boolean needsConfirmation(boolean layoutDirty) {
        return layoutDirty;
    }

    public static Choice confirm(Component parent, boolean layoutDirty) {
        if (!needsConfirmation(layoutDirty)) {
            return Choice.EXIT_WITHOUT_SAVING;
        }
        int choice = NotificationManager.confirmCancel(parent,
                "Salir de CATMAP",
                "Hay cambios sin guardar en el layout.\n"
                        + "Queres guardarlos antes de salir?\n\n"
                        + "Si: guardar y salir\nNo: salir sin guardar");
        if (choice == JOptionPane.YES_OPTION) {
            return Choice.SAVE_AND_EXIT;
        }
        if (choice == JOptionPane.NO_OPTION) {
            return Choice.EXIT_WITHOUT_SAVING;
        }
        return Choice.CANCEL;
    }
}