package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T30: the welcome page is reachable on startup and can be disabled by the user. */
class AppBrandingWelcomeContractTest {

    @Test
    void welcomePageIsEnabledByDefaultAndTogglable() {
        boolean original = AppBranding.isShowWelcomePage();
        try {
            AppBranding.setShowWelcomePage(true);
            assertTrue(AppBranding.isShowWelcomePage());
            AppBranding.setShowWelcomePage(false);
            assertFalse(AppBranding.isShowWelcomePage());
        } finally {
            AppBranding.setShowWelcomePage(original);
        }
    }
}
