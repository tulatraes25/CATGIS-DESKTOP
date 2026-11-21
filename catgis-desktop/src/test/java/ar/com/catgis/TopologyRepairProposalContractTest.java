package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T5/#113: repair proposals are non-destructive and exported atomically. */
class TopologyRepairProposalContractTest {

    @TempDir
    Path directory;

    @Test
    void plannerMapsKnownIssuesToHumanReviewActions() {
        List<TopologyCheckResult> results = List.of(
                new TopologyCheckResult("Error", "Duplicados", "dos iguales", List.of("f1", "f2"), null, "EPSG:3857"),
                new TopologyCheckResult("Advertencia", "Superposiciones", "area", List.of("f3"), null, "EPSG:3857"),
                new TopologyCheckResult("Advertencia", "Cobertura AOI", "faltan 1.0", List.of(), null, "EPSG:3857"),
                new TopologyCheckResult("Error", "Geometria invalida", "self", List.of("f4"), null, "EPSG:3857"),
                new TopologyCheckResult("Advertencia", "Extremos colgantes", "gap", List.of("f5"), null, "EPSG:3857"));

        List<TopologyRepairPlanner.Proposal> proposals = TopologyRepairPlanner.plan(results);
        assertEquals(5, proposals.size());
        assertTrue(proposals.get(0).action().contains("Conservar"));
        assertEquals(List.of("f1", "f2"), proposals.get(0).featureIds());
        assertTrue(proposals.get(1).action().contains("Recortar"));
        assertTrue(proposals.get(2).action().contains("relleno"));
        assertTrue(proposals.get(3).action().contains("buffer(0)"));
        assertTrue(proposals.get(4).action().contains("Snap"));
    }

    @Test
    void planningDoesNotMutateResultsOrGeometries() throws Exception {
        org.locationtech.jts.geom.Geometry focus =
                new org.locationtech.jts.io.WKTReader().read("POLYGON((0 0,1 0,1 1,0 0))");
        TopologyCheckResult result = new TopologyCheckResult("Error", "Huecos", "detalle",
                List.of("f1"), focus, "EPSG:3857");
        String before = focus.toText();
        TopologyRepairPlanner.plan(List.of(result));
        assertEquals(before, focus.toText(), "planner must not edit source geometry");
        assertEquals("detalle", result.getDetail());
    }

    @Test
    void csvAndJsonAreDeterministicAndEscaped() throws Exception {
        var proposal = new TopologyRepairPlanner.Proposal("Error", "Duplicados, \"x\"",
                List.of("f1", "f2"), "Conservar; revisar", "linea\nnueva");
        String csv = TopologyRepairPlanner.toCsv(List.of(proposal));
        assertTrue(csv.startsWith("severidad,regla,entidades,accion,justificacion"));
        assertTrue(csv.contains("\"Duplicados, \"\"x\"\"\""));
        assertTrue(csv.contains("f1|f2"));

        String json = TopologyRepairPlanner.toJson(List.of(proposal));
        assertTrue(json.startsWith("{\"version\":1,\"proposals\":["));
        assertTrue(json.contains("\"featureIds\":[\"f1\",\"f2\"]"));
        assertTrue(json.contains("linea\\nnueva"));
        assertFalse(json.contains("\n\"severity\""), "raw newline must be escaped");
    }

    @Test
    void exportReplacesFileAtomicallyAndCleansStaging() throws Exception {
        Path target = directory.resolve("propuestas.json");
        Files.writeString(target, "OLD", StandardCharsets.UTF_8);
        var proposals = List.of(new TopologyRepairPlanner.Proposal(
                "Advertencia", "Huecos", List.of(), "Crear relleno", "detalle"));

        TopologyRepairPlanner.export(proposals, target.toFile(), true);

        String content = Files.readString(target);
        assertTrue(content.contains("\"proposals\""));
        assertTrue(content.contains("Crear relleno"));
        try (var files = Files.list(directory)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString()
                    .startsWith(".catgis-repair-")), "staging must be removed");
        }
    }

    @Test
    void exportFailsClosedForMissingDirectory() {
        assertThrowsForExport(directory.resolve("missing").resolve("x.json"));
    }

    private static void assertThrowsForExport(Path target) {
        org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class,
                () -> TopologyRepairPlanner.export(List.of(), target.toFile(), true));
    }
}
