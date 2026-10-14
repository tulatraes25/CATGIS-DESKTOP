package ar.com.catgis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Platform-aware WhiteboxTools executable resolver.
 * No shell, 'where', 'which', arbitrary PATH lookup, binary download or EDT I/O.
 * An explicit CATGIS_WHITEBOX_BIN is authoritative: invalid overrides fail
 * closed rather than silently invoking an unrelated executable.
 */
final class WhiteboxExecutableResolver {
    private static final Set<String> OTHER_APPROVED = Set.of(
            "ogr2ogr", "grass", "saga_cmd", "gdalinfo",
            "gdal_translate", "gdalwarp");

    private WhiteboxExecutableResolver() {}

    static String resolve() {
        String os = System.getProperty("os.name", "");
        Path local = Path.of(System.getProperty("user.home"), ".catgis", "tools");
        List<Path> approved = candidateDirectories(os, local,
                System.getenv("ProgramFiles"), System.getenv("ProgramFiles(x86)"));
        return resolve(os, System.getenv("CATGIS_WHITEBOX_BIN"), approved);
    }

    static boolean isOtherApprovedToolPresent(String toolName) {
        if (toolName == null || !OTHER_APPROVED.contains(toolName)) {
            return false;
        }
        String os = System.getProperty("os.name", "");
        Path local = Path.of(System.getProperty("user.home"), ".catgis", "tools");
        List<Path> approved = candidateDirectories(os, local,
                System.getenv("ProgramFiles"), System.getenv("ProgramFiles(x86)"));
        String name = isWindows(os) ? toolName + ".exe" : toolName;
        return locateInApprovedDirectories(List.of(name), approved) != null;
    }

    /** Deterministic policy; no file system or executable probing. */
    static List<Path> candidateDirectories(String osName, Path userTools,
                                           String programFiles,
                                           String programFilesX86) {
        LinkedHashSet<Path> directories = new LinkedHashSet<>();
        addAbsolute(directories, userTools);
        if (userTools != null && userTools.isAbsolute()) {
            addAbsolute(directories, userTools.resolve("WBT"));
            addAbsolute(directories, userTools.resolve("WhiteboxTools"));
            addAbsolute(directories, userTools.resolve("WhiteboxTools").resolve("WBT"));
        }
        if (isWindows(osName)) {
            addProgramFiles(directories, programFiles);
            addProgramFiles(directories, programFilesX86);
            // System install fallback; only meaningful on a Windows filesystem.
            addAbsolute(directories, Path.of("C:\\Program Files\\WhiteboxTools"));
            addAbsolute(directories, Path.of("C:\\Program Files\\WhiteboxTools\\WBT"));
        } else if (isMac(osName)) {
            addAbsolute(directories, Path.of("/opt/homebrew/bin"));
            addAbsolute(directories, Path.of("/usr/local/bin"));
            addAbsolute(directories, Path.of("/opt/local/bin"));
            addAbsolute(directories, Path.of("/usr/bin"));
        } else {
            addAbsolute(directories, Path.of("/usr/bin"));
            addAbsolute(directories, Path.of("/usr/local/bin"));
            addAbsolute(directories, Path.of("/opt/bin"));
            addAbsolute(directories, Path.of("/opt/whitebox/bin"));
        }
        return List.copyOf(directories);
    }

    private static void addProgramFiles(Set<Path> dirs, String base) {
        if (base == null || base.isBlank()) {
            return;
        }
        try {
            Path root = Path.of(base);
            if (root.isAbsolute()) {
                addAbsolute(dirs, root.resolve("WhiteboxTools"));
                addAbsolute(dirs, root.resolve("WhiteboxTools").resolve("WBT"));
            }
        } catch (RuntimeException ignored) {
            // Invalid OS environment variable must not broaden discovery.
        }
    }

    private static void addAbsolute(Set<Path> dirs, Path value) {
        if (value != null && value.isAbsolute()) {
            dirs.add(value.normalize());
        }
    }

    /** Pure allowlisted native names. Windows never picks extensionless binaries. */
    static List<String> nativeNames(String osName) {
        return isWindows(osName)
                ? List.of("whitebox_tools.exe", "wbt.exe")
                : List.of("whitebox_tools", "wbt");
    }

    static String resolve(String osName, String configured,
                          List<Path> approvedDirectories) {
        if (configured != null && !configured.isBlank()) {
            final Path override;
            try {
                override = Path.of(configured.trim());
            } catch (RuntimeException malformedPath) {
                return null;
            }
            if (!override.isAbsolute() || !override.normalize().equals(override)) {
                return null;
            }
            if (Files.isDirectory(override, LinkOption.NOFOLLOW_LINKS)) {
                return locateInApprovedDirectories(nativeNames(osName), List.of(override));
            }
            // Reject arbitrary executable names, even if user configured a path.
            if (!nativeNames(osName).contains(override.getFileName().toString())) {
                return null;
            }
            return verifiedExecutable(override);
        }
        return locateInApprovedDirectories(nativeNames(osName), approvedDirectories);
    }

    private static String locateInApprovedDirectories(List<String> names,
                                                       List<Path> directories) {
        if (directories == null) {
            return null;
        }
        for (Path directory : directories) {
            if (directory == null || !directory.isAbsolute()
                    || !directory.normalize().equals(directory)) {
                continue;
            }
            for (String name : names) {
                String executable = verifiedExecutable(directory.resolve(name));
                if (executable != null) {
                    return executable;
                }
            }
        }
        return null;
    }

    /**
     * Homebrew / MacPorts symlinks are valid if they resolve to a real,
     * executable regular file. Execution always uses the absolute real path.
     */
    private static String verifiedExecutable(Path candidate) {
        try {
            if (candidate == null || !candidate.isAbsolute()
                    || !Files.isRegularFile(candidate)
                    || !Files.isExecutable(candidate)) {
                return null;
            }
            Path real = candidate.toRealPath();
            if (!real.isAbsolute()
                    || !Files.isRegularFile(real, LinkOption.NOFOLLOW_LINKS)
                    || !Files.isExecutable(real)) {
                return null;
            }
            return real.toString();
        } catch (IOException | SecurityException malformed) {
            return null;
        }
    }

    private static boolean isWindows(String os) {
        return os != null && os.toLowerCase(Locale.ROOT).contains("windows");
    }

    private static boolean isMac(String os) {
        String lower = os == null ? "" : os.toLowerCase(Locale.ROOT);
        return lower.contains("mac") || lower.contains("darwin");
    }
}
