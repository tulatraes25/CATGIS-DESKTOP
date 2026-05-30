/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.util.List;

public class CompoundStroke
implements Stroke {
    public static final int ADD = 0;
    public static final int SUBTRACT = 1;
    public static final int INTERSECT = 2;
    public static final int DIFFERENCE = 3;
    protected List<Stroke> strokes;
    private int operation;

    public CompoundStroke(List<Stroke> compoundStrokes, int operation) {
        this.strokes = compoundStrokes;
        this.operation = operation;
    }

    @Override
    public Shape createStrokedShape(Shape shape) {
        Area totalArea = new Area(this.strokes.get(0).createStrokedShape(shape));
        int i = 1;
        while (i < this.strokes.size()) {
            Area currentArea = new Area(this.strokes.get(i).createStrokedShape(shape));
            switch (this.operation) {
                case 0: {
                    totalArea.add(currentArea);
                    break;
                }
                case 1: {
                    totalArea.subtract(currentArea);
                    break;
                }
                case 2: {
                    totalArea.intersect(currentArea);
                    break;
                }
                case 3: {
                    totalArea.exclusiveOr(currentArea);
                }
            }
            ++i;
        }
        return totalArea;
    }
}

