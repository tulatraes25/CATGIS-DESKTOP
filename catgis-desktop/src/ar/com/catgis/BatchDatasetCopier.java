package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * T4/#114: copy a complete vector/raster dataset family instead of a single file.
 *
 * <p>A batch copy of {@code survey.shp} must also preserve {@code .shx/.dbf/.prj/
 * .cpg/.qix} and supported raster sidecars; otherwise the destination looks
 * copied but is not a usable dataset. Shapefile members {@code .shx} and
 * {@code .dbf} are required and a missing one fails closed before any byte is
 * written. The destination family uses the staged output basename so the
 * transactional processor can publish/rollback the whole family as one unit.</p>
 */
public final class BatchDatasetCopier {

    private static final String[] SHAPEFILE_MEMBERS = {
            ".shx", ".dbf", ".prj", ".cpg", ".qix", ".sbn", ".sbx",
            ".shp.xml", ".fbn", ".fbx", ".ain", ".aih", ".atx", ".ixs", ".mxs"
    };
    private static final String[] SHAPEFILE_REQUIRED = {".shx", ".dbf"};
    private static final String[] RASTER_SIDECARS = {
            ".aux.xml", ".tfw", ".tifw", ".wld", ".ovr", ".aux", ".prj"
    };

    private BatchDatasetCopier() {}

    public static void copy(File input, File output) throws IOException {
        if (input == null || !input.isFile()) {
            throw new IOException("Entrada batch inexistente o no regular: " + input);
        }
        if (output == null) {
            throw new IOException("Salida batch requerida.");
        }
        Path source = input.toPath();
        Path target = output.toPath();
        String lower = input.getName().toLowerCase(Locale.ROOT);
        List<Path> members;
        if (lower.endsWith(".shp")) {
            members = shapefileMembers(source);
            for (String required : SHAPEFILE_REQUIRED) {
                Path sibling = siblingCaseInsensitive(source, required);
                if (sibling == null) {
                    throw new IOException("Shapefile incompleto: falta " + required
                            + " junto a " + input.getName());
                }
            }
        } else if (isRaster(lower)) {
            members = new ArrayList<>();
            members.add(source);
            for (String sidecar : RASTER_SIDECARS) {
                Path sibling = siblingCaseInsensitive(source, sidecar);
                if (sibling != null) {
                    members.add(sibling);
                }
            }
        } else {
            members = List.of(source);
        }
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        String targetBase = stripExtension(target.getFileName().toString());
        String sourceName = source.getFileName().toString();
        for (Path member : members) {
            String memberName = member.getFileName().toString();
            String destinationName;
            if (memberName.toLowerCase(Locale.ROOT)
                    .startsWith(sourceName.toLowerCase(Locale.ROOT))
                    && memberName.length() > sourceName.length()) {
                // Full-name sidecars such as dem.tif.aux.xml keep the primary name.
                destinationName = target.getFileName()
                        + memberName.substring(sourceName.length());
            } else {
                // Extension-family members such as .shx/.dbf/.tfw replace the extension.
                destinationName = targetBase + extensionOf(memberName);
            }
            Path destination = target.resolveSibling(destinationName);
            Files.copy(member, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static List<Path> shapefileMembers(Path source) throws IOException {
        List<Path> members = new ArrayList<>();
        members.add(source);
        for (String extension : SHAPEFILE_MEMBERS) {
            Path sibling = siblingCaseInsensitive(source, extension);
            if (sibling != null) {
                members.add(sibling);
            }
        }
        return members;
    }

    private static boolean isRaster(String lowerName) {
        for (String extension : new String[]{".tif", ".tiff", ".img", ".asc", ".vrt"}) {
            if (lowerName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private static Path siblingCaseInsensitive(Path source, String suffix) throws IOException {
        String base = stripExtension(source.getFileName().toString());
        String full = source.getFileName().toString();
        Path directory = source.getParent();
        if (directory == null) {
            return null;
        }
        String shortName = (base + suffix).toLowerCase(Locale.ROOT);
        String fullName = (full + suffix).toLowerCase(Locale.ROOT);
        try (var entries = Files.list(directory)) {
            return entries
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.equals(shortName) || name.equals(fullName);
                    })
                    .findFirst()
                    .orElse(null);
        }
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private static String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : "";
    }
}
