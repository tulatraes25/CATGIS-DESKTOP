/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

public class IllegalFilterException
extends Exception {
    private static final long serialVersionUID = 1L;

    public IllegalFilterException(String message) {
        super(message);
    }

    public IllegalFilterException(Exception cause) {
        super(cause);
    }

    public IllegalFilterException(String msg, Exception cause) {
        super(msg, cause);
    }
}

