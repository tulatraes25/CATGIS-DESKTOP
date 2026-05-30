/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.navigation;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.ILayerIterator;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.model.data.widgets.tables.management.navigation.AbstractNavigationHelper;
import org.saig.jump.lang.I18N;

public class LayerNavigationHelper
extends AbstractNavigationHelper {
    Logger LOGGER = Logger.getLogger(LayerNavigationHelper.class);
    Layer layer;

    public LayerNavigationHelper(Layer layer) {
        this.layer = layer;
        this.type = 1;
        this.setAscendingOrdering(true);
        this.setFilter(null);
        this.setOrderBy(null);
    }

    @Override
    public void setOrderBy(String[] names) {
        if (names == null || names.length == 0) {
            String[] orderAttrs = new String[]{this.layer.getUltimateFeatureCollectionWrapper().getFeatureSchema().getPrimaryKey().getName()};
            super.setOrderBy(orderAttrs);
        } else {
            super.setOrderBy(names);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public List<Object> getElements(int startIndex, int elements) {
        ArrayList<Object> result = new ArrayList<Object>();
        ILayerIterator it = null;
        try {
            it = this.layer.getUltimateFeatureCollectionWrapper().getFullIterator(null, this.filter, this.orderBy, this.ascending);
            Feature feat = it.absolute(startIndex);
            if (feat == null) {
                ArrayList<Object> arrayList = result;
                return arrayList;
            }
            result.add(feat);
            int i = 1;
            feat = it.next();
            while (feat != null) {
                if (i >= elements) {
                    return result;
                }
                result.add(feat);
                feat = it.next();
                ++i;
            }
            return result;
        }
        catch (Exception ex) {
            this.LOGGER.error((Object)"", (Throwable)ex);
            return result;
        }
        finally {
            if (it != null) {
                try {
                    it.close();
                }
                catch (Exception e) {
                    this.LOGGER.error((Object)"", (Throwable)e);
                }
            }
        }
    }

    @Override
    public int getNumElements() {
        int result;
        block12: {
            result = 0;
            ILayerIterator it = null;
            try {
                try {
                    it = this.layer.getUltimateFeatureCollectionWrapper().getFullIterator(null, this.filter, this.orderBy, this.ascending);
                    result = (int)it.size();
                }
                catch (Exception ex) {
                    this.LOGGER.error((Object)I18N.getString(this.getClass(), "an-error-occurred-while-trying-to-obtain-the-number-of-elements"), (Throwable)ex);
                    if (it == null) break block12;
                    try {
                        it.close();
                    }
                    catch (Exception e) {
                        this.LOGGER.error((Object)I18N.getString(this.getClass(), "error-closing-iterator"), (Throwable)e);
                    }
                }
            }
            finally {
                if (it != null) {
                    try {
                        it.close();
                    }
                    catch (Exception e) {
                        this.LOGGER.error((Object)I18N.getString(this.getClass(), "error-closing-iterator"), (Throwable)e);
                    }
                }
            }
        }
        return result;
    }
}

