package ar.com.catgis;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Unified modal dialog factory using FlatLaf-compatible JDialog.
 * Replaces JOptionPane.showMessageDialog / showInputDialog / showConfirmDialog
 * with a consistent visual style.
 */
public final class NotificationDialog {

    private NotificationDialog() {}

    private static final int DIALOG_WIDTH = 520;

    // ---- Error / Info / Warning ----

    public static void showError(Component parent, String title, String message) {
        showMessage(parent, title, message, JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String title, String message) {
        showMessage(parent, title, message, JOptionPane.WARNING_MESSAGE);
    }

    public static void showInfo(Component parent, String title, String message) {
        showMessage(parent, title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showMessage(Component parent, String title, String message, int type) {
        JOptionPane optionPane = new JOptionPane(
                wrap(message), type, JOptionPane.DEFAULT_OPTION);
        JDialog dialog = optionPane.createDialog(parent, title != null ? title : "CATGIS");
        dialog.setMinimumSize(new Dimension(DIALOG_WIDTH, dialog.getHeight()));
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        centerOnParent(dialog, parent);
        dialog.setVisible(true);
    }

    // ---- Confirm ----

    public static boolean showConfirm(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(
                parent, wrap(message), title != null ? title : I18n.t("Confirmar"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public static int showConfirmCancel(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(
                parent, wrap(message), title != null ? title : I18n.t("Confirmar"),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    // ---- Input ----

    public static String showInputText(Component parent, String title, String message,
                                        String defaultValue) {
        String result = (String) JOptionPane.showInputDialog(
                parent, wrap(message), title, JOptionPane.PLAIN_MESSAGE,
                null, null, defaultValue);
        return result;
    }

    public static String showInputChoice(Component parent, String title, String message,
                                          List<String> choices, String defaultChoice) {
        String result = (String) JOptionPane.showInputDialog(
                parent, wrap(message), title, JOptionPane.PLAIN_MESSAGE,
                null, choices.toArray(), defaultChoice);
        return result;
    }

    // ---- Helpers ----

    private static String wrap(String message) {
        if (message == null) return "";
        return "<html><body style='width:360px;padding:8px 0'>"
                + message.replace("\n", "<br>") + "</body></html>";
    }

    private static void centerOnParent(Window dialog, Component parent) {
        if (parent == null) {
            dialog.setLocationRelativeTo(null);
            return;
        }
        Window parentWindow = SwingUtilities.windowForComponent(parent);
        if (parentWindow == null) {
            dialog.setLocationRelativeTo(null);
            return;
        }
        Point parentLoc = parentWindow.getLocationOnScreen();
        Dimension parentSize = parentWindow.getSize();
        Dimension dialogSize = dialog.getSize();
        int x = parentLoc.x + (parentSize.width - dialogSize.width) / 2;
        int y = parentLoc.y + (parentSize.height - dialogSize.height) / 2;
        dialog.setLocation(Math.max(0, x), Math.max(0, y));
    }
}
