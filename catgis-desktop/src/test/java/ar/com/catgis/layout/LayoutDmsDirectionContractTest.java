package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * REQ-AR-5 contract: the layout graticule must label longitude as E/W and
 * latitude as N/S. Before the fix both branches returned the same direction,
 * so latitude labels were emitted as E/W.
 */
class LayoutDmsDirectionContractTest {

    private static LayoutMap graticule() {
        return new LayoutMap("graticule", 0d, 0d, 100d, 100d);
    }

    @Test
    void longitudeUsesEastWestDirections() {
        LayoutMap map = graticule();
        assertTrue(map.formatDMS(-67.4964d, true).endsWith("W"),
                "Longitud negativa (oeste) debe etiquetarse W.");
        assertTrue(map.formatDMS(67.4964d, true).endsWith("E"),
                "Longitud positiva (este) debe etiquetarse E.");
    }

    @Test
    void latitudeUsesNorthSouthDirections() {
        LayoutMap map = graticule();
        assertTrue(map.formatDMS(-45.8587d, false).endsWith("S"),
                "Latitud negativa (sur) debe etiquetarse S.");
        assertTrue(map.formatDMS(45.8587d, false).endsWith("N"),
                "Latitud positiva (norte) debe etiquetarse N.");
    }

    @Test
    void bothAxesStillUseDegreesMinutesSeconds() {
        LayoutMap map = graticule();
        String latitude = map.formatDMS(-45.8587d, false);
        String longitude = map.formatDMS(-67.4964d, true);
        for (String text : new String[] {latitude, longitude}) {
            assertTrue(text.contains("°"), "Se esperaba simbolo de grados en: " + text);
            assertTrue(text.contains("'"), "Se esperaba separador de minutos en: " + text);
            assertTrue(text.contains("\""), "Se esperaba separador de segundos en: " + text);
            assertTrue(text.matches("^\\d+°\\d{2}'.*$"),
                    "Se esperaba el formato grados/minutos en: " + text);
        }
    }
}
