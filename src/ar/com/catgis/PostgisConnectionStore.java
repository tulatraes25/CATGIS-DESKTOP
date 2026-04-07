package ar.com.catgis;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.prefs.Preferences;

public final class PostgisConnectionStore {

    private static final Preferences ROOT = Preferences.userNodeForPackage(PostgisConnectionStore.class).node("postgis");
    private static final String KEY_LAST_HOST = "last.host";
    private static final String KEY_LAST_PORT = "last.port";
    private static final String KEY_LAST_DATABASE = "last.database";
    private static final String KEY_LAST_SCHEMA = "last.schema";
    private static final String KEY_LAST_USER = "last.user";
    private static final String KEY_LAST_REMEMBER = "last.remember";

    private PostgisConnectionStore() {
    }

    public static PostgisConnectionInfo loadLastConnection() {
        String host = ROOT.get(KEY_LAST_HOST, "").trim();
        String database = ROOT.get(KEY_LAST_DATABASE, "").trim();
        String user = ROOT.get(KEY_LAST_USER, "").trim();
        if (host.isBlank() && database.isBlank() && user.isBlank()) {
            return null;
        }

        PostgisConnectionInfo info = new PostgisConnectionInfo();
        info.setHost(host);
        info.setPort(ROOT.getInt(KEY_LAST_PORT, 5432));
        info.setDatabase(database);
        info.setSchema(ROOT.get(KEY_LAST_SCHEMA, "public"));
        info.setUser(user);
        info.setRememberPassword(ROOT.getBoolean(KEY_LAST_REMEMBER, true));
        String rememberedPassword = getStoredPassword(info);
        if (!rememberedPassword.isBlank()) {
            info.setPassword(rememberedPassword);
        }
        return info;
    }

    public static void saveLastConnection(PostgisConnectionInfo info) {
        if (info == null) {
            return;
        }
        ROOT.put(KEY_LAST_HOST, safe(info.getHost()));
        ROOT.putInt(KEY_LAST_PORT, info.getPort());
        ROOT.put(KEY_LAST_DATABASE, safe(info.getDatabase()));
        ROOT.put(KEY_LAST_SCHEMA, safe(info.getSchema()));
        ROOT.put(KEY_LAST_USER, safe(info.getUser()));
        ROOT.putBoolean(KEY_LAST_REMEMBER, info.isRememberPassword());
        rememberPassword(info);
    }

    public static void rememberPassword(PostgisConnectionInfo info) {
        if (info == null || info.buildFingerprint().isBlank()) {
            return;
        }
        String key = passwordKey(info);
        if (info.isRememberPassword() && info.getPassword() != null && !info.getPassword().isBlank()) {
            ROOT.put(key, info.getPassword());
        } else {
            ROOT.remove(key);
        }
    }

    public static PostgisConnectionInfo applyStoredPassword(PostgisConnectionInfo info) {
        if (info == null) {
            return null;
        }
        PostgisConnectionInfo resolved = info.copy();
        if (resolved.getPassword() == null || resolved.getPassword().isBlank()) {
            resolved.setPassword(getStoredPassword(resolved));
        }
        return resolved;
    }

    public static PostgisConnectionInfo promptForPassword(Component parent, PostgisConnectionInfo baseInfo, String reasonLabel) {
        if (baseInfo == null) {
            return null;
        }

        JPasswordField passwordField = new JPasswordField(18);
        JCheckBox rememberCheck = new JCheckBox("Guardar clave en esta computadora", true);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(reasonLabel != null && !reasonLabel.isBlank() ? reasonLabel : "Ingresá la clave de PostGIS."), gc);

        gc.gridy = 1;
        panel.add(new JLabel("Clave para " + baseInfo.buildDisplayLabel() + ":"), gc);

        gc.gridy = 2;
        panel.add(passwordField, gc);

        gc.gridy = 3;
        panel.add(rememberCheck, gc);

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                "Clave PostGIS",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String password = new String(passwordField.getPassword());
        if (password.isBlank()) {
            JOptionPane.showMessageDialog(parent, "La clave no puede quedar vacia para conectar la capa PostGIS.");
            return null;
        }

        PostgisConnectionInfo resolved = baseInfo.copy();
        resolved.setPassword(password);
        resolved.setRememberPassword(rememberCheck.isSelected());
        rememberPassword(resolved);
        return resolved;
    }

    private static String getStoredPassword(PostgisConnectionInfo info) {
        if (info == null || info.buildFingerprint().isBlank()) {
            return "";
        }
        return ROOT.get(passwordKey(info), "");
    }

    private static String passwordKey(PostgisConnectionInfo info) {
        return "password." + info.buildFingerprint();
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
