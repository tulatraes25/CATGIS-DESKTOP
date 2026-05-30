/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.opengis.util.Cloneable
 */
package es.kosmo.core.styling.visitors;

import es.kosmo.core.styling.visitors.AbstractStyleVisitor;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.MathExpressionImpl;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.util.MeasureUtils;

public class UpdateSizeValueStyleVisitor
extends AbstractStyleVisitor {
    protected static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    protected boolean isPointSymb;
    protected boolean isLineSymb;
    protected boolean isPolygonSymb;
    protected boolean isTextSymb;
    protected Symbolizer result;
    protected String uom;
    protected double factor;
    protected double pxSize;
    protected Unit<Length> viewportUnit;

    public UpdateSizeValueStyleVisitor(Symbolizer symb, double factor, double pixelSize, Unit<Length> viewportLengthUnit) {
        this.isPointSymb = symb instanceof PointSymbolizer;
        this.isLineSymb = symb instanceof LineSymbolizer;
        this.isPolygonSymb = symb instanceof PolygonSymbolizer;
        this.isTextSymb = symb instanceof TextSymbolizer;
        this.result = (Symbolizer)((Cloneable)symb).clone();
        this.uom = symb.getUnitsOfMeasurement();
        this.factor = factor;
        this.pxSize = pixelSize;
        this.viewportUnit = viewportLengthUnit;
    }

    public Symbolizer updateSymbolizer() {
        this.result.accept(this);
        return this.result;
    }

    @Override
    public void visit(Graphic gr) {
        if (this.isPointSymb) {
            Expression expSize = this.updateSizeExpression(gr.getSize());
            gr.setSize(expSize);
        }
    }

    @Override
    public void visit(Stroke stroke) {
        if (this.isLineSymb || this.isPolygonSymb) {
            Expression expSize = this.updateSizeExpression(stroke.getWidth());
            stroke.setWidth(expSize);
        }
    }

    private Expression updateSizeExpression(Expression expSize) {
        MathExpressionImpl mathExpression = null;
        double sizeFactor = 1.0;
        if (!"pixel".equals(this.uom)) {
            Unit<Length> heightUnits = MeasureUtils.getLenghtUnitByName(this.uom);
            double unitsFactor = MeasureUtils.getLengthTransformFactor(heightUnits, this.viewportUnit);
            sizeFactor = unitsFactor / this.pxSize / this.factor;
            try {
                mathExpression = (MathExpressionImpl)filterFactory.createMathExpression((short)107);
                mathExpression.addLeftValue(expSize);
                mathExpression.addRightValue(filterFactory.createLiteralExpression(sizeFactor));
            }
            catch (IllegalFilterException illegalFilterException) {}
        } else {
            return expSize;
        }
        return mathExpression;
    }
}

