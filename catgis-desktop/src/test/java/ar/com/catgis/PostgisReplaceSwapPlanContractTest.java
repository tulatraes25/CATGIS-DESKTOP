package ar.com.catgis;

import ar.com.catgis.PostgisTableWriteMode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T4/#156: transactional replace plan and identifier safety (no database needed). */
class PostgisReplaceSwapPlanContractTest {

    @Test
    void planDropsTargetOnlyAfterStagingExistsInsideOneTransaction() {
        List<String> statements = PostgisReplaceSwap.plan("public", "parcelas", "parcelas_catgis_stage_abc");
        assertEquals(3, statements.size());
        assertTrue(statements.get(0).contains("DROP TABLE IF EXISTS \"public\".\"parcelas\""));
        assertTrue(statements.get(1).contains("ALTER TABLE \"public\".\"parcelas_catgis_stage_abc\""));
        assertTrue(statements.get(1).contains("RENAME TO \"parcelas\""));
        assertTrue(statements.get(2).contains("count(*)")
                        && statements.get(2).contains("\"parcelas\""),
                "the plan must verify the published target inside the transaction");
    }

    @Test
    void stagingNameIsUniqueValidAndBounded() {
        String first = PostgisReplaceSwap.stageTableName("parcelas_urbanas");
        String second = PostgisReplaceSwap.stageTableName("parcelas_urbanas");
        assertTrue(first.startsWith("parcelas_urbanas" + PostgisReplaceSwap.STAGE_MARKER));
        assertTrue(first.length() <= 63);
        assertTrue(first.matches("^[A-Za-z_][A-Za-z0-9_]*$"));
        assertTrue(!first.equals(second), "staging names must be unique per attempt");

        String longName = "a".repeat(60);
        String bounded = PostgisReplaceSwap.stageTableName(longName);
        assertTrue(bounded.length() <= 63);
    }

    @Test
    void unsafeIdentifiersAreRejectedBeforeAnySql() {
        assertThrows(IllegalArgumentException.class,
                () -> PostgisReplaceSwap.plan("public", "tabla; DROP TABLE x", "stage"));
        assertThrows(IllegalArgumentException.class,
                () -> PostgisReplaceSwap.plan("public;--", "tabla", "stage"));
        assertThrows(IllegalArgumentException.class,
                () -> PostgisReplaceSwap.stageTableName("con espacio"));
        assertDoesNotThrow(() -> PostgisReplaceSwap.plan("public", "tabla_1", "stage_1"));
    }

    @Test
    void replaceModeStaysHiddenFromTheExportDialogButIsSupportedByService() {
        assertFalse(PostgisTableWriteMode.REPLACE_TABLE.isVisibleInExportDialog(),
                "REPLACE stays out of the export dialog until UI certification");
    }
}
