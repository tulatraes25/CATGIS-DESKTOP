/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import org.saig.jump.lang.I18N;

public class I18NUnsupportedOperationException
extends UnsupportedOperationException {
    private static final long serialVersionUID = 1L;
    private static final String UNSUPPORTED_OPERATION = I18N.getString("org.saig.core.util.I18NUnsupportedOperationException.operation-not-supported");

    public I18NUnsupportedOperationException() {
        super(UNSUPPORTED_OPERATION);
    }

    public I18NUnsupportedOperationException(String message) {
        super(message);
    }
}

