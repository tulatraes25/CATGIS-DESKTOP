/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.parser;

import org.saig.core.filter.parser.ParseException;
import org.saig.core.filter.parser.Token;

public class ExpressionException
extends ParseException {
    private static final long serialVersionUID = 1L;
    Throwable cause;

    public ExpressionException(String message, Token token) {
        this(message, token, null);
    }

    public ExpressionException(String message, Token token, Throwable cause) {
        super(message);
        this.currentToken = token;
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

    @Override
    public String getMessage() {
        if (this.currentToken == null) {
            return super.getMessage();
        }
        return String.valueOf(super.getMessage()) + ", Current Token : " + this.currentToken.image;
    }
}

