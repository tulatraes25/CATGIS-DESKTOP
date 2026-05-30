/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.styling.visitors;

import es.kosmo.core.styling.visitors.AbstractStyleVisitor;
import org.saig.core.styling.TextSymbolizer;

public class LabelFinderVisitor
extends AbstractStyleVisitor {
    private boolean hasLabels = false;
    private boolean checkOnlyActives = false;

    public LabelFinderVisitor(boolean onlyActives) {
        this.checkOnlyActives = onlyActives;
    }

    @Override
    public void visit(TextSymbolizer text) {
        this.hasLabels = this.checkOnlyActives ? text.isActive() : true;
    }

    public boolean hasLabels() {
        return this.hasLabels;
    }
}

