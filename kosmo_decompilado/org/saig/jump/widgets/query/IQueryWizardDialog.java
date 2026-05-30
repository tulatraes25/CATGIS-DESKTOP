/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.query;

import org.saig.core.filter.Filter;

public interface IQueryWizardDialog {
    public void setFilter(Filter var1);

    public Filter getFilter();

    public boolean exitOk();

    public String getRawText();
}

