package ar.com.catgis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;

/**
 * T4/#261: OS-backed protection for the PostGIS installation secret.
 *
 * <p>Windows uses user-scoped DPAPI through PowerShell; Linux uses Secret Service
 * through {@code secret-tool}. The secret never appears in argv, stdout is
 * bounded and failures fail closed. When no supported backend is available the
 * vault keeps its random local key file (documented residual); macOS Keychain is
 * still pending.</p>
 */
final class OsKeyProtector {

    static final String PROPERTY = "catgis.postgis.osprotector";

    interface Protector {
        String name();

        byte[] protect(byte[] secret) throws IOException;

        byte[] unprotect(byte[] protectedSecret) throws IOException;

        default void clear() {
            // OS stores that support removal override this.
        }
    }

    private OsKeyProtector() {}

    static Protector selected() {
        String forced = System.getProperty(PROPERTY, "auto").toLowerCase(Locale.ROOT);
        if ("off".equals(forced) || "none".equals(forced)) {
            return null;
        }
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if ("dpapi".equals(forced)) {
            return windowsDpapi(os);
        }
        if ("secret-tool".equals(forced)) {
            return linuxSecretTool();
        }
        if (os.startsWith("windows")) {
            return windowsDpapi(os);
        }
        if (os.contains("linux")) {
            return linuxSecretTool();
        }
        if (os.contains("mac")) {
            return macKeychain();
        }
        return null;
    }

    private static Protector windowsDpapi(String os) {
        if (!os.startsWith("windows")) {
            return null;
        }
        return new Protector() {
            @Override
            public String name() {
                return "windows-dpapi";
            }

            @Override
            public byte[] protect(byte[] secret) throws IOException {
                return runDpapi(secret, true);
            }

            @Override
            public byte[] unprotect(byte[] protectedSecret) throws IOException {
                return runDpapi(protectedSecret, false);
            }
        };
    }

    private static Protector linuxSecretTool() {
        if (!Files.isExecutable(Path.of("/usr/bin/secret-tool"))
                && !Files.isExecutable(Path.of("/usr/local/bin/secret-tool"))) {
            return null;
        }
        return new Protector() {
            private static final String SERVICE = "catgis";
            private static final String ATTRIBUTE = "postgis-vault-key";

            @Override
            public String name() {
                return "linux-secret-service";
            }

            @Override
            public byte[] protect(byte[] secret) throws IOException {
                String encoded = Base64.getEncoder().encodeToString(secret);
                runLlmTool(new String[]{"secret-tool", "store",
                        "--label=CATGIS PostGIS vault key",
                        "service", SERVICE, "key", ATTRIBUTE}, encoded);
                return secret;
            }

            @Override
            public byte[] unprotect(byte[] protectedSecret) throws IOException {
                String out = runLlmTool(new String[]{"secret-tool", "lookup",
                        "service", SERVICE, "key", ATTRIBUTE}, null);
                if (out == null || out.isBlank()) {
                    throw new IOException("Secret Service no devolvio el secreto PostGIS.");
                }
                return Base64.getDecoder().decode(out.trim());
            }
        };
    }

    private static byte[] runDpapi(byte[] payload, boolean protect) throws IOException {
        String operation = protect ? "Protect" : "Unprotect";
        String script = "$b=[Console]::In.ReadToEnd().Trim(); "
                + "$x=[Convert]::FromBase64String($b); "
                + "$p=[System.Security.Cryptography.ProtectedData]::" + operation
                + "($x,$null,'CurrentUser'); "
                + "[Console]::Out.Write([Convert]::ToBase64String($p))";
        String executable = Files.isExecutable(Path.of("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe"))
                ? "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe" : "powershell.exe";
        String output = runProcess(new String[]{executable, "-NoProfile", "-NonInteractive",
                "-ExecutionPolicy", "Bypass", "-Command", script},
                Base64.getEncoder().encodeToString(payload), 15_000L);
        if (output == null || output.isBlank()) {
            throw new IOException("DPAPI no devolvio una respuesta.");
        }
        return Base64.getDecoder().decode(output.trim());
    }

    private static String runLlmTool(String[] command, String stdinText) throws IOException {
        return runProcess(command, stdinText, 10_000L);
    }

    private static Protector macKeychain() {
        Path security = Path.of("/usr/bin/security");
        if (!Files.isExecutable(security)) {
            return null;
        }
        return new Protector() {
            private static final String ACCOUNT = "catgis";
            private static final String SERVICE = "catgis-postgis-vault";

            @Override
            public String name() {
                return "macos-keychain";
            }

            @Override
            public byte[] protect(byte[] secret) throws IOException {
                // security -i reads the command from stdin: the Base64 secret is
                // never in argv/ps and interactive-mode tokenization is safe for
                // the Base64 alphabet.
                String encoded = Base64.getEncoder().encodeToString(secret);
                String command = "add-generic-password -U -a " + ACCOUNT
                        + " -s " + SERVICE + " -w " + encoded + "\n";
                runProcess(new String[]{security.toString(), "-i"}, command, 10_000L);
                return secret;
            }

            @Override
            public byte[] unprotect(byte[] protectedSecret) throws IOException {
                String encoded = runProcess(new String[]{security.toString(),
                        "find-generic-password", "-w", "-a", ACCOUNT, "-s", SERVICE},
                        null, 10_000L);
                if (encoded == null || encoded.isBlank()) {
                    throw new IOException("Keychain no devolvio el secreto PostGIS.");
                }
                return Base64.getDecoder().decode(encoded.trim());
            }

            @Override
            public void clear() {
                try {
                    runProcess(new String[]{security.toString(), "-i"},
                            "delete-generic-password -a " + ACCOUNT + " -s " + SERVICE + "\n",
                            10_000L);
                } catch (IOException ignored) {
                    // Best-effort removal for tests/credential clearing.
                }
            }
        };
    }

    private static String runProcess(String[] command, String stdinText, long timeoutMillis)
            throws IOException {
        byte[] stdin = stdinText == null
                ? new byte[0] : stdinText.getBytes(StandardCharsets.UTF_8);
        byte[] stdout = ExternalToolService.runBounded(command, stdin, timeoutMillis);
        return new String(stdout, StandardCharsets.UTF_8);
    }
}
