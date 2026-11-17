package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DC-8 / T4 contract: the CATSERVER rewrite scripts must use the shared
 * destructive guard (stage/backup + swap) instead of silent deletions.
 */
class DestructiveScriptPolicyTest {

    private static final Path SCRIPTS = Path.of("..", "CATSERVER", "scripts");

    private static final List<String> GUARDED_SCRIPTS = List.of(
            "07_export_shape.ps1",
            "10_export_dxf_thematic_shapes.ps1",
            "12_clip_shapefile_to_mask.ps1",
            "14_import_cad_shapes_postgis.ps1",
            "16_build_shape_delivery_package.ps1");

    @Test
    void destructiveGuardHelperDefinesThePolicy() throws Exception {
        String helper = read(SCRIPTS.resolve("_common").resolve("DestructiveGuard.ps1"));
        assertTrue(helper.contains("function Move-CatserverToBackup"), "backup helper missing");
        assertTrue(helper.contains("function Restore-CatserverBackup"), "restore helper missing");
        assertTrue(helper.contains("function Assert-CatserverDestructiveAllowed"),
                "explicit destructive confirmation helper missing");
        assertTrue(helper.contains("function Clear-CatserverStaleBackups"),
                "stale-backup helper missing");
        assertTrue(helper.contains("-AllowDestructive"), "guard must require the switch");
    }

    @Test
    void everyRewriteScriptImportsTheGuardAndDeclaresTheSwitch() throws Exception {
        for (String scriptName : GUARDED_SCRIPTS) {
            String script = read(SCRIPTS.resolve(scriptName));
            assertTrue(script.contains("_common\\DestructiveGuard.ps1"),
                    scriptName + " must dot-source the destructive guard");
            assertTrue(script.contains("[switch]$AllowDestructive"),
                    scriptName + " must declare -AllowDestructive");
            boolean guarded = script.contains("Move-CatserverToBackup")
                    || script.contains("Clear-CatserverStaleBackups")
                    || script.contains("Restore-CatserverBackup");
            assertTrue(guarded, scriptName + " must use the stage/backup helpers");
        }
    }

    @Test
    void packageRebuildDoesNotSilentlyDeleteThePreviousDelivery() throws Exception {
        String script = read(SCRIPTS.resolve("16_build_shape_delivery_package.ps1"));
        assertFalse(script.contains(
                        "Get-ChildItem -LiteralPath $packageDir -Force | Remove-Item -Recurse -Force"),
                "the previous delivery must be preserved until the new one is built");
        assertTrue(script.contains("Move-CatserverToBackup"),
                "package rebuild must move the previous delivery to a backup");
    }

    @Test
    void cadImportUsesAStageTableAndTransactionalSwap() throws Exception {
        String script = read(SCRIPTS.resolve("14_import_cad_shapes_postgis.ps1"));
        assertTrue(script.contains("_stage"), "CAD import must write to a stage table");
        assertTrue(script.contains("ALTER TABLE $stageTable RENAME TO $targetLeaf"),
                "CAD import must swap the stage table transactionally");
        assertFalse(script.contains("Invoke-PsqlCommand \"DROP TABLE IF EXISTS $TargetTable;\""),
                "CAD import must not drop the live table before the replacement is ready");
    }

    private static String read(Path path) throws Exception {
        assertTrue(Files.isRegularFile(path), "missing script: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
