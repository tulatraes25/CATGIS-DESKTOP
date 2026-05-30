/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys;

import com.vividsolutions.jump.coordsys.CoordinateSystem;
import com.vividsolutions.jump.coordsys.impl.PredefinedCoordinateSystems;
import com.vividsolutions.jump.util.Blackboard;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CoordinateSystemRegistry {
    private Map<String, CoordinateSystem> nameToCoordinateSystemMap = new HashMap<String, CoordinateSystem>();

    public static CoordinateSystemRegistry instance(Blackboard blackboard) {
        String COORDINATE_SYSTEMS_KEY = String.valueOf(CoordinateSystemRegistry.class.getName()) + " - COORDINATE SYSTEMS";
        if (blackboard.get(COORDINATE_SYSTEMS_KEY) == null) {
            blackboard.put(COORDINATE_SYSTEMS_KEY, new CoordinateSystemRegistry());
        }
        return (CoordinateSystemRegistry)blackboard.get(COORDINATE_SYSTEMS_KEY);
    }

    private CoordinateSystemRegistry() {
        this.add(PredefinedCoordinateSystems.createUTMNorth(28));
        this.add(PredefinedCoordinateSystems.createUTMNorth(29));
        this.add(PredefinedCoordinateSystems.createUTMNorth(30));
        this.add(PredefinedCoordinateSystems.createUTMNorth(31));
        this.add(PredefinedCoordinateSystems.createUTMNorth(32));
        this.add(PredefinedCoordinateSystems.BC_ALBERS_NAD_83);
        this.add(PredefinedCoordinateSystems.GEOGRAPHICS_WGS_84);
        this.add(CoordinateSystem.UNSPECIFIED);
        this.add(PredefinedCoordinateSystems.UTM_07N_WGS_84);
        this.add(PredefinedCoordinateSystems.UTM_08N_WGS_84);
        this.add(PredefinedCoordinateSystems.UTM_09N_WGS_84);
        this.add(PredefinedCoordinateSystems.UTM_10N_WGS_84);
        this.add(PredefinedCoordinateSystems.UTM_11N_WGS_84);
        this.add(PredefinedCoordinateSystems.UTM_29N_ED50);
        this.add(PredefinedCoordinateSystems.UTM_30N_ED50);
        this.add(PredefinedCoordinateSystems.UTM_31N_ED50);
    }

    public void add(CoordinateSystem coordinateSystem) {
        this.nameToCoordinateSystemMap.put(coordinateSystem.getName(), coordinateSystem);
    }

    public Collection<CoordinateSystem> getCoordinateSystems() {
        return Collections.unmodifiableCollection(this.nameToCoordinateSystemMap.values());
    }

    public CoordinateSystem get(String name) {
        return this.nameToCoordinateSystemMap.get(name);
    }

    public CoordinateSystem get(int epsgCode) {
        Collection<CoordinateSystem> coords = this.getCoordinateSystems();
        for (CoordinateSystem element : coords) {
            if (element.getEPSGCode() != epsgCode) continue;
            return element;
        }
        return CoordinateSystem.UNSPECIFIED;
    }
}

