package ar.com.catgis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * Pre-commit resource limits for legacy .catgis text projects.
 * This protects parsing only; layer reconstruction must still be staged.
 */
final class ProjectOpenReadBudget {
    static final long MAX_PROJECT_BYTES = 64L * 1024L * 1024L;
    static final int MAX_LINE_CHARS = 1024 * 1024;
    static final int MAX_LINES = 250_000;
    static final int MAX_LAYERS = 25_000;
    static final int MAX_GROUPS = 10_000;
    static final int MAX_LAYOUT_RECORDS = 50_000;

    private final int maxLineChars;
    private final int maxLines;
    private final int maxLayers;
    private final int maxGroups;
    private final int maxLayoutRecords;
    private int lineCount;
    private int layerCount;
    private int groupCount;
    private int layoutRecordCount;

    ProjectOpenReadBudget() {
        this(MAX_LINE_CHARS, MAX_LINES, MAX_LAYERS, MAX_GROUPS, MAX_LAYOUT_RECORDS);
    }

    ProjectOpenReadBudget(int maxLineChars, int maxLines, int maxLayers,
                          int maxGroups, int maxLayoutRecords) {
        if (maxLineChars <= 0 || maxLines <= 0 || maxLayers <= 0
                || maxGroups <= 0 || maxLayoutRecords <= 0) {
            throw new IllegalArgumentException("Invalid project parser limits");
        }
        this.maxLineChars = maxLineChars;
        this.maxLines = maxLines;
        this.maxLayers = maxLayers;
        this.maxGroups = maxGroups;
        this.maxLayoutRecords = maxLayoutRecords;
    }

    static BufferedReader openChecked(File file) throws IOException {
        verifyFile(file, MAX_PROJECT_BYTES);
        return Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
    }

    static void verifyFile(File file, long maxBytes) throws IOException {
        if (file == null || maxBytes <= 0) {
            throw new IOException("Archivo o límite del proyecto inválido.");
        }
        Path path = file.toPath().toAbsolutePath().normalize();
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("El proyecto debe ser un archivo regular, no un enlace.");
        }
        long length = Files.size(path);
        if (length > maxBytes) {
            throw new IOException("El proyecto supera el tamaño máximo permitido.");
        }
    }

    void checkLine(String line) throws IOException {
        if (line == null) {
            return;
        }
        if (++lineCount > maxLines) {
            throw new IOException("El proyecto supera la cantidad de registros permitida.");
        }
        if (line.length() > maxLineChars) {
            throw new IOException("El proyecto contiene un registro demasiado largo.");
        }
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == 0 || c < 0x20 && c != '\t' || c == 0x7f) {
                throw new IOException("El proyecto contiene caracteres de control inválidos.");
            }
        }
        if (line.startsWith("[")) {
            if (++layerCount > maxLayers) {
                throw new IOException("El proyecto supera la cantidad de capas permitida.");
            }
        } else if (line.startsWith("PROJECT_LAYER_GROUP|")) {
            if (++groupCount > maxGroups) {
                throw new IOException("El proyecto supera la cantidad de grupos permitida.");
            }
        } else if (line.startsWith("PROJECT_LAYOUT_ITEM|")
                || line.startsWith("PROJECT_LEGEND_ITEM|")) {
            if (++layoutRecordCount > maxLayoutRecords) {
                throw new IOException("El proyecto supera la cantidad de elementos CATMAP permitida.");
            }
        }
    }
}
