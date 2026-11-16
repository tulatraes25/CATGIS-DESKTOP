package ar.com.catgis.catmap.ui;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Palette ranking is deterministic and task-oriented. */
class CommandPaletteIndexContractTest {

    private static CommandPaletteIndex index() {
        return new CommandPaletteIndex(List.of(
                new CommandPaletteIndex.Command("export-pdf", "Exportar PDF",
                        "Exportar el layout como PDF vectorial", List.of("pdf", "documento"),
                        () -> { }),
                new CommandPaletteIndex.Command("export-png", "Exportar PNG",
                        "Exportar la pagina como imagen PNG", List.of("png", "imagen"),
                        () -> { }),
                new CommandPaletteIndex.Command("page-setup", "Configurar pagina",
                        "Tamano de hoja, orientacion y margenes", List.of("pagina", "hoja"),
                        () -> { }),
                new CommandPaletteIndex.Command("templates", "Plantillas",
                        "Galeria de plantillas y QPT", List.of("plantilla", "qpt"),
                        () -> { }),
                new CommandPaletteIndex.Command("atlas", "Generar atlas",
                        "Serie de paginas por entidad", List.of("atlas", "serie"),
                        () -> { })));
    }

    @Test
    void emptyQueryListsEverything() {
        assertEquals(5, index().search("").size());
        assertEquals(5, index().search("   ").size());
    }

    @Test
    void labelMatchesRankFirst() {
        List<CommandPaletteIndex.Command> result = index().search("exportar pdf");
        assertEquals("Exportar PDF", result.get(0).label());
    }

    @Test
    void accentsAndCaseAreIgnored() {
        assertEquals("Configurar pagina", index().search("PÁGINA").get(0).label());
        assertEquals("Exportar PDF", index().search("PDF").get(0).label());
    }

    @Test
    void descriptionAndKeywordsMatchTasks() {
        assertEquals("Generar atlas", index().search("serie de paginas").get(0).label());
        assertEquals("Plantillas", index().search("qpt").get(0).label());
        assertEquals("Exportar PNG", index().search("imagen").get(0).label());
    }

    @Test
    void unknownQueryReturnsNothingAndActionRuns() {
        assertTrue(index().search("zzz-no-existe").isEmpty());
        AtomicBoolean ran = new AtomicBoolean();
        CommandPaletteIndex single = new CommandPaletteIndex(List.of(
                new CommandPaletteIndex.Command("x", "Hacer algo", "Accion de prueba",
                        List.of("probar"), () -> ran.set(true))));
        single.search("probar").get(0).action().run();
        assertTrue(ran.get());
    }

    @Test
    void commandsValidateTheirIdentity() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CommandPaletteIndex.Command("", "x", "", List.of(), () -> { }));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CommandPaletteIndex.Command("id", " ", "", List.of(), () -> { }));
        assertEquals(0, new CommandPaletteIndex(null).commands().size());
    }
}
