/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import org.saig.core.model.data.Record;

public interface IFormTablePanel {
    public boolean isInputValid();

    public void refresh(Record var1);

    public void update(Record var1, boolean var2);

    public Object save(Object[] var1);

    public void clearForm();
}

