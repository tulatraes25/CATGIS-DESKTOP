/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.task.TaskMonitor;
import java.util.List;
import java.util.Map;
import org.cresques.cts.IProjection;
import org.saig.core.dao.coverage.Coverage;
import org.saig.core.dao.coverage.CoverageFactory;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.styling.Style;
import org.saig.core.util.I18NUnsupportedOperationException;

public class ImageFileConnection
implements Connection {
    @Override
    public FeatureCollection[] executeQuery(String query) throws Exception {
        return this.createFeatureCollectionFromSelection(query, null);
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection proj) throws Exception {
        return this.createFeatureCollectionFromSelection(query, proj);
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection projection, Map<String, Object> properties) throws Exception {
        return this.createFeatureCollectionFromSelection(query, projection);
    }

    @Override
    public FeatureCollection[] executeQuery(String query, List<Exception> exceptions) {
        try {
            return this.createFeatureCollectionFromSelection(query, null);
        }
        catch (Exception e) {
            exceptions.add(e);
            return null;
        }
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style currentStyle) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes);
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style currentStyle, IProjection proj) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes);
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style clone, IProjection proj, TaskMonitor monitor) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes);
    }

    private void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<AbstractDataSource> getDataSources() {
        return null;
    }

    @Override
    public void close() {
    }

    private FeatureCollection[] createFeatureCollectionFromSelection(String selectedPath, IProjection proj) throws Exception {
        FeatureCollection fc = ImageFileConnection.getFeatureCollectionFromCoverage(CoverageFactory.getInstance().buildCoverageFromPath(selectedPath, proj));
        return new FeatureCollection[]{fc};
    }

    public static FeatureCollection getFeatureCollectionFromCoverage(Coverage coverage) throws Exception {
        GeometryFactory geomFact = new GeometryFactory();
        FeatureSchema schema = new FeatureSchema();
        schema.addAttribute("GID", AttributeType.LONG, true);
        schema.addAttribute("GEOM", AttributeType.GEOMETRY);
        schema.addAttribute("IMAGE", AttributeType.OBJECT);
        schema.addAttribute("TRANSPARENCY", AttributeType.FLOAT);
        FeatureDataset fc = new FeatureDataset(schema);
        BasicFeature f = new BasicFeature(schema);
        if (coverage.getEnvelope() == null) {
            throw new Exception("");
        }
        f.setGeometry(geomFact.toGeometry(coverage.getEnvelope()));
        f.setAttribute("IMAGE", (Object)coverage);
        f.setAttribute("TRANSPARENCY", (Object)new Float(255.0f));
        fc.addWithNewKey(f);
        return fc;
    }

    public static Coverage getCoverageFromFeatureCollection(FeatureCollection fc) throws Exception {
        Feature feat;
        Object coverage;
        if (fc instanceof FeatureDataset && fc.getFeatureSchema().hasAttribute("IMAGE") && fc.size() == 1 && (coverage = (feat = fc.getFeatures().get(0)).getAttribute("IMAGE")) instanceof Coverage) {
            return (Coverage)coverage;
        }
        return null;
    }
}

