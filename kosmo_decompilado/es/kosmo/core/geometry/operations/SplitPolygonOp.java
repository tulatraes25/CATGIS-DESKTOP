/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Polygon
 */
package es.kosmo.core.geometry.operations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import es.kosmo.core.geometry.operations.GeometryOp;
import es.kosmo.core.geometry.operations.GeometryOpException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.SplitPolygonsPlugIn;
import org.saig.jump.tools.editing.SplitPolygonsTool;

public class SplitPolygonOp
implements GeometryOp<List<Feature>[]> {
    protected List<Feature> featsToSplit;
    protected List<Feature> featsToAdd;
    protected List<Feature> featsToUpdate;
    protected Geometry splittingGeometry;

    public SplitPolygonOp(List<Feature>[] selectedFeatures, Geometry splitGeom) {
        this.featsToSplit = selectedFeatures[0];
        this.splittingGeometry = splitGeom;
        this.featsToAdd = new ArrayList<Feature>();
        this.featsToUpdate = new ArrayList<Feature>();
    }

    @Override
    public void executeOperation(TaskMonitor monitor) throws GeometryOpException {
        int numSelectedFeats = this.featsToSplit.size();
        int numProcessedFeats = 0;
        try {
            Iterator<Feature> iter = this.featsToSplit.iterator();
            while (!monitor.isCancelRequested() && iter.hasNext()) {
                Feature currentFeature = iter.next();
                monitor.report(numProcessedFeats++, numSelectedFeats, I18N.getString(SplitPolygonsPlugIn.class, "processed-elements"));
                Geometry g = currentFeature.getGeometry();
                if (!(g instanceof Polygon) && !(g instanceof MultiPolygon)) continue;
                Feature clonedFeature = currentFeature.clone(true);
                List<Geometry> div = SplitPolygonsTool.splitPoligon(this.splittingGeometry, g);
                if (div.size() <= 0) continue;
                Geometry a = div.get(0);
                clonedFeature.setGeometry(a);
                this.featsToUpdate.add(clonedFeature);
                if (div.size() <= 1) continue;
                int j = 1;
                while (j < div.size()) {
                    BasicFeature f = new BasicFeature(currentFeature.getSchema());
                    Geometry b = div.get(j);
                    FeatureUtil.copyAttributes(currentFeature, f);
                    f.setGeometry(b);
                    f.setAttribute(currentFeature.getSchema().getPrimaryKeyName(), null);
                    this.featsToAdd.add(f);
                    ++j;
                }
            }
        }
        catch (Exception ex) {
            throw new GeometryOpException(ex.getMessage(), ex);
        }
        if (monitor.isCancelRequested()) {
            this.featsToAdd = null;
            this.featsToUpdate = null;
        } else {
            FeatureUtil.fillZs(this.featsToAdd);
            FeatureUtil.fillZs(this.featsToUpdate);
        }
    }

    @Override
    public List<Feature>[] getResults() {
        List[] results = new List[]{this.featsToAdd, this.featsToUpdate};
        return results;
    }

    @Override
    public void dispose() {
        this.featsToSplit = null;
        this.featsToAdd = null;
        this.featsToUpdate = null;
    }

    public List<Feature>[][] getErrors() {
        return null;
    }
}

