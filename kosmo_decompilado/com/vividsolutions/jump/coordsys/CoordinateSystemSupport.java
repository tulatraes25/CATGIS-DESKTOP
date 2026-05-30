/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys;

import com.vividsolutions.jump.util.Blackboard;

public class CoordinateSystemSupport {
    private static final String ENABLED_KEY = String.valueOf(CoordinateSystemSupport.class.getName()) + " - ENABLED";

    public static boolean isEnabled(Blackboard blackboard) {
        return blackboard.get(ENABLED_KEY, false);
    }

    public static void setEnabled(boolean enabled, Blackboard blackboard) {
        blackboard.put(ENABLED_KEY, enabled);
    }
}

