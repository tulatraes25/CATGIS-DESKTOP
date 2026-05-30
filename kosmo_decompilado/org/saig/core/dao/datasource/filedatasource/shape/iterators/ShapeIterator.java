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
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileException;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeConnection;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.iterators.ShapeEditionIterator;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class ShapeIterator
implements FeatureIterator {
    private static final Logger LOGGER = Logger.getLogger(ShapeEditionIterator.class);
    private ShapeFileDataSource ds;
    private long total;
    private int contador;
    private Filter filter;
    private Feature readObject;
    private ShapeConnection con;
    private Rectangle2D layerView;
    private boolean isSpatialIndexed;
    private Iterator candidatoIterator;
    private boolean ignored;

    public ShapeIterator(ShapeFileDataSource ds, Filter filter, Envelope envelope) throws IOException, DbfFileException {
        this.ds = ds;
        this.con = ds.getConnection();
        this.con.open();
        this.filter = filter;
        this.total = ds.getNumReg();
        this.contador = 0;
        if (envelope != null) {
            this.layerView = new Rectangle2D.Double(envelope.getMinX() - 1.0E-4, envelope.getMinY() - 1.0E-4, envelope.getWidth() + 2.0E-4, envelope.getHeight() + 2.0E-4);
        } else {
            Rectangle2D extent = ds.getExtent();
            this.layerView = new Rectangle2D.Double(extent.getMinX() - 0.001, extent.getMinY() - 0.001, extent.getWidth() + 0.002, extent.getHeight() + 0.002);
        }
        boolean bl = this.isSpatialIndexed = ds.getSpatialIndex() != null;
        if (this.isSpatialIndexed) {
            List candidatos = ds.getSpatialIndex().query(this.layerView);
            this.candidatoIterator = candidatos == null ? new ArrayList().iterator() : candidatos.iterator();
        }
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

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean hasNext() {
        block20: {
            block19: {
                this.readObject = null;
                if (this.isSpatialIndexed) break block19;
                if (this.layerView == null) ** GOTO lbl35
                while ((long)this.contador < this.total && this.readObject == null) {
                    try {
                        try {
                            bounds = this.con.getShapeBounds(this.contador);
                            if (this.layerView.intersects(bounds)) {
                                this.readObject = this.getFeatureCheckingFilter(this.contador);
                            }
                        }
                        catch (Exception e) {
                            ShapeIterator.LOGGER.error((Object)"", (Throwable)e);
                            ++this.contador;
                            continue;
                        }
                    }
                    catch (Throwable var2_9) {
                        ++this.contador;
                        throw var2_9;
                    }
                    ++this.contador;
                }
                break block20;
lbl-1000:
                // 1 sources

                {
                    try {
                        try {
                            this.readObject = this.getFeatureCheckingFilter(this.contador);
                        }
                        catch (Exception e) {
                            ShapeIterator.LOGGER.error((Object)"", (Throwable)e);
                            ++this.contador;
                            continue;
                        }
                    }
                    catch (Throwable var2_10) {
                        ++this.contador;
                        throw var2_10;
                    }
                    ++this.contador;
lbl35:
                    // 3 sources

                    ** while ((long)this.contador < this.total && this.readObject == null)
                }
lbl36:
                // 1 sources

                break block20;
            }
            if (this.layerView == null) ** GOTO lbl56
            while (this.candidatoIterator.hasNext() && this.readObject == null) {
                try {
                    index = ((Number)this.candidatoIterator.next()).intValue();
                    bounds = this.con.getShapeBounds(index);
                    if (!this.layerView.intersects(bounds)) continue;
                    this.readObject = this.getFeatureCheckingFilter(index);
                }
                catch (Exception e) {
                    ShapeIterator.LOGGER.error((Object)"", (Throwable)e);
                }
            }
            break block20;
lbl-1000:
            // 1 sources

            {
                try {
                    index = ((Number)this.candidatoIterator.next()).intValue();
                    this.readObject = this.getFeatureCheckingFilter(index);
                    continue;
                }
                catch (Exception e) {
                    ShapeIterator.LOGGER.error((Object)"", (Throwable)e);
                }
lbl56:
                // 3 sources

                ** while (this.candidatoIterator.hasNext() && this.readObject == null)
            }
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

    private Feature getFeatureCheckingFilter(int index) throws Exception {
        Feature feat = this.ds.readFeature(index, this.con.getShape(index), true, this.con);
        if (feat != null && this.filter != null && !this.filter.contains(feat)) {
            return null;
        }
        return feat;
    }

    @Override
    public void setIgnoredUpdate(boolean ignored) {
        this.ignored = ignored;
    }
}

