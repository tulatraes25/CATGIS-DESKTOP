/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.util.Iterator;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.NullFilter;

public class BoundsExtractor
implements FilterVisitor {
    protected Envelope clippedbbox;
    private short logicType = (short)2;

    public BoundsExtractor(Envelope bbox) {
        this.clippedbbox = bbox;
    }

    public BoundsExtractor(int minx, int maxx, int miny, int maxy) {
        this.clippedbbox = new Envelope((double)minx, (double)maxx, (double)miny, (double)maxy);
    }

    @Override
    public void visit(Filter filter) {
        if (Filter.NONE == filter) {
            return;
        }
        switch (filter.getFilterType()) {
            case 19: {
                this.visit((BetweenFilter)filter);
                break;
            }
            case 14: 
            case 15: 
            case 16: 
            case 17: 
            case 18: 
            case 23: {
                this.visit((BetweenFilter)filter);
                break;
            }
            case 22: {
                this.visit((BetweenFilter)filter);
                break;
            }
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 12: 
            case 13: 
            case 24: {
                this.visit((GeometryFilter)filter);
                break;
            }
            case 20: {
                this.visit((LikeFilter)filter);
                break;
            }
            case 1: 
            case 2: 
            case 3: {
                this.visit((LogicFilter)filter);
                break;
            }
            case 21: {
                this.visit((NullFilter)filter);
            }
        }
    }

    @Override
    public void visit(BetweenFilter filter) {
        if (filter != null) {
            if (filter.getLeftValue() != null) {
                filter.getLeftValue().accept(this);
            }
            if (filter.getRightValue() != null) {
                filter.getRightValue().accept(this);
            }
            if (filter.getMiddleValue() != null) {
                filter.getMiddleValue().accept(this);
            }
        }
    }

    @Override
    public void visit(CompareFilter filter) {
        if (filter != null) {
            if (filter.getLeftValue() != null) {
                filter.getLeftValue().accept(this);
            }
            if (filter.getRightValue() != null) {
                filter.getRightValue().accept(this);
            }
        }
    }

    @Override
    public void visit(GeometryFilter filter) {
        if (filter != null) {
            LiteralExpression le = null;
            Envelope bbox = null;
            if (filter.getLeftGeometry().getType() == 104) {
                le = (LiteralExpression)filter.getLeftGeometry();
                if (le != null && le.getLiteral() != null && le.getLiteral() instanceof Geometry) {
                    bbox = ((Geometry)le.getLiteral()).getEnvelopeInternal();
                }
            } else if (filter.getRightGeometry().getType() == 104 && (le = (LiteralExpression)filter.getRightGeometry()) != null && le.getLiteral() != null && le.getLiteral() instanceof Geometry) {
                Geometry g = (Geometry)le.getLiteral();
                bbox = g.getEnvelopeInternal();
            }
            if (bbox != null) {
                switch (this.logicType) {
                    case 2: {
                        this.and(bbox, filter.getFilterType());
                        break;
                    }
                    case 1: {
                        this.or(bbox, filter.getFilterType());
                        break;
                    }
                }
            }
        }
    }

    private void or(Envelope bbox, short s) {
        switch (s) {
            case 4: 
            case 5: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 12: 
            case 24: {
                if (!bbox.intersects(this.clippedbbox)) {
                    if (this.clippedbbox == null || this.clippedbbox.isNull()) {
                        this.clippedbbox = bbox;
                    } else {
                        this.clippedbbox.expandToInclude(bbox);
                    }
                } else {
                    boolean changed = false;
                    double minx = this.clippedbbox.getMinX();
                    double miny = this.clippedbbox.getMinY();
                    double maxx = this.clippedbbox.getMaxX();
                    double maxy = this.clippedbbox.getMaxY();
                    if (minx > bbox.getMinX()) {
                        minx = bbox.getMinX();
                        changed = true;
                    }
                    if (maxx < bbox.getMaxX()) {
                        maxx = bbox.getMaxX();
                        changed = true;
                    }
                    if (miny > bbox.getMinY()) {
                        miny = bbox.getMinY();
                        changed = true;
                    }
                    if (maxy < bbox.getMaxY()) {
                        maxy = bbox.getMaxY();
                        changed = true;
                    }
                    if (changed) {
                        this.clippedbbox = new Envelope(minx, maxx, miny, maxy);
                    }
                }
                return;
            }
            case 6: 
            case 13: {
                return;
            }
        }
    }

    private void and(Envelope bbox, short s) {
        switch (s) {
            case 4: 
            case 5: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 12: 
            case 24: {
                if (!bbox.intersects(this.clippedbbox)) {
                    this.clippedbbox = new Envelope(this.clippedbbox.getMinX(), this.clippedbbox.getMinX(), this.clippedbbox.getMinY(), this.clippedbbox.getMinY());
                } else {
                    boolean changed = false;
                    double minx = this.clippedbbox.getMinX();
                    double miny = this.clippedbbox.getMinY();
                    double maxx = this.clippedbbox.getMaxX();
                    double maxy = this.clippedbbox.getMaxY();
                    if (minx < bbox.getMinX()) {
                        minx = bbox.getMinX();
                        changed = true;
                    }
                    if (maxx > bbox.getMaxX()) {
                        maxx = bbox.getMaxX();
                        changed = true;
                    }
                    if (miny < bbox.getMinY()) {
                        miny = bbox.getMinY();
                        changed = true;
                    }
                    if (maxy > bbox.getMaxY()) {
                        maxy = bbox.getMaxY();
                        changed = true;
                    }
                    if (changed) {
                        this.clippedbbox = new Envelope(minx, maxx, miny, maxy);
                    }
                }
                return;
            }
            case 6: 
            case 13: {
                return;
            }
        }
    }

    @Override
    public void visit(LikeFilter filter) {
        if (filter != null && filter.getValue() != null) {
            filter.getValue().accept(this);
        }
    }

    @Override
    public void visit(LogicFilter filter) {
        short oldType = this.logicType;
        if (filter != null) {
            switch (filter.getFilterType()) {
                case 1: {
                    Envelope original = this.clippedbbox;
                    this.clippedbbox = new Envelope();
                    this.logicType = this.logicType == 3 ? this.logicType : (short)1;
                    Iterator<Filter> i = filter.getFilterIterator();
                    while (i.hasNext()) {
                        Filter tmp = i.next();
                        tmp.accept(this);
                    }
                    if (this.logicType == 3) break;
                    this.logicType = (short)2;
                    Envelope newBbox = this.clippedbbox;
                    this.clippedbbox = original;
                    this.and(newBbox, (short)7);
                    break;
                }
                case 2: {
                    Iterator<Filter> i = filter.getFilterIterator();
                    this.logicType = (short)(this.logicType == 3 ? (int)this.logicType : 2);
                    while (i.hasNext()) {
                        Filter tmp = i.next();
                        tmp.accept(this);
                    }
                    break;
                }
                case 3: {
                    Iterator<Filter> i = filter.getFilterIterator();
                    this.logicType = (short)(this.logicType == 3 ? 2 : 3);
                    while (i.hasNext()) {
                        Filter tmp = i.next();
                        tmp.accept(this);
                    }
                    break;
                }
            }
            this.logicType = oldType;
        }
    }

    @Override
    public void visit(NullFilter filter) {
        if (filter != null && filter.getNullCheckValue() != null) {
            filter.getNullCheckValue().accept(this);
        }
    }

    @Override
    public void visit(FidFilter filter) {
    }

    @Override
    public void visit(AttributeExpression expression) {
    }

    @Override
    public void visit(Expression expression) {
    }

    @Override
    public void visit(LiteralExpression expression) {
    }

    @Override
    public void visit(MathExpression expression) {
    }

    @Override
    public void visit(FunctionExpression expression) {
    }

    public Envelope getBBox() {
        return this.clippedbbox;
    }
}

