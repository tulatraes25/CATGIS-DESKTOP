package ar.com.catgis.plugins;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PluginManager opt-in security.
 * Verifies that plugins are disabled by default and can be enabled
 * via system property.
 */
class PluginManagerTest {

    @BeforeEach
    @AfterEach
    void clearProperty() {
        System.clearProperty("catgis.plugins.enabled");
    }

    @Test
    void pluginsAreDisabledByDefault() {
        assertFalse(PluginManager.isEnabled(),
                "plugins should be disabled by default (opt-in required)");
    }

    @Test
    void pluginsCanBeEnabledViaSystemProperty() {
        System.setProperty("catgis.plugins.enabled", "true");
        assertTrue(PluginManager.isEnabled());
    }

    @Test
    void pluginsCanBeEnabledViaTrueMixedCase() {
        System.setProperty("catgis.plugins.enabled", "True");
        assertTrue(PluginManager.isEnabled());
    }

    @Test
    void pluginsRemainDisabledWithFalseValue() {
        System.setProperty("catgis.plugins.enabled", "false");
        assertFalse(PluginManager.isEnabled());
    }

    @Test
    void pluginsRemainDisabledWithInvalidValue() {
        System.setProperty("catgis.plugins.enabled", "yes");
        assertFalse(PluginManager.isEnabled());
    }

    @Test
    void initializeWithPluginsDisabledDoesNotCrash() {
        // By default plugins are disabled — initialize() should be a no-op
        PluginManager.initialize();
        // No exception = pass
    }

    @Test
    void scanWithPluginsDisabledIsNoOp() {
        PluginManager.scanForChanges();
        // No exception = pass
    }
}
