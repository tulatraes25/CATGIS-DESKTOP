/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

public class FilterFactoryCreationException
extends Exception {
    private static final long serialVersionUID = 1L;

    public FilterFactoryCreationException(String msg) {
        super(msg);
    }

    public FilterFactoryCreationException(Exception cause) {
        super(cause);
    }

    public FilterFactoryCreationException(String msg, Exception cause) {
        super(msg, cause);
    }
}

