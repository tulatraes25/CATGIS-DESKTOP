package ar.com.catgis;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * T4/#261: local credential vault for saved PostGIS passwords.
 *
 * <p>New secrets are encrypted with AES-256-GCM under a random, per-installation
 * 32-byte secret stored in {@code ~/.catgis/postgis.key} (override with
 * {@code -Dcatgis.postgis.keyfile=...}) instead of a predictable derivation from
 * user name, architecture and machine id. The key file is written atomically and
 * permissions are restricted on POSIX. Legacy formats (PBKDF2 v1 and XOR) remain
 * readable only to migrate them; any invalid/tampered value fails closed and the
 * user is asked for the password again instead of crashing.</p>
 */
final class PostgisCredentialVault {

    static final String PREFIX = "v2:";
    static final String KEYFILE_PROPERTY = "catgis.postgis.keyfile";
    private static final int KEY_BYTES = 32;
    private static final int SALT_BYTES = 16;
    private static final int IV_BYTES = 12;
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final String AAD = "CATGIS_POSTGIS_V2";

    private static volatile Path keyFileOverride;
    private static volatile byte[] cachedSecret;

    private PostgisCredentialVault() {}

    static Path keyFile() {
        String override = System.getProperty(KEYFILE_PROPERTY);
        if (override != null && !override.isBlank()) {
            return Path.of(override).toAbsolutePath();
        }
        return Path.of(System.getProperty("user.home"), ".catgis", "postgis.key");
    }

    static String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return "";
        }
        try {
            byte[] salt = random(SALT_BYTES);
            byte[] iv = random(IV_BYTES);
            byte[] key = deriveKey(installationSecret(), salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(128, iv));
            cipher.updateAAD(AAD.getBytes(StandardCharsets.UTF_8));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[salt.length + iv.length + ciphertext.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(iv, 0, combined, salt.length, iv.length);
            System.arraycopy(ciphertext, 0, combined, salt.length + iv.length,
                    ciphertext.length);
            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception failure) {
            CatgisLogger.error("PostgisCredentialVault: no se pudo cifrar la credencial", failure);
            throw new IllegalStateException("No se pudo proteger la credencial PostGIS.", failure);
        }
    }

    /** @return plaintext, or "" for anything not a valid v2 value (fail closed). */
    static String decrypt(String stored) {
        if (stored == null || !stored.startsWith(PREFIX)) {
            return "";
        }
        try {
            byte[] combined = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            if (combined.length < SALT_BYTES + IV_BYTES + 16) {
                return "";
            }
            byte[] salt = new byte[SALT_BYTES];
            byte[] iv = new byte[IV_BYTES];
            byte[] ciphertext = new byte[combined.length - SALT_BYTES - IV_BYTES];
            System.arraycopy(combined, 0, salt, 0, SALT_BYTES);
            System.arraycopy(combined, SALT_BYTES, iv, 0, IV_BYTES);
            System.arraycopy(combined, SALT_BYTES + IV_BYTES, ciphertext, 0, ciphertext.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(deriveKey(installationSecret(), salt), "AES"),
                    new GCMParameterSpec(128, iv));
            cipher.updateAAD(AAD.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception invalid) {
            CatgisLogger.warn("PostgisCredentialVault: credencial guardada invalida; "
                    + "se pedira la clave de nuevo", invalid);
            return "";
        }
    }

    /** Test seam: forces a specific key file for the current JVM. */
    static void overrideKeyFileForTest(Path path) {
        keyFileOverride = path;
        cachedSecret = null;
    }

    private static byte[] installationSecret() {
        Path keyFile = keyFileOverride != null ? keyFileOverride : keyFile();
        byte[] cached = cachedSecret;
        if (cached != null && keyFileOverride == null) {
            return cached;
        }
        try {
            if (Files.isRegularFile(keyFile)) {
                byte[] existing = Files.readAllBytes(keyFile);
                if (existing.length == KEY_BYTES) {
                    cachedSecret = existing;
                    return existing;
                }
            }
            byte[] secret = random(KEY_BYTES);
            writeKeyFile(keyFile, secret);
            cachedSecret = secret;
            return secret;
        } catch (Exception failure) {
            throw new IllegalStateException(
                    "No se pudo inicializar el almacen de credenciales PostGIS: " + keyFile,
                    failure);
        }
    }

    private static void writeKeyFile(Path keyFile, byte[] secret) throws IOException {
        Path parent = keyFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = keyFile.resolveSibling("." + keyFile.getFileName() + ".tmp");
        Files.write(temp, secret);
        try {
            Files.setPosixFilePermissions(temp,
                    PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException windows) {
            File file = temp.toFile();
            file.setReadable(false, false);
            file.setReadable(true, true);
            file.setWritable(false, false);
            file.setWritable(true, true);
        }
        Files.move(temp, keyFile, StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
    }

    private static byte[] deriveKey(byte[] secret, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(
                Base64.getEncoder().encodeToString(secret).toCharArray(),
                salt, PBKDF2_ITERATIONS, 256);
        try {
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static byte[] random(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
