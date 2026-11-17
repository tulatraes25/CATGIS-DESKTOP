package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** REQ-BETA-1/2: CLI parsing and the headless self-test. */
class BetaCliContractTest {

    @Test
    void parseRecognizesBetaFlagsInAnyOrder() {
        BetaCli.Options options = BetaCli.parse(new String[]{
                "--project", "/tmp/x.catgis", "--VERSION", "--catmap-standalone",
                "--self-test", "--catmap", "layout.catmap"});
        assertTrue(options.version());
        assertTrue(options.selfTest());
        assertTrue(options.catmapStandalone());
        assertEquals(List.of("--project", "/tmp/x.catgis", "--catmap", "layout.catmap"),
                List.of(options.remaining()));
    }

    @Test
    void parseIgnoresNullAndEmptyArguments() {
        BetaCli.Options options = BetaCli.parse(new String[]{null, "", "  ", "--version"});
        assertTrue(options.version());
        assertFalse(options.selfTest());
        assertEquals(2, options.remaining().length, "null args are dropped; blanks are preserved");
        assertEquals(0, BetaCli.parse(null).remaining().length);
    }

    @Test
    void versionLinesExposeProductAndBetaChannel() {
        List<String> desktop = BetaCli.versionLines(false);
        assertTrue(desktop.get(0).contains("CATGIS Desktop"));
        assertTrue(desktop.get(0).contains(AppVersion.VERSION));
        assertTrue(desktop.get(0).contains("beta"));
        List<String> catmap = BetaCli.versionLines(true);
        assertTrue(catmap.get(0).contains("CATMAP"));
        assertTrue(catmap.get(0).contains(AppVersion.VERSION));
    }

    @Test
    void selfTestPassesHeadlessAndCoversCapabilities() {
        List<String> classes = BetaCli.capabilityClasses();
        assertTrue(classes.size() >= 10, "beta self-test must cover the capabilities");
        assertEquals(0, BetaCli.runSelfTest(),
                "self-test must pass with every capability class on the classpath");
    }
}
