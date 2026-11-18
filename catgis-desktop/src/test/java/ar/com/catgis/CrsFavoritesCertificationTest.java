package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T2/#259: Argentine featured CRS must match authoritative EPSG codes/names
 * (POSGAR 94 = 22181-22187, POSGAR 98 = 22191-22197,
 * POSGAR 2007 = 5343-5349; EPSG:22191-22197 are Campo Inchauspe). The previous catalog labeled EPSG:22192-22197 as
 * "POSGAR 2007", mixing POSGAR 98 codes with a POSGAR 2007 name.
 */
class CrsFavoritesCertificationTest {

    private static Map<String, String> expectedArgentineFavorites() {
        Map<String, String> expected = new LinkedHashMap<>();
        for (int zone = 1; zone <= 7; zone++) {
            expected.put("EPSG:" + (22180 + zone), "POSGAR 94 / Argentina " + zone);
            expected.put("EPSG:" + (22190 + zone), "Campo Inchauspe / Argentina " + zone);
            expected.put("EPSG:" + (5342 + zone), "POSGAR 2007 / Argentina " + zone);
        }
        return expected;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    @Test
    void argentineFavoritesAreAuthoritative() throws Exception {
        Map<String, String> expected = expectedArgentineFavorites();
        List<CRSDefinitions.CrsCatalogEntry> featured = CRSDefinitions.getFeaturedEntries();
        int checkedArgentine = 0;
        for (CRSDefinitions.CrsCatalogEntry entry : featured) {
            String expectedName = expected.get(entry.code());
            if (expectedName == null) {
                continue;
            }
            checkedArgentine++;
            assertTrue(entry.label().contains(expectedName),
                    entry.code() + " label does not match EPSG: " + entry.label());
            assertTrue(normalize(entry.description()).equals(normalize(expectedName)),
                    entry.code() + " description does not match EPSG: " + entry.description());
            var decoded = org.geotools.referencing.CRS.decode(entry.code(), true);
            String decodedName = normalize(String.valueOf(decoded.getName()));
            assertTrue(decodedName.contains(normalize(expectedName)),
                    "EPSG database says '" + decoded.getName() + "' for " + entry.code()
                            + " but catalog claims '" + expectedName + "'");
        }
        assertTrue(checkedArgentine >= 21,
                "expected the 21 Argentine POSGAR favorites, found " + checkedArgentine);
    }

    @Test
    void noWrongFamilyLabelsRemain() {
        for (CRSDefinitions.CrsCatalogEntry entry : CRSDefinitions.getFeaturedEntries()) {
            int code = Integer.parseInt(entry.code().replace("EPSG:", ""));
            if (code >= 22191 && code <= 22197) {
                assertFalse(entry.label().contains("POSGAR 2007"),
                        entry.code() + " is Campo Inchauspe, not POSGAR 2007");
            }
            if (code >= 5343 && code <= 5349) {
                assertFalse(entry.label().contains("POSGAR 98"),
                        entry.code() + " is POSGAR 2007, not POSGAR 98");
            }
        }
    }

    @Test
    void searchFindsEveryArgertineFavoriteByCode() {
        for (String code : expectedArgentineFavorites().keySet()) {
            List<CRSDefinitions.CrsCatalogEntry> matches = CRSDefinitions.filterEntries(code);
            assertFalse(matches.isEmpty(), "search must find " + code);
            assertTrue(matches.stream().anyMatch(entry -> entry.code().equalsIgnoreCase(code)),
                    "search results must include " + code);
        }
    }

    @Test
    void featuredEntriesHaveUniqueCodes() {
        Map<String, Long> counts = CRSDefinitions.getFeaturedEntries().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        CRSDefinitions.CrsCatalogEntry::code,
                        java.util.stream.Collectors.counting()));
        counts.forEach((code, count) -> assertTrue(count == 1,
                "duplicate featured code: " + code));
        assertNotNull(CRSDefinitions.getFeaturedEntries());
    }
}
