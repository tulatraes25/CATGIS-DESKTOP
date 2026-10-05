package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgisWriteServiceTest {

    @Test
    void suggestsStableNeutralTableNames() {
        assertEquals("parcelas_urbanas", PostgisWriteService.suggestTableName("Parcelas urbanas"));
        assertEquals("layer_2026_catastro", PostgisWriteService.suggestTableName("2026 catastro"));
        assertEquals("capa_vectorial", PostgisWriteService.suggestTableName("###"));
    }

    @Test
    void validatesSchemaAndTableIdentifiers() {
        assertTrue(PostgisWriteService.validateIdentifier("public", "El schema").isBlank());
        assertTrue(PostgisWriteService.validateIdentifier("catastro_2026", "El nombre de tabla").isBlank());
        assertTrue(PostgisWriteService.validateIdentifier("tabla con espacios", "El nombre de tabla").contains("solo puede contener"));
        assertTrue(PostgisWriteService.validateIdentifier("2026tabla", "El nombre de tabla").contains("debe empezar"));
    }

    @Test
    void findsQualifiedTypeNamesIgnoringCase() {
        String[] typeNames = {"public.Parcelas", "catastro.calles_eje", "hidro.cuencas"};
        assertEquals("public.Parcelas", PostgisWriteService.findMatchingTypeName(typeNames, "PUBLIC", "parcelas"));
        assertEquals("catastro.calles_eje", PostgisWriteService.findMatchingTypeName(typeNames, "catastro", "CALLES_EJE"));
        assertNull(PostgisWriteService.findMatchingTypeName(typeNames, "public", "edificios"));
    }
}
