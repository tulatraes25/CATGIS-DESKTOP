/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.plugin.clipboard;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;

public abstract class LayerableClipboardPlugIn
extends AbstractPlugIn {
    protected Layerable cloneLayerable(Layerable layerable) {
        if (layerable instanceof Layer) {
            return (Layer)((Layer)layerable).clone();
        }
        if (layerable instanceof WMSLayer) {
            try {
                return (Layerable)((WMSLayer)layerable).clone();
            }
            catch (CloneNotSupportedException e) {
                Assert.shouldNeverReachHere();
            }
        }
        Assert.shouldNeverReachHere();
        return null;
    }
}

