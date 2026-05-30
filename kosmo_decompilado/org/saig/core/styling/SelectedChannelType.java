/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.ContrastEnhancement;
import org.saig.core.styling.StyleVisitor;

public interface SelectedChannelType {
    public void setChannelName(String var1);

    public String getChannelName();

    public void setContrastEnhancement(ContrastEnhancement var1);

    public ContrastEnhancement getContrastEnhancement();

    public void accept(StyleVisitor var1);
}

