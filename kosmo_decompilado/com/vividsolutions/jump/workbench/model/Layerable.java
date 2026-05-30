/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.model.LayerManager;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.saig.core.styling.Style;
import org.saig.core.util.language.ITranslatable;

public interface Layerable
extends ITranslatable {
    public String getName();

    public void setName(String var1);

    public void setVisible(boolean var1);

    public boolean isVisible();

    public LayerManager getLayerManager();

    public void setLayerManager(LayerManager var1);

    public Blackboard getBlackboard();

    public boolean isEnabled();

    public void setEnabled(boolean var1);

    public IProjection getProjection();

    public void setProjection(IProjection var1);

    public ICoordTrans getCoordTrans();

    public void setCoordTrans(ICoordTrans var1);

    public boolean isRaster();

    public Style getModelStyle();

    public String getMetadata();

    public void setMetadata(String var1);

    public void dispose();

    public void fireAppearanceChanged();
}

