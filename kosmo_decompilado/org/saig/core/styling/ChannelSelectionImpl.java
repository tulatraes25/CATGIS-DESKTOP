/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.styling.ChannelSelection;
import org.saig.core.styling.SelectedChannelType;
import org.saig.core.styling.StyleVisitor;

public class ChannelSelectionImpl
implements ChannelSelection,
Cloneable {
    private SelectedChannelType gray;
    private SelectedChannelType red;
    private SelectedChannelType blue;
    private SelectedChannelType green;

    @Override
    public SelectedChannelType getGrayChannel() {
        return this.gray;
    }

    @Override
    public SelectedChannelType[] getRGBChannels() {
        return new SelectedChannelType[]{this.red, this.green, this.blue};
    }

    @Override
    public SelectedChannelType[] getSelectedChannels() {
        SelectedChannelType[] ret = this.gray == null ? new SelectedChannelType[]{this.red, this.green, this.blue} : new SelectedChannelType[]{this.gray};
        return ret;
    }

    @Override
    public void setGrayChannel(SelectedChannelType gray) {
        this.gray = gray;
    }

    @Override
    public void setRGBChannels(SelectedChannelType[] channels) {
        if (channels.length != 3) {
            throw new IllegalArgumentException("Three channels are required in setRGBChannels, got " + channels.length);
        }
        this.red = channels[0];
        this.green = channels[1];
        this.blue = channels[2];
    }

    @Override
    public void setRGBChannels(SelectedChannelType red, SelectedChannelType green, SelectedChannelType blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public void setSelectedChannels(SelectedChannelType[] channels) {
        if (channels.length == 1) {
            this.gray = channels[0];
        } else if (channels.length == 3) {
            this.red = channels[0];
            this.green = channels[1];
            this.blue = channels[2];
        } else {
            throw new IllegalArgumentException("Wrong number of elements in setSelectedChannels, expected 1 or 3, got " + channels.length);
        }
    }

    public Object clone() {
        ChannelSelectionImpl clone = new ChannelSelectionImpl();
        if (this.gray != null) {
            clone.setGrayChannel((SelectedChannelType)((Cloneable)this.gray).clone());
        }
        if (this.blue != null) {
            clone.blue = (SelectedChannelType)((Cloneable)this.blue).clone();
        }
        if (this.green != null) {
            clone.green = (SelectedChannelType)((Cloneable)this.green).clone();
        }
        if (this.red != null) {
            clone.red = (SelectedChannelType)((Cloneable)this.red).clone();
        }
        return clone;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof ChannelSelectionImpl) {
            ChannelSelectionImpl other = (ChannelSelectionImpl)oth;
            return Utilities.equals((Object)this.blue, (Object)other.blue) && Utilities.equals((Object)this.gray, (Object)other.gray) && Utilities.equals((Object)this.green, (Object)other.green) && Utilities.equals((Object)this.red, (Object)other.red);
        }
        return false;
    }
}

