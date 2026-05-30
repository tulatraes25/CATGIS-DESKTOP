/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.cresques.cts.ICoordTrans
 */
package es.kosmo.core.renderer.decorators;

import com.vividsolutions.jump.feature.Feature;
import es.kosmo.core.renderer.decorators.IDecoratorPoint2DTransformer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;
import org.cresques.cts.ICoordTrans;

public interface IDecorator {
    public String getName();

    public Icon getIcon();

    public boolean isFixedRotation();

    public void setFixedRotation(boolean var1);

    public double getRotation();

    public void setRotation(double var1);

    public Point2D getOffset();

    public void setOffset(Point2D var1);

    public double getSize();

    public void setSize(double var1);

    public Color getColor();

    public void setColor(Color var1);

    public Color getSelectionColor();

    public void setSelectionColor(Color var1);

    public void paint(Feature var1, Graphics2D var2, IDecoratorPoint2DTransformer var3, ICoordTrans var4, double var5, Unit<Length> var7) throws Exception;

    public boolean isCompatible(int var1);

    public void setUnit(String var1);

    public String getUnit();
}

