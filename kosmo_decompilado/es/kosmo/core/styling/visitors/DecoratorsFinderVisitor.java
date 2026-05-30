/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package es.kosmo.core.styling.visitors;

import es.kosmo.core.styling.visitors.AbstractStyleVisitor;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;

public class DecoratorsFinderVisitor
extends AbstractStyleVisitor {
    private boolean hasDecorators = false;
    private boolean checkOnlyActives = false;

    public DecoratorsFinderVisitor(boolean onlyActives) {
        this.checkOnlyActives = onlyActives;
    }

    @Override
    public void visit(PointSymbolizer ps) {
        if (!this.checkOnlyActives || ps.isActive()) {
            this.hasDecorators = this.hasDecorators || CollectionUtils.isNotEmpty(ps.getDecorators());
        }
    }

    @Override
    public void visit(LineSymbolizer line) {
        if (!this.checkOnlyActives || line.isActive()) {
            this.hasDecorators = this.hasDecorators || CollectionUtils.isNotEmpty(line.getDecorators());
        }
    }

    @Override
    public void visit(PolygonSymbolizer poly) {
        if (!this.checkOnlyActives || poly.isActive()) {
            this.hasDecorators = this.hasDecorators || CollectionUtils.isNotEmpty(poly.getDecorators());
        }
    }

    public boolean hasDecorators() {
        return this.hasDecorators;
    }
}

