package ar.com.catgis;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Absolute, allowlisted GDAL executable discovery for Windows, Linux and macOS.
 * Never executes a program found by a wildcard PATH search or via a shell.
 */
final class GdalSupport {

    private static final Set<String> ALLOWED_TOOLS = Set.of(
            "gdal_translate", "gdalwarp", "gdalinfo");

    private GdalSupport() {
    }

    /**
     * Resolve the platform's native executable to an absolute filesystem path.
     * The explicit CATGIS_GDAL_BIN overrides standard paths; no implicit PATH
     * probing occurs, so user files cannot impersonate a GDAL binary.
     */
    static String resolve(String executableName) {
        String osName = System.getProperty("os.name", "");
        List<File> search = candidateDirectories(
                osName,
                System.getenv("CATGIS_GDAL_BIN"),
                System.getenv("CATGIS_OSGEO4W"),
                System.getenv("CATGIS_OSGEO4W64"));
        return resolveFromDirectories(executableName, osName, search);
    }

    /** Pure, deterministic OS policy; no I/O, executable discovery or PATH. */
    static List<File> candidateDirectories(String osName, String overrideBin,
                                            String osgeo4wBin, String osgeo4w64Bin) {
        String os = osName == null ? "" : osName.toLowerCase(Locale.ROOT);
        LinkedHashSet<File> dirs = new LinkedHashSet<>();
        if (overrideBin != null && !overrideBin.isBlank()) {
            File override = new File(overrideBin.trim());
            if (!override.isAbsolute()) {
                throw new GdalNotAvailableException(
                        "CATGIS_GDAL_BIN debe ser una carpeta absoluta, no una ruta relativa.");
            }
            dirs.add(override);
        }
        if (os.contains("windows")) {
            dirs.add(configuredOrDefault(osgeo4wBin, "C:\\OSGeo4W\\bin"));
            dirs.add(configuredOrDefault(osgeo4w64Bin, "C:\\OSGeo4W64\\bin"));
        } else if (os.contains("mac") || os.contains("darwin")) {
            // Default Apple Silicon Homebrew, Intel Homebrew and MacPorts bins.
            dirs.add(new File("/opt/homebrew/bin"));
            dirs.add(new File("/usr/local/bin"));
            dirs.add(new File("/opt/local/bin"));
            dirs.add(new File("/usr/bin"));
        } else {
            // Common Linux package-manager paths. Never search the current
            // directory or potentially attacker-controlled PATH entries.
            dirs.add(new File("/usr/bin"));
            dirs.add(new File("/usr/local/bin"));
            dirs.add(new File("/opt/bin"));
        }
        return List.copyOf(dirs);
    }

    private static File configuredOrDefault(String configured, String fallback) {
        if (configured != null && !configured.isBlank()) {
            File directory = new File(configured.trim());
            if (!directory.isAbsolute()) {
                throw new GdalNotAvailableException(
                        "La carpeta GDAL/OSGeo4W configurada debe ser absoluta.");
            }
            return directory;
        }
        return new File(fallback);
    }

    /**
     * Only three reviewed GDAL commands are valid. Normalize .exe input from
     * existing Java callers on Unix and add .exe on Windows as appropriate.
     */
    static String nativeExecutableName(String requestedName, String osName) {
        if (requestedName == null) {
            throw new IllegalArgumentException("El comando GDAL no puede ser null.");
        }
        String name = requestedName.trim().toLowerCase(Locale.ROOT);
        if (name.endsWith(".exe")) {
            name = name.substring(0, name.length() - 4);
        }
        if (!ALLOWED_TOOLS.contains(name)) {
            throw new IllegalArgumentException("Comando GDAL no permitido: " + requestedName);
        }
        String os = osName == null ? "" : osName.toLowerCase(Locale.ROOT);
        return os.contains("windows") ? name + ".exe" : name;
    }

    /**
     * Validate a *real executable file* on an explicit approved directory;
     * symlinks such as Homebrew's /opt/homebrew/bin/gdalinfo are supported.
     */
    static String resolveFromDirectories(String executableName, String osName,
                                         List<File> directories) {
        String nativeName = nativeExecutableName(executableName, osName);
        if (directories == null) {
            throw new IllegalArgumentException("GDAL directories cannot be null.");
        }
        for (File directory : directories) {
            if (directory == null || !directory.isAbsolute()) {
                continue;
            }
            File candidate = new File(directory, nativeName);
            if (candidate.isFile() && candidate.canExecute()) {
                return candidate.getAbsolutePath();
            }
        }
        String os = osName == null ? "" : osName.toLowerCase(Locale.ROOT);
        String advice = os.contains("windows")
                ? "Instala OSGeo4W o configura CATGIS_GDAL_BIN."
                : "Instala GDAL con tu gestor de paquetes o configura CATGIS_GDAL_BIN.";
        throw new GdalNotAvailableException(
                "No se encontro un ejecutable " + nativeName
                        + " en directorios GDAL aprobados. " + advice);
    }

    static final class GdalNotAvailableException extends RuntimeException {
        GdalNotAvailableException(String message) {
            super(message);
        }
    }
}
