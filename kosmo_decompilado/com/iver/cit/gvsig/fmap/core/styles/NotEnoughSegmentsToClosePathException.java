/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.LineSegment
 */
package com.iver.cit.gvsig.fmap.core.styles;

import com.vividsolutions.jts.geom.LineSegment;
import java.util.ArrayList;

class NotEnoughSegmentsToClosePathException
extends Exception {
    private static final long serialVersionUID = 95503944546535L;

    public NotEnoughSegmentsToClosePathException(ArrayList<LineSegment> segments) {
        super("Need at least 2 segments to close a path. I've got " + segments.size() + ".");
    }
}

