package ar.com.catgis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T4/#261: OS-backed key wrapping with fake storage, migration and fail-closed behaviour. */
class OsKeyProtectorContractTest {

    @TempDir
    Path directory;

    private Path keyFile;

    @BeforeEach
    void setUp() {
        System.setProperty(OsKeyProtector.PROPERTY, "off");
        PostgisCredentialVault.overrideProtectorForTest(null);
        keyFile = directory.resolve("postgis.key");
        PostgisCredentialVault.overrideKeyFileForTest(keyFile);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(OsKeyProtector.PROPERTY);
        PostgisCredentialVault.overrideProtectorForTest(null);
        PostgisCredentialVault.overrideKeyFileForTest(null);
    }

    @Test
    void protectedKeyFileIsFramedAndDecryptsStoredCredentials() throws Exception {
        PostgisCredentialVault.overrideProtectorForTest(new FakeProtector("fake-os"));
        String encrypted = PostgisCredentialVault.encrypt("clave-protegida");
        assertEquals("clave-protegida", PostgisCredentialVault.decrypt(encrypted));

        byte[] key = Files.readAllBytes(keyFile);
        assertTrue(key.length > 32, "OS-backed key file must carry the protected payload");
        assertArrayEquals(new byte[]{'C', 'G', 'O', 'S', '1'},
                Arrays.copyOf(key, 5), "versioned OS frame prefix");
    }

    @Test
    void legacyRawKeyMigratesOnceAndRemainsUsable() throws Exception {
        byte[] raw = new byte[32];
        Arrays.fill(raw, (byte) 7);
        Files.write(keyFile, raw);
        PostgisCredentialVault.overrideProtectorForTest(new FakeProtector("fake-os"));

        String encrypted = PostgisCredentialVault.encrypt("migrada");
        assertEquals("migrada", PostgisCredentialVault.decrypt(encrypted));
        assertTrue(Files.size(keyFile) > 32, "legacy raw key must be rewritten with OS protection");
    }

    @Test
    void tamperedProtectedKeyFailsClosedWithoutRawFallback() throws Exception {
        PostgisCredentialVault.overrideProtectorForTest(new FakeProtector("fake-os"));
        String encrypted = PostgisCredentialVault.encrypt("clave");
        byte[] key = Files.readAllBytes(keyFile);
        key[key.length - 1] ^= 0x55;
        Files.write(keyFile, key);

        assertEquals("", PostgisCredentialVault.decrypt(encrypted),
                "tampered OS-wrapped key must not decrypt nor degrade to local ciphertext");
    }

    @Test
    void differentProtectorRefusesProtectedKey() {
        PostgisCredentialVault.overrideProtectorForTest(new FakeProtector("fake-a"));
        String encrypted = PostgisCredentialVault.encrypt("clave");
        PostgisCredentialVault.overrideProtectorForTest(new FakeProtector("fake-b"));

        assertEquals("", PostgisCredentialVault.decrypt(encrypted),
                "a different OS backend must fail closed, never re-wrap silently");
    }

    @Test
    void selectedBackendRespectsOffAndExplicitConfiguration() {
        System.setProperty(OsKeyProtector.PROPERTY, "off");
        assertEquals(null, OsKeyProtector.selected());
        System.setProperty(OsKeyProtector.PROPERTY, "auto");
        OsKeyProtector.Protector auto = OsKeyProtector.selected();
        if (auto != null) {
            assertFalse(auto.name().isBlank());
        }
    }

    /** Deterministic test double; the real backends are OS/PowerShell/secret-tool calls. */
    private record FakeProtector(String name) implements OsKeyProtector.Protector {
        @Override
        public byte[] protect(byte[] secret) {
            byte[] out = new byte[secret.length + 1];
            out[0] = (byte) 0x5A;
            for (int i = 0; i < secret.length; i++) {
                out[i + 1] = (byte) (secret[i] ^ 0x3C);
            }
            return out;
        }

        @Override
        public byte[] unprotect(byte[] protectedSecret) {
            if (protectedSecret.length < 2 || protectedSecret[0] != (byte) 0x5A) {
                throw new IllegalArgumentException("fake protector rejected payload");
            }
            byte[] out = new byte[protectedSecret.length - 1];
            for (int i = 0; i < out.length; i++) {
                out[i] = (byte) (protectedSecret[i + 1] ^ 0x3C);
            }
            return out;
        }
    }
}
