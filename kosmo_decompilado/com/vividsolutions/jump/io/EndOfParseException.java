/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import org.xml.sax.SAXException;

public class EndOfParseException
extends SAXException {
    private static final long serialVersionUID = 1L;

    public EndOfParseException() {
    }

    public EndOfParseException(String msg) {
        super(msg);
    }
}

