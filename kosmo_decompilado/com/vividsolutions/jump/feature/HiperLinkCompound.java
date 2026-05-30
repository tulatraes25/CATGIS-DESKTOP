/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jump.feature.HiperLink;
import org.saig.core.model.data.Table;

public class HiperLinkCompound
extends HiperLink {
    private Table table;
    private String keyFieldSource;
    private String keyFieldTarget;

    public HiperLinkCompound(String fieldWithHiperLink, String keyFieldSource, String keyFieldTarget) {
        super(fieldWithHiperLink);
        this.keyFieldSource = keyFieldSource;
        this.keyFieldTarget = keyFieldTarget;
    }

    public Table getTable() {
        return this.table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getKeyFieldSource() {
        return this.keyFieldSource;
    }

    public String getKeyFieldTarget() {
        return this.keyFieldTarget;
    }
}

