package ar.com.catgis;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Hardened parser for WMS/WFS GetCapabilities endpoints.
 * Legacy WMS 1.1.1 legitimately declares an external DTD; its declaration
 * is accepted without fetching it or expanding any declared XML entities.
 */
final class OgcCapabilitiesXml {

    private OgcCapabilitiesXml() {
    }

    static Document parse(InputStream input, boolean allowLegacyWmsDoctype) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) ->
                new InputSource(new StringReader("")));
        Document document = builder.parse(input);
        DocumentType type = document.getDoctype();
        if (type != null) {
            boolean legacyWms = allowLegacyWmsDoctype
                    && "WMT_MS_Capabilities".equals(type.getName());
            boolean hasEntities = type.getEntities() != null
                    && type.getEntities().getLength() > 0;
            if (!legacyWms || hasEntities) {
                throw new IllegalArgumentException(
                        "GetCapabilities contiene declaraciones XML no permitidas.");
            }
        }
        return document;
    }
}
