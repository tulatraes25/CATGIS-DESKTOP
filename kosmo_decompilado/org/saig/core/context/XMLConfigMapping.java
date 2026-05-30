/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.context;

import java.util.Hashtable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLConfigMapping
extends DefaultHandler {
    private Hashtable<String, String> keysValues;
    private String key;
    private String value;

    public XMLConfigMapping(Hashtable<String, String> keysValues) {
        this.keysValues = keysValues;
    }

    @Override
    public void endElement(String namespaceURI, String localName, String name) throws SAXException {
    }

    @Override
    public void startElement(String namespaceURI, String localName, String name, Attributes attrs) {
        if (!name.equalsIgnoreCase("config")) {
            this.key = name;
            int i = 0;
            while (i < attrs.getLength()) {
                String nattr = attrs.getQName(i);
                String val = attrs.getValue(i);
                this.value = nattr.equalsIgnoreCase("value") ? val : null;
                ++i;
            }
            this.keysValues.put(this.key, this.value);
        }
    }
}

