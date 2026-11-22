package ar.com.catgis;

import org.junit.jupiter.api.Test;

import javax.swing.Icon;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Menu-icon coverage guard: every actionable menu item must carry an icon, and
 * the shared text-to-icon resolver must cover the main action families.
 */
class MenuIconCoverageContractTest {

    @Test
    void everyMenuSourceEitherInjectsIconsInlineOrAppliesTheIconPass() throws Exception {
        Path root = repoRoot().resolve("catgis-desktop/src/ar/com/catgis");
        Pattern constructor = Pattern.compile(
                "new (JMenuItem|JCheckBoxMenuItem|JRadioButtonMenuItem)\\(");
        try (var files = Files.walk(root)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                if (file.getFileName().toString().equals("MenuIconSupport.java")) {
                    continue;
                }
                List<String> lines = Files.readAllLines(file);
                boolean hasConstructor = lines.stream().anyMatch(line -> constructor.matcher(line).find());
                if (!hasConstructor) {
                    continue;
                }
                boolean hasPostIcon = lines.stream().anyMatch(line ->
                        line.contains("MenuIconSupport.apply")
                                || line.contains(".setIcon(")
                                || line.contains("MenuIconSupport.item")
                                || line.contains("MenuIconSupport.checkItem"));
                for (String line : lines) {
                    if (!constructor.matcher(line).find()) {
                        continue;
                    }
                    if (line.contains("MenuIconSupport") || line.contains("AppIcons")
                            || line.contains(", icon")) {
                        continue;
                    }
                    assertTrue(hasPostIcon, "menu item sin icono en "
                            + root.relativize(file) + ": " + line.trim());
                }
            }
        }
    }

    @Test
    void mainMenuHasNoNullIconsAndEveryMenuHeaderHasAnIcon() throws Exception {
        String source = Files.readString(repoRoot()
                .resolve("catgis-desktop/src/ar/com/catgis/MainMenuBar.java"));
        assertFalse(Pattern.compile("createItem\\([^,]+,\\s*null\\s*\\)")
                        .matcher(source).find(),
                "MainMenuBar tiene items sin icono");
        long menus = source.lines().filter(line -> line.contains("new JMenu(")).count();
        long icons = source.lines().filter(line -> line.contains(".setIcon(")).count();
        assertTrue(icons >= menus,
                "faltan iconos de submenu: menus=" + menus + " setIcon=" + icons);
    }

    @Test
    void resolverCoversMainActionFamiliesAtToolbarSize() {
        String[] labels = {
                "Cancelar dibujo", "Terminar medición", "Guardar proyecto",
                "Abrir proyecto", "Copiar coordenada", "Eliminar pin",
                "Medir distancia", "Calcular área", "Agregar pin", "Zoom a capa",
                "Editar vector", "Agregar capa", "Propiedades", "Exportar KML",
                "Importar tabla", "Definir CRS", "Seleccionar", "Cuencas",
                "Análisis de red", "Calculadora raster"
        };
        for (String label : labels) {
            Icon icon = MenuIconSupport.iconFor(label);
            assertNotNull(icon, label);
            assertEquals(24, icon.getIconWidth(), label);
            assertEquals(24, icon.getIconHeight(), label);
        }
    }

    private static Path repoRoot() {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        if (Files.isDirectory(cwd.resolve(".kiro"))) {
            return cwd;
        }
        Path parent = cwd.getParent();
        if (parent != null && Files.isDirectory(parent.resolve(".kiro"))) {
            return parent;
        }
        throw new AssertionError("No se encontro el checkout CATGIS desde " + cwd);
    }
}
