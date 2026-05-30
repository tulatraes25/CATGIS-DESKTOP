/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.filedatasource.shape.iterators;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileException;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeConnection;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;

public class ShapeEditionIterator
implements FeatureIterator {
    private static final Logger LOGGER = Logger.getLogger(ShapeEditionIterator.class);
    private ShapeFileDataSource ds;
    private long total;
    private long onDisk;
    private int contador;
    private Filter filter;
    private Envelope envelope;
    private Feature readObject;
    private ShapeConnection con;
    private Rectangle2D layerView;
    private boolean isSpatialIndexed;
    private Iterator candidatoIterator;
    private Iterator<Feature> editablesIterator;
    private BitSet processedFeatures;
    private boolean ignored;

    public ShapeEditionIterator(ShapeFileDataSource ds, long total, Filter filter, Envelope envelope) throws IOException, DbfFileException {
        this.ds = ds;
        this.con = ds.getConnection();
        this.con.open();
        this.filter = filter;
        this.envelope = envelope;
        if (envelope != null) {
            this.layerView = new Rectangle2D.Double(envelope.getMinX() - 1.0E-4, envelope.getMinY() - 1.0E-4, envelope.getWidth() + 2.0E-4, envelope.getHeight() + 2.0E-4);
        }
        this.total = total;
        this.onDisk = ds.getShapeCount();
        this.contador = 0;
        boolean bl = this.isSpatialIndexed = ds.getSpatialIndex() != null;
        if (this.isSpatialIndexed) {
            List candidatos = ds.getSpatialIndex().query(this.layerView);
            if (candidatos == null) {
                this.candidatoIterator = new ArrayList().iterator();
                total = 0L;
            } else {
                this.candidatoIterator = candidatos.iterator();
                total = candidatos.size();
            }
            this.editablesIterator = ds.getEditableFeatures().iterator();
        }
        this.processedFeatures = new BitSet(ds.getShapeCount());
    }

    public ShapeEditionIterator(ShapeFileDataSource ds, Filter filter, Envelope envelope) throws IOException, DbfFileException {
        this.ds = ds;
        this.con = ds.getConnection();
        this.con.open();
        this.filter = filter;
        this.envelope = envelope;
        this.total = ds.iterableRows();
        this.onDisk = ds.getShapeCount();
        this.contador = 0;
        this.layerView = envelope != null ? new Rectangle2D.Double(envelope.getMinX() - 1.0E-4, envelope.getMinY() - 1.0E-4, envelope.getWidth() + 2.0E-4, envelope.getHeight() + 2.0E-4) : ds.getExtent();
        boolean bl = this.isSpatialIndexed = ds.getSpatialIndex() != null;
        if (this.isSpatialIndexed) {
            List candidatos = ds.getSpatialIndex().query(this.layerView);
            this.candidatoIterator = candidatos == null ? new ArrayList().iterator() : candidatos.iterator();
            this.editablesIterator = ds.getEditableFeatures().iterator();
        }
        this.processedFeatures = new BitSet(ds.getShapeCount());
    }

    @Override
    public void close() {
        LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.iterators.ShapeIterator.Closing-shape-connection"));
        try {
            this.con.close();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public void remove() {
        throw new I18NUnsupportedOperationException();
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean hasNext() {
        block22: {
            this.readObject = null;
            if (this.isSpatialIndexed) ** GOTO lbl53
            while ((long)this.contador < this.total && this.readObject == null) {
                block19: {
                    block21: {
                        block20: {
                            bounds = null;
                            if ((long)this.contador < this.onDisk && !this.ds.isUpdatedOrDeleted(this.contador)) {
                                bounds = this.con.getShapeBounds(this.contador);
                            }
                            if (this.layerView == null) ** GOTO lbl17
                            if (bounds != null && !this.layerView.intersects(bounds) || (feat = this.ds.getRealFeature(this.contador, this.con, this.ignored)) == null) break block19;
                            if (this.filter == null || this.filter.contains(feat)) break block20;
                            ++this.contador;
                            continue;
                        }
                        this.readObject = feat;
                        break block19;
lbl17:
                        // 1 sources

                        feat = this.ds.getRealFeature(this.contador, this.con, this.ignored);
                        if (feat == null) break block19;
                        if (this.filter == null || this.filter.contains(feat)) break block21;
                        ++this.contador;
                        continue;
                    }
                    try {
                        try {
                            this.readObject = feat;
                        }
                        catch (Exception e) {
                            ShapeEditionIterator.LOGGER.error((Object)"", (Throwable)e);
                            ++this.contador;
                            continue;
                        }
                    }
                    catch (Throwable var3_10) {
                        ++this.contador;
                        throw var3_10;
                    }
                }
                ++this.contador;
            }
            break block22;
lbl-1000:
            // 1 sources

            {
                try {
                    index = ((Number)this.candidatoIterator.next()).intValue();
                    bounds = null;
                    if (!this.ds.isUpdatedOrDeleted(index)) {
                        bounds = this.con.getShapeBounds(index);
                    }
                    if (this.layerView != null) {
                        if (bounds != null && !this.layerView.intersects(bounds) || (feat = this.ds.getRealFeature(index, this.con, this.ignored)) == null || this.filter != null && !this.filter.contains(feat)) continue;
                        this.readObject = feat;
                        continue;
                    }
                    feat = this.ds.getRealFeature(index, this.con, this.ignored);
                    if (feat == null || this.filter != null && !this.filter.contains(feat)) continue;
                    this.readObject = feat;
                    continue;
                }
                catch (Exception e) {
                    ShapeEditionIterator.LOGGER.error((Object)"", (Throwable)e);
                }
lbl53:
                // 6 sources

                ** while (this.candidatoIterator.hasNext() && this.readObject == null)
            }
lbl54:
            // 1 sources

            if (this.readObject == null) {
                while (this.editablesIterator.hasNext() && this.readObject == null) {
                    try {
                        feat = this.editablesIterator.next();
                        if (this.processedFeatures.get(feat.getPrimaryKeyAsInt())) continue;
                        if (this.envelope != null) {
                            if (!this.envelope.intersects(feat.getGeometry().getEnvelopeInternal()) || this.filter != null && !this.filter.contains(feat)) continue;
                            this.readObject = feat;
                            continue;
                        }
                        if (this.filter != null && !this.filter.contains(feat)) continue;
                        this.readObject = feat;
                    }
                    catch (Exception e) {
                        ShapeEditionIterator.LOGGER.error((Object)"", (Throwable)e);
                    }
                }
            }
        }
        if (this.readObject != null) {
            this.processedFeatures.set(this.readObject.getPrimaryKeyAsInt());
        }
        return this.readObject != null;
    }

    @Override
    public Feature next() {
        return this.readObject;
    }

    @Override
    public void close(boolean isCancel) {
        this.close();
    }

    @Override
    public void setIgnoredUpdate(boolean ignored) {
        this.ignored = ignored;
    }
}

