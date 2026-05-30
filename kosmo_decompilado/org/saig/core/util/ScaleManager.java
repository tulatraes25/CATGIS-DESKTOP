/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.converter.UnitConverter
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.cresques.cts.IProjection
 */
package org.saig.core.util;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.cresques.cts.IProjection;
import org.saig.core.util.UnitsManager;

public class ScaleManager {
    private static ScaleManager instance;
    public static double dpi;

    static {
        dpi = Toolkit.getDefaultToolkit().getScreenResolution();
    }

    private ScaleManager() {
    }

    public static ScaleManager getInstance() {
        if (instance == null) {
            instance = new ScaleManager();
        }
        return instance;
    }

    public synchronized double generateScaleValue(double maxX, double minX, double width, IProjection proj, Unit<Length> mapLenghtUnit) {
        Unit<Length> meters = UnitsManager.getLengthUnit("m");
        double scale = -1.0;
        if (proj == null) {
            double w = (maxX - minX) * (dpi / 2.54 * 100.0) / width;
            scale = UnitsManager.convertDistanceValue(w, mapLenghtUnit, meters);
        } else if (proj.isProjected()) {
            double convertedMinX = UnitsManager.convertDistanceValue(minX, mapLenghtUnit, meters);
            double convertedMaxX = UnitsManager.convertDistanceValue(maxX, mapLenghtUnit, meters);
            scale = proj.getScale(convertedMinX, convertedMaxX, width, dpi);
        } else {
            scale = proj.getScale(minX, maxX, width, dpi);
        }
        return scale;
    }

    public synchronized Envelope generateNewEnvelopeValue(Envelope currentEnv, double screenWidth, double screenHeight, double scale, IProjection proj, Unit<Length> mapLenghtUnit) {
        UnitConverter converter = mapLenghtUnit.getConverterTo(UnitsManager.getLengthUnit("cm"));
        double changeUnits = converter.convert(1.0);
        Rectangle2D.Double currentExtent = new Rectangle2D.Double(currentEnv.getMinX(), currentEnv.getMinY(), currentEnv.getWidth(), currentEnv.getHeight());
        Rectangle2D rec = proj.getExtent((Rectangle2D)currentExtent, scale, screenWidth, screenHeight, changeUnits, dpi);
        return new Envelope(rec.getMinX(), rec.getMaxX(), rec.getMinY(), rec.getMaxY());
    }
}

