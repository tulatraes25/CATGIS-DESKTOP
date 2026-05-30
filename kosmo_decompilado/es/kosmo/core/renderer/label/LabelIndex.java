/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package es.kosmo.core.renderer.label;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.saig.core.renderer.lite.LabelCacheItem;

public class LabelIndex {
    Quadtree index = new Quadtree();

    public boolean labelsWithinDistance(Rectangle2D bounds, double distance) {
        if (distance < 0.0) {
            return false;
        }
        Envelope e = this.toEnvelope(bounds);
        e.expandBy(distance);
        List results = this.index.query(e);
        if (results.size() == 0) {
            return false;
        }
        for (InterferenceItem item : results) {
            if (!item.env.intersects(e)) continue;
            return true;
        }
        return false;
    }

    public void addLabel(LabelCacheItem item, Rectangle2D bounds) {
        Envelope e = this.toEnvelope(bounds);
        this.index.insert(e, new InterferenceItem(e, item));
    }

    private Envelope toEnvelope(Rectangle2D bounds) {
        return new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
    }

    public void reserveArea(List<Rectangle2D> reserved) {
        for (Rectangle2D area : reserved) {
            Envelope env = this.toEnvelope(area);
            InterferenceItem item = new InterferenceItem(env, null);
            this.index.insert(env, item);
        }
    }

    static class InterferenceItem {
        Envelope env;
        LabelCacheItem item;

        public InterferenceItem(Envelope env, LabelCacheItem item) {
            this.env = env;
            this.item = item;
        }
    }
}

