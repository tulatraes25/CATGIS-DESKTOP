package ar.com.catgis;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ManualIconCatalogExporter {

    private static final int CANVAS_SIZE = 32;

    private ManualIconCatalogExporter() {
    }

    public static void main(String[] args) throws Exception {
        Path outputRoot = args != null && args.length > 0
                ? Paths.get(args[0]).toAbsolutePath()
                : Paths.get("C:\\CATGIS\\catgis-desktop\\packaging\\manual\\generated").toAbsolutePath();
        Path iconsDir = outputRoot.resolve("manual-icons");
        Files.createDirectories(iconsDir);

        List<Entry> entries = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            exportToolbar(entries, iconsDir, "Barra principal", new MainToolBar());
            exportToolbar(entries, iconsDir, "Topografía", new TopographyToolbar());
            exportToolbar(entries, iconsDir, "Cartografía", new CartographyToolbar());
            exportToolbar(entries, iconsDir, "Conexiones online", new OnlineConnectionsToolbar());
            exportToolbar(entries, iconsDir, "Edición vectorial", new FloatingVectorEditToolbar());
            try {
                exportMenuBar(entries, iconsDir, "Menú principal", new MainMenuBar());
            } catch (Throwable ignored) {
                // Algunos entornos sin escritorio completo no permiten construir
                // todos los menús. El catálogo principal de barras sigue siendo válido.
            }
        });

        entries.sort(Comparator
                .comparing(Entry::group, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
        writeCsv(outputRoot.resolve("manual-icon-catalog.csv"), entries);
        writeMarkdown(outputRoot.resolve("manual-icon-catalog.md"), entries);
    }

    private static void exportToolbar(List<Entry> entries, Path iconsDir, String group, Container container) {
        Set<String> seenKeys = new LinkedHashSet<>();
        walkComponents(container, component -> {
            if (!(component instanceof AbstractButton button)) {
                return;
            }
            Icon icon = button.getIcon();
            if (icon == null) {
                return;
            }
            String name = normalizeLabel(prefer(button.getToolTipText(), button.getText(), component.getName()));
            if (name.isBlank()) {
                return;
            }
            String key = group + "|" + name;
            if (!seenKeys.add(key)) {
                return;
            }
            String fileName = sanitize(group + "-" + name) + ".png";
            Path target = iconsDir.resolve(fileName);
            try {
                saveIcon(icon, target);
            } catch (IOException ex) {
                throw new RuntimeException("No se pudo exportar el icono " + fileName, ex);
            }
            entries.add(new Entry(
                    group,
                    name,
                    normalizeLabel(button.getToolTipText()),
                    normalizeLabel(button.getText()),
                    target.getFileName().toString(),
                    button.getClass().getSimpleName()
            ));
        });
    }

    private static void exportMenuBar(List<Entry> entries, Path iconsDir, String group, JMenuBar menuBar) {
        Set<String> seenKeys = new LinkedHashSet<>();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu == null) {
                continue;
            }
            walkMenu(entries, iconsDir, group, menu.getText(), menu, seenKeys);
        }
    }

    private static void walkMenu(List<Entry> entries, Path iconsDir, String group, String menuGroup, JMenuItem item, Set<String> seenKeys) {
        if (item.getIcon() != null) {
            String name = normalizeLabel(prefer(item.getText(), item.getToolTipText(), item.getName()));
            if (!name.isBlank()) {
                String subGroup = group + " / " + normalizeLabel(menuGroup);
                String key = subGroup + "|" + name;
                if (seenKeys.add(key)) {
                    String fileName = sanitize(subGroup + "-" + name) + ".png";
                    Path target = iconsDir.resolve(fileName);
                    try {
                        saveIcon(item.getIcon(), target);
                    } catch (IOException ex) {
                        throw new RuntimeException("No se pudo exportar el icono " + fileName, ex);
                    }
                    entries.add(new Entry(
                            subGroup,
                            name,
                            normalizeLabel(item.getToolTipText()),
                            normalizeLabel(item.getText()),
                            target.getFileName().toString(),
                            item.getClass().getSimpleName()
                    ));
                }
            }
        }
        if (item instanceof JMenu menu) {
            for (int i = 0; i < menu.getItemCount(); i++) {
                JMenuItem child = menu.getItem(i);
                if (child != null) {
                    walkMenu(entries, iconsDir, group, menu.getText(), child, seenKeys);
                }
            }
        }
    }

    private static void walkComponents(Container container, java.util.function.Consumer<Component> consumer) {
        for (Component component : container.getComponents()) {
            consumer.accept(component);
            if (component instanceof Container child) {
                walkComponents(child, consumer);
            }
        }
    }

    private static void saveIcon(Icon icon, Path target) throws IOException {
        BufferedImage image = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int x = Math.max(0, (CANVAS_SIZE - icon.getIconWidth()) / 2);
        int y = Math.max(0, (CANVAS_SIZE - icon.getIconHeight()) / 2);
        icon.paintIcon(null, g, x, y);
        g.dispose();
        ImageIO.write(image, "png", target.toFile());
    }

    private static void writeCsv(Path csvPath, List<Entry> entries) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("grupo,nombre,tooltip,texto_visible,archivo_png,tipo_componente");
        for (Entry entry : entries) {
            lines.add(csv(entry.group()) + "," + csv(entry.name()) + "," + csv(entry.tooltip()) + "," + csv(entry.visibleText()) + "," + csv(entry.fileName()) + "," + csv(entry.componentType()));
        }
        Files.write(csvPath, lines, StandardCharsets.UTF_8);
    }

    private static void writeMarkdown(Path markdownPath, List<Entry> entries) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# Catálogo visual de iconos CATGIS\n\n");
        String currentGroup = null;
        for (Entry entry : entries) {
            if (!entry.group().equals(currentGroup)) {
                currentGroup = entry.group();
                sb.append("## ").append(currentGroup).append("\n\n");
                sb.append("| Icono | Nombre | Tooltip / función rápida | Archivo |\n");
                sb.append("|---|---|---|---|\n");
            }
            sb.append("| ![](")
                    .append("manual-icons/")
                    .append(entry.fileName())
                    .append(") | ")
                    .append(entry.name())
                    .append(" | ")
                    .append(entry.tooltip().isBlank() ? "Sin tooltip documentado" : entry.tooltip())
                    .append(" | `")
                    .append(entry.fileName())
                    .append("` |\n");
        }
        Files.writeString(markdownPath, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private static String prefer(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static String normalizeLabel(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\s+", " ").trim();
    }

    private static String sanitize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^a-zA-Z0-9._-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private record Entry(String group, String name, String tooltip, String visibleText, String fileName, String componentType) {
    }
}
