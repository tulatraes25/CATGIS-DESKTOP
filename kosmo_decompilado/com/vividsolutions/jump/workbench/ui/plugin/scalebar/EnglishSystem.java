/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.scalebar;

import com.vividsolutions.jump.workbench.ui.plugin.scalebar.Unit;
import java.util.ArrayList;
import java.util.Collection;

public class EnglishSystem {
    private double modelUnitsPerInch;
    public static final double MILES_PER_INCH = 1.5708451146716935E-5;
    public static final double YARDS_PER_INCH = 0.027777777777777776;
    public static final double FEET_PER_INCH = 0.08333333333333333;
    public static final double INCHES_PER_INCH = 1.0;

    public EnglishSystem(double modelUnitsPerMetre) {
        this.modelUnitsPerInch = modelUnitsPerMetre;
    }

    public Collection createUnits() {
        ArrayList<Unit> units = new ArrayList<Unit>();
        units.add(new Unit("miles", 63360.0 * this.modelUnitsPerInch));
        units.add(new Unit("yards", 36.0 * this.modelUnitsPerInch));
        units.add(new Unit("feet", 12.0 * this.modelUnitsPerInch));
        units.add(new Unit("inches", 1.0 * this.modelUnitsPerInch));
        return units;
    }
}

