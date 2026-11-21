package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SPEC/Kiro consistency guard: the audit ledger must not reintroduce stale
 * "in progress" statuses, owner decisions must stay recorded, and the final
 * certification state must remain explicitly NOT_CERTIFIED for A0.7/F3.
 */
class KiroAuditSpecConsistencyContractTest {

    private static final String[] STALE = {
            "sigue abierto", "MITIGADO", "PR en curso", "Pendientes:",
            "T1 still needs decision"
    };

    @Test
    void auditTasksLedgerHasNoStaleContradictions() throws Exception {
        Path root = repoRoot();
        Path tasks = root.resolve(".kiro/specs/catgis-audit-20261117/tasks.md");
        assertTrue(Files.isRegularFile(tasks), "missing audit tasks ledger: " + tasks);
        String content = Files.readString(tasks);
        assertTrue(content.contains("1718 tests"), "ledger must record the verified suite size");
        assertTrue(content.contains("A0.7/F3 físico"), "physical gate must stay explicit");
        assertTrue(content.contains("UNSUPPORTED"), "DC-5 decision must stay recorded");
        assertTrue(content.contains("congelada por decisión del owner"),
                "frozen #112 must stay recorded");
        int lineNo = 0;
        for (String line : content.split("\\R")) {
            lineNo++;
            String trimmed = line.trim();
            if (trimmed.startsWith(">")) {
                continue; // explanatory blockquote about the obsolete wording
            }
            for (String stale : STALE) {
                assertFalse(line.contains(stale),
                        "stale SPEC/Kiro status at tasks.md:" + lineNo + " -> " + trimmed);
            }
        }
    }

    @Test
    void ownerDecisionIsReflectedInCatserverSpecAndManifest() throws Exception {
        Path root = repoRoot();
        String catserver = Files.readString(root.resolve(
                ".kiro/specs/catserver-decision-backlog/tasks.md"));
        assertTrue(catserver.contains("DECIDED"), "DC-5 decision status must be explicit");
        assertTrue(catserver.contains("UNSUPPORTED"), "DC-5 must remain UNSUPPORTED");
        assertFalse(catserver.contains("T1 still needs decision"),
                "the old pending status must not return");

        String manifest = Files.readString(root.resolve("docs/CAPABILITY_MANIFEST.md"));
        assertTrue(manifest.contains("DC-5 productor de máscara ejido | UNSUPPORTED"),
                "manifest must keep DC-5 UNSUPPORTED");
        assertTrue(manifest.contains("A0.7/F3") || manifest.contains("NOT_CERTIFIED"),
                "manifest must not silently claim GUI certification");
    }

    @Test
    void everyKiroSpecFileIsReadableUtf8AndNonEmpty() throws Exception {
        Path root = repoRoot();
        Path specs = root.resolve(".kiro/specs");
        assertTrue(Files.isDirectory(specs));
        try (var paths = Files.walk(specs)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                if (!path.toString().endsWith(".md")) {
                    continue;
                }
                String text = Files.readString(path);
                assertFalse(text.isBlank(), "empty SPEC/Kiro file: " + path);
            }
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
        throw new AssertionError("No se encontro el checkout CATGIS con .kiro desde " + cwd);
    }
}
