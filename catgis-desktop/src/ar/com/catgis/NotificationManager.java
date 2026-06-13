package ar.com.catgis;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Central notification and dialog API for CATGIS.
 * <p>
 * Use {@link #toast(String)} / {@link #toastWarning(String)} for non-blocking
 * temporary messages. Use {@link #error(Component, String, String)} and
 * related methods for modal dialogs that require user acknowledgment.
 * <p>
 * All message strings support {@link I18n} keys via {@link I18n#t(String)}.
 * Callers are encouraged to use i18n keys rather than hardcoded strings.
 *
 * <h3>Examples replacing common JOptionPane patterns:</h3>
 * <pre>{@code
 * // Before: JOptionPane.showMessageDialog(parent, "Archivo no existe", "Error", JOptionPane.ERROR_MESSAGE);
 * // After:  NotificationManager.error(parent, "Error", "Archivo no existe");
 *
 * // Before: JOptionPane.showMessageDialog(this, I18n.t("Selecciona una capa"), I18n.t("Aviso"), JOptionPane.WARNING_MESSAGE);
 * // After:  NotificationManager.warn(this, I18n.t("Aviso"), I18n.t("Selecciona una capa"));
 *
 * // Before: String name = JOptionPane.showInputDialog(parent, "Nombre:", "Nueva capa", JOptionPane.PLAIN_MESSAGE);
 * // After:  String name = NotificationDialog.showInputText(parent, "Nueva capa", "Nombre:", null);
 *
 * // Before: int r = JOptionPane.showConfirmDialog(parent, "Seguro?", "Confirmar", JOptionPane.YES_NO_OPTION);
 * // After:  boolean ok = NotificationDialog.showConfirm(parent, "Confirmar", "Seguro?");
 * }</pre>
 */
public final class NotificationManager {

    private NotificationManager() {}

    // ---- Toast (non-blocking) ----

    /**
     * Show a non-blocking info toast at bottom-right of the main frame.
     */
    public static void toast(String message) {
        toast(message, ToastNotification.Severity.INFO);
    }

    /**
     * Show a non-blocking warning toast.
     */
    public static void toastWarning(String message) {
        toast(message, ToastNotification.Severity.WARNING);
    }

    /**
     * Show a non-blocking success toast.
     */
    public static void toastSuccess(String message) {
        toast(message, ToastNotification.Severity.SUCCESS);
    }

    private static void toast(String message, ToastNotification.Severity severity) {
        Window owner = resolveMainFrame();
        if (owner instanceof Frame frame) {
            SwingUtilities.invokeLater(() ->
                    ToastNotification.show(frame, message, severity));
        }
    }

    // ---- Modal dialogs ----

    /** Modal error dialog. */
    public static void error(Component parent, String title, String message) {
        NotificationDialog.showError(parent, title, message);
    }

    /** Modal warning dialog. */
    public static void warn(Component parent, String title, String message) {
        NotificationDialog.showWarning(parent, title, message);
    }

    /** Modal info dialog. */
    public static void info(Component parent, String title, String message) {
        NotificationDialog.showInfo(parent, title, message);
    }

    /** Modal confirm (Yes/No). Returns true if Yes was chosen. */
    public static boolean confirm(Component parent, String title, String message) {
        return NotificationDialog.showConfirm(parent, title, message);
    }

    /** Modal confirm (Yes/No/Cancel). Returns JOptionPane.YES_OPTION, NO_OPTION, or CANCEL_OPTION. */
    public static int confirmCancel(Component parent, String title, String message) {
        return NotificationDialog.showConfirmCancel(parent, title, message);
    }

    /** Modal text input. Returns user input or null if cancelled. */
    public static String inputText(Component parent, String title, String message,
                                    String defaultValue) {
        return NotificationDialog.showInputText(parent, title, message, defaultValue);
    }

    /** Modal choice input (combo box). Returns selected choice or null if cancelled. */
    public static String inputChoice(Component parent, String title, String message,
                                      List<String> choices, String defaultChoice) {
        return NotificationDialog.showInputChoice(parent, title, message, choices, defaultChoice);
    }

    // ---- Internal ----

    private static Window resolveMainFrame() {
        return CatgisDesktopApp.getMainFrameSafe();
    }
}
