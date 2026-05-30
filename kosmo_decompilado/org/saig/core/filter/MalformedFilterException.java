/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

public class MalformedFilterException
extends Exception {
    private static final long serialVersionUID = 1L;

    public MalformedFilterException(String message) {
        super(message);
    }

    public MalformedFilterException(Exception cause) {
        super(cause);
    }

    public MalformedFilterException(String msg, Exception cause) {
        super(msg, cause);
    }
}

