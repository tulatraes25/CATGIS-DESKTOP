/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.writers;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;

public interface SHPShape {
    public int getShapeType();

    public IShapeGeometry read(MappedByteBuffer var1, int var2);

    public void write(ByteBuffer var1, IShapeGeometry var2);

    public int getLength(IShapeGeometry var1);

    public void obtainsPoints(IShapeGeometry var1);
}

