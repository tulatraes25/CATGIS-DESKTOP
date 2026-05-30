/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys;

import com.vividsolutions.jump.coordsys.Projection;
import java.io.Serializable;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;

public class CoordinateSystem
implements Comparable<CoordinateSystem>,
Serializable {
    private static final long serialVersionUID = -811718450919581831L;
    private Projection projection;
    private String name;
    private int epsgCode;
    public static final CoordinateSystem UNSPECIFIED = new CoordinateSystem(I18N.getString("coordsys.CoordinateSystem.unspecified"), 0, null){
        private static final long serialVersionUID = -811718450919581831L;

        @Override
        public Projection getProjection() {
            throw new I18NUnsupportedOperationException();
        }

        @Override
        public int getEPSGCode() {
            throw new I18NUnsupportedOperationException();
        }
    };

    private Object readResolve() {
        return this.name.equals(CoordinateSystem.UNSPECIFIED.name) ? UNSPECIFIED : this;
    }

    public CoordinateSystem(String name, int epsgCode, Projection projection) {
        this.name = name;
        this.projection = projection;
        this.epsgCode = epsgCode;
    }

    public String toString() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    public Projection getProjection() {
        return this.projection;
    }

    public int getEPSGCode() {
        return this.epsgCode;
    }

    @Override
    public int compareTo(CoordinateSystem o) {
        if (this == o) {
            return 0;
        }
        if (this == UNSPECIFIED) {
            return -1;
        }
        if (o == UNSPECIFIED) {
            return 1;
        }
        return this.toString().compareTo(o.toString());
    }
}

