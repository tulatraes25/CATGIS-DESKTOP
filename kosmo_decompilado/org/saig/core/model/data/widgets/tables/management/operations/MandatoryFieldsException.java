/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.operations;

public class MandatoryFieldsException
extends Exception {
    private static final long serialVersionUID = 1L;
    private String mandatoryFields;

    public MandatoryFieldsException(String mandatoryFields) {
        this.mandatoryFields = mandatoryFields;
    }

    public String getMandatoryFields() {
        return this.mandatoryFields;
    }
}

