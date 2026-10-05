package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CRSDefinitionsCatalogTest {

    @Test
    void loadsWorldCatalogFromEmbeddedEpsgDatabase() {
        assertTrue(CRSDefinitions.getCatalogEntries().size() > 7000,
                "El selector de CRS deberia cargar un catalogo mundial real, no solo destacados.");
    }

    @Test
    void findsRepresentativeCountriesAcrossSeveralRegions() {
        String[] queries = {
                "rusia",
                "bielorrusia",
                "china",
                "india",
                "sudafrica",
                "australia",
                "canada",
                "brasil",
                "russia",
                "belarus",
                "south africa",
                "brazil"
        };

        assertAll("busqueda mundial por pais",
                java.util.Arrays.stream(queries)
                        .map(query -> (org.junit.jupiter.api.function.Executable) () ->
                                assertFalse(
                                        CRSDefinitions.filterEntries(query).isEmpty(),
                                        "La busqueda por pais deberia devolver resultados para: " + query
                                )
                        )
        );
    }

    @Test
    void describesCatalogEntriesEvenWhenTheyAreNotInFeaturedList() {
        CRSDefinitions.CrsTechnicalDetails details = CRSDefinitions.describe("EPSG:3035");

        assertAll("detalle tecnico consistente para EPSG no destacado",
                () -> assertEquals("EPSG:3035", details.code()),
                () -> assertTrue(details.label().contains("3035")),
                () -> assertNotEquals("CRS no disponible", details.name()),
                () -> assertFalse(details.name().isBlank()),
                () -> assertFalse(details.areaOfUse().isBlank())
        );
    }

    @Test
    void decodesHistoricPampaDelCastilloFamilyWithoutRuntimeFailure() {
        String[] codes = {
                "EPSG:4161",
                "EPSG:61616405",
                "EPSG:9284",
                "EPSG:2082",
                "EPSG:9285"
        };

        assertAll("fallback manual para familia Pampa del Castillo",
                java.util.Arrays.stream(codes)
                        .map(code -> (org.junit.jupiter.api.function.Executable) () -> {
                            var crs = assertDoesNotThrow(() -> CRSDefinitions.decode(code, true));
                            assertNotNull(crs, "El decode no debe devolver null para " + code);
                        })
        );
    }
}
