/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.navigation.features;

import com.vividsolutions.jump.feature.Feature;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import org.saig.jump.widgets.navigation.listener.DataModifiedListener;
import org.saig.jump.widgets.util.IFormLayer;

public abstract class AbstractFeaturesNavigationPanel
extends JPanel
implements IFormLayer {
    private List<DataModifiedListener> dataModifiedListeners = new ArrayList<DataModifiedListener>();

    public AbstractFeaturesNavigationPanel(LayoutManager lm) {
        super(lm);
    }

    @Override
    public abstract void disable();

    public abstract void update(Feature var1) throws Exception;

    public abstract void compact(boolean var1);

    public void addDataModifiedListener(DataModifiedListener listener) {
        this.dataModifiedListeners.add(listener);
    }

    public void removeDataModifiedListener(DataModifiedListener listener) {
        this.dataModifiedListeners.remove(listener);
    }

    public void fireDataModified() {
        Iterator<DataModifiedListener> it = this.dataModifiedListeners.iterator();
        while (it.hasNext()) {
            it.next().dataModified();
        }
    }

    protected boolean beforeSave() {
        return true;
    }
}

