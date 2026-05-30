/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hardcode.gdbms.engine.data.DataSourceFactory
 *  com.hardcode.gdbms.engine.data.driver.DriverException
 *  com.hardcode.gdbms.engine.data.driver.ObjectDriver
 *  com.hardcode.gdbms.engine.values.StringValue
 *  com.hardcode.gdbms.engine.values.Value
 *  com.iver.cit.gvsig.fmap.core.FGeometry
 *  com.iver.cit.gvsig.fmap.core.FNullGeometry
 *  com.iver.cit.gvsig.fmap.core.FShape
 *  com.iver.cit.gvsig.fmap.core.IGeometry
 *  com.iver.cit.gvsig.fmap.core.ShapeFactory
 *  com.iver.cit.gvsig.fmap.drivers.BoundedShapes
 *  com.iver.cit.gvsig.fmap.drivers.VectorialDriver
 *  com.iver.cit.gvsig.fmap.operations.strategies.MemoryShapeInfo
 */
package com.iver.cit.gvsig.fmap.drivers;

import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.data.driver.ObjectDriver;
import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FNullGeometry;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.BoundedShapes;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.operations.strategies.MemoryShapeInfo;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public abstract class MemoryDriver
implements VectorialDriver,
ObjectDriver,
BoundedShapes {
    private MemoryShapeInfo memShapeInfo = new MemoryShapeInfo();
    private List<IGeometry> arrayGeometries = new ArrayList<IGeometry>();
    private Rectangle2D fullExtent;
    private int m_Position;
    private DefaultTableModel m_TableModel = new DefaultTableModel();
    private int[] fieldWidth = null;

    public DefaultTableModel getTableModel() {
        return this.m_TableModel;
    }

    public void addGeometry(IGeometry geom, Object[] row) {
        Rectangle2D boundsShp;
        if (geom == null) {
            return;
        }
        if (!(geom instanceof FNullGeometry)) {
            boundsShp = geom.getBounds();
            this.memShapeInfo.addShapeInfo(boundsShp, geom.getGeometryType());
            this.arrayGeometries.add(geom);
            if (this.fullExtent == null) {
                this.fullExtent = boundsShp;
            } else {
                this.fullExtent.add(boundsShp);
            }
        } else {
            boundsShp = new Rectangle2D.Double();
            this.memShapeInfo.addShapeInfo(boundsShp, geom.getGeometryType());
            this.arrayGeometries.add(geom);
        }
        if (this.fieldWidth == null) {
            this.initializeFieldWidth(row);
        }
        this.actualizeFieldWidth(row);
        this.m_TableModel.addRow(row);
        try {
            this.fullExtent = this.getFullExtent();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        ++this.m_Position;
    }

    public void addShape(FShape shp, Object[] row) {
        if (shp == null) {
            return;
        }
        FGeometry geom = ShapeFactory.createGeometry((FShape)shp);
        this.addGeometry((IGeometry)geom, row);
    }

    public Rectangle2D getShapeBounds(int index) throws IOException {
        return this.memShapeInfo.getBoundingBox(index);
    }

    public int getShapeType(int index) {
        return this.memShapeInfo.getType(index);
    }

    public IGeometry getShape(int index) {
        IGeometry geom = this.arrayGeometries.get(index);
        return geom.cloneGeometry();
    }

    public int getShapeCount() throws IOException {
        return this.arrayGeometries.size();
    }

    public Rectangle2D getFullExtent() throws IOException {
        return this.fullExtent;
    }

    public abstract int getShapeType();

    public abstract String getName();

    public int getFieldType(int i) throws DriverException {
        if (this.getRowCount() > 1L) {
            Value val = this.getFieldValue(0L, i);
            if (val.getSQLType() == 4) {
                return 6;
            }
            return val.getSQLType();
        }
        if (this.m_TableModel.getColumnClass(i) == String.class) {
            return 12;
        }
        if (this.m_TableModel.getColumnClass(i) == Float.class) {
            return 6;
        }
        if (this.m_TableModel.getColumnClass(i) == Double.class) {
            return 8;
        }
        if (this.m_TableModel.getColumnClass(i) == Double.class) {
            return 4;
        }
        if (this.m_TableModel.getColumnClass(i) == Float.class) {
            return 4;
        }
        if (this.m_TableModel.getColumnClass(i) == Boolean.class) {
            return -7;
        }
        if (this.m_TableModel.getColumnClass(i) == Date.class) {
            return 91;
        }
        return 12;
    }

    public Value getFieldValue(long rowIndex, int fieldId) throws DriverException {
        return (Value)this.m_TableModel.getValueAt((int)rowIndex, fieldId);
    }

    public int getFieldCount() throws DriverException {
        return this.m_TableModel.getColumnCount();
    }

    public String getFieldName(int fieldId) throws DriverException {
        return this.m_TableModel.getColumnName(fieldId);
    }

    public long getRowCount() throws DriverException {
        return this.m_TableModel.getRowCount();
    }

    public void setDataSourceFactory(DataSourceFactory dsf) {
    }

    public void reload() throws DriverException, IOException {
        this.memShapeInfo = new MemoryShapeInfo();
        this.arrayGeometries.clear();
        this.m_TableModel = new DefaultTableModel();
        this.fullExtent = null;
        this.m_Position = 0;
    }

    private void initializeFieldWidth(Object[] row) {
        this.fieldWidth = new int[row.length];
        int i = 0;
        while (i < row.length) {
            this.fieldWidth[i] = row[i] instanceof StringValue && row[i] == null ? 0 : (row[i] == null ? 0 : ((Value)row[i]).getWidth());
            ++i;
        }
    }

    private void actualizeFieldWidth(Object[] row) {
        int i = 0;
        while (i < row.length) {
            int width;
            if (row[i] instanceof StringValue && row[i] != null && this.fieldWidth[i] < (width = ((StringValue)row[i]).getWidth())) {
                this.fieldWidth[i] = width;
            }
            ++i;
        }
    }

    public int getFieldWidth(int fieldId) {
        if (this.fieldWidth == null) {
            return 1;
        }
        return this.fieldWidth[fieldId];
    }
}

