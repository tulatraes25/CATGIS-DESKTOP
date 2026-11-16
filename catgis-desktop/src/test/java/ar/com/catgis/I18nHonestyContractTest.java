package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T29: no partial automatic translation; only complete locales are selectable. */
class I18nHonestyContractTest {

    @Test
    void spanishReturnsTheSourceText() {
        I18n.setLanguage(I18n.Language.SPANISH);
        assertEquals("Exportar PDF", I18n.t("Exportar PDF"));
        assertEquals("", I18n.t(null));
    }

    @Test
    void missingEnglishEntryNeverInventsWords() {
        I18n.setLanguage(I18n.Language.ENGLISH);
        try {
            // The former heuristic translated word by word; the honest contract
            // keeps the reviewed source text until a translation exists.
            assertEquals("dato disponible", I18n.t("dato disponible"));
            assertEquals("Presentacion / Institucional",
                    I18n.t("Presentacion / Institucional"));
        } finally {
            I18n.setLanguage(I18n.Language.SPANISH);
        }
    }

    @Test
    void onlyCompleteLocalesAreSelectable() {
        assertTrue(I18n.isLanguageSelectable(I18n.Language.SPANISH));
        assertFalse(I18n.isLanguageSelectable(I18n.Language.ENGLISH));
        assertTrue(I18n.isLanguageSelectable(null));
    }

    @Test
    void initializationStaysInSpanishRegardlessOfStoredValues() {
        I18n.initialize();
        assertEquals(I18n.Language.SPANISH, I18n.getCurrentLanguage());
        assertEquals("Idioma", I18n.languageMenuLabel());
    }
}
