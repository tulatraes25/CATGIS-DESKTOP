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
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class PostgisConnectionStore {

    private static final Preferences ROOT = Preferences.userNodeForPackage(PostgisConnectionStore.class).node("postgis");
    private static final Preferences PROFILES = ROOT.node("profiles");
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

    public static PostgisConnectionInfo loadProfileConnection(String profileId, PostgisConnectionInfo defaults) {
        if (profileId == null || profileId.isBlank()) {
            return defaults != null ? defaults.copy() : null;
        }

        Preferences node = PROFILES.node(profileId.trim());
        PostgisConnectionInfo base = defaults != null ? defaults.copy() : new PostgisConnectionInfo();
        base.setHost(node.get("host", base.getHost()));
        base.setPort(node.getInt("port", base.getPort()));
        base.setDatabase(node.get("database", base.getDatabase()));
        base.setSchema(node.get("schema", base.getSchema()));
        base.setUser(node.get("user", base.getUser()));
        base.setRememberPassword(node.getBoolean("remember", base.isRememberPassword()));
        String rememberedPassword = getStoredPassword(base);
        if (!rememberedPassword.isBlank()) {
            base.setPassword(rememberedPassword);
        }
        return base;
    }

    public static void saveProfileConnection(String profileId, PostgisConnectionInfo info) {
        if (profileId == null || profileId.isBlank() || info == null) {
            return;
        }
        Preferences node = PROFILES.node(profileId.trim());
        node.put("host", safe(info.getHost()));
        node.putInt("port", info.getPort());
        node.put("database", safe(info.getDatabase()));
        node.put("schema", safe(info.getSchema()));
        node.put("user", safe(info.getUser()));
        node.putBoolean("remember", info.isRememberPassword());
        rememberPassword(info);
    }

    public static void rememberPassword(PostgisConnectionInfo info) {
        if (info == null || info.buildFingerprint().isBlank()) {
            return;
        }
        String key = passwordKey(info);
        if (info.isRememberPassword() && info.getPassword() != null && !info.getPassword().isBlank()) {
            ROOT.put(key, obfuscate(info.getPassword()));
        } else {
            ROOT.remove(key);
        }
    }

    private static String getStoredPassword(PostgisConnectionInfo info) {
        if (info == null || info.buildFingerprint().isBlank()) {
            return "";
        }
        String stored = ROOT.get(passwordKey(info), "");
        if (stored.isBlank()) {
            return "";
        }
        String decrypted = PostgisCredentialVault.decrypt(stored);
        if (!decrypted.isBlank()) {
            return decrypted;
        }
        String legacy = migrateLegacySecret(stored);
        if (!legacy.isBlank()) {
            // v1 PBKDF2 / XOR -> v2 migration; never keep the weak format.
            info.setPassword(legacy);
            rememberPassword(info);
            return legacy;
        }
        CatgisLogger.warn("PostgisConnectionStore: credencial guardada ilegible o corrupta; "
                + "se solicitara la clave de nuevo", null);
        return "";
    }

    /**
     * Decrypts legacy v1/XOR values for migration only. Any invalid or
     * non-printable result fails closed with "" instead of throwing or teaching a
     * corrupt password to the user (#261).
     */
    static String migrateLegacySecret(String stored) {
        if (stored == null || stored.isBlank() || stored.startsWith(PostgisCredentialVault.PREFIX)) {
            return "";
        }
        String v1 = deobfuscate(stored);
        if (!v1.isBlank()) {
            return v1;
        }
        String xor = deobfuscateLegacy(stored);
        return looksLikePrintableSecret(xor) ? xor : "";
    }

    private static boolean looksLikePrintableSecret(String value) {
        if (value == null || value.isEmpty() || value.length() > 512) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch < 0x20 || ch == 0x7F) {
                return false;
            }
        }
        return true;
    }

    private static String obfuscate(String value) {
        // v2: random per-installation secret + AES-GCM (#261).
        return PostgisCredentialVault.encrypt(value);
    }

    private static byte[] deriveKey(byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        String passphrase = getMachinePassphrase();
        PBEKeySpec spec = new PBEKeySpec(
                passphrase.toCharArray(), salt, 100_000, 256);
        try {
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static String getMachinePassphrase() {
        return System.getProperty("user.name", "catgis")
                + "|" + System.getProperty("os.arch", "")
                + "|" + getWindowsMachineGuid()
                + "|CATGIS_V2_SALT_2026";
    }

    private static String getWindowsMachineGuid() {
        try {
            String[] cmd = {"powershell", "-NoProfile", "-Command",
                    "(Get-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Cryptography' -Name MachineGuid).MachineGuid"};
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            try {
                if (p.waitFor(3, TimeUnit.SECONDS) && p.exitValue() == 0) {
                    String output = new String(p.getInputStream().readAllBytes()).trim();
                    if (!output.isBlank()) return output;
                }
            } finally {
                p.destroy();
            }
        } catch (Exception ignored) {
            CatgisLogger.warn("PostgisConnectionStore: no se pudo obtener machine-id del sistema", null);
        }
        return "";
    }

    private static String deobfuscate(String encoded) {
        if (encoded == null || encoded.isBlank() || encoded.startsWith(PostgisCredentialVault.PREFIX)) {
            return "";
        }
        try {
            byte[] combined = java.util.Base64.getDecoder().decode(encoded);
            if (combined.length < 28) return "";
            byte[] salt = new byte[16];
            byte[] iv = new byte[12];
            byte[] ciphertext = new byte[combined.length - 28];
            System.arraycopy(combined, 0, salt, 0, 16);
            System.arraycopy(combined, 16, iv, 0, 12);
            System.arraycopy(combined, 28, ciphertext, 0, ciphertext.length);
            byte[] key = deriveKey(salt);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(ciphertext), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception invalidLegacy) {
            return "";
        }
    }

    private static String deobfuscateLegacy(String encoded) {
        if (encoded == null || encoded.isBlank() || encoded.startsWith(PostgisCredentialVault.PREFIX)) {
            return "";
        }
        try {
            byte[] bytes = java.util.Base64.getDecoder().decode(encoded);
            byte[] key = getMachineKey();
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] ^= key[i % key.length];
            }
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception invalidLegacy) {
            return "";
        }
    }

    /** Test seam: encrypts with the v2 vault without touching Preferences. */
    static String encryptForTest(String plaintext) {
        return PostgisCredentialVault.encrypt(plaintext);
    }

    /** Test seam: removes the stored password for a connection fingerprint. */
    static void clearStoredPasswordForTest(PostgisConnectionInfo info) {
        if (info != null && !info.buildFingerprint().isBlank()) {
            ROOT.remove(passwordKey(info));
        }
    }

    private static byte[] getMachineKey() {
        String seed = System.getProperty("user.name", "catgis")
                + System.getProperty("os.arch", "")
                + "CATGIS_FIXED_SALT_2026";
        try {
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256").digest(
                    seed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] key = new byte[16];
            System.arraycopy(hash, 0, key, 0, 16);
            return key;
        } catch (java.security.NoSuchAlgorithmException e) {
            return seed.getBytes(java.nio.charset.StandardCharsets.UTF_8);
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
            NotificationManager.warn(parent, null, "La clave no puede quedar vacia para conectar la capa PostGIS.");
            return null;
        }

        PostgisConnectionInfo resolved = baseInfo.copy();
        resolved.setPassword(password);
        resolved.setRememberPassword(rememberCheck.isSelected());
        rememberPassword(resolved);
        return resolved;
    }

    private static String passwordKey(PostgisConnectionInfo info) {
        String fingerprint = info.buildFingerprint();
        String legacyKey = "password." + fingerprint;
        // java.util.prefs rejects keys over 80 chars; hash only the too-long ones
        // so short, already-saved keys keep working.
        if (legacyKey.length() <= 80) {
            return legacyKey;
        }
        try {
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256").digest(
                    fingerprint.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return "password.h." + java.util.HexFormat.of().formatHex(hash);
        } catch (Exception impossible) {
            throw new IllegalStateException("SHA-256 no disponible", impossible);
        }
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
