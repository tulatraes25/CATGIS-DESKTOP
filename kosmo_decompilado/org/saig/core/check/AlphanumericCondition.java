/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.check;

public class AlphanumericCondition {
    private String sourceAttributeName;
    private String targetAttributeName;
    private String operator;

    public AlphanumericCondition() {
    }

    public AlphanumericCondition(String sourceAttributeName, String targetAttributeName, String operator) {
        this.sourceAttributeName = sourceAttributeName;
        this.targetAttributeName = targetAttributeName;
        this.operator = operator;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getSourceAttributeName() {
        return this.sourceAttributeName;
    }

    public void setSourceAttributeName(String sourceAttributeName) {
        this.sourceAttributeName = sourceAttributeName;
    }

    public String getTargetAttributeName() {
        return this.targetAttributeName;
    }

    public void setTargetAttributeName(String targetAttributeName) {
        this.targetAttributeName = targetAttributeName;
    }
}

