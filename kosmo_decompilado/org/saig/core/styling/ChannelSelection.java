/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.SelectedChannelType;
import org.saig.core.styling.StyleVisitor;

public interface ChannelSelection {
    public void setRGBChannels(SelectedChannelType var1, SelectedChannelType var2, SelectedChannelType var3);

    public void setRGBChannels(SelectedChannelType[] var1);

    public SelectedChannelType[] getRGBChannels();

    public void setGrayChannel(SelectedChannelType var1);

    public SelectedChannelType getGrayChannel();

    public void setSelectedChannels(SelectedChannelType[] var1);

    public SelectedChannelType[] getSelectedChannels();

    public void accept(StyleVisitor var1);
}

