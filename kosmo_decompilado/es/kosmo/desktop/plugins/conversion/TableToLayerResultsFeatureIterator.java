/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.Point
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.conversion;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import es.kosmo.core.dao.datasource.memory.AbstractResultsFeatureIterator;
import es.kosmo.desktop.widgets.conversion.TableToLayerDialog;
import org.apache.log4j.Logger;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.RecordToFeatureWrapper;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.iterators.ITableIterator;

public class TableToLayerResultsFeatureIterator
extends AbstractResultsFeatureIterator {
    private static final Logger LOGGER = Logger.getLogger(TableToLayerResultsFeatureIterator.class);
    protected FeatureCollection sourceFC;
    protected FeatureSchema resultSchema;
    protected Table sourceTable;
    protected ITableIterator itRecords;
    protected Record currentSourceRecord;
    protected boolean isPointTarget;
    protected boolean hasZ;
    protected String sPointX;
    protected String sPointY;
    protected String sPointZ;
    protected String sEnvXMin;
    protected String sEnvXMax;
    protected String sEnvYMin;
    protected String sEnvYMax;
    protected int errorCounter;

    public TableToLayerResultsFeatureIterator(TableToLayerDialog optionsDialog, FeatureSchema schema, Table table) {
        this.resultSchema = schema;
        this.sourceTable = table;
        this.isPointTarget = optionsDialog.isToPoint();
        boolean bl = this.hasZ = optionsDialog.getSelectedPointZName() != null;
        if (this.isPointTarget) {
            this.sPointX = optionsDialog.getSelectedPointXName();
            this.sPointY = optionsDialog.getSelectedPointYName();
            this.sPointZ = optionsDialog.getSelectedPointZName();
        } else {
            this.sEnvXMin = optionsDialog.getSelectedEnvXMinName();
            this.sEnvXMax = optionsDialog.getSelectedEnvXMaxName();
            this.sEnvYMin = optionsDialog.getSelectedEnvYMinName();
            this.sEnvYMax = optionsDialog.getSelectedEnvYMaxName();
        }
    }

    @Override
    protected void initialize() throws Exception {
        try {
            this.itRecords = this.sourceTable.getDataSource().getIterator();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.errorCounter = 0;
    }

    @Override
    protected Feature generateNextFeature() throws Exception {
        while (this.currentSourceRecord == null && this.itRecords.hasNext()) {
            this.currentSourceRecord = this.itRecords.next();
        }
        if (this.currentSourceRecord == null) {
            return null;
        }
        BasicFeature resultFeat = new BasicFeature(this.resultSchema);
        FeatureUtil.copyAttributes(new RecordToFeatureWrapper(this.currentSourceRecord), resultFeat);
        resultFeat.setGeometry(this.buildGeometry(this.currentSourceRecord));
        this.currentSourceRecord = null;
        return resultFeat;
    }

    private Geometry buildGeometry(Record sourceRecord) {
        Point result = null;
        if (this.isPointTarget) {
            Coordinate coord = null;
            double dX = this.getValue("X", sourceRecord);
            double dY = this.getValue("Y", sourceRecord);
            if (this.hasZ) {
                double dZ = this.getValue("Z", sourceRecord);
                if (!(Double.isNaN(dX) || Double.isNaN(dY) || Double.isNaN(dZ))) {
                    coord = new Coordinate(dX, dY, dZ);
                } else {
                    ++this.errorCounter;
                }
            } else if (!Double.isNaN(dX) && !Double.isNaN(dY)) {
                coord = new Coordinate(dX, dY);
            } else {
                ++this.errorCounter;
            }
            if (coord != null) {
                result = this.geomFact.createPoint(coord);
            }
        } else {
            double dXMin = this.getValue("XMin", sourceRecord);
            double dXMax = this.getValue("XMax", sourceRecord);
            double dYMin = this.getValue("YMin", sourceRecord);
            double dYMax = this.getValue("YMax", sourceRecord);
            if (Double.isNaN(dXMin) || Double.isNaN(dXMax) || Double.isNaN(dYMin) || Double.isNaN(dYMax)) {
                ++this.errorCounter;
            } else if (dXMin >= dXMax || dYMin >= dYMax) {
                ++this.errorCounter;
            } else {
                Coordinate[] coords = new Coordinate[]{new Coordinate(dXMin, dYMax), new Coordinate(dXMax, dYMax), new Coordinate(dXMax, dYMin), new Coordinate(dXMin, dYMin), new Coordinate(dXMin, dYMax)};
                result = this.geomFact.createPolygon(this.geomFact.createLinearRing(coords), null);
            }
        }
        return result;
    }

    private double getValue(String c, Record record) {
        double value;
        String field = null;
        if (c.equals("X")) {
            field = this.sPointX;
        } else if (c.equals("Y")) {
            field = this.sPointY;
        } else if (c.equals("Z")) {
            field = this.sPointZ;
        } else if (c.equals("XMin")) {
            field = this.sEnvXMin;
        } else if (c.equals("XMax")) {
            field = this.sEnvXMax;
        } else if (c.equals("YMin")) {
            field = this.sEnvYMin;
        } else if (c.equals("YMax")) {
            field = this.sEnvYMax;
        } else {
            return Double.NaN;
        }
        try {
            value = Double.valueOf(record.getAttribute(field).toString());
        }
        catch (Exception e) {
            value = Double.NaN;
        }
        return value;
    }

    @Override
    public int size() throws Exception {
        return (int)this.sourceTable.size();
    }

    @Override
    public void reset() {
        if (this.itRecords != null) {
            this.itRecords.close();
        }
        try {
            this.itRecords = this.sourceTable.getDataSource().getIterator();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.currentSourceRecord = null;
        this.errorCounter = 0;
    }

    @Override
    public void close() {
        this.nextFeat = null;
        if (this.itRecords != null) {
            this.itRecords.close();
        }
    }

    @Override
    public void close(boolean isCancel) {
        this.nextFeat = null;
        if (this.itRecords != null) {
            this.itRecords.close();
        }
    }

    public int getNumErrors() {
        return this.errorCounter;
    }
}

