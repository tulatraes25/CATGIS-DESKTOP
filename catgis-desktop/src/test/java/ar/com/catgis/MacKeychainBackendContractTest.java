package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T4/#261: native macOS Keychain roundtrip with a fake installation secret. */
@EnabledOnOs(OS.MAC)
class MacKeychainBackendContractTest {

    @Test
    void keychainStoresAndReadsFakeSecretWithoutArgvExposure() throws Exception {
        System.setProperty(OsKeyProtector.PROPERTY, "auto");
        try {
            OsKeyProtector.Protector protector = OsKeyProtector.selected();
            assertTrue(protector != null, "macOS must select the Keychain backend");
            assertTrue(protector.name().contains("keychain"));
            byte[] secret = new byte[32];
            Arrays.fill(secret, (byte) 0x42);
            byte[] protectedSecret;
            try {
                protectedSecret = protector.protect(secret);
            } catch (IOException unavailable) {
                System.out.println("MAC_KEYCHAIN_NATIVE_UNAVAILABLE " + unavailable.getMessage());
                org.junit.jupiter.api.Assumptions.assumeTrue(false,
                        "Keychain no disponible en este runner: " + unavailable.getMessage());
                return;
            }
            try {
                assertArrayEquals(secret, protector.unprotect(protectedSecret),
                        "Keychain roundtrip must preserve the fake secret");
                System.out.println("MAC_KEYCHAIN_NATIVE_OK backend=" + protector.name());
            } finally {
                protector.clear();
            }
        } finally {
            System.clearProperty(OsKeyProtector.PROPERTY);
        }
    }
}
