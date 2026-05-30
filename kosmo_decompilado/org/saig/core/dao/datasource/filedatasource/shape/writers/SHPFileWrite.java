/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.writers;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import org.saig.core.dao.datasource.filedatasource.shape.ShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHP;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPShape;
import org.saig.core.dao.datasource.filedatasource.shape.writers.ShapeFileHeader;

public class SHPFileWrite {
    private SHPShape m_shape = null;
    private ByteBuffer m_bb = null;
    private ByteBuffer m_indexBuffer = null;
    private int m_pos = 0;
    private int m_offset;
    private int m_type;
    private int m_cnt;
    private FileChannel shpChannel;
    private FileChannel shxChannel;
    private Rectangle2D extent = null;
    private int fileLength = 100;
    private int numberOfGeometries = 0;

    public SHPFileWrite(FileChannel shpChannel, FileChannel shxChannel) {
        this.shpChannel = shpChannel;
        this.shxChannel = shxChannel;
    }

    private void checkShapeBuffer(int size) {
        if (this.m_bb.capacity() < size) {
            this.m_bb = ByteBuffer.allocateDirect(size);
        }
    }

    private void drain() throws IOException {
        this.m_bb.flip();
        this.m_indexBuffer.flip();
        while (this.m_bb.remaining() > 0) {
            this.shpChannel.write(this.m_bb);
        }
        while (this.m_indexBuffer.remaining() > 0) {
            this.shxChannel.write(this.m_indexBuffer);
        }
        this.m_bb.flip().limit(this.m_bb.capacity());
        this.m_indexBuffer.flip().limit(this.m_indexBuffer.capacity());
    }

    private void allocateBuffers() {
        this.m_bb = ByteBuffer.allocateDirect(16384);
        this.m_indexBuffer = ByteBuffer.allocateDirect(100);
    }

    public void write(IShapeGeometry[] geometries, int type) throws IOException, ShapefileException {
        this.m_shape = SHP.create(type);
        this.writeHeaders(geometries, type);
        this.m_pos = this.m_bb.position();
        int i = 0;
        int ii = geometries.length;
        while (i < ii) {
            this.writeGeometry(geometries[i]);
            ++i;
        }
        this.close();
    }

    private void writeHeaders(IShapeGeometry[] geometries, int type) throws IOException {
        int i = geometries.length - 1;
        while (i >= 0) {
            IShapeGeometry fgeometry = geometries[i];
            this.m_shape.obtainsPoints(fgeometry);
            int size = this.m_shape.getLength(fgeometry) + 8;
            this.fileLength += size;
            if (this.extent == null) {
                this.extent = new Rectangle2D.Double(fgeometry.getBounds2D().getMinX(), fgeometry.getBounds2D().getMinY(), fgeometry.getBounds2D().getWidth(), fgeometry.getBounds2D().getHeight());
            } else {
                this.extent.add(fgeometry.getBounds2D());
            }
            --i;
        }
        this.writeHeaders(this.extent, type, geometries.length, this.fileLength);
    }

    public void writeHeaders(Rectangle2D bounds, int type, int numberOfGeometries, int fileLength) throws IOException {
        if (this.m_bb == null) {
            this.allocateBuffers();
        }
        ShapeFileHeader header = new ShapeFileHeader();
        header.write(this.m_bb, type, numberOfGeometries, fileLength / 2, bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY(), 0.0, 0.0, 0.0, 0.0);
        header.write(this.m_indexBuffer, type, numberOfGeometries, 50 + 4 * numberOfGeometries, bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY(), 0.0, 0.0, 0.0, 0.0);
        this.m_offset = 50;
        this.m_type = type;
        this.m_cnt = 0;
        this.shpChannel.position(0L);
        this.shxChannel.position(0L);
        this.drain();
    }

    public void writeGeometry(IShapeGeometry g) throws IOException {
        if (this.m_bb == null) {
            throw new IOException("Must write headers first");
        }
        this.m_pos = this.m_bb.position();
        this.m_shape.obtainsPoints(g);
        int length = this.m_shape.getLength(g);
        this.checkShapeBuffer(length + 8);
        this.m_bb.order(ByteOrder.BIG_ENDIAN);
        this.m_bb.putInt(++this.m_cnt);
        this.m_bb.putInt(length /= 2);
        this.m_bb.order(ByteOrder.LITTLE_ENDIAN);
        this.m_bb.putInt(this.m_type);
        this.m_shape.write(this.m_bb, g);
        this.m_pos = this.m_bb.position();
        this.m_indexBuffer.putInt(this.m_offset);
        this.m_indexBuffer.putInt(length);
        this.m_offset += length + 4;
        this.drain();
        ++this.numberOfGeometries;
    }

    public void initialize(int type) throws IOException, ShapefileException {
        this.m_shape = SHP.create(type);
        Rectangle2D.Double extent = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
        this.writeHeaders(extent, type, 0, 0);
        this.m_pos = this.m_bb.position();
    }

    public void processGeometry(IShapeGeometry fgeometry) throws IOException {
        this.m_shape.obtainsPoints(fgeometry);
        int size = this.m_shape.getLength(fgeometry) + 8;
        this.fileLength += size;
        if (this.extent == null) {
            this.extent = new Rectangle2D.Double(fgeometry.getBounds2D().getMinX(), fgeometry.getBounds2D().getMinY(), fgeometry.getBounds2D().getWidth(), fgeometry.getBounds2D().getHeight());
        } else {
            this.extent.add(fgeometry.getBounds2D());
        }
        this.writeGeometry(fgeometry);
    }

    public void writeRealHeaders() throws IOException {
        this.shpChannel.position(0L);
        this.shxChannel.position(0L);
        if (this.extent == null) {
            this.extent = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
        }
        this.writeHeaders(this.extent, this.m_type, this.numberOfGeometries, this.fileLength);
    }

    public void close() throws IOException {
        this.shpChannel.close();
        this.shxChannel.close();
        this.shpChannel = null;
        this.shxChannel = null;
        this.m_shape = null;
        this.m_indexBuffer = null;
        this.m_bb = null;
    }

    public int getNumberOfGeometries() {
        return this.numberOfGeometries;
    }
}

