/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.GeometryFactory
 */
package es.kosmo.core.geometry.operations;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.task.TaskMonitor;
import es.kosmo.core.geometry.operations.GeometryOpException;

public interface GeometryOp<T> {
    public static final GeometryFactory geomFac = new GeometryFactory();

    public void executeOperation(TaskMonitor var1) throws GeometryOpException;

    public T getResults();

    public void dispose();

    public T[] getErrors();
}

