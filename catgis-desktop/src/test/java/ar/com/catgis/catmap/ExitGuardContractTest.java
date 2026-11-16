package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Safe exit decision matrix. */
class ExitGuardContractTest {

    @Test
    void cleanLayoutsDoNotAsk() {
        assertFalse(ExitGuard.needsConfirmation(false));
        assertTrue(ExitGuard.needsConfirmation(true));
    }

    @Test
    void dirtyLayoutsAskBeforeClosing() {
        // The dialog path needs a display; the decision helper is the contract.
        assertTrue(ExitGuard.needsConfirmation(true));
        org.junit.jupiter.api.Assertions.assertNotNull(ExitGuard.Choice.CANCEL);
        org.junit.jupiter.api.Assertions.assertNotNull(ExitGuard.Choice.SAVE_AND_EXIT);
        org.junit.jupiter.api.Assertions.assertNotNull(ExitGuard.Choice.EXIT_WITHOUT_SAVING);
    }
}