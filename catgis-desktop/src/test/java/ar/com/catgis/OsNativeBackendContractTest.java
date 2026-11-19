package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T4/#261: native OS-backed protection evidence on every desktop platform.
 *
 * <p>Windows exercises user DPAPI, macOS exercises Keychain, and Linux exercises
 * Secret Service when available. Linux without {@code secret-tool} prints an
 * explicit unsupported marker instead of silently claiming protection. No test
 * assumption/skip is used: every platform executes this contract.</p>
 */
class OsNativeBackendContractTest {

    @Test
    void nativeBackendProtectsFakeSecretOrReportsUnsupportedTransparently() throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        OsKeyProtector.Protector protector = OsKeyProtector.selected();
        if (protector == null) {
            // Only Linux may legitimately lack a supported store (no secret-tool).
            assertTrue(os.contains("linux"),
                    "Windows and macOS must always expose an OS-backed store");
            System.out.println("OS_NATIVE_BACKEND_UNAVAILABLE linux-secret-service");
            return;
        }
        if (os.contains("linux") && !protector.name().contains("secret")) {
            System.out.println("OS_NATIVE_BACKEND_UNAVAILABLE linux-secret-service");
            return;
        }
        byte[] secret = new byte[32];
        Arrays.fill(secret, (byte) 0x42);
        try {
            byte[] protectedSecret = protector.protect(secret);
            assertNotNull(protectedSecret);
            assertTrue(protectedSecret.length > 0);
            assertArrayEquals(secret, protector.unprotect(protectedSecret),
                    "native backend must roundtrip the fake installation secret");
            System.out.println("OS_NATIVE_BACKEND_OK backend=" + protector.name()
                    + " os=" + os);
        } finally {
            protector.clear();
        }
    }
}
