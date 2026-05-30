/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.scalebar;

import com.vividsolutions.jump.workbench.ui.plugin.scalebar.Unit;
import java.util.ArrayList;
import java.util.Collection;

public class MetricSystem {
    private double modelUnitsPerMetre;

    public MetricSystem(double modelUnitsPerMetre) {
        this.modelUnitsPerMetre = modelUnitsPerMetre;
    }

    public Collection createUnits() {
        ArrayList<Unit> units = new ArrayList<Unit>();
        units.add(new Unit("km", 1000.0 * this.modelUnitsPerMetre));
        units.add(new Unit("m", 1.0 * this.modelUnitsPerMetre));
        units.add(new Unit("mm", 0.001 * this.modelUnitsPerMetre));
        units.add(new Unit("um", 1.0E-6 * this.modelUnitsPerMetre));
        units.add(new Unit("nm", 1.0E-9 * this.modelUnitsPerMetre));
        units.add(new Unit("pm", 1.0E-12 * this.modelUnitsPerMetre));
        units.add(new Unit("\u00ba", 111319.49079327358 * this.modelUnitsPerMetre));
        units.add(new Unit("nmi", 1852.0 * this.modelUnitsPerMetre));
        return units;
    }
}

