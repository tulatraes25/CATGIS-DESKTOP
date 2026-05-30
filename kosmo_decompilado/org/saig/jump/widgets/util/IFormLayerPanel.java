/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.feature.Feature;

public interface IFormLayerPanel {
    public boolean isInputValid();

    public void refresh(Feature var1);

    public void update(Feature var1, boolean var2) throws Exception;

    public Object save(Object[] var1) throws Exception;

    public void clearForm();
}

