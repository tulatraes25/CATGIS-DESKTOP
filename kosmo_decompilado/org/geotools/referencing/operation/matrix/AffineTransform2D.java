/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.referencing.operation.matrix.GeneralMatrix
 *  org.geotools.resources.i18n.Errors
 *  org.opengis.referencing.operation.Matrix
 */
package org.geotools.referencing.operation.matrix;

import java.awt.geom.AffineTransform;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.resources.i18n.Errors;
import org.opengis.referencing.operation.Matrix;
import org.saig.jump.lang.I18N;

public class AffineTransform2D
extends AffineTransform
implements Matrix {
    private static final long serialVersionUID = -9104194268576601386L;
    public static final int SIZE = 3;

    public AffineTransform2D() {
    }

    public AffineTransform2D(AffineTransform transform) {
        super(transform);
    }

    public AffineTransform2D(Matrix matrix) {
        if (matrix.getNumRow() != 3 || matrix.getNumCol() != 3) {
            throw new IllegalArgumentException(I18N.getString("org.geotools.referencing.operation.matrix.AffineTransform2D.matrix-with-incorrect-size"));
        }
        int i = 0;
        while (i < 3) {
            AffineTransform2D.checkLastRow(i, matrix.getElement(2, i));
            ++i;
        }
        int c = 0;
        double[] values = new double[6];
        int j = 0;
        while (j < 2) {
            int i2 = 0;
            while (i2 < 3) {
                values[c++] = matrix.getElement(j, i2);
                ++i2;
            }
            ++j;
        }
        assert (c == values.length) : c;
        this.setTransform(values);
    }

    private void setTransform(double[] matrix) {
        this.setTransform(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
    }

    public final int getNumRow() {
        return 3;
    }

    public final int getNumCol() {
        return 3;
    }

    public double getElement(int row, int column) {
        switch (row) {
            case 0: {
                switch (column) {
                    case 0: {
                        return this.getScaleX();
                    }
                    case 1: {
                        return this.getShearX();
                    }
                    case 2: {
                        return this.getTranslateX();
                    }
                }
                break;
            }
            case 1: {
                switch (column) {
                    case 0: {
                        return this.getShearY();
                    }
                    case 1: {
                        return this.getScaleY();
                    }
                    case 2: {
                        return this.getTranslateY();
                    }
                }
                break;
            }
            case 2: {
                switch (column) {
                    case 0: 
                    case 1: {
                        return 0.0;
                    }
                    case 2: {
                        return 1.0;
                    }
                }
                break;
            }
            default: {
                throw new IndexOutOfBoundsException(Errors.format((int)42, (Object)"column", (Object)column));
            }
        }
        throw new IndexOutOfBoundsException(Errors.format((int)42, (Object)"row", (Object)row));
    }

    public void setElement(int row, int column, double value) {
        if (row < 0 || row >= 3) {
            throw new IndexOutOfBoundsException(Errors.format((int)42, (Object)"row", (Object)row));
        }
        if (column < 0 || column >= 3) {
            throw new IndexOutOfBoundsException(Errors.format((int)42, (Object)"column", (Object)column));
        }
        if (row == 2) {
            AffineTransform2D.checkLastRow(column, value);
            return;
        }
        double[] matrix = new double[6];
        this.getMatrix(matrix);
        matrix[row * 3 + column] = value;
        this.setTransform(matrix);
        assert (Double.compare(this.getElement(row, column), value) == 0) : value;
    }

    private static void checkLastRow(int column, double value) throws IllegalArgumentException {
        if (value != (double)(column == 2 ? 1 : 0)) {
            throw new IllegalArgumentException(Errors.format((int)42, (Object)("matrix[2," + column + ']'), (Object)value));
        }
    }

    @Override
    public String toString() {
        return GeneralMatrix.toString((Matrix)this);
    }

    @Override
    public AffineTransform2D clone() {
        return (AffineTransform2D)super.clone();
    }
}

