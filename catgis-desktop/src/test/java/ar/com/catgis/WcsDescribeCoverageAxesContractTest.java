package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T4/#108: DescribeCoverage axes are parsed and addressed without guessing. */
class WcsDescribeCoverageAxesContractTest {

    private static byte[] document(String srsName, String axisLabels) {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <wcs:CoverageDescriptions xmlns:wcs="http://www.opengis.net/wcs/2.0"
                    xmlns:gml="http://www.opengis.net/gml/3.2">
                  <wcs:CoverageDescription gml:id="dem">
                    <gml:boundedBy>
                      <gml:Envelope srsName="%s" axisLabels="%s">
                        <gml:lowerCorner>-70 -45</gml:lowerCorner>
                        <gml:upperCorner>-69 -44</gml:upperCorner>
                      </gml:Envelope>
                    </gml:boundedBy>
                    <wcs:CoverageId>dem</wcs:CoverageId>
                  </wcs:CoverageDescription>
                </wcs:CoverageDescriptions>
                """.formatted(srsName, axisLabels);
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void longitudeLatitudeLabelsAreMappedByMeaning() throws Exception {
        var plan = WcsDescribeCoverageAxes.parse(document(
                "http://www.opengis.net/def/crs/EPSG/0/4326", "lat lon"));
        assertEquals("lon", plan.xAxisLabel());
        assertEquals("lat", plan.yAxisLabel());
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/4326", plan.crs());
    }

    @Test
    void eastingNorthingLabelsAreMappedByMeaning() throws Exception {
        var plan = WcsDescribeCoverageAxes.parse(document(
                "EPSG:32719", "N E"));
        assertEquals("E", plan.xAxisLabel());
        assertEquals("N", plan.yAxisLabel());

        var xy = WcsDescribeCoverageAxes.parse(document("EPSG:3857", "x y"));
        assertEquals("x", xy.xAxisLabel());
        assertEquals("y", xy.yAxisLabel());
    }

    @Test
    void ambiguousOrUnsupportedLabelsFailClosed() {
        assertThrows(java.io.IOException.class, () -> WcsDescribeCoverageAxes.parse(
                document("EPSG:4326", "i j")));
        assertThrows(java.io.IOException.class, () -> WcsDescribeCoverageAxes.parse(
                document("EPSG:4326", "lon lon")));
        assertThrows(java.io.IOException.class, () -> WcsDescribeCoverageAxes.parse(
                document("", "lon lat")), "missing srsName must fail closed");
        assertThrows(java.io.IOException.class, () -> WcsDescribeCoverageAxes.parse(
                document("EPSG:4326", "lon")), "a single axis must fail closed");
    }

    @Test
    void dimensionParametersUseAdvertisedLabelsAndValidatedBbox() throws Exception {
        var plan = WcsDescribeCoverageAxes.parse(document("EPSG:4326", "lat lon"));
        String params = WcsDescribeCoverageAxes.dimensionParameters(
                plan, "-69.5,-45.5,-69.0,-45.0", 256, 200);
        assertTrue(params.contains("SUBSET=lon%28-69.5%2C-69.0%29"), params);
        assertTrue(params.contains("SUBSET=lat%28-45.5%2C-45.0%29"), params);
        assertTrue(params.contains("SCALESIZE=lon%28256%29"), params);
        assertTrue(params.contains("SCALESIZE=lat%28200%29"), params);

        assertThrows(java.io.IOException.class, () -> WcsDescribeCoverageAxes
                .dimensionParameters(plan, "-69,-45,-70,-44", 256, 200));
        assertThrows(java.io.IOException.class, () -> WcsDescribeCoverageAxes
                .dimensionParameters(plan, "a,b,c,d", 256, 200));
    }

    @Test
    void oversizedOrMalformedDocumentsFailClosed() {
        assertThrows(java.io.IOException.class,
                () -> WcsDescribeCoverageAxes.parse(new byte[0]));
        assertThrows(java.io.IOException.class,
                () -> WcsDescribeCoverageAxes.parse("<not-xml".getBytes(StandardCharsets.UTF_8)));
        byte[] huge = new byte[WcsDescribeCoverageAxes.MAX_DOCUMENT_BYTES + 1];
        assertThrows(java.io.IOException.class, () -> WcsDescribeCoverageAxes.parse(huge));
    }
}
