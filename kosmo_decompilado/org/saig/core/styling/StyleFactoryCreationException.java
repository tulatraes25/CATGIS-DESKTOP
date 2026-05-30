/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

public class StyleFactoryCreationException
extends Exception {
    private static final long serialVersionUID = 1L;

    public StyleFactoryCreationException() {
    }

    public StyleFactoryCreationException(String msg) {
        super(msg);
    }

    public StyleFactoryCreationException(Exception e) {
        super(e);
    }

    public StyleFactoryCreationException(String msg, Exception e) {
        super(msg, e);
    }
}

