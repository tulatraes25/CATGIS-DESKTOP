package ar.com.catgis;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class AppErrorSupport {

    private AppErrorSupport() {
    }

    public static void logFailure(String context, Throwable ex) {
        CatgisLogger.warn(context, ex);
    }

    static String userMessage(Throwable ex) {
        if (ex == null) {
            return "";
        }
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            return ex.getMessage().trim();
        }
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getMessage().trim();
        }
        return ex.getClass().getSimpleName();
    }

    public static void showErrorDialog(Component parent, String title, String intro, Throwable ex) {
        JOptionPane.showMessageDialog(
                parent,
                buildDialogMessage(intro, ex),
                title != null && !title.isBlank() ? title : "CATGIS",
                JOptionPane.ERROR_MESSAGE
        );
    }

    static String buildDialogMessage(String intro, Throwable ex) {
        StringBuilder message = new StringBuilder();
        if (intro != null && !intro.isBlank()) {
            message.append(intro.trim());
        }
        String detail = userMessage(ex);
        if (!detail.isBlank()) {
            if (message.length() > 0) {
                message.append("\n\n");
            }
            message.append(detail);
        }
        if (message.length() == 0) {
            message.append("Ocurrio un error no especificado.");
        }
        return message.toString();
    }
}
