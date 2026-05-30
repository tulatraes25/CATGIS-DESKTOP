/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

public class TestEntity {
    private String key = "";
    private String stringval = "";
    private Boolean booleanval = new Boolean(true);
    private String discreteval = "test1";

    public TestEntity() {
    }

    public TestEntity(String key, String sval, Boolean bval, String discreteval) {
        this.setKey(key);
        this.setStringval(sval);
        this.setBooleanval(bval);
        this.setDiscreteval(discreteval);
    }

    public String toString() {
        String retVal = "key=" + this.key + "\n" + "boolean=" + this.booleanval;
        return retVal;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStringval() {
        return this.stringval;
    }

    public void setStringval(String stringval) {
        this.stringval = stringval;
    }

    public Boolean getBooleanval() {
        return this.booleanval;
    }

    public void setBooleanval(Boolean booleanval) {
        this.booleanval = booleanval;
    }

    public String getDiscreteval() {
        return this.discreteval;
    }

    public void setDiscreteval(String discreteval) {
        this.discreteval = discreteval;
    }
}

