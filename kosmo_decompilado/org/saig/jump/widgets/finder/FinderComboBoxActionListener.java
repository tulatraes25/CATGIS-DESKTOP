/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.finder;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import org.saig.core.model.feature.FeatureIterator;

public class FinderComboBoxActionListener
implements ActionListener {
    public static String NULL_VALUE = "--------";
    protected Layer layer;
    protected String attr;
    protected JComboBox cb;
    protected JComboBox nextCb;
    protected String nextAttr;
    protected FinderComboBoxActionListener nextActionListener;
    protected String[] attrs;
    protected Object[] ids;

    public FinderComboBoxActionListener(Layer layer, String attr, JComboBox cb, String[] attrs, JComboBox nextCb, String nextAttr, FinderComboBoxActionListener nextActionListener) {
        this.layer = layer;
        this.attr = attr;
        this.cb = cb;
        this.attrs = attrs;
        this.nextActionListener = nextActionListener;
        this.nextAttr = nextAttr;
        this.nextCb = nextCb;
        this.ids = new Object[1];
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = this.cb.getSelectedItem();
        if (obj != null && this.nextCb != null && obj != NULL_VALUE) {
            this.ids[this.ids.length - 1] = obj;
            Set<Object> set = this.executeQuery();
            this.fillNextCb(set);
        } else if (obj != null && obj == NULL_VALUE && this.nextCb != null) {
            this.nextCb.removeAllItems();
            this.nextCb.addItem(NULL_VALUE);
            this.nextCb.setSelectedItem(NULL_VALUE);
        }
    }

    private void fillNextCb(Set<Object> set) {
        this.nextActionListener.setIds(this.ids);
        Object[] objects = set.toArray();
        Arrays.sort(objects);
        this.nextCb.removeAllItems();
        this.nextCb.addItem(NULL_VALUE);
        Object[] objectArray = objects;
        int n = objects.length;
        int n2 = 0;
        while (n2 < n) {
            Object val = objectArray[n2];
            this.nextCb.addItem(val);
            ++n2;
        }
        this.nextCb.setSelectedItem(NULL_VALUE);
    }

    private Set<Object> executeQuery() {
        HashSet<Object> set = new HashSet<Object>();
        if (this.layer.isDataBaseDataSource()) {
            List<Feature> feats = this.layer.getFeatureCollectionWrapper().getByAttribute(this.attrs, this.ids);
            for (Feature feat : feats) {
                set.add(feat.getAttribute(this.nextAttr));
            }
        } else {
            FeatureIterator iterator = this.layer.getFeatureCollectionWrapper().iterator();
            try {
                try {
                    while (iterator.hasNext()) {
                        Feature feat = iterator.next();
                        if (!this.featureEquals(feat, this.attrs, this.ids)) continue;
                        set.add(feat.getAttribute(this.nextAttr));
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    iterator.close();
                }
            }
            finally {
                iterator.close();
            }
        }
        return set;
    }

    private boolean featureEquals(Feature feat, String[] attrs, Object[] ids) {
        boolean ok = true;
        int i = 0;
        while (i < attrs.length) {
            String attr = attrs[i];
            Object id = ids[i];
            ok &= feat.getAttribute(attr) == null ? id == null : feat.getAttribute(attr).equals(id);
            ++i;
        }
        return ok;
    }

    public String getNextAttr() {
        return this.nextAttr;
    }

    public void setNextAttr(String nextAttr) {
        this.nextAttr = nextAttr;
    }

    public FinderComboBoxActionListener getNextActionListener() {
        return this.nextActionListener;
    }

    public void setNextActionListener(FinderComboBoxActionListener nextActionListener) {
        this.nextActionListener = nextActionListener;
    }

    public Layer getLayer() {
        return this.layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public String getAttr() {
        return this.attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public JComboBox getCb() {
        return this.cb;
    }

    public void setCb(JComboBox cb) {
        this.cb = cb;
    }

    public JComboBox getNextCb() {
        return this.nextCb;
    }

    public void setNextCb(JComboBox nextCb) {
        this.nextCb = nextCb;
    }

    public String[] getAttrs() {
        return this.attrs;
    }

    public void setAttrs(String[] attrs) {
        this.attrs = attrs;
    }

    public Object[] getIds() {
        return this.ids;
    }

    public void setIds(Object[] ids) {
        this.ids = new Object[ids.length + 1];
        System.arraycopy(ids, 0, this.ids, 0, ids.length);
    }
}

