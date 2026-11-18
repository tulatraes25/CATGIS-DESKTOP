package ar.com.catgis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T4/#261: v2 credential vault, fail-closed legacy migration and safe errors. */
class PostgisCredentialsVaultContractTest {

    @TempDir
    Path directory;

    private Path keyFile;

    @BeforeEach
    void setUp() {
        keyFile = directory.resolve("postgis.key");
        PostgisCredentialVault.overrideKeyFileForTest(keyFile);
    }

    @AfterEach
    void tearDown() {
        PostgisCredentialVault.overrideKeyFileForTest(null);
    }

    @Test
    void v2RoundtripUsesRandomInstallationSecret() throws Exception {
        String secret = "clave-Ñandú-| % 中文";
        String encrypted = PostgisCredentialVault.encrypt(secret);

        assertTrue(encrypted.startsWith(PostgisCredentialVault.PREFIX));
        assertEquals(secret, PostgisCredentialVault.decrypt(encrypted));
        assertTrue(Files.isRegularFile(keyFile), "key file must be created");
        assertEquals(32, Files.size(keyFile), "installation secret is 32 bytes");
        assertNotEquals(secret, encrypted);
    }

    @Test
    void tamperedOrForeignPayloadFailsClosed() {
        String encrypted = PostgisCredentialVault.encrypt("clave");
        String tampered = encrypted.substring(0, encrypted.length() - 2)
                + (encrypted.endsWith("AA") ? "BB" : "AA");

        assertEquals("", PostgisCredentialVault.decrypt(tampered));
        assertEquals("", PostgisCredentialVault.decrypt("no-es-v2"));
        assertEquals("", PostgisCredentialVault.decrypt(null));
    }

    @Test
    void anotherKeyFileCannotDecryptPreviousCredentials() {
        String encrypted = PostgisCredentialVault.encrypt("clave");
        PostgisCredentialVault.overrideKeyFileForTest(directory.resolve("otra.key"));

        assertEquals("", PostgisCredentialVault.decrypt(encrypted),
                "a different installation secret must not decrypt the credential");
    }

    @Test
    void legacyXorMigrationDecryptsRealValuesOnly() {
        String legacy = legacyXor("secreto-legado");
        assertEquals("secreto-legado", PostgisConnectionStore.migrateLegacySecret(legacy));

        String garbage = Base64.getEncoder().encodeToString(
                new byte[]{(byte) 0x01, (byte) 0xFF, (byte) 0x02, (byte) 0xFE, (byte) 0x03});
        assertEquals("", PostgisConnectionStore.migrateLegacySecret(garbage),
                "unreadable legacy values must fail closed, not migrate garbage");
        assertEquals("", PostgisConnectionStore.migrateLegacySecret(""));
        assertEquals("", PostgisConnectionStore.migrateLegacySecret(
                PostgisCredentialVault.encrypt("v2-value")));
    }

    @Test
    void v2EncryptionIsNotPredictableAcrossValues() {
        String first = PostgisCredentialVault.encrypt("misma-clave");
        String second = PostgisCredentialVault.encrypt("misma-clave");
        assertNotEquals(first, second, "random salt/IV must make ciphertexts differ");
        assertEquals("misma-clave", PostgisCredentialVault.decrypt(first));
        assertEquals("misma-clave", PostgisCredentialVault.decrypt(second));
    }

    @Test
    void preferencesRoundtripAndRemovalUseV2() {
        PostgisConnectionInfo info = new PostgisConnectionInfo();
        info.setHost("localhost");
        info.setDatabase("catastro");
        info.setSchema("public");
        info.setUser("vault_test_" + UUID.randomUUID());
        info.setRememberPassword(true);
        info.setPassword("clave-" + UUID.randomUUID());
        try {
            PostgisConnectionStore.rememberPassword(info);
            PostgisConnectionInfo loaded = PostgisConnectionStore.applyStoredPassword(
                    copyWithoutPassword(info));
            assertEquals(info.getPassword(), loaded.getPassword());
        } finally {
            PostgisConnectionStore.clearStoredPasswordForTest(info);
        }
        PostgisConnectionInfo afterRemoval = PostgisConnectionStore.applyStoredPassword(
                copyWithoutPassword(info));
        assertTrue(afterRemoval.getPassword() == null || afterRemoval.getPassword().isBlank());
    }

    @Test
    void encryptedValueNeverContainsPlaintext() {
        String prompt = "clave-super-secreta";
        String encrypted = PostgisCredentialVault.encrypt(prompt);
        assertFalse(encrypted.contains(prompt));
        assertThrows(IllegalArgumentException.class,
                () -> PostgisCredentialVault.overrideKeyFileForTest(
                        Path.of("\u0000invalid")));
    }

    private static PostgisConnectionInfo copyWithoutPassword(PostgisConnectionInfo info) {
        PostgisConnectionInfo copy = info.copy();
        copy.setPassword("");
        return copy;
    }

    private static String legacyXor(String value) {
        try {
            String seed = System.getProperty("user.name", "catgis")
                    + System.getProperty("os.arch", "")
                    + "CATGIS_FIXED_SALT_2026";
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            byte[] key = new byte[16];
            System.arraycopy(hash, 0, key, 0, 16);
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] ^= key[i % key.length];
            }
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
